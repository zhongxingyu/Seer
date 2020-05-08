 package com.xaf.form;
 
 import java.io.*;
 import java.util.*;
 import java.lang.reflect.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.w3c.dom.*;
 import com.xaf.form.field.*;
 import com.xaf.task.*;
 import com.xaf.value.*;
 
 public class Dialog
 {
     // NOTE: when adding new flags, make sure to create them before the
     // last DLGFLAG_CUSTOM_START entry. This is because QueryBuilderDialog
     // extends this class and has additional flags that is based on the value
     // of DLGFLAG_CUSTOM_START.
 	static public final int DLGFLAG_CONTENTS_FINALIZED         = 1;
 	static public final int DLGFLAG_RETAIN_ALL_REQUEST_PARAMS  = DLGFLAG_CONTENTS_FINALIZED * 2;
 	static public final int DLGFLAG_LOOP_DATA_ENTRY            = DLGFLAG_RETAIN_ALL_REQUEST_PARAMS * 2;
 	static public final int DLGFLAG_APPEND_WHEN_LOOPING        = DLGFLAG_LOOP_DATA_ENTRY * 2;
    	static public final int DLGFLAG_HIDE_READONLY_HINTS        = DLGFLAG_APPEND_WHEN_LOOPING * 2;
     static public final int DLGFLAG_ENCTYPE_MULTIPART_FORMDATA = DLGFLAG_HIDE_READONLY_HINTS * 2;
 	static public final int DLGFLAG_CUSTOM_START               = DLGFLAG_ENCTYPE_MULTIPART_FORMDATA * 2;
 
 	static public final String PARAMNAME_AUTOEXECUTE   = "_d_exec";
 	static public final String PARAMNAME_DIALOGPREFIX  = "_d.";
 	static public final String PARAMNAME_CONTROLPREFIX = "_dc.";
 	static public final String PARAMNAME_DIALOGQNAME   = "_d.dialog_qname";
 
 	static public final String PARAMNAME_ACTIVEMODE    = ".active_mode";
 	static public final String PARAMNAME_NEXTMODE      = ".next_mode";
 	static public final String PARAMNAME_RUNSEQ        = ".run_sequence";
 	static public final String PARAMNAME_EXECSEQ       = ".exec_sequence";
 	static public final String PARAMNAME_ORIG_REFERER  = ".orig_referer";
 	static public final String PARAMNAME_TRANSACTIONID = ".transaction_id";
 	static public final String PARAMNAME_RESETCONTEXT  = ".reset_context";
 
 	/*
 	   the Data Command when first passed in (start of dialog, run seq == 1)
 	   is passed using the parameter "data_cmd" (INITIAL). When the dialog is
 	   in run sequence > 1 (after submit) the data command is passed in as a
 	   hidden "pass-thru" variable with the suffix PARAMNAME_DATA_CMD
     */
 	static public final String PARAMNAME_DATA_CMD_INITIAL  = "data_cmd";
 	static public final String PARAMNAME_DATA_CMD          = ".data_cmd";
 	static public final String PARAMVALUE_DATA_CMD_ADD     = "add";
 	static public final String PARAMVALUE_DATA_CMD_EDIT    = "edit";
 	static public final String PARAMVALUE_DATA_CMD_DELETE  = "delete";
 	static public final String PARAMVALUE_DATA_CMD_CONFIRM = "confirm";
 
 	private ArrayList fields = new ArrayList();
 	private int flags;
     private Task populateTasks;
     private Task executeTasks;
 	private DialogDirector director;
 	private String pkgName;
 	private String nameFromXml;
 	private String name;
 	private SingleValueSource heading;
 	private String actionURL;
 	private String loopSeparator = "<p>";
 	private int layoutColumnsCount = 1;
 	private String[] retainRequestParams;
 	private Class dcClass = DialogContext.class;
     private Class directorClass = DialogDirector.class;
     private SingleValueSource includeJSFile = null;
 
 	public Dialog()
 	{
 		layoutColumnsCount = 1;
 		flags = (DLGFLAG_LOOP_DATA_ENTRY | DLGFLAG_APPEND_WHEN_LOOPING);
 	}
 
 	public Dialog(String aName, String aHeading)
 	{
 		name = aName;
 		setHeading(aHeading);
 		layoutColumnsCount = 1;
 		flags = (DLGFLAG_LOOP_DATA_ENTRY | DLGFLAG_APPEND_WHEN_LOOPING);
 	}
 
 	public String getName() { return name; }
 	public String getNameFromXml() { return nameFromXml; }
 	public void setName(String newName) { name = newName; }
 
 	public final long getFlags() { return flags; }
 	public final boolean flagIsSet(long flag) { return (flags & flag) == 0 ? false : true; }
 	public final void setFlag(long flag) {	flags |= flag; }
 	public final void clearFlag(long flag) { flags &= ~flag; }
 
 	public SingleValueSource getHeading() { return heading; }
 	public void setHeading(String value) { heading = ValueSourceFactory.getSingleOrStaticValueSource(value); }
 	public void setHeading(SingleValueSource vs) { heading = vs; }
 
 	public final boolean loopEntries() { return flagIsSet(DLGFLAG_LOOP_DATA_ENTRY); }
 	public final void setLoopEntries(boolean value) { if(value) setFlag(DLGFLAG_LOOP_DATA_ENTRY); else clearFlag(DLGFLAG_LOOP_DATA_ENTRY); }
 	public final boolean appendAfterLoop() { return flagIsSet(DLGFLAG_APPEND_WHEN_LOOPING); }
 	public final void setAppendAfterLoop(boolean value) { if(value) setFlag(DLGFLAG_APPEND_WHEN_LOOPING); else clearFlag(DLGFLAG_APPEND_WHEN_LOOPING); }
 
 	public final int getLayoutColumnsCount() { return layoutColumnsCount; }
     public final String getLoopSeparator() { return loopSeparator; }
 
 	public final String getOriginalRefererParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_ORIG_REFERER; }
 	public final String getActiveModeParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_ACTIVEMODE; }
 	public final String getNextModeParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_NEXTMODE; }
 	public final String getRunSequenceParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_RUNSEQ; }
 	public final String getExecuteSequenceParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_EXECSEQ; }
 	public final String getTransactionIdParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_TRANSACTIONID; }
 	public final String getResetContextParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_RESETCONTEXT; }
 	public final String getValuesRequestAttrName() { return "dialog-" + name + "-field-values"; }
 	public final String getDataCmdParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_DATA_CMD; }
 
 	public final Task getPopulateTasks() { return populateTasks; }
     public final Task getExecuteTasks() { return executeTasks; }
 
 	public final List getFields() { return fields; }
 	public final boolean retainRequestParams() { return flagIsSet(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS) || (retainRequestParams != null); }
 	public final String[] getRetainRequestParams() { return retainRequestParams; }
 	public final void setRetainRequestParams(String[] value) { retainRequestParams = value; }
 	public final void setRetainAllRequestParams(boolean value) { if(value) setFlag(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS); else clearFlag(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS); }
 	public final boolean retainAllRequestParams() { return flagIsSet(DLGFLAG_RETAIN_ALL_REQUEST_PARAMS); }
 
 	public final DialogDirector getDirector() { return director; }
 	public void setDirector(DialogDirector value) { director = value; }
     public void setDialogDirectorClass(Class cls) { directorClass = cls; }
 
     public SingleValueSource getIncludeJSFile()
     {
         return includeJSFile;
     }
 
     public static Class findDialogContextClass(String packageName, Element elem) throws ClassNotFoundException
 	{
 		Class dcClass = null;
 
 		// see if this dialog is using a special context (bean?) class
 		// if a specific dc-class is provided, use it or try and find one
 		// with the same name as the dialog in "dialog.context." package
 		String dcClassName = elem.getAttribute("dc-class");
 
 		if(dcClassName != null && dcClassName.length() > 0)
 			dcClass = Class.forName(dcClassName);
 		else
 		{
 			String elemName = com.xaf.xml.XmlSource.xmlTextToJavaIdentifier(elem.getAttribute("name"), true);
 			String dlgName = packageName != null ? (packageName + "." + elemName) : elemName;
 			dcClassName = "dialog.context." + dlgName + "Context";
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
 
 	public void importFromXml(String packageName, Element elem)
 	{
 		pkgName = packageName;
 		nameFromXml = elem.getAttribute("name");
 		name = nameFromXml;
 
 		// we delimit with "_" instead of "." because the dialog name must
 		// be a valid single JavaScript token (to make it easy for the skin)
 		if(packageName != null)
 			name = packageName + "_" + name;
 
         if(! Character.isJavaIdentifierStart(name.charAt(0)))
             throw new RuntimeException("Dialog name '"+name+" may contain only digits, letters, and _.'");
 
         for(int c = 1; c < name.length(); c++)
         {
             if(! Character.isJavaIdentifierPart(name.charAt(c)))
                 throw new RuntimeException("Dialog name '"+name+" may contain only digits, letters, and _.'");
         }
 
 		String headingVS = elem.getAttribute("heading");
 		if(headingVS.length() > 0)
 			setHeading(headingVS);
 
 		String loop = elem.getAttribute("loop");
 		if(loop.equals("no"))
 			clearFlag(DLGFLAG_LOOP_DATA_ENTRY);
 
 		String hideHints = elem.getAttribute("hide-readonly-hints");
 		if(hideHints.equals("yes"))
 			setFlag(DLGFLAG_HIDE_READONLY_HINTS);
 
         if(director == null)
         {
             try
             {
                 director = (DialogDirector) directorClass.newInstance();
             }
             catch(Exception e)
             {
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
                     if(! populateTask.isValid())
                     {
                         for(Iterator i = populateTask.getInitErrors().iterator(); i.hasNext(); )
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
                     if(! executeTask.isValid())
                     {
                         for(Iterator i = executeTask.getInitErrors().iterator(); i.hasNext(); )
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
                 }
             }
 			else if(childName.equals("director"))
 			{
 				director.importFromXml((Element) node);
 			}
             else if (childName.equals("client-js"))
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
 
 	public void clearFields()
 	{
 		fields.clear();
 		clearFlag(DLGFLAG_CONTENTS_FINALIZED);
 	}
 
 	public void addField(DialogField field)
 	{
         if(field instanceof FileField)
             setFlag(DLGFLAG_ENCTYPE_MULTIPART_FORMDATA);
 		fields.add(field);
 	}
 
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
 
 	public void finalizeContents()
 	{
 		Iterator i = fields.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			field.finalizeContents(this);
 
 			if(field.flagIsSet(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE | DialogField.FLDFLAG_COLUMN_BREAK_AFTER))
 			{
 				layoutColumnsCount++;
 			}
 		}
 		setFlag(DLGFLAG_CONTENTS_FINALIZED);
 	}
 
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
                 }
             }
         }
     }
 
     public String processExecuteTasks(DialogContext dc)
     {
         if(executeTasks != null && executeTasks.isValid())
         {
 			TaskContext tc = new TaskContext(dc);
 
             int numTasksExecuted = 0;
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
                     return "<pre>" + e.getDetailedMessage() + "</pre>";
                 }
             }
 
             if(numTasksExecuted > 0)
             {
                 dc.setExecuteStageHandled(true);
                 if(tc.hasError())
                     return tc.getErrorMessage();
                 else if(tc.hasResultMessage())
                     return tc.getResultMessage();
                 else
                     return "";
             }
         }
 
         return null;
     }
 
     /**
      * Populate the dialog with field values. If listeners are defined, execute them also.
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
 
         if(dc.isInitialEntry())
             processPopulateTasks(dc);
 
 		List listeners = dc.getListeners();
 		for(int l = 0; l < listeners.size(); l++)
 			((DialogContextListener) listeners.get(l)).populateDialogData(dc);
 	}
 
 	public void makeStateChanges(DialogContext dc, int stage)
 	{
         Iterator i = fields.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
             field.makeStateChanges(dc, stage);
 		}
 
 		List listeners = dc.getListeners();
 		for(int l = 0; l < listeners.size(); l++)
 			((DialogContextListener) listeners.get(l)).makeDialogContextChanges(dc, stage);
 	}
 
 	public String execute(DialogContext dc)
 	{
         String execResults = processExecuteTasks(dc);
         if(execResults != null)
             return execResults;
 
 		List listeners = dc.getListeners();
 		if(listeners.size() > 0)
 		{
 			StringBuffer result = new StringBuffer();
 			for(int i = 0; i < listeners.size(); i++)
 			{
 				String execStr = ((DialogContextListener) listeners.get(i)).executeDialog(dc);
 				if(execStr != null)
 					result.append(execStr);
 			}
 			return result.toString();
 		}
 		else
         {
             return "Need to add Dialog actions, provide listener, or override Dialog.execute(DialogContext)." + dc.getDebugHtml();
         }
 	}
 
 	public DialogContext createContext(ServletContext context, Servlet servlet, HttpServletRequest request, HttpServletResponse response, DialogSkin skin)
 	{
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
 
 	public void prepareContext(DialogContext dc)
 	{
 		if(! flagIsSet(DLGFLAG_CONTENTS_FINALIZED))
 		    finalizeContents();
 		populateValues(dc, DialogField.DISPLAY_FORMAT);
         dc.calcState();
         // validated and the dialog is ready for execution
         if (dc.inExecuteMode())
             this.populateValues(dc, DialogField.SUBMIT_FORMAT);
 
 	}
 
 	public String getHtml(DialogContext dc, boolean contextPreparedAlready)
 	{
 		if(! contextPreparedAlready)
 			prepareContext(dc);
 
 		if(dc.inExecuteMode())
 		{
 			// "looping" means to keep the dialog on the screen even after
 			// the "execute" phase has been reached -- useful for lookups, etc
 			if(loopEntries())
 			{
 				StringBuffer html = new StringBuffer();
 				if(appendAfterLoop())
 				{
 					html.append(execute(dc));
 					html.append(loopSeparator);
 					html.append(dc.getSkin().getHtml(dc));
 				}
 				else
 				{
 					html.append(dc.getSkin().getHtml(dc));
 					html.append(loopSeparator);
 					html.append(execute(dc));
 				}
 				return html.toString();
 			}
 			else
 				return execute(dc);
 		}
 		else
 		{
 			return dc.getSkin().getHtml(dc);
 		}
 	}
 
 	public String getHtml(ServletContext context, Servlet servlet, HttpServletRequest request, HttpServletResponse response, DialogSkin skin)
 	{
 		DialogContext dc = createContext(context, servlet, request, response, skin);
 		return getHtml(dc, false);
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
                         if(! modulesImported.contains(module))
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
 		code.append("\n/* this file is generated by com.xaf.form.Dialog.getSubclassedDialogContextCode(), do not modify (you can extend it, though) */\n\n");
 		code.append("package " + pkgPrefix + pkgName + ";\n\n");
         if(importsCode.length() > 0)
             code.append(importsCode.toString());
 		code.append("import com.xaf.form.*;\n\n");
 		code.append("public class " + com.xaf.xml.XmlSource.xmlTextToJavaIdentifier(getNameFromXml(), true) + "Context extends DialogContext\n");
 		code.append("{\n");
         code.append(membersCode.toString());
 		code.append("}\n");
 		return code.toString();
 	}
 
 	public boolean needsValidation(DialogContext dc)
 	{
 		int validateFieldsCount = 0;
 
 		List listeners = dc.getListeners();
 		for(int l = 0; l < listeners.size(); l++)
 		{
 			if(((DialogContextListener) listeners.get(l)).dialogNeedsValidation(dc))
 				validateFieldsCount++;
 		}
 
 		Iterator i = fields.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc) && field.needsValidation(dc))
 				validateFieldsCount++;
 		}
 
 		return validateFieldsCount > 0 ? true : false;
 	}
 
 	public boolean isValid(DialogContext dc)
 	{
 		int valStage = dc.getValidationStage();
 		if(valStage == DialogContext.VALSTAGE_PERFORMED_SUCCEEDED)
 			return true;
 		if(valStage == DialogContext.VALSTAGE_PERFORMED_FAILED)
 			return false;
 
 		int invalidFieldsCount = 0;
 
 		List listeners = dc.getListeners();
 		for(int l = 0; l < listeners.size(); l++)
 		{
 			if(! ((DialogContextListener) listeners.get(l)).isDialogValid(dc, false))
 				invalidFieldsCount++;
 		}
 
 		Iterator i = fields.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc) && (! field.isValid(dc)))
 				invalidFieldsCount++;
 		}
 
 		for(int l = 0; l < listeners.size(); l++)
 		{
 			if(! ((DialogContextListener) listeners.get(l)).isDialogValid(dc, true))
 				invalidFieldsCount++;
 		}
 
         if(dc.getErrorMessages() != null)
             invalidFieldsCount++;
 
 		boolean isValid = invalidFieldsCount == 0 ? true : false;
 		dc.setValidationStage(isValid ? DialogContext.VALSTAGE_PERFORMED_SUCCEEDED : DialogContext.VALSTAGE_PERFORMED_FAILED);
 		return isValid;
 	}
 }
