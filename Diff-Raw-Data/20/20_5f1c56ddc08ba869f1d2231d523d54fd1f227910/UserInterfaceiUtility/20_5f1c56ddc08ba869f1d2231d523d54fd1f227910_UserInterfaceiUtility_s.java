 /**
  *
  */
 
 package edu.common.dynamicextensions.ui.webui.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 
 import edu.common.dynamicextensions.domain.AbstractEntity;
 import edu.common.dynamicextensions.domain.BaseAbstractAttribute;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.userinterface.AbstractContainmentControl;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.FormControlNotesInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 
 /**
  * @author chetan_patil
  *
  */
 public class UserInterfaceiUtility
 {
 
 	/**
 	 *
 	 * @param stringBuffer
 	 * @param controlInterface
 	 * @param containerInterface
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static String generateHTMLforGrid(ContainerInterface subContainer,
 			List<Map<BaseAbstractAttributeInterface, Object>> valueMaps)
 			throws DynamicExtensionsSystemException
 	{
 		StringBuffer htmlForGrid = new StringBuffer();
 
 		int rowCount = 0;
 		if (valueMaps != null)
 		{
 			rowCount = valueMaps.size();
 		}
 
 		List<ControlInterface> controls = new ArrayList<ControlInterface>(subContainer
 				.getAllControlsUnderSameDisplayLabel());
 
 		// Do not sort the controls list; it jumbles up the attributes order.
 		//Collections.sort(controlsList);
 
 		htmlForGrid.append("<tr width='100%'><td colspan='3'<div style='display:none' id='");
 		htmlForGrid.append(subContainer.getId());
 		htmlForGrid.append("_substitutionDiv'><table>");
 		subContainer.setContainerValueMap(new HashMap<BaseAbstractAttributeInterface, Object>()); //empty hashmap to generate hidden row
 		htmlForGrid.append(getContainerHTMLAsARow(subContainer, -1));
 		htmlForGrid.append("</table></div><input type='hidden' name='");
 
 		htmlForGrid.append(subContainer.getId());
 		htmlForGrid.append("_rowCount' id= '");
 		htmlForGrid.append(subContainer.getId());
 		htmlForGrid.append("_rowCount' value='");
 		htmlForGrid.append(rowCount);
 		htmlForGrid
 				.append("'/> </td></tr><tr width='100%'><td class='formFieldContainer_withoutBorder' colspan='100' align='center'><table cellpadding='3' cellspacing='0' align='center' width='100%'>");
 
 		if (subContainer.getAddCaption())
 		{
 			htmlForGrid
 					.append("<tr width='100%'><td class='td_color_6e81a6' colspan='3' align='left'>");
 			htmlForGrid.append(((AbstractEntity) subContainer.getAbstractEntity())
 					.getCapitalizedName(subContainer.getCaption()));
 			htmlForGrid.append("</td></tr>");
 		}
 
 		htmlForGrid.append("<tr width='100%'><td colspan='3' width='100%'>");
 
 		// For category attribute controls, if heading and/or notes are specified, then
 		// render the UI that displays heading followed by notes for particular
 		// category attribute controls.
 		for (ControlInterface control : controls)
 		{
 			if (control.getHeading() != null && control.getHeading().length() != 0)
 			{
 				htmlForGrid.append("<div width=100% class='td_color_6e81a6' align='left'>"
 						+ control.getHeading() + "</div>");
 			}
 
 			if (control.getFormNotes() != null && control.getFormNotes().size() != 0)
 			{
 				htmlForGrid.append("<div style='width:100%'>&nbsp</div>");
 
 				for (FormControlNotesInterface fcNote : control.getFormNotes())
 				{
 					htmlForGrid.append("<div style='width:100%' class='notes' align='left'>"
 							+ fcNote.getNote() + "</div>");
 				}
 			}
 		}
 
 		htmlForGrid.append("<table id='");
 
 		htmlForGrid.append(subContainer.getId());
 
 		htmlForGrid
 				.append("_table' cellpadding='3' cellspacing='3' border='0' align='center' width='100%'><tr width='100%' class='formLabel_withoutBorder'><th width='1%'>&nbsp;</th>");
 
 		for (ControlInterface control : controls)
 		{
 			boolean isControlRequired = false;
 			/*if(control.getBaseAbstractAttribute() != null)
 			{*/
 			isControlRequired = isControlRequired(control);
 			htmlForGrid.append("<th>");
 			if (isControlRequired)
 			{
 
 				htmlForGrid.append("<span class='font_red'>");
 				htmlForGrid.append(subContainer.getRequiredFieldIndicatior());
 				htmlForGrid.append("</span>&nbsp;&nbsp;<span class='font_bl_nor'>");
 				htmlForGrid.append(((BaseAbstractAttribute) control.getBaseAbstractAttribute())
 						.getCapitalizedName(control.getCaption()));
 				htmlForGrid.append("</span>");
 			}
 			else
 			{
 				htmlForGrid.append("&nbsp;&nbsp;<span class='font_bl_nor'>");
 				htmlForGrid.append(((BaseAbstractAttribute) control.getBaseAbstractAttribute())
 						.getCapitalizedName(control.getCaption()));
 				htmlForGrid.append("</span>");
 			}
 
 			htmlForGrid.append("</th>");
 		}
 
 		htmlForGrid.append("</tr>");
 		if (valueMaps != null)
 		{
 			int index = 1;
 			for (Map<BaseAbstractAttributeInterface, Object> rowValueMap : valueMaps)
 			{
 				subContainer.setContainerValueMap(rowValueMap);
 				htmlForGrid.append(getContainerHTMLAsARow(subContainer, index));
 				index++;
 			}
 		}
 
 		htmlForGrid.append("</table>");
 
 		if (subContainer.getMode().equals("edit"))
 		{
 			htmlForGrid
					.append("<table cellpadding='3' cellspacing='0' align='center' width='100%' class='td_color_e3e2e7'><tr><td align='left'><img src='de/images/b_delete.gif' alt='Delete' width='59' height='20' hspace='3' align='absmiddle' onclick=\"removeCheckedRow('");
			htmlForGrid.append(subContainer.getId());
			htmlForGrid.append("')\"><map alt='Delete'><area href='javascript:removeCheckedRow('");
			htmlForGrid.append(subContainer.getId());
			htmlForGrid
					.append("')' shape='default'></map></td><td align='right'><img src='de/images/b_add_more.gif' alt='Add More' width='76' height='20' hspace='3' vspace='2' align='absmiddle' onclick=\"addRow('");
 			//stringBuffer.append("<button type='button' class='actionButton' id='removeRow' onclick=\"removeCheckedRow('" + subContainer.getId()
 			//		+ "')\">");
 			//stringBuffer.append(ApplicationProperties.getValue("buttons.delete"));
 			//stringBuffer.append("</button>");
			htmlForGrid.append(subContainer.getId());
			htmlForGrid.append("')\"><map alt='Add More'><area href='javascript:\"addRow('");
			htmlForGrid.append(subContainer.getId());
			htmlForGrid.append("')' shape='default'></map></td></tr></table>");
 			//stringBuffer.append("<button type='button' class='actionButton' id='addMore' onclick=\"addRow('" + subContainer.getId() + "')\">");
 			//stringBuffer.append(ApplicationProperties.getValue("eav.button.AddRow"));
 			//stringBuffer.append("</button>");
 		}
 
 		htmlForGrid.append("</td></tr></table></td></tr>");
 
 		return htmlForGrid.toString();
 	}
 
 	/**
 	 *
 	 * @param control
 	 * @return
 	 */
 	public static boolean isControlRequired(ControlInterface control)
 	{
 		if (control.getBaseAbstractAttribute() instanceof AssociationMetadataInterface
 				|| control.getBaseAbstractAttribute() == null)
 		{
 			return false;
 		}
 
 		AttributeMetadataInterface attributeMetadata = (AttributeMetadataInterface) control
 				.getBaseAbstractAttribute();
 		Collection<RuleInterface> rules = attributeMetadata.getRuleCollection();
 
 		boolean required = false;
 		if (rules != null && !rules.isEmpty())
 		{
 			for (RuleInterface attributeRule : rules)
 			{
 				if (attributeRule.getName().equals("required"))
 				{
 					required = true;
 					break;
 				}
 			}
 		}
 
 		return required;
 	}
 
 	/**
 	 *
 	 * @param containers
 	 * @param container
 	 * @param valueMaps
 	 * @param valueMap
 	 */
 	public static void addContainerInfo(Stack<ContainerInterface> containers,
 			ContainerInterface container,
 			Stack<Map<BaseAbstractAttributeInterface, Object>> valueMaps,
 			Map<BaseAbstractAttributeInterface, Object> valueMap)
 	{
 		containers.push(container);
 		valueMaps.push(valueMap);
 	}
 
 	/**
 	 *
 	 * @param containers
 	 * @param valueMaps
 	 */
 	public static void removeContainerInfo(Stack<ContainerInterface> containers,
 			Stack<Map<BaseAbstractAttributeInterface, Object>> valueMaps)
 	{
 		containers.pop();
 		valueMaps.pop();
 	}
 
 	/**
 	 * @param request
 	 */
 	public static void clearContainerStack(HttpServletRequest request)
 	{
 		ContainerInterface container = (ContainerInterface) CacheManager.getObjectFromCache(
 				request, DEConstants.CONTAINER_INTERFACE);
 		if (container != null && container.getId() != null)
 		{
 			request.setAttribute("containerIdentifier", container.getId().toString());
 		}
 
 		CacheManager.addObjectToCache(request, DEConstants.CONTAINER_STACK, null);
 		CacheManager.addObjectToCache(request, DEConstants.VALUE_MAP_STACK, null);
 		CacheManager.addObjectToCache(request, DEConstants.CONTAINER_INTERFACE, null);
 		CacheManager.addObjectToCache(request, "rootRecordIdentifier", null);
 	}
 
 	/**
 	 * @param container
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static String getContainerHTMLAsARow(ContainerInterface container, int rowId)
 			throws DynamicExtensionsSystemException
 	{
 		StringBuffer contHtmlAsARow = new StringBuffer();
 
 		Map<BaseAbstractAttributeInterface, Object> containerValues = container
 				.getContainerValueMap();
 		List<ControlInterface> controls = new ArrayList<ControlInterface>(container
 				.getAllControlsUnderSameDisplayLabel());
 
 		// Do not sort the controls list; it jumbles up the attribute order
 		//Collections.sort(controlsList);
 
 		String rowClass = "formField_withoutBorder";
 		if (rowId % 2 == 0)
 		{
 			rowClass = "td_color_f0f2f6";
 		}
 		contHtmlAsARow.append("<tr width='100%'class='");
 		contHtmlAsARow.append(rowClass);
 		contHtmlAsARow.append("'><td width='1%'>");
 
 		if (container.getMode().equals("edit"))
 		{
 			contHtmlAsARow.append("<input type='checkbox' name='deleteRow' value='' id='1'/>");
 		}
 		else
 		{
 			contHtmlAsARow.append("&nbsp;");
 		}
 
 		contHtmlAsARow.append("</td>");
 
 		for (ControlInterface control : controls)
 		{
 			String controlHTML = "";
 			control.setIsSubControl(true);
 
 			if (control instanceof AbstractContainmentControlInterface)
 			{
 				controlHTML = ((AbstractContainmentControlInterface) control).generateLinkHTML();
 			}
 			else
 			{
 				if (containerValues != null)
 				{
 					Object value = containerValues.get(control.getBaseAbstractAttribute());
 					control.setValue(value);
 				}
 				controlHTML = control.generateHTML();
 				if (rowId != -1)
 				{
 					String oldName = control.getHTMLComponentName();
 					String newName = oldName + "_" + rowId;
 					controlHTML = controlHTML.replaceAll(oldName, newName);
 				}
 			}
 
 			contHtmlAsARow.append("<td valign='middle' NOWRAP='true'>");
 			contHtmlAsARow.append(controlHTML.replaceAll("style='float:left'", ""));
 			contHtmlAsARow.append("</td>");
 		}
 
 		contHtmlAsARow.append("</tr>");
 
 		return contHtmlAsARow.toString();
 	}
 
 	/**
 	 * This method returns the associationControl for a given Container and its child caintener id
 	 * @param container
 	 * @param childContainerId
 	 * @return
 	 */
 	public static AbstractContainmentControlInterface getAssociationControl(
 			ContainerInterface container, String childContainerId)
 	{
 		Collection<ControlInterface> controls = container.getAllControlsUnderSameDisplayLabel();
 		for (ControlInterface control : controls)
 		{
 			if (control instanceof AbstractContainmentControlInterface)
 			{
 				AbstractContainmentControl abstrctCntnmntControl = (AbstractContainmentControl) control;
 				Long containerId = abstrctCntnmntControl.getContainer().getId();
 				if (containerId != null)
 				{
 					String associationControlId = containerId.toString();
 					if (associationControlId.equals(childContainerId))
 					{
 						return abstrctCntnmntControl;
 					}
 					else
 					{
 						abstrctCntnmntControl = (AbstractContainmentControl) getAssociationControl(
 								abstrctCntnmntControl.getContainer(), childContainerId);
 						if (abstrctCntnmntControl != null)
 						{
 							return abstrctCntnmntControl;
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 *
 	 * @param control
 	 * @param htmlString
 	 * @return
 	 */
 	public static String getControlHTMLAsARow(ControlInterface control, String htmlString)
 	{
 		boolean isControlRequired = UserInterfaceiUtility.isControlRequired(control);
 
 		StringBuffer controlHtmlAsARow = new StringBuffer();
 		controlHtmlAsARow.append("<tr><td class='formRequiredNotice_withoutBorder' width='2%'>");
 		if (isControlRequired)
 		{
 			controlHtmlAsARow.append(control.getParentContainer().getRequiredFieldIndicatior());
 			controlHtmlAsARow.append("&nbsp;</td><td class='formRequiredLabel_withoutBorder'>");
 		}
 		else
 		{
 			controlHtmlAsARow.append("&nbsp;</td><td class='formRequiredLabel_withoutBorder'>");
 		}
 
 		controlHtmlAsARow.append(((BaseAbstractAttribute) control.getBaseAbstractAttribute())
 				.getCapitalizedName(control.getCaption()));
 		controlHtmlAsARow.append("</td><td class='formField_withoutBorder'>");
 		controlHtmlAsARow.append(htmlString);
 		controlHtmlAsARow.append("</td></tr>");
 
 		return controlHtmlAsARow.toString();
 	}
 
 	/**
 	 * This method returns true if the cardinality of the Containment Association is One to Many.
 	 * @return true if Caridnality is One to Many, false otherwise.
 	 */
 	public static boolean isCardinalityOneToMany(AbstractContainmentControlInterface control)
 	{
 		boolean isOneToMany = false;
 
 		AssociationInterface association = (AssociationInterface) control
 				.getBaseAbstractAttribute();
 		RoleInterface targetRole = association.getTargetRole();
 		if (targetRole.getMaximumCardinality() == Cardinality.MANY)
 		{
 			isOneToMany = true;
 		}
 
 		return isOneToMany;
 	}
 
 	/**
 	 *
 	 * @param container
 	 * @return
 	 */
 	public static boolean isDataPresent(Map<BaseAbstractAttributeInterface, Object> valueMap)
 	{
 		boolean isDataPresent = false;
 
 		if (valueMap != null)
 		{
 			Set<Map.Entry<BaseAbstractAttributeInterface, Object>> mapEntrySet = valueMap
 					.entrySet();
 			for (Map.Entry<BaseAbstractAttributeInterface, Object> mapEntry : mapEntrySet)
 			{
 				Object value = mapEntry.getValue();
 				if (value != null)
 				{
 					if ((value instanceof String) && (((String) value).length() > 0))
 					{
 						isDataPresent = true;
 						break;
 					}
 					else if ((value instanceof FileAttributeRecordValue)
 							&& (((FileAttributeRecordValue) value).getFileName().length() != 0))
 					{
 						isDataPresent = true;
 						break;
 					}
 					else if ((value instanceof List) && (!((List) value).isEmpty()))
 					{
 						List valueList = (List) value;
 						Object valueObject = valueList.get(0);
 
 						if ((valueObject != null) && (valueObject instanceof Long))
 						{
 							isDataPresent = true;
 							break;
 						}
 						else if ((valueObject != null) && (valueObject instanceof Map))
 						{
 							isDataPresent = isDataPresent((Map<BaseAbstractAttributeInterface, Object>) valueObject);
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return isDataPresent;
 	}
 
 }
