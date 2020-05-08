 package model.productIdentification;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import common.util.IHttpClient;
 import common.util.SignedRequestsHelper;
 
 /**
  * Finds descriptions for Product objects using the Amazon Product Search API
  * 
  * @author Matthew
  * @version 1.0 -- Snell CS 340 Phase 4.0
  * 
  */
 public class AmazonApi implements ProductIdentificationPlugin {
 	private static final String AWS_ACCESS_KEY_ID = "AKIAJSHRDD3C24XD7DMA";
 	private static final String AWS_SECRET_KEY = "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";
 	private static final String ENDPOINT = "ecs.amazonaws.com";
 	private IHttpClient client;
 	private String title;
 
 	@Override
 	public String getDescriptionForProduct(String productBarcode) {
 		SignedRequestsHelper helper;
 		try {
 			helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID,
 					AWS_SECRET_KEY);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		String requestUrl = null;
 
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("Service", "AWSECommerceService");
 		params.put("Version", "2011-08-01");
 		params.put("Operation", "ItemLookup");
 		params.put("AssociateTag", "7546-6649-9191");
 		params.put("SearchIndex", "All");
 		params.put("IdType", "UPC");
 		params.put("ItemId", productBarcode);
 
 		requestUrl = helper.sign(params);
 
 		String response = client.getHttpRequest(requestUrl);
 
 		String description = parseDescription(response);
 
 		return description;
 	}
 
 	@Override
 	public void setClient(IHttpClient client) {
 		this.client = client;
 	}
 
 	private String parseDescription(String xml) {
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 
 		try {
 			SAXParser saxParser = spf.newSAXParser();
 			DefaultHandler handler = new DefaultHandler() {
 				boolean inTitle = false;
 
 				@Override
 				public void characters(char ch[], int start, int length) throws SAXException {
					if (inTitle && title == null) {
 						title = new String(ch, start, length);
 						inTitle = false;
 					}
 				}
 
 				@Override
 				public void startElement(String uri, String localName, String qName,
 						Attributes attributes) throws SAXException {
 					if (qName.equalsIgnoreCase("title")) {
 						inTitle = true;
 					}
 				}
 			};
 			saxParser.parse(new InputSource(new StringReader(xml)), handler);
 		} catch (SAXException se) {
 			se.printStackTrace();
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		} catch (IOException ie) {
 			ie.printStackTrace();
 		}
 
 		return title;
 	}
 }
