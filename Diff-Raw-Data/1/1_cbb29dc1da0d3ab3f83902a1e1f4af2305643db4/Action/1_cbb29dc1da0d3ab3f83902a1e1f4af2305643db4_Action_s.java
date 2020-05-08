 package arena;
 
 // Libraries
 import scenario.*;
 import stackable.*;
 import exception.*;
 import parameters.*;
 import players.Base;
 import operation.Operation;
 
 /**
  * <b>Action</b><br>
  * Decide when a robot may  
  * (or not) make an action,
  * changing the arena 
  * accordingly to it.
  * @see World
  * 
  * @author Karina Suemi Awoki
  * @author Renato Cordeiro Ferreira
  * @author Vinicius Nascimento Silva
  */
 public class Action implements Game
 {      
     /**
      * Given a map, a robot and an operation,
      * calls the method for this operation to
      * the correspondent robot and map, returning
      * the answer to the caller. 
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      *
      * @throws InvalidOperationException
      */
     static Stackable[] ctrl (Map map, Robot turn, Operation op)
        throws InvalidOperationException
     {
         Stackable[] stackable = null;
         boolean can = false;
         switch(op.getAction())
         {
             case "MOVE" : can = MOVE (map, turn, op); break;
             case "DRAG" : can = DRAG (map, turn, op); break;    
             case "DROP" : can = DROP (map, turn, op); break;
             case "SKIP" : can = SKIP (map, turn, op); break;
             case "HIT"  : can = HIT  (map, turn, op); break;
             
             case "LOOK" : stackable = LOOK (map, turn, op); break;
             case "SEE"  : stackable = SEE  (map, turn, op); break;
             case "ASK"  : stackable = ASK  (map, turn, op); break;
         }
         
         if(stackable == null) 
         {
             stackable = new Stackable[1]; 
             stackable[0] = new Num( (can) ? 1 : 0 );
         }
         return stackable;
     }
     
     /**
      * Operation MOVE.<br>
      * Move, if possible, the robot to the selected
      * position in the map.
      * @see robot.Syst#MOVE
      *
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static boolean MOVE (Map map, Robot turn, Operation op) 
     {
         // Extract direction info from operation
         Stackable[] s = op.getArgument();
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int newI = turn.i + update[0];
         int newJ = turn.j + update[1];
         
         if(newI >= MAP_SIZE 
         || newJ >= MAP_SIZE  
         || newI < 0  
         || newJ < 0  
         || map.map[newI][newJ].scenario != null) return false;
         
         // Takes out from original position
         Robot robot = (Robot) map.map[turn.i][turn.j].removeScenario();
         
         // Update robot attributes
         turn.i = newI; 
         turn.j = newJ;
         
         // Goes to the new position in the map
         map.map[turn.i][turn.j].setScenario(robot);
         return true;
     }
     
     /**
      * Operation DRAG.<br>
      * Drag, if possible, an item in the selected
      * position in the map, storing it inside the 
      * robot.
      * @see robot.Syst#DRAG
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static boolean DRAG (Map map, Robot turn, Operation op)
     { 
          // Extract direction info from operation
         Stackable[] s = op.getArgument();
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int lookI = turn.i + update[0];
         int lookJ = turn.j + update[1];
         
         int cont = 0;
         
         if(lookI >= MAP_SIZE 
         || lookJ >= MAP_SIZE  
         || lookI < 0  
         || lookJ < 0  
         || map.map[lookI][lookJ].item == null) return false;
 
         for(int i = 0; i < turn.slots.length && turn.slots[i] != null; i++) cont++;
         if(cont >= turn.slots.length) return false;
             
         Debugger.say("    [DRAG]", map.map[lookI][lookJ] );
         
         turn.slots[cont] = map.map[lookI][lookJ].removeItem();
         
         Debugger.say("    [DRAG]", map.map[lookI][lookJ] );
         
         return true;
     }
     
     /**
      * Operation DROP.<br>
      * Drop, if possible, an item in the selected
      * position in the map, taking it from inside
      * robot.
      * @see robot.Syst#DROP
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static boolean DROP (Map map, Robot turn, Operation op)
     {  
         Stackable[] s = op.getArgument();
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int lookI = turn.i + update[0];
         int lookJ = turn.j + update[1];
         
         int cont = 0;
         
         if(lookI >= MAP_SIZE 
         || lookJ >= MAP_SIZE  
         || lookI < 0  
         || lookJ < 0  
         || map.map[lookI][lookJ].item != null) return false;
         
         // Takes out from original position
         Robot robot = (Robot) map.map[turn.i][turn.j].scenario;
         
         for(int i = 0; i < turn.slots.length && robot.slots[i] != null; i++) cont++;
         if(cont == 0) return false;
             
         Debugger.say("    [DROP]", map.map[lookI][lookJ]);
         
         boolean allow = false;
         if(map.map[lookI][lookJ].scenario instanceof Base)
         {
             // If the scenario is a base, throw the crystal on it
             Base b = (Base) map.map[lookI][lookJ].scenario;
             robot.removeSlots(cont - 1); allow = b.addCrystal(turn);
         }
         else 
         {
             // Otherwise, just throws the item (if possible)
             if(map.map[lookI][lookJ].item != null)
             {
                 robot.removeSlots(cont - 1);
                 allow = true;
             }
         }
         
         Debugger.say("    [DROP]", map.map[lookI][lookJ]);
         return allow;
     }
     
     /**
      * Operation SKIP.<br>
      * Make no operation (skip turn).
      * @see robot.Syst#SKIP
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static boolean SKIP (Map map, Robot turn, Operation op)
     {  
         Debugger.say("    [SKIP]"); // Debug
         return true;
     }
     
     /**
      * Operation HIT.<br>
      * Hit, if exists, the scenario in the selected
      * position in the map, making the damage of the
      * atack made by the robot.
      * @see robot.Syst#HIT
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static boolean HIT  (Map map, Robot turn, Operation op)
     {
         String pre = "    [HIT]";
         Stackable[] s = op.getArgument();
         
         Attack      atk  = (Attack) s[0];
         Num         num  = (Num)    s[1];
         Direction[] dirs = new Direction[(int)num.getNumber()];
         
         int damage = 0;
         int distance = (int) num.getNumber();
         
         // TODO: PROBLEMS HERE ↓ 
         // If we add more commands to HIT, we 
         // need to change this +2.
         for(int i = 0; i < distance; i++)
             dirs[i] = (Direction) s[i + 2];
         
         switch (atk.getAttack())
         {
             case "MELEE" : damage = turn.damageMelee; 
                            if(distance > 1)             return false; break;
             case "RANGED": damage = turn.damageRange; 
                            if(distance > turn.maxRange) return false; break;
         }
                 
         // Debug
         String directions = "";
         for(Direction d: dirs) directions += d.toString() + " ";
         
         int lookI = turn.i;
         int lookJ = turn.j;
         Scenario thing = null;
         
         Debugger.say("    [HIT]", "[", atk.getAttack() + "]");
         Debugger.say("    [HIT]", " ", directions);
 
         for(Direction d: dirs)
         {
             int[] update = d.get(lookI);
             
             lookI += update[0];
             lookJ += update[1];
             
             if(lookI >= MAP_SIZE
             || lookJ >= MAP_SIZE
             || lookI < 0
             || lookJ < 0) return false;
             
             // Debug
             Debugger.say("    [HIT]", map.map[lookI][lookJ]); 
                         
             thing = map.map[lookI][lookJ].getScenario();
             if(thing != null)
             {
                 // No attacks agains allies!
                 if(thing.getTeam() == turn.getTeam())
                 {
                     Debugger.say("    [HIT]", "[NONE]");
                     Debugger.say("    [HIT] ", thing, " is an ally");
                     return false;
                 }
                 
                 int done = thing.takeDamage(damage);
                 Debugger.say("    [HIT]", "[FIGHT]");
                 Debugger.say("         [DAMAGE:", damage, "]");
                 Debugger.say("         [REMAIN:", done  , "]"); 
                 
                 if(thing.getHP() <= 0) 
                 {                 
                     Debugger.say("    [HIT]", "[DESTROYED]");
                     World.destroy(lookI, lookJ);
                 }
                 break;
             }
         }   
         
         if(thing == null) 
         {
             Debugger.say("    [HIT]", "[EMPTY]");
             return false;
         }
         return true;
     }
     
     /**
      * Operation LOOK.<br>
      * Scans the terrain in a given position, to be
      * analysed by the robot virtual machine.
      * @see robot.Syst#LOOK
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static Stackable[] LOOK (Map map, Robot turn, Operation op)
     { 
          // Extract direction info from operation
         Stackable[] s = op.getArgument();
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int lookI = turn.i + update[0];
         int lookJ = turn.j + update[1];
         
         // Debug
         Debugger.say("    [LOOK] ", "dir: "   , d);
         Debugger.say("    [LOOK] ", "pos: I: ", lookI);
         Debugger.say("    [LOOK] ", "pos: J: ", lookJ);
         
         if(lookI >= MAP_SIZE 
         || lookJ >= MAP_SIZE  
         || lookI < 0  
         || lookJ < 0) return null;
         
         // Debug
         Debugger.say("    [LOOK] ", "ter: ", map.map[lookI][lookJ]);
         
         Stackable[] st = new Stackable[1];
         st[0] = map.map[lookI][lookJ];
         
         // Takes out from original position
         return st;
     }
     
     /**
      * Operation SEE.<br>
      * Scans the neighborhood of the robot (accordingly
      * to its position in the map and its sight).
      * @see robot.Syst#SEE
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      *
      * @throws InvalidOperationException
      */
     static Stackable[] SEE (Map map, Robot turn, Operation op)
         throws InvalidOperationException
     {
         Direction d;
         
         Stackable[] st = new Stackable[1];
         
         int nTerrain; 
         if(turn.sight == 1) nTerrain = 7;
         else nTerrain = 19;
         
         Terrain[] ter = new Terrain[nTerrain];
         
         int lookI;
         int lookJ;
         
         for(int i = 0; i < nTerrain; i++)
         {
             d = new Direction(0, i);
             
             int[] update = d.get(turn.i);
             lookI = turn.i + update[0];
             lookJ = turn.j + update[1];
             
             if(lookI >= MAP_SIZE 
             || lookJ >= MAP_SIZE  
             || lookI < 0  
             || lookJ < 0)         ter[i] = null;
             
             else 
             {  
                 if(i < 7)
                     ter[i] = map.map[lookI][lookJ];
                 else
                 {
                     d = new Direction(1, i);
                     update =  d.get(lookI);
                     lookI  += update[0];
                     lookJ  += update[1];
                     
                     if(lookI >= MAP_SIZE 
                     || lookJ >= MAP_SIZE  
                     || lookI < 0  
                     || lookJ < 0)         ter[i] = null;
                     
                     else  ter[i] = map.map[lookI][lookJ];
                     
                 }
             }
         }
         Around a = new Around(ter);
         st[0] = (Stackable) a;
         return st;
     }
     
     /**
      * Operation ASK.<br>
      * Passes an information to the robot, about
      * its own 'body' placed in the world (position).
      * @see robot.Syst#ASK
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static Stackable[] ASK (Map map, Robot turn, Operation op)
     {  
         Stackable[] stk = op.getArgument();
         Text t = (Text) stk[0];
         
         // Debug
         Debugger.say("    [ASK] ", t);
         
         // Return values
         Num one  = new Num(1);
         Num zero = new Num(0);
         
         switch (t.getText())
         {
             case "position":
             case "Position":
                 Num x    = new Num(turn.i);
                 Num y    = new Num(turn.j);
                 stk      = new Stackable[3];
                 stk[2]   = one; 
                 stk[1]   = x; 
                 stk[0]   = y;
                 break;
             
             case "base":
             case "Base":
                 Base b   = turn.getTeam().getBase();
                 Num posX = new Num(b.getPosX(turn));
                 Num posY = new Num(b.getPosY(turn));
                 stk      = new Stackable[3];
                 stk[2]   = one; 
                 stk[1]   = posX;
                 stk[0]   = posY;
                 break;
             
             default:
                 stk      = new Stackable[1];
                 stk[0]   = zero;
         }
         
         // Debug
         Debugger.print("    [ASK] ");
         for(Stackable s: stk)
             Debugger.print(s, ", ");
         Debugger.say();
         
         return stk;
     }
 }
