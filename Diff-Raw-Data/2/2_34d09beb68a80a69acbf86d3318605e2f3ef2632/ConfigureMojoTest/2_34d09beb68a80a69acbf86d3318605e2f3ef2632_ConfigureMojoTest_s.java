 package org.htfv.maven.plugins.buildconfigurator;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.apache.maven.project.MavenProject;
 import org.htfv.maven.plugins.buildconfigurator.stubs.MavenProjectStub;
 import org.htfv.maven.plugins.buildconfigurator.utils.MavenUtils;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(JUnit4.class)
 public class ConfigureMojoTest extends AbstractMojoTestCase
 {
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     private ConfigureMojo createMojo(final String pomFileName) throws Exception
     {
         File basedir = new File(getBasedir(), "src/test/projects");
 
         //
         // Load configured mojo.
         //
 
         ConfigureMojo mojo = (ConfigureMojo) lookupMojo(
                 "configure", new File(basedir, pomFileName));
 
         //
         // Initialize default mojo parameter values.
         //
 
         if (mojo.getProject() == null)
         {
             MavenProjectStub project = new MavenProjectStub(basedir);
             mojo.setProject(project);
         }
 
         return mojo;
     }
 
     @Before
     @Override
     public void setUp() throws Exception
     {
         super.setUp();
     }
 
     @Test
     public void shouldCorrectlyApplyDefaultProperties() throws Exception
     {
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-default-properties.xml");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
 
         //
         // Check to see that all properties were loaded correctly.
         //
 
         Properties projectProperties = mojo.getProject().getProperties();
 
         assertThat(
                 projectProperties.getProperty("japanese"),
                 is("日本語"));
     }
 
     @Test
     public void shouldCorrectlyConfigureDependencies() throws Exception
     {
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-dependencies.xml");
 
         //
         // Add some dependencies to check later if they were correctly updated.
         //
 
         Dependency commonsCodec = new Dependency();
 
         commonsCodec.setGroupId   ("commons-codec");
         commonsCodec.setArtifactId("commons-codec");
         commonsCodec.setVersion   ("1.6");
         commonsCodec.setType      ("jar");
         commonsCodec.setScope     ("runtime");
 
         Dependency commonsIo = new Dependency();
 
         commonsIo.setGroupId   ("commons-io");
         commonsIo.setArtifactId("commons-io");
         commonsIo.setVersion   ("2.1");
         commonsIo.setType      ("jar");
         commonsIo.setScope     ("compile");
 
         Dependency commonsLang = new Dependency();
 
         commonsLang.setGroupId   ("commons-lang");
         commonsLang.setArtifactId("commons-lang");
         commonsLang.setVersion   ("2.5");
         commonsLang.setType      ("jar");
         commonsLang.setScope     ("compile");
 
         MavenProject project = mojo.getProject();
 
         project.getDependencies().add(commonsCodec);
         project.getDependencies().add(commonsIo);
         project.getDependencies().add(commonsLang);
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
 
         //
         // Check whether dependencies were correctly added and updated.
         //
 
         commonsCodec = MavenUtils.findProjectDependency(project,
         		"commons-codec", "commons-codec", null, "jar");
 
         assertThat(
         		commonsCodec,
         		is(notNullValue()));
         assertThat(
         		commonsCodec.getScope(),
         		is("compile"));
 
         commonsIo = MavenUtils.findProjectDependency(project,
                 "commons-io", "commons-io", null, "jar");
 
         assertThat(
                 commonsIo,
                 is(notNullValue()));
         assertThat(
                 commonsIo.getVersion(),
                 is("2.1"));
 
         commonsLang = MavenUtils.findProjectDependency(project,
         		"commons-lang", "commons-lang", null, "jar");
 
         assertThat(
         		commonsLang,
         		is(notNullValue()));
         assertThat(
         		commonsLang.getVersion(),
         		is("2.6"));
 
         Dependency commonsLogging = MavenUtils.findProjectDependency(project,
         		"commons-logging", "commons-logging", null, "jar");
 
         assertThat(
         		commonsLogging,
         		is(notNullValue()));
     }
 
     @Test
     public void shouldCorrectlyLoadEmptyConfiguration() throws Exception
     {
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-empty-configuration.xml");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
     }
 
     /**
      * This test covers most common configuration options. It loads mojo
      * configuration from {@code src/test/projects/configure-basic.xml} and
      * checks whether the project was configured correctly after execution.
      *
      * @throws Exception
      *             this test should not throw any exception.
      */
     @Test
     public void shouldCorrectlyLoadPropertyFiles() throws Exception
     {
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-property-files.xml");
 
         //
         // Configure test-specific project properties.
         //
 
         Properties projectProperties = mojo.getProject().getProperties();
 
         projectProperties.setProperty("existingValue", "loaded from project");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
 
         //
         // Check to see that all properties were loaded correctly.
         //
 
         //
         // Values shall be first loaded from customized.properties, then from default.properties.
         //
 
         assertThat(
                 projectProperties.getProperty("defaultValue"),
                 is("loaded from default.properties"));
         assertThat(
                 projectProperties.getProperty("customizedValue"),
                 is("loaded from customized.properties"));
 
         //
         // Properties shall be immutable - existing properties shall not be overwritten.
         //
 
         assertThat(
                 projectProperties.getProperty("existingValue"),
                 is("loaded from project"));
 
         //
         // Project properties shall be available in EL.
         //
 
         assertThat(
                 projectProperties.getProperty("projectValue"),
                 is("loaded from project"));
 
         //
         // Complex recursive EL expression - properties which are not yet loaded shall be available
         // in EL (we guarantee property file load order, but not property load order).
         //
 
         assertThat(
                 projectProperties.getProperty("factorial5"),
                 is("120"));
 
         //
         // str:concat function shall be available in EL. Null objects shall be ignored.
         //
 
         assertThat(
                 projectProperties.getProperty("concatNullEenie"),
                 is("Eenie"));
         assertThat(
                 projectProperties.getProperty("concatMeenieNullMiney"),
                 is("MeenieMiney"));
         assertThat(
                 projectProperties.getProperty("concatMoCatchNullA"),
                 is("MoCatchA"));
         assertThat(
                 projectProperties.getProperty("concatTigerByTheNullToe"),
                 is("TigerByTheToe"));
 
         //
        // str:join function shall be available in EL. Null objects and empty string shall be
         // ignored.
         //
 
         assertThat(
                 projectProperties.getProperty("joinInkyNull"),
                 is("Inky"));
         assertThat(
                 projectProperties.getProperty("joinBinkyEmptyBonky"),
                 is("Binky Bonky"));
         assertThat(
                 projectProperties.getProperty("joinDaddyHadADonkey"),
                 is("Daddy Had A Donkey"));
         assertThat(
                 projectProperties.getProperty("joinDonkeyDiedNullDaddyCried"),
                 is("Donkey Died Daddy Cried"));
 
         //
         // It should be possible to load property files with non-standard encoding.
         //
 
         assertThat(
                 projectProperties.getProperty("chinese"),
                 is("中文"));
         assertThat(
                 projectProperties.getProperty("japanese"),
                 is("日本語"));
     }
 
     @Test
     public void shouldFailIfDependencyArtifactIdMissing() throws Exception
     {
         thrown.expect(RequiredParameterMissingException.class);
         thrown.expectMessage(containsString("dependencies/dependency/artifactId"));
 
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-dependency-artifact-not-specified.xml");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
     }
 
     @Test
     public void shouldFailIfDependencyGroupIdMissing() throws Exception
     {
         thrown.expect(RequiredParameterMissingException.class);
         thrown.expectMessage(containsString("dependencies/dependency/groupId"));
 
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-dependency-group-not-specified.xml");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
     }
 
     @Test
     public void shouldFailIfPropertyFileMissing() throws Exception
     {
         thrown.expect(PropertyFileMissingException.class);
         thrown.expectMessage(containsString("missing-fail.properties"));
 
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-property-file-missing.xml");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
     }
 
     @Test
     public void shouldFailIfPropertyFileNotSpecified() throws Exception
     {
         thrown.expect(RequiredParameterMissingException.class);
 
         //
         // Load mojo configuration.
         //
 
         ConfigureMojo mojo = createMojo("configure-property-file-not-specified.xml");
 
         //
         // Execute mojo.
         //
 
         mojo.execute();
     }
 }
