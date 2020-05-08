 package org.italiangrid.wm.providers;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Provider;
 
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.openssl.PEMWriter;
 
 @Provider
 @Produces("text/plain")
 public class PKCS10CertificationRequestBodyWriter implements MessageBodyWriter<PKCS10CertificationRequest> {
 
 	public long getSize(PKCS10CertificationRequest certificationRequest, Class<?> type, 
 			Type genericType, Annotation[] annotations, MediaType mediaType) {
 		
 		return -1;
 	}
 
 	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
 		
		return PKCS10CertificationRequest.class == type;
 	}
 
 	public void writeTo(PKCS10CertificationRequest certificateRequest, Class<?> type, Type genericType, Annotation[] annotations, 
 			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) 
 					throws IOException, WebApplicationException {
 		
 		OutputStreamWriter writer = new OutputStreamWriter(entityStream);
 		
 		PEMWriter pemWriter = new PEMWriter(writer);
 		
 		pemWriter.writeObject(certificateRequest);
 
 		pemWriter.close();
 	}
 
 }
