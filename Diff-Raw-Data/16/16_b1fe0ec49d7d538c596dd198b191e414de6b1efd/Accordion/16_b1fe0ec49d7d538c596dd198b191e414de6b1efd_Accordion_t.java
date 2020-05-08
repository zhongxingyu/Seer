 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.mobi.component.accordion;
 
 
 import org.icefaces.mobi.api.ContentPaneController;
 
 import javax.annotation.PostConstruct;
 import javax.faces.component.UIComponent;
 import javax.faces.component.behavior.FacesBehavior;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.el.MethodExpression;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangeEvent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Accordion extends AccordionBase implements ContentPaneController {
      private static Logger logger = Logger.getLogger(Accordion.class.getName());
      public static final String ACCORDION_CLASS = "mobi-accordion";
      public static final String ACCORDION_RIGHT_POINTING_TRIANGLE = "&#9654;";
      public static final String ACCORDION_RIGHT_POINTING_POINTER= "&#9658;";
      public static final String ACCORDION_LEFT_POINTING_TRIANGLE = "&#9664;";
      public static final String ACCORDION_LEFT_POINTING_POINTER= "&#9668;";
     private String selectedId;
      /**
      * method is required by ContentPaneController interface
      * returns null if their are no children of type contentPane or no children at all.
      * If activeIndex is outside of the range of 0 -> number of children -1, then the default
      * valid value is the first child.
      * @return
      */
      public Accordion(){
          super();
      }
 
      /**
       * method is required by ContentPaneController interface no error checking as
       * component is not in the tree
       */
      public String getSelectedId(){
          return getCurrentId();
      }
 
      public void queueEvent(FacesEvent event) {
        if (event.getComponent() == this) {
             if (logger.isLoggable(Level.FINEST)){
                 logger.finest("invoked event for Accordion with selectedId= " + this.getSelectedId());
             }   //no immediate for this component
             event.setPhaseId(PhaseId.INVOKE_APPLICATION);
         }
         super.queueEvent(event);
      }
 
      public void broadcast(javax.faces.event.FacesEvent event) throws AbortProcessingException {
         super.broadcast(event);
         FacesContext facesContext = FacesContext.getCurrentInstance();
         MethodExpression me = getPaneChangeListener();
         if(me != null && event instanceof ValueChangeEvent) {
             me.invoke(facesContext.getELContext(), new Object[] {event});
         }
      }
 
 }
