 package davmail.smtp;
 
 import davmail.AbstractConnection;
 import davmail.tray.DavGatewayTray;
 import davmail.exchange.ExchangeSession;
 import sun.misc.BASE64Decoder;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 /**
  * Dav Gateway smtp connection implementation
  */
 public class SmtpConnection extends AbstractConnection {
     protected static final int INITIAL = 0;
     protected static final int AUTHENTICATED = 1;
     protected static final int STARTMAIL = 2;
     protected static final int RECIPIENT = 3;
     protected static final int MAILDATA = 4;
 
     // Initialize the streams and start the thread
     public SmtpConnection(Socket clientSocket) {
         super(clientSocket);
     }
 
     public void run() {
         String line;
         StringTokenizer tokens;
 
         try {
             ExchangeSession.checkConfig();
             sendClient("220 DavMail SMTP ready at " + new Date());
             for (; ;) {
                 line = readClient();
                 // unable to read line, connection closed ?
                 if (line == null) {
                     break;
                 }
 
                 tokens = new StringTokenizer(line);
                 if (tokens.hasMoreTokens()) {
                     String command = tokens.nextToken();
 
                     if ("QUIT".equalsIgnoreCase(command)) {
                         sendClient("221 Closing connection");
                         break;
                     } else if ("EHLO".equals(command)) {
                        sendClient("250-" + tokens.nextToken());
                         // inform server that AUTH is supported
                         // actually it is mandatory (only way to get credentials)
                         sendClient("250-AUTH LOGIN PLAIN");
                         sendClient("250 Hello");
                     } else if ("HELO".equals(command)) {
                         sendClient("250 Hello");
                     } else if ("AUTH".equals(command)) {
                         if (tokens.hasMoreElements()) {
                             String authType = tokens.nextToken();
                             if ("PLAIN".equals(authType) && tokens.hasMoreElements()) {
                                 decodeCredentials(tokens.nextToken());
                                 session = new ExchangeSession();
                                 try {
                                     session.login(userName, password);
                                     sendClient("235 OK Authenticated");
                                     state = AUTHENTICATED;
                                 } catch (Exception e) {
                                     String message = e.getMessage();
                                     if (message == null) {
                                         message = e.toString();
                                     }
                                     DavGatewayTray.error(message);
                                     message = message.replaceAll("\\n", " ");
                                     sendClient("554 Authenticated failed " + message);
                                     state = INITIAL;
                                 }
                             } else {
                                 sendClient("451 Error : unknown authentication type");
                             }
                         } else {
                             sendClient("451 Error : authentication type not specified");
                         }
                     } else if ("MAIL".equals(command)) {
                         if (state == AUTHENTICATED) {
                             state = STARTMAIL;
                             sendClient("250 Sender OK");
                         } else {
                             state = INITIAL;
                             sendClient("503 Bad sequence of commands");
                         }
                     } else if ("RCPT".equals(command)) {
                         if (state == STARTMAIL || state == RECIPIENT) {
                             state = RECIPIENT;
                             sendClient("250 Recipient OK");
                         } else {
                             state = AUTHENTICATED;
                             sendClient("503 Bad sequence of commands");
                         }
                     } else if ("DATA".equals(command)) {
                         if (state == RECIPIENT) {
                             state = MAILDATA;
                             sendClient("354 Start mail input; end with <CRLF>.<CRLF>");
 
                             try {
                                 session.sendMessage(in);
                                 state = AUTHENTICATED;
                                 sendClient("250 Queued mail for delivery");
                             } catch (Exception e) {
                                 DavGatewayTray.error("Authentication failed", e);
                                 state = AUTHENTICATED;
                                 sendClient("451 Error : " + e + " " + e.getMessage());
                             }
 
                         } else {
                             state = AUTHENTICATED;
                             sendClient("503 Bad sequence of commands");
                         }
                     }
 
                 } else {
                     sendClient("500 Unrecognized command");
                 }
 
                 os.flush();
             }
         } catch (IOException e) {
             DavGatewayTray.error(e.getMessage());
             try {
                 sendClient("500 " + e.getMessage());
             } catch (IOException e2) {
                 DavGatewayTray.debug("Exception sending error to client", e2);
             }
         } finally {
             close();
         }
         DavGatewayTray.resetIcon();
     }
 
     /**
      * Decode SMTP credentials
      *
      * @param encodedCredentials smtp encoded credentials
      * @throws java.io.IOException if invalid credentials
      */
     protected void decodeCredentials(String encodedCredentials) throws IOException {
         BASE64Decoder decoder = new BASE64Decoder();
         String decodedCredentials = new String(decoder.decodeBuffer(encodedCredentials));
         int index = decodedCredentials.indexOf((char) 0, 1);
         if (index > 0) {
             userName = decodedCredentials.substring(1, index);
             password = decodedCredentials.substring(index + 1);
         } else {
             throw new IOException("Invalid credentials");
         }
     }
 
 }
 
