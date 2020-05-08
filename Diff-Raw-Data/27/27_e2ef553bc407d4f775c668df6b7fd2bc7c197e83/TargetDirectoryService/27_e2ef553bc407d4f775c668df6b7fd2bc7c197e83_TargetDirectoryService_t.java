 /**
  * Copyright (c) 2010-2011 Martin M Reed
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
 package org.hardisonbrewing.maven.core;
 
 import java.io.File;
 
 import org.apache.maven.project.MavenProject;
 
 public class TargetDirectoryService {
 
     protected TargetDirectoryService() {
 
         // do nothing
     }
 
     public static final void ensureTargetDirectoryExists() {
 
         File file = getTargetDirectory();
         if ( !file.exists() ) {
             file.mkdirs();
         }
     }
 
     /**
      * Return a {@link File} that represents the target directory.
      * @return
      */
     public static final File getTargetDirectory() {
 
        return ProjectService.getBuildDirectory();
     }
 
     /**
      * Return the path to the target directory.
      * @return
      */
     public static final String getTargetDirectoryPath() {
 
        return ProjectService.getBuildDirectoryPath();
     }
 
     public static final String resolveTargetFilePath( String filePath ) {
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( getTargetDirectoryPath() );
         stringBuffer.append( File.separator );
         stringBuffer.append( FileUtils.getTargetCanonicalPath( filePath ) );
         return stringBuffer.toString();
     }
 
     public static final String getTempPackagePath() {
 
         MavenProject project = ProjectService.getProject();
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( getTargetDirectoryPath() );
         stringBuffer.append( File.separator );
         stringBuffer.append( project.getBuild().getFinalName() );
         return stringBuffer.toString();
     }
 }
