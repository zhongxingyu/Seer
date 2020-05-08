 package controllers;
 
 import play.*;
 import play.db.DB;
 import play.jobs.Job;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.mvc.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.lucene.analysis.TokenFilter;
 import org.apache.lucene.search.BooleanFilter;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.Query;
 import org.elasticsearch.action.count.CountRequestBuilder;
 import org.elasticsearch.action.count.CountResponse;
 import org.elasticsearch.action.search.SearchRequest;
 import org.elasticsearch.action.search.SearchRequestBuilder;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.action.search.SearchType;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.settings.ImmutableSettings;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.elasticsearch.common.unit.TimeValue;
 
 import static org.elasticsearch.index.query.QueryBuilders.*;
 
 import org.elasticsearch.index.analysis.Analysis;
 import org.elasticsearch.index.query.FilterBuilder;
 import org.elasticsearch.index.query.FilterBuilders;
 import static org.elasticsearch.index.query.FilterBuilders.*;
 
 import org.elasticsearch.index.query.AndFilterBuilder;
 import org.elasticsearch.index.query.BoolFilterBuilder;
 import org.elasticsearch.index.query.BoolQueryBuilder;
 import org.elasticsearch.index.query.GeoBoundingBoxFilterBuilder;
 import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
 import org.elasticsearch.index.query.OrFilterBuilder;
 import org.elasticsearch.index.query.QueryBuilder;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.index.query.TextQueryBuilder;
 import org.elasticsearch.index.query.TextQueryBuilder.Operator;
 import org.elasticsearch.index.search.geo.GeoDistanceFilter;
 import org.elasticsearch.node.Node;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.SearchHits;
 import org.elasticsearch.search.facet.FacetBuilders;
 import org.elasticsearch.search.facet.terms.TermsFacet;
 import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
 import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
 import org.gbif.ecat.model.ParsedName;
 import org.gbif.ecat.parser.NameParser;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.mongodb.DBRef;
 import com.mongodb.util.JSON;
 import java.io.FileWriter;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 
 import static org.elasticsearch.node.NodeBuilder.*;
 
 
 import models.*;
 
 public class Occurrences extends Controller {
 
   
   //public static int pagesize = 50;
   
   public static Client setESClient()
   {
 	/*** ElasticSearch configuration ***/
 	Settings settings = ImmutableSettings.settingsBuilder()
 		.put("cluster.name", "elasticsearch_index").put("client.transport.sniff", false).build();
 
 	Client client = new TransportClient(settings)
 		.addTransportAddress(new InetSocketTransportAddress(Play.configuration.getProperty("elasticsearch.server"), Integer.parseInt(Play.configuration.getProperty("elasticsearch.server.port"))));
 	System.out.println(client.toString());
 	return client;
   }
 
   public static SearchRequestBuilder buildRequest(Client client, Search search, Integer pagesize, Integer from, boolean withFacets)
   {
 	
 	/*** Query configuration ***/
 	BoolQueryBuilder scientificNameQ = boolQuery();
 	BoolQueryBuilder genusQ = boolQuery();
 	BoolQueryBuilder classificationInterpretedQ = boolQuery();
 	BoolQueryBuilder placeQ = boolQuery();
 	BoolQueryBuilder dateQ = boolQuery();
 	QueryBuilder boundingBoxLatitudeQ = null, boundingBoxLongitudeQ = null;
 	BoolQueryBuilder boundingBoxQ = boolQuery();
 
 	if (!search.boundingBoxes.isEmpty())
 	{
 	  for (int i = 0; i < search.boundingBoxes.size(); ++i)
 	  {
 	    boundingBoxLatitudeQ = rangeQuery("decimalLatitude_interpreted").from(search.boundingBoxes.get(i)[0]).to(search.boundingBoxes.get(i)[2]);
 		boundingBoxLongitudeQ = rangeQuery("decimalLongitude_interpreted").from(search.boundingBoxes.get(i)[1]).to(search.boundingBoxes.get(i)[3]);
		boundingBoxQ = boundingBoxQ.should(boolQuery().must(boundingBoxLatitudeQ).must(boundingBoxLongitudeQ));
 	  }
 	}
 	
 	/***
 	 * Taxas Query
 	 */
 	for (int i = 0; i < search.taxas.size(); ++i)
 	{ 
 	  if (search.taxas.get(i).split(" ").length > 1)
 	  {
 		scientificNameQ = scientificNameQ.should(textQuery("scientificName", search.taxas.get(i)).operator(Operator.AND));	
 		classificationInterpretedQ = classificationInterpretedQ
 				.should(textQuery("specificEpithet_interpreted", search.taxas.get(i)).operator(Operator.AND))
 				.should(textQuery("ecatConceptId", search.taxas.get(i)).operator(Operator.AND));
 	  }
 	  else
 	  {
 		genusQ = genusQ.should(textQuery("genus", search.taxas.get(i)).operator(Operator.AND));
 		scientificNameQ = scientificNameQ.should(textQuery("scientificName", search.taxas.get(i)).operator(Operator.AND));	 
 		classificationInterpretedQ = classificationInterpretedQ
 				.should(textQuery("kingdom_interpreted", search.taxas.get(i)).operator(Operator.AND))
 				.should(textQuery("phylum_interpreted", search.taxas.get(i)).operator(Operator.AND))
 				.should(textQuery("classs_interpreted", search.taxas.get(i)).operator(Operator.AND))
 				.should(textQuery("orderr_interpreted", search.taxas.get(i)).operator(Operator.AND))		  
 				.should(textQuery("family_interpreted", search.taxas.get(i)).operator(Operator.AND))
 				.should(textQuery("genus_interpreted", search.taxas.get(i)).operator(Operator.AND))
 			  	.should(textQuery("ecatConceptId", search.taxas.get(i)).operator(Operator.AND));
 	  }
 	  
 	}
 	/**
 	 * Place query	
 	 */
 	if (!search.places.isEmpty())
 	{ 
 	  for (String place : search.places)
 	  {
 	    placeQ = placeQ
 		  .should(textQuery("locality", place).operator(Operator.AND).analyzer("french"))
 		  .should(textQuery("county", place).operator(Operator.AND).analyzer("french"))
 	  	  .should(textQuery("country", place))
 	      .should(textQuery("countryCode", place).analyzer("french"))
 	  	  .should(textQuery("stateProvince", place).operator(Operator.AND).analyzer("french"));
 	  }
 	}
 	if (!search.placesText.isEmpty())
 	{
 	  for (String placeText : search.placesText)
 	  {
 	    placeQ = placeQ
 		  .should(textQuery("locality", placeText).operator(Operator.AND))
 	  	  .should(textQuery("county", search.placeText).operator(Operator.AND))
 	  	  .should(textQuery("stateProvince", search.placeText).operator(Operator.AND));
 	  }
 	}
 	if (!search.boundingBoxes.isEmpty() && search.onlyWithCoordinates == false)
 	{
 	  placeQ = placeQ.should(boundingBoxQ);
 	}
 	else if (!search.boundingBoxes.isEmpty() && search.onlyWithCoordinates == true)
 	{
 	  placeQ = placeQ.must(boundingBoxQ);
 	}
 	
 	/**
 	 * Date query
 	 */
 	if (search.fromDate != null && search.toDate != null)
 	{	  
 	  dateQ = dateQ.must(rangeQuery("year_interpreted").from(search.fromDate).to(search.toDate));
 	}
 	
 	
 	BoolQueryBuilder q = null;
 	if (search.taxas.size() > 0 || !search.places.isEmpty() || !search.boundingBoxes.isEmpty() || (search.fromDate != null && search.toDate != null))
 	{
 	  q = boolQuery();
 	  if (search.taxas.size() > 0)
 	  {
 	    q = q.must(boolQuery()
 				.should(scientificNameQ)
 				.should(classificationInterpretedQ)
 				.should(genusQ));
 	  }
 	  if (!search.places.isEmpty() || !search.boundingBoxes.isEmpty())
 	  {
 		q = q.must(boolQuery()	    
 				.should(placeQ));		  
 	  }
 	  if (search.fromDate != null && search.toDate != null)
 	  {
 		q = q.must(boolQuery().must(dateQ)); 	 
 	  }
 	}
 	
 	/*** Filters ***/
 	OrFilterBuilder datasetF = new OrFilterBuilder();
 	FilterBuilder coordinatesF = null;
 
 	if ((search.datasetsIds.size() > 0))
 	{ 
 	  for (int i = 0; i < search.datasetsIds.size(); ++i)
 	  {
 		datasetF.add(boolFilter().must(termFilter("dataset", search.datasetsIds.get(i))));	
 	  }
 	}
 	if (search.onlyWithCoordinates || !search.boundingBoxes.isEmpty())
 	{	
 	  coordinatesF = boolFilter()
 		  			.must(existsFilter("decimalLatitude_interpreted")).must(existsFilter("decimalLongitude_interpreted"))
 		  			.must(notFilter(boolFilter()
 		  					.must(queryFilter(termQuery("decimalLongitude_interpreted", 0)))
 		  					.must(queryFilter(termQuery("decimalLatitude_interpreted", 0)))));  
 	}
 
 	BoolFilterBuilder f = boolFilter();
 
 	if (search.onlyWithCoordinates == true)
 	{
 	  f = f.must(coordinatesF);	  
 	}
 	if (search.datasetsIds.size() > 0)
 	{
 	  f = f.must(datasetF);	  
 	}
 
 	/***
 	 * This facet is working as a SQL "group by ecatConceptId"
 	 */
 	TermsFacetBuilder ecatFacetBuilder = new TermsFacetBuilder("ecatConceptId").size(20);
 	ecatFacetBuilder.field("ecatConceptId");
 	if (search.onlyWithCoordinates == true || search.datasetsIds.size() > 0)
 	{
 	  ecatFacetBuilder.facetFilter(f);
 	}
 
 	/***
 	 * This facet is working as a SQL "group by dataset"
 	 */
 	TermsFacetBuilder datasetFacetBuilder = new TermsFacetBuilder("dataset");
 	datasetFacetBuilder.field("dataset");
 	if (search.onlyWithCoordinates == true || search.datasetsIds.size() > 0)
 	{
 	  datasetFacetBuilder.facetFilter(f);
 	}
 	
 	/***
 	 * This facet is working as a SQL "group by year"
 	 */
 	TermsFacetBuilder yearFacetBuilder = new TermsFacetBuilder("year");
 	yearFacetBuilder.field("year_interpreted");
 	if (search.onlyWithCoordinates == true || search.datasetsIds.size() > 0)
 	{
 	  yearFacetBuilder.facetFilter(f);
 	}
 	
 	if (q != null || search.datasetsIds.size() > 0)
 	{
 	  SearchRequestBuilder searchRequest = client.prepareSearch("idx_occurrence").setFrom(from).setSize(pagesize).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(q);
 	  if (withFacets) searchRequest.addFacet(yearFacetBuilder).addFacet(ecatFacetBuilder).addFacet(datasetFacetBuilder);
 	  if (search.onlyWithCoordinates == true || search.datasetsIds.size() > 0)
 	  {
 	    searchRequest = searchRequest.setFilter(f);
 	  }
 		
 		
 	  System.out.println(searchRequest);
 	  return searchRequest;
 	}
 	else return null;
 	
   }
 
   public static void search(String taxaSearch, String placeSearch, String datasetSearch, String dateSearch, boolean onlyWithCoordinates, Integer from) 
   {   
 	int pagesize = 100;
 	if (from == null) from = 0;
 	
 	Client client = setESClient();
 	Search search = Search.parser(taxaSearch, placeSearch, datasetSearch, dateSearch, onlyWithCoordinates);
 	SearchRequestBuilder searchRequest = buildRequest(client, search, pagesize, from, true);
 	
 	SearchResponse response;
 	
 	if (searchRequest != null)
 	{
 	  response = searchRequest.execute().actionGet();
 	  List<Occurrence> occurrences = new ArrayList<Occurrence>();
 		Long nbHits = response.getHits().getTotalHits();
 		NameParser nameParser = new NameParser();
 		
 		for (SearchHit hit : response.getHits()) {   
 		  Occurrence occurrence = new Occurrence();	
 		  occurrence.id = (Integer) hit.getSource().get("_id");
 		  occurrence.scientificName = (String) hit.getSource().get("scientificName");
 		  occurrence.catalogNumber = (String) hit.getSource().get("catalogNumber");
 		  
 		  occurrence.decimalLatitude = (String) hit.getSource().get("decimalLatitude");
 		  occurrence.decimalLongitude = (String) hit.getSource().get("decimalLongitude");	
 		  
 		  ParsedName<String> parsedNameOriginal = nameParser.parse(occurrence.scientificName);
 		  ParsedName<String> parsedNameInterpreted = nameParser.parse((String) hit.getSource().get("specificEpithet_interpreted"));
 		  try
 		  {
 			if (!(parsedNameOriginal.genusOrAbove.equals(parsedNameInterpreted.genusOrAbove)) || !(parsedNameOriginal.specificEpithet.equals(parsedNameInterpreted.specificEpithet)))
 			{
 			  occurrence.specificEpithet_interpreted = (String) hit.getSource().get("specificEpithet_interpreted");
 			}
 		  }
 		  catch (Exception e) {}
 		  DBRef dbRef = (DBRef) JSON.parse((String) hit.getSource().get("dataset"));
 		  String dataset_id = (String) dbRef.getId();
 		  Dataset dataset = Dataset.findById(dataset_id);
 		  occurrence.dataset = dataset;
 		  occurrence.score = hit.getScore();
 		  occurrences.add(occurrence);
 		  if (occurrences.size() >= pagesize) break;        
 		}
 		
 		/***
 		 * ecat facet
 		 */
 		TermsFacet facet = response.getFacets().facet("ecatConceptId");
 		List<Map<String,Object>> frequentTaxas = new ArrayList<Map<String,Object>>();
 		/***
 		 * Renders (max 10) taxas and their occurrences count that are matching with the request
 		 */
 		
 		for (Entry entry : facet.entries())
 		{
 		  Map<String, Object> frequentTaxa = new HashMap<String, Object>();
 		  Taxa taxa = new Taxa();
 		  Taxas.ecatInformation(Long.parseLong(entry.getTerm()), taxa);
 		  if (taxa.scientificName != null) 
 		  {
 			frequentTaxa.put("taxonId", Long.parseLong(entry.getTerm()));
 			frequentTaxa.put("scientificName", taxa.scientificName);
 			frequentTaxa.put("canonicalName", taxa.canonicalName);
 			frequentTaxa.put("count", entry.getCount());
 			frequentTaxas.add(frequentTaxa);
 		  }
 		}
 		
 		/***
 		 * dataset facet
 		 */
 		facet = response.getFacets().facet("dataset");
 		List<Map<String, Object>> frequentDatasets = new ArrayList<Map<String, Object>>();
 		/***
 		 * Renders datasets and their occurrences count that are matching with the request
 		 */
 		Long id;
 		for (Entry entry : facet.entries())
 		{
 		  if (entry.count() > 0)
 		  {
 		    try 
 		    {
 			  id = Long.parseLong(entry.getTerm());
 		    }
 		    catch (NumberFormatException e)
 		    {
 			  continue;
 		    }
 		    Dataset dataset = Dataset.findById(id); 
 		    Map<String, Object> frequentDataset = new HashMap<String, Object>();
 		    frequentDataset.put("id", dataset.id);
 		    frequentDataset.put("name", dataset.name);
 		    frequentDataset.put("title",dataset.title);
 		    frequentDataset.put("count", entry.count());
 		    frequentDatasets.add(frequentDataset);
 		  }
 		}
 		
 		/***
 		 * year facet
 		 */
 		facet = response.getFacets().facet("year");
 		List<Map<String, Object>> frequentYears = new ArrayList<Map<String, Object>>();
 		
 		for (Entry entry : facet.entries())
 		{
 		  Map<String, Object> frequentYear = new HashMap<String, Object>();
 		  frequentYear.put("year", entry.getTerm());
 		  frequentYear.put("count", entry.count());
 		  frequentYears.add(frequentYear);
 		}
 			
 		int occurrencesTotalPages;
 		int current = from/pagesize + 1;
 		from += pagesize;
 		
 		/***
 		 * Close the ElasticSearch Client
 		 */
 		client.close();
 
 		if (nbHits < pagesize) {
 		  pagesize = nbHits.intValue();
 		  occurrencesTotalPages =  1;
 		} else if (nbHits / pagesize < pagesize)
 		{
 		  occurrencesTotalPages = (int) Math.ceil((double) nbHits / pagesize);
 		}
 		else
 		  occurrencesTotalPages = pagesize;
 		
 		if (request.format.equals("json")) {
 		  JsonObject jsonObject = new JsonObject();
 		  Gson gson = new Gson();
 		  jsonObject.addProperty("Occurrences", gson.toJson(frequentTaxas));
 		  jsonObject.addProperty("frequentTaxas", gson.toJson(frequentTaxas));
 		  jsonObject.addProperty("frequentDatasets", gson.toJson(frequentDatasets));
 		  jsonObject.addProperty("frequentYears", gson.toJson(frequentYears));
 		  renderJSON(jsonObject);
 		} 
 		else
 		{
 		  render("Application/Search/occurrences.html", occurrences, search, nbHits, from, occurrencesTotalPages, pagesize, current, frequentTaxas, frequentDatasets, frequentYears);
 		}
 
 	}
 	else render("Application/Search/occurrences.html", null, search, null, from, null, pagesize, null, null, null, null);
 	
   }
   
   public static void show(Integer id) {
 	Client client = setESClient();
 	QueryBuilder q = termQuery("_id", id);
 	SearchResponse response = client.prepareSearch("idx_occurrence").setSearchType(SearchType.DEFAULT).setQuery(q).setExplain(true).execute().actionGet();
 	Occurrence occurrence = new Occurrence();
 	occurrence.typee = (String) response.getHits().getAt(0).getSource().get("typee");
 	occurrence.modified = (String) response.getHits().getAt(0).getSource().get("modified");
 	occurrence.language = (String) response.getHits().getAt(0).getSource().get("language");
 	occurrence.rights = (String) response.getHits().getAt(0).getSource().get("rights");
 	occurrence.rightsHolder = (String) response.getHits().getAt(0).getSource().get("rightsHolder");
 	occurrence.accessRights = (String) response.getHits().getAt(0).getSource().get("accessRights");
 	occurrence.bibliographicCitation = (String) response.getHits().getAt(0).getSource().get("bibliographicCitation");
 	occurrence.referencess = (String) response.getHits().getAt(0).getSource().get("referencess");
 	occurrence.institutionID = (String) response.getHits().getAt(0).getSource().get("institutionID");
 	occurrence.collectionID = (String) response.getHits().getAt(0).getSource().get("collectionID");
 	occurrence.datasetID = (String) response.getHits().getAt(0).getSource().get("datasetID");
 	occurrence.institutionCode = (String) response.getHits().getAt(0).getSource().get("institutionCode");
 	occurrence.collectionCode = (String) response.getHits().getAt(0).getSource().get("collectionCode");
 	occurrence.datasetName = (String) response.getHits().getAt(0).getSource().get("datasetName");
 	occurrence.ownerInstitutionCode = (String) response.getHits().getAt(0).getSource().get("ownerInstitutionCode");
 	occurrence.basisOfRecord = (String) response.getHits().getAt(0).getSource().get("basisOfRecord");
 	occurrence.informationWithheld = (String) response.getHits().getAt(0).getSource().get("informationWithheld");
 	occurrence.dataGeneralizations = (String) response.getHits().getAt(0).getSource().get("dataGeneralizations");
 	occurrence.dynamicProperties = (String) response.getHits().getAt(0).getSource().get("dynamicProperties");
 	occurrence.occurrenceID = (String) response.getHits().getAt(0).getSource().get("occurrenceID");
 	occurrence.catalogNumber = (String) response.getHits().getAt(0).getSource().get("catalogNumber");
 	occurrence.occurrenceRemarks = (String) response.getHits().getAt(0).getSource().get("occurrenceRemarks");
 	occurrence.recordNumber = (String) response.getHits().getAt(0).getSource().get("recordNumber");
 	occurrence.recordedBy = (String) response.getHits().getAt(0).getSource().get("recordedBy");
 	occurrence.individualID = (String) response.getHits().getAt(0).getSource().get("individualID");
 	occurrence.individualCount = (String) response.getHits().getAt(0).getSource().get("individualCount");
 	occurrence.sex = (String) response.getHits().getAt(0).getSource().get("sex");
 	occurrence.lifeStage = (String) response.getHits().getAt(0).getSource().get("lifeStage");
 	occurrence.reproductiveCondition = (String) response.getHits().getAt(0).getSource().get("reproductiveCondition");
 	occurrence.behavior = (String) response.getHits().getAt(0).getSource().get("behavior");
 	occurrence.establishmentMeans = (String) response.getHits().getAt(0).getSource().get("establishmentMeans");
 	occurrence.occurrenceStatus = (String) response.getHits().getAt(0).getSource().get("occurrenceStatus");
 	occurrence.preparations = (String) response.getHits().getAt(0).getSource().get("preparations");
 	occurrence.disposition = (String) response.getHits().getAt(0).getSource().get("disposition");
 	occurrence.otherCatalogNumbers = (String) response.getHits().getAt(0).getSource().get("otherCatalogNumbers");
 	occurrence.previousIdentifications = (String) response.getHits().getAt(0).getSource().get("previousIdentifications");
 	occurrence.associatedMedia = (String) response.getHits().getAt(0).getSource().get("associatedMedia");
 	occurrence.associatedReferences = (String) response.getHits().getAt(0).getSource().get("associatedReferences");
 	occurrence.associatedOccurrences = (String) response.getHits().getAt(0).getSource().get("associatedOccurrences");
 	occurrence.associatedSequences = (String) response.getHits().getAt(0).getSource().get("associatedSequences");
 	occurrence.associatedTaxa = (String) response.getHits().getAt(0).getSource().get("associatedTaxa");
 	occurrence.eventID = (String) response.getHits().getAt(0).getSource().get("eventID");
 	occurrence.samplingProtocol = (String) response.getHits().getAt(0).getSource().get("samplingProtocol");
 	occurrence.samplingEffort = (String) response.getHits().getAt(0).getSource().get("samplingEffort");
 	occurrence.eventDate = (String) response.getHits().getAt(0).getSource().get("eventDate");
 	occurrence.eventTime = (String) response.getHits().getAt(0).getSource().get("eventTime");
 	occurrence.startDayOfYear = (String) response.getHits().getAt(0).getSource().get("startDayOfYear");
 	occurrence.endDayofYear = (String) response.getHits().getAt(0).getSource().get("endDayofYear");
 	occurrence.year = (String) response.getHits().getAt(0).getSource().get("year");
 	occurrence.month = (String) response.getHits().getAt(0).getSource().get("month");
 	occurrence.day = (String) response.getHits().getAt(0).getSource().get("day");
 	occurrence.verbatimEventDate = (String) response.getHits().getAt(0).getSource().get("verbatimEventDate");
 	occurrence.habitat = (String) response.getHits().getAt(0).getSource().get("habitat");
 	occurrence.fieldNumber = (String) response.getHits().getAt(0).getSource().get("fieldNumber");
 	occurrence.fieldNotes = (String) response.getHits().getAt(0).getSource().get("fieldNotes");
 	occurrence.eventRemarks = (String) response.getHits().getAt(0).getSource().get("eventRemarks");
 	occurrence.locationID = (String) response.getHits().getAt(0).getSource().get("locationID");
 	occurrence.higherGeographyID = (String) response.getHits().getAt(0).getSource().get("higherGeographyID");
 	occurrence.higherGeography = (String) response.getHits().getAt(0).getSource().get("higherGeography");
 	occurrence.continent = (String) response.getHits().getAt(0).getSource().get("continent");
 	occurrence.waterBody = (String) response.getHits().getAt(0).getSource().get("waterBody");
 	occurrence.islandGroup = (String) response.getHits().getAt(0).getSource().get("islandGroup");
 	occurrence.island = (String) response.getHits().getAt(0).getSource().get("island");
 	occurrence.country = (String) response.getHits().getAt(0).getSource().get("country");
 	occurrence.countryCode = (String) response.getHits().getAt(0).getSource().get("countryCode");
 	occurrence.stateProvince = (String) response.getHits().getAt(0).getSource().get("stateProvince");
 	occurrence.county = (String) response.getHits().getAt(0).getSource().get("county");
 	occurrence.municipality = (String) response.getHits().getAt(0).getSource().get("municipality");
 	occurrence.locality = (String) response.getHits().getAt(0).getSource().get("locality");
 	occurrence.verbatimLocality = (String) response.getHits().getAt(0).getSource().get("verbatimLocality");
 	occurrence.verbatimElevation = (String) response.getHits().getAt(0).getSource().get("verbatimElevation");
 	occurrence.minimumElevationInMeters = (String) response.getHits().getAt(0).getSource().get("minimumElevationInMeters");
 	occurrence.maximumElevationInMeters = (String) response.getHits().getAt(0).getSource().get("maximumElevationInMeters");
 	occurrence.verbatimDepth = (String) response.getHits().getAt(0).getSource().get("verbatimDepth");
 	occurrence.minimumDepthInMeters = (String) response.getHits().getAt(0).getSource().get("minimumDepthInMeters");
 	occurrence.maximumDepthInMeters = (String) response.getHits().getAt(0).getSource().get("maximumDepthInMeters");
 	occurrence.minimumDistanceAboveSurfaceInMeters = (String) response.getHits().getAt(0).getSource().get("minimumDistanceAboveSurfaceInMeters");
 	occurrence.maximumDistanceAboveSurfaceInMeters = (String) response.getHits().getAt(0).getSource().get("maximumDistanceAboveSurfaceInMeters");
 	occurrence.locationAccordingTo = (String) response.getHits().getAt(0).getSource().get("locationAccordingTo");
 	occurrence.locationRemarks = (String) response.getHits().getAt(0).getSource().get("locationRemarks");
 	occurrence.verbatimCoordinates = (String) response.getHits().getAt(0).getSource().get("verbatimCoordinates");
 	occurrence.verbatimLatitude = (String) response.getHits().getAt(0).getSource().get("verbatimLatitude");
 	occurrence.verbatimLongitude = (String) response.getHits().getAt(0).getSource().get("verbatimLongitude");
 	occurrence.verbatimCoordinateSystem = (String) response.getHits().getAt(0).getSource().get("verbatimCoordinateSystem");
 	occurrence.verbatimSRS = (String) response.getHits().getAt(0).getSource().get("verbatimSRS");
 	occurrence.decimalLatitude = (String) response.getHits().getAt(0).getSource().get("decimalLatitude");
 	occurrence.decimalLongitude = (String) response.getHits().getAt(0).getSource().get("decimalLongitude");
 	occurrence.geodeticDatum = (String) response.getHits().getAt(0).getSource().get("geodeticDatum");
 	occurrence.coordinateUncertaintyInMeters = (String) response.getHits().getAt(0).getSource().get("coordinateUncertaintyInMeters");
 	occurrence.coordinatePrecision = (String) response.getHits().getAt(0).getSource().get("coordinatePrecision");
 	occurrence.pointRadiusSpatialFit = (String) response.getHits().getAt(0).getSource().get("pointRadiusSpatialFit");
 	occurrence.footprintWKT = (String) response.getHits().getAt(0).getSource().get("footprintWKT");
 	occurrence.footprintSRS = (String) response.getHits().getAt(0).getSource().get("footprintSRS");
 	occurrence.footprintSpatialFit = (String) response.getHits().getAt(0).getSource().get("footprintSpatialFit");
 	occurrence.georeferencedBy = (String) response.getHits().getAt(0).getSource().get("georeferencedBy");
 	occurrence.georeferencedDate = (String) response.getHits().getAt(0).getSource().get("georeferencedDate");
 	occurrence.georeferenceProtocol = (String) response.getHits().getAt(0).getSource().get("georeferenceProtocol");
 	occurrence.georeferenceSources = (String) response.getHits().getAt(0).getSource().get("georeferenceSources");
 	occurrence.georeferenceVerificationStatus = (String) response.getHits().getAt(0).getSource().get("georeferenceVerificationStatus");
 	occurrence.georeferenceRemarks = (String) response.getHits().getAt(0).getSource().get("georeferenceRemarks");
 	occurrence.geologicalContextID = (String) response.getHits().getAt(0).getSource().get("geologicalContextID");
 	occurrence.earliestEonOrLowestEonothem = (String) response.getHits().getAt(0).getSource().get("earliestEonOrLowestEonothem");
 	occurrence.latestEonOrHighestEonothem = (String) response.getHits().getAt(0).getSource().get("latestEonOrHighestEonothem");
 	occurrence.earliestEraOrLowestErathem = (String) response.getHits().getAt(0).getSource().get("earliestEraOrLowestErathem");
 	occurrence.latestEraOrHighestErathem = (String) response.getHits().getAt(0).getSource().get("latestEraOrHighestErathem");
 	occurrence.earliestPeriodOrLowestSystem = (String) response.getHits().getAt(0).getSource().get("earliestPeriodOrLowestSystem");
 	occurrence.latestPeriodOrHighestSystem = (String) response.getHits().getAt(0).getSource().get("latestPeriodOrHighestSystem");
 	occurrence.earliestEpochOrLowestSeries = (String) response.getHits().getAt(0).getSource().get("earliestEpochOrLowestSeries");
 	occurrence.latestEpochOrHighestSeries = (String) response.getHits().getAt(0).getSource().get("latestEpochOrHighestSeries");
 	occurrence.earliestAgeOrLowestStage = (String) response.getHits().getAt(0).getSource().get("earliestAgeOrLowestStage");
 	occurrence.latestAgeOrHighestStage = (String) response.getHits().getAt(0).getSource().get("latestAgeOrHighestStage");
 	occurrence.lowestBiostratigraphicZone = (String) response.getHits().getAt(0).getSource().get("lowestBiostratigraphicZone");
 	occurrence.highestBiostratigraphicZone = (String) response.getHits().getAt(0).getSource().get("highestBiostratigraphicZone");
 	occurrence.lithostratigraphicTerms = (String) response.getHits().getAt(0).getSource().get("lithostratigraphicTerms");
 	occurrence.groupp = (String) response.getHits().getAt(0).getSource().get("groupp");
 	occurrence.formation = (String) response.getHits().getAt(0).getSource().get("formation");
 	occurrence.member = (String) response.getHits().getAt(0).getSource().get("member");
 	occurrence.bed = (String) response.getHits().getAt(0).getSource().get("bed");
 	occurrence.identificationID = (String) response.getHits().getAt(0).getSource().get("identificationID");
 	occurrence.identifiedBy = (String) response.getHits().getAt(0).getSource().get("identifiedBy");
 	occurrence.dateIdentified = (String) response.getHits().getAt(0).getSource().get("dateIdentified");
 	occurrence.identificationVerificationStatus = (String) response.getHits().getAt(0).getSource().get("identificationVerificationStatus");
 	occurrence.identificationRemarks = (String) response.getHits().getAt(0).getSource().get("identificationRemarks");
 	occurrence.identificationQualifier = (String) response.getHits().getAt(0).getSource().get("identificationQualifier");
 	occurrence.typeStatus = (String) response.getHits().getAt(0).getSource().get("typeStatus");
 	occurrence.taxonID = (String) response.getHits().getAt(0).getSource().get("taxonID");
 	occurrence.scientificNameID = (String) response.getHits().getAt(0).getSource().get("scientificNameID");
 	occurrence.acceptedNameUsageID = (String) response.getHits().getAt(0).getSource().get("acceptedNameUsageID");
 	occurrence.parentNameUsageID = (String) response.getHits().getAt(0).getSource().get("parentNameUsageID");
 	occurrence.originalNameUsageID = (String) response.getHits().getAt(0).getSource().get("originalNameUsageID");
 	occurrence.nameAccordingToID = (String) response.getHits().getAt(0).getSource().get("nameAccordingToID");
 	occurrence.namePublishedInID = (String) response.getHits().getAt(0).getSource().get("namePublishedInID");
 	occurrence.taxonConceptID = (String) response.getHits().getAt(0).getSource().get("taxonConceptID");
 	occurrence.scientificName = (String) response.getHits().getAt(0).getSource().get("scientificName");
 	occurrence.acceptedNameUsage = (String) response.getHits().getAt(0).getSource().get("acceptedNameUsage");
 	occurrence.parentNameUsage = (String) response.getHits().getAt(0).getSource().get("parentNameUsage");
 	occurrence.originalNameUsage = (String) response.getHits().getAt(0).getSource().get("originalNameUsage");
 	occurrence.nameAccordingTo = (String) response.getHits().getAt(0).getSource().get("nameAccordingTo");
 	occurrence.namePublishedIn = (String) response.getHits().getAt(0).getSource().get("namePublishedIn");
 	occurrence.namePublishedInYear = (String) response.getHits().getAt(0).getSource().get("namePublishedInYear");
 	occurrence.higherClassification = (String) response.getHits().getAt(0).getSource().get("higherClassification");
 	occurrence.kingdom = (String) response.getHits().getAt(0).getSource().get("kingdom");
 	occurrence.phylum = (String) response.getHits().getAt(0).getSource().get("phylum");
 	occurrence.classs = (String) response.getHits().getAt(0).getSource().get("classs");
 	occurrence.orderr = (String) response.getHits().getAt(0).getSource().get("orderr");
 	occurrence.family = (String) response.getHits().getAt(0).getSource().get("family");
 	occurrence.genus = (String) response.getHits().getAt(0).getSource().get("genus");
 	occurrence.subgenus = (String) response.getHits().getAt(0).getSource().get("subgenus");
 	occurrence.specificEpithet = (String) response.getHits().getAt(0).getSource().get("specificEpithet");
 	occurrence.infraSpecificEpithet = (String) response.getHits().getAt(0).getSource().get("infraSpecificEpithet");
 	occurrence.kingdom_interpreted = (String) response.getHits().getAt(0).getSource().get("kingdom_interpreted");
 	occurrence.phylum_interpreted = (String) response.getHits().getAt(0).getSource().get("phylum_interpreted");
 	occurrence.classs_interpreted = (String) response.getHits().getAt(0).getSource().get("classs_interpreted");
 	occurrence.orderr_interpreted = (String) response.getHits().getAt(0).getSource().get("orderr_interpreted");
 	occurrence.family_interpreted = (String) response.getHits().getAt(0).getSource().get("family_interpreted");
 	occurrence.genus_interpreted = (String) response.getHits().getAt(0).getSource().get("genus_interpreted");
 	occurrence.subgenus_interpreted = (String) response.getHits().getAt(0).getSource().get("subgenus_interpreted");
 	occurrence.specificEpithet_interpreted = (String) response.getHits().getAt(0).getSource().get("specificEpithet_interpreted");
 	occurrence.infraSpecificEpithet_interpreted = (String) response.getHits().getAt(0).getSource().get("infraSpecificEpithet_interpreted");
 	occurrence.taxonRank = (String) response.getHits().getAt(0).getSource().get("taxonRank");
 	occurrence.verbatimTaxonRank = (String) response.getHits().getAt(0).getSource().get("verbatimTaxonRank");
 	occurrence.scientificNameAuthorship = (String) response.getHits().getAt(0).getSource().get("scientificNameAuthorship");
 	occurrence.vernacularName = (String) response.getHits().getAt(0).getSource().get("vernacularName");
 	occurrence.nomenclaturalCode = (String) response.getHits().getAt(0).getSource().get("nomenclaturalCode");
 	occurrence.taxonomicStatus = (String) response.getHits().getAt(0).getSource().get("taxonomicStatus");
 	occurrence.nomenclaturalStatus = (String) response.getHits().getAt(0).getSource().get("nomenclaturalStatus");
 	occurrence.taxonRemarks = (String) response.getHits().getAt(0).getSource().get("taxonRemarks");
 	occurrence.taxonStatus = (String) response.getHits().getAt(0).getSource().get("taxonStatus");
 
 	DBRef dbRef = (DBRef) JSON.parse((String) response.getHits().getAt(0).getSource().get("dataset"));
 	String dataset_id = (String) dbRef.getId();
 	Dataset dataset = Dataset.findById(dataset_id);
 	occurrence.dataset = dataset;
 
 	client.close();
 
 	Taxa taxa = Taxas.getTaxonomy(occurrence);
 
 	render(occurrence, taxa);
   } 
 
   
   public static boolean createCSV(String taxaSearch, String placeSearch, String datasetSearch, String dateSearch, boolean onlyWithCoordinates, String link) throws IOException
   {
 	int pagesize = 500;
 
 	  Client client = setESClient();
 	  Search search = Search.parser(taxaSearch, placeSearch, datasetSearch,
 		  dateSearch, onlyWithCoordinates);
 	  // SearchRequestBuilder searchRequest = buildRequest(client, search,
 	  // pagesize, 0, true);
 
 	  SearchResponse response;
 	  List<Occurrence> occurrences = new ArrayList<Occurrence>();
 	  response = buildRequest(client, search, pagesize, 0, true).execute()
 		  .actionGet();
 	  Long nbHits = response.getHits().getTotalHits();
 	  FileWriter writer;
 
 	  File f = new File(link);
 	  writer = new FileWriter(link);
 	  link = f.getName();
 	  writer.append("ID").append('\t').append("typee").append('\t')
 		  .append("modified").append('\t').append("language").append('\t')
 		  .append("rights").append('\t').append("rightsHolder").append('\t')
 		  .append("accessRights").append('\t').append("bibliographicCitation")
 		  .append('\t').append("referencess").append('\t')
 		  .append("institutionID").append('\t').append("collectionID")
 		  .append('\t').append("datasetID").append('\t')
 		  .append("institutionCode").append('\t').append("collectionCode")
 		  .append('\t').append("datasetName").append('\t')
 		  .append("ownerInstitutionCode").append('\t').append("basisOfRecord")
 		  .append('\t').append("informationWithheld").append('\t')
 		  .append("dataGeneralizations").append('\t')
 		  .append("dynamicProperties").append('\t').append("occurrenceID")
 		  .append('\t').append("catalogNumber").append('\t')
 		  .append("occurrenceRemarks").append('\t').append("recordNumber")
 		  .append('\t').append("recordedBy").append('\t')
 		  .append("individualID").append('\t').append("individualCount")
 		  .append('\t').append("sex").append('\t').append("lifeStage")
 		  .append('\t').append("reproductiveCondition").append('\t')
 		  .append("behavior").append('\t').append("establishmentMeans")
 		  .append('\t').append("occurrenceStatus").append('\t')
 		  .append("preparations").append('\t').append("disposition")
 		  .append('\t').append("otherCatalogNumbers").append('\t')
 		  .append("previousIdentifications").append('\t')
 		  .append("associatedMedia").append('\t')
 		  .append("associatedReferences").append('\t')
 		  .append("associatedOccurrences").append('\t')
 		  .append("associatedSequences").append('\t').append("associatedTaxa")
 		  .append('\t').append("eventID").append('\t')
 		  .append("samplingProtocol").append('\t').append("samplingEffort")
 		  .append('\t').append("eventDate").append('\t').append("eventTime")
 		  .append('\t').append("startDayOfYear").append('\t')
 		  .append("endDayofYear").append('\t').append("year").append('\t')
 		  .append("month").append('\t').append("day").append('\t')
 		  .append("verbatimEventDate").append('\t').append("habitat")
 		  .append('\t').append("fieldNumber").append('\t').append("fieldNotes")
 		  .append('\t').append("eventRemarks").append('\t')
 		  .append("locationID").append('\t').append("higherGeographyID")
 		  .append('\t').append("higherGeography").append('\t')
 		  .append("continent").append('\t').append("waterBody").append('\t')
 		  .append("islandGroup").append('\t').append("island").append('\t')
 		  .append("country").append('\t').append("countryCode").append('\t')
 		  .append("stateProvince").append('\t').append("county").append('\t')
 		  .append("municipality").append('\t').append("locality").append('\t')
 		  .append("verbatimLocality").append('\t').append("verbatimElevation")
 		  .append('\t').append("minimumElevationInMeters").append('\t')
 		  .append("maximumElevationInMeters").append('\t')
 		  .append("verbatimDepth").append('\t').append("minimumDepthInMeters")
 		  .append('\t').append("maximumDepthInMeters").append('\t')
 		  .append("minimumDistanceAboveSurfaceInMeters").append('\t')
 		  .append("maximumDistanceAboveSurfaceInMeters").append('\t')
 		  .append("locationAccordingTo").append('\t').append("locationRemarks")
 		  .append('\t').append("verbatimCoordinates").append('\t')
 		  .append("verbatimLatitude").append('\t').append("verbatimLongitude")
 		  .append('\t').append("verbatimCoordinateSystem").append('\t')
 		  .append("verbatimSRS").append('\t').append("decimalLatitude")
 		  .append('\t').append("decimalLongitude").append('\t')
 		  .append("geodeticDatum").append('\t')
 		  .append("coordinateUncertaintyInMeters").append('\t')
 		  .append("coordinatePrecision").append('\t')
 		  .append("pointRadiusSpatialFit").append('\t').append("footprintWKT")
 		  .append('\t').append("footprintSRS").append('\t')
 		  .append("footprintSpatialFit").append('\t').append("georeferencedBy")
 		  .append('\t').append("georeferencedDate").append('\t')
 		  .append("georeferenceProtocol").append('\t')
 		  .append("georeferenceSources").append('\t')
 		  .append("georeferenceVerificationStatus").append('\t')
 		  .append("georeferenceRemarks").append('\t')
 		  .append("geologicalContextID").append('\t')
 		  .append("earliestEonOrLowestEonothem").append('\t')
 		  .append("latestEonOrHighestEonothem").append('\t')
 		  .append("earliestEraOrLowestErathem").append('\t')
 		  .append("latestEraOrHighestErathem").append('\t')
 		  .append("earliestPeriodOrLowestSystem").append('\t')
 		  .append("latestPeriodOrHighestSystem").append('\t')
 		  .append("earliestEpochOrLowestSeries").append('\t')
 		  .append("latestEpochOrHighestSeries").append('\t')
 		  .append("earliestAgeOrLowestStage").append('\t')
 		  .append("latestAgeOrHighestStage").append('\t')
 		  .append("lowestBiostratigraphicZone").append('\t')
 		  .append("highestBiostratigraphicZone").append('\t')
 		  .append("lithostratigraphicTerms").append('\t').append("groupp")
 		  .append('\t').append("formation").append('\t').append("member")
 		  .append('\t').append("bed").append('\t').append("identificationID")
 		  .append('\t').append("identifiedBy").append('\t')
 		  .append("dateIdentified").append('\t')
 		  .append("identificationVerificationStatus").append('\t')
 		  .append("identificationRemarks").append('\t')
 		  .append("identificationQualifier").append('\t').append("typeStatus")
 		  .append('\t').append("taxonID").append('\t')
 		  .append("scientificNameID").append('\t')
 		  .append("acceptedNameUsageID").append('\t')
 		  .append("parentNameUsageID").append('\t')
 		  .append("originalNameUsageID").append('\t')
 		  .append("nameAccordingToID").append('\t').append("namePublishedInID")
 		  .append('\t').append("taxonConceptID").append('\t')
 		  .append("scientificName").append('\t').append("acceptedNameUsage")
 		  .append('\t').append("parentNameUsage").append('\t')
 		  .append("originalNameUsage").append('\t').append("nameAccordingTo")
 		  .append('\t').append("namePublishedIn").append('\t')
 		  .append("namePublishedInYear").append('\t')
 		  .append("higherClassification").append('\t').append("kingdom")
 		  .append('\t').append("phylum").append('\t').append("classs")
 		  .append('\t').append("orderr").append('\t').append("family")
 		  .append('\t').append("genus").append('\t').append("subgenus")
 		  .append('\t').append("specificEpithet").append('\t')
 		  .append("infraSpecificEpithet").append('\t')
 		  .append("kingdom_interpreted").append('\t')
 		  .append("phylum_interpreted").append('\t')
 		  .append("classs_interpreted").append('\t')
 		  .append("orderr_interpreted").append('\t')
 		  .append("family_interpreted").append('\t')
 		  .append("genus_interpreted").append('\t')
 		  .append("subgenus_interpreted").append('\t')
 		  .append("specificEpithet_interpreted").append('\t')
 		  .append("infraSpecificEpithet_interpreted").append('\t')
 		  .append("taxonRank").append('\t').append("verbatimTaxonRank")
 		  .append('\t').append("scientificNameAuthorship").append('\t')
 		  .append("vernacularName").append('\t').append("nomenclaturalCode")
 		  .append('\t').append("taxonomicStatus").append('\t')
 		  .append("nomenclaturalStatus").append('\t').append("taxonRemarks")
 		  .append('\t').append("taxonStatus").append('\n');
 
 	  for (int i = 0; i < nbHits; i = i + pagesize) {
 
 		response = buildRequest(client, search, pagesize, i, true).execute()
 			.actionGet();
 
 		for (SearchHit hit : response.getHits()) {
 		  Occurrence occurrence = new Occurrence();
 
 		  occurrence.id = (Integer) hit.getSource().get("_id");
 		  occurrence.typee = (String) hit.getSource().get("typee");
 		  occurrence.modified = (String) hit.getSource().get("modified");
 		  occurrence.language = (String) hit.getSource().get("language");
 		  occurrence.rights = (String) hit.getSource().get("rights");
 		  occurrence.rightsHolder = (String) hit.getSource()
 			  .get("rightsHolder");
 		  occurrence.accessRights = (String) hit.getSource()
 			  .get("accessRights");
 		  occurrence.bibliographicCitation = (String) hit.getSource().get(
 			  "bibliographicCitation");
 		  occurrence.referencess = (String) hit.getSource().get("referencess");
 		  occurrence.institutionID = (String) hit.getSource().get(
 			  "institutionID");
 		  occurrence.collectionID = (String) hit.getSource()
 			  .get("collectionID");
 		  occurrence.datasetID = (String) hit.getSource().get("datasetID");
 		  occurrence.institutionCode = (String) hit.getSource().get(
 			  "institutionCode");
 		  occurrence.collectionCode = (String) hit.getSource().get(
 			  "collectionCode");
 		  occurrence.datasetName = (String) hit.getSource().get("datasetName");
 		  occurrence.ownerInstitutionCode = (String) hit.getSource().get(
 			  "ownerInstitutionCode");
 		  occurrence.basisOfRecord = (String) hit.getSource().get(
 			  "basisOfRecord");
 		  occurrence.informationWithheld = (String) hit.getSource().get(
 			  "informationWithheld");
 		  occurrence.dataGeneralizations = (String) hit.getSource().get(
 			  "dataGeneralizations");
 		  occurrence.dynamicProperties = (String) hit.getSource().get(
 			  "dynamicProperties");
 		  occurrence.occurrenceID = (String) hit.getSource()
 			  .get("occurrenceID");
 		  occurrence.catalogNumber = (String) hit.getSource().get(
 			  "catalogNumber");
 		  occurrence.occurrenceRemarks = (String) hit.getSource().get(
 			  "occurrenceRemarks");
 		  occurrence.recordNumber = (String) hit.getSource()
 			  .get("recordNumber");
 		  occurrence.recordedBy = (String) hit.getSource().get("recordedBy");
 		  occurrence.individualID = (String) hit.getSource()
 			  .get("individualID");
 		  occurrence.individualCount = (String) hit.getSource().get(
 			  "individualCount");
 		  occurrence.sex = (String) hit.getSource().get("sex");
 		  occurrence.lifeStage = (String) hit.getSource().get("lifeStage");
 		  occurrence.reproductiveCondition = (String) hit.getSource().get(
 			  "reproductiveCondition");
 		  occurrence.behavior = (String) hit.getSource().get("behavior");
 		  occurrence.establishmentMeans = (String) hit.getSource().get(
 			  "establishmentMeans");
 		  occurrence.occurrenceStatus = (String) hit.getSource().get(
 			  "occurrenceStatus");
 		  occurrence.preparations = (String) hit.getSource()
 			  .get("preparations");
 		  occurrence.disposition = (String) hit.getSource().get("disposition");
 		  occurrence.otherCatalogNumbers = (String) hit.getSource().get(
 			  "otherCatalogNumbers");
 		  occurrence.previousIdentifications = (String) hit.getSource().get(
 			  "previousIdentifications");
 		  occurrence.associatedMedia = (String) hit.getSource().get(
 			  "associatedMedia");
 		  occurrence.associatedReferences = (String) hit.getSource().get(
 			  "associatedReferences");
 		  occurrence.associatedOccurrences = (String) hit.getSource().get(
 			  "associatedOccurrences");
 		  occurrence.associatedSequences = (String) hit.getSource().get(
 			  "associatedSequences");
 		  occurrence.associatedTaxa = (String) hit.getSource().get(
 			  "associatedTaxa");
 		  occurrence.eventID = (String) hit.getSource().get("eventID");
 		  occurrence.samplingProtocol = (String) hit.getSource().get(
 			  "samplingProtocol");
 		  occurrence.samplingEffort = (String) hit.getSource().get(
 			  "samplingEffort");
 		  occurrence.eventDate = (String) hit.getSource().get("eventDate");
 		  occurrence.eventTime = (String) hit.getSource().get("eventTime");
 		  occurrence.startDayOfYear = (String) hit.getSource().get(
 			  "startDayOfYear");
 		  occurrence.endDayofYear = (String) hit.getSource()
 			  .get("endDayofYear");
 		  occurrence.year = (String) hit.getSource().get("year");
 		  occurrence.month = (String) hit.getSource().get("month");
 		  occurrence.day = (String) hit.getSource().get("day");
 		  occurrence.verbatimEventDate = (String) hit.getSource().get(
 			  "verbatimEventDate");
 		  occurrence.habitat = (String) hit.getSource().get("habitat");
 		  occurrence.fieldNumber = (String) hit.getSource().get("fieldNumber");
 		  occurrence.fieldNotes = (String) hit.getSource().get("fieldNotes");
 		  occurrence.eventRemarks = (String) hit.getSource()
 			  .get("eventRemarks");
 		  occurrence.locationID = (String) hit.getSource().get("locationID");
 		  occurrence.higherGeographyID = (String) hit.getSource().get(
 			  "higherGeographyID");
 		  occurrence.higherGeography = (String) hit.getSource().get(
 			  "higherGeography");
 		  occurrence.continent = (String) hit.getSource().get("continent");
 		  occurrence.waterBody = (String) hit.getSource().get("waterBody");
 		  occurrence.islandGroup = (String) hit.getSource().get("islandGroup");
 		  occurrence.island = (String) hit.getSource().get("island");
 		  occurrence.country = (String) hit.getSource().get("country");
 		  occurrence.countryCode = (String) hit.getSource().get("countryCode");
 		  occurrence.stateProvince = (String) hit.getSource().get(
 			  "stateProvince");
 		  occurrence.county = (String) hit.getSource().get("county");
 		  occurrence.municipality = (String) hit.getSource()
 			  .get("municipality");
 		  occurrence.locality = (String) hit.getSource().get("locality");
 		  occurrence.verbatimLocality = (String) hit.getSource().get(
 			  "verbatimLocality");
 		  occurrence.verbatimElevation = (String) hit.getSource().get(
 			  "verbatimElevation");
 		  occurrence.minimumElevationInMeters = (String) hit.getSource().get(
 			  "minimumElevationInMeters");
 		  occurrence.maximumElevationInMeters = (String) hit.getSource().get(
 			  "maximumElevationInMeters");
 		  occurrence.verbatimDepth = (String) hit.getSource().get(
 			  "verbatimDepth");
 		  occurrence.minimumDepthInMeters = (String) hit.getSource().get(
 			  "minimumDepthInMeters");
 		  occurrence.maximumDepthInMeters = (String) hit.getSource().get(
 			  "maximumDepthInMeters");
 		  occurrence.minimumDistanceAboveSurfaceInMeters = (String) hit
 			  .getSource().get("minimumDistanceAboveSurfaceInMeters");
 		  occurrence.maximumDistanceAboveSurfaceInMeters = (String) hit
 			  .getSource().get("maximumDistanceAboveSurfaceInMeters");
 		  occurrence.locationAccordingTo = (String) hit.getSource().get(
 			  "locationAccordingTo");
 		  occurrence.locationRemarks = (String) hit.getSource().get(
 			  "locationRemarks");
 		  occurrence.verbatimCoordinates = (String) hit.getSource().get(
 			  "verbatimCoordinates");
 		  occurrence.verbatimLatitude = (String) hit.getSource().get(
 			  "verbatimLatitude");
 		  occurrence.verbatimLongitude = (String) hit.getSource().get(
 			  "verbatimLongitude");
 		  occurrence.verbatimCoordinateSystem = (String) hit.getSource().get(
 			  "verbatimCoordinateSystem");
 		  occurrence.verbatimSRS = (String) hit.getSource().get("verbatimSRS");
 		  occurrence.decimalLatitude = (String) hit.getSource().get(
 			  "decimalLatitude");
 		  occurrence.decimalLongitude = (String) hit.getSource().get(
 			  "decimalLongitude");
 		  occurrence.geodeticDatum = (String) hit.getSource().get(
 			  "geodeticDatum");
 		  occurrence.coordinateUncertaintyInMeters = (String) hit.getSource()
 			  .get("coordinateUncertaintyInMeters");
 		  occurrence.coordinatePrecision = (String) hit.getSource().get(
 			  "coordinatePrecision");
 		  occurrence.pointRadiusSpatialFit = (String) hit.getSource().get(
 			  "pointRadiusSpatialFit");
 		  occurrence.footprintWKT = (String) hit.getSource()
 			  .get("footprintWKT");
 		  occurrence.footprintSRS = (String) hit.getSource()
 			  .get("footprintSRS");
 		  occurrence.footprintSpatialFit = (String) hit.getSource().get(
 			  "footprintSpatialFit");
 		  occurrence.georeferencedBy = (String) hit.getSource().get(
 			  "georeferencedBy");
 		  occurrence.georeferencedDate = (String) hit.getSource().get(
 			  "georeferencedDate");
 		  occurrence.georeferenceProtocol = (String) hit.getSource().get(
 			  "georeferenceProtocol");
 		  occurrence.georeferenceSources = (String) hit.getSource().get(
 			  "georeferenceSources");
 		  occurrence.georeferenceVerificationStatus = (String) hit.getSource()
 			  .get("georeferenceVerificationStatus");
 		  occurrence.georeferenceRemarks = (String) hit.getSource().get(
 			  "georeferenceRemarks");
 		  occurrence.geologicalContextID = (String) hit.getSource().get(
 			  "geologicalContextID");
 		  occurrence.earliestEonOrLowestEonothem = (String) hit.getSource()
 			  .get("earliestEonOrLowestEonothem");
 		  occurrence.latestEonOrHighestEonothem = (String) hit.getSource().get(
 			  "latestEonOrHighestEonothem");
 		  occurrence.earliestEraOrLowestErathem = (String) hit.getSource().get(
 			  "earliestEraOrLowestErathem");
 		  occurrence.latestEraOrHighestErathem = (String) hit.getSource().get(
 			  "latestEraOrHighestErathem");
 		  occurrence.earliestPeriodOrLowestSystem = (String) hit.getSource()
 			  .get("earliestPeriodOrLowestSystem");
 		  occurrence.latestPeriodOrHighestSystem = (String) hit.getSource()
 			  .get("latestPeriodOrHighestSystem");
 		  occurrence.earliestEpochOrLowestSeries = (String) hit.getSource()
 			  .get("earliestEpochOrLowestSeries");
 		  occurrence.latestEpochOrHighestSeries = (String) hit.getSource().get(
 			  "latestEpochOrHighestSeries");
 		  occurrence.earliestAgeOrLowestStage = (String) hit.getSource().get(
 			  "earliestAgeOrLowestStage");
 		  occurrence.latestAgeOrHighestStage = (String) hit.getSource().get(
 			  "latestAgeOrHighestStage");
 		  occurrence.lowestBiostratigraphicZone = (String) hit.getSource().get(
 			  "lowestBiostratigraphicZone");
 		  occurrence.highestBiostratigraphicZone = (String) hit.getSource()
 			  .get("highestBiostratigraphicZone");
 		  occurrence.lithostratigraphicTerms = (String) hit.getSource().get(
 			  "lithostratigraphicTerms");
 		  occurrence.groupp = (String) hit.getSource().get("groupp");
 		  occurrence.formation = (String) hit.getSource().get("formation");
 		  occurrence.member = (String) hit.getSource().get("member");
 		  occurrence.bed = (String) hit.getSource().get("bed");
 		  occurrence.identificationID = (String) hit.getSource().get(
 			  "identificationID");
 		  occurrence.identifiedBy = (String) hit.getSource()
 			  .get("identifiedBy");
 		  occurrence.dateIdentified = (String) hit.getSource().get(
 			  "dateIdentified");
 		  occurrence.identificationVerificationStatus = (String) hit
 			  .getSource().get("identificationVerificationStatus");
 		  occurrence.identificationRemarks = (String) hit.getSource().get(
 			  "identificationRemarks");
 		  occurrence.identificationQualifier = (String) hit.getSource().get(
 			  "identificationQualifier");
 		  occurrence.typeStatus = (String) hit.getSource().get("typeStatus");
 		  occurrence.taxonID = (String) hit.getSource().get("taxonID");
 		  occurrence.scientificNameID = (String) hit.getSource().get(
 			  "scientificNameID");
 		  occurrence.acceptedNameUsageID = (String) hit.getSource().get(
 			  "acceptedNameUsageID");
 		  occurrence.parentNameUsageID = (String) hit.getSource().get(
 			  "parentNameUsageID");
 		  occurrence.originalNameUsageID = (String) hit.getSource().get(
 			  "originalNameUsageID");
 		  occurrence.nameAccordingToID = (String) hit.getSource().get(
 			  "nameAccordingToID");
 		  occurrence.namePublishedInID = (String) hit.getSource().get(
 			  "namePublishedInID");
 		  occurrence.taxonConceptID = (String) hit.getSource().get(
 			  "taxonConceptID");
 		  occurrence.scientificName = (String) hit.getSource().get(
 			  "scientificName");
 		  occurrence.acceptedNameUsage = (String) hit.getSource().get(
 			  "acceptedNameUsage");
 		  occurrence.parentNameUsage = (String) hit.getSource().get(
 			  "parentNameUsage");
 		  occurrence.originalNameUsage = (String) hit.getSource().get(
 			  "originalNameUsage");
 		  occurrence.nameAccordingTo = (String) hit.getSource().get(
 			  "nameAccordingTo");
 		  occurrence.namePublishedIn = (String) hit.getSource().get(
 			  "namePublishedIn");
 		  occurrence.namePublishedInYear = (String) hit.getSource().get(
 			  "namePublishedInYear");
 		  occurrence.higherClassification = (String) hit.getSource().get(
 			  "higherClassification");
 		  occurrence.kingdom = (String) hit.getSource().get("kingdom");
 		  occurrence.phylum = (String) hit.getSource().get("phylum");
 		  occurrence.classs = (String) hit.getSource().get("classs");
 		  occurrence.orderr = (String) hit.getSource().get("orderr");
 		  occurrence.family = (String) hit.getSource().get("family");
 		  occurrence.genus = (String) hit.getSource().get("genus");
 		  occurrence.subgenus = (String) hit.getSource().get("subgenus");
 		  occurrence.specificEpithet = (String) hit.getSource().get(
 			  "specificEpithet");
 		  occurrence.infraSpecificEpithet = (String) hit.getSource().get(
 			  "infraSpecificEpithet");
 		  occurrence.kingdom_interpreted = (String) hit.getSource().get(
 			  "kingdom_interpreted");
 		  occurrence.phylum_interpreted = (String) hit.getSource().get(
 			  "phylum_interpreted");
 		  occurrence.classs_interpreted = (String) hit.getSource().get(
 			  "classs_interpreted");
 		  occurrence.orderr_interpreted = (String) hit.getSource().get(
 			  "orderr_interpreted");
 		  occurrence.family_interpreted = (String) hit.getSource().get(
 			  "family_interpreted");
 		  occurrence.genus_interpreted = (String) hit.getSource().get(
 			  "genus_interpreted");
 		  occurrence.subgenus_interpreted = (String) hit.getSource().get(
 			  "subgenus_interpreted");
 		  occurrence.specificEpithet_interpreted = (String) hit.getSource()
 			  .get("specificEpithet_interpreted");
 		  occurrence.infraSpecificEpithet_interpreted = (String) hit
 			  .getSource().get("infraSpecificEpithet_interpreted");
 		  occurrence.taxonRank = (String) hit.getSource().get("taxonRank");
 		  occurrence.verbatimTaxonRank = (String) hit.getSource().get(
 			  "verbatimTaxonRank");
 		  occurrence.scientificNameAuthorship = (String) hit.getSource().get(
 			  "scientificNameAuthorship");
 		  occurrence.vernacularName = (String) hit.getSource().get(
 			  "vernacularName");
 		  occurrence.nomenclaturalCode = (String) hit.getSource().get(
 			  "nomenclaturalCode");
 		  occurrence.taxonomicStatus = (String) hit.getSource().get(
 			  "taxonomicStatus");
 		  occurrence.nomenclaturalStatus = (String) hit.getSource().get(
 			  "nomenclaturalStatus");
 		  occurrence.taxonRemarks = (String) hit.getSource()
 			  .get("taxonRemarks");
 		  occurrence.taxonStatus = (String) hit.getSource().get("taxonStatus");
 
 		  writer.append(occurrence.id.toString()).append('\t')
 			  .append(occurrence.typee).append('\t')
 			  .append(occurrence.modified).append('\t')
 			  .append(occurrence.language).append('\t')
 			  .append(occurrence.rights).append('\t')
 			  .append(occurrence.rightsHolder).append('\t')
 			  .append(occurrence.accessRights).append('\t')
 			  .append(occurrence.bibliographicCitation).append('\t')
 			  .append(occurrence.referencess).append('\t')
 			  .append(occurrence.institutionID).append('\t')
 			  .append(occurrence.collectionID).append('\t')
 			  .append(occurrence.datasetID).append('\t')
 			  .append(occurrence.institutionCode).append('\t')
 			  .append(occurrence.collectionCode).append('\t')
 			  .append(occurrence.datasetName).append('\t')
 			  .append(occurrence.ownerInstitutionCode).append('\t')
 			  .append(occurrence.basisOfRecord).append('\t')
 			  .append(occurrence.informationWithheld).append('\t')
 			  .append(occurrence.dataGeneralizations).append('\t')
 			  .append(occurrence.dynamicProperties).append('\t')
 			  .append(occurrence.occurrenceID).append('\t')
 			  .append(occurrence.catalogNumber).append('\t')
 			  .append(occurrence.occurrenceRemarks).append('\t')
 			  .append(occurrence.recordNumber).append('\t')
 			  .append(occurrence.recordedBy).append('\t')
 			  .append(occurrence.individualID).append('\t')
 			  .append(occurrence.individualCount).append('\t')
 			  .append(occurrence.sex).append('\t').append(occurrence.lifeStage)
 			  .append('\t').append(occurrence.reproductiveCondition)
 			  .append('\t').append(occurrence.behavior).append('\t')
 			  .append(occurrence.establishmentMeans).append('\t')
 			  .append(occurrence.occurrenceStatus).append('\t')
 			  .append(occurrence.preparations).append('\t')
 			  .append(occurrence.disposition).append('\t')
 			  .append(occurrence.otherCatalogNumbers).append('\t')
 			  .append(occurrence.previousIdentifications).append('\t')
 			  .append(occurrence.associatedMedia).append('\t')
 			  .append(occurrence.associatedReferences).append('\t')
 			  .append(occurrence.associatedOccurrences).append('\t')
 			  .append(occurrence.associatedSequences).append('\t')
 			  .append(occurrence.associatedTaxa).append('\t')
 			  .append(occurrence.eventID).append('\t')
 			  .append(occurrence.samplingProtocol).append('\t')
 			  .append(occurrence.samplingEffort).append('\t')
 			  .append(occurrence.eventDate).append('\t')
 			  .append(occurrence.eventTime).append('\t')
 			  .append(occurrence.startDayOfYear).append('\t')
 			  .append(occurrence.endDayofYear).append('\t')
 			  .append(occurrence.year).append('\t').append(occurrence.month)
 			  .append('\t').append(occurrence.day).append('\t')
 			  .append(occurrence.verbatimEventDate).append('\t')
 			  .append(occurrence.habitat).append('\t')
 			  .append(occurrence.fieldNumber).append('\t')
 			  .append(occurrence.fieldNotes).append('\t')
 			  .append(occurrence.eventRemarks).append('\t')
 			  .append(occurrence.locationID).append('\t')
 			  .append(occurrence.higherGeographyID).append('\t')
 			  .append(occurrence.higherGeography).append('\t')
 			  .append(occurrence.continent).append('\t')
 			  .append(occurrence.waterBody).append('\t')
 			  .append(occurrence.islandGroup).append('\t')
 			  .append(occurrence.island).append('\t')
 			  .append(occurrence.country).append('\t')
 			  .append(occurrence.countryCode).append('\t')
 			  .append(occurrence.stateProvince).append('\t')
 			  .append(occurrence.county).append('\t')
 			  .append(occurrence.municipality).append('\t')
 			  .append(occurrence.locality).append('\t')
 			  .append(occurrence.verbatimLocality).append('\t')
 			  .append(occurrence.verbatimElevation).append('\t')
 			  .append(occurrence.minimumElevationInMeters).append('\t')
 			  .append(occurrence.maximumElevationInMeters).append('\t')
 			  .append(occurrence.verbatimDepth).append('\t')
 			  .append(occurrence.minimumDepthInMeters).append('\t')
 			  .append(occurrence.maximumDepthInMeters).append('\t')
 			  .append(occurrence.minimumDistanceAboveSurfaceInMeters)
 			  .append('\t')
 			  .append(occurrence.maximumDistanceAboveSurfaceInMeters)
 			  .append('\t').append(occurrence.locationAccordingTo).append('\t')
 			  .append(occurrence.locationRemarks).append('\t')
 			  .append(occurrence.verbatimCoordinates).append('\t')
 			  .append(occurrence.verbatimLatitude).append('\t')
 			  .append(occurrence.verbatimLongitude).append('\t')
 			  .append(occurrence.verbatimCoordinateSystem).append('\t')
 			  .append(occurrence.verbatimSRS).append('\t')
 			  .append(occurrence.decimalLatitude).append('\t')
 			  .append(occurrence.decimalLongitude).append('\t')
 			  .append(occurrence.geodeticDatum).append('\t')
 			  .append(occurrence.coordinateUncertaintyInMeters).append('\t')
 			  .append(occurrence.coordinatePrecision).append('\t')
 			  .append(occurrence.pointRadiusSpatialFit).append('\t')
 			  .append(occurrence.footprintWKT).append('\t')
 			  .append(occurrence.footprintSRS).append('\t')
 			  .append(occurrence.footprintSpatialFit).append('\t')
 			  .append(occurrence.georeferencedBy).append('\t')
 			  .append(occurrence.georeferencedDate).append('\t')
 			  .append(occurrence.georeferenceProtocol).append('\t')
 			  .append(occurrence.georeferenceSources).append('\t')
 			  .append(occurrence.georeferenceVerificationStatus).append('\t')
 			  .append(occurrence.georeferenceRemarks).append('\t')
 			  .append(occurrence.geologicalContextID).append('\t')
 			  .append(occurrence.earliestEonOrLowestEonothem).append('\t')
 			  .append(occurrence.latestEonOrHighestEonothem).append('\t')
 			  .append(occurrence.earliestEraOrLowestErathem).append('\t')
 			  .append(occurrence.latestEraOrHighestErathem).append('\t')
 			  .append(occurrence.earliestPeriodOrLowestSystem).append('\t')
 			  .append(occurrence.latestPeriodOrHighestSystem).append('\t')
 			  .append(occurrence.earliestEpochOrLowestSeries).append('\t')
 			  .append(occurrence.latestEpochOrHighestSeries).append('\t')
 			  .append(occurrence.earliestAgeOrLowestStage).append('\t')
 			  .append(occurrence.latestAgeOrHighestStage).append('\t')
 			  .append(occurrence.lowestBiostratigraphicZone).append('\t')
 			  .append(occurrence.highestBiostratigraphicZone).append('\t')
 			  .append(occurrence.lithostratigraphicTerms).append('\t')
 			  .append(occurrence.groupp).append('\t')
 			  .append(occurrence.formation).append('\t')
 			  .append(occurrence.member).append('\t').append(occurrence.bed)
 			  .append('\t').append(occurrence.identificationID).append('\t')
 			  .append(occurrence.identifiedBy).append('\t')
 			  .append(occurrence.dateIdentified).append('\t')
 			  .append(occurrence.identificationVerificationStatus).append('\t')
 			  .append(occurrence.identificationRemarks).append('\t')
 			  .append(occurrence.identificationQualifier).append('\t')
 			  .append(occurrence.typeStatus).append('\t')
 			  .append(occurrence.taxonID).append('\t')
 			  .append(occurrence.scientificNameID).append('\t')
 			  .append(occurrence.acceptedNameUsageID).append('\t')
 			  .append(occurrence.parentNameUsageID).append('\t')
 			  .append(occurrence.originalNameUsageID).append('\t')
 			  .append(occurrence.nameAccordingToID).append('\t')
 			  .append(occurrence.namePublishedInID).append('\t')
 			  .append(occurrence.taxonConceptID).append('\t')
 			  .append(occurrence.scientificName).append('\t')
 			  .append(occurrence.acceptedNameUsage).append('\t')
 			  .append(occurrence.parentNameUsage).append('\t')
 			  .append(occurrence.originalNameUsage).append('\t')
 			  .append(occurrence.nameAccordingTo).append('\t')
 			  .append(occurrence.namePublishedIn).append('\t')
 			  .append(occurrence.namePublishedInYear).append('\t')
 			  .append(occurrence.higherClassification).append('\t')
 			  .append(occurrence.kingdom).append('\t')
 			  .append(occurrence.phylum).append('\t').append(occurrence.classs)
 			  .append('\t').append(occurrence.orderr).append('\t')
 			  .append(occurrence.family).append('\t').append(occurrence.genus)
 			  .append('\t').append(occurrence.subgenus).append('\t')
 			  .append(occurrence.specificEpithet).append('\t')
 			  .append(occurrence.infraSpecificEpithet).append('\t')
 			  .append(occurrence.kingdom_interpreted).append('\t')
 			  .append(occurrence.phylum_interpreted).append('\t')
 			  .append(occurrence.classs_interpreted).append('\t')
 			  .append(occurrence.orderr_interpreted).append('\t')
 			  .append(occurrence.family_interpreted).append('\t')
 			  .append(occurrence.genus_interpreted).append('\t')
 			  .append(occurrence.subgenus_interpreted).append('\t')
 			  .append(occurrence.specificEpithet_interpreted).append('\t')
 			  .append(occurrence.infraSpecificEpithet_interpreted).append('\t')
 			  .append(occurrence.taxonRank).append('\t')
 			  .append(occurrence.verbatimTaxonRank).append('\t')
 			  .append(occurrence.scientificNameAuthorship).append('\t')
 			  .append(occurrence.vernacularName).append('\t')
 			  .append(occurrence.nomenclaturalCode).append('\t')
 			  .append(occurrence.taxonomicStatus).append('\t')
 			  .append(occurrence.nomenclaturalStatus).append('\t')
 			  .append(occurrence.taxonRemarks).append('\t')
 			  .append(occurrence.taxonStatus).append('\n');
 
 		  occurrences.add(occurrence);
 		}
 
 	  }
 	  writer.flush();
 	  writer.close();
 	  return true;
   }
   
   /*** TODO: envoi de mail, zip, clean du dossier ***/
   public static void download(String taxaSearch, String placeSearch,
 	  String datasetSearch, String dateSearch, boolean onlyWithCoordinates,
 	  String mode, String email) throws IOException 
   {
 	
 	/*** Download location ***/
 	String fileName = String.valueOf(((Double) (Math.random() * 100000000)).intValue());
 	String link = Play.configuration.getProperty("download.physical.path") + fileName + ".csv";
 	if (mode != null)
 	{
 	  if (mode.equals("DIRECT")) 
 	  {
 		createCSV(taxaSearch, placeSearch, datasetSearch, dateSearch, onlyWithCoordinates, link);
 		String serverLink = Play.configuration.getProperty("download.server.path") + fileName + ".csv";
 		render("Occurrences/download.html", serverLink, mode);
 	  }
 	  else if (mode.equals("EMAIL") && !email.isEmpty())
 	  { 
 	    DownloadThread downloadThread = new DownloadThread(taxaSearch, placeSearch, datasetSearch, dateSearch, onlyWithCoordinates, email);
 	    downloadThread.start();
 	    Application.search(taxaSearch, placeSearch, String.valueOf(onlyWithCoordinates), datasetSearch, dateSearch);
 	  }
 	}
 	else 
 	{
 	  render("Occurrences/download.html", taxaSearch, placeSearch, datasetSearch, dateSearch, onlyWithCoordinates, mode);
 	}
   }
   
   public static class DownloadThread extends Thread
   {
     String taxaSearch;
 	String placeSearch;
  	String datasetSearch;
  	String dateSearch; 
  	boolean onlyWithCoordinates;
 	String email;
 	
 	 public DownloadThread(String taxaSearch, String placeSearch,
    	  String datasetSearch, String dateSearch, boolean onlyWithCoordinates, String email)
      {
        this.taxaSearch = taxaSearch;
        this.placeSearch = placeSearch;
        this.datasetSearch = datasetSearch;
        this.dateSearch = dateSearch;
        this.onlyWithCoordinates = onlyWithCoordinates;
 	   this.email = email;
      }
 	 
 	 public void run()
 	 {
 	   /*** Download location ***/
 	   String fileName = String.valueOf(((Double) (Math.random() * 100000000)).intValue());
 	   String link = Play.configuration.getProperty("download.physical.path") + fileName + ".csv";
 	   String serverLink = Play.configuration.getProperty("download.server.path") + fileName + ".csv";
 	   try
 	   {
 		 createCSV(taxaSearch, placeSearch, datasetSearch, dateSearch, onlyWithCoordinates, link);
 	   }
 	   catch (IOException e)
 	   {
 		 System.out.println("Error during the csv file creation: " + e.getMessage());
 	   }  
 	   Properties props = new Properties();
 	   props.put("mail.smtp.host", Play.configuration.getProperty("mail.smtp.host"));
 	   props.put("mail.from", Play.configuration.getProperty("mail.from"));
 	   props.put("mail.smtp.auth", Play.configuration.getProperty("mail.smtp.auth"));
 	   props.put("mail.smtp.starttls.enable", Play.configuration.getProperty("mail.smtp.starttls.enable"));
 	   props.put("mail.smtp.port", Play.configuration.getProperty("mail.smtp.port"));
 	   Session session = Session.getInstance(props, new javax.mail.Authenticator() {
 			protected PasswordAuthentication getPasswordAuthentication() {
 				return new PasswordAuthentication(Play.configuration.getProperty("mail.username"), Play.configuration.getProperty("mail.password"));
 			}
 		  });
 
 	    try {
 	        MimeMessage msg = new MimeMessage(session);
 	        msg.setFrom();
 	        msg.setRecipients(Message.RecipientType.TO,
 	                          this.email);
 	        msg.setSubject("GBIF France : Your requested data");
 	        msg.setSentDate(new Date());
 	        msg.setText("Your data are ready to be downloaded. Here is the link: " + serverLink);
 	        Transport.send(msg);
 	        System.out.println("Email has been sent");
 	    } catch (MessagingException mex) {
 	        System.out.println("send failed, exception: " + mex);
 	    }
 	 }
   }
   
 }
