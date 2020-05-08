 package org.magnolialang.resources;
 
 import static org.magnolialang.terms.TermFactory.tf;
 import static org.magnolialang.terms.TermFactory.ts;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.magnolialang.nullness.Nullable;
import org.magnolialang.terms.TermFactory;
 import org.rascalmpl.tasks.Transaction;
 
 public interface IResourceManager {
 	Type	Type_FileResource	= tf.abstractDataType(ts, "FileResource");
	Type	Cons_FileResource	= tf.constructor(ts, Type_FileResource, "FileResource", tf.stringType(), "val");
	Type	Type_ModuleResource	= tf.abstractDataType(ts, "ModuleResource");
	Type	Cons_ModuleResource	= tf.constructor(ts, Type_ModuleResource, "ModuleResource", TermFactory.Type_AST, "val");
 
 
 	Transaction getTransaction();
 
 
 	@Nullable
 	IManagedResource find(IPath path);
 
 
 	@Nullable
 	IManagedResource find(IProject project, IPath path);
 
 
 	void addListener(IManagedResourceListener listener);
 
 
 	void removeListener(IManagedResourceListener listener);
 
 
 	void dispose();
 
 
 	/**
 	 * @param uri
 	 *            A URI
 	 * @return An appropriate path for subsequent calls to the manager
 	 * @throws IllegalArgumentException
 	 *             if URI points outside workspace
 	 */
 	IPath getPath(URI uri);
 
 
 	/**
 	 * Relative paths are resolved relative to the current project (if any).
 	 * 
 	 * Path needs not exist in the file system.
 	 * 
 	 * @param path
 	 *            A string representation of a path
 	 * @return An appropriate path for subsequent calls to the manager
 	 */
 	IPath getPath(String path);
 
 
 	/**
 	 * @param uri
 	 *            A URI
 	 * @return true if URI points inside workspace
 	 */
 	boolean hasPath(URI uri);
 }
