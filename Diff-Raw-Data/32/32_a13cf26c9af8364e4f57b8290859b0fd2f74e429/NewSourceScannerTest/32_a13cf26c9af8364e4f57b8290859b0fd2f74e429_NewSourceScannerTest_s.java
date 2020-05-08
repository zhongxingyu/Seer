 /*
  * Stefano Maestri, javalinuxlabs.org Copyright 2008, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package it.javalinux.testedby.plugins.scanner;
 
 import static org.junit.Assert.assertTrue;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItem;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 import org.junit.Test;
 
 /**
  * 
  * @author alessio.soldano@javalinux.it
  * @since 21-Nov-2009
  *
  */
 public class NewSourceScannerTest {
     
     @Test
     public void shouldDetectNewClasses() throws IOException, InclusionScanException
     {
 	File srcDir = createTempDir();
 	File targetDir = createTempDir();
 	File source1 = new File(srcDir, "MyClass1.java");
 	assertTrue(source1.createNewFile());
 	File target1 = new File(targetDir, "MyClass1.class");
 	assertTrue(target1.createNewFile());
 	File source2 = new File(srcDir, "MyClass2.java");
 	assertTrue(source2.createNewFile());
 	File target2 = new File(targetDir, "MyClass2.class");
 	assertTrue(target2.createNewFile());
 	
 	RunsRepository repository = new RunsRepository();
 	repository.setLastRunTimeMillis(source1.getCanonicalPath(), new Long(1)); //target last ran years ago...
 	
 	NewSourceScanner scanner = new NewSourceScanner(repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	Set<File> newFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(newFiles.size(), is(1));
 	assertThat(newFiles, hasItem(source2));
 	
 	delete(new File[]{source1, target1, source2, target2, srcDir, targetDir});
     }
     
     @Test
     public void shouldReturnAllClassesOnFirstRun() throws IOException, InclusionScanException
     {
 	File srcDir = createTempDir();
 	File targetDir = createTempDir();
 	File source1 = new File(srcDir, "MyClass1.java");
 	assertTrue(source1.createNewFile());
 	File target1 = new File(targetDir, "MyClass1.class");
 	assertTrue(target1.createNewFile());
 	File source2 = new File(srcDir, "MyClass2.java");
 	assertTrue(source2.createNewFile());
 	File target2 = new File(targetDir, "MyClass2.class");
 	assertTrue(target2.createNewFile());
 	
 	RunsRepository repository = new RunsRepository();
 	
 	NewSourceScanner scanner = new NewSourceScanner(repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	Set<File> newFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(newFiles.size(), is(2));
 	assertThat(newFiles, hasItem(source1));
 	assertThat(newFiles, hasItem(source2));
 	
 	delete(new File[]{source1, target1, source2, target2, srcDir, targetDir});
     }
     
     @Test
     public void shouldCorrectlyHandleExclusions() throws IOException, InclusionScanException
     {
 	File srcDir = createTempDir();
 	File targetDir = createTempDir();
 	File source1 = new File(srcDir, "MyClass1.java");
 	assertTrue(source1.createNewFile());
 	File target1 = new File(targetDir, "MyClass1.class");
 	assertTrue(target1.createNewFile());
 	File source2 = new File(srcDir, "MyClass2.java");
 	assertTrue(source2.createNewFile());
 	File target2 = new File(targetDir, "MyClass2.class");
 	assertTrue(target2.createNewFile());
 	
 	RunsRepository repository = new RunsRepository();
 	
 	Set<String> excludes = new HashSet<String>();
 	Set<String> includes = new HashSet<String>();
 	includes.add("**/*");
 	NewSourceScanner scanner = new NewSourceScanner(includes, excludes, repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	Set<File> changedFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(changedFiles.size(), is(2));
 	assertThat(changedFiles, hasItem(source1));
 	assertThat(changedFiles, hasItem(source2));
 	
 	excludes.add(source1.getName());
 	scanner = new NewSourceScanner(includes, excludes, repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	changedFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(changedFiles.size(), is(1));
 	assertThat(changedFiles, hasItem(source2));
 	
 	excludes.add(source2.getName());
 	scanner = new NewSourceScanner(includes, excludes, repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	changedFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(changedFiles.size(), is(0));
 
 	delete(new File[]{source1, target1, source2, target2, srcDir, targetDir});
     }
     
     @Test
     public void shouldCorrectlyHandleInclusions() throws IOException, InclusionScanException
     {
 	File srcDir = createTempDir();
 	File targetDir = createTempDir();
 	File source1 = new File(srcDir, "MyClass1.java");
 	assertTrue(source1.createNewFile());
 	File target1 = new File(targetDir, "MyClass1.class");
 	assertTrue(target1.createNewFile());
 	File source2 = new File(srcDir, "MyClass2.java");
 	assertTrue(source2.createNewFile());
 	File target2 = new File(targetDir, "MyClass2.class");
 	assertTrue(target2.createNewFile());
 	
 	RunsRepository repository = new RunsRepository();
 	
 	Set<String> excludes = Collections.emptySet();
 	Set<String> includes = new HashSet<String>();
 	NewSourceScanner scanner = new NewSourceScanner(includes, excludes, repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	Set<File> changedFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(changedFiles.size(), is(0));
 	
 	includes.add(source1.getName());
 	scanner = new NewSourceScanner(includes, excludes, repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	changedFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(changedFiles.size(), is(1));
 	assertThat(changedFiles, hasItem(source1));
 	
 	includes.add(source2.getName());
 	scanner = new NewSourceScanner(includes, excludes, repository);
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	changedFiles = scanner.getIncludedSources(srcDir, targetDir);
 	assertThat(changedFiles.size(), is(2));
 	assertThat(changedFiles, hasItem(source1));
 	assertThat(changedFiles, hasItem(source2));
 
 	delete(new File[]{source1, target1, source2, target2, srcDir, targetDir});
     }
     
     private static File createTempDir() throws IOException
     {
 	File dir = File.createTempFile("TestedBy-maven-plugin-test-newsourcescanner-", String.valueOf(System.currentTimeMillis()));
 	dir.delete();
 	dir.mkdir();
 	return dir;
     }
     
     private static void delete(File... files) {
 	for (File file : files) {
 	    try {
 		file.delete();
 	    } catch (Exception e) {
 		//ignore
 	    }
 	}
     }
     
 }
