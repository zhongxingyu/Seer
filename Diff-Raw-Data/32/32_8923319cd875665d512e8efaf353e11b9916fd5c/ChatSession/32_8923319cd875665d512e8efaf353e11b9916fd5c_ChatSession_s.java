 package net.crimsoncactus.radioshark.client;
 
 import static com.calclab.emite.core.client.xmpp.stanzas.XmppURI.uri;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import com.calclab.emite.core.client.xmpp.session.Session;
 import com.calclab.emite.core.client.xmpp.stanzas.Message;
 import com.calclab.emite.im.client.roster.Roster;
 import com.calclab.emite.im.client.roster.RosterItem;
 import com.calclab.emite.xep.muc.client.Occupant;
 import com.calclab.emite.xep.muc.client.Room;
 import com.calclab.emite.xep.muc.client.RoomManager;
 import com.calclab.suco.client.Suco;
 import com.calclab.suco.client.events.Listener;
 import com.calclab.suco.client.events.Listener2;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 
 public class ChatSession {
     RoomManager roomManager;
     private Room room;
     private static final String LINEUPDATE_ATTRIBUTE = "lineupdate";
 
     private List<ChatSessionListener> listeners = new ArrayList<ChatSessionListener>();
 
     String username;
     String fullName;
 
     public ChatSession() {
         
     }
     
     public void login(String chatRoomName, String user, String fullName) {
         // Suco is a facade that give access to every emite component we need
         this.username = user;
         this.fullName = fullName;
 
        // configureConnection(); // we use Browser emite module

         // ******** 1. Session *********
         // Session is the emite component that allows us to login/logout among
         // other things
         final Session session = Suco.get(Session.class);
 
         // Session.onStateChanged allows us to know the state of the session
         session.onStateChanged(new Listener<Session.State>() {
             public void onEvent(final Session.State state) {
                 outputDebug("Session state: " + state);
             }
         });
 
         // Session.login and Session.logout are our xmpp entrance and exit
         session.login(uri(user), "foobar");
 
         // ******** 2. ChatManager *********
         // ... but probably you prefer to use the a powerful abstraction: Chat
 //        chatManager = Suco.get(ChatManager.class);
         roomManager = Suco.get(RoomManager.class);
         
         room = (Room) roomManager.open(uri(chatRoomName, "conference.crimsoncactus.net", user));
 
         room.onSubjectChanged(new Listener2<Occupant, String>() {
 
             public void onEvent(Occupant changer, String newSubject) {
                 parseSubject(newSubject);
             }
         });
         
         
         // with chats you don't have to specify the recipient
//        chat.send(new Message("Hello test2"));
         // and you only receive messages from the entity you specified
         room.onMessageReceived(new Listener<Message>() {
             public void onEvent(final Message message) {
                 fireMessageReceived(message);
             }
         });
 
         // ******** 3. Roster *********
         // As always, Suco is our friend...
         final Roster roster = Suco.get(Roster.class);
         // ... we're in asynchronous world... use listeners
         // onRosterRetrieved is fired when... surprise! the roster is retrieved
         roster.onRosterRetrieved(new Listener<Collection<RosterItem>>() {
             public void onEvent(final Collection<RosterItem> items) {
                 for (final RosterItem item : items) {
                     fireRosterUpdate(item);
                 }
             }
         });
         // we can track changes in roster items (i.e. roster presence changes)
         // using Roster.onItemUpdated
         roster.onItemChanged(new Listener<RosterItem>() {
             public void onEvent(final RosterItem item) {
                 fireRosterUpdate(item);
             }
         });
 
     }
     
     protected void parseSubject(String newSubject) {
         System.out.println("Subject is now " + newSubject);
         fireLineStateUpdate(newSubject);
 
         
     }
 
     JSONObject createLineItemJSON() {
         JSONObject obj = new JSONObject();
         obj.put("line", new JSONNumber(1));
         obj.put("version", new JSONNumber(0));
         return obj;
     }
     
     protected void fireLineStateUpdate(String newSubject) {
        // TODO parse message.
         for (ChatSessionListener l: listeners) {
             l.lineStatusUpdateReceived(newSubject);
         }
     }
 
     protected void fireRosterUpdate(RosterItem item) {
         for (ChatSessionListener l: listeners) {
             l.rosterItemReceived(item);
         }
     }
 
     protected void fireMessageReceived(Message item) {
         for (ChatSessionListener l: listeners) {
             l.messageReceived(item);
         }
     }
 
     
     protected void outputDebug(String string) {
         for (ChatSessionListener l: listeners) {
             l.outputDebug(string);
         }
 
     }
 
     public void addChatSessionListener(ChatSessionListener listener) {
         listeners.add(listener);
     }
     
     public void removeChatSessionListener(ChatSessionListener listener) {
         listeners.remove(listener);
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getFullName() {
         return fullName;
     }
 
     public void setFullName(String fullName) {
         this.fullName = fullName;
     }
     
     protected void sendMessage(String message) {
         Message msg = new Message(message);
         msg.setAttribute(LINEUPDATE_ATTRIBUTE, "false");
         room.send(msg);
         // There's no need to output the message, as it will be parroted back to us by the chat window in due course.
 //        print(user, message);
     }
 
     public void setSubject(String string) {
         room.setSubject(string);
         
     }
 
 }
