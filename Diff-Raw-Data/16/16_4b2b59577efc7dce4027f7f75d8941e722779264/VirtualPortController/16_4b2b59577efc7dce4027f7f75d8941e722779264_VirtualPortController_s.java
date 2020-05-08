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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import nl.surfnet.bod.domain.*;
 import nl.surfnet.bod.domain.validator.VirtualPortValidator;
 import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.web.AbstractSortableListController;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortView;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.ReservationView;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.hibernate.validator.constraints.Range;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 @Controller("managerVirtualPortController")
 @RequestMapping(VirtualPortController.PAGE_URL)
 public class VirtualPortController extends AbstractSortableListController<VirtualPortView> {
 
   public static final String MODEL_KEY = "virtualPort";
   public static final String PAGE_URL = "/manager/virtualports";
 
  private static final Function<VirtualPort, VirtualPortView> TO_VIRTUAL_PORT_VIEW =
       new Function<VirtualPort, VirtualPortView>() {
         @Override
         public VirtualPortView apply(VirtualPort port) {
           return new VirtualPortView(port);
         }
       };
 
   @Autowired
   private VirtualPortService virtualPortService;
 
   @Autowired
   private InstituteService instituteService;
 
   @Autowired
   private VirtualPortValidator virtualPortValidator;
 
   @Autowired
   private ReservationService reservationService;
 
   @Autowired
   private MessageSource messageSource;
 
   @RequestMapping(method = RequestMethod.POST)
   public String create(@Valid VirtualPort virtualPort, BindingResult result, Model model,
       RedirectAttributes redirectAttributes) {
 
     virtualPortValidator.validate(virtualPort, result);
 
     if (result.hasErrors()) {
       instituteService.fillInstituteForPhysicalResourceGroup(virtualPort.getPhysicalResourceGroup());
 
       model.addAttribute(MODEL_KEY, virtualPort);
       model.addAttribute("physicalPorts", virtualPort.getPhysicalResourceGroup() == null ? Collections.emptyList()
           : virtualPort.getPhysicalResourceGroup().getPhysicalPorts());
       model.addAttribute("virtualResourceGroups", ImmutableList.of(virtualPort.getVirtualResourceGroup()));
       model.addAttribute("physicalResourceGroups", ImmutableList.of(virtualPort.getPhysicalResourceGroup()));
 
       return PAGE_URL + CREATE;
     }
 
     model.asMap().clear();
 
     WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualport_created",
         virtualPort.getManagerLabel());
 
     virtualPortService.save(virtualPort);
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = "/create/{uuid}", method = RequestMethod.GET)
   public String createForm(@PathVariable("uuid") String link, Model model, RedirectAttributes redirectAttributes) {
     VirtualPortRequestLink requestLink = virtualPortService.findRequest(link);
 
     if (requestLink == null) {
       WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualportrequestlink_notvalid");
       return "redirect:/manager/virtualports";
     }
 
     if (!Security.isManagerMemberOf(requestLink.getPhysicalResourceGroup())) {
       WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualportrequestlink_notmanager");
       return "redirect:/manager/virtualports";
     }
 
     instituteService.fillInstituteForPhysicalResourceGroup(requestLink.getPhysicalResourceGroup());
 
     VirtualPort virtualPort = new VirtualPort();
     virtualPort.setVirtualResourceGroup(requestLink.getVirtualResourceGroup());
     virtualPort.setMaxBandwidth(requestLink.getMinBandwidth());
     virtualPort.setPhysicalPort(Iterables.get(requestLink.getPhysicalResourceGroup().getPhysicalPorts(), 0));
 
     model.addAttribute(MODEL_KEY, virtualPort);
     model.addAttribute("physicalPorts", requestLink.getPhysicalResourceGroup().getPhysicalPorts());
     model.addAttribute("virtualResourceGroups", ImmutableList.of(requestLink.getVirtualResourceGroup()));
     model.addAttribute("physicalResourceGroups", ImmutableList.of(requestLink.getPhysicalResourceGroup()));
 
     return PAGE_URL + CREATE;
   }
 
   @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
   public String show(@RequestParam(ID_KEY) final Long id, final Model model) {
     model.addAttribute(MODEL_KEY, TO_VIRTUAL_PORT_VIEW.apply(virtualPortService.find(id)));
 
     return PAGE_URL + SHOW;
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid VirtualPortUpdateCommand command, BindingResult result, Model model,
       RedirectAttributes redirectAttributes) {
 
     VirtualPort port = virtualPortService.find(command.getId());
 
     if (port == null || Security.managerMayNotEdit(port)) {
       return "redirect:" + PAGE_URL;
     }
 
     port.setManagerLabel(command.getManagerLabel());
     port.setVlanId(command.getVlanId());
     port.setMaxBandwidth(command.getMaxBandwidth());
     port.setPhysicalPort(command.getPhysicalPort());
     port.setVirtualResourceGroup(command.getVirtualResourceGroup());
 
     virtualPortValidator.validate(port, result);
 
     if (result.hasErrors()) {
      System.err.println(result);
       model.addAttribute("virtualPortUpdateCommand", command);
       return PAGE_URL + UPDATE;
     }
 
     model.asMap().clear();
     WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualport_updated", port.getManagerLabel());
 
     virtualPortService.update(port);
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final Long id, final Model model) {
     VirtualPort virtualPort = virtualPortService.find(id);
 
     if (virtualPort == null || Security.managerMayNotEdit(virtualPort)) {
       return "redirect:" + PAGE_URL;
     }
 
     model.addAttribute("virtualPortUpdateCommand", new VirtualPortUpdateCommand(virtualPort));
     model.addAttribute("physicalPorts", virtualPort.getPhysicalResourceGroup().getPhysicalPorts());
 
     return PAGE_URL + UPDATE;
   }
 
   @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
   public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
       RedirectAttributes redirectAttributes) {
 
     VirtualPort virtualPort = virtualPortService.find(id);
 
     if (virtualPort == null || Security.managerMayNotEdit(virtualPort)) {
       return "redirect:" + PAGE_URL;
     }
 
     virtualPortService.delete(virtualPort);
 
     WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualport_deleted",
         virtualPort.getManagerLabel());
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = "{portId}/reservations", method = RequestMethod.GET, produces = "application/json")
   @ResponseBody
   public Collection<ReservationView> listReservationsForPort(@PathVariable Long portId) {
     VirtualPort port = virtualPortService.find(portId);
 
     if (port == null || Security.managerMayNotEdit(port)) {
       return Collections.emptyList();
     }
 
     Collection<Reservation> reservations = reservationService.findByVirtualPort(port);
 
     return Collections2.transform(reservations, new Function<Reservation, ReservationView>() {
       @Override
       public ReservationView apply(Reservation reservation) {
         return new ReservationView(reservation);
       }
     });
   }
 
   @Override
   protected String listUrl() {
     return PAGE_URL + LIST;
   }
 
   @Override
   protected List<VirtualPortView> list(int firstPage, int maxItems, Sort sort, Model model) {
     return Lists.transform(
         virtualPortService.findEntriesForManager(Security.getUserDetails(), firstPage, maxItems, sort),
         TO_VIRTUAL_PORT_VIEW);
   }
 
   @Override
   protected long count() {
     return virtualPortService.countForManager(Security.getUserDetails());
   }
 
   @Override
   protected String defaultSortProperty() {
     return "managerLabel";
   }
 
   @Override
   protected List<String> translateSortProperty(String sortProperty) {
     if (sortProperty.equals("physicalResourceGroup")) {
       return ImmutableList.of("physicalPort.physicalResourceGroup");
     }
 
     return super.translateSortProperty(sortProperty);
   }
 
   public static final class VirtualPortUpdateCommand {
     private Long id;
     private Integer version;
     @NotEmpty
     private String managerLabel;
     @NotNull
     private Integer maxBandwidth;
     @Range(min = 1, max = 4095)
     private Integer vlanId;
     @NotNull
     private PhysicalPort physicalPort;
     @NotNull
     private VirtualResourceGroup virtualResourceGroup;
     @NotNull
     private PhysicalResourceGroup physicalResourceGroup;
 
     public VirtualPortUpdateCommand() {
     }
 
     public VirtualPortUpdateCommand(VirtualPort port) {
       this.id = port.getId();
       this.version = port.getVersion();
       this.managerLabel = port.getManagerLabel();
       this.maxBandwidth = port.getMaxBandwidth();
       this.vlanId = port.getVlanId();
       this.physicalResourceGroup = port.getPhysicalResourceGroup();
       this.physicalPort = port.getPhysicalPort();
       this.virtualResourceGroup = port.getVirtualResourceGroup();
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
 
     public String getManagerLabel() {
       return managerLabel;
     }
 
     public void setManagerLabel(String managerLabel) {
       this.managerLabel = managerLabel;
     }
 
     public Integer getMaxBandwidth() {
       return maxBandwidth;
     }
 
     public void setMaxBandwidth(Integer maxBandwidth) {
       this.maxBandwidth = maxBandwidth;
     }
 
     public Integer getVlanId() {
       return vlanId;
     }
 
     public void setVlanId(Integer vlanId) {
       this.vlanId = vlanId;
     }
 
     public PhysicalPort getPhysicalPort() {
       return physicalPort;
     }
 
     public void setPhysicalPort(PhysicalPort physicalPort) {
       this.physicalPort = physicalPort;
     }
 
     public VirtualResourceGroup getVirtualResourceGroup() {
       return virtualResourceGroup;
     }
 
     public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
       this.virtualResourceGroup = virtualResourceGroup;
     }
 
     public PhysicalResourceGroup getPhysicalResourceGroup() {
       return physicalResourceGroup;
     }
 
     public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
       this.physicalResourceGroup = physicalResourceGroup;
     }
   }
 
   public static final class VirtualPortView {
     private final Long id;
     private final String managerLabel;
     private final Integer maxBandwidth;
     private final Integer vlanId;
     private final String virtualResourceGroup;
     private final String physicalResourceGroup;
     private final String physicalPort;
     private final String userLabel;
 
     public VirtualPortView(VirtualPort port) {
       id = port.getId();
       managerLabel = port.getManagerLabel();
       userLabel = port.getUserLabel();
       maxBandwidth = port.getMaxBandwidth();
       vlanId = port.getVlanId();
       virtualResourceGroup = port.getVirtualResourceGroup().getName();
       physicalResourceGroup = port.getPhysicalResourceGroup().getName();
       physicalPort = port.getPhysicalPort().getManagerLabel();
     }
 
     public String getManagerLabel() {
       return managerLabel;
     }
 
     public Integer getMaxBandwidth() {
       return maxBandwidth;
     }
 
     public Integer getVlanId() {
       return vlanId;
     }
 
     public String getVirtualResourceGroup() {
       return virtualResourceGroup;
     }
 
     public String getPhysicalResourceGroup() {
       return physicalResourceGroup;
     }
 
     public String getPhysicalPort() {
       return physicalPort;
     }
 
     public Long getId() {
       return id;
     }
 
     public String getUserLabel() {
       return userLabel;
     }
 
   }
 }
