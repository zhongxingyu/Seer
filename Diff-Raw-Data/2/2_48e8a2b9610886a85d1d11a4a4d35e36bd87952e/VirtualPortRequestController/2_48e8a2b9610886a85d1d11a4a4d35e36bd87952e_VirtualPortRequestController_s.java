 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.web;
 
 import java.util.Collection;
 
 import javax.validation.Valid;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.service.EmailSender;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 import nl.surfnet.bod.web.security.Security;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @Controller
 @RequestMapping("/virtualports/request")
 public class VirtualPortRequestController {
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
   @Autowired
   private VirtualResourceGroupService virtualResourceGroupService;
 
   @Autowired
   private EmailSender emailSender;
 
   @RequestMapping(method = RequestMethod.GET)
   public String requestList(Model model) {
     Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllWithPorts();
 
     model.addAttribute("physicalResourceGroups", groups);
 
     return "virtualports/request";
   }
 
   @RequestMapping(params = "id", method = RequestMethod.GET)
   public String requestForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
     PhysicalResourceGroup group = physicalResourceGroupService.find(id);
 
     if (group == null || !group.isActive()) {
       WebUtils.addInfoMessage(redirectAttributes, "A invalid Physical Resource Group was selected.");
 
       return "redirect:/virtualports/request";
     }
 
     model.addAttribute("requestCommand", new RequestCommand(group));
     model.addAttribute("physicalResourceGroup", group);
     model.addAttribute("user", Security.getUserDetails());
 
     return "virtualports/requestform";
   }
 
   @ModelAttribute("virtualResourceGroups")
   public Collection<VirtualResourceGroup> populateVirtualResourceGroups() {
     return virtualResourceGroupService.findAllForUser(Security.getUserDetails());
   }
 
   @RequestMapping(method = RequestMethod.POST)
   public String request(@Valid RequestCommand requestCommand, BindingResult result, Model model,
       RedirectAttributes redirectAttributes) {
     PhysicalResourceGroup pGroup = physicalResourceGroupService.find(requestCommand.getPhysicalResourceGroupId());
     VirtualResourceGroup vGroup = virtualResourceGroupService.find(requestCommand.getVirtualResourceGroupId());
 
     if (pGroup == null || !pGroup.isActive() || vGroup == null || !Security.isUserMemberOf(vGroup)) {
       return "redirect:/virtualports/request";
     }
 
     if (result.hasErrors()) {
       model.addAttribute("user", Security.getUserDetails());
       model.addAttribute("physicalResourceGroup", pGroup);
 
       return "virtualports/requestform";
     }
 
     emailSender.sendVirtualPortRequestMail(Security.getUserDetails(), pGroup, vGroup, requestCommand.getMessage());
 
    WebUtils.addInfoMessage(redirectAttributes, "Your request for a new Virutal Port %s has been send.", pGroup
         .getInstitute().getName());
 
     return "redirect:/";
   }
 
   public static class RequestCommand {
     @NotEmpty
     private String message;
     private Long physicalResourceGroupId;
     private Long virtualResourceGroupId;
 
     public RequestCommand() {
     }
 
     public RequestCommand(PhysicalResourceGroup group) {
       physicalResourceGroupId = group.getId();
     }
 
     public String getMessage() {
       return message;
     }
 
     public void setMessage(String message) {
       this.message = message;
     }
 
     public Long getPhysicalResourceGroupId() {
       return physicalResourceGroupId;
     }
 
     public void setPhysicalResourceGroupId(Long groupId) {
       this.physicalResourceGroupId = groupId;
     }
 
     public Long getVirtualResourceGroupId() {
       return virtualResourceGroupId;
     }
 
     public void setVirtualResourceGroupId(Long virtualResourceGroupId) {
       this.virtualResourceGroupId = virtualResourceGroupId;
     }
   }
 }
