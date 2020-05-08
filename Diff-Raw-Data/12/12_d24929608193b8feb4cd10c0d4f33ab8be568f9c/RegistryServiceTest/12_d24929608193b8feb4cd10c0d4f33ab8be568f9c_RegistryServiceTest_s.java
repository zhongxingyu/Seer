 package org.mule.galaxy.web.server;
 
 import java.io.InputStream;
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
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.PropertyDescriptor;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.Workspace;
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
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WPhase;
 import org.mule.galaxy.web.rpc.WProperty;
 import org.mule.galaxy.web.rpc.WPropertyDescriptor;
 import org.mule.galaxy.web.rpc.WUser;
 import org.mule.galaxy.web.rpc.WWorkspace;
 import org.springframework.context.ApplicationContext;
 
 public class RegistryServiceTest extends AbstractGalaxyTest {
     protected RegistryService gwtRegistry;
 
     @Override
     protected String[] getConfigLocations() {
         return new String[] { "/META-INF/applicationContext-core.xml", 
                               "/META-INF/applicationContext-acegi-security.xml", 
                               "/META-INF/applicationContext-web.xml",
                               "/META-INF/applicationContext-test.xml" };
     }
 
     protected Artifact importHelloTestWSDL() throws Exception
     {
         InputStream helloWsdl = getResourceAsStream("/wsdl/hello-noOperation.wsdl");
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
 
         ArtifactResult ar = registry.createArtifact(workspace,
                                                     "application/xml",
                                                     "hello-noOperation.wsdl",
                                                     "0.1", helloWsdl, getAdmin());
         return ar.getArtifact();
     }
 
     public void testArtifactOperations() throws Exception
     {
         //importHelloTestWSDL();
         Collection workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
 
         Collection artifactTypes = gwtRegistry.getArtifactTypes();
         assertTrue(artifactTypes.size() > 0);
         
         // Grab a group of artifacts
         Collection artifacts = gwtRegistry.getArtifacts(null, null, new HashSet(), null, 0, 20).getResults();
 
         assertTrue(artifacts.size() > 0);
 
         ArtifactGroup g1 = null;
         BasicArtifactInfo info = null;
         for (Iterator itr = artifacts.iterator(); itr.hasNext();) {
             ArtifactGroup group = (ArtifactGroup)itr.next();
 
             if ("WSDL Documents".equals(group.getName())) {
                 for (Iterator itr2 = group.getRows().iterator(); itr2.hasNext();)
                 {
                     info = (BasicArtifactInfo)itr2.next();
                     if(info.getName().equals("hello.wsdl"))
                     {
                         g1 = group;
                         break;
                     }
                 }
             }
         }
         assertNotNull(g1);
 
         List columns = g1.getColumns();
         assertTrue(columns.size() > 0);
 
         List rows = g1.getRows();
         assertEquals(2, rows.size());
 
         Collection deps = gwtRegistry.getDependencyInfo(info.getId());
         assertEquals(1, deps.size());
 
         // Test reretrieving the artifact
         g1 = gwtRegistry.getArtifact(info.getId());
         g1 = (ArtifactGroup) artifacts.iterator().next();
 
         for (Iterator itr = artifacts.iterator(); itr.hasNext();) {
             ArtifactGroup group = (ArtifactGroup)itr.next();
 
             if ("WSDL Documents".equals(group.getName())) {
                 for (Iterator itr2 = group.getRows().iterator(); itr2.hasNext();)
                 {
                     info = (BasicArtifactInfo)itr2.next();
                     if(info.getName().equals("hello.wsdl"))
                     {
                         g1 = group;
                         break;
                     }
                 }
             }
         }
         assertNotNull(g1);
 
         columns = g1.getColumns();
         assertTrue(columns.size() > 0);
 
         rows = g1.getRows();
         assertEquals(2, rows.size());
 
         gwtRegistry.setProperty(info.getId(), "location", "Grand Rapids");
         
         Artifact artifact = registry.getArtifact(info.getId());
         assertEquals("Grand Rapids", artifact.getProperty("location"));
         artifact.setProperty("hidden", "value");
         artifact.setVisible("hidden", false);
         registry.save(artifact);
         
         // see if the hidden property shows up
        g1 = gwtRegistry.getArtifact(info.getId());
        for (Iterator itr2 = g1.getRows().iterator(); itr2.hasNext();) {
            info = (BasicArtifactInfo)itr2.next();
            if (info.getName().equals("hello.wsdl"))
            {
                break;
            }
        }
        assertNotNull(info);
        ExtendedArtifactInfo exInfo = (ExtendedArtifactInfo) info;
        ArtifactVersionInfo av = (ArtifactVersionInfo) exInfo.getVersions().iterator().next();
         
         WProperty hiddenProp = null;
         for (Object o : av.getProperties()) {
             WProperty prop = (WProperty) o;
             
             if (prop.getName().equals("hidden")) {
                 hiddenProp = prop;
                 break;
             }
         }
             
         assertNotNull(hiddenProp);
         
         // try adding a comment
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         assertNotNull(auth);
         Object principal = auth.getPrincipal();
         assertNotNull(principal);
 
         WComment wc = gwtRegistry.addComment(info.getId(), null, "Hello World");
         assertNotNull(wc);
 
         WComment wc2 = gwtRegistry.addComment(info.getId(), wc.getId(), "Hello World");
         assertNotNull(wc2);
 
         // get the extended artifact info again
         g1 = gwtRegistry.getArtifact(info.getId());
 
         rows = g1.getRows();
         ExtendedArtifactInfo ext = (ExtendedArtifactInfo) rows.get(0);
 
         List comments = ext.getComments();
         assertEquals(1, comments.size());
 
         WComment wc3 = (WComment) comments.get(0);
         assertEquals(1, wc3.getComments().size());
 
         assertEquals("/api/registry/Default Workspace/hello.wsdl", ext.getArtifactLink());
         assertEquals("/api/comments", ext.getCommentsFeedLink());
 
         // test desc
         gwtRegistry.setDescription(info.getId(), "test desc");
     }
     
     public void testWorkspaces() throws Exception {
         Collection workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
         
         WWorkspace w = (WWorkspace) workspaces.iterator().next();
         
         gwtRegistry.addWorkspace(w.getId(), "Foo", null);
         
         workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
         
         w = (WWorkspace) workspaces.iterator().next();
         assertNotNull(w.getWorkspaces());
         assertEquals(1, w.getWorkspaces().size());
         
         assertNotNull(w.getPath());
     }
     
     public void testUserInfo() throws Exception {
         WUser user = gwtRegistry.getUserInfo();
         
         assertNotNull(user.getUsername());
         
         Collection permissions = user.getPermissions();
         assertTrue(permissions.size() > 0);
         
         assertTrue(permissions.contains("MANAGE_USERS"));
     }
     
     public void testGovernanceOperations() throws Exception {
         Collection artifacts = gwtRegistry.getArtifacts(null, null, new HashSet(), null, 0, 20).getResults();
         ArtifactGroup g1 = (ArtifactGroup) artifacts.iterator().next();
         
         BasicArtifactInfo a = (BasicArtifactInfo) g1.getRows().get(0);
         ExtendedArtifactInfo ext = (ExtendedArtifactInfo) gwtRegistry.getArtifact(a.getId()).getRows().get(0);
         
         Collection versions = ext.getVersions();
         ArtifactVersionInfo v = (ArtifactVersionInfo) versions.iterator().next();
         
         WGovernanceInfo gov = gwtRegistry.getGovernanceInfo(v.getId());
         
         assertEquals("Created", gov.getCurrentPhase());
         
         Collection nextPhases = gov.getNextPhases();
         assertNotNull(nextPhases);
         assertEquals(1, nextPhases.size());
         
         WPhase next = (WPhase) nextPhases.iterator().next();
         TransitionResponse res = gwtRegistry.transition(v.getId(), next.getId());
         
         assertTrue(res.isSuccess());
         
         // activate a policy which will make transitioning fail
         FauxPolicy policy = new FauxPolicy();
         policyManager.addPolicy(policy);
 
         // Try transitioning
         gov = gwtRegistry.getGovernanceInfo(v.getId());
         
         nextPhases = gov.getNextPhases();
         assertNotNull(nextPhases);
         assertEquals(1, nextPhases.size());
 
         next = (WPhase) nextPhases.iterator().next();
         
         policyManager.setActivePolicies(Arrays.asList(lifecycleManager.getPhaseById(next.getId())), policy);
         
         res = gwtRegistry.transition(v.getId(), next.getId());
         
         assertFalse(res.isSuccess());
         assertEquals(1, res.getMessages().size());
         
         WApprovalMessage msg = (WApprovalMessage) res.getMessages().iterator().next();
         assertEquals("Not approved", msg.getMessage());
         assertFalse(msg.isWarning());
     }
     
     public void testVersioningOperations() throws Exception {
         Set result = registry.search("select artifact where wsdl.service = 'HelloWorldService'", 0, 100).getResults();
         
         Artifact a = (Artifact) result.iterator().next();
         
         registry.newVersion(a, getResourceAsStream("/wsdl/imports/hello.wsdl"), "0.2", getAdmin());
         
         ExtendedArtifactInfo ext = (ExtendedArtifactInfo) gwtRegistry.getArtifact(a.getId()).getRows().get(0);
         
         Collection versions = ext.getVersions();
         assertEquals(2, versions.size());
         
         ArtifactVersionInfo info = (ArtifactVersionInfo) versions.iterator().next();
         assertEquals("0.2", info.getVersionLabel());
         assertNotNull(info.getLink());
         assertNotNull(info.getCreated());
         assertEquals("Administrator", info.getAuthorName());
         assertEquals("admin", info.getAuthorUsername());
         
         TransitionResponse res = gwtRegistry.setDefault(info.getId());
         assertTrue(res.isSuccess());
     }
     
     public void testIndexes() throws Exception {
         Collection indexes = gwtRegistry.getIndexes();
         
         assertTrue(indexes.size() > 0);
         
         WIndex idx = gwtRegistry.getIndex(((WIndex)indexes.iterator().next()).getId());
         assertNotNull(idx.getId());
         assertNotNull(idx.getResultType());
         assertNotNull(idx.getIndexer());
         gwtRegistry.saveIndex(idx);
     }
     
     public void testLifecycles() throws Exception {
         Collection lifecycles = gwtRegistry.getLifecycles();
         
         assertEquals(1, lifecycles.size());
         
         WLifecycle wl = (WLifecycle) lifecycles.iterator().next();
         assertEquals("Default", wl.getName());
         assertNotNull(wl.getId());
      
         wl.setName("newname");
         
         gwtRegistry.saveLifecycle(wl);
         
         lifecycles = gwtRegistry.getLifecycles();
         assertEquals(1, lifecycles.size());
         
         wl = (WLifecycle) lifecycles.iterator().next();
         assertEquals("newname", wl.getName());
     }
     
     public void testPDs() throws Exception {
         WPropertyDescriptor wpd = new WPropertyDescriptor();
         
         wpd.setName("test");
         wpd.setDescription("test");
         
         gwtRegistry.savePropertyDescriptor(wpd);
         
         PropertyDescriptor pd = registry.getPropertyDescriptor(wpd.getId());
         
         assertNotNull(pd);
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
