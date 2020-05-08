 package org.eclipse.dltk.core.tests.cache;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.caching.ArchiveCacheIndexBuilder;
 import org.eclipse.dltk.core.caching.IContentCache;
 import org.eclipse.dltk.core.caching.MetadataContentCache;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.core.tests.model.ModelTestsPlugin;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 public class CacheTests extends AbstractModelTests {
 
 	private IProject PROJECT;
 	private IFile FILE;
 
 	public CacheTests(String name) {
 		super(ModelTestsPlugin.PLUGIN_NAME, name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		PROJECT = createProject("testProject");
 		PROJECT.open(new NullProgressMonitor());
 		FILE = PROJECT.getFile("testFile");
 		FILE.create(new ByteArrayInputStream(new byte[0]), true,
 				new NullProgressMonitor());
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		PROJECT.delete(true, new NullProgressMonitor());
 	}
 
 	public void testCacheItems001() {
 		IEnvironment env = EnvironmentManager.getLocalEnvironment();
 		IFileHandle handle = env.getFile(FILE.getLocation());
 		IContentCache cache = new MetadataContentCache(ModelTestsPlugin
 				.getDefault().getStateLocation().append("cache1"));
 		cache.setCacheEntryAttribute(handle, "attr1", "value1");
 		cache.setCacheEntryAttribute(handle, "attr2", "value2");
 		String value1 = cache.getCacheEntryAttributeString(handle, "attr1");
 		TestCase.assertEquals("value1", value1);
 		String value2 = cache.getCacheEntryAttributeString(handle, "attr2");
 		TestCase.assertEquals("value2", value2);
 	}
 
 	public void testCacheItems002() {
 		IEnvironment env = EnvironmentManager.getLocalEnvironment();
 		IFileHandle handle = env.getFile(FILE.getLocation());
 		IContentCache cache = ModelManager.getModelManager().getCoreCache();
 		cache.setCacheEntryAttribute(handle, "attr1", "value1");
 		cache.setCacheEntryAttribute(handle, "attr2", "value2");
 		String value1 = cache.getCacheEntryAttributeString(handle, "attr1");
 		TestCase.assertEquals("value1", value1);
 		String value2 = cache.getCacheEntryAttributeString(handle, "attr2");
 		TestCase.assertEquals("value2", value2);
 	}
 
 	public void testCacheItems003() throws Throwable {
 		IFile index_file = PROJECT.getFile(".dltk.index");
 		index_file.create(new ByteArrayInputStream(new byte[0]), true,
 				new NullProgressMonitor());
 		ArchiveCacheIndexBuilder builder = new ArchiveCacheIndexBuilder(
 				new FileOutputStream(new File(index_file.getLocation()
						.toOSString())), 0);
 
 		IFile file1 = PROJECT.getFile("file1.te");
 		file1.create(new ByteArrayInputStream(new byte[0]), true,
 				new NullProgressMonitor());
 		IFile file2 = PROJECT.getFile("file2.te");
 		file2.create(new ByteArrayInputStream(new byte[0]), true,
 				new NullProgressMonitor());
 
 		builder.addEntry("file1.te", file1.getLocalTimeStamp(), "ast",
 				new ByteArrayInputStream("testValue1".getBytes()));
 		builder.addEntry("file1.te", file1.getLocalTimeStamp(), "ast2",
 				new ByteArrayInputStream("testValue2".getBytes()));
 
 		builder.addEntry("file2.te", file2.getLocalTimeStamp(), "ast3",
 				new ByteArrayInputStream("testValue3".getBytes()));
 		builder.addEntry("file2.te", file2.getLocalTimeStamp(), "ast4",
 				new ByteArrayInputStream("testValue4".getBytes()));
 		builder.done();
 
 		IContentCache cache = ModelManager.getModelManager().getCoreCache();
 		IEnvironment env = EnvironmentManager.getLocalEnvironment();
 		IFileHandle handle1 = env.getFile(file1.getLocation());
 		IFileHandle handle2 = env.getFile(file2.getLocation());
 		String ast1 = cache.getCacheEntryAttributeString(handle1, "ast");
 		String ast2 = cache.getCacheEntryAttributeString(handle1, "ast2");
 		String ast3 = cache.getCacheEntryAttributeString(handle2, "ast3");
 		String ast4 = cache.getCacheEntryAttributeString(handle2, "ast4");
 		TestCase.assertEquals("testValue1", ast1);
 		TestCase.assertEquals("testValue2", ast2);
 		TestCase.assertEquals("testValue3", ast3);
 		TestCase.assertEquals("testValue4", ast4);
 	}
 }
