 package buffer;
 
 import fruit.Fruit;
 import factory.machine.FruitConveyor;
 
 /**
  * FruitBuffer.java
  * @author:			Devin Barry
  * @date:			13.10.2012
  * @lastModified:	23.10.2012
  *
  * FruitBuffer is a concrete subclass of AbstractBuffer and the close cousin
  * of DrawableFruitBuffer.
  * 
  * Unlike DrawableFruitBuffer, FruitBuffer cannot draw itself and it is used
  * in machines where the FruitBuffer does not draw.
  * 
  * A FruitBuffer is specifically designed to hold Fruit and its subclasses.
  * 
  * Unlike DrawableFruitBuffer, FruitBuffer never stores null elements. This
  * buffer actually works as a buffer (to buffer real items) and is not part
  * of a animated conveyor or similar part. Thus it only contains real Fruit
  * never null.
  * TODO this class now extends FruitConveyor. This may be incorrect!
  */
 public class FruitBuffer extends AbstractBuffer implements FruitConveyor {
 	
 	//Default Fruit Buffer Size is 8
 	public FruitBuffer() {
 		super(8);
 	}
 	
 	//Size can be specified at creation time
 	public FruitBuffer(int size) {
 		super(size);
 	}
 	
 	//Convenience method for adding Fruit to the end of the buffer
 	public void addFruit(Fruit fruit) {
 		if (fruit == null) return; //do not allow null items to be stored in a FruitBuffer
 		super.add(fruit);
 	}
 	
 	//Convenience method for removing Fruit from the front of the buffer
 	public Fruit removeFruit() {
 		BufferSlot bs = super.remove();
 		if (bs instanceof Fruit) {
 			return (Fruit)bs;
 		}
 		throw new RuntimeException("Error, Buffer does not contain Fruit");
 	}
 	
 	/**
 	 * This method moves the fruit along in the buffer by
 	 * a single space. This method is called instead of
 	 * adding a Fruit to the buffer. When this method is
 	 * called an empty space is added to the buffer instead
 	 * 
	 * Advances the buffer forward by one. Because this method
	 * does not actually do anything it does not need to be
	 * synchronized. However if it did do something it should
	 * be, thus incase I will leave it synchronized
 	 */
 	public synchronized void AdvanceBuffer() {
 		//no action occurs for AdvanceBuffer when called on  FruitBuffer.
 		//We dont store null objects in the Buffer when it cannot draw
 	}
 }
