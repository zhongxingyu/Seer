 package com.github.svetlin_mladenov.gwteventcreator.wizards;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jface.operation.*;
 import java.lang.reflect.InvocationTargetException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 import java.io.*;
 
 import org.eclipse.ui.*;
 import org.eclipse.ui.ide.IDE;
 
 /**
  * This is a sample new wizard. Its role is to create a new file 
  * resource in the provided container. If the container resource
  * (a folder or a project) is selected in the workspace 
  * when the wizard is opened, it will accept it as the target
  * container. The wizard creates one file with the extension
  * "mpe". If a sample multi-page editor (also available
  * as a template) is registered for the same extension, it will
  * be able to open it.
  */
 
 public class GwtEventCreatorWizard extends Wizard implements INewWizard {
 	private GwtEventCreatorWizardPage page;
 	private ISelection selection;
 
 	/**
 	 * Constructor for GwtEventCreatorWizard.
 	 */
 	public GwtEventCreatorWizard() {
 		super();
 		setNeedsProgressMonitor(true);
 	}
 	
 	/**
 	 * Adding the page to the wizard.
 	 */
 
 	public void addPages() {
 		page = new GwtEventCreatorWizardPage();
 		if (selection instanceof IStructuredSelection) {
 			//TODO make selection of type IStructedSelection
 			page.initialize((IStructuredSelection)selection);
 		} else {
 			page.initialize(null);
 		}
 		
 		addPage(page);
 	}
 
 	/**
 	 * This method is called when 'Finish' button is pressed in
 	 * the wizard. We will create an operation and run it
 	 * using wizard as execution context.
 	 */
 	public boolean performFinish() {
 		final String containerName = page.getTargetFolderName();
 		
 		
 		final EventStructsNames names = EventStructsNames.create(
 				page.getPackageName(),
 				page.getEventName(),
 				page.generateHasInterface(),
 				page.generateSeperateGetTypeMethod(),
 				page.lazyCreateEventType()
 			);
 		
 		
 		IRunnableWithProgress op = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) throws InvocationTargetException {
 				try {
 					doFinish(containerName, names, monitor);
 				} catch (CoreException e) {
 					throw new InvocationTargetException(e);
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 		try {
 			getContainer().run(true, false, op);
 		} catch (InterruptedException e) {
 			return false;
 		} catch (InvocationTargetException e) {
 			Throwable realException = e.getTargetException();
 			MessageDialog.openError(getShell(), "Error", realException.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * The worker method. It will find the container, create the
 	 * file if missing or just replace its contents, and open
 	 * the editor on the newly created file.
 	 * @param lazyTypeCreation 
 	 * @param generateSeperateGetMethod 
 	 * @param generateHasInterfave 
 	 */
 
 	private void doFinish(String containerName, EventStructsNames names, IProgressMonitor monitor)
 			throws CoreException {
 		
 		// create a sample file
 		monitor.beginTask("Creating " + names.getEventClassName(), 4);
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IResource resource = root.findMember(new Path(containerName));
 		if (!resource.exists() || !(resource instanceof IContainer)) {
 			throwCoreException("Container \"" + containerName + "\" does not exist.");
 		}
 		IContainer container = (IContainer) resource;
 		
 		final IFile file = createEventClass(container, names, monitor);
 		
 		monitor.worked(1);
 		
 		createHandlerInterface(container, names, monitor);
 		monitor.worked(1);
 		
 		createHasInterface(container, names, monitor);
 		monitor.worked(1);
 		
 		monitor.setTaskName("Opening file for editing...");
 		getShell().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				IWorkbenchPage page =
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 				try {
 					IDE.openEditor(page, file, true);
 				} catch (PartInitException e) {
 				}
 			}
 		});
 		monitor.worked(1);
 	}
 	
 	private void createHasInterface(IContainer container,
 			EventStructsNames names, IProgressMonitor monitor) throws CoreException {
 		if (!names.generateHasInterfave()) {
 			return;
 		}
 		createJavaElement(container, names, names.getHasInterfaceName(), monitor, new JavaStreamProvider() {
 			public InputStream openEventClassStream(EventStructsNames names) {
 				String contents =
 					"package " + names.getPackageName() + ";\n\n" +
					"import com.google.gwt.event.shared.HandlerRegistration\n;" +
 					"import com.google.gwt.event.shared.HasHandlers;\n\n" +
 					"public interface " + names.getHasInterfaceName() + " extends HasHandlers {\n\n" +
 						  "\tHandlerRegistration add" + names.getHandlerInterfaceName() + "(" + names.getHandlerInterfaceName() + " handler);\n\n" +
 					"}";
 				
 					return new ByteArrayInputStream(contents.getBytes());
 			}
 		});
 	}
 
 	private void createHandlerInterface(IContainer container,
 			EventStructsNames names, IProgressMonitor monitor) throws CoreException {
 		createJavaElement(container, names, names.getHandlerInterfaceName(), monitor, new JavaStreamProvider() {
 			public InputStream openEventClassStream(EventStructsNames names) {
 				String contents =
 					"package " + names.getPackageName() + ";\n\n" +
 					"import com.google.gwt.event.shared.EventHandler;\n\n" +
 					"public interface " + names.getHandlerInterfaceName() + " extends EventHandler {\n\n" +
 						  "\tvoid on" + names.getSimpleEventName() + "(" + names.getEventClassName() + " event);\n\n" +
 					"}";
 				
 					return new ByteArrayInputStream(contents.getBytes());
 			}
 		});
 	}
 	
 	private static interface JavaStreamProvider {
 		InputStream openEventClassStream(EventStructsNames names);
 	};
 	
 	private IFile createJavaElement(IContainer container, EventStructsNames names, String fileName, IProgressMonitor monitor, JavaStreamProvider provider) throws CoreException {
 		final IFile file = container.getFile(new Path(fileName + ".java"));
 		try {
 			InputStream stream = provider.openEventClassStream(names);
 			if (file.exists()) {
 				file.setContents(stream, true, true, monitor);
 			} else {
 				file.create(stream, true, monitor);
 			}
 			stream.close();
 		} catch (IOException e) {
 		}
 		return file;
 	}
 
 	private IFile createEventClass(IContainer container, EventStructsNames names, IProgressMonitor monitor) throws CoreException {
 		return createJavaElement(container, names, names.getEventClassName(), monitor, new JavaStreamProvider() {
 			public InputStream openEventClassStream(EventStructsNames names) {
 				String contents =
 					"package " + names.getPackageName() + ";\n\n" +
 					"import com.google.gwt.event.shared.GwtEvent;\n\n" +
 					"public class " + names.getEventClassName() + " extends GwtEvent<" + names.getHandlerInterfaceName() + "> {\n\n" +
 					createTypeExpression(names) +
 					createDispatch(names) +
 					createGetAssociatedType(names) +
 					"}";
 					return new ByteArrayInputStream(contents.getBytes());
 			}
 		});
 	}
 	
 	private static String createGetAssociatedType(EventStructsNames names) {
 		return "\t@Override\n" +
 		  "\tpublic final Type<" + names.getHandlerInterfaceName() + "> getAssociatedType() {\n" +
 		    "\t\treturn TYPE;\n" +
 		  "\t}\n\n";
 	}
 
 	private static String createDispatch(EventStructsNames names) {
 		return "\t@Override\n" +
 		  "\tprotected void dispatch(" + names.getHandlerInterfaceName() + " handler) {\n" +
 		    "\t\thandler.on" + names.getSimpleEventName() + "(this);\n" +
 		  "\t}\n\n";
 	}
 
 	private static String createTypeExpression(EventStructsNames names) {
 		if (names.generateSepаrateGetTypeMethod()) {
 			if (names.isLazyTypeCreation()) {
 				return "\tprivate static Type<" + names.getHandlerInterfaceName() + "> TYPE;\n\n" + 
 				"\tpublic static Type<" + names.getHandlerInterfaceName() + "> getType() {\n" +
 				    "\t\tif (TYPE == null) {\n" +
 				      "\t\t\tTYPE = new Type<" + names.getHandlerInterfaceName() + ">();\n" +
 				    "\t\t}\n" +
 				    "\t\treturn TYPE;\n" +
 				  "\t}\n\n";
 				
 			}
 			else {
 				return "\tprivate static final Type<" + names.getHandlerInterfaceName() + "> TYPE = new Type<" + names.getHandlerInterfaceName() + ">();\n\n" +
 				"\tpublic static Type<" + names.getHandlerInterfaceName() + "> getType() {\n" +
 				    "\t\treturn TYPE;\n" +
 				  "\t}\n";
 			}
 		}
 		else {
 			return "\tpublic static final Type<" + names.getHandlerInterfaceName() + "> TYPE = new Type<" + names.getHandlerInterfaceName() + ">();\n\n";
 		}
 	}
 
 	private void throwCoreException(String message) throws CoreException {
 		IStatus status =
 			new Status(IStatus.ERROR, "GwtEventCreatorWizard", IStatus.OK, message, null);
 		throw new CoreException(status);
 	}
 
 	/**
 	 * We will accept the selection in the workbench to see if
 	 * we can initialize from it.
 	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
 	}
 }
