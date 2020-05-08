 /*
  * Class containing listeners for selection modification
  */
 package nodes;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 import controlP5.ListBox;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 
 /**
  * dynamically populated menu system whose entries appear and disappear according
  * to the contents of the current selection.  there are Modifiers and ModifierSets.
  * Both are persistent.  Each of the former can have zero or one menu entries linked.
  * Each of the latter can have zero or more Modifiers within, all of which share
  * the same compatibility test (EX: one rdf:type Modifier for each such triple
  * pertaining to a selected Node, all are compatible if the selected node has *any*
  * rdf:type outgoing predicates).
  */
 public class ModifierPopulator {
     
     private Graph graph;
     private Model model;
     private Selection selection;
     
     private ArrayList<Modifier> modifiers;
     private ArrayList<ModifierSet> modifierSets;
     
     private HashMap<Integer, Modifier> runIndex;
     
     private int menuIndex;
     
     public ModifierPopulator(Graph g) {
         graph = g;
         model = graph.triples;
         selection = graph.selection;
         
         modifiers = new ArrayList<>();
         modifierSets = new ArrayList<>();
         
         runIndex = new HashMap<>();
         
         // integer address for accessing menu item methods when clicked
         // (this value is currently never reset, just incremented.  though it
         // should take a safely long time to traverse all positive integers)
         menuIndex = 0;
         
         // construct all modifiers and modifier sets, adding them to the
         // corresponding lists
         modifiers.add(new SelectAll());
         modifiers.add(new SelectNodes());
         modifiers.add(new SelectEdges());
         modifiers.add(new SelectNeighbors());
         modifiers.add(new InvertSelection());
         modifiers.add(new SelectLastAdded());
         modifiers.add(new SelectCorrespondingEdges());
         
         modifierSets.add(new SelectCorrespondingNode());
     }
     
     // iterates through all persistent Modifiers and ModifierSets
     public void populate(ListBox menu, Selection selection) {
         // add newly compatible modifiers, remove newly incompatible ones
         for (Modifier mod : modifiers) {
             // add the modifier to the menu and register it with runIndex
             // if it isn't already and if it's compatible with the current selection
             if (mod.isCompatible() && !runIndex.containsValue(mod)) {
                 addEntry(menu, mod);
             // if the modifier is not compatible with the current selection but
             // it is still registered/in the menu, then remove it from the menu/index
             } else if (!mod.isCompatible() && runIndex.containsValue(mod)) {
                 removeEntry(menu, mod);
             }
         }
         
         for (ModifierSet mSet : modifierSets) {
             // remove old modifiers
             for (Modifier mod : mSet.getModifiers()) {
                 if (runIndex.containsValue(mod)) removeEntry(menu, mod);
             }
             if (mSet.isCompatible()) {
                 
                 mSet.constructModifiers();
                 for (Modifier mod : mSet.getModifiers()) {
                     addEntry(menu, mod);
                 }
             }
         }
     }
     
     /**
      * runs the Modifier selection modification at the index passed.
      * @param modIdx menu index to run (corresponds to runIndex key, internally)
      */
     public void run(int modIdx) {
         try {
             runIndex.get(modIdx).modify();
         } catch (Exception e) {
             System.out.println("Error: Bad menu index passed");
         }
     }
     
     /*
      * adds menu entry for Modifier passed
      */
     private void addEntry(ListBox menu, Modifier mod) {
         /*
          * TODO: check for runIndex collision (new mod for same key)
          */
         menu.addItem(mod.getTitle(), menuIndex);
         runIndex.put(menuIndex, mod);
 
         menuIndex++;
         if (menuIndex == Integer.MAX_VALUE) {
             // this *really* shouldn't happen, but its bugs will be
             // insidious if it does.  tests have verified bad behaviour
             // when menuIndex wraps around to MIN_VALUE
             System.out.println("ERROR: menuIndex has reached max int");
         }
     }
     
     /**
      * removes menu entry for Modifier passed
      */
     private void removeEntry(ListBox menu, Modifier mod) {
         /*
          * TODO: unchecked conditions:
          * - menu doesn't contain modifier passed
          * - runIndex doesn't contain corresponding entry
          */
         
         int toRemove = menu.getItem(mod.getTitle()).getValue();
 
         menu.removeItem(mod.getTitle());
         runIndex.remove(toRemove);
     }
     
     private abstract class Modifier {
         public abstract boolean isCompatible();
         public abstract String getTitle();
         public abstract void modify();
     }
     
     private abstract class ModifierSet {
         public abstract ArrayList<Modifier> getModifiers();
         public abstract boolean isCompatible();
         public abstract void constructModifiers();
     }
     
     private class SelectAll extends Modifier {
         
         @Override
         public boolean isCompatible() {
             return true;
         }
         
         @Override
         public String getTitle() {
             return "Select all";
         }
         
         @Override
         public void modify() {
             for (GraphElement e : graph) {
                 selection.add(e);
             }
         }
     }
     
     private class SelectNodes extends Modifier {
         
         @Override
         public boolean isCompatible() {
             return selection.nodeCount() > 0 && selection.edgeCount() > 0;
         }
         
         @Override
         public String getTitle() {
             return "Filter only nodes";
         }
         
         @Override
         public void modify() {
             selection.clearEdges();
         }
     }
     
     private class SelectEdges extends Modifier {
         
         @Override
         public boolean isCompatible() {
             return selection.nodeCount() > 0 && selection.edgeCount() > 0;
         }
         
         @Override
         public String getTitle() {
             return "Filter only edges";
         }
         
         @Override
         public void modify() {
             selection.clearNodes();
         }
     }
     
     private class SelectNeighbors extends Modifier {
         
         @Override
         public boolean isCompatible() {
             return selection.nodeCount() > 0 || selection.edgeCount() > 0;
         }
         
         @Override
         public String getTitle() {
             return "Add neighborhood to selection";
         }
         
         @Override
         public void modify() {
             selection.clearBuffer();
             for (Edge e : selection.getEdges()) {
                 selection.addToBuffer(e.dst);
                 selection.addToBuffer(e.src);
             }
             for (Node n : selection.getNodes()) {
                 for (Node nbr : graph.getNbrs(n)) {
                     selection.addToBuffer(nbr);
                     selection.addToBuffer(graph.getEdge(n, nbr));
                 }
             }
             selection.commitBuffer();
         }
     }
     
     private class SelectLastAdded extends Modifier {
         
         @Override
         public boolean isCompatible() {
             return graph.lastAdded != null && !graph.lastAdded.isEmpty();
         }
         
         @Override
         public String getTitle() {
             return "Select most recently added subgraph";
         }
         
         @Override
         public void modify() {
             selection.clear();
             StmtIterator it = graph.lastAdded.listStatements();
             while (it.hasNext()) {
                 Statement s = it.next();
                 
                 Node src = graph.getNode(s.getSubject().toString());
                 Node dst = graph.getNode(s.getObject().toString());
                 
                selection.add(src);
                selection.add(dst);
                selection.add(graph.getEdge(src, dst));
             }
         }
     }
     
     private class InvertSelection extends Modifier {
         
         @Override
         public boolean isCompatible() {
             return selection.nodeCount() > 0 || selection.edgeCount() > 0;
         }
         
         @Override
         public String getTitle() {
             return "Invert selection";
         }
         
         @Override
         public void modify() {
             selection.clearBuffer();
             for (GraphElement e : graph) {
                 if (!selection.contains(e)) selection.addToBuffer(e);
             }
             selection.clear();
             selection.commitBuffer();
         }
     }
     
     private class SelectCorrespondingEdges extends Modifier {
         String uri;
         
         @Override
         public boolean isCompatible() {
             if (selection.nodeCount() == 1 && selection.edgeCount() == 0) {
                 try {
                     uri = selection.getNodes().iterator().next().getName();
                 } catch (NoSuchElementException e) {
                     //user deselected node before this line.  no problem - this
                     //is obviously not compatible. return false
                     return false;
                 }
                 // determine if model contains statements with node's name as property
                 return model.contains(null, model.createProperty(uri));
             }
             return false;
         }
         
         @Override
         public String getTitle() {
             return "Select edges with to property " + graph.prefixed(uri);
         }
         
         @Override
         public void modify() {
             // select all edges with statements with node's name as property
             //////////////////
             selection.clear();
             // query model
             StmtIterator it = model.listStatements(null, model.createProperty(uri), (RDFNode) null);
             
             // for each statement, add edge between subject and object to selection
             while (it.hasNext()) {
                 Statement s = it.next();
                 String sub = s.getSubject().toString();
                 String obj = s.getObject().toString();
                 
                 selection.add(graph.getEdge(sub, obj));
             }
         }
     }
     
     private class SelectCorrespondingNode extends ModifierSet {
         private ArrayList<Modifier> modifiers;
         Edge edge;
         
         public SelectCorrespondingNode() {
             modifiers = new ArrayList<>();
         }
         
         @Override
         public ArrayList<Modifier> getModifiers() {
             return modifiers;
         }
         
         @Override
         public boolean isCompatible() {
             if (selection.edgeCount() == 1 && selection.nodeCount() == 0) {
                 try {
                     edge = selection.getEdges().iterator().next();
                 } catch (NoSuchElementException e) {
                     //user deselected edge before this line.  no problem - this
                     //is obviously not compatible. return false
                     return false;
                 }
                 for (Statement s : edge.triples) {
                     if (graph.cp5.get(s.getPredicate().getURI()) != null) {
                         return true;
                     }
                 }
             }
             return false;
         }
         
         @Override
         public void constructModifiers() {
             modifiers.clear();
             
             for (Statement s : edge.triples) {
                 if (graph.cp5.get(s.getPredicate().getURI()) != null) {
                     modifiers.add(new SelectPredicateNode(s.getPredicate().getURI()));
                 }
             }
         }
         
         private class SelectPredicateNode extends Modifier {
             String pred;
             
             public SelectPredicateNode(String predicate) {
                 pred = predicate;
             }
             
             @Override
             public boolean isCompatible() { return true; }
             
             @Override
             public String getTitle() {
                 return "Select node corresponding to " + graph.prefixed(pred);
             }
             
             @Override
             public void modify() {
                 selection.clear();
                 selection.add((Node) graph.cp5.get(pred));
             }
         }
     }
     
     
     
     /*TODO: modifier set for nodes of same type (multiple types per entity)
     private class SelectSameType extends Modifier {
         ArrayList<RDFNode> types;
         
         Query qry;
         QueryExecution qe;
         ResultSet rs;
         
         public SelectSameType() {
             types = new ArrayList<>();
         }
         
         @Override
         public boolean checkCompatibleAndConstruct() {
             boolean singletNode =  selection.nodeCount() == 1 && selection.edgeCount() == 0;
             boolean isCompatible = false;
             
             if (singletNode) {
                 String sparql = "select ?o where { <" + 
                         selection.iterator().next().getName() + "> <" +
                         RDF.type.getURI() + "> ?o }";
                 
                 qry = QueryFactory.create(sparql);
                 qe = QueryExecutionFactory.create(qry, model);
                 rs = qe.execSelect();
                 
                 while (rs.hasNext()) {
                     types.add(rs.nextSolution().get("o"));
                     isCompatible = true;
                 }
             }
         }
         
         @Override
         public String getTitle() {
             return "Select nodes of type";
         }
     }
         * */
 }
