 package asrs;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import order.Location;
 import order.Product;
 
 import asrsController.ExecutionManager;
 
 import tspAlgorithm.TSPAlgorithm;
 
 public class OrderPickingPanel extends JPanel implements Runnable {
 	private ExecutionManager WMan = new ExecutionManager();
 	private Thread runner;
 	int pause = 1000;
 	Location robotLoc;
 	Location destination = new Location (10,8);
 	
 	public OrderPickingPanel(TSPAlgorithm tsp){
 		super();
 		setSize(300,500);
 		WMan.run(tsp);
 	}
 	@Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		
 		g.setColor(Color.BLACK);
 		
 		//Hier wordt de BinPacking robot (blok) en het magazijn getekend.
 		g.drawRect(0, 340, 60, 60);
 		for(int y = 0; y < 20; y++){
 			for(int x = 0; x < 10; x++){
 				g.drawRect(60+(x*20), 0+(y*20), 20, 20);
 			}
 		}
 		
 		//Hier wordt de inhoud van het warehouse opgehaald.
 //		DBHandler db = new DBHandler();
 //		ArrayList<Location> locations = db.getAllOccupiedLocations();
 		
 		//Aanmaken test gegevens
 		
 		ArrayList<Product> warenhuis = new ArrayList<Product>();
		Location location1 = new Location(1,1);
 		Product product1 = new Product(1, "fiets", 1, 1, location1);
 		
 		Location location2 = new Location(6,2);
 		Product product2 = new Product(2, "fiets", 1, 5, location2);
 		
 		Location location3 = new Location(9,11);
 		Product product3 = new Product(3, "fiets", 1, 8, location3);
 		
		Location location4 = new Location(10,20);
 		Product product4 = new Product(4, "fiets", 1, 10, location4);
 		
 		warenhuis.add(product1);
 		warenhuis.add(product2);
 		warenhuis.add(product3);
 		warenhuis.add(product4);
 		
 		//Tekenen van de producten in het warenhuis
 		
 		for(Product product : warenhuis){
 			Location loc = product.getLocation();
			if(loc.x <= 20 && loc.y <= 30){
				g.fillRect(43+(loc.x*20), (loc.y*20)+3, 15, 15);
 			}
 		}
 		
 		//Tekenen robot
 		
 		if(robotLoc != null) g.drawRect(41+(robotLoc.x*20), (robotLoc.y*20)+1, 18, 18);
 		
 		//Tekenen doel
 		g.setColor(Color.blue);
 		if(destination != null) g.drawRect(42+(destination.x*20), (destination.y*20)+2, 16, 16);
 		//Ophalen van de gegeven order
 		//TO-DO
 		
 		//Testgegevens
 		
 //		ArrayList<Product> order = new ArrayList<Product>();
 //		order.add(product1);
 //		order.add(product2);
 //		order.add(product3);
 	}
 	
 	public void start(){
 		if (runner == null){
 			runner = new Thread(this);
 			runner.start();
 		}
 	}
 	
 	@Override
 	public void run() {
 		Thread thisThread = Thread.currentThread();
 		robotLoc = new Location (0,0);
 		while (runner == thisThread) {
 			repaint();
 			
 			//Location 1 is de huidige postitie, location2 is het doel.
 			if(robotLoc.getDistanceTo(destination) > 0){
 				move();
 				System.out.println("moved");
 			}
 			try {
 				Thread.sleep(pause);
 			} catch (InterruptedException e) { }
 //			stop();
 			
 		}
 	}
 
 	public void stop(){
 		if(runner != null){
 			runner = null;
 			System.out.println("stopping");
 		}
 	}
 	
 	private void move(){
 		int distx = destination.x - robotLoc.x;
 		int disty = destination.y - robotLoc.y;
 		int compy;
 		int compx;
 		
 		if(robotLoc == destination){
 			return;
 		}
 		System.out.println("Coordinaten: " + robotLoc.x + " " + robotLoc.y + " en " + destination.x + " " + destination.y);
 		if (disty < 0) compy = disty * -1;
 		else compy = disty;
 		if (distx < 0) compx = distx * -1;
 		else compx = distx;
 		if (compy < compx) {
 			robotLoc.x = robotLoc.x + 1;
 			System.out.println("Moved right");
 		}
 		if (compy > compx) {
 			robotLoc.y = robotLoc.y + 1;
 			System.out.println("Moved down");
 		}
 		if (compy == compx){
 			robotLoc.y = robotLoc.y +1;
 			System.out.println("Moved down");
 		}
 		System.out.println("Afstanden: " + compx + " " + compy);
 	}
 }
