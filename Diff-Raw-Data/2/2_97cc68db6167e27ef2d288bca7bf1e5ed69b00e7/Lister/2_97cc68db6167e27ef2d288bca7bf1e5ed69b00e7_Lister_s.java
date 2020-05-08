 package org.jenkinsci.backend.github;
 
 import hudson.plugins.jira.soap.ConfluenceSoapService;
 import hudson.plugins.jira.soap.RemotePage;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentFactory;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.io.SAXReader;
 import org.jvnet.hudson.confluence.Confluence;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.kohsuke.github.GHOrganization;
 import org.kohsuke.github.GHRepository;
 import org.kohsuke.github.GitHub;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Properties;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class Lister {
     @Option(name="-wiki",usage="Upload to Jenkins wiki page")
     public String wikiPage;
 
     @Option(name="-o",usage="Output the list to the given file")
     public File output;
 
     public static void main(String[] args) throws Exception {
         Lister app = new Lister();
         CmdLineParser p = new CmdLineParser(app);
         try {
             p.parseArgument(args);
             app.run();
             System.exit(0);
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             p.printUsage(System.err);
             System.exit(1);
         }
     }
 
     public void run() throws Exception {
         ByteArrayOutputStream contents = new ByteArrayOutputStream();
         PrintStream out = System.out;
         if (output!=null)
             out = new PrintStream(new FileOutputStream(output));
 
         out = new PrintStream(new TeeOutputStream(out,contents));
 
         DocumentFactory factory = new DocumentFactory();
         factory.setXPathNamespaceURIs(Collections.singletonMap("m","http://maven.apache.org/POM/4.0.0"));
 
         GitHub gh = GitHub.connectAnonymously();
         GHOrganization org = gh.getOrganization("jenkinsci");
 
         out.println(
                "!https://github.com/images/modules/header/logov3.png|align=left,hspace=30,vspace=30! " +
                 "This auto-generated page lists all the GitHub repositories of the Jenkins project.\n" +
                 "\n" +
                 "||Repository||description||groupId||artifactId||");
 
         for (GHRepository r : org.getRepositories().values()) {
             String desc = r.getDescription();
             if (desc == null) desc = "";
             out.printf("|[%s|%s]| %s|", // Space before %s to ensure no "||" (makes TH)
                     r.getName(),r.getUrl(),
                     desc.replace("[", "\\[")); // Escape [ to avoid wiki links
 
             try {
                 URL pom = new URL(r.getUrl() + "/raw/master/pom.xml");
                 Document dom = new SAXReader(factory).read(pom);
 
                 Element groupId    = getElement(dom, "groupId");
                 Element artifactId = getElement(dom, "artifactId");
 
                 out.printf("%s|%s|", groupId.getTextTrim(), artifactId.getTextTrim());
             } catch (DocumentException e) {
                 if (e.getNestedException() instanceof FileNotFoundException) {
                     // no POM
                 } else {
                     e.printStackTrace();
                 }
             }
 
             out.println();
         }
 
         out.flush();
 
         if (wikiPage!=null) {
             System.err.println("Uploading to "+wikiPage);
             ConfluenceSoapService service = Confluence.connect(new URL("https://wiki.jenkins-ci.org/"));
 
             Properties props = new Properties();
             File credential = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org");
             if (!credential.exists())
                 throw new IOException("You need to have userName and password in "+credential);
             props.load(new FileInputStream(credential));
             String token = service.login(props.getProperty("userName"),props.getProperty("password"));
 
             RemotePage p = service.getPage(token, "JENKINS", wikiPage);
             p.setContent(contents.toString());
             service.storePage(token,p);
         }
     }
 
     private static Element getElement(Document dom, String elementName) {
         Node n = dom.selectSingleNode("/project/"+ elementName);
         if(n==null)
             n = dom.selectSingleNode("/m:project/m:"+elementName);
 
         if (n!=null && n.getText().startsWith("${parent"))
             n = null;   // poor man's way of resolving ${parent.groupId} etc 
 
         if(n==null)
             n = dom.selectSingleNode("/project/parent/"+ elementName);
         if(n==null)
             n = dom.selectSingleNode("/m:project/m:parent/m:"+ elementName);
         return (Element)n;
     }
 }
