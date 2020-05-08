 package org.eclipse.jst.server.generic.tests;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jst.server.generic.core.internal.CorePlugin;
 import org.eclipse.jst.server.generic.servertype.definition.ArchiveType;
 import org.eclipse.jst.server.generic.servertype.definition.Classpath;
 import org.eclipse.jst.server.generic.servertype.definition.JndiConnection;
 import org.eclipse.jst.server.generic.servertype.definition.JndiProperty;
 import org.eclipse.jst.server.generic.servertype.definition.LaunchConfiguration;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.Port;
 import org.eclipse.jst.server.generic.servertype.definition.Project;
 import org.eclipse.jst.server.generic.servertype.definition.Property;
 import org.eclipse.jst.server.generic.servertype.definition.Publisher;
 import org.eclipse.jst.server.generic.servertype.definition.PublisherData;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ServerTypeFactory;
 
 import junit.framework.TestCase;
 
 public class ServerTypeDefinitionModelTest extends TestCase {
 
   private static final String TESTED_SERVER_ID = "org.eclipse.jst.server.generic.runtime.jonas4";
    private ServerRuntime subject = null;
     protected void setUp() throws Exception {
        ServerRuntime[] serverdefs = CorePlugin.getDefault().getServerTypeDefinitionManager().getServerTypeDefinitions();
        for (int i = 0; i < serverdefs.length; i++) {
         if(TESTED_SERVER_ID.equals(serverdefs[i].getId()))
             subject=serverdefs[i];
        }
     }
 
 	
 	public void testServerTypeFactory(){
 		ServerTypeFactory factory = ServerTypeFactory.eINSTANCE;
 		assertNotNull(factory.createArchiveType());
 		assertNotNull(factory.createClasspath());
 		assertNotNull(factory.createJndiConnection());
 		assertNotNull(factory.createJndiProperty());
 		assertNotNull(factory.createLaunchConfiguration());
 		assertNotNull(factory.createModule());
 		assertNotNull(factory.createPort());
 		assertNotNull(factory.createProject());
 		assertNotNull(factory.createProperty());
 		assertNotNull(factory.createPublisher());
 		assertNotNull(factory.createPublisherData());
 		assertNotNull(factory.createServerRuntime());
 		assertNotNull(factory.getServerTypePackage());
 		
 	}
     public void testServerRuntime(){
         assertTrue(subject.getClasspath()!=null && subject.getClasspath().size()>0);
 		assertTrue(subject.getConfigurationElementNamespace()!=null && subject.getConfigurationElementNamespace().length()>0);
         assertTrue(subject.getName()!= null && subject.getName().length()>0);
         assertTrue(subject.getVersion()!=null && subject.getVersion().length()>0);
         assertTrue(subject.getProperty()!=null && subject.getProperty().size()>0);
         assertTrue(subject.getPort()!=null && subject.getPort().size()>0);
         assertTrue(subject.getModule()!=null && subject.getModule().size()>0);
         assertNotNull(subject.getProject());
         assertNotNull(subject.getStop());
         assertNotNull(subject.getStart());
         assertTrue(subject.getId()!= null && subject.getId().length()>0);
         assertTrue(subject.getFilename()!=null && subject.getFilename().length()>0);
         Classpath cp = (Classpath)subject.getClasspath().get(0);
         assertEquals(cp,subject.getClasspath(cp.getId()));
         Module module = (Module)subject.getModule().get(0);
         assertEquals(module,subject.getModule(module.getType()));
 		assertNotNull(subject.getPublisher("org.eclipse.jst.server.generic.antpublisher"));
     }
 	public void testSetServerRuntime()
 	{
 		subject.setId(TESTED_SERVER_ID);
 		subject.setConfigurationElementNamespace("test.namespace");
 		subject.setFilename("test.filename");
 		Map testMap =new HashMap();
 		subject.setPropertyValues(testMap);
 		assertEquals(TESTED_SERVER_ID,subject.getId());
 		assertEquals("test.namespace",subject.getConfigurationElementNamespace());
 		assertEquals("test.filename",subject.getFilename());
 	}
 
 	
     public void testClasspath()
     {
         Classpath cp =(Classpath)subject.getClasspath().get(0);
         assertTrue(cp.getId()!=null && cp.getId().length()>0);
         assertTrue(cp.getArchive()!=null && cp.getArchive().size()>0);
 		ArchiveType archiveType = (ArchiveType)cp.getArchive().get(0);
         assertTrue(archiveType.getPath()!=null && archiveType.getPath().length()>0);
 		
     }
     
     public void testLaunchConfiguration()
     {
        LaunchConfiguration lcfg = subject.getStart();
        assertNotNull(lcfg);
        assertTrue(lcfg.getMainClass()!=null && lcfg.getMainClass().length()>0);
        assertTrue(lcfg.getMainClass()!=null && lcfg.getExternal().isEmpty());
        assertTrue(lcfg.getMainClass()!=null && lcfg.getDebugPort()==null);
        assertTrue(lcfg.getClasspathReference()!=null && lcfg.getClasspathReference().length()>0);
        assertTrue(lcfg.getProgramArguments()!=null && lcfg.getProgramArguments().length()>0);
        assertTrue(lcfg.getVmParameters()!=null && lcfg.getVmParameters().length()>0);
        assertTrue(lcfg.getWorkingDirectory()!=null && lcfg.getWorkingDirectory().length()>0);
     }
     public void testModule()
     {
         Module module = (Module)subject.getModule().get(0);
         assertNotNull(module);
         assertTrue(module.getPublishDir()!= null && module.getPublishDir().length()>0);
         assertTrue(module.getPublisherReference()!=null && module.getPublisherReference().length()>0);
         assertTrue(module.getType()!=null && module.getType().length()>0);
     }
     public void testPort()
     {
         Port port = (Port)subject.getPort().get(0);
         assertNotNull(port);
         assertNotNull(port.getName());
         assertTrue(port.getNo()!= null && port.getNo().length()>0);
         assertTrue(port.getProtocol()!=null && port.getProtocol().length()>0);
     }
     public void testProject()
     {
         Project project = subject.getProject();
         assertNotNull(project);
         assertNotNull(project.getClasspathReference());
     }
     public void testProperty()
     {
         Property property = (Property)subject.getProperty().get(0);
         assertNotNull(property);
         assertTrue(property.getContext()!=null && property.getContext().length()>0);
         assertTrue(property.getDefault()!=null && property.getDefault().length()>0);
         assertTrue(property.getId()!=null && property.getId().length()>0);
         assertTrue(property.getLabel()!= null && property.getLabel().length()>0);
         assertTrue(property.getType()!= null && property.getType().length()>0);
         
     }
     public void testPublisher()
     {
         Publisher publisher = (Publisher)subject.getPublisher().get(0);
         assertNotNull(publisher);
         assertTrue(publisher.getId()!=null && publisher.getId().length()>0);
         assertTrue(publisher.getPublisherdata()!=null && publisher.getPublisherdata().size()>0);
         PublisherData data = (PublisherData)publisher.getPublisherdata().get(0);
         assertNotNull(data);
         assertTrue(data.getDataname()!= null && data.getDataname().length()>0);
         assertTrue(data.getDatavalue()!=null && data.getDatavalue().length()>0);
     }
     public void testJNDIConnection()
     {
         JndiConnection jndi = subject.getJndiConnection();
         assertNotNull(jndi);
         assertTrue(jndi.getProviderUrl()!=null && jndi.getProviderUrl().length()>0);
         assertTrue(jndi.getInitialContextFactory()!= null && jndi.getInitialContextFactory().length()>0);
         assertNotNull(jndi.getJndiProperty());
         if(jndi.getJndiProperty().size()>0)
         {
             JndiProperty property = (JndiProperty)jndi.getJndiProperty().get(0);
             assertNotNull(property.getName());
             assertNotNull(property.getValue());
         }
     }
     
     
     
 }
