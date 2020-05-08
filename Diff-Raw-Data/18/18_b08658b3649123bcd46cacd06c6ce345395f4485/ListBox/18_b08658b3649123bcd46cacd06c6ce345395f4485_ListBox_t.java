 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerUtil;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:08 PM
  * @hibernate.joined-subclass table="DYEXTN_LIST_BOX"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class ListBox extends SelectControl implements ListBoxInterface
 {
 
 	/**
 	 * Serial Version Unique Identifier
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Boolean indicating whether multi selects are allowed in the list box.
 	 */
 	private Boolean isMultiSelect = null;
 
 	/**
 	 * Number of rows to be displayed on the UI for ListBox.
 	 */
 	private Integer noOfRows = null;
 
 	/**
 	 * The list of values to be displayed in the ListBox
 	 */
 	private List listOfValues = null;
 
 	/**
 	 * This method returns the Number of rows to be displayed on the UI for ListBox.
 	 * @hibernate.property name="noOfRows" type="integer" column="NO_OF_ROWS"
 	 * @return Returns the noOfRows.
 	 */
 	public Integer getNoOfRows()
 	{
 		return this.noOfRows;
 	}
 
 	/**
 	 * This method sets the Number of rows to be displayed on the UI for ListBox.
 	 * @param noOfRows the Number of rows to be set for ListBox.
 	 */
 	public void setNoOfRows(Integer noOfRows)
 	{
 		this.noOfRows = noOfRows;
 	}
 
 	/**
 	 * Default Constructor
 	 */
 	public ListBox()
 	{
 	}
 
 	/**
 	 * This method returns whether the ListBox has a multiselect property or not.
 	 * @hibernate.property name="isMultiSelect" type="boolean" column="MULTISELECT"
 	 * @return whether the ListBox has a multiselect property or not.
 	 */
 	public Boolean getIsMultiSelect()
 	{
 		return isMultiSelect;
 	}
 
 	/**
 	 * This method sets whether the ListBox has a multiselect property or not.
 	 * @param isMultiSelect the Boolean value indicating whether the ListBox has a multiselect property or not.
 	 */
 	public void setIsMultiSelect(Boolean isMultiSelect)
 	{
 		this.isMultiSelect = isMultiSelect;
 	}
 
 	/**
 	 * This method generates the HTML code to display the ListBox Control on the form.
 	 * @return HTML code for ListBox Control.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		List<NameValueBean> nameValueBeanList = null;
 		List<String> valueList = new ArrayList<String>();
 		AssociationInterface association = getBaseAbstractAttributeAssociation();
 		if (association != null)
 		{
 			getValueList(association, valueList);
 		}
 		else
 		{
 			if (!(value instanceof List) && value != null)
 			{
 				List<String> tempList = new ArrayList<String>();
 				tempList.add((String) value);
 				valueList = tempList;
 			}
 			else
 			{
 				valueList = (List<String>) this.value;
 			}
 		}
 
 		String strMultiSelect = "";
 		if ((isMultiSelect != null) && (isMultiSelect.booleanValue() == true))
 		{
 			strMultiSelect = "MULTIPLE ";
 		}
 		String htmlString = "<SELECT " + strMultiSelect + " size=" + this.noOfRows + " class='font_bl_s' name='" + getHTMLComponentName() + "' id='"
 				+ name + "' ";
 
 		if (this.isReadOnly != null && this.isReadOnly)
 		{
 			htmlString += " disabled='" + ProcessorConstants.TRUE + "' ";
 		}
 		htmlString += ">";
 
 		if (valueList == null || valueList.isEmpty())
 		{
			String defaultValue = null;
 			valueList = new ArrayList<String>();
 
 			AttributeMetadataInterface attributeMetadataInterface = this.getAttibuteMetadataInterface();
 			if (attributeMetadataInterface != null)
 			{
				if (attributeMetadataInterface instanceof CategoryAttributeInterface)
				{
					AbstractAttributeInterface abstractAttribute = ((CategoryAttributeInterface) attributeMetadataInterface).getAbstractAttribute();
					if (abstractAttribute instanceof AttributeInterface)
					{
						defaultValue = attributeMetadataInterface.getDefaultValue();
					}
				}
				else
				{
					defaultValue = attributeMetadataInterface.getDefaultValue();
				}

 				if (defaultValue != null && defaultValue.trim().length() != 0)
 				{
 					valueList.add(defaultValue);
 				}
 			}
 		}
 
 		if (listOfValues == null)
 		{
 			nameValueBeanList = ControlsUtility.populateListOfValues(this);
 		}
 
 		if (nameValueBeanList != null && nameValueBeanList.size() > 0)
 		{
 			for (NameValueBean nameValueBean : nameValueBeanList)
 			{
 				if (valueList != null && !valueList.isEmpty() && valueList.contains(nameValueBean.getValue()))
 				{
 					htmlString += "<OPTION VALUE='" + nameValueBean.getValue() + "' SELECTED>" + nameValueBean.getName();
 				}
 				else
 				{
 					htmlString += "<OPTION VALUE='" + nameValueBean.getValue() + "'>" + nameValueBean.getName();
 				}
 			}
 		}
 		htmlString = htmlString + "</SELECT>";
 
 		return htmlString;
 	}
 
 	protected String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		List<String> selectedOptions = new ArrayList<String>();
 
 		AssociationInterface association = getBaseAbstractAttributeAssociation();
 		if (association != null)
 		{
 			getValueList(association, selectedOptions);
 		}
 		else
 		{
 			if (!(value instanceof List) && value != null)
 			{
 				List<String> tempList = new ArrayList<String>();
 				tempList.add((String) value);
 				selectedOptions = tempList;
 			}
 			else
 			{
 				selectedOptions = (List<String>) this.value;
 			}
 		}
 
 		//List<String> selectedOptions = (List<String>) this.value;
 		StringBuffer htmlString = new StringBuffer("&nbsp;");
 		if (value != null)
 		{
 			htmlString = new StringBuffer();
 			htmlString.append("<span class = '");
 			htmlString.append("font_bl_s");
 			htmlString.append("'>");
 			for (String string : selectedOptions)
 			{
 				htmlString.append(string);
 				htmlString.append("<br>");
 			}
 
 			htmlString.append("</span>");
 		}
 		return htmlString.toString();
 	}
 
 	/**
 	 * getValueList
 	 * @param association
 	 * @param valueList
 	 */
 	private void getValueList(AssociationInterface association, List<String> valueList)
 	{
 		if (association.getIsCollection())
 		{
 			Collection<AbstractAttributeInterface> attributeCollection = association.getTargetEntity().getAllAbstractAttributes();
 			Collection<AbstractAttributeInterface> filteredAttributeCollection = EntityManagerUtil.filterSystemAttributes(attributeCollection);
 			List<AbstractAttributeInterface> attributesList = new ArrayList<AbstractAttributeInterface>(filteredAttributeCollection);
 			List<Map> values = (List<Map>) this.value;
 			if (values != null)
 			{
 				for (Map valueMap : values)
 				{
 					String value = (String) valueMap.get(attributesList.get(0));
 					valueList.add(value);
 				}
 			}
 		}
 		else
 		{
 			if (!(value instanceof List) && value != null)
 			{
 				List<String> tempList = new ArrayList<String>();
 				tempList.add((String) value);
 				valueList = tempList;
 			}
 			else
 			{
 				if (this.value != null)
 				{
 					for (Long obj : (List<Long>) this.value)
 					{
 						valueList.add(obj.toString());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	public AssociationInterface getBaseAbstractAttributeAssociation()
 	{
 		AssociationInterface association = null;
 		if (baseAbstractAttribute != null && baseAbstractAttribute instanceof AssociationInterface)
 		{
 			association = (AssociationInterface) baseAbstractAttribute;
 		}
 		else if (baseAbstractAttribute != null && baseAbstractAttribute instanceof CategoryAttributeInterface)
 		{
 			CategoryAttributeInterface categoryAttributeInterface = (CategoryAttributeInterface) baseAbstractAttribute;
 			AbstractAttributeInterface abstractAttributeInterface = categoryAttributeInterface.getAbstractAttribute();
 			if (abstractAttributeInterface instanceof AssociationInterface)
 			{
 				association = (AssociationInterface) abstractAttributeInterface;
 			}
 		}
 		return association;
 	}
 }
