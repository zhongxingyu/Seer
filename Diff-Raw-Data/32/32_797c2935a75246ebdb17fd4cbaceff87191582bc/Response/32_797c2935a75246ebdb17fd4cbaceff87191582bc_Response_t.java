 package eu.trentorise.opendata.nlprise.identifiers.date;
 
 import java.util.HashMap;
 
 /**
  * Represent the response from the DateParser.isDate()
 * @author Alberto Zanella <a.zanella@trentorise.eu>
  * Last modified by azanella On 08/lug/2013
  */
 public class Response {
 	/**
 	 * Contains parser used to identify date. INTERNAL is the parser.ItalianDateParser
	 * @author Alberto Zanella <a.zanella@trentorise.eu>
 	 *
 	 */
 	public enum DateParser { INTERNAL, JCHRONIC, POJAVA;}
 		
 	private boolean result;
 	private HashMap<DateParser, Boolean> singleParserReturn;
 	
 	/**
 	 * 
 	 * @return if the string passed was identified as a Date by at least one parser
 	 */
 	public boolean getResult() {
 		return result;
 	}
 	/**
 	 * 
 	 * @return an HashMap containing the parser as the key and the result of the evaluation (boolean) as value
 	 */
 	public HashMap<DateParser, Boolean> getSingleParserReturn() {
 		return singleParserReturn;
 	}
 	protected void setResult(boolean result) {
 		this.result = result;
 	}
 	protected void setSingleParserReturn(
 			HashMap<DateParser, Boolean> singleParserReturn) {
 		this.singleParserReturn = singleParserReturn;
 	}
 	
 }
