 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: SourceMerger.java,v 1.14 2011/08/25 12:34:30 mtaal Exp $
  */
 package org.eclipse.emf.texo.generator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 import org.eclipse.emf.codegen.merge.java.JControlModel;
 import org.eclipse.emf.codegen.merge.java.JMerger;
 import org.eclipse.emf.codegen.merge.java.facade.ast.ASTFacadeHelper;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticException;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.texo.utils.Check;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.ToolFactory;
 import org.eclipse.xpand2.output.FileHandle;
 
 /**
  * Receives the java output of a generate action and the target location. Reads the current source from there and merges
  * the generation output and the current content. Also takes care of resolving and organizing imports.
  * 
  * @see ImportResolver
  * @see EclipseGeneratorUtils
  * @see JMerger
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class SourceMerger extends MergingOutputHandler {
 
   private JControlModel jControlModel;
 
   private Object codeFormatter;
 
   private IJavaProject javaProject;
 
   /**
    * Does the merge operation and returns the new content if the content has really changed, otherwise null is returned.
    */
   @Override
   protected void merge(final FileHandle fileHandle) {
     final String targetLocation = fileHandle.getAbsolutePath();
     // final String targetLocation = fileHandle.getAbsolutePath();
     final File targetFile = new File(targetLocation);
 
     Check.isNotNull(targetFile, "Targetfile is null, for outlet " //$NON-NLS-1$
         + fileHandle.getOutlet().getPath());
     final String generatedSource = fileHandle.getBuffer().toString();
     try {
       // if exists merge with it
       if (targetFile.exists()) {
         mergeImportAndFormat(fileHandle, targetFile);
         return;
       }
 
       // does not yet exist do the basic things
 
       // resolve imports
       String source = organizeImports(targetLocation, generatedSource);
       // and format
       source = EclipseGeneratorUtils.formatSource(source, getCodeFormatter());
       fileHandle.setBuffer(source);
     } catch (final IllegalStateException c) {
       throw c; // rethrow to prevent to many exceptions
     } catch (final Exception e) {
       // catch them all
       throw new IllegalStateException("Exception while merging and saving source file in sourcemerger " //$NON-NLS-1$
           + targetLocation + " " + e.getMessage() + " " + e + "\n" + generatedSource, e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     }
   }
 
   private void mergeImportAndFormat(final FileHandle fileHandle, final File targetFile) throws Exception {
     final String targetLocation = targetFile.getAbsolutePath();
 
     final JControlModel localJControlModel = getJControlModel();
     final JMerger jMerger = new JMerger(localJControlModel);
     jMerger.setFixInterfaceBrace(localJControlModel.getFacadeHelper().fixInterfaceBrace());
 
     final String generatedSource = fileHandle.getBuffer().toString();
     final String source = organizeImports(targetLocation, generatedSource);
     try {
       jMerger.setSourceCompilationUnit(jMerger.createCompilationUnitForContents(source));
     } catch (final WrappedException e) { // something wrong in the code
       // itself
       throw new IllegalStateException("Syntax error in generated source for " + targetLocation //$NON-NLS-1$
           + " :" + getExceptionMessage(e) + "\nSource>>>>>>>>>>>>>>>>>>>>>>>>>\n" + source, e); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     final InputStream is = new FileInputStream(targetFile);
     String newSource = ""; //$NON-NLS-1$
     int location = 0;
     try {
      jMerger.setTargetCompilationUnit(jMerger.createCompilationUnitForInputStream(is));
       location = 1;
       jMerger.merge();
       location = 2;
       newSource = jMerger.getTargetCompilationUnitContents();
 
       // again organize imports after the merge
       location = 3;
       newSource = organizeImports(targetLocation, newSource);
       location = 4;
       newSource = EclipseGeneratorUtils.formatSource(newSource, getCodeFormatter());
 
       // TODO: check if target is read only!
       jControlModel.getFacadeHelper().reset();
 
       location = 5;
 
       fileHandle.setBuffer(newSource);
 
     } catch (final WrappedException e) { // something wrong in the code
       // itself
       throw new IllegalStateException(
           "Syntax error in current source for " + targetLocation //$NON-NLS-1$
               + " :" + getExceptionMessage(e) + " location " + location + " old source \n" + source + " new source \n" + newSource, e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
     } catch (final Throwable t) {
       throw new IllegalStateException(
           "Throwable caught for current source for " + targetLocation //$NON-NLS-1$
               + " :" + getExceptionMessage(t) + " location " + location + " old source \n" + source + " new source \n" + newSource, t); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
     } finally {
       is.close();
     }
   }
 
   private String organizeImports(final String location, final String source) throws Exception {
     final ImportResolver importResolver = new ImportResolver();
     importResolver.setJavaProject(javaProject);
     importResolver.setSource(source);
     final String resolvedSource = importResolver.resolve();
     return resolvedSource;
   }
 
   private String getExceptionMessage(final Throwable t) {
     if (t.getCause() instanceof DiagnosticException) {
       final DiagnosticException d = (DiagnosticException) t.getCause();
       final StringBuilder message = new StringBuilder(d.getDiagnostic().getMessage());
       for (final Diagnostic cd : d.getDiagnostic().getChildren()) {
         message.append("\n\t").append(cd.getMessage()); //$NON-NLS-1$
       }
       return message.toString();
     }
     return t.getMessage();
   }
 
   private JControlModel getJControlModel() {
     if (jControlModel == null) {
       jControlModel = new JControlModel();
       jControlModel.initialize(new ASTFacadeHelper(), this.getClass().getResource("texo-merge.xml").toExternalForm()); //$NON-NLS-1$
     }
     return jControlModel;
   }
 
   private Object getCodeFormatter() {
     if (codeFormatter == null) {
       codeFormatter = ToolFactory.createCodeFormatter(javaProject.getOptions(true));
     }
     return codeFormatter;
   }
 
   @Override
   protected String[] getSupportedExtensions() {
     return new String[] { ".java" }; //$NON-NLS-1$
   }
 
   @Override
   public void setProjectName(final String projectName) {
     super.setProjectName(projectName);
     javaProject = EclipseGeneratorUtils.getJavaProject(projectName);
   }
 
 }
