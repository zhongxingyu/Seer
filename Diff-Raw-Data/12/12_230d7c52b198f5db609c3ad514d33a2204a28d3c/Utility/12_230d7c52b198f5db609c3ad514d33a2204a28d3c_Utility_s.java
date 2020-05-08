 package com.tranhoangdai.korengui.client.imp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.tranhoangdai.korengui.client.SvgPanel;
 import com.tranhoangdai.korengui.client.imp.link.NodeLink;
 import com.tranhoangdai.korengui.client.imp.node.EndHost;
 import com.tranhoangdai.korengui.client.imp.node.Gateway;
 import com.tranhoangdai.korengui.client.imp.node.Node;
 import com.tranhoangdai.korengui.client.imp.node.Switch;
 import com.tranhoangdai.korengui.client.imp.node.VisualNode;
 import com.tranhoangdai.korengui.client.imp.node.zoom.Cluster;
 import com.tranhoangdai.korengui.client.imp.node.zoom.ZoomableNode;
 import com.tranhoangdai.korengui.client.interf.TopologyNotifier;
 import com.tranhoangdai.korengui.client.interf.ZoomNotifier;
 import com.tranhoangdai.korengui.client.service.TopologyService;
 import com.tranhoangdai.korengui.client.service.TopologyServiceAsync;
 
 /**
  * This class handles interaction between nodes model and GUI
  */
 public class Utility {
 
 	public enum ActionState {
 		NOTHING, ZOOMOUT, ZOOMIN, FLOW
 	}
 
 	public static Utility INSTANCE = GWT.create(Utility.class);
 	private ActionState state = ActionState.NOTHING;
 	private List<Node> zoomStack = new ArrayList<Node>();
 	private Map<String, Node> globalNodes = new HashMap<String, Node>();
 	private Map<Integer, NodeLink> globalLinks = new HashMap<Integer, NodeLink>();
 
 	private List<TopologyNotifier> topologyNotifiers = new ArrayList<TopologyNotifier>();
 	private ZoomNotifier zoomNotifier;
 	
 	
 	public List<NodeLink> getPathFlowConnection(Node startNode, Node endNode){
 		return null;
 	}
 
 	private void notifyFinishDownloadGlobalTopology() {
 		for (TopologyNotifier tn : topologyNotifiers) {
 			tn.finishDownload(globalNodes, globalLinks);
 		}
 	}
 
 	public void downloadGlobalTopology() {
 		downloadTopologySwitches();
 	}
 
 	private void downloadTopologySwitches() {
 		TopologyServiceAsync topo = GWT.create(TopologyService.class);
 
 		AsyncCallback<String> callback = new AsyncCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 
 				JSONValue value = JSONParser.parseStrict(result);
 				JSONArray array = value.isArray();
 
 				if (array != null) {
 					for (int i = 0; i < array.size(); i++) {
 						JSONObject jobj = array.get(i).isObject();
 						createNode(jobj);
 					}
 					// finish download nodes info, now download links info
 					downloadTopologyLinks();
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				System.out.println("failed to load nodes");
 
 			}
 		};
 
 		topo.getTopologySwitches(callback);
 	}
 
 	private void downloadTopologyLinks() {
 		TopologyServiceAsync topo = GWT.create(TopologyService.class);
 
 		AsyncCallback<String> callback = new AsyncCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 
 				JSONValue value = JSONParser.parseStrict(result);
 				JSONArray array = value.isArray();
 				if (array != null) {
 					for (int i = 0; i < array.size(); i++) {
 						JSONObject obj = array.get(i).isObject();
 						String srcIp = obj.get("src-switch").isString().stringValue();
 						int srcport = (int) obj.get("src-port").isNumber().doubleValue();
 						String dstIp = obj.get("dst-switch").isString().stringValue();
 						int dstport = (int) obj.get("dst-port").isNumber().doubleValue();
 						NodeLink link = new NodeLink(srcIp, srcport, dstIp, dstport);						
 						
 						link.findAndMatchNode(globalNodes);
 						globalLinks.put(link.getId(), link);
 					}
 					
 					fakeChildLink();
 
 					// finish download links, notify finish download event
 					notifyFinishDownloadGlobalTopology();
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				System.out.println("failed to load links");
 
 			}
 		};
 
 		topo.getTopologyLinks(callback);
 	}
 	
 	private void fakeChildLink(){
 		for (Node node: globalNodes.values()){
 			if(node.getClass().equals(Cluster.class)){
 				((Cluster)node).fakeLinks();
 			}
 		}
 	}
 
 	private void createNode(JSONObject jobj) {
 		Node activeNode = null;
 		Node tempNode = null;
 
 		String nodeId = jobj.get("dpid").isString().stringValue();
 		tempNode = new VisualNode(nodeId, 0, 0);
 		tempNode = setNodeProperties(tempNode, jobj);
 
 		if (setChildNode(tempNode, jobj)) {
 			// do nothing
 		} else {
 			activeNode = tempNode;
 		}
 
 		if (activeNode != null) {
 			globalNodes.put(activeNode.getDpid(), activeNode);
 		}
 	}
 
 	private Node setNodeProperties(Node tempNode, JSONObject jobj) {
 
 		if (jobj.get("type") != null) {
 			String type = jobj.get("type").isString().stringValue();
 
 			if (type.equals("cluster")) {
 				tempNode = new Cluster(tempNode.getDpid(), tempNode.getX(), tempNode.getY());
 			} else if (type.equals("gateway")) {
 				tempNode = new Gateway(tempNode.getDpid(), tempNode.getX(), tempNode.getY());
 			} else if (type.equals("switch")) {
 				tempNode = new Switch(tempNode.getDpid(), tempNode.getX(), tempNode.getY());
 			} else if (type.equals("endhost")) {
 				tempNode = new EndHost(tempNode.getDpid(), tempNode.getX(), tempNode.getY());
 			}
 		}
 
 		return tempNode;
 	}
 
 	private boolean setChildNode(Node tempNode, JSONObject jobj) {
 
 		boolean result = false;
 
 		if (jobj.get("childOf") != null) {
 
 			/*
 			 * doesn't know why it wrap quote value into the string, so remove
 			 * it
 			 */
 			String belongToId = jobj.get("childOf").isString().toString().replaceAll("\"", "");
 
 			if (globalNodes.get(belongToId) != null) {
 				Node parentNode = globalNodes.get(belongToId);
 				((Cluster) parentNode).addChildNode(tempNode);
 				result = true;
 			}
 		}
 		return result;
 	}
 
 	public void notifyGuiWantToZoomToNode(ZoomableNode zoomNode) {
 		if (getState() == ActionState.ZOOMIN) {
 			zoomStack.add(zoomNode);			
 			SvgPanel.INSTANCE.setupZoomTab(zoomNode);
 			setState(ActionState.NOTHING);
 		}
 	}
 
 	public ActionState getState() {
 		return state;
 	}
 
 	public void setState(ActionState state) {
 		this.state = state;
 	}
 
 	public List<Node> getZoomStack() {
 		return zoomStack;
 	}
 
 	public void setZoomStack(List<Node> zoomStack) {
 		this.zoomStack = zoomStack;
 	}
 
 	public Map<String, Node> getGlobalNodes() {
 		return globalNodes;
 	}
 
 	public void setGlobalNodes(Map<String, Node> activeNodes) {
 		globalNodes = activeNodes;
 	}
 
 	public Map<Integer, NodeLink> getGlobalLinks() {
 		return globalLinks;
 	}
 
 	public void setGlobalinks(Map<Integer, NodeLink> activeLinks) {
 		globalLinks = activeLinks;
 	}
 	
 
 	public void addTopologyAble(TopologyNotifier topologyAble) {
 		topologyNotifiers.add(topologyAble);
 	}
 
 	public ZoomNotifier getZoomable() {
 		return zoomNotifier;
 	}
 
 	public void setZoomable(ZoomNotifier zoomable) {
 		this.zoomNotifier = zoomable;
 	}
 
 }
