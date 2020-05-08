 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"GeneralTests"})
 public class GeneralTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to access functionality without registering",
 			groups={},
 			dataProvider="UnregisteredCommandData")
 	@ImplementsNitrateTest(caseId=41697)
 	public void AttemptingCommandsWithoutBeingRegistered_Test(String command) {
 		log.info("Testing subscription-manager-cli command without being registered, expecting it to fail: "+ command);
 		clienttasks.unregister(null, null, null);
 		//RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner, command);
 		RemoteFileTasks.runCommandAndAssert(client,command,1,"^Error: You need to register this system by running `register` command before using this option.",null);
 
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to access functionality that does not exist",
 			groups={},
 			dataProvider="NegativeFunctionalityData")
 	public void AttemptingCommandsThatAreInvalid_Test(String command, int expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing subscription-manager-cli command that is invalid, expecting it to fail: "+ command);
 		RemoteFileTasks.runCommandAndAssert(client,command,expectedExitCode,expectedStdoutRegex,expectedStderrRegex);
 
 	}
 	
 	
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 688469 - subscription-manager <module> --help does not work in localized environment.
 	// TODO Bug 684941 - Deleting a product with a subscription gives ugly error
 	// TODO Bug 629708 - import/export validation error wrapped
	// TODO Bug 744536 - [ALL LANG] [RHSM CLI] unsubscribe module _unexpected 'ascii' code can't decode ...message.
 	
 	
 	// Data Providers ***********************************************************************
 	
 
 	
 	@DataProvider(name="UnregisteredCommandData")
 	public Object[][] getUnregisteredCommandDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUnregisteredCommandDataAsListOfLists());
 	}
 	public List<List<Object>> getUnregisteredCommandDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" facts --update"}));  test moved to FactsTests.FactsWhenNotRegistered_Test()
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" identity"}));  test moved to IdentityTests.IdentityWhenNotRegistered_Test()
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" list"}));	restriction lifted by https://bugzilla.redhat.com/show_bug.cgi?id=725870
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --available --all"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --available"}));
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --consumed"}));	restriction lifted by https://bugzilla.redhat.com/show_bug.cgi?id=725870
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" refresh"}));
 // this functionality appears to have been removed: subscription-manager-0.71-1.el6.i686  - jsefler 7/21/2010
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --product=FOO"}));
 // this functionality appears to have been removed: subscription-manager-0.93.14-1.el6.x86_64 - jsefler 1/21/2011
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --regtoken=FOO"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --pool=FOO"}));
 // ability to unsubscribe without being registered was added after fix for bug 735338  jsefler 9/13/2011
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --all"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --serial=FOO"}));
 // this functionality appears to have been removed: subscription-manager-0.68-1.el6.i686  - jsefler 7/12/2010
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --product=FOO"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --regtoken=FOO"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --pool=FOO"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" redeem"}));
 
 		return ll;
 	}
 	
 	@DataProvider(name="NegativeFunctionalityData")
 	public Object[][] getNegativeFunctionalityDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getNegativeFunctionalityDataAsListOfLists());
 	}
 	protected List<List<Object>> getNegativeFunctionalityDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// due to design changes, this is a decent place to dump old commands that have been removed
 		
 		// String command, int expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --product=FOO",		2,		null,	clienttasks.command+": error: no such option: --product"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --regtoken=FOO",	2,		null,	clienttasks.command+": error: no such option: --regtoken"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --pool=FOO",		2,		null,	clienttasks.command+": error: no such option: --pool"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --pool=123 --auto",	255,	"Only one of --pool or --auto may be used.",	null}));
 		
 		return ll;
 	}
 }
