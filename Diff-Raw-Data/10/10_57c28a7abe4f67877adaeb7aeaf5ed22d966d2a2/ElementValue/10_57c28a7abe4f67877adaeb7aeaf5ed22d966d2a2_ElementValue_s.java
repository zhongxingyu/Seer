 package api.basecamp;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /***
  * 
  * Static Class to process XML Elements
  * 
  * @author jondavidjohn
  *
  */
 public class ElementValue {
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	String value of contents
 	 */
 	static String getTextValue(Element ele, String tagName) {
 		String textVal = null;
 		NodeList nl = ele.getElementsByTagName(tagName);
 		if(nl != null && nl.getLength() > 0) {
 			Element el = (Element)nl.item(0);
 			if (el.hasChildNodes()) {
 				textVal = el.getFirstChild().getNodeValue();
 			}
 			else {
 				textVal = "";
 			}
 		}
 
 		return textVal;
 	}
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	Int value of contents
 	 */
 	static int getIntValue(Element ele, String tagName) {
 		//TODO Catch exception
 		String value = ElementValue.getTextValue(ele,tagName);
 		if (value != "") {
 			return Integer.parseInt(value);
 		}
 		else {
 			return 0;
 		}
 	}
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	Long value of contents
 	 */
 	static long getLongValue(Element ele, String tagName) {
 		//TODO Catch exception
 		String value = ElementValue.getTextValue(ele,tagName);
 		if (value != "") {
 			return Long.parseLong(value);
 		}
 		else {
 			return 0;
 		}	
 	}
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	Double value of contents
 	 */
 	static double getDoubleValue(Element ele, String tagName) {
 		//TODO Catch exception
 		String value = ElementValue.getTextValue(ele,tagName);
 		if (value != "") {
 			return Double.parseDouble(value);
 		}
 		else {
 			return 0;
 		}
 	}
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	Bool value of contents
 	 */
 	static boolean getBoolValue(Element ele, String tagName) {
 		//TODO Catch exception
		return (ElementValue.getTextValue(ele, tagName) == "true");
 	}
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * Expects DateTime in Zulu format ex.. "yyyy-MM-dd'T'HH:mm:ss'Z'"
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	Calendar value of contents
 	 */
 	static Calendar getDateTimeValue(Element ele, String tagName) {
 		//TODO Catch exception
 		try {
 			String dateValue = ElementValue.getTextValue(ele, tagName);
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 			df.setTimeZone(TimeZone.getTimeZone("Zulu"));
 			Date date = df.parse(dateValue);
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(date);
 			return cal;
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return Calendar.getInstance();
 		}
 	}
 	
 	/***
 	 * 
 	 * Get Child Element String Value
 	 * 
 	 * Expects Date in this format -- "yyyy-MM-dd"
 	 * 
 	 * @param ele		Parent Element
 	 * @param tagName	Child tag-name
 	 * @return	Calendar value of contents
 	 */
 	static Calendar getDateValue(Element ele, String tagName) {
 		try {
 			String dateValue = ElementValue.getTextValue(ele, tagName);
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			Date date = df.parse(dateValue);
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(date);
 			return cal;
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return Calendar.getInstance();
 		}
 	}
 }
