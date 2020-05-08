 public class WordNet {
 
     private ST<String, Integer> nounTable;  // keep track of noun, lookup from String
     private ST<Integer, String> synset;     // keep track of synset from id
 
     private Digraph graph;
     private SAP sap;
 
 
     // constructor takes the name of the two input files
     public WordNet(String synsets, String hypernyms) {
         this.nounTable = new ST<String, Integer>();
         this.synset = new ST<Integer, String>();
         In in = new In(synsets);
 
         int size = 0;
 
         while (in.hasNextLine()) {
             size++;
             String currentLine = in.readLine();
 
             String[] allArgs = currentLine.split(",");
             Integer id = Integer.parseInt(allArgs[0]);
 
             synset.put(id, allArgs[1]);
             String[] nouns = allArgs[1].split(" ");
             for (String noun : nouns) {
                 nounTable.put(noun, id);
             }
         }
 
         this.graph = new Digraph(size);
 
         in = new In(hypernyms);
 
         while (in.hasNextLine()) {
             String currentLine = in.readLine();
             String[] numbers = currentLine.split(",");
 
            Integer idV = Integer.parseInt(numbers[0]);
            for (int i = 0; i < numbers.length; i++) {
                Integer n = Integer.parseInt(numbers[i]);
                if (n < 0 || n >= size)
                    throw new IndexOutOfBoundsException();
 
                if (i > 0) {
                    graph.addEdge(idV, n);
                }
            }
         }
 
         // check the graph is valid rooted DAG or not
         if (!isRootedDAG(graph)) {
             throw new IllegalArgumentException();
         }
 
         sap = new SAP(graph);
     }
 
     // returns all WordNet nouns
     public Iterable<String> nouns() {
         return this.nounTable;
     }
 
     // is the word a WordNet noun?
     public boolean isNoun(String word) {
         return this.nounTable.contains(word);
     }
 
     // distance between nounA and nounB (defined below)
     public int distance(String nounA, String nounB) {
 
         if (!this.isNoun(nounA) || !this.isNoun(nounB)) {
             throw new IllegalArgumentException();
         }
 
         Integer idForA = nounTable.get(nounA);
         Integer idForB = nounTable.get(nounB);
         return sap.length(idForA, idForB);
     }
 
     // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
     // in a shortest ancestral path (defined below)
     public String sap(String nounA, String nounB) {
         if (!this.isNoun(nounA) || !this.isNoun(nounB)) {
             throw new IllegalArgumentException();
         }
 
         Integer idForA = nounTable.get(nounA);
         Integer idForB = nounTable.get(nounB);
        int sapId = sap.ancestor(idForA, idForB);
         return synset.get(sapId);
     }
 
     private boolean isRootedDAG(Digraph graph) {
 
         DirectedCycle cycleFinder = new DirectedCycle(graph);
         if (cycleFinder.hasCycle()) {
             return false;
         }
 
         int numberOfZeroOut = 0;
         for (int i = 0; i < graph.V(); i++) {
             int count = 0;
             for (int adjNode : graph.adj(i)) {
                 count++;
             }
 
             if (count == 0) {
                 numberOfZeroOut++;
             }
         }
 
         if (numberOfZeroOut != 1) {
             return false;
         }
 
         return true;
     }
 
 
     // for unit testing of this class
     public static void main(String[] args) {
 
         WordNet wordNet = new WordNet(args[0], args[1]);
 
         StdOut.println(wordNet.isNoun("a"));
        StdOut.println(wordNet.sap("a", "b"));
        StdOut.println();
        StdOut.println(wordNet.sap("b", "f"));
        StdOut.println(wordNet.distance("b", "f"));

        StdOut.println();
        StdOut.println(wordNet.sap("c", "f"));
        StdOut.println(wordNet.distance("c", "f"));
 
     }
 
 
 }
