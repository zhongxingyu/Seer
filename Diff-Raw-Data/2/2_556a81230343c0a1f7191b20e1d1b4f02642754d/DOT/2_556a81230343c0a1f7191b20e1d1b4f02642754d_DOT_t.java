 package jlogic.interpret;
 
 import java.util.Map;
 
 import jlogic.term.AnonymousVariable;
 import jlogic.term.Atom;
 import jlogic.term.Structure;
 import jlogic.term.Term;
 import jlogic.term.Variable;
 import jlogic.term.Visitor;
 
 import fj.F;
 import fj.F2;
 import fj.data.List;
 
 public final class DOT {
     private final SearchTree searchTree;
     private int idCounter;
 
     public DOT(SearchTree searchTree) {
         this.searchTree = searchTree;
     }
 
     public String generateDOT() {
         idCounter = 0;
 
         StringBuilder builder = new StringBuilder();
         builder.append("digraph {\n");
         generateNodeDOT(builder, searchTree.getRoot());
         builder.append("}");
         return builder.toString();
     }
 
     /**
      * Returns a list of variables that were newly instantiated in this node
      * when compared to its parent.
      */
     private List<Map.Entry<Variable, Term>> frameDeltaToParent(SearchNode node) {
         assert node != null;
 
         SearchNode parent = node.getParent();
         assert parent != null;
 
         Frame frame = node.getFrame();
         Frame parentFrame = parent.getFrame();
 
         List<Map.Entry<Variable, Term>> delta = List.nil();
         for (Map.Entry<Variable, Term> entry : frame.getInstantiations().entrySet()) {
             Term parentTerm = parentFrame.getInstantiation(entry.getKey());
             if (parentTerm == null || !parentTerm.equals(entry.getValue())) {
                 entry.setValue(entry.getValue().accept(new Instantiate(frame)));
                 delta = delta.cons(entry);
             }
         }
         return delta;
     }
 
     /**
      * Filters a list of variable instantiations, returning only those variables
      * which are explicitly referred to in the parent's goal list.
      */
     private List<Map.Entry<Variable, Term>> filterFrameDelta(SearchNode node, List<Map.Entry<Variable, Term>> delta) {
         assert node != null;
 
        final SearchNode parent = node.getParent();
         assert parent != null;
 
         return delta.filter(new F<Map.Entry<Variable, Term>, Boolean>() {
             @Override
             public Boolean f(final Map.Entry<Variable, Term> entry) {
                 class ContainsVariable implements Visitor<Boolean> {
                     @Override
                     public Boolean visit(AnonymousVariable anonymousVariable) {
                         return false;
                     }
 
                     @Override
                     public Boolean visit(Atom atom) {
                         return false;
                     }
 
                     @Override
                     public Boolean visit(Structure structure) {
                         Boolean result = false;
                         for (Term argument : structure.getArguments())
                             result = result || argument.accept(this);
                         return result;
                     }
 
                     @Override
                     public Boolean visit(Variable variable) {
                         return variable.equals(entry.getKey());
                     }
 
                     public Boolean visit(List<Term> terms) {
                         final Visitor<Boolean> outer = this;
                         return terms.foldLeft(new F2<Boolean, Term, Boolean>() {
                             @Override
                             public Boolean f(Boolean b, Term term) {
                                 return b || term.accept(outer);
                             }
                         }, false);
                     }
                 };
 
                 return (new ContainsVariable()).visit(parent.getGoals());
             }
         });
     }
 
     /**
      * Creates a DOT string representation of a node and its children.
      * 
      * @return An unique identifier used while generating.
      */
     private int generateNodeDOT(StringBuilder builder, SearchNode node) {
         int thisId = idCounter++;
 
         builder.append("\tN");
         builder.append(thisId);
         builder.append(" [label=\"");
 
         for (Term goal : node.getGoals()) {
             builder.append(goal);
             builder.append(",\\n");
         }
 
         if (!node.getGoals().isEmpty()) {
             // Remove last comma and newline if existing
             builder.delete(builder.length() - 3, builder.length());
         } else {
             Frame resultFrame = searchTree.createResultFrame(node.getFrame());
             for (Map.Entry<Variable, Term> entry : resultFrame.getInstantiations().entrySet()) {
                 builder.append(entry.getKey());
                 builder.append(" = ");
                 builder.append(entry.getValue());
                 builder.append("\\n");
             }
         }
 
         builder.append("\"");
 
         if (node.getGoals().isEmpty())
             builder.append(" color=green");
         else if (node.getChildren().isEmpty())
             builder.append(" color=red");
 
         builder.append("];\n");
 
         for (SearchNode child : node.getChildren()) {
             int childId = generateNodeDOT(builder, child);
             List<Map.Entry<Variable, Term>> delta = filterFrameDelta(child,
                     frameDeltaToParent(child));
 
             builder.append("\tN");
             builder.append(thisId);
             builder.append(" -> ");
             builder.append("N");
             builder.append(childId);
 
             // Label the edge to every child by all the relevant variable
             // instantiations made
             builder.append(" [label=\"");
             for (Map.Entry<Variable, Term> entry : delta) {
                 builder.append(entry.getKey());
                 builder.append(" = ");
                 builder.append(entry.getValue());
                 builder.append("\\n");
             }
             builder.append("\"];\n");
         }
 
         return thisId;
     }
 }
