 package worklists;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import net.sourceforge.interval.ia_math.RealInterval;
 
 import core.Box;
 
 // not a public class
 class IntervalMerger {
 	protected ArrayList<Box> boxes; 
 
 	public IntervalMerger(Box[] boxes) {
 		assert (boxes.length != 0);
 		this.boxes = new ArrayList<>(Arrays.asList(boxes));
 	}
 
 	public Box[] merge() {
 		int size;
 		do {
 			size = boxes.size();
 			assert (size != 0);
			mergeOnce();			
 		} while (boxes.size() != size);
 		return boxes.toArray(new Box[0]);
 	}
 
 	private void mergeOnce() {
 		Box prevBox = null, thisBox = boxes.get(0);
 		final int dim = thisBox.getDimension(); 
 		for (int side = 0; side < dim; side++) {
 			Collections.sort(boxes, new SideSorter(side));
 			thisBox = boxes.get(0);
 			for (int i = 1; i < boxes.size(); /**/) {
 				prevBox = thisBox;
 				thisBox = boxes.get(i);
 				if (boxesFollowedOneAnother(thisBox, prevBox, side) &&
 						allTouchingSidesAreEqual(thisBox, prevBox, side) ) {
 					Box newBox = mergeBoxes(thisBox, prevBox, side);
 					boxes.add(i+1, newBox);
 					boxes.remove(i);
 					boxes.remove(i-1);
 				} else
 					i++;
 			}
 		}
 	}
 
 	boolean boxesFollowedOneAnother(Box two, Box one, int side) {
 		RealInterval i1 = one.getInterval(side);
 		RealInterval i2 = two.getInterval(side);
 		if (i1.hi() == i2.lo() || 
 				i1.lo() == i2.hi() ) { // this part just in case somebody would call this function
 										// with another order of boxes
 			return true;
 		}
 		return false;
 	}
 	boolean allTouchingSidesAreEqual(Box b1, Box b2, int side) {
 		final int dim = b1.getDimension();
		for (int i = 1; i <= dim/2; i++) {
 			int touchingSideNum = (side+i)%dim;
 			if ( !b1.getInterval(touchingSideNum).equals( b2.getInterval(touchingSideNum) ) )
 				return false;
			touchingSideNum = (side-i)%dim;
			if ( !b1.getInterval(touchingSideNum).equals( b2.getInterval(touchingSideNum) ) )
				return false;
 		}
 		return true;
 	}
 	Box mergeBoxes(Box b1, Box b2, int side) {
 		Box box = b1.clone();
 		box.setFunctionValue(new RealInterval()); // flush function estimation
 		RealInterval s1 = b1.getInterval(side);
 		RealInterval s2 = b2.getInterval(side);
 		double l, h;
 		if (s1.lo() < s2.lo()) {
 			l = s1.lo();
 			h = s2.hi();
 		} else {
 			l = s2.lo();
 			h = s1.hi();
 		}
 		box.setInterval(side, new RealInterval(l, h));
 		return box;
 	}
 }
