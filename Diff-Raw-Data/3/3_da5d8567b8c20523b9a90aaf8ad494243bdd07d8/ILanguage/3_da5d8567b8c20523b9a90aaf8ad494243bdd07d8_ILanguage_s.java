 package org.magnolialang.resources;
 
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.magnolialang.compiler.ICompiler;
 import org.magnolialang.load.ModuleParser;
 import org.magnolialang.nullness.Nullable;
 
 public interface ILanguage {
 	/**
 	 * @return User-visible language name
 	 */
 	String getName();
 
 
 	/**
 	 * @return Identifying language name
 	 */
 	String getId();
 
 
 	/**
 	 * @return The main/preferred file name extension, not including the dot
 	 */
 	String getPreferredExtension();
 
 
 	/**
 	 * @return A collection of valid extensions for the language, not including
 	 *         the dot
 	 */
 	Collection<String> getExtensions();
 
 
 	/**
 	 * @param ext
 	 *            filename extension, with or without dot
 	 * @return True if 'ext' is a valid filename extension for this language
 	 */
 	boolean hasExtension(String ext);
 
 
 	/**
 	 * @return A parser for the language
 	 */
 	ModuleParser getParser();
 
 
 	/**
 	 * @param path
 	 *            A path relative to a source folder. The source file need not
 	 *            exist
 	 * @return The canonical module name corresponding to the path, or null if
 	 *         none
 	 *         TODO: should this really be nullable?
 	 */
 	@Nullable
 	String getModuleName(IPath path);
 
 
 	/**
 	 * @param moduleName
 	 *            A module name; module need not exist
 	 * @return A source-folder relative path to the module, including preferred
 	 *         extension
 	 */
 	IPath getModulePath(String moduleName);
 
 
 	/**
 	 * @param name
 	 *            A string representation of a name
 	 * @return The AST representation of the same name
 	 * @throws IllegalArgumentException
 	 *             if argument is not a syntactically valid name
 	 */
 	IConstructor getNameAST(String name);
 
 
 	/**
 	 * @param nameAST
 	 *            An AST representation of a name
 	 * @return The string representation of the same name
 	 * @throws IllegalArgumentException
 	 *             if argument is not a name
 	 */
 	String getNameString(IConstructor nameAST);
 
 
 	@Override
 	int hashCode();
 
 
 	@Override
 	boolean equals(@Nullable Object o);
 
 
 	ICompiler makeCompiler(IModuleManager manager);
 
 }
