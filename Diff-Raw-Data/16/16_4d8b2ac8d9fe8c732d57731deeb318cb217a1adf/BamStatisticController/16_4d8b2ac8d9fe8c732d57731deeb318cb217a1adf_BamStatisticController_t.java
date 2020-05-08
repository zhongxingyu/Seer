 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.bam.controller;
 
 import com.abada.springframework.web.servlet.menu.MenuEntry;
 import java.util.Arrays;
 import javax.annotation.security.RolesAllowed;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 /**
  *
  * @author katsu
  */
 @Controller
 public class BamStatisticController {
 
     @RequestMapping(value = "/bam/statistic.htm")
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     @MenuEntry(icon = "bam/image/estadistica.gif", menuGroup = "Monitorizaci&oacute;n Paciente", order = 2, text = "Estadisticas")
     public String getBam(Model model) {
         model.addAttribute("js", Arrays.asList("bam/js/statistics.js"));
         return "dynamic/main";
     }
 }
