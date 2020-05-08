 /*******************************************************************************
  * Copyright (c) 2011 Guillaume Hillairet.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Guillaume Hillairet - initial API and implementation
  *******************************************************************************/
 package org.eclipselabs.blueprints.emf.internal;
 
 import static org.eclipselabs.blueprints.emf.util.BlueprintsEmfUtil.isNativeType;
 import static org.eclipselabs.blueprints.emf.util.Tokens.BLUEPRINTS_EMF_ECLASS;
 import static org.eclipselabs.blueprints.emf.util.Tokens.BLUEPRINTS_EMF_INDEX_KEY;
 import static org.eclipselabs.blueprints.emf.util.Tokens.RESOURCE_URI;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.XMLResource.URIHandler;
 import org.eclipselabs.blueprints.emf.util.GraphUtil;
 import org.eclipselabs.blueprints.emf.util.Tokens;
 
 import com.tinkerpop.blueprints.pgm.CloseableSequence;
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.Index;
 import com.tinkerpop.blueprints.pgm.IndexableGraph;
 import com.tinkerpop.blueprints.pgm.Vertex;
 
 /**
  * 
  * @author ghillairet
  *
  */
 public class GraphOutputStream extends ByteArrayOutputStream implements URIConverter.Saveable {
 
 	private final IndexableGraph graph;
 	private final Map<?, ?> options;
 	private Resource resource;
 	private final Index<Vertex> vertexIndex;
 	private final Index<Edge> edgeIndex;
 	
 	public GraphOutputStream(IndexableGraph graph, Index<Vertex> vertexIndex, Index<Edge> edgeIndex, Map<?, ?> options) {
 		this.graph = graph;
 		this.vertexIndex = vertexIndex;
 		this.edgeIndex = edgeIndex;
 		this.options = options;
 	}
 
 	@Override
 	public void saveResource(Resource resource) throws IOException {
 		this.resource = resource;
 	}
 	
 	@Override
 	public void close() throws IOException {
 		super.close();
 		
 		XMLResource.URIHandler uriHandler = (XMLResource.URIHandler) options.get(XMLResource.OPTION_URI_HANDLER);
 
 		if (uriHandler == null)
 			uriHandler = new org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl();
 		
 		uriHandler.setBaseURI(resource.getURI());
 		
 		for (TreeIterator<EObject> it = resource.getAllContents(); it.hasNext();) {
 			saveEObject(it.next(), uriHandler);
 		}
 	}
 
 	private Vertex getVertex(EObject object) {
 		final URI uri = EcoreUtil.getURI(object);
 		String safeURI = GraphUtil.safeURI(uri);
 		
 		final CloseableSequence<Vertex> vertices = vertexIndex.get(BLUEPRINTS_EMF_INDEX_KEY, safeURI);
 		final Vertex vertex;
 		
 		if (vertices.hasNext()) {
 			vertex = vertices.next();
 		} else {
 			
 			vertex = buildVertex(object, safeURI);
 			
 			if (resource instanceof XMLResource)
 			{
 				String id = ((XMLResource) resource).getID(object);
 				
 				if (id != null)
 					vertex.setProperty(Tokens.EXTRINSIC_ID_KEY, id);
 			}
 		}
 		
 		return vertex;
 	}
 	
 	private Vertex getVertex(EReference reference, EObject object, URIHandler uriHandler) {
 		final URI uri = EcoreUtil.getURI(object);
 		String safeURI = GraphUtil.safeURI(uri);
 		
 		final CloseableSequence<Vertex> vertices = vertexIndex.get(BLUEPRINTS_EMF_INDEX_KEY, safeURI);
 		final Vertex vertex;
 		
 		if (vertices.hasNext()) {
 			vertex = vertices.next();
 		} else {
 			
 			vertex = buildVertex(object, safeURI);
 			
 			InternalEObject internalEObject = (InternalEObject) object;
 			URI eProxyURI = internalEObject.eProxyURI();
 			
 			if (eProxyURI != null) {
 				URI deresolvedURI = uriHandler.deresolve(eProxyURI);
 				vertex.setProperty(Tokens.BLUEPRINTS_EMF_PROXY_URI, GraphUtil.safeURI(deresolvedURI));
 			}
 			else if (!reference.isContainment() || (reference.isResolveProxies() && internalEObject.eDirectResource() != null)) {
 				URI deresolvedURI = uriHandler.deresolve(EcoreUtil.getURI(object));
 				vertex.setProperty(Tokens.BLUEPRINTS_EMF_PROXY_URI, GraphUtil.safeURI(deresolvedURI));
 			}
 		}
 		
 		return vertex;
 	}
 	
 	private Vertex buildVertex(EObject object, String uri) {
 		Vertex vertex = graph.addVertex(null);
 		vertex.setProperty(BLUEPRINTS_EMF_INDEX_KEY, uri);
 		vertex.setProperty(RESOURCE_URI, object.eResource().getURI().toString());
 		
 		URI eClassURI = EcoreUtil.getURI(object.eClass());
 		vertex.setProperty(BLUEPRINTS_EMF_ECLASS, eClassURI.toString());
 		
 		return vertex;
 	}
 	
 	private Edge getEdge(EReference reference, EObject source, EObject target, URIHandler uriHandler) {
 		final String edgeID = GraphUtil.getEdgeID(source, target, reference);
 		final Iterable<Edge> edges = edgeIndex.get(BLUEPRINTS_EMF_INDEX_KEY, edgeID);
 		final Edge edge;
 		
 		if (edges.iterator().hasNext()) {
 			edge = edges.iterator().next();
 		} else {
 			edge = graph.addEdge(null, getVertex(source), getVertex(reference, target, uriHandler), reference.getName());
 			edge.setProperty(BLUEPRINTS_EMF_INDEX_KEY, edgeID);
 			edgeIndex.put(BLUEPRINTS_EMF_INDEX_KEY, edgeID, edge);
 		}
 		return edge;
 	}
 	
 	protected void saveEObject(EObject object, URIHandler uriHandler) {
 		final Vertex vertex = getVertex(object);
 		
 		for (EAttribute attr: object.eClass().getEAllAttributes()) {
 			if (!attr.isTransient() && object.eIsSet(attr)) {
 				buildVertexProperty(vertex, object, attr);
 			}
 		}
 		
 		for (EReference ref: object.eClass().getEAllReferences()) {
 			if (!ref.isTransient() && object.eIsSet(ref)) {
 				
 				Object value = object.eGet(ref);
 				
 				if (ref.isMany()) {
 					@SuppressWarnings("unchecked")
 					List<EObject> values = ((InternalEList<EObject>) value).basicList();
 					
 					for (EObject obj: values) {
 						getEdge(ref, object, obj, uriHandler);
 					}
 				} else {
 					getEdge(ref, object, (EObject) value, uriHandler);
 				}
 			}
 		}
 	}
 	
 	protected void buildVertexProperty(Vertex vertex, EObject object, EAttribute attribute) {
 		if (!attribute.isTransient() && (object.eIsSet(attribute) || (!attribute.isUnsettable()))) {
 			Object value = object.eGet(attribute);
 
 			if (FeatureMapUtil.isFeatureMap(attribute)) {
 				FeatureMap.Internal featureMap = (FeatureMap.Internal) value;
 				Iterator<FeatureMap.Entry> iterator = featureMap.basicIterator();
 				
 				while (iterator.hasNext()) {
 					// TODO
 					FeatureMap.Entry entry = iterator.next();
 					EStructuralFeature feature = entry.getEStructuralFeature();
 					
 					if (feature instanceof EAttribute) {
 						value = getEAttributeValue((EAttribute) feature, entry.getValue());
 					} else {
 						// TODO
 					}
 				}
 			} else if (attribute.isMany()) {
 				
 				EDataType eDataType = attribute.getEAttributeType();
 
 				if (!isNativeType(eDataType)) {
 					EList<?> rawValues = (EList<?>) value;
 					ArrayList<String> values = new ArrayList<String>(rawValues.size());
 
 					for (Object rawValue : rawValues) {
 						values.add(EcoreUtil.convertToString(eDataType, rawValue));
 					}
 					
 					value = values;
 				}
 				
 			} else {
 				value = getEAttributeValue(attribute, value);
 			}

			vertex.setProperty(attribute.getName(), value);
 		}
 	}
 
 	private Object getEAttributeValue(EAttribute attribute, Object value) {
 		final EDataType eDataType = attribute.getEAttributeType();
 		if (!isNativeType(eDataType))
 			return EcoreUtil.convertToString(eDataType, value);
 		
 		return value;
 	}
 	
 }
