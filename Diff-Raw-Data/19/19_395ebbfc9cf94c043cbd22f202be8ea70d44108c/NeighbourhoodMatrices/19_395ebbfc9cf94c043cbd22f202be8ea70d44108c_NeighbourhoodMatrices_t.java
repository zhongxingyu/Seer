 package wind;
 
 public class NeighbourhoodMatrices extends BaseMatrices {
 
 	public NeighbourhoodMatrices(int cols, int rows, AlgorithmBase updateAlgorithm) {
 		super(cols, rows, updateAlgorithm);
 	}
 	
 	public void update() {
//		WindCell targetCell = this.otherMatrix().getCell(1, 1);
		_algorithm.calculateResultVector(this);
 	}
 
 	@Override
 	public NeighbourhoodMatrix[] setupMatrices(int cols, int rows) {
 		NeighbourhoodMatrix matrices[] = new NeighbourhoodMatrix[2];
 		matrices[0] = new NeighbourhoodMatrix(cols, rows);
 		matrices[1] = new NeighbourhoodMatrix(cols, rows);
 		return matrices;
 	}
 
 }
