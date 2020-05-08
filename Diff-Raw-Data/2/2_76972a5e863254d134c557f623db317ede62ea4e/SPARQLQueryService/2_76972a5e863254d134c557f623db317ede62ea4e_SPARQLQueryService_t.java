 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.ilrt.wf.facets.sparql;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.DataSource;
 import com.hp.hpl.jena.query.DatasetFactory;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.sparql.algebra.Op;
 import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
 import com.hp.hpl.jena.sparql.algebra.TransformBase;
 import com.hp.hpl.jena.sparql.algebra.Transformer;
 import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
 import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
 import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
 import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
 import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
 import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
 import com.hp.hpl.jena.sparql.algebra.op.OpNull;
 import com.hp.hpl.jena.sparql.algebra.op.OpProject;
 import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
 import com.hp.hpl.jena.sparql.core.BasicPattern;
 import com.hp.hpl.jena.sparql.core.Var;
 import com.hp.hpl.jena.sparql.expr.E_LessThan;
 import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
 import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
 import com.hp.hpl.jena.sparql.expr.E_Regex;
 import com.hp.hpl.jena.sparql.expr.E_Str;
 import com.hp.hpl.jena.sparql.expr.Expr;
 import com.hp.hpl.jena.sparql.expr.ExprList;
 import com.hp.hpl.jena.sparql.expr.ExprVar;
 import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
 import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.RangeConstraint;
 import org.ilrt.wf.facets.constraints.RegexpConstraint;
 import org.ilrt.wf.facets.constraints.UnConstraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author pldms
  */
 public class SPARQLQueryService implements FacetQueryService {
 
     private final static Logger log = LoggerFactory.getLogger(SPARQLQueryService.class);
 
     protected final static Var SUBJECT = Var.alloc("s");
 
     protected final QEFactory qef;
 
     public SPARQLQueryService(QEFactory qef) {
         this.qef = qef;
 
         // TODO: Work around. Remove this when ARQ is fixed
 
        System.setProperty("http.keepAlive", "false");
     }
 
     @Override
     public List<Resource> getRefinements(Resource base, Property prop, boolean isBroader) {
         long startTime = 0;
         if (log.isDebugEnabled()) startTime = System.currentTimeMillis();
         Tree<Resource> refinements =
                 getHierarchy(base, prop, isBroader, true) ;
         TreeChildrenList<Resource> toReturn = new TreeChildrenList<Resource>(refinements);
         if (log.isDebugEnabled()) log.debug("getRefinements took: {} ms",
                 System.currentTimeMillis() - startTime);
         return toReturn;
     }
 
     @Override
     public Tree<Resource> getHierarchy(Resource base, Property prop,
             boolean isBroader) {
         long startTime = 0;
         if (log.isDebugEnabled()) startTime = System.currentTimeMillis();
         Tree<Resource> toReturn = getHierarchy(base, prop, isBroader, false);
         if (log.isDebugEnabled()) log.debug("getHierarchy took: {} ms",
                 System.currentTimeMillis() - startTime);
         return toReturn;
     }
 
     /**
      * Common code for refinements and hierarchy. This is a bit confusing, but
      * essentially we use the same basic query, but fill in some values for the
      * only children case (which is anchored at base).
      * @param base Where to begin
      * @param prop Property relating elements in the hierarchy
      * @param isBroader If true go from objects to subjects.
      * @param onlyChildren Only get immediate children of base
      * @return
      */
     protected Tree<Resource> getHierarchy(Resource base, Property prop,
             boolean isBroader, boolean onlyChildren) {
         // If we are only getting immediate children anchor accordingly
         String s = (onlyChildren && !isBroader) ? "<" + base.getURI() + ">" : "?s";
         String o = (onlyChildren && isBroader) ? "<" + base.getURI() + ">" : "?o";
         // Get label for children, according to which will be the child
         String labelFor = (isBroader) ? "?s" : "?o";
 
         Query query = QueryFactory.create(
                 String.format(
                 "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                 "construct " +
                 "{ " +
                 "%1$s <%2$s> %3$s ." +
                 "%4$s rdfs:label ?label . " +
                 "}\n" +
                 "{\n" +
                 "  graph ?g  { %1$s <%2$s> %3$s \n" +
                 "  optional { %4$s rdfs:label ?label } }\n" +
                 "}",
                    s, prop.getURI(), o, labelFor
                 ));
 
         QueryExecution qe = qef.get( query );
 
         Model hierarchy = qe.execConstruct();
         qe.close();
 
         // Make local base, so we don't have to pass model about
         Resource baseL = hierarchy.getResource(base.getURI());
 
         Set<Resource> visited = new HashSet<Resource>(); // avoid loops
         if (isBroader) return makeBroaderTreeStartingAt(baseL, prop, visited);
         else return makeNarrowerTreeStartingAt(baseL, prop, visited);
     }
 
     /**
      * Make a tree starting at base and going up broader relations
      * @param base
      * @param prop
      * @return
      */
     private Tree<Resource> makeBroaderTreeStartingAt(Resource base, Property prop, Set visited) {
         if (visited.contains(base)) throw new RuntimeException("Loop in hierarchy");
         visited.add(base);
         Tree<Resource> t = new Tree<Resource>(base);
         ResIterator r = base.getModel().listSubjectsWithProperty(prop, base);
         while (r.hasNext()) {
             t.addChild( makeBroaderTreeStartingAt(r.next(), prop, visited) );
         }
         r.close();
         return t;
     }
 
     /**
      * Make a tree starting at base and moving down narrower relations
      * @param base
      * @param prop
      * @return
      */
     private Tree<Resource> makeNarrowerTreeStartingAt(Resource base, Property prop, Set visited) {
         if (visited.contains(base)) throw new RuntimeException("Loop in hierarchy");
         visited.add(base);
         Tree<Resource> t = new Tree<Resource>(base);
         StmtIterator r = base.listProperties(prop);
         while (r.hasNext()) {
             t.addChild( makeNarrowerTreeStartingAt(r.next().getResource(), prop, visited) );
         }
         r.close();
         return t;
     }
 
     @Override
     public Map<FacetState, Integer> getCounts(List<? extends FacetState> currentFacetStates) {
         long startTime = 0;
         if (log.isDebugEnabled()) startTime = System.currentTimeMillis();
         // Inefficient first pass
         Map<FacetState, Integer> counts = new HashMap<FacetState, Integer>();
         VarGen vgen = new VarGen();
         for (FacetState state: currentFacetStates) {
             getStateCounts(state, currentFacetStates, counts, vgen);
         }
         if (log.isDebugEnabled()) log.debug("getCounts took: {} ms",
                 System.currentTimeMillis() - startTime);
         return counts;
     }
 
     @Override
     public int getCount(List<? extends FacetState> currentFacetStates) {
         long startTime = 0;
         if (log.isDebugEnabled()) startTime = System.currentTimeMillis();
         int toReturn = getCount(statesToConstraints(currentFacetStates), new VarGen());
         if (log.isDebugEnabled()) log.debug("getCount took: {} ms",
                 System.currentTimeMillis() - startTime);
         return toReturn;
     }
 
     /*
     private List<Resource> describeSubjects(Op constraints, int offset, int number) {
         long startTime = 0;
         if (log.isDebugEnabled()) startTime = System.currentTimeMillis();
         
         // We did use DESCRIBE, but this had scalability issues and didn't go backwards
         // Hence shifted to CONSTRUCT
 
         Triple forward = Triple.create(SUBJECT, genVar(), genVar());
 
         TemplateGroup template = new TemplateGroup();
         template.addTriple(forward);
 
         BasicPattern bgpF = new BasicPattern();
         bgpF.add(forward);
 
         // Join the subject restriction to the info gatherer
         Op op = OpJoin.create(constraints, new OpGraph(genVar(), new OpBGP(bgpF)));
 
         op = new OpSlice(op, offset, number);
 
         Query q = OpAsQuery.asQuery(op);
         
         q.setQueryConstructType();
         q.setConstructTemplate(template);
 
         QueryExecution qe = qef.get(q);
         Model m = qe.execConstruct();
         qe.close();
 
         // We have our results! Now we work out what matched the constraint...
 
         // Just subject, please...
         op = new OpProject(constraints, Collections.singletonList(SUBJECT));
         q = OpAsQuery.asQuery(op);
         q.setQuerySelectType();
 
         log.info("Local seleect:\n{}", q);
 
         DataSource ds = DatasetFactory.create();
         ds.addNamedModel("urn:x-ilrt:graph", m);
         qe = QueryExecutionFactory.create(q, ds);
         ResultSet res = qe.execSelect();
 
         List<Resource> results = new LinkedList<Resource>();
         while (res.hasNext()) {
             Resource result = res.next().getResource(SUBJECT.getVarName());
             // We need to reattach to original model. Nasty.
             result = result.isAnon() ?
                 m.createResource(result.getId()) :
                 m.getResource(result.getURI()) ;
             results.add(result);
         }
 
         qe.close();
 
         if (log.isDebugEnabled()) log.debug("getResults took: {} ms",
                 System.currentTimeMillis() - startTime);
 
         return results;
     }*/
 
     @Override
     public List<Resource> getResults(List<? extends FacetState> currentFacetStates, int offset, int number) {
         long startTime = 0;
         if (log.isDebugEnabled()) startTime = System.currentTimeMillis();
         // Remember this query. We'll use it later.
         Op opBasic = constraintsToOp(statesToConstraints(currentFacetStates), new VarGen());
         Op op = opBasic;
         op = new OpProject(op, Collections.singletonList(SUBJECT));
         // Apply limit and offset
         op = new OpSlice(op, offset, number);
         Query q = OpAsQuery.asQuery(op);
         q.addDescribeNode(SUBJECT);
         q.setQueryDescribeType();
 
         QueryExecution qe = qef.get(q);
         Model m = qe.execDescribe();
         qe.close();
 
         // We now have a model containing the things we were interested in
         // plus a bunch of info about them. We want to return pointer to those
         // original things (backed by this model), so we execute the same query.
 
         q = OpAsQuery.asQuery(opBasic);
         q.addResultVar(SUBJECT);
         q.setQuerySelectType();
         DataSource ds = DatasetFactory.create();
         ds.addNamedModel("urn:x-ilrt:graph", m);
         qe = QueryExecutionFactory.create(q, ds);
         ResultSet res = qe.execSelect();
 
         List<Resource> results = new LinkedList<Resource>();
         while (res.hasNext()) {
             Resource result = res.next().getResource(SUBJECT.getVarName());
             // We need to reattach to original model. Nasty.
             result = result.isAnon() ?
                 m.createResource(result.getId()) :
                 m.getResource(result.getURI()) ;
             results.add(result);
         }
 
         qe.close();
 
         if (log.isDebugEnabled()) log.debug("getResults took: {} ms",
                 System.currentTimeMillis() - startTime);
 
         return results;
     }
 
     protected void getStateCounts(FacetState state,
             List<? extends FacetState> currentFacetStates, 
             Map<FacetState, Integer> counts, VarGen vgen) {
         // Get contrast state
         List<FacetState> otherStates = new LinkedList<FacetState>(currentFacetStates);
         otherStates.remove(state);
         for (FacetState futureState: state.getRefinements()) {
             counts.put(futureState, getCount(futureState, otherStates, vgen));
         }
     }
 
     protected int getCount(FacetState ffs, List<FacetState> otherStates, VarGen vgen) {
         return getCount(statesToConstraints(otherStates, ffs), vgen);
     }
 
     protected int getCount(Collection<Constraint> constraints, VarGen vgen) {
         Op op = constraintsToOp(constraints, vgen);
         
         // Using count(*) -- no idea how to do this in algebra
 
         // Project out everything. We add count later
         op = new OpProject(op, Collections.EMPTY_LIST);
         Query q = OpAsQuery.asQuery(op);
         q.setQuerySelectType();
         q.setQueryResultStar(false);
         Expr agg = q.allocAggregate(new AggCount());
         q.addResultVar("count", agg);
         QueryExecution qe = qef.get(q);
 
         ResultSet r = qe.execSelect();
         int count = r.next().getLiteral("count").getInt();
         qe.close();
 
         // just get subject
         /*
         op = new OpProject(op, Collections.singletonList(SUBJECT));
 
 
 
         Query q = OpAsQuery.asQuery(op);
         QueryExecution qe = qef.get(q);
 
         int count = 0;
         ResultSet r = qe.execSelect();
         while (r.hasNext()) { count++; r.next(); }
 
         qe.close();*/
         
         return count;
     }
 
     protected Op constraintsToOp(Collection<Constraint> cons, VarGen vgen) {
         Op op = null;
         for (Constraint con: cons) {
             Op newOp = constraintToOp(con, vgen);
             if (newOp instanceof OpNull) continue;
             // We wrap each constraint in a new graph
             // TODO prettify queries. this messes up previous work
             else if (op == null) op = new OpGraph(vgen.genVar(), newOp);
             else op = OpJoin.create(op, new OpGraph(vgen.genVar(), newOp));
 
         }
 
         // Completelty unconstrained is not permitted
         if (op == null) throw new RuntimeException("Operation is completely unconstrained");
         op = Transformer.transform(new QueryCleaner(), op);
         return op;
     }
 
     protected Op constraintToOp(Constraint constraint, VarGen vgen) {
         if (constraint instanceof UnConstraint) return OpNull.create();
         else if (constraint instanceof ValueConstraint) {
             return tripleToBGP(SUBJECT,
                     constraint.getProperty().asNode(),
                     ((ValueConstraint) constraint).getValue().asNode(),
                     constraint.isPropertyInverted()
                     );
         } else if (constraint instanceof RangeConstraint) {
             RangeConstraint rc = (RangeConstraint) constraint;
             Var val = vgen.genVar();
             // create filters
             return OpFilter.filter(
                     new E_LogicalAnd(
                         new E_LessThanOrEqual(
                             NodeValueNode.makeNode(rc.getFrom().asNode()),
                             new ExprVar(val) ),
                         new E_LessThan(
                             new ExprVar(val),
                             NodeValueNode.makeNode(rc.getTo().asNode()) )
                             ),
                     tripleToBGP(SUBJECT, constraint.getProperty().asNode(), val, constraint.isPropertyInverted())
                     );
         } else if (constraint instanceof RegexpConstraint) {
             RegexpConstraint rc = (RegexpConstraint) constraint;
             Var val = vgen.genVar();
             return OpFilter.filter(
                     new E_Regex(
                       new E_Str(new ExprVar(val)),
                       rc.getRegexp(),
                       "i"
                       ),
                     tripleToBGP(SUBJECT, constraint.getProperty().asNode(), val, constraint.isPropertyInverted())
                     );
         } else throw new RuntimeException("Unknown constraint type");
     }
 
     protected Op tripleToBGP(Node s, Node p, Node o, boolean invert) {
         BasicPattern bgp = new BasicPattern();
         if (invert) bgp.add(Triple.create(o, p, s));
         else bgp.add(Triple.create(s, p, o));
         return new OpBGP(bgp);
     }
 
     protected Collection<Constraint> statesToConstraints(Collection<? extends FacetState> states, FacetState... moreStates) {
         Collection<Constraint> cs = new LinkedList<Constraint>();
         for (FacetState s: states) cs.addAll(s.getConstraints());
         for (FacetState s: moreStates) cs.addAll(s.getConstraints());
         return cs;
     }
 
     //private int varCount = 0;
     //private Var genVar() { varCount++; return Var.alloc("v" + varCount); }
 
     // TODO Need to think about making this smarter
     // How to choose from multiple results, for example
     @Override
     public String getLabelFor(Resource thing) {
         Var label = Var.alloc("label");
         Op op = tripleToBGP(thing.asNode(), RDFS.label.asNode(), label, false);
         op = new OpGraph(Var.alloc("g"), op);
         op = new OpProject(op, Arrays.asList(label));
         Query q = OpAsQuery.asQuery(op);
         QueryExecution qe = qef.get(q);
         ResultSet results = qe.execSelect();
         String lab;
         if (results.hasNext()) {
             RDFNode val = results.next().get("label");
             if (val.isLiteral()) lab = val.as(Literal.class).getLexicalForm();
             else lab = null;
         } else {
             lab = null;
         }
         qe.close();
         return lab;
     }
 
     @Override
     public Resource getInformationAbout(Resource thing) {
         if (thing.isAnon()) throw new IllegalArgumentException("Described nodes must be ground");
         Query q = QueryFactory.create(String.format("DESCRIBE <%s>", thing.getURI()));
         QueryExecution qe = qef.get(q);
         Model m = qe.execDescribe();
         qe.close();
         return m.createResource(thing.getURI());
     }
 
     @Override
     public Resource getInformationAboutIndirect(Property property, RDFNode value) {
         Var thing = Var.alloc("thing");
         Op op = tripleToBGP(thing, property.asNode(), value.asNode(), false);
         op = new OpGraph(Var.alloc("g"), op);
         op = new OpProject(op, Arrays.asList(thing));
         Query q = OpAsQuery.asQuery(op);
         q.setQueryDescribeType();
         QueryExecution qe = qef.get(q);
         Model m = qe.execDescribe();
         qe.close();
 
         // Ok, now the same thing locally to find out what we described
 
         q.setQuerySelectType(); // select this time
         DataSource ds = DatasetFactory.create();
         ds.addNamedModel("http://example.com", m);
         qe = QueryExecutionFactory.create(q, ds);
         ResultSet results = qe.execSelect();
         Resource t;
         if (results.hasNext()) {
             t = results.next().getResource("thing");
             t = m.createResource(t.getURI()); // ensure t is associated with model
         } else {
             t = null;
         }
         qe.close();
         return t;
     }
 
     @Override
     public Collection<RDFNode> getValuesOfPropertyForType(Resource type, Property property, boolean invert) {
         Var thing = Var.alloc("thing");
         Var val = Var.alloc("val");
         Var label = Var.alloc("label");
 
         BasicPattern bgp = new BasicPattern();
         bgp.add(Triple.create(thing, RDF.type.asNode(), type.asNode()));
         if (invert) // reverse
             bgp.add(Triple.create(val, property.asNode(), thing));
         else
             bgp.add(Triple.create(thing, property.asNode(), val));
         
         // Bit to get label (if available)
         BasicPattern labelBGP = new BasicPattern();
         labelBGP.add(Triple.create(val, RDFS.label.asNode(), label));
         Op opGetLabel = new OpGraph(Var.alloc("lg"), new OpBGP(labelBGP));
         
         Op op = new OpBGP(bgp);
         op = new OpGraph(Var.alloc("g"), op);
         op = OpLeftJoin.create(op, opGetLabel, (ExprList) null);
         op = new OpProject(op, Arrays.asList(val, label)); // select ?val, ?label
         op = new OpDistinct(op);
 
         Query q = OpAsQuery.asQuery(op);
         QueryExecution qe = qef.get(q);
         ResultSet results = qe.execSelect();
         Collection<RDFNode> vals = new ArrayList<RDFNode>();
 
         RDFNode aValue;
         RDFNode aLabel;
         QuerySolution result;
         Model resModel = ModelFactory.createDefaultModel();
         while (results.hasNext()) {
             result = results.next();
             aValue = result.get("val");
             aLabel = result.get("label");
             if (aLabel != null) {
                 aValue = aValue.inModel(resModel); // attach to our model
                 aValue.asResource().addProperty(RDFS.label, aLabel);
             }
             vals.add(aValue);
         }
 
         return vals;
     }
 
     /**
      * This is for cleaning up the query having created it
      */
     private static class QueryCleaner extends TransformBase
     {
         @Override
         public Op transform(OpJoin join, Op left, Op right) {
             if (!(left instanceof OpFilter && right instanceof OpFilter)) return join;
 
             OpFilter leftF = (OpFilter) left;
             OpFilter rightF = (OpFilter) right;
 
             if (!(leftF.getSubOp() instanceof OpBGP &&
                     rightF.getSubOp() instanceof OpBGP)) return join;
 
             ((OpBGP) leftF.getSubOp()).getPattern().addAll(((OpBGP) rightF.getSubOp()).getPattern());
             leftF.getExprs().addAll(rightF.getExprs());
             return leftF;
         }
     }
 
     /**
      * Convenience wrapper to avoid copying
      * @param <T>
      */
     private static class TreeChildrenList<T> extends AbstractList<T> {
         private final Tree<T> tree;
 
         public TreeChildrenList(Tree<T> tree) {
             super();
             this.tree = tree;
         }
 
         @Override
         public T get(int i) {
             return tree.getChildren().get(i).getValue();
         }
 
         @Override
         public int size() {
             return tree.getChildren().size();
         }
 
         @Override
         public Iterator<T> iterator() {
             return new TreeIterator<T>(tree.getChildren().iterator());
         }
 
     }
 
     /**
      * Helper for the above, to avoid thrashing linked list
      *
      * @param <T>
      */
     private static class TreeIterator<T> implements Iterator<T> {
         private Iterator<Tree<T>> it;
 
         public TreeIterator(Iterator<Tree<T>> wrappedIt) {
             this.it = wrappedIt;
         }
 
         @Override
         public boolean hasNext() { return it.hasNext(); }
 
         @Override
         public T next() { return it.next().getValue(); }
 
         @Override
         public void remove() { throw new UnsupportedOperationException("Not supported"); }
     }
     
     /**
      * Simple class to generate variables when needed
      */
     protected static class VarGen {
         private int num = 0;
         
         public Var genVar() {
             num++;
             return Var.alloc("v" + num);
         }
     }
 }
