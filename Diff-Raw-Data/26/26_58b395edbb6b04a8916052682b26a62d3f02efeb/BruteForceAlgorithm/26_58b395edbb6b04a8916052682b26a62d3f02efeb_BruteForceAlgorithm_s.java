 package tspsolver.model.algorithm.start;
 
 import java.util.Vector;
 
 import tspsolver.model.algorithm.StartAlgorithm;
 import tspsolver.model.scenario.grid.Edge;
 import tspsolver.model.scenario.grid.Vertex;
 import tspsolver.model.scenario.path.Path;
 import tspsolver.model.scenario.path.PathUpdater;
 
 public class BruteForceAlgorithm extends StartAlgorithm {
 
 	private final Vector<Vertex> startPath;
 	private final Vector<Vertex> currentPath;
 
 	public BruteForceAlgorithm() {
 		this.startPath = new Vector<Vertex>();
 		this.currentPath = new Vector<Vertex>();
 	}
 
 	@Override
 	protected void doInitialize() {
 		this.getPathUpdater().addPath(new Path());
 
 		// convert to vertex path
 		Vertex startingVertex = this.getStartingVertex();
 
 		for (Vertex vertex : this.getGrid().getVertices()) {
 			if (startingVertex != vertex) {
 				this.startPath.add(vertex);
 			}
 		}
 
 		this.currentPath.addAll(this.startPath);
 	}
 
 	@Override
 	protected void doReset() {
 		this.startPath.clear();
 		this.currentPath.clear();
 	}
 
 	@Override
 	protected boolean doStep() {
 		boolean successfulStep = true;
 
 		boolean lastPermutation = this.nextPermutation();
 
 		// Create the new path
 		Path newPath = this.convertToPath(this.currentPath);
 
 		// Check if the new path is lighter
		if (this.getPath().isEmpty() || (!newPath.isEmpty() && newPath.getWeight() < this.getPath().getWeight())) {
 			this.getPathUpdater().clearPath();
 			this.getPathUpdater().addPath(newPath);
 		}
 
 		// Check if we are finished
 		if (lastPermutation) {
 			if (!this.getPath().isEmpty()) {
 				this.finishedSuccessfully();
			}
			else {
 				successfulStep = false;
 			}
 		}
 
 		this.getPathUpdater().updatePath();
 		return successfulStep;
 	}
 
 	private boolean nextPermutation() {
 
 		// letztes Element; man arbeitet sich von hinten nach vorne durch
 		int i = this.currentPath.size() - 1; //
 
 		// keine Endlosschleife, da i dekrementiert wird und damit irgendwann 0
 		// wird
 		while (true) {
 			if (i <= 0) {
 				// a ist letzte Permutation
 				return true;
 			}
 			i -= 1;
 			// lexikogr. Nachfolger hat größeres a[i]
			if (this.startPath.indexOf(this.currentPath.get(i)) < this.startPath.indexOf(this.currentPath.get(i + 1))) {
 				break;
 			}
 		}
 		int j = this.currentPath.size();
 		while (true) {
 			j -= 1;
			if (this.startPath.indexOf(this.currentPath.get(i)) < this.startPath.indexOf(this.currentPath.get(j))) {
 				break;
 			}
 		}
 
 		this.swap(i, j);
 		// sortiere aufsteigend zwischen a[i] und Ende
 		// zur Zeit absteigend sortiert => invertieren
 
 		i += 1;
 		j = this.currentPath.size() - 1;
 
 		while (i < j) {
 			this.swap(i, j);
 			i += 1;
 			j -= 1;
 		}
 
 		// eine weitere Permutation gefunden
 		return false;
 	}
 
 	private void swap(int first, int second) {
 		Vertex temp = this.currentPath.get(first);
 
 		this.currentPath.set(first, this.currentPath.get(second));
 
 		this.currentPath.set(second, temp);
 	}
 
 	private Path convertToPath(Vector<Vertex> vertices) {
 		Path newPath = new Path();
 		PathUpdater newPathUpdater = new PathUpdater(newPath);
 
 		boolean isNewPathValid = true;
 		Vertex currentVertex = this.getStartingVertex();
 
 		// Link all vertices
 		for (Vertex vertex : vertices) {
 			Edge edge = currentVertex.getEdgeToVertex(vertex);
 
 			if (edge != null) {
 				newPathUpdater.addEdge(edge);
 				currentVertex = vertex;
			}
			else {
 				isNewPathValid = false;
 			}
 		}
 
 		// Link the last vertex with the starting vertex
 		Edge edge = currentVertex.getEdgeToVertex(this.getStartingVertex());
 		if (edge != null) {
 			newPathUpdater.addEdge(edge);
		}
		else {
 			isNewPathValid = false;
 		}
 
 		// Update the path if it is valid
 		if (isNewPathValid) {
 			newPathUpdater.updatePath();
 		}
 
 		return newPath;
 	}
 }
