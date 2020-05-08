 package swirc;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 /**
  * Controller class for Swirc MVC-model. Implements ActionListener and Observer
  * interfaces.
  * @author Janne Kallunki, Ville Hämäläinen, Jaakko Ritvanen
  */
 public class SwircController implements ActionListener, Observer {
     private SwircModel model;
     private SwircView view;
     
     /**
      * Constructer.
      * @param model Model object of Swircs MVC-model
      * @param view View object of Swircs MVC-model
      */
     public SwircController(SwircModel model, SwircView view) {
        this.model = model;
        this.view = view;
     }
 
     /**
      * Invoked when an action occurs. 
      * @param e ActionEvent
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         String code = e.getActionCommand();
         if(code.equals("connectServer")) {
             HashMap<String,String> con = view.connectPrompt();
            if(con != null) {
                if(!con.get("serverAddress").equals("") && !con.get("nick").equals("")) {
                    this.view.addServerView(con.get("serverAddress"));
                    this.model.connect(con.get("serverAddress"), con.get("nick"), con.get("port"), con.get("pasword"));
                }
                else {
                    view.showWarning("Your server address or nick was empty!");
                }
             }
         }
         else if(code.equals("disconnect")) {
             this.model.disconnect();
         }
         else if(code.equals("reconnect")) {
             this.model.reconnect();
         }
         else if(code.equals("join")) {
             HashMap<String, String> join = view.joinPrompt();
             if(join != null) {
                 model.joinChannel(join.get("channel"), Integer.parseInt(join.get("server")));
             }
         }
         else if(code.equals("leave")) {
             String channel = view.getActiveChannel();
             if(channel.startsWith("#")) {
                 model.leaveChannel(channel);
             }
         }
         else if(code.equals("quit")) {
             System.exit(0);
         }
         else if(code.equals("userData")) {
             HashMap<String, String> saveUser = view.userPrompt();
             if(saveUser != null) {
                 this.model.setUserData(saveUser);
                 this.model.saveUserData();
 
             }
         }
         else if(code.equals("send")) {
             String msg = view.getInput();
             String channel = view.getActiveChannel();
             if(!msg.isEmpty() && channel.startsWith("#")) { 
                 model.sendMsg(msg, channel);
             }
             view.resetInput();
         }
     }
 
     /**
      * This method is called whenever the observed object is changed.
      * @param o The observable object.
      * @param arg  An argument passed to the notifyObservers method.
      */
     @Override
     public void update(Observable o, Object arg) {
         String code = arg.toString();
         //System.out.println(code);
         if(code.startsWith("ConnectedServer")) {
             this.view.setJoinEnabled();
             this.view.setReconnectEnabled();
             this.view.setDisconnectEnabled();
         }
         else if(code.equals("disconnect")) {
             this.view.closeAllTabs();
             this.view.setJoinUnenabled();
             this.view.setLeaveUnenabled();
             this.view.setDisconnectUnenabled();
         }
         else if(code.equals("reconnect")) {
             String[] servers = this.model.getConnectedServers();
             if(servers != null) {
                 for(int i = 0; i < servers.length; i++) {
                     this.view.addServerView(servers[i]);
                 }
             }
         }
         else if(code.equals("join")) {
             this.view.setLeaveEnabled();
         }
         else if(code.equals("leave")) {
             this.view.closeTab();
             if(this.view.getTabCount() < 2) {
                 this.view.setLeaveUnenabled();
             }
         }
         else if(code.equals("cant connect")) {
             this.view.showWarning("Can't connect server!");
         }
         else if(code.equals("cant join")) {
             this.view.showWarning("Can't join channel!");
         }
         else if(code.equals("userDataError")) {
             this.view.showWarning("There were empty fields in userdata!");
         }
         else if(code.equals("userDataDublicate")) {
             this.view.showWarning("Some of your userdata was wrong!");
         }
     }
 }
