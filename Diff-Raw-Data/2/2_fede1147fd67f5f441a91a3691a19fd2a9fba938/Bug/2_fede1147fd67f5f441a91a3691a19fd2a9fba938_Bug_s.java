 package com.bitsend.evogene.agents;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector3;
 import com.bitsend.evogene.Direction;
 import com.bitsend.evogene.Environment;
 import com.bitsend.evogene.Place;
 import com.bitsend.evogene.World;
 
 public class Bug extends Agent {
 	public float speed = 15; // moves per second
 	protected float totalDelta = 0f;
 	protected float waitTime = 0f;
 	int energyUsedPerTurn = 1;
 	int energy = 20;
 	
 	public Bug(float x, float y, float z, int speed) {
 		super(x, y, z);
 		create();
 	}
 
 	@Override
 	public void update(World world, float delta) {
 		totalDelta += delta;
 		Gdx.app.log("Bug render", "this: " + this.toString() );
 		energy -= energyUsedPerTurn;
 		
 		if (totalDelta > waitTime) {
 			boolean searching = true;
 			while (searching) {
 			
 				Direction dir = Direction.RandomDirection();
 				int nextX = (int) position.x + dir.getXDelta();
 				int nextY = (int) position.y + dir.getYDelta();
 				if (world.inWorld(nextX, nextY)) {
 					Agent agent = world.getContents(nextX, nextY);
					if (agent instanceof Agent) {
 						eat(world,agent);
 						moveTo(nextX, nextY);
 						searching = false;
 					}
 					else if (agent==null) {
 						moveTo(nextX, nextY);
 						searching = false;
 					}
 				}
 				/*
 				HashMap<Direction, Agent> surroundings = world.getSurroundings((int)position.x, (int)position.y, 1);
 				Direction dir = bestMove(surroundings);
 				int nextX = (int) position.x + dir.getXDelta();
 				int nextY = (int) position.y + dir.getYDelta();
 				if (world.inWorld(nextX, nextY)
 						&& world.getContents(nextX, nextY) == null) {
 					moveTo(nextX, nextY);
 					searching = false;
 				}
 				*/
 			}
 			
 			totalDelta = 0f;
 		}
 	}
 
 	public void die(World world) {
 		Gdx.app.log("Bug.die", "");
 		world.resetContents((int)this.position.x, (int)this.position.y);
 	}
 	
 	public void eat(World world, Agent agent) {
 		energy += agent.energy;
 		world.resetContents((int)agent.position.x, (int)agent.position.y);
 		
 	}
 	protected Direction bestMove(HashMap<Direction, Agent> surroundings) {
 		Direction dir = Direction.E;
 		ArrayList<Direction> food = new ArrayList<Direction>();
 		ArrayList<Direction> empty = new ArrayList<Direction>();
 		ArrayList<Direction> predator = new ArrayList<Direction>();
 		for(Direction d : Direction.values()) {
 			Agent agent = surroundings.get(dir);
 			if (agent==null) {
 				empty.add(d);
 			}
 			else if(agent instanceof Bug) {
 				predator.add(d);
 			}
 			else if(agent instanceof Agent) {
 				food.add(d);
 			}
 			
 			
 		}
 		if (food.get(0) != null) {
 			dir = food.get(0);
 		}
 		else if (empty.size() > 0) {
 			int i = (int)Math.floor(Math.random() * empty.size());
 			dir = empty.get(i);
 		}
 			
 		return dir;
 	}
 	@Override
 	public void create() {
 		super.create();
 		waitTime = 1/speed;
 		sprite = new Sprite(com.bitsend.evogene.Assets.cell);
 		sprite.setPosition(0f, 0f);
 	}
 	
 	@Override
 	public String toString() {
 		return  "How Many: " + Bug.HOW_MANY +  " Type: " + this.getClass().getName() + " Energy: " + energy;
 	}
 	
 	@Override
 	public void act(Environment environment) {
 		
 		for(Direction dir : Direction.values()) {
 			Place place = environment.get(dir);
 			if (place.contents != null) {
 				Agent thing = place.contents;
 				if (thing instanceof Food) {
 					eat(thing);
 				}
 				else if(thing instanceof Bug) {
 					continue;
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void eat(Agent agent) {
 		this.energy += agent.energy;
 		this.position.x = agent.position.x;
 		this.position.y = agent.position.y;
 		agent.kill(agent);
 		
 	}
 
 	@Override
 	public void kill(Agent agent) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Agent[] reproduce(Agent mate) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Vector3 move(int x, int y) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
