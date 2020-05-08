 package edu.brown.cs32.MFTG.gui.properties;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import edu.brown.cs32.MFTG.gui.Constants.ColorProperties;
 import edu.brown.cs32.MFTG.gui.Houses;
 import edu.brown.cs32.MFTG.tournament.data.PropertyDataReport;
 
 public class AggregateColorProperty extends NonstaticProperty {
 
 	private Color _color;
 	private Houses _houseRow;
	private double _houses = 1;
 
 	
 	public AggregateColorProperty(ColorProperties color) {
 		super(color);
 		_color = color.getColor();
 		_houseRow = new Houses(_houses, _color, getTimeOwned(), getProfit());
 		updateTimeOwned(1);
 	}
 	
 	public void updateTimeOwned(double timeOwned) {
 		super.updateTimeOwned(timeOwned);
 		_houseRow.setTimeOwned(timeOwned);
 	}
 	
 	public void updateProfit(double profit) {
 		super.updateProfit(profit);
 		_houseRow.setProfit(profit);
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g, _color, _houseRow, Color.WHITE);
 	}
 
 	@Override
 	public void setData(PropertyDataReport data) {
 		this.updateProfit(data.accTotalRevenueWithHouses);
 		_houseRow.setNumHouses(data.accNumHouses);
 	}
 	
 	public int getValue() {
 		throw new NullPointerException("user cannot set value of aggregate property");
 	}
 
 	@Override
 	public void setValue(int integer) {}
 }
