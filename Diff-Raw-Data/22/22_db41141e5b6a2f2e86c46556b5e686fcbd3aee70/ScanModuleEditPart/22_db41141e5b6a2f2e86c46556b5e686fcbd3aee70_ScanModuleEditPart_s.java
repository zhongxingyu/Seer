 package de.ptb.epics.eve.editor.gef.editparts;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.NodeEditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
 import org.eclipse.gef.requests.CreateConnectionRequest;
 import org.eclipse.gef.tools.DirectEditManager;
 import org.eclipse.jface.viewers.TextCellEditor;
 
 import de.ptb.epics.eve.data.scandescription.Connector;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.editor.gef.editpolicies.ScanModuleComponentEditPolicy;
 import de.ptb.epics.eve.editor.gef.editpolicies.ScanModuleDirectEditPolicy;
 import de.ptb.epics.eve.editor.gef.editpolicies.ScanModuleGraphicalNodeEditPolicy;
 import de.ptb.epics.eve.editor.gef.figures.ScanModuleFigure;
 import de.ptb.epics.eve.editor.gef.tools.ScanModuleCellEditorLocator;
 import de.ptb.epics.eve.editor.gef.tools.ScanModuleDirectEditManager;
 
 /**
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class ScanModuleEditPart extends AbstractGraphicalEditPart implements
		NodeEditPart, PropertyChangeListener {
 
 	private static Logger logger = Logger.getLogger(ScanModuleEditPart.class
 			.getName());
 
 	private DirectEditManager directEditManager;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param scanModule the model element
 	 */
 	public ScanModuleEditPart(ScanModule scanModule) {
 		this.setModel(scanModule);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void activate() {
 		this.getModel().addPropertyChangeListener("x", this);
 		this.getModel().addPropertyChangeListener("y", this);
 		this.getModel().addPropertyChangeListener("name", this);
 		
 		this.getModel().addPropertyChangeListener(
 				ScanModule.PARENT_CONNECTION_PROP, this);
 		this.getModel().addPropertyChangeListener(
 				ScanModule.APPENDED_CONNECTION_PROP, this);
 		this.getModel().addPropertyChangeListener(
 				ScanModule.NESTED_CONNECTION_PROP, this);
 		super.activate();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void deactivate() {
 		this.getModel().removePropertyChangeListener("x", this);
 		this.getModel().removePropertyChangeListener("y", this);
 		this.getModel().removePropertyChangeListener("name", this);
 		
 		this.getModel().removePropertyChangeListener(
 				ScanModule.PARENT_CONNECTION_PROP, this);
 		this.getModel().removePropertyChangeListener(
 				ScanModule.APPENDED_CONNECTION_PROP, this);
 		this.getModel().removePropertyChangeListener(
 				ScanModule.NESTED_CONNECTION_PROP, this);
 		super.deactivate();
 	}
 	
 	/**
 	 * Returns the model element.
 	 * 
 	 * @return the model elements
 	 */
 	public ScanModule getModel() {
 		return (ScanModule)super.getModel();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected IFigure createFigure() {
 		return new ScanModuleFigure(this.getModel().getName(), this.getModel()
 				.getX(), this.getModel().getY(), this.getModel().getWidth(),
 				this.getModel().getHeight());
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createEditPolicies() {
 		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
 				new ScanModuleDirectEditPolicy());
 		installEditPolicy(EditPolicy.COMPONENT_ROLE, 
 				new ScanModuleComponentEditPolicy());
 		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
 				new ScanModuleGraphicalNodeEditPolicy(this));
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void performRequest(Request request) {
 		logger.debug("perform request: " + request.getType());
 		// click when selected || double click
 		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT
 				|| request.getType() == RequestConstants.REQ_OPEN) {
 			if (directEditManager == null) {
 				directEditManager = new ScanModuleDirectEditManager(this,
 						TextCellEditor.class, new ScanModuleCellEditorLocator(
 								(ScanModuleFigure) this.getFigure()));
 			}
 			directEditManager.show();
 		} else if (request.getType() == RequestConstants.REQ_CREATE) {
 			super.performRequest(request);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void fireSelectionChanged() {
 		((ScanModuleFigure) this.getFigure())
 				.setSelected(this.getSelected() == EditPart.SELECTED_PRIMARY);
 		super.fireSelectionChanged();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void refreshVisuals() {
 		ScanModule sm = this.getModel();
 		((ScanModuleFigure)this.getFigure()).setX(sm.getX());
 		((ScanModuleFigure)this.getFigure()).setY(sm.getY());
 		Rectangle bounds = new Rectangle(sm.getX(), sm.getY(), sm.getWidth(),
 				sm.getHeight());
 		((GraphicalEditPart) this.getParent()).setLayoutConstraint(this,
 				this.getFigure(), bounds);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected List<?> getModelSourceConnections() {
 		final List<Object> sourceList = new ArrayList<Object>();
 		final ScanModule scanModule = this.getModel();
 		
 		if(scanModule.getAppended() != null) {
 			sourceList.add(scanModule.getAppended());
 		}
 		if(scanModule.getNested() != null) {
 			sourceList.add(scanModule.getNested());
 		}
 		return sourceList;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected List<?> getModelTargetConnections() {
 		final List<Object> sourceList = new ArrayList<Object>();
 		final ScanModule scanModule = this.getModel();
 		if(scanModule.getParent() != null) {
 			sourceList.add(scanModule.getParent());
 		}
 		return sourceList;
 	}
 
 	// ************************************************************************
 	// ****************** Methods inherited from NodeEditPart *****************
 	// ************************************************************************	
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ConnectionAnchor getSourceConnectionAnchor(
 										final ConnectionEditPart connection) {
 		// used when connection is created
 		final ScanModule scanModule = this.getModel();
 		if(connection.getModel() == scanModule.getAppended()) {
 			return ((ScanModuleFigure)this.figure).getAppendedAnchor();
 		}
 		return ((ScanModuleFigure)this.figure).getNestedAnchor();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ConnectionAnchor getSourceConnectionAnchor(final Request request) {
 		// used during creation, for feedback only
 		if (request instanceof CreateConnectionRequest) {
 			String type = ((ScanModuleFigure) this.getFigure())
 					.getConnectionType(((CreateConnectionRequest) request)
 							.getLocation());
 			if (type.equals(Connector.APPENDED)) {
 				return ((ScanModuleFigure)this.getFigure()).getAppendedAnchor();
 			} else if (type.equals(Connector.NESTED)) {
 				return ((ScanModuleFigure)this.getFigure()).getNestedAnchor();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ConnectionAnchor getTargetConnectionAnchor(
 										final ConnectionEditPart connection) {
 		// used when connection is created
 		return ((ScanModuleFigure)this.getFigure()).getTargetAnchor();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ConnectionAnchor getTargetConnectionAnchor(final Request request) {
 		// used during creation, for feedback only
 		if (request instanceof CreateConnectionRequest) {
 			CreateConnectionRequest connReq = (CreateConnectionRequest)request;
 			if (connReq.getSourceEditPart() == connReq.getTargetEditPart()) {
 				return null;
 			}
 			if (((ScanModuleEditPart) connReq.getTargetEditPart()).getModel()
 					.getParent() != null) {
 				return null;
 			}
 			return ((ScanModuleFigure) this.getFigure()).getTargetAnchor();
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals("x")) {
 			((ScanModuleFigure)this.getFigure()).setX((Integer)e.getNewValue());
 		} else if (e.getPropertyName().equals("y")) {
 			((ScanModuleFigure)this.getFigure()).setY((Integer)e.getNewValue());
 		} else if (e.getPropertyName().equals("name")) {
 			((ScanModuleFigure)this.getFigure()).setName((String)e.getNewValue());
 		}
 		if (e.getPropertyName().equals(ScanModule.APPENDED_CONNECTION_PROP) ||
 				e.getPropertyName().equals(ScanModule.NESTED_CONNECTION_PROP)) {
 			this.refreshSourceConnections();
 		}
 		if (e.getPropertyName().equals(ScanModule.PARENT_CONNECTION_PROP)) {
 			this.refreshTargetConnections();
 		}
 		this.refreshVisuals();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		return super.toString() + "(" + this.getModel().getName() + ")";
 	}
 }
