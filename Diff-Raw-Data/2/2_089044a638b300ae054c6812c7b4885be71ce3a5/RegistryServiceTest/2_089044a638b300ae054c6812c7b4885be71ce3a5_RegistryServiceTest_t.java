 package org.mule.galaxy.web.server;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.acegisecurity.Authentication;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.providers.AuthenticationProvider;
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 import org.mule.galaxy.web.rpc.ArtifactGroup;
 import org.mule.galaxy.web.rpc.ArtifactVersionInfo;
 import org.mule.galaxy.web.rpc.BasicArtifactInfo;
 import org.mule.galaxy.web.rpc.ExtendedArtifactInfo;
 import org.mule.galaxy.web.rpc.RegistryService;
 import org.mule.galaxy.web.rpc.TransitionResponse;
 import org.mule.galaxy.web.rpc.WApprovalMessage;
 import org.mule.galaxy.web.rpc.WComment;
 import org.mule.galaxy.web.rpc.WGovernanceInfo;
 import org.mule.galaxy.web.rpc.WIndex;
 import org.mule.galaxy.web.rpc.WWorkspace;
 import org.springframework.context.ApplicationContext;
 
 public class RegistryServiceTest extends AbstractGalaxyTest {
     protected RegistryService gwtRegistry;
     
     
     @Override
     protected void onSetUp() throws Exception {
         super.onSetUp();
 
         createSecureContext(applicationContext, "admin", "admin");
     }
 
     @Override
     protected String[] getConfigLocations() {
         return new String[] { "/META-INF/applicationContext-core.xml", 
                               "/META-INF/applicationContext-acegi-security.xml", 
                               "/META-INF/applicationContext-web.xml" };
         
     }
     
     public void testArtifactOperations() throws Exception {
         Collection workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
         
         Collection artifactTypes = gwtRegistry.getArtifactTypes();
         assertTrue(artifactTypes.size() > 0);
         
         Collection artifacts = gwtRegistry.getArtifacts(null, null, new HashSet(), null);
         
         assertTrue(artifacts.size() > 0);
         
         ArtifactGroup g1 = null;
         for (Iterator itr = artifacts.iterator(); itr.hasNext();) {
             ArtifactGroup group = (ArtifactGroup)itr.next();
             
             if ("Mule Configurations".equals(group.getName())) {
                 g1 = group;
             }
         }
         assertNotNull(g1);
         
         List columns = g1.getColumns();
         assertEquals(6, columns.size());
         
         List rows = g1.getRows();
         assertEquals(1, rows.size());
         
         BasicArtifactInfo a = (BasicArtifactInfo) g1.getRows().get(0);
         Collection deps = gwtRegistry.getDependencyInfo(a.getId());
         assertEquals(0, deps.size());
         
         // Test reretrieving the artifact
         g1 = gwtRegistry.getArtifact(a.getId());
         g1 = (ArtifactGroup) artifacts.iterator().next();
 
         for (Iterator itr = artifacts.iterator(); itr.hasNext();) {
             ArtifactGroup group = (ArtifactGroup)itr.next();
 
             if ("Mule Configurations".equals(group.getName())) {
                 g1 = group;
             }
         }
         assertNotNull(g1);
         assertEquals("Mule Configurations", g1.getName());
         
         columns = g1.getColumns();
         assertEquals(6, columns.size());
         
         rows = g1.getRows();
         assertEquals(1, rows.size());
         
         gwtRegistry.setProperty(a.getId(), "location", "Grand Rapids");
         
         Artifact artifact = registry.getArtifact(a.getId());
         assertEquals("Grand Rapids", artifact.getProperty("location"));
         
         // try adding a comment
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         assertNotNull(auth);
         Object principal = auth.getPrincipal();
         assertNotNull(principal);
         
         WComment wc = gwtRegistry.addComment(a.getId(), null, "Hello World");
         assertNotNull(wc);
         
         WComment wc2 = gwtRegistry.addComment(a.getId(), wc.getId(), "Hello World");
         assertNotNull(wc2);
         
         // get the extended artifact info again
         g1 = gwtRegistry.getArtifact(a.getId());
         
         rows = g1.getRows();
         ExtendedArtifactInfo ext = (ExtendedArtifactInfo) rows.get(0);
         
         List comments = ext.getComments();
         assertEquals(1, comments.size());
         
         WComment wc3 = (WComment) comments.get(0);
         assertEquals(1, wc3.getComments().size());
         
         assertEquals("/api/registry/Default Workspace/hello-config.xml", ext.getArtifactLink());
        assertEquals("/api/comments", ext.getCommentsFeedLink());
         
         // test desc
         gwtRegistry.setDescription(a.getId(), "test desc");
     }
     
     
     private static void createSecureContext(final ApplicationContext ctx, final String username, final String password) {
         AuthenticationProvider provider = (AuthenticationProvider) ctx.getBean("daoAuthenticationProvider");
         Authentication auth = provider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
         SecurityContextHolder.getContext().setAuthentication(auth);
     }
     
     public void testWorkspaces() throws Exception {
         Collection workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
         
         WWorkspace w = (WWorkspace) workspaces.iterator().next();
         
         gwtRegistry.addWorkspace(w.getId(), "Foo");
         
         workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
         
         w = (WWorkspace) workspaces.iterator().next();
         assertNotNull(w.getWorkspaces());
         assertEquals(1, w.getWorkspaces().size());
         
         assertNotNull(w.getPath());
     }
     
     public void testGovernanceOperations() throws Exception {
         Collection artifacts = gwtRegistry.getArtifacts(null, null, new HashSet(), null);
         ArtifactGroup g1 = (ArtifactGroup) artifacts.iterator().next();
         
         BasicArtifactInfo a = (BasicArtifactInfo) g1.getRows().get(0);
         
         WGovernanceInfo gov = gwtRegistry.getGovernanceInfo(a.getId());
         
         assertEquals("Created", gov.getCurrentPhase());
         
         Collection nextPhases = gov.getNextPhases();
         assertNotNull(nextPhases);
         assertEquals(1, nextPhases.size());
         
         String next = (String) nextPhases.iterator().next();
         TransitionResponse res = gwtRegistry.transition(a.getId(), next);
         
         assertTrue(res.isSuccess());
         
         // activate a policy which will make transitioning fail
         FauxPolicy policy = new FauxPolicy();
         policyManager.addPolicy(policy);
         policyManager.setActivePolicies(lifecycleManager.getDefaultLifecycle(), policy);
         
         // Try transitioning
         gov = gwtRegistry.getGovernanceInfo(a.getId());
         
         nextPhases = gov.getNextPhases();
         assertNotNull(nextPhases);
         assertEquals(1, nextPhases.size());
         
         next = (String) nextPhases.iterator().next();
         
         res = gwtRegistry.transition(a.getId(), next);
         
         assertFalse(res.isSuccess());
         assertEquals(1, res.getMessages().size());
         
         WApprovalMessage msg = (WApprovalMessage) res.getMessages().iterator().next();
         assertEquals("Not approved", msg.getMessage());
         assertFalse(msg.isWarning());
     }
     
     public void testVersioningOperations() throws Exception {
         Set result = registry.search("select artifact where wsdl.service = 'HelloWorldService'");
         
         Artifact a = (Artifact) result.iterator().next();
         
         registry.newVersion(a, getResourceAsStream("/wsdl/imports/hello.wsdl"), "0.2", getAdmin());
         
         
         Collection versions = gwtRegistry.getArtifactVersions(a.getId());
         assertEquals(2, versions.size());
         
         ArtifactVersionInfo info = (ArtifactVersionInfo) versions.iterator().next();
         assertEquals("0.2", info.getVersionLabel());
         assertNotNull(info.getLink());
         assertNotNull(info.getCreated());
         assertEquals("Administrator", info.getAuthorName());
         assertEquals("admin", info.getAuthorUsername());
         
         TransitionResponse res = gwtRegistry.setActive(a.getId(), "0.1");
         assertTrue(res.isSuccess());
     }
     
     public void testIndexes() throws Exception {
         Collection indexes = gwtRegistry.getIndexes();
         
         assertTrue(indexes.size() > 0);
         
         WIndex idx = gwtRegistry.getIndex("wsdl.service");
         assertNotNull(idx);
         assertNotNull(idx.getResultType());
     }
     
     private final class FauxPolicy implements ArtifactPolicy {
         public String getDescription() {
             return "Faux policy description";
         }
 
         public boolean applies(Artifact a) {
             return true;
         }
 
         public String getId() {
             return "faux";
         }
 
         public String getName() {
             return "Faux policy";
         }
 
         public Collection<ApprovalMessage> isApproved(Artifact a, ArtifactVersion previous, ArtifactVersion next) {
             return Arrays.asList(new ApprovalMessage("Not approved"));
         }
 
         public void setRegistry(Registry registry) {
             
         }
     }
 
 }
