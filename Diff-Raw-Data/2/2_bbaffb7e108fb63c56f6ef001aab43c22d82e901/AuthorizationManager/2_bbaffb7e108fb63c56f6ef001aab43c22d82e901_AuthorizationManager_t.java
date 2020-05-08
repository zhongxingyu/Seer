 package gov.nih.nci.security;
 
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
 
 import gov.nih.nci.security.authorization.ObjectPrivilegeMap;
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.authorization.jaas.AccessPermission;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.SecurityServiceProvider;
 
 import java.security.Principal;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import javax.security.auth.Subject;
 
 
 
 
 
 /**
  * 
  * The <code>AuthorizationManager</code> interface provides all the
  * authorization methods and services offered by the Common Security Module.
  * This interface defines the contract that any class which wants to acts as an
  * authorization manager should follow to be able to fit in the Common Security
  * Framework. It defines the methods which are required for the purpose of
  * authorizing a user against the configured authorization data. This
  * interface by default is implemented by the
  * {@link UserProvisioningManager}. If the client application wants to use its own
  * Authorization Class, then it should implement the
  * <code>AuthorizationManager</code> interface. Also an entry should be configured
  * in the <code>ApplicationServiceConfig</code> file against the Application
  * Context Name regsitering the class, which it wants to use, as shown below
  * <p>
  * <blockquote>
  * 
  * <pre>
  *  	&lt;application&gt;
  *	   		&lt;context-name&gt;
  *	   			FooApplication
  *	      	&lt;/context-name&gt;
  *			:
  *			:
  *	      	&lt;authorization&gt;
  *		      	&lt;authorization-provider-class&gt;
  *	     			com.Foo.AuthorizationManagerClass
  *	     		&lt;/authorization-provider-class&gt;
  *			&lt;/authorization&gt;
  *		&lt;/application&gt;
  * </pre>
  * 
  * </blockquote>
  * <p>
  * 
  * If the client application wants to use just the authorization service then it can
  * obtain the implementation of the <code>AuthorizationManager</code> interface from the 
  * {@link SecurityServiceProvider} class.
  * 
  * @author Vinay Kumar(Ekagra Software Technologies Ltd.) 
  * 
  */
 
 public interface AuthorizationManager {
 
 	/**
 	 * This method returns the User object from the database for the passed User's Login Name. If no User is
 	 * found then null is returned
 	 * @param loginName The Login Name of the User which is to be obtained
 	 * 
 	 * @return User The User object from the database for the passed Login Name
 	 */
 	public User getUser(String loginName);
 
 	/**
 	 * Returns the {@link ApplicationContext} for which the AuthorizationManager is instantiated. This 
 	 * ApplicationContext object contains the information about the application as well all the associated
 	 * data for the application
 	 * @return ApplicationContext The {@link ApplicationContext} object for which the AuthorizationManager is instantiated
 	 */
 	public ApplicationContext getApplicationContext();
 
 	/**
 	 * Assigns a ProtectionElement to the Protection Group. The Protection Element is first retrieved using the passed
 	 * Object Id and Attribute Name from the database. Similarly the Protection Group is retrieved using the name passed.
 	 * Then both of these entities are associated in the database. If there is any error in the association then a 
 	 * {@link CSTransactionException} is thrown. These errors could be raised if it isnt able to retrieve either the 
 	 * Protection Element or Protection Group for the given data or there are any errors in the actual assignment
 	 * @param protectionGroupName The name of the Protection Group to which the Protection Element is to be associated
 	 * @param protectionElementObjectId The object Id of the Protection Element to which the Protection Group is to be associated
 	 * @param protectionElementAttributeName The attribute name of the Protection Element to which the Protection Group is to be associated
 	 * 
 	 * @throws CSTransactionException If it isnt able to retrieve either the  Protection Element or Protection Group 
 	 * for the given data or there are any errors in the actual assignment.
 	 */
 	public void assignProtectionElement(String protectionGroupName, String protectionElementObjectId, String protectionElementAttributeName)throws CSTransactionException;
 
 	/**
 	 * Assigns Owners for a Protection Elements. It retrieves the Protection Element from the database for the passed Object Id
 	 * The retrieves the User object for the list of USer Names passed. It then associated the Users as owners to the 
 	 * Protection Element
 	 * @param protectionElementObjectId The Object Id of the Protection Element to which the Owners are to be assigned
 	 * @param userNames The list of User names which are to be assigned as owners to the Protection Element
 	 * @throws CSTransactionException If it isnt able to retrieve either the  Protection Element or Users 
 	 * for the given data or there are any errors in the actual assignment.
 	 */
 	public void setOwnerForProtectionElement(String protectionElementObjectId, String[] userNames)throws CSTransactionException;
 
 	/**
 	 * Deassigns a ProtectionElement from the Protection Group. The Protection Element is first retrieved using the passed
 	 * Object Id from the database. Similarly the Protection Group is retrieved using the name passed.
 	 * Then both of these entities are de-associated in the database. If there is any error in the de-association then a 
 	 * {@link CSTransactionException} is thrown. These errors could be raised if it isnt able to retrieve either the 
 	 * Protection Element or Protection Group for the given data or there are any errors in the actual deassignment
 	 * @param protectionGroupName The name of the Protection Group from which the Protection Element is to be de-associated
 	 * @param protectionElementObjectId The object Id of the Protection Element from which the Protection Group is to be de-associated
 	 * 
 	 * @throws CSTransactionException If it isnt able to retrieve either the  Protection Element or Protection Group 
 	 * for the given data or there are any errors in the actual deassignment.
 	 */
 	public void deAssignProtectionElements(String protectionGroupName,String protectionElementObjectId)throws CSTransactionException;
 
 	/**
 	 * This method creates a new Protection Element in the database based on the data passed
 	 * @param protectionElement the Protection Element object which is to be created
 	 * 
 	 * @throws CSTransactionException If there is any exception in creating the Protection Element
 	 */
 	public void createProtectionElement(ProtectionElement protectionElement)throws CSTransactionException;
 
 	
 	/**
 	 * The method checks the permission for a {@link Subject} for a given {@link AccessPermission}. The {@link Subject}
 	 * is nothing but the <code>JAAS</code> subject and the {@link AccessPermission} is collection of the 
 	 * resource on the operation is to be performed and the actual operation itself.
 	 * @param permission The collection of resource and the operation to be performed on it
 	 * @param subject The <code>JAAS</code> representation of the user.
 	 * 
 	 * @return boolean Returns true if the user has permission to perform the operation on that particular resource
 	 * @throws CSException If there are any errors while checking for permission
 	 */
 	public boolean checkPermission(AccessPermission permission, Subject subject) throws CSException;
 
 	/**
 	 * The method checks the permission for a {@link User} for a given {@link AccessPermission}. The 
 	 * {@link AccessPermission} is collection of the resource on the operation is to be performed and 
 	 * the actual operation itself. The userName is used to to obtain the User object and then the check
 	 * permission operation is performed to see if the user has the required access or not.
 	 * @param permission The collection of resource and the operation to be performed on it
 	 * @param userName The user name of the user which is trying to perform the operation
 	 * 
 	 * @return boolean Returns true if the user has permission to perform the operation on that particular resource
 	 * @throws CSException If there are any errors while checking for permission
 	 */
 	public boolean checkPermission(AccessPermission permission, String userName) throws CSException;
 
 	/**
 	 * The method checks the permission for a {@link User} for a given {@link ProtectionElement}. The 
 	 * {@link ProtectionElement} is obtained using the object id and the attribute name both. 
 	 * The userName is used to to obtain the User object. Then the check
 	 * permission operation is performed to see if the user has the required access or not.
 	 * @param userName The user name of the user which is trying to perform the operation
 	 * @param objectId The object id of the protection element on which the user wants to perform the operation
 	 * @param attributeName The attribute of the protection element on which the user wants to perform the operation
 	 * @param privilegeName The operation which the user wants to perform on the protection element
 	 * 
 	 * @return boolean Returns true if the user has permission to perform the operation on that particular resource
 	 * @throws CSException If there are any errors while checking for permission
 	 */
 	public boolean checkPermission(String userName, String objectId, String attributeName, String privilegeName) throws CSException;
 
 	/**
 	 * The method checks the permission for a {@link User} for a given {@link ProtectionElement}. The 
 	 * {@link ProtectionElement} is obtained using the object id only. 
 	 * The userName is used to to obtain the User object. Then the check
 	 * permission operation is performed to see if the user has the required access or not.
 	 * @param userName The user name of the user which is trying to perform the operation
 	 * @param objectId The object id of the protection element on which the user wants to perform the operation
 	 * @param privilegeName The operation which the user wants to perform on the protection element
 	 * 
 	 * @return boolean Returns true if the user has permission to perform the operation on that particular resource
 	 * @throws CSException If there are any errors while checking for permission
 	 */
 	public boolean checkPermission(String userName, String objectId, String privilegeName) throws CSException;
 
 	/**
 	 * The method checks the permission for a {@link Group} for a given {@link ProtectionElement}. The 
 	 * {@link ProtectionElement} is obtained using the object id and the attribute name both. 
 	 * The groupName is used to to obtain the Group object. Then the check
 	 * permission operation is performed to see if the Group has the required access or not.
 	 * @param groupName The group name which is trying to perform the operation
 	 * @param objectId The object id of the protection element on which the user wants to perform the operation
 	 * @param attributeName The attribute of the protection element on which the user wants to perform the operation
 	 * @param privilegeName The operation which the user wants to perform on the protection element
 	 * 
 	 * @return boolean Returns true if the user has permission to perform the operation on that particular resource
 	 * @throws CSException If there are any errors while checking for permission
 	 */
 	public boolean checkPermissionForGroup(String groupName, String objectId, String attributeName, String privilegeName) throws CSException;
 
 	/**
 	 * The method checks the permission for a {@link Group} for a given {@link ProtectionElement}. The 
 	 * {@link ProtectionElement} is obtained using the object id only. 
 	 * The userName is used to to obtain the Group object. Then the check
 	 * permission operation is performed to see if the group has the required access or not.
	 * @param groupName The group name which is trying to perform the operation
 	 * @param objectId The object id of the protection element on which the user wants to perform the operation
 	 * @param privilegeName The operation which the user wants to perform on the protection element
 	 * 
 	 * @return boolean Returns true if the user has permission to perform the operation on that particular resource
 	 * @throws CSException If there are any errors while checking for permission
 	 */
 	public boolean checkPermissionForGroup(String groupName, String objectId, String privilegeName) throws CSException;
 	
 	/**
 	 * The method returns a list of all the {@link Group} which has permission to perform 
 	 * the passed {@link Privilege} for the passed {@link ProtectionElement}. This method queries 
 	 * the database using the passed objectId and the privilege and obtains all the groups which have access over.
 	 * @param objectId The object id of the protection element on which the operation is performed
 	 * @param privilegeName The operation which is performed on the protection element
 	 * 
 	 * @return List Returns the list of {@link Group} which as access permission
 	 * @throws CSException If there are any errors while retrieving the accessbile groups
 	 */
 	public List getAccessibleGroups(String objectId, String privilegeName) throws CSException;
 
 	/**
 	 * The method returns a list of all the {@link Group} which has permission to perform 
 	 * the passed {@link Privilege} for the passed {@link ProtectionElement}. This method queries 
 	 * the database using the passed objectId and the privilege and obtains all the groups which have access over.
 	 * @param objectId The object id of the protection element on which the operation is performed
 	 * @param attributeName The attribute of the protection element on  on which the operation is performed
 	 * @param privilegeName The operation which is performed on the protection element
 	 * 
 	 * @return List Returns the list of {@link Group} which as access permission
 	 * @throws CSException If there are any errors while retrieving the accessbile groups
 	 */
 	public List getAccessibleGroups(String objectId, String attributeName, String privilegeName) throws CSException;
 	
 	/**
 	 * The method returns all the principals for the user.
 	 * @param userName The user name whose principals we are trying to obtain
 	 * 
 	 * @return Principal[] The <code>JAAS</code> Principals for the given user
 	 */
 	public Principal[] getPrincipals(String userName);
 
 	/**
 	 * Retrieves the Protection Element object from the database for the passed Object Id
 	 * @param objectId The object Id of the Protection Element which is to be retrieved from the Database
 	 * 
 	 * @return ProtectionElement The Protection Element object which is returned from the database for the passed Object Id
 	 * @throws CSObjectNotFoundException if the Protection Element object is not found for the given object id
 	 */
 	public ProtectionElement getProtectionElement(String objectId)throws CSObjectNotFoundException;
 	/**
 	 * Returns the Protection Element object from the database for the passed Protection Element Id
 	 * @param protectionElementId The id of the Protection Element object which is to be obtained
 	 * 
 	 * @return ProtectionElement The Protection Element object from the database for the passed Protection Element id
 	 * @throws CSObjectNotFoundException if the Protection Element object is not found for the given id
 	 */
 	public ProtectionElement getProtectionElementById(String protectionElementId) throws CSObjectNotFoundException;
 
 	/**
 	 * Assigns a ProtectionElement to the Protection Group. The Protection Element is first retrieved using the passed
 	 * Object Id from the database. Similarly the Protection Group is retrieved using the name passed.
 	 * Then both of these entities are associated in the database. If there is any error in the association then a 
 	 * {@link CSTransactionException} is thrown. These errors could be raised if it isnt able to retrieve either the 
 	 * Protection Element or Protection Group for the given data or there are any errors in the actual assignment
 	 * @param protectionGroupName The name of the Protection Group to which the Protection Element is to be associated
 	 * @param protectionElementObjectId The object Id of the Protection Element to which the Protection Group is to be associated
 	 * 
 	 * @throws CSTransactionException If it isnt able to retrieve either the  Protection Element or Protection Group 
 	 * for the given data or there are any errors in the actual assignment.
 	 */
 	public void assignProtectionElement(String protectionGroupName, String protectionElementObjectId)throws CSTransactionException;
 
 	/**
 	 * The methods sets the ownership of a given {@link ProtectionElement} to the given {@link User}. The protectio element
 	 * is obtained using the object id and attribute name passed and the user is retrieved using the user name passed
 	 * @param userName The user name of the user who is to become the owner of the protection element
 	 * @param protectionElementObjectId The object id of the protection element which is to be associated with the user
 	 * @param protectionElementAttributeName The attribute name of the protection element which is to be associated with the user
 	 * 
 	 * @throws CSTransactionException If there were issues in either obtaining the {@link ProtectionElement} or the {@link User} or in the
 	 * actual assignment of ownership
 	 */
 	public void setOwnerForProtectionElement(String userName, String protectionElementObjectId, String protectionElementAttributeName)throws CSTransactionException;
 
 	
 
 	/**
 	 * Accepts the applicationContextName to initialize the AuthorizationManager
 	 * @param applicationContextName The name of the application Context which is used to instantiate this Authorization Manager
 	 * 
 	 */
 	public void initialize(String applicationContextName);
 
 	/**
 	 * This methods return a List of all the {@link ProtectionGroup} for the current application.
 	 * @return List List of all the Protection Groups for the current application
 	 */
 	public java.util.List getProtectionGroups();
 	
 	/**
 	 * This method returns the {@link ProtectionElement} for a given objectId and attributeName
 	 * @param objectId The object id of the protection element to be obtained
 	 * @param attributeName The attribute name of the protection element to be obtained
 	 * @return ProtectionElement Returns the {@link ProtectionElement} if found else null
 	 */
 	public ProtectionElement getProtectionElement(String objectId,String attributeName);
 	
 	/**
 	 *  The secure object method can be used only with objects which follow the java beans specifications.
 	 *  This method assumes that Object passed has all the public getter and setter methods. The method checks
 	 *  permission for every attribute of the object for the passed userName. If the user does not have read
 	 *  permission on the few attributes, then they will appear as null.
 	 * @param userName The user name for the User who is trying to access the object
 	 * @param obj The Java Bean data object whose attribute needs to be protected.
 	 * @return Object The mutated Java Bean object which is a copy of the original object. This object doesnt not contain
 	 * values for the attributes on which the user doesnot have permission
 	 * @throws CSException If there is any problem the checking for access permissions for securing the object
 	 */
 	public Object secureObject(String userName, Object obj) throws CSException;
 	
 	/**
 	 * This methods works the same way as the <code>secureObject</code> except that it accepts an collection of Objects
 	 * rather then single instance
 	 * @param userName The user name for the User who is trying to access the object
 	 * @param objects The collection Java Bean data objects whose attribute needs to be protected.
 	 * @return Collection The collection mutated Java Bean objects which are a copy of the original objects. These objects doesnt not contain
 	 * values for the attributes on which the user doesnot have permission.
 	 * @throws CSException If there is any problem the checking for access permissions for securing the objects
 	 */
 	public Collection secureCollection(String userName,Collection objects) throws CSException;
 	
 	/**
 	 * Returns the Assigned Protection Groups for a particular Protection Element. The Protection Element is obtained from the Protection Element Id passed
 	 * @param protectionElementId The id of the Protection Element object whose associated Protection Groups are to be obtained
 	 * 
 	 * @return Set The list of the associated Protection Groups for the User
 	 * @throws CSObjectNotFoundException if the Protection Element object is not found for the given id
 	 */
 	public Set getProtectionGroups(String protectionElementId) throws CSObjectNotFoundException;
 	
 	/**
 	 * The method returns a Collection of {@link ObjectPrivilegeMap}. For every passed <code>ProtectionElement</code>,the 
 	 * method looks for the privileges that this {@link User} have on the {@link ProtectionElement}
 	 * @param userName The user name for the User who is trying to access the object
 	 * @param protectionElements Collection of Protection Elements for which the access priveleges are to be retrieved for the given user
 	 * @return Collection The collection of {@link ObjectPrivilegeMap}
 	 * @throws CSException If there is any problem obtaining the access permissions for securing the objects
 	 */
 	public Collection getPrivilegeMap(String userName,Collection protectionElements) throws CSException;
 	
 	
 	/**
 	 * This method accepts the original as well as the mutated object. It then retrieved the Attribute level privilege using
 	 * the object's name. Using this privilege access map it checks if the attributes on which the {@link User} doesnt have 
 	 * access has been modified in the mutated object. If found then it replaces the value of the same back from the original
 	 * object. Thus this methods doesnt allow modification of attributes of an object on which the user doesnt have access.
 	 * This new object is returned which contains the changed values only for those attributes on which the User has access.
 	 * @param userName The user name of the {@link User} which is trying to update the object
 	 * @param originalObject The original data object as it was read from the data base
 	 * @param mutatedObject The data object which contains the changes which the user has made
 	 * @return The object which contains the changed values only for those attributes on which the User has access.
 	 * @throws CSException If there is any problem obtaining the access permissions for securing the objects
 	 */
 	public Object secureUpdate(String userName, Object originalObject,Object mutatedObject) throws CSException;
 	
 	/**
 	 * The methods checks if the given {@link User} is owner of the {@link ProtectionElement}. The {@link  User} object is
 	 * obtained using the user name provided . The {@link ProtectionElement} is retrieved using the object id passed. Even though
 	 * if the user has ownership on a particular attribute of the object then he has ownership on the entire object
 	 * @param userName The user for which the ownership for the protection element is to be determined
 	 * @param protectionElementObjectId The object id of the protection element for which the ownership is to be determined 
 	 * @return True if the user has ownership on the protection element else False
 	 */
 	public boolean checkOwnership(String userName, String protectionElementObjectId);
 	
 	/**
 	 * This method is used to set the Audit user info. This method should be used
 	 * only if you are creating a new Authentication Manager for each user. Else
 	 * just set the user info using the <code>UserInfoHelper</code> class directly 
 	 * @param userName The name of the user accessing the Authentication Service
 	 * @param sessionId The session id of the user trying to access the Authentication Service
 	 */
 	public void setAuditUserInfo(String userName, String sessionId);
 	
 
 	/**
 	 * This method is used to indicate if encryption is enabled or not for user passwords
 	 * stored in the database. 
 	 *  
 	 * @param isEncryptionEnabled boolean value 
 	 */
 	public void setEncryptionEnabled(boolean isEncryptionEnabled);
 
 	
 	/**
 	 * This method returns the Application Object for which the manager is obtained. This 
 	 * method uses the application Context name passed and retrieves the application object
 	 * from the database.
 	 * @param applicationContextName the name of the application which is to be retrieved from the database.
 	 * @return Application object for the application context name using which this manager was obtained.
 	 * @throws CSObjectNotFoundException 
 	 */
 	public Application getApplication(String applicationContextName) throws CSObjectNotFoundException;
 	
 }
 
