 package com.computas.sublima.query;
 
 import org.apache.log4j.Logger;
 
 import java.io.Serializable;
 
 /**
  * A class that uses heuristics to determine the type of a RDF object string. An
  * RDF object can be either a URI or a Literal, in the latter case, the Literal
  * can be a string in some language or a datatype such as a date. This class
  * attempts to determine which is the case for a given string. <p/> The
  * heuristics used in this class is relatively fragile, and should be tested
  * against real-world data to determine if it is sufficient. By separating it
  * out in a class, it can more easily rewritten if more advanced needs to be
  * taken into account. For example, it can query the information model to to
  * determine the OWL property types, which should be highly reliable.
  * 
  * @author mha
  * @since 11.des.2007
  */
 public class RDFObject implements Serializable {
 	
 	static Logger logger = Logger.getLogger(RDFObject.class);
 
 	private String value = null;
 
 	private String language = null;
 
 	private Integer freetext = null;
 
 	public RDFObject() {
 	}
 
 	/**
 	 * Constructor where the value of the object can be entered.
 	 * 
 	 * @param value
 	 *            The value of the object.
 	 */
 	public RDFObject(String value) {
 		this.value = value;
 	}
 
 	/**
 	 * Constructor where the value and the language of this object can be
 	 * entered.
 	 * 
 	 * @param value
 	 *            The value of the object.
 	 * @param language
 	 *            The language the object is known to have.
 	 */
 	public RDFObject(String value, String language) {
 		this.value = value;
 		this.language = language;
 	}
 	
 	/**
 	 * Constructor where the value, the language and if the field is a freetext field 
 	 * of this object can be entered.
 	 * 
 	 * @param value
 	 *            The value of the object.
 	 * @param language
 	 *            The language the object is known to have.
 	 * @param freetext
 	 *            An integer giving an unique index to the freetext field.
 	 *            If not set, the field is regarded a non-freetext-field.
 	 */
 	public RDFObject(String value, String language, Integer freetext) {
 		this.value = value;
 		this.language = language;
 		this.freetext = freetext;
 	}
 
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 	
 	public Integer getFreetext() {
 		return freetext;
 	}
 
 	public void setFreetext(Integer freetext) {
 		this.freetext = freetext;
 	}
 
 	
 	
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	/**
 	 * Turns the value into an N3 string, with necessary quotes, language tags
 	 * and datatype URIs depending on the type of the object.
 	 * 
 	 * @return An N3 string with correct quotes.
 	 */
 	public String toN3() {
 		// TODO: Move this to a method of its own if other serialisations are
 		// required
 		StringBuffer n3Buffer = new StringBuffer();
 		// Very ad-hoc date detection, doesn't support negative years,
 		// fractions of seconds or timezones
 		if (getValue().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
 			logger.trace("Found xsd:dateTime " + getValue());
 			n3Buffer.append("\"" + getValue()
 					+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .");
 		}
 		// To check if we have a URI, use regexp from RFC 2396, modified
 		else if (getValue().matches(
 				"^([^:/?#]+):(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?")) {
 			logger.debug("Found URI " + getValue());
 			n3Buffer.append("<" + getValue() + "> .");
 		} else { // We have a literal
 			logger.trace("Found Literal " + getValue());
 			if (freetext != null && freetext > 0) { // Should this literal be treated as a free text?
 				n3Buffer.append("?free" + freetext + " .\n"); // The actual previous object
				n3Buffer.append("?free" + freetext + " pf:textMatch '+" + getValue() + "' .");
 			} else {
 				n3Buffer.append("\"" + getValue() + "\"");
 				if (language != null) {
 					n3Buffer.append("@" + language);
 				}
 				n3Buffer.append(" .");
 			}
 		}
 		return n3Buffer.toString();
 	}
 }
