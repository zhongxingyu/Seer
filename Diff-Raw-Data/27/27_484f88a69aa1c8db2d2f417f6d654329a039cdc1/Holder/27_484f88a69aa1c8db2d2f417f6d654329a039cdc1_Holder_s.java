 package game.interactables;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import game.Collectable;
 import game.GameObject;
 import game.gameplayStates.GamePlayState;
 import game.player.Player;
 
 /**
  * Holders are objects that can hold and release collectables.
  *
  */
 public class Holder extends GameObject implements Interactable {
 
 	private Collectable m_itemHeld;
	private String m_imgPath;
 	
	public Holder(String name, String imgPath) {
 		super(name);
 		try {
			this.m_imgPath = imgPath;
			this.setSprite(new Image(imgPath));
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * If the holder has an item, the item is given to the player.
 	 * Otherwise, if the player has a collectible equipped, this holder
 	 * takes the collectible from the player.
 	 */
 	@Override
 	public Interactable fireAction(GamePlayState state, Player p) {
 		if(m_itemHeld==null) {
 			if(p.getUsing()!=null) {
 				m_itemHeld = p.getUsing();
 				p.getInventory().removeItem(p.getUsing().getName());
 			}
 		} else {
 			p.addToInventory(m_itemHeld);
 			m_itemHeld = null;
 		}
 		return this;
 	}
 	
 	/**
 	 * Returns the holder's item held.
 	 * @return
 	 */
 	public Collectable getItemHeld() {
 		return m_itemHeld;
 	}
 
 	@Override
 	public Types getType() {
 		return GameObject.Types.HOLDER;
 	}
 
 }
