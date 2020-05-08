 /*
  * UMLet Maven Plugin
 * Copyright (C) 2013 Yuriy Kryshchuk
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses />.
  */
 package com.kryshchuk.maven.plugins.umlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 import org.slf4j.impl.StaticLoggerBinder;
 
 import com.baselet.control.Constants.Program;
 import com.baselet.control.Constants.RuntimeType;
 import com.baselet.diagram.DiagramHandler;
 import com.kryshchuk.maven.plugins.filevisitor.AbstractFileVisitor;
 import com.kryshchuk.maven.plugins.filevisitor.FileMapper;
 import com.kryshchuk.maven.plugins.filevisitor.FileSet;
 import com.kryshchuk.maven.plugins.filevisitor.FileSetIterator;
 import com.kryshchuk.maven.plugins.filevisitor.FileVisitor;
 import com.kryshchuk.maven.plugins.filevisitor.ReplaceExtensionFileMapper;
 import com.kryshchuk.maven.plugins.filevisitor.VisitorException;
 
 /**
  * Converts the UMLet UXF diagrams into the graphics files.
  * 
  * @since 1.0.0
  */
 @Mojo(name = "convert", defaultPhase = LifecyclePhase.PRE_SITE, threadSafe = true, requiresProject = true)
 public class ConvertDiagramMojo extends AbstractMojo {
 
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
 
   @Parameter(required = false)
   private List<FileSet> filesets;
 
   /**
    * Convert diagram to this image format. Supported formats are <code>bmp</code>, <code>eps</code>, <code>gif</code>,
    * <code>jpg</code>, <code>pdf</code>, <code>png</code> and <code>svg</code>.
    * 
    * @since 1.0.0
    */
   @Parameter(defaultValue = "png", property = "umlet.format", required = true)
   private ConvertFormat format;
 
   @Parameter(defaultValue = "${project}", required = true, readonly = true)
   private MavenProject mavenProject;
 
   private final FileVisitor visitor = new AbstractFileVisitor() {
 
     @Override
     protected boolean shouldOverwrite(final File inputFile, final File outputFile) {
       return false;
     }
 
     @Override
     protected void handleFile(final File inputFile, final File outputFile) throws VisitorException {
       verifyParentDirectory(outputFile);
       Program.RUNTIME_TYPE = RuntimeType.BATCH;
       final DiagramHandler diagramHandler = new DiagramHandler(inputFile);
       try {
         getLog().info("Converting " + inputFile);
         diagramHandler.getFileHandler().doExportAs(format.name(), outputFile);
       } catch (final IOException e) {
         getLog().error("Failed to convert diagram", e);
         throw new VisitorException("Cannot convert diagram " + inputFile, e);
       }
     }
   };
 
   /**
    * <p>
    * execute.
    * </p>
    * 
    * @throws org.apache.maven.plugin.MojoExecutionException
    *           if any.
    * @throws org.apache.maven.plugin.MojoFailureException
    *           if any.
    * @since 1.0.8
    */
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     StaticLoggerBinder.getSingleton().setLog(getLog());
     if (filesets == null || filesets.isEmpty()) {
       getLog().info("Using default diagrams location");
       processFileSet(new DefaultDiagramSet());
     } else {
       for (final FileSet fs : filesets) {
         processFileSet(fs);
       }
     }
   }
 
   /**
    * @param fileSet
    *          fileset to process
    * @throws MojoExecutionException
    */
   private void processFileSet(final FileSet fileSet) throws MojoExecutionException {
     final FileMapper fileMapper = new ReplaceExtensionFileMapper(fileSet.getDirectory(), outputDirectory, format.name());
     final FileSetIterator i = new FileSetIterator(fileSet, fileMapper);
     try {
       i.iterate(visitor);
     } catch (final VisitorException e) {
       throw new MojoExecutionException("Failed to iterate fileset", e);
     }
   }
 
   private class DefaultDiagramSet extends FileSet {
 
     /**
      * 
      */
     DefaultDiagramSet() {
       final File diagramsDirectory = new File(mavenProject.getBasedir(), "src/site/resources/uml");
       setDirectory(diagramsDirectory);
       setIncludes(Arrays.asList("**/*.uxf"));
     }
   }
 }
