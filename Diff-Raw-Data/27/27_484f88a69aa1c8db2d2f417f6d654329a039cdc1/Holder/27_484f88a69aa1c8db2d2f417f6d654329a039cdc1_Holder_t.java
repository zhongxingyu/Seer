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
	private String m_openPath;
	private String m_closedPath;
 	
	private Image m_openSprite;
	private Image m_closedSprite;
	
	/**
	 * Takes in a name, and paths to open and closed images.
	 * @param name
	 * @param openImgPath
	 * @param closedImgPath
	 */
	public Holder(String name, String openImgPath, String closedImgPath) {
 		super(name);
 		try {
			this.m_openPath = openImgPath;
			this.m_openSprite = new Image(openImgPath);
			this.m_closedPath = closedImgPath;
			this.m_closedSprite = new Image(closedImgPath);
			this.setSprite(m_openSprite);
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
				this.setSprite(m_closedSprite);
 			}
 		} else {
 			p.addToInventory(m_itemHeld);
 			m_itemHeld = null;
			this.setSprite(m_openSprite);
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
