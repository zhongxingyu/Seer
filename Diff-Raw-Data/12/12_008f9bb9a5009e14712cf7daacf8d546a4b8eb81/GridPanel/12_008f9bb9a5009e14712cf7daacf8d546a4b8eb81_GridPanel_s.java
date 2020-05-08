 package edu.wheaton.simulator.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 import edu.wheaton.simulator.entity.Agent;
 
 public class GridPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6168906849044462629L;
 
 	private ScreenManager sm;
 
 	private int width;
 
 	private int height;
 
 	private int gridWidth;
 
 	private int gridHeight;
 
 	public GridPanel(ScreenManager sm) {
 		this.sm = sm;
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		width = this.getWidth();
 		height = this.getHeight();
 		gridWidth = sm.getGUIwidth();
 		gridHeight = sm.getGUIheight();
 		int pixelWidth = width / gridWidth;
 		int pixelHeight = height / gridHeight;
 		int squareSize = Math.min(pixelWidth, pixelHeight);
 		for (int i = 0; i < gridWidth; i++) {
 			for (int j = 0; j < gridHeight; j++) {
 				g.drawRect(squareSize * i, squareSize * j, 
 						squareSize, squareSize);
 			}
 		}
 		clearAgents(g);
 		agentPaint(g);
 	}
 
 	public void agentPaint(Graphics g){
 		if(g==null){
 			System.err.println("GridPanel.java ( agentPaint(Graphics g) ): parameter 'g' is null");
 			throw new NullPointerException();
 		}
 		
 		width = this.getWidth();
 		height = this.getHeight();
 		gridWidth = sm.getGUIwidth();
 		gridHeight = sm.getGUIheight();
 		int pixelWidth = width / gridWidth;
 		int pixelHeight = height / gridHeight;
 		int squareSize = Math.min(pixelWidth, pixelHeight);
 		for (int x = 0; x < gridWidth; x++) {
 			for (int y = 0; y < gridHeight; y++) {
 				Agent agent = sm.getFacade().getAgent(x, y);
 				if(agent != null) {
					System.out.println("\tg null " + (g == null));
 					
 					//agent cannot be null at this location
 					System.out.println("\tagent null " + false);
 					
 					System.out.println("\tcolor null " + (agent.getColor() == null));
 					g.setColor(agent.getColor());
 
 					if(squareSize < 9){
 						g.fillRect(squareSize * x + (x + 1), squareSize * y + (y + 1), 
 								squareSize, squareSize);
 					}
 //Still not working yet. Time for bed though. Fix it tomorrow.					
 					else{
 						int iconSize = squareSize/7;
 						for (int a = 0; x < squareSize; x+=iconSize) {
 							for (int b = 0; y <  squareSize; y+=iconSize) {
 								byte[] icon = agent.getDesign();
 								byte val = new Byte("0000001");
 								if((icon[a]&(val<<b)) == 1){
 									g.fillRect((squareSize * x) + a*iconSize,
 											(squareSize * y) + b*iconSize, 
 											iconSize, iconSize);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void clearAgents(Graphics g) {
 		width = this.getWidth();
 		height = this.getHeight();
 		gridWidth = sm.getGUIwidth();
 		gridHeight = sm.getGUIheight();
 		int pixelWidth = width / gridWidth;
 		int pixelHeight = height / gridHeight;
 		int squareSize = Math.min(pixelWidth, pixelHeight) - 1;
 		g.setColor(Color.WHITE);
 		for (int x = 0; x < gridWidth; x++) {
 			for (int y = 0; y < gridHeight; y++) {
 				g.fillRect(squareSize * x + (x + 1), squareSize * y + (y + 1), squareSize, squareSize);
 			}
 		}
 	}
 
 }
