 package edu.gatech.cs2340.ui;
 import java.awt.Color;
import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridLayout; 
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import edu.gatech.cs2340.data.Map;
 import edu.gatech.cs2340.data.Tile;
 
 
 /**
  * 
  * @author Stephen Conway
  * 		Function group:		View: Background
  * 		Created for:		M6		10/8/13
  * 		Assigned to:		Dan
  * 		Modifications:		M6		10/15/2013 Thomas Mark
  * 									Removed GUIManager references.								
  * 
  * 
  * 		Purpose: Provide a graphical representation of the Map
  */
 public class MapRenderer extends JPanel{
 	private static final long serialVersionUID = 1L;
 	private static final int TILE_WIDTH = 75;
 	private static final int TILE_HEIGHT = 75;
 	
 	private Map map;
 	private JLabel[] tileLabels;
 	private boolean displayPrices = false;
 
 	
 	/**
 	 * #M6
 	 * Constructor. Connects renderer with data.
 	 * @param map
 	 */
 	public MapRenderer(Map map) {
 		this.map = map;	
		
		Dimension dim = new Dimension(72*9+40, 5*72+3*4);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		
 		setLayout(new GridLayout(map.getNumRows(),map.getNumCols(),0,0));
 		tileLabels = new JLabel[map.getNumTiles()];
 		for (int i=0; i<tileLabels.length; i++) {
 			Tile tile = map.getTileNumber(i);
 			JLabel label = TileImageFactory.getTileLabelImage(tile);
 			tileLabels[i] = label;
 			add(label);
 		}
 		revalidate();
 	}
 	
 	public void refreshAll() {
 		boolean repaintNeeded = false;
 		for (int i=0; i<map.getNumTiles(); i++) {
 			Tile tile = map.getTileNumber(i);
 			if (tile.isDirty()) {
 				repaintNeeded = true;
 				JLabel holderLabel = TileImageFactory.getTileLabelImage(tile);
 				JLabel tileLabel = tileLabels[i];
 				tileLabel.setIcon(holderLabel.getIcon());
 				tileLabel.setBorder(holderLabel.getBorder());
 				tile.cleaned();
 			}
 		}
 		
 		if (repaintNeeded) {
 			revalidate();
 		}
 	}
 
 	
 	/**
 	 * #M6
 	 * Method that draws the Sprite if one is currently on the map.
 	 */
 	@Override
 	public void paint(Graphics g) {	// TODO change to paintComponent?
         super.paint(g);
         if (displayPrices){
         	for (int i = 0; i < map.getNumTiles(); i++){
             	Tile tile = map.getTileNumber(i);
             	if (tile.getPrice() != 0){ // only display a non-zero price
             		int x = getXCoord(i);
 	            	int y = getYCoord(i);
 	            	// start drawing the string in the middle left of the tile
 	            	g.setColor(Color.RED);
 	            	g.setFont(new Font("Serif", Font.PLAIN, 30));
 	            	g.drawString("$" + tile.getPrice(), x*TILE_WIDTH + (int) TILE_WIDTH/4, y*TILE_HEIGHT + (int) TILE_HEIGHT*2/3);
         		}
             }
         }
         g.dispose();
     }
 	
 	/**
 	 * Get the x coordinate for an index
 	 * @param index Index into a one-dimensional array
 	 * @return Equivalent x coordinate for the two-dimensional array
 	 */
 	private int getXCoord(int index){
 		return index % map.getNumCols();
 	}
 	
 	/**
 	 * Get the y coordinate for an index
 	 * @param index Index into a one-dimensional array
 	 * @return Equivalent y coordinate for the two-dimensional array
 	 */
 	private int getYCoord(int index){
 		return (int) index / map.getNumCols();
 	}
 
 	public void setDisplayPrices(boolean displayPrices) {
 		this.displayPrices = displayPrices;
 		if (!displayPrices){ // if we're clearing the display prices,
 			for (int i = 0; i < map.getNumTiles(); i++){
 				map.getTileNumber(i).setPrice(0); // clear all tile prices so they won't display again
 			}
 		}
 		refreshAll();
 	}
 }
