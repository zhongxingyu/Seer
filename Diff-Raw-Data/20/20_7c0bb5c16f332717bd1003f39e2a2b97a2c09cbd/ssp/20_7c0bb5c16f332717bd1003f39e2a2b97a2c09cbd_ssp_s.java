 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 
 
 class BinaryHeap {
 
 }
 
 class FibonacciHeap{
 
 }
 
 class Edge {
     public int to;
     public int from;
     public int weight;
     public void Edge(int from, int to, int weight){
         this.to = to;
         this.from = from;
         this.weight = weight;
     }
 }
 
 class ListNode {
     int toVertex;
     int weight;
     public ListNode next;
     public ListNode(int vertex,int weight){
         this.toVertex = vertex;
         this.weight = weight;
         this.next = null;
     }
     public int to(){
         return toVertex;
     }
     public int weight(){
         return weight;
     }
 }
 
 class LinkedList implements Iterable<ListNode>{
     public ListNode head;
     private ListNode last;
     private int size;
     public LinkedList(){
         this.head = null;
         last = head;
     }
     public void add(int vertex, int weight){
         if(last==null){
            this.head = new ListNode(vertex, weight);
            last  = head;
         }
         else{
             last.next = new ListNode(vertex, weight);
             last = last.next;
         }
     }
     public String toString(){
         ListNode l = head;
         String s = "";
         while(l!=null){
             s = s + "--" + l.toVertex+"  "+l.weight;
             l = l.next;
         }
 
         return s;
 
     }
     public Iterator<ListNode> iterator()  {
         return new ListIterator();
     }
 
     //an iterator, doesn't implement remove() since it's optional
     private class ListIterator implements Iterator<ListNode> {
         private ListNode current = head;
 
         public boolean hasNext()  { return current != null;                     }
         public void remove()      { throw new UnsupportedOperationException();  }
 
         public ListNode next() {
             if (!hasNext()) throw new NoSuchElementException();
             ListNode item = current;
             current = current.next;
             return item;
         }
     }
 }
 
 class Graph {
    public static final double INFINITY = Double.MAX_VALUE;
     private int vertices;
     public LinkedList[] adj;
 
     public Graph(int vertices){
         this.vertices = vertices;
         adj = new LinkedList[vertices];
         for(int i=0; i<vertices;i++ )
            adj[i] = new LinkedList();
     }
 
     public void RandomGraph(int density){
         int edges = density*vertices*(vertices-1)/100;
         for(int i=0; i<edges;i++)
         {
             int x=(int) Math.random()%vertices;
             int y=(int) Math.random()%vertices;
             int weight = (int) Math.round(100 * Math.random());
             adj[x].add(y, weight);
         }
         //Check Connected ness
        }
     public void RandomGraph(){
       //  for(int i=0; i<vertices;i++)
            for(int j=0; j<vertices;j++ ){
                 int weight = (int) Math.round(100 * Math.random());
                 System.out.println(weight);
                 adj[j].add((j+1)%vertices, weight);
             }
     }
 
     public void print(){
         for(int i=0; i<vertices;i++ )
             System.out.println(i + "  " + adj[i].toString());
     }
 
     private void addEdge(int vertex, int weight){
     }
     public int noOfVertices(){
         return vertices;
     }
 
 }
 
 interface PQ {
     public int delMin();
     public void decreaseKey();
     public void insert(int vertex, int weight);
     public boolean isEmpty();
     public boolean contains();
 }
 
 class ArrayQueue {
     private int[] dist;
     private boolean[] visited;
     private int size;
     public ArrayQueue(int vertices){
         this.size  = vertices;
         this.dist = new int[size];
         this.visited = new boolean[size];
         for(int i=0;i<size;i++)
            dist[i]= (int) Graph.INFINITY;
         for(int i=0;i<size;i++)
             visited[i]=false;
     }
     public int delMin(){
         int v=0;
         for (int i=0;i<size;i++)
             if(visited[i] == false && (dist[i] < dist[v]))
                 v=i;
         visited[v]=true;
         return v;
     }
     public void decreaseKey(int vertex, int newValue){
         dist[vertex] = newValue;
     }
 
     public void insert(int vertex, int weight){
         dist[vertex]=weight;
     }
 
     public boolean isEmpty(){
         // wrong for non Connected graph
         boolean x=true;
         for(int i=0;i<size;i++)
            x &= visited[i];
         return x;
     }
     public boolean contains(int vertex){
        if(dist[vertex] == -1)
             return false;
         else
             return true;
 
     }
 }
 
 class Dsp {
     private int[] dist;
     private int[] edge;
     private ArrayQueue pq;
     Graph g;
 
     public Dsp(Graph G, int source){
         dist = new int[G.noOfVertices()];
         edge = new int[G.noOfVertices()];
         this.g = G;
         pq = new ArrayQueue(G.noOfVertices());
         for (int v = 0; v < G.noOfVertices(); v++)
            dist[v] = (int)Graph.INFINITY;
         dist[source] = 0;
 
         //relax vertices in order of distance from send
         pq.insert(source, dist[source]);
         while (!pq.isEmpty()) {
             int v = pq.delMin();
             for ( ListNode e : G.adj[v])
                 relax(e, v);
         }
     }
     void relax(ListNode e, int v){
         int w = e.to();
         if (dist[w] > dist[v] + e.weight()) {
             dist[w] = dist[v] + e.weight();
             edge[w] = e.to();
             if (pq.contains(w)) pq.decreaseKey(w, dist[w]);
             else                pq.insert(w, dist[w]);
         }
     }
 
     public void print(){
         for(int i=0;i < g.noOfVertices(); i++)
             System.out.println(dist[i]);
     }
 
 }
 
 public class ssp {
     public static void main(String[] args){
        Graph G=new Graph(5);
         G.RandomGraph();
         G.print();
         Dsp d = new Dsp(G, 1);
         d.print();
     }
 }
