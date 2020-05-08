 package net.crimsoncactus.radioshark.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.calclab.emite.core.client.bosh.BoshSettings;
 import com.calclab.emite.core.client.bosh.Connection;
 import com.calclab.emite.core.client.xmpp.stanzas.Message;
 import com.calclab.emite.core.client.xmpp.stanzas.XmppURI;
 import com.calclab.emite.im.client.roster.RosterItem;
 import com.calclab.suco.client.Suco;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class ChatWindow extends VerticalPanel {
     private ScrollPanel outputScroller;
     private VerticalPanel outputs;
     
     private TextBox input;
 //    private ChatManager chatManager;
     
     ChatSession session;
     HorizontalPanel presence;
     Map<XmppURI, RosterItem> roster = new HashMap<XmppURI, RosterItem>();
     
     public ChatWindow(ChatSession session) {
         this.session = session;
         
         presence = new HorizontalPanel();
         presence.add(new Label("Member Presence:"));
         
         outputs = new VerticalPanel();
         outputScroller = new ScrollPanel(outputs);
         outputScroller.setHeight("600px");
         
         
         input = new TextBox();
         input.setWidth("100%");
         input.addKeyPressHandler(new KeyPressHandler() {
             public void onKeyPress(KeyPressEvent event) {
                 if (event.getCharCode() == 13) {
                     ChatWindow.this.session.sendMessage(input.getText());
                     input.setText("");
 
                 }
             }
         });
         
         setWidth("600px");
         add(presence);
         add(outputScroller);
         add(input);
 
         // do the chat session thing
         
         
         session.addChatSessionListener(new ChatSessionListener() {
             
             public void rosterItemReceived(RosterItem rosterItem) {
                 roster.put(rosterItem.getJID(), rosterItem);
                 updateRosterUI();
             }
             
             public void outputDebug(String message) {
                 print(null, message);
             }
             
             public void messageReceived(Message message) {
                 print(lookupUser(message.getFrom().getResource()), message.getBody());
             }
             
             public void lineStatusUpdateReceived(String status) {
             }
         });
         
     }
     
     protected String lookupUser(String resource) {
         if (resource.equals(session.getUsername())) {
             return session.getFullName();
         }
         XmppURI uri = XmppURI.uri(resource);
         RosterItem i = roster.get(uri);
         if (i == null)
             return resource;
         else
             return i.getName();
     }
 
     protected void updateRosterUI() {
         while (presence.getWidgetCount() > 1) {
             presence.remove(1);
         }
         for (RosterItem item: roster.values()) {
             presence.add(new RosterLabel(item));
         }
         
     }
 
     
     
     protected void configureConnection() {
         // ******** 0. Configure connection settings *********
         final Connection connection = Suco.get(Connection.class);
         connection.setSettings(new BoshSettings("proxy", "localhost"));
         // ...but there's a module, BrowserModule, that allows to configure
         // the connections settings in the html directly
     }
     
     private MessageBubble getLastBubble() {
         int count = outputs.getWidgetCount();
         return (MessageBubble) (count == 0 ? null : outputs.getWidget(count-1));
     }
 
 
     boolean compare(String s1, String s2) {
         return s1 == null ? s2 == null : s1.equals(s2);
     }
 
     /**
      * a helper method to output messages
      * 
      * @param message
      */
     private void print(String author,  String message) {
         MessageBubble last = getLastBubble();
         
         if (last != null && compare(last.getAuthor(), author)) {
             last.appendMessage(message);
         } else {
             MessageBubble newBubble = new MessageBubble(author, message, author != null && author.equals(session.getFullName()));
             outputs.add(newBubble);
         }
         outputScroller.scrollToBottom();
     }
 }
