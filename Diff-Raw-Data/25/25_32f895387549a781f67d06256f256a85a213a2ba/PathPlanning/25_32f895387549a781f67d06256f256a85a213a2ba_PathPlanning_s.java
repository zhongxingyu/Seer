 package rrt;
 
 import java.util.LinkedList;
 
 import core.Config;
 import core.StateEstimator;
 
 import map.Map;
 import map.Point;
 import map.Segment;
 import state_machine.StateMachine;
 
 public class PathPlanning {
     private static PathPlanning instance;
     
     
 	private StateMachine sm;
     private StateEstimator se;
     private Map map;
     
     LinkedList<Point> path;
     Point nextWaypoint;
     public Point goal;
 
     public PathPlanning() {
         this.sm = StateMachine.getInstance();
         this.se = StateEstimator.getInstance();
         this.map = Map.getInstance();
     }
 
     public PathPlanning(StateMachine sm, StateEstimator se, Map map) {
         this.sm = sm;
         this.se = se;
         this.map = map;
         this.goal = sm.goal;
     }
 
     
     public static PathPlanning getInstance() {
         if (instance == null)
             instance = new PathPlanning();
         return instance;   
     }
     
     public static PathPlanning getInstance(StateMachine sm, StateEstimator se, Map map) {
         if (instance == null)
             instance = new PathPlanning(sm,se,map);
         return instance;   
     }
 
     public void step() {
    	Point curLoc= new Point(map.bot.pose.x, map.bot.pose.y);
     	if(sm.goal != goal || nextWaypoint == null || !map.checkSegment(new Segment(curLoc, nextWaypoint))){
 //    		System.out.println("finding path");
     		findPath();
     	}
 //		System.out.println("have path");
    	for (Point p : path){
    		if (curLoc.distance(p) < curLoc.distance(nextWaypoint) && map.checkSegment(new Segment(curLoc, p))){
    			nextWaypoint = p;
     		}
     	}
     }
 
     public Point getNextWaypoint(){
     	return nextWaypoint;
     }
     
     public void findPath() {
     	Point goal = sm.goal;
     	Point start = new Point(map.bot.pose.x, map.bot.pose.y);
     	TreeNode root = new TreeNode(start);
     	Tree rrt = new Tree(root);
     	
     	Point p;	
     	TreeNode closest, newNode, goalNode;
     	Segment seg;
     	if(map.checkSegment(new Segment(start,goal))){
     		path = new LinkedList<Point>();
         	path.add(start);
     		path.add(goal);
     	}
     	
     	while (true){
     		p = map.randomPoint();
 
     		closest = root;
     		for (TreeNode node : rrt.nodes){
     			if (node.loc.distance(p) < closest.loc.distance(p)){
     				closest = node;
     			}
     		}
     		
     		seg = new Segment(closest.loc, p);
     		seg = seg.trim(Config.MAXLENGTH);
     		p = seg.end;
     		if (!map.checkSegment(seg)){  	        
 //    			System.out.println("not valid");
     			continue;
     		}
            
     		newNode = new TreeNode(seg.end);
     		closest.addChild(newNode);
     		seg = new Segment(goal, p);
     	
     		if (!map.checkSegment(seg)){
     			continue;
     		}
     		
     		goalNode = new TreeNode(goal);
     		newNode.addChild(goalNode);
     		break;
     	}
     	
     	TreeNode curNode = goalNode;
     	Boolean pathcomplete = false;
     	path = new LinkedList<Point>();
     	System.out.print("path = ");
     	while (!pathcomplete){
     		path.add(0, curNode.loc);
     		if (curNode == rrt.root){
     			pathcomplete = true;
     			break;
     		}
     		System.out.print(new Segment(curNode.loc, curNode.parent.loc)+", ");
     		curNode = curNode.parent;
     	}
     	map.path = path;
     	nextWaypoint = path.get(0);
     	System.out.println(path);
     	
     }
 }
