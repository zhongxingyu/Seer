 package mel.fencing.server;
 
 import java.util.ArrayList;
 
 public class Lobby
 {
    private UserSession openChallenger = null;
    private ArrayList<UserSession> unavailable = new ArrayList<UserSession>();
     
     /*
      * OPCODES FOR LOBBY
      * F = Failure
      * T = Targeted
      * A = Accept
      * C = Canceled
      */
     
    synchronized public void challengeOpen(UserSession challenger)
     {
        if(openChallenger == null) openChallenger = challenger;
        else startGame(challenger, openChallenger);
     }
     
    synchronized public void challengeTarget(UserSession challenger, UserSession target)
     {
        if(target == null)
        {
            challenger.send("FOpponent is not logged in");
        }
         if(challenger == target)
         {
             challenger.send("FCannot challenge self.");
             return;
         }
         if(unavailable.contains(target))
         {
             challenger.send("F"+target.getName()+" is already in a game.");
             return;
         }
         target.send("T"+challenger.getName());
     }
     
    synchronized public void cancel(UserSession out)
    {
        //TODO cancel challenge (if any) by out -- to be called by purge and when user manually cancels
    }
    
    public void startGame(UserSession a, UserSession b)
    {
        // TODO call the appropriate item in Game module
    }
    
     public void acceptChallenge(UserSession challenger, UserSession target)
     {
         if(unavailable.contains(challenger))
         {
             target.send("F"+challenger.getName()+" is already in a game");
             return;
         }
         unavailable.add(challenger);
         unavailable.add(target);
         Game.newGame(challenger,target);
     }    
 }
