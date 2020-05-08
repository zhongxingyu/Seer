 package com.albertofaci.dropwizard.handlebars;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Provider;
 
 import com.github.jknack.handlebars.Handlebars;
 import com.github.jknack.handlebars.Helper;
 import com.github.jknack.handlebars.Template;
 import com.google.common.io.CharStreams;
 import com.yammer.metrics.core.TimerContext;
 
 @Provider
 @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
 public class HandlebarsViewMessageBodyWriter implements MessageBodyWriter<HandlebarsView> {
 	
 	private Map<String, Helper<Object>> helpers = new HashMap<String, Helper<Object>>();
 	
     public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
         return HandlebarsView.class.isAssignableFrom(type);
     }
 
     public long getSize(HandlebarsView t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
         return -1;
     }
 
     public void writeTo(HandlebarsView handlebarsView, Class<?> type, Type genericType,
             Annotation[] annotations, MediaType mediaType,
             MultivaluedMap<String, Object> httpHeaders,
             OutputStream entityStream) throws IOException,
             WebApplicationException {
 
         final TimerContext context = handlebarsView.getRenderingTimer().time();
 
         try {
         	Handlebars handlebars = new Handlebars();
         	for(Map.Entry<String, Helper<Object>> e: helpers.entrySet()){
         		handlebars.registerHelper(e.getKey(), e.getValue());
         	}
         	String templateName = handlebarsView.getTemplateName();
         	InputStream inputStream = getClass().getResourceAsStream(templateName);
         	if(inputStream == null) {
         		throw new IOException("Could not find template file:" +templateName);
         	}
             String templateString = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));
             Template template = handlebars.compile(templateString);
             String renderedTemplate = template.apply(handlebarsView);
             entityStream.write(renderedTemplate.getBytes());
 		} finally {
             context.stop();
         }
     }
     
     public void registerHelper(String name, Helper<Object> helper) {
     	helpers.put(name, helper);
     }
 
 }
