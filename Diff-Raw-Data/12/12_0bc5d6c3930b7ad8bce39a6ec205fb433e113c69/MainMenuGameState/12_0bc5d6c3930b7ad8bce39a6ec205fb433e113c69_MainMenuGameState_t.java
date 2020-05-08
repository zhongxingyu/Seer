 package tutorials.slickout.mainmenu;
  
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.MouseListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
  
 import tutorials.slickout.GameInfo;
 import tutorials.slickout.gameplay.GameplayState;
 import tutorials.slickout.playerinfo.PlayerInfo;
  
 public class MainMenuGameState extends BasicGameState implements MouseListener{
  
 	private Image background;
 	private Image selector;
 	//1 = start game; 2 = instructions; 3 = high scores; 4 = quit; -1 = none;
 	private int selection; 
 	private int optionSelected;
 	private int topScore;
  
 	@Override
 	public int getID() {
 		return 0;
 	}
  
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		
 		container.setMouseGrabbed(false);
 		
 		selection = -1;
 		optionSelected = selection;
  
 		if ( GameInfo.getCurrentGameInfo() != null){
 			topScore = ( topScore > GameInfo.getCurrentGameInfo().getPlayerInfo().getScore() ) ? topScore : GameInfo.getCurrentGameInfo().getPlayerInfo().getScore(); 
 		}
 	}
  
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		background = new Image("data/dynamoMenuBig.jpg");
 		selector = new Image("data/selector.png");
 	}
  
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		background.draw();
  
 		if(selection == 1){
 			selector.draw(400, 355);
 			GameInfo.createNewGameInfo();
 		}else if(selection == 2){
 			selector.draw(400, 405);
 		}else if(selection == 3){
 			selector.draw(400, 455);
 		}else if(selection == 4){
 			selector.draw(400, 505);
 		}
  
 		g.drawString("TOPSCORE : " + topScore, 10, 10) ;
 	}
  
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		if(optionSelected == 1){
			String levelfile = "data/level2.lvl";
 			// obtain the game state
 			GameplayState gameplay = (GameplayState) game.getState(1);
 
 			gameplay.setLevelFile(levelfile);
 
 			game.enterState(1);
 		}else if(optionSelected == 2){
 			//go to instructions
 			game.enterState(3);
 		}else if(optionSelected == 3){
 			//go to high score display
 			game.enterState(5);
 		}else if(optionSelected == 4){
 			System.exit(0);
 		}
 	}
  
 	public void mouseMoved(int oldx, int oldy, int newX, int newY){
  
 		selection = -1;
 
 		if(newX > 479 && newX < 777){
 			if ( newY > 369 && newY < 423){
 				selection = 1;
 			}else if ( newY > 423 && newY < 477){
 				selection = 2;
 			}else if ( newY > 477 && newY < 531){
 				selection = 3;
 			}else if ( newY > 531 && newY < 585){
 				selection = 4;
 			}
 		}
 		
 	}
  
 	public void mouseClicked(int button, int x, int y, int clickCount){
 		optionSelected = selection;
 	}
 }
