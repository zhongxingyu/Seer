 /*
  * Copyright (C) 2013 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.javnce.ui;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import javafx.application.Platform;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.control.ListView;
 import org.javnce.upnp.UpnPServer;
 import org.javnce.vnc.server.RemoteClient;
 import org.javnce.vnc.server.RemoteClientObserver;
 import org.javnce.vnc.server.VncServerController;
 
 /**
  * The Class ServerView implements the server view where connected clients are
  * shown.
  */
 public class ServerView extends View implements Initializable, RemoteClientObserver {
 
     /**
      * The VNC server.
      */
     final private VncServerController vnc;
     /**
      * The UPnP server.
      */
     private UpnPServer upnp;
     /**
      * The items.
      */
     final private ObservableList<String> items;
     /**
      * The list view.
      */
     @FXML
     ListView<String> listView;
 
     /**
      * Instantiates a new server view.
      *
      * @param controller the controller
      */
     public ServerView(Controller controller) {
         super(controller);
         items = FXCollections.observableArrayList();
         vnc = new VncServerController(controller.getConfig().fullAccessMode().get());
     }
 
     /* (non-Javadoc)
      * @see org.javnce.ui.View#createNode()
      */
     @Override
     public Node createNode() throws IOException {
         ResourceBundle bundle = ResourceBundle.getBundle("org.javnce.ui.ServerView", Locale.getDefault());
         URL fxmlUrl = View.class.getResource("ServerView.fxml");
         FXMLLoader loader = new FXMLLoader();
         loader.setLocation(fxmlUrl);
         loader.setResources(bundle);
         loader.setController(this);
         return (Node) loader.load();
     }
 
     /* (non-Javadoc)
      * @see org.javnce.ui.View#onExit()
      */
     @Override
     public void onExit() {
         vnc.removeObserver(this);
         vnc.shutdown();
         if (null != upnp) {
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     upnp.shutdown();
                 }
             }).start();
         }
     }
 
     /* (non-Javadoc)
      * @see org.javnce.ui.View#createFactory()
      */
     @Override
     public ViewFactory createFactory() {
         return new ViewFactory() {
             @Override
             public View viewFactory(Controller controller) {
                 return new ServerView(controller);
             }
         };
     }
 
     /* (non-Javadoc)
      * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         listView.setItems(items);
         vnc.addObserver(this);
         vnc.launch();
     }
 
     /* (non-Javadoc)
      * @see org.javnce.vnc.server.RemoteClientObserver#vncClientChanged(org.javnce.vnc.server.RemoteClient)
      */
     @Override
     public void vncClientChanged(RemoteClient client) {
         Runnable r = null;
 
         if (RemoteClient.State.PendingConnection == client.state()) {
             final RemoteClient temp = client;
             r = new Runnable() {
                 @Override
                 public void run() {
                     acceptConnection(temp);
                 }
             };
         } else if (RemoteClient.State.Connected == client.state()) {
             final String address = client.address();
             r = new Runnable() {
                 @Override
                 public void run() {
                     items.add(address);
                 }
             };
         } else if (RemoteClient.State.Disconnected == client.state()) {
             final String address = client.address();
             r = new Runnable() {
                 @Override
                 public void run() {
                     items.remove(address);
                 }
             };
         }
 
         if (null != r) {
             Platform.runLater(r);
         }
 
     }
 
     /* (non-Javadoc)
      * @see org.javnce.vnc.server.RemoteClientObserver#portChanged(int)
      */
     @Override
     public void portChanged(int port) {
         if (null == upnp && 0 < port) {
             upnp = new UpnPServer(getController().getConfig().serverName().get(), port);
             new Thread(upnp).start();
         }
     }
 
     /**
      * Accept connection.
      *
      * @param client the client
      */
     private void acceptConnection(RemoteClient client) {
         boolean accept;
         if (getController().getConfig().autoConnect().get()) {
             accept = true;
         } else {
             String text = new MessageBox("Accept client ?",
                     "New client from " + client.address(), "Accept", "Decline").exec();
             accept = text.equals("Accept");
         }
         if (accept) {
             client.connect();
         } else {
             client.disconnect();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //Client gone.
                    //Run gc later on in event loop 
                    System.gc();
                }
            });
         }
     }
 }
