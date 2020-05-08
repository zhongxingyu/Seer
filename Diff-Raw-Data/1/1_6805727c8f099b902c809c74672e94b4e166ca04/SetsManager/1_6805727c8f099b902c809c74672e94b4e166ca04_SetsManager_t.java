 package edu.ucsf.rbvi.setsApp.internal.tasks;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyRow;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyTableUtil;
 import org.cytoscape.model.subnetwork.CySubNetwork;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewManager;
 
 import edu.ucsf.rbvi.setsApp.internal.CyIdType;
 import edu.ucsf.rbvi.setsApp.internal.Set;
 import edu.ucsf.rbvi.setsApp.internal.events.SetChangedEvent;
 import edu.ucsf.rbvi.setsApp.internal.events.SetChangedListener;
 
 public class SetsManager {
 	private ConcurrentHashMap<String, Set<? extends CyIdentifiable>> setsMap;
 	private ConcurrentHashMap<String, CyNetwork> networkSetNames;
 	private ConcurrentHashMap<String, CyIdType> setType;
 	private ArrayList<SetChangedListener> setChangedListener = new ArrayList<SetChangedListener>();
 	
 	public SetsManager() {
 		this.setsMap = new ConcurrentHashMap<String, Set<? extends CyIdentifiable>> ();
 		this.networkSetNames = new ConcurrentHashMap<String, CyNetwork>();
		this.setType = new ConcurrentHashMap<String, CyIdType>();
 	}
 	
 	public SetsManager(SetChangedListener s) {
 		this.setsMap = new ConcurrentHashMap<String, Set<? extends CyIdentifiable>> ();
 		this.networkSetNames = new ConcurrentHashMap<String, CyNetwork>();
 		if (s != null)
 			this.setChangedListener.add(s);
 	}
 	
 	public void addSetChangedListener(SetChangedListener s) {
 		if (s != null)
 			this.setChangedListener.add(s);
 	}
 	
 	private void fireSetCreatedEvent(String setName) {
 		SetChangedEvent event = new SetChangedEvent(setName);
 		event.setSetName(setName);
 		Iterator<SetChangedListener> iterator = setChangedListener.iterator();
 		while (iterator.hasNext())
 			iterator.next().setCreated(event);
 	}
 	
 	private void fireSetAddedEvent(String setName, List<? extends CyIdentifiable> added) {
 		SetChangedEvent event = new SetChangedEvent(setName);
 		event.setSetName(setName);
 		event.cyIdsAdded(added);
 		for (SetChangedListener listener: setChangedListener)
 			listener.setChanged(event);
 	}
 	
 	private void fireSetRemovedEvent(String setName, List<? extends CyIdentifiable> removed) {
 		SetChangedEvent event = new SetChangedEvent(setName);
 		event.setSetName(setName);
 		event.cyIdsRemoved(removed);
 		Iterator<SetChangedListener> iterator = setChangedListener.iterator();
 		for (SetChangedListener listener: setChangedListener)
 			listener.setChanged(event);
 	}
 	
 	private void fireSetRenamedEvent(String oldName, String setName) {
 		SetChangedEvent event = new SetChangedEvent(setName);
 		event.setSetName(setName);
 		event.changeSetName(oldName, setName);
 		for (SetChangedListener listener: setChangedListener)
 			listener.setRenamed(event);
 	}
 	
 	private void fireSetRemovedEvent(String setName) {
 		SetChangedEvent event = new SetChangedEvent(setName);
 		event.setSetName(setName);
 		for (SetChangedListener listener: setChangedListener)
 			listener.setRemoved(event);
 	}
 	
 	public void createSet(String name, CyNetwork cyNetwork, List<CyNode> cyNodes, List<CyEdge> cyEdges) {
 		if (! setsMap.containsKey(name))
 			if ((cyNodes != null && ! cyNodes.isEmpty()) || (cyEdges != null && ! cyEdges.isEmpty())) {
 				if (cyNodes != null && ! cyNodes.isEmpty()) {
 					List<CyNode> filteredNodes = new ArrayList<CyNode>();
 					for (CyNode n: cyNodes) {
 						if (cyNetwork.getRow(n) != null) filteredNodes.add(n);
 					}
 					setsMap.put(name, new Set<CyNode>(name,filteredNodes));
 					setType.put(name, CyIdType.NODE);
 				}
 				if (cyEdges != null && ! cyEdges.isEmpty()) {
 					List<CyEdge> filteredEdges = new ArrayList<CyEdge>();
 					for (CyEdge e: cyEdges) {
 						if (cyNetwork.getRow(e) != null) filteredEdges.add(e);
 					}
 					setsMap.put(name, new Set<CyEdge>(name, filteredEdges));
 					setType.put(name, CyIdType.EDGE);
 				}
 				networkSetNames.put(name, cyNetwork);
 				fireSetCreatedEvent(name);
 			}
 	}
 	
 	public void createSetFromStream(String name, String column, CyNetwork network, BufferedReader reader, CyIdType type) {
 		if (! setsMap.containsKey(name)) {
 			HashSet<String> inputSet = new HashSet<String>();
 			String curLine;
 			try {
 				while ((curLine = reader.readLine()) != null) inputSet.add(curLine);
 			}
 			catch (IOException e) {e.printStackTrace();}
 			if (network != null) {
 				CyTable table = null;
 				if (type == CyIdType.NODE) {
 					table = network.getDefaultNodeTable();
 					List<Long> cyIdList = table.getPrimaryKey().getValues(Long.class);
 					List<CyNode> cyNodes = new ArrayList<CyNode>();
 					for (Long cyId: cyIdList) {
 						String curCell = table.getRow(cyId).get(column, String.class);
 						if (inputSet.contains(curCell))
 							cyNodes.add(network.getNode(cyId));
 					}
 					setsMap.put(name, new Set<CyNode>(name, cyNodes));
 				}
 				if (type == CyIdType.EDGE) {
 					table = network.getDefaultEdgeTable();
 					List<Long> cyIdList = table.getPrimaryKey().getValues(Long.class);
 					List<CyEdge> cyEdges = new ArrayList<CyEdge>();
 					for (Long cyId: cyIdList) {
 						String curCell = table.getRow(cyId).get(column, String.class);
 						if (inputSet.contains(curCell))
 							cyEdges.add(network.getEdge(cyId));
 					}
 					setsMap.put(name, new Set<CyEdge>(name, cyEdges));
 				}
 				networkSetNames.put(name, network);
 				setType.put(name, type);
 				fireSetCreatedEvent(name);
 			}
 		}
 	}
 	
 	public void createSetFromAttributes(String name, String column, Object attribute, CyNetworkManager networkManager, String networkName, CyIdType type) {
 		if (! setsMap.containsKey(name)) {
 			CyNetwork network = null;
 			java.util.Set<CyNetwork> networkSet = networkManager.getNetworkSet();
 			for (CyNetwork net: networkSet) {
 				CyTable cyTable = net.getDefaultNetworkTable();
 				String netName = cyTable.getRow(net.getSUID()).get("name", String.class);
 				if (networkName.equals(netName))
 					network = net;
 			}
 			if (network != null) {
 				if (type == CyIdType.NODE) {
 					CyTable cyTable = network.getDefaultNodeTable();
 					final ArrayList<CyNode> nodes = new ArrayList<CyNode>();
 					final Collection<CyRow> selectedRows = cyTable.getMatchingRows(column, attribute);
 					String primaryKey = cyTable.getPrimaryKey().getName();
 					for (CyRow row: selectedRows) {
 						final Long nodeId = row.get(primaryKey, Long.class);
 						if (nodeId == null) continue;
 						final CyNode node = network.getNode(nodeId);
 						if (node == null) continue;
 						nodes.add(node);
 					}
 					setsMap.put(name, new Set<CyNode>(name, nodes));
 				}
 				if (type == CyIdType.EDGE) {
 					CyTable cyTable = network.getDefaultEdgeTable();
 					final ArrayList<CyEdge> edges = new ArrayList<CyEdge>();
 					final Collection<CyRow> selectedRows = cyTable.getMatchingRows(column, attribute);
 					String primaryKey = cyTable.getPrimaryKey().getName();
 					for (CyRow row: selectedRows) {
 						final Long edgeId = row.get(primaryKey, Long.class);
 						if (edgeId == null) continue;
 						final CyEdge edge = network.getEdge(edgeId);
 						if (edge == null) continue;
 						edges.add(edge);
 					}
 					setsMap.put(name, new Set<CyEdge>(name, edges));		
 				}
 				networkSetNames.put(name, network);
 				setType.put(name, type);
 				fireSetCreatedEvent(name);
 			}
 		}
 	}
 	
 	public void createSetsFromAttributes(String name, String column, CyNetwork network, CyIdType type) {
 		if (! setsMap.containsKey(name)) {
 			if (network != null) {
 				if (type == CyIdType.NODE) {
 					CyTable cyTable = network.getDefaultNodeTable();
 					CyColumn cyIdColumn = cyTable.getPrimaryKey();
 					List<Long> cyIdList = cyIdColumn.getValues(Long.class);
 					HashMap<String, Set<CyNode>> cyNodeSet = new HashMap<String, Set<CyNode>>();
 					for (Long cyId: cyIdList) {
 						String attrName = name + ":" + cyTable.getRow(cyId).get(column, String.class);
 						if (!cyNodeSet.containsKey(attrName)) cyNodeSet.put(attrName, new Set<CyNode>(attrName));
 						cyNodeSet.get(attrName).add(network.getNode(cyId));
 					}
 					for (String s: cyNodeSet.keySet()) {
 						setsMap.put(s, cyNodeSet.get(s));
 						networkSetNames.put(s, network);
 						setType.put(s, CyIdType.NODE);
 						fireSetCreatedEvent(s);
 					}
 				}
 				if (type == CyIdType.EDGE) {
 					CyTable cyTable = network.getDefaultEdgeTable();
 					CyColumn cyIdColumn = cyTable.getPrimaryKey();
 					List<Long> cyIdList = cyIdColumn.getValues(Long.class);
 					HashMap<String, Set<CyEdge>> cyNodeSet = new HashMap<String, Set<CyEdge>>();
 					for (Long cyId: cyIdList) {
 						String attrName = name + ":" + cyTable.getRow(cyId).get(column, String.class);
 						if (!cyNodeSet.containsKey(attrName)) cyNodeSet.put(attrName, new Set<CyEdge>(attrName));
 						cyNodeSet.get(attrName).add(network.getEdge(cyId));
 					}
 					for (String s: cyNodeSet.keySet()) {
 						setsMap.put(s, cyNodeSet.get(s));
 						networkSetNames.put(s, network);
 						setType.put(s, CyIdType.EDGE);
 						fireSetCreatedEvent(s);
 					}
 				}
 			}
 		}
 	}
 	
 	public void createSetFromNetworkView(String name, CyNetworkView cyNetworkView, CyIdType type) {
 		if (! setsMap.containsKey(name)) {
 			List<CyNode> cyNodes = null;
 			List<CyEdge> cyEdges = null;
 			CyNetwork cyNetwork = cyNetworkView.getModel();
 			if (type == CyIdType.NODE) {
 				cyNodes = CyTableUtil.getNodesInState(cyNetwork, CyNetwork.SELECTED, true);
 				if (! cyNodes.isEmpty()) {
 					setsMap.put(name, new Set<CyNode>(name,cyNodes));
 					networkSetNames.put(name, cyNetwork);
 					setType.put(name, type);
 					fireSetCreatedEvent(name);
 				}
 			}
 			else if (type == CyIdType.EDGE) {
 				cyEdges = CyTableUtil.getEdgesInState(cyNetwork, CyNetwork.SELECTED, true);
 				if (! cyEdges.isEmpty()) {
 					setsMap.put(name, new Set<CyEdge>(name,cyEdges));
 					networkSetNames.put(name, cyNetwork);
 					setType.put(name, type);
 					fireSetCreatedEvent(name);
 				}
 			}
 		}
 	}
 	
 	public void createSetFromSelectedNetwork(String name, CyNetworkViewManager networkViewManager, CyIdType type) {
 		if (! setsMap.containsKey(name)) {
 			List<CyNode> cyNodes = null;
 			List<CyEdge> cyEdges = null;
 			CyNetwork cyNetwork = null;
 			for (CyNetworkView networkView: networkViewManager.getNetworkViewSet()) {
 				cyNetwork = networkView.getModel();
 				if (cyNetwork != null && cyNetwork.getRow(cyNetwork).get(CyNetwork.SELECTED, Boolean.class)) {
 					if (type == CyIdType.NODE) {
 						cyNodes = CyTableUtil.getNodesInState(cyNetwork, CyNetwork.SELECTED, true);
 						if (! cyNodes.isEmpty()) {
 							setsMap.put(name, new Set<CyNode>(name,cyNodes));
 							networkSetNames.put(name, cyNetwork);
 							setType.put(name, type);
 							fireSetCreatedEvent(name);
 						}
 					}
 					else if (type == CyIdType.EDGE) {
 						cyEdges = CyTableUtil.getEdgesInState(cyNetwork, CyNetwork.SELECTED, true);
 						if (! cyEdges.isEmpty()) {
 							setsMap.put(name, new Set<CyEdge>(name,cyEdges));
 							networkSetNames.put(name, cyNetwork);
 							setType.put(name, type);
 							fireSetCreatedEvent(name);
 						}
 					}
 				}
 			}
 		}
 	/*	if (cyNodes != null && ! cyNodes.isEmpty())
 			setsMap.put(name, new Set<CyNode>(name,cyNodes));
 		else if (cyEdges != null && ! cyEdges.isEmpty())
 			setsMap.put(name, new Set<CyEdge>(name,cyEdges));
 		if ((cyNodes != null && ! cyNodes.isEmpty()) || (cyEdges != null && ! cyEdges.isEmpty())) {
 			networkSetNames.put(name, cyNetwork);
 			setType.put(name, type);
 			fireSetCreatedEvent(name);
 		} */
 	}
 	
 	public void rename(String name, String newName) {
 		if (setsMap.containsKey(name) && ! setsMap.containsKey(newName)) {
 			Set<? extends CyIdentifiable> oldSet = setsMap.get(name);
 			CyNetwork oldNetwork = networkSetNames.get(name);
 			CyIdType oldType = setType.get(name);
 			
 			setsMap.remove(name);
 			networkSetNames.remove(name);
 			setType.remove(name);
 			
 			setsMap.put(newName, oldSet);
 			networkSetNames.put(newName, oldNetwork);
 			setType.put(newName, oldType);
 			oldSet.rename(newName);
 			fireSetRenamedEvent(name, newName);
 		}
 	}
 	
 	public void removeSet(String name) {
 		setsMap.remove(name);
 		networkSetNames.remove(name);
 		setType.remove(name);
 		fireSetRemovedEvent(name);
 	}
 	
 	public boolean union(String newName, String set1, String set2) {
 		if (! setsMap.containsKey(newName) && networkSetNames.get(set1).getSUID() == networkSetNames.get(set2).getSUID()) {
 			setsMap.put(newName, setsMap.get(set1).unionGeneric(newName, setsMap.get(set2)));
 			networkSetNames.put(newName, networkSetNames.get(set1));
 			if (setType.get(set1) != null && setType.get(set2) != null && setType.get(set1) == setType.get(set2))
 				setType.put(newName, setType.get(set1));
 			fireSetCreatedEvent(newName);
 			return true;
 		}
 		else return false;
 	}
 	
 	public boolean intersection(String newName, String set1, String set2) {
 		if (! setsMap.containsKey(newName) && networkSetNames.get(set1).getSUID() == networkSetNames.get(set2).getSUID()) {
 			setsMap.put(newName, setsMap.get(set1).intersectionGeneric(newName, setsMap.get(set2)));
 			networkSetNames.put(newName, networkSetNames.get(set1));
 			if (setType.get(set1) != null && setType.get(set2) != null && setType.get(set1) == setType.get(set2))
 				setType.put(newName, setType.get(set1));
 			fireSetCreatedEvent(newName);
 			return true;
 		}
 		else return false;
 	}
 	
 	public boolean difference(String newName, String set1, String set2) {
 		if (! setsMap.containsKey(newName) && networkSetNames.get(set1).getSUID() == networkSetNames.get(set2).getSUID()) {
 			setsMap.put(newName, setsMap.get(set1).differenceGeneric(newName, setsMap.get(set2)));
 			networkSetNames.put(newName, networkSetNames.get(set1));
 			if (setType.get(set1) != null && setType.get(set2) != null && setType.get(set1) == setType.get(set2))
 				setType.put(newName, setType.get(set1));
 			fireSetCreatedEvent(newName);
 			return true;
 		}
 		else return false;
 	}
 	
 	public boolean addToSet(String name, CyIdentifiable cyId) {
 		Set<? extends CyIdentifiable> s = setsMap.get(name);
 		try {
 			if (networkSetNames.get(name).getRow(cyId) != null && s.addCyId(cyId)) {
 				ArrayList<CyIdentifiable> cyIdList = new ArrayList<CyIdentifiable>();
 				cyIdList.add(cyId);
 				fireSetAddedEvent(name, cyIdList);
 				return true;
 			}
 			else return false;
 		}
 		catch (Exception E) {
 			System.err.println("Unsupported operation: Trying to insert node into a different network.");
 		}
 		return false;
 	}
 	
 	public boolean isInSet(String name, Long cyId) {
 		Set<? extends CyIdentifiable> thisSet;
 		if ((thisSet = setsMap.get(name)) != null) return  thisSet.hasCyId(cyId);
 		else return false;
 	}
 	
 	public void removeFromSet(String name, CyIdentifiable cyId) {
 		Set<? extends CyIdentifiable> s = setsMap.get(name);
 		if (s.removeCyId(cyId)) {
 			ArrayList<CyIdentifiable> cyIdList = new ArrayList<CyIdentifiable>();
 			cyIdList.add(cyId);
 			fireSetRemovedEvent(name, cyIdList);
 		}
 	}
 	
 	public void exportSetToStream(String name, String column, BufferedWriter writer) {
 		Collection<? extends CyIdentifiable> cyIds = setsMap.get(name).getElements();
 		CyNetwork network = networkSetNames.get(name);
 		CyTable table = null;
 		if (setType.get(name) == CyIdType.NODE) table = network.getDefaultNodeTable();
 		if (setType.get(name) == CyIdType.EDGE) table = network.getDefaultEdgeTable();
 		if (table != null) {
 			try {
 				for (CyIdentifiable cyId: cyIds)
 					writer.write(table.getRow(cyId.getSUID()).get(column, String.class) + "\n");
 			} catch (IOException e) {
 				System.err.println("Cannot write to file: " + writer.toString());
 				e.printStackTrace();
 			}
 		}
 		try {
 			writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			System.err.println("Problems writing to stream: " + writer.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	public CyNetwork getCyNetwork(String name) {
 		return networkSetNames.get(name);
 	}
 	
 	public List<String> getSetNames() {
 		List<String> setNames = new ArrayList<String>();
 		java.util.Set<String> set = setsMap.keySet();
 		for (String s: set) {
 			setNames.add(s);
 		}
 		return setNames;
 	}
 	
 	public boolean isInSetsManager(String name) {
 		if (setsMap.get(name) == null)
 			return false;
 		else
 			return true;
 	}
 	
 	public CyIdType getType(String name) {
 		return setType.get(name);
 	}
 	
 	public void reset() {
 		this.setsMap = new ConcurrentHashMap<String, Set<? extends CyIdentifiable>> ();
 		this.networkSetNames = new ConcurrentHashMap<String, CyNetwork>();
 		this.setType = new ConcurrentHashMap<String, CyIdType>();
 	}
 	
 	public Set<? extends CyIdentifiable> getSet(String setName) {
 		return setsMap.get(setName);
 	}
 }
