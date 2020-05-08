 package se.chalmers.kangaroo.model.iobject;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.GameMap;
 import se.chalmers.kangaroo.model.InteractiveTile;
 import se.chalmers.kangaroo.model.utils.Position;
 import se.chalmers.kangaroo.utils.Sound2;
 
 /**
  * 
  * A class for the interactive object Red/Blue-button. If the outer part of the
  * button is red, all red tiles will be visible and collidable, while all blue
  * tiles are not visible and not collidable. If Kangaroo collides with the
  * button, it will become blue and all the blue tiles will be visible and
  * collidable instead.
  * 
  * @author pavlov
  * 
  */
 public class RedBlueButton implements InteractiveObject {
 	private GameMap gameMap;
 	private Position pos;
 	private boolean sleep;
 	private int id;
 	private Sound2 s;
 
 	public RedBlueButton(Position p, int id, GameMap gameMap) {
 		this.gameMap = gameMap;
 		this.pos = p;
 		this.sleep = false;
 		this.id = id;
 		this.s = Sound2.getInstance();
 	}
 
 	@Override
 	public void onCollision() {
 		if (!sleep) {
 			if (getId() % 2 == 0) {
 				s.playSfx("red");
 			} else {
 				s.playSfx("blue");
 			}
 			int x = gameMap.getTileWidth();
 			int y = gameMap.getTileHeight();
 			for (int i = 0; i < y; i++) {
 				for (int j = 0; j < x; j++) {
 					if (Constants.INTERACTIVE_TILES_REDBLUE.contains(" "
 							+ gameMap.getTile(j, i).getId() + " ")) {
 						((InteractiveTile) gameMap.getTile(j, i)).onTrigger();
 					}
 						InteractiveObject iobj = gameMap.getIObjectAt(j, i);
 						if(iobj != null)
							iobj.changeId();
 					
 				}
 			}
 			sleep = true;
 			new Thread() {
 				@Override
 				public void run() {
 					try {
 						sleep(600);
 						sleep = false;
 					} catch (InterruptedException e) {
 
 					}
 				};
 			}.start();
 		}
 	}
 
 	@Override
 	public int getChangedId(int currentId) {
 		if (currentId % 2 == 0) {
 			return currentId - 1;
 		} else {
 			return currentId + 1;
 		}
 	}
 
 	@Override
 	public Position getPosition() {
 		return pos;
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 	
 	@Override
 	public void changeId(){
 		id = id == 71 ? id + 1 : id - 1;
 	}
 
 }
