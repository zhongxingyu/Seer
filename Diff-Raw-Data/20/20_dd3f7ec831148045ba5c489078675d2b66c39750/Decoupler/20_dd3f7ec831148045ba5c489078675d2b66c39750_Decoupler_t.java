 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.dedup.utils;
 
 import java.io.StringReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 import java.util.List;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 import eu.europeana.corelib.definitions.jibx.AgentType;
 import eu.europeana.corelib.definitions.jibx.Aggregation;
 import eu.europeana.corelib.definitions.jibx.Concept;
 import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
 import eu.europeana.corelib.definitions.jibx.HasView;
 import eu.europeana.corelib.definitions.jibx.PlaceType;
 import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
 import eu.europeana.corelib.definitions.jibx.ProxyType;
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.jibx.TimeSpanType;
 import eu.europeana.corelib.definitions.jibx.WebResourceType;
 import eu.europeana.dedup.osgi.service.exceptions.DeduplicationException;
 
 /**
  * Helper Class that checks if a received EDM record contains
  * 
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @since 27 Sep 2012
  */
 public class Decoupler {
 
 	private static Decoupler INSTANCE;
 
 	/**
 	 * Private constructor, instantiate via getInstance() method
 	 */
 	private Decoupler() {
 	}
 
 	/**
 	 * Static initialiser method
 	 * 
 	 * @return an instance of this class
 	 */
 	public static Decoupler getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new Decoupler();
 		}
 		return INSTANCE;
 	}
 
 	
 	/**
 	 * Method that performs the decoupling on a given EDM xml string. 
 	 *  
 	 * @param edmXML the edm xml
 	 * @return a list of the decoupled RDF jibx objects
 	 * @throws DeduplicationException
 	 */
 	public List<RDF> decouple(String edmXML) throws DeduplicationException {
 
 		if (edmXML == null) {
 			throw new DeduplicationException(
 					"Parameter null passed as an argument in Decoupler.decouple(RDF edmXML) method");
 		}
 
 		IBindingFactory context;
 
 		try {
 
 			context = BindingDirectory.getFactory(RDF.class);
 
 			IUnmarshallingContext uctx = context.createUnmarshallingContext();
 			RDF edmOBJ = (RDF) uctx.unmarshalDocument(new StringReader(edmXML));
 
 			InfoStub stub = new InfoStub(edmOBJ);
 			stub.init();
 
 			if (stub.proxyList.size() == 1) {
 				ArrayList<RDF> list = new ArrayList<RDF>();
 				list.add(edmOBJ);
 				return list;
 			} else {
 				return process(stub);
 			}
 
 		} catch (JiBXException e) {
 
 			throw new DeduplicationException(e);
 		}
 
 	}
 
 	
 	/**
 	 * Populates a given list of RDF resources given a populated 
 	 * "stub" object
 	 * 
 	 * @param stub an object used as information holder for the decoupling operation
 	 * @return a list of RDF resources
 	 */
 	private List<RDF> process(InfoStub stub) {
 		Vector<RDF> edmList = new Vector<RDF>();
 
 		for (ProxyType proxy : stub.proxyList) {
 			RDF cleandoc = new RDF();
 			cleandoc.getProxyList().add(proxy);  
 			appendPrCHOs(proxy, stub, cleandoc);
 			List<Aggregation> aggregations = appendAggregations(proxy, stub,
 					cleandoc);
 			appendWebResources(aggregations, stub, cleandoc);
 			appendContextualEntities(proxy, stub, cleandoc);
 			edmList.add(cleandoc);
 		}
 		return edmList;
 	}
 
 	/**
 	 * Appends the related Aggregations to an RDF document given a specific Proxy object
 	 * 
 	 * @param proxy the proxy object
 	 * @param stub an object used as information holder for the decoupling operation
 	 * @param cleandoc a JIBX representation of a reconstructed EDM document
 	 * @return a copy of the appended Aggregations
 	 */
 	private List<Aggregation> appendAggregations(ProxyType proxy,
 			InfoStub stub, RDF cleandoc) {
 		// Get the Aggregator References
 		List<Aggregation> foundaggregationlist = new ArrayList<Aggregation>();
 
 		Vector<Aggregation> aglist = stub.aggregationList;
 
 		for (Aggregation agg : aglist) {
 			// if the edm:aggregatedCHO property value  of the Aggregation equals the rdf:about
 			// value of the Proxy then append it to the RDF document
 			if (agg.getAggregatedCHO().getResource().equals(proxy.getAbout())) {
 				foundaggregationlist.add(agg);
 				cleandoc.getAggregationList().add(agg);  
 				
 				if (stub.orphanEntities.contains(agg)) {
 					stub.orphanEntities.remove(agg);
 				} else {
 					stub.orphanEntities.add(agg);
 				}
 			}
 		}
 
 		return foundaggregationlist;
 	}
 
 	
 	/**
 	 * Appends the related Aggregations to an RDF document given a list of Aggregations
 	 * 
 	 * @param aggregations the list of Aggregations to inspect
 	 * @param stub  an object used as information holder for the decoupling operation
 	 */
 	private void appendWebResources(List<Aggregation> aggregations,
 			InfoStub stub, RDF cleandoc) {
 		HashSet<String> refstring = new HashSet<String>();
 
 		for (Aggregation agg : aggregations) {
 
 			// First try to isolate any possible resource references to
 			// WebResources by checking all relevant Aggregation fields
 			// (edm:object,edm:isShownBy,edm:isShownAt,edm:hasViewList)
 			// and add these references to a Set
 			if (agg.getObject() != null
 					&& agg.getObject().getResource() != null) {
 				String resource = agg.getObject().getResource();
 				refstring.add(resource);
 			}
 			if (agg.getIsShownBy() != null
 					&& agg.getIsShownBy().getResource() != null) {
 				String resource = agg.getIsShownBy().getResource();
				refstring.add(resource);
			}
			if (agg.getIsShownAt() != null
					&& agg.getIsShownAt().getResource() != null) {
				String resource = agg.getIsShownAt().getResource();
				refstring.add(resource);
 			}
 			if (agg.getHasViewList() != null && !agg.getHasViewList().isEmpty()) {
 				List<HasView> viewlist = agg.getHasViewList();
 
 				for (HasView view : viewlist) {
 						refstring.add(view.getResource());
 				}
 			}
 		}
 
 		//Then for all registered Web resources in the "stub" object
 		//check if their rdf:about is contained in the refstring
 		//set. In case it does then append them to the document
 		Vector<WebResourceType> wrlist = stub.webresourceList;
 
		
 		for (WebResourceType wtype : wrlist) {
 			if (refstring.contains(wtype.getAbout())) {
 				cleandoc.getWebResourceList().add(wtype); 
 			}
 		}
		}




 
 
 	/**
 	 * Appends the related ContextualEntities to an RDF document given a specific Proxy object
 	 * 
 	 * @param proxy the proxy object
 	 * @param stub an object used as information holder for the decoupling operation
 	 * @param cleandoc a JIBX representation of a reconstructed EDM document
 	 */
 	private void appendContextualEntities(ProxyType proxy, InfoStub stub,
 			RDF cleandoc) {
 		HashSet<String> refset = new HashSet<String>();
 
 		//First itearate the dc & dcterms elements of the given Proxy looking for references
 		//to contextual resources. Append these references to the refset HashSet.
 		List<eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice> dclist = proxy
 				.getChoiceList();
 
 		for (eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice choiceitem : dclist) {
 			//if (choiceitem.ifAlternative()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getAlternative()));
 			//}
 			//if (choiceitem.ifConformsTo()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getConformsTo()));
 			//}
 			if (choiceitem.ifContributor()) {
 				refset.add(returnResourceFromClass(choiceitem.getContributor()));
 			}
 			if (choiceitem.ifCoverage()) {
 				refset.add(returnResourceFromClass(choiceitem.getCoverage()));
 			}
 			if (choiceitem.ifCreated()) {
 				refset.add(returnResourceFromClass(choiceitem.getCreated()));
 			}
 			if (choiceitem.ifCreator()) {
 				refset.add(returnResourceFromClass(choiceitem.getCreator()));
 			}
 			if (choiceitem.ifDate()) {
 				refset.add(returnResourceFromClass(choiceitem.getDate()));
 			}
 			//if (choiceitem.ifDescription()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getDescription()));
 			//}
 			//if (choiceitem.ifExtent()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getExtent()));
 			//}
 			//if (choiceitem.ifFormat()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getFormat()));
 			//}
 			//if (choiceitem.ifHasFormat()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getHasFormat()));
 			//}
 			//if (choiceitem.ifHasPart()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getHasPart()));
 			//}
 			//if (choiceitem.ifHasVersion()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getHasVersion()));
 			//}
 			//if (choiceitem.ifIdentifier()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getIdentifier()));
 			//}
 			//if (choiceitem.ifIsFormatOf()) {
 			//	refset.add(returnResourceFromClass(choiceitem.getIsFormatOf()));
 			//}
 //			if (choiceitem.ifIsPartOf()) {
 //				refset.add(returnResourceFromClass(choiceitem.getIsPartOf()));
 //			}
 //			if (choiceitem.ifIsReferencedBy()) {
 //				refset.add(returnResourceFromClass(choiceitem
 //						.getIsReferencedBy()));
 //			}
 //			if (choiceitem.ifIsReplacedBy()) {
 //				refset.add(returnResourceFromClass(choiceitem.getIsReplacedBy()));
 //			}
 //			if (choiceitem.ifIssued()) {
 //				refset.add(returnResourceFromClass(choiceitem.getIssued()));
 //			}
 //			if (choiceitem.ifIsVersionOf()) {
 //				refset.add(returnResourceFromClass(choiceitem.getIsVersionOf()));
 //			}
 //			if (choiceitem.ifLanguage()) {
 //				refset.add(returnResourceFromClass(choiceitem.getLanguage()));
 //			}
 //			if (choiceitem.ifMedium()) {
 //				refset.add(returnResourceFromClass(choiceitem.getMedium()));
 //			}
 			if (choiceitem.ifProvenance()) {
 				refset.add(returnResourceFromClass(choiceitem.getProvenance()));
 			}
 //			if (choiceitem.ifPublisher()) {
 //				refset.add(returnResourceFromClass(choiceitem.getPublisher()));
 //			}
 //			if (choiceitem.ifReferences()) {
 //				refset.add(returnResourceFromClass(choiceitem.getReferences()));
 //			}
 //			if (choiceitem.ifRelation()) {
 //				refset.add(returnResourceFromClass(choiceitem.getRelation()));
 //			}
 //			if (choiceitem.ifReplaces()) {
 //				refset.add(returnResourceFromClass(choiceitem.getReplaces()));
 //			}
 //			if (choiceitem.ifRequires()) {
 //				refset.add(returnResourceFromClass(choiceitem.getRequires()));
 //			}
 //			if (choiceitem.ifRights()) {
 //				refset.add(returnResourceFromClass(choiceitem.getRights()));
 //			}
 			if (choiceitem.ifSpatial()) {
 				refset.add(returnResourceFromClass(choiceitem.getSpatial()));
 			}
 			if (choiceitem.ifSubject()) {
 				refset.add(returnResourceFromClass(choiceitem.getSubject()));
 			}
 			if (choiceitem.ifSource()) {
 				refset.add(returnResourceFromClass(choiceitem.getSource()));
 			}
 //			if (choiceitem.ifTableOfContents()) {
 //				refset.add(returnResourceFromClass(choiceitem
 //						.getTableOfContents()));
 //			}
 			if (choiceitem.ifTemporal()) {
 				refset.add(returnResourceFromClass(choiceitem.getTemporal()));
 			}
 //			if (choiceitem.ifTitle()) {
 //				refset.add(returnResourceFromClass(choiceitem.getTitle()));
 //			}
 //			if (choiceitem.ifType()) {
 //				refset.add(returnResourceFromClass(choiceitem.getType()));
 //			}
 		}
 
 		//Do the same for the remaining EDM elements in the Proxy
 		refset.add(returnResourceFromClass(proxy.getCurrentLocation()));
 
 		//refset.addAll(returnResourceFromList(proxy.getHasTypeList()));
 
 //		refset.addAll(returnResourceFromList(proxy.getIncorporateList()));
 //
 //		refset.addAll(returnResourceFromList(proxy.getIsDerivativeOfList()));
 //
 //		refset.add(returnResourceFromClass(proxy.getIsNextInSequence()));
 //
 //		refset.addAll(returnResourceFromList(proxy.getIsRelatedToList()));
 //
 //		refset.addAll(returnResourceFromList(proxy.getIsRelatedToList()));
 //
 //		refset.add(returnResourceFromClass(proxy.getIsRepresentationOf()));
 //
 //		refset.addAll(returnResourceFromList(proxy.getIsSimilarToList()));
 //
 //		refset.addAll(returnResourceFromList(proxy.getIsSuccessorOfList()));
 //
 //		refset.addAll(returnResourceFromList(proxy.getRealizeList()));
 
 		//Populate the contextualEntities given the references located in refset
 		populateContextualEntities(refset, stub, cleandoc);
 
 	}
 
 	/**
 	 * Appends the related Povided CHOS to an RDF document given a specific Proxy object
 	 * 
 	 * @param proxy the proxy object
 	 * @param stub an object used as information holder for the decoupling operation
 	 * @param cleandoc a JIBX representation of a reconstructed EDM document
 	 */
 	private void appendPrCHOs(ProxyType proxy, InfoStub stub, RDF cleandoc) {
 
 		Vector<ProvidedCHOType> cholist = stub.prchoList;
 
 		String id = proxy.getAbout();
 		for (ProvidedCHOType cho : cholist) {
 			if (id.equals(cho.getAbout())) {
 				cleandoc.getProvidedCHOList().add(cho); 
 			}
 		}
 
 	}
 
 	/**
 	 * Appends the related ContextualEntities to an RDF document given a specific Set of references
 	 * to contextual entities.
 	 * 
 	 * @param refset a set of references to resources
 	 * @param stub an object used as information holder for the decoupling operation
 	 * @param cleandoc a JIBX representation of a reconstructed EDM document
 	 */
 	private void populateContextualEntities(Set<String> refset, InfoStub stub,
 			RDF cleandoc) {
 
 		//Check all contextual entities stored in the "stub" object and for each one of
 		//them check if they are present in the refset.
 		Vector<AgentType> agentlist = stub.agentList;
 		Vector<PlaceType> placelist = stub.placeList;
 		Vector<TimeSpanType> timelist = stub.timeList;
 		Vector<Concept> conceptlist = stub.conceptList;
 
 		for (AgentType agtype : agentlist) {
 			if (refset.contains(agtype.getAbout())) {
 				cleandoc.getAgentList().add(agtype);
 			}
 		}
 
 		for (PlaceType type : placelist) {
 			if (refset.contains(type.getAbout())) {
 				cleandoc.getPlaceList().add(type);   
 			}
 		}
 
 		for (TimeSpanType type : timelist) {
 			if (refset.contains(type.getAbout())) {
 				cleandoc.getTimeSpanList().add(type); 
 			}
 		}
 
 		for (Concept type : conceptlist) {
 			if (refset.contains(type.getAbout())) {
 				cleandoc.getConceptList().add(type);  
 			}
 		}
 	}
 
 	
 	/**
 	 * Invokes the getResource method on a list of objects via reflection
 	 * 
 	 * @param list the list of objects where the operation needs to be applied
 	 * @return
 	 */
 	private <T> List<String> returnResourceFromList(List<T> list) {
 
 		if (list == null)
 			return new ArrayList<String>();
 
 		ArrayList<String> returnList = new ArrayList<String>();
 
 		for (T object : list) {
 			String resource = returnResourceFromClass(object);
 			if (resource != null) {
 				returnList.add(resource);
 			}
 		}
 
 		return returnList;
 	}
 
 	
 	/**
 	 * Invokes the getResource method on an object via reflection
 	 * @param object
 	 * @return
 	 */
 	private <T> String returnResourceFromClass(T object) {
 
 		if (object == null)
 			return null;
 
 		Method[] methods = object.getClass().getMethods();
 
 		for (int i = 0; i < methods.length; i++) {
 
 			if (methods[i].getName().equals("getResource")) {
 
 				try {
 					String resource = (String) methods[i].invoke(object);
 
 					return resource;
 				} catch (IllegalArgumentException e) {
 
 				} catch (IllegalAccessException e) {
 
 				} catch (InvocationTargetException e) {
 
 				}
 			}
 
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Inner Class that creates a "registry" of all the EDM entities contained in the
 	 * current decoupling process.
 	 * 
 	 * @author Georgios Markakis <gwarkx@hotmail.com>
 	 * @since 1 Oct 2012
 	 */
 	private class InfoStub {
 
 		//A Set of orphan entities
 		HashSet<Object> orphanEntities = new HashSet<Object>();
 		//The original 
 		RDF edmXML;
 		Vector<ProvidedCHOType> prchoList = new Vector<ProvidedCHOType>();
 		Vector<ProxyType> proxyList = new Vector<ProxyType>();
 		Vector<Aggregation> aggregationList = new Vector<Aggregation>();
 		Vector<EuropeanaAggregationType> euaggregationList = new Vector<EuropeanaAggregationType>();
 		Vector<AgentType> agentList = new Vector<AgentType>();
 		Vector<Concept> conceptList = new Vector<Concept>();
 		Vector<PlaceType> placeList = new Vector<PlaceType>();
 		Vector<TimeSpanType> timeList = new Vector<TimeSpanType>();
 		Vector<WebResourceType> webresourceList = new Vector<WebResourceType>();
 
 		/**
 		 * Default constructor
 		 * @param edmXML
 		 */
 		public InfoStub(RDF edmXML) {
 			this.edmXML = edmXML;
 		}
 
 		/**
 		 * Initialize the object by appending all elements in the given  
 		 */
 		public void init() {
 			if(edmXML.getProxyList() != null){
 				proxyList.addAll(edmXML.getProxyList());
 			}
 
 			if(edmXML.getAgentList() != null){
 				agentList.addAll(edmXML.getAgentList());
 			}
 			
 			
 			if(edmXML.getAggregationList() != null){
 				aggregationList.addAll(edmXML.getAggregationList());
 			}
 			
 			if(edmXML.getConceptList()!=null){
 				conceptList.addAll(edmXML.getConceptList());
 			}
 			
 			if(edmXML.getEuropeanaAggregationList()!=null){
 				euaggregationList.addAll(edmXML.getEuropeanaAggregationList());
 			}
 			
 			if(edmXML.getPlaceList()!=null){
 				placeList.addAll(edmXML.getPlaceList());
 			}
 				
 			if(edmXML.getProvidedCHOList()!=null){
 				prchoList.addAll(edmXML.getProvidedCHOList());
 			}
 			
 			if(edmXML.getTimeSpanList()!=null){
 				timeList.addAll(edmXML.getTimeSpanList());
 			}
 			
 			if(edmXML.getWebResourceList()!=null){
 				webresourceList.addAll(edmXML.getWebResourceList());
 			}
 
 		}
 
 	}
 }
