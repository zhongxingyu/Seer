 package com.robonobo.gui.tasks;
 
 import java.io.File;
 import java.util.*;
 
 import com.robonobo.core.RobonoboController;
 import com.robonobo.core.api.RobonoboException;
 import com.robonobo.core.api.Task;
 import com.robonobo.core.api.model.Stream;
 
 public class ImportFilesTask extends Task {
 	private List<File> files = new ArrayList<File>();
 	private RobonoboController control;
 
 	public ImportFilesTask(RobonoboController control, List<File> files) {
 		this.control = control;
 		this.files = files;
 		title = "Importing " + files.size() + " files";
 	}
 
 	@Override
 	public void runTask() throws Exception {
 		log.info("Running import files task for "+files.size()+" files");
 		List<String> streamIds = new ArrayList<String>();
 		int totalSz = files.size();
 		int i = 0;
 		Iterator<File> it = files.iterator();
 		try {
 			while (it.hasNext()) {
 				if (cancelRequested) {
 					cancelConfirmed();
 					return;
 				}
 				completion = (float) i / totalSz;
 				i++;
 				statusText = "Importing file " + i + " of " + totalSz;
 				fireUpdated();
 
 				File f = it.next();
 				String filePath = f.getAbsolutePath();
 				Stream s = null;
 				try {
 					s = control.addShare(filePath);
 				} catch (RobonoboException e) {
					log.error("Error adding share from file " + filePath+": "+e.getMessage());
 					continue;
 				} finally {
 					it.remove();
 				}
 				streamIds.add(s.getStreamId());
 			}
 			statusText = "Done.";
 			completion = 1f;
 			fireUpdated();
 		} finally {
 			streamsAdded(streamIds);
 		}
 	}
 
 	protected void streamsAdded(List<String> streamIds) {
 		// Default impl does nothing
 	}
 }
