 /**
  * Copyright (c) 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Alexander Fedorov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.tr;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gmf.gmfgraph.Canvas;
 import org.eclipse.gmf.gmfgraph.Connection;
 import org.eclipse.gmf.gmfgraph.DiagramLabel;
 import org.eclipse.gmf.gmfgraph.Node;
 import org.eclipse.gmf.internal.bridge.transform.GenModelDetector;
 import org.eclipse.gmf.internal.bridge.transform.TransformToGenModelOperation;
 import org.eclipse.gmf.internal.common.URIUtil;
 import org.eclipse.gmf.mappings.CanvasMapping;
 import org.eclipse.gmf.mappings.GMFMapFactory;
 import org.eclipse.gmf.mappings.LinkMapping;
 import org.eclipse.gmf.mappings.Mapping;
 import org.eclipse.gmf.mappings.NodeMapping;
 import org.eclipse.gmf.mappings.TopNodeReference;
 import org.eclipse.gmf.tests.ConfiguredTestCase;
 import org.eclipse.gmf.tests.setup.DiaDefSetup;
 import org.eclipse.gmf.tests.setup.DiaDefSource;
 import org.eclipse.gmf.tests.setup.MapDefSource;
 import org.eclipse.gmf.tests.setup.ToolDefSource;
 
 
 public class TransformToGenModelOperationTest extends ConfiguredTestCase {
 	
 	public TransformToGenModelOperationTest(String name) {
 		super(name);
 	}
 	
 	private static String FOLDER_MODELS = "models"; //$NON-NLS-1$
 	private static String FILE_BASE = "test"; //$NON-NLS-1$
 	private static String FILE_EXT_ECORE = "ecore"; //$NON-NLS-1$
 	private static String FILE_EXT_GMFGEN = "gmfgen"; //$NON-NLS-1$
 	private static String FILE_EXT_GMFGRAPH = "gmfgraph"; //$NON-NLS-1$
 	private static String FILE_EXT_GMFMAP = "gmfmap"; //$NON-NLS-1$
 	private static String FILE_EXT_GMFTOOL = "gmftool"; //$NON-NLS-1$
 
 	private IProject myProject;
 	private TransformToGenModelOperation myOperation;
 	private ResourceSet myRS;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		myOperation = new TransformToGenModelOperation();
 		myRS = new ResourceSetImpl();
 		myProject = createProject();
 		IFolder models = myProject.getFolder(FOLDER_MODELS);
 		models.create(true, false, new NullProgressMonitor());
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		deleteProject();
 		myProject = null;
 		myOperation = null;
 		super.tearDown();
 	}
 	
 	public void testErrorArguments() {
 		URI toolURI = prepareResource(myRS, FILE_EXT_GMFTOOL, getSetup().getToolDefModel().getRegistry());
 		URI mapURI = prepareResource(myRS, FILE_EXT_GMFMAP, getSetup().getMapModel().getMapping());
 		try {
 			myOperation.loadMappingModel(myRS, toolURI, null);
 			fail("Invalid MappingModel should not be accepted");
 		} catch (CoreException e) {
			Diagnostic validationResult = myOperation.getMapmodelValidationResult();
			assertNotNull("MappingModel validation result should be set", validationResult);
			assertFalse("Load MappingModel status should differ from OK value", IStatus.OK == validationResult.getSeverity());
 			assertNull("GenModelDetector should be reset", myOperation.getGenModelDetector());
 			assertNull("GenModel should be reset", myOperation.getGenModel());
 		}
 		try {
 			myOperation.loadMappingModel(myRS, null, null);
 			fail("null gmfmap URI should not be accepted");
 		} catch (CoreException e) {
 			assertIllegalArgument(e);
 		}
 		try {
 			myOperation.loadMappingModel(null, mapURI, null);
 			fail("null ResorceSet should not be accepted");
 		} catch (CoreException e) {
 			assertIllegalArgument(e);
 		}
 		try {
 			myOperation.findGenmodel(null);
 			fail("null ResorceSet should not be accepted");
 		} catch (CoreException e) {
 			assertIllegalArgument(e);
 		}
 		try {
 			myOperation.loadGenModel(null, null, null);
 			fail("null ResorceSet should not be accepted");
 		} catch (CoreException e) {
 			assertIllegalArgument(e);
 		}
 		IStatus s = myOperation.executeTransformation(null, null);
 		assertFalse("null ResorceSet should not be accepted", s.isOK());
 	}
 	
 	public void testErrorState() {
 		ResourceSetImpl rs = new ResourceSetImpl();
 		IStatus s = myOperation.executeTransformation(rs, null);
 		assertFalse("Target gmfgen URI should be specified", s.isOK());
 		myOperation.setGenURI(createURI(FILE_EXT_GMFGEN));
 		s = myOperation.executeTransformation(rs, null);
 		assertFalse("Mapping model should be loaded before calling executeTransformation()", s.isOK());
 
 		try {
 			myOperation.loadGenModel(rs, null, null);
 			fail("MappingModel should be loaded before calling loadGenModel()");
 		} catch (CoreException e) {
 			assertIllegalState(e);
 		}
 		try {
 			myOperation.findGenmodel(rs);
 			fail("MappingModel should be loaded before calling findGenmodel()");
 		} catch (CoreException e) {
 			assertIllegalState(e);
 		}
 	}
 
 	public void testTransformMappingModel() {
 		URI mapURI = prepareResources();
 		loadMappingModel(mapURI);
 		findAbsentGenmodel();
 		URI genmodelURI = createDefaultGenModel(mapURI);
 		GenModel genModel = null;
 		try {
 			genModel = myOperation.loadGenModel(myRS, genmodelURI, null);
 			assertNotNull("Failed to load GenModel", genModel);
 		} catch (CoreException e) {
 			assertNotNull("Failed to load GenModel", genModel);
 		}
 		myOperation.setGenURI(createURI(FILE_EXT_GMFGEN));
 		IStatus status = myOperation.executeTransformation(myRS, null);
 		assertTrue(status.isOK());
 	}
 	
 	public void testTransfromDesignMapping() {
 		URI mapURI = prepareDesignResources();
 		loadMappingModel(mapURI);
 		myOperation.setGenURI(createURI(FILE_EXT_GMFGEN));
 		IStatus status = myOperation.executeTransformation(myRS, null);
 		assertTrue(status.isOK());
 	}
 
 	private void loadMappingModel(URI mapURI) {
 		Mapping mapping = null;
 		try {
 			mapping = myOperation.loadMappingModel(myRS, mapURI, null);
 			assertNotNull("Mapping should be resolved", mapping);
			Diagnostic validationResult = myOperation.getMapmodelValidationResult();
			assertNotNull("MappingModel validation result should be set", validationResult);
			assertFalse("MappingModel validation result should differ from CANCEL value", IStatus.CANCEL == validationResult.getSeverity());
			assertFalse("MappingModel validation result should differ from ERROR value", IStatus.ERROR == validationResult.getSeverity());
 			assertNotNull("GenModelDetector should be initialized", myOperation.getGenModelDetector());
 			assertNull("GenModel should be reset", myOperation.getGenModel());
 		} catch (CoreException e) {
 			fail(e.getStatus().getMessage());
 		}
 	}
 
 	private void findAbsentGenmodel() {
 		try {
 			myOperation.findGenmodel(myRS);
 			fail("GenModel should not be found");
 		} catch (CoreException e) {
 			assertNull("GenModel should not be found", myOperation.getGenModel());
 		}
 	}
 	
 	private URI createDefaultGenModel(URI mapURI) {
 		URI genmodelURI = null;
 		try {
 			GenModelDetector gmd = myOperation.getGenModelDetector();
 			assertNotNull("GenModelDetector should be initialized", gmd);
 			genmodelURI = gmd.createDefault(getProject().getName(), URIUtil.getFile(mapURI));
 			assertNotNull("GenModel URI should be created", genmodelURI);
 			IFile file = URIUtil.getFile(mapURI);
 			assertNotNull("GenModel file should be created", file);
 			assertTrue("GenModel file should exists", file.exists());
 		} catch (CoreException e) {
 			fail("Failed to create default GenModel: " + e.getMessage());
 		}
 		return genmodelURI;
 	}
 
 	protected IProject createProject() {
 		String projectName = "TransformToGenModelOperationTest_" + String.valueOf(System.currentTimeMillis()); //$NON-NLS-1$
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
 
 	protected IProject getProject() {
 		return myProject;
 	}
 	
 	private URI prepareResources() {
 		ResourceSet rs = new ResourceSetImpl();
 		prepareResource(rs, FILE_EXT_ECORE, getSetup().getDomainModel().getModel());
 		prepareResource(rs, FILE_EXT_GMFGRAPH, getSetup().getGraphDefModel().getCanvasDef());
 		prepareResource(rs, FILE_EXT_GMFTOOL, getSetup().getToolDefModel().getRegistry());
 		URI mapURI = prepareResource(rs, FILE_EXT_GMFMAP, getSetup().getMapModel().getMapping());
 		return mapURI;
 	}
 
 	private URI prepareDesignResources() {
 		ResourceSet rs = new ResourceSetImpl();
 		DiaDefSource ddSource = getSetup().getGraphDefModel();
 		prepareResource(rs, FILE_EXT_GMFGRAPH, ddSource.getCanvasDef());
 		ToolDefSource toolDef = getSetup().getToolDefModel();
 		prepareResource(rs, FILE_EXT_GMFTOOL, toolDef.getRegistry());
 		URI mapURI = prepareResource(rs, FILE_EXT_GMFMAP, new DesignMapDefSource().init(ddSource, toolDef).getMapping());
 		return mapURI;
 	}
 
 	private URI prepareResource(ResourceSet rs, String extension, EObject root) {
 		URI uri = createURI(extension);
 		Resource resource = rs.createResource(uri);
 		resource.getContents().add(root);
 		try {
 			resource.save(Collections.EMPTY_MAP);
 		} catch (IOException e) {
 			fail("Failed to save " + uri.toFileString() + " :" + e.getMessage());
 		}
 		return uri;
 	}
 
 	private URI createURI(String extension) {
 		String pathName = getProject().getName() + 
 		IPath.SEPARATOR + FOLDER_MODELS + 
 		IPath.SEPARATOR + FILE_BASE + "." + extension; //$NON-NLS-1$
 		URI uri = URI.createPlatformResourceURI(pathName, true);
 		return uri;
 	}
 	
 	private void assertIllegalArgument(CoreException e) {
 		Throwable cause = e.getStatus().getException();
 		if (!(cause instanceof IllegalArgumentException)) {
 			fail(e.getMessage());
 		}
 	}
 
 	private void assertIllegalState(CoreException e) {
 		Throwable cause = e.getStatus().getException();
 		if (!(cause instanceof IllegalStateException)) {
 			fail(e.getMessage());
 		}
 	}
 	
 	private class DesignMapDefSource implements MapDefSource {
 
 		private Mapping myMap;
 
 		private NodeMapping myNodeA;
 
 		private NodeMapping myNodeB;
 
 		private LinkMapping myClassLink;
 
 		private LinkMapping myRefLink;
 		
 		
 		public DesignMapDefSource() {
 			// just create
 		}
 
 		public DesignMapDefSource init(DiaDefSource ddSource, ToolDefSource toolDef) {
 			initCanvasMapping(ddSource.getCanvasDef());
 			if (toolDef.getMainMenu() != null) {
 				myMap.getDiagram().getMenuContributions().add(toolDef.getMainMenu());
 			}
 			myMap.getDiagram().setPalette(toolDef.getPalette());
 			
 			initCommonMapping(ddSource, toolDef);
 
 			return this;
 		}
 
 		private void initCanvasMapping(Canvas canvas) {
 			Mapping m = GMFMapFactory.eINSTANCE.createMapping();		
 				
 			CanvasMapping cme = GMFMapFactory.eINSTANCE.createCanvasMapping();
 			cme.setDiagramCanvas(canvas);
 			m.setDiagram(cme);
 			myMap = m;
 		}
 
 		private void initCommonMapping(DiaDefSource ddSource, ToolDefSource toolDef) {
 			myNodeA = createNodeMapping(ddSource.getNodeDef(), ddSource.getLabelDef(), true);
 			myNodeA.setContextMenu(toolDef.getNodeContextMenu());
 			myNodeA.setTool(toolDef.getNodeCreationTool());
 			
 			DiaDefSetup ddSetup = ddSource instanceof DiaDefSetup ? (DiaDefSetup) ddSource : null;
 			Node graphNode = ddSetup != null ? ddSetup.getColoredNodeDef() : ddSource.getNodeDef();
 			DiagramLabel graphLabel = ddSetup != null ? ddSetup.getDecoratedLabelDef() : ddSource.getLabelDef();
 			myNodeB = createNodeMapping(graphNode, graphLabel, true);
 			
 			myClassLink = createLinkMapping(ddSource.getLinkDef());
 			myClassLink.setTool(toolDef.getLinkCreationTool());
 			myRefLink = createLinkMapping(ddSetup != null ? ddSetup.getColoredLinkDef() : ddSource.getLinkDef());
 		}
 
 		private final NodeMapping createNodeMapping(Node nodeDef, DiagramLabel labelDef, boolean addTopNodeReference) {
 			NodeMapping nme = GMFMapFactory.eINSTANCE.createNodeMapping();
 			nme.setDiagramNode(nodeDef);
 		
 			if (addTopNodeReference){
 				TopNodeReference tnr = GMFMapFactory.eINSTANCE.createTopNodeReference();
 				tnr.setOwnedChild(nme);
 				myMap.getNodes().add(tnr);
 			}
 			return nme;	
 		}
 
 		private LinkMapping createLinkMapping(Connection link) {		
 			LinkMapping lme = GMFMapFactory.eINSTANCE.createLinkMapping();
 			lme.setDiagramLink(link);
 			myMap.getLinks().add(lme);
 			return lme;
 		}
 
 		public final Mapping getMapping() {
 			return myMap;
 		}
 
 		public NodeMapping getNodeA() {
 			return myNodeA;
 		}
 
 		public NodeMapping getNodeB() {
 			return myNodeB;
 		}
 
 		public LinkMapping getClassLink() {
 			return myClassLink;
 		}
 
 		public LinkMapping getReferenceLink() {
 			return myRefLink;
 		}
 		
 	}
 }
