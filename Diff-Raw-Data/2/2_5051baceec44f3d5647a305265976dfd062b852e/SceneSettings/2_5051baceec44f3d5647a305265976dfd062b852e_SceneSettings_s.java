 package com.vloxlands.scene;
 
 import org.lwjgl.opengl.Display;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.settings.Settings;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.FlagButton;
 import com.vloxlands.ui.GuiRotation;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.Slider;
 import com.vloxlands.ui.TextButton;
 
 public class SceneSettings extends Scene
 {
 	@Override
 	public void init()
 	{
 		setBackground();
 		setTitle(Tr._("settings"));
 		
 		content.add(new Container(0, 115, Display.getWidth() / 2, Display.getHeight() - 220));
 		
 		content.add(new Label(20, 125, 0, 25, Tr._("language") + ":", false));
 		content.add(new FlagButton(Display.getWidth() / 4, 130, "de"));
 		content.add(new FlagButton(Display.getWidth() / 4 + FlagButton.SIZE, 130, "us"));
 		
 		content.add(new Label(20, 185, 0, 25, Tr._("fov") + ":", false));
 		
 		final Slider fov = new Slider(Display.getWidth() / 4, 200, Display.getWidth() / 4 - 20, 30, 150, CFG.FOV, GuiRotation.HORIZONTAL);
 		fov.setIntegerMode(true);
 		
 		content.add(fov);
 		
 		content.add(new Label(20, 245, 0, 25, Tr._("fps") + ":", false));
 		
 		final Slider fps = new Slider(Display.getWidth() / 4, 260, Display.getWidth() / 4 - 20, 30, 121, CFG.FPS, GuiRotation.HORIZONTAL);
 		fps.addCustomTitle(121, Tr._("unlimited"));
 		fps.setIntegerMode(true);
 		content.add(fps);
 		
		content.add(new Label(20, 245, 0, 25, Tr._("zfar") + ":", false));
 		
 		final Slider zfar = new Slider(Display.getWidth() / 4, 320, Display.getWidth() / 4 - 20, 0, CFG.RENDER_DISTANCES.length - 1, CFG.RENDER_DISTANCE, GuiRotation.HORIZONTAL);
 		zfar.setIntegerMode(true);
 		zfar.setStepSize(1);
 		zfar.addCustomTitle(0, Tr._("zfar.tiny"));
 		zfar.addCustomTitle(1, Tr._("zfar.short"));
 		zfar.addCustomTitle(2, Tr._("zfar.normal"));
 		zfar.addCustomTitle(3, Tr._("zfar.far"));
 		zfar.addCustomTitle(4, Tr._("zfar.unlimited"));
 		
 		content.add(zfar);
 		
 		TextButton b = new TextButton(Display.getWidth() / 2 - TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("back"));
 		b.setClickEvent(new IGuiEvent()
 		{
 			
 			@Override
 			public void trigger()
 			{
 				Game.currentGame.removeScene(SceneSettings.this);
 			}
 		});
 		content.add(b);
 		
 		TextButton s = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("save"));
 		s.setClickEvent(new IGuiEvent()
 		{
 			
 			@Override
 			public void trigger()
 			{
 				CFG.FOV = (int) fov.getValue();
 				CFG.FPS = (int) fps.getValue();
 				CFG.RENDER_DISTANCE = (int) zfar.getValue();
 				// CFG.SAVE_USER = remember.isSelected();
 				
 				Settings.saveSettings();
 			}
 		});
 		content.add(s);
 	}
 }
