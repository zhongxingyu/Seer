 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextAreaInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.Constants;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:09 PM
  * @hibernate.joined-subclass table="DYEXTN_TEXTAREA" 
  * @hibernate.joined-subclass-key column="IDENTIFIER" 
  */
 public class TextArea extends Control implements TextAreaInterface
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 201964699680324430L;
 	/**
 	 * Number of columns in the text area.
 	 */
 	protected Integer columns;
 	/**
 	 * Number of rows in the text area.
 	 */
 	protected Integer rows;
 	//password
 	protected Boolean isPassword;
 
 	/**
 	 * Empty Constructor 
 	 */
 	public TextArea()
 	{
 	}
 
 	/**
 	 * @hibernate.property name="columns" type="integer" column="TEXTAREA_COLUMNS" 
 	 * @return Returns the columns.
 	 */
 	public Integer getColumns()
 	{
 		return columns;
 	}
 
 	/**
 	 * @param columns The columns to set.
 	 */
 	public void setColumns(Integer columns)
 	{
 		this.columns = columns;
 	}
 
 	/**
 	 * @hibernate.property name="rows" type="integer" column="TEXTAREA_ROWS" 
 	 * @return Returns the rows.
 	 */
 	public Integer getRows()
 	{
 		return rows;
 	}
 
 	/**
 	 * @param rows The rows to set.
 	 */
 	public void setRows(Integer rows)
 	{
 		this.rows = rows;
 	}
 
 	/**
 	 * This method generates the HTML code for TextArea control on the HTML form
 	 * @return HTML code for TextArea
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		String defaultValue = (String) this.value;
 
 		if (this.value == null)
 		{
 			defaultValue = this.getAttibuteMetadataInterface().getDefaultValue();
 		}
 
 		String htmlComponentName = getHTMLComponentName();
 
 		String htmlString = "<textarea " + "class='font_bl_nor' " + "name='" + htmlComponentName + "' " + "id='" + htmlComponentName + "' ";
 
 		//set isdisabled property
		htmlString += " isDisabled='" + (this.isReadOnly != null && this.isReadOnly ? ProcessorConstants.TRUE : ProcessorConstants.FALSE) + "' ";

 		int noCols = columns.intValue();
 		int noRows = rows.intValue();

 		if (noCols > 0)
 		{
 			htmlString += "cols='" + noCols + "' ";
 		}
 		else
 		{
 			htmlString += "cols='" + Constants.DEFAULT_COLUMN_SIZE + "' ";
 		}
 
 		if (noRows > 0)
 		{
 			htmlString += "rows='" + noRows + "' ";
 		}
 		else
 		{
 			htmlString += "rows='" + Constants.DEFAULT_ROW_SIZE + "' ";
 		}
 
 		int maxChars = 0;
 		AttributeMetadataInterface attibute = this.getAttibuteMetadataInterface();
 		if (attibute != null)
 		{
 			maxChars = attibute.getMaxSize();
 		}
 
 		if (maxChars > 0)
 		{
 			htmlString += " onblur='textCounter(this," + maxChars + ")'  ";
 		}
 
 		htmlString += " wrap='virtual'>";
 
 		if (defaultValue == null || (defaultValue.length() == 0))
 		{
 			htmlString += "</textarea>";
 		}
 		else
 		{
 			htmlString += defaultValue + "</textarea>";
 		}
 		return htmlString;
 	}
 
 	/**
 	 * This method associates the Attribute to this Control.
 	 * @param attribute the Attribute to be associated.
 	 */
 	public void setAttribute(AbstractAttributeInterface attribute)
 	{
 	}
 
 	/**
 	 * This method returns the Boolean indicating whether the value is to be shown in password style or not.
 	 * If Boolean is true then show value as password otherwise not.
 	 * @return the Boolean indicating whether the value is to be shown in password style or not.
 	 */
 	public Boolean getIsPassword()
 	{
 		return isPassword;
 	}
 
 	/**
 	 * This method sets the Boolean indicating whether the value is to be shown in password style or not.
 	 * If Boolean is true then show value as password otherwise not.
 	 * @param isPassword the Boolean indicating whether the value is to be shown in password style or not.
 	 */
 	public void setIsPassword(Boolean isPassword)
 	{
 		this.isPassword = isPassword;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.domain.userinterface.Control#generateViewModeHTML()
 	 */
 	protected String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 
 		String htmlString = "&nbsp;";
 		if (value != null)
 		{
 			htmlString = "<span class = 'font_bl_nor'> " + this.value.toString() + "</span>";
 		}
 		return htmlString;
 
 	}
 
 }
