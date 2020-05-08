 package buffer;
 
 import draw.Drawable;
 import draw.StdDraw;
 import factory.machine.FruitConveyor;
 import factory.dimension.PointXY;
 import factory.dimension.ConveyorBeltDimension;
 
 
 /**
  * DrawableBuffer.java
  * @author:			Devin Barry
  * @date:			13.10.2012
  * @lastModified:	23.10.2012
  * 
  * DrawableBuffer is the abstract superclass of all machines on factory floor that
  * contain a buffer that has draw methods (where some or all of the buffer spaces 
  * are drawn in the representation of the machine)
  * 
  * DrawableBuffer implements the Drawable interface and contains a copy of the
  * FactoryDimension for the machine that it is part of. It uses data from its
  * FactoryDimension to assist its draw methods.
  * 
  * Because this class needs it's buffer to be able to draw, it will usually be
  * representing a conveyor belt or similar item. ConveyorBelts and related
  * machines must be able to "advance" their belt forward. This operation is
  * represented in this class with the AdvanceBuffer method.
  * 
  * Presently, the only machine that meets these criteria is a ConveyorBelt.
  * If more machines in the future contain DrawableBuffer then we will have to
  * genericise the dimension class which we use.
  */
 
 public abstract class DrawableBuffer extends AbstractBuffer implements Drawable, FruitConveyor {
 	
 	ConveyorBeltDimension dimension;
 	
 	/**
 	 * Constructor must be passed an initialised
 	 * FactoryDimension. Currently only ConveyorBelts
 	 * support drawing their buffers thus it is a 
 	 * ConveyorBeltDimension.
 	 * 
 	 * @param d
 	 */
 	public DrawableBuffer(ConveyorBeltDimension d) {
 		super();
 		this.dimension = d;
 	}
 	
 	/**
 	 * Constructor must be passed an initialised
 	 * FactoryDimension. Currently only ConveyorBelts
 	 * support drawing their buffers thus it is a 
 	 * ConveyorBeltDimension.
 	 * 
 	 * @param d
 	 * @param maxSize
 	 */
 	public DrawableBuffer(ConveyorBeltDimension d, int maxSize) {
 		super(maxSize); //set the maximum size of the AbstractBuffer
 		this.dimension = d;
 	}
 	
 	//Draws all the objects in the buffer
 	public synchronized void draw(PointXY location) {
 		//the location for each item in the buffer
 		PointXY itemLocation = new PointXY(location);
 		//an array containing all items in the buffer (for printing only)
 		BufferSlot[] items = super.getPrintArray();
 		
 		for (int i = (items.length-1); i >= 0; i--) {
		//for (int i = 0; i < items.length; i++) {
 			
 			if (items[i] == null) {
 				//this slot is empty, don't draw it
 				//Draw an apple at location
				StdDraw.setPenColor(StdDraw.WHITE);
				StdDraw.filledCircle(itemLocation.getX(), itemLocation.getY(), 10);
 			}
 			else {
 				//calls the draw method of the item actually in the buffer
 				//eg. Apple.draw()
 				items[i].draw(itemLocation);
 			}
 			
 			//fetches the centre point of the next slot
 			itemLocation = dimension.getNextSlotDrawPoint(itemLocation);
 		}
 	}
 	
 	/**
 	 * This method moves the fruit along in the buffer by
 	 * a single space. This method is called instead of
 	 * adding a Fruit to the buffer. When this method is
 	 * called an empty space is added to the buffer instead
 	 * 
 	 * Advances the buffer forward by one
 	 */
 	public synchronized void AdvanceBuffer() {
 		if (super.isFull()) {
 			super.remove();
 		}
 		this.add(null); //add a null position to the front
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see buffer.AbstractBuffer#toString()
 	 */
 	@Override
 	public String toString() {
 		return super.toString();
 	}
 }
