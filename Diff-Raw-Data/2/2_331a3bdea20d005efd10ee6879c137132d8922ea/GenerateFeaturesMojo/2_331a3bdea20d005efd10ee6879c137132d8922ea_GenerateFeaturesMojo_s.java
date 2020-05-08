 package com.github.praxissoftware.maven.plugins;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.google.common.io.CharStreams;
 import com.google.common.io.Closeables;
 import com.google.common.io.InputSupplier;
 import com.sampullara.mustache.Mustache;
 import com.sampullara.mustache.MustacheBuilder;
 
 /**
  * Generates a single feature with all relevant dependencies inside of it.
  * @goal generate-features-xml
  */
 public class GenerateFeaturesMojo extends AbstractMojo {
 
   /**
    * The Maven project to analyze.
    * 
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
   private MavenProject project;
 
   /**
    * The file to output the features.xml to.
    * 
    * @parameter expression="${project.build.outputDirectory}/feature.xml"
    */
   private File outputFile;
 
   @SuppressWarnings("unchecked")
   @Override
   public void execute() throws MojoExecutionException {
     Writer out = null;
     try {
 
       // Get the template text from the jar's resources.
       final InputSupplier<InputStreamReader> supplier = CharStreams.newReaderSupplier(new InputSupplier<InputStream>() {
         @Override
         public InputStream getInput() throws IOException {
           return getClass().getClassLoader().getResourceAsStream("features.mustache.xml");
         }
       }, Charsets.UTF_8);
       final String template = CharStreams.toString(supplier);
 
       // Create the mustache factory from the loaded template.
       final Mustache mustache = new MustacheBuilder().parse(template, "features.mustache.xml");
 
       // Establish output stream.
       final File featureFile = setUpFile(outputFile);
       out = new FileWriter(featureFile);
 
       // Build context.
       final Map<String, Object> context = convert(project.getArtifact());
 
       final List<Map<String, Object>> dependencies = Lists.newArrayList();
       for( final Artifact dependency : Ordering.natural().onResultOf(new SortByCoordinates()).sortedCopy(Iterables.filter((Collection<Artifact>) project.getDependencyArtifacts(), new ArtifactsWeWant())) ) {
         dependencies.add(convert(dependency));
       }
       context.put("dependencies", dependencies);
 
       getLog().info("Writing feature to " + outputFile.getAbsolutePath());
 
       // Render template.
       mustache.execute(out, context);
     } catch( final Exception e ) {
       Throwables.propagateIfInstanceOf(e, MojoExecutionException.class);
       Throwables.propagateIfPossible(e);
       throw new MojoExecutionException("Unable to generate features.xml.", e);
     } finally {
       Closeables.closeQuietly(out);
     }
   }
 
   private Map<String, Object> convert(final Artifact artifact) {
     final Map<String, Object> map = Maps.newHashMap();
     map.put("groupId", artifact.getGroupId());
     map.put("artifactId", artifact.getArtifactId());
    map.put("version", artifact.getVersion());
     map.put("hasClassifier", artifact.getClassifier() != null);
     map.put("classifier", artifact.getClassifier());
     map.put("hasType", artifact.getType() != null);
     map.put("type", artifact.getType());
     return map;
   }
 
   private File setUpFile(final File target) {
     if( target.exists() ) {
       target.delete();
     } else if( !target.getParentFile().exists() ) {
       target.getParentFile().mkdirs();
     }
     return target;
   }
 
   private class ArtifactsWeWant implements Predicate<Artifact> {
     private final Set<String> valid = Sets.newHashSet("compile", "runtime");
 
     @Override
     public boolean apply(final Artifact input) {
       return valid.contains(input.getScope());
     }
   }
 
   private class SortByCoordinates implements Function<Artifact, String> {
     @Override
     public String apply(final Artifact input) {
       return String.format("%s:%s:%s:%s", input.getGroupId(), input.getArtifactId(), input.getVersion(), input.getType());
     }
   }
 }
