 package webcrawler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 public class LinkExtractor {
 
     private HTMLReader hTMLReader;
     
 	public LinkExtractor(HTMLReader hTMLReader) {
 		this.sethTMLReader(hTMLReader);
 	}
 
 	private HTMLReader gethTMLReader() {
 		return hTMLReader;
 	}
 
 	private void sethTMLReader(HTMLReader hTMLReader) {
 		this.hTMLReader = hTMLReader;
 	}
 
 	private String expandURL(URL url, String urlString) {
 
 		if (urlString != null) {
 
 			boolean hasProtocol = false;
 
 			int protocolEndPosition = urlString.indexOf(":");
 
 			if (protocolEndPosition > 0) {
 				hasProtocol = true;
 			}
 
 			if (!hasProtocol) {
 				return url.getProtocol()
 						+ "://"
 						+ url.getHost()
 						+ ((url.getPort() == -1) ? "" : url.getPort())
 						+ ((url.getPath().endsWith("/")) ? url.getPath()
 								.substring(0, url.getPath().length() - 1) : url
 								.getPath())
 						+ ((!urlString.equals("") && urlString.charAt(0) == '/') ? urlString
 								: "/" + urlString);
 			} else {
 				return urlString;
 			}
 
 		} else {
 
 			return urlString;
 
 		}
 
 	}
 
	public URLList extractLinks(int level, String urlString) throws IOException {
 
 		URLList uRLList = new URLListArrayListImpl();
 
		URL url = new URL(urlString);
 
 		InputStream ins;
 
		ins = url.openStream();
 
 		// Traverse the HTML one element at a time.
 
 		while (this.gethTMLReader().readUntil(ins, '<', '<')) {
 
 			// Inside an element - may be a closing element - but an element at
 			// least.
 
 			String token = this.gethTMLReader().readString(ins, ' ', '>');
 			String element;
 			String attribute;
 			String attributeValue = null;
 
 			if (token != null) {
 				
 				element = token.trim();
 
 				if (element.equals("a")) {
 
 					token = this.gethTMLReader().readString(ins, '=', '>');
 
 					while (token != null) {
 						
 						attribute = token.replace(" ", "").replace("=", "");
 						
 						if (attribute.equals("href")) {
 							
 							char nextChar = this.gethTMLReader().skipSpace(ins, '>');
 							
 							if (nextChar == '"') {
 								// Looks like the element value is enclosed in quotes so
 								// read to the next double quote.
 								// This time there is no need for special terminal
 								// test of '>' as it will be valid inside a quote.
 								token = this.gethTMLReader().readString(ins, '"', '"');
 								if (token != null) {
 									attributeValue = token.substring(0,token.length() - 1);
 								}
 								
 							} else {
 
 								// Element value is not quoted 
 								token = this.gethTMLReader().readString(ins, ' ', '>');
 								attributeValue = token;
 
 							}
 							
							String expandedURL =  expandURL(url,attributeValue);
 							
 							if (expandedURL != null && expandedURL.startsWith("http:")) {
 								uRLList.add(level+1,expandedURL);								
 							}
 							
 
 						}
 
 						token = this.gethTMLReader().readString(ins, '=', '>');
 					}
 
 				}
 
 			}
 
 		}
 
 		ins.close();
 
 		return uRLList;
 
 	}
 
 }
