 package net.sourceforge.eclipseccase.ui.compare;
 
import net.sourceforge.eclipseccase.ClearCaseProvider;

 import java.io.IOException;
 
 import java.io.ByteArrayInputStream;
 
 import org.eclipse.core.resources.IResource;
 
 import org.eclipse.compare.BufferedContent;
 
 import org.eclipse.core.runtime.CoreException;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.ResourceNode;
 import org.eclipse.compare.structuremergeviewer.DiffNode;
 import org.eclipse.compare.structuremergeviewer.Differencer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * Implements compare editor input for two versions of a ClearCase element.
  * 
  * @author eplatti
  */
 
 public class VersionCompareInput extends CompareEditorInput {
 
 	private final ITypedElement leftElement;
 
 	private final ITypedElement rightElement;
 
 	/**
 	 * Create a new VersionCompareInput input instance.
 	 * 
 	 * @param configuration
 	 *            The compare configuration.
 	 * @param resource
 	 *            The ClearCase element resource.
 	 * @param leftVersion
 	 *            The version identifier for the left pane, may be
 	 *            <code>null</code> for version specified by the view.
 	 * @param rigthVersion
 	 *            The version identifier for the right pane, may be
 	 *            <code>null</code> for version specified by the view.
 	 */
 
	public VersionCompareInput(CompareConfiguration configuration, IFile resource, String leftVersion, String rigthVersion,ClearCaseProvider provider) {
 		super(configuration);
 
		leftElement = leftVersion != null ? new ClearCaseResourceNode(resource, leftVersion,provider) : new ResourceNode(resource);
		rightElement = rigthVersion != null ? new ClearCaseResourceNode(resource, rigthVersion,provider) : new ResourceNode(resource);
 
 		configuration.setLeftImage(CompareUI.getImage(resource));
 		if (leftVersion != null) {
 			configuration.setLeftLabel(leftVersion);
 		}
 
 		configuration.setRightImage(CompareUI.getImage(resource));
 		if (rigthVersion != null) {
 			configuration.setRightLabel(rigthVersion);
 		}
 
 		setTitle(resource.getName());
 	}
 	/**
 	 * Method is used to saving compare results.
 	 */
 	public void saveChanges(IProgressMonitor pm) throws CoreException {
 		super.saveChanges(pm);
 		if (!(rightElement instanceof ResourceNode && rightElement instanceof BufferedContent)) {
 			return;
 		}
 		ResourceNode rn = (ResourceNode) rightElement;
 		BufferedContent bc = (BufferedContent) rightElement;
 		IResource resource = rn.getResource();
 		if (resource instanceof IFile) {
 			byte[] bytes = bc.getContent();
 			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
 			try {
 				IFile file = (IFile) resource;
 				if (file.exists()) {
 					file.setContents(is, false, true, pm);
 				} else {
 					file.create(is, false, pm);
 				}
 			} finally {
 				if (is != null)
 					try {
 						is.close();
 						setDirty(false);
 					} catch (IOException ex) {
 						// Ignored
 					}
 				resource.getParent().refreshLocal(IResource.DEPTH_INFINITE, pm);
 			}
 		}
 	}
 
 	@Override
 	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		return new DiffNode(null, Differencer.CHANGE, null, leftElement, rightElement);
 	}
 }
