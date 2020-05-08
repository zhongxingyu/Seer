 package tsp;
 
 import Graph.*;
 import AdjMatrix.IllegalAdjMatrixOperation;
 
 /**
  *
  * @author lucasdavid
  */
 public class Tsp extends Graph {
 
     public Tsp(int _nodes, int _max_edge_value, int _min_edge_value) {
         super(_nodes);
         if (_nodes < 4) {
             throw new IllegalArgumentException(
                     "TSP is not applicable for graph instances with " + _nodes + " nodes or below.");
         }
         
         RandomInit(_nodes, _max_edge_value, _min_edge_value);
         m.validContext = true;
     }
 
     public Tsp(double[][] _m) {
         super(_m);
     }
 
     private void RandomInit(int _nodes, int _max_edge_value, int _min_edge_value) {
 
         if (m.validContext) {
             throw new IllegalAdjMatrixOperation();
         }
 
         for (int i = 0; i < m.m.length; i++) {
             m.m[i][i] = 0;
             
             for (int j = i +1; j < m.m.length; j++) {
                 m.m[i][j] = m.m[j][i] = (double) (Math.random() * _max_edge_value);
                 
                 if (m.m[i][j] < _min_edge_value) {
                     m.m[i][j] = m.m[j][i] = m.m[i][j] + _min_edge_value;
                     
                     if (m.m[i][j] > _max_edge_value)
                         m.m[i][j] = m.m[j][i] = m.m[i][j] + _max_edge_value;
                 }
             }
         }
     }
 
//    private int[][] ReadFromFile(String _file) {
//        return null;
//    }
     public String report() {
         return new String();
     }
 
     public void print() {
         for (int i = 0; i < m.m.length; i++) {
             for (int j = 0; j < m.m.length; j++) {
                 System.out.print(m.m[i][j] + "  ");
             }
 
             System.out.println("");
         }
         System.out.println("---\n" +m.nodes + " nodes\n\n");
     }
 }
