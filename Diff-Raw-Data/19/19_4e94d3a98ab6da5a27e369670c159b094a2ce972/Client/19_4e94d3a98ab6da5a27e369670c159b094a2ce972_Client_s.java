 package de.icetruck;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JOptionPane;
 
 public class Client implements Runnable {
 	private Socket s_;
 	private boolean running_;
 	
 	Client() {
 		running_ = true;
 	}
 
 	public void setRunning(boolean running) {
 		running_ = running;
 	}
 	
 	public void sendMessage(String msg) {
 		try {
 			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s_.getOutputStream()));
 			writer.write("MSG " + msg.replace(";", "\\;") + ";");			
 			writer.flush();
 		} catch(IOException e) {
 			return;
 		}
 	}
 	
 	@Override
 	public void run() {
 		Application.getInstance().addChatLine("Connecting...");
 		try {
 			s_ = new Socket(Application.getInstance().getHost(), Application.getInstance().getPort());
 		} catch(UnknownHostException e) {
 			JOptionPane.showMessageDialog(Application.getInstance(), "Unknown host (" + Application.getInstance().getHost() + ").");
 			return;
 		} catch(IOException e) {
 			JOptionPane.showMessageDialog(Application.getInstance(), "Could not connect to server (" + Application.getInstance().getHost() + ":" + Application.getInstance().getPort() + ").");
 			return;
 		}
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(s_.getInputStream()));
 			// writer = new BufferedWriter(new OutputStreamWriter(s_.getOutputStream()));
 		} catch(IOException e) {
 			JOptionPane.showMessageDialog(Application.getInstance(), "Broken pipe");
 			return;
 		}
 		while(running_) {
 			try {
 				String line = "";
 				do {
 					line += reader.readLine();
				} while(line.endsWith("\\;") || !line.endsWith(";"));
 				// Application.getInstance().addChatLine(line);
 				
 				System.out.println(line.replace(" ", "~"));
 
 				Command c = null;
 				try {
 					int cmdend = line.indexOf(" ".charAt(0));
 					String cmd = line.substring(0, cmdend);
 
					c = new Command(cmd, line.substring(cmdend + 1, line.lastIndexOf(";")));
 				} catch(StringIndexOutOfBoundsException e) {
 					
 				}
 				Application.getInstance().addCommand(c);
 				// Application.getInstance().addChatLine(cmd);
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(Application.getInstance(), "Broken pipe");
 				return;
 			}
 		}
 	}
 }
