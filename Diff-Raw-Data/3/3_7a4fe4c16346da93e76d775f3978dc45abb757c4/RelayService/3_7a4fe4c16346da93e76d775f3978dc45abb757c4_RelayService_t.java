 /**
  * 
  */
 package ar.com.cuyum.cnc.service;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.Stateless;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Clase creada para llamada de servicios de listas externas.<br />
  * Principalmente creada para evitar limitaciones por CSRF.
  * @author Jorge Morando
  * 
  */
 @Stateless
 public class RelayService {
 
 	public transient Logger log = Logger.getLogger(RelayService.class);
 	
 	private HttpClient client = new DefaultHttpClient();
 	
 	/*================ POINT OF ENTRANCE =================*/
 	
 	public String submit(URL remoteUrl, String data){
 		return performSubmission(remoteUrl,data);
 	}
 	
 	public String retrieve(URL remoteUrl){
 		return performRetrieval(remoteUrl);
 	}
 	
 	public String request(URL remoteUrl, String fkey){
 		return performRequest(remoteUrl,fkey);
 	}
 
 	private String performSubmission(URL url,String data){
 		HttpPost request = buildSubmission(url,data);
 		HttpResponse rawResponse = execute(request);
 		String responseStr = processResponse(rawResponse);
 		return responseStr;
 	}
 	
 	private String performRetrieval(URL url){
 		HttpGet request = buildRetrieval(url);
 		HttpResponse rawResponse = execute(request);
 		String responseStr = processResponse(rawResponse);
 		return responseStr;
 	}
 	
 	private String performRequest(URL url,String fkey){
 		HttpPost request = buildRequest(url,fkey);
 		HttpResponse rawResponse = execute(request);
 		String responseStr = processResponse(rawResponse);
 		return responseStr;
 	}
 	
 	/*=============== HTTP REQUEST======================*/
 	
 	private final HttpPost buildSubmission(URL url,String data) {
 		HttpPost method = null;
 		try {
 			method = new HttpPost(url.toURI());
 			method.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
 		} catch (Exception e) {
 			log.error("No se pudo construir el request",e);
 		}
 		return method;
 	}
 	
 	private final HttpGet buildRetrieval(URL url) {
 		HttpGet method = null;
 		try {
 			method = new HttpGet(url.toURI());
 		} catch (Exception e) {
 			log.error("No se pudo construir el request",e);
 		}
 		return method;
 	}
 	
 	private final HttpPost buildRequest(URL url,String fkey) {
 		HttpPost method = null;
 		try {
 			method = new HttpPost(url.toURI());
 			
 			List <NameValuePair> params = new ArrayList <NameValuePair>();
 		    params.add(new BasicNameValuePair("fkey", fkey));
 
 		    method.setEntity(new UrlEncodedFormEntity(params));
 			
 		} catch (Exception e) {
 			log.error("No se pudo construir el request",e);
 		}
 		return method;
 	}
 	
 	private HttpResponse execute(HttpRequestBase method) {
 		HttpResponse resp = null;
 		try {
 			log.info("Ejecutando: " + method);
 			resp = client.execute(method);
 		} catch (Exception e) {
 			log.error(e.getMessage());
 		}
 		return resp;
 	}
 	
 	private final String processResponse(HttpResponse rawResponse) {
		String entity = "{\"success\":false,\"msg\":\"Empty response received.\"}";
		if(rawResponse==null)return entity;
 		try {
 			entity = EntityUtils.toString(rawResponse.getEntity());
 		} catch (Exception e) {
 			log.error(e);
 		}
 		return entity;
 	}
 }
