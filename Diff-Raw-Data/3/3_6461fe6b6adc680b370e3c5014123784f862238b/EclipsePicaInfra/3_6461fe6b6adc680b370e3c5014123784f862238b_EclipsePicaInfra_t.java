 /**************************************************************************
  * Copyright (c) 2012-2013 Anya Helene Bagge
  * Copyright (c) 2012-2013 Tero Hasu
  * Copyright (c) 2012-2013 University of Bergen
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version. See http://www.gnu.org/licenses/
  * 
  * 
  * See the file COPYRIGHT for more information.
  * 
  * Contributors:
  * * Anya Helene Bagge
  * * Tero Hasu
  * 
  *************************************************************************/
 package org.nuthatchery.pica;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.model.ISourceProject;
 
 import org.nuthatchery.pica.eclipse.PicaActivator;
 import org.nuthatchery.pica.errors.Severity;
 import org.nuthatchery.pica.rascal.EclipseEvaluatorPool;
 import org.nuthatchery.pica.rascal.IEvaluatorPool;
 import org.nuthatchery.pica.resources.IWorkspaceConfig;
 import org.nuthatchery.pica.resources.IWorkspaceManager;
 import org.nuthatchery.pica.resources.eclipse.EclipseWorkspaceManager;
 import org.nuthatchery.pica.terms.TermFactory;
 import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
 import org.rascalmpl.eclipse.uri.BundleURIResolver;
 import org.rascalmpl.interpreter.Evaluator;
 import org.rascalmpl.interpreter.env.GlobalEnvironment;
 import org.rascalmpl.interpreter.env.ModuleEnvironment;
 import org.rascalmpl.interpreter.load.RascalURIResolver;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
 import org.rascalmpl.uri.BadURIException;
 import org.rascalmpl.uri.ClassResourceInputOutput;
 import org.rascalmpl.uri.URIResolverRegistry;
 import org.rascalmpl.uri.UnsupportedSchemeException;
 
 public final class EclipsePicaInfra extends AbstractPicaInfra {
 
 	public EclipsePicaInfra(IWorkspaceConfig config) {
 		super(config);
 	}
 
 
 	@Override
 	public boolean areModuleFactsPreloaded() {
 		return true;
 	}
 
 
 	@Override
 	public IWorkspaceManager getWorkspaceManager() {
 		return EclipseWorkspaceManager.getInstance(config);
 	}
 
 
 	@Override
 	public void logException(String msg, Throwable t) {
 		PicaActivator.getDefault().logMsg(msg, Severity.ERROR, t);
 	}
 
 
 	@Override
 	public void logMessage(String msg, Severity severity) {
 		PicaActivator.getDefault().logMsg(msg, severity, null);
 
 	}
 
 
 	@Override
 	public Evaluator makeEvaluator(PrintWriter out, PrintWriter err) {
 		GlobalEnvironment heap = new GlobalEnvironment();
 		ModuleEnvironment root = heap.addModule(new ModuleEnvironment("***magnolia***", heap));
 
 		List<ClassLoader> loaders = new ArrayList<ClassLoader>(Arrays.asList(getClass().getClassLoader(), Evaluator.class.getClassLoader(), RascalScriptInterpreter.class.getClassLoader()));
 		if(config.getParserClassLoader() != null)
 			loaders.add(config.getParserClassLoader());
 		URIResolverRegistry registry = new URIResolverRegistry();
 		RascalURIResolver resolver = new RascalURIResolver(registry);
 		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(registry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
 		registry.registerInput(eclipseResolver);
 		registry.registerInput(new BundleURIResolver(registry));
 		Evaluator eval = new Evaluator(TermFactory.vf, out, err, root, heap, loaders, resolver); // URIResolverRegistry
		eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());
 		for(URI uri : config.moreRascalSearchPath()) {
 			System.err.println("makeEvaluator: adding path: " + uri);
 			eval.addRascalSearchPath(uri);
 //			System.err.println("makeEvaluator: adding path: " + uri.resolve("src"));
 //			eval.addRascalSearchPath(uri.resolve("src"));
 		}
 		eval.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
 		try {
 			eval.addRascalSearchPath(getClass().getClassLoader().getResource("").toURI());
 		}
 		catch(URISyntaxException e) {
 			Pica.get().logException("URL conversion", e);
 		}
 		String property = getRascalClassPath();
 		if(!property.equals("")) {
 			eval.getConfiguration().setRascalJavaClassPathProperty(property);
 		}
 
 		return eval;
 	}
 
 
 	@Override
 	public IEvaluatorPool makeEvaluatorPool(String name, List<String> imports) {
 		return new EclipseEvaluatorPool(name, imports);
 	}
 
 
 	public static URI constructProjectURI(IProject project, IPath path) {
 		return constructProjectURI(project.getName(), path);
 	}
 
 
 	public static URI constructProjectURI(ISourceProject project, IPath path) {
 		return constructProjectURI(project.getRawProject().getName(), path);
 	}
 
 
 	public static URI constructProjectURI(String project, IPath path) {
 		try {
 			// making sure that spaces in 'path' are properly escaped
 			path = path.makeAbsolute();
 			return new URI("project", project, path.toString(), null, null);
 		}
 		catch(URISyntaxException usex) {
 			throw new BadURIException(usex);
 		}
 	}
 
 
 	/**
 	 * @param uri
 	 *            The URI of the desired file
 	 * @return An IFile representing the URI
 	 */
 	public static IFile getFileHandle(URI uri) {
 		IPath path = null;
 		try {
 			path = new Path(new File(Pica.getResolverRegistry().getResourceURI(uri)).getAbsolutePath());
 		}
 		catch(UnsupportedSchemeException e) {
 			Pica.get().logException(e.getMessage(), e);
 			e.printStackTrace();
 			return null;
 		}
 		catch(IOException e) {
 			Pica.get().logException(e.getMessage(), e);
 			e.printStackTrace();
 			return null;
 		}
 		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
 
 	}
 
 
 	public static void setInfra(IWorkspaceConfig config) {
 		Pica.set(new EclipsePicaInfra(config));
 	}
 
 }
