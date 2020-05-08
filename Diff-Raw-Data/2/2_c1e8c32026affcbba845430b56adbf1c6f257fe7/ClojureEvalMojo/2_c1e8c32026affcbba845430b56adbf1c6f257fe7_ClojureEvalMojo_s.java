 package org.clojure.maven;
 
 import java.io.File;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Evaluate Clojure source code
  *
  * @goal eval
  */
 public class ClojureEvalMojo extends AbstractClojureMojo {
 
     /**
      * Clojure code to be evaluated
      * @parameter expression="${clojure.eval}"
      * @required
      */
     private String eval;
 
     /**
      * Classpath scope: one of 'compile', 'test', or
      * 'runtime'. Defaults to 'test'.
      * @parameter expression="${clojure.scope}" default-value="test"
      * @required
      */
     private String scope;
 
     public void execute() throws MojoExecutionException {
         Classpath classpath;
         int classpathScope = getClasspathScope();
         try {
             classpath = new Classpath(project, classpathScope, null);
         } catch (Exception e) {
             throw new MojoExecutionException("Classpath initialization failed", e);
         }
         IsolatedThreadRunner runner =
             new IsolatedThreadRunner(getLog(), classpath,
                                      new ClojureEvalTask(getLog(), eval));
         runner.run();
         Throwable t = runner.getUncaught();
         if (t != null) {
            throw new MojoExecutionException("Clojure compilation failed", t);
         }
     }
 
     private int getClasspathScope() throws MojoExecutionException {
         if ("compile".equals(scope)) {
             return Classpath.COMPILE_CLASSPATH | Classpath.COMPILE_SOURCES;
         } else if ("test".equals(scope)) {
             return Classpath.COMPILE_CLASSPATH | Classpath.COMPILE_SOURCES |
                 Classpath.TEST_CLASSPATH | Classpath.TEST_SOURCES |
                 Classpath.RUNTIME_CLASSPATH;
         } else if ("runtime".equals(scope)) {
             return Classpath.COMPILE_CLASSPATH | Classpath.COMPILE_SOURCES |
                 Classpath.RUNTIME_CLASSPATH;
         } else {
             throw new MojoExecutionException("Invalid classpath scope: " + scope);
         }
     }
 }
 
