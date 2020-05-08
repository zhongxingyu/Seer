 package cytoscape.actions;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import javax.swing.JOptionPane;
 
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.InternalFrameComponent;
 import cytoscape.util.CytoscapeAction;
 
 import cytoscape.util.FileUtil;
 import cytoscape.util.CyFileFilter;
 
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.util.TaskManager;
 
 import cytoscape.util.export.Exporter;
 import cytoscape.util.export.BitmapExporter;
 import cytoscape.util.export.PDFExporter;
 import cytoscape.util.export.SVGExporter;
 import cytoscape.dialogs.ExportBitmapOptionsDialog;
 
 /**
  * Action for exporting a network view to bitmap or vector graphics.
  * @author Samad Lotia
  */
 public class ExportAsGraphicsAction extends CytoscapeAction
 {
 	private static ExportFilter BMP_FILTER = new BitmapExportFilter("bmp", "BMP");
 	private static ExportFilter JPG_FILTER = new BitmapExportFilter("jpg", "JPEG");
 	private static ExportFilter PDF_FILTER = new PDFExportFilter();
 	private static ExportFilter PNG_FILTER = new BitmapExportFilter("png", "PNG");
 	private static ExportFilter SVG_FILTER = new SVGExportFilter();
 	private static ExportFilter[] FILTERS = { BMP_FILTER, JPG_FILTER, PDF_FILTER, PNG_FILTER, SVG_FILTER };
 
 	private static String TITLE = "Network View as Graphics";
 
 	public ExportAsGraphicsAction()
 	{
 		super(TITLE + "...");
 		setPreferredMenu("File.Export");
 		setAcceleratorCombo(KeyEvent.VK_P, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		// Show the file chooser
 		File[] files = FileUtil.getFiles(TITLE, FileUtil.SAVE, FILTERS, null, null, false, true);
 		if (files == null || files.length == 0 || files[0] == null)
 			return;
 		File file = files[0];
 
 		// Create the file stream
 		FileOutputStream stream = null;
 		try
 		{
 			stream = new FileOutputStream(file);
 		}
 		catch (Exception exp)
 		{
 			JOptionPane.showMessageDialog(	Cytoscape.getDesktop(),
 							"Could not create file " + file.getName()
 							+ "\n\nError: " + exp.getMessage());
 			return;
 		}
 
 		// Export
 		while (true)
 		{
 			CyNetworkView view = Cytoscape.getCurrentNetworkView();
 			for (int i = 0; i < FILTERS.length; i++)
 			{
 				if (FILTERS[i].accept(file))
 				{
 					FILTERS[i].export(view, stream);
					break;
 				}
 			}
 
 			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 				"An appropriate extension was not specified for the file.\n\n" +
 				"Please specify an extension for the desired format.");
 		}
 	}
 }
 
 class ExportTask
 {
 	public static void run(	final String title,
 				final Exporter exporter,
 				final CyNetworkView view,
 				final FileOutputStream stream)
 	{
 		// Create the Task
 		Task task = new Task()
 		{
 			TaskMonitor monitor;
 
 			public String getTitle()
 			{
 				return title;
 			}
 
 			public void setTaskMonitor(TaskMonitor monitor)
 			{
 				this.monitor = monitor;
 			}
 
 			public void halt()
 			{
 			}
 
 			public void run()
 			{
 				try
 				{
 					exporter.export(view, stream);
 				}
 				catch (IOException e)
 				{
 					monitor.setException(e, "Could not complete export of network");
 				}
 			}
 		};
 		
 		// Execute the task
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.displayCancelButton(false);
 		jTaskConfig.displayCloseButton(false);
 		jTaskConfig.displayStatus(false);
 		jTaskConfig.displayTimeElapsed(true);
 		jTaskConfig.displayTimeRemaining(false);
 		jTaskConfig.setAutoDispose(true);
 		jTaskConfig.setModal(true);
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 }
 
 abstract class ExportFilter extends CyFileFilter
 {
 	public ExportFilter(String extension, String description)
 	{
 		super(extension, description);
 	}
 
 	public boolean isExtensionListInDescription()
 	{
 		return true;
 	}
 
 	public abstract void export(CyNetworkView view, FileOutputStream stream);
 }
 
 class PDFExportFilter extends ExportFilter
 {
 	public PDFExportFilter()
 	{
 		super("pdf", "PDF");
 	}
 	public void export(final CyNetworkView view, final FileOutputStream stream)
 	{
 		PDFExporter exporter = new PDFExporter();
 		ExportTask.run("Exporting to PDF", exporter, view, stream);
 	}
 }
 
 class BitmapExportFilter extends ExportFilter
 {
 	private String extension;
 
 	public BitmapExportFilter(String extension, String description)
 	{
 		super(extension, description);
 		this.extension = extension;
 	}
 
 	public void export(final CyNetworkView view, final FileOutputStream stream)
 	{
 		final InternalFrameComponent ifc = Cytoscape.getDesktop().getNetworkViewManager().getInternalFrameComponent(view);
 		final ExportBitmapOptionsDialog dialog = new ExportBitmapOptionsDialog(ifc.getWidth(), ifc.getHeight());
 		ActionListener listener = new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				BitmapExporter exporter = new BitmapExporter(extension, dialog.getZoom());
 				dialog.dispose();
 				ExportTask.run("Exporting to " + extension, exporter, view, stream);
 			}
 		};
 		dialog.addActionListener(listener);
 		dialog.setVisible(true);
 	}
 }
 
 class SVGExportFilter extends ExportFilter
 {
 	public SVGExportFilter()
 	{
 		super("svg", "SVG");
 	}
 
 	public void export(final CyNetworkView view, final FileOutputStream stream)
 	{
 		SVGExporter exporter = new SVGExporter();
 		ExportTask.run("Exporting to SVG", exporter, view, stream);
 	}
 }
