 package com.openstat;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.binary.Base64;
 
 /**
  * Basic Openstat marker container, parsing & encoding class.
  *
  * Openstat marker consists of 4 fields:
  * <ul>
  * <li><b>service</b> - identifies a web service (engine) that powers the
  * advertising</li>
  * <li><b>campaign</b> - identifies an advertising campaign (i.e. a group of
  * ads with a single purpose)</li>
  * <li><b>ad</b> - identifies a particular ad</li>
  * <li><b>source</b> - identifies where this particular ad was shown (i.e.,
  * site, positition inside that site, etc)</li>
  * </ul>
  *
  * Instance of this class can contain these fields and deal with encoding /
  * decoding of fields in several forms.
  *
  * @see <a href="http://marker.openstat.ru">Openstat Marker official site</a>
  */
 public class OpenstatMarker {
     /**
      * Constant name of key to look for in HTTP query key-value pairs in search
      * for a valid Openstat marker.
      */
     public static final String PREFIX = "_openstat";
 
     /**
      * Openstat marker is always UTF8 encoded.
      */
     private static final Charset UTF8 = Charset.forName("utf-8");
 
    private static final Pattern BASE64_PATTERN = Pattern.compile("[A-Za-z0-9_-]+=*");
 
     private String service;
     private String campaign;
     private String ad;
     private String source;
 
     /**
      * Constructs a new Openstat marker using provided strings as fields.
      * @param service
      * @param campaign
      * @param ad
      * @param source
      */
     public OpenstatMarker(String service, String campaign, String ad, String source) {
         setService(service);
         setCampaign(campaign);
         setAd(ad);
         setSource(source);
     }
 
     /**
      * Sets "service" field of Openstat marker.
      * @param service the service to set
      */
     public void setService(String service) {
         this.service = service;
     }
 
     /**
      * Retrieves "service" field of Openstat marker.
      * @return the service
      */
     public String getService() {
         return service;
     }
 
     /**
      * Sets "campaign" field of Openstat marker.
      * @param campaign the campaign to set
      */
     public void setCampaign(String campaign) {
         this.campaign = campaign;
     }
 
     /**
      * Retrieves "campaign" field of Openstat marker.
      * @return the campaign
      */
     public String getCampaign() {
         return campaign;
     }
 
     /**
      * Sets "ad" field of Openstat marker.
      * @param ad the ad to set
      */
     public void setAd(String ad) {
         this.ad = ad;
     }
 
     /**
      * Retrieves "ad" field of Openstat marker.
      * @return the ad
      */
     public String getAd() {
         return ad;
     }
 
     /**
      * Sets "source" field of Openstat marker.
      * @param source the source to set
      */
     public void setSource(String source) {
         this.source = source;
     }
 
     /**
      * Retrieves "source" field of Openstat marker.
      * @return the source
      */
     public String getSource() {
         return source;
     }
 
     /**
      * Returns Openstat marker fields as array, in the order they would
      * appear in string-serialized form by standard.
      * @return array with 4 elements: service, campaign, ad, source fields
      */
     public String[] asArray() {
         return new String[] {
                 service,
                 campaign,
                 ad,
                 source
         };
     }
 
     /**
      * Returns Openstat marker as encoded string, optionally using modified
      * Base64 encoding to conceal contents from human's eye.
      * @param base64
      * @return marker as encoded or raw string
      */
     public String encodedMarker(boolean base64) {
         StringBuilder sb = new StringBuilder(service);
         sb.append(';');
         sb.append(campaign);
         sb.append(';');
         sb.append(ad);
         sb.append(';');
         sb.append(source);
 
         String s = sb.toString();
 
         if (base64) {
             return modifyBase64(new String(Base64.encodeBase64(s.getBytes(UTF8)), UTF8));
         } else {
             return s;
         }
     }
 
     @Override
     public String toString() {
         return encodedMarker(false);
     }
 
     @Override
     public int hashCode() {
         return toString().hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof OpenstatMarker))
             return false;
 
         OpenstatMarker m = (OpenstatMarker) obj;
         return service.equals(m.service)
             && campaign.equals(m.campaign)
             && ad.equals(m.ad)
             && source.equals(m.source);
     }
 
     /**
      * Parses Openstat marker from a given URL; Openstat marker can be in
      * both encoded and raw forms and can appear in query or fragment parts
      * of URL.
      * @param spec URL to parse in string form
      * @return Openstat marker object or null if no marker was found
      * @throws MalformedURLException if URL can't be parsed
      */
     public static OpenstatMarker parseURL(String spec) throws MalformedURLException {
         URL u = new URL(spec);
         String query = u.getQuery();
         OpenstatMarker m;
 
         // Try to find a marker in query
         m = parseQuery(query);
         if (m != null)
             return m;
 
         // Try to find a marker in fragment
         m = parseFrag(u.getRef());
         return m;
     }
 
     /**
      * Parses Openstat marker from HTTP URL's query string.
      * @param query HTTP URL query string (for example, "k1=v1&_openstat=1;2;3;4&k2=v2")
      * @return Openstat marker object or null if no marker was found
      */
     public static OpenstatMarker parseQuery(String query) {
         if (query == null)
             return null;
         RequestParameters rp = new RequestParameters(query);
         return parseMarker(rp.getParameter(PREFIX));
     }
 
     /**
      * Parses Openstat marker from HTTP URL's fragment string.
      * @param frag HTTP URL fragment string (for example, "AAA_openstat=1;2;3;4")
      * @return Openstat marker object or null if no marker was found
      */
     public static OpenstatMarker parseFrag(String frag) {
         if (frag == null)
             return null;
         int p1 = frag.indexOf(PREFIX + "=");
         if (p1 == -1)
             return null;
         return parseMarker(frag.substring(p1 + PREFIX.length() + 1));
     }
 
     /**
      * Parses Openstat marker, which can be in "encoded" or "raw" forms,
      * automatically detecting if encoding is used, and returns an instance
      * of OpenstatMarker. For reverse operation, see
      * {@link OpenstatMarker#encodedMarker(boolean)}.
      * @param marker
      * @return Openstat marker object or null if no marker was found
      */
     public static OpenstatMarker parseMarker(String marker) {
         if (marker == null)
             return null;
 
         Matcher matcher = BASE64_PATTERN.matcher(marker);
         if (matcher.matches()) {
             // Try decoding base64 label
 
             // replace URI alphabet
             String m = unmodifyBase64(marker);
 
             return parseRawMarker(new String(Base64.decodeBase64(m.getBytes(UTF8)), UTF8));
         } else {
             // Try decoding it as raw marker
             return parseRawMarker(marker);
         }
     }
 
     /**
      * Parses "raw" Openstat marker (i.e. "aaa;bbb;ccc;ddd") and returns an
      * instance of OpenstatMarker. For reverse operation, see
      * {@link OpenstatMarker#toString()}.
      * @param rawMarker raw Openstat marker
      * @return Openstat marker object or null if no marker was found
      */
     public static OpenstatMarker parseRawMarker(String rawMarker) {
         String[] components = rawMarker.split(";", -1);
         if (components.length == 4) {
             return new OpenstatMarker(components[0], components[1], components[2], components[3]);
         } else {
             return null;
         }
     }
 
     /**
      * Converts a regular base64-encoded string into a URL-safe modified
      * base64-encoded string.
      * @param s regular base64-encoded string
      * @return modified base64-encoded string
      */
     private static String modifyBase64(String s) {
         // replace some characters
         String m = s.replace('+', '-').replace('/', '_');
 
         int cut = m.length();
         while (cut >= 0 && m.charAt(cut - 1) == '=') {
             cut--;
         }
         return m.substring(0, cut);
     }
 
     /**
      * Converts an URL-safe modified base64-encoded string into a regular
      * base64-encoded string.
      * @param s modified base64-encoded string
      * @return regular base64-encoded string
      */
     private static String unmodifyBase64(String s) {
         // replace some characters
         String m = s.replace('-', '+').replace('_', '/');
 
         // add padding to work around Base64 incorrect length after decoding
         switch (m.length() % 4) {
         case 1: m += "==="; break;
         case 2: m += "=="; break;
         case 3: m += "="; break;
         default:
             // add nothing
         }
 
         return m;
     }
 }
