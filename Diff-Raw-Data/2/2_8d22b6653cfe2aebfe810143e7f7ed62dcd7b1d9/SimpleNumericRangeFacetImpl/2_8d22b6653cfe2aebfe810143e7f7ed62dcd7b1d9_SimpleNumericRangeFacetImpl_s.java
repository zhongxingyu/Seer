 package org.ilrt.wf.facets.impl;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import org.ilrt.wf.facets.Facet;
 import org.ilrt.wf.facets.FacetEnvironment;
 import org.ilrt.wf.facets.FacetFactory;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.RangeConstraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class SimpleNumericRangeFacetImpl extends AbstractFacetFactoryImpl implements FacetFactory {
 
     @Override
     public Facet create(FacetEnvironment environment) {
 
         // the facet state to be passed to the facet
         FacetState facetState;
 
         // each state is constrained to a type, e.g. foaf:Person
         ValueConstraint typeConstraint = createTypeConstraint(environment.getConfig()
                 .get(Facet.CONSTRAINT_TYPE));
 
         // property used in each state
         Property p = ResourceFactory.createProperty(environment.getConfig()
                 .get(Facet.LINK_PROPERTY));
 
         // create a pseudo parent
         FacetStateImpl root = new FacetStateImpl();
         root.setRoot(true);
 
         // this range facet has been selected via the request object
         if (environment.getParameters().containsKey(environment.getConfig()
                 .get(Facet.PARAM_NAME))) {
 
             // get the label from the parameter value
             String[] values = environment.getParameters()
                     .get(environment.getConfig().get(Facet.PARAM_NAME));
             String value = values[0];
 
             String[] parts = value.split(":");
 
             // create a state to represent the currently selected state
             facetState = new FacetStateImpl(label(parts), root, value,
                     Arrays.asList(typeConstraint, rangeConstraint(p, parts)));
 
         } else { // we want them all
 
             root.getConstraints().addAll(Arrays.asList(typeConstraint));
             root.setRefinements(refinements(environment.getConfig().get(Facet.NUMERIC_RANGE),
                     typeConstraint, p, root));
             facetState = root;
         }
 
         // create the facet
         return new FacetImpl(getFacetTitle(environment), facetState, getParameterName(environment));
     }
 
 
     protected List<FacetState> refinements(String rangeConfig, Constraint typeConstraint,
                                            Property linkProperty, FacetState rootState) {
 
         // list to hold refinements
         List<FacetState> refinementsList = new ArrayList<FacetState>();
 
         String[] ranges = rangeConfig.split(",");
 
         for (String range : ranges) {
 
             String[] parts = range.split(":");
 
             refinementsList.add(new FacetStateImpl(label(parts), rootState, range,
                     Arrays.asList(typeConstraint, rangeConstraint(linkProperty, parts))));
         }
 
         return refinementsList;
     }
 
     protected String label(String[] parts) {
         return df.format(Integer.parseInt(parts[0])) + " - " + df.format(Integer.parseInt(parts[1]));
     }
 
     protected Constraint rangeConstraint(Property property, String[] parts) {
 
         Literal lower = ResourceFactory.createTypedLiteral(parts[0], XSDDatatype.XSDinteger);
         Literal upper = ResourceFactory.createTypedLiteral(parts[1], XSDDatatype.XSDinteger);
         return new RangeConstraint(property, lower, upper);
     }
 
    DecimalFormat df = new DecimalFormat("###,###");
 }
