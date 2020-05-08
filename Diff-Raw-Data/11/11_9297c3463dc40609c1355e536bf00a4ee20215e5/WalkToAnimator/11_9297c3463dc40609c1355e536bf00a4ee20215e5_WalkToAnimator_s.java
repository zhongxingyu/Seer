 package de.hohenheim.view.mobile.animation;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Vector;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.ManhattanConnectionRouter;
 import org.eclipse.draw2d.geometry.PointList;
 import de.hohenheim.controller.events.AnimationEvents;
 import de.hohenheim.modell.State;
 import de.hohenheim.view.canvas.AnimationControllerCanvas;
 import de.hohenheim.view.map.NodeMap;
 import de.hohenheim.view.mobile.AnimationFigure;
 import de.hohenheim.view.mobile.TrainFigure;
 import de.hohenheim.view.mobile.Utility;
 import de.hohenheim.view.mobile.animation.exceptions.PathNotFoundException;
 import de.hohenheim.view.mobile.animation.listeners.AnimationFinishedEvent;
 import de.hohenheim.view.mobile.animation.listeners.AnimationStartedEvent;
 import de.hohenheim.view.path.PathFigure;
 import de.hohenheim.view.node.NodeFigure;
 
 /**
  * This class animates a {@link AnimationFigure}. The figure moves form its current position along a path
  * to a specified room.
  * @author Marc Fernandes
  *
  */
 public class WalkToAnimator extends Observable implements Runnable, Animator {
 	/**
 	 * The {@link NodeMap} where the animation takes place.
 	 */
 	NodeMap map;
 	/**
 	 * The figure to animate.
 	 */
 	AnimationFigure animationFigure;
 	
 	NodeFigure start_node;
 	
 	/**
 	 * The room where the figure should go. this is a temporal position at a moment during the animation
 	 * because along a path a figure can visit a lot of rooms.
 	 * 
 	 */
 	NodeFigure end_node;
 	/**
 	 * This is the room where we really want to go. So the last room in this animation.
 	 */
 	NodeFigure total_end;
 	/**
 	 * Indicates if animation started for the first time for one direct path.
 	 * This means: if the animation has more then one direct paths from one room to another
 	 * then for each inner animation init must be set to true.
 	 */
 	boolean init;
 	/**
 	 * The parent of the animated figue. We need that to repaint the parent.
 	 */
 	IFigure parent       = null;
 	/**
 	 * We use a {@link ManhattanConnectionRouter}. So there can be rectangular branches
 	 * along a path between two rooms. To animate the figure along this paths we separate
 	 * all horizontal and vertical lines as segments to change direction (e.g. left then up). 
 	 */
 	PointList segments = null;
 	/**
 	 * The run_count for each point inside a segment. So this can be from 0 to segment.size()
 	 */
 	int run_count        = 0;
 	/**
 	 * The actual segment along the path which is animated in the moment.
 	 */
 	int segment_nr       = 0;	
 	
 	/**
 	 * Indicates if the animation is stopped.
 	 */
 	boolean stopped      = false;
 	
 	/**
 	 * Indicates that a stopaction has started. Problem is that we nust wait till it is really stopped!
 	 */
 	boolean stop=false;
 	
 	/**
 	 * A list with all rooms where we want to go.
 	 */
 	Iterator<NodeFigure> walking_path=null;	
 	/**
 	 * indicates if the animation is finished.
 	 */
 	boolean finished     = false;
 	int trainSpeed = 0;
 	
 	/**
 	 * Constructor. creates the Animation.
 	 * @param {@link NodeMap} map - The NodeMap where the animation takes place. 
 	 * @param {@link AnimationFigure} figure - the figure we want to animate itself. 
 	 * @param {@link NodeFigure} end_node - The node where we want to go to.
 	 */
 	public WalkToAnimator(NodeMap map, AnimationFigure figure, NodeFigure end_node) {
 		this.animationFigure=figure;
 		TrainFigure tf = (TrainFigure)animationFigure;
 		this.init=true;
 		this.total_end=end_node;
 		this.map=map;	
 		this.trainSpeed = tf.getSpeed(tf.getFigureId());
 	}
 	
 	/**
 	 * Helper method to get a path from the figures ({@link AnimationFigure}) current position to the specified node.
 	 * @param {@link NodeFigure} end_node - The node we want to have a direct path to. 
 	 * @return {@link PathFigure}.
 	 * @throws PathNotFoundException if no path can be found.
 	 */
 	private PathFigure getPathTo(NodeFigure end_node) {
 	  ArrayList<PathFigure> edges_end = map.getPaths().get(end_node);
 	  
 	  if(animationFigure.getPath() != null) {
 		 for(int i=0; i< edges_end.size();i++) {
 			 if(edges_end.get(i).equals(animationFigure.getPath())) {
 				 return animationFigure.getPath(); 
 			 }
 		 }
 	     return null;
 	  }
 	  	
 	  NodeFigure start_node = this.animationFigure.getNodeFigure();
 	  ArrayList<PathFigure> edges_start = map.getPaths().get(start_node);
 	  
 	  if(start_node.equals(end_node)) {		  
 		  return null;
 	  }
 	  for(int i=0; i<edges_start.size(); i++) {
 		  for(int j=0; j<edges_end.size();j++) {
 			  if(edges_start.get(i).equals(edges_end.get(j))) {
 				  return edges_start.get(i);
 			  }
 		  }
 	  }
 	  return null;
 	}
 	
 	/**
 	 * Indicates if the animation is finished.
 	 */
 	public boolean isFinished() {
 		return this.finished;
 	}
 	
 	/**
 	 * Indicates if the current animation is stopped succesfully.
 	 */
 	public boolean isStopped() {
 		return this.stopped;
 	}
 	
 	/**
 	 * runs the animation and restarts itself as long as the animation is not finished.
 	 * After it is finished the observer listening for the finished event is notified.
 	 * Don't use this method to start the animation!
 	 */
 	public void run() {
 		
 		
 		PathFigure path = null;
 		if(init){			
 			start_node = animationFigure.getNodeFigure();			
 			if(walking_path==null) {
 			  //Problem: When room is not set for animationFigure...
 			  if(start_node==null) {
 			 	start_node = animationFigure.getDirection_to_node();
 			 	path=getPathTo(start_node);
 			  }
 			 
 			  //Set all Nodes on the way to be blocked
 			 
 			  Iterator<NodeFigure> nodesToBlock = Utility.getOptimizedRoute(map, start_node, total_end);
 			  List<NodeFigure> nodesAlreadyBlocked = new Vector<NodeFigure>();
 			  List<NodeFigure> walkToNodes = new Vector<NodeFigure>();
 			  while (nodesToBlock.hasNext()) {
 				NodeFigure n = nodesToBlock.next();
 				walkToNodes.add(n);
 				State stateToBlock = State.statemap.get(n.getName());
 				if(stateToBlock.geState() == State.UNBLOCKED){
 					stateToBlock.setState(State.BLOCKED);
 					nodesAlreadyBlocked.add(n);
 				}else{
 					TrainFigure tf = (TrainFigure)animationFigure;
 					
 					
 					//unblock nodes again and clear animations of trainfigure then call walktomethod again					
 					tf.stopAnimation();
 					tf.clearAnimations();
 					
 					for(NodeFigure nodeToUnblock : nodesAlreadyBlocked){
 						State.statemap.get(nodeToUnblock.getName()).setState(State.UNBLOCKED);
 					}
 					
 					AnimationEvents.walkTo(tf, total_end, map);
 					
 					return;
 				}
 			  }
 			  
 			  walking_path = walkToNodes.iterator();
 			}
 			
 			if(!walking_path.hasNext()) {
 				this.finished=true;
 				//Inform the observer that animation is finished.
 				//this is needed, so that we can e.g. start a new animation after this one is finished.
 				animationFigure.setNode(end_node);
 				
 				//notify Listeners
 				animationFigure.notifyAnimationListener(new AnimationFinishedEvent(animationFigure, AnimationFinishedEvent.MOVE_FINISHED));
 				
 				//Notify Observers
 				setChanged();				
 				notifyObservers(animationFigure);
 			
 				return;
 			}
 			
 			if(path!=null) {
 			  end_node=start_node;	
 			} else {
 			  State.statemap.get(start_node.getName()).setState(State.UNBLOCKED);
 			  end_node = walking_path.next();			
 			  path = getPathTo(end_node);
 			}
 			
 			segments=Utils.getSegments(path, end_node, animationFigure);
 			if(segments.size()==0) {
 				this.finished=true;
 				//Inform the observer that animation is finished.
 				//this is needed, so that we can e.g. start a new animation after this one is finished.
 				animationFigure.setNode(end_node);
 				
 				//notify Listeners
 				animationFigure.notifyAnimationListener(new AnimationFinishedEvent(animationFigure, AnimationFinishedEvent.MOVE_FINISHED));
 				
 				//Notify Observers
 				setChanged();				
 				notifyObservers(animationFigure);
 				return;
 			}
 			animationFigure.setDirection_to_node(end_node);
 			animationFigure.setPath(path);
 			
 			parent=animationFigure.getParent();
 			init=false;		
 		}
 		
 		animationFigure.setLocation(segments.getPoint(run_count));
 		if(parent!= null){
 			parent.repaint();
 		}
 		
 		int pathSpeed = getValueSpeed(animationFigure.getPath().getPathSpeed());
		System.out.println(getMaxDrivingSpeed(pathSpeed, trainSpeed));
 		run_count+= getMaxDrivingSpeed(pathSpeed, trainSpeed) + getControllerSpeed(AnimationControllerCanvas.getSimulationSpeed());
 		if(run_count>=segments.size()) {
 			init=true;
 			run_count=0;
 			animationFigure.setNode(end_node);
 		}	
 		
 		if(stop) { 
 			//Stop can cause problems when we restart it later again and the figur is not in a room and not 
 			//on a direct path: means it is between a path and a room...
 			if(this.animationFigure.getNodeFigure() == null) {
 				NodeFigure next_node = this.animationFigure.getDirection_to_node();
 				if(next_node.intersects(animationFigure.getBounds())) {
 					this.animationFigure.setNode(next_node);
 				}
 			}	
 			this.stopped=true;
 			return;
 		}
 		map.getDisplay().timerExec(0, this);			
 	}
 	private int getValueSpeed(int pathSpeed) {
 		if(pathSpeed == 100){
 			return 1;
 		}else if(pathSpeed == 150){
 			return 2;
 		}else if(pathSpeed == 200){
 			return 3;
 		}else if(pathSpeed == 250){
 			return 4;
 		}else{
 			return 5;
 		}
 		
 	}
 
 	private int getControllerSpeed(int i) {
 		
 		return (i/100);
 	}
 
 	private int getMaxDrivingSpeed(int pathSpeed, int trainSpeed2) {
 		if (pathSpeed < trainSpeed2){
 			return pathSpeed;
 		}
 		return trainSpeed;
 	}
 
 	
 
 
 	/**
 	 * Starts the animation. Can also be used to restart the animation if it was stopped.
 	 * To start(run) the animation only use this method!
 	 */
 	public void start() {
 		this.stop=false;
 		//notify Listeners
 		animationFigure.notifyAnimationListener(new AnimationStartedEvent(animationFigure, AnimationStartedEvent.MOVE_STARTED));
 		map.getDisplay().timerExec(0, this);
 	}
 	/**
 	 * Stops the animation. The animation can be restarted with @see {@link #start()}
 	 */
 	public void stop() {
 		this.stop=true;				
 	}	
 }
 
 
 
