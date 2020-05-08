 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
 *    mwenz - Bug 356828 - Escaped diagram name is used as editor title
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.bot.tests;
 
 import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.bot.pageobjects.PoDiagramEditor;
 import org.eclipse.graphiti.bot.pageobjects.PoWorkbenchPage;
 import org.eclipse.graphiti.bot.tests.util.ITestConstants;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.examples.common.ExampleProjectNature;
 import org.eclipse.graphiti.examples.common.FileService;
 import org.eclipse.graphiti.features.ConfigurableFeatureProviderWrapper;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.AreaContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.CreateContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.testtool.sketch.SketchFeatureProvider;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
 import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
 import org.eclipse.swtbot.swt.finder.results.Result;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.ui.PlatformUI;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 public abstract class AbstractGFTests extends SWTBotGefTestCase {
 
 	public static final IPath SOURCE_FOLDER = new Path("src"); //$NON-NLS-1$
 	private static IPath DIAGRAMS_FOLDER = SOURCE_FOLDER.append("diagrams");
 	final protected PoDiagramEditor ed = new PoDiagramEditor();
 	final protected PoWorkbenchPage page = new PoWorkbenchPage();
 	private static final String PROJECT_NAME = "GraphitiTestProject";
 	private IProject project;
 
 	public AbstractGFTests() {
 		super();
 		setName(getClass().getName());
 	}
 
 	@BeforeClass
 	public static void beforeClass() {
 
 	}
 
 	protected void addClassToDiagram(IFeatureProvider fp, Diagram diagram, int x, int y, String className) {
 
 		// create add context
 		AreaContext areaContext = new AreaContext();
 		areaContext.setLocation(x, y);
 		EClass newEClass = createEClass(diagram, className);
 		AddContext addContext = new AddContext(areaContext, newEClass);
 		addContext.setTargetContainer(diagram);
 
 		// get add class feature
 		IAddFeature feature = fp.getAddFeature(addContext);
 		assertNotNull("add class feature not available", feature);
 
 		if (feature.canAdd(addContext)) {
 			feature.execute(addContext);
 		}
 	}
 	
 	
 	public static void executeInRecordingCommand(IDiagramEditor diagramEditor, final Runnable run){
 		TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 
 			@Override
 			protected void doExecute() {
 				run.run();
 			}
 		});
 	}
 	
 	public static void executeInRecordingCommand(TransactionalEditingDomain domain, final Runnable run){
 		domain.getCommandStack().execute(new RecordingCommand(domain) {
 
 			@Override
 			protected void doExecute() {
 				run.run();
 			}
 		});
 	}
 	
 	public static void executeInRecordingCommandInUIThread(final IDiagramEditor diagramEditor, final Runnable run){
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				executeInRecordingCommand(diagramEditor, run);
 			}
 		});
 	}
 	
 	
 	protected void addClassesAndReferenceToDiagram(IFeatureProvider fp, Diagram diagram, int sourceX, int sourceY, String sourceClassName,
 			int targetX, int targetY, String targetClassName) {
 		addClassToDiagram(fp, diagram, sourceX, sourceY, sourceClassName);
 		addClassToDiagram(fp, diagram, targetX, targetY, targetClassName);
 		addReferenceToDiagram(fp, diagram, sourceClassName, targetClassName);
 
 	}
 
 	protected void addEnumToDiagram(IFeatureProvider fp, Diagram diagram, int x, int y, String enumName) {
 
 		// create add context
 		AreaContext areaContext = new AreaContext();
 		areaContext.setLocation(x, y);
 		EEnum newEEnum = createEEnum(diagram, enumName);
 		AddContext addContext = new AddContext(areaContext, newEEnum);
 		addContext.setTargetContainer(diagram);
 
 		// get add class feature
 		IAddFeature addFeature = fp.getAddFeature(addContext);
 		assertNotNull("add class feature not available", addFeature);
 
 		if (addFeature.canAdd(addContext)) {
 			addFeature.execute(addContext);
 		}
 	}
 
 	protected Diagram createDiagram(String diagramTypeId) {
 		return createDiagram(diagramTypeId, "xmi");
 	}
 
 
 	/**
 	 * Create Diagram and diagram file.
 	 * 
 	 * @param diagramTypeId
 	 * @param fileExtension
 	 * @return
 	 */
 	protected Diagram createDiagram(String diagramTypeId, String fileExtension) {
 		return createDiagram(diagramTypeId, fileExtension, "diagram");
 	}
 
 	/**
 	 * Create Diagram and diagram file.
 	 * 
 	 * @param diagramTypeId
 	 * @param fileExtension
 	 * @return
 	 */
 	protected Diagram createDiagram(String diagramTypeId, String fileExtension, String diagramName) {
 
 		String fileName = createDiagramFileName(fileExtension);
 		URI diagramUri = createDiagramFileUri(fileName);
 		final Diagram diagram = getPeService().createDiagram(diagramTypeId, diagramName, true);
 
 		TransactionalEditingDomain editingDomain = FileService.createEmfFileForDiagram(diagramUri, diagram);
 		ed.setEditingDomain(editingDomain);
 		return diagram;
 	}
 
 
 	protected EEnum createEEnum(Diagram diagram, String enumName) {
 		EEnum newEnum = EcoreFactory.eINSTANCE.createEEnum();
 		newEnum.setName(enumName);
 
 		diagram.eResource().getContents().add(newEnum);
 
 		return newEnum;
 	}
 
 	protected EClass createEClass(Diagram diagram, String className) {
 		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
 		eClass.setName(className);
 
 		diagram.eResource().getContents().add(eClass);
 
 		return eClass;
 	}
 
 	protected Shape findShapeForEClass(Diagram diagram, String className) {
 		Object[] allShapes = getPeService().getAllContainedShapes(diagram).toArray();
 		Shape shape = null;
 		for (int i = 0; i < allShapes.length; i++) {
 			shape = (Shape) allShapes[i];
 			if (shape instanceof ContainerShape) {
 				Object bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(shape);
 				if (bo instanceof EClass) {
 					String boName = ((EClass) bo).getName();
 					if (boName.equals(className)) {
 						return shape;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 
 	protected void moveClassShape(IFeatureProvider fp, Diagram diagram, int x, int y, String className) {
 
 		Shape shape = findShapeForEClass(diagram, className);
 
 		MoveShapeContext moveShapeContext = new MoveShapeContext(shape);
 
 		moveShapeContext.setSourceContainer(shape.getContainer());
 		moveShapeContext.setTargetContainer(shape.getContainer());
 		moveShapeContext.setLocation(x, y);
 		moveShapeContext.setDeltaX(x - shape.getGraphicsAlgorithm().getX());
 		moveShapeContext.setDeltaY(y - shape.getGraphicsAlgorithm().getY());
 
 		IMoveShapeFeature moveShapeFeature = fp.getMoveShapeFeature(moveShapeContext);
 		assertNotNull("move shape feature not available", moveShapeFeature);
 
 		if (moveShapeFeature.canMoveShape(moveShapeContext)) {
 			moveShapeFeature.execute(moveShapeContext);
 		}
 	}
 
 	protected DiagramEditor openDiagram(final String type) {
 		DiagramEditor diagramEditor = syncExec(new Result<DiagramEditor>() {
 			@Override
 			public DiagramEditor run() {
 
 				/**/
 				final Diagram newDiagram = createDiagram(type);
 				assertTrue("create diagram does not work", newDiagram != null);
 
 				//				assertEditingDomainSave(getTransactionalEditingDomain());
 
 				// use TestUtil to open editor since this waits for late
 				// initialization
 				DiagramEditor diagramEditor = (DiagramEditor) GraphitiUiInternal.getWorkbenchService().openDiagramEditor(newDiagram,
 						ed.getTransactionalEditingDomain(), false);
 				return diagramEditor;
 			}
 		});
 		if (ITestConstants.DIAGRAM_TYPE_ID_SKETCH.equals(type)) {
 			IFeatureProvider featureProvider = diagramEditor.getDiagramTypeProvider().getFeatureProvider();
 			if (featureProvider instanceof ConfigurableFeatureProviderWrapper) {
 				ConfigurableFeatureProviderWrapper fpw = (ConfigurableFeatureProviderWrapper) featureProvider;
 				IFeatureProvider innerFeatureProvider = fpw.getInnerFeatureProvider();
 				if (innerFeatureProvider instanceof SketchFeatureProvider) {
 					((SketchFeatureProvider) innerFeatureProvider).setTestMode(true);
 				}
 			}
 		}
 		return diagramEditor;
 	}
 
 	@Override
 	@Before
 	protected void setUp() throws Exception {
 		//		T.racer().setInfoAlwaysTrue(true); // tracing enabled for testing
 
 		super.setUp();
 
 		// Force activation of current shell to avoid "no widget found"
 		// exceptions
 		// see http://www.eclipse.org/forums/index.php?t=msg&goto=484090&
 		UIThreadRunnable.syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
 			}
 		});
 
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceRoot root = workspace.getRoot();
 		IProject project = root.getProject(PROJECT_NAME);
 		project.create(null);
 		project.open(null);
 		IProjectDescription desc = project.getDescription();
 		desc.setNatureIds(new String[] { ExampleProjectNature.NATURE_ID });
 		project.setDescription(desc, null);
 
 		this.project = project;
 		page.closeWelcomeView(this);
 		page.openGraphitiTestPerspective();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		ed.cleanupEditingDomain();
 		if (this.project != null) {
 			this.project.close(null);
 			this.project.delete(true, null);
 		}
 		super.tearDown();
 	}
 
 
 	private String createDiagramFileName(String extension) {
 
 		List<String> currentDiagramNames = new ArrayList<String>();
 
 		IFolder folder = getProject().getFolder(DIAGRAMS_FOLDER);
 		try {
 			if (folder.exists()) {
 				IResource[] members = folder.members();
 				for (IResource resource : members) {
 					if (resource instanceof IFile) {
 						String fileName = ((IFile) resource).getName();
 						if (fileName.startsWith(ITestConstants.TEST_DIAGRAM_PREFIX)) {
 							int indexOf = fileName.indexOf('.');
 							if (indexOf > 0) {
 								fileName = fileName.substring(0, indexOf);
 							}
 							currentDiagramNames.add(fileName);
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 		int i = 0;
 		String diagramName;
 
 		do {
 			diagramName = ITestConstants.TEST_DIAGRAM_PREFIX + i++;
 		} while (currentDiagramNames.contains(diagramName));
 
 		diagramName = diagramName + "." + extension;
 
 		return diagramName;
 	}
 
 	private URI createDiagramFileUri(String diagramName) {
 		URI uri = null;
 		IFolder folder = getProject().getFolder(DIAGRAMS_FOLDER);
 		IFile file = folder.getFile(diagramName);
 		uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
 		return uri;
 	}
 
 	private IProject getProject() {
 		return this.project;
 	}
 
 	private void addReferenceToDiagram(IFeatureProvider fp, Diagram diagram, String sourceClass, String targetClass) {
 		ICreateConnectionFeature[] ccfs = fp.getCreateConnectionFeatures();
 		if (ccfs != null) {
 			Shape sourceShape = findShapeForEClass(diagram, sourceClass);
 			Anchor sourceAnchor = getPeService().getChopboxAnchor(sourceShape);
 			Shape targetShape = findShapeForEClass(diagram, targetClass);
 			Anchor targetAnchor = getPeService().getChopboxAnchor(targetShape);
 			CreateConnectionContext ccc = new CreateConnectionContext();
 			ccc.setSourceAnchor(sourceAnchor);
 			ccc.setTargetAnchor(targetAnchor);
 			// question instantiate create feature directly? 
 			for (ICreateConnectionFeature ccf : ccfs) {
 				if (ccf.canCreate(ccc)) {
 					ccf.execute(ccc);
 					break;
 				}
 			}
 		}
 	}
 
 	protected IPeService getPeService() {
 		return Graphiti.getPeService();
 	}
 
 
 	protected ICreateContext createCreateContext(ContainerShape target, Rectangle rect) {
 		CreateContext ret = new CreateContext();
 		ret.setTargetContainer(target);
 		ret.setX(rect.x);
 		ret.setY(rect.y);
 		ret.setWidth(rect.width);
 		ret.setHeight(rect.height);
 
 		return ret;
 	}
 
 	protected void createClassesAndConnection(final int x, final int y, final IDiagramTypeProvider diagramTypeProvider, final String toolToActivate, final String shapename) {
 		syncExec(new VoidResult() {
 			@Override
 			public void run() {
 				final IFeatureProvider fp = diagramTypeProvider.getFeatureProvider();
 				final Diagram currentDiagram = diagramTypeProvider.getDiagram();
 				executeInRecordingCommand(diagramTypeProvider.getDiagramEditor(), new Runnable() {
 					@Override
 					public void run() {
 						addClassesAndReferenceToDiagram(fp, currentDiagram, x, y, shapename, x, y + 300, "ConnectionDecorator");
 					}
 				});
 				if (toolToActivate != null)
 					ed.getGefEditor().activateTool(toolToActivate);
 			}
 		});
 	}
 
 	protected Event createMouseEvent(int x, int y, int button, int stateMask, int count) {
 		Event event = new Event();
 		event.time = (int) System.currentTimeMillis();
 		event.x = x;
 		event.y = y;
 		event.button = button;
 		event.stateMask = stateMask;
 		event.count = count;
 		return event;
 	}
 }
