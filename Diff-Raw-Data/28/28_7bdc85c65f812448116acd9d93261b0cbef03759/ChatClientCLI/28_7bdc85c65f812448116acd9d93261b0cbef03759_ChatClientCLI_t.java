 package ch.hszt.groupf.fallstudie.client.cli;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Currency;
 
 import ch.hszt.groupf.fallstudie.client.controller.ClientController;
 import ch.hszt.groupf.fallstudie.client.controller.IfcUserInterface;
 import ch.hszt.groupf.fallstudie.client.log.LogFactory;
 
 
 public class ChatClientCLI implements IfcUserInterface {
 	private boolean _exitCLI = false;
 	private final ClientController _controller;
 
 	public ChatClientCLI(ClientController inClientController) {
 		_controller = inClientController;
 		runSubshell();
 	}
 	
 	public ChatClientCLI(ClientController inClientController, boolean testing) {
 		_controller = inClientController;
 	}
 
 	private void runSubshell() {
 		String inText = "";
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
 
 		welcomeMsg();
 		while (!_exitCLI) {
 
 			try {
 				msgParser(in.readLine());
 //				inText = in.readLine();
 //				if (inText.contains("\\q")) {
 //					_exitCLI = true;
 //				}
 				// out.write(strLine, 0, strLine.length());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 //			} finally {
 //
 			}
 
 		}
 		try {
 			in.close();
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void msgParser(String inText)  {
 		// check whether a command is executed
 		String[] currentLine = inText.split(" ");
 
 		if (currentLine[0].startsWith("\\")) {
			currentLine[0] = currentLine[0].replaceFirst("\\\\", "");
 			String command = currentLine[0];
 			
 			if (command.equals("quit")) {
 				System.out.println("quit");
 				_exitCLI = true;
 			} else if (command.equals("connect")){
 				System.out.println("connect");
 				//_controller.connect(currentLine[1], currentLine[2], username)
 			} else if (command.equals("username")) {
 				System.out.println("Set Username");
 			} else if (command.equals("logfile")) {
 				try {
 					_controller.setLogger(new File(currentLine[1]));
 					_controller.turnLogOn();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else if (command.equals("log:on")) {
 				_controller.turnLogOn();
 			} else if (command.equals("log:off" )) {
 				_controller.turnLogOff();
 			} else {
 				System.out.println("command not found");
 			}
 		
 		// check whether the message is private nor not		
 		} else if (currentLine[0].startsWith("/")) {
 			currentLine[0] = currentLine[0].replaceFirst("/", "");
 			String receipt = currentLine[0];
 			System.out.println("sending msg to user " + receipt);
 		} else {
 			System.out.println("sending msg to all users");
 		}
 		
 			//		if (inText.startsWith("/quit")) {
 //			return "Exit Command";
 //		} else if (){
 //			
 //		} else {
 //			return "Command not found";
 //		}
 	}
	
	public void postMsg(String line) {
		msgParser(line);
	}
 	private void welcomeMsg() {
 
 		System.out.println("Welcome to the CLI-Chat Client IRCv2" + System.getProperty("line.separator"));
 		// TODO print out the help (possible commands)
 	}
 
 	public void onDisconnected(Exception ex) {
 		// TODO evtl. use a write-buffer
 		System.out.println("Connection lost!");
 
 	}
 
 	public void onReceivedMsg(String inMessage) {
 		// TODO evtl. use a write-buffer
 		System.out.println(inMessage);
 	}
 
 	public void displayConnStatus(String connectionStatus) {
 		// TODO evtl. use a write-buffer
 		// System.out.println(connectionStatus);
 	}
 
 	/**
 	 * This method is just used for JUnit Tests
 	 */
 	@Override
 	public void setLoggeronController(File file) throws IOException, NullPointerException{
 		_controller.setLogger(file);
 		
 	}
 
 	@Override
 	public LogFactory getLoggeronController() {
 		return _controller.getLogger();
 	}
 
 	@Override
 	public String getChatClientString() {
 		return "CLI";
 	}
 
 }
