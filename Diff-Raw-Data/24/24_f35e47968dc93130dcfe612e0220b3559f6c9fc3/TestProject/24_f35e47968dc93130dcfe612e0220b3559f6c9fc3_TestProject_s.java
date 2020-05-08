 package net.bodz.redist.installer;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.bodz.bas.c.java.io.FileFinder;
 import net.bodz.bas.c.m2.MavenPomDir;
 import net.bodz.redist.installer.builtins.CTAProject;
 import net.bodz.redist.installer.builtins.FileCopy;
 import net.bodz.redist.installer.builtins.OptionalSection;
 import net.bodz.redist.installer.builtins.Section;
 
 /**
  * @label ABC Program
  */
 public class TestProject
         extends CTAProject {
 
     /**
      * Test source and classes
      */
     public Section testSection;
 
     public FileCopy copyTestClassesFiles;
     public FileCopy copyTestSrcFiles;
 
     public TestProject()
             throws IOException {
         // XXX - TestProject.class annotations?
         File parent = (File) get(BASE_PROGRAMS).getDefaultValue();
         Variable var = new BaseDirVariable(//
                 "Test Files", //
                 "A place to put test related files.", //
                 new File(parent, "tests"));
         define("BASE_TEST", var);
 
         testSection = new OptionalSection("test");
         {
             MavenPomDir testPomDir = MavenPomDir.fromClass(TestProject.class);
             File testBinDir = testPomDir.find("target/bin");
             File testSrcDir = testPomDir.find("src/main/java");
             FileFinder testbin = new FileFinder(FileCopy.NoSVN, testBinDir);
             FileFinder testsrc = new FileFinder(FileCopy.NoSVN, testSrcDir);
            copyTestClassesFiles = new FileCopy(BASE_TEST, testbin);
            copyTestSrcFiles = new FileCopy(BASE_TEST, testsrc);
             testSection.add(copyTestClassesFiles);
             testSection.add(copyTestSrcFiles);
         }
 
         add(classesSection);
         add(sourceSection);
         add(testSection);
     }
 
 }
