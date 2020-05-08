 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.AbstractEntity;
 import edu.common.dynamicextensions.domain.DynamicExtensionBaseDomainObject;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:07 PM
  * @hibernate.class table="DYEXTN_CONTAINER"
  * @hibernate.cache  usage="read-write"
  */
 public class Container extends DynamicExtensionBaseDomainObject
 		implements
 			Serializable,
 			ContainerInterface
 {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 8092366994778601914L;
 
 	/**
 	 * @return
 	 * @hibernate.id name="id" column="IDENTIFIER" type="long"
 	 * length="30" unsaved-value="null" generator-class="native"
 	 * @hibernate.generator-param name="sequence" value="DYEXTN_CONTAINER_SEQ"
 	 */
 	public Long getId()
 	{
 		return id;
 	}
 
 	/**
 	 * css for the buttons on the container.
 	 */
 	protected String buttonCss;
 	/**
 	 * Caption to be displayed on the container.
 	 */
 	protected String caption;
 	/**
 	 * css for the main table in the container.
 	 */
 	protected String mainTableCss;
 	/**
 	 * Specifies the indicator symbol that will be used to denote a required field.
 	 */
 	protected String requiredFieldIndicatior;
 	/**
 	 * Specifies the warning mesaage to be displayed in case required fields are not entered by the user.
 	 */
 	protected String requiredFieldWarningMessage;
 	/**
 	 * css of the title in the container.
 	 */
 	protected String titleCss;
 	/**
 	 * Collection of controls that are in this container.
 	 */
 	protected Collection<ControlInterface> controlCollection = new HashSet<ControlInterface>();
 	/**
 	 *
 	 */
 	protected Map containerValueMap = new HashMap<BaseAbstractAttributeInterface, Object>();
 	/**
 	 * Entity to which this container is associated.
 	 */
 	protected AbstractEntity abstractEntity;
 	/**
 	 *
 	 */
 	protected String mode = WebUIManagerConstants.EDIT_MODE;
 	/**
 	 *
 	 */
 	protected Boolean showAssociationControlsAsLink = false;
 
 	/**
 	 * parent of this entity, null is no parent present.
 	 */
 	protected ContainerInterface baseContainer = null;
 
 	/**
 	 *
 	 */
 	protected ContainerInterface incontextContainer = this;
 
 	private Boolean addCaption = true;
 
 	private Collection<ContainerInterface> childContainerCollection = new HashSet<ContainerInterface>();
 
 	/**
 	 * @hibernate.set name="childContainerCollection" table="DYEXTN_CONTAINER"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="PARENT_CONTAINER_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.userinterface.Container"
 	 * @return the childCategories
 	 */
 	public Collection<ContainerInterface> getChildContainerCollection()
 	{
 		return childContainerCollection;
 	}
 
 	public void setChildContainerCollection(Collection<ContainerInterface> childContainerCollection)
 	{
 		this.childContainerCollection = childContainerCollection;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getMode()
 	{
 		return mode;
 	}
 
 	/**
 	 * @param mode
 	 */
 	public void setMode(String mode)
 	{
 		this.mode = mode;
 	}
 
 	/**
 	 * @hibernate.property name="buttonCss" type="string" column="BUTTON_CSS"
 	 * @return Returns the buttonCss.
 	 */
 	public String getButtonCss()
 	{
 		return buttonCss;
 	}
 
 	/**
 	 * @param buttonCss The buttonCss to set.
 	 */
 	public void setButtonCss(String buttonCss)
 	{
 		this.buttonCss = buttonCss;
 	}
 
 	/**
 	 * @hibernate.property name="caption" type="string" column="CAPTION" length="800"
 	 * @return Returns the caption.
 	 */
 	public String getCaption()
 	{
 		return caption;
 	}
 
 	/**
 	 * @param caption The caption to set.
 	 */
 	public void setCaption(String caption)
 	{
 		this.caption = caption;
 	}
 
 	/**
 	 * @hibernate.set name="controlCollection" table="DYEXTN_CONTROL"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="CONTAINER_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.userinterface.Control"
 	 * @return Returns the controlCollection.
 	 */
 	public Collection<ControlInterface> getControlCollection()
 	{
 		return controlCollection;
 	}
 
 	/**
 	 * @param controlCollection The controlCollection to set.
 	 */
 	public void setControlCollection(Collection<ControlInterface> controlCollection)
 	{
 		this.controlCollection = controlCollection;
 	}
 
 	/**
 	 * @hibernate.many-to-one column ="ABSTRACT_ENTITY_ID" class="edu.common.dynamicextensions.domain.AbstractEntity"
 	 * cascade="save-update"
 	 * @return Returns the entity.
 	 */
 	public AbstractEntityInterface getAbstractEntity()
 	{
 		return abstractEntity;
 	}
 
 	/**
 	 * @hibernate.property name="mainTableCss" type="string" column="MAIN_TABLE_CSS"
 	 * @return Returns the mainTableCss.
 	 */
 	public String getMainTableCss()
 	{
 		return mainTableCss;
 	}
 
 	/**
 	 * @param mainTableCss The mainTableCss to set.
 	 */
 	public void setMainTableCss(String mainTableCss)
 	{
 		this.mainTableCss = mainTableCss;
 	}
 
 	/**
 	 * @hibernate.property name="requiredFieldIndicatior" type="string" column="REQUIRED_FIELD_INDICATOR"
 	 * @return Returns the requiredFieldIndicatior.
 	 */
 	public String getRequiredFieldIndicatior()
 	{
 		return requiredFieldIndicatior;
 	}
 
 	/**
 	 * @param requiredFieldIndicatior The requiredFieldIndicatior to set.
 	 */
 	public void setRequiredFieldIndicatior(String requiredFieldIndicatior)
 	{
 		this.requiredFieldIndicatior = requiredFieldIndicatior;
 	}
 
 	/**
 	 * @hibernate.property name="requiredFieldWarningMessage" type="string" column="REQUIRED_FIELD_WARNING_MESSAGE"
 	 * @return Returns the requiredFieldWarningMessage.
 	 */
 	public String getRequiredFieldWarningMessage()
 	{
 		return requiredFieldWarningMessage;
 	}
 
 	/**
 	 * @param requiredFieldWarningMessage The requiredFieldWarningMessage to set.
 	 */
 	public void setRequiredFieldWarningMessage(String requiredFieldWarningMessage)
 	{
 		this.requiredFieldWarningMessage = requiredFieldWarningMessage;
 	}
 
 	/**
 	 * @hibernate.property name="titleCss" type="string" column="TITLE_CSS"
 	 * @return Returns the titleCss.
 	 */
 	public String getTitleCss()
 	{
 		return titleCss;
 	}
 
 	/**
 	 * @param titleCss The titleCss to set.
 	 */
 	public void setTitleCss(String titleCss)
 	{
 		this.titleCss = titleCss;
 	}
 
 	/**
 	 *
 	 */
 	public void addControl(ControlInterface controlInterface)
 	{
 		if (controlCollection == null)
 		{
 			controlCollection = new HashSet<ControlInterface>();
 		}
 		controlCollection.add(controlInterface);
 		controlInterface.setParentContainer(this);
 	}
 
 	/**
 	 *
 	 */
 	public void setAbstractEntity(AbstractEntityInterface abstractEntityInterface)
 	{
 		abstractEntity = (AbstractEntity) abstractEntityInterface;
 	}
 
 	/**
 	 *
 	 * @param sequenceNumber
 	 * @return
 	 */
 	public ControlInterface getControlInterfaceBySequenceNumber(String sequenceNumber)
 	{
 		boolean found = false;
 		ControlInterface controlInterface = null;
 		Collection<ControlInterface> controlsCollection = this.getControlCollection();
 		if (controlsCollection != null)
 		{
 			Iterator<ControlInterface> controlsIterator = controlsCollection.iterator();
 			while (controlsIterator.hasNext())
 			{
 				controlInterface = controlsIterator.next();
 				if (controlInterface.getSequenceNumber().equals(Integer.valueOf(sequenceNumber)))
 				{
 					found = true;
 					break;
 				}
 			}
 		}
 		return found ? controlInterface : null;
 	}
 
 	/**
 	 *
 	 */
 	public void removeControl(ControlInterface controlInterface)
 	{
 		if ((controlInterface != null) && (controlCollection != null)
 				&& (controlCollection.contains(controlInterface)))
 		{
 			controlCollection.remove(controlInterface);
 		}
 	}
 
 	/**
 	 * remove all controls from the controls collection
 	 */
 	public void removeAllControls()
 	{
 		if (controlCollection != null)
 		{
 			controlCollection.clear();
 		}
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.EntityInterface#getAllAttributes()
 	 */
 	public List<ControlInterface> getAllControls()
 	{
 		List<ControlInterface> controlsList = new ArrayList<ControlInterface>(this
 				.getControlCollection());
 		Collections.sort(controlsList);
 
 		List<ControlInterface> baseControlsList = new ArrayList<ControlInterface>();
 
 		ContainerInterface baseContainer = this.baseContainer;
 		while (baseContainer != null)
 		{
 			baseControlsList = new ArrayList(baseContainer.getControlCollection());
 			Collections.sort(baseControlsList);
 			Collections.reverse(baseControlsList);
 
 			controlsList.addAll(baseControlsList);
 
 			baseContainer.setIncontextContainer(this);
 			baseContainer = baseContainer.getBaseContainer();
 
 		}
 		Collections.reverse(controlsList);
 		return controlsList;
 	}
 
 	/**
 	 * @return
 	 */
 	public List<ControlInterface> getAllControlsUnderSameDisplayLabel()
 	{
 		List<ControlInterface> controlsList = new ArrayList<ControlInterface>(this
 				.getControlCollection());
 		for (ContainerInterface containerInterface : childContainerCollection)
 		{
 			controlsList.addAll(containerInterface.getAllControls());
 		}
 
 		List<ControlInterface> baseControlsList = new ArrayList<ControlInterface>();
 
 		ContainerInterface baseContainer = this.baseContainer;
 		while (baseContainer != null)
 		{
 			baseControlsList = new ArrayList(baseContainer.getControlCollection());
 			Collections.reverse(baseControlsList);
 
 			controlsList.addAll(baseControlsList);
 
 			baseContainer.setIncontextContainer(this);
 			baseContainer = baseContainer.getBaseContainer();
 
 		}
 		Collections.sort(controlsList);
 		Collections.reverse(controlsList);
 		return controlsList;
 	}
 
 	/**
 	 * @param controlInterface
 	 */
 	private void updateValueMap(ControlInterface controlInterface)
 	{
 		Object value = containerValueMap.get(controlInterface.getBaseAbstractAttribute());
 		Map<BaseAbstractAttributeInterface, Object> displayContainerValueMap = null;
 		if (value != null && value instanceof List)
 		{
 			if (((List) value).size() > 0)
 			{
 				displayContainerValueMap = ((List<Map<BaseAbstractAttributeInterface, Object>>) value)
 						.get(0);
 				((AbstractContainmentControl) controlInterface).isCardinalityOneToMany();
 			}
 
 		}
 		else
 		{
 			displayContainerValueMap = (Map<BaseAbstractAttributeInterface, Object>) value;
 		}
 		if (displayContainerValueMap != null)
 		{
			for (BaseAbstractAttributeInterface abstractAttributeInterface : displayContainerValueMap.keySet())
 			{
 				containerValueMap.put(abstractAttributeInterface, displayContainerValueMap
 						.get(abstractAttributeInterface));
 			}
 			containerValueMap.remove(controlInterface.getBaseAbstractAttribute());
 		}
 
 	}
 
 	/**
 	 * @return return the HTML string for this type of a object
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String generateContainerHTML() throws DynamicExtensionsSystemException
 	{
 		StringBuffer stringBuffer = new StringBuffer();
 
 		stringBuffer
 				.append("<table summary='' cellpadding='3' cellspacing='0' align='center' width='100%'>");
 
 		if (this.getMode() != null
 				&& this.getMode().equalsIgnoreCase(WebUIManagerConstants.EDIT_MODE))
 		{
 			stringBuffer.append("<tr><td class='formMessage' colspan='3'><span class='font_red'>");
 			stringBuffer.append(this.getRequiredFieldIndicatior());
 			stringBuffer.append("&nbsp;</span><span class='font_gr_s'>");
 			stringBuffer.append(this.getRequiredFieldWarningMessage());
 			stringBuffer.append("</span></td></tr>");
 		}
 		else
 		{
 			//Changed by : Kunal
 			//Reviewed by: Sujay
 			//Container hierarchy can be n level
 			//So, mode of the n containers in the hierarchy need to be same.
 
 			ContainerInterface tempContainerInterface = this.baseContainer;
 			while (tempContainerInterface != null)
 			{
 				tempContainerInterface.setMode(this.mode);
 				tempContainerInterface = tempContainerInterface.getBaseContainer();
 			}
 		}
 		stringBuffer.append(generateControlsHTML());
 		stringBuffer.append("</table>");
 		return stringBuffer.toString();
 	}
 
 	/**
 	 * @return return the HTML string for this type of a object
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String generateControlsHTML() throws DynamicExtensionsSystemException
 	{
 		StringBuffer stringBuffer = new StringBuffer();
 
 		if (addCaption)
 		{
 			addCaption(stringBuffer);
 		}
 
 		List<ControlInterface> controlsList = getAllControlsUnderSameDisplayLabel(); //UnderSameDisplayLabel();
 		int lastRow = 0;
 		int i = 0;
 
 		for (ControlInterface control : controlsList)
 		{
 			Object value = containerValueMap.get(control.getBaseAbstractAttribute());
 			control.setValue(value);
 			if (lastRow == control.getSequenceNumber())
 			{
 				stringBuffer.append("<div style='float:left'>&nbsp;");
 				stringBuffer.append("</div>");
 			}
 			else
 			{
 				if (i != 0)
 				{
 					stringBuffer.append("</td></tr><tr><td height='7'></td></tr>");
 				}
 				stringBuffer.append("<tr valign='center'>");
 			}
 
 			stringBuffer.append(control.generateHTML());
 
 			i++;
 			lastRow = control.getSequenceNumber();
 
 		}
 		stringBuffer.append("</td></tr>");
 		this.showAssociationControlsAsLink = false;
 		return stringBuffer.toString();
 	}
 
 	private void addCaption(StringBuffer stringBuffer)
 	{
 		stringBuffer.append("<tr><td class='td_color_6e81a6' colspan='100' align='left'>");
 		stringBuffer.append(DynamicExtensionsUtility.getFormattedStringForCapitalization(this
 				.getCaption()));
 		stringBuffer.append("<tr><td height='5'></td></tr>");
 	}
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String generateControlsHTMLAsGrid(
 			List<Map<BaseAbstractAttributeInterface, Object>> valueMapList)
 			throws DynamicExtensionsSystemException
 	{
 		return UserInterfaceiUtility.generateHTMLforGrid(this, valueMapList);
 	}
 
 	/**
 	 * @return
 	 */
 	public Map<BaseAbstractAttributeInterface, Object> getContainerValueMap()
 	{
 		return containerValueMap;
 	}
 
 	/**
 	 * @param containerValueMap
 	 */
 	public void setContainerValueMap(Map<BaseAbstractAttributeInterface, Object> containerValueMap)
 	{
 		this.containerValueMap = containerValueMap;
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	public Boolean getShowAssociationControlsAsLink()
 	{
 		return showAssociationControlsAsLink;
 	}
 
 	/**
 	 *
 	 * @param showAssociationControlsAsLink
 	 */
 	public void setShowAssociationControlsAsLink(Boolean showAssociationControlsAsLink)
 	{
 		this.showAssociationControlsAsLink = showAssociationControlsAsLink;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface#generateLinkHTML()
 	 */
 	public String generateLink(ContainerInterface containerInterface)
 			throws DynamicExtensionsSystemException
 	{
 		String detailsString = "";
 		boolean isDataPresent = UserInterfaceiUtility.isDataPresent(containerInterface
 				.getContainerValueMap());
 		if (isDataPresent)
 		{
 			if (mode.equals(WebUIManagerConstants.EDIT_MODE))
 			{
 				detailsString = ApplicationProperties.getValue("eav.att.EditDetails");
 			}
 			else if (mode.equals(WebUIManagerConstants.VIEW_MODE))
 			{
 				detailsString = ApplicationProperties.getValue("eav.att.ViewDetails");
 			}
 		}
 		else
 		{
 			if (mode.equals(WebUIManagerConstants.EDIT_MODE))
 			{
 				detailsString = ApplicationProperties.getValue("eav.att.EnterDetails");
 			}
 			else if (mode.equals(WebUIManagerConstants.VIEW_MODE))
 			{
 				detailsString = ApplicationProperties.getValue("eav.att.NoDataToView");
 			}
 		}
 		StringBuffer stringBuffer = new StringBuffer();
 		stringBuffer
 				.append("<img src='de/images/ic_det.gif' alt='Details' width='12' height='12' hspace='3' border='0' align='absmiddle'><a href='#' style='cursor:hand' class='set1' onclick='showChildContainerInsertDataPage(");
 		stringBuffer.append(containerInterface.getId());
 		stringBuffer.append(",this)'>");
 		stringBuffer.append(detailsString);
 		stringBuffer.append("</a><tr><td></td></tr>");
 		return stringBuffer.toString();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.EntityInterface#getParentEntity()
 	 * @hibernate.many-to-one column="BASE_CONTAINER_ID" class="edu.common.dynamicextensions.domain.userinterface.Container" constrained="true"
 	 *                        cascade="save-update"
 	 */
 	public ContainerInterface getBaseContainer()
 	{
 		return baseContainer;
 	}
 
 	/**
 	 *
 	 * @param baseContainer
 	 */
 	public void setBaseContainer(ContainerInterface baseContainer)
 	{
 		this.baseContainer = baseContainer;
 	}
 
 	/**
 	 * @return the incontextContainer
 	 */
 	public ContainerInterface getIncontextContainer()
 	{
 		return incontextContainer;
 	}
 
 	/**
 	 * @param incontextContainer the incontextContainer to set
 	 */
 	public void setIncontextContainer(ContainerInterface incontextContainer)
 	{
 		this.incontextContainer = incontextContainer;
 	}
 
 	/**
 	 * @hibernate.property name="addCaption" type="boolean" column="ADD_CAPTION"
 	 * @return Returns the addCaption.
 	 */
 	public Boolean getAddCaption()
 	{
 		return addCaption;
 	}
 
 	public void setAddCaption(Boolean addCaption)
 	{
 		this.addCaption = addCaption;
 	}
 
 	/**
 	 * @param xPosition
 	 * @param yPosition
 	 * @return
 	 */
 	public ControlInterface getControlByPosition(Integer xPosition, Integer yPosition)
 	{
 		ControlInterface control = null;
 		for (ControlInterface controlInterface : controlCollection)
 		{
 			if (controlInterface.getSequenceNumber() != null
 					&& controlInterface.getSequenceNumber().equals(xPosition))
 			{
 				if (controlInterface.getSequenceNumber() != null
 						&& controlInterface.getYPosition().equals(yPosition))
 				{
 					control = controlInterface;
 				}
 			}
 		}
 		return control;
 	}
 
 }
