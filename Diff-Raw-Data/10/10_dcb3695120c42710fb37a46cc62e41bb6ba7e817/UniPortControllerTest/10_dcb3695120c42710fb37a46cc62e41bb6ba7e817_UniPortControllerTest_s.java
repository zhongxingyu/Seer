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
 package nl.surfnet.bod.web.noc;
 
 import static nl.surfnet.bod.web.WebUtils.DATA_LIST;
 import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyVararg;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 
 import nl.surfnet.bod.domain.NbiPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.UniPort;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.support.NbiPortFactory;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.base.MessageManager;
 import nl.surfnet.bod.web.base.MessageRetriever;
 import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.core.convert.converter.Converter;
 import org.springframework.data.domain.Sort;
 import org.springframework.format.support.FormattingConversionService;
 import org.springframework.test.web.servlet.MockMvc;
 
 @RunWith(MockitoJUnitRunner.class)
 public class UniPortControllerTest {
 
   @InjectMocks
   private UniPortController subject;
 
   @Mock private PhysicalPortService physicalPortServiceMock;
   @Mock private PhysicalResourceGroupService physicalResourceGroupServiceMock;
   @Mock private MessageRetriever messageRetriever;
   @Mock private VirtualPortService virtualPortService;
   @Mock private ReservationService reservationService;
 
   private MessageManager messageManager;
 
   private MockMvc mockMvc;
 
   @Before
   public void setup() {
     FormattingConversionService conversionService = new FormattingConversionService();
     conversionService.addConverter(new Converter<String, PhysicalResourceGroup>() {
       @Override
       public PhysicalResourceGroup convert(final String id) {
         return physicalResourceGroupServiceMock.find(Long.valueOf(id));
       }
     });
 
     messageManager = new MessageManager(messageRetriever);
     subject.setMessageManager(messageManager);
 
     mockMvc = standaloneSetup(subject).setConversionService(conversionService).build();
   }
 
   @Test
   public void updateUniForm() throws Exception {
     UniPort port = new PhysicalPortFactory().create();
 
     when(physicalPortServiceMock.findUniPort(12L)).thenReturn(port);
 
     mockMvc.perform(get("/noc/physicalports/uni/edit").param("id", "12"))
       .andExpect(status().isOk())
       .andExpect(model().attributeExists("updateUniPortCommand"));
   }
 
   @Test
   public void updateFormWithNonExistingPortId() throws Exception {
     when(physicalPortServiceMock.findUniPort(12L)).thenReturn(null);
 
     mockMvc.perform(get("/noc/physicalports/uni/edit").param("id", "12"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(view().name("redirect:"));
   }
 
   @Test
   public void addPhysicalPortFormWithUnallocatedPorts() throws Exception {
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(new PhysicalResourceGroupFactory().create());
     when(physicalPortServiceMock.findUnallocatedUniPorts()).thenReturn(ImmutableList.of(new NbiPortFactory().create()));
 
     mockMvc.perform(get("/noc/physicalports/uni/add").param("prg", "1"))
         .andExpect(status().isOk())
         .andExpect(model().attributeExists("addPhysicalPortCommand", "unallocatedPhysicalPorts"));
   }
 
   @Test
   public void updateWithErrorsShouldGoBackToForm() throws Exception {
     UniPort port = new PhysicalPortFactory().create();
 
     when(physicalPortServiceMock.findByNmsPortId("12")).thenReturn(port);
 
     mockMvc.perform(put("/noc/physicalports/uni")
         .param("version", "0")
         .param("nmsPortId", "12")
         .param("nocLabel", "")
         .param("managerLabel", "")
         .param("physicalResourceGroup", "1"))
         .andExpect(status().isOk())
         .andExpect(model().attributeExists("updateUniPortCommand"))
         .andExpect(view().name("noc/physicalports/uni/update"));
 
     verify(physicalPortServiceMock, never()).save(port);
   }
 
   @Test
   public void updateShouldGoToDefaultListPageAndShowMessage() throws Exception {
     UniPort port = new PhysicalPortFactory().setNocLabel("wrong").setManagerLabel("wrong").create();
 
     when(physicalPortServiceMock.findByNmsPortId("12")).thenReturn(port);
     when(physicalPortServiceMock.findNbiPort("12")).thenReturn(Optional.of(port.getNbiPort()));
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(port.getPhysicalResourceGroup());
     when(messageRetriever.getMessageWithBoldArguments("info_physicalport_updated", "NOC port", port.getPhysicalResourceGroup().getName()))
         .thenReturn("expectedMessage");
 
     mockMvc.perform(put("/noc/physicalports/uni")
         .param("version", "0")
         .param("nmsPortId", "12")
         .param("nocLabel", "NOC port")
         .param("bodPortId", "2-2b")
         .param("managerLabel", "Manager port")
         .param("physicalResourceGroup", "1"))
         .andExpect(status().isMovedTemporarily())
        .andExpect(view().name("redirect:/noc/physicalports"))
         .andExpect(flash().attribute("infoMessages", hasItem("expectedMessage")));
 
     assertThat(port.getNocLabel(), is("NOC port"));
     assertThat(port.getManagerLabel(), is("Manager port"));
 
     verify(physicalPortServiceMock).save(port);
   }
 
   @Test
   public void listAllPortsShouldSetPortsAndMaxPages() throws Exception {
     when(physicalPortServiceMock.findAllocatedUniEntries(eq(0), anyInt(), any(Sort.class))).thenReturn(ImmutableList.of(new PhysicalPortFactory().create()));
 
     mockMvc.perform(get("/noc/physicalports/uni"))
       .andExpect(status().isOk())
       .andExpect(model().<Collection<?>> attribute(DATA_LIST, hasSize(1)))
       .andExpect(model().attribute(MAX_PAGES_KEY, 1))
       .andExpect(model().attribute("filterSelect", PhysicalPortFilter.UNI_ALLOCATED))
       .andExpect(view().name("noc/physicalports/uni/list"));
   }
 
   @Test
   public void addPhysicalPortFormWithoutExistingPhysicalResourceGroup() throws Exception {
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);
 
     mockMvc.perform(get("/noc/physicalports/uni/add").param("prg", "1"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(view().name("redirect:/"));
   }
 
   @Test
   public void addPhysicalPortFormWithoutAnyUnallocatedPorts() throws Exception {
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(new PhysicalResourceGroupFactory().create());
     when(physicalPortServiceMock.findUnallocated()).thenReturn(Collections.<NbiPort> emptyList());
 
     when(messageRetriever.getMessageWithBoldArguments(eq("info_physicalport_updated"), (String[]) anyVararg()))
         .thenReturn("test message");
 
     mockMvc.perform(get("/noc/physicalports/uni/add").param("prg", "1"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(view().name("redirect:/noc/institutes"))
         .andExpect(flash().<Collection<?>> attribute(MessageManager.INFO_MESSAGES_KEY, hasSize(1)));
   }
 
   @Test
   public void addPhysicalPortWithoutPostData() throws Exception {
     mockMvc.perform(post("/noc/physicalports/uni/add"))
         .andExpect(status().isOk())
         .andExpect(model().hasErrors())
         .andExpect(view().name("noc/physicalports/addPhysicalPort"));
   }
 
   @Test
   public void addPhysicalPort() throws Exception {
     String nmsPortId = "00-BB_ETH0";
     NbiPort nbiPort = new NbiPortFactory().create();
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(prg);
     when(physicalPortServiceMock.findNbiPort(nmsPortId)).thenReturn(Optional.of(nbiPort));
 
     mockMvc.perform(post("/noc/physicalports/uni/add")
         .param("nmsPortId", nmsPortId)
         .param("nocLabel", "NOC port")
         .param("bodPortId", "2-2a")
         .param("managerLabel", "")
         .param("physicalResourceGroup", "1"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(model().hasNoErrors())
         .andExpect(view().name("redirect:/noc/institutes"));
 
     ArgumentCaptor<UniPort> portCaptor = ArgumentCaptor.forClass(UniPort.class);
     verify(physicalPortServiceMock).save(portCaptor.capture());
 
     assertThat(portCaptor.getValue().getNocLabel(), is("NOC port"));
     assertThat(portCaptor.getValue().getPhysicalResourceGroup(), is(prg));
   }
 
   @Test
   public void deleteShouldStayOnSamePageButReload() throws Exception {
     UniPort port = new PhysicalPortFactory().create();
 
     mockMvc.perform(delete("/noc/physicalports/uni").param("id", "" + port.getId()).param("page", "3"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(model().attribute(PAGE_KEY, is("3")))
         .andExpect(view().name("redirect:"));
 
     verify(physicalPortServiceMock).delete(port.getId());
   }
 
   @Test
   public void listAllPortsWithAPageParam() throws Exception {
     when(physicalPortServiceMock.findAllocatedUniEntries(eq(2 * WebUtils.MAX_ITEMS_PER_PAGE), anyInt(), any(Sort.class)))
         .thenReturn(ImmutableList.of(new PhysicalPortFactory().create()));
 
     mockMvc.perform(get("/noc/physicalports/uni").param(PAGE_KEY, "3"))
         .andExpect(status().isOk())
         .andExpect(model().<Collection<?>> attribute(DATA_LIST, hasSize(1)))
         .andExpect(model().attribute(MAX_PAGES_KEY, 1));
   }
 
   @Test
   public void updateWithNonExistingPort() throws Exception {
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(new PhysicalResourceGroupFactory().create());
     when(physicalPortServiceMock.findByNmsPortId("12")).thenReturn(null);
     when(physicalPortServiceMock.findNbiPort("12")).thenReturn(Optional.<NbiPort> absent());
 
     mockMvc.perform(put("/noc/physicalports/uni")
         .param("version", "0")
         .param("nmsPortId", "12")
         .param("bodPortId", "2-2")
         .param("nocLabel", "NOC label")
         .param("managerLabel", "")
         .param("physicalResourceGroup", "1"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(view().name("redirect:"));
   }
 
   @Test
   public void updateWithoutManagerLabelShouldClearManagerLabel() throws Exception {
     UniPort port = new PhysicalPortFactory().setNocLabel("wrong").setManagerLabel("wrong").create();
 
     when(physicalPortServiceMock.findByNmsPortId("12")).thenReturn(port);
     when(physicalPortServiceMock.findNbiPort("12")).thenReturn(Optional.of(port.getNbiPort()));
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(port.getPhysicalResourceGroup());
 
     mockMvc.perform(put("/noc/physicalports/uni")
         .param("version", "0")
         .param("nmsPortId", "12")
         .param("nocLabel", "NOC port")
         .param("bodPortId", "2-2b")
         .param("managerLabel", "")
         .param("physicalResourceGroup", "1"))
         .andExpect(status().isMovedTemporarily());
 
     assertThat(port.getManagerLabel(), is("NOC port"));
 
     verify(physicalPortServiceMock).save(port);
   }
 
 
 }
