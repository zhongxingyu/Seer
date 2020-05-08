 package edu.cst316.gompersacousticsystem;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class graphFactoryASU {
 		
 		private List<Edge> edgeList;
 		private List<Vertex> vertexList;
 		private Graph myGraph;
 		
 		public graphFactoryASU()
 		{
 			Vertex backLeftCorner = new Vertex("back corner", 
 					"back corner", new Area(new Point(0, 0), new Point(5, 5)));
 			Vertex backRightCorner = new Vertex("back right corner", "back right corner", 
 					new Area(new Point(45, 0),new Point(50, 5) ));
			Vertex frontLeftCorner = new Vertex("Front left corner", "Front left corner",
 					new Area(new Point(0, 20), new Point(5, 25)));
 			Vertex frontRightCorner = new Vertex("Front right corner", "Front right corner", 
 					new Area(new Point(45, 20), new Point(50, 25)));
 			Vertex door = new Vertex("door", "door",
 					new Area(new Point(30, 20), new Point(35, 25)));
 			
 			vertexList = new ArrayList<Vertex>();
 			vertexList.add(backLeftCorner);
 			vertexList.add(backRightCorner);
			vertexList.add(frontLeftCorner);
 			vertexList.add(frontRightCorner);
 			vertexList.add(door);
 			
 			Edge cafToBath = new Edge("caf to bath", backLeftCorner, backRightCorner, 45);
			Edge bathToFront = new Edge("bath to front", backLeftCorner, frontLeftCorner, 20);
			Edge frontToBack = new Edge("front to back", frontLeftCorner, door, 30);
 			Edge frontToCaseTurn = new Edge("front to case turn", door, frontRightCorner, 15);
 			Edge caseTurnToManagers = new Edge("case turn to managers", backRightCorner, frontRightCorner, 20);
 			
 			edgeList = new ArrayList<Edge>();
 			
 			edgeList.add(cafToBath);
 			edgeList.add(bathToFront);
 			edgeList.add(frontToBack);
 			edgeList.add(frontToCaseTurn);
 			edgeList.add(caseTurnToManagers);
 			
 			myGraph = new Graph(vertexList, edgeList);
 		}
 		
 		public List<Vertex> getVertices()
 		{
 			return vertexList;
 		}
 		
 		public List<Edge> getEdges()
 		{
 			return edgeList;
 		}
 		
 		public Graph getGraph()
 		{
 			return myGraph;
 		}
 
 
 
 }
