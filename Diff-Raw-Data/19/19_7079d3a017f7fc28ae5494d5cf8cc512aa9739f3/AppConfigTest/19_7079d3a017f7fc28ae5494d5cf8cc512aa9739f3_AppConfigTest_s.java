 package org.sukrupa.app.config;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.Properties;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AppConfigForTestsContextLoader.class)
 public class AppConfigTest {
 
     @Value("${jdbc.url}")
     private String jdbcUrl;
 
     @Value("${db.root.dir}")
     private String hsqlDbRoot;
 
     @Autowired
     private Properties properties;
 
     @Test
     public void shouldWireUpCorrectly() {
         // nothing to do here
     }
 
     @Test
     public void shouldWireUpProperties() {
         assertThat(properties, hasKey("jdbc.url"));
     }
 
     @Test
     public void shouldResolvePropertyValues() {
         assertThat(jdbcUrl, containsString("jdbc:"));
     }
 
     @Test
     public void shouldAuthenticateByDefault() {
         pretendWeAreNotInIntelliJ();
         clearEnvironmentVariable();
         assertThat(property("web.server.authenticate"), is("true"));
     }
 
     @Test
     public void shouldNotAuthenticateWhenRunningBuild() {
         pretendWeAreNotInIntelliJ();
         System.setProperty("environment", "build");
         assertThat(property("web.server.authenticate"), is("false"));
     }
 
     @Test
     public void shouldUseWebRootInSouceTreeWhenRunningInIntellij() {
        System.setProperty("java.class.path", "IntelliJ something");
         clearEnvironmentVariable();
         assertThat(property("web.root.dir"), is("../../src/web"));
     }
 
     private String property(String key) {
         return new AppConfig().properties().getProperty(key);
     }
 
     private void pretendWeAreNotInIntelliJ() {
        System.setProperty("java.class.path", "");
     }
 
     private void clearEnvironmentVariable() {
         System.setProperty("environment", "");
     }
 }
