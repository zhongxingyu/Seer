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
 
 import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasEntry;
 import static org.hamcrest.Matchers.hasKey;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.support.ModelStub;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.noc.PhysicalPortController.CreatePhysicalPortCommand;
 import nl.surfnet.bod.web.view.ElementActionView;
 import nl.surfnet.bod.web.view.PhysicalPortView;
 
 import org.hamcrest.Matchers;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.context.MessageSource;
 import org.springframework.data.domain.Sort;
 import org.springframework.ui.Model;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PhysicalPortControllerTest {
 
   @InjectMocks
   private PhysicalPortController subject;
 
   @Mock
   private PhysicalPortService physicalPortServiceMock;
 
   @Mock
   private PhysicalResourceGroupService physicalResourceGroupServiceMock;
 
   @Mock
   private VirtualPortService virtualPortServiceMock;
 
   @Mock
   private InstituteService instituteServiceMock;
 
   @Mock
   private MessageSource messageSource;
 
   @Test
   public void listAllPortsShouldSetPortsAndMaxPages() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     ElementActionView deleteActionView = new ElementActionView(true, "label_unallocate");
 
     List<PhysicalPort> ports = Lists.newArrayList(port);
     List<PhysicalPortView> transformedPorts = Lists.newArrayList(new PhysicalPortView(port, deleteActionView));
 
     when(physicalPortServiceMock.findAllocatedEntries(eq(0), anyInt(), org.mockito.Matchers.any(Sort.class)))
         .thenReturn(ports);
 
     subject.list(1, null, null, model);
 
     List<PhysicalPortView> modelList = WebUtils.getAttributeFromModel("list", model);
 
     assertThat(modelList.get(0), is(transformedPorts.get(0)));
     assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
   }
 
   @Test
   public void listAllPortsWithoutAPageParam() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     ElementActionView deleteActionView = new ElementActionView(true, "label_unallocate");
     List<PhysicalPort> ports = Lists.newArrayList(port);
     List<PhysicalPortView> transformedPorts = Lists.newArrayList(new PhysicalPortView(port, deleteActionView));
 
     when(physicalPortServiceMock.findAllocatedEntries(eq(0), anyInt(), org.mockito.Matchers.any(Sort.class)))
         .thenReturn(ports);
 
     subject.list(null, null, null, model);
 
     List<PhysicalPortView> modelList = WebUtils.getAttributeFromModel("list", model);
     assertThat(modelList.get(0), is(transformedPorts.get(0)));
     assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
   }
 
   @Test
   public void listAllUnallocatedPortsShouldSetPortsAndMaxPages() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     List<PhysicalPort> ports = Lists.newArrayList(port);
     //Unallocated can never be 'deleted'
     PhysicalPortView physicalPortView = new PhysicalPortView(port, null, 0);
     List<PhysicalPortView> transformedPorts = Lists.newArrayList(physicalPortView);
 
     when(physicalPortServiceMock.findUnallocatedEntries(eq(0), anyInt())).thenReturn(ports);
 
     subject.listUnallocated(1, null, null, model);
 
     List<PhysicalPortView> modelList = WebUtils.getAttributeFromModel("list", model);
     assertThat(modelList.get(0), is(transformedPorts.get(0)));
     assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
   }
 
   @Test
   public void updateForm() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().setNmsPortId("00:00/port2").create();
 
     when(physicalPortServiceMock.findByNmsPortId("00:00/port2")).thenReturn(port);
 
     subject.updateForm("00:00/port2", model);
 
     assertThat(model.asMap(), hasKey("createPhysicalPortCommand"));
     assertThat(((CreatePhysicalPortCommand) model.asMap().get("createPhysicalPortCommand")).getNmsPortId(),
         is("00:00/port2"));
   }
 
   @Test
   public void updateShouldGoToFreePortsAndShowMessage() {
     RedirectAttributes model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     BindingResult result = new BeanPropertyBindingResult(port, "physicalPort");
 
     when(
         messageSource.getMessage(anyString(), org.mockito.Matchers.any(Object[].class),
             org.mockito.Matchers.any(Locale.class))).thenReturn("Flash message");
     when(physicalPortServiceMock.findByNmsPortId(port.getNmsPortId())).thenReturn(port);
 
     String page = subject.update(new CreatePhysicalPortCommand(port), result, model, model);
 
     assertThat(page, is("redirect:physicalports"));
     assertThat(model.getFlashAttributes(), hasKey("infoMessages"));
 
     @SuppressWarnings("unchecked")
     String flashMessage = ((List<String>) model.getFlashAttributes().get("infoMessages")).get(0);
     assertThat(flashMessage, is("Flash message"));
 
     verify(physicalPortServiceMock).save(port);
   }
 
   @Test
   public void deleteShouldStayOnSamePage() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     when(physicalPortServiceMock.findByNmsPortId("port_name")).thenReturn(port);
 
     subject.delete("port_name", 3, model);
 
     assertThat(model.asMap(), hasEntry(PAGE_KEY, Object.class.cast("3")));
 
     verify(physicalPortServiceMock, times(1)).delete(port);
   }
 
   @Test
   public void addPhysicalPortFormWithoutExistingPhysicalResourceGroup() {
     ModelStub model = new ModelStub();
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);
 
     String page = subject.addPhysicalPortForm(1L, model, model);
 
     assertThat(page, is("redirect:/"));
   }
 
   @Test
   public void addPhysicalPortFormWithoutAnyUnallocatedPorts() {
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
 
     ModelStub model = new ModelStub();
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(prg);
     when(physicalPortServiceMock.findUnallocated()).thenReturn(Collections.<PhysicalPort> emptyList());
     when(messageSource.getMessage(eq("info_physicalport_nounallocated"), any(Object[].class), any(Locale.class)))
         .thenReturn("no more ports");
 
     String page = subject.addPhysicalPortForm(1L, model, model);
 
    assertThat(page, is("redirect:/noc/institutes"));
     assertThat(model.getFlashAttributes(),
         Matchers.<String, Object> hasEntry(WebUtils.INFO_MESSAGES_KEY, Lists.newArrayList("no more ports")));
   }
 
   @Test
   public void addPhysicalPortFormWithUnallocatedPorts() {
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
     PhysicalPort port = new PhysicalPortFactory().create();
 
     ModelStub model = new ModelStub();
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(prg);
     when(physicalPortServiceMock.findUnallocated()).thenReturn(ImmutableList.of(port));
 
     String page = subject.addPhysicalPortForm(1L, model, model);
 
     assertThat(page, is("physicalports/addPhysicalPort"));
     assertThat(model.asMap(), hasKey("addPhysicalPortCommand"));
     assertThat(model.asMap(), hasKey("unallocatedPhysicalPorts"));
   }
 }
