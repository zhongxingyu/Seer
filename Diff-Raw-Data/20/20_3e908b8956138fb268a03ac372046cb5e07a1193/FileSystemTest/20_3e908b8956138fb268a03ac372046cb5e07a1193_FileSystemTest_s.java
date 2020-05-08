 package org.genericsystem.file;
 
 import java.util.Arrays;
 import org.genericsystem.core.Cache;
import org.genericsystem.core.CacheImpl;
 import org.genericsystem.core.GenericSystem;
import org.genericsystem.exception.InstanceClassConstraintViolationException;
 import org.genericsystem.exception.ExistsException;
 import org.genericsystem.file.AbstractTest.RollbackCatcher;
 import org.genericsystem.file.FileSystem.Directory;
 import org.genericsystem.file.FileSystem.FileType;
 import org.genericsystem.file.FileSystem.FileType.File;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Node;
 import org.testng.annotations.Test;
 
 @Test
 public class FileSystemTest {
 
 	public void testUpdateRootDirectory() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
		assert ((CacheImpl) cache).update(rootDirectory, "rootDirectory2").getValue().equals("rootDirectory2");
 		assert !rootDirectory.isAlive(cache);
 	}
 
 	public void testUpdateRootDirectoryWithFile() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
 		rootDirectory.addFile(cache, "file");
		assert ((CacheImpl) cache).update(rootDirectory, "rootDirectory2").getValue().equals("rootDirectory2");
 		assert !rootDirectory.isAlive(cache);
 	}
 
 	public void testUpdateDirectory() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
 		Directory directory = rootDirectory.addDirectory(cache, "directory");
		assert ((CacheImpl) cache).update(directory, "directory2").getValue().equals("directory2");
 		assert !directory.isAlive(cache);
 	}
 
 	public void testUpdateDirectoryWithFile() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
 		Directory directory = rootDirectory.addDirectory(cache, "directory");
 		directory.addFile(cache, "file");
		assert ((CacheImpl) cache).update(directory, "directory2").getValue().equals("directory2");
 		assert !directory.isAlive(cache);
 	}
 
 	public void testUpdateFile() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
 		File file = rootDirectory.addFile(cache, "file");
		assert ((CacheImpl) cache).update(file, "file2").getValue().equals("file2");
 		assert !file.isAlive(cache);
 	}
 
 	public void testDirectoryNameNotUniqueInDifferentDirectories() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
 		Directory directory1 = rootDirectory.addDirectory(cache, "directory1");
 		final Directory directory2 = rootDirectory.addDirectory(cache, "directory2");
 		assert !directory2.addDirectory(cache, "directory1").equals(directory1); // No Exception
 		new RollbackCatcher() {
 
 			@Override
 			public void intercept() {
 				directory2.addDirectory(cache.newSuperCache(), "directory1"); // Exception
 			}
 		}.assertIsCausedBy(ExistsException.class);
 		// assert false : fileSystem.getRootDirectories(cache).toList();
 		directory1.addFile(cache, "fileName", new byte[] { Byte.MAX_VALUE });
 		assert Arrays.equals(directory1.getFile(cache, "fileName").getContent(cache), new byte[] { Byte.MAX_VALUE });
 
 		// assert false : rootDirectory.getDirectories(cache);
 		directory1.getFile(cache, "fileName").remove(cache);
 	}
 
 	public void testFileNameNotUniqueInDifferentDirectories() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory(cache, "rootDirectory");
 		Directory directory1 = rootDirectory.addDirectory(cache, "directory1");
 		Directory directory2 = rootDirectory.addDirectory(cache, "directory2");
 		File file1 = directory1.addFile(cache, "test.hmtl", "<html/>".getBytes());
 		File file2 = directory2.addFile(cache, "test.hmtl", "<html/>".getBytes());// No Exception
 		assert file1 != file2;
 	}
 
 	public void testFileNameValueClassViolation() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		FileSystem directoryTree = cache.find(FileSystem.class);
 		final Node rootDirectory = directoryTree.addRootDirectory(cache, "rootDirectory");
 		final Attribute fileSystem = cache.find(FileType.class);
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				rootDirectory.setValue(cache, fileSystem, 2L);
 			}
 		}.assertIsCausedBy(InstanceClassConstraintViolationException.class);
 	}
 }
