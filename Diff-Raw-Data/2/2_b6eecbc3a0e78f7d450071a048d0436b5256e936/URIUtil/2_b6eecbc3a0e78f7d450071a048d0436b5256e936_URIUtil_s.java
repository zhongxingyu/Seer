 /*
  * Copyright 2010 Kodapan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package se.kodapan.net;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import se.kodapan.collections.ListMap;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class URIUtil {
 
   private static final Logger log = LoggerFactory.getLogger(URIUtil.class);
 
   public static ListMap<String, String> resolveQueryValues(URI uri) {
     ListMap<String, String> map = new ListMap<String, String>();
     if (uri.getQuery() != null) {
       for (String pair : uri.getQuery().split("&")) {
         String[] parts = pair.split("=");
         if (parts.length > 0) {
           map.add(parts[0], parts[1]);
         } else {
           if (map.get(parts[0]) == null) {
             map.put(parts[0], new ArrayList<String>());
           }
         }
       }
     }
     return map;
   }
 
   public static URI resolve(String ownerURI, String href) throws URISyntaxException {
     return resolve(log, ownerURI, href);
   }
 
   public static URI resolve(Logger log, String ownerURI, String href) throws URISyntaxException {
     return resolve(log, toURI(log, ownerURI), href);
   }
 
   public static URI toURI(String uri) throws URISyntaxException {
     return toURI(log, uri);
   }
 
   public static URI toURI(Logger log, String uri) throws URISyntaxException {
     try {
       return new URI(uri);
     } catch (URISyntaxException e) {
       log.debug("URI syntax error in string value '" + uri + "', attempting to semi-encode.");
       return new URI(semiEncode(uri));
     }
   }
 
   public static URI resolve(URI uri, String value) {
     return resolve(log, uri, value);
   }
 
   /**
    * this is not safe!
    * output urls might be crippled and not comaptible with how firefox and other browsers might intreprete the url!
    */
   public static URI resolve(Logger log, URI uri, String value) {
     if (value == null || uri == null) {
       throw new NullPointerException();
     }
     value = value.trim();
     try {
       return uri.resolve(value);
     } catch (IllegalArgumentException e) {
       log.debug("URI syntax error in string value '" + value + "', attempting to semi-encode.");
       return uri.resolve(semiEncode(value));
     }
   }
 
   // ERROR - ImageHarvester             - Bad! documentURI: http://www.svenskfast.se/Templates/ObjectView.aspx?objectid=3M73ETT7D4UCJURE value: http://karta.hitta.se/mapstore/service/image/0/10/6406185/1400761/90/63?filter={name:markers,props:{pe:[1400761],pn:[6406185]}}
 
   public static void main(String[] args) throws Exception {
     resolve(log, URI.create("http://www.svenskfast.se/Templates/ObjectView.aspx?objectid=3M73ETT7D4UCJURE"), "http://karta.hitta.se/mapstore/service/image/0/10/6406185/1400761/90/63?filter={name:markers,props:{pe:[1400761],pn:[6406185]}}");
   }
 
   private static final Pattern formattedPercentUTF = Pattern.compile("%u([0-9a-fA-F]{4})");
 
 
   public static String semiEncode(String input) {
     StringBuilder out = new StringBuilder(input.length() + 50);
     boolean inPath = true; // keep the first ?, denoting begining of query
     for (char c : input.toCharArray()) {
       switch (c) {
         case '&':
           out.append(inPath ? "%26" : c);
           break;
         case '?':
           out.append(inPath ? c : "%3F");
           inPath = false;
           break;
         case ' ':
           out.append(inPath ? "%20" : '+');
           break;
         case '|':
           out.append("%7C");
           break;
         case '[':
           out.append("%5B");
           break;
         case ']':
           out.append("%5D");
           break;
         case '{':
           out.append("%7B");
           break;
         case '}':
           out.append("%7D");
           break;
 //        case ',':
 //          out.append("%2C");
 //          break;
         case 'œ':
           out.append(inPath ? c : "%9C");
           break;
         case 'À':
           out.append(inPath ? c : "%C0");
           break;
         case 'Á':
           out.append(inPath ? c : "%C1");
           break;
         case 'Â':
           out.append(inPath ? c : "%C2");
           break;
         case 'Ã':
           out.append(inPath ? c : "%C3");
           break;
         case 'Ä':
           out.append(inPath ? c : "%C4");
           break;
         case 'Å':
           out.append(inPath ? c : "%C5");
           break;
         case 'Æ':
           out.append(inPath ? c : "%C6");
           break;
         case 'Ç':
           out.append(inPath ? c : "%C7");
           break;
         case 'È':
           out.append(inPath ? c : "%C8");
           break;
         case 'É':
           out.append(inPath ? c : "%C9");
           break;
         case 'Ê':
           out.append(inPath ? c : "%CA");
           break;
         case 'Ë':
           out.append(inPath ? c : "%CB");
           break;
         case 'Ì':
           out.append(inPath ? c : "%CC");
           break;
         case 'Í':
           out.append(inPath ? c : "%CD");
           break;
         case 'Î':
           out.append(inPath ? c : "%CE");
           break;
         case 'Ï':
           out.append(inPath ? c : "%CF");
           break;
         case 'Ð':
           out.append(inPath ? c : "%D0");
           break;
         case 'Ñ':
           out.append(inPath ? c : "%D1");
           break;
         case 'Ò':
           out.append(inPath ? c : "%D2");
           break;
         case 'Ó':
           out.append(inPath ? c : "%D3");
           break;
         case 'Ô':
           out.append(inPath ? c : "%D4");
           break;
         case 'Õ':
           out.append(inPath ? c : "%D5");
           break;
         case 'Ö':
           out.append(inPath ? c : "%D6");
           break;
         case 'Ø':
           out.append(inPath ? c : "%D8");
           break;
         case 'Ù':
           out.append(inPath ? c : "%D9");
           break;
         case 'Ú':
           out.append(inPath ? c : "%DA");
           break;
         case 'Û':
           out.append(inPath ? c : "%DB");
           break;
         case 'Ü':
           out.append(inPath ? c : "%DC");
           break;
         case 'Ý':
           out.append(inPath ? c : "%DD");
           break;
         case 'Þ':
           out.append(inPath ? c : "%DE");
           break;
         case 'ß':
           out.append(inPath ? c : "%DF");
           break;
         case 'à':
           out.append(inPath ? c : "%E0");
           break;
         case 'á':
           out.append(inPath ? c : "%E1");
           break;
         case 'â':
           out.append(inPath ? c : "%E2");
           break;
         case 'ã':
           out.append(inPath ? c : "%E3");
           break;
         case 'ä':
           out.append(inPath ? c : "%E4");
           break;
         case 'å':
           out.append(inPath ? c : "%E5");
           break;
         case 'æ':
           out.append(inPath ? c : "%E6");
           break;
         case 'ç':
           out.append(inPath ? c : "%E7");
           break;
         case 'è':
           out.append(inPath ? c : "%E8");
           break;
         case 'é':
           out.append(inPath ? c : "%E9");
           break;
         case 'ê':
           out.append(inPath ? c : "%EA");
           break;
         case 'ë':
           out.append(inPath ? c : "%EB");
           break;
         case 'ì':
           out.append(inPath ? c : "%EC");
           break;
         case 'í':
           out.append(inPath ? c : "%ED");
           break;
         case 'î':
           out.append(inPath ? c : "%EE");
           break;
         case 'ï':
           out.append(inPath ? c : "%EF");
           break;
         case 'ð':
           out.append(inPath ? c : "%F0");
           break;
         case 'ñ':
           out.append(inPath ? c : "%F1");
           break;
         case 'ò':
           out.append(inPath ? c : "%F2");
           break;
         case 'ó':
           out.append(inPath ? c : "%F3");
           break;
         case 'ô':
           out.append(inPath ? c : "%F4");
           break;
         case 'õ':
           out.append(inPath ? c : "%F5");
           break;
         case 'ö':
           out.append(inPath ? c : "%F6");
           break;
         case 'ø':
           out.append(inPath ? c : "%F8");
           break;
         case 'ù':
           out.append(inPath ? c : "%F9");
           break;
         case 'ú':
           out.append(inPath ? c : "%FA");
           break;
         case 'û':
           out.append(inPath ? c : "%FB");
           break;
         case 'ü':
           out.append(inPath ? c : "%FC");
           break;
         case 'ý':
           out.append(inPath ? c : "%FD");
           break;
         case 'þ':
           out.append(inPath ? c : "%FE");
           break;
         case 'ÿ':
           out.append(inPath ? c : "%FF");
           break;
         default:
           out.append(c);
           break;
       }
     }
 
     // from http path and forward
     // re-encode UTF-16 from %u00f6 -> UTF-8 HTML encoded value
     int builderOffset = 0;
     int matcherPos = 0;
     Matcher matcher = formattedPercentUTF.matcher(out.toString());
     while (matcher.find(matcherPos)) {
       String hex = matcher.group(1);
       byte[] bytes = new byte[2];
       bytes[0] = (byte) Integer.parseInt(hex.substring(0, 2), 16);
       bytes[1] = (byte) Integer.parseInt(hex.substring(2, 4), 16);
       DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
       String decoded;
       try {
         char utf = dis.readChar();
         decoded = String.valueOf(utf);
         dis.close();
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
       try {
         decoded = URLEncoder.encode(decoded, "UTF-8");
       } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
       }
       out.replace(builderOffset + matcher.start(), builderOffset + matcher.end(), decoded);
       matcherPos = matcher.end();
       builderOffset += matcher.end() - matcher.start() + decoded.length();
     }
 
     return out.toString();
   }
 
   public static String getQueryParameter(URI uri, String parameter, boolean caseSensitive) {
     String value = null;
     for (QueryParameter queryParameter : getQueryParameters(uri)) {
       if (caseSensitive) {
         if (parameter.equals(queryParameter.getName())) {
           if (value != null) {
             throw new RuntimeException("Several parameters match name " + parameter);
           }
           value = queryParameter.getValue();
         }
       } else {
         if (parameter.equalsIgnoreCase(queryParameter.getName())) {
           if (value != null) {
             throw new RuntimeException("Several parameters match name " + parameter);
           }
           value = queryParameter.getValue();
         }
       }
     }
     return value;
   }
 
   public static List<QueryParameter> getQueryParameters(URI uri) {
     if (uri.getQuery() == null || uri.getQuery().length() == 0) {
       return Collections.emptyList();
     }
 
     List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
 
     String[] params = uri.getQuery().split("&");
     for (String param : params) {
       if (param.indexOf("=") > -1) {
         String[] split = param.split("=");
        queryParameters.add(new QueryParameter(split[0], split[1]));
       } else {
         queryParameters.add(new QueryParameter(param));
       }
     }
     return queryParameters;
   }
 
 
   public static class QueryParameter {
     private String name;
     private String value;
 
     public QueryParameter() {
     }
 
     public QueryParameter(String name) {
       this.name = name;
     }
 
     public QueryParameter(String name, String value) {
       this.name = name;
       this.value = value;
     }
 
     public String getName() {
       return name;
     }
 
     public void setName(String name) {
       this.name = name;
     }
 
     public String getValue() {
       return value;
     }
 
     public void setValue(String value) {
       this.value = value;
     }
   }
 
 }
