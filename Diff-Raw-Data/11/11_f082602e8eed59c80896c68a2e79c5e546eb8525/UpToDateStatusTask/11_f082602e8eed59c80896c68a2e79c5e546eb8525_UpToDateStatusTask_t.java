 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.editor.semantic;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.StyledDocument;
 import javax.tools.Diagnostic;
 import javax.tools.JavaFileObject;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CancellableTask;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
 import org.netbeans.spi.editor.hints.ErrorDescription;
 import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
 import org.netbeans.spi.editor.hints.HintsController;
 import org.netbeans.spi.editor.hints.Severity;
 import org.openide.filesystems.FileObject;
 import org.openide.text.NbDocument;
 /**
  *
  * @author David Strupl
  */
 class UpToDateStatusTask implements CancellableTask<CompilationInfo> {
     
     private static final Logger LOGGER = Logger.getLogger(UpToDateStatusTask.class.getName());
     private static final boolean LOGGABLE = LOGGER.isLoggable(Level.FINE);
     
     private AtomicBoolean cancel = new AtomicBoolean();
 
     UpToDateStatusTask(FileObject file) {
     }
     
     private UpToDateStatusTask() {
     }
     
     public void cancel() {
         cancel.set(true);
     }
 
     public void run(CompilationInfo info) {
         cancel.set(false);
         process(info);
     }
 
     private void process(CompilationInfo info) {
         if (LOGGABLE) log("process: " + info.getJavaFXSource().getFileObject());
         
         Document doc = info.getJavaFXSource().getDocument();
         if (doc == null) {
             if (LOGGABLE) log("  no document for: " + info.getJavaFXSource());
             return;
         }
 
         List<Diagnostic> diag = info.getDiagnostics();
 
         ArrayList<ErrorDescription> c = new ArrayList<ErrorDescription>();
 
         for (Diagnostic d : diag) {
             if (LOGGABLE) log("    diagnostics: " + d);
             if (d.getSource() instanceof JavaFileObject) {
                 JavaFileObject jfo = (JavaFileObject)d.getSource();
                 if (! jfo.getName().equals(info.getJavaFXSource().getFileObject().getNameExt())) {
                     if (LOGGABLE) log("    in different file: " + jfo.getName() + " vs.: " + info.getJavaFXSource().getFileObject().getNameExt());
                     continue;
                 }
             } else {
                 if (LOGGABLE) log("    source is not JavaFileObject but: " + (d.getSource() != null ? d.getSource().getClass().getName() : "null"));
             }
             long start = d.getStartPosition();
             long end = d.getEndPosition();
             if (start != Diagnostic.NOPOS && end != Diagnostic.NOPOS) {
                 if (LOGGABLE) log("    start == " + start + "  end == " + end);
                 if (start == end) {
                     end = skipWhiteSpace(info, (int)start);
                     if (LOGGABLE) log("  after skip  start == " + start + "  end == " + end);
                 }
                 try {
                     c.add(ErrorDescriptionFactory.createErrorDescription(
                         Severity.ERROR,
                         d.getMessage(Locale.getDefault()),
                         doc,
                         doc.createPosition((int) start),
                         doc.createPosition((int) end)
                     ));
                     continue;
                 } catch (BadLocationException ex) {
                     if (LOGGABLE) {
                         LOGGER.log(Level.INFO, "Problem with error underlining", ex);
                     }
                 }
             } 
             // let's use the line number
             int lastLine = NbDocument.findLineNumber((StyledDocument)doc, doc.getEndPosition().getOffset());
             long linu = d.getLineNumber();
             if (LOGGABLE) log("    lastLine == " + lastLine + " linu == " + linu);
            if ( (linu>0) && (linu-1 <= lastLine)) {
                 c.add(ErrorDescriptionFactory.createErrorDescription(
                     Severity.ERROR, d.getMessage(Locale.getDefault()),
                    doc,(int)linu));
             } else {
                 if (LOGGABLE) log("   NOT USED (wrong bounds): " + d);
             }
         }
         HintsController.setErrors(doc, "semantic-highlighter", c);
 
         UpToDateStatusProviderImpl p = UpToDateStatusProviderImpl.forDocument(doc);
         p.refresh(diag, UpToDateStatus.UP_TO_DATE_OK);
     }
 
     private int skipWhiteSpace(CompilationInfo info, int start) {
         TokenSequence<JFXTokenId> ts =  ((TokenHierarchy<?>) info.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         ts.move(start);
         boolean nonWSFound = false;
         while (ts.moveNext()) {
             if (ts.token().id() != JFXTokenId.WS) {
                 nonWSFound = true;
                 break;
             }
         }
         if (!nonWSFound) {
             if (LOGGABLE) log("NOT skipping the WS because we are at the very end");
             return start;
         }
         int res = ts.offset();
         if (res > start && res < info.getJavaFXSource().getDocument().getLength()) {
             if (LOGGABLE) log("skipping the whitespace start == " + start + "  res == " + res);
             return res;
         } else {
             if (LOGGABLE) log("NOT skipping the whitespace start == " + start + "  res == " + res);
             return start;
         }
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             LOGGER.fine(s);
         }
     }
 }
