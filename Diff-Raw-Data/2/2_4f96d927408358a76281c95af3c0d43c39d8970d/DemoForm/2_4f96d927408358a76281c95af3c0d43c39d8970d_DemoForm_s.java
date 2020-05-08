 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.form;
 
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 
 /**
  * DemoForm collects the list of avialable requests for the demo.
  * <p>
  * Stores the request & post for the demo page, to be used by the DemoAction.
  * </p>
  *
  * @author jgarnett, Refractions Research, Inc.
  * @author $Author: jive $ (last modification)
 * @version $Id: DemoForm.java,v 1.3 2004/04/16 07:25:20 jive Exp $
  */
 public class DemoForm extends ActionForm {
     /**
          * Comment for <code>serialVersionUID</code>
          */
     private static final long serialVersionUID = 3978983293029005618L;
 
     /**
      *
      * @uml.property name="action" multiplicity="(0 1)"
      */
     private String action;
 
     /**
      *
      * @uml.property name="url" multiplicity="(0 1)"
      */
     private String url;
 
     /**
      *
      * @uml.property name="body" multiplicity="(0 1)"
      */
     private String body;
 
     /**
      *
      * @uml.property name="demo" multiplicity="(0 1)"
      */
     private String demo;
 
     /**
      *
      * @uml.property name="dir" multiplicity="(0 1)"
      */
     private File dir;
 
     /**
      *
      * @uml.property name="demoList"
      * @uml.associationEnd elementType="java.lang.String" multiplicity="(0 -1)"
      */
     List demoList;
 
     /**
      * Sets request & post based on file selection.
      *
      * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
      *
      * @param arg0
      * @param request
      */
     public void reset(ActionMapping arg0, HttpServletRequest request) {
         super.reset(arg0, request);
 
         ServletContext context = getServlet().getServletContext();
         //DJB: changed this for geoserver_data_dir 
         // this.dir = new File(context.getRealPath("/data/demo"));
         this.dir = new File(GeoserverDataDirectory.getGeoserverDataDirectory(), "/data/demo");
         demoList = new ArrayList();
         demoList.add("");
 
         if (dir.exists() && dir.isDirectory()) {
             File[] files = dir.listFiles();
 
             for (int i = 0; i < files.length; i++) {
                 File file = files[i];
                 demoList.add(file.getName());
             }
         }
 
         Collections.sort(demoList);
     }
 
     /**
      *
      * Verifies that username is not null or empty.
      * Could potentially do the same for password later.
      *
      * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
      *
      * @param mapping
      * @param request
      * @return
      */
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errors = new ActionErrors();
 
         return errors;
     }
 
     /**
      * @return Returns the demo.
      *
      * @uml.property name="demo"
      */
     public String getDemo() {
         return demo;
     }
 
     /**
      * @param demo The demo to set.
      *
      * @uml.property name="demo"
      */
     public void setDemo(String demo) {
         this.demo = demo;
     }
 
     /**
      * @return Returns the dir.
      *
      * @uml.property name="dir"
      */
     public File getDir() {
         return dir;
     }
 
     /**
      * @return Returns the url.
      *
      * @uml.property name="url"
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * @param url The url to set.
      *
      * @uml.property name="url"
      */
     public void setUrl(String url) {
         this.url = url;
     }
 
     /**
      * @return Returns the demoList.
      *
      * @uml.property name="demoList"
      */
     public List getDemoList() {
         return demoList;
     }
 
     /**
      * @return Returns the action.
      *
      * @uml.property name="action"
      */
     public String getAction() {
         return action;
     }
 
     /**
      * @param action The action to set.
      *
      * @uml.property name="action"
      */
     public void setAction(String action) {
         this.action = action;
     }
 
     /**
      * @return Returns the body.
      *
      * @uml.property name="body"
      */
     public String getBody() {
         return body;
     }
 
     /**
      * @param body The body to set.
      *
      * @uml.property name="body"
      */
     public void setBody(String body) {
         this.body = body;
     }
 }
