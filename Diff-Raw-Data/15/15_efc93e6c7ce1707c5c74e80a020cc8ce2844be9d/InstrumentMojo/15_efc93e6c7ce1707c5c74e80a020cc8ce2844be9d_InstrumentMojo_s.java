 // cobertura plugin for Maven 2
 // Copyright (c) 2007 Alexandre ROMAN and contributors
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files (the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions:
 //
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 //
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 
 // $Id: coberturaInstrumentMojo.java 6585 2008-03-28 10:45:54Z bentmann $
 
 package org.sonatype.maven.plugin.cobertura4it;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
import java.util.Enumeration;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
 import net.sourceforge.cobertura.coveragedata.ProjectData;
 import net.sourceforge.cobertura.instrument.NonDummClassInstrumenter;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.ArtifactUtils;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.handler.ArtifactHandler;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.IOUtil;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 
 /**
  * Offline class instrumentor.
  * 
  * @author <a href="mailto:velo.br@gmail.com">Marvin Froeder</a>
  * @goal instrument
  * @requiresDependencyResolution test
  */
 public class InstrumentMojo
     extends AbstractMojo
 {
 
     /**
      * Artifact factory.
      * 
      * @component
      */
     private ArtifactFactory factory;
 
     /**
      * Specifies the instrumentation paths to use.
      * 
      * @parameter
      */
     protected File[] instrumentationPaths;
 
     /**
      * Extra parameters for JVM used by cobertura.
      * 
      * @parameter expression="${cobertura.jvmParameters}" default-value="-Xmx256m"
      */
     protected String jvmParameters;
 
     /**
      * Indicates whether the metadata should be merged into the destination <code>metadataFile</code>, if any.
      * 
      * @parameter expression="${cobertura.merge}" default-value="true"
      */
     protected boolean merge;
 
     /**
      * Location to store class coverage metadata.
      * 
      * @parameter expression="${cobertura.metadataFile}" default-value="${project.build.directory}/coverage.em"
      */
     protected File metadataFile;
 
     /**
      * Location to store cobertura generated resources.
      * 
      * @parameter expression="${cobertura.outputDirectory}"
      *            default-value="${project.build.directory}/generated-classes/cobertura"
      */
     protected File outputDirectory;
 
     /**
      * Plugin classpath.
      * 
      * @parameter expression="${plugin.artifacts}"
      * @required
      * @readonly
      */
     protected List pluginClasspath;
 
     /**
      * Maven project.
      * 
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
 
     /**
      * // TODO need to figure out what are this about
      * 
      * @parameter
      */
     private Collection ignoreRegexes = new Vector();
 
     /**
      * // TODO need to figure out what are this about
      * 
      * @parameter
      */
     private Collection ignoreBranchesRegexes = new Vector();
 
     /**
      * Add cobertura dependency to project test classpath. When tests are executed, cobertura runtime dependency is
      * required.
      * 
      * @throws MojoExecutionException if cobertura dependency could not be added
      */
     private void addCoberturaDependenciesToTestClasspath()
         throws MojoExecutionException
     {
         // look for cobertura dependency in this plugin classpath
         final Map<String, Artifact> pluginArtifactMap = ArtifactUtils.artifactMapByVersionlessId( pluginClasspath );
        Artifact coberturaArtifact = (Artifact) pluginArtifactMap.get( "net.sourceforge.cobertura:cobertura" );
 
         if ( coberturaArtifact == null )
         {
             // this should not happen
             throw new MojoExecutionException( "Failed to find 'cobertura' artifact in plugin dependencies" );
         }
 
         // set cobertura dependency scope to test
         coberturaArtifact = artifactScopeToTest( coberturaArtifact );
 
         // add cobertura to project dependencies
         final Set<Artifact> deps = new LinkedHashSet<Artifact>();
         if ( project.getDependencyArtifacts() != null )
         {
             deps.addAll( project.getDependencyArtifacts() );
         }
         deps.add( coberturaArtifact );
         project.setDependencyArtifacts( deps );
     }
 
     /**
      * Convert an artifact to a test artifact.
      * 
      * @param artifact to convert
      * @return an artifact with a test scope
      */
     private Artifact artifactScopeToTest( Artifact artifact )
     {
         return factory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                       Artifact.SCOPE_TEST, artifact.getType() );
     }
 
     /**
      * Execute this Mojo.
      * 
      * @throws MojoExecutionException if execution failed
      * @throws MojoFailureException if execution failed
      */
     public final void execute()
         throws MojoExecutionException, MojoFailureException
     {
         final ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
         if ( !"java".equals( artifactHandler.getLanguage() ) )
         {
             getLog().info( "Not executing cobertura, as the project is not a Java classpath-capable package" );
             return;
         }
 
         if ( instrumentationPaths == null )
         {
             instrumentationPaths = new File[] { new File( project.getBuild().getOutputDirectory() ) };
         }
 
         getLog().info( "Instrumenting classes with cobertura" );
         ProjectData projectData = new ProjectData();
 
         for ( File path : instrumentationPaths )
         {
             instrumentPath( projectData, path );
         }
 
         File coberturaFolder = new File( project.getBuild().getDirectory(), "cobertura" );
         if ( !coberturaFolder.exists() )
         {
             coberturaFolder.mkdirs();
         }
 
         CoverageDataFileHandler.saveCoverageData( projectData, new File( coberturaFolder, "cobertura.ser" ) );
 
         // prepare test execution by adding Cobertura dependencies
         addCoberturaDependenciesToTestClasspath();
 
     }
 
     private void instrumentPath( ProjectData projectData, File path )
         throws MojoExecutionException
     {
         if ( !path.exists() )
         {
             return;
         }
 
         DirectoryScanner scan = new DirectoryScanner();
         scan.setBasedir( path );
         scan.setIncludes( new String[] { "**/*" } );
         scan.addDefaultExcludes();
         scan.scan();
 
         try
         {
             String[] files = scan.getIncludedFiles();
             for ( String file : files )
             {
                 FileInputStream input = new FileInputStream( new File( path, file ) );
 
                 File dest = new File( outputDirectory, file );
                 dest.getParentFile().mkdirs();
                 FileOutputStream output = new FileOutputStream( dest );
 
                 try
                 {
 
                     byte[] classContent = IOUtil.toByteArray( input );
                    if ( file.endsWith( ".class" ) )
                     {
                         ClassReader cr = new ClassReader( classContent );
                         ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
                         NonDummClassInstrumenter cv =
                             new NonDummClassInstrumenter( projectData, cw, ignoreRegexes, ignoreBranchesRegexes );
                         cr.accept( cv, 0 );
 
                         if ( cv.isInstrumented() )
                         {
                             classContent = cw.toByteArray();
                         }
                     }
 
                     IOUtil.copy( classContent, output );
                 }
                 finally
                 {
                     IOUtil.close( input );
                     IOUtil.close( output );
                 }
 
             }
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( e.getMessage(), e );
         }
     }
 }
