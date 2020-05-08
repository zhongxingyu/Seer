 package com.atlassian.maven.plugins.amps.osgi;
 
 import aQute.lib.osgi.Constants;
 import com.atlassian.maven.plugins.amps.AbstractAmpsMojo;
 import static com.atlassian.maven.plugins.amps.util.FileUtils.file;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.jar.Manifest;
 
 @MojoGoal ("validate-manifest")
 public class ValidateManifestMojo extends AbstractAmpsMojo
 {
     /**
      * Whether to skip validation or not
      */
     @MojoParameter (expression = "${manifest.validation.skip}")
     protected boolean skipManifestValidation = false;
 
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         final File mfile = file(getMavenContext().getProject().getBuild().getOutputDirectory(), "META-INF", "MANIFEST.MF");
 
         // Only valid if the manifest exists
         if (!skipManifestValidation && mfile.exists())
         {
             getLog().info("Manifest found, validating...");
             InputStream mfin = null;
             try
             {
                 checkManifestEndsWithNewLine(mfile);
 
                 mfin = new FileInputStream(mfile);
                 Manifest mf = new Manifest(mfin);
                 PackageImportVersionValidator validator = new PackageImportVersionValidator(getMavenContext().getProject(),
                        getMavenContext().getLog(), getPluginInformation().getId());
                 validator.validate(mf.getMainAttributes().getValue(Constants.IMPORT_PACKAGE));
             }
             catch (IOException e)
             {
                 throw new MojoExecutionException("Unable to read manifest", e);
             }
             finally
             {
                 IOUtils.closeQuietly(mfin);
             }
             getLog().info("Manifest validated");
         }
         else
         {
             getLog().info("No manifest found or validation skip flag specified, skipping validation");
         }
     }
 
     private void checkManifestEndsWithNewLine(final File mfile)
             throws IOException, MojoExecutionException, MojoFailureException
     {
         InputStream is = null;
         try
         {
             is = new FileInputStream(mfile);
             final long bytesToSkip = mfile.length() - 1;
             long bytesSkipped = is.skip(bytesToSkip);
             if (bytesSkipped != bytesToSkip)
             {
                 throw new MojoExecutionException("Could not skip " + bytesToSkip + " bytes reading " + mfile.getAbsolutePath());
             }
             else if (is.read() != '\n')
             {
                 throw new MojoFailureException("Manifests must end with a new line. " + mfile.getAbsolutePath() + " doesn't.");
             }
         }
         finally
         {
             IOUtils.closeQuietly(is);
         }
     }
 }
