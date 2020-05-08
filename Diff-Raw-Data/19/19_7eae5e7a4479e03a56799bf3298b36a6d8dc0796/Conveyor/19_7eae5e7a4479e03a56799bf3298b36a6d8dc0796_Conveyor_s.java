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
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Properties;
 
 import net.wimpi.modbus.procimg.SimpleDigitalIn;
 import net.wimpi.modbus.procimg.SimpleDigitalOut;
 
 import com.feup.sfs.block.Block;
 import com.feup.sfs.exceptions.FactoryInitializationException;
 import com.feup.sfs.modbus.ModbusSlave;
 
 public class Conveyor extends Facility{
 	public enum Direction {VERTICAL, HORIZONTAL}
 
 	private double centerX;
 	private double centerY;
 	protected double length;
 	protected double width;
 	
 	protected Direction orientation;
 	
 	public Conveyor(Properties properties, int id) throws FactoryInitializationException {
 		super(id);
 
 		setCenterX(new Double(properties.getProperty("facility."+id+".center.x")).doubleValue());
 		setCenterY(new Double(properties.getProperty("facility."+id+".center.y")).doubleValue());
 		length = new Double(properties.getProperty("facility."+id+".length")).doubleValue();
 		width = new Double(properties.getProperty("facility."+id+".width")).doubleValue();
 		if (properties.getProperty("facility."+id+".orientation").equals("vertical"))
 			orientation = Direction.VERTICAL;
 		else if (properties.getProperty("facility."+id+".orientation").equals("horizontal"))
 			orientation = Direction.HORIZONTAL;
 		else throw new FactoryInitializationException("No such orientation " + properties.getProperty("facility."+id+".orientation"));
 		
 		ModbusSlave.getSimpleProcessImage().addDigitalOut(new SimpleDigitalOut(false)); //M+
 		ModbusSlave.getSimpleProcessImage().addDigitalOut(new SimpleDigitalOut(false)); //M-
 		ModbusSlave.getSimpleProcessImage().addDigitalIn(new SimpleDigitalIn(false)); // middle sensor
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
 		g.fillRect(bounds.x + bounds.width / 2 - 2, bounds.y + bounds.height / 2 - 2, 4, 4);
 		
 		paintLight(g, false, 0, isMotorPlusOn(), 0);
 		paintLight(g, true, 1, isSensorActive(), 0);
 		paintLight(g, false, 2, isMotorMinusOn(), 0);
 	}
 
 	@Override
 	public void doStep(boolean conveyorBlocked){
 		if (facilityError) return;
 		ArrayList<Block> blocks = getFactory().getBlocks();
 		boolean middleSensor = false;
 		for (Block block : blocks) {
 			if (!conveyorBlocked && getBounds().intersects(block.getBounds())){
 
 				if (isRunningLeft()) block.setMoveLeft(true);
 				if (isRunningRight()) block.setMoveRight(true);
 				if (isRunningTop()) block.setMoveTop(true);
 				if (isRunningBottom()) block.setMoveBottom(true);
 			}
			if (block.getDistanceTo(getCenterX(), getCenterY()) < getFactory().getSensorRadius()) 
 				middleSensor = true;
 		}
 		setDigitalIn(0, middleSensor);
 	}
 	
 	public boolean isRunningLeft(){
 		return getOrientation() == Direction.HORIZONTAL && isMotorPlusOn() && !isMotorMinusOn();
 	}
 
 	public boolean isRunningRight(){
 		return getOrientation() == Direction.HORIZONTAL && !isMotorPlusOn() && isMotorMinusOn();
 	}
 
 	public boolean isRunningTop(){
 		return getOrientation() == Direction.VERTICAL && isMotorPlusOn() && !isMotorMinusOn();
 	}
 
 	public boolean isRunningBottom(){
 		return getOrientation() == Direction.VERTICAL && !isMotorPlusOn() && isMotorMinusOn();
 	}
 	
 	public boolean isSensorActive(){
 		return getDigitalIn(0);
 	}
 	
 	public boolean isMoving(){
 		return getDigitalOut(0) != getDigitalOut(1);
 	}	
 	
 	public boolean isMotorPlusOn(){
 		return getDigitalOut(0);
 	}
 
 	public boolean isMotorMinusOn(){
 		return getDigitalOut(1);
 	}
 	
 	@Override
 	public Rectangle getBounds() {
 		double pixelSize = getFactory().getPixelSize();
 		int x = getOrientation()==Direction.VERTICAL?(int) (getCenterX()/pixelSize - width/2/pixelSize):(int) (getCenterX()/pixelSize - length/2/pixelSize); 
 		int y = getOrientation()==Direction.VERTICAL?(int) (getCenterY()/pixelSize - length/2/pixelSize):(int) (getCenterY()/pixelSize - width/2/pixelSize);
 		int w = getOrientation()==Direction.VERTICAL?(int) (width/pixelSize):(int) (length/pixelSize);
 		int h = getOrientation()==Direction.VERTICAL?(int) (length/pixelSize):(int) (width/pixelSize);
 		return new Rectangle(x, y, w, h);
 	}
 	
 	public Direction getOrientation(){
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
 
 	@Override
 	public String getName() {
 		return "Conveyor";
 	}
 	
 	@Override
 	public int getNumberDigitalIns() {return 1;}
 
 	@Override
 	public int getNumberDigitalOuts() {return 2;}
 	
 	@Override
 	public int getNumberRegisters() {return 0;}
 
 	@Override
 	public Collection<String> getActions() {
 		ArrayList<String> actions = new ArrayList<String>();
 		actions.add("Motor +");
 		actions.add("Motor -");
 		return actions;
 	}
 
 	@Override
 	public void doAction(String actionName) {
 		if (actionName.equals("Motor +")) setDigitalOut(0, !getDigitalOut(0));
 		if (actionName.equals("Motor -")) setDigitalOut(1, !getDigitalOut(1));
 	}
 }
