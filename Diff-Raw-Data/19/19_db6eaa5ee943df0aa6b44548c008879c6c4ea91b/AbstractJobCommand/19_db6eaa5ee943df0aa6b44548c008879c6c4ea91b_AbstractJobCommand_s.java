 package info.mikaelsvensson.ftpbackup.command.job;
 
 import info.mikaelsvensson.ftpbackup.command.*;
 import info.mikaelsvensson.ftpbackup.command.fileprocessing.FileProcessingStrategy;
 import info.mikaelsvensson.ftpbackup.command.fileprocessing.FileProcessingStrategyException;
 import info.mikaelsvensson.ftpbackup.command.jobaction.JobAction;
 import info.mikaelsvensson.ftpbackup.command.jobaction.JobActionException;
 import info.mikaelsvensson.ftpbackup.command.jobactiongenerator.JobActionsGeneratorStrategy;
 import info.mikaelsvensson.ftpbackup.model.*;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemBaseFolder;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFile;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFolder;
 import info.mikaelsvensson.ftpbackup.model.filesystem.LightFileSystemObject;
 import info.mikaelsvensson.ftpbackup.report.*;
 import info.mikaelsvensson.ftpbackup.util.CalendarUtil;
 import info.mikaelsvensson.ftpbackup.util.I18n;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 
 public abstract class AbstractJobCommand extends AbstractCommand<JobCommandResult> implements CommandCancellationListener {
 // ------------------------------ FIELDS ------------------------------
 
     private static final Logger LOGGER = Logger.getLogger(AbstractJobCommand.class);
 
     private final Job job;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     AbstractJobCommand(Job job) {
         super(job.getFileSet());
         this.job = job;
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     private FileSystemFolder getRemoteFiles(FileProcessingStrategy strategy, String currentPath, FileSystemFolder folder) throws IOException, CancellationRequestedException {
         LOGGER.debug(I18n.text(I18n.CORE_STRINGS, "JobCommand.looking.for.files.in.remote.folder.0.of.1", currentPath, strategy.getDestinationId()));
         fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.processing.destination.0.looking.for.files.in.remote.folder.1", strategy.getDestinationId(), currentPath));
 
         if (isCancellationRequested()) {
             throw new CancellationRequestedException();
         }
 
         for (LightFileSystemObject dir : strategy.getDirectoriesInWorkingDirectory()) {
 //            if (!dir.getName().equals(".") && !dir.getName().equals("..") && dir.isDirectory()) {
             strategy.changeWorkingDirectory(dir.getName());
             FileSystemFolder subFolder = null;//dir;//new FileSystemFolder(dir.getName(), dir.getTimestamp());
             if (dir instanceof FileSystemFolder) {
                 subFolder = (FileSystemFolder) dir;
             } else {
                 subFolder = new FileSystemFolder(dir.getName(), dir.getTimeStamp());
             }
             folder.addChild(subFolder);
             getRemoteFiles(strategy, currentPath + strategy.getDirectorySeparator() + dir.getName(), subFolder);
             strategy.changeToParentDirectory();
 //            }
         }
         Collection<? extends LightFileSystemObject> files = strategy.getFilesInWorkingDirectory();
         for (LightFileSystemObject file : files) {
             if (file instanceof FileSystemFile) {
                 folder.addChild((FileSystemFile) file);
             } else {
                 folder.addChild(new FileSystemFile(file.getName(), file.getTimeStamp(), file.getSize()));
             }
         }
         return folder;
     }
 
     void fireCommandProgressEvent(final String text) {
         fireCommandProgressEvent(new CommandEvent<>(this, new CommandProgress(text)));
     }
 
     @Override
     public JobCommandResult performImpl() {
         Date commandStartDate = CalendarUtil.now().getTime();
 
        Collection<Report> reports = createReports(job.getReportConfigurations());
 
         fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.building.local.file.list"));
 
         if (job.getDestinations().size() == 0) {
             return new JobCommandResult(CommandResultCode.FAILURE, I18n.text(I18n.CORE_STRINGS, "JobCommand.no.destinations.specified"), Collections.<Report>emptyList());
         }
 
         try {
             performDestinationActions(commandStartDate, reports);
         } catch (CancellationRequestedException e) {
             return new JobCommandResult(I18n.text(I18n.CORE_STRINGS, "JobCommand.command.cancelled.by.user"), e);
         }
 
         generateReports(reports);
 
         String doneMessage = I18n.text(I18n.CORE_STRINGS, "JobCommand.done");
         fireCommandProgressEvent(doneMessage);
         return new JobCommandResult(CommandResultCode.SUCCESS, doneMessage, reports);
     }
 
    Collection<Report> createReports(final Collection<? extends ReportConfiguration> reportConfigurations) {
         fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.initating.reports"));
 
         Collection<Report> reports = new LinkedList<>();
         for (ReportConfiguration reportConfiguration : reportConfigurations) {
            try {
                reports.add(createReport(reportConfiguration));
            } catch (ReportException e) {
                LOGGER.warn(e);
            }
         }
         return reports;
     }
 
     Report createReport(ReportConfiguration configuration) throws ReportException {
         Date backupStartDate = CalendarUtil.now().getTime();
         if (configuration instanceof ConsoleReportConfiguration) {
             ConsoleReportConfiguration cfg = (ConsoleReportConfiguration) configuration;
 
             return new ConsoleReport(
                     job.getName(),
                     cfg.isStandardError(),
                     cfg.getBodyTemplate(),
                     cfg.getBodyDestinationTemplate(),
                     backupStartDate);
         } else if (configuration instanceof EmailReportConfiguration) {
             EmailReportConfiguration cfg = (EmailReportConfiguration) configuration;
 
             return new EmailReport(
                     job.getName(),
                     cfg.getTo(),
                     cfg.getTitleTemplate(),
                     cfg.getBodyTemplate(),
                     cfg.getBodyDestinationTemplate(),
                     backupStartDate,
                     cfg.isRemoteActionsLogAttached(),
                     cfg.getRemoteActionsLogDefaultItemFormat(),
                     cfg.getRemoteActionsLogMovedItemFormat());
         } else if (configuration instanceof RemoteActionsLogFileReportConfiguration) {
             RemoteActionsLogFileReportConfiguration cfg = (RemoteActionsLogFileReportConfiguration) configuration;
 
             return new RemoteActionsLogFileReport(
                     cfg.getFile(),
                     cfg.getDefaultFormat(),
                     cfg.getMovedFileFormat(),
                     job.getName());
         } else if (configuration instanceof LocalFileSetReportConfiguration) {
             LocalFileSetReportConfiguration cfg = (LocalFileSetReportConfiguration) configuration;
 
             return new LocalFileSetReport(
                     cfg.getFile(),
                     cfg.getFormat(),
                     job.getName());
         } else {
             throw new ReportException("Does not understand report type " + configuration.getClass().getSimpleName());
         }
     }
 
     void performDestinationActions(final Date commandStartDate, final Collection<Report> reports) throws CancellationRequestedException {
         int i = 1;
         for (Destination destination : job.getDestinations()) {
             if (StringUtils.isEmpty(destination.getId())) {
                 destination.setId(I18n.text(I18n.CORE_STRINGS, "JobCommand.default.destination.id.format", i++, destination.getClass().getSimpleName().toLowerCase()));
             }
 
             fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.processing.destination.0", destination.getId()));
 
             FileProcessingStrategy fileProcessingStrategy = null;
             try {
                 fileProcessingStrategy = createStrategy(destination);
                 fileProcessingStrategy.connect();
                 JobActionsGeneratorStrategy jobActionsGeneratorStrategy = createJobActionsGeneratorStrategy(job.getFileSet(), fileProcessingStrategy);
                 if (jobActionsGeneratorStrategy != null) {
                     for (Report report : reports) {
                         fileProcessingStrategy.addFileProcessingStrategyListener(report);
                         jobActionsGeneratorStrategy.addJobActionsGeneratorStrategyListener(report);
                         localFileSystemFolderReader.addFolderReaderListener(report);
                     }
 
                     FileSystemFolder remoteBaseFolder = getRemoteFiles(fileProcessingStrategy, destination);
 
                     fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.creating.job.actions"));
 
                     Collection<JobAction> actions = jobActionsGeneratorStrategy.createJobActions(job.getFileSet().getPathToBaseFolderFromRoot(), remoteBaseFolder, commandStartDate);
 
                     int x = 1;
                     for (JobAction action : actions) {
                         fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.processing.destination.0.performing.1.task.2.of.3", destination.getId(), action.toString(), x++, actions.size()));
                         action.run();
                     }
 
                     for (Report report : reports) {
                         fileProcessingStrategy.removeFileProcessingStrategyListener(report);
                         jobActionsGeneratorStrategy.removeJobActionsGeneratorStrategyListener(report);
                         localFileSystemFolderReader.removeFolderReaderListener(report);
                     }
 
                     fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.all.job.actions.performed.successfully"));
 
                     LOGGER.info(I18n.text(I18n.CORE_STRINGS, "JobCommand.remote.file.set.contains.0.files", remoteBaseFolder.getFileCount(true)));
                 } else {
                     LOGGER.info(I18n.text(I18n.CORE_STRINGS, "JobCommand.could.not.decide.how.to.generate.actions"));
                 }
             } catch (IOException e) {
                 fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.execution.aborted.due.to.0", e.getMessage()));
                 LOGGER.error(e.getMessage(), e);
             } catch (JobActionException e) {
                 fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.execution.aborted.due.to.0", e.getMessage()));
                 LOGGER.error(e.getMessage(), e);
             } catch (FileProcessingStrategyException e) {
                 fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.execution.aborted.due.to.0", e.getMessage()));
                 LOGGER.error(e.getMessage(), e);
             } catch (CancellationRequestedException e) {
//                LOGGER.error(e.getMessage(), e);
                 throw e;
             } finally {
                 if (fileProcessingStrategy != null) {
                     try {
                         fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.disconnecting.from.destination.0", destination.getId()));
                         fileProcessingStrategy.disconnect();
                         LOGGER.info(I18n.text(I18n.CORE_STRINGS, "JobCommand.disconnected.from.destination.0", destination.getId()));
                     } catch (IOException e) {
                         LOGGER.debug(e.getMessage(), e);
                     }
                 }
             }
         }
     }
 
     private FileSystemFolder getRemoteFiles(FileProcessingStrategy strategy, Destination destination) throws IOException, CancellationRequestedException {
         String pathToBaseFromRoot = destination.getPathToBaseFromRoot();
         return getRemoteFiles(strategy, strategy.getWorkingDirectory(), new FileSystemBaseFolder(pathToBaseFromRoot, strategy.getDirectorySeparator()));
     }
 
     void generateReports(final Collection<Report> reports) {
         fireCommandProgressEvent(I18n.text(I18n.CORE_STRINGS, "JobCommand.generating.reports"));
 
         for (Report report : reports) {
             report.generate();
         }
     }
 }
