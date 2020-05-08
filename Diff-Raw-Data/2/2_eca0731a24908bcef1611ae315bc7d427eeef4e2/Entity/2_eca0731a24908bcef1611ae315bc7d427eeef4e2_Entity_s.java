 /**
  * 
  */
 package org.ggj.supernatural.RenderObjects.Movable;
 
 import java.util.Iterator;
 
 import org.ggj.supernatural.RenderObjects.WorldObject;
 import org.ggj.supernatural.WorldMap.WorldMap;
 import org.newdawn.slick.Image;
 
 /**
  * @author Colby Dame
  *
  */
public class Entity extends WorldObject implements Cloneable {
 	
 	public static enum SpriteDirection { North, East, South, West }
 	protected SpriteDirection Orientation;
 	protected float Speed;
 	protected WorldMap Map;
 
 	public SpriteDirection getOrientation() {
 		return Orientation;
 	}
 
 	public void setOrientation(SpriteDirection orientation) {
 		Orientation = orientation;
 	}
 
 	/**
 	 * @param other
 	 * @param X
 	 * @param Y
 	 * @param Passable
 	 */
 	public Entity(Image other, float X, float Y, boolean Passable, WorldMap Map) {
 		super(other, X, Y, Passable);
 		this.Map = Map;
 		Speed = 5.0f;
 		Orientation = SpriteDirection.South;
 		// TODO Auto-generated constructor stub
 	}
 	
 	public boolean CollidesWith(WorldObject Compare){
 		if (X + 32 > Compare.GetX())
 			if(X < Compare.GetX())
 				if (Y + 32 > Compare.GetY())
 					if(Y < Compare.GetY())
 						return true;
 		return false;
 	}
 	
 	public boolean HasMovementCollisions(float Movement, Entity.SpriteDirection Direction){
 		
 		Float TempX = X;
 		Float TempY = Y;
 		
 		switch(Direction){
 		case North: SetY(GetY() - Movement);
 			break;
 		case South: SetY(GetY() + Movement);
 			break;
 		case East: SetX(GetX() + Movement);
 			break;
 		case West: SetX(GetX() - Movement);
 			break;
 		}
 		
 		Iterator<WorldObject> Itr = Map.iterator();
 		while(Itr.hasNext()){
 			WorldObject Temp = Itr.next();
 			if(Temp.IsPassable())
 				continue;
 			else if(CollidesWith(Temp)){
 				X = TempX;
 				Y = TempY;
 				return true;					
 			}
 			
 		}
 		X = TempX;
 		Y = TempY;
 		return false;
 	}
 	
 }
