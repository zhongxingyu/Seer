 package pratica02;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.Timer;
 
 public class Server implements Runnable {
     
     ///###Shared variables
     public static List<Listener> clientList=new ArrayList<Listener>();
     public static Timer timer;
     
     //###Usefull Constants
     final static String CRLF = "\r\n";
     
     //###RTSP variables
     //* rtsp states
     final static int INIT = 0;
     final static int READY = 1;
     final static int PLAYING = 2;
     //* rtsp message types
     final static int SETUP = 3;
     final static int TEARDOWN = 6;
     
     //RTSP Server state == INIT or READY or PLAY
     public int state; 
     
     public int RTSP_ID = 123456; //ID of the RTSP session
     public int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
     
     public Socket RTSPsocket;
     public BufferedWriter RTSPBufferedWriter;
     public BufferedReader RTSPBufferedReader;
     public InetAddress clientIPAddr;
     public int RTPClientPort;
     public DatagramSocket datagramSocket;
     
     
     public Server(Socket socket) {
         super();
         System.out.println("Accepted resquest from :"+ socket.getInetAddress());
         this.RTSPsocket = socket;
     }
     
     @Override
     public void run() {
         //*Init buffers
         try {
             RTSPBufferedReader= new BufferedReader(new InputStreamReader(RTSPsocket.getInputStream()) );
             RTSPBufferedWriter= new BufferedWriter(new OutputStreamWriter(RTSPsocket.getOutputStream()) );
         } catch (IOException e) { e.printStackTrace();}
         
         //*Init clientIpAdress
         clientIPAddr=RTSPsocket.getInetAddress();
         
         //*Init server state
         state=INIT;
         
         //###Wait for the SETUP message
         waitRSTPMessage(SETUP);
         
         //* update RTSP state
         state = READY;
 
         //* send response 200 OK
         send_RTSP_response();
 
         //*Init datagramSocket and add the server in the list to start sending
         try {
             datagramSocket = new DatagramSocket();
             startSending(datagramSocket,RTPClientPort,clientIPAddr);
         } catch (SocketException e) {
             e.printStackTrace();
         }
         finally {
             try {
                 RTSPsocket.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     //Start sending frame to the client
     //-------------------------------
     public void startSending(DatagramSocket datagramSocket,int RTPClientPort,InetAddress clientIPAddr) {
         System.out.println("startSending to Client on port: "+RTPClientPort);
         Listener newClient=new Listener(datagramSocket, RTPClientPort, clientIPAddr);
         
         if(Server.clientList.size()==0) {
             System.out.println("Starting timer");
             Server.timer.start();
         }
         
         try {
             
             //*Add client into the list
             synchronized (clientList) {
                 System.out.println("add Client on port: "+RTPClientPort);
                 clientList.add(newClient);
             }
             waitRSTPMessage(TEARDOWN);
         }
         finally {
            //*When the user click on TEARDOWN, why remove the client from the list
             synchronized (clientList) {
                 clientList.remove(newClient);
             }
         }
     }
     
     
     //Parse RTSP Request
     //------------------------------------
     public int parse_RTSP_request()
     {
       int request_type = -1;
       try{
         //* parse request line and extract the request_type:
         String RequestLine = RTSPBufferedReader.readLine();
         //* print the request
         System.out.println(RequestLine);
 
         StringTokenizer tokens = new StringTokenizer(RequestLine);
         String request_type_string = tokens.nextToken();
 
         //* convert to request_type structure:
         if ((new String(request_type_string)).compareTo("SETUP") == 0)
       request_type = SETUP;
         else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
       request_type = TEARDOWN;
 
         //* parse the SeqNumLine and extract CSeq field
         String SeqNumLine = RTSPBufferedReader.readLine();
         System.out.println(SeqNumLine);
         tokens = new StringTokenizer(SeqNumLine);
         tokens.nextToken();
         RTSPSeqNb = Integer.parseInt(tokens.nextToken());
 
         //* get LastLine
         String LastLine = RTSPBufferedReader.readLine();
         System.out.println(LastLine);
 
         if (request_type == SETUP)
         {
         //* extract RTPClientPort from LastLine
         tokens = new StringTokenizer(LastLine);
         for (int i=0; i<3; i++)
           tokens.nextToken(); //skip unused stuff
         RTPClientPort = Integer.parseInt(tokens.nextToken());
         }
       }
       catch(Exception ex)
         {
       System.out.println("Exception caught: "+ex);
       System.exit(0);
         }
       return(request_type);
     }
     
     //Send RTSP Response
     //------------------------------------
     public void send_RTSP_response()
     {
       try{
         RTSPBufferedWriter.write("RTSP/1.0 200 OK"+CRLF);
         RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);
         RTSPBufferedWriter.write("Session: "+RTSP_ID+CRLF);
         RTSPBufferedWriter.flush();
         System.out.println("RTSP Server - Sent response to Client.");
       }
       catch(Exception ex)
         {
       System.out.println("Exception caught: "+ex);
       System.exit(0);
         }
     }
     
     //Wait RTSP Message
     //------------------------------------
     public void waitRSTPMessage(int message) {
         int request_type;
         boolean done = false;
         while(!done) {
             request_type = parse_RTSP_request(); //blocking
             if (request_type == message) { 
                 done = true;
                 System.out.println("RTSP message : "+message+" received");
             }
         }
     }
 
 }
