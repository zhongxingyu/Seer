 
 package edu.common.dynamicextensions.ui.util;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.validation.ValidatorRuleInterface;
 import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author preeti_munot
  * @author deepti_shelar
  * @version 1.0
  */
 public final class ControlConfigurationsFactory
 {
 
 	private static ControlConfigurationsFactory m_instance = null;
 	private Map controlsConfigurationMap = null;
 	private Map<String, RuleConfigurationObject> rulesConfigurationMap = null;
 
 	/**
 	 * private constructor for ControlConfigurationsFactory.
 	 * This will initialize maps for controls and their appropriate rules.
 	 * The call to parseXML will parse two configuration files in order to 
 	 * fill the data in their objects.
 	 * @throws DynamicExtensionsSystemException  dynamicExtensionsSystemException
 	 */
 	private ControlConfigurationsFactory() throws DynamicExtensionsSystemException
 	{
 		controlsConfigurationMap = new HashMap();
 		rulesConfigurationMap = new HashMap<String, RuleConfigurationObject>();
 
 		parseXML("RuleConfigurations.xml");
 		parseXML("ControlConfigurations.xml");
 	}
 
 	/**
 	 * 
 	 * @return ControlConfigurationsFactory instance of ControlConfigurationsFactory
 	 * @throws DynamicExtensionsSystemException dynamicExtensionsSystemException
 	 */
 	public static synchronized ControlConfigurationsFactory getInstance()
 			throws DynamicExtensionsSystemException
 	{
 		if (m_instance == null)
 		{
 			m_instance = new ControlConfigurationsFactory();
 		}
 		return m_instance;
 	}
 
 	/**
 	 * Parse the XML
 	 * @param configurationFileName name of the file to be parsed.
 	 * @throws DynamicExtensionsSystemException dynamicExtensionsSystemException
 	 */
 	private void parseXML(String configurationFileName) throws DynamicExtensionsSystemException
 	{
 		Document document = null;
 		try
 		{
 			final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = null;
 			if (FACTORY != null)
 			{
 				docBuilder = FACTORY.newDocumentBuilder();
 				if (docBuilder != null)
 				{
 					InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(
 							configurationFileName);
 					if (inputStream != null)
 					{
 						document = docBuilder.parse(inputStream);
 
 						if (document != null)
 						{
 							if (configurationFileName.equalsIgnoreCase("RuleConfigurations.xml"))
 							{
 								loadRuleConfigurations(document);
 							}
 							else if (configurationFileName
 									.equalsIgnoreCase("ControlConfigurations.xml"))
 							{
 								loadControlConfigurations(document);
 							}
 						}
 					}
 					else
 					{
						Logger.out.info("InputStream null...Please check");
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 
 	}
 
 	/**
 	 * This method wil parse the ruleConfigurations xml file and wil populate the ruleConfigurationObject.
 	 * @param document to be pared
 	 * @throws DynamicExtensionsSystemException dynamicExtensionsSystemException
 	 */
 	private void loadRuleConfigurations(Document document) throws DynamicExtensionsSystemException
 	{
 		if (document != null)
 		{
 			try
 			{
 				NodeList validationRulesList = document
 						.getElementsByTagName(Constants.VALIDATION_RULE);
 				if (validationRulesList != null)
 				{
 					int noOfRules = validationRulesList.getLength();
 					Node validationRuleNode = null, ruleDisplayLabelNode = null, ruleNameNode = null, ruleClassNode = null;
 					Node errorKey = null, errorKeyNameNode = null;
 					NamedNodeMap ruleAttributes = null, errorKeysList = null;
 					NodeList ruleParameters = null, childNodes = null;
 					String ruleName = null, errorKeyValue = null;
 					for (int i = 0; i < noOfRules; i++)
 					{
 						validationRuleNode = validationRulesList.item(i);
 						if (validationRuleNode != null)
 						{
 							RuleConfigurationObject ruleConfigurationObject = new RuleConfigurationObject();
 							ruleAttributes = validationRuleNode.getAttributes();
 							if (ruleAttributes != null)
 							{
 								ruleDisplayLabelNode = ruleAttributes
 										.getNamedItem(Constants.DISPLAY_LABEL);
 								ruleNameNode = ruleAttributes.getNamedItem(Constants.RULE_NAME);
 								ruleClassNode = ruleAttributes.getNamedItem(Constants.RULE_CLASS);
 								if (ruleDisplayLabelNode != null && ruleNameNode != null
 										&& ruleClassNode != null)
 								{
 									ruleName = ruleNameNode.getNodeValue();
 									ruleConfigurationObject.setDisplayLabel(ruleDisplayLabelNode
 											.getNodeValue());
 									ruleConfigurationObject.setRuleName(ruleName);
 									ruleConfigurationObject.setRuleClassName(ruleClassNode
 											.getNodeValue());
 								}
 							}
 							ruleParameters = ((Element) validationRuleNode)
 									.getElementsByTagName(Constants.PARAM);
 							if (ruleParameters.getLength() != 0)
 							{
 								ruleConfigurationObject
 										.setRuleParametersList(loadRuleParameters(ruleParameters));
 							}
 							childNodes = ((Element) validationRuleNode)
 									.getElementsByTagName(Constants.ERROR_KEY);
 							errorKey = childNodes.item(0);
 							errorKeysList = errorKey.getAttributes();
 							if (errorKeysList != null)
 							{
 								errorKeyNameNode = errorKeysList.getNamedItem(Constants.NAME);
 								errorKeyValue = errorKeyNameNode.getNodeValue();
 								ruleConfigurationObject.setErrorKey(errorKeyValue);
 							}
 							rulesConfigurationMap.put(ruleName, ruleConfigurationObject);
 
 						}
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 
 			}
 		}
 	}
 
 	/**
 	 * @param ruleParameters list Of Nodes
 	 * @return List list of ruleParameters.
 	 */
 	private List loadRuleParameters(NodeList ruleParameters)
 	{
 		ArrayList<NameValueBean> ruleParametersAttributes = new ArrayList<NameValueBean>();
 		if (ruleParameters != null)
 		{
 			NameValueBean nameValueBeanAttribute = null;
 			NamedNodeMap paramAttributes = null;
 			Node paramLabelNode = null, paramNameNode = null, paramNode = null;
 			String paramLabel = null, paramName = null;
 			int noOfParameters = ruleParameters.getLength();
 			for (int i = 0; i < noOfParameters; i++)
 			{
 				paramNode = ruleParameters.item(i);
 				paramAttributes = paramNode.getAttributes();
 				if (paramAttributes != null)
 				{
 					paramLabelNode = paramAttributes.getNamedItem(Constants.PARAM_LABEL);
 					paramNameNode = paramAttributes.getNamedItem(Constants.PARAM_NAME);
 					if (paramLabelNode != null && paramNameNode != null)
 					{
 						paramLabel = paramLabelNode.getNodeValue();
 						paramName = paramNameNode.getNodeValue();
 					}
 				}
 				if (paramName != null && paramLabel != null)
 				{
 					nameValueBeanAttribute = new NameValueBean(paramName, paramLabel);
 					ruleParametersAttributes.add(nameValueBeanAttribute);
 				}
 			}
 		}
 		return (ArrayList) ruleParametersAttributes;
 	}
 
 	/**
 	 * This method wil parse the ControlConfigurations xml file and will populate the controlsConfigurationObject
 	 * @param document to be parsed.
 	 */
 	private void loadControlConfigurations(Document document)
 	{
 		if (document != null)
 		{
 			NodeList controlsList = document.getElementsByTagName(Constants.CONTROL_TAGNAME);
 			if (controlsList != null)
 			{
 				ControlsConfigurationObject controlsConfigurationObject = null;
 				List<NameValueBean> listOfControls = new ArrayList<NameValueBean>();
 				Node controlNode = null, controlNameNode = null, displayLabelNode = null, jspNameNode = null, dataTypeNode = null, imageFilePathNode = null;
 				NodeList controlDataTypesNodesList = null, controlCommonValidationRules = null;
 				NamedNodeMap controlAttributes = null;
 				String controlName = null, displayLabel = null;
 				NameValueBean nameValueBean = null;
 				Map dataTypeRulesMap = null, dataTypeImplicitRulesMap = null, dataTypeExplicitRulesMap = null;
 
 				int noOfControls = controlsList.getLength();
 				for (int i = 0; i < noOfControls; i++)
 				{
 					controlNode = controlsList.item(i);
 					if (controlNode != null)
 					{
 						List dataTypeValidationRulesList = new ArrayList(), dataTypeRuleObjectsList = new ArrayList();
 						List implicitRulesList = null, implicitExplicitRulesList = null;
 						NodeList dataTypeRulesList = null;
 
 						controlsConfigurationObject = new ControlsConfigurationObject();
 						controlAttributes = controlNode.getAttributes();
 						if (controlAttributes != null)
 						{
 							controlNameNode = controlAttributes.getNamedItem(Constants.NAME);
 							displayLabelNode = controlAttributes
 									.getNamedItem(Constants.DISPLAY_LABEL);
 							jspNameNode = controlAttributes.getNamedItem(Constants.JSP_NAME);
 							imageFilePathNode = controlAttributes
 									.getNamedItem(Constants.IMAGE_PATH);
 							if (controlNameNode != null && displayLabelNode != null)
 							{
 								controlName = controlNameNode.getNodeValue();
 								displayLabel = displayLabelNode.getNodeValue();
 								nameValueBean = new NameValueBean(controlName, displayLabel);
 
 								listOfControls.add(nameValueBean);
 								controlsConfigurationObject.setControlName(controlName);
 								controlsConfigurationObject.setDisplayLabel(displayLabel);
 							}
 							if (jspNameNode != null)
 							{
 								controlsConfigurationObject.setJspName(jspNameNode.getNodeValue());
 							}
 							if (imageFilePathNode != null)
 							{
 								controlsConfigurationObject.setImageFilePath(imageFilePathNode
 										.getNodeValue());
 							}
 						}
 
 						List commonImplicitRules = new ArrayList();
 						List commonExplicitRules = new ArrayList();
 						controlCommonValidationRules = ((Element) controlNode)
 								.getElementsByTagName(Constants.COMMON_VALIDATION_RULE);
 						List commonValidationsList = getChildNodesList(
 								controlCommonValidationRules, Constants.NAME);
 						implicitRulesList = getChildNodesList(controlCommonValidationRules,
 								Constants.IS_IMPLICIT);
 						for (int j = 0; j < implicitRulesList.size(); j++)
 						{
 							if (implicitRulesList.get(j).toString().equalsIgnoreCase("false"))
 							{
 								commonExplicitRules.add(commonValidationsList.get(j));
 							}
 							else if (implicitRulesList.get(j).toString().equalsIgnoreCase("true"))
 							{
 								commonImplicitRules.add(commonValidationsList.get(j));
 							}
 						}
 
 						controlsConfigurationObject
 								.setCommonValidationRules(getRuleObjectsList(commonValidationsList));
 						controlsConfigurationObject.setCommonExplicitRules(commonExplicitRules);
 						controlsConfigurationObject.setCommonImplicitRules(commonImplicitRules);
 
 						controlDataTypesNodesList = ((Element) controlNode)
 								.getElementsByTagName(Constants.DATA_TYPE_TAGNAME);
 						List dataTypesList = getChildNodesList(controlDataTypesNodesList,
 								Constants.NAME);
 
 						dataTypeRulesMap = new HashMap();
 						dataTypeImplicitRulesMap = new HashMap();
 						dataTypeExplicitRulesMap = new HashMap();
 
 						for (int j = 0; j < dataTypesList.size(); j++)
 						{
 							dataTypeNode = controlDataTypesNodesList.item(j);
 							dataTypeRulesList = ((Element) dataTypeNode)
 									.getElementsByTagName(Constants.VALIDATION_RULE);
 							dataTypeValidationRulesList = getChildNodesList(dataTypeRulesList,
 									Constants.NAME);
 
 							implicitRulesList = getChildNodesList(dataTypeRulesList,
 									Constants.IS_IMPLICIT);
 							implicitExplicitRulesList = getImplicitExplicitRules(
 									dataTypeValidationRulesList, implicitRulesList);
 
 							dataTypeImplicitRulesMap.put(dataTypesList.get(j),
 									implicitExplicitRulesList.get(0));
 							dataTypeExplicitRulesMap.put(dataTypesList.get(j),
 									implicitExplicitRulesList.get(1));
 
 							dataTypeRuleObjectsList = getRuleObjectsList(dataTypeValidationRulesList);
 							dataTypeRulesMap.put(dataTypesList.get(j), dataTypeRuleObjectsList);
 						}
 						controlsConfigurationObject.setDataTypeValidationRules(dataTypeRulesMap);
 						controlsConfigurationObject
 								.setDataTypesList(getNameValueBeansList(dataTypesList));
 						controlsConfigurationObject
 								.setDataTypeExplicitRules(dataTypeExplicitRulesMap);
 						controlsConfigurationObject
 								.setDataTypeImplicitRules(dataTypeImplicitRulesMap);
 					}
 					controlsConfigurationMap.put(controlName, controlsConfigurationObject);
 					controlsConfigurationMap.put("ListOfControls", listOfControls);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param controlName
 	 * @return
 	 */
 	public List<String> getAllImplicitRules(String controlName, String dataType)
 	{
 		List<String> allImplicitRules = new ArrayList<String>();
 
 		ControlsConfigurationObject controlsConfiguration = (ControlsConfigurationObject) controlsConfigurationMap
 				.get(controlName);
 		if (controlsConfiguration != null)
 		{
 			Map dataTypeImplicitRulesMap = controlsConfiguration.getDataTypeImplicitRules();
 			List<String> dataTypeImplicitRuleList = (List) dataTypeImplicitRulesMap.get(dataType);
 			if (dataTypeImplicitRuleList != null && !dataTypeImplicitRuleList.isEmpty())
 			{
 				allImplicitRules.addAll(dataTypeImplicitRuleList);
 			}
 
 			List<String> commonImplicitRuleList = controlsConfiguration.getCommonImplicitRules();
 			if (commonImplicitRuleList != null || !commonImplicitRuleList.isEmpty())
 			{
 				allImplicitRules.addAll(commonImplicitRuleList);
 			}
 		}
 		return allImplicitRules;
 	}
 
 	/**
 	 * Gets all ImplicitExplicitRules 
 	 * @param rulesList list of rules
 	 * @param implicitRulesList list of the rules which will not be shown on ui
 	 * @return List of addition of the rules which will be shown on ui and which will not be shown on ui.
 	 */
 	private List getImplicitExplicitRules(List rulesList, List implicitRulesList)
 	{
 		List listOfImplicitRules = new ArrayList();
 		List listOfExplicitRules = new ArrayList();
 		List listOfImplicitExplicitRules = new ArrayList();
 		for (int i = 0; i < implicitRulesList.size(); i++)
 		{
 			if (implicitRulesList.get(i).toString().equalsIgnoreCase("false"))
 			{
 				listOfExplicitRules.add(rulesList.get(i));
 			}
 			else if (implicitRulesList.get(i).toString().equalsIgnoreCase("true"))
 			{
 				listOfImplicitRules.add(rulesList.get(i));
 			}
 		}
 		listOfImplicitExplicitRules.add(listOfImplicitRules);
 		listOfImplicitExplicitRules.add(listOfExplicitRules);
 
 		return listOfImplicitExplicitRules;
 	}
 
 	/**
 	 * 
 	 * @param ruleValidationsList list of ruleNames
 	 * @return List of ruleObjects.
 	 */
 	private List getRuleObjectsList(List ruleValidationsList)
 	{
 		List<RuleConfigurationObject> ruleObjectsList = new ArrayList<RuleConfigurationObject>();
 		Iterator rulesIterator = ruleValidationsList.iterator();
 		RuleConfigurationObject ruleObject = null;
 		while (rulesIterator.hasNext())
 		{
 			String ruleName = (String) rulesIterator.next();
 			ruleObject = (RuleConfigurationObject) rulesConfigurationMap.get(ruleName);
 			ruleObjectsList.add(ruleObject);
 		}
 		return ruleObjectsList;
 	}
 
 	/**
 	 * This method gets all the ValidationRules associated with the dataType selected by user
 	 * @param dataTypeName name of datatype selected by user
 	 * @param controlNode node
 	 * @return List ValidationRules associated with the dataType selected by user
 	 */
 	/*
 	 private List getDataTypeValidationRules(String dataTypeName, Node controlNode)
 	 {
 	 List<String> dataTypeValidations = new ArrayList<String>();
 	 NodeList rules;
 	 Node ruleNode = null, ruleAttributeNode = null;
 	 String attrName = "", attrValue = "";
 	 NamedNodeMap ruleAttributes = null;
 	 if (controlNode != null && dataTypeName != null)
 	 {
 	 rules = ((Element) controlNode).getElementsByTagName(Constants.VALIDATION_RULE);
 	 for (int i = 0; i < rules.getLength(); i++)
 	 {
 	 ruleNode = rules.item(i);
 	 ruleAttributes = ruleNode.getAttributes();
 	 if (ruleAttributes != null)
 	 {
 	 for (int j = 0; j < ruleAttributes.getLength(); j++)
 	 {
 	 ruleAttributeNode = ruleAttributes.item(j);
 	 attrName = ruleAttributeNode.getNodeName();
 	 attrValue = ruleAttributeNode.getNodeValue();
 	 if (attrName != null && attrValue != null && attrName.equalsIgnoreCase(Constants.NAME))
 	 {
 	 dataTypeValidations.add(attrValue);
 	 }
 
 	 }
 	 }
 	 }
 	 }
 	 return dataTypeValidations;
 	 }*/
 
 	/**
 	 * Method common for various method to get the list of childnodes.
 	 * @param mappedNodesList nodesList
 	 * @param key key for parentNode
 	 * @return ArrayList list of childNodes
 	 */
 	private ArrayList getChildNodesList(NodeList mappedNodesList, String key)
 	{
 		ArrayList<String> childNodesList = new ArrayList<String>();
 		if (mappedNodesList != null)
 		{
 			NamedNodeMap childNodes = null;
 			Node childNameNode = null, childNode = null;
 			String childNodeName = null;
 			int noOfChildNodes = mappedNodesList.getLength();
 			for (int i = 0; i < noOfChildNodes; i++)
 			{
 				childNode = mappedNodesList.item(i);
 				childNodes = childNode.getAttributes();
 				if (childNodes != null)
 				{
 					childNameNode = childNodes.getNamedItem(key);
 					if (childNameNode != null)
 					{
 						childNodeName = childNameNode.getNodeValue();
 						childNodesList.add(childNodeName);
 					}
 				}
 			}
 		}
 		return childNodesList;
 	}
 
 	/**
 	 * This method converts the the passed list into namevalue beans objects list in order to help in rendering Ui.
 	 * @param dataTypesList list Of all dataTypes
 	 * @return List NameValueBeansList
 	 */
 	private List getNameValueBeansList(List dataTypesList)
 	{
 		List<NameValueBean> nameValueBeansList = new ArrayList<NameValueBean>();
 		Iterator iter = dataTypesList.iterator();
 		NameValueBean nameValueBeanAttribute;
 		String nodeName = "";
 		while (iter.hasNext())
 		{
 			nodeName = (String) iter.next();
 			nameValueBeanAttribute = new NameValueBean(nodeName, nodeName);
 			nameValueBeansList.add(nameValueBeanAttribute);
 		}
 
 		return nameValueBeansList;
 	}
 
 	/**
 	 * 
 	 * @param controlName name of the control selected by user
 	 * @return List dataTypes associated with the selected control
 	 */
 	public List getControlsDataTypes(String controlName)
 	{
 		ArrayList dataTypesList = null;
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 			if (controlsConfigurationObject != null)
 			{
 				dataTypesList = (ArrayList) controlsConfigurationObject.getDataTypesList();
 			}
 		}
 		return dataTypesList;
 	}
 
 	/**
 	 * Returns the jspname for the selected control by user.
 	 * @param controlName name of the control selected by user
 	 * @return String jspname associated with the control
 	 */
 	public String getControlJspName(String controlName)
 	{
 		String jspName = null;
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 			if (controlsConfigurationObject != null)
 			{
 				jspName = controlsConfigurationObject.getJspName();
 			}
 		}
 		return jspName;
 	}
 
 	/**
 	 * Returns the jspname for the selected control by user.
 	 * @param controlName name of the control selected by user
 	 * @return String jspname associated with the control
 	 */
 	public String getControlImagePath(String controlName)
 	{
 		String imagepath = null;
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 			if (controlsConfigurationObject != null)
 			{
 				imagepath = controlsConfigurationObject.getImageFilePath();
 			}
 		}
 		return imagepath;
 	}
 
 	/**
 	 * 
 	 * @param controlName name of the control selected by user
 	 * @return ControlDisplayLabel DisplayLabel for the control selected by user
 	 */
 	public String getControlDisplayLabel(String controlName)
 	{
 		String displayLabel = null;
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 			if (controlsConfigurationObject != null)
 			{
 				displayLabel = controlsConfigurationObject.getDisplayLabel();
 			}
 		}
 		return displayLabel;
 	}
 
 	/**
 	 * Returns ImplicitRules : The rules which are not going to be shown on ui.
 	 * @param controlName name of the control selected by user
 	 * @param dataType dataType selected by user.
 	 * @return ArrayList list of all the rules related to control and its dataType selected.
 	 */
 	public ArrayList getImplicitRules(String controlName, String dataType)
 	{
 		ArrayList<String> implicitRules = new ArrayList<String>();
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 			if (controlsConfigurationObject != null)
 			{
 				implicitRules = (ArrayList<String>) getListOfRules(controlsConfigurationObject
 						.getCommonImplicitRules());
 				Map map = controlsConfigurationObject.getDataTypeImplicitRules();
 				List rulesList = (ArrayList) map.get(dataType);
 				Iterator iter = rulesList.iterator();
 				while (iter.hasNext())
 				{
 					String ruleName = iter.next().toString();
 					if (!ruleName.equalsIgnoreCase(""))
 					{
 						implicitRules.add(ruleName);
 					}
 				}
 			}
 		}
 		return implicitRules;
 	}
 
 	/**
 	 * Returns ExplicitRules : The rules which are will be shown on ui.
 	 * @param controlName selected by user
 	 * @param dataType selected by user
 	 * @return ArrayList list of all the Explicit rules related to control and its dataType selected.
 	 */
 	public ArrayList getExplicitRules(String controlName, String dataType)
 	{
 		ArrayList<String> explicitRules = new ArrayList<String>();
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 
 			if (controlsConfigurationObject != null)
 			{
 				//explicitRules = (ArrayList<String>) getListOfRules(controlsConfigurationObject.getCommonExplicitRules());
 				Map map = controlsConfigurationObject.getDataTypeExplicitRules();
 				List rulesList = (ArrayList) map.get(dataType);
 				if (rulesList != null)
 				{
 					Iterator iter = rulesList.iterator();
 					while (iter.hasNext())
 					{
 						String ruleName = iter.next().toString();
 						if (!ruleName.equalsIgnoreCase(""))
 						{
 							explicitRules.add(ruleName);
 						}
 					}
 				}
 			}
 		}
 		return explicitRules;
 	}
 
 	/**
 	 * Returns the list in String format
 	 * @param rules list of rules
 	 * @return listOfRules 
 	 */
 	private List getListOfRules(List rules)
 	{
 		ArrayList<String> listOfRules = new ArrayList<String>();
 
 		Iterator iter = rules.iterator();
 		while (iter.hasNext())
 		{
 			listOfRules.add(iter.next().toString());
 		}
 		return listOfRules;
 	}
 
 	/**
 	 * gets the RuleConfigurationObject given a name of the rule.
 	 * @param ruleName name  of the rule
 	 * @return RuleConfigurationObject
 	 */
 	public RuleConfigurationObject getRuleObject(String ruleName)
 	{
 		RuleConfigurationObject ruleConfigurationObject = null;
 		if (ruleName != null && rulesConfigurationMap != null)
 		{
 			ruleConfigurationObject = (RuleConfigurationObject) rulesConfigurationMap.get(ruleName);
 		}
 
 		return ruleConfigurationObject;
 	}
 
 	/**
 	 * Gets all the controls required at the time of loading
 	 * @return List of controls
 	 */
 	public List getListOfControls()
 	{
 		ArrayList listOfControls = null;
 		if (controlsConfigurationMap != null)
 		{
 			listOfControls = (ArrayList) controlsConfigurationMap.get("ListOfControls");
 		}
 		return listOfControls;
 	}
 
 	/**
 	 * Gets the DisplayLabels of the rules.
 	 * @param ruleNamesList list of rules
 	 * @return List list of DisplayLabels
 	 */
 	public List getRuleDisplayLabels(List ruleNamesList)
 	{
 		List<String> listOfDisplayLabels = new ArrayList<String>();
 		Iterator iter = (Iterator) ruleNamesList.iterator();
 		RuleConfigurationObject ruleConfigurationObject = null;
 		while (iter.hasNext())
 		{
 			String ruleName = iter.next().toString();
 			ruleConfigurationObject = (RuleConfigurationObject) rulesConfigurationMap.get(ruleName);
 			listOfDisplayLabels.add(ruleConfigurationObject.getDisplayLabel());
 		}
 		return listOfDisplayLabels;
 	}
 
 	/**
 	 * This method returns rule instance for given rule name
 	 * @param ruleName name of the rule
 	 * @return ValidatorRuleInterface class instance of the rulename passed.
 	 * @throws DynamicExtensionsSystemException dynamicExtensionsSystemException
 	 */
 	public ValidatorRuleInterface getValidatorRule(String ruleName)
 			throws DynamicExtensionsSystemException
 	{
 		RuleConfigurationObject ruleConfiguration = (RuleConfigurationObject) rulesConfigurationMap
 				.get(ruleName);
 		ValidatorRuleInterface ruleInterface;
 
 		Class ruleClass;
 
 		try
 		{
 			ruleClass = Class.forName(ruleConfiguration.getRuleClassName());
 			ruleInterface = (ValidatorRuleInterface) ruleClass.newInstance();
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		return ruleInterface;
 	}
 
 	/**
 	 * 
 	 * @param controlName selected by user
 	 * @return Map of rules
 	 */
 	public Map getRulesMap(String controlName)
 	{
 		Map<String, List<RuleConfigurationObject>> rulesMap = new HashMap<String, List<RuleConfigurationObject>>();
 		ControlsConfigurationObject ccf = (ControlsConfigurationObject) controlsConfigurationMap
 				.get(controlName);
 		List dataTypes = ccf.getDataTypesList();
 		List rules = new ArrayList();
 		String dataType = null;
 		Iterator iter1 = null;
 		Iterator iter = dataTypes.iterator();
 		while (iter.hasNext())
 		{
 			dataType = ((NameValueBean) iter.next()).getName();
 			rules = (ArrayList) getExplicitRules(controlName, dataType);
 			iter1 = rules.iterator();
 			List<RuleConfigurationObject> ruleObjects = new ArrayList<RuleConfigurationObject>();
 			while (iter1.hasNext())
 			{
 
 				String ruleName = (String) iter1.next();
 				ruleObjects.add(getRuleObject(ruleName));
 
 			}
 			rulesMap.put(dataType, ruleObjects);
 		}
 		List commonRules = new ArrayList();
 		commonRules = (ArrayList) getCommonExplicitRules(controlName);
 		iter1 = commonRules.iterator();
 		List<RuleConfigurationObject> commonRuleObjects = new ArrayList<RuleConfigurationObject>();
 		while (iter1.hasNext())
 		{
 			String ruleName = (String) iter1.next();
 			commonRuleObjects.add(getRuleObject(ruleName));
 		}
 		rulesMap.put("commons", commonRuleObjects);
 		return rulesMap;
 	}
 
 	/**
 	 * Returns ExplicitRules : The rules which are will be shown on ui.
 	 * @param controlName selected by user
 	 * @param dataType selected by user
 	 * @return ArrayList list of all the Explicit rules related to control and its dataType selected.
 	 */
 	public ArrayList getCommonExplicitRules(String controlName)
 	{
 		ArrayList<String> explicitRules = new ArrayList<String>();
 		if ((controlName != null) && (controlsConfigurationMap != null))
 		{
 			ControlsConfigurationObject controlsConfigurationObject = (ControlsConfigurationObject) controlsConfigurationMap
 					.get(controlName);
 			if (controlsConfigurationObject != null)
 			{
 				explicitRules = (ArrayList<String>) getListOfRules(controlsConfigurationObject
 						.getCommonExplicitRules());
 			}
 		}
 		return explicitRules;
 	}
 
 	public static void main(String[] args) throws DynamicExtensionsSystemException
 	{
 		ControlConfigurationsFactory ccf = ControlConfigurationsFactory.getInstance();
 		List<NameValueBean> list = ccf.getListOfControls();
 
 		Iterator<NameValueBean> iter = list.iterator();
 		while (iter.hasNext())
 		{
 			NameValueBean nvb = iter.next();
			Logger.out.info(nvb.getName() + " " + nvb.getValue());
			Logger.out.info(ccf.getControlJspName(nvb.getName()));
 		}
 		//ccf.getRulesMap("TextControl");
 	}
 
 }
