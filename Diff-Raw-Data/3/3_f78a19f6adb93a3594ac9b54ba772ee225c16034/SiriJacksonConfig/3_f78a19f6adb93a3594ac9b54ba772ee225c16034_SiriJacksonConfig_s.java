 package edu.usf.cutr.siri.android.util;
 
 import uk.org.siri.siri.Siri;
 
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectReader;
 import com.fasterxml.jackson.dataformat.xml.XmlMapper;
 
 import edu.usf.cutr.siri.jackson.PascalCaseStrategy;
 
 /**
  * This class holds a static instance of a Jackson ObjectMapper that is
  * configured for parsing Siri JSON responses
  * 
  * The ObjectMapper is thread-safe after it is configured:
  * http://wiki.fasterxml.com/JacksonFAQThreadSafety
  * 
  * ...so we can configure it once here and then use it in multiple fragments.
  * 
  * @author Sean J. Barbeau
  * 
  */
 public class SiriJacksonConfig {
 
 	//For JSON
 	private static ObjectMapper mapper = null;
 	private static ObjectReader reader = null;
 	
 	//For XML
 	private static XmlMapper xmlMapper = null;
 
 	/**
 	 * Constructs a thread-safe instance of a Jackson ObjectMapper configured to parse
 	 * JSON responses from a Mobile Siri API.
 	 * 
 	 * According to Jackson Best Practices (http://wiki.fasterxml.com/JacksonBestPracticesPerformance),
 	 * for efficiency reasons you should use the ObjectReader instead of the ObjectMapper.
 	 * 
 	 * @deprecated
 	 * @return thread-safe ObjectMapper configured for SIRI JSON responses
 	 */
 	public synchronized static ObjectMapper getObjectMapperInstance() {		
 		return initObjectMapper();		
 	}
 	
 	/**
 	 * Constructs a thread-safe instance of a Jackson ObjectReader configured to parse
 	 * JSON responses from a Mobile Siri API 
 	 * 
 	 * According to Jackson Best Practices (http://wiki.fasterxml.com/JacksonBestPracticesPerformance),
 	 * this should be more efficient than the ObjectMapper.
 	 * 
 	 * @return thread-safe ObjectMapper configured for SIRI JSON responses
 	 */
 	public synchronized static ObjectReader getObjectReaderInstance() {
 		
 		if(reader == null){
 			reader = initObjectMapper().reader(Siri.class);
 		}
 		
 		return reader;
 	}
 	
 	/**
 	 * Internal method used to init main ObjectMapper for JSON parsing
 	 * @return initialized ObjectMapper ready for JSON parsing
 	 */
 	private static ObjectMapper initObjectMapper(){
 		if (mapper == null) {
 			// Jackson configuration
 			mapper = new ObjectMapper();
 
 			mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
 			mapper.configure(
 					DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
 			mapper.configure(
 					DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
 					true);
 			mapper.configure(
 					DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
 			mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,
 					true);
 
 			// Tell Jackson to expect the JSON in PascalCase, instead of
 			// camelCase
 			mapper.setPropertyNamingStrategy(new PascalCaseStrategy());			
 		}
 		return mapper;
 	}
 	
 	/**
 	 * Constructs a thread-safe instance of a Jackson XmlMapper configured to parse
 	 * XML responses from a Mobile Siri API.
 	 * 
 	 * @return thread-safe ObjectMapper configured for SIRI XML responses
 	 */
 	public synchronized static ObjectMapper getXmlMapperInstance() {		
 		return initXmlMapper();		
 	}
 	
 	/**
 	 * Internal method used to init main XmlMapper for XML parsing
 	 * @return initialized XmlMapper ready for XML parsing
 	 */
 	private static XmlMapper initXmlMapper(){
 		if(xmlMapper == null){
 			xmlMapper = new XmlMapper();
 			
 			xmlMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
 			xmlMapper.configure(
 					DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
 			xmlMapper.configure(
 					DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
 					true);
 			xmlMapper.configure(
 					DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
 			xmlMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,
 					true);
 			
			// Tell Jackson to expect the JSON in PascalCase, instead of
			// camelCase
 			xmlMapper.setPropertyNamingStrategy(new PascalCaseStrategy());	
 		}
 		
 		return xmlMapper;
 		
 	}
 }
