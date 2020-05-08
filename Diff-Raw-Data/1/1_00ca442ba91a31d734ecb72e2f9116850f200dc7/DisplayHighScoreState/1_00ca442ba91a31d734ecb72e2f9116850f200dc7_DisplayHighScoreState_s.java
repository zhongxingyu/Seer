 package tutorials.slickout.mainmenu;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.MouseListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import tutorials.slickout.GameInfo;
 import tutorials.slickout.gameplay.GameplayState;
 
 public class DisplayHighScoreState extends BasicGameState implements MouseListener {
 	
 	private Image background;
 	private Image selector;
 	private int selection; 
 	private int optionSelected;
 	private TreeMap<Integer,String> names;
 	private TreeMap<Integer,Integer> scores;
  
 	@Override
 	public int getID() {
 		return 5;
 	}
  
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		
 		names = new TreeMap<Integer, String>();
 		scores = new TreeMap<Integer, Integer>();
 		
 		container.setMouseGrabbed(false);
 	    
 		//read in highscores
 	    BufferedReader br = null;
 		try
 		{
 			br = new BufferedReader(new FileReader("data/highScores.txt"));
 	        String line = br.readLine();
 	        //key used to associate names with scores
 	        int key = 0;
 	        while (line != null) {
 	            names.put(key, line);
 	            line = br.readLine();
 	            scores.put(key, Integer.parseInt(line));
 	            line = br.readLine();
 	        }
 		}
 		catch ( IOException e)
 		{
 		}
 		finally
 		{
 			try
 			{
 				if ( br != null)
 					br.close( );
 			}
 			catch ( IOException e)
 			{
 			}
 	     }
 				
 		selection = -1;
 		optionSelected = selection;
 
 	}
  
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		background = new Image("data/highScoreScreen.png");
 		selector = new Image("data/selector.png");
 	}
  
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		
 		background.draw();
 		
 		//draw names
 		int i = 0;
 		for(Map.Entry<Integer,String> entry : names.entrySet()) {
 			  String name = entry.getValue();
 
 			  g.drawString(name , 200, 200 +( i * 50));
 			  i++;
 			}
 		
 		//draw scores
 		int j = 0;
 		for(Map.Entry<Integer,Integer> entry : scores.entrySet()) {
 			  Integer score = entry.getValue();
 
 			  g.drawString(score.toString() , 400, 200 +( j * 50));
 			  j++;
 			}
 		
 		
 		if(selection == 1){
 			selector.draw(242,520);
 		}else if(selection == 2){
 			selector.draw(575, 520);
 		}
  
 	}
  
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		
 		//go to main menu
 		if(optionSelected==1){
 			game.enterState(0);
 		}
 		else if(optionSelected == 2){
 			//go to game
 			GameInfo.createNewGameInfo();
 
 			String levelfile = "data/level1.lvl";
 			// obtain the game state
 			GameplayState gameplay = (GameplayState) game.getState(1);
 
 			gameplay.setLevelFile(levelfile);
 
 			game.enterState(1);
 		}
 	}
  
 	public void mouseMoved(int oldx, int oldy, int newX, int newY){
 		selection = -1;
 		if(newX > 30 && newX < 242){
 			if ( newY > 540 && newY < 580){
 				selection = 1;
 			}
 		}
 			
 		if(newX > 640 && newX < 750){
 			if ( newY > 540 && newY < 580){
 				selection = 2;
 			}
 		}
 	}
  
 	public void mouseClicked(int button, int x, int y, int clickCount){
 		optionSelected = selection;
 	}
 }
