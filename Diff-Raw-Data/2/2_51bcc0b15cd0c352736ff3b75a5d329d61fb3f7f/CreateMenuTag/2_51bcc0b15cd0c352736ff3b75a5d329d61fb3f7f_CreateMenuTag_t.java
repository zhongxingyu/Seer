 package gov.nih.nci.cadsr.cdecurate.tool.tags;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * This JSP tag library class is for Create Menu
  * @author hveerla
  *
  */
 public class CreateMenuTag extends MenuTag {
 	public int doEndTag() throws JspException {
 		JspWriter createMenu = this.pageContext.getOut();
 		HttpSession session = pageContext.getSession();
 		String userName = null;
 		if (session != null) {
 		   userName = (String) session.getAttribute("Username");
 		}
 		try {
 			createMenu.println("<dl class=\"menu\">"
 			    				+generateDT("","displayStepsToFollow()","New Using Existing")
 					            +generateDT("","displayStepsToFollow2()","New Version")
 					            +separator()
 					            +generateDT("","callCCNew('"+StringEscapeUtils.escapeJavaScript(userName)+"')","Concept Class")
 					            +generateDT("","callDENew('"+StringEscapeUtils.escapeJavaScript(userName)+"')","Data Element")
 					            +generateDT("","callDECNew('"+StringEscapeUtils.escapeJavaScript(userName)+"')","Data Element Concept")
 					            +generateDT("","callVDNew('"+StringEscapeUtils.escapeJavaScript(userName)+"')","Value Domain")
 					            +"</dl>");
 			createMenu.println(generateForm("newDEForm", "../../cdecurate/NCICurationServlet?reqType=newDEFromMenu")
 			                    +generateForm("newDECForm", "../../cdecurate/NCICurationServlet?reqType=newDECFromMenu")
			                    +generateForm("newVDForm", "../../cdecurate/NCICurationServlet?reqType=newVDFromMenu")
 			                    +generateForm("newCCForm", "../../cdecurate/NCICurationServlet?reqType=newCCFromMenu"));
         } catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return EVAL_PAGE;
 	}
 
 }
 
 
 
 
 
    
 
 
 
     
