 import java.io.File;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.gui.MouseOverArea;
 import org.newdawn.slick.particles.ConfigurableEmitter;
 import org.newdawn.slick.particles.ParticleIO;
 import org.newdawn.slick.particles.ParticleSystem;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.Log;
 
 
 public class MenuState extends BasicGameState {
 
 	public static int ID = 3;
 
 	// TODO: Move these into the config file
 	private static String themeDir = "./res/themes/";
 	private static int padding = 10;
 	private ParticleSystem ps;
 	
 	// These have to be matching pairs
 	final String[] Buttons = { "start", "quit" };
 	final Integer[] mStates = { CastleScreen.ID, QuitState.ID };
 	
 	private Sound Laugh;
 	
 	private ArrayList<MenuChoice> mList;
 	Image BackgroundImage;
 
 	private ConfigurableEmitter emitter1;
 	
 	@Override
 	public void enter(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		Log.info("Entered Menu State");
 	
 		((SlickGame)arg1).getGameCore().initFinal();
 		
 		for ( int i = 0; i < mList.size(); i++ )
 			mList.get(i).getDisplayImage().setAcceptingInput(true);
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		
 		Laugh = new Sound("./res/Sounds/Evil-Laugh.wav");
		ps = new ParticleSystem(new Image("./res/dot.png"), 500);
 		
 		try {
 			File xmlFile = new File("./res/smoke.xml");
 			emitter1 = ParticleIO.loadEmitter(xmlFile);
 			emitter1.setPosition(360,240);
 		} catch (Exception e) {
 			System.out.println("Exception: " +e.getMessage());
 			e.printStackTrace();
 			Log.error("No fancy particles for you...");
 		}
 		
 		ps.addEmitter(emitter1);	
 		ps.setBlendingMode(ParticleSystem.BLEND_ADDITIVE);	
 		ps.setUsePoints(false);
 		
 		if ( Buttons.length != mStates.length )
 			throw new SlickException("Sanity check error, The number of states does not match the number of buttons");
 		
 		// Assemble the basics
 		SlickGame mGame = (SlickGame)arg1;
 		Properties mConfig = mGame.getGameCore().getConfiguration();
 		Properties mTheme = ConfigReader.readConfig(themeDir + mConfig.getProperty("theme"));
 		BackgroundImage = new Image(themeDir + mTheme.getProperty("folder") + "/" + mTheme.getProperty("main-background"));
 		
 		int vertOffset = arg0.getHeight()/2;
 		mList = new ArrayList<MenuChoice>();
 		
 		for ( int i = 0; i < Buttons.length; i++ )
 		{
 			// Read in our button image
 			Image temp;
 			try {
 				temp = new Image(themeDir + mTheme.getProperty("folder") + "/" + mTheme.getProperty(Buttons[i]));
 			}
 			catch (Exception e)
 			{
 				temp = new Image("res/placeholder.png");
 			}
 			
 			String position = mTheme.getProperty(Buttons[i] + "-pos", "auto");
 			int xpos = 0, ypos = 0;
 			if ( position.equalsIgnoreCase("auto") )
 			{
 				xpos = (arg0.getWidth()/2)-(temp.getWidth()/2);
 				ypos = vertOffset;
 				vertOffset += temp.getHeight() + padding;
 			}
 			else
 			{
 				String[] mPos = position.split(",");
 				xpos = Integer.parseInt(mPos[0]);
 				ypos = Integer.parseInt(mPos[1]);
 			}
 			
 			// Gross, no better way to handle this Java? :\
 			final StateBasedGame game = arg1; 
 			final int fi = i;
 			
 			MouseOverArea mButton = new MouseOverArea(arg0, temp, xpos, ypos)
 			{
 				public void mouseClicked(int button,
                         int x, int y, int clickCount)
 				{
 					if ( x >= this.getX() && x < this.getX()+this.getWidth() && y >= this.getY() && y <= this.getY() + this.getHeight())
 					{
 						if ( Buttons[fi].compareToIgnoreCase("start") == 0 )
 							Laugh.play();
 						game.enterState(mStates[fi]);
 					}
 				}
 			};
 			mList.add(new MenuChoice(mButton, mStates[i]));
 		}
 		
 
 		for (int i = 0; i < mList.size(); i++) 
 			mList.get(i).getDisplayImage().setAcceptingInput(false);
 		
 	}
 
 	@Override
 	public void leave(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 
 		Log.info("Leaving MenuState");
 		for (int i = 0; i < mList.size(); i++) {
 			mList.get(i).getDisplayImage().setAcceptingInput(false);
 		}
 			
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 			throws SlickException {
 		
 		// Draw our background, then our UI Elements over top
 		BackgroundImage.draw(0,0);
 		for ( int i = 0; i < mList.size(); i++ )
 			mList.get(i).getDisplayImage().render(arg0, arg2);
 		
 		ps.render();
 	}
 
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
 			throws SlickException {
 		ps.update(arg2);
 	}
 
 }
