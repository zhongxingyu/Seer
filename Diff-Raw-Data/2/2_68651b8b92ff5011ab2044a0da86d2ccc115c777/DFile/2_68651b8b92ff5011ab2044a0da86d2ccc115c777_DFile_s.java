 package edu.berkeley.grippus.fs;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.berkeley.grippus.Errno;
 
 public abstract class DFile {
 
 	private Map<String, DFile> children = new HashMap<String, DFile>();
 
 	private final String name;
 
 	public DFile(String name) {
 		this.name = name;
 	}
 
 	public Map<String, DFile> getChildren() {
 		return children;
 	}
 
 	public DFile find(DFileSpec path) {
 		return find(path.getPath());
 	}
 
 	private DFile find(List<String> pathbits) {
 		final String first = pathbits.get(0);
 		if (pathbits.isEmpty()) return this;
 		if (first.isEmpty() || first.equals("."))
 			return find(pathbits.subList(1, pathbits.size()));
 		DFile child = getChildren().get(first);
 		if (child != null) 
 			if (pathbits.size() == 1)
 				return child;
 			else
 				return child.find(pathbits.subList(1, pathbits.size()));
 		if (pathbits.size() > 1)
 			throw new RuntimeException("No such file or directory");
 		return new DirectoryPlaceholderDFile(this, first);
 	}
 
 	public DFile find(String path) {
 		List<String> pathbits = Arrays.asList(path.split("/"));
 		return find(pathbits);
 	}
 
 	protected boolean nameValid(String name) {
 		return name.matches("[a-zA-Z_][a-zA-Z_0-9]+");
 	}
	
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	/** On a directory entry, create it */
 	public abstract Errno mkdir();
 
 	/** On a directory, create the named child */
 	public abstract Errno mkdir(String name);
 }
