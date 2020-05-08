 package org.jetbrains.plugins.xml.searchandreplace.search;
 
 import com.intellij.openapi.util.Condition;
 import com.intellij.psi.PsiElement;
 import com.intellij.util.containers.FilteringIterator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class Pattern implements Cloneable {
 
   private HashSet<Node> roots;
   private HashMap<Node, Integer> parentsNum;
 
   private Node theOne;
 
   private HashMap<Node, PsiElement> matchedNodes = new HashMap<Node, PsiElement>();
 
   private HashSet<Node> unmatchedNodes;
 
   public Pattern(HashSet<Node> nodes) {
     unmatchedNodes = nodes;
     endBuild();
   }
 
   public Node getTheOne() {
     return theOne;
   }
 
   public Set<Node> getUnmatchedNodes() {
     return unmatchedNodes;
   }
 
   private Iterable<Node> unmatchedChildrenOfNode(final Node n) {
     return new Iterable<Node>() {
       public Iterator<Node> iterator() {
         return new FilteringIterator<Node, Node>(n.getChildren().iterator(), new Condition<Node>() {
           public boolean value(Node node) {
             return unmatchedNodes.contains(node);
           }
         });
       }
     };
   }
 
   private int getParentsNum(Node n) {
     Integer num = parentsNum.get(n);
     return num == null ? 0 : num;
   }
 
   private void setParentsNum(Node n, int num) {
     parentsNum.put(n, num);
   }
 
   public void validateNodes() {
     roots = new HashSet<Node>(unmatchedNodes);
     parentsNum = new HashMap<Node, Integer>();
     for (Node n : unmatchedNodes) {
 
       matchedNodes.remove(n);
       if (n.isTheOne()) {
         theOne = n;
       }
       for (Node c : unmatchedChildrenOfNode(n)) {
         roots.remove(c);
         retain(c);
       }
     }
   }
 
   @Override
   public String toString() {
     return "\nPattern{" +
                    "\nroots=" + roots +
                    ",\n parentsNum=" + parentsNum +
                    ",\n theOne=" + theOne +
                    ",\n unmatchedNodes=" + unmatchedNodes +
                    "}\n";
   }
 
   public final Pattern clone() {
     Pattern result = null;
     try {
       result = (Pattern) super.clone();
       result.theOne = theOne;
       result.roots = new HashSet<Node>(roots);
       result.parentsNum = new HashMap<Node, Integer>(parentsNum);
       result.unmatchedNodes = new HashSet<Node>(unmatchedNodes);
       result.matchedNodes = new HashMap<Node, PsiElement>(matchedNodes);
     } catch (CloneNotSupportedException e) {
       e.printStackTrace();
     }
     return result;
   }
 
   private void removeRoot(Node root) {
     roots.remove(root);
     for (Node child : unmatchedChildrenOfNode(root)) {
       release(child);
       if (getParentsNum(child) == 0) {
         roots.add(child);
       }
     }
     unmatchedNodes.remove(root);
   }
 
   private void release(Node child) {
     int parentsNum = getParentsNum(child);
     setParentsNum(child, parentsNum - 1);
     if (parentsNum == 1) {
       roots.add(child);
     }
   }
 
   private void retain(Node child) {
     setParentsNum(child, getParentsNum(child) + 1);
     if (roots.contains(child)) {
       roots.remove(child);
     }
   }
 
   private void repair(Node n, boolean needValidate) {
     unmatchedNodes.add(n);
     for (Node c : n.getChildren()) {
       repair(c, false);
     }
     if (needValidate) {
       validateNodes();
     }
   }
 
   private Pattern repaired() {
     Pattern result = clone();
     result.repair(theOne, true);
     return result;
   }
 
   private static boolean isEmpty(Iterable i) {
     for (Object o : i) {
       return false;
     }
     return true;
   }
 
   private void reduce(PsiElement tag, Node node) {
     if (roots.contains(node)) {
       if (node.isNot()) {
         if (node.apply(tag)) {
           if (!node.getChildren().isEmpty()) {
             removeRoot(node);
             for (Node child : unmatchedChildrenOfNode(node)) {
               reduce(tag, child);
             }
             for (Node child : unmatchedChildrenOfNode(node)) {
               retain(child);
             }
             if (!isEmpty(unmatchedChildrenOfNode(node))) {
               roots.add(node);
               unmatchedNodes.add(node);
             }
           }
         } else {
           removeRoot(node);
           Node neverSuccess = new Node.NeverSuccessfull(node);
           unmatchedNodes.add(neverSuccess);
           roots.add(neverSuccess);
         }
       } else {
         if (node.apply(tag)) {
           matchedNodes.put(node, tag);
           removeRoot(node);
         }
       }
     }
   }
 
   private Pattern reduced(PsiElement psiElement, boolean forceClone) {
     Pattern reduced = this;
     if (forceClone || canBeReducedBy(psiElement)) {
       reduced = this.clone();
       for (Node root : roots) {
         reduced.reduce(psiElement, root);
       }
     }
     return reduced;
   }
 
   private boolean canBeReducedBy(PsiElement psiElement) {
     for (Node root : roots) {
       if (root.apply(psiElement)) {
         if (root.isNot()) {
           for (Node child : unmatchedChildrenOfNode(root)) {
             if (child.apply(psiElement)) {
               return true;
             }
           }
         } else {
           return true;
         }
       } else {
         if (root.isNot()) {
           return true;
         }
       }
     }
     return false;
   }
 
   public HashMap<Node, PsiElement> getMatchedNodes() {
     return matchedNodes;
   }
 
   private boolean substract(Pattern other) {
     boolean changed, onceChanged = false;
     do {
       changed = false;
       for (Node n : roots) {
         PsiElement matchByOther = other.matchedNodes.get(n);
         if (matchByOther != null) {
           if (matchedInTheSameWay(other, n)) {
             removeRoot(n);
             matchedNodes.put(n, matchByOther);
             onceChanged = changed = true;
             break;
           }
         } else {
           if (n.isNot()) {
             boolean foundNeverSuccess = false;
             for (Node othersNode : other.unmatchedNodes) {
               if (othersNode instanceof Node.NeverSuccessfull) {
                 if (((Node.NeverSuccessfull)othersNode).getUnderlyingNode() == n) {
                   foundNeverSuccess = true;
                 }
               }
             }
             if (foundNeverSuccess) {
               if (matchedInTheSameWay(other, n)) {
               Node.NeverSuccessfull e = new Node.NeverSuccessfull(n);
               unmatchedNodes.add(e);
               unmatchedNodes.remove(n);
               roots.add(e);
               roots.remove(n);
               onceChanged = changed = true;
               break;
               }
             }
           }
         }
       }
     } while (changed);
     return onceChanged;
   }
 
   private boolean matchedInTheSameWay(Pattern other, Node n) {
     Set<Node> nodeParents = new HashSet<Node>();
 
     n.gatherParents(nodeParents);
     boolean ok = true;
     for (Node p : nodeParents) {
       if (matchedNodes.get(p) != other.matchedNodes.get(p)) {
         ok = false;
       }
     }
     return ok;
   }
 
   private static void mergePatterns(Set<Pattern> patterns) {
     boolean done;
     do {
       done = true;
       for (Pattern p : patterns) {
         for (Pattern other : patterns) {
           if (other != p) {
             if (p.substract(other)) {
               done = false;
             }
           }
         }
       }
     } while (!done);
 
     boolean changed;
     do {
       changed = false;
       for (Pattern p : patterns) {
         for (Pattern other : patterns) {
          if (p != other && isEqual(p, other)) {
             changed = true;
             patterns.remove(other);
             break;
           }
         }
         if (changed) {
           break;
         }
       }
     } while (changed);
   }
 
  private static boolean isEqual(Pattern p, Pattern other) {
    return p.matchedNodes.equals(other.matchedNodes) && p.unmatchedNodes.equals(other.unmatchedNodes);
  }

   private static Set<Pattern> matchChildren(PsiElement element, Set<Pattern> patternSet) {
     if (element == null) return patternSet;
     Set<Pattern> afterChildrenMatching = new HashSet<Pattern>();
     for (PsiElement c : element.getChildren()) {
 
         for (Pattern p : patternSet) {
           Set<Pattern> matchChildResult = p.match(c, false);
           afterChildrenMatching.addAll(matchChildResult);
         }
     }
     if (afterChildrenMatching.isEmpty()) {
       return patternSet;
     }
     return afterChildrenMatching;
   }
 
   private boolean isEmptyOrContainsOnlyNot(Pattern p) {
     for (Node n : p.unmatchedNodes) {
       if (!n.isNot()) {
         return false;
       }
     }
     return true;
   }
 
   public Set<Pattern> match(PsiElement element, boolean root) {
     Pattern reduced = reduced(element, root);
     Set<Pattern> forFurtherMatching = new HashSet<Pattern>();
     forFurtherMatching.add(reduced);
     if (reduced.matchedNodes.containsKey(theOne) && matchedNodes.containsKey(theOne) && reduced.theOne.apply(element)) {
       Pattern repaired = reduced.repaired();
       repaired.reduce(element, repaired.getTheOne());
       forFurtherMatching.add(repaired);
     }
     Set<Pattern> afterChildrenMatching = matchChildren(element, forFurtherMatching);
     Set<Pattern> result = new HashSet<Pattern>();
     for (Pattern p : afterChildrenMatching) {
       boolean unique = true;
       for (Pattern other : result) {
         if (p != other) {
          if (isEqual(other, p)) {
             unique = false;
           }
         }
       }
       if (unique) {
         result.add(p);
       }
     }
     return result;
   }
 
   public void match(PsiElement element, TagSearchObserver observer) {
    synchronized (this) {
      Set<Pattern> match = match(element, true);
      mergePatterns(match);
      for (Pattern p : match) {
        if (isEmptyOrContainsOnlyNot(p)) {
          observer.elementFound(p, p.matchedNodes.get(p.theOne));
        }
       }
     }
   }
 
   public void endBuild() {
     for (Node n : unmatchedNodes) {
       for (Node c : n.getChildren()) {
         c.getParents().add(n);
       }
       if (n.isTheOne()) {
         theOne = n;
       }
     }
     validateNodes();
   }
 }
