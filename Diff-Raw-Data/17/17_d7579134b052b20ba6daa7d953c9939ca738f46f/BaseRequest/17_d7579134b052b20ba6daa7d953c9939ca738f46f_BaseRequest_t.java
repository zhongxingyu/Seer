 package gov.nih.nci.evs.browser.webapp;
 
 import java.util.*;
 import javax.servlet.http.*;
 import org.apache.log4j.*;
 import gov.nih.nci.evs.utils.*;
 
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
  * @author EVS Team (David Yee, Kim Ong)
  * @version 1.0
  */
 
 public class BaseRequest {
     private static Logger _logger = Logger.getLogger(BaseRequest.class);
     public static final String[] EMPTY_PARAMETERS = new String[] {};
     protected final String INDENT = "    ";
     protected HttpServletRequest _request = HTTPUtils.getRequest();
     protected String[] _parameters = EMPTY_PARAMETERS;
     protected HashMap<String, String> _parametersHashMap = null;
 
     private static final String VERSION = FormRequest.VERSION;
 
     public static void clearAllAttributes(HttpServletRequest request) {
         clearAttributes(request, FormRequest.get_ALL_PARAMETERS());
         clearAttributes(request, SuggestionRequest.get_MOST_PARAMETERS());
         clearAttributes(request, SuggestionCDISCRequest.get_MOST_PARAMETERS());
     }
 
     public static String getIndexPage(HttpServletRequest request) {
         String version = (String)
             request.getSession().getAttribute(VERSION);
 
         String basePath = FormUtils.getBasePath(request);
         String indexPage = basePath + "/" + getUrlParameter(version);
         clearAllAttributes(request);
         return indexPage;
     }
 
 
 /*
     public static Prop.Version getVersion(HttpServletRequest request, boolean clearAll) {
 
         Prop.Version curr_version = (Prop.Version) request.getSession().getAttribute(VERSION);
 
         String parameterVersion = HTTPUtils.getJspParameter(request, VERSION, false);
 
 
         Prop.Version parameter_version = Prop.Version.valueOfOrDefault(parameterVersion);
 
 
         if (parameter_version != curr_version) {
             curr_version = parameter_version;
             if (clearAll) {
 			    clearAllAttributes(request);
 		    }
         }
 
         request.getSession().setAttribute(VERSION, curr_version);
         return curr_version;
     }
 */
     public static String getVersion(HttpServletRequest request) {
 		String curr_version = null;

         Object curr_version_obj = request.getSession().getAttribute(VERSION);
         if (curr_version_obj == null) {
 			curr_version = "Default";
 		} else {
 			curr_version = (String) curr_version_obj;
 		}
 
		//String parameter_version = null;
		String parameter_version = HTTPUtils.getJspParameter(request, VERSION, false);
		if (parameter_version == null || parameter_version.compareToIgnoreCase("null") == 0) {
			parameter_version = "Default";
		}
		/*
        Object parameter_version_obj = HTTPUtils.cleanXSS((String) request.getParameter(VERSION));
         if (parameter_version_obj == null) {
 			parameter_version = "Default";
 		} else {
 			parameter_version = (String) parameter_version_obj;
 		}
		*/
 
        //if (parameter_version != null && parameter_version.compareToIgnoreCase(curr_version) != 0) {
		if (parameter_version.compareToIgnoreCase(curr_version) != 0) {
             curr_version = parameter_version;
 		    clearAllAttributes(request);
         }
 
         request.getSession().setAttribute(VERSION, curr_version);
         return curr_version;
     }
 
 
     protected void setDefaulSessiontAttribute(
         HttpServletRequest request, String name, Object value) {
         Object v = request.getSession().getAttribute(name);
         if (v == null)
             request.getSession().setAttribute(name, value);
     }
 
 
 
 	private String getSessionAttribute(String key) {
 
 		String value = 	(String) _request.getSession().getAttribute(key);
 		return value;
 	}
 
 
 	public HashMap<String, String> getParametersHashMap(
         HttpServletRequest request, String[] parameters) {
         HashMap<String, String> hashMap = new HashMap<String, String>();
         for (int i = 0; i < parameters.length; ++i) {
             String key = parameters[i];
             String value = HTTPUtils.cleanXSS((String) request.getParameter(key));
             if (value == null || value.compareTo("") == 0) {
 				value = getSessionAttribute(key);
 			}
 
             if (value == null) {
                 value = "[Not Set]";
 			}
             hashMap.put(key, value);
         }
         return hashMap;
     }
 
     protected void setParameters(String[] parameters) {
         _parameters = parameters;
         _parametersHashMap = getParametersHashMap(_request, parameters);
     }
 
     private String debugParameters(String text, String[] parameters,
         HashMap<String, String> parametersHashMap) {
         StringBuffer buffer = new StringBuffer();
         buffer.append(text + "\n");
         for (int i = 0; i < parameters.length; ++i) {
             String parameter = parameters[i];
             buffer.append("  * ");
             buffer.append(parameter + ": ");
             buffer.append(parametersHashMap.get(parameter));
             buffer.append("\n");
         }
         _logger.debug(StringUtils.SEPARATOR);
         _logger.debug(buffer.toString());
         return buffer.toString();
     }
 
     protected String debugParameters(String text) {
         return debugParameters(text, _parameters, _parametersHashMap);
     }
 
     public void clear() {
         clearAttributes();
         setParameters(EMPTY_PARAMETERS);
     }
 
     private void updateAttributes(HttpServletRequest request,
         String[] parameters, HashMap<String, String> parametersHashMap) {
         for (int i = 0; i < parameters.length; ++i) {
             String parameter = parameters[i];
             String value = parametersHashMap.get(parameter);
             //request.setAttribute(parameter, value);
             request.getSession().setAttribute(parameter, value);
         }
         debugParameters("HTTPUtils.updateAttributes:", parameters,
             parametersHashMap);
     }
 
     protected void updateAttributes(String[] parameters) {
         updateAttributes(_request, parameters, _parametersHashMap);
     }
 
     protected void updateAttributes() {
         updateAttributes(_parameters);
     }
 
     private static void clearAttributes(HttpServletRequest request,
         String[] parameters) {
         for (int i = 0; i < parameters.length; ++i) {
             String parameter = parameters[i];
             request.getSession().setAttribute(parameter, null);
         }
     }
 
     protected void clearAttributes(String[] parameters) {
         clearAttributes(_request, parameters);
     }
 
     protected void clearAttributes() {
         clearAttributes(_parameters);
     }
 
     private void updateSessionAttributes(HttpServletRequest request,
         String[] parameters) {
         for (int i = 0; i < parameters.length; ++i) {
             String name = parameters[i];
             String value = HTTPUtils.cleanXSS((String) request.getParameter(name));
             request.getSession().setAttribute(name, value);
         }
     }
 
     protected void updateSessionAttributes(String[] parameters) {
         updateSessionAttributes(_request, parameters);
     }
 
     private void clearSessionAttributes(HttpServletRequest request,
         String[] parameters) {
         for (int i = 0; i < parameters.length; ++i) {
             String name = parameters[i];
             request.getSession().setAttribute(name, null);
         }
     }
     protected void clearSessionAttributes(String[] parameters) {
         clearSessionAttributes(_request, parameters);
     }
 
 
 	public static String getUrlParameter(String version) {
 		if (version == null || version.compareTo("null") == 0 || version.compareToIgnoreCase("Default") == 0)
 			return "";
 		return "?version=" + version.toLowerCase();
 	}
 
 }
