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
 package dk.nsi.haiba.epimibaimporter.importer;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import dk.nsi.haiba.epimibaimporter.dao.ClassificationCheckDAO;
 import dk.nsi.haiba.epimibaimporter.dao.DefaultClassificationCheckDAOColumnMapper;
 import dk.nsi.haiba.epimibaimporter.dao.HAIBADAO;
 import dk.nsi.haiba.epimibaimporter.email.EmailSender;
 import dk.nsi.haiba.epimibaimporter.log.Log;
 import dk.nsi.haiba.epimibaimporter.model.CaseDef;
 import dk.nsi.haiba.epimibaimporter.model.Header;
 import dk.nsi.haiba.epimibaimporter.model.Isolate;
 import dk.nsi.haiba.epimibaimporter.model.Quantitative;
 import dk.nsi.haiba.epimibaimporter.status.CurrentImportProgress;
 import dk.nsi.haiba.epimibaimporter.status.ImportStatusRepository;
 import dk.nsi.haiba.epimibaimporter.ws.EpimibaWebserviceClient;
 import dk.nsi.stamdata.jaxws.generated.Answer;
 import dk.nsi.stamdata.jaxws.generated.ArrayOfPIsolate;
 import dk.nsi.stamdata.jaxws.generated.ArrayOfPQuantitative;
 import dk.nsi.stamdata.jaxws.generated.PIsolate;
 import dk.nsi.stamdata.jaxws.generated.PQuantitative;
 
 /*
  * Scheduled job, responsible for fetching new data from LPR, then send it to the RulesEngine for further processing
  */
 public class ImportExecutor {
     private static Log log = new Log(Logger.getLogger(ImportExecutor.class));
 
     private boolean manualOverride;
 
     @Autowired
     HAIBADAO haibaDao;
 
     @Autowired
     ImportStatusRepository statusRepo;
 
     @Autowired
     EpimibaWebserviceClient epimibaWebserviceClient;
 
     @Autowired
     EmailSender emailSender;
 
     @Autowired
     ClassificationCheckDAO classificationCheckDAO;
 
     @Autowired
     CurrentImportProgress currentImportProgress;
 
     private Comparator<? super Answer> aSortAnswersByTransactionIdComparator = new SortAnswersByTransactionIdComparator();
 
     @Scheduled(cron = "${cron.import.job}")
     public void run() {
         if (!isManualOverride()) {
             log.debug("Running Importer: " + new Date().toString());
             doProcess();
         } else {
             log.debug("Importer must be started manually");
         }
     }
 
     /*
      * Separated into its own method for testing purpose, because testing a scheduled method isn't good
      */
     public void doProcess() {
         // Fetch new records from LPR contact table
         try {
             statusRepo.importStartedAt(new DateTime());
             currentImportProgress.reset();
 
             // update tab tables first, in order to copy proper values into location/classification tables for used
             // values
             currentImportProgress.addStatusLine("importing and storing analysis data");
             haibaDao.clearAnalysisTable();
             haibaDao.saveAnalysis(epimibaWebserviceClient.getClassifications("Analysis"));
 
             currentImportProgress.addStatusLine("importing and storing investigation data");
             haibaDao.clearInvestigationTable();
             haibaDao.saveInvestigation(epimibaWebserviceClient.getClassifications("Investigation"));
 
             currentImportProgress.addStatusLine("importing and storing labsection data");
             haibaDao.clearLabSectionTable();
             haibaDao.saveLabSection(epimibaWebserviceClient.getClassifications("LabSection"));
 
             currentImportProgress.addStatusLine("importing and storing locations data");
             haibaDao.clearLocationTable();
             haibaDao.saveLocation(epimibaWebserviceClient.getClassifications("Locations"));
 
             currentImportProgress.addStatusLine("importing and storing organization data");
             haibaDao.clearOrganizationTable();
             haibaDao.saveOrganization(epimibaWebserviceClient.getClassifications("Organization"));
 
             currentImportProgress.addStatusLine("importing and storing microorganism data");
             haibaDao.clearMicroorganismTable();
             haibaDao.saveMicroorganism(epimibaWebserviceClient.getClassifications("Microorganism"));
 
             // iterate through case definitions from casedef table
             CaseDef[] caseDefArray = haibaDao.getCaseDefs();
             for (CaseDef caseDef : caseDefArray) {
                 currentImportProgress.addStatusLine("importing and storing headers for " + caseDef.getText() + "/"
                         + caseDef.getId());
                 boolean hasAnswers = true;
                 log.debug("testing for " + caseDef);
                 String lastStatus = null;
                 while (hasAnswers) {
                     long latestTransactionId = haibaDao.getLatestTransactionId(caseDef.getId());
                     List<Answer> answers = epimibaWebserviceClient.getAnswers(latestTransactionId + 1, caseDef.getId());
                     // be sure that answers are sorted by transaction id - else failure/recovery breaks as unordered
                     // transaction ids allows answers overtaking each other and potentially prohibiting fetching of all
                     // answers
                     Collections.sort(answers, aSortAnswersByTransactionIdComparator);
                     String status = "read " + (answers != null ? answers.size() : 0) + " answers for "
                             + caseDef.getText();
                     if (status.equals(lastStatus)) {
                         currentImportProgress.addProgressDot();
                     } else {
                         currentImportProgress.addStatusLine(status);
                     }
                     if (answers == null || answers.size() == 0) {
                         log.debug("No more answers on " + caseDef);
                         hasAnswers = false;
                     } else {
                         log.debug("got " + answers.size());
                         for (Answer answer : answers) {
                             log.debug(answer.getCprnr());
                            if (!answer.isTestPt()) {
                                 Header header = getHeader(answer);
                                 haibaDao.saveHeader(header, answer.getTransactionID().longValue(), caseDef.getId());
                             } else {
                                 log.debug("not saving header for test patient " + answer.getCprnr());
                             }
                         }
                     }
                 }
             }
             currentImportProgress.addStatusLine("checking for new alnr/banr");
             Collection<String> alnrInNewAnswers = haibaDao.getAllAlnr();
             Collection<String> banrInNewAnswers = haibaDao.getAllBanr();
             checkAndSendEmailOnNewImports(alnrInNewAnswers, banrInNewAnswers);
             currentImportProgress.addStatusLine("done");
 
             statusRepo.importEndedWithSuccess(new DateTime());
         } catch (Exception e) {
             log.error("", e);
             statusRepo.importEndedWithFailure(new DateTime(), e.getMessage());
         }
     }
 
     private void checkAndSendEmailOnNewImports(Collection<String> alnrInNewAnswers, Collection<String> banrInNewAnswers) {
         Collection<String> unknownBanrSet = classificationCheckDAO.checkClassifications(banrInNewAnswers, "Banr",
                 new MyBanrClassificationCheckMapper());
         Collection<String> unknownAlnrSet = classificationCheckDAO.checkClassifications(alnrInNewAnswers, "Alnr",
                 new MyAlnrClassificationCheckMapper());
 
         if (!unknownBanrSet.isEmpty() || !unknownAlnrSet.isEmpty()) {
             currentImportProgress.addStatusLine("sending email about " + unknownBanrSet.size() + " new banr and "
                     + unknownAlnrSet.size() + " new alnr to " + emailSender.getTo());
             log.debug("send email about new banr=" + unknownBanrSet + " or new alnr=" + unknownAlnrSet);
             emailSender.send(unknownBanrSet, unknownAlnrSet);
         }
     }
 
     // private void updateAlnrBanr(Header header, Set<String> alnrInNewAnswers, Set<String> banrInNewAnswers) {
     // alnrInNewAnswers.add(header.getAlnr());
     // for (Isolate isolate : header.getIsolates()) {
     // banrInNewAnswers.add(isolate.getBanr());
     // }
     // }
 
     private Header getHeader(Answer answer) {
         Header h = new Header();
         h.setHeaderId(answer.getHeaderId());
         h.setAlnr(answer.getLocationAlnr());
         h.setAvd(answer.getAvd());
         h.setCprnr(answer.getCprnr().replace("-", ""));
         h.setEvaluationText(answer.getEvaluationText());
         h.setExtid(answer.getExtId());
         if (answer.getIndate() != null) {
             h.setInDate(answer.getIndate().toGregorianCalendar().getTime());
         }
         h.setLabnr("" + answer.getLabnr());
         h.setLar("" + answer.getLar());
         h.setMgkod(answer.getMgkod());
         h.setPname(answer.getPname());
         if (answer.getPrdate() != null) {
             h.setPrDate(answer.getPrdate().toGregorianCalendar().getTime());
         }
         h.setRefnr(answer.getRefnr());
         h.setResult(answer.getResult());
         // h.setStnr() - TODO not found in answer
         h.setUsnr(answer.getUsnr());
 
         ArrayOfPIsolate isolates = answer.getIsolates();
         List<PIsolate> pIsolate = isolates.getPIsolate();
         log.debug("Adding " + pIsolate.size() + " isolates");
         for (PIsolate isolate : pIsolate) {
             Isolate i = new Isolate();
             i.setBanr(isolate.getIsolateBanr());
             i.setIsolateId(isolate.getIsolateId());
             i.setQuantity(isolate.getIsolateQuantity());
             log.debug("Adding isolate: " + isolate.getIsolateId());
             h.addIsolate(i);
         }
 
         ArrayOfPQuantitative quantitatives = answer.getQuantitatives();
         List<PQuantitative> pQuantitative = quantitatives.getPQuantitative();
         log.debug("Adding " + pQuantitative.size() + " quantitatives");
         for (PQuantitative quantitative : pQuantitative) {
             Quantitative q = new Quantitative();
             q.setQuantitativeId(quantitative.getQuantitativeId());
             q.setAnalysis(quantitative.getQuantitativeAnalysis());
             q.setComment(quantitative.getQuantitativeComment());
             q.setEvaluationText(quantitative.getQuantitativeEvaluationText());
             q.setQtnr("" + quantitative.getQuantitativeQtnr()); // TODO shouldn't this be an int?
             q.setQuantity(quantitative.getQuantitativeQuantity());
             log.debug("Adding quantitative: " + quantitative.getQuantitativeId());
             h.addQuantitative(q);
         }
 
         return h;
     }
 
     public boolean isManualOverride() {
         return manualOverride;
     }
 
     public void setManualOverride(boolean manualOverride) {
         this.manualOverride = manualOverride;
     }
 
     private final class MyBanrClassificationCheckMapper extends DefaultClassificationCheckDAOColumnMapper {
         public MyBanrClassificationCheckMapper() {
             super("Klass_microorganism", "Tabmicroorganism", new String[] { "TabmicroorganismId", "Banr", "Text" },
                     "Banr");
         }
     }
 
     private final class MyAlnrClassificationCheckMapper extends DefaultClassificationCheckDAOColumnMapper {
         public MyAlnrClassificationCheckMapper() {
             super("Klass_Location", "TabLocation", new String[] { "TabLocationId", "Alnr", "Text" }, "Alnr");
         }
     }
 
     public static class SortAnswersByTransactionIdComparator implements Comparator<Answer> {
         @Override
         public int compare(Answer a1, Answer a2) {
             return a1.getTransactionID().compareTo(a2.getTransactionID());
         }
     }
 }
