 /*******************************************************************************
  * Copyright (c) 2008 Michael Clay and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.xtext.example.handler;
 
import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 /**
  * Experimental to test the serialization from .ecore to .dsl
  * 
  * TODO: pls clean up and doc me 
  * @author Michael Clay - Initial contribution and API
  * 
  */
 public class GenerateEcoreDslHandler extends AbstractHandler {
 
 	@SuppressWarnings( { "unchecked" })
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		EvaluationContext applicationContext = (EvaluationContext) event.getApplicationContext();
 		List fileList = (List) applicationContext.getDefaultVariable();
 		IFile ecoreFile = (IFile) fileList.get(0);
 		ResourceSet xtextResourceSet = new ResourceSetImpl();
 		Resource ecoreResource = xtextResourceSet.getResource(URI.createFileURI(ecoreFile.getLocation().toFile()
 				.getAbsolutePath()), true);
 		EObject ecorePackage = (EObject) ecoreResource.getContents().get(0);
 
 		XtextResourceSet resourceSet = new XtextResourceSet();
 		String fileNameWithExtension = ecoreFile.getName();
 		URI fileURI = URI.createFileURI(new java.io.File(ecoreFile.getParent().getLocation().toFile(),
 				fileNameWithExtension.substring(0, fileNameWithExtension.indexOf(".")) + "_test.ecoredsl")
 				.getAbsolutePath());
 		XtextResource poResource = (XtextResource) resourceSet.createResource(fileURI);
 		poResource.getContents().add(ecorePackage);
 		try {
 			poResource.save(null);
 			ecoreFile.getParent().refreshLocal(1, new NullProgressMonitor());
 		}
		catch (IOException e) {
 			e.printStackTrace();
 		}
		catch (CoreException e) {
			e.printStackTrace();
		}

 		return null;
 	}
 
 }
