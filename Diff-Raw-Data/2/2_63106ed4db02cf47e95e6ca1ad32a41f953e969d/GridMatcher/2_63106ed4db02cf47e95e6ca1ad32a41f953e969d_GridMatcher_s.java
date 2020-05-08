 import java.util.Random;
 import ucigame.*;
 
 public class GridMatcher extends Ucigame {
 
 	private static final long serialVersionUID = -7166809080197331440L;
 	private boolean gameStart;
 	private Sprite startButton;
 	private Sprite matchee;
 	private int matcheeNo;
 	private final int[] grid = {39, 139, 239, 339};
 	private Sprite[][] gridSprites;
 	private long popTime;
 	private int numberOfShapes = 36;
 	private Random rand;
 	private int score;
 	private int scoreMultiplier;
 	private int multiplierCounter;
 	private long start;
 	private long end;
 	private int endCondition;
 	private boolean endGame;
 	private double lastPopTimeDecrease;
 	private long startQueue;
 
 	public void setup() {
 		resize(800, 500);
 		window.size(800, 500);
 		window.title("Grid Matcher");
 		framerate(30);
 		window.showFPS();
 		
 		canvas.background(getImage("images/HUD_fade.png"));
 		canvas.font("Times New Roman", 1, 35);
 		matchee = setMatchee(0);
 		
 		gameStart = false;
 		startButton = makeButton("StartButton", getImage("images/start.png"), 200, 100);
 		startButton.position(300, 200);
 		
 		gridSprites = new Sprite[4][4];
 		popTime = 10000;
 		rand = new Random();
 		score = 0;
 		scoreMultiplier = 1;
 		multiplierCounter = 0;
 		endCondition = 0;
 		endGame = false;
 		lastPopTimeDecrease = 0;
 	}
 
 	public void draw() {
 		canvas.clear();
 
 		if(gameStart) {
 			matchee.draw();
 			canvas.putText(score, 120, 480);
 			canvas.putText(getElapsedStartTime(), 305, 477);
 			canvas.putText((1000+popTime-getElapsedQueueTime())/1000, 590, 320);
 			for(Sprite[] a : gridSprites) {
 				for(Sprite s : a) {
 					if(s!=null) {
 						s.draw();
 					}
 				}
 			}
 		}
 		else {
 			if(endGame) {
 				matchee.draw();
 				canvas.putText(score, 120, 480);
 				canvas.putText(getElapsedStartTime(end), 305, 477);
 				for(Sprite[] a : gridSprites) {
 					for(Sprite s : a) {
 						if(s!=null) {
 							s.draw();
 						}
 					}
 				}
 			}
 			startButton.draw();
 		}
 	}
 
 	private void populateGrid() {
 		int row = rand.nextInt(4);
 		int col = rand.nextInt(4);
 		for(int i=0;i < 4;i++) {
 			for(int j=0;j < 4;j++) {
 				if(i==row && j==col) {
 					//place one correct answer
 					setCorrect(row, col);
 				}
 				else {
 					//and the rest as incorrect
 					gridSprites[i][j] = getIncorrectIcon(i, j);
 				}
 			}
 		}
 	}
 	
 	private void destroyGrid() {
 		for(Sprite[] a : gridSprites) {
 			for(Sprite s : a) {
 				s.hide();
 			}
 		}
 	}
 	
 	//Button Click Methods...
 	public void onClickStartButton() {
 		canvas.background(getImage("images/HUD.png"));
 		start = System.currentTimeMillis();
 		gameStart = true;
 		populateGrid();
 		popTime = 10000;
 		startTimer("popQueue", popTime);
 		endCondition = 0;
 		startQueue = System.currentTimeMillis();
 		startButton.hide();
 	}
 	public void onClickCorrect() {
 		//System.out.println("You got it!");
 		if(++multiplierCounter >= 10) {
 			scoreMultiplier++;
 			multiplierCounter = 0;
 			//System.out.println("score multiplier is now "+scoreMultiplier+"!");
 		}
 		score += 50*scoreMultiplier;
 		//System.out.println("You earned 50x"+scoreMultiplier+" points!");
 		resetQueue();
 		matchee = setMatchee(rand.nextInt(numberOfShapes));
 		destroyGrid();
 		populateGrid();
 	}
 	public void onClickIncorrect() {
 		multiplierCounter = 0;
 		try {
 		mouse.sprite().pin(makeSprite(getImage("images/icons/xout.png", 252, 252, 252)), 0, 0);
 		}
 		catch(NullPointerException e) {
 		}
 		//System.out.println("Nope, try again...");
 		//System.out.println(10-multiplierCounter+" more correct until another multiplier bonus!");
 	}
 
 	//Timer Methods...
 	public void popQueueTimer() {
 		//System.out.println("POP THE QUEUE!");
 		if(++endCondition >= 5) {
 			end = System.currentTimeMillis();
 			endGame = true;
 			gameStart = false;
 			stopTimer("popQueue");
			canvas.background(getImage("images/HUD fade.png"));
 			startButton.show();
 		}
 		else {
 			resetQueue();
 			matchee = setMatchee(rand.nextInt(numberOfShapes));
 			destroyGrid();
 			populateGrid();
 		}
 	}
 	private void resetQueue() {
 		if(lastPopTimeDecrease != Math.floor(getElapsedStartTime()/20)*1000) {
 			popTime -= 1000;
 			lastPopTimeDecrease = Math.floor(getElapsedStartTime()/20)*1000;
 		}
 		if(popTime <= 2000) {
 			popTime = 2000;
 		}
 		stopTimer("popQueue");
 		startTimer("popQueue", popTime);
 		startQueue = System.currentTimeMillis();
 		//System.out.println("popTime is now: "+popTime/1000+" seconds");
 	}
 	
     private long getElapsedStartTime() {
         return (System.currentTimeMillis() - start)/1000;
     }
     private long getElapsedStartTime(long end) {
         return (end - start)/1000;
     }
     private long getElapsedQueueTime() {
     	return System.currentTimeMillis() - startQueue;
     }
 	
 	//Setters, Getters, and Makers
 	private Sprite setMatchee(int imageNo) {
 		matcheeNo = imageNo;
 		Sprite queueImage = makeSprite(getImage("images/queue/shape"+imageNo+".png", 255));
 		queueImage.position(485, 35);
 		return queueImage;
 	}
 	private Sprite setCorrect(int row, int col) {
 		Sprite icon = makeButton("Correct", getImage("images/icons/shape"+matcheeNo+".png", 255), 90, 90);
 		icon.position(grid[row], grid[col]);
 		gridSprites[row][col] = icon;
 		return icon;
 	}
 	private Sprite getIncorrectIcon(int row, int col) {
 		int number = rand.nextInt(numberOfShapes);
 		while(number == matcheeNo) {
 			number = rand.nextInt(numberOfShapes);
 		}
 		Sprite icon = makeButton("Incorrect", getImage("images/icons/shape"+number+".png", 255), 90, 90);
 		icon.position(grid[row], grid[col]);
 		return icon;
 	}
 	
 }
