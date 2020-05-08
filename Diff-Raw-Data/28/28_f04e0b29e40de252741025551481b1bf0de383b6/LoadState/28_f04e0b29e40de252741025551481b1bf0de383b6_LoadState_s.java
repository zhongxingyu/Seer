 package com.vulcastudios.states;
 
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.vulcastudios.TestGame;
 import com.vulcastudios.actors.Level;
 import com.vulcastudios.ui.LoadingBar;
 import com.vulcastudios.util.Resource;
 import com.vulcastudios.util.ResourceManager;
 
 public class LoadState extends BasicGameState {
 
 	private ResourceManager rm;
 	private Iterator<Entry<String, Resource>> images, maps;
 	//private Iterator<Entry<String, Resource>> animations;
 
 	public LoadState(ResourceManager rm){
 		this.rm = rm;
 		images = rm.imageResources.entrySet().iterator();
 		maps = rm.mapResources.entrySet().iterator();
 		//animations = rm.animationResources.entrySet().iterator();
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		rm.startLoad();
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		//g.drawString("Progress: "+this.rm.getProgress(), 50, 50);
 		
		IntBuffer o = ByteBuffer.allocateDirect(200).asIntBuffer();
 		GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE, o);
 		System.err.println("GL_MAX_TEXTURE_SIZE: " + o.get());
 		LoadingBar lb = new LoadingBar(5, container.getHeight()-35, container.getWidth()-10, 30);
 		lb.setProgress(this.rm.getProgress());
		lb.render(container, game, g);
 		
 		g.drawString("VulcaStudios", (container.getWidth()/2)-75, (container.getHeight()/2-150));
 
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		if(this.rm.getProgress() == 100)
 			game.enterState(TestGame.MAIN_MENU_STATE_ID);
 
 		if(images.hasNext()){
 			Resource r = images.next().getValue();
 			rm.load(r.getKey(), new Image(r.getLocation()));
 		}else{
 
 
 			if(maps.hasNext()){
 				Resource r = maps.next().getValue();
 				rm.load(r.getKey(), new TiledMap(r.getLocation(), "tilesets/"));
 				if(game instanceof TestGame){
 					((TestGame)game).addLevel(new Level("level1", this.rm));
 					((TestGame)game).getCurrentLevel().initLevel();
 				}
 
 			}
 
 		}
 
 		/*if(animations.hasNext()){
 			Resource r = animations.next().getValue();
 			rm.load(r.getKey(), new Image(r.getLocation()));
 		}*/
 
 
 		if(rm.getProgress() ==100){
 			game.enterState(TestGame.MAIN_MENU_STATE_ID);
 		}
 
 	}
 
 	@Override
 	public int getID() {
 		return TestGame.LOAD_STATE_ID;
 	}
 
 }
