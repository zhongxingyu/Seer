 package game.screen;
 
 import game.Character;
 import game.Follower;
 import game.Map;
 import game.Mule;
 import game.Plot;
 import game.Session;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import ui.Window;
 import core.Keyboard;
 
 /**
  * The Development Screen is the screen that shows all the tiles!  A character can walk around the screen and enter town
  * @author Matt
  */
 
 public class DevelopmentScreen extends Screen 
 {	
 	private int plotTimer;
 	private ArrayList<Mule> badMules;
 	
 	private Keyboard keyboard;
 	
 	public DevelopmentScreen(Session session) 
 	{
 		super(session);
 		
 		plotTimer = 15;
 		badMules = new ArrayList<Mule>();
 		keyboard = Keyboard.getInstance();
 
 	}
 
     /**
      * Update refreshes the screen and draws the different entities
      */
 	public void update() 
 	{
 		boolean onOwnedPlot = false;
 		
 		renderables.clear();
 		renderableStrings.clear();
 		
 		for (int a = 0; a < 5; a++)
 		{
 			for (int b = 0; b < 9; b++)
 			{
 				Map map = session.getMap();
 				Plot plot = map.getPlot(a, b);
 				renderables.add(plot);
 				
 				Character character = session.getCurrentCharacter();
 				
 				if (plot.inBounds(character.getX(), character.getY()))
 				{
 					for (Plot characterPlot : character.getPlots())
 					{
 						if (characterPlot == plot)
 						{
 							onOwnedPlot = true;
 						}
 					}
 				}
 			}
 		}	
 
 		if (!badMules.isEmpty())
 		{
 			renderables.addAll(badMules);
 			for(Mule baddie: badMules)
 			{
 				baddie.update();
 			}
 		}
 		
 		if (keyboard.isDown(KeyEvent.VK_SPACE) && plotTimer <= 0)
 		{
 			if (onOwnedPlot)
 			{
 				// change plot
 			}
 			else
 			{
 				Follower follower = session.getCurrentCharacter().getFollower();
 				if(follower != null && follower instanceof Mule)
 				{
 					((Mule)follower).runAway();
 					badMules.add((Mule)follower);
 					session.getCurrentCharacter().setFollower(null);
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
      * Checks to see if the player should switch screens
      * @return the boolean whether or not the player is at an exit
      */
 	public boolean shouldSwitch() 
 	{
		// x: town
		// c: character
		
		//   x x x
		// c x x x
		//   x x x 
 		if (character.getX() > Plot.SIZE * 4 - Character.WIDTH && character.getX() < Plot.SIZE * 4 + 10 && character.getY() < Plot.SIZE * 3 && character.getY() > Plot.SIZE * 2)
 		{
 			character.setX(Character.WIDTH * 3);
 			character.setY(Plot.SIZE * 2 + Plot.SIZE / 2 - Character.HEIGHT / 2);
 			return true;
 		}

		//   x x x
		//   x x x c
		//   x x x 
 		if (character.getX() > Plot.SIZE * 5 - 10 && character.getX() < Plot.SIZE * 5 && character.getY() < Plot.SIZE * 3 && character.getY() > Plot.SIZE * 2)
 		{
 			character.setX(Window.WIDTH - Character.WIDTH * 2);
 			character.setY(Plot.SIZE * 2 + Plot.SIZE / 2 - Character.HEIGHT / 2);
 			return true;
 		}
 		
		//     c
		//   x x x
		//   x x x
		//   x x x 
 		if (character.getX() > Plot.SIZE * 4 + 10 && character.getX() < Plot.SIZE * 5 - 10 && character.getY() < Plot.SIZE * 2 + 10 - Character.HEIGHT && character.getY() > Plot.SIZE * 2 - Character.HEIGHT)
 		{
 			character.setX(Window.WIDTH / 2 - Character.WIDTH / 2);
 			character.setY(10);
 			return true;
 		}
 
		//   x x x
		//   x x x
		//   x x x 
		//     c
 		if (character.getX() > Plot.SIZE * 4 + 10 && character.getX() < Plot.SIZE * 5 - 10 && character.getY() < Plot.SIZE * 3 && character.getY() > Plot.SIZE * 3 - 10)
 		{
 			character.setX(Window.WIDTH / 2 - Character.WIDTH / 2);
 			character.setY(Window.HEIGHT - Character.HEIGHT - 100);
 			return true;
 		}
 			
 		return false;
 	}
 }
