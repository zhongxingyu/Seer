 package tsa2035.game.content.levels.level1;
 
 import java.io.IOException;
 
 import tsa2035.game.content.levels.MainCharacter;
 import tsa2035.game.engine.core.Menu;
 import tsa2035.game.engine.core.Renderer;
 import tsa2035.game.engine.scene.InteractionCallback;
 import tsa2035.game.engine.scene.PolyTexSprite;
 import tsa2035.game.engine.scene.Scene;
 import tsa2035.game.engine.scene.Sprite;
 import tsa2035.game.engine.scene.background.SpriteBackground;
 import tsa2035.game.engine.texture.AnimatedTexture;
 import tsa2035.game.engine.texture.AnimationFinishedCallback;
 import tsa2035.game.engine.texture.TextureManager;
 
 public class Level8 extends Scene {
 	
 	public Level8()
 	{
 		try {
 			setBackground(new SpriteBackground(TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/wallpanels.png")));
 			addToScene("character", new MainCharacter(0f, -0.6f)).setLayer(10);
 			addToScene("floor", new Sprite(0f, -0.98f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/floor.png"))).setSolid(true);
 			addToScene("pipes", new Sprite(0f, 0.89f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/pipes.png"))).setLayer(2);
 			addToScene("vents", new Sprite(0f, 0.7f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/vents.png"))).setLayer(2);
 			addToScene("door", new Sprite(-0.8f, 0.5f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/door.png"))).setInteractable(true);
 			
 			addToScene("platform", new Sprite(-0.48f, 0.15f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/platform400.png"))).setSolid(true);
 			
 			addToScene("crate1", new Sprite(0.4f, -0.57f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true);
 			addToScene("crate1", new Sprite(0.4f, -0.78f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f);
 			addToScene("crate1", new Sprite(0.4f, -0.94f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/halfcrate.png"))).setScale(0.5f);
 
 			addToScene("crate1", new Sprite(-0.4f, -0.67f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true);
 			addToScene("crate1", new Sprite(-0.4f, -0.88f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true);
 			
 			addToScene("crate1", new Sprite(0.68f, -0.058f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true);
 			addToScene("crate1", new Sprite(0.68f, -0.25f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f);
 			addToScene("crate1", new Sprite(0.68f, -0.46f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f);
 			addToScene("crate1", new Sprite(0.68f, -0.67f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f);
 			addToScene("crate1", new Sprite(0.68f, -0.88f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f);
 
 			addToScene("crate1", new Sprite(-0.89f, -0.92f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/halfcrate.png"))).setScale(0.5f).setSolid(true);
 			addToScene("crate1", new Sprite(-0.67f, -0.92f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/halfcrate.png"))).setScale(0.5f).setSolid(true);
 			addToScene("crate1", new Sprite(-0.8f, -0.76f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/crate.png"))).setScale(0.5f).setSolid(true);
 
 			final PolyTexSprite switchbox = new PolyTexSprite(-0.8f,-0.0f, "off", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/switch_off.png"), false);
 			addToScene("switch", switchbox).setScale(0.5f).setInteractable(true);
 			switchbox.addTexture("on", TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/switch_on.png"));
 			switchbox.registerInteractionCallback(new InteractionCallback(){
 			@Override
 			public void interactionOccured(Sprite interacter,
 			Sprite interactee) 
 			{
 			switchbox.setTexture("on");
 				((AnimatedTexture)getObject("gate").getTexture()).fire();
 			}});
 			
 			addToScene("gate", new Sprite(-0.52f, 0.425f, new AnimatedTexture("/tsa2035/game/content/images/gate", "gate", 35, 12))).setSolid(true);
 			((AnimatedTexture)getObject("gate").getTexture()).registerFinishedCallback(new AnimationFinishedCallback(){
 
 				@Override
 				public void animationFinished(AnimatedTexture animation) {
 					getObject("gate").setSolid(false);
 					
 				}
 			});
 			
 			getObject("door").registerInteractionCallback(new InteractionCallback()
 			{
 
 				@Override
 				public void interactionOccured(Sprite registeredObject,
 						Sprite withObject) {
 					Renderer.animatedSceneSwitch(new Menu());
 					
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
