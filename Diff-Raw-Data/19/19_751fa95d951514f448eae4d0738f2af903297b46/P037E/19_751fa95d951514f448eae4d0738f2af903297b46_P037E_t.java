 import java.util.*;
 
 public class P037E {
     private static class Graph {
         private final int N;
         private List<Set<Integer>> adj = new ArrayList<Set<Integer>>();
         
         public Graph(int N) {
             this.N = N;
             for (int i = 0; i < N; i++) {
                 adj.add(new HashSet<Integer>());
             }
         }
         
         public void add(int u, int v) {
             adj.get(u).add(v);
             adj.get(v).add(u);
         }
         
         public void print() {
             for (int u = 0; u < N; u++) {
                 System.out.printf("%d: ", u);
                 for (int v: adj.get(u)) {
                     System.out.printf("%d ", v);
                 }
                 System.out.printf("\n");
             }
         }
     }
     
     private int[] rank;
     private int[] parent;
     private final int N;
     private final int M;
     private final int NM;
     private int[][] board;  // 0 - black, 1 - white
     private Graph graph;
     private HashMap<Integer, Integer> b2g = new HashMap<Integer, Integer>();  // board index to graph node
     private HashMap<Integer, Integer> g2b = new HashMap<Integer, Integer>();  // graph node to board index
     
     private int getIndex(int row, int col) {
         return row * M + col;
     }
     
     private Set<Integer> neighbors(int k) {
         int row = k / M;
         int col = k % M;
         
         HashSet<Integer> result = new HashSet<Integer>();
         if (row > 0) {
             result.add(getIndex(row-1, col));
         }
         if (row < N-1) {
             result.add(getIndex(row+1, col));
         }
         if (col > 0) {
             result.add(getIndex(row, col-1));
         }
         if (col < M-1) {
             result.add(getIndex(row, col+1));
         }
         return result;
     }
     
     private void makeSet(int k) {
         assert 0 <= k && k < NM;
         
         parent[k] = k;
         rank[k]   = 0;
     }
     
     private int find(int k) {
         assert 0 <= k && k < NM;
         
         if (parent[k] != k) {
             parent[k] = find(parent[k]);
         }
         return parent[k];
     }
     
     private void link(int u, int v) {
         if (rank[u] > rank[v]) {
             parent[v] = u;
         }
         else {
             parent[u] = v;
             if (rank[u] == rank[v]) {
                 rank[v]++;
             }
         }
     }
     
     private void union(int u, int v) {
         link(find(u), find(v));
     }
     
     private int left(int k) {
         int row = k / M;
         int col = k % M;
         if (col == 0) {
             return -1;
         }
         return getIndex(row, col-1);
     }
     
     private int right(int k) {
         int row = k / M;
         int col = k % M;
         if (col == M-1) {
             return -1;
         }
         return getIndex(row, col+1);
     }
     
     private int upper(int k) {
         int row = k / M;
         int col = k % M;
         if (row == 0) {
             return -1;
         }
         return getIndex(row-1, col);
     }
     
     private int lower(int k) {
         int row = k / M;
         int col = k % M;
         if (row == N-1) {
             return -1;
         }
         return getIndex(row+1, col);
     }
     
     private int getColor(int k) {
         return board[k/M][k%M];
     }
     
     // construct side-linked sets
     private void constructSLS() {
         for (int row = 0; row < N; row++) {
             for (int col = 0; col < M; col++) {
                 makeSet(getIndex(row, col));
             }
         }
         
         for (int row = 0; row < N; row++) {
             for (int col = 0; col < M; col++) {
                 int color = board[row][col];
                 int id = getIndex(row, col);
                 int leftId  = left(id);
                 int upperId = upper(id);
                 if (leftId >= 0 && getColor(leftId) == color) {
                     union(id,leftId);
                 }
                 if (upperId >= 0 && getColor(upperId) == color) {
                     union(id, upperId);
                 }
             }
         }
     }
     
     private void constructGraph() {
         // first find how many nodes there are in the graph
         // also create map between board index and graph node
         int nodeNum = 0;
         for (int row = 0; row < N; row++) {
             for (int col = 0; col < M; col++) {
                 int k = find(getIndex(row, col));
                 if (!b2g.containsKey(k)) {
                     b2g.put(k, nodeNum);
                     g2b.put(nodeNum, k);
                     nodeNum++;
                 }
             }
         }
         
         graph = new Graph(nodeNum);
         for (int row = 0; row < N; row++) {
             for (int col = 0; col < M; col++) {
                 int id = getIndex(row, col);
                 int leftId  = left(id);
                 int upperId = upper(id);
                 int rightId = right(id);
                 int lowerId = lower(id);
                addEdge(id, leftId);
                addEdge(id, rightId);
                addEdge(id, upperId);
                addEdge(id, lowerId);
             }
         }
     }
     
    private void addEdge(int id1, int id2) {
        assert id1 >= 0;
        if (id2 < 0) return;
        
        int u = b2g.get(find(id1));
        int v = b2g.get(find(id2));
        if (u != v) graph.add(u, v);
    }
    
     public P037E() {
         Scanner sc = new Scanner(System.in);
         N = sc.nextInt();
         M = sc.nextInt();
         NM = N * M;
         rank   = new int[NM];
         parent = new int[NM];
         board = new int[N][M];
         for (int i = 0; i < N; i++) {
             String s = sc.next();
             for (int j = 0; j < M; j++) {
                 board[i][j] = (s.charAt(j) == 'W' ? 1 : 0);
             }
         }
         
         constructSLS();
         constructGraph();
     }
     
     public static void main(String[] args) {
         P037E solution = new P037E();
     }
 }
