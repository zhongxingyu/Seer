 package com.voracious.dragons.client.screens;
 
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 
 import com.voracious.dragons.client.Game;
 import com.voracious.dragons.client.graphics.Screen;
 import com.voracious.dragons.client.graphics.Sprite;
 import com.voracious.dragons.client.graphics.ui.Button;
 import com.voracious.dragons.client.utils.InputHandler;
 
 public class StatScreen extends Screen {
 	public static final int WIDTH = 2160/3;
     public static final int HEIGHT = 1440/3;
 
     private Button returnButton;
     private Sprite background;
     
 	public StatScreen(/*player's pid to do db searching*/) {
 		super(WIDTH, HEIGHT);
 		background = new Sprite("/mainMenuBackground.png");
 		returnButton=new Button("Back",0,0);
 		returnButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Game.setCurrentScreen(new MainMenuScreen());
 			}
 		});
 		/*TODO Stat's to be shown:
 		 *		user's username
 		 *		# of finished games
 		 * 		# of current games
 		 * 		# won
 		 * 		# loss
 		 *		% won
 		 * 		% loss
 		 * 		Ave time between moves
 		 * 		Ave number of turns per game
 		 */
 		
 		//the PID is assumed to be the pid of the person logged in to the game
 		
 		/*# of finished games
 		 * SELECT COUNT(gid)
 		 * FROM GAME
 		 * WHERE (pid1=PID OR pid2=PID) AND inProgress=false
 		 * GROUP BY GAME.gid
 		 */
 		
 		/*# of current games
 		 * SELECT COUNT(gid)
 		 * FROM GAME 
 		 * WHERE (pid1=PID OR pid2=PID) AND inProgress=true
 		 * GROUP BY GAME.gid
 		 */
 		
 		/*#won
 		 * SELECT COUNT(gid)
 		 * FROM WINNER
 		 * WHERE pid=PID
 		 * GROUP BY gid
 		 */
 		
 		/*#loss
 		 * =#finished games - #won
 		 */
 		
 		/*% won
 		 * =#won / #finished games
 		 */
 		
 		/*%loss
 		 * =#loss / #finished games
 		 */
 		
 		/*Ave time between turns
 		 * (sum of each games's sum of differences in timestamps (
 		 * 		from j=1 to n-1 timestamp[j]-timestamp[j-1])) 
		 * / (#finished Games+#current games)
 		 */
 		
 		/*Ave turns a game
 		 * (SELECT COUNT(*)//all tuples
 		 * FROM TURN
 		 * WHERE pid=PID
 		 * ) / (#finished Games+#current games)
 		 */
 	}
 	
 	@Override
 	public void start(){
 		InputHandler.registerScreen(this);
 	}
 	
 	@Override
 	public void stop(){
 		InputHandler.deregisterScreen(this);
 	}
 
 
 	@Override
 	public void render(Graphics2D g) {
 		background.draw(g, 0, 0);
 		this.returnButton.draw(g);
 
 	}
 
 	@Override
 	public void tick() {
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e){
 		int ex=e.getX();
 		int ey=e.getY();
 		this.returnButton.mouseClicked(ex, ey);
 	}
 }
