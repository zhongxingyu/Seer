 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.SkipException;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  */
 @Test(groups={"HelpTests"})
 public class HelpTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: man page",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41697)
 	public void ManPageForCLI_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String cliCommand = clienttasks.command;
 		RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+cliCommand,0);
 		RemoteFileTasks.runCommandAndAssert(client,"whatis "+cliCommand,0,"^"+cliCommand+" ",null);
 		log.warning("We only tested the existence of the man page; NOT the content.");
 	}
 	
 	@Test(	description="subscription-manager-gui: man page",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ManPageForGUI_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String guiCommand = clienttasks.command+"-gui";
 		// is the guiCommand installed?
 		if (client.runCommandAndWait("rpm -q "+clienttasks.command+"-gnome").getStdout().contains("is not installed")) {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+guiCommand,1,null,"^No manual entry for "+guiCommand);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+guiCommand,0,"^"+guiCommand+": nothing appropriate",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 			throw new SkipException(guiCommand+" is not installed and therefore its man page is also not installed.");
 		} else {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+guiCommand,0);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+guiCommand,0,"^"+guiCommand+" ",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 		}
 	}
 	
 	@Test(	description="rhsm-icon: man page",
 			groups={"blockedByBug-771726"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ManPageForRhsmIcon_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String command = "rhsm-icon"; //iconCommand = "rhsm-compliance-icon"; // prior to bug 771726
 		// is the command installed?
 		if (client.runCommandAndWait("rpm -q "+clienttasks.command+"-gnome").getStdout().contains("is not installed")) {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,1,null,"^No manual entry for "+command);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+": nothing appropriate",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 			throw new SkipException(command+" is not installed and therefore its man page is also not installed.");
 		} else {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,0);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+" ",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 		}
 	}
 	
 	@Test(	description="install-num-migrate-to-rhsm: man page",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ManPageForInstallNumMigrateToRhsm_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String command = MigrationTests.installNumTool;
 		// is the command installed?
 		if (client.runCommandAndWait("rpm -q "+clienttasks.command+"-migration").getStdout().contains("is not installed")) {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,1,null,"^No manual entry for "+command);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+": nothing appropriate",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 			throw new SkipException(command+" is not installed and therefore its man page cannot be installed.");
 		} else if (!clienttasks.redhatReleaseX.equals("5")) {
 			log.info("The man page for '"+command+"' should only be installed on RHEL5.");
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,1,null,"^No manual entry for "+command);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+": nothing appropriate",null);
 			throw new SkipException("The migration tool '"+command+"' and its man page is only applicable on RHEL5.");
 		} else {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,0);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+" ",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 		}
 	}
 	
 	@Test(	description="rhn-migrate-classic-to-rhsm: man page",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ManPageForRhnMigrateClassicToRhsm_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String command = MigrationTests.rhnMigrateTool;
 		// is the command installed?
 		if (client.runCommandAndWait("rpm -q "+clienttasks.command+"-migration").getStdout().contains("is not installed")) {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,1,null,"^No manual entry for "+command);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+": nothing appropriate",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 			throw new SkipException(command+" is not installed and therefore its man page is also not installed.");
 		} else {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+command,0);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+command,0,"^"+command+" ",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 		}
 	}
 	
 
 	
 	
 	
 	
 	@Test(	description="subscription-manager-cli: assert only expected command line options are available",
 			groups={},
 			dataProvider="ExpectedCommandLineOptionsData")
 	@ImplementsNitrateTest(caseId=46713)
 	//@ImplementsNitrateTest(caseId=46707)
 	public void ExpectedCommandLineOptions_Test(Object meta, String command, String stdoutRegex, List<String> expectedOptions) {
 		log.info("Testing subscription-manager-cli command line options '"+command+"' and verifying that only the expected options are available.");
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		
 		Pattern pattern = Pattern.compile(stdoutRegex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		Assert.assertTrue(matcher.find(),"Available command line options matching regex '"+stdoutRegex+"' are shown with command: "+command);
 		
 		// find all the matches to stderrRegex
 		List <String> actualOptions = new ArrayList<String>();
 		do {
 			actualOptions.add(matcher.group().trim());
 		} while (matcher.find());
 		
 		// assert all of the expectedOptions were found and that no unexpectedOptions were found
 		for (String expectedOption : expectedOptions) {
 			if (!actualOptions.contains(expectedOption)) {
 				log.warning("Could not find the expected command '"+command+"' option '"+expectedOption+"'.");
 			} else {
 				Assert.assertTrue(actualOptions.contains(expectedOption),"The expected command '"+command+"' option '"+expectedOption+"' is available.");
 			}
 		}
 		for (String actualOption : actualOptions) {
 			if (!expectedOptions.contains(actualOption))
 				log.warning("Found an unexpected command '"+command+"' option '"+actualOption+"'.");
 		}
 		Assert.assertTrue(actualOptions.containsAll(expectedOptions), "All of the expected command '"+command+"' line options are available.");
 		Assert.assertTrue(expectedOptions.containsAll(actualOptions), "All of the available command '"+command+"' line options are expected.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: assert help commands return translated text",
 			groups={/*"blockedByBug-756156"*/},
 			dataProvider="TranslatedCommandLineHelpData")
 	//@ImplementsNitrateTest(caseId=)
 	public void TranslatedCommandLineHelp_Test(Object meta, String lang, String command, List<String> stdoutRegexs) {
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,"LANG="+lang+".UTF-8 "+command,0,stdoutRegexs,null);
 	}
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 694662 - the whitespace in the title line of man subscription-manager-gui is completely consumed
 	// TODO Bug 752321 - [ALL LANG] [RHSM CLI] Word [OPTIONS] is unlocalized and some message translation is still not complete
 	//      TODO NESTED LANG LOOP...  for L in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do for C in list refresh register subscribe unregister unsubscribe clean config environments facts identity import orgs redeem repos; do echo ""; echo "# LANG=$L.UTF8 subscription-manager $C --help | grep OPTIONS"; LANG=$L.UTF8 subscription-manager $C --help | grep OPTIONS; done; done;
 	// TODO Bug 765905 - add man pages for subscription-manager-migration
 	// TODO Bug 812104 - Unable to tab complete new subscription-manager modules (release and service-level)
	
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeClass(groups={"setup"})
 	public void makewhatisBeforeClass() {
 		// running makewhatis to ensure that the whatis database is built on Beaker provisioned systems
 //debugTesting if (true) return;
 		RemoteFileTasks.runCommandAndAssert(client,"makewhatis",0);
 	}
 	
 	
 	// Protected Methods ***********************************************************************
 
 	protected List<String> newList(String item) {
 		List <String> newList = new ArrayList<String>();
 		newList.add(item);
 		return newList;
 	}
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="ExpectedCommandLineOptionsData")
 	public Object[][] getExpectedCommandLineOptionsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getExpectedCommandLineOptionsDataAsListOfLists());
 	}
 	protected List<List<Object>> getExpectedCommandLineOptionsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// String command, String stdoutRegex, List<String> expectedOptions
 		List<String> options = new ArrayList<String>();
 		String module;
 		String modulesRegex = "^	[\\w-]+";
 		String optionsRegex = "^  --[\\w\\.]+(=[\\w\\.]+)*|^  -\\w(=\\w+)*, --\\w+(=\\w+)*";
 		       optionsRegex = "^  --[\\w\\.-]+(=[\\w\\.-]+)*|^  -[\\?\\w]( \\w+)*, --[\\w\\.-]+(=\\w+)*";
 		
 		// EXAMPLES FOR optionsRegex
 		//  -h, --help            show this help message and exit
 		//  --list                list the configuration for this system
 		//  --remove=REMOVE       remove configuration entry by section.name
 		//  --server.hostname=SERVER.HOSTNAME
 		//  -?, --help                  Show help options
 		//  --help-all                  Show all help options
 		//  -f, --force-icon=TYPE       Force display of the icon (expired, partial or warning)
 		//  -c, --check-period          How often to check for validity (in seconds)
 		//  -i INSTNUMBER, --instnumber=INSTNUMBER
 		
 		// subscription-manager MODULES
 		List <String> modules = new ArrayList<String>();
 		modules.add("config");
 		modules.add("import");
 		modules.add("redeem");
 		modules.add("orgs");
 		modules.add("repos");
 		modules.add("clean");
 		modules.add("environments");
 		modules.add("facts");
 		modules.add("identity");
 		modules.add("list");
 		modules.add("refresh");
 		modules.add("register");
 		modules.add("subscribe");
 		modules.add("unregister");
 		modules.add("unsubscribe");
 		modules.add("service-level");
 		modules.add("release");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h",clienttasks.command+" --help"}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s [options] MODULENAME --help",clienttasks.command);	// prior to Bug 796730 - subscription-manager usage statement
 			usage = String.format("Usage: %s MODULE-NAME [MODULE-OPTIONS] [--help]",clienttasks.command);
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("796730"), smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, modulesRegex, modules}));
 		}
 		
 		
 		// subscription-manager config OPTIONS
 		module = "config";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--list");
 		options.add("--remove=REMOVE");
 		options.add("--server.ca_cert_dir=SERVER.CA_CERT_DIR");
 		options.add("--server.hostname=SERVER.HOSTNAME");
 		options.add("--server.insecure=SERVER.INSECURE");
 		options.add("--server.port=SERVER.PORT");
 		options.add("--server.prefix=SERVER.PREFIX");
 		options.add("--server.proxy_hostname=SERVER.PROXY_HOSTNAME");
 		options.add("--server.proxy_password=SERVER.PROXY_PASSWORD");
 		options.add("--server.proxy_port=SERVER.PROXY_PORT");
 		options.add("--server.proxy_user=SERVER.PROXY_USER");
 		options.add("--server.repo_ca_cert=SERVER.REPO_CA_CERT");
 		options.add("--server.ssl_verify_depth=SERVER.SSL_VERIFY_DEPTH");
 		options.add("--rhsm.baseurl=RHSM.BASEURL");
 		options.add("--rhsm.ca_cert_dir=RHSM.CA_CERT_DIR");
 		options.add("--rhsm.consumercertdir=RHSM.CONSUMERCERTDIR");
 		options.add("--rhsm.entitlementcertdir=RHSM.ENTITLEMENTCERTDIR");
 		options.add("--rhsm.hostname=RHSM.HOSTNAME");
 		options.add("--rhsm.insecure=RHSM.INSECURE");
 		options.add("--rhsm.port=RHSM.PORT");
 		options.add("--rhsm.prefix=RHSM.PREFIX");
 		options.add("--rhsm.productcertdir=RHSM.PRODUCTCERTDIR");
 		options.add("--rhsm.proxy_hostname=RHSM.PROXY_HOSTNAME");
 		options.add("--rhsm.proxy_password=RHSM.PROXY_PASSWORD");
 		options.add("--rhsm.proxy_port=RHSM.PROXY_PORT");
 		options.add("--rhsm.proxy_user=RHSM.PROXY_USER");
 		options.add("--rhsm.repo_ca_cert=RHSM.REPO_CA_CERT");
 		options.add("--rhsm.manage_repos=RHSM.MANAGE_REPOS");	// Bug 797996 - new configuration for rhsm.manage_repos should be exposed
 		options.add("--rhsm.ssl_verify_depth=RHSM.SSL_VERIFY_DEPTH");
 		options.add("--rhsmcertd.ca_cert_dir=RHSMCERTD.CA_CERT_DIR");
 		options.add("--rhsmcertd.certfrequency=RHSMCERTD.CERTFREQUENCY");
 		options.add("--rhsmcertd.healfrequency=RHSMCERTD.HEALFREQUENCY");
 		options.add("--rhsmcertd.hostname=RHSMCERTD.HOSTNAME");
 		options.add("--rhsmcertd.insecure=RHSMCERTD.INSECURE");
 		options.add("--rhsmcertd.port=RHSMCERTD.PORT");
 		options.add("--rhsmcertd.prefix=RHSMCERTD.PREFIX");
 		options.add("--rhsmcertd.proxy_hostname=RHSMCERTD.PROXY_HOSTNAME");
 		options.add("--rhsmcertd.proxy_password=RHSMCERTD.PROXY_PASSWORD");
 		options.add("--rhsmcertd.proxy_port=RHSMCERTD.PROXY_PORT");
 		options.add("--rhsmcertd.proxy_user=RHSMCERTD.PROXY_USER");
 		options.add("--rhsmcertd.repo_ca_cert=RHSMCERTD.REPO_CA_CERT");
 		options.add("--rhsmcertd.ssl_verify_depth=RHSMCERTD.SSL_VERIFY_DEPTH");
 		// after bug 807721, more config options are available
 		options.add("--server.certfrequency=SERVER.CERTFREQUENCY");
 		options.add("--server.manage_repos=SERVER.MANAGE_REPOS");
 		options.add("--server.entitlementcertdir=SERVER.ENTITLEMENTCERTDIR");
 		options.add("--server.baseurl=SERVER.BASEURL");
 		options.add("--server.productcertdir=SERVER.PRODUCTCERTDIR");
 		options.add("--server.consumercertdir=SERVER.CONSUMERCERTDIR");
 		options.add("--server.healfrequency=SERVER.HEALFREQUENCY");
 		options.add("--rhsm.certfrequency=RHSM.CERTFREQUENCY");
 		options.add("--rhsm.healfrequency=RHSM.HEALFREQUENCY");
 		options.add("--rhsmcertd.manage_repos=RHSMCERTD.MANAGE_REPOS");
 		options.add("--rhsmcertd.entitlementcertdir=RHSMCERTD.ENTITLEMENTCERTDIR");
 		options.add("--rhsmcertd.baseurl=RHSMCERTD.BASEURL");
 		options.add("--rhsmcertd.productcertdir=RHSMCERTD.PRODUCTCERTDIR");
 		options.add("--rhsmcertd.consumercertdir=RHSMCERTD.CONSUMERCERTDIR");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager import OPTIONS
 		module = "import";
 		options.clear();
 		options.add("-h, --help");
 		//options("--certificate=CERTIFICATE_FILES");	// prior to fix for Bug 735212
 		options.add("--certificate=CERTIFICATE_FILE");
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=733873
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="733873"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			options.add("--proxy=PROXY_URL");
 			options.add("--proxyuser=PROXY_USER");
 			options.add("--proxypassword=PROXY_PASSWORD");
 		}
 		// END OF WORKAROUND
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 
 		// subscription-manager redeem OPTIONS
 		module = "redeem";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--email=EMAIL");
 		options.add("--locale=LOCALE");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager orgs OPTIONS
 		module = "orgs";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--username=USERNAME");
 		options.add("--password=PASSWORD");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager repos OPTIONS
 		module = "repos";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--list");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager clean OPTIONS
 		module = "clean";
 		options.clear();
 		options.add("-h, --help");
 		// removed in https://bugzilla.redhat.com/show_bug.cgi?id=664581
 		//options("--proxy=PROXY_URL");
 		//options("--proxyuser=PROXY_USER");
 		//options("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("664581"), smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager environments OPTIONS
 		module = "environments";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--username=USERNAME");
 		options.add("--password=PASSWORD");
 		options.add("--org=ORG");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager facts OPTIONS
 		module = "facts";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--list");
 		options.add("--update");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager identity OPTIONS
 		module = "identity";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--username=USERNAME");
 		options.add("--password=PASSWORD");
 		options.add("--regenerate");
 		options.add("--force");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=678151
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager list OPTIONS
 		module = "list";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--installed");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=634254
 		options.add("--consumed");
 		options.add("--available");
 		options.add("--all");
 		options.add("--servicelevel=SERVICE_LEVEL");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=800999
 		options.add("--ondate=ON_DATE");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=672562
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager refresh OPTIONS
 		module = "refresh";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager register OPTIONS
 		module = "register";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--username=USERNAME");
 		options.add("--type=CONSUMERTYPE");
 		options.add("--name=CONSUMERNAME");
 		options.add("--password=PASSWORD");
 		options.add("--consumerid=CONSUMERID");
 		options.add("--org=ORG");
 		options.add("--environment=ENVIRONMENT");
 		options.add("--autosubscribe");
 		options.add("--force");
 		options.add("--activationkey=ACTIVATION_KEYS");
 		options.add("--servicelevel=SERVICE_LEVEL");
 		options.add("--release=RELEASE");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[]{null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("628589"), smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager subscribe OPTIONS
 		module = "subscribe";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--pool=POOL");
 		options.add("--quantity=QUANTITY");
 		options.add("--auto");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=680399
 		options.add("--servicelevel=SERVICE_LEVEL");
 		//options("--regtoken=REGTOKEN");	// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		//options("--email=EMAIL");			// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		//options("--locale=LOCALE");		// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager unregister OPTIONS
 		module = "unregister";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager unsubscribe OPTIONS
 		module = "unsubscribe";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--serial=SERIAL");
 		options.add("--all");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// subscription-manager service-level OPTIONS
 		module = "service-level";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		options.add("--username=USERNAME");
 		options.add("--password=PASSWORD");
 		options.add("--org=ORG");
 		options.add("--show");
 		options.add("--list");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 				
 		// subscription-manager release OPTIONS
 		module = "release";
 		options.clear();
 		options.add("-h, --help");
 		options.add("--proxy=PROXY_URL");
 		options.add("--proxyuser=PROXY_USER");
 		options.add("--proxypassword=PROXY_PASSWORD");
 		options.add("--set=RELEASE");
 		options.add("--list");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = String.format("Usage: %s %s [OPTIONS]",clienttasks.command,module);
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 		}
 		
 		// rhsm-icon OPTIONS
 		if (!client.runCommandAndWait("rpm -q "+clienttasks.command+"-gnome").getStdout().contains("is not installed")) {	// test only when the rpm is installed
 			//[root@jsefler-onprem-5server ~]# rhsm-icon -?
 			//Usage:
 			//  rhsm-icon [OPTION...] rhsm icon
 			//
 			//Help Options:
 			//  -?, --help                  Show help options
 			//  --help-all                  Show all help options
 			//  --help-gtk                  Show GTK+ Options
 			//
 			//Application Options:
 			//  -c, --check-period          How often to check for validity (in seconds)
 			//  -d, --debug                 Show debug messages
 			//  -f, --force-icon=TYPE       Force display of the icon (expired, partial or warning)
 			//  -i, --check-immediately     Run the first status check right away
 			//  --display=DISPLAY           X display to use
 			String rhsmIconCommand = "rhsm-icon"; 
 			List <String> rhsmIconOptions = new ArrayList<String>();
 			rhsmIconOptions.add("-h, --help");	//rhsmIconOptions.add("-?, --help");
 			rhsmIconOptions.add("--help-all");
 			rhsmIconOptions.add("--help-gtk");
 			rhsmIconOptions.add("-c, --check-period");
 			rhsmIconOptions.add("-d, --debug");
 			rhsmIconOptions.add("-f, --force-icon=TYPE");
 			rhsmIconOptions.add("-i, --check-immediately");
 			rhsmIconOptions.add("--display=DISPLAY");
 			for (String rhsmIconHelpCommand : new String[]{rhsmIconCommand+" -?", rhsmIconCommand+" --help"}) {
 				List <String> usages = new ArrayList<String>();
 				String usage = rhsmIconCommand+" [OPTIONS]";
 				usage = rhsmIconCommand+" [OPTIONS]"; // usage = rhsmIconCommand+" [OPTION...] rhsm icon"; // Bug 771756 - rhsm-icon --help usage message is misleading 
 				//if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 				usages.add(usage);
 				ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("771756"), rhsmIconHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 				ll.add(Arrays.asList(new Object[] {null, rhsmIconHelpCommand, optionsRegex, rhsmIconOptions}));
 			}
 			List <String> rhsmIconGtkOptions = new ArrayList<String>();
 			rhsmIconGtkOptions.add("--screen=SCREEN");
 			// GTK options are presented here: http://developer.gnome.org/gtk-tutorial/2.90/c39.html
 			rhsmIconGtkOptions.add("--class=CLASS");
 			rhsmIconGtkOptions.add("--name=NAME");
 			rhsmIconGtkOptions.add("--display=DISPLAY");
 			rhsmIconGtkOptions.add("--sync");
 			rhsmIconGtkOptions.add("--gtk-module=MODULES");
 			rhsmIconGtkOptions.add("--g-fatal-warnings");
 			rhsmIconGtkOptions.add("--gdk-debug=FLAGS");
 			rhsmIconGtkOptions.add("--gdk-no-debug=FLAGS");
 			rhsmIconGtkOptions.add("--gtk-debug=FLAGS");
 			rhsmIconGtkOptions.add("--gtk-no-debug=FLAGS");
 			ll.add(Arrays.asList(new Object[] {null, rhsmIconCommand+" --help-gtk", optionsRegex, rhsmIconGtkOptions}));
 			List <String> rhsmIconAllOptions = new ArrayList<String>();
 			rhsmIconAllOptions.addAll(rhsmIconOptions);
 			rhsmIconAllOptions.addAll(rhsmIconGtkOptions);
 			ll.add(Arrays.asList(new Object[] {null, rhsmIconCommand+" --help-all", optionsRegex, rhsmIconAllOptions}));
 		}
 		
 		// rhn-migrate-classic-to-rhsm OPTIONS
 		if (!client.runCommandAndWait("rpm -q "+clienttasks.command+"-migration").getStdout().contains("is not installed")) {	// test only when the rpm is installed
 			//[root@jsefler-onprem-5server ~]# rhn-migrate-classic-to-rhsm -h
 			//usage: /usr/sbin/rhn-migrate-classic-to-rhsm [--force|--cli-only|--help|--no-auto]
 			//
 			//options:
 			//  -f, --force     Ignore Channels not available on RHSM
 			//  -c, --cli-only  Don't launch the GUI tool to subscribe the system, just use
 			//                  the CLI tool which will do it automatically
 			//  -n, --no-auto   Don't launch subscription manager at end of process.
 			//  -h, --help      show this help message and exit
 
 			String rhnMigrateClassicToRhsmCommand = MigrationTests.rhnMigrateTool; 
 			options.clear();
 			options.add("-f, --force");
 			options.add("-c, --cli-only");
 			options.add("-n, --no-auto");
 			options.add("-h, --help");
 			for (String rhnMigrateClassicToRhsmHelpCommand : new String[]{rhnMigrateClassicToRhsmCommand+" -h", rhnMigrateClassicToRhsmCommand+" --help"}) {
 				List <String> usages = new ArrayList<String>();
 				String usage = String.format("usage: %s [OPTIONS]",rhnMigrateClassicToRhsmCommand);
 				usage = String.format("Usage: %s [OPTIONS]",rhnMigrateClassicToRhsmCommand);
 				usages.add(usage);
 				ll.add(Arrays.asList(new Object[] {null, rhnMigrateClassicToRhsmHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\|", "\\\\|")+"$", usages}));
 				ll.add(Arrays.asList(new Object[] {null, rhnMigrateClassicToRhsmHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 			}
 		}
 		
 		// install-num-migrate-to-rhsm OPTIONS
 		if (!client.runCommandAndWait("rpm -q "+clienttasks.command+"-migration").getStdout().contains("is not installed")) {	// test only when the rpm is installed
 		if (clienttasks.redhatReleaseX.equals("5")) {	// test only on RHEL5
 			//[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm --help
 			//usage: install-num-migrate-to-rhsm [options]
 			//
 			//options:
 			//  -h, --help            show this help message and exit
 			//  -i INSTNUMBER, --instnumber=INSTNUMBER
 			//                        Install number to run against
 			//  -d, --dryrun          Only print the files which would be copied over
 
 			String rhnMigrateClassicToRhsmCommand = MigrationTests.installNumTool; 
 			options.clear();
 			options.add("-h, --help");
 			options.add("-i INSTNUMBER, --instnumber=INSTNUMBER");
 			options.add("-d, --dryrun");
 			for (String rhnMigrateClassicToRhsmHelpCommand : new String[]{rhnMigrateClassicToRhsmCommand+" -h", rhnMigrateClassicToRhsmCommand+" --help"}) {
 				List <String> usages = new ArrayList<String>();
 				String usage = String.format("usage: %s [options]",rhnMigrateClassicToRhsmCommand);
 				usages.add(usage);
 				ll.add(Arrays.asList(new Object[] {null, rhnMigrateClassicToRhsmHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\|", "\\\\|")+"$", usages}));
 				ll.add(Arrays.asList(new Object[] {null, rhnMigrateClassicToRhsmHelpCommand, optionsRegex, new ArrayList<String>(options)}));
 			}
 		}
 		}
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="TranslatedCommandLineHelpData")
 	public Object[][] getTranslatedCommandLineHelpDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getTranslatedCommandLineHelpDataAsListOfLists());
 	}
 	protected List<List<Object>> getTranslatedCommandLineHelpDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		String usage,lang,module;
 		
 		// MODULES
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h",clienttasks.command+" --help"}) {
 		
 			// TODO new BlockedByBzBug("707080")
 			// [root@jsefler-r63-server ~]# for L in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do echo ""; echo "# LANG=$L.UTF-8 subscription-manager --help | grep -- --help"; LANG=$L.UTF-8 subscription-manager  --help | grep -- --help; done;
 			lang = "en_US"; usage = "(U|u)sage: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "de_DE"; usage = "(V|v)erwendung: subscription-manager MODUL-NAME [MODUL-OPTIONEN] [--help]";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "es_ES"; usage = "(U|u)so: subscription-manager MÓDULO-NOMBRE [MÓDULO-OPCIONES] [--help]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 //			lang = "fr_FR"; usage = "(U|u)tilisation\\s?: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";	ll.add(Arrays.asList(new Object[] {null/*new BlockedByBzBug(new String[]{"707080","743734","743732"})*/, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "fr_FR"; usage = "(U|u)tilisation.*: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";	ll.add(Arrays.asList(new Object[] {null/*new BlockedByBzBug(new String[]{"707080","743734","743732"})*/, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "it_IT"; usage = "(U|u)tilizzo: subscription-manager NOME-MODULO [OPZIONI-MODULO] [--help]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ja_JP"; usage = "使い方: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ko_KR"; usage = "사용법: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pt_BR"; usage = "(U|u)so: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ru_RU"; usage = "(Ф|ф)ормат: subscription-manager ДЕЙСТВИЕ [ПАРАМЕТРЫ] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_CN"; usage = "用法: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null/*new BlockedByBzBug(new String[]{"707080","743732"})*/, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_TW"; usage = "使用方法：subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "as_IN"; usage = "ব্যৱহাৰ: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null/*new BlockedByBzBug(new String[]{"743732","750807"})*/, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "bn_IN"; usage = "ব্যবহারপ্রণালী: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "hi_IN"; usage = "प्रयोग: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "mr_IN"; usage = "(वापर|वपार): subscription-manager मॉड्युल-नाव [मॉड्युल-पर्याय] [--help]";						ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "gu_IN"; usage = "વપરાશ: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "kn_IN"; usage = "ಬಳಕೆ: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("811294"), lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ml_IN"; usage = "ഉപയോഗിയ്ക്കേണ്ട വിധം: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "or_IN"; usage = "ବ୍ୟବହାର ବିଧି: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pa_IN"; usage = "ਵਰਤੋਂ: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ta_IN"; usage = "பயன்பாடு: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";				ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("811301"), lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "te_IN"; usage = "వాడుక: subscription-manager MODULE-NAME [MODULE-OPTIONS] [--help]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			
 			// TODO MODULE: clean
 			// TODO MODULE: activate
 			// TODO MODULE: facts
 			// TODO MODULE: identity
 			// TODO MODULE: list
 			// TODO MODULE: refresh
 			
 			// MODULE: register
 			// [root@jsefler-r63-server ~]# for L in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do echo ""; echo "# LANG=$L.UTF-8 subscription-manager register --help | grep -- 'subscription-manager register'"; LANG=$L.UTF-8 subscription-manager register --help | grep -- 'subscription-manager register'; done;
 			module = "register";
 			lang = "en_US"; usage = "(U|u)sage: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "de_DE"; usage = "(V|v)erwendung: subscription-manager register [OPTIONS]";		ll.add(Arrays.asList(new Object[] {null/*new BlockedByBzBug("693527")*/, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "es_ES"; usage = "(U|u)so: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 //			lang = "fr_FR"; usage = "(U|u)tilisation\\s?: subscription-manager register [OPTIONS]";	ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "fr_FR"; usage = "(U|u)tilisation.*subscription-manager register [OPTIONS]";	ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "it_IT"; usage = "(U|u)tilizzo: subscription-manager register [OPTIONS]";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ja_JP"; usage = "使用法: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ko_KR"; usage = "사용법: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pt_BR"; usage = "(U|u)so: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ru_RU"; usage = "(Ф|ф)ормат: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_CN"; usage = "使用：subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_TW"; usage = "使用方法：subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "as_IN"; usage = "ব্যৱহাৰ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null/*new BlockedByBzBug(new String[]{"743732"})*/, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "bn_IN"; usage = "ব্যবহারপ্রণালী: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "hi_IN"; usage = "प्रयोग: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "mr_IN"; usage = "(वापर|वपार): subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "gu_IN"; usage = "વપરાશ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "kn_IN"; usage = "ಬಳಕೆ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("811294"), lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ml_IN"; usage = "ഉപയോഗിയ്ക്കേണ്ട വിധം: subscription-manager register [OPTIONS]";	ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			//lang = "or_IN"; usage = "ବ୍ଯବହାର ବିଧି: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));	//rhel57 rhel61
 			lang = "or_IN"; usage = "ଉପଯୋଗ: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pa_IN"; usage = "ਵਰਤੋਂ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ta_IN"; usage = "பயன்பாடு: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "te_IN"; usage = "వా‍డుక: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 
 			// TODO MODULE: subscribe
 			// TODO MODULE: unregister
 			// TODO MODULE: unsubscribe
 		}
 		
 		return ll;
 	}
 }
