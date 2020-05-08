 package edu.wcu.RTPandRTSPStreamingVideo;
 
 import java.net.UnknownHostException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.Socket;
 import java.net.ServerSocket;
 import java.net.InetAddress;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 import java.io.InterruptedIOException;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.NoSuchElementException;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.BorderLayout;
 import javax.swing.Timer;
 import javax.swing.JLabel;
 import javax.swing.JFrame;
 
 
 /**
  * Models an RTSP server.
  * usage: java Server <RTSP port>
  *
  * @author Jeremy Stilwell
  * @author Alisha Hayman
  * @author William Kreahling, based on Kurose/Ross
  * @version 10/26/13.
  */
 public class Server extends Stream
 {
 
     //Helper to build a server response since not all of the information 
     //required for this response retrievable by method calls 
     private StringBuilder responseOne = new StringBuilder();
 
     // Enumeration for readability
     private enum Message
     {
         SETUP, PLAY, PAUSE, TEARDOWN, INVALID
     }
 
     // Packet:
     /**
      * UDP packet containing the video frames
      */
     private DatagramPacket senddp;
     /**
      * Client IP address
      */
     private InetAddress ClientIPAddr;
     /**
      * Destination port for RTP packets
      */
     private int rtpDestPort;
 
     //UI:
     /**
      * Main UI window
      */
     private JFrame frame;
     /**
      * Label to display text
      */
     private JLabel label;
 
     //Video:
     /**
      * ID number for the image currently being transmitted
      */
     private int imageNum;
     /**
      * Access video frames
      */
     private VideoStream video;
 
     /**
      * RTP payload type for MJPEG video
      */
     public final static int MJPEG_TYPE = 26;
     /**
      * Frame period of the video to stream, in ms
      */
     public final static int FRAME_PERIOD = 100;
     /**
      * Length of the video in frames
      */
     public final static int VIDEO_LENGTH = 3000;
 
     /**
      * Scanner connected to the input stream of the RTSP socket
      */
     private Scanner scanIn;
     /**
      * Scanner connected to the output stream of the RTSP socket
      */
     private BufferedWriter scanOut;
 
     /**
      * Constructor that creates a server that listens an RTSP client.
      *
      * @param portNum the listening port number.
      * @throws NumberFormatException thrown if port number is incorrectly
      *                               formatted.
      * @throws IOException           thrown if an error occurs creating the
      *                               socket.
      */
     public Server(String portNum) throws NumberFormatException,
             IOException
     {
         createUI();
         int rtspPort;
 
         // Initiate TCP connection with the client for the RTSP session
         rtspPort = Integer.parseInt(portNum);
         ServerSocket listenSocket = new ServerSocket(rtspPort);
         setRtspSocket(listenSocket.accept());
         listenSocket.close();
 
         // Get Client IP address
         ClientIPAddr = getRtspSocket().getInetAddress();
 
         setInitState();
 
         // Initialize input and output streams
         scanIn = new Scanner(getRtspSocket().getInputStream());
         scanOut = new BufferedWriter(new
                 OutputStreamWriter(getRtspSocket().getOutputStream()));
 
     }
 
     /**
      * Create the server's UI. It is kind of pathetic, but the server only has
      * a UI because this is a learning experience.
      */
     private void createUI()
     {
 
         frame = new JFrame("Server");
         setRtspID(123456);  // Not really used, right now, maybe someday!
         initTimer(FRAME_PERIOD, new TimerListener());
 
         // Handler to close the main window
         frame.addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 // Stop the timer and exit
                 stopTimer();
                 System.exit(0);
             }
         });
 
         // Populate and visibilize the UI:
         label = new JLabel("Send frame #        ", JLabel.CENTER);
         frame.getContentPane().add(label, BorderLayout.CENTER);
         frame.pack();
         frame.setLocationByPlatform(true);
         frame.setVisible(true);
     }
 
     /**
      * Create a new video object connected to the movie!
      *
      * @throws IOException if we cannot create the object for some reason.
      */
     public void setVideo() throws IOException
     {
         this.video = new VideoStream(getVideoFileName());
     }
 
     /**
      * Print a usage message and end the program.
      */
     public static void printUsageAndExit()
     {
         System.out.println("usage: java Server <RTSP port>");
         System.exit(1);
 
     }
 
     /**
      * Entry point into the program. Whee
      *
      * @param args the port number for this server.
      */
     public static void main(String args[])
     {
         if (args.length != 1)
         {
             Server.printUsageAndExit();
         }
 
         Server server = null;
         try
         {
             server = new Server(args[0]);
         }
         catch (IOException | NumberFormatException ex)
         {
             System.out.println("Error creating the server: " + ex.getMessage());
             System.exit(2);
         }
         // Wait for the SETUP message from the client, good server!
         Message requestType = Message.PLAY;
         boolean done = false;
         while (!done)
         {
             requestType = server.parseRtspRequest(); // blocking call
             if (requestType == Message.SETUP)
             {
                 done = true;
                 server.setReadyState();
 
                 // Create VideoStream
                 try
                 {
                     server.sendRtspResponse();
                     server.setVideo();
                     server.setRtpSocket(new DatagramSocket());
                 }
                 catch (IOException ioe)
                 {
                     System.out.println("Error communicating with the client: " +
                             ioe.getMessage());
                     System.exit(3);
                 }
             }
         }
 
         // Loop to handle RTSP requests
         try
         {
             while (requestType != Message.TEARDOWN)
             {
                 requestType = server.parseRtspRequest(); //blocking
                 if (requestType == Message.PLAY && server.isReadyState())
                 {
                     server.sendRtspResponse();
                     server.startTimer();
                     server.setPlayState();
                 }
                 else if (requestType == Message.PAUSE &&
                         server.isPlayState())
                 {
                     server.sendRtspResponse();
                     server.stopTimer();
                     server.setReadyState();
                 }
             }
         }
         catch (IOException ioe)
         {
             System.out.println("IOException caught: " + ioe.getMessage());
             System.exit(1);
 
         }
         try
         {
             server.sendRtspResponse();
             server.stopTimer();
             server.getRtspSocket().close();
             server.getRtpSocket().close();
         }
         catch (IOException ioe)
         {
             // Ignore this because the sockets are in the process of being
             // closed down anyway.
         }
         System.exit(0);
     }
 
     /**
      * Handler for the timer. Tick tock.
      */
     class TimerListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
 
             int imageLength = 1;
             // if the current image number is less than the length of the video
             if (imageNum < VIDEO_LENGTH && imageLength > 0)
             {
                 imageNum++;
 
                 try
                 {
                     /*
                      * Get the next frame to send from the video, as well as its
                      * size
                      */
                     imageLength = video.getNextFrame(getBuffer());
 
                     // Build an RTPpacket object containing the frame
                     if (imageLength > 0)
                     {
                         RTPpacket rtpPacket = new RTPpacket(MJPEG_TYPE,
                                 imageNum,
                                 (imageNum *
                                         FRAME_PERIOD),
                                 getBuffer(),
                                 imageLength);
 
                         // Get to total length of the full RTP packet to send
                         int packetLength = rtpPacket.getlength();
                         /* 
                          * Retrieve the packet bitstream and store it in an
                          * array of bytes.
                          */
                         byte[] packetBits = new byte[packetLength];
                         rtpPacket.getpacket(packetBits);
                         /* 
                          * Send the packet as a DatagramPacket over the UDP
                          * socket.
                          */
                         senddp = new DatagramPacket(packetBits, packetLength,
                                 ClientIPAddr, rtpDestPort);
                         getRtpSocket().send(senddp);
 
                         // update UI
                         label.setText("Send frame #" + imageNum);
                     }
                 }
                 catch (Exception ex)
                 {
                     System.out.println("EXCEPTION caught: " + ex);
                     ex.printStackTrace();
                     System.exit(0);
                 }
             }
             else
             {
                 // If we have reached the end of the video file, stop the timer
                 stopTimer();
             }
         }
     }
 
     /**
      * Turn a String representation into its enum equivalent.
      * Should the enum itself do this, probably, but Meh.
      *
      * @param word the string to change into an enumerated type.
      */
     private Message string2Message(String word)
     {
         Message result = Message.INVALID;
 
         switch (word)
         { // Requires Java 7
             case "SETUP":
                 result = Message.SETUP;
                 break;
             case "PLAY":
                 result = Message.PLAY;
                 break;
             case "PAUSE":
                 result = Message.PAUSE;
                 break;
             case "TEARDOWN":
                 result = Message.TEARDOWN;
                 break;
         }
         return result;
 
     }
 
     /**
      * Figure out what the server(client?) sent to us!
      */
     private Message parseRtspRequest()
     {
         // When in doubt kill all the sockets!
         Message requestType = Message.TEARDOWN;
         try
         {
             // Parse request line and extract the requestType:
             String RequestLine = scanIn.nextLine();
             StringTokenizer tokens = new StringTokenizer(RequestLine);
             requestType = string2Message(tokens.nextToken());
 
             // if request type is setup
             if (requestType == Message.SETUP)
             {
                 //extract and set VideoFileName from RequestLine
                 setVideoFileName(tokens.nextToken());
             }
 
             //send client OK response
             responseOne.append(tokens.nextToken() + " " + OKAY + " OK");
 
             // Parse the SeqNumLine and extract CSeq field
             String SeqNumLine = scanIn.nextLine();
             tokens = new StringTokenizer(SeqNumLine);
             tokens.nextToken();
             setRtspSeqNum(Integer.parseInt(tokens.nextToken()));
 
             // Get LastLine
             String LastLine = scanIn.nextLine();
 
             if (requestType == Message.SETUP)
             {
                 // Extract rtpDestPort from LastLine
                 tokens = new StringTokenizer(LastLine);
 
                 /**
                  * Format is : "Transport: RTP/UDP; Client_port= portNum"
                  */
                 for (int i = 0; i < 3; i++)
                 {
                     tokens.nextToken(); //skip unused stuff
                 }
                 rtpDestPort = Integer.parseInt(tokens.nextToken());
             }
             // else LastLine will be the SessionId line, do not check for now.
         }
         catch (NoSuchElementException | IllegalStateException ex)
         {
             // If this happens we are borked, so quiting now, instead of
             // sending to main!
             System.out.println("Error Parsing RTSP request: " +
                     ex.getMessage());
             System.exit(1);
         }
         return (requestType);
     }
 
     /**
      * Send our response to the request!
      *
      * @throws IOException if something is wrong with the socket.
      */
     private void sendRtspResponse() throws IOException
     {
         //System.out.println("Test1: " + scanIn.hasNext() + "\n");
         // TODO
         //send Client
         scanOut.write(responseOne.toString() + "\n");
        responseOne.delete(0, responseOne.length());
 
         //Send client sequence response
         scanOut.write("CSeq: " + getRtspSeqNum() + "\n");

         //Send client session response
         scanOut.write("Session: " + getRtspID() + "\n");
         scanOut.flush();
 
 
     }
 }
