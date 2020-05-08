 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool.producers;
 
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.producers.NullaryProducer;
 import uk.org.ponder.rsf.view.ViewGroup;
 import uk.org.ponder.rsf.view.support.ViewGroupResolver;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 
 public class LayoutProducer implements NullaryProducer {
 
     private NullaryProducer pageproducer;
     public void setPageProducer(NullaryProducer pageproducer) {
         this.pageproducer = pageproducer;
     }
 
     private ViewGroupResolver viewGroupResolver;
     private ViewParameters viewParameters;
     private ViewGroup group;
 
     public void fillComponents(UIContainer tofill) {
 
         if (!viewGroupResolver.isMatch(group, viewParameters)){
             pageproducer.fillComponents(tofill);
         } else {
             UIJointContainer page = new UIJointContainer(tofill, "page-replace:", "page:");
 
             if (org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement() != null) {
                 //Initialize iframeId var -- for a few pages that need it still :-(
                 String frameId = org.sakaiproject.util.Web.escapeJavascript("Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId());
                 UIVerbatim.make(tofill, "iframeId_init", "var iframeId = \"" + frameId + "\";");
             }
             
             
             if (viewParameters.viewID.equals(ListProducer.VIEW_ID)){
                UILink.make(tofill, "asnn-js-include:","/sakai-assignment2-tool/content/js/inst-asnn-list.js");
                UILink.make(tofill, "asnn-css-include:","/sakai-assignment2-tool/content/css/inst-asnn-list.css");
             }
             else if (viewParameters.viewID.equals(ViewSubmissionsProducer.VIEW_ID)) {
                UILink.make(tofill, "asnn-js-include:", "/sakai-assignment2-tool/content/js/submissionview.js");
             }
             else {
                 makeIter3Javascript(tofill);
             }
 
             //include the components from the page body into tag "page-replace:"
             pageproducer.fillComponents(page);
         }
     }
 
     public void setViewGroupResolver(ViewGroupResolver viewGroupResolver) {
         this.viewGroupResolver = viewGroupResolver;
     }
     public void setViewParameters(ViewParameters viewParameters) {
         this.viewParameters = viewParameters;
     }
     public void setGroup(ViewGroup group) {
         this.group = group;
     }
     
     /**
      * This will create the legacy Javascript and CSS imports that a number of
      * the existing pages require, but we want to exclude for newer dynamic
      * pages that require bleeding edge versions of javascript libraries.
      * 
      * @param tofill
      */
     private void makeIter3Javascript(UIContainer tofill) {
        //UILink.make(tofill, "fluid-0.6beta1.js");
        //UILink.make(tofill, "ui.sortable.1.5.3.js");
        //UILink.make(tofill, "thickbox.js");
        //UILink.make(tofill, "jquery.color.js");
     }
 
 }
