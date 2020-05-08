 package com.droste.file;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.*;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.zip.Adler32;
 
 import org.junit.*;
 
 import com.droste.file.report.Report;
 
 public class TestDirectorySyncer
 {
 	private final String tempSrcDir = "temp";
 	private final String tempTargetDir = "temp2";
 	private Path sourceFile;
 	private Path targetFile;
 
 	@Before
 	public void setup() throws IOException
 	{
 		sourceFile = createTempFile(tempSrcDir, "src/test/resources/source/einsteiger.php.html");
 		targetFile = createTempFile(tempTargetDir, "src/test/resources/target/einsteiger.php.html");
 	}
 
 	@After
 	public void cleanup() throws IOException
 	{
 		cleanup(tempSrcDir);
 		cleanup(tempTargetDir);
 	}
 
 	@Test
 	public void testAdler32Checksum() throws IOException
 	{
 		long time = System.currentTimeMillis();
 		Adler32 adler = new Adler32();
 		for (int i = 0; i < 1000; i++) {
 			adler.update(Files.readAllBytes(targetFile));
 			adler.getValue();
 			adler.reset();
 		}
 		assertTrue(System.currentTimeMillis() - time < 1000);
 	}
 
 	@Test
 	public void testBuildTargetFileMap() throws IOException
 	{
 		DirectorySyncer syncer = new DirectorySyncer("src/test/resources/source", "src/test/resources/target", false);
 		assertNotNull(syncer);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(9, targetMap.size());
 		// los gif exists twice in different locations:
 		assertEquals(8, syncer.getHashedTargetMap().size());
 	}
 
 	@Test
 	public void testFindSourcesSameFileSameSizeDontCopy() throws IOException
 	{
 		Files.delete(targetFile);
 		Files.copy(sourceFile, targetFile);
 
 		DirectorySyncer syncer = new DirectorySyncer("src/test/resources/source/same",
 				"src/test/resources/target/same", false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(1, targetMap.size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertTrue(Files.size(sourceFile) == Files.size(targetFile));
 		assertEquals(1, syncer.buildTargetFileMap().size());
 		assertEquals(0, report.getNoOfChangedFiles());
 		assertEquals(0, report.getNoOfNewFiles());
 		assertEquals(0, report.getNoOfNewDirectories());
 	}
 
 	@Test
 	public void testRenameDuplicateFile()
 	{
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		assertEquals("einsteiger.php (1).html", syncer.renameDuplicateFile(sourceFile, 1));
 		assertEquals("einsteiger.php (2).html", syncer.renameDuplicateFile(sourceFile, 2));
 	}
 
 	@Test
 	public void testRenameWithExistingDuplicates() throws IOException
 	{
 		Files.copy(targetFile, new File(tempTargetDir + "/" + "einsteiger.php (1).html").toPath());
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(2, targetMap.size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 
 		assertTrue(Files.exists(new File("temp2/einsteiger.php (2).html").toPath()));
 		assertEquals(3, syncer.buildTargetFileMap().size());
 
 		checkReport(report, 1, 0, 0);
 		final Entry<Path, Path> theEntry = report.getChangedFiles().entrySet().iterator().next();
 		assertTrue(theEntry.getKey().endsWith("einsteiger.php.html"));
 		assertTrue(theEntry.getValue().endsWith("einsteiger.php (2).html"));
 	}
 
 	/**
 	 * if there was a sync before, the source file would already have been copied, but renamed. Find the renamed copy
 	 * and don't copy again
 	 */
 	@Test
 	public void testNoRenameWithExistingDuplicates() throws IOException
 	{
 		Files.copy(sourceFile, new File(tempTargetDir + "/" + "einsteiger.php (1).html").toPath());
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(2, targetMap.size());
 		assertEquals(2, syncer.getHashedTargetMap().size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 
 		assertFalse(Files.exists(new File("temp2/einsteiger.php (2).html").toPath()));
 		assertEquals(2, syncer.buildTargetFileMap().size());
 		checkReport(report, 0, 0, 0);
 	}
 
 	@Test
 	public void testDifferentFileSizeCopy() throws IOException
 	{
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(1, targetMap.size());
 		assertEquals(1, syncer.getHashedTargetMap().size());
 
 		assertFalse(Files.size(sourceFile) == Files.size(targetFile));
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		Path copyOfSource = targetFile.getParent().resolve("einsteiger.php (1).html");
 		assertTrue(Files.exists(copyOfSource));
 		assertTrue(Files.size(sourceFile) == Files.size(copyOfSource));
 		checkReport(report, 1, 0, 0);
 		final Entry<Path, Path> theEntry = report.getChangedFiles().entrySet().iterator().next();
 		assertTrue(theEntry.getKey().endsWith("einsteiger.php.html"));
 		assertTrue(theEntry.getValue().endsWith("einsteiger.php (1).html"));
 	}
 
 	@Test
 	public void testNewFileCopy() throws IOException
 	{
 		Files.delete(targetFile);
 
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(0, targetMap.size());
 
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertTrue(Files.exists(targetFile));
 		assertTrue(Files.size(sourceFile) == Files.size(targetFile));
 		checkReport(report, 0, 1, 0);
 	}
 
 	/**
 	 * Search for file(hash) in the whole target. if exists then <br/>
 	 * 1. check if the new location is in the source, too. If so, copy, because it's an add-on.<br/>
 	 * 2. else Compare the siblings in the source with the new target location. If all siblings are at the new location
 	 * in the target, too, we assume a move and don't copy.
 	 */
 	@Test
 	public void testFileWasMovedSiblingsTooDontCopy() throws IOException
 	{
 		Files.delete(targetFile);
 		Files.delete(sourceFile);
 		createTempFile(tempTargetDir + "/newLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempTargetDir + "/newLocation", "src/test/resources/source/links.html");
 		createTempFile(tempTargetDir + "/newLocation", "src/test/resources/source/martin.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/links.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/martin.html");
 
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(3, targetMap.size());
 		assertEquals(3, syncer.getHashedTargetMap().size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertEquals(0, report.getNoOfNewFiles());
 		assertEquals(3, report.getNoOfRelocatedFiles());
 	}
 
 	@Test
 	public void testFileWasMovedSiblingsNotCopy() throws IOException
 	{
 		Files.delete(targetFile);
 		Files.delete(sourceFile);
 		createTempFile(tempTargetDir + "/newLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/links.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/martin.html");
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(1, targetMap.size());
 		assertEquals(1, syncer.getHashedTargetMap().size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertEquals(3, report.getNoOfNewFiles());
 		assertEquals(0, report.getNoOfRelocatedFiles());
 		assertEquals(1, report.getAdditionalFiles().size());
 		String additionalFileName = report.getAdditionalFiles().keySet().toArray(new Path[0])[0].getFileName()
 				.toString();
 		assertEquals("einsteiger.php.html", additionalFileName);
 	}
 
 	@Test
 	public void testFileWasMovedButSourceHasNewLocationCopy() throws IOException
 	{
 		Files.delete(targetFile);
 		Files.delete(sourceFile);
 		createTempFile(tempTargetDir + "/newLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempSrcDir + "/newLocation", "src/test/resources/source/einsteiger.php.html");
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(1, targetMap.size());
 		assertEquals(1, syncer.getHashedTargetMap().size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertEquals(1, report.getNoOfNewFiles());
 		assertEquals(0, report.getNoOfRelocatedFiles());
 		assertEquals(1, report.getAdditionalFiles().size());
 	}
 
 	/**
 	 * file is twice in the target , one source-loc matches one of the target-locs, <br/>
 	 * the other is relocated
 	 */
 	@Test
 	public void testFileMultipleTimesInTarget() throws IOException
 	{
 		Files.delete(targetFile);
 		Files.copy(sourceFile, targetFile);
 		createTempFile(tempTargetDir + "/newLocation", "src/test/resources/source/einsteiger.php.html");
 		createTempFile(tempSrcDir + "/oldLocation", "src/test/resources/source/einsteiger.php.html");
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(2, targetMap.size());
 		assertEquals(1, syncer.getHashedTargetMap().size());
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertEquals(0, report.getNoOfNewFiles());
 		assertEquals(1, report.getNoOfRelocatedFiles());
 		assertEquals(0, report.getAdditionalFiles().size());
 	}
 
 	/**
 	 * Multimedia files use the size as hash. i.e. there will be collisions.<br/>
 	 * The file still has to be copied if name is different.
 	 */
 	@Test
 	public void testMultiMediaFile() throws IOException
 	{
 		long emptyAvi = Files.size(Paths.get("src/test/resources/source/multimedia/test1.avi"));
 		long emptyAvi2 = Files.size(Paths.get("src/test/resources/target/multimedia/test2.avi"));
 		assertEquals(emptyAvi, emptyAvi2);
 
 		final Path rootSource = new File("src/test/resources/source/multimedia").toPath();
 		final Path rootTarget = new File("src/test/resources/target/multimedia").toPath();
 		copyDirectory(rootSource, tempSrcDir);
 		copyDirectory(rootTarget, tempTargetDir);
 
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertEquals(1, report.getNoOfNewFiles());
 	}
 
 	@Test
 	public void testAll() throws IOException
 	{
 		final Path rootSource = new File("src/test/resources/source").toPath();
 		final Path rootTarget = new File("src/test/resources/target").toPath();
 		copyDirectory(rootSource, tempSrcDir);
 		copyDirectory(rootTarget, tempTargetDir);
 
 		DirectorySyncer syncer = new DirectorySyncer(tempSrcDir, tempTargetDir, false);
 		Map<String, Path> targetMap = syncer.buildTargetFileMap();
 		assertEquals(9, targetMap.size());
 		assertEquals(8, syncer.getHashedTargetMap().size());
 
 		Report report = syncer.findAndHandleSourcesInTargetMap(targetMap);
 		assertEquals(20, syncer.buildTargetFileMap().size());
 		checkReport(report, 1, 10, 1);
 		assertEquals(29, report.getNoOfTargetFiles());
 		assertEquals(15, report.getNoOfSourceFiles());
 		assertEquals(5, report.getNoOfSourceDirectories());
 		assertTrue("report time was " + report.getSyncTime(), report.getSyncTime() > 0.0 && report.getSyncTime() < 1.0);
 	}
 
 	private void checkReport(Report report, int noChanged, int noNew, int noDir)
 	{
 		assertEquals(noChanged, report.getNoOfChangedFiles());
 		assertEquals(noNew, report.getNoOfNewFiles());
 		assertEquals(noDir, report.getNoOfNewDirectories());
 		Set<Entry<Path, Path>> changedFiles = report.getChangedFiles().entrySet();
 		assertEquals(noChanged, changedFiles.size());
 		Set<Entry<Path, Path>> newFiles = report.getNewFiles().entrySet();
 		assertEquals(noNew, newFiles.size());
 		List<Path> newDirs = report.getNewDirectories();
 		assertEquals(noDir, newDirs.size());
 	}
 
 	private void copyDirectory(final Path rootSource, final String copytodir) throws IOException
 	{
 		Files.walkFileTree(rootSource, new SimpleFileVisitor<Path>()
 		{
 			@Override
 			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
 			{
 				Files.copy(file, new File(copytodir + "/" + rootSource.relativize(file)).toPath(),
 						StandardCopyOption.REPLACE_EXISTING);
 				return super.visitFile(file, attrs);
 			}
 
 			@Override
 			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
 			{
 				Path newdir = new File(copytodir + "/" + rootSource.relativize(dir)).toPath();
 				if (!Files.exists(newdir))
 					Files.createDirectory(newdir);
 				return super.preVisitDirectory(dir, attrs);
 			}
 		});
 	}
 
 	private void cleanup(String dir) throws IOException
 	{
 		Files.walkFileTree(new File(dir).toPath(), new SimpleFileVisitor<Path>()
 		{
 			@Override
 			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
 			{
 				Files.delete(file);
 				return super.visitFile(file, attrs);
 			}
 
 			@Override
 			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
 			{
 				Files.delete(dir);
 				return super.postVisitDirectory(dir, exc);
 			}
 		});
 
 	}
 
 	private Path createTempFile(String tempDir, String filePath) throws IOException
 	{
 		Path path = new File(tempDir).toPath();
 		if (!Files.exists(path))
 			Files.createDirectory(path);
 		final Path source = new File(filePath).toPath();
 		Path target = new File(tempDir + "/" + source.getFileName()).toPath();
 		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
 		return target;
 	}
 }
