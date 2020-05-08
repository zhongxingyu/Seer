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
         }
         ServerDocument doc = docList.get(tokens[1]);
         Edit edit;
         System.out.println("tokens[3]: "+tokens[3]);
         //if (tokens[3].equals("space")) {
         //    edit = new Edit(" ", tokens[0]);
         //} else {
             edit = new Edit(tokens[3], tokens[0]);
         //}
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
         return doc.removeContent(tokens[4], tokens[5], tokens[0]);
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
     
     public String putOnQueue(String input) {
         if (!queue.add(input)) {
             return "fail with message: " + input;
         } else {
             // TODO: figure out how this works
             return takeFromQueue();
         }
     }
     
     public synchronized String takeFromQueue() {
         // Create a caret and a key listener, and listen to client.
         // sent client input as an EDIT message, to Edit queue
         // edit queue will handle processing 
         
 //        // TODO: define regex of protocol from user to server, decide if necessary
 //        String regex = "()|" + //insert
 //                "()|" + //remove
 //                "()|" + //spaceEntered
 //                "()|" + //cursorMoved
 //                "()|" + //save
 //                "()|" + //disconnect
 //                "()"; //newDoc
 //        if(!input.matches(regex)) {
 //            //invalid input
 //            return null;
 //        }
         
         String next = "";
         if (!queue.isEmpty()) {
             next = queue.remove();
         } else {
             return "emptyQueue";
         }
 
         String[] tokens = next.split(" ");
 
         if (tokens.length > 1 && tokens[1].equals("NewDoc")) { 
             System.out.println("made it to new doc");
             // If creating a new document
             String title = tokens[2];
             System.out.println("current keys: "+docList.keySet());
             if (docList.containsKey(title)) {
                 return "new fail";
             } else {
                 docList.put(title, new ServerDocument(title));
                 return "new success";
             }
         } else if (tokens.length > 0 && tokens[0].equals("open")) {
             System.out.println("made it to open");
             ServerDocument doc = docList.get(tokens[2]);
             if (doc == null) {
                 System.out.println("doc is null");
                 return "open fail";
             } else {
                 String contents = doc.getDocContent();
                 System.out.println("contents: "+contents);
                 return "open success " + tokens[1] + " " + contents;
             }
         } else if (tokens.length > 0 && tokens[0].equals("getDocNames")) {
             System.out.println("reached getdocnames");
             // If asking for list of document names
             String names = "docNames";
             for (String key: docList.keySet()) {
                 names += " ";
                 names += key;
             }
             return names;
         } else if (tokens.length > 0 && tokens[0].equals("update")) {
             System.out.println("update");
             ServerDocument doc = docList.get(tokens[1]);
             if (doc == null) {
                 return "update fail";
             } else {
                 String contents = doc.getDocContent();
                 return "update " + tokens[1] + " " + contents;
             }
         } else if (tokens.length > 2 && tokens[2].equals("Save")) {
            return endEdit(next);
         } else if (tokens.length > 2 && tokens[2].equals("Insert")) {
             return insert(next);
         } else if (tokens.length > 2 && tokens[2].equals("Remove")) {
             return remove(next);
         } else if (tokens.length > 2 && tokens[2].equals("SpaceEntered")) {
             insert("addOneSpace "+ next);
             return endEdit(next);
         } else if (tokens.length > 2 && tokens[2].equals("CursorMoved")) {
             return endEdit(next);
         } else {
             return "Invalid input";
         }
     }
 }
