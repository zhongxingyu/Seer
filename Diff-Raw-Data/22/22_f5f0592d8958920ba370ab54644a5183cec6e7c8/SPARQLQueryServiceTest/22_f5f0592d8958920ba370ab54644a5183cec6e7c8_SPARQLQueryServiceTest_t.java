 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.ilrt.wf.facets.sparql;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.sparql.algebra.Algebra;
 import com.hp.hpl.jena.util.FileManager;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.RangeConstraint;
 import org.ilrt.wf.facets.constraints.RegexpConstraint;
 import org.ilrt.wf.facets.constraints.UnConstraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.junit.Ignore;
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
     private final RDFNode val = ResourceFactory.createResource(NS + "val");
     private final Property broader = ResourceFactory.createProperty(NS, "broader");
     private final Property narrower = ResourceFactory.createProperty(NS, "narrower");
 
 
     public SPARQLQueryServiceTest() {
         URL data = this.getClass().getResource("/sparql/testdata.ttl");
         this.model = FileManager.get().loadModel(data.toExternalForm());
     }
 
     /**
      * Test of getRefinements method, of class SPARQLQueryService.
      */
     @Test
     public void testGetBroaderRefinements() {
         FacetState fs = new MFacetState(ResourceFactory.createResource(NS + "a"),
                 broader, FacetState.NONE);
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
         Map<FacetState, List<RDFNode>> a = instance.getRefinements(fs);
 
         assertEquals("Got correct broader refinements", 3, a.get(fs).size());
     }
 
     @Test
     public void testGetNarrowerRefinements() {
         FacetState fs = new MFacetState(ResourceFactory.createResource(NS + "a"),
                 FacetState.NONE, narrower);
         SPARQLQueryService instance = new SPARQLQueryService(new ModelQEFactory(model));
         Map<FacetState, List<RDFNode>> a = instance.getRefinements(fs);
 
         assertEquals("Got correct narrower refinements", 4, a.get(fs).size());
     }
 
     @Test
     public void checkConstraintToOp() {
         SPARQLQueryService instance = new SPARQLQueryService(null);
 
         Constraint constraint = new UnConstraint();
         assertEquals(Algebra.parse("(null)"),
                 instance.constraintToOp(constraint));
 
         constraint = new ValueConstraint(prop, val);
         assertEquals(Algebra.parse("(bgp (triple ?s <http://example.com/ns#prop> <http://example.com/ns#val>))"),
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
                 new RangeConstraint(prop, ResourceFactory.createPlainLiteral("a"), ResourceFactory.createPlainLiteral("z"))
                 );
 
 
     }
 
     /**
      * Test of getCounts method, of class SPARQLQueryService.
      */
     @Ignore
     @Test
     public void testGetCounts() {
     }
 
     static class MFacetState implements FacetState {
         private final Property broader;
         private final Property narrower;
         private final RDFNode value;
         private Collection<Constraint> constraints;
 
         public MFacetState(RDFNode value, Property broader, Property narrower) {
             if (broader == null ||
                     narrower == null) throw new IllegalArgumentException("No nulls");
             this.value = value;
             this.broader = broader;
             this.narrower = narrower;
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
             return value == null;
         }
 
         @Override
         public List<FacetState> getRefinements() {
             throw new UnsupportedOperationException("Not supported yet.");
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
         public Property getBroaderProperty() {
             return broader;
         }
 
         @Override
         public Property getNarrowerProperty() {
             return narrower;
         }
 
         @Override
         public RDFNode getValue() {
             return value;
         }
 
         @Override
         public Collection<Constraint> getConstraints() {
             return constraints;
         }
 
         public void setConstraint(Constraint constraint) {
             this.constraints = Collections.singleton(constraint);
         }
 
         public void setConstraints(Collection<Constraint> constraints) {
             this.constraints = constraints;
         }
     }
 
 }
