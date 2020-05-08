 package net.cheney.cocktail.resource.file;
 
 import java.io.File;
 import java.net.URI;
 
 import net.cheney.cocktail.application.Path;
 import net.cheney.cocktail.resource.Lock;
 import net.cheney.cocktail.resource.LockManager;
 import net.cheney.cocktail.resource.Resource;
 import net.cheney.cocktail.resource.ResourceProvidor;
 import net.cheney.cocktail.resource.Lock.Scope;
 import net.cheney.cocktail.resource.Lock.Type;
 
 public class FileResourceProvidor implements ResourceProvidor {
 
 	private final File root;
 	private final LockManager lockManager;
 
 	public FileResourceProvidor(String root) {
 		this(new File(root));
 	}
 	
 	public FileResourceProvidor(File root) {
 		this.root = root;
 		this.lockManager = new FileResourceLockManager();
 	}
 	
 	public final Resource resolveResource(Path path) {
		return new FileResource(this, fileForPath(path));
 	}
 	
	private File fileForPath(Path path) {
		return path.isEmpty() ? root : new File(root, path.toString());
	}

 	public final LockManager lockManager() {
 		return lockManager;
 	}
 	
 	private class FileResourceLockManager implements LockManager {
 		
 		@Override
 		public boolean isLocked(Resource resource) {
 			return false;
 		}
 
 		@Override
 		public Lock lock(Resource resource, Type type, Scope scope) {
 			return new Lock(type, scope, resource);
 		}
 
 		@Override
 		public Lock unlock(Resource resource) {
 			return new Lock(Type.NONE, Scope.NONE, resource);
 		}
 		
 	}
 
 	public URI relativizeResource(Resource resource) {
 		return root.toURI().relativize(((FileResource) resource).file().toURI());
 	}
 
 	public boolean isRoot(FileResource fileResource) {
 		return fileResource.file().equals(root);
 	}
 
 }
