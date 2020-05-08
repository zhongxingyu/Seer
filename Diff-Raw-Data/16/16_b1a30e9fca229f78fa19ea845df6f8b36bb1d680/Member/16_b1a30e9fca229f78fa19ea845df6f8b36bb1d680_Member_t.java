 package k11.encrunner;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
import java.util.ArrayList;
 import java.util.Date;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.content.SharedPreferences;
 import android.sax.EndTextElementListener;
 import android.sax.RootElement;
 
 public class Member {
 	public Integer id;
 	public String name;
 	public Integer enc_hp;
 	public Integer position;
 	public Integer character_id;
 	public Integer monster_id;
 	public Date created_at; //Not implemented
 	public Integer encounter_id;
 	public Integer initiative;
 	public Date updated_at; //Not implemented
 	public Boolean visible;
	public ArrayList<Marker> markers;
 	
 	private SharedPreferences prefs;
 	
 	/** Initializes a Member to null values (Date fields as current date) */
 	Member(SharedPreferences prefs)
 	{
		markers = new ArrayList<Marker>();
 		created_at = new Date();
 		updated_at = new Date();
 		this.prefs = prefs;
 	}
 	
 	/** Initializes a Member with the given id and gets data */
 	Member(Integer id,SharedPreferences prefs)
 	{
 		this(prefs);
 		this.id=id;
 		Get();
 	}
 	
 	/** Gets Member data from XML, must have id set */ //TODO Enforce id with exception handling
 	public void Get()
 	{
 		//Setup listeners for XML handling
 		RootElement memberElement = new RootElement("member");
 		
 		memberElement.getChild("character-id").setEndTextElementListener( //TODO throw all this common code into another class, RubyXMLParser?
 				new EndTextElementListener() {
 					public void end(String body) {
 						if (body != "")
 						{
 							character_id = Integer.valueOf(body);
 						}
 					}
 				});
 		
 		memberElement.getChild("monster-id").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						if (body != "")
 						{
 							monster_id = Integer.valueOf(body);
 						}
 					}
 				});
 		
 		memberElement.getChild("name").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						name = body;
 					}
 				});
 		
 		memberElement.getChild("enc-hp").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						enc_hp = Integer.valueOf(body);
 					}
 				});
 		
 		memberElement.getChild("position").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						position = Integer.valueOf(body); //should always equal index +1 of creatureList
 					}
 				});
 		
 //		memberElement.getChild("created-at").setEndTextElementListener( //TODO Implement ISO 8601 parsing
 //				new EndTextElementListener() {
 //					public void end(String body) {
 //						created_at = Time.body;
 //					}
 //				});
 		
 		memberElement.getChild("encounter-id").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						encounter_id = Integer.valueOf(body);
 					}
 				});
 		
 		memberElement.getChild("initiative").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						initiative = Integer.valueOf(body);
 					}
 				});
 		
 //		memberElement.getChild("updated-at").setEndTextElementListener( //TODO Implement ISO 8601 parsing
 //		new EndTextElementListener() {
 //			public void end(String body) {
 //				updated_at = Time.body;
 //			}
 //		});
 		
 		memberElement.getChild("visible").setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						visible = Boolean.valueOf(body);
 					}
 				});
 		
 		//TODO Handle markers
 		
 		//Open HTTP Connection
 		HttpURLConnection urlConnection = null;
 		try {
 		URL url = new URL(Options.getURL(prefs) + "/members/" + id + ".xml?api_key=" + Options.getAPIKey(prefs));
 		urlConnection = (HttpURLConnection) url.openConnection();
 		InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 		
 		//XML Parsing
 
 		SAXParser myParser = (SAXParserFactory.newInstance()).newSAXParser();
 		myParser.parse(in, (DefaultHandler) memberElement.getContentHandler());
 		}catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    finally {
 		     urlConnection.disconnect();
 		    }
 	}
 	
 	/**Updates Member XML with current object data */ //TODO Requires id
 	public void Update()
 	{
 		String xml = "<member>";
 		xml += "<name>" + name + "</name>";
 		xml += "<enc-hp>" + enc_hp + "</enc-hp>";
 		xml += "<position>" + position + "</position>";
 		if (character_id != null)
 			xml += "<character-id>" + character_id + "</character-id>";
 		if (monster_id != null)
 			xml += "<monster-id>" + monster_id + "</monster-id>";
 		xml += "<encounter-id>" + encounter_id + "</encounter-id>";
 		xml += "<initiative>" + initiative + "</initiative>";
 		xml += "<visible>" + visible + "</visible>";
 		xml += "</member>";
 		StringEntity se = null;
 		try {
 			se = new StringEntity(xml,"UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		se.setContentType("application/xml");
 		HttpPut putRequest = new HttpPut(Options.getURL(prefs) + "/members/" + id + ".xml?api_key=" + Options.getAPIKey(prefs));
 		putRequest.setEntity(se);
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpResponse response = null;
 		try {
 			response = httpclient.execute(putRequest);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
