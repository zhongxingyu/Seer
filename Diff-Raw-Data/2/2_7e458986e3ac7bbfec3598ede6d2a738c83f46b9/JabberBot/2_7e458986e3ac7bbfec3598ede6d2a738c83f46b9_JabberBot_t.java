 package com.kdab.restbot;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 import java.util.concurrent.BlockingQueue;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 public class JabberBot implements Runnable {
     public JabberBot( BlockingQueue<Message> in, Account account, String nick, Vector<String> roomsToJoin ) {
         m_in = in;
         m_account = account;
         m_roomsToJoin = roomsToJoin;
         m_nick = nick;
         m_rooms = new HashMap<String, MultiUserChat>();
     }
 
     public void run() {
         try {
             login();
             joinRooms();
         } catch ( XMPPException e ) {
             System.err.println( e );
             // TODO how to report?
         }
         try {
             while ( true ) {
                 Message msg = m_in.take();
                 if ( msg.isPoison() ) {
                     logout();
                     return;
                 }
                 try {
                     send( msg );
                 } catch ( XMPPException e ) {
                     System.err.println( e );
                     // TODO how to report?
                 }
             }
         } catch ( InterruptedException e ) {
             logout();
             Thread.currentThread().interrupt();
         }
     }
 
     private void send( Message msg ) throws XMPPException {
         for ( Message.Receiver i : msg.receivers() ) {
             final String rec = i.receiver;
             assert (rec != null);
             assert (!rec.isEmpty());
 
             if ( i.type == Message.ReceiverType.User ) {
                 Chat chat = m_connection.getChatManager().createChat( rec, null );
                 chat.sendMessage( msg.text() );
             } else {
                 MultiUserChat c = m_rooms.get( rec );
                 if ( c != null ) {
                     c.sendMessage( msg.text() );
                 } else {
                     // report?
                 }
             }
         }
     }
 
     private void login() throws XMPPException {
         assert (m_connection == null);
         final int magic = new Random().nextInt( 1000 );
         final ConnectionConfiguration connconf = new ConnectionConfiguration( m_account.server(), m_account
                 .port() );
         connconf.setSecurityMode( SecurityMode.required );
         connconf.setSendPresence( true );
 
         try {
             m_connection = new XMPPConnection( connconf );
             m_connection.connect();
             m_connection.login( m_account.user(), m_account.password(), "RestBot" + magic );
         } catch ( XMPPException e ) {
            m_connection.disconnect();
             m_connection = null;
             throw e;
         }
     }
 
     private void joinRooms() throws XMPPException {
         for ( String i : m_roomsToJoin ) {
             MultiUserChat c = new MultiUserChat( m_connection, i );
             c.join( m_nick );
             c.changeAvailabilityStatus( "Yo.", Presence.Mode.available );
             m_rooms.put( i, c );
         }
     }
 
     private void logout() {
         m_connection.disconnect();
     }
 
     private BlockingQueue<Message> m_in;
     private Account m_account;
     private Vector<String> m_roomsToJoin;
     private String m_nick;
     private XMPPConnection m_connection;
     private Map<String, MultiUserChat> m_rooms;
 
 }
