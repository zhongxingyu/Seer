 package edu.wustl.query.htmlprovider;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
 import edu.wustl.common.util.Utility;
 import edu.wustl.query.util.global.Constants;
 
 /**
  * This class generates html for text box, radio button and
  * other components for the Add Limits section of Query Page.
  * @author rukhsana_sameer
  */
 public class GenerateHtml
 {
 	/**
 	 * End tag for html tag td.
 	 */
 	public static final String endTD = "\n</td>";
 
 	/**
 	 * css with white background.
 	 */
 	public static final String CSS_BGWHITE = "rowBGWhiteColor";
 	/**
 	 * css with grey background.
 	 */
 	public static final String CSS_BGGREY = "rowBGGreyColor1";
 	/**
 	 * css for text box.
 	 */
 	public static final String CSS_TEXT = "standardTextQuery";
 	/**
 	 * css for permissible values.
 	 */
 	public static final String CSS_PV= "PermissibleValuesQuery";
 	/**
 	 * css for highlighted background.
 	 */
 	public static final String CSS_HIGHLIGHT= "td_blue_highlight";
 	/**
 	 * This method generates html for RadioButton.
 	 * @param componentId
 	 * 		String
 	 * @param values
 	 * 		List values
 	 * @return String
 	 */
 	public static String generateHTMLForRadioButton(String  componentId, List<String> values)
 	{
 		String cssClass=CSS_TEXT;
 		StringBuffer html = new StringBuffer(Constants.MAX_SIZE);
 		html.append("\n<td class='" + cssClass + "' >");
 		if (values == null)
 		{
 			html.append(getHtmlRadioButton(componentId,cssClass,true,""));
 			html.append(getHtmlRadioButton(componentId,cssClass,false,""));
 		}
 		else
 		{
 			if(values.get(0) == null)
 			{
 				html.append(getHtmlRadioButton(componentId,cssClass,true,""));
 				html.append(getHtmlRadioButton(componentId,cssClass,false,""));
 			}
 			else
 			{
 				getHtmlForValuesNotNull(componentId, values, cssClass, html);
 			}
 		}
 		html.append(endTD);
 		html.append("\n<td class='" + cssClass + "'>&nbsp;");
 		html.append(endTD);
 		html.append("\n<td class='" + cssClass + "'>&nbsp;");
 		html.append(endTD);
 		html.append("\n<td class='" + cssClass + "'>&nbsp;");
 		html.append(endTD);
 
 		return html.toString();
 	}
 	/**
 	 *
 	 * @param componentId id of component
 	 * @param cssClass name of css class
 	 * @param isRadioButtonTrue boolean
 	 * @param checked String
 	 * @return String
 	 */
 	private static String getHtmlRadioButton(String componentId, String cssClass,
 			 boolean isRadioButtonTrue,String checked)
 	{
 		StringBuffer html= new StringBuffer(Constants.MAX_SIZE);
 		String componentName = componentId + "_booleanAttribute";
 		String radioButtonTrueId = componentId + Constants.UNDERSCORE + Constants.TRUE;// "_true";
 		String radioButtonFalseId = componentId + Constants.UNDERSCORE + Constants.FALSE;// "_false"
 		String buttonId = "";
 		String name="";
 		String value="";
 		if(isRadioButtonTrue)
 		{
 			buttonId = radioButtonTrueId;
 			name = "True";
 			value=Constants.TRUE;
 		}
 		else
 		{
 			buttonId = radioButtonFalseId;
 			name="False";
 			value=Constants.FALSE;
 		}
 		html.append("\n<input type='radio' id = '" + componentId
 				+ "_"+value+"' value='"+value+"' onclick=\"resetOptionButton('" + buttonId
 				+ "',this)\" name='" + componentName + checked + "'/><font class='" + cssClass
 				+ "'>"+name+"</font>");
 		return html.toString();
 	}
 
 	/**
 	 * Generates html for radio button depending upon value of radio button.
 	 * @param componentId generated component id
 	 * @param values list of values
 	 * @param cssClass css class name
 	 * @param html generated html
 	 */
 	private static void getHtmlForValuesNotNull(String componentId, List<String> values, String cssClass,
 			StringBuffer html)
 	{
 		if (values.get(0).equalsIgnoreCase(Constants.TRUE))
 		{
 			html.append(getHtmlRadioButton(componentId,cssClass,true,"checked"));
 			html.append(getHtmlRadioButton(componentId,cssClass,false,""));
 		}
 		else if(values.get(0).equalsIgnoreCase(Constants.FALSE))
 		{
 			html.append(getHtmlRadioButton(componentId,cssClass,true,""));
 			html.append(getHtmlRadioButton(componentId,cssClass,false,"checked"));
 		}
 		else
 		{
 			html.append(getHtmlRadioButton(componentId,cssClass,true,""));
 			html.append(getHtmlRadioButton(componentId,cssClass,false,""));
 		}
 	}
 	/**
 	 * Generates html for textBox to hold the input for operator selected.
 	 *
 	 * @param componentId
 	 *            String
 	 * @param attrDetails
 	 *            AttributeDetails
 	 * @param entity 
 	 * @param attribute 
 	 * @return String HTMLForTextBox
 	 */
 	public static String generateHTMLForTextBox(String componentId,AttributeDetails attrDetails, AttributeInterface attribute, EntityInterface entity)
 	{
 		String cssClass = CSS_TEXT;
 		//String componentId = generateComponentName(attributeInterface);
 		String textBoxId = componentId + "_textBox";
 		String textBoxId1 = componentId + "_textBox1";
 		//String dataType = attributeInterface.getDataType();
 		StringBuffer html = new StringBuffer(Constants.MAX_SIZE);
 		String newLine = "\n";
 		html.append("<td width='10%' valign='top' class=\"standardTextQuery\" >\n");
 		getHtmlValueAndOperator(
 				attrDetails.getEditValues(),attrDetails.getSelectedOperator(), textBoxId, html);
 		html.append(endTD);
 		if (attrDetails.getDataType().equalsIgnoreCase(Constants.DATE))
 		{
 			html.append(newLine).append(generateHTMLForCalendar(componentId, true, false,cssClass));
 		}
 		else
 		{
 			html.append("\n<td valign='top' width='1%'>&nbsp;</td>");
 		}
 		html.append("<td width='15%'  valign='top' class=\"standardTextQuery\">\n");
 		if (isBetween(attrDetails))
 		{
 			getHtmlTextBoxForBetweenOperator(attrDetails.getEditValues(), textBoxId1, html);
 		}
 		else
 		{
 			html.append("<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
 					+ "\" style=\"display:none\">");
 		}
 		html.append(endTD);
 		if (attrDetails.getDataType().equalsIgnoreCase(Constants.DATE))
 		{
 			html.append(newLine).append(generateHTMLForCalendar(
 					componentId, false,attrDetails.isBetween(),cssClass));
 		}
 		else
 		{
 			html.append("\n<td valign='top' />");
 			// html.append("\n<td valign='top' />");
 		}
 		return html.toString();
 	}
 	/**
 	 *
 	 * @param attrDetails AttributeDetails
 	 * @return boolean
 	 */
 	private static boolean isBetween(AttributeDetails attrDetails)
 	{
 		return (attrDetails.getSelectedOperator()==null && attrDetails.isBetween())
 				|| checkBetweenOperator(attrDetails.getSelectedOperator());
 	}
 
 	/**
 	 * Generate html for text box based upon operator and values.
 	 * @param values list of values
 	 * @param operator selected operator
 	 * @param textBoxId id of text box
 	 * @param html generated html
 	 */
 	private static void getHtmlValueAndOperator(List<String> values, String operator, String textBoxId,
 			StringBuffer html)
 	{
 		if (values == null || values.isEmpty())
 		{
 			getHtmlValueNull(operator, textBoxId, html);
 		}
 		else
 		{
 			getHtmlValueNotNull(values, operator, textBoxId, html);
 		}
 	}
 	/**
 	 *
 	 * @param operator selected operator
 	 * @param textBoxId id of text box
 	 * @param html generated html
 	 */
 	private static void getHtmlValueNull(String operator, String textBoxId, StringBuffer html)
 	{
 		String temp ="";
 		if(operator == null)
 		{
 			temp ="<input style=\"width:150px; display:block;\" type=\"text\" name=\""
 				+ textBoxId + "\" id=\"" + textBoxId + "\">";
 			html.append(temp);
 		}
 		else
 		{
 			if(operator.equalsIgnoreCase(Constants.IS_NOT_NULL) ||
 					operator.equalsIgnoreCase(Constants.IS_NULL))
 			{
 				temp="<input style=\"width:150px; display:block;\" "
 					+ "type=\"text\" disabled='true' name=\""
 					+ textBoxId + "\" id=\"" + textBoxId + "\">";
 				html.append(temp);
 			}
 
 		}
 	}
 	/**
 	 * @param values list of values
 	 * @param operator selected operator
 	 * @param textBoxId id of textbox component
 	 * @param html generated html
 	 */
 	private static void getHtmlValueNotNull(List<String> values, String operator, String textBoxId,
 			StringBuffer html)
 	{
 		String valueStr = "";
 		if (operator.equalsIgnoreCase(Constants.IN) || operator.equalsIgnoreCase(Constants.Not_In))
 		{
 			valueStr = values.toString();
 			valueStr = valueStr.replace("[", "");
 			valueStr = valueStr.replace("]", "");
 			if(values.get(0) == null)
 			{
 				valueStr = "";
 			}
 			html.append("<input style=\"width:150px; display:block;\" type=\"text\" name=\""
 					+ textBoxId + "\" id=\"" + textBoxId + "\" value=\"" + valueStr + "\">");
 		}
 		else
 		{
 			if(values.get(0) == null)
 			{
 				String temp = "<input style=\"width:150px; display:block;\" type=\"text\" name=\""
 						+ textBoxId + "\" id=\"" + textBoxId + "\" value=\"" + "\">";
 				html.append(temp);
 			}
 			else
 			{
 				html.append("<input style=\"width:150px; display:block;\" type=\"text\" name=\""
 					+ textBoxId + "\" id=\"" + textBoxId + "\" value=\"" + values.get(0)
 					+ "\">");
 			}
 		}
 	}
 
 	/**
 	 * Method provides html for text box when operator is IsBetween.
 	 * @param values list of values
 	 * @param textBoxId1 id of textbox component
 	 * @param html generated html
 	 */
 	private static void getHtmlTextBoxForBetweenOperator(List<String> values, String textBoxId1,
 			StringBuffer html)
 	{
 		if (values == null || values.isEmpty())
 		{
 			html.append("<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
 					+ "\" style=\"display:block\">");
 		}
 		else
 		{
 			if(values.get(1) == null)
 			{
 				String temp = "<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
 				+ "\" value=\"" + "\" style=\"display:block\">";
 				html.append(temp);
 				//getInputTypeHtml(values.get(1), textBoxId1, html,"block");
 			}
 			else
 			{
 			html.append("<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
 					+ "\" value=\"" + values.get(1) + "\" style=\"display:block\">");
 				//getInputTypeHtml(values.get(1), textBoxId1, html,"block");
 			}
 		}
 	}
 	/**
 	 * Generators html for Calendar.Depending upon the value of operator the
 	 * calendar is displayed(hidden/visible).
 	 * @param componentId String
 	 * @param isFirst
 	 *            boolean
 	 * @param isBetween
 	 *            boolean
 	 * @param cssClass
 	 * 	       String
 	 * @return String HTMLForCalendar
 	 */
 	private static String generateHTMLForCalendar(String  componentId, boolean isFirst,
 			boolean isBetween, String cssClass)
 	{
 		StringBuffer innerStr = new StringBuffer("");
 		if (isFirst)
 		{
 			String textBoxId = componentId + "_textBox";
 			String calendarId = componentId + "_calendar";
 			String imgStr = "\n<img id=\"calendarImg\" " +
 					"src=\"images/advancequery/calendar.gif\" width=\"24\" height=\"22\"" +
 					" border=\"0\" onclick='scwShow("+ textBoxId + ",event);'>";
 			innerStr = innerStr.append("\n<td width='3%' class='"+ cssClass
 					    + "' valign='top' align='left' id=\"" + calendarId + "\">"
 						+ "\n" + imgStr);
 		}
 		else
 		{
 			String textBoxId1 = componentId + "_textBox1";
 			String calendarId1 = componentId + "_calendar1";
 			String imgStr = "\n<img id=\"calendarImg\" " +
 					"src=\"images/advancequery/calendar.gif\"" +
 					" width=\"24\" height=\"22\" border='0'" +
 					" onclick='scwShow(" + textBoxId1 + ",event);'>";
 			String style = "";
 			if (isBetween)
 			{
 				style = "display:block";
 			}
 			else
 			{
 				style = "display:none";
 			}
 			innerStr = innerStr.append("\n<td width='3%' class='"+ cssClass
 						+ "' valign='top' id=\"" + calendarId1 + "\" style=\"" + style
 						+ "\">"
 						+ "\n" + imgStr) ;
 		}
 		innerStr = innerStr.append(endTD);
 		return innerStr.toString();
 	}
 
 	/**
 	 * Method provides html for Add Limits Header.
 	 * @param entityName String
 	 * @param entityId String
 	 * @param attributeCollection String
 	 * @param isEditLimits boolean
 	 * @return StringBuffer
 	 */
 	public static StringBuffer getHtmlHeader(String entityName,String entityId,
 			String attributeCollection, boolean isEditLimits)
 	{
 		StringBuffer generatedPreHTML = new StringBuffer(Constants.MAX_SIZE);
 		//String header = Constants.DEFINE_SEARCH_RULES;
 		String html = "<table border=\"0\" width=\"100%\" height=\"30%\" background=\"images/advancequery/bg_content_header.gif\" " +
 					  "cellspacing=\"0\" cellpadding=\"0\" >" +
 					  "\n<tr height=\"2%\" >" +
 					  "<td  valign='top' height=\"2%\" class=\"grey_bold_big\" " +
 					  "colspan=\"8\" ><img src=\"images/advancequery/t_define_limits.gif\"  align=\"absmiddle\" />";
 		generatedPreHTML.append(html); 
 		generatedPreHTML.append(" '" + entityName + "'");
 		generatedPreHTML.append(endTD);
 		generatedPreHTML.append("####");
 		generatedPreHTML.append(generateHTMLForButton(entityId,attributeCollection, isEditLimits));
 		generatedPreHTML.append("\n</tr></table>");
 		return generatedPreHTML;
 	}
 	/** 
 	 * Generates html for button.
 	 * @param entityName
 	 *            entityName
 	 * @param attributesStr
 	 *            attributesStr
 	 * @param isEditLimits boolean
 	 * @return String HTMLForButton
 	 */
 	private static String generateHTMLForButton(String entityName, String attributesStr,
 			boolean isEditLimits)
 	{
 		//String buttonName = "addLimit";
 		String buttonId = "";
 		String imgsrc="images/advancequery/b_add_limit.gif";
 		StringBuffer html = new StringBuffer(Constants.MAX_SIZE);
 		
 		String temp = "\n<td  colspan=\"2\" " +
 						"height=\"30\" valign=\"top\" align=\"right\" >";
 		buttonId = "TopAddLimitButton";
 		html.append(temp);
 		String buttonCaption = "Add Limit";
 		if (isEditLimits)
 		{
 			buttonCaption = "Edit Limit";
 			imgsrc="images/advancequery/b_edit_limit.gif";
 		}
 		html.append("\n<img src=\"" + imgsrc + "\"  id=\"" + buttonId
 				+ "\" onClick=\"produceQuery('" + buttonCaption
 				+ "', 'addToLimitSet.do', 'categorySearchForm', '" + entityName + "','"
 				+ attributesStr + "')\" value=\"" + buttonCaption + "\"/>");
 		html.append(endTD);
 		return html.toString();
 	}
 	/**
 	 * @param attributeCollection String
 	 * @param nameOfTheEntity String
 	 * @param isEditLimits boolean
 	 * @return StringBuffer
 	 */
 	public static StringBuffer generatePreHtml(String attributeCollection,
 			String nameOfTheEntity, boolean isEditLimits)
 	{
 		String header = Constants.DEFINE_SEARCH_RULES;
 		String entityName = Utility.parseClassName(nameOfTheEntity);
 		StringBuffer generatedPreHTML = new StringBuffer(Constants.MAX_SIZE);
 		String html = "<table border=\"0\" width=\"100%\" height=\"30%\" " +
 					  "cellspacing=\"0\" cellpadding=\"0\">" +
 					  "\n<tr height=\"2%\"> " +
 					  "<td valign='top' height=\"2%\" colspan=\"8\" " +
 					  "bgcolor=\"#EAEAEA\" ><font face=\"Arial\" size=\"2\" " +
 					  "color=\"#000000\"><b>";
 		generatedPreHTML.append(html);
 		generatedPreHTML.append(header + " '" + entityName + "'</b></font>");
 		generatedPreHTML.append(endTD);
 		generatedPreHTML.append(generateHTMLForButton(nameOfTheEntity,attributeCollection,isEditLimits));
 		generatedPreHTML.append("\n</tr></table>");
 		return generatedPreHTML;
 	}
 	/**
 	 * This method generates the combobox's html to show the operators valid for
 	 * the attribute passed to it.
 	 * @param componentId
 	 *            String
 	 * @param isDate
 	 *            boolean
 	 * @param attributeDetails
 	 *            AttributeDetails
 	 * @return String HTMLForOperators
 	 */
 	public static String generateHTMLForOperators(String componentId,boolean isDate,
 			AttributeDetails attributeDetails)
 	{
 		String cssClass=CSS_PV;
 		StringBuffer html = new StringBuffer();
 		List<String> operatorsList = attributeDetails.getOperatorsList();
 		if (operatorsList != null && !operatorsList.isEmpty())
 		{
 			html.append("\n<td width='15%' class=" + cssClass + " valign='top' >");
 			if (isDate)
 			{
 				html
 						.append("\n<select   class=" + cssClass
 								+ " style=\"width:150px; display:block;\" name=\""
 								+ componentId + "_combobox\" "
 								+ "onChange=\"operatorChanged('"
 								+ componentId + "','true')\">");
 			}
 			else
 			{
 				html.append("\n<select  class=" + cssClass
 						+ " style=\"width:150px; display:block;\" name=\"" + componentId
 						+ "_combobox\" onChange=\"operatorChanged('" + componentId
 						+ "','false')\">");
 			}
 			getHtmlForSelectedOperator(attributeDetails, cssClass, html, operatorsList);
 			html.append("\n</select>");
 			html.append(endTD);
 		}
 		return html.toString();
 	}
 
 	/**
 	 * Method generates html for selected operator.
 	 * @param attributeDetails AttributeDetails
 	 * @param cssClass String
 	 * @param html StringBuffer
 	 * @param operatorsList List
 	 */
 	private static void getHtmlForSelectedOperator(AttributeDetails attributeDetails,
 			String cssClass, StringBuffer html, List<String> operatorsList)
 	{
 		Iterator<String> iter = operatorsList.iterator();
 
 		while (iter.hasNext())
 		{
 			String operator = iter.next().toString();
 			if (operator.equalsIgnoreCase(attributeDetails.getSelectedOperator()))
 			{
 				html.append("\n<option  class=" + cssClass + " value=\"" + operator
 						+ "\" SELECTED>" + operator + "</option>");
 			}
 			else
 			{
 				html.append("\n<option  class=" + cssClass + " value=\"" + operator + "\">"
 						+ operator + "</option>");
 			}
 		}
 	}
 	/**
 	 * @param generatedHTML StringBuffer
 	 */
 	public static void getTags(StringBuffer generatedHTML)
 	{
 		generatedHTML.append("\n<tr>\n<td valign=\"top\">");
 		generatedHTML.append(endTD);
 		generatedHTML.append("\n</tr>");
 	}
 
 	/**
 	 * Create a map which holds the list of all Expression(DAGNode) ids for a particular entity.
 	 * @param expressionMap Map
 	 * @return map consisting of the entity and their corresponding expression ids
 	 */
 	public static Map<EntityInterface, List<Integer>> getEntityExpressionIdListMap(
 			Map<Integer, Map<EntityInterface, List<ICondition>>> expressionMap)
 	{
 			Map<EntityInterface, List<Integer>> entityExpressionIdMap =
 				new HashMap<EntityInterface,List<Integer>>();
 			Iterator<Integer> outerMapIterator = expressionMap.keySet().iterator();
 			List<Integer> dagIdList = new ArrayList<Integer>();
 			while (outerMapIterator.hasNext())
 			{
 				Integer expressionId = (Integer) outerMapIterator.next();
 				Map<EntityInterface, List<ICondition>> entityMap = expressionMap.get(expressionId);
 				if (!entityMap.isEmpty())
 				{
 					Iterator<EntityInterface> innerMapIterator = entityMap.keySet().iterator();
 					while (innerMapIterator.hasNext())
 					{
 						EntityInterface entity = (EntityInterface)innerMapIterator.next();
 						if (!entityExpressionIdMap.containsKey(entity))
 						{
 							//if the entity is not present in the map
 							//create new list and add it to map
 							dagIdList.clear();
 							dagIdList.add(expressionId);
 							entityExpressionIdMap.put(entity, dagIdList);
 							continue;
 						}
 						//if the entity is present in the map
 						//add the dag id to the existing list
 						dagIdList = (List<Integer>)entityExpressionIdMap.get(entity);
 						dagIdList.add(expressionId);
 						entityExpressionIdMap.put(entity, dagIdList);
 					}
 				}
 			}
 		return entityExpressionIdMap;
 	}
 	/**
 	 *
 	 * @param componentId String
 	 * @param isSelected boolean
 	 * @return String
 	 */
 	public static String generateCheckBox(String componentId, boolean isSelected)
 	{
 		String select = "";
 		if(isSelected)
 		{
 			select="select";
 		}
 		String tag = "<td class=\"standardTextQuery\"  width=\"5\" valign=\"top\">"
 				+ "<input type=\"checkbox\"   id='"
 				+ componentId
 				+ "_checkbox'"
 				+ select
 				+ "  onClick=\"enableDisplayField(this.form,'" + componentId + "')\"></td>";
 		return tag;
 	}
 	/**
 	 *
 	 * @param componentId String
 	 * @param oper String
 	 * @param operatorList List
 	 * @param isSecondTime boolean
 	 * @return String
 	 */
 	public static String generateHTMLForOperator(String componentId,String oper,
 			List<String>operatorList,boolean isSecondTime)
 	{
 		StringBuffer generateHTML = new StringBuffer();
 		String comboboxId = "_combobox";
 		if(isSecondTime)
 		{
 			comboboxId = "_combobox1";
 		}
 		String comboboxName = componentId+comboboxId;
 		if (operatorList != null && !operatorList.isEmpty())
 		{
 			String html = "\n<td width='15%'  valign='top' >"
 						+"\n<select "
 						+ " style=\"width:150px; display:block;\" name=\"" + comboboxName
 						+ "\" id = '"+comboboxName+"'onChange=\"operatorChanged('"
 						+ componentId + "','true')\">";
 			generateHTML.append(html);
 			Iterator<String> iter = operatorList.iterator();
 			String operator;
 			while (iter.hasNext())
 			{
 				operator = iter.next().toString();
 				getHtmlSelectAttribute(oper, generateHTML, operator);
 			}
 			generateHTML.append("\n</select>");
 		}
 		return generateHTML.toString();
 	}
 	/**
 	 * @param oper
 	 * 		String
 	 * @param generateHTML
 	 * 		StringBuffer
 	 * @param operator String
 	 */
 	private static void getHtmlSelectAttribute(String oper, StringBuffer generateHTML,
 			String operator)
 	{
 		if (operator.equalsIgnoreCase(oper))
 		{
 			generateHTML.append("\n<option   value=\"" + operator
 					+ "\" SELECTED>" + operator + "</option>");
 		}
 		else
 		{
 			generateHTML.append("\n<option   value=\"" + operator + "\">"
 					+ operator + "</option>");
 		}
 	}
 	/**
 	 * @param forPage
 	 * 		String
 	 * @param generatedHTML
 	 * 	StringBuffer
 	 */
 	public static void getHtmlAddEditPage(String forPage, StringBuffer generatedHTML)
 	{
 		if (forPage.equalsIgnoreCase(Constants.ADD_EDIT_PAGE))
 		{
 			generatedHTML
 					.append("<table border=\"0\" width=\"100%\" " +
 							"height=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
 			GenerateHtml.getTags(generatedHTML);
 		}
 	}
 	/**
 	 * Method generates html for bold attribute label.
 	 * @param attrLabel
 	 * 		String
 	 * @return StringBuffer
 	 */
 	public static StringBuffer getBoldLabel(String attrLabel)
 	{
 		String imgStr = "\n<img id=\"arrowImg\" " +
 		"src=\"images/advancequery/ic_black_arrow.gif\" />";
 		StringBuffer label = new StringBuffer(imgStr);
 		label.append("  <b>");
 		label.append(attrLabel).append("</b>");
 		return label;
 	}
 
 	/**
 	 * This method provides the alternate css for alternate attributes of an entity.
 	 * @param generatedHTML
 	 * 			StringBuffer
 	 * @param isBGColor
 	 * 		boolean
 	 * @param componentId
 	 * 		String
 	 * @return boolean
 	 */
 	public static boolean getAlternateCss(StringBuffer generatedHTML, boolean isBGColor,
 			String componentId,boolean isBold)
 	{
 		String styleSheetClass="";
 		boolean bgColor = isBGColor;
 		if(isBold)
 		{
 			styleSheetClass = CSS_HIGHLIGHT;
 		}
 		else if (bgColor)
 		{
 			styleSheetClass = CSS_BGGREY;
 		}
 		else
 		{
 			styleSheetClass = CSS_BGWHITE;
 		}
 		bgColor ^= true;  //bgColor = !bgColor;
 		String html = "\n<tr class='"
 			+ styleSheetClass
 			+ "' id=\""
 			+ componentId
 			+ "\" height=\"6%\" >\n"
 			+ "<td valign='top' align='right' "
 			+ "class='standardLabelQuery' nowrap='nowrap' width=\"15%\">";
 		generatedHTML.append(html);
 		
 		return bgColor;
 	}
 	/**
 	 * Checks if operator is between operator.
 	 * @param operator
 	 * 			String
 	 * @return boolean
 	 */
 	public static boolean checkBetweenOperator(String operator)
 	{
 		boolean isBetween = false;
 		if (operator!=null && operator.equalsIgnoreCase(
 						RelationalOperator.Between.toString()))
 		{
 			isBetween = true;
 		}
 		return isBetween;
 	}
 	/**
 	 * Generates html for date format.
 	 * @param generatedHTML
 	 * 			StringBuffer
 	 * @param isBold
 	 * 			boolean
 	 * @param attribute
 	 * 		AttributeInterface
 	 */
 	public static void getDateFormat(StringBuffer generatedHTML, boolean isBold,
 			AttributeInterface attribute)
 	{
 		if (attribute.getDataType().equalsIgnoreCase(Constants.DATE))
 		{
 			StringBuffer dateFormat = new StringBuffer(Constants.DATE_FORMAT);
 			StringBuffer format = dateFormat;
 			if(isBold)
 			{
 				format = new StringBuffer();
 				format.append("<b>");
 				format.append(dateFormat).append("</b>");
 			}
 			generatedHTML.append("\n(" + format + ")");
 		}
 	}
 	public static String getHtmlForOperators(String componentId,AttributeDetails attributeDetails,String compIdofID)
 	{
 		String cssClass=CSS_PV;
 		String temp="";
 		StringBuffer html = new StringBuffer();
 		List<String> operatorsList = attributeDetails.getOperatorsList();
 		if (operatorsList != null && !operatorsList.isEmpty())
 		{
 			temp="\n<td width='20%' class=" + cssClass + " valign='top' >";
 			html.append(temp);
 			html.append("\n<select  class=" + cssClass
 						+ " style=\"width:150px; display:block;\" name=\"" + componentId
						+ "_combobox\" id=\""+ componentId+ "_combobox\" onChange=\"changeIdOperator('" + componentId + "','"+compIdofID+"')\">");
 			getHtmlForSelectedOperator(attributeDetails, cssClass, html, operatorsList);
 			html.append("\n</select>");
 			html.append(endTD);
 		}
 		return html.toString();
 	}
 }
