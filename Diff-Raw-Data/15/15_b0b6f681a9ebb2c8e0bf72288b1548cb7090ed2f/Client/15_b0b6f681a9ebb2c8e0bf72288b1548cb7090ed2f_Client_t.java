 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 
 public class Client implements Constants {
 	public static int		indexCommand;
 	public String			serverIp;
 	public int				serverPort;
 	public byte[]			buffer;
 	public DatagramSocket	socket;
 	public InetAddress		server;
 	public Command			command;
 
 	public Client(String serverIp, int serverPort) throws SocketException, UnknownHostException {
 		this.serverIp = serverIp;
 		this.serverPort = serverPort;
 		this.buffer = new byte[BUFFER_SIZE];
 		this.socket = new DatagramSocket();
 		this.server = InetAddress.getByName(this.serverIp);
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
 	private void send(Command cmdReadFile) throws IOException {
 		send(cmdReadFile + "");
 	}
 
 	public int displayCommands() {
 		System.out.println("**********");
 		System.out.println("1. Read from a file -- " + OPT_READFILE);
 		System.out.println("2. Write to a file -- " + OPT_WRITEFILE);
 		System.out.println("3. List all files");
 		System.out.println("4. Exit -- " + OPT_EXIT);
 		System.out.println("**********");
 		System.out.print("Your command?  ");
 		Scanner sc = new Scanner(System.in);
 		int result = sc.nextInt();
 		return result;
 	}
 
 	public void displayMenu() throws IOException {
 		int choiceCode = displayCommands();
 
 		while (choiceCode != OPT_EXIT) {
 			switch (choiceCode) {
 			case OPT_READFILE:
 				command = new CommandReadFile();
 				command.requestData();
 				indexCommand++;
 				break;
 			case OPT_WRITEFILE:
 				command = new CommandWriteFile();
 				command.requestData();
 				indexCommand++;
 				break;
 			case OPT_REGISTER:
 				command = new CommandRegister();
 				command.requestData();
 				indexCommand++;
 				break;
 			default:
 				System.out.println("Invalid command...");
 			}
 			this.send(command);
 			this.recv();
 			command.processReply();
			if (choiceCode == OPT_REGISTER) {
				while (true) {
 					this.recv();
 					command.processReply();
 				}
 			}
 			choiceCode = displayCommands();
 		}
 	}
 }
