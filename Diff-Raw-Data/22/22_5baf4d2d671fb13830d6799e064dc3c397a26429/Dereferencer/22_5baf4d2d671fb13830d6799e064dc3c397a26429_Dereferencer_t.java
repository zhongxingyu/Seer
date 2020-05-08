 package eu.europeana.uim.plugin.solr.helpers;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowService;
 import eu.europeana.uim.plugin.solr.utils.OsgiExtractor;
 import eu.europeana.uim.plugin.solr.utils.VocMemCache;
 
 import eu.europeana.corelib.definitions.jibx.AgentType;
 import eu.europeana.corelib.definitions.jibx.Concept;
 import eu.europeana.corelib.definitions.jibx.LiteralType;
 import eu.europeana.corelib.definitions.jibx.PlaceType;
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
 import eu.europeana.corelib.definitions.jibx.ResourceType;
 import eu.europeana.corelib.definitions.jibx.TimeSpanType;
 import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
 
 public abstract class Dereferencer {
 
 	private SolrWorkflowService solrWorkflowService;
 	private Map<String, List<ControlledVocabularyImpl>> vocMemCache;
 
 	public Dereferencer(SolrWorkflowService solrWorkflowService) {
 		vocMemCache = VocMemCache.getMemCache(solrWorkflowService);
 		this.solrWorkflowService = solrWorkflowService;
 	}
 
 	private List<ControlledVocabularyImpl> getReplaceUri(String vocabularyUri) {
 		for (Entry<String, List<ControlledVocabularyImpl>> entries : vocMemCache
 				.entrySet()) {
 			for (ControlledVocabularyImpl voc : entries.getValue()) {
 				if (StringUtils.contains(vocabularyUri, voc.getReplaceUrl())) {
 					return entries.getValue();
 				}
 			}
 		}
 		return null;
 	}
 
 	private boolean hasReplaceUri(String vocabularyUri) {
 		for (Entry<String, List<ControlledVocabularyImpl>> entries : vocMemCache
 				.entrySet()) {
 			for (ControlledVocabularyImpl voc : entries.getValue()) {
 				if (StringUtils.contains(vocabularyUri, voc.getReplaceUrl())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private ControlledVocabularyImpl getControlledVocabulary(String str) {
 		String[] splitName = str.split("/");
 		if (splitName.length > 3) {
 			String vocabularyUri = splitName[0] + "/" + splitName[1] + "/"
 					+ splitName[2] + "/";
 			if (vocMemCache.containsKey(vocabularyUri)
 					|| hasReplaceUri(vocabularyUri)) {
 				List<ControlledVocabularyImpl> vocabularies = vocMemCache
 						.get(vocabularyUri) != null ? vocMemCache
 						.get(vocabularyUri) : getReplaceUri(vocabularyUri);
 				if (vocabularies != null) {
 					for (ControlledVocabularyImpl vocabulary : vocabularies) {
 						for (String rule : vocabulary.getRules()) {
 					
 							if (StringUtils.equals(rule, "*")
 									|| StringUtils.contains(str, rule)) {
 								System.out.println("found vocabulary:" + vocabulary.getName());
 								return vocabulary;
 							}
 						}
 
 					}
 				}
 			}
 
 		}
 		return null;
 	}
 
 	protected void derefResourceOrLiteralList(RDF rdf, List<?> list)
 			throws MalformedURLException, IOException, SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 		if (list != null) {
 			for (Object object : list) {
 				derefResourceOrLiteral(rdf, object);
 			}
 		}
 	}
 
 	protected void derefResourceOrLiteral(RDF rdf, Object object)
 			throws MalformedURLException, IOException, SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 		OsgiExtractor extractor = solrWorkflowService.getExtractor();
 		extractor.setDatastore(solrWorkflowService.getDatastore());
 		
 		if (object instanceof String) {
 
 			if (isURI((String) object)) {
 
 				ControlledVocabularyImpl controlVocabulary = getControlledVocabulary((String) object);
 				if (controlVocabulary != null) {
//					String res = (String) object;
//					if(controlVocabulary.getReplaceUrl()!=null){
//						res  = StringUtils.replace(res, controlVocabulary.getURI(), controlVocabulary.getReplaceUrl());
//					}
					appendInRDF(rdf, extractor.denormalize((String)object,
 							controlVocabulary, 0, true));
 
 				}
 			}
 		} else if (object instanceof ResourceType) {
 
 			if (((ResourceType) object).getResource() != null) {
 
 				if (isURI(((ResourceType) object).getResource())) {
 					ControlledVocabularyImpl controlVocabulary = getControlledVocabulary(((ResourceType) object)
 							.getResource());
 					if (controlVocabulary != null) {
 						appendInRDF(rdf, extractor.denormalize(
 								((ResourceType) object).getResource(),
 								controlVocabulary, 0, true));
 					}
 				}
 			}
 		} else if (object instanceof ResourceOrLiteralType) {
 			if (((ResourceOrLiteralType) object).getResource() != null) {
 
 				if (isURI(((ResourceOrLiteralType) object).getResource().getResource())) {
 					ControlledVocabularyImpl controlVocabulary = getControlledVocabulary(((ResourceOrLiteralType) object)
 							.getResource().getResource());
 					if (controlVocabulary != null) {
 						appendInRDF(rdf, extractor.denormalize(
 								((ResourceOrLiteralType) object).getResource().getResource(),
 								controlVocabulary, 0, true));
 
 					}
 				}
 			}
 			if (((ResourceOrLiteralType) object).getString() != null) {
 
 				if (isURI(((ResourceOrLiteralType) object).getString())) {
 					ControlledVocabularyImpl controlVocabulary = getControlledVocabulary(((ResourceOrLiteralType) object)
 							.getString());
 					if (controlVocabulary != null) {
 						appendInRDF(rdf, extractor.denormalize(
 								((ResourceOrLiteralType) object).getString(),
 								controlVocabulary, 0, true));
 
 					}
 				}
 			}
 		} else if (object instanceof LiteralType) {
 			if (((LiteralType) object).getString() != null) {
 
 				if (isURI(((LiteralType) object).getString())) {
 					ControlledVocabularyImpl controlVocabulary = getControlledVocabulary(((LiteralType) object)
 							.getString());
 					if (controlVocabulary != null) {
 						appendInRDF(rdf, extractor.denormalize(
 								((LiteralType) object).getString(),
 								controlVocabulary, 0, true));
 					}
 
 				}
 			}
 		}
 
 	}
 
 	private boolean isURI(String uri) {
 
 		try {
 			new URL(uri);
 			return true;
 		} catch (MalformedURLException e) {
 			return false;
 		}
 
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void appendInRDF(RDF rdf, Map<String, List> denormalize) {
 		if(denormalize!=null){
 		for (Entry<String, List> entry : denormalize.entrySet()) {
 			if (StringUtils.equals(entry.getKey(), "concepts")) {
 				for (Concept concept : (List<Concept>) entry.getValue()) {
 					if (rdf.getConceptList() != null) {
 						rdf.getConceptList().add(concept);
 					} else {
 						List<Concept> concepts = new ArrayList<Concept>();
 						concepts.add(concept);
 						rdf.setConceptList(concepts);
 					}
 				}
 			}
 			if (StringUtils.equals(entry.getKey(), "agents")) {
 				for (AgentType agent : (List<AgentType>) entry.getValue()) {
 					if (rdf.getAgentList() != null) {
 						rdf.getAgentList().add(agent);
 					} else {
 						List<AgentType> agents = new ArrayList<AgentType>();
 						agents.add(agent);
 						rdf.setAgentList(agents);
 					}
 				}
 			}
 			if (StringUtils.equals(entry.getKey(), "timespans")) {
 				for (TimeSpanType timespan : (List<TimeSpanType>) entry
 						.getValue()) {
 					if (rdf.getTimeSpanList() != null) {
 						rdf.getTimeSpanList().add(timespan);
 					} else {
 						List<TimeSpanType> timespans = new ArrayList<TimeSpanType>();
 						timespans.add(timespan);
 						rdf.setTimeSpanList(timespans);
 					}
 				}
 			}
 			if (StringUtils.equals(entry.getKey(), "places")) {
 				for (PlaceType place : (List<PlaceType>) entry.getValue()) {
 					if (rdf.getPlaceList() != null) {
 						rdf.getPlaceList().add(place);
 					} else {
 						List<PlaceType> places = new ArrayList<PlaceType>();
 						places.add(place);
 						rdf.setPlaceList(places);
 					}
 				}
 			}
 		}
 		}
 	}
 
 	public abstract <T> void dereference(RDF rdf, T t) throws MalformedURLException, SecurityException, IllegalArgumentException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;
 }
