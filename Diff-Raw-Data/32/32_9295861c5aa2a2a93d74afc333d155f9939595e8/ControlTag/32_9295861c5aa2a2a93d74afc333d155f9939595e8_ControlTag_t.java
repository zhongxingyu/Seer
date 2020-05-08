 package org.tothought.tags;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import org.apache.commons.lang3.StringUtils;
 
 public class ControlTag extends SimpleTagSupport {
 
 	private String text = "";
 	private String imageClass = "";
 	private String classes = "";
 	private String href = "#";
 	private Boolean shrinkwrap = false;
 	private String floatImage = "left";
 	
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 	public String getImageClass() {
 		return imageClass;
 	}
 
 	public void setImageClass(String imageClass) {
 		this.imageClass = imageClass;
 	}
 
 	public String getClasses() {
 		return classes;
 	}
 
 	public void setClasses(String classes) {
 		this.classes = classes;
 	}
 
 	public String getHref() {
 		return href;
 	}
 
 	public void setHref(String href) {
 		this.href = href;
 	}
 
 	public Boolean getShrinkwrap() {
 		return shrinkwrap;
 	}
 
 	public void setShrinkwrap(Boolean shrinkwrap) {
 		this.shrinkwrap = shrinkwrap;
 	}
 	
 	public String getFloatImage() {
 		return floatImage;
 	}
 
 	public void setFloatImage(String floatImage) {
 		this.floatImage = floatImage;
 	}
 
 	public void doTag() throws JspException {
 		PageContext pageContext = (PageContext) getJspContext();
 		JspWriter out = pageContext.getOut();
 		
 		String anchorClasses = this.createAnchorClassLink();
 		try {
 			StringBuilder sb = new StringBuilder();
			String image = "<span class=\"button-32-no-hover " + this.imageClass + "\"></span>";
			String text =  "<span class=\"text\">" + this.text + "</span>";
 		
 			sb.append("<a href=\"" + this.href + "\" class=\"control" + anchorClasses + "\">");

			if(this.floatImage.equalsIgnoreCase("left")){
				sb.append(image).append(text);
			}else{
				sb.append(text).append(image);
			}
			
 			sb.append("</a>");
 
 			out.write(sb.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private String createAnchorClassLink() {
 		String anchorClasses = "";
 		if(this.shrinkwrap){
 			anchorClasses = " shrink-wrap-control" + (!StringUtils.isEmpty(this.floatImage) ? ("-" + this.floatImage): " ");
 		}
 		anchorClasses += (!StringUtils.isEmpty(this.classes) ? (" " + this.classes): "");
 		return anchorClasses;
 	}
 }
