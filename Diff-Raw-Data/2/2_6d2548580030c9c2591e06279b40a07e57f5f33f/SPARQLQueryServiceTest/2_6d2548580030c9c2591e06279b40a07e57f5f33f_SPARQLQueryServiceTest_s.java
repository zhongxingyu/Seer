 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.ilrt.wf.facets.sparql;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.sparql.algebra.Algebra;
 import com.hp.hpl.jena.util.FileManager;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.ilrt.wf.facets.FacetQueryService.Tree;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.RangeConstraint;
 import org.ilrt.wf.facets.constraints.RegexpConstraint;
 import org.ilrt.wf.facets.constraints.UnConstraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author pldms
  */
 public class SPARQLQueryServiceTest {
     private final Model model;
     private final String NS = "http://example.com/ns#";
     private final Property prop = ResourceFactory.createProperty(NS, "prop");
     private final Property range = ResourceFactory.createProperty(NS, "range");
     private final Property label = ResourceFactory.createProperty(NS, "label");
     private final RDFNode val = ResourceFactory.createResource(NS + "value");
     private final Property broader = ResourceFactory.createProperty(NS, "broader");
     private final Property narrower = ResourceFactory.createProperty(NS, "narrower");
 
 
     private Resource make(String n) {
         return ResourceFactory.createResource(NS + n);
     }
 
     public SPARQLQueryServiceTest() {
         URL data = this.getClass().getResource("/sparql/testdata.ttl");
         this.model = FileManager.get().loadModel(data.toExternalForm());
     }
 
     /**
      * Test of getRefinements method, of class SPARQLQueryService.
      */
     @Test
     public void testGetBroaderRefinements() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
         List<Resource> a = instance.getRefinements(make("a"), broader, true);
 
         assertEquals("Got correct broader refinements", 3, a.size());
     }
 
     @Test
     public void testGetNarrowerRefinements() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
         List<Resource> a = instance.getRefinements(make("a"), narrower, false);
 
         assertEquals("Got correct narrower refinements", 4, a.size());
     }
 
     @Test
     public void testGetHierarchy() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
         Tree<Resource> node1, node2, node3;
         node1 = new Tree<Resource>(make("y2"));
         node1.addChild(new Tree<Resource>(make("z1"))).
                 addChild(new Tree<Resource>(make("z2")));
         node2 = new Tree<Resource>(make("y1"));
         node2.addChild(new Tree<Resource>(make("z3")));
         node3 = new Tree<Resource>(make("x"));
         node3.addChild(new Tree<Resource>(make("y3"))).
                 addChild(node1).addChild(node2);
         Tree<Resource> a = instance.getHierarchy(make("x"), broader, true);
         assertEquals(node3, a);
 
         a = instance.getHierarchy(make("x"), broader, false);
         assertFalse(node3.equals(a));
 
         a = instance.getHierarchy(make("x"), narrower, false);
         assertEquals(node3, a);
 
         a = instance.getHierarchy(make("x"), broader, true);
         assertTrue(node3.equals(a));
     }
 
     @Test
     public void checkConstraintToOp() {
         SPARQLQueryService instance = new SPARQLQueryService(null);
 
         Constraint constraint = new UnConstraint();
         assertEquals(Algebra.parse("(null)"),
                 instance.constraintToOp(constraint));
 
         constraint = new ValueConstraint(prop, val);
         assertEquals(Algebra.parse("(bgp (triple ?s <http://example.com/ns#prop> <http://example.com/ns#value>))"),
                 instance.constraintToOp(constraint));
 
         constraint = new RangeConstraint(prop, ResourceFactory.createPlainLiteral("a"), ResourceFactory.createPlainLiteral("z"));
         assertEquals(Algebra.parse("(filter (&& (<= \"a\" ?v1) (< ?v1 \"z\") ) (bgp (triple ?s <http://example.com/ns#prop> ?v1)))"),
                 instance.constraintToOp(constraint));
 
         constraint = new RegexpConstraint(prop, "^a");
        assertEquals(Algebra.parse("(filter (regex ?v2 \"^a\" \"i\") (bgp (triple ?s <http://example.com/ns#prop> ?v2)))"),
                 instance.constraintToOp(constraint));
     }
 
     @Test
     public void checkCount() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
 
         Collection<Constraint> cos = new LinkedList<Constraint>();
         Collections.addAll(cos,
                 new ValueConstraint(prop, val),
                 makeRangeCon(0, 5)
                 );
 
         assertEquals(3, instance.getCount(cos));
 
         cos = new LinkedList<Constraint>();
         Collections.addAll(cos,
                 new ValueConstraint(prop, val),
                 new RegexpConstraint(label, "^A")
                 );
 
         assertEquals(4, instance.getCount(cos));
 
         cos = new LinkedList<Constraint>();
         Collections.addAll(cos,
                 makeRangeCon(6, 9),
                 new ValueConstraint(prop, val),
                 new RegexpConstraint(label, "^g")
                 );
 
         assertEquals(1, instance.getCount(cos));
     }
 
     @Test
     public void checkFullCounts() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
 
         MFacetState a = new MFacetState(new UnConstraint());
         FacetState a1 =
                 a.addRefinement( new MFacetState(new RegexpConstraint(label, "^a")) );
         FacetState a2 =
                 a.addRefinement( new MFacetState(new RegexpConstraint(label, "^b")) );
         FacetState a3 =
                 a.addRefinement( new MFacetState(new RegexpConstraint(label, "^g")) );
         FacetState a4 =
                 a.addRefinement( new MFacetState(new RegexpConstraint(label, "^x")) );
 
         Map<FacetState, Integer> counts = instance.getCounts(Collections.singletonList(a));
 
         assertEquals(8, (int) counts.get(a1));
         assertEquals(4, (int) counts.get(a2));
         assertEquals(2, (int) counts.get(a3));
         assertEquals(0, (int) counts.get(a4));
 
         MFacetState b = new MFacetState(new ValueConstraint(prop, val));
 
         counts = instance.getCounts(Arrays.asList(a, b));
 
         assertEquals(4, (int) counts.get(a1));
         assertEquals(2, (int) counts.get(a2));
         assertEquals(1, (int) counts.get(a3));
         assertEquals(0, (int) counts.get(a4));
 
         MFacetState c = new MFacetState(makeRangeCon(5, 9));
         FacetState c1 =
                 c.addRefinement( new MFacetState(makeRangeCon(5,7)) );
         FacetState c2 =
                 c.addRefinement( new MFacetState(makeRangeCon(7,9)) );
 
         counts = instance.getCounts(Arrays.asList(a, c));
 
         assertEquals(2, (int) counts.get(a1));
         assertEquals(4, (int) counts.get(a2));
         assertEquals(2, (int) counts.get(a3));
 
         assertEquals(4, (int) counts.get(c1));
         assertEquals(4, (int) counts.get(c2));
     }
 
     @Test
     public void checkResults() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
 
         MFacetState a = new MFacetState(new ValueConstraint(prop, val));
         List<Resource> r = instance.getResults(Collections.singletonList(a), 0, 100);
         assertEquals(7, r.size());
 
         r = instance.getResults(Collections.singletonList(a), 1, 3);
         assertEquals(3, r.size());
 
         r = instance.getResults(Collections.singletonList(a), 2, 3);
         assertEquals(3, r.size());
 
         r = instance.getResults(Collections.singletonList(a), 6, 3);
         assertEquals(1, r.size());
 
         r = instance.getResults(Collections.singletonList(a), 9, 3);
         assertEquals(0, r.size());
 
         Resource res =
                 instance.getResults(Collections.singletonList(a), 1, 3).get(0);
 
         assertTrue(res.hasProperty(prop));
         assertTrue(res.hasProperty(prop, val));
         assertTrue(res.hasProperty(range));
         assertTrue(res.hasProperty(label));
     }
 
     @Test
     public void checkTotalCount() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
 
         MFacetState a = new MFacetState(new ValueConstraint(prop, val));
         MFacetState b = new MFacetState(new RegexpConstraint(label, "^b"));
 
         assertEquals(7, instance.getCount(Collections.singletonList(a)));
         assertEquals(4, instance.getCount(Collections.singletonList(b)));
         assertEquals(2, instance.getCount(Arrays.asList(a, b)));
     }
 
     @Test
     public void checkSafety() {
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
 
         try {
             instance.constraintsToOp(Arrays.<Constraint>asList(
                 new UnConstraint(),
                 new UnConstraint()
                 ));
         } catch (RuntimeException e) {
             return;
         }
 
         fail();
     }
 
     private RangeConstraint makeRangeCon(int start, int end) {
         return new RangeConstraint(range,
                 ResourceFactory.createTypedLiteral(start),
                 ResourceFactory.createTypedLiteral(end)
                 );
     }
 
     static class MFacetState implements FacetState {
         private final List<Constraint> constraints = new LinkedList<Constraint>();
         private final List<FacetState> refinements = new LinkedList<FacetState>();
 
         public MFacetState(Constraint... cons) {
             Collections.addAll(constraints, cons);
         }
 
         @Override
         public String getName() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public int getCount() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean isRoot() {
             return false;
         }
 
         @Override
         public List<FacetState> getRefinements() {
             return refinements;
         }
 
         @Override
         public FacetState getParent() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String getParamValue() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Collection<Constraint> getConstraints() {
             return constraints;
         }
 
         public FacetState addRefinement(FacetState state) {
             refinements.add(state);
             return state;
         }
     }
 
 }
