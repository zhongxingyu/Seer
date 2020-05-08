 package states;
 
 import java.io.IOException;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Camera;
 import game.GunsAndHats;
 import game.History;
 
 
 public class HistoryScreen extends BasicGameState {
 
 	private int id;
 	private Camera camera;
 	
 	History history;
 	String[] matches;
 	
 	Image bg;
 	
 	public HistoryScreen(int id, Camera camera) {
 		this.camera = camera;
 		try {
 			bg = new Image("data/HistoryScreen.JPG");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.id=id;
 	}
 	
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		try {
 			history = new History();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			matches = history.getMatches();
 			
 			for (int i=0;i <=8;i++) {
 				//Split the current string into substrings: str[i].split(",")
 				//print out the substrings on JPanel, formatted output
 				//take new line
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics gr)
 		throws SlickException {
 		gr.setColor(Color.black);
 		bg.draw(camera.getOffset().getX(), camera.getOffset().getY(), camera.getScale());
 //		gr.drawString("Match", camera.getOffset().getX() + 165 * camera.getScale(), camera.getOffset().getY() + (255* camera.getScale()));
 //		gr.drawString("Winner", camera.getOffset().getX() + 228 * camera.getScale(), camera.getOffset().getY() + (255 * camera.getScale()));
 //		gr.drawString("2nd Place", camera.getOffset().getX() + 305 * camera.getScale(), camera.getOffset().getY() + (255 * camera.getScale()));
 //		gr.drawString("3rd Place", camera.getOffset().getX() + 400 * camera.getScale(), camera.getOffset().getY() + (255 * camera.getScale()));
 //		gr.drawString("4th Place", camera.getOffset().getX() + 490 * camera.getScale(), camera.getOffset().getY() + (255 * camera.getScale()));
 //		gr.drawString("Time", camera.getOffset().getX() + 586 * camera.getScale(), camera.getOffset().getY() + (255 * camera.getScale()));
 		for (int i = 0; i < matches.length; i++) {
 			String[] match = matches[i].split(",");
 			gr.setColor(Color.black);
 			gr.drawString("No."+(i+1) + ":", camera.getOffset().getX() + 165 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 			gr.drawString(match[0], camera.getOffset().getX() + 225 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 			gr.drawString(match[1], camera.getOffset().getX() + 310 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 			
 			if(match.length == 3) {
				gr.drawString(match[2], camera.getOffset().getX() + 580 * camera.getScale(), camera.getOffset().getY() + (285 + (4 * 40)) * camera.getScale());
 			}
 			if (match.length == 4) {
 				gr.drawString(match[2], camera.getOffset().getX() + 400 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 				gr.drawString(match[3], camera.getOffset().getX() + 580 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 				
 			} else if (match.length == 5) {
 				gr.drawString(match[2], camera.getOffset().getX() + 400 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 				gr.drawString(match[3], camera.getOffset().getX() + 490 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 				gr.drawString(match[4], camera.getOffset().getX() + 580 * camera.getScale(), camera.getOffset().getY() + (285 + (i * 40)) * camera.getScale());
 
 			}
 
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		Input input = gc.getInput();
 		
 		if(input.isKeyPressed(Input.KEY_BACK)) {
 			sb.enterState(GunsAndHats.STARTSCREEN);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 
 }
