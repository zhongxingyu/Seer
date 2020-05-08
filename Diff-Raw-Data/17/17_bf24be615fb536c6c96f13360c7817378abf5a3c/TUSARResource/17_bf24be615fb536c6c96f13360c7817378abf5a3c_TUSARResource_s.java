 /*******************************************************************************
 
  * Copyright (c) 2010 Thales Corporate Services SAS                             *
  *                                                                              *
  * Permission is hereby granted, free of charge, to any person obtaining a copy *
  * of this software and associated documentation files (the "Software"), to deal*
  * in the Software without restriction, including without limitation the rights *
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
  * copies of the Software, and to permit persons to whom the Software is        *
  * furnished to do so, subject to the following conditions:                     *
  *                                                                              *
  * The above copyright notice and this permission notice shall be included in   *
  * all copies or substantial portions of the Software.                          *
  *                                                                              *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
  * THE SOFTWARE.                                                                *
  *******************************************************************************/
 
 package com.thalesgroup.sonar.plugins.tusar;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.sonar.api.resources.*;
 import org.sonar.api.utils.WildcardPattern;
 
 import java.util.List;
 
 /**
  * This class is an implementation of a resource of type TUSARRessource
  * (similar to FILE, but with unit test implementation)
  */
 public class TUSARResource extends Resource<Directory> {
 
     private String directoryKey;
     private String filename;
     private Language language;
     private Directory parent;
     private boolean unitTest;
 
     /**
      * File in project. Key is the path relative to project source directories. It is not the absolute path
      * and it does not include the path to source directories. Example : <code>new File("org/sonar/foo.sql")</code>. The
      * absolute path may be c:/myproject/src/main/sql/org/sonar/foo.sql. Project root is c:/myproject and source dir
      * is src/main/sql.
      */
     public TUSARResource(String key, boolean unitTest) {
         if (key == null) {
             throw new IllegalArgumentException("File key is null");
         }
         String realKey = parseKey(key);
        if (realKey.indexOf(Directory.SEPARATOR) >= 0) {
             this.directoryKey = Directory.parseKey(StringUtils.substringBeforeLast(key, Directory.SEPARATOR));
             this.filename = StringUtils.substringAfterLast(realKey, Directory.SEPARATOR);
             realKey = new StringBuilder().append(this.directoryKey).append(Directory.SEPARATOR).append(filename).toString();
 
         } else {
             this.filename = key;
         }
         setKey(realKey);
         this.unitTest = unitTest;
     }
 
     public TUSARResource(String key) {
         this(key, false);
     }
 
     /**
      * Creates a file from its containing directory and name
      */
     public TUSARResource(String directory, String filename, boolean unitTest) {
         this.filename = StringUtils.trim(filename);
         if (StringUtils.isBlank(directory)) {
             setKey(filename);
 
         } else {
             this.directoryKey = Directory.parseKey(directory);
             setKey(new StringBuilder().append(directoryKey).append(Directory.SEPARATOR).append(this.filename).toString());
         }
         this.unitTest = unitTest;
     }
 
     /**
      * Creates a File from its language and its key
      */
     public TUSARResource(Language language, String key, boolean unitTest) {
         this(key, unitTest);
         this.language = language;
     }
 
     /**
      * Creates a File from language, directory and filename
      */
     public TUSARResource(Language language, String directory, String filename, boolean unitTest) {
         this(directory, filename, unitTest);
         this.language = language;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see Resource#getParent()
      */
     public Directory getParent() {
         if (parent == null) {
             parent = new Directory(directoryKey);
         }
         return parent;
     }
 
     private static String parseKey(String key) {
         if (StringUtils.isBlank(key)) {
             return null;
         }
 
         key = key.replace('\\', '/');
         key = StringUtils.trim(key);
         return key;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see Resource#matchFilePattern(String)
      */
     public boolean matchFilePattern(String antPattern) {
         WildcardPattern matcher = WildcardPattern.create(antPattern, "/");
         return matcher.match(getKey());
     }
 
     /**
      * Creates a File from an io.file and a list of sources directories
      */
     public static TUSARResource fromIOFile(java.io.File file, List<java.io.File> sourceDirs, boolean unitTest) {
         String relativePath = DefaultProjectFileSystem.getRelativePath(file, sourceDirs);
         if (relativePath != null) {
             return new TUSARResource(relativePath, unitTest);
         }
         return null;
     }
 
     /**
      * Creates a File from its name and a project
      */
     public static TUSARResource fromIOFile(java.io.File file, Project project, boolean unitTest) {
         return fromIOFile(file, project.getFileSystem().getSourceDirs(), unitTest);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see Resource#getName()
      */
     public String getName() {
         return filename;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see Resource#getLongName()
      */
     public String getLongName() {
         return getKey();
     }
 
     /**
      * {@inheritDoc}
      *
      * @see Resource#getDescription()
      */
     public String getDescription() {
         return null;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see Resource#getLanguage()
      */
     public Language getLanguage() {
         return language;
     }
 
     /**
      * Sets the language of the file
      */
     public void setLanguage(Language language) {
         this.language = language;
     }
 
     /**
      * @return SCOPE_ENTITY
      */
     public String getScope() {
         return Resource.SCOPE_ENTITY;
     }
 
     /**
      * @return QUALIFIER_UNIT_TEST_CLASS or QUALIFIER_FILE depending whether it is a unit test class
      */
     public String getQualifier() {
         return unitTest ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_FILE;
     }
 
     /**
      * @return whether the TUSARRessource is a unit test element or not
      */
     public boolean isUnitTest() {
         return unitTest;
     }
 
     public static TUSARResource fromAbsOrRelativePath(String resourcePath, Project project, boolean unitTest) {
 
         java.io.File file;
 
         // try in relative : take each source directory and try to create a file as {directory}/{file}
         // we do that because we need a java.io.File to instantiate the TUSARResource
         for (java.io.File sourceDir : project.getFileSystem().getSourceDirs()) {
             file = new java.io.File(sourceDir.getAbsolutePath(), resourcePath);
             if (file.exists()) {
                 return fromIOFile(file, project.getFileSystem().getSourceDirs(), unitTest);
             }
         }
 
         // relative path does not exists, try to create as absolute path,
         // but with the path matching the source directories
         file = new java.io.File(resourcePath);
         TUSARResource tusarResource = fromIOFile(file, project.getFileSystem().getSourceDirs(), unitTest);
         if (tusarResource != null) {
             return tusarResource;
         }
 
         // path does not exists in sources directories
         // neither as absolute nor as relative,
         // -> still create the resource, but this will be a "ghost" resource
         return new TUSARResource(resourcePath, unitTest);
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this)
                 .append("key", getKey())
                 .append("dir", directoryKey)
                 .append("filename", filename)
                 .append("language", language)
                 .append("unitTest", unitTest)
                 .toString();
     }
 }
