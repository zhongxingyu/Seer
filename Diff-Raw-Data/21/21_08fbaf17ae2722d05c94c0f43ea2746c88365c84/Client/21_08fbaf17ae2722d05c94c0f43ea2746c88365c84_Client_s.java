 package handler;
 
 import network.*;
 import user.*;
 import gui.*;
 import java.util.Observable;
 import static java.lang.System.out;
 
 public class Client extends Observable {
     
     private DocumentController _controller;
    private ConnectionManager _connectionManager;
 
     
     public Client() {
         _controller = new DocumentController();
         //_connectionManager = new ConnectionManager();
     }  
     
     /**
      * Creates a new Document with the given String.
      *
      * @Requires
      *      text != null
      * @Ensures
      *      this.toString() == text
      */
     public synchronized void setDocument ( String text ) {
         Document newDoc = new Document(text);
         _controller.setDocument(newDoc);
         
         //update text panes
         notifyDocumentGotUpdated();
     }
     
     public synchronized void insertTextInDocument ( String text ) {
         //creates a corresponding command to send to the document controller
         InsertTextCommand command = new InsertTextCommand("username", text);
         
         try{
             _controller.executeCommand(command);
         }catch(UserNotFoundException iue){
             iue.printStackTrace();
         }catch(InvalidUserIDException iuide){
             iuide.printStackTrace();
         }catch(OutOfBoundsException oobe){
             oobe.printStackTrace();
         }
         
         //since command execution was successful, update everyone's text pane
         notifyDocumentGotUpdated();
     }
     
     public synchronized void initateCollaboration ( ) {
         //_connectionManager = new ConnectionManager();
     }
   
     public synchronized Document getDocument () {
         return _controller.getDocument();
     }
 
     public String toString() {
         return "Client{" + "_controller=" + _controller.toString() + '}';
     }
     
     private void notifyDocumentGotUpdated(){
         setChanged();
         notifyObservers(_controller.getDocument().toString());
     }
 }
