 /*
  * Copyright (c) 2012-2013 Open Source Community - <http://www.peerfact.org>
  * Copyright (c) 2011-2012 University of Paderborn - UPB
  * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
  *
  * This file is part of PeerfactSim.KOM.
  * 
  * PeerfactSim.KOM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * PeerfactSim.KOM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.peerfact.impl.overlay.unstructured.zeroaccess.analyzer;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.peerfact.api.common.Host;
 import org.peerfact.impl.analyzer.AbstractFileStringAnalyzer;
 import org.peerfact.impl.overlay.AbstractOverlayNode.PeerStatus;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.components.ZeroAccessBotmasterOverlayNode;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.components.ZeroAccessOverlayID;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.components.ZeroAccessOverlayNode;
 import org.peerfact.impl.simengine.Simulator;
 import org.peerfact.impl.util.oracle.GlobalOracle;
 
 /**
  * Analyzer to regularly check the structure of the Chord ring
  * 
  * @author Julius Rueckert <peerfact@kom.tu-darmstadt.de>
  * @version 05/06/2011
  */
 public class ZeroAccessBotSoftwareUpdatesAnalyzer extends
 		AbstractFileStringAnalyzer {
 
 	final public Logger log = Logger
 			.getLogger(ZeroAccessBotSoftwareUpdatesAnalyzer.class);
 
 	public ZeroAccessBotSoftwareUpdatesAnalyzer() {
 		setFlushEveryLine(true);
 		setOutputFileName("ZeroAccessBotSoftwareVersion");
 	}
 
 	@Override
 	protected List<String> generateHeadlineForMetrics() {
 		List<String> fieldNames = new LinkedList<String>();
 		fieldNames.add("live peers");
 		fieldNames.add("updated peers");
 		fieldNames.add("current software version");
 		fieldNames.add("average software version");
 		return fieldNames;
 	}
 
 	@Override
 	protected void resetEvaluationMetrics() {
 		// nothing to do
 	}
 
 	@Override
 	protected List<String> generateEvaluationMetrics(long currentTime) {
 
 		List<String> measurements = new LinkedList<String>();
 
 		long current_software_id = get_current_software_id();
 
 		Map<ZeroAccessOverlayID, ZeroAccessOverlayNode> nodes_map = getAllPresentZeroAccessNodes();
 
 		double software_version_sum = 0;
 		long live_count = 0;
 		long software_updated_nodes_size = 0;
 		for (ZeroAccessOverlayID id : nodes_map.keySet())
 		{
 			ZeroAccessOverlayNode node = nodes_map.get(id);
 			if (node.isPresent()) {
				long node_software_version = node.getBot_software_version();
				if (current_software_id == node_software_version)
				{
					software_updated_nodes_size++;
				}
 				software_version_sum += node_software_version;
 				live_count += 1;
 			}
 		}
 
 		measurements.add(Long.valueOf(live_count).toString());
 		measurements.add(Long.valueOf(software_updated_nodes_size)
 				.toString());
 		measurements.add(Long.valueOf(current_software_id)
 				.toString());
		if (live_count == 0)
		{
			measurements.add(Double.valueOf(0)
					.toString());
		} else {
			measurements.add(Double.valueOf(software_version_sum / live_count)
					.toString());
		}
 		return measurements;
 	}
 
 	private static Map<ZeroAccessOverlayID, ZeroAccessOverlayNode> getAllPresentZeroAccessNodes() {
 		List<Host> hosts = GlobalOracle.getHosts();
 		LinkedHashMap<ZeroAccessOverlayID, ZeroAccessOverlayNode> nodes = new LinkedHashMap<ZeroAccessOverlayID, ZeroAccessOverlayNode>();
 
 		for (Host host : hosts) {
 			ZeroAccessOverlayNode olNode = (ZeroAccessOverlayNode) host
 					.getOverlay(ZeroAccessOverlayNode.class);
 			if (olNode != null && olNode instanceof ZeroAccessOverlayNode) {
 				ZeroAccessOverlayNode cNode = olNode;
 
 				if (cNode.getPeerStatus() == PeerStatus.PRESENT) {
 					nodes.put(cNode.getOverlayID(), cNode);
 				}
 			}
 		}
 		return nodes;
 	}
 
 	private static int getNumOfLiveNodes() {
 		List<Host> hosts = GlobalOracle.getHosts();
 		int numOfLiveNodes = 0;
 
 		for (Host host : hosts) {
 			ZeroAccessOverlayNode olNode = (ZeroAccessOverlayNode) host
 					.getOverlay(ZeroAccessOverlayNode.class);
 			if (olNode != null && olNode instanceof ZeroAccessOverlayNode) {
 				ZeroAccessOverlayNode cNode = olNode;
 
 				if (cNode.getPeerStatus() == PeerStatus.PRESENT) {
 					numOfLiveNodes++;
 				}
 			}
 		}
 		return numOfLiveNodes;
 	}
 
 	private long get_current_software_id() {
 		long current_bot_software_id = 0;
 		List<Host> hosts = GlobalOracle.getHosts();
 		for (Host host : hosts) {
 			ZeroAccessBotmasterOverlayNode olNode = (ZeroAccessBotmasterOverlayNode) host
 					.getOverlay(ZeroAccessBotmasterOverlayNode.class);
 			if (olNode != null
 					&& olNode instanceof ZeroAccessBotmasterOverlayNode) {
 				ZeroAccessBotmasterOverlayNode cNode = olNode;
 				if (cNode.getPeerStatus() != PeerStatus.ABSENT)
 				{
 					log.warn(Simulator.getSimulatedRealtime()
 							+ " : God damm wrong, God is dead");
 				}
 				current_bot_software_id = cNode.getBot_software_version();
 			}
 		}
 		return current_bot_software_id;
 	}
 
 	private static int getNumOfNodesSuccesfullyUpdated() {
 		List<Host> hosts = GlobalOracle.getHosts();
 
 		int numOfNodesUpdated = 0;
 		long current_bot_software_id = 0;
 
 		for (Host host : hosts) {
 			ZeroAccessBotmasterOverlayNode olNode = (ZeroAccessBotmasterOverlayNode) host
 					.getOverlay(ZeroAccessBotmasterOverlayNode.class);
 			if (olNode != null
 					&& olNode instanceof ZeroAccessBotmasterOverlayNode) {
 				ZeroAccessBotmasterOverlayNode cNode = olNode;
 				current_bot_software_id = cNode.getBot_software_version();
 			}
 		}
 
 		for (Host host : hosts) {
 			ZeroAccessOverlayNode olNode = (ZeroAccessOverlayNode) host
 					.getOverlay(ZeroAccessOverlayNode.class);
 			if (olNode != null && olNode instanceof ZeroAccessOverlayNode) {
 				ZeroAccessOverlayNode cNode = olNode;
 
 				if (cNode.getPeerStatus() == PeerStatus.PRESENT) {
 					if (cNode.getBot_software_version() == current_bot_software_id)
 					{
 						numOfNodesUpdated++;
 					}
 					else if (cNode.getBot_software_version() < current_bot_software_id)
 					{
 						System.out
 								.println("fata error, bot software version invalid, exceeding largest possible id");
 					}
 				}
 			}
 		}
 		return numOfNodesUpdated;
 	}
 }
