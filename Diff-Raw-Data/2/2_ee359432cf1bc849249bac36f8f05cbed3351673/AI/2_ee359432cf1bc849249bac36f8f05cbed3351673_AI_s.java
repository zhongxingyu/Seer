 import com.sun.jna.Pointer;
 import java.awt.Point;
 
 ///The class implementing gameplay logic.
class AI extends BaseAI
 {
   public String username()
   {
     return "Shell AI";
   }
   public String password()
   {
     return "password";
   }
 
   //This function is called each time it is your turn
   //Return true to end your turn, return false to ask the server for updated information
   public boolean run()
   {
     System.out.println("Starting turn " + turnNumber() + " of round " + roundNumber());
     // Find each player's warp gate
     int myGateIndex=0, theirGateIndex=0;
     for(int i = 0; i < ships.length; i++)
     {
       // If this ship is of type Warp Gate
       if(ships[i].getType().compareTo("Warp Gate") == 0)
       {
         // If you own this ship
         if(ships[i].getOwner() == playerID())
         {
           myGateIndex = i;
         }
         else
         {
           theirGateIndex = i;
         }
       }
     }
 
     // Warp in some ships
     for(int i = 0; i < shipTypes.length; i++)
     {
       // If you have enough energy to warp in this type of ship
       if(shipTypes[i].getCost() <= players[playerID()].getEnergy())
       {
         // Warp it in directly on top of your warp gate
         shipTypes[i].warpIn(ships[myGateIndex].getX(), ships[myGateIndex].getY());
       }
     }
 
     // Command your ships
     for(int i = 0; i < ships.length; i++)
     {
       // if you own this ship, it can move and it can attack
       if(ships[i].getOwner() == playerID() && ships[i].getMovementLeft() > 0 && ships[i].getAttacksLeft() > 0)
       {
         // Find a point on the line connecting this ship and their warp gate that is close enough for this ship to move to.
         // x and y are out parameters
         Point goal = pointOnLine(ships[i].getX(), ships[i].getY(), ships[theirGateIndex].getX(), ships[theirGateIndex].getY(), ships[i].getMovementLeft());
         // If I have to move to get there
         if(ships[i].getX() != goal.x || ships[i].getY() != goal.y)
           ships[i].move(goal.x, goal.y);
 
         // If the distance from my ship to their warp gate is less than my ships attack range plus their gate's radius
         if(distance(ships[i].getX(), ships[i].getY(), ships[theirGateIndex].getX(), ships[theirGateIndex].getY()) <= ships[i].getRange() + ships[theirGateIndex].getRadius())
         {
           // If their warp gate is still alive
           if(ships[theirGateIndex].getHealth() > 0)
             ships[i].attack(ships[theirGateIndex]);
         }
       }
     }
 
     return true;
   }
 
 
   //This function is called once, before your first turn
   public void init() {}
 
   //This function is called once, after your last turn
   public void end() {}
   
   
   public AI(Pointer c)
   {
     super(c);
   }
 }
