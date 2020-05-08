 /**
  * Copyright (c) 2013 Martin M Reed
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hardisonbrewing.maven.cxx.msbuild;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.IOUtil;
import org.hardisonbrewing.maven.core.FileUtils;
 import org.hardisonbrewing.maven.core.JoJoMojoImpl;
 
 /**
  * @goal msbuild-assembly-info
  * @phase process-sources
  */
 public final class AssemblyInfoMojo extends JoJoMojoImpl {
 
     /**
      * @parameter
      */
     public String project;
 
     /**
      * @parameter default-value="${project.version}"
      */
     public String assemblyVersion;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
 
         File assemblyInfoFile = MSBuildService.getAssemblyInfoFile( project );
         File assemblyInfoBakFile = TargetDirectoryService.getAssemblyInfoBakFile();
         File assemblyInfoGenFile = TargetDirectoryService.getAssemblyInfoGenFile();
 
         BufferedReader reader = null;
         Writer writer = null;
 
         try {
 
             reader = new BufferedReader( new FileReader( assemblyInfoFile ) );
             writer = new FileWriter( assemblyInfoGenFile );
 
             Pattern assemblyVersionPattern = getAssemblyRegex( "AssemblyVersion" );
             Pattern assemblyFileVersionPattern = getAssemblyRegex( "AssemblyFileVersion" );
 
            assemblyVersion = assemblyVersion.replace( "-SNAPSHOT", ".*" );
 
             String line;
             while (( line = reader.readLine() ) != null) {
                 line = updateVersion( line, assemblyVersion, assemblyVersionPattern, assemblyFileVersionPattern );
                 writer.write( line );
                 writer.write( "\r\n" );
             }
 
             FileUtils.copyFile( assemblyInfoFile, assemblyInfoBakFile );
             FileUtils.copyFile( assemblyInfoGenFile, assemblyInfoFile );
         }
         catch (Exception e) {
             getLog().error( "Unable to update assembly info: " + assemblyInfoFile );
             throw new IllegalStateException();
         }
         finally {
             IOUtil.close( reader );
             IOUtil.close( writer );
         }
     }
 
     private String updateVersion( String line, String replacement, Pattern... patterns ) {
 
         for (Pattern pattern : patterns) {
             Matcher matcher = pattern.matcher( line );
             if ( matcher.matches() ) {
                 StringBuffer stringBuffer = new StringBuffer();
                 stringBuffer.append( matcher.group( 1 ) );
                 stringBuffer.append( replacement );
                 stringBuffer.append( matcher.group( 2 ) );
                 return stringBuffer.toString();
             }
         }
 
         return line;
     }
 
     private Pattern getAssemblyRegex( String key ) {
 
         return Pattern.compile( "(\\s*\\[\\s*assembly\\s*\\:\\s*" + key + "\\s*\\(\\s*\").+(\"\\s*\\)\\s*\\]\\s*)" );
     }
 }
