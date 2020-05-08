 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 
 /**
  * @author mandar_shidhore
  * @hibernate.joined-subclass table="DYEXTN_ABSTR_CONTAIN_CTR"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public abstract class AbstractContainmentControl extends Control implements AbstractContainmentControlInterface
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Parent container 
 	 */
 	protected ContainerInterface container;
 
 
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		ContainerInterface containerInterface = this.getContainer();
 		this.setIsSubControl(true);
		  if (this.getParentContainer().showAssociationControlsAsLink)
          {
              String link = containerInterface.generateLink(containerInterface);
              
              link = UserInterfaceiUtility.getControlHTMLAsARow(this, link);
              return link;
          }
          
 		String subContainerHTML = "";
 		if (isCardinalityOneToMany())
 		{
 			List<Map<BaseAbstractAttributeInterface, Object>> valueMapList = (List<Map<BaseAbstractAttributeInterface, Object>>) value;
 			subContainerHTML = containerInterface.generateControlsHTMLAsGrid(valueMapList);
 		}
 		else
 		{
 			if (value != null && ((List) value).size() > 0)
 			{
 				Map<BaseAbstractAttributeInterface, Object> displayContainerValueMap = ((List<Map<BaseAbstractAttributeInterface, Object>>) value).get(0);
 				containerInterface.setContainerValueMap(displayContainerValueMap);
 			}
 			this.getContainer().setShowAssociationControlsAsLink(true);
 			subContainerHTML = containerInterface.generateControlsHTML();
 		}
 		return subContainerHTML;
 	}
 
 	public String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		String subContainerHTML = "";
 		this.setIsSubControl(true);
 		if (isCardinalityOneToMany())
 		{
 			List<Map<BaseAbstractAttributeInterface, Object>> valueMapList = (List<Map<BaseAbstractAttributeInterface, Object>>) value;
 			String oldMode = this.getContainer().getMode();
 			this.getContainer().setMode(WebUIManagerConstants.VIEW_MODE);
 			subContainerHTML = this.getContainer().generateControlsHTMLAsGrid(valueMapList);
 			this.getContainer().setMode(oldMode);
 		}
 		else
 		{
 			if (value != null && ((List) value).size() > 0)
 			{
 				Map<BaseAbstractAttributeInterface, Object> displayContainerValueMap = ((List<Map<BaseAbstractAttributeInterface, Object>>) value).get(0);
 				this.getContainer().setContainerValueMap(displayContainerValueMap);
 			}
 			this.getContainer().setShowAssociationControlsAsLink(true);
 			String oldMode = this.getContainer().getMode();
 			this.getContainer().setMode(WebUIManagerConstants.VIEW_MODE);
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
 	
 	/**
 	 * @return container
 	 * @hibernate.many-to-one cascade="save-update" column="CONTAINER_ID" class="edu.common.dynamicextensions.domain.userinterface.Container" constrained="true"
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
 	
 
 }
