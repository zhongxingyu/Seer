 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package arena;
 
 // Default Libraries
 
 import java.util.ArrayList;
 
 // Libraries
 import scenario.*;
 import stackable.*;
 import exception.*;
 import parameters.*;
 import players.Base;
 import robot.Returns;
 import remote.Operation;
 
 // Import links
 import static parameters.Game.*;
 import static robot.Returns.*;
 
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
 public class Action
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
         String action = op.getAction();
         
         // Try to collect the power to do the action.
         // If the robot do not have it, returns NO_ENERGY
         // to the RVM.
         if (!turn.spendPower(action)) 
         {
             stackable = new Stackable[1]; 
             stackable[0] = returnValue(NO_ENERGY);
             return stackable;
         }
         
         switch(action)
         {
             case "MOVE" : stackable = MOVE (map, turn, op); break;
             case "DRAG" : stackable = DRAG (map, turn, op); break;    
             case "DROP" : stackable = DROP (map, turn, op); break;
             case "SKIP" : stackable = SKIP (map, turn, op); break;
             case "HIT"  : stackable = HIT  (map, turn, op); break;
             case "LOOK" : stackable = LOOK (map, turn, op); break;
             case "SEE"  : stackable = SEE  (map, turn, op); break;
             case "ASK"  : stackable = ASK  (map, turn, op); break;
             case "SEND" : stackable = SEND (map, turn, op); break;
         }
         
         if(stackable == null) 
         {
             stackable = new Stackable[1]; 
             stackable[0] = returnValue(INVALID_ACTION);
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
     static Stackable[] MOVE (Map map, Robot turn, Operation op) 
     {
         // Extract direction info from operation
         Stackable[] s = op.getArgument();
         Stackable[] ret = new Stackable[1];
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int newI = turn.i + update[0];
         int newJ = turn.j + update[1];
         
         if(newI >= MAP_SIZE 
         || newJ >= MAP_SIZE  
         || newI < 0  
         || newJ < 0  
         || map.map[newI][newJ].scenario != null)
         {
             ret[0] = returnValue(END_OF_MAP);
             return ret;
         }
         
         Type type = map.map[newI][newJ].type;
         switch(type)
         {
             case BLOCKED: ret[0] = returnValue(BLOCKED); return ret;
         }
         
         // Takes out from original position
         Robot robot = (Robot) map.map[turn.i][turn.j].removeScenario();
         
         // Update robot attributes
         turn.i = newI; 
         turn.j = newJ;
         
         turn.terrain = map.map[turn.i][turn.j];
         
         // Goes to the new position in the map
         map.map[turn.i][turn.j].setScenario(robot);
         ret[0] = returnValue(SUCCEDED);
         
         //Set the phase of the animation of the robot
         turn.setPhase(d); 
         return ret;
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
     static Stackable[] DRAG (Map map, Robot turn, Operation op)
     { 
          // Extract direction info from operation
         Stackable[] s = op.getArgument();
         Stackable[] ret = new Stackable[1];
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int lookI = turn.i + update[0];
         int lookJ = turn.j + update[1];
         
         int cont = 0;
         
         if(lookI >= MAP_SIZE 
         || lookJ >= MAP_SIZE  
         || lookI < 0  
         || lookJ < 0  
         || map.map[lookI][lookJ].item == null)
         {
             ret[0] = returnValue(END_OF_MAP);
             return ret;
         }
 
         for(int i = 0; i < turn.slots.length && turn.slots[i] != null; i++) cont++;
         if(cont >= turn.slots.length)
         {
             ret[0] = returnValue(FULL_SLOTS);
             return ret;
         }
             
         Debugger.say("    [DRAG]", map.map[lookI][lookJ] );
         
         turn.slots[cont] = map.map[lookI][lookJ].removeItem();
         
         Debugger.say("    [DRAG]", map.map[lookI][lookJ] );
         
         ret[0] = returnValue(SUCCEDED);
         return ret;
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
     static Stackable[] DROP (Map map, Robot turn, Operation op)
     {  
         Stackable[] s = op.getArgument();
         Stackable[] ret = new Stackable[1];
         boolean allow;
         Direction d = (Direction) s[0];
         int[] update = d.get(turn.i);
         
         int lookI = turn.i + update[0];
         int lookJ = turn.j + update[1];
         
         int cont = 0;
         
         System.out.println("entrei");
         
         if(lookI >= MAP_SIZE 
         || lookJ >= MAP_SIZE  
         || lookI < 0  
         || lookJ < 0  
         || map.map[lookI][lookJ].item != null)
         {
             ret[0] = returnValue(END_OF_MAP);
             return ret;
         }
         
         // Takes out from original position
         Robot robot = (Robot) map.map[turn.i][turn.j].scenario;
         
         for(int i = 0; i < turn.slots.length && robot.slots[i] != null; i++) cont++;
         if(cont == 0)
         {
             ret[0] = returnValue(EMPTY_SLOTS);
             return ret;
         }
             
         Debugger.say("    [DROP]", map.map[lookI][lookJ]);
         
         ret[0] = returnValue(NOT_SUCCEDED);
         if(map.map[lookI][lookJ].scenario instanceof Base)
         {
             // If the scenario is a base, throw the crystal on it
             Base b = (Base) map.map[lookI][lookJ].scenario;
             robot.removeSlots(cont - 1); allow = b.addCrystal(turn);
             ret[0] = returnValue((allow)? SUCCEDED : NOT_SUCCEDED);
         }
         else 
         {
             // Otherwise, just throws the item (if possible)
             if(map.map[lookI][lookJ].item == null)
             {
                 map.map[lookI][lookJ].item = robot.removeSlots(cont - 1);
                 ret[0] = returnValue(SUCCEDED);
             }
         }
         
         Debugger.say("    [DROP]", map.map[lookI][lookJ]);
         return ret;
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
     static Stackable[] SKIP (Map map, Robot turn, Operation op)
     {  
         Stackable[] ret = new Stackable[1];
         Debugger.say("    [SKIP]"); // Debug
         ret[0] = returnValue(SUCCEDED);
         return ret;
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
     static Stackable[] HIT  (Map map, Robot turn, Operation op)
     {
         String pre = "    [HIT]";
         Stackable[] ret = new Stackable[1];
         Stackable[] s = op.getArgument();
         
         int damage = 0;
         Attack atk = (Attack) s[0];
         
         Scenario thing = null;
         int lookI = turn.i, lookJ = turn.j;
         
         ;
         
         if(s[1] instanceof Coordinate)
         {
             Coordinate coord = (Coordinate) s[1];
             int targetI = coord.getI(), targetJ = coord.getJ();
             
             int range = 1;
             switch (atk.getAttack())
             {
                 case "MELEE" : damage = turn.damageMelee; 
                                range = 1;
                                break;
                 case "RANGED": damage = turn.damageRange; 
                                range = turn.maxRange;
                                break;
             }
             
             for(int i = 0; i < range; i++)
             {
                 int Δi = targetI - lookI;
                 int Δj = targetJ - lookJ;
                 
                 if(Δj == 0)
                 {
                     if(Δi > 0) lookI += 1;
                     if(Δi < 0) lookI -= 1;
                 }
                 else if(Δi == 0)
                 {
                     if(Δj > 0) lookJ += 1;
                     if(Δj < 0) lookJ -= 1;
                 }
                 else
                 {
                    if(Δj < 0) lookJ += -1 * (lookI)%2;
                     if(Δj > 0) lookJ +=  1 * (lookI)%2;
                     lookI += (Δi > 0) ? 1 : -1;
                 }
                 
                 if(lookI >= MAP_SIZE
                 || lookJ >= MAP_SIZE
                 || lookI < 0
                 || lookJ < 0)
                 {
                     ret[0] = returnValue(END_OF_MAP);
                     return ret;
                 }    
                 
                 thing = map.map[lookI][lookJ].getScenario();
                 if(thing != null) break;
             }
             
             // If it is not reached returns out of range error
             if(targetI - lookI != 0 || targetJ - lookJ != 0)
             {
                 ret[0] = returnValue(OUT_OF_RANGE);
                 return ret;    
             }
         }
         else if(s[1] instanceof Num)
         {
             Num         num  = (Num)    s[1];
             Direction[] dirs = new Direction[(int)num.getNumber()];
             
             int distance = (int) num.getNumber();
             
             // TODO: PROBLEMS HERE ↓ 
             // If we add more commands to HIT, we 
             // need to change this +2.
             for(int i = 0; i < distance; i++)
                 dirs[i] = (Direction) s[i + 2];
             
             switch (atk.getAttack())
             {
                 case "MELEE" : damage = turn.damageMelee; 
                                if(distance > 1)
                                {
                                     ret[0] = returnValue(OUT_OF_RANGE);
                                     return ret;
                                }    
                                break;
                 case "RANGED": damage = turn.damageRange; 
                                if(distance > turn.maxRange)
                                {
                                     ret[0] = returnValue(OUT_OF_RANGE);
                                     return ret;
                                }    
                                break;
             }
             
             // Debug
             String directions = "";
             for(Direction d: dirs) directions += d.toString() + " ";
             
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
                 || lookJ < 0)
                 {
                     ret[0] = returnValue(END_OF_MAP);
                     return ret;
                 }    
                 
                 // Debug
                 Debugger.say("    [HIT]", map.map[lookI][lookJ]); 
                             
                 thing = map.map[lookI][lookJ].getScenario();
                 if(thing != null) break;
             }   
         }
         
         // Attack in the air
         if(thing == null) 
         {
             Debugger.say("    [HIT]", "[EMPTY]");
             ret[0] = returnValue(NO_TARGET);
             return ret;
         }
         
         // No attacks against allies!
         if(thing.getTeam() == turn.getTeam())
         {
             Debugger.say("    [HIT]", "[NONE]");
             Debugger.say("    [HIT] ", thing, " is an ally");
             ret[0] = returnValue(END_OF_MAP);
             return ret; 
         }
         
         // Thing take damage
         int done = thing.takeDamage(damage);
         Debugger.say("    [HIT]", "[FIGHT]");
         Debugger.say("         [DAMAGE:", damage, "]");
         Debugger.say("         [REMAIN:", done  , "]"); 
         
         // If its HP are below 0
         if(thing.getHP() <= 0) 
         {                 
             Debugger.say("    [HIT]", "[DESTROYED]");
             World.destroy(lookI, lookJ);
         }
         
         ret[0] = returnValue(SUCCEDED);
         return ret;
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
         
         Stackable[] st = new Stackable[2];
         
         ArrayList<Terrain>    ter    = new ArrayList<Terrain>(); 
         ArrayList<Coordinate> coords = new ArrayList<Coordinate>();    
         
         searchR(map, ter, coords, turn.i, turn.j, turn.sight);
         
         Terrain   [] arrayOfTer = ter   .toArray(new Terrain   [0]);
         Coordinate[] arrayOfCoo = coords.toArray(new Coordinate[0]);
         
         Around a = new Around(arrayOfTer, arrayOfCoo);
         st[0] = (Stackable) a; 
         st[1] = new Num (1);
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
                 Num i    = new Num(turn.i);
                 Num j    = new Num(turn.j);
                 stk      = new Stackable[2];
                 stk[1]   = j; 
                 stk[0]   = i;
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
                 
             case "edge":
             case "Edge":
                 stk      = new
                  Stackable[2];
                 stk[1]   = one;
                 stk[0]   = new Num(MAP_SIZE);
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
     
     /**
      * Operation SEND.<br>
      * Receives an info from the cache memory of the robot,
      * to be resent to the other robots through their network.
      * @see robot.Syst#SEND
      * 
      * @param map  Map of the arena
      * @param turn Robot that may do the action
      * @param op   Operation to be executed (or not)
      */
     static Stackable[] SEND (Map map, Robot turn, Operation op)
     {  
         Stackable[] info = op.getArgument();
         
         // Send message to all robots
         for(Robot r: turn.getTeam().getRobots())
             if(r != turn) r.download(info);
         
         return new Stackable[] { new Num(1) };
     }
     
     /**
      * Auxiliar function for returning
      * a value from a Returns enum.
      * @param r Returns enum with 
      *          return value associated
      *          with a string
      */
     private static Num returnValue(Returns r)
     {
         return new Num(r.getValue());
     }
     
     private static void searchR
     (
         Map map, 
         ArrayList<Terrain> ter,
         ArrayList<Coordinate> coords, 
         int I, 
         int J, 
         int range
     )
         throws InvalidOperationException
     {
         Direction dir = new Direction("");
         int [] update;
         
         if(I < 0 || I >= MAP_SIZE) return;
         if(J < 0 || J >= MAP_SIZE) return;
         
         if(!ter.contains(map.map[I][J]))
         {
             coords.add(new Coordinate(I,J));
             ter.add(map.map[I][J]);
         }
         if(range == 0) return;
             
         for(int k = 1; k <= 6; k++)
         {
             dir.set(k); update = dir.get(I);
             if(J + update[1] < 0 || J + update[1] >= MAP_SIZE) continue;
             if(I + update[0] < 0 || I + update[0] >= MAP_SIZE) continue;
             searchR(map, ter, coords, I + update[0], J + update[1], range-1);
         }
     }
 }
