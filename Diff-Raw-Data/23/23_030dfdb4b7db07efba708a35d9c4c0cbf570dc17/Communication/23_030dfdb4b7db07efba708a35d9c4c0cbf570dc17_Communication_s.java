 /*
 * Strat-game PC-Server :
  * 
  * Code : Frédéric Meslin, Florent Touchard, Benjamin Blois
  * Released on 29/01/12 at 15h00 for the 4th GGJ
  * 
  * This program and all its resources included is
  * published under the Creative common license By-NC 3.0.
  * 
  * For more informations :
  * http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package org.globalgamejam.strat;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import android.util.Log;
 
 public class Communication {
 	// Communication objects
 	private Socket socket;
 	private OutputStream ostream;
 	private StreamAnalyzer sa;
 
 	private int iD;
 	private int totalId;
 	private int stones;
 	private int actions;
 	private int bonus;
 	private int status;
 	private boolean[] alives;
 	private static final int MAX_CLIENTS = 6;
 
 	// Global client -> server protocol
 	private static final int STEAL_STONE = 0;
 	private static final int GIVE_STONE = 1;
 	private static final int USE_BONUS = 2;
 
 	// Global server -> client protocol
 	private static final int START_GAME = 0;
 	private static final int STONE_QUANTITY = 1;
 	private static final int ACTION_GAUGE = 2;
 	private static final int OBTAIN_BONUS = 3;
 	private static final int DISCONNECT = 4;
 	private static final int END_GAME = 5;
 	
 	// Global status
 	public static final int  STATUS_IN_PROGRESS = 0;
 	public static final int  STATUS_WIN = 1;
 	public static final int  STATUS_LOST = 2;
 	public static final int  STATUS_DECO= 3;
 
 	public Communication(String host, int port) throws IOException {
 		try {
 			this.socket = new Socket(host, port);
 			ostream = socket.getOutputStream();
 			Log.d("Communication", "Socket connected");
 		} catch (IOException ex) {
 			Log.v("Communication",
 					"Unable to connect socket " + ex.getLocalizedMessage());
 			throw ex;
 		}
 		alives = new boolean[MAX_CLIENTS];
 		for (int i = 0; i < MAX_CLIENTS; i++)
 			alives[i] = false;
 		iD = -1;
 		totalId = 0;
 		stones = 0;
 		actions = 0;
 		bonus = -1;
 		status = 0;
 	}
 
 	public void stealStone(int who) {
 		byte[] msg = new byte[2];
 		msg[0] = STEAL_STONE;
 		msg[1] = (byte) who;
 		try {
 			ostream.write(msg);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e("Communication",
 					"stealStone : IOException (" + e.getMessage() + ")");
 		}
 	}
 
 	public void giveStone(int who) {
 		byte[] msg = new byte[2];
 		msg[0] = GIVE_STONE;
 		msg[1] = (byte) who;
 		try {
 			ostream.write(msg);
 		} catch (IOException e) {
 			Log.e("Communication", "giveStone : IOException (" + e.getMessage()
 					+ ")");
 		}
 	}
 
 	public void useBonus(int bonus, int from, int to) {
 		byte[] msg = new byte[4];
 		msg[0] = USE_BONUS;
 		msg[1] = (byte) bonus;
 		msg[2] = (byte) from;
 		msg[3] = (byte) to;
 		try {
 			ostream.write(msg);
 		} catch (IOException e) {
 			Log.e("Communication", "useBonus : IOException (" + e.getMessage()
 					+ ")");
 		}
 	}
 
 	public int getId() {
 		return iD;
 	}
 
 	public int getTotalId() {
 		return totalId;
 	}
 
 	public int getStones() {
 		return stones;
 	}
 
 	public int getActions() {
 		return actions;
 	}
 
 	public int getBonus() {
 		return bonus;
 	}
 
 	// return one of the STATUS_* value
 	public int getStatus() {
 		return status;
 	}
 
 	public boolean isConnected() {
 		return (iD != -1);
 	}
 
 	public boolean isAlive(int id) {
 		if (id >= 0 && id < alives.length)
 			return alives[id];
 		return false;
 	}
 
 	public void start() {
 		sa = new StreamAnalyzer();
 		sa.start();
 	}
 
 	/**************************************************************************/
 	private class StreamAnalyzer extends Thread {
 		private boolean running;
 
 		public void start() {
 			running = true;
 			super.start();
 		}
 
 		public void end() {
 			if (!running)
 				return;
 			running = false;
 			try {
 				this.join();
 			} catch (Exception ex) {
 			}
 		}
 
 		public void run() {
 			// Open the input stream
 			InputStream istream;
 			try {
 				istream = socket.getInputStream();
 				while (running) {
 					// Get the message
 					int cmd = istream.read();
 					if (cmd < 0)
 						break;
 
 					// Interpret the message
 					int tmp;
 					switch (cmd) {
 					case START_GAME:
 						iD = istream.read();
 						totalId = istream.read();
 						for (int i = 0; i < totalId; i++)
 							alives[i] = true;
 						Log.v("Communication", "Start game : " + iD + "/"
 								+ totalId);
 						break;
 					case STONE_QUANTITY:
 						stones = istream.read();
 						Log.v("Communication", "Stones update : " + stones);
 						break;
 					case ACTION_GAUGE:
 						actions = istream.read();
 						Log.v("Communication", "Action power update : "
 								+ actions);
 						break;
 					case OBTAIN_BONUS:
 						bonus = istream.read();
 						Log.v("Communication", "Try to catch bonus : " + bonus);
 						break;
 					case DISCONNECT:
 						tmp = istream.read();
 						if (tmp > 0 && tmp < alives.length)
 							alives[tmp] = false;
 						Log.v("Communication", "Player " + tmp
 								+ " is gone away");
 						break;
 					case END_GAME:
 						tmp = istream.read();
 						if (tmp == 1)
 							status = 1;
 						else if (tmp == 0)
 							status = 2;
 						break;
 					}
 				}
 			} catch (IOException io) {
 				Log.e("Communication", "StreamAnalyzer : " + io.getMessage());
 				running = false;
 				status = 3;
 				iD = -1;
 			}
 		}
 	}
 }
