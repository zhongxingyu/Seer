 package org.vme.fimes.jaxb;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
 
 public class FimesNamespacePrefixMapper extends NamespacePrefixMapper {
 
 	private static Map<String, String> prefixMap = new HashMap<String, String>();
 	static {
 		prefixMap.put("http://www.fao.org/fi/figis/devcon/", "fi");
 		prefixMap.put("http://www.naa.gov.au/recordkeeping/gov_online/agls/1.1", "agls");
 		prefixMap.put("http://www.purl.org/agmes/1.1/", "ags");
 		prefixMap.put("http://www.idmlinitiative.org/resources/dtds/aida22.xsd", "aida");
 		prefixMap.put("http://purl.org/dc/elements/1.1/", "dc");
 		prefixMap.put("http://rs.tdwg.org/dwc/terms/", "dwc");
 		prefixMap.put("http://purl.org/dc/terms/", "dcterms");
 		prefixMap.put("http://www.w3.org/2001/xmlschema-instance", "xsi");
		prefixMap.put("http://www.w3.org/xml/1998/namespace", "ns");
 	}
 
 	@Override
 	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
 		// System.out.println(namespaceUri);
 		// System.out.println(requirePrefix);
 		String namespaceUriLowerCase = namespaceUri.toLowerCase();
 		String prefix = prefixMap.get(namespaceUriLowerCase);
 		return prefix;
 	}
 }
