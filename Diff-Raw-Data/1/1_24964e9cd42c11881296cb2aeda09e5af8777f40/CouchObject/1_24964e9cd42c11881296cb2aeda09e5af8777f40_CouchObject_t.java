 package dk.teamonline.util.couchDo;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 public class CouchObject {
 	private URL dbUrl;
 	
 	public CouchObject( URL dbUrl ) {
 		this.dbUrl = dbUrl;
 	}
 	
 	URL getDbUrl() {
 		return dbUrl;
 	}
 
 	void setDbUrl(String dbUrl) throws MalformedURLException {
 		this.dbUrl = new URL( dbUrl );
 	}
 
 	public JSONObject saveJSONObject( JSONObject doc ) {
 		String inLine = "";
 		try {
 			URLConnection con = dbUrl.openConnection();
			con.setRequestProperty("Content-Type", "application/json; charset=iso-8859-1");
 			con.setDoOutput( true );
 			
 			OutputStreamWriter out = new OutputStreamWriter( con.getOutputStream() );
 			out.write( doc.toString() );
 			out.flush();
 			
 			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
 			inLine = in.readLine();
 			out.close();
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return ( JSONObject )JSONValue.parse( inLine );
 	}
 	
 	public JSONObject loadJSONObject( String url ) throws FileNotFoundException, MalformedURLException {
 		try {
 			return loadJSONObject( new URL( this.getDbUrl() + "/" + URLEncoder.encode( url, "UTF-8" ) ) );
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	public JSONObject loadJSONObject( URL url ) throws java.io.FileNotFoundException {
 		System.out.println( "Request: " + url.toString() );
 		JSONObject json = new JSONObject();
 		try {
 			URLConnection con = url.openConnection();
 			BufferedReader reader = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
 			
 			StringBuilder jsonString = new StringBuilder();
 			String lastLine;
 			while( ( lastLine = reader.readLine() ) != null ) {
 				jsonString.append( lastLine );	
 			}
 			json = ( JSONObject )JSONValue.parse( jsonString.toString() );
 
 			reader.close();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch ( java.io.FileNotFoundException e ) {
 			throw e;
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		
 		System.out.println( "Read JSONObject: " + json.toString() );
 		return json;
 	}
 
 	public JSONObject deleteJSOBObject( JSONObject doc ) {
 		String inLine = "";
 		try {
 			String id = doc.get( "_id" ).toString();
 			String rev = doc.get( "_rev" ).toString();
 			URL deleteUrl = new URL( dbUrl.toString() + "/" + URLEncoder.encode( id, "UTF-8" ) + "?rev=" + rev );
 			
 			HttpURLConnection con  = (HttpURLConnection)deleteUrl.openConnection();
 			con.setDoOutput( true );
 			con.setRequestMethod( "DELETE" );
 			con.setRequestProperty(
 				    "Content-Type", "application/x-www-form-urlencoded" );
 			con.connect();
 			
 			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
 			inLine = in.readLine();
 			in.close();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		System.out.println( "Delete JSONObject: " + doc.toString() );
 		return ( JSONObject )JSONValue.parse( inLine );
 	}
 }
