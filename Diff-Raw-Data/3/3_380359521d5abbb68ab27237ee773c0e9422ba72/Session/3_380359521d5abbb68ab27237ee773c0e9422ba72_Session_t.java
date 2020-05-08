 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmppclient.jingle;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jivesoftware.smack.XMPPConnection;
 import xmppclient.jingle.packet.Jingle;
 
 /**
  *
  * @author Lee Boynton (323326)
  */
 public abstract class Session
 {
     private XMPPConnection xmppConnection;
     private String responder;
     private String sid;
 
     public Session(XMPPConnection xmppConnection, String responder)
     {
         this.xmppConnection = xmppConnection;
         this.responder = responder;
     }
 
     public Session(XMPPConnection xmppConnection, String responder, String sid)
     {
         this.xmppConnection = xmppConnection;
         this.responder = responder;
         this.sid = sid;
     }
 
     public abstract void start();
 
     public void terminate()
     {
         Jingle terminate = new Jingle(Jingle.Action.SESSIONTERMINATE);
         terminate.setTo(responder);
         terminate.setFrom(xmppConnection.getUser());
         terminate.setSid(sid);
         xmppConnection.sendPacket(terminate);
     }
 
     public String getResponder()
     {
         return responder;
     }
 
     public String getSid()
     {
         return sid;
     }
 
     public XMPPConnection getXmppConnection()
     {
         return xmppConnection;
     }
 
     public int getFreePort()
     {
         int port = 0;
         try
         {
             ServerSocket socket = new ServerSocket(0);
             port = socket.getLocalPort();
             socket.close();
         }
         catch (IOException ex)
         {
             Logger.getLogger(JingleManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return port;
     }
     
     public String getHostAddress()
     {
         String address = null;
         
         try
         {
            InetAddress addr = InetAddress.getLocalHost();
             address = addr.getHostAddress();
         }
         catch (UnknownHostException ex)
         {
             Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return address;
     }
 }
