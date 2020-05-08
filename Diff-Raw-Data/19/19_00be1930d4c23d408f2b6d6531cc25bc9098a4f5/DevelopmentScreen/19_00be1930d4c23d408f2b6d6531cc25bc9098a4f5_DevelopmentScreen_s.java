 package game.screen;
 
 import game.Follower;
 import game.ImprovementType;
 import game.Map;
 import game.Mule;
 import game.Player;
 import game.Plot;
 import game.Session;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import ui.Window;
 import core.Keyboard;
 
 /**
  * The Development Screen is the screen that shows all the tiles!  A character can walk around the screen and enter town
  */
 public class DevelopmentScreen extends Screen 
 {	
 	private int plotTimer;
 	private ArrayList<Mule> badMules;
 	private Keyboard keyboard;
 
     /**
      * Sets up the DevelopmentScreen and initializes instance variables needed.
      * @param session
      */
 	public DevelopmentScreen(Session session) 
 	{
 		super(session);
 		plotTimer = 15;
 		badMules = new ArrayList<>();
 		keyboard = Keyboard.getInstance();
 	}
 
     /**
      * Update refreshes the screen and draws the different entities
      */
 	public void update() 
 	{
 		renders.clear();
 		stringRenders.clear();
 		
 		boolean onOwnedPlot = false;
 		
 		checkPlayerBounds();
 		
 		for (int a = 0; a < Map.WIDTH; a++)
 		{
 			for (int b = 0; b < Map.HEIGHT; b++)
 			{
 				Plot plot = session.getPlot(a, b);
 				renders.add(plot.getRender());
 			}
 		}	
 		
 		int plotX = (int)Math.floor(session.getPlayerX(playerId) / Plot.SIZE);
 		int plotY = (int)Math.floor(session.getPlayerY(playerId) / Plot.SIZE);
 		Plot plotPlayerIsOn = session.getPlot(plotX, plotY);
 
 		if (plotPlayerIsOn.inBounds(session.getPlayerX(playerId), session.getPlayerY(playerId)) && session.isPlotOwnedByPlayer(playerId, plotPlayerIsOn.getId()))
 		{
 			onOwnedPlot = true;
 			if (keyboard.isDown(KeyEvent.VK_SPACE) && plotTimer <= 0)
 			{
 				Follower follower = session.getPlayerFollower(playerId);
 				if (!plotPlayerIsOn.hasMule())
 				{
 	                if (follower != null && follower instanceof Mule && ((Mule)follower).getType() != ImprovementType.EMPTY)
 	                {
 	                	plotPlayerIsOn.setMule((Mule)follower);
 	                    session.setPlayerFollower(playerId, null);
 	                }
 	            }
 				else 
 	            {
 	                if (follower == null)
 	                {
 	                	session.setPlayerFollower(playerId, plotPlayerIsOn.getMule());
 	                	plotPlayerIsOn.setMule(null);
 	                }
 	                else if (follower instanceof Mule) 
 	                {
 	                    badMules.add((Mule)follower);
 	                    ((Mule)follower).runAway();
 	                    session.setPlayerFollower(playerId, null);
 	                }
 	            }
 			}		
 		}
 
 		if (!badMules.isEmpty())
 		{
 			for (Mule baddie: badMules)
 			{
 				baddie.update();
 				renders.add(baddie.getRender());
 			}
 		}
 		
 		if (keyboard.isDown(KeyEvent.VK_SPACE) && plotTimer <= 0)
 		{
 			if (!onOwnedPlot)
 			{
 				Follower follower = session.getPlayerFollower(playerId);
 				if (follower != null && follower instanceof Mule)
 				{
 					((Mule)follower).runAway();
 					badMules.add((Mule)follower);
 					session.removePlayerFollower(playerId);
 					plotTimer = 15;
 				}
 			}
 			
 			plotTimer = 15;
 		}
 		
 		if (plotTimer > 0)
 		{
 			plotTimer--;
 		}
 	}
 
     /**
      * Makes sure the player can't run off the screen
      */
 	public void checkPlayerBounds()
 	{
 		if (session.getPlayerX(playerId) < 0)
 		{
 			session.setPlayerX(playerId, 1);
 		} 
 		else if (session.getPlayerX(playerId) > Window.WIDTH - Player.WIDTH)
 		{
 			session.setPlayerX(playerId, Window.WIDTH - Player.WIDTH - 1);
 		}
 
 		if (session.getPlayerY(playerId) < 0)
 		{
 			session.setPlayerY(playerId, 1);
 		} 
 		else if (session.getPlayerY(playerId) > Plot.SIZE * 5 - Player.HEIGHT)
 		{
 			session.setPlayerY(playerId, Plot.SIZE * 5 - Player.HEIGHT - 1);
 		}
 	}
 
     /**
      * Checks to see if the player should switch screens
      * @return the boolean whether or not the player is at an exit
      */
 	public boolean shouldSwitch() 
 	{
 		boolean switching = false;
        
        if (session.getPlayerX(playerId) > Plot.SIZE * 4 - Player.WIDTH && session.getPlayerX(playerId) < Plot.SIZE * 4 + 10 && session.getPlayerY(playerId) < Plot.SIZE * 3 && session.getPlayerY(playerId) > Plot.SIZE * 2)
         {
                 session.setPlayerX(playerId, Player.WIDTH * 3);
                 session.setPlayerY(playerId, Plot.SIZE * 2 + Plot.SIZE / 2 - Player.HEIGHT / 2);
                 switching = true;
         }
        
        if (session.getPlayerX(playerId) > Plot.SIZE * 5 - 10 && session.getPlayerX(playerId) < Plot.SIZE * 5 && session.getPlayerY(playerId) < Plot.SIZE * 3 && session.getPlayerY(playerId) > Plot.SIZE * 2)
         {
                 session.setPlayerX(playerId, Window.WIDTH - Player.WIDTH * 2);
                 session.setPlayerY(playerId, Plot.SIZE * 2 + Plot.SIZE / 2 - Player.HEIGHT / 2);
                 switching = true;
         }
        
        if (session.getPlayerX(playerId) > Plot.SIZE * 4 + 10 && session.getPlayerX(playerId) < Plot.SIZE * 5 - 10 && session.getPlayerY(playerId) < Plot.SIZE * 2 + 10 - Player.HEIGHT && session.getPlayerY(playerId) > Plot.SIZE * 2 - Player.HEIGHT)
         {
                 session.setPlayerX(playerId, Window.WIDTH / 2 - Player.WIDTH / 2);
                 session.setPlayerY(playerId, 10);
                 switching = true;
         }
 
         if (session.getPlayerX(playerId) > Plot.SIZE * 4 + 10 && session.getPlayerX(playerId) < Plot.SIZE * 5 - 10 && session.getPlayerY(playerId) < Plot.SIZE * 3 && session.getPlayerY(playerId) > Plot.SIZE * 3 - 10)
         {
                 session.setPlayerX(playerId, Window.WIDTH / 2 - Player.WIDTH / 2);
                 session.setPlayerY(playerId, Window.HEIGHT - Player.HEIGHT - 100);
                 switching = true;
         }
         
         return switching;
 	}
 }
