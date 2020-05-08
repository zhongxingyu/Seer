 package org.eclipse.stem.core.graph.impl;
 
 /******************************************************************************* 
  * Copyright (c) 2006 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  *     IBM Corporation - initial API and implementation 
  *******************************************************************************/
  
 import java.util.Collection;
 import java.util.Comparator;
 
 import org.eclipse.emf.common.notify.Notification;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.ECollections;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 //import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.common.IdentifiableFilter;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.common.impl.IdentifiableFilterImpl;
 import org.eclipse.stem.core.common.impl.IdentifiableImpl;
 import org.eclipse.stem.core.graph.DynamicLabel;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.EdgeLabel;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.GraphPackage;
 import org.eclipse.stem.core.graph.Label;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.graph.UnresolvedIdentifiable;
 
 import org.eclipse.stem.core.model.Decorator;
 
 import org.eclipse.stem.core.model.ModelPackage;
 import org.eclipse.stem.core.model.STEMTime;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Graph</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getEdges <em>Edges</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNodes <em>Nodes</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getGraphLabels <em>Graph Labels</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNodeLabels <em>Node Labels</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getDynamicLabels <em>Dynamic Labels</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNumEdges <em>Num Edges</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNumNodes <em>Num Nodes</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNumGraphLabels <em>Num Graph Labels</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNumNodeLabels <em>Num Node Labels</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getNumDynamicLabels <em>Num Dynamic Labels</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getUnresolvedIdentifiables <em>Unresolved Identifiables</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getDecorators <em>Decorators</em>}</li>
  *   <li>{@link org.eclipse.stem.core.graph.impl.GraphImpl#getTime <em>Time</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GraphImpl extends IdentifiableImpl implements Graph {
 	
 	
 	/**
 	 * The cached value of the '{@link #getEdges() <em>Edges</em>}' map. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getEdges()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<URI, Edge> edges;
 
 	/**
 	 * The cached value of the '{@link #getNodes() <em>Nodes</em>}' map. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getNodes()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<URI, Node> nodes;
 
 	/**
 	 * The cached value of the '{@link #getGraphLabels() <em>Graph Labels</em>}' map.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getGraphLabels()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<URI, Label> graphLabels;
 
 	/**
 	 * The cached value of the '{@link #getNodeLabels() <em>Node Labels</em>}' map.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNodeLabels()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<URI, NodeLabel> nodeLabels;
 
 	/**
 	 * The cached value of the '{@link #getDynamicLabels() <em>Dynamic Labels</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getDynamicLabels()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<DynamicLabel> dynamicLabels;
 
 	/**
 	 * The default value of the '{@link #getNumEdges() <em>Num Edges</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNumEdges()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int NUM_EDGES_EDEFAULT = 0;
 
 	/**
 	 * The default value of the '{@link #getNumNodes() <em>Num Nodes</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNumNodes()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int NUM_NODES_EDEFAULT = 0;
 
 	/**
 	 * The default value of the '{@link #getNumGraphLabels() <em>Num Graph Labels</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNumGraphLabels()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int NUM_GRAPH_LABELS_EDEFAULT = 0;
 
 	/**
 	 * The default value of the '{@link #getNumNodeLabels() <em>Num Node Labels</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNumNodeLabels()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int NUM_NODE_LABELS_EDEFAULT = 0;
 
 	/**
 	 * The default value of the '{@link #getNumDynamicLabels() <em>Num Dynamic Labels</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNumDynamicLabels()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int NUM_DYNAMIC_LABELS_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getUnresolvedIdentifiables() <em>Unresolved Identifiables</em>}' containment reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getUnresolvedIdentifiables()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<UnresolvedIdentifiable> unresolvedIdentifiables;
 
 	/**
 	 * The cached value of the '{@link #getDecorators() <em>Decorators</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Decorator> decorators;
 
 	/**
 	 * The cached value of the '{@link #getTime() <em>Time</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTime()
 	 * @generated
 	 * @ordered
 	 */
 	protected STEMTime time;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected GraphImpl() {
 		super();
 		setURI(STEMURI.createURI(URI_TYPE_GRAPH_SEGMENT + "/"
 				+ STEMURI.generateUniquePart()));
 		setTypeURI(STEMURI.GRAPH_TYPE_URI);
 	} // GraphImpl
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GraphPackage.Literals.GRAPH;
 	}
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#setURI(org.eclipse.emf.common.util.URI)
 	 * @generated NOT
 	 */
 	@Override
 	public void setURI(URI newURI) {
 		super.setURI(newURI);
 		// Now go through all the graph labels and update then to reference the
 		// new URI
 		for (final Iterator<Label> labelIter = getGraphLabels().values().iterator(); labelIter
 				.hasNext();) {
 			final Label label = labelIter.next();
 			label.setURIOfIdentifiableToBeLabeled(newURI);
 		} // for
 	} // setURI
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EMap<URI, Edge> getEdges() {
 		if (edges == null) {
 			edges = new EcoreEMap<URI,Edge>(GraphPackage.Literals.URI_TO_EDGE_MAP_ENTRY, URIToEdgeMapEntryImpl.class, this, GraphPackage.GRAPH__EDGES);
 		}
 		return edges;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EMap<URI, Node> getNodes() {
 		if (nodes == null) {
 			nodes = new EcoreEMap<URI,Node>(GraphPackage.Literals.URI_TO_NODE_MAP_ENTRY, URIToNodeMapEntryImpl.class, this, GraphPackage.GRAPH__NODES);
 		}
 		return nodes;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<DynamicLabel> getDynamicLabels() {
 		if (dynamicLabels == null) {
 			dynamicLabels = new EObjectResolvingEList<DynamicLabel>(DynamicLabel.class, this, GraphPackage.GRAPH__DYNAMIC_LABELS);
 		}
 		return dynamicLabels;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EMap<URI, Label> getGraphLabels() {
 		if (graphLabels == null) {
 			graphLabels = new EcoreEMap<URI,Label>(GraphPackage.Literals.URI_TO_LABEL_MAP_ENTRY, URIToLabelMapEntryImpl.class, this, GraphPackage.GRAPH__GRAPH_LABELS);
 		}
 		return graphLabels;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<UnresolvedIdentifiable> getUnresolvedIdentifiables() {
 		if (unresolvedIdentifiables == null) {
 			unresolvedIdentifiables = new EObjectContainmentEList<UnresolvedIdentifiable>(UnresolvedIdentifiable.class, this, GraphPackage.GRAPH__UNRESOLVED_IDENTIFIABLES);
 		}
 		return unresolvedIdentifiables;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Decorator> getDecorators() {
 		if (decorators == null) {
 			decorators = new EObjectContainmentWithInverseEList<Decorator>(Decorator.class, this, GraphPackage.GRAPH__DECORATORS, ModelPackage.DECORATOR__GRAPH);
 		}
 		return decorators;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public STEMTime getTime() {
 		return time;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetTime(STEMTime newTime, NotificationChain msgs) {
 		STEMTime oldTime = time;
 		time = newTime;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GraphPackage.GRAPH__TIME, oldTime, newTime);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTime(STEMTime newTime) {
 		if (newTime != time) {
 			NotificationChain msgs = null;
 			if (time != null)
 				msgs = ((InternalEObject)time).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GraphPackage.GRAPH__TIME, null, msgs);
 			if (newTime != null)
 				msgs = ((InternalEObject)newTime).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GraphPackage.GRAPH__TIME, null, msgs);
 			msgs = basicSetTime(newTime, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GRAPH__TIME, newTime, newTime));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * Return all of the Node Labels in the graph that have a particular type
 	 * URI.
 	 * 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public EList<NodeLabel> getNodeLabelsByTypeURI(URI typeURI) {
 		// We could perform this function in a number of ways. We could, for
 		// instance, create a Map between the type URI's and the instances,
 		// however if we do that then we do two things: 1) make management of
 		// the collection of instances more complicated (we have to add them to
 		// two maps), and, 2) we "bloat" the serialization as we now basically
 		// double the size of the serialized collection(s).
 		// Given that this method is called infrequently it's probably better to
 		// just do a "dumb" sequential scan of the instances and match them.
 		// We can always change out minds later.
 
 		final EList<NodeLabel> retValue = new BasicEList<NodeLabel>();
 
 		for (final Iterator<NodeLabel> nodeLabelIter = getNodeLabels().values().iterator(); nodeLabelIter
 				.hasNext();) {
 			final NodeLabel nodeLabel = nodeLabelIter.next();
 			// Does this one the type we're looking for?
 			if (nodeLabel.getTypeURI().equals(typeURI)) {
 				// Yes
 				retValue.add(nodeLabel);
 			}
 		} // for each nodeLabel
 
 		// Stefan 7/23/09. If need to guarantee the order of objects this list
 		// being the same for each call since the list is used to partition
 		// the work up among multiple worker threads. Luckily this call
 		// is only made once for every decorator in the beginning of a simulation
 		// so sorting is not expensive.
 		
 		ECollections.sort(retValue, new Comparator<NodeLabel>() {
 
 			public int compare(NodeLabel arg0, NodeLabel arg1) {
				return arg0.getNode().getURI().toString().compareTo(arg1.getNode().getURI().toString());
 			}
 			
 		});
 		
 		return retValue;
 	} // getNodeLabelsByTypeURI
 
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EMap<URI, NodeLabel> getNodeLabels() {
 		if (nodeLabels == null) {
 			nodeLabels = new EcoreEMap<URI,NodeLabel>(GraphPackage.Literals.URI_TO_NODE_LABEL_MAP_ENTRY, URIToNodeLabelMapEntryImpl.class, this, GraphPackage.GRAPH__NODE_LABELS);
 		}
 		return nodeLabels;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public int getNumEdges() {
 		return edges == null ? 0 : getEdges().size();
 	} // getNumEdges
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public int getNumNodes() {
 		return nodes == null ? 0 : getNodes().size();
 	} // getNumNodes
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public int getNumGraphLabels() {
 		return graphLabels == null ? 0 : getGraphLabels().size();
 	} // getNumGraphLabels
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public int getNumNodeLabels() {
 		return nodeLabels == null ? 0 : getNodeLabels().size();
 	} // getNumNodeLabels
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public int getNumDynamicLabels() {
 		return dynamicLabels == null ? 0 : getDynamicLabels().size();
 	} // getNumDynamicLabels
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * This method adds the {@link Edge}s, {@link Node}s, {@link Label}s and {@link Decorator} 
 	 * from another graph to  the collections maintained by this graph. 
 	 * <p>
 	 * It does not attempt to resolve
 	 * connections between Edges and the Nodes they reference.
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addGraph(final Graph graph, IdentifiableFilter filter) {
 		IdentifiableFilterImpl _filter = (IdentifiableFilterImpl)filter;
 		EMap<URI, Edge>edges = graph.getEdges();
 		EMap<URI, Node>nodes = graph.getNodes();
 		EMap<URI, NodeLabel>nodeLabels = graph.getNodeLabels();
 		EMap<URI, Label>graphLabels = graph.getGraphLabels();
 		
 		if(filter != null) {
 			_filter.filterEdges(edges);
 			_filter.filterNodes(nodes);
 			_filter.filterNodeLabels(nodeLabels);
 			_filter.filterLabels(graphLabels);
 		}
 		
 		getEdges().addAll(edges);
 		getNodes().addAll(nodes);
 		getNodeLabels().addAll(nodeLabels);
 		getDecorators().addAll(graph.getDecorators());
 		
 		// We need to update the graph labels to the new graph
 		for (final Iterator<Label> graphLabelIter = graphLabels.values()
 				.iterator(); graphLabelIter.hasNext();) {
 			final Label graphLabel = graphLabelIter.next();
 			graphLabel.setURIOfIdentifiableToBeLabeled(getURI());
 			putGraphLabel(graphLabel);
 		} // for each new graph label
 
 		getDynamicLabels().addAll(graph.getDynamicLabels());
 		getUnresolvedIdentifiables().addAll(graph.getUnresolvedIdentifiables());
 	} // addGraph
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void putEdge(Edge edge) {
 		getEdges().put(edge.getURI(), edge);
 		final EdgeLabel edgeLabel = edge.getLabel();
 		// Dynamic?
 		if (edgeLabel instanceof DynamicLabel) {
 			// Yes
 			addDynamicLabel((DynamicLabel)edgeLabel);
 		} // if DynamicLabel
 	} // putEdge
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Edge getEdge(URI uri) {
 		return getEdges().get(uri);
 	} // getEdge
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void putNode(Node node) {
 		getNodes().put(node.getURI(), node);
 		for (NodeLabel nodeLabel : node.getLabels()) {
 			putNodeLabel(nodeLabel);
 		} // for each NodeLabel
 	} // putNode
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Node getNode(URI uri) {
 		return getNodes().get(uri);
 	} // getNode
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void putNodeLabel(NodeLabel label) {
 		getNodeLabels().put(label.getURI(), label);
 		// Dynamic?
 		if (label instanceof DynamicLabel) {
 			// Yes
 			addDynamicLabel((DynamicLabel)label);
 		} // if DynamicLabel
 	} // putNodeLabel
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public NodeLabel getNodeLabel(URI uri) {
 		return getNodeLabels().get(uri);
 	} // getNodeLabel
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void putGraphLabel(Label label) {
 		getGraphLabels().put(label.getURI(), label);
 		// Dynamic?
 		if (label instanceof DynamicLabel) {
 			// Yes
 			addDynamicLabel((DynamicLabel)label);
 		} // if DynamicLabel
 	} // putGraphLabel
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Label getGraphLabel(URI uri) {
 		return getGraphLabels().get(uri);
 	} // getGraphLabel
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addDynamicLabel(DynamicLabel dynamiclabel) {
 		getDynamicLabels().add(dynamiclabel);
 	} // addDynamicLabel
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void switchToNextValue(STEMTime currentTime) {
 		for (final Iterator<DynamicLabel> graphLabelIter = getDynamicLabels()
 				.iterator(); graphLabelIter.hasNext();) {
 			final DynamicLabel dynamicLabel = graphLabelIter.next();
 			dynamicLabel.switchToNextValue();
 		} // for dynamic labels
 		setTime((STEMTime)EcoreUtil.copy(currentTime));
 	} // switchToNextValue
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 		@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GraphPackage.GRAPH__DECORATORS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getDecorators()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#toString()
 	 */
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder("\"" + dublinCore.getTitle()
 				+ "\",\n");
 
 		// How many nodes?
 
 		sb.append(getNumNodes() > 0 ? getNumNodes() + " Nodes\n" : "");
 		sb.append(getNumEdges() > 0 ? getNumEdges() + " Edges\n" : "");
 		sb.append(getNumNodeLabels() > 0 ? getNumNodeLabels() + " Node Labels "
 				: "");
 		sb.append(getNumGraphLabels() > 0 ? getNumGraphLabels()
 				+ " Graph Labels\n" : "");
 
 		sb.append(getNumDynamicLabels() > 0 ? getNumDynamicLabels()
 				+ " Dynamic Labels\n" : "");
 
 		return sb.toString();
 	} // toString
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 
 		// This is the number of labels that each node references
 		int nodeLabelCount = 0;
 		int dynamicLabelCount = 0;
 
 		// Nodes
 		if (nodes != null) {
 			// Yes
 			for (final Iterator<Node> nodeIter = getNodes().values().iterator(); nodeIter
 					.hasNext()
 					&& retValue;) {
 				final Node node = nodeIter.next();
 				retValue = retValue && node.sane();
 				assert retValue;
 
 				nodeLabelCount += node.getLabels().size();
 
 				// How many dynamic labels?
 				for (final Iterator<NodeLabel> labelIter = node.getLabels()
 						.iterator(); labelIter.hasNext();) {
 					final Label label = labelIter.next();
 					if (label instanceof DynamicLabel) {
 						dynamicLabelCount++;
 						retValue = false;
 						
 						// The dynamic label should be in the collection of
 						// dynamic labels maintained by the graph
 						for (DynamicLabel dl : getDynamicLabels()) {
 							if (dl == label) {
 								retValue = true;
 								break;
 							}
 						} // for each dynamic label
 						assert retValue;
 						
 					} // if DynamicLabel
 					// The label should be in the collection of node labels
 					retValue = retValue
 							&& getNodeLabels().containsKey(label.getURI());
 					assert retValue;
 					
 					
 				} // for each label of the node
 			} // for each node
 		} // if
 
 		// Edges?
 		if (edges != null) {
 			// Yes
 			for (final Iterator<Edge> edgeIter = getEdges().values().iterator(); edgeIter
 					.hasNext()
 					&& retValue;) {
 				final Edge edge = edgeIter.next();
 				retValue = retValue && edge.sane();
 				assert retValue;
 
 				// Is the edge's label dynamic?
 				if (edge.getLabel() instanceof DynamicLabel) {
 					// Yes
 					dynamicLabelCount++;
 				}
 			} // for each edge
 		} // if
 
 		// Node Labels?
 		if (nodeLabels != null) {
 			// Yes
 			// The number of node labels should match the number we counted
 			// above...not true for graph fragments that might have labels for
 			// nodes in other (sub) graph fragments.
 			// retValue = retValue && nodeLabelCount ==
 			// getNodeLabels().getSize();
 			// assert retValue;
 
 			for (final Iterator<NodeLabel> labelIter = getNodeLabels().values()
 					.iterator(); labelIter.hasNext() && retValue;) {
 				final NodeLabel nodeLabel = labelIter.next();
 				retValue = retValue && nodeLabel.sane();
 				assert retValue;
 			} // for each node label
 		} // if
 
 		if (graphLabels != null) {
 			// Yes
 			for (final Iterator<Label> graphLabelIter = getGraphLabels().values()
 					.iterator(); graphLabelIter.hasNext();) {
 				final Label graphLabel = graphLabelIter.next();
 				retValue = retValue && graphLabel.sane();
 				assert retValue;
 
 				if (graphLabel instanceof DynamicLabel) {
 					dynamicLabelCount++;
 				}
 			}
 		} // if graphLabels
 
 		// Dynamic Labels?
 		if (dynamicLabels != null) {
 			// Yes
 			// The number of dynamic labels should equal the count
 			retValue = dynamicLabelCount == getNumDynamicLabels();
 			assert retValue;
 
 			for (final Iterator<DynamicLabel> dynamiclIter = getDynamicLabels()
 					.iterator(); dynamiclIter.hasNext() && retValue;) {
 				final DynamicLabel dynamicLabel = dynamiclIter.next();
 				retValue = retValue && dynamicLabel.sane();
 				assert retValue;
 			} // for each dynamic label
 		} // if
 
 		return retValue;
 	} // sane
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GraphPackage.GRAPH__EDGES:
 				return ((InternalEList<?>)getEdges()).basicRemove(otherEnd, msgs);
 			case GraphPackage.GRAPH__NODES:
 				return ((InternalEList<?>)getNodes()).basicRemove(otherEnd, msgs);
 			case GraphPackage.GRAPH__GRAPH_LABELS:
 				return ((InternalEList<?>)getGraphLabels()).basicRemove(otherEnd, msgs);
 			case GraphPackage.GRAPH__NODE_LABELS:
 				return ((InternalEList<?>)getNodeLabels()).basicRemove(otherEnd, msgs);
 			case GraphPackage.GRAPH__UNRESOLVED_IDENTIFIABLES:
 				return ((InternalEList<?>)getUnresolvedIdentifiables()).basicRemove(otherEnd, msgs);
 			case GraphPackage.GRAPH__DECORATORS:
 				return ((InternalEList<?>)getDecorators()).basicRemove(otherEnd, msgs);
 			case GraphPackage.GRAPH__TIME:
 				return basicSetTime(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GraphPackage.GRAPH__EDGES:
 				if (coreType) return getEdges();
 				else return getEdges().map();
 			case GraphPackage.GRAPH__NODES:
 				if (coreType) return getNodes();
 				else return getNodes().map();
 			case GraphPackage.GRAPH__GRAPH_LABELS:
 				if (coreType) return getGraphLabels();
 				else return getGraphLabels().map();
 			case GraphPackage.GRAPH__NODE_LABELS:
 				if (coreType) return getNodeLabels();
 				else return getNodeLabels().map();
 			case GraphPackage.GRAPH__DYNAMIC_LABELS:
 				return getDynamicLabels();
 			case GraphPackage.GRAPH__NUM_EDGES:
 				return getNumEdges();
 			case GraphPackage.GRAPH__NUM_NODES:
 				return getNumNodes();
 			case GraphPackage.GRAPH__NUM_GRAPH_LABELS:
 				return getNumGraphLabels();
 			case GraphPackage.GRAPH__NUM_NODE_LABELS:
 				return getNumNodeLabels();
 			case GraphPackage.GRAPH__NUM_DYNAMIC_LABELS:
 				return getNumDynamicLabels();
 			case GraphPackage.GRAPH__UNRESOLVED_IDENTIFIABLES:
 				return getUnresolvedIdentifiables();
 			case GraphPackage.GRAPH__DECORATORS:
 				return getDecorators();
 			case GraphPackage.GRAPH__TIME:
 				return getTime();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 		@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case GraphPackage.GRAPH__EDGES:
 				((EStructuralFeature.Setting)getEdges()).set(newValue);
 				return;
 			case GraphPackage.GRAPH__NODES:
 				((EStructuralFeature.Setting)getNodes()).set(newValue);
 				return;
 			case GraphPackage.GRAPH__GRAPH_LABELS:
 				((EStructuralFeature.Setting)getGraphLabels()).set(newValue);
 				return;
 			case GraphPackage.GRAPH__NODE_LABELS:
 				((EStructuralFeature.Setting)getNodeLabels()).set(newValue);
 				return;
 			case GraphPackage.GRAPH__DYNAMIC_LABELS:
 				getDynamicLabels().clear();
 				getDynamicLabels().addAll((Collection<? extends DynamicLabel>)newValue);
 				return;
 			case GraphPackage.GRAPH__UNRESOLVED_IDENTIFIABLES:
 				getUnresolvedIdentifiables().clear();
 				getUnresolvedIdentifiables().addAll((Collection<? extends UnresolvedIdentifiable>)newValue);
 				return;
 			case GraphPackage.GRAPH__DECORATORS:
 				getDecorators().clear();
 				getDecorators().addAll((Collection<? extends Decorator>)newValue);
 				return;
 			case GraphPackage.GRAPH__TIME:
 				setTime((STEMTime)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case GraphPackage.GRAPH__EDGES:
 				getEdges().clear();
 				return;
 			case GraphPackage.GRAPH__NODES:
 				getNodes().clear();
 				return;
 			case GraphPackage.GRAPH__GRAPH_LABELS:
 				getGraphLabels().clear();
 				return;
 			case GraphPackage.GRAPH__NODE_LABELS:
 				getNodeLabels().clear();
 				return;
 			case GraphPackage.GRAPH__DYNAMIC_LABELS:
 				getDynamicLabels().clear();
 				return;
 			case GraphPackage.GRAPH__UNRESOLVED_IDENTIFIABLES:
 				getUnresolvedIdentifiables().clear();
 				return;
 			case GraphPackage.GRAPH__DECORATORS:
 				getDecorators().clear();
 				return;
 			case GraphPackage.GRAPH__TIME:
 				setTime((STEMTime)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case GraphPackage.GRAPH__EDGES:
 				return edges != null && !edges.isEmpty();
 			case GraphPackage.GRAPH__NODES:
 				return nodes != null && !nodes.isEmpty();
 			case GraphPackage.GRAPH__GRAPH_LABELS:
 				return graphLabels != null && !graphLabels.isEmpty();
 			case GraphPackage.GRAPH__NODE_LABELS:
 				return nodeLabels != null && !nodeLabels.isEmpty();
 			case GraphPackage.GRAPH__DYNAMIC_LABELS:
 				return dynamicLabels != null && !dynamicLabels.isEmpty();
 			case GraphPackage.GRAPH__NUM_EDGES:
 				return getNumEdges() != NUM_EDGES_EDEFAULT;
 			case GraphPackage.GRAPH__NUM_NODES:
 				return getNumNodes() != NUM_NODES_EDEFAULT;
 			case GraphPackage.GRAPH__NUM_GRAPH_LABELS:
 				return getNumGraphLabels() != NUM_GRAPH_LABELS_EDEFAULT;
 			case GraphPackage.GRAPH__NUM_NODE_LABELS:
 				return getNumNodeLabels() != NUM_NODE_LABELS_EDEFAULT;
 			case GraphPackage.GRAPH__NUM_DYNAMIC_LABELS:
 				return getNumDynamicLabels() != NUM_DYNAMIC_LABELS_EDEFAULT;
 			case GraphPackage.GRAPH__UNRESOLVED_IDENTIFIABLES:
 				return unresolvedIdentifiables != null && !unresolvedIdentifiables.isEmpty();
 			case GraphPackage.GRAPH__DECORATORS:
 				return decorators != null && !decorators.isEmpty();
 			case GraphPackage.GRAPH__TIME:
 				return time != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 } // GraphImpl
