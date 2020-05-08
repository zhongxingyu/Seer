 package com.cedarsoft.maven.instrumentation.plugin;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Nonnull;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  * @goal instrument-tests
 * @phase process-classes
  * @requiresDependencyResolution compile+runtime+test
  */
 public class InstrumentationTestsMojo extends AbstractInstrumentationMojo {
   /**
    * @parameter expression="${project.build.testOutputDirectory}"
    * @read-only
    * @required
    */
   private File outputDirectory;
 
   @Override
   @Nonnull
   protected File getOutputDirectory() {
     return outputDirectory;
   }
 
   /**
    * Project classpath.
    *
    * @parameter default-value="${project.compileClasspathElements}"
    * @required
    * @readonly
    */
   private List<String> classpathElements;
 
   @Nonnull
   @Override
   protected Iterable<? extends String> getClasspathElements() {
     return Collections.unmodifiableList(classpathElements);
   }
 }
