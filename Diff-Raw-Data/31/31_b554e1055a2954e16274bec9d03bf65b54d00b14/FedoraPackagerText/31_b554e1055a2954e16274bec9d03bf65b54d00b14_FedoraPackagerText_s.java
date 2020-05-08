 package org.fedoraproject.eclipse.packager;
 
 import org.fedoraproject.eclipse.packager.nls.NLS;
 import org.fedoraproject.eclipse.packager.nls.TranslationBundle;
 
 /**
  * Translation bundle for FedoraPackager core.
  */
 public class FedoraPackagerText extends TranslationBundle {
 
 	/**
 	 * @return an instance of this translation bundle
 	 */
 	public static FedoraPackagerText get() {
 		return NLS.getBundleFor(FedoraPackagerText.class);
 	}
 
 	/****/ public String commandWasCalledInTheWrongState;
 	// ConsoleWriterThread
 	/****/ public static String consoleWriterThread_ioFail;
 	// DownloadJob
 	/****/ public static String downloadJob_name;
 	/****/ public static String downloadJob_fileDoesNotExist;
 	/****/ public static String downloadJob_badHostname;
 	// CommonHandler Strings
 	/****/ public static String commonHandler_fedoraPackagerName;
 	/****/ public static String commonHandler_branchAlreadyTaggedMessage;
 	// DownloadHandler Strings
 	/****/ public static String downloadHandler_jobName;
 	/****/ public static String downloadHandler_downloadSourceTask;
 	// NewSourcesHandler Strings
 	/****/ public static String newSourcesHandler_jobName;
 	/****/ public static String newSourcesHandler_taskName;
 	/****/ public static String newSourcesHandler_invalidFile;
 	/****/ public static String newSourcesHandler_failUpdateSourceFile;
 	/****/ public static String newSourcesHandler_failVCSUpdate;
 	// UploadHandler Strings
 	/****/ public static String uploadHandler_taskName;
 	/****/ public static String uploadHandler_versionExists;
 	/****/ public static String uploadHandler_invalidFile;
 	/****/ public static String uploadHandler_failUpdatSourceFile;
 	/****/ public static String uploadHandler_failVCSUpdate;
 	/****/ public static String uploadHandler_checkingRemoteStatus;
 	/****/ public static String uploadHandler_uploadFail;
 	/****/ public static String uploadHandler_fileAlreadyUploaded;
 	/****/ public static String uploadHandler_progressMsg;
 	/****/ public static String uploadHandler_invalidUrlError;
 	// WGetHandler Strings
 	/****/ public static String wGetHandler_nothingToDownload;
 	/****/ public static String wGetHandler_badMd5sum;
 	/****/ public static String wGetHandler_couldNotCreate;
 	/****/ public static String wGetHandler_couldNotRefresh;
 	// UploadCommand
 	/****/ public static String uploadSourceCommand_uploadFileUnspecified;
 	/****/ public static String uploadSourceCommand_uploadFileInvalid;
 	// DownloadSourceCommand
 	/****/ public String downloadSourceCommand_nothingToDownload;
 	/****/ public String downloadSourceCommand_downloadFile;
 	/****/ public String downloadSourceCommand_downloadFileError;
 	/****/ public String downloadSourceCommand_invalidURL;
 	/****/ public String downloadSourceCommand_downloadingFileXofY;
 	// SourcesFile
 	/****/ public String sourcesFile_saveFailedMsg;
 	// Generics
 	/****/ public String somethingUnexpectedHappenedError;
 }
