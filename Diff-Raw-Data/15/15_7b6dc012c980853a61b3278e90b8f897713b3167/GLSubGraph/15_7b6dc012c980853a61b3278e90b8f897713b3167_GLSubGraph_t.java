 package org.caleydo.view.subgraph;
 
 import gleem.linalg.Vec2f;
 
 import java.awt.Point;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.media.opengl.GL2;
 
 import org.caleydo.core.data.datadomain.DataDomainManager;
 import org.caleydo.core.data.datadomain.IDataSupportDefinition;
 import org.caleydo.core.data.perspective.table.TablePerspective;
 import org.caleydo.core.data.perspective.variable.Perspective;
 import org.caleydo.core.data.selection.EventBasedSelectionManager;
 import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
 import org.caleydo.core.data.selection.SelectionType;
 import org.caleydo.core.event.EventListenerManager.ListenTo;
 import org.caleydo.core.event.EventPublisher;
 import org.caleydo.core.event.view.MinSizeUpdateEvent;
 import org.caleydo.core.id.IDType;
 import org.caleydo.core.manager.GeneralManager;
 import org.caleydo.core.serialize.ASerializedView;
 import org.caleydo.core.util.collection.Pair;
 import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
 import org.caleydo.core.view.ViewManager;
 import org.caleydo.core.view.contextmenu.AContextMenuItem;
 import org.caleydo.core.view.opengl.camera.ViewFrustum;
 import org.caleydo.core.view.opengl.canvas.AGLView;
 import org.caleydo.core.view.opengl.canvas.IGLCanvas;
 import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
 import org.caleydo.core.view.opengl.canvas.IGLMouseListener;
 import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
 import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
 import org.caleydo.core.view.opengl.layout.util.multiform.IMultiFormChangeListener;
 import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
 import org.caleydo.core.view.opengl.layout2.AGLElementGLView;
 import org.caleydo.core.view.opengl.layout2.GLElement;
 import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
 import org.caleydo.core.view.opengl.layout2.GLElementAdapter;
 import org.caleydo.core.view.opengl.layout2.GLElementContainer;
 import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
 import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
 import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
 import org.caleydo.core.view.opengl.layout2.layout.GLSizeRestrictiveFlowLayout;
 import org.caleydo.core.view.opengl.picking.PickingMode;
 import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
 import org.caleydo.datadomain.genetic.EGeneIDTypes;
 import org.caleydo.datadomain.pathway.IPathwayRepresentation;
 import org.caleydo.datadomain.pathway.PathwayDataDomain;
 import org.caleydo.datadomain.pathway.VertexRepBasedContextMenuItem;
 import org.caleydo.datadomain.pathway.VertexRepBasedEventFactory;
 import org.caleydo.datadomain.pathway.data.PathwayTablePerspective;
 import org.caleydo.datadomain.pathway.graph.PathwayGraph;
 import org.caleydo.datadomain.pathway.graph.PathwayPath;
 import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
 import org.caleydo.datadomain.pathway.listener.EnablePathSelectionEvent;
 import org.caleydo.datadomain.pathway.listener.PathwayMappingEvent;
 import org.caleydo.datadomain.pathway.listener.PathwayPathSelectionEvent;
 import org.caleydo.datadomain.pathway.listener.ShowNodeContextEvent;
 import org.caleydo.datadomain.pathway.manager.PathwayManager;
 import org.caleydo.view.subgraph.GLWindow.ICloseWindowListener;
 import org.caleydo.view.subgraph.MultiLevelSlideInElement.IWindowState;
 import org.caleydo.view.subgraph.SlideInElement.ESlideInElementPosition;
 import org.caleydo.view.subgraph.contextmenu.ShowCommonNodeItem;
 import org.caleydo.view.subgraph.datamapping.GLExperimentalDataMapping;
 import org.caleydo.view.subgraph.event.ShowCommonNodePathwaysEvent;
 import org.caleydo.view.subgraph.event.ShowCommonNodesPathwaysEvent;
 import org.caleydo.view.subgraph.event.ShowPortalsEvent;
 import org.caleydo.view.subgraph.ranking.PathwayFilters;
 import org.caleydo.view.subgraph.ranking.PathwayRankings;
 import org.caleydo.view.subgraph.ranking.RankingElement;
 import org.caleydo.view.subgraph.toolbar.ShowPortalsAction;
 import org.eclipse.swt.widgets.Composite;
 
 public class GLSubGraph extends AGLElementGLView implements IMultiTablePerspectiveBasedView, IGLRemoteRenderingView,
 		IMultiFormChangeListener, IEventBasedSelectionManagerUser {
 
 	public static String VIEW_TYPE = "org.caleydo.view.subgraph";
 
 	public static String VIEW_NAME = "Entourage";
 
 	// private List<TablePerspective> tablePerspectives = new ArrayList<>();
 
 	// private Set<String> remoteRenderedPathwayMultiformViewIDs;
 
 	private String pathEventSpace = GeneralManager.get().getEventPublisher().createUniqueEventSpace();
 
 	private AnimatedGLElementContainer baseContainer = new AnimatedGLElementContainer(new GLSizeRestrictiveFlowLayout(
 			true, 10, GLPadding.ZERO));
 	private GLElementContainer root = new GLElementContainer(GLLayouts.LAYERS);
 	private AnimatedGLElementContainer pathwayRow = new PathwayRowElement(this);
 	// private AnimatedGLElementContainer pathwayRow = new AnimatedGLElementContainer();
 	private GLSubGraphAugmentation augmentation = new GLSubGraphAugmentation(this);
 	private AnimatedGLElementContainer nodeInfoContainer = new AnimatedGLElementContainer(
 			new GLSizeRestrictiveFlowLayout(true, 10, GLPadding.ZERO));
 
 	private GLWindow activeWindow = null;
 
 	private ShowPortalsAction showPortalsButton;
 	// private HighlightAllPortalsAction highlightAllPortalsButton;
 
 	protected GLWindow rankingWindow;
 
 	// private List<IPathwayRepresentation> pathwayRepresentations = new ArrayList<>();
 
 	private PathEventSpaceHandler pathEventSpaceHandler = new PathEventSpaceHandler();
 
 	/**
 	 * All segments of the currently selected path.
 	 */
 	private List<PathwayPath> pathSegments = new ArrayList<>();
 
 	/**
 	 * Info for the {@link MultiFormRenderer} of the selected path.
 	 */
 	private MultiFormInfo pathInfo;
 
 	// /**
 	// * All portals currently present.
 	// */
 	// protected Set<PathwayVertexRep> portals = new HashSet<>();
 
 	/**
 	 * List of infos for all pathways.
 	 */
 	protected List<PathwayMultiFormInfo> pathwayInfos = new ArrayList<>();
 
 	protected static int currentPathwayAge = Integer.MAX_VALUE;
 
 	protected MultiFormRenderer lastUsedRenderer;
 
 	protected MultiFormRenderer lastUsedLevel1Renderer;
 
 	protected GLPathwayGridLayout pathwayLayout = new GLPathwayGridLayout(this, GLPadding.ZERO, 10);
 
 	protected GLExperimentalDataMapping experimentalDataMappingElement = new GLExperimentalDataMapping(this);
 
 	/**
 	 * Determines whether path selection mode is currently active.
 	 */
 	protected boolean isPathSelectionMode = false;
 
 	/**
 	 * Determines whether a new pathway was recently added. This information is needed to send events when dependent
 	 * views need to be initialized in the first display cycle.
 	 */
 	protected boolean wasPathwayAdded = false;
 
 	/**
 	 * Determines whether the path window is maximized.
 	 */
 	protected boolean isPathWindowMaximized = false;
 
 	/**
 	 * Determines whether portal highlighting is currently enabled.
 	 */
 	protected boolean isShowPortals = false;
 
 	private final DragAndDropController dndController = new DragAndDropController(this);
 
 	/**
 	 * The element that shows the ranked pathway list
 	 */
 	protected RankingElement rankingElement;
 
 	/**
 	 * The portal that is currently mouse-overed
 	 */
 	protected PathwayVertexRep currentPortalVertexRep;
 
 	/**
 	 * The vertex rep that is used for context path determination.
 	 */
 	private PathwayVertexRep currentContextVertexRep;
 
 	private EventBasedSelectionManager vertexSelectionManager;
 
 	private Map<Integer, PathwayVertexRep> allVertexReps = new HashMap<>();
 
 	private List<Pair<PathwayVertexRep, PathwayVertexRep>> selectedPortalLinks = new ArrayList<>();
 
 	private GLWindow dataMappingWindow;
 
 	private List<AContextMenuItem> contextMenuItemsToShow = new ArrayList<>();
 
 	protected Set<GLPathwayWindow> pinnedWindows = new HashSet<>();
 
 	private GLWindow windowToSetActive;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param glCanvas
 	 * @param viewLabel
 	 * @param viewFrustum
 	 */
 	public GLSubGraph(IGLCanvas glCanvas, Composite parentComposite, ViewFrustum viewFrustum) {
 
 		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);
 		GLElementContainer column = new GLElementContainer(new GLSizeRestrictiveFlowLayout(false, 10, GLPadding.ZERO));
 		column.add(baseContainer);
 		nodeInfoContainer.setSize(Float.NaN, 0);
 		dataMappingWindow = new GLWindow("Data Mapping", this);
 		dataMappingWindow.setSize(Float.NaN, 80);
 		dataMappingWindow.setContent(experimentalDataMappingElement);
 		dataMappingWindow.setShowCloseButton(false);
 		SlideInElement slideInElement = new SlideInElement(dataMappingWindow, ESlideInElementPosition.TOP);
 		dataMappingWindow.addSlideInElement(slideInElement);
 
 		vertexSelectionManager = new EventBasedSelectionManager(this, IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX_REP
 				.name()));
 		vertexSelectionManager.registerEventListeners();
 
 		column.add(dataMappingWindow);
 		// column.add(nodeInfoContainer);
 		rankingWindow = new GLWindow("Pathways", this);
 		rankingWindow.setSize(150, Float.NaN);
 		rankingElement = new RankingElement(this);
 		rankingWindow.setContent(rankingElement);
 		slideInElement = new SlideInElement(rankingWindow, ESlideInElementPosition.RIGHT);
 		rankingWindow.addSlideInElement(slideInElement);
 		rankingWindow.setShowCloseButton(false);
 		rankingElement.setWindow(rankingWindow);
 		baseContainer.add(rankingWindow);
 		// pathwayRow.setLayout(new GLMultiFormPathwayLayout(10, GLPadding.ZERO, this, pathwayRow));
 		pathwayRow.setLayout(pathwayLayout);
 
 		// pathwayRow.setDefaultDuration(Durations.fix(600));
 		// pathwayRow
 		// .setDefaultInTransition(new InOutTransitionBase(InOutInitializers.RIGHT, MoveTransitions.MOVE_LINEAR));
 		//
 		baseContainer.add(pathwayRow);
 
 		root.add(column);
 		root.add(augmentation);
 		registerListeners();
 
 	}
 
 	@Override
 	public void init(GL2 gl) {
 		super.init(gl);
 		pathInfo = new MultiFormInfo();
 		createMultiformRenderer(new ArrayList<>(experimentalDataMappingElement.getDmState().getTablePerspectives()),
 				EnumSet.of(EEmbeddingID.PATH_LEVEL1, EEmbeddingID.PATH_LEVEL2), baseContainer, 0.3f, pathInfo);
 		MultiLevelSlideInElement slideInElement = new MultiLevelSlideInElement(pathInfo.window,
 				ESlideInElementPosition.LEFT);
 		slideInElement.addWindowState(new IWindowState() {
 
 			@Override
 			public void apply() {
 				rankingWindow.setVisibility(EVisibility.VISIBLE);
 				pathwayRow.setVisibility(EVisibility.VISIBLE);
 				rankingWindow.setVisibility(EVisibility.VISIBLE);
 				pathInfo.window.setLayoutData(Float.NaN);
 				pathInfo.window.setSize(1, Float.NaN);
 				pathInfo.window.background.setVisibility(EVisibility.NONE);
 				pathInfo.window.baseContainer.setVisibility(EVisibility.NONE);
 				isPathWindowMaximized = false;
 			}
 		});
 		IWindowState currentWindowState = new IWindowState() {
 
 			@Override
 			public void apply() {
 				if (isPathWindowMaximized) {
 					baseContainer.remove(0);
 				}
 				rankingWindow.setVisibility(EVisibility.VISIBLE);
 				pathwayRow.setVisibility(EVisibility.VISIBLE);
 				rankingWindow.setVisibility(EVisibility.VISIBLE);
 				pathInfo.window.background.setVisibility(EVisibility.PICKABLE);
 				pathInfo.window.baseContainer.setVisibility(EVisibility.VISIBLE);
 				isPathWindowMaximized = false;
 				setPathLevel(pathInfo.getEmbeddingIDFromRendererID(pathInfo.multiFormRenderer.getActiveRendererID()));
 				augmentation.enable();
 				isLayoutDirty = true;
 			}
 		};
 		slideInElement.addWindowState(currentWindowState);
 		slideInElement.addWindowState(new IWindowState() {
 
 			@Override
 			public void apply() {
 				rankingWindow.setVisibility(EVisibility.NONE);
 				// Adding an element to get the gap is not so nice...
 				GLElement element = new GLElement();
 				element.setSize(0, Float.NaN);
 				baseContainer.add(0, element);
 				pathwayRow.setVisibility(EVisibility.NONE);
 				pathInfo.window.setLayoutData(Float.NaN);
 				pathInfo.window.setSize(Float.NaN, Float.NaN);
 				pathInfo.window.background.setVisibility(EVisibility.PICKABLE);
 				pathInfo.window.baseContainer.setVisibility(EVisibility.VISIBLE);
 				isPathWindowMaximized = true;
 				augmentation.disable();
 				updateAugmentation();
 			}
 		});
 		slideInElement.setCurrentWindowState(currentWindowState);
 
 		pathInfo.window.addSlideInElement(slideInElement);
 		pathInfo.window.setShowCloseButton(false);
 		// This assumes that a path level 2 view exists.
 		int rendererID = pathInfo.embeddingIDToRendererIDs.get(EEmbeddingID.PATH_LEVEL2).get(0);
 		if (pathInfo.multiFormRenderer.getActiveRendererID() != rendererID) {
 			pathInfo.multiFormRenderer.setActive(rendererID);
 		} else {
 			setPathLevel(EEmbeddingID.PATH_LEVEL2);
 		}
 		augmentation.init(gl);
 	}
 
 	/**
 	 * @return the dndController, see {@link #dndController}
 	 */
 	public DragAndDropController getDndController() {
 		return dndController;
 	}
 
 	@Override
 	public ASerializedView getSerializableRepresentation() {
 		SerializedSubGraphView serializedForm = new SerializedSubGraphView();
 		serializedForm.setViewID(this.getID());
 		return serializedForm;
 	}
 
 	protected void registerListeners() {
 
 		parentGLCanvas.addMouseListener(new IGLMouseListener() {
 
 			@Override
 			public void mouseWheelMoved(IMouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseReleased(IMouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mousePressed(IMouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseMoved(IMouseEvent mouseEvent) {
 				Point mousePosition = mouseEvent.getPoint();
				if (pathwayRow.getVisibility() != EVisibility.NONE) {
					for (PathwayMultiFormInfo info : pathwayInfos) {
						if (setWindowActive(mousePosition, info.window))
							return;
					}
 				}
 				if (setWindowActive(mousePosition, pathInfo.window))
 					return;
 				if (setWindowActive(mousePosition, rankingWindow))
 					return;
 				if (setWindowActive(mousePosition, dataMappingWindow))
 					return;
 			}
 
 			private boolean setWindowActive(Point mousePosition, GLWindow window) {
 				Vec2f location = window.getAbsoluteLocation();
 				Vec2f size = window.getSize();
 				if ((mousePosition.x >= location.x() && mousePosition.x <= location.x() + size.x())
 						&& (mousePosition.y >= location.y() && mousePosition.y <= location.y() + size.y())) {
 					windowToSetActive = window;
 					return true;
 				}
 				return false;
 			}
 
 			@Override
 			public void mouseExited(IMouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseEntered(IMouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseDragged(IMouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseClicked(IMouseEvent mouseEvent) {
 
 			}
 		});
 		parentGLCanvas.addKeyListener(new IGLKeyListener() {
 			@Override
 			public void keyPressed(IKeyEvent e) {
 				update(e);
 			}
 
 			@Override
 			public void keyReleased(IKeyEvent e) {
 				// update(e);
 			}
 
 			private void update(IKeyEvent e) {
 				boolean isPPressed = e.isKeyDown('p');
 				// augmentation.showPortals(isPPressed);
 				if (isPPressed) {
 					// boolean isOPressed = e.isKeyDown('o');
 					ShowPortalsEvent event = new ShowPortalsEvent(!showPortalsButton.isChecked());
 					showPortalsButton.setChecked(!showPortalsButton.isChecked());
 					event.setEventSpace(pathEventSpace);
 					EventPublisher.INSTANCE.triggerEvent(event);
 				}
 				// highlightAllPortalsButton.setChecked(isOPressed);
 			}
 
 		});
 	}
 
 	@Override
 	public String toString() {
 		return "Subgraph view";
 	}
 
 	@Override
 	public void registerEventListeners() {
 		super.registerEventListeners();
 		eventListeners.register(pathEventSpaceHandler, pathEventSpace);
 	}
 
 	@Override
 	public void unregisterEventListeners() {
 		super.unregisterEventListeners();
 		vertexSelectionManager.unregisterEventListeners();
 	}
 
 	@Override
 	protected void destroyViewSpecificContent(GL2 gl) {
 		gl.glDeleteLists(displayListIndex, 1);
 	}
 
 	public void addPathway(PathwayGraph pathway) {
 
 		PathwayDataDomain pathwayDataDomain = (PathwayDataDomain) DataDomainManager.get().getDataDomainByType(
 				PathwayDataDomain.DATA_DOMAIN_TYPE);
 		List<TablePerspective> tablePerspectives = new ArrayList<>(experimentalDataMappingElement.getDmState()
 				.getTablePerspectives());
 		TablePerspective tablePerspective = tablePerspectives.get(0);
 		Perspective recordPerspective = tablePerspective.getRecordPerspective();
 		Perspective dimensionPerspective = tablePerspective.getDimensionPerspective();
 
 		PathwayTablePerspective pathwayTablePerspective = new PathwayTablePerspective(tablePerspective.getDataDomain(),
 				pathwayDataDomain, recordPerspective, dimensionPerspective, pathway);
 		pathwayDataDomain.addTablePerspective(pathwayTablePerspective);
 
 		List<TablePerspective> pathwayTablePerspectives = new ArrayList<>(1);
 		pathwayTablePerspectives.add(pathwayTablePerspective);
 		for (int i = 1; i < tablePerspectives.size(); i++) {
 			pathwayTablePerspectives.add(tablePerspectives.get(i));
 		}
 
 		PathwayMultiFormInfo info = new PathwayMultiFormInfo();
 		info.pathway = pathway;
 
 		info.age = currentPathwayAge--;
 		// pathwayColumn.setLayout(new GLSizeRestrictiveFlowLayout(false, 10, GLPadding.ZERO));
 		createMultiformRenderer(pathwayTablePerspectives, EnumSet.of(EEmbeddingID.PATHWAY_LEVEL1,
 				EEmbeddingID.PATHWAY_LEVEL2, EEmbeddingID.PATHWAY_LEVEL3, EEmbeddingID.PATHWAY_LEVEL4), pathwayRow,
 				Float.NaN, info);
 		pathwayLayout.addColumn((GLPathwayWindow) info.window);
 		for (PathwayVertexRep vertexRep : pathway.vertexSet()) {
 			allVertexReps.put(vertexRep.getID(), vertexRep);
 		}
 		int rendererID = info.embeddingIDToRendererIDs.get(EEmbeddingID.PATHWAY_LEVEL1).get(0);
 		if (info.multiFormRenderer.getActiveRendererID() != rendererID) {
 			info.multiFormRenderer.setActive(rendererID);
 		}
 		lastUsedLevel1Renderer = info.multiFormRenderer;
 		lastUsedRenderer = info.multiFormRenderer;
 
 		PathwayMappingEvent event = new PathwayMappingEvent(experimentalDataMappingElement.getDmState()
 				.getPathwayMappedTablePerspective());
 		EventPublisher.trigger(event);
 
 		pathwayInfos.add(info);
 
 		wasPathwayAdded = true;
 	}
 
 	private void createMultiformRenderer(List<TablePerspective> tablePerspectives, EnumSet<EEmbeddingID> embeddingIDs,
 			final AnimatedGLElementContainer parent, Object layoutData, MultiFormInfo info) {
 
 		// GLElementContainer backgroundContainer = new GLElementContainer(GLLayouts.LAYERS);
 		// backgroundContainer.setLayoutData(layoutData);
 
 		// Different renderers should receive path updates from the beginning on, therefore no lazy creation.
 		MultiFormRenderer renderer = new MultiFormRenderer(this, false);
 		renderer.addChangeListener(this);
 		info.multiFormRenderer = renderer;
 		PathwayGraph pathway = null;
 
 		for (EEmbeddingID embedding : embeddingIDs) {
 			String embeddingID = embedding.id();
 			Set<String> ids = ViewManager.get().getRemotePlugInViewIDs(VIEW_TYPE, embeddingID);
 
 			for (String viewID : ids) {
 				List<Integer> rendererIDList = info.embeddingIDToRendererIDs.get(embedding);
 				if (rendererIDList == null) {
 					rendererIDList = new ArrayList<>(ids.size());
 					info.embeddingIDToRendererIDs.put(embedding, rendererIDList);
 				}
 
 				int rendererID = renderer.addPluginVisualization(viewID, getViewType(), embeddingID, tablePerspectives,
 						pathEventSpace);
 				rendererIDList.add(rendererID);
 
 				IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(renderer, rendererID);
 				if (pathwayRepresentation != null) {
 					pathway = pathwayRepresentation.getPathway();
 					// pathwayRepresentation.addVertexRepBasedContextMenuItem(new VertexRepBasedContextMenuItem(
 					// "Show Node Info", ShowNodeInfoEvent.class, pathEventSpace));
 					pathwayRepresentation.addVertexRepBasedContextMenuItem(new ShowCommonNodeItem(
 							ShowCommonNodePathwaysEvent.class, pathEventSpace));
 					pathwayRepresentation.addVertexRepBasedContextMenuItem(new VertexRepBasedContextMenuItem(
 							"Show Context", ShowNodeContextEvent.class, pathEventSpace));
 					pathwayRepresentation.addVertexRepBasedSelectionEvent(new VertexRepBasedEventFactory(
 							ShowNodeContextEvent.class, pathEventSpace), PickingMode.CLICKED);
 				}
 			}
 		}
 		GLMultiFormWindow window = null;
 		if (pathway == null) {
 			window = new GLMultiFormWindow("Selected Path", this, info, true);
 		} else {
 			final GLPathwayWindow pathwayWindow = new GLPathwayWindow(pathway.getTitle(), this,
 					(PathwayMultiFormInfo) info, false);
 			window = pathwayWindow;
 			pathwayWindow.onClose(new ICloseWindowListener() {
 				@Override
 				public void onWindowClosed(GLWindow w) {
 					pathwayLayout.removeWindow(pathwayWindow);
 					parent.remove(pathwayWindow);
 					pathwayInfos.remove(pathwayWindow.info);
 					for (PathwayVertexRep vertexRep : ((PathwayMultiFormInfo) (pathwayWindow.info)).pathway.vertexSet()) {
 						allVertexReps.remove(vertexRep.getID());
 					}
 				}
 			});
 		}
 		info.window = window;
 		parent.add(window);
 	}
 
 	@Override
 	public List<AGLView> getRemoteRenderedViews() {
 		// TODO: implement
 		return null;
 	}
 
 	@Override
 	public boolean isDataView() {
 		return true;
 	}
 
 	@Override
 	protected GLElement createRoot() {
 
 		return root;
 	}
 
 	/**
 	 * Gets the renderer of the specified {@link MultiFormRenderer} as {@link IPathwayRepresentation}.
 	 *
 	 * @param renderer
 	 * @param id
 	 *            id of the visualization in the renderer.
 	 * @return The pathway representation or null, if the active renderer is no pathway representation.
 	 */
 	protected IPathwayRepresentation getPathwayRepresentation(MultiFormRenderer renderer, int id) {
 		// int id = renderer.getActiveRendererID();
 		IPathwayRepresentation pathwayRepresentation = null;
 
 		if (renderer.isView(id)) {
 			AGLView view = renderer.getView(id);
 			if (view instanceof IPathwayRepresentation) {
 				pathwayRepresentation = (IPathwayRepresentation) view;
 
 			}
 		} else {
 			ALayoutRenderer layoutRenderer = renderer.getLayoutRenderer(id);
 			if (layoutRenderer instanceof IPathwayRepresentation) {
 				pathwayRepresentation = (IPathwayRepresentation) layoutRenderer;
 			}
 		}
 
 		return pathwayRepresentation;
 	}
 
 	protected Rectangle2D getAbsoluteVertexLocation(IPathwayRepresentation pathwayRepresentation,
 			PathwayVertexRep vertexRep, GLElement element) {
 
 		Vec2f elementPosition = element.getAbsoluteLocation();
 		Rectangle2D location = pathwayRepresentation.getVertexRepBounds(vertexRep);
 		if (location != null) {
 			return new Rectangle2D.Float((float) (location.getX() + elementPosition.x()),
 					(float) (location.getY() + elementPosition.y()), (float) location.getWidth(),
 					(float) location.getHeight());
 			// return new Rectangle2D.Float((elementPosition.x()), (elementPosition.y()), (float) location.getWidth(),
 			// (float) location.getHeight());
 
 		}
 		return null;
 	}
 
 	protected void updatePathLinks() {
 		augmentation.isDirty = true;
 		augmentation.setPxlSize(this.getParentGLCanvas().getWidth(), this.getParentGLCanvas().getHeight());
 
 		// augmentation.clearRenderers();
 		// PathwayVertexRep start = null;
 		// PathwayVertexRep end = null;
 		// List<Pair<MultiFormRenderer, GLElement>> rendererList = multiFormRenderers.get(EEmbeddingID.PATHWAY_MULTIFORM
 		// .id());
 		// if (rendererList == null)
 		// return;
 
 		// for (PathwayPath path : pathSegments) {
 		// if (start == null) {
 		// start = path.getPath().getEndVertex();
 		// } else {
 		// end = path.getPath().getStartVertex();
 		// // draw link
 		// //
 		// PathwayVertexRep referenceVertexRep = start;
 		// Rectangle2D referenceRectangle = null;
 		// for (PathwayMultiFormInfo info : pathwayInfos) {
 		// IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(info.multiFormRenderer,
 		// info.multiFormRenderer.getActiveRendererID());
 		// if (pathwayRepresentation != null
 		// && pathwayRepresentation.getPathway() == referenceVertexRep.getPathway()) {
 		// referenceRectangle = getAbsoluteVertexLocation(pathwayRepresentation, referenceVertexRep,
 		// info.container);
 		// break;
 		// }
 		// }
 		// if (referenceRectangle == null)
 		// return;
 		//
 		// for (PathwayMultiFormInfo info : pathwayInfos) {
 		// IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(info.multiFormRenderer,
 		// info.multiFormRenderer.getActiveRendererID());
 		//
 		// if (pathwayRepresentation != null) {
 		//
 		// // for (PathwayVertexRep vertexRep : vertexReps) {
 		// Rectangle2D rect = getAbsoluteVertexLocation(pathwayRepresentation, end, info.container);
 		// if (rect != null) {
 		// // augmentation.addConnectionRenderer(new GLSubGraphAugmentation.ConnectionRenderer(referenceRectangle,
 		// // rect));
 		// }
 		// // }
 		// }
 		// }
 		// //
 		// start = path.getPath().getEndVertex();
 		// end = null;
 		// }
 		// }
 		List<Rectangle2D> path = new ArrayList<>();
 
 		IPathwayRepresentation pathwayRepresentation = null;
 		PathwayMultiFormInfo pwInfo = null;
 		for (PathwayPath segment : pathSegments) {
 			for (PathwayMultiFormInfo info : pathwayInfos) {
 				pathwayRepresentation = getPathwayRepresentation(info.multiFormRenderer,
 						info.multiFormRenderer.getActiveRendererID());
 				if (pathwayRepresentation != null && (segment.getPathway() == pathwayRepresentation.getPathway())) {
 					pwInfo = info;
 					break;
 				}
 			}
 			if (pathwayRepresentation != null && pwInfo != null) {
 				for (PathwayVertexRep v : segment.getNodes()) {
 					Rectangle2D rect = getAbsoluteVertexLocation(pathwayRepresentation, v, pwInfo.container);
 					if (rect != null)
 						path.add(rect);
 				}
 			}
 		}
 
 		augmentation.setPath(path);
 
 	}
 
 	public void updateAugmentation() {
 		updatePathLinks();
 		// updatePortalLinks();
 		updatePathwayPortals();
 	}
 
 	public void setLayoutDirty() {
 		isLayoutDirty = true;
 	}
 
 	@Override
 	public void display(GL2 gl) {
 		if (windowToSetActive != null) {
 			windowToSetActive.setActive(true);
 			windowToSetActive = null;
 		}
 		boolean updateAugmentation = false;
 		if (isLayoutDirty)
 			updateAugmentation = true;
 
 		super.display(gl);
 		// for (AContextMenuItem item : contextMenuItemsToShow) {
 		// getContextMenuCreator().add(item);
 		// }
 		if (wasPathwayAdded) {
 			EnablePathSelectionEvent event = new EnablePathSelectionEvent(isPathSelectionMode);
 			event.setEventSpace(pathEventSpace);
 			eventPublisher.triggerEvent(event);
 			wasPathwayAdded = false;
 		}
 		// The augmentation has to be updated after the layout was updated in super; updating on relayout would be too
 		// early, as the layout is not adapted at that time.
 		if (updateAugmentation) {
 			updateAugmentation();
 		}
 		// }
 
 		// call after all other rendering because it calls the onDrag
 		// methods
 		// which need alpha blending...
 		dndController.handleDragging(gl, glMouseListener);
 		contextMenuItemsToShow.clear();
 
 	}
 
 	@Override
 	public void activeRendererChanged(MultiFormRenderer multiFormRenderer, int rendererID, int previousRendererID,
 			boolean wasTriggeredByUser) {
 
 		if (pathInfo != null && pathInfo.isInitialized()) {
 
 			if (multiFormRenderer == pathInfo.multiFormRenderer) {
 				EEmbeddingID embeddingID = pathInfo.getEmbeddingIDFromRendererID(rendererID);
 				setPathLevel(embeddingID);
 			}
 		}
 		if (wasTriggeredByUser && rendererID != previousRendererID) {
 			for (PathwayMultiFormInfo info : pathwayInfos) {
 				if (info.multiFormRenderer == multiFormRenderer) {
 					if (info.getEmbeddingIDFromRendererID(rendererID) == EEmbeddingID.PATHWAY_LEVEL1) {
 						pathwayLayout.setLevel1((GLPathwayWindow) info.window);
 						lastUsedLevel1Renderer = info.multiFormRenderer;
 					}
 					info.age = currentPathwayAge--;
 					lastUsedRenderer = info.multiFormRenderer;
 					break;
 				}
 			}
 
 			pathwayRow.relayout();
 		}
 	}
 
 	private void setPathLevel(EEmbeddingID embeddingID) {
 		if (embeddingID == null)
 			return;
 		if (embeddingID == EEmbeddingID.PATH_LEVEL1) {
 			if (!isPathWindowMaximized) {
 				pathInfo.window.setSize(Float.NaN, Float.NaN);
 				pathInfo.window.setLayoutData(0.5f);
 			}
 		} else if (embeddingID == EEmbeddingID.PATH_LEVEL2) {
 			if (!isPathWindowMaximized) {
 				pathInfo.window.setLayoutData(Float.NaN);
 				pathInfo.window.setSize(150, Float.NaN);
 			}
 		}
 		isLayoutDirty = true;
 	}
 
 	@Override
 	public void rendererAdded(MultiFormRenderer multiFormRenderer, int rendererID) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void rendererRemoved(MultiFormRenderer multiFormRenderer, int rendererID) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void destroyed(MultiFormRenderer multiFormRenderer) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @return the currentActiveBackground, see {@link #activeWindow}
 	 */
 	public GLWindow getActiveWindow() {
 		return activeWindow;
 	}
 
 	/**
 	 * @param currentActiveBackground
 	 *            setter, see {@link currentActiveBackground}
 	 */
 	public void setActiveWindow(GLWindow activeWindow) {
 		if (activeWindow != null && this.activeWindow != null && activeWindow != this.activeWindow) {
 			this.activeWindow.setActive(false);
 		}
 		this.activeWindow = activeWindow;
 		isLayoutDirty = true;
 		// clearSelectedPortalLinks();
 		// updatePathwayPortals();
 
 	}
 
 	/**
 	 * @return the pathEventSpace, see {@link #pathEventSpace}
 	 */
 	public String getPathEventSpace() {
 		return pathEventSpace;
 	}
 
 	/**
 	 * Info about container, embeddings, etc. for each {@link MultiFormRenderer} in this view.
 	 *
 	 * @author Christian
 	 *
 	 */
 	protected class MultiFormInfo {
 		/**
 		 * The multiform renderer.
 		 */
 		protected MultiFormRenderer multiFormRenderer;
 		/**
 		 * The element that represents the "window" of the multiform renderer, which includes a title bar. This element
 		 * should be used for resizing.
 		 */
 		protected GLMultiFormWindow window;
 		/**
 		 * The parent {@link GLElementAdapter} of this container.
 		 */
 		protected GLElementAdapter container;
 		protected Map<EEmbeddingID, List<Integer>> embeddingIDToRendererIDs = new HashMap<>();
 
 		protected boolean isInitialized() {
 			return multiFormRenderer != null && window != null && container != null && embeddingIDToRendererIDs != null;
 		}
 
 		protected EEmbeddingID getEmbeddingIDFromRendererID(int rendererID) {
 			for (Entry<EEmbeddingID, List<Integer>> entry : embeddingIDToRendererIDs.entrySet()) {
 				List<Integer> rendererIDs = entry.getValue();
 				if (rendererIDs.contains(rendererID)) {
 					return entry.getKey();
 				}
 			}
 			return null;
 		}
 
 		protected EEmbeddingID getCurrentEmbeddingID() {
 			return getEmbeddingIDFromRendererID(multiFormRenderer.getActiveRendererID());
 		}
 	}
 
 	/**
 	 * Same as {@link MultiFormInfo}, but especially for MultiFormRenderers of pathways.
 	 *
 	 * @author Christian
 	 *
 	 */
 	protected class PathwayMultiFormInfo extends MultiFormInfo {
 		protected PathwayGraph pathway;
 		protected int age;
 
 		@Override
 		protected boolean isInitialized() {
 			return super.isInitialized() && pathway != null;
 		}
 	}
 
 	protected ArrayList<Rectangle2D> portalRects = new ArrayList<>();
 
 	private class PathEventSpaceHandler {
 
 		// @ListenTo(restrictExclusiveToEventSpace = true)
 		// public void onShowPortalNodes(ShowPortalNodesEvent event) {
 		// currentPortalVertexRep = event.getVertexRep();
 		//
 		// }
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onPathSelection(PathwayPathSelectionEvent event) {
 			pathSegments = event.getPathSegments();
 			updatePathLinks();
 		}
 
 		// @ListenTo(restrictExclusiveToEventSpace = true)
 		// public void onShowNodeInfo(ShowNodeInfoEvent event) {
 		// GLNodeInfo nodeInfo = new GLNodeInfo(event.getVertexRep());
 		// nodeInfo.setSize(80, 80);
 		// nodeInfoContainer.add(nodeInfo, 200, new InOutTransitionBase(InOutInitializers.BOTTOM,
 		// MoveTransitions.MOVE_LINEAR));
 		// nodeInfoContainer.setSize(Float.NaN, 80);
 		// nodeInfoContainer.relayout();
 		// }
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onShowPathwaysWithVertex(ShowCommonNodePathwaysEvent event) {
 			rankingElement.setFilter(new PathwayFilters.CommonVertexFilter(event.getVertexRep(), false));
 			rankingElement.setRanking(new PathwayRankings.CommonVerticesRanking(event.getVertexRep().getPathway()));
 			isLayoutDirty = true;
 		}
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onMinSizeUpdate(MinSizeUpdateEvent event) {
 			pathwayRow.relayout();
 			isLayoutDirty = true;
 		}
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onShowPathwaysWithVertex(ShowCommonNodesPathwaysEvent event) {
 			rankingElement.setFilter(new PathwayFilters.CommonVerticesFilter(event.getPathway(), false));
 			rankingElement.setRanking(new PathwayRankings.CommonVerticesRanking(event.getPathway()));
 			isLayoutDirty = true;
 		}
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onEnablePathSelection(EnablePathSelectionEvent event) {
 			isPathSelectionMode = event.isPathSelectionMode();
 		}
 
 		// @ListenTo(restrictExclusiveToEventSpace = true)
 		// public void onHighlightAllPortals(HighlightAllPortalsEvent event) {
 		//
 		//
 		// }
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onShowPortalLinks(ShowPortalsEvent event) {
 			// augmentation.showPortals(event.isShowPortals());
 			isShowPortals = event.isShowPortals();
 			updatePathwayPortals();
 		}
 
 		@ListenTo(restrictExclusiveToEventSpace = true)
 		public void onShowNodeContext(ShowNodeContextEvent event) {
 			currentContextVertexRep = event.getVertexRep();
 			updatePathwayPortals();
 		}
 	}
 
 	// protected void updateAllPortals() {
 	// portals.clear();
 	// for (PathwayMultiFormInfo info : pathwayInfos) {
 	// addPortalsOfPathway(info);
 	// }
 	// updatePortalHighlights();
 	// }
 
 	// protected void addPortalsOfPathway(PathwayMultiFormInfo info) {
 	// for (PathwayVertexRep vertexRep : info.pathway.vertexSet()) {
 	// for (PathwayMultiFormInfo i : pathwayInfos) {
 	// if (info != i) {
 	// if (!portals.contains(vertexRep)) {
 	// Set<PathwayVertexRep> vertexReps = PathwayManager.get().getEquivalentVertexRepsInPathway(
 	// vertexRep, i.pathway);
 	// if (vertexReps.size() > 0) {
 	// portals.addAll(vertexReps);
 	// portals.add(vertexRep);
 	// }
 	// }
 	// } else {
 	// for (PathwayVertexRep v : i.pathway.vertexSet()) {
 	// if (v != vertexRep && PathwayManager.get().areVerticesEquivalent(vertexRep, v)) {
 	// portals.add(vertexRep);
 	// portals.add(v);
 	// }
 	// }
 	// }
 	// }
 	// }
 	// updatePortalHighlights();
 	// }
 
 	private PathwayMultiFormInfo getInfo(PathwayVertexRep vertexRep) {
 		for (PathwayMultiFormInfo info : pathwayInfos) {
 			if (info.pathway == vertexRep.getPathway())
 				return info;
 		}
 		return null;
 	}
 
 	protected void updatePathwayPortals() {
 		PathwayMultiFormInfo info = null;
 		if (activeWindow instanceof GLPathwayWindow) {
 			GLPathwayWindow w = (GLPathwayWindow) activeWindow;
 			if (w.info instanceof PathwayMultiFormInfo) {
 				info = (PathwayMultiFormInfo) w.info;
 
 			}
 		}
 		if (info == null)
 			return;
 		augmentation.clear();
 		PathwayVertexRep lastNodeOfPrevSegment = null;
 
 		for (PathwayPath segment : pathSegments) {
 			List<PathwayVertexRep> nodes = segment.getNodes();
 			if (!nodes.isEmpty()) {
 				if (lastNodeOfPrevSegment != null) {
 					PathwayMultiFormInfo info1 = getInfo(lastNodeOfPrevSegment);
 					PathwayMultiFormInfo info2 = getInfo(nodes.get(0));
					if (pathwayRow.getVisibility() == EVisibility.NONE)
						continue;
 					Rectangle2D loc1 = getAbsoluteVertexLocation(
 							getPathwayRepresentation(info1.multiFormRenderer,
 									info1.multiFormRenderer.getActiveRendererID()), lastNodeOfPrevSegment,
 							info1.container);
 					Rectangle2D loc2 = getAbsoluteVertexLocation(
 							getPathwayRepresentation(info2.multiFormRenderer,
 									info2.multiFormRenderer.getActiveRendererID()), nodes.get(0), info2.container);
 					augmentation.add(new LinkRenderer(this, true, loc1, loc2, info1, info2, 1, false, false, false,
 							true, lastNodeOfPrevSegment, nodes.get(0)));
 				}
 
 				lastNodeOfPrevSegment = nodes.get(nodes.size() - 1);
 			}
 		}
 
 		if (isShowPortals) {
 			for (PathwayVertexRep vertexRep : info.pathway.vertexSet()) {
 				Pair<Rectangle2D, Boolean> sourcePair = getPortalLocation(vertexRep, info);
 				for (PathwayMultiFormInfo i : pathwayInfos) {
 					if (info != i) {
 						if (info.getCurrentEmbeddingID() != EEmbeddingID.PATHWAY_LEVEL1) {
 							// Only connect lv2 or higher with current lv1
 							if (i.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL1) {
 								addLinkRenderers(vertexRep, info, i, sourcePair);
 							}
 						} else {
 							// Connect lv1 with all
 							addLinkRenderers(vertexRep, info, i, sourcePair);
 						}
 					} else {
 						// TODO: implement
 					}
 				}
 			}
 		}
 		// clearSelectedPortalLinks();
 		// System.out.println("update");
 	}
 
 	private void addLinkRenderers(PathwayVertexRep vertexRep, PathwayMultiFormInfo sourceInfo,
 			PathwayMultiFormInfo targetInfo, Pair<Rectangle2D, Boolean> sourcePair) {
 		Set<PathwayVertexRep> equivalentVertexReps = PathwayManager.get().getEquivalentVertexRepsInPathway(vertexRep,
 				targetInfo.pathway);
 		for (PathwayVertexRep v : equivalentVertexReps) {

			if (isPathLink(vertexRep, v) || pathwayRow.getVisibility() == EVisibility.NONE)
 				continue;
 
 			Pair<Rectangle2D, Boolean> targetPair = getPortalLocation(v, targetInfo);
 
 			// System.out.println("Add link from: " + vertexRep.getShortName() + " to " + v.getShortName() + "("
 			// + sourceLocation + " to " + targetLocation + ")");
 			float stubSize = Math.max(
 					1,
 					Math.abs(pathwayLayout.getColumnIndex((GLPathwayWindow) sourceInfo.window)
 							- pathwayLayout.getColumnIndex((GLPathwayWindow) targetInfo.window)));
 			LinkRenderer renderer = new LinkRenderer(this, vertexRep == currentPortalVertexRep
 					|| v == currentPortalVertexRep || isSelectedPortalLink(vertexRep, v), sourcePair.getFirst(),
 					targetPair.getFirst(), sourceInfo, targetInfo, stubSize, sourcePair.getSecond(),
 					targetPair.getSecond(), PathwayManager.get().areVerticesEquivalent(vertexRep,
 							currentContextVertexRep), false, vertexRep, v);
 			augmentation.add(renderer);
 		}
 	}
 
 	protected boolean isSelectedPortalLink(PathwayVertexRep v1, PathwayVertexRep v2) {
 		for (Pair<PathwayVertexRep, PathwayVertexRep> pair : selectedPortalLinks) {
 			if ((pair.getFirst() == v1 && pair.getSecond() == v2) || (pair.getFirst() == v2 && pair.getSecond() == v1))
 				return true;
 		}
 		return false;
 	}
 
 	protected boolean isPathLink(PathwayVertexRep v1, PathwayVertexRep v2) {
 		PathwayVertexRep lastNodeOfPrevSegment = null;
 		for (PathwayPath segment : pathSegments) {
 			List<PathwayVertexRep> nodes = segment.getNodes();
 			if (!nodes.isEmpty()) {
 				if (lastNodeOfPrevSegment != null) {
 					if ((v1 == lastNodeOfPrevSegment && v2 == nodes.get(0))
 							|| (v2 == lastNodeOfPrevSegment && v1 == nodes.get(0))) {
 						return true;
 					}
 				}
 
 				lastNodeOfPrevSegment = nodes.get(nodes.size() - 1);
 			}
 		}
 		return false;
 	}
 
 	protected Pair<Rectangle2D, Boolean> getPortalLocation(PathwayVertexRep vertexRep, PathwayMultiFormInfo info) {
 		Rectangle2D rect = null;
 		boolean isLocationWindow = false;
 		IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(info.multiFormRenderer,
 				info.multiFormRenderer.getActiveRendererID());
 		if (pathwayRepresentation != null)
 			rect = getAbsoluteVertexLocation(pathwayRepresentation, vertexRep, info.container);
 
 		if (rect == null || info.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL3
 				|| info.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL4) {
 			rect = new Rectangle2D.Float(info.window.getAbsoluteLocation().x(), info.window.getAbsoluteLocation().y(),
 					info.window.getSize().x(), 20);
 			isLocationWindow = true;
 		}
 		return new Pair<Rectangle2D, Boolean>(rect, isLocationWindow);
 	}
 
 	// protected void updatePortalLinks() {
 	// // augmentation.clearPortalConnectionRenderers();
 	// // if (isHighlightPortals) {
 	// // for (PathwayVertexRep vertexRep : portals) {
 	// // for (PathwayMultiFormInfo info : pathwayInfos) {
 	// // IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(info.multiFormRenderer,
 	// // info.multiFormRenderer.getActiveRendererID());
 	// // if (pathwayRepresentation != null) {
 	// // Rectangle2D rect = getAbsoluteVertexLocation(pathwayRepresentation, vertexRep, info.container);
 	// // if (rect != null)
 	// // augmentation.addPortalConnectionRenderer(new PortalHighlightRenderer(rect));
 	// // }
 	// // }
 	// // }
 	// // }
 	//
 	// if (currentPortalVertexRep != null) {
 	// portalRects.clear();
 	// Rectangle2D nodeRect = null;
 	// // find in all open pathways
 	// for (PathwayMultiFormInfo info : pathwayInfos) {
 	// IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(info.multiFormRenderer,
 	// info.multiFormRenderer.getActiveRendererID());
 	// if (pathwayRepresentation != null) {
 	// Set<PathwayVertexRep> portalVertexRepsInPathway = PathwayManager.get()
 	// .getEquivalentVertexRepsInPathway(currentPortalVertexRep,
 	// pathwayRepresentation.getPathway());
 	//
 	// for (PathwayVertexRep portalVertexRep : portalVertexRepsInPathway) {
 	// Rectangle2D rect = getAbsoluteVertexLocation(pathwayRepresentation, portalVertexRep,
 	// info.container);
 	// if (rect != null)
 	// portalRects.add(rect);
 	// }
 	// if (nodeRect == null && pathwayRepresentation.getPathway().containsVertex(currentPortalVertexRep))
 	// nodeRect = getAbsoluteVertexLocation(pathwayRepresentation, currentPortalVertexRep,
 	// info.container);
 	// }
 	// }
 	//
 	// augmentation.updatePortalRects(nodeRect, portalRects);
 	// }
 	// augmentation.isDirty = true;
 	// }
 
 	@Override
 	public IDataSupportDefinition getDataSupportDefinition() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void addTablePerspective(TablePerspective newTablePerspective) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<TablePerspective> getTablePerspectives() {
 		// TODO Auto-generated method stub
 		return experimentalDataMappingElement.getDmState().getTablePerspectives();
 	}
 
 	@Override
 	public void removeTablePerspective(TablePerspective tablePerspective) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// /**
 	// * @return the portals, see {@link #portals}
 	// */
 	// public Set<PathwayVertexRep> getPortals() {
 	// return portals;
 	// }
 
 	/**
 	 * @param showPortalsButton
 	 *            setter, see {@link showPortalsButton}
 	 */
 	public void setShowPortalsButton(ShowPortalsAction showPortalsButton) {
 		this.showPortalsButton = showPortalsButton;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see
 	 * org.caleydo.core.data.selection.IEventBasedSelectionManagerUser#notifyOfSelectionChange(org.caleydo.core.data
 	 * .selection.EventBasedSelectionManager)
 	 */
 	@Override
 	public void notifyOfSelectionChange(EventBasedSelectionManager selectionManager) {
 		Set<Integer> selectedVertexIDs = selectionManager.getElements(SelectionType.MOUSE_OVER);
 		// currentPortalVertexRep = null;
 		for (Integer id : selectedVertexIDs) {
 			currentPortalVertexRep = allVertexReps.get(id);
 		}
 		clearSelectedPortalLinks();
 		// updatePortalLinks();
 		isLayoutDirty = true;
 		// updatePathwayPortals();
 	}
 
 	/**
 	 * @return the selectedPortalLinks, see {@link #selectedPortalLinks}
 	 */
 	public List<Pair<PathwayVertexRep, PathwayVertexRep>> getSelectedPortalLinks() {
 		return selectedPortalLinks;
 	}
 
 	public void clearSelectedPortalLinks() {
 		selectedPortalLinks.clear();
 	}
 
 	public void addSelectedPortalLink(PathwayVertexRep vertexRep1, PathwayVertexRep vertexRep2) {
 		selectedPortalLinks.add(new Pair<PathwayVertexRep, PathwayVertexRep>(vertexRep1, vertexRep2));
 	}
 
 	public void setCurrentPortalVertexRep(PathwayVertexRep currentPortalVertexRep) {
 		this.currentPortalVertexRep = currentPortalVertexRep;
 	}
 
 	/**
 	 * @return the pinnedWindows, see {@link #pinnedWindows}
 	 */
 	public Set<GLPathwayWindow> getPinnedWindows() {
 		return pinnedWindows;
 	}
 
 	public void addPinnedWindow(GLPathwayWindow window) {
 		pinnedWindows.add(window);
 	}
 
 	public void removePinnedWindow(GLPathwayWindow window) {
 		pinnedWindows.remove(window);
 	}
 
 	// /**
 	// * @param highlightAllPortalsButton
 	// * setter, see {@link highlightAllPortalsButton}
 	// */
 	// public void setHighlightAllPortalsButton(HighlightAllPortalsAction highlightAllPortalsButton) {
 	// this.highlightAllPortalsButton = highlightAllPortalsButton;
 	// }
 }
