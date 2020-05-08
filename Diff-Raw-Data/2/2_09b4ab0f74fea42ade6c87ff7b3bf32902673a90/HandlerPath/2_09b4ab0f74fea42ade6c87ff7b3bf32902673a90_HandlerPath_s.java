 package main;
 
 public class HandlerPath implements Runnable {
 
     protected GuiGame parent;
 
    public HandlerPath(GuiGame parent, int id, int initialX, int initialY, int destinationX, int destinationY) {
         this.parent = parent;
         System.out.println("moving from " + initialX + ";" + initialY + " to " + destinationX + ";" + destinationY);
     }
 
     @Override
     public void run() {
         System.out.println("movement started");
     }
 }
