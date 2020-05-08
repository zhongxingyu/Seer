 package com.xaf.form;
 
 import java.lang.reflect.*;
 import java.util.*;
 import java.security.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import com.xaf.db.*;
 import com.xaf.log.*;
 import com.xaf.sql.*;
 import com.xaf.value.*;
 import com.xaf.task.TaskExecuteException;
 import com.xaf.task.TaskContext;
 import com.xaf.task.sql.DmlTask;
 import com.xaf.task.sql.TransactionTask;
 
 public class DialogContext extends ServletValueContext
 {
     /* if dialog fields need to be pre-populated (before the context is created)
      * then a java.util.Map can be created and stored in the request attribute
      * called "dialog-field-values". All keys in the map are field names, and values
      * are the field values
      */
 	static public final String DIALOG_FIELD_VALUES_ATTR_NAME = "dialog-field-values";
 
     /* after the dialog context is created, it is automatically stored as
      * request parameter with this name so that it is available throughout the
      * request
      */
 	static public final String DIALOG_CONTEXT_ATTR_NAME = "dialog-context";
 
 	static public final SingleValueSource dialogFieldStoreValueSource = new com.xaf.value.DialogFieldValue();
 
 	public class DialogFieldState
 	{
 		public DialogField field;
 		public String value;
 		public String[] values;
 		public long flags;
 		public ArrayList errorMessages;
 
 		DialogFieldState(DialogField aField, int dataCmd)
 		{
 			field = aField;
 			flags = field.getFlags();
 
 			switch(dataCmd)
 			{
 				case DATA_CMD_NONE:
 				case DATA_CMD_ADD:
 					break;
 
 				case DATA_CMD_EDIT:
 					// when in "edit" mode, the primary key should be read-only
 					if((flags & DialogField.FLDFLAG_PRIMARYKEY) != 0)
 						flags |= DialogField.FLDFLAG_READONLY;
 					break;
 
 				case DATA_CMD_CONFIRM:
 				case DATA_CMD_DELETE:
 					// when in "delete" mode, all the fields should be read-only
 					flags |= DialogField.FLDFLAG_READONLY;
 					break;
 			}
 		}
 	}
 
 	static public final char DIALOGMODE_UNKNOWN  = ' ';
 	static public final char DIALOGMODE_INPUT    = 'I';
 	static public final char DIALOGMODE_VALIDATE = 'V';
 	static public final char DIALOGMODE_EXECUTE  = 'E';
 
     /**
      * The following constants are setup as flags but are most often used as enumerations. We use powers of two to
      * allow them to be used either as enums or as flags.
      */
 	static public final int DATA_CMD_NONE    = 0;
 	static public final int DATA_CMD_ADD     = 1;
 	static public final int DATA_CMD_EDIT    = DATA_CMD_ADD * 2;
 	static public final int DATA_CMD_DELETE  = DATA_CMD_EDIT * 2;
 	static public final int DATA_CMD_CONFIRM = DATA_CMD_DELETE * 2;
     static public final int DATA_CMD_CUSTOM_START = DATA_CMD_CONFIRM * 2;
 
 	static public final int VALSTAGE_NOT_PERFORMED       = 0;
 	static public final int VALSTAGE_PERFORMED_FAILED    = 1;
 	static public final int VALSTAGE_PERFORMED_SUCCEEDED = 2;
 
 	static public final int STATECALCSTAGE_INITIAL = 0;
 	static public final int STATECALCSTAGE_FINAL   = 1;
 
     private Map fieldStates = new HashMap();
 	private List listeners = new ArrayList();
     private ArrayList dlgErrorMessages;
 	private boolean resetContext;
 	private String transactionId;
 	private Dialog dialog;
 	private DialogSkin skin;
 	private char activeMode;
 	private char nextMode;
 	private int runSequence;
     private int execSequence;
 	private int validationStage;
 	private String originalReferer;
 	private DatabaseContext dbContext;
 	private boolean executeHandled;
 	private String dataCmdStr;
 	private int dataCmd;
 	private String[] retainReqParams;
     private int errorsCount;
 
 	public DialogContext()
 	{
 	}
 
 	public void initialize(ServletContext aContext, Servlet aServlet, HttpServletRequest aRequest, HttpServletResponse aResponse, Dialog aDialog, DialogSkin aSkin)
 	{
 		AppServerCategory monitorLog = (AppServerCategory) AppServerCategory.getInstance(LogManager.MONITOR_PAGE);
 		long startTime = 0;
 		if(monitorLog.isInfoEnabled())
 			startTime = new Date().getTime();
 
         super.initialize(aContext, aServlet, aRequest, aResponse);
         aRequest.setAttribute(DIALOG_CONTEXT_ATTR_NAME, this);
 
 		if(servlet instanceof DialogContextListener)
 			listeners.add(servlet);
 
 		dialog = aDialog;
 		skin = aSkin;
 		activeMode = DIALOGMODE_UNKNOWN;
 		nextMode = DIALOGMODE_UNKNOWN;
 		validationStage = VALSTAGE_NOT_PERFORMED;
 		String runSeqValue = request.getParameter(dialog.getRunSequenceParamName());
 		if(runSeqValue != null)
 			runSequence = new Integer(runSeqValue).intValue();
 		else
 			runSequence = 1;
 
 		String execSeqValue = request.getParameter(dialog.getExecuteSequenceParamName());
 		if(execSeqValue != null)
 			execSequence = new Integer(execSeqValue).intValue();
 		else
 			execSequence = 0; // we have not executed at all yet
 
 		String resetContext = request.getParameter(dialog.getResetContextParamName());
 		if(resetContext != null)
 		{
 			runSequence = 1;
 			execSequence = 0;
 			this.resetContext = true;
 		}
 
 		if(runSequence == 1)
 		{
 			originalReferer = aRequest.getHeader("Referer");
 			try
 			{
 				MessageDigest md = MessageDigest.getInstance("MD5");
 	    		md.update((dialog.getName() + new Date().toString()).getBytes());
 		    	transactionId = md.digest().toString();
 			}
 			catch(NoSuchAlgorithmException e)
 			{
 				transactionId = "No MessageDigest Algorithm found!";
 			}
             dataCmdStr = (String) aRequest.getAttribute(Dialog.PARAMNAME_DATA_CMD_INITIAL);
             if(dataCmdStr == null)
                 dataCmdStr = aRequest.getParameter(Dialog.PARAMNAME_DATA_CMD_INITIAL);
 		}
 		else
 		{
 			originalReferer = aRequest.getParameter(dialog.getOriginalRefererParamName());
 			transactionId = aRequest.getParameter(dialog.getTransactionIdParamName());
 			dataCmdStr = aRequest.getParameter(dialog.getDataCmdParamName());
 		}
 
         dataCmd = getDataCmdIdForCmdText(dataCmdStr);
 		createStateFields(dialog.getFields());
 
 		DialogDirector director = dialog.getDirector();
 		if(director != null)
 		{
 			String qName = director.getQualifiedName();
 			if(qName != null)
 				fieldStates.put(qName, new DialogFieldState(director, dataCmd));
 		}
 
 		LogManager.recordAccess(aRequest, monitorLog, this.getClass().getName(), getLogId(), startTime);
 	}
 
     /**
      * Accept a single or multiple (comma-separated) data commands and return a bitmapped set of flags that
      * represents the commands.
      */
 
     static public int getDataCmdIdForCmdText(String dataCmdText)
     {
         int result = DATA_CMD_NONE;
         if(dataCmdText != null)
 		{
             StringTokenizer st = new StringTokenizer(dataCmdText, ",");
             while(st.hasMoreTokens())
             {
                 String dataCmdToken = st.nextToken();
                 if(dataCmdToken.equals(Dialog.PARAMVALUE_DATA_CMD_ADD))
                     result |= DATA_CMD_ADD;
                 else if(dataCmdToken.equals(Dialog.PARAMVALUE_DATA_CMD_EDIT))
                     result |= DATA_CMD_EDIT;
                 else if(dataCmdToken.equals(Dialog.PARAMVALUE_DATA_CMD_DELETE))
                     result |= DATA_CMD_DELETE;
                 else if(dataCmdToken.equals(Dialog.PARAMVALUE_DATA_CMD_CONFIRM))
                     result |= DATA_CMD_CONFIRM;
             }
 		}
         return result;
     }
 
     static public String getDataCmdTextForCmdId(int dataCmd)
     {
         if(dataCmd == DATA_CMD_NONE)
            return "[none]";
 
         StringBuffer dataCmdText = new StringBuffer();
         if((dataCmd & DATA_CMD_ADD) != 0)
             dataCmdText.append(Dialog.PARAMVALUE_DATA_CMD_ADD);
 
         if((dataCmd & DATA_CMD_EDIT) != 0)
         {
             if(dataCmdText.length() > 0) dataCmdText.append(",");
             dataCmdText.append(Dialog.PARAMVALUE_DATA_CMD_EDIT);
         }
 
         if((dataCmd & DATA_CMD_DELETE) != 0)
         {
             if(dataCmdText.length() > 0) dataCmdText.append(",");
             dataCmdText.append(Dialog.PARAMVALUE_DATA_CMD_DELETE);
         }
 
         if((dataCmd & DATA_CMD_CONFIRM) != 0)
         {
             if(dataCmdText.length() > 0) dataCmdText.append(",");
             dataCmdText.append(Dialog.PARAMVALUE_DATA_CMD_CONFIRM);
         }
 
         return dataCmdText.toString();
     }
 
     public int getLastDataCmd()
     {
         return DATA_CMD_CONFIRM;
     }
 
     /**
      * Check the dataCmdCondition against each available data command and see if it's set in the condition; if the
      * data command is set in the condition, then check to see if our data command for that command id is set. If any
      * of the data commands in dataCommandCondition match our current dataCmd, return true.
      */
     public boolean matchesDataCmdCondition(int dataCmdCondition)
     {
         if(dataCmdCondition == DATA_CMD_NONE || dataCmd == DATA_CMD_NONE)
             return false;
 
         int lastDataCmd = getLastDataCmd();
         for(int i = 1; i <= lastDataCmd; i *= 2)
         {
             // if the dataCmdCondition's dataCmd i is set, it means we need to check our dataCmd to see if we're set
             if((dataCmdCondition & i) != 0 && (dataCmd & i) != 0)
                 return true;
         }
 
         // if we get to here, nothing matched
         return false;
     }
 
 	/**
 	 * Returns a string useful for displaying a unique Id for this DialogContext
 	 * in a log or monitor file.
 	 */
 	public String getLogId()
 	{
 		return dialog.getName() + " (" + transactionId + ")";
 	}
 
 	public List getListeners() { return listeners; }
 	public void addListener(DialogContextListener listener)
 	{
 		listeners.add(listener);
 	}
 
     public void assignFieldValues(Map values)
     {
         for(Iterator i = values.keySet().iterator(); i.hasNext(); )
         {
             String keyName = (String) i.next();
             DialogFieldState state = (DialogFieldState) fieldStates.get(keyName);
             if(state != null)
             {
                 Object keyObj = (Object) values.get(keyName);
                 if (keyObj != null)
                     state.value =  values.get(keyName).toString();
                 else
                     state.value = null;
             }
         }
     }
 
 	public void createStateFields(List fields)
 	{
 		int fieldsCount = fields.size();
 		for(int i = 0; i < fieldsCount; i++)
 		{
 			DialogField field = (DialogField) fields.get(i);
 			String qName = field.getQualifiedName();
 			if(qName != null)
 				fieldStates.put(qName, new DialogFieldState(field, dataCmd));
 			List children = field.getChildren();
 			if(children != null)
 				createStateFields(children);
 		}
 
 		if(runSequence == 1)
 		{
 			Map values = (Map) request.getAttribute(dialog.getValuesRequestAttrName());
 			if(values == null)
 				values = (Map) request.getAttribute(DIALOG_FIELD_VALUES_ATTR_NAME);
 			if(values != null)
                 assignFieldValues(values);
 		}
 	}
 
 	public void calcState()
 	{
 		activeMode = DIALOGMODE_INPUT;
 		dialog.makeStateChanges(this, STATECALCSTAGE_INITIAL);
 
 		String autoExec = request.getParameter(Dialog.PARAMNAME_AUTOEXECUTE);
 		if(autoExec != null && ! autoExec.equals("no"))
 		{
 			activeMode = dialog.isValid(this) ? DIALOGMODE_EXECUTE : DIALOGMODE_VALIDATE;
 		}
 		else if (! resetContext)
 		{
 			String modeParamValue = request.getParameter(dialog.getActiveModeParamName());
 			if(modeParamValue != null)
 			{
 				char givenMode = modeParamValue.charAt(0);
 				activeMode = (
 					givenMode == DIALOGMODE_VALIDATE ?
 						(dialog.isValid(this) ? DIALOGMODE_EXECUTE : DIALOGMODE_VALIDATE) :
 						givenMode
 					);
 			}
 		}
 
 		nextMode = activeMode;
 		if(activeMode == DIALOGMODE_INPUT)
 		{
 			nextMode = dialog.needsValidation(this) ? DIALOGMODE_VALIDATE : DIALOGMODE_EXECUTE;
 		}
 		else if(activeMode == DIALOGMODE_VALIDATE)
 		{
 			nextMode = dialog.isValid(this) ? DIALOGMODE_EXECUTE : DIALOGMODE_VALIDATE;
 		}
 		else if(activeMode == DIALOGMODE_EXECUTE)
 		{
             execSequence++;
 
 			if(dialog.loopEntries())
 				nextMode = dialog.needsValidation(this) ? DIALOGMODE_VALIDATE : DIALOGMODE_EXECUTE;
 			else
 				nextMode = DIALOGMODE_INPUT;
 		}
 
         if(dlgErrorMessages != null)
             nextMode = DIALOGMODE_VALIDATE;
 
 		dialog.makeStateChanges(this, STATECALCSTAGE_FINAL);
 	}
 
     public final Map getFieldStates() { return this.fieldStates; }
 	public final String getTransactionId() { return transactionId; }
 	public final boolean contextWasReset() { return resetContext; }
 	public final int getRunSequence() { return runSequence; }
 	public final int getExecuteSequence() { return execSequence; }
 	public final boolean isInitialEntry() { return runSequence == 1; }
 	public final boolean isInitialExecute() { return execSequence == 1; }
 	public final boolean isDuplicateExecute() { return execSequence > 1; }
 	public final char getActiveMode() { return activeMode; }
 	public final char getNextMode() { return nextMode; }
 	public final boolean inInputMode() { return activeMode == DIALOGMODE_INPUT; }
 	public final boolean inExecuteMode() { return activeMode == DIALOGMODE_EXECUTE; }
 	public final String getOriginalReferer() { return originalReferer; }
 	public final Dialog getDialog() { return dialog; }
 	public final DialogSkin getSkin() { return skin; }
     public final int getErrorsCount() { return errorsCount; }
 
 	public final int getDataCommand() { return dataCmd; }
     public final String getDataCommandText(boolean titleCase)
     {
         String dataCmdText = getDataCmdTextForCmdId(getDataCommand());
         if(! titleCase)
             return dataCmdText;
 
         StringBuffer dataCmdSb = new StringBuffer(dataCmdText);
         dataCmdSb.setCharAt(0, Character.toUpperCase(dataCmdSb.charAt(0)));
         return dataCmdSb.toString();
     }
 
     public final boolean addingData() { return (dataCmd & DATA_CMD_ADD) == 0 ? false : true; }
     public final boolean editingData() { return (dataCmd & DATA_CMD_EDIT) == 0 ? false : true; }
     public final boolean deletingData() { return (dataCmd & DATA_CMD_DELETE) == 0 ? false : true; }
     public final boolean confirmingData() { return (dataCmd & DATA_CMD_CONFIRM) == 0 ? false : true; }
 
 	public final DatabaseContext getDatabaseContext() { return dbContext; }
 	public final void setDatabaseContext(DatabaseContext value) { dbContext = value; }
 
 	public boolean validationPerformed() { return validationStage != VALSTAGE_NOT_PERFORMED ? true : false; }
 	public int getValidationStage() { return validationStage; }
 	public void setValidationStage(int value) { validationStage = value; }
 
 	public boolean executeStageHandled() { return executeHandled; }
 	public void setExecuteStageHandled(boolean value) { executeHandled = value; }
 
 	public String[] getRetainRequestParams() { return retainReqParams; }
 	public void setRetainRequestParams(String[] params) { retainReqParams = params; }
 
 	public String getStateHiddens()
 	{
 		StringBuffer hiddens = new StringBuffer();
 		hiddens.append("<input type='hidden' name='"+ dialog.getOriginalRefererParamName() +"' value='"+ originalReferer +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getTransactionIdParamName() +"' value='"+ transactionId +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getRunSequenceParamName() +"' value='"+ (runSequence + 1) +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getExecuteSequenceParamName() +"' value='"+ execSequence +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.getActiveModeParamName() +"' value='"+ nextMode +"'>\n");
 		hiddens.append("<input type='hidden' name='"+ dialog.PARAMNAME_DIALOGQNAME +"' value='"+ (runSequence > 1 ? request.getParameter(Dialog.PARAMNAME_DIALOGQNAME) : request.getParameter(DialogManager.REQPARAMNAME_DIALOG)) +"'>\n");
 
 		if(dataCmdStr != null)
 			hiddens.append("<input type='hidden' name='"+ dialog.getDataCmdParamName() +"' value='"+ dataCmdStr + "'>\n");
 
 		Set retainedParams = null;
 		if(retainReqParams != null)
 		{
 			retainedParams = new HashSet();
 			for(int i = 0; i < retainReqParams.length; i++)
 			{
 				String paramName = retainReqParams[i];
 				Object paramValue = request.getParameter(paramName);
 				if(paramValue == null)
 					continue;
 
 				hiddens.append("<input type='hidden' name='");
 				hiddens.append(paramName);
 				hiddens.append("' value='");
 				hiddens.append(paramValue);
 				hiddens.append("'>\n");
 				retainedParams.add(paramName);
 			}
 		}
 		boolean retainedAnyParams = retainedParams != null;
 
 		if(dialog.retainRequestParams())
 		{
 			if(dialog.retainAllRequestParams())
 			{
 				for(Enumeration e = request.getParameterNames(); e.hasMoreElements(); )
 				{
 					String paramName = (String) e.nextElement();
 					if( paramName.startsWith(Dialog.PARAMNAME_DIALOGPREFIX) ||
 						paramName.startsWith(Dialog.PARAMNAME_CONTROLPREFIX) ||
 						(retainedAnyParams && retainedParams.contains(paramName)))
 						continue;
 
 					hiddens.append("<input type='hidden' name='");
 					hiddens.append(paramName);
 					hiddens.append("' value='");
 					hiddens.append(request.getParameter(paramName));
 					hiddens.append("'>\n");
 				}
 			}
 			else
 			{
 				String[] retainParams = dialog.getRetainRequestParams();
 				int retainParamsCount = retainParams.length;
 
 				for(int i = 0; i < retainParamsCount; i++)
 				{
 					String paramName = retainParams[i];
 					if(retainedAnyParams && retainedParams.contains(paramName))
 						continue;
 
 					hiddens.append("<input type='hidden' name='");
 					hiddens.append(paramName);
 					hiddens.append("' value='");
 					hiddens.append(request.getParameter(paramName));
 					hiddens.append("'>\n");
 				}
 			}
 		}
 
 		return hiddens.toString();
 	}
 
 	public boolean flagIsSet(String fieldQName, long flag)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(fieldQName);
 		return (state.flags & flag) != 0;
 	}
 
 	public void setFlag(String fieldQName, long flag)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(fieldQName);
 		if(state != null)
 		{
 			state.flags |= flag;
 			List children = state.field.getChildren();
 			if(children != null)
 			{
 				Iterator i = children.iterator();
 				while(i.hasNext())
 				{
 					setFlag(((DialogField) i.next()).getQualifiedName(), flag);
 				}
 			}
 		}
 		else
 			throw new RuntimeException("Attempting to set flag '"+ flag +"' for non-existant field '"+ fieldQName + "': " + toString());
 	}
 
 	public void clearFlag(String fieldQName, long flag)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(fieldQName);
 		if(state != null)
 		{
 			state.flags &= ~flag;
 			List children = state.field.getChildren();
 			if(children != null)
 			{
 				Iterator i = children.iterator();
 				while(i.hasNext())
 				{
 					clearFlag(((DialogField) i.next()).getQualifiedName(), flag);
 				}
 			}
 		}
 		else
 			throw new RuntimeException("Attempting to clear flag '"+ flag +"' for non-existant field '"+ fieldQName + "'" + toString());
 	}
 
     public DialogField getField(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
             return null;
 		else
 			return state.field;
 	}
 
     public DialogFieldState getFieldState(String qualifiedName)
 	{
 		return (DialogFieldState) fieldStates.get(qualifiedName);
 	}
 
     public boolean hasValue(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return false;
 		else
 			return state.field.getValueAsObject(state.value) != null;
 	}
 
 	public boolean hasValue(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
             return false;
 		else
 			return state.field.getValueAsObject(state.value) != null;
 	}
 
 	public String getValue(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
 			return state.value;
 	}
 
 	public String getValue(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
 			return null;
 		else
 			return state.value;
 	}
 
     public String getValue(DialogField field, String defaultValue)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
         {
             String value = state.value;
 			return (value == null || value.length() == 0) ? defaultValue : value;
         }
 	}
 
 	public String getValue(String qualifiedName, String defaultValue)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
 			return null;
         else
         {
             String value = state.value;
             return (value == null || value.length() == 0) ? defaultValue : value;
         }
 	}
 
 	public Object getValueForSqlBindParam(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
             return field.getValueForSqlBindParam(state.value);
 	}
 
 	public Object getValueForSqlBindParam(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
 			return null;
 		else
             return state.field.getValueForSqlBindParam(state.value);
 	}
 
     public Object getValueAsObject(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
             return field.getValueAsObject(state.value);
 	}
 
 	public Object getValueAsObject(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
 			return null;
 		else
             return state.field.getValueAsObject(state.value);
 	}
 
     public Object getValueAsObject(DialogField field, Object defaultValue)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
         {
             Object value = state.field.getValueAsObject(state.value);
 			return value == null ? defaultValue : value;
         }
 	}
 
 	public Object getValueAsObject(String qualifiedName, Object defaultValue)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
 			return null;
         else
         {
             Object value = state.field.getValueAsObject(state.value);
 			return value == null ? defaultValue : value;
         }
 	}
 
 	public void setValue(String qualifiedName, String value)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		state.value = value;
 	}
 
 	public void setValue(DialogField field, String value)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		state.value = value;
 	}
 
 	public String[] getValues(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return null;
 		else
 			return state.values;
 	}
 
 	public String[] getValues(String qualifiedName)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state == null)
 			return null;
 		else
 			return state.values;
 	}
 
     public void setValues(String qualifiedName, String[] values)
 	{
         DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
 		if(state != null)
             state.values = values;
 	}
 
 	public void setValues(DialogField field, String[] values)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		state.values = values;
 	}
 
 	public List getErrorMessages(DialogField field)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(field.getQualifiedName());
 		if(state == null)
 			return field.getErrors();
 		else
 		{
 			List fieldErrors = state.field.getErrors();
 			if(fieldErrors != null)
 				return fieldErrors;
 			return state.errorMessages;
 		}
 	}
 
     public void addErrorMessage(String qualifiedName, String message)
 	{
 		DialogFieldState state = (DialogFieldState) fieldStates.get(qualifiedName);
         if(state == null)
             throw new RuntimeException("DialogField '"+ qualifiedName +"' does not exist.");
 
 		if(state.errorMessages == null)
 			state.errorMessages = new ArrayList();
 
 		for(Iterator i = state.errorMessages.iterator(); i.hasNext(); )
 		{
 			if(((String) i.next()).equals(message))
 				return;
 		}
 
 		state.errorMessages.add(message);
         errorsCount++;
 	}
 
 	public void addErrorMessage(DialogField field, String message)
 	{
 		addErrorMessage(field.getQualifiedName(), message);
         errorsCount++;
 	}
 
 
     /**
      *  Return error messages that are not specific to a particular field (at the Dialog level)
      */
 
     public List getErrorMessages()
 	{
         return dlgErrorMessages;
 	}
 
     /**
      *  Add error message that is not specific to a particular field (at the Dialog level)
      */
 
     public void addErrorMessage(String message)
 	{
 		if(dlgErrorMessages == null)
 			dlgErrorMessages = new ArrayList();
 
 		for(Iterator i = dlgErrorMessages.iterator(); i.hasNext(); )
 		{
 			if(((String) i.next()).equals(message))
 				return;
 		}
 
 		dlgErrorMessages.add(message);
         errorsCount++;
 	}
 
 	public void populateValuesFromStatement(String statementId)
 	{
 		populateValuesFromStatement(null, statementId, null);
 	}
 
 	public void populateValuesFromStatement(String statementId, Object[] params)
 	{
 		populateValuesFromStatement(null, statementId, params);
 	}
 
 	public void populateValuesFromStatement(String dataSourceId, String statementId, Object[] params)
 	{
 		try
 		{
 			ServletContext context = getServletContext();
 			StatementManager stmtMgr = StatementManagerFactory.getManager(context);
 			DatabaseContext dbContext = DatabaseContextFactory.getContext(getRequest(), context);
 			StatementManager.ResultInfo ri = stmtMgr.execute(dbContext, this, dataSourceId, statementId, params);
 			dialogFieldStoreValueSource.setValue(this, ri.getResultSet(), SingleValueSource.RESULTSET_STORETYPE_SINGLEROWFORMFLD);
 			ri.close();
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException(e.toString());
 		}
 	}
 
     public void populateValuesFromSql(String sql)
 	{
 		populateValuesFromStatement(null, sql, null);
 	}
 
 	public void populateValuesFromSql(String sql, Object[] params)
 	{
 		populateValuesFromStatement(null, sql, params);
 	}
 
 	public void populateValuesFromSql(String dataSourceId, String sql, Object[] params)
 	{
 		try
 		{
 			ServletContext context = getServletContext();
 			DatabaseContext dbContext = DatabaseContextFactory.getContext(getRequest(), context);
 			StatementManager.ResultInfo ri = StatementManager.executeSql(dbContext, this, dataSourceId, sql, params);
 			dialogFieldStoreValueSource.setValue(this, ri.getResultSet(), SingleValueSource.RESULTSET_STORETYPE_SINGLEROWFORMFLD);
 			ri.close();
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException(e.toString());
 		}
 	}
 
     public void beginSqlTransaction() throws TaskExecuteException
     {
         beginSqlTransaction(null);
     }
 
     public void beginSqlTransaction(String dataSourceId) throws TaskExecuteException
     {
         TransactionTask task = new TransactionTask();
         task.setDataSource(dataSourceId);
         task.setCommand(TransactionTask.COMMAND_BEGIN);
         task.execute(new TaskContext(this));
         task.reset();
         task = null;
     }
 
     public void endSqlTransaction() throws TaskExecuteException
     {
         endSqlTransaction(null);
     }
 
     public void endSqlTransaction(String dataSourceId) throws TaskExecuteException
     {
         TransactionTask task = new TransactionTask();
         task.setDataSource(dataSourceId);
         task.setCommand(TransactionTask.COMMAND_END);
         task.execute(new TaskContext(this));
         task.reset();
         task = null;
     }
 
     public void executeSqlInsert(String table, String fields, String columns) throws TaskExecuteException
     {
         executeSqlInsert(null, table, fields, columns);
     }
 
     public void executeSqlInsert(String dataSourceId, String table, String fields, String columns) throws TaskExecuteException
     {
         DmlTask task = new DmlTask();
         task.setCommand(DmlTask.DMLCMD_INSERT);
         task.setDataSource(dataSourceId);
         task.setTable(table);
         task.setFields(fields);
         task.setColumns(columns);
         task.execute(new TaskContext(this));
         task.reset();
         task = null;
     }
 
     public void executeSqlUpdate(String table, String fields, String columns, String whereCond, String whereCondBindParams) throws TaskExecuteException
     {
         executeSqlUpdate(null, table, fields, columns, whereCond, whereCondBindParams);
     }
 
     public void executeSqlUpdate(String dataSourceId, String table, String fields, String columns, String whereCond, String whereCondBindParams) throws TaskExecuteException
     {
         DmlTask task = new DmlTask();
         task.setCommand(DmlTask.DMLCMD_UPDATE);
         task.setDataSource(dataSourceId);
         task.setTable(table);
         task.setFields(fields);
         task.setColumns(columns);
         task.setWhereCond(whereCond);
         task.setWhereCondBindParams(whereCondBindParams);
         task.execute(new TaskContext(this));
         task.reset();
         task = null;
     }
 
     public void executeSqlRemove(String table, String fields, String columns, String whereCond, String whereCondBindParams) throws TaskExecuteException
     {
         executeSqlRemove(null, table, fields, columns, whereCond, whereCondBindParams);
     }
 
     public void executeSqlRemove(String dataSourceId, String table, String fields, String columns, String whereCond, String whereCondBindParams) throws TaskExecuteException
     {
         DmlTask task = new DmlTask();
         task.setCommand(DmlTask.DMLCMD_REMOVE);
         task.setDataSource(dataSourceId);
         task.setTable(table);
         task.setFields(fields);
         task.setColumns(columns);
         task.setWhereCond(whereCond);
         task.setWhereCondBindParams(whereCondBindParams);
         task.execute(new TaskContext(this));
         task.reset();
         task = null;
     }
 
 	public String getDebugHtml()
 	{
 		StringBuffer values = new StringBuffer();
 
 		for(Iterator i = fieldStates.values().iterator(); i.hasNext(); )
 		{
 			DialogFieldState state = (DialogFieldState) i.next();
 			if(state.values != null)
 			{
 				StringBuffer multiValues = new StringBuffer();
 				for(int v = 0; v < state.values.length; v++)
 					multiValues.append(state.values[v] + "<br>");
 
 				values.append("<tr valign=top><td>"+state.field.getQualifiedName()+"</td><td>"+multiValues.toString()+"</td></tr>");
 			}
 			else
 			{
 				values.append("<tr valign=top><td>"+state.field.getQualifiedName()+"</td><td>"+state.value+"</td></tr>");
 			}
 		}
 
 		return "<table border=1 cellspacing=0 cellpadding=4>"+
 			    "<tr><td><b>Dialog</b></td><td>"+ dialog.getName() +"</td></tr>" +
 			    "<tr><td><b>Run Sequence</b></td><td>"+ runSequence +"</td></tr>" +
 			    "<tr><td><b>Active/Next Mode</b></td><td>"+ activeMode + " -> " + nextMode +"</td></tr>" +
 			    "<tr><td><b>Validation Stage</b></td><td>"+ validationStage +"</td></tr>" +
                 "<tr><td><b>Data Command</b></td><td>"+ getDataCmdTextForCmdId(this.getDataCommand()) +"</td></tr>" +
                 "<tr><td><b>Populate Tasks</b></td><td>"+ (dialog.getPopulateTasks() != null ? dialog.getPopulateTasks().getDebugHtml(this) : "none") +"</td></tr>" +
                 "<tr><td><b>Execute Tasks</b></td><td>"+ (dialog.getExecuteTasks() != null ? dialog.getExecuteTasks().getDebugHtml(this) : "none") +"</td></tr>" +
 			    values.toString()+
 				"</table>";
 	}
 }
