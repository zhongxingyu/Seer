 package org.genericsystem.file;
 
 import java.util.Arrays;

 import org.genericsystem.core.Cache;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.Snapshot;
 import org.genericsystem.exception.ExistsException;
 import org.genericsystem.exception.InstanceClassConstraintViolationException;
 import org.genericsystem.file.FileSystem.Directory;
 import org.genericsystem.file.FileSystem.FileType;
 import org.genericsystem.file.FileSystem.FileType.File;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Node;
 import org.testng.annotations.Test;
 
 @Test
 public class FileSystemTest extends AbstractTest {
 
 	public void testUpdateRootDirectory() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
		assert rootDirectory.updateKey("rootDirectory2").getValue().equals("rootDirectory2");
 		assert !rootDirectory.isAlive();
 	}
 
 	public void testUpdateRootDirectoryWithFile() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
 		rootDirectory.addFile("file");
		assert rootDirectory.updateKey("rootDirectory2").getValue().equals("rootDirectory2");
 		assert !rootDirectory.isAlive();
 	}
 
 	public void testUpdateDirectory() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
 		Directory directory = rootDirectory.addDirectory("directory");
		assert directory.updateKey("directory2").getValue().equals("directory2");
 		assert !directory.isAlive();
 	}
 
 	public void testUpdateDirectoryWithFile() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
 		Directory directory = rootDirectory.addDirectory("directory");
 		directory.addFile("file");
		assert directory.updateKey("directory2").getValue().equals("directory2");
 		assert !directory.isAlive();
 	}
 
 	public void testUpdateFile() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
 		File file = rootDirectory.addFile("file");
		assert file.updateKey("file2").getValue().equals("file2");
 		assert !file.isAlive();
 	}
 
 	public void testDirectoryNameNotUniqueInDifferentDirectories() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
 		Directory directory1 = rootDirectory.addDirectory("directory1");
 		final Directory directory2 = rootDirectory.addDirectory("directory2");
 		assert !directory2.addDirectory("directory1").equals(directory1); // No Exception
 		new RollbackCatcher() {
 
 			@Override
 			public void intercept() {
 				cache.newSuperCache().start();
 				directory2.addDirectory("directory1"); // Exception
 			}
 		}.assertIsCausedBy(ExistsException.class);
 		// assert false : fileSystem.getRootDirectories().toList();
 		directory1.addFile("fileName", new byte[] { Byte.MAX_VALUE });
 		assert Arrays.equals(directory1.getFile("fileName").getContent(), new byte[] { Byte.MAX_VALUE });
 
 		// assert false : rootDirectory.getDirectories();
 		directory1.getFile("fileName").remove();
 	}
 
 	public void testFileNameNotUniqueInDifferentDirectories() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		Directory rootDirectory = fileSystem.addRootDirectory("rootDirectory");
 		Directory directory1 = rootDirectory.addDirectory("directory1");
 		Directory directory2 = rootDirectory.addDirectory("directory2");
 		File file1 = directory1.addFile("test.hmtl", "<html/>".getBytes());
 		File file2 = directory2.addFile("test.hmtl", "<html/>".getBytes());// No Exception
 		assert file1 != file2;
 	}
 
 	public void testFileNameValueClassViolation() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem directoryTree = cache.find(FileSystem.class);
 		final Node rootDirectory = directoryTree.addRootDirectory("rootDirectory");
 		final Attribute fileSystem = cache.find(FileType.class);
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				rootDirectory.setValue(fileSystem, 2L);
 			}
 		}.assertIsCausedBy(InstanceClassConstraintViolationException.class);
 	}
 
 	public void testGetRootDirectories() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class).start();
 		FileSystem fileSystem = cache.find(FileSystem.class);
 		fileSystem.addRootDirectory("root");
 		Snapshot<Directory> rootDirectories = fileSystem.getRootDirectories();
 		assert !rootDirectories.isEmpty() : rootDirectories;
 	}
 }
