 package cytoscape.visual.customgraphic;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import cytoscape.Cytoscape;
 import cytoscape.logger.CyLogger;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.visual.customgraphic.impl.bitmap.URLImageCustomGraphics;
 
 public class PersistImageTask implements Task {
 	private File location;
 	private TaskMonitor taskMonitor;
 
 	private static final int TIMEOUT = 1000;
 	private static final int NUM_THREADS = 4;
 	
 	private static final CyLogger logger = CyLogger.getLogger();
 
 	/**
 	 * Constructor.<br>
 	 * 
 	 * @param fileName
 	 *            Absolute path to the Session file.
 	 */
 	protected PersistImageTask(File location) {
 		this.location = location;
 	}
 
 	/**
 	 * Execute task.<br>
 	 */
 	public void run() {
 		taskMonitor
 				.setStatus("Saving image library to your local disk.\n\nPlease wait...");
 		taskMonitor.setPercentCompleted(-1);
 		// Remove all existing files
 		final File[] files = location.listFiles();
		for (File old : files)
			old.delete();
 
 		final long startTime = System.currentTimeMillis();
 		final CustomGraphicsManager pool = Cytoscape.getVisualMappingManager()
 				.getCustomGraphicsManager();
 
 		final ExecutorService exService = Executors
 				.newFixedThreadPool(NUM_THREADS);
 
 		for (CyCustomGraphics cg : pool.getAll()) {
 			
 			// Save ONLY bitmap image Custom Graphics.
 			if (cg instanceof NullCustomGraphics
 					|| cg instanceof URLImageCustomGraphics == false)
 				continue;
 
 			final Image img = cg.getRenderedImage();
 			if (img != null) {
 				try {
 					exService.submit(new SaveImageTask(location, cg.getIdentifier().toString(),
 							ImageUtil.toBufferedImage(img)));
 				} catch (Exception e) {
					e.printStackTrace();
 				}
 			}
 			
 		}
 
 		try {
 			exService.shutdown();
 			exService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			taskMonitor.setException(e, "Image saving task interrupted.");
 		}
 
 		try {
 			pool.getMetadata().store(new FileOutputStream(new File(location, CustomGraphicsManager.METADATA_FILE)),
 					"Image Metadata");
 		} catch (IOException e) {
 			taskMonitor.setException(e, "Could not save image metadata.");
			e.printStackTrace();
 		}
 
 		long endTime = System.currentTimeMillis();
 		double sec = (endTime - startTime) / (1000.0);
 		logger.info("Image saving process finished in " + sec + " sec.");
 	}
 
 	/**
 	 * Halts the Task: Not Currently Implemented.
 	 */
 	public void halt() {
 		// Task can not currently be halted.
 	}
 
 	/**
 	 * Sets the Task Monitor.
 	 * 
 	 * @param taskMonitor
 	 *            TaskMonitor Object.
 	 */
 	public void setTaskMonitor(TaskMonitor taskMonitor)
 			throws IllegalThreadStateException {
 		this.taskMonitor = taskMonitor;
 	}
 
 	/**
 	 * Gets the Task Title.
 	 * 
 	 * @return Task Title.
 	 */
 	public String getTitle() {
 		return "Saving Image Library";
 	}
 } // End of SaveSessionTask
