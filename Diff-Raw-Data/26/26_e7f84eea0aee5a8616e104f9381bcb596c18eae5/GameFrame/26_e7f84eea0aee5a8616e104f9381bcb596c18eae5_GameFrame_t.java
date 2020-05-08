 import javax.swing.JFrame;
 
 /**
  * Represents the JFrame in which the game will reside
  * @author ivan
  */
 public class GameFrame extends JFrame 
 {
 	private ButtonDemo menu;
 	
 	public GameFrame() {
 	    setDefaultCloseOperation(this.EXIT_ON_CLOSE);
 	    setSize(500, 500);
 	    menu = new ButtonDemo(this);
 	    this.shwoMenu();
 //	    this.runGame();
 	    
 	}
 	
 	public void shwoMenu()
 	{	
         //Create and set up the window.
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Create and set up the content pane.
         menu.setOpaque(true); //content panes must be opaque
         this.setContentPane(menu);
         //Display the window.
         this.setVisible(true);
 	}
 	
     void runGame()
     {
    	int defaultLane = 1;
        Lane[] lanes = new Lane[3];
        int x = 152;

        for (int i = 0; i < lanes.length; i++) {
        	lanes[i] = new Lane(50,this.getHeight(),x);
        	x = x + 50;
        }
        
        Car car = new Car(lanes[defaultLane], lanes);
        Car[] cars = new Car[0];
 	    
        DrawComponent dc = new DrawComponent(lanes, car, cars, getHeight());
        
        this.setContentPane(dc);
        this.setFocusTraversalKeysEnabled(true);
        this.setVisible(true);
        MoveCars mc = new MoveCars(car,getHeight(),dc);
 	    
 	    MoveListener kl = new MoveListener(car);
 	    addKeyListener(kl);
 
 	    mc.start();
     }
 }
