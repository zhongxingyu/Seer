 package gov.nih.nci.evs.browser.utils;
 
 import java.util.*;
 
 import javax.servlet.http.*;
 
 import org.apache.log4j.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 public class JSPUtils {
     private static Logger _logger = Logger.getLogger(JSPUtils.class);
 
     public static boolean isNull(String text) {
         return text == null || text.equalsIgnoreCase("null");
     }
 
     public static class JSPHeaderInfo {
         public String dictionary;
         public String version;
         public String version_deprecated;
         
         private void debugAllVersions(HttpServletRequest request) {
             String dictionary = request.getParameter("dictionary");
             String version = request.getParameter("version");
             String prefix = "TESTING: ";
             _logger.debug(Utils.SEPARATOR);
             _logger.debug(prefix + "Request Parameters: " + 
                 "version=" + version + ", dictionary=" + dictionary);
             
             dictionary = (String) request.getAttribute("dictionary");
             version = (String) request.getAttribute("version");
             _logger.debug(prefix + "Request Attributes: " + 
                 "version=" + version + ", dictionary=" + dictionary);
 
             dictionary = (String) request.getSession().getAttribute("dictionary");
             version = (String) request.getSession().getAttribute("version");
             _logger.debug(prefix + "Session Attributes: " + 
                 "version=" + version + ", dictionary=" + dictionary);
         }
 
         public JSPHeaderInfo(HttpServletRequest request) {
             // debugAllVersions(request);
             dictionary = request.getParameter("dictionary");
             version = request.getParameter("version");
             _logger.debug(Utils.SEPARATOR);
             _logger.debug("Request Parameters: " + 
                 "version=" + version + ", dictionary=" + dictionary);
 
             if (isNull(dictionary) && isNull(version)) {
                 dictionary = (String) request.getAttribute("dictionary");
                 version = (String) request.getAttribute("version");
                 _logger.debug("Request Attributes: " + 
                     "version=" + version + ", dictionary=" + dictionary);
             }
 
             if (isNull(dictionary) && isNull(version)) {
                 dictionary = (String) request.getSession().getAttribute("dictionary");
                 version = (String) request.getSession().getAttribute("version");
                 _logger.debug("Session Attributes: " + 
                     "version=" + version + ", dictionary=" + dictionary);
             }
 
             if (isNull(dictionary) && ! isNull(version)) {
                 _logger.debug("Defaulting dictionary to: NCI Thesaurus");
                 dictionary = "NCI Thesaurus";
             }
             
            if (!DataUtils.isCodingSchemeLoaded(dictionary, version)) {
                 version_deprecated = version;
                 version =
                     DataUtils.getVocabularyVersionByTag(dictionary,
                         "PRODUCTION");
                 _logger.debug(Utils.SEPARATOR);
                 _logger.debug("dictionary: " + dictionary);
                 _logger.debug("  * version: " + version);
                 if (version_deprecated != null)
                     _logger.debug("  * version_deprecated: " + version_deprecated);
                 else _logger.debug("  * Note: Version was not specified.  Defaulting to producion.");
             }
             request.getSession().setAttribute("dictionary", dictionary);
             request.getSession().setAttribute("version", version);
         }
     }
 
     public static class JSPHeaderInfoMore extends JSPHeaderInfo {
         public String display_name;
         public String term_browser_version;
 
         public JSPHeaderInfoMore(HttpServletRequest request) {
             super(request);
             String localName = DataUtils.getLocalName(dictionary);
             String formalName = DataUtils.getFormalName(localName);
 
             display_name =
                 DataUtils
                     .getMetadataValue(formalName, version, "display_name");
             if (isNull(display_name))
                 display_name = localName;
 
             term_browser_version =
                 DataUtils.getMetadataValue(formalName, version,
                     "term_browser_version");
             if (isNull(term_browser_version))
                 term_browser_version = version;
         }
     }
 
     public static String getSelectedVocabularyTooltip(HttpServletRequest request) {
         String ontologiesToSearchOn =
             (String) request.getSession().getAttribute("ontologiesToSearchOn");
         if (ontologiesToSearchOn == null)
             return "";
 
         @SuppressWarnings("unchecked")
         HashMap<String, String> display_name_hmap =
             (HashMap<String, String>) request.getSession().getAttribute(
                 "display_name_hmap");
         @SuppressWarnings("unchecked")
         Vector<OntologyInfo> display_name_vec =
             (Vector<OntologyInfo>) request.getSession().getAttribute(
                 "display_name_vec");
 
         Vector<String> ontologies_to_search_on =
             DataUtils.parseData(ontologiesToSearchOn);
         String value = "";
         for (int i = 0; i < ontologies_to_search_on.size(); i++) {
             String s = ontologies_to_search_on.elementAt(i);
             String t1 = DataUtils.key2CodingSchemeName(s);
             String v1 = DataUtils.key2CodingSchemeVersion(s);
             String term_browser_version =
                 DataUtils.getMetadataValue(t1, v1, "term_browser_version");
 
             if (term_browser_version == null)
                 term_browser_version = v1;
             for (int j = 0; j < display_name_vec.size(); j++) {
                 OntologyInfo info = display_name_vec.elementAt(j);
                 String nm = info.getDisplayName();
                 String val = display_name_hmap.get(nm);
                 if (val.compareTo(s) == 0) {
                     s = nm;
                     break;
                 }
             }
             int k = s.lastIndexOf('$');
             if (k >= 0)
                 s = s.substring(0, k);
             s = s + " (" + term_browser_version + ")";
             value = value + s + "<br/>";
         }
         return value;
     }
 }
