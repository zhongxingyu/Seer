 package bitcity;
 
 import javax.swing.JPanel;
 import java.awt.Point;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 public class WorldMap extends JPanel implements Runnable {
 
 	/**
 	 * XXX ? 
 	 */
 	private static final long serialVersionUID = 2398485906470095960L;
 
 	public static boolean ambulanceAround = false;
 	public static int carCount = 0;
	private static final int carLimit = 70;
 	
 	private World world;
 	private Thread thread;
 	
 	public WorldMap(World world) {
 		this.world = world;
 		this.thread = new Thread(this);
 		this.thread.start();
 	}
 	
 	public void run() {
 		Point startPos;
 		
 		for (int i = 0; i < this.world.getTrafficLight().size(); i++){
 			this.world.getTrafficLight().get(i).start();
 		}
 		
 		while (true) {
 			try {
 				Thread.sleep((int)(1000/30.));
 				if (WorldMap.carCount < WorldMap.carLimit && Math.random() < 0.15) {
 					/* Add a new car. */
 					for (int i = 0; i < 2; i++) {
 						startPos = this.world.getRandomStartPos();
 						if ((this.world.getRoadElement(startPos.x, startPos.y) & World.CAR) != 0) {
 							System.out.println(">> There is a car in the soup");
 						} else {
 							System.out.println(">> Add car");
 							Car.createCar(this.world, startPos).start();
 							break;
 						}
 					}
 				} else if ((WorldMap.carCount >= WorldMap.carLimit - 5) && !WorldMap.ambulanceAround &&
 						Math.random() < 0.01) {
 					startPos = this.world.getRandomStartPos();
 					Ambulance.createCar(this.world, startPos).start();
 				}
 			} catch (InterruptedException e) {
 				System.out.println(e.getMessage());
 				break;
 			} catch (Exception e) {
 				System.out.println("Nice unwanted exception :) " + e.getMessage());
 			}
 			repaint();
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		Graphics2D ctx = (Graphics2D)g;
 		int width, height;
 		int i, j;
 		float stepw, steph;
 		char elem;
 		
 		width = this.getParent().getWidth();
 		height = this.getParent().getHeight();
 		stepw = (float)width / (this.world.columnCount() - 1);
 		steph = (float)height / (this.world.rowCount() - 1);
 		//System.out.println("stepw = " + stepw + ", steph = " + steph);
 		
 		ctx.setColor(new Color(0, (float)0.7, 0));
 		ctx.fillRect(0, 0, width, height);
 		
 		//System.out.println("redrawing");
 		for (i = 1; i < this.world.rowCount(); i++) {
 			for (j = 1; j < this.world.columnCount(); j++) {
 				elem = this.world.getElementAt(i, j);
 
 				if (elem == Parser.SIDEWALK || elem == Parser.GARAGE) {
 					ctx.setColor(new Color((float)0.7, (float)0.7, (float)0.7));
 				} else if (elem == '$') {
 					/* Some kind of decoration. */
 					ctx.setColor(new Color(0, (float)0.5, 0));
 				} else if (this.world.getSemaphores().containsKey(elem)) {
 					/* Traffic light. */
 					ctx.setColor(Color.BLACK);
					if ((this.world.getRoadElement(i, j) & World.CAR) != 0) {
						ctx.setColor(Color.BLUE);
					} else if (this.world.getRoadElement(i, j) == World.AMBULANCE) {
						ctx.setColor(Color.WHITE);
					}
 					ctx.fillRect((int)((j - 1) * stepw), (int)((i - 1) * steph), 
							(int)stepw + 1, (int)steph + 1);
 					
 					switch (this.world.getSemaphores().get(elem).status(i, j)) {
 					case ALERT:
 						ctx.setColor(Color.YELLOW);
 						break;
 					case CLOSED:
 						ctx.setColor(Color.RED);
 						break;
 					case OPEN:
 						ctx.setColor(Color.GREEN);
 						break;
 					}
 					//ctx.setColor(Color.YELLOW);
 					ctx.fillRect((int)((j - 1) * stepw), (int)((i - 1) * steph), 
							(int)stepw + 1, (int)stepw + 1);
 					
					ctx.setColor(Color.BLACK);
 					
 					ctx.drawString("" + this.world.getElementAt(i, j) + "",
 							(int)((j - 1) * stepw), (int)((i - 1) * steph) + (steph / 2));
 					
 					continue;
 				} else if ((this.world.getRoadElement(i, j) & World.CAR) != 0) { 
 					/* This is a car. */
 					ctx.setColor(Color.BLUE);
 				} else if (this.world.getRoadElement(i, j) == World.AMBULANCE) {
 					ctx.setColor(Color.WHITE);
 				} else if (this.world.isRoad(i, j)) {
 					/* Road. */
 					ctx.setColor(Color.BLACK);
 				} else {
 					continue;
 				}
 				
 				ctx.fillRect((int)((j - 1) * stepw), (int)((i - 1) * steph), 
 						(int)stepw + 1, (int)steph + 1);
 				if ((this.world.getRoadElement(i, j) & World.CAR_HONKING) != 0) {
 					ctx.setColor(Color.WHITE);
 					ctx.fillRect((int)(((j - 1) * stepw) + 1), (int)(((i - 1) * steph) + (steph/4.)), 
 							(int)(stepw - 2), (int)((steph - 1)/2.));
 				}
 			}
 		}
 	}
 }
