 package org.genericsystem.file;
 
 import java.util.Arrays;
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.exception.InstanceClassConstraintViolationException;
 import org.genericsystem.file.AbstractTest.RollbackCatcher;
 import org.genericsystem.file.FileSystem.Directory;
 import org.genericsystem.file.FileSystem.FileType;
 import org.genericsystem.file.FileSystem.FileType.File;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Node;
 import org.genericsystem.generic.Tree;
 import org.testng.annotations.Test;
 
 @Test
 public class FileSystemTest {
 
 	@Test
 	public void testFileSystemAndDirectoryTree() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(FileSystem.class);
 		Tree fileSystem = cache.find(FileSystem.class);
 		assert cache.isAlive(fileSystem);
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
 				directory2.addDirectory(cache, "directory1"); // Exception
 			}
 		}.assertIsCausedBy(IllegalStateException.class);
 		// assert false : fileSystem.getRootDirectories(cache).toList();
 		directory1.addFile(cache, "fileName", new byte[] { Byte.MAX_VALUE });
 		assert Arrays.equals(directory1.getFile(cache, "fileName").getContent(cache), new byte[] { Byte.MAX_VALUE });
 
 		// assert false : rootDirectory.getDirectories(cache);
 		directory1.getFile(cache, "fileName").remove(cache);
 	}
 
 	// public void testDirectoryNameValueClassViolation() {
 	// final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(DirectoryTree.class);
 	// DirectoryTree directoryTree = cache.find(DirectoryTree.class);
 	// assert directoryTree.getConstraintClass(cache) != null;
 	// final Directory rootDirectory = directoryTree.addRootDirectory(cache, "rootDirectory");
 	// new RollbackCatcher() {
 	// @Override
 	// public void intercept() {
 	// rootDirectory.addDirectory(cache, 2L);
 	// }
 	// }.assertIsCausedBy(InstanceClassConstraintViolationException.class);
 	// }
 
 	// //
 	// // // Constraint order problem: NotDuplicated vs Unique
 	// public void testFileNameUniqueInASameDirectory() {
 	// final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine(DirectoryTree.class);
 	// Tree directoryTree = cache.find(DirectoryTree.class);
 	// Node rootDirectory = directoryTree.newRoot(cache, "rootDirectory");
 	// final Node directory = rootDirectory.setNode(cache, "directory");
 	// final Attribute fileSystem = cache.find(FileType.class);
 	// directory.setValue(cache, fileSystem, "test.html");
 	// new RollbackCatcher() {
 	// @Override
 	// public void intercept() {
 	// directory.setValue(cache, fileSystem, "test.html");
 	// }
 	// }.assertIsCausedBy(UniqueConstraintViolationException.class);
 	// }
 
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
