 /**
  * 
  */
 package gov.nih.nci.cadsr.cdecurate.tool.tags;
 
 import java.io.IOException;
 
 import gov.nih.nci.cadsr.cdecurate.util.ToolURL;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
 /**
  * This JSP tag library class is for Menu Bar
  * @author hveerla
  *
  */
 public class MenuBarTag extends TagSupport {
 	public int doEndTag() throws JspException {
 		JspWriter menuBar = this.pageContext.getOut();
 		String helpLink = "window.open('" + ToolURL.getCurationToolHelpURL(this.pageContext) +"', '_blank')";
 		try {
 			menuBar.println("<col style=\"width: 2in\"/><col />"
                             +"<tr>"
                             +"<td class=\"menuItemBlank\" align=\"left\">&nbsp;</td>"
                             +"<td>"
                             +"<table class=\"footerBanner1\" cellspacing=\"0\" cellpadding=\"0\">" 
                             +"<col style=\"width: 1px\"/><col style=\"width: 1px\"/><col style=\"width: 1px\"/><col style=\"width: 1px\"/>"
                             +"<col /><col style=\"width: 1px\"/>"
                             +"<tr>"
                             +"<td class=\"menuItemNormal\" onmouseover=\"menuOver(this, event);\" onmouseout=\"menuRootOut(this, event);\" onclick=\"showHomePage();\">Home</td>"
                             +generateTD("createMenu","Create") 
                             +generateTD("linksMenu","Links")
                             +"<td class=\"menuItemNormal\">&nbsp;</td>"
                             +"<td class=\"menuNormal\">&nbsp;</td>"
                             +"<td class=\"menuItemNormal\" onmouseover=\"menuOver(this, event);\" onmouseout=\"menuRootOut(this, event);\" onclick=\""+helpLink+";\">Help</td>"  
                             +"</tr>"
                             +"</table>"
                             +"</td></tr>");
 			menuBar.println("<form name=\"homePageForm\" method=\"post\" action=\"../../cdecurate/NCICurationServlet?reqType=getSearchFilter\"></form>");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return EVAL_PAGE;
 	}
 	/* This method will generate the td tag
 	 * @param menuId 
 	 * @param value - actual text to display on the page
 	 * @return  returns the String which contains td tag
 	 * 
 	 */
 	public String generateTD(String menuId, String value){
 		String tdTag = "<td class=\"menuItemNormal\""
                        +"onmouseover=\"menuRootOver(this, event);\""
                        +"onmouseout=\"menuRootOut(this, event);\""
                       +"onclick=\"menuShow(this, event, 'no');\""
                        +"menuID=\""+ menuId +"\">"
                        + value +"</td>";
 		return tdTag;
 	}
 }
