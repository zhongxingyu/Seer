 package de.ptb.epics.eve.editor.jobs.file;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Locale;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.progress.UIJob;
 
 import de.ptb.epics.eve.data.measuringstation.filter.ExcludeFilter;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.data.scandescription.processors.ScanDescriptionSaver;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.gef.ScanDescriptionEditor;
 
 /**
  * <code>Save</code> is a {@link org.eclipse.core.runtime.jobs.Job} to save 
  * a {@link de.ptb.epics.eve.data.scandescription.ScanDescription} into a file.
  * 
  * @author Marcus Michalsky
  * @since 1.1
  */
 public class Save extends Job {
 	
 	private final static Logger logger = Logger.getLogger(Save.class.getName());
 	
 	private final String family = "file";
 	
 	private String filename;
 	private ScanDescription scanDescription;
 	private EditorPart editor;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param name the name of the job
 	 * @param filename the filename where the scan description should be saved
 	 * @param scanDescription the scan description that should be saved
 	 * @param editor the current editor
 	 */
 	public Save(String name, String filename, ScanDescription scanDescription, 
 			EditorPart editor) {
 		super(name);
 		this.filename = filename;
 		this.scanDescription = scanDescription;
 		this.editor = editor;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		try {
 			monitor.beginTask(this.getName(), 3);
 			monitor.subTask("creating file");
 			File saveFile = new File(filename);
 			final FileOutputStream os = new FileOutputStream(saveFile);
 			monitor.worked(1);
 			
 			if(monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			if(logger.isDebugEnabled()) {
 				Thread.sleep(500);
 			}
 			
 			// do the filtering
 			monitor.subTask("running filter");
 			ExcludeFilter measuringStation = new ExcludeFilter();
 			measuringStation.setSource(scanDescription.getMeasuringStation());
 			measuringStation.excludeUnusedDevices(scanDescription);
 			monitor.worked(1);
 			
 			if(monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			if(logger.isDebugEnabled()) {
 				Thread.sleep(500);
 			}
 			
 			// save the file
 			monitor.subTask("saving scml file");
 			final ScanDescriptionSaver scanDescriptionSaver = 
 					new ScanDescriptionSaver(
 						os, measuringStation, this.scanDescription);
 			boolean success = scanDescriptionSaver.save();
 			if(success) {
 				logger.info("Save was successful.");
 			} else {
 				logger.error("Save Error!");
 				return Status.CANCEL_STATUS;
 			}
 			monitor.worked(1);
 			
 			if(logger.isDebugEnabled()) {
 				Thread.sleep(500);
 				calculateSavings();
 			}
 		} catch(FileNotFoundException fnfe) {
 			logger.error(fnfe.getMessage(), fnfe);
 			UIJob reportError = new ReportError(fnfe.getMessage(), this.editor);
 			reportError.schedule();
 			return Status.CANCEL_STATUS;
 		} catch (InterruptedException e) {
 			logger.error(e.getMessage(), e);
 			return Status.CANCEL_STATUS;
 		} finally {
 			monitor.done();
 		}
 		
 		UIJob refreshEditorState = new RefreshEditorState("", this.editor);
 		refreshEditorState.schedule();
 		
 		return Status.OK_STATUS;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean belongsTo(Object family) {
 		return family.equals(this.family);
 	}
 	
 	/*
 	 * saves a "full" scan description without filtering and calculates the 
 	 * sizes of the filtered and unfiltered file. A debug level log message 
 	 * contains the saving (in percent).
 	 */
 	private void calculateSavings() throws FileNotFoundException {
 		File tempFile = new File((filename + ".temp"));
		final FileOutputStream os_temp = new FileOutputStream(tempFile);
 		// save the whole file without filtering
 		final ScanDescriptionSaver scanDescriptionSaverFull = 
			new ScanDescriptionSaver(os_temp, 
 				scanDescription.getMeasuringStation(), this.scanDescription);
 		scanDescriptionSaverFull.save();
 		// get the size of the unfiltered file
 		long full = tempFile.length();
 		
 		// determine filtered file size
 		long filtered = new File(filename).length();
 		
 		// format to percent
 		NumberFormat form = 
 			NumberFormat.getPercentInstance(new Locale("de-DE"));
 		
 		// log the result
 		logger.debug("File size reduced due to filtering: " + 
 			(form.format(new Double(((double)(full - filtered))/full))) + 
 			" (" + new DecimalFormat("#.##").format((float)filtered/1024) + 
 			"kB of " + new DecimalFormat("#.##").format((float)full/1024) + 
 			"kB remain)");
 		
 		// delete the temp file
 		tempFile.delete();
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.1
 	 */
 	private class RefreshEditorState extends UIJob {
 		
 		private EditorPart editor;
 		
 		/**
 		 * Constructor.
 		 * 
 		 * @param name the name of the job
 		 * @param editor the editor that should be refreshed
 		 */
 		public RefreshEditorState(String name, EditorPart editor) {
 			super(name);
 			this.editor = editor;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			((ScanDescriptionEditor)this.editor).resetEditorState(filename);
 			((ScanDescriptionEditor) this.editor).getCommandStack()
 					.markSaveLocation();
 			return Status.OK_STATUS;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean belongsTo(Object family) {
 			return family.equals(family);
 		}
 	}
 	
 	/**
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.6
 	 */
 	private class ReportError extends UIJob {
 
 		private String message;
 		private EditorPart editor;
 		
 		public ReportError(String message, EditorPart editor) {
 			super("Save");
 			this.message = message;
 			this.editor = editor;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					this.message);
 			ErrorDialog.openError(this.editor.getSite().getShell(),
 					"Save Error", "Save Error", status);
 			return Status.OK_STATUS;
 		}
 	}
 }
