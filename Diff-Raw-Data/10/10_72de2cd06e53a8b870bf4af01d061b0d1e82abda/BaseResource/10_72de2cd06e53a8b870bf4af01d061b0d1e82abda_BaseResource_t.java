 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.resources;
 
 import java.security.cert.X509Certificate;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.restlet.Request;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.MediaType;
 import org.restlet.ext.freemarker.TemplateRepresentation;
 import org.restlet.resource.ServerResource;
 
 import eu.stratuslab.storage.disk.main.RootApplication;
 import eu.stratuslab.storage.disk.main.ServiceConfiguration;
 import eu.stratuslab.storage.persistence.Disk;
 import freemarker.template.Configuration;
 
 public class BaseResource extends ServerResource {
 
     private static final String CLIENT_CERTS_ATTR = "org.restlet.https.clientCertificates";
 
     public enum DiskVisibility {
         PRIVATE,
         // RESTRICTED,
         PUBLIC;
 
         public static DiskVisibility valueOfIgnoreCase(String value) {
             return valueOf(value.toUpperCase());
         }
     }
 
     public static Map<String, Object> createInfoStructure(String title,
             Request request, String baseUrl) {
 
         Map<String, Object> info = new HashMap<String, Object>();
 
         // Add the title if appropriate.
         if (title != null && !"".equals(title)) {
             info.put("title", title);
         }
 
         // Add the standard base URL declaration.
         info.put("baseurl", baseUrl);
 
         // Add user name information
         info.put("username", getUsername(request));
 
         return info;
     }
 
     public static String getUsername(Request request) {
 
         // Prefer the username and password over the certificate
         // because browsers remember what certificate was used on
         // a site and continue to use it, even if it isn't required.
         ChallengeResponse cr = request.getChallengeResponse();
         if (cr != null) {
             return cr.getIdentifier();
         }
 
         String dn = extractUserDn(request);
         if (!"".equals(dn)) {
             return dn;
         }
 
         // FIXME: This should really throw an exception instead.
         return "UNKNOWN";
     }
 
     @SuppressWarnings("rawtypes")
     protected static String extractUserDn(Request request) {
 
         Map<String, Object> attrs = request.getAttributes();
         Object c = attrs.get(CLIENT_CERTS_ATTR);
 
         if (c instanceof List) {
             List certs = (List) c;
             if (certs.size() > 0) {
                 X509Certificate cert = (X509Certificate) certs.get(0);
                 X500Principal principal = cert.getSubjectX500Principal();
                 String dn = principal.getName();
                 return stripCNProxy(dn);
             }
         }
 
         return "";
 
     }
 
    //                                                                                          
    // Different proxy versions have different DN structures. Old style has                     
    // explicitly CN=proxy at the beginning. The new RFC proxies use                            
    // CN=serial-no with 'serial-no' being a string of digits.                                  
    //                                                                                          
    public static String stripCNProxy(String username) {
        return username.replaceFirst("^CN\\s*=\\s*proxy\\s*,\\s*", "")
                .replaceFirst("^CN\\s*=\\s*\\d+\\s*,\\s*", "");
     }
 
     protected String getBaseUrl() {
         return getRequest().getRootRef().toString();
     }
 
     public static String getBaseUrl(Request request) {
         return request.getRootRef().toString();
     }
 
     protected String getCurrentUrl() {
         return getCurrentUrlWithQueryString().replaceAll("\\?.*", "");
     }
 
     protected String getCurrentUrlWithQueryString() {
         return getRequest().getResourceRef().toString();
     }
 
     protected String getQueryString() {
         return getRequest().getResourceRef().getQuery();
     }
 
     protected Boolean hasQueryString(String key) {
         String queryString = getQueryString();
         return (queryString != null && queryString.equals(key));
     }
 
     protected boolean isSuperUser(String username) {
         return RootApplication.CONFIGURATION.CLOUD_SERVICE_USER
                 .equals(username);
     }
 
     protected String serviceName() {
         return getRequest().getHostRef().getHostDomain();
     }
 
     protected int servicePort() {
         return getRequest().getHostRef().getHostPort();
     }
 
     public Configuration extractFmConfiguration() {
         Request request = getRequest();
         return extractFmConfiguration(request);
     }
 
     public static Configuration extractFmConfiguration(Request request) {
         try {
             Map<String, Object> attributes = request.getAttributes();
             Object value = attributes.get(RootApplication.FM_CONFIGURATION_KEY);
             return (Configuration) value;
         } catch (ClassCastException e) {
             return null;
         }
     }
 
     public ServiceConfiguration extractSvcConfiguration() {
         Request request = getRequest();
         return extractSvcConfiguration(request);
     }
 
     public static ServiceConfiguration extractSvcConfiguration(Request request) {
         try {
             Map<String, Object> attributes = request.getAttributes();
             Object value = attributes
                     .get(RootApplication.SVC_CONFIGURATION_KEY);
             return (ServiceConfiguration) value;
         } catch (ClassCastException e) {
             return null;
         }
     }
 
     protected TemplateRepresentation createTemplateRepresentation(String tpl,
             Map<String, Object> info, MediaType mediaType) {
 
         Configuration freeMarkerConfig = getFreeMarkerConfiguration();
         return createTemplateRepresentation(freeMarkerConfig, tpl, info,
                 mediaType);
 
     }
 
 	private Configuration getFreeMarkerConfiguration() {
 		return ((RootApplication) getApplication())
 				.getFreeMarkerConfiguration();
 	}
     public static TemplateRepresentation createTemplateRepresentation(
 			Configuration freeMarkerConfig, String tpl,
 			Map<String, Object> info, MediaType mediaType) {
 		return new TemplateRepresentation(tpl, freeMarkerConfig, info,
 				mediaType);
 	}
 
 	protected Map<String, Object> createInfoStructure(String title) {
 
 		return createInfoStructure(title, getRequest(), getBaseUrl());
 	}
 
 	protected Boolean hasSufficientRightsToView(Disk disk) {
 		String username = getUsername(getRequest());
 		if (username.equals(disk.getOwner()) || isSuperUser(username)) {
 			return true;
 		}
 
 		DiskVisibility visibility = disk.getVisibility();
 
 		return visibility == DiskVisibility.PUBLIC;
 	}
 
 	protected Boolean hasSufficientRightsToEdit(Disk disk) {
 		String username = getUsername(getRequest());
 		return username.equals(disk.getOwner()) || isSuperUser(username);
 	}
 
 }
