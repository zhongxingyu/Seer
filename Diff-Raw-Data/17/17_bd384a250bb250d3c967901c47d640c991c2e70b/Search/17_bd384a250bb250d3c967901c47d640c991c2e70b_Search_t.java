 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.elasticsearch.action.search.SearchRequest;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.action.search.SearchType;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.index.query.FieldQueryBuilder;
 import org.elasticsearch.index.query.FilterBuilders;
 import org.elasticsearch.index.query.FuzzyLikeThisFieldQueryBuilder;
 import org.elasticsearch.index.query.QueryBuilder;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.search.SearchHit;
 
 
 import play.modules.*;
 import models.*;
 import play.mvc.*;
 
 import play.modules.elasticsearch.ElasticSearch;
 import play.modules.elasticsearch.Query;
 import play.modules.elasticsearch.search.SearchResults;
 
 
 public class Search extends Controller {
	@Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();

//            if(user!= null)
                renderArgs.put("user", user);
//            else 
//            	renderArgs.put("user", "guest");
        }
    }
 	
 	@Before
 	static void addDefaults() {
 	    renderArgs.put("mainCates", Inventory.getAllMainCategories());
 	}
 
 	/**
 	 * Generates a "fuzzy like this query" with the provided search string, and renders a list of the result offers to the index page.
 	 * 
 	 * @param search_string The search string
 	 */
 	public static void index(String search_string){
 		
 		//.setQuery(QueryBuilders.fuzzyQuery("_all", "summer")) 
 		SearchResponse response = ElasticSearch.client().prepareSearch()
 		        .setQuery(QueryBuilders.fuzzyLikeThisQuery().likeText(search_string))
 				.execute()
 		        .actionGet();
 		
 		
 		List<Offer> offers = new ArrayList<Offer>();
 		for(SearchHit hit : response.getHits()){
 			
 			//Offer o =Offer.findById(hit.getId());
 			//System.out.println(hit.getScore());
 			long id = Long.parseLong(hit.getId());
 			List<Offer> temp = Offer.find("select o from Offer o where o.id =" +id).fetch();
 			offers.addAll(temp);
 			
 			
 		}
 	
 		render(offers);
 		
 	}
 	
 	public static void getOffersByCategory(long id_category){
 		List<Offer> offers = Inventory.getOffersByCategory(id_category);
 		render("@index", offers);
 	}
 	
 }
