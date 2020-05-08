 package gov.nih.nci.evs.browser.utils;
 
 import gov.nih.nci.evs.browser.common.*;
 
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
     private static final String DEFAULT_DICTIONARY = Constants.NCI_THESAURUS;
 
     public static boolean isNull(String text) {
         return text == null || text.equalsIgnoreCase("null");
     }
 
     public static class JSPHeaderInfo {
         public String dictionary;
         public String version;
         public String version_deprecated;
         private boolean debugAll = false;
 
         private void debugDV(String msg, String dictionary, String version) {
             _logger.debug(msg + "version=" + version + ", dictionary=" + dictionary);
         }
 
         private void debugAllVersions(HttpServletRequest request) {
             String prefix = "ALL: ";
             _logger.debug(Utils.SEPARATOR_DASHES);
 
             String dictionary = request.getParameter("dictionary");
             String version = request.getParameter("version");
             debugDV(prefix + "Request Parameters: ", dictionary, version);
 
             dictionary = (String) request.getAttribute("dictionary");
             version = (String) request.getAttribute("version");
             debugDV(prefix + "Request Attributes: ", dictionary, version);
 
             dictionary = (String) request.getSession().getAttribute("dictionary");
             version = (String) request.getSession().getAttribute("version");
             debugDV(prefix + "Session Attributes: ", dictionary, version);
         }
 
         public JSPHeaderInfo(HttpServletRequest request) {
             if (debugAll)
                 debugAllVersions(request);
             _logger.debug(Utils.SEPARATOR);
             dictionary = request.getParameter("dictionary");
             version = request.getParameter("version");
             debugDV("Request Parameters: ", dictionary, version);
 
             if (isNull(dictionary) && isNull(version)) {
                 dictionary = (String) request.getAttribute("dictionary");
                 version = (String) request.getAttribute("version");
                 debugDV("Request Attributes: ", dictionary, version);
             }
 
             if (isNull(dictionary) && isNull(version)) {
                 dictionary = (String) request.getSession().getAttribute("dictionary");
                 version = (String) request.getSession().getAttribute("version");
                 debugDV("Session Attributes: ", dictionary, version);
             }
 
             boolean isDictionaryNull = isNull(dictionary);
             boolean isVersionNull = isNull(version);
             if (isDictionaryNull && ! isVersionNull &&
                     DataUtils.isCodingSchemeLoaded(DEFAULT_DICTIONARY, version)) {
                 dictionary = DEFAULT_DICTIONARY;
                 debugDV("Defaulting to: ", dictionary, version);
             } else if (! isDictionaryNull && isVersionNull) {
                 version =
                     DataUtils.getVocabularyVersionByTag(dictionary,
                         "PRODUCTION");
                 debugDV("Defaulting to: ", dictionary, version);
             } else if (! isDictionaryNull && ! isVersionNull &&
                     ! DataUtils.isCodingSchemeLoaded(dictionary, version)) {
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
         Vector<OntologyInfo> display_name_vec =
             (Vector<OntologyInfo>) request.getSession().getAttribute(
                 "display_name_vec");
 
         Vector<String> ontologies_to_search_on =
             DataUtils.parseData(ontologiesToSearchOn);
         String value = "";
         for (int i = 0; i < ontologies_to_search_on.size(); i++) {
             String s = ontologies_to_search_on.elementAt(i);
             String csName = DataUtils.key2CodingSchemeName(s);
             String csVersion = DataUtils.key2CodingSchemeVersion(s);
             String term_browser_version =
                 DataUtils.getMetadataValue(csName, csVersion, "term_browser_version");
             String displayName = "";
 
             if (term_browser_version == null)
                 term_browser_version = csVersion;
             for (int j = 0; j < display_name_vec.size(); j++) {
                 OntologyInfo info = display_name_vec.elementAt(j);
                 String label = info.getLabel();
                 if (label.compareTo(s) == 0) {
                     displayName = info.getDisplayName();
                     break;
                 }
             }
             displayName += " (" + term_browser_version + ")";
             value = value + displayName + "<br/>";
         }
         return value;
     }
 
     public static String getPipeSeparator(Boolean[] display) {
         boolean isDisplayed = display[0].booleanValue();
         if (isDisplayed)
             return "|";
         display[0] = Boolean.TRUE;
         return "";
     }
 
     public static String getNavType(HttpServletRequest request) {

		String navigation_type = (String) request.getSession().getAttribute("navigation_type");
		if (navigation_type != null) {
			request.getSession().removeAttribute("navigation_type");
			return navigation_type;
		}

         JSPUtils.JSPHeaderInfo info = new JSPUtils.JSPHeaderInfo(request);
 
         String vsd_view = (String) request.getParameter("view");
         if (vsd_view != null) return "valuesets";
 
         String vsd_uri = (String) request.getParameter("vsd_uri");
         String dictionary = info.dictionary;
         String version = info.version;
 
         _logger.debug(Utils.SEPARATOR);
         String nav_type = (String) request.getParameter("nav_type");
         _logger.debug("nav_type (Parameter): " + nav_type);
 
         nav_type = DataUtils.getNavigationTabType(
             dictionary, version, vsd_uri, nav_type);
         _logger.debug("nav_type (getNavigationTabType): " + nav_type);
 
         if (nav_type == null) {
             nav_type = (String) request.getSession().getAttribute("nav_type");
             _logger.debug("nav_type (Session): " + nav_type);
         }
         if (nav_type == null) {
             nav_type = "terminologies";
             _logger.debug("nav_type (Default): " + nav_type);
         }
         request.getSession().setAttribute("nav_type", nav_type);
         return nav_type;
     }

     public static int parseInt(String text, int defaultValue) {
         if (isNull(text))
             return defaultValue;
         try {
             int value = Integer.parseInt(text);
             return value;
         } catch (Exception e) {
             _logger.error(e.getMessage());
             return defaultValue;
         }
     }
 }
