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
 				objMenu.println("<tr style = \"background-color:#4876FF\"><td class=\"cell\" align=\"center\"><input type=\"checkbox\" onclick=\"this.checked=false;\"></td><td class=\"cell\" align=\"center\"><b><font color = \"#FFFFFF\">Action</b></td><td class=\"cell\" align=\"center\"><input type=\"checkbox\" checked onclick=\"this.checked=true;\"></td></tr>");
                 
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
 					objMenu.println(generateTR("edit","edit","performUncheckedCkBoxAction('edit')","","","Edit")
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
 				
 				objMenu.println(generateTR("","","","16_show_rows","ShowSelectedRows(true)","Show Selected Rows"));
 				objMenu.println(generateTR("","","","select_all","SelectAllCheckBox()","Select All")); 		         
 				objMenu.println(generateTR("","","","unselect_all","UnSelectAllCheckBox()","Unselect All"));
                         
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
 			tdTag1 = "<td class=\"cell\" align = \"center\" onmouseover=\"menuItemFocus(this);\" onmouseout=\"menuItemNormal(this);\" onclick=\"javascript:" + jsMethodSingle + ";\">"+image_single+"</td>";
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
 		String tag = generateTR("edit","edit_16","performUncheckedCkBoxAction('edit')","block_edit_new","performAction('blockEdit')","Edit");
 		return tag;	
 	}
 	public String displayView(){
 		String tag = generateTR("view","16_preview","viewAC()","","","View");
 		return tag;	
 	}
 	public String displayDesignate(){
 		String tag = generateTR("","16_designate","performUncheckedCkBoxAction('designate')","16_designate_multi","performAction('designate')","Designate");
 		return tag;	
 	}
 	public String displayViewDetiails(){
 		String tag = generateTR("details","cde-book-16","GetDetails()","","","View Details");
 		return tag;	
 	}
 	public String displayGetAssociatedDE(){
 		String tag = generateTR("associatedDE","16_associated","getAssocDEs()","","","Get Associated DE");
 		return tag;	
 	}
 	public String displayGetAssociatedDEC(){
 		String tag = generateTR("associatedDEC","16_associated","getAssocDECs()","","","Get Associated DEC");
 		return tag;	
 	}
 	public String displayGetAssociatedVD(){
 		String tag = generateTR("associatedVD","16_associated","getAssocVDs()","","","Get Associated VD");
 		return tag;	
 	}
 	public String displayUploadDoc(){
 		String tag = generateTR("uploadDoc","16_uploading","performUncheckedCkBoxAction('uploadDoc')","","","Upload Document(s)");
 		return tag;	
 	}
 	public String displayMonitor(){
 		String tag = generateTR("","monitor","performUncheckedCkBoxAction('monitor')","monitor_multi","performAction('monitor')","Monitor");
 		return tag;	
 	}
 	public String displayUnMonitor(){
 		String tag = generateTR("","unmonitor","performUncheckedCkBoxAction('unmonitor')","unmonitor_multi","performAction('unmonitor')","Unmonitor");
 		return tag;	
 	}
 	public String displayNewUsingExisting(){
 		String tag = generateTR("newUE","16_new_use_existing","createNew('newUsingExisting')","","","New Using Existing");
 		return tag;	
 	}
 	public String displayNewVersion(){
 		String tag = generateTR("newVersion","16_new_version","createNew('newVersion')","","","New Version");
 		return tag;	
 	}
 	public String displayAppend(){
 		String tag = generateTR("","","","16_append","performAction('append')","Append");
 		return tag;	
 	}
 }
