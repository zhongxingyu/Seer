 package tsa2035.game.content.levels.level1;
 
 import java.io.IOException;
 
 import tsa2035.game.content.levels.MainCharacter;
 import tsa2035.game.engine.bounding.Side;
 import tsa2035.game.engine.core.Renderer;
 import tsa2035.game.engine.scene.CollisionCallback;
 import tsa2035.game.engine.scene.InteractionCallback;
 import tsa2035.game.engine.scene.PolyTexSprite;
 import tsa2035.game.engine.scene.Scene;
 import tsa2035.game.engine.scene.Sprite;
 import tsa2035.game.engine.scene.background.SpriteBackground;
 import tsa2035.game.engine.texture.LoopedAnimatedTexture;
 import tsa2035.game.engine.texture.TextureManager;
 
 public class Level1 extends Scene {
 	
 	public Level1()
 	{
 		try {
 			setBackground(new SpriteBackground(TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/wallpanels.png")));
 			addToScene("character", new MainCharacter(-0.7f, -0.5f)).setLayer(10);
 			addToScene("floor", new Sprite(0f, -0.98f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/floor.png"))).setSolid(true);
 			addToScene("pipes", new Sprite(0f, 0.89f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/pipes.png"))).setSolid(true).setLayer(0);
 			addToScene("vents", new Sprite(0f, 0.7f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/vents.png"))).setSolid(true).setLayer(-1);
 			addToScene("wasd", new Sprite(-0.7f, 0.2f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/wasd.png")));
			addToScene("door", new Sprite(0.75f, -0.58f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/door.png"))).setLayer(-2).setSolid(true);
 
			getObject("door").registerCollisionCallback(new CollisionCallback()
 			{
 
 				@Override
				public void collisionOccured(Sprite registeredObject,
						Sprite withObject, Side onSide) {
 					Renderer.animatedSceneSwitch(new Level2());
 					
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
