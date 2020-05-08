 /*
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.tags.web.element;
 
 import org.gridlab.gridsphere.tags.web.model.DefaultList;
 
 import java.util.Iterator;
 
public class TableBean extends BaseElementBean {
 
     protected String tablestring = "table";
 
     protected DefaultList list = new DefaultList();
 
     public void add(TagBean bean) {
         list.addBean(bean);
     }
 
     protected String getCSS() {
         String css = new String();
         if (!cssStyle.equals("")) {
             css = css + " class='" + cssStyle + "' ";
         }
         css = css + " Style='";
         if (!color.equals("")) {
             css = css + "color:" + color + ";";
         }
         if (!font.equals("")) {
             css = css + "font:" + font + ";";
         }
         if (!backgroundcolor.equals("")) {
             css = css + "background:" + backgroundcolor + ";";
         }
 
         css = css + "'";
         return css;
     }
 
     public String toString() {
         String result = "<"+tablestring+" "+getCSS()+">";
         Iterator it = list.iterator();
         while (it.hasNext()) {
             result = result + it.next().toString();
         }
         result = result + "</"+tablestring+">";
         return result;
 
     }
 }
