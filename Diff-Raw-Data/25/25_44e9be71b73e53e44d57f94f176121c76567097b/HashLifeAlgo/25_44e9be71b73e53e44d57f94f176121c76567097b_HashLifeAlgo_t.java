 package life.hashlife;
 
 import life.LifeAlgo;
 import life.LifeDrawer;
 import life.LifeState;
 
 public class HashLifeAlgo implements LifeAlgo {
 	
 	HashLifeState s;
 	
 	@Override
 	public void setState(LifeState state) {
 		if (state instanceof HashLifeState) {
			s = ((HashLifeState) state).copy();
 		} else {
 			throw new RuntimeException("NaiveLife.loadState needs a HashLifeState");
 		}
 	}
 
 	@Override
 	public LifeState getState() {
 		return s.copy();
 	}
 
 	@Override
 	public LifeDrawer getDrawer() {
 		return new HashLifeDrawer();
 	}
 
 	@Override
 	public void loadFromArray(int[][] array) {
 		s = new HashLifeState(array);
 	}
 
 	@Override
 	public int[][] saveToArray() {
 		return s.toArray();
 	}
 
 	@Override
 	public int getCellAt(int x, int y) {
 		return s.getCellAt(x, y);
 	}
 
 	@Override
 	public void setCellAt(int x, int y, int status) {
 		s.setCellAt(x, y, status);
 	}
 
 	@Override
 	public int toggleCellAt(int x, int y) {
 		int state = s.getCellAt(x, y);
 		s.setCellAt(x, y, 1 - state);
 		return state;
 	}
 
 	@Override
 	public void evolve(int steps) {
 		s.evolve(steps);
 	}
 
 }
