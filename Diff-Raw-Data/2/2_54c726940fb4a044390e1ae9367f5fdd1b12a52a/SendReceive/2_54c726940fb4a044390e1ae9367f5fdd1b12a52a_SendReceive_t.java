 /* KilCli, an OGC mud client program
  * Copyright (C) 2002 - 2004 Jason Baumeister
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *  notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *  notice, this list of conditions and the following disclaimer in the
  *  documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its contributors
  *  may be used to endorse or promote products derived from this software
  *  without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  */
 
 package terris.kilcli.io;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedOutputStream;
 import java.io.BufferedInputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.SocketException;
 import java.net.Socket;
 import java.net.NoRouteToHostException;
 import java.net.UnknownHostException;
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.io.File;
 import javax.swing.Timer;
 import terris.kilcli.*;
 import terris.kilcli.io.*;
 import terris.kilcli.window.*;
 import terris.kilcli.thread.*;
 import terris.kilcli.resource.*;
 
 /**
  * SendReceive for KilCli is the class used to send and receive<br>
  * data from the game server<br>
  * Ver: 1.0.2
  */
 
 public class SendReceive implements Runnable {
 
 	private static int netBufferSize = 2048;
 	private byte nextLine[] = new byte[2];
 	private String newLine;
 
 	private Socket connSock;
 	private BufferedOutputStream outStream;
 	private BufferedInputStream inStream;
 	private boolean connected;
 	private String input = "";
 	private boolean skipOutput = false;
 	private static boolean scriptSkip = false;
 	private boolean scriptReset = false;
 	private boolean enableTriggers = false;
 	private int gameNumber = 0;
 	private boolean loggedIn = false;
 	private Thread receiveThread = null;
 	private byte netBuffer[] = null;
     private String tempString1 = "";
     private Timer quitCountDown;
     //private Timer encryptDelay;
     private String lastLine = "";
     private String carryOver = "";
     private long sendTime = -1;
     private boolean netMelee = false;
     private boolean checkAttack = false;
     private int damageTotal = 0;
     private int attackCount = 0;
     private int maxDamage = 0;
     //private static final BASE64Encoder encoder = new BASE64Encoder();
     //private ArrayList encryptDelayList = new ArrayList();
     //private int sentEncryptCount = 0;
     private String charSet = "windows-1253";
 
 	/**
 	 * Creates a SendReceive object set to connect to the
 	 * given game number
 	 *
 	 * @param gn - the game number to connect to
 	 */
 
 	public SendReceive(int gn) {
 		gameNumber = gn;
 		nextLine[0] = (byte)13;
 		nextLine[1] = (byte)10;
 		newLine = new String(nextLine);
 		//timer to make sure we quit the game
 		quitCountDown = new Timer(4000, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				quitCountDown.stop();
 				loggedIn = false;
 				connected = false;
 				KilCli.setLoggedIn(false);
 				int flag = KilCli.getQuitFlag();
 				if (flag == 0) {
 					KilCliThread.getKilCli().disconnect("true", 0);
 				} else if (flag == 1) {
 					KilCliThread.getKilCli().reconnect();
 				} else if (flag == 2) {
 					KilCliThread.getKilCli().exit("true");
 				}
 			}
 		});
 		quitCountDown.setRepeats(false);
 
 		/*encryptDelay = new Timer(50, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				encryptDelay.stop();
 				if (encryptDelayList.size() == 1) {
 					write((String)(encryptDelayList.get(0)));
 					sentEncryptCount++;
 					encryptDelayList.remove(0);
 
 				} else if (encryptDelayList.size() > 1) {
 					write((String)(encryptDelayList.get(0)));
 					sentEncryptCount++;
 					encryptDelayList.remove(0);
 					encryptDelay.start();
 				}
 			}
 		});
 		encryptDelay.setRepeats(false);*/
 
 	}
 
 	/**
 	 * Prepares a thread to do all of the receiving in
 	 */
 
     public void start() {
         if (receiveThread == null) {
             receiveThread = new Thread(this, "Receive");
             receiveThread.start();
         }
     }
 
     /**
      * Stops the quit count down
      */
     public void stopQuitCountDown() {
 		quitCountDown.stop();
 	}
 
 	/**
 	 * Checks the thread references and establishes the connection
 	 * to the remove server
 	 */
 
     public void run() {
         Thread myThread = Thread.currentThread();
         establishConnection();
     }
 
 	/**
 	 * Method to connect to server and prepare to receive data
 	 */
 
 	public void establishConnection() {
 		//attempt to connect
 		if (connect()) {
 			//if we get connected, run program
 			Receive();
 		} else {
 			//else write, could not connect
 			KilCli.gameWrite("Could not connect");
 		}
 	}
 
 	public boolean blankBeforeEcho() {
 		if (lastLine.length() > 3) {
 			return !(lastLine.substring(lastLine.length()-4, lastLine.length()).equals("<br>"));
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * Connects to the needed remote server
 	 *
 	 * @return boolean true/false depending on if connection was successful
 	 */
 
 	public boolean connect() {
 		connSock = null;
 		String server = "";
 		int port = 0;
 		//attempts to establish a connection
 		try {
 			if (gameNumber == 0) {
				server = "ogcserver02.onlinegamescompany.net";
 				port = 31000;
 			} else if (gameNumber == 1) {
 				server = "ogcserver03.onlinegamescompany.net";
 				port = 31000;
 			} else if (gameNumber == 2) {
 				server = "ogcserver01.onlinegamescompany.net";
 				port = 31000;
 			}
 			connectToServer(server, port);
 		    outStream = new BufferedOutputStream(connSock.getOutputStream());
 
 		} catch (Exception oops) {
 			KilCli.gameWrite("Exception while connecting:" + oops);
 			KilCli.gameWrite("Trying to connect via IP address...");
 			try {
 				connSock.close();
 				if (gameNumber == 0) {
 					server = "81.138.236.173";
 				} else if (gameNumber == 1) {
 					server = "217.29.193.246";
 				} else if (gameNumber == 2) {
 					server = "217.29.193.218";
 				}
 				connectToServer(server, port);
 				outStream = new BufferedOutputStream(connSock.getOutputStream());
 			} catch (Exception oops2) {
 				KilCli.gameWrite("IP Address failed, cannot connect to game.");
 				connected = false;
 				return connected;
 			}
 		}
 
 	    try {
 			inStream = new BufferedInputStream(connSock.getInputStream());
         	//set connected flag to true
         	connected = true;
 	    	//outputs string to authenticate with remote server
 			String tempString = "";
 			tempString = tempString + (char)27 + (char)87 + (char)77 + (char)52 + (char)10;
 			write(tempString);
 			tempString = null;
         	return connected;
 
 	    } catch (Exception oops) {
 	        KilCli.gameWrite("No inStream, could not complete connection");
 	        connected = false;
 	        inStream = null;
 	        return connected;
 	    }
 	}
 
 
 	/**
 	 * Attempts to connect to the given server, port.
 	 * Uses the proxy settings if needed
 	 */
 	private void connectToServer(String server, int port) throws IOException {
 		if (KilCli.getProxy().toLowerCase().equals("true")) {
            	String sockHost = KilCli.getProxyHost();
            	int sockPort = KilCli.getProxyPort();
            	if (KilCli.getProxyType().equalsIgnoreCase("Socks4/4A")) {
 				System.getProperties().put("proxySet", "true" );
 				System.getProperties().put("socksProxyHost", sockHost);
 				System.getProperties().put("socksProxyPort", sockPort + "");
                	connSock = new Socket(server, port);
                	connSock.setTcpNoDelay(true);
 			} else if (KilCli.getProxyType().equalsIgnoreCase("Socks5")) {
 				connSock = (Socket)new SocksSocket(sockHost, sockPort, KilCli.getProxyUsername(), KilCli.getProxyPassword(), server, port);
 				connSock.setTcpNoDelay(true);
 			} else if (KilCli.getProxyType().equalsIgnoreCase("HTTP Tunnel")) {
 				connSock = (Socket)new HTTPSocket(sockHost, sockPort, KilCli.getProxyUsername(), KilCli.getProxyPassword(), server, port);
 				connSock.setTcpNoDelay(true);
 			} else {
 				KilCli.gameWrite("Unrecognized proxy type, using direct connection");
 				connSock = new Socket(server, port);
 				connSock.setTcpNoDelay(true);
 			}
 		} else {
 			connSock = new Socket(server, port);
 			connSock.setTcpNoDelay(true);
 		}
 	}
 
 	/**
 	 * Receives data from the server and figures out what to do with it
 	 */
 
 	public void Receive() {
 
 		//while we are connected....
 		while (connected) {
 			//try to get text from the server
 			try {
 				inputCheck(netRead());
 			} catch (SocketException se) {
 				//socket says connection reset
 				loggedIn = false;
 				KilCli.setLoggedIn(false);
 				connected = false;
 				System.err.println(se);
 				se.printStackTrace();
 				KilCli.gameWrite("*** Connection to game Lost *** (Connection reset by peer)");
 				disconnect("quit");
 			} catch (IOException ioe) {
 				//netRead says the stream is dead
 				loggedIn = false;
 				KilCli.setLoggedIn(false);
 				connected = false;
 				System.err.println(ioe);
 				ioe.printStackTrace();
 				KilCli.gameWrite("*** Connection to game Lost *** (read segment)");
 				disconnect("quit");
 			} catch (Exception e) {
 				// We've run into some other problem, try to keep the connection
 				// alive.
 				System.err.println(e);
 				e.printStackTrace();
         		SMTPClient.sendError("receiveNonIO", e);
         	}
 
 			if (scriptSkip && (input.length() > 0)) {
 				skipOutput = true;
 				scriptReset = true;
 			}
 
 			//if we haven't been told to skip the output already
 			if (!skipOutput) {
 
 				//if triggers are enabled, check this line
 				if (enableTriggers) {
 					new TriggerThread(input).start();
 				}
 				//send the output to the game window
 				KilCli.gameWrite(input);
 			}
 			//reset temporary flags
 			skipOutput = false;
 			if (scriptReset) {
 				scriptSkip = false;
 			}
 			scriptReset = false;
 			input = "";
 		}
 	}
 
 	/**
 	 * Method to set the netMelee boolean variable
 	 *
 	 * @param nm - the new value of netMelee
 	 */
 
 	public void setNetMelee(boolean nm) {
 		netMelee = nm;
 	}
 
 	/**
 	 * Method to toggle the value of skipOutput, which is used
 	 * to skip sending data to the display routines
 	 *
 	 * @param so - the new value of skipOutput
 	 */
 
 	public void setSkipOutput(boolean so) {
 		skipOutput = so;
 	}
 
 	/**
 	 * Method to toggle the value of scriptSkip, which is used
 	 * to skip the resulting data from running a script
 	 *
 	 * @param ss - the new value of scriptSkip
 	 */
 
 	public static void setScriptSkip(boolean ss) {
 		scriptSkip = ss;
 	}
 
 	/**
 	 * Toggle for if we are to analyze incoming data for triggers
 	 *
 	 * @param t - the new value of enableTriggers
 	 */
 
 	public void setTrigger(boolean t) {
 		enableTriggers = t;
 	}
 
 	/**
 	 * Resets the checkAttack flag to false
 	 */
 
 	public void resetCheckAttack() {
 		checkAttack = false;
 	}
 
 	/**
 	 * Resets the damage tracking stats:<br>
 	 * damageTotal, attackCount, maxDamage
 	 */
 	public void resetDamageCount() {
 		damageTotal = 0;
 		attackCount = 0;
 		maxDamage = 0;
 	}
 
 	/**
 	 * Computes the average damage, and returns a string<br>
 	 * contain Total damage, average damage, and max damage
 	 */
 	public String getAvgDamage() {
 		double avg = ((double)damageTotal) / ((double)attackCount);
 		return "Total Damage: " + damageTotal + "<br>Average Damage: " + avg + "<br>Max Damage: " + maxDamage;
 	}
 
 	/**
 	 * Adds a blank line to the output
 	 */
 
 	public void addBlank() {
 		if ((input.length() > 3) && (!input.substring(input.length()-4, input.length()).equals("<br>"))) {
 			input += "<br>";
 			lastLine = "<br>";
 		}
 	}
 
 	/**
 	 * Disconnects from remote server and cleans up the socket
 	 */
 
 	public boolean disconnect(String quit) {
 		String exit = "";
 
 		try {
 			if (loggedIn) {
 				quitCountDown.start();
 				write(quit);
 			}
 			connected = false;
 			Thread.sleep(1000);
 			exit = netRead();
 			exit = exit.trim();
 
 		} catch (Exception e) {
 			System.err.println("Error while closing socket.");
 			System.err.println(e);
 		}
 		if (exit.indexOf("Thanks for playing!") > -1) {
 			loggedIn = false;
 			KilCli.setLoggedIn(false);
 			try {
 				inStream.close();
 				outStream.close();
 				connSock.close();
 			} catch (Exception e) {
 				System.err.println(e);
 				e.printStackTrace();
 			}
 			connSock = null;
 			inStream = null;
 			outStream = null;
 			return true;
 		} else {
 			System.err.println("error while disconnecting");
 			System.err.println("exit=" + exit);
 		}
 		return true;
 
 	}
 
 	/**
 	 * Sends a stream of data to the remote server
 	 *
 	 * @param output - the string to be sent
 	 */
 
 	public synchronized void write(String output) {
 		if (connected) {
 			if (output.length() < 1) {
 				return;
 			}
 
 			//if (output.startsWith("etell")) {
 			//	encryptedWrite(output.substring(1, output.length()));
 			//	return;
 			//}
 
 			//attempt to send the string
 			if (output.length() > 123) {
 				output = output.substring(0, 122);
 			}
 
 			output += '\n';
 			try {
 				outStream.write(output.getBytes(charSet), 0, output.length());
 				outStream.flush();
 				sendTime = System.currentTimeMillis();
 			//otherwise, drop the connection and say we have an error
 			} catch (Exception e) {
 				connected = false;
 				loggedIn = false;
 				KilCli.setLoggedIn(false);
 				System.err.println("SendText exception: " + e);
 				SMTPClient.sendError("sending", e);
         	}
         	if (netMelee) {
 				output = output.toLowerCase();
 				if (output.startsWith("aim")) {
 					checkAttack = true;
 				} else if (output.startsWith("attack")) {
 					checkAttack = true;
 				} else if (output.startsWith("ch")) {
 					checkAttack = true;
 				} else if (output.equals("kill")) {
 					checkAttack = true;
 				} else if (output.charAt(0) == 'a') {
 					if (output.length() == 2) {
 						checkAttack = true;
 					} else if (output.charAt(1) == ' ') {
 						checkAttack = true;
 					}
 				}
 			}
 		}
 	}
 
 	/*private void encryptedWrite(String output) {
 		int spaceIndex = 0;
 		spaceIndex = output.indexOf(" ", spaceIndex);
 		spaceIndex = output.indexOf(" ", spaceIndex+1);
 		if (spaceIndex > 4) {
 			output = output.substring(0, spaceIndex+1) + "|e*|" + toBase64(output.substring(spaceIndex+1, output.length())) + "|*e|";
 			if (output.length() > 110) {
 				//we cut the line shorter than max so the end tag is always kept intact
 				write(output.substring(0, 109));
 				sentEncryptCount++;
 				//put the rest of the line in the queue to be sent later
 				encryptDelayList.add(output.substring(0, spaceIndex+1) + " " + output.substring(109, output.length()));
 				if (!encryptDelay.isRunning()) {
 					encryptDelay.start();
 				}
 			} else {
 				write(output);
 				sentEncryptCount++;
 			}
 		} else {
 			KilCli.gameWrite("Bad encrypted tell!");
 		}
 	}
 
 	/**
 	 * Convert a string to base 64 encoded form.
 	 *
 	 *
     protected static String toBase64(String s) {
         return encoder.encodeBuffer(s.getBytes());
     }*/
 
 	/**
 	 * Changes the read buffer size
 	 *
 	 * @param s - the new size of the read buffer, max is 8704, min is 1024
 	 */
 
 	public void setNetBufferSize(int s) {
 		int oldSize = netBufferSize;
 		//check to see if the size changed
 		if (oldSize != s) {
 			//make sure the new size is valid
 			if (s < 1024) {
 				netBufferSize = 1024;
 			} else if (s > 8704) {
 				netBufferSize = 8704;
 			} else {
 				netBufferSize = s;
 			}
 
 			//copy old buffer to the new buffer
 			if (netBuffer != null) {
 				oldSize = java.lang.Math.min(oldSize, netBufferSize);
 				byte tmpBuffer[] = new byte[netBufferSize];
 				System.arraycopy(netBuffer, 0, tmpBuffer, 0, oldSize);
 				netBuffer = tmpBuffer;
 			} else {
 				netBuffer = new byte[netBufferSize];
 			}
 		}
 	}
 
 	/**
 	 * Reads available data from socket in chunks
 	 * whose size varies with the size of the current buffer
 	 *
 	 * @return String - the string representation data that was received
 	 */
 
 	private String netRead() throws Exception {
     	int len;
 
     	//Check to make sure the buffer has been allocated
     	if (netBuffer == null) {
     		netBuffer = new byte[netBufferSize];
 		}
 
     	//Read any waiting input for the connection...
         len = inStream.read(netBuffer, 0, netBufferSize);
 
 		//see if we need to throw an exception because we read -1 bytes
 		//this basically means that the inStream doesn't exist anymore
 		if (len == -1) {
 			throw new IOException();
 		}
 
      	// Convert the buffer into a String and return it
 		return new String(netBuffer, 0, len, charSet);
 	}
 
 	/**
 	 * Checks the received data to see what needs to be done with it
 	 *
 	 * @param tempInput - the string that was received from the server
 	 */
 
 	public void inputCheck(String tempInput) throws Exception {
 		if (sendTime > 0) {
 			InfoPanel.updatePing(System.currentTimeMillis() - sendTime);
     		sendTime = -1;
 		}
     	int stringSearchIndex = 0;
     	int lastSearchIndex = 0;
     	//Go through the block of text we got and filter it
      	//appropriately.
      	if (tempInput != null) {
 			if (carryOver.length() > 0) {
 				tempInput = carryOver + tempInput;
 				carryOver = "";
 			}
 			//run through the text looking for any newlines, so we can analyze each of them
     		stringSearchIndex = tempInput.indexOf(newLine);
     		while (stringSearchIndex != -1) {
 				if (stringSearchIndex > lastSearchIndex) {
 					//otherwise, let's analyze the line
 					inputLineCheck(tempInput.substring(lastSearchIndex, stringSearchIndex));
 				} else if (input.length() > 0) {
 					addBlank();
 				}
 				//then remove the newline and continue the search
 				//tempInput = tempInput.substring(stringSearchIndex + 2, tempInput.length());
 				lastSearchIndex = stringSearchIndex+2;
 				stringSearchIndex = tempInput.indexOf(newLine, lastSearchIndex);
     		}
     		//analyze what's left.
     		if (!loggedIn) {
 				inputLineCheck(tempInput.substring(lastSearchIndex, tempInput.length()));
 			} else {
 				carryOver = tempInput.substring(lastSearchIndex, tempInput.length());
 			}
 		}
 	}
 
 	/**
 	 * EffectsPanel says we were just stunned, <br>
 	 * so we analyze the last line to see how long we were stunned for
 	 */
 	public void checkStun() {
 		int tempY = lastLine.indexOf("^Y");
 		int tempN = lastLine.indexOf("^N", tempY);
 		if ((tempY != -1) && (tempN != -1)) {
 			try {
 				int time = Integer.parseInt(lastLine.substring(tempY+2, tempN));
 				KilCli.startStun(time);
 			} catch (Exception e) {
 				if ("stunned".equals(lastLine.substring(tempY+2, tempN))) {
 					tempY=tempN+7;
 					tempN = lastLine.indexOf(" ", tempY);
 					try {
 						int time = Integer.parseInt(lastLine.substring(tempY, tempN));
 						KilCli.startStun(time);
 					} catch (Exception e2) {
 						System.err.println(e2);
 						e2.printStackTrace();
 						
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * EffectsPanel says we were just slowed, <br>
 	 * so we analyze the last line to see how long we were slowed for
 	 */
 
 	public void checkSlow() {
 		int tempY = lastLine.indexOf("^Y");
 		int tempN = lastLine.indexOf("^N", tempY);
 		if ((tempY != -1) && (tempN != -1)) {
 			try {
 				int time = Integer.parseInt(lastLine.substring(tempY+2, tempN));
 				KilCli.startSlow(time);
 			} catch (Exception e) {
 				//KilCli.gameWrite("this line was checked2 - \"" + lastLine + "\"");
 			}
 		}
 	}
 
 	public void hardenReset() {
 		if (KilCli.getCheckHarden()) {
 			String slash = System.getProperty("file.separator");
 			File script = new File("scripts" + slash + "HardenArmour");
 			if (script.isFile() && script.canRead()) {
 				Trigger.setAnalyzeInventory(true);
 				new ScriptExecuteThread("HardenArmour", new Script()).start();
 
 			} else {
 				KilCli.gameWrite("Could not find HardenArmour script, cannot reset the display");
 			}
 		}
 	}
 
 	/**
 	 * Method to check a line input from the game.<br>
 	 * to see what should be done with it
 	 */
 
 	public void inputLineCheck(String tempInput) {
 		int stringSearchIndex = 0;
 		tempString1 = "";
 		char previewChar;
 
 		if (!loggedIn) {
 
 			if (gameNumber == 1) {
 				if (tempInput.indexOf("Password?") > -1) {
 					KilCli.setCharacter(KilCli.getLastCommand(), 0);
 				}
 			} else {
 				//get character names
 				stringSearchIndex = tempInput.indexOf("1)");
 				if (stringSearchIndex != -1) {
 					KilCli.setCharacter(tempInput.substring(stringSearchIndex+3, tempInput.length()), 0);
 				}
 				stringSearchIndex = tempInput.indexOf("2)");
 				if (stringSearchIndex != -1) {
 					KilCli.setCharacter(tempInput.substring(stringSearchIndex+3, tempInput.length()), 1);
 				}
 				stringSearchIndex = tempInput.indexOf("3)");
 				if (stringSearchIndex != -1) {
 					KilCli.setCharacter(tempInput.substring(stringSearchIndex+3, tempInput.length()), 2);
 				}
 			}
 
 			//check for the "Today in Terris" string that appears after successful login
 			if (gameNumber == 0) {
 				stringSearchIndex = tempInput.indexOf("Today in Terris");
 			} else if (gameNumber == 1) {
 				stringSearchIndex = tempInput.indexOf("Today in Cosrin");
 			} else if (gameNumber == 2) {
 				stringSearchIndex = tempInput.indexOf("Today in the Old World");
 			}
 			//if the string is found, set loggedIn to 'true'
 			if (stringSearchIndex != -1) {
 				int num;
 				try {
 					num = Integer.parseInt(KilCli.getLastCommand());
 				} catch (Exception e) {
 					num = 1;
 				}
 				KilCli.setUsedCharacter(num-1);
 				KilCli.setLoggedIn(true);
 				loggedIn = true;
 			}
 		}
 
 		//set the initial search index
 		stringSearchIndex = tempInput.indexOf("<");
 		//check to see if we need to convert any < (to avoid the html parser)
 		while (stringSearchIndex != -1) {
 			tempString1 = tempInput.substring(0, stringSearchIndex);
 			tempInput = tempString1 + "&lt;" + tempInput.substring(stringSearchIndex + 1, tempInput.length());
 			stringSearchIndex = tempInput.indexOf("<", stringSearchIndex);
 		}
 		//reset search index
 		stringSearchIndex = 0;
 
 		//get the first character of the line
 		if (tempInput.length() > 0) {
 			previewChar = tempInput.charAt(0);
 
 			//check if line has characters that need to be translated
 			tempInput = KilCli.characterTranslate(tempInput);
 
 			//if its a [ or a control char, we need to analyze the line now
 			if (((previewChar == 27) || (previewChar == 91))) {
 				if (enableTriggers) {
 					new TriggerThread(tempInput).start();
 				}
 
 				//if that character is a control character...
 				if (previewChar == 27) {
 					//call the control character handler
 					tempInput = KilCli.controlChar(tempInput);
 				} else {
 					KilCli.log(tempInput);
 					//call the window picker
 					tempInput = KilCli.pickWindow(tempInput);
 
 				}
 			} else if (tempInput.startsWith("Attack mode: ^Y")) {
 				KilCli.updateStance(tempInput.substring(15, tempInput.length()));
 			//} else if (sentEncryptCount > 0) {
 			//	if (tempInput.indexOf(" is not currently ") > -1) {
 			//		sentEncryptCount--;
 			//	} else if (tempInput.indexOf("You tell ^G") > -1) {
 			//		tempInput = tempInput.substring(0, tempInput.indexOf("'") + 1) + "&lt;encrypted message>'";
 			//		sentEncryptCount--;
 			//	}
 			}
 
 			KilCli.log(tempInput);
 
 			//if we haven't been told to ignore this line
 			if (!skipOutput) {
 
 				if (checkAttack) {
 					if (tempInput.startsWith("^GYou hit for ^C")) {
 						try {
 							stringSearchIndex = tempInput.indexOf("^N", 14);
 							int total = Integer.parseInt(tempInput.substring(16, stringSearchIndex));
 							int stringSearchIndex2 = tempInput.indexOf("^N.", stringSearchIndex + 48);
 							int absorbed = Integer.parseInt(tempInput.substring(stringSearchIndex+48, stringSearchIndex2));
 							int net = total-absorbed;
 							damageTotal+=net;
 							tempInput += " (" + net + " total)";
 							attackCount++;
 							if (net > maxDamage) {
 								maxDamage = net;
 							}
 						} catch (Exception e) {
 							System.err.println(e);
 							e.printStackTrace();
 						}
 					}
 				}
 
 				//if this is not the first line of the packet
 				if (input.length() != 0) {
 					input += "^N<br>" + tempInput;
 				} else if (tempInput.length() > 0) {
 					//if it is the first line...
 					input = tempInput;
 				}
 				lastLine = tempInput;
 			}
 		}
 
 		//reset flags and temp vars
 		skipOutput = false;
 	}
 
 }
