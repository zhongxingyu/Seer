 package nitrogene.core;
 
 import java.awt.FontFormatException;
 import java.io.IOException;
 
 import nitrogene.util.AppData;
 import nitrogene.util.Button;
 import nitrogene.util.Slider;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class OptionState extends BasicGameState{
 	
 	private Slider music, sfx, alarm; 
 	private Button accept, decline;
 	private float x,y = 0;
 	private Font largefont, normalfont;
 	
 	public OptionState(int width, int height){
 		this.x = (float) width / 2 - 400;
 		this.y = (float) height / 2 - 200;
 	}
 	
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
		music = new Slider(x+151+(GlobalInformation.musiclevel*6),y+86-15,20,40,x+751,y+86-15,x+151,y+86-15);
		sfx = new Slider(x+151+(GlobalInformation.sfxlevel*6),y+146-15,20,40,x+751,y+146-15,x+151,y+146-15);
		alarm = new Slider(x+151+(GlobalInformation.alarmlevel*6),y+205-15,20,40,x+751,y+205-15,x+151,y+205-15);
 		
 		try {
 			decline = new Button("Menu", x+720, y+300, 110, 40);
 			accept = new Button("Accept", x+20, y+300, 110, 40);
 		} catch (FontFormatException | IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
 		this.largefont = GlobalInformation.getMenuFont(70f);
 		this.normalfont = GlobalInformation.getMenuFont(18f);
 	}
 	
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		music.update(container);
 		sfx.update(container);
 		alarm.update(container);
 		decline.update(container);
 		accept.update(container);
 		
 		if(accept.isClicked()){
 			AppData.saveOptions();
 			game.enterState(1);
 		} else if(decline.isClicked()){
 			game.enterState(1);
 		}
 		
 		GlobalInformation.musiclevel = (music.getX()-151-x)/6/100;
 		GlobalInformation.sfxlevel = (sfx.getX()-151-x)/6/100;
 		GlobalInformation.alarmlevel = (alarm.getX()-151-x)/6/100;
 	}
 	
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		g.setBackground(Color.black);
 		Image a = (Image) AssetManager.get().get("optionsbackground");
 		a.draw(x, y);
 		
 		Image temp = (Image) AssetManager.get().get("slider");
 		music.render(temp,temp);
 		sfx.render(temp,temp);
 		alarm.render(temp,temp);
 		
 		accept.render(g, (Image) AssetManager.get().get("defaultbuttonnormal"), (Image) AssetManager.get().get("defaultbuttonpressed"), (Image) AssetManager.get().get("defaultbuttonhover"));
 		decline.render(g, (Image) AssetManager.get().get("defaultbuttonnormal"), (Image) AssetManager.get().get("defaultbuttonpressed"), (Image) AssetManager.get().get("defaultbuttonhover"));
 		
 		g.setFont(this.largefont);
 		//title
 		g.drawString("Options", x+271, y+5);
 		
 		g.setFont(this.normalfont);
 		//slider labels
 		g.drawString("Music Volume:", x+19, y+81);
 		g.drawString("SFX Volume:", x+19, y+141);
 		g.drawString("Alarm Volume:", x+19, y+201);
 	}
 
 	public int getID() {
 		return 4;
 	}
 
 }
