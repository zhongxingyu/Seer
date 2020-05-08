 /**
  * 
  */
 package edu.gsu.cs.networks;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 /**
  * Open an SMTP connection to a mailserver and send one mail.
  */
 public class SMTPConnection 
 {
     /* The socket to the server */
     private Socket connection;
 
     /* Streams for reading and writing the socket */
     private BufferedReader fromServer; // InFromServer - characters that arrive from the network
     private DataOutputStream toServer; // OutToServer - characters that the client sends to the network
     
     private static final int SMTP_PORT = 25;
     private static final String CRLF = "\r\n";
 
     /* Are we connected? Used in close() to determine what to do. */
     private boolean isConnected = false;
 
     /* Create an SMTPConnection object
      * Create the socket and the associated streams. Initialize SMTP connection.
      */
     public SMTPConnection(Envelope envelope) throws IOException
     {
 		connection = new Socket("sphinx.gsu.edu", SMTP_PORT);
 		fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 		toServer   = new DataOutputStream(connection.getOutputStream());
 		
 		/* Read a line from server and check that the reply code is 220.
 		 * If not, throw an IOException.
 		 */
 		if (parseReply(fromServer.readLine()) != 220) {
 			throw new IOException();
 		} 
 		
 		/* SMTP handshake. We need the name of the local machine.
 		 * Send the appropriate SMTP handshake command.
 		 */
 		try {
 			InetAddress addr = InetAddress.getLocalHost();
 			String localhost = addr.getHostName();
 			sendCommand("HELO " + localhost,250);
 		} catch (UnknownHostException e) {
 		}
 
 		isConnected = true;
     }
 
     /* Close the connection. First, terminate on SMTP level, then close the socket. */
     public void close()
     {
 		isConnected = false;
 		try {
 			sendCommand("QUIT",221);
 		    connection.close();
 		} catch (IOException e) {
 		    System.out.println("Unable to close connection: " + e);
 		    isConnected = true;
 		}
     }
 
     /* Parse the reply line from the server. Returns the reply code. */
     private int parseReply(String reply)
     {
     	String[] result = reply.split("\\s");
     	System.out.println("rc=" + result[0] + ", fromServer=" + reply);
     	return Integer.parseInt(result[0]); 
     }
     
     /* Send the message.
      * Write the correct SMTP-commands in the correct order.
      * No checking for errors, just throw them to the caller.
      */
     public void send(Envelope envelope) throws IOException
     {
     	/* Send all the necessary commands to send a message.
 		 * Call sendCommand() to do the dirty work.
 		 * Do _not_ catch the exception thrown from sendCommand().
 		 * 
 		 * MAIL FROM:<a@b.com> CRLF, 250
 		 * RECPT TO:<a@b.com> CRLF, 250
 		 * DATA CRLF, 354
 		 * envelope.message CRLF,354
 		 * . CRLF, 354
 		 * QUIT, 221
 		 */
     	sendCommand("MAIL FROM:<" + envelope.Sender + ">", 250);
     	sendCommand("RCPT TO:<" + envelope.Recipient + ">", 250);
     	sendCommand("DATA",354);
    	sendCommand(envelope.Message + CRLF + ".",250);
     }
 
     /* Send an SMTP command to the server.
      * Check that the reply code is what is is supposed to be according to RFC 821.
      * @var rc reply code
      */
     private void sendCommand(String command, int rc) throws IOException
     {
 		/* Write command to server and read reply from server. */
     	toServer.writeBytes(command + CRLF);
     	String serverResponse = fromServer.readLine();
 		
     	/* Check that the server's reply code is the same as the parameter rc.
     	 * If not, throw an IOException.
     	 */
        	if (parseReply(serverResponse) != rc) {
     		throw new IOException();
     	}
     }
 
     /* Destructor. Closes the connection if something bad happens. */
     protected void finalize() throws Throwable
     {
     	if (isConnected) {
     		close();
     	}
     	super.finalize();
     }
 }
