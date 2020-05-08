 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twosnakes;
 
 import java.awt.event.KeyEvent;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 
 
 /**
  *
  * @author jovan
  */
 public class Update
 {
 	static final int timeBetweenPivotsMs = 60;
 	static final int maxTimeCharged = 10000;
	static final int lungeTimeStep = 1000;
 
 	List<Event> events;
 	boolean snake1Left, snake1Right, snake2Left, snake2Right;
 	boolean snake1Charging, snake2Charging;
 	long snake1ChargeTime, snake2ChargeTime;
 	long lastSnake1PivotTime, lastSnake2PivotTime;
 	Random r = new Random();
 	private GameState state;
 //	List<Item> removings = new ArrayList<Item>();
 	
 	Runnable gameOverCallback;
 	
 	boolean s1_collide = false;
 	boolean s2_collide = false;
 
 	public Update(GameState state, Runnable gameOverCallback)
 	{
 		this.gameOverCallback = gameOverCallback;
 		snake1Left = snake1Right = snake2Left = snake2Right = false;
 		events = new ArrayList<Event>();
 		this.state = state;
 		events.add(0,new Collision(this.state));
 		events.add(1,new SnakeCollision(this.state));
 		snake1ChargeTime = snake2ChargeTime = 0;
 	}
 
 	void processKeyPress(KeyEvent e)
 	{
 		if(e.getKeyCode() == KeyEvent.VK_LEFT)
 		{
 			snake1Left = true;
 		}
 		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
 		{
 			snake1Right = true;
 		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN);
 		{
 			snake1Charging = true;
 		}
 
 		if(e.getKeyCode() == KeyEvent.VK_A)
 		{
 			snake2Left = true;
 		}
 		else if (e.getKeyCode() == KeyEvent.VK_D) 
 		{
 			snake2Right = true;
 		}
 	}
 
 	void processKeyRelease(KeyEvent e)
 	{
 		if(e.getKeyCode() == KeyEvent.VK_LEFT)
 		{
 			snake1Left = false;
 		}
 		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
 		{
 			snake1Right = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN)
 		{
 			snake1Charging = false;
 		}
 
 		if(e.getKeyCode() == KeyEvent.VK_A)
 		{
 			snake2Left = false;
 		}
 		else if (e.getKeyCode() == KeyEvent.VK_D) 
 		{
 			snake2Right = false;
 		}
 	}
 
 	void gameUpdate(long timePassed, List<Item> objects, List<Item> removings)
 	{
 		System.out.println(timePassed);
 		
 		if(state.snake1 != null && state.snake2 != null){
 			if( state.snake1.bodyList.isEmpty() )
 			{
 				System.out.println("GAME OVER");
 				state.winner = "Player 1";
 				gameOverCallback.run();
 			}
 			if ( state.snake2.bodyList.isEmpty() )
 			{
 				System.out.println("GAME OVER");
 				state.winner = "Player 2";
 				gameOverCallback.run();
 			}
 			
 			if( s1_collide = events.get(0).isCollide(state.snake1, objects, removings) ){
 				int item_val = r.nextInt(10);
 				if(item_val == 0 || item_val == 3 || item_val == 4 || item_val == 8){ //add apple
 					objects.add(new Apple(5, Math.floor((r.nextDouble()*1280)),  Math.floor((r.nextDouble()*720))));
 				}
 				else if(item_val == 1 || item_val == 5 || item_val == 9){ //add mouse
 					objects.add(new Mouse(1, 100, 100));
 				}
 				else if(item_val == 2 || item_val == 6 || item_val == 7 ){ //add turtle
 					objects.add(new Turtle(1, 100, 600));
 				}
 			}
 			if( s1_collide = events.get(0).isCollide(state.snake2, objects, removings) ){
 				
 				int item_val = r.nextInt(10);
 				if(item_val == 0 || item_val == 3 || item_val == 4 || item_val == 8){ //add apple
 					objects.add(new Apple(5, Math.floor((r.nextDouble()*1280)),  Math.floor((r.nextDouble()*720))));
 				}
 				else if(item_val == 1 || item_val == 5 || item_val == 7){ //add mouse
 					objects.add(new Mouse(1, 100, 100));
 				}
 				else if(item_val == 2 || item_val == 6 || item_val == 9){ //add turtle
 					objects.add(new Turtle(1, 100, 600));
 				}
 			}
 
 			if( events.get(1).s1_eat_s2(state.snake1, state.snake2) ){
 				System.out.println("adaf");
 			}
 			
 			if( events.get(1).s2_eat_s1(state.snake1, state.snake2)){
 				System.out.println("ASDA");
 			}
 		}
 		
 		for (int i = 0 ; i < state.objects.size() ; i++)
 		{
 			Item item = state.objects.get(i);
 			item.update(timePassed);
 		}
 
 		if (state.snake1 != null)
 		{
 			// get the new direction vector based on which key (left or right) is pressed.
 			Vector currentDirection = state.snake1.getDirection();
 			double x = currentDirection.x;
 			double y = currentDirection.y;
 			if (snake1Left && System.currentTimeMillis() - lastSnake1PivotTime > timeBetweenPivotsMs)
 			{
 				x = x + y/10.0;
 				y = y - x/10.0;
 				lastSnake1PivotTime = System.currentTimeMillis();
 			}
 			else if (snake1Right && System.currentTimeMillis() - lastSnake1PivotTime > timeBetweenPivotsMs)
 			{
 				x = x - y/10.0;
 				y = y + x/10.0;
 				lastSnake1PivotTime = System.currentTimeMillis();
 			}
 			state.snake1.setDirection(new Vector(x,y));
 		}
 
 		if (state.snake2 != null)
 		{
 			//get the new direction vector based on which key (left or right) is pressed.
 			Vector currentDirection = state.snake2.getDirection();
 			double x = currentDirection.x;
 			double y = currentDirection.y;
 			if (snake2Left && System.currentTimeMillis() - lastSnake2PivotTime > timeBetweenPivotsMs)
 			{
 				x = x + y/10.0;
 				y = y - x/10.0;
 				lastSnake2PivotTime = System.currentTimeMillis();
 			}
 			else if (snake2Right && System.currentTimeMillis() - lastSnake2PivotTime > timeBetweenPivotsMs) 
 			{
 				x = x - y/10.0;
 				y = y + x/10.0;
 				lastSnake2PivotTime = System.currentTimeMillis();
 			}
 			state.snake2.setDirection(new Vector(x,y));
 		}
 
 		if (state.snake1 != null)
 		{
 			if (snake1Charging)
 			{
 				snake1ChargeTime += timePassed;
 				if (snake1ChargeTime > maxTimeCharged)
 					snake1ChargeTime = maxTimeCharged;
 			}
 			else
 			{
 				if (snake1ChargeTime > 0)
 				{
 					state.snake1.move(lungeTimeStep);
 					snake1ChargeTime -= lungeTimeStep;
 					if (snake1ChargeTime < 0)
 						snake1ChargeTime = 0;
 				}
 				else
 				{
 					state.snake1.move(timePassed);
 				}
 			}
 			
 			
 			double xDir = state.snake1.getDirection().x;
 			double yDir = state.snake1.getDirection().y;
 			if(Collision.isOutOfBoundX(state.snake1))
 			{
 				if((xDir == 1 || xDir == -1)&&yDir==0)
 				{
 					xDir *= -1;
 					yDir = 0.3;
 				}
 				else
 					xDir *=-1;
 				Vector newDir = new Vector(xDir,yDir);
 				state.snake1.setDirection(newDir);
 			}
 
 			if(Collision.isOutOfBoundY(state.snake1))
 			{
 				if((yDir == 1 || yDir== -1)&&xDir==0)
 				{
 					yDir *= -1;
 					xDir = 0.3;
 				}
 				else 
 					yDir *=-1;
 				Vector newDir = new Vector(xDir,yDir);
 				state.snake1.setDirection(newDir);
 			}
 		}
 
 		if (state.snake2 != null)
 		{	
 			double xDir = state.snake2.getDirection().x;
 			double yDir = state.snake2.getDirection().y;
 			state.snake2.move(timePassed);
 
 			if(Collision.isOutOfBoundX(state.snake2))
 			{
 				if((xDir == 1 || xDir == -1)&&yDir==0)
 				{
 					xDir *= -1;
 					yDir = 0.3;
 				}
 				else 
 					xDir *=-1;
 				Vector newDir = new Vector(xDir,yDir);
 				state.snake2.setDirection(newDir);
 			}
 
 			if(Collision.isOutOfBoundY(state.snake2))
 			{
 				if((yDir == 1 || yDir== -1)&&xDir==0)
 				{
 					yDir *= -1;
 					xDir = 0.3;
 				}
 				else 
 					yDir *=-1;
 				Vector newDir = new Vector(xDir,yDir);
 				state.snake2.setDirection(newDir);
 			}
 		}
 	}
 }
