 /**
  * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *    vano - initial API and implementation
  */
 package org.eclipse.gmf.internal.bridge.trace;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.gmf.codegen.gmfgen.GenChildNode;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenLinkLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenNodeLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.ToolGroup;
 import org.eclipse.gmf.internal.bridge.StatefulVisualIdentifierDispencer;
 
 public class MergingIdentifierDispenser implements StatefulVisualIdentifierDispencer {
 	
 	private static final int CANVAS_COUNT_BASE = 1000;
 	private static final int TOP_NODE_COUNT_BASE = 2000;
 	private static final int CHILD_NODE_COUNT_BASE = 3000;
 	private static final int LINK_COUNT_BASE = 4000;
 	private static final int NODE_LABEL_COUNT_BASE = 5000;
 	private static final int LINK_LABEL_COUNT_BASE = 6000;
 	private static final int COMPARTMENT_COUNT_BASE = 7000;
 	private static final int OVERFLOW_COUNT_BASE = 8000;
 	
 	private static final int TOOL_GROUP_COUNT_BASE = 0;
 	
 	private int myTopNodeCount = TOP_NODE_COUNT_BASE;
 	private int myChildNodeCount = CHILD_NODE_COUNT_BASE;
 	private int myLinkCount = LINK_COUNT_BASE;
 	private int myNodeLabelCount = NODE_LABEL_COUNT_BASE;
 	private int myLinkLabelCount = LINK_LABEL_COUNT_BASE;
 	private int myCompartmentCount = COMPARTMENT_COUNT_BASE;
 	private int myToolGroupCount = TOOL_GROUP_COUNT_BASE;
 	private int myOverflowCount = OVERFLOW_COUNT_BASE;
 	
 	private TraceModel myTraceModel;
 	private Map mySavingOptions;
 	
 	public void loadState(URI genModelFileURI) {
 		loadTraceModel(genModelFileURI);
 		initCounters();
 	}
 
 	public void saveState() {
 		myTraceModel.purgeUnprocessedTraces();
 		try {
 			myTraceModel.eResource().save(getSavingOptions());
 		} catch (IOException e) {
 			GmfTracePlugin.getInstance().logError("Unable to save trace model", e);
 		}
 		myTraceModel = null;
 	}
 	
 	private Map getSavingOptions() {
 		if (mySavingOptions == null) {
 			mySavingOptions = new HashMap();
 			mySavingOptions.put(XMIResource.OPTION_ENCODING, "UTF-8");
 		}
 		return mySavingOptions;
 	}
 	
 	private void loadTraceModel(URI genModelFileURI) {
 		URI traceModelURI = genModelFileURI.trimFileExtension().appendFileExtension("trace");
 		ResourceSet resSet = new ResourceSetImpl();
 		Resource traceRes;
 		try {
 			traceRes = resSet.getResource(traceModelURI, true);
 		} catch (RuntimeException e) {
 			traceRes = resSet.createResource(traceModelURI);
 		}
 
 		if (traceRes.getContents().size() > 0 && traceRes.getContents().get(0) instanceof TraceModel) {
 			myTraceModel = (TraceModel) traceRes.getContents().get(0);
 		} else {
 			myTraceModel = TraceFactory.eINSTANCE.createTraceModel();
 			traceRes.getContents().add(0, myTraceModel);
 		}
 	}
 
 	private void initCounters() {
 		myTopNodeCount = Math.max(myTopNodeCount, getMaxVid(myTraceModel.getNodeTraces()));
 		myChildNodeCount = Math.max(myChildNodeCount, getMaxVid(myTraceModel.getChildNodeTraces()));
 		myLinkCount = Math.max(myLinkCount, getMaxVid(myTraceModel.getLinkTraces()));
 		
 		initNodeChildrenCounters(myTraceModel.getNodeTraces());
 		initNodeChildrenCounters(myTraceModel.getChildNodeTraces());
 
 		for (Iterator it = myTraceModel.getLinkTraces().iterator(); it.hasNext();) {
 			GenLinkTrace trace = (GenLinkTrace) it.next();
 			myLinkLabelCount = Math.max(myLinkLabelCount, getMaxVid(trace.getLinkLabelTraces()));
 		}
 		
 		myToolGroupCount = Math.max(myToolGroupCount, getMaxVid(myTraceModel.getToolGroupTraces()));
 	}
 	
 	private void initNodeChildrenCounters(Collection nodeTraces) {
 		for (Iterator it = nodeTraces.iterator(); it.hasNext();) {
 			GenNodeTrace trace = (GenNodeTrace) it.next();
 			myNodeLabelCount = Math.max(myNodeLabelCount, getMaxVid(trace.getNodeLabelTraces()));
 			myCompartmentCount = Math.max(myCompartmentCount, getMaxVid(trace.getCompartmentTraces()));
 		}
 	}
 	
 	private int getMaxVid(Collection abstractTraces) {
 		int id = -1;
 		for (Iterator it = abstractTraces.iterator(); it.hasNext();) {
 			AbstractTrace nextTrace = (AbstractTrace) it.next();
 			id = Math.max(id, nextTrace.getVisualID());
 			myOverflowCount = Math.max(myOverflowCount, nextTrace.getVisualID());
 		}
 		return id;
 	}
 
 	public int get(GenDiagram diagram) {
 		return CANVAS_COUNT_BASE;
 	}
 
 	public int get(GenTopLevelNode node) {
 		int visualID = getMatchingVID(node, myTraceModel.getNodeTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextTopNodeVID();
 		GenNodeTrace nodeTrace = TraceFactory.eINSTANCE.createGenNodeTrace();
 		nodeTrace.setVisualID(visualID);
 		nodeTrace.setContext(node);
 		nodeTrace.setProcessed(true);
 		myTraceModel.getNodeTraces().add(nodeTrace);
 		return visualID;
 	}
 
 	public int get(GenNodeLabel nodeLabel) {
 		GenNodeTrace nodeTrace = myTraceModel.getNodeTrace(nodeLabel.getNode().getVisualID());
 		int visualID = getMatchingVID(nodeLabel, nodeTrace.getNodeLabelTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextNodeLabelVID();
 		GenNodeLabelTrace nodeLabelTrace = TraceFactory.eINSTANCE.createGenNodeLabelTrace();
 		nodeLabelTrace.setVisualID(visualID);
 		nodeTrace.getNodeLabelTraces().add(nodeLabelTrace);
 		nodeLabelTrace.setContext(nodeLabel);
 		nodeLabelTrace.setProcessed(true);
 		return visualID;
 	}
 
 	public int get(GenLink link) {
 		int visualID = getMatchingVID(link, myTraceModel.getLinkTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextLinkVID();
 		GenLinkTrace nodeLabelTrace = TraceFactory.eINSTANCE.createGenLinkTrace();
 		nodeLabelTrace.setVisualID(visualID);
 		nodeLabelTrace.setContext(link);
 		myTraceModel.getLinkTraces().add(nodeLabelTrace);
 		nodeLabelTrace.setProcessed(true);
 		return visualID;
 	}
 
 	public int get(GenChildNode childNode) {
 		int visualID = getMatchingVID(childNode, myTraceModel.getChildNodeTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextChildNodeVID();
 		GenChildNodeTrace childNodeTrace = TraceFactory.eINSTANCE.createGenChildNodeTrace();
 		childNodeTrace.setVisualID(visualID);
 		childNodeTrace.setContext(childNode);
 		myTraceModel.getChildNodeTraces().add(childNodeTrace);
 		childNodeTrace.setProcessed(true);
 		return visualID;
 	}
 
 	public int get(GenCompartment compartment) {
 		GenNodeTrace nodeTrace = myTraceModel.getNodeTrace(compartment.getNode().getVisualID());
 		int visualID = getMatchingVID(compartment, nodeTrace.getCompartmentTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextCompartmentVID();
 		GenCompartmentTrace compartmentTrace = TraceFactory.eINSTANCE.createGenCompartmentTrace();
 		compartmentTrace.setVisualID(visualID);
 		nodeTrace.getCompartmentTraces().add(compartmentTrace);
 		compartmentTrace.setContext(compartment);
 		compartmentTrace.setProcessed(true);
 		return visualID;
 	}
 
 	public int get(GenLinkLabel label) {
 		GenLinkTrace linkTrace = myTraceModel.getLinkTrace(label.getLink().getVisualID());
 		int visualID = getMatchingVID(label, linkTrace.getLinkLabelTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextLinkLabelVID();
 		GenLinkLabelTrace linkLabelTrace = TraceFactory.eINSTANCE.createGenLinkLabelTrace();
 		linkLabelTrace.setVisualID(visualID);
 		linkTrace.getLinkLabelTraces().add(linkLabelTrace);
 		linkLabelTrace.setContext(label);
 		linkLabelTrace.setProcessed(true);
 		return visualID;
 	}
 
 	public int get(ToolGroup toolGroup) {
 		int visualID = getMatchingVID(toolGroup, myTraceModel.getToolGroupTraces());
 		if (visualID > -1) {
 			return visualID;
 		}
 		
 		visualID = getNextToolVID();
 		ToolGroupTrace toolGroupTrace = TraceFactory.eINSTANCE.createToolGroupTrace();
 		toolGroupTrace.setVisualID(visualID);
 		toolGroupTrace.setContext(toolGroup);
 		myTraceModel.getToolGroupTraces().add(toolGroupTrace);
 		toolGroupTrace.setProcessed(true);
 		return visualID;
 	}
 
 	private int getMatchingVID(Object context, Collection matchingTraces) {
 		for (Iterator it = matchingTraces.iterator(); it.hasNext();) {
 			MatchingTrace trace = (MatchingTrace) it.next();
 			if (trace.isProcessed()) {
 				continue;
 			}
 			Object result = trace.getQuery().evaluate(context);
 			if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
 				trace.setProcessed(true);
 				return trace.getVisualID();
 			}
 		}
 		return -1;
 	}
 
 	private int getNextToolVID() {
 		if (++myToolGroupCount < CANVAS_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New tool visualID issued: " + myToolGroupCount);
 			return myToolGroupCount;
 		}
 		return ++myOverflowCount;
 	}
 	
 	private int getNextTopNodeVID() {
 		if (++myTopNodeCount < CHILD_NODE_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New top node visualID issued: " + myTopNodeCount);
 			return myTopNodeCount;
 		}
 		return ++myOverflowCount;
 	}
 	
 	private int getNextChildNodeVID() {
 		if (++myChildNodeCount < LINK_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New child node visualID issued: " + myChildNodeCount);
 			return myChildNodeCount;
 		}
 		return ++myOverflowCount;
 	}
 	
 	private int getNextLinkVID() {
 		if (++myLinkCount < NODE_LABEL_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New link visualID issued: " + myLinkCount);
 			return myLinkCount;
 		}
 		return ++myOverflowCount;
 	}
 	
 	private int getNextNodeLabelVID() {
 		if (++myNodeLabelCount < LINK_LABEL_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New node label visualID issued: " + myNodeLabelCount);
 			return myNodeLabelCount;
 		}
 		return ++myOverflowCount;
 	}
 	
 	private int getNextLinkLabelVID() {
 		if (++myLinkLabelCount < COMPARTMENT_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New link label visualID issued: " + myLinkLabelCount);
 			return myLinkLabelCount;
 		}
 		return ++myOverflowCount;
 	}
 	
 	private int getNextCompartmentVID() {
 		if (++myCompartmentCount < OVERFLOW_COUNT_BASE) {
 			GmfTracePlugin.getInstance().logDebugInfo("New compartment visualID issued: " + myCompartmentCount);
 			return myCompartmentCount;
 		}
 		return ++myOverflowCount;
 	}
 
 }
 
