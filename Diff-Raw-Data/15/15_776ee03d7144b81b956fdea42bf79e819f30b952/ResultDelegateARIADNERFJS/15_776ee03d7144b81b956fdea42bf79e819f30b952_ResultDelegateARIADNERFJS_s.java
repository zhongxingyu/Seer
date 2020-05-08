 package org.ariadne_eu.metadata.resultsformat;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import net.sourceforge.minor.lucene.core.searcher.IndexSearchDelegate;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.TopDocs;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.FacetField;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.client.solrj.response.FacetField.Count;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.params.FacetParams;
 import org.ariadne.config.PropertiesManager;
 import org.ariadne_eu.utils.config.RepositoryConstants;
 import org.ariadne_eu.utils.solr.SolrServerManagement;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 public class ResultDelegateARIADNERFJS implements IndexSearchDelegate {
 	private static Logger log = Logger
 			.getLogger(ResultDelegateARIADNERFJS.class);
 
 	private int start;
 	private int max;
 	private String lQuery;
 	private static Vector<String> facetFields;
 
 	static {
 		try {
 			facetFields = new Vector<String>();
 
 			Collection solrs = PropertiesManager
 					.getInstance()
 					.getPropertyStartingWith(
 							RepositoryConstants.getInstance().SR_SOLR_FACETFIELD
 									+ ".").values();
 			for (Object object : solrs) {
 				facetFields.add((String) object);
 			}
 
 			if (!(facetFields.size() > 0)) {
 				log.error("initialize:property \""
 						+ RepositoryConstants.getInstance().SR_SOLR_FACETFIELD
 						+ ".n\" not defined");
 			}
 
 		} catch (Throwable t) {
 			log.error("initialize: ", t);
 		}
 	}
 
 	public ResultDelegateARIADNERFJS(int start, int max, String lQuery) {
 		this.start = start;
 		this.max = max;
 		this.lQuery = lQuery;
 	}
 
 	public String result(TopDocs topDocs, IndexSearcher searcher)
 			throws JSONException, CorruptIndexException, IOException {
 		SolrDocument doc;
 
 		QueryResponse response = getSolrResponse();
 
 		JSONObject resultsJson = new JSONObject();
 		JSONObject resultJson = new JSONObject();
 		JSONArray idArrayJson = new JSONArray();
 		JSONArray metadataArrayJson = new JSONArray();
 		resultJson.put("error", "");
 		resultJson.put("errorMessage", "");
 		resultJson.put("facets", getFacets(response.getFacetFields()));
 
 		int size = (int) response.getResults().getNumFound();
 		if (size == -1)
 			size = Integer.MAX_VALUE;
 
 		for (int i = 0; i < max && (max < 0 || i < size - start); i++) {
 			JSONObject json = new JSONObject();
 			doc = response.getResults().get(i);
 			try {
 				idArrayJson.put(doc.get("lom.general.identifier.entry"));
 
 				addJsonObjectWE(doc, json, "lom.general.title.langstring",
 						"title");
 				addJsonObjectWE(doc, json, "lom.general.title.string", "title");
 
 				addJsonObjectWE(doc, json, "lom.general.identifier.entry",
 						"identifier");
 
 				addJsonObjectWE(doc, json,
 						"lom.general.description.langstring", "descriptions");
 				addJsonObjectWE(doc, json, "lom.general.description.string",
 						"descriptions");
 
 				addJsonObjectWE(doc, json, "lom.general.language", "language");
 
 				addJsonObjectWE(doc, json, "lom.general.keyword.langstring",
 						"keywords");
 				addJsonObjectWE(doc, json, "lom.general.keyword.string",
 						"keywords");
 
 				addJsonObjectWE(doc, json, "lom.technical.location", "location");
 
 				addJsonObjectWE(doc, json, "lom.general.identifier.entry",
 						"identifier");
 
 				addJsonObjectWE(doc, json, "lom.educational.context.value",
 						"context");
 
 				addJsonObjectWE(doc, json, "lom.technical.size", "size");
 
 				addJsonObjectWE(doc, json, "lom.metametadata.identifier.entry",
 						"metaMetadataId");
 
 				addJsonObjectWE(doc, json, "lom.technical.format", "format");
 				addJsonObjectWE(doc, json,
 						"lom.metametadata.identifier.catalog", "dataProvider");
 
 				addJsonObjectWE(doc, json, "lom.technical.duration", "thumbURL");
 
 				addJsonObjectWE(doc, json,
 						"lom.rights.copyrightandotherestrictions.value",
 						"license");
 				//
 				addJsonObjectWE(doc, json,
 						"lom.educational.learningresourcetype.value", "type");
 				addJsonObject(doc, json, "lom.lifecycle.contribute.role.value",
 						"contributor role");
 
 				addJsonObjectWE(doc, json, "lom.general.identifier.catalog",
 						"catalog");
 
 				addJsonObjectWE(doc, json,
 						"lom.lifecycle.contribute.date.datetime",
 						"contribution date");
 
 				addJsonObjectWE(doc, json, "lom.lifecycle.contribute.entity",
 						"contributor");
 
 				addJsonObjectWE(doc, json,
 						"lom.classification.taxonpath.taxon.id",
 						"classification");
 
 				addJsonObjectWE(doc, json,
 						"lom.rights.copyrightandotherrestrictions.source",
 						"license source");
 
 				addJsonObjectWE(doc, json, "lom.annotation.date.datetime",
 						"annotation date");
 
 				addJsonObjectWE(doc, json, "lom.rights.description.langstring",
 						"rights");
 				addJsonObjectWE(doc, json, "lom.rights.description.string",
 						"rights");
 
 				addJsonObjectWE(doc, json, "lom.classification.purpose.value",
 						"classification purpose");
 				addJsonObjectWE(doc, json,
 						"lom.relation.resource.identifier.entry", "relation");
 
 				addJsonObject(doc, json, "mdPath", "mdPath");
 
 			} catch (JSONException ex) {
 				log.error(ex);
 			}
 			metadataArrayJson.put(json);
 		}
 		resultJson.put("id", idArrayJson);
 		resultJson.put("metadata", metadataArrayJson);
 		resultJson.put("nrOfResults", size);
 
 		resultsJson.put("result", resultJson);
 
 		return resultsJson.toString();
 	}
 
 	private void addJsonObjectWE(SolrDocument doc, JSONObject json,
 			String fieldName, String responeseName) throws JSONException {
 
 		Collection collection = new HashSet();
 
 		handleAttributeElements(doc, json, fieldName, responeseName, collection);
 	}
 
 	private void handleAttributeElements(SolrDocument doc, JSONObject json,
 			String fieldName, String responseName, Collection data)
 			throws JSONException {
 
 		String langAttributes = fieldName + ".language";
 
 		Collection<Object> fieldValues = doc.getFieldValues(fieldName);
 		Collection<Object> fieldLangValues = doc.getFieldValues(langAttributes);
 
 		if (fieldLangValues == null) {
 			langAttributes = fieldName + ".xml:lang";
 			fieldLangValues = doc.getFieldValues(langAttributes);
 		}
 
 		if (fieldValues != null && fieldLangValues != null) {
 
 			Object[] fValuesarray = fieldValues.toArray();
 			Object[] flvArray = fieldLangValues.toArray();
 
 			for (int i = 0; i < fValuesarray.length; i++) {
 
 				Object fValue = fValuesarray[i];
 
 				Object fLangValue = "noLangValue";
 				try {
 					fLangValue = flvArray[i];
 
 					JSONObject jsonObject = new JSONObject();
 					jsonObject.put("value", fValue);
 					jsonObject.put("lang", fLangValue);
 
 					data.add(jsonObject);
 				} catch (IndexOutOfBoundsException ex) {
 
 					JSONObject jsonObject = new JSONObject();
 					jsonObject.put("value", fValue);
 					jsonObject.put("lang", fLangValue);
 					data.add(jsonObject);
 				}
 
 			}
 
 			json.put(responseName, data);
 		} else if (fieldValues != null && fieldLangValues == null) {
 			addJsonObject(doc, json, fieldName, responseName);
 
 		}
 
 	}
 
 	private void addJsonObject(SolrDocument doc, JSONObject json,
 			String fieldName, String responeseName) throws JSONException {
 		// Object field = doc.get(fieldName);
 
 		Collection<Object> values = doc.getFieldValues(fieldName);
 
		Object[] results = values.toArray();
		
//		if (values != null) {
//
//			Object[] results = values.toArray();
//
 //			int length = results.length;
 //			if (length == 1) {
 //				Object object = results[0];
 //				json.put(responeseName, object);
 //
 //			} else {
 				JSONObject jsonObject = new JSONObject();
 
 				for (int i = 0; i < results.length; i++) {
 
 					Object object = results[i];
 
 					jsonObject.put(responeseName + "_" + i, object);
 
 				}
 				json.put(responeseName, jsonObject);
 			//}
 
		//}
 
 		// if (field != null)
 		// json.put(responeseName, field);
 		// else
 		// json.put(responeseName, new String(""));
 	}
 
 	private QueryResponse getSolrResponse() {
 		SolrServerManagement serverMgt = SolrServerManagement.getInstance();
 
 		SolrQuery solrQuery = new SolrQuery().setQuery(lQuery).setFacet(true)
 				.setFacetLimit(-1).setFacetMinCount(1)
 				.setFacetSort(FacetParams.FACET_SORT_COUNT)
 				.setParam("rows", Integer.toString(max))
 				.setParam("start", Integer.toString(start));
 
 		for (Iterator<String> iterator = facetFields.iterator(); iterator
 				.hasNext();) {
 			String facetField = (String) iterator.next();
 			solrQuery.addFacetField(facetField);
 		}
 		QueryResponse rsp = null;
 
 		try {
 			rsp = serverMgt.getServer().query(solrQuery);
 
 		} catch (SolrServerException e) {
 			log.error("getSolrResponse: Solr server error", e);
 		} catch (IOException e) {
 			log.error("getSolrResponse: Solr I/O error", e);
 		}
 		return rsp;
 	}
 
 	private JSONArray getFacets(List facetsFields) {
 		JSONArray facetsJson = new JSONArray();
 		try {
 			if (facetsFields.size() > 0) {
 				List<Count> facetValues;
 				FacetField facetField;
 				FacetField.Count innerFacetField;
 				for (Iterator facetIterator = facetsFields.iterator(); facetIterator
 						.hasNext();) {
 					JSONObject facetJson = new JSONObject();
 					facetField = (FacetField) facetIterator.next();
 					facetJson.put("field",
 							changeFacetName(facetField.getName()));
 
 					facetValues = facetField.getValues();
 					if (facetValues != null) {
 						JSONArray valuesJson = new JSONArray();
 						for (Iterator ifacetIterator = facetValues.iterator(); ifacetIterator
 								.hasNext();) {
 							JSONObject value = new JSONObject();
 							innerFacetField = (FacetField.Count) ifacetIterator
 									.next();
 							value.put("val", innerFacetField.getName());
 							value.put("count", innerFacetField.getCount());
 							valuesJson.put(value);
 						}
 						facetJson.put("numbers", valuesJson);
 					}
 					facetsJson.put(facetJson);
 				}
 
 			}
 		} catch (JSONException e) {
 			log.error("getFacets: JSON format error", e);
 		}
 		return facetsJson;
 	}
 
 	private String changeFacetName(String internalName) {
 		if (internalName
 				.equalsIgnoreCase("lom.educational.learningresourcetype.value"))
 			return "lrt";
 		else if (internalName.equalsIgnoreCase("lom.educational.context.value"))
 			return "context";
 		else if (internalName.equalsIgnoreCase("lom.technical.format"))
 			return "format";
 		else if (internalName.equalsIgnoreCase("lom.general.language"))
 			return "language";
 		else if (internalName.equalsIgnoreCase("collection"))
 			return "provider";
 		else if (internalName
 				.equalsIgnoreCase("lom.educational.interactivitytype.value"))
 			return "it";
 		else if (internalName
 				.equalsIgnoreCase("lom.educational.interactivitylevel.value"))
 			return "il";
 		else if (internalName
 				.equalsIgnoreCase("lom.educational.intendedenduserrole.value"))
 			return "iur";
 		else if (internalName
 				.equalsIgnoreCase("lom.educational.typicalagerange.string"))
 			return "tagr";
 		else if (internalName.equalsIgnoreCase("lom.general.keyword.string"))
 			return "keyword";
 		else if (internalName.equalsIgnoreCase("lom.rights.description.string"))
 			return "rights";
 		else if (internalName
 				.equalsIgnoreCase("lom.rights.copyrightandotherrestrictions.string"))
 			return "licences";
 		else if (internalName
 				.equalsIgnoreCase("lom.classification.taxonpath.taxon.entry.langstring"))
 			return "classification";
 		else if (internalName
 				.equalsIgnoreCase("lom.educational.typicalagerange.string"))
 			return "temporal";
 		else if (internalName.equalsIgnoreCase("lom.general.coverage.string"))
 			return "spatial";
 		else if (internalName
 				.equalsIgnoreCase("lom.classification.description.string"))
 			return "common";
 
 		return internalName;
 	}
 }
