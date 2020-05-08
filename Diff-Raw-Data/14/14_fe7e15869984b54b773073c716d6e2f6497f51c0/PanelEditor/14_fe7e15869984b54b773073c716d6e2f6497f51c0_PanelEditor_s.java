 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Aye Thu
  */
 
 /**
 * $Id: PanelEditor.java,v 1.5 2004-03-15 05:12:01 aye.thu Exp $
  */
 
 package com.netspective.sparx.panel.editor;
 
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.value.source.RedirectValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.Project;
 import com.netspective.sparx.command.PanelEditorCommand;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.panel.AbstractPanel;
 import com.netspective.sparx.panel.BasicHtmlPanelValueContext;
 import com.netspective.sparx.panel.HtmlPanelAction;
 import com.netspective.sparx.panel.HtmlPanelActionStates;
 import com.netspective.sparx.panel.HtmlPanelActions;
 import com.netspective.sparx.panel.HtmlPanelBanner;
 import com.netspective.sparx.panel.HtmlPanelFrame;
 import com.netspective.sparx.panel.HtmlPanelSkin;
 import com.netspective.sparx.panel.HtmlPanelValueContext;
 import com.netspective.sparx.theme.Theme;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Base class for custom panel editors. This class is meant to be EXTENDED to create new panel editor types. This does not
  * define a complete panel editor behavior since the content editing will be up to the child class to implement. The
  * constructor's of this class are all protected and only meant to be instantiated by the PanelEditorsPackage.
  *
  */
 public class PanelEditor extends AbstractPanel
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     public static final String PANEL_RECORD_EDIT_ACTION     = "Edit";
     public static final String PANEL_CONTENT_ADD_ACTION     = "Add";
     public static final String PANEL_CONTENT_MANAGE_ACTION  = "Manage";
     public static final String PANEL_RECORD_DONE_ACTION     = "Done";
     public static final String PANEL_CONTENT_DELETE_ACTION  = "Delete";
 
     // the following are all the possible displaymodes that the editor panel can be in
     public static final int UNKNOWN_MODE        = -1;
     public static final int MODE_DISPLAY        = 1;    /* default display report mode */
     public static final int MODE_EDIT           = 2;    /* editing panel content mode (dialog and current content (e.g. report)) */
     public static final int MODE_DELETE         = 3;    /* deleting panel content mode (dialog and current content) */
     public static final int MODE_ADD            = 4;    /* add content mode (dialog and current content) */
     public static final int MODE_MANAGE         = 5;    /* managing content (report only but different from default) */
 
 
     /* default skin to use to display query report panel */
     public static final String DEFAULT_EDITOR_SKIN = "panel-editor";
     /* default name assigned to the dialog defined in the panel editor */
     public static final String DEFAULT_DIALOG_NAME = "panel-editor-dialog";
     /* a request attribute name used to save the name of the panel editor */
     public static final String PANEL_EDITOR_REQ_ATTRIBUTE_PREFIX = "com.netspective.sparx.panel.editor.PanelEditor";
     /* the display mode is passed to the panel using a request attribute with this name */
     public static final String CURRENT_MODE_CONTEXT_ATTRIBUTE = "panel-editor-mode";
     /* the primary key of the record that is to be edited/deleted is passed to the dialog context using this attribute name */
     public static final String POPULATE_KEY_CONTEXT_ATTRIBUTE = "panel-editor-key";
     /* the previous mode of the panel passed using the request attribute */
     public static final String PREV_MODE_REQ_ATTRIBUTE = "com.netspective.sparx.panel.editor.PanelEditor.previousMode";
 
     protected static final Log log = LogFactory.getLog(PanelEditor.class);
 
     /* associated project */
     protected Project project;
     /* the package that the panel belongs to */
     private PanelEditorsPackage nameSpace;
     /* name of the panel */
     private String name;
     /* flag to indicate if the record actions for this panel have been prepared or not */
     private boolean initialized = false;
     /* indicates what request parameter is required for this panel */
     private String requireRequestParam = null;
     /* child elements for content */
     private Map elements = new HashMap();
 
     public PanelEditor(Project project)
     {
         this.project = project;
     }
 
     /**
      *
      */
     public PanelEditor(Project project, PanelEditorsPackage pkg)
     {
         this(project);
         setNameSpace(pkg);
     }
 
     /**
      *
      * @return
      */
     public String getNameForRequestAttribute()
     {
         return PANEL_EDITOR_REQ_ATTRIBUTE_PREFIX + getName();
     }
 
     /**
      * Create the state object for the panel editor
      *
      * @param nc
      * @return
      */
     public PanelEditorState constructPanelEditorState(NavigationContext nc)
     {
         return new PanelEditorState(getQualifiedName());
     }
 
     /**
      * Creates a child content element
      *
      * @return
      */
     public PanelEditorContentElement createElement()
     {
         return new PanelEditorContentElement();
     }
 
     public PanelEditorContentElement createElement(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         if(PanelEditorContentElement.class.isAssignableFrom(cls))
         {
             return (PanelEditorContentElement) cls.newInstance();
         }
         else
             throw new RuntimeException("Don't know what to do with with class: " + cls);
     }
 
 
     /**
      * Adds a child content element
      *
      * @param element
      */
     public void addElement(PanelEditorContentElement element)
     {
         element.setParent(this);
         elements.put(element.getName(), element);
     }
 
     /**
      * Gets the content elements
      *
      * @return
      */
     public Map getElementsAsMap()
     {
         return elements;
     }
 
     public PanelEditorContentElement getElement(String name)
     {
         return (PanelEditorContentElement) elements.get(name);
     }
 
     /**
      * Gets the content elements
      *
      * @return
      */
     public PanelEditorContentElement[] getElementsAsArray()
     {
         int i = 0;
         PanelEditorContentElement[] list = new PanelEditorContentElement[elements.keySet().size()];
         Iterator itr = elements.keySet().iterator();
         while (itr.hasNext())
         {
             list[i++] = (PanelEditorContentElement) elements.get(itr.next());
         }
         return list;
     }
 
 
     /**
     * Calculate the mode the record editor panel is in and also set the
     *
     * @return  the current mode of the panel editor
     */
    public static int validatePanelEditorMode(String panelMode, String recordKey)
    {
        int mode = translateMode(panelMode);
         // if mode is supposed to be in add, edit, or delete then the recordKey should have been provided.
        if ((mode == PanelEditor.MODE_EDIT ||
            mode == MODE_DELETE) && recordKey == null)
        {
            mode = UNKNOWN_MODE;
        }
        return mode;
     }
 
     /**
      * Translates the mode name into a mode value. This doesn't do any validation checking of the mode.
      *
      * @param modeName
      * @return
      */
     public static int translateMode(String modeName)
     {
         int mode = MODE_DISPLAY;
         if (modeName == null || modeName.equals("display"))
             mode = MODE_DISPLAY;
         else if (modeName.equals("add"))
             mode = MODE_ADD;
         else if (modeName.equals("edit"))
             mode = MODE_EDIT;
         else if (modeName.equals("delete"))
             mode = MODE_DELETE;
         else if (modeName.equals("manage"))
            mode = MODE_MANAGE;
         else
             mode = UNKNOWN_MODE;
 
         return mode;
     }
 
     /**
      * Translates the mode integer to its string version
      * @param mode
      * @return
      */
     public static String translateModeToString(int mode)
     {
         String modeName = null;
         if (mode == MODE_DISPLAY)
             modeName = "display";
         else if (mode == MODE_ADD)
             modeName = "add";
         else if (mode == MODE_EDIT)
             modeName = "edit";
         else if (mode == MODE_DELETE)
             modeName = "delete";
         else if (mode == MODE_MANAGE)
             modeName = "manage";
 
         return modeName;
     }
 
     /**
      * Gets the associated project
      *
      * @return  associated project
      */
     public Project getProject()
     {
         return project;
     }
 
     /**
      * Gets the request parameter that is required for this panel editor
      *
      * @return      request parameter name
      */
     public String getRequireRequestParam()
     {
         return requireRequestParam;
     }
 
     /**
      * Sets the request parameter that is required for this panel editor
      *
      * @param requireRequestParam
      */
     public void setRequireRequestParam(String requireRequestParam)
     {
         this.requireRequestParam = requireRequestParam;
     }
 
     /**
      * Gets the name of the panel
      *
      * @return  name of the panel
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Sets the name of the panel
      *
      * @param name      name of the panel
      */
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getQualifiedName()
     {
         return nameSpace != null ? nameSpace.getNameSpaceId() + "." + name : name;
     }
 
     /**
      * Gets the namespace (package name)
      *
      * @return      namespace
      */
     public PanelEditorsPackage getNameSpace()
     {
         return nameSpace;
     }
 
     /**
      * Sets the namespace (package name)
      *
      * @param nameSpace     package name
      */
     public void setNameSpace(PanelEditorsPackage nameSpace)
     {
         this.nameSpace = nameSpace;
     }
 
     /**
      * Gets the name to use when saving this panel editor in a collection map
      *
      * @return
      */
     public String getNameForMapKey()
     {
         return translateNameForMapKey(getQualifiedName());
     }
 
     /**
      * Translate name of the panel editor to a key name suitable for use in a map
      *
      * @param name  name of the panel editor
      * @return      translated key name
      */
     public static final String translateNameForMapKey(String name)
     {
         return name != null ? name.toUpperCase() : null;
     }
 
     public void setInitialized(boolean initialized)
     {
         this.initialized = initialized;
     }
 
     /**
      * Generates the URL string for the  panel editor's associated actions. This URL is set only once and any dynamic
      * information needs to be passed using value sources.
      *
      * @param actionMode    the mode  for which the URL is being calculated
      * @return              url string (containing context sensitive elements) used to construct a redirect value source
      */
     public String generatePanelActionUrl(int actionMode)
     {
         String url = "active-page:";
         if (actionMode == PanelEditor.MODE_EDIT)
         {
             url = url + PanelEditorCommand.PANEL_EDITOR_COMMAND_REQUEST_PARAM_NAME + "=" + getQualifiedName() +
                     ",edit,${request-attr:" + PREV_MODE_REQ_ATTRIBUTE + "}";
         }
         else if (actionMode == MODE_DELETE)
         {
             url = url + PanelEditorCommand.PANEL_EDITOR_COMMAND_REQUEST_PARAM_NAME + "=" + this.getQualifiedName() +
                     ",delete,${request-attr:" + PREV_MODE_REQ_ATTRIBUTE + "}";
         }
         else if (actionMode == MODE_ADD)
         {
             url = url + PanelEditorCommand.PANEL_EDITOR_COMMAND_REQUEST_PARAM_NAME + "=" + this.getQualifiedName() +
                     ",add,${request-attr:" + PREV_MODE_REQ_ATTRIBUTE + "}";
         }
         else if (actionMode == MODE_MANAGE)
             url = url + PanelEditorCommand.PANEL_EDITOR_COMMAND_REQUEST_PARAM_NAME + "=" + this.getQualifiedName() +
                     ",manage,${request-attr:" + PREV_MODE_REQ_ATTRIBUTE + "}";
 
         return url;
     }
 
     public static String calculateNextModeUrl(DialogContext dc, String panelName, int panelMode, int previousMode,
                                          String panelRecordKey)
     {
         String url = "active-page:";
         String currentUrl = dc.getNavigationContext().getActivePage().getUrl(dc);
         if (previousMode != MODE_DISPLAY)
             url = url + PanelEditorCommand.PANEL_EDITOR_COMMAND_REQUEST_PARAM_NAME + "=" + panelName + ",manage";
         else
             url = currentUrl;
 
         String urlStr = ValueSources.getInstance().getValueSourceOrStatic(url).getTextValue(dc);
         return urlStr;
     }
 
     /**
      * Creates all the panel actions for the  panel editor. This method SHOULD only be called once to populate the
      * panel editor.
      *
      */
     public void initialize()
     {
         createPanelBannerActions();
         createPanelFrameActions();
         initialized = true;
     }
 
 
     /**
      *
      *
      * @param writer
      * @param dc
      * @param theme
      * @param flags
      * @throws IOException
      */
     public void render(Writer writer, DialogContext dc, Theme theme, int flags) throws IOException
     {
 
     }
 
     /**
      *
      *
      * @param writer
      * @param nc
      * @param theme
      * @param flags
      * @throws IOException
      */
     public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
     {
         PanelEditorState state = (PanelEditorState) nc.getAttribute("");
         if (state != null)
             render(writer, nc, state);
 
     }
 
     /**
      * Renders the panel editor. When the panel editor is in ADD, EDIT, or DELETE modes, it has an active element
      * and the display of the editor is broken into two columns. The left column is for display of the
      * active element's "action" item while the right column contains all the elements in their inactive display
      * mode.
      *
      * @param writer    writer to render the output to
      * @param nc        current navigation context
      * @throws IOException
      */
     public void render(Writer writer, NavigationContext nc, PanelEditorState state) throws IOException
     {
         if (getRequireRequestParam() != null)
         {
             if (nc.getHttpRequest().getParameter(getRequireRequestParam()) == null)
                 throw new RuntimeException("Record editor panel '" + getQualifiedName() + "' requires the request " +
                         "parameter '" + getRequireRequestParam() + "'.");
         }
         if (!isInitialized())
             initialize();
         int mode = state.getCurrentMode();
         BasicHtmlPanelValueContext pvc = new BasicHtmlPanelValueContext(nc, this);
 
         // only when the mode is in DISPLAY mode, the panel editor is displayed
         HtmlPanelSkin skin = null;
         skin = nc.getActiveTheme().getTemplateSkin("panel-editor-compressed");
         /*
         if (mode != PanelEditor.MODE_DISPLAY && mode != PanelEditor.MODE_MANAGE)
         {
             skin = nc.getActiveTheme().getTemplateSkin("panel-editor-compressed");
         }
         else
         {
             skin = nc.getActiveTheme().getTemplateSkin("panel-editor-full");
         }
         */
 
         nc.getHttpRequest().setAttribute(PREV_MODE_REQ_ATTRIBUTE, translateModeToString(state.getCurrentMode()));
 
         String activeElement = state.getActiveElement();
         StringWriter activeEditorWriter = new StringWriter();
         StringWriter activeDisplayWriter = new StringWriter();
         StringWriter inactiveWriter = new StringWriter();
         PanelEditorContentElement[] elements = getElementsAsArray();
         for (int i=0; i < elements.length; i++)
         {
             if (activeElement != null && elements[i].getName().equals(activeElement))
             {
                 elements[i].renderEditorContent(activeEditorWriter, nc, state);
                 elements[i].renderDisplayContent(activeDisplayWriter, nc, state);
             }
             else
             {
                 elements[i].renderDisplayContent(inactiveWriter, nc, state);
             }
         }
         preparePanelActionStates(nc, pvc, state, mode);

         if (activeElement != null)
            writer.write("<div class=\"panel-editor-active-content\">" + activeEditorWriter.getBuffer().toString() + "</div>");
 
        writer.write("<div class=\"panel-editor-display-content\">");
         skin.renderPanelRegistration(writer, pvc);
         skin.renderFrameBegin(writer, pvc);
         writer.write((activeElement != null ? activeDisplayWriter.getBuffer().toString() : "") +
                 inactiveWriter.getBuffer().toString());
         skin.renderFrameEnd(writer, pvc);
        writer.write("</div><p/>");
     }
 
     /**
      * Calculate and process the state of the all the panel actions based on current context
      *
      * @param nc                current navigation context
      * @param vc                current report panel context
      * @param mode              panel mode
      */
     public void preparePanelActionStates(NavigationContext nc, HtmlPanelValueContext vc, PanelEditorState state, int mode)
     {
         HtmlPanelActionStates actionStates = vc.getPanelActionStates();
         if (mode == MODE_DISPLAY)
         {
             actionStates.getState("Done").getStateFlags().setFlag(HtmlPanelAction.Flags.HIDDEN);
             PanelEditorContentElement[] elements = getElementsAsArray();
             String caption = null;
             for (int i=0; i < elements.length; i++)
             {
                 caption = elements[i].getCaption();
                 PanelEditorContentElement.PanelEditorContentState elementState = state.getElementState(elements[i].getName());
                 // hide the ADD action only if there is content in the content element
                 if (!elementState.isEmptyContent())
                     actionStates.getState("Add " + (caption != null ? caption : "")).getStateFlags().setFlag(HtmlPanelAction.Flags.HIDDEN);
             }
         }
         else if (mode == MODE_ADD || mode == MODE_EDIT || mode == MODE_DELETE)
         {
             actionStates.getState(PANEL_CONTENT_MANAGE_ACTION).getStateFlags().setFlag(HtmlPanelAction.Flags.HIDDEN);
         }
         else if (mode == MODE_MANAGE)
         {
             actionStates.getState(PANEL_CONTENT_MANAGE_ACTION).getStateFlags().setFlag(HtmlPanelAction.Flags.HIDDEN);
         }
 
     }
 
     /**
      * Creates the frame actions for the panel editor. They are MANAGE, DONE.
      *
      */
     public void createPanelFrameActions()
     {
         HtmlPanelFrame frame = getFrame();
         if (frame == null)
         {
             setFrame(createFrame());
         }
         HtmlPanelActions actions = new HtmlPanelActions();
         HtmlPanelAction manageAction = frame.createAction();
 
         String manageUrl = generatePanelActionUrl(PanelEditor.MODE_MANAGE);
         manageAction.setCaption(new StaticValueSource("Manage"));
         manageAction.setRedirect(new RedirectValueSource(manageUrl));
         actions.add(manageAction);
 
         String doneUrl = generatePanelActionUrl(PanelEditor.MODE_DISPLAY);
         HtmlPanelAction doneAction = frame.createAction();
         doneAction.setCaption(new StaticValueSource("Done"));
         doneAction.setRedirect(new RedirectValueSource(doneUrl));
         actions.add(doneAction);
         frame.setActions(actions);
     }
 
     public void createPanelBannerActions()
     {
         // Calculate what to display in the banner
         if (getBanner() == null)
             setBanner(new HtmlPanelBanner());
 
         HtmlPanelBanner banner = getBanner();
         HtmlPanelActions actions = new HtmlPanelActions();
 
         PanelEditorContentElement[] elements = getElementsAsArray();
         for (int i=0; i < elements.length; i++)
         {
             HtmlPanelAction addAction = banner.createAction();
             PanelEditorContentElement element = elements[i];
             String addUrl = generatePanelActionUrl(PanelEditor.MODE_ADD);
             addUrl = element.appendElementInfoToActionUrl(addUrl, PanelEditor.MODE_ADD);
             String caption = element.getCaption();
             addAction.setCaption(new StaticValueSource("Add " + (caption != null ? caption : "")));
             addAction.setRedirect(new RedirectValueSource(addUrl));
             actions.add(addAction);
         }
         banner.setActions(actions);
     }
 
 
     /**
      * Check to see if all the actions needed for the  panel edito have been ADDED. The
      * adding of the actions need to be done only once and this flag is set
      * once the addition is done.
      *
      * @return  True if all required actions have been added
      */
     public boolean isInitialized()
     {
         return initialized;
     }
 }
