 package program.mapping;
 
 import java.util.ArrayList;
 
 public class Mapper {
 
     public Mapper() {
     }
 
     public ArrayList<Step> getPath(Road current, ArrayList<Goal> goals) throws Exception {
         if (goals.size() < 1) {
             throw new Exception("No goals given! Must have at least one goal!");
         }
         //Store were we started so we can return here at the end
         Goal start = new Goal(current.getName(), 0, Direction.None);
         //store start in a convenient list so we can pass it to findPath()
         ArrayList<Goal> _start = new ArrayList<Goal>();
         _start.add(start);
         //find the path to all the goals
         ArrayList<Step> path = findPath(current, goals);
         //find were we stopped
         Road last = path.get(path.size() - 1).getRoad();
         //find the path from where we stopped to where we started
       // ArrayList<Step> pathToStart = findPath(last, _start);
         //combine the two paths
        //for (int i = 0; i < pathToStart.size(); i++) {
        //    path.add(pathToStart.get(i));
        //}
         //since were we started is not actually a parking space
         //remove the last parking direction and just put in a blank Step
         //object so we know that we finish here
        //path.remove(path.size() - 1);
         path.add(new Step());
         //return the complete path from the start to all the goals and back again
         return path;
     }
 
     private ArrayList<Step> findPath(Road current, ArrayList<Goal> goals) throws Exception { // Runs the A* search, puts result in path
         assert goals.size() > 0;
         assert current != null;
         assert current.hasLeftChild() || current.hasRightChild() || current.hasStraightChild();
 
         ArrayList<Road> open = new ArrayList<Road>(); // The nodes that need to be expanded
         ArrayList<Road> closed = new ArrayList<Road>(); // The nodes that have already been expanded
         Road goal = null;
         current.setG_value(0);// make sure we start at 0
         open.add(current);//to comply with expand assertions
         while (goal == null) {// while we have not hit a goal
             goal = expand(current, open, closed, goals);// Expand from the current node to the surrounding nodes
             if (goal == null) {
                 current = getBestNode(open);// Picks the next current node that will most likely lead to the goal		
             }
         }
 
         //travels backward form goal to the start and records that path
         //also removes the goals we just found from goals
         ArrayList<Step> path = genPath(goal, goals);
 
         //if goals is > 0 then we have more goals to find, so call findPath again with goal as starting point and with the new goals
         if (goals.size() > 0) {
             ArrayList<Step> nextPath = findPath(goal, goals);//Recursively call findPath to find the next goal
             //store the next part of the path in path
             for (int i = 0; i < nextPath.size(); i++) {
                 path.add(nextPath.get(i));
             }
         }
         assert path.size() > 0;
         assert goals.size() == 0;
         //if goals.size() = 0, we are done so return the full path
         return path;
     }
     //takes the expanded map and works backward from goal to the start and generates the directions
 
     private ArrayList<Step> genPath(Road goal, ArrayList<Goal> goals) throws Exception {
         assert goal != null;
         assert goals.size() > 0;
         assert goal.hasExpandedLeftParent() || goal.hasExpandedRightParent() || goal.hasExpandedStraightParent();
         assert goal.getG_value() > 0;
 
         ArrayList<Step> path = new ArrayList<Step>();
         Goal goalInfo = Goal.getGoal(goals, goal);
         assert goalInfo != null;
         //give parking information
         path.add(new Park(goal, goalInfo.getDirection(), goalInfo.getPark()));
 
         goals.remove(goalInfo);//we already found this goal, so remove it
         Road current = goal;
        System.out.println(goal.getName());
         while (current.getG_value() != 0) {//while we have not found our starting point: a g_value of 0
             //place all the parents of current in convenient variables
             //get_parent returns null if no parent
             Road right = current.getRightParent();
             Road left = current.getLeftParent();
             Road straight = current.getStraightParent();
 
             ArrayList<Road> parents = new ArrayList<Road>();//put all parents in here to run through the getBestNode function
             //only add parent to parents if it is not null. makes sure you don't accidently choose a -1 node
             if (current.hasExpandedRightParent()) {
                 parents.add(right);
             }
             if (current.hasExpandedLeftParent()) {
                 parents.add(left);
             }
             if (current.hasExpandedStraightParent()) {
                 parents.add(straight);
             }
 
             assert parents.size() > 0;//all parents are null
 
             Road best = getBestNode(parents);//find the node with the least g_value that is not -1
 
             //record in path, reverses directions because this function travels backwards: from goal to start
             if (best == right) {
                path.add(0, new Turn(current, Direction.Left));
             } else if (best == left) {
                path.add(0, new Turn(current, Direction.Right));
             } else if (best == straight) {
                path.add(0, new Turn(current, Direction.Straight));
             } else {
                 assert false;//code should not reach this!
             }
             current = best;// record best in current so we can continue traveling backwards toward the start
 
         }
         assert current.getG_value() == 0;
         assert path.size() > 0;
         assert path.get(path.size() - 1) instanceof Park;
         return path;
     }
 
     // returns the node in open with the smallest gvalue + h(x) where h(x) is the heuristic
     private Road getBestNode(ArrayList<Road> open) {
         assert open.size() > 0;
         Road best = open.get(0);
         for (int i = 0; i < open.size(); i++) {
             Road road = open.get(i);
             if (road.getG_value() < best.getG_value()) {
                 best = road;
             }
         }
         assert best.getG_value() != -1;
         assert best != null;
         return best;
     }
 
     //expands open into the nodes surrounding current. Returns the first goal hit, if one is hit and returns null otherwise
     private Road expand(Road current, ArrayList<Road> open, ArrayList<Road> closed, ArrayList<Goal> goals) {
         assert current != null;
         assert goals.size() > 0;
         assert !closed.contains(current);
         assert open.contains(current);
         assert current.hasLeftChild() || current.hasRightChild() || current.hasStraightChild();
         assert !Goal.isGoal(goals, current);
 
         //getChild returns null if no child
         Road right = current.getChildRight();
         Road left = current.getChildLeft();
         Road straight = current.getChildStraight();
 
 
         //if child exists and we have not expanded it yet
         if (current.hasRightChild() && !closed.contains(right)) {
             right.setG_value(current.getG_value() + right.getCost());//record the cost of getting here
             if (Goal.isGoal(goals, right)) {
                 assert !closed.contains(right);
                 return right; //check if we hit one of the goals, if so return it
             }
 
             open.add(right);//add road to the open list so it can be expanded in the future
         }
 
         if (current.hasLeftChild() && !closed.contains(left)) {
             left.setG_value(current.getG_value() + left.getCost());
             if (Goal.isGoal(goals, left)) {
                 assert !closed.contains(left);
                 return left;
             }
             open.add(left);
         }
 
         if (current.hasStraightChild() && !closed.contains(straight)) {
             straight.setG_value(current.getG_value() + straight.getCost());
             if (Goal.isGoal(goals, straight)) {
                 assert !closed.contains(straight);
                 return straight;
             }
             open.add(straight);
         }
 
         closed.add(current);//make sure we don't expand current again
         open.remove(current);//remove from open because it has already been expanded
 
         assert open.size() > 0;
         return null;//null because we did not find the goal yet
     }
 
     public static Road getMap() {
         Road start = new Road("start", 1);
         Road R0 = new Road("R0", 1);
         Road R1 = new Road("R1", 1);
         Road R2 = new Road("R2", 1);
         Road R3 = new Road("R3", 1);
         Road R4 = new Road("R4", 1);
         Road R5 = new Road("R5", 1);
         Road R6 = new Road("R6", 1);
         Road R7 = new Road("R7", 1);
         Road R8 = new Road("R8", 1);
         Road R9 = new Road("R9", 1);
         Road R10 = new Road("R10", 1);
         Road R11 = new Road("R11", 1);
         Road R12 = new Road("R12", 1);
         Road R13 = new Road("R13", 1);
         Road R14 = new Road("R14", 1);
         Road R15 = new Road("R15", 1);
         Road R16 = new Road("R16", 1);
         Road R17 = new Road("R17", 1);
         Road R18 = new Road("R18", 1);
         Road R19 = new Road("R19", 1);
         Road R20 = new Road("R20", 1);
         Road R21 = new Road("R21", 1);
         Road R22 = new Road("R22", 1);
         Road R23 = new Road("R23", 1);
         Road R24 = new Road("R24", 1);
         Road R25 = new Road("R25", 1);
         Road R26 = new Road("R26", 1);
         Road R27 = new Road("R27", 1);
         Road R28 = new Road("R28", 1);
         Road R29 = new Road("R29", 1);
         Road R30 = new Road("R30", 1);
         Road R31 = new Road("R31", 1);
         Road R32 = new Road("R32", 1);
         Road R33 = new Road("R33", 1);
         Road R34 = new Road("R34", 1);
         Road R35 = new Road("R35", 1);
         Road R36 = new Road("R36", 1);
         Road R37 = new Road("R37", 1);
         Road R38 = new Road("R38", 1);
         Road R39 = new Road("R39", 1);
         Road R40 = new Road("R40", 1);
         Road R41 = new Road("R41", 1);
         Road R42 = new Road("R42", 1);
         Road R43 = new Road("R43", 1);
         Road R44 = new Road("R44", 1);
         Road R45 = new Road("R45", 1);
         Road R46 = new Road("R46", 1);
         Road R47 = new Road("R47", 1);
         Road R48 = new Road("R48", 1);
         Road R49 = new Road("R49", 1);
         Road R50 = new Road("R50", 1);
         Road R51 = new Road("R51", 1);
         Road R52 = new Road("R52", 1);
         Road R53 = new Road("R53", 1);
         Road R54 = new Road("R54", 1);
         Road R55 = new Road("R55", 1);
         Road R56 = new Road("R56", 1);
         Road R57 = new Road("R57", 1);
         Road R58 = new Road("R58", 1);
         Road R59 = new Road("R59", 1);
         Road R60 = new Road("R60", 1);
         Road R61 = new Road("R61", 1);
         Road R62 = new Road("R62", 1);
         Road R63 = new Road("R63", 1);
         Road R64 = new Road("R64", 1);
         Road R65 = new Road("R65", 1);
         Road R66 = new Road("R66", 1);
         Road R67 = new Road("R67", 1);
         Road R68 = new Road("R68", 1);
         Road R69 = new Road("R69", 1);
         Road R70 = new Road("R70", 1);
         Road R71 = new Road("R71", 1);
 
         start.setLeftChild(R23);
 
         R0.setLeftChild(R1);
 
         R1.setStraightChild(R3);
         R1.setLeftChild(R2);
 
         R2.setStraightChild(R4);
 
         R3.setStraightChild(R5);
 
         R4.setLeftChild(R5);
 
         R5.setStraightChild(R6);
         R5.setLeftChild(R7);
         
         
 
         R6.setStraightChild(R8);
         R6.setRightChild(R9);
 
         R7.setStraightChild(R24);
         R7.setRightChild(R38);
 
         R8.setStraightChild(R10);
         R8.setLeftChild(R11);
 
         R9.setRightChild(R40);
 
         R10.setRightChild(R12);
         R10.setLeftChild(R56);
 
         R11.setStraightChild(R71);
         R11.setLeftChild(R43);
 
         R12.setLeftChild(R14);
 
         //no R13
 
         R14.setStraightChild(R15);
 
         R15.setLeftChild(R16);
 
         R16.setLeftChild(R39);
         R16.setStraightChild(R17);
 
         R17.setLeftChild(R50);
         R17.setStraightChild(R18);
 
         R18.setStraightChild(R19);
         R18.setLeftChild(R20);
 
         R19.setLeftChild(R22);
         R19.setStraightChild(R21);
 
         R20.setStraightChild(R29);
         R20.setLeftChild(R28);
         
         R21.setLeftChild(R64);
         R21.setStraightChild(R23);
          
         R22.setLeftChild(R70);
         R22.setStraightChild(R45);
         
         R23.setLeftChild(R0);
         
         R24.setLeftChild(R33);
         R24.setStraightChild(R25);
         
         R25.setLeftChild(R70);
         R25.setStraightChild(R26);
         
         R26.setLeftChild(R19);
         
         R27.setLeftChild(R25);
         R27.setStraightChild(R70);
         
         R28.setLeftChild(R29);
         R28.setStraightChild(R27);
         
         R28.setRightChild(R30);
         
         R30.setRightChild(R32);
         R30.setStraightChild(R31);
         
         R31.setRightChild(R48);
         R31.setStraightChild(R30);
         
         R33.setLeftChild(R46);
         R33.setStraightChild(R34);
         
         R34.setLeftChild(R35);
         
         R35.setLeftChild(R36);
         
         R36.setRightChild(R47);
         R36.setStraightChild(R37);
         
         R37.setLeftChild(R24);
         R37.setStraightChild(R38);
         
         R38.setRightChild(R49);
         R38.setStraightChild(R40);
         
         R39.setLeftChild(R55);
         R39.setStraightChild(R59);
         
         R40.setRightChild(R53);
         R40.setStraightChild(R41);
         
         R41.setLeftChild(R71);
         R41.setStraightChild(R42);
         
         R42.setRightChild(R60);
         R42.setStraightChild(R43);
         
         R43.setLeftChild(R57);
         R43.setStraightChild(R44);
         
         R44.setLeftChild(R15);
         
         R45.setRightChild(R34);
         R45.setStraightChild(R46);
         
         R46.setLeftChild(R37);
         R46.setStraightChild(R47);
         
         R47.setLeftChild(R5);
         
         R48.setLeftChild(R40);
         R48.setStraightChild(R49);
         
         R49.setLeftChild(R6);
         
         R50.setLeftChild(R54);
         R50.setStraightChild(R51);
         
         R51.setRightChild(R63);
         R51.setStraightChild(R52);
         
         R52.setLeftChild(R41);
         R52.setStraightChild(R53);
         
         R53.setLeftChild(R8);
         
         R54.setRightChild(R59);
         R54.setStraightChild(R55);
         
         R55.setLeftChild(R58);
         
         R56.setRightChild(R44);
         R56.setStraightChild(R57);
         
         R57.setLeftChild(R55);
         R57.setStraightChild(R58);
         
         R58.setRightChild(R16);
         
         R59.setRightChild(R55);
         R59.setStraightChild(R39);
         
         R60.setRightChild(R43);
         R60.setStraightChild(R59);
         
         R61.setStraightChild(R62);
         
         R62.setLeftChild(R52);
         R62.setStraightChild(R63);
         
         R63.setRightChild(R30);
         
         R64.setStraightChild(R65);
         R64.setLeftChild(R67);
         
         R65.setLeftChild(R36);
         R65.setStraightChild(R66);
         
         R66.setLeftChild(R4);
         
         R67.setRightChild(R69);
         R67.setStraightChild(R68);
         
         R68.setStraightChild(R64);
         
         R69.setStraightChild(R70);
         R69.setRightChild(R45);
         
         R70.setLeftChild(R26);
         R70.setStraightChild(R27);
         
         R71.setLeftChild(R62);
         
 
         return start;
 
     }
 }
