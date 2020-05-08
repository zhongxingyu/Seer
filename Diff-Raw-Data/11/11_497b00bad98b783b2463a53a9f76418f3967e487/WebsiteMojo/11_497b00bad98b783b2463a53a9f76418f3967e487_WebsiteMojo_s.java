 /*
  * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.qipki.site.plugin;
 
 import com.petebevin.markdown.MarkdownProcessor;
 import japa.parser.JavaParser;
 import japa.parser.ParseException;
 import japa.parser.ast.Comment;
 import japa.parser.ast.CompilationUnit;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.shared.filtering.MavenFileFilter;
 import org.apache.maven.shared.filtering.MavenFilteringException;
 import org.codeartisans.java.toolbox.Pair;
 import org.codehaus.plexus.util.FileUtils;
 
 /**
  * @goal site
  */
 //
 // @parameter [alias="someAlias"] [expression="${someExpression}"] [default-value="value"]
 // @required
 // @readonly
 // @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
 // @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
 //
 public class WebsiteMojo
         extends AbstractMojo
 {
 
     // -----------------------------------------------------------------------------------------------------------------
     /**
      * @parameter default-value="${project.basedir}/src/main/website/static"
      */
     private File staticDirectory;
     /**
      * @parameter default-value="${project.basedir}/src/main/website/layout"
      */
     private File layoutDirectory;
     /**
      * @parameter default-value="${project.basedir}/src/main/website/markup"
      */
     private File markupDirectory;
     /**
      * @parameter default-value="true"
      */
     private boolean filterMarkup;
     /**
      * @parameter default-value="true"
      */
     private boolean filterStatic;
     /**
      * @parameter
      */
     private List<File> snippetSources;
     /**
      * @parameter default-value="false"
      */
     private boolean staticLast;
     /**
      * @parameter default-value="${project.build.directory}/website"
      */
     private File outputDirectory;
     // -----------------------------------------------------------------------------------------------------------------
     /**
      * @parameter default-value="${project.basedir}"
      * @readonly
      */
     private File basedir;
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject mavenProject;
     /**
      * @parameter expression="${session}"
      * @required
      * @readonly
      */
     protected MavenSession mavenSession;
     /**
      * @component
      * @required
      * @readonly
      */
     private MavenFileFilter mavenFileFilter;
 
     @Override
     public void execute()
             throws MojoExecutionException, MojoFailureException
     {
         prepareOutputDirectory();
         gatherCodeSnippets();
         if ( staticLast ) {
             if ( validateSource( "markup", markupDirectory ) ) {
                 doMarkup();
             }
             if ( validateSource( "static", staticDirectory ) ) {
                 doStatic();
             }
         } else {
             if ( validateSource( "static", staticDirectory ) ) {
                 doStatic();
             }
             if ( validateSource( "markup", markupDirectory ) ) {
                 doMarkup();
             }
         }
     }
 
     // This is a ugly complex memory hungry first try, it needs to be reworked
     private void gatherCodeSnippets()
             throws MojoExecutionException
     {
         getLog().info( ">>> Website Maven Plugin :: gatherCodeSnippets( )" );
         try {
             Pattern beginPattern = Pattern.compile( "^.*SNIPPET BEGIN ([A-Za-z0-9-_\\.]+).*$" );
             Pattern endPattern = Pattern.compile( "^.*SNIPPET END ([A-Za-z0-9-_\\.]+).*$" );
 
             Map<String, String> snippets = new HashMap<String, String>();
 
             for ( File eachSnippetSource : snippetSources ) {
                 for ( File eachSourceFile : ( List<File> ) FileUtils.getFiles( eachSnippetSource, "**/*.java", null, true ) ) {
 
                     CompilationUnit cu = JavaParser.parse( eachSourceFile, "UTF-8" );
 
                     if ( cu.getComments() != null ) {
                         Map<String, Pair<Integer>> snippetsRanges = new HashMap<String, Pair<Integer>>();
                         Map<String, Integer> openSnippets = new HashMap<String, Integer>();
 
                         for ( Comment eachComment : cu.getComments() ) {
                             String candidate = eachComment.toString().trim();
                             Matcher matcher = beginPattern.matcher( candidate );
                             if ( matcher.matches() ) {
                                 String snippetName = matcher.group( 1 );
                                 openSnippets.put( snippetName, eachComment.getEndLine() + 1 );
                             }
                             matcher = endPattern.matcher( candidate );
                             if ( matcher.matches() ) {
                                 String snippetName = matcher.group( 1 );
                                 if ( !openSnippets.containsKey( snippetName ) ) {
                                     getLog().warn( "!!! Found a closing snippet but it was not openning, it will be ignored: " + snippetName );
                                 } else {
                                     snippetsRanges.put( snippetName, new Pair<Integer>( openSnippets.get( snippetName ), eachComment.getBeginLine() - 1 ) );
                                     openSnippets.remove( snippetName );
                                 }
                             }
                         }
 
                         if ( !openSnippets.isEmpty() ) {
                             for ( String eachOpenSnippet : openSnippets.keySet() ) {
                                 getLog().warn( "!!! Snippet '" + eachOpenSnippet + "' was left open, it will be ignored." );
                             }
                         }
 
                         if ( !snippetsRanges.isEmpty() ) {
                            String wholeSource = FileUtils.fileRead( eachSourceFile );
                             for ( Map.Entry<String, Pair<Integer>> eachEntry : snippetsRanges.entrySet() ) {
                                 String snippetName = eachEntry.getKey();
                                 int start = eachEntry.getValue().left();
                                 int end = eachEntry.getValue().right();
                                 String snippet = extractRange( wholeSource, start, end );
                                 snippet = "// Snippet extracted from " + eachSourceFile.getName() + " starting on line " + start + "\n\n" + snippet;
                                 snippets.put( "snippet." + snippetName, snippet );
                             }
                         }
                     }
                 }
             }
 
             mavenProject.getProperties().putAll( snippets );
 
             getLog().info( "- Gathered " + snippets.size() + " code snippets: " + snippets.keySet() );
 
         } catch ( IOException ex ) {
             throw new MojoExecutionException( "Unable to gather code snippets: " + ex.getMessage(), ex );
         } catch ( ParseException ex ) {
             throw new MojoExecutionException( "Unable to gather code snippets: " + ex.getMessage(), ex );
         }
     }
 
     public static String extractRange( String source, int firstline, int lastline )
     {
         String[] lines = source.split( "\n" );
         int idx = firstline - 1;
         StringWriter sw = new StringWriter();
         while ( idx < lines.length && idx < lastline ) {
             sw.append( lines[idx] ).append( "\n" );
             idx++;
         }
         return sw.toString();
     }
 
     private void prepareOutputDirectory()
             throws MojoExecutionException
     {
         try {
             if ( false ) {
                 FileUtils.deleteDirectory( outputDirectory );
             }
             FileUtils.mkdir( outputDirectory.getAbsolutePath() );
         } catch ( IOException ex ) {
             throw new MojoExecutionException( "Unable to prepare output directory: " + ex.getMessage(), ex );
         }
     }
 
     private boolean validateSource( String name, File directory )
     {
         if ( !directory.exists() || directoryIsEmpty( directory ) ) {
             getLog().warn( "!!!" + name + "Directory does not exists or is empty" );
             return false;
         }
         return true;
     }
 
     private void doStatic()
             throws MojoExecutionException
     {
         getLog().info( ">>> Website Maven Plugin :: doStatic()" );
         try {
             if ( filterStatic ) {
                 copyDirectoryStructureFiltering( staticDirectory, outputDirectory, true, mavenProject, Collections.emptyList(), false, "UTF-8", mavenSession );
                 getLog().info( "- Filtered " + staticDirectory + " to " + outputDirectory );
             } else {
                 FileUtils.copyDirectoryStructure( staticDirectory, outputDirectory );
                 getLog().info( "- Copied " + staticDirectory + " to " + outputDirectory );
             }
         } catch ( MavenFilteringException ex ) {
             throw new MojoExecutionException( "Unable to filter static content: " + ex.getMessage(), ex );
         } catch ( IOException ex ) {
             throw new MojoExecutionException( "Unable to copy static content: " + ex.getMessage(), ex );
         }
     }
 
     public void copyDirectoryStructureFiltering( File sourceDirectory, File destinationDirectory, boolean filtering,
                                                  MavenProject mavenProject, List filters, boolean escapedBackslashesInFilePath,
                                                  String encoding, MavenSession mavenSession )
             throws IOException, MavenFilteringException
     {
         copyDirectoryStructureFiltering( sourceDirectory, destinationDirectory, destinationDirectory, filtering, mavenProject,
                                          filters, escapedBackslashesInFilePath, encoding, mavenSession );
     }
 
     public void copyDirectoryStructureFiltering( File sourceDirectory, File destinationDirectory, File rootDestinationDirectory, boolean filtering,
                                                  MavenProject mavenProject, List filters, boolean escapedBackslashesInFilePath,
                                                  String encoding, MavenSession mavenSession )
             throws IOException, MavenFilteringException
     {
         if ( sourceDirectory == null ) {
             throw new IOException( "source directory can't be null." );
         }
         if ( destinationDirectory == null ) {
             throw new IOException( "destination directory can't be null." );
         }
         if ( sourceDirectory.equals( destinationDirectory ) ) {
             throw new IOException( "source and destination are the same directory." );
         }
         if ( !sourceDirectory.exists() ) {
             throw new IOException( "Source directory doesn't exists (" + sourceDirectory.getAbsolutePath() + ")." );
         }
 
         File[] files = sourceDirectory.listFiles();
         String sourcePath = sourceDirectory.getAbsolutePath();
 
         for ( int i = 0; i < files.length; i++ ) {
             File file = files[i];
 
             if ( file.equals( rootDestinationDirectory ) ) {
                 // We don't copy the destination directory in itself
                 continue;
             }
 
             String dest = file.getAbsolutePath();
             dest = dest.substring( sourcePath.length() + 1 );
             File destination = new File( destinationDirectory, dest );
 
             if ( file.isFile() ) {
 
                 destination = destination.getParentFile();
                 if ( "html".equals( FileUtils.getExtension( file.getName() ) ) ) {
                     mavenFileFilter.copyFile( file, new File( destination, file.getName() ), filtering, mavenProject, filters, escapedBackslashesInFilePath, encoding, mavenSession );
                 } else {
                     FileUtils.copyFile( file, new File( destination, file.getName() ) );
                 }
 
             } else if ( file.isDirectory() ) {
 
                 if ( !destination.exists() && !destination.mkdirs() ) {
                     throw new IOException( "Could not create destination directory '" + destination.getAbsolutePath() + "'." );
                 }
                 copyDirectoryStructureFiltering( file, destination, filtering, mavenProject, filters, escapedBackslashesInFilePath, encoding, mavenSession );
 
             } else {
                 throw new IOException( "Unknown file type: " + file.getAbsolutePath() );
             }
         }
     }
 
     private void doMarkup()
             throws MojoExecutionException
     {
         getLog().info( ">>> Website Maven Plugin :: doMarkup()" );
         try {
             String layout = FileUtils.fileRead( new File( layoutDirectory, "main.layout.html" ), "UTF-8" );
             MarkdownProcessor md = new MarkdownProcessor();
             FileFilter fileFilter = new MarkdownFileFilter();
             Stack<File> stack = new Stack<File>();
             stack.addAll( Arrays.asList( markupDirectory.listFiles( fileFilter ) ) );
             while ( !stack.empty() ) {
                 File eachFile = stack.pop();
                 // Recurse if needed
                 if ( eachFile.isDirectory() ) {
                     stack.addAll( Arrays.asList( eachFile.listFiles( fileFilter ) ) );
                     getLog().info( "- Added descendants of " + eachFile );
                     continue;
                 }
                 // Resolve target path
                 File target = resolveTargetPath( eachFile, "html", markupDirectory );
                 // Process Markdown
                 String input = FileUtils.fileRead( eachFile, "UTF-8" );
                 String output = md.markdown( input ).trim();
                 // Apply Theme
                 output = applyLayout( layout, output );
                 // Save to target path
                 FileUtils.fileWrite( target.getAbsolutePath(), "UTF-8", output );
 
                 getLog().info( "- Processed " + eachFile + " to " + target );
             }
         } catch ( IOException ex ) {
             throw new MojoExecutionException( "Unable to process markup content: " + ex.getMessage(), ex );
         }
     }
 
     private File resolveTargetPath( File file, String newExtension, File inputBaseDir )
     {
         String originalRelativePath = inputBaseDir.toURI().relativize( file.toURI() ).getPath();
         String targetRelativePath = originalRelativePath.replaceAll( "md$", newExtension );
         return new File( outputDirectory, targetRelativePath );
     }
 
     private String applyLayout( String layout, String htmlContent )
             throws IOException
     {
         return layout.replaceAll( "@@CONTENT@@", htmlContent );
     }
 
     /**
      * Accept only directories or files with the .md extension.
      */
     private static class MarkdownFileFilter
             implements FileFilter
     {
 
         @Override
         public boolean accept( File file )
         {
             return file.isDirectory() || file.getName().endsWith( ".md" );
         }
 
     }
 
     private static boolean directoryIsEmpty( File directory )
     {
         File[] files = directory.listFiles();
         return files == null || files.length == 0;
     }
 
 }
