 package org.devilry.core.dao;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.ejb.SessionContext;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptors;
 import javax.interceptor.InvocationContext;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 import org.devilry.core.NodePath;
 import org.devilry.core.daointerfaces.CourseNodeCommon;
 import org.devilry.core.daointerfaces.CourseNodeLocal;
 import org.devilry.core.daointerfaces.NodeCommon;
 import org.devilry.core.daointerfaces.NodeLocal;
 import org.devilry.core.daointerfaces.NodeRemote;
 import org.devilry.core.daointerfaces.PeriodNodeLocal;
 import org.devilry.core.entity.BaseNode;
 import org.devilry.core.entity.CourseNode;
 import org.devilry.core.entity.Node;
 import org.devilry.core.entity.PeriodNode;
 
 @Stateless
 @Interceptors( { AuthorizationInterceptor.class })
 public class NodeImpl extends BaseNodeImpl implements NodeRemote, NodeLocal {
 	
 	@EJB(beanInterface=CourseNodeLocal.class) 
 	private CourseNodeCommon courseBean;
 
 	@EJB(beanInterface=NodeLocal.class) 
 	private NodeCommon nodeBean;
 
 	private Node getNode(long nodeId) {
 		return getNode(Node.class, nodeId);
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long create(String name, String displayName) {
 		if (getIdFromPath(new NodePath(new String[]{name})) != -1) {
 			throw new RuntimeException(
 					"Node name must be unique on toplevel nodes.");
 		}
 		Node node = new Node();
 		node.setName(name.toLowerCase());
 		node.setDisplayName(displayName);
 		em.persist(node);
 		em.flush();
 		return node.getId();
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long create(String name, String displayName, long parentId) {
 		Node node = new Node();
 		node.setName(name.toLowerCase());
 		node.setDisplayName(displayName);
 		node.setParent(getNode(parentId));
 		em.persist(node);
 		em.flush();
 		return node.getId();
 	}
 
 	public long getParentNode(long nodeId) {
 		return getNode(nodeId).getParent().getId();
 	}
 
 	public List<Long> getToplevelNodes() {
 		Query q = em
 				.createQuery("SELECT n.id FROM Node n WHERE n.parent IS NULL");
 		return q.getResultList();
 	}
 
 
 	public List<Long> getChildnodes(long nodeId) {
 		Query q = em
 				.createQuery("SELECT n.id FROM Node n WHERE n.parent IS NOT NULL AND n.parent.id = :parentId");
 		q.setParameter("parentId", nodeId);
 		return q.getResultList();
 	}
 
 	public List<Long> getChildcourses(long nodeId) {
 		Query q = em
 				.createQuery("SELECT c.id FROM CourseNode c WHERE c.parent.id = :parentId");
 		q.setParameter("parentId", nodeId);
 		return q.getResultList();
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void remove(long nodeId) {
 		// Remove childnodes
 		List<Long> childNodes = getChildnodes(nodeId);
 		for (Long childNodeId : childNodes) {
 			remove(childNodeId);
 		}
 
 		// Remove child-courses
 		List<Long> childCourses = getChildcourses(nodeId);
 		for (Long courseId : childCourses) {
 			courseBean.remove(courseId);
 		}
 	
 		// Remove *this* node
 		removeNode(nodeId, Node.class);
 	}
 
 	
 	
 	public NodePath getPath(long nodeId) {
 		
 		Node node = getNode(nodeId);
 		String nodeName = node.getName();
 		
 		NodePath path;
 		
 		// If node has parent node
 		if (node.getParent() != null) {
 			// Get path from parent node
 			path = nodeBean.getPath(node.getParent().getId());
 			// Add current node name to path
 			path.addToEnd(nodeName);
 		}
 		else {
 			path = new NodePath(new String[]{nodeName});
 		}
 				
 		return path;
 	}
 		
 	
 	/*
 	public long getIdFromPath(NodePath nodePath, long parentNodeId) {
 		
 		if (nodePath.size() == 1) {
 			return getNodeId(nodePath.get(0), parentNodeId);
 		}
 		else {
 		
 			long parentId = parentNodeId;
 			
 			for (int i = 0; i < nodePath.size(); i++) {
 				
 				long nodeId = getNodeId(nodePath.get(i), parentId);
 				
 				// If valid node id
 				if (nodeId != -1) {
 					parentId = nodeId;
 				}// If invalid node id, try course
 				else {
 					nodePath.removeFirst();					
 					return courseBean.getIdFromPath(nodePath, parentId); 
 				}
 			}
 			
 			// If no more values in nodePath - return the id of the last node (not course, period or assignment)
 			return parentId;
 		}
 	}
 	*/
 
 	public long getIdFromPath(NodePath nodePath) {
		
 		NodePath pathCopy = new NodePath(nodePath);
		
 		String nodeName = pathCopy.removeLastPathComponent();
		long parentNodeId = nodeBean.getIdFromPath(nodePath);
 		long nodeId = getNodeId(nodeName, parentNodeId);
 		
 		return nodeId;
 	}	
 		
 	
 	private long getNodeId(String name, long parentId) {
 		Query q;
 
 		if (parentId != -1) {
 			q = em.createQuery("SELECT n FROM Node n WHERE n.name=:name AND n.parent IS NOT NULL AND n.parent.id=:parentId");
 			q.setParameter("name", name);
 			q.setParameter("parentId", parentId);
 		} else {
 			q = em.createQuery("SELECT n FROM Node n WHERE n.name=:name AND n.parent IS NULL");
 			q.setParameter("name", name);
 		}
 
 		Node node;
 
 		try {
 			node = (Node) q.getSingleResult();
 		} catch (NoResultException e) {
 			node = null;
 		}
 
 		return node == null ? -1 : node.getId();
 	}
 
 	
 	private long getNodeId(String name, String parent) {
 		Query q;
 
 		if (parent != null) {
 			q = em.createQuery("SELECT n FROM Node n WHERE n.name=:name AND n.parent IS NOT NULL AND n.parent.name=:parent");
 			q.setParameter("name", name);
 			q.setParameter("parent", parent);
 		} else {
 			q = em.createQuery("SELECT n FROM Node n WHERE n.name=:name AND n.parent IS NULL");
 			q.setParameter("name", name);
 		}
 
 		Node node;
 
 		try {
 			node = (Node) q.getSingleResult();
 		} catch (NoResultException e) {
 			node = null;
 		}
 
 		return node == null ? -1 : node.getId();
 	}
 
 	public List<Long> getNodesWhereIsAdmin() {
 		return getNodesWhereIsAdmin(Node.class);
 	}
 	
 	public boolean isNodeAdmin(long nodeId, long userId) {
 		Node node = getNode(nodeId);
 		return isAdmin(node, userId);
 	}
 	
 	public void addNodeAdmin(long nodeId, long userId) {
 		Node node = getNode(nodeId);
 		addAdmin(node, userId);
 	}
 
 	public void removeNodeAdmin(long nodeId, long userId) {
 		Node node = getNode(nodeId);
 		removeAdmin(node, userId);
 	}
 	
 	public List<Long> getNodeAdmins(long nodeId) {
 		Node node = getNode(nodeId);
 		return getAdmins(node);
 	}
 
 }
