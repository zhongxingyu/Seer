 package com.unitedinternet.jenkins.plugins.scm2job;
 
 import java.util.ArrayList;
 import hudson.model.FreeStyleProject;
 import hudson.model.Project;
 import hudson.plugins.git.GitSCM;
 import hudson.scm.NullSCM;
 import hudson.scm.SubversionSCM;
 
 import org.jvnet.hudson.test.HudsonTestCase;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * Tests whether git and svn jobs are correctly found
  * and get displayed correctly as text and as links
  * @author kstutz
  */
 public class SCM2JobTest extends HudsonTestCase {
 
     private ArrayList<Project> projects = new ArrayList<Project>();
 
     private String[] gitProjectNames = {"gitproject1", "gitproject2", "gitproject3"};
 
     private String[] gitUrls = {"git://github.com/stefanbrausch/scm2job-plugin.git",
                                 "git://github.com/abracadabra/hmpf.git",
                                 "git://github.com/abracadabra/hmpf.git", };
 
     private String[] svnProjectNames = {"svnproject1", "svnproject2", "svnproject3"};
 
     private String[] svnUrls = {"https://svn.foo.org/mojo/trunk/foobar-plugin",
                                 "https://svn.foo.org/mojo/trunk/foobar-plugin",
                                 "https://svn.foo.org/mojo/trunk/lorem", };
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         
         for (int i = 0; i < gitProjectNames.length; i++) {
             final FreeStyleProject project = createFreeStyleProject(gitProjectNames[i]);
             project.setScm(new GitSCM(gitUrls[i]));
             projects.add(project);
         }
      
         for (int i = 0; i < svnProjectNames.length; i++) {
             final FreeStyleProject project = createFreeStyleProject(svnProjectNames[i]);
             project.setScm(new SubversionSCM(svnUrls[i]));
             projects.add(project);
         }
  
         final FreeStyleProject project = createFreeStyleProject("nullproject");
         project.setScm(new NullSCM());
         projects.add(project);
     }
 
 
     //Tests whether the two projects with the same git repo are found
     public void testGitPlain() throws Exception {
         final WebClient webClient =  new WebClient();
         final HtmlPage htmlpage = webClient.goTo("scm2job");
         
         final HtmlElement element = htmlpage.getElementByName("path");
         element.type(gitUrls[1]);
        final HtmlPage results = submit(element.getEnclosingForm(), "submit");
         
         assertStringContains(results.asXml(), gitProjectNames[1]);
         assertStringContains(results.asXml(), gitProjectNames[2]);
     }
 
     //Tests whether the two projects with the same svn location are found
     public void testSVNPlain() throws Exception {
         final WebClient webClient =  new WebClient();
         final HtmlPage htmlpage = webClient.goTo("scm2job");
         
         final HtmlElement element = htmlpage.getElementByName("path");
         element.type(svnUrls[0]);
        final HtmlPage results = submit(element.getEnclosingForm(), "submit");
         
         assertStringContains(results.asXml(), svnProjectNames[0]);
         assertStringContains(results.asXml(), svnProjectNames[1]);
     }
     
     //Tests whether the links to the two git projects are found
     public void testGitFancy() throws Exception {
         final WebClient webClient =  new WebClient();
         final HtmlPage htmlpage = webClient.goTo("scm2job");
         
         final HtmlElement textbox = htmlpage.getElementByName("path");
         textbox.type(gitUrls[1]);
         final HtmlElement radioButton = htmlpage.getElementsByName("format").get(1);
         radioButton.click();
        final HtmlPage results = submit(textbox.getEnclosingForm(), "submit");
         
         assertStringContains(results.asXml(), "<a href=\"../job/" + gitProjectNames[1] + "/\">");
         assertStringContains(results.asXml(), "<a href=\"../job/" + gitProjectNames[2] + "/\">");
     }
     
     //Tests whether the links to the two svn projects are found
     public void testSVNFancy() throws Exception {
         final WebClient webClient =  new WebClient();
         final HtmlPage htmlpage = webClient.goTo("scm2job");
         
         final HtmlElement textbox = htmlpage.getElementByName("path");
         textbox.type(svnUrls[0]);
         final HtmlElement radioButton = htmlpage.getElementsByName("format").get(1);
         radioButton.click();
        final HtmlPage results = submit(textbox.getEnclosingForm(), "submit");
         
         assertStringContains(results.asXml(), "<a href=\"../job/" + svnProjectNames[0] + "/\">");
         assertStringContains(results.asXml(), "<a href=\"../job/" + svnProjectNames[1] + "/\">");
     }
     
     //Tests whether error message is displayed when no path submitted
     public void testNoPathGiven() throws Exception {
         final WebClient webClient =  new WebClient();
         final HtmlPage htmlpage = webClient.goTo("scm2job");
 
         final HtmlElement textbox = htmlpage.getElementByName("path");
        final HtmlPage results = submit(textbox.getEnclosingForm(), "submit");
 
         assertStringContains(results.asXml(), Messages.pathMissing());
     }
 
     //Tests whether error message is displayed when no job found
     public void testNoJobFound() throws Exception {
         final WebClient webClient =  new WebClient();
         final HtmlPage htmlpage = webClient.goTo("scm2job");
         
         final HtmlElement textbox = htmlpage.getElementByName("path");
         textbox.type("bla");
        final HtmlPage results = submit(textbox.getEnclosingForm(), "submit");
         
         assertStringContains(results.asXml(), "No Jenkins job found");
     }
 }
