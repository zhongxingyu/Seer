 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.rest.form;
 
 import com.abada.bpm.console.server.plugin.FormAuthorityRef;
 import com.abada.cleia.dao.PatientDao;
 import com.abada.cleia.dao.ProcessInstanceDao;
 import com.abada.cleia.entity.user.Patient;
 import com.abada.springframework.web.servlet.view.InputStreamView;
 import com.abada.web.util.URL;
 import java.io.IOException;
 import java.util.Map;
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import org.jboss.bpm.console.client.model.ProcessInstanceRef;
 import org.jboss.bpm.console.server.integration.ProcessManagement;
 import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mobile.device.Device;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.View;
 
 /**
  *
  * @author katsu
  */
 @Controller
 @RequestMapping("/rs/form/process")
 public class FormProcessController {
 
     @Autowired
     private FormDispatcherPlugin formDispatcherPlugin;
     @Autowired
     private ProcessManagement processManagement;
     @Autowired
     private PatientDao patientDao;
     @Autowired
     private ProcessInstanceDao pInstancePatientDao;
 
     /**
      * 
      * Displays a form of a process and assign for a patient. Used to display an html form to start a process instance.
      * @param definitionid Process id.
      * @param patientId Patient id to assign the started process instance.
      * @param request Do nothing.
      * @return Return the html to show in a iframe in the client interface.
      * @throws IOException 
      */
     @RolesAllowed(value={"ROLE_ADMIN","ROLE_USER"})
     @RequestMapping(value = {"/{definitionid}/patient/{patientId}/render"}, method = RequestMethod.GET)
     public View renderProcess(@PathVariable String definitionid, @PathVariable Long patientId, HttpServletRequest request,Device device) throws IOException {
         com.abada.springframework.web.servlet.menu.Device deviceAux;
         if (device.isMobile()) {
             deviceAux = com.abada.springframework.web.servlet.menu.Device.MOBILE;
         } else if (device.isNormal()) {
             deviceAux = com.abada.springframework.web.servlet.menu.Device.DESKTOP;
         } else {
             deviceAux = com.abada.springframework.web.servlet.menu.Device.TABLET;
         }
         return new InputStreamView(formDispatcherPlugin.provideForm(new FormAuthorityRef(definitionid,deviceAux.name(), FormAuthorityRef.Type.PROCESS)).getInputStream(), "text/html", null);
     }
 
     /**
      * Submit and start the process instance of the form showed in the renderProcess method.
      * @param definitionid Process id
      * @param patientId Patient id
      * @param request Do nothing.
      * @return Return success structure.
      */
     @RolesAllowed(value={"ROLE_ADMIN","ROLE_USER"})
     @RequestMapping(value = "/{definitionid}/patient/{patientId}/complete", method = RequestMethod.POST)
     public String completeProcess(@PathVariable String definitionid, @PathVariable Long patientId, HttpServletRequest request, Model model) {
         return completeProcessPriv(definitionid, patientId, request, model);
     } 
     
     private String completeProcessPriv(String definitionid, Long patientId,HttpServletRequest request, Model model) {
         try {
             Map<String, Object> params = URL.parseRequest(request);
 
             Patient patient = this.patientDao.getPatientById(patientId);
             //params.put("patient_patientids", patient.getPatientidList());
             params.put("patient_birthday", patient.getBirthDay());
             params.put("patient_genre", patient.getGenre().toString());
             params.put("patient_id", patient.getId());
            params.put("putoactor",patient.getUser().getUsername());
 
             ProcessInstanceRef pir = processManagement.newInstance(definitionid, params);            
             if (pir != null) {                                
                 this.pInstancePatientDao.addPInstancePatient(patientId, Long.parseLong(pir.getId()));
                 model.addAttribute("error", pir.getId());
                 model.addAttribute("success", Boolean.TRUE.toString());
             } else {
                 model.addAttribute("success", Boolean.FALSE.toString());
                 model.addAttribute("error", "ProcessInstance null");
             }
         } catch (Exception e) {
             model.addAttribute("success", Boolean.FALSE.toString());
             model.addAttribute("error", e.getMessage());
         }
         return "success";
     }
     
     //<editor-fold defaultstate="collapsed" desc="GWT Console">
     /**
      *
      * Displays a form of a process. Used to display an html form to start a process instance.
      * @param definitionid Process id.
      * @param request Do nothing.
      * @return Return the html to show in a iframe in the client interface.
      * @throws IOException
      */
     @RolesAllowed(value={"ROLE_ADMIN","ROLE_USER"})
     @RequestMapping(value = "/{definitionid}/render", method = RequestMethod.GET)
     public View renderProcess(@PathVariable String definitionid, HttpServletRequest request,Device device) throws IOException {
         com.abada.springframework.web.servlet.menu.Device deviceAux;
         if (device.isMobile()) {
             deviceAux = com.abada.springframework.web.servlet.menu.Device.MOBILE;
         } else if (device.isNormal()) {
             deviceAux = com.abada.springframework.web.servlet.menu.Device.DESKTOP;
         } else {
             deviceAux = com.abada.springframework.web.servlet.menu.Device.TABLET;
         }
         return new InputStreamView(formDispatcherPlugin.provideForm(new FormAuthorityRef(definitionid,deviceAux.name(), FormAuthorityRef.Type.PROCESS)).getInputStream(), "text/html", null);
     }
 
     /**
      * Submit and start the process instance of the form showed in the renderProcess method.
      * @param definitionid Process id
      * @param request Do nothing.
      * @param model Do nothing.
      * @return Return success structure.
      */
     @RolesAllowed(value={"ROLE_ADMIN","ROLE_USER"})
     @RequestMapping(value = "/{definitionid}/complete", method = RequestMethod.POST)
     /*public ProcessInstanceRef completeProcess(@PathVariable String definitionid, HttpServletRequest request) throws IOException {
      * return processManagement.newInstance(definitionid, URL.parseRequest(request));
      * }*/
     public String completeProcess(@PathVariable String definitionid, HttpServletRequest request, Model model) {
         try {
             ProcessInstanceRef pir = processManagement.newInstance(definitionid, URL.parseRequest(request));
             model.addAttribute("error", pir.getId());
             model.addAttribute("success", Boolean.TRUE.toString());
         } catch (Exception e) {
             model.addAttribute("success", Boolean.FALSE.toString());
             model.addAttribute("error", e.getMessage());
         }
         return "success";
     }
     
     //</editor-fold>
 }
