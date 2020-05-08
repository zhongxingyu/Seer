 package cs224n.corefsystems;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Stack;
 
 import cs224n.coref.Mention;
 import cs224n.coref.Document;
 import cs224n.coref.Sentence;
 import cs224n.ling.Tree;
 import cs224n.util.Pair;
 import edu.stanford.nlp.util.IdentityHashSet;
 
 public class Hobbs {
   public static class Candidate {
     int sentenceIndex;
     int wordIndexStart;
     int wordIndexEnd;
     
     public Candidate(int sentenceIndex, int wordIndexStart, int wordIndexEnd) {
       this.sentenceIndex = sentenceIndex;
       this.wordIndexStart = wordIndexStart;
       this.wordIndexEnd = wordIndexEnd;
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + sentenceIndex;
       result = prime * result + wordIndexEnd;
       result = prime * result + wordIndexStart;
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj) return true;
       if (obj == null) return false;
       if (getClass() != obj.getClass()) return false;
       Candidate other = (Candidate) obj;
       if (sentenceIndex != other.sentenceIndex) return false;
       if (wordIndexEnd != other.wordIndexEnd) return false;
       if (wordIndexStart != other.wordIndexStart) return false;
       return true;
     }
 
     @Override
     public String toString() {
       return "Candidate [" + sentenceIndex + ": " + wordIndexStart
           + ", " + wordIndexEnd + "]";
     }
   }
   
   public static final int NUM_SENTENCES_LOOKBEHIND = 3;
   
   public static Map<Candidate, Integer> getHobbsCandidates(Mention pronoun) {
     Document doc = pronoun.doc;
     Sentence sentence = pronoun.sentence;
     int startSentenceIndex = doc.indexOfSentence(sentence);
     int endSentenceIndex = Math.max(0, startSentenceIndex-NUM_SENTENCES_LOOKBEHIND);
     
     Tree<String> parse = sentence.parse;
     
     Stack<Tree<String>> ancestors = new Stack<Tree<String>>();
    if (!findMentionNP(pronoun, parse, ancestors))
      return Collections.<Candidate, Integer>emptyMap();
     
     for (Tree<String> item: ancestors) {
       System.out.println(item.getLabel());
     }
     
     Set<Tree<String>> p = new IdentityHashSet<Tree<String>>();
     Tree<String> X = walkUpFindNPorS(ancestors, p);
     
     List<Tree<String>> candidates = new ArrayList<Tree<String>>();
     
     if (X != null) {
       candidates.addAll(leftRightBFS(X, p, true));
       
       while ((X = walkUpFindNPorS(ancestors, p)) != null) {
         if (X.getLabel().equals("NP")) {
           boolean passThroughDominatedNominal = false;
           
           for (Tree<String> child: candidates)
             if (p.contains(child)) {
               String label = child.getLabel();
               if (label.equals("NN") || label.equals("NNS") || label.equals("NNP") || label.equals("NNPS"))
                 passThroughDominatedNominal = true;
             }
           
           if (!passThroughDominatedNominal)
             candidates.add(X);
         }
         
         candidates.addAll(leftRightBFS(X, p, false));
         
         if (X.getLabel().equals("S"))
           candidates.addAll(stepEight(X, p));
       }
     }
     
     for (int sentenceIndex = startSentenceIndex - 1; sentenceIndex >= endSentenceIndex; sentenceIndex--)
       candidates.addAll(leftRightBFS(doc.sentences.get(sentenceIndex).parse, Collections.<Tree<String>>emptySet(), false));
     
     System.out.println();
     for (Tree<String> cand: candidates)
       System.out.println(cand.getYield());
     
     Map<Tree<String>, Pair<Integer, Integer>> treeIndexMap = new IdentityHashMap<Tree<String>, Pair<Integer, Integer>>();    
     
     for (int sentenceIndex = endSentenceIndex; sentenceIndex <= startSentenceIndex; sentenceIndex++)
       appendLocations(doc.sentences.get(sentenceIndex).parse, sentenceIndex, 0, treeIndexMap);
     
     Map<Candidate, Integer> result = new HashMap<Candidate, Integer>();
     
     for (int dist = 0; dist < candidates.size(); dist++) {
       Tree<String> candidate = candidates.get(dist);
       Pair<Integer, Integer> location = treeIndexMap.get(candidate);
       result.put(new Candidate(location.getFirst(), location.getSecond(), location.getSecond() + candidate.getYield().size()), dist);
     }
     
     System.out.println(result);
     
     return result;
   }
   
  public static boolean findMentionNP(Mention pronoun, Tree<String> parse, Stack<Tree<String>> path) {
     path.push(parse);
     findMentionNPInternal(0, pronoun.headWordIndex, parse, path); 
     
    while(!path.isEmpty() && !path.peek().getLabel().equals("NP"))
       path.pop();
    
    return !path.isEmpty();
   }
   
   public static void findMentionNPInternal(int currentIndex, int desiredIndex, Tree<String> tree, Stack<Tree<String>> path) {
     for (Tree<String> child: tree.getChildren()) {
       int yield = child.getYield().size();
       
       if (desiredIndex < currentIndex + yield) {
         path.push(child);
         findMentionNPInternal(currentIndex, desiredIndex, child, path);
         return;
       }
       
       currentIndex += yield;
     }
   }
   
   public static Tree<String> walkUpFindNPorS(Stack<Tree<String>> ancestors, Set<Tree<String>> p) {
     if (ancestors.size() < 2)
       return null;
     
     p.add(ancestors.pop());
     Tree<String> X = ancestors.pop();
     
     while (!(X.getLabel().equals("NP") || X.getLabel().equals("S"))) {
       p.add(X);
       
       if (ancestors.isEmpty())
         return null;
       X = ancestors.pop();
     }
     
     p.add(X);
     return X;
   }
   
   public static List<Tree<String>> leftRightBFS(Tree<String> start, Set<Tree<String>> blocker, boolean requireIntermediate) {
     Queue<Pair<Tree<String>, Boolean>> bfsQ = new LinkedList<Pair<Tree<String>, Boolean>>();
     List<Tree<String>> candidates = new ArrayList<Tree<String>>();
     
     for (Tree<String> child: start.getChildren()) {
       if (blocker.contains(child))
         break;
         
       bfsQ.add(new Pair<Tree<String>, Boolean>(child, !requireIntermediate));
     }
     
     while (!bfsQ.isEmpty()) {
       Pair<Tree<String>, Boolean> current = bfsQ.poll();
       
       Tree<String> tree = current.getFirst();
       boolean canAdd = current.getSecond();
       
       if (tree.getLabel().equals("S") || tree.getLabel().equals("NP")) { 
         if (canAdd) {
           if (tree.getLabel().equals("NP"))
             candidates.add(tree);
         }
         canAdd = true;
       }
       
       for (Tree<String> child: tree.getChildren()) {
         if (blocker.contains(child))
           break;
         
         bfsQ.add(new Pair<Tree<String>, Boolean>(child, canAdd));
       }
     }
     
     return candidates;
   }
   
   public static List<Tree<String>> stepEight(Tree<String> start, Set<Tree<String>> blocker) {
     Queue<Tree<String>> bfsQ = new LinkedList<Tree<String>>();
     List<Tree<String>> candidates = new ArrayList<Tree<String>>();
     
     boolean onRight = false;
     for (Tree<String> child: start.getChildren()) {
       if (blocker.contains(child)) {
         onRight = true;
         continue;
       }
         
       if (onRight)
         bfsQ.add(child);
     }
     
     while (!bfsQ.isEmpty()) {
       Tree<String> tree = bfsQ.poll();
       
       if (tree.getLabel().equals("S") || tree.getLabel().equals("NP")) {
         if (tree.getLabel().equals("NP"))
           candidates.add(tree);
         
         continue;
       }
       
       for (Tree<String> child: tree.getChildren()) {
         bfsQ.add(child);
       }
     }
     
     return candidates;
   }
   
   private static int appendLocations(Tree<String> current, int sentenceIndex, int currentWordIndex, Map<Tree<String>, Pair<Integer, Integer>> treeIndexMap) {
     if (current.isLeaf()) {
       treeIndexMap.put(current, new Pair<Integer, Integer>(sentenceIndex, currentWordIndex));
       return currentWordIndex + 1;
     }
     
     treeIndexMap.put(current, new Pair<Integer, Integer>(sentenceIndex, currentWordIndex));
     for (Tree<String> child: current.getChildren()) {
       currentWordIndex = appendLocations(child, sentenceIndex, currentWordIndex, treeIndexMap);
     }
     
     return currentWordIndex;
   }
 }
