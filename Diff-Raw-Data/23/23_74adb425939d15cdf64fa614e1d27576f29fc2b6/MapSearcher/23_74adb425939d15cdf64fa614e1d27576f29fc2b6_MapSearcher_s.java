 import java.util.ArrayList;
 import java.util.List;
 import java.util.LinkedList;
 import visualizer.*;
 
 public class MapSearcher extends MapExtractor{
     MakeTree map;
     
     public MapSearcher (MakeTree map_) {
         map = map_;
     }
 
 	public LinkedList<GEdge> getEdges(int maxy, int miny, int maxx, int minx){
 
 //		System.out.println("maxx"+maxx+ "   minx"+minx+ "   maxy"+maxy+ "  miny"+miny);
 		List<Vertex> vertexList = findInMap(minx,miny,maxx-minx,maxy-miny);
		int[] vertexMap=map. generateVertexMap(map.vertices);
 		
 		LinkedList<GEdge> edgeList= new LinkedList<GEdge>();
 //		System.out.println(vertexList.size());
 		
 		for (Vertex v: vertexList ){
 			for (Edge e: v.edges){
				if ((e==null)||(vertexMap[e.vertexIdx]==0)) continue;
				Vertex w=map.vertices[vertexMap[e.vertexIdx]];
 				if  (v.id < w.id)
 					edgeList.add(new GEdge(v.y,v.x,w.y,w.x));
 			}			
 		}
 		return edgeList;
 	}
 
     
 	
 	List<Vertex> findInMap(int x, int y, int width, int height) {
         ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
         
         return findInRect(new MapRect(x,y,width,height), vertexList);
      
     }
     
     List<Vertex> findInRect(MapRect rect, List<Vertex> vertexList) {
         Vertex[] vertices = map.getVertexArray();
         
         addMatchingVertices(rect, vertices, 0, vertices.length, true, vertexList);
 
         return vertexList;
     }
 
     void addMatchingVertices(MapRect rect, Vertex[] vertices, int start, int end, boolean xAxis, List<Vertex> vertexList) {
         int medianIdx = (start+end)/2;
         
         if (end - start <= 1) {
             vertexList.add(vertices[medianIdx]);
             return;
         }
         
         VertexComparator comparator = new VertexComparator(xAxis);
         
         if (comparator.compare(vertices[medianIdx], rect) <= 0)
             addMatchingVertices(rect, vertices, medianIdx, end, !xAxis, vertexList);
         
         if (comparator.compare(vertices[medianIdx], rect) >= 0)
             addMatchingVertices(rect, vertices, start, medianIdx, !xAxis, vertexList);
     }
}
