 package edu.common.dynamicextensions.ui.webui.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.struts.upload.FormFile;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.wustl.common.actionForm.AbstractActionForm;
 import edu.wustl.common.domain.AbstractDomainObject;
 
 
 public class CommonControlModel extends AbstractActionForm
 {
 	/**
 	 * Attribute Name
 	 */
 	AbstractAttributeInterface abstractAttribute;
 	/**
 	 *
 	 */
 	protected String description;
 	/**
 	 * Concept code
 	 */
 
 	protected String attributeConceptCode;
 
 	/**
 	 *
 	 */
 	protected String dataType;
 
 	/**
 	 *
 	 */
 	protected List dataTypeList;
 
 	/**
 	 *
 	 */
 	protected String attributeSize;
 
 	/**
 	 *
 	 */
 	protected String attributeDefaultValue;
 
 	/**
 	 *
 	 */
 	protected String format;
 	/**
 	 *
 	 */
 	protected String attributeValidationRules;
 
 	/**
 	 *
 	 */
 	protected String attributeDisplayUnits;
 
 	/**
 	 *
 	 */
 	protected String referenceValues;
 	/**
 	 *
 	 */
 
 	protected String displayChoice;
 	/**
 	 *  number of decimal places
 	 */
 	protected String attributeDecimalPlaces;
 	/**
 	 * Number of digits in number
 	 */
 	protected String attributeDigits;
 
 	/**
 	 *
 	 */
 	protected String htmlFile;
 	/**
 	 * Data type changed
 	 */
 	protected String dataTypeChanged;
 	/**
 	 * Attribute identifier
 	 */
 	protected String attributeIdentifier;
 	/**
 	 *
 	 */
 	protected String caption;
 	/**
 	 *
 	 */
 	protected String attributeIsPassword;
 	/**
 	 *
 	 */
 	protected Boolean isPassword;
 	/**
 	 *
 	 */
 	protected Boolean isUrl;
 	/**
 	 *
 	 */
 	protected List toolsList = new ArrayList();
 	/**
 	 *
 	 */
 	protected Boolean isHidden;
 	/**
 	 *
 	 */
 	protected Integer sequenceNumber;
 	/**
 	 * order of controls on the form
 	 */
 	protected String controlsSequenceNumbers;
 	/**
 	 *
 	 */
 	protected String cssClass;
 	/**
 	 *
 	 */
 	protected String name;
 	/**
 	 *
 	 */
 	protected String tooltip;
 	/**
 	 *
 	 */
 	protected String attributeNoOfRows;
 	/**
 	 *
 	 */
 	protected String attributenoOfCols;
 	/**
 	 *
 	 */
 	protected String attributeMultiSelect;
 	/**
 	 *
 	 */
 	protected String attributeSequenceNumber;
 	/**
 	 *
 	 */
 	protected String attributeMeasurementUnits;
 	/**
 	 *
 	 */
 	protected String attributeScale;
 	/**
 	 *
 	 */
 	protected String userSelectedTool;
 	/**
 	 *
 	 */
 	protected Integer columns;
 	/**
 	 *
 	 */
 	protected Integer rows;
 	/**
 	 *
 	 */
 	protected Boolean isMultiSelect;
 
 	/**
 	 *
 	 */
 	protected String controlOperation;
 	/**
 	 *
 	 */
 	protected String selectedControlId;
 	/**
 	 *
 	 */
 
 	protected String rootName;
 
 	/**
 	 *
 	 */
 	protected List childList;
 
 	/**
 	 *
 	 */
 	protected String linesType;
 	/**
 	 *
 	 */
 	protected String dateValueType;
 	/**
 	 *
 	 */
 	protected String[] validationRules = new String[0];
 	/**
 	 *
 	 */
 	protected String[] tempValidationRules = new String[]{""};
 
 	/**
 	 * File formats for file control
 	 */
 	protected String[] fileFormats;
 
 	/**
 	 * option names
 	 */
 	protected String[] optionNames;
 	protected String[] optionDescriptions;
 	protected String[] optionConceptCodes;
 
 	protected List optionDetails;
 	/**
 	 *
 	 */
 	protected String min;
 	/**
 	 *
 	 */
 	protected String max;
 	/**
 	 *
 	 */
 	protected String minTemp;
 	/**
 	 *
 	 */
 	protected String maxTemp;
 	/**
 	 *
 	 */
 	protected Map controlRuleMap;
 	/**
 	 *
 	 */
 	protected String selectedControlCaption;
 	/**
 	 * is attribute identified
 	 */
 	protected String attributeIdentified;
 
 	/**
 	 * is attribute mandatory
 	 */
 	protected String attributeMandatory;
 	/**
 	 * List of measurement units
 	 */
 	protected List measurementUnitsList;
 
 	/**
 	 * This field will only be used if measurement unit "other" has been selected
 	 */
 	protected String measurementUnitOther;
 
 	/**
 	 * list of file formats
 	 */
 	protected List<String> supportedFileFormatsList;
 	/**
 	 * display as url
 	 */
 	protected String attributeDisplayAsURL;
 	/**
 	 * type of form selected for lookup
 	 */
 	protected String formTypeForLookup;
 	/**
 	 * Public domain ID for CDE
 	 */
 	protected String publicDomainId;
 	protected List groupNames;
 	protected List separatorList;
 	protected String groupName;
 	protected String formName;
 	protected String separator;
 	protected List selectedAttributes;
 	protected String[] selectedAttributeIds;
 	//Current container name
 	protected String currentContainerName;
 
 	protected FormFile csvFile;
 
 	protected FormFile tempcsvFile;
 
 	protected String csvString;
 
 	/**
 	 * @return the tempcsvFile
 	 */
 	public FormFile getTempcsvFile()
 	{
 		return tempcsvFile;
 	}
 
 	/**
 	 * @param tempcsvFile the tempcsvFile to set
 	 */
 	public void setTempcsvFile(FormFile tempcsvFile)
 	{
 		this.tempcsvFile = tempcsvFile;
 	}
 
 	/**
 	 * @return the current container name
 	 */
 	public String getCurrentContainerName()
 	{
 		return this.currentContainerName;
 	}
 
 	/**
 	 * @param currentContainerName set current container name
 	 */
 	public void setCurrentContainerName(String currentContainerName)
 	{
 		this.currentContainerName = currentContainerName;
 	}
 
 	/**
 	 * @return collection of EntityGroup names
 	 */
 	public List getGroupNames()
 	{
 		return this.groupNames;
 	}
 
 	/**
 	 * @param groupNames set collection of EntityGroup names
 	 */
 	public void setGroupNames(List groupNames)
 	{
 		this.groupNames = groupNames;
 	}
 
 	/**
 	 *
 	 * @return List MeasurementUnitsList
 	 */
 	public List getMeasurementUnitsList()
 	{
 		return this.measurementUnitsList;
 	}
 
 	/**
 	 *
 	 * @param measurementUnitsList list of measurementUnits
 	 */
 	public void setMeasurementUnitsList(List measurementUnitsList)
 	{
 		this.measurementUnitsList = measurementUnitsList;
 	}
 
 	/**
 	 *
 	 * @return String AttributeIdentified
 	 */
 	public String getAttributeIdentified()
 	{
 		return this.attributeIdentified;
 	}
 
 	/**
 	 *
 	 * @param attributeIdentified attributeIdentified
 	 */
 	public void setAttributeIdentified(String attributeIdentified)
 	{
 		this.attributeIdentified = attributeIdentified;
 	}
 
 	/**
 	 *
 	 * @return String selectedControlCaption
 	 */
 	public String getSelectedControlCaption()
 	{
 		return this.selectedControlCaption;
 	}
 
 	/**
 	 *
 	 * @param selectedControlCaption selectedControlCaption
 	 */
 	public void setSelectedControlCaption(String selectedControlCaption)
 	{
 		this.selectedControlCaption = selectedControlCaption;
 	}
 
 	/**
 	 *
 	 */
 	public void reset()
 	{
 		// TODO This method will be provided if required.
 	}
 
 	/**
 	 * Returns the id assigned to form bean.
 	 * @return the id assigned to form bean.
 	 */
 	public int getFormId()
 	{
 		return DEConstants.ATTRIBUTE_FORM_ID;
 	}
 
 	/**
 	 * @param abstractDomain abstractDomain
 	 */
 	public void setAllValues(AbstractDomainObject abstractDomain)
 	{
 		// TODO This method will be provided if required.
 	}
 
 	/**
 	 * @return Returns the attributeDescription.
 	 */
 	public String getDescription()
 	{
 		return description;
 	}
 
 	/**
 	 * @param description The attributeDescription to set.
 	 */
 	public void setDescription(String description)
 	{
 		this.description = description;
 	}
 
 	/**
 	 * @return Returns the dataTypeList.
 	 */
 	public List getDataTypeList()
 	{
 		return dataTypeList;
 	}
 
 	/**
 	 * @param dataTypeList The dataTypeList to set.
 	 */
 	public void setDataTypeList(List dataTypeList)
 	{
 		this.dataTypeList = dataTypeList;
 	}
 
 	/**
 	 * @return Returns the dataType.
 	 */
 	public String getDataType()
 	{
 		return dataType;
 	}
 
 	/**
 	 * @param dataType The dataType to set.
 	 */
 	public void setDataType(String dataType)
 	{
 		this.dataType = dataType;
 	}
 
 	/**
 	 * @return Returns the dataTypeChanged.
 	 */
 	public String getDataTypeChanged()
 	{
 		return dataTypeChanged;
 	}
 
 	/**
 	 * @param dataTypeChanged The dataTypeChanged to set.
 	 */
 	public void setDataTypeChanged(String dataTypeChanged)
 	{
 		this.dataTypeChanged = dataTypeChanged;
 	}
 
 	/**
 	 * @return Returns the htmlFile.
 	 */
 	public String getHtmlFile()
 	{
 		return htmlFile;
 	}
 
 	/**
 	 * @param htmlFile The htmlFile to set.
 	 */
 	public void setHtmlFile(String htmlFile)
 	{
 		this.htmlFile = htmlFile;
 	}
 
 	/**
 	 * @return Returns the attributeSize.
 	 */
 	public String getAttributeSize()
 	{
 		return attributeSize;
 	}
 
 	/**
 	 * @param attributeSize The attributeSize to set.
 	 */
 	public void setAttributeSize(String attributeSize)
 	{
 		this.attributeSize = attributeSize;
 	}
 
 	/**
 	 * @return Returns the attributeDefaultValue.
 	 */
 	public String getAttributeDefaultValue()
 	{
 		return attributeDefaultValue;
 	}
 
 	/**
 	 * @param attributeDefaultValue The attributeDefaultValue to set.
 	 */
 	public void setAttributeDefaultValue(String attributeDefaultValue)
 	{
 		this.attributeDefaultValue = attributeDefaultValue;
 	}
 
 	/**
 	 * @return Returns the attributeDisplayUnits.
 	 */
 	public String getAttributeDisplayUnits()
 	{
 		return attributeDisplayUnits;
 	}
 
 	/**
 	 * @param attributeDisplayUnits The attributeDisplayUnits to set.
 	 */
 	public void setAttributeDisplayUnits(String attributeDisplayUnits)
 	{
 		this.attributeDisplayUnits = attributeDisplayUnits;
 	}
 
 	/**
 	 * @return Returns the attributeFormat.
 	 */
 	public String getFormat()
 	{
 		return format;
 	}
 
 	/**
 	 * @param format The attributeFormat to set.
 	 */
 	public void setFormat(String format)
 	{
 		this.format = format;
 	}
 
 	/**
 	 * @return Returns the attributeValidationRules.
 	 */
 	public String getAttributeValidationRules()
 	{
 		return attributeValidationRules;
 	}
 
 	/**
 	 * @param attributeValidationRules The attributeValidationRules to set.
 	 */
 	public void setAttributeValidationRules(String attributeValidationRules)
 	{
 		this.attributeValidationRules = attributeValidationRules;
 	}
 
 	/**
 	 * @return Returns the attributeIdentifier.
 	 */
 	public String getAttributeIdentifier()
 	{
 		return attributeIdentifier;
 	}
 
 	/**
 	 * @param attributeIdentifier The attributeIdentifier to set.
 	 */
 	public void setAttributeIdentifier(String attributeIdentifier)
 	{
 		this.attributeIdentifier = attributeIdentifier;
 	}
 
 	/**
 	 * @return Returns the attributeDecimalPlaces.
 	 */
 	public String getAttributeDecimalPlaces()
 	{
 		return attributeDecimalPlaces;
 	}
 
 	/**
 	 * @param attributeDecimalPlaces The attributeDecimalPlaces to set.
 	 */
 	public void setAttributeDecimalPlaces(String attributeDecimalPlaces)
 	{
 		this.attributeDecimalPlaces = attributeDecimalPlaces;
 	}
 
 	/**
 	 * @return Returns the referenceValues.
 	 */
 	public String getReferenceValues()
 	{
 		return referenceValues;
 	}
 
 	/**
 	 * @param referenceValues The referenceValues to set.
 	 */
 	public void setReferenceValues(String referenceValues)
 	{
 		this.referenceValues = referenceValues;
 	}
 
 	/**
 	 * @return Returns the displayChoice.
 	 */
 	public String getDisplayChoice()
 	{
 		return displayChoice;
 	}
 
 	/**
 	 * @param displayChoice The displayChoice to set.
 	 */
 	public void setDisplayChoice(String displayChoice)
 	{
 		this.displayChoice = displayChoice;
 	}
 
 	/**
 	 * @return the attributeCssClass
 	 */
 	public String getCssClass()
 	{
 		return cssClass;
 	}
 
 	/**
 	 * @param cssClass the cssClass to set
 	 */
 	public void setCssClass(String cssClass)
 	{
 		this.cssClass = cssClass;
 	}
 
 	/**
 	 * @return the attributeMeasurementUnits
 	 */
 	public String getAttributeMeasurementUnits()
 	{
 		return attributeMeasurementUnits;
 	}
 
 	/**
 	 * @param attributeMeasurementUnits the attributeMeasurementUnits to set
 	 */
 	public void setAttributeMeasurementUnits(String attributeMeasurementUnits)
 	{
 		this.attributeMeasurementUnits = attributeMeasurementUnits;
 	}
 
 	/**
 	 * @return the attributeMultiSelect
 	 */
 	public String getAttributeMultiSelect()
 	{
 		return attributeMultiSelect;
 	}
 
 	/**
 	 * @param attributeMultiSelect the attributeMultiSelect to set
 	 */
 	public void setAttributeMultiSelect(String attributeMultiSelect)
 	{
 		this.attributeMultiSelect = attributeMultiSelect;
 		if (attributeMultiSelect != null)
 		{
 			isMultiSelect = Boolean.valueOf(attributeMultiSelect
 					.equals(ProcessorConstants.LIST_TYPE_MULTI_SELECT));
 		}
 	}
 
 	/**
 	 * @return the attributenoOfCols
 	 */
 	public String getAttributenoOfCols()
 	{
 		return attributenoOfCols;
 	}
 
 	/**
 	 * @param attributenoOfCols the attributenoOfCols to set
 	 */
 	public void setAttributenoOfCols(String attributenoOfCols)
 	{
 		this.attributenoOfCols = attributenoOfCols;
 	}
 
 	/**
 	 * @return the attributeNoOfRows
 	 */
 	public String getAttributeNoOfRows()
 	{
 		return attributeNoOfRows;
 	}
 
 	/**
 	 * @param attributeNoOfRows the attributeNoOfRows to set
 	 */
 	public void setAttributeNoOfRows(String attributeNoOfRows)
 	{
 		this.attributeNoOfRows = attributeNoOfRows;
 	}
 
 	/**
 	 * @return the attributeScale
 	 */
 	public String getAttributeScale()
 	{
 		return attributeScale;
 	}
 
 	/**
 	 * @param attributeScale the attributeScale to set
 	 */
 	public void setAttributeScale(String attributeScale)
 	{
 		this.attributeScale = attributeScale;
 	}
 
 	/**
 	 * @return the toolsList
 	 */
 	public List getToolsList()
 	{
 		return toolsList;
 	}
 
 	/**
 	 * @param toolsList the toolsList to set
 	 */
 	public void setToolsList(List toolsList)
 	{
 		this.toolsList = toolsList;
 	}
 
 	/**
 	 * @return the userSelectedTool
 	 */
 	public String getUserSelectedTool()
 	{
 		return userSelectedTool;
 	}
 
 	/**
 	 * @param userSelectedTool the userSelectedTool to set
 	 */
 	public void setUserSelectedTool(String userSelectedTool)
 	{
 		this.userSelectedTool = userSelectedTool;
 	}
 
 	/**
 	 * @return the caption
 	 */
 	public String getCaption()
 	{
 		return caption;
 	}
 
 	/**
 	 * @param caption the caption to set
 	 */
 	public void setCaption(String caption)
 	{
 		this.caption = caption;
 	}
 
 	/**
 	 *
 	 * @return Boolean isPassword
 	 */
 	public Boolean getIsPassword()
 	{
 		return this.isPassword;
 	}
 
 	/**
 	 *
 	 * @param isPassword isPassword
 	 */
 	public void setIsPassword(Boolean isPassword)
 	{
 		this.isPassword = isPassword;
 		if (isPassword != null)
 		{
 			this.attributeIsPassword = isPassword.toString();
 		}
 	}
 
 	/**
 	 * @return the tooltip
 	 */
 	public String getTooltip()
 	{
 		return tooltip;
 	}
 
 	/**
 	 * @param tooltip the tooltip to set
 	 */
 	public void setTooltip(String tooltip)
 	{
 		this.tooltip = tooltip;
 	}
 
 	/**
 	 *
 	 * @return AbstractAttributeInterface abstractAttributeInterface
 	 */
 	public AbstractAttributeInterface getAbstractAttribute()
 	{
 		return abstractAttribute;
 	}
 
 	/**
 	 *
 	 * @param abstractAttributeInterface abstractAttributeInterface
 	 */
 	public void setAbstractAttribute(AbstractAttributeInterface abstractAttributeInterface)
 	{
 		abstractAttribute = abstractAttributeInterface;
 	}
 
 	/**
 	 *
 	 * @return Integer columns
 	 */
 
 	public Integer getColumns()
 	{
 		if ((attributenoOfCols == null) || (attributenoOfCols.trim().equals("")))
 		{
 			columns = Integer.valueOf(0); //blank values will be considered as 0
 		}
 		else
 		{
 			columns = Integer.valueOf(attributenoOfCols);
 		}
 		return columns;
 	}
 
 	/**
 	 *
 	 * @return Boolean isHidden
 	 */
 	public Boolean getIsHidden()
 	{
 		return isHidden;
 	}
 
 	/**
 	 *
 	 * @return name name
 	 */
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 *
 	 * @return Integer  rows
 	 */
 	public Integer getRows()
 	{
 		if ((attributeNoOfRows == null) || (attributeNoOfRows.trim().equals("")))
 		{
 			rows = Integer.valueOf(0); //blank values will be considered as 0
 		}
 		else
 		{
 			rows = Integer.valueOf(attributeNoOfRows);
 		}
 
 		return rows;
 	}
 
 	/**
 	 *
 	 * @return Integer sequenceNumber
 	 */
 	public Integer getSequenceNumber()
 	{
 		if ((attributeSequenceNumber != null) && (attributeSequenceNumber.trim().equals("")))
 		{
 			sequenceNumber = Integer.valueOf(0); //blank values will be considered as 0
 		}
 		else
 		{
 			sequenceNumber = Integer.valueOf(attributeSequenceNumber);
 		}
 		return sequenceNumber;
 	}
 
 	/**
 	 *
 	 * @param columns Integer columns
 	 */
 	public void setColumns(Integer columns)
 	{
 		this.columns = columns;
 		if (columns != null)
 		{
 			this.attributenoOfCols = columns.toString();
 		}
 
 	}
 
 	/**
 	 *
 	 * @param isHidden isHidden
 	 */
 	public void setIsHidden(Boolean isHidden)
 	{
 		this.isHidden = isHidden;
 	}
 
 	/**
 	 *
 	 * @param name name to be set
 	 */
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	/**
 	 *
 	 * @param rows Integer rows
 	 */
 	public void setRows(Integer rows)
 	{
 		this.rows = rows;
 		if (rows != null)
 		{
 			this.attributeNoOfRows = rows.toString();
 		}
 	}
 
 	/**
 	 *
 	 * @param sequenceNumber Integer sequenceNumber
 	 */
 	public void setSequenceNumber(Integer sequenceNumber)
 	{
 		this.sequenceNumber = sequenceNumber;
 	}
 
 	/**
 	 * @return Returns the isMultiSelect.
 	 */
 	public Boolean getIsMultiSelect()
 	{
 		return isMultiSelect;
 	}
 
 	/**
 	 * @param isMultiSelect The isMultiSelect to set.
 	 */
 	public void setIsMultiSelect(Boolean isMultiSelect)
 	{
 		this.isMultiSelect = isMultiSelect;
		if ((isMultiSelect != null) && isMultiSelect)
 		{
 			this.attributeMultiSelect = ProcessorConstants.LIST_TYPE_MULTI_SELECT;
 		}
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.AbstractAttributeUIBeanInterface#getAttributeDigits()
 	 */
 	public String getAttributeDigits()
 	{
 		return attributeDigits;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.AbstractAttributeUIBeanInterface#setAttributeDigits(java.lang.String)
 	 */
 	public void setAttributeDigits(String attribDigits)
 	{
 		attributeDigits = attribDigits;
 	}
 
 	/**
 	 *
 	 * @return attributeSequenceNumber attributeSequenceNumber
 	 */
 	public String getAttributeSequenceNumber()
 	{
 		return this.attributeSequenceNumber;
 	}
 
 	/**
 	 *
 	 * @param attributeSequenceNumber attributeSequenceNumber
 	 */
 	public void setAttributeSequenceNumber(String attributeSequenceNumber)
 	{
 		this.attributeSequenceNumber = attributeSequenceNumber;
 	}
 
 	/**
 	 *
 	 * @return String attributeIsPassword
 	 */
 	public String getAttributeIsPassword()
 	{
 		return this.attributeIsPassword;
 	}
 
 	/**
 	 *
 	 * @param attributeIsPassword attributeIsPassword
 	 */
 	public void setAttributeIsPassword(String attributeIsPassword)
 	{
 		this.attributeIsPassword = attributeIsPassword;
 		isPassword = Boolean.valueOf(attributeIsPassword);
 	}
 
 	/**
 	 * @return Returns the controlOperation.
 	 */
 	public String getControlOperation()
 	{
 		return controlOperation;
 	}
 
 	/**
 	 * @param controlOperation The controlOperation to set.
 	 */
 	public void setControlOperation(String controlOperation)
 	{
 		this.controlOperation = controlOperation;
 	}
 
 	/**
 	 * @return Returns the selectedControlId.
 	 */
 	public String getSelectedControlId()
 	{
 		return selectedControlId;
 	}
 
 	/**
 	 * @param selectedControlId The selectedControlId to set.
 	 */
 	public void setSelectedControlId(String selectedControlId)
 	{
 		this.selectedControlId = selectedControlId;
 	}
 
 	/**
 	 * @return Returns the childList.
 	 */
 	public List getChildList()
 	{
 		return childList;
 	}
 
 	/**
 	 * @param childList The childList to set.
 	 */
 	public void setChildList(List childList)
 	{
 		this.childList = childList;
 	}
 
 	/**
 	 * @return Returns the rootName.
 	 */
 	public String getRootName()
 	{
 		return rootName;
 	}
 
 	/**
 	 * @param rootName The rootName to set.
 	 */
 	public void setRootName(String rootName)
 	{
 		this.rootName = rootName;
 	}
 
 	/**
 	 * @return Returns the showPreview.
 	 */
 	/*
 	 public String getShowPreview()
 	 {
 	 return showPreview;
 	 }
 
 	 *//**
 		 * @param showPreview The showPreview to set.
 		 */
 	/*
 	 public void setShowPreview(String showPreview)
 	 {
 	 this.showPreview = showPreview;
 	 }*/
 
 	/**
 	 * @return the linesType
 	 */
 	public String getLinesType()
 	{
 		return linesType;
 	}
 
 	/**
 	 * @param linesType the linesType to set
 	 */
 	public void setLinesType(String linesType)
 	{
 		this.linesType = linesType;
 	}
 
 	/**
 	 * @return attributeConceptCode
 	 */
 	public String getAttributeConceptCode()
 	{
 		return this.attributeConceptCode;
 	}
 
 	/**
 	 * @param attributeConceptCode attributeConceptCode
 	 */
 	public void setAttributeConceptCode(String attributeConceptCode)
 	{
 		this.attributeConceptCode = attributeConceptCode;
 	}
 
 	/**
 	 * @return String dateValueType
 	 */
 	public String getDateValueType()
 	{
 		return this.dateValueType;
 	}
 
 	/**
 	 * @param dateValueType String dateValueType
 	 */
 	public void setDateValueType(String dateValueType)
 	{
 		this.dateValueType = dateValueType;
 	}
 
 	/**
 	 * @return the validationRules
 	 */
 	public String[] getValidationRules()
 	{
 		return validationRules;
 	}
 
 	/**
 	 * @param validationRules the validationRules to set
 	 */
 	public void setValidationRules(String[] validationRules)
 	{
 		this.validationRules = validationRules;
 	}
 
 	/**
 	 * @return the controlRuleMap
 	 */
 	public Map getControlRuleMap()
 	{
 		return controlRuleMap;
 	}
 
 	/**
 	 * @param controlRuleMap the controlRuleMap to set
 	 */
 	public void setControlRuleMap(Map controlRuleMap)
 	{
 		this.controlRuleMap = controlRuleMap;
 	}
 
 	/**
 	 * @param dataTypeName String dataTypeName
 	 * @return List ListOfRules
 	 */
 	public List getListOfRules(String dataTypeName)
 	{
 		return (List) controlRuleMap.get(dataTypeName);
 	}
 
 	/**
 	 * @return the max
 	 */
 	public String getMax()
 	{
 		return max;
 	}
 
 	/**
 	 * @param max the max to set
 	 */
 	public void setMax(String max)
 	{
 		this.max = max;
 	}
 
 	/**
 	 * @return the min
 	 */
 	public String getMin()
 	{
 		return min;
 	}
 
 	/**
 	 * @param min the min to set
 	 */
 	public void setMin(String min)
 	{
 		this.min = min;
 	}
 
 	/**
 	 *
 	 * @return String measurementUnitOther
 	 */
 	public String getMeasurementUnitOther()
 	{
 		return this.measurementUnitOther;
 	}
 
 	/**
 	 *
 	 * @param measurementUnitOther measurementUnitOther
 	 */
 	public void setMeasurementUnitOther(String measurementUnitOther)
 	{
 		this.measurementUnitOther = measurementUnitOther;
 	}
 
 	/**
 	 * @return the tempValidationRules
 	 */
 	public String[] getTempValidationRules()
 	{
 		return tempValidationRules;
 	}
 
 	/**
 	 * @param tempValidationRules the tempValidationRules to set
 	 */
 	public void setTempValidationRules(String[] tempValidationRules)
 	{
 		this.tempValidationRules = tempValidationRules;
 	}
 
 	public List<String> getSupportedFileFormatsList()
 	{
 		return this.supportedFileFormatsList;
 	}
 
 	public void setSupportedFileFormatsList(List<String> fileFormatsList)
 	{
 		this.supportedFileFormatsList = fileFormatsList;
 	}
 
 	public String getAttributeMandatory()
 	{
 		return this.attributeMandatory;
 	}
 
 	public void setAttributeMandatory(String attributeMandatory)
 	{
 		this.attributeMandatory = attributeMandatory;
 	}
 
 	public String getAttributeDisplayAsURL()
 	{
 		return this.attributeDisplayAsURL;
 	}
 
 	public void setAttributeDisplayAsURL(String attributeDisplayAsURL)
 	{
 		this.attributeDisplayAsURL = attributeDisplayAsURL;
 		this.isUrl = Boolean.valueOf(attributeDisplayAsURL);
 	}
 
 	public String[] getOptionConceptCodes()
 	{
 		return this.optionConceptCodes;
 	}
 
 	public void setOptionConceptCodes(String[] optionConceptCodes)
 	{
 		this.optionConceptCodes = optionConceptCodes;
 	}
 
 	public String[] getOptionDescriptions()
 	{
 		return this.optionDescriptions;
 	}
 
 	public void setOptionDescriptions(String[] optionDescriptions)
 	{
 		this.optionDescriptions = optionDescriptions;
 	}
 
 	public String[] getOptionNames()
 	{
 		return this.optionNames;
 	}
 
 	public void setOptionNames(String[] optionNames)
 	{
 		this.optionNames = optionNames;
 	}
 
 	public List getOptionDetails()
 	{
 		return this.optionDetails;
 	}
 
 	public void setOptionDetails(List optionDetails)
 	{
 		this.optionDetails = optionDetails;
 	}
 
 	public String[] getFileFormats()
 	{
 		return this.fileFormats;
 	}
 
 	public void setFileFormats(String[] fileFormats)
 	{
 		this.fileFormats = fileFormats;
 	}
 
 	public String getFormTypeForLookup()
 	{
 		return this.formTypeForLookup;
 	}
 
 	public void setFormTypeForLookup(String formTypeForLookup)
 	{
 		this.formTypeForLookup = formTypeForLookup;
 	}
 
 	public String getPublicDomainId()
 	{
 		return this.publicDomainId;
 	}
 
 	public void setPublicDomainId(String publicDomainId)
 	{
 		this.publicDomainId = publicDomainId;
 	}
 
 	public String getGroupName()
 	{
 		return this.groupName;
 	}
 
 	public void setGroupName(String groupName)
 	{
 		this.groupName = groupName;
 	}
 
 	public String getFormName()
 	{
 		return this.formName;
 	}
 
 	public void setFormName(String formName)
 	{
 		this.formName = formName;
 	}
 
 	public List getSeparatorList()
 	{
 		return this.separatorList;
 	}
 
 	public void setSeparatorList(List separatorList)
 	{
 		this.separatorList = separatorList;
 	}
 
 	public String getSeparator()
 	{
 		return this.separator;
 	}
 
 	public void setSeparator(String separator)
 	{
 		this.separator = separator;
 	}
 
 	public String[] getSelectedAttributeIds()
 	{
 		return this.selectedAttributeIds;
 	}
 
 	public void setSelectedAttributeIds(String[] selectedAttributeIds)
 	{
 		this.selectedAttributeIds = selectedAttributeIds;
 	}
 
 	public List getSelectedAttributes()
 	{
 		return this.selectedAttributes;
 	}
 
 	public void setSelectedAttributes(List selectedAttributes)
 	{
 		this.selectedAttributes = selectedAttributes;
 	}
 
 	public String getControlsSequenceNumbers()
 	{
 		return this.controlsSequenceNumbers;
 	}
 
 	public void setControlsSequenceNumbers(String controlsSequenceNumbers)
 	{
 		this.controlsSequenceNumbers = controlsSequenceNumbers;
 	}
 
 	/**
 	 * @return the maxTemp
 	 */
 	public String getMaxTemp()
 	{
 		return maxTemp;
 	}
 
 	/**
 	 * @param maxTemp the maxTemp to set
 	 */
 	public void setMaxTemp(String maxTemp)
 	{
 		this.maxTemp = maxTemp;
 	}
 
 	/**
 	 * @return the minTemp
 	 */
 	public String getMinTemp()
 	{
 		return minTemp;
 	}
 
 	/**
 	 * @param minTemp the minTemp to set
 	 */
 	public void setMinTemp(String minTemp)
 	{
 		this.minTemp = minTemp;
 	}
 
 	/**
 	 * @return the csvFile
 	 */
 	public FormFile getCsvFile()
 	{
 		return csvFile;
 	}
 
 	/**
 	 * @param csvFile the csvFile to set
 	 */
 	public void setCsvFile(FormFile csvFile)
 	{
 		this.csvFile = csvFile;
 	}
 
 	/**
 	 * @return the isUrl
 	 */
 	public Boolean getIsUrl()
 	{
 		return isUrl;
 	}
 
 	/**
 	 * @param isUrl the isUrl to set
 	 */
 	public void setIsUrl(Boolean isUrl)
 	{
 		this.isUrl = isUrl;
 		if (isUrl != null)
 		{
 			this.attributeDisplayAsURL = isUrl.toString();
 		}
 	}
 
 	/**
 	 * @return the csvString
 	 */
 	public String getCsvString()
 	{
 		return csvString;
 	}
 
 	/**
 	 * @param csvString the csvString to set
 	 */
 	public void setCsvString(String csvString)
 	{
 		this.csvString = csvString;
 	}
 
 	@Override
 	public void setAddNewObjectIdentifier(String addNewFor, Long addObjectIdentifier)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 }
