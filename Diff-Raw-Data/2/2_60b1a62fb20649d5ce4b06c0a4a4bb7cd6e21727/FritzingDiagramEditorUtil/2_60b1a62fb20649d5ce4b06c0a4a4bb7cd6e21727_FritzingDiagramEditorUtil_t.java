 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.part;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
 import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
 import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.fritzing.fritzing.DocumentRoot;
 import org.fritzing.fritzing.FritzingFactory;
 import org.fritzing.fritzing.Sketch;
 import org.fritzing.fritzing.diagram.edit.parts.SketchEditPart;
 
 import com.ice.jni.registry.Registry;
 import com.ice.jni.registry.RegistryKey;
 
 /**
  * @generated
  */
 /**
  * @author andre
  *
  */
 public class FritzingDiagramEditorUtil {
 
 	/**
 	 * @generated
 	 */
 	public static Map getSaveOptions() {
 		Map saveOptions = new HashMap();
 		saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
 				Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 		return saveOptions;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static boolean openDiagram(Resource diagram)
 			throws PartInitException {
 		IWorkbenchPage page = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage();
 		page.openEditor(new URIEditorInput(diagram.getURI()),
 				FritzingDiagramEditor.ID);
 		return true;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static String getUniqueFileName(IPath containerFullPath,
 			String fileName, String extension) {
 		if (containerFullPath == null) {
 			containerFullPath = new Path(""); //$NON-NLS-1$
 		}
 		if (fileName == null || fileName.trim().length() == 0) {
 			fileName = "default"; //$NON-NLS-1$
 		}
 		IPath filePath = containerFullPath.append(fileName);
 		if (extension != null && !extension.equals(filePath.getFileExtension())) {
 			filePath = filePath.addFileExtension(extension);
 		}
 		extension = filePath.getFileExtension();
 		fileName = filePath.removeFileExtension().lastSegment();
 		int i = 1;
 		while (filePath.toFile().exists()) {
 			i++;
 			filePath = containerFullPath.append(fileName + i);
 			if (extension != null) {
 				filePath = filePath.addFileExtension(extension);
 			}
 		}
 		return filePath.lastSegment();
 	}
 
 	/**
 	 * Allows user to select file and loads it as a model.
 	 * 
 	 * @generated
 	 */
 	public static Resource openModel(Shell shell, String description,
 			TransactionalEditingDomain editingDomain) {
 		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
 		if (description != null) {
 			fileDialog.setText(description);
 		}
 		fileDialog.open();
 		String fileName = fileDialog.getFileName();
 		if (fileName == null || fileName.length() == 0) {
 			return null;
 		}
 		if (fileDialog.getFilterPath() != null) {
 			fileName = fileDialog.getFilterPath() + File.separator + fileName;
 		}
 		URI uri = URI.createFileURI(fileName);
 		Resource resource = null;
 		try {
 			resource = editingDomain.getResourceSet().getResource(uri, true);
 		} catch (WrappedException we) {
 			FritzingDiagramEditorPlugin.getInstance().logError(
 					"Unable to load resource: " + uri, we); //$NON-NLS-1$
 			MessageDialog
 					.openError(
 							shell,
 							Messages.FritzingDiagramEditorUtil_OpenModelResourceErrorDialogTitle,
 							NLS
 									.bind(
 											Messages.FritzingDiagramEditorUtil_OpenModelResourceErrorDialogMessage,
 											fileName));
 		}
 		return resource;
 	}
 
 	/**
 	 * Runs the wizard in a dialog.
 	 * 
 	 * @generated
 	 */
 	public static void runWizard(Shell shell, Wizard wizard, String settingsKey) {
 		IDialogSettings pluginDialogSettings = FritzingDiagramEditorPlugin
 				.getInstance().getDialogSettings();
 		IDialogSettings wizardDialogSettings = pluginDialogSettings
 				.getSection(settingsKey);
 		if (wizardDialogSettings == null) {
 			wizardDialogSettings = pluginDialogSettings
 					.addNewSection(settingsKey);
 		}
 		wizard.setDialogSettings(wizardDialogSettings);
 		WizardDialog dialog = new WizardDialog(shell, wizard);
 		dialog.create();
 		dialog.getShell().setSize(Math.max(500, dialog.getShell().getSize().x),
 				500);
 		dialog.open();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static Resource createDiagram(URI diagramURI, URI modelURI,
 			IProgressMonitor progressMonitor) {
 		TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE
 				.createEditingDomain();
 		progressMonitor
 				.beginTask(
 						Messages.FritzingDiagramEditorUtil_CreateDiagramProgressTask,
 						3);
 		final Resource diagramResource = editingDomain.getResourceSet()
 				.createResource(diagramURI);
 		final Resource modelResource = editingDomain.getResourceSet()
 				.createResource(modelURI);
 		final String diagramName = diagramURI.lastSegment();
 		AbstractTransactionalCommand command = new AbstractTransactionalCommand(
 				editingDomain,
 				Messages.FritzingDiagramEditorUtil_CreateDiagramCommandLabel,
 				Collections.EMPTY_LIST) {
 			protected CommandResult doExecuteWithResult(
 					IProgressMonitor monitor, IAdaptable info)
 					throws ExecutionException {
 				Sketch model = createInitialModel();
 				attachModelToResource(model, modelResource);
 
 				Diagram diagram = ViewService.createDiagram(model,
 						SketchEditPart.MODEL_ID,
 						FritzingDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
 				if (diagram != null) {
 					diagramResource.getContents().add(diagram);
 					diagram.setName(diagramName);
 					diagram.setElement(model);
 				}
 
 				try {
 					modelResource
 							.save(org.fritzing.fritzing.diagram.part.FritzingDiagramEditorUtil
 									.getSaveOptions());
 					diagramResource
 							.save(org.fritzing.fritzing.diagram.part.FritzingDiagramEditorUtil
 									.getSaveOptions());
 				} catch (IOException e) {
 
 					FritzingDiagramEditorPlugin.getInstance().logError(
 							"Unable to store model and diagram resources", e); //$NON-NLS-1$
 				}
 				return CommandResult.newOKCommandResult();
 			}
 		};
 		try {
 			OperationHistoryFactory.getOperationHistory().execute(command,
 					new SubProgressMonitor(progressMonitor, 1), null);
 		} catch (ExecutionException e) {
 			FritzingDiagramEditorPlugin.getInstance().logError(
 					"Unable to create model and diagram", e); //$NON-NLS-1$
 		}
 		return diagramResource;
 	}
 
 	/**
 	 * Create a new instance of domain element associated with canvas.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static Sketch createInitialModel() {
 		return FritzingFactory.eINSTANCE.createSketch();
 	}
 
 	/**
 	 * Store model element in the resource.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static void attachModelToResource(Sketch model, Resource resource) {
 		resource.getContents().add(createDocumentRoot(model));
 	}
 
 	/**
 	 * @generated
 	 */
 	private static DocumentRoot createDocumentRoot(Sketch model) {
 		DocumentRoot docRoot = FritzingFactory.eINSTANCE.createDocumentRoot();
 
 		docRoot.setSketch(model);
 		return docRoot;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static void selectElementsInDiagram(
 			IDiagramWorkbenchPart diagramPart, List/*EditPart*/editParts) {
 		diagramPart.getDiagramGraphicalViewer().deselectAll();
 
 		EditPart firstPrimary = null;
 		for (Iterator it = editParts.iterator(); it.hasNext();) {
 			EditPart nextPart = (EditPart) it.next();
 			diagramPart.getDiagramGraphicalViewer().appendSelection(nextPart);
 			if (firstPrimary == null && nextPart instanceof IPrimaryEditPart) {
 				firstPrimary = nextPart;
 			}
 		}
 
 		if (!editParts.isEmpty()) {
 			diagramPart.getDiagramGraphicalViewer().reveal(
 					firstPrimary != null ? firstPrimary : (EditPart) editParts
 							.get(0));
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	private static int findElementsInDiagramByID(DiagramEditPart diagramPart,
 			EObject element, List editPartCollector) {
 		IDiagramGraphicalViewer viewer = (IDiagramGraphicalViewer) diagramPart
 				.getViewer();
 		final int intialNumOfEditParts = editPartCollector.size();
 
 		if (element instanceof View) { // support notation element lookup
 			EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(
 					element);
 			if (editPart != null) {
 				editPartCollector.add(editPart);
 				return 1;
 			}
 		}
 
 		String elementID = EMFCoreUtil.getProxyID(element);
 		List associatedParts = viewer.findEditPartsForElement(elementID,
 				IGraphicalEditPart.class);
 		// perform the possible hierarchy disjoint -> take the top-most parts only
 		for (Iterator editPartIt = associatedParts.iterator(); editPartIt
 				.hasNext();) {
 			EditPart nextPart = (EditPart) editPartIt.next();
 			EditPart parentPart = nextPart.getParent();
 			while (parentPart != null && !associatedParts.contains(parentPart)) {
 				parentPart = parentPart.getParent();
 			}
 			if (parentPart == null) {
 				editPartCollector.add(nextPart);
 			}
 		}
 
 		if (intialNumOfEditParts == editPartCollector.size()) {
 			if (!associatedParts.isEmpty()) {
 				editPartCollector.add(associatedParts.iterator().next());
 			} else {
 				if (element.eContainer() != null) {
 					return findElementsInDiagramByID(diagramPart, element
 							.eContainer(), editPartCollector);
 				}
 			}
 		}
 		return editPartCollector.size() - intialNumOfEditParts;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static View findView(DiagramEditPart diagramEditPart,
 			EObject targetElement, LazyElement2ViewMap lazyElement2ViewMap) {
 		boolean hasStructuralURI = false;
 		if (targetElement.eResource() instanceof XMLResource) {
 			hasStructuralURI = ((XMLResource) targetElement.eResource())
 					.getID(targetElement) == null;
 		}
 
 		View view = null;
 		if (hasStructuralURI
 				&& !lazyElement2ViewMap.getElement2ViewMap().isEmpty()) {
 			view = (View) lazyElement2ViewMap.getElement2ViewMap().get(
 					targetElement);
 		} else if (findElementsInDiagramByID(diagramEditPart, targetElement,
 				lazyElement2ViewMap.editPartTmpHolder) > 0) {
 			EditPart editPart = (EditPart) lazyElement2ViewMap.editPartTmpHolder
 					.get(0);
 			lazyElement2ViewMap.editPartTmpHolder.clear();
 			view = editPart.getModel() instanceof View ? (View) editPart
 					.getModel() : null;
 		}
 
 		return (view == null) ? diagramEditPart.getDiagramView() : view;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static class LazyElement2ViewMap {
 		/**
 		 * @generated
 		 */
 		private Map element2ViewMap;
 
 		/**
 		 * @generated
 		 */
 		private View scope;
 
 		/**
 		 * @generated
 		 */
 		private Set elementSet;
 
 		/**
 		 * @generated
 		 */
 		public final List editPartTmpHolder = new ArrayList();
 
 		/**
 		 * @generated
 		 */
 		public LazyElement2ViewMap(View scope, Set elements) {
 			this.scope = scope;
 			this.elementSet = elements;
 		}
 
 		/**
 		 * @generated
 		 */
 		public final Map getElement2ViewMap() {
 			if (element2ViewMap == null) {
 				element2ViewMap = new HashMap();
 				// map possible notation elements to itself as these can't be found by view.getElement()
 				for (Iterator it = elementSet.iterator(); it.hasNext();) {
 					EObject element = (EObject) it.next();
 					if (element instanceof View) {
 						View view = (View) element;
 						if (view.getDiagram() == scope.getDiagram()) {
 							element2ViewMap.put(element, element); // take only those that part of our diagram
 						}
 					}
 				}
 
 				buildElement2ViewMap(scope, element2ViewMap, elementSet);
 			}
 			return element2ViewMap;
 		}
 
 		/**
 		 * @generated
 		 */
 		static Map buildElement2ViewMap(View parentView, Map element2ViewMap,
 				Set elements) {
 			if (elements.size() == element2ViewMap.size())
 				return element2ViewMap;
 
 			if (parentView.isSetElement()
 					&& !element2ViewMap.containsKey(parentView.getElement())
 					&& elements.contains(parentView.getElement())) {
 				element2ViewMap.put(parentView.getElement(), parentView);
 				if (elements.size() == element2ViewMap.size())
 					return element2ViewMap;
 			}
 
 			for (Iterator it = parentView.getChildren().iterator(); it
 					.hasNext();) {
 				buildElement2ViewMap((View) it.next(), element2ViewMap,
 						elements);
 				if (elements.size() == element2ViewMap.size())
 					return element2ViewMap;
 			}
 			for (Iterator it = parentView.getSourceEdges().iterator(); it
 					.hasNext();) {
 				buildElement2ViewMap((View) it.next(), element2ViewMap,
 						elements);
 				if (elements.size() == element2ViewMap.size())
 					return element2ViewMap;
 			}
 			for (Iterator it = parentView.getSourceEdges().iterator(); it
 					.hasNext();) {
 				buildElement2ViewMap((View) it.next(), element2ViewMap,
 						elements);
 				if (elements.size() == element2ViewMap.size())
 					return element2ViewMap;
 			}
 			return element2ViewMap;
 		}
 	} //LazyElement2ViewMap	
 
 	/**
 	 * @generated NOT
 	 */
 	public static URI getActiveDiagramURI() throws NullPointerException {
 		IEditorInput input = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
 				.getEditorInput();
 		return ((URIEditorInput) input).getURI();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static FritzingDiagramEditor getActiveDiagramPart()
 			throws NullPointerException {
 		IEditorPart editor = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		return ((FritzingDiagramEditor) editor);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static Boolean openFritzingFile(URI fileURI) {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
 		IWorkbenchPage page = workbenchWindow.getActivePage();
 		IEditorDescriptor editorDescriptor = workbench.getEditorRegistry()
 				.getDefaultEditor(fileURI.toFileString());
 		if (editorDescriptor == null) {
 			MessageDialog
 					.openError(
 							workbenchWindow.getShell(),
 							Messages.DiagramEditorActionBarAdvisor_DefaultFileEditorTitle,
 							NLS
 									.bind(
 											Messages.DiagramEditorActionBarAdvisor_DefaultFileEditorMessage,
 											fileURI.toFileString()));
 			return false;
 		} else {
 			try {
 				page.openEditor(new URIEditorInput(fileURI), editorDescriptor
 						.getId());
 			} catch (PartInitException exception) {
 				MessageDialog
 						.openError(
 								workbenchWindow.getShell(),
 								Messages.DiagramEditorActionBarAdvisor_DefaultEditorOpenErrorTitle,
 								exception.getMessage());
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @return the install location of Fritzing on the hard drive
 	 */
 	public static String getFritzingLocation() {
 		String fritzingLocation = Platform.getInstallLocation().getURL()
 				.getPath();
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			fritzingLocation = fritzingLocation.startsWith("/") ? fritzingLocation
 					.substring(1)
 					: fritzingLocation;
 		}
 		return fritzingLocation;
 	}
 
 	public static File getFritzingUserFolder() {
 		File location = new File(System.getProperty("user.home") + "/Fritzing"); // fallback location
 
 		// taken from Arduinos Base.getDefaultSketchbookFolder():
 		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 			try {
 				Class clazz = Class.forName("com.apple.eio.FileManager");
 				java.lang.reflect.Method m = clazz.getMethod("findFolder",
 						new Class[] { int.class });
 				String docPath = (String) m.invoke(null,
						new Object[] { new Short(kUserDomain), new Integer(kDocumentsFolderType) });
 
 				location = new File(docPath + "/Fritzing");
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 
 			// carbon folder constants
 			// http://developer.apple.com/documentation/Carbon/Reference/Folder_Manager/folder_manager_ref/constant_6.html#//apple_ref/doc/uid/TP30000238/C006889
 
 			// additional information found in the local file:
 			// /System/Library/Frameworks/CoreServices.framework/Versions/Current/Frameworks/CarbonCore.framework/Headers/
 			/*
 			 try {
 			 MRJOSType domainDocuments = new MRJOSType("docs");
 			 //File libraryFolder = MRJFileUtils.findFolder(domainDocuments);
 
 			 // for 77, try switching this to the user domain, just to be sure
 			 Method findFolderMethod =
 			 MRJFileUtils.class.getMethod("findFolder",
 			 new Class[] { Short.TYPE,
 			 MRJOSType.class });
 			 File documentsFolder = (File)
 			 findFolderMethod.invoke(null, new Object[] { new Short(kUserDomain),
 			 domainDocuments });
 			 location = new File(documentsFolder, "Fritzing");
 
 			 } catch (Exception e) {
 			 //showError("Could not find folder",
 			 //          "Could not locate the Documents folder.", e);
 			 //	        sketchbookFolder = promptSketchbookLocation();
 			 }
 			 */
 		} else if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			// looking for Documents and Settings/blah/My Documents/Fritzing
 			// or on Vista Users/blah/Documents/Fritzing
 			// (though using a reg key since it's different on other platforms)
 
 			// http://support.microsoft.com/?kbid=221837&sd=RMVP
 			// The path to the My Documents folder is stored in the
 			// following registry key, where path is the complete path
 			// to your storage location:
 			// HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders
 			// Value Name: Personal
 			// Value Type: REG_SZ
 			// Value Data: path
 
 			try {
 				RegistryKey topKey = Registry.HKEY_CURRENT_USER;
 
 				String localKeyPath = "Software\\Microsoft\\Windows\\CurrentVersion"
 						+ "\\Explorer\\Shell Folders";
 				RegistryKey localKey = topKey.openSubKey(localKeyPath);
 				String personalPath = cleanKey(localKey
 						.getStringValue("Personal"));
 				//topKey.closeKey();  // necessary?
 				//localKey.closeKey();
 				location = new File(personalPath, "Fritzing");
 
 			} catch (Exception e) {
 				//showError("Problem getting folder",
 				//          "Could not locate the Documents folder.", e);
 				//	        sketchbookFolder = promptSketchbookLocation();
 			}
 		}
 		return location;
 	}
 
 	// taken from Arduinos Base.cleanKey():
 	static public String cleanKey(String what) {
 		// jnireg seems to be reading the chars as bytes
 		// so maybe be as simple as & 0xff and then running through decoder
 
 		char c[] = what.toCharArray();
 
 		// if chars are in the tooHigh range, it's prolly because
 		// a byte from the jni registry was turned into a char
 		// and there was a sign extension.
 		// e.g. 0xFC (252, umlaut u) became 0xFFFC (65532).
 		// but on a japanese system, maybe this is two-byte and ok?
 		int tooHigh = 65536 - 128;
 		for (int i = 0; i < c.length; i++) {
 			if (c[i] >= tooHigh)
 				c[i] &= 0xff;
 		}
 		return new String(c);
 	}
 
 	static final int kDocumentsFolderType = ('d' << 24) | ('o' << 16)
 			| ('c' << 8) | 's';
 	static final int kPreferencesFolderType = ('p' << 24) | ('r' << 16)
 			| ('e' << 8) | 'f';
 	static final int kDomainLibraryFolderType = ('d' << 24) | ('l' << 16)
 			| ('i' << 8) | 'b';
 	static final short kUserDomain = -32763;
 }
