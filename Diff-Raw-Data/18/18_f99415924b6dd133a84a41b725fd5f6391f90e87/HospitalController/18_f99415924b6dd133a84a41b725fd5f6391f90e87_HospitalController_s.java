 package at.ac.tuwien.dse.fairsurgeries.web;
 
import at.ac.tuwien.dse.fairsurgeries.domain.Hospital;
 import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @RequestMapping("/hospitals")
 @Controller
 @RooWebScaffold(path = "hospitals", formBackingObject = Hospital.class)
 public class HospitalController {
 }
