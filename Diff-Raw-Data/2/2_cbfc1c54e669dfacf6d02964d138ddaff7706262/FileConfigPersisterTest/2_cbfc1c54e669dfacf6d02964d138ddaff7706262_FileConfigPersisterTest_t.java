 /*
  * Copyright (c) 2011: Edmund Wagner, Wolfram Weidel, Lukas Gross
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the jeconfig nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jeconfig.filepersister;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.jeconfig.api.dto.ComplexConfigDTO;
 import org.jeconfig.api.exception.StaleConfigException;
 import org.jeconfig.api.exception.StoreConfigException;
 import org.jeconfig.api.persister.IScopePathGenerator;
 import org.jeconfig.api.scope.ClassScopeDescriptor;
 import org.jeconfig.api.scope.CodeDefaultScopeDescriptor;
 import org.jeconfig.api.scope.DefaultScopeDescriptor;
 import org.jeconfig.api.scope.GlobalScopeDescriptor;
 import org.jeconfig.api.scope.IScopePath;
 import org.jeconfig.api.scope.IScopePathBuilderFactory;
 import org.jeconfig.api.scope.UserScopeDescriptor;
 import org.jeconfig.server.marshalling.XStreamXmlMarshaller;
 import org.jeconfig.server.persister.DefaultScopePathGenerator;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 public class FileConfigPersisterTest extends AbstractConfigPersisterTest {
 
 	// CHECKSTYLE:OFF
 	@Rule
 	public TemporaryFolder folder = new TemporaryFolder();
 	// CHECKSTYLE:ON
 
 	private String rootDirectory;
 	private IScopePathGenerator gen;
 	private final XStreamXmlMarshaller serializer = new XStreamXmlMarshaller();
 	private FileConfigPersister persister;
 	private String fileExtension;
 
 	@Before
 	public void setUp() throws Exception {
 		rootDirectory = folder.getRoot().getAbsolutePath();
 		fileExtension = ".xml"; //$NON-NLS-1$
 		persister = new FileConfigPersister(serializer, rootDirectory, fileExtension);
 		gen = new DefaultScopePathGenerator(File.separator);
 		getConfigPersistenceService().addConfigPersister(persister);
 	}
 
 	@Test
 	public void testSaveConfiguration() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final ComplexConfigDTO configuration = createTestConfigDTO(factory.annotatedPath().create());
 		persister.saveConfiguration(configuration);
 		final File file = new File(rootDirectory + File.separator + "default" //$NON-NLS-1$
 			+ File.separator
 			+ "global" //$NON-NLS-1$
 			+ File.separator
 			+ "user" //$NON-NLS-1$
 			+ File.separator
 			+ "userName-hugo" //$NON-NLS-1$
 			+ File.separator
 			+ gen.createName(factory.annotatedPath().create())
 			+ fileExtension);
 
 		Assert.assertTrue(file.exists());
 	}
 
 	@Test(expected = StaleConfigException.class)
	public void testSaveConfigurationAlreadyExists() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final ComplexConfigDTO configuration = createTestConfigDTO(factory.annotatedPath().create());
 		persister.saveConfiguration(configuration);
 		persister.saveConfiguration(configuration);
 	}
 
 	@Test(expected = StoreConfigException.class)
 	public void testSaveConfigurationWithIllegalVersion() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final ComplexConfigDTO configuration = new ComplexConfigDTO();
 		configuration.setPolymorph(false);
 		configuration.setDefiningScopePath(factory.annotatedPath().create());
 		configuration.setVersion(-1);
 		configuration.setNulled(false);
 		persister.saveConfiguration(configuration);
 	}
 
 	@Test
 	public void testLoadConfiguration() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final IScopePath path = factory.annotatedPath().create();
 		final ComplexConfigDTO configuration = createTestConfigDTO(path);
 		persister.saveConfiguration(configuration);
 		final ComplexConfigDTO result = persister.loadConfiguration(path);
 
 		Assert.assertEquals(configuration, result);
 	}
 
 	@Test
 	public void testUpdateConfiguration() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final IScopePath path = factory.annotatedPath().create();
 		final ComplexConfigDTO configuration = createTestConfigDTO(path);
 
 		persister.saveConfiguration(configuration);
 		final ComplexConfigDTO newConfiguration = configuration.flatCopy(
 				2,
 				configuration.getDefiningScopePath().getLastScope().getName(),
 				configuration.getVersion());
 		persister.updateConfiguration(newConfiguration);
 
 		final ComplexConfigDTO result = persister.loadConfiguration(path);
 		Assert.assertTrue(result.getVersion() == 2);
 	}
 
 	@Test(expected = StaleConfigException.class)
 	public void testUpdateConfigurationDoesntExists() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final ComplexConfigDTO configuration = createTestConfigDTO(factory.annotatedPath().create());
 		persister.updateConfiguration(configuration);
 	}
 
 	@Test(expected = StaleConfigException.class)
 	public void testUpdateConfigurationWithOlderVersion() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final ComplexConfigDTO configuration = new ComplexConfigDTO();
 		configuration.setPolymorph(false);
 		configuration.setDefiningScopePath(factory.annotatedPath().create());
 		configuration.setVersion(2);
 		configuration.setNulled(false);
 		persister.saveConfiguration(configuration);
 		final ComplexConfigDTO newConfiguration = configuration.flatCopy(
 				1,
 				configuration.getDefiningScopePath().getLastScope().getName(),
 				configuration.getVersion());
 		persister.updateConfiguration(newConfiguration);
 	}
 
 	@Test
 	public void testDeleteConfigurationDeleteChildrenFalse() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final IScopePath path = factory.annotatedPath().create();
 		final ComplexConfigDTO configuration = createTestConfigDTO(path);
 		persister.saveConfiguration(configuration);
 		persister.delete(path, false);
 
 		final File file = new File(rootDirectory + File.separator + "default" + File.separator //$NON-NLS-1$
 			+ "global" + File.separator //$NON-NLS-1$
 			+ "user" + File.separator //$NON-NLS-1$
 			+ "userName-hugo" //$NON-NLS-1$
 			+ File.separator
 			+ gen.createName(path));
 		Assert.assertTrue(!file.exists());
 	}
 
 	@Test
 	public void testDeleteConfigurationDeleteChildrenTrue() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final IScopePath parentPath = factory.annotatedPath().create();
 		final IScopePath childPath = factory.annotatedPathUntil(GlobalScopeDescriptor.NAME).create();
 		final IScopePath child2Path = factory.annotatedPathUntil(DefaultScopeDescriptor.NAME).create();
 		final ComplexConfigDTO parentConfig = createTestConfigDTO(parentPath);
 		final ComplexConfigDTO childConfig = createTestConfigDTO(childPath);
 		final ComplexConfigDTO child2Config = createTestConfigDTO(child2Path);
 
 		persister.saveConfiguration(parentConfig);
 		persister.saveConfiguration(childConfig);
 		persister.saveConfiguration(child2Config);
 
 		final File parentFile = new File(rootDirectory
 			+ "default" + File.separator + gen.createName(factory.annotatedPath().create())); //$NON-NLS-1$
 		final File childFile = new File(rootDirectory + File.separator + "default" + File.separator + "global" //$NON-NLS-1$ //$NON-NLS-2$
 			+ File.separator
 			+ gen.createName(childPath));
 		final File childFile2 = new File(rootDirectory
 			+ File.separator
 			+ "default" + File.separator + "global" + File.separator + "user" + File.separator + "userName-hugo" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 			+ File.separator
 			+ gen.createName(child2Path));
 
 		persister.delete(child2Path, true);
 		Assert.assertTrue(!parentFile.exists() && !childFile.exists() && !childFile2.exists());
 	}
 
 	@Test
 	public void testDeleteAllOccurences() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		final IScopePath parentPath = factory.annotatedPath().create();
 		final IScopePath childPath = factory.annotatedPathUntil(GlobalScopeDescriptor.NAME).create();
 		final IScopePath child2Path = factory.annotatedPathUntil(DefaultScopeDescriptor.NAME).create();
 		final ComplexConfigDTO parentConfig = createTestConfigDTO(parentPath);
 		final ComplexConfigDTO childConfig = createTestConfigDTO(childPath);
 		final ComplexConfigDTO child2Config = createTestConfigDTO(child2Path);
 
 		persister.saveConfiguration(parentConfig);
 		persister.saveConfiguration(childConfig);
 		persister.saveConfiguration(child2Config);
 
 		final File file = new File(rootDirectory
 			+ File.separator
 			+ "default" + File.separator + "global" + File.separator + "user" + File.separator + "userName-hugo" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 			+ File.separator
 			+ gen.createName(factory.annotatedPath().create()));
 
 		persister.deleteAllOccurences(UserScopeDescriptor.NAME, getUserScopeProperties());
 		Assert.assertTrue(!file.exists());
 	}
 
 	@Test
 	public void testListScopesLastScopeWithProperty() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 		persister.saveConfiguration(createTestConfigDTO(factory.annotatedPath().create()));
 		persister.saveConfiguration(createTestConfigDTO(factory.annotatedPathUntil(DefaultScopeDescriptor.NAME).create()));
 		persister.saveConfiguration(createTestConfigDTO(factory.annotatedPathUntil(GlobalScopeDescriptor.NAME).create()));
 
 		final Collection<IScopePath> scopePaths = persister.listScopes(UserScopeDescriptor.NAME, getUserScopeProperties());
 
 		for (final IScopePath scopePath : scopePaths) {
 			final Map<String, String> properties = new HashMap<String, String>();
 			properties.put("className", TestConfiguration.class.getName()); //$NON-NLS-1$
 			Assert.assertTrue((scopePath.findScopeByName(ClassScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(CodeDefaultScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(DefaultScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(GlobalScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(UserScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(UserScopeDescriptor.NAME).containsAllProperties(getUserScopeProperties()))
 				&& (scopePath.findScopeByName(ClassScopeDescriptor.NAME).containsAllProperties(properties)));
 		}
 	}
 
 	@Test
 	public void testListScopesLastScopeWithoutProperty() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(TestConfiguration.class);
 
 		final ComplexConfigDTO configuration = createTestConfigDTO(factory.annotatedPathUntil(DefaultScopeDescriptor.NAME).create());
 		final ComplexConfigDTO configuration2 = createTestConfigDTO(factory.annotatedPathUntil(GlobalScopeDescriptor.NAME).create());
 		persister.saveConfiguration(configuration);
 		persister.saveConfiguration(configuration2);
 
 		final Collection<IScopePath> scopePaths = persister.listScopes(DefaultScopeDescriptor.NAME, new HashMap<String, String>());
 
 		for (final IScopePath scopePath : scopePaths) {
 			final Map<String, String> properties = new HashMap<String, String>();
 			properties.put("className", TestConfiguration.class.getName()); //$NON-NLS-1$
 			Assert.assertTrue((scopePath.findScopeByName(ClassScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(CodeDefaultScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(DefaultScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(ClassScopeDescriptor.NAME).containsAllProperties(properties)));
 		}
 	}
 
 	@Test
 	public void testListScopesScopeWithManyProperties() {
 		final IScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(
 				TestConfigurationScopeWithManyProperties.class);
 		final ComplexConfigDTO configuration = createTestConfigDTO(factory.annotatedPath().create());
 		persister.saveConfiguration(configuration);
 
 		final Collection<IScopePath> scopePaths = persister.listScopes("test", getTestScopeProperties()); //$NON-NLS-1$
 		for (final IScopePath scopePath : scopePaths) {
 			final Map<String, String> properties = new HashMap<String, String>();
 			properties.put("className", TestConfigurationScopeWithManyProperties.class.getName()); //$NON-NLS-1$
 			Assert.assertTrue((scopePath.findScopeByName(ClassScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(CodeDefaultScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(DefaultScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(TestScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(UserScopeDescriptor.NAME) != null)
 				&& (scopePath.findScopeByName(ClassScopeDescriptor.NAME).containsAllProperties(properties))
 				&& (scopePath.findScopeByName(UserScopeDescriptor.NAME).containsAllProperties(getUserScopeProperties()))
 				&& (scopePath.findScopeByName(TestScopeDescriptor.NAME).containsAllProperties(getTestScopeProperties())));
 		}
 	}
 
 	private ComplexConfigDTO createTestConfigDTO(final IScopePath path) {
 		final ComplexConfigDTO configuration = new ComplexConfigDTO();
 		configuration.setPolymorph(false);
 		configuration.setDefiningScopePath(path);
 		configuration.setVersion(1);
 		configuration.setNulled(false);
 		return configuration;
 	}
 }
