 package game;
 
 import interaction.Input;
 import interaction.KeyBindings;
 
 import java.util.ArrayList;
 
 import main.Battlepath;
 
 
 import collision.CollisionSystem;
 import collision.MovementSystem;
 
 import util.Rectangle2D;
 import util.SafeList;
 import util.Vector2D;
 
 import engine.Field;
 import engine.GlobalInfo;
 import engine.Pathplanner;
 import entities.CollisionEntity;
 import entities.Entity;
 import entities.Projectile;
 import entities.Unit;
 import fx.EffectsSystem;
 
 public class Game {
 	
 	public Field field;
 	public Pathplanner pathPlanner;
 	public CollisionSystem collisionSystem;
 	public MovementSystem movementSystem;
 	public EffectsSystem particleSystem;
 	public Input input;
 	//private Unit u;
 	public SafeList<Entity> entities = new SafeList<Entity>();
 	public ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
 	public Rectangle2D selectionRect;
	public boolean found = false;
 	
 	public View view;
 	public GameMode mode;
 	
 	boolean[] lastMouseState = new boolean[3];
 	double lastShot = 0;
 	
 	public double dt;
 	
 	public Game(Vector2D startpos) {
 		movementSystem = new MovementSystem(this);
 		particleSystem = new EffectsSystem();
 		entities.add(new Unit(startpos, this));
 	}
 	
 	public void step(double dt) {
 		this.dt = dt;
 		processInput(dt);
 		
 		
 		for(Entity e : entities) {
 			e.process(dt);
			
			
			
			
 			if(e instanceof Unit) {
				if(selectionRect != null && !(found)) {
 					if(selectionRect.inside(e.pos)) {
 						if(!((Unit) e).isSelected) {
 							selectedUnits.add((Unit)e);
 							((Unit) e).isSelected = true;
 						}
 					}
 					else {
 						if(((Unit) e).isSelected) {
 							selectedUnits.remove((Unit)e);
 							((Unit) e).isSelected = false;
 						}
 					}
 				}
 			}
 		}
 		
 		movementSystem.process(getCollisionEntities());
 		particleSystem.process(dt);
 		
 		entities.applyChanges();
 		view.process(dt);
 	}
 	
 	public void setMode(GameMode gm) {
 		switch(gm) {
 		case ACTION:
 			if(selectedUnits.size() != 0) {
 				mode = gm;
 				view.follow(selectedUnits.get(0));
 			}
 			break;
 		case STRATEGY:
 			mode = gm;
 			view.unfollow();
 			if(selectedUnits.size() != 0){
 				selectedUnits.get(0).velocity = new Vector2D(0,0);
 			}
 			break;
 		}
 	}
 	
 	public ArrayList<CollisionEntity> getCollisionEntities() {
 		ArrayList<CollisionEntity> es = new ArrayList<CollisionEntity>();
 		for(Entity e : entities) {
 			if(e instanceof CollisionEntity) {
 				es.add((CollisionEntity) e);
 			}
 		}
 		return es;
 	}
 	
 	public void toggleMode() {
 		if(mode == GameMode.ACTION) {
 			setMode(GameMode.STRATEGY);
 		}
 		else if(mode == GameMode.STRATEGY) {
 			setMode(GameMode.ACTION);
 		}
 	}
 	
 	public void processInput(double dt) {
 		//Mouse
 		
 		
 		if(mode == GameMode.STRATEGY) {
 			if(input.mouseButtonPressed[0]  && !lastMouseState[0]) {
				found = false;
 				for(Entity e : entities) {
					if(!(e instanceof Unit)) continue;
					Unit u = (Unit)e;
					if(input.getCursorPos().distance(e.pos) < e.getRadius()) {
						if(!u.isSelected) {
							selectedUnits.add((Unit)e);
							((Unit) e).isSelected = true;
						}
 						found = true;
 					}
 				}
 				
 				selectionRect = new Rectangle2D(input.getCursorPos(), input.getCursorPos());
 			}
 			
 			if(!input.mouseButtonPressed[0]) {
 				selectionRect = null;
 			}
 			
 			if(input.mouseButtonPressed[0]  && lastMouseState[0]) {
 				selectionRect.bottomright = input.getCursorPos();
 			}
 			
 			if(input.mouseButtonPressed[2] && !lastMouseState[0] && selectedUnits.size() != 0) {
 				for(Unit u : selectedUnits) {
 					u.moveTo(input.getCursorPos());
 				}
 			}
 		}
 		
 		if(mode == GameMode.ACTION) {
 			if((input.mouseButtonPressed[0] && selectedUnits.size() != 0 && GlobalInfo.time - lastShot > 0.3)) {
 				selectedUnits.get(0).shoot(input.getCursorPos().subtract(selectedUnits.get(0).pos).normalize());
 				lastShot = GlobalInfo.time;
 			}
 		}
 		
 		System.arraycopy(input.mouseButtonPressed, 0, lastMouseState, 0, input.mouseButtonPressed.length);
 		
 		//Keyboard part one (input.isPressed)
 		
 		if(selectedUnits.size() != 0 & mode == GameMode.ACTION) {
 			if(input.isPressed(KeyBindings.MOVE_LEFT)) selectedUnits.get(0).velocity.x = 1;
 			else if(input.isPressed(KeyBindings.MOVE_RIGHT)) selectedUnits.get(0).velocity.x = -1;
 			else selectedUnits.get(0).velocity.x = 0;
 			
 			if(input.isPressed(KeyBindings.MOVE_DOWN)) selectedUnits.get(0).velocity.y = -1;
 			else if(input.isPressed(KeyBindings.MOVE_UP)) selectedUnits.get(0).velocity.y = 1;
 			else selectedUnits.get(0).velocity.y = 0;
 			
 			if(selectedUnits.get(0).velocity.length() > 0)
 				selectedUnits.get(0).velocity = selectedUnits.get(0).velocity.normalize().scalar(10);
 		}
 		
 		if(mode == GameMode.STRATEGY) {
 			if(input.isPressed(KeyBindings.SCROLL_LEFT)) view.velocity.x = -20;
 			else if(input.isPressed(KeyBindings.SCROLL_RIGHT)) view.velocity.x = 20;
 			else view.velocity.x = 0;
 			
 			if(input.isPressed(KeyBindings.SCROLL_UP)) view.velocity.y = 20;
 			else if(input.isPressed(KeyBindings.SCROLL_DOWN)) view.velocity.y = -20;
 			else view.velocity.y = 0;
 		}
 		
 		//Keyboard part two (input.getKeyBuffer)
 		
 		for(int key : input.getKeyBuffer()) {
 			switch(key) {
 				case KeyBindings.ZOOM_IN:
 					view.setZoom(view.zoom+0.1);
 					break;
 				case KeyBindings.ZOOM_OUT:
 					view.setZoom(view.zoom-0.1);
 					break;
 				/*DEBUGGING KEYS*/
 				case 't':
 					toggleMode();
 					break;
 				case 'r':
 					entities.add(new Unit(Battlepath.findStartPos(field), this));
 					break;
 			}
 		}
 		
 	}
 	
 	public void emitShot(Vector2D start, Vector2D direction) {
 		Projectile p = new Projectile(start, direction, this);
 		entities.add(p);
 	}
 	
 }
