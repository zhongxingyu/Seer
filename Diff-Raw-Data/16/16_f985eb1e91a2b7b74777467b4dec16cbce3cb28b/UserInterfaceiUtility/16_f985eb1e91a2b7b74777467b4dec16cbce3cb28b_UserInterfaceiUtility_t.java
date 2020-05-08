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
 
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
import edu.common.dynamicextensions.ui.util.FileAttributeRecordValue;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.common.util.global.ApplicationProperties;
 
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
             List<Map<AbstractAttributeInterface, Object>> valueMapList)
             throws DynamicExtensionsSystemException
     {
         StringBuffer stringBuffer = new StringBuffer();
         int rowCount = 0;
         if (valueMapList != null)
         {
             rowCount = valueMapList.size();
         }
 
         List<ControlInterface> controlsList = new ArrayList<ControlInterface>(subContainer
                 .getAllControls());
         
         // Do not sort the controls list; it jumbles up the attribute order
         //Collections.sort(controlsList);
 
         stringBuffer.append("<tr width='100%'><td colspan='3' class='formFieldContainer'>");
         stringBuffer.append("<div style='display:none' id='" + subContainer.getId()
                 + "_substitutionDiv'>");
         stringBuffer.append("<table>");
         subContainer.setContainerValueMap(new HashMap<AbstractAttributeInterface, Object>()); //empty hashmap to generate hidden row
         stringBuffer.append(getContainerHTMLAsARow(subContainer, -1));
         stringBuffer.append("</table>");
         stringBuffer.append("</div>");
 
         stringBuffer.append("<input type='hidden' name='" + subContainer.getId()
                 + "_rowCount' id= '" + subContainer.getId() + "_rowCount' value='" + rowCount
                 + "'/> ");
         stringBuffer.append("</td></tr>");
 
         stringBuffer.append("<tr width='100%'>");
         stringBuffer.append("<td class='formFieldContainer' colspan='3' align='center'>");
         stringBuffer.append("<table cellpadding='3' cellspacing='0' align='center' width='100%'>");
 
         stringBuffer.append("<tr width='100%'>");
         stringBuffer.append("<td class='formTitle' colspan='3' align='left'>");
         stringBuffer.append(subContainer.getCaption());
         stringBuffer.append("</td>");
         stringBuffer.append("</tr>");
 
         stringBuffer.append("<tr width='100%'>");
         stringBuffer.append("<td class='formFieldContainer' colspan='3'>");
         stringBuffer.append("<table id='" + subContainer.getId()
                 + "_table' cellpadding='3' cellspacing='0' align='center' width='100%'>");
 
         stringBuffer.append("<tr width='100%'>");
         stringBuffer.append("<th class='formRequiredNotice' width='1%'>&nbsp;</th>");
         for (ControlInterface control : controlsList)
         {
             boolean isControlRequired = isControlRequired(control);
             if (isControlRequired)
             {
                 stringBuffer.append("<th class='formRequiredLabel'>");
                 stringBuffer.append(subContainer.getRequiredFieldIndicatior() + "&nbsp;"
                         + control.getCaption());
             }
             else
             {
                 stringBuffer.append("<th class='formLabel'>");
                 stringBuffer.append("&nbsp;" + control.getCaption());
             }
             stringBuffer.append("</th>");
         }
 
         stringBuffer.append("</tr>");
         if (valueMapList != null)
         {
             int index = 1;
             for (Map<AbstractAttributeInterface, Object> rowValueMap : valueMapList)
             {
                 subContainer.setContainerValueMap(rowValueMap);
                 stringBuffer.append(getContainerHTMLAsARow(subContainer, index));
                 index++;
             }
         }
 
         stringBuffer.append("</table>");
 
         if (subContainer.getMode().equals("edit"))
         {
             stringBuffer
                     .append("<table cellpadding='3' cellspacing='0' align='center' width='100%'><tr>");
 
             stringBuffer.append("<td align='left'>");
             stringBuffer
                     .append("<button type='button' class='actionButton' id='removeRow' onclick=\"removeCheckedRow('"
                             + subContainer.getId() + "')\">");
             stringBuffer.append(ApplicationProperties.getValue("buttons.delete"));
             stringBuffer.append("</button>");
             stringBuffer.append("</td>");
 
             stringBuffer.append("<td align='right'>");
             stringBuffer
                     .append("<button type='button' class='actionButton' id='addMore' onclick=\"addRow('"
                             + subContainer.getId() + "')\">");
             stringBuffer.append(ApplicationProperties.getValue("eav.button.AddRow"));
             stringBuffer.append("</button>");
             stringBuffer.append("</td>");
 
             stringBuffer.append("</tr></table>");
         }
 
         stringBuffer.append("</td>");
         stringBuffer.append("</tr>");
 
         stringBuffer.append("</table></td></tr>");
 
         return stringBuffer.toString();
     }
 
     /**
      *
      * @param controlInterface
      * @return
      */
     public static boolean isControlRequired(ControlInterface controlInterface)
     {
         AbstractAttributeInterface abstractAttribute = controlInterface.getAbstractAttribute();
         Collection<RuleInterface> ruleCollection = abstractAttribute.getRuleCollection();
         boolean required = false;
         if (ruleCollection != null && !ruleCollection.isEmpty())
         {
             for (RuleInterface attributeRule : ruleCollection)
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
      * @param containerStack
      * @param containerInterface
      * @param valueMapStack
      * @param valueMap
      */
     public static void addContainerInfo(Stack<ContainerInterface> containerStack,
             ContainerInterface containerInterface,
             Stack<Map<AbstractAttributeInterface, Object>> valueMapStack,
             Map<AbstractAttributeInterface, Object> valueMap)
     {
         containerStack.push(containerInterface);
         valueMapStack.push(valueMap);
     }
 
     /**
      *
      * @param containerStack
      * @param valueMapStack
      */
     public static void removeContainerInfo(Stack<ContainerInterface> containerStack,
             Stack<Map<AbstractAttributeInterface, Object>> valueMapStack)
     {
         containerStack.pop();
         valueMapStack.pop();
     }
 
     /**
      * @param request
      */
     public static void clearContainerStack(HttpServletRequest request)
     {
         ContainerInterface containerInterface = (ContainerInterface) CacheManager
                 .getObjectFromCache(request, Constants.CONTAINER_INTERFACE);
         if (containerInterface != null && containerInterface.getId() != null)
         {
             request.setAttribute("containerIdentifier", containerInterface.getId().toString());
         }
 
         CacheManager.addObjectToCache(request, Constants.CONTAINER_STACK, null);
         CacheManager.addObjectToCache(request, Constants.VALUE_MAP_STACK, null);
         CacheManager.addObjectToCache(request, Constants.CONTAINER_INTERFACE, null);
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
 
         StringBuffer stringBuffer = new StringBuffer();
         Map<AbstractAttributeInterface, Object> containerValueMap = container
                 .getContainerValueMap();
         List<ControlInterface> controlsList = new ArrayList<ControlInterface>(container
                 .getAllControls());
         
         // Do not sort the controls list; it jumbles up the attribute order
         //Collections.sort(controlsList);
 
         stringBuffer.append("<tr width='100%'>");
 
         stringBuffer.append("<td class='formRequiredNotice' width='1%'>");
         if (container.getMode().equals("edit"))
         {
             stringBuffer.append("<input type='checkbox' name='deleteRow' value='' id='1'/>");
         }
         else
         {
             stringBuffer.append("&nbsp;");
         }
         stringBuffer.append("</td>");
         for (ControlInterface control : controlsList)
         {
             String controlHTML = "";
             control.setIsSubControl(true);
 
             if (control instanceof ContainmentAssociationControlInterface)
             {
                 controlHTML = ((ContainmentAssociationControlInterface) control).generateLinkHTML();
             }
             else
             {
                 if (containerValueMap != null)
                 {
                     Object value = containerValueMap.get(control.getAbstractAttribute());
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
 
             stringBuffer.append("<td class='formField'>");
             stringBuffer.append(controlHTML);
             stringBuffer.append("</td>");
         }
         stringBuffer.append("</tr>");
 
         return stringBuffer.toString();
     }
 
     /**
      * This method returns the associationControl for a given Container and its child caintener id
      * @param containerInterface
      * @param childContainerId
      * @return
      */
     public static ContainmentAssociationControl getAssociationControl(
             ContainerInterface containerInterface, String childContainerId)
     {
         Collection<ControlInterface> controlCollection = containerInterface.getAllControls();
         for (ControlInterface control : controlCollection)
         {
             if (control instanceof ContainmentAssociationControl)
             {
                 ContainmentAssociationControl containmentAssociationControl = (ContainmentAssociationControl) control;
                 String containmentAssociationControlId = containmentAssociationControl
                         .getContainer().getId().toString();
                 if (containmentAssociationControlId.equals(childContainerId))
                 {
                     return containmentAssociationControl;
                 }
                 else
                 {
                     containmentAssociationControl = getAssociationControl(
                             containmentAssociationControl.getContainer(), childContainerId);
                     if (containmentAssociationControl != null)
                     {
                         return containmentAssociationControl;
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      *
      * @param controlInterface
      * @param htmlString
      * @return
      */
     public static String getControlHTMLAsARow(ControlInterface controlInterface, String htmlString)
     {
         boolean isControlRequired = UserInterfaceiUtility.isControlRequired(controlInterface);
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append("<tr>");
 
         stringBuffer.append("<td class='formRequiredNotice' width='2%'>");
         if (isControlRequired)
         {
             stringBuffer.append(controlInterface.getParentContainer().getRequiredFieldIndicatior()
                     + "&nbsp;");
             stringBuffer.append("</td>");
 
             stringBuffer.append("<td class='formRequiredLabel' width='20%'>");
         }
         else
         {
             stringBuffer.append("&nbsp;");
             stringBuffer.append("</td>");
 
             stringBuffer.append("<td class='formLabel' width='20%'>");
         }
         stringBuffer.append(controlInterface.getCaption());
         stringBuffer.append("</td>");
 
         stringBuffer.append("<td class='formField'>");
         stringBuffer.append(htmlString);
         stringBuffer.append("</td>");
         stringBuffer.append("</tr>");
 
         return stringBuffer.toString();
     }
 
     /**
      * This method returns true if the cardinality of the Containment Association is One to Many.
      * @return true if Caridnality is One to Many, false otherwise.
      */
     public static boolean isCardinalityOneToMany(ContainmentAssociationControlInterface control)
     {
         boolean isOneToMany = false;
         AssociationInterface associationInterface = (AssociationInterface) control
                 .getAbstractAttribute();
         RoleInterface targetRole = associationInterface.getTargetRole();
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
     public static boolean isDataPresent(Map<AbstractAttributeInterface, Object> valueMap)
     {
         boolean isDataPresent = false;
         if (valueMap != null)
         {
             Set<Map.Entry<AbstractAttributeInterface, Object>> mapEntrySet = valueMap.entrySet();
             for (Map.Entry<AbstractAttributeInterface, Object> mapEntry : mapEntrySet)
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
                             isDataPresent = isDataPresent((Map<AbstractAttributeInterface, Object>) valueObject);
                             break;
                         }
                     }
                 }
             }
         }
         return isDataPresent;
     }
 }
