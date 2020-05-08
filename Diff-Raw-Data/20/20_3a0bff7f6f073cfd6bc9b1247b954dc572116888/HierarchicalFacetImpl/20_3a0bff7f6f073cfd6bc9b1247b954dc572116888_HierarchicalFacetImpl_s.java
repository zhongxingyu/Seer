 package org.ilrt.wf.facets.impl;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.vocabulary.RDF;
 import org.ilrt.wf.facets.Facet;
 import org.ilrt.wf.facets.FacetEnvironment;
 import org.ilrt.wf.facets.FacetFactory;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.QNameUtility;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * @author Mike Jones (mike.a.jones@bristol.ac.uk)
  */
 public class HierarchicalFacetImpl extends AbstractFacetFactoryImpl implements FacetFactory {
 
     public HierarchicalFacetImpl(FacetQueryService facetQueryService, QNameUtility qNameUtility) {
         this.facetQueryService = facetQueryService;
         this.qNameUtility = qNameUtility;
     }
 
     // ---------- methods implementing the interface
 
     @Override
     public Facet create(FacetEnvironment environment) {
 
         // constraints used in the facet states of this facet
         List<? extends Constraint> constraints = createHierarchicalConstraintList(environment);
 
         // base resource representing the currently selected uri in the hierarchy
         Resource baseResource = createBaseResource(environment);
 
         // broader property
         Property broaderProperty = createBroaderProperty(environment);
 
         // TODO - hard coded true = isBroader -> should be derived from configuration?
         // create the current state - will include refinements
         FacetStateImpl currentFacetState = currentHierarchicalState(constraints, baseResource,
                 broaderProperty, true, environment);
 
         findParents(currentFacetState, ResourceFactory.createResource(environment.getConfig()
                 .get(Facet.FACET_BASE)), baseResource, broaderProperty, true);
 
         // create the facet
 
         return new FacetImpl(getFacetTitle(environment), currentFacetState,
                 getParameterName(environment));
     }
 
     // ---------- methods relating to hierarchical facets
 
     protected FacetStateImpl currentHierarchicalState(List<? extends Constraint> constraints,
                                                       Resource resource, Property property,
                                                       boolean isBroader,
                                                       FacetEnvironment environment) {
         FacetStateImpl currentFacetState = new FacetStateImpl();
         currentFacetState.setParamValue(qNameUtility.getQName(resource.getURI()));
         currentFacetState.getConstraints().addAll(constraints);
 
         List<Resource> resources = facetQueryService.getRefinements(resource,
                 property, isBroader);
 
         Resource type = ResourceFactory.createResource(
                 environment.getConfig().get(Facet.CONSTRAINT_TYPE)
         );
 
         Property link = ResourceFactory.createProperty(
                 environment.getConfig().get(Facet.LINK_PROPERTY)
         );
 
         currentFacetState.setRefinements(hierarchicalRefinements(resources,
                 currentFacetState, link, type));
 
         return currentFacetState;
     }
 
     protected List<FacetState> hierarchicalRefinements(List<Resource> resources,
                                                        FacetState parentState,
                                                        Property link,
                                                        Resource type) {
         List<FacetState> refinementList = new ArrayList<FacetState>();
 
         for (Resource resource : resources) {
 
             String uri = resource.getURI();
             String label = getLabel(resource);
 
             refinementList.add(new FacetStateImpl(label, parentState,
                     qNameUtility.getQName(uri),
                     createHierarchicalConstraintList(type, link, resource)
             ));
         }
 
         return refinementList;
     }
 
     protected void findParents(FacetStateImpl currentState,
                                Resource base, Resource current, Property property, boolean isBroader) {
 
         FacetQueryService.Tree<Resource> results = facetQueryService.getHierarchy(base, property, isBroader);
 
         List<Resource> familyHistory = findChildren(results, current.getURI());
 
         FacetStateImpl parentState = null;
 
         ListIterator<Resource> iter = familyHistory.listIterator();
 
         while (iter.hasNext()) {
 
             // get the next resource in the path
             Resource resource = iter.next();
 
             // get the URIs and label
             String uri = resource.getURI();
             String label = getLabel(resource);
 
             // end of the line - so is actually the current state
             if (!iter.hasNext()) {
 
                 currentState.setName(label);
                 //currentState.setParamValue(qNameUtility.getQName(uri));
 
                 // does the current state have a parent? if not, it's the root
                 if (parentState != null) {
                     currentState.setParent(parentState);
                 } else {
                     currentState.setRoot(true);
                 }
 
             } else { // we are dealing with the parent of the current state
 
                 FacetStateImpl aState = new FacetStateImpl();
                 aState.setName(label);
                 aState.setParamValue(qNameUtility.getQName(uri));
 
                 if (parentState != null) {
                     aState.setParent(parentState);
                 } else {
                     aState.setRoot(true);
                 }
 
                 parentState = aState;
 
             }
         }
 
     }
 
     protected List<Resource> findChildren(FacetQueryService.Tree<Resource> hierarchy, String currentUri) {
         List<Resource> familyHistory = new ArrayList<Resource>();
         findChildren(familyHistory, hierarchy, currentUri, 0, true);
         return familyHistory;
     }
 
 
     protected boolean findChildren(List<Resource> path, FacetQueryService.Tree<Resource> hierarchy, String currentUri,
                                    int level, boolean follow) {
 
         if (follow) {
 
             // anything on the path greater than the current level
             // is a failed search and can be removed
             if (path.size() > level) {
                 for (int i = level; i <= path.size(); i++) {
                     path.remove(level);
                 }
             }
 
             // add the current node to the path
             path.add(level, hierarchy.getValue());
 
             // have we reached our goal?
             if (hierarchy.getValue().getURI().equals(currentUri)) {
                 return false;
             }
 
             // recursively follow the children ...
             for (FacetQueryService.Tree<Resource> tree : hierarchy.getChildren()) {
                 level++;
                 follow = findChildren(path, tree, currentUri, level, follow);
                 level--;
             }
         }
 
         return follow;
     }
 
 
     protected boolean hasUriParameterValue(FacetEnvironment environment) {
 
         return environment.getParameters().containsKey(environment.getConfig()
                 .get(Facet.PARAM_NAME));
     }
 
     protected String getUriFromParameterValue(FacetEnvironment environment) {
 
         String[] paramStrings = environment.getParameters()
                 .get(environment.getConfig().get(Facet.PARAM_NAME));
         String uri = paramStrings[0];
 
         return qNameUtility.expandQName(uri);
     }
 
     protected List<? extends Constraint> createHierarchicalConstraintList(FacetEnvironment environment) {
 
         // Get the current position from the param state (if any)
         String[] paramVals = environment.getParameters().get(environment.getConfig().get(Facet.PARAM_NAME));
         // If it exists, use it. Otherwise use base value
         String currVal = (paramVals != null && paramVals.length != 0) ?
                 paramVals[0] :
                 environment.getConfig().get(Facet.FACET_BASE);
 
         return createHierarchicalConstraintList(
                 ResourceFactory.createResource(environment.getConfig().get(Facet.CONSTRAINT_TYPE)),
                 ResourceFactory.createProperty(environment.getConfig().get(Facet.LINK_PROPERTY)),
                 ResourceFactory.createResource(currVal)
         );
     }
 
     protected List<? extends Constraint> createHierarchicalConstraintList(
             Resource type, Property link, Resource value) {
 
         ValueConstraint typeConstraint = new ValueConstraint(RDF.type, type);
 
         ValueConstraint linkConstraint = new ValueConstraint(link, value);
 
         return Arrays.asList(typeConstraint, linkConstraint);
     }
 
     protected Resource createBaseResource(FacetEnvironment environment) {
         if (hasUriParameterValue(environment)) {
             String uri = getUriFromParameterValue(environment);
             return ResourceFactory.createResource(uri);
         } else {
             return ResourceFactory.createResource(environment.getConfig()
                     .get(Facet.FACET_BASE));
         }
     }
 
     protected Property createBroaderProperty(FacetEnvironment environment) {
         return ResourceFactory.createProperty(environment.getConfig().get(Facet.BROADER_PROPERTY));
     }
 
 
     private FacetQueryService facetQueryService;
     private QNameUtility qNameUtility;
 }
