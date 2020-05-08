 package clients;
 
 import java.util.Collection;
 import java.util.Hashtable;
 
 import jig.engine.physics.vpe.VanillaSphere;
 import jig.engine.util.Vector2D;
 
 import net.NetStateManager;
 import net.NetObject;
 
 public class ClientGameState {
 	
 	private Hashtable<Integer, VanillaSphere> spriteList = new Hashtable<Integer, VanillaSphere>();
 	
 	public ClientGameState () { }
 	
 	public void init(NetStateManager gameState) {
 		for(NetObject no : gameState.getState().getNetObjects())
 			init(no);
 	}
 	
 	public void init(NetObject no) {
 		if (spriteList.containsKey(no.getId()))
 			return;
 		
 		switch(no.getType()) {
 		case NetObject.PLAYER:
 			spriteList.put(no.getId(), new SpriteObject("player"));
 			break;
 		case NetObject.PLATFORM:
 			spriteList.put(no.getId(), new SpriteObject("platform"));
 			break;
 		case NetObject.SMALLBOX:
 			spriteList.put(no.getId(), new SpriteObject("smallbox"));
 			break;
 		case NetObject.GROUND:
 			spriteList.put(no.getId(), new SpriteObject("ground"));
 			break;
 		}
 		
 	}
 	
 	public void sync(NetStateManager gameState) {
 		for (NetObject no : gameState.getState().getNetObjects()) {
 			if (spriteList.containsKey(no.getId())) {
 				
 				// fixing the offset, because in jig, rectangle extending VanillaShere is just a giant sphere
 				SpriteObject s = (SpriteObject)spriteList.get(no.getId());
 				Vector2D p = no.getPosition();
 				Vector2D newPos = new Vector2D(p.getX()-(s.getRadius()-s.getImgWidth()/2), 
 											   p.getY()-(s.getRadius()-s.getImgHeight()/2));
 				spriteList.get(no.getId()).setPosition(newPos);
				
 				spriteList.get(no.getId()).setVelocity(no.getVelocity());
 				spriteList.get(no.getId()).setRotation(no.getRotation());
 			} else
 				init(no);
 		}
 	}
 	
 	public Collection<VanillaSphere> getSprites() { 
 		return spriteList.values(); 
 	} 
 }
