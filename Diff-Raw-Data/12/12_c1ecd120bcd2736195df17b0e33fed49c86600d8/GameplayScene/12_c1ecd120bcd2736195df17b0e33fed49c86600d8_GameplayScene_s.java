 package com.macbury.unamed.scenes;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.macbury.unamed.Core;
 import com.macbury.unamed.ShaderManager;
 import com.macbury.unamed.SoundManager;
 import com.macbury.unamed.intefrace.InGameInterface;
 import com.macbury.unamed.intefrace.InterfaceManager;
 import com.macbury.unamed.level.Level;
 import com.macbury.unamed.level.LevelLoader;
 import com.macbury.unamed.level.LevelLoaderInterface;
 
 public class GameplayScene extends BasicGameState implements LevelLoaderInterface {
   public final static int STATE_GAMEPLAY = 0;
 
   private boolean loading = true;
   private int startTime = 0;
 
   @Override
   public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
   }
 
   @Override
   public void enter(GameContainer container, StateBasedGame game) throws SlickException {
     super.enter(container, game);
     SoundManager.shared();
     Input input = container.getInput();
     input.pause();
     input.clearKeyPressedRecord();
     if (Level.shared() == null) {
       LevelLoader.defferedLoad(this);
      SoundManager.shared().music(SoundManager.shared().caveMusic);
       SoundManager.shared().loop(SoundManager.shared().drips);
     }
   }
 
   @Override
   public void render(GameContainer gc, StateBasedGame sb, Graphics gr) throws SlickException {
     gr.setAntiAlias(false);
     if (loading) {
       Core.instance().getMainScreenImage().draw();
     } else {
       Level.shared().render(gc, sb, gr);
       InterfaceManager.shared().render(gc, sb, gr);
     }
   }
 
   @Override
   public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
     if (loading) {
       return;
     }
     if (startTime > 0) {
       startTime -= delta;
     } else {
       Level.shared().update(gc, sb, delta);
       InterfaceManager.shared().update(gc, sb, delta);
     }
   }
 
   @Override
   public int getID() {
     return STATE_GAMEPLAY;
   }
 
   @Override
   public void onLevelLoad(Level level) throws SlickException {
     InterfaceManager.shared().push(new InGameInterface());
     Level.shared().setupViewport(Core.instance().getContainer());
     this.startTime  = 100;
     loading         = false;
     Input input     = Core.instance().getContainer().getInput();
     input.clearKeyPressedRecord();
     input.resume();
   }
 
   @Override
   public void onError(Exception e) {
     Sys.alert("Load error", "Save file is corrupted!");
     
     Core.instance().exit();
   }
   
   
 }
