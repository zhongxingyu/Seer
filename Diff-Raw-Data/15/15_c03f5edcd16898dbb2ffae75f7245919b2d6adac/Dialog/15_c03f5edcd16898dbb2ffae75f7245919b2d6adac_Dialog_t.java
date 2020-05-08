 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.form;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.collections.LRUMap;
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.tools.ant.Main;
 
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.xdm.XdmParseContext;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xdm.exception.DataModelException;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateCatalog;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 import com.netspective.sparx.Project;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.form.field.type.CompositeField;
 import com.netspective.sparx.form.field.type.GridField;
 import com.netspective.sparx.form.field.type.SectionField;
 import com.netspective.sparx.form.field.type.SeparatorField;
 import com.netspective.sparx.form.handler.DialogExecuteDefaultHandler;
 import com.netspective.sparx.form.handler.DialogExecuteHandler;
 import com.netspective.sparx.form.handler.DialogExecuteHandlerTemplateConsumer;
 import com.netspective.sparx.form.handler.DialogExecuteHandlers;
 import com.netspective.sparx.form.handler.DialogNextActionProvider;
 import com.netspective.sparx.form.listener.DialogInitialPopulateForDisplayListener;
 import com.netspective.sparx.form.listener.DialogInitialPopulateForSubmitListener;
 import com.netspective.sparx.form.listener.DialogInitialPopulateListener;
 import com.netspective.sparx.form.listener.DialogListener;
 import com.netspective.sparx.form.listener.DialogListenerPlaceholder;
 import com.netspective.sparx.form.listener.DialogPopulateForDisplayListener;
 import com.netspective.sparx.form.listener.DialogPopulateForSubmitListener;
 import com.netspective.sparx.form.listener.DialogPopulateListener;
 import com.netspective.sparx.form.listener.DialogStateAfterValidationListener;
 import com.netspective.sparx.form.listener.DialogStateBeforeValidationListener;
 import com.netspective.sparx.form.listener.DialogStateListener;
 import com.netspective.sparx.form.listener.DialogValidateListener;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.panel.AbstractPanel;
 import com.netspective.sparx.panel.HtmlInputPanel;
 import com.netspective.sparx.panel.editor.PanelEditor;
 import com.netspective.sparx.panel.editor.PanelEditorState;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.value.source.ThemeResourceUrlValueSource;
 
 /**
  * The <code>Dialog</code> object contains the dialog/form's structural information, field types, rules, and
  * execution logic. It is cached and reused whenever needed. It contains methods to create the HTML for display,
  * to perform client-side validations, and to perform server-side validations.
  */
 public class Dialog extends AbstractPanel implements HtmlInputPanel, TemplateConsumer, XmlDataModelSchema.ConstructionFinalizeListener
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     public static final String ATTRNAME_TYPE = "type";
     public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[]{"name"};
 
     private static DialogTypeTemplateConsumerDefn dialogTypeConsumer = new DialogTypeTemplateConsumerDefn();
     private static int dialogNumber = 0;
 
     static
     {
         TemplateCatalog.registerConsumerDefnForClass(dialogTypeConsumer, Dialog.class, true, true);
         TemplateCatalog.registerConsumerDefnForClass(DialogExecuteHandlerTemplateConsumer.INSTANCE, DialogExecuteHandler.class, true, true);
     }
 
     protected static class DialogTypeTemplateConsumerDefn extends TemplateConsumerDefn
     {
         public DialogTypeTemplateConsumerDefn()
         {
             super(Dialog.class.getName(), ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
         }
 
         public String getNameSpaceId()
         {
             return Dialog.class.getName();
         }
     }
 
     /**
      * Request parameter which indicates whether or not the dialog should be automatically executed when it is being loaded
      */
     public static final String PARAMNAME_AUTOEXECUTE = "_d_exec";
     public static final String PARAMNAME_OVERRIDE_SKIN = "_d_skin";
     public static final String PARAMNAME_DIALOGPREFIX = "_d.";
     public static final String PARAMNAME_CONTROLPREFIX = "_dc.";
 
     public static final String ATTRNAME_DIALOG_STATES = Dialog.class.getName() + ".STATES";
     public static final String ATTRNAME_DIALOG_STATES_MAX_ENTRIES = Dialog.class.getName() + ".STATES.MAX_ENTRIES";
     public static final int DIALOG_STATES_LRU_MAP_DEFAULT_MAX_SIZE = 16; // keep the last 32 dialog states in the session at any time
 
     public static final String PARAMNAME_DIALOG_STATE_ID = ".dialog_state_id";
     public static final String PARAMNAME_POST_EXECUTE_REDIRECT = ".post_exec_redirect";
     public static final String PARAMNAME_SUBMIT_DATA = ".submit_data";
     public static final String PARAMNAME_PEND_DATA = ".pend_data";
     public static final String PARAMNAME_CANCEL_DATA = ".cancel_data";
     public static final String PARAMNAME_RESET_CONTEXT = ".reset_context";
     public static final String PARAMNAME_VALIDATE_TRIGGER_FIELD = ".validate_trigger_field";
 
     /**
      * Converts dialog name to uppercase for use as MapKey
      *
      * @param name dialog name
      *
      * @return formatted dialog name
      */
     public static final String translateNameForMapKey(String name)
     {
         return name != null ? name.toUpperCase() : null;
     }
 
     private Project project;
     private Log log = LogFactory.getLog(Dialog.class);
     private DialogFields fields;
     private DialogFlags dialogFlags;
     private DialogDebugFlags debugFlags;
     private DialogLoopStyle loop = new DialogLoopStyle(DialogLoopStyle.APPEND);
     private DialogDirector director = createDirector();
     private DialogsNameSpace nameSpace;
     private String name = "dialog_" + (++dialogNumber);
     private String htmlFormName;
     private int layoutColumnsCount = 1;
     private String[] retainRequestParams;
     private Class dialogContextClass = DialogContext.class;
     private List dialogTypes = new ArrayList();
     private List clientJavascripts = new ArrayList();
     private DialogExecuteHandlers executeHandlers = new DialogExecuteHandlers();
     private DialogNextActionProvider nextActionProvider;
     private boolean redirectAfterExecute = true;
     private ValueSource multipleExecErrorMessage = new StaticValueSource("Multiple executions of this dialog are not allowed.");
     private String cookieName;
 
     private boolean haveInitialPopulateForDisplayListeners;
     private boolean haveInitialPopulateForSubmitListeners;
     private boolean haveInitialPopulateListeners;
     private boolean havePopulateForDisplayListeners;
     private boolean havePopulateForSubmitListeners;
     private boolean havePopulateListeners;
     private boolean haveStateAfterValidationListeners;
     private boolean haveStateBeforeValidationListeners;
     private boolean haveStateListeners;
     private boolean haveValidationListeners;
     private List initialPopulateForDisplayListeners = new ArrayList();
     private List initialPopulateForSubmitListeners = new ArrayList();
     private List initialPopulateListeners = new ArrayList();
     private List populateForDisplayListeners = new ArrayList();
     private List populateForSubmitListeners = new ArrayList();
     private List populateListeners = new ArrayList();
     private List stateAfterValidationListeners = new ArrayList();
     private List stateBeforeValidationListeners = new ArrayList();
     private List stateListeners = new ArrayList();
     private List validationListeners = new ArrayList();
 
     /**
      * Creates an empty dialog object.
      */
     public Dialog()
     {
         fields = constructFields();
         dialogFlags = createDialogFlags();
         debugFlags = createDebugFlags();
     }
 
     /**
      * Creates a dialog in the project.
      *
      * @param project the project in which the dialog is created
      */
     public Dialog(Project project)
     {
         this.project = project;
         fields = constructFields();
         dialogFlags = createDialogFlags();
         debugFlags = createDebugFlags();
     }
 
     /**
      * Creates a dialog in the project.
      *
      * @param project the project in which the dialog is created
      * @param pkg     the package in which the dialog is created
      */
     public Dialog(Project project, DialogsPackage pkg)
     {
         this(project);
         setNameSpace(pkg);
     }
 
     /**
      * Gets the project object for the dialog.
      *
      * @return the project object for the dialog
      */
     public Project getProject()
     {
         return project;
     }
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         return dialogTypeConsumer;
     }
 
     public void registerTemplateConsumption(Template template)
     {
         dialogTypes.add(template.getTemplateName());
     }
 
     public DialogFields constructFields()
     {
         return new DialogFields(this);
     }
 
     public DialogFlags createDialogFlags()
     {
         return new DialogFlags();
     }
 
     public DialogDebugFlags createDebugFlags()
     {
         return new DialogDebugFlags();
     }
 
     public Log getLog()
     {
         return log;
     }
 
     /**
      * Gets the name of the dialog.
      *
      * @return dialog name
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Gets the complete qualified dialog name in the format appropriate to be used as Map Key.
      *
      * @return the formatted dialog name
      */
     public String getNameForMapKey()
     {
         return translateNameForMapKey(getQualifiedName());
     }
 
     /**
      * Gets the complete qualified name of the dialog.
      *
      * @return qualified name of the dialog
      */
     public String getQualifiedName()
     {
         return nameSpace != null ? nameSpace.getNameSpaceId() + "." + name : name;
     }
 
     /**
      * Sets the name of the dialog. The name may only contain upper or lowercase letters, numbers, and underscores.
      * There should no punctuation characters or spaces and the name should be a valid JavaScript name.
      *
      * @param name the name of the dialog
      */
     public void setName(String name)
     {
         this.name = name;
         setHtmlFormName(name);
         log = LogFactory.getLog(getClass() + "." + getQualifiedName());
     }
 
     /**
      * Gets the name of the dialog created for HTML form.
      *
      * @return the name of HTML form for the dialog
      */
     public String getHtmlFormName()
     {
         return htmlFormName;
     }
 
     /**
      * Sets the name of the dialog created for HTML form. The name may only contain
      * upper or lowercase letters, numbers, and underscores.  There should no
      * punctuation characters or spaces and the name should be a valid JavaScript
      * name.  This name may or may not be the same as that specified by the Sparx
      * <code>dialog</code> tag.
      *
      * @param newName dialog name
      */
     public void setHtmlFormName(String newName)
     {
         htmlFormName = TextUtils.getInstance().xmlTextToJavaIdentifier(newName, false);
     }
 
     public DialogDebugFlags getDebugFlags()
     {
         return debugFlags;
     }
 
     public void setDebugFlags(DialogDebugFlags debugFlags)
     {
         this.debugFlags = debugFlags;
     }
 
     public DialogFlags getDialogFlags()
     {
         return dialogFlags;
     }
 
     public void setDialogFlags(DialogFlags dialogFlags)
     {
         this.dialogFlags = dialogFlags;
     }
 
     /**
      * Gets the cookie name used to store client-persistent field values
      *
      * @return String cookie name
      */
     public String getCookieName()
     {
         return cookieName == null ? getQualifiedName() : cookieName;
     }
 
     /**
      * Sets the cookie name used to store client-persistent field values
      *
      * @param name cookie name for this dialog
      */
     public void setCookieName(String name)
     {
         cookieName = name;
     }
 
 
     /**
      * Gets the loop style for the dialog. By default, it is set to append.
      *
      * @return dialog loop style
      */
     public DialogLoopStyle getLoop()
     {
         return loop;
     }
 
     /**
      * Specifies whether or not to show the dialog even after the dialog executes.
      * Used to show forms after searches complete.
      *
      * @param loop loop style such as append or prepend
      */
     public void setLoop(DialogLoopStyle loop)
     {
         this.loop = loop;
     }
 
     /**
      * Gets the HTML to be inserted between the dialog and its execution content,
      * if the loop is set to append or prepend.
      *
      * @return the HTML to be inserted between the dialog and its execution content
      */
     public String getLoopSeparator()
     {
         return loop.getLoopSeparator();
     }
 
     /**
      * Specifies the HTML to be inserted between the dialog and its execution content,
      * if the loop is set to append or prepend.
      *
      * @param loopSeparator HTML to be inserted between the dialog and its execution content
      */
 
     public void setLoopSeparator(String loopSeparator)
     {
         loop.setLoopSeparator(loopSeparator);
     }
 
     /**
      * Checks whether the dialog should be redirected after executing
      * (performing the tasks it's been instructed to perform).
      *
      * @return <code>true</code> if the dialog needs to be redirected after execution;
      *         <code>false</code> otherwise
      */
     public boolean isRedirectAfterExecute()
     {
         return redirectAfterExecute;
     }
 
     /**
      * Sets whether or not the dialog should be redirected after executing
      * (performing the tasks it's been instructed to perform).
      *
      * @param redirectAfterExecute <code>true</code> if the dialog needs to be
      *                             redirected after execution; <code>false</code> otherwise
      */
     public void setRedirectAfterExecute(boolean redirectAfterExecute)
     {
         this.redirectAfterExecute = redirectAfterExecute;
     }
 
     /**
      * Checks whether or not the dialog heading is to be hidden.
      *
      * @param dc current dialog context for the dialog
      *
      * @return <code>true</code> if the heading should be hidden; <code>false</code> otherwise
      */
     public boolean hideHeading(DialogContext dc)
     {
         if(dialogFlags.flagIsSet(DialogFlags.HIDE_HEADING_IN_EXEC_MODE) && dc.getDialogState().isInExecuteMode())
             return true;
         else
             return false;
     }
 
     /**
      * Gets the custom context class associated with the dialog.
      *
      * @return dialog context class
      */
     public Class getDialogContextClass()
     {
         return dialogContextClass;
     }
 
     /**
      * Sets a custom class as the context class for the dialog, to be dynamically
      * loaded and instantiated.
      *
      * @param dialogContextClass custom class to be set as the dialog context class
      */
     public void setDialogContextClass(Class dialogContextClass)
     {
         this.dialogContextClass = dialogContextClass;
     }
 
     public int getLayoutColumnsCount()
     {
         return layoutColumnsCount;
     }
 
     public String getPostExecuteRedirectUrlParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_POST_EXECUTE_REDIRECT;
     }
 
     /**
      * Gets the name of dialog's state identifier.
      *
      * @return name of dialog's state identifier
      */
 
     public String getDialogStateIdentifierParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_DIALOG_STATE_ID;
     }
 
     /**
      * Gets the name of the hidden input field used to keep track of the name of the dialog field that triggered
      * the submission of the form so that the field can be validated immediately.
      */
     public String getDialogValidateTriggerFieldParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_VALIDATE_TRIGGER_FIELD;
     }
 
     public String getResetContextParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_RESET_CONTEXT;
     }
 
     /**
      * Gets the name of the Submit button.
      *
      * @return name of the Submit button
      */
 
     public String getSubmitDataParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_SUBMIT_DATA;
     }
 
     /**
      * Gets the name of the Cancel button.
      *
      * @return name of the Cancel button
      */
     public String getCancelDataParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_CANCEL_DATA;
     }
 
     public String getPendDataParamName()
     {
         return PARAMNAME_DIALOGPREFIX + htmlFormName + PARAMNAME_PEND_DATA;
     }
 
     public String getValuesRequestAttrName()
     {
         return "dialog-" + htmlFormName + "-field-values";
     }
 
     /**
      * Gets a list of dialog fields.
      *
      * @return list of all dialog fields
      */
     public DialogFields getFields()
     {
         return fields;
     }
 
     /**
      * Indicates whether or not to retain the HTTP request parameters as dialog fields.
      *
      * @return <code>true</code> if the request parameters are retained in the dialog;
      *         <code>false</code> otherwise
      */
     public boolean retainRequestParams()
     {
         return dialogFlags.flagIsSet(DialogFlags.RETAIN_ALL_REQUEST_PARAMS) || (retainRequestParams != null);
     }
 
     /**
      * Gets the retained request parameters as a string array.
      *
      * @return the request parameters that were retained
      */
     public String[] getRetainParams()
     {
         return retainRequestParams;
     }
 
     /**
      * Sets the request parameters to be retained for use in multiple invocation
      * of the dialog during more than one request/response cycle.
      *
      * @param value names of request parameters to be retained.  Pass asterist (*) as
      *              the value to retain all request parameters
      */
     public void setRetainParams(String value)
     {
         if(value.equals("*"))
             dialogFlags.setFlag(DialogFlags.RETAIN_ALL_REQUEST_PARAMS);
         else
             retainRequestParams = TextUtils.getInstance().split(value, ",", true);
     }
 
     /**
      * Gets the error message to be displayed when multiple executions of the
      * dialog are detected.
      *
      * @return the error message
      */
     public ValueSource getMultipleExecErrorMessage()
     {
         return multipleExecErrorMessage;
     }
 
     /**
      * Sets the error message for multiple executions of the dialog.
      */
     public void setMultipleExecErrorMessage(ValueSource multipleExecErrorMessage)
     {
         this.multipleExecErrorMessage = multipleExecErrorMessage;
     }
 
     /**
      * Gets the URL for the next action of the dialog after execution. It searches for a next action URL using the
      * following order (and uses the first one found)<p>
      * <ol>
      * <li>The dialog director next actions field. If the director returns "-" as the URL, it means keep checking.</li>
      * <li>The dialog's next action provider delegated class (using Dialog.getNextActionProvider())</li>
      * <li>Active page next action provider delegated class (using dc.NavigationContext().getNextActionProvider)</li>
      * <li>The default url passed in</li>
      * </ol>
      *
      * @param dc         The dialog context for the dialog that just executed
      * @param defaultUrl The URL to use if no specific next actions are provided
      *
      * @return URL string to use for the URL (to send in redirect)
      */
     public String getNextActionUrl(DialogContext dc, String defaultUrl)
     {
         if(director != null && director.hasNextAction())
         {
             String url = director.getDialogNextActionUrl(dc, null);
             if(url != null)
                 return url;
 
             // if the url is null, it means that the director returned the default (NULL) and wants to let the
             // the delegated callers handle it so we'll fall through to the rest of the providers below
         }
         // if this dialog is being executed within a panel editor context, then use the panel'editor's mode URL
         PanelEditorState state = (PanelEditorState) dc.getHttpRequest().getAttribute(PanelEditor.PANEL_EDITOR_REQ_ATTRIBUTE_PREFIX);
         if(state != null)
         {
             return state.calculateNextModeUrl(dc);
         }
 
         // see if we are delegating our next action call to another class
         if(nextActionProvider != null)
             return nextActionProvider.getDialogNextActionUrl(dc, defaultUrl);
 
         // first see if there is page-specific or tree-wide next action provider
         DialogNextActionProvider navNextActionProvider = dc.getNavigationContext().getDialogNextActionProvider();
         if(navNextActionProvider != null)
             return navNextActionProvider.getDialogNextActionUrl(dc, defaultUrl);
 
         return defaultUrl;
     }
 
     /**
      * Gets the dialog director object.
      *
      * @return the dialog director object
      */
     public DialogDirector getDirector()
     {
         return director;
     }
 
     /**
      * Creates and returns the dialog director. Used mainly for XDM dialog creation.
      *
      * @return dialog director object
      */
     public DialogDirector createDirector()
     {
         return new DialogDirector();
     }
 
     /**
      * Sets the director for the dialog.
      *
      * @param value the director to be set for the dialog
      */
     public void addDirector(DialogDirector value)
     {
         director = value;
         value.setOwner(this);
     }
 
     /**
      * Gets all the javascript files to be included with this dialog.
      *
      * @return all the client javascript files
      */
     public List getClientJs()
     {
         return this.clientJavascripts;
     }
 
     /**
      * Adds a javascript file to be included.
      *
      * @param clientJsFile the javascript file to be included
      */
     public void addClientJs(DialogIncludeJavascriptFile clientJsFile)
     {
         clientJavascripts.add(clientJsFile);
     }
 
     /**
      * Gets the package namespace to which this dialog belongs.
      *
      * @return package namespace of the dialog
      */
     public DialogsNameSpace getNameSpace()
     {
         return nameSpace;
     }
 
     /**
      * Sets the package namespace for the dialog.
      *
      * @param nameSpace the namespace to be set for the dialog
      */
     public void setNameSpace(DialogsNameSpace nameSpace)
     {
         this.nameSpace = nameSpace;
     }
 
     /**
      * Creates a new DialogField object and returns it. Used mainly by XDM to create a dialog field.
      *
      * @return a new dialog field object
      */
     public DialogField createField()
     {
         return new DialogField();
     }
 
     /**
      * Adds a dialog field.
      *
      * @param field a dialog field
      */
     public void addField(DialogField field)
     {
         fields.add(field);
         field.setOwner(this);
     }
 
     /**
      * Creates a new composite field and returns it. This is used mainly by XDM
      * to instantiate a composite field.
      *
      * @return the newly created composite field
      */
     public CompositeField createComposite()
     {
         return new CompositeField();
     }
 
     public SectionField createSection()
     {
         return new SectionField();
     }
 
     public void addSection(SectionField field)
     {
         addField(field);
     }
 
     /**
      * Adds a composite field to the dialog.
      *
      * @param field the CompositeField object to be added to the dialog
      */
     public void addComposite(CompositeField field)
     {
         addField(field);
     }
 
     /**
      * Creates a new separator field. This is used mainly by XDM to instantiate
      * a separator field.
      *
      * @return the newly created separator field
      */
     public SeparatorField createSeparator()
     {
         return new SeparatorField();
     }
 
     /**
      * Adds a separator field to the dialog.
      *
      * @param field the separator field to be added to the dialog
      */
     public void addSeparator(SeparatorField field)
     {
         addField(field);
     }
 
     /**
      * Creates a new grid field. This is used mainly by XDM to instantiate
      * a grid field.
      *
      * @return the newly created grid field
      */
     public GridField createGrid()
     {
         return new GridField();
     }
 
     /**
      * Adds a grid field to the dialog.
      *
      * @param field the grid field to be added to the dialog
      */
     public void addGrid(GridField field)
     {
         addField(field);
     }
 
     /**
      * Calls the <code>finalizeContents</code> for each field belonging to the
      * dialog and also calculates the layout of the dialog fields.
      */
     public void finalizeContents()
     {
         boolean includedUnixCryptJS = false;
 
         fields.finalizeContents();
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             if(field.getFlags().flagIsSet(DialogFieldFlags.COLUMN_BREAK_BEFORE | DialogFieldFlags.COLUMN_BREAK_AFTER))
                 layoutColumnsCount++;
 
             if(field.getClientEncryption() != null)
             {
                 ClientDataEncryption encryption = field.getClientEncryption();
                 if(encryption.getStyle().getValueIndex() == DataEncryptionStyle.UNIX_CRYPT)
                 {
                     if(!includedUnixCryptJS)
                     {
                         DialogIncludeJavascriptFile js = new DialogIncludeJavascriptFile();
                         js.setHref(new ThemeResourceUrlValueSource("/scripts/javacrypt.js"));
                         addClientJs(js);
                         includedUnixCryptJS = true;
                     }
                 }
             }
         }
     }
 
     /**
      * Called at the end of  XDM processing to create the Dialog object. Currently
      * calls <code>finalizeContents</code>.
      *
      * @param pc          The XDM parsing context
      * @param element     The XML element for the dialog object
      * @param elementName The name of the element
      */
     public void finalizeConstruction(XdmParseContext pc, Object element, String elementName) throws DataModelException
     {
         finalizeContents();
     }
 
     /**
      * Populates the dialog with field values.  This should be called everytime
      * the dialog is loaded except when it is ready for execution (validated already)
      *
      * @param dc         dialog context
      * @param formatType format for the field
      */
     public void populateValues(DialogContext dc, int formatType)
     {
         DialogState dialogState = dc.getDialogState();
 
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             if(field.isAvailable(dc))
                 field.populateValue(dc, formatType);
         }
 
         if(director != null)
         {
             DialogField field = director.getNextActionsField();
             if(field != null)
                 field.populateValue(dc, formatType);
         }
 
         if(dialogState.isInitialEntry())
         {
             if(formatType == DialogField.DISPLAY_FORMAT && haveInitialPopulateForDisplayListeners)
             {
                 for(int i = 0; i < initialPopulateForDisplayListeners.size(); i++)
                     ((DialogInitialPopulateForDisplayListener) initialPopulateForDisplayListeners.get(i)).populateInitialDialogValuesForDisplay(dc);
             }
 
             if(formatType == DialogField.SUBMIT_FORMAT && haveInitialPopulateForSubmitListeners)
             {
                 for(int i = 0; i < initialPopulateForSubmitListeners.size(); i++)
                     ((DialogInitialPopulateForSubmitListener) initialPopulateForSubmitListeners.get(i)).populateInitialDialogValuesForSubmit(dc);
             }
 
             if(haveInitialPopulateListeners)
             {
                 for(int i = 0; i < initialPopulateListeners.size(); i++)
                     ((DialogInitialPopulateListener) initialPopulateListeners.get(i)).populateInitialDialogValues(dc, formatType);
             }
         }
 
         if(formatType == DialogField.DISPLAY_FORMAT && havePopulateForDisplayListeners)
         {
             for(int i = 0; i < populateForDisplayListeners.size(); i++)
                 ((DialogPopulateForDisplayListener) populateForDisplayListeners.get(i)).populateDialogValuesForDisplay(dc);
         }
 
         if(formatType == DialogField.SUBMIT_FORMAT && havePopulateForSubmitListeners)
         {
             for(int i = 0; i < populateForSubmitListeners.size(); i++)
                 ((DialogPopulateForSubmitListener) populateForSubmitListeners.get(i)).populateDialogValuesForSubmit(dc);
         }
 
         if(havePopulateListeners)
         {
             for(int i = 0; i < populateListeners.size(); i++)
                 ((DialogPopulateListener) populateListeners.get(i)).populateDialogValues(dc, formatType);
         }
 
         if(getDialogFlags().flagIsSet(DialogFlags.RETAIN_INITIAL_STATE) && dialogState.isInitialEntry())
             dialogState.saveInitialState(dc);
     }
 
     /**
      * Checks each field to see if its state needs to be changed or not, usually
      * based on Conditionals.
      * <p/>
      * <b>IMPORTANT</b>: If any changes are made in this class, make sure
      * that they are also reflected in QuerySelectDialog and QueryBuilderDialog classes
      * which extend this class but they overwrite this method and don't make a call
      * to this method.
      *
      * @param dc    dialog context
      * @param stage stage which the dialog is currently in, such as before validation
      *              and after validation)
      */
     public void makeStateChanges(DialogContext dc, int stage)
     {
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             field.makeStateChanges(dc, stage);
         }
         DialogDirector director = getDirector();
         if(director != null)
             director.makeStateChanges(dc, stage);
 
         if(stage == DialogContext.STATECALCSTAGE_BEFORE_VALIDATION && haveStateBeforeValidationListeners)
         {
             for(int i = 0; i < stateBeforeValidationListeners.size(); i++)
                 ((DialogStateBeforeValidationListener) stateBeforeValidationListeners.get(i)).makeDialogStateChangesBeforeValidation(dc);
         }
 
         if(stage == DialogContext.STATECALCSTAGE_AFTER_VALIDATION && haveStateAfterValidationListeners)
         {
             for(int i = 0; i < stateAfterValidationListeners.size(); i++)
                 ((DialogStateAfterValidationListener) stateAfterValidationListeners.get(i)).makeDialogStateChangesChangesAfterValidation(dc);
         }
 
         if(haveStateListeners)
         {
             for(int i = 0; i < stateListeners.size(); i++)
                 ((DialogStateListener) stateListeners.get(i)).makeDialogStateChanges(dc, stage);
         }
     }
 
     /**
      * Creates a new <code>DialogExecuteDefaultHandler</code> object. This is
      * used mainly by XDM to instantiate a DialogExecuteHandler object.
      *
      * @return the newly created dialog execute handler object
      */
     public DialogExecuteHandler createOnExecute()
     {
         return new DialogExecuteDefaultHandler();
     }
 
     /**
      * Adds a new <code>DialogExecuteHandler</code> object to the list of execute
      * handlers for the dialog. These listeners that implement the
      * <code>DialogExecuteHandler</code> interface will be called at execution time
      * to process custome dialog execute actions.
      *
      * @param handler execution handler object to be added for the dialog
      */
     public void addOnExecute(DialogExecuteHandler handler)
     {
         executeHandlers.add(handler);
         addListener(handler); // see if there are any other interfaces implemented by this handler
     }
 
     /**
      * Gets all the dialog execute handlers associated with the dialog.
      *
      * @return all the execute handlers for the dialog
      */
     public DialogExecuteHandlers getExecuteHandlers()
     {
         return executeHandlers;
     }
 
     /**
      * Ascertains whether or not this dialog should just auto-execute (not show
      * any input) by default.
      *
      * @return <code>true</code> if auto executing by default
      */
     public boolean isAutoExecByDefault()
     {
         return false;
     }
 
     /**
      * If this dialog is not auto-exec by default, then does the current state of the dialog (using the context)
      * indicate that it should be auto-executed.
      *
      * @param dc                    The current dialog context
      * @param autoExecReqParamValue The value of the _d_exec request parameter
      *
      * @return <code>true</code> if the current state indicates auto-execution; <code>false</code> otherwise
      */
     public boolean isAutoExec(DialogContext dc, String autoExecReqParamValue)
     {
         return autoExecReqParamValue != null && !autoExecReqParamValue.equals("no");
     }
 
     /**
      * Executes the actions of the dialog.
      *
      * @param writer stream for dialog execution output
      * @param dc     dialog context
      */
     public void execute(Writer writer, DialogContext dc) throws IOException, DialogExecuteException
     {
         if(dc.executeStageHandled())
             return;
 
         try
         {
             if(executeHandlers.size() > 0)
                 executeHandlers.handleDialogExecute(writer, dc);
             else
                 dc.renderDebugPanels(writer);
             getDialogState(dc).setAlreadyExecuted();
             handlePostExecute(writer, dc);
         }
         catch(DialogExecuteException e)
         {
             handlePostExecuteException(writer, dc, null, e);
         }
     }
 
     /**
      * Gets the next action provider of the dialog. The next action represents
      * the action to be performed after dialog execution.
      *
      * @return the next action provider for the dialog
      */
     public DialogNextActionProvider getNextActionProvider()
     {
         return nextActionProvider;
     }
 
     /**
      * Sets the next action provider for the dialog.  The next action represents
      * the action to be performed after dialog execution.
      *
      * @param nextActionProvider the next action provider for the dialog
      */
     public void addNextActionProvider(DialogNextActionProvider nextActionProvider)
     {
         this.nextActionProvider = nextActionProvider;
     }
 
     /**
      * Handles any post execution actions. Currently, it sets a flag to indicate
      * that the execution has been handled and then performs a URL redirection.
      *
      * @param writer Writer object related to the response buffer
      * @param dc     current dialog context
      */
     public void handlePostExecute(Writer writer, DialogContext dc) throws IOException
     {
         if(!getDialogFlags().flagIsSet(DialogFlags.DISABLE_ACTIVITY_ANNOUNCEMENT))
             dc.getParentActivity().broadcastChildActivity(dc);
 
         dc.setExecuteStageHandled(true);
         if(getDialogFlags().flagIsSet(DialogFlags.CLOSE_PAGE_AFTER_EXECUTE))
         {
             writer.write("<script>\n" +
                          "<!--\n" +
                          "    window.close();\n" +
                          "-->\n" +
                          "</script>");
             return;
         }
 
         dc.performDefaultRedirect(writer, null);
     }
 
     /**
      * Handles any post execution actions. Currently, it sets a flag to indicate
      * that the execution has been handled and then performs a URL redirection.
      *
      * @param writer   Writer object related to the response buffer
      * @param dc       current dialog context
      * @param redirect the URL to redirect to
      */
     public void handlePostExecute(Writer writer, DialogContext dc, String redirect) throws IOException
     {
         if(!getDialogFlags().flagIsSet(DialogFlags.DISABLE_ACTIVITY_ANNOUNCEMENT))
             dc.getParentActivity().broadcastChildActivity(dc);
 
         dc.setExecuteStageHandled(true);
         if(getDialogFlags().flagIsSet(DialogFlags.CLOSE_PAGE_AFTER_EXECUTE))
         {
             writer.write("<script>\n" +
                          "<!--\n" +
                          "    window.close();\n" +
                          "-->\n" +
                          "</script>");
             return;
         }
         dc.performDefaultRedirect(writer, redirect);
     }
 
     /**
      * Logs the exeception and writes it to the Writer.
      *
      * @param writer  Writer object related to the response buffer
      * @param dc      current dialog context
      * @param message custom exception message
      * @param e       the exception object
      */
     public void handlePostExecuteException(Writer writer, DialogContext dc, String message, Exception e) throws DialogExecuteException, IOException
     {
         dc.setExecuteStageHandled(true);
         getLog().error(message, e);
         dc.setRedirectDisabled(true);
         if(e instanceof DialogExecuteException)
             throw (DialogExecuteException) e;
         else
             throw new DialogExecuteException(e);
     }
 
     /**
      * Gets the current state of the dialog.
      *
      * @param dc current dialog context
      *
      * @return the current state of the dialog
      */
     public DialogState getDialogState(DialogContext dc)
     {
         HttpSession session = dc.getHttpRequest().getSession();
         Map dialogStates = (Map) session.getAttribute(ATTRNAME_DIALOG_STATES);
         if(dialogStates == null)
         {
             Integer maxEntriesAttr = (Integer) session.getAttribute(ATTRNAME_DIALOG_STATES_MAX_ENTRIES);
             dialogStates = new LRUMap(maxEntriesAttr != null
                                       ? maxEntriesAttr.intValue() : DIALOG_STATES_LRU_MAP_DEFAULT_MAX_SIZE);
             session.setAttribute(ATTRNAME_DIALOG_STATES, dialogStates);
         }
 
         DialogState result = null;
         String existingStateId = dc.getRequest().getParameter(getDialogStateIdentifierParamName());
         if(existingStateId != null)
             result = (DialogState) dialogStates.get(existingStateId);
 
         if(result == null)
         {
             result = constructDialogState();
             result.initialize(dc);
             dialogStates.put(result.getIdentifier(), result);
         }
 
         return result;
     }
 
     /**
      * Creates a new dialog state object.
      *
      * @return newly created dialog state object
      */
     public DialogState constructDialogState()
     {
         return new DialogState();
     }
 
     /**
      * Create dialog context for a dialog. If a custom dialog context class is
      * defined, the custom class will be instantiated, else a default
      * <code>DialogContext</code> object will be returned.
      *
      * @param nc   current navigation context for the dialog
      * @param skin dialog skin
      *
      * @return DialogContext newly create dialog context object
      */
     public DialogContext createContext(NavigationContext nc, DialogSkin skin)
     {
         DialogContext dc = null;
         try
         {
             dc = (DialogContext) dialogContextClass.newInstance();
         }
         catch(Exception e)
         {
            getLog().error(e);
             dc = new DialogContext();
         }
         dc.initialize(nc, this, skin);
         return dc;
     }
 
     /**
      * Initially populates the dialog with values in display format and then
      * calculates the state of the dialog.  If the dialog is in execute mode,
      * the values are then formatted for submittal.
      *
      * @param dc current dialog context
      */
     public void prepareContext(DialogContext dc)
     {
         populateValues(dc, DialogField.DISPLAY_FORMAT);
         dc.calcState();
         // validated and the dialog is ready for execution
         if(dc.getDialogState().isInExecuteMode())
         {
             dc.persistValuesToBrowser();
             populateValues(dc, DialogField.SUBMIT_FORMAT);
         }
     }
 
     /**
      * Creates and writes the HTML for the dialog
      *
      * @param writer                 stream to write the dialog HTML to
      * @param dc                     dialog context
      * @param contextPreparedAlready flag to indicate whether or not the
      *                               context has been prepared
      */
     public void render(Writer writer, DialogContext dc, boolean contextPreparedAlready) throws IOException, DialogExecuteException
     {
         if(!contextPreparedAlready)
             prepareContext(dc);
 
         if(dc.getDialogState().isInExecuteMode())
         {
             boolean debug = debugFlags.flagIsSet(DialogDebugFlags.SHOW_FIELD_DATA);
             if(debug)
                 dc.setRedirectDisabled(true);
 
             switch(loop.getValueIndex())
             {
                 case DialogLoopStyle.NONE:
                     if(debug)
                         dc.renderDebugPanels(writer);
                     else
                         execute(writer, dc);
                     break;
 
                 case DialogLoopStyle.APPEND:
                     if(debug)
                         dc.renderDebugPanels(writer);
                     else
                         execute(writer, dc);
                     writer.write(loop.getLoopSeparator());
                     dc.getSkin().renderHtml(writer, dc);
                     break;
 
                 case DialogLoopStyle.PREPEND:
                     dc.getSkin().renderHtml(writer, dc);
                     writer.write(loop.getLoopSeparator());
                     if(debug)
                         dc.renderDebugPanels(writer);
                     else
                         execute(writer, dc);
                     break;
             }
         }
         else
         {
             dc.getSkin().renderHtml(writer, dc);
         }
 
         renderViewSource(writer, dc.getNavigationContext());
     }
 
 
     /**
      * Creates and writes the HTML for the dialog using a dialog theme.
      *
      * @param writer stream to write the HTML
      * @param dc     current dialog context
      * @param theme  dialog theme
      */
     public void render(Writer writer, DialogContext dc, Theme theme, int flags) throws IOException
     {
         render(writer, dc.getNavigationContext(), theme, flags);
     }
 
 
     /**
      * Creates and writes the HTML for the dialog using a dialog theme.
      *
      * @param writer stream to write the HTML
      * @param nc     current navigation context for the dialog
      * @param theme  dialog theme
      *
      * @throws IOException when an error occurs while writing to HTML stream
      */
     public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
     {
         DialogContext dc = createContext(nc, theme.getDefaultDialogSkin());
         try
         {
             render(writer, dc, false);
         }
         catch(DialogExecuteException e)
         {
             log.error("Dialog execute error", e);
             throw new NestableRuntimeException(e);
         }
     }
 
     /**
      * Generates a custom java bean class file representing the context of the dialog.
      *
      * @param destDir   the destination directory to write the bean class
      * @param pkgPrefix the package to which the bean class belongs
      *
      * @return the bean class file
      *
      * @throws IOException when an error occurs during file or html stream I/O
      */
     public File generateDialogContextBean(File destDir, String pkgPrefix) throws IOException
     {
         StringBuffer importsCode = new StringBuffer();
         StringBuffer membersCode = new StringBuffer();
 
         Set modulesImported = new HashSet();
 
         DialogFields fields = getFields();
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             DialogContextBeanMemberInfo mi = field.getDialogContextBeanMemberInfo();
             if(mi != null)
             {
                 String[] importModules = mi.getImportModules();
                 if(importModules != null)
                 {
                     for(int m = 0; m < importModules.length; m++)
                     {
                         String module = importModules[m];
                         if(!modulesImported.contains(module))
                         {
                             modulesImported.add(module);
                             importsCode.append("import " + module + ";\n");
                         }
                     }
                 }
                 membersCode.append(mi.getCode());
                 membersCode.append("\n");
             }
             if(field instanceof GridField || field instanceof CompositeField || field instanceof SectionField)
                 continue;
             DialogFields childrenFields = field.getChildren();
             if(childrenFields != null && childrenFields.size() > 0)
             {
                 for(int j = 0; j < childrenFields.size(); j++)
                 {
                     DialogField child = childrenFields.get(j);
                     DialogContextBeanMemberInfo miChild = child.getDialogContextBeanMemberInfo();
                     if(mi != null)
                     {
                         String[] importModules = miChild.getImportModules();
                         if(importModules != null)
                         {
                             for(int m = 0; m < importModules.length; m++)
                             {
                                 String module = importModules[m];
                                 if(!modulesImported.contains(module))
                                 {
                                     modulesImported.add(module);
                                     importsCode.append("import " + module + ";\n");
                                 }
                             }
                         }
                         membersCode.append(miChild.getCode());
                         membersCode.append("\n");
                     }
                 }
             }
         }
 
         TextUtils textUtils = TextUtils.getInstance();
         String className = textUtils.xmlTextToJavaIdentifier(getName(), true);
 
         String packageName = pkgPrefix + "." + textUtils.xmlTextToJavaPackageName(getNameSpace().getNameSpaceId());
 
         StringBuffer code = new StringBuffer();
         code.append("\n/* this file is generated by com.netspective.sparx.form.Dialog.getSubclassedDialogContextCode(), do not modify (you can extend it, though) */\n\n");
         code.append("package " + packageName + ";\n\n");
         if(importsCode.length() > 0)
             code.append(importsCode.toString());
         code.append("import com.netspective.sparx.form.*;\n");
         code.append("import com.netspective.sparx.form.field.*;\n");
         code.append("import com.netspective.sparx.form.field.type.*;\n\n");
         code.append("public class " + className + "Context\n");
         code.append("{\n");
         code.append("    public static final String DIALOG_ID = \"" + getQualifiedName() + "\";\n");
         code.append("    private DialogContext dialogContext;\n");
         code.append("    private DialogFieldStates fieldStates;\n\n");
         code.append("    public " + className + "Context(DialogContext dc)\n");
         code.append("    {\n");
         code.append("        this.dialogContext = dc;\n");
         code.append("        this.fieldStates = dc.getFieldStates();\n");
         code.append("    }\n\n");
         code.append("    public DialogContext getDialogContext() { return dialogContext; }\n\n");
         code.append(membersCode.toString());
         code.append("}\n");
 
         File javaFilePath = new File(destDir, packageName.replace('.', '/'));
         javaFilePath.mkdirs();
 
         File javaFile = new File(javaFilePath, className + "Context.java");
 
         Writer writer = new java.io.FileWriter(javaFile);
         writer.write(code.toString());
         writer.close();
 
         return javaFile;
     }
 
     /**
      * Indicates whether or not the dialog needs validation.
      *
      * @param dc dialog context
      *
      * @return <code>true</code> if the dialog needs validation; <code>false</code> otherwise
      */
     public boolean needsValidation(DialogContext dc)
     {
         int validateFieldsCount = 0;
 
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             if(field.isAvailable(dc) && field.needsValidation(dc))
                 validateFieldsCount++;
         }
 
         return validateFieldsCount > 0 ? true : false;
     }
 
     /**
      * Checks to see if the dialog submission was triggered for validation of a single field.
      *
      * @param dc current dialog context
      *
      * @return NULL if the dialog doesn't allow the event or if the triggering field name is empty
      *
      * @see #getDialogValidateTriggerFieldParamName()
      */
     public String checkFieldValidationTrigger(DialogContext dc)
     {
         // check to see which field initiated the submission
         String validateFieldName = dc.getHttpRequest().getParameter(getDialogValidateTriggerFieldParamName());
         if(validateFieldName != null && validateFieldName.length() > 0)
             return validateFieldName;
         return null;
     }
 
     /**
      * Checks whether or not the dailog is valid for execution.
      *
      * @param dc dialog context
      *
      * @return <code>true</code> if the dialog is valid; <code>false</code> otherwise.
      */
     public boolean isValid(DialogContext dc)
     {
         DialogValidationContext dvc = dc.getValidationContext();
         int valStage = dvc.getValidationStage();
         if(valStage == DialogValidationContext.VALSTAGE_PERFORMED_SUCCEEDED || valStage == DialogValidationContext.VALSTAGE_IGNORE)
             return true;
         if(valStage == DialogValidationContext.VALSTAGE_PERFORMED_FAILED)
             return false;
 
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             if((field.isAvailable(dc) && !field.isInputHidden(dc)))
                 field.validate(dvc);
         }
 
         if(haveValidationListeners)
         {
             for(int i = 0; i < validationListeners.size(); i++)
                 ((DialogValidateListener) validationListeners.get(i)).validateDialog(dvc);
         }
 
         boolean isValid = dvc.isValid();
         dvc.setValidationStage(isValid
                                ? DialogValidationContext.VALSTAGE_PERFORMED_SUCCEEDED
                                : DialogValidationContext.VALSTAGE_PERFORMED_FAILED);
         return isValid;
     }
 
     /**
      * Formats and displays the error message in HTML format.
      *
      * @param writer html stream
      * @param e      error message
      */
     protected void renderFormattedExceptionMessage(Writer writer, Exception e) throws IOException
     {
         writer.write("<div class='textbox'>" + Main.getAntVersion() + "<p><pre>");
         writer.write(TextUtils.getInstance().getStackTrace(e));
         writer.write("</pre>");
     }
 
     /**
      * Attaches a custom class to manage dialog events such as validation,
      * execution and so on.  Creates a placeholder for the dynamic listener class.
      *
      * @return a placeholder for the dialog listener
      */
     public DialogListener createListener()
     {
         return new DialogListenerPlaceholder();
     }
 
     /**
      * Registers a listener for the dialog.  Listeners are used to define custom actions
      * for different stages that the dialog goes through.
      *
      * @param listeners list of listeners to which the listener is being registered
      * @param listener  the listener to be registered
      */
     private void registerListener(List listeners, DialogListener listener)
     {
         if(!listeners.contains(listener))
             listeners.add(listener);
     }
 
     /**
      * Adds a listener for the dialog.  Listeners are used to define custom actions
      * for different stages that the dialog goes through.  There are several listener
      * interfaces available for a dialog:
      * <ul>
      * <li>DialogInitialPopulateForDisplayListener: processed during initial population of the dialog and the format type is set to display mode </li>
      * <li>DialogInitialPopulateForSubmitListener: processed during initial population of the dialog  and the format type is set to submit mode </li>
      * <li>DialogInitialPopulateListener</li>
      * <li>DialogPopulateForDisplayListener: processed during subsequent population of the dialog and the format type is set to display mode</li>
      * <li>DialogPopulateForSubmitListener: porcessed during subsequent population of the dialog and the format type is set to submit mode</li>
      * <li>DialogPopulateListener</li>
      * <li>DialogStateAfterValidationListener</li>
      * <li>DialogStateBeforeValidationListener</li>
      * <li>DialogStateListener</li>
      * <li>DialogValidateListener</li>
      * <li>DialogExecuteHandler: </li>
      * </ul>
      * Implementing listeners classes can be registered to the dialog using the
      * <code>&lt;listener&gt;</code> tag.
      *
      * @param listener the listener to be added for the dialog
      */
     public void addListener(DialogListener listener)
     {
         if(listener instanceof DialogInitialPopulateForDisplayListener)
         {
             haveInitialPopulateForDisplayListeners = true;
             registerListener(initialPopulateForDisplayListeners, listener);
         }
 
         if(listener instanceof DialogInitialPopulateForSubmitListener)
         {
             haveInitialPopulateForSubmitListeners = true;
             registerListener(initialPopulateForSubmitListeners, listener);
         }
 
         if(listener instanceof DialogInitialPopulateListener)
         {
             haveInitialPopulateListeners = true;
             registerListener(initialPopulateListeners, listener);
         }
 
         if(listener instanceof DialogPopulateForDisplayListener)
         {
             havePopulateForDisplayListeners = true;
             registerListener(populateForDisplayListeners, listener);
         }
 
         if(listener instanceof DialogPopulateForSubmitListener)
         {
             havePopulateForSubmitListeners = true;
             registerListener(populateForSubmitListeners, listener);
         }
 
         if(listener instanceof DialogPopulateListener)
         {
             havePopulateListeners = true;
             registerListener(populateListeners, listener);
         }
 
         if(listener instanceof DialogStateAfterValidationListener)
         {
             haveStateAfterValidationListeners = true;
             registerListener(stateAfterValidationListeners, listener);
         }
 
         if(listener instanceof DialogStateBeforeValidationListener)
         {
             haveStateBeforeValidationListeners = true;
             registerListener(stateBeforeValidationListeners, listener);
         }
 
         if(listener instanceof DialogStateListener)
         {
             haveStateListeners = true;
             registerListener(stateListeners, listener);
         }
 
         if(listener instanceof DialogValidateListener)
         {
             haveValidationListeners = true;
             registerListener(validationListeners, listener);
         }
 
         if(listener instanceof DialogExecuteHandler)
             executeHandlers.add((DialogExecuteHandler) listener);
     }
 
     /**
      * Gets a list of all the initial dialog population listeners for display mode.
      *
      * @return all the initial dialog population listeners, for display mode
      */
     public List getInitialPopulateForDisplayListeners()
     {
         return initialPopulateForDisplayListeners;
     }
 
     /**
      * Gets a list of all the initial dialog population for submit mode.
      *
      * @return all the initial dialog population, for submit mode
      */
     public List getInitialPopulateForSubmitListeners()
     {
         return initialPopulateForSubmitListeners;
     }
 
     /**
      * Gets all the initial population listeners for the dialog.
      *
      * @return all the initial population listeners
      */
     public List getInitialPopulateListeners()
     {
         return initialPopulateListeners;
     }
 
     /**
      * Gets all the subsequent population listeners for display mode.
      *
      * @return all the population listeners, for display mode
      */
     public List getPopulateForDisplayListeners()
     {
         return populateForDisplayListeners;
     }
 
     /**
      * Gets all the subsequent population listeners for submit mode.
      *
      * @return all the population listeners, for submit mode
      */
     public List getPopulateForSubmitListeners()
     {
         return populateForSubmitListeners;
     }
 
     /**
      * Gets all the dialog population listeners.
      *
      * @return all the population listeners for the dialog
      */
     public List getPopulateListeners()
     {
         return populateListeners;
     }
 
     /**
      * Gets all the listeners for the dialog after validation stage.
      *
      * @return all the listeners for the dialog, after validation stage
      */
     public List getStateAfterValidationListeners()
     {
         return stateAfterValidationListeners;
     }
 
     /**
      * Gets all the listeners for the dialog before validation stage.
      *
      * @return all the listeners for the dialog, before validation stage
      */
     public List getStateBeforeValidationListeners()
     {
         return stateBeforeValidationListeners;
     }
 
     /**
      * Gets all the dialog state listeners.
      *
      * @return all the state listeners for the dialog
      */
     public List getStateListeners()
     {
         return stateListeners;
     }
 
     /**
      * Gets all the dialog validation listeners.
      *
      * @return all the validation listeners for the dialog
      */
     public List getValidationListeners()
     {
         return validationListeners;
     }
 }
