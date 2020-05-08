 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: Dialog.java,v 1.16 2002-12-23 04:33:59 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.form;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.apache.log4j.Logger;
 
 import com.netspective.sparx.xaf.form.field.SelectField;
 import com.netspective.sparx.xaf.form.field.StaticField;
 import com.netspective.sparx.xaf.task.BasicTask;
 import com.netspective.sparx.xaf.task.Task;
 import com.netspective.sparx.xaf.task.TaskContext;
 import com.netspective.sparx.xaf.task.TaskExecuteException;
 import com.netspective.sparx.xaf.task.TaskInitializeException;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 import com.netspective.sparx.util.log.AppServerLogger;
 import com.netspective.sparx.util.log.LogManager;
 
 /**
  * The <code>Dialog</code> object contains the dialog/form's structural information, field types, rules, and
  * execution logic. It is cached and reused whenever needed. It contains methods to create the HTML for display,
  * to perform client-side validations, and to perform server-side validations.
  * <p>
  * <center>
  * <img src="doc-files/dialog-1.jpg"/>
  * </center>
  * <p>
  * The dialog execution logic can contain different actions
  * such as SQL and business actions. These actions may be specified as XML or can point to any Java action classes.
  * For dialog objects that need more complex actions for data population, validation,
  * and execution, the <code>Dialog</code> class can be subclassed to implement customized actions.
  */
 public class Dialog
 {
     // NOTE: when adding new flags, make sure to create them before the
     // last DLGFLAG_CUSTOM_START entry. This is because QueryBuilderDialog
     // extends this class and has additional flags that is based on the value
     // of DLGFLAG_CUSTOM_START.
     static public final int DLGFLAG_CONTENTS_FINALIZED = 1;
     static public final int DLGFLAG_RETAIN_ALL_REQUEST_PARAMS = DLGFLAG_CONTENTS_FINALIZED * 2;
     static public final int DLGFLAG_LOOP_DATA_ENTRY = DLGFLAG_RETAIN_ALL_REQUEST_PARAMS * 2;
     static public final int DLGFLAG_APPEND_WHEN_LOOPING = DLGFLAG_LOOP_DATA_ENTRY * 2;
     static public final int DLGFLAG_HIDE_READONLY_HINTS = DLGFLAG_APPEND_WHEN_LOOPING * 2;
     static public final int DLGFLAG_ENCTYPE_MULTIPART_FORMDATA = DLGFLAG_HIDE_READONLY_HINTS * 2;
     static public final int DLGFLAG_HIDE_HEADING_IN_EXEC_MODE = DLGFLAG_ENCTYPE_MULTIPART_FORMDATA * 2;
     static public final int DLGFLAG_READONLY_FIELDS_HIDDEN_UNLESS_HAVE_DATA = DLGFLAG_HIDE_HEADING_IN_EXEC_MODE * 2;
     static public final int DLGFLAG_READONLY_FIELDS_INVISIBLE_UNLESS_HAVE_DATA = DLGFLAG_READONLY_FIELDS_HIDDEN_UNLESS_HAVE_DATA * 2;
     static public final int DLGFLAG_CUSTOM_START = DLGFLAG_READONLY_FIELDS_INVISIBLE_UNLESS_HAVE_DATA * 2;
 
     static public final int DLGDEBUGFLAG_NONE = 0;
     /**
      * If this debug flag is set, the execute mode will always be to dump the debug information and skip the execute
      * portion of the dialog (hence showing only the input in a nicely formatted table).
      */
     static public final int DLGDEBUGFLAG_SHOW_FIELD_DATA = 1;
 
     static public final String DLGDEBUGFLAGNAME_SHOW_DATA = "SHOW_DATA";
 
     /**
      * Request parameter which indicates whether or not the dialog should be automatically executed when it is being loaded
      */
     static public final String PARAMNAME_AUTOEXECUTE = "_d_exec";
     static public final String PARAMNAME_OVERRIDE_SKIN = "_d_skin";
     static public final String PARAMNAME_DIALOGPREFIX = "_d.";
     static public final String PARAMNAME_CONTROLPREFIX = "_dc.";
     static public final String PARAMNAME_DIALOGQNAME = "_d.dialog_qname";
     static public final String PARAMNAME_IGNORE_VALIDATION = PARAMNAME_CONTROLPREFIX + "ignore_val";
 
     static public final String PARAMNAME_ACTIVEMODE = ".active_mode";
     static public final String PARAMNAME_NEXTMODE = ".next_mode";
     static public final String PARAMNAME_RUNSEQ = ".run_sequence";
     static public final String PARAMNAME_EXECSEQ = ".exec_sequence";
     static public final String PARAMNAME_ORIG_REFERER = ".orig_referer";
     static public final String PARAMNAME_POST_EXECUTE_REDIRECT = ".post_exec_redirect";
     static public final String PARAMNAME_TRANSACTIONID = ".transaction_id";
     static public final String PARAMNAME_RESETCONTEXT = ".reset_context";
 
     /*
 	   the debug flags when first passed in (start of dialog, run seq == 1)
 	   is passed using the parameter "debug_flags" (INITIAL). When the dialog is
 	   in run sequence > 1 (after submit) the data command is passed in as a
 	   hidden "pass-thru" variable with the suffix PARAMNAME_DEBUG_FLAGS
     */
     static public final String PARAMNAME_DEBUG_FLAGS_INITIAL = "data_cmd";
     static public final String PARAMNAME_DEBUG_FLAGS = ".data_cmd";
     /*
 	   the Data Command when first passed in (start of dialog, run seq == 1)
 	   is passed using the parameter "data_cmd" (INITIAL). When the dialog is
 	   in run sequence > 1 (after submit) the data command is passed in as a
 	   hidden "pass-thru" variable with the suffix PARAMNAME_DATA_CMD
     */
     static public final String PARAMNAME_DATA_CMD_INITIAL = "data_cmd";
     static public final String PARAMNAME_DATA_CMD = ".data_cmd";
     /**
      * Add data command. In 'Add' mode, the default action button is a 'Save' button.
      */
     static public final String PARAMVALUE_DATA_CMD_ADD = "add";
     /**
      * Edit data command. IN 'Edit' mode, the default action button is a 'Apply' button.
      */
     static public final String PARAMVALUE_DATA_CMD_EDIT = "edit";
     /**
      * Delete data command. In 'Delete' mode, all fields are read-only and the default
      * action button is a 'Delete' button.
      */
     static public final String PARAMVALUE_DATA_CMD_DELETE = "delete";
     /**
      * Confirm data command
      */
     static public final String PARAMVALUE_DATA_CMD_CONFIRM = "confirm";
     /**
      * Print data command
      */
     static public final String PARAMVALUE_DATA_CMD_PRINT = "print";
 
     /**
      * This flag is used to search for a session parameter indicating whether or not
      * the dialog is being used in an application or ACE.
      */
     public static final String ENV_PARAMNAME = "dialog_environment";
 
     public static final String FIELDNAME_SCHEMA_TABLECOL = "field.table-column";
 
     private ArrayList fields = new ArrayList();
     private int flags;
     private Task populateTasks;
     private Task executeTasks;
     private DialogDirector director;
     private String pkgName;
     private String nameFromXml;
     private String name;
     private SingleValueSource heading;
     private String loopSeparator = "<p>";
     private int layoutColumnsCount = 1;
     private String[] retainRequestParams;
     private Class dcClass = DialogContext.class;
     private Class directorClass = DialogDirector.class;
     private SingleValueSource includeJSFile = null;
 
     /**
      * Create a dialog
      */
     public Dialog()
     {
         layoutColumnsCount = 1;
         flags = (DLGFLAG_LOOP_DATA_ENTRY | DLGFLAG_APPEND_WHEN_LOOPING);
     }
 
     /**
      * Create a dialog
      *
      * @param aName dialog name
      * @param aHeading dialog heading
      */
     public Dialog(String aName, String aHeading)
     {
         name = aName;
         setHeading(aHeading);
         layoutColumnsCount = 1;
         flags = (DLGFLAG_LOOP_DATA_ENTRY | DLGFLAG_APPEND_WHEN_LOOPING);
     }
 
     /**
      * Gets the dialog name
      *
      * @return String name
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Gets the dialog name defined in XML
      *
      * @return String
      */
     public String getNameFromXml()
     {
         return nameFromXml;
     }
 
     /**
      * Sets the dialog name
      *
      * @param newName dialog name
      */
     public void setName(String newName)
     {
         name = newName;
     }
 
     /**
      * Gets the flags as a bitmapped value
      *
      * @return long
      */
     public final long getFlags()
     {
         return flags;
     }
 
     /**
      * Checks to see if a flag is set
      *
      * @param flag      bit-mapped value
      * @return boolean  True if the flag is set
      */
     public final boolean flagIsSet(long flag)
     {
         return (flags & flag) == 0 ? false : true;
     }
 
     /**
      * Sets the flag
      *
      * @param flag bit-mapped value
      */
     public final void setFlag(long flag)
     {
         flags |= flag;
     }
 
     /**
      * Clears the flag
      *
      * @param flag bit-mapped value
      */
     public final void clearFlag(long flag)
     {
         flags &= ~flag;
     }
 
     /**
      * Gets the dialog heading as a value source
      *
      * @return SingleValueSource
      */
     public SingleValueSource getHeading()
     {
         return heading;
     }
 
     /**
      * Sets the heading of the dialog
      *
      * @param value value source string
      */
     public void setHeading(String value)
     {
         heading = ValueSourceFactory.getSingleOrStaticValueSource(value);
     }
 
     /**
      * Sets the heading of the dialog
      *
      * @param vs value source object
      */
     public void setHeading(SingleValueSource vs)
     {
         heading = vs;
     }
 
     /**
      * Returns true if the heading should be hidden
      */
     public boolean hideHeading(DialogContext dc)
     {
         if(flagIsSet(DLGFLAG_HIDE_HEADING_IN_EXEC_MODE) && dc.inExecuteMode())
             return true;
         else
             return false;
     }
 
     /**
      * Checks to see if the loop entries flag is set
      *
      * @return boolean
      */
     public final boolean loopEntries()
     {
         return flagIsSet(DLGFLAG_LOOP_DATA_ENTRY);
     }
 
     /**
      * Sets the loop entries flag
      *
      * @param value
      */
     public final void setLoopEntries(boolean value)
     {
         if(value) setFlag(DLGFLAG_LOOP_DATA_ENTRY); else clearFlag(DLGFLAG_LOOP_DATA_ENTRY);
     }
 
     public final boolean appendAfterLoop()
     {
         return flagIsSet(DLGFLAG_APPEND_WHEN_LOOPING);
     }
 
     public final void setAppendAfterLoop(boolean value)
     {
         if(value) setFlag(DLGFLAG_APPEND_WHEN_LOOPING); else clearFlag(DLGFLAG_APPEND_WHEN_LOOPING);
     }
 
     public final int getLayoutColumnsCount()
     {
         return layoutColumnsCount;
     }
 
     public final String getLoopSeparator()
     {
         return loopSeparator;
     }
 
     public final String getPostExecuteRedirectUrlParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_POST_EXECUTE_REDIRECT;
     }
 
     public final String getOriginalRefererParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_ORIG_REFERER;
     }
 
     public final String getActiveModeParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_ACTIVEMODE;
     }
 
     public final String getNextModeParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_NEXTMODE;
     }
 
     public final String getRunSequenceParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_RUNSEQ;
     }
 
     public final String getExecuteSequenceParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_EXECSEQ;
     }
 
     public final String getTransactionIdParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_TRANSACTIONID;
     }
 
     public final String getResetContextParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_RESETCONTEXT;
     }
 
     public final String getValuesRequestAttrName()
     {
         return "dialog-" + name + "-field-values";
     }
 
     public final String getDataCmdParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_DATA_CMD;
     }
 
     public final String getDebugFlagsParamName()
     {
         return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_DEBUG_FLAGS;
     }
 
     public final Task getPopulateTasks()
     {
         return populateTasks;
     }
 
     public final Task getExecuteTasks()
     {
         return executeTasks;
     }
     /**
      * Get a list of dialog fields
      *
      * @return List
      */
     public final List getFields()
     {
         return fields;
     }
 
     /**
      * Indicates whether or not to retain the HTTP request parameters as dialog fields
      *
      * @return boolean True if the request parameters are retained in the dialog
      */
     public final boolean retainRequestParams()
     {
         return flagIsSet(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS) || (retainRequestParams != null);
     }
     /**
      * Get the retained request parameters as a string array
      *
      * @return String[]
      */
     public final String[] getRetainRequestParams()
     {
         return retainRequestParams;
     }
 
     /**
      * Set the retained request parameters
      *
      * @param value array of string values
      */
     public final void setRetainRequestParams(String[] value)
     {
         retainRequestParams = value;
     }
 
     /**
      *
      */
     public final void setRetainAllRequestParams(boolean value)
     {
         if(value) setFlag(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS); else clearFlag(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS);
     }
 
     public final boolean retainAllRequestParams()
     {
         return flagIsSet(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS);
     }
 
     public String getNextActionUrl(DialogContext dc, String defaultUrl)
     {
         if(director == null)
             return defaultUrl;
 
         String result = director.getNextActionUrl(dc);
         if(result == null || result.equals("-"))
             return defaultUrl;
 
         return result;
     }
 
     public DialogDirector getDirector()
     {
         return director;
     }
 
     public void setDirector(DialogDirector value)
     {
         director = value;
     }
 
     public void setDialogDirectorClass(Class cls)
     {
         directorClass = cls;
     }
 
     public SingleValueSource getIncludeJSFile()
     {
         return includeJSFile;
     }
 
     /**
      * Get the dialog context class for the dialog object
      *
      * @return Class
      */
     public static Class findDialogContextClass(String packageName, Element elem) throws ClassNotFoundException
     {
         Class dcClass = null;
 
         // see if this dialog is using a special context (bean?) class
         // if a specific dc-class is provided, use it or try and find one
         // with the same name as the dialog in "app.form.context." package
         String dcClassName = elem.getAttribute("dc-class");
 
         if(dcClassName != null && dcClassName.length() > 0)
             dcClass = Class.forName(dcClassName);
         else
         {
             String elemName = com.netspective.sparx.util.xml.XmlSource.xmlTextToJavaIdentifier(elem.getAttribute("name"), true);
             String dlgName = packageName != null ? (packageName + "." + elemName) : elemName;
             dcClassName = "app.form.context." + dlgName + "Context";
             try
             {
                 dcClass = Class.forName(dcClassName);
             }
             catch(ClassNotFoundException e)
             {
                 dcClass = DialogContext.class;
             }
         }
 
         return dcClass;
     }
 
     public void setDialogContextClass(Class cls)
     {
         dcClass = cls;
     }
 
     /**
      * Import the dialog configuration from XML
      *
      * @param packageName   Dialog package name
      * @param elem          the dialog XML element
      */
     public void importFromXml(String packageName, Element elem)
     {
         pkgName = packageName;
         nameFromXml = elem.getAttribute("name");
         name = nameFromXml;
 
         // we delimit with "_" instead of "." because the dialog name must
         // be a valid single JavaScript token (to make it easy for the skin)
         if(packageName != null)
             name = packageName + "_" + name;
 
         if(!Character.isJavaIdentifierStart(name.charAt(0)))
             throw new RuntimeException("Dialog name '" + name + " may contain only digits, letters, and _.'");
 
         for(int c = 1; c < name.length(); c++)
         {
             if(!Character.isJavaIdentifierPart(name.charAt(c)))
                 throw new RuntimeException("Dialog name '" + name + " may contain only digits, letters, and _.'");
         }
 
         String headingVS = elem.getAttribute("heading");
         if(headingVS.length() > 0)
             setHeading(headingVS);
 
         String loop = elem.getAttribute("loop");
         if(loop.equals("no"))
             clearFlag(DLGFLAG_LOOP_DATA_ENTRY);
         else if(loop.length() > 0)
         {
             setFlag(DLGFLAG_LOOP_DATA_ENTRY);
             if(loop.equals("prepend")) // value can be (yes | append), prepend
                 this.setAppendAfterLoop(false);
             else
                 this.setAppendAfterLoop(true);
         }
 
         String loopSep = elem.getAttribute("loop-sep");
         if(loopSep.length() > 0)
             loopSeparator = loopSep;
 
         String hideHints = elem.getAttribute("hide-readonly-hints");
         if(hideHints.equals("yes"))
             setFlag(DLGFLAG_HIDE_READONLY_HINTS);
 
         if(elem.getAttribute("read-only-fields-hidden-unless-have-data").equalsIgnoreCase("yes"))
             setFlag(DLGFLAG_READONLY_FIELDS_HIDDEN_UNLESS_HAVE_DATA);
 
         if(elem.getAttribute("read-only-fields-invisible-unless-have-data").equalsIgnoreCase("yes"))
             setFlag(DLGFLAG_READONLY_FIELDS_INVISIBLE_UNLESS_HAVE_DATA);
 
         if(director == null)
         {
             try
             {
                 director = (DialogDirector) directorClass.newInstance();
             }
             catch(Exception e)
             {
                 LogManager.recordException(this.getClass(), "importFromXml", "Unable to instantiate dialog director '"+ directorClass.getName() +"'", e);
                 addField(new StaticField("error", "Dialog director problem: " + e.toString()));
                 director = new DialogDirector();
             }
         }
 
         String retainRequestParamsStr = elem.getAttribute("retain-params");
         if(retainRequestParamsStr.length() > 0)
         {
             if(retainRequestParamsStr.equals("*"))
                 setFlag(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS);
             else
             {
                 ArrayList paramNames = new ArrayList();
                 StringTokenizer st = new StringTokenizer(retainRequestParamsStr, ",");
                 while(st.hasMoreTokens())
                     paramNames.add(st.nextToken());
                 retainRequestParams = new String[paramNames.size()];
                 paramNames.toArray(retainRequestParams);
                 paramNames = null;
             }
         }
         else
         {
             clearFlag(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS);
             retainRequestParams = null;
         }
 
         NodeList children = elem.getChildNodes();
         for(int n = 0; n < children.getLength(); n++)
         {
             Node node = children.item(n);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             String childName = node.getNodeName();
             if(childName.startsWith(DialogField.FIELDTAGPREFIX))
             {
                 Element fieldElem = (Element) node;
                 DialogField field = DialogFieldFactory.createField(childName);
                 if(field != null)
                     field.importFromXml(fieldElem);
                 else
                     field = new SelectField("error", "Unable to create field of type '" + childName, SelectField.SELECTSTYLE_COMBO, ValueSourceFactory.getListValueSource("dialog-field-types:"));
                 addField(field);
             }
             else if(childName.equals("populate-tasks"))
             {
                 if(populateTasks == null)
                     populateTasks = new BasicTask();
 
                 try
                 {
                     Task populateTask = new BasicTask();
                     populateTask.initialize((Element) node);
                     if(!populateTask.isValid())
                     {
                         for(Iterator i = populateTask.getInitErrors().iterator(); i.hasNext();)
                         {
                             addField(new StaticField("error in populate-tasks", (String) i.next()));
                         }
                     }
                     else
                         populateTasks.addChildTask(populateTask);
                 }
                 catch(TaskInitializeException e)
                 {
                     addField(new StaticField("exception in populate-tasks", e.toString()));
                     LogManager.recordException(this.getClass(), "importFromXml", "unable to initalize populate tasks", e);
                 }
             }
             else if(childName.equals("execute-tasks"))
             {
                 if(executeTasks == null)
                     executeTasks = new BasicTask();
                 try
                 {
                     Task executeTask = new BasicTask();
                     executeTask.initialize((Element) node);
                     if(!executeTask.isValid())
                     {
                         for(Iterator i = executeTask.getInitErrors().iterator(); i.hasNext();)
                         {
                             addField(new StaticField("error in execute-tasks", (String) i.next()));
                         }
                     }
                     else
                         executeTasks.addChildTask(executeTask);
                 }
                 catch(TaskInitializeException e)
                 {
                     addField(new StaticField("exception in execute-tasks", e.toString()));
                     LogManager.recordException(this.getClass(), "importFromXml", "unable to initalize execute tasks", e);
                 }
             }
             else if(childName.equals("director"))
             {
                 director.importFromXml((Element) node);
             }
             else if(childName.equals("client-js"))
             {
                 Element jsElem = (Element) node;
                 String hrefStr = jsElem.getAttribute("href");
                 this.includeJSFile = (hrefStr != null ? ValueSourceFactory.getSingleOrStaticValueSource(hrefStr): null);
             }
         }
 
         Iterator j = fields.iterator();
         while(j.hasNext())
         {
             DialogField field = (DialogField) j.next();
             field.finalizeQualifiedName(this);
         }
     }
 
     /**
      * Clear all the dialog fields. Also clear the <code>DLGFLAG_CONTENTS_FINALIZED</code> flag.
      */
     public void clearFields()
     {
         fields.clear();
         clearFlag(DLGFLAG_CONTENTS_FINALIZED);
     }
 
     /**
      * Add a dialog field
      *
      * @param field datlog field
      */
     public void addField(DialogField field)
     {
         fields.add(field);
     }
 
     /**
      * Get the dialog field by its qualified name
      *
      * @param qualifiedName  qualified field name
      * @return DialogField
      */
     public DialogField findField(String qualifiedName)
     {
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             DialogField found = field.findField(qualifiedName);
             if(found != null)
                 return found;
         }
 
         return null;
     }
 
     /**
      * Loops through each dialog field and finalize them.
      */
     public void finalizeContents(ServletContext context)
     {
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             field.finalizeContents(this);
 
             if(field.requiresMultiPartEncoding())
                 setFlag(DLGFLAG_ENCTYPE_MULTIPART_FORMDATA);
 
             if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE | DialogField.FLDFLAG_COLUMN_BREAK_AFTER))
                 layoutColumnsCount++;
         }
         setFlag(DLGFLAG_CONTENTS_FINALIZED);
     }
 
     /**
      * Process the tasks that has been registered for populating the dialog fields
      * These are the tasks that has been registered using the &lt;populate-tasks&gt; XML tag.
      *
      * @param dc dialog context
      *
      */
     public void processPopulateTasks(DialogContext dc)
     {
         if(populateTasks != null && populateTasks.isValid())
         {
             TaskContext tc = new TaskContext(dc);
 
             if(populateTasks.allowExecute(tc))
             {
                 try
                 {
                     populateTasks.execute(tc);
                     if(tc.hasError())
                         dc.addErrorMessage(tc.getErrorMessage());
                 }
                 catch(TaskExecuteException e)
                 {
                     dc.addErrorMessage(e.getMessage());
                     LogManager.recordException(this.getClass(), "processPopulateTasks", "unable to execute populate tasks", e);
                 }
             }
         }
     }
 
     /**
      * Process the tasks that has been configured as execution actions of the dialog.
      * These are the tasks that has been configured using the &lt;execute-tasks&gt; XML Tag.
      *
      * @param writer    output stream for error messages
      * @param dc        dialog context
      */
     public void processExecuteTasks(Writer writer, DialogContext dc) throws IOException
     {
         if(executeTasks != null && executeTasks.isValid())
         {
             TaskContext tc = new TaskContext(dc);
 
             int numTasksExecuted = 0;
             Logger logger = AppServerLogger.getLogger(LogManager.DEBUG_PAGE);
             if(logger.isDebugEnabled())
             {
                 logger.debug("Current data-cmd is " + dc.getDataCommandText(false));
                 logger.debug(executeTasks.getDebugHtml(tc));
             }
 
             if(executeTasks.allowExecute(tc))
             {
                 try
                 {
                     int tasksExecutedBefore = tc.getCountOfTasksExecuted();
                     executeTasks.execute(tc);
                     numTasksExecuted = tc.getCountOfTasksExecuted() - tasksExecutedBefore;
                 }
                 catch(TaskExecuteException e)
                 {
                     dc.setExecuteStageHandled(true);
                     writer.write("<pre>" + e.getDetailedMessage() + "</pre>");
                     LogManager.recordException(this.getClass(), "processExecuteTasks", "unable to execute tasks", e);
                 }
             }
 
             if(numTasksExecuted > 0)
             {
                 dc.setExecuteStageHandled(true);
                 if(tc.hasError())
                     writer.write(tc.getErrorMessage());
                 else if(tc.hasResultMessage())
                     writer.write(tc.getResultMessage());
             }
         }
     }
 
     /**
      * Populate the dialog with field values.
      * This should be called everytime the dialog is loaded except when it is ready for
      * execution (validated already)
      */
     public void populateValues(DialogContext dc, int formatType)
     {
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             if(field.isVisible(dc))
                 field.populateValue(dc, formatType);
         }
 
         if(director != null)
         {
             DialogField field = director.getNextActionsField();
             if(field != null)
                 field.populateValue(dc, formatType);
         }
 
         if(dc.isInitialEntry())
             processPopulateTasks(dc);
     }
 
     /**
      * Checks each field to make sure the state of it needs to be changed or not
      * usually based on Conditionals.
      *
      * <b>IMPORTANT</b>: If any changes are made in this class, make sure
      * that they are also reflected in QuerySelectDialog and QueryBuilderDialog classes
      * which extend this class but they overwrite this method and doesn't make a call
      * to this method.
      */
     public void makeStateChanges(DialogContext dc, int stage)
     {
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             field.makeStateChanges(dc, stage);
         }
         DialogDirector director = getDirector();
         if(director != null)
             director.makeStateChanges(dc, stage);
     }
 
     /**
      * Execute the actions of the dialog
      * @param writer output stream for error message
      * @param dc dialog context
      */
     public void execute(Writer writer, DialogContext dc) throws IOException
     {
         processExecuteTasks(writer, dc);
 
         if(! dc.executeStageHandled())
         {
             writer.write("Need to add Dialog actions or override Dialog.execute(DialogContext)." + dc.getDebugHtml());
             dc.setExecuteStageHandled(true);
         }
     }
 
     public void handlePostExecute(Writer writer, DialogContext dc) throws IOException
     {
        dc.setExecuteStageHandled(true);
         dc.performDefaultRedirect(writer);
     }
 
    public void handlePostExecuteException(Writer writer, DialogContext dc, String message, Exception e) throws IOException
    {
        dc.setExecuteStageHandled(true);
        LogManager.recordException(this.getClass(), "handlePostExecuteException", message, e);
        dc.setRedirectDisabled(true);
        dc.performDefaultRedirect(writer);
        writer.write(message + e.toString());
    }

     /**
      * Create a dialog context for this dialog
      *
      * @param context   servlet context
      * @param servlet   Servlet object
      * @param request   Http servlet request
      * @param response  Http servlet response
      * @param skin      dialog skin
      *
      * @return DialogContext
      */
     public DialogContext createContext(ServletContext context, Servlet servlet, HttpServletRequest request, HttpServletResponse response, DialogSkin skin)
     {
         if(!flagIsSet(DLGFLAG_CONTENTS_FINALIZED))
             finalizeContents(context);
 
         DialogContext dc = null;
         try
         {
             dc = (DialogContext) dcClass.newInstance();
         }
         catch(Exception e)
         {
             dc = new DialogContext();
         }
         dc.initialize(context, servlet, request, response, this, skin);
         return dc;
     }
 
     /**
      * Initially populates the dialog with values in display format and then calculates the state of the dialog.
      * If the dialog is in execute mode, the values are then formatted for submittal.
      *
      * @param dc dialog context
      */
     public void prepareContext(DialogContext dc)
     {
         populateValues(dc, DialogField.DISPLAY_FORMAT);
         dc.calcState();
         // validated and the dialog is ready for execution
         if(dc.inExecuteMode())
         {
             dc.persistValues();
             this.populateValues(dc, DialogField.SUBMIT_FORMAT);
         }
 
     }
 
     /**
      * Create and write the HTML for the dialog
      *
      * @param writer                    stream to write the HTML
      * @param dc                        dialog context
      * @param contextPreparedAlready    flag to indicate whether or not the context has been prepared
      */
     public void renderHtml(Writer writer, DialogContext dc, boolean contextPreparedAlready) throws IOException
     {
         if(!contextPreparedAlready)
             prepareContext(dc);
 
         if(dc.inExecuteMode())
         {
             // "looping" means to keep the dialog on the screen even after
             // the "execute" phase has been reached -- useful for lookups, etc
             if(loopEntries())
             {
                 if(appendAfterLoop())
                 {
                     execute(writer, dc);
                     writer.write(loopSeparator);
                     dc.getSkin().renderHtml(writer, dc);
                 }
                 else
                 {
                     dc.getSkin().renderHtml(writer, dc);
                     writer.write(loopSeparator);
                     execute(writer, dc);
                 }
             }
             else
                 execute(writer, dc);
         }
         else
         {
             dc.getSkin().renderHtml(writer, dc);
         }
     }
 
     /**
      * Create and write the HTML for the dialog. This method calls <code>renderHtml(Writer writer, DialogContext dc, boolean contextPreparedAlready)</code>
      * with the context flag set to <code>false</code>.
      *
      * @param context servlet context
      * @param servlet servlet object
      * @param request Http servlet request
      * @param response Http servlet response
      * @param skin dialog skin
      */
     public void renderHtml(ServletContext context, Servlet servlet, HttpServletRequest request, HttpServletResponse response, DialogSkin skin) throws IOException
     {
         DialogContext dc = createContext(context, servlet, request, response, skin);
         renderHtml(response.getWriter(), dc, false);
     }
 
     public String getSubclassedDialogContextCode(String pkgPrefix)
     {
         StringBuffer importsCode = new StringBuffer();
         StringBuffer membersCode = new StringBuffer();
 
         Set modulesImported = new HashSet();
 
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             DialogContextMemberInfo mi = field.getDialogContextMemberInfo();
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
         }
 
         StringBuffer code = new StringBuffer();
         code.append("\n/* this file is generated by com.netspective.sparx.xaf.form.Dialog.getSubclassedDialogContextCode(), do not modify (you can extend it, though) */\n\n");
         code.append("package " + pkgPrefix + pkgName + ";\n\n");
         if(importsCode.length() > 0)
             code.append(importsCode.toString());
         code.append("import com.netspective.sparx.xaf.form.*;\n\n");
         code.append("public class " + com.netspective.sparx.util.xml.XmlSource.xmlTextToJavaIdentifier(getNameFromXml(), true) + "Context extends DialogContext\n");
         code.append("{\n");
         code.append(membersCode.toString());
         code.append("}\n");
         return code.toString();
     }
 
     /**
      * Indicates whether not the dialog needs validation
      *
      * @param dc dialog context
      * @return boolean
      */
     public boolean needsValidation(DialogContext dc)
     {
         int validateFieldsCount = 0;
 
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
             if(field.isVisible(dc) && field.needsValidation(dc))
                 validateFieldsCount++;
         }
 
         return validateFieldsCount > 0 ? true : false;
     }
 
     /**
      * Checks whether or not the dailog is valid for the execution
      *
      * @param dc dialog context
      * @return boolean
      */
     public boolean isValid(DialogContext dc)
     {
         int valStage = dc.getValidationStage();
         if(valStage == DialogContext.VALSTAGE_PERFORMED_SUCCEEDED || valStage == DialogContext.VALSTAGE_IGNORE)
             return true;
         if(valStage == DialogContext.VALSTAGE_PERFORMED_FAILED)
             return false;
 
         int invalidFieldsCount = 0;
 
         Iterator i = fields.iterator();
         while(i.hasNext())
         {
             DialogField field = (DialogField) i.next();
            if((field.isVisible(dc) && !field.isInputHidden(dc)) && (!field.isValid(dc)))
                 invalidFieldsCount++;
         }
 
         if(dc.getErrorMessages() != null)
             invalidFieldsCount++;
 
         boolean isValid = invalidFieldsCount == 0 ? true : false;
         dc.setValidationStage(isValid ? DialogContext.VALSTAGE_PERFORMED_SUCCEEDED : DialogContext.VALSTAGE_PERFORMED_FAILED);
         return isValid;
     }
 }
