 package gov.nih.nci.rembrandt.web.ajax;
 
 import gov.nih.nci.caintegrator.application.configuration.SpringContext;
 import gov.nih.nci.caintegrator.application.lists.ListOrigin;
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.application.workspace.TreeStructureType;
 import gov.nih.nci.caintegrator.application.workspace.UserQuery;
 import gov.nih.nci.caintegrator.application.workspace.Workspace;
 import gov.nih.nci.caintegrator.application.workspace.WorkspaceList;
 import gov.nih.nci.caintegrator.security.UserCredentials;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.query.Queriable;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.util.RembrandtListLoader;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.hibernate.util.SerializationHelper;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import uk.ltd.getahead.dwr.WebContext;
 import uk.ltd.getahead.dwr.WebContextFactory;
 
 public class WorkspaceHelper {
     private static RembrandtListLoader myListLoader = (RembrandtListLoader) SpringContext.getBean("listLoader");
 	private static Logger logger = Logger.getLogger(WorkspaceHelper.class);
     private static RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 
 	public WorkspaceHelper()	{}
 	public static Workspace fetchListWorkspaceFromDB(HttpSession sess, Long userId){
 		Workspace listWorkspace = null;
 		if(userId != null){
 			//get LISTS
 		}
 		listWorkspace = myListLoader.loadTreeStructure(userId, TreeStructureType.LIST);
 		////// NOTE: we want to read this from the DB, if null then create and persist
 		if(listWorkspace != null  && listWorkspace.getTreeStructure()!= null)	{
 			sess.setAttribute(RembrandtConstants.LIST_WORKSPACE, listWorkspace);
 			}
 		return listWorkspace;
 	}
 	public static Workspace fetchQueryWorkspaceFromDB(HttpSession sess, Long userId){
 		Workspace queryWorkspace = null;
 		if(userId != null){
 			//get Queries
 		}
 		queryWorkspace = myListLoader.loadTreeStructure(userId, TreeStructureType.QUERY);
 		////// NOTE: we want to read this from the DB, if null then create and persist
 		if(queryWorkspace != null  && queryWorkspace.getTreeStructure()!= null)	{
 			sess.setAttribute(RembrandtConstants.QUERY_WORKSPACE, queryWorkspace);
 			}
 		return queryWorkspace;
 	}
 	public static SessionQueryBag loadSessionQueryBagFromDB(HttpSession session , Long userId){
 		SessionQueryBag queryBag = null;
 		UserQuery userQuery = null;
 		if(userId != null){
             userQuery = myListLoader.loadUserQuery(userId);
             if(userQuery != null  && userQuery.getQueryContent()!= null){
             	session.setAttribute(RembrandtConstants.USER_QUERY, userQuery);
             	byte[] objectData = userQuery.getQueryContent();
             	Object obj =  SerializationHelper.deserialize(objectData);   
             	if(obj instanceof SessionQueryBag){
             		queryBag = (SessionQueryBag)obj;
             	}
             }else{
             	session.removeAttribute(RembrandtConstants.USER_QUERY);
             }
 		}
 		return queryBag;
 
 	}
 	public static List<UserList> loadCustomListsFromDB(String userName){
 		//load custom lists
 		List<UserList> customLists= new ArrayList<UserList>();
 		if(userName != null ){
 			customLists = (List<UserList>) myListLoader.loadCustomListsByUserName(userName);	            
 		}
 		return customLists;
 	}
 	public static String fetchListTreeStructures()	{
 		String listTrees = "";
 		WebContext ctx = WebContextFactory.get();
 		HttpServletRequest req = ctx.getHttpServletRequest();
 		HttpSession sess = req.getSession(); 
 		JSONArray jsaList = generateListJSONArray(sess);
 		
 		listTrees = jsaList.toString();
 		
 		//store it to DB when DAOs are ready
 		sess.setAttribute(RembrandtConstants.OLIST_STRUCT, listTrees);
 						
 		return listTrees;
 	}
 	public static String fetchQueryTreeStructures()	{
 		String queryTrees = "";
 		WebContext ctx = WebContextFactory.get();
 		HttpServletRequest req = ctx.getHttpServletRequest();
 		HttpSession sess = req.getSession(); 
 		JSONArray jsaQuery = generateQueryJSONArray(sess);
 
 		queryTrees = jsaQuery.toString();
 		//store it to DB when DAOs are ready
 		sess.setAttribute(RembrandtConstants.OQUERY_STRUCT, queryTrees);
 
 				
 		return queryTrees;
 	}
 	public static JSONArray generateQueryJSONArray(HttpSession sess) {
 		String trees;
 		UserCredentials credentials = (UserCredentials)sess.getAttribute(RembrandtConstants.USER_CREDENTIALS);
 		
 		////// NOTE: we want to read this from the DB, if null then create and persist
 		Workspace queryWorkspace = fetchQueryWorkspaceFromDB(sess, credentials.getUserId());
 		if(queryWorkspace != null  && queryWorkspace.getTreeStructure()!= null)	{
 				trees = queryWorkspace.getTreeStructure();
 			}else{
 				trees = null;
 		}
 			////////////// TEST DATA String
 			//trees = "[ { 'id' : 'root', 'txt' : 'Lists', 'editable': false, 'items' : [ { 'id' : 'ast', 'txt' : 'ASTROCYTOMA', 'editable': false, 'acceptdrop' : false }, { 'id' : 'branch_t21', 'txt' : 'high survival patients', 'img' : 'folder.gif', 'items' : [ { 'id': 'sub11', 'txt': 'my patient list', 'editable': false, 'acceptdrop' : false } ] }, { 'id' : 'branch_t22', 'txt' : 'ALL GLIOMA', 'editable': false, 'acceptdrop' : false }, { 'id' : 'branch_t23', 'txt' : 'MySaved Patients', 'editable': false, 'acceptdrop' : false }, { 'id' : 'branch_t24', 'txt' : 'Patients_good_survival', 'editable': false, 'acceptdrop' : false }, { 'id' : 'trash', 'last' : true, 'draggable' : false, 'txt' : 'Trash', 'img' : 'trash.gif', 'imgopen' : 'trash.gif', 'imgclose' : 'trash.gif', 'open' : false, 'editable': false, 'tooltip' : 'Items here will be removed when the workspace is saved', 'items' : [ { 'id': 'sub1', 'txt': 'one to delete' , 'editable': false, 'acceptdrop' : false } ] } ] } ]";
 
 			//generate the Tree on the first time thru, and persist
 			SessionQueryBag queryBag = _cacheManager.getSessionQueryBag(sess.getId());
 			//convert this into the JSON format we need
 			JSONArray jsa = new JSONArray();
 			JSONObject root = null;
 			JSONObject query = null;
 			JSONArray queryItems = null;
 			JSONArray rootItems = null;
 			if(trees != null){
 				root = findNode( trees, "root" );
 				rootItems = getNodeItems(root);
 				query = findNodeInTree(rootItems,"geQuery");
 				queryItems = getNodeItems(query);				
 				JSONArray newItems = new JSONArray();
 				for(String queryName :  queryBag.getGeneExpressionQueryNames()){
 					if(!foundItemInTree(rootItems, queryName)){
 							String id = queryName;
 							String txt = queryName;
 							String tooltip = queryBag.getQuery(queryName).toString();							JSONObject item = createNodeItem(id,txt,tooltip);
 							newItems.add(item);
 						}
 					}
 				queryItems.addAll(newItems);
 				query = findNodeInTree(rootItems,"cpQuery");
 				queryItems = getNodeItems(query);				
 				newItems = new JSONArray();
 				for(String queryName :  queryBag.getComparativeGenomicQueryNames()){
 					if(!foundItemInTree(rootItems, queryName)){
 							String id = queryName;
 							String txt = queryName;
 							String tooltip = queryBag.getQuery(queryName).toString();							JSONObject item = createNodeItem(id,txt,tooltip);
 							newItems.add(item);
 						}
 					}
 				queryItems.addAll(newItems);
 				query = findNodeInTree(rootItems,"clinicalQuery");
 				queryItems = getNodeItems(query);				
 				newItems = new JSONArray();
 				for(String queryName :  queryBag.getClinicalDataQueryNames()){
 					if(!foundItemInTree(rootItems, queryName)){
 							String id = queryName;
 							String txt = queryName;
 							String tooltip = queryBag.getQuery(queryName).toString();							JSONObject item = createNodeItem(id,txt,tooltip);
 							newItems.add(item);
 						}
 					}
 				queryItems.addAll(newItems);
 				jsa.add(root);
 			}else {
 			root= createDefaultNode("root","Queries");
 			rootItems = new JSONArray();
 			//for each list, add it to the root folder, since they are not organized yet
 			//GE
 			query = createDefaultNode("geQuery","Gene Expression Queries");
 			queryItems = new JSONArray();
 			for(String queryName :  queryBag.getGeneExpressionQueryNames()){
 							String id = queryName;
 							String txt = queryName;
 							String tooltip = queryBag.getQuery(queryName).toString();
 							JSONObject item = createNodeItem(id,txt,tooltip);
 							queryItems.add(item);
 			}
 			query.put("items", queryItems);
 			rootItems.add(query);
 	        root.put("items", queryItems);
 	        //CP
 			query = createDefaultNode("cpQuery","Copy Number Queries");
 			queryItems = new JSONArray();
 			for(String queryName :  queryBag.getComparativeGenomicQueryNames()){
 				String id = queryName;
 				String txt = queryName;
 				String tooltip = queryBag.getQuery(queryName).toString();
 				JSONObject item = createNodeItem(id,txt,tooltip);
 				queryItems.add(item);
 			}
 			query.put("items", queryItems);
 			rootItems.add(query);
 			//CLINICAL
 			query = createDefaultNode("clinicalQuery","Clinical Data Queries");
 			queryItems = new JSONArray();
 			for(String queryName :  queryBag.getClinicalDataQueryNames()){
 				String id = queryName;
 				String txt = queryName;
 				String tooltip = queryBag.getQuery(queryName).toString();
 				JSONObject item = createNodeItem(id,txt,tooltip);
 				queryItems.add(item);
 			}
 			query.put("items", queryItems);
 			rootItems.add(query);
 			//add the trash
 			JSONObject trashList = createTrashNode();			
 			rootItems.add(trashList);
 	        root.put("items", rootItems);
 			
 	        jsa.add(root);
 	}
 		return jsa;
 	}
 	public static JSONArray generateListJSONArray(HttpSession sess) {
 		String trees;
 		UserCredentials credentials = (UserCredentials)sess.getAttribute(RembrandtConstants.USER_CREDENTIALS);
 		
 		////// NOTE: we want to read this from the DB, if null then create and persist
 		Workspace workspace = fetchListWorkspaceFromDB(sess, credentials.getUserId());
 		if(workspace != null  && workspace.getTreeStructure()!= null)	{
 				trees = workspace.getTreeStructure();
 			}else{
 				trees = null;
 		}
 
 
 			////////////// TEST DATA String
 			//trees = "[ { 'id' : 'root', 'txt' : 'Lists', 'editable': false, 'items' : [ { 'id' : 'ast', 'txt' : 'ASTROCYTOMA', 'editable': false, 'acceptdrop' : false }, { 'id' : 'branch_t21', 'txt' : 'high survival patients', 'img' : 'folder.gif', 'items' : [ { 'id': 'sub11', 'txt': 'my patient list', 'editable': false, 'acceptdrop' : false } ] }, { 'id' : 'branch_t22', 'txt' : 'ALL GLIOMA', 'editable': false, 'acceptdrop' : false }, { 'id' : 'branch_t23', 'txt' : 'MySaved Patients', 'editable': false, 'acceptdrop' : false }, { 'id' : 'branch_t24', 'txt' : 'Patients_good_survival', 'editable': false, 'acceptdrop' : false }, { 'id' : 'trash', 'last' : true, 'draggable' : false, 'txt' : 'Trash', 'img' : 'trash.gif', 'imgopen' : 'trash.gif', 'imgclose' : 'trash.gif', 'open' : false, 'editable': false, 'tooltip' : 'Items here will be removed when the workspace is saved', 'items' : [ { 'id': 'sub1', 'txt': 'one to delete' , 'editable': false, 'acceptdrop' : false } ] } ] } ]";
 
 			//generate the Tree on the first time thru, and persist
 			UserListBeanHelper ulbh = new UserListBeanHelper(sess.getId());
 			List<UserList> uls = ulbh.getAllLists(); //getAllLists() for testing,  getAllCustomLists() for prod
 			//convert this into the JSON format we need
 			JSONArray jsa = new JSONArray();
 			JSONObject root = null;
 			JSONObject customNode = null;
 			JSONArray customItems = null;
 			JSONArray rootItems = null;
 			// if one exists, check if all lists are within it
 			if(trees != null){
 				root = findNode( trees, "root" );
 				rootItems = getNodeItems(root);
 				customNode = findNodeInTree(rootItems,"custom");
 				customItems = getNodeItems(customNode);				
 				JSONArray newItems = new JSONArray();
 				for(UserList ul : uls){
 					if(ul.getListOrigin().equals(ListOrigin.Custom)){
 						if(!foundItemInTree(rootItems, ul.getName())){
 								String id = ul.getName();
 								String txt = ul.getName();
 								String tooltip = ul.getListOrigin() + " " + ul.getListType() + " (" + ul.getItemCount() + ")";
 								JSONObject item = createNodeItem(id,txt,tooltip);
 								newItems.add(item);
 							}
 						}
 					}
 				if(customItems != null){
 					customItems.addAll(newItems);
 				}
 				jsa.add(root);
 				
 			}else {	// create initial struct if none exists
 					root= createDefaultNode("root","Lists");
 					rootItems = new JSONArray();
 					//for each list, add it to the root folder, since they arent organized yet
 					customNode = createDefaultNode("custom","Custom Lists");
 					customItems = new JSONArray();
 					for(UserList ul : uls){
 						if(ul.getListOrigin().equals(ListOrigin.Custom)){
 									String id = ul.getName();
 									String txt = ul.getName();
 									String tooltip = ul.getListOrigin() + " " + ul.getListType() + " (" + ul.getItemCount() + ")";
 									JSONObject item = createNodeItem(id,txt,tooltip);
 									customItems.add(item);
 							}
 					}		
 					customNode.put("items", customItems);
 					rootItems.add(customNode);
 					//add the trash
 			        JSONObject trashList = createTrashNode();
 					rootItems.add(trashList);
 			        root.put("items", rootItems);			
 			        jsa.add(root);
 			}
 		return jsa;
 	}
 	public static JSONObject createTrashNode(){
         JSONObject trashNode = new JSONObject();
 		trashNode.put("id", "trash");
 		trashNode.put("last", true);
 		trashNode.put("draggable", false);
 		trashNode.put("txt", "Trash");
 		trashNode.put("editable", false);
 		trashNode.put("img", "trash.gif");
 		trashNode.put("imgopen", "trash.gif");
 		trashNode.put("imgclose", "trash.gif");
 		trashNode.put("open", false);
 		trashNode.put("tooltip", "Items here will be removed when the workspace is saved");
         return trashNode;
 	}
 	public static JSONObject createNodeItem(String id, String txt, String tooltip){
 		JSONObject nodeItem = new JSONObject();
 		nodeItem.put("id", id); //can nodes contain spaces?
 		nodeItem.put("editable", false);
 		nodeItem.put("txt", txt);
 		nodeItem.put("acceptdrop", false);
 		nodeItem.put("tooltip", tooltip);
 		//TODO: set the style att for a CSS class that will color by list type?
 		return nodeItem;
 	}
 	public static JSONObject createDefaultNode(String id, String txt){
 		JSONObject node = new JSONObject();
 		node.put("id", id);
 		node.put("txt", txt);
 		node.put("editable", false);
 		node.put("draggable", false);
 		node.put("img", "folder.gif");
 		return node;
 	}
 	public static String saveTreeStructures(String listTreeString, String queryTreeString)	{
 		//this should be an array or hash
 		//for testing, its just 1 string now
 		//need to decouple this from DWR, so other classes (i.e. logoutAction can call this and access the session)
 		WebContext ctx = WebContextFactory.get();
 		HttpServletRequest req = ctx.getHttpServletRequest();
 		HttpSession sess = req.getSession(); 
 		//delete any items in the trash can in the Lists Tree
 		String listTree = removeListsFromTrash( listTreeString, sess.getId() );
 		sess.setAttribute(RembrandtConstants.OLIST_STRUCT, listTree);
 		
 		String queryTree = removeQueriesFromTrash( queryTreeString, sess.getId() );
 		sess.setAttribute(RembrandtConstants.OQUERY_STRUCT, queryTree);
 		
 		saveWorkspace( sess );
 
 	return "pass";
 	}
 	private static String removeListsFromTrash(String listTreeString, String sessionId) {
 		JSONArray jsa = new JSONArray();
 		JSONObject root = findNode( listTreeString, "root" );
 		JSONArray rootItems = getNodeItems(root);
 		JSONObject trashNode = findNodeInTree( rootItems, "trash" );	
 		JSONArray trashItems = getNodeItems(trashNode);
 		UserListBeanHelper ulbh = new UserListBeanHelper(sessionId);
 		List<UserList> userLists = ulbh.getAllCustomLists();
 		for(UserList userList :userLists){
 			if(foundItemInTree(trashItems, userList.getName())){
				ulbh.removeList(userList.getName());
 			}
 		}
 		trashItems.clear();
 		jsa.add(root);
 		return jsa.toString();	
 		}
 	private static String removeQueriesFromTrash(String queryTreeString, String sessionId) {
 		JSONArray jsa = new JSONArray();
 		JSONObject root = findNode( queryTreeString, "root" );
 		JSONArray rootItems = getNodeItems(root);
 		JSONObject trashNode = findNodeInTree( rootItems, "trash" );	
 		JSONArray trashItems = getNodeItems(trashNode);
 		SessionQueryBag queryBag = _cacheManager.getSessionQueryBag(sessionId);
//		List<String> trashItemsList = new ArrayList<String>();
 		Collection<String> queryList = queryBag.getQueryNames();
 		for(String queryName :queryList){
 			if(foundItemInTree(trashItems, queryName)){
//				trashItemsList.add(queryName);
				queryBag.removeQuery(queryName);
 			}
 		}
//		for(String trashItem: trashItemsList){
//			removeItemFromTree(trashItems, trashItem);
//			queryBag.removeQuery(trashItem);			
//		}
 		_cacheManager.putSessionQueryBag(sessionId, queryBag);
 		trashItems.clear();
 		jsa.add(root);
 		return jsa.toString();	
 		}
 	public static String saveWorkspace(HttpSession sess ){
 		/*
     	 * User has selected to save the current session and log out of the
     	 * application
     	 */
     	UserCredentials credentials = (UserCredentials)sess.getAttribute(RembrandtConstants.USER_CREDENTIALS);
     	
     	/*******************************************************************
     	 * This is only here to prevent a user logged in on the public
     	 * account "RBTuser" from persisting their session
     	 *******************************************************************/
     	if(!"RBTuser".equals(credentials.getUserName())) {
     		//Check to see user also created some custom lists.
 
     		UserListBeanHelper userListBeanHelper = new UserListBeanHelper(sess.getId());
     		
     		List<UserList> customLists = userListBeanHelper.getAllCustomLists();
     		if (!customLists.isEmpty()){
     			myListLoader.saveUserCustomLists(sess.getId(), credentials.getUserName());        			
     		}
     		List<UserList> removedLists = userListBeanHelper.getAllDeletedCustomLists();
     		if (!removedLists.isEmpty()){
     			myListLoader.deleteUserCustomLists(sess.getId(), credentials.getUserName());        			
     		}
 
     		//get List Tree from session to save to DB
 			String listTree = (String) sess.getAttribute(RembrandtConstants.OLIST_STRUCT);
 			Workspace listWorkspace = (Workspace) sess.getAttribute(RembrandtConstants.LIST_WORKSPACE);
 			Long userId = credentials.getUserId();
 			//Save Lists
 			if(listTree != null && userId != null){
 				myListLoader.saveTreeStructure(userId, TreeStructureType.LIST, listTree, listWorkspace);
 			}
     		//get Query Tree from session to save to DB
 			String queryTree = (String) sess.getAttribute(RembrandtConstants.OQUERY_STRUCT);
 			UserQuery userQuery = (UserQuery) sess.getAttribute(RembrandtConstants.USER_QUERY);
 			SessionQueryBag queryBag = _cacheManager.getSessionQueryBag(sess.getId());
 			myListLoader.saveSessionQueryBag(userId, queryBag, userQuery);
 			//Save Query
 			Workspace queryWorkspace = (Workspace) sess.getAttribute(RembrandtConstants.QUERY_WORKSPACE);
 			if(queryTree != null && userId != null){
 				myListLoader.saveTreeStructure(userId, TreeStructureType.QUERY, queryTree, queryWorkspace);
 			}
 			return "pass";
     	}
 		return "fail";
 	}
 	
 	public static boolean foundItemInTree(JSONArray treeArray, String itemName){
 		boolean foundInTree = false;
 		JSONObject node = null;
 		JSONArray items = null;
 		if(treeArray != null){
 		    Iterator iterator = treeArray.iterator();
 		    Object obj = null;
 			while(iterator.hasNext()){
 				obj = iterator.next();
 				if( obj instanceof JSONObject){
 					node = (JSONObject)obj;
 					String txt = (String)node.get("id");
 					if(txt != null && txt.equals(itemName)){
 						foundInTree = true;
 						break;
 					}
 					JSONArray nodeItems = getNodeItems(node);
 					if(!foundInTree){
 						foundInTree = foundItemInTree(nodeItems, itemName );	// recursive call to find the item in the tree
 					}
 
 				}
 		    }
 		}
 		return foundInTree;
 	}
 	public static JSONObject findNode( String tree, String inName )
 	{
 		JSONArray jsonArray = null;
 		Object parseObj = JSONValue.parse(tree);
 		if(parseObj instanceof JSONArray){
 			jsonArray =(JSONArray)parseObj;
 		}else if (parseObj instanceof JSONObject){
 			jsonArray = getNodeItems((JSONObject) parseObj);
 		}
 		
 		JSONObject root = null;
 		JSONArray rootItems = null;
 		JSONObject node = null;
 		
 	    Iterator iterator = jsonArray.iterator();
 	    Object obj = null;
 		while(iterator.hasNext()){
 			obj = iterator.next();
 			
 			root = (JSONObject)obj;
 			if(root.containsValue("root")){
 				if( inName.equals( "root"))		// User wants the top root
 				{
 					return root;
 				}
 
 				rootItems = (JSONArray) root.get("items");		
 				node = findNodeInTree(rootItems, inName );	// recursive call to find the node in the tree
 			}
 	    }
 		
 		return node;
 		
 	}
 	public static JSONArray getNodeItems( JSONObject node)
 	{
 		JSONArray nodeItems = null;
 		if(node != null){
 			nodeItems = (JSONArray) node.get("items");	
 		}
 
 		return nodeItems;
 		
 	}
 	private static JSONObject findNodeInTree(JSONArray items, String inName ) {
 		Object obj;
 		if(items != null){
 			Iterator iterator = items.iterator();
 			while(iterator.hasNext()){
 				obj = iterator.next();
 				JSONObject jsaObj = (JSONObject)obj;
 				if(jsaObj.containsValue(inName)) {		// found the node 
 					return jsaObj;
 				}
 				else	// search recursively
 				{
 					JSONArray customItems = (JSONArray) jsaObj.get("items");
 					jsaObj = findNodeInTree(customItems, inName );
 					
 					if ( jsaObj != null )
 						return jsaObj;
 				}
 			}
 		}
 		return null;
 	}
 	
 
 }
