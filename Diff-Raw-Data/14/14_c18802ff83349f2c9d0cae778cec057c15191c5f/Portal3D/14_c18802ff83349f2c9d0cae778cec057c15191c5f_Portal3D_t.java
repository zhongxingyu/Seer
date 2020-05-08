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
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import net.wimpi.modbus.procimg.SimpleDigitalIn;
 import net.wimpi.modbus.procimg.SimpleDigitalOut;
 
 import com.feup.sfs.block.Block;
 import com.feup.sfs.exceptions.FactoryInitializationException;
 import com.feup.sfs.factory.Factory;
 
 public class Portal3D extends Facility{
 	private static int TIMETOCLOSE = 1000;
 	private static int TIMETOOPEN = 1000;
 	
 	private enum ClawStates {CLOSED, OPENING, OPENED, CLOSING};
 	
 	private static double wallthickness = 0.3;
 	private static double sensorradius = 0.3;
 	
 	private double centerX;
 	private double centerY;
 	protected double width;
 	protected double height;
 	protected int sensorsx;
 	protected int sensorsy;
 	protected double positionx;
 	protected double positiony;
 	protected double positionz;
 	
 	protected Block block = null;
 	protected ClawStates clawState = ClawStates.OPENED;
 	protected int clawTime = 0;
 	
 	public Portal3D(Properties properties, int id) throws FactoryInitializationException {
 		super(properties, id);
 
 		setCenterX(new Double(properties.getProperty("facility."+id+".center.x")).doubleValue());
 		setCenterY(new Double(properties.getProperty("facility."+id+".center.y")).doubleValue());
 		width = new Double(properties.getProperty("facility."+id+".width")).doubleValue();
 		height = new Double(properties.getProperty("facility."+id+".height")).doubleValue();
 		sensorsx = new Integer(properties.getProperty("facility."+id+".sensorsx", "1")).intValue();
 		sensorsy = new Integer(properties.getProperty("facility."+id+".sensorsy", "1")).intValue();
 
 		positionx = Factory.generateRandom(0, width);
 		positiony = Factory.generateRandom(0, height);
 		positionz = Factory.generateRandom(0, 1);
 		
 		addDigitalOut(new SimpleDigitalOut(false), "MotorX +");
 		addDigitalOut(new SimpleDigitalOut(false), "MotorX -");
 		addDigitalOut(new SimpleDigitalOut(false), "MotorY +");
 		addDigitalOut(new SimpleDigitalOut(false), "MotorY -");
 		addDigitalOut(new SimpleDigitalOut(false), "MotorZ +");
 		addDigitalOut(new SimpleDigitalOut(false), "MotorZ -");
 		addDigitalOut(new SimpleDigitalOut(false), "Grab");
 		
 		for (int i = 0; i < sensorsx; i++)
 			addDigitalIn(new SimpleDigitalIn(false), "Sensor X " + i);
 
 		for (int i = 0; i < sensorsy; i++)
 			addDigitalIn(new SimpleDigitalIn(false), "Sensor Y " + i);
 		
 		addDigitalIn(new SimpleDigitalIn(false), "Sensor Z Bottom");
 		addDigitalIn(new SimpleDigitalIn(false), "Sensor Z Top");
 
 		addDigitalIn(new SimpleDigitalIn(false), "Piece Sensor");
 	}
 	
 	@Override
 	public String getName() {
 		return "3D Portal";
 	}
 	
 	@Override
 	public void paint(Graphics g){
	}
	
	@Override
	public void paintTop(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		Rectangle bounds = getBounds();
 		double pixelSize = getFactory().getPixelSize();
 		int wt = (int) (wallthickness / pixelSize);
 
 		g2.setStroke(new BasicStroke(wt));
 		g2.setColor(Color.lightGray);
 		g2.drawRect(bounds.x - wt, bounds.y - wt, bounds.width + wt * 2, bounds.height + wt * 2);		
 		g2.setStroke(new BasicStroke(1));
 
 		g2.setColor(facilityError?Color.red:Color.black);
 		g2.drawRect(bounds.x - wt, bounds.y - wt, bounds.width + wt * 2, bounds.height + wt * 2);
 		
 		g2.setColor(Color.black);
 		g2.drawLine((int)(bounds.x + positionx / pixelSize), bounds.y - wt, (int)(bounds.x + positionx / pixelSize), bounds.y + bounds.height + wt);
 		g2.drawLine(bounds.x - wt, (int)(bounds.y + positiony / pixelSize), bounds.x + bounds.width + wt, (int)(bounds.y + positiony / pixelSize));
 
 		g2.setStroke(new BasicStroke(2));
 		g2.setColor(facilityError?Color.red:Color.black);
 		g2.drawRect((int)(bounds.x + positionx / pixelSize - 1 / pixelSize), (int)(bounds.y + positiony / pixelSize - 1 / pixelSize), (int)(2 / pixelSize), (int)(2 / pixelSize));
 		g2.setStroke(new BasicStroke());
 		
 		g2.setColor(Color.pink);
 		
 		for (int i = 0; i < sensorsx; i++) {
 			Point sp = getSensorBoundsX(i);
 			g.fillRect(sp.x - 1, sp.y - 1, 3, 3);
 		}
 
 		for (int i = 0; i < sensorsy; i++) {
 			Point sp = getSensorBoundsY(i);
 			g.fillRect(sp.x - 1, sp.y - 1, 3, 3);
 		}
 
 		if (block != null) {
 			block.paint(g);	
 		}
 		
 		paintLight(g, false, 0, getDigitalOut(0), 0);		
 		paintLight(g, false, 1, getDigitalOut(1), 0);		
 		paintLight(g, false, 2, getDigitalOut(2), 0);		
 		paintLight(g, false, 3, getDigitalOut(3), 0);		
 		paintLight(g, false, 4, getDigitalOut(4), 0);		
 		paintLight(g, false, 5, getDigitalOut(5), 0);		
 
 		for (int i = 0; i < sensorsx; i++) paintLight(g, true, i, getDigitalIn(i), 1);		
 		for (int i = 0; i < sensorsy; i++) paintLight(g, true, i, getDigitalIn(sensorsx + i), 2);
 
 		paintLight(g, false, 0, getDigitalIn(sensorsx + sensorsy), 3);
 		paintLight(g, false, 1, getDigitalIn(sensorsx + sensorsy + 1), 3);
 		
 		g.setColor(Color.white);
 		g.fillRect((int) (bounds.x + positionx / pixelSize + 1 / pixelSize - 1), (int) (bounds.y + positiony / pixelSize - 1 / pixelSize + (1 - positionz) * 2 / pixelSize - 1), 3, 3);
 
 		paintLight(g, false, 0, getDigitalOut(6), 4);
 		paintLight(g, false, 1, getDigitalIn(sensorsx + sensorsy + 2), 4);
 	}
 
 	protected void paintLight(Graphics g, boolean type, int position, boolean value, int line) {
 		double pixelSize = Factory.getInstance().getPixelSize();
 		int x = (int) ((position + 0.5) * .2 / pixelSize + (positionx - 1) / pixelSize);
 		int y = (int) ((line + 0.5) * .2  / pixelSize + (positiony - 1) / pixelSize);
 		if (value) g.setColor(Color.green); else g.setColor(Color.red);
 		g.fillRect(getBounds().x + x, getBounds().y + y, (int)(.1 / pixelSize), (int)(.1 / pixelSize));
 		g.setColor(Color.black);
 		if (type) g.drawRect(getBounds().x + x - 1, getBounds().y + y - 1, (int)(.1 / pixelSize) + 1, (int)(.1 / pixelSize) + 1);
 	}
 	
 	private Point getSensorBoundsX(int i) {
 		double sp = getSensorPositionX(i);
 		double pixelSize = getFactory().getPixelSize();
 		Rectangle bounds = getBounds();
 
 		return new Point((int)(sp / pixelSize), (int)(bounds.y + positiony / pixelSize));
 	}
 
 	private Point getSensorBoundsY(int i) {
 		double sp = getSensorPositionY(i);
 		double pixelSize = getFactory().getPixelSize();
 		Rectangle bounds = getBounds();
 
 		return new Point((int)(bounds.x + positionx / pixelSize), (int)(sp / pixelSize));
 	}
 
 	public double getSensorPositionX(int i) {
 		return getCenterX() + width / sensorsx * i - width / 2 + width / sensorsx / 2;
 	}
 
 	public double getSensorPositionY(int i) {
 		return getCenterY() + height / sensorsy * i - height / 2 + height / sensorsy / 2;
 	}
 	
 	@Override
 	public void doStep(boolean conveyorBlocked){
 		if (facilityError) return;
 		double speed = getFactory().getConveyorSpeed()*getFactory().getSimulationTime()/1000;
 		boolean forcing = false;
 		
 		// Claw State Machine
 		
 		if (getDigitalOut(6) && clawState == ClawStates.CLOSING) {
 			clawTime -= getFactory().getSimulationTime();
 			if (clawTime <= 0) clawState = ClawStates.CLOSED;
 		}
 		
 		if (getDigitalOut(6) && clawState == ClawStates.OPENING) clawState = ClawStates.CLOSED;
 		
 		if (getDigitalOut(6) && clawState == ClawStates.OPENED) {
 			clawState = ClawStates.CLOSING;
 			clawTime = TIMETOCLOSE;
 		}
 		
 		if (!getDigitalOut(6) && clawState == ClawStates.CLOSED) {
 			clawState = ClawStates.OPENING;
 			clawTime = TIMETOOPEN;
 		}
 		
 		if (!getDigitalOut(6) && clawState == ClawStates.CLOSING) clawState = ClawStates.OPENED;
 		
 		if (!getDigitalOut(6) && clawState == ClawStates.OPENING) {
 			clawTime -= getFactory().getSimulationTime();
 			if (clawTime <= 0) clawState = ClawStates.OPENED;
 		}
 		
 		// Movement
 		
 		if (getDigitalOut(0) && !getDigitalOut(1) && positionz == 1) positionx += speed;
 		if (getDigitalOut(1) && !getDigitalOut(0) && positionz == 1) positionx -= speed;
 		if (getDigitalOut(2) && !getDigitalOut(3) && positionz == 1) positiony += speed;
 		if (getDigitalOut(3) && !getDigitalOut(2) && positionz == 1) positiony -= speed;
 		if (getDigitalOut(4) && !getDigitalOut(5)) positionz += speed / 4;
 		if (getDigitalOut(5) && !getDigitalOut(4)) positionz -= speed / 4;
 		
 		if (positionx < 0) positionx = 0;
 		if (positionx > width) positionx = width;
 		if (positiony < 0) positiony = 0;
 		if (positiony > height) positiony = height;
 		if (positionz < 0) { forcing = true; positionz = 0; setDigitalIn(sensorsx + sensorsy, true); } else setDigitalIn(sensorsx + sensorsy, false);
 		if (positionz > 1) { forcing = true; positionz = 1; setDigitalIn(sensorsx + sensorsy + 1, true); } else setDigitalIn(sensorsx + sensorsy + 1, false);
 		
 		for (int i = 0; i < sensorsx; i++)
 			if (Math.abs(getSensorPositionX(i) - getCenterX() + width / 2 - positionx) < sensorradius) setDigitalIn(i, true); else setDigitalIn(i, false);
 
 		for (int i = 0; i < sensorsy; i++) 
 			if (Math.abs(getSensorPositionY(i) - getCenterY() + height / 2 - positiony) < sensorradius) setDigitalIn(i + sensorsx, true); else setDigitalIn(i + sensorsx, false);
 		
 		if (block != null && clawState == ClawStates.OPENED) {
 			block.setHeight(1);
 			getFactory().addBlock(block);
 			block = null;
 		}
 		
 		if (block == null && isBlockPresent() && clawState == ClawStates.CLOSED) {
 			block = getCurrentBlock();
 			System.out.println(block);
 			if (block != null) 	getFactory().removeBlock(block);				
 		}
 		
 		if (block != null) {
 			block.setCenterX(getCenterX() - width / 2 + positionx);
 			block.setCenterY(getCenterY() - height / 2 + positiony);
 			block.setHeight(positionz + 1);
 		} 
 		
 		if (block != null || isBlockPresent()) setDigitalIn(sensorsx + sensorsy + 2, true); else setDigitalIn(sensorsx + sensorsy + 2, false);
 		
 		isForcing(forcing);
 	}
 	
 	private boolean isBlockPresent() {
 		return (getCurrentBlock() != null);
 	}
 
 	private Block getCurrentBlock() {
 		if (positionz == 0) {
 			ArrayList<Block> blocks = getFactory().getBlocks();
 			for (Block block : blocks) {
 				Point2D.Double sp = getClawPosition();
 				if (block.getDistanceTo(sp.x, sp.y) < sensorradius) return block;
 			}
 		}
 		return null;
 	}
 
 	private Point2D.Double getClawPosition() {
 		return new Point2D.Double(getCenterX() - width / 2 + positionx, getCenterY() - height / 2 + positiony);
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		double pixelSize = getFactory().getPixelSize();
 		int x = (int) (getCenterX()/pixelSize - width/2/pixelSize); 
 		int y = (int) (getCenterY()/pixelSize - height/2/pixelSize);
 		int w = (int) (width/pixelSize);
 		int h = (int) (height/pixelSize);
 		return new Rectangle(x, y, w, h);
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
 	public boolean canAddBlocks() {
 		return false;
 	}
 }
