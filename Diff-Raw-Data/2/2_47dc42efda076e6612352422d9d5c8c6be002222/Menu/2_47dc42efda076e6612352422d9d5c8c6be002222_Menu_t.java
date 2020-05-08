 package tsa2035.game.content.core;
 
 import java.io.IOException;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.openal.AL;
 import org.lwjgl.opengl.Display;
 
 import tsa2035.game.content.levels.Level1;
 import tsa2035.game.content.levels.cutscenes.Intro;
 import tsa2035.game.engine.core.Renderer;
 import tsa2035.game.engine.scene.PolyTexSprite;
 import tsa2035.game.engine.scene.Scene;
 import tsa2035.game.engine.scene.SinglePressKeyboard;
 import tsa2035.game.engine.scene.Sprite;
 import tsa2035.game.engine.scene.background.SpriteBackground;
 import tsa2035.game.engine.texture.TextureManager;
 
 public class Menu extends Scene {
 
 	PolyTexSprite[] menuOptions = new PolyTexSprite[3];
 	SinglePressKeyboard upArrow = new SinglePressKeyboard(Keyboard.KEY_W);
 	SinglePressKeyboard downArrow = new SinglePressKeyboard(Keyboard.KEY_S);
 	int selected = 0;
 	
	public Menu()
 	{
 		Game.getAirMeter().stop();
 		Game.getAirMeter().setCurrentCallback(null);
 		try {
 			setBackground(new SpriteBackground(TextureManager.getTextureFromResource("/tsa2035/game/content/images/common/wallpanels.png")));
 			
 			menuOptions[0] = new PolyTexSprite(0,0.5f,"grey", TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/grey_play.png"), false);
 			menuOptions[0].addTexture("blue", TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/blue_play.png"));
 			
 			menuOptions[1] = new PolyTexSprite(0,0,"grey", TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/grey_tutorial.png"), false);
 			menuOptions[1].addTexture("blue", TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/blue_tutorial.png"));
 			
 			menuOptions[2] = new PolyTexSprite(0,-0.5f,"grey", TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/grey_exit.png"), false);
 			menuOptions[2].addTexture("blue", TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/blue_exit.png"));
 			
 			addToScene("menunav", new Sprite(0.7f, -0.7f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/menunav.png")));
 			addToScene("title", new Sprite(0f, 0.8f, TextureManager.getTextureFromResource("/tsa2035/game/content/images/menu/title.png")));
 			for ( int i = 0; i < menuOptions.length; i++ )
 				addToScene("opt"+i, menuOptions[i]);
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void sceneLogic() {
 		if ( upArrow.check() )
 			selected--;
 		if ( downArrow.check() )
 			selected++;
 		
 		if ( selected > (menuOptions.length-1) )
 			selected = menuOptions.length-1;
 		else if ( selected < 0 )
 			selected = 0;
 		
 		for ( int i = 0; i < menuOptions.length; i++ )
 		{
 			if ( i == selected )
 				menuOptions[i].setTexture("blue");
 			else 
 				menuOptions[i].setTexture("grey");
 		}
 		
 		if ( Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_RETURN) )
 		{
 			switch (selected)
 			{
 				case 0: // Play
 					Renderer.animatedSceneSwitch(new Intro());
 					break;
 				case 1: // Tut
 					Renderer.animatedSceneSwitch(new Level1());
 					break;
 				case 2: // Exit
 					AL.destroy();
 					Display.destroy();
 					System.exit(0);
 					break;
 					
 			}
 		}
 		
 	}
 
 }
