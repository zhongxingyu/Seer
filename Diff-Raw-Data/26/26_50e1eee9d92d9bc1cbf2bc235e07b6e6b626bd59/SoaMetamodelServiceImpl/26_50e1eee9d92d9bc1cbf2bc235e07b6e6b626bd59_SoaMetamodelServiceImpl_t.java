 package org.easysoa.registry;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.inheritance.ChildrenModelSelector;
 import org.easysoa.registry.inheritance.InheritedFacetDescriptor;
 import org.easysoa.registry.inheritance.InheritedFacetDescriptor.TransferLogic;
 import org.easysoa.registry.inheritance.InheritedFacetModelSelector;
 import org.easysoa.registry.inheritance.UuidInSourceSelector;
 import org.easysoa.registry.inheritance.UuidInTargetSelector;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.schema.SchemaManager;
 import org.nuxeo.ecm.core.schema.types.CompositeType;
 import org.nuxeo.ecm.core.schema.types.Field;
 import org.nuxeo.ecm.core.schema.types.Schema;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.ComponentInstance;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
 import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.Pair;
 
 
 /**
  * 
  * @author mkalam-alami
  *
  */
 public class SoaMetamodelServiceImpl extends DefaultComponent implements SoaMetamodelService {
 
     private static Logger logger = Logger.getLogger(SoaMetamodelServiceImpl.class);
     
     private Graph<String, String> graph = new DirectedSparseGraph<String, String>();
    
     private UnweightedShortestPath<String, String> shortestPath
     	= new UnweightedShortestPath<String, String>(graph);
     
     private Map<String, InheritedFacetDescriptor> inheritedFacets
     	= new HashMap<String, InheritedFacetDescriptor>();
 
 	private Map<String, InheritedFacetModelSelector> facetTransferSelectors
 		= new HashMap<String, InheritedFacetModelSelector>();
     
 	public SoaMetamodelServiceImpl() {
 		facetTransferSelectors.put(ChildrenModelSelector.NAME, new ChildrenModelSelector());
 		facetTransferSelectors.put(UuidInSourceSelector.NAME, new UuidInSourceSelector());
 		facetTransferSelectors.put(UuidInTargetSelector.NAME, new UuidInTargetSelector());
 	}
 	
     @Override
     public void registerContribution(Object contribution, String extensionPoint,
             ComponentInstance contributor) throws Exception {
         if (EXTENSIONPOINT_TYPES.equals(extensionPoint)) {
             SoaNodeTypeDescriptor descriptor = (SoaNodeTypeDescriptor) contribution;
             graph.addVertex(descriptor.name);
             for (String subtype : descriptor.subtypes) {
                 graph.addVertex(subtype);
                 graph.addEdge(descriptor.name + " contains " + subtype, descriptor.name, subtype);
             }
         }
         else if (EXTENSIONPOINT_INHERITEDFACETS.equals(extensionPoint)) {
         	InheritedFacetDescriptor descriptor = (InheritedFacetDescriptor) contribution;
         	if (descriptor.facetName != null && !descriptor.transferLogicList.isEmpty()) {
         		inheritedFacets.put(descriptor.facetName, descriptor);
         	}
         	else {
         		logger.error("Invalid " + EXTENSIONPOINT_INHERITEDFACETS + " contribution");
         	}
         }
     }
     
     public Collection<String> getChildren(String type) {
         return graph.getSuccessors(type);
     }
 
     public List<String> getPath(String fromType, String toType) {
         return getPath(graph, shortestPath, fromType, toType);
     }
 
     // Modified version of ShortestPathUtils.getPath()
     private static <V, E> List<V> getPath(Graph<V, E> graph, ShortestPath<V, E> sp, V source, V target) {
         LinkedList<V> path = new LinkedList<V>();
         Map<V, E> incomingEdges = sp.getIncomingEdgeMap(source);
 
         if (incomingEdges.isEmpty() || incomingEdges.get(target) == null) {
             return null;
         }
         V current = target;
         while (!current.equals(source)) {
             E incoming = incomingEdges.get(current);
             path.addFirst(current);
             Pair<V> endpoints = graph.getEndpoints(incoming);
             if (endpoints.getFirst().equals(current)) {
                 current = endpoints.getSecond();
             } else {
                 current = endpoints.getFirst();
             }
         }
 
         return path;
     }
     
     public Set<String> getInheritedFacets(Set<String> facetsToTest) {
     	Set<String> facetsSubset = new HashSet<String>(inheritedFacets.keySet());
     	facetsSubset.retainAll(facetsToTest);
     	return facetsSubset;
     }
 
 	@Override
 	public void applyFacetInheritance(CoreSession documentManager,
 			DocumentModel model, boolean isFacetSource) throws Exception {
 		SchemaManager schemaManager = Framework.getService(SchemaManager.class);
 		for (String facet : getInheritedFacets(model.getFacets())) {
 			InheritedFacetDescriptor inheritance = inheritedFacets.get(facet);
 			for (TransferLogic transferLogic : inheritance.transferLogicList) {
 				if (isFacetSource && model.getType().equals(transferLogic.from)) {
 					DocumentModelList modelsToWriteOn = facetTransferSelectors 
 							.get(transferLogic.selectorType)
 							.findTargets(documentManager, model, transferLogic.to, transferLogic.parameters);
 					
 					if (modelsToWriteOn != null) {
 						for (DocumentModel modelToWriteOn : modelsToWriteOn) {
 							CompositeType facetToCopy = schemaManager.getFacet(facet);
 							for (String schemaToCopy : facetToCopy.getSchemaNames()) {
 								modelToWriteOn.setProperties(schemaToCopy,
 										model.getProperties(schemaToCopy));
 								documentManager.saveDocument(modelToWriteOn);
 							}
 						}
 					}
 				}
 				if (!isFacetSource && model.getType().equals(transferLogic.to)) {
 					DocumentModel modelToReadFrom = facetTransferSelectors 
 							.get(transferLogic.selectorType)
 							.findSource(documentManager, model, transferLogic.from, transferLogic.parameters);
 
 					if (modelToReadFrom != null) {
 						CompositeType facetToCopy = schemaManager.getFacet(facet);
 						for (String schemaToCopy : facetToCopy.getSchemaNames()) {
 							model.setProperties(schemaToCopy,
 									modelToReadFrom.getProperties(schemaToCopy));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void resetInheritedFacets(DocumentModel model) throws Exception {
 		SchemaManager schemaManager = Framework.getService(SchemaManager.class);
 		for (String inheritedFacet : getInheritedFacets(model.getFacets())) {
 			CompositeType facetToReset = schemaManager.getFacet(inheritedFacet);
			InheritedFacetDescriptor inheritedFacetDesc = inheritedFacets.get(inheritedFacet);
			
			boolean isFacetInherited = false;
			for (TransferLogic transferLogic : inheritedFacetDesc.transferLogicList) {
				if (model.getType().equals(transferLogic.to)) {
					isFacetInherited = true;
					break;
				}
			}
			
			if (isFacetInherited) {
				for (Schema schemaToReset : facetToReset.getSchemas()) {
					for (Field fieldToReset : schemaToReset.getFields()) {
						model.setPropertyValue(fieldToReset.getName().toString(), null);
					}
 				}
 			}
 		}
 	}
 
 }
