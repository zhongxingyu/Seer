 package jsk.sudoku.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public final class Cell {
 	
 	private final Possibilities possibilities;
 	private int value = -1;
 	private Collection<CellListener> listeners = new ArrayList<CellListener>();
 	
 	Cell(Cell cell) {
 		possibilities = new Possibilities(cell.possibilities);
 	}
 	
 	Cell(int possibilities) {
 		this.possibilities = new Possibilities(possibilities);
 	}
 	
 	public int getValue() {
 		return value;
 	}
 	
 	public boolean isSolved() {
 		return value != -1;
 	}
 	
 	public Possibilities getPossibilities() {
 		return new Possibilities(possibilities);
 	}
 	
 	public void solve(int value) {
 		if (!isSolved()) {
 			possibilities.is(value);
 			solved();
 		}
 	}
 	
 	public void eliminate(int value) {
 		if (possibilities.remove(value)) {
 			int newSize = possibilities.size();
 			if (newSize == 0) {
 				throw new IllegalStateException("All values have been eliminated. A mistake has been made.");
 			} else if (newSize == 1) {
 				solved();
 			} else {
 				changed();
 			}
 		}
 	}
 	
 	public void mustBe(Possibilities possibilities) {
 		if (this.possibilities.retainAll(possibilities))
 			changed();
 	}
 	
 	public void mustBe(int... possibilities) {
 		Possibilities p = new Possibilities(0);
 		for (int poss : possibilities) {
 			p.add(poss);
 		}
 		mustBe(p);
 	}
 	
 	public boolean couldBe(int value) {
 		return possibilities.contains(value);
 	}
 	
 	public void addListener(CellListener listener) {
 		listeners.add(listener);
 	}
 	
 	// TODO Eliminate or Improve
 	public String toString() {
 		return (value == -1 ? possibilities : value).toString();
 	}
 	
 	private void solved() {
 		assert possibilities.size() == 1;
 		value = possibilities.value();
 		
 		for (CellListener listener : listeners) {
 			listener.solved(this);
 		}
 	}
 
 	private void changed() {
 		for (CellListener listener : listeners) {
 			listener.cellChanged(this);
 		}
 	}
 
 }
