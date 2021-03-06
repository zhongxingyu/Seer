 /*
 * Copyright 2010 Wyona
  */
 
 package org.wyona.yanel.impl.resources.dashboard;
 
 import org.wyona.yanel.impl.resources.BasicXMLResource;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 
 /**
 * A simple Resource which extends BasicXMLResource
  */
 public class WorkflowDashboardResource extends BasicXMLResource {
     
     private static Logger log = Logger.getLogger(WorkflowDashboardResource.class);
     
     /**
     * This method overrides the method to create the InputStream called by BasicXMLResource
     * Since you extend the BasicXMLResource this has to contain well-formed xml.
     * Should return a InputStream which contains XML. 
     * Use String, StingBuffer, dom, jdom, org.apache.commons.io.IOUtils and so on to generate the XML.
      */
     protected InputStream getContentXML(String viewId) throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("requested viewId: " + viewId);
         }
 
         // TODO: Get list of workflow states from workflow definition (referenced by resource configuration)
         String workflowState = "draft";
         if (getEnvironment().getRequest().getParameter("workflow-state") != null) {
             workflowState = getEnvironment().getRequest().getParameter("workflow-state");
         }
 
         org.wyona.yarep.core.Node[] nodes = getRealm().getRepository().getSearcher().searchProperty("workflow-state", workflowState, "/");
 
         StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>");
         sb.append("<workflow-dashboard>");
         if (nodes.length > 0) {
             for (int i = 0; i < nodes.length; i++) {
                 sb.append("<node path=\"" + nodes[i].getPath() + "\">");
                 if (nodes[i] instanceof org.wyona.yarep.core.Revision) { // INFO: This only makes sense if revisions are being indexed actually
                     sb.append("<revision>" + ((org.wyona.yarep.core.Revision) nodes[i]).getRevisionName() + "</revision>");
                 } else {
                     sb.append("<no-revision/>");
                 }
                 sb.append("</node>");
             }
         } else {
             sb.append("<no-nodes-found/>");
         }
         sb.append("</workflow-dashboard>");
         return new ByteArrayInputStream(sb.toString().getBytes());
     }
 }
