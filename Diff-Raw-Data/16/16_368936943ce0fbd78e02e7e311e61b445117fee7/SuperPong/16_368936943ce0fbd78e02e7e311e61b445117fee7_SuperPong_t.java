 import ucigame.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class SuperPong extends Ucigame
 {
 	Sprite ball,ball1, red_ball, blue_ball, green_ball, black_ball;
 	Sprite left_paddle,right_paddle;
 	Sprite force, red, blue, green, black;
 	Sprite blackHole1, blackHole2;
 	List<Sprite> listForce; 
 	ArrayList<Object> listBall = new ArrayList<Object>();
 	int num_ball;
 	int currentForce;
 	boolean DirectionDown = true;
 	int degree = 0;	
 	int health1;
 	int health2;
 	boolean startPlayGameHasRun = false;
 	final double theta = 10 * 3.141592653586 / 180;
 	Sprite splitBlock;
 	boolean canSplit = true;
 	int winner = 0;
 	Sprite selectArrow;
 	int whichState = 0; // 1 for arrow pointing start game, 2 for credit, 3 for in help scene, 0 for in credit scene
 	int leftPaddleSpeed = 10;
 	int rightPaddleSpeed = 10;
 	int timer = 1;
 	int ball_limit = 6;
 	int force_speed = 2;
 	double blackHoleSpeed = 1;
 	public void setup()
 	{
		//window.size(1000, 660);
 		window.title("SuperPong");
 		framerate(30);
 		Image bkg = getImage("../Resources/Image/background.png");
 		canvas.background(bkg);
 		initializeField();
 		initializeBall();
 		selectArrow = makeSprite(getImage("../Resources/Image/selectarrow.png"));
 		selectArrow.position(canvas.width()/2 - 130, canvas.height()/2 - 25);
 		splitBlock = makeSprite(getImage("../Resources/Image/splitBlock.png"));
 		splitBlock.position(canvas.width()/2 - 90 ,150);
 		splitBlock.motion(2,0);
 	
 		startScene("Start");		
 		
 	}
 
 	public void startStart()
     	{
         	canvas.background(getImage("../Resources/Image/firstscene.png"));
         	String[] fontlist = arrayOfAvailableFonts();
         	//println("fontlist[3]: " + fontlist[3]);   // arbitrarily choose fourth font
        	canvas.font(fontlist[3], BOLD, 24, 255, 255, 255);
     	}
 
     	public void drawStart()
     	{
         	canvas.clear();
        	canvas.putText("Start Game", canvas.width()/2 - 80, canvas.height()/2);
 			canvas.putText("Instruction & Credit", canvas.width()/2 - 80, canvas.height()/2 + 30);
			canvas.putText("Press 'Enter' to select...", canvas.width()/2 + 150, canvas.height()/2 + 300);
 			selectArrow.draw();
 		
 		if(whichState == 3)
 		{	
 			if(winner == 1)
 				canvas.putText("Player 1 is Winner!", canvas.width()/2 - 103, canvas.height()/2 - 80);
 			else if(winner == 2)
 				canvas.putText("Player 2 is Winner!", canvas.width()/2 - 103, canvas.height()/2 - 80);
 			canvas.putText("Press 'Enter' to select...", canvas.width()/2 + 280, canvas.height()/2 + 320);
 		}		
     	}
 
     	
     	public void onKeyPressStart()
     	{
 		if (keyboard.isDown(keyboard.ENTER))
 		{
 			if(whichState == 2)
     	    			startScene("Instruction");
 			else
     	    			startScene("PlayGame");			
 		}		
 		else if(keyboard.isDown(keyboard.UP))
 		{
 			whichState = 1;
 			selectArrow.position(canvas.width()/2 - 130, canvas.height()/2 - 25);
 		}
 		else if(keyboard.isDown(keyboard.DOWN))
 		{
 			whichState = 2;
 			selectArrow.position(canvas.width()/2 - 130, canvas.height()/2 + 5);
 		}
 		
     	}
 	
 	public void startInstruction()
     	{
 		canvas.background(getImage("../Resources/Image/instruction.png"));			
     	}
 	public void drawInstruction()
 	{
 		canvas.clear();
 		canvas.putText("Press 'B' to go back", canvas.width()/2 + 180, canvas.height()/2 + 320);
 		if(keyboard.isDown(keyboard.B))
 		{			
 			whichState = 0;
 			startScene("Start");
 		}
 	}
 	
     	public void startPlayGame()
     	{
         	health1 = 10;
 		health2 = 10;
 		left_paddle.position(10,
 		               (canvas.height() - left_paddle.height()) / 2);
 		right_paddle.position(canvas.width() - right_paddle.width() - 10,
 		               (canvas.height() - right_paddle.height()) / 2);
 		Image bkg = getImage("../Resources/Image/background.png");
         	canvas.background(bkg);        	
 		canvas.font("Arial", BOLD, 16, 255, 255, 255);
    	}
 
 	public void drawPlayGame()
 	{		
 		canvas.clear();
 		force.move();
 		moveBHole(blackHole1, blackHole2);		
 		moveRandom(splitBlock);
 		degree += 2;
 		splitBlock.rotate(degree);
 		int index = 0, count = num_ball;
 		while(count != 0)
 		{
 			ball = (Sprite) listBall.get(index);
 			ball.move();
 			index = index + 2;
 			--count;
 		}
 		
 		//Force change if collided with top and bottom of the screen
 		if((force.y() <= 0) ||(force.y() >= canvas.height() - force.height())) 
 			forceChange();
 		
 		
 		checkBallCollision();
 		collideBlackHole();
 		
 		left_paddle.stopIfCollidesWith(TOPEDGE, BOTTOMEDGE, LEFTEDGE, RIGHTEDGE);
 		right_paddle.stopIfCollidesWith(TOPEDGE, BOTTOMEDGE, LEFTEDGE, RIGHTEDGE);
 		
 		//Draw everything to the background.
 		force.draw();
 		
 		blackHole1.draw();		
 		blackHole2.draw();	
 		count = num_ball;
 		index = 0;
 		while(count != 0)
 		{
 			ball = (Sprite) listBall.get(index);
 			ball.draw();
 			index = index + 2;
 			--count;
 		}
 		left_paddle.draw();		
 		right_paddle.draw();	
 		splitBlock.draw();
 		canvas.putText("Player 1: " + health1, 10, 25);
 		canvas.putText("Player 2: " + health2, canvas.width() - 100, 25);
 
 		
 		
 		timer += 1;
 		generateBall();
 	}
 
 	public void onKeyPressPlayGame()
 	{
 		// Arrow keys move the paddle
 		if (keyboard.isDown(keyboard.W))
 			left_paddle.nextY(left_paddle.y() - leftPaddleSpeed);
 		if (keyboard.isDown(keyboard.S))
 			left_paddle.nextY(left_paddle.y() + leftPaddleSpeed);		
 
 		if (keyboard.isDown(keyboard.UP))
 			right_paddle.nextY(right_paddle.y() - rightPaddleSpeed);
 		if (keyboard.isDown(keyboard.DOWN))
 			right_paddle.nextY(right_paddle.y() + rightPaddleSpeed);
 
 	}
 
 	//Function change the force field whenever it hit the top or bottom of background.
 	public void forceChange()
 	{
 		double currentY = -1, speed;
 		//Force change randomly
 		currentForce = (new Random()).nextInt(4);
 		speed = -force.yspeed();
 		if(force.y() <= 0)
 			currentY = 5;
 				
 		force = listForce.get(currentForce);		
 		if(currentY == -1)
 			currentY = canvas.height() - force.height() -5;
 		force.position((canvas.width() - force.width()) / 2, currentY);
 		force.motion(0, speed);
 		//force.hide();
 	}
 	
 	//Set up a few balls to the game.
 	public void initializeBall()
 	{
 		ball = makeBall(0);
 		ball.position(canvas.width()/2 - 50 ,canvas.height()/2 - ball.height()/2 -50); //90 150
 		ball.motion(5, 2);
 		listBall.add(ball);
 		listBall.add(0);
 
 		ball = makeBall(0);
 		ball.position(canvas.width()/2 + 50, canvas.height()/2 - ball.height()/2 + 50);
 		ball.motion(-5, -2);
 		listBall.add(ball);
 		listBall.add(0);
 		num_ball = 2;
 	}
 
 	//Function to change the ball based on the force field it contact with.
 	public void ballChange()
 	{
 		int index = listBall.indexOf(ball);
 		double currentX = ball.x();
 		double currentY = ball.y();		
 		double currentSpeedX = ball.xspeed();
 		double currentSpeedY = ball.yspeed();
 		
 		
 		//Adjust speed of the ball based on the current force in which the ball contact with.
 		//Blue ball: slow down
 		if(currentForce == 1)
 		{
 			currentSpeedX = currentSpeedX / 1.5;
 			currentSpeedY = currentSpeedY / 1.5;
 		}
 		//Green ball: speed up
 		if(currentForce == 2)
 		{
 			currentSpeedX = currentSpeedX * 2;
 			currentSpeedY = currentSpeedY * 2;
 		}
 
 		//Set a speed limit for the ball.
 		double magnitude = Math.pow(Math.pow(currentSpeedX, 2.0) + Math.pow(currentSpeedY, 2.0), 0.5);
 		if(magnitude >= 15)
 		{
 			currentSpeedX = currentSpeedX / 2;
 			currentSpeedY = currentSpeedY / 2;
 		}
 		else if(magnitude <= 2.5)
 		{
 			currentSpeedX = currentSpeedX * 1.5;
 			currentSpeedY = currentSpeedY * 1.5;
 		}
 		
 		//Replace old ball with new one
 		ball = 	makeBall(currentForce);
 		ball.motion(currentSpeedX, currentSpeedY);
 		ball.position(currentX, currentY);
 		listBall.set(index, ball);
 		listBall.set(index + 1, currentForce);
 		ball.move();
 		
 	}
 
 	//Assist function to ballChange() function
 	public Sprite makeBall(int currentForce)
 	{
 		if(currentForce == 0)
 			return ball = makeSprite(getImage("../Resources/Image/red_ball.png", 255, 255, 255));
 		else if(currentForce == 1)
 			return ball = makeSprite(getImage("../Resources/Image/blue_ball.png", 255, 255, 255));
 		else if(currentForce == 2)
 			return ball = makeSprite(getImage("../Resources/Image/green_ball.png", 255, 255, 255));
 		else
 			return ball = makeSprite(getImage("../Resources/Image/black_ball.png", 255, 255, 255));
 	}
 	
 	public void paddleChange()
 	{
 		
 	}
 
 	//Initialize the field.
 	public void initializeField()
 	{
 		//Add 4 types of forces
 		listForce = new ArrayList<Sprite>();
 		red = makeSprite(getImage("../Resources/Image/red.png", 255, 255, 255), 125 , 125);
 		listForce.add(red);
 		blue = makeSprite(getImage("../Resources/Image/blue.png", 255, 255, 255));
 		listForce.add(blue);
 		green = makeSprite(getImage("../Resources/Image/green.png", 255, 255, 255));
 		listForce.add(green);
 		black = makeSprite(getImage("../Resources/Image/black.png", 255, 255, 255));
 		listForce.add(black);
 
 		force = red;
 		force.position((canvas.width() - force.width()) /2,
 		               (canvas.height() - force.height()) / 2);
 		force.motion(0, force_speed);
 		currentForce = 0;
 
 		//Add 2 black holes
 		blackHole1 =  makeSprite(getImage("../Resources/Image/blackhole.png"));
 		blackHole1.position(550,50);
 
 		blackHole2 =  makeSprite(getImage("../Resources/Image/blackhole.png"));
 		blackHole2.position(canvas.width() - 600,canvas.height() - 100);
 		
 		//Add 2 paddles	
 		left_paddle = makeSprite(getImage("../Resources/Image/red_paddle.png"));	
 		left_paddle.position(-5,
 		               (canvas.height() - left_paddle.height()) / 2);
 		right_paddle = makeSprite(getImage("../Resources/Image/red_paddle.png"));	
 		right_paddle.position(canvas.width() - right_paddle.width() - 5,
 		               (canvas.height() - right_paddle.height()) / 2);
 	}
 	
 	public void checkBallCollision()
 	{
 		for(int index = 0; index/2 < num_ball; index += 2)
 		{
 			ball = (Sprite) listBall.get(index);
 			ball.checkIfCollidesWith(force);
 			if(ball.collided())
 				ballChange();
 					
 						
 			ball.bounceIfCollidesWith(left_paddle);
 			if(ball.collided())
 				left_paddle = paddleChange(left_paddle, "left", index);
 			ball.bounceIfCollidesWith(right_paddle);
 			if(ball.collided())
 				right_paddle = paddleChange(right_paddle, "right", index);
 			ball.bounceIfCollidesWith(TOPEDGE, BOTTOMEDGE);
 			
 			
 			ball.checkIfCollidesWith(splitBlock);
 			if(ball.collided() && canSplit)				
 				Split(ball, index);					
 			else 
 				regenSplitBlock();	
 			
 			ball.checkIfCollidesWith(LEFTEDGE);
 			if(ball.collided())
 			{
 				health1 -= 1;
 				num_ball -= 1;
 				listBall.remove(index+1);
 				listBall.remove(ball);
 				index -= 2;
 	
 				if(health1 <= 0)
 					GameOver(2);
 				else if(num_ball <= 0)
 					MakeBallWhenNoBall();
 			}
 			ball.checkIfCollidesWith(RIGHTEDGE);
 			if(ball.collided())
 			{
 				health2 -= 1;
 				num_ball -= 1;
 				listBall.remove(index+1);
 				listBall.remove(ball);
 				index -= 2;
 				if(health2 <= 0)
 					GameOver(1);
 				else if(num_ball <= 0)
 					MakeBallWhenNoBall();
 			}
 		}		
 	}
 	
 	//split the ball into two with 2*theta degree. theta for each..
 	public void Split(Sprite ball, int index)
 	{
 		double currentSpeedX = ball.xspeed();
 		double currentSpeedY = ball.yspeed();
 		double currentX = ball.x();
 		double currentY = ball.y();
 		ball.motion(Math.cos(theta)*currentSpeedX + Math.sin(theta)*currentSpeedY ,- Math.sin(theta)*currentSpeedX + Math.cos(theta)*currentSpeedY);
 		num_ball += 1;
 		int color = (Integer) listBall.get(index+1);
 		//System.out.println(color);
 		Sprite splitBall = makeBall(color);
 		splitBall.motion(Math.cos(theta)*currentSpeedX-Math.sin(theta)*currentSpeedY ,Math.sin(theta)*currentSpeedX + Math.cos(theta)*currentSpeedY);
 		splitBall.position(currentX,currentY);
 		listBall.add(splitBall);
 		listBall.add(color);	
 		splitBlock.hide();
 		canSplit = false;
 	}	
 	
 	//regenerage the split block
 	public void regenSplitBlock()
 	{	
 		//The rate split block reappear will base on the numball of ball on the filed: more ball -> less chance it will reappear.
 		int rng = (new Random()).nextInt(100 * num_ball);
 		if(rng == 0 && num_ball < ball_limit)
 		{
 			splitBlock.show();
 			canSplit = true;
 		}		
 	}
 
 	public void moveBHole(Sprite first_blackhole, Sprite second_blackhole)
 	{
 		double currentX, currentY;
 		if(first_blackhole.y() <= first_blackhole.height())
 			DirectionDown = true;
 		if(first_blackhole.y() >= canvas.height() - first_blackhole.height())
 			DirectionDown = false;
 		if(DirectionDown)
 			currentY = first_blackhole.y() + blackHoleSpeed;
 		else
 			currentY = first_blackhole.y() - blackHoleSpeed;
 		//black holes move in curve.
 		currentX = -Math.pow(currentY - canvas.height()/2 - 50, 2.0) / 562 + 800;
 		first_blackhole.position(currentX, currentY);
 		second_blackhole.position(canvas.width() - currentX, canvas.height() - currentY);
 	}
 	
 	public void moveRandom(Sprite obj)
 	{
 		int chance = (new Random()).nextInt(80);
 		double xSpd = obj.xspeed();
 		double ySpd = obj.yspeed();
 
 		if(chance == 0)
 		{
 			chance = (new Random()).nextInt(7);
 			if(chance == 0)
 			{	
 				xSpd = -xSpd;
 				ySpd = -ySpd;
 			}
 			else if(chance == 1)
 				xSpd = -xSpd;			
 			else 	if(chance == 2)		
 				ySpd = -ySpd;	
 			else if(chance == 3)
 				xSpd = 1 + xSpd;			
 			else 	if(chance == 4)		
 				ySpd = 1 + ySpd;	
 			else if(chance == 5)	
 				xSpd = xSpd - 1;			
 			else 			
 				ySpd = ySpd - 1;
 		
 		}			
 		obj.motion(xSpd, ySpd);
 		
 		if(obj.x() <= 200)
 		{
 			obj.position(200, obj.y());
 			obj.motion(2, obj.yspeed());
 		}
 		if(obj.x() >= canvas.width() - 200)
 		{
 			obj.position(canvas.width() - 200, obj.y());
 			obj.motion(-2, obj.yspeed());
 		}
 		if(obj.y() <= 50)
 		{
 			obj.position(obj.x(), 50);
 			obj.motion(obj.xspeed(), 2);
 		}
 		if(obj.y() >= canvas.height() - 50)
 		{
 			obj.position(obj.x(), canvas.height() - 50);
 			obj.motion(obj.yspeed(), -2);
 		}
 		obj.move();
 		
 		
 }		
 	public void collideBlackHole()	
 	{
 		int index = 0, count = num_ball;
 		while(count != 0)
 		{
 			ball = (Sprite) listBall.get(index);
 			ball.checkIfCollidesWith(blackHole1);
 			if(ball.collided())
 				ball.position(blackHole2.x(),blackHole2.y());
 			else
 			{
 				ball.checkIfCollidesWith(blackHole2);
 				if(ball.collided())
 					ball.position(blackHole1.x(),blackHole1.y());
 			}
 			index = index + 2;
 			--count;			
 		}
 	}
 	
 	public void MakeBallWhenNoBall()
 	{
 		int tmpForce = (new Random()).nextInt(4);
 		Sprite tmpBall = makeBall(tmpForce);
 		tmpBall.position(canvas.width()/2 - ball.width()/2, canvas.height()/2 - ball.height()/2);
 		tmpBall.motion((new Random()).nextInt(19)-9, (new Random()).nextInt(7)-3);
 		if(tmpBall.xspeed() == 0)
 			tmpBall.motion(-1, (new Random()).nextInt(7)-3);
 		listBall.add(tmpBall);
 		listBall.add(tmpForce);
 		num_ball += 1;	
 	}
 
 	public void GameOver(int winner)
 	{
 		whichState = 3;
 		timer = 0;
 		listBall.clear();
 		num_ball = 0;		
 		this.winner = winner;
 		setup();
 		startScene("Start");		
 	}
 	public Sprite paddleChange(Sprite paddle,String position,int ball_index)
 	{
 		Sprite newPaddle;
 		double currentX = paddle.x();
 		double currentY = paddle.y();
 		int color = (Integer) listBall.get(ball_index + 1);
 		//System.out.println("CC" + color);
 		if(color == 0)
 		{
 			newPaddle = makeSprite(getImage("../Resources/Image/red_paddle.png"));
 			/*if(position == "right")
 				rightPaddleSpeed += 2;
 			else
 				leftPaddleSpeed += 2;*/
 		}
 		else if(color == 1)
 		{
 			newPaddle = makeSprite(getImage("../Resources/Image/blue_paddle.png"));
 			if(position == "right")
 				rightPaddleSpeed -= 3;
 			else
 				leftPaddleSpeed -= 3;
 		}
 		else if(color == 2)
 		{
 			newPaddle = makeSprite(getImage("../Resources/Image/green_paddle.png"));
 			if(position == "right")
 				rightPaddleSpeed += 5;
 			else
 				leftPaddleSpeed += 5;
 		}
 		else 
 		{
 			newPaddle = makeSprite(getImage("../Resources/Image/black_paddle.png"));
 			/*if(position == "right")
 				rightPaddleSpeed += 3;
 			else
 				leftPaddleSpeed += 10;*/
 		}
 		if(rightPaddleSpeed >= 25)
 			rightPaddleSpeed = 25;
 		if(leftPaddleSpeed >= 25)
 			leftPaddleSpeed = 25;
 		if(rightPaddleSpeed <= 8)
 			rightPaddleSpeed = 8;
 		if(leftPaddleSpeed <= 8)
 			leftPaddleSpeed = 8;
 		newPaddle.position(currentX,currentY);
 		return newPaddle;
 	}
 	public void generateBall()
     	{
 		if((timer % 1000) == 0 && num_ball < ball_limit)
 		{
 			int tmpForce = (new Random()).nextInt(4);
 			Sprite tmpBall = makeBall(tmpForce);
 			tmpBall.position(canvas.width()/2 - ball.width()/2, canvas.height()/2 - ball.height()/2);
 			tmpBall.motion((new Random()).nextInt(19)-9, (new Random()).nextInt(7)-3);
 			if(tmpBall.xspeed() == 0)
 				tmpBall.motion(-1, (new Random()).nextInt(7)-3);
 			listBall.add(tmpBall);
 			listBall.add(tmpForce);
 			num_ball += 1;	
 		}
 		
    	}
 	
 }
 
 
