 package edu.ucsb.cs56.projects.games.memorycard;
 
 import java.awt.*;
 import java.awt.event.*; // for ActionListener and ActionEvent
 import javax.swing.*;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.lang.Math;
 
 /**
  * A Swing component for playing the Memory Card Game
  @author Bryce McGaw and Jonathan Yau (with some of Phill Conrad's code as a basis)
  @author Ryan Halbrook and Yun Suk Chang
  @version CS56 Spring 2013
  @see MemoryGrid 
 */
 public class MemoryGameComponent extends JComponent implements ActionListener
 {
     
     private JButton []        buttons;
     private ArrayList<Icon>   imgIcons = new ArrayList<Icon>();
     public  JComponent        restartB = new JButton("Restart");
     private Icon              imgBlank;
     
     private MemoryGrid        grid;
     private int               currentLevel;
     private MemoryGameLevel[] levels;
     private MemoryGameLevel   level             = new MemoryGameLevel(36, 100, 2000);
     private long              startTime         = 0;
     private boolean           cheatEnabled      = false; // Cheat code related.
     private boolean	      isOver            = false; // Cheat code related.
     private int               gameCounter       = 0;
 
     private JLabel            timeLabel         = null;
     private boolean           firstImageFlipped = false;
     private Timer             timer;
     private JButton	      pauseButton;
     // For pausing. pausing just stops the timer and the play
     // time is computed as final time minus start time.
     // Therefore, this total pause time is used to
     // adjust the elapsed time to the actual play time.
     private long pauseTime = 0;
     private long pauseStart;
 
     // Pause the game
     public void pauseB() {
 	pauseStart = new Date().getTime();
 	timer.stop();
         JOptionPane popup = new JOptionPane("PAUSED");
         Object[] options= {"Resume"};
 
         int selection=popup.showOptionDialog(
 					     null,
 					     "GAME PAUSED",
 					     "PAUSED",
 					      JOptionPane.OK_CANCEL_OPTION,
 					      JOptionPane.INFORMATION_MESSAGE, null,
 					      options, options[0]);
 	
         if(selection==JOptionPane.YES_OPTION)
 	{
 		resume();
 	}
     }
     public void pause() {
 	pauseStart = new Date().getTime();
 	timer.stop();
  
     }
 
     // resume the game
     public void resume() {
 	long currentTime = new Date().getTime();
 	pauseTime += currentTime - pauseStart;
 	timer = new Timer(1000, this);
 	timer.start();
     }
 
     private void updateTimeLabel(long minutes, long seconds) {
 	String m = " minutes, ";
 	if (minutes == 1) m = " minute, ";
 	if (minutes > 0) {
 	    timeLabel.setText("Time Remaining: " + minutes + m + seconds + " seconds");
 	} else {
 	    timeLabel.setText("Time Remaining: " + seconds + " seconds");
 	}
     }
 
     public void actionPerformed(ActionEvent e) {
 	long finalTime = new Date().getTime();
 	long deltaTime = (long)((finalTime - startTime) / 1000.0);
 	long timeRemaining = (long)(level.getSecondsToSolve() - deltaTime + pauseTime  / 1000.0);
 	
 	if (timeRemaining < 0) {
 	    endGame();
 	}
 	if (timeRemaining < 0)
 	    timeRemaining = 0;
 
         updateTimeLabel(timeRemaining / 60, timeRemaining % 60);
     }
 
     public void setLabel(JLabel label) {
 	this.timeLabel = label;
     }
     public void setPauseButton(JButton b){
 	pauseButton=b;
 	pauseButton.setEnabled(false);
     }
     /**
        Loads a basic set of levels for the game
     */
     private void loadLevelSet1() {
 	levels = new MemoryGameLevel[3];
 	levels[0] = new MemoryGameLevel(16, 75, 1500);
 	levels[1] = new MemoryGameLevel(36, 300, 1000);
 	levels[2] = new MemoryGameLevel(36, 150, 500);
 	currentLevel = 0;
 	level = levels[currentLevel];
     }
 
     // The first 8 images. These will be used
     // for a 4 by 4 game.
     private String[] images8 = {
 	"/images/200.jpg", "/images/201.jpg",
 	"/images/202.jpg", "/images/203.jpg",
 	"/images/204.jpg", "/images/205.jpg",
 	"/images/206.jpg", "/images/207.jpg",
     };
     // The next ten images. These, in addition
     // to the first 8 will be used for a
     // 6 by 6 game.
     private String[] images10 = {
 	"/images/208.jpg", "/images/209.jpg",
 	"/images/210.jpg", "/images/211.jpg",
 	"/images/212.jpg", "/images/213.jpg",
 	"/images/214.jpg", "/images/215.jpg",
        	"/images/216.jpg", "/images/217.jpg",
         
     };
 
     /** Constructor
 	
 	@param game an object that implements the MemoryGrid interface
 	to keep track of the moves in each game, ensuring the rules are
 	followed and detecting when the user has won.
     */
     public MemoryGameComponent(MemoryGrid game) {
 	super(); 
 	timeLabel = new JLabel("Time Remaining");
 	timer = new Timer(250, this);
 
 	this.grid = game;
         int gridSize = grid.getSize();
 	buttons= new JButton[gridSize];
 	
 	loadLevelSet1();
 	loadImageIcons(); // loads the array list of icons and sets imgBlank
 	buildTiles();
 	startTime = new Date().getTime();
     }
 
     public void buildTiles() {
 	this.removeAll();
 	int gridSize = grid.getSize();
 
 	//set layout to a grid of length sqrt(grid size)
 	this.setLayout(new GridLayout(0,(int)Math.sqrt(gridSize))); 
 	buttons = new JButton[gridSize];
 	for(int i=0; i<=(gridSize-1); i++) {
 	    //initially all buttons are blank
 	    JButton jb = new JButton(imgBlank);   
 	    
 	    buttons[i] = jb;
 	    jb.addActionListener(new ButtonListener(i));
 
 	    //get rid of annoying boxes appearing around icon next to clicked icon
 	    jb.setFocusPainted(false);          
 	    
 	    this.add(jb);  
 	}
 	this.repaint();
 	this.validate();
 	startTime = new Date().getTime();
     }
 
     class ButtonListener implements ActionListener {
 	private int num;
 
 	public ButtonListener(int i) {
 	    super();
 	    this.num = i;
 	}
 
         @Override
 	public void actionPerformed (ActionEvent event) {
 	    
 	    Class classs = this.getClass();
 	    Icon imgBlank = new ImageIcon(
 					  classs.getResource("/images/000.jpg"));
             
 	    //if 2 MemoryCards are flipped, flip back over
 	    flipBack();
             //if no MemoryCards are flipped, flip one
             if (!grid.isOneFlipped()){
 		if (!firstImageFlipped) {
 		    startTime = (new Date().getTime());
 		    timer.start();
 		    firstImageFlipped = true;
 		    pauseButton.setEnabled(true);
 		}
 		grid.flip(num);
 		JButton jb = buttons[num];
 		Icon i = imgIcons.get(grid.getVal(num)-1);
 		jb.setIcon(i);            //set image according to val
 		if(num!=1)                //cheat code
 		    jb.setEnabled(false); //make unclickable
                 else
 		    //cheat code. Needs to override the button so that button is same color as regular button.
 		    cheatEnabled=true;	
 	    }
 
             //if one MemoryCard is flipped, flip other
             //then check if theyre matching
             else{
 		if((num==1&&cheatEnabled))//cheat code
 		    {
 			cheatEnabled=false;
                         isOver=true;
 		        endGame();
 			return;
 		    }
 		cheatEnabled=false;//cheat code
                 grid.flip(num);
                 JButton jb = buttons[num];
 		
 		jb.setIcon(imgIcons.get(grid.getVal(num)-1));      //set image according to val
       
                 jb.setEnabled(false);
                 if (grid.flippedEquals(num)){    //if they're matching keep num displayed and set flipped as false
                     gameCounter++;
                     grid.flip(num); 
                     grid.flip(grid.getFlipped());
                     
                     //check if game is over
                     if(gameCounter==grid.getSize()/2){
 			isOver=true;
 		        endGame();
                     } 
                 } else {
 		    // start the flip back timer
 		    int delay = level.getFlipTime();
 		    ActionListener listener = new ActionListener() {
 			    public void actionPerformed(ActionEvent e) { flipBack(); }
 			};
 		    Timer t = new Timer(delay, listener);
 		    t.setRepeats(false);
 		    t.start();
 		} // end of inner if else
 		
             } // end of outer if else
         }
     }
     
     /**
        Starts a new level or restarts the current level
     */
     public void newGame(int lvl) {
 	gameCounter = 0;
 	if (currentLevel < levels.length) {
 	    currentLevel=lvl;
 	    level = levels[currentLevel];
 	}
 	int gridSize = level.getGridSize();
 	grid = new MemoryGrid(gridSize);
 	buildTiles();
 	if (timer != null) timer.stop();
 	
 	firstImageFlipped = false;
 	pauseButton.setEnabled(false);
     }
     public void reset() {
 	pauseTime = 0;
 	updateTimeLabel(level.getSecondsToSolve() / 60, level.getSecondsToSolve() % 60);	    
  	newGame(currentLevel);
 	firstImageFlipped = false;
 	pauseButton.setEnabled(false);
     }
 
     /**
        Ends the game and starts a new game if the user selects new game
        from a dialog menu.
      */
     public void endGame() {
 	
 	timer.stop();
 	long finalTime = new Date().getTime();
	long deltaTime = (long)((finalTime - startTime) / 1000.0) - pauseTime * 1000;
 	pauseTime = 0;
         grid.isOver=true;
                         
 	if (deltaTime < level.getSecondsToSolve()&&currentLevel<2) {
 	    JOptionPane popup = new JOptionPane("Good Job!");
 	    Object[] options= {"Continue","Quit"};
 
 	    int selection=popup.showOptionDialog(
 						 null,
 						 "-~*´¨¯¨`*·~-.¸-  You beat the level!!  -,.-~*´¨¯¨`*·~-",
 						 "Good Job!",
 						 JOptionPane.YES_NO_OPTION,
 						 JOptionPane.INFORMATION_MESSAGE, null,
 						 options, options[0]);
 	
             if(selection==JOptionPane.YES_OPTION)
 		{
 		    long time = levels[currentLevel+1].getSecondsToSolve();
 		    updateTimeLabel(time / 60, time % 60);
 		    newGame(currentLevel+1);
 		}
 	    else	    
 		System.exit(0);
 
 	}
         else if(deltaTime < level.getSecondsToSolve()&&currentLevel==2){
 	    JOptionPane popup = new JOptionPane("Good Job!");
 	    Object[] options= {"Play Again?","Quit"};
 	    int selection=popup.showOptionDialog(
 						 null,
 						 "-~*´¨¯¨`*·~-.¸-  You beat the game!!  -,.-~*´¨¯¨`*·~-",
 						 "Good Job!",
 						 JOptionPane.YES_NO_OPTION,
 						 JOptionPane.INFORMATION_MESSAGE, null,
 						 options, options[0]);
 	    if(selection==JOptionPane.YES_OPTION)
 		{
 		    long time = levels[0].getSecondsToSolve();
 		    updateTimeLabel(time / 60, time % 60);
 		    newGame(0);
 		}
 	    else
 		System.exit(0);
 	
 	} 
         else {
 	    JOptionPane popup = new JOptionPane("Game Over");
 	    Object[] options= {"Try Again?","Quit"};
 	    int selection=popup.showOptionDialog(
 						 null,
 						 "Please Try Again",
 						 "Game Over",
 						 JOptionPane.YES_NO_OPTION,
 						 JOptionPane.INFORMATION_MESSAGE, null,
 						 options, options[0]);
 	    if(selection==JOptionPane.YES_OPTION)
 		{
 		    long time = level.getSecondsToSolve();
 		    updateTimeLabel(time / 60, time % 60);
 		    newGame(currentLevel);
 		}
 	    else
 		System.exit(0);
 	}
     }
 
     /**
        If two cards are showing, flips them back over
     */
     public void flipBack() {
 	
 	if(grid.isTwoFlipped()){
 	    JButton jb = buttons[grid.getFlipped()];
 	    jb.setEnabled(true);
 	    jb.setIcon(imgBlank);
 	    grid.flip(grid.getFlipped());
 	    jb = buttons[grid.getFlipped()];
 	    jb.setEnabled(true);
 	    jb.setIcon(imgBlank);
 	    grid.flip(grid.getFlipped());
 
 	}
     }
 
     public void loadImageIcons() {
 	//get the current classloader (needed for getResource method..  )
 	//                            (which is required for jws to work)
 	//ClassLoader classLoader = this.getClass().getClassLoader();
 	Class classs = this.getClass();
 	//load Icons 
 	for (String image : images8) {
 	    imgIcons.add(new ImageIcon(classs.getResource(image)));
 	}
 	for (String image : images10) {
 	    imgIcons.add(new ImageIcon(classs.getResource(image)));
 	}
 	imgBlank = new ImageIcon(classs.getResource("/images/000.jpg"));
     }
 }
