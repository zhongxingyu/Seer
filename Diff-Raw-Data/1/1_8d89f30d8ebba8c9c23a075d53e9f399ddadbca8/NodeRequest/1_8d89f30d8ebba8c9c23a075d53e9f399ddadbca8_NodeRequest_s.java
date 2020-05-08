 package myhomeaudio.server.request;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import javax.swing.Timer;
 
 import myhomeaudio.server.Node;
 import myhomeaudio.server.AudioStream;
 import myhomeaudio.server.Packet;
 
 /* Type of message sent by nodes to server (temporary)
  * 
  * id# \n request \n userId'
  * Example: 8787\nUSER\n2
  * id# is the unique identifier for the particular node (use IP)
  * 	-Some sort of identification for node, s
  * request is the type of request the node is making
  * 		- "INIT" Initialization with the server
  * 		- "PLAY" Request data, user entered room
  * 		- "SWITCH" Request new data, new user entered room
  * 		- "HALT" Stop the streaming of data, user left room
  * 				TODO-Keep queue of users in room, if user of high priority leaves, then low priority takes over
  * 		- "DISCONNECT" Node leaves, thread allocated to it by the server is removed
  * userId is optional, used for USER and SWITCH, indicates the user who entered the room
  * 		Server checks the preferences to find the necessary data to send to node.
  * 		For INIT, HALT, and DISCONNT use userId = 0
  * 
  */
 
 //TODO Remove excess System.out.println statements 
 
 /**
  * NodeRequest Class -Uses TCP connection to communicate with nodes -Receiving
  * action requests from the nodes
  * 
  * @author Ryan Brown
  */
 public class NodeRequest extends Thread implements ActionListener {
 	// Network Variables
 	protected Socket tcpSocket = null;
 	protected static DatagramSocket udpSocket = null;
 	DatagramPacket sendPacket = null;
 	DatagramPacket recvPacket = null;
 	protected int tcpPortClient = 0;
 	protected int tcpPortServer = 0;
 	protected int udpPortClient = 0;
 	protected int udpPortServer = 0;
 	protected InetAddress udpClientAddress = null;
 	// protected InetAddress localAddr = null;
 	final static int serverId = 10;
 	Node node;
 	int userId = -1;
 
 	// File variables
 	String musicName = "01 Fortune Faded.wav";
 	File musicFile = new File(musicName);
 	int audioNum = 0; // current frame of audio ready for transmission
 	int audioLen = 0; // length of the audio file
 	int audioFrameSize = 0;
 	final static int BUFFERSIZE = 15000;
 
 	// Transmitting or Receiving variables
 	int seqNumber = 0;
 	InputStream inputStream = null;
 	OutputStream outputStream = null;
 	ByteArrayInputStream bais = null;
 	ByteArrayOutputStream baos = null;
 	BufferedReader bufferedReader = null;
 	BufferedWriter bufferedWriter = null;
 	AudioStream audio = null;
 
 	// Request variables
 	final static int INIT = 0;
 	final static int PLAY = 1;
 	final static int SWITCH = 2;
 	final static int HALT = 3;
 	final static int DISCONNECT = 4;
 	final static int RECEIVED = 5;
 
 	// State variables
 	final static int INITIALIZING = 0;
 	final static int WAITING = 1;
 	final static int STREAMING = 2;
 	static int state = -1;
 
 	Timer timer;
 	byte[] buf;
 	static int frameDelay = 100;
 
 	/**
 	 * 
 	 * @param socket
 	 *            server side socket connected to client
 	 * 
 	 */
 	public NodeRequest(Socket socket, int numClients) {
 		try {
 			this.tcpSocket = socket;
 			this.tcpPortServer = socket.getLocalPort();
 			this.tcpPortClient = socket.getPort();
 			// SocketException, cannot use same port
 			// TODO use tcp connection, server picks random port and sends to
 			// client, cont. from here
 			// Make socket static?
 
 			if (udpSocket != null) {
 				if (!udpSocket.isBound()) {
 					udpSocket = new DatagramSocket(tcpPortServer);
 				}
 			} else {
 				udpSocket = new DatagramSocket(tcpPortServer);
 			}
 
 			byte[] buf = new byte[5];
 			recvPacket = new DatagramPacket(buf, buf.length);
 			System.out.println("Waiting for first datagram from client");
 			udpSocket.receive(recvPacket);
 			System.out.println("Datagram Received");
 			this.udpClientAddress = recvPacket.getAddress();
 			this.udpPortClient = recvPacket.getPort();
 			this.node = new Node(numClients);
			udpSocket.connect(udpClientAddress, udpPortClient);
 
 			sendPacket = new DatagramPacket(buf, buf.length, udpClientAddress,
 					udpPortClient);
 			udpSocket.send(sendPacket);
 
 			timer = new Timer(frameDelay, this);
 			this.timer.setInitialDelay(0);
 			this.timer.setCoalesce(true);
 
 			// allocate memory for the sending buffer
 			this.buf = new byte[BUFFERSIZE];
 		} catch (SocketException e) {
 			// TODO
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Parses requests made by the node.
 	 * 
 	 * @return The request being made.
 	 */
 	private int parseRequest() {
 		try {
 			if (!bufferedReader.ready()) {
 				// System.out.println("BufferedReader Not Ready");
 				return -1;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return -1;
 		}
 		System.out.println("Server - Client Msg Received");
 		String requestState = null;
 		int senderId = 0;
 		int userId = 0;
 		int request = -1;
 		try {
 			String temp = bufferedReader.readLine();
 			// System.out.println(temp);
 			senderId = Integer.parseInt(temp);
 			// System.out.println("NodeId: "+nodeId);
 			requestState = bufferedReader.readLine();
 			// System.out.println("RequestState: "+requestState);
 			temp = bufferedReader.readLine();
 			// System.out.println(temp);
 			userId = Integer.parseInt(temp);
 			System.out.println("Server - Client Msg: " + senderId + " "
 					+ requestState + " " + userId);
 
 			if (requestState.equals("INIT")) {
 				request = INIT;
 			} else if (requestState.equals("PLAY")) {
 				request = PLAY;
 			} else if (requestState.equals("SWITCH")) {
 				request = SWITCH;
 			} else if (requestState.equals("HALT")) {
 				request = HALT;
 			} else if (requestState.equals("DISCONNECT")) {
 				request = DISCONNECT;
 			} else if (requestState.equals("RECEIVED")) {
 				request = RECEIVED;
 			}
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return request;
 	}
 
 	private void sendResponse(String request) {
 		try {
 			/*
 			 * The server sends two kinds of messages INIT of the form -
 			 * serverId \n request \n nodeId \n ServerId - Ident of server,
 			 * normally 0 Request - Type of request of server to client -
 			 * RECEIVED - returns after successful client request msg - INIT -
 			 * returns if client sends INIT, so client knows to set nodeId for
 			 * future transmissions nodeId - Always includes nodeId the server
 			 * is talking to
 			 */
 			String msg = serverId + "\n" + request + "\n" + node.getId() + "\n";
 			System.out.println(serverId + " " + request + " " + node.getId() + " ");
 			bufferedWriter.write(msg);
 			bufferedWriter.flush();
 			System.out.println("Server - Sent response to Client.");
 		} catch (IOException ex) {
 			System.out.println("Exception caught: " + ex);
 			System.exit(0);
 		}
 	}
 
 	public void run() {
 		// Initialize server state
 		this.state = INITIALIZING;
 		try {
 			this.inputStream = this.tcpSocket.getInputStream();
 			this.outputStream = this.tcpSocket.getOutputStream();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
 		bufferedWriter = new BufferedWriter(
 				new OutputStreamWriter(outputStream));
 
 		// Get initial request from client, should be INIT request
 		int requestType = -1;
 		boolean setup = false;
 		while (!setup) {
 			requestType = parseRequest();
 			if (requestType == INIT) {
 				setup = true;
 				state = WAITING;
 				sendResponse("INIT");
 			}
 		}
 		while (true) {
 			requestType = parseRequest();
 			if (requestType == INIT) {
 				// Node requesting another initialization?
 			} else if (requestType == PLAY && state == WAITING) {
 				// Node wants audio stream and server ready to stream
 				sendResponse("RECEIVED");
 				audio = new AudioStream(musicFile);
 				audioLen = (int) audio.getNumFrames();
 				audioFrameSize = audio.getFrameSize();
 				System.out.println("Streaming Audio: " + musicName);
 				timer.start();
 
 			} else if (requestType == SWITCH && state == STREAMING) {
 				// Node needs new audio stream and server currently streaming
 				// TODO complete
 			} else if (requestType == HALT && state == STREAMING) {
 				// Node requests streaming halt
 				// TODO complete
 			} else if (requestType == DISCONNECT) {
 				// Node wants to disconnect
 				// TODO complete
 			}
 		}
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		System.out.println("Streaming: " + audioNum + " "
 				+ ((audioLen * audioFrameSize) / BUFFERSIZE));
 		if (audioNum < ((audioLen * audioFrameSize) / BUFFERSIZE)) {
 			audioNum++;
 			try {
 				// get next frame to send
 				int audioSize = audio.getNextFrame(buf);
 
 				// Builds a Packet
 				Packet packet = new Packet(audioNum, buf, audioSize);
 
 				// get to total length of the packet
 				int packetLen = packet.getLength();
 
 				// get packet stream
 				byte[] packet_bits = new byte[packetLen];
 				packet.getPacket(packet_bits);
 
 				// send the packet over the UDP socket
 				sendPacket = new DatagramPacket(packet_bits, packetLen,
 						udpClientAddress, udpPortClient);
 				udpSocket.send(sendPacket);
 				System.out.println("Send frame #" + audioNum);
 			} catch (Exception ex) {
 				System.out.println("Exception caught: " + ex);
 				System.exit(0);
 			}
 		} else {
 			// if we have reached the end of the audio file, stop the timer
 			timer.stop();
 		}
 	}
 }
