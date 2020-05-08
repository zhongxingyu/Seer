 import java.util.Collection;
 
 
 public class GOLCell extends AbstractCell<MooreNeighbourhood, Boolean> {
 
 	public GOLCell(Boolean initState) {
 		super(initState);
 	}
 	
 	public void updateNextState() {
 		Collection<Cell<MooreNeighbourhood, Boolean>> neighbourCells = this.neigbours.values();
 		int liveCellCount = 0;
 		for(Cell<MooreNeighbourhood, Boolean> cell : neighbourCells) {
 			if (cell.getState()) liveCellCount++;
 		}
 		
		if (this.state) {
			// underpopulated or overcrowded
			if(liveCellCount < 2 || liveCellCount > 3) {
				this.nextState = false;
			} else {
				this.nextState = this.state;
			}
		} else {
			// reproduce
			if(liveCellCount == 3) this.nextState = true;
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return (state ? "#" : "_");
 	}
 
 }
