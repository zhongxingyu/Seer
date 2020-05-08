 package net.codjo.tools.pyp.pages;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import junit.framework.Assert;
 import net.codjo.test.common.fixture.CompositeFixture;
 import net.codjo.test.common.fixture.DirectoryFixture;
 import net.codjo.test.common.fixture.MailFixture;
 import net.codjo.tools.pyp.WicketFixture;
 import net.codjo.util.file.FileUtil;
 import org.apache.wicket.Session;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.util.tester.FormTester;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 /**
  *
  */
 public class HomePageTest extends WicketFixture {
     static final int LAST_WEEK_FILTER = 0;
     private static final int CURRENT_MONTH_FILTER = 1;
     private static final int CURRENT_YEAR_FILTER = 2;
     static final int ALL_BRIN_FILTER = 3;
 
     private DirectoryFixture fixture = new DirectoryFixture("target/pyp");
     private MailFixture mailFixture = new MailFixture(89);
     private CompositeFixture compositeFixture = new CompositeFixture(fixture, mailFixture);
 
     private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S z");
 
 
     @Before
     public void setUp() throws Exception {
         compositeFixture.doSetUp();
     }
 
 
     @After
     public void tearDown() throws Exception {
         compositeFixture.doTearDown();
     }
 
 
     @Test
     public void test_homePage() throws Exception {
         doInit("/pyp.properties");
         getWicketTester().startPage(HomePage.class);
         getWicketTester().assertContains("Title");
         getWicketTester().assertContains("Creation");
         getWicketTester().assertContains("Status");
         getWicketTester().assertContains("Add new BRIN");
 
         assertTextIsNotPresent("thisIsA new Brin");
 
         assertLabelInLeftPanelAtRow(1, "current", "0");
         assertLabelInLeftPanelAtRow(2, "unblocked", "0");
         assertLabelInLeftPanelAtRow(3, "toEradicate", "0");
         assertLabelInLeftPanelAtRow(4, "eradicated", "0");
 
         addNewBrin("thisIsA new Brin", 2, "2001-01-12");
 
         getWicketTester().assertRenderedPage(HomePage.class);
         getWicketTester().assertLabel("leftPanel:summaryPanel:infoList:3:nbBrin", "1");
         getWicketTester().assertContains("thisIsA new Brin");
         getWicketTester().assertContains("toEradicate");
 
         updateBrin("titre Modifi", 2);
        mailFixture.getReceivedMessage(0).assertThat()
              .from(System.getProperty("user.name") + "@allianz.fr")
              .to("USER1@allianz.fr", "USER2@allianz.fr")
              .subject("[BRIN][TO_ERADICATE] - thisIsA new Brin")
              .bodyContains("Bonjour,<br><br>Le BRIN suivant a t cr.<br>")
              .bodyContains("<br><b>Description:</b><br><br><br><br>")
              .bodyContains("Pour plus de dtails, ")
              .bodyContains("merci de consulter le BRIN dans l'application ")
              .bodyContains("<a href=\"http://localhost:8080/pyp/edit.html/(.*)/1\">PostYourProblem</a>")
              .bodyContains("Cordialement.")
        ;
        mailFixture.assertReceivedMessagesCount(1);
 
         getWicketTester().assertRenderedPage(HomePage.class);
         getWicketTester().assertContains("titre Modifi");
         getWicketTester().assertContains("toEradicate");
 
         assertLabelInLeftPanelAtRow(1, "current", "0");
         assertLabelInLeftPanelAtRow(2, "unblocked", "0");
         assertLabelInLeftPanelAtRow(3, "toEradicate", "1");
         assertLabelInLeftPanelAtRow(4, "eradicated", "0");
     }
 
 
     @Test
     public void test_BrinFilter() throws Exception {
         Date today = Calendar.getInstance().getTime();
         Date twoDaysAgo = shiftDate(today, -2);
         Date sevenDaysAgo = shiftDate(today, -7);
         Date aMonthAgo = shiftDate(today, -30);
         Date moreThanThreeMonthAgo = shiftDate(today, -90);
         Date moreThanAYearAgo = shiftDate(today, -370);
 
         String fileContent = "<brinList>\n"
                              + "  <repository>\n"
                              + "    <brin>\n"
                              + "      <uuid>1</uuid>\n"
                              + "      <title>Brin de plus d'un mois</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <status>unblocked</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>2</uuid>\n"
                              + "      <title>Brin d'il y a deux jours</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(twoDaysAgo) + "</creationDate>\n"
                              + "      <status>current</status>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>sqdqd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>3</uuid>\n"
                              + "      <title>Brin pile ya une semaine</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(sevenDaysAgo) + "</creationDate>\n"
                              + "      <status>current</status>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>sqdqd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>4</uuid>\n"
                              + "      <title>Plus d'un mois MAIS status current</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <status>current</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>5</uuid>\n"
                              + "      <title>Deboque ya moins d une semaine</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <unblockingDate>" + simpleDateFormat.format(twoDaysAgo) + "</unblockingDate>\n"
                              + "      <status>unblocked</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>6</uuid>\n"
                              + "      <title>A eradiquer ya plus de 3 mois (90jours)</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <unblockingDate>" + simpleDateFormat.format(moreThanThreeMonthAgo)
                              + "</unblockingDate>\n"
                              + "      <status>toEradicate</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>7</uuid>\n"
                              + "      <title>Eradique ya plus d'un an (370 jours)</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(moreThanAYearAgo) + "</creationDate>\n"
                              + "      <unblockingDate>" + simpleDateFormat.format(moreThanAYearAgo)
                              + "      </unblockingDate>\n"
                              + "      <status>eradicated</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "  </repository>\n"
                              + "</brinList>";
 
         FileUtil.saveContent(new File(fixture.getCanonicalFile(), "pypRepository.xml"), fileContent);
 
         doInit("/pyp.properties");
 
         getWicketTester().startPage(HomePage.class);
 
         int row = 1;
         assertLabelAtRow(row++, "Brin d&#039;il y a deux jours");
         assertLabelAtRow(row++, "Brin pile ya une semaine");
         assertLabelAtRow(row++, "Plus d&#039;un mois MAIS status current");
         assertLabelAtRow(row++, "A eradiquer ya plus de 3 mois (90jours)");
         assertLabelAtRow(row++, "Brin de plus d&#039;un mois");
         assertLabelAtRow(row++, "Deboque ya moins d une semaine");
         assertLabelAtRow(row, "Eradique ya plus d&#039;un an (370 jours)");
 
         assertLabelInLeftPanelAtRow(1, "current", "3");
         assertLabelInLeftPanelAtRow(2, "unblocked", "2");
         assertLabelInLeftPanelAtRow(3, "toEradicate", "1");
         assertLabelInLeftPanelAtRow(4, "eradicated", "1");
 
         switchFilter(this, LAST_WEEK_FILTER);
 
         //TODO decalage des indices de la liste a cause de 2 appels a la construction de la liste ?
         row = 15;
         assertLabelAtRow(row++, "Brin d&#039;il y a deux jours");
         assertLabelAtRow(row++, "Brin pile ya une semaine");
         assertLabelAtRow(row++, "Plus d&#039;un mois MAIS status current");
         assertLabelAtRow(row, "Deboque ya moins d une semaine");
         assertTextIsNotPresent("Brin de plus d&#039;un mois");
         assertTextIsNotPresent("A eradiquer ya plus de 3 mois (90jours)");
         assertTextIsNotPresent("Eradique ya plus d&#039;un an (370 jours)");
 
         assertLabelInLeftPanelAtRow(9, "current", "3");
         assertLabelInLeftPanelAtRow(10, "unblocked", "1");
         assertLabelInLeftPanelAtRow(11, "toEradicate", "0");
         assertLabelInLeftPanelAtRow(12, "eradicated", "0");
 
         switchFilter(this, CURRENT_MONTH_FILTER);
 
         assertLabelInLeftPanelAtRow(17, "current", "3");
         assertLabelInLeftPanelAtRow(18, "unblocked", "1");
         assertLabelInLeftPanelAtRow(19, "toEradicate", "0");
         assertLabelInLeftPanelAtRow(20, "eradicated", "0");
 
         switchFilter(this, CURRENT_YEAR_FILTER);
         assertLabelInLeftPanelAtRow(25, "current", "3");
         assertLabelInLeftPanelAtRow(26, "unblocked", "2");
         assertLabelInLeftPanelAtRow(27, "toEradicate", "1");
         assertLabelInLeftPanelAtRow(28, "eradicated", "0");
 
         switchFilter(this, ALL_BRIN_FILTER);
         assertLabelInLeftPanelAtRow(33, "current", "3");
         assertLabelInLeftPanelAtRow(34, "unblocked", "2");
         assertLabelInLeftPanelAtRow(35, "toEradicate", "1");
         assertLabelInLeftPanelAtRow(36, "eradicated", "1");
     }
 
 
     @Test
     public void test_BrinFilterPersistentWithSession() throws Exception {
         Date today = Calendar.getInstance().getTime();
         Date twoDaysAgo = shiftDate(today, -2);
         Date aMonthAgo = shiftDate(today, -30);
 
         String fileContent = "<brinList>\n"
                              + "  <repository>\n"
                              + "    <brin>\n"
                              + "      <uuid>1</uuid>\n"
                              + "      <title>Brin de plus d'un mois</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <status>unblocked</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>2</uuid>\n"
                              + "      <title>Brin d'il y a deux jours</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(twoDaysAgo) + "</creationDate>\n"
                              + "      <status>current</status>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>sqdqd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "  </repository>\n"
                              + "</brinList>";
 
         FileUtil.saveContent(new File(fixture.getCanonicalFile(), "pypRepository.xml"), fileContent);
 
         doInit("/pyp.properties");
 
         getWicketTester().startPage(HomePage.class);
 
         int row = 1;
         assertLabelAtRow(row++, "Brin d&#039;il y a deux jours");
         assertLabelAtRow(row, "Brin de plus d&#039;un mois");
 
         switchFilter(this, LAST_WEEK_FILTER);
 
         row = 5;
         assertLabelAtRow(row, "Brin d&#039;il y a deux jours");
         assertTextIsNotPresent("Brin de plus d&#039;un mois");
 
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 
         updateBrinWithParams(row, "Brin d'il y a deux jours (modifi)", 1, format.format(today));
 
         getWicketTester().assertRenderedPage(HomePage.class);
         assertLabelAtRow(1, "Brin d&#039;il y a deux jours (modifi)");
         assertTextIsNotPresent("Brin de plus d&#039;un mois");
 
         addNewBrin("thisIsA new Brin", 1, format.format(today));
         getWicketTester().assertRenderedPage(HomePage.class);
         assertLabelAtRow(1, "Brin d&#039;il y a deux jours (modifi)");
         assertLabelAtRow(2, "thisIsA new Brin");
         assertTextIsNotPresent("Brin de plus d&#039;un mois");
     }
 
 
     @Test
     public void test_wikiExport() throws Exception {
         Date today = Calendar.getInstance().getTime();
         Date twoDaysAgo = shiftDate(today, -2);
         Date aMonthAgo = shiftDate(today, -30);
 
         String fileContent = "<brinList>\n"
                              + "  <repository>\n"
                              + "    <brin>\n"
                              + "      <uuid>1</uuid>\n"
                              + "      <title>Brin de plus d'un mois</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <status>unblocked</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>3</uuid>\n"
                              + "      <title>Autre Brin de plus d'un mois</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <status>unblocked</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "    <brin>\n"
                              + "      <uuid>2</uuid>\n"
                              + "      <title>Brin d'il y a deux jours</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(twoDaysAgo) + "</creationDate>\n"
                              + "      <status>current</status>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>sqdqd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "  </repository>\n"
                              + "</brinList>";
 
         FileUtil.saveContent(new File(fixture.getCanonicalFile(), "pypRepository.xml"), fileContent);
 
         doInit("/pyp.properties");
 
         getWicketTester().startPage(HomePage.class);
 
         int row = 1;
         assertLabelAtRow(row++, "Brin d&#039;il y a deux jours");
         assertLabelAtRow(row++, "Brin de plus d&#039;un mois");
         assertLabelAtRow(row, "Autre Brin de plus d&#039;un mois");
 
         getWicketTester().clickLink("rightPanel:myContainer:menuList:2:imageLink", true);
 
         ModalWindow modalWindow = (ModalWindow)getWicketTester().getComponentFromLastRenderedPage("wikiExportPanel");
         Assert.assertTrue(modalWindow.isShown());
 
         String expectedWikiContent = ""
                                      + "* current\n"
                                      + "** [Brin d'il y a deux jours | http://localhost/edit.html?id=2]\n"
                                      + "* unblocked\n"
                                      + "** [Brin de plus d'un mois | http://localhost/edit.html?id=1]\n"
                                      + "** [Autre Brin de plus d'un mois | http://localhost/edit.html?id=3]\n"
               ;
         getWicketTester().assertModelValue("wikiExportPanel:content:wikiContent", expectedWikiContent);
     }
 
     @Test
     public void test_sendMail() throws Exception {
         Date today = Calendar.getInstance().getTime();
         Date twoDaysAgo = shiftDate(today, -2);
         Date aMonthAgo = shiftDate(today, -30);
 
         String fileContent = "<brinList>\n"
                              + "  <repository>\n"
                              + "    <brin>\n"
                              + "      <uuid>1</uuid>\n"
                              + "      <title>Brin de plus d'un mois</title>\n"
                              + "      <creationDate>" + simpleDateFormat.format(aMonthAgo) + "</creationDate>\n"
                              + "      <status>unblocked</status>\n"
                              + "      <description>sdq</description>\n"
                              + "      <affectedTeams/>\n"
                              + "      <unblockingDescription>qsd</unblockingDescription>\n"
                              + "    </brin>\n"
                              + "  </repository>\n"
                              + "</brinList>";
 
         FileUtil.saveContent(new File(fixture.getCanonicalFile(), "pypRepository.xml"), fileContent);
 
         doInit("/pyp.properties");
 
         getWicketTester().startPage(HomePage.class);
 
         updateBrin("new title",2);
         mailFixture.getReceivedMessage(0).assertThat()
               .from(System.getProperty("user.name") + "@allianz.fr")
               .to("USER1@allianz.fr", "USER2@allianz.fr")
               .subject("[BRIN][TO_ERADICATE] - new title")
               .bodyContains("Bonjour,<br><br>Le BRIN suivant doit tre radiqu.<br>")
               .bodyContains("<br><b>Description:</b><br><br><br><br>")
               .bodyContains("Pour plus de dtails, ")
               .bodyContains("merci de consulter le BRIN dans l'application ")
               .bodyContains("<a href=\"http://localhost//edit.html/id/1\">PostYourProblem</a>")
               .bodyContains("Cordialement.")
         ;
         mailFixture.assertReceivedMessagesCount(1);
 
 
     }
 
 
     private void assertLabelAtRow(int row, String expectedLabel) {
         getWicketTester().assertLabel("brinListContainer:brinList:" + row + ":title",
                                       expectedLabel);
     }
 
 
     private void assertLabelInLeftPanelAtRow(int row, String expectedLabelText, String expectedBrinNumber) {
         getWicketTester().assertLabel("leftPanel:summaryPanel:infoList:" + row + ":statusLabel", expectedLabelText);
         getWicketTester().assertLabel("leftPanel:summaryPanel:infoList:" + row + ":nbBrin",
                                       expectedBrinNumber);
     }
 
 
     //TODO copierColler de BrinFilterForm pas beau
     public static Date shiftDate(Date dateToShift, int nbOfDays) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(dateToShift);
         calendar.add(Calendar.DAY_OF_MONTH, nbOfDays);
         return calendar.getTime();
     }
 
 
     private void addNewBrin(String title, int statusIndex, String newCreationDate) {
         getWicketTester().assertRenderedPage(HomePage.class);
         getWicketTester().clickLink("rightPanel:myContainer:menuList:0:imageLink");
         getWicketTester().assertRenderedPage(BrinEditPage.class);
 
         getWicketTester().setParameterForNextRequest("brinForm:title", title);
         getWicketTester().setParameterForNextRequest("brinForm:status", statusIndex);
         getWicketTester().submitForm("brinForm");
         getWicketTester().assertErrorMessages(new String[]{"Le champ 'creationDate' est obligatoire."});
 
         Session.get().cleanupFeedbackMessages();
         getWicketTester().setParameterForNextRequest("brinForm:title", title);
         getWicketTester().setParameterForNextRequest("brinForm:status", statusIndex);
         getWicketTester().setParameterForNextRequest("brinForm:creationDate", newCreationDate);
         getWicketTester().submitForm("brinForm");
 
         getWicketTester().assertNoErrorMessage();
     }
 
 
     private void updateBrinWithParams(int rowIndex, String newTitle, int newStatusIndex, String newCreationDate) {
         getWicketTester().clickLink("brinListContainer:brinList:" + rowIndex + ":editBrin");
         getWicketTester().assertRenderedPage(BrinEditPage.class);
 
         getWicketTester().setParameterForNextRequest("brinForm:title", newTitle);
         getWicketTester().setParameterForNextRequest("brinForm:status", newStatusIndex);
         getWicketTester().setParameterForNextRequest("brinForm:creationDate", newCreationDate);
 
         getWicketTester().submitForm("brinForm");
 
         getWicketTester().assertNoErrorMessage();
     }
 
 
     private void updateBrin(String newTitle, int newStatusIndex) {
         updateBrinWithParams(1, newTitle, newStatusIndex, "2001-01-12");
     }
 
 
     private void switchFilter(WicketFixture fixture, int index) {
         FormTester tester = fixture.newFormTester("leftPanel:filterForm");
         tester.select("brinFilters", index);
         tester.submit();
         fixture.executeAjaxEvent("leftPanel:filterForm:brinFilters", "onchange");
     }
 }
