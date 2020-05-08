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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.NocService;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.util.Functions;
 import nl.surfnet.bod.util.ReflectiveFieldComparator;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.PhysicalPortView;
 
import org.apache.lucene.queryParser.ParseException;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.context.MessageSource;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
 
 import static nl.surfnet.bod.web.WebUtils.DELETE;
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 import static nl.surfnet.bod.web.WebUtils.LIST;
 import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
 import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static nl.surfnet.bod.web.WebUtils.UPDATE;
 import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
 import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;
 
 @Controller
 @RequestMapping("/noc/" + PhysicalPortController.PAGE_URL)
 public class PhysicalPortController extends AbstractSearchableSortableListController<PhysicalPortView, PhysicalPort> {
 
   public static final String PAGE_URL = "physicalports";
   public static final String PAGE_UNALIGNED_URL = "/noc/" + PAGE_URL + "/unaligned";
   static final String MODEL_KEY = "createPhysicalPortCommand";
 
   @Resource
   private PhysicalPortService physicalPortService;
 
   @Resource
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Resource
   private VirtualPortService virtualPortService;
 
   @Resource
   private ReservationService reservationService;
 
   @Resource
   private NocService nocService;
 
   @Resource
   private MessageSource messageSource;
 
   @RequestMapping(value = "add", method = RequestMethod.GET)
   public String addPhysicalPortForm(@RequestParam(value = "prg") Long prgId, Model model,
       RedirectAttributes redirectAttrs) {
     PhysicalResourceGroup prg = physicalResourceGroupService.find(prgId);
     if (prg == null) {
       return "redirect:/";
     }
 
     Collection<PhysicalPort> unallocatedPorts = physicalPortService.findUnallocated();
 
     if (unallocatedPorts.isEmpty()) {
       WebUtils.addInfoMessage(redirectAttrs, messageSource, "info_physicalport_nounallocated");
       return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
     }
 
     AddPhysicalPortCommand addCommand = new AddPhysicalPortCommand();
     addCommand.setPhysicalResourceGroup(prg);
     PhysicalPort port = Iterables.get(unallocatedPorts, 0);
 
     addCommand.setNmsPortId(port.getNmsPortId());
     addCommand.setNocLabel(port.getNocLabel());
     addCommand.setManagerLabel(port.hasManagerLabel() ? port.getManagerLabel() : "");
     addCommand.setBodPortId(port.getBodPortId());
 
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
 
     PhysicalPort port = physicalPortService.findByNmsPortId(addCommand.getNmsPortId());
     if (!Strings.isNullOrEmpty(addCommand.getManagerLabel())) {
       port.setManagerLabel(addCommand.getManagerLabel());
     }
     port.setNocLabel(addCommand.getNocLabel());
     port.setPhysicalResourceGroup(addCommand.getPhysicalResourceGroup());
     if (addCommand.getManagerLabel() == null || addCommand.getManagerLabel().isEmpty()) {
       port.setManagerLabel(null);
     }
     else {
       port.setManagerLabel(addCommand.getManagerLabel());
     }
     port.setBodPortId(addCommand.getBodPortId());
 
     physicalPortService.save(port);
 
     WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_physicalport_added", port.getNocLabel(), port
         .getPhysicalResourceGroup().getName());
 
     return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid CreatePhysicalPortCommand command, BindingResult result, Model model,
       final RedirectAttributes redirectAttributes) {
 
     if (result.hasErrors()) {
       model.addAttribute(MODEL_KEY, command);
       return PAGE_URL + UPDATE;
     }
 
     PhysicalPort portToSave = physicalPortService.findByNmsPortId(command.getNmsPortId());
     portToSave.setPhysicalResourceGroup(command.getPhysicalResourceGroup());
     portToSave.setNocLabel(command.getNocLabel());
     portToSave.setBodPortId(command.getBodPortId());
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
   public String listUnallocated(@RequestParam(value = PAGE_KEY, required = false) Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, Model model) {
 
     final List<PhysicalPortView> transformedUnallocatedPhysicalPorts = Functions
         .transformUnallocatedPhysicalPorts((List<PhysicalPort>) physicalPortService.findUnallocatedEntries(
             calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
     if (!StringUtils.hasText(sort)) {
       sort = "nocLabel";
     }
     sortExternalResources(sort, order, model, transformedUnallocatedPhysicalPorts);
     model.addAttribute(WebUtils.DATA_LIST, transformedUnallocatedPhysicalPorts);
     model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnallocated()));
     return PAGE_URL + "/listunallocated";
   }
 
   /**
    * @param sort
    * @param order
    * @param model
    * @param transformedUnallocatedPhysicalPorts
    */
   private void sortExternalResources(String sort, String order, Model model,
       final List<PhysicalPortView> transformedUnallocatedPhysicalPorts) {
     prepareSortOptions(sort, order, model);
     Collections.sort(transformedUnallocatedPhysicalPorts, new ReflectiveFieldComparator(sort));
 
     if (StringUtils.hasText(order)) {
       if ("DESC".equals(order)) {
         Collections.reverse(transformedUnallocatedPhysicalPorts);
       }
     }
   }
 
   @RequestMapping(value = "/unaligned", method = RequestMethod.GET)
   public String listUnaligned(@RequestParam(value = PAGE_KEY, required = false) Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, //
       Model model) {
 
     Sort sortOptions = prepareSortOptions(sort, order, model);
     model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnalignedPhysicalPorts()));
 
     model.addAttribute(WebUtils.DATA_LIST, Functions.transformAllocatedPhysicalPorts(
         physicalPortService.findUnalignedPhysicalPorts(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions),
         virtualPortService));
 
     return listUrl();
   }
 
   @RequestMapping(value = "/unaligned/search", method = RequestMethod.GET)
   public String listUnalignedSearch(@RequestParam(value = PAGE_KEY, required = false) final Integer page, //
       @RequestParam final String search, //
       final Model uiModel) {
 
     List<PhysicalPortView> unalignedPorts = getFullTextSearchableService().transformToView(
         physicalPortService.findUnalignedPhysicalPorts(), Security.getUserDetails());
 
    int firstItem = calculateFirstPage(page);
    List<PhysicalPortView> searchedUnalignedPortViews = Lists.newArrayList();
    try {
      searchedUnalignedPortViews = getFullTextSearchableService().searchForInFilteredList(PhysicalPort.class, search,
          0, Integer.MAX_VALUE, null, Security.getUserDetails(), unalignedPorts);
 
      uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(searchedUnalignedPortViews.size()));
      int toSize = Math.min(firstItem + MAX_ITEMS_PER_PAGE, searchedUnalignedPortViews.size());
 
      searchedUnalignedPortViews = searchedUnalignedPortViews.subList(firstItem, toSize);
 
      uiModel.addAttribute(WebUtils.PARAM_SEARCH, search);
      uiModel.addAttribute(WebUtils.DATA_LIST, searchedUnalignedPortViews);
    }
    catch (ParseException e) {
      uiModel.addAttribute(WebUtils.WARN_MESSAGES_KEY,
          Lists.newArrayList("Sorry, we could not process your search query."));
    }
 
     return listUrl();
   }
 
   @RequestMapping(value = "/mtosi", method = RequestMethod.GET)
   public String listMtosi(@RequestParam(value = PAGE_KEY, required = false) final Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, final Model model) {
 
     final List<PhysicalPortView> transformUnallocatedPhysicalPorts = Functions
         .transformUnallocatedPhysicalPorts((List<PhysicalPort>) physicalPortService.findUnallocatedMTOSIEntries(
             calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
 
     if (!StringUtils.hasText(sort)) {
       sort = "nocLabel";
     }
     sortExternalResources(sort, order, model, transformUnallocatedPhysicalPorts);
     model.addAttribute(WebUtils.DATA_LIST, transformUnallocatedPhysicalPorts);
     model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnallocatedMTOSI()));
     return PAGE_URL + "/listunallocated";
   }
 
   @RequestMapping(value = "edit", params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final String nmsPortId, final Model model) {
     PhysicalPort port;
     try {
       port = physicalPortService.findByNmsPortId(nmsPortId);
     }
     catch (IllegalStateException e) {
       return "redirect:";
     }
 
     model.addAttribute(MODEL_KEY, new CreatePhysicalPortCommand(port));
 
     return PAGE_URL + UPDATE;
   }
 
   @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
   public String delete(@RequestParam(ID_KEY) final String nmsPortId,
       @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
 
     physicalPortService.deleteByNmsPortId(nmsPortId);
 
     uiModel.asMap().clear();
     uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());
 
     return "redirect:";
   }
 
   @RequestMapping(value = "move", method = RequestMethod.GET)
   public String moveForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttrs) {
     PhysicalPort port = physicalPortService.find(id);
 
     if (port == null) {
       redirectAttrs.addFlashAttribute(WebUtils.INFO_MESSAGES_KEY, ImmutableList.of("Could not find port.."));
       return "redirect:/noc/physicalports";
     }
 
     Collection<PhysicalPort> unallocatedPorts = physicalPortService.findUnallocated();
     if (unallocatedPorts.isEmpty()) {
       WebUtils.addInfoMessage(redirectAttrs, messageSource, "info_physicalport_nounallocated");
       return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
     }
 
     long numberOfVirtualPorts = virtualPortService.countForPhysicalPort(port);
     long numberOfReservations = reservationService.countForPhysicalPort(port);
     long numberOfActiveReservations = reservationService.countActiveForPhysicalPort(port);
 
     model.addAttribute("relatedObjects", new RelatedObjects(numberOfVirtualPorts, numberOfReservations,
         numberOfActiveReservations));
     model.addAttribute("physicalPort", port);
     model.addAttribute("unallocatedPhysicalPorts", unallocatedPorts);
     model.addAttribute("movePhysicalPortCommand", new MovePhysicalPortCommand(port));
 
     return PAGE_URL + "/move";
   }
 
   @Override
   protected List<String> translateSortProperty(String sortProperty) {
     if (sortProperty.equals("instituteName")) {
       return ImmutableList.of("physicalResourceGroup.institute.name");
     }
 
     return super.translateSortProperty(sortProperty);
   }
 
   public static final class RelatedObjects {
     private final Long numberOfVirtualPorts;
     private final Long numberOfReservations;
     private final Long numberOfActiveReservations;
 
     public RelatedObjects(Long numberOfVirtualPorts, Long numberOfReservations, Long numberOfActiveReservations) {
       this.numberOfActiveReservations = numberOfActiveReservations;
       this.numberOfVirtualPorts = numberOfVirtualPorts;
       this.numberOfReservations = numberOfReservations;
     }
 
     public Long getNumberOfVirtualPorts() {
       return numberOfVirtualPorts;
     }
 
     public Long getNumberOfReservations() {
       return numberOfReservations;
     }
 
     public Long getNumberOfActiveReservations() {
       return numberOfActiveReservations;
     }
   }
 
   @RequestMapping(value = "move", method = RequestMethod.PUT)
   public String move(MovePhysicalPortCommand command, BindingResult result, Model model) {
 
     PhysicalPort newPort = physicalPortService.findByNmsPortId(command.getNewPhysicalPort());
     PhysicalPort oldPort = physicalPortService.find(command.getId());
 
     Collection<Reservation> reservations = nocService.movePort(oldPort, newPort);
 
     model.addAttribute("reservations", reservations);
 
     return "physicalports/moveResult";
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
   protected List<PhysicalPortView> list(int firstPage, int maxItems, Sort sort, Model model) {
 
     return Functions.transformAllocatedPhysicalPorts(
         physicalPortService.findAllocatedEntries(firstPage, maxItems, sort), virtualPortService);
   }
 
   @Override
   protected long count() {
     return physicalPortService.countAllocated();
   }
 
   @Override
   protected String getDefaultSortProperty() {
     return "nocLabel";
   }
 
   public static class MovePhysicalPortCommand {
     private Long id;
     private String newPhysicalPort;
 
     public MovePhysicalPortCommand() {
     }
 
     public MovePhysicalPortCommand(PhysicalPort port) {
       this.id = port.getId();
     }
 
     public String getNewPhysicalPort() {
       return newPhysicalPort;
     }
 
     public void setNewPhysicalPort(String newPhysicalPort) {
       this.newPhysicalPort = newPhysicalPort;
     }
 
     public Long getId() {
       return id;
     }
 
     public void setId(Long id) {
       this.id = id;
     }
   }
 
   public static class PhysicalPortCommand {
     @NotNull
     private PhysicalResourceGroup physicalResourceGroup;
     @NotEmpty
     private String nmsPortId;
     @NotEmpty
     private String nocLabel;
     @NotEmpty
     private String bodPortId;
 
     private String managerLabel;
 
     public PhysicalPortCommand() {
     }
 
     public PhysicalResourceGroup getPhysicalResourceGroup() {
       return physicalResourceGroup;
     }
 
     public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
       this.physicalResourceGroup = physicalResourceGroup;
     }
 
     public String getNmsPortId() {
       return nmsPortId;
     }
 
     public void setNmsPortId(String nmsPortId) {
       this.nmsPortId = nmsPortId;
     }
 
     public String getNocLabel() {
       return nocLabel;
     }
 
     public void setNocLabel(String nocLabel) {
       this.nocLabel = nocLabel;
     }
 
     public String getBodPortId() {
       return bodPortId;
     }
 
     public void setBodPortId(String portId) {
       this.bodPortId = portId;
     }
 
     public String getManagerLabel() {
       return managerLabel;
     }
 
     public void setManagerLabel(String managerLabel) {
       this.managerLabel = managerLabel;
     }
 
     @Override
     public String toString() {
       return "PhysicalPortCommand [physicalResourceGroup=" + physicalResourceGroup + ", nmsPortId=" + nmsPortId
           + ", nocLabel=" + nocLabel + ", bodPortId=" + bodPortId + ", managerLabel=" + managerLabel + "]";
     }
 
   }
 
   public static final class AddPhysicalPortCommand extends PhysicalPortCommand {
 
   }
 
   public static final class CreatePhysicalPortCommand extends PhysicalPortCommand {
     private Integer version;
 
     public CreatePhysicalPortCommand() {
     }
 
     public CreatePhysicalPortCommand(PhysicalPort port) {
       setNmsPortId(port.getNmsPortId());
       setPhysicalResourceGroup(port.getPhysicalResourceGroup());
       setNocLabel(port.getNocLabel());
       setManagerLabel(port.hasManagerLabel() ? port.getManagerLabel() : "");
       setBodPortId(port.getBodPortId());
       this.version = port.getVersion();
     }
 
     public Integer getVersion() {
       return version;
     }
 
     public void setVersion(Integer version) {
       this.version = version;
     }
 
   }
 
   @Override
   protected Class<PhysicalPort> getEntityClass() {
     return PhysicalPort.class;
   }
 
   @Override
   protected AbstractFullTextSearchService<PhysicalPortView, PhysicalPort> getFullTextSearchableService() {
     return (AbstractFullTextSearchService<PhysicalPortView, PhysicalPort>) physicalPortService;
   }
 
 }
