 package org.talend;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.impl.DefaultProducer;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.api.client.googleapis.GoogleHeaders;
 import com.google.api.client.googleapis.GoogleTransport;
 import com.google.api.client.googleapis.auth.storage.GoogleStorageAuthentication;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.InputStreamContent;
 
 /**
  * The gcs producer.
  */
 public class gcsProducer extends DefaultProducer {
     private static final transient Logger LOG = LoggerFactory.getLogger(gcsProducer.class);
     private gcsEndpoint endpoint;
 
     private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
     static{
 		httpDateFormat.setTimeZone(TimeZone.getTimeZone("pst"));
 	}
     
     public gcsProducer(gcsEndpoint endpoint) {
         super(endpoint);
         this.endpoint = endpoint;
     }
 
     public void process(Exchange exchange) throws Exception {
    	System.out.println(exchange.getIn().getBody() + " / " + endpoint.getApiKey() + " / " + endpoint.getApiKeySecret()  + " / " + endpoint.getBucket()  + " / " + endpoint.getUrl() );
     	
     	HttpTransport transport = GoogleTransport.create();
 		GoogleStorageAuthentication.authorize(transport, endpoint.getApiKey(), endpoint.getApiKeySecret());
 		
 		HttpRequest request = transport.buildPutRequest();
 		
		FileInputStream testFile = new FileInputStream( endpoint.getFilePath() + "/" + endpoint.getFileName() );
 		
 		InputStreamContent isc = new InputStreamContent();
 		isc.inputStream = new ByteArrayInputStream( IOUtils.toByteArray(testFile) );
 		isc.type = endpoint.getContentType();
 		request.content = isc;
		request.url = new GenericUrl("http://"+ endpoint.getUrl() +"/"+ endpoint.getBucket() +"/" + URLEncoder.encode(endpoint.getFileName(), "utf8"));		
 		GoogleHeaders headers = (GoogleHeaders) request.headers;
 		headers.date = httpDateFormat.format(new Date());
 		try {
 			request.execute();			
 		} catch (HttpResponseException e) {
 			LOG.warn(getStreamContent(e.response.getContent()), e);
 		}
     }
 
     private static String getStreamContent(InputStream in) throws IOException {
 		StringBuffer out = new StringBuffer();
         byte[] b = new byte[4096];
         for (int n; (n = in.read(b)) != -1;) {
             out.append(new String(b, 0, n));
         }
         return out.toString();
 	}
 }
