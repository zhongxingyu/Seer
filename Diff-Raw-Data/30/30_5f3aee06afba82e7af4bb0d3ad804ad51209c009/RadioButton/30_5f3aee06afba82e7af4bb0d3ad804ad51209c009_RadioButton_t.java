 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.util.List;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.RadioButtonInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:08 PM
  * @hibernate.joined-subclass table="DYEXTN_RADIOBUTTON" 
  * @hibernate.joined-subclass-key column="IDENTIFIER" 
  */
 public class RadioButton extends Control implements RadioButtonInterface
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Empty Constructor 
 	 */
 	public RadioButton()
 	{
 	}
 
 	/**
 	 * This method generates the HTML code for RadioButton control on the HTML form
 	 * @return HTML code for RadioButton
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		List<NameValueBean> nameValueBeanList = null;
 		String htmlString = "";
 
 		String defaultValue = (String) this.value;
 		if (defaultValue == null)
 		{
 			defaultValue = ControlsUtility.getDefaultValue(this.getAbstractAttribute());
 			if (defaultValue == null || defaultValue.length() == 0)
 			{
 				defaultValue = "";
 			}
 		}
 
 		nameValueBeanList = ControlsUtility.populateListOfValues(this);
 
 		String htmlComponentName = getHTMLComponentName();
 		if (nameValueBeanList != null || nameValueBeanList.size() > 0)
 		{
 			for (NameValueBean nameValueBean : nameValueBeanList)
 			{
 				String optionName = nameValueBean.getName();
 				String optionValue = nameValueBean.getValue();
 				if (optionValue.equals(defaultValue))
 				{
 					htmlString += "<input type='radio' " + "class='" + cssClass + "' " + "name='"
 							+ htmlComponentName + "' " + "value='" + optionValue + "' " + "id='"
							+ optionName + "' checked>"  
							 + "<label for=\"" + htmlComponentName + "\">"
							+ optionName
							+ "</label> ";
 				}
 				else
 				{
 					htmlString += "<input type='radio' " + "class='" + cssClass + "' " + "name='"
 							+ htmlComponentName + "' " + "value='" + optionValue + "' " + "id='"
							+ optionName + "'>" 
							+ "<label for=\"" + htmlComponentName + "\">" + optionName
 							+ "</label>";
 				}
 			}
 		}
 
 		return htmlString;
 	}
 
 	/**
 	 * This method sets the corresponding Attribute of this control.
 	 * @param abstractAttribute the AbstractAttribute to be set. 
 	 */
 	public void setAttribute(AbstractAttributeInterface abstractAttribute)
 	{
 	}
 
 	public String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		String htmlString = "&nbsp;";
 		if (value != null)
 		{
 			htmlString = "<span class = '" + cssClass + "'> " + this.value.toString() + "</span>";
 		}
 		return htmlString;
 	}
 
 }
