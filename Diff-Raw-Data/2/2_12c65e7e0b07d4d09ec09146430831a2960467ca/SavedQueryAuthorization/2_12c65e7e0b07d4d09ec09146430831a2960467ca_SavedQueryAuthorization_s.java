 /**
  * 
  */
 
 package edu.wustl.common.query.authoriztion;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import java.util.Vector;
 
 import edu.wustl.common.beans.SecurityDataBean;
 import edu.wustl.common.querysuite.queryobject.impl.ParameterizedQuery;
 import edu.wustl.common.security.PrivilegeCache;
 import edu.wustl.common.security.PrivilegeManager;
 import edu.wustl.common.security.PrivilegeUtility;
 import edu.wustl.common.security.SecurityManager;
 import edu.wustl.common.security.exceptions.SMException;
 
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.query.util.global.Constants;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.dao.ProtectionElementSearchCriteria;
 import gov.nih.nci.security.exceptions.CSException;
 
 public class SavedQueryAuthorization implements Roles
 {
 
 	public void authenticate(Set<ParameterizedQuery> protectionObjects,final String csmUserId,
 			boolean shareQuery, User user) throws DAOException
 	{
 
 		try
 		{
 			ParameterizedQuery query = (ParameterizedQuery) protectionObjects.iterator().next();
 			PrivilegeManager privilegeManager = PrivilegeManager.getInstance();
 
 			privilegeManager.insertAuthorizationData(getAuthorizationData(query, user, shareQuery),
 					protectionObjects, getDynamicGroups(query), query.getObjectId());
 						if(shareQuery)
 						{
 							insertProtectionElementForSharedQueries(query);
 						}
 						
 			updateProtectionGroup(query, user);
 		}
 		catch (Exception e)
 		{
 			throw new DAOException(e);
 		}
 
 	}
 
 	private void insertProtectionElementForSharedQueries(ParameterizedQuery query)
 			throws CSException
 	{
 		ProtectionElement protectionElement = new ProtectionElement();
 
		List<ProtectionElement> peList = new ArrayList<ProtectionElement>();
 		PrivilegeUtility privilegeUtility = new PrivilegeUtility();
 		protectionElement.setProtectionElementName(query.getObjectId());
 		protectionElement
 				.setApplication(privilegeUtility
 						.getApplication(SecurityManager.APPLICATION_CONTEXT_NAME));
 		ProtectionElementSearchCriteria searchCriteria = new ProtectionElementSearchCriteria(protectionElement);
 		peList = privilegeUtility.getUserProvisioningManager().getObjects(searchCriteria);
 		if (peList != null && !peList.isEmpty())
 		{
 			protectionElement = peList.get(0);
 		}
 		privilegeUtility.getUserProvisioningManager().assignProtectionElement(
 				Constants.PUBLIC_QUERY_PROTECTION_GROUP, protectionElement.getObjectId());
 
 	}
 
 	private void updateProtectionGroup(ParameterizedQuery query, User user) throws SMException,
 			CSException, Exception
 	{
 		PrivilegeUtility privilegeUtility = new PrivilegeUtility();
 		privilegeUtility.getUserProvisioningManager().assignProtectionElement(
 				getUserProtectionGroup(user.getUserId().toString()), query.getObjectId());
 		PrivilegeCache privilegeCache = PrivilegeManager.getInstance().getPrivilegeCache(
 				user.getLoginName());
 		privilegeCache.refresh();
 	}
 
 	public String[] getDynamicGroups(ParameterizedQuery query)
 	{
 		String[] dynamicGroups = null;
 		return dynamicGroups;
 	}
 
 	/**
 	 * This method returns collection of UserGroupRoleProtectionGroup objects that specifies the 
 	 * user group protection group linkage through a role. It also specifies the groups the protection  
 	 * elements returned by this class should be added to.
 	 * @return
 	 * @throws CSException 
 	 */
 	protected Vector<SecurityDataBean> getAuthorizationData(ParameterizedQuery query, User user,
 			boolean shareQuery) throws SMException, CSException
 	{
 		Vector<SecurityDataBean> authorizationData = new Vector<SecurityDataBean>();
 		Set<gov.nih.nci.security.authorization.domainobjects.User> group = new HashSet<gov.nih.nci.security.authorization.domainobjects.User>();
 		group.add(user);
 
 		String pgName = getUserProtectionGroup(user.getUserId().toString());//new String(ManagedQueryCSMUtil.getSavedQueryPGName(query.getId()));
 		SecurityDataBean securityDataBean = getSaveQuerySecurityBean( user.getUserId()
 				.toString(), group, pgName);
 		authorizationData.add(securityDataBean);
 		
 		return authorizationData;
 	}
 
 	/**
 	 * @param query
 	 * @param csmUserId
 	 * @param authorizationData
 	 * @param group
 	 * @return 
 	 */
 	private SecurityDataBean getSaveQuerySecurityBean(String csmUserId,
 			Set<gov.nih.nci.security.authorization.domainobjects.User> group,
 			String pgName)
 	{
 		SecurityDataBean securityDataBean = new SecurityDataBean();
 		securityDataBean.setUser(csmUserId);
 		securityDataBean.setRoleName(EXECUTE_QUERY);
 		securityDataBean.setProtectionGroupName(pgName);
 		securityDataBean.setGroupName(getUserProtectionGroup(csmUserId));
 		securityDataBean.setGroup(group);
 
 		return securityDataBean;
 	}
 
 	public String getUserProtectionGroup(String csmUserId)
 	{
 		return "User_" + csmUserId;
 	}
 }
