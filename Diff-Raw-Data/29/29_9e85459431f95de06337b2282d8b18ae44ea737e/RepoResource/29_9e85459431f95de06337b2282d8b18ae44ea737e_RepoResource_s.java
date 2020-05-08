 package org.codefaces.core.models;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 public class RepoResource extends RepoEntry {
 	private RepoResourceType type;
 
 	private RepoResource parent;
 
 	private RepoFolderRoot root;
 
 	private IPath path;
 
 	private RepoResourceInfo info;
 
 	private Map<String, Object> properties;
 
 	protected RepoResource(RepoFolderRoot root, RepoResource parent, String id,
 			String name, RepoResourceType type) {
 		super(id, name);
 		this.root = root;
 		this.type = type;
 		this.parent = parent;
		this.path = buildPath();
 	}
 
	private IPath buildPath() {
		if (getType() == RepoResourceType.FOLDER_ROOT) {
 			return Path.ROOT;
 		}
 
 		IPath path = null;
		if (getParent() != null) {
			path = getParent().getPath().append(getName());
 		} else {
			path = new Path(getName());
 		}
 
 		return path;
 	}
 
 	// This method is intended to use only in testing
 	protected RepoResource(RepoFolderRoot root, RepoResource parent, String id,
 			String name, RepoResourceType type, RepoResourceInfo info) {
 		this(root, parent, id, name, type);
 		this.info = info;
 	}
 
 	public void setProperty(String key, Object value) {
 		if (properties == null) {
 			properties = new HashMap<String, Object>();
 		}
 		properties.put(key, value);
 	}
 
 	public Object getProperty(String key) {
 		if (properties == null) {
 			return null;
 		}
 
 		return properties.get(key);
 	}
 
 	public IPath getPath() {
 		return path;
 	}
 
 	public RepoFolderRoot getRoot() {
 		return root;
 	}
 
 	public RepoResource getParent() {
 		return parent;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		RepoResource other = (RepoResource) obj;
 		if (parent == null) {
 			if (other.parent != null)
 				return false;
 		} else if (!parent.equals(other.parent))
 			return false;
 		if (type == null) {
 			if (other.type != null)
 				return false;
 		} else if (!type.equals(other.type))
 			return false;
 		return true;
 	}
 
 	public RepoResourceType getType() {
 		return type;
 	}
 
 	public Collection<RepoResource> getChildren() {
 		return getInfo().getChildren();
 	}
 
 	public boolean hasChildren() {
 		return getInfo().hasChildren();
 	}
 
 	protected RepoResourceInfo getInfo() {
 		if (info == null) {
 			info = RepoResourceManager.getInstance().getInfo(this);
 		}
 
 		return info;
 	}
 }
