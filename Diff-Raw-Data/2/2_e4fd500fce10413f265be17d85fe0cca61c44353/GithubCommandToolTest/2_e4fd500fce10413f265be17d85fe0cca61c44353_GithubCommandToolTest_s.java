 package net.codjo.tools.github;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.PrintStream;
 import net.codjo.test.common.LogString;
 import net.codjo.util.file.FileUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static net.codjo.test.common.matcher.JUnitMatchers.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 public class GithubCommandToolTest {
     private static final String endOfLine = System.getProperty("line.separator");
     private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
     private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
     private GithubCommandTool githubCommandTool;
     private GithubUtilService mockGithubService;
     private LogString logString = new LogString();
     private String proxyMessage;
 
 
     @Before
     public void setUpStreams() {
         githubCommandTool = new GithubCommandTool();
         logString.clear();
         mockGithubService = new GithubUtilMockService(logString);
         //TODO Testing System.out could be yeald to codjo-test ?
         System.setOut(new PrintStream(outContent));
         System.setErr(new PrintStream(errContent));
         GitConfigUtil gitConfigUtil = GithubCommandTool.tryToLoadProxyConfig();
         if (gitConfigUtil == null || gitConfigUtil.getProxyHost() != null) {
             proxyMessage = "";
         }
         else {
             proxyMessage = GithubCommandTool.PROXY_CONFIG_MESSAGE;
         }
     }
 
 
     @After
     public void cleanUpStreams() {
         System.setOut(null);
         System.setErr(null);
     }
 
 
     @Test
     public void test_badMethodPrintsHelp() {
         String[] args = new String[]{"badMethod", "githubUser", "githubPassword"};
 
         githubCommandTool.localMain(mockGithubService, args);
         assertThat(outContent.toString(), is(helpInConsole(true)));
         assertNoError();
     }
 
 
     @Test
     public void test_listDefaultRepositories() {
         String[] args = new String[]{"list", "githubUser", "githubPassword"};
         githubCommandTool.localMain(mockGithubService, args);
         assertThat(outContent.toString(), is(repositoryListInConsole("githubUser")));
         assertNoError();
     }
 
 
     @Test
     public void test_listRepositoriesFromOtherUser() {
         String[] args = new String[]{"list", "githubUser", "githubPassword", "codjo-sandbox"};
         githubCommandTool.localMain(mockGithubService, args);
         assertThat(outContent.toString(), is(repositoryListInConsole("githubUser")));
         assertNoError();
     }
 
 
     @Test
     public void test_forkRepository() {
         String[] args = new String[]{"fork", "githubUser", "githubPassword", "codjo-github-tools"};
         githubCommandTool.localMain(mockGithubService, args);
         logString.assertContent(
               "initGithubClient(githubUser, githubPassword), forkRepo(githubUser, githubPassword, codjo-github-tools)");
         assertThat(outContent.toString(), is(forkRepositoryInConsole()));
         assertNoError();
     }
 
 
     @Test
     public void test_deleteRepository() {
         String[] args = new String[]{"delete", "githubUser", "githubPassword", "codjo-github-tools"};
         String data = "Yes" + endOfLine;
         InputStream stdin = System.in;
         try {
             System.setIn(new ByteArrayInputStream(data.getBytes()));
             githubCommandTool.localMain(mockGithubService, args);
             logString.assertContent(
                   "initGithubClient(githubUser, githubPassword), deleteRepo(githubUser, githubPassword, codjo-github-tools)");
             assertThat(outContent.toString(), is(deleteRepositoryInConsole("githubUser")));
             assertNoError();
         }
         finally {
             System.setIn(stdin);
         }
     }
 
 
     @Test
     public void test_deleteRepositoryCanceledByUser() {
         String[] args = new String[]{"delete", "githubUser", "githubPassword", "codjo-github-tools"};
         String data = "No" + endOfLine;
         InputStream stdin = System.in;
         try {
             System.setIn(new ByteArrayInputStream(data.getBytes()));
             githubCommandTool.localMain(mockGithubService, args);
             logString.assertContent(
                   "initGithubClient(githubUser, githubPassword)");
             assertThat(outContent.toString(), is(deleteRepositoryCanceledByUserInConsole()));
             assertNoError();
         }
         finally {
             System.setIn(stdin);
         }
     }
 
 
     @Test
     public void test_deleteWithCodjoAccount() {
         String[] args = new String[]{"delete", "codjo", "githubPassword", "codjo-github-tools"};
         String data = "Yes" + endOfLine;
         InputStream stdin = System.in;
         try {
             System.setIn(new ByteArrayInputStream(data.getBytes()));
             githubCommandTool.localMain(mockGithubService, args);
             logString.assertContent(
                   "initGithubClient(codjo, githubPassword)");
             assertThat(outContent.toString(), is(deleteRepositoryWithCodjoAccountInConsole()));
             assertNoError();
         }
         finally {
             System.setIn(stdin);
         }
     }
 
 
     @Test
     public void test_postIssue() throws Exception {
         final String issueTitle = "myFirstIssue";
         File issueContentFile = new File(getClass().getResource("/" + issueTitle).toURI());
         String[] args = new String[]{"postIssue", "codjo", "password", "monRepo", issueTitle, "closed",
                                      issueContentFile.getCanonicalPath(), "label_1", "label_2"};
         InputStream stdin = System.in;
         try {
             githubCommandTool.localMain(mockGithubService, args);
             logString.assertContent(
                   "initGithubClient(codjo, password), "
                  + "postIssue(codjo, password, monRepo, myFirstIssue, " + issueContentFile.getPath() + ", closed), "
                   + "addLabels(codjo, password, monRepo, myFirstIssue, [label_1, label_2])");
             assertThat(outContent.toString(),
                        is(postIssueWithCodjoAccountInConsole(issueTitle, FileUtil.loadContent(issueContentFile))));
             assertNoError();
         }
         finally {
             System.setIn(stdin);
         }
     }
 
 
     @Test
     public void test_noParameterPrintsHelp
           () {
         String[] args = new String[]{};
         githubCommandTool.localMain(mockGithubService, args);
         assertEquals(helpInConsole(false), outContent.toString());
         assertNoError();
     }
 
 
     @Test
     public void test_listOpenedPullRequest() {
         String[] args = new String[]{"events", "codjo", "githubPassword"};
         githubCommandTool.localMain(mockGithubService, args);
         logString.assertContent("initGithubClient(codjo, githubPassword)");
         assertThat(outContent.toString(), equalTo(listEventsSinceLastStabilisationInConsole()));
         assertNoError();
     }
 
 
     private String helpInConsole(boolean wihtQuotas) {
         String result = ConsoleManager.OCTOPUS + endOfLine
                         + proxyMessage +
                         " Did you mean :" + endOfLine +
                         "         - gh list [ACCOUNT_NAME] : list all repositories from ACCOUNT_NAME" + endOfLine +
                         "         - gh fork REPO_NAME      : fork a repository from codjo" + endOfLine +
                         "         - gh delete REPO_NAME    : delete a repository if exists" + endOfLine +
                         "         - gh postIssue REPO_NAME ISSUE_TITLE STATE ISSUE_CONTENT_FILE_PATH    : add a new issue in repository"
                         + endOfLine +
                         "         - gh events [ACCOUNT_NAME] [ACCOUNT_PASSWORD]    : list all events since last stabilisation (last pull request with 'For Release' title"
                         + endOfLine;
 
         if (wihtQuotas) {
             result += printApiQuota();
         }
         return result;
     }
 
 
     private String repositoryListInConsole(String githubUser) {
         return ConsoleManager.OCTOPUS + endOfLine + "\n"
                + "Here are the repositories from " + githubUser + endOfLine
                + "\tLast push\t\t\t\tName" + endOfLine
                + "\t19/07/2012 00:00\t\tcodjo-repoOne" + endOfLine
                + "\t05/07/2012 00:00\t\tcodjo-repoTwo" + endOfLine
                + printApiQuota();
     }
 
 
     private String forkRepositoryInConsole() {
         return ConsoleManager.OCTOPUS + "" + endOfLine
                + "\tRepository codjo-github-tools has been forked from codjo." + endOfLine
                + printApiQuota();
     }
 
 
     private String deleteRepositoryInConsole(String githubUser) {
         return ConsoleManager.OCTOPUS + "" + endOfLine
                + "Do you really want to delete the repository codjo-github-tools on  githubUser account ? (y = yes / n = no/) : \n"
                + "\tRepository codjo-github-tools has been removed from " + githubUser + " account" + endOfLine
                + printApiQuota();
     }
 
 
     private String deleteRepositoryCanceledByUserInConsole() {
         return ConsoleManager.OCTOPUS + "" + endOfLine
                + "Do you really want to delete the repository codjo-github-tools on  githubUser account ? (y = yes / n = no/) : "
                + printApiQuota();
     }
 
 
     private String deleteRepositoryWithCodjoAccountInConsole() {
         return ConsoleManager.OCTOPUS + "" + endOfLine
                + "\tRepositoy deletion with codjo account is not allowed." + endOfLine
                + "\t--> Please, use web interface instead." + endOfLine
                + printApiQuota();
     }
 
 
     private String postIssueWithCodjoAccountInConsole(String title, String content) {
         return ConsoleManager.OCTOPUS + "" + endOfLine
                + "\tIssue " + title + " has been created with codjo account" + endOfLine
                + "\twith the following content:" + endOfLine
                + content + endOfLine
                + printApiQuota();
     }
 
 
     private String listEventsSinceLastStabilisationInConsole() {
         return ConsoleManager.OCTOPUS + "" + endOfLine
                + "\tHere are the last events on codjo"
                + endOfLine
                + "\tUser\t\t\t\t\tName\t\t\t\tUrl" + endOfLine
                + "\tcodjo-sandbox\t\tfirst pullRequest\t\thttp://urlr/pullRequest/1" + endOfLine
                + "\tgonnot\t\tSecond pullRequest\t\thttp://urlr/pullRequest/2/other " + endOfLine
                + printApiQuota();
     }
 
 
     private String printApiQuota() {
         return ConsoleManager.printApiQuota(5) + endOfLine;
     }
 
 
     private void assertNoError() {
         assertEquals("", errContent.toString());
     }
 }
