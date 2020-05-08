 package de.fiz.ddb.aas.services;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.ext.ContextResolver;
 import javax.xml.bind.JAXBContext;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.api.json.JSONJAXBContext;
 
//@Provider
 public class JAXBContextResolver implements ContextResolver<JAXBContext> {
     private JAXBContext context;
 
    private Class<?>[] types = {};// OrganizationSearchResult.class, Adresse.class, Organisation.class };
 
     public JAXBContextResolver() throws Exception {
         Map<String, String> ns2json = new HashMap<String, String>();
         ns2json.put("http://deutsche-digitale-bibliothek.de/organization", "org");
         // ns2json.put("http://deutsche-digitale-bibliothek.de/search-result", "");
 
         // mappedJettison with ns mapped to "" update works but root element.
         JSONConfiguration jc = JSONConfiguration.mapped().xml2JsonNs(ns2json).build();
 
         // using static create methods with mappedJettison does not work (does not format, does not rootUnWrap).
         // JSONConfiguration.DEFAULT;
         // jc = JSONConfiguration.createJSONConfigurationWithFormatted(jc, true);
         // jc = JSONConfiguration.createJSONConfigurationWithRootUnwrapping(jc, true);
 
         this.context = new JSONJAXBContext(jc, types);
         // this.context =
         // new JSONJAXBContext(JSONConfiguration.natural().rootUnwrapping(true).humanReadableFormatting(true).build(),
         // types);
     }
 
     public JAXBContext getContext(Class<?> objectType) {
         for (Class<?> type : types) {
             if (type == objectType) {
                 return context;
             }
         }
         return null;
     }
 }
