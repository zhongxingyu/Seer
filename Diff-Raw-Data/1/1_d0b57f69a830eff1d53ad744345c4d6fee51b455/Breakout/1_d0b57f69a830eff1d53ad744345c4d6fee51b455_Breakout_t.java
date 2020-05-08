 package com.jsteadman.Breakout;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.KeyEvent;
 
 import acm.graphics.GLabel;
 import acm.graphics.GObject;
 import acm.graphics.GOval;
 import acm.graphics.GRect;
 import acm.graphics.GRoundRect;
 import acm.program.GraphicsProgram;
 import acm.util.RandomGenerator;
 
 @SuppressWarnings("serial")
 public class Breakout extends GraphicsProgram {
 
 	// constants for applet size
 	private final int APPLET_WIDTH = 400;
 	private final int APPLET_HEIGHT = 600;
 
 	// constants for paddle
 	private final int PADDLE_Y_OFFSET = 30;
 	private double PADDLE_WIDTH;
 	private final int PADDLE_HEIGHT = 10;
 	private double PADDLE_X;
 	private final int PADDLE_Y = APPLET_HEIGHT - PADDLE_Y_OFFSET;
 
 	// constants for bricks
 	private int BRICK_X = 2;
 	private int BRICK_Y = 70;
 	private final int BRICK_ROWS = 10;
 	private final int BRICK_COLUMNS = 10;
 	private final int BRICK_SEPARATION = 4;
 	private final int BRICK_HEIGHT = 8;
 	private final int BRICK_WIDTH = (APPLET_WIDTH / BRICK_COLUMNS)
 			- BRICK_SEPARATION;
 	private static Color BRICK_COLOR;
 
 	// constants for ball
 	private final int BALL_RADIUS = 10;
 	private final int BALL_X = APPLET_WIDTH / 2 - BALL_RADIUS;
 	private final int BALL_Y = APPLET_HEIGHT / 2 - BALL_RADIUS;
 	private final int BALL_DIAMETER = 2 * BALL_RADIUS;
 	private float BALL_DELAY;
 
 	// counter and keeping score
 	private int BRICK_COUNTER = BRICK_COLUMNS * BRICK_ROWS;
 	private int POINTS;
 	GLabel wordScore;
 	GLabel displayPoints;
 
 	// variables for objects
 	private GRect brick;
 	private GOval ball;
 	private GRoundRect paddle;
 
 	// ball velocity
 	private double ballVX = 3;
 	private double ballVY;
 
 	// random generator used to determine initial ball direction
 	RandomGenerator rand = new RandomGenerator();
 
 	// difficulty
 	int difficulty;
 
 	// replay
 	String userResponse;
 
 	// balls remaining
 	GLabel wordBallsRemaining;
 	GLabel ballsRemaining;
 	int BALLS_REMAINING;
 
 	public void run() {
 		setSize(APPLET_WIDTH, APPLET_HEIGHT);
 		addKeyListeners();
 		chooseDifficulty();		
 	}
 
 	/*
 	 * This method is used to call everything needed in order to play the game.
 	 */
 	private void setUpGame() {
 		wordScore();
 		ballsRemaining();
 		trackBallsRemaining();
 		createBricks();
 		theBall();
 		thePaddle();
 		gameCountdown();
 		moveBall();
 	}
 
 	/*
 	 * Here we allow the user to choose a difficulty for playing.
 	 * 
 	 * 1 - easy
 	 * 2 - medium
 	 * 3 - hard
 	 * 
 	 * The values for the ball delay and paddle are changed depending on what
 	 * difficulty is chosen. After a difficulty is chosen and values are set,
 	 * the setUpGame() method is called.
 	 */
 	private void chooseDifficulty() {
 		GLabel diff = new GLabel("Please select a difficulty");
 		diff.setColor(Color.BLUE);
 		diff.setFont(new Font("Arial", Font.PLAIN, 20));
 		diff.setLocation(APPLET_WIDTH / 2 - diff.getWidth() / 2, APPLET_HEIGHT
 				/ 2 - diff.getHeight() / 2);
 		add(diff);
 
 		GLabel easy = new GLabel("Press 1 for EASY");
 		easy.setColor(Color.ORANGE);
 		easy.setFont(new Font("Arial", Font.PLAIN, 15));
 		easy.setLocation(APPLET_WIDTH / 2 - easy.getWidth() / 2, APPLET_HEIGHT
 				/ 2 + easy.getHeight());
 		add(easy);
 
 		GLabel medium = new GLabel("Press 2 for MEDIUM");
 		medium.setColor(Color.CYAN);
 		medium.setFont(new Font("Arial", Font.PLAIN, 15));
 		medium.setLocation(APPLET_WIDTH / 2 - easy.getWidth() / 2,
 				APPLET_HEIGHT / 2 + medium.getHeight() * 2);
 		add(medium);
 
 		GLabel hard = new GLabel("Press 3 for HARD");
 		hard.setColor(Color.RED);
 		hard.setFont(new Font("Arial", Font.PLAIN, 15));
 		hard.setLocation(APPLET_WIDTH / 2 - easy.getWidth() / 2, APPLET_HEIGHT
 				/ 2 + medium.getHeight() * 3);
 		add(hard);
 		
 		GLabel troll = new GLabel("Press 4 for TROLL");
 		troll.setColor(Color.GREEN);
 		troll.setFont(new Font("Arial", Font.PLAIN, 15));
 		troll.setLocation(APPLET_WIDTH / 2 - easy.getWidth() / 2, APPLET_HEIGHT
 				/ 2 + medium.getHeight() * 4);
 		add(troll);
 
 		difficulty = 0;
 
 		while (difficulty == 0) {
 
 			pause(50);
 		}
 
 		if (difficulty == 1) {
 			PADDLE_WIDTH = 60;
 			BALL_DELAY = 15;
 		} else if (difficulty == 2) {
 			PADDLE_WIDTH = 50;
 			BALL_DELAY = 14;
 		} else if (difficulty == 3) {
 			PADDLE_WIDTH = 40;
 			BALL_DELAY = 13;
 		// troll mode just for fun!
 		} else if (difficulty == 4) {
 			PADDLE_WIDTH = 50;
 			BALL_DELAY = 13;
 		}
 		// set starting location for paddle here
 		PADDLE_X = APPLET_WIDTH / 2 - PADDLE_WIDTH / 2;
 		remove(diff);
 		remove(easy);
 		remove(medium);
 		remove(hard);
 		remove(troll);
 		setUpGame();
 
 	}
 
 	private void gameCountdown() {
 		for (int countdown = 4; countdown > 0; countdown--) {
 			GLabel counter = new GLabel("" + countdown);
 			counter.setColor(Color.RED);
 			counter.setFont(new Font("Arial", Font.PLAIN, 25));
 			counter.setLocation(APPLET_WIDTH / 2 - counter.getWidth() / 2,
 					APPLET_HEIGHT / 2 + counter.getHeight() * 2);
 			add(counter);
 			pause(1000);
 			remove(counter);
 
 		}
 	}
 
 	private void createBricks() {
 
 		/*
 		 * adjust the color of the bricks based every two rows
 		 */
 
 		for (int j = 1; j <= BRICK_ROWS; j++) {
 			if (j <= 2) {
 				BRICK_COLOR = Color.RED;
 			} else if (j > 2 && j <= 4) {
 				BRICK_COLOR = Color.ORANGE;
 			} else if (j > 4 && j <= 6) {
 				BRICK_COLOR = Color.YELLOW;
 			} else if (j > 6 && j <= 8) {
 				BRICK_COLOR = Color.GREEN;
 			} else if (j > 8) {
 				BRICK_COLOR = Color.CYAN;
 			}
 			for (int i = 1; i <= (BRICK_COLUMNS); i++) {
 				brick = new GRect(BRICK_WIDTH, BRICK_HEIGHT);
 				brick.setFillColor(BRICK_COLOR);
 				brick.setColor(BRICK_COLOR);
 				brick.setFilled(true);
 				brick.setLocation(BRICK_X, BRICK_Y);
 				BRICK_X += BRICK_WIDTH + BRICK_SEPARATION;
 				add(brick);
 			}
 			/*
 			 * Since the offset changes as the above loop adds a new brick,
 			 * reset it back to the start for each new row that is created.
 			 */
 			BRICK_X = BRICK_SEPARATION / 2;
 			BRICK_Y += BRICK_HEIGHT + BRICK_SEPARATION;
 
 		}
 
 	}
 
 	/*
 	 * Create the paddle.
 	 */
 	private void thePaddle() {
 		paddle = new GRoundRect(PADDLE_WIDTH, PADDLE_HEIGHT);
 		paddle.setFillColor(Color.DARK_GRAY);
 		paddle.setFilled(true);
 		paddle.setLocation(PADDLE_X, PADDLE_Y);
 		add(paddle);
 	}
 	
 	/*
 	 * Troll mode!
 	 * 
 	 * I'm a dirty stinker.
 	 * 
 	 * With this mode active the paddle shrinks half a pixel every time
 	 * a brick is removed. With one brick remaining the paddle will only
 	 * be 0.5 pixels wide. I suppose it's beatable... MAYBE. ;)
 	 */
 	private void changePaddleWidth() {
 		double x = paddle.getX() + 0.25;
 		double y = paddle.getY();
 		
 		remove(paddle);
 		PADDLE_WIDTH -=0.5;
 		paddle = new GRoundRect(PADDLE_WIDTH, PADDLE_HEIGHT);
 		paddle.setFillColor(Color.DARK_GRAY);
 		paddle.setFilled(true);
 		paddle.setLocation(x, y);		
 		add(paddle);;
 		
 	}
 
 	/*
 	 * Handles controlling the paddle with the arrow keys.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see acm.program.Program#keyPressed(java.awt.event.KeyEvent)
 	 */
 	public void keyPressed(KeyEvent e) {
 
 		/*
 		 * A null check is used here since the paddle method has not yet been
 		 * called to prevent the game from crashing.
 		 */
 		if (paddle != null) {
 			double x = paddle.getX();
 			double y = 0;
 
 			switch (e.getKeyCode()) {
 
 			case KeyEvent.VK_RIGHT:
 				if (x < (APPLET_WIDTH - PADDLE_WIDTH) - PADDLE_WIDTH) {
 					paddle.move(PADDLE_WIDTH, y);
 				} else {
 					paddle.move(APPLET_WIDTH - x - PADDLE_WIDTH, y);
 				}
 				break;
 			case KeyEvent.VK_LEFT:
 				if (x > 0 && x > PADDLE_WIDTH) {
 					paddle.move(-PADDLE_WIDTH, y);
 				} else {
 					paddle.move(-x, y);
 				}
 				break;
 			}
 		}
 
 		switch (e.getKeyCode()) {
 		case KeyEvent.VK_1:
 			difficulty = 1;
 			break;
 		case KeyEvent.VK_2:
 			difficulty = 2;
 			break;
 		case KeyEvent.VK_3:
 			difficulty = 3;
 			break;
 		case KeyEvent.VK_4:
 			difficulty = 4;
 			break;
 		case KeyEvent.VK_Y:
 			userResponse = "y";
 			break;
 		case KeyEvent.VK_N:
 			userResponse = "n";
 			break;
 		default:
 			break;
 		}
 	}
 
 	/*
 	 * Create the ball.
 	 */
 	private void theBall() {
 		// launches ball in random direction
 		ballVY = rand.nextDouble(1.0, 3.0);
 		ball = new GOval(BALL_DIAMETER, BALL_DIAMETER);
 		ball.setFillColor(Color.DARK_GRAY);
 		ball.setFilled(true);
 		ball.setLocation(BALL_X, BALL_Y);
 		add(ball);
 	}
 
 	/*
 	 * This accounts for all four "corners" of the ball and returns each element
 	 * that the ball interacts with. If no element is detected, return null.
 	 */
 	private GObject detectCollision() {
 		if (getElementAt(ball.getX(), ball.getY()) != null) {
 			return getElementAt(ball.getX(), ball.getY());
 		} else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()) != null) {
 			return getElementAt(ball.getX() + BALL_DIAMETER, ball.getY());
 		} else if (getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER) != null) {
 			return getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER);
 		} else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()
 				+ BALL_DIAMETER) != null) {
 			return getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()
 					+ BALL_DIAMETER);
 		} else {
 			return null;
 		}
 
 	}
 
 	/*
 	 * Here we make the ball move. First we make it change directions when it
 	 * touches the walls and ceiling. Then we use the detectCollision method to
 	 * determine what object the ball has hit. If that object is not the paddle,
 	 * then we remove it. The infinite loop is broken when either all the bricks
 	 * are destroyed or the ball touches the bottom of the screen.
 	 */
 	private void moveBall() {
 		boolean play = true;
 
 		while (play) {
 			// bounce ball off walls and ceiling
 			if (ball.getX() >= APPLET_WIDTH - BALL_DIAMETER) {
 				ballVX = -Math.abs(ballVX);
 			}
 			if (ball.getX() <= 0) {
 				ballVX = Math.abs(ballVX);
 			}
 			if (ball.getY() <= 0) {
 				ballVY = Math.abs(ballVY);
 			}
 			GObject collider = detectCollision();
 			if (collider == paddle && ballVY > 0) {
 				ballVY = -Math.abs(ballVY);
 			} else if (collider == paddle && ballVY < 0) {
 
 				// do nothing for score, points and balls remaining
 			} else if (collider == wordScore || collider == displayPoints
 					|| collider == wordBallsRemaining
 					|| collider == ballsRemaining) {
 
 				// handle the bricks
 			} else if (collider != null) {
 				if (ballVY > 0) {
 					ballVY = -Math.abs(ballVY);
 				} else if (ballVY < 0) {
 					ballVY = Math.abs(ballVY);
 				}
 
 				/*
 				 * Increase ball velocity
 				 */
 				BALL_DELAY -= 0.05;
 				/*
 				 * Call method to change paddle width on troll difficulty.
 				 */
 				if (difficulty == 4) {
 					changePaddleWidth();
 				}
 				/*
 				 * Count down from the total number of bricks each time one is
 				 * removed.
 				 */
 				BRICK_COUNTER--;
 				/*
 				 * The displayPoints must first be removed before setting the
 				 * new value. Otherwise, the new value is always written on top
 				 * of the previous.
 				 */
 				remove(displayPoints);
 				/*
 				 * The GObejct collider is sent to the track points method so we
 				 * can get the color of the object for tracking the points.
 				 */
 				trackPoints(collider);
 				/*
 				 * Remove the brick.
 				 */
 				remove(collider);
 				if (BRICK_COUNTER == 0) {
 					play = false;
 				}
 				/*
 				 * Break the while loop if the ball touches the bottom of the
 				 * screen thus ending the game.
 				 */
 			} else if (ball.getY() > APPLET_HEIGHT - BALL_DIAMETER) {
 				if (BALLS_REMAINING > 0) {
 					BALLS_REMAINING--;
 					remove(ballsRemaining);
 					trackBallsRemaining();
 					remove(ball);
 					theBall();
 					pause(500);
 				} else {
 					play = false;
 				}
 			}
 
 			// move the ball
 			ball.move(ballVX, ballVY);
 			// set the speed of the moving ball
 			pause(BALL_DELAY);
 		}
 
 		// Call the endGame() method if the while loop is broken
 		endGame();
 	}
 
 	/*
 	 * Use two separate methods for the score. One for just the word "Score" and
 	 * another to track the points. This is so we don't redraw the word every
 	 * time we score which causes a minor blip. It looks much cleaner.
 	 */
 	private void wordScore() {
 		/*
 		 * We have to set the initial points to zero here. If we don't then
 		 * there is nothing to remove and the program will crash.
 		 */
 		POINTS = 0;
 		displayPoints = new GLabel("" + POINTS);
 		displayPoints.setLocation(65, 25);
 		displayPoints.setFont(new Font("Arial", Font.PLAIN, 20));
 		add(displayPoints);
 		/*
 		 * This just adds the word "Score" which is never changed.
 		 */
 		wordScore = new GLabel("Score: ");
 		wordScore.setLocation(5, 25);
 		wordScore.setFont(new Font("Arial", Font.PLAIN, 20));
 		add(wordScore);
 
 	}
 
 	/*
 	 * This method keeps track of the points accumulated. We use the getColor()
 	 * method to return the color of the collider and adjust the points based on
 	 * that color.
 	 */
 	private void trackPoints(GObject collider) {
 
 		BRICK_COLOR = collider.getColor();
 
 		if (BRICK_COLOR == Color.CYAN) {
 			POINTS += 10;
 		} else if (BRICK_COLOR == Color.GREEN) {
 			POINTS += 20;
 		} else if (BRICK_COLOR == Color.YELLOW) {
 			POINTS += 30;
 		} else if (BRICK_COLOR == Color.ORANGE) {
 			POINTS += 40;
 		} else if (BRICK_COLOR == Color.RED) {
 			POINTS += 50;
 		}
 
 		displayPoints = new GLabel("" + POINTS);
 		displayPoints.setLocation(65, 25);
 		displayPoints.setFont(new Font("Arial", Font.PLAIN, 20));
 		add(displayPoints);
 	}
 	
 	/*
 	 * Just like with the score, we display just the word Balls Remaining and
 	 * adjust the number independently.
 	 */
 	private void ballsRemaining() {
 		BALLS_REMAINING = 2;
 
 		wordBallsRemaining = new GLabel("Balls Remaining: ");
 		wordBallsRemaining.setLocation(235, 25);
 		wordBallsRemaining.setFont(new Font("Arial", Font.PLAIN, 20));
 		add(wordBallsRemaining);
 
 	}
 	
 	/*
 	 * This method keeps track of the number of balls remaining.
 	 */
 	private void trackBallsRemaining() {
 		ballsRemaining = new GLabel("" + BALLS_REMAINING);
 		ballsRemaining.setLocation(385, 25);
 		ballsRemaining.setFont(new Font("Arial", Font.PLAIN, 20));
 		add(ballsRemaining);
 	}
 
 	/*
 	 * This handles what action to take once the game ends.
 	 */
 	private void endGame() {
 		GLabel end;
 		/*
 		 * If the game ends and the brick counter is 0 then all the bricks have
 		 * been removed and the user has won the game.
 		 */
 		if (BRICK_COUNTER == 0) {
 			end = new GLabel("Congratulations! You won!");
 			end.setFont(new Font("Arial", Font.BOLD, 20));
 			end.setColor(Color.BLUE);
 			/*
 			 * If there are bricks remaining when the game has ended then the
 			 * game is lost.
 			 */
 		} else {
 			// a tribute to Hudson
 			end = new GLabel("Game Over, Man!");
 			end.setFont(new Font("Arial", Font.BOLD, 20));
 			end.setColor(Color.RED);
 		}
 		end.setLocation(getWidth() / 2 - end.getWidth() / 2, getHeight() / 2
 				- end.getHeight() / 2);
 		add(end);
 		pause(500);
 		replayGame();
 	}
 	
 	/*
 	 * This method is called at the end of the endGame() method. It asks the user
 	 * whether they would like to replay or not. If they choose yes, everything is
 	 * removed and the Y location for the bricks is reset. Then the chooseDifficulty()
 	 * method is re-called and everything resumes like when the game is initially
 	 * started. If the user says no they do not want to replay, the game closes.
 	 */
 	private void replayGame() {
 		GLabel replay = new GLabel("Would you like to play again?" + "\n"
 				+ "Press Y or N");
 		replay.setFont(new Font("Arial", Font.PLAIN, 20));
 		replay.setLocation(getWidth() / 2 - replay.getWidth() / 2, getHeight()
 				/ 2 + replay.getHeight());
 		add(replay);
 
 		userResponse = "";
 
 		while (userResponse != "y") {
 
 			pause(50);
 
 			if (userResponse == "y") {
 				removeAll(); // remove everything on the screen
 				BRICK_Y = 70; // reset Y location of bricks for loop
 				chooseDifficulty();
 			}
 			if (userResponse == "n") {
 				System.exit(0);
 			}
 
 		}
 
 	}
 
 }
