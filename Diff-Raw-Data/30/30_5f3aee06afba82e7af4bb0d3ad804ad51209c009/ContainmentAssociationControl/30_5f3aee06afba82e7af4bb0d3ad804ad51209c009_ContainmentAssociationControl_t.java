 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright:TODO</p>
  *@author Vishvesh Mulay
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 
 /**
  * @author vishvesh_mulay
  * @hibernate.joined-subclass table="DYEXTN_CONTAINMENT_CONTROL"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class ContainmentAssociationControl extends Control
 		implements
 			ContainmentAssociationControlInterface
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * 
 	 */
 	protected ContainerInterface container;
 
 	/**
 	 * 
 	 */
 	public ContainmentAssociationControl()
 	{
 		super();
 	}
 
 	/**
 	 * @return container
 	 * @hibernate.many-to-one  cascade="none" column="DISPLAY_CONTAINER_ID" class="edu.common.dynamicextensions.domain.userinterface.Container" constrained="true"
 	 */
 	public ContainerInterface getContainer()
 	{
 		return container;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface#setContainer(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public void setContainer(ContainerInterface container)
 	{
 		this.container = container;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see edu.common.dynamicextensions.domain.userinterface.Control#getControlHTML(java.lang.String)
 	 */
 	protected String getControlHTML(String htmlString)
 	{
 		return htmlString;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see edu.common.dynamicextensions.domain.userinterface.Control#generateEditModeHTML()
 	 */
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		String subContainerHTML = "";
 		if (isCardinalityOneToMany())
 		{
 			List<Map<AbstractAttributeInterface, Object>> valueMapList = (List<Map<AbstractAttributeInterface, Object>>) value;
 			subContainerHTML = this.getContainer().generateControlsHTMLAsGrid(valueMapList);
 		}
 		else
 		{
 			if (value != null && ((List) value).size() > 0)
 			{
 				Map<AbstractAttributeInterface, Object> displayContainerValueMap = ((List<Map<AbstractAttributeInterface, Object>>) value)
 						.get(0);
 				this.getContainer().setContainerValueMap(displayContainerValueMap);
 			}
 			this.getContainer().setShowAssociationControlsAsLink(true);
 			subContainerHTML = this.getContainer().generateControlsHTML();
 		}
 		return subContainerHTML;
 	}
 	
 	/**
 	 * This method returns true if the cardinality of the Containment Association is One to Many.
 	 * @return true if Caridnality is One to Many, false otherwise.
 	 */
 	public boolean isCardinalityOneToMany()
 	{
 		return UserInterfaceiUtility.isCardinalityOneToMany(this);
 	}
 
 	protected String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		String subContainerHTML = "";
 		if (isCardinalityOneToMany())
 		{
 			List<Map<AbstractAttributeInterface, Object>> valueMapList = (List<Map<AbstractAttributeInterface, Object>>) value;
 			String oldMode = this.getContainer().getMode();
 			this.getContainer().setMode("view");
 			subContainerHTML = this.getContainer().generateControlsHTMLAsGrid(valueMapList);
 			this.getContainer().setMode(oldMode);
 		}
 		else
 		{
 			if (value != null && ((List) value).size() > 0)
 			{
 				Map<AbstractAttributeInterface, Object> displayContainerValueMap = ((List<Map<AbstractAttributeInterface, Object>>) value)
 						.get(0);
 				this.getContainer().setContainerValueMap(displayContainerValueMap);
 			}
 			this.getContainer().setShowAssociationControlsAsLink(true);
 			String oldMode = this.getContainer().getMode();
 			this.getContainer().setMode("view");
 			subContainerHTML = this.getContainer().generateControlsHTML();
 			this.getContainer().setMode(oldMode);
 		}
 		return subContainerHTML;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface#generateLinkHTML()
 	 */
 	public String generateLinkHTML() throws DynamicExtensionsSystemException
 	{
 		String detailsString = "Details";
 		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<a href='#' style='cursor:hand' class='" + cssClass + "' ");
 		stringBuffer.append("onclick='showChildContainerInsertDataPage(");
 		stringBuffer.append(this.getParentContainer().getIncontextContainer().getId() + ",this");
 		stringBuffer.append(")'>");
 		stringBuffer.append(detailsString);
		stringBuffer.append("</a>");
 
 		return stringBuffer.toString();
 	}
 	
 		
 
 }
