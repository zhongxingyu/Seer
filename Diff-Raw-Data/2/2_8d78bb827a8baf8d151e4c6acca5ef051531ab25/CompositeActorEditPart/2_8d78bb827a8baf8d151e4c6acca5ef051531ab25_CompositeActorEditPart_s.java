 package com.isencia.passerelle.workbench.model.editor.ui.editpart;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.draw2d.Clickable;
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.ImageFigure;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.AccessibleAnchorProvider;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.requests.DropRequest;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ptolemy.actor.Actor;
 import ptolemy.actor.CompositeActor;
 import ptolemy.actor.IOPort;
 import ptolemy.actor.IORelation;
 import ptolemy.actor.TypedCompositeActor;
 import ptolemy.actor.TypedIOPort;
 import ptolemy.actor.TypedIORelation;
 import ptolemy.kernel.Port;
 import ptolemy.kernel.Relation;
 import ptolemy.kernel.util.ChangeRequest;
 import ptolemy.kernel.util.NamedObj;
 import ptolemy.moml.Vertex;
 
 import com.isencia.passerelle.workbench.model.editor.ui.Activator;
 import com.isencia.passerelle.workbench.model.editor.ui.WorkbenchUtility;
 import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
 import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
 import com.isencia.passerelle.workbench.model.editor.ui.editor.WizardWorkflowEditor;
 import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ActorEditPolicy;
 import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
 import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
 import com.isencia.passerelle.workbench.model.editor.ui.figure.CompositeActorFigure;
 import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;
 import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;
 import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;
 import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
 import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
 import com.isencia.passerelle.workbench.model.utils.ModelUtils;
 import com.isencia.passerelle.workbench.model.utils.ModelUtils.ConnectionType;
 
 public class CompositeActorEditPart extends ContainerEditPart implements
 		IActorNodeEditPart {
 	private PasserelleModelMultiPageEditor multiPageEditorPart;
 
 	public PasserelleModelMultiPageEditor getMultiPageEditorPart() {
 		return multiPageEditorPart;
 	}
 
 	public void setMultiPageEditorPart(
 			PasserelleModelMultiPageEditor multiPageEditorPart) {
 		this.multiPageEditorPart = multiPageEditorPart;
 	}
 
 	private final static Logger logger = LoggerFactory
 			.getLogger(ActorEditPart.class);
 	public final static ImageDescriptor IMAGE_DESCRIPTOR_DRILLDOWN = Activator
 			.getImageDescriptor("icons/add.gif");
 
 	public Logger getLogger() {
 		return logger;
 	}
 
 	public CompositeActorEditPart(CompositeActor actor) {
 		super(actor);
 
 	}
 
 	private void openSubModelEditor(TypedCompositeActor actor) {
 		
 		PasserelleModelMultiPageEditor multiPageEditor = (PasserelleModelMultiPageEditor) searchPasserelleModelSampleEditor(getParent());
 		CompositeActor                 mainFlow        = multiPageEditor.getModel();
 		try {
 			mainFlow.initialize();
 		} catch (Exception e1) {
 			logger.error("Cannot open composite!", e1);
 		}
 		try {
 
 			TypedCompositeActor model = (TypedCompositeActor) getModel();
 
			final String name = WorkbenchUtility.getPath(model);	
 			if (name != null) {
 				final IProject pass = ModelUtils.getPasserelleProject();
 				final IResource res = pass.findMember(name+".moml");
 				if (res instanceof IFile) {
                    EclipseUtils.openEditor((IFile)res, PasserelleModelMultiPageEditor.ID);
 
 				}
 			}
 			
 		} catch (Exception e) {
 			logger.error("Cannot open composite!", e);
 		}
 
 	}
 
 	public MultiPageEditorPart searchPasserelleModelSampleEditor(EditPart child) {
 		if (child != null) {
 			if (child instanceof DiagramEditPart) {
 				return ((DiagramEditPart) child).getMultiPageEditorPart();
 			}
 			return searchPasserelleModelSampleEditor(child.getParent());
 		}
 		return null;
 	}
 
 	public CompositeActorEditPart(boolean showChildren,
 			PasserelleModelMultiPageEditor multiPageEditorPart) {
 		super(showChildren);
 		this.multiPageEditorPart = multiPageEditorPart;
 	}
 
 	public void changeExecuted(ChangeRequest changerequest) {
 		super.changeExecuted(changerequest);
 
 		Object source = changerequest.getSource();
 		if (changerequest instanceof ModelChangeRequest) {
 			Class<?> type = ((ModelChangeRequest) changerequest).getType();
 			NamedObj child = ((ModelChangeRequest) changerequest).getChild();
 			NamedObj container = ((ModelChangeRequest) changerequest)
 					.getContainer();
 			updateFigure(type, child, container);
 		} else {
 			if (source instanceof TypedIOPort
 					&& getModel().equals(((TypedIOPort) source).getContainer())) {
 				if (changerequest.getDescription().equals("undo-delete")) {
 					updateFigure(CreateComponentCommand.class,
 							(NamedObj) source, (NamedObj) getModel());
 				}
 
 			}
 		}
 
 	}
 
 	private void updateFigure(Class<?> type, NamedObj child, NamedObj container) {
 		if ((CreateComponentCommand.class.equals(type))
 				&& child instanceof IOPort
 				&& ModelUtils.isPortOfActor((IOPort) child, (Actor) getModel())) {
 			updateFigure();
 			getFigure().repaint();
 		} else if (getModel() == container
 				&& (DeleteComponentCommand.class.equals(type) && child instanceof IOPort)) {
 			deletePort((IOPort) child);
 			getFigure().repaint();
 
 		}
 	}
 
 	private void deletePort(IOPort port) {
 		if (port.isInput()) {
 			((CompositeActorFigure) getFigure()).removeInput(port.getName());
 		} else {
 			((CompositeActorFigure) getFigure()).removeOutput(port.getName());
 
 		}
 	}
 
 	protected boolean containsPort(CompositeActor actor, IOPort port) {
 		Object o = port.getContainer();
 		Object s = port.getSource();
 		for (Object p : actor.portList()) {
 			if (p.equals(port)) {
 				return true;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Installs EditPolicies specific to this.
 	 */
 	protected void createEditPolicies() {
 		installEditPolicy(
 				EditPolicy.COMPONENT_ROLE,
 				new ComponentNodeDeletePolicy(
 						(PasserelleModelMultiPageEditor) getMultiPageEditorPart()));
 		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ActorEditPolicy(
 				multiPageEditorPart, this));
 	}
 
 	/**
 	 * Returns a Figure to represent this.
 	 * 
 	 * @return Figure.
 	 */
 	protected IFigure createFigure() {
 		ImageFigure drillDownImageFigure = new ImageFigure(
 				createImage(IMAGE_DESCRIPTOR_DRILLDOWN));
 		drillDownImageFigure.setAlignment(PositionConstants.SOUTH);
 		drillDownImageFigure.setBorder(new MarginBorder(0, 0, 5, 0));
 
 		// Implement drilldown in composite
 		Clickable button = new Clickable(drillDownImageFigure);
 		button.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseDoubleClicked(MouseEvent e) {
 				openSubModelEditor((TypedCompositeActor) getModel());
 
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// Not action when mouse pressed
 
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// Not action when mouse released
 
 			}
 		});
 
 		Actor actorModel = getActorModel();
 		ImageDescriptor imageDescriptor = getIcon();
 		CompositeActorFigure actorFigure = createCompositeActorFigure(button,
 				actorModel, imageDescriptor);
 		// Add TargetConnectionAnchors
 		List<TypedIOPort> inputPortList = actorModel.inputPortList();
 		if (inputPortList != null) {
 			for (TypedIOPort inputPort : inputPortList) {
 				actorFigure.addInput(inputPort.getName(), inputPort
 						.getDisplayName());
 			}
 		}
 		// Add SourceConnectionAnchors
 		List<TypedIOPort> outputPortList = actorModel.outputPortList();
 		if (outputPortList != null) {
 			for (TypedIOPort outputPort : outputPortList) {
 				actorFigure.addOutput(outputPort.getName(), outputPort
 						.getDisplayName());
 			}
 		}
 		return actorFigure;
 	}
 
 	protected CompositeActorFigure createCompositeActorFigure(Clickable button,
 			Actor actorModel, ImageDescriptor imageDescriptor) {
 		CompositeActorFigure actorFigure = new CompositeActorFigure(actorModel
 				.getDisplayName(), getModel().getClass(),
 				createImage(imageDescriptor), new Clickable[] { button });
 		return actorFigure;
 	}
 
 	protected ImageDescriptor getIcon() {
 		ImageDescriptor imageDescriptor = PaletteItemFactory.getInstance().getIcon(
 				getModel().getClass());
 		return imageDescriptor;
 	}
 
 	protected void updateFigure() {
 		CompositeActorFigure actorFigure = (CompositeActorFigure) getFigure();
 
 		Actor actorModel = getActorModel();
 
 		List<TypedIOPort> inputPortList = actorModel.inputPortList();
 		if (inputPortList != null) {
 			for (TypedIOPort inputPort : inputPortList) {
 				if (actorFigure.getInputPort(inputPort.getName()) == null) {
 					actorFigure.addInput(inputPort.getName(), inputPort
 							.getDisplayName());
 				}
 			}
 		}
 		// Add SourceConnectionAnchors
 		List<TypedIOPort> outputPortList = actorModel.outputPortList();
 		if (outputPortList != null) {
 			for (TypedIOPort outputPort : outputPortList) {
 				if (actorFigure.getOutputPort(outputPort.getName()) == null) {
 					actorFigure.addOutput(outputPort.getName(), outputPort
 							.getDisplayName());
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Returns the Figure of this as a ActorFigure.
 	 * 
 	 * @return ActorFigure of this.
 	 */
 	public ActorFigure getComponentFigure() {
 		return (ActorFigure) getFigure();
 	}
 
 	/**
 	 * Returns the model of this as a TypedCompositeActor.
 	 * 
 	 * @return Model of this as an TypedCompositeActor.
 	 */
 	protected TypedCompositeActor getActorModel() {
 		return (TypedCompositeActor) getModel();
 	}
 
 	public void setSelected(int i) {
 		super.setSelected(i);
 		refreshVisuals();
 	}
 
 	/**
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class key) {
 		if (key == AccessibleAnchorProvider.class)
 			return new DefaultAccessibleAnchorProvider() {
 				public List getSourceAnchorLocations() {
 					List list = new ArrayList();
 					Vector sourceAnchors = getComponentFigure()
 							.getSourceConnectionAnchors();
 					for (int i = 0; i < sourceAnchors.size(); i++) {
 						ConnectionAnchor anchor = (ConnectionAnchor) sourceAnchors
 								.get(i);
 						list.add(anchor.getReferencePoint()
 								.getTranslated(0, -3));
 					}
 					return list;
 				}
 
 				public List getTargetAnchorLocations() {
 					List list = new ArrayList();
 					Vector targetAnchors = getComponentFigure()
 							.getTargetConnectionAnchors();
 					for (int i = 0; i < targetAnchors.size(); i++) {
 						ConnectionAnchor anchor = (ConnectionAnchor) targetAnchors
 								.get(i);
 						list
 								.add(anchor.getReferencePoint().getTranslated(
 										0, 3));
 					}
 					return list;
 				}
 			};
 		return super.getAdapter(key);
 	}
 
 	@Override
 	protected List getModelTargetConnections() {
 		return getModelConnections(ModelUtils.ConnectionType.TARGET);
 	}
 
 	@Override
 	protected List getModelSourceConnections() {
 		return getModelConnections(ModelUtils.ConnectionType.SOURCE);
 	}
 
 	protected List getModelConnections(ConnectionType connectionType) {
 		Set<Relation> connectedRelations = ModelUtils.getConnectedRelations(
 				getActorModel(), connectionType);
 		List modelTargetConnections = new ArrayList();
 		for (Relation rel : connectedRelations) {
 			Vertex vertex = getVertex(rel);
 			if (vertex != null) {
 				List<IOPort> ports = ModelUtils.getPorts(rel,
 						(NamedObj) getActorModel());
 				for (IOPort port : ports) {
 					Object relation = VertexEditPart.getRelation(
 							(TypedIORelation) rel, port, vertex, false);
 					modelTargetConnections.add(relation);
 				}
 			} else {
 				modelTargetConnections.add(rel);
 			}
 		}
 		return modelTargetConnections;
 	}
 
 	public Vertex getVertex(Relation model) {
 		Enumeration attributes = model.getAttributes();
 		while (attributes.hasMoreElements()) {
 			Object temp = attributes.nextElement();
 			if (temp instanceof Vertex) {
 				return (Vertex) temp;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the Output Port based on a given Anchor
 	 * 
 	 * @return Port.
 	 */
 	public Port getSourcePort(ConnectionAnchor anchor) {
 		getLogger().debug("Get Source port  based on anchor");
 
 		return getPort(anchor, getActorModel().outputPortList());
 	}
 
 	/**
 	 * Returns the Input Port based on a given Anchor
 	 * 
 	 * @return Port.
 	 */
 	public Port getTargetPort(ConnectionAnchor anchor) {
 		getLogger().debug("Get Target port  based on anchor");
 
 		return getPort(anchor, getActorModel().inputPortList());
 	}
 
 	protected Port getPort(ConnectionAnchor anchor, List outputPortList) {
 		ActorFigure anchorFigure = getComponentFigure();
 		for (Iterator iterator = outputPortList.iterator(); iterator.hasNext();) {
 			Port port = (Port) iterator.next();
 			if (port.getName() != null
 					&& port.getName().equals(
 							anchorFigure.getConnectionAnchorName(anchor)))
 				return port;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the connection anchor of a source connection which is at the
 	 * given point.
 	 * 
 	 * @return ConnectionAnchor.
 	 */
 	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
 		Point pt = new Point(((DropRequest) request).getLocation());
 		return getComponentFigure().getSourceConnectionAnchorAt(pt);
 	}
 
 	/**
 	 * Returns the connection anchor of a terget connection which is at the
 	 * given point.
 	 * 
 	 * @return ConnectionAnchor.
 	 */
 	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
 		Point pt = new Point(((DropRequest) request).getLocation());
 		return getComponentFigure().getTargetConnectionAnchorAt(pt);
 	}
 
 	/**
 	 * Returns the connection anchor for the given ConnectionEditPart's source.
 	 * 
 	 * @return ConnectionAnchor.
 	 */
 	public ConnectionAnchor getSourceConnectionAnchor(
 			ConnectionEditPart connEditPart) {
 		getLogger().debug(
 				"Get SourceConnectionAnchor based on ConnectionEditPart");
 		return getConnectionActor(connEditPart, true);
 	}
 
 	/**
 	 * Returns the connection anchor for the given ConnectionEditPart's target.
 	 * 
 	 * @return ConnectionAnchor.
 	 */
 	public ConnectionAnchor getTargetConnectionAnchor(
 			ConnectionEditPart connEditPart) {
 		getLogger().debug(
 				"Get TargetConnectionAnchor based on ConnectionEditPart");
 		return getConnectionActor(connEditPart, false);
 	}
 
 	private ConnectionAnchor getConnectionActor(
 			ConnectionEditPart connEditPart, boolean isSource) {
 		Relation relation = null;
 		Port port = null;
 		if (connEditPart instanceof VertexLinkEditPart) {
 			relation = ((VertexLinkEditPart) connEditPart).getRelation();
 			port = ((VertexLinkEditPart) connEditPart).getPort();
 
 		} else {
 			relation = (Relation) connEditPart.getModel();
 			List linkedPortList = isSource ? ((IORelation) relation)
 					.linkedSourcePortList() : ((IORelation) relation)
 					.linkedDestinationPortList();
 			if (linkedPortList == null || linkedPortList.size() == 0)
 				return null;
 			port = (Port) linkedPortList.get(0);
 
 		}
 		ConnectionAnchor connectionAnchor = getComponentFigure()
 				.getConnectionAnchor(port.getName());
 		return connectionAnchor;
 	}
 
 	/**
 	 * Returns the name of the given connection anchor.
 	 * 
 	 * @return The name of the ConnectionAnchor as a String.
 	 */
 	final protected String mapConnectionAnchorToTerminal(ConnectionAnchor c) {
 		return getComponentFigure().getConnectionAnchorName(c);
 	}
 
 	protected void specificTreatment(Object source) {
 		if (source instanceof CompositeActor) {
 			updatePageName((CompositeActor) getModel());
 			updateCompositeActors((CompositeActor) getModel());
 		}
 	}
 
 	private void updateCompositeActors(CompositeActor actor) {
 		Enumeration enumeration = actor.getEntities();
 		while (enumeration.hasMoreElements()) {
 			Object o = enumeration.nextElement();
 			if (o instanceof CompositeActor) {
 				updatePageName((CompositeActor) o);
 				updateCompositeActors((CompositeActor) o);
 			}
 		}
 	}
 
 	private void updatePageName(CompositeActor actor) {
 		PasserelleModelMultiPageEditor parent = (PasserelleModelMultiPageEditor) multiPageEditorPart;
 		int index = parent.getPageIndex(actor);
 
 		parent.setText(index, WorkbenchUtility.getPath(actor));
 	}
 
 }
