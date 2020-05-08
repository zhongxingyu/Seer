 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CheckBoxInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 
 /**
  * @author chetan patil
  * @version 1.0
  * @created 28-Sep-2006 12:20:07 PM
  * @hibernate.joined-subclass table="DYEXTN_CHECK_BOX"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class CheckBox extends Control implements CheckBoxInterface
 {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Empty Constructor
 	 */
 	public CheckBox()
 	{
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.domain.userinterface.Control#generateEditModeHTML()
 	 */
 	protected String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		String checked = (String) this.value;
 		String htmlString = "";
 		if (this.value == null)
 		{
 			checked = this.getAttibuteMetadataInterface().getDefaultValue();
 		}
		String disabled = "";
//		If control is defined as readonly through category CSV file,make it Disabled
		if(this.isReadOnly!=null && getIsReadOnly())
		{
			disabled = ProcessorConstants.DISABLED;
		}
		
 		String htmlComponentName = getHTMLComponentName();
 		if (checked != null &&(checked.equals("true") || checked.equals("1")))
 		{
 			htmlString = "<input type='checkbox' class='" + this.cssClass + "' name='" + htmlComponentName + "' checkedValue='"
 					+ DynamicExtensionsUtility.getValueForCheckBox(true) + "' uncheckedValue='" + DynamicExtensionsUtility.getValueForCheckBox(false)
					+ "'" + "value='" + DynamicExtensionsUtility.getValueForCheckBox(true) + "' " + "id='" + htmlComponentName + "'" + "checked" + disabled 
 					+ " onclick='changeValueForCheckBox(this);'>";
 		}
 		else
 		{
 			htmlString = "<input type='checkbox' class='" + this.cssClass + "' name='" + htmlComponentName + "' checkedValue='"
 					+ DynamicExtensionsUtility.getValueForCheckBox(true) + "' uncheckedValue='" + DynamicExtensionsUtility.getValueForCheckBox(false)
					+ "'" + "value='" + DynamicExtensionsUtility.getValueForCheckBox(false) + "' " + disabled  + "id='" + htmlComponentName
 					+ "' onclick='changeValueForCheckBox(this);'>";
 		}
 
 		return htmlString;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.domain.userinterface.Control#generateViewModeHTML()
 	 */
 	protected String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		String htmlString = "&nbsp;";
 		if (value != null)
 		{
 			String checked = (String) this.value;
 			htmlString = "<input type='checkbox' class='" + cssClass + "' " + DynamicExtensionsUtility.getCheckboxSelectionValue(checked)
 					+ " disabled>";
 		}
 		return htmlString;
 	}
 
 	/**
 	 * This method sets the corresponding AbstractAttribute of this Control.
 	 * @param abstractAttribute AbstractAttribute to be set.
 	 */
 	public void setAttribute(AbstractAttributeInterface abstractAttribute)
 	{
 	}
 
 }
