 package grouppractical.client;
 
 import grouppractical.client.commands.Command;
 import grouppractical.client.commands.CommandParser;
 import grouppractical.client.commands.CommandType;
 import grouppractical.client.commands.MPositionCommand;
 import grouppractical.client.commands.RStatusCommand;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import javax.swing.event.EventListenerList;
 
 public class ServerListenerThread extends Thread {
 
 	private EventListenerList listeners;
 	private Socket socket;
 	private BufferedReader br;
 	private ClientConnection conn;
 	private boolean running;
 	
 	public ServerListenerThread(EventListenerList listeners, Socket socket, ClientConnection conn) {
 		this.listeners = listeners;
 		this.socket = socket;
 		this.conn = conn;
 	}
 	
 	public void close() {
 		if (!running) return;
 		
 		running = false;
 		
 		//Interrupts the thread (to break from the run loop)
 		this.interrupt();
 		//Closes the input stream
 		if (br != null)
 			try {
 				br.close();
 			} catch (IOException e) { }
 		try {
 			socket.close();
 		} catch (IOException e) { }
 		
 		//Close the connection
 		conn.close();
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 	
 	public void run() {
 		CommandParser cmdparse = new CommandParser();
 		try {
 			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		} catch (IOException e) {
 			close();
 		}
 		boolean loop = true;
 		while(loop) {
 			boolean word = false;
 			while (!word) {
 				try {
 					cmdparse.enqueue((char)br.read());
 				} catch (IOException e) {
 					System.err.println(e.toString());
 					close();
 					return;
 				} catch (InterruptedException e) {
 					close();
 					return;
 				}
				
 				while (!cmdparse.isOutputEmpty()) {
 					Command cmd = cmdparse.dequeue();
 					unpackCommand(cmd);
 				}
		
 			}
 		}
 	}
 	
 	public void unpackCommand(Command cmd) {
 		// unpack to RobotListener and MapListener commands
		CommandType cmdType = cmd.getCommandType();
		switch (cmdType) {
 		
 		case MINITIALIZE:
 			for(int i = 0; i < listeners.getListeners(MapListener.class).length; i++) {
				//listeners.getListeners(MapListener.class)[i].updateMap(map);
 			}
 			break;
		
 		case MPOSITION:
 			MPositionCommand posCmd = (MPositionCommand) cmd;
 			for(int i = 0; i < listeners.getListeners(MapListener.class).length; i++) {
 				listeners.getListeners(MapListener.class)[i].updatePosition(posCmd.getPosition());
 			}
 			break;
 		case RSTATUS:
 			RStatusCommand statCmd = (RStatusCommand) cmd;
 			for(int i = 0; i < listeners.getListeners(RobotListener.class).length; i++) {
 				RobotListener l = listeners.getListeners(RobotListener.class)[i];
 				l.updatePosition(statCmd.getPosition().getX(), statCmd.getPosition().getY(), (float)statCmd.getAngleDegrees());
 				l.updateVoltage(statCmd.getVoltage());
 			}
 			break;
 		default:
 			break;
 		}
	
 	}
	
 }
