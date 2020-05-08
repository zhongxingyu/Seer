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
    	System.out.println("SAAAAAAAAAAAA");
    	Car car = new Car();
	    Car[] cars = new Car[0];
	    
	    DrawComponent dc = new DrawComponent(3, car, cars, getHeight());
 	    
	    this.setContentPane(dc);
	    this.setVisible(true);
	    MoveCars mc = new MoveCars(car,getHeight(),dc);
 	    
 	    MoveListener kl = new MoveListener(car);
 	    addKeyListener(kl);
 
 	    mc.start();
     }
 }
