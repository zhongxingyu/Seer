 package org.jboss.shrinkwrap.resolver.test;
 
 import java.util.Collection;
 
 import org.jboss.shrinkwrap.api.GenericArchive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenImporter;
 import org.jboss.shrinkwrap.resolver.api.maven.filter.DependencyFilter;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * This class shows how to use settings.xml file to set proxy, secured access, different repositories, etc.
  *
 * FIXME: There should be a way how to disable the need to load settings.xml. ShrinkWrap Resolver should be able to
  * get the information which file is being processed from IDE or CLI. See https://jira.codehaus.org/browse/SUREFIRE-790
  *
  * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
  *
  */
 public class SettingsXmlUsageTest {
 
     @Test
     @Ignore("No settings.xml file example needed for now")
     public void loadSettingsXml() {
         Collection<GenericArchive> junitDeps = DependencyResolvers.use(MavenDependencyResolver.class)
         // set settings.xml file which defines JBoss Nexus repository
                 .configureFrom("setting.xml")
                 // get the artifact from there
                 .artifact("artifact-in:in-jboss-repository-only:1").resolveAs(GenericArchive.class);
     }
 
     @Test
     @Ignore("No settings.xml file example needed for now")
     public void loadSettingsXmlFromClasspath() {
         Collection<GenericArchive> junitDeps = DependencyResolvers.use(MavenDependencyResolver.class)
         // set settings.xml file which defines JBoss Nexus repository, we put it on the classpath
                 .configureFrom("classpath:setting.xml")
                 // get the artifact from there
                 .artifact("artifact-in:in-jboss-repository-only:1").resolveAs(GenericArchive.class);
     }
 
     @Test
     @Ignore("No settings.xml file example needed for now")
     public void loadSettingsXmlWithMavenImporter() {
         ShrinkWrap.create(MavenImporter.class)
         // set settings.xml file which defines JBoss Nexus repository, we put it on the classpath
                 .configureFrom("classpath:setting.xml").loadEffectivePom("pom.xml")
                 // get the artifact from there
                 .importAnyDependencies(new DependencyFilter("artifact-in:in-jboss-repository-only:1")).as(WebArchive.class);
     }
 
 }
