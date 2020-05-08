 package tsa2035.game.content.levels.level1;
 
 import java.io.IOException;
 
 import tsa2035.game.content.levels.MainCharacter;
 import tsa2035.game.content.levels.meta.Status1;
 import tsa2035.game.engine.core.Renderer;
 import tsa2035.game.engine.scene.InteractionCallback;
 import tsa2035.game.engine.scene.Ladder;
 import tsa2035.game.engine.scene.PolyTexSprite;
 import tsa2035.game.engine.scene.Scene;
 import tsa2035.game.engine.scene.Sprite;
 import tsa2035.game.engine.scene.background.SpriteBackground;
 import tsa2035.game.engine.texture.AnimatedTexture;
 import tsa2035.game.engine.texture.AnimationFinishedCallback;
 import tsa2035.game.engine.texture.LoopedAnimatedTexture;
 import tsa2035.game.engine.texture.TextureManager;
 
 public class Level4 extends Scene {
 	boolean statusHasBeenChecked = false;
 	boolean box1Opened = false;
 	boolean box2Opened = false;
 	public Level4()
 	{
 		try {
 			setBackground(new SpriteBackground(TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/wallpanels.png")));
 			addToScene("character", new MainCharacter(0.28f, -0.5f)).setLayer(10);
 			addToScene("floor", new Sprite(0f, -0.98f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/floor.png"))).setSolid(true);
 			addToScene("pipes", new Sprite(0f, 0.89f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/pipes.png"))).setSolid(true).setLayer(0);
 			addToScene("vents", new Sprite(0f, 0.7f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/vents.png"))).setLayer(-1);
 			addToScene("door", new PolyTexSprite(0.75f, -0.58f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/doororiginal.png"))).setLayer(-2).setInteractable(false);
 			((PolyTexSprite)getObject("door")).addTexture("ready", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/door.png"));
 			
 			addToScene("platform", new Sprite(-0.78f, -0.34f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/platform150.png"))).setSolid(true);
 			addToScene("ladder2", new Ladder(-0.69f, -0.64f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/ladder_medium.png"), "character", "platform")).setInteractable(true);
 			addToScene("ladder1", new Ladder(-0.87f, -0.13f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/ladder_short.png"), "character")).setInteractable(true);
 			addToScene("statuspanel", new Sprite(-0.87f, 0.5f, new LoopedAnimatedTexture("/tsa2035/game/content/images/common", "statuspanel", 2, 2))).setScale(0.5f).setInteractable(true);
 			addToScene("authorzied", new Sprite(0.75f, -0.1f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/authorized.png"))).setScale(0.7f);
 			addToScene("crate1", new Sprite(-0.2f, -0.85f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true).setLayer(5);
 			addToScene("crate2", new Sprite(0f, -0.85f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true).setLayer(5);
 			addToScene("crate3", new Sprite(-0.01f, -0.645f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true).setLayer(5);
 			addToScene("crate4", new Sprite(-0.18f, -0.645f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true).setLayer(5);
 			addToScene("crate5", new Sprite(-0.1f, -0.43f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true).setLayer(5);
 			addToScene("gosign", new Sprite(-0.1f, 0.3f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/go.png"))).setScale(0.8f);
			addToScene("divider", new Sprite(0.45f, 0.1f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/divider.png"))).setScale(0.8f).setSolid(true);
 			
 			addToScene("gate", new Sprite(0.45f, -0.68f, new AnimatedTexture("/tsa2035/game/content/images/gate", "gate", 35, 12))).setSolid(true);
 			((AnimatedTexture)getObject("gate").getTexture()).registerFinishedCallback(new AnimationFinishedCallback(){
 
 				@Override
 				public void animationFinished(AnimatedTexture animation) {
 					getObject("gate").setSolid(false);
 					
 				}
 			});
 			
 			
 			
 			getObject("statuspanel").registerInteractionCallback(new InteractionCallback(){
 
 				@Override
 				public void interactionOccured(Sprite interacter,
 						Sprite interactee) {
 					Renderer.animatedSceneSwitch(new Status1(Level4.this));
 					statusHasBeenChecked = true;
 					if ( statusHasBeenChecked && box1Opened && box2Opened )
 					{
 						((PolyTexSprite)getObject("door")).setInteractable(true);
 						((PolyTexSprite)getObject("door")).setTexture("ready");
 						((AnimatedTexture)getObject("gate").getTexture()).fire();
 					}
 					
 				}});
 			
 			
 			final PolyTexSprite gen1box = new PolyTexSprite(-0.2f,-0.4f, "closed", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/accessdoor_all_closed.png"), false);
 			addToScene("gen1box", gen1box).setScale(0.5f).setInteractable(true);
 			gen1box.addTexture("open", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/accessdoor_genbank1_open.png"));
 			gen1box.registerInteractionCallback(new InteractionCallback(){
 			@Override
 			public void interactionOccured(Sprite interacter,
 			Sprite interactee) 
 			{
 			gen1box.setTexture("open");
 			box1Opened = true;
 			if ( statusHasBeenChecked && box1Opened && box2Opened )
 			{
 				((PolyTexSprite)getObject("door")).setInteractable(true);
 				((PolyTexSprite)getObject("door")).setTexture("ready");
 				((AnimatedTexture)getObject("gate").getTexture()).fire();
 			}
 			}});
 			
 			final PolyTexSprite gen2box = new PolyTexSprite(0.2f,-0.4f, "closed", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/accessdoor_all_closed.png"), false);
 			addToScene("gen2box", gen2box).setScale(0.5f).setInteractable(true);
 			gen2box.addTexture("open", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/accessdoor_genbank2_open.png"));
 			gen2box.registerInteractionCallback(new InteractionCallback(){
 			@Override
 			public void interactionOccured(Sprite interacter,
 			Sprite interactee) 
 			{
 			gen2box.setTexture("open");
 			box2Opened = true;
 			if ( statusHasBeenChecked && box1Opened && box2Opened )
 			{
 				((PolyTexSprite)getObject("door")).setInteractable(true);
 				((PolyTexSprite)getObject("door")).setTexture("ready");
 				((AnimatedTexture)getObject("gate").getTexture()).fire();
 			}
 			}});
 			
 			getObject("door").registerInteractionCallback(new InteractionCallback()
 			{
 
 				@Override
 				public void interactionOccured(Sprite registeredObject,
 						Sprite withObject) {
 					if ( statusHasBeenChecked && box1Opened && box2Opened )
 					{
 						Renderer.animatedSceneSwitch(new Level5());
 					}
 					
 				}
 				
 			});
 		} catch (IOException e) {
 			System.out.println("Texture loading failed!");
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void sceneLogic() {
 		// This function is called every render loop
 		// Note: callbacks are the prefered way to do collision/interaction checking, not polling
 	}
 
 }
