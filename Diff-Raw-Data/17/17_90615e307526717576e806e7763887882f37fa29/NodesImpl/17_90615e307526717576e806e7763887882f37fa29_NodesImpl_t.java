 /*
  *  Copyright (C) 2012 Royal Dutch Army
  *  
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *  MA  02110-1301, USA.
  */
 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;
 
 import java.io.IOException;
 import java.io.OutputStream;
import java.io.Serializable;
 import java.net.InetAddress;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.constants.Constants;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 @Repository
 public class NodesImpl implements Nodes {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 
 	private SessionFactory sessionFactory;
 
 	/**
 	 * @param sessionFactory
 	 *          the sessionFactory to set
 	 */
 	@Required
 	public final void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	private TxChecker txChecker;
 
 	/**
 	 * @param txChecker
 	 *          the txChecker to set
 	 */
 	@Required
 	public final void setTxChecker(TxChecker txChecker) {
 		this.txChecker = txChecker;
 	}
 
 	private void addToCluster(List<Node> allNodes, Set<Node> clusterLeaders, List<Node> cluster, Node node, boolean up) {
 		assert (allNodes != null);
 		assert (cluster != null);
 		assert (node != null);
 		assert (!cluster.contains(node));
 
 		cluster.add(node);
 		allNodes.remove(node);
 
 		/* search down */
 		Set<ClusterLeaderMsg> clusterNodeMsgs = node.getClusterNodes();
 		if (clusterNodeMsgs.size() > 0) {
 			clusterLeaders.add(node);
 
 			for (ClusterLeaderMsg clusterNodeMsg : clusterNodeMsgs) {
 				Node clusterNode = clusterNodeMsg.getNode();
 				if ((node != clusterNode) && !cluster.contains(clusterNode)) {
 					addToCluster(allNodes, clusterLeaders, cluster, clusterNode, false);
 				}
 			}
 		}
 
 		/* search up */
 		if (up) {
 			ClusterLeaderMsg clusterLeaderMsg = node.getClusterLeaderMsg();
 			if (clusterLeaderMsg != null) {
 				Node clusterLeaderNode = clusterLeaderMsg.getClusterLeaderNode();
 				if ((node != clusterLeaderNode) && !clusterLeaders.contains(clusterLeaderNode)) {
 					addToCluster(allNodes, clusterLeaders, cluster, clusterLeaderNode, true);
 				}
 			}
 		}
 	}
 
	protected static class NodeComparatorOnClusterNodes_ReceptionTime_MainIP implements Comparator<Node>, Serializable {
		private static final long serialVersionUID = 8059264913324031432L;

 		private static  int compareInetAddresses(InetAddress ip1, InetAddress ip2) {
 			byte[] ip1a = ip1.getAddress();
 			byte[] ip2a = ip2.getAddress();
 
 			/* IPv4 before IPv6 */
 			if (ip1a.length < ip2a.length) {
 				return -1;
 			}
 			if (ip1a.length > ip2a.length) {
 				return 1;
 			}
 
 			/* 2 IPs of the same type, so we have to compare each byte */
 			for (int i = 0; i < ip1a.length; i++) {
 				int b1 = ip1a[i] & 0xff;
 				int b2 = ip2a[i] & 0xff;
 				if (b1 == b2) {
 					continue;
 				}
 
 				if (b1 < b2) {
 					return -1;
 				}
 
 				return 1;
 			}
 
 			return 0;
 		}
 
 		@Override
 		public int compare(Node o1, Node o2) {
 			/* first sort on the number of cluster nodes (cluster leaders first) */
 			int o1ClusterNodesCount = o1.getClusterNodes().size();
 			int o2ClusterNodesCount = o2.getClusterNodes().size();
 			if (o1ClusterNodesCount > o2ClusterNodesCount) {
 				return -1;
 			}
 			if (o1ClusterNodesCount < o2ClusterNodesCount) {
 				return 1;
 			}
 
 			/* then sort on the reception time of the cluster leader message (recently seen nodes first) */
 			ClusterLeaderMsg o1ClMsg = o1.getClusterLeaderMsg();
 			ClusterLeaderMsg o2ClMsg = o2.getClusterLeaderMsg();
 			long o1ReceptionTime = (o1ClMsg == null) ? 0 : o1ClMsg.getReceptionTime();
 			long o2ReceptionTime = (o2ClMsg == null) ? 0 : o2ClMsg.getReceptionTime();
 			if (o1ReceptionTime > o2ReceptionTime) {
 				return -1;
 			}
 			if (o1ReceptionTime < o2ReceptionTime) {
 				return 1;
 			}
 
 			/* as a final discriminator sort on the mainIP of the node */
 			return compareInetAddresses(o1.getMainIp(), o2.getMainIp());
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	@SuppressWarnings("unchecked")
 	public List<List<Node>> getClusters(RelayServer relayServer) {
 		try {
 			this.txChecker.checkInTx("Nodes::getClusters");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		List<Node> allNodes = null;
 		if (relayServer == null) {
 			allNodes = this.sessionFactory.getCurrentSession()
 					.createQuery("select node from Node node order by size(node.clusterNodes) desc").list();
 		} else {
 			allNodes = this.sessionFactory
 					.getCurrentSession()
 					.createQuery(
 							"select node from Node node where node.sender is not null and node.sender.relayServer.id = :rsId "
 									+ "order by size(node.clusterNodes) desc").setLong("rsId", relayServer.getId().longValue()).list();
 		}
 
 		if (allNodes.size() == 0) {
 			return null;
 		}
 
 		List<List<Node>> clusters = new LinkedList<List<Node>>();
 
 		while (!allNodes.isEmpty()) {
 			LinkedList<Node> cluster = new LinkedList<Node>();
 			Set<Node> clusterLeaders = new HashSet<Node>();
 			addToCluster(allNodes, clusterLeaders, cluster, allNodes.get(0), true);
 			if (cluster.size() > 0) {
 				Collections.sort(cluster, new NodeComparatorOnClusterNodes_ReceptionTime_MainIP());
 				clusters.add(cluster);
 			}
 		}
 
 		if (clusters.size() > 0) {
 			return clusters;
 		}
 
 		return null;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public Node getNode(InetAddress mainIp) {
 		try {
 			this.txChecker.checkInTx("Nodes::getNode");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		if (mainIp == null) {
 			return null;
 		}
 
 		@SuppressWarnings("unchecked")
 		List<Node> result = this.sessionFactory.getCurrentSession()
 				.createQuery("select node from Node node where node.mainIp = :ip").setParameter("ip", mainIp).list();
 
 		if (result.size() == 0) {
 			return null;
 		}
 
 		assert (result.size() == 1);
 
 		return result.get(0);
 	}
 
 	@Override
 	@Transactional
 	public void saveNode(Node node) {
 		try {
 			this.txChecker.checkInTx("Nodes::saveNode");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		this.sessionFactory.getCurrentSession().saveOrUpdate(node);
 	}
 
 	@Override
 	@Transactional
 	public boolean removeExpiredNodes() {
 		try {
 			this.txChecker.checkInTx("Nodes::removeExpiredNodes");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		@SuppressWarnings("unchecked")
 		List<Node> result = this.sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"select node from Node node where positionUpdateMsg is null and clusterLeaderMsg is null and size(clusterNodes) = 0")
 				.list();
 
 		if (result.size() == 0) {
 			return false;
 		}
 
 		for (Node node : result) {
 			node.setSender(null);
 			this.sessionFactory.getCurrentSession().delete(node);
 		}
 
 		if (this.logger.isDebugEnabled()) {
 			this.logger.debug("removed " + result.size() + " Node objects");
 		}
 
 		return true;
 	}
 
 	private String getNodesDump() {
 		@SuppressWarnings("unchecked")
 		List<Node> result = this.sessionFactory.getCurrentSession().createQuery("from Node node").list();
 
 		StringBuilder s = new StringBuilder();
 		s.append("[Nodes]\n");
 		for (Node node : result) {
 			s.append(node.toString() + "\n");
 		}
 
 		return s.toString();
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public void log(Logger log, Level level) {
 		try {
 			this.txChecker.checkInTx("Nodes::log");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		if (log.isEnabledFor(level)) {
 			log.log(level, getNodesDump());
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public void print(OutputStream out) throws IOException {
 		try {
 			this.txChecker.checkInTx("Nodes::print");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		String s = getNodesDump();
 		out.write(s.getBytes(Constants.CHARSET_DEFAULT), 0, s.length());
 	}
 }
