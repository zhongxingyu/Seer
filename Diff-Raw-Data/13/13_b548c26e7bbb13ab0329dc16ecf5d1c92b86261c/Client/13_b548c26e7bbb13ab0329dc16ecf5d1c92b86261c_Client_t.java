 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class Client implements Constants {
 	public static int		indexCommand;
 	public String			serverIp;
 	public int				serverPort;
 	public byte[]			buffer;
 	public DatagramSocket	socket;
 	public InetAddress		server;
 	public Command			command;
 	public Thread			registerThread;
 	public double			msgLostProb;
 
 	/* Timeout for sending a datagram, in seconds */
 	public int				sendTimeout;
 
 	public Client(String serverIp, int serverPort) throws SocketException, UnknownHostException {
 		this.serverIp = serverIp;
 		this.serverPort = serverPort;
 		this.buffer = new byte[BUFFER_SIZE];
 		this.socket = new DatagramSocket();
 		this.server = InetAddress.getByName(this.serverIp);
 		this.sendTimeout = 10000;
 		this.msgLostProb = 0.5;
 	}
 
 	public void send(byte[] bytes, int length) throws IOException {
 		DatagramPacket request = new DatagramPacket(bytes, length, this.server, this.serverPort);
 		this.socket.send(request);
 	}
 
 	public void send(String str) throws IOException {
 		send(str.getBytes(), str.length());
 	}
 
 	public void recv() throws IOException {
 		DatagramPacket request = new DatagramPacket(this.command.buffer, this.command.buffer.length);
 		this.socket.receive(request);
 	}
 
 	public static void main(String args[]) throws IOException {
 		String serverIp = "127.0.0.1";
 		// String serverIp = "192.168.1.14";
 		int serverPort = 6789;
 		indexCommand = 0;
 		Client client = new Client(serverIp, serverPort);
 		client.displayMenu();
 	}
 
 	/**
 	 * `
 	 * 
 	 * @param cmdReadFile
 	 * @throws IOException
 	 */
 	private void send(Command command) throws IOException {
 		send(command + "");
 	}
 
 	public int displayCommands() {
 		System.out.println("**********");
 		System.out.println("1. Read from a file");
 		System.out.println("2. Write to a file");
 		System.out.println("3. Monitor a file on server");
 		System.out.println("4. List file in a directory");
 		System.out.println("5. Delete file in server");
		System.out.println("6. Simulate message lost (read a file)");
		System.out.println("7. Simulate message lost (write a file)");
 		System.out.println("8. Exit");
 		System.out.println("**********");
 		System.out.print("Your command?  ");
 		Scanner sc = new Scanner(System.in);
 		int result = sc.nextInt();
 		return result;
 	}
 
 	public void displayMenu() throws IOException {
 		int choiceCode = displayCommands();
 		boolean choiceValid = true;
 		while (choiceCode != OPT_EXIT) {
 			switch (choiceCode) {
 			case OPT_SIMLOST_IDEM:
 			case OPT_READFILE:
 				command = new CommandReadFile();
 				break;
 			case OPT_SIMLOST_NONIDEM:
 			case OPT_WRITEFILE:
 				command = new CommandWriteFile();
 				break;
 			case OPT_REGISTER:
 				command = new CommandRegister();
 				break;
 			case OPT_DIRECTORY:
 				command = new CommandListDir();
 				break;
 			case OPT_DELETE:
 				command = new CommandDelete();
 				break;
 			default:
 				System.out.println("Invalid command...");
 				choiceValid = false;
 			}
 
 			/* General command flow: - Request related data - Send request -
 			 * Recv reply. Depend on command type, additional data will be
 			 * requested or done accordingly */
 			if (choiceValid) {
 				/* Step1: Request data */
 				if (choiceCode == OPT_DIRECTORY) {
 					this.send(command);
 					this.recv();
 					command.processReply();
 					command.isServed = false;
 
 				}
 				command.requestData();
 				indexCommand++;
 
 				/* only if the command can not be served locally, client send
 				 * the request to server. IMPORTANT: In checking for lost
 				 * simulation, remember to wait until the cache is expired,
 				 * otherwise the data will always be served from cache */
 				if (!command.isServed) {
 					this.send(command);
 					this.recv();
 					if (choiceCode == OPT_SIMLOST_IDEM || choiceCode == OPT_SIMLOST_NONIDEM) {
 						try {
 							Thread.sleep(sendTimeout);
 							System.out.println("[error] Send request timeout");
 							System.out.println("[info] Resending request");
 							this.send(command);
 							this.recv();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					command.processReply();
 				}
 
 				/* In case of registering for monitoring a new file... */
 				if (choiceCode == OPT_REGISTER) {
 					/* set time out so that it won't wait forever */
 					socket.setSoTimeout(((CommandRegister) command).interval * 100);
 					Timer timer = new Timer();
 					timer.schedule(new TimerTask(){
 						public void run() {
 							((CommandRegister) command).timeElapsed = true;
 						}
 					}, ((CommandRegister) command).interval * 1000);
 
 					while (!((CommandRegister) command).timeElapsed)
 						try {
 							recv();
 							command.processReply();
 						} catch (SocketTimeoutException e) {} catch (IOException e) {
 							e.printStackTrace();
 						}
 					/* reset this value for waiting infinitely */
 					socket.setSoTimeout(0);
 					System.out.println("[info] Register period elapsed. Monitor removed");
 				} else if (choiceCode == OPT_DIRECTORY) {
 					command.requestData();
 					while (!((CommandListDir) command).directory.trim().equalsIgnoreCase("quit")) {
 						indexCommand++;
 						this.send(command);
 						this.recv();
 						command.processReply();
 						command.requestData();
 					}
 				}
 			}
 
 			choiceCode = displayCommands();
 		}
 	}
 }
