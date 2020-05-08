 package net.sourceforge.eclipseccase;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 
 import net.sourceforge.clearcase.simple.IClearcase;
 
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
 	private boolean isSnapShot = false;
 	private boolean isHijacked = false;
 	private String version = "";
 
 	StateCache(IResource resource)
 	{
 		this.resource = resource;
 		this.osPath = resource.getLocation().toOSString();
 	}
 
 	private void createResource()
 	{
 		IPath path = new Path(osPath);
 		resource =
 			ClearcasePlugin.getWorkspace().getRoot().getFileForLocation(path);
 		if (resource == null || !resource.exists())
 			resource =
 				ClearcasePlugin
 					.getWorkspace()
 					.getRoot()
 					.getContainerForLocation(
 					path);
 	}
 
 	private void readObject(ObjectInputStream s)
 		throws IOException, ClassNotFoundException
 	{
 		s.defaultReadObject();
 		createResource();
 	}
 
 	public synchronized void updateAsync()
 	{
 		updateAsync(false, false);
 	}
 
 	public synchronized void updateAsync(boolean quick, boolean asap)
 	{
 		if (! quick)
 			uninitialized = true;
 		Runnable cmd = new UpdateCacheCommand(this, quick);
 		UpdateQueue queue = UpdateQueue.getInstance();
 		if (asap)
 		{
 			queue.remove(cmd);
 			queue.addFirst(cmd);
 		}
 		else
 		{
 			if (!UpdateQueue.getInstance().contains(cmd))
 				UpdateQueue.getInstance().add(cmd);
 		}
 	}
 
 	private synchronized void doUpdate()
 	{
 		boolean changed = uninitialized;
 
 		boolean hasRemote = ClearcasePlugin.getEngine().isElement(osPath);
 		changed = changed || hasRemote != this.hasRemote;
 		this.hasRemote = hasRemote;
 
 		boolean isCheckedOut =
 			hasRemote && ClearcasePlugin.getEngine().isCheckedOut(osPath);
 		changed = changed || isCheckedOut != this.isCheckedOut;
 		this.isCheckedOut = isCheckedOut;
 
 		boolean isSnapShot =
 			hasRemote && ClearcasePlugin.getEngine().isSnapShot(osPath);
 		changed = changed || isSnapShot != this.isSnapShot;
 		this.isSnapShot = isSnapShot;
 
 		boolean isHijacked =
 			isSnapShot && ClearcasePlugin.getEngine().isHijacked(osPath);
 		changed = changed || isHijacked != this.isHijacked;
 		this.isHijacked = isHijacked;
 
 		if (hasRemote)
 		{
 			String version =
 				ClearcasePlugin
 					.getEngine()
 					.cleartool("describe -fmt \"%Vn\" \"" + osPath + "\"")
 					.message
 					.trim()
 					.replace('\\', '/');
 			changed = changed || !version.equals(this.version);
 			this.version = version;
 		}
 
 		uninitialized = false;
 		if (changed)
 			StateCacheFactory.getInstance().fireStateChanged(this);
 	}
 
 	/**
 	 * Updates the cache.  If quick is false, an update is always performed.  If
 	 * quick is true, and update is only performed if the file's readonly status
 	 * differs from what it should be according to the state.
 	 */
 	public void update(boolean quick)
 	{
 		if (quick && !uninitialized)
 		{
 			if (resource.getType() != IResource.FILE
 				|| (resource.isReadOnly()
 					== (isCheckedOut || !hasRemote || isHijacked)))
 			{
 				doUpdate();
 			}
 		}
 		else
 		{
 			doUpdate();
 		}
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
 		return ClearcasePlugin.getEngine().isDifferent(osPath);
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
 	 * Returns the predecessor version.
 	 * @return String
 	 */
 	public String getPredecessorVersion()
 	{
 		String version = null;
 		IClearcase.Status status = ClearcasePlugin.getEngine().cleartool("describe -fmt %PVn " + resource.getLocation().toOSString());
 		if (status.status)
 			version = status.message.trim().replace('\\', '/');
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
 		boolean quick = false;
 
 		UpdateCacheCommand(StateCache cache, boolean quick)
 		{
 			this.cache = cache;
 			this.quick = quick;
 		}
 
 		public void run()
 		{
 			cache.update(quick);
 		}
 
 		public boolean equals(Object obj)
 		{
 			if (!(obj instanceof UpdateCacheCommand))
 				return false;
 			return cache.equals(((UpdateCacheCommand) obj).cache);
 		}
 		public int hashCode()
 		{
 			return cache.hashCode();
 		}
 	}
 
 }
