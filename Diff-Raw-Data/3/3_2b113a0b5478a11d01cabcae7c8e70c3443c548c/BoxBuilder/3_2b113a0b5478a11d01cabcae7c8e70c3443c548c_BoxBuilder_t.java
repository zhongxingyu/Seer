 /*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jurgen Vinju (jurgen@vinju.org) - initial API and implementation
 *******************************************************************************/
 package org.eclipse.imp.box.builders;
 
 import java.io.FileOutputStream;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.box.Activator;
 import org.eclipse.imp.box.parser.BoxParseController;
 import org.eclipse.imp.builder.BuilderBase;
 import org.eclipse.imp.builder.BuilderUtils;
 import org.eclipse.imp.builder.MarkerCreator;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.model.ModelFactory;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.runtime.PluginBase;
 import org.eclipse.imp.utils.StreamUtils;
 
 /**
  * @author
  */
 public class BoxBuilder extends BuilderBase {
 	public static final String BUILDER_ID = Activator.kPluginID
 			+ ".builder";
 
 	public static final String PROBLEM_MARKER_ID = Activator.kPluginID
 			+ ".problem";
 
 	public static final String LANGUAGE_NAME = "Box";
 
 	public static final Language LANGUAGE = LanguageRegistry
 			.findLanguage(LANGUAGE_NAME);
 
 	protected PluginBase getPlugin() {
 		return Activator.getInstance();
 	}
 
 	protected String getErrorMarkerID() {
 		return PROBLEM_MARKER_ID;
 	}
 
 	protected String getWarningMarkerID() {
 		return PROBLEM_MARKER_ID;
 	}
 
 	protected String getInfoMarkerID() {
 		return PROBLEM_MARKER_ID;
 	}
 
 	protected boolean isSourceFile(IFile file) {
 		IPath path = file.getRawLocation();
 		if (path == null)
 			return false;
 
 		String pathString = path.toString();
 		if (pathString.indexOf("/bin/") != -1)
 			return false;
 
 		return LANGUAGE.hasExtension(path.getFileExtension());
 	}
 
 	/**
 	 * @return true iff the given file is a source file that this builder should scan
 	 * for dependencies, but not compile as a top-level compilation unit.<br>
 	 * <code>isNonRootSourceFile()</code> and <code>isSourceFile()</code> should never
 	 * return true for the same file.
 	 */
 	protected boolean isNonRootSourceFile(IFile resource) {
 		return false;
 	}
 
 	/**
 	 * Collects compilation-unit dependencies for the given file, and records
 	 * them via calls to <code>fDependency.addDependency()</code>.
 	 */
 	protected void collectDependencies(IFile file) {
 		return;
 	}
 
 	protected boolean isOutputFolder(IResource resource) {
 		return resource.getFullPath().lastSegment().equals("bin");
 	}
 
 	protected void compile(final IFile file, IProgressMonitor monitor) {
 		try {
 			runParserForCompiler(file, monitor);
 			doRefresh(file.getParent());
 			
 			String absPath = file.getLocation().toOSString();
 			
 			String box = BoxFactory.box2text(StreamUtils.readStreamContents(file.getContents()));
 			FileOutputStream out = new FileOutputStream(absPath + ".fmt");
 			out.write(box.getBytes());
 			out.close();
 	
 			doRefresh(file);
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 
 			e.printStackTrace();
 		}
 	}
 
 	protected void runParserForCompiler(final IFile file,
 			IProgressMonitor monitor) {
 		try {
 			// Parse controller is the "compiler" here; parses and reports errors
 			IParseController parseController = new BoxParseController();
 
 			// Marker creator handles error messages from the parse controller (and
 			// uses the parse controller to get additional information about the errors)
			MarkerCreator markerCreator = new MarkerCreator(file, PROBLEM_MARKER_ID);
 
 			// If we have a kind of parser that might be receptive, tell it
 			// what types of problem marker the builder will create
 			parseController.getAnnotationTypeInfo().addProblemMarkerType(getErrorMarkerID());
 
 			// Need to tell the parse controller which file in which project to parse
 			// and also the message handler to which to report errors
 			IProject project = file.getProject();
 			ISourceProject sourceProject = null;
 			try {
 				sourceProject = ModelFactory.open(project);
 			} catch (ModelException me) {
 				System.err
 						.println("BoxParseController.runParserForComplier(..):  Model exception:\n"
 								+ me.getMessage()
 								+ "\nReturning without parsing");
 				return;
 			}
 			parseController.initialize(file.getProjectRelativePath(),
 					sourceProject, markerCreator);
 
 			String contents = BuilderUtils.getFileContents(file);
 
 			// Finally parse it
 			parseController.parse(contents, monitor);
 
 			doRefresh(file.getParent());
 		} catch (Exception e) {
 			getPlugin().writeErrorMsg(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
