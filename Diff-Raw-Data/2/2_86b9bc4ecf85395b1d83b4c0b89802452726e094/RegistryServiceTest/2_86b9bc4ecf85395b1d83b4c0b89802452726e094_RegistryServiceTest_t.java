 package org.mule.galaxy.web.server;
 
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Index;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.impl.jcr.AbstractJcrObject;
 import org.mule.galaxy.impl.jcr.JcrVersion;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 import org.mule.galaxy.util.Constants;
 import org.mule.galaxy.web.client.RegistryService;
 
 public class RegistryServiceTest extends AbstractGalaxyTest {
     protected RegistryService gwtRegistry;
     
     
     @Override
     protected String[] getConfigLocations() {
         return new String[] { "/META-INF/applicationContext-core.xml", 
                               "/META-INF/applicationContext-web.xml" };
         
     }
     public void testWorkspaces() throws Exception {
         Collection workspaces = gwtRegistry.getWorkspaces();
         assertEquals(1, workspaces.size());
         
         Collection artifactTypes = gwtRegistry.getArtifactTypes();
        assertTrue(artifactTypes.size() > 0);
         
     }
 }
