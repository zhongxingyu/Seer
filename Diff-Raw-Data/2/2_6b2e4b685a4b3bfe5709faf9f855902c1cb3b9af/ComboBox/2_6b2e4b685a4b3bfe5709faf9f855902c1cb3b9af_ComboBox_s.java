 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.util.List;
 
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ComboBoxInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:07 PM
  * @hibernate.joined-subclass table="DYEXTN_COMBOBOX"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class ComboBox extends SelectControl implements ComboBoxInterface
 {
 
 	/**
 	 * Serial Version Unique Identifier.
 	 */
 	private static final long serialVersionUID = 3062212342005513616L;
 
 	/**
 	 * List of Choices.
 	 */
 	List listOfValues = null;
 
 	/**
 	 * This method generates the HTML code for ComboBox control on the HTML form
 	 * @return HTML code for ComboBox
 	 * @throws DynamicExtensionsSystemException if HTMLComponentName() fails.
 	 */
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		String defaultValue = "";
 		if (this.value == null)
 		{
 			AttributeMetadataInterface attributeMetadataInterface = this
 					.getAttibuteMetadataInterface();
 			if (attributeMetadataInterface != null)
 			{
 				this.value = this.getAttibuteMetadataInterface().getDefaultValue();
 			}
 		}
 		if (this.value != null)
 		{
 			if (this.value instanceof String)
 			{
 				defaultValue = (String) this.value;
 			}
 			else if (this.value instanceof List)
 			{
 				List valueList = (List) this.value;
 				if (!valueList.isEmpty())
 				{
 					defaultValue = valueList.get(0).toString();
 				}
 			}
 		}
 		else
 		{
 			defaultValue = "";
 		}
 
 		String isDisabled = "";
 		if ((this.isReadOnly != null && this.isReadOnly))
 		{
 			isDisabled = ",disabled:'" + ProcessorConstants.TRUE + "'";
 		}
 		String htmlComponentName = getHTMLComponentName();
 		String parentContainerId = "";
 		if (this.getParentContainer() != null && this.getParentContainer().getId() != null)
 		{
 			parentContainerId = this.getParentContainer().getId().toString();
 		}
 		String id = "";
 		if (this.getId() != null)
 		{
 			id = this.getId().toString();
 		}
 		/* Bug Id:9030
 		 * textComponent is the name of the text box.
 		 * if default value is not empty loading the data store first, and then setting the value in 
 		 * combo box to default value.
 		 */
 		String textComponent = "combo" + htmlComponentName;
 		String htmlString = "<script>Ext.onReady(function(){ "
 				+ "var myUrl= 'ComboDataAction.do?controlId= "
 				+ id
 				+ "~containerIdentifier="
 				+ parentContainerId
 				+ "';"
 				+ "var ds = new Ext.data.Store({"
 				+ "proxy: new Ext.data.HttpProxy({url: myUrl}),"
 				+ "reader: new Ext.data.JsonReader({root: 'row',totalProperty: 'totalCount',id: 'id'}, "
 				+ "[{name: 'id', mapping: 'id'},{name: 'excerpt', mapping: 'field'}])});"
 				+ "var combo = new Ext.form.ComboBox({store: ds,"
 				+ "hiddenName: '"
 				+ textComponent
 				+ "',displayField:'excerpt',valueField: 'id',"
 				+ "typeAhead: true,width:200,pageSize:15,forceSelection: true,queryParam : 'query',"
 				+ "mode: 'remote',triggerAction: 'all',minChars : 1" + isDisabled + ",emptyText:'"
 				+ defaultValue + "'," + "selectOnFocus:true,applyTo: '" + htmlComponentName
 				+ "'});";
		if (!defaultValue.equals(""))
 		{
 			htmlString = htmlString
 					+ "ds.load({params:{start:0, limit:999,query:''}}); ds.on('load',function(){combo.setValue('"
 					+ defaultValue + "',false);});";
 		}
 
 		htmlString = htmlString
 				+ "});</script>"
 				+ "<div id='auto_complete_dropdown'>"
 				+ "<input type='text' onmouseover=\"showToolTip('"
 				+ htmlComponentName
 				+ "')\" id='"
 				+ htmlComponentName
 				+ "' "
 				+ " name='"
 				+ htmlComponentName
 				+ "' size='20'/>"
 				+ "<div name='comboScript' style='display:none'>"
 				+ "Ext.onReady(function(){ "
 				+ "var myUrl='ComboDataAction.do?controlId= "
 				+ id
 				+ "~containerIdentifier="
 				+ parentContainerId
 				+ "';var ds = new Ext.data.Store({"
 				+ "proxy: new Ext.data.HttpProxy({url: myUrl}),"
 				+ "reader: new Ext.data.JsonReader({root: 'row',totalProperty: 'totalCount',id: 'id'}, "
 				+ "[{name: 'id', mapping: 'id'},{name: 'excerpt', mapping: 'field'}])});"
 				+ "var combo = new Ext.form.ComboBox({store: ds,"
 				+ "hiddenName: '"
 				+ textComponent
 				+ "',displayField:'excerpt',valueField: 'id',"
 				+ "typeAhead: true,width:200,pageSize:15,forceSelection: true,queryParam : 'query',"
 				+ "mode: 'remote',triggerAction: 'all',minChars : 1" + isDisabled + ",emptyText:'"
 				+ defaultValue + "'," + "selectOnFocus:true,applyTo: '" + htmlComponentName
 				+ "'});});" + "</div>" + "<div name=\"comboHtml\" style='display:none'>" + "<div>"
 				+ "<input type='text' onmouseover=\"showToolTip('" + htmlComponentName
 				+ "')\" id='" + htmlComponentName + "' " + " name='" + htmlComponentName
 				+ "' size='20' class='font_bl_nor' />" + "</div>" + "</div>" + "</div>";
 
 		return htmlString;
 	}
 
 	/**
 	 * This method returns the list of values that are displayed as choices.
 	 * @return the list of values that are displayed as choices.
 	 */
 	public List getChoiceList()
 	{
 		return listOfValues;
 	}
 
 	/**
 	 * This method sets the list of values that are displayed as choices.
 	 * @param choiceList the List of values that is to set as ChoiceList.
 	 */
 	public void setChoiceList(List choiceList)
 	{
 		listOfValues = choiceList;
 	}
 
 	protected String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		String htmlString = "&nbsp;";
 
 		String defaultValue = "";
 		if (this.value != null)
 		{
 			if (this.value instanceof String)
 			{
 				defaultValue = (String) this.value;
 			}
 			else if (this.value instanceof List)
 			{
 				List valueList = (List) this.value;
 				if (!valueList.isEmpty())
 				{
 					defaultValue = valueList.get(0).toString();
 				}
 			}
 		}
 
 		List<NameValueBean> nameValueBeanList = null;
 		if (listOfValues == null)
 		{
 			nameValueBeanList = ControlsUtility.populateListOfValues(this);
 		}
 
 		if (nameValueBeanList != null && !nameValueBeanList.isEmpty())
 		{
 			for (NameValueBean nameValueBean : nameValueBeanList)
 			{
 				if (nameValueBean.getValue().equals(defaultValue))
 				{
 					htmlString = "<span class='font_bl_s'> " + nameValueBean.getName() + "</span>";
 					break;
 				}
 			}
 		}
 
 		return htmlString;
 	}
 
 }
