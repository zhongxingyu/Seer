 /**
  * @author Glenn Rivkees (grivkees)
  */
 
 package core.playfield;
 
 
 import java.awt.Graphics2D;
 
 
 
 
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.background.ParallaxBackground;
 
 import core.characters.*;
import core.characters.Character;
 import core.collision.PlayerCollectibleItemCollision;
 import core.collision.SideScrollerBoundsCollision;
 import core.items.CollectibleItem;
 import core.tiles.Tile;
 
 public class AdvancedPlayField extends PlayField {
 	
 	private GameScroller gamescroller;
 	
     private SpriteGroup Players;
     private SpriteGroup Characters;
     private SpriteGroup Setting;
     private SpriteGroup Items;
     
     /*
      * Initialize PlayField, Background, and common SpriteGroups
      */
     public AdvancedPlayField (int width, int height) {
     	super(new ParallaxBackground (
 				new Background[] { new Background(width, height), new Background() } 
 				) 
 		);
     	Players = this.addGroup(new SpriteGroup("Player Group"));
     	Characters = this.addGroup(new SpriteGroup("Character Group"));
     	Setting = this.addGroup(new SpriteGroup("Setting Group"));
     	Items = this.addGroup(new SpriteGroup("Setting Group"));
     	
     	// Add Bounds Collsion
     	this.addCollisionGroup(this.getPlayers(),
 		        null, new SideScrollerBoundsCollision(this.getBackground()));
     }
 	
 	/*
 	 * GameScroller Methods
 	 */
 	
 	public void setGameScroller (GameScroller gs) {
 		gamescroller = gs;
 		gs.setBackground(this.getBackground());
 		gs.setPlayers(Players);
 	}
 	
 	/*
 	 * Additional Render Stuff
 	 */
 	public void render(Graphics2D g) {
 		gamescroller.scroll();
 		super.render(g);
 	}
 	
 	/*
 	 * Set Background
 	 */
 	
     public void setBackground(Background backgr) {
     	Background[] bkg = ((ParallaxBackground) this.getBackground()).getParallaxBackground();
         if (backgr == null) {
         	bkg[1] = Background.getDefaultBackground();
         } else {
         	bkg[1] = backgr;
         }
         super.setBackground(new ParallaxBackground(bkg));
     }
     
     /*
      * Common Sprite Groups
      */
     public void addPlayer (Player p) {
     	Players.add(p);
     }
     public void addCharacter (Character c) {
     	Characters.add(c);
     }
     public void addItem (CollectibleItem ci) {
     	Items.add(ci);
     }
     public void addSetting (Tile p) {
     	Setting.add(p);
     }
     
     /*
      * Get Groups
      */
     public SpriteGroup getPlayers () {
     	return Players;
     }
     public SpriteGroup getCharacters() {
     	return Characters;
     }
     public SpriteGroup getItems () {
     	return Items;
     }
     public SpriteGroup getSetting () {
     	return Setting;
     }
 	
 	
 }
