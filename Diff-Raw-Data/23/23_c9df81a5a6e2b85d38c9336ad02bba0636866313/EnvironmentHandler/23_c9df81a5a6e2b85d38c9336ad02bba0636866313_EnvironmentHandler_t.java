 package org.noip.staticattic.world;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.noip.staticattic.GUI.MainWindow;
 import org.noip.staticattic.entities.Entity.AnimationState;
 import org.noip.staticattic.entities.Tile;
 
 public class EnvironmentHandler implements Runnable {
 
 	private MainWindow main;
 	private Environment environment = null;
 	private Environment MainEnvironment;	
 	private int xradius, yradius;
 	private JLabel[][] onscreenarray;
 	private Tile[][] array;
 	private int count = 0;
 	private boolean moving = false;
 	JPanel panel = new JPanel();
 	
 	public EnvironmentHandler(MainWindow main) {
 		
 		this.main = main;
 		
 		xradius = (int) (((main.screensize.width/34)/2)+1);
 		yradius = (int) (((main.screensize.height/34)/2)+1);
 		
 		onscreenarray = new JLabel[(xradius*2)+1][(yradius*2)+1];
 		
 	}
 	
 	@Override
 	public void run() {
 		
 		if (!main.getPlayer().getCurrentEnvironment().equals(environment)) {
 			
 			environment = main.getPlayer().getCurrentEnvironment();
 			changeEnvironments();
 			
 		}
 				
 		if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_DOWN) || main.getPlayer().getAnimationState().equals(AnimationState.WALKING_UP) || main.getPlayer().getAnimationState().equals(AnimationState.WALKING_LEFT) || main.getPlayer().getAnimationState().equals(AnimationState.WALKING_RIGHT)) {
 			
 			setMoving(true);
 			
 			if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_LEFT) && main.getPlayer().getLocation().getX() > 0) {
 							
 				panel.setLocation(panel.getLocation().x+2, panel.getLocation().y);
 							
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_RIGHT) && main.getPlayer().getLocation().getX() < array.length-1) {
 							
 				panel.setLocation(panel.getLocation().x-2, panel.getLocation().y);
 							
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_DOWN) && main.getPlayer().getLocation().getY() < array.length-1) {
 							
 				panel.setLocation(panel.getLocation().x, panel.getLocation().y-2);
 							
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_UP) && main.getPlayer().getLocation().getY() > 0) {
 						
 				panel.setLocation(panel.getLocation().x, panel.getLocation().y+2);
 							
 			} else {
 								
 				if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_LEFT)) {
 									
 					main.getPlayer().setAnimationState(AnimationState.IDLE_LEFT);
 								
 				} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_RIGHT)) {
 								
 					main.getPlayer().setAnimationState(AnimationState.IDLE_RIGHT);
 									
 				} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_DOWN)) {
 									
 					main.getPlayer().setAnimationState(AnimationState.IDLE_DOWN);
 									
 				} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_UP)) {
 									
 					main.getPlayer().setAnimationState(AnimationState.IDLE_UP);
 									
 				}
 								
 			}
 						
 			count ++;
 			
 		} 
 		
 		if (count >= 17) {
 			
 			array = main.getPlayer().getCurrentEnvironment().getArray();
 				
 			if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_LEFT)) {
 					
 				if (main.getPlayer().getLocation().getX() > 0) {
 						
 					main.getPlayer().getLocation().setX(main.getPlayer().getLocation().getX()-1);
 					main.getPlayer().setCurrentTile(array[main.getPlayer().getLocation().getX()][main.getPlayer().getLocation().getY()]);
 						
 					main.getPlayer().setAnimationState(AnimationState.IDLE_LEFT);
 					
 				}
 				
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_RIGHT)) {
 				
 				if (main.getPlayer().getLocation().getX() < array.length-1) {
 						
 					main.getPlayer().getLocation().setX(main.getPlayer().getLocation().getX()+1);
 					main.getPlayer().setCurrentTile(array[main.getPlayer().getLocation().getX()][main.getPlayer().getLocation().getY()]);
 						
 					main.getPlayer().setAnimationState(AnimationState.IDLE_RIGHT);
 					
 				}
 					
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_DOWN)) {
 					
 				if (main.getPlayer().getLocation().getY() < array.length-1) {
 						
 					main.getPlayer().getLocation().setY(main.getPlayer().getLocation().getY()+1);
 					main.getPlayer().setCurrentTile(array[main.getPlayer().getLocation().getX()][main.getPlayer().getLocation().getY()]);
 						
 					main.getPlayer().setAnimationState(AnimationState.IDLE_DOWN);
 					
 				}
 					
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.WALKING_UP)) {
 					
 				if (main.getPlayer().getLocation().getY() > 0) {
 						
 					main.getPlayer().getLocation().setY(main.getPlayer().getLocation().getY()-1);
 					main.getPlayer().setCurrentTile(array[main.getPlayer().getLocation().getX()][main.getPlayer().getLocation().getY()]);
 						
 					main.getPlayer().setAnimationState(AnimationState.IDLE_UP);
 					
 				}
 					
 			}
 				
 			if (main.getPlayer().getAnimationState().equals(AnimationState.IDLE_LEFT)) {
 							
 				panel.setLocation(panel.getLocation().x-34, panel.getLocation().y);
 							
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.IDLE_RIGHT)) {
 							
 				panel.setLocation(panel.getLocation().x+34, panel.getLocation().y);
 							
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.IDLE_DOWN)) {
 							
 				panel.setLocation(panel.getLocation().x, panel.getLocation().y+34);
 							
 			} else if (main.getPlayer().getAnimationState().equals(AnimationState.IDLE_UP)) {
 						
 				panel.setLocation(panel.getLocation().x, panel.getLocation().y-34);
 							
 			}
 				
 			updateEnvironment();
 				
 			count = 0;
 			
 			setMoving(false);
 			
 		}
 		
 	}
 	
 	private void changeEnvironments() {
 		
 		array = main.getPlayer().getCurrentEnvironment().getArray();
 		
 		main.getPlayer().setCurrentTile(array[main.getPlayer().getLocation().getX()][main.getPlayer().getLocation().getY()]);
 		
 		main.getANhandler().setEntityList(main.getPlayer().getCurrentEnvironment().getEntityList());
 		
 		main.mainpanel.add(main.getPlayer().getLabel());
 		
 		try {
 			
 			main.remove(panel);
 			
 		} catch (Exception e) {
 			
 			//swallow - panel not added in current state
 			
 		}
 		
 		panel.removeAll();
 				
 		for (int x = 0; x < (xradius*2)+1; x++) {
 			
 			for (int y = 0; y < (yradius*2)+1; y++) {
 				
 				onscreenarray[x][y] = new JLabel();
				onscreenarray[x][y].setBounds(main.getPlayer().getLabel().getLocation().x-(xradius*34)+(34*x)+34, main.getPlayer().getLabel().getLocation().y-(yradius*34)+(34*y)+48, 34, 34);
 				
 				try {
 					
 					onscreenarray[x][y].setIcon(array[main.getPlayer().getLocation().getX()-xradius+x][main.getPlayer().getLocation().getY()-yradius+y].getIcon());
 					
 				} catch (Exception e) {
 					
 					//swallow - outside of map;
 					
 				}
 				
 				panel.add(onscreenarray[x][y]);
 				
 			}
 			
 		}
 		
 		panel.setVisible(true);
		panel.setBounds(-34, -34, onscreenarray[xradius*2][0].getX()+34, onscreenarray[0][yradius*2].getY()+34);
 		panel.setLayout(null);
 		
 		main.mainpanel.add(panel);
 		main.repaint();
 		  
 	}
 	
 	private void updateEnvironment() {
 		
 		for (int x = 0; x < (xradius*2)+1; x++) {
 			
 			for (int y = 0; y < (yradius*2)+1; y++) {
 				
 				try {
 					
 					onscreenarray[x][y].setIcon(array[main.getPlayer().getLocation().getX()-xradius+x][main.getPlayer().getLocation().getY()-yradius+y].getIcon());
 					
 				} catch (Exception e) {
 					
 					onscreenarray[x][y].setIcon(null);
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 
 	public Environment getMainEnvironment() {
 		
 		return MainEnvironment;
 		
 	}
 
 	public void setMainEnvironment(Environment mainEnvironment) {
 		
 		MainEnvironment = mainEnvironment;
 		
 	}
 
 	public boolean isMoving() {
 		
 		return moving;
 		
 	}
 
 	public void setMoving(boolean moving) {
 		
 		this.moving = moving;
 		
 	}
 
 }
