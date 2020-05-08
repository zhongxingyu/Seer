 package uk.ac.ebi.fgpt.conan.ae.service;
 
 import uk.ac.ebi.fgpt.conan.ae.dao.SubmitterDetails;
 import uk.ac.ebi.fgpt.conan.ae.dao.SubmitterDetailsFromAE1DAO;
 import uk.ac.ebi.fgpt.conan.ae.dao.SubmitterDetailsFromAE2DAO;
 import uk.ac.ebi.fgpt.conan.model.ConanProcess;
 import uk.ac.ebi.fgpt.conan.model.ConanTask;
 import uk.ac.ebi.fgpt.conan.service.AbstractEmailResponderService;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * An implementation of {@link uk.ac.ebi.fgpt.conan.service.AbstractEmailResponderService} that generates email content
  * based on a number of arrayexpress recognised process types.
  *
  * @author Tony Burdett
  * @date 10-Nov-2010
  */
 public class ArrayExpressResponderService extends AbstractEmailResponderService {
     private SubmitterDetailsFromAE1DAO ae1SubmitterDetailsDAO;
     private SubmitterDetailsFromAE2DAO ae2SubmitterDetailsDAO;
 
     public SubmitterDetailsFromAE1DAO getAE1SubmitterDetailsDAO() {
         return ae1SubmitterDetailsDAO;
     }
 
     public void setAE1SubmitterDetailsDAO(SubmitterDetailsFromAE1DAO ae1SubmitterDetailsDAO) {
         this.ae1SubmitterDetailsDAO = ae1SubmitterDetailsDAO;
     }
 
     public SubmitterDetailsFromAE2DAO getAE2SubmitterDetailsDAO() {
         return ae2SubmitterDetailsDAO;
     }
 
     public void setAE2SubmitterDetailsDAO(SubmitterDetailsFromAE2DAO ae2SubmitterDetailsDAO) {
         this.ae2SubmitterDetailsDAO = ae2SubmitterDetailsDAO;
     }
 
     public boolean respondsTo(ConanTask task) {
         getLog().debug("Checking if a response is required to task ID '" + task.getId() + "' for current state");
         // has the current task failed?
         if (task.getCurrentState() == ConanTask.State.FAILED) {
             // we always notify of fails
             return true;
         }
         else {
             // first, check pipelines that issue notifications
             getLog().debug("Checking if response is required to task ID '" + task.getId() + "': " +
                                    "Pipeline is '" + task.getPipeline().getName() + "'");
 
             // respond to completed tasks
             if (task.getCurrentState() == ConanTask.State.COMPLETED) {
                 getLog().debug("Current state of task ID '" + task.getId() + "' is COMPLETE, requires response");
                 return true;
             }
 
             // respond to ae1 loads that have finished afterload
             if (task.getPipeline().getName().equalsIgnoreCase("AE1 Loading") ||
                     task.getPipeline().getName().equalsIgnoreCase("AE1 Loading with insert")) {
                 getLog().debug(
                         "Task ID '" + task.getId() + "' requires response if last process was 'AE1 Afterload'...");
                 if (task.getLastProcess().getName().equals("AE1 Afterload")) {
                     return true;
                 }
             }
 
             // respond to AE2/AE1 combined experiment loads at various states
             if (task.getPipeline().getName().equalsIgnoreCase("Experiment Loading (Combined AE2/Atlas)")) {
                 getLog().debug(
                         "Task ID '" + task.getId() + "' requires response if last process was 'afterloading'...");
                 if (task.getLastProcess().getName().equals("afterloading")) {
                     return true;
                 }
             }
 
             // anything else, we don't respond to
         }
 
         // no checks passed, so return false
         getLog().debug("No response required");
         return false;
     }
 
     protected String getEmailSubject(ConanTask task, ConanProcess process) {
         String response = "[conan2]";
         if (task.getCurrentState() == ConanTask.State.FAILED) {
             response = response + "[failure] Conan has a problem with task '" + task.getName() + "' - " +
                     task.getStatusMessage();
         }
         else {
             response = response + "[completion] Conan confirmation details for task '" + task.getName() + "'";
         }
         return response;
     }
 
     protected String getEmailContent(ConanTask task, ConanProcess process) {
         // if the task failed, email should be a failure notification to the submitter
         if (task.getCurrentState() == ConanTask.State.FAILED) {
             getLog().debug("Generating failure response for '" + task.getName() + "', " +
                                    "state = " + task.getCurrentState() + ", last process = " +
                                    task.getLastProcess().getName());
             return getFailureContent(task.getId(),
                                      task.getSubmitter().getFirstName(),
                                      task.getName(),
                                      task.getStatusMessage(),
                                      process.getName(),
                                      1,
                                      "Unknown host",
                                      new String[]{"Not available: no output captured"});
         }
         else {
             // respond to completed experiment loading runs
             if (task.getPipeline().getName().equalsIgnoreCase("AE2 Experiment Loading")) {
                 // notify on completion of this pipeline
                 if (task.getCurrentState() == ConanTask.State.COMPLETED) {
                     List<SubmitterDetails> details =
                             getAE2SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                     task.getName(), SubmitterDetails.ObjectType.EXPERIMENT);
                     getLog().debug("Generating confirmation response for '" + task.getName() + "', " +
                                            "state = " + task.getCurrentState() + ", last process = " +
                                            task.getLastProcess().getName());
                     return getConfirmationContent(task.getName(), details);
                 }
             }
 
             // respond to completed array loading runs
             if (task.getPipeline().getName().equalsIgnoreCase("AE2 ADF Loading")) {
                 // notify on completion of this pipeline
                 if (task.getCurrentState() == ConanTask.State.COMPLETED) {
                     List<SubmitterDetails> details =
                             getAE2SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                     task.getName(), SubmitterDetails.ObjectType.ARRAY_DESIGN);
                     getLog().debug("Generating default response for '" + task.getName() + "', " +
                                            "state = " + task.getCurrentState() + ", last process = " +
                                            task.getLastProcess().getName());
                     return getConfirmationContent(task.getName(),
                                                   details); //getDefaultContent(task.getName(), process.getName());
                 }
             }
 
             // respond to ae1 loads that have finished afterload
             if (task.getPipeline().getName().equalsIgnoreCase("AE1 Loading") ||
                     task.getPipeline().getName().equalsIgnoreCase("AE1 Loading with insert")) {
                 // notify if we've done afterload, and it didn't fail
                 if (task.getCurrentState() != ConanTask.State.FAILED &&
                         task.getLastProcess().getName().equals("AE1 Afterload")) {
                     List<SubmitterDetails> details =
                             getAE1SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                     task.getName(), SubmitterDetails.ObjectType.UNKNOWN);
                     getLog().debug("Generating confirmation response for '" + task.getName() + "', " +
                                            "state = " + task.getCurrentState() + ", last process = " +
                                            task.getLastProcess().getName());
                     return getConfirmationContent(task.getName(), details);
                 }
             }
 
             // respond to ae1 loads that have finished afterload
             if (task.getPipeline().getName().equalsIgnoreCase("Experiment Loading (Combined AE2/Atlas)")) {
                 // notify if we've done afterload, and it didn't fail
                 if (task.getCurrentState() != ConanTask.State.FAILED &&
                         task.getLastProcess().getName().equals("afterloading")) {
                     List<SubmitterDetails> details =
                             getAE2SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                     task.getName(), SubmitterDetails.ObjectType.EXPERIMENT);
                     getLog().debug("Generating confirmation response for '" + task.getName() + "', " +
                                            "state = " + task.getCurrentState() + ", last process = " +
                                            task.getLastProcess().getName());
                     return getConfirmationContent(task.getName(), details);
                 }
             }
 
             // otherwise, generate a stock response
             getLog().debug("Generating default response for '" + task.getName() + "', " +
                                    "state = " + task.getCurrentState() + ", last process = " +
                                    task.getLastProcess().getName());
             return getDefaultContent(task.getId(),
                                      task.getSubmitter().getFirstName(),
                                      task.getName(),
                                      process.getName());
         }
     }
 
     protected String getEmailContent(ConanTask task, ConanProcess process, ProcessExecutionException pex) {
         // if the task failed, email should be a failure notification to the submitter
         if (task.getCurrentState() == ConanTask.State.FAILED) {
             return getFailureContent(task.getId(),
                                      task.getSubmitter().getFirstName(),
                                      task.getName(),
                                      task.getStatusMessage(),
                                      process.getName(),
                                      pex.getExitValue(),
                                      pex.getProcessExecutionHost(),
                                      pex.getProcessOutput());
         }
         else {
             if (process.getName().equals("experiment loading")) {
                 List<SubmitterDetails> details =
                         getAE2SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                 task.getName(), SubmitterDetails.ObjectType.EXPERIMENT);
                 return getConfirmationContent(task.getName(), details);
             }
             else if (process.getName().equals("adf loading")) {
                 List<SubmitterDetails> details =
                         getAE2SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                 task.getName(), SubmitterDetails.ObjectType.ARRAY_DESIGN);
                 return getConfirmationContent(task.getName(), details);
             }
             else if (task.getLastProcess().getName().equals("AE1 Afterload")) {
                 List<SubmitterDetails> details =
                         getAE1SubmitterDetailsDAO().getSubmitterDetailsByAccession(
                                 task.getName(), SubmitterDetails.ObjectType.UNKNOWN);
                 return getConfirmationContent(task.getName(), details);
             }
             else {
                 return getDefaultContent(task.getId(),
                                          task.getSubmitter().getFirstName(),
                                          task.getName(),
                                          process.getName());
             }
         }
     }
 
     private String getFailureContent(String taskID,
                                      String taskOwnerFirstName,
                                      String taskName,
                                      String taskStatus,
                                      String processName,
                                      int processExitCode,
                                      String processExecutionHost,
                                      String[] processOutput) {
         StringBuilder stdout = new StringBuilder();
         for (String s : processOutput) {
             stdout.append(s).append("\n");
         }
 
         return "Hi " + taskOwnerFirstName + ",\n\n" +
                 "Conan encountered a problem that it could not handle, and requires your attention!\n\n" +
                 "Conan was doing task '" + taskName + "', but the process '" + processName + "' failed.\n\n" +
                 "This explanation was provided:\n\t" + taskStatus + "\n" +
                 "Please go to http://dukkha.ebi.ac.uk:14054/conan2/summary/" + taskID + " for more information.\n\n" +
                 "Process '" + processName + "' terminated with exit code " + processExitCode + " " +
                 "and ran on host '" + processExecutionHost + "'.\n" +
                 "The standard out of this process follows:\n" +
                 stdout.toString();
     }
 
     private String getDefaultContent(String taskID, String taskOwnerFirstName, String taskName, String processName) {
         return "Hi " + taskOwnerFirstName + ",\n\n" +
                 "This is confirmation that your task '" + taskName + "' successfully completed " + processName + ".\n" +
                 "A full report summary is available at http://dukkha.ebi.ac.uk:14054/conan2/summary/" + taskID;
     }
 
     public String getConfirmationContent(String taskName, List<SubmitterDetails> details) {
         String account_owners = "";
         boolean owners_created = false;
         String account_reviewers = "";
         boolean reviewers_created = false;
         String accession = "";
         String name = "";
         Date releaseDate = null;
         Date today = null;
 
         String releaseDateString = "";
         String activationDate = "";
         boolean public_record = false;
         String AE_URL = "http://www.ebi.ac.uk/arrayexpress/";
 
         AE_URL = AE_URL + (accession.startsWith("E") ? "experiments/" : "arrays/") +
                 accession;
 
         for (SubmitterDetails submitter : details) {
             accession = submitter.getAccession();
             name = submitter.getName();
 
             try {
               DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
               releaseDate = df.parse(submitter.getReleaseDate());
               releaseDateString = df2.format(releaseDate);
 
               Calendar cal = Calendar.getInstance();
               today = cal.getTime();
               cal.setTime(releaseDate);
               cal.add(Calendar.HOUR, 24);
 
              if (cal.getTime().after(today))
                 public_record = true;
 
 
             }
             catch (Exception e){
               releaseDateString =  submitter.getReleaseDate();
             }
 
             activationDate = submitter.getActivationDate();
             if (!public_record){
               if (submitter.getUsername().startsWith("Reviewer")) {
                     if (account_reviewers.length() == 0) {
                         account_reviewers = "We have also created an additional user account for a reviewer(s).  " +
                                 "Please provide this login to journals for reviewer access. " +
                                 "Do not provide your own login details.\n\n";
                     }
                     account_reviewers = account_reviewers + "Reviewer's user account:\n\n" +
                             "Username: " + submitter.getUsername() + "\n" +
                             "Password: " + submitter.getPassword() + "\n\n";
                     reviewers_created = true;
                 }
                 else {
                     if (!submitter.getUsername().equals("guest")) {
                         if (account_owners.length() == 0) {
                             account_owners = "You can access your private data via a user account, " +
                                     "which has been created for you.\n" +
                                     "This is your ArrayExpress user account and should " +
                                     "not be shared.\n\n";
                         }
                         account_owners = account_owners + "Your user account details are as follows:\n\n" +
                                 "Username: " + submitter.getUsername() + "\n" +
                                 "Password: " + submitter.getPassword() + "\n" +
                                 "E-mail address: " + submitter.getEmail() + "\n\n";
                         owners_created = true;
                     }
                     else {
                         public_record = true;
                     }
                 }
             }
         }
         if (!details.isEmpty()) {
             return "Dear ArrayExpress submitter,\n\n" +
                     "Your " +
                     (accession.startsWith("E") ? "experiment" : "array design") + " " +
                     "submitted to ArrayExpress has " +
                     "been loaded successfully " +
                     (public_record ? "with the following details:\n\n" :
                             "and will be kept private till the release date given below:\n\n") +
 
                     (accession.startsWith("E") ? "Experiment" : "Array design") + " " +
                     "name: " + name + "\n" +
                     "ArrayExpress accession: " + accession + "\n" +
                     "Specified release date: " + releaseDateString + ".\n\n" +
                     (public_record ? "" :
                             "You will get a reminder email one month before the release " +
                                     "date but you can change the release date at any time by emailing " +
                                     "us at miamexpress@ebi.ac.uk with the " +
                                     (accession.startsWith("E") ? "experiment" : "array design") + " " +
                                     "accession and a " +
                                     "revised release date. If the release date is within one month you " +
                                     "will not be sent a reminder.\n\n") +
 
                     (owners_created ? account_owners +
                             "From http://www.ebi.ac.uk/arrayexpress/, go to the link \"Submitter/reviewer login\" " +
                             "login using your account details and query for the accession " +
                             "number of your experiment or array design.\n\n"
                             : "Your " + (accession.startsWith("E") ? "experiment" : "array design")
                                     + " is available under this link " + AE_URL) +
                     (reviewers_created ? account_reviewers : "") +
                     ((owners_created || reviewers_created) ?
                             "These user accounts will be activated on " + activationDate + " at " +
                                     "approximately 06:00 GMT.\n\n" : "") +
                     "If you have any queries please contact the curation team\n" +
                     "at (miamexpress@ebi.ac.uk). Please remember to update your " +
                     "submission with publication details when\n" +
                     "these are available by emailing to miamexpress@ebi.ac.uk.\n\n" +
                     "Thank you for submitting to ArrayExpress.\n\n\n";
         }
         else {
             return "Conan was fetching the submitter details for '" + taskName +
                     "' from database and found nothing.\n\n" +
                     "You can check the details by using one of the sql queries below.\n\n" +
                     "EXPERIMENTS:\n" +
                     "select exp.ACC, exp.TITLE, exp.RELEASEDATE, usr.USERNAME, usr.USERPASSWORD, usr.USEREMAIL, usr.NOTE " +
                     "from SC_LABEL lbl, SC_OWNER own, SC_USER usr, STUDY exp" +
                     "where lbl.ID = own.SC_LABEL_ID " +
                     "and own.SC_USER_ID = usr.ID " +
                     "and exp.ACC = lbl.NAME " +
                     "and lbl.NAME = \'" + taskName + "\';\n\n" +
                     "ARRAY DESIGNS:\n" +
                     "select ad.ACC, ad.NAME, ad.RELEASEDATE, usr.USERNAME, usr.USERPASSWORD, usr.USEREMAIL, usr.NOTE " +
                     "from SC_LABEL lbl, SC_OWNER own, SC_USER usr, PLAT_DESIGN ad " +
                     "where lbl.ID = own.SC_LABEL_ID " +
                     "and own.SC_USER_ID = usr.ID " +
                     "and ad.ACC = lbl.NAME " +
                     "and lbl.NAME = \'" + taskName + "\';\n";
         }
     }
 }
