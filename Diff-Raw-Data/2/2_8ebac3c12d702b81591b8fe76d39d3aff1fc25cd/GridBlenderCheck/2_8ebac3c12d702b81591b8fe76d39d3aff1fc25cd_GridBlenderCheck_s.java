 package org.vpac.grisu.clients.blender;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.client.control.clientexceptions.FileTransferException;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.model.job.MultiPartJobEventListener;
 import org.vpac.grisu.frontend.model.job.MultiPartJobObject;
 
 import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
 import uk.co.flamingpenguin.jewel.cli.Cli;
 import uk.co.flamingpenguin.jewel.cli.CliFactory;
 
 public class GridBlenderCheck implements BlenderMode, MultiPartJobEventListener {
 
 	private BlenderCheckCommandLineArgs commandlineArgs = null;
 	private final ServiceInterface si;
 
 	public GridBlenderCheck(String[] args) {
 
 		final Cli<BlenderCheckCommandLineArgs> cli = CliFactory
 				.createCli(BlenderCheckCommandLineArgs.class);
 
 		try {
 			commandlineArgs = cli.parseArguments(args);
 			if (commandlineArgs.getHelp()) {
 				System.out.println(cli.getHelpMessage());
 				System.exit(1);
 			}
 		} catch (ArgumentValidationException e) {
 			System.err.println("Could not start blender-job-monitor:\n"
 					+ e.getLocalizedMessage() + "\n");
 			System.out.println(cli.getHelpMessage());
 			System.exit(1);
 		}
 
 		if (!commandlineArgs.isJobname()) {
 			System.out.println("Jobname not specified.");
 			System.out.println(cli.getHelpMessage());
 			System.exit(1);
 		}
 
 		if (commandlineArgs.isDownloadResults()) {
 			try {
 				File downloadDir = commandlineArgs.getDownloadResults();
 				if (downloadDir.exists()) {
 					if (!downloadDir.canWrite()) {
 						System.out
 								.println("Can't write to specified output directory "
 										+ downloadDir.toString() + ".");
 						System.exit(1);
 					}
 				}
 			} catch (Exception e) {
 				System.out
 						.println("Could not access specified download directory.");
 			}
 		}
 
 		si = GridBlenderUtils.login(commandlineArgs);
 
 	}
 
 	public void execute() {
 
 		System.out.println("Retrieving job " + commandlineArgs.getJobname()
 				+ ". This might take a while...");
 
 		GrisuBlenderJob blenderJob = null;
 		try {
 			blenderJob = new GrisuBlenderJob(si, commandlineArgs.getJobname());
 		} catch (Exception e) {
 			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		MultiPartJobObject blenderMultiPartJobObject = blenderJob
 				.getMultiPartJobObject();
 		blenderMultiPartJobObject.addJobStatusChangeListener(this);
 
 		boolean firstTime = true;
 
 		if ( commandlineArgs.isLoopUntilFinished() || firstTime ) {
 
 			int sleepTime = 60 * 1000;
 			try {
 				sleepTime = commandlineArgs.getLoopUntilFinished() * 60 * 1000;
 			} catch (Exception e) {
 //				e.printStackTrace();
 				// doesn't matter
 			}
 
 			while (!blenderMultiPartJobObject.isFinished(false) || firstTime) {
 
 				if (!firstTime) {
 					if (sleepTime / 60000 == 1) {
 						System.out.println("Sleeping for " + sleepTime / 60000
 								+ " minute.");
 					} else {
 						System.out.println("Sleeping for " + sleepTime / 60000
 								+ " minutes.");
 					}
 
 					try {
 						Thread.sleep(sleepTime);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 				firstTime = false;
 
 				blenderMultiPartJobObject.refresh();
 				if (commandlineArgs.isDetailed()) {
 
 					System.out.println(blenderMultiPartJobObject.getDetails());
 
 				}
 
 				if (commandlineArgs.isStatus()) {
 					System.out.println(blenderMultiPartJobObject
 							.getProgress(null));
 				}
 				
				if ( blenderMultiPartJobObject.isFinished(false) ) {
 					break;
 				}
 
 				if (commandlineArgs.isDownloadResults()) {
 					System.out
 							.println("Downloading already finished frames to: "
 									+ commandlineArgs.getDownloadResults()
 											.toString());
 					downloadCurrentlyFinishedFiles(blenderMultiPartJobObject);
 				}
 
 			}
 
 			if (blenderMultiPartJobObject.isFinished(false)) {
 
 				if (commandlineArgs.isDownloadResults()) {
 
 					downloadCurrentlyFinishedFiles(blenderMultiPartJobObject);
 
 				}
 			}
 
 		}
 
 	}
 
 	private void downloadCurrentlyFinishedFiles(
 			MultiPartJobObject blenderMultiPartJobObject) {
 		File downloadDirectory = commandlineArgs.getDownloadResults();
 
 		String pattern = blenderMultiPartJobObject
 				.getJobProperty(GrisuBlenderJob.BLENDER_OUTPUTFILENAME_KEY);
 		if (StringUtils.isBlank(pattern)) {
 			System.out
 					.println("Could not determine output filename. Exiting...");
 			System.exit(1);
 		}
 		String[] patterns = new String[] { pattern };
 		try {
 			System.out.println("Downloading output files that match \""
 					+ pattern + "\".\n");
 			blenderMultiPartJobObject.downloadResults(true, downloadDirectory,
 					patterns, false, false);
 			System.out.println("Downloads finished.");
 		} catch (Exception e) {
 			System.out.println("Could not download results: "
 					+ e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void eventOccured(MultiPartJobObject job, String eventMessage) {
 		System.out.println(eventMessage);
 	}
 
 }
