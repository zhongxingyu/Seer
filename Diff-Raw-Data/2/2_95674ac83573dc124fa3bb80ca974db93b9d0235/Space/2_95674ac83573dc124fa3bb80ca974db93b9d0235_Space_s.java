 package p5;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import jdsl.graph.algo.IntegerDijkstraPathfinder;
 import jdsl.graph.api.Edge;
 import jdsl.graph.api.EdgeIterator;
 import jdsl.graph.api.VertexIterator;
 import jdsl.graph.api.Graph;
 import jdsl.graph.api.Vertex;
 import jdsl.graph.ref.IncidenceListGraph;
 import jdsl.core.api.*;
 
 public class Space {
 	private Block[] blocks;
 	ArrayList<Line> lines;
 	Map<Line, Vertex> vertices = new HashMap<Line, Vertex>();
 	private Graph graph;
 	Map<Integer, ArrayList<Line> > linesMap;
 	Vertex startVertex, endVertex;
 	Robot r;
 
 	Space(Robot _r) {
 		lines = new ArrayList<Line>();
 		this.r = _r;
 		graph = new IncidenceListGraph();
 	}
 
 	public void importBlocks(Block[] blocks) {
 		////System.out.println("IMPORTING BLOCKS TO SPACE");
 		this.blocks = blocks;
 		this.blocks[0].corners();
 		this.blocks[1].corners();
 		this.blocks[2].corners();
 	}
 
 	public int intersectsAny(Line l) {
 		int r = -1;
 		for (int i = 0; i < 3; i++)
 		{
 			////System.out.println("Checking block " + i);
 			if (blocks[i].intersects(l))
 			{
 				if (r != -1)
 					r = 3;
 				else
 					r = i;
 			}
 		}
 		////System.out.println("DID NOT INTERSECT Y'ALL");
 		return r;
 	}
 
 	public int[] getOthers(int i) {
 		int other[] = new int[2];
 		if (i == 0)
 		{
 			other[0] = 1;
 			other[1] = 2;
 		}
 		else if (i == 1)
 		{
 			other[0] = 0;
 			other[1] = 2;
 		}
 		else
 		{
 			other[0] = 0;
 			other[1] = 1;
 		}
 		return other;
 	}
 
 	public void generateLines() {
 		Position start = r.s();
 		Position end = r.e();
 		int o[] = new int[2];
 		int id = 0, newy = 0;
 		//System.out.println("GENERATING LINES");
 		int j = 0; // intersected block
 		Line line;
 
 		//System.out.println("FAR LEFT");
 		// set far left and right lines
 		line = new Line (0, 0, 0, 500, id);
 		// check for intersect at top
 		if ((j = intersectsAny(line)) == 3)
 		{
 			newy = Math.min(blocks[0].tr().Y(), blocks[1].tr().Y());
 			newy = Math.min(newy, blocks[2].tr().Y());
 			if (newy > 0)
 			{
 				line.setB(0, newy);
 				addLine(line);
 				line.print();
 				id++;
 			}
 			else {
 				//System.out.println("NOT CREATING LINE.");
 			}
 		}
 		else if (j != -1)
 		{
 			if (blocks[j].tl().Y() > 0)
 			{
 				line.setB(0, blocks[j].bl().Y());
 				addLine(line);
 				line.print();
 				id++;
 			}
 			else {
 				//System.out.println("NOT CREATING LINE.");
 			}
 		}
 		else
 		{
 			addLine(line);
 			line.print();
 			id++;
 		}
 
 		// set block lines
 		for (int i = 0; i < 3; i++)
 		{
 			//System.out.println("BLOCK " + i);
 			// top left line
 			o = getOthers(i);
 			//System.out.println("TOP LEFT");
 			line = new Line (blocks[i].tl().X(), 0, blocks[i].tl(), id);
 			if ((j = intersectsAny(line)) == 3)
 			{
 				newy = Math.max(blocks[o[0]].bl().Y(), blocks[o[1]].bl().Y());
 				if (blocks[i].tl().Y() > newy)
 				{
 					line.setT(blocks[i].tl().X(), newy);
 					addLine(line);
 					line.print();
 					id++;
 				}
 			}
 			else if (j != -1)
 			{
 				if (blocks[i].tl().Y() > blocks[j].bl().Y())
 				{
 					line.setT(blocks[i].tl().X(), blocks[j].bl().Y());
 					addLine(line);
 					line.print();
 					id++;
 				}
 			}
 			else
 			{
 				addLine(line);
 				line.print();
 				id++;
 			}
 
 			// bottom left line
 			//System.out.println("BOTTOM LEFT");
 			line = new Line(blocks[i].bl(), blocks[i].bl().X(), 500, id);
 			if ((j = intersectsAny(line)) == 3)
 			{
 				newy = Math.min(blocks[o[0]].tl().Y(), blocks[o[1]].tl().Y());
 				if (blocks[i].bl().Y() < newy)
 				{
 					line.setB(blocks[i].bl().X(), newy);
 					addLine(line);
 					line.print();
 					id++;
 				}
 				else {
 					//System.out.println("NOT CREATING LINE.");
 				}
 			}
 			else if (j != -1)
 			{
 				if (blocks[i].bl().Y() < blocks[j].tl().Y())
 				{
 					line.setB(blocks[i].bl().X(), blocks[j].tl().Y());
 					addLine(line);
 					line.print();
 					id++;
 				}
 			}
 			else
 			{
 				addLine(line);
 				line.print();
 				id++;
 			}
 
 			// top right line
 			//System.out.println("TOP RIGHT");
 			line = new Line(blocks[i].tr().X(), 0, blocks[i].tr(), id);
 			if ((j = intersectsAny(line)) == 3)
 			{
 				newy = Math.max(blocks[o[0]].bl().Y(), blocks[o[1]].bl().Y());
 				if (blocks[i].tr().Y() > newy)
 				{
 					line.setT(blocks[i].tr().X(), newy);
 					addLine(line);
 					line.print();
 					id++;
 				}
 			}
 			else if (j != -1)
 			{
 				if (blocks[i].tr().Y() > blocks[j].bl().Y())
 				{
 					line.setT(blocks[i].tr().X(), blocks[j].bl().Y());
 					addLine(line);
 					line.print();
 					id++;
 				}
 			}
 			else
 			{
 				addLine(line);
 				line.print();
 				id++;
 			}
 
 			// bottom right
 			//System.out.println("BOTTOM RIGHT");
 			line = new Line(blocks[i].br(), blocks[i].br().X(), 500, id);
 			if ((j = intersectsAny(line)) == 3)
 			{
 				newy = Math.min(blocks[o[0]].tr().Y(), blocks[o[1]].tr().Y());
 				if (blocks[i].br().Y() < newy)
 				{
 					line.setB(blocks[i].br().X(), newy);
 					addLine(line);
 					line.print();
 					id++;
 				}
 				else {
 					//System.out.println("NOT CREATING LINE.");
 				}
 			}
 			else if (j != -1)
 			{
 				//System.out.println("br.Y() " + blocks[i].br().Y() + " b[j].tr.y() " + blocks[j].tr().Y());
 				if (blocks[i].br().Y() < blocks[j].tr().Y())
 				{
 					line.setB(blocks[i].br().X(), blocks[j].tr().Y());
 					addLine(line);
 					line.print();
 					id++;
 				}
 			}
 			else
 			{
 				addLine(line);
 				line.print();
 				id++;
 			}
 		}
 	}
 	
 	public void findShortestPath(){
 		GraphSearch pathFinder = new GraphSearch();
 		//pathFinder.execute(graph, startVertex, endVertex);
 		//if(pathFinder.pathExists())
 		//	System.out.println("PATH FOUND!");
 	}
 	
 	private void addLine(Line line) {
 		
 		if (line.length() > 0) {
 			lines.add(line);
 			System.out.println("Added" + line.toString());
 		}
 	}
 	
 	public void printGraph()
 	{
 		/*VertexIterator it = graph.vertices();
 		while(it.hasNext())
 		{
 			VertexIterator neighbors = 
 			System.out.println()
 			
 		}
 		*/
 		
 	}
 	
 	public void findVerticesAndEdges() {
 		linesMap = new HashMap<Integer, ArrayList<Line>>();
 		int x = 0;
 		//places arrays of lines into map with x-value as key
 		for (Line line : lines)
 		{
 			x = line.X();
 			
 			if(!linesMap.containsKey(x)) {
 				linesMap.put(x, new ArrayList<Line>());
 			}
 			System.out.println("Adding line: ");
 			System.out.println(line);
 			linesMap.get(x).add(line);
 		}
 
 		Set<Integer> keys;
 		keys = linesMap.keySet();
 		Integer[] keyArray = new Integer[keys.size()];
 		keyArray =  keys.toArray(keyArray);
 		Arrays.sort(keyArray);
 		Vertex v1, v2;
 		for(int i=0;i<keyArray.length;i++)
 		{
 			int key = keyArray[i];
 			ArrayList<Line> lineSet = linesMap.get(key);
 
 			for (Line line : lineSet) {
 				if (vertices.containsKey(line))
 					v1 = vertices.get(line);
 				else {
 					v1 = graph.insertVertex(line);
 					vertices.put(line, v1);
 				}
 
 				if (i < lineSet.size()-2)
 					for (Line lineRight : linesMap.get(keyArray[i+1])) {
 						if (vertices.containsKey(lineRight))
 							v2 = vertices.get(lineRight);
 						else {
 							v2 = graph.insertVertex(lineRight);
 							vertices.put(lineRight, v2);
 						}
 						if (overlap(line, lineRight) || neighborSpecialCase(line,lineRight)){
 							Line midToMid = new Line(line.center(),	lineRight.center(), -1);
 							graph.insertEdge(v1, v2, midToMid);
 						}
 
 					}
 				
 				if (i > 0)
 					for (Line lineLeft : linesMap.get(keyArray[i-1])) {
 						if (vertices.containsKey(lineLeft))
 							v2 = vertices.get(lineLeft);
 						else {
 							v2 = graph.insertVertex(lineLeft);
 							vertices.put(lineLeft, v2);
 						}
 						if (overlap(line, lineLeft) || neighborSpecialCase(line,lineLeft)) {
 							Line midToMid = new Line(line.center(),	lineLeft.center(), -1);
 							graph.insertEdge(v1, v2, midToMid);
 						}
 					}
 			}
 		}
 	}
 	
	public boolean neighborSpecialCase(Line A, Line B){
 		for (int i = 0; i<blocks.length; i++){
 			if(A.center().X()>=blocks[i].tl().X() && B.center().X()<=blocks[i].tr().X()){
 				if(A.center().Y() < B.center().Y()){
 					if(blocks[i].bl().Y()>=A.top().Y() && blocks[i].tl().Y() <=B.bottom().Y()){
 						return false;
 					}
 				}
 				else if(A.center().Y()>B.center().Y()){
 					if(blocks[i].bl().Y()>=B.top().Y() && blocks[i].tl().Y() <=A.bottom().Y()){
 						return false;
 					}
 				}
 			}
 		}
 		System.out.println("\nHERE'S A SPECIAL CASE: ");
 		A.print();
 		B.print();
 		return true;
 	}
 	
 	public Line getRobotLine(Position robot, Integer id){
 		Line robotLine;
 		Integer b = 500;
 		Integer newb = 500;
 		Integer t = 0;
 		Integer newt = 0;
 		for (int i = 0; i<blocks.length; i++){
 			if(robot.Y() < blocks[i].tl().Y()){
 				if(robot.X() >= blocks[i].tl().X() && robot.X() <= blocks[i].tr().X()){
 					newb = blocks[i].tl().Y();
 					if (newb < b){
 						b = newb; 
 					}
 				}
 			}
 			else if (robot.Y() > blocks[i].tl().Y()){
 				if(robot.X() >=blocks[i].bl().X() && robot.X() <= blocks[i].br().X()){
 					newt = blocks[i].bl().Y();
 					if(newt > t){
 						t = newt;
 					}
 				}
 			}
 			else{
 				//ys equal - shouldn't encounter this case
 			}
 		}
 		robotLine = new Line( robot.X(), t, robot.X(), b, id);
 		return robotLine;
 	}
 
 	public double getDistance(Line a, Line b)
 	{
 		double xOffset = Math.abs(a.center().X() - b.center().X());
 		double yOffset = Math.abs(a.center().Y() - b.center().Y());
 		return Math.sqrt(Math.pow(xOffset, 2) + Math.pow(yOffset, 2));
 	}
 
 	//not sure if this function goes here......
 	public boolean overlap(Line A, Line B){
 		
 		if (A.length()>B.length()){
 			if (exists(B.top(),A) && B.top() != A.bottom()){
 				System.out.println("\nHERE'S AN OVERLAP: ");
 				A.print();
 				B.print();
 				return true;
 			}
 			else if (exists(B.bottom(),A) && B.bottom() != A.top()){
 				System.out.println("\nHERE'S AN OVERLAP: ");
 				A.print();
 				B.print();
 				return true;
 			}
 			else {
 				System.out.println("\nNOT AN OVERLAP CASE 1: ");
 				A.print();
 				B.print();
 				return false;
 			}
 		}
 		else if(A.length() <= B.length()){
 			if (exists(A.top(),B) && A.top() != B.bottom()){
 				System.out.println("\nHERE'S AN OVERLAP: ");
 				A.print();
 				B.print();
 				return true;
 			}
 			else if (exists( A.bottom(),B) && A.bottom() != B.top()){
 				System.out.println("\nHERE'S AN OVERLAP: ");
 				A.print();
 				B.print();
 				return true;
 			}
 			else if (A.top().Y() == B.top().Y() && A.bottom().Y()==B.bottom().Y()){
 				System.out.println("\nHERE'S AN OVERLAP: ");
 				A.print();
 				B.print();
 				return true;
 			}
 			else {
 				System.out.println("\nNOT AN OVERLAP CASE 2: ");
 				A.print();
 				B.print();
 				return false;
 			}
 		}
 		else {
 			System.out.println("\nNOT AN OVERLAP CASE 3: ");
 			A.print();
 			B.print();
 			return false;
 		}
 	}
 
 	public boolean exists(Position A, Line B){
 		if (A.Y() < B.bottom().Y() && A.Y()>B.top().Y()){
 			return true;
 		}
 		else
 			return false;
 	}
 
 
 	public void decompose() {
 		graph = new IncidenceListGraph();
 		generateLines();
 		
 		findVerticesAndEdges();
 		
 		System.out.println("Num Edges: " + graph.numEdges());
 		System.out.println("Num Vertices: " + graph.numVertices());
 	}
 
 	public void paint(Graphics g) {
 
 		int radius = 6;
 		for (int i = 0; i < lines.size(); i++) {
 			g.setColor(Color.green);
 			int topX = lines.get(i).top().X();
 			int topY = lines.get(i).top().Y();
 
 			int bottomX = lines.get(i).bottom().X();
 			int bottomY = lines.get(i).bottom().Y();
 
 
 			g.drawLine(topX, topY, bottomX, bottomY);
 		}
 		
 		Iterator it = vertices.entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        // painting midpoints
 	        System.out.println("getting..");
 			Line line = (Line) pairs.getKey();
 			System.out.println(line);
 			int topX = line.top().X();
 			int topY = line.top().Y();
 
 			int bottomX = line.bottom().X();
 			int bottomY = line.bottom().Y();
 
 			g.setColor(Color.orange);
 			g.fillOval((topX-bottomX)/2 + bottomX - radius/2, (topY-bottomY)/2 + bottomY - radius/2, radius, radius);
 			g.drawOval((topX-bottomX)/2 + bottomX - radius/2, (topY-bottomY)/2 + bottomY - radius/2, radius, radius);
 		}
 	    
 		if(!graph.isEmpty()){
 			System.out.println("num of edges: "+graph.numEdges());
 			EdgeIterator ei = graph.edges();
 			while(ei.hasNext()){
 				//System.out.println("EDGE");
 				Edge current = (Edge)ei.nextEdge();
 				Line line = (Line)current.element();
 				g.setColor(Color.blue);
 				int topX = line.top().X();
 				int topY = line.top().Y();
 				
 				int bottomX = line.bottom().X();
 				int bottomY = line.bottom().Y();
 
 				System.out.println("Line: ("+topX+","+topY+"),("+bottomX+","+bottomY+")");
 				g.drawLine(topX, topY, bottomX, bottomY);
 			}
 		}
 	}
 
 	public void clear() {
 		lines.clear();
 		vertices.clear();
 		graph = new IncidenceListGraph();
 	}
 }
 
 class GraphSearch extends IntegerDijkstraPathfinder {
 
 	@Override
 	protected int weight(Edge e) {
 		if(Line.class == e.element().getClass()){
 			Line current = (Line)e.element();
 			double weight = current.length();
 			if(weight < 0)
 				weight = 0;
 			return (int)weight;
 		}
 		else return 0;
 	} 
 	
 }
