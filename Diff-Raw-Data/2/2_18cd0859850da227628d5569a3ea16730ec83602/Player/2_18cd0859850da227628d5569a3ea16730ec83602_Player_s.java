 package gameObjects;
 
 import java.util.ArrayList;
 
 import weapons.Weapon;
 
 import com.golden.gamedev.Game;
 
 import decorator.DecoratedShip;
 import decorator.MovementFactory;
 import levelLoadSave.ForSave;
 
 @ForSave
 public class Player extends GameObject {
 
 	private static DecoratedShip decorations;
 	private ArrayList<String> myDecs = new ArrayList<String>();
 	MovementFactory decman = null;
 	protected int myHealth; 
 	protected ArrayList<Weapon> myWeapons;
     
 	public Player(double x, double y, String imgPath){
         myX = x;
         myY = y;
         myImgPath = imgPath;
         myType = "Player";
         setLocation(myX, myY);
         myWeapons = new ArrayList<Weapon>();
         myHealth = 10;
         this.createDecorator(); 
         //myDecs = startDecorations; 
         this.addDecorationCollection(myDecs); 
     }
 	
     public Player(double x, double y, String imgPath, ArrayList<String> startDecorations){
         myX = x;
         myY = y;
         myImgPath = imgPath;
         myType = "Player";
         setLocation(myX, myY);
         myWeapons = new ArrayList<Weapon>();
         myHealth = 10;
         this.createDecorator(); 
         myDecs = startDecorations; 
         this.addDecorationCollection(myDecs); 
     }
     
     
     public void move(){
 		decorations.move(this);
     }
     
     
     public DecoratedShip getDecorations(){
     	return decorations; 
     }
     private void createDecorator(){
 		try {
 			decman = new MovementFactory();
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
     }
     
     public void addDecoration(String decorator){
     	myDecs.add(decorator); 
 		try {
 			decman.addDecorators(myDecs);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		decorations = decman.getDecorators();
 		
 		System.out.println("added " + decorations);
     }
     
     public void addDecorationCollection(ArrayList <String> incomingDecs){
     	myDecs = incomingDecs; 
     	try {
 			decman.addDecorators(myDecs);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		decorations = decman.getDecorators();
         myHealth = 10; 
     }
     
     public String getImgPath()
     {
     	return myImgPath;
     }
 
     @Override
     public GameObject makeGameObject(GameObjectData god) {
     	System.out.println("entered makeGameObject");
         Double x = god.getX();
         Double y = god.getY();
         String imgPath = god.getImgPath();
         ArrayList<String> incomingDecorations = god.getDecorations(); 
         return new Player(x, y, imgPath, incomingDecorations);
     }
 
     
     public void reduceHealth(int damage){
 		myHealth -= damage; 
 	}
 	
 	public int getHealth(){
 		return myHealth; 
 	}
 	
 	public void addWeapon(Weapon w){
 		myWeapons.add(w); 
 	}
 	
 	public void fire(Game g, long elapsedTime){
		if(g.keyPressed(java.awt.event.KeyEvent.VK_SPACE)){
 			myWeapons.get(0).fire(elapsedTime, this.getX(), this.getY()); 
 		}
 	}
     
     /**
      * Player() and getFactory() must be implemented by each game object; 
      * they are used for the factory system.
      */
     protected Player() {
         myType = "Player";
     }
     
     public static GameObjectFactory getFactory() {
         return new GameObjectFactory(new Player());
     }
 }
