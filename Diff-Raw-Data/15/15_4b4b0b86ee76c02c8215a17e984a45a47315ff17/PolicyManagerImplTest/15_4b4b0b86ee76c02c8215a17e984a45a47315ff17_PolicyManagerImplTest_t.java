 package org.wyona.security.test;
 
 import java.io.File;
 
 import org.wyona.security.core.api.PolicyManager;
 import org.wyona.security.core.api.Item;
 import org.wyona.security.core.api.Usecase;
 import org.wyona.security.core.api.User;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.impl.PolicyManagerFactoryImplVersion2;
 //import org.wyona.security.impl.PolicyManagerImpl;
 import org.wyona.security.impl.yarep.YarepIdentityManagerImpl;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import junit.framework.TestCase;
 
 import org.w3c.dom.Document;
 
 import org.apache.log4j.Category;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.URIResolver;
 
 /**
  * Test for the PolicyManager.
  */
 public class PolicyManagerImplTest extends TestCase {
 
     private static Category log = Category.getInstance(PolicyManagerImplTest.class);
 
     protected Repository repoPolicies;
     protected PolicyManager policyManager;
     
     /**
      * Note that the directory build/repository is added to the CLASSPATH by the build.xml file!
      */
     public void setUp() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
 
         repoPolicies = repoFactory.newRepository("policies-v2-repository", new File("repository-policies-version2/repository.xml"));
         if (log.isDebugEnabled()) log.debug("Repo config file: " + repoPolicies.getConfigFile());
         //policyManager = new PolicyManagerFactoryImplVersion2().newPolicyManager(repoPolicies);
 
         Document config = getDocument("http://www.wyona.org/security/1.0", "policy-manager-config");
         config.getDocumentElement().appendChild(config.createTextNode("repository-policies-version2/repository.xml"));
         policyManager = new PolicyManagerFactoryImplVersion2().newPolicyManager(config, new RepositoryClasspathResolver());
 
         //repoPolicies = repoFactory.newRepository("policies-repository", new File("repository1/config/repository.xml"));
         //policyManager = new PolicyManagerImpl(repo);
     }
     
     /*
      * Test simple authorization
      */
     public void testAuthorization() throws Exception {
         RepositoryFactory repoFactory = new RepositoryFactory();
         Repository repoIdentities = repoFactory.newRepository("identities-repository", new File("repository2/repository.xml"));
        IdentityManager identityManager = new YarepIdentityManagerImpl(repoIdentities, true);
 
         User lenya = identityManager.getUserManager().getUser("lenya");
         User alice = identityManager.getUserManager().getUser("alice");
 
         assertFalse(policyManager.authorize("/hello", new Identity(alice), new Usecase("view")));
         assertTrue(policyManager.authorize("/hello", new Identity(lenya), new Usecase("view")));
 
         assertFalse(policyManager.authorize("/hello", new Identity(alice), new Usecase("read")));
         assertFalse(policyManager.authorize("/hello", new Identity(lenya), new Usecase("read")));
     }
 
     /**
      * Create a DOM Document
      */
     private Document getDocument(String namespace, String localname) throws Exception {
         javax.xml.parsers.DocumentBuilderFactory dbf= javax.xml.parsers.DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);
         javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();
         org.w3c.dom.DOMImplementation impl = parser.getDOMImplementation();
         org.w3c.dom.DocumentType doctype = null;
         return impl.createDocument(namespace, localname, doctype);
     }
 }
 
 /**
  *
  */
 class RepositoryClasspathResolver implements URIResolver {
 
     /**
      *
      */
     public Source resolve(String href, String base) throws TransformerException {
         java.net.URL url = RepositoryClasspathResolver.class.getClassLoader().getResource(href);
         Source source = new StreamSource();
         source.setSystemId(url.getFile());
         return source;
     }
 }
