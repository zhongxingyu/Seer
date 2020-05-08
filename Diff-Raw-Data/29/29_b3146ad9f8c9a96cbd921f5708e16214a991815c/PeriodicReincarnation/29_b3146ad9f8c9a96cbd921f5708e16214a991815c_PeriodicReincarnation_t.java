 package org.jenkinsci.plugins.periodicreincarnation;
 
 import java.util.logging.Logger;
 
 import hudson.model.AsyncPeriodicWork;
 import hudson.model.Result;
 import hudson.model.Hudson;
 import hudson.model.Project;
 import hudson.model.TaskListener;
 import hudson.scheduler.CronTab;
 
 import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnationGlobalConfiguration.RegEx;
 
 import antlr.ANTLRException;
 
 import hudson.Extension;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.HashMap;
 
 /**
  * Main class of the periodic reincarnation plug-in. Method execute is called
  * every minute but further functionality and restart of failed jobs happens
  * only when the time specified in the cron tab overlaps with the current
  * minute.
  * 
  * @author yboev
  * 
  */
 @Extension
 public class PeriodicReincarnation extends AsyncPeriodicWork {
 
     /**
      * Logger for PeriodicReincarnation.
      */
     private static final Logger LOGGER = Logger
             .getLogger(PeriodicReincarnation.class.getName());
 
     /**
      * For every RegEx holds the projects being restarted because of it.
      */
     private HashMap<RegEx, ArrayList<Project<?, ?>>> regExRestartList;
 
     /**
      * Set with all projects scheduled for restart. Used to determine if a
      * project is already scheduled for restart.
      */
     private Set<String> scheduledProjects;
 
     /**
      * Contains the projects that will be restarted because there was no change
      * between the last builds and the last failed, but the second last was a
      * success.
      */
     private ArrayList<Project<?, ?>> unchangedRestartProjects;
 
     /**
      * Constructor.
      */
     public PeriodicReincarnation() {
         super("PeriodicReincarnation");
     }
 
     /**
      * This method is called every minute.
      * 
      * @param taskListener
      *            TaskListener
      */
     @Override
     protected void execute(TaskListener taskListener) {
         final PeriodicReincarnationGlobalConfiguration config = PeriodicReincarnationGlobalConfiguration
                 .get();
 
         // everything(cron regexs, enabled features) is configured in the
         // configuration, so exit if with error message if no configuration can
         // be retrieved. Should not happen!
         if (config == null) {
             LOGGER.warning("No configuration available...returning with nothing being done!");
             return;
         }
 
         final String cron = config.getCronTime();
 
         // if cron is not enabled just exit with no calculations.
         if (!config.isCronActive()) {
             return;
         }
 
         // Initialize the data structures where the to-be-restarted projects are
         // held temporarily.
         // Needed for sorting them by the reason for the restart.
         this.regExRestartList = new HashMap<RegEx, ArrayList<Project<?, ?>>>();
         this.scheduledProjects = new HashSet<String>();
         this.unchangedRestartProjects = new ArrayList<Project<?, ?>>();
 
         // record current time
         final long currentTime = System.currentTimeMillis();
 
         addProjectsFoundByRegExHit(currentTime);
 
         if (cron != null && config.isRestartUnchangedJobsEnabled()) {
             addUnchangedProjects(cron, currentTime);
         }
 
         printCronRestartProjects();
     }
 
     /**
      * Returns the number of projects scheduled for restart because of a RegEx
      * hit.
      * 
      * @return the number as int.
      */
     private int getNumberOfProjectsForRegExRestart() {
         int count = 0;
         for (RegEx regEx : this.regExRestartList.keySet()) {
             count += this.regExRestartList.get(regEx).size();
         }
         return count;
     }
 
     private int countProjectsForRestart() {
         return this.scheduledProjects.size();
     }
 
     private int getNumberOfProjectsForUnchangedRestart() {
         return this.unchangedRestartProjects.size();
     }
 
     /**
      * Prints all projects that are scheduleder for restart by this current cron
      * cycle. Groups them according to the reason they were restarted.
      */
     private void printCronRestartProjects() {
         // Initializ summary. This variables contains the whole output that
         // shows which projects have been restarted during this cron-cycle.
         String summary = "Periodic Reincarnation cron restart summary:" + "\n";
         summary += "Number of projects to restart: "
                 + this.countProjectsForRestart() + " (RegEx hit "
                 + this.getNumberOfProjectsForRegExRestart()
                 + ", Unchanged restart "
                 + this.getNumberOfProjectsForUnchangedRestart() + ")" + "\n";
 
         // Restarts all projects found by RegEx (also adds them to summary for
         // printing)
         summary += restartRegExProjects();
 
         // Restarts all projects found by no difference between the last two
         // builds criteria (also adds them to summary for printing)
         if (this.unchangedRestartProjects.size() > 0) {
             summary += restartUnchanged();
         }
         LOGGER.info(summary);
     }
 
     /**
      * Restarts all projects listed for unchanged restart.
      * 
      * @return the output that should be added to summary String.
      */
     private String restartUnchanged() {
         String summary = "No difference between the last two builds: "
                 + this.unchangedRestartProjects.size()
                 + " projects scheduled for restart" + "\n";
        StringBuilder sb = new StringBuilder(); 
         for (Project<?, ?> proj : this.unchangedRestartProjects) {
             Utils.restart(proj,
                     "(Cron restart) No difference between the last two builds",
                     null);
            sb.append("\t" + proj.getDisplayName() + "\n");
         }
        summary += sb.toString();
         return summary;
     }
 
     /**
      * Restarts all projects found via RegEx Hit.
      * 
      * @return the output that should be added to summary String.
      */
     private String restartRegExProjects() {
         String summary = "";
         for (RegEx regEx : this.regExRestartList.keySet()) {
             summary += getRestartCause(regEx) + ": "
                     + this.regExRestartList.get(regEx).size()
                     + " projects scheduled for restart" + "\n";
            StringBuilder sb = new StringBuilder();            
             for (Project<?, ?> proj : this.regExRestartList.get(regEx)) {
                 Utils.restart(proj, getRestartCause(regEx), regEx);
                sb.append("\t" + proj.getDisplayName() + "\n");
             }
            summary += sb.toString();
         }
         return summary;
     }
 
     /**
      * Returns the restart cause for a RegEx.
      * 
      * @param regEx
      *            the RegEx.
      * @return the restart cause as String.
      */
     private String getRestartCause(RegEx regEx) {
         String restartCause;
         if (regEx.getDescription() != null
                 && regEx.getDescription().length() > 1) {
             restartCause = regEx.getDescription();
         } else {
             restartCause = regEx.getValue();
         }
         return restartCause;
     }
 
     /**
      * Adds all projects found because of a regEx hit to the regExRestartList
      * Map.
      * 
      * @param currentTime
      *            current time, recorded previously.
      */
     private void addProjectsFoundByRegExHit(final long currentTime) {
         for (RegEx regEx : PeriodicReincarnationGlobalConfiguration.get()
                 .getRegExprs()) {
             if (regEx.isTimeToRestart(currentTime)) {
                 for (Project<?, ?> project : Hudson.getInstance().getProjects()) {
                     if (isValidCandidateForRestart(project)
                             && Utils.checkBuild(project.getLastBuild(), regEx)
                             && !scheduledProjects.contains(project
                                     .getFullDisplayName())) {
                         this.scheduledProjects
                                 .add(project.getFullDisplayName());
                         if (this.regExRestartList.containsKey(regEx)) {
                             this.regExRestartList.get(regEx).add(project);
                         } else {
                             ArrayList<Project<?, ?>> newList = new ArrayList<Project<?, ?>>();
                             newList.add(project);
                             this.regExRestartList.put(regEx, newList);
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Populates the array unchangedRestartProjects, that contains unchanged
      * projects failing for the first time.
      * 
      * @param cron
      *            cron parameter as String
      * @param currentTime
      *            current time recorded previously
      */
     private void addUnchangedProjects(final String cron, final long currentTime) {
         CronTab cronTab;
         try {
             cronTab = new CronTab(cron);
             if ((cronTab.ceil(currentTime).getTimeInMillis() - currentTime) == 0) {
                 for (Project<?, ?> project : Hudson.getInstance().getProjects()) {
                     if (isValidCandidateForRestart(project)
                             && Utils.qualifyForUnchangedRestart(project)
                             && !scheduledProjects.contains(project
                                     .getFullDisplayName())) {
                         this.scheduledProjects
                                 .add(project.getFullDisplayName());
                         this.unchangedRestartProjects.add(project);
                     }
                 }
             }
         } catch (ANTLRException e1) {
             LOGGER.warning("Global cron time could not be parsed!");
             e1.printStackTrace();
         }
     }
 
     /**
      * Determines if a project should be tested for RegEx match or no error
      * between the last two builds.
      * 
      * @param project
      *            the current project
      * @return true should be tested, false otherwise
      */
     private boolean isValidCandidateForRestart(final Project<?, ?> project) {
         return project != null
                 && project.isBuildable()
                 && project.getLastBuild() != null
                 && project.getLastBuild().getResult() != null
                 && project.getLastBuild().getResult()
                         .isWorseOrEqualTo(Result.FAILURE)
                 && !project.isBuilding() && !project.isInQueue();
     }
 
     /**
      * Recurrence will occur every minute, but action will be taken according to
      * the cron time set in the configuration. Only when this minute and the
      * cron tab time overlap.
      * 
      * @return The recurrence period in ms.
      */
     @Override
     public long getRecurrencePeriod() {
         return MIN;
     }
 
     /**
      * Returns this AsyncTask.
      * 
      * @return the instance of PeriodicReincarnation which is currently running
      */
     public static PeriodicReincarnation get() {
         return AsyncPeriodicWork.all().get(PeriodicReincarnation.class);
     }
 }
