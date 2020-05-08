 package edu.teco.dnd.graphiti;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.util.ClassPath;
 import org.apache.bcel.util.Repository;
 import org.apache.bcel.util.SyntheticRepository;
 import org.apache.logging.log4j.Level;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddConnectionContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 
 import edu.teco.dnd.blocks.FunctionBlockClass;
 import edu.teco.dnd.blocks.FunctionBlockClassFactory;
 import edu.teco.dnd.eclipse.EclipseUtil;
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.graphiti.model.OptionModel;
 import edu.teco.dnd.meeting.BeamerActorBlock;
 import edu.teco.dnd.meeting.BeamerOperatorBlock;
 import edu.teco.dnd.meeting.DisplayActorBlock;
 import edu.teco.dnd.meeting.DisplayOperatorBlock;
 import edu.teco.dnd.meeting.LightSensorBlock;
 import edu.teco.dnd.meeting.MeetingOperatorBlock;
 import edu.teco.dnd.meeting.OutletActorBlock;
 import edu.teco.dnd.meeting.OutletSensorBlock;
 import edu.teco.dnd.temperature.TemperatureActorBlock;
 import edu.teco.dnd.temperature.TemperatureOperatorBlock;
 import edu.teco.dnd.temperature.TemperatureSensorBlock;
 import edu.teco.dnd.util.ClassScanner;
 import edu.teco.dnd.util.StringUtil;
 
 /**
  * Provides the features that are used by the editor.
  */
 public class DNDFeatureProvider extends DefaultFeatureProvider {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(DNDFeatureProvider.class);
 
 	private Resource resource = null;
 
 	/**
 	 * Extension for the files the blocks are stored in. This file can be used to deploy the blocks after they've been
 	 * assembled in a .diagram file. This should be the same for all languages and therefore wasn't moved to the
 	 * Messages.
 	 */
 	public static final String BLOCKS_FILE_EXTENSION = "blocks";
 
 	/**
 	 * Default FunctionBlocks.
 	 */
 	private static final Class<?>[] DEFAULT_TYPES = new Class<?>[] { OutletActorBlock.class, BeamerOperatorBlock.class,
 			DisplayActorBlock.class, DisplayOperatorBlock.class, LightSensorBlock.class, MeetingOperatorBlock.class,
 			OutletSensorBlock.class, TemperatureActorBlock.class, TemperatureOperatorBlock.class,
 			TemperatureSensorBlock.class, BeamerActorBlock.class };
 
 	/**
 	 * Feature factory for block create features.
 	 */
 	private final DNDCreateFeatureFactory createFeatureFactory = new DNDCreateFeatureFactory();
 
 	/**
 	 * Used to inspect classes (including loading through {@link #blockFactory}.
 	 */
 	private Repository repository;
 
 	/**
 	 * Used to inspect FunctionBlocks.
 	 */
 	private FunctionBlockClassFactory blockFactory;
 
 	/**
 	 * Whether or not {@link #createFeatureFactory} was initialised.
 	 */
 	private boolean factoryInitialised = false;
 
 	/**
 	 * Passes the {@link IDiagramTypeProvider} to the super constructor.
 	 * 
 	 * @param dtp
 	 *            the diagram type provider this feature provider belongs to
 	 * @throws ClassNotFoundException
 	 */
 	public DNDFeatureProvider(final IDiagramTypeProvider dtp) throws ClassNotFoundException {
 		super(dtp);
 		LOGGER.info("DNDFeatureProvider created successfully");
 	}
 
 	/**
 	 * Returns the class path for the Eclipse project this diagram is part of.
 	 * 
 	 * @return the class path for the enclosing Eclipse project
 	 */
 	private Set<IPath> getProjectClassPath() {
 		final Diagram diagram = getDiagramTypeProvider().getDiagram();
 		if (diagram == null) {
 			return Collections.emptySet();
 		}
 		IProject project = EclipseUtil.getWorkspaceProject(URI.createURI(EcoreUtil.getURI(diagram).toString()));
 		return EclipseUtil.getAbsoluteBinPaths(project);
 	}
 
 	/**
 	 * Registers default create features at the factory.
 	 */
 	private void registerDefaultTypes() {
 		final FunctionBlockClassFactory factory;
 		try {
 			factory = new FunctionBlockClassFactory(SyntheticRepository.getInstance());
 		} catch (final ClassNotFoundException e) {
 			LOGGER.catching(Level.WARN, e);
 			return;
 		}
 		for (Class<?> type : DEFAULT_TYPES) {
 			try {
 				createFeatureFactory.registerBlockType(factory.getFunctionBlockClass(type));
 			} catch (final ClassNotFoundException e) {
 				LOGGER.catching(Level.WARN, e);
 			}
 		}
 	}
 
 	/**
 	 * Returns the CreateFeatures for all loaded blocks.
 	 * 
 	 * @return CreateFeatures for all loaded blocks
 	 */
 	public final synchronized ICreateFeature[] getCreateFeatures() {
 		initialiseFactory();
 		return createFeatureFactory.getCreateFeatures(this).toArray(new ICreateFeature[0]);
 	}
 
 	/**
 	 * Used to initialize a Factory.
 	 */
 	private synchronized void initialiseFactory() {
 		if (!factoryInitialised) {
 			final Set<IPath> ipaths = getProjectClassPath();
 			repository = SyntheticRepository.getInstance(new ClassPath(StringUtil.joinIterable(ipaths, ":")));
 			try {
 				blockFactory = new FunctionBlockClassFactory(repository);
 			} catch (final ClassNotFoundException e) {
 				LOGGER.catching(e);
 			}
 			registerDefaultTypes();
 			final Set<File> paths = new HashSet<File>();
 			for (final IPath ipath : getProjectClassPath()) {
 				paths.add(ipath.toFile());
 			}
 			final ClassScanner scanner = new ClassScanner(repository);
 			for (final JavaClass cls : scanner.getClasses(paths)) {
 				final FunctionBlockClass blockClass;
 				try {
 					blockClass = blockFactory.getFunctionBlockClass(cls.getClassName());
 				} catch (final ClassNotFoundException e) {
 					continue;
 				} catch (final IllegalArgumentException e) {
 					continue;
 				}
 				createFeatureFactory.registerBlockType(blockClass);
 			}
 			factoryInitialised = true;
 		}
 	}
 
 	/**
 	 * Returns the FunctionBlockFactory.
 	 * 
 	 * @return the FunctionBlockFactory
 	 */
 	public final synchronized FunctionBlockClassFactory getFunctionBlockFactory() {
 		initialiseFactory();
 		return blockFactory;
 	}
 
 	public final Repository getRepository() {
 		return repository;
 	}
 
 	/**
 	 * Returns the AddFeatures for FunctionBlocks and data connections.
 	 * 
 	 * @param context
 	 *            the context for which to return a feature
 	 * @return a matching AddFeature
 	 */
 	@Override
 	public final IAddFeature getAddFeature(final IAddContext context) {
 		LOGGER.entry(context);
 		if (context instanceof IAddConnectionContext) {
 			return new DNDAddDataConnectionFeature(this);
 		} else if (context.getNewObject() instanceof FunctionBlockModel) {
 			return new DNDAddBlockFeature(this);
 		}
 		return super.getAddFeature(context);
 	}
 
 	/**
 	 * Returns the LayoutFeature for FunctionBlocks.
 	 * 
 	 * @param context
 	 *            the context for which to return a feature
 	 * @return the LayoutFeature for FunctionBlocks
 	 */
 	@Override
 	public final ILayoutFeature getLayoutFeature(final ILayoutContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
 		if (bo instanceof FunctionBlockModel) {
 			return new DNDLayoutBlockFeature(this);
 		}
 		return super.getLayoutFeature(context);
 	}
 
 	/**
 	 * Returns the UpdateFeature for FunctionBlocks.
 	 * 
 	 * @param context
 	 *            the context for which to return a feature
 	 * @return the UpdateFeature for FunctionBlocks
 	 */
 	@Override
 	public final IUpdateFeature getUpdateFeature(final IUpdateContext context) {
 		PictogramElement pe = context.getPictogramElement();
 		Object bo = getBusinessObjectForPictogramElement(pe);
 		if (bo instanceof OptionModel) {
 			return new DNDUpdateOptionFeature(this);
 		} else if (bo instanceof FunctionBlockModel) {
 			GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 			if (TypePropertyUtil.isPositionText(ga)) {
 				return new DNDUpdatePositionFeature(this);
 			} else if (TypePropertyUtil.isBlockNameText(ga)) {
 				return new DNDUpdateBlockNameFeature(this);
 			} else if (TypePropertyUtil.isBlockShape(ga)) {
 				return new DNDUpdateBlockFeature(this);
 			}
 		}
 		return super.getUpdateFeature(context);
 	}
 
 	/**
 	 * Returns the CreateConnectionFeatures for data connections.
 	 * 
 	 * @return the CreateConnectionFeatures for data connections
 	 */
 	@Override
 	public final ICreateConnectionFeature[] getCreateConnectionFeatures() {
 		return new ICreateConnectionFeature[] { new DNDCreateDataConnectionFeature(this) };
 	}
 
 	/**
 	 * Returns a DirectEditFeature for Options and positions.
 	 * 
 	 * @param context
 	 *            the context for which to return a feature
 	 * @return a matching DirectEditFeature
 	 */
 	@Override
 	public final IDirectEditingFeature getDirectEditingFeature(final IDirectEditingContext context) {
 		LOGGER.entry(context);
 		PictogramElement pe = context.getPictogramElement();
 		Object bo = getBusinessObjectForPictogramElement(pe);
 		IDirectEditingFeature feature = null;
 		if (bo instanceof OptionModel) {
 			feature = new DNDEditOptionFeature(this);
 		} else if (bo instanceof FunctionBlockModel) {
 			GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 			if (TypePropertyUtil.isBlockNameText(ga)) {
 				feature = new DNDEditBlockNameFeature(this);
 			} else if (TypePropertyUtil.isPositionText(ga)) {
 				feature = new DNDEditPositionFeature(this);
 			}
 		}
 		if (feature == null) {
 			feature = super.getDirectEditingFeature(context);
 		}
 		LOGGER.exit(feature);
 		return feature;
 	}
 
 	@Override
 	public final IRemoveFeature getRemoveFeature(final IRemoveContext context) {
 		if (context.getPictogramElement() instanceof Connection) {
 			return new DNDRemoveDataConnectionFeature(this);
 		}
 		return super.getRemoveFeature(context);
 	}
 
 	@Override
 	public final ICustomFeature[] getCustomFeatures(final ICustomContext context) {
 		return new ICustomFeature[] { new DNDDebugFeature(this), new DNDCustomUpdateFeature(this) };
 	}
 
 	@Override
 	public final IFeature[] getDragAndDropFeatures(final IPictogramElementContext context) {
 		return getCreateConnectionFeatures();
 	}
 
 	/**
 	 * Returns the resource containing the desired FunctionBlockModels. Use this method to get to the resource whenever
 	 * you need it (to get to or add FunctionBlockModels) instead of creating a new one or getting it from somewhere
 	 * else. TThat way multiple competitive resources or files can be prevented.
 	 * 
 	 * @return the resource containing FunctionBlockModels.
 	 */
 	public synchronized Resource getEMFResource() {
 		if (resource == null) {
 			Diagram d = getDiagramTypeProvider().getDiagram();
 
 			URI uri = d.eResource().getURI();
 			uri = uri.trimFragment();
 			uri = uri.trimFileExtension();
 			uri = uri.appendFileExtension(BLOCKS_FILE_EXTENSION);
 
 			ResourceSet rSet = d.eResource().getResourceSet();
 			final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 			IResource file = workspaceRoot.findMember(uri.toPlatformString(true));
 			if (file == null || !file.exists()) {
 				Resource createResource = rSet.createResource(uri);
 				try {
 					createResource.save(null);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				createResource.setTrackingModification(true);
 			}
 			resource = rSet.getResource(uri, true);
 			resource.setTrackingModification(true);
 		}
 		return resource;
 	}
 
 	/**
 	 * Updates the contents (FunctionBlockModels) of its resource. This can be invoked whenever @emfResourceChanged()
 	 * returns true, but also works in case nothing changed. This method updates the BlockName and Position of all
 	 * FunctionBlockModel in the resource to be consistent with changes made outside the resource.
 	 */
 	public synchronized void updateEMFResource() {
 		Resource newResource = getNewEMFResource();
 
 		Collection<FunctionBlockModel> oldModels = new ArrayList<FunctionBlockModel>();
 		for (EObject obj : resource.getContents()) {
 			if (obj instanceof FunctionBlockModel) {
 				oldModels.add((FunctionBlockModel) obj);
 			}
 		}
 
 		for (EObject obj : newResource.getContents()) {
 			if (obj instanceof FunctionBlockModel) {
 				FunctionBlockModel newModel = (FunctionBlockModel) obj;
 				for (FunctionBlockModel oldModel : oldModels) {
 					if (oldModel.getID().equals(newModel.getID())) {
 						oldModel.setPosition(newModel.getPosition());
 						oldModel.setBlockName(newModel.getBlockName());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Updates the BlockName of one FunctionBlockModel in the resource to be consistent with changes made outside the
 	 * resource.
 	 * 
 	 * @param blockID
 	 *            UUID of the FunctionBlockModel that changed its name
 	 * @param newName
 	 *            new blockName of the FunctionBlockModel
 	 */
 	public synchronized void updateEMFResourceName(UUID blockID, String newName) {
 		FunctionBlockModel model;
 		Resource newResource = getNewEMFResource();
 		for (EObject obj : newResource.getContents()) {
 			if (obj instanceof FunctionBlockModel) {
 				model = (FunctionBlockModel) obj;
 				if (model.getID().equals(blockID)) {
 					model.setBlockName(newName);
 					try {
 						newResource.save(null);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Updates the Position of one FunctionBlockModel in the resource to be consistent with changes made outside the
 	 * resource.
 	 * 
 	 * @param blockID
 	 *            UUID of the FunctionBlockModel that changed its name
 	 * @param newPosition
 	 *            new position of the FunctionBlockModel
 	 */
 	public synchronized void updateEMFResourcePosition(UUID blockID, String newPosition) {
 		FunctionBlockModel model;
 		Resource newResource = getNewEMFResource();
 		for (EObject obj : newResource.getContents()) {
 			if (obj instanceof FunctionBlockModel) {
 				model = (FunctionBlockModel) obj;
 				if (model.getID().equals(blockID)) {
 					model.setPosition(newPosition);
 					try {
 						newResource.save(null);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the FunctionBlockModels contained by the resource have been changed outside this specific
 	 * resource.
 	 * 
 	 * @return true if changes happened, false if not.
 	 */
 	public synchronized boolean emfResourceChanged() {
 		Resource newResource = getNewEMFResource();
 
 		Collection<FunctionBlockModel> oldModels = new ArrayList<FunctionBlockModel>();
 		for (EObject obj : resource.getContents()) {
 			if (obj instanceof FunctionBlockModel) {
 				oldModels.add((FunctionBlockModel) obj);
 			}
 		}
 
 		for (EObject obj : newResource.getContents()) {
 			if (obj instanceof FunctionBlockModel) {
 				FunctionBlockModel newModel = (FunctionBlockModel) obj;
 				for (FunctionBlockModel oldModel : oldModels) {
					if (oldModel.getID().equals(newModel.getID())
							&& !(oldModel.getPosition().equals(newModel.getPosition()) && oldModel.getBlockName()
									.equals(newModel.getBlockName()))) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Used by updateEMFResource and emfResourceChanged to load the new resource.
 	 * 
 	 * @return new Resource.
 	 */
 	private Resource getNewEMFResource() {
 		if (resource == null) {
 			getEMFResource();
 		}
 		Diagram d = getDiagramTypeProvider().getDiagram();
 
 		URI uri = d.eResource().getURI();
 		uri = uri.trimFragment();
 		uri = uri.trimFileExtension();
 		uri = uri.appendFileExtension(BLOCKS_FILE_EXTENSION);
 
 		Resource newResource = new XMIResourceImpl(uri);
 		try {
 			newResource.load(null);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return newResource;
 	}
 }
