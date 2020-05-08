 package test.gov.nih.nci.security.provisioning;
 
 

 import java.util.Iterator;
 import java.util.Set;
 import java.util.List;


 //import java.util.TreeSet;
 
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import gov.nih.nci.security.provisioning.UserProvisioningManagerImpl;
 import junit.framework.TestCase;
 
 
 
 
 public class UserProvisioningManagerImplTest extends TestCase {
 
 	private UserProvisioningManagerImpl userProvisioningManagerImpl; 
 	
 	//Test Set-up variables
	private int NumberOfApplicationsToTest			= 500;
	private int NumberOfUsersToTest 				= 10000;
	private int NumberOfGroupsToTest 				= 1000;
	private int NumberOfPrivilegesToTest 			= 20;
	private int NumberOfProtectionElementsToTest 	= 5000;  //Must be larger than NumberOfProtectionGroupsToTest?
	private int NumberOfProtectionGroupsToTest 		= 100;
	private int NumberOfRolesToTest 				= 500;
 	private String	   StrangeCharacters			= new String("");       //~!@#$%^&*()_+1234567890");      //-=[]\\;//,./{}:\"<>?-+/*&&||==.");
 	//TODO: Get single quote and other characters above figured out, so they work
 	
 	//Test text comparison arrays - initialized in Setup() and used to confirm every text related field in each object (user, PE, etc.)
 	//TODO: change all variable names to be more meaningful and easier to read (NumberOf = nbr, UserStringArray = UserTextFields
 	private String[][] UserStringArray 				= new String[NumberOfUsersToTest]				[9];
 	private String[][] RoleStringArray 				= new String[NumberOfRolesToTest]				[2];
 	private String[][] GroupStringArray 			= new String[NumberOfGroupsToTest]				[2];
 	private String[][] ApplicationStringArray 		= new String[NumberOfApplicationsToTest]		[2];
 	private String[][] PrivilegeStringArray 		= new String[NumberOfPrivilegesToTest]			[2];
 	private String[][] ProtectionElementStringArray = new String[NumberOfProtectionElementsToTest]	[4];
 	private String[][] ProtectionGroupStringArray 	= new String[NumberOfProtectionGroupsToTest]	[4];
 	private String[][] PG_PERelationship			= new String[NumberOfProtectionGroupsToTest]	[NumberOfProtectionElementsToTest];
 	private String[][] Group_UserRelationship		= new String[NumberOfGroupsToTest]				[NumberOfUsersToTest];
 	
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		System.setProperty("gov.nih.nci.security.configFile", "C:/securityConfig/ApplicationSecurityConfig.xml");
 		userProvisioningManagerImpl = new UserProvisioningManagerImpl("TestApplication");
 
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
 
 	public void testRun() throws CSTransactionException, CSObjectNotFoundException {
 		
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
 		//TODO: Write an AssertEqualsForTextInXXX() for all of these "Get" functions below - make it as generic as possible (possibly one function)
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
 
 		//Assigns each user 1 group
 		this.testAssignUserToGroup();
 		this.testGetGroups();
 		
 		//Remove the associations just made, then remake associations where first user
 		// 		gets assigned to all the groups, second to all groups - 1, etc.
 		this.testRemoveUserFromGroup(); 	// Removes only groups added by "testAssignUserToGroup()"
 		this.testAssignGroupsToUser();  	// Assigns multiple groups per most users
 		this.testGetGroupsMulti();			// Checks all user to group associations are correct
 		
 		// PE to PG Associations
 		//Assigns PE starting with all PE to first PG then decrementing the number assigned by one for each new PG, 
 		//			Also initializes PG_PERelationships array, used in testGetProtectionGroupsString()
 		this.testAssignProtectionElements();
 		this.testGetProtectionGroupsString();
 		//this.testDeAssignProtectionElements();
 		
 		//this.testAssignProtectionElementStringString();
 		//this.testGetProtectionGroupsString();
 		
 		//this.testAssignProtectionElementStringStringString();
 		
 		//this.testAddUserToGroup();  //This is not yet implemented
 	}	
 	
 	
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.finalize()'
 	 */
 	@SuppressWarnings("unused")
 	private void testFinalize() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.addUserToGroup(String, String)'
 	 */
 	@SuppressWarnings("unused")  // This is not yet implemented
 	private void testAddUserToGroup() throws CSTransactionException 
 	{
 //		//User tempUser;
 //		String tempString = "";
 //		
 //		for (int x=0; x<NumberOfUsersToTest; x++)  //NumberOfUsersToTest; x++)
 //		{
 //			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 //			//tempUser = userProvisioningManagerImpl.getUser(UserStringArray[x][0]);  //UserStringArray[x][0] contains user login name
 //			
 //			tempString = Integer.toString(x+1);
 //			userProvisioningManagerImpl.addUserToGroup(tempString, tempString);  //Adds User 1 to Group 1, etc.
 //		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.UserProvisioningManagerImpl(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testUserProvisioningManagerImpl() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createProtectionGroup(ProtectionGroup)'
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
 			
 			userProvisioningManagerImpl.createProtectionGroup(tempProtectionGroup);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getUser(String)'
 	 */
 	private void testGetUser() 
 	{
 		User tempUser;
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)  //NumberOfUsersToTest; x++)
 		{
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			tempUser = userProvisioningManagerImpl.getUser(UserStringArray[x][0]);  //UserStringArray[x][0] contains user login name
 
 			AssertEqualsForUsers(x, tempUser);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.setAuthorizationDAO(AuthorizationDAO)'
 	 */
 	@SuppressWarnings("unused")
 	private void testSetAuthorizationDAO() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyProtectionGroup(ProtectionGroup)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignProtectionElement(String, String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignProtectionElementStringStringString() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeProtectionGroup(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeProtectionElement(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveProtectionElement() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.setOwnerForProtectionElement(String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testSetOwnerForProtectionElementStringStringArray() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignUserRoleToProtectionGroup(String, String[], String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignUserRoleToProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.deAssignProtectionElements(String, String)'
 	 */
 
 	//Deassign all PG_PE relationships in the same way they were added
	@SuppressWarnings("unused")
 	private void testDeAssignProtectionElements() throws CSTransactionException, CSObjectNotFoundException 
 	{
 		//Cycle through each PE and make sure each element and remove each association
 //		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 //		{
 //			Set SetOfProtectionGroups = userProvisioningManagerImpl.getProtectionGroups(Integer.toString(x+1));
 //			assertNotNull("\ngetProtectionGroups(String) returned NULL\n", SetOfProtectionGroups);
 //			if ((SetOfProtectionGroups != null) && (!SetOfProtectionGroups.isEmpty())) 
 //			{
 //				Iterator i = SetOfProtectionGroups.iterator();
 //				while (i.hasNext()) 
 //				{
 //					ProtectionGroup tempProtectionGroup = (ProtectionGroup) i.next();
 //					
 //					userProvisioningManagerImpl.deAssignProtectionElements(tempProtectionGroup.getProtectionGroupName(),ProtectionElementStringArray[x][2]);
 //				}
 //			}
 //		}
 					
 		userProvisioningManagerImpl.deAssignProtectionElements("TestProtectionGroupName0", "TestProtectionElementObjectID0");
 		
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
 //					userProvisioningManagerImpl.deAssignProtectionElements(ProtectionGroupStringArray[x][0], ProtectionElementStringArray[y][2]);
 //			}
 //			tempPECounter--;
 //		}
 		//TODO: confirm the deletion of all records in PG_PE
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createProtectionElement(ProtectionElement)'
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
 			
 			userProvisioningManagerImpl.createProtectionElement(tempProtectionElement);
 			
 			tempProtectionElement = null;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeUserRoleFromProtectionGroup(String, String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveUserRoleFromProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createRole(Role)'
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
 			
 			userProvisioningManagerImpl.createRole(tempRole);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.checkPermission(AccessPermission, Subject)'
 	 */
 	@SuppressWarnings("unused")
 	private void testCheckPermissionAccessPermissionSubject() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyRole(Role)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyRole() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.checkPermission(AccessPermission, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testCheckPermissionAccessPermissionString() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.initialize(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testInitialize() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeRole(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveRole() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.checkPermission(String, String, String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testCheckPermissionStringStringStringString() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createPrivilege(Privilege)'
 	 */
 	private void testCreatePrivilege() throws CSTransactionException {
 
 		for (int x=0; x<NumberOfPrivilegesToTest; x++)
 		{
 			Privilege tempPrivilege = new Privilege();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempPrivilege.setName(PrivilegeStringArray[x][0]);
 			tempPrivilege.setDesc(PrivilegeStringArray[x][1]);
 			tempPrivilege.setUpdateDate(CurrentTime);
 			userProvisioningManagerImpl.createPrivilege(tempPrivilege);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.checkPermission(String, String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testCheckPermissionStringStringString() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyPrivilege(Privilege)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyPrivilege() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removePrivilege(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemovePrivilege() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignPrivilegesToRole(String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignPrivilegesToRole() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionElement(String)'
 	 */
 
 	private void testGetProtectionElementString() throws CSObjectNotFoundException 
 	{
 		ProtectionElement tempProtectionElement;
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempProtectionElement = userProvisioningManagerImpl.getProtectionElement(ProtectionElementStringArray[x][2]);  //By Object ID
 			AssertEqualsForProtectionElements(x, tempProtectionElement);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionElementById(String)'
 	 */
 
 	private void testGetProtectionElementById() throws CSObjectNotFoundException 
 	{
 		ProtectionElement tempProtectionElement;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			
 			//Id is assigned by database automatically, so this test must be done with a fresh database
 			tempProtectionElement = userProvisioningManagerImpl.getProtectionElementById(tempString);
 			
 			AssertEqualsForProtectionElements(x, tempProtectionElement);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignProtectionElement(String, String)'
 	 */
 
	@SuppressWarnings("unused")
 	private void testAssignProtectionElementStringString() 
 	{
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createGroup(Group)'
 	 */
 	private void testCreateGroup() throws CSTransactionException {
 		
 		for (int x=0; x<NumberOfGroupsToTest; x++)
 		{
 			Group tempGroup = new Group();
 			java.util.Date CurrentTime = new java.util.Date();
 			tempGroup.setGroupName(GroupStringArray[x][0]);
 			tempGroup.setGroupDesc(GroupStringArray[x][1]);
 			tempGroup.setUpdateDate(CurrentTime);
 			
 			userProvisioningManagerImpl.createGroup(tempGroup);
 		}
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.setOwnerForProtectionElement(String, String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testSetOwnerForProtectionElementStringStringString() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeGroup(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyGroup(Group)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignGroupsToUser(String, String[])'
 	 */
 
 	// This test assigns all possible grous to the first user and increments DOWNWARD with each user
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
 			userProvisioningManagerImpl.assignGroupsToUser(Integer.toString(x+1), tempGroupsToAdd);
 			
 			NumberOfGroupsToAddToThisUser--;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeUserFromGroup(String, String)'
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
 			tempStringUserID = Integer.toString(x + 1);		
 			userProvisioningManagerImpl.removeUserFromGroup(tempStringGroupID, tempStringUserID);
 			y++;
 		}
 		
 		//Confirm that all associations are removed
 		String tempString = "";
 
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			Set SetOfGroups = userProvisioningManagerImpl.getGroups(tempString);
 	
 			assertTrue("Group was not cleared from User" + x, SetOfGroups.isEmpty());
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignGroupRoleToProtectionGroup(String, String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignGroupRoleToProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getPrivilegeById(String)'
 	 */
 
 	private void testGetPrivilegeById() throws CSObjectNotFoundException 
 	{
 		Privilege tempPrivilege;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfPrivilegesToTest; x++)
 		{
 			tempString = Integer.toString(x+8); //7 Privileges already loaded
 			
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			tempPrivilege = userProvisioningManagerImpl.getPrivilegeById(tempString);
 			
 			assertEquals("\nIncorrect Privilege Name\n", PrivilegeStringArray[x][0], tempPrivilege.getName() );
 			assertEquals("\nIncorrect Privilege Desc\n", PrivilegeStringArray[x][1], tempPrivilege.getDesc() );
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeUserFromProtectionGroup(String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveUserFromProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeGroupRoleFromProtectionGroup(String, String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveGroupRoleFromProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeGroupFromProtectionGroup(String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveGroupFromProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionGroupById(String)'
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
 			tempProtectionGroup = userProvisioningManagerImpl.getProtectionGroupById(tempString);
 			
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
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getRoleById(String)'
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
 			tempRole = userProvisioningManagerImpl.getRoleById(tempString);
 			
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
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getPrivileges(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetPrivileges() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getObjects(SearchCriteria)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetObjects() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createUser(User)'
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
 
 			userProvisioningManagerImpl.createUser(tempUser);
 			//userList.add(x,tempUser);
 			
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignProtectionElements(String, String[])'
 	 * This test adds 2 protection elements to each protection group
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
 			userProvisioningManagerImpl.assignProtectionElements(Integer.toString(x+1), tempProtectionElementsArray);
 		
 			tempProtectionElementsArray = null;
 			tempPECounter--;
 		}
 		
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeProtectionElementsFromProtectionGroup(String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveProtectionElementsFromProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionGroupRoleContextForUser(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetProtectionGroupRoleContextForUser() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionGroupRoleContextForGroup(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetProtectionGroupRoleContextForGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionElementPrivilegeContextForUser(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetProtectionElementPrivilegeContextForUser() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionElementPrivilegeContextForGroup(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetProtectionElementPrivilegeContextForGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getGroupById(String)'
 	 */
 
 	private void testGetGroupById() throws CSObjectNotFoundException 
 	{
 		Group tempGroup;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfGroupsToTest; x++)  //NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			
 			//Retrieve the User based off the login ID (see setup() for initialization of UserStringArray)
 			tempGroup = userProvisioningManagerImpl.getGroupById(tempString);
 			
 			assertEquals("\nIncorrect Group Name\n", GroupStringArray[x][0], tempGroup.getGroupName() );
 			assertEquals("\nIncorrect Group Desc\n", GroupStringArray[x][1], tempGroup.getGroupDesc() );
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyProtectionElement(ProtectionElement)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyProtectionElement() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getUserById(String)'
 	 */
 	
 	private void testGetUserById() throws CSObjectNotFoundException 
 	{
 		User tempUser;
 		String tempString = "";
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			tempString = Integer.toString(x+1);
 			
 			//Id is assigned by database automatically, so this test must be done with a fresh database
 			tempUser = userProvisioningManagerImpl.getUserById(tempString); 
 			
 			AssertEqualsForUsers(x, tempUser);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyUser(User)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyUser() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeUser(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveUser() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getGroups(String)'
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
 			Set SetOfGroups = userProvisioningManagerImpl.getGroups(tempString);
 			
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
 					assertEquals(tempString, y+1, (long)tempGroup.getGroupId() );
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
 			Set SetOfGroups = userProvisioningManagerImpl.getGroups(Integer.toString(x+1));
 			assertNotNull("No groups were returned with testGetGroupsDecrementing()\n", SetOfGroups);
 			if ((null != SetOfGroups) && (!SetOfGroups.isEmpty())) 
 			{
 				Iterator i = SetOfGroups.iterator();
 				//For each group assigned to this user, check Group_UserRelationship array to see if the relationship is supposed to exist
 				while (i.hasNext()) 
 				{
 					Group tempGroup = (Group) i.next();
 					
 					//Get the Group's ID minus one, so it corresponds to the array element associated with it
 					int tempGroupID = Integer.parseInt(Long.toString(tempGroup.getGroupId() -1) );	
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
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionElements(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetProtectionElements() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionGroups(String)'
 	 */
 
 	//	 Use PG_PERelationship Array to check that PE and PG associations are correct
 	private void testGetProtectionGroupsString() throws CSObjectNotFoundException 
 	{
 		String tempString = "";	
 		
 		//Cycle through each PE and make sure each element has the proper groups associated with it
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			Set SetOfProtectionGroups = userProvisioningManagerImpl.getProtectionGroups(Integer.toString(x+1));
 			assertNotNull("\ngetProtectionGroups(String) returned NULL\n", SetOfProtectionGroups);
 			if ((SetOfProtectionGroups != null) && (!SetOfProtectionGroups.isEmpty())) 
 			{
 				Iterator i = SetOfProtectionGroups.iterator();
 				while (i.hasNext()) 
 				{
 					ProtectionGroup tempProtectionGroup = (ProtectionGroup) i.next();
 					
 					//Get the PG's ID minus one, so it corresponds to the array element associated with it
 					int tempPGID = Integer.parseInt(Long.toString(tempProtectionGroup.getProtectionGroupId()-1));		
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
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignToProtectionGroups(String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignToProtectionGroups() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignParentProtectionGroup(String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignParentProtectionGroup() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.createApplication(Application)'
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
 
 			userProvisioningManagerImpl.createApplication(tempApplication);
 			tempApplication = null;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.modifyApplication(Application)'
 	 */
 	@SuppressWarnings("unused")
 	private void testModifyApplication() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.removeApplication(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testRemoveApplication() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getApplicationById(String)'
 	 */
 
 	private void testGetApplicationById() throws CSObjectNotFoundException 
 	{
 		String tempString = "";
 		Application tempApplication;
 		byte tempFlag = 0;
 		
 		for (int x=0; x<NumberOfApplicationsToTest; x++)
 		{
 			tempString = Integer.toString(x+2);  //Loading script loads an app in the database, so 2 is my first id I've created
 			tempApplication = userProvisioningManagerImpl.getApplicationById(tempString);
 			
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
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignOwners(String, String[])'
 	 */
 	@SuppressWarnings("unused")
 	private void testAssignOwners() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getOwners(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetOwners() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getApplicationContext()'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetApplicationContext() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getPrincipals(String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetPrincipals() 
 	{
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionGroups()'
 	 */
 
 	//Gets all PG, and checks the text of each group and the number of PG records returned to make sure they are correct
 	private void testGetProtectionGroups() throws CSObjectNotFoundException 
 	{
 		List tempPGList = userProvisioningManagerImpl.getProtectionGroups();
 
 		
 	    Iterator i = tempPGList.iterator();
 	    while (i.hasNext())
 	    {
 	    	ProtectionGroup tempProtectionGroup = (ProtectionGroup) i.next();
 	    	AssertEqualsForTextInProtectionGroup(Integer.parseInt(Long.toString(tempProtectionGroup.getProtectionGroupId() - 1)), tempProtectionGroup);
 	 
 	    }
 
 		if (tempPGList.size() != (long)NumberOfProtectionGroupsToTest || tempPGList == null || tempPGList.isEmpty())
 	    {
 	    	String tempString = "";
 	    	tempString = "\nThe Number of PGs in the DB is different than origionally created\nExpected: " + NumberOfProtectionGroupsToTest + "\nActual: " + tempPGList.size() + "\n";
 	   								 										
 	    	fail(tempString);
 	    }
 
 		
 		
 		
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getProtectionElement(String, String)'
 	 */
 
 	private void testGetProtectionElementStringString() 
 	{
 		ProtectionElement tempProtectionElement;
 		
 		for (int x=0; x<NumberOfProtectionElementsToTest; x++)
 		{
 			tempProtectionElement = userProvisioningManagerImpl.getProtectionElement(ProtectionElementStringArray[x][2], ProtectionElementStringArray[x][3]);
 			
 			AssertEqualsForProtectionElements(x, tempProtectionElement);
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.secureObject(String, Object)'
 	 */
 	@SuppressWarnings("unused")
 	private void testSecureObject() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.secureCollection(String, Collection)'
 	 */
 	@SuppressWarnings("unused")
 	private void testSecureCollection() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.getPrivilegeMap(String, Collection)'
 	 */
 	@SuppressWarnings("unused")
 	private void testGetPrivilegeMap() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.secureUpdate(String, Object, Object)'
 	 */
 	@SuppressWarnings("unused")
 	private void testSecureUpdate() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.checkOwnership(String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testCheckOwnership() {
 
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.assignUserToGroup(String, String)'
 	 */
 	
 	private void testAssignUserToGroup() throws CSTransactionException 
 	{
 		int y = 0;
 		
 		for (int x=0; x<NumberOfUsersToTest; x++)
 		{
 			if (y == NumberOfGroupsToTest) y=0;
 			userProvisioningManagerImpl.assignUserToGroup(UserStringArray[x][0], GroupStringArray[y][0] );
 			y++;
 		}
 	}
 
 	/*
 	 * Test method for 'gov.nih.nci.security.provisioning.UserProvisioningManagerImpl.setAuditUserInfo(String, String)'
 	 */
 	@SuppressWarnings("unused")
 	private void testSetAuditUserInfo() {
 
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
 		assertEquals("\nIncorrect User ID\n",			tempLong					 , (long)tempUser.getUserId());
 	}
 	
 	private void AssertEqualsForProtectionElements (int iteration, ProtectionElement tempProtectionElement)
 	{
 		long tempLong;
 		tempLong = (long)iteration+1;
 		
 		assertEquals("\nIncorrect Protection Element Name\n",		 ProtectionElementStringArray[iteration][0], tempProtectionElement.getProtectionElementName() );
 		assertEquals("\nIncorrect Protection Element Description\n", ProtectionElementStringArray[iteration][1], tempProtectionElement.getProtectionElementDescription() );
 		assertEquals("\nIncorrect Protection Element ObjectId\n", 	 ProtectionElementStringArray[iteration][2], tempProtectionElement.getObjectId() );
 		assertEquals("\nIncorrect Protection Element Description\n", ProtectionElementStringArray[iteration][3], tempProtectionElement.getAttribute() );
 		assertEquals("\nIncorrect Protection Element ID\n", 		 tempLong, (long)tempProtectionElement.getProtectionElementId() );	
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
 
