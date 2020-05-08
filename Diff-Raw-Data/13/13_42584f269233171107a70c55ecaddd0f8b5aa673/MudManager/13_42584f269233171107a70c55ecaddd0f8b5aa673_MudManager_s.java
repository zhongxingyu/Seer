 package cs3517.hale.mud;
 
 import java.rmi.*;
 import java.util.*;
 import javax.naming.*;
 
 /**
  * The remote interface for the MudManager.  The mud manager is responsible for
  * joining, listing, creating and deleting MUDs.
  */
 public interface MudManager extends Remote
 {
 
   /**
    * Creates a new MUD.
    *
    * @param name     Uniquely identifies the game.
   * @param edges    Path to an edges file.
   * @param messages Path to an messages file.
   * @param things   Path to an things file.
    */
   public void makeMud(String name) throws RemoteException, NamingException;
 
   /**
    * Generates a nicely formatted list of muds managed by this MudManager.
    */
   public String printableMudList() throws RemoteException, NamingException;
 
   /**
    * Returns the stub interface for a MUD with a given name.
    *
    * @param name The name of the mud game to return.
    * @return A Mud
    */
   public Mud getGame(String name) throws RemoteException, NamingException;
 
 
 }
