 package pratica01;
 /* ------------------
    Client
    usage: java Client [Server hostname] [Server RTSP listening port] [Video file requested]
    ---------------------- */
   //How to use the client
   //----------------
   //1.    Compile with javac 
   //2.    Run : *java Client [Server hostname] [Server RTSP listening port] [Video file requested path] [Listening RTP Client Port]*
   //
   //      example: `java 127.0.0.1 3000 src/pratica01/movie.Mjpeg 2500`
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.Timer;
 
 public class Client{
   //Variables
   //---------
   //###GUI variables
   JFrame f = new JFrame("Client");
   JButton setupButton = new JButton("Setup");
   JButton playButton = new JButton("Play");
   JButton pauseButton = new JButton("Pause");
   JButton tearButton = new JButton("Teardown");
   JPanel mainPanel = new JPanel();
   JPanel buttonPanel = new JPanel();
   JLabel iconLabel = new JLabel();
   ImageIcon icon;
 
 
   //###RTP variables:
   DatagramPacket rcvdp; //UDP packet received from the server
   DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
   public int RTP_RCV_PORT; //port where the client will receive the RTP packets
 
   Timer timer; //timer used to receive data from the UDP socket
   byte[] buf; //buffer used to store data received from the server
 
   //###RTSP variables
   //* rtsp states
   final static int INIT = 0;
   final static int READY = 1;
   final static int PLAYING = 2;
   static int state; //RTSP state == INIT or READY or PLAYING
   Socket RTSPsocket; //socket used to send/receive RTSP messages
   //* input and output stream filters
   public BufferedReader RTSPBufferedReader;
   public BufferedWriter RTSPBufferedWriter;
   public String VideoFileName; //video file to request to the server
   public int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
   public int RTSPid = 0; //ID of the RTSP session (given by the RTSP Server)
 
   final static String CRLF = "\r\n";
 
   //* video constants
   static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
 
   //Constructor
   //----------
   public Client() {
 
     //###Build GUI
 
     //* frame
     f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
 	 System.exit(0);
        }
     });
 
     //* init buttons linked to handlers
     buttonPanel.setLayout(new GridLayout(1,0));
     buttonPanel.add(setupButton);
     buttonPanel.add(playButton);
     buttonPanel.add(pauseButton);
     buttonPanel.add(tearButton);
     setupButton.addActionListener(new setupButtonListener());
     playButton.addActionListener(new playButtonListener());
     pauseButton.addActionListener(new pauseButtonListener());
     tearButton.addActionListener(new tearButtonListener());
 
     //* image display label
     iconLabel.setIcon(null);
 
     //* frame layout
     mainPanel.setLayout(null);
     mainPanel.add(iconLabel);
     mainPanel.add(buttonPanel);
     iconLabel.setBounds(0,0,380,280);
     buttonPanel.setBounds(0,280,380,50);
 
     f.getContentPane().add(mainPanel, BorderLayout.CENTER);
     f.setSize(new Dimension(390,370));
     f.setVisible(true);
 
     //###Init timer
     timer = new Timer(20, new timerListener());
     timer.setInitialDelay(0);
     timer.setCoalesce(true);
 
     //###Init buffer
     //* allocate enough memory for the buffer used to receive data from the server
     buf = new byte[15000];
   }
 
   //Main
   //-----
   public static void main(String argv[]) throws Exception
   {
     //* create a Client object
     Client theClient = new Client();
 
     //* get server RTSP port and IP address from the command line
     int RTSP_server_port = Integer.parseInt(argv[1]);
     String ServerHost = argv[0];
     InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);
 
     //* get video filename to request:
     theClient.VideoFileName = argv[2];
     
     //* get RTP Receive Port from the command line
     theClient.RTP_RCV_PORT = Integer.parseInt(argv[3]);
 
     //* establish a TCP connection with the server to exchange RTSP messages
     theClient.RTSPsocket = new Socket(ServerIPAddr, RTSP_server_port);
 
     //* set input and output stream filters:
     theClient.RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()) );
     theClient.RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()) );
 
     //* init RTSP state:
     state = INIT;
     
     System.out.println("Init Success");
   }
   
   //Handlers for buttons Setup,Play,Pause,Teardown
   //------------------------------------
 
   //### Setup button handler
   class setupButtonListener implements ActionListener{
     public void actionPerformed(ActionEvent e){
 
       System.out.println("Setup Button pressed !");
 
       if (state == INIT)
 	{
 	  //*    init non-blocking RTPsocket that will be used to receive data
 	  try{
 	  //    *construct a new DatagramSocket to receive RTP packets from the server, on port RTP_RCV_PORT*
 	    RTPsocket = new DatagramSocket(RTP_RCV_PORT);
 
 	  //    *set TimeOut value of the socket to 5msec.*
 	    RTPsocket.setSoTimeout(5);
 
 	  }
 	  catch (SocketException se)
 	    {
 	      System.out.println("Socket exception: "+se);
 	      System.exit(0);
 	    }
 
 	  //* init RTSP sequence number
 	  RTSPSeqNb = 1;
 
 	  //* send SETUP message to the server
 	  send_RTSP_request("SETUP");
 
 	  //* wait for the response
 	  if (parse_server_response() != 200)
 	    System.out.println("Invalid Server Response");
 	  else
 	    {
 	      //* change RTSP state and print new state
 	      state = READY;
 	      System.out.println("New RTSP state: READY");
 	    }
 	}//else if state != INIT then do nothing
     }
   }
 
    //### Play button handler
   class playButtonListener implements ActionListener {
     public void actionPerformed(ActionEvent e){
 
       System.out.println("Play Button pressed !");
 
       if (state == READY)
 	{
 	  //* increase RTSP sequence number
 	  RTSPSeqNb++;
 
 
 	  //* send PLAY message to the server
 	  send_RTSP_request("PLAY");
 
 	  //* wait for the response
 	  if (parse_server_response() != 200)
 		  System.out.println("Invalid Server Response");
 	  else
 	    {
 	      //* change RTSP state and print out new state
 	      state=PLAYING;
 	      System.out.println("New RTSP state: PLAYING");
 
 	      //* start the timer
 	      timer.start();
 	    }
 	}//else if state != READY then do nothing
     }
   }
 
 
     //### Pause button handler
   class pauseButtonListener implements ActionListener {
     public void actionPerformed(ActionEvent e){
 
       System.out.println("Pause Button pressed !");
 
       if (state == PLAYING)
 	{
 	  //* increase RTSP sequence number
 	  RTSPSeqNb++;
 
 	  //* send PAUSE message to the server
 	  send_RTSP_request("PAUSE");
 
 	  //* wait for the response
 	 if (parse_server_response() != 200)
 		  System.out.println("Invalid Server Response");
 	  else
 	    {
 	      //* change RTSP state and print out new state
 	      state=READY;
 	      System.out.println("New RTSP state: READY");
 
 	      //* stop the timer
 	      timer.stop();
 	    }
 	}//else if state != PLAYING then do nothing
     }
   }
 
     //### Teardown button handler
   class tearButtonListener implements ActionListener {
     public void actionPerformed(ActionEvent e){
 
       System.out.println("Teardown Button pressed !");
 
       //* increase RTSP sequence number
       RTSPSeqNb++;
 
       //* send TEARDOWN message to the server
       send_RTSP_request("TEARDOWN");
 
       //* wait for the response
       if (parse_server_response() != 200)
 	System.out.println("Invalid Server Response");
       else
 	{
 	  //* change RTSP state and print out new state
      state=INIT;
 	  System.out.println("New RTSP state: INIT");
 
 	  //* stop the timer
 	  timer.stop();
 
 	  //* exit
 	  System.exit(0);
 	}
     }
   }
   
   //Handler for timer
   //------------------------------------
   //*It read the data socket (RTPsocket) and display the frame every tick*
   class timerListener implements ActionListener {
     public void actionPerformed(ActionEvent e) {
 
       //* construct a DatagramPacket to receive data from the UDP socket
       rcvdp = new DatagramPacket(buf, buf.length);
 
       try{
 	//* receive the DP from the socket:
 	RTPsocket.receive(rcvdp);
 
 	//* create an RTPpacket object from the DP
 	RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());
 
 	//* print important header fields of the RTP packet received:
 	System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());
 
 	//* print header bitstream:
 	rtp_packet.printheader();
 
 	//* get the payload bitstream from the RTPpacket object
 	int payload_length = rtp_packet.getpayload_length();
 	byte [] payload = new byte[payload_length];
 	rtp_packet.getpayload(payload);
 
 	//* get an Image object from the payload bitstream
 	Toolkit toolkit = Toolkit.getDefaultToolkit();
 	Image image = toolkit.createImage(payload, 0, payload_length);
 
 	//* display the image as an ImageIcon object
 	icon = new ImageIcon(image);
 	iconLabel.setIcon(icon);
       }
       catch (InterruptedIOException iioe){
 	System.out.println("Nothing to read");
       }
       catch (IOException ioe) {
 	System.out.println("Exception caught: "+ioe);
       }
     }
   }
 
   //Parse Server Response
   //------------------------------------
   //*Function use to read and parse the information sended by the server on the RTSP socket*  
   //*return 200 if OK, 0 else*
   private int parse_server_response()
   {
     int reply_code = 0;
 
     try{
       //parse status line and extract the reply_code:
       String StatusLine = RTSPBufferedReader.readLine();
       System.out.println("RTSP Client - Received from Server:");
       System.out.println(StatusLine);
 
       StringTokenizer tokens = new StringTokenizer(StatusLine);
       tokens.nextToken(); //skip over the RTSP version
       reply_code = Integer.parseInt(tokens.nextToken());
 
       //if reply code is OK get and print the 2 other lines
       if (reply_code == 200)
 	{
 	  String SeqNumLine = RTSPBufferedReader.readLine();
 	  System.out.println(SeqNumLine);
 
 	  String SessionLine = RTSPBufferedReader.readLine();
 	  System.out.println(SessionLine);
 
 	  //if state == INIT gets the Session Id from the SessionLine
 	  tokens = new StringTokenizer(SessionLine);
 	  tokens.nextToken(); //skip over the Session:
 	  RTSPid = Integer.parseInt(tokens.nextToken());
 	}
     }
     catch(Exception ex)
       {
 	System.out.println("Exception caught: "+ex);
 	System.exit(0);
       }
 
     return(reply_code);
   }
 
   //Send RTSP Request
   //------------------------------------
   //*Send message to the server on the RTSP socket*
   //
   //*Example of message:*
   //
   //        PLAY movie.Mjpeg RTSP/1.0  
   //        CSeq: 2  
   //        Session: 123456  
   private void send_RTSP_request(String request_type)
   {
     try{
       //use the RTSPBufferedWriter to write to the RTSP socket
 
       //write the request line
       RTSPBufferedWriter.write(request_type+" "+VideoFileName+" "+"RTSP/1.0"+CRLF);
 
       //write the CSeq line
       RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);
 
       //check if request type is equal to "SETUP" and in this case write the Transport: line advertising to the server the port used to receive the RTP packets RTP_RCV_PORT
       if(request_type.equals("SETUP")) {
           RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= "+RTP_RCV_PORT+CRLF);
       }
       //otherwise, write the Session line from the RTSPid field
       else {
           RTSPBufferedWriter.write("Session: "+RTSPid+CRLF);
       }
 
       RTSPBufferedWriter.flush();
     }
     catch(Exception ex)
       {
 	System.out.println("Exception caught: "+ex);
       }
   }
 
 }//end of Class Client
 
