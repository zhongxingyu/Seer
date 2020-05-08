 package je.techtribes.web.tagext;
 
 import je.techtribes.domain.badge.Badge;
 import je.techtribes.domain.badge.Badges;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 import java.io.IOException;
 
 public class BadgeTag extends SimpleTagSupport {
 
     private int id;
     private int size = 96;
 
     @Override
     public void doTag() throws JspException, IOException {
         Badge badge = Badges.find(id);
         if (badge != null) {
            getJspContext().getOut().write("<img src=\"/static/img/badges/" + id + ".png\" alt=\"" + badge.getName() +"\" title=\"" + badge.getName() + "\" border=\"0\" width=\"" + size + "px\"/>");
         }
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public void setSize(int size) {
         this.size = size;
     }
 
 }
