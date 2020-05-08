 /*
  * JabberConfigWindow.java
  * 
  * See LICENCE file for usage and redistribution terms
  * Copyright (c) 2005, 2008 Operational Dynamics
  */
 package xseq.ui;
 
 import generic.util.Debug;
 
 import java.io.FileNotFoundException;
 
 import org.gnome.gdk.Event;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.glade.Glade;
 import org.gnome.glade.XML;
 import org.gnome.gtk.Button;
 import org.gnome.gtk.Editable;
 import org.gnome.gtk.Entry;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.Widget;
 import org.gnome.gtk.Window;
 
 import xseq.client.ProcedureClient;
 import xseq.network.NetworkConnection;
 
 /**
  * A window to enter Jabber configuration information.
  * 
  * @author Andrew Cowie
  */
 public class JabberConfigWindow
 {
     private String _username = null;
 
     private String _server = null;
 
     private String _password = null;
 
     /*
      * Cached widgets and UI elements
      */
     private XML _glade = null;
 
     Window _top = null;
 
     private Entry _server_entry = null;
 
     private Entry _username_entry = null;
 
     private Entry _password_entry = null;
 
     private Label _jid_label = null;
 
     private static final String _jid_label_emptyMessage = "<span color=\"red\"><big><b>Enter details above</b></big></span>";
 
     private Button _apply_button = null;
 
     private JabberConnectionWindow _jcw = null;
 
     /**
      * Fire up a new JabberConfig Window with initially default fields (ie,
      * original uninitialized state)
      */
     public JabberConfigWindow() {
         // this(null, "jabber.org", null);
         this(null, "localhost", null);
     }
 
     /**
      * If the connection parameters are known, pass them in and they will be
      * used as initial field values. Use null for uninitialized.
      * 
      * @param username
      * @param server
      * @param password
      */
     public JabberConfigWindow(String username, String server, String password) {
         if (username != null) {
             this._username = username;
         }
         if (server != null) {
             this._server = server;
         }
         if (password != null) {
             this._password = password;
         }
 
         try {
             _glade = Glade.parse("share/jabberconfig.glade", null);
         } catch (FileNotFoundException e) {
             // If it can't find that glade file, we have an app
             // configuration problem or worse some UI bug, and need to abort.
             e.printStackTrace();
             ProcedureClient.abort("Can't find glade file for JabberConfigWindow.");
         } catch (Exception e) {
             e.printStackTrace();
             ProcedureClient.abort("An internal error occured trying to read and process the glade file for the JabberConfigWindow.");
         }
         _top = (Window) _glade.getWidget("jabberconfig");
         _top.hide();
 
         _top.connect(new Window.DELETE_EVENT() {
             public boolean onDeleteEvent(Widget source, Event event) {
                 Debug.print("listeners", "calling end_program() to initiate app termination");
                 close_window();
                 return false;
             }
         });
 
         _apply_button = (Button) _glade.getWidget("apply_button");
 
         /*
          * Some boilerplate code to grab an image, use it as icon, and then
          * set it as the nifty looking logo image in the window.
          */
         Pixbuf jabber_pixbuf = null;
         try {
             jabber_pixbuf = new Pixbuf("share/pixmaps/jabber-48x48.png");
         } catch (Exception e) {
             e.printStackTrace();
             // TODO be nicer!
             System.exit(1);
         }
         _top.setIcon(jabber_pixbuf);
 
         Image jabber_image = (Image) _glade.getWidget("jabber_image");
         jabber_image.setImage(jabber_pixbuf);
 
         /*
          * As characters are entered in the server and username fields, update
          * the label showing the JID that will be used.
          */
         _server_entry = (Entry) _glade.getWidget("server_entry");
         _username_entry = (Entry) _glade.getWidget("username_entry");
         _password_entry = (Entry) _glade.getWidget("password_entry");
         _jid_label = (Label) _glade.getWidget("jid_label");
         /*
          * Save the markup text as specified in the glade file so we only
          * actually write it in one place. Note not using getText().
          */
         _jid_label.setLabel(_jid_label_emptyMessage);
 
         /*
          * A listener that can be used for both entry fields
          */
        Editable.CHANGED jidUpdater = new Editable.CHANGED() {
            public void entryEvent(EntryEvent event) {
                if (event.getType() == EntryEvent.Type.CHANGED) {
                } else if (event.getType() == EntryEvent.Type.ACTIVATE) {
                    _apply_button.grabDefault();
                    _apply_button.emitClicked();
                }
             }
 
             public void onChanged(Editable source) {
                 _username = _username_entry.getText();
                 _server = _server_entry.getText();
                 _password = _password_entry.getText();
 
                 String message = null;
 
                 if (_username.equals("") || _server.equals("")) {
                     message = _jid_label_emptyMessage;
                     _apply_button.setSensitive(false);
                 } else {
                     message = "<big><b>" + _username + "@" + _server + "/" + NetworkConnection.RESOURCE
                             + "</b></big>";
                     if (_password.equals("")) {
                         message = "<span color=\"#ecdc2b\">" + message + "</span>";
                         _apply_button.setSensitive(false);
                     } else {
                         message = "<span color=\"blue\">" + message + "</span>";
                         _apply_button.setSensitive(true);
                         _apply_button.grabDefault();
                     }
                 }
                 _jid_label.setLabel(message);
             }
         };
 
         /*
          * Hook up listeners
          */
         _server_entry.connect(jidUpdater);
         _username_entry.connect(jidUpdater);
         _password_entry.connect(jidUpdater);
 
         /*
          * Set passed in values, if present. TODO does this actually work with
          * nulls?
          */
         _server_entry.setText(_server);
         _username_entry.setText(_username);
         _password_entry.setText(_password);
 
         /*
          * Hook up action buttons
          */
         _apply_button.connect(new Button.CLICKED() {
             public void onClicked(Button source) {
                 /*
                  * Launch connection & validation test.
                  */
                 _jcw = new JabberConnectionWindow(_username, _server, _password);
                 // _top.hide();
                 Debug.print("listeners", "after JabberConnectionWindow instantiation");
                 // TODO ... and?
             }
         });
 
         Button cancel_button = (Button) _glade.getWidget("cancel_button");
 
         cancel_button.connect(new Button.CLICKED() {
             public void onClicked(Button source) {
                 // TODO revert values
                 _top.hide();
                 System.exit(1); // TODO NO!
             }
         });
 
         _top.present();
     }
 
     public void close_window() {
         _top.hide();
         System.exit(1);
         // TODO and...?
     }
 }
