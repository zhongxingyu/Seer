 /*
  * Author: David Corbin
  *
  * Copyright (c) 2005 RubyPeople.
  *
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
  * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
  * RDT except in compliance with the License. For further information see 
  * org.rubypeople.rdt/rdt.license.
  */
 package org.rubypeople.rdt.internal.core.builder;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.jruby.ast.Node;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 
 public class MassIndexUpdater {
 
     private final IndexUpdater updater;
     private final RubyParser parser;
 
     public MassIndexUpdater(IndexUpdater updater, RubyParser parser) {
         this.updater = updater;
         this.parser = parser;
     }
 
     public MassIndexUpdater(IndexUpdater indexUpdater) {
         this(indexUpdater, new RubyParser());
     }
 
     public void updateProjects(List projects, IProgressMonitor monitor) {
         try {
             List files = findFiles(projects);
             monitor.beginTask("Update symbol index", files.size());
             processFiles(files, monitor);
         } catch (CoreException e) {
             RubyCore.log(e);
         }
     }
 
     private void processFiles(List files, IProgressMonitor monitor) {
         for (Iterator iter = files.iterator(); iter.hasNext();) {
             IFile file = (IFile) iter.next();
             monitor.subTask(file.getFullPath().toString());
             monitor.worked(1);
             processFile(file);
         }
     }
 
     private void processFile(IFile file) {
         try {
             Node node = parser.parse(file);
             updater.update(file, node, false);
         } catch (CoreException e) {
             RubyCore.log(e);
         } catch (SyntaxException se) {
        	RubyCore.log("Explicit catch of SyntaxError in MassIndexUpdater (jpm)");
         	RubyCore.log(se);
         } catch (Exception ex) {
         	// e.g: the parser currently throws a ClassCastExcpetion when parsing xmldecl.rb
         	RubyCore.log(ex);
         }
     }
 
     private List findFiles(List projects) throws CoreException {
         List files = new ArrayList();
         RubySourceFileCollectingVisitor visitor = new RubySourceFileCollectingVisitor(files);
         for (Iterator iter = projects.iterator(); iter.hasNext();) {
             IProject project = (IProject) iter.next();
             project.accept(visitor, 0);
         }
         return files;
     }
     
     public boolean equals(Object obj) {
         if (!(obj instanceof  MassIndexUpdater))
             return false;
         
         MassIndexUpdater that = (MassIndexUpdater) obj;
         return updater.getClass().equals(that.updater.getClass())
             && parser.getClass().equals(that.parser);
     }
     
     public int hashCode() {
         return 0;
     }
 
 }
