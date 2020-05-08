 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *     documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 20th September 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.server.pages;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfigDescriptions;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfigDescriptions.Property;
 
 /**
  * Configuration page.
  */
 public class ConfigPage extends AbstractPage
 {
     /** Sorted configuration stanzas. */
     private  Map<String, List<Entry<String, String>>> stanzas;
     
     /** Configuration properties. */
     private Map<String, String> props;
     
     /** Configuration descriptions. */
     private IConfigDescriptions desc;
     
     /** Stanza to load. */
     private String uriSuffix;
     
     @Override
     public void preService(HttpServletRequest req)
     {
         this.props = this.config.getAllProperties();
         this.desc = ConfigFactory.getDescriptions();
         
         /* Group the properties by stanza. */
         this.stanzas = new TreeMap<String, List<Entry<String, String>>>();
         for (Entry<String, String> e : this.props.entrySet())
         {
             Property p = this.desc.getPropertyDescription(e.getKey());
             String sta = "Unknown";
             if (p != null)
             {
                 /* The property description exists so add the property to the 
                  * configured stanza. */
                 sta = p.getStanza();
             }
             if (!this.stanzas.containsKey(sta)) this.stanzas.put(sta, new ArrayList<Entry<String, String>>());
             this.stanzas.get(sta).add(e);
         }
         
         String url = "";
         try
         {
             url = URLDecoder.decode(req.getRequestURI(), 
                     req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding());
         }
         catch (UnsupportedEncodingException ex)
         {
            this.logger.error("Unknown character encoding from HTTP request. Reqest character encoding  is '" + 
                    req.getCharacterEncoding() + "'. Going to attempt to use the UTF-8 encoding.");
            try
            {
                url = URLDecoder.decode(req.getRequestURI(), "UTF-8");
            }
            catch (UnsupportedEncodingException e1)
            {
                this.logger.error("Failed URL decoding with 'UTF-8' character encoding, failing request.");
            } 
         }
         
         if (!"/config".equals(url))
         {
             this.uriSuffix = url.substring(url.lastIndexOf("/config") + 8);
             if (this.stanzas.containsKey(this.uriSuffix))
             {
                 this.framing = false;
             }
         }
         else this.uriSuffix = url;
     }
  
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         if (this.stanzas.containsKey(this.uriSuffix))
         {
             if ("POST".equalsIgnoreCase((req.getMethod())))
             {
                 /* Stored the posted values. */
                 // TODO 
             }
             this.getConfigStanza(this.uriSuffix);
         }
         else
         {
             /* Add tabs to page. */
             this.println("<div id='lefttabbar'>");
             this.println("  <ul id='lefttablist'>");
             
             int i = 0;
             for (String s : this.stanzas.keySet())
             {
                 String id = s.replace(" ", "");
                 
                 String classes = "Identity".equals(s) ? "selectedtab" : "notselectedtab";
                 if (i == 0) classes += " ui-corner-tl";
                 else if (i == this.stanzas.size() - 1) classes += " ui-corner-bl";
     
                 this.println("<li><a id='" + id + "tab' class='" + classes + "' onclick='loadConfStanza(\"" + s + "\")'>");
                 this.println("  <div class='conftab'>");
                 this.println("    " + s);
                 this.println("  </div>");
                 this.println("</a></li>");
                 
                 i++;
             }
             
             this.println("  </ul>");
             this.println("</div>");
             
             this.println("<div id='contentspane' class='ui-corner-tr ui-corner-bottom'>");
 	        this.getConfigStanza("Identity");
 	        this.println("</div>");
 	        
 	        this.println("<script type='text/javascript'>");
 	        this.println(
 	                "$(document).ready(function() {\n" +
 	                "     $('#confform').validationEngine();\n" +
 	                "     $('#confform').jqTransform();\n" +
	                "     $('.jqTransformInputWrapper').css('width', '345px');\n" +
 	                "     $('.jqTransformInputInner div input').css('width', '100%');\n");
 	        
 	        /* Contents pane height. */
 	        this.println(
 	                "     $('#contentspane').css('height', $(window).height() - 230);");
 	        this.println(
 	                "     $(window).resize(function() { \n" +
 	                "         $('#contentspane').css('height', $(window).height() - 230);\n" +
 		            "     });");
 	        
 	        this.println("});");
 		
 	        this.println("</script>");
         }
     }
     
     /**
      * Gets the configuration for the supplied stanza.
      * 
      * @param stanza stanza name
      */
     private void getConfigStanza(String stanza)
     {
         List<Entry<String, String>> stanzaProps = this.stanzas.get(stanza);
         Collections.sort(stanzaProps, new EntryComparator());
         
         this.println("<form id='confform' target='/config/" + stanza + "' method='POST'>");
         this.println("  <table id='contentstable'>");
         int i = 0;
         for (Entry<String, String> e : stanzaProps)
         {
             Property p = this.desc.getPropertyDescription(e.getKey());
             
             this.println("<tr class='" + (i % 2 == 0 ? "evenrow" : "oddrow") + "'>");
             
             /* Property details. */
             this.println("  <td class='pcol'>");
            this.println(e.getKey());
             if (p != null)
             {
                 this.println("<div class='pdesc'>");
                 this.println(p.getDescription());
                 this.println("</div>");
             }
             
             this.println("  </td>");
            this.println("  <td class='valcol'>");
             
             if (p != null)
             {
                 String vd = "validate[";
                 vd += p.isMandatory() ? "required" : "optional";
                 switch (p.getType())
                 {
                     case BOOLEAN:
                         break;
                     case CHAR:
                         if (vd.charAt(vd.length() - 1) != '[') vd += ',';
                         vd += "length[1,1]]";
                         this.println("<input id='" + e.getKey() + "' type='text' name='" + e.getKey() + "' class='" + vd + "' value='" + 
                                 e.getValue() + "' />");
                         break;
                     case FLOAT:
                         if (vd.charAt(vd.length() - 1) != '[') vd += ',';
                         vd += "custom[onlyNumber]]";
                         this.println("<input id='" + e.getKey() + "' type='text' name='" + e.getKey() + "' class='" + vd + "' value='" + 
                                 e.getValue() + "' />");
                         break;
                     case INTEGER:
                         if (vd.charAt(vd.length() - 1) != '[') vd += ',';
                         vd += "custom[onlyNumber]]";
                         this.println("<input id='" + e.getKey() + "' type='text' name='" + e.getKey() + "' class='" + vd + "' value='" + 
                                 e.getValue() + "' />");
                         break;
                     case STRING:
                         this.println("<input id='" + e.getKey() + "' type='text' name='" + e.getKey() + "' class='" + vd + "]' value='" + 
                                 e.getValue() + "' />");
                         break;
                 }
             }
             else
             {
                 this.println("<input id='" + e.getKey() + "' type='text' name='" + e.getKey() + "' value='" + 
                         e.getValue() + "' />");
             }
             
             this.println("  </td>");
             this.println("</tr>");
             i++;
         }
         this.println("  </table>");
         this.println("</form>");
     }
 
     @Override
     protected String getPageType()
     {
         return "Configuration";
     }
     
     static class EntryComparator implements Comparator<Entry<String, String>>
     {
         @Override
         public int compare(Entry<String, String> o1, Entry<String, String> o2)
         {
            return o1.getKey().compareTo(o2.getKey());
         }
     }
 }
