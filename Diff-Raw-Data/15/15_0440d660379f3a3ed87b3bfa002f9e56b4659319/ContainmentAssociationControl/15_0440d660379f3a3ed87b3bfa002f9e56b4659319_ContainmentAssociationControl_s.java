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
import edu.common.dynamicextensions.domaininterface.AssociationInterface;
import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 
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
 	 * @hibernate.many-to-one  cascade="save-update" column="DISPLAY_CONTAINER_ID" class="edu.common.dynamicextensions.domain.userinterface.Container" constrained="true"
 	 */
 	public ContainerInterface getContainer()
 	{
 		return container;
 	}
 
 	/**
 	 * @param container The container to set.
 	 */
 	public void setContainer(ContainerInterface container)
 	{
 		this.container = container;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domain.userinterface.Control#getControlLabelHTML()
 	 */
 	protected String getControlHTML(String htmlString)
 	{
 		return htmlString;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface#generateHTML()
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
 		return "&nbsp;";
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface#generateLinkHTML()
 	 */
 	public String generateLinkHTML() throws DynamicExtensionsSystemException
 	{
 		String detailsString = "Details";
 		StringBuffer stringBuffer = new StringBuffer();
 		stringBuffer.append("<span style='cursor:hand' class='" + cssClass + "' ");
 		stringBuffer.append("onclick='showChildContainerInsertDataPage(");
 		stringBuffer.append(this.getParentContainer().getId() + ",this");
 		stringBuffer.append(")'>");
 		stringBuffer.append(detailsString);
 		stringBuffer.append("</span>");
 
 		return stringBuffer.toString();
 	}
 
 }
