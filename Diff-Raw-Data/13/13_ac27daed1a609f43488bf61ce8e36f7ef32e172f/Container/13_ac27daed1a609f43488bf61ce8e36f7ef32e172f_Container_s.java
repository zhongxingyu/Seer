 package com.pbst.gameobjects;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.physics.box2d.World;
 import com.pbst.pibsty.R;
 import com.pbst.pibsty.TetrisLevel;
 import com.pbst.pibsty.size.Pixels;
 
 public class Container extends GameObject
 {
 	private static final int numRows = 12;
 	private static final int numCols = 12;
 	World world_;
 	LineSensor[][] sensors = new LineSensor[numRows][numCols];//	Grid resolution of the various sensors
 	
 	public Container(Pixels x, Pixels y, ArrayList<Sprite> slist, World world)
 	{
 		world_ = world;
 		for (int i = 0; i < numRows; ++i)
 		{
 			for (int j = 0; j < numCols; ++j)
 			{
 				LineSensor sensor = new LineSensor( new Pixels(j*(420F/numCols) + x.value()), new Pixels(i*(420F/numRows) + y.value()), world, (i*numRows) + j);
 				sensors[i][j] = sensor;
 			}
 		}
 	}
 	
 	public void Update()
 	{
 		//	Create a list of destroyable objects - get them from each sensor in this row
 		ArrayList<GameObject> destroyableObjects = new ArrayList<GameObject>();
 		//	for each row
 		for (int i = 0; i < numRows; ++i)
 		{
 			int collisions = 0;	//	number of collisions in this row
 			//for each sensor in the row
 			for (int j = 0; j < numCols; ++j)
 			{
 				if (sensors[i][j].touchingObject != null) collisions++;
 			}
 			
 			//	If there are enough collisions in this row (as a percentage of the number of sensors
 			if (collisions >= (numCols * (100/100F)))
 			{
 				
 				//	Add each object from the sensors to the destroyable list
 				for (int j = 0; j < numCols ; ++j)
 				{
 						destroyableObjects.add(sensors[i][j].touchingObject);
 				}
 				
 				TetrisLevel.score += 100;
 			}
 		}
 		
 		//	Clear all sensors that have one of these objects touching them
 		for (int i = 0; i < numRows; i++)
 		{
 			for (int j = 0; j < numCols; ++j)
 			{
 				for (GameObject g: destroyableObjects)
 				{
 					if (sensors[i][j].touchingObject == g)
 					{
 						sensors[i][j].touchingObject = null;
 						sensors[i][j].setVisuallyEmpty();
 					}
 				}
 			}
 		}
 		
 		//	Delete destroyable objects list
 		for (GameObject g: destroyableObjects)
 		{
 			// destroy each gameobject
			TetrisLevel.gameObjects_.remove(g);
			TetrisLevel.spriteList_.remove(g._sprite);
			g.destroy();
			world_.destroyBody(g._body);
 		}
 		
 		destroyableObjects.clear();
 	}
 	
 	@Override
 	public void beginCollision(GameObject collider) {
 		// TODO Auto-generated method stub
 		super.beginCollision(collider);
 	}
 	
 	@Override
 	public void endCollision(GameObject collider) {
 		// TODO Auto-generated method stub
 		super.endCollision(collider);
 	}
 }
