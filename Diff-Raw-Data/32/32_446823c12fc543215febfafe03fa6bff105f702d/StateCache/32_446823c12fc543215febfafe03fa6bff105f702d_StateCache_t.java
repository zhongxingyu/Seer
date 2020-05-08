 package net.sourceforge.eclipseccase;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 
 import net.sourceforge.eclipseccase.ui.ClearcaseDecorator;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 public class StateCache implements Serializable
 {
 	static final long serialVersionUID = -7439899000320633901L;
 
 	public static final String STATE_CHANGE_MARKER_TYPE =
 		"net.sourceforge.eclipseccase.statechangedmarker";
 	private String osPath;
 	private transient IResource resource;
 	private boolean uninitialized = true;
 	private boolean hasRemote = false;
 	private boolean isCheckedOut = false;
 	private boolean isDirty = false;
 	private boolean isSnapShot = false;
 	private boolean isHijacked = false;
 	private String version = "";
 
 	public StateCache(IResource resource)
 	{
 		this.resource = resource;
 		this.osPath = resource.getLocation().toOSString();
 	}
 
 	private void createResource()
 	{
 		File file = new File(osPath);
 		IPath path = new Path(osPath);
 		if (file.isDirectory())
 			resource = ClearcasePlugin.getWorkspace().getRoot().getContainerForLocation(path);
 		else
 			resource = ClearcasePlugin.getWorkspace().getRoot().getFileForLocation(path);
 	}
 	
 	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
 	{
 		s.defaultReadObject();
 		createResource();		
 	}
 	
 	public synchronized void updateAsync()
 	{
 		updateAsync(false);
 	}
 	
 	public synchronized void updateAsync(boolean asap)
 	{
 		uninitialized = true;
 		Runnable cmd = new UpdateCacheCommand(this);
 		UpdateQueue queue = UpdateQueue.getInstance();
 		if (asap)
 		{
 			queue.remove(cmd);
 			queue.addFirst(cmd);
 		}
 		else
 		{
			if (! UpdateQueue.getInstance().contains(cmd))
				UpdateQueue.getInstance().add(cmd);
 		}
 	}
 
 	public synchronized void update()
 	{
 		hasRemote = ClearcasePlugin.getEngine().isElement(osPath);
 		isCheckedOut = hasRemote && ClearcasePlugin.getEngine().isCheckedOut(osPath);
 		isDirty = isCheckedOut && ClearcasePlugin.getEngine().isDifferent(osPath);
 		isSnapShot = hasRemote && ClearcasePlugin.getEngine().isSnapShot(osPath);
 		isHijacked = isSnapShot && ClearcasePlugin.getEngine().isHijacked(osPath);
 		if (hasRemote)
 			version = ClearcasePlugin.getEngine().cleartool("describe -fmt \"%Vn\" \"" + osPath + "\"").message.trim().replace('\\', '/');
 		uninitialized = false;
 		ClearcaseDecorator.labelResource(resource);
 	}
 	
 	/**
 	 * Gets the hasRemote.
 	 * @return Returns a boolean
 	 */
 	public boolean hasRemote()
 	{
 		return hasRemote;
 	}
 
 	/**
 	 * Gets the isCheckedOut.
 	 * @return Returns a boolean
 	 */
 	public boolean isCheckedOut()
 	{
 		return isCheckedOut;
 	}
 
 	/**
 	 * Gets the isDirty.
 	 * @return Returns a boolean
 	 */
 	public boolean isDirty()
 	{
 		return isDirty;
 	}
 
 	/**
 	 * Returns the osPath.
 	 * @return String
 	 */
 	public String getPath()
 	{
 		return osPath;
 	}
 
 	/**
 	 * Returns the version.
 	 * @return String
 	 */
 	public String getVersion()
 	{
 		return version;
 	}
 
 	/**
 	 * Returns the uninitialized.
 	 * @return boolean
 	 */
 	public boolean isUninitialized()
 	{
 		return uninitialized;
 	}
 
 	/**
 	 * Returns the isHijacked.
 	 * @return boolean
 	 */
 	public boolean isHijacked()
 	{
 		return isHijacked;
 	}
 
 	/**
 	 * Returns the isSnapShot.
 	 * @return boolean
 	 */
 	public boolean isSnapShot()
 	{
 		return isSnapShot;
 	}
 
 	/**
 	 * Returns the resource.
 	 * @return IResource
 	 */
 	public IResource getResource()
 	{
 		return resource;
 	}
 
 	private static class UpdateCacheCommand implements Runnable
 	{
 		StateCache cache;
 		UpdateCacheCommand(StateCache cache)
 		{
 			this.cache = cache;
 		}
 		
 		public void run()
 		{
 			cache.update();
 		}
 		
 		public boolean equals(Object obj)
 		{
 			if (! (obj instanceof UpdateCacheCommand))
 				return false;
 			return cache.equals(((UpdateCacheCommand) obj).cache);
 		}
 		public int hashCode()
 		{
 			return cache.hashCode();
 		}
 	}
 	
 
 }
