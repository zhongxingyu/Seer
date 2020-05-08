 package com.sun.facelets.tag.jsf.html;
 
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.sun.facelets.Facelet;
import com.sun.facelets.FaceletFactory;
 import com.sun.facelets.FaceletTestCase;
 
 public class HtmlTestCase extends FaceletTestCase {
 
     public void testPanelGrid() throws Exception {
        FacesContext faces = FacesContext.getCurrentInstance();

        FaceletFactory f = FaceletFactory.getInstance();
        Facelet at = f.getFacelet("panelGrid.xml");

        UIViewRoot root = faces.getViewRoot();
        at.apply(faces, root);
     }
 
 }
