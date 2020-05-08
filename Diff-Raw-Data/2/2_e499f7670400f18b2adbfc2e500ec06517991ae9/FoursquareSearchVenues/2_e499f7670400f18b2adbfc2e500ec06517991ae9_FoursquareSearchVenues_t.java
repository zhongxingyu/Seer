 package it.unito.geosummly;
 
 import it.unito.geosummly.utils.PropFactory;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import fi.foyt.foursquare.api.FoursquareApi;
 import fi.foyt.foursquare.api.FoursquareApiException;
 import fi.foyt.foursquare.api.Result;
 import fi.foyt.foursquare.api.entities.Category;
 import fi.foyt.foursquare.api.entities.CompactVenue;
 import fi.foyt.foursquare.api.entities.VenuesSearchResult;
 
 
 /**
  * @author Giacomo Falcone
  *
  * This class built for download venue informations from 4square 
  */
 
 public class FoursquareSearchVenues{
 	private FoursquareApi foursquareApi;
 	
 	//Constructor method
 	public FoursquareSearchVenues(){
 		//Initialize FoursquareApi
 		foursquareApi = new FoursquareApi(
 		        PropFactory.config.getProperty("it.unito.geosummly.foursquare.clientID"), 
 		        PropFactory.config.getProperty("it.unito.geosummly.foursquare.clientSecret"), 
 		        "http://www.foursquare.com");
 	}
 	
 	//Search venue informations
 	public ArrayList<FoursquareDataObject> searchVenues(int row, int column, double north, double south, double west, double east) throws FoursquareApiException, UnknownHostException {
 		
 		//Initialize parameters for venues search
 		String ne=north+","+east;
 		String sw=south+","+west;
 		Map<String, String> searchParams = new HashMap<String, String>(); 
 		searchParams.put("intent", "browse");
 		searchParams.put("ne", ne); 
 		searchParams.put("sw", sw);
 		
 		//Array to return
 		ArrayList<FoursquareDataObject> doclist=new ArrayList<FoursquareDataObject>(); 
 	    
 	    //After client has been initialized we can make queries.
 	    Result<VenuesSearchResult> result = foursquareApi.venuesSearch(searchParams);
 	    if(result.getMeta().getCode() == 200) {
 	    	   	
     		//Declare a FoursquareDataObject
     		FoursquareDataObject dataobj;
 	    	
 	    	//For each point: create a FoursquareDataObject)
 	    	for(CompactVenue venue : result.getResult().getVenues()){
 	    		//Initialize the FoursquareDataObject and fill it with the venue informations
 	    		dataobj=new FoursquareDataObject();
 	    		dataobj.setRow(row);
 	    		dataobj.setColumn(column);
 	    		dataobj.setVenueId(venue.getId());
 	    		dataobj.setVenueName(venue.getName());
 	    		dataobj.setLatitude(venue.getLocation().getLat());
 	    		dataobj.setLongitude(venue.getLocation().getLng());
 	    		dataobj.setCategories(venue.getCategories());
 	    		dataobj.setEmail(venue.getContact().getEmail());
 	    		dataobj.setPhone(venue.getContact().getPhone());
 	    		dataobj.setFacebook(venue.getContact().getFacebook());
 	    		dataobj.setTwitter(venue.getContact().getTwitter());
 	    		dataobj.setVerified(venue.getVerified());
 	    		dataobj.setCheckinsCount(venue.getStats().getCheckinsCount());
 	    		dataobj.setUsersCount(venue.getStats().getUsersCount());
 	    		dataobj.setUrl(venue.getUrl());
 	    		dataobj.setHereNow(venue.getHereNow().getCount());
 	    		doclist.add(dataobj);
 	    	}
 	    	return doclist;
     	} 
     	else {
 		      System.out.println("Error occured: ");
 		      System.out.println("  code: " + result.getMeta().getCode());
 		      System.out.println("  type: " + result.getMeta().getErrorType());
 		      System.out.println("  detail: " + result.getMeta().getErrorDetail());
 		      return doclist;
 	    }
 	}
 	
 	//Return the total number of categories
 	public int getCategoriesNumber(ArrayList<FoursquareDataObject> array){
 		int n=0;
 		for(FoursquareDataObject fdo: array){
 			n+=fdo.getCategories().length;
 		}
 		return n;
 	}
 	
 	//Create a list with distinct categories for a bounding box cell
 	public ArrayList<String> createCategoryList(ArrayList<FoursquareDataObject> array){
 		ArrayList<String> categories=new ArrayList<String>();
		//categories.add(array.get(0).getCategories()[0].getName());
 		for(int i=0; i<array.size();i++){
 			Category[] cat_array=array.get(i).getCategories();
 			for(int j=0; j<cat_array.length;j++){
 				Category c=cat_array[j];
 				int k=0;
 				boolean found=false;
 				while(k<categories.size() && !found){
 					String s=categories.get(k);
 					if(c.getName().equals((String) s))
 						found=true;
 					k++;
 				}
 				if(!found)
 					categories.add(c.getName());
 			}
 		}
 		return categories;
 	}
 		
 	//Create a list with the number of occurrences for each distinct category
 	public ArrayList<Integer> getCategoryOccurences(ArrayList<FoursquareDataObject> array, ArrayList<String> cat_list){
 		int n=0;
 		ArrayList<Integer> occurrences=new ArrayList<Integer>();
 		for(String s: cat_list){
 			n=0;
 			for(FoursquareDataObject fdo: array)
 				for(Category c: fdo.getCategories())
 					if(c.getName().equals((String) s))
 						n++;
 			occurrences.add(n);
 		}
 		return occurrences;
 	}
  }
