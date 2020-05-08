 package visibilityGraph;
 import geometry.GeometryException;
 import geometry.Point;
 import geometry.Vector;
 import java.util.ArrayList;
 import java.util.Collections;
 import octree.OctNode;
 import octree.OctNodeException;
 import octree.Octree;
 import octree.OctreeException;
 
 public class Graph {
     private ArrayList<GraphNode> nodes = new ArrayList<GraphNode>();
     private ArrayList<GraphEdge> edges = new ArrayList<GraphEdge>();
     private int numOfDims;
     private Octree surface;
     
     public Graph(ArrayList<Point> nds, int dims, Octree tree) throws GraphException,GraphNodeException{
         if(dims < 1 )
             throw new InvalidGraphNumberOfDimensionsException();
         if(nds.isEmpty())
             throw new EmptyNodeSetException();
         
         numOfDims = dims;
         for(Point p : nds){
             nodes.add(new GraphNode(dims, p));
         }
         surface = tree;
         
     }
     
     public int getNumOfDims(){
             return this.numOfDims;
     }
     
     public GraphNode getNodeAtPosition(int x){
         return nodes.get(x);
     }
     
     public GraphEdge getEdgeAtPosition(int x){
         return edges.get(x);
     }
     
     private void recurseGetOctreeLeafs(Point origin, Vector ray, ArrayList<Point> visible, OctNode root) throws GeometryException, OctNodeException{
         if(root.getBoundingBox().intersectWithRay(ray, origin, Boolean.FALSE)){
             if(root.getNodeType() == OctNode.OCTNODE_LEAF){
                 visible.add(root.getPoint());
             }
             if(root.getNodeType() == OctNode.OCTNODE_INTERMEDIATE){
                 ArrayList<OctNode> children = root.getChildren();
                 for(OctNode n : children)
                     recurseGetOctreeLeafs(origin,ray,visible,n);
             }
         }
     }
     
     private ArrayList<Point> getOctreeLeafs(Point origin,Vector ray) throws GeometryException, OctNodeException{
         ArrayList<Point> visible = new ArrayList<Point>();
         recurseGetOctreeLeafs(origin,ray,visible,this.surface.getRoot());
         return visible;
     }
     
     public void iterateToCreateEdges() throws GeometryException, OctNodeException, GraphException, GraphEdgeException,OctreeException{
         for(int i=0;i<nodes.size();i++){
             for(int j=i+1;j<nodes.size();j++){
                 //create ray and call octree code here
                 Vector ray = new Vector(nodes.get(i).getPoint(),nodes.get(j).getPoint());
                 ArrayList<Point> visibleList = getOctreeLeafs(nodes.get(i).getPoint(), ray);
                 ArrayList<Float> projections = new ArrayList<Float>();
                 for(Point p : visibleList){
                     Vector v = new Vector(p);
                     projections.add(ray.getProjection(v));
                 }
                 if(projections.isEmpty())
                     throw new ZeroProjectedClustersException();
                 Collections.sort(projections);
                 boolean visible=false;
                 int clusterCount = 1;
                 float D = surface.getMinNodeLength();
                 float distance;
                 for(int k=1;(k<projections.size() && clusterCount < 3);k++){
                    distance = projections.get(i) - projections.get(i-1);
                     if(distance > (1.5f*D))
                         clusterCount++;
                 }
                 switch(clusterCount){
                     case 1:
                         visible=true;
                         break;
                     case 2:
                         //arkoudia here
                         if(surface.getSignForPointInSpace(nodes.get(i).getPoint().midpointFromPoint(nodes.get(j).getPoint())))
                             visible=true;
                         break;
                     case 3:
                         visible=false;
                         break;
                     default:
                         throw new GraphException("Default switch case on clusters");
                 }
                 if(visible)
                     edges.add(new GraphEdge(i, j, nodes.get(i).getPoint().minkowskiDistanceFrom(nodes.get(j).getPoint(), 2)));
             }
         }
     }
     
     private ArrayList<Integer> getNeighboors(int node){
         ArrayList<Integer> neighboors = new ArrayList<Integer>();
         for(GraphEdge e : this.edges){
             if(e.getNodes()[0] == node){
                 neighboors.add(e.getNodes()[1]);
             }
             if(e.getNodes()[1] == node){
                 neighboors.add(e.getNodes()[0]);
             }
         }
         return neighboors;
     }
     
     private Float calculateShortestPathDijkstra(int start, int end){
         Float[] tentative = new Float[nodes.size()];
         tentative[start] = 0.0f;
         for(int i=0;i<nodes.size();i++){
             if(i!=start)
                 tentative[i] = Float.POSITIVE_INFINITY;
         }
         int current = start;
         ArrayList<Integer> visited = new ArrayList<Integer>();
         ArrayList<Integer> neighboors;
         while(!visited.contains(end)){
             neighboors = getNeighboors(current);
             for(Integer i : neighboors){
                 Float weight = 0.0f;
                 for(GraphEdge e : this.edges){
                     Integer[] nds = e.getNodes();
                     if(nds[0] == current && nds[1] == i)
                         weight = e.getWeight();
                     if(nds[1] == current && nds[0] == i)
                         weight = e.getWeight();
                 }
                 tentative[i] = tentative[current] + weight;
             }
             visited.add(current);
             Float minDist = Float.POSITIVE_INFINITY;
             int next = -1;
             for(int i=0;i<tentative.length;i++){
                 if((!visited.contains(i)) && (tentative[i] < minDist)){
                     minDist = tentative[i];
                     next = i;
                 }
             }
             if(next == -1)
                 break;
             current = next;
         }
         return tentative[end];
     }
     
     public ArrayList<Float> getShortestPathCosts(){
         ArrayList<Float> costs = new ArrayList<Float>();
         for(int i=0;i<nodes.size();i++){
             for(int j=i+1;j<nodes.size();j++){
                 costs.add(calculateShortestPathDijkstra(i, j));
             }
         }    
         return costs;
     }
     
 }
