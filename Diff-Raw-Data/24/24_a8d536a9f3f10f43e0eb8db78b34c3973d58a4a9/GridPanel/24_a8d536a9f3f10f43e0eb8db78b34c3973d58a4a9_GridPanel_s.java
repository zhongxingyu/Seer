 package edu.wheaton.simulator.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import edu.wheaton.simulator.datastructure.AgentAppearance;
 
 public class GridPanel extends JPanel {
 
 	private static final long serialVersionUID = 6168906849044462629L;
 
 	private SimulatorFacade gm;
 
 	private int panelWidth;
 
 	private int panelHeight;
 
 	private int gridWidth;
 
 	private int gridHeight;
 
 	private float squareWidth;
 
 	private float squareHeight;
 
 	private Set<AgentAppearance> grid;
 
 	public GridPanel(SimulatorFacade gm) {
 		this.gm = gm;
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		panelWidth = this.getWidth();
 		panelHeight = this.getHeight();
 
 		gridWidth = gm.getGridWidth();
 		gridHeight = gm.getGridHeight();
 
 		squareWidth = panelWidth / gridWidth;
 		squareHeight = panelHeight / gridHeight;
 
 		clearAgents(g);
 		if (grid != null)
 			agentPaint(g);
 		paintLines(g);
 	}
 
 	public void agentPaint(Graphics g) {
 		float squareSize = Math.min(squareWidth, squareHeight);
 
 		for (AgentAppearance agent : grid) {
 			int x = agent.getX();
 			int y = agent.getY();
 			g.setColor(agent.getColor());
 
 			if (squareSize < 9) {
 				g.fillRect(Math.round(squareSize * x + (x + 1)),
 						Math.round(squareSize * y + (y + 1)),
 						Math.round(squareSize), Math.round(squareSize));
 			} else {
 				float iconSize = squareSize / 7;
 				for (int a = 0; a < 7; a++) {
 					for (int b = 0; b < 7; b++) {
 						byte[] icon = agent.getDesign();
 						byte val = new Byte("0000001");
 						if ((icon[a] & (val << b)) > 0) {
 							g.fillRect(
									Math.round((squareSize * x + 1) + (6 - b)
 											* iconSize),
											Math.round((squareSize * y + 1) + (a)
 													* iconSize), Math.round(iconSize),
 													Math.round(iconSize));
 
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void clearAgents(Graphics g) {
 		float squareSize = Math.min(squareWidth, squareHeight) - 1;
 		g.setColor(Color.WHITE);
 
 		for (int x = 0; x < gridWidth; x++)
 			for (int y = 0; y < gridHeight; y++)
 				g.fillRect(Math.round(squareSize * x + (x + 1)),
 						Math.round(squareSize * y + (y + 1)),
 						Math.round(squareSize), Math.round(squareSize));
 	}
 
 	public void paintLines(Graphics g) { 
 		g.setColor(Color.BLACK); 
 		float squareSize = Math.min(squareWidth, squareHeight); 
 		for (int i = 0; i < gridWidth; i++) 
 			for (int j = 0; j < gridHeight; j++)
 				g.drawRect(Math.round(squareSize * i), Math.round(squareSize * j),
 						Math.round(squareSize), Math.round(squareSize)); 
 	}
 
 	public void setAgents(Set<AgentAppearance> grid) {
 		this.grid = grid;
 	}
 }
