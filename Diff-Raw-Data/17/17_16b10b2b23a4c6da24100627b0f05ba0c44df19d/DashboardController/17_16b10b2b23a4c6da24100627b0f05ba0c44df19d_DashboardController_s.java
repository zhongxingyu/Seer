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
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import com.google.common.annotations.VisibleForTesting;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.support.ReservationFilterViewFactory;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.base.MessageManager;
 import nl.surfnet.bod.web.view.NocStatisticsView;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller("nocDashboardController")
 @RequestMapping(DashboardController.PAGE_URL)
 public class DashboardController {
 
   public static final String PAGE_URL = "/noc";
   private static final String CHECK_PORTS = "/checkports";
   private static final String CHECK_PORTS_URL = PAGE_URL + CHECK_PORTS;
 
   private final Logger log = LoggerFactory.getLogger(this.getClass());
 
   @Resource private PhysicalPortService physicalPortService;
   @Resource private ReservationService reservationService;
   @Resource private MessageManager messageManager;
 
   @Resource(name = "bodEnvironment")
   private Environment environment;
 
   @RequestMapping(method = RequestMethod.GET)
   public String index(Model model) {
 
     model.addAttribute("stats", determineStatistics());
     model.addAttribute("defaultDuration", ReservationFilterViewFactory.DEFAULT_FILTER_INTERVAL_STRING);
 
     generateErrorMessagesForUnalignedPorts(model, physicalPortService.findUnalignedPhysicalPorts());
 
     return "noc/index";
   }
 
   @RequestMapping(value = CHECK_PORTS, method = RequestMethod.GET)
   public String forceCheckForPortInconsistencies(String callbackViewName) {
     log.info("Manually forcing check for port inconsistencies");
     physicalPortService.forceCheckForPortInconsistencies();
     return "redirect:/noc";
   }
 
   @VisibleForTesting
   NocStatisticsView determineStatistics() {
     ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();
 
     long countUniPorts = physicalPortService.countUniPorts();
     long countEnniPorts = physicalPortService.countEnniPorts();
 
     long countElapsedReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
         .create(ReservationFilterViewFactory.ELAPSED));
 
     long countActiveReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
         .create(ReservationFilterViewFactory.ACTIVE));
 
     long countComingReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
         .create(ReservationFilterViewFactory.COMING));
 
     long countMissingPhysicalPorts = physicalPortService.countUnalignedPhysicalPorts();
 
     return new NocStatisticsView(countUniPorts, countEnniPorts, countElapsedReservations, countActiveReservations,
         countComingReservations, countMissingPhysicalPorts);
   }
 
   private void generateErrorMessagesForUnalignedPorts(Model model, List<PhysicalPort> unalignedPorts) {

    final String forcePortCheckButton = createForcePortCheckButton(environment.getExternalBodUrl() + CHECK_PORTS_URL);
 
     for (PhysicalPort port : unalignedPorts) {
       messageManager.addErrorMessage(forcePortCheckButton, model, "info_physicalport_unaligned_with_nms." + port.getNmsAlignmentStatus().name(), port
           .getNmsPortId(), port.getNocLabel());
     }
   }
 
   private String createForcePortCheckButton(String actionUrl) {
     return String.format(
        "<a href=\"%s\" rel=\"tooltip\" title=\"Force port check\"><i class=\"icon-refresh\"><!--  --></i></a>",
        actionUrl);
   }
 }
