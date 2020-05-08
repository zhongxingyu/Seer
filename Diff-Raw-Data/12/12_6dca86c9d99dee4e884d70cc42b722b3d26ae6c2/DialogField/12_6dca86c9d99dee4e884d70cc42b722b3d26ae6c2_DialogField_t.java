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
 * $Id: DialogField.java,v 1.25 2003-04-21 22:03:00 thai.nguyen Exp $
  */
 
 package com.netspective.sparx.xaf.form;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.*;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.NamedNodeMap;
 
 import com.netspective.sparx.xaf.form.field.SelectField;
 import com.netspective.sparx.xaf.form.conditional.DialogFieldConditionalApplyFlag;
 import com.netspective.sparx.xaf.form.conditional.DialogFieldConditionalData;
 import com.netspective.sparx.xaf.form.conditional.DialogFieldConditionalDisplay;
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 import com.netspective.sparx.xif.SchemaDocument;
 import com.netspective.sparx.xif.SchemaDocFactory;
 import com.netspective.sparx.xif.dal.Schema;
 import com.netspective.sparx.xif.dal.Table;
 import com.netspective.sparx.xif.dal.Column;
 
 /**
  * A <code>DialogField</code> object represents a data field of a form/dialog. It contains functionalities
  * such as data validation rules, dynamic data binding, HTML rendering, and conditional logics.
  * It provides the default behavior and functionality for all types of dialog fields.
  * All dialog classes representing specialized  fields such as text fields, numerical fields, and phone fields subclass
  * the <code>DialogField</code> class.
  */
 public class DialogField
 {
 	// all these values are also defined in dialog.js (make sure they are always in sync)
 	static public final int FLDFLAG_REQUIRED = 1;
 	static public final int FLDFLAG_PRIMARYKEY = FLDFLAG_REQUIRED * 2;
 	static public final int FLDFLAG_INVISIBLE = FLDFLAG_PRIMARYKEY * 2;
 	static public final int FLDFLAG_READONLY = FLDFLAG_INVISIBLE * 2;
 	static public final int FLDFLAG_INITIAL_FOCUS = FLDFLAG_READONLY * 2;
 	static public final int FLDFLAG_PERSIST = FLDFLAG_INITIAL_FOCUS * 2;
 	static public final int FLDFLAG_CREATEADJACENTAREA = FLDFLAG_PERSIST * 2;
 	static public final int FLDFLAG_SHOWCAPTIONASCHILD = FLDFLAG_CREATEADJACENTAREA * 2;
 	static public final int FLDFLAG_INPUT_HIDDEN = FLDFLAG_SHOWCAPTIONASCHILD * 2;
 	static public final int FLDFLAG_HAS_CONDITIONAL_DATA = FLDFLAG_INPUT_HIDDEN * 2;
 	static public final int FLDFLAG_COLUMN_BREAK_BEFORE = FLDFLAG_HAS_CONDITIONAL_DATA * 2;
 	static public final int FLDFLAG_COLUMN_BREAK_AFTER = FLDFLAG_COLUMN_BREAK_BEFORE * 2;
 	static public final int FLDFLAG_BROWSER_READONLY = FLDFLAG_COLUMN_BREAK_AFTER * 2;
 	static public final int FLDFLAG_IDENTIFIER = FLDFLAG_BROWSER_READONLY * 2;
 	static public final int FLDFLAG_READONLY_HIDDEN_UNLESS_HAS_DATA = FLDFLAG_IDENTIFIER * 2;
 	static public final int FLDFLAG_READONLY_INVISIBLE_UNLESS_HAS_DATA = FLDFLAG_READONLY_HIDDEN_UNLESS_HAS_DATA * 2;
 	static public final int FLDFLAG_DOUBLEENTRY = FLDFLAG_READONLY_INVISIBLE_UNLESS_HAS_DATA * 2;
 	static public final int FLDFLAG_SCANNABLE = FLDFLAG_DOUBLEENTRY * 2;
 	static public final int FLDFLAG_AUTOBLUR = FLDFLAG_SCANNABLE * 2;
 	static public final int FLDFLAG_SUBMIT_ONBLUR = FLDFLAG_AUTOBLUR * 2;
 	static public final int FLDFLAG_CREATEADJACENTAREA_HIDDEN = FLDFLAG_SUBMIT_ONBLUR * 2;
 	static public final int FLDFLAG_STARTCUSTOM = FLDFLAG_CREATEADJACENTAREA_HIDDEN * 2; // all DialogField "children" will use this
 
 	// flags used to describe what kind of formatting needs to be done to the dialog field
 	public static final int DISPLAY_FORMAT = 1;
 	public static final int SUBMIT_FORMAT = 2;
 
 	static public int[] CHILD_CARRY_FLAGS = new int[]{FLDFLAG_REQUIRED, FLDFLAG_INVISIBLE, FLDFLAG_READONLY, FLDFLAG_PERSIST, FLDFLAG_CREATEADJACENTAREA, FLDFLAG_SHOWCAPTIONASCHILD};
 
 	static public String CUSTOM_CAPTION = new String();
 	static public String GENERATE_CAPTION = "*";
 
 	static public String FIELDTAGPREFIX = "field.";
 
 	static public int fieldCounter = 0;
 	private boolean multi = false;
 
 	private DialogField parent;
 	private String id;
 	private String simpleName;
 	private String qualifiedName;
 	private SingleValueSource caption;
 	private String cookieName;
 	private String errorMessage;
 	private SingleValueSource defaultValue;
 	private List errors;
 	private List children;
 	private List conditionalActions;
 	private List dependentConditions;
 	private List clientJavascripts;
 	private long flags;
 	private DialogFieldPopup popup;
 	private SingleValueSource hint;
 	private String schemaName;
 	private Schema schema;
 	private String tableName;
 	private Table table;
 	private String columnName;
 	private Column column;
 	private Map dalProperties;
 	private String scanStartCode;
 	private String scanStopCode;
 	private String scanPartnerField;
 	private String scanFieldCustomScript;
 	private int autoBlurLength;
 	private String autoBlurExcludeRegExp;
 	private String submitOnBlurPartnerField;
 	private String submitOnBlurCustomScript;
	private String scanCodeIgnoreCase;
 
 	/**
 	 * Creates a dialog field
 	 */
 	public DialogField()
 	{
 		defaultValue = null;
 		errorMessage = null;
 		flags = 0;
 	}
 
 	/**
 	 * Creates a dialog field
 	 *
 	 * @param aName field name
 	 * @param aCaption field caption
 	 */
 	public DialogField(String aName, String aCaption)
 	{
 		this();
 		setSimpleName(aName);
 		caption = aCaption != null ? ValueSourceFactory.getSingleOrStaticValueSource(aCaption) : null;
 	}
 
 	/**
 	 * Checks to see if the field requires multi-part endcoding
 	 *
 	 * @return boolean
 	 */
 	public boolean requiresMultiPartEncoding()
 	{
 		// if any child requires multi part encoding, then return true (this will take of things recursively)
 		if (children != null)
 		{
 			Iterator c = children.iterator();
 			while (c.hasNext())
 			{
 				DialogField field = (DialogField) c.next();
 				if (field.requiresMultiPartEncoding())
 					return true;
 			}
 		}
 
 		// no child requires it and we don't require it by default, either
 		return false;
 	}
 
 	/**
 	 * Checks to see if the default value is a list value source. Returns <code>false</code>
 	 * always for a <code>DialogField</code> object but child classes requiring a default
 	 * list value source will return <code>true</code>.
 	 *
 	 * @return boolean
 	 */
 	public boolean defaultIsListValueSource()
 	{
 		return false;
 	}
 
 	/**
 	 * Import dialog XML declaration
 	 *
 	 * @param elem DOM Document element representing a dialog
 	 */
 	public void importFromXml(Element elem)
 	{
 		simpleName = elem.getAttribute("name");
 		if (simpleName.length() == 0) simpleName = null;
 		setSimpleName(simpleName);
 
 		String captionStr = elem.getAttribute("caption");
 		if (captionStr.length() > 0)
 			caption = ValueSourceFactory.getSingleOrStaticValueSource(captionStr);
 
 		if (!defaultIsListValueSource())
 		{
 			String defaultv = elem.getAttribute("default");
 			if (defaultv.length() > 0)
 			{
 				defaultValue = ValueSourceFactory.getSingleOrStaticValueSource(defaultv);
 			}
 			else
 				defaultValue = null;
 		}
 
 		String hintStr = elem.getAttribute("hint");
 		if (hintStr.length() > 0)
 			hint = ValueSourceFactory.getSingleOrStaticValueSource(hintStr);
 
 		if (elem.getAttribute("required").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_REQUIRED);
 
 		if (elem.getAttribute("primary-key").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_PRIMARYKEY);
 
 		if (elem.getAttribute("initial-focus").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_INITIAL_FOCUS);
 
 		// 1. read-only flag of 'yes' will display the field value as a static string
 		// within SPAN tags and the INPUT will be hidden.
 		// 2. read-only flag of 'browser' will display the field value as
 		// an INPUT with readonly flag set.
 		String readonly = elem.getAttribute("read-only");
 		if (readonly.equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_READONLY);
 		else if (readonly.equalsIgnoreCase("browser"))
 			setFlag(DialogField.FLDFLAG_BROWSER_READONLY);
 
 		if (elem.getAttribute("visible").equalsIgnoreCase("no"))
 			setFlag(DialogField.FLDFLAG_INVISIBLE);
 
 		if (elem.getAttribute("hidden").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_INPUT_HIDDEN);
 
 		if (elem.getAttribute("persist").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_PERSIST);
 
 		if (elem.getAttribute("show-child-caption").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_SHOWCAPTIONASCHILD);
 
 		if (elem.getAttribute("create-adjacent-area").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_CREATEADJACENTAREA);
 
 		if (elem.getAttribute("identifier").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_IDENTIFIER);
 
 		if (elem.getAttribute("read-only-hidden-unless-has-data").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_READONLY_HIDDEN_UNLESS_HAS_DATA);
 
 		if (elem.getAttribute("read-only-invisible-unless-has-data").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_READONLY_INVISIBLE_UNLESS_HAS_DATA);
 
 		String colBreak = elem.getAttribute("col-break");
 		if (colBreak.length() > 0)
 		{
 			if (colBreak.equals("before"))
 				setFlag(DialogField.FLDFLAG_COLUMN_BREAK_BEFORE);
 			else if (colBreak.equals("after") || colBreak.equals("yes"))
 				setFlag(DialogField.FLDFLAG_COLUMN_BREAK_AFTER);
 		}
 
 		if (elem.getAttribute("schema").length() > 0)
 			setSchemaName(elem.getAttribute("schema"));
 
 		if (elem.getAttribute("table").length() > 0)
 			setTableName(elem.getAttribute("table"));
 
 		if (elem.getAttribute("column").length() > 0)
 		{
 			setColumnName(elem.getAttribute("column"));
 			dalProperties = new HashMap();
 			NamedNodeMap nnm = elem.getAttributes();
 			for (int i = 0; i < nnm.getLength(); i++)
 			{
 				Node attr = nnm.item(i);
 				if (attr.getNodeName().startsWith("dal-"))
 					dalProperties.put(attr.getNodeName(), attr.getNodeValue());
 			}
 		}
 
 		if (elem.getAttribute("double-entry").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_DOUBLEENTRY);
 
 		importChildrenFromXml(elem);
 	}
 
 	/**
 	 * Import children nodes of a dialog element
 	 *
 	 * @param elem dialog element
 	 */
 	public void importChildrenFromXml(Element elem)
 	{
 		NodeList children = elem.getChildNodes();
 		for (int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if (node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			String childName = node.getNodeName();
 			if (childName.startsWith(FIELDTAGPREFIX))
 			{
 				Element fieldElem = (Element) node;
 				DialogField field = DialogFieldFactory.createField(childName);
 				if (field != null)
 					field.importFromXml(fieldElem);
 				else
 					field = new SelectField("error", "Unable to create field of type '" + childName, SelectField.SELECTSTYLE_COMBO, ValueSourceFactory.getListValueSource("dialog-field-types:"));
 				addChildField(field);
 			}
 			else if (childName.equals("conditional"))
 			{
 				importConditionalFromXml((Element) node);
 			}
 			else if (childName.equals("popup"))
 			{
 				importPopupFromXml((Element) node);
 			}
 			else if (childName.equals("client-js"))
 			{
 				importCustomJavaScriptFromXml((Element) node);
 			}
 			else if (childName.equals("scan-entry"))
 			{
 				setFlag(DialogField.FLDFLAG_SCANNABLE);
 				importScanEntryFromXml((Element) node);
 			}
 			else if (childName.equals("auto-blur"))
 			{
 				setFlag(DialogField.FLDFLAG_AUTOBLUR);
 				importAutoBlurFromXml((Element) node);
 			}
 			else if (childName.equals("submit-onblur"))
 			{
 				setFlag(DialogField.FLDFLAG_SUBMIT_ONBLUR);
 				importSubmitOnBlurFromXml((Element) node);
 			}
 		}
 	}
 
 	public void importSubmitOnBlurFromXml(Element elem)
 	{
 		String partnerField = elem.getAttribute("partner");
 		submitOnBlurPartnerField = (partnerField == null || partnerField.length() == 0) ? "" : partnerField;
 
 		String customScript = elem.getAttribute("custom-script");
 		submitOnBlurCustomScript = (customScript == null || customScript.length() == 0) ? "" : customScript;
 	}
 
 	public void importAutoBlurFromXml(Element elem)
 	{
 		String length = elem.getAttribute("length");
 		autoBlurLength = (length == null || length.length() == 0) ? 0 : Integer.parseInt(length);
 
 		String excExpr = elem.getAttribute("exclude-expr");
 		autoBlurExcludeRegExp = (excExpr == null || excExpr.length() == 0) ? "" : excExpr;
 	}
 
 	public void importScanEntryFromXml(Element elem)
 	{
 		String startCode = elem.getAttribute("start-code");
 		scanStartCode = (startCode == null || startCode.length() == 0) ? "" : startCode;
 
 		String stopCode = elem.getAttribute("stop-code");
 		scanStopCode = (stopCode == null || stopCode.length() == 0) ? "" : stopCode;
 
 		String partnerField = elem.getAttribute("partner");
 		scanPartnerField = (partnerField == null || partnerField.length() == 0) ? "" : partnerField;
 
 		String customScript = elem.getAttribute("custom-script");
 		scanFieldCustomScript = (customScript == null || customScript.length() == 0) ? "" : customScript;

		scanCodeIgnoreCase = (elem.getAttribute("ignore-case").equalsIgnoreCase("yes")) ? "i" : "";
 	}
 
 	/**
 	 * Reads the XML for Custom Javascript configuration assigned to a dialog field
 	 *
 	 * @param elem client-js node
 	 */
 	public void importCustomJavaScriptFromXml(Element elem)
 	{
 		// what time of event should this custom JS respond to
 		String event = elem.getAttribute("event");
 		if (event == null || event.length() == 0)
 		{
 			addErrorMessage("No 'event' specified for custom Javascript.");
 			return;
 		}
 		else if (!DialogFieldClientJavascript.isValidEvent(event))
 		{
 			addErrorMessage("Invalid 'event' specified for custom Javascript.");
 			return;
 		}
 
 		// whether or not if this JS script should overwrite or extend the existing default JS
 		// assigned to the event
 		String type = elem.getAttribute("type");
 		if (type == null || type.length() == 0)
 		{
 			addErrorMessage("No 'type' specified for custom Javascript.");
 			return;
 		}
 		else if (!type.equals("extends") && !type.equals("override"))
 		{
 			addErrorMessage("Invalid 'type' specified for custom Javascript.");
 			return;
 		}
 
 		// get the custom script
 		String script = elem.getAttribute("js-expr");
 		if (script == null || script.length() == 0)
 		{
 			addErrorMessage("No custom Javascript defined.");
 			return;
 		}
 		DialogFieldClientJavascript customJS = new DialogFieldClientJavascript();
 		customJS.setEvent(event);
 		customJS.setType(type);
 		customJS.setScript(script);
 
 		this.addClientJavascript(customJS);
 	}
 
 	/**
 	 * Imports XML nodes representing conditional logic for a dialog element
 	 *
 	 * @param elem dialog element
 	 */
 	public void importConditionalFromXml(Element elem)
 	{
 		String action = elem.getAttribute("action");
 		if (action == null || action.length() == 0)
 		{
 			addErrorMessage("No 'action' specified for conditional.");
 			return;
 		}
 
 		DialogFieldConditionalAction actionInst = DialogFieldFactory.createConditional(action);
 		if (actionInst != null)
 		{
 			int conditionalItem = (conditionalActions == null ? 0 : conditionalActions.size()) + 1;
 			if (actionInst.importFromXml(this, elem, conditionalItem))
 				addConditionalAction(actionInst);
 		}
 		else
 		{
 			addErrorMessage("Conditional action '" + action + "' unknown.");
 		}
 	}
 
 	/**
 	 * Imports XML nodes representing popup window logic for a dialog element
 	 *
 	 * @param elem dialog element
 	 */
 	public void importPopupFromXml(Element elem)
 	{
 		String action = elem.getAttribute("action");
 		if (action.length() == 0)
 		{
 			addErrorMessage("Popup has no associated 'action' (URL).");
 			return;
 		}
 
 		String[] fillFields = null;
 		String fill = elem.getAttribute("fill");
 		if (fill.length() == 0)
 		{
 			fillFields = new String[]{getQualifiedName()};
 		}
 		else
 		{
 			if (fill.indexOf(",") > 0)
 			{
 				int fillCount = 0;
 				StringTokenizer st = new StringTokenizer(fill, ",");
 				while (st.hasMoreTokens())
 				{
 					st.nextToken();
 					fillCount++;
 				}
 
 				int fillIndex = 0;
 				fillFields = new String[fillCount];
 				st = new StringTokenizer(fill, ",");
 				while (st.hasMoreTokens())
 				{
 					fillFields[fillIndex] = st.nextToken();
 					fillIndex++;
 				}
 			}
 			else
 				fillFields = new String[]{fill};
 		}
 
 		String extractFields[] = null;
 		String extract = elem.getAttribute("extract");
 		if (extract != null && extract.length() > 0)
 		{
 			if (extract.indexOf(",") > 0)
 			{
 				int fillCount = 0;
 				StringTokenizer st = new StringTokenizer(extract, ",");
 				while (st.hasMoreTokens())
 				{
 					st.nextToken();
 					fillCount++;
 				}
 
 				int extractIndex = 0;
 				fillFields = new String[fillCount];
 				st = new StringTokenizer(extract, ",");
 				while (st.hasMoreTokens())
 				{
 					extractFields[extractIndex] = st.nextToken();
 					extractIndex++;
 				}
 			}
 			else
 				extractFields = new String[]{extract};
 		}
 
 		popup = new DialogFieldPopup(action, fillFields);
 		if (extractFields != null)
 			popup.setExtractFields(extractFields);
 		String imgsrc = elem.getAttribute("image-src");
 		if (imgsrc.length() > 0)
 			popup.setImageUrl(imgsrc);
 	}
 
 	public void invalidate(DialogContext dc, String message)
 	{
 		dc.addErrorMessage(parent != null ? parent : this, message);
 	}
 
 	/**
 	 * Gets the parent dialog field
 	 *
 	 * @return DialogField
 	 */
 	public DialogField getParent()
 	{
 		return parent;
 	}
 
 	/**
 	 * Sets the parent dialog field
 	 *
 	 * @param newParent the parent field
 	 */
 	public void setParent(DialogField newParent)
 	{
 		parent = newParent;
 	}
 
 	/**
 	 * Returns true if the field is supposed to return multiple values
 	 *
 	 * @return boolean Does this field return multiple values (e.g. multilist select field)
 	 */
 	public boolean isMulti()
 	{
 		return multi;
 	}
 
 	/**
 	 * Sets the value of the multi attribute which determines whether a field returns multiple values or not (e.g.
 	 * a multilist select field)
 	 *
 	 * @param multi boolean true/false - Whether or not this field returns multiple values
 	 */
 	public void setMulti(boolean multi)
 	{
 		this.multi = multi;
 	}
 
 	public String getId()
 	{
 		return id;
 	}
 
 	/**
 	 * If this dialog is a "table-dialog" then this is the name of the schema the table belongs to (null if default)
 	 */
 	public String getSchemaName()
 	{
 		return schemaName;
 	}
 
 	/**
 	 * Set the schema name for a "table-dialog"
 	 */
 	public void setSchemaName(String schemaName)
 	{
 		this.schemaName = schemaName;
 		schema = null;
 	}
 
 	/**
 	 * Return the schema document associated with the schema of this table-dialog (if it's a table-dialog)
 	 */
 	public SchemaDocument getSchemaDoc(DialogContext dc)
 	{
 		return schemaName == null ? SchemaDocFactory.getDoc(dc.getServletContext()) : SchemaDocFactory.getDoc(schemaName);
 	}
 
 	/**
 	 * Return the actual schema instance associated with the schema document
 	 */
 	public Schema getSchema(DialogContext dc)
 	{
 		if (schema != null)
 			return schema;
 
 		SchemaDocument schemaDoc = getSchemaDoc(dc);
 		if (schemaDoc != null)
 			schema = schemaDoc.getSchema();
 		else
 			schema = null;
 
 		return schema;
 	}
 
 	/**
 	 * If this dialog is a "table-dialog" then this is the name of the table (null if not a table-dialog)
 	 */
 	public String getTableName()
 	{
 		return tableName;
 	}
 
 	public Table getTable(DialogContext dc)
 	{
 		if (table != null)
 			return table;
 
 		if (tableName != null)
 		{
 			Schema schema = getSchema(dc);
 			if (schema != null)
 				table = schema.getTable(tableName);
 			else
 				table = null;
 		}
 		else
 			table = null;
 
 		return table;
 	}
 
 	/**
 	 * Set the table name for a "table-dialog"
 	 */
 	public void setTableName(String tableName)
 	{
 		this.tableName = tableName;
 		table = null;
 	}
 
 	public String getColumnName()
 	{
 		return columnName;
 	}
 
 	public void setColumnName(String columnName)
 	{
 		this.columnName = columnName;
 	}
 
 	public Column getColumn(DialogContext dc)
 	{
 		if (column != null)
 			return column;
 
 		if (columnName != null)
 		{
 			Table table = getTable(dc);
 			if (table != null)
 				column = table.getColumnByName(columnName);
 			else
 				column = null;
 		}
 		else
 			column = null;
 
 		return column;
 	}
 
 	public boolean isTableColumnField()
 	{
 		return tableName != null && columnName != null;
 	}
 
 	/**
 	 * Gets the simple name of the dialog
 	 *
 	 * @return String
 	 */
 	public String getSimpleName()
 	{
 		return simpleName;
 	}
 
 	/**
 	 * Gets the qualified name of the dialog
 	 *
 	 * @return String
 	 */
 	public String getQualifiedName()
 	{
 		return qualifiedName;
 	}
 
 	/**
 	 * Sets the simple name of the dialog
 	 *
 	 * @param newName new simple name
 	 */
 	public void setSimpleName(String newName)
 	{
 		simpleName = newName;
 		if (simpleName != null)
 		{
 			id = Dialog.PARAMNAME_CONTROLPREFIX + simpleName;
 			setQualifiedName(simpleName);
 		}
 	}
 
 	/**
 	 * Sets the qualified name of the dialog
 	 *
 	 * @param newName new qualified name
 	 */
 	public void setQualifiedName(String newName)
 	{
 		qualifiedName = newName;
 		if (qualifiedName != null)
 			id = Dialog.PARAMNAME_CONTROLPREFIX + qualifiedName;
 	}
 
 	/**
 	 * Gets the cookie name associated with the dialog
 	 *
 	 * @return String cookie name
 	 */
 	public String getCookieName()
 	{
 		return cookieName == null ? (Dialog.PARAMNAME_CONTROLPREFIX + getQualifiedName()) : cookieName;
 	}
 
 	/**
 	 * Sets the cookie name associated with the dialog
 	 *
 	 * @param name cookie name
 	 */
 	public void setCookieName(String name)
 	{
 		cookieName = name;
 	}
 
 	/**
 	 * Gets the caption of the dialog as a single value source
 	 *
 	 * @return SingleValueSource
 	 */
 	public SingleValueSource getCaptionSource()
 	{
 		return caption;
 	}
 
 	/**
 	 * Gets the caption string of the dialog
 	 *
 	 * @param dc dialog context
 	 * @return String
 	 */
 	public String getCaption(DialogContext dc)
 	{
 		return caption != null ? caption.getValue(dc) : null;
 	}
 
 	/**
 	 * Sets the caption of the dialog from a value source
 	 *
 	 * @param value value source object from which the caption is being extracted
 	 */
 	public void setCaption(SingleValueSource value)
 	{
 		caption = value;
 	}
 
 	/**
 	 * Sets the caption of the dialog from a value source
 	 *
 	 * @param value value source from which the caption string is being extracted
 	 */
 	public void setCaption(String value)
 	{
 		setCaption(value != null ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null);
 	}
 
 	/**
 	 * Gets the hint string associated with the dialog field
 	 *
 	 * @return String
 	 */
 	public String getHint(DialogContext dc)
 	{
 		return hint != null ? hint.getValue(dc) : null;
 	}
 
 	/**
 	 * Sets the hint string associated with the dialog field
 	 *
 	 * @param value hint string
 	 */
 	public void setHint(SingleValueSource value)
 	{
 		hint = value;
 	}
 
 	/**
 	 * Sets the hint string associated with the dialog field
 	 *
 	 * @param value hint string
 	 */
 	public void setHint(String value)
 	{
 		setHint(value != null ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null);
 	}
 
 	/**
 	 * Gets the display error message when a validation fails
 	 *
 	 * @return String
 	 */
 	public String getErrorMessage()
 	{
 		return errorMessage;
 	}
 
 	/**
 	 * Sets the error message for display when a validation fails
 	 *
 	 * @param newMessage error message string
 	 */
 	public void setErrorMessage(String newMessage)
 	{
 		errorMessage = newMessage;
 	}
 
 	/**
 	 * Gets the default value of the field as a value source
 	 *
 	 * @return SingleValueSource    value source containing the field's value
 	 */
 	public SingleValueSource getDefaultValue()
 	{
 		return defaultValue;
 	}
 
 	/**
 	 * Sets the default value for the field
 	 *
 	 * @param value value source containing the value
 	 */
 	public void setDefaultValue(SingleValueSource value)
 	{
 		defaultValue = value;
 	}
 
 	public DialogFieldPopup getPopup()
 	{
 		return popup;
 	}
 
 	public void setPopup(DialogFieldPopup value)
 	{
 		popup = value;
 	}
 
 	/**
 	 * Gets a list of children fields
 	 *
 	 * @return List list of children fields
 	 */
 	public List getChildren()
 	{
 		return children;
 	}
 
 	/**
 	 * Adds a child field.
 	 *
 	 * @param field child field
 	 */
 	public void addChildField(DialogField field)
 	{
 		for (int i = 0; i < CHILD_CARRY_FLAGS.length; i++)
 		{
 			int flag = CHILD_CARRY_FLAGS[i];
 			if ((flags & flag) != 0)
 				field.setFlag(flag);
 		}
 
 		if (children == null) children = new ArrayList();
 		children.add(field);
 
 		field.setParent(this);
 		if (qualifiedName != null)
 			field.setQualifiedName(qualifiedName + "." + field.getSimpleName());
 	}
 
 	/**
 	 * Gets a list of errors
 	 *
 	 * @return List list of errors
 	 */
 	public List getErrors()
 	{
 		return errors;
 	}
 
 	/**
 	 * Adds a error message for the field
 	 *
 	 * @param msg error message
 	 */
 	public void addErrorMessage(String msg)
 	{
 		if (errors == null) errors = new ArrayList();
 		errors.add(msg);
 	}
 
 	/**
 	 * Get a list of conditional actions
 	 *
 	 * @return List a list of conditional actions
 	 */
 	public List getConditionalActions()
 	{
 		return conditionalActions;
 	}
 
 	/**
 	 * Adds a conditional action
 	 *
 	 * @param action conditional action object
 	 */
 	public void addConditionalAction(DialogFieldConditionalAction action)
 	{
 		if (conditionalActions == null) conditionalActions = new ArrayList();
 
 		if (action instanceof DialogFieldConditionalData || action instanceof DialogFieldConditionalApplyFlag)
 			setFlag(FLDFLAG_HAS_CONDITIONAL_DATA);
 
 		conditionalActions.add(action);
 	}
 
 	/**
 	 * Gets all the javascripts defined for this field
 	 *
 	 * @return ArrayList
 	 */
 	public List getClientJavascripts()
 	{
 		return this.clientJavascripts;
 	}
 
 	/**
 	 * Adds a javascript to the list of scripts defined for this field
 	 *
 	 * @param script custom js object
 	 */
 	public void addClientJavascript(DialogFieldClientJavascript script)
 	{
 		if (this.clientJavascripts == null)
 			this.clientJavascripts = new ArrayList();
 		this.clientJavascripts.add(script);
 	}
 
 	/**
 	 * Returns the current dialog field or one of its children which has the passed in qualified name
 	 *
 	 * @param qualifiedName qualified name
 	 * @return DialogField
 	 */
 	public DialogField findField(String qualifiedName)
 	{
 		if (this.qualifiedName != null && this.qualifiedName.equals(qualifiedName))
 			return this;
 
 		if (children != null)
 		{
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				DialogField field = (DialogField) i.next();
 				DialogField found = field.findField(qualifiedName);
 				if (found != null)
 					return found;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Set the qualified name of the field and its' children fields. The qualified name of a field is a sum of
 	 * the parent field's qualified name and the field's simple name.
 	 *
 	 * @param dialog parent dialog
 	 */
 	public void finalizeQualifiedName(Dialog dialog)
 	{
 		String newQName = simpleName;
 		if (parent != null && simpleName != null)
 			newQName = parent.getQualifiedName() + "." + simpleName;
 		if (newQName != null)
 			setQualifiedName(newQName);
 
 		if (children != null)
 		{
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				DialogField field = (DialogField) i.next();
 				field.finalizeQualifiedName(dialog);
 			}
 		}
 	}
 
 	/**
 	 * Finalize the dialog field's contents: loops through each conditional action of the field to
 	 * assign partner fields and loops through each child field to finalize their contents.
 	 *
 	 * @param dialog parent dialog
 	 */
 	public void finalizeContents(Dialog dialog)
 	{
 		if (conditionalActions != null)
 		{
 			Iterator i = conditionalActions.iterator();
 			while (i.hasNext())
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) i.next();
 				DialogField partnerField = dialog.findField(action.getPartnerFieldName());
 				if (partnerField != null)
 					action.setPartnerField(partnerField);
 			}
 		}
 
 		if (children != null)
 		{
 			Iterator c = children.iterator();
 			while (c.hasNext())
 			{
 				DialogField field = (DialogField) c.next();
 				field.finalizeContents(dialog);
 			}
 		}
 
 		if (flagIsSet(DialogField.FLDFLAG_DOUBLEENTRY))
 			this.setupDoubleEntry();
 	}
 
 	public void setupDoubleEntry()
 	{
 		this.setHint("Double Entry");
 		DialogFieldClientJavascript doubleEntryJS = new DialogFieldClientJavascript();
 		doubleEntryJS.setType("extends");
 		doubleEntryJS.setEvent("lose-focus");
 		doubleEntryJS.setScript("doubleEntry(field, control)");
 		this.addClientJavascript(doubleEntryJS);
 
 		DialogFieldClientJavascript deOnChangeJS = new DialogFieldClientJavascript();
 		deOnChangeJS.setType("extends");
 		deOnChangeJS.setEvent("value-changed");
 		deOnChangeJS.setScript("field.successfulEntry = false");
 		this.addClientJavascript(deOnChangeJS);
 	}
 
 	public List getDependentConditions()
 	{
 		return dependentConditions;
 	}
 
 	public void addDependentCondition(DialogFieldConditionalAction action)
 	{
 		if (dependentConditions == null) dependentConditions = new ArrayList();
 		dependentConditions.add(action);
 	}
 
 	public final long getFlags()
 	{
 		return flags;
 	}
 
 	/**
 	 * Check if a flag is set
 	 *
 	 * @param flag      bit-mapped flag
 	 * @return boolean  True if the passed in flag is set, else False
 	 */
 	public final boolean flagIsSet(long flag)
 	{
 		return (flags & flag) == 0 ? false : true;
 	}
 
 	/**
 	 * Set a flag
 	 *
 	 * @param flag bit-mapped flag
 	 */
 	public final void setFlag(long flag)
 	{
 		flags |= flag;
 		if (children != null)
 		{
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				((DialogField) i.next()).setFlag(flag);
 			}
 		}
 	}
 
 	/**
 	 * Cleat a flag
 	 *
 	 * @param flag bit-mapped flag
 	 */
 	public final void clearFlag(long flag)
 	{
 		flags &= ~flag;
 		if (children != null)
 		{
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				((DialogField) i.next()).clearFlag(flag);
 			}
 		}
 	}
 
 	/**
 	 * Indicates whether or not the field is a required field. It checks the  <code>FLDFLAG_REQUIRED</code>
 	 * flag of the field and its' children.
 	 *
 	 * @param dc  dialog context
 	 */
 	public boolean isRequired(DialogContext dc)
 	{
 		String qName = getQualifiedName();
 		if (qName != null)
 		{
 			if (dc.flagIsSet(qName, FLDFLAG_REQUIRED)) return true;
 		}
 		else
 		{
 			if (flagIsSet(FLDFLAG_REQUIRED)) return true;
 		}
 
 		if (children != null)
 		{
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				if (((DialogField) i.next()).isRequired(dc))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether or not the field is visible. The check is done by seeing if the invisible flag, <code>FLDFLAG_INVISIBLE</code>
 	 * is set or not and by making sure each partner field of its' conditionals have a value or not.
 	 *
 	 * @param dc dialog context
 	 * @return boolean True if the field is visible
 	 */
 	public boolean isVisible(DialogContext dc)
 	{
 		if (flagIsSet(FLDFLAG_HAS_CONDITIONAL_DATA))
 		{
 			Iterator i = conditionalActions.iterator();
 			while (i.hasNext())
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) i.next();
 				if (action instanceof DialogFieldConditionalData)
 				{
 					// if the partner field doesn't have data yet, hide this field
 					if (isRequired(dc))
 					{
 						String value = dc.getValue(action.getPartnerField());
 						if (value == null || value.length() == 0)
 							return false;
 					}
 				}
 			}
 		}
 		String qName = getQualifiedName();
 		if (qName != null)
 		{
 			DialogContext.DialogFieldState state = dc.getFieldState(qName);
 			if (state.flagIsSet(FLDFLAG_INVISIBLE))
 				return false;
 
 			if (children == null && state.flagIsSet(FLDFLAG_READONLY) &&
 				(state.flagIsSet(FLDFLAG_READONLY_INVISIBLE_UNLESS_HAS_DATA) ||
 				dc.getDialog().flagIsSet(Dialog.DLGFLAG_READONLY_FIELDS_INVISIBLE_UNLESS_HAVE_DATA)))
 			{
 				Object value = state.getValueAsObject();
 				return value == null ? false : (value instanceof String ? (((String) value).length() == 0 ? false : true) : true);
 			}
 			else
 				return true;
 		}
 		else
 		{
 			return flagIsSet(FLDFLAG_INVISIBLE) ? false : true;
 		}
 	}
 
 	public boolean isReadOnly(DialogContext dc)
 	{
 		String qName = getQualifiedName();
 		if (qName != null)
 			return dc.flagIsSet(qName, FLDFLAG_READONLY);
 		else
 			return flagIsSet(FLDFLAG_READONLY);
 	}
 
 	public boolean isBrowserReadOnly(DialogContext dc)
 	{
 		String qName = getQualifiedName();
 		if (qName != null)
 			return dc.flagIsSet(qName, FLDFLAG_BROWSER_READONLY);
 		else
 			return flagIsSet(FLDFLAG_BROWSER_READONLY);
 	}
 
 	public boolean isInputHiddenFlagSet(DialogContext dc)
 	{
 		String qName = getQualifiedName();
 		if (qName != null)
 		{
 			DialogContext.DialogFieldState state = dc.getFieldState(qName);
 			return state.flagIsSet(FLDFLAG_INPUT_HIDDEN);
 		}
 		else
 			return flagIsSet(FLDFLAG_INPUT_HIDDEN);
 	}
 
 	public boolean isInputHidden(DialogContext dc)
 	{
 		String qName = getQualifiedName();
 		if (qName != null)
 		{
 			DialogContext.DialogFieldState state = dc.getFieldState(qName);
 			if (state.flagIsSet(FLDFLAG_INPUT_HIDDEN))
 				return true;
 
 			if (children == null && state.flagIsSet(FLDFLAG_READONLY) &&
 				(state.flagIsSet(FLDFLAG_READONLY_HIDDEN_UNLESS_HAS_DATA) ||
 				dc.getDialog().flagIsSet(Dialog.DLGFLAG_READONLY_FIELDS_HIDDEN_UNLESS_HAVE_DATA)))
 			{
 				Object value = state.getValueAsObject();
 				return value == null ? true : (value instanceof String ? (((String) value).length() == 0 ? true : false) : false);
 			}
 			else
 				return false;
 		}
 		else
 			return flagIsSet(FLDFLAG_INPUT_HIDDEN);
 	}
 
 	public boolean persistValue()
 	{
 		return (flags & FLDFLAG_PERSIST) == 0 ? false : true;
 	}
 
 	public boolean showCaptionAsChild()
 	{
 		return (flags & FLDFLAG_SHOWCAPTIONASCHILD) == 0 ? false : true;
 	}
 
 	public static final String escapeHTML(String s)
 	{
 		if (s == null) return null;
 		StringBuffer sb = new StringBuffer();
 		int n = s.length();
 		for (int i = 0; i < n; i++)
 		{
 			char c = s.charAt(i);
 			switch (c)
 			{
 				case '<': sb.append("&lt;"); break;
 				case '>': sb.append("&gt;"); break;
 				case '&': sb.append("&amp;"); break;
 				case '"': sb.append("&quot;"); break;
 				case '': sb.append("&agrave;"); break;
 				case '': sb.append("&Agrave;"); break;
 				case '': sb.append("&acirc;"); break;
 				case '': sb.append("&Acirc;"); break;
 				case '': sb.append("&auml;"); break;
 				case '': sb.append("&Auml;"); break;
 				case '': sb.append("&aring;"); break;
 				case '': sb.append("&Aring;"); break;
 				case '': sb.append("&aelig;"); break;
 				case '': sb.append("&AElig;"); break;
 				case '': sb.append("&ccedil;"); break;
 				case '': sb.append("&Ccedil;"); break;
 				case '': sb.append("&eacute;"); break;
 				case '': sb.append("&Eacute;"); break;
 				case '': sb.append("&egrave;"); break;
 				case '': sb.append("&Egrave;"); break;
 				case '': sb.append("&ecirc;"); break;
 				case '': sb.append("&Ecirc;"); break;
 				case '': sb.append("&euml;"); break;
 				case '': sb.append("&Euml;"); break;
 				case '': sb.append("&iuml;"); break;
 				case '': sb.append("&Iuml;"); break;
 				case '': sb.append("&ocirc;"); break;
 				case '': sb.append("&Ocirc;"); break;
 				case '': sb.append("&ouml;"); break;
 				case '': sb.append("&Ouml;"); break;
 				case '': sb.append("&oslash;"); break;
 				case '': sb.append("&Oslash;"); break;
 				case '': sb.append("&szlig;"); break;
 				case '': sb.append("&ugrave;"); break;
 				case '': sb.append("&Ugrave;"); break;
 				case '': sb.append("&ucirc;"); break;
 				case '': sb.append("&Ucirc;"); break;
 				case '': sb.append("&uuml;"); break;
 				case '': sb.append("&Uuml;"); break;
 				case '': sb.append("&reg;"); break;
 				case '': sb.append("&copy;"); break;
 				case '': sb.append("&euro;"); break;
 
 				default:
 					sb.append(c); break;
 			}
 		}
 		return sb.toString();
 	}
 
 	public String getHiddenControlHtml(DialogContext dc)
 	{
 		String value = dc.getValue(this);
 		return "<input type='hidden' name='" + getId() + "' value=\"" + (value != null ? escapeHTML(value) : "") + "\">";
 	}
 
 	public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
 	{
 		if (isInputHidden(dc))
 		{
 			getHiddenControlHtml(dc);
 			return;
 		}
 
 		if (children == null)
 			return;
 
 		dc.getSkin().renderCompositeControlsHtml(writer, dc, this);
 	}
 
 	public boolean needsValidation(DialogContext dc)
 	{
 		if (flagIsSet(FLDFLAG_HAS_CONDITIONAL_DATA))
 			return true;
 
 		if (children == null)
 			return isRequired(dc);
 
 		int validateFieldsCount = 0;
 		Iterator i = children.iterator();
 		while (i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if (field.isVisible(dc) && field.needsValidation(dc))
 				validateFieldsCount++;
 		}
 
 		return validateFieldsCount > 0 ? true : false;
 	}
 
 	public boolean defaultIsValid(DialogContext dc)
 	{
 		if (flagIsSet(FLDFLAG_HAS_CONDITIONAL_DATA))
 		{
 			Iterator i = conditionalActions.iterator();
 			while (i.hasNext())
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) i.next();
 				if (action instanceof DialogFieldConditionalData)
 				{
 					// if the partner field doesn't have data, then this field is "invalid"
 					if (isRequired(dc) && dc.getValue(action.getPartnerField()) == null)
 						return false;
 				}
 			}
 		}
 		if (children == null)
 			return true;
 
 		int invalidFieldsCount = 0;
 		Iterator i = children.iterator();
 		while (i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if (field.isVisible(dc) && (!field.isValid(dc)))
 				invalidFieldsCount++;
 		}
 		return invalidFieldsCount == 0 ? true : false;
 	}
 
 	public boolean isValid(DialogContext dc)
 	{
 		return defaultIsValid(dc);
 	}
 
 	/**
 	 * Format the dialog value after it has been validated and is ready for submission
 	 *
 	 * @param value dialog field value
 	 * @return String
 	 */
 	public String formatSubmitValue(String value)
 	{
 		return value;
 	}
 
 	/**
 	 * Format the dialog value for every dialog stage except before submission
 	 *
 	 * @param value dialog field value
 	 * @return String
 	 */
 	public String formatDisplayValue(String value)
 	{
 		return value;
 	}
 
 
 	public Object getValueAsObject(String value)
 	{
 		return value;
 	}
 
 	public Object getValueForSqlBindParam(String value)
 	{
 		return getValueAsObject(value);
 	}
 
 	public void makeStateChanges(DialogContext dc, int stage)
 	{
 		if (stage == DialogContext.STATECALCSTAGE_INITIAL && conditionalActions != null)
 		{
 			for (int i = 0; i < conditionalActions.size(); i++)
 			{
 				DialogFieldConditionalAction action = (DialogFieldConditionalAction) conditionalActions.get(i);
 				if (action instanceof DialogFieldConditionalApplyFlag)
 					((DialogFieldConditionalApplyFlag) action).applyFlags(dc);
 			}
 		}
 
 		if (children != null)
 		{
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				DialogField field = (DialogField) i.next();
 				field.makeStateChanges(dc, stage);
 			}
 		}
 	}
 
 	public void populateValue(DialogContext dc, int formatType)
 	{
 		if (id == null) return;
 
 		String value = dc.getValue(this);
 		if (value == null)
 			value = dc.getRequest().getParameter(id);
 		if (dc.getRunSequence() == 1)
 		{
 			if ((value != null && value.length() == 0 && defaultValue != null) ||
 				(value == null && defaultValue != null))
 				value = defaultValue.getValueOrBlank(dc);
 		}
 		if (formatType == DialogField.DISPLAY_FORMAT)
 			dc.setValue(this, this.formatDisplayValue(value));
 		else if (formatType == DialogField.SUBMIT_FORMAT)
 			dc.setValue(this, this.formatSubmitValue(value));
 
 		if (children == null) return;
 
 		Iterator i = children.iterator();
 		while (i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			if (field.isVisible(dc)) field.populateValue(dc, formatType);
 		}
 	}
 
 	/**
 	 * Produces JavaScript code to handle Client-side events for the dialog field
 	 *
 	 */
 	public String getJavaScriptDefn(DialogContext dc)
 	{
 		String fieldClassName = this.getClass().getName();
 		String fieldQualfName = this.getQualifiedName();
 		String js =
 			"field = new DialogField(\"" + fieldClassName + "\", \"" + this.getId() + "\", \"" + this.getSimpleName() + "\", \"" + fieldQualfName + "\", \"" + this.getCaption(dc) + "\", " + dc.getFieldFlags(fieldQualfName) + ");\n" +
 			"dialog.registerField(field);\n";
 		String customStr = this.getEventJavaScriptFunctions(dc);
 		customStr += this.getCustomJavaScriptDefn(dc);
 		if (customStr != null)
 			js += customStr;
 
 		List dependentConditions = this.getDependentConditions();
 		if (dependentConditions != null)
 		{
 			StringBuffer dcJs = new StringBuffer();
 			Iterator i = dependentConditions.iterator();
 			while (i.hasNext())
 			{
 				DialogFieldConditionalAction o = (DialogFieldConditionalAction) i.next();
 
 				if (o instanceof DialogFieldConditionalDisplay)
 				{
 					DialogFieldConditionalDisplay action = (DialogFieldConditionalDisplay) o;
 					if (action.getPartnerField().isVisible(dc))
 						dcJs.append("field.dependentConditions[field.dependentConditions.length] = new DialogFieldConditionalDisplay(\"" + action.getSourceField().getQualifiedName()
 							+ "\", \"" + action.getPartnerField().getQualifiedName() + "\", \"" + action.getExpression() + "\");\n");
 				}
 			}
 			js = js + dcJs.toString();
 		}
 
 		List children = this.getChildren();
 		if (children != null)
 		{
 			StringBuffer childJs = new StringBuffer();
 			Iterator i = children.iterator();
 			while (i.hasNext())
 			{
 				DialogField child = (DialogField) i.next();
 				childJs.append(child.getJavaScriptDefn(dc));
 			}
 			js = js + childJs.toString();
 		}
 
 		return js;
 	}
 
 	/**
 	 * Retrieves user defined java script strings for this field and creates JS functions
 	 * out of them
 	 */
 	public String getEventJavaScriptFunctions(DialogContext dc)
 	{
 		String ret = "";
 
 		List jsList = this.getClientJavascripts();
 		if (jsList != null)
 		{
 			String eventName = "";
 
 			StringBuffer jsBuffer = new StringBuffer();
 			Iterator i = jsList.iterator();
 			while (i.hasNext())
 			{
 				DialogFieldClientJavascript jsObject = (DialogFieldClientJavascript) i.next();
 				String script = (jsObject.getScript() != null ? jsObject.getScript().getValue(dc) : null);
 				eventName = com.netspective.sparx.util.xml.XmlSource.xmlTextToJavaIdentifier(jsObject.getEvent().getValue(dc), false);
 				// append function signature
 				if (script != null)
 				{
 					jsBuffer.append("field.customHandlers." + eventName + " = new Function(\"field\", \"control\", \"" +
 						jsObject.getScript().getValue(dc) + "\");\n");
 					jsBuffer.append("field.customHandlers." + eventName + "Type = '" + jsObject.getType().getValue(dc) + "';\n");
 				}
 			}
 			ret = ret + jsBuffer.toString();
 		}
 		return ret;
 	}
 
 	public String getCustomJavaScriptDefn(DialogContext dc)
 	{
 		StringBuffer sb = new StringBuffer();
 
 		if (flagIsSet(FLDFLAG_DOUBLEENTRY))
 		{
 			sb.append("field.doubleEntry = 'yes';\n");
 			sb.append("field.firstEntryValue = '';\n");
 			sb.append("field.successfulEntry = true;\n");
 		}
 
 		if (flagIsSet(FLDFLAG_SCANNABLE))
 		{
 			sb.append("field.scannable = 'yes';\n");
 			sb.append("field.scanStartCode = '" + scanStartCode + "';\n");
 			sb.append("field.scanStopCode = '" + scanStopCode + "';\n");
			sb.append("field.scanCodeIgnoreCase = '" + scanCodeIgnoreCase + "';\n");
 			sb.append("field.isScanned = false;\n");
 			sb.append("field.scanPartnerField = '" + scanPartnerField + "';\n");
 			if(scanFieldCustomScript != null && scanFieldCustomScript.length() > 0)
				sb.append("field.scanFieldCustomScript = new Function(\"field\", \"control\", \"inputString\", \"" + scanFieldCustomScript + "\");\n");
 			else
 				sb.append("field.scanFieldCustomScript = '';\n");
 		}
 
 		if (flagIsSet(FLDFLAG_AUTOBLUR))
 		{
 			sb.append("field.autoBlur = 'yes';\n");
 			sb.append("field.autoBlurLength = " + autoBlurLength + ";\n");
 			sb.append("field.autoBlurExcRegExp = '" + autoBlurExcludeRegExp + "';\n");
 			sb.append("field.numCharsEntered = 0;\n");
 		}
 
 		if (flagIsSet(DialogField.FLDFLAG_SUBMIT_ONBLUR))
 		{
 			sb.append("field.submitOnBlur = true;\n");
 			sb.append("field.submitOnBlurPartnerField ='" + submitOnBlurPartnerField + "';\n");
 			if(submitOnBlurCustomScript != null && submitOnBlurCustomScript.length() >0)
 				sb.append("field.submitOnBlurCustomScript = new Function(\"field\", \"control\", \"" + submitOnBlurCustomScript + "\");\n");
 			else
 				sb.append("field.submitOnBlurCustomScript = '';\n");
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * Produces Java code when a custom DialogContext is created
 	 */
 	public DialogContextMemberInfo createDialogContextMemberInfo(String dataTypeName)
 	{
 		DialogContextMemberInfo mi = new DialogContextMemberInfo(this.getQualifiedName(), dataTypeName);
 
 		String memberName = mi.getMemberName();
 		String fieldName = mi.getFieldName();
 
 		mi.addJavaCode("\t/* To change the following auto-generated code, modify createDialogContextMemberInfo() method in " + this.getClass().getName() + " */\n");
 		mi.addJavaCode("\tpublic boolean is" + memberName + "ValueSet() { return hasValue(\"" + fieldName + "\"); }\n");
 		mi.addJavaCode("\tpublic boolean is" + memberName + "FlagSet(long flag) { return flagIsSet(\"" + fieldName + "\", flag); }\n");
 		mi.addJavaCode("\tpublic void set" + memberName + "Flag(long flag) { setFlag(\"" + fieldName + "\", flag); }\n");
 		mi.addJavaCode("\tpublic void clear" + memberName + "Flag(long flag) { clearFlag(\"" + fieldName + "\", flag); }\n");
 		mi.addJavaCode("\tpublic String get" + memberName + "RequestParam() { return request.getParameter(\"" + Dialog.PARAMNAME_CONTROLPREFIX + fieldName + "\"); }\n");
 		mi.addJavaCode("\tpublic DialogField get" + memberName + "Field() { return getField(\"" + fieldName + "\"); }\n");
 		mi.addJavaCode("\tpublic DialogContext.DialogFieldState get" + memberName + "FieldState() { return getFieldState(\"" + fieldName + "\"); }\n");
 		mi.addJavaCode("\tpublic void add" + memberName + "ErrorMsg(String msg) { addErrorMessage(\"" + fieldName + "\", msg); }\n");
 
 		if (isTableColumnField())
 		{
 			mi.addJavaCode("\tpublic " + dalProperties.get("dal-column-data-type-class") + " get" + memberName + "Column() { return (" + dalProperties.get("dal-column-data-type-class") + ") getField(\"" + fieldName + "\").getColumn(this); }\n");
 			mi.addJavaCode("\tpublic " + dalProperties.get("dal-table-class-name") + " get" + memberName + "Table() { return (" + dalProperties.get("dal-table-class-name") + ") getField(\"" + fieldName + "\").getTable(this); }\n");
 		}
 
 		return mi;
 	}
 
 	/**
 	 * Produces Java code when a custom DialogContext is created
 	 * The default method produces nothing; all the subclasses must define what they need.
 	 */
 	public DialogContextMemberInfo getDialogContextMemberInfo()
 	{
 		if (children == null) return null;
 
 		DialogContextMemberInfo mi = createDialogContextMemberInfo("children");
 		Iterator i = children.iterator();
 		while (i.hasNext())
 		{
 			DialogField field = (DialogField) i.next();
 			DialogContextMemberInfo childMI = field.getDialogContextMemberInfo();
 			if (childMI == null)
 				continue;
 
 			String[] childImports = childMI.getImportModules();
 			if (childImports != null)
 			{
 				for (int m = 0; m < childImports.length; m++)
 					mi.addImportModule(childImports[m]);
 			}
 
 			mi.addJavaCode(childMI.getCode());
 		}
 
 		return mi;
 	}
 }
