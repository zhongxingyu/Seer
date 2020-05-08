 package com.starscriber.dijkstra.matrix;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.starscriber.dijkstra.Dijkstra;
 import com.starscriber.dijkstra.Edge;
 import com.starscriber.dijkstra.Graph;
 import com.starscriber.dijkstra.Vertex;
 
 public class StepsMatrix {
 	protected int[][] matrix;
 	private List<Edge> edges;
 	private List<Vertex> vertices;
 
 	public StepsMatrix(Graph graph) {
 		vertices = graph.getVertices();
 		edges = graph.getEdges();
 		matrix = new int[vertices.size()][vertices.size()];
 	}
 
 	/**
 	 * Creates connection matrix of the vertices. If a vertex is connected to
 	 * another matrix the connection is shown as 1, else 0 in the matrix.
 	 */
 	public void createMatrix() {
 		int length = vertices.size();
 		Graph graph = new Graph(vertices, edges);
 		Vertex v = null;
 		Dijkstra d = null;
 		for (int i = 0; i < length; i++) {
 			v = vertices.get(i);
 			for (int j = 0; j < length; j++) {
 				d = new Dijkstra(graph);
 				if (i == j) {
 					matrix[i][j] = 0;
 				} else {
 					d.prepareData(v);
 					List<Vertex> path = d.getShortestPath(vertices.get(j));
					matrix[i][j] = path.size() - 1;
 				}
 			}
 		}
 	}// end of createMatrix()
 
 	public void prepareDiameterAndCentrality() {
 		int length = vertices.size();
 		Vertex v = null;
 		DecimalFormat df = new DecimalFormat("###.###");
 		for (int i = 0; i < length; i++) {
 			int sum = 0;
 			int j = 0;
 			for (j = 0; j < length; j++) {
 				sum = sum + matrix[i][j];
 			}
 			float centrality = Float.parseFloat(df.format((float) 1/sum));
 			v = vertices.get(i);
 			v.setDiameter(sum);
 			v.setCentrality(Float.parseFloat(df.format(centrality)));
 		}
 	}
 
 	/**
 	 * Prints the connection matrix. Call only when created else a matrix of all
 	 * ZEROs will be shown.
 	 */
 	public void showMatrix() {
 		int length = vertices.size();
 		Vertex v = null;
 		for (int i = 0; i < length; i++) {
 			for (int j = 0; j < length; j++) {
 				if (matrix[i][j] > 9) {
 					System.out.print(matrix[i][j] + "  ");
 				} else {
 					System.out.print(matrix[i][j] + "   ");
 				}
 			}
 			v = vertices.get(i);
 			System.out.print(v.getDiameter() + "  ");
 			System.out.print(v.getCentrality());
 			System.out.println();
 		}
 	}// end of showMatrix()
 
 	public List<Vertex> addSteps() {
 		int length = vertices.size();
 		Vertex v = null;
 		List<Integer> steps = null;
 
 		for (int i = 0; i < length; i++) {
 			v = vertices.get(i);
 			steps = new ArrayList<Integer>();
 			for (int j = 0; j < length; j++) {
 				steps.add(matrix[i][j]);
 			}
 			v.setSteps(steps);
 		}
 
 		return vertices;
 	}
 
 	public int[][] getMatrix() {
 		return matrix;
 	}
 
 	public void setMatrix(int[][] matrix) {
 		this.matrix = matrix;
 	}
 }//end of class
