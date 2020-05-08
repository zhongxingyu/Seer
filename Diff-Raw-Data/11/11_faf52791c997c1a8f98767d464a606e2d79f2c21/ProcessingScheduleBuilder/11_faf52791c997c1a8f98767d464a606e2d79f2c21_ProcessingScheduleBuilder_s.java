 /*
  * Copyright © 2013 VillageReach. All Rights Reserved. This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
  *
  * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package org.openlmis.performancetesting.builder;
 
 import org.openlmis.core.domain.ProcessingPeriod;
 import org.openlmis.core.domain.ProcessingSchedule;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
 import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
 import static org.openlmis.performancetesting.Utils.randomDate;
 
 public class ProcessingScheduleBuilder {
 
   public ProcessingSchedule createSchedule(String code, String name) {
     ProcessingSchedule schedule = new ProcessingSchedule();
     schedule.setCode(code);
     schedule.setName(name);
     schedule.setDescription(randomAlphanumeric(30));
     schedule.setModifiedBy(Integer.valueOf(randomNumeric(5)));
     schedule.setModifiedDate(randomDate());
 
     return schedule;
   }
 
  public ProcessingPeriod createPeriod(Date startDate, Date endDate, ProcessingSchedule monthlySchedule) {
     ProcessingPeriod period = new ProcessingPeriod();
     period.setName(new SimpleDateFormat("MM-dd-yyyy").format(startDate));
     period.setDescription(randomAlphanumeric(10));
     period.setNumberOfMonths(1);
     period.setStartDate(startDate);
     period.setEndDate(endDate);
     period.setScheduleId(monthlySchedule.getId());
     period.setModifiedBy(Integer.valueOf(randomNumeric(5)));
     period.setModifiedDate(randomDate());
 
     return period;
   }
 }
