 package se.diabol.maven.plugins.checks;
 
 import org.apache.maven.enforcer.rule.api.EnforcerRule;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.*;
 import org.apache.maven.plugins.enforcer.EnforceMojo;
 import org.apache.maven.plugins.enforcer.RequireReleaseDeps;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.path.PathTranslator;
 import org.codehaus.plexus.context.Context;
 import org.codehaus.plexus.context.ContextException;
 
 @Mojo(name = "check-snapshots", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true,
         requiresDependencyResolution = ResolutionScope.TEST)
public class CheckSnapshotsMojo extends AbstractMojo {
 
     private EnforceMojo enforce = new EnforceMojo();
 
 
     /**
      * Path Translator needed by the ExpressionEvaluator
      */
     @Component(role = PathTranslator.class)
     protected PathTranslator translator;
 
     /**
      * The MavenSession
      */
     @Component
     protected MavenSession session;
 
     /**
      * POM
      */
     @Component
     protected MavenProject project;
 
 
     /**
      * Flag to fail the build if a version check fails.
      */
    @Parameter(property = "enforcer.fail", defaultValue = "true")
     protected boolean fail = true;
 
     /**
      * Fail on the first rule that doesn't pass
      */
    @Parameter(property = "enforcer.failFast", defaultValue = "false")
     protected boolean failFast = false;
 
     public void contextualize(Context context)
             throws ContextException {
         enforce.contextualize(context);
     }
 
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         enforce.setProject(project);
         enforce.setTranslator(translator);
         enforce.setSession(session);
         enforce.setFail(fail);
         enforce.setFailFast(failFast);
         enforce.setRules(new EnforcerRule[]{new RequireReleaseDeps()});
         enforce.execute();
     }
 }
