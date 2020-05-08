 package au.gov.nsw.records.search.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.facet.index.CategoryDocumentBuilder;
 import org.apache.lucene.facet.search.params.CountFacetRequest;
 import org.apache.lucene.facet.search.params.FacetSearchParams;
 import org.apache.lucene.facet.taxonomy.CategoryPath;
 import org.apache.lucene.index.CorruptIndexException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import au.gov.nsw.records.search.bean.FacetResultItem;
 import au.gov.nsw.records.search.bean.SearchResult;
 import au.gov.nsw.records.search.bean.SearchResultItem;
 import au.gov.nsw.records.search.model.Activity;
 import au.gov.nsw.records.search.model.Agency;
 import au.gov.nsw.records.search.model.Functionn;
 import au.gov.nsw.records.search.model.Item;
 import au.gov.nsw.records.search.model.Ministry;
 import au.gov.nsw.records.search.model.Person;
 import au.gov.nsw.records.search.model.Portfolio;
 import au.gov.nsw.records.search.model.Serie;
 import au.gov.nsw.records.search.service.LuceneSearchParams;
 import au.gov.nsw.records.search.service.LuceneService;
 import au.gov.nsw.records.search.service.StringService;
 
 @RequestMapping("/search/**")
 @Controller
 public class SearchController {
 
 		private static final Logger logger = Logger.getLogger(SearchController.class);
 		
 	  private static LuceneService lucene = new LuceneService();
 	  private static boolean isIndexing = false;
 	
 	  private static List<Class<?>> entitiesList = new ArrayList<Class<?>>(Arrays.asList(new Class<?>[]{Serie.class, Item.class, Activity.class, Functionn.class, Person.class, Agency.class}));
 	  @RequestMapping(method = RequestMethod.GET)
     public String index() {
         return "search/index";
     }
     
     @RequestMapping(value = "/createindex", method = RequestMethod.POST)
     public void startIndex(HttpServletRequest request, HttpServletResponse response) {
 //     	ExecutorService indexWorker = Executors.newSingleThreadExecutor();
      	if (!isIndexing){
 //     		indexWorker.execute(new Runnable() {
 //  				@Override
 //  				public void run() {
   		    	try {
   		    		
   		    		isIndexing = true;
   		    		
   		    		CategoryDocumentBuilder builder = lucene.startWriting();
   						//Activities
   						logger.info("Indexing activities");
   						lucene.indexDocuments(Activity.getIndexData(Activity.findAllActivitys(), builder), Activity.class);
   						lucene.commit();
   						
   						//Agencies
   						logger.info("Indexing agencies");
   						lucene.indexDocuments(Agency.getIndexData(Agency.findAllAgencys(), builder), Agency.class); 
   						lucene.commit();
   						
   						logger.info("Indexing series");
   						lucene.indexDocuments(Serie.getIndexData(Serie.findAllSeries(), builder), Serie.class); 
   						lucene.commit();
   						
   						logger.info("Indexing ministries");
   						lucene.indexDocuments(Ministry.getIndexData(Ministry.findAllMinistrys(), builder), Ministry.class);
   						lucene.commit();
   						
   						logger.info("Indexing persons");
   						lucene.indexDocuments(Person.getIndexData(Person.findAllPeople(), builder), Person.class);
   						lucene.commit();
   						
   						logger.info("Indexing portfolios");
   						lucene.indexDocuments(Portfolio.getIndexData(Portfolio.findAllPortfolios(), builder), Portfolio.class);
   						lucene.commit();
   										
   						logger.info("Indexing items " + Item.countItems());
   						long itemCount = Item.countItems();
   						int pageSize = 1000;
   						int pageCount = 0;
   						while(pageCount * pageSize < itemCount){
   							logger.info("Indexing items page " + pageCount + " of " + itemCount/pageSize);
   							lucene.indexDocuments(Item.getIndexData(Item.findItemEntries(pageCount * pageSize, pageSize), builder), Item.class);
   							lucene.commit();
   							pageCount++;
   							Item.clear();
   							System.gc();
   						}
   						lucene.finishWriting();
   						
   						logger.info("Indexing finished");
   		    	} catch (CorruptIndexException e) {
   		    		logger.error("Indexing error", e);
   						e.printStackTrace();
  					} catch (Exception e) {
   						logger.error("Indexing error", e);
   						e.printStackTrace();
   					}finally{
   						isIndexing = false;
   					}
 //  				}
 //  			});
 
   			logger.info("Indexing started");
      	}else{
      		logger.warn("Indexing already in progress");
      	}
      	
     }
     
   	@RequestMapping(value = "/search", method = RequestMethod.GET)
     public String search(@RequestParam(value = "page", required = false, defaultValue="1") Integer page, 
     		@RequestParam(value = "fpage", required = false, defaultValue="1") Integer fpage,
     		@RequestParam(value = "apage", required = false, defaultValue="1") Integer apage,
     		@RequestParam(value = "size", required = false, defaultValue="30") Integer pageSize,
     		@RequestParam(value = "count", required = false, defaultValue="30") Integer count,
     		@RequestParam(value = "location", required = false) String location,
     		@RequestParam(value = "series", required = false) String series,
     		@RequestParam(value = "from", required = false) String from,
     		@RequestParam(value = "to", required = false) String to,
     		@RequestParam(value = "entities", required = false) String entities,
     		@RequestParam(value = "q", defaultValue="") String queryText,
     		Model model, HttpServletRequest request) {
   		
   		if (page==null){
   			page=1;
   		}
   		if (pageSize==null){
   			pageSize=30;
   		}
   		if (count!=null && !count.equals(new Integer(30))){
   			pageSize = count.intValue();
   		}
   		
   		if (queryText.contains("entities:")){
   			StringTokenizer st = new StringTokenizer(queryText, " ");
   			String strippedQueryText = ""; 
   			while(st.hasMoreTokens()){
   				String token = st.nextToken();
   				if (token.startsWith("entities:")){
   					entities = token.replace("entities:", "");
   				}else{
   					strippedQueryText += " " + token; 
   				}
   			}
   			queryText = strippedQueryText.trim();
   		}
   		
   		LuceneSearchParams params = new LuceneSearchParams();
   		FacetSearchParams facetParams = new FacetSearchParams();
   		
   		facetParams.addFacetRequest(new CountFacetRequest(new CategoryPath("series_id"), 10));
   		facetParams.addFacetRequest(new CountFacetRequest(new CategoryPath("series"), 10));
   		facetParams.addFacetRequest(new CountFacetRequest(new CategoryPath("location"), 10));
   		//facetParams.addFacetRequest(new CountFacetRequest(new CategoryPath("startyear", "endyear"), 10));
   		int asize = 15;
   		int fsize = 5;
   		
   		if (entities!=null){
   			List<Class<?>> searchClass = new ArrayList<Class<?>>();
   			StringTokenizer st = new StringTokenizer(entities, ",");
   			while (st.hasMoreTokens()){
   				String entity = st.nextToken();
   				if (entity.length()>4){
   					entity = entity.substring(0, 4);
   				}
   				for(Class<?> cls: entitiesList){
   					if (cls.getName().toLowerCase().contains(entity.toLowerCase())){
   						searchClass.add(cls);
   						break;
   					}
   				}
   			}
   			if (searchClass.size()>0){
   				Class<?>[] clazzParams = new Class<?>[searchClass.size()];
     			searchClass.toArray(clazzParams);
     			SearchResult customSearch = lucene.search(params.setQuery(queryText).setFacetParams(facetParams).setPage(fpage).setSize(pageSize).setClazz(clazzParams));
     			
     			model.addAttribute("customsearch", customSearch.getResults());
     			model.addAttribute("customsearch_count", Math.ceil(customSearch.getResultCount()/Double.valueOf(pageSize)));
   			}
   			
   		}
   		else {
 	  		SearchResult activitiesFunctions = lucene.search(params.setQuery(queryText).setFacetParams(facetParams).setPage(fpage).setSize(fsize).setClazz(Activity.class, Functionn.class));
 	  		SearchResult agenciesPeople = lucene.search(params.setQuery(queryText).setFacetParams(facetParams).setPage(apage).setSize(asize).setClazz(Agency.class, Person.class));
 	  		SearchResult seriesItems = lucene.search(params.setQuery(queryText).setSeries(series).setLocation(location).setFrom(from).setTo(to).setFacetParams(facetParams).setPage(page).setSize(pageSize).setClazz(Serie.class, Item.class));
 	  	  
 	  		List<FacetResultItem> facets = seriesItems.getFacets();
 	      for (FacetResultItem fri:facets){
 	      	if (fri.getLabel().equals("series")){
 	      		for (FacetResultItem subFri:fri.getItems()){
 	      			subFri.setLabel(Serie.findSerie(Integer.valueOf(subFri.getLabel())).getTitle());
 	      		}
 	      	}
 	      }
 	      
 	      model.addAttribute("activitiesfunctions", activitiesFunctions.getResults());
 	      model.addAttribute("activitiesfunctions_count", Math.ceil(activitiesFunctions.getResultCount()/Double.valueOf(fsize)));
 	      
 	      
 	      model.addAttribute("seriesitems", seriesItems.getResults());
 	      model.addAttribute("seriesitems_count", Math.ceil(seriesItems.getResultCount()/Double.valueOf(pageSize)));
 	      model.addAttribute("seriesitems_total", seriesItems.getResultCount());
 	      
 	      model.addAttribute("agenciespeoples", agenciesPeople.getResults());
 	      model.addAttribute("agenciespeoples_count", Math.ceil(agenciesPeople.getResultCount()/Double.valueOf(asize)));
 	      
 	      model.addAttribute("facets", facets);
 	      
 	      // also make it available in custom search
 	      model.addAttribute("count", seriesItems.getResultCount());
 	      
 	      if (page < Math.ceil(seriesItems.getResultCount()/Double.valueOf(pageSize))){
 	      	model.addAttribute("next_url", request.getRequestURL() + "?q=" + queryText + "&page=" + (page+1) + "&size=" + pageSize);
 	      }
   		}
   		String nonPageParams = "&q=" + queryText;
       if (location!=null){ nonPageParams += "&location=" + location; }
       if (series!=null){ nonPageParams += "&series=" + series; }
       if (from!=null){ nonPageParams += "&from=" + from; }
       if (to!=null){ nonPageParams += "&to=" + to; }
   	
       model.addAttribute("self_url", request.getRequestURL() + "?q=" + queryText);
       
       if (page>1){
       	model.addAttribute("prev_url", request.getRequestURL() + "?q=" + queryText + "&page=" + (page-1) + "&size=" + pageSize);
       }
       
       
       List<SearchResultItem> hotLinks = new ArrayList<SearchResultItem>();
       if (StringService.isNumeric(queryText)){
   			Agency ag = Agency.findAgency(Integer.parseInt(queryText));
   			if (ag!=null){
   				hotLinks.add(new SearchResultItem("agencies", ag.getTitle(), "", String.valueOf(ag.getId()), "/agencies/"+ag.getId(), true));
   			}
   			Serie sr = Serie.findSerie(Integer.parseInt(queryText));
   			if (sr!=null){
   				hotLinks.add(new SearchResultItem("series", sr.getTitle(), "", String.valueOf(sr.getId()), "/series/"+sr.getId(), true));
   			}
   			Item it = Item.findItem(Integer.parseInt(queryText));
   			if (it!=null){
   				hotLinks.add(new SearchResultItem("items", it.getTitle(), "", String.valueOf(it.getId()), "/items/"+it.getId(), true));
   			}
   		}
       
       
       model.addAttribute("hotlinks", hotLinks);
       model.addAttribute("q", queryText);
       
       model.addAttribute("page", page);
       model.addAttribute("fpage", fpage);
       model.addAttribute("apage", apage);
       model.addAttribute("size", pageSize);
       model.addAttribute("asize", asize);
       model.addAttribute("fsize", fsize);
       
       model.addAttribute("entities", entities);
       
       model.addAttribute("location", location);
       model.addAttribute("series", series);
       model.addAttribute("from", from);
       model.addAttribute("to", to);
     
       model.addAttribute("nonPageParams", nonPageParams);
       
       model.addAttribute("baseurl", request.getRequestURL() + String.format("?q=%s", queryText));
   		model.addAttribute("view", "search/list");
       return "search/list";
     }
 }
 
