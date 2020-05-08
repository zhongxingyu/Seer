 package com.ownedthx.xmldoclet;
 
 import com.thoughtworks.xstream.*;
 import com.thoughtworks.xstream.io.xml.XppDriver;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import com.sun.javadoc.*;
 import com.ownedthx.xmldoclet.xmlbindings.*;
 import com.ownedthx.xmldoclet.xmlbindings.Param;
 import com.ownedthx.xmldoclet.xmlbindings.Package;
 import com.ownedthx.xmldoclet.xmlbindings.Class;
 import com.ownedthx.xmldoclet.xmlbindings.Enum;
import com.ownedthx.xmldoclet.xmlbindings.TypeVar;
 import com.ownedthx.xmldoclet.parser.Parser;
 import com.ownedthx.xmldoclet.cmdline.CmdlineParser;
 import com.ownedthx.xmldoclet.xstream.CdataWrapper;
 import org.apache.commons.cli.ParseException;
 
 import java.lang.Exception;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.io.File;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.BasicConfigurator;
 
 /** Entry point into xml-doclet.
  * It provides a main(string[] args) for command line invokation,
  * as well as a constructor followed by processSource so that it can be invoked directly
  * by other applications with proper arguments.
  */
 public class App
 {
     public static final String loggingDir = "./log/doclet/";
 
     static Logger log = Logger.getLogger(App.class.getName());
 
 
     /**
      * This intemediary output static variable exists in order to capture
      * output of the start static method
      */
     protected static Root parsedRoot;
 
     public static void main( String[] args )
     {
         try
         {
             CmdlineParser.parse(args);
         }
         catch( ParseException e)
         {
             initializeLogging(null);
 
             BasicConfigurator.configure();
 
               log.error("Unable to parse command-line arguments.\n\n" +
                       "Exceptions:\n" + e +
                       "\n\n");
               CmdlineParser.printHelp();
         }
 
         initializeLogging(CmdlineParser.getLogPropertiesPath());
 
 
         // bail out if a help request
         if(CmdlineParser.helpRequested())
         {
             CmdlineParser.printHelp();
             System.exit(0);
         }
 
         // validate output path.  Append javadoc.xml if it's a directory instead of a file
         String output = CmdlineParser.getOut();
         if(output != null)
         {
             File outputFile = new File(output);
             if(outputFile.isDirectory())
             {
                 log.warn("The -out parameter is a directory.  Appending 'javadoc.xml'");
                 output = output + File.separatorChar + "javadoc.xml";
             }
         }
         else
         {
             output = "./javadoc.xml";
         }
 
         String[] sourcePaths = ParseConcatedPath(CmdlineParser.getSourcePath());
         String[] packages = ParseConcatedPath(CmdlineParser.getPackages());
         String[] sourceFiles = ParseConcatedPath(CmdlineParser.getSourceFiles());
         String[] subPackages = ParseConcatedPath(CmdlineParser.getSubPackages());
 
         App app = new App();
         Root root = app.processSource(CmdlineParser.getClassPath(), sourcePaths, packages, sourceFiles, subPackages);
         app.write(root, output);
     }
 
 
     /**
      * The constructor for App.
      * Note that if you use xml-doclet via this constructor, instead of
      * via command-line, then you should invoke processSource next to
      * kick-off processing.  Also, if not using command-line to invoke
      * xml-doclet, no log4j settings are set.  So either set them yourself,
      * or you can use the initializeLogging method to initialize log4j.
      *
      * Finally, if you do use this constructor, invoke write() after processSource
      * completes if you wish to write the XML to file.
      */
     public App()
     {
     }
 
     /**
      * Initializes logging.   If no log file exists, the log4j BasicConfigurator
      * will be made.  If a log file does exist in the specified location,
      * then it will be used to establish the log settings.
      * @param logPropertiesPath
      */
     public static void initializeLogging(String logPropertiesPath)
     {
         if(logPropertiesPath == null)
         {
             BasicConfigurator.configure();
 
             log.info( "No log4j properties file specified.  Using BasicConfigurator to log." );
         }
         else
         {
             File file = new File(logPropertiesPath);
 
             String errorMessage = null;
 
             boolean success = false;
             if(file.exists())
             {
                 PropertyConfigurator.configure( logPropertiesPath );
                 success = true; // TODO: figure out how this fails
             }
             else
             {
                 errorMessage = "No log property file exists at location: '" + logPropertiesPath +
                         "'\nDefaulting to basic log configuration";
 
                 BasicConfigurator.configure();
             }
         }
     }
 
     /**
      * Processes the source code using javadoc. After this method completes,
      * usually you would use write() to emit the parsed data out to XML.
      * @param extendedClassPath Any classpath information required to help along javadoc.
      *      Javadoc will actualy compile the source code you specify; so if there are any
      *      jars or classes that are referenced by the source code to process, then including
      *      those compiled items in the classpath will give you more complete data in
      *      the resulting XML.
      * @param sourcePaths Usually sourcePaths is specified in conjuction with either/both
      *      packages & subpackages.  The sourcepaths value should be the path of the source
      *      files right before the standard package-based folder layout of projects begins.
      *      For example, if you have code that exists in package foo.bar, and your code is physically
      *      in /MyFolder/foo/bar/ , then the sourcePaths would be /MyFolder
      * @param packages Use if you want to detail specific packages to process (contrast with subpackages,
      *      which is probably the easiest/most brute force way of using xml-doclet).  If you have
      *      within your code two packages, foo.bar and bar.foo, but only wanted foo.bar processed,
      *      then specify just 'foo.bar' for this argument.
      * @param sourceFiles You can specify source files individually.  This usually is used instead of
      *      sourcePaths/subPackages/packages.  If you use this parameter, specify the full path of
      *      any java file you want processed.
      * @param subPackages You can specify 'subPackages', which simply gives one an easy way to specify
      *      the root package, and have javadoc recursively look through everything under that package.
      *      So for instance, if you had foo.bar, foo.bar.bar, and bar.foo, specifying 'foo' will process
      *      foo.bar and foo.bar.bar packages, but not bar.foo (unless you specify 'bar' as a subpackage, too)
      * @return XStream compatible data structure
      */
     public Root processSource(String extendedClassPath, String[] sourcePaths,
                               String[] packages, String[] sourceFiles, String[] subPackages)
     {
         String classPath = CreateClassPath(extendedClassPath);
 
         String concatedSourcePaths = ConcatPaths(sourcePaths);
         String concatedSubPackages = ConcatStrings(subPackages, ':');
         
         // Prepare doclet output loggers
         CreateLoggingDirectory();
 
         try
         {
             // TODO: Currently logs created by the javadoc parser are not written
             // with log4j.  It would seem one would need to subclass PrintWriter
             // and have it write to the log4j system in order to integrate these logs
             // more fully.
             FileOutputStream errors = new FileOutputStream(loggingDir + "javadoc-errors.log");
             FileOutputStream warnings = new FileOutputStream(loggingDir + "javadoc-warnings.log");
             FileOutputStream notices = new FileOutputStream(loggingDir + "javadoc-notices.log");
 
             PrintWriter errorWriter = new PrintWriter(errors, false);
             PrintWriter warningWriter = new PrintWriter(warnings, false);
             PrintWriter noticeWriter = new PrintWriter(notices, false);
 
              // aggregate arguments and packages
             ArrayList<String> arguments = new ArrayList<String>();
 
             // by setting this to 'private', nothing is omitted in the parsing
             arguments.add("-private");
 
             arguments.add("-classpath");
             arguments.add(classPath);
 
             if(concatedSourcePaths != null)
             {
                 arguments.add("-sourcepath");
                 arguments.add(concatedSourcePaths);
             }
 
             if(concatedSubPackages != null)
             {
                 arguments.add("-subpackages");
                 arguments.add(concatedSubPackages);
             }
 
             if(packages != null && packages.length > 0)
             {
                 arguments.addAll(java.util.Arrays.asList(packages));
             }
 
             if(sourceFiles != null && sourceFiles.length > 0)
             {
                 arguments.addAll(Arrays.asList(sourceFiles));
             }
 
             String[] staticArguments = arguments.toArray(new String[] {});
 
             log.info( "Executing doclet with arguments: " + ConcatStrings(staticArguments, ' ') );
 
             com.sun.tools.javadoc.Main.execute(
                     "ownedthxxmldoclet",
                     errorWriter, warningWriter, noticeWriter,
                     "com.ownedthx.xmldoclet.App",
                     staticArguments
             );
 
             errors.close();
             warnings.close();
             notices.close();
 
             log.info("done with doclet processing");
         }
         catch(Exception e)
         {
             log.error("doclet exception", e);
         }
         catch(Error e)
         {
             log.error("doclet error", e);
         }
 
         return parsedRoot;
     }
 
     /**
      * Appends a new classpath to the current classpath
      * @param extendedClassPath The classpath info to add to the current classpath
      * @return The augmented classpath
      */
     private String CreateClassPath(String extendedClassPath)
     {
         // Construct classPath
         String classPath = System.getProperty("java.class.path",".");
         if(extendedClassPath != null)
         {
             classPath += File.pathSeparator + extendedClassPath;
         }
         return classPath;
     }
 
     /**
      * Concats an array of strings into a single string, using the specified delimiter
      * @param strings The array of strings to concat
      * @param delimiter The character to use when concating
      * @return The concated string array
      */
     private String ConcatStrings(String[] strings, char delimiter)
     {
          String concated = null;
         
         if(strings != null && strings.length > 0)
         {
             StringBuffer buffer = new StringBuffer();
 
             for(String item : strings)
             {
                 buffer.append(item);
 
                 buffer.append(delimiter);
             }
 
             buffer.deleteCharAt(buffer.length() - 1);
             concated = buffer.toString();
         }
 
         return concated;
     }
 
     /** Concats an array of strings into a single string,
      * using the system path seperator as delimiter
      * @param paths A set of paths to concat
      * @return The concated string array
      */
     private String ConcatPaths(String[] paths)
     {
         String concated = null;
         
         if(paths != null && paths.length > 0)
         {
             StringBuffer buffer = new StringBuffer();
 
             for(String item : paths)
             {
                 buffer.append(item);
                 buffer.append(File.pathSeparator);
             }
 
             buffer.deleteCharAt(buffer.length() - 1);
             concated = buffer.toString();
 
         }
 
         return concated;
     }
 
     /**
      * Splits path
      * @param path
      * @return
      */
     private static String[] ParseConcatedPath(String path)
     {
         String[] splitPath = null;
         if(path != null)
         {
             splitPath = path.split(File.pathSeparator);
         }
         return splitPath;
     }
 
     /**
      * Creates the directory for logging out the javadoc/doclet logging
      * @return success
      */
     private static boolean CreateLoggingDirectory()
     {
         boolean success = false;
 
         // create relative log folder
         File logFolder = new File(loggingDir);
         try
         {
             logFolder.mkdirs();
             success = true;
         }
         catch(Exception e)
         {
             // unable to create the log folder!
             log.error("Unable to create the log folder 'log'.", e);
         }
 
         return success;
     }
 
     public static LanguageVersion languageVersion()
     {
          return LanguageVersion.JAVA_1_5;
     }
 
     public static boolean start(RootDoc doc)
     {
         parsedRoot = Parser.ParseRoot(doc);
 
         return true;
     }
 
     /**
      * Writes the XStream compatible Root structure to file
      * @param root XStream compatible data structure
      * @param outputFile The file to write to
      * @return success
      */
     public boolean write(Root root, String outputFile)
     {
         if(outputFile == null)
         {
             // default of current directory with a name of javadoc.xml
             outputFile = "./javadoc.xml";
         }
         
         XStream stream = new XStream(new XppDriver()
         {
             public HierarchicalStreamWriter createWriter(Writer out)
             {
                 // Pass our custom cdata wrapper class here.
                 // this will insert CDATA tags around the start
                 // and end of content in the elemest
                 return new CdataWrapper(out) {};
             }
         });
 
         // you alias with XStream to avoid XStream's default serialization of
         // <com.ownedthx.xmldoclet.Method />
 
         stream.alias("method", Method.class);
         stream.alias("parameter", Param.class);
         stream.alias("annotationInstance", AnnotationInstance.class);
         stream.alias("exception", ExceptionInstance.class);
         stream.alias("class", Class.class);
         stream.alias("annotation", Annotation.class);
         stream.alias("element", AnnotationElement.class);
         stream.alias("package", Package.class);
         stream.alias("root", Root.class);
         stream.alias("constructor", Constructor.class);
         stream.alias("interface", Interface.class);
         stream.alias("enum", Enum.class);
         stream.alias("enumField", EnumField.class);
         stream.alias("field", Field.class);
         stream.alias("type", TypeInfo.class);
         stream.alias("scope", ScopeModifier.class);
         stream.alias("return", Result.class);
        stream.alias("typeVar", TypeVar.class);
 
         boolean outputDirExists = false;
         try
         {
             // attempt to create output Directory, if needed
             File outFile = new File(outputFile);
             File outDir = outFile.getParentFile();
 
             if(outDir != null && !outDir.exists())
             {
                 log.debug( "Creating folders for " + outDir );
                 outputDirExists = outDir.mkdirs();
             }
             else
             {
                 outputDirExists = true;
             }
         }
         catch(Exception e)
         {
             log.error("Unable to create output directory.", e);
             System.exit(1);
         }
 
         boolean createdOutputFile = false;
         FileOutputStream fileStream = null;
         if(outputDirExists)
         {
             try
             {
                 fileStream = new FileOutputStream(outputFile);
                 log.info("Created output file at: " + outputFile);
                 createdOutputFile = true;
             }
             catch(Exception e)
             {
                 log.error("Unable to create output file with javadoc XML content.", e);
             }
         }
 
 
         PrintWriter writer = new PrintWriter(fileStream);
 
         writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
         if(createdOutputFile)
         {
             log.info("Writing content to file.");
             stream.toXML(root, writer);
         }
         return outputDirExists && createdOutputFile;
     }
 }
 
