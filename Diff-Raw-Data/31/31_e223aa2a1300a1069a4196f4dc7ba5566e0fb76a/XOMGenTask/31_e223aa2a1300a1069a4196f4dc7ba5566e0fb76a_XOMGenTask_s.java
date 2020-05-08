 /*
 // Licensed to Julian Hyde under one or more contributor license
 // agreements. See the NOTICE file distributed with this work for
 // additional information regarding copyright ownership.
 //
 // Julian Hyde licenses this file to you under the Apache License,
 // Version 2.0 (the "License"); you may not use this file except in
 // compliance with the License. You may obtain a copy of the License at:
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 */
 package org.eigenbase.xom;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * <code>XOMGenTask</code> is an ANT task with which to invoke {@link
  * MetaGenerator}.
  *
  * @author jhyde
  *
 * <hr/>
  *
  * <h2><a name="XOMGen">XOMGen</a></h2>
  * <h3>Description</h3>
  * <p>
  *   Invokes the {@link MetaGenerator}.
  * </p>
  * <p>
  *   This task only invokes XOMGen if the grammar file is newer than the
  *   generated Java files.
  * </p>
  *
  * <h3>Parameters</h3>
 * <table border="1" cellpadding="2" cellspacing="0">
  *   <tr>
  *     <td valign="top"><b>Attribute</b></td>
  *     <td valign="top"><b>Description</b></td>
  *     <td align="center" valign="top"><b>Required</b></td>
  *   </tr>
  *   <tr>
  *     <td valign="top"><a name="model">model</a></td>
  *     <td valign="top">The name of the XML file which holds the XOM
  *       model.</td>
  *     <td valign="top" align="center">Yes</td>
  *   </tr>
  *   <tr>
  *     <td valign="top"><a name="destdir">destdir</a></td>
  *     <td valign="top">The name of the output directory. Default is the
  *       current directory.</td>
  *     <td valign="top" align="center">No</td>
  *   </tr>
  *   <tr>
  *     <td valign="top"><a name="classname">classname</a></td>
  *     <td valign="top">The full name of the class to generate.</td>
  *     <td valign="top" align="center">Yes</td>
  *   </tr>
  *   <tr>
  *     <td valign="top"><a name="dtdname">dtdname</a></td>
  *     <td valign="top">The name of the DTD file to generate. The path may be
  *       either absolute, or relative to <code>destdir</code>.</td>
  *     <td valign="top" align="center">Yes</td>
  *   </tr>
  * </table>
  *
  * <h3>Example</h3>
  * <blockquote><pre>&lt;xomgen
  *     model=&quot;src/org/eigenbase/xom/Meta.xml&quot;
  *     destdir=&quot;src&quot;
  *     classname=&quot;org.eigenbase.xom.MetaDef&quot;/&gt;</pre></blockquote>
  * <p>
  *   This invokes XOMGen on the model file
  *   <code>src/org/eigenbase/xom/Meta.xml</code>, and generates
  *   <code>src/org/eigenbase/xom/MetaDef.java</code> and
  *   <code>src/org/eigenbase/xom/meta.dtd</code>.
  * </p>
  *
 * <hr/>
  */
 public class XOMGenTask extends Task {
     String modelFileName;
     String destDir;
     String dtdFileName;
     String className;
 
     public XOMGenTask()
     {}
 
     public void execute() throws BuildException {
         try {
             if (modelFileName == null) {
                 throw new BuildException("You must specify model.");
             }
             if (className == null) {
                 throw new BuildException("You must specify className.");
             }
 
             File projectBase = getProject().getBaseDir();
             File destinationDirectory;
             if (destDir == null) {
                 destinationDirectory = projectBase;
             } else {
                 destinationDirectory = new File(projectBase, destDir);
             }
             if (!destinationDirectory.exists()) {
                 throw new BuildException(
                     "Destination directory doesn't exist: " +
                         destinationDirectory.toString());
             }
 
             File modelFile = new File(projectBase, modelFileName);
             File classFile = classNameToFile(destinationDirectory, className);
             File outputDir = classFile.getParentFile();
             File dtdFile = new File(outputDir, dtdFileName);
 
             if (modelFile.exists() &&
                 classFile.exists() &&
                 dtdFile.exists()) {
                 long modelStamp = modelFile.lastModified(),
                     classStamp = classFile.lastModified(),
                     dtdStamp = dtdFile.lastModified();
                 if (classStamp > modelStamp &&
                     dtdStamp > modelStamp) {
                     // files are up to date
                     return;
                 }
             }
 
             final boolean testMode = false;
             MetaGenerator generator = new MetaGenerator(
                 modelFile.toString(), testMode, className);
             generator.writeFiles(destinationDirectory.toString(), dtdFileName);
             generator.writeOutputs();
         } catch (XOMException e) {
             throw new BuildException("Generation of model failed: " + e);
         } catch (IOException e) {
             throw new BuildException("Generation of model failed: " + e);
         }
     }
 
     // ------------------------------------------------------------------------
     // ANT attribute methods
 
     /** See parameter <code><a href="#model">model</a></code>. */
     public void setModel(String model) {
         this.modelFileName = model;
     }
 
     /** See parameter <code><a href="#destdir">destdir</a></code>. */
     public void setDestdir(String destdir) {
         this.destDir = destdir;
     }
 
     /** See parameter <code><a href="#classname">classname</a></code>. */
     public void setClassname(String classname) {
         this.className = classname;
     }
 
     /** See parameter <code><a href="#dtdname">dtdname</a></code>. */
     public void setDtdname(String dtdname) {
         this.dtdFileName = dtdname;
     }
 
     // ------------------------------------------------------------------------
 
     /**
      * Creates the File that a java class will live in. For example,
      * <code>makeJavaFileName("com.myproj", "MyClass")</code> returns
      * "com/myproj/MyClass.java".
      */
     static File classNameToFile(File dir, String className) {
         char fileSep = System.getProperty("file.separator").charAt(0); // e.g. '/'
 
         String relativePath = className.replace('.', fileSep) + ".java";
 
         if (dir == null) {
             return new File(relativePath);
         } else {
             return new File(dir, relativePath);
         }
     }
 
 }
 
 
 // End XOMGenTask.java
