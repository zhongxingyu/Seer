 package org.analogweb.core;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.analogweb.MediaType;
 import org.analogweb.util.Maps;
 import org.analogweb.util.StringUtils;
 
 /**
  * @author snowgoose
  */
 public final class MediaTypes {
 
     protected final static String WILDCARD_VALUE = "*";
     /** "*&#47;*" */
     public final static MediaType WILDCARD_TYPE = new DefaultMediaType();
     /** "*&#47;*" */
     public final static String WILDCARD = "*/*";
 
     /** "application/xml" */
     public final static String APPLICATION_XML = "application/xml";
     /** "application/xml" */
     public final static MediaType APPLICATION_XML_TYPE = new DefaultMediaType("application", "xml");
 
     /** "application/atom+xml" */
     public final static String APPLICATION_ATOM_XML = "application/atom+xml";
     /** "application/atom+xml" */
     public final static MediaType APPLICATION_ATOM_XML_TYPE = new DefaultMediaType("application",
             "atom+xml");
 
     /** "application/xhtml+xml" */
     public final static String APPLICATION_XHTML_XML = "application/xhtml+xml";
     /** "application/xhtml+xml" */
     public final static MediaType APPLICATION_XHTML_XML_TYPE = new DefaultMediaType("application",
             "xhtml+xml");
 
     /** "application/svg+xml" */
     public final static String APPLICATION_SVG_XML = "application/svg+xml";
     /** "application/svg+xml" */
     public final static MediaType APPLICATION_SVG_XML_TYPE = new DefaultMediaType("application",
             "svg+xml");
 
     /** "application/json" */
     public final static String APPLICATION_JSON = "application/json";
     /** "application/json" */
     public final static MediaType APPLICATION_JSON_TYPE = new DefaultMediaType("application",
             "json");
 
     /** "application/x-www-form-urlencoded" */
     public final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
     /** "application/x-www-form-urlencoded" */
     public final static MediaType APPLICATION_FORM_URLENCODED_TYPE = new DefaultMediaType(
             "application", "x-www-form-urlencoded");
 
     /** "multipart/form-data" */
     public final static String MULTIPART_FORM_DATA = "multipart/form-data";
     /** "multipart/form-data" */
     public final static MediaType MULTIPART_FORM_DATA_TYPE = new DefaultMediaType("multipart",
             "form-data");
 
     /** "application/octet-stream" */
     public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";
     /** "application/octet-stream" */
     public final static MediaType APPLICATION_OCTET_STREAM_TYPE = new DefaultMediaType(
             "application", "octet-stream");
 
     /** "text/plain" */
     public final static String TEXT_PLAIN = "text/plain";
     /** "text/plain" */
     public final static MediaType TEXT_PLAIN_TYPE = new DefaultMediaType("text", "plain");
 
     /** "text/xml" */
     public final static String TEXT_XML = "text/xml";
     /** "text/xml" */
     public final static MediaType TEXT_XML_TYPE = new DefaultMediaType("text", "xml");
 
     /** "text/html" */
     public final static String TEXT_HTML = "text/html";
     /** "text/html" */
     public final static MediaType TEXT_HTML_TYPE = new DefaultMediaType("text", "html");
 
     private static final class DefaultMediaType implements MediaType {
 
         private Map<String, String> parameters;
         private String type = WILDCARD_VALUE;
         private String subType = WILDCARD_VALUE;
         private String value;
 
         DefaultMediaType() {
             this(null, null);
         }
 
         DefaultMediaType(String type, String subType) {
             this(type, subType, null);
         }
 
         @SuppressWarnings("unchecked")
         DefaultMediaType(String type, String subType, Map<String, String> parameters) {
             this.type = (StringUtils.isEmpty(type)) ? WILDCARD_VALUE : type;
             this.subType = (StringUtils.isEmpty(subType)) ? WILDCARD_VALUE : subType;
             this.parameters = (Map<String, String>) ((parameters == null) ? Maps.newEmptyHashMap()
                     : initParameters(parameters));
            this.parameters = Collections.unmodifiableMap(this.parameters);
         }
 
         private Map<String, String> initParameters(Map<String, String> parameters) {
             Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
                 public int compare(String o1, String o2) {
                     return o1.compareToIgnoreCase(o2);
                 }
             });
             for (Map.Entry<String, String> e : parameters.entrySet()) {
                 map.put(e.getKey().toLowerCase(), e.getValue());
             }
             return Collections.unmodifiableMap(map);
         }
 
         @Override
         public String getType() {
             return this.type;
         }
 
         @Override
         public String getSubType() {
             return this.subType;
         }
 
         @Override
         public Map<String, String> getParameters() {
             return this.parameters;
         }
 
         @Override
         public boolean isCompatible(MediaType other) {
             if (other == null) {
                 return false;
             }
             if (getType().equalsIgnoreCase(WILDCARD_VALUE)
                     || other.getType().equalsIgnoreCase(getType())) {
                 return getSubType().equalsIgnoreCase(WILDCARD_VALUE)
                         || other.getSubType().equalsIgnoreCase(getSubType());
             }
             return false;
         }
 
         @Override
         public String toString() {
             if (this.value == null) {
                 StringBuilder b = new StringBuilder();
                 b.append(getType()).append("/").append(getSubType());
                 for (Entry<String, String> e : getParameters().entrySet()) {
                     b.append(';').append(e.getKey()).append('=').append(e.getValue());
                 }
                 this.value = b.toString();
             }
             return this.value;
         }
 
     }
 
     public static MediaType valueOf(String value) {
         if (StringUtils.isEmpty(value)) {
             return WILDCARD_TYPE;
         }
         int s = value.indexOf('/');
         if (s < 1) {
             return WILDCARD_TYPE;
         }
         String type = value.substring(0, s);
         int p = value.indexOf(';');
         String subType = "*";
         Map<String, String> parameterMap = null;
         if (p > s) {
             subType = value.substring(s + 1, p);
             parameterMap = Maps.newEmptyHashMap();
             for (String param : StringUtils.split(value.substring(p + 1), ';')) {
                List<String> sp = StringUtils.split(param.trim(), '=');
                 if (sp.size() > 1) {
                     parameterMap.put(sp.get(0), sp.get(1));
                 }
             }
         } else {
             subType = value.substring(s + 1);
         }
         return new DefaultMediaType(type, subType, parameterMap);
     }
 }
