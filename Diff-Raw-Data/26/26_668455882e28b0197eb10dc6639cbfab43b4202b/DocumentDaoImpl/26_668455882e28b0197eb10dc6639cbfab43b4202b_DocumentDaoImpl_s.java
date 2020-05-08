 package com.duggan.workflow.server.dao;
 
 import java.math.BigInteger;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.jbpm.task.Task;
 import org.jbpm.task.query.TaskSummary;
 
 import com.duggan.workflow.client.model.TaskType;
 import com.duggan.workflow.server.dao.model.ADDocType;
 import com.duggan.workflow.server.dao.model.DetailModel;
 import com.duggan.workflow.server.dao.model.DocumentModel;
 import com.duggan.workflow.server.dao.model.TaskDelegation;
 import com.duggan.workflow.server.helper.auth.LoginHelper;
 import com.duggan.workflow.server.helper.session.SessionHelper;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentType;
 import com.duggan.workflow.shared.model.SearchFilter;
 import com.duggan.workflow.shared.model.UserGroup;
 
 /**
  * 
  * @author duggan
  *
  */
 public class DocumentDaoImpl extends BaseDaoImpl{
 	
 	public DocumentDaoImpl(EntityManager em){
 		super(em);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<DocumentModel> getAllDocuments(DocStatus status){
 		
 		return em.createQuery("FROM DocumentModel d where status=:status and createdBy=:createdBy and isActive=:isActive").
 				setParameter("status", status).
 				setParameter("createdBy", SessionHelper.getCurrentUser().getUserId()).
 				setParameter("isActive", 1).
 				getResultList();
 	}
 	
 	public DocumentModel getById(Long id){
 		List lst = em.createQuery("FROM DocumentModel d where id= :id").setParameter("id", id).getResultList();
 		
 		if(lst.size()>0){
 			return (DocumentModel)lst.get(0);
 		}
 		
 		return null;
 	}
 	
 	public DocumentModel saveDocument(DocumentModel document){
 		
 		if(document.getId()==null){
 			document.setCreated(new Date());
 			if(document.getStatus()==null){
 				document.setStatus(DocStatus.DRAFTED);
 			}
 			document.setCreatedBy(SessionHelper.getCurrentUser().getUserId());
 		}else{
 			document.setUpdated(new Date());
 			document.setUpdatedBy(SessionHelper.getCurrentUser().getUserId());
 		}
 		
 		em.persist(document);
 		
 		/*
 		 * Do not flush - This reflects data in the database immediately and the BTM transaction 
 		 * in my tests so far cannot rollback the flushed data - You can actually query the new values 
 		 * directly from the database even as the transaction is ongoing - Could it be the isolation
 		 * level being used in the db?
 		 * 
 		 */
 		//em.flush();
 		return document;
 	}
 	
 	public void delete(DocumentModel document){
 		
 		em.remove(document);
 		
 	}
 
 	public Integer count(DocStatus status) {
 
 		 Long value = (Long)em.createQuery("select count(d) FROM DocumentModel d where status=:status and createdBy=:createdBy  and isActive=:isActive").
 				setParameter("status", status).
 				setParameter("createdBy", SessionHelper.getCurrentUser().getUserId())
 				.setParameter("isActive", 1)
 				.getSingleResult();
 		 
 		 return value.intValue();
 	}
 
 	public List<DocumentModel> search(String subject) {
 		List lst = em.createQuery("FROM DocumentModel d where subject like :subject  and isActive=:isActive")
 				.setParameter("subject", "%"+subject+"%")
 				.setParameter("isActive", 1)
 				.getResultList();
 				
 		return lst;
 	}
 
 	public Long getProcessInstanceIdByDocumentId(Long documentId) {
 		Object processInstanceId = em.createQuery("select n.processInstanceId FROM DocumentModel n" +
 				" where n.id=:documentId")
 				.setParameter("documentId", documentId)
 				.getSingleResult();
 	
 		return processInstanceId==null? null : (Long)processInstanceId;
 	}
 	
 	public DocumentModel getDocumentByProcessInstanceId(Long processInstanceId){
 		List lst = em.createQuery("FROM DocumentModel d where processInstanceId= :processInstanceId " +
 				"and createdBy=:createdBy  and isActive=:isActive")
 				.setParameter("processInstanceId", processInstanceId)
 				.setParameter("createdBy", SessionHelper.getCurrentUser().getUserId())
 				.setParameter("isActive", 1)
 				.getResultList();
 		
 		if(lst.size()>0){
 			return (DocumentModel)lst.get(0);
 		}
 		
 		return null;
 	}
 	
 	@SuppressWarnings("unused")
 	public List<DocumentModel> search(String userId,SearchFilter filter){
 		
 		String subject=filter.getSubject();
 		Date startDate= filter.getStartDate();
 		Date endDate = filter.getEndDate();
 		Integer priority=filter.getPriority();
 		String phrase=filter.getPhrase();
 		DocumentType docTypeIn=filter.getDocType();
 		
 		ADDocType docType = null;
 		if(docTypeIn!=null){
 			docType = getDocumentTypeById(docTypeIn.getId());
 		}
 		
 		Boolean hasAttachment=filter.hasAttachment();
 
 		Map<String, Object> params = new HashMap<>();
 		
 		StringBuffer query = new StringBuffer("FROM DocumentModel d where ");
 			
 		boolean isFirst=true;
 		if(subject!=null){
 			isFirst=false;
 			query.append("(Lower(subject) like :subject");
 			params.put("subject", "%"+subject.toLowerCase()+"%");
 			
 			if(phrase==null){
 				query.append(" or Lower(description) like :phrase");
 				params.put("phrase", "%"+subject.toLowerCase()+"%");
 			}
 			query.append(")");
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(startDate!=null && endDate!=null){
 			isFirst=false;
 			query.append("created>:startDate " +
 					"and " +
 					"created<:endDate");
 			
 			params.put("startDate", startDate);
 			params.put("endDate", endDate);
 		}else if(startDate!=null){
 			isFirst=false;
 			//mysql functions - They wont work on postgres
 			query.append("STR_TO_DATE(DATE_FORMAT(created, '%d/%m/%y'), '%d/%m/%y')=:startDate");
 			params.put("startDate", startDate);
 			
 		}else if(endDate!=null){
 			isFirst=false;
 			//mysql functions - They wont work on postgres
 			query.append("STR_TO_DATE(DATE_FORMAT(created, '%d/%m/%y'), '%d/%m/%y')=:endDate");
 			params.put("endDate", endDate);
 			
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(priority!=null){
 			isFirst=false;
 			query.append("priority=:priority");
 			params.put("priority", priority);
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(phrase!=null){
 			isFirst=false;
 			query.append("Lower(description) like :phrase");
 			params.put("phrase", "%"+phrase.toLowerCase()+"%");
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(docType!=null){
 			isFirst=false;
 			query.append("type=:docType");
 			params.put("docType", docType);
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		{
 			isFirst=false;
 			query.append("createdBy=:createdBy");
 			//params.put("createdBy", SessionHelper.getCurrentUser().getUserId());
 			params.put("createdBy", userId);
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		{
 			isFirst=false;
 			query.append("isActive=:isActive");
 			//params.put("createdBy", SessionHelper.getCurrentUser().getUserId());
 			params.put("isActive", 1);
 		}
 		 
 				 
 		if(isFirst){
 			//we ended up with an and at the end
 			query = new StringBuffer(query.subSequence(0, query.length()-5).toString());
 		}
 		
 		Query hquery = em.createQuery(query.toString());
 		Set<String> keys = params.keySet();
 		
 		for(String key: keys){
 			hquery.setParameter(key, params.get(key));
 		}
 		
 		List list = hquery.getResultList();
 		
 		return list;
 	}
 	
 	public List<TaskSummary> searchTasks(String userId,SearchFilter filter){
 		
 		String subject=filter.getSubject();
 		Date startDate= filter.getStartDate();
 		Date endDate = filter.getEndDate();
 		Integer priority=filter.getPriority();
 		String phrase=filter.getPhrase();
 		DocumentType docTypeIn=filter.getDocType();
 		
 		ADDocType docType = null;
 		if(docTypeIn!=null){
 			docType = getDocumentTypeById(docTypeIn.getId());
 		}
 		
 		Boolean hasAttachment=filter.hasAttachment();
 		
 		List<UserGroup> groups = LoginHelper.getHelper().getGroupsForUser(userId);
 		String groupsIds="";
 		for(UserGroup group: groups){
 			groupsIds = groupsIds.concat(group.getName()+",");
 		}
 		
 		if(groupsIds.isEmpty()){
 			return new ArrayList<>();
 		}
 		groupsIds = groupsIds.substring(0, groupsIds.length()-1);
 		
 		
 		List<Object> params = new ArrayList<>();
 		
 		StringBuffer query = new StringBuffer("select t.id "+
 		//"d.id documentId "+
 		"from localdocument d "+
 		"inner join Task t on (t.processInstanceId=d.processInstanceId) "+
 		"left join OrganizationalEntity owner on (owner.id= t.actualOwner_id and owner.DTYPE='User') "+ 
 		"left join PeopleAssignments_PotOwners potowners on (potowners.task_id=t.id)  "+
 		"where "+
 		"t.archived = 0 and "+ 
 		"(owner.id = ? "+
 		 "or "+
 		"( potowners.entity_id = ? or potowners.entity_id in (?) )) " +
 		"and " +
 		"t.expirationTime is null "
 		 );
 		
 		params.add(userId);
 		params.add(userId);
 		params.add(groupsIds);
 		
 		boolean isFirst=false;
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(subject!=null){
 			isFirst=false;
 			/**
 			 * TODO:Necessary Table index has to be added; otherwise full table scan will be used 
 			 */
 			query.append("(LOWER(d.subject) like ?");
 			params.add( "%"+subject.toLowerCase()+"%");
 			
 			if(phrase==null){
 				query.append(" or Lower(d.description) like ?");
 				params.add("%"+subject.toLowerCase()+"%");
 			}
 			query.append(")");
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(startDate!=null && endDate!=null){
 			isFirst=false;
 			query.append("t.createdOn>? " +
 					"and " +
 					"t.createdOn<?");
 			
 			params.add( startDate);
 			params.add( endDate);
 		}else if(startDate!=null){
 			isFirst=false;
 			/**
 			 * TODO:This needs to be changed - It will force full table scan 
 			 * -- further these functions only work on Mysql/ Fail on postgres
 			 */
			query.append("STR_TO_DATE(DATE_FORMAT(created, '%d/%m/%y'), '%d/%m/%y')=?");
			params.add( startDate);
 		}else if(endDate!=null){
 			isFirst=false;
 			/**
 			 * TODO:This needs to be changed - It will force full table scan 
 			 */
			query.append("STR_TO_DATE(DATE_FORMAT(created, '%d/%m/%y'), '%d/%m/%y')=?");
			params.add( endDate);
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(priority!=null){
 			isFirst=false;
 			query.append("d.priority=?");
 			params.add( priority);
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(phrase!=null){
 			isFirst=false;
 			query.append("Lower(d.description) like ?");
 			params.add("%"+phrase.toLowerCase()+"%");
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(docType!=null){
 			isFirst=false;
 			query.append("d.docType=?");
 			params.add( docType);
 		}
 		
 		if(!isFirst){
 			query.append(" AND ");
 			isFirst=true;//so that we dont add another AND
 		}
 		
 		if(isFirst){
 			//we ended up with an "and" at the end
 			query = new StringBuffer(query.subSequence(0, query.length()-5).toString());
 		}
 	
 		Query hquery = em.createNativeQuery(query.toString());
 		
 		for(int i=0; i<params.size(); i++){
 			hquery.setParameter(i+1, params.get(i));
 		}
 		
 		List<BigInteger> b_ids = hquery.getResultList();
 		
 		List<Long> ids = new ArrayList<>();
 		
 		for(BigInteger b: b_ids){
 			ids.add(b.longValue());
 		}
 		
 		if(ids.isEmpty()){
 			return new ArrayList<>();
 		}
 		
 		return searchTasks(ids);
 		
 //		String queryIds = "select t.id taskId " +
 //				"d.id as documentId " +
 //				"from localdocument d " +
 //				"inner join Task t "+
 //				"on t.processInstanceId=d.processInstanceId " +
 //				"left join OrganizationalEntity owner " +
 //				"on (owner.id= t.actualOwner_id and owner.DTYPE='User') "+
 //				"left join PeopleAssignments_PotOwners potowners " +
 //				"on (potowners.taskid=t.id) "+
 //				"where " +
 //				 "t.archived = 0 and "+
 //			    "(owner.id = ? "+
 //			   " or  "+
 //			    	"( potowners.entity_id = ? or potowners.entity_id in (?) ) "+
 //			    ") "+
 //			    
 //			     "and "+
 //
 //			    "( "+
 //			    "name.language = :language "+
 //			    "or t.names.size = 0 "+
 //			    ") and "+
 //			    
 //			    "( "+
 //			   " subject.language = :language "+
 //			   " or t.subjects.size = 0 "+
 //			   " ) and "+
 //
 //			    "( "+
 //			    "description.language = :language "+
 //			    "or t.descriptions.size = 0 "+ 
 //			    ")and  "+
 //			    "t.taskData.expirationTime is null ";
 		
 				
 	}
 
 	private List<TaskSummary> searchTasks(List<Long> ids) {
 		String query = "select "+
 		   "distinct "+
 		   "new org.jbpm.task.query.TaskSummary( "+
 		    "t.id, "+
 		    "t.taskData.processInstanceId, "+
 		    "name.text, "+
 		    "subject.text, "+
 		    "description.text, "+
 		    "t.taskData.status, "+
 		    "t.priority, "+
 		    "t.taskData.skipable, "+
 		    "actualOwner, "+
 		    "createdBy, "+
 		    "t.taskData.createdOn, "+
 		    "t.taskData.activationTime, "+
 		    "t.taskData.expirationTime, "+
 		    "t.taskData.processId, "+
 		    "t.taskData.processSessionId) "+
 		"from "+
 		    "Task t " +
 		    "left join t.taskData.createdBy as createdBy "+
 		    "left join t.taskData.actualOwner as actualOwner "+ 
 		    "left join t.subjects as subject "+
 		    "left join t.descriptions as description "+
 		    "left join t.names as name, "+
 		    "OrganizationalEntity potentialOwners "+
 		"where " +
 		//"t.processInstanceId=d.processInstanceId "+
 		    "t.id in (:taskIds) ";
 
 		Query hquery = em.createQuery(query)		
 		.setParameter("taskIds", ids);
 		
 		List list = hquery.getResultList();
 
 		return list;
 	}
 
 	/**
 	 * Returns DocType with the provided ID
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public ADDocType getDocumentTypeById(Long id) {
 
 		ADDocType docType = (ADDocType)em.createQuery("from ADDocType t where t.id=:id")
 				.setParameter("id", id)
 				.getSingleResult();
 		
 		return docType;
 	}
 
 	public ADDocType getDocumentTypeByName(String docTypeName) {
 
 		ADDocType docType = (ADDocType)em.createQuery("from ADDocType t where t.name=:name")
 				.setParameter("name", docTypeName)
 				.getSingleResult();
 		
 		return docType;
 	}
 
 	public ADDocType getDocumentTypeByDocumentId(Long documentId) {
 		
 		ADDocType type = (ADDocType)em.createQuery("select d.type FROM DocumentModel d where d.id=:documentId")
 		.setParameter("documentId", documentId).getSingleResult();
 		
 		return type;
 	}
 
 	public List<ADDocType> getDocumentTypes() {
 		
 		List docTypes = em.createQuery("from ADDocType t order by t.display")
 				.getResultList();
 		
 		return docTypes;
 	}
 
 	/**
 	 * Return form ID for a document type
 	 * - The document type to form id is matched using the form name and process name 
 	 * 
 	 * @param documentTypeId
 	 * @return
 	 */
 	public Long getFormId(Long documentTypeId) {
 		String query = "select f.id from ADForm f " +
 				"inner join ProcessDefModel p on (f.name=p.processid) " +
 				"inner join ADDocType t on t.processDefId=p.id " +
 				"where t.id=:id";
 		Long value = null;
 		
 		try{
 			BigInteger bint = (BigInteger)em.createNativeQuery(query).setParameter("id", documentTypeId).getSingleResult();
 			value = bint.longValue();
 		}catch(Exception e){
 			//e.printStackTrace();
 		}
 
 		return value;
 	}
 	
 	public DetailModel getDetailById(Long detailId){
 		return (DetailModel)em.createQuery("FROM DetailModel d where id=:id")
 				.setParameter("id", detailId).
 				getSingleResult();	
 	}
 
 	public TaskDelegation getTaskDelegationByTaskId(Long taskId) {
 		try{
 		return (TaskDelegation)em.createQuery("FROM TaskDelegation d where d.taskId=:taskId")
 				.setParameter("taskId", taskId).
 				getSingleResult();
 		}catch(Exception e){}
 		
 		return null;
 	}
 	
 	/**
 	 * Request/{No}/{YY} - Request/0001/14 <br/>
 	 * Request/{No}/{MM}/{YY} - Request/0001/01/14 <br/> 
 	 * Request/{No}/{YYYY} - Request/0001/2014 <br/>
 	 * 
 	 * @param type
 	 * @return String generated subject
 	 */
 	public String generateDocumentSubject(ADDocType type){
 		String format = "Request/"+type.getId()+"/{No}/{YY}";
 				
 		Integer no = type.getLastNum();
 		if(no==null || no==0){
 			no=0;
 		}
 		++no;
 		
 		type.setLastNum(no);
 		
 		String subjectFormat = type.getSubjectFormat();
 		
 		if(subjectFormat!=null){
 			format = subjectFormat;
 		}
 
 		String num = (no<10)? "000"+no :
 			(no<100)? "00"+no :
 				(no<1000)? "0"+no :no+"";
 		
 		SimpleDateFormat formatter = new SimpleDateFormat("YY");
 		String yy = formatter.format(new Date());
 		
 		formatter = new SimpleDateFormat("yyyy");
 		String yyyy = formatter.format(new Date());
 		
 		formatter = new SimpleDateFormat("MM");
 		String mm = formatter.format(new Date());
 		
 		format = format.replaceAll("\\{No\\}", num)
 				.replaceAll("\\{YY\\}",yy)
 				.replaceAll("\\{YYYY\\}",yyyy)
 				.replaceAll("\\{MM\\}",mm);
 		
 		return format;
 	}
 
 	public boolean deleteDocument(Long documentId) {
 		DocumentModel document = getById(documentId);
 		
 		document.setIsActive(0);
 		save(document);
 		
 		return true;
 	}
 	
 }
