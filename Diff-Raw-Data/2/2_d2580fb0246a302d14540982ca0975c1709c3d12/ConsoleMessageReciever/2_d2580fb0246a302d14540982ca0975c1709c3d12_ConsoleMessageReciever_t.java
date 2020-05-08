 package me.heldplayer.GitIRC;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import me.heldplayer.GitIRC.client.IncomingMessage;
 
 public class ConsoleMessageReciever extends MessageReciever {
 
 	public static ConsoleMessageReciever instance;
 	private boolean running = false;
 	private final BufferedReader in;
 	private String nick;
	protected volatile HashMap<Integer, String> inputBuffer;
 	protected int index = 0;
 
 	public ConsoleMessageReciever() {
 		in = new BufferedReader(new InputStreamReader(System.in));
 		inputBuffer = new HashMap<Integer, String>();
 	}
 
 	public void recieve(String message) {
 		IncomingMessage msg = new IncomingMessage(message);
 
 		if (!msg.parse(this)) {
 			System.out.println(message);
 		}
 	}
 
 	public String getNick() {
 		return nick;
 	}
 
 	public void setNick(String newNick) {
 		nick = newNick;
 		send("NICK " + nick);
 	}
 
 	public void stop() throws IOException {
 		running = false;
 		client.in.close();
 		client.out.close();
 		client.socket.close();
 	}
 
 	public void init() throws IOException {
 		System.out.print("Enter an IRC server to connect to: ");
 
 		String adress = in.readLine();
 
 		System.out.print("Enter a nickname: ");
 
 		nick = in.readLine();
 
 		super.init(adress);
 
 		send("CAP LS");
 		send("NICK " + nick);
 		send("USER GitIRC 0 * :" + nick);
 
 		running = true;
 	}
 
 	public void parse() throws IOException {
 		for (Entry<Integer, String> entry : inputBuffer.entrySet()) {
 			String command = entry.getValue();
 			
 			if(command.startsWith("/join")){
 				send("JOIN " + command.split(" ")[1]);
 				continue;
 			} else if(command.startsWith("/me")){
 				String[] args = command.split(" ");
 				
 				String result = "";
 				for (int i = 2; i < args.length; i++) {
 					if (i != 2) {
 						result += " ";
 					}
 					result += args[i];
 				}
 				
 				send("PRIVMSG " + args[1] + " :\u0001ACTION " + result + "\u0001");
 				System.out.println("[" + args[1] + "] * " + nick + " " + result);
 				continue;
 			} else if(command.startsWith("/say")){
 				String[] args = command.split(" ");
 				
 				String result = "";
 				for (int i = 2; i < args.length; i++) {
 					if (i != 2) {
 						result += " ";
 					}
 					result += args[i];
 				}
 				
 				send("PRIVMSG " + args[1] + " :" + result);
 				System.out.println("[" + args[1] + "] <" + nick + "> " + result);
 				continue;
 			} else if(command.startsWith("/quit")){
 				String[] args = command.split(" ");
 				
 				String result = "";
 				for (int i = 1; i < args.length; i++) {
 					if (i != 1) {
 						result += " ";
 					}
 					result += args[i];
 				}
 				
 				send("QUIT :" + result);
 				stop();
 				return;
 			} else if(command.startsWith("/setupbot")){
 				ThreadCommitReader commitReader = new ThreadCommitReader(this, command.substring(10));
 				commitReader.setDaemon(true);
 				commitReader.start();
 				
 				continue;
 			}
 			
 			send(command);
 		}
 
 		inputBuffer.clear();
 
 		super.parse();
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public static void main(String[] args) {
 		instance = new ConsoleMessageReciever();
 
 		try {
 			instance.init();
 		} catch (IOException ex) {
 			System.err.println("Unexpected IO error! Stopping!");
 			ex.printStackTrace();
 			try {
 				instance.stop();
 			} catch (IOException e) {
 			}
 		}
 		
 		ThreadCommandReader commandReader = new ThreadCommandReader(instance);
 		commandReader.setDaemon(true);
 		commandReader.start();
 
 		while (instance.running) {
 			try {
 				instance.parse();
 				Thread.sleep(1L);
 			} catch (InterruptedException ex) {
 			} catch (Exception ex) {
 				System.err.println("Unexpected error! Stopping!");
 				ex.printStackTrace();
 				try {
 					instance.stop();
 				} catch (IOException e) {
 				}
 			}
 		}
 
 		System.out.println("Disconneted.");
 	}
 }
