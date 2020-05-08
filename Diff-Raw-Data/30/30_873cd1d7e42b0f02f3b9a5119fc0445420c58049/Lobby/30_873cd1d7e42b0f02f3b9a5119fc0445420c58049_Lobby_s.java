 package mel.fencing.server;
 
 import java.util.ArrayList;
 
 public class Lobby
 {
    ArrayList<UserSession> openChallenges = new ArrayList<UserSession>();
    ArrayList<UserSession> unavailable = new ArrayList<UserSession>();
     
     /*
      * OPCODES FOR LOBBY
      * F = Failure
      * T = Targeted
      * A = Accept
      * C = Canceled
      */
     
    public void challengeOpen(UserSession challenger)
     {
        openChallenges.add(challenger);
     }
     
    public void challengeTarget(UserSession challenger, UserSession target)
     {
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
