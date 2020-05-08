 package com.thegrayfiles.tests.integration;
 
 import com.thegrayfiles.exception.CompilationFailedException;
 import com.thegrayfiles.processor.SpringControllerAnnotationProcessor;
 import com.thegrayfiles.util.TestDirectories;
 import org.testng.annotations.Test;
 
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static org.testng.Assert.assertTrue;
 
 public class GeneratedClientTests {
 
     private TestDirectories testDirectories = new TestDirectories();
 
     @Test
     public void processorProducesClientSourceFile() throws CompilationFailedException, IOException {
         File clientSourceFile = processTestController();
         assertTrue(clientSourceFile.exists(), "Client source file does not exist.");
     }
 
     @Test
     public void processorProducesCompilableClientSourceFile() throws CompilationFailedException, IOException {
         File clientSourceFile = processTestController();
         compile(clientSourceFile);
     }
 
     private File processTestController() throws CompilationFailedException, IOException {
         SpringControllerAnnotationProcessor processor = new SpringControllerAnnotationProcessor();
         File annotatedSourceFile = new File(testDirectories.getTestSources() + "/TestController.java");
 
         // create file to get the appropriate temp file name and then delete it so that the processor can recreate it
         File generatedSourcesDirectory = new File(testDirectories.getGeneratedSources());
         File clientSourceFile = File.createTempFile("TestClient", ".java", generatedSourcesDirectory);
         clientSourceFile.delete();
 
         Map<String, String> options = new HashMap<String, String>();
         options.put(SpringControllerAnnotationProcessor.OPTION_CLIENT_OUTPUT_FILE, clientSourceFile.getAbsolutePath());
         compile(annotatedSourceFile, processor, options);
         clientSourceFile.deleteOnExit();
 
         return clientSourceFile;
     }
 
     private void compile(File file) throws CompilationFailedException {
         compile(file, new SpringControllerAnnotationProcessor());
     }
 
     private void compile(File file, SpringControllerAnnotationProcessor processor) throws CompilationFailedException {
         compile(file, processor, new HashMap<String, String>());
     }
 
     /**
      * Compile a file and process it using the annotation processor specified.
      * @param file the file to compile
      * @param processor the annotation processor to use
      * @param options the options to pass to the annotation processor
      * @throws CompilationFailedException if compilation of the specified file fails
      */
     private void compile(File file, SpringControllerAnnotationProcessor processor, Map<String, String> options) throws CompilationFailedException {
         List<String> processorOptions = buildAnnotationProcessorOptionsList(options);
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
         StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
         Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjectsFromFiles(asList(file));
         JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, processorOptions, null, javaFileObjects);
 
         task.setProcessors(asList(processor));
 
         if (!task.call()) {
             throw new CompilationFailedException(file);
         }
     }
 
     private List<String> buildAnnotationProcessorOptionsList(Map<String, String> options) {
         List<String> optionsString = new ArrayList<String>();
         for (String key : options.keySet()) {
             optionsString.add("-A" + key + "=" + options.get(key));
         }
         return optionsString;
     }
 }
