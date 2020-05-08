 package org.jboss.forge.jrebel;
 
 import static org.jboss.forge.jrebel.test.JRebelConfigUtil.configFile;
 import static org.jboss.forge.jrebel.test.JRebelConfigUtil.createEmptyConfigFile;
 import static org.jboss.forge.jrebel.test.JRebelConfigUtil.readConfigFile;
 import static org.jboss.forge.jrebel.test.JRebelConfigUtil.rebelXml;
 import static org.jboss.forge.jrebel.test.POMUtil.findPlugin;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.PluginExecution;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.jboss.arquillian.api.Deployment;
 import org.jboss.forge.project.Project;
 import org.jboss.forge.test.AbstractShellTest;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Test;
 
 public class JRebelPluginTest extends AbstractShellTest {
 
     @Deployment
     public static JavaArchive getDeployment() {
         return AbstractShellTest.getDeployment().addPackages(true, JRebelPlugin.class.getPackage());
     }
     
     @Test
     public void shouldInstallJRebel() throws Exception {
         // given
         Project p = initializeJavaProject();
         
         // when
         getShell().execute("jrebel setup");
         
         // then
         Plugin plugin = findPlugin(p, "org.zeroturnaround", "jrebel-maven-plugin");
         assertNotNull(plugin);
         PluginExecution execution = plugin.getExecutionsAsMap().get("generate-rebel-xml");
         assertEquals("process-resources", execution.getPhase());
         assertEquals(1, execution.getGoals().size());
         assertEquals("generate", execution.getGoals().get(0));
     }
 
     @Test
     public void shouldInstallJRebelWithoutMaven() throws Exception {
         // given
         Project p = initializeJavaProject();
         createEmptyConfigFile();
         
         // when
         getShell().execute("jrebel setup --withoutMavenPlugin --rebelHome ./target");
         
         // then
         assertTrue(configFile().exists());
         String content = readConfigFile();
         assertTrue(content.contains("test.root"));
         assertTrue(rebelXml(p.getProjectRoot().getUnderlyingResourceObject()).exists());
     }
     
     @Test
     public void shouldInstallContainer() throws Exception {
         // given
         Project p = initializeJavaProject();
         createEmptyConfigFile();
         
         // when
        getShell().execute("jrebel container --named JBOSS_AS7");
         
         // then
         Plugin plugin = findPlugin(p, "org.codehaus.cargo", "cargo-maven2-plugin");
         assertNotNull(plugin);
         Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
         assertNotNull(config);
         String text = config.toString();
         assertNotNull(text);
         assertTrue(text.contains("-javaagent:${env.REBEL_HOME}/jrebel.jar"));
         assertTrue(text.contains("<containerId>jboss7x</containerId>"));
     }
 
 }
