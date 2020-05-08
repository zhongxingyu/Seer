 /**
  * 
  */
 package liglab.imag.fr.metadata.ui.editor.page.component;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import liglab.imag.fr.metadata.emf.CommandFactory;
 import liglab.imag.fr.metadata.emf.ModelUtil;
 import liglab.imag.fr.metadata.ui.editor.MetadataEditor;
 import liglab.imag.fr.metadata.ui.editor.PojoMasterDetailBlock;
 import liglab.imag.fr.metadata.ui.editor.providers.ComponentContentProvider;
 import liglab.imag.fr.metadata.util.ImplementationClassModel;
 import liglab.imag.fr.metadata.util.JDTUtil;
 import liglab.imag.fr.metadata.util.ComponentValidatorVisitor;
 
 import org.apache.felix.ComponentType;
 import org.apache.felix.impl.ComponentTypeImpl;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.forms.DetailsPart;
 import org.eclipse.ui.forms.editor.FormPage;
 
 /**
  * Master Detail Block for components types
  * 
  * @author Gabriel
  */
 public class ComponentMasterDetailBlock extends PojoMasterDetailBlock {
 
 
 	public ComponentMasterDetailBlock(MetadataEditor editor, FormPage page) {
 		super(editor, page);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.forms.MasterDetailsBlock#registerPages(org.eclipse.ui.forms
 	 * .DetailsPart)
 	 */
 	@Override
 	protected void registerPages(DetailsPart detailsPart) {
 		detailsPart.registerPage(ComponentTypeImpl.class, new ComponentDetailsPage(editor, componentsViewer));
 	}
 
 	@Override
 	protected boolean addEditionButtons() {
 		return true;
 	}
 
 	@Override
 	protected String getPanelMessage() {
 		return "Bundle component type definition, details are editable on the right";
 	}
 
 	@Override
 	protected String getPanelTitle() {
 		return "Bundle Component Types List";
 	}
 
 	@Override
 	public IContentProvider getContentProvider() {
 		return new ComponentContentProvider();
 	}
 
 	@Override
 	protected void addContextMenu() {
 		final GenerateComponentClassAction generateClassAction = new GenerateComponentClassAction(
 		      "Generate Implementation Class");
 		final ValidateComponentClassAction validateComponentAction = new ValidateComponentClassAction(
 		      "Validate Implementation Class");
 		final CompleteComponentClassAction completeClassAction = new CompleteComponentClassAction(
 		      "Synchronize Implementation Class");
 		
 		try {
 	      generateClassAction.setImageDescriptor(ImageDescriptor.createFromURL(new URL("platform:/plugin/org.eclipse.jdt.ui/icons/full/etool16/newclass_wiz.gif")));
 	      validateComponentAction.setImageDescriptor(ImageDescriptor.createFromURL(new URL("platform:/plugin/org.eclipse.jdt.ui/icons/full/obj16/translate.gif")));
 	      completeClassAction.setImageDescriptor(ImageDescriptor.createFromURL(new URL("platform:/plugin/org.eclipse.jdt.ui/icons/full/obj16/correction_multi_fix.gif")));	      
       } catch (MalformedURLException e) {
 	      e.printStackTrace();
       }
 
 		final MenuManager menuMng = new MenuManager();
 		menuMng.setRemoveAllWhenShown(true);
 
 		menuMng.addMenuListener(new IMenuListener() {
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see
 			 * org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse
 			 * .jface.action.IMenuManager)
 			 */
 			public void menuAboutToShow(IMenuManager manager) {
 				IStructuredSelection selection = (IStructuredSelection) componentsViewer.getSelection();
 				if (!selection.isEmpty()) {
 					// Add instance action is used on Component Types
 					if (selection.getFirstElement() instanceof ComponentType) {
 						menuMng.add(generateClassAction);
 						menuMng.add(validateComponentAction);
 						menuMng.add(completeClassAction);
 					}
 				}
 			}
 		});
 		componentsViewer.getControl().setMenu(menuMng.createContextMenu(componentsViewer.getControl()));
 	}
 
 	/**
 	 * Action to generate the implementation class for the selected component
 	 * 
 	 * @author Gabriel
 	 * 
 	 */
 	class GenerateComponentClassAction extends Action {
 
 		public GenerateComponentClassAction(String text) {
 			super(text);
 		}
 
 		@Override
 		public void run() {
 
 			IJavaProject javaProject = JDTUtil.getCurrentJavaProject();
 
 			IStructuredSelection selection = (IStructuredSelection) componentsViewer.getSelection();
 			if (!selection.isEmpty()) {
 				ComponentType componentType = (ComponentType) selection.getFirstElement();
 				String fullClassName = componentType.getClassname();
 				if (fullClassName==null || fullClassName.isEmpty())
 					fullClassName = componentType.getName();
 				
 				List<String> interfaces = ModelUtil.getInterfaces(componentType);				
 				IType newClass = JDTUtil.openCreateJavaClassWizard(fullClassName, javaProject, interfaces);
 
 				ImplementationClassModel classModel = new ImplementationClassModel(componentType);
 				classModel.completeImplementationClass(newClass);
 				
 
 				// Seting immplementation class into the file
 				EditingDomain editingDomain = editor.getEditingDomain();
 				Command command = CommandFactory.createSetComponentClassCommand(editingDomain, componentType,
 				      newClass.getFullyQualifiedName());
 				ModelUtil.executeCommand(editingDomain, command);
 
 				// Updates GUI elements with changes
 				componentsViewer.setSelection(selection);
 				
 				//Save the editor state
 				editor.doSave(new NullProgressMonitor());
 			}
 		}
 	}
 
 	class CompleteComponentClassAction extends Action {
 
 		public CompleteComponentClassAction(String text) {
 			super(text);
 		}
 
 		@Override
 		public void run() {
 
 			IJavaProject javaProject = JDTUtil.getCurrentJavaProject();
 
 			IStructuredSelection selection = (IStructuredSelection) componentsViewer.getSelection();
 			if (!selection.isEmpty()) {
 				ComponentType componentType = (ComponentType) selection.getFirstElement();
 				String fullClassName = componentType.getClassname();
 
 				ICompilationUnit compilationUnit = JDTUtil.getCompilationUnit(javaProject, fullClassName);
 				if (compilationUnit == null)
 					return;
 
 				ImplementationClassModel classModel = validateComponent(componentType, compilationUnit);
 				classModel.completeImplementationClass(compilationUnit.getType(JDTUtil
 				      .getJavaClassName(fullClassName)));
 
 			}
 		}
 	}
 
 	/**
 	 * Action to validate the implementation class for the selected component
 	 * 
 	 * @author Gabriel
 	 * 
 	 */
 	class ValidateComponentClassAction extends Action {
 
 		public ValidateComponentClassAction(String text) {
 			super(text);
 		}
 
 		@Override
 		public void run() {
 
 			IJavaProject javaProject = JDTUtil.getCurrentJavaProject();
 
 			IStructuredSelection selection = (IStructuredSelection) componentsViewer.getSelection();
 			if (!selection.isEmpty()) {
 				ComponentType componentType = (ComponentType) selection.getFirstElement();
 
 				try {
 
 					String fullClassName = componentType.getClassname();
 					ICompilationUnit compilationUnit = JDTUtil.getCompilationUnit(javaProject, fullClassName);
 
 					if (compilationUnit == null) {
 						String message = "The implementation class " + fullClassName + " does not exist";
 						MessageDialog.openError(null, "iCasa Component Validation", message);
 						return;
 					}
 
 					ImplementationClassModel classModel = validateComponent(componentType, compilationUnit);
 
 					List<String> errorMsgs = new ArrayList<String>();
 					if (classModel != null) {
 						errorMsgs = classModel.getErrorMessages();
 						IResource resource = javaProject.getUnderlyingResource();
 						String componentName = componentType.getName();
 						deletePreviousErrors(resource, componentName);
 						if (!errorMsgs.isEmpty()) {
 							String message = "The implementation class of component " + componentName
 							      + " is not valid (see the Problems View)";
 							MessageDialog.openError(null, "iCasa Component Validation", message);
 							generateErrors(resource, errorMsgs, componentName);
 						} else {
 							String message = "The implementation class of component " + componentName + " is valid";
 							MessageDialog.openConfirm(null, "iCasa Component Validation", message);
 						}
 					}
 				} catch (JavaModelException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 
 	private ImplementationClassModel validateComponent(ComponentType componentType,
 	      ICompilationUnit compilationUnit) {
 
 		ImplementationClassModel classModel = new ImplementationClassModel(componentType);
 		if (compilationUnit != null) {
 			ASTParser parser = ASTParser.newParser(AST.JLS3);
 			parser.setKind(ASTParser.K_COMPILATION_UNIT);
 			parser.setSource(compilationUnit); // set source
 			parser.setResolveBindings(true); // we need bindings later
 			                                 // on
 			CompilationUnit unit = (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
 			ComponentValidatorVisitor visitor = new ComponentValidatorVisitor(classModel);					
 			return visitor.process(unit);
 		}
 		return null;
 	}
 
 	private void deletePreviousErrors(IResource resource, String componentName) {
 		IMarker[] problems = null;
 		try {
 			problems = resource.findMarkers("icasa.env.marker", false, IResource.DEPTH_ZERO);
 			for (int i = 0; i < problems.length; i++) {
 				IMarker problem = problems[i];
 				String markerComponentName = (String) problem.getAttribute("Component");
 				if (markerComponentName != null && markerComponentName.equals(componentName))
 					problem.delete();
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void generateErrors(IResource resource, List<String> errorMsgs, String componentName) {
 		try {
 			// Create the new problems
 			for (String error : errorMsgs) {
 				IMarker marker = resource.createMarker("icasa.env.marker");
 				marker.setAttribute(IMarker.MESSAGE, error);
 				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
 				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
 				marker.setAttribute("Component", componentName);
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
