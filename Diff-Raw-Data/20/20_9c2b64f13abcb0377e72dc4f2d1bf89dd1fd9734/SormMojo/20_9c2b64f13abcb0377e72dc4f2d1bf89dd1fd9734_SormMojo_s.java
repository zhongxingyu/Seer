 package net.jonp.sorm.mojo;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 
 import net.jonp.sorm.codegen.CodeGenerator;
 import net.jonp.sorm.codegen.model.Sorm;
 import net.jonp.sorm.codegen.parser.BadInputException;
 import net.jonp.sorm.codegen.parser.XMLParser;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Generates Java code for Sorm data model objects out of configuration files.
  * 
  * @goal sorm
  * @phase generate-sources
  */
 public class SormMojo
     extends AbstractMojo
 {
     /**
      * Maven project object.
      * 
      * @readonly
      * @parameter default-value="${project}"
      */
     private MavenProject project;
 
     /**
      * Source directory in the project containing Sorm sources.
      * 
      * @parameter default-value="${project.basedir}/src/main/sorm"
      */
     private String sourceDirectory;
 
     /**
      * Source directory is for test sources (not normal sources). Affects what
      * is available during compilation.
      * 
      * @parameter default-value="${project.basedir}/src/test/sorm"
      */
     private String testDirectory;
 
     /** XML parser used for input file processing. */
     private final XMLParser xmlParser = new XMLParser();
 
     /** Code generator used for output file generation. */
     final CodeGenerator codegen = new CodeGenerator();
 
     public void execute()
         throws MojoExecutionException
     {
         final File sourceDir = new File(sourceDirectory);
         if (sourceDir.isDirectory()) {
             getLog().info("Sorm source directory: " + sourceDir.getPath());
             final File outdir = new File(project.getBasedir(), "target/generated-sources/sorm/");
             if (codegen(sourceDir, outdir) > 0) {
                 // Add the output directory as a compilable source root
                 // directory
                 project.addCompileSourceRoot(outdir.getPath());
             }
         }
         else {
             getLog().warn("No Sorm source directory found at: " + sourceDir.getAbsolutePath());
         }
 
         final File testDir = new File(testDirectory);
         if (testDir.isDirectory()) {
             getLog().info("Sorm test source directory: " + testDir.getPath());
             final File outdir = new File(project.getBasedir(), "target/generated-sources/sorm-test/");
             if (codegen(testDir, outdir) > 0) {
                 project.addTestCompileSourceRoot(outdir.getPath());
             }
         }
         else {
             getLog().info("No Sorm test source directory found at: " + testDir.getAbsolutePath());
         }
     }
 
     private int codegen(final File from, final File to)
         throws MojoExecutionException
     {
         getLog().debug("Output root directory: " + to.getAbsolutePath());
         to.mkdirs();
         if (!to.isDirectory()) {
             throw new MojoExecutionException("Failed to create output root directory: " + to.getAbsolutePath());
         }
 
         // Grab all of the XML files
         final Collection<File> inputFiles = locateInputFiles(from);
         getLog().debug("Located " + inputFiles.size() + " input XML files");
 
         if (inputFiles.isEmpty()) {
             getLog().info("No Sorm source files found in " + sourceDirectory);
         }
         else {
             // Process each file
             for (final File file : inputFiles) {
                 process(to, file);
             }
         }
 
         return inputFiles.size();
     }
 
     public void process(final File outdir, final File file)
         throws MojoExecutionException
     {
         getLog().info("Processing input file " + file.getPath());
 
 
         final Reader in;
         try {
             in = new FileReader(file);
         }
         catch (final FileNotFoundException fnfe) {
             throw new MojoExecutionException("Failed to open file " + file.getAbsolutePath() + ": " + fnfe.getMessage(), fnfe);
         }
 
         final Sorm sorm;
         try {
             try {
                 sorm = xmlParser.parseSorm(in);
             }
             catch (final BadInputException bie) {
                 throw new MojoExecutionException("Error processing input file " + file.getAbsolutePath() + ": " + bie.getMessage(),
                                                  bie);
             }
             catch (final IOException ioe) {
                 throw new MojoExecutionException("Error reading input file " + file.getAbsolutePath() + ": " + ioe.getMessage(),
                                                  ioe);
             }
         }
         finally {
             try {
                 in.close();
             }
             catch (final IOException ioe) {
                 getLog().debug("Failed to close input file " + file.getAbsolutePath() + ": " + ioe.getMessage(), ioe);
             }
         }
 
         final File pkgdir = new File(outdir, sorm.getPkg().replaceAll("\\.", "/"));
         pkgdir.mkdirs();
         if (!pkgdir.isDirectory()) {
             throw new MojoExecutionException("Failed to create output directory: " + pkgdir.getAbsolutePath());
         }
 
         final File outfile = new File(pkgdir, sorm.getName() + ".java");
         getLog().debug("Dumping output from " + file.getAbsolutePath() + " to " + outfile.getAbsolutePath());
 
         final PrintWriter out;
         try {
             out = new PrintWriter(new FileWriter(outfile));
         }
         catch (final IOException ioe) {
             throw new MojoExecutionException("Failed to open output file " + outfile.getAbsolutePath() + ": " + ioe.getMessage(),
                                              ioe);
         }
 
         try {
             codegen.setSorm(sorm);
             codegen.setOut(out);
             codegen.run();
 
             if (out.checkError()) {
                 throw new MojoExecutionException("Error writing to output file " + outfile.getAbsolutePath());
             }
         }
         finally {
             out.close();
         }
 
         if (out.checkError()) {
             throw new MojoExecutionException("Error closing output file " + outfile.getAbsolutePath());
         }
     }
 
     private Collection<File> locateInputFiles(final File root)
         throws MojoExecutionException
     {
         if (!root.isDirectory()) {
             return Collections.emptyList();
         }
 
         final Collection<File> matches = new LinkedList<File>();
         search(root, matches);
         return matches;
     }
 
     private void search(final File root, final Collection<File> matches)
     {
         final File[] check = root.listFiles(new FileFilter() {
             @Override
             public boolean accept(final File file)
             {
                 return (file.isDirectory() || file.getName().toLowerCase().endsWith(".xml"));
             }
         });
 
         for (final File file : check) {
             if (file.isDirectory()) {
                 search(file, matches);
             }
             else {
                 matches.add(file);
             }
         }
     }
 }
