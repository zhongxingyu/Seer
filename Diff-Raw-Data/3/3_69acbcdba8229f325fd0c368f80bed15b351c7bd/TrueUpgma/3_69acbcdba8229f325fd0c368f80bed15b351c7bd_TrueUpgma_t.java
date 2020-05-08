 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ua.edu.lnu.cluster.upgma;
 
 import ua.edu.lnu.cluster.algorithm.api.ClusterTree;
 import ua.edu.lnu.cluster.algorithm.api.HierarchicalClustering;
 import java.util.HashMap;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import org.openide.util.NotImplementedException;
import org.openide.util.lookup.ServiceProvider;
 
 /**
  *
  * @author Taras
  */
@ServiceProvider(service=HierarchicalClustering.class)
 public class TrueUpgma implements HierarchicalClustering {
 
     private double[][] matrix = new double[0][0];
 //    {{0.095, 0.0, 0.0, 0.0},
 //        {0.113, 0.118, 0.0, 0.0},
 //        {0.183, 0.201, 0.195, 0.0},
 //        {0.212, 0.225, 0.225, 0.222}
 //    };
     private double[][] input = new double[0][0];
 //    {{0.095, 0.0, 0.0, 0.0},
 //        {0.113, 0.118, 0.0, 0.0},
 //        {0.183, 0.201, 0.195, 0.0},
 //        {0.212, 0.225, 0.225, 0.222}
 //    };
     private String[] clusters = new String[]{};//{"1", "2", "3", "4"};
     private JTree tree;
     private double minElement;
     private int minRow, minColumn;
     private HashMap<String, DefaultMutableTreeNode> map;
 
     public TrueUpgma() {
         tree = new JTree();
         map = new HashMap<String, DefaultMutableTreeNode>();
         minElement = -1;
         minRow = -1;
         minColumn = -1;
     }
 
     public TrueUpgma(double[][] arr) {
         init(arr);
     }
 
     private void init(double[][] arr) {
         matrix = new double[arr.length][arr.length];
         clusters = new String[arr.length + 1];
         map = new HashMap<String, DefaultMutableTreeNode>();
 
         for (int i = 0; i < arr.length + 1; i++) {
             clusters[i] = Integer.toString(i);
         }
 
         for (int i = 0; i < arr.length; i++) {
             System.arraycopy(arr[i], 0, matrix[i], 0, arr.length);
         }
 
         for (int i = 0; i < clusters.length; i++) {
             map.put(clusters[i], null);
         }
 
         input = new double[arr.length][arr.length];
         input = matrix;
         tree = new JTree();
         minElement = -1;
         minRow = -1;
         minColumn = -1;
     }
 
     public void findMin(double[][] arr) {
         minElement = arr[0][0];
         minRow = 0;
         minColumn = 0;
 
         for (int i = 0; i < arr.length; i++) {
             for (int j = 0; j < arr.length; j++) {
                 if (arr[i][j] < minElement && arr[i][j] != 0) {
                     minElement = arr[i][j];
                     minRow = i;
                     minColumn = j;
                 }
             }
         }
     }
 
     public int[] getNumbers(String cluster) {
         String[] temp = cluster.split(" ");
         int[] res = new int[temp.length];
 
         for (int i = 0; i < temp.length; i++) {
             res[i] = Integer.parseInt(temp[i]);
         }
 
         return res;
     }
 
     public double getArithmetic(int[] arr2, int[] arr1) {
         double res = 0;
 
         for (int i = 0; i < arr1.length; i++) {
             for (int j = 0; j < arr2.length; j++) {
                 if (arr2[j] > matrix.length + 1) {
                     res += input[arr2[j] - 1][arr1[i]];
                 } else if (input[arr1[i] - 1][arr2[j]] != 0) {
                     res += input[arr1[i] - 1][arr2[j]];
                 } else {
                     res += input[arr2[j] - 1][arr1[i]];
                 }
             }
         }
 
         //if(arr1.length == 1 || arr2.length == 1)
         //  res /= (arr1.length + arr2.length - 1);
         //else    
         res /= (arr1.length * arr2.length);
 
         return res;
     }
 
     public void updateClusters(int ind) {
         String[] temp = new String[clusters.length - 1];
         boolean shift = false;
 
         for (int i = 0; i < temp.length; i++) {
             if (ind == minColumn) {
                 if (i == minColumn) {
                     //temp[i] = Integer.toString(minColumn) + " " + Integer.toString(minRow + 1);
                     temp[i] = clusters[i] + " " + clusters[minRow + 1];// Integer.toString(minRow + 1);
                 } else if (i != minRow + 1) {
                     if (!shift) {
                         temp[i] = clusters[i];
                     } else {
                         temp[i] = clusters[i + 1];
                     }
                 } else {
                     temp[i] = clusters[i + 1];
                     shift = true;
                     //i++;
                 }
             } else {
                 if (i == minRow + 1) {
                     //temp[i] = Integer.toString(minColumn) + " " + Integer.toString(minRow + 1);
                     temp[i] = clusters[i] + " " + clusters[minColumn];// Integer.toString(minColumn);
                 } else if (i != minColumn) {
                     if (!shift) {
                         temp[i] = clusters[i];
                     } else {
                         temp[i] = clusters[i + 1];
                     }
                 } else {
                     temp[i] = clusters[i + 1];
                     shift = true;
                     //i++;
                 }
             }
         }
 
         clusters = new String[temp.length];
         clusters = temp;
     }
 
     public void buildTree() {
         DefaultMutableTreeNode node1 = null;
         DefaultMutableTreeNode node2 = null;
         if (minColumn < minRow + 1) {
             if (clusters[minColumn].length() == 1) {
                 node1 = new DefaultMutableTreeNode(clusters[minColumn]);
             }
             if (minRow + 1 < clusters.length) {
                 if (clusters[minRow + 1].length() == 1) {
                     node2 = new DefaultMutableTreeNode(clusters[minRow + 1]);
                 }
             } else if (minRow + 1 == clusters.length) {
                 if (clusters[minRow].length() == 1) {
                     node2 = new DefaultMutableTreeNode(clusters[minRow]);
                 }
             }
             if (clusters[minColumn].length() != 1) {
                 node1 = map.get(clusters[minColumn]);
             }
             if (minRow + 1 < clusters.length) {
                 if (clusters[minRow + 1].length() != 1) {
                     node2 = map.get(clusters[minRow + 1]);
                 }
             } else if (minRow + 1 == clusters.length) {
                 if (clusters[minRow].length() != 1) {
                     node2 = map.get(clusters[minRow]);
                 }
             }
         } else {
             if (minRow + 1 < clusters.length) {
                 if (clusters[minRow + 1].length() == 1) {
                     node2 = new DefaultMutableTreeNode(clusters[minRow + 1]);
                 }
             } else if (minRow + 1 == clusters.length) {
                 if (clusters[minRow].length() == 1) {
                     node2 = new DefaultMutableTreeNode(clusters[minRow]);
                 }
             }
             if (minRow + 1 < clusters.length) {
                 if (clusters[minRow + 1].length() != 1) {
                     node2 = map.get(clusters[minRow + 1]);
                 }
             } else if (minRow + 1 == clusters.length) {
                 if (clusters[minRow].length() != 1) {
                     node2 = map.get(clusters[minRow]);
                 }
             }
             if (clusters[minColumn].length() == 1) {
                 node1 = new DefaultMutableTreeNode(clusters[minColumn]);
             }
 
             if (clusters[minColumn].length() != 1) {
                 node1 = map.get(clusters[minColumn]);
             }
         }
 
         if (minRow + 1 == clusters.length) {
             map.remove(clusters[minRow]);
         } else {
             map.remove(clusters[minRow + 1]);
         }
 
         map.remove(clusters[minColumn]);
 
         DefaultMutableTreeNode parent = null;
 //        if(node1 != null)
 //            parent.add(node1);
 //        if(node2 != null)
 //            parent.add(node2);
 
         if (minRow + 1 < clusters.length) {
             if (minColumn < minRow + 1) {
                 parent = subIf(node1, parent, node2, minColumn, minRow + 1);
             } else {
                 parent = subIf(node1, parent, node2, minRow + 1, minColumn);
             }
 
             if (clusters.length == 2) {
                 tree = new JTree(parent);
             }
         } else if (minRow + 1 == clusters.length) {
             if (minColumn < minRow + 1) {
                 parent = subIf(node1, parent, node2, minColumn, minRow);
             } else {
                 parent = subIf(node1, parent, node2, minRow, minColumn);
             }
 
             if (clusters.length == 2) {
                 tree = new JTree(parent);
             }
         }
     }
 
     private DefaultMutableTreeNode subIf(DefaultMutableTreeNode node1, DefaultMutableTreeNode parent, DefaultMutableTreeNode node2, int idx1, int idx2) {
         parent = new DefaultMutableTreeNode(clusters[idx1] + " " + clusters[idx2]);
         if (node1 != null) {
             parent.add(node1);
         }
         if (node2 != null) {
             parent.add(node2);
         }
         map.put(clusters[idx1] + " " + clusters[idx2], parent);
         return parent;
     }
 
     public void updateMatrix() {
         findMin(matrix);
         double[][] res = new double[matrix.length - 1][matrix.length - 1];
         int ii = 0, jj = 0;
         boolean breaked = false;
 
         buildTree();
 
         //jj = (minRow <= minColumn) ? minRow : minColumn;
         int ind = (minRow <= minColumn) ? minRow : minColumn;
         updateClusters(ind);
 
         for (int i = 0; i < matrix.length; i++) {
             for (int j = 0; j < matrix.length; j++) {
                 if (i < ind && j < ind) {
                     int[] cl1 = getNumbers(clusters[j]);
                     int[] cl2 = getNumbers(clusters[i + 1]);
 
                     res[ii][jj] = getArithmetic(cl1, cl2);
                     jj++;
                     j++;
                 }
                 if (breaked) {
                     if (j > i - 1) {
                         jj = 0;
                         ii++;
                         break;
                     }
 
                 }
                 //if(minColumn == 0){
                 if (j > i) {
                     jj = 0;
                     ii++;
                     break;
                 } else {
                     if (i == minRow) {
                         if (minRow != 0) {
                             breaked = true;
                         }
                         break;
                     }
                     if (j == minRow + 1) {
                         j++;
                         if (j >= matrix.length) {
                             break;
                         }
                         //break;
                     }
                     if (i != minRow && j != minColumn) {
                         res[ii][jj] = matrix[i][j];
                         jj++;
                     } else if (j == minColumn) {
                         int[] cl1 = getNumbers(clusters[j]);
                         int[] cl2;
                         if (i != clusters.length - 1) {
                             //if(i + 1 == clusters.length - 1){
                             cl2 = getNumbers(clusters[ii + 1]);
                             //}
                             //else
                             //  cl2 = getNumbers(clusters[i + 1]);
                         } else {
                             cl2 = getNumbers(clusters[i]);
                         }
                         res[ii][jj] = getArithmetic(cl1, cl2);
                         jj++;
                     }
                     if (jj == matrix.length - 1) {
                         jj = 0;
                         ii++;
                     }
                 }
                 // }
             }
         }
 
         matrix = new double[res.length][res.length];
         matrix = res;
 
         if (matrix.length == 1) {
             buildTree();
         }
     }
 
     public double[][] getMatrix() {
         return matrix;
     }
 
     public String[] getClusters() {
         return clusters;
     }
 
     public JTree getTree() {
         return tree;
     }
 
     public double getMinElement() {
         return minElement;
     }
 
     public int GetMinRow() {
         return minRow;
     }
 
     public int getMinColumn() {
         return minColumn;
     }
 
     private ClusterTree convert(JTree jTree) {
         ClusterTree ctree = new ClusterTree();
         throw new NotImplementedException("Implement conversion!!!!");
         //return tree;
     }
 
     private void calculate(double[][] matrix) {
         init(matrix);
 
         while (getMatrix().length > 1) {
             updateMatrix();
         }
     }
 
     @Override
     public ClusterTree clusterData(double[][] matrix) {
         calculate(matrix);
 
         return convert(tree);
     }
 
     @Override
     public JTree getResultTree(double[][] matrix) {
         calculate(matrix);
         return tree;
     }
 }
