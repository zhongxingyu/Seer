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
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.support.ModelStub;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 import nl.surfnet.bod.support.VirtualPortFactory;
 import nl.surfnet.bod.web.noc.PhysicalPortController.CreatePhysicalPortCommand;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.data.domain.Sort;
 import org.springframework.ui.Model;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.google.common.collect.Lists;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PhysicalPortControllerTest {
 
   @InjectMocks
   private PhysicalPortController subject;
 
   @Mock
   private PhysicalPortService physicalPortServiceMock;
 
   @Mock
   private VirtualPortService virtualPortServiceMock;
 
   @Test
   public void listAllPortsShouldSetPortsAndMaxPages() {
     Model model = new ModelStub();
     List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
     when(physicalPortServiceMock.findAllocatedEntries(eq(0), anyInt(), any(Sort.class))).thenReturn(ports);
 
     subject.list(1, null, null, model);
 
     assertThat(model.asMap(), hasEntry("list", Object.class.cast(ports)));
     assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
   }
 
   @Test
   public void listAllPortsWithoutAPageParam() {
     Model model = new ModelStub();
     List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
     when(physicalPortServiceMock.findAllocatedEntries(eq(0), anyInt(), any(Sort.class))).thenReturn(ports);
 
     subject.list(null, null, null, model);
 
     assertThat(model.asMap(), hasEntry("list", Object.class.cast(ports)));
     assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
   }
 
   @Test
   public void listAllUnallocatedPortsShouldSetPortsAndMaxPages() {
     Model model = new ModelStub();
     List<PhysicalPort> ports = Lists.newArrayList(new PhysicalPortFactory().create());
     when(physicalPortServiceMock.findUnallocatedEntries(eq(0), anyInt())).thenReturn(ports);
 
     subject.listUnallocated(1, model);
 
     assertThat(model.asMap(), hasEntry("list", Object.class.cast(ports)));
     assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
   }
 
   @Test
   public void showNonExistingPort() {
     Model model = new ModelStub();
     when(physicalPortServiceMock.findByNetworkElementPk("12:00/port1")).thenReturn(null);
 
     subject.show("12:00/port1", model);
 
     assertThat(model.asMap(), hasEntry(is("physicalPort"), nullValue()));
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void showExistingPort() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     VirtualPort virtualPort = new VirtualPortFactory().create();
 
     when(physicalPortServiceMock.findByNetworkElementPk("12:00/port1")).thenReturn(port);
     when(virtualPortServiceMock.findAllForPhysicalPort(port)).thenReturn(Lists.newArrayList(virtualPort));
 
     subject.show("12:00/port1", model);
 
     assertThat(model.asMap(), hasEntry("physicalPort", Object.class.cast(port)));
     assertThat(model.asMap(), hasKey("virtualPorts"));
     assertThat(((List<VirtualPort>) model.asMap().get("virtualPorts")), hasSize(1));
   }
 
   @Test
   public void updateForm() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().setNetworkElementPk("00:00/port2").create();
 
     when(physicalPortServiceMock.findByNetworkElementPk("00:00/port2")).thenReturn(port);
 
     subject.updateForm("00:00/port2", model);
 
     assertThat(model.asMap(), hasKey("createPhysicalPortCommand"));
     assertThat(((CreatePhysicalPortCommand) model.asMap().get("createPhysicalPortCommand")).getNetworkElementPk(),
         is("00:00/port2"));
   }
 
   @Test
   public void updateShouldGoToFreePortsAndShowMessage() {
     RedirectAttributes model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     BindingResult result = new BeanPropertyBindingResult(port, "physicalPort");
 
     when(physicalPortServiceMock.findByNetworkElementPk(port.getNetworkElementPk())).thenReturn(port);
 
     String page = subject.update(new CreatePhysicalPortCommand(port), result, model, model);
 
    assertThat(page, is("redirect:free"));
     assertThat(model.getFlashAttributes(), hasKey("infoMessages"));
 
     @SuppressWarnings("unchecked")
     String flashMessage = ((List<String>) model.getFlashAttributes().get("infoMessages")).get(0);
     assertThat(flashMessage, containsString(port.getNocLabel()));
     assertThat(flashMessage, containsString(port.getPhysicalResourceGroup().getInstitute().getName()));
 
     verify(physicalPortServiceMock).save(port);
   }
 
   @Test
   public void deleteShouldStayOnSamePage() {
     Model model = new ModelStub();
     PhysicalPort port = new PhysicalPortFactory().create();
     when(physicalPortServiceMock.findByNetworkElementPk("port_name")).thenReturn(port);
 
     subject.delete("port_name", 3, model);
 
     assertThat(model.asMap(), hasEntry(PAGE_KEY, Object.class.cast("3")));
 
     verify(physicalPortServiceMock, times(1)).delete(port);
   }
 
 }
