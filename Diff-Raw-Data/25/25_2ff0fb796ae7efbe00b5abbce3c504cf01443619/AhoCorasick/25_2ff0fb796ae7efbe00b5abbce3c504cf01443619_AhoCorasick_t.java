 package awesome.tony.fastfind;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 /**
  * Implementation of the Aho Corasick string search algorithm.  Given dictionary
  * terms are evaluated against a given data stream to find matches.
  * @author 
  */
 public class AhoCorasick {
 
     static void addResult(final Map<SearchTerm, List<Long>> results, final long index, final SearchTerm term) {
         final List<Long> indexes = (results.containsKey(term)) ? results.get(term) : new ArrayList<Long>(5);
         indexes.add(index);
         results.put(term, indexes);
     }
 
     private final Node root;
 
     /**
      * Constructs an instance of the Aho Corasick search algorithm initialized
      * with the given dictionary.
      * @param dictionary 
      * @throws IllegalArgumentException if given dictionary is null or empty
      */
     public AhoCorasick(final Collection<SearchTerm> dictionary) {
         if (dictionary.isEmpty()) {
             throw new IllegalArgumentException();
         }
         root = new Node();
         for (final SearchTerm term : dictionary) {
             addMatchRecursive(term, 0, root);
         }
         initialize();
     }
 
     private void addMatchRecursive(final SearchTerm term, final int offset, final Node current) {
         final int index = term.get(offset);
         final boolean atEnd = (offset == (term.size() - 1));
         if (current.getNeighbor(index) == null) {
             if (atEnd) {
                 current.setNeighbor(new Node(term), index);
                 return;
             }
             current.setNeighbor(new Node(), index);
         } else if (atEnd) {
             current.getNeighbor(index).setSearchTerm(term);
             return;
         }
         addMatchRecursive(term, offset + 1, current.getNeighbor(index));
     }
 
     private void initialize() {
         // perform bfs to build failure links
         final Queue<Node> queue = new LinkedList<>();
         queue.add(root);
         root.setFailureNode(null);
         while (!queue.isEmpty()) {
             final Node current = queue.poll();
             for (int i = 0; i < 256; i++) {
                 final Node next = current.getNeighbor(i);
                 if (next != null) {
                     // traverse failure to get state
                     Node fail = current.getFailureNode();
                     while ((fail != null) && fail.getNeighbor(i) == null) {
                         fail = fail.getFailureNode();
                     }
                     if (fail != null) {
                         next.setFailureNode(fail.getNeighbor(i));
                     } else {
                         next.setFailureNode(root);
                     }
                     queue.add(next);
                 }
             }
         }
 
     }
 
     /**
      * Searches through the given stream for matches against this objects already loaded
      * dictionary.
      * @param stream source stream to search for patterns in
      * @param findAll determines if all possible patterns will be found (true) or if it will stop on first match (false)
      * @return Map of search terms with each containing a list of long indexes which indicate which byte offset a search term was found at
      * in the source stream
      * @throws IOException 
      */
     public Map<SearchTerm, List<Long>> evaluate(final InputStream stream, final boolean findAll) throws IOException {
         final Map<SearchTerm, List<Long>> resultMap = new HashMap<>(findAll ? 5 : 1);
         long bytesRead = 0L;        
         Node current = root;
         int currentChar;
         while ((currentChar = stream.read()) >= 0) {
             bytesRead++;
             Node next = current.getNeighbor(currentChar);
            // Condition 1 - continue down 
             if (next == null) {
                 next = current.getFailureNode();
                // look for a fail that has a path down
                 while ((next != null) && next.getNeighbor(currentChar) == null) {
                     next = next.getFailureNode();
                 }
                
                 if (next != null) {
                	// Condition 2 found a path down in fail path
                     next = next.getNeighbor(currentChar);
                 } else {
                	// Condition 3 back to root
                     next = root;
                 }
             }
            // Note - next is not going to be null, conditions 1,2,3 above all set next to 
            // 	non null. If you change above code, add the next few lines 
            //if (next == null) {
            //   throw new IllegalStateException("this doesn't make sense to me yet...based on above assignment it seems it could be null yet null seems not legit");
            //}
             if (next.isMatchingNode()) {
                 addResult(resultMap, bytesRead, next.getSearchTerm());
             }
             for (Node failNode = next.getFailureNode(); failNode != null; failNode = failNode.getFailureNode()) {
                 if (failNode.isMatchingNode()) {
                     addResult(resultMap, bytesRead, failNode.getSearchTerm());
                 }
             }
             current = next;
             if(!findAll && resultMap.size() > 0){
                 break;//we've got enough
             }
         }
         return resultMap;
     }
 }
