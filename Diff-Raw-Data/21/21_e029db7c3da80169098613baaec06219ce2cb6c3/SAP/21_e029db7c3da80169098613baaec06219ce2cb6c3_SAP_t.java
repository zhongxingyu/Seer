 public class SAP {
 
     private Digraph graph;
 
     private boolean[] markedByV;
     private boolean[] markedByW;
 
     private int[] minStepsFromV;
     private int[] minStepsFromW;
 
     private Queue<Integer> changedForV; //caching the changed 
     private Queue<Integer> changedForW; //caching the changed 
 
     private int shortestLengthSoFar;
     private int ancestor;
 
     // constructor takes a digraph (not necessarily a DAG)
     public SAP(Digraph G) {
         this.graph = new Digraph(G);
 
         markedByV = new boolean[G.V()];
         markedByW = new boolean[G.V()];
 
         minStepsFromV = new int[G.V()];
         minStepsFromW = new int[G.V()];
 
         changedForV = new Queue<Integer>(); // might be used in DeluxeBFS
         changedForW = new Queue<Integer>(); // might be used in DeluxeBFS
 
         shortestLengthSoFar = -1;
         ancestor = -1;
     }
 
     // length of shortest ancestral path between v and w; -1 if no such path
     public int length(int v, int w) {
 
         this.calculateResults(v, w);
         return shortestLengthSoFar;
     }
 
     // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
     public int ancestor(int v, int w) {
 
         this.calculateResults(v, w);
         return ancestor;
     }
 
     // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
     public int length(Iterable<Integer> v, Iterable<Integer> w) {
 
         calculateResults(v, w);
         return shortestLengthSoFar;
     }
 
     // a common ancestor that participates in shortest ancestral path; -1 if no such path
     public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
 
         calculateResults(v, w);
         return ancestor;
     }
 
     private void cleanup() {
         //cleanup for V
         while(!changedForV.isEmpty()) {
             int index = changedForV.dequeue();
             markedByV[index] = false;
             minStepsFromV[index] = 0;
         }
 
         // cleanup for W
         while(!changedForW.isEmpty()) {
             int index = changedForW.dequeue();
             markedByW[index] = false;
             minStepsFromW[index] = 0;
         }
         shortestLengthSoFar = -1;
         ancestor = -1;
     }
 
     private void calculateResults(int v, int w) {
 
         if (v < 0 || w < 0 || v >= graph.V() || w >= graph.V())
             throw new java.lang.IndexOutOfBoundsException();
         if (v == w) {
             shortestLengthSoFar = 0;
             ancestor = v;
             return;
         }
 
         this.cleanup();
         BreadthFirstDirectedPaths bfsForV = new BreadthFirstDirectedPaths(graph, v);
         BreadthFirstDirectedPaths bfsForW = new BreadthFirstDirectedPaths(graph, w);
 
 
         for (int i = 0; i < graph.V(); i++) {
 
             if (bfsForV.hasPathTo(i) && bfsForW.hasPathTo(i)) {
                 //StdOut.println(i);
                 if (shortestLengthSoFar == -1) {
                     shortestLengthSoFar = bfsForV.distTo(i) + bfsForW.distTo(i);
                     ancestor = i;
                 } else if (bfsForV.distTo(i) + bfsForW.distTo(i) < shortestLengthSoFar) {
                     shortestLengthSoFar = bfsForV.distTo(i) + bfsForW.distTo(i);
                     ancestor = i;
                 } else
                     continue;
             }
         }
     }
 
     private void calculateResults(Iterable<Integer> v, Iterable<Integer> w) {
 
         if (v == null || w == null) throw new NullPointerException();
 
         for (int a : v) {
             if (a < 0 || a >= graph.V() )
                 throw new java.lang.IndexOutOfBoundsException();
         }
         for (int b : w) {
             if (b < 0 || b >= graph.V() )
                 throw new java.lang.IndexOutOfBoundsException();
         }
 
         this.cleanup();
         BreadthFirstDirectedPaths bfsForV = new BreadthFirstDirectedPaths(graph, v);
         BreadthFirstDirectedPaths bfsForW = new BreadthFirstDirectedPaths(graph, w);
 
         for (int i = 0; i < graph.V(); i++) {
 
             if (bfsForV.hasPathTo(i) && bfsForW.hasPathTo(i)) {
                 //StdOut.println(i);
                 if (shortestLengthSoFar == -1) {
                     shortestLengthSoFar = bfsForV.distTo(i) + bfsForW.distTo(i);
                     ancestor = i;
                 } else if (bfsForV.distTo(i) + bfsForW.distTo(i) < shortestLengthSoFar) {
                     shortestLengthSoFar = bfsForV.distTo(i) + bfsForW.distTo(i);
                     ancestor = i;
                 } else
                     continue;
             }
         }
     }
 
 
     public void test1() {
         Bag<Integer> listV = new Bag<Integer>();
         Bag<Integer> listW = new Bag<Integer>();
 
         listV.add(7); listV.add(8);listV.add(4);
         listW.add(9); listW.add(11);
 
 
         StdOut.println("length is " + this.length(listV, listW));
         StdOut.println("ancestor is " + this.ancestor(listV, listW));
         assert this.length(listV, listW) == 8;
     }
 
     public void test2()  {
         Bag<Integer> listV = new Bag<Integer>();
         Bag<Integer> listW = new Bag<Integer>();
 
         listV.add(9); listV.add(1);listV.add(2);
         listW.add(9); listW.add(5);
 
 
         StdOut.println("length is " + this.length(listV, listW));
         StdOut.println("ancestor is " + this.ancestor(listV, listW));
         assert this.length(listV, listW) == 4 : "This should be 4";
     }
 
     public void test3()  {
         Bag<Integer> listV = new Bag<Integer>();
         Bag<Integer> listW = new Bag<Integer>();
 
         listV.add(7); listV.add(1);listV.add(1);
         listW.add(1); listW.add(7);
 
 
         StdOut.println("length is " + this.length(listV, listW));
         StdOut.println("ancestor is " + this.ancestor(listV, listW));
         assert this.length(listV, listW) == 2 : "This should be 2";
     }
 
    /* This test is only worked for digraph3.txt */
    public void test4() {
        Bag<Integer> listV = new Bag<Integer>();
        Bag<Integer> listW = new Bag<Integer>();

        listV.add(7); listV.add(8);listV.add(0);
        listW.add(1); listW.add(3);


        StdOut.println("length is " + this.length(listV, listW));
        StdOut.println("ancestor is " + this.ancestor(listV, listW));
        assert this.length(listV, listW) == 2 : "This should be 2";
    }

     // for unit testing of this class (such as the one below)
     public static void main(String[] args) {
 
 
         In in = new In(args[0]);
         Digraph G = new Digraph(in);
         SAP sap = new SAP(G);
 
        sap.test1(); sap.test2();sap.test3();
        //sap.test4();
 
         while (!StdIn.isEmpty()) {
             int v = StdIn.readInt();
             int w = StdIn.readInt();
             int length   = sap.length(v, w);
             int ancestor = sap.ancestor(v, w);
             StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
         }
     }
 }
