 /*
  * Copyright © 2013 VillageReach. All Rights Reserved. This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
  *
  * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package org.openlmis.performancetesting.builder;
 
 import org.openlmis.core.domain.Facility;
 import org.openlmis.core.domain.ProcessingPeriod;
 import org.openlmis.core.domain.Program;
 import org.openlmis.core.domain.SupervisoryNode;
 import org.openlmis.rnr.domain.Rnr;
 import org.openlmis.rnr.domain.RnrStatus;
 
 import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
 import static org.openlmis.performancetesting.Utils.randomDate;
 import static org.openlmis.performancetesting.Utils.randomMoney;
 
 public class RequisitionBuilder {
 
   public Rnr createRequisition(Facility facility, Program program, ProcessingPeriod period,
                                SupervisoryNode supervisoryNode, Facility supplyingFacility, RnrStatus status) {
     Rnr rnr = new Rnr();
     rnr.setStatus(status);
     rnr.setFullSupplyItemsSubmittedCost(randomMoney(7));
     rnr.setNonFullSupplyItemsSubmittedCost(randomMoney(8));
     rnr.setModifiedBy(Long.valueOf(randomNumeric(5)));
     rnr.setModifiedDate(randomDate());
    rnr.setSubmittedDate(period.getEndDate());
 
     rnr.setFacility(facility);
     rnr.setProgram(program);
     rnr.setPeriod(period);
     rnr.setSupplyingFacility(supplyingFacility);
 
 
     if (supervisoryNode == null) {
       supervisoryNode = new SupervisoryNode();
     }
     rnr.setSupervisoryNodeId(supervisoryNode.getId());
 
 
     return rnr;
 
   }
 
 }
