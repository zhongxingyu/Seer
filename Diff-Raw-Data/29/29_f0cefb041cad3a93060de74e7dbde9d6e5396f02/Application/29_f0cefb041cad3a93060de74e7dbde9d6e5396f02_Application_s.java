 package grouppractical.remote.simple;
 
 import grouppractical.client.ClientConnection;
 import grouppractical.client.commands.Command;
 import grouppractical.client.commands.RDistanceCommand;
 import grouppractical.client.commands.RRotateCommand;
 import grouppractical.client.commands.RStopCommand;
 import grouppractical.utils.Console;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 public class Application implements KeyListener {
 	public static void main(String[] args) {
		new Application("localhost","Remote Java Client");
 	}
 	
 	private final Console console;
 	private final ClientConnection conn;
 	
 	/**
 	 * Constructs new client application
 	 * @param host Host name of server
 	 * @param name Friendly name of this client
 	 */
 	public Application(String host, String name) {
 		//Constructs new output window
 		console = new Console("Simple Client Console");
 		console.pack();
 		console.setVisible(true);
 		console.addKeyListener(this);
 		console.println("Client Console Created");
 		
 		//Attempts to connect to server
 		ClientConnection conn = null;
 		try {
 			conn = new ClientConnection(host, name);
 		} catch (UnknownHostException e) {
 			System.err.println(e.toString());
 			console.println("Error - Unknown Hostname");
 		} catch (IOException e) {
 			System.err.println(e.toString());
 			console.println("Error - I/O Exception whilst opening connection to server");
 		}
 		this.conn = conn;
 		
 	}
 	
 	/**
 	 * Sends command to server and prints it to output
 	 * @param cmd Command to send
 	 */
 	public void sendCommand(Command cmd) {
 		if (conn != null) {
 			conn.sendCommand(cmd);
 			console.println("Command Sent: " + cmd.toString());
 		}
 	}
 
 	/**
 	 * <p>Processes user input via keyboard.</p>
 	 * <dl>
 	 * 	<dt>Up and down</dt>
 	 * 	<dd>Moves robot forward or backward 5cm, respectively</dd>
 	 * 	<dt>Left and right</dt>
 	 * 	<dd>Turns robot left or right 30, respectively</dd>
 	 * 	<dt>Escape</dt>
 	 * 	<dd>Stops the robot</dd>
 	 * </dl>  
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		switch (e.getKeyCode()) {
 		case KeyEvent.VK_UP:
 			sendCommand(new RDistanceCommand((short) 5));
 			break;
 		case KeyEvent.VK_DOWN:
 			sendCommand(new RDistanceCommand((short) -5));
 			break;
 		case KeyEvent.VK_LEFT:
 			sendCommand(RRotateCommand.constructFromDegrees(-30));
 			break;
 		case KeyEvent.VK_RIGHT:
 			sendCommand(RRotateCommand.constructFromDegrees(-30));
 			break;
 		case KeyEvent.VK_ESCAPE:
 			sendCommand(new RStopCommand());
 			break;
 		}
 	}
 	
 	/**
 	 * Not Implemented
 	 */
 	@Override
 	public void keyReleased(KeyEvent arg0) { }
 
 	/**
 	 * Not Implemented
 	 */
 	@Override
 	public void keyTyped(KeyEvent arg0) { }
 }
