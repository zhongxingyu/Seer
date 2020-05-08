 /*
  * Copyright (c) 2011, Carlos Eduardo da Silva <kaduardo@gmail.com>
  *
  * 
  *  This file is part of libjhgdc.
  * 
  *  libjhgdc is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  libjhgdc is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with libjhgdc.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jhgdc.library;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 
 /**
  * This class implements a HGD client.
  * 
  * You can use an instance of this class to connect to a HGD daemon and do HGD
  * operations.
  * 
  * The common flow is: create the object, connect to a HGD daemon using the
  * connect() method, authenticate with login(), do whatever you want to do, and
  * close the connection with disconnect().
  * 
  * A HGDClient object can handle a connection per time. Once you have used and
  * disconnected a HGDClient object you can use it again to connect to another
  * HGD daemon.
  * 
  * @author Carlos Eduardo da Silva
  * @since 22/03/2011
  * 
  */
 public class HGDClient {
 
 	/**
 	 * The remote host name/adress of the HGD daemon.
 	 */
 	private String host = null;
 
 	/**
 	 * The remote port number of the HGD daemon.
 	 */
 	private int port = 0;
 
 	/**
 	 * The username used in case the client is authenticated.
 	 */
 	private String username;
 
 	/**
 	 * The passwork used in case the client is authenticated.
 	 */
 	private String password;
 
 	/**
 	 * A flag with the connection status.
 	 */
 	private boolean connected = false;
 
 	/**
 	 * A flag with the authentication status.
 	 */
 	private boolean authenticated = false;
 
 	/**
 	 * Input channel.
 	 */
 	private BufferedReader input;
 
 	/**
 	 * Output channel used for commands.
 	 */
 	private BufferedWriter output;
 
 	/**
 	 * Output channel used to send files.
 	 */
 	private BufferedOutputStream fileOutput;
 
 	/**
 	 * The socket.
 	 */
 	private Socket clientSocket;
 
 	/**
 	 * Default constructor initializes the client.
 	 */
 	public HGDClient() {
 
 	}
 
 	/**
 	 * This method tests if this client is authenticated.
 	 * 
 	 * @return true if this client is authenticated, false otherwise.
 	 */
 	public boolean isAuthenticated() {
 		return authenticated;
 	}
 
 	/**
 	 * This method tests if this client is connected to a remote FTP server.
 	 * 
 	 * @return true if this client is connected to a remote FTP server, false
 	 *         otherwise.
 	 */
 	public boolean isConnected() {
 		return connected;
 	}
 
 	/**
 	 * Returns the host name or address of the HGD daemon if connected.
 	 * 
 	 * @return The remote host name or address, or null if not connected.
 	 */
 	public String getHost() {
 		return host;
 	}
 
 	/**
 	 * Returns the port number of the HGD daemon if connected.
 	 * 
 	 * @return The HGD daemon port number.
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/**
 	 * Returns the authenticated password in case the client is authenticated.
 	 * 
 	 * @return The authentication password.
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * Returns the authentication username in case the client is authenticated.
 	 * 
 	 * @return The authentication username.
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	/**
 	 * Connects the client to the HGD daemon using the default port.
 	 * 
 	 * @param host
 	 *            The host name or address of the daemon.
 	 * @throws IllegalStateException
 	 *             If the client is already connected.
 	 * @throws IOException
 	 *             If an I/O exception occurs.
 	 * @throws JHGDException
 	 *             If the connection can not be established.
 	 */
 	public void connect(String host) throws IllegalStateException,
 			IOException, JHGDException {
 		connect(host, HGDConsts.DEFAULT_PORT);
 	}
 
 	/**
 	 * Connects the client to the HGD daemon.
 	 * 
 	 * @param host
 	 *            The host name or address of the daemon.
 	 * @param port
 	 *            The port listened by the daemon.
 	 * @throws IllegalStateException
 	 *             If the client is already connected.
 	 * @throws IOException
 	 *             If an I/O exception occurs.
 	 * @throws JHGDException
 	 *             If the connection can not be established.
 	 */
 	public void connect(String host, int port) throws IllegalStateException,
 			IOException, JHGDException {
 		// Check if the client is already connected
 		if (connected) {
 			throw new IllegalStateException("Client already connected to "
 					+ host + " on port " + port);
 		}
 
 		// Open socket
 		openSocket(host, port);
 
 		String returnMessage = (String) input.readLine();
 
 		if (checkServerResponse(returnMessage) != HGDConsts.SUCCESS) {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 		
 		String protocolVersion = requestProto();
 		if ( !protocolVersion.equalsIgnoreCase(HGDConsts.PROTOCOLVERSION)) {
 			throw new JHGDException("Incompatible protocols. Client: " + HGDConsts.PROTOCOLVERSION + ", Daemon: " + protocolVersion);
 		}
 		
 		// set the flags
 		this.connected = true;
 		this.authenticated = false;
 		this.host = host;
 		this.port = port;
 		this.username = null;
 		this.password = null;
 		
 	}
 
 	/**
 	 * Disconnects from the HGD daemon, optionally using the BYE command.
 	 * 
 	 * @param sendQuitCommand
 	 *            If true, uses the BYE command, otherwise the connection is
 	 *            abruptly closed by the client without sending any advice to
 	 *            the server.
 	 * @throws IllegalStateException
 	 *             If the client is not connected to a HGD daemon.
 	 * @throws IOException
 	 *             If there is an I/O exception (can be thrown only if
 	 *             sendQuitCommand is true).
 	 * @throws JHGDException
 	 *             If the server returns a message different than ok.
 	 */
 	public void disconnect(boolean sendQuitCommand)
 			throws IllegalStateException, IOException, JHGDException {
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 
 		if (sendQuitCommand) {
 			sendLineCommand("bye");
 
 			String returnMessage = (String) input.readLine();
 			// System.out.println("closeConnection - returned: "+returnMessage);
 			if (checkServerResponse(returnMessage) != HGDConsts.SUCCESS) {
 				throw new JHGDException(returnMessage.substring(returnMessage
 						.indexOf('|') + 1));
 			}
 		}
 
 		// close the socket and clean the flags
 		closeSocket();
 
 		// Set the flags
 		connected = false;
 		authenticated = false;
 		username = null;
 		password = null;
 		host = null;
 		port = 0;
 
 	}
 
 	/**
 	 * Authenticates the user against the HGD daemon.
 	 * 
 	 * This method implements the "user" command of the HGD protocol.
 	 * 
 	 * @param username
 	 *            The username.
 	 * @param password
 	 *            The password (if none it is set to null).
 	 * @throws IllegalStateException
 	 * @throws IOException If an I/O exception occurs.
 	 * @throws JHGDException
 	 */
 	public void login(String username, String password)
 			throws IllegalStateException, IOException, JHGDException {
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 		// Set SSL if necessary
 
 		// Reset the authentication flag.
 		authenticated = false;
 
 		// Check received parameters
 		if (username == null || username.isEmpty()) {
 			throw new JHGDException("Null or empty username");
 		}
 		if (password == null) {
 			throw new JHGDException("Null password");
 		}
 
 		// send the command: "user|%s|%s"
 		sendLineCommand("user|" + username + "|" + password);
 
 		String returnMessage = (String) input.readLine();
 		// check server response
 		if (checkServerResponse(returnMessage) == HGDConsts.SUCCESS) {
 			// set the flags
 			this.authenticated = true;
 			this.username = username;
 			this.password = password;
 		} else {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 
 	}
 
 	/**
 	 * This method recovers the playlist from the daemon and returns it as an
 	 * array of string. It implements the "ls" command of the HGD protocol.
 	 * {"ls", 0, 0, hgd_req_playlist},
 	 * 
 	 */
 	public String[] requestPlaylist() throws IllegalStateException,
 			IOException, JHGDException {
 		// Check if the connection is established
 		if (!connected)
 			throw new IllegalStateException("Client not connected");
 
 		// send the command
 		sendLineCommand("ls");
 
 		String returnMessage = (String) input.readLine();
 		// Debug
 		// System.out.println("req_playlist - returned: "+returnMessage);
 
 		String returnList[];
 
 		if (checkServerResponse(returnMessage) == HGDConsts.SUCCESS) {
 			int numberOfItems = Integer.parseInt(returnMessage.split("\\|")[1]);
 			returnList = new String[numberOfItems];
 
 			if (numberOfItems > 0) {
 				String returnedItem;
 
 				for (int i = 0; i < numberOfItems; i++) {
 					returnedItem = (String) input.readLine();
 					returnList[i] = returnedItem;
 				}
 			}
 			return returnList;
 		} else {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 	}
 
 	/**
 	 * Gets the currently playing item, if any. This method implement the "np"
 	 * command of the HGD protocol.
 	 * 
 	 * @return a String in the following format:
 	 *         ok|<playing?>[|<track-id>|<file‐name>|<artist>|<title>|<user>].
 	 *         If <playing?> = 0, then nothing is playing and therefore, no
 	 *         further information is available. The <artist> and <title> fields
 	 *         are generated from metadata using taglib at time of upload. If no
 	 *         tag information was available, the <artist> and <title> fields
 	 *         remain blank.
 	 * @throws IllegalStateException
 	 *             in case the library is not connected.
 	 * @throws IOException
 	 * @throws JHGDException
 	 */
 	public String requestNowPlaying() throws IllegalStateException,
 			IOException, JHGDException {
 		// Check if the connection is established
 		if (!connected)
 			throw new IllegalStateException("Client not connected");
 
 		sendLineCommand("np");
 		String returnMessage = (String) input.readLine();
 
 		if (checkServerResponse(returnMessage) == HGDConsts.SUCCESS) {
 			return returnMessage;
 		} else {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 	}
 
 	/**
 	 * Requests the protocol major version.
 	 * 
 	 * This method implements the "proto" command of the HGD protocol.
 	 * 
 	 * @return The protocol major version of the daemon.
 	 * @throws IllegalStateException
 	 *             in case the library is not connected.
 	 * @throws IOException
 	 * @throws JHGDException
 	 */
 	public String requestProto() throws IllegalStateException, IOException,
 			JHGDException {
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 
 		sendLineCommand("proto");
 		String returnMessage = (String) input.readLine();
 
 		if (checkServerResponse(returnMessage) == HGDConsts.SUCCESS) {
 			return returnMessage.split("\\|")[1];
 		} else {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 	}
 
 	/**
 	 * Votes off the currently playing track.
 	 * 
 	 * It is recommended that clients use the 1-argument variant of this command
 	 * to avoid race conditions in voting off.
 	 * This method has been maintained for testing purposes.
 	 * 
 	 * @deprecated
 	 * {"vo", 0, 1, hgd_req_vote_off},
 	 */
 	public void requestVoteOff() throws IllegalStateException, IOException,
 			JHGDException {
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 
 		if (!authenticated) {
 			throw new IllegalStateException("Client not authenticated");
 		}
 
 		sendLineCommand("vo");
 
 		String returnMessage = input.readLine();
 		if (checkServerResponse(returnMessage) != HGDConsts.SUCCESS) {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 	}
 
     /**
      * Votes off the track with the playlist id <playlist-id> if and
      only if it is now playing. 
 	 *
      * @param playlistId The id of the playlist.
      * @throws IllegalStateException
      * @throws IOException
      * @throws JHGDException
      */
 	public void requestVoteOff(String playlistId) throws IllegalStateException,
 			IOException, JHGDException {
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 
 		if (!authenticated) {
 			throw new IllegalStateException("Client not authenticated");
 		}
 		
 		sendLineCommand("vo|" + playlistId);
 		String returnMessage = input.readLine();
 		if (checkServerResponse(returnMessage) != HGDConsts.SUCCESS) {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 	}
 
 	/**
 	 * Sends a file to the daemon.
 	 * 
 	 * This method implements the "q" command of the HGD protocol.
 	 * 
 	 * {"q", 1, 1, hgd_req_queue},
 	 */
 	public void requestQueue(File file) throws IllegalStateException,
 			IOException, JHGDException {
 		// Check if the connection is established
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 
 		// Check authentication
 		if (!authenticated) {
 			throw new IllegalStateException("Client not authenticated");
 		}
 
 		// Check if file exists
 
 		// Check it is not a directory
 		if (file.isDirectory()) {
 			throw new JHGDException("Cannot send a directory");
 		}
 
 		// get size
 		long fileSize = file.length();
 
 		BufferedInputStream fileInput = new BufferedInputStream(
 				new FileInputStream(file));
 
 		// send request to upload
 		sendLineCommand("q|" + file.getName() + "|" + fileSize);
 
 		// Check we are allowed
 		String returnMessage = input.readLine();
 		if (checkServerResponse(returnMessage) == HGDConsts.FAILURE) {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 
 		// send file
 		byte[] buffer = new byte[HGDConsts.BINARY_CHUNK];
 		int bytesRead = 0;
 		while ((bytesRead = fileInput.read(buffer)) != -1) {
 			fileOutput.write(buffer, 0, bytesRead);
 			fileOutput.flush();
 		}
 
 		// check server response
 		returnMessage = input.readLine();
 		if (checkServerResponse(returnMessage) != HGDConsts.SUCCESS) {
 			throw new JHGDException(returnMessage.substring(returnMessage
 					.indexOf('|') + 1));
 		}
 	}
 
 	/*
 	 * Util methods
 	 */
 	/**
 	 * Opens a socket to the server
 	 */
 	private void openSocket(String host, int port) throws IOException {
 		// Debug - attempting connection
 
 		clientSocket = new Socket(InetAddress.getByName(host), port);
 
 		output = new BufferedWriter(new OutputStreamWriter(
 				clientSocket.getOutputStream()));
 		output.flush();
 
 		fileOutput = new BufferedOutputStream(clientSocket.getOutputStream());
 
 		input = new BufferedReader(new InputStreamReader(
 				clientSocket.getInputStream()));
 
 		// Debug - done
 	}
 
 	/**
 	 * Closes the socket.
 	 */
 	private void closeSocket() {
 		try {
 			// close streams
 			if (input != null)
 				input.close();
 			if (output != null)
 				output.close();
 			if (fileOutput != null)
 				fileOutput.close();
 
 			// close socket
 			if (!clientSocket.isClosed()) {
 				clientSocket.close();
 			}
 		} catch (Exception e) {
 			System.out.println("Error closing socket");
 		} finally {
 			input = null;
 			output = null;
 			fileOutput = null;
 			clientSocket = null;
 		}
 
 	}
 
 	/**
 	 * Sends the received message to the daemon
 	 * 
 	 * @param message
 	 *            The message to be sent.
 	 * @throws IOException
 	 */
 	private void sendLineCommand(String message) throws IOException,
 			IllegalStateException {
 		if (!connected) {
 			throw new IllegalStateException("Client not connected");
 		}
 		output.write(message + "\r");
 		output.newLine();
 		output.flush();
 	}
 
 	/**
 	 * Extract the reply from the returned message.
 	 * 
 	 * @param message
 	 *            The message received from the server.
 	 * @return The corresponding return code.
 	 */
 	private int checkServerResponse(String message) {
 		if (message.substring(0, 2).equalsIgnoreCase("ok")) {
 			return HGDConsts.SUCCESS;
 		}
 		return HGDConsts.FAILURE;
 	}
 }
