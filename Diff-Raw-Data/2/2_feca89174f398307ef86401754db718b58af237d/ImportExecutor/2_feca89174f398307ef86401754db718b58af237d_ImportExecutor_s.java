 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.haiba.lprimporter.importer;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
 import dk.nsi.haiba.lprimporter.dao.LPRDAO;
 import dk.nsi.haiba.lprimporter.email.EmailSender;
 import dk.nsi.haiba.lprimporter.log.Log;
 import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
 import dk.nsi.haiba.lprimporter.model.lpr.Administration;
 import dk.nsi.haiba.lprimporter.rules.RulesEngine;
 import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;
 
 /*
  * Scheduled job, responsible for fetching new data from LPR, then send it to the RulesEngine for further processing
  */
 public class ImportExecutor {
 	
 	private static Log log = new Log(Logger.getLogger(ImportExecutor.class));
 
 	@Value("${lpr.cpr.batchsize}")
 	int batchsize;
 	
     @Value("${currentpatient.default.outdate.days.after.indate}")
 	private int currentPatientDaysIfGreaterThanInterval;
 
     private boolean manualOverride;
 	
     LPRDAO lprdao;
     
     @Autowired
     HAIBADAO haibaDao;
     
 	@Autowired
 	RulesEngine rulesEngine;
 	
 	@Autowired
 	ImportStatusRepository statusRepo;
 	
     @Autowired
     EmailSender emailSender;
 	
     public ImportExecutor(LPRDAO lprdao) {
         this.lprdao = lprdao;
     }
 
     @Scheduled(cron = "${cron.import.job}")
 	public void run() {
 		if(!isManualOverride()) {
 			log.debug("Running Importer: " + new Date().toString());
 			doProcess(false);
 		} else {
 			log.debug("Importer must be started manually");
 		}
 	}
 
 	/*
 	 * Separated into its own method for testing purpose, because testing a scheduled method isn't good
 	 */
 	public void doProcess(boolean manual) {
 		log.info("Started processing, manual="+isManualOverride());
 		// Fetch new records from LPR contact table
 		try {
 			statusRepo.importStartedAt(new DateTime());
             if (manual) {
                 emailSender.sendHello();
             }
 			// if syncId > 0, the Carecom job is finished, and the database is ready for import.
 			long syncId = lprdao.isdatabaseReadyForImport();
 			log.debug("syncId is " + syncId);
 			if(syncId == 0) {
 				String status = "HAIBA_LPR_REPLIKA is not ready for import, Carecom job is not finished yet.";
 				log.warn(status);
 				statusRepo.importEndedWithFailure(new DateTime(), status);
 				if (manual) {
 	                emailSender.sendDone("Carecom job is not finished yet");
 	            }
 				return;
 			}
 			
 			if(lprdao.hasUnprocessedCPRnumbers()) {
 				Statistics statistics = Statistics.getInstance();
 
 				log.info("LPR has unprocessed CPR numbers, starting import");
 				
 				//check if any contacts are deleted, and recalculate the affected CPR numbers
 				List<String> cprNumbersWithDeletedContacts = lprdao.getCPRnumbersFromDeletedContacts();
 				log.debug("processing "+cprNumbersWithDeletedContacts.size()+ " cprnumbers with deleted contacts");
 				// count CPR numbers with deleted contacts
 				statistics.cprNumbersWithDeletedContactsCounter += cprNumbersWithDeletedContacts.size();
 				for (String cpr : cprNumbersWithDeletedContacts) {
 					processCPRNumber(cpr, statistics, false);
 				}
 				
 				// new data has arrived, check if any of the processed current patients are discharged
 				List<String> currentPatients = haibaDao.getCurrentPatients();
 				log.debug("processing "+currentPatients.size()+ " current patients cprnumbers");
 				// count CPR numbers processed for current patients
 				statistics.currentPatientsCounter += currentPatients.size();
 				for (String cpr : currentPatients) {
 					processCPRNumber(cpr, statistics, true);
 				}
 
 				// process the new data
 				List<String> unprocessedCPRnumbers = lprdao.getCPRnumberBatch(batchsize);
 				while(unprocessedCPRnumbers.size() > 0) {
 					// count the unprocessed CPR numbers
 					statistics.cprCounter += unprocessedCPRnumbers.size();
 
 					log.debug("processing "+unprocessedCPRnumbers.size()+ " cprnumbers");
 					for (String cpr : unprocessedCPRnumbers) {
 						processCPRNumber(cpr, statistics, false);
 					}
 					// fetch the next batch
 					unprocessedCPRnumbers = lprdao.getCPRnumberBatch(batchsize);
 				}
 				statusRepo.importEndedWithSuccess(new DateTime());
 				haibaDao.saveStatistics(statistics);
 				statistics.resetInstance();
 			} else {
 				log.info("No unprocessed CPR numbers found");
 				statusRepo.importEndedWithSuccess(new DateTime());
 			}
 			if (manual) {
 			    emailSender.sendDone(null);
 			}
 		} catch(Exception e) {
 			log.error("", e);
			statusRepo.importEndedWithFailure(new DateTime(), e.getMessage());
             if (manual) {
                 emailSender.sendDone(ExceptionUtils.getStackTrace(e));
             }
 		}
 	}
 
 
 	private void processCPRNumber(String cpr, Statistics statistics, boolean currentPatient) {
 		List<Administration> contactsByCPR = lprdao.getContactsByCPR(cpr);
 		log.debug("Fetched "+contactsByCPR.size()+ " contacts");
 		
 		if(currentPatient) {
 			boolean recalculate = false;
 			// check if data is changed, and if current contact is more than 30 days old.
 			for (Administration contact : contactsByCPR) {
 				// check if any contacts has outdate set to null
 				if(contact.getUdskrivningsDatetime() == null) {
 					// if an outdate is null, check if the indate is more than 30 days old - if so nothing has changed
 					DateTime in = new DateTime(contact.getIndlaeggelsesDatetime());
 					if(in.plusDays(currentPatientDaysIfGreaterThanInterval).isBeforeNow()) {
 						// current patient doesn't need to be recalculated, as nothing has changed
 					} else {
 						recalculate = true;
 						break;
 					}
 					
 				}
 			}
 			if(!recalculate) {
 				log.debug("No need to recalculate current patient data");
 				return;
 			}
 		}
 
 		// count the processed contacts
 		statistics.contactCounter += contactsByCPR.size();
 		
 		// ensure old data for this cpr number is removed before applying businessrules.
 		haibaDao.prepareCPRNumberForImport(cpr);
 		log.debug("Removed earlier processed admissions for CPR number");
 		
 		// Process the LPR data according to the defined business rules
 		rulesEngine.processRuleChain(contactsByCPR, statistics);
 		log.debug("Rules processed for CPR number");
 	}
 
 	public boolean isManualOverride() {
 		return manualOverride;
 	}
 
 	public void setManualOverride(boolean manualOverride) {
 		this.manualOverride = manualOverride;
 	}
 	
 }
