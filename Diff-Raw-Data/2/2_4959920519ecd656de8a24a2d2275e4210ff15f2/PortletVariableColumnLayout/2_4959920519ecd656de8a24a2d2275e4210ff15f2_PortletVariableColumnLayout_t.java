 /*
  * @author <a href="mailto:makub@ics.muni.cz">Martin Kuba</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Collections;
 
 /**
  * The <code>PortletVariableColumnLayout</code> is a concrete implementation of the <code>PortletFrameLayout</code>
  * that organizes portlets into a column, but displays only one of its children. 
  * Displays only the child, which has the same label as is value of session attribute
  * with name <code>variant.layout.mylabel</code> where mylabel is label of this component.
  * If such session attribute doesn't exist, displays the child specified by "variant" attribute.
  */
 public class PortletVariableColumnLayout extends PortletFrameLayout implements Cloneable {
 
     /**
      * Prefix to be prepended to component label when searching session for variant.
      */
    public static final String LABEL_PREFIX = "variant.layout.";
 
     protected String variant = "";
 
     public PortletVariableColumnLayout() {
     }
 
     /**
      * Initializes the portlet component. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a list of component identifiers
      * @return a list of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(PortletRequest req, List list) {
         return  super.init(req, list);
     }
 
     public void setVariant(String variant) {
         this.variant = variant;
     }
 
     public String getVariant() {
         return variant;
     }
 
     /**
      * Renders the component
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         PortletResponse res = event.getPortletResponse();
         PortletRequest req = event.getPortletRequest();
         PrintWriter out = res.getWriter();
 
         PortletComponent p = null;
 
         // starting of the gridtable
 
         if (!components.isEmpty()) {
             out.println("<table width=\"100%\" cellspacing=\"2\" cellpadding=\"0\"> <!-- START COLUMN -->");
 
             out.println("<tbody>");
 
 	    //find which variant to display
 	    String thisLabel = this.getLabel();
 	    String sesvariant = null;
 	    if(thisLabel!=null) {
 	        Object _sesvariant = req.getSession(true).getAttribute(this.LABEL_PREFIX+thisLabel);
 	        if(_sesvariant instanceof String) { sesvariant=(String)_sesvariant; }
 	    }
 	    if(sesvariant==null) sesvariant=this.getVariant();
 
             List scomponents = Collections.synchronizedList(components);
             synchronized(scomponents) {
             for (int i=0;i<scomponents.size();i++) {
                 out.print("<tr><td valign=\"top\">");
 
                 p = (PortletComponent) scomponents.get(i);
 		String plabel = p.getLabel();
 
                 if (p.getVisible()&&sesvariant.equals(plabel)) {
                     p.doRender(event);
                 }
 
                 out.println("</td></tr>");
             }
             }
             out.println("</tbody>");
             out.println("</table> <!-- END COLUMN -->");
         }
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletVariableColumnLayout g = (PortletVariableColumnLayout)super.clone();
         return g;
     }
 
 }
 
 
 
