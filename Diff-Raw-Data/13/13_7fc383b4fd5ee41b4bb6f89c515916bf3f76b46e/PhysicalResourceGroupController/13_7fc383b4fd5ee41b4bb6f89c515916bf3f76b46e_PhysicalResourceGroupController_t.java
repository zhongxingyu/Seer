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
 package nl.surfnet.bod.web.manager;
 
 import static nl.surfnet.bod.web.WebUtils.*;
 
 import javax.validation.Valid;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @Controller("managerPhysicalResourceGroupController")
 @RequestMapping("/manager/physicalresourcegroups")
 public class PhysicalResourceGroupController {
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @RequestMapping(method = RequestMethod.GET)
   public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
     RichUserDetails user = Security.getUserDetails();
 
     uiModel.addAttribute("physicalResourceGroups",
         physicalResourceGroupService.findEntriesForManager(user, calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
 
     uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalResourceGroupService.countForManager(user)));
 
     return "manager/physicalresourcegroups/list";
   }
 
   @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
   public String updateForm(@RequestParam("id") final Long id, final Model model) {
     PhysicalResourceGroup group = physicalResourceGroupService.find(id);
 
     if (group == null || !Security.managerMayEdit(group)) {
       return "redirect:physicalresourcegroups";
     }
 
     model.addAttribute("updateEmailCommand", new UpdateEmailCommand(group));
     model.addAttribute("physicalResourceGroup", group);
 
     return "manager/physicalresourcegroups/update";
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid final UpdateEmailCommand command, final BindingResult result,
       final Model model, final RedirectAttributes redirectAttributes) {
 
     PhysicalResourceGroup group = physicalResourceGroupService.find(command.getId());
     if (group == null || !Security.managerMayEdit(group)) {
       return "redirect:physicalresourcegroups";
     }
 
     if (result.hasErrors()) {
       model.addAttribute("physicalResourceGroup", group);
 
       return "manager/physicalresourcegroups/update";
     }
 
     if (emailChanged(group, command)) {
      group.setManagerEmail(command.getManagerEmail());
       physicalResourceGroupService.sendAndPersistActivationRequest(group);
 
       WebUtils.addInfoMessage(redirectAttributes, "A new activation email request has been sent to '{}'",
              group.getManagerEmail());
     }
 
     return "redirect:physicalresourcegroups";
   }
 
   private boolean emailChanged(PhysicalResourceGroup group, UpdateEmailCommand command) {
     return group.getManagerEmail() == null || !group.getManagerEmail().equals(command.getManagerEmail());
   }
 
   public static final class UpdateEmailCommand {
     private Long id;
     private Integer version;
     @Email
     @NotEmpty
     private String managerEmail;
 
     public UpdateEmailCommand() {
     }
 
     public UpdateEmailCommand(PhysicalResourceGroup group) {
       this.id = group.getId();
       this.version = group.getVersion();
       this.managerEmail = group.getManagerEmail();
     }
 
     public Long getId() {
       return id;
     }
 
     public void setId(Long id) {
       this.id = id;
     }
 
     public Integer getVersion() {
       return version;
     }
 
     public void setVersion(Integer version) {
       this.version = version;
     }
 
     public String getManagerEmail() {
       return managerEmail;
     }
 
     public void setManagerEmail(String email) {
       this.managerEmail = email;
     }
 
   }
 }
