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
 
 import static nl.surfnet.bod.web.WebUtils.DELETE;
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 import static nl.surfnet.bod.web.WebUtils.LIST;
 import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
 import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static nl.surfnet.bod.web.WebUtils.UPDATE;
 import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
 import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.base.AbstractSortableListController;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
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
 import com.google.common.collect.Iterables;
 
 @Controller
 @RequestMapping("/noc/" + PhysicalPortController.PAGE_URL)
 public class PhysicalPortController extends AbstractSortableListController<PhysicalPort> {
 
   static final String PAGE_URL = "physicalports";
   static final String MODEL_KEY = "createPhysicalPortCommand";
 
   @Autowired
   private PhysicalPortService physicalPortService;
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Autowired
   private MessageSource messageSource;
 
   @RequestMapping(value = "add", method = RequestMethod.GET)
   public String addPhysicalPortForm(@RequestParam(value = "prg") Long prgId, Model model) {
     PhysicalResourceGroup prg = physicalResourceGroupService.find(prgId);
     if (prg == null) {
       return "redirect:/";
     }
 
     Collection<PhysicalPort> unallocatedPorts = physicalPortService.findUnallocated();
 
     AddPhysicalPortCommand addCommand = new AddPhysicalPortCommand();
     addCommand.setPhysicalResourceGroup(prg);
     PhysicalPort port = Iterables.get(unallocatedPorts, 0);
     
     addCommand.setNetworkElementPk(port.getNetworkElementPk());
     addCommand.setNocLabel(port.getNocLabel());
     addCommand.setManagerLabel(port.hasManagerLabel() ? port.getManagerLabel() : "");
     addCommand.setPortId(port.getPortId());
 
     model.addAttribute("addPhysicalPortCommand", addCommand);
     model.addAttribute("unallocatedPhysicalPorts", unallocatedPorts);
 
     return "physicalports/addPhysicalPort";
   }
 
   @RequestMapping(value = "add", method = RequestMethod.POST)
   public String addPhysicalPort(@Valid AddPhysicalPortCommand addCommand, BindingResult result,
       RedirectAttributes redirectAttributes, Model model) {
 
     if (result.hasErrors()) {
       model.addAttribute("addPhysicalPortCommand", addCommand);
       model.addAttribute("unallocatedPhysicalPorts", physicalPortService.findUnallocated());
       return "physicalports/addPhysicalPort";
     }
 
     PhysicalPort port = physicalPortService.findByNetworkElementPk(addCommand.getNetworkElementPk());
     if (!Strings.isNullOrEmpty(addCommand.getManagerLabel())) {
       port.setManagerLabel(addCommand.getManagerLabel());
     }
     port.setNocLabel(addCommand.getNocLabel());
     port.setPhysicalResourceGroup(addCommand.getPhysicalResourceGroup());
    port.setManagerLabel(addCommand.getManagerLabel());
     port.setPortId(addCommand.getPortId());
 
     physicalPortService.save(port);
 
     WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_physicalport_added", port.getNocLabel(),
         port.getPhysicalResourceGroup().getName());
 
     return "redirect:/noc/physicalresourcegroups";
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid CreatePhysicalPortCommand command, BindingResult result, Model model,
       final RedirectAttributes redirectAttributes) {
     
     if (result.hasErrors()) {
       model.addAttribute(MODEL_KEY, command);
       return PAGE_URL + UPDATE;
     }
 
     PhysicalPort portToSave = physicalPortService.findByNetworkElementPk(command.getNetworkElementPk());
     portToSave.setPhysicalResourceGroup(command.getPhysicalResourceGroup());
     portToSave.setNocLabel(command.getNocLabel());
     portToSave.setPortId(command.getPortId());
     if (Strings.emptyToNull(command.getManagerLabel()) != null) {
       portToSave.setManagerLabel(command.getManagerLabel());
     }
 
     physicalPortService.save(portToSave);
 
     model.asMap().clear();
 
     WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_physicalport_updated", portToSave.getNocLabel(),
         portToSave.getPhysicalResourceGroup().getName());
 
     return "redirect:physicalports";
   }
 
   @RequestMapping(value = "/free", method = RequestMethod.GET)
   public String listUnallocated(@RequestParam(value = PAGE_KEY, required = false) final Integer page,
       final Model uiModel) {
     uiModel.addAttribute("list",
         physicalPortService.findUnallocatedEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
 
     uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnallocated()));
 
     return PAGE_URL + "/listunallocated";
   }
 
   @RequestMapping(value = "edit", params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final String networkElementPk, final Model uiModel) {
     PhysicalPort port;
     try {
       port = physicalPortService.findByNetworkElementPk(networkElementPk);
     }
     catch (IllegalStateException e) {
       return "redirect:";
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
 
   @Override
   protected String listUrl() {
     return PAGE_URL + LIST;
   }
 
   @Override
   protected List<PhysicalPort> list(int firstPage, int maxItems, Sort sort, Model model) {
     return physicalPortService.findAllocatedEntries(firstPage, maxItems, sort);
   }
 
   @Override
   protected long count() {
     return physicalPortService.countAllocated();
   }
 
   @Override
   protected String defaultSortProperty() {
     return "nocLabel";
   }
 
   public static final class AddPhysicalPortCommand {
     @NotNull
     private PhysicalResourceGroup physicalResourceGroup;
     @NotEmpty
     private String networkElementPk;
     @NotEmpty
     private String nocLabel;
     private String managerLabel;
     
     @NotEmpty
     private String portId;
 
     public AddPhysicalPortCommand() {
     }
 
     public PhysicalResourceGroup getPhysicalResourceGroup() {
       return physicalResourceGroup;
     }
 
     public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
       this.physicalResourceGroup = physicalResourceGroup;
     }
 
     public String getNetworkElementPk() {
       return networkElementPk;
     }
 
     public void setNetworkElementPk(String networkElementPk) {
       this.networkElementPk = networkElementPk;
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
 
     public String getPortId() {
       return portId;
     }
 
     public void setPortId(String portId) {
       this.portId = portId;
     }
 
     @Override
     public String toString() {
       StringBuilder builder = new StringBuilder();
       builder.append("AddPhysicalPortCommand [physicalResourceGroup=");
       builder.append(physicalResourceGroup);
       builder.append(", networkElementPk=");
       builder.append(networkElementPk);
       builder.append(", nocLabel=");
       builder.append(nocLabel);
       builder.append(", managerLabel=");
       builder.append(managerLabel);
       builder.append(", portId=");
       builder.append(portId);
       builder.append("]");
       return builder.toString();
     }
   }
 
   public static final class CreatePhysicalPortCommand {
     private String networkElementPk;
     @NotNull
     private PhysicalResourceGroup physicalResourceGroup;
     @NotEmpty
     private String nocLabel;
     private String managerLabel;
     private Integer version;
     
     @NotEmpty
     private String portId;
 
     public CreatePhysicalPortCommand() {
     }
 
     public CreatePhysicalPortCommand(PhysicalPort port) {
       this.networkElementPk = port.getNetworkElementPk();
       this.physicalResourceGroup = port.getPhysicalResourceGroup();
       this.nocLabel = port.getNocLabel();
       this.managerLabel = port.hasManagerLabel() ? port.getManagerLabel() : "";
       this.version = port.getVersion();
       this.portId = port.getPortId();
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
 
     public String getPortId() {
       return portId;
     }
 
     public void setPortId(String portId) {
       this.portId = portId;
     }
 
     @Override
     public String toString() {
       StringBuilder builder = new StringBuilder();
       builder.append("CreatePhysicalPortCommand [networkElementPk=");
       builder.append(networkElementPk);
       builder.append(", physicalResourceGroup=");
       builder.append(physicalResourceGroup);
       builder.append(", nocLabel=");
       builder.append(nocLabel);
       builder.append(", managerLabel=");
       builder.append(managerLabel);
       builder.append(", version=");
       builder.append(version);
       builder.append(", portId=");
       builder.append(portId);
       builder.append("]");
       return builder.toString();
     }
 
   }
 
 }
