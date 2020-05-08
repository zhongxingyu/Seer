 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.bh.controller;
 
 import java.awt.event.ActionEvent;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.bh.data.IDTO;
 import org.bh.gui.View;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  *
  * @author Marco Hammel
  */
 interface IController {
 
     /**
      * return value of the Plugin Interface
      * @return
      */
     String getUniqueId();//Zu welchem gehörst du??
 
     //getAnzeigename
 
     /**
      * return the view of the component if no view is defined the method return null
      * @return
      * @throws ControllerException
      */
     View getView();
     /**
      * define the access to the translator engine of the platform
      * @param translator
      */
     ITranslator getTranslator();
     /**
      * platform can put events to the component. If the event can be handled by
      * the component it returns true otherwise false
      * @param e
      */
     void handlePlattformEvent(ActionEvent e);
     /**
      * platform can define the dto access of the component by this method
      * @param model
      */
     void setModel(IDTO model);
    //TODO datatyp result is not yet defined
     /**
      * platform can overhand the result(s) of an operation to a component by this method
      * @param result
      */
     void setResult(Map result);
    
 }
