 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.base.MessageManager;
 import nl.surfnet.bod.web.base.MessageRetriever;
 import nl.surfnet.bod.web.manager.ActivationEmailController;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.base.Optional;
 
 @Controller
 @RequestMapping("/switchrole")
 public class SwitchRoleController {
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
   @Resource
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Resource(name = "bodEnvironment")
   private Environment environment;
 
   @Resource
   private MessageManager messageManager;

   @Resource
   private MessageRetriever messageRetriever;
 
   @RequestMapping(method = RequestMethod.POST)
   public String switchRole(final String roleId, final Model model, final RedirectAttributes redirectAttribs) {
     RichUserDetails userDetails = Security.getUserDetails();
 
     if (StringUtils.hasText(roleId)) {
      try {
        userDetails.switchToRoleById(Long.valueOf(roleId));
      } catch (Exception e) {
        logger.warn("Error switching to role {}: {}", roleId, e.toString());
        throw e;
      }
     }
 
     return determineViewNameAndAddAttributes(userDetails.getSelectedRole(), redirectAttribs);
   }
 
   @RequestMapping(method = RequestMethod.GET)
   public String wrongSwitchRoleRequest(HttpServletRequest request, RedirectAttributes redirectAttributes) {
     logger.error("Could not process get request for switch role: {}", request);
 
     messageManager.addErrorFlashMessage(redirectAttributes, "error_post_when_shibboleth_timeout");
 
     return "redirect:/";
   }
 
   @RequestMapping(value = "logout", method = RequestMethod.GET)
   public String logout(HttpServletRequest request) {
     logger.info("Logging out user: {}", Security.getUserDetails().getUsername());
     request.getSession().invalidate();
 
     return "redirect:" + environment.getShibbolethLogoutUrl();
   }
 
   private String determineViewNameAndAddAttributes(BodRole selectedRole, RedirectAttributes redirectAttribs) {
     Optional<Long> groupId = WebUtils.getSelectedPhysicalResourceGroupId();
 
     if (groupId.isPresent()) {
       PhysicalResourceGroup group = physicalResourceGroupService.find(groupId.get());
 
       if (!group.isActive()) {
         String successMessage = messageRetriever.getMessageWithBoldArguments("info_activation_request_send", group
             .getName(), group.getManagerEmail());
         String newLinkButton = createNewActivationLinkForm(environment.getExternalBodUrl()
             + ActivationEmailController.ACTIVATION_MANAGER_PATH, group.getId().toString(), successMessage);
 
         messageManager.addInfoFlashMessage(newLinkButton, redirectAttribs, "info_physicalresourcegroup_not_activated");
 
         return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
       }
     }
     return selectedRole.getRole().getViewName();
   }
 
   String createNewActivationLinkForm(Object... args) {
     return String.format(
         "<a href=\"%s?id=%s\" class=\"btn btn-primary\" data-form=\"true\" data-success=\"%s\">Resend email</a>", args);
   }
 }
