 package net.sourceforge.eclipseccase.ui.compare;
 
 import java.lang.reflect.InvocationTargetException;
 
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.ResourceNode;
 import org.eclipse.compare.structuremergeviewer.DiffNode;
 import org.eclipse.compare.structuremergeviewer.Differencer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * 
  * Implements compare editor input for the version selected by the view and its
  * predecessor version. 
  * 
  * @author eplatti
  */
 
 public class PredecessorCompareInput extends CompareEditorInput {
 
 	private final IFile resource;
 	private final String previousVersion;
	private final ClearCaseProvider provider;
 
 	/**
 	 * Create a new PredecessorCompareInput input instance.
 	 * 
 	 * @param configuration The compare configuration.
 	 * @param resource The ClearCase element resource.
 	 * @param provider The ClearCase provider.
 	 */
 	
 	public PredecessorCompareInput(CompareConfiguration configuration, IFile resource, ClearCaseProvider provider) {
 		super(configuration);
 		this.resource = resource;
		this.provider = provider;
 		previousVersion = provider.getPredecessorVersion(resource);
 		
 		configuration.setLeftImage(CompareUI.getImage(resource));
 		configuration.setLeftLabel(previousVersion);
 		
 		configuration.setRightImage(CompareUI.getImage(resource));
 		configuration.setRightLabel(provider.getVersion(resource));		
 		
     setTitle(resource.getName());
 	}
 	
 	@Override
 	protected Object prepareInput(IProgressMonitor monitor)
 			throws InvocationTargetException, InterruptedException {
 
 		return new DiffNode(null, Differencer.CHANGE, null,
				new ClearCaseResourceNode(resource, previousVersion,provider),
 				new ResourceNode(resource));
 	}
 }
