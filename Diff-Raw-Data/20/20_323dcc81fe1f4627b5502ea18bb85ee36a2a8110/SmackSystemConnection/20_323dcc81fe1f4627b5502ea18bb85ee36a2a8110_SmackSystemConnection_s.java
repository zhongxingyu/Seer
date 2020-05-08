 /*
  * SmackSystemConnection.java
  *
  * Created on August 9, 2010, 2:34 PM
  * @author jaycverg
  */
 
 package com.rameses.messaging.xmpp;
 
 import com.rameses.messaging.SystemConnection;
 import com.rameses.messaging.SystemMessage;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.DefaultPacketExtension;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.PacketExtension;
 
 
 public class SmackSystemConnection extends SystemConnection implements PacketListener, PacketFilter, ConnectionListener {
     
     public static final String NAME_SPACE = "smack:asyncextension2";
     private XMPPConnection conn;
     private boolean autoCreateAccount = true;
     private boolean connected;
     
     
     public SmackSystemConnection() {
         setPort(5222);
     }
     
     public void open() throws Exception {
         ConnectionConfiguration conf = new ConnectionConfiguration(getHost(), getPort());
         conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
         
         conn = new XMPPConnection(conf);
         conn.connect();
         conn.addConnectionListener(this);
         conn.addPacketListener(this, this);
         
         connected = true;
         super.notifyConnected();
         
         String username = getUsername();
         String password = getPassword();
         
         //force create the account.
         if ( autoCreateAccount ) {
             try {
                 conn.getAccountManager().createAccount(username, password);
             } catch(Exception ign){;}
         }
         
         conn.login(username, password);
     }
     
     public void close() {
         if ( autoCreateAccount ) {
             try {
                 conn.getAccountManager().deleteAccount();
             } catch(Exception ign){;}
         }
         conn.disconnect();
     }
     
     public void sendMessage(SystemMessage message) {
         Message msg = (Message) message.getMessage();
         msg.setBody("REQUEST-ID:" + message.getRequestId() + "\n" + "PUSH-ID:" + message.getPushId());
         
         PacketExtension pe = new DefaultPacketExtension(NAME_SPACE, NAME_SPACE);
         msg.addExtension(pe);
         
         conn.sendPacket( msg );
     }
     
     public SystemMessage createSystemMessage(Map properties) {
         return new SmackSystemMessage();
     }
     
     
     public void processPacket(Packet packet) {
         org.jivesoftware.smack.packet.Message sm = (org.jivesoftware.smack.packet.Message) packet;
         SmackSystemMessage msg = new SmackSystemMessage();
         msg.setMessage( sm );
         
         String resp = sm.getBody();
         String exp = "REQUEST-ID:(.*)\nPUSH-ID:(.*)";
         Matcher m = Pattern.compile(exp).matcher( resp );
         if ( m.matches() ) {
             msg.setRequestId( m.group(1) );
             msg.setPushId( m.group(2) );
         }
         
         super.processMessage( msg );
     }
     
     public boolean accept(Packet packet) {
         return packet.getExtension(NAME_SPACE) != null;
     }
     
     //---- connection listening support
     public void connectionClosed() {
         connected = false;
         super.notifyDisconnected();
     }
     
     public void connectionClosedOnError(Exception e) {
         connected = false;
         super.notifyDisconnected();
     }
     
     public void reconnectionSuccessful() {
         connected = true;
         super.notifyConnected();
     }
     
     public void reconnectingIn(int i) {}
     public void reconnectionFailed(Exception exception) {}
     
     
     //<editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     public boolean isAutoCreateAccount() {
         return autoCreateAccount;
     }
     
     public void setAutoCreateAccount(boolean autoCreateAccount) {
         this.autoCreateAccount = autoCreateAccount;
     }
     
     public boolean isConnected() {
         return connected;
     }
     //</editor-fold>
 }
