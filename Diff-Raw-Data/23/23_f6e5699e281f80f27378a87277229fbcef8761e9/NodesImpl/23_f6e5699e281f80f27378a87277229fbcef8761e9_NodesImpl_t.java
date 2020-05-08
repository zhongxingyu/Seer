 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.util.List;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
 
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
 
 	private boolean clusterLeadersIncludesTransitionalNodes = false;
 
 	/**
 	 * @param clusterLeadersIncludesTransitionalNodes
 	 *          the clusterLeadersIncludesTransitionalNodes to set
 	 */
 	@Required
 	public final void setClusterLeadersIncludesTransitionalNodes(boolean clusterLeadersIncludesTransitionalNodes) {
 		this.clusterLeadersIncludesTransitionalNodes = clusterLeadersIncludesTransitionalNodes;
 	}
 
 	@Override
 	@Transactional
 	public Node getNode(InetAddress mainIp) {
 		if (mainIp == null) {
 			return null;
 		}
 
 		@SuppressWarnings("unchecked")
 		List<Node> result = sessionFactory.getCurrentSession()
 				.createQuery("select node from Node node where node.mainIp = :ip").setParameter("ip", mainIp).list();
 
 		if (result.size() == 0) {
 			return null;
 		}
 
 		assert (result.size() == 1);
 
 		return result.get(0);
 	}
 
 	@Override
 	@Transactional
 	public List<Node> getClusterLeaders() {
 		@SuppressWarnings("unchecked")
 		List<Node> result = sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"select node from Node node"
 								/* do an eager fetch of the gateway */
								+ " left join node.gateway"
								/* node has cluster nodes AND node is not a cluster leader that doesn't point to itself */
								+ " where (node.clusterNodes is not empty and node not in (select cl.clusterLeaderNode from ClusterLeaderMsg cl where cl.clusterLeaderNode.id != cl.clusterLeaderNode.clusterLeaderMsg.clusterLeaderNode.id))"

 								/*
								 * (when clusterLeadersIncludesTransitionalNodes is set): or node is a node that points to a cluster
								 * leader that doesn't point to itself
 								 */
								+ (!clusterLeadersIncludesTransitionalNodes ? ""
										: " or (node in (select cl.node from ClusterLeaderMsg cl where cl.clusterLeaderNode.id != cl.clusterLeaderNode.clusterLeaderMsg.clusterLeaderNode.id))")

								+ " order by node.mainIp").list();
 		if (result.size() == 0) {
 			return null;
 		}
 
 		return result;
 	}
 
 	@Override
 	@Transactional
 	public Node getSubstituteClusterLeader(Node clusterLeader) {
 		assert (clusterLeader != null);
 		Long clId = clusterLeader.getId();
 
 		@SuppressWarnings("unchecked")
 		List<Node> result = sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"select node from Node node"
 						/* do an eager fetch of the gateway */
 						+ " left join node.gateway where"
 						/* node is not the cluster leader itself and node points to cluster leader */
 						+ " node.id != " + clId
 								+ " and node.clusterLeaderMsg is not null and node.clusterLeaderMsg.clusterLeaderNode.id = " + clId
								/* node has a valid gateway (a gateway always has a valid IP address and a valid port) */
 								+ " and node.gateway is not null"
 								/* keep the node with the most recently received cluster leader message on top of the list */
 								+ " order by node.clusterLeaderMsg.receptionTime desc").list();
 
 		if (result.size() == 0) {
 			return null;
 		}
 
 		return result.get(0);
 	}
 
 	@Override
 	@Transactional
 	public void saveNode(Node node, boolean newObject) {
 		// FIXME try with only the saveOrUpdate method
 		if (newObject) {
 			sessionFactory.getCurrentSession().saveOrUpdate(node);
 		} else {
 			sessionFactory.getCurrentSession().merge(node);
 		}
 	}
 
 	@Override
 	@Transactional
 	public boolean removeExpiredNodes() {
 		@SuppressWarnings("unchecked")
 		List<Node> result = sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"select node from Node node where positionUpdateMsg is null and clusterLeaderMsg is null"
 								+ " and size(clusterNodes) = 0").list();
 
 		if (result.size() == 0) {
 			return false;
 		}
 
 		for (Node node : result) {
 			node.setGateway(null);
 			sessionFactory.getCurrentSession().delete(node);
 		}
 
 		if (logger.isDebugEnabled()) {
 			logger.debug("removed " + result.size() + " Node objects");
 		}
 
 		return true;
 	}
 
 	private String getNodesDump() {
 		@SuppressWarnings("unchecked")
 		List<Node> result = sessionFactory.getCurrentSession().createQuery("from Node node").list();
 
 		StringBuilder s = new StringBuilder();
 		s.append("[Nodes]\n");
 		for (Node node : result) {
 			s.append(node.toString() + "\n");
 		}
 
 		return s.toString();
 	}
 
 	@Override
 	@Transactional
 	public void log(Logger logger, Level level) {
 		if (logger.isEnabledFor(level)) {
 			logger.log(level, getNodesDump());
 		}
 	}
 
 	@Override
 	@Transactional
 	public void print(OutputStream out) throws IOException {
 		String s = getNodesDump();
 		out.write(s.getBytes(), 0, s.length());
 	}
 }
