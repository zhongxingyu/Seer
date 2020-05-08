 package net.unikernel.bummel.visual_editor;
 
 import com.kitfox.svg.SVGException;
 import java.awt.Point;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.net.MalformedURLException;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import net.unikernel.bummel.project_model.api.Circuit;
 import net.unikernel.bummel.project_model.api.Toggle;
 import org.netbeans.api.visual.action.*;
 import org.netbeans.api.visual.action.WidgetAction.State;
 import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
 import org.netbeans.api.visual.anchor.AnchorFactory;
 import org.netbeans.api.visual.anchor.AnchorShape;
 import org.netbeans.api.visual.anchor.PointShape;
 import org.netbeans.api.visual.graph.GraphPinScene;
 import org.netbeans.api.visual.router.RouterFactory;
 import org.netbeans.api.visual.vmd.VMDConnectionWidget;
 import org.netbeans.api.visual.widget.ConnectionWidget;
 import org.netbeans.api.visual.widget.LayerWidget;
 import org.netbeans.api.visual.widget.Scene;
 import org.netbeans.api.visual.widget.Widget;
 import org.openide.nodes.Node;
 import org.openide.nodes.NodeTransfer;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author mcangel
  */
 public class CircuitGraphPinScene extends GraphPinScene<ElementNode, String, ElementPortNode>
 {
 	private LayerWidget mainLayer = new LayerWidget(this);
 	private LayerWidget connectionLayer = new LayerWidget(this);
 	private LayerWidget interractionLayer = new LayerWidget(this);
 	private long edgeCounter = 0;
 	private WidgetAction connectAction = ActionFactory.createConnectAction(interractionLayer, new SceneConnectProvider());
 	private WidgetAction reconnetAction = ActionFactory.createReconnectAction(new SceneReconnectProvider());
 	private WidgetAction toggleGeneratorAction = new WidgetAction.Adapter(){
 
 		@Override
 		public State mouseClicked(Widget widget, WidgetMouseEvent event)
 		{
 			((ElementNode)findObject(widget)).getLookup().lookup(BasicElement.class).setState(
 					(((ElementNode)findObject(widget)).getLookup().lookup(BasicElement.class).getState()+1)%2);
 			hardcodedCircuitRun();
 			return State.CONSUMED;
 		}
 	};
 	private Circuit circuit;
 
 	public CircuitGraphPinScene(final Circuit circuit)
 	{
 		this.circuit = circuit;
 		this.addChild(mainLayer);
 		this.addChild(connectionLayer);
 		this.addChild(interractionLayer);
 		
 		getActions().addAction(ActionFactory.createAcceptAction(new AcceptProvider()
 		{
 			@Override
 			public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
 			{
 				Node node = NodeTransfer.node(transferable,	NodeTransfer.DND_COPY);
 				if (node != null && (node.getLookup().lookup(BasicElement.class)) != null)
 					return ConnectorState.ACCEPT;
 				return ConnectorState.REJECT_AND_STOP;
 			}
 
 			@Override
 			public void accept(Widget widget, Point point, Transferable transferable)
 			{
 				Node node = NodeTransfer.node(transferable, NodeTransfer.DND_COPY);//Defines the instance from drag and drop action.
 				
 				BasicElement el = node.getLookup().lookup(BasicElement.class);  //Get BasicElement instance
 				try
 				{
 					el = el.getClass().newInstance();	//create new instance of it to avoid equality
 					circuit.addElement(el);//add element to the model
 					
 					ElementNode elNode = new ElementNode(el);  //create new element node with default constructor
 					Widget nodeWidget = addNode(elNode);//add new ElementNode to the scene
 					nodeWidget.setPreferredLocation(widget.convertLocalToScene(point));
 					for (String port : el.getPorts())
 					{//add pins to the scene
 						addPin(elNode, new ElementPortNode(port));
 					}
 					
 					validate();
 					nodeWidget.repaint();
 				} catch (InstantiationException | IllegalAccessException ex)
 				{
 					Exceptions.printStackTrace(ex);
 				}
 			}
 		}));
 	}
 	
 	@Override
 	protected Widget attachNodeWidget(ElementNode node)
 	{
 		ElementWidget widget = null;
 		try
 		{
 			widget = new ElementWidget(this, node);
 		}
 		catch (MalformedURLException | SVGException ex)
 		{
 			Exceptions.printStackTrace(ex);
 		}
 		mainLayer.addChild(widget);
 
 		if(node.getLookup().lookup(BasicElement.class) instanceof Toggle)
 		{
 			widget.getActions().addAction(toggleGeneratorAction);
 		}
 		widget.getActions().addAction(createSelectAction());
 		widget.getActions().addAction(ActionFactory.createMoveAction());
 		widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider()
 		{
 			@Override
 			public JPopupMenu getPopupMenu(Widget widget, Point localLocation)
 			{
 				final Widget w = widget;
 				JPopupMenu menu = new JPopupMenu("Menu");
 				JMenuItem mi = new JMenuItem(new AbstractAction()
 				{
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 						circuit.removeElement(((ElementNode)findObject(w))
 								.getLookup().lookup(BasicElement.class));
 						CircuitGraphPinScene.this.internalNodeRemove((ElementNode)findObject(w));
 						
 						hardcodedCircuitRun();
 					}
 				});
 				mi.setText("Remove");
 				menu.add(mi);
 				return menu;
 			}
 		}));
 
 		return widget;
 	}
 	
 	//TODO - remake this through another thread!!!
 	private void hardcodedCircuitRun()
 	{
		for(int i = 0, n = circuit.getElements().size(); i < n; i++, circuit.step()){}
 	}
 	
 	/**
 	 * Removes object and it's widget from the scene.
 	 * @param object - object to be removed.
 	 */
 	private void internalObjectRemove(final Object object)
 	{
 		final List<Widget> widgets = findWidgets(object);
 
 		//removeObject() does not remove widgets
 		for (final Widget widget : widgets)
 		{
 			widget.removeFromParent();
 		}
 	}
 	
 	private void internalNodeRemove(final ElementNode node)
 	{
 		//first remove all node pins
 		for(ElementPortNode pin : getNodePins(node))
 		{
 			//removing pin first romeve all connected edges
 			for(String edge : findPinEdges(pin, true, true))
 			{
 				internalObjectRemove(edge);
 			}
 			//remove pin itself
 			internalObjectRemove(pin);
 		}
 		//remove node itself
 		internalObjectRemove(node);
 	}
 
 	@Override
 	protected Widget attachPinWidget(ElementNode node, ElementPortNode pin)
 	{
 		ElementPortWidget widget = new ElementPortWidget(this, node, pin.getPort());
 		((ElementWidget) findWidget(node)).attachPortWidget(widget);
 
 		widget.getActions().addAction(createObjectHoverAction());
 		widget.getActions().addAction(createSelectAction());
 		widget.getActions().addAction(connectAction);
 
 		return widget;
 	}
 	
 	@Override
 	protected Widget attachEdgeWidget(String edge)
 	{
 		VMDConnectionWidget connection = new VMDConnectionWidget(this,
 				RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer));
 		connection.setTargetAnchorShape(AnchorShape.NONE);
 		connection.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
 
 		connection.getActions().addAction(createObjectHoverAction());
 		connection.getActions().addAction(createSelectAction());
 		connection.getActions().addAction(reconnetAction);
 		connection.getActions().addAction(ActionFactory.createOrthogonalMoveControlPointAction());
 
 		connectionLayer.addChild(connection);
 		return connection;
 	}
 
 	@Override
 	protected void attachEdgeSourceAnchor(String edge, ElementPortNode oldSourcePin, ElementPortNode sourcePin)
 	{
 		Widget w = sourcePin != null ? findWidget(sourcePin) : null;
 		((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(w));
 		//((ConnectionWidget) findWidget(edge)).setSourceAnchor(((ElementPortWidget)w).getAnchor());
 		//((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(((ElementPortWidget)w).getAnchorWidget()));
 	}
 
 	@Override
 	protected void attachEdgeTargetAnchor(String edge, ElementPortNode oldTargetPin, ElementPortNode targetPin)
 	{
 		Widget w = targetPin != null ? findWidget(targetPin) : null;
 		((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(w));
 		//((ConnectionWidget) findWidget(edge)).setSourceAnchor(((ElementPortWidget)w).getAnchor());
 		//((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(((ElementPortWidget)w).getAnchorWidget()));
 	}
 
 	private class SceneConnectProvider implements ConnectProvider
 	{
 		private ElementPortNode source = null;
 		private ElementPortNode target = null;
 
 		@Override
 		public boolean isSourceWidget(Widget sourceWidget)
 		{
 			Object object = findObject(sourceWidget);
 			source = isPin(object) ? (ElementPortNode) object : null;
 			return source != null;
 		}
 
 		@Override
 		public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget)
 		{
 			//TODO: add single connection check
 			Object object = findObject(targetWidget);
 			target = isPin(object) ? (ElementPortNode) object : null;
 			if (target != null)
 			{
 				return !source.equals(target) ? ConnectorState.ACCEPT : ConnectorState.REJECT_AND_STOP;
 			}
 			return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
 		}
 
 		@Override
 		public boolean hasCustomTargetWidgetResolver(Scene scene)
 		{
 			return false;
 		}
 
 		@Override
 		public Widget resolveTargetWidget(Scene scene, Point sceneLocation)
 		{
 			return null;
 		}
 
 		@Override
 		public void createConnection(Widget sourceWidget, Widget targetWidget)
 		{
 			//connect elements in the model
 			BasicElement srcElem = getPinNode(source)
 					.getLookup().lookup(BasicElement.class);
 			BasicElement tgtElem = getPinNode(target)
 					.getLookup().lookup(BasicElement.class);
 			circuit.connectElements(srcElem, source.getPort(), 
 					tgtElem, target.getPort());
 			hardcodedCircuitRun();
 			
 			String edge = "edge" + edgeCounter++;
 			addEdge(edge);
 			setEdgeSource(edge, source);
 			setEdgeTarget(edge, target);
 		}
 	}
 
 	private class SceneReconnectProvider implements ReconnectProvider
 	{
 		String edge;
 		ElementPortNode originalNode;
 		ElementPortNode replacementNode;
 
 		@Override
 		public void reconnectingStarted(ConnectionWidget connectionWidget, boolean reconnectingSource)
 		{
 		}
 
 		@Override
 		public void reconnectingFinished(ConnectionWidget connectionWidget, boolean reconnectingSource)
 		{
 		}
 
 		@Override
 		public boolean isSourceReconnectable(ConnectionWidget connectionWidget)
 		{
 			Object object = findObject(connectionWidget);
 			edge = isEdge(object) ? (String) object : null;
 			originalNode = edge != null ? getEdgeSource(edge) : null;
 			return originalNode != null;
 		}
 
 		@Override
 		public boolean isTargetReconnectable(ConnectionWidget connectionWidget)
 		{
 			Object object = findObject(connectionWidget);
 			edge = isEdge(object) ? (String) object : null;
 			originalNode = edge != null ? getEdgeTarget(edge) : null;
 			return originalNode != null;
 		}
 
 		@Override
 		public ConnectorState isReplacementWidget(ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource)
 		{
 			Object object = findObject(replacementWidget);
 			replacementNode = isPin(object) ? (ElementPortNode) object : null;
 			if (replacementNode != null)
 			{
 				return ConnectorState.ACCEPT;
 			}
 			return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
 		}
 
 		@Override
 		public boolean hasCustomReplacementWidgetResolver(Scene scene)
 		{
 			return false;
 		}
 
 		@Override
 		public Widget resolveReplacementWidget(Scene scene, Point sceneLocation)
 		{
 			return null;
 		}
 
 		@Override
 		public void reconnect(ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource)
 		{
 			//remove old connection from the model
 			BasicElement srcElem = getPinNode(getEdgeSource(edge))
 					.getLookup().lookup(BasicElement.class);
 			BasicElement tgtElem = getPinNode(getEdgeTarget(edge))
 					.getLookup().lookup(BasicElement.class);
 			circuit.disconnectElements(srcElem, getEdgeSource(edge).getPort(),
 					tgtElem, getEdgeTarget(edge).getPort());
 			if (replacementWidget == null)
 			{
 				//disconnection already done
 				removeEdge(edge);
 			} else if (reconnectingSource)
 			{
 				//create new connection in the model
 				srcElem = getPinNode(replacementNode)
 						.getLookup().lookup(BasicElement.class);
 				circuit.connectElements(srcElem, replacementNode.getPort(),
 						tgtElem, getEdgeTarget(edge).getPort());
 				
 				setEdgeSource(edge, replacementNode);
 			} else
 			{
 				//create new connection in the model
 				tgtElem = getPinNode(replacementNode)
 						.getLookup().lookup(BasicElement.class);
 				circuit.connectElements(srcElem, getEdgeSource(edge).getPort(),
 						tgtElem, replacementNode.getPort());
 				
 				setEdgeTarget(edge, replacementNode);
 			}
 			
 			hardcodedCircuitRun();
 		}
 	}
 }
