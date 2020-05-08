 package info.mikaelsvensson.ftpbackup.command.job;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 public abstract class AbstractJobActionsGeneratorStrategy implements JobActionsGeneratorStrategy {
     private final Collection<JobActionsGeneratorStrategyListener> eventListeners = new LinkedList<>();
 
     @Override
     public void addJobActionsGeneratorStrategyListener(final JobActionsGeneratorStrategyListener listener) {
         eventListeners.add(listener);
     }
 
     @Override
     public void removeJobActionsGeneratorStrategyListener(final JobActionsGeneratorStrategyListener listener) {
         eventListeners.remove(listener);
     }
 
     void fireFoundModifiedFileEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
             listener.onFoundModifiedFile(event);
         }
     }
 
     void fireFoundAddedFileEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
             listener.onFoundAddedFile(event);
         }
     }
 
     void fireFoundDeletedFileEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
             listener.onFoundDeletedFile(event);
         }
     }
 
     void fireFoundFileToArchiveRemotelyEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
             listener.onFoundFileToArchiveRemotely(event);
         }
     }
 
     void fireFoundFileToSkipEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
             listener.onFoundFileToSkip(event);
         }
     }
 
     void fireFoundFileToUploadEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
             listener.onFoundFileToUpload(event);
         }
     }
     void fireFoundFolderToUploadEvent(final JobActionsGeneratorStrategyEvent event) {
         for (JobActionsGeneratorStrategyListener listener : eventListeners) {
            listener.onFoundFileToUpload(event);
         }
     }
 }
