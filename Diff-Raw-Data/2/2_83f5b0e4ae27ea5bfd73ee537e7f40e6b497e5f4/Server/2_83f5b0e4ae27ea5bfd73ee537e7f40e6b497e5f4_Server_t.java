 package lab3;
  
 import java.rmi.registry.Registry;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 import java.util.LinkedList;
  
 /**
  * An object implementing the Remote interface. This allows it to be
  * called through RMI.
  */
 public class Server implements Hello {
 
   public Server () {}
 
   private LinkedList<Book> stock = new LinkedList<Book>();
   /**
    * This port will be assigned to your group for use on EC2. For local testing, you can use any (nonstandard) port you wish.
    */
   public final static int REGISTRY_PORT = 54001;
 
   public String sayHello() {
     System.out.println("sayHello() was called");
     return "Hello, remote world!";
   }
 
   private void printStock(){
     System.out.println(stock.size() + " total books:");
     for(Book b : stock){
       System.out.println("  " + b.getTitle() + ": " + b.getCopies() + " copies.");
     }
   }
 
   public int sell(String bookname, int copies){
     long timeStarted = System.currentTimeMillis();
     int currentStock = 0;
     Book book = null;
     for(Book b : stock){
       if(b.getTitle().equals(bookname)){
         synchronized(b){
           //book exists in stock, so update number of copies
           currentStock = b.getCopies();
           //don't allow decreasing number of copies. that would be rude!
           if(copies > 0) b.setCopies(currentStock + copies);
           //wakes up any threads waiting on this book
           b.notify();
           System.out.println("Someone just sold " + copies + " of " + bookname + ". There are now " + currentStock + ".");
           long timeToSell = System.currentTimeMillis() - timeStarted;
           System.out.println("It took " + timeToSell + " milliseconds to process the request on the server.");
           //returns stock before more copies were added
           return currentStock;
         }
       }
     }
     book = new Book(bookname, copies);
     stock.add(book);
     System.out.println("Someone just sold " + copies + " of " + bookname + ". There are now " + copies + ".");
     long timeToSell = System.currentTimeMillis() - timeStarted;
     System.out.println("It took " + timeToSell + " milliseconds to process the request on the server.");
     return 0; //no copies previously existed
   }
 
   public int buy(String bookname, int copies){
     long timeStarted = System.currentTimeMillis();
     for(Book b : stock){
       if(b.getTitle().equals(bookname)){
         synchronized(b){
           System.out.println("Someone's buying " + b.getTitle() + ". We have " + b.getCopies() + " copies, and they want " + copies + " copies.");
           if(copies <= b.getCopies()){
             //all copies can immediately be bought
             b.setCopies(b.getCopies() - copies);
             //Calculate how many milliseconds have gone by since this method was started.
             long timeToBuy = System.currentTimeMillis() - timeStarted;
             System.out.println("It took " + timeToBuy + " milliseconds to buy this.");
             return copies;
           } else {
             long time = System.currentTimeMillis() + (long)10000; //picks a time 10 seconds from now.
             int bought = 0;
             while(System.currentTimeMillis() < time){
               //this makes it wait only for the remainder of the 10 seconds.
               try{
                 b.wait(time - System.currentTimeMillis());
               }catch(InterruptedException e){
                 System.out.println("Interrupted: " + e);
               }
               if(b.getCopies() < copies){
                 //not enough copies were sold in 10 seconds
                 //so, we buy as many as there are
                 bought += b.getCopies();
                 b.setCopies(0);
               } else {
                 //enough copies now exist! we buy all we need.
                 b.setCopies(b.getCopies() - copies);
                 long timeToBuy = System.currentTimeMillis() - timeStarted;
                 System.out.println("It took " + timeToBuy + " milliseconds to buy this.");
                 return copies;
               }
             }
             long timeToBuy = System.currentTimeMillis() - timeStarted;
             System.out.println("It took " + timeToBuy + " milliseconds to buy this.");
             return bought;
           }
         }
       }
     }
     System.out.println("\"" + bookname + "\" not found.");
     Book b = new Book(bookname, 0);
     long time = System.currentTimeMillis() + (long)10000; //picks a time 10 seconds from now.
     int bought = 0;
     while(System.currentTimeMillis() < time){
       //this makes it wait only for the remainder of the 10 seconds.
       try{
        System.out.println("Waiting for " + (time - System.currentTimeMillis()) + " more milliseconds.");
         b.wait(time - System.currentTimeMillis());
       }catch(InterruptedException e){
         System.out.println("Interrupted: " + e);
       }
       if(b.getCopies() < copies){
         //not enough copies were sold in 10 seconds
         //so, we buy as many as there are
         bought += b.getCopies();
         b.setCopies(0);
         long timeToBuy = System.currentTimeMillis() - timeStarted;
         System.out.println("It took " + timeToBuy + " milliseconds to buy this.");
         return bought;
       } else {
         //enough copies now exist! we buy all we need.
         b.setCopies(b.getCopies() - copies);
         long timeToBuy = System.currentTimeMillis() - timeStarted;
         System.out.println("It took " + timeToBuy + " milliseconds to buy this.");
         return copies;
       }
     }
     //return 0 if all of the above doesn't work out.
     return 0;
   }
   
   public static void main(String args[]) {
 
     try {
       // create the RMI registry on the local machine
       Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
 
       // create an object we're going to call methods on remotely
       Server obj = new Server();
 
       // export the object in the registry so it can be retrieved by client,
       // casting to the Remote interface
       Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);
 
       // bind the remote object's stub in the registry
       registry.bind("Hello", stub);
 
       System.err.println("Server ready");
     } catch (Exception e) {
       System.err.println("Server exception: " + e.toString());
       e.printStackTrace();
     }
 
   }
 
 }
