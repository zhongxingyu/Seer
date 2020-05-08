 
 package net.sourceforge.eclipseccase;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileModificationValidator;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
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
 		if (context != null && ! ClearcasePlugin.isCheckoutOnEdit())
 			return new Status(IStatus.ERROR, ClearcaseProvider.ID, TeamException.NOT_CHECKED_OUT,
 								"The resource is not checked out, and checkout on edit is disabled", null);
 			
 		IStatus result = new Status(IStatus.OK, ClearcaseProvider.ID, TeamException.OK, "OK", null);
 		List needCheckout = new ArrayList();
 		for (int i = 0; i < files.length; ++i)
 		{
 			StateCache cache = StateCacheFactory.getInstance().get(files[i]);
 			if (cache.hasRemote() && ! cache.isCheckedOut())
 				needCheckout.add(files[i]);
 		}
 		try
 		{
 			provider.checkout((IResource[]) needCheckout.toArray(new IResource[needCheckout.size()]),
 							 IResource.DEPTH_INFINITE, null);
 			// Refresh resource state so that editor context menus/completion/etc know that file is now writable
 			for (Iterator iter = needCheckout.iterator(); iter.hasNext();)
 			{
 				IResource element = (IResource) iter.next();
 				element.refreshLocal(IResource.DEPTH_ZERO, null);
 				
 			}
 		}
 		catch(TeamException ex)
 		{
 			result = ex.getStatus();
 		}
 		catch (CoreException ex)
 		{
 			result = new Status(IStatus.WARNING, ClearcaseProvider.ID, TeamException.IO_FAILED, "Failed to refresh resource state: " + ex, null);
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
