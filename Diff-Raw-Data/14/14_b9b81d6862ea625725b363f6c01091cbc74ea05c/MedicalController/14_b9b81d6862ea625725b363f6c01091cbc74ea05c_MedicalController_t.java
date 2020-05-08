 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.medical.controller;
 
 import com.abada.springframework.web.servlet.menu.MenuEntry;
 import java.util.Arrays;
 import javax.annotation.security.RolesAllowed;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 /**
  *
  * @author david
  */
 @Controller
 public class MedicalController {
 
     /**
      * js Medical load
      *
      * @param model
      * @return
      *
      */
     @RequestMapping(value = "/medical/medical.htm")
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER", "ROLE_ADMINISTRATIVE"})
     @MenuEntry(icon = "medical/image/paciente.png", menuGroup = "Medicos", order = 0, text = "Gestión Medicos")
     public String gridPatient(Model model) {
        model.addAttribute("js", Arrays.asList("medical/js/common/gridMedical.js","medical/js/common/gridMedicalExpander.js", "medical/js/medical.js",
                "manager/js/common/gridrole.js", "manager/js/common/gridgroup.js","patient/js/common/gridPatient.js",
                "patient/js/common/gridPatientExpander.js","manager/js/manager-utils.js"));
         return "dynamic/main";
 
     }
 }
