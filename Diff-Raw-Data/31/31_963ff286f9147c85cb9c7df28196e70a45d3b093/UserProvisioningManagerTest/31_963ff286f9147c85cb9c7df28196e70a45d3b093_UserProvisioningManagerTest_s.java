 package test.gov.nih.nci.security;
 
 
 
 import gov.nih.nci.security.SecurityServiceProvider;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.authorization.jaas.AccessPermission;
 import gov.nih.nci.security.dao.GroupSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 
 import java.security.Principal;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
 
 
 import junit.framework.TestCase;
 
 public class UserProvisioningManagerTest extends TestCase {
 
 	private UserProvisioningManager userProvisioningManager; 
 	
 	//Test Set-up variables
 	private int NumberOfApplicationsToTest			= 5;
 	private int NumberOfUsersToTest 				= 10;
 	private int NumberOfGroupsToTest 				= 10;
 	private int NumberOfPrivilegesToTest 			= 5;
 	private int NumberOfProtectionElementsToTest 	= 50;  //Must be larger than NumberOfProtectionGroupsToTest?
 	private int NumberOfProtectionGroupsToTest 		= 10;
 	private int NumberOfRolesToTest 				= 5;
 	private String	   StrangeCharacters			= new String("");  //-\\\\=[]\\;//,./{}:\"<>?-+/*&&||==.");
 	//TODO: Get single quote and other characters above figured out, so they work
 	
 	//Test text comparison arrays - initialized in Setup() and used to confirm every text related field in each object (user, PE, etc.)
 	//TODO: change all variable names to be more meaningful and easier to read (NumberOf = nbr, UserStringArray = UserTextFields
 	private String[][] UserStringArray 				= new String[NumberOfUsersToTest]				[9];
 	private String[][] RoleStringArray 				= new String[NumberOfRolesToTest]				[2];
 	private String[][] GroupStringArray 			= new String[NumberOfGroupsToTest]				[2];
 	private String[][] ApplicationStringArray 		= new String[NumberOfApplicationsToTest+3]		[2];
 	private String[][] PrivilegeStringArray 		= new String[NumberOfPrivilegesToTest]			[2];
 	private String[][] ProtectionElementStringArray = new String[NumberOfProtectionElementsToTest+3]	[4];
 	private String[][] ProtectionGroupStringArray 	= new String[NumberOfProtectionGroupsToTest]	[4];
 	private String[][] PG_PERelationship			= new String[NumberOfProtectionGroupsToTest]	[NumberOfProtectionElementsToTest];
 	private String[][] Group_UserRelationship		= new String[NumberOfGroupsToTest]				[NumberOfUsersToTest];
 	//private String[][] Privilege_RoleRelationship	= new String[NumberOfPrivilegesToTest+7]		[NumberOfRolesToTest];
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		System.setProperty("gov.nih.nci.security.configFile", "C:/securityConfig/ApplicationSecurityConfig.xml");
		userProvisioningManager = SecurityServiceProvider.getUserProvisioningManager("csmupt");
 
 		//Initialize the userList - used to check the "get" functions
 		InitializeUserStringArray();
 		InitializeRoleStringArray();
 		InitializeGroupStringArray();
 		InitializeApplicationStringArray();
 		InitializePrivilegeStringArray();
 		InitializeProtectionElementStringArray();
 		InitializeProtectionGroupStringArray();
 
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testRun() throws CSException {
 		
 		//Order of Execution
 		
 		//Create Objects
 		this.testCreateApplication();
 		this.testCreateUser();
 		this.testCreatePrivilege();
 		this.testCreateProtectionElement();
 		this.testCreateRole();
 		this.testCreateGroup();
 		this.testCreateProtectionGroup();
 
 		
 		//Confirm Created (only tests basic data - strings, IDs, and flags)
 		this.testGetApplicationById();
 		this.testGetUser();
 		this.testGetUserById();
 		this.testGetPrivilegeById();
 		this.testGetProtectionElementById();
 		this.testGetProtectionElementString();
 		this.testGetProtectionElementStringString();
 		this.testGetRoleById();
 		this.testGetGroupById();
 		this.testGetProtectionGroupById();
 		this.testGetProtectionGroups();
 		this.testGetProtectionElementString();
 
 		this.testGetObjects();
 		//this.testGetApplicationContext(); TODO			
 		//Assigns each user 1 group
 		this.testAssignUserToGroup();
 		this.testGetGroups();
 		
 		//Remove the associations just made, then remake associations where first user
 		// 		gets assigned to all the groups, second to all groups - 1, etc.
 		this.testRemoveUserFromGroup(); 	// Removes only groups added by "testAssignUserToGroup()"
 		this.testAssignGroupsToUser();  	// Assigns multiple groups per most users
 		this.testGetGroupsMulti();			// Checks all user to group associations are correct
 		this.testGetPrincipals();			//NOT tested by UPT  
 
 		
 		// PE to PG Associations
 		this.testAssignProtectionElementStringStringString();	//This works, but is not dynamic, only assigns 1st PE to 1st PG
 		this.testRemoveAllAssignedProtectionElementsFromAllProtectionGroups();
 		this.testAssignToProtectionGroups();						//NOT tested by UPT  //Assigns all Protection Groups to all Protection Elements
 		//this.testRemoveAllAssignedProtectionElementsFromAllProtectionGroups();
 		this.testDeAssignProtectionElements();  				//BUGGED - posted in GForge
 		this.testRemoveAllAssignedProtectionElementsFromAllProtectionGroups();
 		this.testAssignProtectionElementStringString();  		//Needs above line before can execute //NOT Tested by UPT
 		this.testRemoveAllAssignedProtectionElementsFromAllProtectionGroups();
 
 		
 		//Assigns PE starting with all PE to first PG then decrementing the number assigned by one for each new PG, 
 		//			Also initializes PG_PERelationships array, used in testGetProtectionGroupsString()
 		this.testAssignProtectionElements();					//After this function executes is where the occational freeze occurs!!!!!!!!!
 		this.testGetProtectionGroupsString();					//Must be paired with testAssignProtectionElements()
 		
 		
 		
 		
 		//TODO: Enhance this method to ensure the CORRECT PEs are returned
 		this.testGetProtectionElements();						//This method only checks to make sure a valid set of PEs was returned
 		
 		//MODIFY
 		this.testModifyProtectionGroup();  				
 		this.testModifyProtectionElement();  				
 		this.testModifyRole();
 		this.testModifyApplication();
 		this.testModifyGroup();
 		this.testModifyPrivilege();
 		this.testModifyUser();
 		
 		//Associate Privileges to Role
 		this.testAssignPrivilegesToRole();
 		this.testGetPrivileges();  			
 		
 		this.testAssignUserRoleToProtectionGroup();					//NOT tested by UPT
		this.testRemoveUserRoleFromProtectionGroup();				//NOT tested by UPT
 		this.testAssignParentProtectionGroup();						//NOT tested by UPT
 		
 		//this.testSetOwnerForProtectionElementStringStringArray();	//NOT tested by UPT  //Needs work
 		this.testAssignOwners();									//TODO: make this more dynamic, only assigns one PE to each user
 		this.testSetOwnerForProtectionElementStringStringString();	//NOT tested by UPT  //Assigns users 2 to PE 2 //TODO: make more diverse
 		
 			  	
 		
 		this.testCheckOwnership();									//NOT tested by UPT
 		this.testGetOwners();										//Suggested by Kunal
  
 		
 		this.testGetPrivilegeMap();	//NOT tested by UPT  
 		
		this.testRemoveProtectionElementsFromProtectionGroup(); 
 		this.testAssignGroupRoleToProtectionGroup();
 		this.testGetProtectionElementPrivilegeContextForUser();
 		this.testGetProtectionElementPrivilegeContextForGroup();  // Requires this.testAssignGroupRoleToProtectionGroup()
 
 		this.testGetProtectionGroupRoleContextForGroup();
 		this.testGetProtectionGroupRoleContextForUser();
 		this.testRemoveGroupFromProtectionGroup();
 		this.testRemoveUserFromProtectionGroup();

 		this.testRemoveGroupRoleFromProtectionGroup(); 
     	
 		//DELETE EVERYTHING
 		this.testRemoveGroup();
 		this.testRemovePrivilege();
 		this.testRemoveRole();
 		this.testRemoveProtectionElement();
 		this.testRemoveProtectionGroup();
 		this.testRemoveUser();
     	this.testRemoveApplication();
 
 		
 		//---------------------------------------------------------------------------------------------------
 		//Still to be done
 		//---------------------------------------------------------------------------------------------------
 
 //		this.testSecureUpdate();									//NOT tested by UPT
 		/* 	Parameters for Secure Update()
 		 * 	userName - The user name of the User which is trying to update the object
 		 	originalObject - The original data object as it was read from the data base
 		 	mutatedObject - The data object which contains the changes which the user has made */
 //		this.testSecureCollection();								//NOT tested by UPT
 //		this.testSecureObject();									//NOT tested by UPT
 		//this.testCheckPermissionAccessPermissionSubject();					
 		//this.testCheckPermissionStringStringString();
     	//this.testCheckPermissionStringStringStringString();			
 
 
 		
 	}	
 	
 	
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.finalize()'
 	 */
 	
 	private void testRemoveProtectionElementsFromProtectionGroup() {
 		
 		String tempString=null;
 		for(int i=0; i<NumberOfProtectionGroupsToTest; i++){
 			// Retrieve List of ProtectionElement for the ProtectionGroup
 			Set protectionElements = null;
 			ProtectionGroup tempProtectionGroup=null;
 			try {
 				tempString = Integer.toString(i+1);
 				tempProtectionGroup= userProvisioningManager.getProtectionGroupById(tempString);
 				protectionElements = userProvisioningManager.getProtectionElements(tempProtectionGroup.getProtectionGroupId().toString());
 			} catch (CSObjectNotFoundException e) {
 				e.printStackTrace();
 				fail("Unable to retrieve Protection Group.");
 			}
 			if(protectionElements==null || protectionElements.size()<=0) 
 				fail("Protection Group does not have any associated Protection Elements.");
 				
 			
 			int x = 0;
 			String[] protectionElementIds = new String[protectionElements.size()];
 			Iterator iterator = protectionElements.iterator();
 			while(iterator.hasNext()){
 				ProtectionElement pe = (ProtectionElement)iterator.next();
 				protectionElementIds[x++] = pe.getProtectionElementId().toString();
 			}
 			
 			// Remove ProtectionElements from ProtectionGroup
 			try {
 				userProvisioningManager.removeProtectionElementsFromProtectionGroup(tempProtectionGroup.getProtectionGroupId().toString(),protectionElementIds);
 			} catch (CSTransactionException e) {
 				e.printStackTrace();
 				fail("Unable to remove ProtectionElements from Protection Group.");
 			}
 
 			
 		}
 		
 		
 	}
 
 	private void testFinalize() {
 
 	}
 
 
 	  // This is not yet implemented
 	/*private void testAddUserToGroup() throws CSTransactionException 
 	{
 		//User tempUser;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)  //NumberOfUsersToTest; x++)
 		{
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			//tempUser = userProvisioningManager.getUser(UserStringArray[x][0]);  //UserStringArray[x][0] contains user login name
 		
 			tempString = Integer.toString(x+1);
 			userProvisioningManager.addUserToGroup(tempString, tempString);  //Adds User 1 to Group 1, etc.
 		}
 	}*/
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.userProvisioningManager(String)'
 	 */
 	
 	private void testUserProvisioningManagerImpl() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createProtectionGroup(ProtectionGroup)'
 	 */
 	
 	private void testCreateProtectionGroup() throws CSTransactionException 
 	{
 		
 		byte tempFlag = 0;
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			ProtectionGroup tempProtectionGroup = new ProtectionGroup();
 			java.util.Date CurrentTime = new java.util.Date();
 	
 			tempProtectionGroup.setProtectionGroupName(ProtectionGroupStringArray[x][0]);
 			tempProtectionGroup.setProtectionGroupDescription(ProtectionGroupStringArray[x][1]);
 			tempProtectionGroup.setUpdateDate(CurrentTime);
 			
 			tempProtectionGroup.setLargeElementCountFlag(tempFlag);
 			if (tempFlag == 1)
 				tempFlag = 0;
 			else
 				tempFlag = 1;
 			
 			userProvisioningManager.createProtectionGroup(tempProtectionGroup);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getUser(String)'
 	 */
 	private void testGetUser() 
 	{
 		User tempUser;
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)  //NumberOfUsersToTest; x++)
 		{
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			tempUser = userProvisioningManager.getUser(UserStringArray[x][0]);  //UserStringArray[x][0] contains user login name
 
 			AssertEqualsForUsers(x, tempUser);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.setAuthorizationDAO(AuthorizationDAO)'
 	 */
 	
 	private void testSetAuthorizationDAO() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyProtectionGroup(ProtectionGroup)'
 	 */
 
 	private void testModifyProtectionGroup() throws CSTransactionException, CSObjectNotFoundException 
 	{
 		byte tempFlag = 0;
 		ProtectionGroup tempProtectionGroup = new ProtectionGroup();
 		java.util.Date tempDate = new java.util.Date();
 		
 		tempProtectionGroup = userProvisioningManager.getProtectionGroupById("4");
 		
 		tempProtectionGroup.setProtectionGroupName(ProtectionGroupStringArray[3][0] + "Modified");
 		tempProtectionGroup.setProtectionGroupDescription(ProtectionGroupStringArray[3][1] + "Modified");
 		tempProtectionGroup.setUpdateDate(tempDate);	
 		tempProtectionGroup.setLargeElementCountFlag(tempFlag);
 		
 		userProvisioningManager.modifyProtectionGroup(tempProtectionGroup);
 		
 		tempProtectionGroup = userProvisioningManager.getProtectionGroupById("4");
 		assertEquals("\nModifyProtectionGroup did not modify the Group Name\n", ProtectionGroupStringArray[3][0] + "Modified", tempProtectionGroup.getProtectionGroupName());
 		assertEquals("\nModifyProtectionGroup did not modify the Group Description\n", ProtectionGroupStringArray[3][1] + "Modified", tempProtectionGroup.getProtectionGroupDescription());
 		assertEquals("\nModifyProtectionGroup did not modify the Large Element Count Flag\n", tempFlag, tempProtectionGroup.getLargeElementCountFlag());
 		//assertEquals("\nModifyProtectionGroup did not modify the UpdateDate\n", tempDate, tempProtectionGroup.getUpdateDate());
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignProtectionElement(String, String, String)'
 	 */
 
 	private void testAssignProtectionElementStringStringString() throws CSTransactionException 
 	{
 		userProvisioningManager.assignProtectionElement(ProtectionGroupStringArray[0][0], ProtectionElementStringArray[0][2], ProtectionElementStringArray[0][3]);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeProtectionGroup(String)'
 	 */
 
 	private void testRemoveProtectionGroup() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfProtectionGroupsToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			userProvisioningManager.removeProtectionGroup(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeProtectionElement(String)'
 	 */
 
 	private void testRemoveProtectionElement() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfProtectionElementsToTest; x++)
 		{
 			tempString = Integer.toString(x+3);
 			userProvisioningManager.removeProtectionElement(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.setOwnerForProtectionElement(String, String[])'
 	 */
 	
 	private void testSetOwnerForProtectionElementStringStringArray() throws CSTransactionException 
 	{
 		//Adds all PEs to all Users
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			for (int y=0; y<NumberOfProtectionElementsToTest; y++)
 			{
 				userProvisioningManager.setOwnerForProtectionElement(UserStringArray[x][0], ProtectionElementStringArray[y][2], ProtectionElementStringArray[y][3]);
 			}
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignUserRoleToProtectionGroup(String, String[], String)'
 	 */
 	
 	private void testAssignUserRoleToProtectionGroup() throws CSTransactionException 
 	{
 		//This method adds all roles to all groups for each user
 		String[] tempRoleIDs = new String[NumberOfRolesToTest];
 		
 		for (int x=0; x<NumberOfRolesToTest; x++)
 		{
 			tempRoleIDs[x] = x+1+"";
 		}
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			for (int y=NumberOfGroupsToTest; y>0; y--)
 			{
 				userProvisioningManager.assignUserRoleToProtectionGroup(x+1+"", tempRoleIDs, y+"");
 			}	
 		}
 		
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.deAssignProtectionElements(String, String)'
 	 */
 
 	//  Removes all PE associations from the first PG
 	
 	private void testDeAssignProtectionElements() throws CSTransactionException, CSObjectNotFoundException 
 	{
 		//TODO: Make this dynamic (see commented code below)
 		userProvisioningManager.deAssignProtectionElements("TestProtectionGroupName0", "TestProtectionElementObjectID0");
 		//userProvisioningManager.deAssignProtectionElements(ProtectionGroupStringArray[0][0], ProtectionElementStringArray[0][2]);
 //		for (int x=0; x < NumberOfProtectionElementsToTest; x++)
 //		{	
 //			//userProvisioningManager.deAssignProtectionElements(ProtectionGroupStringArray[0][0], ProtectionElementStringArray[x][2]);
 //		}
 
 		
 		//Cycle through each PE and make sure each element and remove each association
 //		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 //		{
 //			Set SetOfProtectionGroups = userProvisioningManager.getProtectionGroups(Integer.toString(x+1));
 //			assertNotNull("\ngetProtectionGroups(String) returned NULL\n", SetOfProtectionGroups);
 //			if ((SetOfProtectionGroups != null) && (!SetOfProtectionGroups.isEmpty())) 
 //			{
 //				Iterator i = SetOfProtectionGroups.iterator();
 //				while (i.hasNext()) 
 //				{
 //					ProtectionGroup tempProtectionGroup = (ProtectionGroup) i.next();
 //					
 //					userProvisioningManager.deAssignProtectionElements(tempProtectionGroup.getProtectionGroupName(),ProtectionElementStringArray[x][2]);
 //				}
 //			}
 //		}
 		
 		//		int tempPECounter = NumberOfProtectionElementsToTest;
 //		
 //		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 //		{
 //			if ( tempPECounter == 0 ) 
 //				tempPECounter = NumberOfProtectionElementsToTest;
 //
 //			for (int y=0; y<tempPECounter; y++)
 //			{
 //				if (ProtectionGroupStringArray[x][0] != null && ProtectionElementStringArray[y][2] != null)
 //					userProvisioningManager.deAssignProtectionElements(ProtectionGroupStringArray[x][0], ProtectionElementStringArray[y][2]);
 //			}
 //			tempPECounter--;
 //		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createProtectionElement(ProtectionElement)'
 	 */
 	private void testCreateProtectionElement() throws CSTransactionException 
 	{
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			ProtectionElement tempProtectionElement = new ProtectionElement();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempProtectionElement.setProtectionElementName(ProtectionElementStringArray[x][0]);
 			tempProtectionElement.setProtectionElementDescription(ProtectionElementStringArray[x][1]);
 			tempProtectionElement.setObjectId(ProtectionElementStringArray[x][2]);
 			tempProtectionElement.setAttribute(ProtectionElementStringArray[x][3]);
 			tempProtectionElement.setUpdateDate(CurrentTime);
 			
 			userProvisioningManager.createProtectionElement(tempProtectionElement);
 			
 			tempProtectionElement = null;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeUserRoleFromProtectionGroup(String, String, String[])'
 	 */
 	
 	private void testRemoveUserRoleFromProtectionGroup() throws CSTransactionException 
 	{
 		//Removes all PGs and Roles from user 1
 		String[] tempRoleIDs = new String[NumberOfRolesToTest];
 		
 		for (int x=0; x<NumberOfRolesToTest; x++)
 		{
 			tempRoleIDs[x] = x+1+"";
 		}
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			userProvisioningManager.removeUserRoleFromProtectionGroup(x+1+"", "2", tempRoleIDs);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createRole(Role)'
 	 */
 	private void testCreateRole() throws CSTransactionException 
 	{
 		byte tempFlag = 0;
 		
 		for ( int x=0; x<NumberOfRolesToTest; x++)
 		{
 			Role tempRole = new Role();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempRole.setName(RoleStringArray[x][0]);
 			tempRole.setDesc(RoleStringArray[x][1]);
 			tempRole.setUpdateDate(CurrentTime);
 			
 			tempRole.setActive_flag(tempFlag);
 			if (tempFlag == 1)
 				tempFlag = 0;
 			else
 				tempFlag = 1;
 			
 			userProvisioningManager.createRole(tempRole);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.checkPermission(AccessPermission, Subject)'
 	 */
 	
 	private void testCheckPermissionAccessPermissionSubject() throws CSException 
 	{
 		AccessPermission AP = null;
 		userProvisioningManager.checkPermission(AP, UserStringArray[0][0]);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyRole(Role)'
 	 */
 
 	private void testModifyRole() throws CSObjectNotFoundException, CSTransactionException 
 	{
 		Role tempRole = new Role();
 		byte tempFlag = 0;
 		java.util.Date midnight_jan2_1970 = new java.util.Date(24L*60L*60L*1000L);
 		
 		tempRole = userProvisioningManager.getRoleById("4");
 		
 		tempRole.setName(RoleStringArray[3][0] + "Modified");
 		tempRole.setDesc(RoleStringArray[3][1] + "Modified");
 		tempRole.setUpdateDate(midnight_jan2_1970);
 		tempRole.setActive_flag(tempFlag);
 		
 		userProvisioningManager.modifyRole(tempRole);
 		
 		tempRole = userProvisioningManager.getRoleById("4");
 		
 		assertEquals("\nmodifyRole did not modify the Role Name\n", RoleStringArray[3][0] + "Modified", tempRole.getName());
 		assertEquals("\nmodifyRole did not modify the Role Description\n", RoleStringArray[3][1] + "Modified", tempRole.getDesc());
 		assertEquals("\nmodifyRole did not modify the Role Active Flag\n", tempFlag, tempRole.getActive_flag());
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.checkPermission(AccessPermission, String)'
 	 */
 	
 	private void testCheckPermissionAccessPermissionString() {
 
 	}
 
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeRole(String)'
 	 */
 
 	private void testRemoveRole() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfRolesToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			userProvisioningManager.removeRole(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.checkPermission(String, String, String, String)'
 	 */
 	
 	private void testCheckPermissionStringStringStringString() throws CSException 
 	{
 		boolean HasPermission = userProvisioningManager.checkPermission(UserStringArray[1][0], ProtectionElementStringArray[1][2], ProtectionElementStringArray[1][3], PrivilegeStringArray[0][0]);
 		assertEquals("\nIncorrect permission from the checkPermission method with 4 strings for parameters", true, HasPermission);
 		
 		HasPermission = userProvisioningManager.checkPermission(UserStringArray[NumberOfUsersToTest-1][0], ProtectionElementStringArray[1][2], ProtectionElementStringArray[1][3], PrivilegeStringArray[0][0]);
 		assertEquals("\nIncorrect permission from the checkPermission method with 4 strings for parameters - user should not have permission", false, HasPermission);
 		
 		HasPermission = userProvisioningManager.checkPermission("asdfasdfasdfasdfasdfasdf", "asdfasdfasdfasdfasdfasdf", "asdfasdfasdfasdfasdfasdf", "asdfasdfasdfasdfasdfasdf");
 		assertEquals("\nIncorrect permission from the checkPermission method with 4 strings for parameters - user should not have permission", false, HasPermission);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createPrivilege(Privilege)'
 	 */
 	private void testCreatePrivilege() throws CSTransactionException {
 
 		for (int x=0; x<NumberOfPrivilegesToTest; x++)
 		{
 			Privilege tempPrivilege = new Privilege();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempPrivilege.setName(PrivilegeStringArray[x][0]);
 			tempPrivilege.setDesc(PrivilegeStringArray[x][1]);
 			tempPrivilege.setUpdateDate(CurrentTime);
 			userProvisioningManager.createPrivilege(tempPrivilege);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.checkPermission(String, String, String)'
 	 */
 	
 	private void testCheckPermissionStringStringString() throws CSException 
 	{
 		boolean HasPermission = userProvisioningManager.checkPermission(UserStringArray[0][0], ProtectionElementStringArray[0][2], PrivilegeStringArray[0][0]);
 		assertEquals("\nIncorrect permission from the checkPermission method with 3 strings for parameters", true, HasPermission);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyPrivilege(Privilege)'
 	 */
 
 	private void testModifyPrivilege() throws CSObjectNotFoundException, CSTransactionException 
 	{
 		Privilege tempPrivilege = new Privilege();
 		java.util.Date CurrentTime = new java.util.Date();
 		
 		tempPrivilege = userProvisioningManager.getPrivilegeById("11");
 		
 		tempPrivilege.setName(PrivilegeStringArray[3][0] + "Modified");
 		tempPrivilege.setDesc(PrivilegeStringArray[3][1] + "Modified");
 		tempPrivilege.setUpdateDate(CurrentTime);
 		
 		userProvisioningManager.modifyPrivilege(tempPrivilege);
 
 		tempPrivilege = userProvisioningManager.getPrivilegeById("11");
 		
 		assertEquals("\nmodifyPrivilege did not modify the Name\n", PrivilegeStringArray[3][0] + "Modified", tempPrivilege.getName());
 		assertEquals("\nmodifyPrivilege did not modify the Description\n", PrivilegeStringArray[3][1] + "Modified", tempPrivilege.getDesc());
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removePrivilege(String)'
 	 */
 
 	private void testRemovePrivilege() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfPrivilegesToTest; x++)
 		{
 			tempString = Integer.toString(x + 8);
 			userProvisioningManager.removePrivilege(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignPrivilegesToRole(String, String[])'
 	 */
 	private void testAssignPrivilegesToRole() throws CSTransactionException 
 	{
 		int NumberOfPrivilegesToAddToThisRole = NumberOfPrivilegesToTest+7;  
 
 		for (int x=0; x<NumberOfRolesToTest; x++)
 		{
 			if (NumberOfPrivilegesToAddToThisRole == 0) 
 				NumberOfPrivilegesToAddToThisRole = NumberOfPrivilegesToTest+7;	
 			String[] tempPrivilegesToAdd = new String[NumberOfPrivilegesToAddToThisRole];
 			for (int z=0; z<NumberOfPrivilegesToAddToThisRole; z++)
 			{
 				tempPrivilegesToAdd[z] = Integer.toString(z+1);
 				
 				//Populate array used to check the added associations
 				//Privilege_RoleRelationship[x][z] = Integer.toString(z+1); //Stores which Privilege_Role association exists, if blank (null) then it doesn't exist
 			}
 			userProvisioningManager.assignPrivilegesToRole(Integer.toString(x+1), tempPrivilegesToAdd);
 			
 			NumberOfPrivilegesToAddToThisRole--;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionElement(String)'
 	 */
 
 	private void testGetProtectionElementString() throws CSObjectNotFoundException 
 	{
 		ProtectionElement tempProtectionElement;
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempProtectionElement = userProvisioningManager.getProtectionElement(ProtectionElementStringArray[x][2]);  //By Object ID
 			AssertEqualsForProtectionElements(x, tempProtectionElement);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionElementById(String)'
 	 */
 
 	private void testGetProtectionElementById() throws CSObjectNotFoundException 
 	{
 		ProtectionElement tempProtectionElement;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempString = Integer.toString(x+3);
 			
 			//Id is assigned by database automatically, so this test must be done with a fresh database
 			tempProtectionElement = userProvisioningManager.getProtectionElementById(tempString);
 			
 			AssertEqualsForProtectionElements(x, tempProtectionElement);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignProtectionElement(String, String)'
 	 */
 
 	
 	private void testAssignProtectionElementStringString() throws CSTransactionException
 	{
 		// Pulls the Group Name and PEObjectID from the initialization string.
 		// TODO: Update to be more dynamic.
 		userProvisioningManager.assignProtectionElement(ProtectionGroupStringArray[0][0], ProtectionElementStringArray[0][2]);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createGroup(Group)'
 	 */
 	private void testCreateGroup() throws CSTransactionException 
 	{
 		
 		for (int x=0; x<NumberOfGroupsToTest; x++)
 		{
 			Group tempGroup = new Group();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempGroup.setGroupName(GroupStringArray[x][0]);
 			tempGroup.setGroupDesc(GroupStringArray[x][1]);
 			tempGroup.setUpdateDate(CurrentTime);
 			
 			userProvisioningManager.createGroup(tempGroup);
 		}
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.setOwnerForProtectionElement(String, String, String)'
 	 */
 	
 	private void testSetOwnerForProtectionElementStringStringString() throws CSTransactionException 
 	{
 		userProvisioningManager.setOwnerForProtectionElement(UserStringArray[1][0], ProtectionElementStringArray[2][2], ProtectionElementStringArray[2][3]);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeGroup(String)'
 	 */
 	//
 	private void testRemoveGroup() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfGroupsToTest; x++)
 		{
 			tempString = Integer.toString(x + 1);
 			userProvisioningManager.removeGroup(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyGroup(Group)'
 	 */
 
 	private void testModifyGroup() throws CSTransactionException, CSObjectNotFoundException 
 	{
 		Group tempGroup = new Group();
 		java.util.Date CurrentTime = new java.util.Date();
 		
 		tempGroup = userProvisioningManager.getGroupById("4");
 		
 		tempGroup.setGroupName(GroupStringArray[3][0] + "Modified");
 		tempGroup.setGroupDesc(GroupStringArray[3][1] + "Modified");
 		tempGroup.setUpdateDate(CurrentTime);
 		
 		userProvisioningManager.modifyGroup(tempGroup);
 		
 		tempGroup = userProvisioningManager.getGroupById("4");
 		
 		assertEquals("\nmodifyGroup did not modify Group Name\n", GroupStringArray[3][0] + "Modified", tempGroup.getGroupName());
 		assertEquals("\nmodifyGroup did not modify Group Description\n", GroupStringArray[3][1] + "Modified", tempGroup.getGroupDesc());
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignGroupsToUser(String, String[])'
 	 */
 
 	// This test assigns all possible groups to the first user and increments DOWNWARD with each user
 	private void testAssignGroupsToUser() throws CSTransactionException 
 	{
 		int NumberOfGroupsToAddToThisUser = NumberOfGroupsToTest;
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			if (NumberOfGroupsToAddToThisUser == 0) 
 				NumberOfGroupsToAddToThisUser = NumberOfGroupsToTest;	
 			String[] tempGroupsToAdd = new String[NumberOfGroupsToAddToThisUser];
 			for (int z=0; z<NumberOfGroupsToAddToThisUser; z++)
 			{
 				tempGroupsToAdd[z] = Integer.toString(z+1);
 				
 				//Populate array used to check the added associations
 				Group_UserRelationship[z][x] = Integer.toString(z+1); //Stores which Group_User association exists, if blank (null) then it doesn't exist
 			}
 			userProvisioningManager.assignGroupsToUser(Integer.toString(x+1), tempGroupsToAdd);
 			
 			NumberOfGroupsToAddToThisUser--;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeUserFromGroup(String, String)'
 	 */
 
 	private void testRemoveUserFromGroup() throws CSTransactionException, CSObjectNotFoundException 
 	{
 		int y = 0;
 		String tempStringGroupID = "";
 		String tempStringUserID = "";
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			if (y == NumberOfGroupsToTest) y = 0;			
 			tempStringGroupID = Integer.toString(y + 1);
 			tempStringUserID = Integer.toString(x + 2);		
 			userProvisioningManager.removeUserFromGroup(tempStringGroupID, tempStringUserID);
 			y++;
 		}
 		
 		//Confirm that all associations are removed
 		String tempString = "";
 
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			Set SetOfGroups = userProvisioningManager.getGroups(tempString);
 	
 			assertTrue("Group was not cleared from User" + x, SetOfGroups.isEmpty());
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignGroupRoleToProtectionGroup(String, String, String[])'
 	 */
 	
 	private void testAssignGroupRoleToProtectionGroup() throws CSTransactionException 
 	{
		String[]ArrayOfRoles = {"1", "2", "3"};
		userProvisioningManager.assignGroupRoleToProtectionGroup("1", "1", ArrayOfRoles); //PGID, GID, RoleID array of strings
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getPrivilegeById(String)'
 	 */
 
 	private void testGetPrivilegeById() throws CSObjectNotFoundException 
 	{
 		Privilege tempPrivilege;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfPrivilegesToTest; x++)
 		{
 			tempString = Integer.toString(x+8); //7 Privileges already loaded
 			
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			tempPrivilege = userProvisioningManager.getPrivilegeById(tempString);
 			
 			assertEquals("\nIncorrect Privilege Name\n", PrivilegeStringArray[x][0], tempPrivilege.getName() );
 			assertEquals("\nIncorrect Privilege Desc\n", PrivilegeStringArray[x][1], tempPrivilege.getDesc() );
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeUserFromProtectionGroup(String, String)'
 	 */
 	
 	private void testRemoveUserFromProtectionGroup() {
 		String tempString=null;
 		for(int i=0; i<NumberOfProtectionGroupsToTest; i++){
 			
 			for(int j=0; j<NumberOfUsersToTest; j++){
 				try {
 					userProvisioningManager.removeUserFromProtectionGroup(String.valueOf(i+1),String.valueOf(j+1));
 				} catch (CSTransactionException e) {
 					e.printStackTrace();
 					fail("Unable to remove User from Protection Group.");
 				}
 			}
 			
 
 			
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeGroupRoleFromProtectionGroup(String, String, String[])'
 	 */
 	
 	private void testRemoveGroupRoleFromProtectionGroup() throws CSTransactionException 
 	{
 		String[] tempRoleIDs = new String[NumberOfRolesToTest];
 		
 		for (int x=0; x<NumberOfRolesToTest; x++)
 		{
 			tempRoleIDs[x] = x+1+"";
 		}
 		
 		userProvisioningManager.removeGroupRoleFromProtectionGroup("1", "1", tempRoleIDs);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeGroupFromProtectionGroup(String, String)'
 	 */
 	
 	private void testRemoveGroupFromProtectionGroup() {
 		try {
 			userProvisioningManager.removeGroupFromProtectionGroup("1", "1");
 		} catch (CSTransactionException e) {
 			e.printStackTrace();
 			fail("Unable to remove Group from Protection Group");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionGroupById(String)'
 	 */
 
 	private void testGetProtectionGroupById() throws CSObjectNotFoundException 
 	{
 		ProtectionGroup tempProtectionGroup;
 		String tempString = "";
 		byte tempFlag = 0;
 		
 		for(int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			//java.util.Date CurrentTime = new java.util.Date();
 			tempString = Integer.toString(x+1);
 			tempProtectionGroup = userProvisioningManager.getProtectionGroupById(tempString);
 			
 			assertEquals("\nIncorrect Protection Group Name\n", ProtectionGroupStringArray[x][0], tempProtectionGroup.getProtectionGroupName() );
 			assertEquals("\nIncorrect Protection Group Desc\n", ProtectionGroupStringArray[x][1], tempProtectionGroup.getProtectionGroupDescription() );
 			//assertEquals("\nIncorrect Update Date\n", CurrentTime, tempRole.getUpdateDate() );
 			assertEquals("\nIncorrect LargeElementCountFlag\n", tempFlag, tempProtectionGroup.getLargeElementCountFlag() );
 			if (tempFlag == 1)
 				tempFlag = 0;
 			else
 				tempFlag = 1;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getRoleById(String)'
 	 */
 	
 	private void testGetRoleById() throws CSObjectNotFoundException 
 	{
 		Role tempRole;
 		String tempString = "";
 		byte tempFlag = 0;
 		
 		for(int x=0; x<NumberOfRolesToTest; x++)
 		{
 			//java.util.Date CurrentTime = new java.util.Date();
 			tempString = Integer.toString(x+1);
 			tempRole = userProvisioningManager.getRoleById(tempString);
 			
 			assertEquals("\nIncorrect Role Name\n", RoleStringArray[x][0], tempRole.getName() );
 			assertEquals("\nIncorrect Role Desc\n", RoleStringArray[x][1], tempRole.getDesc() );
 			//TODO: Confirm dates in general (we don't store time anyway, so should be easy)
 			//assertEquals("\nIncorrect Update Date\n", CurrentTime, tempRole.getUpdateDate() );
 			assertEquals("\nIncorrect Active_Flag\n", tempFlag, tempRole.getActive_flag() );
 			if (tempFlag == 1)
 				tempFlag = 0;
 			else
 				tempFlag = 1;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getPrivileges(String)'
 	 */
 	
 	private void testGetPrivileges() throws CSObjectNotFoundException 
 	{
 		
 		Set tempPrivilegesSet = (Set) userProvisioningManager.getPrivileges("1");
 		
 		if (tempPrivilegesSet.size() != (long)NumberOfPrivilegesToTest+7 || tempPrivilegesSet == null || tempPrivilegesSet.isEmpty())
 	    {
 	    	String tempString = "";
 	    	tempString = "\nThe Number of Privileges associated to this Role is different than expected\nExpected: " + NumberOfPrivilegesToTest + "\nActual: " + tempPrivilegesSet.size() + "\n";
 	   								 										
 	    	fail(tempString);
 	    }
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getObjects(SearchCriteria)'
 	 */
 	
 	private void testGetObjects() {
 
 		 Group group = new Group();
          group.setGroupName("TestGroupName%");
          SearchCriteria searchCriteria = new GroupSearchCriteria(group);
          Collection totalGroups = (Collection)userProvisioningManager.getObjects(searchCriteria);
          assertEquals("\nIncorrect Number of Objects\n",NumberOfGroupsToTest, totalGroups.size());
          
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createUser(User)'
 	 */
 	private void testCreateUser() throws CSTransactionException {
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			User tempUser = new User();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempUser.setLoginName(UserStringArray[x][0]);
 			tempUser.setFirstName(UserStringArray[x][1]);
 			tempUser.setLastName(UserStringArray[x][2]);
 			tempUser.setDepartment(UserStringArray[x][3]);
 			tempUser.setEmailId(UserStringArray[x][4]);
 			tempUser.setOrganization(UserStringArray[x][5]);
 			tempUser.setPassword(UserStringArray[x][6]);
 			tempUser.setTitle(UserStringArray[x][7]);
 			tempUser.setPhoneNumber(UserStringArray[x][8]);
 			
 			tempUser.setEndDate(CurrentTime);
 			tempUser.setStartDate(CurrentTime);
 			tempUser.setUpdateDate(CurrentTime);
 
 			userProvisioningManager.createUser(tempUser);
 			//userList.add(x,tempUser);
 			
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignProtectionElements(String, String[])'
 	 * This test adds all of the protection elements to the first protection group, allPE-1 to the next PG, etc.
 	 */
 
 	private void testAssignProtectionElements() throws CSTransactionException 
 	{
 		int tempPECounter = NumberOfProtectionElementsToTest;
 		
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			if ( tempPECounter == 0 ) 
 				tempPECounter = NumberOfProtectionElementsToTest;
 			String[] tempProtectionElementsArray = new String[tempPECounter]; //Min value = 1, max = NumberOfProtectionElementsToTest
 			for (int y=0; y<tempPECounter; y++)
 			{
 				tempProtectionElementsArray[y] = Integer.toString(y+1); //+1 because that is ID stored in database
 				
 				// Propogate PG_PERelationship array (see getProtectionGroups() for use
 				PG_PERelationship[x][y] = Integer.toString(y);  //Don't add +1 for the ID, just assume the +1
 			}
 			userProvisioningManager.assignProtectionElements(Integer.toString(x+1), tempProtectionElementsArray);
 		
 			tempPECounter--;
 		}
 	}
 
 	private void testRemoveAllAssignedProtectionElementsFromAllProtectionGroups() throws CSTransactionException
 	{
 		String[] tempProtectionElementsArray = new String[0];
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			userProvisioningManager.assignProtectionElements(Integer.toString(x+1), tempProtectionElementsArray);
 		}
 	}
 	
 	
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionGroupRoleContextForUser(String)'
 	 */
 	
 	private void testGetProtectionGroupRoleContextForUser() {
 		try {
 			Set set = userProvisioningManager.getProtectionGroupRoleContextForUser("3");
 			if(set==null || set.isEmpty()) fail("Unable to obtain  Protection Group Role Context for User.");
 		} catch (CSObjectNotFoundException e) {
 			e.printStackTrace();
 			fail("Unable to obtain Protection Group Role Context for User.");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionGroupRoleContextForGroup(String)'
 	 */
 	
 	private void testGetProtectionGroupRoleContextForGroup() {
 		try {
 			Set set = userProvisioningManager.getProtectionGroupRoleContextForGroup("1");
 			if(set==null || set.isEmpty()) fail("Unable to obtain  Protection Group Role Context for Group.");
 		} catch (CSObjectNotFoundException e) {
 			e.printStackTrace();
 			fail("Unable to obtain Protection Group Role Context for Group.");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionElementPrivilegeContextForUser(String)'
 	 */
 	
 	private void testGetProtectionElementPrivilegeContextForUser() {
 		try {
			Set set = userProvisioningManager.getProtectionElementPrivilegeContextForUser("1");
 			if(set==null || set.isEmpty()) fail("Unable to obtain  Protection Element Privilege Context for User.");
 		} catch (CSObjectNotFoundException e) {
 			e.printStackTrace();
 			fail("Unable to obtain Protection Element Privilege Context for User.");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionElementPrivilegeContextForGroup(String)'
 	 */
 	
 	private void testGetProtectionElementPrivilegeContextForGroup() {
 		try {
 			Set set = userProvisioningManager.getProtectionElementPrivilegeContextForGroup("1");
 			if(set==null || set.size()==0) 	fail("Unable to obtain ProtectionElementPrivilegeContext for Group.");
 		} catch (CSObjectNotFoundException e) {
 			e.printStackTrace();
 			fail("Unable to obtain ProtectionElementPrivilegeContext for Group.");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getGroupById(String)'
 	 */
 
 	private void testGetGroupById() throws CSObjectNotFoundException 
 	{
 		Group tempGroup;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfGroupsToTest; x++)  //NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			tempGroup = userProvisioningManager.getGroupById(tempString);
 			
 			assertEquals("\nIncorrect Group Name\n", GroupStringArray[x][0], tempGroup.getGroupName() );
 			assertEquals("\nIncorrect Group Desc\n", GroupStringArray[x][1], tempGroup.getGroupDesc() );
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyProtectionElement(ProtectionElement)'
 	 */
 
 	private void testModifyProtectionElement() throws CSTransactionException, CSObjectNotFoundException 
 	{
 		ProtectionElement tempProtectionElement = new ProtectionElement();
 		java.util.Date midnight_jan2_1970 = new java.util.Date(24L*60L*60L*1000L);
 
 		tempProtectionElement = userProvisioningManager.getProtectionElementById("4");
 		
 		tempProtectionElement.setProtectionElementName(ProtectionElementStringArray[3][0] + "Modified");
 		tempProtectionElement.setProtectionElementDescription(ProtectionElementStringArray[3][1] + "Modified");
 		tempProtectionElement.setObjectId(ProtectionElementStringArray[3][2] + "Modified");
 		tempProtectionElement.setAttribute(ProtectionElementStringArray[3][3] + "Modified");
 		tempProtectionElement.setUpdateDate(midnight_jan2_1970);	//TODO: Not updating the "Update Date"
 		
 		userProvisioningManager.modifyProtectionElement(tempProtectionElement); 
 		
 		tempProtectionElement = userProvisioningManager.getProtectionElementById("4");
 		
 		assertEquals("\nmodifyProtectionElement did not modify the Name\n", ProtectionElementStringArray[3][0] + "Modified", tempProtectionElement.getProtectionElementName());
 		assertEquals("\nmodifyProtectionElement did not modify the Description\n", ProtectionElementStringArray[3][1] + "Modified", tempProtectionElement.getProtectionElementDescription());
 		assertEquals("\nmodifyProtectionElement did not modify the Object ID\n", ProtectionElementStringArray[3][2] + "Modified", tempProtectionElement.getObjectId());
 		assertEquals("\nmodifyProtectionElement did not modify the Attribute\n", ProtectionElementStringArray[3][3] + "Modified", tempProtectionElement.getAttribute());
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getUserById(String)'
 	 */
 	
 	private void testGetUserById() throws CSObjectNotFoundException 
 	{
 		User tempUser;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+2);
 			
 			//Id is assigned by database automatically, so this test must be done with a fresh database
 			tempUser = userProvisioningManager.getUserById(tempString); 
 			
 			AssertEqualsForUsers(x, tempUser);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyUser(User)'
 	 */
 
 	private void testModifyUser() throws CSObjectNotFoundException, CSTransactionException 
 	{
 		User tempUser = new User();
 		java.util.Date CurrentTime = new java.util.Date();
 		
 		tempUser = userProvisioningManager.getUserById("4");
 		
 		tempUser.setLoginName(UserStringArray[3][0] + "Modified");
 		tempUser.setFirstName(UserStringArray[3][1] + "Modified");
 		tempUser.setLastName(UserStringArray[3][2] + "Modified");
 		tempUser.setDepartment(UserStringArray[3][3] + "Modified");
 		tempUser.setEmailId(UserStringArray[3][4] + "Modified");
 		tempUser.setOrganization(UserStringArray[3][5] + "Modified");
 		tempUser.setPassword(UserStringArray[3][6] + "Modified");
 		tempUser.setTitle(UserStringArray[3][7] + "Modified");
 		//TODO: Make this phone number modification more flexable if you have time
 		tempUser.setPhoneNumber(UserStringArray[3][8] + "Mod");  //Database truncates this if try to go over max, so have to use 3 characters.  Reduce if add more characters in create
 		
 		tempUser.setEndDate(CurrentTime);
 		tempUser.setStartDate(CurrentTime);
 		tempUser.setUpdateDate(CurrentTime);
 
 		userProvisioningManager.modifyUser(tempUser);		
 		
 		tempUser = userProvisioningManager.getUserById("4");
 		
 		assertEquals("\nmodifyUser did not modify Login Name\n", UserStringArray[3][0] + "Modified", tempUser.getLoginName());
 		assertEquals("\nmodifyUser did not modify First Name\n", UserStringArray[3][1] + "Modified", tempUser.getFirstName());
 		assertEquals("\nmodifyUser did not modify Last Name\n", UserStringArray[3][2] + "Modified", tempUser.getLastName());
 		assertEquals("\nmodifyUser did not modify Department\n", UserStringArray[3][3] + "Modified", tempUser.getDepartment());
 		assertEquals("\nmodifyUser did not modify Email ID\n", UserStringArray[3][4] + "Modified", tempUser.getEmailId());
 		assertEquals("\nmodifyUser did not modify Organization\n", UserStringArray[3][5] + "Modified", tempUser.getOrganization());
 		assertEquals("\nmodifyUser did not modify Password\n", UserStringArray[3][6] + "Modified", tempUser.getPassword());
 		assertEquals("\nmodifyUser did not modify Title\n", UserStringArray[3][7] + "Modified", tempUser.getTitle());
 		assertEquals("\nmodifyUser did not modify Phone Number\n", UserStringArray[3][8] + "Mod", tempUser.getPhoneNumber());
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeUser(String)'
 	 */
 
 	private void testRemoveUser() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+2);
 			userProvisioningManager.removeUser(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getGroups(String)'
 	 */
 	//This test only checks the User/Group associations added by the blasdsdasdf() method
 	private void testGetGroups() throws CSObjectNotFoundException 
 	{
 		String tempString = "";
 		long y = 0;
 		
 		//Cycle through each user, assigning to a group
 		//		If the NumberOfGroupsToTest is larger than the NumberOfUsersToTest, then 
 		//		not all groups will be assigned to a user but who cares
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			if (y == NumberOfGroupsToTest) y=0;
 			tempString = Integer.toString(x+1);
 			Set SetOfGroups = userProvisioningManager.getGroups(tempString);
 			
 			assertNotNull("\ntestGetGroups returned no groups for user " + x, SetOfGroups);
 			if ((null != SetOfGroups) && (!SetOfGroups.isEmpty())) 
 			{
 				Iterator i = SetOfGroups.iterator();
 				while (i.hasNext()) 
 				{
 					Group tempGroup = (Group) i.next();
 					tempString = "\nIncorrect GroupID assigned to User with ID = " + Integer.toString(x+1) + "\n";
 					//Only reason this works like this is because there is always only one group assigned right now
 					//TODO: change this so it assigns differently, and checks here differently
 					assertEquals(tempString, y, ((Long)tempGroup.getGroupId()).longValue() );
 				}
 			}
 			y++;
 		}
 	}
 	
 	private void testGetGroupsMulti() throws CSObjectNotFoundException
 	{
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{			
 			Set SetOfGroups = userProvisioningManager.getGroups(Integer.toString(x+1));
 			assertNotNull("No groups were returned with testGetGroupsDecrementing()\n", SetOfGroups);
 			if ((null != SetOfGroups) && (!SetOfGroups.isEmpty())) 
 			{
 				Iterator i = SetOfGroups.iterator();
 				//For each group assigned to this user, check Group_UserRelationship array to see if the relationship is supposed to exist
 				while (i.hasNext()) 
 				{
 					Group tempGroup = (Group) i.next();
 					
 					//Get the Group's ID minus one, so it corresponds to the array element associated with it
 					int tempGroupID = Integer.parseInt(Long.toString(((Long)tempGroup.getGroupId()).longValue() -1) );	
 					if (Group_UserRelationship[tempGroupID][x].length() == 0)
 					{
 						tempString = "\nIncorrect Group assigned to User with ID = " + Integer.toString(x+1) + "\n";
 						fail(tempString);
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionElements(String)'
 	 */
 	
 	private void testGetProtectionElements() throws CSObjectNotFoundException 
 	{
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			Set tempPESet = (Set) userProvisioningManager.getProtectionElements(x+1+"");
 			
 		    Iterator i = tempPESet.iterator();
 		    while (i.hasNext())
 		    {
 		    	ProtectionElement tempPE = (ProtectionElement) i.next();
 		    	AssertEqualsForProtectionElements2(tempPE.getProtectionElementId().intValue(), tempPE);
 		    }
 		}
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionGroups(String)'
 	 */
 
 	//	 Use PG_PERelationship Array to check that PE and PG associations are correct
 	private void testGetProtectionGroupsString() throws CSObjectNotFoundException 
 	{
 		String tempString = "";	
 		
 		//Cycle through each PE and make sure each element has the proper groups associated with it
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			Set SetOfProtectionGroups = userProvisioningManager.getProtectionGroups(Integer.toString(x+1));
 			assertNotNull("\ngetProtectionGroups(String) returned NULL\n", SetOfProtectionGroups);
 			if ((SetOfProtectionGroups != null) && (!SetOfProtectionGroups.isEmpty())) 
 			{
 				Iterator i = SetOfProtectionGroups.iterator();
 				while (i.hasNext()) 
 				{
 					ProtectionGroup tempProtectionGroup = (ProtectionGroup) i.next();
 					
 					//Get the PG's ID minus one, so it corresponds to the array element associated with it
 					int tempPGID = Integer.parseInt(Long.toString(((Long)tempProtectionGroup.getProtectionGroupId()).longValue() -1));		
 					boolean PEExistsWithinPG = false;
 
 					//Look within each PG for the PE, if not there than this test fails
 					//This loop is unecessary because if PG_PERelationship[tempPGID][x] is not null, then the relationship exists
 					//	but it works, so I'm not going to change it.
 					for (int y=0; y<NumberOfProtectionElementsToTest; y++)
 					{
 						if (PG_PERelationship[tempPGID][y] != null)
 						{
 							if ( PG_PERelationship[tempPGID][y].equals(Integer.toString(x)) )
 								PEExistsWithinPG = true;
 						}
 					}
 					tempString = "\nIncorrect PG assigned to PE with ID = " + (x+1) + "\n";
 					assertTrue(tempString, PEExistsWithinPG);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignToProtectionGroups(String, String[])'
 	 */
 	
 	private void testAssignToProtectionGroups() throws CSTransactionException 
 	{
 		//This method adds all of the Protection Groups to all of the protection elements
 		String[] tempStringArrayOfProtectionGroups = new String[NumberOfProtectionGroupsToTest];
 		
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			tempStringArrayOfProtectionGroups[x] = x+1+"";
 		}
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			userProvisioningManager.assignToProtectionGroups(x+1+"", tempStringArrayOfProtectionGroups);	
 		}
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignParentProtectionGroup(String, String)'
 	 */
 	
 	private void testAssignParentProtectionGroup() throws CSTransactionException 
 	{
 		for (int x=NumberOfProtectionGroupsToTest; x > 1; x--)
 		{
 			userProvisioningManager.assignParentProtectionGroup(x+"", x-1+"");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.createApplication(Application)'
 	 */
 	private void testCreateApplication() throws CSTransactionException 
 	{
 		byte tempFlag = 0;
 		for (int x=0; x<NumberOfApplicationsToTest; x++)
 		{
 			Application tempApplication = new Application();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempApplication.setApplicationName(ApplicationStringArray[x][0]);
 			tempApplication.setApplicationDescription(ApplicationStringArray[x][1]);
 			tempApplication.setUpdateDate(CurrentTime);
 			tempApplication.setActiveFlag(tempFlag);
 			if (tempFlag == 1)
 				tempFlag = 0;
 			else
 				tempFlag = 1;
 
 			userProvisioningManager.createApplication(tempApplication);
 			tempApplication = null;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.modifyApplication(Application)'
 	 */
 
 	private void testModifyApplication() throws CSObjectNotFoundException, CSTransactionException 
 	{
 		Application tempApplication = new Application();
 		byte tempFlag = 0;
 		java.util.Date midnight_jan2_1970 = new java.util.Date(0);
 		
 		tempApplication = userProvisioningManager.getApplicationById("3");
 		
 		tempApplication.setApplicationName(ApplicationStringArray[2][0] + "Modified");
 		tempApplication.setApplicationDescription(ApplicationStringArray[2][1] + "Modified");
 		tempApplication.setUpdateDate(midnight_jan2_1970);  //TODO: I have no way of testing this without a BIG long wait
 		tempApplication.setActiveFlag(tempFlag);
 		
 		userProvisioningManager.modifyApplication(tempApplication);
 		
 		tempApplication = userProvisioningManager.getApplicationById("3");
 		
 		assertEquals("\nmodifyApplication did not modify Application name\n", ApplicationStringArray[2][0] + "Modified", tempApplication.getApplicationName());
 		assertEquals("\nmodifyApplication did not modify Application Description\n", ApplicationStringArray[2][1] + "Modified", tempApplication.getApplicationDescription());
 		assertEquals("\nmodifyApplication did not modify Application Active Flag\n", tempFlag, tempApplication.getActiveFlag());
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.removeApplication(String)'
 	 */
 
 	private void testRemoveApplication() throws CSTransactionException 
 	{
 		String tempString = "";
 		for (int x=0; x < NumberOfApplicationsToTest; x++)
 		{
 			tempString = Integer.toString(x+3);
 			userProvisioningManager.removeApplication(tempString);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getApplicationById(String)'
 	 */
 
 	private void testGetApplicationById() throws CSObjectNotFoundException 
 	{
 		String tempString = "";
 		Application tempApplication;
 		byte tempFlag = 0;
 		
 		for (int x=0; x<NumberOfApplicationsToTest; x++)
 		{
 			tempString = Integer.toString(x+3);  //Loading script loads an app in the database, so 2 is my first id I've created
 			tempApplication = userProvisioningManager.getApplicationById(tempString);
 			
 			assertEquals("\nIncorrect Application Name\n", ApplicationStringArray[x][0], tempApplication.getApplicationName() );
 			assertEquals("\nIncorrect Application Description\n", ApplicationStringArray[x][1], tempApplication.getApplicationDescription() );
 			assertEquals("\nIncorrect ActiveFlag\n", tempFlag, tempApplication.getActiveFlag() );
 			if (tempFlag == 1)
 				tempFlag = 0;
 			else
 				tempFlag = 1;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignOwners(String, String[])'
 	 */
 	
 	private void testAssignOwners() throws CSTransactionException 
 	{
 		// Assigns all users to PE #1
 		String[] tempUserIDs = new String[NumberOfUsersToTest];
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			tempUserIDs[x] = x+1+"";
 		}
 		
		userProvisioningManager.assignOwners("1", tempUserIDs);
	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getOwners(String)'
 	 */
 	
 	private void testGetOwners() {
 		try {
			Set set = userProvisioningManager.getOwners("1");
 			set.size();
 			
 		} catch (CSObjectNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getApplicationContext()'
 	 */
 	
 	private void testGetApplicationContext() {
 		ApplicationContext ac = userProvisioningManager.getApplicationContext();
 		if(ac ==null ){
 			fail("Application Context could not be obtained.");
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getPrincipals(String)'
 	 */
 	
 	private void testGetPrincipals() 
 	{
 		Principal[] tempPrincipal = userProvisioningManager.getPrincipals(UserStringArray[0][0]);
 		assertNotNull("\nThe pricipal returned was NULL.\n", tempPrincipal);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionGroups()'
 	 */
 
 	//Gets all PG, and checks the text of each group and the number of PG records returned to make sure they are correct
 	private void testGetProtectionGroups() throws CSObjectNotFoundException 
 	{
 		List tempPGList = userProvisioningManager.getProtectionGroups();
 
 		
 	    Iterator i = tempPGList.iterator();
 	    while (i.hasNext())
 	    {
 	    	ProtectionGroup tempProtectionGroup = (ProtectionGroup) i.next();
 	    	AssertEqualsForTextInProtectionGroup(Integer.parseInt(Long.toString(((Long)tempProtectionGroup.getProtectionGroupId()).longValue() - 1)), tempProtectionGroup);
 	 
 	    }
 
 		if (tempPGList.size() != (long)NumberOfProtectionGroupsToTest || tempPGList == null || tempPGList.isEmpty())
 	    {
 	    	String tempString = "";
 	    	tempString = "\nThe Number of PGs in the DB is different than origionally created\nExpected: " + NumberOfProtectionGroupsToTest + "\nActual: " + tempPGList.size() + "\n";
 	   								 										
 	    	fail(tempString);
 	    }
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getProtectionElement(String, String)'
 	 */
 
 	private void testGetProtectionElementStringString() 
 	{
 		ProtectionElement tempProtectionElement;
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempProtectionElement = userProvisioningManager.getProtectionElement(ProtectionElementStringArray[x][2], ProtectionElementStringArray[x][3]);
 			
 			AssertEqualsForProtectionElements(x, tempProtectionElement);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.secureObject(String, Object)'
 	 */
 	
 	private void testSecureObject() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.secureCollection(String, Collection)'
 	 */
 	
 	private void testSecureCollection() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.getPrivilegeMap(String, Collection)'
 	 */
 	
 	private void testGetPrivilegeMap() throws CSException 
 	{
 		ArrayList tempPEArrayList = new ArrayList();
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempPEArrayList.add((ProtectionElement)userProvisioningManager.getProtectionElementById(x+1+""));
 		}
 		Collection tempResults = userProvisioningManager.getPrivilegeMap(UserStringArray[1][0], tempPEArrayList);
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.secureUpdate(String, Object, Object)'
 	 */
 	
 	private void testSecureUpdate() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.checkOwnership(String, String)'
 	 */
 	
 	private void testCheckOwnership() 
 	{
 		assertEquals("\nIncorrect Ownership\n", true, userProvisioningManager.checkOwnership(UserStringArray[1][0], ProtectionElementStringArray[2][2]));
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.assignUserToGroup(String, String)'
 	 */
 	
 	private void testAssignUserToGroup() throws CSTransactionException 
 	{
 		int y = 0;
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			if (y == NumberOfGroupsToTest) y=0;
 			userProvisioningManager.assignUserToGroup(UserStringArray[x][0], GroupStringArray[y][0] );
 			y++;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.userProvisioningManager.setAuditUserInfo(String, String)'
 	 */
 	
 	private void testSetAuditUserInfo() 
 	{
 		userProvisioningManager.setAuditUserInfo(UserStringArray[0][0], "sessionid");
 	}
 	
 	//Setup UserStringArray
 	private void InitializeUserStringArray()
 	{
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{	
 			UserStringArray[x][0] = "TestUserLoginName" + StrangeCharacters + x;
 			UserStringArray[x][1] = "TestUserFirstName" + StrangeCharacters + x;
 			UserStringArray[x][2] = "TestUserLastName" + StrangeCharacters + x;
 			UserStringArray[x][3] = "TestUserDepartment" + StrangeCharacters + x;
 			UserStringArray[x][4] = "TestUserEmailID" + x + "@TestUserEmailID" + x + ".com";//TODO: Make a seperate StrangeCharacters variable to test this
 			UserStringArray[x][5] = "TestUserOrganizationName" + StrangeCharacters + x;
 			UserStringArray[x][6] = "TestUserPassword" + StrangeCharacters + x;
 			UserStringArray[x][7] = "TestUserTitle" + StrangeCharacters + x;
 			UserStringArray[x][8] = new String("###-###-####").replace('#',Integer.toString(x).charAt(0));
 		}
 	}
 	private void AssertEqualsForUsers (int iteration, User tempUser)
 	{
 		long tempLong;
 		tempLong = iteration + 1;
 		//Assertions to check for proper data
 		assertEquals("\nIncorrect Login Name\n", 		UserStringArray[iteration][0], tempUser.getLoginName());
 		assertEquals("\nIncorrect First Name\n", 		UserStringArray[iteration][1], tempUser.getFirstName());
 		assertEquals("\nIncorrect Last Name\n", 		UserStringArray[iteration][2], tempUser.getLastName());
 		assertEquals("\nIncorrect Department\n", 		UserStringArray[iteration][3], tempUser.getDepartment());
 		assertEquals("\nIncorrect EmailId\n", 			UserStringArray[iteration][4], tempUser.getEmailId());
 		assertEquals("\nIncorrect Organization Name\n", UserStringArray[iteration][5], tempUser.getOrganization());
 		assertEquals("\nIncorrect Password\n", 			UserStringArray[iteration][6], tempUser.getPassword());
 		assertEquals("\nIncorrect Title\n", 			UserStringArray[iteration][7], tempUser.getTitle());
 		assertEquals("\nIncorrect Phone Number\n", 		UserStringArray[iteration][8], tempUser.getPhoneNumber());
 		assertEquals("\nIncorrect User ID\n",			tempLong+1					 , ((Long)tempUser.getUserId()).longValue());
 	}
 	
 	private void AssertEqualsForProtectionElements (int iteration, ProtectionElement tempProtectionElement)
 	{
 		long tempLong;
 		tempLong = (long)iteration;
 		
 		assertEquals("\nIncorrect Protection Element Name\n",		 ProtectionElementStringArray[iteration][0], tempProtectionElement.getProtectionElementName() );
 		assertEquals("\nIncorrect Protection Element Description\n", ProtectionElementStringArray[iteration][1], tempProtectionElement.getProtectionElementDescription() );
 		assertEquals("\nIncorrect Protection Element ObjectId\n", 	 ProtectionElementStringArray[iteration][2], tempProtectionElement.getObjectId() );
 		assertEquals("\nIncorrect Protection Element Attribute\n", ProtectionElementStringArray[iteration][3], tempProtectionElement.getAttribute() );
 		assertEquals("\nIncorrect Protection Element ID\n", 		 tempLong+3, ((Long)tempProtectionElement.getProtectionElementId()).longValue() );	
 	}
 	
 	private void AssertEqualsForProtectionElements2 (int iteration, ProtectionElement tempProtectionElement)
 	{
 		long tempLong;
 		tempLong = (long)iteration;
 		if(tempLong==1 || tempLong==2){
 			
 		}else{
 			assertEquals("\nIncorrect Protection Element Name\n",		 ProtectionElementStringArray[iteration-3][0], tempProtectionElement.getProtectionElementName() );
 			assertEquals("\nIncorrect Protection Element Description\n", ProtectionElementStringArray[iteration-3][1], tempProtectionElement.getProtectionElementDescription() );
 			assertEquals("\nIncorrect Protection Element ObjectId\n", 	 ProtectionElementStringArray[iteration-3][2], tempProtectionElement.getObjectId() );
 			assertEquals("\nIncorrect Protection Element Attribute\n", ProtectionElementStringArray[iteration-3][3], tempProtectionElement.getAttribute() );
 			assertEquals("\nIncorrect Protection Element ID\n", 		 tempLong, ((Long)tempProtectionElement.getProtectionElementId()).longValue() );
 		}
 	}
 	
 	private void AssertEqualsForTextInProtectionGroup(int iteration, ProtectionGroup tempPG)
 	{
 		assertEquals("\nIncorrect PG Name\n", ProtectionGroupStringArray[iteration][0], tempPG.getProtectionGroupName() );
 		assertEquals("\nIncorrect PG Description\n", ProtectionGroupStringArray[iteration][1], tempPG.getProtectionGroupDescription() );
 	}
 	
 	private void InitializeRoleStringArray()
 	{
 		for ( int x=0; x<NumberOfRolesToTest; x++)
 		{
 			//java.util.Date CurrentTime = new java.util.Date();
 			RoleStringArray[x][0] = "TestRoleName" + StrangeCharacters + x;
 			RoleStringArray[x][1] = "TestRoleDesc" + StrangeCharacters + x;
 		}
 	}
 	
 	private void InitializeGroupStringArray()
 	{
 		for (int x=0; x<NumberOfGroupsToTest; x++)
 		{
 			GroupStringArray[x][0] = "TestGroupName" + StrangeCharacters + x;
 			GroupStringArray[x][1] = "TestGroupDesc" + StrangeCharacters + x;
 		}
 	}
 	
 	private void InitializeApplicationStringArray()
 	{
 		for (int x=0; x<NumberOfApplicationsToTest; x++)
 		{
 			ApplicationStringArray[x][0] = "TestApplicationName" + StrangeCharacters + x;
 			ApplicationStringArray[x][1] = "TestApplicationDescription"  + StrangeCharacters + x;
 		}
 	}
 	
 	private void InitializePrivilegeStringArray()
 	{
 		for (int x=0; x<NumberOfPrivilegesToTest; x++)
 		{
 			PrivilegeStringArray[x][0] = "TestPrivilegeName" + StrangeCharacters + x;
 			PrivilegeStringArray[x][1] = "TestPrivilegeDesc" + StrangeCharacters + x;
 		}
 	}
 	
 	private void InitializeProtectionElementStringArray()
 	{
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			ProtectionElementStringArray[x][0] = "TestProtectionElementName" + StrangeCharacters + x;
 			ProtectionElementStringArray[x][1] = "TestProtectionElementDescription" + StrangeCharacters + x;
 			ProtectionElementStringArray[x][2] = "TestProtectionElementObjectID" + StrangeCharacters + x;
 			ProtectionElementStringArray[x][3] = "TestProtectionElementAttribute" + StrangeCharacters + x;
 		}
 	}
 	
 	private void InitializeProtectionGroupStringArray()
 	{
 		for (int x=0; x<NumberOfProtectionGroupsToTest; x++)
 		{
 			ProtectionGroupStringArray[x][0] = "TestProtectionGroupName" + StrangeCharacters + x;
 			ProtectionGroupStringArray[x][1] = "TestProtectionGroupDesc" + StrangeCharacters + x;
 		}
 	}
 	
 //	private void AssertGroupAssignmentsToUsers(int x, int y, Group tempGroupsForUserX)
 //	{
 //		static int tempy = 0;
 //		
 //		assertEquals((Group)tempGroupsForUserX)
 //	}
 }
 
