 package org.codehaus.mojo.rspec;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.jruby.Ruby;
 import org.jruby.RubyBoolean;
 import org.jruby.javasupport.JavaSupport;
 
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

 /**
  * Mojo to run Ruby Spec test
  * 
  * @author Mike Ward
  * @goal spec
  */
 public class RspecRunnerMojo extends AbstractMojo {
 
     /**
      * The classpath elements of the project being tested.
      *
      * @parameter expression="${project.testClasspathElements}"
      * @required
      * @readonly
      */
     protected List classpathElements;
 
     /**
      * The directory containing the RSpec source files
      *
      * @parameter
      * @required
      */
     protected String sourceDirectory;
 
     /**
      * The directory where the RSpec report will be written to
      *
      * @parameter
      * @required
      */
     protected String outputDirectory;
 
     /**
      * The directory where JRuby is installed (defaults to ~/.jruby)
      *
      * @parameter expression="${jruby.home}"
      */
     protected String jrubyHome;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         getLog().info("Running RSpec tests from " + sourceDirectory);
 
         Ruby runtime = Ruby.getDefaultInstance();
 
         if(jrubyHome != null) {
             runtime.setJRubyHome(jrubyHome);
         }
         JavaSupport javaSupport = new JavaSupport(runtime);
         runtime.getLoadService().init(classpathElements);
         runtime.defineGlobalConstant("ARGV", runtime.newArray());
 
         // Build ruby script to run RSpec's
         StringBuilder script = new StringBuilder();
         try {
             script.append(handleClasspathElements(javaSupport));
         } catch (MalformedURLException e) {
             throw new MojoExecutionException(e.getMessage());
         }
 
         // Run all specs
         script.append("require 'rubygems'\n")
                 .append("require 'java'\n")
                 .append("require 'spec'\n")
                 .append("specs = Dir[\"").append(sourceDirectory).append("/**/*_spec.rb\"]\n")
                 .append("specs << '-f'; specs << 'h'\n")
                 .append("report_file = File.new('").append(outputDirectory).append("/rspec_report.html', 'w')\n")
                .append("::Spec::Runner::CommandLine.run(specs, STDERR, report_file, false, true)\n");
         runtime.evalScript(script.toString());
 
         // Were there failures?
         script = new StringBuilder();
         script.append("if File.new('").append(outputDirectory).append("/rspec_report.html', 'r').read =~ /, 0 failures/ \n")
                 .append(" false\n")
                 .append("else\n")
                 .append(" true\n")
                 .append("end");
 
         RubyBoolean failure = (RubyBoolean) runtime.evalScript(script.toString());
 
         if (failure.isTrue()) {
             throw new MojoFailureException("RSpec tests failed.");
         }
 
         getLog().info("");
         getLog().info("------------------------------------------------------------------------");
         getLog().info("RSpec tests successful.");
         getLog().info("------------------------------------------------------------------------");
         getLog().info("");
     }
 
     private String handleClasspathElements(JavaSupport javaSupport) throws MalformedURLException {
         StringBuilder script = new StringBuilder();
 
        for (Object classpathElement : classpathElements) {
             String path = classpathElement.toString();
 
             if (path.endsWith(".jar")) {
                 script.append("require '").append(path).append("'\n"); // handling jar files
             } else {
                 javaSupport.addToClasspath(new URL("file:" + path + "/")); // handling directories
             }
         }
 
         return script.toString();
     }
 
 }
