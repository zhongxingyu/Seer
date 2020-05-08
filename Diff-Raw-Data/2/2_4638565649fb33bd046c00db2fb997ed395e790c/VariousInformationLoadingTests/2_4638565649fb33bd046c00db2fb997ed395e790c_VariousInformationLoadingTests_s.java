 package com.yoursway.autoupdate.core.tests.versiondef;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.yoursway.autoupdate.core.tests.internal.AbstractVersionDefinitionLoaderTestCase;
 import com.yoursway.autoupdate.core.versions.Version;
 import com.yoursway.autoupdate.core.versions.definitions.InvalidVersionDefinitionException;
 import com.yoursway.autoupdate.core.versions.definitions.VersionDefinition;
 import com.yoursway.autoupdate.core.versions.definitions.VersionDefinitionNotAvailable;
 
 public class VariousInformationLoadingTests extends AbstractVersionDefinitionLoaderTestCase {
     
     private static final Version V11 = new Version("1.1.shit");
     private VersionDefinition def;
     
     @Before
     public void loadInfo() throws VersionDefinitionNotAvailable, InvalidVersionDefinitionException {
         def = loader.loadDefinition(V11);
     }
     
     @Test
     public void hasNewerVersion() throws Exception {
         assertEquals(true, def.hasNewerVersion());
     }
     
     @Test
     public void nextVersion() throws Exception {
         assertEquals("1.2.shit", def.nextVersion().toString());
     }
     
     @Test
     public void changesDescription() throws Exception {
         assertEquals("<b>Hello</b>", def.changesDescription());
     }
     
     @Test
     public void displayName() throws Exception {
         assertEquals("Megashit 1.2", def.displayName());
     }
     
     @Test
     public void url() throws Exception {
        assertEquals("files/a.jar", def.files().iterator().next().source().url().toString());
     }
     
     @Test
     public void updaterInfoMainJar() throws Exception {
         assertEquals("plugins/updater.jar", def.updaterInfo().mainJar().toPortableString());
     }
     
     @Test
     public void updaterInfoFiles() throws Exception {
         assertEquals("[plugins/collections.jar, plugins/updater.jar, plugins/utils.jar]", def.updaterInfo()
                 .files().toString());
     }
     
 }
