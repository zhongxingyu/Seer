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
 
 import java.io.File;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
import org.hardisonbrewing.maven.core.FileUtils;
 import org.hardisonbrewing.maven.core.JoJoMojoImpl;
 
 /**
  * @goal msbuild-revert-assembly-info
  * @phase compile
  */
 public final class RevertAssemblyInfoMojo extends JoJoMojoImpl {
 
     /**
      * @parameter
      */
     public String project;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
 
         File assemblyInfoFile = MSBuildService.getAssemblyInfoFile( project );
         File assemblyInfoBakFile = TargetDirectoryService.getAssemblyInfoBakFile();
 
         try {
             FileUtils.copyFile( assemblyInfoBakFile, assemblyInfoFile );
         }
         catch (Exception e) {
             getLog().error( "Unable to revert assembly info: " + assemblyInfoFile );
             throw new IllegalStateException();
         }
     }
 }
