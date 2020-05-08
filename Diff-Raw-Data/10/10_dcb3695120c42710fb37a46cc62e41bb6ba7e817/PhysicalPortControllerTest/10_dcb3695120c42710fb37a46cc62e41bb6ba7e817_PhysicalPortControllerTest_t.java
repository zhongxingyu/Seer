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
 
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 import static nl.surfnet.bod.web.base.MessageManager.INFO_MESSAGES_KEY;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.isA;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
 
 import java.util.Collections;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 
 import nl.surfnet.bod.domain.NbiPort;
 import nl.surfnet.bod.domain.UniPort;
 import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.support.NbiPortFactory;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 import nl.surfnet.bod.web.base.MessageManager;
 import nl.surfnet.bod.web.base.MessageRetriever;
 import nl.surfnet.bod.web.noc.EnniPortController.CreateEnniPortCommand;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.test.web.servlet.MockMvc;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PhysicalPortControllerTest {
 
   @InjectMocks
   private PhysicalPortController subject;
 
   @Mock private PhysicalPortService physicalPortServiceMock;
   @Mock private VirtualPortService virtualPortServiceMock;
   @Mock private MessageRetriever messageRetriever;
   @Mock private ReservationService reservationService;
  @Mock private PhysicalResourceGroupService physicalResourceGroupServiceMock;
 
   private MessageManager messageManager;
 
   private MockMvc mockMvc;
 
   @Before
   public void setup() {
     messageManager = new MessageManager(messageRetriever);
     subject.setMessageManager(messageManager);
 
     mockMvc = standaloneSetup(subject).build();
   }
 
   @Test
   public void create_enni_form() throws Exception {
     final String nmsPortId = "foo";
     final NbiPort nbiPort = new NbiPortFactory().create();
     nbiPort.setNmsPortId(nmsPortId);
     nbiPort.setInterfaceType(NbiPort.InterfaceType.E_NNI);
 
     when(physicalPortServiceMock.findNbiPort(nmsPortId)).thenReturn(Optional.of(nbiPort));
 
     mockMvc.perform(get("/noc/physicalports/create").param(ID_KEY, nmsPortId))
         .andExpect(status().isOk())
         .andExpect(model().attribute("vlanRequired", notNullValue()))
         .andExpect(model().attribute("createEnniPortCommand", isA(CreateEnniPortCommand.class)))
         .andExpect(view().name("noc/physicalports/enni/create"));
   }
 
   @Test
   public void testCreateNoNbiFound() throws Exception {
     final String nmsId = "foo";
     when(physicalPortServiceMock.findNbiPort(nmsId)).thenReturn(Optional.<NbiPort>absent());
     mockMvc.perform(get("/noc/physicalports/create").param(ID_KEY, nmsId))
         .andExpect(status().isMovedTemporarily())
         .andExpect(view().name("redirect:"));
   }
 
   @Test
   public void create_uni_form() throws Exception {
     final String nmsPortId = "foo";
     final NbiPort nbiPort = new NbiPortFactory().create();
     nbiPort.setNmsPortId(nmsPortId);
     when(physicalPortServiceMock.findNbiPort(nmsPortId)).thenReturn(Optional.of(nbiPort));
 
     mockMvc.perform(get("/noc/physicalports/create").param(ID_KEY, nmsPortId))
         .andExpect(status().isOk())
         .andExpect(model().attribute("createUniPortCommand", isA(UniPortController.CreateUniPortCommand.class)))
         .andExpect(view().name("noc/physicalports/uni/create"));
   }
 
   @Test
   public void moveWithNonExistingPhysicalPortId() throws Exception {
     when(physicalPortServiceMock.find(8L)).thenReturn(null);
 
     mockMvc.perform(get("/noc/physicalports/move").param("id", "8"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(flash().attribute(INFO_MESSAGES_KEY, hasItem(containsString("Could not find"))))
         .andExpect(view().name("redirect:/noc/physicalports"));
   }
 
   @Test
   public void movePhysicalPortWithNoAvailableUnallocatedPorts() throws Exception {
     UniPort port = new PhysicalPortFactory().create();
 
     when(physicalPortServiceMock.find(8L)).thenReturn(port);
     when(physicalPortServiceMock.findUnallocated()).thenReturn(Collections.<NbiPort> emptyList());
     when(messageRetriever.getMessageWithBoldArguments("info_physicalport_nounallocated", "EPL")).thenReturn("expectedMessage");
 
     mockMvc.perform(get("/noc/physicalports/move").param("id", "8"))
         .andExpect(status().isMovedTemporarily())
         .andExpect(flash().attribute(INFO_MESSAGES_KEY, hasItem("expectedMessage")))
         .andExpect(view().name("redirect:/noc/physicalports"));
   }
 
   @Test
   public void movePhysicalPort() throws Exception {
     UniPort port = new PhysicalPortFactory().create();
 
     when(physicalPortServiceMock.find(8L)).thenReturn(port);
     when(physicalPortServiceMock.findUnallocated()).thenReturn(Lists.newArrayList(new NbiPortFactory().create()));
 
     mockMvc.perform(get("/noc/physicalports/move").param("id", "8"))
         .andExpect(status().isOk())
         .andExpect(model().attributeExists("relatedObjects", "movePhysicalPortCommand"))
         .andExpect(model().attribute("physicalPort", port))
         .andExpect(view().name("physicalports/move"));
   }
 
 }
