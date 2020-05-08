 package edu.brown.cs32.MFTG.gui.properties;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import edu.brown.cs32.MFTG.gui.Constants.ColorProperties;
 import edu.brown.cs32.MFTG.gui.Constants.Colors;
 import edu.brown.cs32.MFTG.gui.Constants.Properties;
 import edu.brown.cs32.MFTG.gui.Houses;
 import edu.brown.cs32.MFTG.tournament.data.PropertyDataAccumulator;
 import edu.brown.cs32.MFTG.tournament.data.PropertyDataReport;
 
 public class MyColorPropertyPanel extends MyPropertyPanel {
 
 	private Color _color;
 	private Houses _houseRow;
	private double _houses = 2.5;
 	
 	public MyColorPropertyPanel(ColorProperties color) {
 		super(color);
 		_color = color.getColor();
 		_houseRow = new Houses(_houses, _color, getTimeOwned(), getProfit());
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g, _color, _houseRow, Color.BLACK);
 	}
 	
 	public void updateProfit(double profit) {
 		super.updateProfit(profit);
 		_houseRow.setProfit(profit);
 	}
 
 	@Override
 	public void setData(PropertyDataReport data) {
 		updateProfit(data.accTotalRevenueWithHouses);
 		_houseRow.setNumHouses(data.accNumHouses);
 	}
 
 }
