 package org.jclouds.cleanup;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.jclouds.cleanup.output.BeanBuilderPrinter;
 import org.jclouds.cleanup.output.BeanPrinter;
 import org.jclouds.cleanup.output.ClassDocPrinter;
 import org.jclouds.cleanup.output.IndentedPrintWriter;
 import org.jclouds.cleanup.output.InstanceFieldPrinter;
 import org.jclouds.cleanup.output.StaticFieldPrinter;
 import org.jclouds.cleanup.output.bean.AccessorPrinter;
 import org.jclouds.cleanup.output.bean.BuilderArgConstructorPrinter;
 import org.jclouds.cleanup.output.bean.EqualsPrinter;
 import org.jclouds.cleanup.output.bean.HashCodePrinter;
 import org.jclouds.cleanup.output.bean.NoArgsConstructorPrinter;
 import org.jclouds.cleanup.output.bean.ToStringPrinter;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Objects;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.io.PatternFilenameFilter;
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.DocErrorReporter;
 import com.sun.javadoc.Doclet;
 import com.sun.javadoc.LanguageVersion;
 import com.sun.javadoc.RootDoc;
 
 /**
  * Interrogates the class structure using the doclet api and extracts the comments immediately preceding classes, fields
  * and methods as required.
  *
  * @see <a href=
  *      "http://docs.oracle.com/javase/6/docs/technotes/guides/javadoc/doclet/overview.html"
  *      />
  */
 public class DomainObjectDocletCleaner extends Doclet {
    private static String outputPath = "target/generated-sources/cleanbeans";
    private static boolean jaxbOutput = false;
    private static boolean minimalOutput = false;
 
    /**
     * Bootstrapping javadoc application to save users having to remember all the arguments.
     *
     * @param args the path to locate the source code and the path to the compiled classes
     * @throws IOException          if there are problems traversing the source code hierarchy
     * @throws InterruptedException if the spawned javadoc process is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException {
       String sourcePath = args[0];
       String classPath = args[1];
 
       List<String> command = Lists.newArrayList("javadoc",
             "-classpath", System.getProperty("java.class.path") + ":" + classPath,
             "-docletpath", System.getProperty("java.class.path"),
             "-private",
             "-doclet", DomainObjectDocletCleaner.class.getCanonicalName()
       );
 
       if (args.length > 2) {
          Collections.addAll(command, Arrays.copyOfRange(args, 2, args.length));
       }
 
       command.addAll(listFileNames(new File(sourcePath)));
        
       Process process = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
 
       BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
       BufferedReader processError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 
       String stdout = "Running cleaner...", stderr = null;
       while (stdout != null || stderr != null) {
          stdout = processOutput.readLine();
          stderr = processError.readLine();    
          if (stdout != null) System.out.println("INFO: jclouds cleaner: " + stdout);
          if (stderr != null) System.out.println("ERROR: jclouds cleaner: " + stderr);
       }
 
       process.destroy();
 
       if (process.exitValue() == 0) {
          System.out.println("Javadoc returned successfully");
       } else {
          System.out.println("Javadoc returned an error code");
          System.out.println("You passed the following arguments: " + Joiner.on(" ").join(args));
       }
    }
 
    /**
     * @see com.sun.javadoc.Doclet#languageVersion()
     */
    @SuppressWarnings("unused")
    public static LanguageVersion languageVersion() {
       return LanguageVersion.JAVA_1_5;
    }
 
    /**
     * Adding -d option for output path and -z for structure output
     *
     * @see com.sun.javadoc.Doclet#optionLength(String)
     */
    @SuppressWarnings("unused")
    public static int optionLength(String option) {
       if (Objects.equal(option, "-d")) return 2;
       if (Objects.equal(option, "-z")) return 1;
       if (Objects.equal(option, "-jaxb")) return 1;
       return Doclet.optionLength(option);
    }
 
    /**
     * Adding -d option for output path and -z for structure output
     *
     * @see com.sun.javadoc.Doclet#validOptions(String[][], com.sun.javadoc.DocErrorReporter)
     */
    @SuppressWarnings("unused")
    public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
       return true;
    }
 
    /**
     * Route into doclet processing
     *
     * @see com.sun.javadoc.Doclet#start(com.sun.javadoc.RootDoc)
     */
    @SuppressWarnings("unused")
    public static boolean start(RootDoc root) {
       try {
          readOptions(root.options());
          List<ClassDocPrinter> printers;
          if (jaxbOutput) {
             printers = ImmutableList.of(
                   new StaticFieldPrinter(),
                   new BeanBuilderPrinter(),
                   new InstanceFieldPrinter(true, true, false),
                   new BuilderArgConstructorPrinter(),
                   new NoArgsConstructorPrinter("for JAXB"),
                   new AccessorPrinter(),
                   new EqualsPrinter(),
                   new HashCodePrinter(),
                   new ToStringPrinter());
          } else if (minimalOutput) {
             printers = ImmutableList.<ClassDocPrinter>of(new StaticFieldPrinter(), new InstanceFieldPrinter(true, true, true));
          } else {
             printers = ImmutableList.of(
                   new StaticFieldPrinter(),
                   new BeanBuilderPrinter(),
                   new InstanceFieldPrinter(true, true, false),
                   new BuilderArgConstructorPrinter(),
                   new AccessorPrinter(),
                   new EqualsPrinter(),
                   new HashCodePrinter(),
                   new ToStringPrinter());
          }
 
 
          BeanPrinter writer = new BeanPrinter(outputPath, printers);
          for (ClassDoc clazz : root.classes()) {
             String className = clazz.simpleTypeName();
             String packageName = clazz.containingPackage().name();
             File outputFile = new File(outputPath, packageName.replaceAll("\\.", File.separator) + File.separator + className + ".java");
             outputFile.getParentFile().mkdirs();
             System.out.println("Processing " + clazz.name() + " writing to " + outputFile.getAbsolutePath());
             if (clazz.containingClass() == null) {
                writer.write(clazz, new IndentedPrintWriter(new FileOutputStream(outputFile)));
             }
          }
          return true;
       } catch (IOException e) {
          throw Throwables.propagate(e);
       }
    }
 
    private static void readOptions(String[][] options) {
       for (String[] opt : options) {
          if (opt[0].equals("-d")) {
             outputPath = opt[1];
          }
          if (opt[0].equals("-jaxb")) {
             jaxbOutput = true;
          }
          if (opt[0].equals("-z")) {
             System.out.println("minimizing!");
             minimalOutput = true;
          }
       }
    }
 
    private static List<String> listFileNames(File file) throws IOException {
       List<String> result = Lists.newArrayList();
       for (File f : listFiles(file)) {
          result.add(f.getAbsolutePath());
       }
       return result;
    }
 
    private static List<File> listFiles(File file) throws IOException {
       ImmutableList.Builder<File> newOnes = ImmutableList.builder();
       if (file.isDirectory()) {
          newOnes.addAll(listFiles(file, new ArrayList<File>()));
       } else if (file.getName().endsWith(".java")) {
          newOnes.add(file);
       }
       return newOnes.build();
    }
 
    private static List<File> listFiles(File file, List<File> result) {
       if (file.isDirectory()) {
          for (File directory : file.listFiles(new FileFilter() {
             public boolean accept(File file) {
                return file.isDirectory();
             }
          })) {
             listFiles(directory, result);
          }
          result.addAll(Arrays.asList(file.listFiles(new PatternFilenameFilter(".*\\.java"))));
       }
       return result;
    }
 }
