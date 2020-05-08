 package handler;
 
 import commands.*;
 import server.*;
 import java.util.Observable;
 import java.awt.event.KeyEvent;
 import java.rmi.*;
 import user.*;
 import network.*;
 import java.net.*;
 import debug.Debug;
 
 public class Client extends Observable {
 
     private static final boolean DEBUG = Debug.VERBOSE;
 
     private ClientCommandServerThread _commServerThread;
     private ServerCommandListenerInterface _serverCommandListener;
     private DocumentController _controller;
     private CTEUser _localUser;
     private DocumentKey _docKey;
     private boolean _isCollaborating;
     private String _pid;
 
     /**
      * Create a new Client with a blank Document.
      * Ensures:
      *      _controller.getDocument().toString() = "";
      */
     public Client ( ) {
         _isCollaborating = false;
         try { _localUser = new CTEUser("DefaultUser", InetAddress.getLocalHost(), ColorList.getColor(0)); }
         catch (Exception e) { e.printStackTrace(); }
         _docKey = new DocumentKey("DefaultDoc", "password");
         _controller = new DocumentController();
         try { _controller.executeCommand(new AddUserCommand(_localUser)); }
         catch (Exception e) { e.printStackTrace(); }
        _pid = "" + System.nanoTime();
         _commServerThread = new ClientCommandServerThread(this, _pid);
     }
 
 
     /***********
      * Queries *
      **********/
 
     /*
      * Return the DocumentController
      */
     public DocumentController getController() {
         return _controller;
     }
 
     /**
      * Return whether this Client is in Collaboration or not.
      */
     public boolean isCollaborating ( ) { return _isCollaborating; }
 
     /**
      * Return a String representation of this Client.
      */
     public String toString ( ) {
         return "Client{ " + "_controller: " + _controller.toString() + "_isCollaborating: " + _isCollaborating + " }";
     }
 
     /**
      * Return the CTEUser of this Client.
      */
     public CTEUser getUser ( ) { return _localUser; }
 
 
     /************
      * Commands *
      ***********/
 
 
     public void changeClientName ( String name ) throws OutOfBoundsException, UserNotFoundException, RemoteException, InvalidUserIDException {
         passCommand(new UpdateUserNameCommand(_localUser, name));
         _localUser.setName(name);
     }
 
     /**
      * Creates a new Document with the given String.
      *
      * Requires:
      *      text != null
      * Ensures:
      *      this.toString() == text
      */
     public void setDocument ( String text ) {
         Document newDoc = new Document(text);
         _controller.setDocument(newDoc);
 
         //update text panes
         notifyDocumentGotUpdated();
     }
 
     /**
      * Given a command if this Client IS NOT collaborating, execute it on this._controller,
      *                  if this Client IS collaborating, wrap it with a
      *                  NetworkCommand and Forward it to the Server.
      *
      */
     public void passCommand ( Command command ) throws RemoteException, InvalidUserIDException, UserNotFoundException, OutOfBoundsException {
         if (DEBUG) { System.out.println("passCommand called"); }
         if (_isCollaborating) { _wrapAndForward(command); }
         else {
             _controller.executeCommand(command);
 
             //since command execution was successful, update everyone's text pane
             notifyDocumentGotUpdated();
         }
     }
 
     /**
      * Given the Network Command, execute it.
      */
     public void executeNetworkCommand ( NetworkCommand netCommand ) throws InvalidUserIDException, UserNotFoundException, OutOfBoundsException {
         // TODO Actually execute the command and notify the GUI
         if (DEBUG) { System.out.println("executeNetworkCommand called with Command: " + netCommand); }
 
         Command command = netCommand.getCommand();
         _executeCommand(command);
     }
 
     /**
      * Initiates a Connection to the Server.
      */
     public void initiateCollaboration ( ) throws InvalidUserIDException, UserNotFoundException, OutOfBoundsException, NotBoundException, MalformedURLException, RemoteException {
         _isCollaborating = true;
         if (DEBUG) { System.out.println("initiateCollaboration called"); }
         _commServerThread.start();
         _serverCommandListener = (ServerCommandListenerInterface) Naming.lookup("rmi://localhost/CommandListener");
         _serverCommandListener.initializeClient(_localUser, _pid);
         boolean initSuccessful = _serverCommandListener.initializeDocument(_docKey, _controller);
 
         if (initSuccessful) {
             if (DEBUG) { System.out.println("Successfully initialized a new document on the server."); }
         }
         else {
             if (DEBUG) { System.out.println("Document already initialized; Attempted to join document in-progress."); }
             if (DEBUG) { System.out.println(_controller); }
             _controller = _serverCommandListener.joinDocument(_localUser, _docKey);
             if (DEBUG) { System.out.println(_controller); }
             //_serverCommandListener.registerClient(_localUser, _docKey, _pid);
             AddUserCommand addUser = new AddUserCommand(_localUser);
             _executeCommand(addUser);
             passCommand(addUser);
         }
     }
 
     /**
      * Sets the DocumentKey.
      */
     public void setDocumentKey ( DocumentKey key ) { _docKey = key; }
 
 
     /*******************
      * Private Methods *
      *******************/
 
 
     /**
      * Notifies the observers of the Controller that it has changed.
      * Partially implemented for a GUI observer.
      * TODO: Implement for a Server Observer
      */
     private void notifyDocumentGotUpdated ( ) {
         if (DEBUG) { System.out.println("notifyDocumentGotUpdated called"); }
         if (DEBUG) { System.out.println("" + _controller + "\n" + _localUser); }
         setChanged();
         notifyObservers(_controller);
     }
 
     /**
      * Creates a NetworkCommand and sends it to the Server.
      */
     private void _wrapAndForward ( Command command ) throws RemoteException {
         if (DEBUG) { System.out.println("_wrapAndForward called"); }
         NetworkCommand netCommand = new NetworkCommand(command, _docKey, _localUser);
         _serverCommandListener.execute(netCommand);
     }
 
     private void _executeCommand ( Command command ) throws InvalidUserIDException, UserNotFoundException, OutOfBoundsException {
         if (DEBUG) { System.out.println("_executeCommand called"); }
 
         try  { _controller.executeCommand(command); }
         catch (UserNotFoundException unfe) { System.out.println("OMGHI\n" + _controller); unfe.printStackTrace(); }
 
         notifyDocumentGotUpdated();
     }
 }
