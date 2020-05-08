 package com.thisisdinosaur.protosaurus.shared;
 import java.util.List;
 
 import com.thisisdinosaur.protosaurus.client.GameEntity;
 import com.thisisdinosaur.protosaurus.client.MapLogic;
 
 public class ResourceNodeEntity extends GameEntity
 {	
 	private MapLogic ml;
 	private List<Dinosaur> playerDinosaurs;
 	private Dinosaur owner;
 	
 	public ResourceNodeEntity(MapLogic ml)
 	{
 		this.ml = ml;
 		playerDinosaurs = ml.getPlayerControlledDinosaurs();
 		owner = null;
 	}
 	
 	public void tick(int delta)
 	{
//		System.out.println(ml.getPlayerControlledDinosaurs().isEmpty());
 		//The getPlayerControlledDinosaurs() method currently returns an empty list, until it's fixed this code will do nothing
 		for(Dinosaur d : playerDinosaurs)
 		{
 			float dist = Maths.getDistance(this.getX(), this.getY(), d.getX(), d.getY());
 			if(dist < 200)
 			{
 				owner = d;
 			}
 		}
 	}
 
 	public Dinosaur getOwner()
 	{
 		return owner;
 	}
 }
