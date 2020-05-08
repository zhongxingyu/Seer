 /* 
  * Project       : Bachelor Thesis - SudokuPortlet game implementation as portlet
  * Document      : SudokuPortlet.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.portlet.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.gatein.portal.examples.games.sudoku.util.CustomErrorHandler;
 import org.w3c.dom.*;
 import org.xml.sax.SAXParseException;
 
 /**
  * The Sudoku Portlet Class implements view, edit and help modes of portlet.
  * The content of the portlet in the view mode contains an JavaScript application
  * for playing of games and statistics.
  * The content of the portlet in the edit mode allows to change a skin of the
  * game. Services may be also managed from this mode if an admin if logged in.
  * The content of the portlet in the help mode contains a link to an end-user
  * documentation.
  * 
  * @author Ondřej Fibich
  * @version 1.0
  */
 public class SudokuPortlet extends GenericPortlet
 {
     /**
      * Path to a document which contains a skin definition
      */
     private static final String skinConfPath = "/WEB-INF/sudoku-portlet-skins.xml";
     
     /**
      * Logger
      */
     private static final Logger logger = Logger.getLogger(SudokuPortlet.class.getName());
     
     /**
      * CSS definitions for skins
      */
     private static final String[][] SKIN_CSS_DEFINITIONS = {
        { "td.sudoku-game_board-field-package input, "
        + "table.sudoku-game_input-hint td span", "color" },
        { "td.sudoku-game_board-field-package, .sudoku-game_board, "
        + "table.sudoku-game_input-hint td span, "
        + ".sudoku-game_input-hint-close", "border-color" },
        { "td.sudoku-game_board-field-package, "
        + "td.sudoku-game_board-field-package input", "background-color" },
        { "td.sudoku-game_readonly-input, "
        + "td.sudoku-game_readonly-input input", "background-color"},
        { "td.sudoku-game_board-field-package input", "font-family" }
     };
     
     /**
      * A map of predefined skins loaded during initialization
      */
     private Map<String, Map<String, String>> skins;
     
     /**
      * Name of a default skin name which correspondes to one in the skins Map
      */
     private String defaultSkinName;
     
     /**
      * Enables/disables remote publishers
      */
     private boolean remotePublishersEnabled = true;
     
     /**
      * @see GenericPortlet#init(javax.portlet.PortletConfig) 
      * @throws PortletException 
      */
     @Override
     public void init(PortletConfig config) throws PortletException
     {
         super.init(config);
 
         // enable/disable remote publisher
         final String rpe = config.getInitParameter("remotePublishersEnabled");
         remotePublishersEnabled = Boolean.valueOf(rpe);
         
         // init skins
         skins = new LinkedHashMap<String, Map<String, String>>();
         defaultSkinName = null;
         
         try
         {
             loadSkins();
         }
         catch (SAXParseException npex)
         {
             final String m = "Wrong sytax of portlet skins document, "
                            + "check DTD for more details";
             
             throw new PortletException(m, npex);
         }
         catch (Exception ex)
         {
             throw new PortletException("Can not load portlet skins", ex);
         }
     }
     
     
     /**
      * @see GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse) 
      */
     @Override
     public void doView(RenderRequest request, RenderResponse response)
             throws PortletException, IOException
     {
         final String prevDolId = request.getPreferences().getValue("game-lastPlayedSolutionId", null);
         
         request.setAttribute("lastPlayedSolutionId", prevDolId);
         request.setAttribute("remotePublisherEnabled", remotePublishersEnabled);
         response.setContentType("text/html");
         
         PortletRequestDispatcher dispatcher =
                 getPortletContext().getRequestDispatcher("/WEB-INF/jsp/view.jsp");
         dispatcher.include(request, response);
     }
 
     /**
      * @see GenericPortlet#doEdit(javax.portlet.RenderRequest, javax.portlet.RenderResponse) 
      */
     @Override
     public void doEdit(RenderRequest request, RenderResponse response)
             throws PortletException, IOException
     {
         final String skinName = getCurrentSkinNameOfUser(request.getPreferences());
         final Map<String, String> skin = getCurrentSkinOfUser(request.getPreferences());
         
         request.setAttribute("skinsMap", skins);
         request.setAttribute("skinNames", skins.keySet());
         request.setAttribute("defaultSkinName", defaultSkinName);
         request.setAttribute("currentSkin", skin);
         request.setAttribute("currentSkinName", skinName);
         request.setAttribute("remotePublisherEnabled", remotePublishersEnabled);
         request.setAttribute("isAdmin", request.isUserInRole("administrator"));
         response.setContentType("text/html");
         
         PortletRequestDispatcher dispatcher =
                 getPortletContext().getRequestDispatcher("/WEB-INF/jsp/edit.jsp");
         dispatcher.include(request, response);
     }
 
     /**
      * @see GenericPortlet#doHelp(javax.portlet.RenderRequest, javax.portlet.RenderResponse) 
      */
     @Override
     public void doHelp(RenderRequest request, RenderResponse response)
             throws PortletException, IOException
     {
         response.setContentType("text/html");
         PortletRequestDispatcher dispatcher =
                 getPortletContext().getRequestDispatcher("/WEB-INF/jsp/help.jsp");
         dispatcher.include(request, response);
     }
     
     /**
      * Changes skin of a game for the current logged user.
      * 
      * @param request
      * @param response 
      */
     @ProcessAction(name="changeSkin")
     public void processChangeSkin(ActionRequest request, ActionResponse response)
             throws PortletException, PortletSecurityException, IOException
     {
         PortletPreferences p = request.getPreferences();
         
         try
         {
             p.setValue("skin-fontColor", request.getParameter("board-font-color"));
             p.setValue("skin-borderColor", request.getParameter("board-border-color"));
             p.setValue("skin-background", request.getParameter("field-bg-color"));
             p.setValue("skin-fixedBackground", request.getParameter("field-bg-fixed-color"));
             p.setValue("skin-font", request.getParameter("field-font"));
             p.store();
         }
         catch (ReadOnlyException ex)
         {
             logger.log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * @see GenericPortlet#doHeaders(javax.portlet.RenderRequest, javax.portlet.RenderResponse) 
      */
     @Override
     public void doHeaders(RenderRequest request, RenderResponse response)
     {
         final String path = request.getContextPath();
         
         Element link = response.createElement("link");
         link.setAttribute("type", "text/css");
         link.setAttribute("rel", "stylesheet");
         link.setAttribute("href", path + "/css/all-packed.css");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, link);
         
         link = response.createElement("style");
         link.setAttribute("type", "text/css");
         link.setTextContent(buildSkinStyleTagContent(request.getPreferences()));
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, link);
 
         Element script = response.createElement("script");
         script.setAttribute("type", "text/javascript");
         script.setAttribute("src", path + "/js/all-packed.js");
         response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, script);
     }
     
     /**
      * Loads predefined skins
      * 
      * @throws SAXParseException    On invalid document syntax
      * @throws Exception            On other errors
      */
     private void loadSkins() throws SAXParseException, Exception
     {
         final String skinDocPath = getPortletContext().getRealPath(skinConfPath);
         
         // load a factory
         DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
         fac.setValidating(true);
 
         // load a builder
         DocumentBuilder builder = fac.newDocumentBuilder();
         builder.setErrorHandler(new CustomErrorHandler(logger));
 
         // load a document
         Document doc = builder.parse(skinDocPath);
         NodeList skinNodes = doc.getElementsByTagName("skin");
 
         // load each skin tag
         for (int i = 0; i < skinNodes.getLength(); i++)
         {
             Map<String, String> skin = new LinkedHashMap<String, String>();
             Node skinNode = skinNodes.item(i);
             NamedNodeMap attrsSkin = skinNode.getAttributes();
             String name = attrsSkin.getNamedItem("name").getTextContent();
             NodeList childrensNodes = skinNode.getChildNodes();
 
             for (int u = 0; u < childrensNodes.getLength(); u++)
             {
                 Node childNode = childrensNodes.item(u);
                 NamedNodeMap attrsChild = childNode.getAttributes();
 
                 if (childNode.getNodeName().equals("game-board"))
                 {
                     Node fontColorNode = attrsChild.getNamedItem("font-color");
                     Node borderColorNode = attrsChild.getNamedItem("border-color");
 
                     skin.put("fontColor", fontColorNode.getTextContent());
                     skin.put("borderColor", borderColorNode.getTextContent());
 
                     childrensNodes = childNode.getChildNodes();
 
                     for (int w = 0; w < childrensNodes.getLength(); w++)
                     {
                         childNode = childrensNodes.item(w);
                         attrsChild = childNode.getAttributes();
 
                         if (childNode.getNodeName().equals("field"))
                         {
                             Node bgNode = attrsChild.getNamedItem("background");
                             Node fixBgNode = attrsChild.getNamedItem("fixed-background");
                             Node fontNode = attrsChild.getNamedItem("font");
 
                             skin.put("background", bgNode.getTextContent());
                             skin.put("fixedBackground", fixBgNode.getTextContent());
                             skin.put("font", fontNode.getTextContent());
 
                             break;
                         }
                     }
 
                     break; 
                 }
             }
 
             // undefined or empty name of a skin
             if (name == null || name.isEmpty())
             {
                 name = "Unknown";
                 logger.log(Level.WARNING, "Empty skin name");
             }
 
             int counter = 1;
             String newName = name;
 
             while (skins.containsKey(newName))
             {
                 newName = name + " " + (++counter);
             }
 
             name = newName;
 
             skins.put(name, skin);
 
             Node defaultAttr = attrsSkin.getNamedItem("default");
 
             if (defaultAttr != null && defaultAttr.getTextContent().equals("true"))
             {
                 defaultSkinName = name;
             }
         }
 
         if (defaultSkinName == null)
         {
             logger.log(Level.WARNING, "Undefined default skin");
 
             // pick a random skin if there is any 
             if (skins.size() > 0)
             {
                 defaultSkinName = skins.keySet().iterator().next();
             }
             else
             {
                 throw new Exception("no skin defined");
             }
         }
     }
     
     /**
      * Gets name of current skin of a user
      * 
      * @param preferences       Preferences of a user
      * @return                  A name of the current skin of null for a custom skin
      */
     private String getCurrentSkinNameOfUser(PortletPreferences preferences)
     {
         if (!skins.isEmpty())
         {
             for (String skinName : skins.keySet())
             {
                 Map<String, String> skin = skins.get(skinName);
                 boolean equals = true;
                 
                 for (String key : skin.keySet())
                 {
                     final String pVal = preferences.getValue("skin-" + key, null);
                     
                     if (pVal == null)
                     {
                         return defaultSkinName;
                     }
                     
                     if (!skin.get(key).equals(pVal))
                     {
                         equals = false;
                         break;
                     }
                 }
                 
                 if (equals)
                 {
                     return skinName;
                 }
             }
         }
         
         return null;
     }
     
     /**
      * Gets the current skin of a user
      * 
      * @param preferences       Preferences of a user
      * @return                  The current skin
      */
     private Map<String, String> getCurrentSkinOfUser(PortletPreferences preferences)
     {
         String defName = getCurrentSkinNameOfUser(preferences);
         Map<String, String> ret;
         
         if (defName != null)
         {
             ret = skins.get(defName);
         }
         else
         {
             ret = new LinkedHashMap<String, String>();
             ret.put("fontColor", preferences.getValue("skin-fontColor", null));
             ret.put("borderColor", preferences.getValue("skin-borderColor", null));
             ret.put("background", preferences.getValue("skin-background", null));
             ret.put("fixedBackground", preferences.getValue("skin-fixedBackground", null));
             ret.put("font", preferences.getValue("skin-font", null));
         }
         
         return ret;
     }
     
     /**
      * Builds a skin of the portlet by prefrences or default skin
      * 
      * @param preferences       Preferences of a user
      * @return                  Builded content of a style element
      */
     private String buildSkinStyleTagContent(PortletPreferences preferences)
     {
         StringBuilder buffer = new StringBuilder();
         Map<String, String> skin = getCurrentSkinOfUser(preferences);
         int i = 0;
         
         for (String key : skin.keySet())
         {
             String value = preferences.getValue("skin-" + key, skin.get(key));
 
             buffer.append(SKIN_CSS_DEFINITIONS[i][0]).append(" { ")
                 .append(SKIN_CSS_DEFINITIONS[i][1]).append(": ");
 
             if (!key.equals("font"))
             {
                 buffer.append("#");
             }
 
            buffer.append(value).append(" ! important } ");
 
             i++;
         }
         
         return buffer.toString();
     }
     
 }
