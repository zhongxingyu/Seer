 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.io.InputStream;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.IStorageEditorInput;
 
 public class StorageRevisionEditorInputAdapter implements IStorageEditorInput {
 
 	private IStorageEditorInput parent;
 	private IStorage storage;
 	public StorageRevisionEditorInputAdapter(IStorageEditorInput parent) {	
 		this.parent = parent;
 	}
 
 	public IStorage getStorage() throws CoreException {
 		if (storage == null)
 			storage = new StorageAdapter(parent.getStorage());
 		return storage;
 	}
 
 	public boolean exists() {
 		return parent.exists();
 	}
 
 	public ImageDescriptor getImageDescriptor() {
 		return parent.getImageDescriptor();
 	}
 
 	public String getName() {
 		return parent.getName();
 	}
 
 	public IPersistableElement getPersistable() {
 		return parent.getPersistable();
 	}
 
 	public String getToolTipText() {
 		return parent.getToolTipText();
 	}
 
 	public Object getAdapter(Class adapter) {
 		return parent.getAdapter(adapter);
 	}
 
 	private class StorageAdapter implements IStorage {
 
 		private IStorage storage;
 		
 		public StorageAdapter(IStorage storage) {
 			this.storage = storage;
 		}
 
 		public InputStream getContents() throws CoreException {
 			return storage.getContents();
 		}
 
 		public IPath getFullPath() {
 			return storage.getFullPath();
 		}
 
 		public String getName() {			
			return storage.getFullPath().toString();
 		}
 
 		public boolean isReadOnly() {
 			return storage.isReadOnly();
 		}
 
 		public Object getAdapter(Class adapter) {
 			return storage.getAdapter(adapter);
 		}
 		
 	}
 }
