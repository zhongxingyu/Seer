 package net.rcode.assetserver.core;
 
 import java.io.File;
 import java.util.Collection;
 
 /**
  * Represent information about a resource.  Given an AssetPath, a ResourceMount
  * can report what is behind the path.
  * 
  * @author stella
  *
  */
 public class ResourceStat {
 	/**
 	 * The stat'ed path
 	 */
 	public AssetPath path;
 	
 	/**
 	 * The physical path of the resource or null if not relevant
 	 */
 	public File physicalPath;
 	
 	/**
 	 * True if the resource is a directory.  False if a file.
 	 */
 	public boolean isDirectory;
 	
 	/**
 	 * If not null, then this is a collection of associated resources
 	 * that should be output as a unit whenever this resource
 	 * is processed.  This is typically used to represent "expansions"
 	 * or parameterizations of a given base resource that should be
 	 * generated in response to a batch operation.
 	 */
 	public Collection<AssetPath> associatedResources;
	
	@Override
	public String toString() {
		return "ResourceStat(" + path + ")";
	}
 }
