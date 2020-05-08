 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
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
 import com.redhat.qe.sm.base.CandlepinType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.Translation;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  * References:
  *   Engineering Localization Services: https://home.corp.redhat.com/node/53593
  *   http://git.fedorahosted.org/git/?p=subscription-manager.git;a=blob;f=po/pt.po;h=0854212f4fab348a25f0542625df343653a4a097;hb=RHEL6.3
  *   Here is the raw rhsm.po file for LANG=pt
  *   http://git.fedorahosted.org/git/?p=subscription-manager.git;a=blob;f=po/pt.po;hb=RHEL6.3
  *   
  *   https://engineering.redhat.com/trac/LocalizationServices
  *   https://engineering.redhat.com/trac/LocalizationServices/wiki/L10nRHEL6LanguageSupportCriteria
  *   
  *   https://translate.zanata.org/zanata/project/view/subscription-manager/iter/0.99.X/stats
  *   
  *   https://fedora.transifex.net/projects/p/fedora/
  *   
  *   http://translate.sourceforge.net/wiki/
  *   http://translate.sourceforge.net/wiki/toolkit/index
  *   http://translate.sourceforge.net/wiki/toolkit/pofilter
  *   http://translate.sourceforge.net/wiki/toolkit/pofilter_tests
  *   http://translate.sourceforge.net/wiki/toolkit/installation
  *   
  *   https://github.com/translate/translate
  *   
  **/
 @Test(groups={"TranslationTests"})
 public class TranslationTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: assert help commands return translated text",
 			groups={/*"blockedByBug-756156"*/},
 			dataProvider="getTranslatedCommandLineHelpData")
 	//@ImplementsNitrateTest(caseId=)
 	public void TranslatedCommandLineHelp_Test(Object meta, String lang, String command, List<String> stdoutRegexs) {
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,"LANG="+lang+".UTF-8 "+command,0,stdoutRegexs,null);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register to a Candlepin server using bogus credentials and check for localized strings results",
 			groups={"AcceptanceTests"},
 			dataProvider="getInvalidRegistrationWithLocalizedStringsData")
 	@ImplementsNitrateTest(caseId=41691)
 	public void AttemptLocalizedRegistrationWithInvalidCredentials_Test(Object bugzilla, String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex) {
 
 		// ensure we are unregistered
 		clienttasks.unregister(null, null, null);
 		
 		log.info("Attempting to register to a candlepin server using invalid credentials and expecting output in language "+(lang==null?"DEFAULT":lang));
 		String command = String.format("%s %s register --username=%s --password=%s", lang==null?"":"LANG="+lang, clienttasks.command, username, password);
 		RemoteFileTasks.runCommandAndAssert(client, command, exitCode, stdoutRegex, stderrRegex);
 		
 		// assert that the consumer cert and key have NOT been dropped
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client,clienttasks.consumerKeyFile()),0, "Consumer key file '"+clienttasks.consumerKeyFile()+"' does NOT exist after an attempt to register with invalid credentials.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client,clienttasks.consumerCertFile()),0, "Consumer cert file '"+clienttasks.consumerCertFile()+" does NOT exist after an attempt to register with invalid credentials.");
 	}
 	
 	
 	@Test(	description="attempt LANG=C subscription-manager register",
 			groups={"blockedByBug-729988"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithFallbackCLocale_Test() {
 
 		//	[root@rhsm-compat-rhel61 ~]# LANG=C subscription-manager register --username stage_test_12 --password redhat 1>/tmp/stdout 2>/tmp/stderr
 		//	[root@rhsm-compat-rhel61 ~]# echo $?
 		//	255
 		//	[root@rhsm-compat-rhel61 ~]# cat /tmp/stdout 
 		//	[root@rhsm-compat-rhel61 ~]# cat /tmp/stderr
 		//	'NoneType' object has no attribute 'lower'
 		//	[root@rhsm-compat-rhel61 ~]# 
 		
 		for(String lang: new String[]{"C","us"}) {
 			clienttasks.unregister(null, null, null);
 			String command = String.format("%s register --username %s --password %s", clienttasks.command,sm_clientUsername,sm_clientPassword);
 			if (sm_clientOrg!=null) command += String.format(" --org %s", sm_clientOrg);
 			SSHCommandResult sshCommandResult = clienttasks.runCommandWithLang(lang,clienttasks.command+" register --username "+sm_clientUsername+" --password "+sm_clientPassword+" "+(sm_clientOrg!=null?"--org "+sm_clientOrg:""));
 			Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0),"ExitCode after register with LANG="+lang+" fallback locale.");
 			//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with LANG="+lang+" fallback locale.");
 			//Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after register with LANG="+lang+" fallback locale.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt redeem without --email option using LANG",
 			groups={"blockedByBug-766577","AcceptanceTests"},
 			enabled=false)	// TODO PASSES ON THE COMMAND LINE BUT FAILS WHEN RUN THROUGH AUTOMATION - NOTE STDOUT DISPLAYS DOUBLE BYTE BUT NOT STDERR
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptRedeemWithoutEmailUsingLang_Test() {
 		
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, true, false, null, null, null);
 		//SSHCommandResult redeemResult = clienttasks.redeem_(null,null,null,null,null)
 		String lang = "de_DE";
 		log.info("Attempting to redeem without specifying email expecting output in language "+(lang==null?"DEFAULT":lang));
 		String command = String.format("%s %s redeem", lang==null?"":"LANG="+lang+".UTF-8", clienttasks.command);
 		client.runCommandAndWait(command+" --help");
 		SSHCommandResult redeemResult = client.runCommandAndWait(command);
 
 		// bug766577
 		// 201112191709:14.807 - FINE: ssh root@jsefler-onprem-5server.usersys.redhat.com LANG=de_DE subscription-manager redeem
 		// 201112191709:17.276 - FINE: Stdout: 
 		// 201112191709:17.277 - FINE: Stderr: 'ascii' codec can't encode character u'\xf6' in position 20: ordinal not in range(128)
 		// 201112191709:17.277 - FINE: ExitCode: 255
 		
 		// [root@jsefler-onprem-5server ~]# LANG=de_DE.UTF-8 subscription-manager redeem
 		// E-Mail-Adresse ist nötig zur Benachrichtigung
 
 		// assert redemption results
 		//Assert.assertEquals(redeemResult.getStdout().trim(), "email and email_locale are required for notification","Redeem should require that the email option be specified.");
 		Assert.assertEquals(redeemResult.getStderr().trim(), "");
 		Assert.assertEquals(redeemResult.getStdout().trim(), "E-Mail-Adresse ist nötig zur Benachrichtigung","Redeem should require that the email option be specified.");
 		Assert.assertEquals(redeemResult.getExitCode(), Integer.valueOf(255),"Exit code from redeem when executed without an email option.");
 	}
 	
 	
 	@Test(	description="verify that rhsm.mo is installed for each of the supported locales",
 			groups={"AcceptanceTests"},
 			dataProvider="getSupportedLocalesData",
 			enabled=false)	// replaced by VerifyOnlyExpectedTranslationFilesAreInstalled_Test
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyTranslationFileIsInstalled_Test(Object bugzilla, String locale) {
 		File localeFile = localeFile(locale);
 		Assert.assertTrue(RemoteFileTasks.testExists(client, localeFile.getPath()),"Supported locale file '"+localeFile+"' is installed.");
 		if (!translationFileMap.keySet().contains(localeFile)) Assert.fail("Something went wrong in TranslationTests.buildTranslationFileMap().  File '"+localeFile+"' was not found in the translationFileMap.keySet().");
 	}
 	
 	@Test(	description="verify that only the expected rhsm.mo tranlation files are installed for each of the supported locales",
 			groups={"AcceptanceTests", "blockedByBug-824100"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyOnlyExpectedTranslationFilesAreInstalled_Test() {
 		List<File> supportedTranslationFiles = new ArrayList<File>();
 		for (String supportedLocale : supportedLocales) supportedTranslationFiles.add(localeFile(supportedLocale));
 		log.info("Expected locales include: "+supportedLocales);
 		
 		// assert no unexpected translation files are installed
 		boolean unexpectedTranslationFilesFound = false;
 		for (File translationFile : translationFileMap.keySet()) {
 			if (!supportedTranslationFiles.contains(translationFile)) {
 				unexpectedTranslationFilesFound = true;
 				log.warning("Unexpected translation file '"+translationFile+"' is installed.");
 			}
 		}
 		// assert that all expected translation files are installed
 		boolean allExpectedTranslationFilesFound = true;
 		for (File translationFile : supportedTranslationFiles) {
 			if (!translationFileMap.keySet().contains(translationFile)) {
 				log.warning("Expected translation file '"+translationFile+"' is NOT installed.");
 				allExpectedTranslationFilesFound = false;
 			} else {
 				log.info("Expected translation file '"+translationFile+"' is installed.");
 			}
 		}
 		Assert.assertTrue(!unexpectedTranslationFilesFound, "No unexpected translation files were found installed.");
 		Assert.assertTrue(allExpectedTranslationFilesFound, "All expected translation files were found installed.");
 	}
 	
 	@Test(	description="verify that only the expected rhsm.mo tranlation files are installed for each of the supported locales",
			groups={"AcceptanceTest"},
 			dataProvider="getTranslationFileData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyTranslationFileContainsAllMsgids_Test(Object bugzilla, File translationFile) {
 		List<Translation> translationList = translationFileMap.get(translationFile);
 		boolean translationFilePassed=true;
 		for (String msgid : translationMsgidSet) {
 			int numMsgidOccurances=0;
 			for (Translation translation : translationList) {
 				if (translation.msgid.equals(msgid)) numMsgidOccurances++;
 			}
 			if (numMsgidOccurances!=1) {
 				log.warning("Expected 1 occurance (actual='"+numMsgidOccurances+"') of the following msgid in translation file '"+translationFile+"':  msgid \""+msgid+"\"");
 				translationFilePassed=false;
 			}
 		}
 		Assert.assertTrue(translationFilePassed,"Exactly 1 occurance of all the expected translation msgids ("+translationMsgidSet.size()+") were found in translation file '"+translationFile+"'.");
 	}
 
 	@Test(	description="run pofilter translate tests on the translation file",
 			groups={"debugTest"},
 			dataProvider="getTranslationFilePofilterTestData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void pofilter_Test(Object bugzilla, String pofilterTest, File translationFile) {
 		log.info("For an explanation of pofilter test '"+pofilterTest+"', see: http://translate.sourceforge.net/wiki/toolkit/pofilter_tests");
 		
 		File translationPoFile = new File(translationFile.getPath().replaceFirst(".mo$", ".po"));
 		
 		// execute the pofilter test
 		String pofilterCommand = "pofilter -t "+pofilterTest;
 		SSHCommandResult pofilterResult = client.runCommandAndWait(pofilterCommand+" "+translationPoFile);
 		Assert.assertEquals(pofilterResult.getExitCode(), new Integer(0), "Successfully executed the pofilter tests.");
 		
 		// convert the pofilter test results into a list of failed Translation objects for simplified handling of special cases 
 		List<Translation> pofilterFailedTranslations = Translation.parse(pofilterResult.getStdout());
 		
 		// remove the first translation which contains only meta data
 		if (!pofilterFailedTranslations.isEmpty() && pofilterFailedTranslations.get(0).msgid.equals("")) pofilterFailedTranslations.remove(0);
 		
 		// ignore the following special cases of acceptable results..........
 		if (pofilterTest.equals("unchanged")) {
 			Translation ignoreTranslation = Translation.findFirstInstanceWithMatchingFieldFromList("msgid", "%s [OPTIONS]", pofilterFailedTranslations);
 			if (ignoreTranslation!=null) {
 				log.info("Ignoring result of pofiliter test '"+pofilterTest+"' for msgid: "+ignoreTranslation.msgid);
 				pofilterFailedTranslations.remove(ignoreTranslation);
 			}
 		}
 		Assert.assertEquals(pofilterFailedTranslations.size(),0, "Discounting the ignored test results, the number of failed pofilter '"+pofilterTest+"' tests for translation file '"+translationFile+"'.");
 	}
 	
 	
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 752321 - [ALL LANG] [RHSM CLI] Word [OPTIONS] is unlocalized and some message translation is still not complete
 	//      TODO NESTED LANG LOOP...  for L in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do for C in list refresh register subscribe unregister unsubscribe clean config environments facts identity import orgs redeem repos; do echo ""; echo "# LANG=$L.UTF8 subscription-manager $C --help | grep OPTIONS"; LANG=$L.UTF8 subscription-manager $C --help | grep OPTIONS; done; done;
 
 	
 	// Configuration Methods ***********************************************************************
 	@BeforeClass (groups="setup")
 	public void buildTranslationFileMap() {
 		if (clienttasks==null) return;
 		
 		//List<File> translationFiles =  new ArrayList<File>();
 		SSHCommandResult translationFileListingResult = client.runCommandAndWait("rpm -ql subscription-manager | grep rhsm.mo");
 		for (String translationFilePath : translationFileListingResult.getStdout().trim().split("\\n")) {
 			if (translationFilePath.isEmpty()) continue; // skip empty lines
 			
 			File translationFile = new File(translationFilePath);
 			//translationFiles.add(translationFile);
 			
 			// decompile the rhsm.mo file into its original-like rhsm.po file
 			log.info("Decompiling the rhsm.mo file...");
 			File translationPoFile = new File(translationFile.getPath().replaceFirst(".mo$", ".po"));
 			RemoteFileTasks.runCommandAndAssert(client,"msgunfmt --no-wrap "+translationFile+ " -o "+translationPoFile,new Integer(0));
 			
 			// parse the translations from the rhsm.mo into the translationFileMap
 			SSHCommandResult msgunfmtListingResult = client.runCommandAndWaitWithoutLogging("msgunfmt --no-wrap "+translationFilePath);
 			translationFileMap.put(translationFile, Translation.parse(msgunfmtListingResult.getStdout()));
 		}
 	}
 	
 	@BeforeClass (groups="setup",dependsOnMethods={"buildTranslationFileMap"})
 	public void buildTranslationMsgidSet() {
 		if (clienttasks==null) return;
 		
 		// assemble a unique set of msgids
 		for (File translationFile : translationFileMap.keySet()) {
 			List<Translation> translationList = translationFileMap.get(translationFile);
 			for (Translation translation : translationList) {
 				translationMsgidSet.add(translation.msgid);
 			}
 		}
 	}
 
 	
 	
 	// Protected Methods ***********************************************************************
 	List<String> supportedLocales = Arrays.asList(	"as",	"bn_IN","de_DE","es_ES","fr",	"gu",	"hi",	"it",	"ja",	"kn",	"ko",	"ml",	"mr",	"or",	"pa",	"pt_BR","ru",	"ta_IN","te",	"zh_CN","zh_TW"); 
 	List<String> supportedLangs = Arrays.asList(	"as_IN","bn_IN","de_DE","es_ES","fr_FR","gu_IN","hi_IN","it_IT","ja_JP","kn_IN","ko_KR","ml_IN","mr_IN","or_IN","pa_IN","pt_BR","ru-RU","ta_IN","te_IN","zh_CN","zh_TW"); 
 	Map<File,List<Translation>> translationFileMap = new HashMap<File, List<Translation>>();
 	Set<String> translationMsgidSet = new HashSet<String>(500);  // 500 is an estimated size
 
 	
 	protected List<String> newList(String item) {
 		List <String> newList = new ArrayList<String>();
 		newList.add(item);
 		return newList;
 	}
 	protected File localeFile(String locale) {
 		return new File("/usr/share/locale/"+locale+"/LC_MESSAGES/rhsm.mo");
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getSupportedLocalesData")
 	public Object[][] getSupportedLocalesDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getSupportedLocalesDataAsListOfLists());
 	}
 	protected List<List<Object>> getSupportedLocalesDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (String locale : supportedLocales) {
 			
 			// bugzillas
 			Object bugzilla = null;
 			if (locale.equals("kn")) bugzilla = new BlockedByBzBug("811294");
 			
 			// Object bugzilla, String locale
 			ll.add(Arrays.asList(new Object[] {bugzilla,	locale}));
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getTranslationFileData")
 	public Object[][] getTranslationFileDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getTranslationFileDataAsListOfLists());
 	}
 	protected List<List<Object>> getTranslationFileDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (File translationFile : translationFileMap.keySet()) {
 			BlockedByBzBug bugzilla = null;
 			// Bug 824100 - pt_BR translations are outdated for subscription-manager 
 			if (translationFile.getPath().contains("/pt_BR/")) bugzilla = new BlockedByBzBug("824100");
 			// Bug 824184 - [ta_IN] translations for subscription-manager are missing (95% complete)
 			if (translationFile.getPath().contains("/ta/") || translationFile.getPath().contains("/ta_IN/")) bugzilla = new BlockedByBzBug("824184");
 			
 			ll.add(Arrays.asList(new Object[] {bugzilla,	translationFile}));
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getTranslationFilePofilterTestData")
 	public Object[][] getTranslationFilePofilterTestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getTranslationFilePofilterTestDataAsListOfLists());
 	}
 	protected List<List<Object>> getTranslationFilePofilterTestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		// see http://translate.sourceforge.net/wiki/toolkit/pofilter_tests
 		//	Critical -- can break a program
 		//    	accelerators, escapes, newlines, nplurals, printf, tabs, variables, xmltags, dialogsizes
 		//	Functional -- may confuse the user
 		//    	acronyms, blank, emails, filepaths, functions, gconf, kdecomments, long, musttranslatewords, notranslatewords, numbers, options, purepunc, sentencecount, short, spellcheck, urls, unchanged
 		//	Cosmetic -- make it look better
 		//    	brackets, doublequoting, doublespacing, doublewords, endpunc, endwhitespace, puncspacing, simplecaps, simpleplurals, startcaps, singlequoting, startpunc, startwhitespace, validchars
 		//	Extraction -- useful mainly for extracting certain types of string
 		//    	compendiumconflicts, credits, hassuggestion, isfuzzy, isreview, untranslated
 
 		List<String> pofilterTests = Arrays.asList(
 				//	Critical -- can break a program
 				"accelerators","escapes","newlines","nplurals","printf","tabs","variables","xmltags",
 				//	Functional -- may confuse the user
 				"blank","emails","filepaths","gconf","long","notranslatewords","numbers","options","short","urls","unchanged",
 				//	Cosmetic -- make it look better
 				"doublewords",
 				//	Extraction -- useful mainly for extracting certain types of string
 				"untranslated");
 pofilterTests = Arrays.asList("accelerators","escapes"/* TODO ,"unchanged"*/);
 		for (File translationFile : translationFileMap.keySet()) {
 			for (String pofilterTest : pofilterTests) {
 				BlockedByBzBug bugzilla = null;
 				ll.add(Arrays.asList(new Object[] {bugzilla,	pofilterTest,	translationFile}));
 			}
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getTranslatedCommandLineHelpData")
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
 			lang = "zh_CN"; usage = "用法：subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
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
 	
 	
 	@DataProvider(name="getInvalidRegistrationWithLocalizedStringsData")
 	public Object[][] getInvalidRegistrationWithLocalizedStringsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationWithLocalizedStringsAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationWithLocalizedStringsAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 
 		// String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex
 		
 		// registration test for a user who is invalid
 		ll.add(Arrays.asList(new Object[]{null, "en_US.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, uErrMsg}));
 		
 		// registration test for a user who with "invalid credentials" (translated)
 		//if (!isServerOnPremises)	ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"}),	"de_DE.UTF-8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Berechtigungnachweise"/*"Ungültige Mandate"*//*"Ungültiger Benutzername oder Kennwort"*/:"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc"}));
 		//else 						ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362"),                      	"de_DE.UTF-8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Berechtigungnachweise"/*"Ungültige Mandate"*//*"Ungültiger Benutzername oder Kennwort"*/:"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc"}));
 		if (sm_serverType.equals(CandlepinType.standalone)) {
 			ll.add(Arrays.asList(new Object[]{null,								"en_US.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Invalid Credentials"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362"),		"de_DE.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Ungültige Berechtigungnachweise"}));
 			ll.add(Arrays.asList(new Object[]{null,								"es_ES.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Credenciales inválidas"}));
 			ll.add(Arrays.asList(new Object[]{null,								"fr_FR.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Informations d’identification invalides"}));
 			ll.add(Arrays.asList(new Object[]{null,								"it_IT.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Credenziali invalide"}));
 			ll.add(Arrays.asList(new Object[]{null,								"ja_JP.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "無効な識別情報"}));
 			ll.add(Arrays.asList(new Object[]{null,								"ko_KR.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "잘못된 인증 정보"}));
 			ll.add(Arrays.asList(new Object[]{null,								"pt_BR.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Credenciais inválidas"}));
 			ll.add(Arrays.asList(new Object[]{null,								"ru_RU.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Недопустимые учетные данные"}));
 			ll.add(Arrays.asList(new Object[]{null,								"zh_CN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "无效证书"}));
 			ll.add(Arrays.asList(new Object[]{null,								"zh_TW.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "無效的認證"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("683914"),		"as_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "অবৈধ পৰিচয়"}));
 			ll.add(Arrays.asList(new Object[]{null,								"bn_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "অবৈধ পরিচয়"}));
 			ll.add(Arrays.asList(new Object[]{null,								"hi_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "अवैध श्रेय"}));
 			ll.add(Arrays.asList(new Object[]{null,								"mr_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "अवैध श्रेय"}));
 			ll.add(Arrays.asList(new Object[]{null,								"gu_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "અયોગ્ય શ્રેય"}));
 			ll.add(Arrays.asList(new Object[]{null,								"kn_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ಅಮಾನ್ಯವಾದ ಪರಿಚಯಪತ್ರ"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("683914"),		"ml_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "തെറ്റായ ആധികാരികതകള്‍"}));
 			ll.add(Arrays.asList(new Object[]{null,								"or_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ଅବୈଧ ପ୍ରାଧିକରଣ"}));
 			ll.add(Arrays.asList(new Object[]{null,								"pa_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ਗਲਤ ਕਰੀਡੈਂਸ਼ਲ"}));
 			ll.add(Arrays.asList(new Object[]{null,								"ta_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "தவறான சான்றுகள்"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("683914"),		"te_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "చెల్లని ప్రమాణాలు"}));
 		} else {
 			ll.add(Arrays.asList(new Object[]{null,								"en_US.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Invalid username or password. To create a login, please visit https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"de_DE.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Ungültiger Benutzername oder Passwort. Um ein Login anzulegen, besuchen Sie bitte https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{null,								"es_ES.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "El nombre de usuario o contraseña es inválido. Para crear un nombre de usuario, por favor visite https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{null,								"fr_FR.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Nom d'utilisateur ou mot de passe non valide. Pour créer une connexion, veuillez visiter https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{null,								"it_IT.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Nome utente o password non valide. Per creare un login visitare https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{null,								"ja_JP.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ユーザー名かパスワードが無効です。ログインを作成するには、https://www.redhat.com/wapps/ugc/register.html に進んでください"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"ko_KR.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "사용자 이름 또는 암호가 잘못되었습니다. 로그인을 만들려면, https://www.redhat.com/wapps/ugc/register.html으로 이동해 주십시오."}));
 			ll.add(Arrays.asList(new Object[]{null,								"pt_BR.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Nome do usuário e senha incorretos. Por favor visite https://www.redhat.com/wapps/ugc/register.html para a criação do logon."}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"ru_RU.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "Неверное имя пользователя или пароль. Для создания учётной записи перейдите к https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{null,								"zh_CN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "无效用户名或者密码。要创建登录，请访问 https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"zh_TW.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "無效的使用者名稱或密碼。若要建立登錄帳號，請至 https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"as_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "অবৈধ ব্যৱহাৰকাৰী নাম অথবা পাছৱাৰ্ড। এটা লগিন সৃষ্টি কৰিবলে, অনুগ্ৰহ কৰি চাওক https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"bn_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ব্যবহারকারীর নাম অথবা পাসওয়ার্ড বৈধ নয়। লগ-ইন প্রস্তুত করার জন্য অনুগ্রহ করে https://www.redhat.com/wapps/ugc/register.html পরিদর্শন করুন"}));
 			ll.add(Arrays.asList(new Object[]{null,								"hi_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "अवैध उपयोक्तानाम या कूटशब्द. लॉगिन करने के लिए, कृपया https://www.redhat.com/wapps/ugc/register.html भ्रमण करें"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"mr_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "अवैध वापरकर्तानाव किंवा पासवर्ड. प्रवेश निर्माण करण्यासाठी, कृपया https://www.redhat.com/wapps/ugc/register.html येथे भेट द्या"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"gu_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "અયોગ્ય વપરાશકર્તાનામ અથવા પાસવર્ડ. લૉગિનને બનાવવા માટે, મહેરબાની કરીને https://www.redhat.com/wapps/ugc/register.html મુલાકાત લો"}));
 			ll.add(Arrays.asList(new Object[]{null,								"kn_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ಅಮಾನ್ಯವಾದ ಬಳಕೆದಾರ ಹೆಸರು ಅಥವ ಗುಪ್ತಪದ. ಒಂದು ಲಾಗಿನ್ ಅನ್ನು ರಚಿಸಲು, ದಯವಿಟ್ಟು https://www.redhat.com/wapps/ugc/register.html ಗೆ ತೆರಳಿ"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"ml_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "തെറ്റായ ഉപയോക്തൃനാമം അല്ലെങ്കില്<200d> രഹസ്യവാക്ക്. പ്രവേശനത്തിനായി, ദയവായി https://www.redhat.com/wapps/ugc/register.html സന്ദര്<200d>ശിയ്ക്കുക"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"or_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ଅବୈଧ ଚାଳକନାମ କିମ୍ବା ପ୍ରବେଶ ସଂକେତ। ଗୋଟିଏ ଲଗଇନ ନିର୍ମାଣ କରିବା ପାଇଁ, ଦୟାକରି https://www.redhat.com/wapps/ugc/register.html କୁ ପରିଦର୍ଶନ କରନ୍ତୁ"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"pa_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "ਗਲਤ ਯੂਜ਼ਰ-ਨਾਂ ਜਾਂ ਪਾਸਵਰਡ। ਲਾਗਇਨ ਬਣਾਉਣ ਲਈ, ਕਿਰਪਾ ਕਰਕੇ ਇਹ ਵੇਖੋ https://www.redhat.com/wapps/ugc/register.html"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"ta_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "தவறான பயனர்பெயர் அல்லது கடவுச்சொல். ஒரு உட்புகுவை உருவாக்குவதற்கு, https://www.redhat.com/wapps/ugc/register.html பார்வையிடவும்"}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("706197"),		"te_IN.UTF-8", sm_clientUsername+getRandInt(), sm_clientPassword+getRandInt(), 255, null, "చెల్లని వాడుకరిపేరు లేదా సంకేతపదము. లాగిన్ సృష్టించుటకు, దయచేసి https://www.redhat.com/wapps/ugc/register.html దర్శించండి"}));
 		}
 		// registration test for a user who has not accepted Red Hat's Terms and conditions (translated)  Man, why did you do something?
 		if (!sm_usernameWithUnacceptedTC.equals("")) {
 			if (sm_serverType.equals(CandlepinType.hosted))	ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"}),"de_DE.UTF-8", sm_usernameWithUnacceptedTC, sm_passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!"}));
 			else											ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362"),                       "de_DE.UTF-8", sm_usernameWithUnacceptedTC, sm_passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!"}));
 		}
 		
 		// registration test for a user who has been disabled (translated)
 		if (!sm_disabledUsername.equals("")) {
 			ll.add(Arrays.asList(new Object[]{null, "en_US.UTF-8", sm_disabledUsername, sm_disabledPassword, 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 		}
 		// [root@jsefler-onprem-server ~]# for l in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do echo ""; echo ""; echo "# LANG=$l.UTF-8 subscription-manager clean --help"; LANG=$l.UTF-8 subscription-manager clean --help; done;
 		/* TODO reference for locales
 		[root@jsefler-onprem03 ~]# rpm -lq subscription-manager | grep locale
 		/usr/share/locale/as_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/bn_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/de_DE/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/en_US/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/es_ES/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/fr_FR/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/gu_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/hi_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/it_IT/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ja_JP/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/kn_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ko_KR/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ml_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/mr_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/or_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/pa_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/pt_BR/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ru_RU/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ta_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/te_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/zh_CN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/zh_TW/LC_MESSAGES/rhsm.mo
 		*/
 		
 		// TODO HERE IS A COMMAND FOR GETTING THE EXPECTED TRANSLATION MESSAGE STRINGS
 		/* msgunfmt /usr/share/locale/de/LC_MESSAGES/rhsm.mo
 		msgid "%prog [options]"
 		msgstr "%prog [Optionen]"
 
 		msgid "%s (first date of invalid entitlements)"
 		msgstr "%s (erster Tag mit ungültigen Berechtigungen)"
 		*/
 		
 		/* python script that alikins wrote to pad a language strings with _
 	    #!/usr/bin/python
 	     
 	    import polib
 	    import sys
 	     
 	    path = sys.argv[1]
 	    pofile = polib.pofile(path)
 	     
 	    for entry in pofile:
 	    orig = entry.msgstr
 	    new = orig + "_"*40
 	    entry.msgstr = new
 	     
 	    pofile.save(path)
 	    */
 
 		/* TODO Here is a script from alikins that will report untranslated strings
 		#!/usr/bin/python
 		
 		# NEEDS polib from http://pypi.python.org/pypi/polib
 		# or easy_install polib
 		 
 		import glob
 		import polib
 		 
 		#FIXME
 		PO_PATH = "po/"
 		 
 		po_files = glob.glob("%s/*.po" % PO_PATH)
 		 
 		for po_file in po_files:
 		  print
 		  print po_file
 		  p = polib.pofile(po_file)
 		  for entry in p.untranslated_entries():
 		    for line in entry.occurrences:
 		      print "%s:%s" % (line[0], line[1])
 		    print "\t%s" % entry.msgid
 		 
 		  for entry in p.fuzzy_entries():
 		    for line in entry.occurrences:
 		      print "%s:%s" % (line[0], line[1])
 		    print "\t%s" % entry.msgid
 		 */
 		return ll;
 	}
 
 }
