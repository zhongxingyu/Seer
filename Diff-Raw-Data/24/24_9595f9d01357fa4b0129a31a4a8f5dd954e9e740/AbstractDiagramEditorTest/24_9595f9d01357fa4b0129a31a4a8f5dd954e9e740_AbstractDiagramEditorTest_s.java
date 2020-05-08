 /*
 * Copyright (c) 2006, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    bblajer - initial API and implementation
  */
 package org.eclipse.gmf.tests.gef;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ContentHandler;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.tests.Utils;
 import org.eclipse.gmf.tests.rt.AbstractCanvasTest;
 import org.eclipse.gmf.tests.setup.GeneratorConfiguration;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.FileEditorInput;
 
 /**
  * Abstract test that operates with open diagram editors instead of fake no-update viewers.
  */
 public class AbstractDiagramEditorTest extends AbstractCanvasTest {
 	private IProject myProject;
 	private IEditorPart myEditor;
 	private IFile myDiagramFile;
 
 	public AbstractDiagramEditorTest(String name) {
 		super(name);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		myProject = createProject();
 		myDiagramFile = createDiagram();
 		myEditor = openEditor(myDiagramFile);
 	}
 	
 	protected IProject getProject() {
 		return myProject;
 	}
 	
 	protected IEditorPart getEditor() {
 		return myEditor;
 	}
 
 	@Override
 	protected GeneratorConfiguration.ViewerConfiguration createViewerConfiguration() throws Exception {
 		return createViewerConfiguration(myEditor);
 	}
 	
 	protected GeneratorConfiguration.ViewerConfiguration createViewerConfiguration(IEditorPart editorPart) throws Exception {
 		GraphicalViewer viewer = (GraphicalViewer) editorPart.getAdapter(GraphicalViewer.class);
 		return getSetup().getGeneratorConfiguration().createViewerConfiguration(viewer, getSetup().getGenModel().getGenDiagram(), getSetup().getGeneratedPlugin());
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		closeEditor(myEditor);
// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=154767
// Leaving editor open till bundle uninstallation
//		deleteProject();
 		myProject = null;
 		myDiagramFile = null;
 		super.tearDown();
 	}
 
	protected void closeEditor(IEditorPart editor) {
		myEditor.doSave(new NullProgressMonitor());
 		redispatchEvents();
// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=154767
// Leaving editor open till bundle uninstallation
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(myEditor, true);
 	}
 	
	protected final void redispatchEvents() {
 		final boolean t = Utils.dispatchDisplayMessages(3);
 		assertTrue("Display message redispatch was not expected to end by timeout", t);
 	}
 	
 	protected IProject createProject() {
 		String projectName = getUniqueString();
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 		assertFalse("Project with this name already present in the workspace: " + projectName, project.exists());
 		try {
 			project.create(new NullProgressMonitor());
 			project.open(new NullProgressMonitor());
 		} catch (CoreException e) {
 			fail(e.getMessage());
 		}
 		assertTrue("Project was not created: " + projectName, project.exists());
 		return project;
 	}
 	
 	protected void deleteProject() throws CoreException {
 		if (getProject() != null) {
 			if (getProject().isOpen()) {
 				getProject().close(new NullProgressMonitor());
 			}
 			if (getProject().exists()) {
 				getProject().delete(true, new NullProgressMonitor());
 			}
 		}
 	}
 
 	protected IFile createDiagram() throws Exception {
 		return createDiagram(getSetup().getGenModel().getGenDiagram().getEditorGen().isSameFileForDiagramAndModel());
 	}
 
 	protected IFile createDiagram(boolean storeModelInDiagramFile) throws Exception {
 		GenDiagram genDiagram = getSetup().getGenModel().getGenDiagram();
 		String uniqueName = getUniqueString();
 		final String diagramFileName = uniqueName + "." + genDiagram.getEditorGen().getDiagramFileExtension();
 		IFile diagramFile = getProject().getFile(diagramFileName);
 		assertFalse("Diagram file was already created", diagramFile.exists());
 
 		IFile modelFile = null;
 		if (!storeModelInDiagramFile) {
 			IPath diagramFilePath = diagramFile.getProjectRelativePath();
 			IPath modelFilePath = diagramFilePath.removeFileExtension().addFileExtension(genDiagram.getEditorGen().getDomainFileExtension());	
 			modelFile = getProject().getFile(modelFilePath);
 			assertFalse("Model file was already created", modelFile.exists());
 		}
 
 		ResourceSet resourceSet = new ResourceSetImpl();
 		Resource diagramResource = resourceSet.createResource(URI.createPlatformResourceURI(diagramFile.getFullPath().toOSString(), true), ContentHandler.UNSPECIFIED_CONTENT_TYPE);
 		Resource modelResource = modelFile != null ? resourceSet.createResource(URI.createPlatformResourceURI(modelFile.getFullPath().toOSString(), true), ContentHandler.UNSPECIFIED_CONTENT_TYPE) : null;		
 		
 		EObject domainDiagramElement = createDiagramDomainObject();
 
 		if (modelResource != null) {
 			modelResource.getContents().add(domainDiagramElement);
 		} else {
 			diagramResource.getContents().add(domainDiagramElement);
 		}
 		Diagram diagram = getSetup().getGeneratorConfiguration().createDiagram(domainDiagramElement, getSetup());
 		assertNotNull("Diagram was not created", diagram);
 		if (diagram != null) {
 			diagramResource.getContents().add(diagram);
 			diagram.setName(uniqueName);
 			diagram.setElement(domainDiagramElement);
 		}
 		try {
 			if (modelResource != null) {
 				modelResource.save(Collections.EMPTY_MAP);
 			}
 			diagramResource.save(Collections.EMPTY_MAP);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		return diagramFile;
 	}
 
 	protected EObject createDiagramDomainObject() throws Exception {
 		GenClass diagramElementGenClass = getSetup().getGenModel().getGenDiagram().getDomainDiagramElement();
 		GenPackage domainGenPackage = diagramElementGenClass.getGenPackage();
 		Class<?> factoryInterface = getSetup().loadGeneratedClass(domainGenPackage.getQualifiedFactoryInterfaceName());
 		assertNotNull("Factory interface not found", factoryInterface);
 		Field accessor = null;
 		accessor = factoryInterface.getField(domainGenPackage.getFactoryInstanceName());
 		assertNotNull("Accessor field not found", accessor);
 		Object factory = null;
 		factory = accessor.get(null);
 		assertNotNull("Factory unavailable", factory);
 		Method createMethod = null;
 		createMethod = factory.getClass().getMethod("create" + diagramElementGenClass.getName(), new Class[0]);
 		assertNotNull("Create method unavailable", createMethod);
 		EObject domainDiagramElement = null;
 		domainDiagramElement = (EObject) createMethod.invoke(factory, new Object[0]);
 		assertNotNull("Domain diagram element was not created", domainDiagramElement);
 		return domainDiagramElement;
 	}
 
 	protected IEditorPart openEditor(IFile diagramFile) {
 		try {
 	        IEditorDescriptor editorDesc = IDE.getEditorDescriptor(diagramFile, true);
 	        IEditorPart result = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(diagramFile), editorDesc.getId(), true);
 	        return result;
 		} catch (PartInitException e) {
 			fail(e.getMessage());
 		}
 		return null;
 	}
 
 	protected final String getUniqueString() {
 		return "AbstractDiagramEditorTest_" + String.valueOf(System.currentTimeMillis());
 	}
 }
