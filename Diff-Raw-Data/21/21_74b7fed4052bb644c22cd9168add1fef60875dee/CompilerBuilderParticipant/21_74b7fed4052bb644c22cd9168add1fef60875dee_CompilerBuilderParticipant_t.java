 /**
  * 
  */
 package hu.e.parser.ui;
 
 import hu.e.compiler.ECompiler;
import hu.e.parser.ui.internal.ESyntaxActivator;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
 import org.eclipse.xtext.builder.IXtextBuilderParticipant;
 
 /**
  * @author balazs.grill
  *
  */
 public class CompilerBuilderParticipant implements IXtextBuilderParticipant {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.xtext.builder.IXtextBuilderParticipant#build(org.eclipse.xtext.builder.IXtextBuilderParticipant.IBuildContext, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void build(final IBuildContext context, IProgressMonitor monitor)
 			throws CoreException {
 		
 		IProject project = context.getBuiltProject();
 		project.accept(new IResourceVisitor() {
 			
 			@Override
 			public boolean visit(IResource resource) throws CoreException {
 				if (resource instanceof IFile){
 					if (resource.getName().endsWith(".e")){
 						Resource res = context.getResourceSet().
 								getResource(URI.createPlatformResourceURI(resource.getFullPath().toString(), true), true);
 						ECompiler compiler = new ECompiler();
 						System.out.println("Compile "+resource);
 						try{
 							compiler.compile(res, (IFile)resource);
						}catch (final Exception e) {
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Can't compile", "Compiler error", 
											new Status(IStatus.ERROR, ESyntaxActivator.PLUGIN_PREFERENCE_SCOPE,e.getMessage(),e));
								}
							});
 						}
 					}
 					return false;
 				}
 				if (resource instanceof IContainer){
 					return true;
 				}
 				return false;
 			}
 		});
 
 	}
 
 }
