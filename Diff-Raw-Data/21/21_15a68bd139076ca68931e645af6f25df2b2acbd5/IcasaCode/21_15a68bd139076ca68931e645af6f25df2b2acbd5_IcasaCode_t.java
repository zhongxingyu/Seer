 package org.agmip.translators.stics.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class use to map stics and icasa codes.
  * 
  * @author jucufi
  * 
  */
public final class IcasaCode {
	private IcasaCode(){
		// utility class hide constructor
	}
	
 	private static final Logger log = LoggerFactory.getLogger(IcasaCode.class);
	private static final String CODE_UNKNOWN = "code_unknown";
	private static final Properties codeMap;
	private static final HashMap<String, String> unknownIcasaCode = new HashMap<String, String>();
 	
 	static {
		InputStream inputStream = CropCycle.class.getResourceAsStream("/code-mapping.properties");
 		codeMap = new Properties();
 		try {
 			codeMap.load(inputStream);
 			log.debug(codeMap.toString());
 		} catch (IOException e) {
 			log.error(e.toString());
 		}
 	}
 
 	public static String toSticsCode(String icasaCode) {
 		String result;
 		log.debug("icasa code : "+icasaCode);
 		if (!codeMap.containsKey(icasaCode)) {
 			if(!unknownIcasaCode.containsKey(icasaCode)){
				Report.addInfo("Unknown icasa code : " + icasaCode+"\n");
 			}
 			unknownIcasaCode.put(icasaCode, icasaCode);
 			result = CODE_UNKNOWN;
 		} else {
 			result = (String) codeMap.get(icasaCode);
 		}
 		return result;
 	}
 }
