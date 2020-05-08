 package ab11.aufgabe3;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Baka
  * Date: 23.01.13
  * Time: 21:19
  * To change this template use File | Settings | File Templates.
  */
 public class Graph implements IGraph {
 
     private static final double NOT_EXISTENT = Double.NEGATIVE_INFINITY;
     private static final double NO_EDGE = Double.POSITIVE_INFINITY;
     private static final int DEFAULT_SIZE = 6;
 
     private List<Integer> nodes;
     private String[] nodesValues;
     private double [][] matrix;
 
 
     public Graph(){
         this.create(DEFAULT_SIZE);
     }
 
     public Graph(int size){
         this.create(size);
     }
 
     /**
      * Adds a new node if the key doesnt exist or manipulate the given on
      * @param key
      * @param value
      */
     @Override
     public void node(int key, String value) {
         if(this.nodesValues.length <= key){
             this.create(key);
         }
         if (this.matrix[key][key] == NOT_EXISTENT) this.vitalizeKey(key);
         this.nodesValues[key] = value;
     }
 
     @Override
     public void edge(int nodeA, int nodeB, double weight) {
         if (!this.exists(nodeA) || !this.exists(nodeB)) throw new RuntimeException("Node not existent"); // jaja! :D
         if(nodeA >= this.matrix.length || nodeB >= this.matrix.length){
             this.create(Math.max(nodeA,nodeB));
         }
         this.set(nodeA,nodeB,weight);
     }
 
     @Override
     public double shortestPathAsWeight(int startNode, int endNode) {
         if (!this.exists(startNode) || !this.exists(endNode)) throw new RuntimeException("Node not existent"); // jaja! :D
         return 0;
     }
 
     @Override
     public int[] shortestPathAsNodes(int startNode, int endNode) {
         if (!this.exists(startNode) || !this.exists(endNode)) throw new RuntimeException("Node not existent"); // jaja! :D
         return new int[0];
     }
 
     @Override
     public void print() {
         String adjSymbol = "#";
         String notAdjSymbol = ".";
         System.out.println("=================================");
         System.out.println("Nodes are adjetant: " + adjSymbol);
         System.out.println("Nodes are not adjetant: " + notAdjSymbol);
         System.out.println("=================================");
 
         StringBuilder sb = new StringBuilder();
 
         for(int i = 0; i < this.matrix.length; i++){
             if (this.matrix[i][i] == NOT_EXISTENT) continue;
             for(int j = 0; j < this.matrix[i].length; j++){
                 if (this.matrix[j][j] == NOT_EXISTENT) continue;
                 sb.append("\t\t");
                 if(i==j){
                     sb.append("<");
                     sb.append(i);
                     sb.append(">");
                 }
                 sb.append(this.matrix[i][j]);
                 if (this.matrix[i][j] != NO_EDGE){
                     sb.append("\t");
                 }
             }
             sb.append("\r\n");
         }
 
         System.out.println(sb.toString());
     }
 
     /**
      * Jap, diese Methode ist nicht schön, da Performance / Speicherplatz nicht sehr günstig gehandhabt werden.
      * Wir möchten für die Implementierung aber umbedingt ein 2d-Array [][] haben, da das im Src-Code einfach besser
      * aussieht (imho)
      * Aus diesem Grund gehen wir bei diesem Programm von einem gutwilligem Beutzer ein, der nur Zahlen <= 0 wählt und
      * diese auch möglichst zusammenhängend (also, 0,1,2,3,4,5,6 und nicht 1, 100, 100000, ...)
      * @param newNode
      * @return
      */
     private void create(int newNode){
         double [][] former = this.matrix != null ? this.matrix : new double[0][0];
        double [][] matrix = new double[newNode][newNode];
         int formerLength = former.length;
        for(int i = 0; i < newNode; i++){
            for(int j = 0; j < newNode; j++){
                 if (i < formerLength && j < formerLength){
                     matrix[i][j] = former[i][j];
                 }else {
                     matrix[i][j] = NOT_EXISTENT;
                 }
             }
         }
         this.matrix = matrix;
 
         // nodesValues:
         String[] formerNodes = this.nodesValues != null ? this.nodesValues : new String[0];
        String[] nodes = new String[newNode];
         for(int i = 0; i < formerNodes.length; i++){
             nodes[i] = formerNodes[i];
         }
         this.nodesValues = nodes;
         this.nodes = new ArrayList<Integer>();
     }
 
     private boolean exists(int key){
         return this.matrix.length > key ? this.matrix[key][0] != NOT_EXISTENT : false;
     }
 
     /**
      * "belebt" einen Schlüssel der Matrix. Das heißt, alle Knotenpaare werden auf NO_EDGE gestellt
       * @param key
      */
     private void vitalizeKey(int key){
         this.nodes.add(key);
         for(int i = 0; i < this.nodes.size(); i++){
             this.matrix[this.nodes.get(i)][key] = NO_EDGE;
             this.matrix[key][this.nodes.get(i)] = NO_EDGE;
         }
     }
 
     private void set(int a, int b, double weight){
         this.matrix[a][b] = weight;
         if (a!=b){
             this.matrix[b][a] = weight;
         }
     }
 }
