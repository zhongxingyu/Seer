 package eu.europeana.uim.plugin.solr.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.ctc.wstx.stax.WstxInputFactory;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFReaderF;
 import com.mongodb.MongoException;
 
 import eu.europeana.corelib.definitions.jibx.AgentType;
 import eu.europeana.corelib.definitions.jibx.Alt;
 import eu.europeana.corelib.definitions.jibx.Concept;
 import eu.europeana.corelib.definitions.jibx.Lat;
 import eu.europeana.corelib.definitions.jibx.LiteralType;
 import eu.europeana.corelib.definitions.jibx.PlaceType;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
 import eu.europeana.corelib.definitions.jibx.ResourceType;
 import eu.europeana.corelib.definitions.jibx.TimeSpanType;
 import eu.europeana.corelib.definitions.jibx._Long;
 import eu.europeana.corelib.definitions.model.EdmLabel;
 import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
 import eu.europeana.corelib.dereference.impl.EdmMappedField;
 import eu.europeana.corelib.dereference.impl.EntityImpl;
 import eu.europeana.corelib.dereference.impl.Extractor;
 
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowService;
 
 public class OsgiExtractor extends Extractor {
 
 	private static MemCache memCache;
 	private Datastore datastore;
 	private final static long UPDATETIMESTAMP = 5184000000l;
 
 	public static SolrWorkflowService solrWorkFlowService;
 
 	public OsgiExtractor() {
 
 	}
 
 	public OsgiExtractor(SolrWorkflowService solrWorkflowService) {
 		memCache = MemCache.getInstance();
 
 		OsgiExtractor.solrWorkFlowService = solrWorkflowService;
 	}
 
 	public List<EdmMappedField> getEdmLabel(String field) {
 
 		if (vocabulary != null) {
 
 			if (vocabulary.getElements().get(field) != null) {
 				return vocabulary.getElements().get(field);
 			}
 		}
 		return new ArrayList<EdmMappedField>();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public Map<String, List> denormalize(String resource,
 			ControlledVocabularyImpl controlledVocabulary, int iterations,
 			boolean iterFromVocabulary) throws SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 
 		if (controlledVocabulary != null) {
 			vocabulary = controlledVocabulary;
 			String suffix = controlledVocabulary.getSuffix() != null ? controlledVocabulary
 					.getSuffix() : "";
 			// int iters = iterFromVocabulary ? controlledVocabulary
 			// .getIterations() : iterations;
 			if (resource + suffix != null) {
				String fullUri = resource + suffix;
 				if (!fullUri.contains("/.")) {
 					Map<String, List> retMap = retrieveMapFromCache(fullUri);
 					if (retMap != null) {
 						return retMap;
 					}
 					EntityImpl entity = retrieveValueFromResource(fullUri);
 
 					if (entity != null && entity.getContent().length() > 0) {
 						String ref = resource;
						if (controlledVocabulary.getReplaceUrl() != null) {
 							ref = StringUtils.replace(resource,
 									controlledVocabulary.getURI(),
 									controlledVocabulary.getReplaceUrl());
 						}
 						Map<String, List> entityCache = createDereferencingMapRDF(
 								ref, entity.getContent(), iterations);
 						synchronized (memCache) {
 							memCache.getEntityCache().put(entity.getUri(),
 									entityCache);
 						}
 
 						return entityCache;
 					}
 				}
 			}
 		}
 
 		return new HashMap<String, List>();
 	}
 
 	private Map<String, List> retrieveMapFromCache(String fullUri) {
 		return memCache.getEntityCache().containsKey(fullUri) ? memCache
 				.getEntityCache().get(fullUri) : null;
 	}
 
 	private Map<String, List> createDereferencingMapRDF(String resource,
 			String xmlString, int iterations) {
 
 		String SPARQL_TEMPLATE = "%s SELECT ?predicate ?object WHERE {?res ?predicate ?object . FILTER(?res=<%s>||?res=<%s>||?res=<%s>)}";
 		String ROOT_TEMPLATE = "%s SELECT ?object WHERE {?res rdf:type ?object . FILTER(?res=<%s>||?res=<%s>||?res=<%s>)}";
 		String PREFIX_TEMPLATE = "PREFIX  %s:<%s> ";
 		Map<String, List> denormalizedValues = new HashMap<String, List>();
 		List<Concept> concepts = new ArrayList<Concept>();
 		List<AgentType> agents = new ArrayList<AgentType>();
 		List<TimeSpanType> timespans = new ArrayList<TimeSpanType>();
 		List<PlaceType> places = new ArrayList<PlaceType>();
 
 		Concept lastConcept = null;
 		AgentType lastAgent = null;
 		TimeSpanType lastTimespan = null;
 		PlaceType lastPlace = null;
 		System.out.println(xmlString);
 		RDFReaderF rdfReader = solrWorkFlowService.getRDFReaderF();
 		Model model = ModelFactory.createDefaultModel();
 
 		rdfReader.getReader().read(model,
 				new ByteArrayInputStream(xmlString.getBytes()), "");
 
 		Map<String, String> prefixNS = model.getNsPrefixMap();
 		StringBuilder sb = new StringBuilder();
 		for (Entry<String, String> prefix : prefixNS.entrySet()) {
 
 			sb.append(String.format(PREFIX_TEMPLATE, prefix.getKey(),
 					prefix.getValue()));
 		}
 
 		String rootEntity = String.format(ROOT_TEMPLATE, sb.toString(),
 				resource, resource + "/",
 				resource.subSequence(0, resource.length() - 1));
 
 		com.hp.hpl.jena.query.Query queryRoot = QueryFactory.create(rootEntity);
 		QueryExecution qRoot = QueryExecutionFactory.create(queryRoot, model);
 		try {
 			ResultSet rootRs = qRoot.execSelect();
 			while (rootRs.hasNext()) {
 				String element = rootRs.next().get("?object").toString();
 				System.out.println(element);
 				String[] nsAndLocal = element.split("#");
 				if (nsAndLocal.length == 1) {
 					nsAndLocal = new String[] {
 							StringUtils.substringBeforeLast(element, "/"),
 							StringUtils.substringAfterLast(element, "/") };
 
 				}
 				String prefix = findByPrefix(prefixNS, nsAndLocal[0]);
 				if (prefix != null) {
 					String normalizedElement = prefix + ":" + nsAndLocal[1];
 
 					if (isMapped(normalizedElement)) {
 						List<EdmMappedField> edmList = getEdmLabel(normalizedElement);
 
 						if (edmList != null && edmList.size() > 0) {
 
 							for (EdmMappedField edmLabel : edmList) {
 
 								if (StringUtils.equalsIgnoreCase(edmLabel
 										.getLabel().toString(), "skos_concept")) {
 									if (lastConcept != null) {
 										if (lastConcept.getAbout() != null) {
 											concepts.add(lastConcept);
 											lastConcept = createNewEntity(
 													Concept.class, resource);
 										}
 									} else {
 										lastConcept = createNewEntity(
 												Concept.class, resource);
 
 									}
 
 								} else if (StringUtils.equalsIgnoreCase(
 										edmLabel.getLabel().toString(),
 										"edm_agent")) {
 									if (lastAgent != null) {
 										if (lastAgent.getAbout() != null) {
 											agents.add(lastAgent);
 											lastAgent = createNewEntity(
 													AgentType.class, resource);
 										}
 									} else {
 										lastAgent = createNewEntity(
 												AgentType.class, resource);
 									}
 
 								} else if (StringUtils.equalsIgnoreCase(
 										edmLabel.getLabel().toString(),
 										"edm_timespan")) {
 									if (lastTimespan != null) {
 										if (lastTimespan.getAbout() != null) {
 											timespans.add(lastTimespan);
 											lastTimespan = createNewEntity(
 													TimeSpanType.class,
 													resource);
 										}
 
 									} else {
 										lastTimespan = createNewEntity(
 												TimeSpanType.class, resource);
 									}
 
 								} else if (StringUtils.equalsIgnoreCase(
 										edmLabel.getLabel().toString(),
 										"edm_place")) {
 									if (lastPlace != null) {
 										if (lastPlace.getAbout() != null) {
 											places.add(lastPlace);
 											lastPlace = createNewEntity(
 													PlaceType.class, resource);
 										}
 									} else {
 										System.out
 												.println("Creating new place");
 										lastPlace = createNewEntity(
 												PlaceType.class, resource);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		// Find the rest and append them as appropriate
 		String qString = String.format(SPARQL_TEMPLATE, sb.toString(),
 				resource, resource + "/",
 				resource.substring(0, resource.length() - 1));
 		com.hp.hpl.jena.query.Query query = QueryFactory.create(qString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, model);
 		try {
 			ResultSet results = qexec.execSelect();
 
 			while (results.hasNext()) {
 				QuerySolution element = results.next();
 				String[] nsAndLocal = element.get("?predicate").toString()
 						.split("#");
 				if (nsAndLocal.length == 1) {
 					nsAndLocal = new String[] {
 							StringUtils.substringBeforeLast(
 									element.get("?predicate").toString(), "/"),
 							StringUtils.substringAfterLast(
 									element.get("?predicate").toString(), "/") };
 
 				}
 				String prefix = findByPrefix(prefixNS, nsAndLocal[0]);
 				if (prefix != null) {
 					String normalizedElement = prefix + ":" + nsAndLocal[1];
 					if (isMapped(normalizedElement)) {
 						List<EdmMappedField> edmList = getEdmLabel(normalizedElement);
 						if (edmList != null && edmList.size() > 0) {
 							for (EdmMappedField edmLabel : edmList) {
 								if (StringUtils.startsWith(edmLabel.getLabel()
 										.toString(), "cc")) {
 
 									if (element.get("?object").isLiteral()) {
 										appendConceptValue(
 												lastConcept == null ? new Concept()
 														: lastConcept, edmLabel
 														.getLabel().toString(),
 												element.get("?object")
 														.asLiteral()
 														.getString(),
 												"xml:lang",
 												element.get("?object")
 														.asLiteral()
 														.getLanguage(),
 												iterations);
 									} else {
 										appendConceptValue(
 												lastConcept == null ? new Concept()
 														: lastConcept, edmLabel
 														.getLabel().toString(),
 												element.get("?object")
 														.asResource().getURI(),
 												null, null, iterations);
 									}
 									ControlledVocabularyImpl oldVoc = vocabulary;
 									if (StringUtils.equals(edmLabel.getLabel()
 											.toString(), "cc_skos_broader")
 											&& iterations > 0) {
 										ControlledVocabularyImpl controlledVocabulary = getControlledVocabulary(
 												datastore, "URI",
 												element.get("?object")
 														.asResource().getURI());
 
 										concepts.addAll(denormalize(
 												element.get("?object")
 														.asResource().getURI(),
 												controlledVocabulary,
 												iterations - 1, true).get(
 												"concepts"));
 									}
 									vocabulary = oldVoc;
 								} else if (StringUtils.startsWith(edmLabel
 										.getLabel().toString(), "pl")) {
 									if (element.get("?object").isLiteral()) {
 										appendValue(
 												PlaceType.class,
 												lastPlace == null ? new PlaceType()
 														: lastPlace, edmLabel
 														.getLabel().toString(),
 												element.get("?object")
 														.asLiteral()
 														.getString(),
 												"xml:lang",
 												element.get("?object")
 														.asLiteral()
 														.getLanguage(),
 												iterations);
 									} else {
 										appendValue(
 												PlaceType.class,
 												lastPlace == null ? new PlaceType()
 														: lastPlace, edmLabel
 														.getLabel().toString(),
 												element.get("?object")
 														.asResource().getURI(),
 												null, null, iterations);
 									}
 									ControlledVocabularyImpl oldVoc = vocabulary;
 									if (StringUtils.equals(edmLabel.getLabel()
 											.toString(), "pl_dcterms_isPartOf")
 											&& iterations > 0) {
 										ControlledVocabularyImpl controlledVocabulary = getControlledVocabulary(
 												datastore, "URI",
 												element.get("?object")
 														.asResource().getURI());
 										Map<String, List> pl = denormalize(
 												element.get("?object")
 														.asResource().getURI(),
 												controlledVocabulary,
 												iterations - 1, true);
 										if (pl != null) {
 											places.addAll(pl.get("places"));
 										}
 									}
 									vocabulary = oldVoc;
 								} else if (StringUtils.startsWith(edmLabel
 										.getLabel().toString(), "ag")) {
 									if (element.get("?object").isLiteral()) {
 										appendValue(
 												AgentType.class,
 												lastAgent == null ? new AgentType()
 														: lastAgent, edmLabel
 														.getLabel().toString(),
 												element.get("?object")
 														.asLiteral()
 														.getString(),
 												"xml:lang",
 												element.get("?object")
 														.asLiteral()
 														.getLanguage(),
 												iterations);
 									} else {
 										appendValue(
 												AgentType.class,
 												lastAgent == null ? new AgentType()
 														: lastAgent, edmLabel
 														.getLabel().toString(),
 												element.get("?object")
 														.asResource().getURI(),
 												null, null, iterations);
 									}
 								} else if (StringUtils.startsWith(edmLabel
 										.getLabel().toString(), "ts")) {
 									if (element.get("?object").isLiteral()) {
 										appendValue(
 												TimeSpanType.class,
 												lastTimespan == null ? new TimeSpanType()
 														: lastTimespan,
 												edmLabel.getLabel().toString(),
 												element.get("?object")
 														.asLiteral()
 														.getString(),
 												"xml:lang",
 												element.get("?object")
 														.asLiteral()
 														.getLanguage(),
 												iterations);
 									} else {
 										appendValue(
 												TimeSpanType.class,
 												lastTimespan == null ? new TimeSpanType()
 														: lastTimespan,
 												edmLabel.getLabel().toString(),
 												element.get("?object")
 														.asResource().getURI(),
 												null, null, iterations);
 									}
 									ControlledVocabularyImpl oldVoc = vocabulary;
 									if (StringUtils.equals(edmLabel.getLabel()
 											.toString(), "ts_dcterms_isPartOf")
 											&& iterations > 0) {
 										ControlledVocabularyImpl controlledVocabulary = getControlledVocabulary(
 												datastore, "URI",
 												element.get("?object")
 														.asResource().getURI());
 
 										timespans.addAll(denormalize(
 												element.get("?object")
 														.asResource().getURI(),
 												controlledVocabulary,
 												iterations - 1, true).get(
 												"timespans"));
 									}
 									vocabulary = oldVoc;
 								}
 							}
 
 						}
 					}
 				}
 			}
 			if (lastConcept != null)
 				concepts.add(lastConcept);
 			if (lastAgent != null)
 				agents.add(lastAgent);
 			if (lastTimespan != null)
 				timespans.add(lastTimespan);
 			if (lastPlace != null)
 				places.add(lastPlace);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		denormalizedValues.put("concepts", concepts);
 		denormalizedValues.put("agents", agents);
 		denormalizedValues.put("timespans", timespans);
 		denormalizedValues.put("places", places);
 
 		return denormalizedValues;
 	}
 
 	private String findByPrefix(Map<String, String> prefixNS, String string) {
 		for (Entry<String, String> prefix : prefixNS.entrySet()) {
 			if (StringUtils.startsWith(prefix.getValue(), string)
 					|| StringUtils.startsWith(string, prefix.getValue())) {
 				return prefix.getKey();
 			}
 		}
 		return null;
 	}
 
 	public void setDatastore(Datastore datastore) {
 		this.datastore = datastore;
 	}
 
 	public boolean isMapped(String field) {
 
 		if (vocabulary != null) {
 			for (Entry<String, List<EdmMappedField>> entry : vocabulary
 					.getElements().entrySet()) {
 				if (StringUtils.contains(entry.getKey(), field)
 						&& entry.getValue() != null) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private EntityImpl retrieveValueFromResource(String resource) {
 		EntityImpl entity;
 		synchronized (this) {
 			entity = datastore.find(EntityImpl.class).filter("uri", resource)
 					.get();
 		}
 		if (entity == null) {
 			String val = retrieveValue(resource);
 			if (val.length() > 0) {
 				EntityImpl newEntity = new EntityImpl();
 				newEntity.setUri(resource);
 				newEntity.setTimestamp(new Date().getTime());
 				newEntity.setContent(val);
 				try {
 					datastore.save(newEntity);
 					return newEntity;
 				} catch (Exception e) {
 					return datastore.find(EntityImpl.class)
 							.filter("uri", resource).get();
 				}
 			}
 			return null;
 		} else {
 			if (new Date().getTime() - entity.getTimestamp() < UPDATETIMESTAMP) {
 				return entity;
 			} else {
 				String val = retrieveValue(resource);
 				Query<EntityImpl> updateQuery = datastore
 						.createQuery(EntityImpl.class).field("uri")
 						.equal(resource);
 				UpdateOperations<EntityImpl> ops = datastore
 						.createUpdateOperations(EntityImpl.class).set(
 								"content", val);
 				datastore.update(updateQuery, ops);
 				entity.setContent(val);
 				return entity;
 			}
 		}
 	}
 
 	private String retrieveValue(String resource) {
 		URLConnection urlConnection;
 		if (resource != null && vocabulary != null) {
 			try {
 
 				if (StringUtils.isNotBlank(vocabulary.getReplaceUrl())) {
 					System.out.println("replacing with "
 							+ vocabulary.getReplaceUrl());
 					resource = StringUtils.replace(resource,
 							vocabulary.getURI(), vocabulary.getReplaceUrl());
 				}
 				urlConnection = new URL(resource).openConnection();
 
 				InputStream inputStream = urlConnection.getInputStream();
 				StringWriter writer = new StringWriter();
 				IOUtils.copy(inputStream, writer, "UTF-8");
 				return writer.toString();
 
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return "";
 	}
 
 	private <T> T createNewEntity(Class<T> clazz, String val)
 			throws InstantiationException, IllegalAccessException,
 			SecurityException, NoSuchMethodException, IllegalArgumentException,
 			InvocationTargetException {
 		System.out.println("Creating new agent " + clazz.getCanonicalName());
 		T obj = clazz.newInstance();
 		Class<?>[] cls = new Class<?>[1];
 		cls[0] = (String.class);
 		Method method = clazz.getMethod("setAbout", cls);
 		method.invoke(obj, val);
 		return obj;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> T appendValue(Class<T> clazz, T obj, String edmLabel,
 			String val, String edmAttr, String valAttr, int iterations)
 			throws SecurityException, NoSuchMethodException,
 			IllegalArgumentException, IllegalAccessException,
 			InvocationTargetException {
 		System.out.println("appendign field:" + edmLabel);
 		if (val != null) {
 			String valOld = val;
 			Pattern jibxFixerPattern = Pattern.compile("[\\uD800-\\uE000]",
 					Pattern.UNICODE_CASE | Pattern.CANON_EQ
 							| Pattern.CASE_INSENSITIVE);
 			Matcher jibxFixerMatcher = jibxFixerPattern.matcher(val);
 			String valNew = jibxFixerMatcher.replaceAll("");
 			if (!StringUtils.equals(valNew, valOld)) {
 				return obj;
 			}
 		}
 
 		RdfMethod RDF = null;
 		for (RdfMethod rdfMethod : RdfMethod.values()) {
 			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
 				RDF = rdfMethod;
 			}
 		}
 		if (RDF.getMethodName().endsWith("List")) {
 
 			Method mthd = clazz.getMethod(RDF.getMethodName());
 
 			@SuppressWarnings("rawtypes")
 			List lst = mthd.invoke(obj) != null ? (ArrayList) mthd.invoke(obj)
 					: new ArrayList();
 			if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceType.class)) {
 
 				ResourceType rs = new ResourceType();
 				rs.setResource(val != null ? val : valAttr);
 				if (isURI(rs.getResource())) {
 					denormalize(rs.getResource(), iterations - 1);
 				}
 				lst.add(RDF.returnObject(RDF.getClazz(), rs));
 
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceOrLiteralType.class)) {
 				ResourceOrLiteralType rs = new ResourceOrLiteralType();
 				if (isURI(val)) {
 					Resource res = new Resource();
 					res.setResource(val);
 					rs.setResource(res);
 				} else {
 					rs.setString(val);
 				}
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						Lang lang = new Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 					}
 
 				}
 				lst.add(RDF.returnObject(RDF.getClazz(), rs));
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(LiteralType.class)) {
 				LiteralType rs = new LiteralType();
 				rs.setString(val);
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						LiteralType.Lang lang = new LiteralType.Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 					}
 				}
 				lst.add(RDF.returnObject(RDF.getClazz(), rs));
 			}
 
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = List.class;
 			Method method = obj.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(obj, lst);
 		} else {
 			if (RDF.getClazz().isAssignableFrom(_Long.class)) {
 				Float rs = Float.parseFloat(val);
 				_Long lng = new _Long();
 				lng.setLong(rs);
 				((PlaceType) obj).setLong(lng);
 
 			} else if (RDF.getClazz().isAssignableFrom(Lat.class)) {
 				Float rs = Float.parseFloat(val);
 				Lat lng = new Lat();
 				lng.setLat(rs);
 				((PlaceType) obj).setLat(lng);
 
 			} else if (RDF.getClazz().isAssignableFrom(Alt.class)) {
 				Float rs = Float.parseFloat(val);
 				Alt lng = new Alt();
 				lng.setAlt(rs);
 				((PlaceType) obj).setAlt(lng);
 
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceType.class)) {
 				ResourceType rs = new ResourceType();
 				rs.setResource(val != null ? val : valAttr);
 				if (isURI(rs.getResource())) {
 					denormalize(rs.getResource(), iterations - 1);
 				}
 				Class<?>[] cls = new Class<?>[1];
 				cls[0] = RDF.getClazz();
 				Method method = obj.getClass().getMethod(
 						StringUtils.replace(RDF.getMethodName(), "get", "set"),
 						cls);
 				method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(LiteralType.class)) {
 				LiteralType rs = new LiteralType();
 				rs.setString(val);
 				if (isURI(val)) {
 					denormalize(val, iterations - 1);
 				}
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						LiteralType.Lang lang = new LiteralType.Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 
 					}
 				}
 				Class<?>[] cls = new Class<?>[1];
 				cls[0] = RDF.getClazz();
 				Method method = obj.getClass().getMethod(
 						StringUtils.replace(RDF.getMethodName(), "get", "set"),
 						cls);
 				method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
 
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceOrLiteralType.class)) {
 				ResourceOrLiteralType rs = new ResourceOrLiteralType();
 				if (isURI(val)) {
 					Resource res = new Resource();
 					res.setResource(val);
 					rs.setResource(res);
 					denormalize(val, iterations - 1);
 				} else {
 					rs.setString(val);
 				}
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						Lang lang = new Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 					}
 				}
 				Class<?>[] cls = new Class<?>[1];
 				cls[0] = clazz;
 				Method method = obj.getClass().getMethod(
 						StringUtils.replace(RDF.getMethodName(), "get", "set"),
 						cls);
 				method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
 			}
 		}
 
 		return obj;
 	}
 
 	private Concept appendConceptValue(Concept concept, String edmLabel,
 			String val, String edmAttr, String valAttr, int iterations)
 			throws SecurityException, NoSuchMethodException,
 			IllegalArgumentException, IllegalAccessException,
 			InvocationTargetException {
 		RdfMethod RDF = null;
 		if (val != null) {
 			String valOld = val;
 			Pattern jibxFixerPattern = Pattern.compile("[\\uD800-\\uE000]",
 					Pattern.UNICODE_CASE | Pattern.CANON_EQ
 							| Pattern.CASE_INSENSITIVE);
 			Matcher jibxFixerMatcher = jibxFixerPattern.matcher(val);
 			String valNew = jibxFixerMatcher.replaceAll("?");
 			if (!StringUtils.equals(valNew, valOld)) {
 				return concept;
 			}
 		}
 
 		for (RdfMethod rdfMethod : RdfMethod.values()) {
 			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
 				RDF = rdfMethod;
 				break;
 			}
 		}
 		List<Concept.Choice> lst = concept.getChoiceList() != null ? concept
 				.getChoiceList() : new ArrayList<Concept.Choice>();
 		if (RDF.getClazz().getSuperclass().isAssignableFrom(ResourceType.class)) {
 
 			ResourceType obj = new ResourceType();
 			obj.setResource(val != null ? val : valAttr);
 			if (isURI(obj.getResource())) {
 				denormalize(obj.getResource(), iterations - 1);
 			}
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = RDF.getClazz();
 			Concept.Choice choice = new Concept.Choice();
 			Method method = choice.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
 			lst.add(choice);
 
 		} else if (RDF.getClazz().getSuperclass()
 				.isAssignableFrom(ResourceOrLiteralType.class)) {
 
 			ResourceOrLiteralType obj = new ResourceOrLiteralType();
 
 			if (isURI(val)) {
 				Resource res = new Resource();
 				res.setResource(val);
 				obj.setResource(res);
 				denormalize(val, iterations - 1);
 			} else {
 				obj.setString(val);
 			}
 			if (edmAttr != null) {
 
 				if (StringUtils.equals(edmAttr, "xml:lang")) {
 					Lang lang = new Lang();
 					lang.setLang(valAttr);
 					obj.setLang(lang);
 				}
 			}
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = RDF.getClazz();
 			Concept.Choice choice = new Concept.Choice();
 			Method method = choice.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
 			lst.add(choice);
 
 		} else if (RDF.getClazz().getSuperclass()
 				.isAssignableFrom(LiteralType.class)) {
 			LiteralType obj = new LiteralType();
 			obj.setString(val);
 			if (isURI(val)) {
 				denormalize(val, iterations - 1);
 			}
 			if (edmAttr != null) {
 
 				if (StringUtils.equals(edmAttr, "xml:lang")) {
 					LiteralType.Lang lang = new LiteralType.Lang();
 					lang.setLang(valAttr);
 					obj.setLang(lang);
 				}
 			}
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = RDF.getClazz();
 			Concept.Choice choice = new Concept.Choice();
 			Method method = choice.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
 			lst.add(choice);
 		}
 		concept.setChoiceList(lst);
 		return concept;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private Map<String, List> denormalize(String val, int iterations) {
 		try {
 			if (iterations > 0) {
 				ControlledVocabularyImpl controlledVocabulary = getControlledVocabulary(
 						datastore, "URI", val);
 				return denormalize(val, controlledVocabulary, iterations, false);
 			}
 			return new HashMap<String, List>();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static boolean isURI(String uri) {
 
 		try {
 			new URL(uri);
 			return true;
 		} catch (MalformedURLException e) {
 			return false;
 		}
 
 	}
 
 	public void setMappedField(String fieldToMap, EdmLabel europeanaField,
 			String attribute) {
 		HashMap<String, List<EdmMappedField>> elements = vocabulary
 				.getElements() != null ? (HashMap<String, List<EdmMappedField>>) vocabulary
 				.getElements() : new HashMap<String, List<EdmMappedField>>();
 		List<EdmMappedField> element = elements.get(fieldToMap);
 		if (!element.contains(europeanaField)) {
 			EdmMappedField edmField = new EdmMappedField();
 			edmField.setLabel(europeanaField.toString());
 			edmField.setAttribute(StringUtils.isNotBlank(attribute) ? attribute
 					: "");
 
 			element.add(edmField);
 			elements.put(fieldToMap, element);
 			vocabulary.setElements(elements);
 		}
 
 	}
 
 	public String getMappedField(EdmMappedField europeanaField) {
 		for (String key : vocabulary.getElements().keySet()) {
 			if (europeanaField.equals(vocabulary.getElements().get(key))) {
 				return key;
 			}
 		}
 		return null;
 	}
 
 	public ControlledVocabularyImpl getControlledVocabulary(
 			Datastore datastore, String field, String filter)
 			throws UnknownHostException, MongoException {
 		String[] splitName = filter.split("/");
 		if (splitName.length > 3) {
 			String vocabularyName = splitName[0] + "/" + splitName[1] + "/"
 					+ splitName[2] + "/";
 			List<ControlledVocabularyImpl> vocabularies = VocMemCache
 					.getMemCache(solrWorkFlowService).get(vocabularyName);
 			if (vocabularies.size() == 0) {
 				for (Entry<String, List<ControlledVocabularyImpl>> vocs : VocMemCache
 						.getMemCache(solrWorkFlowService).entrySet()) {
 					for (ControlledVocabularyImpl voc : vocs.getValue()) {
 						if (voc.getReplaceUrl() != null
 								&& StringUtils.equals(voc.getReplaceUrl(),
 										vocabularyName)) {
 							vocabularies.add(voc);
 						}
 					}
 				}
 			}
 
 			for (ControlledVocabularyImpl vocabulary : vocabularies) {
 				for (String rule : vocabulary.getRules()) {
 					if (StringUtils.equals(rule, "*")
 							|| StringUtils.contains(filter, rule)) {
 						return vocabulary;
 					}
 				}
 
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Retrieve all the stored controlled vocabularies
 	 * 
 	 * @return A list with all the stored controlled vocabularies
 	 */
 
 	public List<ControlledVocabularyImpl> getControlledVocabularies() {
 		return datastore.find(ControlledVocabularyImpl.class) != null ? datastore
 				.find(ControlledVocabularyImpl.class).asList() : null;
 	}
 
 }
