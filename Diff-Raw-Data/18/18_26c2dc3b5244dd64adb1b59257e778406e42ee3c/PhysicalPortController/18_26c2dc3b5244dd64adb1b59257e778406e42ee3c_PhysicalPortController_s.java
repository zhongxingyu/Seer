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
 package nl.surfnet.bod.web.noc;
 
 import static nl.surfnet.bod.web.WebUtils.*;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.web.AbstractSortableListController;
 import nl.surfnet.bod.web.WebUtils;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.base.Strings;
 
 @Controller
 @RequestMapping("/noc/" + PhysicalPortController.PAGE_URL)
 public class PhysicalPortController extends AbstractSortableListController<PhysicalPort> {
 
   static final String PAGE_URL = "physicalports";
   static final String MODEL_KEY = "createPhysicalPortCommand";
 
   @Autowired
   private PhysicalPortService physicalPortService;
 
   @Autowired
   private VirtualPortService virutalPortService;
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
 
  @RequestMapping(value = "/free", method = RequestMethod.PUT)
   public String update(@Valid CreatePhysicalPortCommand command, final BindingResult result, final Model model,
       final RedirectAttributes redirectAttributes) {
 
     if (result.hasErrors()) {
       model.addAttribute(MODEL_KEY, command);
       return PAGE_URL + UPDATE;
     }
 
     PhysicalPort portToSave = physicalPortService.findByNetworkElementPk(command.getNetworkElementPk());
     portToSave.setPhysicalResourceGroup(command.getPhysicalResourceGroup());
     portToSave.setNocLabel(command.getNocLabel());
     if (Strings.emptyToNull(command.getManagerLabel()) != null) {
       portToSave.setManagerLabel(command.getManagerLabel());
     }
 
     physicalPortService.save(portToSave);
 
     model.asMap().clear();
 
     WebUtils.addInfoMessage(redirectAttributes, "Physical Port %s was assigned to %s", portToSave.getNocLabel(),
         portToSave.getPhysicalResourceGroup().getInstitute().getName());
 
    return "redirect:free";
   }
 
   @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
   public String show(@RequestParam(ID_KEY) final String networkElementPk, final Model model) {
     PhysicalPort physicalPort = physicalPortService.findByNetworkElementPk(networkElementPk);
 
     Collection<VirtualPort> virutalPorts = physicalPort != null && physicalPort.isAllocated() ? virutalPortService
         .findAllForPhysicalPort(physicalPort) : Collections.<VirtualPort> emptyList();
 
     model.addAttribute("physicalPort", physicalPort);
     model.addAttribute("virtualPorts", virutalPorts);
 
     return PAGE_URL + SHOW;
   }
 
   @RequestMapping(value = "/free", method = RequestMethod.GET)
   public String listUnallocated(@RequestParam(value = PAGE_KEY, required = false) final Integer page,
       final Model uiModel) {
     uiModel.addAttribute("list",
         physicalPortService.findUnallocatedEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
 
     uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnallocated()));
 
     return PAGE_URL + "/listunallocated";
   }
 
  @RequestMapping(value = "free/edit", params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final String networkElementPk, final Model uiModel) {
     PhysicalPort port;
     try {
       port = physicalPortService.findByNetworkElementPk(networkElementPk);
     }
     catch (IllegalStateException e) {
      return "redirect:free";
     }
 
     uiModel.addAttribute(MODEL_KEY, new CreatePhysicalPortCommand(port));
 
     return PAGE_URL + UPDATE;
   }
 
   @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
   public String delete(@RequestParam(ID_KEY) final String networkElementPk,
       @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
 
     PhysicalPort physicalPort = physicalPortService.findByNetworkElementPk(networkElementPk);
     physicalPortService.delete(physicalPort);
 
     uiModel.asMap().clear();
     uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());
 
     return "redirect:";
   }
 
   /**
    * Puts all {@link PhysicalResourceGroup}s on the model, needed to relate a
    * group to a {@link PhysicalPort}.
    *
    * @return Collection<PhysicalResourceGroup>
    */
   @ModelAttribute(PhysicalResourceGroupController.MODEL_KEY_LIST)
   public Collection<PhysicalResourceGroup> populatePhysicalResourceGroups() {
     return physicalResourceGroupService.findAll();
   }
 
   public static final class CreatePhysicalPortCommand {
     private String networkElementPk;
     @NotNull
     private PhysicalResourceGroup physicalResourceGroup;
     @NotEmpty
     private String nocLabel;
     private String managerLabel;
     private Integer version;
 
     public CreatePhysicalPortCommand() {
     }
 
     public CreatePhysicalPortCommand(PhysicalPort port) {
       this.networkElementPk = port.getNetworkElementPk();
       this.physicalResourceGroup = port.getPhysicalResourceGroup();
       this.nocLabel = port.getNocLabel();
       this.managerLabel = "";
       this.version = port.getVersion();
     }
 
     public String getNetworkElementPk() {
       return networkElementPk;
     }
 
     public void setNetworkElementPk(String networkElementPk) {
       this.networkElementPk = networkElementPk;
     }
 
     public PhysicalResourceGroup getPhysicalResourceGroup() {
       return physicalResourceGroup;
     }
 
     public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
       this.physicalResourceGroup = physicalResourceGroup;
     }
 
     public String getNocLabel() {
       return nocLabel;
     }
 
     public void setNocLabel(String nocLabel) {
       this.nocLabel = nocLabel;
     }
 
     public String getManagerLabel() {
       return managerLabel;
     }
 
     public void setManagerLabel(String managerLabel) {
       this.managerLabel = managerLabel;
     }
 
     public Integer getVersion() {
       return version;
     }
 
     public void setVersion(Integer version) {
       this.version = version;
     }
 
   }
 
   @Override
   protected String listUrl() {
     return PAGE_URL + LIST;
   }
 
   @Override
   protected List<PhysicalPort> list(int firstPage, int maxItems, Sort sort) {
     return physicalPortService.findAllocatedEntries(firstPage, maxItems, sort);
   }
 
   @Override
   protected long count() {
     return physicalPortService.countAllocated();
   }
 }
