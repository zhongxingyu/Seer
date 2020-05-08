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
 
 public class Rail extends Conveyor {
 	private int railSize;
 	private double railPosition = 0;
 	
 	public Rail(Properties properties, int id) throws FactoryInitializationException {
 		super(properties, id);
 
 		setRailSize(new Integer(properties.getProperty("facility."+id+".rail.size")).intValue());
 		
 		railPosition = -railSize / 2;
 		
 		ModbusSlave.getSimpleProcessImage().addDigitalOut(new SimpleDigitalOut(false));//R-
 		ModbusSlave.getSimpleProcessImage().addDigitalOut(new SimpleDigitalOut(false));//R+
 		ModbusSlave.getSimpleProcessImage().addDigitalIn(new SimpleDigitalIn(false)); // R- Sensor	
 		ModbusSlave.getSimpleProcessImage().addDigitalIn(new SimpleDigitalIn(false)); // R+ Sensor	
 	}
 	
 	private void paintConveyor(Graphics g){
 		super.paint(g);
 		paintLight(g, false, 0, getDigitalOut(2), 1);
 		paintLight(g, true, 1, getDigitalIn(1), 1);
 		paintLight(g, true, 2, getDigitalIn(2), 1);
 		paintLight(g, false, 3, getDigitalOut(3), 1);
 	}
 	
 	public void paint(Graphics g){
 		
 		double oldRailPosition = railPosition;
 		railPosition = 0;
 		Rectangle bounds = getBounds();
 		railPosition = oldRailPosition;
 		g.setColor(Color.darkGray);
 		
 		double pixelSize = getFactory().getPixelSize();
 		int railSize = (int) (this.railSize / pixelSize);
 		
 		if (getOrientation()==Direction.VERTICAL) {
 			g.drawLine(bounds.x - railSize / 2 + bounds.width / 2, bounds.y + bounds.height / 4, bounds.x + railSize / 2 + bounds.width / 2, bounds.y + bounds.height / 4);
 			g.drawLine(bounds.x - railSize / 2 + bounds.width / 2, bounds.y + 3 * bounds.height / 4, bounds.x + railSize / 2 + bounds.width / 2 , bounds.y + 3 * bounds.height / 4);
 		} else {
 			g.drawLine(bounds.x + bounds.width / 4, bounds.y - railSize / 2 + bounds.height / 2, bounds.x + bounds.width / 4, bounds.y + railSize / 2 + bounds.height / 2);
 			g.drawLine(bounds.x + 3 * bounds.width / 4, bounds.y - railSize / 2 + bounds.height / 2, bounds.x + 3 * bounds.width / 4, bounds.y + railSize / 2 + bounds.height / 2);
 		}
 	
 		paintConveyor(g);
 	}
 
 	@Override
 	public void doStep(boolean conveyorBlocked){
 		boolean movedLeft = false;
 		boolean movedRight = false;
 		if (facilityError) return;
 		boolean forcing = false;
		if (getOrientation() == Direction.VERTICAL)
			super.doStep(isRailMovingLeft() || isRailMovingLeft(), getCenterX() + railPosition, getCenterY());
		else
			super.doStep(isRailMovingLeft() || isRailMovingLeft(), getCenterX(), getCenterY() + railPosition);
 		
 		double speed = getFactory().getConveyorSpeed()*getFactory().getSimulationTime()/1000;
 		
 		if (isRailMovingLeft() && !isRailMovingRight()) {railPosition -= speed; movedLeft = true;}
 		if (isRailMovingRight() && !isRailMovingLeft()) {railPosition += speed; movedRight = true;}
 		
 		if (railPosition < -railSize / 2) {railPosition = -railSize / 2; forcing = true; movedLeft = false;}
 		if (railPosition > railSize / 2) {railPosition = railSize / 2; forcing = true; movedRight = false;}
 		
 		if (railPosition <= -railSize / 2) setDigitalIn(1, true); else setDigitalIn(1, false);
 		if (railPosition >= railSize / 2) setDigitalIn(2, true); else setDigitalIn(2, false);
 		
 		if (movedLeft || movedRight) {
 			ArrayList<Block> blocks = getFactory().getBlocks();
 			for (Block block : blocks) {
 				if (getBounds().intersects(block.getBounds())){
 					if (movedLeft && getOrientation()==Direction.VERTICAL) block.setMoveLeft(true);
 					if (movedRight && getOrientation()==Direction.VERTICAL) block.setMoveRight(true);
 					if (movedLeft && getOrientation()==Direction.HORIZONTAL) block.setMoveTop(true);
 					if (movedRight && getOrientation()==Direction.HORIZONTAL) block.setMoveBottom(true);
 				}
 			}
 		}
 		
 		isForcing(forcing);
 	}
 	
 	private boolean isRailMovingLeft() {
 		return getDigitalOut(2) && !getDigitalOut(3);
 	}
 
 	private boolean isRailMovingRight() {
 		return getDigitalOut(3) && !getDigitalOut(2);
 	}
 
 	@Override
 	public String getName() {
 		return "Rail";
 	}
 
 	@Override
 	public int getNumberDigitalIns() {return 3;}
 
 	@Override
 	public int getNumberDigitalOuts() {return 4;}
 
 	public void setRailSize(int railSize) {
 		this.railSize = railSize;
 	}
 
 	public int getRailSize() {
 		return railSize;
 	}
 
 	@Override
 	public Collection<String> getActions() {
 		ArrayList<String> actions = new ArrayList<String>();
 		actions.add("Motor +");
 		actions.add("Motor -");
 		actions.add("Slide +");
 		actions.add("Slide -");
 		return actions;
 	}
 
 	@Override
 	public void doAction(String actionName) {
 		if (actionName.equals("Slide +")) setDigitalOut(3, !getDigitalOut(3));
 		if (actionName.equals("Slide -")) setDigitalOut(2, !getDigitalOut(2));
 		super.doAction(actionName);
 	}
 		
 	@Override
 	public Rectangle getBounds() {
 		double pixelSize = getFactory().getPixelSize();
 		int x = getOrientation()==Direction.VERTICAL?(int) (getCenterX()/pixelSize - width/2/pixelSize + railPosition/pixelSize):(int) (getCenterX()/pixelSize - length/2/pixelSize); 
 		int y = getOrientation()==Direction.VERTICAL?(int) (getCenterY()/pixelSize - length/2/pixelSize):(int) (getCenterY()/pixelSize - width/2/pixelSize + railPosition/pixelSize);
 		int w = getOrientation()==Direction.VERTICAL?(int) (width/pixelSize):(int) (length/pixelSize);
 		int h = getOrientation()==Direction.VERTICAL?(int) (length/pixelSize):(int) (width/pixelSize);
 		return new Rectangle(x, y, w, h);
 	}
 }
