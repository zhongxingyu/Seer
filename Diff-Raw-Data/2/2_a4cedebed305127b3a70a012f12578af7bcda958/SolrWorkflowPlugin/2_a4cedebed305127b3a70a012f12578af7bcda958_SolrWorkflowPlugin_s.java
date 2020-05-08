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
 
 package eu.europeana.uim.plugin.solr.service;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.apache.commons.lang.StringUtils;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IMarshallingContext;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 import org.theeuropeanlibrary.model.common.qualifier.Status;
 
 import com.google.code.morphia.Datastore;
 
 import eu.europeana.corelib.definitions.jibx.AgentType;
 import eu.europeana.corelib.definitions.jibx.Concept;
 import eu.europeana.corelib.definitions.jibx.EuropeanaProxy;
 import eu.europeana.corelib.definitions.jibx.LiteralType;
 import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
 import eu.europeana.corelib.definitions.jibx.PlaceType;
 import eu.europeana.corelib.definitions.jibx.ProxyType;
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
 import eu.europeana.corelib.definitions.jibx.ResourceType;
 import eu.europeana.corelib.definitions.jibx.TimeSpanType;
 import eu.europeana.corelib.definitions.jibx.WebResourceType;
 import eu.europeana.corelib.definitions.jibx.Year;
 import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
 import eu.europeana.uim.orchestration.ExecutionContext;
 import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
 import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
 import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
 import eu.europeana.uim.plugin.solr.utils.EuropeanaDateUtils;
 import eu.europeana.uim.plugin.solr.utils.JibxUtils;
 import eu.europeana.uim.plugin.solr.utils.OsgiExtractor;
 import eu.europeana.uim.store.MetaDataRecord;
 
 /**
  * This is the main class implementing the UIM functionality for the solr
  * workflow plugin exposed as an OSGI service.
  * 
  * @author Georgios Markakis
  * @author Yorgos.Mamakis@ kb.nl
  * 
  */
 public class SolrWorkflowPlugin<I> extends
 		AbstractIngestionPlugin<MetaDataRecord<I>, I> {
 
 	private static int recordNumber;
 	private static Map<String, List<ControlledVocabularyImpl>> vocMemCache;
 
 	private static SolrWorkflowService solrWorkflowService;
 	private static Datastore datastore = null;
 	private static IBindingFactory bfact;
 	/**
 	 * The parameters used by this WorkflowStart
 	 */
 	private static final List<String> params = new ArrayList<String>() {
 		private static final long serialVersionUID = 1L;
 
 	};
 
 	/**
 	 * 
 	 */
 	public SolrWorkflowPlugin(SolrWorkflowService solrWorkflowService) {
 		super("solr_workflow", "Solr Repository Ingestion Plugin");
 		SolrWorkflowPlugin.solrWorkflowService = solrWorkflowService;
 
 		try {
 			bfact = BindingDirectory.getFactory(RDF.class);
 			datastore = solrWorkflowService.getDatastore();
 		} catch (JiBXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.IngestionPlugin#processRecord(eu.europeana.uim.
 	 * MetaDataRecord, eu.europeana.uim.api.ExecutionContext)
 	 */
 	@Override
 	public boolean process(MetaDataRecord<I> mdr,
 			ExecutionContext<MetaDataRecord<I>, I> context)
 			throws IngestionPluginFailedException, CorruptedDatasetException {
 		mdr.deleteValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD);
 		if (mdr.getValues(EuropeanaModelRegistry.STATUS).size() == 0
 				|| !mdr.getValues(EuropeanaModelRegistry.STATUS).get(0)
 						.equals(Status.DELETED)) {
 			datastore = solrWorkflowService.getDatastore();
 			try {
 
 				if (vocMemCache == null || vocMemCache.size() > 0) {
 					OsgiExtractor extractor = solrWorkflowService
 							.getExtractor();
 
 					List<ControlledVocabularyImpl> vocs = extractor
 							.getControlledVocabularies(datastore);
 					vocMemCache = new HashMap<String, List<ControlledVocabularyImpl>>();
 					List<ControlledVocabularyImpl> vocsInMap;
 					for (ControlledVocabularyImpl voc : vocs) {
 						if (vocMemCache.containsKey(voc.getURI())) {
 							vocsInMap = vocMemCache.get(voc.getURI());
 						} else {
 							vocsInMap = new ArrayList<ControlledVocabularyImpl>();
 						}
 						vocsInMap.add(voc);
 						vocMemCache.put(voc.getURI(), vocsInMap);
 					}
 				}
 
 				String value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD)
 						.get(0);
 				IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
 				IMarshallingContext marshallingContext = bfact
 						.createMarshallingContext();
 				marshallingContext.setIndent(2);
 				RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
 				RDF rdfCopy = (RDF) uctx.unmarshalDocument(new StringReader(
 						value));
 
 				if (rdf.getAgentList() != null) {
 					for (AgentType agent : rdf.getAgentList()) {
 						dereferenceAgent(rdfCopy, datastore, agent);
 					}
 				}
 				if (rdf.getConceptList() != null) {
 					for (Concept concept : rdf.getConceptList()) {
 						dereferenceConcept(rdfCopy, datastore, concept);
 					}
 				}
 				// for (Aggregation aggregation : rdf.getAggregationList()) {
 				// dereferenceAggregation(rdfCopy, datastore, aggregation);
 				// }
 				// if (rdf.getEuropeanaAggregationList() != null) {
 				// for (EuropeanaAggregationType euaggregation : rdf
 				// .getEuropeanaAggregationList()) {
 				// dereferenceEuropeanaAggregation(rdfCopy, datastore,
 				// euaggregation);
 				// }
 				// }
 				if (rdf.getPlaceList() != null) {
 					for (PlaceType place : rdf.getPlaceList()) {
 						dereferencePlace(rdfCopy, datastore, place);
 					}
 				}
 
 				// for (ProvidedCHOType place : rdf.getProvidedCHOList()) {
 				// dereferenceProvidedCHO(rdfCopy, datastore, place);
 				// }
 				for (ProxyType proxy : rdf.getProxyList()) {
 					if (proxy.getEuropeanaProxy() == null
 							|| !proxy.getEuropeanaProxy().isEuropeanaProxy()) {
 						dereferenceProxy(rdfCopy, datastore, proxy);
 					}
 				}
 
 				if (rdf.getTimeSpanList() != null) {
 					for (TimeSpanType timespan : rdf.getTimeSpanList()) {
 						dereferenceTimespan(rdfCopy, datastore, timespan);
 					}
 				}
 				if (rdf.getWebResourceList() != null) {
 					for (WebResourceType webresource : rdf.getWebResourceList()) {
 						dereferenceWebResource(rdfCopy, datastore, webresource);
 					}
 				}
 
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				RDF rdfFinal = cleanRDF(rdfCopy);
 
 				ProxyType europeanaProxy = null;
 				List<String> years = new ArrayList<String>();
 				for (ProxyType proxy : rdfFinal.getProxyList()) {
 
 					if (proxy.getEuropeanaProxy() == null
 							|| proxy.getEuropeanaProxy().isEuropeanaProxy()) {
 						if (europeanaProxy == null) {
 							europeanaProxy = new ProxyType();
 							EuropeanaProxy prx = new EuropeanaProxy();
 							prx.setEuropeanaProxy(true);
 							europeanaProxy.setEuropeanaProxy(prx);
 							europeanaProxy.setType(proxy.getType());
 						}
 						years.addAll(EuropeanaDateUtils
 								.createEuropeanaYears(proxy));
 					} else {
 						europeanaProxy = proxy;
 					}
 				}
 
 				if (europeanaProxy != null) {
 					List<Year> yearList = new ArrayList<Year>();
 					for (String year : years) {
 						Year yearObj = new Year();
 						Lang lang = new Lang();
 						lang.setLang("eur");
 						yearObj.setLang(lang);
 						yearObj.setString(year);
 						yearList.add(yearObj);
 					}
 					europeanaProxy.setYearList(yearList);
 				}
 
 				for (ProxyType proxy : rdfFinal.getProxyList()) {
 					if (proxy != null && proxy.getEuropeanaProxy() != null
 							&& proxy.getEuropeanaProxy().isEuropeanaProxy()) {
 						rdfFinal.getProxyList().remove(proxy);
 					}
 				}
 				rdfFinal.getProxyList().add(europeanaProxy);
 				marshallingContext
 						.marshalDocument(rdfFinal, "UTF-8", null, out);
 				String der = out.toString("UTF-8");
 				mdr.addValue(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD, der);
 				return true;
 
 			} catch (JiBXException e) {
 				context.getLoggingEngine().logFailed(
 						Level.SEVERE,
 						this,
 						e,
 						"JiBX unmarshalling has failed with the following error: "
 								+ e.getMessage());
 				e.printStackTrace();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return false;
 		}
 		return true;
 	}
 
 	private RDF cleanRDF(RDF rdf) {
 		RDF rdfFinal = new RDF();
 		List<AgentType> agents = new ArrayList<AgentType>();
 		List<TimeSpanType> timespans = new ArrayList<TimeSpanType>();
 		List<PlaceType> places = new ArrayList<PlaceType>();
 		List<Concept> concepts = new ArrayList<Concept>();
 		JibxUtils utils = new JibxUtils();
 
 		if (rdf.getAgentList() != null) {
 
 			agents.addAll(rdf.getAgentList());
 
 			for (int i = 0; i < agents.size() - 1; i++) {
 				AgentType sAgent = agents.get(i);
 				for (int k = i + 1; k < agents.size(); k++) {
 					AgentType fAgent = agents.get(k);
 					if (StringUtils.contains(fAgent.getAbout(),
 							sAgent.getAbout())
 							|| StringUtils.contains(fAgent.getAbout(),
 									sAgent.getAbout())) {
 
 						agents.set(i, utils.mergeAgentFields(fAgent, sAgent));
 						sAgent = agents.get(i);
 						agents.remove(k);
 						k--;
 					}
 				}
 
 			}
 			rdfFinal.setAgentList(agents);
 		}
 		if (rdf.getConceptList() != null) {
 			concepts.addAll(rdf.getConceptList());
 			for (int i = 0; i < concepts.size() - 1; i++) {
 				Concept sConcept = concepts.get(i);
 				for (int k = i + 1; k < concepts.size(); k++) {
 					Concept fConcept = concepts.get(k);
 					if (StringUtils.contains(fConcept.getAbout(),
 							sConcept.getAbout())
 							|| StringUtils.contains(sConcept.getAbout(),
 									fConcept.getAbout())) {
 						concepts.set(i,
 								utils.mergeConceptsField(fConcept, sConcept));
 						sConcept = concepts.get(i);
 						concepts.remove(k);
 						k--;
 					}
 				}
 			}
 
 			rdfFinal.setConceptList(concepts);
 		}
 		if (rdf.getTimeSpanList() != null) {
 			timespans.addAll(rdf.getTimeSpanList());
 			for (int i = 0; i < timespans.size() - 1; i++) {
 				TimeSpanType sTs = timespans.get(i);
 				for (int k = i + 1; k < timespans.size(); k++) {
 					TimeSpanType fTs = timespans.get(k);
 					if (StringUtils.contains(fTs.getAbout(), sTs.getAbout())
 							|| StringUtils.contains(sTs.getAbout(),
 									fTs.getAbout())) {
 						timespans.set(i, utils.mergeTimespanFields(fTs, sTs));
 						sTs = timespans.get(i);
 						timespans.remove(k);
 						k--;
 					}
 				}
 			}
 			rdfFinal.setTimeSpanList(timespans);
 		}
 		if (rdf.getPlaceList() != null) {
 			places.addAll(rdf.getPlaceList());
 
 			for (int i = 0; i < places.size() - 1; i++) {
 				PlaceType sPlace = places.get(i);
 				for (int k = i + 1; k < places.size(); k++) {
 					PlaceType fPlace = places.get(k);
 					if (StringUtils
 							.equals(fPlace.getAbout(), sPlace.getAbout())
 							|| StringUtils.contains(sPlace.getAbout(),
 									fPlace.getAbout())) {
 						if (fPlace.getAbout() != null
 								&& sPlace.getAbout() != null) {
 							places.set(i,
 									utils.mergePlacesFields(fPlace, sPlace));
 						}
 						sPlace = places.get(i);
 						places.remove(k);
 						k--;
 
 					}
 				}
 			}
 
 			rdfFinal.setPlaceList(places);
 		}
 		rdfFinal.setAggregationList(rdf.getAggregationList());
 		rdfFinal.setProxyList(rdf.getProxyList());
 		rdfFinal.setProvidedCHOList(rdf.getProvidedCHOList());
 		rdfFinal.setEuropeanaAggregationList(rdf.getEuropeanaAggregationList());
 		rdfFinal.setWebResourceList(rdf.getWebResourceList());
 
 		return rdfFinal;
 	}
 
 	private void dereferenceWebResource(RDF rdf, Datastore datastore,
 			WebResourceType webResource) throws MalformedURLException,
 			IOException, SecurityException, IllegalArgumentException,
 			InstantiationException, IllegalAccessException,
 			NoSuchMethodException, InvocationTargetException {
 		// derefResourceOrLiteralList(rdf, datastore,
 		// webResource.getConformsToList());
 		derefResourceOrLiteralList(rdf, datastore, webResource.getCreatedList());
 		// derefResourceOrLiteralList(rdf, datastore,
 		// webResource.getDescriptionList());
 		derefResourceOrLiteralList(rdf, datastore, webResource.getExtentList());
 		derefResourceOrLiteralList(rdf, datastore, webResource.getFormatList());
 		derefResourceOrLiteralList(rdf, datastore, webResource.getHasPartList());
 		derefResourceOrLiteralList(rdf, datastore,
 				webResource.getIsFormatOfList());
 		derefResourceOrLiteralList(rdf, datastore, webResource.getIssuedList());
 		// derefResourceOrLiteralList(rdf, datastore,
 		// webResource.getRightList());
 		// derefResourceOrLiteralList(rdf, datastore,
 		// webResource.getSourceList());
 		// derefResourceOrLiteral(rdf, datastore,
 		// webResource.getIsNextInSequence());
 		// derefResourceOrLiteral(rdf, datastore, webResource.getRights());
 		// derefResourceOrLiteral(rdf, datastore, webResource.getAbout());
 	}
 
 	private void dereferenceTimespan(RDF rdf, Datastore datastore,
 			TimeSpanType timeSpan) throws MalformedURLException, IOException,
 			SecurityException, IllegalArgumentException,
 			InstantiationException, IllegalAccessException,
 			NoSuchMethodException, InvocationTargetException {
 		// derefResourceOrLiteralList(rdf, datastore,
 		// timeSpan.getAltLabelList());
 		// derefResourceOrLiteralList(rdf, datastore,
 		// timeSpan.getPrefLabelList());
 		derefResourceOrLiteralList(rdf, datastore, timeSpan.getHasPartList());
 		derefResourceOrLiteralList(rdf, datastore, timeSpan.getIsPartOfList());
 		// derefResourceOrLiteralList(rdf, datastore, timeSpan.getNoteList());
 		derefResourceOrLiteralList(rdf, datastore, timeSpan.getSameAList());
 		derefResourceOrLiteral(rdf, datastore, timeSpan.getAbout());
 		derefResourceOrLiteral(rdf, datastore, timeSpan.getBegin());
 		derefResourceOrLiteral(rdf, datastore, timeSpan.getEnd());
 	}
 
 	private void dereferenceProxy(RDF rdf, Datastore datastore, ProxyType proxy)
 			throws MalformedURLException, IOException, SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 		derefResourceOrLiteralList(rdf, datastore, proxy.getHasMetList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getHasTypeList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getIncorporateList());
 		derefResourceOrLiteralList(rdf, datastore,
 				proxy.getIsDerivativeOfList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getIsRelatedToList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getIsSimilarToList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getIsSuccessorOfList());
 		// derefResourceOrLiteralList(rdf, datastore, proxy.getProxyInList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getRealizeList());
 		// derefResourceOrLiteralList(rdf, datastore, proxy.getUserTagList());
 		derefResourceOrLiteralList(rdf, datastore, proxy.getYearList());
 		// derefResourceOrLiteral(rdf, datastore, proxy.getAbout());
 		derefResourceOrLiteral(rdf, datastore, proxy.getCurrentLocation());
 		// derefResourceOrLiteral(rdf, datastore, proxy.getProxyFor());
 		// derefResourceOrLiteral(rdf, datastore, proxy.getType());
 		List<eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice> choices = proxy
 				.getChoiceList();
 		if (choices != null) {
 			for (eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice choice : choices) {
 				// if (choice.ifAlternative())
 				// derefResourceOrLiteral(rdf, datastore,
 				// choice.getAlternative());
 				// if (choice.ifConformsTo())
 				// derefResourceOrLiteral(rdf, datastore,
 				// choice.getConformsTo());
 				if (choice.ifContributor())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getContributor());
 				if (choice.ifCoverage())
 					derefResourceOrLiteral(rdf, datastore, choice.getCoverage());
 				if (choice.ifCreated())
 					derefResourceOrLiteral(rdf, datastore, choice.getCreated());
 				if (choice.ifCreator())
 					derefResourceOrLiteral(rdf, datastore, choice.getCreator());
 				if (choice.ifDate())
 					derefResourceOrLiteral(rdf, datastore, choice.getDate());
 				// if (choice.ifDescription())
 				// derefResourceOrLiteral(rdf, datastore,
 				// choice.getDescription());
 				if (choice.ifExtent())
 					derefResourceOrLiteral(rdf, datastore, choice.getExtent());
 				if (choice.ifFormat())
 					derefResourceOrLiteral(rdf, datastore, choice.getFormat());
 				if (choice.ifHasFormat())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getHasFormat());
 				if (choice.ifHasPart())
 					derefResourceOrLiteral(rdf, datastore, choice.getHasPart());
 				if (choice.ifHasVersion())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getHasVersion());
 				if (choice.ifIdentifier())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getIdentifier());
 				if (choice.ifIsFormatOf())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getIsFormatOf());
 				if (choice.ifIsPartOf())
 					derefResourceOrLiteral(rdf, datastore, choice.getIsPartOf());
 				if (choice.ifIsReferencedBy())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getIsReferencedBy());
 				if (choice.ifIsReplacedBy())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getIsReplacedBy());
 				if (choice.ifIsRequiredBy())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getIsRequiredBy());
 				if (choice.ifIssued())
 					derefResourceOrLiteral(rdf, datastore, choice.getIssued());
 				if (choice.ifIsVersionOf())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getIsVersionOf());
 				if (choice.ifLanguage())
 					derefResourceOrLiteral(rdf, datastore, choice.getLanguage());
 				if (choice.ifMedium())
 					derefResourceOrLiteral(rdf, datastore, choice.getMedium());
 				// if (choice.ifProvenance())
 				// derefResourceOrLiteral(rdf, datastore,
 				// choice.getProvenance());
 				if (choice.ifPublisher())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getPublisher());
 				if (choice.ifReferences())
 					derefResourceOrLiteral(rdf, datastore,
 							choice.getReferences());
 				if (choice.ifRelation())
 					derefResourceOrLiteral(rdf, datastore, choice.getRelation());
 				if (choice.ifReplaces())
 					derefResourceOrLiteral(rdf, datastore, choice.getReplaces());
 				// if (choice.ifRights())
 				// derefResourceOrLiteral(rdf, datastore, choice.getRights());
 				if (choice.ifSource())
 					derefResourceOrLiteral(rdf, datastore, choice.getSource());
 				if (choice.ifSpatial())
 					derefResourceOrLiteral(rdf, datastore, choice.getSpatial());
 				if (choice.ifSubject())
 					derefResourceOrLiteral(rdf, datastore, choice.getSubject());
 				// if (choice.ifTableOfContents())
 				// derefResourceOrLiteral(rdf, datastore,
 				// choice.getTableOfContents());
 				if (choice.ifTemporal())
 					derefResourceOrLiteral(rdf, datastore, choice.getTemporal());
 				if (choice.ifTitle())
 					derefResourceOrLiteral(rdf, datastore, choice.getTitle());
 				if (choice.ifType())
 					derefResourceOrLiteral(rdf, datastore, choice.getType());
 			}
 		}
 	}
 
 	// private void dereferenceProvidedCHO(RDF rdf, Datastore datastore,
 	// ProvidedCHOType providedCHO) throws MalformedURLException,
 	// IOException, SecurityException, IllegalArgumentException,
 	// InstantiationException, IllegalAccessException,
 	// NoSuchMethodException, InvocationTargetException {
 	// derefResourceOrLiteral(rdf, datastore, providedCHO.getAbout());
 	// derefResourceOrLiteralList(rdf, datastore, providedCHO.getSameAList());
 	// }
 
 	private void dereferencePlace(RDF rdf, Datastore datastore, PlaceType place)
 			throws MalformedURLException, IOException, SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 		derefResourceOrLiteral(rdf, datastore, place.getAbout());
 		// derefResourceOrLiteral(rdf, datastore, place.getAlt());
 		// derefResourceOrLiteral(rdf, datastore, place.getLat());
 		// derefResourceOrLiteral(rdf, datastore, place.getLong());
 		// derefResourceOrLiteralList(rdf, datastore, place.getAltLabelList());
 		// derefResourceOrLiteralList(rdf, datastore, place.getPrefLabelList());
 		derefResourceOrLiteralList(rdf, datastore, place.getIsPartOfList());
 		// derefResourceOrLiteralList(rdf, datastore, place.getNoteList());
 		derefResourceOrLiteralList(rdf, datastore, place.getSameAList());
 		derefResourceOrLiteralList(rdf, datastore, place.getHasPartList());
 	}
 
 	private void dereferenceConcept(RDF rdf, Datastore datastore,
 			Concept concept) throws MalformedURLException, IOException,
 			SecurityException, IllegalArgumentException,
 			InstantiationException, IllegalAccessException,
 			NoSuchMethodException, InvocationTargetException {
 		derefResourceOrLiteral(rdf, datastore, concept.getAbout());
 		for (eu.europeana.corelib.definitions.jibx.Concept.Choice choice : concept
 				.getChoiceList()) {
 			// if (choice.ifAltLabel())
 			// derefResourceOrLiteral(rdf, datastore, choice.getAltLabel());
 			// if (choice.ifPrefLabel())
 			// derefResourceOrLiteral(rdf, datastore, choice.getPrefLabel());
 			if (choice.ifBroader())
 				derefResourceOrLiteral(rdf, datastore, choice.getBroader());
 			if (choice.ifBroadMatch())
 				derefResourceOrLiteral(rdf, datastore, choice.getBroadMatch());
 			if (choice.ifCloseMatch())
 				derefResourceOrLiteral(rdf, datastore, choice.getCloseMatch());
 			if (choice.ifExactMatch())
 				derefResourceOrLiteral(rdf, datastore, choice.getExactMatch());
 			if (choice.ifNarrower())
 				derefResourceOrLiteral(rdf, datastore, choice.getNarrower());
 			if (choice.ifNarrowMatch())
 				derefResourceOrLiteral(rdf, datastore, choice.getNarrowMatch());
 			// if (choice.ifNote())
 			// derefResourceOrLiteral(rdf, datastore, choice.getNote());
 			// if (choice.ifNotation())
 			// derefResourceOrLiteral(rdf, datastore, choice.getNotation());
 			if (choice.ifRelated())
 				derefResourceOrLiteral(rdf, datastore, choice.getRelated());
 			if (choice.ifRelatedMatch())
 				derefResourceOrLiteral(rdf, datastore, choice.getRelatedMatch());
 		}
 	}
 
 	// private void dereferenceEuropeanaAggregation(RDF rdf, Datastore
 	// datastore,
 	// EuropeanaAggregationType aggregation) throws MalformedURLException,
 	// IOException, SecurityException, IllegalArgumentException,
 	// InstantiationException, IllegalAccessException,
 	// NoSuchMethodException, InvocationTargetException {
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getAbout());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getAggregatedCHO());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getCountry());
 	// derefResourceOrLiteralList(rdf, datastore, aggregation.getHasViewList());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getCreator());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getIsShownBy());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getLandingPage());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getLanguage());
 	// derefResourceOrLiteral(rdf, datastore, aggregation.getRights());
 	// derefResourceOrLiteralList(rdf, datastore,
 	// aggregation.getAggregateList());
 	// }
 
 	// private void dereferenceAggregation(RDF rdf, Datastore datastore,
 	// Aggregation aggregation) throws MalformedURLException, IOException,
 	// SecurityException, IllegalArgumentException,
 	// InstantiationException, IllegalAccessException,
 	// NoSuchMethodException, InvocationTargetException {
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getAbout());
 	// // derefResourceOrLiteral(rdf, datastore,
 	// aggregation.getAggregatedCHO());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getDataProvider());
 	// // derefResourceOrLiteralList(rdf, datastore,
 	// aggregation.getHasViewList());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getIsShownAt());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getIsShownBy());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getObject());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getProvider());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getRights());
 	// // derefResourceOrLiteral(rdf, datastore, aggregation.getUgc());
 	// derefResourceOrLiteralList(rdf, datastore, aggregation.getRightList());
 	// }
 
 	private void dereferenceAgent(RDF rdf, Datastore datastore, AgentType agent)
 			throws MalformedURLException, IOException, SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 		derefResourceOrLiteral(rdf, datastore, agent.getAbout());
 		// derefResourceOrLiteralList(rdf, datastore, agent.getAltLabelList());
 		// derefResourceOrLiteralList(rdf, datastore, agent.getDateList());
 		derefResourceOrLiteralList(rdf, datastore, agent.getHasMetList());
 		// derefResourceOrLiteralList(rdf, datastore,
 		// agent.getIdentifierList());
 		derefResourceOrLiteralList(rdf, datastore, agent.getIsRelatedToList());
 		// derefResourceOrLiteralList(rdf, datastore, agent.getNameList());
 		// derefResourceOrLiteralList(rdf, datastore, agent.getNoteList());
 		// derefResourceOrLiteralList(rdf, datastore, agent.getPrefLabelList());
 		// derefResourceOrLiteralList(rdf, datastore, agent.getSameAList());
 		// derefResourceOrLiteral(rdf, datastore, agent.getBegin());
 		// derefResourceOrLiteral(rdf, datastore, agent.getEnd());
 		// derefResourceOrLiteral(rdf, datastore,
 		// agent.getBiographicalInformation());
 		// derefResourceOrLiteral(rdf, datastore, agent.getDateOfBirth());
 		// derefResourceOrLiteral(rdf, datastore, agent.getDateOfDeath());
 		// derefResourceOrLiteral(rdf, datastore,
 		// agent.getDateOfEstablishment());
 		// derefResourceOrLiteral(rdf, datastore, agent.getDateOfTermination());
 		// derefResourceOrLiteral(rdf, datastore, agent.getGender());
 	}
 
 	private void derefResourceOrLiteralList(RDF rdf, Datastore datastore,
 			List<?> list) throws MalformedURLException, IOException,
 			SecurityException, IllegalArgumentException,
 			InstantiationException, IllegalAccessException,
 			NoSuchMethodException, InvocationTargetException {
 		if (list != null) {
 			for (Object object : list) {
 				derefResourceOrLiteral(rdf, datastore, object);
 			}
 		}
 	}
 
	private void derefResourceOrLiteral(RDF rdf, Datastore datastore,
 			Object object) throws MalformedURLException, IOException,
 			SecurityException, IllegalArgumentException,
 			InstantiationException, IllegalAccessException,
 			NoSuchMethodException, InvocationTargetException {
 		OsgiExtractor extractor = solrWorkflowService.getExtractor();
 		extractor.setDatastore(solrWorkflowService.getDatastore());
 		if (object instanceof String) {
 
 			if (isURI((String) object)) {
 
 				ControlledVocabularyImpl controlVocabulary = getControlledVocabulary((String) object);
 				if (controlVocabulary != null) {
 
 					appendInRDF(rdf, extractor.denormalize((String) object,
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
 
 				if (isURI(((ResourceOrLiteralType) object).getResource())) {
 					ControlledVocabularyImpl controlVocabulary = getControlledVocabulary(((ResourceOrLiteralType) object)
 							.getResource());
 					if (controlVocabulary != null) {
 						appendInRDF(rdf, extractor.denormalize(
 								((ResourceOrLiteralType) object).getResource(),
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
 
 	private synchronized ControlledVocabularyImpl getControlledVocabulary(
 			String str) {
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
 								return vocabulary;
 							}
 						}
 
 					}
 				}
 			}
 
 		}
 		return null;
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
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void appendInRDF(RDF rdf, Map<String, List> denormalize) {
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.Plugin#initialize()
 	 */
 	@Override
 	public void initialize() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.Plugin#shutdown()
 	 */
 	@Override
 	public void shutdown() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.ExecutionPlugin#completed(eu.europeana.uim.
 	 * orchestration.ExecutionContext)
 	 */
 	@Override
 	public void completed(ExecutionContext<MetaDataRecord<I>, I> context)
 			throws IngestionPluginFailedException {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.ExecutionPlugin#initialize(eu.europeana.uim.
 	 * orchestration.ExecutionContext)
 	 */
 	@Override
 	public void initialize(ExecutionContext<MetaDataRecord<I>, I> context)
 			throws IngestionPluginFailedException {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.Plugin#getPreferredThreadCount()
 	 */
 	@Override
 	public int getPreferredThreadCount() {
 		return 12;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.Plugin#getMaximumThreadCount()
 	 */
 	@Override
 	public int getMaximumThreadCount() {
 		return 15;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#getInputFields()
 	 */
 	@Override
 	public TKey<?, ?>[] getInputFields() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.plugin.ingestion.IngestionPlugin#getOptionalFields()
 	 */
 	@Override
 	public TKey<?, ?>[] getOptionalFields() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#getOutputFields()
 	 */
 	@Override
 	public TKey<?, ?>[] getOutputFields() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.plugin.Plugin#getParameters()
 	 */
 	@Override
 	public List<String> getParameters() {
 		return params;
 	}
 
 	public static int getRecords() {
 		return recordNumber;
 	}
 
 	private static boolean isURI(String uri) {
 
 		try {
 			new URL(uri);
 			return true;
 		} catch (MalformedURLException e) {
 			return false;
 		}
 
 	}
 
 }
