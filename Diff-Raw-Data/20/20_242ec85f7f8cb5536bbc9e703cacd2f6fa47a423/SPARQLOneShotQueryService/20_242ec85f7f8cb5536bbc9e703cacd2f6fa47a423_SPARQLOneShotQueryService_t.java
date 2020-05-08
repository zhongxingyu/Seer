 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.ilrt.wf.facets.sparql;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.sparql.algebra.Op;
 import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
 import com.hp.hpl.jena.sparql.algebra.OpVisitor;
 import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
 import com.hp.hpl.jena.sparql.algebra.OpWalker;
 import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
 import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
 import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
 import com.hp.hpl.jena.sparql.core.Var;
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 
 /**
  *
  * @author pldms
  */
 public class SPARQLOneShotQueryService extends SPARQLQueryService {
 
     public SPARQLOneShotQueryService(QEFactory qef) {
         super(qef);
     }
 
     /**
      * Implementation using one query
      * @param currentFacetStates
      * @return
      */
     @Override
     public Map<FacetState, Integer> getCounts(List<? extends FacetState> currentFacetStates) {
         Map<FacetState, Integer> counts = new HashMap<FacetState, Integer>();
         Collection<Constraint> allConstraints = statesToConstraints(currentFacetStates);
 
         // This limits SUBJECT to those things matching current state
         Op op = constraintsToOp(allConstraints);
 
         // Now we want to grab the values to count for refinements
         Map<Property, String> propToVar = new HashMap<Property, String>();
         VarMaker vf = new VarMaker();
         Collection<Constraint> refinedConstraints = new LinkedList<Constraint>();
         for (FacetState parent: currentFacetStates) {
             for (FacetState child: parent.getRefinements())
                 refinedConstraints.addAll(child.getConstraints());
         }
         for (Constraint c: refinedConstraints) {
             if (!propToVar.containsKey(c.getProperty())) {
                 Var val = findVariable(c.getProperty(), op);
                 if (val == null) {
                     val = vf.make();
                     op = OpJoin.create(op,
                         new OpGraph( vf.make() ,
                         tripleToBGP(SUBJECT, c.getProperty().asNode(), val)));
                 }
                 propToVar.put(c.getProperty(), val.getVarName());
             }
         }
 
        List<Var> vars = new ArrayList<Var>();
        vars.add(SUBJECT);
        // Bah, I just can't win here.
        for (String varName: propToVar.values()) vars.add(Var.alloc(varName));
        op = new OpProject(op, vars);

         // Now get the results...
         Query q = OpAsQuery.asQuery(op);
         q.setQuerySelectType();
         q.setQueryResultStar(false);
 
         QueryExecution qe = qef.get(q);
         ResultSet results = qe.execSelect();
         countMatchingRefinements(counts, currentFacetStates, results, propToVar);
 
         qe.close();
 
         return counts;
     }
 
     private void countMatchingRefinements(Map<FacetState, Integer> counts,
             List<? extends FacetState> currentFacetStates,
             ResultSet results,
             Map<Property, String> propToVar) {
 
         // Construct accumulators
         List<Accumulator> accumulators = new LinkedList<Accumulator>();
         for (FacetState state: currentFacetStates) {
             for (FacetState child: state.getRefinements()) {
                 accumulators.add(new Accumulator(child, propToVar));
             }
         }
 
         // Handle results
         String subj = SUBJECT.getVarName();
         while (results.hasNext()) {
             QuerySolution result = results.next();
             for (Accumulator acc: accumulators) {
                 RDFNode subject = result.get(subj);
                 acc.handle(result, subject);
             }
         }
 
         // Collect counts
         for (Accumulator acc: accumulators) {
             counts.put(acc.getState(), acc.getCount());
         }
     }
 
     // Find a variable which is the object of property, if any
     private Var findVariable(Property property, Op op) {
         final Node p = property.asNode();
         final Var[] foundVar = new Var[1];
         OpVisitor finder = new OpVisitorBase() {
             boolean found = false;
             @Override public void visit(OpBGP op) {
                 if (found) return;
                 for (Triple triple: op.getPattern()) {
                     if (triple.getPredicate().equals(p) &&
                             triple.getObject() instanceof Var) {
                         foundVar[0] = (Var) triple.getObject();
                         found = true;
                     }
                 }
             }
         };
 
         OpWalker.walk(op, finder);
 
         return foundVar[0];
     }
 
     public static class Accumulator {
         private final FacetState state;
         private final Set<RDFNode> counted;
         private int count;
         private final Map<Property, String> propToVar;
 
         public Accumulator(FacetState state, Map<Property, String> propToVar) {
             this.state = state;
             this.counted = new HashSet<RDFNode>();
             this.count = 0;
             this.propToVar = propToVar;
         }
 
         public void handle(QuerySolution soln, RDFNode subject) {
             if (counted.contains(subject)) return; // no double counting
             boolean matches = true;
             for (Constraint constraint: state.getConstraints()) {
                 // Phew!
                 // check that constraint matches the value bound to the requisite
                 // property
                 if (!constraint.matches(
                         soln.get(
                         propToVar.get(constraint.getProperty())))) matches = false;
             }
             if (matches) {
                 counted.add(subject);
                 count++;
             }
         }
 
         public int getCount() { return count; }
 
         public FacetState getState() { return state; }
     }
 
     public static class VarMaker {
         private int num = 0;
 
         public Var make() { num++; return Var.alloc("vm" + num); }
         
     }
 }
