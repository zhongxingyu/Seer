 /*L
  *  Copyright Ekagra Software Technologies Ltd.
  *  Copyright SAIC, SAIC-Frederick
  *
  *  Distributed under the OSI-approved BSD 3-Clause License.
  *  See http://ncip.github.com/common-security-module/LICENSE.txt for details.
  */
 
 package gov.nih.nci.security.provisioning;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module (CSM) Software License, Version 3.0 Copyright
  *2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'CSM Software').  The CSM Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.
  *
  *This CSM Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the CSM Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the CSM Software; (ii) distribute and have distributed to
  *and by third parties the CSM Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
 import gov.nih.nci.security.authorization.domainobjects.FilterClause;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.InstanceLevelMappingElement;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.authorization.jaas.AccessPermission;
 import gov.nih.nci.security.constants.Constants;
 import gov.nih.nci.security.dao.AuthorizationDAO;
 import gov.nih.nci.security.dao.AuthorizationDAOImpl;
 import gov.nih.nci.security.dao.ProtectionElementSearchCriteria;
 import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.exceptions.CSConfigurationException;
 import gov.nih.nci.security.exceptions.CSDataAccessException;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import gov.nih.nci.security.system.ApplicationSessionFactory;
 import gov.nih.nci.security.util.ConfigurationHelper;
 import gov.nih.nci.security.util.StringEncrypter;
 import gov.nih.nci.security.util.StringUtilities;
 import gov.nih.nci.security.util.StringEncrypter.EncryptionException;
 
 import java.net.URL;
 import java.security.Principal;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.security.auth.Subject;
 import javax.security.auth.login.LoginException;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 
 
 
 
 
 /**
  * This class is an implementation of UserProvisioningManager. All the methods
  * from UserProvisioingManager are implemented here.
  * @version 1.0
  * @author modik
  */
 public class AuthorizationManagerImpl implements UserProvisioningManager {
 
 
 	/**
 	 * authorizationDAO is an instance of AuthorizationDAO , which is used for
 	 * peristence.
 	 */
 	private AuthorizationDAO authorizationDAO;
 
 	static final Logger log = Logger.getLogger(AuthorizationManagerImpl.class.getName());
 	
 	/**
 	 * The application context object for the given application
 	 * peristence.
 	 */
 	private ApplicationContext applicationContext;
 
 	/**
 	 * Is Encryption enabled for the givent application
 	 * peristence.
 	 */
 	private boolean isEncryptionEnabled;
 
 
 	/**
 	 * Constructor for UserProvisioningManagerImpl.
 	 * @param applicationContextName String
 	 * @throws CSConfigurationException
 	 */
 	public AuthorizationManagerImpl(String applicationContextName) throws CSConfigurationException{
 		/**
 		 *  Ultimately we have to use ApplicationSessionFactory class
 		 *  to get appropriate sessionFcatory for a application.
 		 */
 		//SessionFactory sf = AuthorizationDAOSessionFactory.getHibernateSessionFactory(applicationContextName);
 		SessionFactory sf = ApplicationSessionFactory.getSessionFactory(applicationContextName);
 		ConfigurationHelper.getInstance(applicationContextName);
 		AuthorizationDAOImpl adi = new AuthorizationDAOImpl(sf,applicationContextName);
 		authorizationDAO = (AuthorizationDAO)(adi);
 		try
 		{
 			this.applicationContext = (ApplicationContext)authorizationDAO.getApplication(applicationContextName);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public AuthorizationManagerImpl(String applicationContextName, HashMap connectionProperties) throws CSConfigurationException{
 		/**
 		 *  Ultimately we have to use ApplicationSessionFactory class
 		 *  to get appropriate sessionFcatory for a application.
 		 */
 		SessionFactory sf = ApplicationSessionFactory.getSessionFactory(applicationContextName, connectionProperties);
 		ConfigurationHelper.getInstance(applicationContextName);
 		AuthorizationDAOImpl adi = new AuthorizationDAOImpl(sf,applicationContextName);
 		authorizationDAO = (AuthorizationDAO)(adi);
 		try
 		{
 			this.applicationContext = (ApplicationContext)authorizationDAO.getApplication(applicationContextName);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public AuthorizationManagerImpl(String applicationContextName, URL url) throws CSConfigurationException
 	{
 		SessionFactory sf = ApplicationSessionFactory.getSessionFactory(applicationContextName, url);
 		ConfigurationHelper.getInstance(applicationContextName);
 		AuthorizationDAOImpl adi = new AuthorizationDAOImpl(sf,applicationContextName);
 		authorizationDAO = (AuthorizationDAO)(adi);
 		try
 		{
 			this.applicationContext = (ApplicationContext)authorizationDAO.getApplication(applicationContextName);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Constructor for UserProvisioningManagerImpl.
 	 * @param applicationContextName String
 	 * @param userOrGroupName
 	 * @param isUserName
 	 * @throws CSConfigurationException
 	 */
 	public AuthorizationManagerImpl(String applicationContextName, String userOrGroupName, boolean isUserName) throws CSConfigurationException{
 		/**
 		 *  Ultimately we have to use ApplicationSessionFactory class
 		 *  to get appropriate sessionFcatory for a application.
 		 */
 		//SessionFactory sf = AuthorizationDAOSessionFactory.getHibernateSessionFactory(applicationContextName);
 		SessionFactory sf = ApplicationSessionFactory.getSessionFactory(applicationContextName);
 		AuthorizationDAOImpl adi = new AuthorizationDAOImpl(sf,applicationContextName, userOrGroupName, isUserName);
 		authorizationDAO = (AuthorizationDAO)(adi);
 		try
 		{
 			this.applicationContext = (ApplicationContext)authorizationDAO.getApplication(applicationContextName);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public AuthorizationManagerImpl(String applicationContextName, String userOrGroupName, boolean isUserName, URL url) throws CSConfigurationException{
 		/**
 		 *  Ultimately we have to use ApplicationSessionFactory class
 		 *  to get appropriate sessionFcatory for a application.
 		 */
 		//SessionFactory sf = AuthorizationDAOSessionFactory.getHibernateSessionFactory(applicationContextName);
 		SessionFactory sf = ApplicationSessionFactory.getSessionFactory(applicationContextName, url);
 		ConfigurationHelper.getInstance(applicationContextName);
 		AuthorizationDAOImpl adi = new AuthorizationDAOImpl(sf,applicationContextName, userOrGroupName, isUserName);
 		authorizationDAO = (AuthorizationDAO)(adi);
 		try
 		{
 			this.applicationContext = (ApplicationContext)authorizationDAO.getApplication(applicationContextName);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 
 	/**
 	 * Method finalize.
 	 * @throws Throwable
 	 */
 	public void finalize() throws Throwable {
 
 	}
 
 	/**
 	 * @param protectionGroup
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#createProtectionGroup(ProtectionGroup)
 	 */
 	public void createProtectionGroup(ProtectionGroup protectionGroup) throws CSTransactionException{
 		protectionGroup.setApplication(authorizationDAO.getApplication());
 		protectionGroup.setUpdateDate(new Date());
 		authorizationDAO.createObject(protectionGroup);
 
 		//authorizationDAO.createProtectionGroup(protectionGroup);
 
 	}
 
 	public boolean checkPermissionForProvisioningOperation(String operationName, String privilegeName, String userId, String applicationContext)
 	throws CSTransactionException
 	{
 		String uptPeOjectId=privilegeName +"_"+ operationName;
 		return authorizationDAO.checkPermissionForUserProvisioningOperation(uptPeOjectId,  userId, applicationContext);
 	}
 
 	/**
 	 * @param loginName
 	 *
 	 * @return User
 	 * @see gov.nih.nci.security.AuthorizationManager#getUser(String)
 	 */
 	public User getUser(String loginName){
 		return authorizationDAO.getUser(loginName);
 	}
 
 	/**
 	 *
 	 * @return Users
 	 * @see gov.nih.nci.security.AuthorizationManager#getUser(String)
 	 */
 	public List<User> getUsers(){
 		return authorizationDAO.getUsers();
 	}
 	
 	/**
 	 * @param authorizationDAO
 	 *
 	 */
 	public void setAuthorizationDAO(AuthorizationDAO authorizationDAO){
 		this.authorizationDAO = authorizationDAO;
 	}
 
 
 	/**
 	 * @param protectionGroup
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyProtectionGroup(ProtectionGroup)
 	 */
 	public void modifyProtectionGroup(ProtectionGroup protectionGroup)throws CSTransactionException{
 
 		protectionGroup.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(protectionGroup);
 
 
 	}
 
 	/**
 	 * @param protectionGroupName
 	 * @param protectionElementObjectId String
 	 * @param protectionElementAttributeName
 	 *
 	 * @throws CSTransactionException
 	 *
 	 */
 	public void assignProtectionElement(String protectionGroupName, String protectionElementObjectId, String protectionElementAttributeName) throws CSTransactionException{
 
 		authorizationDAO.assignProtectionElement(protectionGroupName,protectionElementObjectId);
 	}
 	/**
 	 * @param protectionGroupId String
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeProtectionGroup(String)
 	 */
 	public void removeProtectionGroup(String protectionGroupId) throws CSTransactionException{
 		ProtectionGroup protectionGroup;
 		try
 		{
 			protectionGroup = this.getProtectionGroupById(protectionGroupId);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			protectionGroup = new ProtectionGroup();
 			protectionGroup.setProtectionGroupId(new Long(protectionGroupId));
 			protectionGroup.setProtectionGroupName("XX");
 			protectionGroup.setProtectionGroupDescription("XX");
 			protectionGroup.setUpdateDate(new Date());
 		}
 		authorizationDAO.removeObject(protectionGroup);
 
     }
 
 	/**
 	 * @param protectionElementId String
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeProtectionElement(String)
 	 */
 	public void removeProtectionElement(String  protectionElementId) throws CSTransactionException{
 		ProtectionElement protectionElement;
 		try
 		{
 			protectionElement = this.getProtectionElementById(protectionElementId);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			protectionElement = new ProtectionElement();
 			protectionElement.setProtectionElementId(new Long(protectionElementId));
 			protectionElement.setProtectionElementName("XX");
 			protectionElement.setProtectionElementDescription("XX");
 			protectionElement.setUpdateDate(new Date());
 		}
 
 		authorizationDAO.removeObject(protectionElement);
 	}
 
 	/**
 	 * @param protectionElementObjectId String
 	 * @param userNames String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.AuthorizationManager#setOwnerForProtectionElement(String, String[])
 	 */
 	public void setOwnerForProtectionElement(String protectionElementObjectId, String[] userNames)throws CSTransactionException{
 
 		authorizationDAO.setOwnerForProtectionElement(protectionElementObjectId,userNames);
 	}
 
 	/**
 	 * @param userId
 	 * @param rolesId
 	 * @param protectionGroupId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#addUserRoleToProtectionGroup(String, String[], String)
 	 */
 	public void addUserRoleToProtectionGroup(String userId, String[] rolesId, String protectionGroupId)throws CSTransactionException{
 
 		authorizationDAO.addUserRoleToProtectionGroup(userId,rolesId,protectionGroupId);
 	}
 
 	/**
 	 * @param userId
 	 * @param rolesId
 	 * @param protectionGroupId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignUserRoleToProtectionGroup(String, String[], String)
 	 */
 	public void assignUserRoleToProtectionGroup(String userId, String[] rolesId, String protectionGroupId)throws CSTransactionException{
 
 		authorizationDAO.assignUserRoleToProtectionGroup(userId,rolesId,protectionGroupId);
 	}
 
 	/**
 	 * @param protectionGroupName
 	 * @param protectionElementObjectId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.AuthorizationManager#deAssignProtectionElements(String, String)
 	 */
 	public void deAssignProtectionElements(String protectionGroupName,String protectionElementObjectId)throws CSTransactionException{
 		authorizationDAO.deAssignProtectionElements(protectionGroupName,protectionElementObjectId);
 	}
 
 
 	/**
 	 * @param protectionGroupId
 	 * @param protectionElementIds
 	 *
 	 * @throws CSTransactionException
 	 *
 	 */
 	public void removeProtectionElementsFromProtectionGroup(String protectionGroupId, String[] protectionElementIds) throws CSTransactionException {
 		authorizationDAO.removeProtectionElementsFromProtectionGroup(protectionGroupId, protectionElementIds);
 	}
 
 
 
 
 	/**
 	 * @param protectionElement
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.AuthorizationManager#createProtectionElement(ProtectionElement)
 	 */
 	public void createProtectionElement(ProtectionElement protectionElement)throws CSTransactionException{
 		if(protectionElement==null){
 			throw new CSTransactionException("protection element could not be created as it is null");
 		}
 		protectionElement.setApplication(authorizationDAO.getApplication());
 		protectionElement.setUpdateDate(new Date());
 		authorizationDAO.createObject(protectionElement);
 
 		//authorizationDAO.createProtectionElement(protectionElement);
 	}
 
 	/**
 	 * @param protectionGroupId String
 	 * @param userId String
 	 * @param rolesId String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeUserRoleFromProtectionGroup(String, String, String[])
 	 */
 	public void removeUserRoleFromProtectionGroup(String protectionGroupId, String userId, String[] rolesId)throws CSTransactionException{
 		authorizationDAO.removeUserRoleFromProtectionGroup(protectionGroupId,userId,rolesId);
 	}
 
 
 
 	/**
 	 * @param role
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#createRole(Role)
 	 */
 	public void createRole(Role role)throws CSTransactionException{
 		role.setApplication(authorizationDAO.getApplication());
 		role.setUpdateDate(new Date());
 		authorizationDAO.createObject(role);
 
 		//authorizationDAO.createRole(role);
 
 	}
 
 	/**
 	 * @param permission
 	 * @param subject
 	 *
 	 * @return boolean
 	 * @throws CSException
 	 * @see gov.nih.nci.security.AuthorizationManager#checkPermission(AccessPermission, Subject)
 	 */
 	public boolean checkPermission(AccessPermission permission, Subject subject) throws CSException{
 	   return authorizationDAO.checkPermission(permission,subject);
 	}
 
 	/**
 	 * @param role
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyRole(Role)
 	 */
 	public void modifyRole(Role role) throws CSTransactionException{
 		role.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(role);
 	}
 
 	/**
 	 * @param permission
 	 * @param userName
 	 *
 	 * @return boolean
 	 * @see gov.nih.nci.security.AuthorizationManager#checkPermission(AccessPermission, String)
 	 */
 	public boolean checkPermission(AccessPermission permission, String userName) throws CSException{
 		return authorizationDAO.checkPermission(permission,userName);
 	}
 
 	/**
 	 * @param applicationContextName
 	 *
 	 * @see gov.nih.nci.security.AuthorizationManager#initialize(String)
 	 */
 	public void initialize(String applicationContextName){
 
 	}
 
 
 	public void initialize(String applicationContextName, URL url)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 
 
 	/**
 	 * @param roleId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeRole(String)
 	 */
 	public void removeRole(String roleId) throws CSTransactionException{
 		Role r;
 		try
 		{
 			r = this.getRoleById(roleId);
 		}
 		catch (CSObjectNotFoundException e){
 
 		r = new Role();
 		r.setId(new Long(roleId));
 		r.setName("XX");
 		r.setDesc("XX");
 		r.setUpdateDate(new Date());
 		}
 		authorizationDAO.removeObject(r);
 
 	}
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#checkPermission(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public boolean checkPermission(String userName, String objectId, String attributeName, String privilegeName)throws CSException{
 		return authorizationDAO.checkPermission(userName,objectId,attributeName,privilegeName) ;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#checkPermission(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public boolean checkPermission(String userName, String objectId, String attributeName, String attributeValue,  String privilegeName)throws CSException{
 		return authorizationDAO.checkPermission(userName,objectId,attributeName,attributeValue,privilegeName) ;
 	}
 
 
 	/**
 	 * @param privilege
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#createPrivilege(Privilege)
 	 */
 	public void createPrivilege(Privilege privilege) throws CSTransactionException{
 		privilege.setUpdateDate(new Date());
 		authorizationDAO.createObject(privilege);
 		//authorizationDAO.createPrivilege(privilege);
 	}
 
 	/**
 	 * @param userName
 	 * @param objectId
 	 * @param privilegeName
 	 *
 	 * @return boolean
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.AuthorizationManager#checkPermission(String, String, String)
 	 */
 	public boolean checkPermission(String userName, String objectId, String privilegeName) throws CSException{
 		return authorizationDAO.checkPermission(userName,objectId,privilegeName);
 	}
 
 
 
 	public boolean checkPermissionForGroup(String groupName, String objectId, String attributeName, String privilegeName) throws CSException
 	{
 		return authorizationDAO.checkPermissionForGroup(groupName,objectId,attributeName,privilegeName) ;
 	}
 
 	public boolean checkPermissionForGroup(String groupName, String objectId, String attributeName, String attributeValue, String privilegeName) throws CSException
 	{
 		return authorizationDAO.checkPermissionForGroup(groupName,objectId,attributeName,attributeValue,privilegeName) ;
 	}
 	public boolean checkPermissionForGroup(String groupName, String objectId, String privilegeName) throws CSException
 	{
 		return authorizationDAO.checkPermissionForGroup(groupName,objectId,privilegeName) ;
 	}
 
 
 	public List getAccessibleGroups(String objectId, String privilegeName) throws CSException
 	{
 		return authorizationDAO.getAccessibleGroups(objectId, privilegeName) ;
 	}
 
 	public List getAccessibleGroups(String objectId, String attributeName, String privilegeName) throws CSException
 	{
 		return authorizationDAO.getAccessibleGroups(objectId, attributeName, privilegeName) ;
 	}
 
 	/**
 	 * @param privilege
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyPrivilege(Privilege)
 	 */
 	public void modifyPrivilege(Privilege privilege) throws CSTransactionException{
 		privilege.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(privilege);
 	}
 
 
 	/**
 	 * @param privilegeId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removePrivilege(String)
 	 */
 	public void removePrivilege(String privilegeId) throws CSTransactionException{
 		Privilege p;
 		try
 		{
 			p = this.getPrivilegeById(privilegeId);
 		}
 		catch (CSObjectNotFoundException e){
 		p = new Privilege();
 		p.setId(new Long(privilegeId));
 		p.setDesc("XX");
 		p.setName("XX");
 		p.setUpdateDate(new Date());
 		}
 		authorizationDAO.removeObject(p);
 
 	}
 
 	/**
 	 * @param roleId
 	 * @param privilegeIds
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignPrivilegesToRole(String, String[])
 	 */
 	public void addPrivilegesToRole(String roleId,String[] privilegeIds)throws CSTransactionException{
 		authorizationDAO.addPrivilegesToRole(roleId,privilegeIds);
 	}
 
 	/**
 	 * @param roleId
 	 * @param privilegeIds
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignPrivilegesToRole(String, String[])
 	 */
 	public void assignPrivilegesToRole(String roleId,String[] privilegeIds)throws CSTransactionException{
 		authorizationDAO.assignPrivilegesToRole(roleId,privilegeIds);
 	}
 
 
 	/**
 	 * Returns the protection element for the passed object id
 	 * @param objectId
 	 *
 	 * @return ProtectionElement
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.AuthorizationManager#getProtectionElement(String)
 	 */
 	public ProtectionElement getProtectionElement(String objectId) throws CSObjectNotFoundException{
 		return authorizationDAO.getProtectionElement(objectId);
 	}
 
 	/**
 	 * Returns the protection element for the passed object id
 	 * @param protectionElementId String
 	 * @return ProtectionElement
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.AuthorizationManager#getProtectionElementById(String)
 	 */
 	public ProtectionElement getProtectionElementById(String protectionElementId) throws CSObjectNotFoundException{
 		return (ProtectionElement)authorizationDAO.getObjectByPrimaryKey(ProtectionElement.class,protectionElementId);
 		//authorizationDAO.getProtectionElement(protectionElementId);
 	}
 
 	/**
 	 * @param protectionGroupName
 	 * @param protectionElementObjectId
 	 *
 	 * @throws CSTransactionException
 	 *
 	 */
 	public void assignProtectionElement(String protectionGroupName, String protectionElementObjectId)throws CSTransactionException{
             authorizationDAO.assignProtectionElement(protectionGroupName,protectionElementObjectId);
 	}
 
 	/**
 	 * @param group
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#createGroup(Group)
 	 */
 	public void createGroup(Group group)throws CSTransactionException{
 		group.setApplication(authorizationDAO.getApplication());
 		group.setUpdateDate(new Date());
 		authorizationDAO.createObject(group);
 		//authorizationDAO.createGroup(group);
 	}
 
 	/**
 	 * @param userName
 	 * @param protectionElementObjectId String
 	 * @param protectionElementAttributeName
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.AuthorizationManager#setOwnerForProtectionElement(String, String, String)
 	 */
 	public void setOwnerForProtectionElement(String userName, String protectionElementObjectId, String protectionElementAttributeName)throws CSTransactionException{
 		authorizationDAO.setOwnerForProtectionElement( userName, protectionElementObjectId, protectionElementAttributeName );
 	}
 
 
 
 	/**
 	 * @param groupId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeGroup(String)
 	 */
 	public void removeGroup(String groupId)throws CSTransactionException{
 		Group group;
 		try
 		{
 			group = this.getGroupById(groupId);
 		}
 		catch (CSObjectNotFoundException e){
 		group = new Group();
 		group.setGroupId(new Long(groupId));
 		group.setGroupName("XX");
 		group.setGroupDesc("XX");
 		group.setUpdateDate(new Date());
 		}
 		authorizationDAO.removeObject(group);
 	}
 
 	/**
 	 * @param group
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyGroup(Group)
 	 */
 	public void modifyGroup(Group group)throws CSTransactionException{
 		group.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(group);
 	}
 
 	/**
 	 * @param userId
 	 *
 	 * @param groupIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignGroupsToUser(String, String[])
 	 */
 	public void assignGroupsToUser(String userId,String[] groupIds)throws CSTransactionException{
         authorizationDAO.assignGroupsToUser(userId,groupIds);
 	}
 
 	/**
 	 * @param userId
 	 *
 	 * @param groupIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignGroupsToUser(String, String[])
 	 */
 	public void addGroupsToUser(String userId,String[] groupIds)throws CSTransactionException{
         authorizationDAO.addGroupsToUser(userId,groupIds);
 	}
 
 	/**
 	 * @param userId
 	 *
 	 * @param groupIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignGroupsToUser(String, String[])
 	 */
 	public void assignUsersToGroup(String groupId,String[] userIds)throws CSTransactionException{
         authorizationDAO.assignUsersToGroup(groupId,userIds);
 	}
 
 	/**
 	 * @param userId
 	 *
 	 * @param groupIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignGroupsToUser(String, String[])
 	 */
 	public void addUsersToGroup(String groupId,String[] userIds)throws CSTransactionException{
         authorizationDAO.addUsersToGroup(groupId,userIds);
 	}
 
 	/**
 	 * @param groupId
 	 * @param userId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeUserFromGroup(String, String)
 	 */
 	public void removeUserFromGroup(String groupId, String userId)throws CSTransactionException{
            authorizationDAO.removeUserFromGroup(groupId,userId);
 	}
 
 	/**
 	 * @param protectionGroupId
 	 * @param groupId
 	 * @param rolesId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignGroupRoleToProtectionGroup(String, String, String[])
 	 */
 	public void addGroupRoleToProtectionGroup(String protectionGroupId, String groupId, String rolesId[])throws CSTransactionException{
 		authorizationDAO.addGroupRoleToProtectionGroup( protectionGroupId, groupId, rolesId );
 	}
 
 	/**
 	 * @param protectionGroupId
 	 * @param groupId
 	 * @param rolesId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignGroupRoleToProtectionGroup(String, String, String[])
 	 */
 	public void assignGroupRoleToProtectionGroup(String protectionGroupId, String groupId, String rolesId[])throws CSTransactionException{
 		authorizationDAO.assignGroupRoleToProtectionGroup( protectionGroupId, groupId, rolesId );
 	}
 
 	/**
 	 * Returns the privilege for the passed name privilege id
 	 * @param privilegeId
 	 *
 	 * @return Privilege
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getPrivilegeById(String)
 	 */
 	public Privilege getPrivilegeById(String privilegeId)throws CSObjectNotFoundException{
 		return (Privilege)authorizationDAO.getObjectByPrimaryKey(Privilege.class,privilegeId);
 	}
 
 	/**
 	 * This method removes the user from a protection group irrespective of all the
 	 * roles
 	 * @param protectionGroupId
 	 * @param userId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeUserFromProtectionGroup(String, String)
 	 */
 	public void removeUserFromProtectionGroup(String protectionGroupId, String userId) throws CSTransactionException{
 		authorizationDAO.removeUserFromProtectionGroup(protectionGroupId,userId);
 	}
 
 	/**
 	 * @param protectionGroupId
 	 * @param groupId
 	 * @param roleId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeGroupRoleFromProtectionGroup(String, String, String[])
 	 */
 	public void removeGroupRoleFromProtectionGroup(String protectionGroupId, String groupId, String[] roleId) throws CSTransactionException{
 		authorizationDAO.removeGroupRoleFromProtectionGroup(protectionGroupId, groupId, roleId);
 	}
 
 	/**
 	 * @param protectionGroupId
 	 * @param groupId
 	 *
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeGroupFromProtectionGroup(String, String)
 	 */
 	public void removeGroupFromProtectionGroup(String protectionGroupId, String groupId) throws CSTransactionException{
 		authorizationDAO.removeGroupFromProtectionGroup(protectionGroupId,groupId);
 	}
 
 	/**
 	 * @param protectionGroupId
 	 *
 	 * @return ProtectionGroup
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionGroupById(String)
 	 */
 	public ProtectionGroup getProtectionGroupById(String protectionGroupId) throws CSObjectNotFoundException{
 		return (ProtectionGroup)authorizationDAO.getObjectByPrimaryKey(ProtectionGroup.class,protectionGroupId);
 	}
 
 	/**
 	 * @param roleId
 	 *
 	 * @return Role
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getRoleById(String)
 	 */
 	public Role getRoleById(String roleId) throws CSObjectNotFoundException{
 		return (Role)authorizationDAO.getObjectByPrimaryKey(Role.class,roleId);
 	}
 
 	/**
 	 * @param roleId
 	 *
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getPrivileges(String)
 	 */
 	public Set getPrivileges(String roleId) throws CSObjectNotFoundException{
 		return authorizationDAO.getPrivileges(roleId);
 	}
 
 	/**
 	 * @param searchCriteria
 	 *
 	 * @return java.util.List
 	 * @see gov.nih.nci.security.UserProvisioningManager#getObjects(SearchCriteria)
 	 */
 	public java.util.List getObjects(SearchCriteria searchCriteria){
 		return authorizationDAO.getObjects(searchCriteria);
 	}
 	/**
 	 * Method createUser.
 	 * @param user User
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#createUser(User)
 	 */
 	public void createUser(User user) throws CSTransactionException{
 		try {
 			authorizationDAO.validateUser(user);
 		} catch (LoginException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CSException e) {
 			// TODO Auto-generated catch block
 			throw new CSTransactionException(e.getMessage());
 		}
 		user.setUpdateDate(new Date());
 		authorizationDAO.createObject(user);
 		//authorizationDAO.createUser(user);
 	}
 	/**
 	 * Method assignProtectionElements.
 	 * @param protectionGroupId String
 	 * @param protectionElementIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignProtectionElements(String, String[])
 	 */
 	public void assignProtectionElements(String protectionGroupId,String[] protectionElementIds) throws CSTransactionException{
 		authorizationDAO.assignProtectionElements(protectionGroupId,protectionElementIds);
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#addProtectionElements(java.lang.String, java.lang.String[])
 	 */
 	public void addProtectionElements(String protectionGroupId,String[] protectionElementIds) throws CSTransactionException{
 		authorizationDAO.addProtectionElements(protectionGroupId,protectionElementIds);
 	}
 
 	/**
 	 * Method getProtectionGroupRoleContextForUser.
 	 * @param userId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionGroupRoleContextForUser(String)
 	 */
 	public Set getProtectionGroupRoleContextForUser(String userId) throws CSObjectNotFoundException{
 		return authorizationDAO.getProtectionGroupRoleContextForUser(userId);
 	}
 
 	/**
 	 * Method getProtectionGroupRoleContextForGroup.
 	 * @param groupId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionGroupRoleContextForGroup(String)
 	 */
 	public Set getProtectionGroupRoleContextForGroup(String groupId) throws CSObjectNotFoundException{
 		return authorizationDAO.getProtectionGroupRoleContextForGroup(groupId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionElementPrivilegeContextForUser(java.lang.String)
 	 */
 	public Set getProtectionElementPrivilegeContextForUser(String userId) throws CSObjectNotFoundException {
 		return authorizationDAO.getProtectionElementPrivilegeContextForUser(userId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionElementPrivilegeContextForGroup(java.lang.String)
 	 */
 	public Set getProtectionElementPrivilegeContextForGroup(String groupId) throws CSObjectNotFoundException {
 		return authorizationDAO.getProtectionElementPrivilegeContextForGroup(groupId);
 	}
 
 	/**
 	 * Method getGroupById.
 	 * @param groupId String
 	 * @return Group
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getGroupById(String)
 	 */
 	public Group getGroupById(String groupId) throws CSObjectNotFoundException{
 		return (Group)authorizationDAO.getObjectByPrimaryKey(Group.class,groupId);
 	}
 
 	/**
 	 * Method modifyProtectionElement.
 	 * @param protectionElement ProtectionElement
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyProtectionElement(ProtectionElement)
 	 */
 	public void modifyProtectionElement(ProtectionElement protectionElement) throws CSTransactionException{
 		protectionElement.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(protectionElement);
 	}
 
 	/**
 	 * Method getUserById.
 	 * @param userId String
 	 * @return User
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getUserById(String)
 	 */
 	public User getUserById(String userId) throws CSObjectNotFoundException{
 		return (User)authorizationDAO.getObjectByPrimaryKey(User.class,userId);
 	}
 
 	/**
 	 * Method getUsers.
 	 * @param groupId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getUsers(String)
 	 */
 	public Set getUsers(String groupId) throws CSObjectNotFoundException{
 		return authorizationDAO.getUsers(groupId);
 	}
 
 
 
 	/**
 	 * Method modifyUser.
 	 * @param user User
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyUser(User)
 	 */
 	public void modifyUser(User user)throws CSException,LoginException{
 		
 		User currUser = authorizationDAO.getUser(user.getLoginName());
		if(!currUser.getPassword().equalsIgnoreCase(user.getPassword()))
 		{
 			authorizationDAO.validateUser(user);
 		}
 		
 		user.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(user);
 		// update the password history here!!!
 		if(currUser.getPassword()!=null && currUser.getPassword().length()>0)
 		{
 			if(!user.getPassword().equals(encryptPassword(currUser.getPassword(),"YES" )))
 			{
 			
 				// insert into password history!!
 				authorizationDAO.insertIntoPasswordHistory(currUser.getLoginName(), currUser.getPassword());
 			
 			}
 		}
 	}
 	
 	private static String encryptPassword(String encryptedPassword,
 			String encryptionEnabled) {
 		if (!StringUtilities.isBlank(encryptionEnabled) && encryptionEnabled.equalsIgnoreCase(Constants.YES)){
 			StringEncrypter se;
 			try {
 				se = new StringEncrypter();
 				encryptedPassword = se.encrypt(new String(encryptedPassword));
 			} catch (EncryptionException e) {				
 				e.printStackTrace();
 			}
 		}
 		return encryptedPassword;
 	}
 	
 	/**
 	 * Method removeUser.
 	 * @param userId String
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeUser(String)
 	 */
 	public void removeUser(String userId)throws CSTransactionException{
 		try{
 		User user = this.getUserById(userId);
 		 authorizationDAO.removeObject(user);
 		}catch(CSObjectNotFoundException ex){
 			throw new CSTransactionException("Failed to find this user with userId:"+userId,ex);
 		}
 
 	}
 
 	/**
 	 * Method getGroups.
 	 * @param userId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getGroups(String)
 	 */
 	public Set getGroups(String userId) throws CSObjectNotFoundException{
 		return authorizationDAO.getGroups(userId);
 	}
 
 	/**
 	 * Method getProtectionElements.
 	 * @param protectionGroupId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionElements(String)
 	 */
 	public Set getProtectionElements(String protectionGroupId) throws CSObjectNotFoundException{
 		return authorizationDAO.getProtectionElements(protectionGroupId);
 	}
 
 	/**
 	 * Method getProtectionGroups.
 	 * @param protectionElementId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionGroups(String)
 	 */
 	public Set getProtectionGroups(String protectionElementId) throws CSObjectNotFoundException{
 		return authorizationDAO.getProtectionGroups(protectionElementId);
 	}
 
 	/**
 	 * Method addToProtectionGroups.
 	 * @param protectionElementId String
 	 * @param protectionGroupIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignToProtectionGroups(String, String[])
 	 */
 	public void addToProtectionGroups(String protectionElementId,String[] protectionGroupIds) throws CSTransactionException{
 		authorizationDAO.addToProtectionGroups(protectionElementId,protectionGroupIds);
 	}
 	/**
 	 * Method assignToProtectionGroups.
 	 * @param protectionElementId String
 	 * @param protectionGroupIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignToProtectionGroups(String, String[])
 	 */
 	public void assignToProtectionGroups(String protectionElementId,String[] protectionGroupIds) throws CSTransactionException{
 		authorizationDAO.assignToProtectionGroups(protectionElementId,protectionGroupIds);
 	}
 
 	/**
 	 * Method assignParentProtectionGroup.
 	 * @param parentProtectionGroupId String
 	 * @param childProtectionGroupId String
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignParentProtectionGroup(String, String)
 	 */
 	public void assignParentProtectionGroup(String parentProtectionGroupId,String childProtectionGroupId) throws CSTransactionException{
 		authorizationDAO.assignParentProtectionGroup(parentProtectionGroupId,childProtectionGroupId);
 	}
 	/**
 	 * Method createApplication.
 	 * @param application Application
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#createApplication(Application)
 	 */
 	public void createApplication(Application application)throws CSTransactionException{
 		application.setUpdateDate(new Date());
 		authorizationDAO.createObject(application);
 
 	}
 	/**
 	 * Method modifyApplication.
 	 * @param application Application
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#modifyApplication(Application)
 	 */
 	public void modifyApplication(Application application)throws CSTransactionException{
 		application.setUpdateDate(new java.util.Date());
 		authorizationDAO.modifyObject(application);
 	}
 	/**
 	 * Method removeApplication.
 	 * @param applicationId String
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#removeApplication(String)
 	 */
 	public void removeApplication(String applicationId) throws CSTransactionException{
 	        Application app;
 			try
 			{
 				app = this.getApplicationById(applicationId);
 				authorizationDAO.removeObject(app);
 			}
 			catch (CSObjectNotFoundException e)
 			{
 				throw new CSTransactionException("Error in Removing the Application", e);
 			}
 
 	}
 	/**
 	 * Method getApplicationById.
 	 * @param applicationId String
 	 * @return Application
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getApplicationById(String)
 	 */
 	public Application getApplicationById(String applicationId) throws CSObjectNotFoundException{
 		return (Application)authorizationDAO.getObjectByPrimaryKey(Application.class,applicationId);
 		//authorizationDAO.getProtectionElement(protectionElementId);
 	}
 
 	/**
 	 * Method addOwners.
 	 * @param protectionElementId String
 	 * @param userIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#addOwners(String, String[])
 	 */
 	public void addOwners(String protectionElementId,String[] userIds) throws CSTransactionException{
 		authorizationDAO.addOwners(protectionElementId,userIds);
 	}
 
 	/**
 	 * Method assignOwners.
 	 * @param protectionElementId String
 	 * @param userIds String[]
 	 * @throws CSTransactionException
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignOwners(String, String[])
 	 */
 	public void assignOwners(String protectionElementId,String[] userIds) throws CSTransactionException{
 		authorizationDAO.assignOwners(protectionElementId,userIds);
 	}
 
 	/**
 	 * Method getOwners.
 	 * @param protectionElementId String
 	 * @return Set
 	 * @throws CSObjectNotFoundException
 	 * @see gov.nih.nci.security.UserProvisioningManager#getOwners(String)
 	 */
 	public Set getOwners(String protectionElementId) throws CSObjectNotFoundException{
 		return authorizationDAO.getOwners(protectionElementId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getApplicationContext()
 	 */
 	public ApplicationContext getApplicationContext() {
 		// TODO Auto-generated method stub
 		return applicationContext;
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getPrincipals(java.lang.String)
 	 */
 	public Principal[] getPrincipals(String userName) {
 		// TODO Auto-generated method stub
 		return authorizationDAO.getPrincipals(userName);
 	}
 
 	public java.util.List getProtectionGroups(){
 		ProtectionGroup pg = new ProtectionGroup();
 		pg.setProtectionGroupName("%");
 		SearchCriteria sc = new ProtectionGroupSearchCriteria(pg);
 		return this.getObjects(sc);
 	}
 
 	public ProtectionElement getProtectionElement(String objectId,String attributeName){
 		ProtectionElement result = null;
 		ProtectionElement pe = new ProtectionElement();
 		pe.setObjectId(objectId);
 		pe.setAttribute(attributeName);
 		SearchCriteria sc = new ProtectionElementSearchCriteria(pe);
 		java.util.List list = this.getObjects(sc);
 		if(list.size()!=0){
 			result = (ProtectionElement)list.get(0);
 		}
 		return result;
 	}
 
 	public Object secureObject(String userName, Object obj) throws CSException{
 		return authorizationDAO.secureObject(userName,obj);
 	}
 
 	public Collection secureCollection(String userName,Collection objects) throws CSException{
 		return authorizationDAO.secureCollection(userName,objects);
 	}
 
 	public Collection getPrivilegeMap(String userName,Collection protectionElements) throws CSException{
 		return authorizationDAO.getPrivilegeMap(userName,protectionElements);
 	}
 
 	public Object secureUpdate(String userName, Object originalObject,Object mutatedObject) throws CSException{
 		return authorizationDAO.secureUpdate(userName,originalObject,mutatedObject);
 	}
 
 	public boolean checkOwnership(String userName,
 			String protectionElementObjectId){
 		return authorizationDAO.checkOwnership(userName,protectionElementObjectId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignUserToGroup(java.lang.String, java.lang.String)
 	 */
 	public void assignUserToGroup(String userName, String groupName)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 		authorizationDAO.assignUserToGroup( userName, groupName );
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#setAuditUserInfo(java.lang.String, java.lang.String)
 	 */
 	public void setAuditUserInfo(String userName, String sessionId)
 	{
 		UserInfoHelper.setUserInfo(userName, sessionId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#setEncryptionEnabled(boolean)
 	 */
 	public void setEncryptionEnabled(boolean isEncryptionEnabled) {
 		this.isEncryptionEnabled = isEncryptionEnabled;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getApplication(java.lang.String)
 	 */
 	public Application getApplication(String applicationContextName) throws CSObjectNotFoundException
 	{
 		return authorizationDAO.getApplication(applicationContextName);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#removeOwnerForProtectionElement(java.lang.String, java.lang.String[])
 	 */
 	public void removeOwnerForProtectionElement(String protectionElementObjectId, String[] userNames) throws CSTransactionException
 	{
 		authorizationDAO.removeOwnerForProtectionElement(protectionElementObjectId,userNames);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#removeOwnerForProtectionElement(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public void removeOwnerForProtectionElement(String userName, String protectionElementObjectId, String protectionElementAttributeName) throws CSTransactionException
 	{
 		authorizationDAO.removeOwnerForProtectionElement( userName, protectionElementObjectId, protectionElementAttributeName );
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getAttributeMap(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public List getAttributeMap(String userName, String className, String privilegeName)
 	{
 		return authorizationDAO.getAttributeMap(userName, className, privilegeName);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getAttributeMap(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public List getAttributeMapForGroup(String groupName, String className, String privilegeName)
 	{
 		return authorizationDAO.getAttributeMapForGroup(groupName, className, privilegeName);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#createFilterClause(gov.nih.nci.security.authorization.domainobjects.FilterClause)
 	 */
 	public void createFilterClause(FilterClause filterClause) throws CSTransactionException
 	{
 		filterClause.setApplication(authorizationDAO.getApplication());
 		filterClause.setUpdateDate(new Date());
 		authorizationDAO.createObject(filterClause);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getFilterClauseById(java.lang.String)
 	 */
 	public FilterClause getFilterClauseById(String filterClauseId) throws CSObjectNotFoundException
 	{
 		return (FilterClause)authorizationDAO.getObjectByPrimaryKey(FilterClause.class,filterClauseId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#modifyFilterClause(gov.nih.nci.security.authorization.domainobjects.FilterClause)
 	 */
 	public void modifyFilterClause(FilterClause filterClause) throws CSTransactionException
 	{
 		filterClause.setUpdateDate(new Date());
 		authorizationDAO.modifyObject(filterClause);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#removeFilterClause(java.lang.String)
 	 */
 	public void removeFilterClause(String filterClauseId) throws CSTransactionException
 	{
 		FilterClause filterClause;
 		try
 		{
 			filterClause = this.getFilterClauseById(filterClauseId);
 		}
 		catch (CSObjectNotFoundException e){
 			throw new CSTransactionException("Failed to find this Filter Clause with filterClauseId : "+ filterClauseId, e);		}
 		authorizationDAO.removeObject(filterClause);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#createInstanceLevelMappingElement(gov.nih.nci.security.authorization.domainobjects.InstanceLevelMappingElement)
 	 */
 	public void createInstanceLevelMappingElement(InstanceLevelMappingElement instanceLevelMappingElement) throws CSTransactionException {
 		instanceLevelMappingElement.setApplication(authorizationDAO.getApplication());
 		instanceLevelMappingElement.setUpdateDate(new Date());
 		authorizationDAO.createObject(instanceLevelMappingElement);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#getInstanceLevelMappingElementById(java.lang.String)
 	 */
 	public InstanceLevelMappingElement getInstanceLevelMappingElementById(String instanceLevelMappingElementId) throws CSObjectNotFoundException {
 		return (InstanceLevelMappingElement)authorizationDAO.getObjectByPrimaryKey(InstanceLevelMappingElement.class,instanceLevelMappingElementId);
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#modifyInstanceLevelMappingElement(gov.nih.nci.security.authorization.domainobjects.InstanceLevelMappingElement)
 	 */
 	public void modifyInstanceLevelMappingElement(InstanceLevelMappingElement instanceLevelMappingElement) throws CSTransactionException {
 		instanceLevelMappingElement.setUpdateDate(new Date());
 		authorizationDAO.modifyObject(instanceLevelMappingElement);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#removeInstanceLevelMappingElement(java.lang.String)
 	 */
 	public void removeInstanceLevelMappingElement(String instanceLevelMappingElementId) throws CSTransactionException {
 		InstanceLevelMappingElement instanceLevelMappingElement;
 		try{
 			instanceLevelMappingElement= this.getInstanceLevelMappingElementById(instanceLevelMappingElementId);
 		}catch (CSObjectNotFoundException e){
 			throw new CSTransactionException("Failed to find this InstanceLevelMappingElement with Id : "+ instanceLevelMappingElementId, e);
 		}
 		authorizationDAO.removeObject(instanceLevelMappingElement);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.AuthorizationManager#refreshInstanceTables(boolean)
 	 */
 	public void refreshInstanceTables(boolean instanceLevelSecurityForUser) throws CSObjectNotFoundException, CSDataAccessException {
 
 		authorizationDAO.refreshInstanceTables(instanceLevelSecurityForUser);
 	}
 
 
 	public void maintainInstanceTables(String instanceLevelMappingElementId) throws CSObjectNotFoundException, CSDataAccessException {
 		authorizationDAO.maintainInstanceTables(instanceLevelMappingElementId);
 
 	}
 
 
 
 }
