 package com.kryshchuk.maven.plugins.umlet;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 import com.baselet.diagram.DiagramHandler;
 
 /**
  * Converts the UMLet UXF diagrams into the graphics files.
  * 
  * @since 1.0.0
  */
 @Mojo(name = "convert", defaultPhase = LifecyclePhase.PRE_SITE, threadSafe = true)
 public class ConvertDiagramMojo extends AbstractMojo {
 
   private static final FileFilter DIAGRAM_FILEFILTER = new FileFilter() {
 
    /**
     * @return
     */
     public boolean accept(final File f) {
       if (f.isDirectory()) {
         return true;
       }
       if (f.getName().toLowerCase().endsWith(".uxf")) {
         return true;
       }
       return false;
     }
 
   };
 
   /**
    * Target directory to store the converted images. If the source directory contains the sub-directories with diagram
    * sources the generated image files will be created in the same sub-directories under the target directory. For
    * example, the diagram <code>sourceDirectory/overview.uxf</code> will be converted into
    * <code>outputDirectory/overview.png</code>, and the directory <code>sourceDirectory/sub1/sub2/details.uxf</code>
    * will be converted into <code>outputDirectory/sub1/sub2/details.png</code>
    * 
    * @since 1.0.0
    */
   @Parameter(defaultValue = "${project.reporting.outputDirectory}/uml", property = "umlet.targetDir", required = true)
   private File outputDirectory;
 
   /**
    * Root source directory with UMLet UXF diagrams. Sub-directories will be scanned for the diagram sources as well.
    * 
    * @since 1.0.0
    */
   @Parameter(defaultValue = "${basedir}/src/site/resources/uml", property = "umlet.sourceDir", required = true)
   private File sourceDirectory;
 
   /**
    * Convert diagram to this image format. Supported formats are <code>bmp</code>, <code>eps</code>, <code>gif</code>,
    * <code>jpg</code>, <code>pdf</code>, <code>png</code> and <code>svg</code>.
    * 
    * @since 1.0.0
    */
   @Parameter(defaultValue = "png", property = "umlet.format", required = true)
   private ConvertFormat format;
 
  /**
   * <p>
   * execute.
   * </p>
   * 
   * @throws org.apache.maven.plugin.MojoExecutionException
   *           if any.
   * @throws org.apache.maven.plugin.MojoFailureException
   *           if any.
   */
   public void execute() throws MojoExecutionException, MojoFailureException {
     getLog().debug("Diagram directory: " + sourceDirectory);
     getLog().debug("Output directory: " + outputDirectory);
     getLog().debug("Format: " + format);
     if (sourceDirectory.isDirectory()) {
       processDirectory(sourceDirectory);
     } else {
       getLog().warn("Diagram directory does not exist: " + sourceDirectory);
     }
   }
 
   private void processDirectory(final File dir) throws MojoExecutionException, MojoFailureException {
     final File[] files = sourceDirectory.listFiles(DIAGRAM_FILEFILTER);
     for (final File f : files) {
       if (f.isDirectory()) {
         processDirectory(f);
       } else {
         processDiagram(f);
       }
     }
   }
 
   private void processDiagram(final File diagram) throws MojoExecutionException, MojoFailureException {
     final String diagramFilepath = diagram.getAbsolutePath();
     final String sourceDirpath = sourceDirectory.getAbsolutePath();
     if (diagramFilepath.startsWith(sourceDirpath)) {
       final String relPath = diagramFilepath.substring(sourceDirpath.length());
       final int extIndex = relPath.lastIndexOf('.');
       final String targetRel = relPath.substring(0, extIndex) + "." + format;
       getLog().debug("Found source diagram " + targetRel);
       final File imageFile = new File(outputDirectory, targetRel);
       if (!imageFile.isFile() || diagram.lastModified() > imageFile.lastModified()) {
         checkOutputDirectory(imageFile.getParentFile());
         final DiagramHandler diagramHandler = new DiagramHandler(diagram);
         try {
           getLog().info("Converting " + diagram);
           diagramHandler.getFileHandler().doExportAs(format.name(), imageFile);
         } catch (final IOException e) {
           getLog().error("Failed to convert diagram", e);
           throw new MojoFailureException(this, "Cannot convert diagram " + diagram.getAbsolutePath(), e.getMessage());
         }
       } else {
         getLog().debug("Diagram image is up to date: " + imageFile.getAbsolutePath());
       }
     } else {
       throw new MojoExecutionException("Could not determine target image path");
     }
   }
 
   private void checkOutputDirectory(final File dir) throws MojoFailureException {
     if (dir.isDirectory()) {
       getLog().debug("Output directory exists: " + dir.getAbsolutePath());
     } else {
       if (dir.exists()) {
         throw new MojoFailureException("Output destination exists, but is not a directory");
       } else {
         if (dir.mkdirs()) {
           getLog().debug("Created output directory: " + dir.getAbsolutePath());
         } else {
           throw new MojoFailureException("Could not create output directory");
         }
       }
     }
   }
 
 }
