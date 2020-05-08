 package gov.nih.nci.rembrandt.util;
 
import gov.nih.nci.caintegrator.application.lists.ListItem;
 import gov.nih.nci.caintegrator.application.lists.ListLoader;
 import gov.nih.nci.caintegrator.application.lists.ListManager;
 import gov.nih.nci.caintegrator.application.lists.ListOrigin;
 import gov.nih.nci.caintegrator.application.lists.ListType;
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBean;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.application.workspace.TreeStructureType;
 import gov.nih.nci.caintegrator.application.workspace.UserQuery;
 import gov.nih.nci.caintegrator.application.workspace.Workspace;
 import gov.nih.nci.caintegrator.dto.de.InstitutionDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.rembrandt.dto.lookup.DiseaseTypeLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.service.findings.strategies.StrategyHelper;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.helper.RembrandtListValidator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.type.SerializationException;
 import org.hibernate.util.SerializationHelper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
 import org.springframework.jdbc.support.lob.LobHandler;
 
 public class RembrandtListLoader extends ListLoader {
     private static Logger logger = Logger.getLogger(RembrandtListLoader.class);    
     private SessionFactory sessionFactory;
 
 
 	/**
      * @return Returns the sessionFactory.
      */
     public SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     /**
      * @param sessionFactory The sessionFactory to set.
      */
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
     public RembrandtListLoader() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     public UserListBean loadDiseaseGroups(UserListBean userListBean, HttpSession session) throws OperationNotSupportedException{
         ListManager listManager = new ListManager();
         List<String> allSamplesList = new ArrayList<String>();
         List<String> allGliomaSamplesList = new ArrayList<String>();
         /**
          * this section loops through all REMBRANDTs disease groups found
          * in the getDiseaseType below. Based on credentials, queries are
          * run to return sample according to each disease and made into 
          * default user lists.
          */
 		try {
 			DiseaseTypeLookup[] myDiseaseTypes =LookupManager.getDiseaseType();
 			if(myDiseaseTypes != null){
 				for (DiseaseTypeLookup diseaseTypeLookup : myDiseaseTypes){
 					//1. Get the sample Ids from the each disease type
                     if(!diseaseTypeLookup.getDiseaseType().equals("CELL_LINE")){ //ONLY TEMPORARY!!!!
         					Collection<InstitutionDE> insitutions = InsitutionAccessHelper.getInsititutionCollection(session);
         					List<SampleIDDE> sampleIDDEs = LookupManager.getSampleIDDEs(diseaseTypeLookup.getDiseaseDesc(),insitutions);
         			        //2. Extracts sampleIds as Strings
         			        Collection<String> sampleIDs = StrategyHelper.extractSamples(sampleIDDEs);
         			        List<String> pdids = new ArrayList<String>(sampleIDs);       			        RembrandtListValidator listValidator = new RembrandtListValidator(ListType.PatientDID, pdids);
         			        if(sampleIDDEs != null){
         			            //create userlist with valid samples included
         			            UserList myList = listManager.createList(ListType.PatientDID,diseaseTypeLookup.getDiseaseType(),pdids,listValidator);
         			            if(!myList.getList().isEmpty()){
                                     myList.setListOrigin(ListOrigin.Default);
         			               /**
         			                * add valid samples to allSamplesList to be created last.
         			                * Do not add unknown and unclassified samples. 
         			                */
         			               if(!(diseaseTypeLookup.getDiseaseType().compareToIgnoreCase(RembrandtConstants.UNKNOWN)==0)
         			                       && !(diseaseTypeLookup.getDiseaseType().compareToIgnoreCase(RembrandtConstants.UNCLASSIFIED)==0)){
         			                   allSamplesList.addAll(myList.getList());
         			                   if(!(diseaseTypeLookup.getDiseaseType().compareToIgnoreCase(RembrandtConstants.NON_TUMOR)==0)){
         			                	   allGliomaSamplesList.addAll(myList.getList());
         				               }
         			               }
         			               
         			               //add my list to the userListBean
         			                userListBean.addList(myList);
         			            }
 			        }
 				}
               }
 			}
 		} catch (OperationNotSupportedException e1) {
 			logger.error(e1);
 		} catch (Exception e1) {
 			logger.error(e1);
 		}
         
          //now add the all samples userlist
         if(!allSamplesList.isEmpty()){
 
             RembrandtListValidator listValidator = new RembrandtListValidator(ListType.PatientDID, allGliomaSamplesList);
             UserList myAllGliomaSampleList = listManager.createList(ListType.PatientDID,RembrandtConstants.ALL_GLIOMA,allGliomaSamplesList,listValidator);
             myAllGliomaSampleList.setListOrigin(ListOrigin.Default);
             userListBean.addList(myAllGliomaSampleList);
             
             listValidator = new RembrandtListValidator(ListType.PatientDID, allSamplesList);
             UserList myAllSampleList = listManager.createList(ListType.PatientDID,RembrandtConstants.ALL,allSamplesList,listValidator); 
             myAllSampleList.setListOrigin(ListOrigin.Default);
             userListBean.addList(myAllSampleList);
         }
         return userListBean;        
     }
     public List<UserList> loadUserListsByInstitution(String institutionName){
         Session currentSession = sessionFactory.getCurrentSession(); 
         List<UserList> lists = new ArrayList<UserList>();
         String theHQL = "";
         Query theQuery = null;        
         Collection<UserList> userLists = null;
         theHQL = "select distinct ul from UserList ul where ul.institute = :institutionName";        
         theQuery = currentSession.createQuery(theHQL);
         theQuery.setParameter("institutionName", institutionName);
         System.out.println("HQL: " + theHQL);        
         userLists = theQuery.list();        
         for(UserList list: userLists){
             logger.debug("List name: " + list.getName()); 
             lists.add(list);
         }
         
         return lists;
     }
     public List<UserList> loadCustomListsByUserName(String userName){
         Session currentSession = sessionFactory.getCurrentSession(); 
         List<UserList> lists = new ArrayList<UserList>();
         String theHQL = "";
         Query theQuery = null;        
         Collection<UserList> userLists = null;
         theHQL = "select distinct ul from UserList ul where ul.author = :userName and ul.listOrigin = :origin";        
         theQuery = currentSession.createQuery(theHQL);
         theQuery.setParameter("userName", userName);
         theQuery.setParameter("origin", ListOrigin.Custom);        
         System.out.println("HQL: " + theHQL);        
         userLists = theQuery.list();        
         for(UserList list: userLists){
             logger.debug("List name: " + list.getName()); 
             lists.add(list);
         }
         
         return lists;
     }
     public void saveUserCustomLists(String httpSessionID, String userName){
     	if(httpSessionID != null  && userName != null){
     		UserListBeanHelper userListBeanHelper = new UserListBeanHelper(httpSessionID);
     		List<UserList> customlists =  userListBeanHelper.getAllCustomLists();
     		for(UserList list:customlists){
 		        try {
 					Session currentSession = sessionFactory.getCurrentSession(); 
 					currentSession = sessionFactory.openSession();
 					Transaction transaction = currentSession.beginTransaction();
 					transaction = currentSession.beginTransaction();
 					transaction.begin();
 					list.setAuthor(userName);
 					currentSession.saveOrUpdate(list);
 					transaction.commit();
 					currentSession.close();
					
					//for some reason listid is null. update after DB changes are complete.
					for( ListItem li: list.getListItems()) {
						li.setListId(list.getId());
					}
 				} catch (HibernateException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     		}
     	}
 
     }
     public void deleteUserCustomLists(String httpSessionID, String userName){
     	if(httpSessionID != null  && userName != null ){
     		UserListBeanHelper userListBeanHelper = new UserListBeanHelper(httpSessionID);
     		List<UserList> customlists =  userListBeanHelper.getAllDeletedCustomLists();
     		for(UserList list:customlists){
 			        Session currentSession = sessionFactory.getCurrentSession(); 
 			        currentSession = sessionFactory.openSession();
 			        Transaction transaction = currentSession.beginTransaction();
 			    	transaction = currentSession.beginTransaction();
 			    	transaction.begin();
 			    	currentSession.delete(list);
 			    	transaction.commit();
 			    	currentSession.close();
 			    	userListBeanHelper.clearAllDeletedCustomLists();			    	
     		}
     	}
 
     }
     public Workspace  loadTreeStructure(Long userId, TreeStructureType type){
         Session currentSession = sessionFactory.getCurrentSession(); 
         List<Workspace> lists = null;
         String theHQL = "";
         Query theQuery = null;        
         if(userId != null && type != null){
 	        theHQL = "select w from Workspace w where w.userId = :userId and w.treeType = :treeType";        
 	        theQuery = currentSession.createQuery(theHQL);
 	        theQuery.setParameter("userId", userId);
 	        theQuery.setParameter("treeType", type.toString());        
 	        System.out.println("HQL: " + theHQL);        
 	        lists = theQuery.list();  
 	        if(lists != null  && lists.size() == 1){
 		        for(Workspace wp: lists){
 		            return wp;
 		        }
 	        }
         }
         return null;
     }
     
     public void saveTreeStructure(Long userId, TreeStructureType treeType, String treeStructure, Workspace workspace){
     	if(userId != null  && treeType != null  &&  treeStructure != null){
 	        Session currentSession = sessionFactory.getCurrentSession(); 
 	        currentSession = sessionFactory.openSession();
 	        Transaction transaction = currentSession.beginTransaction();
 	    	transaction = currentSession.beginTransaction();
 	    	transaction.begin();
 	    	if(workspace == null){ //first time create a new workspace
 	    		workspace = new Workspace();
 		    	workspace.setTreeType(treeType.toString());
 		    	workspace.setUserId(userId);
 	    	}
 	    	workspace.setTreeStructure(treeStructure);
 	    	currentSession.saveOrUpdate(workspace);
 	    	transaction.commit();
 	    	currentSession.close();
     	}
     }
     public UserQuery  loadUserQuery(Long userId){
         Session currentSession = sessionFactory.getCurrentSession(); 
         String theHQL = "";
         Query theQuery = null;        
         List<UserQuery> listUserQuery = null;
         if(userId != null ){
 	        theHQL = "select u from UserQuery u where u.userId = :userId";        
 	        theQuery = currentSession.createQuery(theHQL);
 	        theQuery.setParameter("userId", userId);   
 	        System.out.println("HQL: " + theHQL);        
 	        listUserQuery = theQuery.list();  
 	        if(listUserQuery != null  && listUserQuery.size() == 1){
 		        for(UserQuery uq: listUserQuery){
 		            return uq;
 		        }
 	        }
         }
         return null;
     }
     public UserQuery saveSessionQueryBag(Long userId, SessionQueryBag queryBag, UserQuery userQuery){
     	if(userId != null   &&  queryBag != null){
 	        try {
 	        	        	
 				Session currentSession = sessionFactory.getCurrentSession(); 
 				currentSession = sessionFactory.openSession();
 				Transaction transaction = currentSession.beginTransaction();
 				transaction = currentSession.beginTransaction();
 				transaction.begin();
 				if(userQuery == null){ //first time create a new userQuery
 					userQuery = new UserQuery();
 					userQuery.setUserId(userId);
 				}
 				byte[] serializedBag = SerializationHelper.serialize(queryBag);
 				userQuery.setQueryContent(serializedBag);
 				currentSession.saveOrUpdate(userQuery);
 				transaction.commit();
 				currentSession.close();
 				
 			} catch (SerializationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (HibernateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     	
     	return userQuery;
     }
 
 }
