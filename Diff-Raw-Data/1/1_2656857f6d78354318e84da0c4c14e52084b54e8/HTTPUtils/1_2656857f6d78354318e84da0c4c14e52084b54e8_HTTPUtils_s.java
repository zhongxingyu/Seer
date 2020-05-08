 package gov.nih.nci.evs.browser.utils;
 
 import java.util.*;
 
 import javax.faces.context.*;
 import javax.servlet.http.*;
 
 import java.io.*;
 import java.net.*;
 import java.util.regex.*;
 
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
 
 /**
  * HTTP Utility methods
  *
  * @author garciawa2
  *
  */
 public class HTTPUtils {
     private static Logger _logger = Logger.getLogger(HTTPUtils.class);
     private static final String REFERER = "referer";
     private static final int MAX_FONT_SIZE = 29;
     private static final int MIN_FONT_SIZE = 22;
     private static final int MAX_STR_LEN = 18;
     
     public  static final int ABS_MAX_STR_LEN = 40;
 
     /**
      * Remove potentially bad XSS syntax
      *
      * @param value
      * @return
      */
 
     public static String cleanXSS(String value) {
 
         if (value == null || value.length() < 1)
             return value;
 
         // Remove XSS attacks
         value = replaceAll(value, "<\\s*script\\s*>.*</\\s*script\\s*>", "");
         value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
         value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
         value = value.replaceAll("'", "&#39;");
         value = value.replaceAll("eval\\((.*)\\)", "");
         value =
             replaceAll(value, "[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']",
                 "\"\"");
         value = value.replaceAll("\"", "&quot;");
         return value;
 
     }
 
     /**
      * Calculate a max font size for the length of the text to be
      * 	displayed.
      * @param value
      * @param width
      * @return
      */
     public static int maxFontSize(String value) {
     	int size;    	
 		if (value == null || value.length() == 0)
 			size = MAX_FONT_SIZE;
 		else if (value.length() >= MAX_STR_LEN)
 			size = MIN_FONT_SIZE;
 		else {
 			// Calculate an intermediate font size
 			size = MIN_FONT_SIZE
 					+ Math.round((MAX_FONT_SIZE / MAX_STR_LEN)
 							/ (MIN_FONT_SIZE / value.length()));
 		}
     	return size;
     }
 
     /**
      * @param string
      * @param regex
      * @param replaceWith
      * @return
      */
     public static String replaceAll(String string, String regex,
         String replaceWith) {
 
         Pattern myPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
         string = myPattern.matcher(string).replaceAll(replaceWith);
         return string;
 
     }
 
     public static void printRequestSessionAttributes() {
         _logger.debug(" ");
         _logger.debug(Utils.SEPARATOR);
         _logger.debug("Request Session Attribute(s):");
 
         try {
             HttpServletRequest request =
                 (HttpServletRequest) FacesContext.getCurrentInstance()
                     .getExternalContext().getRequest();
 
             HttpSession session = request.getSession();
             Enumeration<?> enumeration =
                 SortUtils.sort(session.getAttributeNames());
             int i = 0;
             while (enumeration.hasMoreElements()) {
                 String name = (String) enumeration.nextElement();
                 Object value = session.getAttribute(name);
                 _logger.debug("  " + i + ") " + name + ": " + value);
                 ++i;
             }
         } catch (Exception e) {
             _logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
         }
     }
 
     public static void printRequestAttributes() {
         _logger.debug(" ");
         _logger.debug(Utils.SEPARATOR);
         _logger.debug("Request Attribute(s):");
 
         try {
             HttpServletRequest request =
                 (HttpServletRequest) FacesContext.getCurrentInstance()
                     .getExternalContext().getRequest();
 
             Enumeration<?> enumeration =
                 SortUtils.sort(request.getAttributeNames());
             int i = 0;
             while (enumeration.hasMoreElements()) {
                 String name = (String) enumeration.nextElement();
                 Object value = request.getAttribute(name);
                 _logger.debug("  " + i + ") " + name + ": " + value);
                 ++i;
             }
         } catch (Exception e) {
             _logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
         }
     }
 
     public static void printRequestParameters() {
         _logger.debug(" ");
         _logger.debug(Utils.SEPARATOR);
         _logger.debug("Request Parameter(s):");
 
         try {
             HttpServletRequest request =
                 (HttpServletRequest) FacesContext.getCurrentInstance()
                     .getExternalContext().getRequest();
 
             Enumeration<?> enumeration =
                 SortUtils.sort(request.getParameterNames());
             int i = 0;
             while (enumeration.hasMoreElements()) {
                 String name = (String) enumeration.nextElement();
                 Object value = request.getParameter(name);
                 _logger.debug("  " + i + ") " + name + ": " + value);
                 ++i;
             }
         } catch (Exception e) {
             _logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
         }
     }
 
     public static void printAttributes() {
         printRequestSessionAttributes();
         printRequestAttributes();
         printRequestParameters();
         _logger.debug(" ");
     }
 
     public static String convertJSPString(String t) {
         // Convert problem characters to JavaScript Escaped values
         if (t == null) {
             return "";
         }
 
         if (t.compareTo("") == 0) {
             return "";
         }
 
         String sigleQuoteChar = "'";
         String doubleQuoteChar = "\"";
 
         String dq = "&quot;";
 
         t = t.replaceAll(sigleQuoteChar, "\\" + sigleQuoteChar);
         t = t.replaceAll(doubleQuoteChar, "\\" + dq);
         t = t.replaceAll("\r", "\\r"); // replace CR with \r;
         t = t.replaceAll("\n", "\\n"); // replace LF with \n;
 
         return cleanXSS(t);
     }
 
     /**
      * @param request
      * @return
      */
     public static String getRefererParmEncode(HttpServletRequest request) {
         String iref = request.getHeader(REFERER);
         String referer = "N/A";
         if (iref != null)
             try {
                 referer = URLEncoder.encode(iref, "UTF-8");
             } catch (UnsupportedEncodingException e) {
                 // return N/A if encoding is not supported.
             }
         return cleanXSS(referer);
     }
 
     /**
      * @param request
      * @return
      */
     public static String getRefererParmDecode(HttpServletRequest request) {
         String refurl = "N/A";
         try {
             String iref = request.getParameter(REFERER);
             if (iref != null)
                 refurl =
                     URLDecoder.decode(request.getParameter(REFERER), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // return N/A if encoding is not supported.
         }
         return cleanXSS(refurl);
     }
 
     /**
      * @param request
      */
     public static void clearRefererParm(HttpServletRequest request) {
         request.setAttribute(REFERER, null);
     }
 
     /**
      * @return
      */
     public static HttpServletRequest getRequest() {
         return (HttpServletRequest) FacesContext.getCurrentInstance()
             .getExternalContext().getRequest();
     }
 }
