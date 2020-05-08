 package com.espn.api.sports;
 
 import com.espn.api.API;
 import com.espn.api.InvalidResourceException;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * SportsAPI class that represents the sports ESPN API. 
  * 
  * <br/>
  * <br/>
  * <strong>Sample Usages:</strong>
  * <pre>
  * {@code
  * // Default constructor makes a request to /sports endpoint.
  * SportsAPI api = new SportsAPI();
  * Sports apiRoot = api.getAPIData();
  * 
  * // The following example makes a request to /sports/basketball endpoint.
  * SportsAPI api = new SportsAPI(HeadlinesAPI.RESOURCE_BASKETBALL);
 * SportsAPI apiRoot = api.getAPIData();
  * }
  * <pre>
  */
 public class SportsAPI extends API<Sports> {
 
    /**
     * Initialize a new SportsAPI.
     */
    public SportsAPI() {
       super(API.RESOURCE_SPORTS);
    }
    
    /**
     * Initialize a new SportsAPI for a specific resource.
     * @param resource The resource.
     * @throws InvalidResourceException Thrown if the resource is not supported.
     */
    public SportsAPI(String resource) throws InvalidResourceException {
       super(resource);
       
       if (!validResource && !isSupportedResource(SportsAPI.class, resource)) {
          throw new InvalidResourceException("The API resource '" + resource + "' does not exists.");
       }
    }
    
    /**
     * Method to retrieve the API data in Java representation.
     * @return The Sports data.
     */
    @Override
    public Sports getAPIData() {
       GsonBuilder builder = new GsonBuilder();
       Gson gson = builder.create();
       // Deserialize the JSON string to a Sports object.
       return gson.fromJson(this.getJsonResponse(), Sports.class);
    }
 }
