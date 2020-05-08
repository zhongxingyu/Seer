 package RingMaster.NetCode;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 
 /**
  * Author:      Grant Kurtz
  */
 public abstract class Talker implements Runnable {
 
 	private BufferedReader input;
 	private BufferedWriter output;
 	private boolean gameRunning;
 	private boolean ourTurn;
 	private String receivedCommand;
 	private ActionListener listener;
 	private String toSendCommand;
 
 	public Talker(ActionListener listener, boolean ourTurn) {
 		this.listener = listener;
 		this.ourTurn = ourTurn;
 	}
 
 	public void talk() {
 		while (gameRunning) {
 			if (ourTurn) {
 				try {
					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			// Process client's turn
 			else{
 
 				// Tell client card that was played
 				if(toSendCommand != null){
 					try {
 						output.write(toSendCommand+"\n");
 						output.flush();
 					} catch (IOException e) {
 						System.err.println("O GOD I AM SO SORRY SOMETHING BLOWED " +
 								"UP! TAKING THE WHOLE SHIP WITH US, BECAUSE FUCK YOU!");
 						e.printStackTrace();
 						try {
 							input.close();
 							output.close();
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
 						System.exit(1);
 					}
 				}
 
 				// Wait for client to respond with card played
 				try {
 					receivedCommand = input.readLine();
 					if(receivedCommand == null){
 						// I know this is horrible and wrong, I don't give a
 						// shit
						System.out.println("Game Ended...");
 						System.exit(1);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				listener.actionPerformed(new ActionEvent(this,
 						ActionEvent.ACTION_PERFORMED, "turnEnd"));
 				ourTurn = true;
 			}
 		}
 
 		// Close up everything
 		try {
 			input.close();
 			output.close();
 			closeSocket();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected abstract void closeSocket();
 
 	public void setGameRunning() {
 		gameRunning = true;
 	}
 
 	public void setInput(InputStream input) {
 		this.input = new BufferedReader(new InputStreamReader(input));
 	}
 
 	public void setOutput(OutputStream output) {
 		this.output = new BufferedWriter(new OutputStreamWriter(output));
 	}
 
 	public void endGame() {
 		gameRunning = false;
 	}
 
 	public void setTheirTurn(String command) {
 		toSendCommand = command;
 		ourTurn = false;
 	}
 
 	public ActionListener getListener() {
 		return listener;
 	}
 
 	public BufferedReader getInput() {
 		return input;
 	}
 
 	public BufferedWriter getOutput() {
 		return output;
 	}
 
 	public String getCommand() {
 		return receivedCommand;
 	}
 }
