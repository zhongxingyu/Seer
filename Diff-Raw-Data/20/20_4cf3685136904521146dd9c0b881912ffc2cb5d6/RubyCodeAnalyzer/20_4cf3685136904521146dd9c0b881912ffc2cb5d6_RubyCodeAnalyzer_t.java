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
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.jruby.ast.Node;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.core.parser.ImmediateWarnings;
 import org.rubypeople.rdt.internal.core.parser.RubyLintVisitor;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 
 public final class RubyCodeAnalyzer implements SingleFileCompiler {
     private final IMarkerManager markerManager;
     private RubyParser parser;
     private final IndexUpdater indexUpdater;
 
     public RubyCodeAnalyzer(IMarkerManager markerManager) {
         this(markerManager, new RubyParser(new ImmediateWarnings(markerManager)), new IndexUpdater((RubyCore.getPlugin()).getSymbolIndex()));
     }
     
     public RubyCodeAnalyzer(IMarkerManager markerManager, RubyParser parser, IndexUpdater indexUpdater) {
         this.markerManager = markerManager;
         this.parser = parser;
         this.indexUpdater = indexUpdater;
     }
 
     public void compileFile(IFile file) throws CoreException {
         Reader reader = null;
 		try {
 			reader = new InputStreamReader(file.getContents(), file.getCharset());
 		} catch (UnsupportedEncodingException e1) {
 			RubyCore.log(e1);
 			return;
 		}
         String contents = readContents(reader);
         markerManager.removeProblemsAndTasksFor(file);
         try {
            Node rootNode = parser.parse(file, new StringReader(contents));
             if (rootNode == null) return;            
 			RubyLintVisitor visitor = new RubyLintVisitor(contents, new ProblemRequestorMarkerManager(file, markerManager));
             rootNode.accept(visitor);
             indexUpdater.update(file, rootNode, true);
         } catch (SyntaxException e) {
             // Should we really put a marker here? I think the normal parsing process will create syntax markers just fine
            markerManager.createSyntaxError(file, e);
         } finally {
             IoUtils.closeQuietly(reader);
         }
     }
 
 	private String readContents(Reader reader) {
 		try {
 			int c = 0;
 			StringBuffer str = new StringBuffer();
 			while ((c = reader.read()) != -1) {
 				str.append((char) c);
 			}
 			return str.toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "";
 		}
 	}
 
 }
