 /*
  * This file is part of ShopFloorSimulator.
  * 
  * ShopFloorSimulator is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ShopFloorSimulator is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with ShopFloorSimulator.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.feup.sfs.facility;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import net.wimpi.modbus.procimg.SimpleDigitalIn;
 
 import com.feup.sfs.block.Block;
 import com.feup.sfs.exceptions.FactoryInitializationException;
 
 public class Roller extends Facility{
 	public enum Orientation {VERTICAL, HORIZONTAL}
 
 	private double centerX;
 	private double centerY;
 	protected double length;
 	protected double width;
 	protected int sensors;
 	
 	protected Orientation orientation;
 	private int direction = 0;
 	
 	public Roller(Properties properties, int id) throws FactoryInitializationException {
 		super(id);
 		this.name = "Roller";
 
 		setCenterX(new Double(properties.getProperty("facility."+id+".center.x")).doubleValue());
 		setCenterY(new Double(properties.getProperty("facility."+id+".center.y")).doubleValue());
 		length = new Double(properties.getProperty("facility."+id+".length")).doubleValue();
 		width = new Double(properties.getProperty("facility."+id+".width")).doubleValue();
 		sensors = new Integer(properties.getProperty("facility."+id+".sensors", "1")).intValue();
		direction = new Integer(properties.getProperty("facility."+id+".direction", "0")).intValue(); 
 		if (properties.getProperty("facility."+id+".orientation").equals("vertical"))
 			orientation = Orientation.VERTICAL;
 		else if (properties.getProperty("facility."+id+".orientation").equals("horizontal"))
 			orientation = Orientation.HORIZONTAL;
 		else throw new FactoryInitializationException("No such orientation " + properties.getProperty("facility."+id+".orientation"));
 
 		for (int i = 0; i < sensors; i++)
 			addDigitalIn(new SimpleDigitalIn(false), "Sensor " + i);
 	}
 	
 
 	@Override
 	public void paint(Graphics g){
 		g.setColor(Color.lightGray);
 		Rectangle bounds = getBounds();
 		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
 		if (facilityError) g.setColor(Color.red);
 		else g.setColor(Color.black);
 		g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
 		
 		g.setColor(Color.orange);
 		
 		for (int i = 0; i < sensors; i++) {
 			Point sp = getSensorBounds(i);
 			g.fillRect(sp.x - 2, sp.y - 2, 4, 4);
 		}
 				
 		for (int i = 0; i < sensors; i++)
 			paintLight(g, true, i, isSensorActive(i), 0);
 	}
 
 	@Override
 	public void doStep(boolean conveyorBlocked){
 		if (facilityError) return;
 		ArrayList<Block> blocks = getFactory().getBlocks();
 		boolean middleSensor[] = new boolean[sensors];
 		for (Block block : blocks) {
 			if (!conveyorBlocked && getBounds().intersects(block.getBounds())){
 				if (isRunningLeft()) block.setMoveLeft(true);
 				if (isRunningRight()) block.setMoveRight(true);
 				if (isRunningTop()) block.setMoveTop(true);
 				if (isRunningBottom()) block.setMoveBottom(true);
 			}
 			for (int i = 0; i < sensors; i++) {
 				Point2D.Double sp = getSensorPosition(i);
 				if (block.getDistanceTo(sp.x, sp.y) < getFactory().getSensorRadius()) 
 					middleSensor[i] = true;
 			}
 		}
 		for (int i = 0; i < sensors; i++) setDigitalIn(i, middleSensor[i]);
 	}
 	
 	public boolean isRunningLeft(){
 		return getOrientation() == Orientation.HORIZONTAL && direction  == 0;
 	}
 
 	public boolean isRunningRight(){
 		return getOrientation() == Orientation.HORIZONTAL && direction == 1;
 	}
 
 	public boolean isRunningTop(){
 		return getOrientation() == Orientation.VERTICAL && direction == 0;
 	}
 
 	public boolean isRunningBottom(){
 		return getOrientation() == Orientation.VERTICAL && direction == 1;
 	}
 	
 	public boolean isSensorActive(int i){
 		return getDigitalIn(i);
 	}
 	
 	@Override
 	public Rectangle getBounds() {
 		double pixelSize = getFactory().getPixelSize();
 		int x = getOrientation()==Orientation.VERTICAL?(int) (getCenterX()/pixelSize - width/2/pixelSize):(int) (getCenterX()/pixelSize - length/2/pixelSize); 
 		int y = getOrientation()==Orientation.VERTICAL?(int) (getCenterY()/pixelSize - length/2/pixelSize):(int) (getCenterY()/pixelSize - width/2/pixelSize);
 		int w = getOrientation()==Orientation.VERTICAL?(int) (width/pixelSize):(int) (length/pixelSize);
 		int h = getOrientation()==Orientation.VERTICAL?(int) (length/pixelSize):(int) (width/pixelSize);
 		return new Rectangle(x, y, w, h);
 	}
 	
 	public Point getSensorBounds(int i) {
 		Point2D.Double sp = getSensorPosition(i);
 		double pixelSize = getFactory().getPixelSize();
 		return new Point((int)(sp.x/pixelSize), (int)(sp.y/pixelSize));
 	}
 
 	public Point2D.Double getSensorPosition(int i) {
 		return getSensorPosition(i, 0, 0);
 	}
 
 	public Point2D.Double getSensorPosition(int i, double dispX, double dispY) {
 		if (getOrientation()==Orientation.HORIZONTAL) return new Point2D.Double(dispX + getCenterX() + length / sensors * i - length / 2 + length / sensors / 2, dispY + getCenterY());
 		else return new Point2D.Double(dispX + getCenterX(), dispY + getCenterY() + length / sensors * i - length / 2 + length / sensors / 2);
 	}
 	
 	public Orientation getOrientation(){
 		return orientation;
 	}
 
 	public void setCenterX(double centerX) {
 		this.centerX = centerX;
 	}
 
 	public double getCenterX() {
 		return centerX;
 	}
 
 	public void setCenterY(double centerY) {
 		this.centerY = centerY;
 	}
 
 	public double getCenterY() {
 		return centerY;
 	}
 }
