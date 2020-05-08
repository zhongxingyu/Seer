 /*
  * HTMLUrlRenderer.java
  *
  * Created on October 18, 2001, 3:32 AM
  */
 
 package org.tcdi.opensource.wiki.renderer;
 
 import org.tcdi.opensource.wiki.*;
 
 /**
  *
  * @author  e_ridge
  */
 public class HTMLURLRenderer implements WikiURLRenderer {
     
     private final WikiSystem _wiki;
    
     public HTMLURLRenderer(WikiSystem wiki) {
         _wiki = wiki;
     }
     
     public String renderURL(String url) {
         if (url == null)
             return null;
         
         int l_paren = url.indexOf('(');
         int r_paren = url.indexOf(')');
         String label;
         if (r_paren > l_paren && l_paren != -1) {
             label = url.substring(l_paren+1, r_paren);
             url = url.substring(0, l_paren);
         } else {
             label = url;
         }
         
         int idx = url.indexOf(':');
         String protocol = url.substring(0, idx);
         String location = url.substring(idx+1);
         
         StringBuffer sb = new StringBuffer();
         
         if (protocol.equalsIgnoreCase("http")
         || protocol.equalsIgnoreCase("ftp")
         || protocol.equalsIgnoreCase("gopher")) {
             sb.append("<a href=\"")
             .append(url)
             .append("\" target=_blank>")
             .append(label)
             .append("</a>");
         } else if (protocol.equalsIgnoreCase("mailto")) {
             sb.append("<a href=\"")
             .append(url)
             .append("\">")
             .append(label)
             .append("</a>");
        } else if (protocol.equalsIgnoreCase("javadoc")) {
             sb.append("<a target=javadoc href=\"")
             .append(_wiki.getProperties().getProperty("JavaDocRoot"))
             .append(location.replace('.', '/'))
             .append(".html\">")
             .append(label)
             .append("</a>");
         } else if (protocol.equalsIgnoreCase("image")) {
             sb.append("<img src=\"")
             .append(location)
             .append("\" border=0>");
         } else {  // unrecognized protocol
             return url;
         }
         
         return sb.toString();
     }
     
 }
