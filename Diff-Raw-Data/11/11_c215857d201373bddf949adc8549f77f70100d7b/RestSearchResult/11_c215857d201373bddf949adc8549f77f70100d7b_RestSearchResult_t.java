 package ecologylab.xml.library.rest;
 
 import java.net.URL;
 
 import ecologylab.xml.ElementState;
 
 /**
  * Rest Search Results
  * @author blake
  *
  */
 public class RestSearchResult extends ElementState
 {
 	@xml_attribute protected String 		schemaVersion;
 	@xml_attribute protected String 		xmlns;
 	@xml_attribute @xml_tag("xmlns:xsi")	
 					protected String			xsi = "http://www.w3.org/2001/XMLSchema-instance";
 	@xml_attribute @xml_tag("xmlns:dc")	
 					protected String			dc = "http://purl.org/dc/elements/1.1/";
 	@xml_attribute @xml_tag("xsi:schemaLocation")
 					protected String		schemaLocation;
 	
 	@xml_leaf 	protected 	 String 		responseTime;
 	@xml_leaf   protected 	 URL			request;
 	@xml_nested @xml_tag("SearchResults") 
				protected 	 SearchResults 	SearchResults;
 	
 	public RestSearchResult() {}
 
 	/**
 	 * @param schemaVersion the schemaVersion to set
 	 */
 	public void setSchemaVersion(String schemaVersion)
 	{
 		this.schemaVersion = schemaVersion;
 	}
 
 	/**
 	 * @return the schemaVersion
 	 */
 	public String getSchemaVersion()
 	{
 		return schemaVersion;
 	}
 
 	/**
 	 * @param xmlns the xmlns to set
 	 */
 	public void setXmlns(String xmlns)
 	{
 		this.xmlns = xmlns;
 	}
 
 	/**
 	 * @return the xmlns
 	 */
 	public String getXmlns()
 	{
 		return xmlns;
 	}
 
 	/**
 	 * @param searchResults the searchResults to set
 	 */
 	public void setSearchResults(SearchResults searchResults)
 	{
		this.SearchResults = searchResults;
 	}
 
 	/**
 	 * @return the searchResults
 	 */
 	public SearchResults getSearchResults()
 	{
		return SearchResults;
 	}
 	
 	public String toString()
 	{
 		return "RestSearchResult{\n" + 
 				"responseTime: "	+ responseTime 	+ "\n" + 
 				"request: "			+ getRequest()		+ "\n" +
				"SearchResults: "	+ SearchResults + "\n" +
 				"}";
 	}
 
 	public void setRequest(URL request)
 	{
 		this.request = request;
 	}
 
 	public URL getRequest()
 	{
 		return request;
 	}
 
 	/**
 	 * @param schemaLocation the schemaLocation to set
 	 */
 	public void setSchemaLocation(String schemaLocation)
 	{
 		this.schemaLocation = schemaLocation;
 	}
 
 	/**
 	 * @return the schemaLocation
 	 */
 	public String getSchemaLocation()
 	{
 		return schemaLocation;
 	}
 	
 }
