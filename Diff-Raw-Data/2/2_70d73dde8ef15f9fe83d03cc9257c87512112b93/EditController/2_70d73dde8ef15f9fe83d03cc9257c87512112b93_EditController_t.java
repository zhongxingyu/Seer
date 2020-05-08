 package backend;
 
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * Deals with the actual Edits in the docs by adding things to the
  * EditQueue and dealing with the response from the queue. Also does
  * things like reassign Edit owners at the end of an Edit.
  *
  *
  * Testing strategy -- This will be tested using arbitrary Strings and
  * ServerDocs to make sure that every method works correctly. The testing
  * will include mock objects since this class interacts with so many
  * other classes.
  */
 public class EditController {
     private ArrayBlockingQueue<String> queue;
     private Map<String, ServerDocument> docList;
     
     /**
      * Empty EditController constructor
      */
     public EditController(ArrayBlockingQueue<String> queue, Map<String, ServerDocument> docList) {
         this.queue = queue;
         this.docList = docList;
     }
     
     //returns the queue
     public ArrayBlockingQueue<String> getQueue() {
         return queue;
     }
 
     /**
      * Deals with inserts. Interacts with the EditQueue to make sure that 
      *   everything is threadsafe and in order
      * @param input The message from the GUI, passed through the server
      * @param doc The ServerDocument that the GUI is currently editing
      * @return The message to return to the server. Returns "Success" if 
      *   successful and "Error" if not successful
      */
     public String insert(String input) {
         String[] tokens = input.split(" ");
         
         if (tokens[0].equals("addOneSpace")) {
             ServerDocument doc1 = docList.get(tokens[2]);
             System.out.println("new input: "+input);
             return doc1.insertContent(new Edit(" ", tokens[1]), tokens[5], tokens[1]);
         } else if (tokens[0].equals("addOneEnter")) {
             ServerDocument doc1 = docList.get(tokens[2]);
             System.out.println("new input: "+input);
             String str = doc1.insertContent(new Edit("\n", tokens[1]), tokens[5], tokens[1]);
             System.out.println(doc1.getDocContent());
             return str;
         }
         ServerDocument doc = docList.get(tokens[1]);
         Edit edit;
         edit = new Edit(tokens[3], tokens[0]);
         return doc.insertContent(edit, tokens[4], tokens[0]);
     }
     
     /**
      * Deals with removals. Interacts with the EditQueue to make sure that
      *   everything is threadsafe and in order
      * @param input The message from the GUI, passed through the server
      * @param doc The ServerDocument that the GUI is currently editing
      * @return The message to return to the server
      */
     public String remove(String input) {
         String[] tokens = input.split(" ");
         ServerDocument doc = docList.get(tokens[1]);
         return doc.removeContent(tokens[3], tokens[4], tokens[0]);
     }
     
     /**
      * Deals with the end of an edit, iterates through and changes
      *   the owner and color of the finished edit. Interacts with the
      *   EditQUeue to make sure that everything is threadsafe and in order
      * @param input The message from the GUI, passed through the server
      * @param doc The ServerDocument that the GUI is currently editing
      * @return The message to return to the server
      */
     public String endEdit(String input) {
         String[] tokens = input.split(" ");
         ServerDocument doc = docList.get(tokens[1]);
         return doc.endEdit(tokens[0]);
     }
     
     /**
      * Puts the received message on the queue and looks at the message at the head of the queue.
      * This ensures that there are always messages being put on the queue at the same rate as ones
      * being taken off of the queue.
      * @param input The input from the GUI to the server
      * @return The result of taking the head message from the queue and dealing with it
      */
     public synchronized String putOnQueue(String input) {
         if (!queue.add(input)) {
             return "fail with message: " + input;
         } else {
             //return takeFromQueue();
             return "";
         }
     }
     
     /**
      * Takes the message that is at the head of the queue and deals with it appropriately
      * @return A message to send back to the server; the server will send a corresponding
      *    message to the GUI
      * @throws InterruptedException 
      */
     public synchronized String takeFromQueue() throws InterruptedException {
         // A regex is unnecessary here since the messages are hardcoded into the GUI
         // and will never be wrong, assuming that the GUI has been thoroughly tested.
         
         String next = "";
         if (!queue.isEmpty()) {
             next = queue.take();
         } else {
             return "emptyQueue";
         }
 
         String[] tokens = next.split(" ");
 
         // Enormous if/else statement that handles the messages from the GUI, calls 
         // the relevant methods on the message based on the message, and returns 
         // the message to be sent from the server to the GUI.
         
         if (tokens.length > 2 && tokens[2].equals("new")) { 
             // For new document messages
             // Input: clientName docName new
             // Successful output: clientName docName new success
             // Unsuccessful output: clientName docName new fail
             
             System.out.println("made it to new doc");
             String title = tokens[1];
             if (docList.containsKey(title)) {
                 return tokens[0] + " " + tokens[1] + " new fail";
             } else {
                 docList.put(title, new ServerDocument(title));
                 return tokens[0] + " " + tokens[1] + " new success";
             }
         } else if (tokens.length > 2 && tokens[2].equals("open")) {
             // For open document messages
             // Input: clientName docName open
             // Successful output: clientName docName open success lines content
             // Unsuccessful output: clientName docName open fail
             
             System.out.println("made it to open");
             ServerDocument doc = docList.get(tokens[1]);
             if (doc == null) {
                 return tokens[0] + " " + tokens[1] + " open fail";
             } else {
                 String lineAndContents = doc.getDocContent();
                 return tokens[0] + " " + tokens[1] + " open " + lineAndContents;
             }
         } else if (tokens.length > 2 && tokens[2].equals("getDocNames")) {
             // For get doc names messages
             // Input: clientName docName getDocNames
             // Output: clientName docName getDocNames names
             // There can be no unsuccessful output
             
             System.out.println("reached getdocnames");
             String names = " getDocNames";
             for (String key: docList.keySet()) {
                 names += " ";
                 names += key;
             }
             System.out.println(tokens[0] + " " + tokens[1] + names);
             return tokens[0] + " " + tokens[1] + names;
         } else if (tokens.length > 2 && tokens[2].equals("checkNames")) {
             // For check names messages
             // Input: clientName docName checkNames
             // Output: clientName docName checkNames names
             // There can be no unsuccessful output
             
             System.out.println("reached checknames");
             String names = " checkNames";
             for (String key: docList.keySet()) {
                 names += " ";
                 names += key;
             }
             return tokens[0] + " " + tokens[1] + names;
         } 
         
         // There's no longer an update message here because the GUI never asks for an update,
         // the server just sends one. It makes the update message in the server itself.
         
         else if (tokens.length > 2 && tokens[2].equals("save")) {
             // For save messages
             // Input: clientName docName save
             // Output: clientName docName save
             // There can be no unsuccessful output
             
             endEdit(next);
             return tokens[0] + " " + tokens[1] + " save";
         } else if (tokens.length > 2 && tokens[2].equals("insert")) {
             // For input messages
             // Input: clientName docName insert keyChar index
             // Successful output: clientName docName insert success
             // Unsuccessful output: clientName docName insert fail
             
             String result = insert(next);
             if (result.equals("LockedEdit")) {
                 return tokens[0] + " " + tokens[1] + " insert fail";
             } else {
                 return tokens[0] + " " + tokens[1] + " insert " + tokens[4];
             }
         } else if (tokens.length > 2 && tokens[2].equals("remove")) {
             // For remove messages
             // Input: clientName docName remove keyChar indexBegin indexEnd
             // Successful output: clientName docName remove success
             // Unsuccessful output: clientName docName remove fail
             
             String result = remove(next);
             if (result.equals("SingleLock") || result.equals("SomeLocked")) {
                 return tokens[0] + " "  + tokens[1] + " remove fail";
             } else {
                 return tokens[0] + " "  + tokens[1] + " remove " + tokens[3] + " " + tokens[4];
             }
             
         } else if (tokens.length > 2 && tokens[2].equals("spaceEntered")) {
             // TODO: clean this up later with a better protocol, add in other whitespace chars
             // For whitespace entered messages
             // Input for enter: clientName docName spaceEntered enter index
             // Input for space: clientName docName spaceEntered space index
             // Successful output: clientName docName spaceEntered success
             // Unsuccessful output: clientName docName spaceEntered fail
             
             String result = "";
             if (tokens[3].equals("space")) {
                 result = insert("addOneSpace "+ next);
             } else if (tokens[3].equals("enter")) {
                 result = insert("addOneEnter "+next);
             }
             
             endEdit(next);
             if (result.equals("LockedEdit")) {
                 return tokens[0] + " " + tokens[1] + " spaceEntered fail";
             } else {
                return tokens[0] + " " + tokens[1] + " spaceEntered "+ tokens[4];
             }
         } else if (tokens.length > 2 && tokens[2].equals("cursorMoved")) {
             // For cursor moved messages
             // Input: clientName docName cursorMoved
             // Output: clientName docName cursorMoved success
             // There is no output failure
             
             endEdit(next);
             return tokens[0] + " "  + tokens[1] + " cursorMoved";
         } else {
             // If a message somehow makes it all the way through the if/else.
             // Shouldn't reach here. Is here for debugging.
             return "InvalidInput";
         }
     }
 }
