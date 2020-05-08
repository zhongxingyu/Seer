 /**
  * 
  */
 package gov.nih.nci.cadsr.cdecurate.tool.tags;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 /**
  * This JSP tag library class is for action Menu
  * @author hveerla
  *
  */
 public class ObjMenuTag extends MenuTag {
 		
 	public int doEndTag() throws JspException {
 		
 		getSessionAttributes();
 		HttpSession session = pageContext.getSession();
 		int rowsChecked = 0;
 		if (vCheckList!=null){
 		    rowsChecked = vCheckList.size();
 		}
 		JspWriter objMenu = this.pageContext.getOut();
 		if (selACType != null) {
 			try {
 				objMenu.println("<table style = \"border-collapse: collapse; bgcolor: #d8d8df\">");
 				objMenu.println("<tr><td colspan=\"3\"><p style=\"margin: 0px 0px 5px 0px; color: red\"><span id=\"selCnt\">" + rowsChecked + "</span> Record(s) Selected</p></td></tr>");
 				objMenu.println("<tr style = \"background-color:#4876FF\"><td class=\"cell\"><input type=\"checkbox\" disabled></td><td class=\"cell\"><b><font color = \"#FFFFFF\">Action</b></td><td class=\"cell\"><input type=\"checkbox\" checked disabled></td></tr>");
                 
 			if ((selACType).equals("DataElement")) {
 					objMenu.println(displayEdit()
 									  + displayView()
 									  + displayDesignate()
 									  + displayViewDetiails()
 									  + displayGetAssociatedDEC()
 									  + displayGetAssociatedVD()
 									  + displayUploadDoc()
 									  + displayMonitor()
 									  + displayUnMonitor()
 									  + displayNewUsingExisting()
 									  + displayNewVersion()
 									  + displayAppend());
 				}
 				if ((selACType).equals("DataElementConcept")) {
 					objMenu.println(displayEdit()
 									  + displayView()
 									  + displayGetAssociatedDE()
 									  + displayUploadDoc()
 									  + displayMonitor()
 									  + displayUnMonitor()
 									  + displayNewUsingExisting()
 									  + displayNewVersion()
 									  + displayAppend());
 				}
 				if ((selACType).equals("ValueDomain")) {
 					objMenu.println(displayEdit()
 									  + displayView()
 									  + displayGetAssociatedDE()
 									  + displayUploadDoc()
 									  + displayMonitor()
 									  + displayUnMonitor()
 									  + displayNewUsingExisting()
 									  + displayNewVersion()
 									  + displayAppend());
 				}
 				if ((selACType).equals("ConceptualDomain")) {
 					objMenu.println(displayGetAssociatedDE()
 							          + displayGetAssociatedDEC()
 							          + displayGetAssociatedVD());
 				}
 				if ((selACType).equals("PermissibleValue")) {
 					objMenu.println(displayGetAssociatedDE()
 							          + displayGetAssociatedVD());
 				}
 				if ((selACType).equals("ClassSchemeItems")){
 					objMenu.println(displayGetAssociatedDE()
 							          + displayGetAssociatedDEC()
 							          + displayGetAssociatedVD());
 				}          
 				if ((selACType).equals("ValueMeaning")) {
 					objMenu.println(generateTR("edit","","performAction('edit')","","","Edit")
 							          + displayView()
 							          + displayGetAssociatedDE()
 							          + displayGetAssociatedVD());
 				}
 				if ((selACType).equals("ConceptClass")) {
 					objMenu.println(displayGetAssociatedDE()
 							          + displayGetAssociatedDEC()
 							          + displayGetAssociatedVD());
 			    }
 				if ((selACType).equals("ObjectClass")){
 					objMenu.println(displayGetAssociatedDEC());
 				}
 				if ((selACType).equals("Property")) {
 					objMenu.println(displayGetAssociatedDEC());
 				}
 				
 				objMenu.println(generateTR("","","","new","ShowSelectedRows(true)","Show Selected Rows"));
 						         
 				if (sSelectAll.equals("true")){
 					objMenu.println(generateTR("","","","new","SelectAllCheckBox()","Clear All"));
                  }else{
                 	 objMenu.println(generateTR("","","","new","SelectAllCheckBox()","Select All"));  
                  }
 				objMenu.println("</table></div>");
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return EVAL_PAGE;
 	}
 	public String generateTR(String id, String imageSingle, String jsMethodSingle, String imageMultiple, String jsMethodMultiple, String value){
 		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
 		String image_single = null;
 		String image_multiple = null;
 		String tdTag1 = null;
 		String tdTag2 = null;
 		if (imageSingle != null && !(imageSingle == "" ))
 		  image_single = "<img src=\""+ request.getContextPath() +"/images/"+imageSingle+".gif\" border=\"0\">";
 		else
 			image_single = "---";
 		if (imageMultiple != null && !(imageMultiple == "" ))
 			image_multiple = "<img src=\""+ request.getContextPath() +"/images/"+imageMultiple+".gif\" border=\"0\">";
 		else
 			image_multiple = "---";
 		if (image_single == "---"){
 			tdTag1 = "<td class=\"cell\" align = \"center\" style = \"cursor:default\">"+image_single+"</td>";
 		}else{
 			tdTag1 = "<td class=\"cell\" align = \"center\" onmouseover=\"menuItemFocusRed(this);\" onmouseout=\"menuItemNormal(this);\" onclick=\"javascript:" + jsMethodSingle + ";\">"+image_single+"</td>";
 		}
         if (image_multiple == "---"){
         	tdTag2 = "<td class=\"cell\" align = \"center\" style = \"cursor:default\">"+image_multiple+"</td>" ;
 		}else{
 			tdTag2 = "<td class=\"cell\" align = \"center\" onmouseover=\"menuItemFocus(this);\" onmouseout=\"menuItemNormal(this);\" onclick=\"javascript:" + jsMethodMultiple + ";\">"+image_multiple+"</td>";
 		}
 		String tag ="<tr>"
 			        + tdTag1
 			        +"<td class=\"cell\" align = \"left\">"+ value +"</td>" 
 			        + tdTag2
 			        +"</tr>"; 
 		return tag;
 	}
 	public String displayEdit(){
 		String tag = generateTR("edit","edit","performUncheckedCkBoxAction('edit')","block_edit","performAction('blockEdit')","Edit");
 		return tag;	
 	}
 	public String displayView(){
 		String tag = generateTR("view","new","","","","View");
 		return tag;	
 	}
 	public String displayDesignate(){
		String tag = generateTR("","designate","performUncheckedCkBoxAction('designate')","designate","performAction('designate')","Designate");
 		return tag;	
 	}
 	public String displayViewDetiails(){
		String tag = generateTR("details","new","GetDetails()","","","View Details");
 		return tag;	
 	}
 	public String displayGetAssociatedDE(){
		String tag = generateTR("associatedDE","getAssociated","getAssocDEs()","","","Get Associated DE");
 		return tag;	
 	}
 	public String displayGetAssociatedDEC(){
		String tag = generateTR("associatedDEC","getAssociated","getAssocDECs()","","","Get Associated DEC");
 		return tag;	
 	}
 	public String displayGetAssociatedVD(){
		String tag = generateTR("associatedVD","getAssociated","getAssocVDs()","","","Get Associated VD");
 		return tag;	
 	}
 	public String displayUploadDoc(){
		String tag = generateTR("uploadDoc","uploadDoc","performUncheckedCkBoxAction('uploadDoc')","","","Upload Document(s)");
 		return tag;	
 	}
 	public String displayMonitor(){
		String tag = generateTR("","monitor","performUncheckedCkBoxAction('monitor')","monitor","performAction('monitor')","Monitor");
 		return tag;	
 	}
 	public String displayUnMonitor(){
		String tag = generateTR("","unmonitor","performUncheckedCkBoxAction('unmonitor')","unmonitor","performAction('unmonitor')","Unmonitor");
 		return tag;	
 	}
 	public String displayNewUsingExisting(){
		String tag = generateTR("newUE","new","createNew('newUsingExisting')","","","New Using Existing");
 		return tag;	
 	}
 	public String displayNewVersion(){
		String tag = generateTR("newVersion","new","createNew('newVersion')","","","New Version");
 		return tag;	
 	}
 	public String displayAppend(){
 		String tag = generateTR("","","","new","performAction('append')","Append");
 		return tag;	
 	}
 }
