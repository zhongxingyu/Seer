 package net.guto.steamer;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class Steamer {
 
 	public SteamUser getUser(String steamId) {
 		SteamUser user = new SteamUser(steamId);
 		return user;
 	}
 
 	public List<SteamGame> getGames(String steamId) {
 		SteamGames steamGames = new SteamGames(steamId);
 		return steamGames.getGames();
 	}
 
 	protected static Integer getIntValue(final Field field, final Document document) {
 		return Integer.parseInt((String) getValue(field, document));
 	}
 	
 	protected static Long getLongValue(final Field field, final Document document) {
 		return Long.parseLong((String) getValue(field, document));
 	}
 
 	protected static String getStringValue(final Field field, final Document document) {
 		return (String) getValue(field, document);
 	}
 
 	protected static NodeList getNodeListValue(final Field field, final Document document) {
 		return (NodeList) getValue(field, document);
 	}
 
 
 	private static Object getValue(final Field field, final Document document) {
 		try {
 			return field.getXPath().evaluate(document, field.getDataType());
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	protected static Document getDocument(final String xml) {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			Document document = builder.parse(xml);
 			return document;
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return null;
 		} catch (SAXException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	protected static Document getDocument(final InputStream in) {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			Document document = builder.parse(in);
 			return document;
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return null;
 		} catch (SAXException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static HttpClient client;
 
 	protected static HttpClient getHttpClient() {
 		if (client == null) {
 			client = new DefaultHttpClient();
 		}
 		return client;
 	}
 
 	public static String steamcommunity = "steamcommunity.com";
 	public static String mediasteampowered = "media.steampowered.com";
 
 	//TODO: treate connection errors
 	protected static InputStream connect(String url) {
 		try {
 			HttpClient client = getHttpClient();
 			HttpGet request = new HttpGet();
 			request.setURI(new URI(url));
 			HttpResponse response = client.execute(request);
 			return response.getEntity().getContent();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
