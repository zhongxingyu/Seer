 package com.codefest2013.game.scenes.objects;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.CardinalSplineMoveModifier;
 import org.andengine.entity.modifier.CardinalSplineMoveModifier.CardinalSplineMoveModifierConfig;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.andengine.entity.modifier.PathModifier;
 import org.andengine.entity.modifier.PathModifier.IPathModifierListener;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.util.algorithm.path.Path;
 import org.andengine.util.color.Color;
 
 import com.codefest2013.game.MainActivity;
 
 public class Squirrel extends Entity {
 	private WayPoint wps[];
 	private ArrayList<Integer> throwablePoints;
 	private ArrayList<WayPoint> currentWay;
 	private int current;
 	//private int goal;
 	private Random r;
 	Rectangle rect;
 	IPathModifierListener modifierListener;
 	
 	public Squirrel(WayPoint[] wayPointsArray, int startIndex){
 		r = new Random();
 		wps = wayPointsArray;
 		current = startIndex;
 		throwablePoints = new ArrayList<Integer>();
 		currentWay = new ArrayList<WayPoint>();
 		for(int i=0; i<wps.length; i++)
 		{
 			if (wps[i].isThrowable)
 			{
 				throwablePoints.add(i);
 			}
 		}
 		modifierListener = new IPathModifierListener() {
 			
 			@Override
 			public void onPathWaypointStarted(PathModifier pPathModifier, IEntity pEntity, int index) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onPathWaypointFinished(PathModifier pPathModifier, IEntity pEntity, int index) {
 				pEntity.setColor(Color.GREEN);
 				
 			}
 			
 			@Override
 			public void onPathStarted(PathModifier pPathModifier, IEntity pEntity) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onPathFinished(PathModifier pPathModifier, IEntity pEntity) {
				pEntity.clearEntityModifiers();
				currentWay.clear();
 				setNextGoal();
 			}
 		};
 		
 		
 		
 		
 		rect = new Rectangle(wps[0].x, wps[0].y, 20, 20, MainActivity.getInstance().getVertexBufferObjectManager());
 		rect.setColor(Color.YELLOW);
 		attachChild(rect);
 	}
 	public Squirrel(WayPoint[] wayPointsArray){
 		this(wayPointsArray, 0);
 	}
 		
 	public void start(){
 		setNextGoal();
 	} 
 	
 	public void stop(){
 		// TODO: implement
 	}
 	
 	private void setNextGoal(){
 		int randIndex = throwablePoints.get(r.nextInt(throwablePoints.size()));
 		if (current > randIndex){
 			do {
 				current--;
 				currentWay.add(wps[current]);
 			} while (current > randIndex);
 		} else if (current < randIndex) {
 			do {
 				current++;
 				currentWay.add(wps[current]);
 			} while (current < randIndex);
 		} else {
 			setNextGoal(); // :>
 		}
 		org.andengine.entity.modifier.PathModifier.Path path = new org.andengine.entity.modifier.PathModifier.Path(currentWay.size());
 		for (int i = 0; i < currentWay.size(); i++) {
 			path.to(currentWay.get(i).x, currentWay.get(i).y);
 		}
 		PathModifier pathModifier = new PathModifier(5, path, modifierListener);
 		rect.registerEntityModifier(pathModifier);
 	}
 	
 	
 }
