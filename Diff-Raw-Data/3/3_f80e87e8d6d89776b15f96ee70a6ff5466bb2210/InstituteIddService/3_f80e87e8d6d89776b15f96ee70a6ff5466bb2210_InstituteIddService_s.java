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
 package nl.surfnet.bod.service;
 
 import java.util.Collection;
 import java.util.List;
 
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.idd.generated.Klanten;
 import nl.surfnet.bod.repo.InstituteRepo;
 import nl.surfnet.bod.util.Functions;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class InstituteIddService implements InstituteService {
 
   private static final String INSTITUTE_REFRESH_CRON_KEY = "institute.refresh.job.cron";
 
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
   @Autowired
   private IddClient iddClient;
 
   @Autowired
   private InstituteRepo instituteRepo;
 
   @Autowired
   private LogEventService logEventService;
 
   @Override
   public Institute find(Long id) {
     return instituteRepo.findOne(id);
   }
 
   @Override
   public Collection<Institute> findAlignedWithIDD() {
     return instituteRepo.findByAlignedWithIDD(true);
   }
 
   @Override
   @Scheduled(cron = "${" + INSTITUTE_REFRESH_CRON_KEY + "}")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void refreshInstitutes() {
     logger
         .info("Refreshing institutes from IDD to BoD, job based on configuration key: {}", INSTITUTE_REFRESH_CRON_KEY);
 
     // Mark not aligned
     List<Institute> currentInstitutes = instituteRepo.findAll();
     markNotAligned(currentInstitutes);
     instituteRepo.save(currentInstitutes);
 
     // Align With IDD
     Collection<Klanten> klanten = iddClient.getKlanten();
     Collection<Institute> institutes = Functions.transformKlanten(klanten, true);
     instituteRepo.save(institutes);
     logger.info("Retrieved {} institutes from IDD and updated them in the BoD database: ", klanten.size());
 
     logEventService.logUpdateEvent(institutes);
   }
 
   private void markNotAligned(List<Institute> allInstitutes) {
     // Mark all not aligned
     for (Institute institute : allInstitutes) {
       institute.setAlignedWithIDD(false);
     }
   }
 
 }
