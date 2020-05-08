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
 
 	private ArrayList fields = new ArrayList();
     private Task[] executeTasks;
 	private DialogDirector director;
 	private String name;
 	private SingleValueSource heading;
 	private String actionURL;
 	private boolean loopDataEntry = true;
 	private boolean appendWhenLooping = true;
 	private String loopSeparator = "<p>";
 	private boolean contentsFinalized;
 	private int layoutColumnsCount = 1;
 	private boolean retainAllRequestParams;
 	private String[] retainRequestParams;
 
 	public Dialog()
 	{
 		layoutColumnsCount = 1;
 	}
 
 	public Dialog(String aName, String aHeading)
 	{
 		name = aName;
 		setHeading(aHeading);
 		layoutColumnsCount = 1;
 	}
 
 	public String getName() { return name; }
 	public void setName(String newName) { name = newName; }
 
 	public SingleValueSource getHeading() { return heading; }
 	public void setHeading(String value) { heading = ValueSourceFactory.getSingleOrStaticValueSource(value); }
 	public void setHeading(SingleValueSource vs) { heading = vs; }
 
 	public String getActionURL() { return actionURL; }
 	public void setActionURL(String newURL) { actionURL = newURL; }
 
 	public final boolean loopEntries() { return loopDataEntry; }
 	public final void setLoopEntries(boolean value) { loopDataEntry = value; }
 	public final boolean appendAfterLoop() { return appendWhenLooping; }
 	public final void setAppendAfterLoop(boolean value) { appendWhenLooping = value; }
 
 	public final int getLayoutColumnsCount() { return layoutColumnsCount; }
 
 	public final String getOriginalRefererParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_ORIG_REFERER; }
 	public final String getActiveModeParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_ACTIVEMODE; }
 	public final String getNextModeParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_NEXTMODE; }
 	public final String getRunSequenceParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_RUNSEQ; }
 	public final String getExecuteSequenceParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_EXECSEQ; }
 	public final String getTransactionIdParamName() { return PARAMNAME_DIALOGPREFIX + name + PARAMNAME_TRANSACTIONID; }
 	public final String getValuesRequestAttrName() { return "dialog-" + name + "-field-values"; }
 
 	public final ArrayList getFields() { return fields; }
 	public final boolean retainRequestParams() { return retainAllRequestParams || (retainRequestParams != null); }
 	public final String[] getRetainRequestParams() { return retainRequestParams; }
 	public final boolean retainAllRequestParams() { return retainAllRequestParams; }
 
 	public final DialogDirector getDirector() { return director; }
 	public void setDirector(DialogDirector value) { director = value; }
 
 	public void importFromXml(String packageName, Element elem)
 	{
 		name = elem.getAttribute("name");
 
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
 		if(loop.equals("yes"))
 			loopDataEntry = true;
 
 		if(director == null)
 			director = new DialogDirector();
 
 		String retainRequestParamsStr = elem.getAttribute("retain-params");
 		if(retainRequestParamsStr.length() > 0)
 		{
 			if(retainRequestParamsStr.equals("*"))
 				retainAllRequestParams = true;
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
 			retainAllRequestParams = false;
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
             else if(childName.equals("execute-tasks"))
             {
                 ArrayList tasksList = new ArrayList();
         		NodeList eaChildren = node.getChildNodes();
                 for(int eac = 0; eac < eaChildren.getLength(); eac++)
                 {
                     Node eaNode = eaChildren.item(eac);
                     if(eaNode.getNodeType() == Node.ELEMENT_NODE)
                     {
                         try
                         {
                             Task task = TaskFactory.getTask((Element) eaNode, true);
                             tasksList.add(task);
                         }
                         catch(Exception e)
                         {
                             addField(new StaticField("error", e.toString()));
                         }
                     }
                 }
                 if(tasksList.size() > 0)
                 {
                     executeTasks = (Task[]) tasksList.toArray(new Task[tasksList.size()]);
                 }
             }
 			else if(childName.equals("director"))
 			{
 				director.importFromXml((Element) node);
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
 		contentsFinalized = false;
 	}
 
 	public void addField(DialogField field)
 	{
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
 		contentsFinalized = true;
 	}
 
 	public void populateValues(DialogContext dc)
 	{
 		Iterator i = fields.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc))
 				field.populateValue(dc);
 		}
 	}
 
 	public void makeStateChanges(DialogContext dc, int stage)
 	{
 	}
 
 	public String execute(DialogContext dc)
 	{
         if(executeTasks != null && executeTasks.length > 0)
         {
 			TaskContext tc = new TaskContext(dc);
 
 			try
 			{
 				for(int i = 0; i < executeTasks.length; i++)
 				{
 					executeTasks[i].execute(tc);
 					if(tc.haltProcessing())
 						break;
 				}
 				if(tc.hasError())
 					return tc.getErrorMessage();
 				else if(tc.hasResultMessage())
 					return tc.getResultMessage();
 				else
 					return "";
 			}
 			catch(TaskExecuteException e)
 			{
 				return e.getMessage();
 			}
         }
 
 		return "Need to add Dialog actions or override Dialog.execute(DialogContext)." + dc.getDebugHtml();
 	}
 
 	public void prepareContext(DialogContext dc)
 	{
 		if(! contentsFinalized)
 		    finalizeContents();
 
 		populateValues(dc);
 		dc.calcState();
 	}
 
 	public String getHtml(DialogContext dc, boolean contextPreparedAlready)
 	{
 		if(! contextPreparedAlready)
 			prepareContext(dc);
 
 		if(dc.inExecuteMode())
 		{
 			// "looping" means to keep the dialog on the screen even after
 			// the "execute" phase has been reached -- useful for lookups, etc
 			if(loopDataEntry)
 			{
 				StringBuffer html = new StringBuffer();
 				if(appendWhenLooping)
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
 			return dc.getSkin().getHtml(dc);
 	}
 
 	public String getHtml(HttpServletRequest request, HttpServletResponse response, ServletContext context, DialogSkin skin)
 	{
 		DialogContext dc = new DialogContext(request, response, context, this, skin);
 		return getHtml(dc, false);
 	}
 
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
 
 	public boolean isValid(DialogContext dc)
 	{
 		int valStage = dc.getValidationStage();
 		if(valStage == DialogContext.VALSTAGE_PERFORMED_SUCCEEDED)
 			return true;
 		if(valStage == DialogContext.VALSTAGE_PERFORMED_FAILED)
 			return false;
 
 		int invalidFieldsCount = 0;
 		Iterator i = fields.iterator();
 		while(i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if(field.isVisible(dc) && (! field.isValid(dc)))
 				invalidFieldsCount++;
 		}
 
 		boolean isValid = invalidFieldsCount == 0 ? true : false;
 		dc.setValidationStage(isValid ? DialogContext.VALSTAGE_PERFORMED_SUCCEEDED : DialogContext.VALSTAGE_PERFORMED_FAILED);
 		return isValid;
 	}
 }
