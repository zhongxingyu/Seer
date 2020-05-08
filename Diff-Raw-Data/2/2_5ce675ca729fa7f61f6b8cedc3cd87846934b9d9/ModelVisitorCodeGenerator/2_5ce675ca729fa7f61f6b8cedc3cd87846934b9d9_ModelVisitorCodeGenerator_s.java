 /**
  * <copyright>
  *
  * Copyright (c) 2010 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: ModelVisitorCodeGenerator.java,v 1.4 2011/03/17 20:01:45 ewillink Exp $
  */
 package org.eclipse.ocl.examples.build.utilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.mwe.core.WorkflowContext;
 import org.eclipse.emf.mwe.core.issues.Issues;
 import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent;
 import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.build.acceleo.GeneratePivotVisitors;
 
 /**
  * Generates the javaFolder/'javaPackageName'/javaClassName.java file providing
  * a static Java-creation of the libraryFile OCL standard library definition.
  */
 public class ModelVisitorCodeGenerator extends AbstractWorkflowComponent
 {
 	private static final String EMPTY_STRING = "";
 	private Logger log = Logger.getLogger(getClass());	
 	private ResourceSet resourceSet = null;
 	protected String modelPackageName;
 	protected String visitorPackageName;
 	protected String visitorClassName;
 	protected String visitablePackageName;
 	protected String visitableClassName;
 	protected String javaFolder;	
 	protected String genModelFile;
 
 	public void checkConfiguration(Issues issues) {
 		if (modelPackageName == null) {
 			issues.addError(this, "modelPackageName not specified.");
 		}
 		if (visitorPackageName == null) {
 			issues.addError(this, "visitorPackageName not specified.");
 		}
 		if (visitorClassName == null) {
 			issues.addError(this, "visitorClassName not specified.");
 		}
 		if (visitorClassName == null) {
 			issues.addError(this, "visitableClassName not specified.");
 		}
 		if (genModelFile == null) {
 			issues.addError(this, "genModelFile not specified.");
 		}
 	}
 
 
 	@Override
 	public void invokeInternal(WorkflowContext ctx, ProgressMonitor arg1, Issues issues) {
 		URI fileURI = URI.createPlatformResourceURI(genModelFile, true);
 		File outputFolder = new File(getJavaFolder() + '/' + visitorPackageName.replace('.', '/'));
 		log.info("Loading Ecore Model '" + fileURI);
 		
 		try {
 			ResourceSet resourceSet = getResourceSet();
 			Resource genModelResource = resourceSet.getResource(fileURI, true);
 			EPackage targetEPackage = getEPackage(genModelResource);
 			String copyright = getCopyright(genModelResource);
 			
 			List<Object> arguments = new ArrayList<Object>();
 			arguments.add(getModelPackageName());
 			arguments.add(getVisitorPackageName());
 			arguments.add(getVisitorClassName());			
 			arguments.add(getSuperVisitorPackageName() == null ? getVisitorPackageName() : getSuperVisitorPackageName());
 			arguments.add(getSuperVisitorClassName());
 			arguments.add(getVisitablePackageName() == null ? getVisitorPackageName() : getVisitablePackageName()); // If null, we use the visitorPackageName
 			arguments.add(getVisitableClassName());
 			arguments.add(getGenModelFile());
 			arguments.add(copyright);
 
 			AbstractAcceleoGenerator acceleo = createAcceleoGenerator(targetEPackage, outputFolder, arguments);
 			log.info("Generating to ' " + outputFolder + "'");
 			EMF2MWEMonitorAdapter monitor = new EMF2MWEMonitorAdapter(arg1);
 			acceleo.generate(monitor);
 		} catch (IOException e) {
 			throw new RuntimeException("Problems running " + getClass().getSimpleName(), e);
 		}
 	}
 
 	public void setVisitorClassName(String visitorClassName) {
 		this.visitorClassName = visitorClassName;
 	}
 	
 	public void setVisitableClassName(String visitableClassName) {
 		this.visitableClassName = visitableClassName;
 	}
 
 	public void setJavaFolder(String javaFolder) {
 		this.javaFolder = javaFolder;
 	}
 
 	public void setVisitorPackageName(String javaPackageName) {
 		this.visitorPackageName = javaPackageName;
 	}
 	
 	public void setVisitablePackageName(String visitablePackageName) {
 		this.visitablePackageName = visitablePackageName;
 	}
 
 	public void setModelPackageName(String modelPackageName) {
 		this.modelPackageName = modelPackageName;
 	}
 
 	public void setGenModelFile(String genModelFile) {
 		this.genModelFile = genModelFile;
 	}
 	
 	public void setResourceSet(ResourceSet resourceSet) {
 		this.resourceSet = resourceSet;
 	}
 
 	public @NonNull ResourceSet getResourceSet() {
 		ResourceSet resourceSet2 = resourceSet;
 		if (resourceSet2 == null) {
 			resourceSet = resourceSet2 = new ResourceSetImpl();
 		}
 		return resourceSet2;
 	}
 	
 	public String getModelPackageName() {
 		return modelPackageName;
 	}
 	
 	public String getVisitorPackageName() {
 		return visitorPackageName;
 	}
 	
 	public String getVisitorClassName() {
 		return visitorClassName;
 	}
 	
 	public String getVisitablePackageName() {
 		return visitablePackageName;
 	}
 	
 	public String getVisitableClassName() {
 		return visitableClassName;
 	}
 
 	public String getJavaFolder() {
 		return javaFolder;
 	}
 
 	public String getGenModelFile() {
 		return genModelFile;
 	}
 	
 	protected String getSuperVisitorPackageName() {
 		return EMPTY_STRING;
 	}
 	
 	protected String getSuperVisitorClassName() {
 		return EMPTY_STRING;
 	}
 
 	protected AbstractAcceleoGenerator createAcceleoGenerator(EObject ecoreModel,
 			File outputFolder, List<Object> arguments) throws IOException {
 		return new GeneratePivotVisitors(ecoreModel, outputFolder, arguments);
 	}
 	
 	private EPackage getEPackage(Resource genModelResource) { 
 		GenModel genModel = (GenModel) genModelResource.getContents().get(0);
 		List<GenPackage> genPackages = genModel.getAllGenPackagesWithConcreteClasses();
 		return genPackages.isEmpty()  
 			 ?  null
 			 : genPackages.get(0).getEcorePackage(); // We assume we want the first one;
 	}
 	
 	private String getCopyright(Resource genModelResource) {
 		GenModel genModel = (GenModel) genModelResource.getContents().get(0);
		return genModel.getCopyrightText();
 	}
 }
