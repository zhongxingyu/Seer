 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /* Notes...
 <jsefler> I'm trying to strategize an automated test for the virt entitlements stuff you demo'ed on Wednesday.  I got a few questions to start with...
 <jharris> sure
  shoot
 <jsefler> using the RESTapi, if I search through all the owners subscriptions and find one with a virt_limit attribute, then that means that two pools should get created corresponding to it.  correct?
  one pool for the host and one pool fir the guests
 <jharris> yes
  specifically the attribute is on either the product or the pool
 <jsefler> what does that mean?
  the virt_limit is an attribute of the product - that I know
  next I need to figure out what the relevant attributes are on the pool
 <jharris> pools have attributes
  products have attributes
  the two pools are created, as you said
  the physical (host) pool will have no additional attributes
  the virt (guest) pool will have an attribute of "virt_only" set to true
  the candlepin logic should only let virtual machines subscribe to that second pool
  this is done by checking the virt.is_guest fact
  that is set in subscription manager
 <jsefler> yup - that sounds good - that's what I need to get started
 <jharris> excellent
  but the virt_only attribute can also just be used on a product, for example
  so that maybe we want to start selling a product that is like RHEL for virtual machines
  IT can just stick that virt_only attribute on the product directly
  and it should do the same filtering
  */
 
 /**
  * @author jsefler
  *
  */
 @Test(groups="VirtualizationTests")
 public class VirtualizationTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: facts list should report virt.is_guest and virt.host_type",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VirtualizationFactsReportedOnThisClient_Test() {
 		
 		// make sure the original virt-what is in place 
 		RemoteFileTasks.runCommandAndAssert(client, "cp -f "+virtWhatFileBackup+" "+virtWhatFile, 0);
 		SSHCommandResult result = client.runCommandAndWait("virt-what");
 		
 		log.info("Running virt-what version: "+client.runCommandAndWait("rpm -q virt-what").getStdout().trim());
 		
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(Boolean.valueOf(virtIsGuest),result.getStdout().trim().equals("")?Boolean.FALSE:Boolean.TRUE,"subscription-manager facts list reports virt.is_guest as true when virt-manager returns stdout.");
 		
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertEquals(virtHostType,result.getStdout().trim(),"subscription-manager facts list reports the same virt.host_type as what is returned by the virt-what installed on the client.");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list reports the host hypervisor type on which the guest client is running",
 			dataProvider="getVirtWhatData",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=70202)
 	public void VirtualizationFactsWhenClientIsAGuest_Test(String host_type) {
 		
 		log.info("We will fake out the ability of subscription-manager to read virt-what output on a '"+host_type+"' hypervisor by clobbering virt-what with a fake bash script...");
 		forceVirtWhatToReturnGuest(host_type);
 
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(Boolean.valueOf(virtIsGuest),Boolean.TRUE,"subscription-manager facts list reports virt.is_guest as true when the client is running on a '"+host_type+"' hypervisor.");
 
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertEquals(virtHostType,host_type,"subscription-manager facts list reports the same virt.host_type value of as returned by "+virtWhatFile);
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list reports when the client is running on bare metal",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=70203)
 	public void VirtualizationFactsWhenClientIsAHost_Test() {
 		
 		log.info("We will fake out the ability of subscription-manager to read virt-what output on bare metal by clobbering virt-what with a fake bash script...");
 		forceVirtWhatToReturnHost();
 
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(Boolean.valueOf(virtIsGuest),Boolean.FALSE,"subscription-manager facts list reports virt.is_guest as false when the client is running on bare metal.");
 
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertEquals(virtHostType,"","subscription-manager facts list reports no value for virt.host_type when run on bare metal ");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list should not crash when virt-what fails",
 			groups={"blockedByBug-668936"}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VirtualizationFactsWhenVirtWhatFails_Test() {
 		
 		log.info("We will fail virt-what by forcing it to return a non-zero value...");
 		forceVirtWhatToFail();
 		
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(virtIsGuest,"Unknown","subscription-manager facts list reports virt.is_guest as Unknown when virt-manager fails.");
 
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertNull(virtHostType,"subscription-manager facts list should NOT report a virt.host_type when the hypervisor is undeterminable." );
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list should report is_guest as Unknown when virt-what is not installed",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VirtualizationFactsWhenVirtWhatIsNotInstalled_Test() {
 		
 		log.info("We will remove virt-what for this test...");
 		
 		RemoteFileTasks.runCommandAndWait(client,"rm -f "+virtWhatFile, LogMessageUtil.action());
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(virtIsGuest,"Unknown","subscription-manager facts list reports virt.is_guest as Unknown when virt-manager in not installed.");
 		
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertNull(virtHostType,"subscription-manager facts list should NOT report a virt.host_type when the hypervisor is undeterminable." );
 	}
 	
 	
 	
 	
 	
 	
 	
 	@Test(	description="Verify host and guest pools are generated from a virtualization-aware subscription.",
 			groups={},
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostAndGuestPoolsAreGeneratedForVirtualizationAwareSubscription_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 
 		log.info("When an owner has purchased a virtualization-aware subscription ("+productName+"; subscriptionId="+subscriptionId+"), he should have subscription access to two pools: one for the host and one for the guest.");
 
 		// assert that there are two (one for the host and one for the guest)
 		log.info("Using the RESTful Candlepin API, let's find all the pools generated from subscription id: "+subscriptionId);
 		List<String> poolIds = CandlepinTasks.findPoolIdsFromSubscriptionId(serverHostname,serverPort,serverPrefix,clientusername,clientpassword, ownerKey, subscriptionId);
 		Assert.assertEquals(poolIds.size(), 2, "Exactly two pools should be derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// assert that one pool is for the host and the other is for the guest
 		guestPoolId = null;
 		hostPoolId = null;
 		for (String poolId : poolIds) {
 			Boolean virtOnly = CandlepinTasks.isPoolVirtOnly (serverHostname,serverPort,serverPrefix,clientusername,clientpassword, poolId);		
 			if (virtOnly!=null && virtOnly) {
 				guestPoolId = poolId;
 			} else {
 				hostPoolId = poolId;
 			}
 		}
 		Assert.assertNotNull(guestPoolId, "Found the guest pool id ("+guestPoolId+") with an attribute of virt_only=true");
 		Assert.assertNotNull(hostPoolId, "Found the host pool id ("+hostPoolId+") without an attribute of virt_only=true");	
 	}
 	
 	
 	@Test(	description="Verify host and guest pools quantities generated from a virtualization-aware subscription",
 			groups={}, // "blockedByBug-679617" indirectly when this script is run as part of the full TestNG suite since this is influenced by other scripts calling refresh pools
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostAndGuestPoolQuantities_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
		// trick this system into believing it is a virt guest
		forceVirtWhatToReturnGuest("kvm");
		
 		// get the hostPool
 		List<SubscriptionPool> allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, allAvailablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+hostPool);
 
 		// assert hostPoolId quantity
 		Assert.assertEquals(Integer.valueOf(hostPool.quantity), Integer.valueOf(quantity), "Assuming that nobody else is consuming from this host pool '"+hostPool.poolId+"', the maximum quantity of available entitlements should be "+quantity+".");
 		
 		// get the guestPool
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, allAvailablePools);
 		Assert.assertNotNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+guestPool);
 
 		// assert guestPoolId quantity
 		Assert.assertEquals(Integer.valueOf(guestPool.quantity), Integer.valueOf(quantity*Integer.valueOf(virtLimit)), "Assuming that nobody else is consuming from this guest pool '"+guestPool.poolId+"', the maximum quantity of available entitlements should be the virt_limit of '"+virtLimit+"' times the host quantity '"+quantity+"'.");
 	}
 	
 	
 	@Test(	description="Verify the virt_limit multiplier on guest pool quantity is not clobbered by refresh pools",
 			groups={"blockedByBug-679617"},
 			dependsOnGroups={},
 			dependsOnMethods={"VerifyHostAndGuestPoolQuantities_Test"},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyGuestPoolQuantityIsNotClobberedByRefreshPools_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// get the hostPool
 		List<SubscriptionPool> allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, allAvailablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+hostPool);
 
 		// remember the hostPool quantity before calling refresh pools
 		String hostPoolQuantityBefore = hostPool.quantity;
 		
 		// get the guestPool
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, allAvailablePools);
 		Assert.assertNotNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+guestPool);
 
 		// remember the hostPool quantity before calling refresh pools
 		String guestPoolQuantityBefore = guestPool.quantity;
 
 		log.info("Now let's modify the start date of the virtualization-aware subscription id '"+subscriptionId+"'...");
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/owners/"+ownerKey+"/subscriptions"));	
 		JSONObject jsonSubscription = null;
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			if (jsonSubscription.getString("id").equals(subscriptionId)) {break;} else {jsonSubscription=null;}
 		}
 		Calendar startDate = parseDateString(jsonSubscription.getString("startDate"));	// "startDate":"2012-02-08T00:00:00.000+0000"
 		Calendar newStartDate = (Calendar) startDate.clone(); newStartDate.add(Calendar.MONTH, -1);	// subtract a month
 		updateSubscriptionDatesOnDatabase(subscriptionId,newStartDate,null);
 
 		log.info("Now let's refresh the subscription pools...");
 		JSONObject jobDetail = CandlepinTasks.refreshPoolsUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,serverAdminUsername,serverAdminPassword, ownerKey);
 		jobDetail = CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,serverAdminUsername,serverAdminPassword, jobDetail, "FINISHED", 10*1000, 3);
 		allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 
 		// retrieve the host pool again and assert the quantity has not changed
 		hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, allAvailablePools);
 		Assert.assertEquals(hostPool.quantity, hostPoolQuantityBefore, "The quantity of entitlements available from the host pool has NOT changed after refreshing pools.");
 		
 		// retrieve the guest pool again and assert the quantity has not changed
 		guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, allAvailablePools);
 		Assert.assertEquals(guestPool.quantity, guestPoolQuantityBefore, "The quantity of entitlements available from the guest pool has NOT changed after refreshing pools.");
 	}
 	
 	
 	@Test(	description="Verify host and guest pools to a virtualization-aware subscription are subscribable on a guest system.",
 			groups={},
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostAndGuestPoolsAreSubscribableOnGuestSystem_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// trick this system into believing it is a virt guest
 		forceVirtWhatToReturnGuest("kvm");
 		
 		// assert that the hostPoolId is available
 		List<SubscriptionPool> availablePools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, availablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is available on a guest system: "+hostPool);
 		
 		// attempt to subscribe to the hostPoolId
 		clienttasks.subscribeToSubscriptionPool(hostPool);
 		
 		// assert that the guestPoolId is available
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, availablePools);
 		Assert.assertNotNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is available on a guest system: "+guestPool);
 
 		// attempt to subscribe to the guestPoolId
 		clienttasks.subscribeToSubscriptionPool(guestPool);
 	}
 	
 	
 	@Test(	description="Verify only the derived host pool from a virtualization-aware subscription is subscribable on a host system.  The guest pool should not be available nor subscribable.",
 			groups={},
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostPoolIsSubscribableOnHostSystemWhileGuestPoolIsNot_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// trick this system into believing it is a host
 		forceVirtWhatToReturnHost();
 		
 		// assert that the hostPoolId is available
 		List<SubscriptionPool> availablePools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, availablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is available on a host system: "+hostPool);
 
 		// assert that the guestPoolId is NOT available
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, availablePools);
 		Assert.assertNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' should NOT be available on a host system: "+guestPool);
 
 		// attempt to subscribe to the hostPoolId
 		clienttasks.subscribeToSubscriptionPool(hostPool);
 
 		// attempt to subscribe to the guestPoolId (should be blocked)
 		SSHCommandResult result = clienttasks.subscribe(null,guestPoolId,null,null,null,null,null,null, null);
 		// Unable to entitle consumer to the pool with id '8a90f8b42e3e7f2e012e3e7fc653013e': rulefailed.virt.only
 		Assert.assertContainsMatch(result.getStdout(), "^Unable to entitle consumer to the pool with id '"+guestPoolId+"':");
 	}
 	
 
 
 	
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=683459
 	
 	
 	
 	
 	// Configuration methods ***********************************************************************
 		
 	@BeforeClass(groups="setup")
 	public void backupVirtWhatBeforeClass() {
 		// finding location of virt-what...
 		SSHCommandResult result = client.runCommandAndWait("which virt-what");
 		virtWhatFile = new File(result.getStdout().trim());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, virtWhatFile.getPath())==1,"virt-what is in the client's path");
 		
 		// making a backup of virt-what...
 		virtWhatFileBackup = new File(virtWhatFile.getPath()+".bak");
 		RemoteFileTasks.runCommandAndAssert(client, "cp -n "+virtWhatFile+" "+virtWhatFileBackup, 0);
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, virtWhatFileBackup.getPath())==1,"successfully made a backup of virt-what to: "+virtWhatFileBackup);
 
 	}
 	
 	@AfterClass(groups="setup")
 	public void restoreVirtWhatAfterClass() {
 		// restoring backup of virt-what
 		if (virtWhatFileBackup!=null && RemoteFileTasks.testFileExists(client, virtWhatFileBackup.getPath())==1) {
 			RemoteFileTasks.runCommandAndAssert(client, "mv -f "+virtWhatFileBackup+" "+virtWhatFile, 0);
 		}
 	}
 	
 	@BeforeClass(groups="setup")
 	public void registerBeforeClass() throws Exception {
 		clienttasks.unregister(null, null, null);
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null));
 		
 		ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(serverHostname, serverPort, serverPrefix, clientusername, clientpassword, consumerId);
 	}
 	
 	@BeforeMethod(groups="setup")
 	public void unsubscribeBeforeMethod() throws Exception {
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 	}
 	
 	// protected methods ***********************************************************************
 	
 	protected String ownerKey = "";
 	protected File virtWhatFile = null;
 	protected File virtWhatFileBackup = null;
 	
 	protected Calendar parseDateString(String dateString){
 		String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; //"2012-02-08T00:00:00.000+0000"
 		try{
 			DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
 			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 			Calendar calendar = new GregorianCalendar();
 			calendar.setTimeInMillis(dateFormat.parse(dateString).getTime());
 			return calendar;
 		}
 		catch (ParseException e){
 			log.warning("Failed to parse GMT date string '"+dateString+"' with format '"+simpleDateFormat+"':\n"+e.getMessage());
 			return null;
 		}
 	}
 
 	
 	protected void forceVirtWhatToReturnGuest(String hypervisorType) {
 		// Note: when client is a guest, virt-what returns stdout="<hypervisor type>" and exitcode=0
 		RemoteFileTasks.runCommandAndWait(client,"echo '#!/bin/bash - ' > "+virtWhatFile+"; echo 'echo "+hypervisorType+"' >> "+virtWhatFile+"; chmod a+x "+virtWhatFile, LogMessageUtil.action());
 	}
 	
 	protected void forceVirtWhatToReturnHost() {
 		// Note: when client is a host, virt-what returns stdout="" and exitcode=0
 		RemoteFileTasks.runCommandAndWait(client,"echo '#!/bin/bash - ' > "+virtWhatFile+"; echo 'exit 0' >> "+virtWhatFile+"; chmod a+x "+virtWhatFile, LogMessageUtil.action());
 	}
 	
 	protected void forceVirtWhatToFail() {
 		// Note: when virt-what does not know if the system is on bare metal or on a guest, it returns a non-zero value
 		RemoteFileTasks.runCommandAndWait(client,"echo '#!/bin/bash - ' > "+virtWhatFile+"; echo 'echo \"virt-what is about to exit with code 255\"; exit 255' >> "+virtWhatFile+"; chmod a+x "+virtWhatFile, LogMessageUtil.action());
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getVirtWhatData")
 	public Object[][] getVirtWhatDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getVirtWhatDataAsListOfLists());
 	}
 	protected List<List<Object>> getVirtWhatDataAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		// man virt-what  (virt-what-1.3) shows support for the following hypervisors
 		ll.add(Arrays.asList(new Object[]{"openvz"}));
 		ll.add(Arrays.asList(new Object[]{"kvm"}));
 		ll.add(Arrays.asList(new Object[]{"qemu"}));
 		ll.add(Arrays.asList(new Object[]{"uml"}));
 		ll.add(Arrays.asList(new Object[]{"virtualbox"}));
 		ll.add(Arrays.asList(new Object[]{"virtualpc"}));
 		ll.add(Arrays.asList(new Object[]{"vmware"}));
 		ll.add(Arrays.asList(new Object[]{"xen"}));
 		ll.add(Arrays.asList(new Object[]{"xen-dom0"}));
 		ll.add(Arrays.asList(new Object[]{"xen-domU"}));
 		ll.add(Arrays.asList(new Object[]{"xen-hvm"}));
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getVirtSubscriptionData")
 	public Object[][] getVirtSubscriptionDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getVirtSubscriptionDataAsListOfLists());
 	}
 	protected List<List<Object>> getVirtSubscriptionDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			String subscriptionId = jsonSubscription.getString("id");
 			Calendar startDate = parseDateString(jsonSubscription.getString("startDate"));	// "startDate":"2012-02-08T00:00:00.000+0000"
 			Calendar endDate = parseDateString(jsonSubscription.getString("endDate"));	// "endDate":"2013-02-07T00:00:00.000+0000"
 			int quantity = jsonSubscription.getInt("quantity");
 			JSONObject jsonProduct = (JSONObject) jsonSubscription.getJSONObject("product");
 			String productName = jsonProduct.getString("name");
 			String productId = jsonProduct.getString("id");
 			JSONArray jsonAttributes = jsonProduct.getJSONArray("attributes");
 			// loop through the attributes of this jsonProduct looking for the "virt_limit" attribute
 			for (int j = 0; j < jsonAttributes.length(); j++) {
 				JSONObject jsonAttribute = (JSONObject) jsonAttributes.get(j);
 				String attributeName = jsonAttribute.getString("name");
 				if (attributeName.equals("virt_limit")) {
 					// found the virt_limit attribute - get its value
 					String virt_limit = jsonAttribute.getString("value");
 					
 					// only retrieve data that is valid today (at this time)
 					if (startDate.before(now) && endDate.after(now)) {
 
 						// save some computation cycles in the testcases and get the hostPoolId and guestPoolId
 						List<String> poolIds = CandlepinTasks.findPoolIdsFromSubscriptionId(serverHostname,serverPort,serverPrefix,clientusername,clientpassword, ownerKey, subscriptionId);
 
 						// determine which pool is for the guest, the other must be for the host
 						String guestPoolId = null;
 						String hostPoolId = null;
 						for (String poolId : poolIds) {
 							Boolean virtOnly = CandlepinTasks.isPoolVirtOnly (serverHostname,serverPort,serverPrefix,clientusername,clientpassword, poolId);		
 							if (virtOnly!=null && virtOnly) {
 								guestPoolId = poolId;
 							} else {
 								hostPoolId = poolId;
 							}
 						}
 						if (poolIds.size() != 2) {hostPoolId=null; guestPoolId=null;}	// set pools to null if there was a problem
 						
 						ll.add(Arrays.asList(new Object[]{subscriptionId, productName, productId, quantity, virt_limit, hostPoolId, guestPoolId}));
 					}
 				}
 			}
 		}
 		
 		return ll;
 	}
 	
 	/* Example jsonSubscription:
 	  {
 		    "id": "8a90f8b42e398f7a012e398ff0ef0104",
 		    "owner": {
 		      "href": "/owners/admin",
 		      "id": "8a90f8b42e398f7a012e398f8d310005"
 		    },
 		    "certificate": null,
 		    "product": {
 		      "name": "Awesome OS with up to 4 virtual guests",
 		      "id": "awesomeos-virt-4",
 		      "attributes": [
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "MKT",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        },
 		        {
 		          "name": "version",
 		          "value": "6.1",
 		          "updated": "2011-02-18T16:17:37.961+0000",
 		          "created": "2011-02-18T16:17:37.961+0000"
 		        },
 		        {
 		          "name": "virt_limit",
 		          "value": "4",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/awesomeos-virt-4",
 		      "updated": "2011-02-18T16:17:37.959+0000",
 		      "created": "2011-02-18T16:17:37.959+0000"
 		    },
 		    "providedProducts": [
 		      {
 		        "name": "Awesome OS Server Bits",
 		        "id": "37060",
 		        "attributes": [
 		          {
 		            "name": "variant",
 		            "value": "ALL",
 		            "updated": "2011-02-18T16:17:22.174+0000",
 		            "created": "2011-02-18T16:17:22.174+0000"
 		          },
 		          {
 		            "name": "sockets",
 		            "value": "2",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "arch",
 		            "value": "ALL",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "type",
 		            "value": "SVC",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "warning_period",
 		            "value": "30",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "version",
 		            "value": "6.1",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          }
 		        ],
 		        "multiplier": 1,
 		        "productContent": [
 		          {
 		            "content": {
 		              "name": "always-enabled-content",
 		              "id": "1",
 		              "type": "yum",
 		              "modifiedProductIds": [
 
 		              ],
 		              "label": "always-enabled-content",
 		              "vendor": "test-vendor",
 		              "contentUrl": "/foo/path/always",
 		              "gpgUrl": "/foo/path/always/gpg",
 		              "metadataExpire": 200,
 		              "updated": "2011-02-18T16:17:16.254+0000",
 		              "created": "2011-02-18T16:17:16.254+0000"
 		            },
 		            "flexEntitlement": 0,
 		            "physicalEntitlement": 0,
 		            "enabled": true
 		          },
 		          {
 		            "content": {
 		              "name": "never-enabled-content",
 		              "id": "0",
 		              "type": "yum",
 		              "modifiedProductIds": [
 
 		              ],
 		              "label": "never-enabled-content",
 		              "vendor": "test-vendor",
 		              "contentUrl": "/foo/path/never",
 		              "gpgUrl": "/foo/path/never/gpg",
 		              "metadataExpire": 600,
 		              "updated": "2011-02-18T16:17:16.137+0000",
 		              "created": "2011-02-18T16:17:16.137+0000"
 		            },
 		            "flexEntitlement": 0,
 		            "physicalEntitlement": 0,
 		            "enabled": false
 		          },
 		          {
 		            "content": {
 		              "name": "content",
 		              "id": "1111",
 		              "type": "yum",
 		              "modifiedProductIds": [
 
 		              ],
 		              "label": "content-label",
 		              "vendor": "test-vendor",
 		              "contentUrl": "/foo/path",
 		              "gpgUrl": "/foo/path/gpg/",
 		              "metadataExpire": 0,
 		              "updated": "2011-02-18T16:17:16.336+0000",
 		              "created": "2011-02-18T16:17:16.336+0000"
 		            },
 		            "flexEntitlement": 0,
 		            "physicalEntitlement": 0,
 		            "enabled": true
 		          }
 		        ],
 		        "dependentProductIds": [
 
 		        ],
 		        "href": "/products/37060",
 		        "updated": "2011-02-18T16:17:22.174+0000",
 		        "created": "2011-02-18T16:17:22.174+0000"
 		      }
 		    ],
 		    "endDate": "2012-02-18T00:00:00.000+0000",
 		    "startDate": "2011-02-18T00:00:00.000+0000",
 		    "quantity": 5,
 		    "contractNumber": "39",
 		    "accountNumber": "12331131231",
 		    "modified": null,
 		    "tokens": [
 
 		    ],
 		    "upstreamPoolId": null,
 		    "updated": "2011-02-18T16:17:38.031+0000",
 		    "created": "2011-02-18T16:17:38.031+0000"
 		  }
 	  */
 	
 	
 
 	
 	
 
 }
