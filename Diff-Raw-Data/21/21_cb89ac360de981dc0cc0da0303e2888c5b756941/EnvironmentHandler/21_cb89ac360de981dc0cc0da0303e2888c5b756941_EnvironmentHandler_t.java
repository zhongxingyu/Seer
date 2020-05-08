 package org.noip.staticattic.world;
 
 import org.noip.staticattic.GUI.MainWindow;
 import org.noip.staticattic.entities.Tile;
 
 public class EnvironmentHandler implements Runnable {
 
 	private MainWindow main;
 	private Environment environment = null;
 	private Environment MainEnvironment;	
 	
 	public EnvironmentHandler(MainWindow main) {
 		
 		this.main = main;
 		
 	}
 	
 	@Override
 	public void run() {
 	
 		if (!main.getPlayer().getCurrentEnvironment().equals(environment)) {
 			
 			environment = main.getPlayer().getCurrentEnvironment();
 			changeEnvironments();
 			
 		}
 		
 	}
 	
 	private void changeEnvironments() {
 		
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 		
 		main.mainpanel.add(main.getPlayer().getLabel());
 		
 		main.getPlayer().getCurrentTile().getLocation().setX(main.getPlayer().getLocation().getX());
 		main.getPlayer().getCurrentTile().getLocation().setY(main.getPlayer().getLocation().getY()+14);
 		
 		main.getPlayer().getCurrentTile().getLabel().setLocation(main.getPlayer().getCurrentTile().getLocation().getX(), main.getPlayer().getCurrentTile().getLocation().getY());
 
 		main.mainpanel.add(main.getPlayer().getCurrentTile().getLabel());
 		
 		int x = 0;
 		int y = 0;
 		
 		for (Tile[] row : array) {
 		   
 			for (Tile e : row) {
 				
 				if (!e.equals(main.getPlayer().getCurrentTile())) {
 				
 					e.getLocation().setX(main.getPlayer().getCurrentTile().getLocation().getX()+((x - main.getPlayer().getXID())*34));
 					e.getLocation().setY(main.getPlayer().getCurrentTile().getLocation().getY()+((y - main.getPlayer().getYID())*34));
 				
 					e.getLabel().setLocation(e.getLocation().getX(), e.getLocation().getY());
 				
 					main.mainpanel.add(e.getLabel());
 				
 				}
				
				if (y == array.length-1) {
					
					y = 0;
					
				} else {
					
					y++;
					
				}
 		    
 			}
 			
 			x++;
 		   
 		}
 		
 		System.out.println(main.getPlayer().getXID()+","+main.getPlayer().getYID());
 		  
 		main.repaint();
 		  
 	}
 	
 	public void moveRight() {
 				
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 		
 		try {
 			
 			main.getPlayer().setCurrentTile(array[main.getPlayer().getXID()+1][main.getPlayer().getYID()]);
 			main.getPlayer().setXID(main.getPlayer().getXID()+1);
 						
 		} catch (IndexOutOfBoundsException e1) {
 			
 			//swallow - edge of map
 			
 		}
 				
 		for (Tile[] row : array) {
 			
 			for (Tile e : row) {
 								
 				e.getLocation().setX(e.getLocation().getX()+34);				
 				e.updatePosition();
 																
 			}
 						
 		}
 		
 		main.repaint();
 		
 	}
 	
 	public void moveLeft() {			
 		
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 			
 		try {
 			
 			main.getPlayer().setCurrentTile(array[main.getPlayer().getXID()-1][main.getPlayer().getYID()]);
 			main.getPlayer().setXID(main.getPlayer().getXID()-1);
 			
 		} catch (IndexOutOfBoundsException e1) {
 			
 			//swallow - edge of map
 			
 		}
 		
 		for (Tile[] row : array) {
 				
 			for (Tile e : row) {
 				
 					
 				
 				e.getLocation().setX(e.getLocation().getX()-34);				
 				e.updatePosition();
 				
 			}			
 		}
 			
 		main.repaint();	
 		
 	}
 	
 	public void moveUp() {
 					
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 			
 		try {
 			
 			main.getPlayer().setCurrentTile(array[main.getPlayer().getXID()][main.getPlayer().getYID()-1]);
 			main.getPlayer().setYID(main.getPlayer().getYID()-1);
 			
 		} catch (IndexOutOfBoundsException e1) {
 			
 			//swallow - edge of map
 			
 		}
 		
 		for (Tile[] row : array) {
 				
 			for (Tile e : row) {
 				
 				e.getLocation().setY(e.getLocation().getY()-34);				
 				e.updatePosition();
 				
 			}		
 		}
 			
 		main.repaint();	
 		
 	}
 	
 	public void moveDown() {
 					
 		Tile[][] array = main.getPlayer().getCurrentEnvironment().getArray();
 			
 		try {
 			
 			main.getPlayer().setCurrentTile(array[main.getPlayer().getXID()][main.getPlayer().getYID()+1]);
 			main.getPlayer().setYID(main.getPlayer().getYID()+1);
 			
 		} catch (IndexOutOfBoundsException e1) {
 			
 			//swallow - edge of map
 			
 		}
 		
 		for (Tile[] row : array) {
 			
 			for (Tile e : row) {					
 				
 					
 				
 				e.getLocation().setY(e.getLocation().getY()+34);				
 				e.updatePosition();
 				
 			}			
 		}
 			
 		main.repaint();	
 		
 	}
 
 	public Environment getMainEnvironment() {
 		
 		return MainEnvironment;
 		
 	}
 
 	public void setMainEnvironment(Environment mainEnvironment) {
 		
 		MainEnvironment = mainEnvironment;
 		
 	}
 
 }
