 package com.cyganov.simplecms.tags;
 
 import com.cyganov.simplecms.domain.Section;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tsyhanou Siarhei
  * Date: 17.07.13
  * Time: 12:05
  */
 public class SectionDisplayTag extends SimpleTagSupport {
 
 	private List<Section> list;
 
 	public void setList(List<Section> list) {
 		this.list = list;
 	}
 
 	@Override
 	public void doTag() throws JspException, IOException {
 		JspWriter out = getJspContext().getOut();
 		out.println(writeSections(list));
 		super.doTag();
 	}
 
 	private String writeSections(List<Section> list) throws IOException {
 
 		StringBuilder structure = new StringBuilder();
 		for (Section section : list){
 			if (section.isPublished()){
 				Section root = section;
 				while (root.getParent() != null){
 					root = root.getParent();
 				}
				structure.append("<li><a href=\"/site?rootId=").append(root.getId())
 						.append("&sectionId=").append(section.getId()).append("\">").append(section.getName()).append("</a>");
 				if (section.getChildren().size() != 0){
 					structure.append("<ul>");
 					structure.append(writeSections(section.getChildren()));
 					structure.append("</ul>");
 				}
 				structure.append("</li>");
 			}
 		}
 		return structure.toString();
 	}
 
 }
