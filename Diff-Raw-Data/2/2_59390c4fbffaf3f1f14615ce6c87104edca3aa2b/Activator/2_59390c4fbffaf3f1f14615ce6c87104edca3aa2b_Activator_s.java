 package de.unisiegen.informatik.bs.alvis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TreeSet;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import de.unisiegen.informatik.bs.alvis.editors.AlgorithmPartitionScanner;
 import de.unisiegen.informatik.bs.alvis.editors.EDecisionPoint;
 import de.unisiegen.informatik.bs.alvis.exceptions.VirtualMachineException;
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IDatatypeList;
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IExportItem;
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IFileExtension;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCBoolean;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCInteger;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCList;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCQueue;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCStack;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCString;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCVoid;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.SortableCollection;
 import de.unisiegen.informatik.bs.alvis.views.AlgorithmContainer;
 import de.unisiegen.informatik.bs.alvis.views.RunAlgorithm;
 import de.unisiegen.informatik.bs.alvis.vm.BPListener;
 import de.unisiegen.informatik.bs.alvis.vm.DPListener;
 import de.unisiegen.informatik.bs.alvis.vm.VirtualMachine;
 
 import de.unisiegen.informatik.bs.alvis.io.dialogs.*;
 
 /* Ein paar Notizen
  * 
  * Der Ordner in dem der Workspace liegt auf dem System:
  * Platform.getInstanceLocation().getURL().getPath();
  * 
  * 
  */
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 	// The plug-in ID
 	public static final String PLUGIN_ID = "de.unisiegen.informatik.bs.alvis"; //$NON-NLS-1$
 	/** Naming convention of partitioning */
 	public static final String ALGORITHM_PARTITIONING = "___algorithm__partitioning____"; //$NON-NLS-1$
 
 	// The shared instance
 	private static Activator plugin;
 
 	private AlgorithmPartitionScanner fPartitionsScanner;
 
 	private static boolean shutUpForExport;
 
 	private RunAlgorithm algorithmRunPerspective;
 
 	// private Export myExport = new Export();
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
 	/**
 	 * Attributes for a run.
 	 */
 	private Run activeRun;
 	private AlgorithmContainer algorithmContainer = new AlgorithmContainer();
 	private PCObject runObject;
 
 	public void setRunObject(PCObject runObject) {
 		this.runObject = runObject;
 	}
 
 	public PCObject getRunObject() {
 		return runObject;
 	}
 
 	public void setActiveRun(Run activeRun) {
 		this.activeRun = activeRun;
 	}
 
 	public Run getActiveRun() {
 		return this.activeRun;
 	}
 
 	/**
 	 * @return the AlgorithmPartition Scannner used for the Editor
 	 */
 	public AlgorithmPartitionScanner getAlgorithmPartitionScanner() {
 		if (fPartitionsScanner == null) {
 			fPartitionsScanner = new AlgorithmPartitionScanner();
 			return fPartitionsScanner;
 		}
 		return fPartitionsScanner;
 	}
 
 	private HashMap<String, PCObject> paraMap = new HashMap<String, PCObject>();
 
 	Shell shellContainer;
 
 	public void setPseudoCodeList(HashMap<String, PCObject> para) {
 		this.paraMap = para;
 	}
 
 	public HashMap<String, PCObject> getPseudoCodeList() {
 		return paraMap;
 	}
 
 	private VirtualMachine vm = VirtualMachine.getInstance();
 
 	// Storage for Decision Points
 	public int DPNr;
 	@SuppressWarnings("rawtypes")
 	public SortableCollection toSort;
 	public PCObject from;
 
 	public void runStart() {
 		if (!shutUpForExport) {
 			vm.removeAllBPListener();
 		}
 		vm.stopAlgos();
 		vm.setParameter("algo", paraMap);
 		vm.addBPListener(new BPListener() {
 			@Override
 			public void onBreakPoint(int BreakPointNumber) {
 
 				if (shutUpForExport)
 					return; // this deactivates the listener when run export
 							// works
 
 				Activator.getDefault().algorithmContainer
 						.removeAllCurrentLine();
 				Activator.getDefault().algorithmContainer
 						.addCurrentBP(BreakPointNumber);
 			}
 		});
 
 		vm.addDPListener(new DPListener() {
 			@Override
 			public void onDecisionPoint(int DPNr, PCObject from,
 					@SuppressWarnings("rawtypes") SortableCollection toSort) {
 
 				if (shutUpForExport)
 					return; // this deactivates the listener when run export
 							// works
 
 				// Check if the user wants to order the decisions
 				if (activeRun.getOnDecisionPoint().equals(EDecisionPoint.RAND))
 					return;
 				toSort.sort();
 				Activator.getDefault().DPNr = DPNr;
 				Activator.getDefault().toSort = toSort;
 				Activator.getDefault().from = from;
 
 				Runnable progress = new Runnable() {
 					public void run() {
 						Activator.getDefault().shellContainer = Display
 								.getDefault().getActiveShell();
 
 						Activator.getDefault().algorithmContainer
 								.removeAllCurrentLine();
 						Activator.getDefault().algorithmContainer
 								.addCurrentDP(Activator.getDefault().DPNr);
 
 						String name = Activator.getDefault().from.toString();
 						if (name == null)
 							name = Messages.Activator_DP_position_begin_algo;
 						if (Display.getDefault() != null) {
 							AskMeAgain ask = new AskMeAgain(true);
 							// OrderDialog can change the order of toSort
 							// or changes the attribute of ask.
 							OrderDialog toOrder = new OrderDialog(
 									shellContainer,
 									Activator.getDefault().toSort,
 									ask,
 									Messages.Activator_DP_order,
 									NLS.bind(
 											Messages.Activator_DP_current_position,
 											name),
 									Messages.Activator_DP_drag_and_drop);
 							toOrder.open();
 							if (ask.getAsk() == false) {
 								// the user hit the box ,,Do not askme again''
 								getActiveRun().setOnDecisionPoint(
 										EDecisionPoint.RAND);
 							}
 						}
 					}
 				};
 				Display.getDefault().syncExec(progress);
 
 			}
 		});
 
 		vm.startAlgos();
 	}
 
 	public void runNext() {
 		vm.stepAlgoForward();
 	}
 
 	public void runBack() {
 		// TODO change for multiple Algos
 		vm.stepAlgoBackward("algo"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Set the compiled .java algorithm path @throws VMException
 	 * 
 	 * @param pathToAlgoInJava
 	 */
 	public void setJavaAlgorithmToVM(String pathToFile, String fileName,
 			ArrayList<PCObject> datatypesToAddToClasspathAsPCObjects)
 			throws VirtualMachineException {
 
 		// Cycle through the list of delivered data types and extract the
 		// package they belong to
 		// TreeSet is used so that every packages is only added once
 		TreeSet<String> dynamicallyReferencedPackagesNeededToCompile = new TreeSet<String>();
 		for (Object obj : datatypesToAddToClasspathAsPCObjects) {
 			String path = obj.getClass().getProtectionDomain().getCodeSource()
 					.getLocation().getFile().toString();
 			if (path.endsWith(".jar")) //$NON-NLS-1$
 				dynamicallyReferencedPackagesNeededToCompile.add(path);
 			else
 				dynamicallyReferencedPackagesNeededToCompile.add(path + "src/"); //$NON-NLS-1$
 		}
 		
 		// add the path to the plugin org.eclipse.osgi.util
		dynamicallyReferencedPackagesNeededToCompile.add(Messages.class.getSuperclass().getProtectionDomain().getCodeSource().getLocation().toString());
 
 		try {
 			vm.addAlgoToVM("algo", pathToFile, fileName,
 					dynamicallyReferencedPackagesNeededToCompile);
 
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			throw new VirtualMachineException(
 					"Adding the algorithm to the Virtual Machine failed.");
 		}
 
 	}
 
 	/* ************************************************************************
 	 * DATATYPES AND PACKAGES OUT OF THE PLUGINS
 	 * ***********************************************************************
 	 */
 
 	private ArrayList<PCObject> allDatatypesInPlugIns = null;
 	private ArrayList<String> allDatatypesPackagesInPlugIns = null;
 
 	/**
 	 * Get an instance of each datatype, that is used in some plug-in. With this
 	 * instance you can get informations about the datatype.
 	 * 
 	 * @return a list with all datatypes in the plug-ins
 	 */
 	public ArrayList<PCObject> getAllDatatypesInPlugIns() {
 		if (allDatatypesInPlugIns == null)
 			registerAllDatatypes();
 
 		return allDatatypesInPlugIns;
 	}
 
 	/**
 	 * Get the names of the packages the datatypes from
 	 * getAllDatatypesInPlugIns() are in.
 	 * 
 	 * @return a list with all packagenames that contain datatypes in the
 	 *         plug-ins
 	 */
 	public ArrayList<String> getAllDatatypesPackagesInPlugIns() {
 		if (allDatatypesPackagesInPlugIns == null)
 			registerAllDatatypes();
 		return allDatatypesPackagesInPlugIns;
 	}
 
 	/**
 	 * Fill allDatatypesInPlugIns and allDatatypesPackagesInPlugIns with
 	 * datatypes and packagenames.
 	 */
 	@SuppressWarnings("rawtypes")
 	private void registerAllDatatypes() {
 		// The list to add all known datatypes
 		allDatatypesInPlugIns = new ArrayList<PCObject>();
 		// The list to add all known packages that contain datatypes
 		allDatatypesPackagesInPlugIns = new ArrayList<String>();
 
 		/*
 		 * ADD DATATYPES AND PACKAGENAMES FROM ALVIS-PLUG-INS
 		 */
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry
 				.getExtensionPoint("de.unisiegen.informatik.bs.alvis.extensionpoints.datatypelist"); //$NON-NLS-1$
 		IExtension[] extensions = extensionPoint.getExtensions();
 
 		// * For all Extensions that contribute:
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] elements = extension
 					.getConfigurationElements();
 			for (int j = 0; j < elements.length; j++) {
 				try {
 					IConfigurationElement element = elements[j];
 					IDatatypeList datatypes = (IDatatypeList) element
 							.createExecutableExtension("class"); //$NON-NLS-1$
 					// Save the found datatypes in allDatatypes
 					allDatatypesInPlugIns.addAll(datatypes
 							.getAllDatatypesInThisPlugin());
 					allDatatypesPackagesInPlugIns.addAll(datatypes
 							.getDatatypePackagesInThisPlugin());
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		/*
 		 * ADD DATATYPES THAT COME WITH ALVIS
 		 */
 		allDatatypesInPlugIns.add(new PCBoolean(true));
 		allDatatypesInPlugIns.add(new PCInteger(0));
 		// allDatatypes.add(new PCListIterator(null));
 		allDatatypesInPlugIns.add(new PCList());
 		allDatatypesInPlugIns.add(new PCStack());
 		allDatatypesInPlugIns.add(new PCQueue());
 		allDatatypesInPlugIns.add(new PCString("")); //$NON-NLS-1$
 		allDatatypesInPlugIns.add(new PCVoid());
 
 		/*
 		 * ADD THE DATATYPE-PACKAGENAMES THAT COME WITH ALVIS
 		 */
 		allDatatypesPackagesInPlugIns
 				.add("de.unisiegen.informatik.bs.alvis.primitive.datatypes"); //$NON-NLS-1$
 	}
 
 	/* ************************************************************************
 	 * RUNALGORITHM TOOLS
 	 * ***********************************************************************
 	 */
 	/**
 	 * @param algorithmContainer
 	 */
 	public void setAlgorithmContainer(AlgorithmContainer algorithmContainer) {
 		this.algorithmContainer = algorithmContainer;
 	}
 
 	/**
 	 * Get the AlgorithmContainer. The AlgorithmContainer contains surroundings
 	 * for the graphical represenation of the Algorithm that is currently
 	 * running. You can highlight lines as an example.
 	 * 
 	 * @return
 	 */
 	public AlgorithmContainer getAlgorithmContainer() {
 		return algorithmContainer;
 	}
 
 	/* ************************************************************************
 	 * EXPORT
 	 * ***********************************************************************
 	 */
 
 	// public void registerExport(IExportItem item) {
 	// myExport.register(item);
 	// }
 	//
 	// public ArrayList<IExportItem> getExportItems() {
 	// return myExport.getExportItems();
 	// }
 
 	/**
 	 * returns active editor as export item
 	 * 
 	 * @return active editor as export item
 	 */
 	public IExportItem getActivePartToExport() {
 
 		IWorkbenchPart myPart = getWorkbench().getActiveWorkbenchWindow()
 				.getActivePage().getActivePart();
 
 		if (myPart instanceof IExportItem)
 			return (IExportItem) myPart;
 
 		@SuppressWarnings("deprecation")
 		IViewPart[] parts = getWorkbench().getActiveWorkbenchWindow()
 				.getActivePage().getViews();
 		for (IViewPart iViewPart : parts) {
 			if (iViewPart instanceof IExportItem) {
 				return (IExportItem) iViewPart;
 			}
 		}
 
 		IEditorReference[] editors = null;
 		editors = getWorkbench().getActiveWorkbenchWindow().getActivePage()
 				.getEditorReferences();
 		int i;
 		for (i = 0; i < editors.length; i++) {
 			String editorTitle;
 			try {
 				editorTitle = getWorkbench().getActiveWorkbenchWindow()
 						.getActivePage().getActiveEditor().getTitle();
 			} catch (NullPointerException npe) {
 				break;// no editor in foreground, first one gets chosen
 			}
 			if (editors[i].getTitle().equals(editorTitle))
 				break;
 		}
 
 		if (editors.length == 0)
 			return null; // no editor opened
 
 		if (i == editors.length)
 			i = 0;
 
 		try {
 			getWorkbench()
 					.getActiveWorkbenchWindow()
 					.getActivePage()
 					.openEditor(editors[i].getEditorInput(), editors[i].getId());
 			IWorkbenchPart part = getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().getActivePart();
 			return (IExportItem) part;
 		} catch (PartInitException e) {
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Creates and returns an ArrayList<String> containing the file extensions
 	 * allowed as algorithm files in the run.
 	 * 
 	 * @return the ArrayList
 	 */
 
 	public ArrayList<String> getFileExtensions() {
 		ArrayList<String> fileextensions = new ArrayList<String>();
 
 		for (IExtension ext : Platform
 				.getExtensionRegistry()
 				.getExtensionPoint(
 						"de.unisiegen.informatik.bs.alvis.extensionpoints.fileextension").getExtensions()) { //$NON-NLS-1$
 			for (IConfigurationElement con : ext.getConfigurationElements()) {
 				try {
 					IFileExtension extension = (IFileExtension) con
 							.createExecutableExtension("class"); //$NON-NLS-1$
 					fileextensions.add(extension.getFileExtension());
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 		return fileextensions;
 	}
 
 	/**
 	 * Creates and returns a comma separated list in one string containing the
 	 * file extensions allowed as algorithm files in the run. This list is meant
 	 * for direct use in the GUI or during debugging.
 	 * 
 	 * @return
 	 */
 
 	public String getFileExtensionsAsCommaSeparatedList() {
 		String extensions = "";
 		ArrayList<String> fileextensions = Activator.getDefault()
 				.getFileExtensions();
 		for (int i = 0; i < fileextensions.size(); i++) {
 			if (i == 0) {
 				extensions += fileextensions.get(i);
 			} else {
 				extensions += ", " + fileextensions.get(i);
 			}
 		}
 
 		return extensions;
 	}
 
 	public boolean isShuttingUpForExport() {
 		return shutUpForExport;
 	}
 
 	public void shutUpForExport(boolean shutUpForExport) {
 		this.shutUpForExport = shutUpForExport;
 	}
 
 	public void setActiveRunAlgorithm(RunAlgorithm runAlgorithm) {
 		this.algorithmRunPerspective = runAlgorithm;
 	}
 
 	public RunAlgorithm getActiveRunAlgorithm() {
 		return this.algorithmRunPerspective;
 	}
 
 }
