 
 package net.sourceforge.eclipseccase;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileModificationValidator;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.team.core.TeamException;
 
 public class ModificationHandler implements IFileModificationValidator
 {
 	
 	ClearcaseProvider provider;
 
 	/**
 	 * Constructor for ModificationHandler.
 	 */
 	public ModificationHandler(ClearcaseProvider provider)
 	{
 		this.provider = provider;
 	}
 
 	/**
 	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
 	 */
 	public IStatus validateEdit(IFile[] files, Object context)
 	{
 		IStatus result = new Status(IStatus.OK, ClearcaseProvider.ID, TeamException.OK, "OK", null);
 		List needCheckout = new ArrayList();
 		for (int i = 0; i < files.length; ++i)
 		{
 			StateCache cache = StateCache.getState(files[i]);
 			if (cache.hasRemote() && ! cache.isCheckedOut())
 				needCheckout.add(files[i]);
 		}
 		try
 		{
 			provider.checkout((IResource[]) needCheckout.toArray(new IResource[needCheckout.size()]),
 							 IResource.DEPTH_INFINITE, null);
 		}
 		catch(TeamException ex)
 		{
 			result = ex.getStatus();
 		}
 		return result;			
 	}
 
 	/**
 	 * @see IFileModificationValidator#validateSave(IFile)
 	 */
 	public IStatus validateSave(IFile file)
 	{
 		return validateEdit(new IFile[] {file}, null);
 	}
 
 }
