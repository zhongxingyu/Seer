 /*
  */
 package prosjekt;
 
 import prosjekt.interfaces.AdminWindow;
 import prosjekt.rooms.RoomRegistry;
 import prosjekt.rooms.types.SingleRoom;
 
 /**
  *
  * @author Kristoffer Berdal <web@flexd.net>
  */
 public class Main {
  public static RoomRegistry roomRegistry;
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    roomRegistry = new RoomRegistry();
    // TODO code application logic here
     for (int i = 0; i < 10; i++) {
       SingleRoom room = new SingleRoom(500f);
       roomRegistry.add(room);
     }
     new AdminWindow();
   }
 }
