 package behaviors;
 
 import java.util.ArrayList;
 import artlife.*;
 
 public class SCAN_TERRAIN extends Behavior {
 
 	public SCAN_TERRAIN(int numBehs) {
 		super(numBehs, 2);
 	}
 	
 	public SCAN_TERRAIN(int numbehs, ArrayList<Integer> n) {
 		super(numbehs, 2, n);
 	}
 	
 
 	@Override
 	public int perform(Grid grid, Organism self) {
 		terrain t = grid.terrainAt(self.getX()+self.getDir().dx, self.getY()+self.getDir().dy);
 		if(t.cost(self.getMode())>grid.terrainAt(self.getX(), self.getY()).cost(self.getMode()))
 			return next(1);
 		return next(0);
 	}
 
 	@Override
 	public Behavior clone() {
		SCAN_TERRAIN temp = new SCAN_TERRAIN(numBehs,next);
 		return temp;
 	}
 
 	@Override
 	public Behavior mutate() {
 		return Behavior.mutate(clone());
 	}
 
 }
