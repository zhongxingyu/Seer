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
 package nl.surfnet.bod.web.user;
 
 import static nl.surfnet.bod.web.WebUtils.DELETE;
 import static nl.surfnet.bod.web.WebUtils.EDIT;
 import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
 import nl.surfnet.bod.nsi.NsiHelper;
 import nl.surfnet.bod.repo.VirtualPortRequestLinkRepo;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.VirtualPortView;
 
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

 @Controller
 @RequestMapping("/virtualports")
 public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {
 
   @Resource private VirtualPortService virtualPortService;
   @Resource private VirtualPortRequestLinkRepo virtualPortRequestLinkRepo;
   @Resource private ReservationService reservationService;
   @Resource private NsiHelper nsiHelper;
 
   @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
     VirtualPort virtualPort = virtualPortService.find(id);
 
     if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
       return "redirect:";
     }
 
     uiModel.addAttribute("virtualPort", virtualPort);
     uiModel.addAttribute("updateUserLabelCommand", new UpdateUserLabelCommand(virtualPort));
 
     return "virtualports/update";
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(final UpdateUserLabelCommand command, final BindingResult bindingResult, final Model uiModel) {
     VirtualPort virtualPort = virtualPortService.find(command.getId());
 
     if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
       return "redirect:/virtualports";
     }
 
     validateUpdateUserLabelCommand(command, bindingResult);
 
     if (bindingResult.hasErrors()) {
       uiModel.addAttribute("updateUserLabelCommand", command);
       uiModel.addAttribute("virtualPort", virtualPort);
 
       return "virtualports/update";
     }
 
     uiModel.asMap().clear();
 
     virtualPort.setUserLabel(command.getUserLabel());
     virtualPortService.update(virtualPort);
 
     return "redirect:/virtualports";
   }
 
   @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
   public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
       RedirectAttributes redirectAttributes) {
 
     VirtualPort virtualPort = virtualPortService.find(id);
 
     if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
       return "redirect:/virtualports";
     }
     virtualPortService.requestDeleteVirtualPort(Security.getUserDetails(),"VirtualPort Delete Request", virtualPort);
 
     return "redirect:/virtualports";
   }
 
   private void validateUpdateUserLabelCommand(UpdateUserLabelCommand command, Errors errors) {
     ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userLabel", "validation.not.empty");
 
     VirtualPort existingVirtualPort = virtualPortService.findByUserLabel(command.getUserLabel());
 
     if (existingVirtualPort != null && existingVirtualPort.getUserLabel().equalsIgnoreCase(command.getUserLabel())
         && !existingVirtualPort.getId().equals(command.getId())) {
       errors.rejectValue("userLabel", "validation.not.unique");
     }
   }
 
   @Override
   protected String listUrl() {
     return "virtualports/list";
   }
 
   @Override
   protected List<VirtualPort> list(int firstPage, int maxItems, Sort sort, Model model) {
     return virtualPortService.findEntriesForUser(Security.getUserDetails(), firstPage, maxItems, sort);
   }
 
   @Override
   protected long count(Model model) {
     return virtualPortService.countForUser(Security.getUserDetails());
   }
 
   @Override
   protected String getDefaultSortProperty() {
     return "userLabel";
   }
 
   @Override
   protected List<String> translateSortProperty(String sortProperty) {
     if ("physicalResourceGroup".equals(sortProperty)) {
       return ImmutableList.of("physicalPort.physicalResourceGroup.institute.name");
     }
 
     // Optional field, might be null, then sort on managerLabel which is shown
     // when no userLabel is present
     if ("userLabel".equals(sortProperty)) {
       return ImmutableList.of("userLabel", "managerLabel");
     }
 
     return super.translateSortProperty(sortProperty);
   }
 
   public static class UpdateUserLabelCommand {
     private String userLabel;
     private Long id;
     private Integer version;
 
     public UpdateUserLabelCommand() {
     }
 
     public UpdateUserLabelCommand(VirtualPort port) {
       this.userLabel = port.getUserLabel();
       this.id = port.getId();
       this.version = port.getVersion();
     }
 
     public String getUserLabel() {
       return userLabel;
     }
 
     public void setUserLabel(String userLabel) {
       this.userLabel = userLabel;
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
   }
 
   @Override
   protected AbstractFullTextSearchService<VirtualPort> getFullTextSearchableService() {
     return virtualPortService;
   }
 
   @Override
   protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
     final VirtualPortView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
     return virtualPortService.findIdsForUserUsingFilter(Security.getUserDetails(), filter, sort);
   }
 
   @Override
   protected List<? extends VirtualPortView> transformToView(List<? extends VirtualPort> entities, RichUserDetails user) {
     return Lists.transform(entities, FROM_VIRTUALPORT_TO_VIRTUALPORT_VIEW);
   }
 
   public final Function<VirtualPort, VirtualPortView> FROM_VIRTUALPORT_TO_VIRTUALPORT_VIEW =
       new Function<VirtualPort, VirtualPortView>() {
         @Override
         public VirtualPortView apply(VirtualPort port) {
           final long counter = reservationService.findAllActiveByVirtualPort(port).size();
          final long pendingDeleteRequests = virtualPortRequestLinkRepo.findByUserLabelAndStatus(port.getUserLabel(), RequestStatus.DELETE_REQUEST_PENDING).size();
           return new VirtualPortView(port, nsiHelper, Optional.<Long> of(counter + pendingDeleteRequests));
         }
       };
 
 }
