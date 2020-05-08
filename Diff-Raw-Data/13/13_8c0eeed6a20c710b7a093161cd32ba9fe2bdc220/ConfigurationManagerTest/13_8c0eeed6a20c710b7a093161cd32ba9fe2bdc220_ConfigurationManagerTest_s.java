 /**
  * JBackup is a software managing backups.
  *
  * Copyright (C) 2013-2013 Fabien DUMINY (fabien [dot] duminy [at] webmails [dot] com)
  *
  * JBackup is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * JBackup is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
  * USA.
  */
 package fr.duminy.jbackup.core;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.io.FileUtils;
 import org.fest.assertions.Assertions;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import javax.naming.InvalidNameException;
 import java.io.File;
 import java.util.*;
 
 import static java.lang.Thread.sleep;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 /**
  * Tests for class {@link fr.duminy.jbackup.core.ConfigurationManager}.
  */
 public class ConfigurationManagerTest {
     private static final String TARGET_DIRECTORY = "aDirectory";
     private static final String CONFIG1 = "config1";
     private static final String CONFIG2 = "config2";
     private static final String CONFIG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
             "<backupConfiguration>\n" +
             "    <sources>\n" +
             "        <source>\n" +
             "            <dirFilter>aDirFilter</dirFilter>\n" +
             "            <fileFilter>aFileFilter</fileFilter>\n" +
             "            <sourceDirectory>aSource</sourceDirectory>\n" +
             "        </source>\n" +
             "        <source>\n" +
             "            <sourceDirectory>aSource2</sourceDirectory>\n" +
             "        </source>\n" +
             "        <source>\n" +
             "            <dirFilter>anotherDirFilter</dirFilter>\n" +
             "            <fileFilter>anotherFileFilter</fileFilter>\n" +
             "            <sourceDirectory>anotherSource</sourceDirectory>\n" +
             "        </source>\n" +
             "    </sources>\n" +
             "    <name>" + CONFIG1 + "</name>\n" +
             "    <targetDirectory>" + TARGET_DIRECTORY + "</targetDirectory>\n" +
             "</backupConfiguration>\n";
     private static final String CONFIG_XML2 = CONFIG_XML.replace("<name>config1</name>", "<name>" + CONFIG2 + "</name>");
 
     private File configDir;
     private ConfigurationManager manager;
 
     @Rule
     public final TemporaryFolder tempFolder = new TemporaryFolder();
 
     @Before
     public void setUp() throws Exception {
         configDir = tempFolder.newFolder();
         manager = new ConfigurationManager(configDir);
     }
 
     @Test
     public void testGetBackupConfigurations() throws Exception {
         File configFile1 = new File(configDir, "config1.xml");
         FileUtils.write(configFile1, CONFIG_XML);
 
         File configFile2 = new File(configDir, "config2.xml");
         FileUtils.write(configFile2, CONFIG_XML2);
 
         Collection<BackupConfiguration> configs = manager.getBackupConfigurations();
         assertNotNull(configs);
         assertEquals(2, configs.size());
         boolean found1 = false;
         boolean found2 = false;
         for (BackupConfiguration config : configs) {
             switch (config.getName()) {
                 case "config1":
                     found1 = true;
                     break;
                 case "config2":
                     found2 = true;
             }
         }
         assertTrue("config1 not found", found1);
         assertTrue("config2 not found", found2);
     }
 
     @Test
     public void testRemoveBackupConfiguration_sameName() throws Exception {
         testRemoveBackupConfiguration(false, false);
     }
 
     @Test
     public void testRemoveBackupConfiguration_sameInstance() throws Exception {
         testRemoveBackupConfiguration(true, false);
     }
 
     @Test
     public void testRemoveBackupConfiguration_unknownName() throws Exception {
         testRemoveBackupConfiguration(false, true);
     }
 
     private void testRemoveBackupConfiguration(boolean sameInstance, boolean unknownName) throws Exception {
         BackupConfiguration config = createConfiguration();
         manager.addBackupConfiguration(config);
 
         BackupConfiguration configToRemove = config;
         if (!sameInstance) {
             configToRemove = createConfiguration();
             if (unknownName) {
                 configToRemove.setName("unknownName");
             }
         }
         manager.removeBackupConfiguration(configToRemove);
 
         if (unknownName) {
             Assertions.assertThat(manager.getBackupConfigurations()).hasSize(1);
             Assertions.assertThat(manager.getBackupConfigurations()).containsOnly(config);
 
             Assertions.assertThat(configFileFor(config)).exists();
             Assertions.assertThat(configFileFor(configToRemove)).doesNotExist();
         } else {
             Assertions.assertThat(manager.getBackupConfigurations()).isEmpty();
             Assertions.assertThat(configFileFor(config)).doesNotExist();
 
             Assertions.assertThat(configFileFor(configToRemove)).doesNotExist();
         }
     }
 
     @Test
     public void testAddBackupConfiguration() throws Exception {
         BackupConfiguration expectedConfiguration = createConfiguration();
 
         manager.addBackupConfiguration(expectedConfiguration);
 
         Collection<BackupConfiguration> configs = manager.getBackupConfigurations();
         assertEquals(1, configs.size());
         BackupConfiguration actualConfiguration = configs.iterator().next();
         assertConfigEquals(expectedConfiguration, actualConfiguration);
         Assertions.assertThat(configFileFor(expectedConfiguration)).exists();
     }
 
     @Test(expected = DuplicateNameException.class)
     public void testAddBackupConfiguration_duplicateName() throws Exception {
         manager.addBackupConfiguration(createConfiguration());
         manager.addBackupConfiguration(createConfiguration());
     }
 
     @Test
     public void testAddBackupConfiguration_invalidNames() throws Exception {
         String[] names = {null, "", " ", "a.b", "a b"};
 
         Set<String> failingNames = new HashSet<>();
         for (String name : names) {
             try {
                 manager.addBackupConfiguration(createConfiguration(name));
                 failingNames.add(name);
             } catch (InvalidNameException ine) {
                 // ok
             }
         }
 
         Assertions.assertThat(failingNames).as("failingNames").isEmpty();
     }
 
     @Test
     public void testLoadAllConfigurations() throws Exception {
         // prepare mock
         File validXmlConfigFile1 = new File(configDir, "config1.xml");
         FileUtils.write(validXmlConfigFile1, CONFIG_XML);
 
         File validXmlConfigFile2 = new File(configDir, "config2.xml");
         FileUtils.write(validXmlConfigFile2, CONFIG_XML2);
 
         ConfigurationManager mock = spy(manager);
         doCallRealMethod().when(mock).loadAllConfigurations();
 
         // test
         mock.loadAllConfigurations();
 
         // verify
         verify(mock, times(1)).loadBackupConfiguration(eq(validXmlConfigFile1));
         verify(mock, times(1)).loadBackupConfiguration(eq(validXmlConfigFile2));
         verify(mock, times(1)).loadAllConfigurations();
         verifyNoMoreInteractions(mock);
         Assertions.assertThat(toConfigNames(mock.getBackupConfigurations())).as("fully valid config files").containsOnly(CONFIG1, CONFIG2);
     }
 
     @Test
     public void testLoadAllConfigurations_nonXmlFile() throws Exception {
         // prepare mock
         File notAnXmlFile = new File(configDir, "notAnXmlFile");
         FileUtils.write(notAnXmlFile, "");
 
         ConfigurationManager mock = spy(manager);
         doCallRealMethod().when(mock).loadAllConfigurations();
 
         // test
         mock.loadAllConfigurations();
 
         // verify
         verify(mock, times(1)).loadAllConfigurations();
         verify(mock, never()).loadBackupConfiguration(eq(notAnXmlFile));
         verifyNoMoreInteractions(mock);
         Assertions.assertThat(mock.getBackupConfigurations()).isEmpty();
     }
 
     @Test
     public void testLoadAllConfigurations_wrongXmlConfigFile() throws Exception {
         // prepare mock
         final File wrongXmlConfigFile = new File(configDir, "wrongXmlConfigFile.xml");
         FileUtils.write(wrongXmlConfigFile, "<root></root>");
 
         ConfigurationManager mock = spy(manager);
         doCallRealMethod().when(mock).loadAllConfigurations();
         final List<Exception> errors = new ArrayList<>();
         doAnswer(new Answer() {
             @Override
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 File file = (File) invocation.getArguments()[0];
                 if (file.equals(wrongXmlConfigFile)) {
                     try {
                         return invocation.callRealMethod();
                     } catch (Exception e) {
                         errors.add(e);
                         throw e;
                     }
                 } else {
                     return invocation.callRealMethod();
                 }
             }
         }).when(mock).loadBackupConfiguration(any(File.class));
 
         // test
         mock.loadAllConfigurations();
 
         // verify
         verify(mock, times(1)).loadBackupConfiguration(eq(wrongXmlConfigFile));
         verify(mock, times(1)).loadAllConfigurations();
         verifyNoMoreInteractions(mock);
         assertEquals("wrong number of invalid xml files", 1, errors.size());
         Assertions.assertThat(mock.getBackupConfigurations()).isEmpty();
     }
 
 
     @Test
     public void testLoadAllConfigurations_wrongConfigName() throws Exception {
         // prepare mock
         final File wrongConfigName = new File(configDir, "config4.xml");
         String otherTargetDirectory = TARGET_DIRECTORY + '2';
         FileUtils.write(wrongConfigName, CONFIG_XML2.replace(TARGET_DIRECTORY, otherTargetDirectory));
 
         ConfigurationManager mock = spy(manager);
         doCallRealMethod().when(mock).loadAllConfigurations();
 
         // test
         mock.loadAllConfigurations();
 
         // verify
         verify(mock, times(1)).loadBackupConfiguration(eq(wrongConfigName));
         verify(mock, times(1)).loadAllConfigurations();
         verifyNoMoreInteractions(mock);
         Assertions.assertThat(mock.getBackupConfigurations()).isEmpty();
     }
 
     @Test
     public void testLoadBackupConfiguration() throws Exception {
         BackupConfiguration expectedConfiguration = createConfiguration();
 
         File input = tempFolder.newFile();
         FileUtils.write(input, CONFIG_XML);
         BackupConfiguration actualConfiguration = manager.loadBackupConfiguration(input);
 
         assertConfigEquals(expectedConfiguration, actualConfiguration);
     }
 
     @Test
     public void testSaveRenamedBackupConfiguration() throws Exception {
         BackupConfiguration config = createConfiguration();
 
         String oldName = config.getName();
         File output1 = manager.saveBackupConfiguration(config);
         Assertions.assertThat(output1).exists();
 
         config.setName("NewName");
 
         File output2 = manager.saveRenamedBackupConfiguration(oldName, config);
         Assertions.assertThat(output1).doesNotExist();
         Assertions.assertThat(output2).exists();
     }
 
     @Test
     public void testSaveBackupConfiguration_newConfig() throws Exception {
         testSaveBackupConfiguration(createConfiguration());
     }
 
     @Test
     public void testSaveBackupConfiguration_updateConfig() throws Exception {
         BackupConfiguration config = createConfiguration();
 
         File output = testSaveBackupConfiguration(config);
         long t0 = output.lastModified();
 
         sleep(1000);
 
         output = testSaveBackupConfiguration(config);
         long t1 = output.lastModified();
 
         Assertions.assertThat(t1).isGreaterThan(t0);
     }
 
     private File testSaveBackupConfiguration(BackupConfiguration config) throws Exception {
         File output = manager.saveBackupConfiguration(config);
 
         Assertions.assertThat(output.getParentFile()).isEqualTo(configDir);
         String actualXML = FileUtils.readFileToString(output);
         assertEquals(CONFIG_XML, actualXML);
 
         return output;
     }
 
     private BackupConfiguration createConfiguration() {
         return createConfiguration(CONFIG1);
     }
 
     private BackupConfiguration createConfiguration(String configName) {
         BackupConfiguration config = new BackupConfiguration();
         config.setName(configName);
         config.setTargetDirectory("aDirectory");
         config.addSource("aSource", "aDirFilter", "aFileFilter");
         config.addSource("aSource2");
         config.addSource("anotherSource", "anotherDirFilter", "anotherFileFilter");
         return config;
     }
 
     private void assertConfigEquals(BackupConfiguration expected, BackupConfiguration actual) throws Exception {
         Map expectedProperties = describe(expected);
         Map actualProperties = describe(actual);
         assertEquals(expectedProperties, actualProperties);
     }
 
     private Map describe(BackupConfiguration config) throws Exception {
         Map properties = PropertyUtils.describe(config);
         properties.remove("class");
         List<BackupConfiguration.Source> sources = (List<BackupConfiguration.Source>) properties.remove("sources");
         properties.put("sources.size", sources.size());
         for (int i = 0; i < sources.size(); i++) {
             Map sourceProperties = PropertyUtils.describe(sources.get(i));
             sourceProperties.remove("class");
             properties.put("sources[" + i + "]", sourceProperties);
         }
         return properties;
     }
 
     private Collection<String> toConfigNames(Collection<BackupConfiguration> configs) {
         List<String> names = new ArrayList<>(configs.size());
         for (BackupConfiguration config : configs) {
             names.add(config.getName());
         }
         return names;
     }
 
     private File configFileFor(BackupConfiguration config) {
         return manager.configFileFor(config);
     }
 }
