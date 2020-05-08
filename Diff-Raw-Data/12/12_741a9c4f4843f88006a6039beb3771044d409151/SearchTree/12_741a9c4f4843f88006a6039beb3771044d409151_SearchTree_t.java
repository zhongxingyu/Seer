 package jlogic.interpret;
 
 import java.util.Map;
 
 import jlogic.Knowledge;
 import jlogic.Predicate;
 import jlogic.Rule;
 import jlogic.term.Structure;
 import jlogic.term.Term;
 import jlogic.term.Variable;
 
 import fj.F2;
 import fj.data.List;
 
 /**
  * Interprets queries on a knowledge base using a tree. Each node in the tree
  * contains a list of goals which must be fulfilled.
  * 
  * This implementation is quite inefficient as it copies terms every time their
  * variables are instantiated.
  */
 public final class SearchTree {
     private final Knowledge knowledge;
     private final InternalVariableFactory internalVariableFactory;
     private final Node root;
     private final Frame queryFrame;
 
     private Node current;
     private Frame result;
 
     public SearchTree(Knowledge knowledge, Structure query) {
         this.knowledge = knowledge;
         internalVariableFactory = new InternalVariableFactory();
 
         queryFrame = new Frame();
         FreeVariablesInternalizer internalizer =
                 new FreeVariablesInternalizer(internalVariableFactory, queryFrame);
         query = (Structure) internalizer.visit(query);
 
         root = current = new Node(null, new Frame(), List.single((Term) query));
     }
 
     public Frame searchOne() {
         result = null;
         while (current != null && result == null)
             current.searchOne();
 
         if (result != null) {
             Frame frame = new Frame();
             Instantiator instantiator = new Instantiator(result);
             for (Map.Entry<Variable, Term> entry : queryFrame.getInstantiations().entrySet()) {
                 frame.instantiate(entry.getKey(), result.getInstantiation((Variable) entry.getValue()).accept(instantiator));
             }
             return frame;
         } else
             return null;
     }
 
     // Used to generate node names in Node.toDOT
     private int idCounter = 0;
 
     public String toDOT() {
         idCounter = 0;
 
         StringBuilder builder = new StringBuilder();
         builder.append("digraph {\n");
         root.toDOT(builder);
         builder.append("}");
         return builder.toString();
     }
 
     // TODO: Hack
     private List<Term> getTerms(Rule rule) {
         List<Term> result = List.nil();
 
         if (!rule.isFact()) {
             for (Term term : rule.getBody()) {
                 result = result.append(List.single(term));
             }
         }
 
         return result;
     }
 
     private Predicate getPredicate(Term term) {
         if (term instanceof Structure) {
             Structure structure = (Structure) term;
             // System.out.println("Looking up " + structure.getFullName());
             return knowledge.getPredicate(structure.getFullName());
         }
         assert false; // TODO
         throw new AssertionError(term.toString());
     }
 
     private String clausesToString(List<Term> clauses) {
         return clauses.foldLeft(new F2<String, Term, String>() {
             @Override
             public String f(String accum, Term term) {
                 return accum + term.toString();
             }
         }, "");
     }
 
     private final class Node {
         private final Node parent;
 
         private final Frame frame;
 
         // List of goals that must be fulfilled
         private final List<Term> goals;
 
         // Null if goals is empty, head of goals otherwise
         private final Term goal;
 
         // Null if goals is empty or there is no predicate of the name that
         // goal requires
         private final Predicate goalPredicate;
         private final int numClauses;
         private int currentClause = 0;
 
         // The children list is only used to create pretty graphs using toDOT.
         // Since the actual evaluation is depth-first, control is always
         // immediately given to newly created children. Hence, we don't need a
         // list of children there.
         private List<Node> children = List.nil();
 
         public Node(Node parent, Frame frame, List<Term> goals) {
             this.parent = parent;
             this.goals = goals;
             this.frame = frame;
 
             this.goal = goals.isEmpty() ? null : goals.head();
             this.goalPredicate = goal != null ? getPredicate(this.goal) : null;
             this.numClauses = this.goalPredicate != null ? this.goalPredicate.getClauses().length : 0;
 
             // System.out.println("New Node with clauses " +
             // clausesToString(goals) + " and frame " + frame);
         }
 
         public void searchOne() {
             if (goals.isEmpty()) {
                 // System.out.println("Reached empty node");
 
                 // An empty goal list means that we have found a valid result.
                 // Hand control to our parent and return our frame of variable
                 // instantiations.
                 current = parent;
                 result = frame;
                 return;
             }
 
             if (currentClause == numClauses || goalPredicate == null) {
                 // All clauses in our predicate were tried already, or our
                 // predicate does not exist: backtrack to parent.
                 current = parent;
                 return;
             }
 
             Frame matchFrame;
             do {
                 Rule clause = this.goalPredicate.getClauses()[currentClause];
                 ++currentClause;
 
                 // Replace all free variables in the clause's arguments by
                 // internal variables before matching
                 Frame augmentedFrame = new Frame(frame);
                 FreeVariablesInternalizer internalizer =
                         new FreeVariablesInternalizer(internalVariableFactory,
                                 augmentedFrame);
                 Instantiator instantiator = new Instantiator(augmentedFrame);
                 List<Term> clauseBody = getTerms(clause);
                 // System.out.println("Before: " + clause.getHead() + " :- " +
                 // clausesToString(clauseBody));
                 Term clauseHead = clause.getHead().accept(internalizer);
                 clauseBody = instantiator.visit(clauseBody);
                 // System.out.println("After: " + clauseHead + " :- " +
                 // clausesToString(clauseBody));
 
                 // System.out.println("Matching: " + goal + " vs. " + clauseHead
                 // + " with " + frame);
                 matchFrame = Matcher.match(frame, goal, clauseHead);
 
                 // Create a new child with the first clause in our predicate
                 // that matches
                 if (matchFrame != null) {
                     matchFrame = new Frame(matchFrame);
                     // System.out.println("Matched in " + matchFrame);
 
                     // Instantiate the clause's body with known variables,
                     // then replace all remaining free variables by internal
                     // variables
                     instantiator = new Instantiator(matchFrame);
                     internalizer = new FreeVariablesInternalizer(internalVariableFactory, new Frame(matchFrame));
 
                     clauseBody = internalizer.visit(clauseBody);
 
                    List<Term> childGoals = clauseBody.append(goals.tail());
                     childGoals = instantiator.visit(childGoals);
 
                     Node childNode = new Node(this, matchFrame, childGoals);
                     children = children.cons(childNode);
 
                     // Give control to our new child
                     current = childNode;
                     return;
                 }
             } while (matchFrame == null && currentClause != numClauses);
 
             // No matching clause was found. Backtrack to our parent.
             current = parent;
         }
 
         public int toDOT(StringBuilder builder) {
             int thisId = idCounter++;
 
             builder.append("\tN");
             builder.append(thisId);
             builder.append(" [label=\"");
 
             for (Term goal : goals) {
                 builder.append(goal);
                 builder.append(",\\n");
             }
 
             if (!goals.isEmpty()) // Remove last comma and newline if existing
                 builder.delete(builder.length() - 3, builder.length());
             // else
             // builder.append(frame);
 
             builder.append("\"];\n");
 
             for (Node child : children) {
                 int childId = child.toDOT(builder);
 
                 builder.append("\tN");
                 builder.append(thisId);
                 builder.append(" -> ");
                 builder.append("N");
                 builder.append(childId);
                 builder.append(";\n");
             }
 
             return thisId;
         }
     }
 }
