 package gov.nih.nci.evs.browser.servlet;
 
 import org.json.*;
 
 import gov.nih.nci.evs.browser.utils.*;
 import gov.nih.nci.evs.browser.common.*;
 
 import java.io.*;
 import java.util.*;
 import java.net.URI;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.log4j.*;
 
 import gov.nih.nci.evs.browser.properties.*;
 import static gov.nih.nci.evs.browser.common.Constants.*;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.valueSets.ValueSetDefinition;
 
 import org.LexGrid.LexBIG.DataModel.Collections.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.LexBIGService.*;
 import org.LexGrid.LexBIG.Utility.*;
 import org.LexGrid.codingSchemes.*;
 import org.LexGrid.naming.*;
 import org.LexGrid.LexBIG.Impl.Extensions.GenericExtensions.*;
 import org.apache.log4j.*;
 import javax.faces.event.ValueChangeEvent;
 
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
 import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
 import org.LexGrid.valueSets.ValueSetDefinition;
 import org.LexGrid.commonTypes.Source;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.lexgrid.valuesets.dto.ResolvedValueSetDefinition;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import javax.servlet.ServletOutputStream;
 import org.LexGrid.concepts.*;
 import org.lexgrid.valuesets.dto.ResolvedValueSetCodedNodeSet;
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
 import org.LexGrid.concepts.Definition;
 import org.LexGrid.commonTypes.PropertyQualifier;
 import org.LexGrid.commonTypes.Property;
 
 
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
  * @author EVS Team
  * @version 1.0
  *
  * Modification history
  *     Initial implementation kim.ong@ngc.com
  *
  */
 
 public final class AjaxServlet extends HttpServlet {
     private static Logger _logger = Logger.getLogger(AjaxServlet.class);
     /**
      * local constants
      */
     private static final long serialVersionUID = 1L;
     //private static final int  STANDARD_VIEW = 1;
     //private static final int  TERMINOLOGY_VIEW = 2;
 
     /**
      * Validates the Init and Context parameters, configures authentication URL
      *
      * @throws ServletException if the init parameters are invalid or any other
      *         problems occur during initialisation
      */
     public void init() throws ServletException {
 
     }
 
     /**
      * Route the user to the execute method
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         execute(request, response);
     }
 
     /**
      * Route the user to the execute method
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a Servlet exception occurs
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         execute(request, response);
     }
 
     private static void debugJSONString(String msg, String jsonString) {
     	boolean debug = false;  //DYEE_DEBUG (default: false)
     	if (! debug)
     		return;
     	_logger.debug(Utils.SEPARATOR);
 	    if (msg != null && msg.length() > 0)
 	    	_logger.debug(msg);
 	    _logger.debug("jsonString: " + jsonString);
 	    _logger.debug("jsonString length: " + jsonString.length());
 	    Utils.debugJSONString(jsonString);
     }
 
 
 
 
 
 
     public static void search_tree(HttpServletResponse response, String node_id,
         String ontology_display_name, String ontology_version) {
         try {
             String jsonString = search_tree(node_id,
                 ontology_display_name, ontology_version);
             if (jsonString == null)
                 return;
 
             JSONObject json = new JSONObject();
             JSONArray rootsArray = new JSONArray(jsonString);
             json.put("root_nodes", rootsArray);
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             response.getWriter().write(json.toString());
             response.getWriter().flush();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static String search_tree(String node_id,
         String ontology_display_name, String ontology_version) throws Exception {
         if (node_id == null || ontology_display_name == null)
             return null;
 
         Utils.StopWatch stopWatch = new Utils.StopWatch();
 //        String max_tree_level_str =
 //            NCItBrowserProperties.getProperty(
 //                NCItBrowserProperties.MAXIMUM_TREE_LEVEL);
 //        int maxLevel = Integer.parseInt(max_tree_level_str);
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (ontology_version != null) versionOrTag.setVersion(ontology_version);
 
         String jsonString =
             CacheController.getTree(
                 ontology_display_name, versionOrTag, node_id);
         debugJSONString("Section: search_tree", jsonString);
 
         _logger.debug("search_tree: " + stopWatch.getResult());
         return jsonString;
     }
 
     /**
      * Process the specified HTTP request, and create the corresponding HTTP
      * response (or forward to another web component that will create it).
      *
      * @param request The HTTP request we are processing
      * @param response The HTTP response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void execute(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         // Determine request by attributes
         String action = request.getParameter("action");// DataConstants.ACTION);
         String node_id = request.getParameter("ontology_node_id");// DataConstants.ONTOLOGY_NODE_ID);
         String ontology_display_name =
             request.getParameter("ontology_display_name");// DataConstants.ONTOLOGY_DISPLAY_NAME);
 
         String ontology_version = request.getParameter("version");
         if (ontology_version == null) {
 			ontology_version = DataUtils.getVocabularyVersionByTag(ontology_display_name, "PRODUCTION");
 		}
 
         long ms = System.currentTimeMillis();
 
         if (action.equals("expand_tree")) {
             if (node_id != null && ontology_display_name != null) {
 
 				System.out.println("(*) EXPAND TREE NODE: " + node_id);
 
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
                 try {
 
 /*
                     // for HL7 (temporary fix)
                     ontology_display_name =
                         DataUtils.searchFormalName(ontology_display_name);
 
 */
                     nodesArray =
                         CacheController.getInstance().getSubconcepts(
                             ontology_display_name, ontology_version, node_id);
                     if (nodesArray != null) {
                         json.put("nodes", nodesArray);
                     }
 
 
 
                 } catch (Exception e) {
                 }
 
                 debugJSONString("Section: expand_tree", json.toString());
                 response.getWriter().write(json.toString());
                 /*
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
                     */
             }
         }
 
         /*
          * else if (action.equals("search_tree")) {
          *
          *
          * if (node_id != null && ontology_display_name != null) {
          * response.setContentType("text/html");
          * response.setHeader("Cache-Control", "no-cache"); JSONObject json =
          * new JSONObject(); try { // testing // JSONArray rootsArray = //
          * CacheController.getInstance().getPathsToRoots(ontology_display_name,
          * // null, node_id, true);
          *
          * String max_tree_level_str = null; int maxLevel = -1; try {
          * max_tree_level_str = NCItBrowserProperties .getInstance()
          * .getProperty( NCItBrowserProperties.MAXIMUM_TREE_LEVEL); maxLevel =
          * Integer.parseInt(max_tree_level_str);
          *
          * } catch (Exception ex) {
          *
          * }
          *
          * JSONArray rootsArray = CacheController.getInstance()
          * .getPathsToRoots(ontology_display_name, null, node_id, true,
          * maxLevel);
          *
          * if (rootsArray.length() == 0) { rootsArray =
          * CacheController.getInstance() .getRootConcepts(ontology_display_name,
          * null);
          *
          * boolean is_root = isRoot(rootsArray, node_id); if (!is_root) {
          * //rootsArray = null; json.put("dummy_root_nodes", rootsArray);
          * response.getWriter().write(json.toString());
          * response.getWriter().flush();
          *
          * _logger.debug("Run time (milliseconds): " +
          * (System.currentTimeMillis() - ms)); return; } }
          * json.put("root_nodes", rootsArray); } catch (Exception e) {
          * e.printStackTrace(); }
          *
          * response.getWriter().write(json.toString());
          * response.getWriter().flush();
          *
          * _logger.debug("Run time (milliseconds): " +
          * (System.currentTimeMillis() - ms)); return; } }
          */
 
         if (action.equals("search_value_set")) {
             search_value_set(request, response);
         } else if (action.equals("create_src_vs_tree")) {
             create_src_vs_tree(request, response);
         } else if (action.equals("create_cs_vs_tree")) {
             create_cs_vs_tree(request, response);
         } else if (action.equals("search_hierarchy")) {
             search_hierarchy(request, response, node_id, ontology_display_name, ontology_version);
         } else if (action.equals("search_tree")) {
             search_tree(response, node_id, ontology_display_name, ontology_version);
         } else if (action.equals("build_tree")) {
             if (ontology_display_name == null)
                 ontology_display_name = CODING_SCHEME_NAME;
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
                 nodesArray =
                     CacheController.getInstance().getRootConcepts(
                         ontology_display_name, ontology_version);
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             debugJSONString("Section: build_tree", json.toString());
             response.getWriter().write(json.toString());
             // response.getWriter().flush();
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
 
         } else if (action.equals("build_vs_tree")) {
 
             if (ontology_display_name == null)
                 ontology_display_name = CODING_SCHEME_NAME;
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
 				//HashMap getRootValueSets(String codingSchemeURN)
 				String codingSchemeVersion = null;
                 nodesArray =
                     CacheController.getInstance().getRootValueSets(
                         ontology_display_name, codingSchemeVersion);
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             response.getWriter().write(json.toString());
             //System.out.println(json.toString());
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
 
         } else if (action.equals("expand_vs_tree")) {
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 
                 try {
                     nodesArray =
                         CacheController.getInstance().getSubValueSets(
                             ontology_display_name, ontology_version, node_id);
                     if (nodesArray != null) {
 						System.out.println("expand_vs_tree nodesArray != null");
                         json.put("nodes", nodesArray);
                     } else {
 						System.out.println("expand_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
 
 
         } else if (action.equals("expand_entire_vs_tree")) {
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 
                 try {
                     nodesArray =
                         CacheController.getInstance().getSourceValueSetTree(
                             ontology_display_name, ontology_version, true);
                     if (nodesArray != null) {
 						System.out.println("expand_entire_vs_tree nodesArray != null");
                         json.put("root_nodes", nodesArray);
                     } else {
 						System.out.println("expand_entire_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
 
         } else if (action.equals("expand_entire_cs_vs_tree")) {
             //if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 
                 try {
                     nodesArray =
                         CacheController.getInstance().getCodingSchemeValueSetTree(
                             ontology_display_name, ontology_version, true);
                     if (nodesArray != null) {
 						System.out.println("expand_entire_vs_tree nodesArray != null");
                         json.put("root_nodes", nodesArray);
                     } else {
 						System.out.println("expand_entire_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             //}
 
 
         } else if (action.equals("build_cs_vs_tree")) {
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
 				//HashMap getRootValueSets(String codingSchemeURN)
 				String codingSchemeVersion = null;
                 nodesArray =
                     CacheController.getInstance().getRootValueSets(true);
 
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             response.getWriter().write(json.toString());
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
         } else if (action.equals("expand_cs_vs_tree")) {
 
 			response.setContentType("text/html");
 			response.setHeader("Cache-Control", "no-cache");
 			JSONObject json = new JSONObject();
 			JSONArray nodesArray = null;
 
 			String vsd_uri = ValueSetHierarchy.getValueSetURI(node_id);
 			node_id = ValueSetHierarchy.getCodingSchemeName(node_id);
 
             //if (node_id != null && ontology_display_name != null) {
 			if (node_id != null) {
 				ValueSetDefinition vsd = ValueSetHierarchy.findValueSetDefinitionByURI(vsd_uri);
 				if (vsd == null) {
 					System.out.println("(****) coding scheme name: " + node_id);
 
 				   try {
 					   //
 					    nodesArray = CacheController.getInstance().getRootValueSets(node_id, null);
 						//nodesArray = CacheController.getInstance().getRootValueSets(node_id, null); //find roots (by source)
 
 						if (nodesArray != null) {
 							json.put("nodes", nodesArray);
 						} else {
 							System.out.println("expand_vs_tree nodesArray == null???");
 						}
 
 					} catch (Exception e) {
 					}
 			    } else {
 					try {
 						nodesArray =
 							CacheController.getInstance().getSubValueSets(
 								node_id, null, vsd_uri);
 
 						if (nodesArray != null) {
 							json.put("nodes", nodesArray);
 						}
 
 					} catch (Exception e) {
 					}
 				}
 
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
 
 
         } else if (action.equals("build_src_vs_tree")) {
 
 
             response.setContentType("text/html");
             response.setHeader("Cache-Control", "no-cache");
             JSONObject json = new JSONObject();
             JSONArray nodesArray = null;// new JSONArray();
             try {
 				//HashMap getRootValueSets(String codingSchemeURN)
 				String codingSchemeVersion = null;
                 nodesArray =
                     //CacheController.getInstance().getRootValueSets(true, true);
                     CacheController.getInstance().build_src_vs_tree();
 
                 if (nodesArray != null) {
                     json.put("root_nodes", nodesArray);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             response.getWriter().write(json.toString());
             //System.out.println(json.toString());
 
             _logger.debug("Run time (milliseconds): "
                 + (System.currentTimeMillis() - ms));
             return;
 
         } else if (action.equals("expand_src_vs_tree")) {
 
             if (node_id != null && ontology_display_name != null) {
                 response.setContentType("text/html");
                 response.setHeader("Cache-Control", "no-cache");
                 JSONObject json = new JSONObject();
                 JSONArray nodesArray = null;
 				nodesArray = CacheController.getInstance().expand_src_vs_tree(node_id);
 
 				if (nodesArray == null) {
                     System.out.println("(*) CacheController returns nodesArray == null");
 				}
 
 
                 try {
                     if (nodesArray != null) {
 						System.out.println("expand_src_vs_tree nodesArray != null");
                         json.put("nodes", nodesArray);
                     } else {
 						System.out.println("expand_src_vs_tree nodesArray == null???");
 					}
 
                 } catch (Exception e) {
 					e.printStackTrace();
 
                 }
                 response.getWriter().write(json.toString());
                 _logger.debug("Run time (milliseconds): "
                     + (System.currentTimeMillis() - ms));
             }
         }
 
 
     }
 
     private boolean isRoot(JSONArray rootsArray, String code) {
         for (int i = 0; i < rootsArray.length(); i++) {
             String node_id = null;
             try {
                 JSONObject node = rootsArray.getJSONObject(i);
                 node_id = (String) node.get(CacheController.ONTOLOGY_NODE_ID);
                 if (node_id.compareTo(code) == 0)
                     return true;
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return false;
     }
 
     private static boolean _debug = false; // DYEE_DEBUG (default: false)
     private static StringBuffer _debugBuffer = null;
 
     public static void println(PrintWriter out, String text) {
         if (_debug) {
             _logger.debug("DBG: " + text);
             _debugBuffer.append(text + "\n");
         }
         out.println(text);
     }
 
 
     public static void search_hierarchy(HttpServletRequest request, HttpServletResponse response, String node_id,
         String ontology_display_name, String ontology_version) {
 
       Enumeration parameters = request.getParameterNames();
       String param = null;
       while (parameters.hasMoreElements())
       {
          param = (String) parameters.nextElement();
          String paramValue = request.getParameter(param);
       }
       response.setContentType("text/html");
       PrintWriter out = null;
 
       try {
       	  out = response.getWriter();
       } catch (Exception ex) {
 		  ex.printStackTrace();
 		  return;
 	  }
 
       if (_debug) {
           _debugBuffer = new StringBuffer();
       }
 
       String localName = DataUtils.getLocalName(ontology_display_name);
       String formalName = DataUtils.getFormalName(localName);
       String term_browser_version = DataUtils.getMetadataValue(formalName, ontology_version, "term_browser_version");
       String display_name = DataUtils.getMetadataValue(formalName, ontology_version, "display_name");
 
       println(out, "");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/yahoo-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/event-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/dom-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/animation-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/container-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/connection-min.js\" ></script>");
       //println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/autocomplete-min.js\" ></script>");
       println(out, "<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/treeview-min.js\" ></script>");
 
 
 
       println(out, "");
       println(out, "");
       println(out, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
       println(out, "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
       println(out, "  <head>");
       println(out, "  <title>Vocabulary Hierarchy</title>");
       println(out, "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/styleSheet.css\" />");
       println(out, "  <link rel=\"shortcut icon\" href=\"/ncitbrowser/favicon.ico\" type=\"image/x-icon\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/fonts.css\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/grids.css\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/code.css\" />");
       println(out, "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/yui/tree.css\" />");
 
 
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/script.js\"></script>");
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/search.js\"></script>");
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/dropdown.js\"></script>");
 
 
       println(out, "");
       println(out, "  <script language=\"JavaScript\">");
       println(out, "");
       println(out, "    var tree;");
       println(out, "    var nodeIndex;");
       println(out, "    var rootDescDiv;");
       println(out, "    var emptyRootDiv;");
       println(out, "    var treeStatusDiv;");
       println(out, "    var nodes = [];");
       println(out, "    var currOpener;");
       println(out, "");
       println(out, "    function load(url,target) {");
       println(out, "      if (target != '')");
       println(out, "        target.window.location.href = url;");
       println(out, "      else");
       println(out, "        window.location.href = url;");
       println(out, "    }");
       println(out, "");
       println(out, "    function init() {");
       println(out, "");
       println(out, "      rootDescDiv = new YAHOO.widget.Module(\"rootDesc\", {visible:false} );");
       println(out, "      resetRootDesc();");
       println(out, "");
       println(out, "      emptyRootDiv = new YAHOO.widget.Module(\"emptyRoot\", {visible:true} );");
       println(out, "      resetEmptyRoot();");
       println(out, "");
       println(out, "      treeStatusDiv = new YAHOO.widget.Module(\"treeStatus\", {visible:true} );");
       println(out, "      resetTreeStatus();");
       println(out, "");
       println(out, "      currOpener = opener;");
       println(out, "      initTree();");
       println(out, "    }");
       println(out, "");
       println(out, "    function addTreeNode(rootNode, nodeInfo) {");
       println(out, "      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       println(out, "      var newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       println(out, "      var newNode = new YAHOO.widget.TextNode(newNodeData, rootNode, false);");
       println(out, "      if (nodeInfo.ontology_node_child_count > 0) {");
       println(out, "        newNode.setDynamicLoad(loadNodeData);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function buildTree(ontology_node_id, ontology_display_name) {");
       println(out, "      var handleBuildTreeSuccess = function(o) {");
       println(out, "        var respTxt = o.responseText;");
       println(out, "        var respObj = eval('(' + respTxt + ')');");
       println(out, "        if ( typeof(respObj) != \"undefined\") {");
       println(out, "          if ( typeof(respObj.root_nodes) != \"undefined\") {");
       println(out, "            var root = tree.getRoot();");
       println(out, "            if (respObj.root_nodes.length == 0) {");
       println(out, "              showEmptyRoot();");
       println(out, "            }");
       println(out, "            else {");
       println(out, "              for (var i=0; i < respObj.root_nodes.length; i++) {");
       println(out, "                var nodeInfo = respObj.root_nodes[i];");
       println(out, "                var expand = false;");
       println(out, "                addTreeNode(root, nodeInfo, expand);");
       println(out, "              }");
       println(out, "            }");
       println(out, "");
       println(out, "            tree.draw();");
       println(out, "          }");
       println(out, "        }");
       println(out, "        resetTreeStatus();");
       println(out, "      }");
       println(out, "");
       println(out, "      var handleBuildTreeFailure = function(o) {");
       println(out, "        resetTreeStatus();");
       println(out, "        resetEmptyRoot();");
       println(out, "        alert('responseFailure: ' + o.statusText);");
       println(out, "      }");
       println(out, "");
       println(out, "      var buildTreeCallback =");
       println(out, "      {");
       println(out, "        success:handleBuildTreeSuccess,");
       println(out, "        failure:handleBuildTreeFailure");
       println(out, "      };");
       println(out, "");
       println(out, "      if (ontology_display_name!='') {");
       println(out, "        resetEmptyRoot();");
       println(out, "");
       println(out, "        showTreeLoadingStatus();");
       println(out, "        var ontology_source = null;");
       println(out, "        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=build_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetTree(ontology_node_id, ontology_display_name) {");
       println(out, "");
       println(out, "      var handleResetTreeSuccess = function(o) {");
       println(out, "        var respTxt = o.responseText;");
       println(out, "        var respObj = eval('(' + respTxt + ')');");
       println(out, "        if ( typeof(respObj) != \"undefined\") {");
       println(out, "          if ( typeof(respObj.root_node) != \"undefined\") {");
       println(out, "            var root = tree.getRoot();");
       println(out, "            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.root_node.ontology_node_id + \"');\";");
       println(out, "            var rootNodeData = { label:respObj.root_node.ontology_node_name, id:respObj.root_node.ontology_node_id, href:nodeDetails };");
       println(out, "            var expand = false;");
       println(out, "            if (respObj.root_node.ontology_node_child_count > 0) {");
       println(out, "              expand = true;");
       println(out, "            }");
       println(out, "            var ontRoot = new YAHOO.widget.TextNode(rootNodeData, root, expand);");
       println(out, "");
       println(out, "            if ( typeof(respObj.child_nodes) != \"undefined\") {");
       println(out, "              for (var i=0; i < respObj.child_nodes.length; i++) {");
       println(out, "                var nodeInfo = respObj.child_nodes[i];");
       println(out, "                addTreeNode(ontRoot, nodeInfo);");
       println(out, "              }");
       println(out, "            }");
       println(out, "            tree.draw();");
       println(out, "            setRootDesc(respObj.root_node.ontology_node_name, ontology_display_name);");
       println(out, "          }");
       println(out, "        }");
       println(out, "        resetTreeStatus();");
       println(out, "      }");
       println(out, "");
       println(out, "      var handleResetTreeFailure = function(o) {");
       println(out, "        resetTreeStatus();");
       println(out, "        alert('responseFailure: ' + o.statusText);");
       println(out, "      }");
       println(out, "");
       println(out, "      var resetTreeCallback =");
       println(out, "      {");
       println(out, "        success:handleResetTreeSuccess,");
       println(out, "        failure:handleResetTreeFailure");
       println(out, "      };");
       println(out, "      if (ontology_node_id!= '') {");
       println(out, "        showTreeLoadingStatus();");
       println(out, "        var ontology_source = null;");
       println(out, "        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=reset_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name + '&version='+ ontology_version +'&ontology_source='+ontology_source,resetTreeCallback);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function onClickTreeNode(ontology_node_id) {");
       out.println("       if (ontology_node_id.indexOf(\"_dot_\") != -1) return;");
       println(out, "      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       println(out, "      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       println(out, "      load('/ncitbrowser/ConceptReport.jsp?dictionary='+ ontology_display_name + '&version='+ ontology_version  + '&code=' + ontology_node_id, currOpener);");
       println(out, "    }");
       println(out, "");
       println(out, "    function onClickViewEntireOntology(ontology_display_name) {");
       println(out, "      var ontology_display_name = document.pg_form.ontology_display_name.value;");
       println(out, "      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       println(out, "      tree.draw();");
       println(out, "      resetRootDesc();");
       println(out, "      buildTree('', ontology_display_name);");
       println(out, "    }");
       println(out, "");
       println(out, "    function initTree() {");
       println(out, "");
       println(out, "      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       println(out, "      var ontology_node_id = document.forms[\"pg_form\"].ontology_node_id.value;");
       println(out, "      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       println(out, "");
       println(out, "      if (ontology_node_id == null || ontology_node_id == \"null\")");
       println(out, "      {");
       println(out, "          buildTree(ontology_node_id, ontology_display_name);");
       println(out, "      }");
       println(out, "      else");
       println(out, "      {");
       println(out, "          searchTree(ontology_node_id, ontology_display_name);");
       println(out, "      }");
       println(out, "    }");
       println(out, "");
       println(out, "    function initRootDesc() {");
       println(out, "      rootDescDiv.setBody('');");
       println(out, "      initRootDesc.show();");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetRootDesc() {");
       println(out, "      rootDescDiv.hide();");
       println(out, "      rootDescDiv.setBody('');");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetEmptyRoot() {");
       println(out, "      emptyRootDiv.hide();");
       println(out, "      emptyRootDiv.setBody('');");
       println(out, "      emptyRootDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function resetTreeStatus() {");
       println(out, "      treeStatusDiv.hide();");
       println(out, "      treeStatusDiv.setBody('');");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showEmptyRoot() {");
       println(out, "      emptyRootDiv.setBody(\"<span class='instruction_text'>No root nodes available.</span>\");");
       println(out, "      emptyRootDiv.show();");
       println(out, "      emptyRootDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showNodeNotFound(node_id) {");
       println(out, "      //emptyRootDiv.setBody(\"<span class='instruction_text'>Concept with code \" + node_id + \" not found in the hierarchy.</span>\");");
       println(out, "      emptyRootDiv.setBody(\"<span class='instruction_text'>Concept not part of the parent-child hierarchy in this source; check other relationships.</span>\");");
       println(out, "      emptyRootDiv.show();");
       println(out, "      emptyRootDiv.render();");
       println(out, "    }");
       println(out, "    ");
       println(out, "    function showPartialHierarchy() {");
       println(out, "      rootDescDiv.setBody(\"<span class='instruction_text'>(Note: This tree only shows partial hierarchy.)</span>\");");
       println(out, "      rootDescDiv.show();");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showTreeLoadingStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Building tree ...</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showTreeDrawingStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Drawing tree ...</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showSearchingTreeStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Searching tree... Please wait.</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "    function showConstructingTreeStatus() {");
       println(out, "      treeStatusDiv.setBody(\"<img src='/ncitbrowser/images/loading.gif'/> <span class='instruction_text'>Constructing tree... Please wait.</span>\");");
       println(out, "      treeStatusDiv.show();");
       println(out, "      treeStatusDiv.render();");
       println(out, "    }");
       println(out, "");
 
 /*
       println(out, "    function loadNodeData(node, fnLoadComplete) {");
       println(out, "      var id = node.data.id;");
       println(out, "");
       println(out, "      var responseSuccess = function(o)");
       println(out, "      {");
       println(out, "        var path;");
       println(out, "        var dirs;");
       println(out, "        var files;");
       println(out, "        var respTxt = o.responseText;");
       println(out, "        var respObj = eval('(' + respTxt + ')');");
       println(out, "        var fileNum = 0;");
       println(out, "        var categoryNum = 0;");
       println(out, "        if ( typeof(respObj.nodes) != \"undefined\") {");
       println(out, "          for (var i=0; i < respObj.nodes.length; i++) {");
       println(out, "            var name = respObj.nodes[i].ontology_node_name;");
       println(out, "            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       println(out, "            var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       println(out, "            var newNode = new YAHOO.widget.TextNode(newNodeData, node, false);");
       println(out, "            if (respObj.nodes[i].ontology_node_child_count > 0) {");
       println(out, "              newNode.setDynamicLoad(loadNodeData);");
       println(out, "            }");
       println(out, "          }");
       println(out, "        }");
       println(out, "        tree.draw();");
       println(out, "        fnLoadComplete();");
       println(out, "      }");
 */
 
 
       out.println("    function loadNodeData(node, fnLoadComplete) {");
       out.println("      var id = node.data.id;");
       out.println("");
       out.println("      var responseSuccess = function(o)");
       out.println("      {");
       out.println("        var path;");
       out.println("        var dirs;");
       out.println("        var files;");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        var fileNum = 0;");
       out.println("        var categoryNum = 0;");
       out.println("        var pos = id.indexOf(\"_dot_\");");
       out.println("        if ( typeof(respObj.nodes) != \"undefined\") {");
       out.println("	    if (pos == -1) {");
       out.println("	      for (var i=0; i < respObj.nodes.length; i++) {");
       out.println("		var name = respObj.nodes[i].ontology_node_name;");
       out.println("		var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       out.println("		var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       out.println("		var newNode = new YAHOO.widget.TextNode(newNodeData, node, false);");
       out.println("		if (respObj.nodes[i].ontology_node_child_count > 0) {");
       out.println("		    newNode.setDynamicLoad(loadNodeData);");
       out.println("		}");
       out.println("	      }");
       out.println("");
       out.println("	    } else {");
       out.println("");
       out.println("		var parent = node.parent;");
       out.println("		for (var i=0; i < respObj.nodes.length; i++) {");
       out.println("		  var name = respObj.nodes[i].ontology_node_name;");
       out.println("		  var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       out.println("		  var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       out.println("");
       out.println("		  var newNode = new YAHOO.widget.TextNode(newNodeData, parent, true);");
       out.println("		  if (respObj.nodes[i].ontology_node_child_count > 0) {");
       out.println("		     newNode.setDynamicLoad(loadNodeData);");
       out.println("		  }");
       out.println("		}");
       out.println("		tree.removeNode(node,true);");
       out.println("	    }");
       out.println("        }");
       out.println("        fnLoadComplete();");
       out.println("      }");
 
 
 
       println(out, "");
       println(out, "      var responseFailure = function(o){");
       println(out, "        alert('responseFailure: ' + o.statusText);");
       println(out, "      }");
       println(out, "");
       println(out, "      var callback =");
       println(out, "      {");
       println(out, "        success:responseSuccess,");
       println(out, "        failure:responseFailure");
       println(out, "      };");
       println(out, "");
 
       println(out, "      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       println(out, "      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
 
       //println(out, "      var ontology_display_name = " + "\"" + ontology_display_name + "\";");
       //println(out, "      var ontology_version = " + "\"" + ontology_version + "\";");
 
       println(out, "      var cObj = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_tree&ontology_node_id=' +id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version,callback);");
 
       println(out, "    }");
       println(out, "");
       println(out, "    function setRootDesc(rootNodeName, ontology_display_name) {");
       println(out, "      var newDesc = \"<span class='instruction_text'>Root set to <b>\" + rootNodeName + \"</b></span>\";");
       println(out, "      rootDescDiv.setBody(newDesc);");
       println(out, "      var footer = \"<a onClick='javascript:onClickViewEntireOntology();' href='#' class='link_text'>view full ontology}</a>\";");
       println(out, "      rootDescDiv.setFooter(footer);");
       println(out, "      rootDescDiv.show();");
       println(out, "      rootDescDiv.render();");
       println(out, "    }");
       println(out, "");
       println(out, "");
       println(out, "    function searchTree(ontology_node_id, ontology_display_name) {");
       println(out, "");
 
       println(out, "      var root = tree.getRoot();");
 
       //new ViewInHierarchyUtil().printTree(out, ontology_display_name, ontology_version, node_id);
       new ViewInHierarchyUtils().printTree(out, ontology_display_name, ontology_version, node_id);
       println(out, "             showPartialHierarchy();");
       println(out, "             tree.draw();");
 
       println(out, "    }");
       println(out, "");
       println(out, "");
       println(out, "    function addTreeBranch(ontology_node_id, rootNode, nodeInfo) {");
       println(out, "      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       println(out, "      var newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       println(out, "");
       println(out, "      var expand = false;");
       println(out, "      var childNodes = nodeInfo.children_nodes;");
       println(out, "");
       println(out, "      if (childNodes.length > 0) {");
       println(out, "          expand = true;");
       println(out, "      }");
       println(out, "      var newNode = new YAHOO.widget.TextNode(newNodeData, rootNode, expand);");
       println(out, "      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       println(out, "          newNode.labelStyle = \"ygtvlabel_highlight\";");
       println(out, "      }");
       println(out, "");
       println(out, "      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       println(out, "         newNode.isLeaf = true;");
       println(out, "         if (nodeInfo.ontology_node_child_count > 0) {");
       println(out, "             newNode.isLeaf = false;");
       println(out, "             newNode.setDynamicLoad(loadNodeData);");
       println(out, "         } else {");
       println(out, "             tree.draw();");
       println(out, "         }");
       println(out, "");
       println(out, "      } else {");
       println(out, "          if (nodeInfo.ontology_node_id != ontology_node_id) {");
       println(out, "          if (nodeInfo.ontology_node_child_count == 0 && nodeInfo.ontology_node_id != ontology_node_id) {");
       println(out, "        newNode.isLeaf = true;");
       println(out, "          } else if (childNodes.length == 0) {");
       println(out, "        newNode.setDynamicLoad(loadNodeData);");
       println(out, "          }");
       println(out, "        }");
       println(out, "      }");
       println(out, "");
       println(out, "      tree.draw();");
       println(out, "      for (var i=0; i < childNodes.length; i++) {");
       println(out, "         var childnodeInfo = childNodes[i];");
       println(out, "         addTreeBranch(ontology_node_id, newNode, childnodeInfo);");
       println(out, "      }");
       println(out, "    }");
       println(out, "    YAHOO.util.Event.addListener(window, \"load\", init);");
       println(out, "");
       println(out, "  </script>");
       println(out, "</head>");
       println(out, "<body>");
       println(out, "  ");
       println(out, "    <!-- Begin Skip Top Navigation -->");
       println(out, "      <a href=\"#evs-content\" class=\"hideLink\" accesskey=\"1\" title=\"Skip repetitive navigation links\">skip navigation links</A>");
       println(out, "    <!-- End Skip Top Navigation --> ");
       println(out, "    <div id=\"popupContainer\">");
       println(out, "      <!-- nci popup banner -->");
       println(out, "      <div class=\"ncipopupbanner\">");
       println(out, "        <a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\"><img src=\"/ncitbrowser/images/nci-banner-1.gif\" width=\"440\" height=\"39\" border=\"0\" alt=\"National Cancer Institute\" /></a>");
       println(out, "        <a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\"><img src=\"/ncitbrowser/images/spacer.gif\" width=\"48\" height=\"39\" border=\"0\" alt=\"National Cancer Institute\" class=\"print-header\" /></a>");
       println(out, "      </div>");
       println(out, "      <!-- end nci popup banner -->");
       println(out, "      <div id=\"popupMainArea\">");
       println(out, "        <a name=\"evs-content\" id=\"evs-content\"></a>");
       println(out, "        <table class=\"evsLogoBg\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
       println(out, "        <tr>");
       println(out, "          <td valign=\"top\">");
       println(out, "            <a href=\"http://evs.nci.nih.gov/\" target=\"_blank\" alt=\"Enterprise Vocabulary Services\">");
       println(out, "              <img src=\"/ncitbrowser/images/evs-popup-logo.gif\" width=\"213\" height=\"26\" alt=\"EVS: Enterprise Vocabulary Services\" title=\"EVS: Enterprise Vocabulary Services\" border=\"0\" />");
       println(out, "            </a>");
       println(out, "          </td>");
       println(out, "          <td valign=\"top\"><div id=\"closeWindow\"><a href=\"javascript:window.close();\"><img src=\"/ncitbrowser/images/thesaurus_close_icon.gif\" width=\"10\" height=\"10\" border=\"0\" alt=\"Close Window\" />&nbsp;CLOSE WINDOW</a></div></td>");
       println(out, "        </tr>");
       println(out, "        </table>");
       println(out, "");
       println(out, "");
 
       String release_date = DataUtils.getVersionReleaseDate(ontology_display_name, ontology_version);
       if (ontology_display_name.compareTo("NCI Thesaurus") == 0 || ontology_display_name.compareTo("NCI_Thesaurus") == 0) {
 
       println(out, "    <div>");
       println(out, "      <img src=\"/ncitbrowser/images/thesaurus_popup_banner.gif\" width=\"612\" height=\"56\" alt=\"NCI Thesaurus\" title=\"\" border=\"0\" />");
       println(out, "      ");
       println(out, "	 ");
       println(out, "             <span class=\"texttitle-blue-rightjust-2\">" + ontology_version + " (Release date: " + release_date + ")</span>");
       println(out, "      ");
       println(out, "");
       println(out, "    </div>");
 
       } else {
 
       println(out, "    <div>");
       println(out, "      <img src=\"/ncitbrowser/images/other_popup_banner.gif\" width=\"612\" height=\"56\" alt=\"" + display_name + "\" title=\"\" border=\"0\" />");
       println(out, "      <div class=\"vocabularynamepopupshort\">" + display_name );
       println(out, "      ");
       println(out, "	 ");
       println(out, "             <span class=\"texttitle-blue-rightjust\">" + ontology_version + " (Release date: " + release_date + ")</span>");
       println(out, "         ");
       println(out, " ");
       println(out, "      </div>");
       println(out, "    </div>");
 
       }
 
 
       println(out, "");
       println(out, "        <div id=\"popupContentArea\">");
       println(out, "          <table width=\"580px\" cellpadding=\"3\" cellspacing=\"0\" border=\"0\">");
       println(out, "            <tr class=\"textbody\">");
       println(out, "              <td class=\"pageTitle\" align=\"left\">");
       println(out, "                " + display_name + " Hierarchy");
       println(out, "              </td>");
       println(out, "              <td class=\"pageTitle\" align=\"right\">");
       println(out, "                <font size=\"1\" color=\"red\" align=\"right\">");
       println(out, "                  <a href=\"javascript:printPage()\"><img src=\"/ncitbrowser/images/printer.bmp\" border=\"0\" alt=\"Send to Printer\"><i>Send to Printer</i></a>");
       println(out, "                </font>");
       println(out, "              </td>");
       println(out, "            </tr>");
       println(out, "          </table>");
 
       if (! ServerMonitorThread.getInstance().isLexEVSRunning()) {
           println(out, "            <div class=\"textbodyredsmall\">" + ServerMonitorThread.getInstance().getMessage() + "</div>");
       } else {
           println(out, "            <!-- Tree content -->");
           println(out, "            <div id=\"rootDesc\">");
           println(out, "              <div id=\"bd\"></div>");
           println(out, "              <div id=\"ft\"></div>");
           println(out, "            </div>");
           println(out, "            <div id=\"treeStatus\">");
           println(out, "              <div id=\"bd\"></div>");
           println(out, "            </div>");
           println(out, "            <div id=\"emptyRoot\">");
           println(out, "              <div id=\"bd\"></div>");
           println(out, "            </div>");
           println(out, "            <div id=\"treecontainer\"></div>");
       }
 
       println(out, "");
       println(out, "          <form id=\"pg_form\">");
       println(out, "            ");
 
 
 	  String ontology_node_id_value = HTTPUtils.cleanXSS(node_id);
 	  String ontology_display_name_value = HTTPUtils.cleanXSS(ontology_display_name);
 	  String ontology_version_value = HTTPUtils.cleanXSS(ontology_version);
 
       println(out, "            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"" + ontology_node_id_value + "\" />");
       println(out, "            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"" + ontology_display_name_value + "\" />");
       //println(out, "            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"" + scheme_value + "\" />");
       println(out, "            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"" + ontology_version_value + "\" />");
 
       println(out, "");
       println(out, "          </form>");
       println(out, "          <!-- End of Tree control content -->");
       println(out, "        </div>");
       println(out, "      </div>");
       println(out, "    </div>");
       println(out, "  ");
       println(out, "</body>");
       println(out, "</html>");
 
       if (_debug) {
           _logger.debug(Utils.SEPARATOR);
           _logger.debug("VIH HTML:\n" + _debugBuffer);
           _debugBuffer = null;
           _logger.debug(Utils.SEPARATOR);
       }
     }
 
 
     public static void create_src_vs_tree(HttpServletRequest request, HttpServletResponse response) {
 		create_vs_tree(request, response, Constants.STANDARD_VIEW);
 	}
 
     public static void create_cs_vs_tree(HttpServletRequest request, HttpServletResponse response) {
 		String dictionary = (String) request.getParameter("dictionary");
 		if (!DataUtils.isNull(dictionary)) {
 			String version = (String) request.getParameter("version");
 			create_vs_tree(request, response, Constants.TERMINOLOGY_VIEW, dictionary, version);
 		} else {
 		    create_vs_tree(request, response, Constants.TERMINOLOGY_VIEW);
 		}
 	}
 
     public static void create_vs_tree(HttpServletRequest request, HttpServletResponse response, int view) {
 
 	  request.getSession().removeAttribute("b");
 	  request.getSession().removeAttribute("m");
 
 
       response.setContentType("text/html");
       PrintWriter out = null;
 
 
 		String checked_vocabularies = (String) request.getParameter("checked_vocabularies");
 		System.out.println("checked_vocabularies: " + checked_vocabularies);
 
 		String partial_checked_vocabularies = (String) request.getParameter("partial_checked_vocabularies");
 		System.out.println("partial_checked_vocabularies: " + partial_checked_vocabularies);
 
 
       try {
       	  out = response.getWriter();
       } catch (Exception ex) {
 		  ex.printStackTrace();
 		  return;
 	  }
 
 	  String message = (String) request.getSession().getAttribute("message");
 
       out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
       out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
       out.println("<head>");
 
 
 if (view == Constants.STANDARD_VIEW) {
 	out.println("  <title>NCI Term Browser - Value Set Source View</title>");
 } else {
 	out.println("  <title>NCI Term Browser - Value Set Terminology View</title>");
 }
 
 
       //out.println("  <title>NCI Thesaurus</title>");
       out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
       out.println("");
       out.println("<style type=\"text/css\">");
       out.println("/*margin and padding on body element");
       out.println("  can introduce errors in determining");
       out.println("  element position and are not recommended;");
       out.println("  we turn them off as a foundation for YUI");
       out.println("  CSS treatments. */");
       out.println("body {");
       out.println("	margin:0;");
       out.println("	padding:0;");
       out.println("}");
       out.println("</style>");
       out.println("");
       out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.9.0/build/fonts/fonts-min.css\" />");
       out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.9.0/build/treeview/assets/skins/sam/treeview.css\" />");
       out.println("");
       out.println("<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.9.0/build/yahoo-dom-event/yahoo-dom-event.js\"></script>");
       //Before(GF31982): out.println("<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.9.0/build/treeview/treeview-min.js\"></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/treeview-min.js\" ></script>"); //GF31982
       out.println("");
       out.println("");
       out.println("<!-- Dependency -->");
       out.println("<script src=\"http://yui.yahooapis.com/2.9.0/build/yahoo/yahoo-min.js\"></script>");
       out.println("");
       out.println("<!-- Source file -->");
       out.println("<!--");
       out.println("	If you require only basic HTTP transaction support, use the");
       out.println("	connection_core.js file.");
       out.println("-->");
       out.println("<script src=\"http://yui.yahooapis.com/2.9.0/build/connection/connection_core-min.js\"></script>");
       out.println("");
       out.println("<!--");
       out.println("	Use the full connection.js if you require the following features:");
       out.println("	- Form serialization.");
       out.println("	- File Upload using the iframe transport.");
       out.println("	- Cross-domain(XDR) transactions.");
       out.println("-->");
       out.println("<script src=\"http://yui.yahooapis.com/2.9.0/build/connection/connection-min.js\"></script>");
       out.println("");
       out.println("");
       out.println("");
       out.println("<!--begin custom header content for this example-->");
       out.println("<!--Additional custom style rules for this example:-->");
       out.println("<style type=\"text/css\">");
       out.println("");
       out.println("");
       out.println(".ygtvcheck0 { background: url(/ncitbrowser/images/yui/treeview/check0.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
       out.println(".ygtvcheck1 { background: url(/ncitbrowser/images/yui/treeview/check1.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
       out.println(".ygtvcheck2 { background: url(/ncitbrowser/images/yui/treeview/check2.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
       out.println("");
       out.println("");
       out.println(".ygtv-edit-TaskNode  {	width: 190px;}");
       out.println(".ygtv-edit-TaskNode .ygtvcancel, .ygtv-edit-TextNode .ygtvok  {	border:none;}");
       out.println(".ygtv-edit-TaskNode .ygtv-button-container { float: right;}");
       out.println(".ygtv-edit-TaskNode .ygtv-input  input{	width: 140px;}");
       out.println(".whitebg {");
       out.println("	background-color:white;");
       out.println("}");
       out.println("</style>");
       out.println("");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/styleSheet.css\" />");
       out.println("  <link rel=\"shortcut icon\" href=\"/ncitbrowser/favicon.ico\" type=\"image/x-icon\" />");
       out.println("");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/script.js\"></script>");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/tasknode.js\"></script>");
 
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/search.js\"></script>");
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/dropdown.js\"></script>");
 
 
       out.println("");
       out.println("  <script type=\"text/javascript\">");
       out.println("");
       out.println("    function refresh() {");
       out.println("");
       out.println("      var selectValueSetSearchOptionObj = document.forms[\"valueSetSearchForm\"].selectValueSetSearchOption;");
       out.println("");
       out.println("      for (var i=0; i<selectValueSetSearchOptionObj.length; i++) {");
       out.println("        if (selectValueSetSearchOptionObj[i].checked) {");
       out.println("            selectValueSetSearchOption = selectValueSetSearchOptionObj[i].value;");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("");
       out.println("      window.location.href=\"/ncitbrowser/pages/value_set_source_view.jsf?refresh=1\""); //Before(GF31982)
       //GF31982(Not Sure): out.println("      window.location.href=\"/ncitbrowser/ajax?action=create_src_vs_tree?refresh=1\"");
       out.println("          + \"&nav_type=valuesets\" + \"&opt=\"+ selectValueSetSearchOption;");
       out.println("");
       out.println("    }");
       out.println("  </script>");
       out.println("");
       out.println("  <script language=\"JavaScript\">");
       out.println("");
       out.println("    var tree;");
       out.println("    var nodeIndex;");
       out.println("    var nodes = [];");
       out.println("");
       out.println("    function load(url,target) {");
       out.println("      if (target != '')");
       out.println("        target.window.location.href = url;");
       out.println("      else");
       out.println("        window.location.href = url;");
       out.println("    }");
       out.println("");
       out.println("    function init() {");
       out.println("       //initTree();");
       out.println("    }");
       out.println("");
       out.println("	//handler for expanding all nodes");
       out.println("	YAHOO.util.Event.on(\"expand_all\", \"click\", function(e) {");
       out.println("	     //expandEntireTree();");
       out.println("");
       out.println("	     tree.expandAll();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("	//handler for collapsing all nodes");
       out.println("	YAHOO.util.Event.on(\"collapse_all\", \"click\", function(e) {");
       out.println("		tree.collapseAll();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("	//handler for checking all nodes");
       out.println("	YAHOO.util.Event.on(\"check_all\", \"click\", function(e) {");
       out.println("		check_all();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("	//handler for unchecking all nodes");
       out.println("	YAHOO.util.Event.on(\"uncheck_all\", \"click\", function(e) {");
       out.println("		uncheck_all();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("");
       out.println("");
       out.println("	YAHOO.util.Event.on(\"getchecked\", \"click\", function(e) {");
       out.println("               //alert(\"Checked nodes: \" + YAHOO.lang.dump(getCheckedNodes()), \"info\", \"example\");");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("");
       out.println("	});");
       out.println("");
       out.println("");
       out.println("    function addTreeNode(rootNode, nodeInfo) {");
       out.println("      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       out.println("");
       out.println("      if (nodeInfo.ontology_node_id.indexOf(\"TVS_\") >= 0) {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id };");
       out.println("      } else {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       out.println("      }");
       out.println("");
       out.println("      var newNode = new YAHOO.widget.TaskNode(newNodeData, rootNode, false);");
       out.println("      if (nodeInfo.ontology_node_child_count > 0) {");
       out.println("        newNode.setDynamicLoad(loadNodeData);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function buildTree(ontology_node_id, ontology_display_name) {");
       out.println("      var handleBuildTreeSuccess = function(o) {");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("          if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            if (respObj.root_nodes.length == 0) {");
       out.println("              //showEmptyRoot();");
       out.println("            }");
       out.println("            else {");
       out.println("              for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.root_nodes[i];");
       out.println("                var expand = false;");
       out.println("                //addTreeNode(root, nodeInfo, expand);");
       out.println("");
       out.println("                addTreeNode(root, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("");
       out.println("            tree.draw();");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=build_src_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function resetTree(ontology_node_id, ontology_display_name) {");
       out.println("");
       out.println("      var handleResetTreeSuccess = function(o) {");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("          if ( typeof(respObj.root_node) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.root_node.ontology_node_id + \"');\";");
       out.println("            var rootNodeData = { label:respObj.root_node.ontology_node_name, id:respObj.root_node.ontology_node_id, href:nodeDetails };");
       out.println("            var expand = false;");
       out.println("            if (respObj.root_node.ontology_node_child_count > 0) {");
       out.println("              expand = true;");
       out.println("            }");
       out.println("            var ontRoot = new YAHOO.widget.TaskNode(rootNodeData, root, expand);");
       out.println("");
       out.println("            if ( typeof(respObj.child_nodes) != \"undefined\") {");
       out.println("              for (var i=0; i < respObj.child_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.child_nodes[i];");
       out.println("                addTreeNode(ontRoot, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("            tree.draw();");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleResetTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var resetTreeCallback =");
       out.println("      {");
       out.println("        success:handleResetTreeSuccess,");
       out.println("        failure:handleResetTreeFailure");
       out.println("      };");
       out.println("      if (ontology_node_id!= '') {");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=reset_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name + '&version='+ ontology_version +'&ontology_source='+ontology_source,resetTreeCallback);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function onClickTreeNode(ontology_node_id) {");
       out.println("        //alert(\"onClickTreeNode \" + ontology_node_id);");
       out.println("        window.location = '/ncitbrowser/pages/value_set_treenode_redirect.jsf?ontology_node_id=' + ontology_node_id;");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function onClickViewEntireOntology(ontology_display_name) {");
       out.println("      var ontology_display_name = document.pg_form.ontology_display_name.value;");
       out.println("      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
 
 
       out.println("      tree.draw();");
 
 
 //      out.println("      buildTree('', ontology_display_name);");
 
 
 
 
       out.println("    }");
       out.println("");
       out.println("    function initTree() {");
       out.println("");
       out.println("        tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       out.println("	tree.setNodesProperty('propagateHighlightUp',true);");
       out.println("	tree.setNodesProperty('propagateHighlightDown',true);");
       out.println("	tree.subscribe('keydown',tree._onKeyDownEvent);");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("		    tree.subscribe(\"expand\", function(node) {");
       out.println("");
       out.println("			YAHOO.util.UserAction.keydown(document.body, { keyCode: 39 });");
       out.println("");
       out.println("		    });");
       out.println("");
       out.println("");
       out.println("");
       out.println("		    tree.subscribe(\"collapse\", function(node) {");
       out.println("			//alert(\"Collapsing \" + node.label );");
       out.println("");
       out.println("			YAHOO.util.UserAction.keydown(document.body, { keyCode: 109 });");
       out.println("		    });");
       out.println("");
       out.println("		    // By default, trees with TextNodes will fire an event for when the label is clicked:");
       out.println("		    tree.subscribe(\"checkClick\", function(node) {");
       out.println("			//alert(node.data.myNodeId + \" label was checked\");");
       out.println("		    });");
       out.println("");
       out.println("");
 
       println(out, "            var root = tree.getRoot();");
 
 
 HashMap value_set_tree_hmap = null;
 if (view == Constants.STANDARD_VIEW) {
 	value_set_tree_hmap = DataUtils.getSourceValueSetTree();
 } else {
 	value_set_tree_hmap = DataUtils.getCodingSchemeValueSetTree();
 }
 
 
  TreeItem root = (TreeItem) value_set_tree_hmap.get("<Root>");
 
  //
  //new ValueSetUtils().printTree(out, root);
  new ValueSetUtils().printTree(out, root, view);
 
 
  String contextPath = request.getContextPath();
  String view_str = new Integer(view).toString();
 
 
 //[#31914] Search option and algorithm in value set search box are not preserved in session.
 //String option = (String) request.getSession().getAttribute("selectValueSetSearchOption");
 //String algorithm = (String) request.getSession().getAttribute("valueset_search_algorithm");
 
 String option = (String) request.getParameter("selectValueSetSearchOption");
 if (DataUtils.isNull(option)) {
 	option = (String) request.getSession().getAttribute("selectValueSetSearchOption");
 }
 
 if (DataUtils.isNull(option)) {
 	option = "Code";
 }
 request.getSession().setAttribute("selectValueSetSearchOption", option);
 
 
 
 String algorithm = (String) request.getParameter("valueset_search_algorithm");
 if (DataUtils.isNull(algorithm)) {
 	algorithm = (String) request.getSession().getAttribute("valueset_search_algorithm");
 }
 
 if (DataUtils.isNull(algorithm)) {
 	algorithm = "exactMatch";
 }
 request.getSession().setAttribute("valueset_search_algorithm", algorithm);
 
 
 
 
 
 
         String matchText = (String) request.getParameter("matchText");
         if (DataUtils.isNull(matchText)) {
 			matchText = (String) request.getSession().getAttribute("matchText");
 		}
 
 		if (DataUtils.isNull(matchText)) {
 			matchText = "";
 		} else {
 			matchText = matchText.trim();
 		}
         request.getSession().setAttribute("matchText", matchText);
 
 
 
 
 String option_code = "";
 String option_name = "";
 if (DataUtils.isNull(option)) {
 	option_code = "checked";
 } else {
 	if (option.compareToIgnoreCase("Code") == 0) {
 		option_code = "checked";
 	}
 	if (option.compareToIgnoreCase("Name") == 0) {
 		option_name = "checked";
 	}
 }
 
 
 String algorithm_exactMatch = "";
 String algorithm_startsWith = "";
 String algorithm_contains = "";
 if (DataUtils.isNull(algorithm)) {
 	algorithm_exactMatch = "checked";
 } else {
 	if (algorithm.compareToIgnoreCase("exactMatch") == 0) {
 		algorithm_exactMatch = "checked";
 	}
 
 	if (algorithm.compareToIgnoreCase("startsWith") == 0) {
 		algorithm_startsWith = "checked";
 	}
 
 	if (algorithm.compareToIgnoreCase("contains") == 0) {
 		algorithm_contains = "checked";
 	}
 }
 
 
 System.out.println("*** OPTION: " + option);
 System.out.println("*** ALGORITHM: " + algorithm);
 
 System.out.println("*** matchText: " + matchText);
 
 System.out.println("AjaxServlet option_code: " + option_code);
 System.out.println("AjaxServlet option_name: " + option_name);
 
 System.out.println("AjaxServlet algorithm_exactMatch: " + algorithm_exactMatch);
 System.out.println("AjaxServlet algorithm_startsWith: " + algorithm_startsWith);
 System.out.println("AjaxServlet algorithm_contains: " + algorithm_contains);
 
 
 
 
       out.println("");
       if (message == null) {
       	  out.println("		 tree.collapseAll();");
 	  }
 
 
       //initializeNodeCheckState(out);
 
       out.println("      initializeNodeCheckState();");
 
 
       out.println("      tree.draw();");
 
 
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function onCheckClick(node) {");
       out.println("        YAHOO.log(node.label + \" check was clicked, new state: \" + node.checkState, \"info\", \"example\");");
       out.println("    }");
       out.println("");
       out.println("    function check_all() {");
       out.println("        var topNodes = tree.getRoot().children;");
       out.println("        for(var i=0; i<topNodes.length; ++i) {");
       out.println("            topNodes[i].check();");
       out.println("        }");
       out.println("    }");
       out.println("");
       out.println("    function uncheck_all() {");
       out.println("        var topNodes = tree.getRoot().children;");
       out.println("        for(var i=0; i<topNodes.length; ++i) {");
       out.println("            topNodes[i].uncheck();");
       out.println("        }");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function expand_all() {");
       out.println("        //alert(\"expand_all\");");
       out.println("        var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("        onClickViewEntireOntology(ontology_display_name);");
       out.println("    }");
       out.println("");
       out.println("");
 
    // 0=unchecked, 1=some children checked, 2=all children checked
 /*
       out.println("   // Gets the labels of all of the fully checked nodes");
       out.println("   // Could be updated to only return checked leaf nodes by evaluating");
       out.println("   // the children collection first.");
       out.println("    function getCheckedNodes(nodes) {");
       out.println("        nodes = nodes || tree.getRoot().children;");
       out.println("        checkedNodes = [];");
       out.println("        for(var i=0, l=nodes.length; i<l; i=i+1) {");
       out.println("            var n = nodes[i];");
       out.println("            //if (n.checkState > 0) { // if we were interested in the nodes that have some but not all children checked");
       out.println("            if (n.checkState == 2) {");
       out.println("                checkedNodes.push(n.label); // just using label for simplicity");
       out.println("");
       out.println("		    if (n.hasChildren()) {");
       out.println("			checkedNodes = checkedNodes.concat(getCheckedNodes(n.children));");
       out.println("		    }");
       out.println("");
       out.println("            }");
       out.println("        }");
       out.println("");
       out.println("       var checked_vocabularies = document.forms[\"valueSetSearchForm\"].checked_vocabularies;");
       out.println("       checked_vocabularies.value = checkedNodes;");
       out.println("");
       out.println("       return checkedNodes;");
       out.println("    }");
 */
       out.println("    function getCheckedVocabularies(nodes) {");
       out.println("       getCheckedNodes(nodes);");
       out.println("       getPartialCheckedNodes(nodes);");
       out.println("    }");
 
       writeInitialize(out);
       initializeNodeCheckState(out);
 
       out.println("   // Gets the labels of all of the fully checked nodes");
       out.println("   // Could be updated to only return checked leaf nodes by evaluating");
       out.println("   // the children collection first.");
 
       out.println("    function getCheckedNodes(nodes) {");
       out.println("        nodes = nodes || tree.getRoot().children;");
       out.println("        var checkedNodes = [];");
       out.println("        for(var i=0, l=nodes.length; i<l; i=i+1) {");
       out.println("            var n = nodes[i];");
       out.println("            if (n.checkState == 2) {");
       out.println("                checkedNodes.push(n.label); // just using label for simplicity");
       out.println("            }");
       out.println("		       if (n.hasChildren()) {");
       out.println("			      checkedNodes = checkedNodes.concat(getCheckedNodes(n.children));");
       out.println("		       }");
       out.println("        }");
       //out.println("		   checkedNodes = checkedNodes.concat(\",\");");
       out.println("        var checked_vocabularies = document.forms[\"valueSetSearchForm\"].checked_vocabularies;");
       out.println("        checked_vocabularies.value = checkedNodes;");
       out.println("        return checkedNodes;");
       out.println("    }");
 
 
       out.println("    function getPartialCheckedNodes(nodes) {");
       out.println("        nodes = nodes || tree.getRoot().children;");
       out.println("        var checkedNodes = [];");
       out.println("        for(var i=0, l=nodes.length; i<l; i=i+1) {");
       out.println("            var n = nodes[i];");
       out.println("            if (n.checkState == 1) {");
       out.println("                checkedNodes.push(n.label); // just using label for simplicity");
       out.println("            }");
       out.println("		       if (n.hasChildren()) {");
       out.println("			      checkedNodes = checkedNodes.concat(getPartialCheckedNodes(n.children));");
       out.println("		       }");
       out.println("        }");
       //out.println("		   checkedNodes = checkedNodes.concat(\",\");");
       out.println("        var partial_checked_vocabularies = document.forms[\"valueSetSearchForm\"].partial_checked_vocabularies;");
       out.println("        partial_checked_vocabularies.value = checkedNodes;");
       out.println("        return checkedNodes;");
       out.println("    }");
 
 
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function loadNodeData(node, fnLoadComplete) {");
       out.println("      var id = node.data.id;");
       out.println("");
       out.println("      var responseSuccess = function(o)");
       out.println("      {");
       out.println("        var path;");
       out.println("        var dirs;");
       out.println("        var files;");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        var fileNum = 0;");
       out.println("        var categoryNum = 0;");
       out.println("        if ( typeof(respObj.nodes) != \"undefined\") {");
       out.println("          for (var i=0; i < respObj.nodes.length; i++) {");
       out.println("            var name = respObj.nodes[i].ontology_node_name;");
       out.println("            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       out.println("            var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       out.println("            var newNode = new YAHOO.widget.TaskNode(newNodeData, node, false);");
       out.println("            if (respObj.nodes[i].ontology_node_child_count > 0) {");
       out.println("              newNode.setDynamicLoad(loadNodeData);");
       out.println("            }");
       out.println("          }");
       out.println("        }");
       out.println("        tree.draw();");
       out.println("        fnLoadComplete();");
       out.println("      }");
       out.println("");
       out.println("      var responseFailure = function(o){");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var callback =");
       out.println("      {");
       out.println("        success:responseSuccess,");
       out.println("        failure:responseFailure");
       out.println("      };");
       out.println("");
       out.println("      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("      var cObj = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_src_vs_tree&ontology_node_id=' +id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version,callback);");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function searchTree(ontology_node_id, ontology_display_name) {");
       out.println("");
       out.println("        var handleBuildTreeSuccess = function(o) {");
       out.println("");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("");
       out.println("          if ( typeof(respObj.dummy_root_nodes) != \"undefined\") {");
       out.println("              showNodeNotFound(ontology_node_id);");
       out.println("          }");
       out.println("");
       out.println("          else if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            if (respObj.root_nodes.length == 0) {");
       out.println("              //showEmptyRoot();");
       out.println("            }");
       out.println("            else {");
       out.println("              showPartialHierarchy();");
       out.println("              showConstructingTreeStatus();");
       out.println("");
       out.println("              for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.root_nodes[i];");
       out.println("                //var expand = false;");
       out.println("                addTreeBranch(ontology_node_id, root, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        var ontology_source = null;//document.pg_form.ontology_source.value;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=search_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function expandEntireTree() {");
       out.println("        tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       out.println("        //tree.draw();");
       out.println("");
       out.println("        var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("        var ontology_node_id = document.forms[\"pg_form\"].ontology_node_id.value;");
       out.println("");
       out.println("        var handleBuildTreeSuccess = function(o) {");
       out.println("");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("");
       out.println("             if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("");
       out.println("                    //alert(respObj.root_nodes.length);");
       out.println("");
       out.println("                    var root = tree.getRoot();");
       out.println("		    if (respObj.root_nodes.length == 0) {");
       out.println("		      //showEmptyRoot();");
       out.println("		    } else {");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("		      for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("			 var nodeInfo = respObj.root_nodes[i];");
       out.println("	                 //alert(\"calling addTreeBranch \");");
       out.println("");
       out.println("			 addTreeBranch(ontology_node_id, root, nodeInfo);");
       out.println("		      }");
       out.println("		    }");
       out.println("              }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_entire_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function addTreeBranch(ontology_node_id, rootNode, nodeInfo) {");
       out.println("      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       out.println("");
       out.println("      var newNodeData;");
       out.println("      if (ontology_node_id.indexOf(\"TVS_\") >= 0) {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id };");
       out.println("      } else {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       out.println("      }");
       out.println("");
       out.println("      var expand = false;");
       out.println("      var childNodes = nodeInfo.children_nodes;");
       out.println("");
       out.println("      if (childNodes.length > 0) {");
       out.println("          expand = true;");
       out.println("      }");
       out.println("      var newNode = new YAHOO.widget.TaskNode(newNodeData, rootNode, expand);");
       out.println("      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       out.println("          newNode.labelStyle = \"ygtvlabel_highlight\";");
       out.println("      }");
       out.println("");
       out.println("      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       out.println("         newNode.isLeaf = true;");
       out.println("         if (nodeInfo.ontology_node_child_count > 0) {");
       out.println("             newNode.isLeaf = false;");
       out.println("             newNode.setDynamicLoad(loadNodeData);");
       out.println("         } else {");
       out.println("             tree.draw();");
       out.println("         }");
       out.println("");
       out.println("      } else {");
       out.println("          if (nodeInfo.ontology_node_id != ontology_node_id) {");
       out.println("          if (nodeInfo.ontology_node_child_count == 0 && nodeInfo.ontology_node_id != ontology_node_id) {");
       out.println("        newNode.isLeaf = true;");
       out.println("          } else if (childNodes.length == 0) {");
       out.println("        newNode.setDynamicLoad(loadNodeData);");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      tree.draw();");
       out.println("      for (var i=0; i < childNodes.length; i++) {");
       out.println("         var childnodeInfo = childNodes[i];");
       out.println("         addTreeBranch(ontology_node_id, newNode, childnodeInfo);");
       out.println("      }");
       out.println("    }");
       out.println("    YAHOO.util.Event.addListener(window, \"load\", init);");
       out.println("");
       out.println("    YAHOO.util.Event.onDOMReady(initTree);");
       out.println("");
       out.println("");
       out.println("  </script>");
       out.println("");
       out.println("</head>");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
 
       out.println("<body onLoad=\"document.forms.valueSetSearchForm.matchText.focus();\">");
       //out.println("<body onLoad=\"initialize();\">");
 
 
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/wz_tooltip.js\"></script>");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/tip_centerwindow.js\"></script>");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/tip_followscroll.js\"></script>");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("  <!-- Begin Skip Top Navigation -->");
       out.println("  <a href=\"#evs-content\" class=\"hideLink\" accesskey=\"1\" title=\"Skip repetitive navigation links\">skip navigation links</A>");
       out.println("  <!-- End Skip Top Navigation -->");
       out.println("");
       out.println("<!-- nci banner -->");
       out.println("<div class=\"ncibanner\">");
       out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
       out.println("    <img src=\"/ncitbrowser/images/logotype.gif\"");
       out.println("      width=\"440\" height=\"39\" border=\"0\"");
       out.println("      alt=\"National Cancer Institute\"/>");
       out.println("  </a>");
       out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
       out.println("    <img src=\"/ncitbrowser/images/spacer.gif\"");
       out.println("      width=\"48\" height=\"39\" border=\"0\"");
       out.println("      alt=\"National Cancer Institute\" class=\"print-header\"/>");
       out.println("  </a>");
       out.println("  <a href=\"http://www.nih.gov\" target=\"_blank\" >");
       out.println("    <img src=\"/ncitbrowser/images/tagline_nologo.gif\"");
       out.println("      width=\"173\" height=\"39\" border=\"0\"");
       out.println("      alt=\"U.S. National Institutes of Health\"/>");
       out.println("  </a>");
       out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
       out.println("    <img src=\"/ncitbrowser/images/cancer-gov.gif\"");
       out.println("      width=\"99\" height=\"39\" border=\"0\"");
       out.println("      alt=\"www.cancer.gov\"/>");
       out.println("  </a>");
       out.println("</div>");
       out.println("<!-- end nci banner -->");
       out.println("");
       out.println("  <div class=\"center-page\">");
       out.println("    <!-- EVS Logo -->");
       out.println("<div>");
       out.println("  <img src=\"/ncitbrowser/images/evs-logo-swapped.gif\" alt=\"EVS Logo\"");
       out.println("       width=\"745\" height=\"26\" border=\"0\"");
       out.println("       usemap=\"#external-evs\" />");
       out.println("  <map id=\"external-evs\" name=\"external-evs\">");
       out.println("    <area shape=\"rect\" coords=\"0,0,140,26\"");
       out.println("      href=\"/ncitbrowser/start.jsf\" target=\"_self\"");
       out.println("      alt=\"NCI Term Browser\" />");
       out.println("    <area shape=\"rect\" coords=\"520,0,745,26\"");
       out.println("      href=\"http://evs.nci.nih.gov/\" target=\"_blank\"");
       out.println("      alt=\"Enterprise Vocabulary Services\" />");
       out.println("  </map>");
       out.println("</div>");
       out.println("");
       out.println("");
       out.println("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
       out.println("  <tr>");
       out.println("    <td width=\"5\"></td>");
       out.println("    <td><a href=\"/ncitbrowser/pages/multiple_search.jsf?nav_type=terminologies\">");
       out.println("      <img name=\"tab_terms\" src=\"/ncitbrowser/images/tab_terms.gif\"");
       out.println("        border=\"0\" alt=\"Terminologies\" title=\"Terminologies\" /></a></td>");
       //Before(GF31982): out.println("    <td><a href=\"/ncitbrowser/pages/value_set_source_view.jsf?nav_type=valuesets\">");
       out.println("    <td><a href=\"/ncitbrowser/ajax?action=create_src_vs_tree\">"); //GF31982
       out.println("      <img name=\"tab_valuesets\" src=\"/ncitbrowser/images/tab_valuesets_clicked.gif\"");
       out.println("        border=\"0\" alt=\"Value Sets\" title=\"ValueSets\" /></a></td>");
       out.println("    <td><a href=\"/ncitbrowser/pages/mapping_search.jsf?nav_type=mappings\">");
       out.println("      <img name=\"tab_map\" src=\"/ncitbrowser/images/tab_map.gif\"");
       out.println("        border=\"0\" alt=\"Mappings\" title=\"Mappings\" /></a></td>");
       out.println("  </tr>");
       out.println("</table>");
       out.println("");
       out.println("<div class=\"mainbox-top\"><img src=\"/ncitbrowser/images/mainbox-top.gif\" width=\"745\" height=\"5\" alt=\"\"/></div>");
       out.println("<!-- end EVS Logo -->");
       out.println("    <!-- Main box -->");
       out.println("    <div id=\"main-area\">");
       out.println("");
       out.println("      <!-- Thesaurus, banner search area -->");
       out.println("      <div class=\"bannerarea\">");
       out.println("        <a href=\"/ncitbrowser/start.jsf\" style=\"text-decoration: none;\">");
       out.println("          <div class=\"vocabularynamebanner_tb\">");
       out.println("            <span class=\"vocabularynamelong_tb\">" + JSPUtils.getApplicationVersionDisplay() + "</span>");
       out.println("          </div>");
       out.println("        </a>");
 
       out.println("        <div class=\"search-globalnav\">");
       out.println("          <!-- Search box -->");
       out.println("          <div class=\"searchbox-top\"><img src=\"/ncitbrowser/images/searchbox-top.gif\" width=\"352\" height=\"2\" alt=\"SearchBox Top\" /></div>");
       out.println("          <div class=\"searchbox\">");
       out.println("");
       out.println("");
 
 
       //out.println("<form id=\"valueSetSearchForm\" name=\"valueSetSearchForm\" method=\"post\" action=\"" + contextPath + + "/ajax?action=saerch_value_set_tree\"> "/pages/value_set_source_view.jsf\" class=\"search-form-main-area\" enctype=\"application/x-www-form-urlencoded\">");
       out.println("<form id=\"valueSetSearchForm\" name=\"valueSetSearchForm\" method=\"post\" action=\"" + contextPath + "/ajax?action=search_value_set\" class=\"search-form-main-area\" enctype=\"application/x-www-form-urlencoded\">");
 
       out.println("<input type=\"hidden\" name=\"valueSetSearchForm\" value=\"valueSetSearchForm\" />");
 
       out.println("<input type=\"hidden\" name=\"view\" value=\"" + view_str + "\" />");
 
 
 
 
       out.println("");
       out.println("");
       out.println("");
       out.println("            <input type=\"hidden\" id=\"checked_vocabularies\" name=\"checked_vocabularies\" value=\"\" />");
       out.println("            <input type=\"hidden\" id=\"partial_checked_vocabularies\" name=\"partial_checked_vocabularies\" value=\"\" />");
 
       out.println("");
       out.println("");
       out.println("");
       out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 2px\" >");
       out.println("  <tr valign=\"top\" align=\"left\">");
       out.println("    <td align=\"left\" class=\"textbody\">");
       out.println("");
       out.println("                  <input CLASS=\"searchbox-input-2\"");
       out.println("                    name=\"matchText\"");
       out.println("                    value=\"" + matchText + "\"");
       out.println("                    onFocus=\"active = true\"");
       out.println("                    onBlur=\"active = false\"");
       out.println("                    onkeypress=\"return submitEnter('valueSetSearchForm:valueset_search',event)\"");
       out.println("                    tabindex=\"1\"/>");
       out.println("");
       out.println("");
       out.println("                <input id=\"valueSetSearchForm:valueset_search\" type=\"image\" src=\"/ncitbrowser/images/search.gif\" name=\"valueSetSearchForm:valueset_search\" alt=\"Search Value Sets\" onclick=\"javascript:getCheckedVocabularies();\" tabindex=\"2\" class=\"searchbox-btn\" /><a href=\"/ncitbrowser/pages/help.jsf#searchhelp\" tabindex=\"3\"><img src=\"/ncitbrowser/images/search-help.gif\" alt=\"Search Help\" style=\"border-width:0;\" class=\"searchbox-btn\" /></a>");
       out.println("");
       out.println("");
       out.println("    </td>");
       out.println("  </tr>");
       out.println("");
       out.println("  <tr valign=\"top\" align=\"left\">");
       out.println("    <td>");
       out.println("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 0px\">");
       out.println("");
       out.println("        <tr valign=\"top\" align=\"left\">");
       out.println("        <td align=\"left\" class=\"textbody\">");
       out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"exactMatch\" alt=\"Exact Match\" " + algorithm_exactMatch + " tabindex=\"3\">Exact Match&nbsp;");
       out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"startsWith\" alt=\"Begins With\" " + algorithm_startsWith + " tabindex=\"3\">Begins With&nbsp;");
       out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"contains\" alt=\"Contains\" "      + algorithm_contains   + " tabindex=\"3\">Contains");
       out.println("        </td>");
       out.println("        </tr>");
       out.println("");
       out.println("        <tr align=\"left\">");
       out.println("            <td height=\"1px\" bgcolor=\"#2F2F5F\" align=\"left\"></td>");
       out.println("        </tr>");
       out.println("        <tr valign=\"top\" align=\"left\">");
       out.println("          <td align=\"left\" class=\"textbody\">");
       out.println("                <input type=\"radio\" id=\"selectValueSetSearchOption\" name=\"selectValueSetSearchOption\" value=\"Code\" " + option_code + " alt=\"Code\" tabindex=\"1\" >Code&nbsp;");
       out.println("                <input type=\"radio\" id=\"selectValueSetSearchOption\" name=\"selectValueSetSearchOption\" value=\"Name\" " + option_name + " alt=\"Name\" tabindex=\"1\" >Name");
       out.println("          </td>");
       out.println("        </tr>");
       out.println("      </table>");
       out.println("    </td>");
       out.println("  </tr>");
       out.println("</table>");
       out.println("                <input type=\"hidden\" name=\"referer\" id=\"referer\" value=\"http%3A%2F%2Flocalhost%3A8080%2Fncitbrowser%2Fpages%2Fresolved_value_set_search_results.jsf\">");
       out.println("                <input type=\"hidden\" id=\"nav_type\" name=\"nav_type\" value=\"valuesets\" />");
       out.println("                <input type=\"hidden\" id=\"view\" name=\"view\" value=\"source\" />");
       out.println("");
       out.println("<input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"j_id22:j_id23\" />");
       out.println("</form>");
 
 
 
       addHiddenForm(out, checked_vocabularies, partial_checked_vocabularies);
 
 
 
       out.println("          </div> <!-- searchbox -->");
       out.println("");
       out.println("          <div class=\"searchbox-bottom\"><img src=\"/ncitbrowser/images/searchbox-bottom.gif\" width=\"352\" height=\"2\" alt=\"SearchBox Bottom\" /></div>");
       out.println("          <!-- end Search box -->");
       out.println("          <!-- Global Navigation -->");
       out.println("");
       out.println("<table class=\"global-nav\" border=\"0\" width=\"100%\" height=\"37px\" cellpadding=\"0\" cellspacing=\"0\">");
       out.println("  <tr>");
       out.println("    <td align=\"left\" valign=\"bottom\">");
       out.println("      <a href=\"#\" onclick=\"javascript:window.open('/ncitbrowser/pages/source_help_info-termbrowser.jsf',");
       out.println("        '_blank','top=100, left=100, height=740, width=780, status=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no');\" tabindex=\"13\">");
       out.println("        Sources</a>");
       out.println("");
 
 
 
  String cart_size = (String) request.getSession().getAttribute("cart_size");
  if (!DataUtils.isNull(cart_size)) {
           out.write("|");
           out.write("                        <a href=\"");
           out.print(request.getContextPath());
           out.write("/pages/cart.jsf\" tabindex=\"16\">Cart</a>\r\n");
  }
 
 
 //KLO, 022612
 	  out.println(" \r\n");
 	  out.println("      ");
 	  out.print( VisitedConceptUtils.getDisplayLink(request, true) );
 	  out.println(" \r\n");
 
 // Visited concepts -- to be implemented.
 //      out.println("      | <A href=\"#\" onmouseover=\"Tip('<ul><li><a href=\'/ncitbrowser/ConceptReport.jsp?dictionary=NCI Thesaurus&version=11.09d&code=C44256\'>Ratio &#40;NCI Thesaurus 11.09d&#41;</a><br></li></ul>',WIDTH, 300, TITLE, 'Visited Concepts', SHADOW, true, FADEIN, 300, FADEOUT, 300, STICKY, 1, CLOSEBTN, true, CLICKCLOSE, true)\" onmouseout=UnTip() >Visited Concepts</A>");
 
       out.println("    </td>");
       out.println("    <td align=\"right\" valign=\"bottom\">");
 
 	  out.println("      <a href=\"");
 	  out.print( request.getContextPath() );
 	  out.println("/pages/help.jsf\" tabindex=\"16\">Help</a>\r\n");
 	  out.println("    </td>\r\n");
 	  out.println("    <td width=\"7\"></td>\r\n");
 	  out.println("  </tr>\r\n");
 	  out.println("</table>");
 
 /*
       out.println("      <a href=\"/ncitbrowser/pages/help.jsf\" tabindex=\"16\">Help</a>");
       out.println("    </td>");
       out.println("    <td width=\"7\"></td>");
       out.println("  </tr>");
       out.println("</table>");
 */
 
       out.println("          <!-- end Global Navigation -->");
       out.println("");
       out.println("        </div> <!-- search-globalnav -->");
       out.println("      </div> <!-- bannerarea -->");
       out.println("");
       out.println("      <!-- end Thesaurus, banner search area -->");
       out.println("      <!-- Quick links bar -->");
       out.println("");
       out.println("<div class=\"bluebar\">");
       out.println("  <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
       out.println("  <tr>");
       out.println("    <td><div class=\"quicklink-status\">&nbsp;</div></td>");
       out.println("    <td>");
       out.println("");
 
       /*
       out.println("  <div id=\"quicklinksholder\">");
       out.println("      <ul id=\"quicklinks\"");
       out.println("        onmouseover=\"document.quicklinksimg.src='/ncitbrowser/images/quicklinks-active.gif';\"");
       out.println("        onmouseout=\"document.quicklinksimg.src='/ncitbrowser/images/quicklinks-inactive.gif';\">");
       out.println("        <li>");
       out.println("          <a href=\"#\" tabindex=\"-1\"><img src=\"/ncitbrowser/images/quicklinks-inactive.gif\" width=\"162\"");
       out.println("            height=\"18\" border=\"0\" name=\"quicklinksimg\" alt=\"Quick Links\" />");
       out.println("          </a>");
       out.println("          <ul>");
       out.println("            <li><a href=\"http://evs.nci.nih.gov/\" tabindex=\"-1\" target=\"_blank\"");
       out.println("              alt=\"Enterprise Vocabulary Services\">EVS Home</a></li>");
       out.println("            <li><a href=\"http://localhost/ncimbrowserncimbrowser\" tabindex=\"-1\" target=\"_blank\"");
       out.println("              alt=\"NCI Metathesaurus\">NCI Metathesaurus Browser</a></li>");
       out.println("");
       out.println("            <li><a href=\"/ncitbrowser/start.jsf\" tabindex=\"-1\"");
       out.println("              alt=\"NCI Term Browser\">NCI Term Browser</a></li>");
       out.println("            <li><a href=\"http://www.cancer.gov/cancertopics/terminologyresources\" tabindex=\"-1\" target=\"_blank\"");
       out.println("              alt=\"NCI Terminology Resources\">NCI Terminology Resources</a></li>");
       out.println("");
       out.println("              <li><a href=\"http://ncitermform.nci.nih.gov/ncitermform/?dictionary=NCI%20Thesaurus\" tabindex=\"-1\" target=\"_blank\" alt=\"Term Suggestion\">Term Suggestion</a></li>");
       out.println("");
       out.println("");
       out.println("          </ul>");
       out.println("        </li>");
       out.println("      </ul>");
       out.println("  </div>");
       */
       addQuickLink(request, out);
 
       out.println("");
       out.println("      </td>");
       out.println("    </tr>");
       out.println("  </table>");
       out.println("");
       out.println("</div>");
 
       if (! ServerMonitorThread.getInstance().isLexEVSRunning()) {
       out.println("    <div class=\"redbar\">");
       out.println("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
       out.println("        <tr>");
       out.println("          <td class=\"lexevs-status\">");
       out.println("            " + ServerMonitorThread.getInstance().getMessage());
       out.println("          </td>");
       out.println("        </tr>");
       out.println("      </table>");
       out.println("    </div>");
       }
 
       out.println("      <!-- end Quick links bar -->");
       out.println("");
       out.println("      <!-- Page content -->");
       out.println("      <div class=\"pagecontent\">");
       out.println("");
 
 
       if (message != null) {
           out.println("\r\n");
           out.println("      <p class=\"textbodyred\">");
           out.print(message);
           out.println("</p>\r\n");
           out.println("    ");
           request.getSession().removeAttribute("message");
       }
 
       out.println("<p class=\"textbody\">");
       out.println("View value sets organized by standards category or source terminology.");
       out.println("Standards categories group the value sets supporting them; all other labels lead to the home pages of actual value sets or source terminologies.");
       out.println("Search or browse a value set from its home page, or search all value sets at once from this page (very slow) to find which ones contain a particular code or term.");
       out.println("</p>");
       out.println("");
       out.println("        <div id=\"popupContentArea\">");
       out.println("          <a name=\"evs-content\" id=\"evs-content\"></a>");
       out.println("");
       out.println("          <table width=\"580px\" cellpadding=\"3\" cellspacing=\"0\" border=\"0\">");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("            <tr class=\"textbody\">");
       out.println("              <td class=\"textbody\" align=\"left\">");
       out.println("");
 
 
 
 if (view == Constants.STANDARD_VIEW) {
       out.println("                Standards View");
       out.println("                &nbsp;|");
       out.println("                <a href=\"" + contextPath + "/ajax?action=create_cs_vs_tree\">Terminology View</a>");
 } else {
       out.println("                <a href=\"" + contextPath + "/ajax?action=create_src_vs_tree\">Standards View</a>");
       out.println("                &nbsp;|");
       out.println("                Terminology View");
 }
       out.println("              </td>");
       out.println("");
       out.println("              <td align=\"right\">");
       out.println("               <font size=\"1\" color=\"red\" align=\"right\">");
       out.println("                 <a href=\"javascript:printPage()\"><img src=\"/ncitbrowser/images/printer.bmp\" border=\"0\" alt=\"Send to Printer\"><i>Send to Printer</i></a>");
       out.println("               </font>");
 
 
       out.println("              </td>");
       out.println("            </tr>");
       out.println("          </table>");
       out.println("");
       out.println("          <hr/>");
       out.println("");
       out.println("");
       out.println("");
       out.println("<style>");
       out.println("#expandcontractdiv {border:1px solid #336600; background-color:#FFFFCC; margin:0 0 .5em 0; padding:0.2em;}");
       out.println("#treecontainer { background: #fff }");
       out.println("</style>");
       out.println("");
       out.println("");
       out.println("<div id=\"expandcontractdiv\">");
       out.println("	<a id=\"expand_all\" href=\"#\">Expand all</a>");
       out.println("	<a id=\"collapse_all\" href=\"#\">Collapse all</a>");
       out.println("	<a id=\"check_all\" href=\"#\">Check all</a>");
       out.println("	<a id=\"uncheck_all\" href=\"#\">Uncheck all</a>");
       out.println("</div>");
       out.println("");
       out.println("");
       out.println("");
       out.println("          <!-- Tree content -->");
       out.println("");
       out.println("          <div id=\"treecontainer\" class=\"ygtv-checkbox\"></div>");
       out.println("");
       out.println("          <form id=\"pg_form\">");
       out.println("            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"null\" />");
       out.println("            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"null\" />");
       out.println("            <input type=\"hidden\" id=\"view\" name=\"view\" value=\"source\" />");
       out.println("          </form>");
 
 
 
 
 
       out.println("");
       out.println("");
       out.println("        </div> <!-- popupContentArea -->");
       out.println("");
       out.println("");
       out.println("<div class=\"textbody\">");
       out.println("<!-- footer -->");
       out.println("<div class=\"footer\" style=\"width:720px\">");
       out.println("  <ul>");
       out.println("    <li><a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\">NCI Home</a> |</li>");
       out.println("    <li><a href=\"/ncitbrowser/pages/contact_us.jsf\">Contact Us</a> |</li>");
       out.println("    <li><a href=\"http://www.cancer.gov/policies\" target=\"_blank\" alt=\"National Cancer Institute Policies\">Policies</a> |</li>");
       out.println("    <li><a href=\"http://www.cancer.gov/policies/page3\" target=\"_blank\" alt=\"National Cancer Institute Accessibility\">Accessibility</a> |</li>");
       out.println("    <li><a href=\"http://www.cancer.gov/policies/page6\" target=\"_blank\" alt=\"National Cancer Institute FOIA\">FOIA</a></li>");
       out.println("  </ul>");
       out.println("  <p>");
       out.println("    A Service of the National Cancer Institute<br />");
       out.println("    <img src=\"/ncitbrowser/images/external-footer-logos.gif\"");
       out.println("      alt=\"External Footer Logos\" width=\"238\" height=\"34\" border=\"0\"");
       out.println("      usemap=\"#external-footer\" />");
       out.println("  </p>");
       out.println("  <map id=\"external-footer\" name=\"external-footer\">");
       out.println("    <area shape=\"rect\" coords=\"0,0,46,34\"");
       out.println("      href=\"http://www.cancer.gov\" target=\"_blank\"");
       out.println("      alt=\"National Cancer Institute\" />");
       out.println("    <area shape=\"rect\" coords=\"55,1,99,32\"");
       out.println("      href=\"http://www.hhs.gov/\" target=\"_blank\"");
       out.println("      alt=\"U.S. Health &amp; Human Services\" />");
       out.println("    <area shape=\"rect\" coords=\"103,1,147,31\"");
       out.println("      href=\"http://www.nih.gov/\" target=\"_blank\"");
       out.println("      alt=\"National Institutes of Health\" />");
       out.println("    <area shape=\"rect\" coords=\"148,1,235,33\"");
       out.println("      href=\"http://www.usa.gov/\" target=\"_blank\"");
       out.println("      alt=\"USA.gov\" />");
       out.println("  </map>");
       out.println("</div>");
       out.println("<!-- end footer -->");
       out.println("</div>");
       out.println("");
       out.println("");
       out.println("      </div> <!-- pagecontent -->");
       out.println("    </div> <!--  main-area -->");
       out.println("    <div class=\"mainbox-bottom\"><img src=\"/ncitbrowser/images/mainbox-bottom.gif\" width=\"745\" height=\"5\" alt=\"Mainbox Bottom\" /></div>");
       out.println("");
       out.println("  </div> <!-- center-page -->");
       out.println("");
       out.println("</body>");
       out.println("</html>");
       out.println("");
   }
 
 
     public static void search_value_set(HttpServletRequest request, HttpServletResponse response) {
         String selectValueSetSearchOption = (String) request.getParameter("selectValueSetSearchOption");
 		request.getSession().setAttribute("selectValueSetSearchOption", selectValueSetSearchOption);
 
         String algorithm = (String) request.getParameter("valueset_search_algorithm");
         request.getSession().setAttribute("valueset_search_algorithm", algorithm);
 
 		// check if any checkbox is checked.
         String contextPath = request.getContextPath();
 		String view_str = (String) request.getParameter("view");
 		int view = Integer.parseInt(view_str);
 		String msg = null;
 
 		request.getSession().removeAttribute("checked_vocabularies");
 		String checked_vocabularies = (String) request.getParameter("checked_vocabularies");
 		System.out.println("checked_vocabularies: " + checked_vocabularies);
 
 		request.getSession().removeAttribute("partial_checked_vocabularies");
 		String partial_checked_vocabularies = (String) request.getParameter("partial_checked_vocabularies");
 		System.out.println("partial_checked_vocabularies: " + partial_checked_vocabularies);
 
 
         String matchText = (String) request.getParameter("matchText");
         if (DataUtils.isNull(matchText)) {
 			matchText = "";
 		} else {
 			matchText = matchText.trim();
 		}
         request.getSession().setAttribute("matchText", matchText);
 
 		String ontology_display_name = (String) request.getParameter("ontology_display_name");
 		String ontology_version = (String) request.getParameter("ontology_version");
 
 		if (matchText.compareTo("") == 0) {
 			msg = "Please enter a search string.";
 			System.out.println(msg);
 			request.getSession().setAttribute("message", msg);
 
 
 			if (!DataUtils.isNull(ontology_display_name) && !DataUtils.isNull(ontology_version)) {
 				create_vs_tree(request, response, view, ontology_display_name, ontology_version);
 			} else {
 			    create_vs_tree(request, response, view);
 			}
 			return;
 		}
 
 
         if (checked_vocabularies == null || (checked_vocabularies != null && checked_vocabularies.compareTo("") == 0)) { //DYEE
 			msg = "No value set definition is selected.";
 			System.out.println(msg);
 			request.getSession().setAttribute("message", msg);
 
 
 			if (!DataUtils.isNull(ontology_display_name) && !DataUtils.isNull(ontology_version)) {
 				create_vs_tree(request, response, view, ontology_display_name, ontology_version);
 			} else {
 			    create_vs_tree(request, response, view);
 			}
 
 
 		} else {
 			String destination = contextPath + "/pages/value_set_search_results.jsf";
 			try {
 				String retstr = valueSetSearchAction(request);
 
 				//KLO, 041312
 				if (retstr.compareTo("message") == 0) {
 
 					if (!DataUtils.isNull(ontology_display_name) && !DataUtils.isNull(ontology_version)) {
 						create_vs_tree(request, response, view, ontology_display_name, ontology_version);
 					} else {
 						create_vs_tree(request, response, view);
 					}
 					return;
 				}
 
 
 				System.out.println("(*) redirecting to: " + destination);
 				response.sendRedirect(response.encodeRedirectURL(destination));
 	            request.getSession().setAttribute("checked_vocabularies", checked_vocabularies);
 			} catch (Exception ex) {
 				System.out.println("response.sendRedirect failed???");
 			}
 	    }
     }
 
 
     public static String valueSetSearchAction(HttpServletRequest request) {
 		java.lang.String valueSetDefinitionRevisionId = null;
 		String msg = null;
 
         String selectValueSetSearchOption = (String) request.getParameter("selectValueSetSearchOption");
 
         if (DataUtils.isNull(selectValueSetSearchOption)) {
 			selectValueSetSearchOption = "Name";
 		}
 		request.getSession().setAttribute("selectValueSetSearchOption", selectValueSetSearchOption);
 
         String algorithm = (String) request.getParameter("valueset_search_algorithm");
         if (DataUtils.isNull(algorithm)) {
 			algorithm = "exactMatch";
 		}
         request.getSession().setAttribute("valueset_search_algorithm", algorithm);
 
 		String checked_vocabularies = (String) request.getParameter("checked_vocabularies");
 
 		System.out.println("checked_vocabularies: " + checked_vocabularies);
 		if (checked_vocabularies != null && checked_vocabularies.compareTo("") == 0) {
 			msg = "No value set definition is selected.";
 			System.out.println(msg);
 			request.getSession().setAttribute("message", msg);
 			return "message";
 		}
 
 		Vector selected_vocabularies = new Vector();
 		selected_vocabularies = DataUtils.parseData(checked_vocabularies, ",");
 
 		System.out.println("selected_vocabularies count: " + selected_vocabularies.size());
 
 
         String VSD_view = (String) request.getParameter("view");
         request.getSession().setAttribute("view", VSD_view);
 
         String matchText = (String) request.getParameter("matchText");
 
         Vector v = new Vector();
         LexEVSValueSetDefinitionServices vsd_service = null;
         vsd_service = RemoteServerUtil.getLexEVSValueSetDefinitionServices();
 
         if (matchText != null) matchText = matchText.trim();
 		if (selectValueSetSearchOption.compareTo("Code") == 0) {
             String uri = null;
 
 			try {
 				String versionTag = null;//"PRODUCTION";
 				if (checked_vocabularies != null) {
 					for (int k=0; k<selected_vocabularies.size(); k++) {
 						String vsd_name = (String) selected_vocabularies.elementAt(k);
 						String vsd_uri = DataUtils.getValueSetDefinitionURIByName(vsd_name);
 
 						System.out.println("vsd_name: " + vsd_name + " (vsd_uri: " + vsd_uri + ")");
 
                         try {
 							//ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(vsd_uri), null);
 							if (vsd_uri != null) {
                                 ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(vsd_uri), null);
 								AbsoluteCodingSchemeVersionReference acsvr = vsd_service.isEntityInValueSet(matchText,
 									  new URI(vsd_uri),
 									  null,
 									  versionTag);
 								if (acsvr != null) {
 									String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 									if (metadata != null) {
 										v.add(metadata);
 									}
 								}
 							} else {
 								System.out.println("WARNING: Unable to find vsd_uri for " + vsd_name);
 							}
 						} catch (Exception ex) {
                             System.out.println("WARNING: vsd_service.getValueSetDefinition threw exception: " + vsd_name);
 						}
 
 					}
 			    } else {
 				    AbsoluteCodingSchemeVersionReferenceList csVersionList = null;//ValueSetHierarchy.getAbsoluteCodingSchemeVersionReferenceList();
 					List list = vsd_service.listValueSetsWithEntityCode(matchText, null, csVersionList, versionTag);
 					if (list != null) {
 
 						for (int j=0; j<list.size(); j++) {
 							uri = (String) list.get(j);
 
 							String vsd_name = DataUtils.valueSetDefiniionURI2Name(uri);
 							if (selected_vocabularies.contains(vsd_name)) {
 								try {
 									ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), null);
 									if (vsd == null) {
 										msg = "Unable to find any value set with URI " + uri + ".";
 										request.getSession().setAttribute("message", msg);
 										return "message";
 									}
 
 									String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 									if (metadata != null) {
 										v.add(metadata);
 									}
 
 								} catch (Exception ex) {
 									ex.printStackTrace();
 									msg = "Unable to find any value set with URI " + uri + ".";
 									request.getSession().setAttribute("message", msg);
 									return "message";
 								}
 
 							}
 
 						}
 					}
 			    }
 
 
 				request.getSession().setAttribute("matched_vsds", v);
 				if (v.size() == 0) {
 					msg = "No match found.";
 					request.getSession().setAttribute("message", msg);
 					return "message";
 				} else if (v.size() == 1) {
 					request.getSession().setAttribute("vsd_uri", uri);
 				}
 
 				return "value_set";
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				System.out.println("vsd_service.listValueSetsWithEntityCode throws exceptions???");
 			}
 
 			msg = "Unexpected errors encountered; search by code failed.";
 			request.getSession().setAttribute("message", msg);
 
 			return "message";
 
 		} else if (selectValueSetSearchOption.compareTo("Name") == 0) {
 
             String uri = null;
 			try {
 
 				Vector uri_vec = DataUtils.getValueSetURIs();
 
                 for (int i=0; i<uri_vec.size(); i++) {
 					uri = (String) uri_vec.elementAt(i);
 
 					String vsd_name = DataUtils.valueSetDefiniionURI2Name(uri);
 
 					if (checked_vocabularies == null || selected_vocabularies.contains(vsd_name)) {
 
 						//System.out.println("Searching " + vsd_name + "...");
 
 						AbsoluteCodingSchemeVersionReferenceList csVersionList = null;
 						/*
 						Vector cs_ref_vec = DataUtils.getCodingSchemeReferencesInValueSetDefinition(uri, "PRODUCTION");
 						if (cs_ref_vec != null) {
 							csVersionList = DataUtils.vector2CodingSchemeVersionReferenceList(cs_ref_vec);
 						}
 						*/
 
 						ResolvedValueSetCodedNodeSet rvs_cns = null;
 						SortOptionList sortOptions = null;
 						LocalNameList propertyNames = null;
 						CodedNodeSet.PropertyType[] propertyTypes = null;
 
 						try {
 							System.out.println("URI: " + uri);
 
 							rvs_cns = vsd_service.getValueSetDefinitionEntitiesForTerm(matchText, algorithm, new URI(uri), csVersionList, null);
 
 							if (rvs_cns != null) {
 								CodedNodeSet cns = rvs_cns.getCodedNodeSet();
 								ResolvedConceptReferencesIterator itr = cns.resolve(sortOptions, propertyNames, propertyTypes);
 								if (itr != null && itr.numberRemaining() > 0) {
 
 									AbsoluteCodingSchemeVersionReferenceList ref_list = rvs_cns.getCodingSchemeVersionRefList();
 									if (ref_list.getAbsoluteCodingSchemeVersionReferenceCount() > 0) {
 										try {
 											ValueSetDefinition vsd = vsd_service.getValueSetDefinition(new URI(uri), null);
 											if (vsd == null) {
 												msg = "Unable to find any value set with name " + matchText + ".";
 												request.getSession().setAttribute("message", msg);
 												return "message";
 											}
 
 											String metadata = DataUtils.getValueSetDefinitionMetadata(vsd);
 											if (metadata != null) {
 												v.add(metadata);
 
 											}
 
 										} catch (Exception ex) {
 											ex.printStackTrace();
 											msg = "Unable to find any value set with name " + matchText + ".";
 											request.getSession().setAttribute("message", msg);
 											return "message";
 										}
 									}
 								}
 							}
 
 
 						} catch (Exception ex) {
 							//System.out.println("WARNING: getValueSetDefinitionEntitiesForTerm throws exception???");
 							msg = "getValueSetDefinitionEntitiesForTerm throws exception -- search by \"" + matchText + "\" failed. (VSD URI: " + uri + ")";
 							System.out.println(msg);
 							request.getSession().setAttribute("message", msg);
 
 							ex.printStackTrace();
 							return "message";
 						}
 					}
 				}
 
 				request.getSession().setAttribute("matched_vsds", v);
 				if (v.size() == 0) {
 					msg = "No match found.";
 					request.getSession().setAttribute("message", msg);
 					return "message";
 				} else if (v.size() == 1) {
 					request.getSession().setAttribute("vsd_uri", uri);
 				}
 				return "value_set";
 
 			} catch (Exception ex) {
 				//ex.printStackTrace();
 				System.out.println("vsd_service.getValueSetDefinitionEntitiesForTerm throws exceptions???");
 			}
 
 			msg = "Unexpected errors encountered; search by name failed.";
 			request.getSession().setAttribute("message", msg);
 			return "message";
 
 		}
 		return "value_set";
     }
 
 
     public static void create_vs_tree(HttpServletRequest request, HttpServletResponse response, int view, String dictionary, String version) {
 
 	  request.getSession().removeAttribute("b");
 	  request.getSession().removeAttribute("m");
 
       response.setContentType("text/html");
       PrintWriter out = null;
 
 
 		String checked_vocabularies = (String) request.getParameter("checked_vocabularies");
 		System.out.println("checked_vocabularies: " + checked_vocabularies);
 
 		String partial_checked_vocabularies = (String) request.getParameter("partial_checked_vocabularies");
 		System.out.println("partial_checked_vocabularies: " + partial_checked_vocabularies);
 
 
       try {
       	  out = response.getWriter();
       } catch (Exception ex) {
 		  ex.printStackTrace();
 		  return;
 	  }
 
 	  String message = (String) request.getSession().getAttribute("message");
 
       out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
       out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
       out.println("<head>");
 
 	  out.println("  <title>" + dictionary + " value set</title>");
 
       //out.println("  <title>NCI Thesaurus</title>");
       out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
       out.println("");
       out.println("<style type=\"text/css\">");
       out.println("/*margin and padding on body element");
       out.println("  can introduce errors in determining");
       out.println("  element position and are not recommended;");
       out.println("  we turn them off as a foundation for YUI");
       out.println("  CSS treatments. */");
       out.println("body {");
       out.println("	margin:0;");
       out.println("	padding:0;");
       out.println("}");
       out.println("</style>");
       out.println("");
       out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.9.0/build/fonts/fonts-min.css\" />");
       out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.9.0/build/treeview/assets/skins/sam/treeview.css\" />");
       out.println("");
       out.println("<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.9.0/build/yahoo-dom-event/yahoo-dom-event.js\"></script>");
       out.println("<script type=\"text/javascript\" src=\"/ncitbrowser/js/yui/treeview-min.js\" ></script>");
       out.println("");
       out.println("");
       out.println("<!-- Dependency -->");
       out.println("<script src=\"http://yui.yahooapis.com/2.9.0/build/yahoo/yahoo-min.js\"></script>");
       out.println("");
       out.println("<!-- Source file -->");
       out.println("<!--");
       out.println("	If you require only basic HTTP transaction support, use the");
       out.println("	connection_core.js file.");
       out.println("-->");
       out.println("<script src=\"http://yui.yahooapis.com/2.9.0/build/connection/connection_core-min.js\"></script>");
       out.println("");
       out.println("<!--");
       out.println("	Use the full connection.js if you require the following features:");
       out.println("	- Form serialization.");
       out.println("	- File Upload using the iframe transport.");
       out.println("	- Cross-domain(XDR) transactions.");
       out.println("-->");
       out.println("<script src=\"http://yui.yahooapis.com/2.9.0/build/connection/connection-min.js\"></script>");
       out.println("");
       out.println("");
       out.println("");
       out.println("<!--begin custom header content for this example-->");
       out.println("<!--Additional custom style rules for this example:-->");
       out.println("<style type=\"text/css\">");
       out.println("");
       out.println("");
       out.println(".ygtvcheck0 { background: url(/ncitbrowser/images/yui/treeview/check0.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
       out.println(".ygtvcheck1 { background: url(/ncitbrowser/images/yui/treeview/check1.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
       out.println(".ygtvcheck2 { background: url(/ncitbrowser/images/yui/treeview/check2.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
       out.println("");
       out.println("");
       out.println(".ygtv-edit-TaskNode  {	width: 190px;}");
       out.println(".ygtv-edit-TaskNode .ygtvcancel, .ygtv-edit-TextNode .ygtvok  {	border:none;}");
       out.println(".ygtv-edit-TaskNode .ygtv-button-container { float: right;}");
       out.println(".ygtv-edit-TaskNode .ygtv-input  input{	width: 140px;}");
       out.println(".whitebg {");
       out.println("	background-color:white;");
       out.println("}");
       out.println("</style>");
       out.println("");
       out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/ncitbrowser/css/styleSheet.css\" />");
       out.println("  <link rel=\"shortcut icon\" href=\"/ncitbrowser/favicon.ico\" type=\"image/x-icon\" />");
       out.println("");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/script.js\"></script>");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/tasknode.js\"></script>");
 
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/search.js\"></script>");
       println(out, "  <script type=\"text/javascript\" src=\"/ncitbrowser/js/dropdown.js\"></script>");
 
       out.println("");
       out.println("  <script type=\"text/javascript\">");
       out.println("");
       out.println("    function refresh() {");
       out.println("");
       out.println("      var selectValueSetSearchOptionObj = document.forms[\"valueSetSearchForm\"].selectValueSetSearchOption;");
       out.println("");
       out.println("      for (var i=0; i<selectValueSetSearchOptionObj.length; i++) {");
       out.println("        if (selectValueSetSearchOptionObj[i].checked) {");
       out.println("            selectValueSetSearchOption = selectValueSetSearchOptionObj[i].value;");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("");
       out.println("      window.location.href=\"/ncitbrowser/pages/value_set_source_view.jsf?refresh=1\"");
       out.println("          + \"&nav_type=valuesets\" + \"&opt=\"+ selectValueSetSearchOption;");
       out.println("");
       out.println("    }");
       out.println("  </script>");
       out.println("");
       out.println("  <script language=\"JavaScript\">");
       out.println("");
       out.println("    var tree;");
       out.println("    var nodeIndex;");
       out.println("    var nodes = [];");
       out.println("");
       out.println("    function load(url,target) {");
       out.println("      if (target != '')");
       out.println("        target.window.location.href = url;");
       out.println("      else");
       out.println("        window.location.href = url;");
       out.println("    }");
       out.println("");
       out.println("    function init() {");
       out.println("       //initTree();");
       out.println("    }");
       out.println("");
       out.println("	//handler for expanding all nodes");
       out.println("	YAHOO.util.Event.on(\"expand_all\", \"click\", function(e) {");
       out.println("	     //expandEntireTree();");
       out.println("");
       out.println("	     tree.expandAll();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("	//handler for collapsing all nodes");
       out.println("	YAHOO.util.Event.on(\"collapse_all\", \"click\", function(e) {");
       out.println("		tree.collapseAll();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("	//handler for checking all nodes");
       out.println("	YAHOO.util.Event.on(\"check_all\", \"click\", function(e) {");
       out.println("		check_all();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("	//handler for unchecking all nodes");
       out.println("	YAHOO.util.Event.on(\"uncheck_all\", \"click\", function(e) {");
       out.println("		uncheck_all();");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("	});");
       out.println("");
       out.println("");
       out.println("");
       out.println("	YAHOO.util.Event.on(\"getchecked\", \"click\", function(e) {");
       out.println("               //alert(\"Checked nodes: \" + YAHOO.lang.dump(getCheckedNodes()), \"info\", \"example\");");
       out.println("		//YAHOO.util.Event.preventDefault(e);");
       out.println("");
       out.println("	});");
       out.println("");
       out.println("");
       out.println("    function addTreeNode(rootNode, nodeInfo) {");
       out.println("      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       out.println("");
       out.println("      if (nodeInfo.ontology_node_id.indexOf(\"TVS_\") >= 0) {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id };");
       out.println("      } else {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       out.println("      }");
       out.println("");
       out.println("      var newNode = new YAHOO.widget.TaskNode(newNodeData, rootNode, false);");
       out.println("      if (nodeInfo.ontology_node_child_count > 0) {");
       out.println("        newNode.setDynamicLoad(loadNodeData);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function buildTree(ontology_node_id, ontology_display_name) {");
       out.println("      var handleBuildTreeSuccess = function(o) {");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("          if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            if (respObj.root_nodes.length == 0) {");
       out.println("              //showEmptyRoot();");
       out.println("            }");
       out.println("            else {");
       out.println("              for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.root_nodes[i];");
       out.println("                var expand = false;");
       out.println("                //addTreeNode(root, nodeInfo, expand);");
       out.println("");
       out.println("                addTreeNode(root, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("");
       out.println("            tree.draw();");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=build_src_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function resetTree(ontology_node_id, ontology_display_name) {");
       out.println("");
       out.println("      var handleResetTreeSuccess = function(o) {");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("          if ( typeof(respObj.root_node) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.root_node.ontology_node_id + \"');\";");
       out.println("            var rootNodeData = { label:respObj.root_node.ontology_node_name, id:respObj.root_node.ontology_node_id, href:nodeDetails };");
       out.println("            var expand = false;");
       out.println("            if (respObj.root_node.ontology_node_child_count > 0) {");
       out.println("              expand = true;");
       out.println("            }");
       out.println("            var ontRoot = new YAHOO.widget.TaskNode(rootNodeData, root, expand);");
       out.println("");
       out.println("            if ( typeof(respObj.child_nodes) != \"undefined\") {");
       out.println("              for (var i=0; i < respObj.child_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.child_nodes[i];");
       out.println("                addTreeNode(ontRoot, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("            tree.draw();");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleResetTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var resetTreeCallback =");
       out.println("      {");
       out.println("        success:handleResetTreeSuccess,");
       out.println("        failure:handleResetTreeFailure");
       out.println("      };");
       out.println("      if (ontology_node_id!= '') {");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=reset_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name + '&version='+ ontology_version +'&ontology_source='+ontology_source,resetTreeCallback);");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("    function onClickTreeNode(ontology_node_id) {");
       out.println("        //alert(\"onClickTreeNode \" + ontology_node_id);");
       out.println("        window.location = '/ncitbrowser/pages/value_set_treenode_redirect.jsf?ontology_node_id=' + ontology_node_id;");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function onClickViewEntireOntology(ontology_display_name) {");
       out.println("      var ontology_display_name = document.pg_form.ontology_display_name.value;");
       out.println("      tree = new YAHOO.widget.TreeView(\"treecontainer\");");
 
 
       out.println("      tree.draw();");
 
 
       out.println("    }");
       out.println("");
       out.println("    function initTree() {");
       out.println("");
       out.println("        tree = new YAHOO.widget.TreeView(\"treecontainer\");");
 
       //out.println("         pre_check();");
 
 
       out.println("	tree.setNodesProperty('propagateHighlightUp',true);");
       out.println("	tree.setNodesProperty('propagateHighlightDown',true);");
       out.println("	tree.subscribe('keydown',tree._onKeyDownEvent);");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("		    tree.subscribe(\"expand\", function(node) {");
       out.println("");
       out.println("			YAHOO.util.UserAction.keydown(document.body, { keyCode: 39 });");
 
 
       out.println("");
       out.println("		    });");
       out.println("");
       out.println("");
       out.println("");
       out.println("		    tree.subscribe(\"collapse\", function(node) {");
       out.println("			//alert(\"Collapsing \" + node.label );");
       out.println("");
       out.println("			YAHOO.util.UserAction.keydown(document.body, { keyCode: 109 });");
       out.println("		    });");
       out.println("");
       out.println("		    // By default, trees with TextNodes will fire an event for when the label is clicked:");
       out.println("		    tree.subscribe(\"checkClick\", function(node) {");
       out.println("			//alert(node.data.myNodeId + \" label was checked\");");
       out.println("		    });");
       out.println("");
       out.println("");
 
       println(out, "            var root = tree.getRoot();");
 
 
  HashMap value_set_tree_hmap = DataUtils.getCodingSchemeValueSetTree();
 
  TreeItem root = (TreeItem) value_set_tree_hmap.get("<Root>");
  new ValueSetUtils().printTree(out, root, Constants.TERMINOLOGY_VIEW, dictionary);
 
  //new ValueSetUtils().printTree(out, root, Constants.TERMINOLOGY_VIEW);
 
 
  String contextPath = request.getContextPath();
  String view_str = new Integer(view).toString();
 
  //String option = (String) request.getSession().getAttribute("selectValueSetSearchOption");
  //String algorithm = (String) request.getSession().getAttribute("valueset_search_algorithm");
 
 
  String option = (String) request.getParameter("selectValueSetSearchOption");
  String algorithm = (String) request.getParameter("valueset_search_algorithm");
 
 
 
 String option_code = "";
 String option_name = "";
 if (DataUtils.isNull(option)) {
 	option_code = "checked";
 } else {
 	if (option.compareToIgnoreCase("Code") == 0) {
 		option_code = "checked";
 	}
 	if (option.compareToIgnoreCase("Name") == 0) {
 		option_name = "checked";
 	}
 }
 
 
 String algorithm_exactMatch = "";
 String algorithm_startsWith = "";
 String algorithm_contains = "";
 if (DataUtils.isNull(algorithm)) {
 	algorithm_exactMatch = "checked";
 } else {
 	if (algorithm.compareToIgnoreCase("exactMatch") == 0) {
 		algorithm_exactMatch = "checked";
 	}
 
 	if (algorithm.compareToIgnoreCase("startsWith") == 0) {
 		algorithm_startsWith = "checked";
 	}
 
 	if (algorithm.compareToIgnoreCase("contains") == 0) {
 		algorithm_contains = "checked";
 	}
 }
 
 
       out.println("");
       if (message == null) {
       	  out.println("		 tree.collapseAll();");
 	  }
 
       out.println("      initializeNodeCheckState();");
 
       out.println("      tree.draw();");
 
 
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function onCheckClick(node) {");
       out.println("        YAHOO.log(node.label + \" check was clicked, new state: \" + node.checkState, \"info\", \"example\");");
       out.println("    }");
       out.println("");
       out.println("    function check_all() {");
       out.println("        var topNodes = tree.getRoot().children;");
       out.println("        for(var i=0; i<topNodes.length; ++i) {");
       out.println("            topNodes[i].check();");
       out.println("        }");
       out.println("    }");
       out.println("");
       out.println("    function uncheck_all() {");
       out.println("        var topNodes = tree.getRoot().children;");
       out.println("        for(var i=0; i<topNodes.length; ++i) {");
       out.println("            topNodes[i].uncheck();");
       out.println("        }");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function expand_all() {");
       out.println("        //alert(\"expand_all\");");
       out.println("        var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("        onClickViewEntireOntology(ontology_display_name);");
       out.println("    }");
       out.println("");
 
 
       out.println("    function pre_check() {");
       out.println("        var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
 
 
 //out.println(" alert(ontology_display_name);");
 
       out.println("        var topNodes = tree.getRoot().children;");
       out.println("        for(var i=0; i<topNodes.length; ++i) {");
       out.println("            if (topNodes[i].label == ontology_display_name) {");
       out.println("            	topNodes[i].check();");
       out.println("            }");
       out.println("        }");
       out.println("    }");
 
 
       out.println("");
 
    // 0=unchecked, 1=some children checked, 2=all children checked
 
 
 /*
     function getCheckedNodes(nodes) {
         nodes = nodes || tree.getRoot().children;
         checkedNodes = [];
         for(var i=0, l=nodes.length; i<l; i=i+1) {
             var n = nodes[i];
             //if (n.checkState > 0) { // if we were interested in the nodes that have some but not all children checked
             if (n.checkState == 2) {
                 checkedNodes.push(n.label); // just using label for simplicity
             }
 
             if (n.hasChildren()) {
 		        checkedNodes = checkedNodes.concat(getCheckedNodes(n.children));
             }
         }
 
        //var checked_vocabularies = document.forms["valueSetSearchForm"].checked_vocabularies;
        //checked_vocabularies.value = checkedNodes;
 
 
        return checkedNodes;
     }
 
 */
 
 
       out.println("    function getCheckedVocabularies(nodes) {");
       out.println("       getCheckedNodes(nodes);");
       out.println("       getPartialCheckedNodes(nodes);");
       out.println("    }");
 
       writeInitialize(out);
       initializeNodeCheckState(out);
 
       out.println("   // Gets the labels of all of the fully checked nodes");
       out.println("   // Could be updated to only return checked leaf nodes by evaluating");
       out.println("   // the children collection first.");
 
       out.println("    function getCheckedNodes(nodes) {");
       out.println("        nodes = nodes || tree.getRoot().children;");
       out.println("        var checkedNodes = [];");
       out.println("        for(var i=0, l=nodes.length; i<l; i=i+1) {");
       out.println("            var n = nodes[i];");
       out.println("            if (n.checkState == 2) {");
       out.println("                checkedNodes.push(n.label); // just using label for simplicity");
       out.println("            }");
       out.println("		       if (n.hasChildren()) {");
       out.println("			      checkedNodes = checkedNodes.concat(getCheckedNodes(n.children));");
       out.println("		       }");
       out.println("        }");
       //out.println("		   checkedNodes = checkedNodes.concat(\",\");");
       out.println("        var checked_vocabularies = document.forms[\"valueSetSearchForm\"].checked_vocabularies;");
       out.println("        checked_vocabularies.value = checkedNodes;");
       out.println("        return checkedNodes;");
       out.println("    }");
 
 
       out.println("    function getPartialCheckedNodes(nodes) {");
       out.println("        nodes = nodes || tree.getRoot().children;");
       out.println("        var checkedNodes = [];");
       out.println("        for(var i=0, l=nodes.length; i<l; i=i+1) {");
       out.println("            var n = nodes[i];");
       out.println("            if (n.checkState == 1) {");
       out.println("                checkedNodes.push(n.label); // just using label for simplicity");
       out.println("            }");
       out.println("		       if (n.hasChildren()) {");
       out.println("			      checkedNodes = checkedNodes.concat(getPartialCheckedNodes(n.children));");
       out.println("		       }");
       out.println("        }");
       //out.println("		   checkedNodes = checkedNodes.concat(\",\");");
       out.println("        var partial_checked_vocabularies = document.forms[\"valueSetSearchForm\"].partial_checked_vocabularies;");
       out.println("        partial_checked_vocabularies.value = checkedNodes;");
       out.println("        return checkedNodes;");
       out.println("    }");
 
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function loadNodeData(node, fnLoadComplete) {");
       out.println("      var id = node.data.id;");
       out.println("");
       out.println("      var responseSuccess = function(o)");
       out.println("      {");
       out.println("        var path;");
       out.println("        var dirs;");
       out.println("        var files;");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        var fileNum = 0;");
       out.println("        var categoryNum = 0;");
       out.println("        if ( typeof(respObj.nodes) != \"undefined\") {");
       out.println("          for (var i=0; i < respObj.nodes.length; i++) {");
       out.println("            var name = respObj.nodes[i].ontology_node_name;");
       out.println("            var nodeDetails = \"javascript:onClickTreeNode('\" + respObj.nodes[i].ontology_node_id + \"');\";");
       out.println("            var newNodeData = { label:name, id:respObj.nodes[i].ontology_node_id, href:nodeDetails };");
       out.println("            var newNode = new YAHOO.widget.TaskNode(newNodeData, node, false);");
       out.println("            if (respObj.nodes[i].ontology_node_child_count > 0) {");
       out.println("              newNode.setDynamicLoad(loadNodeData);");
       out.println("            }");
       out.println("          }");
       out.println("        }");
       out.println("        tree.draw();");
       out.println("        fnLoadComplete();");
       out.println("      }");
       out.println("");
       out.println("      var responseFailure = function(o){");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var callback =");
       out.println("      {");
       out.println("        success:responseSuccess,");
       out.println("        failure:responseFailure");
       out.println("      };");
       out.println("");
       out.println("      var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("      var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("      var cObj = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_src_vs_tree&ontology_node_id=' +id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version,callback);");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("    function searchTree(ontology_node_id, ontology_display_name) {");
       out.println("");
       out.println("        var handleBuildTreeSuccess = function(o) {");
       out.println("");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("");
       out.println("          if ( typeof(respObj.dummy_root_nodes) != \"undefined\") {");
       out.println("              showNodeNotFound(ontology_node_id);");
       out.println("          }");
       out.println("");
       out.println("          else if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("            var root = tree.getRoot();");
       out.println("            if (respObj.root_nodes.length == 0) {");
       out.println("              //showEmptyRoot();");
       out.println("            }");
       out.println("            else {");
       out.println("              showPartialHierarchy();");
       out.println("              showConstructingTreeStatus();");
       out.println("");
       out.println("              for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("                var nodeInfo = respObj.root_nodes[i];");
       out.println("                //var expand = false;");
       out.println("                addTreeBranch(ontology_node_id, root, nodeInfo);");
       out.println("              }");
       out.println("            }");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        var ontology_source = null;//document.pg_form.ontology_source.value;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=search_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function expandEntireTree() {");
       out.println("        tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       out.println("        //tree.draw();");
       out.println("");
       out.println("        var ontology_display_name = document.forms[\"pg_form\"].ontology_display_name.value;");
       out.println("        var ontology_node_id = document.forms[\"pg_form\"].ontology_node_id.value;");
       out.println("");
       out.println("        var handleBuildTreeSuccess = function(o) {");
       out.println("");
       out.println("        var respTxt = o.responseText;");
       out.println("        var respObj = eval('(' + respTxt + ')');");
       out.println("        if ( typeof(respObj) != \"undefined\") {");
       out.println("");
       out.println("             if ( typeof(respObj.root_nodes) != \"undefined\") {");
       out.println("");
       out.println("                    //alert(respObj.root_nodes.length);");
       out.println("");
       out.println("                    var root = tree.getRoot();");
       out.println("		    if (respObj.root_nodes.length == 0) {");
       out.println("		      //showEmptyRoot();");
       out.println("		    } else {");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("		      for (var i=0; i < respObj.root_nodes.length; i++) {");
       out.println("			 var nodeInfo = respObj.root_nodes[i];");
       out.println("	                 //alert(\"calling addTreeBranch \");");
       out.println("");
       out.println("			 addTreeBranch(ontology_node_id, root, nodeInfo);");
       out.println("		      }");
       out.println("		    }");
       out.println("              }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      var handleBuildTreeFailure = function(o) {");
       out.println("        alert('responseFailure: ' + o.statusText);");
       out.println("      }");
       out.println("");
       out.println("      var buildTreeCallback =");
       out.println("      {");
       out.println("        success:handleBuildTreeSuccess,");
       out.println("        failure:handleBuildTreeFailure");
       out.println("      };");
       out.println("");
       out.println("      if (ontology_display_name!='') {");
       out.println("        var ontology_source = null;");
       out.println("        var ontology_version = document.forms[\"pg_form\"].ontology_version.value;");
       out.println("        var request = YAHOO.util.Connect.asyncRequest('GET','/ncitbrowser/ajax?action=expand_entire_vs_tree&ontology_node_id=' +ontology_node_id+'&ontology_display_name='+ontology_display_name+'&version='+ontology_version+'&ontology_source='+ontology_source,buildTreeCallback);");
       out.println("");
       out.println("      }");
       out.println("    }");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("    function addTreeBranch(ontology_node_id, rootNode, nodeInfo) {");
       out.println("      var newNodeDetails = \"javascript:onClickTreeNode('\" + nodeInfo.ontology_node_id + \"');\";");
       out.println("");
       out.println("      var newNodeData;");
       out.println("      if (ontology_node_id.indexOf(\"TVS_\") >= 0) {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id };");
       out.println("      } else {");
       out.println("          newNodeData = { label:nodeInfo.ontology_node_name, id:nodeInfo.ontology_node_id, href:newNodeDetails };");
       out.println("      }");
       out.println("");
       out.println("      var expand = false;");
       out.println("      var childNodes = nodeInfo.children_nodes;");
       out.println("");
       out.println("      if (childNodes.length > 0) {");
       out.println("          expand = true;");
       out.println("      }");
       out.println("      var newNode = new YAHOO.widget.TaskNode(newNodeData, rootNode, expand);");
       out.println("      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       out.println("          newNode.labelStyle = \"ygtvlabel_highlight\";");
       out.println("      }");
       out.println("");
       out.println("      if (nodeInfo.ontology_node_id == ontology_node_id) {");
       out.println("         newNode.isLeaf = true;");
       out.println("         if (nodeInfo.ontology_node_child_count > 0) {");
       out.println("             newNode.isLeaf = false;");
       out.println("             newNode.setDynamicLoad(loadNodeData);");
       out.println("         } else {");
       out.println("             tree.draw();");
       out.println("         }");
       out.println("");
       out.println("      } else {");
       out.println("          if (nodeInfo.ontology_node_id != ontology_node_id) {");
       out.println("          if (nodeInfo.ontology_node_child_count == 0 && nodeInfo.ontology_node_id != ontology_node_id) {");
       out.println("        newNode.isLeaf = true;");
       out.println("          } else if (childNodes.length == 0) {");
       out.println("        newNode.setDynamicLoad(loadNodeData);");
       out.println("          }");
       out.println("        }");
       out.println("      }");
       out.println("");
       out.println("      tree.draw();");
       out.println("      for (var i=0; i < childNodes.length; i++) {");
       out.println("         var childnodeInfo = childNodes[i];");
       out.println("         addTreeBranch(ontology_node_id, newNode, childnodeInfo);");
       out.println("      }");
       out.println("    }");
       out.println("    YAHOO.util.Event.addListener(window, \"load\", init);");
       out.println("");
       out.println("    YAHOO.util.Event.onDOMReady(initTree);");
       out.println("");
       out.println("");
       out.println("  </script>");
       out.println("");
       out.println("</head>");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       //out.println("<body>");
 
       out.println("<body onLoad=\"document.forms.valueSetSearchForm.matchText.focus();\">");
 
       //out.println("<body onLoad=\"initialize();\">");
 
 
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/wz_tooltip.js\"></script>");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/tip_centerwindow.js\"></script>");
       out.println("  <script type=\"text/javascript\" src=\"/ncitbrowser/js/tip_followscroll.js\"></script>");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("  <!-- Begin Skip Top Navigation -->");
       out.println("  <a href=\"#evs-content\" class=\"hideLink\" accesskey=\"1\" title=\"Skip repetitive navigation links\">skip navigation links</A>");
       out.println("  <!-- End Skip Top Navigation -->");
       out.println("");
       out.println("<!-- nci banner -->");
       out.println("<div class=\"ncibanner\">");
       out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
       out.println("    <img src=\"/ncitbrowser/images/logotype.gif\"");
       out.println("      width=\"440\" height=\"39\" border=\"0\"");
       out.println("      alt=\"National Cancer Institute\"/>");
       out.println("  </a>");
       out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
       out.println("    <img src=\"/ncitbrowser/images/spacer.gif\"");
       out.println("      width=\"48\" height=\"39\" border=\"0\"");
       out.println("      alt=\"National Cancer Institute\" class=\"print-header\"/>");
       out.println("  </a>");
       out.println("  <a href=\"http://www.nih.gov\" target=\"_blank\" >");
       out.println("    <img src=\"/ncitbrowser/images/tagline_nologo.gif\"");
       out.println("      width=\"173\" height=\"39\" border=\"0\"");
       out.println("      alt=\"U.S. National Institutes of Health\"/>");
       out.println("  </a>");
       out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
       out.println("    <img src=\"/ncitbrowser/images/cancer-gov.gif\"");
       out.println("      width=\"99\" height=\"39\" border=\"0\"");
       out.println("      alt=\"www.cancer.gov\"/>");
       out.println("  </a>");
       out.println("</div>");
       out.println("<!-- end nci banner -->");
       out.println("");
       out.println("  <div class=\"center-page\">");
       out.println("    <!-- EVS Logo -->");
       out.println("<div>");
 
 
 // to be modified
       out.println("  <img src=\"/ncitbrowser/images/evs-logo-swapped.gif\" alt=\"EVS Logo\"");
       out.println("       width=\"745\" height=\"26\" border=\"0\"");
       out.println("       usemap=\"#external-evs\" />");
       out.println("  <map id=\"external-evs\" name=\"external-evs\">");
       out.println("    <area shape=\"rect\" coords=\"0,0,140,26\"");
       out.println("      href=\"/ncitbrowser/start.jsf\" target=\"_self\"");
       out.println("      alt=\"NCI Term Browser\" />");
       out.println("    <area shape=\"rect\" coords=\"520,0,745,26\"");
       out.println("      href=\"http://evs.nci.nih.gov/\" target=\"_blank\"");
       out.println("      alt=\"Enterprise Vocabulary Services\" />");
       out.println("  </map>");
 
 
 
       out.println("</div>");
       out.println("");
       out.println("");
       out.println("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
       out.println("  <tr>");
       out.println("    <td width=\"5\"></td>");
 
 //to be modified
       out.println("    <td><a href=\"/ncitbrowser/pages/multiple_search.jsf?nav_type=terminologies\">");
       out.println("      <img name=\"tab_terms\" src=\"/ncitbrowser/images/tab_terms_clicked.gif\"");
       out.println("        border=\"0\" alt=\"Terminologies\" title=\"Terminologies\" /></a></td>");
       out.println("    <td><a href=\"/ncitbrowser/ajax?action=create_src_vs_tree\">");
       out.println("      <img name=\"tab_valuesets\" src=\"/ncitbrowser/images/tab_valuesets.gif\"");
       out.println("        border=\"0\" alt=\"Value Sets\" title=\"ValueSets\" /></a></td>");
       out.println("    <td><a href=\"/ncitbrowser/pages/mapping_search.jsf?nav_type=mappings\">");
       out.println("      <img name=\"tab_map\" src=\"/ncitbrowser/images/tab_map.gif\"");
       out.println("        border=\"0\" alt=\"Mappings\" title=\"Mappings\" /></a></td>");
 
 
       out.println("  </tr>");
       out.println("</table>");
 
 
 
       out.println("");
       out.println("<div class=\"mainbox-top\"><img src=\"/ncitbrowser/images/mainbox-top.gif\" width=\"745\" height=\"5\" alt=\"\"/></div>");
       out.println("<!-- end EVS Logo -->");
       out.println("    <!-- Main box -->");
       out.println("    <div id=\"main-area\">");
       out.println("");
 
 
 
       out.println("      <!-- Thesaurus, banner search area -->");
       out.println("      <div class=\"bannerarea\">");
 
 
 /*
 
       out.println("        <a href=\"/ncitbrowser/start.jsf\" style=\"text-decoration: none;\">");
       out.println("          <div class=\"vocabularynamebanner_tb\">");
       out.println("            <span class=\"vocabularynamelong_tb\">" + JSPUtils.getApplicationVersionDisplay() + "</span>");
       out.println("          </div>");
       out.println("        </a>");
 */
 
 JSPUtils.JSPHeaderInfoMore info = new JSPUtils.JSPHeaderInfoMore(request);
 String scheme = info.dictionary;
 String term_browser_version = info.term_browser_version;
 String display_name = info.display_name;
  String basePath = request.getContextPath();
 
 /*
 <a href="/ncitbrowser/pages/home.jsf?version=12.02d" style="text-decoration: none;">
 	<div class="vocabularynamebanner_ncit">
 	    <span class="vocabularynamelong_ncit">Version: 12.02d (Release date: 2012-02-27-08:00)</span>
 	</div>
 </a>
 
 */
 String release_date = DataUtils.getVersionReleaseDate(scheme, version);
 if (dictionary != null && dictionary.compareTo("NCI Thesaurus") == 0) {
 
 
 
       out.println("<a href=\"/ncitbrowser/pages/home.jsf?version=" + version + "\" style=\"text-decoration: none;\">");
       out.println("	<div class=\"vocabularynamebanner_ncit\">");
       out.println("	    <span class=\"vocabularynamelong_ncit\">Version: " + version + " (Release date: " + release_date + ")</span>");
       out.println("	</div>");
       out.println("</a>");
 
 
       /*
           out.write("\r\n");
           out.write("    <div class=\"banner\"><a href=\"");
           out.print(basePath);
           out.write("\"><img src=\"");
           out.print(basePath);
           out.write("/images/thesaurus_browser_logo.jpg\" width=\"383\" height=\"117\" alt=\"Thesaurus Browser Logo\" border=\"0\"/></a></div>\r\n");
       */
 } else {
 
           out.write("\r\n");
           out.write("\r\n");
           out.write("                ");
 			 if (version == null) {
 					  out.write("\r\n");
 					  out.write("                  <a class=\"vocabularynamebanner\" href=\"");
 					  out.print(request.getContextPath());
 					  out.write("/pages/vocabulary.jsf?dictionary=");
 					  out.print(HTTPUtils.cleanXSS(dictionary));
 					  out.write("\">\r\n");
 					  out.write("                ");
 			 } else {
 					  out.write("\r\n");
 					  out.write("                  <a class=\"vocabularynamebanner\" href=\"");
 					  out.print(request.getContextPath());
 					  out.write("/pages/vocabulary.jsf?dictionary=");
 					  out.print(HTTPUtils.cleanXSS(dictionary));
 					  out.write("&version=");
 					  out.print(HTTPUtils.cleanXSS(version));
 					  out.write("\">\r\n");
 					  out.write("                ");
 			 }
           out.write("\r\n");
           out.write("                    <div class=\"vocabularynamebanner\">\r\n");
           out.write("                      <div class=\"vocabularynameshort\" STYLE=\"font-size: ");
           out.print(HTTPUtils.maxFontSize(display_name));
           out.write("px; font-family : Arial\">\r\n");
           out.write("                          ");
           out.print(HTTPUtils.cleanXSS(display_name));
           out.write("\r\n");
           out.write("                      </div>\r\n");
           out.write("                      \r\n");
 
 
 boolean display_release_date = true;
 if (release_date == null || release_date.compareTo("") == 0) {
     display_release_date = false;
 }
 if (display_release_date) {
 
           out.write("\r\n");
           out.write("    <div class=\"vocabularynamelong\">Version: ");
           out.print(HTTPUtils.cleanXSS(term_browser_version));
           out.write(" (Release date: ");
           out.print(release_date);
           out.write(")</div>\r\n");
 
 } else {
 
           out.write("\r\n");
           out.write("    <div class=\"vocabularynamelong\">Version: ");
           out.print(HTTPUtils.cleanXSS(term_browser_version));
           out.write("</div>\r\n");
 
 }
 
           out.write("                    \r\n");
           out.write("                      \r\n");
           out.write("                    </div>\r\n");
           out.write("                  </a>\r\n");
 
 }
 
 
 
 
 
       out.println("        <div class=\"search-globalnav\">");
       out.println("          <!-- Search box -->");
       out.println("          <div class=\"searchbox-top\"><img src=\"/ncitbrowser/images/searchbox-top.gif\" width=\"352\" height=\"2\" alt=\"SearchBox Top\" /></div>");
       out.println("          <div class=\"searchbox\">");
       out.println("");
       out.println("");
 
       out.println("<form id=\"valueSetSearchForm\" name=\"valueSetSearchForm\" method=\"post\" action=\"" + contextPath + "/ajax?action=search_value_set\" class=\"search-form-main-area\" enctype=\"application/x-www-form-urlencoded\">");
 
       out.println("<input type=\"hidden\" name=\"valueSetSearchForm\" value=\"valueSetSearchForm\" />");
 
       out.println("<input type=\"hidden\" name=\"view\" value=\"" + view_str + "\" />");
 
 
 String matchText = (String) request.getSession().getAttribute("matchText");
 if (DataUtils.isNull(matchText)) {
 	matchText = "";
 }
 
 
 
 
       out.println("");
       out.println("");
       out.println("");
       out.println("            <input type=\"hidden\" id=\"checked_vocabularies\" name=\"checked_vocabularies\" value=\"\" />");
       out.println("            <input type=\"hidden\" id=\"partial_checked_vocabularies\" name=\"partial_checked_vocabularies\" value=\"\" />");
       out.println("");
       out.println("");
       out.println("");
       out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 2px\" >");
       out.println("  <tr valign=\"top\" align=\"left\">");
       out.println("    <td align=\"left\" class=\"textbody\">");
       out.println("");
       out.println("                  <input CLASS=\"searchbox-input-2\"");
       out.println("                    name=\"matchText\"");
       out.println("                    value=\"" + matchText + "\"");
       out.println("                    onFocus=\"active = true\"");
       out.println("                    onBlur=\"active = false\"");
       out.println("                    onkeypress=\"return submitEnter('valueSetSearchForm:valueset_search',event)\"");
       out.println("                    tabindex=\"1\"/>");
       out.println("");
       out.println("");
       out.println("                <input id=\"valueSetSearchForm:valueset_search\" type=\"image\" src=\"/ncitbrowser/images/search.gif\" name=\"valueSetSearchForm:valueset_search\" alt=\"Search Value Sets\" onclick=\"javascript:getCheckedVocabularies();\" tabindex=\"2\" class=\"searchbox-btn\" /><a href=\"/ncitbrowser/pages/help.jsf#searchhelp\" tabindex=\"3\"><img src=\"/ncitbrowser/images/search-help.gif\" alt=\"Search Help\" style=\"border-width:0;\" class=\"searchbox-btn\" /></a>");
       out.println("");
       out.println("");
       out.println("    </td>");
       out.println("  </tr>");
       out.println("");
       out.println("  <tr valign=\"top\" align=\"left\">");
       out.println("    <td>");
       out.println("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 0px\">");
       out.println("");
       out.println("        <tr valign=\"top\" align=\"left\">");
       out.println("        <td align=\"left\" class=\"textbody\">");
       out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"exactMatch\" alt=\"Exact Match\" " + algorithm_exactMatch + " tabindex=\"3\">Exact Match&nbsp;");
       out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"startsWith\" alt=\"Begins With\" " + algorithm_startsWith + " tabindex=\"3\">Begins With&nbsp;");
       out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"contains\" alt=\"Contains\" "      + algorithm_contains   + " tabindex=\"3\">Contains");
       out.println("        </td>");
       out.println("        </tr>");
       out.println("");
       out.println("        <tr align=\"left\">");
       out.println("            <td height=\"1px\" bgcolor=\"#2F2F5F\" align=\"left\"></td>");
       out.println("        </tr>");
       out.println("        <tr valign=\"top\" align=\"left\">");
       out.println("          <td align=\"left\" class=\"textbody\">");
       out.println("                <input type=\"radio\" id=\"selectValueSetSearchOption\" name=\"selectValueSetSearchOption\" value=\"Code\" " + option_code + " alt=\"Code\" tabindex=\"1\" >Code&nbsp;");
       out.println("                <input type=\"radio\" id=\"selectValueSetSearchOption\" name=\"selectValueSetSearchOption\" value=\"Name\" " + option_name + " alt=\"Name\" tabindex=\"1\" >Name");
       out.println("          </td>");
       out.println("        </tr>");
       out.println("      </table>");
       out.println("    </td>");
       out.println("  </tr>");
       out.println("</table>");
       out.println("                <input type=\"hidden\" name=\"referer\" id=\"referer\" value=\"http%3A%2F%2Flocalhost%3A8080%2Fncitbrowser%2Fpages%2Fresolved_value_set_search_results.jsf\">");
       out.println("                <input type=\"hidden\" id=\"nav_type\" name=\"nav_type\" value=\"valuesets\" />");
       out.println("                <input type=\"hidden\" id=\"view\" name=\"view\" value=\"source\" />");
 
       out.println("            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"" + dictionary + "\" />");
       out.println("            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"" + dictionary + "\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"" + version + "\" />");
 
 
 
       out.println("");
       out.println("<input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"j_id22:j_id23\" />");
       out.println("</form>");
 
       addHiddenForm(out, checked_vocabularies, partial_checked_vocabularies);
 
 
       out.println("          </div> <!-- searchbox -->");
       out.println("");
       out.println("          <div class=\"searchbox-bottom\"><img src=\"/ncitbrowser/images/searchbox-bottom.gif\" width=\"352\" height=\"2\" alt=\"SearchBox Bottom\" /></div>");
       out.println("          <!-- end Search box -->");
       out.println("          <!-- Global Navigation -->");
       out.println("");
 
 
 
 /*
       out.println("<table class=\"global-nav\" border=\"0\" width=\"100%\" height=\"37px\" cellpadding=\"0\" cellspacing=\"0\">");
       out.println("  <tr>");
       out.println("    <td align=\"left\" valign=\"bottom\">");
       out.println("      <a href=\"#\" onclick=\"javascript:window.open('/ncitbrowser/pages/source_help_info-termbrowser.jsf',");
       out.println("        '_blank','top=100, left=100, height=740, width=780, status=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no');\" tabindex=\"13\">");
       out.println("        Sources</a>");
       out.println("");
 
 
 	  out.println(" \r\n");
 	  out.println("      ");
 	  out.print( VisitedConceptUtils.getDisplayLink(request, true) );
 	  out.println(" \r\n");
 
       out.println("    </td>");
       out.println("    <td align=\"right\" valign=\"bottom\">");
 
 	  out.println("      <a href=\"");
 	  out.print( request.getContextPath() );
 	  out.println("/pages/help.jsf\" tabindex=\"16\">Help</a>\r\n");
 	  out.println("    </td>\r\n");
 	  out.println("    <td width=\"7\"></td>\r\n");
 	  out.println("  </tr>\r\n");
 	  out.println("</table>");
 */
  boolean hasValueSet = ValueSetHierarchy.hasValueSet(scheme);
  boolean hasMapping = DataUtils.hasMapping(scheme);
 
  boolean tree_access_allowed = true;
  if (DataUtils._vocabulariesWithoutTreeAccessHashSet.contains(scheme)) {
      tree_access_allowed = false;
  }
  boolean vocabulary_isMapping = DataUtils.isMapping(scheme, null);
 
 
          out.write("                  <table class=\"global-nav\" border=\"0\" width=\"100%\" height=\"37px\" cellpadding=\"0\" cellspacing=\"0\">\r\n");
           out.write("                    <tr>\r\n");
           out.write("                      <td valign=\"bottom\">\r\n");
           out.write("                         ");
  Boolean[] isPipeDisplayed = new Boolean[] { Boolean.FALSE };
           out.write("\r\n");
           out.write("                         ");
  if (vocabulary_isMapping) {
           out.write("\r\n");
           out.write("                              ");
           out.print( JSPUtils.getPipeSeparator(isPipeDisplayed) );
           out.write("\r\n");
           out.write("                              <a href=\"");
           out.print(request.getContextPath() );
           out.write("/pages/mapping.jsf?dictionary=");
           out.print(HTTPUtils.cleanXSS(scheme));
           out.write("&version=");
           out.print(version);
           out.write("\">\r\n");
           out.write("                                Mapping\r\n");
           out.write("                              </a>\r\n");
           out.write("                         ");
  } else if (tree_access_allowed) {
           out.write("\r\n");
           out.write("                              ");
           out.print( JSPUtils.getPipeSeparator(isPipeDisplayed) );
           out.write("\r\n");
           out.write("                              <a href=\"#\" onclick=\"javascript:window.open('");
           out.print(request.getContextPath());
           out.write("/pages/hierarchy.jsf?dictionary=");
           out.print(HTTPUtils.cleanXSS(scheme));
           out.write("&version=");
           out.print(HTTPUtils.cleanXSS(version));
           out.write("', '_blank','top=100, left=100, height=740, width=680, status=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no');\" tabindex=\"12\">\r\n");
           out.write("                                Hierarchy </a>\r\n");
           out.write("                         ");
  }
           out.write("       \r\n");
           out.write("                                \r\n");
           out.write("                                \r\n");
           out.write("      ");
  if (hasValueSet) {
           out.write("\r\n");
           out.write("        ");
           out.print( JSPUtils.getPipeSeparator(isPipeDisplayed) );
           out.write("\r\n");
           out.write("        <!--\r\n");
           out.write("        <a href=\"");
           out.print( request.getContextPath() );
           out.write("/pages/value_set_hierarchy.jsf?dictionary=");
           out.print(HTTPUtils.cleanXSS(scheme));
           out.write("&version=");
           out.print(HTTPUtils.cleanXSS(version));
           out.write("\" tabindex=\"15\">Value Sets</a>\r\n");
           out.write("        -->\r\n");
           out.write("        <a href=\"");
           out.print( request.getContextPath() );
           out.write("/ajax?action=create_cs_vs_tree&dictionary=");
           out.print(HTTPUtils.cleanXSS(scheme));
           out.write("&version=");
           out.print(HTTPUtils.cleanXSS(version));
           out.write("\" tabindex=\"15\">Value Sets</a>\r\n");
           out.write("\r\n");
           out.write("\r\n");
           out.write("      ");
  }
           out.write("\r\n");
           out.write("      \r\n");
           out.write("      ");
  if (hasMapping) {
           out.write("\r\n");
           out.write("          ");
           out.print( JSPUtils.getPipeSeparator(isPipeDisplayed) );
           out.write("\r\n");
           out.write("          <a href=\"");
           out.print( request.getContextPath() );
           out.write("/pages/cs_mappings.jsf?dictionary=");
           out.print(HTTPUtils.cleanXSS(scheme));
           out.write("&version=");
           out.print(HTTPUtils.cleanXSS(version));
           out.write("\" tabindex=\"15\">Maps</a>      \r\n");
           out.write("      ");
  }
 
 
  String cart_size = (String) request.getSession().getAttribute("cart_size");
  if (!DataUtils.isNull(cart_size)) {
           out.write("|");
           out.write("                        <a href=\"");
           out.print(request.getContextPath());
           out.write("/pages/cart.jsf\" tabindex=\"16\">Cart</a>\r\n");
  }
 
 
           out.write("                         ");
           out.print( VisitedConceptUtils.getDisplayLink(request, isPipeDisplayed) );
           out.write("\r\n");
           out.write("                      </td>\r\n");
           out.write("                      <td align=\"right\" valign=\"bottom\">\r\n");
           out.write("                        <a href=\"");
           out.print(request.getContextPath());
           out.write("/pages/help.jsf\" tabindex=\"16\">Help</a>\r\n");
           out.write("                      </td>\r\n");
 
 
           out.write("                      <td width=\"7\" valign=\"bottom\"></td>\r\n");
           out.write("                    </tr>\r\n");
           out.write("                  </table>\r\n");
 
 
 
 
       out.println("          <!-- end Global Navigation -->");
       out.println("");
       out.println("        </div> <!-- search-globalnav -->");
       out.println("      </div> <!-- bannerarea -->");
       out.println("");
       out.println("      <!-- end Thesaurus, banner search area -->");
       out.println("      <!-- Quick links bar -->");
       out.println("");
       out.println("<div class=\"bluebar\">");
       out.println("  <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
       out.println("  <tr>");
       out.println("    <td><div class=\"quicklink-status\">&nbsp;</div></td>");
       out.println("    <td>");
       out.println("");
 
       /*
       out.println("  <div id=\"quicklinksholder\">");
       out.println("      <ul id=\"quicklinks\"");
       out.println("        onmouseover=\"document.quicklinksimg.src='/ncitbrowser/images/quicklinks-active.gif';\"");
       out.println("        onmouseout=\"document.quicklinksimg.src='/ncitbrowser/images/quicklinks-inactive.gif';\">");
       out.println("        <li>");
       out.println("          <a href=\"#\" tabindex=\"-1\"><img src=\"/ncitbrowser/images/quicklinks-inactive.gif\" width=\"162\"");
       out.println("            height=\"18\" border=\"0\" name=\"quicklinksimg\" alt=\"Quick Links\" />");
       out.println("          </a>");
       out.println("          <ul>");
       out.println("            <li><a href=\"http://evs.nci.nih.gov/\" tabindex=\"-1\" target=\"_blank\"");
       out.println("              alt=\"Enterprise Vocabulary Services\">EVS Home</a></li>");
       out.println("            <li><a href=\"http://localhost/ncimbrowserncimbrowser\" tabindex=\"-1\" target=\"_blank\"");
       out.println("              alt=\"NCI Metathesaurus\">NCI Metathesaurus Browser</a></li>");
       out.println("");
       out.println("            <li><a href=\"/ncitbrowser/start.jsf\" tabindex=\"-1\"");
       out.println("              alt=\"NCI Term Browser\">NCI Term Browser</a></li>");
       out.println("            <li><a href=\"http://www.cancer.gov/cancertopics/terminologyresources\" tabindex=\"-1\" target=\"_blank\"");
       out.println("              alt=\"NCI Terminology Resources\">NCI Terminology Resources</a></li>");
       out.println("");
       out.println("              <li><a href=\"http://ncitermform.nci.nih.gov/ncitermform/?dictionary=NCI%20Thesaurus\" tabindex=\"-1\" target=\"_blank\" alt=\"Term Suggestion\">Term Suggestion</a></li>");
       out.println("");
       out.println("");
       out.println("          </ul>");
       out.println("        </li>");
       out.println("      </ul>");
       out.println("  </div>");
       */
       addQuickLink(request, out);
 
 
       out.println("");
       out.println("      </td>");
       out.println("    </tr>");
       out.println("  </table>");
       out.println("");
       out.println("</div>");
 
       if (! ServerMonitorThread.getInstance().isLexEVSRunning()) {
       out.println("    <div class=\"redbar\">");
       out.println("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
       out.println("        <tr>");
       out.println("          <td class=\"lexevs-status\">");
       out.println("            " + ServerMonitorThread.getInstance().getMessage());
       out.println("          </td>");
       out.println("        </tr>");
       out.println("      </table>");
       out.println("    </div>");
       }
 
       out.println("      <!-- end Quick links bar -->");
       out.println("");
       out.println("      <!-- Page content -->");
       out.println("      <div class=\"pagecontent\">");
       out.println("");
 
 
       if (message != null) {
           out.println("\r\n");
           out.println("      <p class=\"textbodyred\">");
           out.print(message);
           out.println("</p>\r\n");
           out.println("    ");
           request.getSession().removeAttribute("message");
       }
 
 // to be modified
 /*
       out.println("<p class=\"textbody\">");
       out.println("View value sets organized by standards category or source terminology.");
       out.println("Standards categories group the value sets supporting them; all other labels lead to the home pages of actual value sets or source terminologies.");
       out.println("Search or browse a value set from its home page, or search all value sets at once from this page (very slow) to find which ones contain a particular code or term.");
       out.println("</p>");
 */
       out.println("");
       out.println("        <div id=\"popupContentArea\">");
       out.println("          <a name=\"evs-content\" id=\"evs-content\"></a>");
       out.println("");
       out.println("          <table width=\"580px\" cellpadding=\"3\" cellspacing=\"0\" border=\"0\">");
       out.println("");
       out.println("");
       out.println("");
       out.println("");
       out.println("            <tr class=\"textbody\">");
       out.println("              <td class=\"textbody\" align=\"left\">");
       out.println("");
 
 /*
 if (view == Constants.STANDARD_VIEW) {
       out.println("                Standards View");
       out.println("                &nbsp;|");
       out.println("                <a href=\"" + contextPath + "/ajax?action=create_cs_vs_tree\">Terminology View</a>");
 } else {
       out.println("                <a href=\"" + contextPath + "/ajax?action=create_src_vs_tree\">Standards View</a>");
       out.println("                &nbsp;|");
       out.println("                Terminology View");
 }
 */
 
 
       out.println("              </td>");
       out.println("");
       out.println("              <td align=\"right\">");
       out.println("               <font size=\"1\" color=\"red\" align=\"right\">");
       out.println("                 <a href=\"javascript:printPage()\"><img src=\"/ncitbrowser/images/printer.bmp\" border=\"0\" alt=\"Send to Printer\"><i>Send to Printer</i></a>");
       out.println("               </font>");
 
 
       out.println("              </td>");
       out.println("            </tr>");
       out.println("          </table>");
       out.println("");
       out.println("          <hr/>");
       out.println("");
       out.println("");
       out.println("");
       out.println("<style>");
       out.println("#expandcontractdiv {border:1px solid #336600; background-color:#FFFFCC; margin:0 0 .5em 0; padding:0.2em;}");
       out.println("#treecontainer { background: #fff }");
       out.println("</style>");
       out.println("");
       out.println("");
       out.println("<div id=\"expandcontractdiv\">");
       out.println("	<a id=\"expand_all\" href=\"#\">Expand all</a>");
       out.println("	<a id=\"collapse_all\" href=\"#\">Collapse all</a>");
       out.println("	<a id=\"check_all\" href=\"#\">Check all</a>");
       out.println("	<a id=\"uncheck_all\" href=\"#\">Uncheck all</a>");
       out.println("</div>");
       out.println("");
       out.println("");
       out.println("");
       out.println("          <!-- Tree content -->");
       out.println("");
       out.println("          <div id=\"treecontainer\" class=\"ygtv-checkbox\"></div>");
       out.println("");
       out.println("          <form id=\"pg_form\">");
       out.println("");
       out.println("            <input type=\"hidden\" id=\"ontology_node_id\" name=\"ontology_node_id\" value=\"null\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_display_name\" name=\"ontology_display_name\" value=\"" + dictionary + "\" />");
       out.println("            <input type=\"hidden\" id=\"schema\" name=\"schema\" value=\"" + dictionary + "\" />");
       out.println("            <input type=\"hidden\" id=\"ontology_version\" name=\"ontology_version\" value=\"" + version + "\" />");
       out.println("            <input type=\"hidden\" id=\"view\" name=\"view\" value=\"source\" />");
       out.println("          </form>");
       out.println("");
 
 
 
       out.println("");
       out.println("        </div> <!-- popupContentArea -->");
       out.println("");
       out.println("");
       out.println("<div class=\"textbody\">");
       out.println("<!-- footer -->");
       out.println("<div class=\"footer\" style=\"width:720px\">");
       out.println("  <ul>");
       out.println("    <li><a href=\"http://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\">NCI Home</a> |</li>");
       out.println("    <li><a href=\"/ncitbrowser/pages/contact_us.jsf\">Contact Us</a> |</li>");
       out.println("    <li><a href=\"http://www.cancer.gov/policies\" target=\"_blank\" alt=\"National Cancer Institute Policies\">Policies</a> |</li>");
       out.println("    <li><a href=\"http://www.cancer.gov/policies/page3\" target=\"_blank\" alt=\"National Cancer Institute Accessibility\">Accessibility</a> |</li>");
       out.println("    <li><a href=\"http://www.cancer.gov/policies/page6\" target=\"_blank\" alt=\"National Cancer Institute FOIA\">FOIA</a></li>");
       out.println("  </ul>");
       out.println("  <p>");
       out.println("    A Service of the National Cancer Institute<br />");
       out.println("    <img src=\"/ncitbrowser/images/external-footer-logos.gif\"");
       out.println("      alt=\"External Footer Logos\" width=\"238\" height=\"34\" border=\"0\"");
       out.println("      usemap=\"#external-footer\" />");
       out.println("  </p>");
       out.println("  <map id=\"external-footer\" name=\"external-footer\">");
       out.println("    <area shape=\"rect\" coords=\"0,0,46,34\"");
       out.println("      href=\"http://www.cancer.gov\" target=\"_blank\"");
       out.println("      alt=\"National Cancer Institute\" />");
       out.println("    <area shape=\"rect\" coords=\"55,1,99,32\"");
       out.println("      href=\"http://www.hhs.gov/\" target=\"_blank\"");
       out.println("      alt=\"U.S. Health &amp; Human Services\" />");
       out.println("    <area shape=\"rect\" coords=\"103,1,147,31\"");
       out.println("      href=\"http://www.nih.gov/\" target=\"_blank\"");
       out.println("      alt=\"National Institutes of Health\" />");
       out.println("    <area shape=\"rect\" coords=\"148,1,235,33\"");
       out.println("      href=\"http://www.usa.gov/\" target=\"_blank\"");
       out.println("      alt=\"USA.gov\" />");
       out.println("  </map>");
       out.println("</div>");
       out.println("<!-- end footer -->");
       out.println("</div>");
       out.println("");
       out.println("");
       out.println("      </div> <!-- pagecontent -->");
       out.println("    </div> <!--  main-area -->");
       out.println("    <div class=\"mainbox-bottom\"><img src=\"/ncitbrowser/images/mainbox-bottom.gif\" width=\"745\" height=\"5\" alt=\"Mainbox Bottom\" /></div>");
       out.println("");
       out.println("  </div> <!-- center-page -->");
       out.println("");
       out.println("</body>");
       out.println("</html>");
       out.println("");
   }
 
 
    public static void addHiddenForm(PrintWriter out, String checkedNodes, String partialCheckedNodes) {
       out.println("   <form id=\"hidden_form\">");
       out.println("      <input type=\"hidden\" id=\"checkedNodes\" name=\"checkedNodes\" value=\"" + checkedNodes + "\" />");
       out.println("      <input type=\"hidden\" id=\"partialCheckedNodes\" name=\"partialCheckedNodes\" value=\"" + partialCheckedNodes + "\" />");
       out.println("   </form>");
    }
 
    public static void writeInitialize(PrintWriter out) {
       out.println("   function initialize() {");
       out.println("	     tree = new YAHOO.widget.TreeView(\"treecontainer\");");
       out.println("	     initializeNodeCheckState();");
       out.println("	     tree.expandAll();");
       out.println("	     tree.draw();");
       out.println("   }");
    }
 
 
    public static void initializeNodeCheckState(PrintWriter out) {
       out.println("   function initializeNodeCheckState(nodes) {");
       out.println("       nodes = nodes || tree.getRoot().children;");
       out.println("       var checkedNodes = document.forms[\"hidden_form\"].checkedNodes.value;");
       out.println("       var partialCheckedNodes = document.forms[\"hidden_form\"].partialCheckedNodes.value;");
       out.println("       for(var i=0, l=nodes.length; i<l; i=i+1) {");
       out.println("            var n = nodes[i];");
       out.println("");
       out.println("            if (checkedNodes.indexOf(n.label) != -1) {");
       out.println("                n.setCheckState(2);");
       out.println("            } else if (partialCheckedNodes.indexOf(n.label) != -1) {");
       out.println("                n.setCheckState(1);");
       out.println("            }");
       out.println("");
       out.println("            if (n.hasChildren()) {");
       out.println("		        initializeNodeCheckState(n.children);");
       out.println("            }");
       out.println("        }");
       out.println("   }");
    }
 
 
     public static void addQuickLink(HttpServletRequest request, PrintWriter out) {
 
 		String basePath = request.getContextPath();
 		String ncim_url = new DataUtils().getNCImURL();
 		String quicklink_dictionary = (String) request.getSession().getAttribute("dictionary");
 		quicklink_dictionary = DataUtils.getFormalName(quicklink_dictionary);
 		String term_suggestion_application_url2 = "";
 		String dictionary_encoded2 = "";
 		if (quicklink_dictionary != null) {
 			term_suggestion_application_url2 = DataUtils.getMetadataValue(quicklink_dictionary, "term_suggestion_application_url");
 			dictionary_encoded2 = DataUtils.replaceAll(quicklink_dictionary, " ", "%20");
 		}
 
 
               out.write("  <div id=\"quicklinksholder\">\r\n");
               out.write("      <ul id=\"quicklinks\"\r\n");
               out.write("        onmouseover=\"document.quicklinksimg.src='");
               out.print(basePath);
               out.write("/images/quicklinks-active.gif';\"\r\n");
               out.write("        onmouseout=\"document.quicklinksimg.src='");
               out.print(basePath);
               out.write("/images/quicklinks-inactive.gif';\">\r\n");
               out.write("        <li>\r\n");
               out.write("          <a href=\"#\" tabindex=\"-1\"><img src=\"");
               out.print(basePath);
               out.write("/images/quicklinks-inactive.gif\" width=\"162\"\r\n");
               out.write("            height=\"18\" border=\"0\" name=\"quicklinksimg\" alt=\"Quick Links\" />\r\n");
               out.write("          </a>\r\n");
               out.write("          <ul>\r\n");
               out.write("            <li><a href=\"http://evs.nci.nih.gov/\" tabindex=\"-1\" target=\"_blank\"\r\n");
               out.write("              alt=\"Enterprise Vocabulary Services\">EVS Home</a></li>\r\n");
               out.write("            <li><a href=\"");
               out.print(ncim_url);
               out.write("\" tabindex=\"-1\" target=\"_blank\"\r\n");
               out.write("              alt=\"NCI Metathesaurus\">NCI Metathesaurus Browser</a></li>\r\n");
               out.write("\r\n");
               out.write("            ");
 
             if (quicklink_dictionary == null || quicklink_dictionary.compareTo("NCI Thesaurus") != 0) {
 
               out.write("\r\n");
               out.write("\r\n");
               out.write("            <li><a href=\"");
               out.print( request.getContextPath() );
               out.write("/index.jsp\" tabindex=\"-1\"\r\n");
               out.write("              alt=\"NCI Thesaurus Browser\">NCI Thesaurus Browser</a></li>\r\n");
               out.write("\r\n");
               out.write("            ");
 
             }
 
               out.write("\r\n");
               out.write("\r\n");
               out.write("            <li>\r\n");
               out.write("              <a href=\"");
               out.print( request.getContextPath() );
               out.write("/termbrowser.jsf\" tabindex=\"-1\" alt=\"NCI Term Browser\">NCI Term Browser</a>\r\n");
               out.write("            </li>\r\n");
               out.write("              \r\n");
               out.write("            <li><a href=\"http://www.cancer.gov/cancertopics/terminologyresources\" tabindex=\"-1\" target=\"_blank\"\r\n");
               out.write("              alt=\"NCI Terminology Resources\">NCI Terminology Resources</a></li>\r\n");
               out.write("            ");
  if (term_suggestion_application_url2 != null && term_suggestion_application_url2.length() > 0) {
               out.write("\r\n");
               out.write("              <li><a href=\"");
               out.print(term_suggestion_application_url2);
               out.write("?dictionary=");
               out.print(dictionary_encoded2);
               out.write("\" tabindex=\"-1\" target=\"_blank\" alt=\"Term Suggestion\">Term Suggestion</a></li>\r\n");
               out.write("            ");
  }
               out.write("\r\n");
               out.write("\r\n");
               out.write("          </ul>\r\n");
               out.write("        </li>\r\n");
               out.write("      </ul>\r\n");
               out.write("  </div>\r\n");
 
 	  }
 
 
 
 
 }
