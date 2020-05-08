 package ecologylab.xml.library.yahoo;
 
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationSpace;
 import ecologylab.xml.XmlTranslationException;
 import ecologylab.xml.xml_inherit;
 import ecologylab.xml.subelements.ArrayListState;
 
 /**
  * Top level result set collection element for Yahoo search.
  * The collected elements are all of type result.
  *
  * @author andruid
  */
 @xml_inherit
 public class ResultSet extends ArrayListState
 {
 	public int			totalResultsAvailable;
 	public int			totalResultsReturned;
 	
 	public int			firstResultPosition;
 	
 	public String		xmlns;
 	public String		schemaLocation;
 	public String		xsi;
 	
 	
 	public static TranslationSpace translationSpace()
 	{
		return TranslationSpace.get("yahoo", "ecologylab.xml.library.yahoo");
 	}
 	
 	public static String SEOUL_SHOPPING		= "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=yahoosearchwebrss&results=15&start=1&query=shopping+seoul";
 	
 	public static void main(String[] a)
 	{
 		ParsedURL purl = ParsedURL.getAbsolute(SEOUL_SHOPPING, "uck");
 		try
 		{
 			ResultSet resultSet	= 
 				(ResultSet) ElementState.translateFromXML(purl, ResultSet.translationSpace());
 		} catch (XmlTranslationException e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 }
