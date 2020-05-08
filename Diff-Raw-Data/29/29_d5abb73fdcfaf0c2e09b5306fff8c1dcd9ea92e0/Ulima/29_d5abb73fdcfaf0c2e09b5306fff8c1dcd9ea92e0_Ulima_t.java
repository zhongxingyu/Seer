 package models;
 
 /*
  * Author : Daniel Flores
  */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.CookiePolicy;
 import java.net.CookieStore;
 import java.net.HttpCookie;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class Ulima {
 	
 	public static String login(String codigo, String password) throws Exception{
 		String response = "";
 		try {
 			
 			/*
 			 * This cookie manager is important for save cookies through the request
 			 */
 			CookieManager manager = new CookieManager();
 			manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
 			CookieHandler.setDefault(manager);
 			
 			System.setProperty("http.keepAlive", "false");
 			String url = "https://webaloe.ulima.edu.pe/portalUL/j_security_check";
 			String charset = "ISO-8859-1";
 			String param1 = codigo;
 			String param2 = password;
 			String query = String.format("j_username=%s&j_password=%s",
 					URLEncoder.encode(param1,charset),
 					URLEncoder.encode(param2,charset));
 			
 			HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 			connection.setDoOutput(true);
 			connection.setInstanceFollowRedirects(true);
 			connection.setRequestProperty("Accept-Charset",charset);
 			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=" + charset);
 			OutputStream output = null;
 			
 			//System.out.println("RESPONSE --> : " + connection.getResponseCode());
 			
 			try{
 				output = connection.getOutputStream();
 				output.write(query.getBytes(charset));
 			}finally{
 				if(output != null) try { output.close(); } catch(IOException logOrIgnore){
 					System.out.println("FAIL");
 				}
 			}
 			
 			// Make request
 			connection.connect();
 
 			// THIS LINE OF CODE IS SO IMPORTANT FOR SAVE THE COOKIES IN THE COOKIESTORE [*]
 			connection.getHeaderFields().get("Set-Cookie");
 
 			// Detect if the user and password are correct
 			CookieStore cookieStore = manager.getCookieStore();
 			List<HttpCookie> cookies = cookieStore.getCookies();
 			
 			for(HttpCookie cookie: cookies){
 				//System.out.println(cookie.getName() + " --> " + cookie.getValue());
 			}
 
 			connection.disconnect();
 			
 			// The cookies for session has been saved by the cookie manager.
 			// Now the application make a request to another URL ["Consolidado de Matrícula"]
 			String url1 = "http://webaloe.ulima.edu.pe/portalUL/layout.jsp";
 			String url2 = "http://webaloe.ulima.edu.pe/portalUL/gama/servlets/ComandoMostrarConsMatr?COCICLO=20121&Fg=1";
 			URLConnection connection2 = new URL(url2).openConnection();
 			connection2.setRequestProperty("Accept-Charset", charset);
 			connection2.setDoInput(true);
 			connection2.setDoOutput(true);
 			
 			// Read and show response
 			
 		    BufferedReader reader = null;
 		    
 		    try {
 		        reader = new BufferedReader(new InputStreamReader(connection2.getInputStream(), charset));
 		        for (String line; (line = reader.readLine()) != null;) {
 		            response+=line;
 		        }
 		    } finally {
 		        if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
 		    }
 		    
 		    System.out.println("RESPONSE --> : " + connection.getResponseCode());
 			
 		    
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		
		System.out.println("length --> " + response.length());
		
		if(response.length() <= 460){
 			// Not user
 			throw new Exception("No es usuario");
 		}else{
 			return response;
 		}
 	}
 
 	
 	public static List<CursoInfo> getCourses(String html){
 		
 		// READ TABLE 87
 		Document doc = Jsoup.parse(html);
 		
 		Elements tables = doc.getElementsByTag("table");
 		
 		// For information about the user, the number of table is 4
 		Element tableInfo = tables.get(4);
 		System.out.println(tableInfo.child(0).toString());
 
 		// FOR ["CONSOLIDAD DE MATRICULA"] the number of table is 5, whereas for miulima is 87
 		Element table = tables.get(5);
 		
 		Element tbody = table.child(0);
 		
 		//List<Node> nodes = tbody.childNodes();
 		
 		Elements nodes = tbody.children();
 		
 		Elements nodeCourses = new Elements();
 		
 		System.out.println(String.valueOf(nodes.size()));
 		
 		for(int i=0;i<nodes.size();i++){
 			//System.out.println("ELEMENT------------------------------------");
 			String nodeContent = nodes.get(i).toString().trim();
 			//System.out.println(nodeContent.length());
 			if(nodeContent.length() != 0){
 				nodeCourses.add(nodes.get(i));
 			}
 		}
 		
 		List<CursoInfo> cursos = new ArrayList<CursoInfo>();
 		
 		for(int j=1;j<nodeCourses.size();j++){
 			Elements td = nodeCourses.get(j).children();
 			
 			// CODIGO DE CURSO
 			String codigo = td.get(1).text();
 			String nombre = td.get(4).text();
 			String seccion = td.get(2).text();
 
 			System.out.println(codigo + " " + nombre + " " + seccion);
 			
 			CursoInfo curso = new CursoInfo();
 				  curso.setCodigo(codigo.trim());
 				  curso.setNombre(nombre.trim());
 				  curso.setSeccion(Integer.parseInt(seccion.trim()));
 				  
 			cursos.add(curso);
 			//System.out.println("CURSO --> " + courseInfo);
 		}
 		
 		return cursos;
 	}
 
 
 	public static void main(String[] args){
 		String html;
 		try {
 			html = login("20082219","XXXXXX");
 			List<CursoInfo> cursos = getCourses(html);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 		}		
 		//List<CursoInfo> cursos = getCourses(html);
 	}
 
 	// How to run this with cmd?
 	// javac -cp .;C:/jsoup-1.6.3.jar models/Ulima.java
 	// java -cp .;C:/jsoup-1.6.3.jar models.Ulima
 	
 	
 
 }
