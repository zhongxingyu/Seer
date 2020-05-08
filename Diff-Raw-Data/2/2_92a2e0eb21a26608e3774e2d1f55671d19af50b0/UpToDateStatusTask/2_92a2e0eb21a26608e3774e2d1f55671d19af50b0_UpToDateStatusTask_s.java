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
 
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import java.io.IOException;
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
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.StyledDocument;
 import javax.tools.Diagnostic;
 import javax.tools.JavaFileObject;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.type.TypeKind;
 import javax.swing.text.Position;
 import org.netbeans.api.javafx.editor.Cancellable;
 import org.netbeans.api.javafx.editor.SafeTokenSequence;
 import org.netbeans.api.lexer.Token;
 import org.openide.cookies.LineCookie;
 import org.openide.loaders.DataObject;
 import org.openide.text.Line;
 import org.openide.util.NbBundle;
 /**
  *
  * @author David Strupl
  */
 class UpToDateStatusTask implements CancellableTask<CompilationInfo> {
     
     private static final Logger LOGGER = Logger.getLogger(UpToDateStatusTask.class.getName());
     private static final boolean LOGGABLE = LOGGER.isLoggable(Level.FINE);
 
     private static final Set<String> CANNOT_RESOLVE = new HashSet<String>(Arrays.asList(
             "compiler.err.cant.resolve", // NOI18N
             "compiler.err.cant.resolve.location", // NOI18N
             "compiler.err.cant.resolve.location.args", // NOI18N
             "compiler.err.doesnt.exist" // NOI18N
     ));
 
     private static final Set<String> UNDERLINE_IDENTIFIER = new HashSet<String>(Arrays.asList(
             "compiler.err.local.var.accessed.from.icls.needs.final", // NOI18N
             "compiler.err.var.might.not.have.been.initialized", // NOI18N
             "compiler.err.report.access" // NOI18N
     ));
 
     private static final Set<JFXTokenId> WHITESPACE = EnumSet.of(JFXTokenId.COMMENT, JFXTokenId.DOC_COMMENT, JFXTokenId.LINE_COMMENT, JFXTokenId.WS);
     
     private AtomicBoolean cancel = new AtomicBoolean();
     private Cancellable cancellable;
 
     UpToDateStatusTask(FileObject file) {
         this.cancellable = new Cancellable() {
             public boolean isCancelled() {
                 return UpToDateStatusTask.this.isCanceled();
             }
 
             public void cancell() {
                 UpToDateStatusTask.this.cancel();
             }
         };
     }
     
     private UpToDateStatusTask() {
     }
     
     public void cancel() {
         cancel.set(true);
     }
 
     private boolean isCanceled() {
         return cancel.get();
     }
 
     public void run(CompilationInfo info) {
         cancel.set(false);
         process(info);
     }
 
     private void process(CompilationInfo info) {
         if (LOGGABLE) log("process: " + info.getFileObject()); // NOI18N
         
         Document doc = info.getDocument();
         if (doc == null) {
             if (LOGGABLE) log("  no document for: " + info); // NOI18N
             return;
         }
 
         List<Diagnostic> diag = info.getDiagnostics();
 
         ArrayList<ErrorDescription> c = new ArrayList<ErrorDescription>();
 
         for (Diagnostic d : diag) {
             if (LOGGABLE) log("    diagnostics: " + d); // NOI18N
             if (d.getSource() instanceof JavaFileObject) {
                 JavaFileObject jfo = (JavaFileObject)d.getSource();
                 if (! jfo.getName().equals(info.getFileObject().getNameExt())) {
                     if (LOGGABLE) log("    in different file: " + jfo.getName() + " vs.: " + info.getFileObject().getNameExt()); // NOI18N
                     continue;
                 }
             } else {
                 if (LOGGABLE) log("    source is not JavaFileObject but: " + (d.getSource() != null ? d.getSource().getClass().getName() : "null")); // NOI18N
             }
             int start = (int)d.getStartPosition();
             int end = (int)d.getEndPosition();
 
             if (start != Diagnostic.NOPOS && end != Diagnostic.NOPOS) {
                 Position[] positions = null;
                 try {
                     positions = getLine(info, d, doc, start, end);
                     final Position sPosition = positions[0];
                     final Position ePosition = positions[1];
                     
                     if (sPosition != null && ePosition != null) {
                         start = sPosition.getOffset();
                         end = ePosition.getOffset();
 
                         if (LOGGABLE) {
                             log("    start == " + start + "  end == " + end); // NOI18
                         }
                         if (start == end) {
                             end = skipWhiteSpace(info, start);
                             if (LOGGABLE) {
                                 log("  after skip  start == " + start + "  end == " + end); // NOI18N
                             }
                         }
 
                         c.add(ErrorDescriptionFactory.createErrorDescription(
                                Severity.ERROR,
                                 d.getMessage(Locale.getDefault()),
                                 doc,
                                 doc.createPosition(start),
                                 doc.createPosition(end)));
                     }
                     continue;
                 } catch (BadLocationException ex) {
                     if (LOGGABLE) {
                         LOGGER.log(Level.INFO, NbBundle.getBundle("org/netbeans/modules/javafx/editor/semantic/Bundle").getString("Problem_with_error_underlining"), ex); // NOI18N
                     }
                 } catch (IOException e) {
                     throw new IllegalStateException(e);
                 }
             } 
             // let's use the line number
             int lastLine = NbDocument.findLineNumber((StyledDocument)doc, doc.getEndPosition().getOffset());
             long linu = d.getLineNumber();
             if (LOGGABLE) log("    lastLine == " + lastLine + " linu == " + linu); // NOI18N
             if ( (linu>0) && (linu-1 <= lastLine)) {
                 c.add(ErrorDescriptionFactory.createErrorDescription(
                     Severity.ERROR, d.getMessage(Locale.getDefault()),
                     doc,(int)linu));
             } else {
                 if (LOGGABLE) log("   NOT USED (wrong bounds): " + d); // NOI18N
             }
         }
         HintsController.setErrors(doc, "semantic-highlighter", c); // NOI18N
 
         UpToDateStatusProviderImpl p = UpToDateStatusProviderImpl.forDocument(doc);
         p.refresh(diag, UpToDateStatus.UP_TO_DATE_OK);
     }
 
     @SuppressWarnings("empty-statement")
     private Position[] getLine(CompilationInfo info, Diagnostic d, final Document doc, int startOffset, int endOffset) throws IOException {
         if (LOGGABLE) {
             LOGGER.fine("diagnostic code: " + d.getCode()); // NOI18N
         }
         StyledDocument sdoc = (StyledDocument) doc;
         DataObject dObj = (DataObject)doc.getProperty(doc.StreamDescriptionProperty );
         if (dObj == null)
             return new Position[] {null, null};
         LineCookie lc = dObj.getCookie(LineCookie.class);
         int lineNumber = NbDocument.findLineNumber(sdoc, startOffset);
         int lineOffset = NbDocument.findLineOffset(sdoc, lineNumber);
         Line line = lc.getLineSet().getCurrent(lineNumber);
 
         boolean rangePrepared = false;
 
 //        if (INVALID_METHOD_INVOCATION.contains(d.getCode())) {
 //            int[] span = translatePositions(info, handlePossibleMethodInvocation(info, d, doc, startOffset, endOffset));
 //
 //            if (span != null) {
 //                startOffset = span[0];
 //                endOffset = span[1];
 //                rangePrepared = true;
 //            }
 //        }
 //
         if (CANNOT_RESOLVE.contains(d.getCode()) && !rangePrepared) {
             int[] span = translatePositions(info, findUnresolvedElementSpan(info, (int) getPrefferedPosition(info, d)));
 
             if (span != null) {
                 startOffset = span[0];
                 endOffset   = span[1];
                 rangePrepared = true;
             }
         }
 
         if (UNDERLINE_IDENTIFIER.contains(d.getCode())) {
             int offset = (int) getPrefferedPosition(info, d);
             TokenSequence<JFXTokenId> ts_ = info.getTokenHierarchy().tokenSequence(JFXTokenId.language());
             SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, doc, cancellable);
 
             int diff = ts.move(offset);
 
             if (ts.moveNext() && diff >= 0 && diff < ts.token().length()) {
                 Token<JFXTokenId> t = ts.token();
 
                 if (t.id() == JFXTokenId.DOT) {
                     while (ts.moveNext() && WHITESPACE.contains(ts.token().id()));
                     t = ts.token();
                 }
 
                 if (t.id() == JFXTokenId.NEW) {
                     while (ts.moveNext() && WHITESPACE.contains(ts.token().id()));
                     t = ts.token();
                 }
 
                 if (t.id() == JFXTokenId.IDENTIFIER) {
                     int[] span = translatePositions(info, new int[] {ts.offset(), ts.offset() + t.length()});
 
                     if (span != null) {
                         startOffset = span[0];
                         endOffset   = span[1];
                         rangePrepared = true;
                     }
                 }
             }
         }
 
         if (!rangePrepared) {
             String text = line.getText();
 
             if (text == null) {
                 //#116560, (according to the javadoc, means the document is closed):
                 cancel();
                 return null;
             }
 
             int column = 0;
             int length = text.length();
 
             while (column < text.length() && Character.isWhitespace(text.charAt(column)))
                 column++;
 
             while (length > 0 && Character.isWhitespace(text.charAt(length - 1)))
                 length--;
 
             if(length == 0) //whitespace only
                 startOffset = lineOffset;
             else
                 startOffset = lineOffset + column;
 
             endOffset = lineOffset + length;
         }
 
         if (LOGGABLE) {
             LOGGER.log(Level.FINE, "startOffset = " + startOffset ); // NOI18N
             LOGGER.log(Level.FINE, "endOffset = " + endOffset ); // NOI18N
         }
 
         final int startOffsetFinal = startOffset;
         final int endOffsetFinal = endOffset;
         final Position[] result = new Position[2];
 
         doc.render(new Runnable() {
             public void run() {
                 if (isCanceled()) {
                     return;
                 }
 
                 int len = doc.getLength();
 
                 if (startOffsetFinal >= len || endOffsetFinal > len) {
                     if (LOGGER.isLoggable(Level.WARNING)) {
                         LOGGER.log(Level.WARNING, "document changed, but not canceled?" ); // NOI18N
                         LOGGER.log(Level.WARNING, "len = " + len ); // NOI18N
                         LOGGER.log(Level.WARNING, "startOffset = " + startOffsetFinal ); // NOI18N
                         LOGGER.log(Level.WARNING, "endOffset = " + endOffsetFinal ); // NOI18N
                     }
                     cancel();
 
                     return;
                 }
 
                 try {
                     result[0] = NbDocument.createPosition(doc, startOffsetFinal, Position.Bias.Forward);
                     result[1] = NbDocument.createPosition(doc, endOffsetFinal, Position.Bias.Backward);
                 } catch (BadLocationException e) {
                     LOGGER.log(Level.SEVERE, "error getting document positions", e); // NOI18N
                 }
             }
         });
 
         return result;
     }
 
     private int skipWhiteSpace(CompilationInfo info, int start) {
         TokenSequence<JFXTokenId> ts_ =  ((TokenHierarchy<?>) info.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, info.getDocument(), cancellable);
         ts.move(start);
         boolean nonWSFound = false;
         while (ts.moveNext()) {
             if (ts.token().id() != JFXTokenId.WS) {
                 nonWSFound = true;
                 break;
             }
         }
         if (!nonWSFound) {
             if (LOGGABLE) log("NOT skipping the WS because we are at the very end"); // NOI18N
             return start;
         }
         int res = ts.offset();
         if (res > start && res < info.getSnapshot().getSource().getDocument(true).getLength()) {
             if (LOGGABLE) log("skipping the whitespace start == " + start + "  res == " + res); // NOI18N
             return res;
         } else {
             if (LOGGABLE) log("NOT skipping the whitespace start == " + start + "  res == " + res); // NOI18N
             return start;
         }
     }
 
     public Token findUnresolvedElementToken(CompilationInfo info, int offset) throws IOException {
         TokenHierarchy<?> th = info.getTokenHierarchy();
         TokenSequence<JFXTokenId> ts_ = th.tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, info.getDocument(), cancellable);
 
         if (ts == null) {
             return null;
         }
 
         ts.move(offset);
         if (ts.moveNext()) {
             Token t = ts.token();
 
             if (t.id() == JFXTokenId.DOT) {
                 ts.moveNext();
                 t = ts.token();
             } else {
                 if (t.id() == JFXTokenId.LT) {
                     ts.moveNext();
                     t = ts.token();
                 } else {
                     if (t.id() == JFXTokenId.NEW || t.id() == JFXTokenId.WS) {
                         boolean cont = ts.moveNext();
 
                         while (cont && ts.token().id() == JFXTokenId.WS) {
                             cont = ts.moveNext();
                         }
 
                         if (!cont)
                             return null;
 
                         t = ts.token();
                     }
                 }
             }
 
             if (t.id() == JFXTokenId.IDENTIFIER) {
                 return ts.offsetToken();
             }
         }
         return null;
     }
 
     private int[] findUnresolvedElementSpan(CompilationInfo info, int offset) throws IOException {
         Token t = findUnresolvedElementToken(info, offset);
 
         if (t != null) {
             return new int[] {
                 t.offset(null),
                 t.offset(null) + t.length()
             };
         }
 
         return null;
     }
 
     public JavaFXTreePath findUnresolvedElement(CompilationInfo info, int offset) throws IOException {
         int[] span = findUnresolvedElementSpan(info, offset);
 
         if (span != null) {
             return info.getTreeUtilities().pathFor(span[0] + 1);
         } else {
             return null;
         }
     }
 
     private int[] translatePositions(CompilationInfo info, int[] span) {
         if (span == null || span[0] == (-1) || span[1] == (-1))
             return null;
 
         int start = span[0];
         int end   = span[1];
 
         if (start == (-1) || end == (-1))
             return null;
 
         return new int[] {start, end};
     }
 
     private long getPrefferedPosition(CompilationInfo info, Diagnostic d) throws IOException {
         if ("compiler.err.doesnt.exist".equals(d.getCode())) { // NOI18N
             return d.getStartPosition();
         }
         if ("compiler.err.cant.resolve.location".equals(d.getCode()) || "compiler.err.cant.resolve.location.args".equals(d.getCode())) { // NOI18N
             int[] span = findUnresolvedElementSpan(info, (int) d.getPosition());
 
             if (span != null) {
                 return span[0];
             } else {
                 return d.getPosition();
             }
         }
         if ("compiler.err.not.stmt".equals(d.getCode())) { // NOI18N
             //check for "Collections.":
             JavaFXTreePath path = findUnresolvedElement(info, (int) d.getStartPosition() - 1);
             Element el = path != null ? info.getTrees().getElement(path) : null;
 
             if (el == null || el.asType().getKind() == TypeKind.ERROR) {
                 return d.getStartPosition() - 1;
             }
 
             if (el.asType().getKind() == TypeKind.PACKAGE) {
                 //check if the package does actually exist:
                 String s = ((PackageElement) el).getQualifiedName().toString();
                 if (info.getElements().getPackageElement(s) == null) {
                     //it does not:
                     return d.getStartPosition() - 1;
                 }
             }
 
             return d.getStartPosition();
         }
 
         return d.getPosition();
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             LOGGER.fine(s);
         }
     }
 }
