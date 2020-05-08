 
 package haflow.service;
 
 import haflow.dto.entity.Edge;
 import haflow.dto.entity.Flow;
 import haflow.dto.entity.MainUser;
 import haflow.dto.entity.Node;
 import haflow.util.SessionHelper;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class FlowService {
 	private SessionHelper sessionHelper;
 
 	private SessionHelper getSessionHelper() {
 		return sessionHelper;
 	}
 
 	@Autowired
 	private void setSessionHelper(SessionHelper sessionHelper) {
 		this.sessionHelper = sessionHelper;
 	}
 
 	public boolean saveFlow(UUID flowId, String name, boolean isnode,String path,String parentpath,Set<Node> nodes,
 			Set<Edge> edges, int userid) {
 		Session session = this.getSessionHelper().openSession();
 		Transaction transaction = session.beginTransaction();
 		try {
 			MainUser user = (MainUser) session.get(MainUser.class, userid);
 			Flow flow = (Flow) session.get(Flow.class, flowId);
 			if (flow == null) {
 				flow = new Flow();
 				flow.setId(flowId);
 				flow.setName(name);
 				flow.setNode(isnode);
 				flow.setPath(path);
 				flow.setParentpath(parentpath);
 				flow.setNodes(new HashSet<Node>());
 				flow.setEdges(new HashSet<Edge>());
 				flow.setUser(user);
 			} 
 			
 			flow.setName(name);
 			flow.setNode(isnode);
 			flow.setPath(path);
 			flow.setParentpath(parentpath);
 			flow.getNodes().clear();
 			flow.getEdges().clear();
 			
 
 			for (Node node : nodes) {
 				node.setFlow(flow);
 				session.merge(node);
 				flow.getNodes().add(node);
 			}
 			for (Edge edge : edges) {
 				edge.setFlow(flow);
 				UUID sourceNodeId = edge.getSourceNode().getId();
 				UUID targetNodeId = edge.getTargetNode().getId();
 				for (Node node : nodes) {
 					if (node.getId().equals(sourceNodeId)) {
 						edge.setSourceNode(node);
 					}
 					if (node.getId().equals(targetNodeId)) {
 						edge.setTargetNode(node);
 					}
 				}
 				if (edge.getSourceNode() == null
 						|| edge.getTargetNode() == null) {
 					return false;
 				}
 				flow.getEdges().add(edge);
 			}
 			
 			session.merge(flow);
 			
 			transaction.commit();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			transaction.rollback();
 			return false;
 		} finally {
 			if (session != null)
 				session.close();
 		}
 	}
 
 	public boolean removeFlow(UUID flowId,int userid) {
 		Session session = this.getSessionHelper().openSession();
 		Transaction transaction = session.beginTransaction();
 		Flow flow=(Flow) session.get(Flow.class, flowId);
 		MainUser user=(MainUser) session.get(MainUser.class, userid);
 		if(flow==null) return false;
 		try {
 			for(Flow tmpFlow:user.getFlows()){
 				if(flow.getId().equals(tmpFlow.getId())){
 					user.getFlows().remove(tmpFlow);
 					flow.setUser(null);
 					session.delete(flow); 
 					transaction.commit();
 					session.close();
 					return true;
 				}
 			}
 			return false;
 		}catch (Exception e) {
 			e.printStackTrace();
 			transaction.rollback();
 			session.close();
 			return false;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Flow> getFlowList(int userid, String path) {
 		Session session = this.getSessionHelper().openSession();
 		try {
			Query query = session.createQuery("select f from Flow f where f.parentpath =? and f.user.id=?");
 			query.setString(0, path); 
			query.setInteger(1, userid);
 			List<Flow> flows = (List<Flow>) query.list();
 	        for(Flow flow : flows){
 	        	
	            System.out.println("database��"+flow.getId() + " : " + flow.getNode() + " : " + flow.getPath()+ " : " + flow.getParentpath());      
 	        } 
 			session.close();
 			return flows;
 		} catch (Exception e) {
 			e.printStackTrace();
 			session.close();
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void renameFlowPath(int userid,UUID flowId,String path,Session session)
 	{
 		Flow flowfolder = (Flow) session.get(Flow.class, flowId);
		//�޸�·��
 		String newpath=path+"/"+flowfolder.getName();
 		flowfolder.setPath(newpath);
 		session.update(flowfolder);
 		String parentpath=flowId.toString();
 		Query querychild= session.createQuery("select f from Flow f where f.parentpath =?");
 		querychild.setString(0, parentpath); 
 		List<Flow> flows = (List<Flow>)querychild.list();
         for(Flow flow : flows){	        	
            System.out.println("child��"+flow.getId() + " : " + flow.getNode() + " : " + flow.getPath()+ " : " + flow.getParentpath());         
             if(flow.getNode()==false)
             {
             	renameFlowPath(userid,flow.getId(),newpath,session);
             }
             
         } 
 	}
 	
 	@SuppressWarnings("unchecked")
 	public boolean renameFlowFolder(int userid,UUID flowId,String name) {
 		Session session = this.getSessionHelper().openSession();
 		Transaction trans=session.beginTransaction();
 		try {
			//�޸��ļ���
 			Flow flowfolder = (Flow) session.get(Flow.class, flowId);
 			flowfolder.setName(name);
			//�޸�·��
 			String []path=flowfolder.getPath().split("/");
 			String newpath="";
 		     for(int i=0;i<path.length-1;i++)
 		     {
 		      newpath+=path[i]+"/";
 		     }
 		    newpath+=name;
 			flowfolder.setPath(newpath);
 			session.update(flowfolder);
			//�������ļ�
 			String parentpath=flowId.toString();
 			Query querychild= session.createQuery("select f from Flow f where f.parentpath =?");
 			querychild.setString(0, parentpath); 
 			List<Flow> flows = (List<Flow>)querychild.list();
 	        for(Flow flow : flows){	        	
	            System.out.println("child��"+flow.getId() + " : " + flow.getNode() + " : " + flow.getPath()+ " : " + flow.getParentpath());
 	            renameFlowPath(userid,flow.getId(),newpath,session);	            
 	        } 
			//�ҵ�����д�ļ�·��
 	        trans.commit();
 			session.close();			
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			session.close();
 			return false;
 		}
 		
 	}
 	public Flow getFlow(UUID flowId,int userid) {
 		Session session = this.getSessionHelper().openSession();
 		try {
 			MainUser user = (MainUser) session.get(MainUser.class, userid);
 
 			Set<Flow> flows = (Set<Flow>) user.getFlows();
 			for (Flow flow : flows) {
 				if (flow.getId().equals(flowId)) {
 					return flow;
 				}
 			}
 			return null;
 			//Flow flow = (Flow) session.get(Flow.class, flowId);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			session.close();
 			return null;
 		}finally{
 			if(session!=null)
 				session.close();
 		}
 	}
 }
