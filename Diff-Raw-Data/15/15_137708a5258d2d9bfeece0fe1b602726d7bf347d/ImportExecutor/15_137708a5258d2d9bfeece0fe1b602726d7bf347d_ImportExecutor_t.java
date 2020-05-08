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
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import dk.nsi.haiba.lprimporter.dao.LPRDAO;
 import dk.nsi.haiba.lprimporter.log.Log;
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
 	
 	@Autowired
 	LPRDAO lprdao;
 
 	@Autowired
 	RulesEngine rulesEngine;
 	
 	@Autowired
 	ImportStatusRepository statusRepo;
 	
 	@Scheduled(fixedDelay = 10000)
 	public void run() {
 		log.trace("Running Importer: " + new Date().toString());
 		doProcess();
 	}
 
 	
 	/*
	 * Separated into its own method for testing purpose, because testing a scheduled method isn't good
 	 */
 	public void doProcess() {
 		// Fetch new records from LPR contact table
 		try {
 			if(lprdao.hasUnprocessedCPRnumbers()) {
 				statusRepo.importStartedAt(new DateTime());
 
 				List<String> unprocessedCPRnumbers = lprdao.getCPRnumberBatch(batchsize);
 				while(unprocessedCPRnumbers.size() > 0) {
 					log.debug("processing "+unprocessedCPRnumbers.size()+ " cprnumbers");
 					for (String cpr : unprocessedCPRnumbers) {
 						List<Administration> contactsByCPR = lprdao.getContactsByCPR(cpr);
 						// Process the LPR data according to the defined business rules
 						rulesEngine.processRuleChain(contactsByCPR);
 					}
 					// fetch the next batch
 					unprocessedCPRnumbers = lprdao.getCPRnumberBatch(batchsize);
 				}
 				statusRepo.importEndedWithSuccess(new DateTime());
 			}
 			
 		} catch(Exception e) {
 			statusRepo.importEndedWithFailure(new DateTime(), e.getMessage());
 			log.error("", e);
 		}
 	}
 }
