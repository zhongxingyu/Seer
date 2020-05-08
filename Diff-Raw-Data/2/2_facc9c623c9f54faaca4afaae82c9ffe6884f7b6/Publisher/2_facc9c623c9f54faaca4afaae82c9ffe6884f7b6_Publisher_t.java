 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
  * Contributor(s):
  *
  * Portions Copyrighted 1997-2008 Sun Microsystems, Inc.
  */
 package org.netbeans.modules.javafx.editor.imports;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenId;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.editor.indent.api.Reformat;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import java.util.logging.Logger;
 import javax.swing.text.Caret;
 import org.netbeans.modules.javafx.editor.imports.ImportsModel.Declared;
 
 /**
  * The publisher will use the information stored in the {@linkplain ImportsModel} instance
  * to modify the given {@linkplain Document} accordingly
  * After modification the document will contain only used and valid imports
  *
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
  * @author Jaroslav Bachorik
  * @todo documentation
  */
 class Publisher implements Runnable {
     private static final class PositionComparator implements Comparator<Declared> {
 
         public int compare(Declared o1, Declared o2) {
             if (o1.getStart() < o2.getStart()) return 1;
             else if (o1.getStart() > o2.getStart()) return -1;
             else return 0;
         }
 
     }
     private final Document doc;
     private static Logger log = Logger.getLogger(Publisher.class.getName());
     private final ImportsModel model;
     private int origCaret;
     private Caret currentCaret;
     private Comparator<Declared> importsComparator = new PositionComparator();
 
     public Publisher(Document doc, ImportsModel model, Caret currentCaret, int origCaret) {
         this.doc = doc;
         this.model = model;
         this.origCaret = origCaret;
         this.currentCaret = currentCaret;
     }
 
     @SuppressWarnings("empty-statement")
    synchronized public void run() {
         // no need to guard against TS modifications, runs over locked Document
         TokenSequence<JFXTokenId> ts = getTokenSequence(doc, 0);
 
         // reformat not used for now
         Reformat reformat = null;
         int offsetDiff = 0;
 
         try {
             int offset = (int)(model.getImportsEnd() + 1);
             if (offset < 0) {
                 offset = moveBehindPackage(ts);
             }
 
             if (!model.getUnresolved().isEmpty()) {
                 StringBuilder newImports = new StringBuilder();
 
                 ImportsModel.Unresolved[] unresolvedArr = model.getUnresolved().toArray(new ImportsModel.Unresolved[0]);
                 Arrays.sort(unresolvedArr);
                 for(ImportsModel.Unresolved unresolved : unresolvedArr) {
                     if (unresolved.getResolvedName() != null) {
                         newImports.append("\nimport ").append(unresolved.getResolvedName()).append(";");
                     }
                 }
                 if (newImports.length() > 0) {
                     offsetDiff += newImports.toString().length();
                     doc.insertString(offset, newImports.toString(), null);
                 }
             }
 
             Set<ImportsModel.Declared> unusedSet = new TreeSet<ImportsModel.Declared>(importsComparator);
             unusedSet.addAll(model.getUnusedImports());
 
             for(ImportsModel.Declared unused : unusedSet) {
                 int end = (int)unused.getEnd();
                 while (!doc.getText(end++, 1).equals("\n"));
 
                 offsetDiff -= (int)(end - unused.getStart());
                 doc.remove((int)unused.getStart(), (int)(end - unused.getStart()));
             }
 
 //            reformat = Reformat.get(doc);
 //            reformat.lock();
 //            reformat.reformat(0, end.getOffset());
         } catch (BadLocationException e) {
             log.severe(e.getLocalizedMessage());
         } finally {
             if (origCaret > model.getImportsEnd()) {
                 currentCaret.setDot(origCaret + offsetDiff);
             }
 //            if (reformat != null) {
 //                reformat.unlock();
 //            }
         }
     }
 
     @SuppressWarnings({"MethodWithMultipleLoops"})
     private int moveBehindPackage(TokenSequence<JFXTokenId> ts) {
         boolean wasWS = false;
         int lastNonWSOffset = 0;
         while (ts.moveNext()) {
             JFXTokenId id = ts.token().id();
             if (JFXTokenId.isComment(id)
                     || id == JFXTokenId.WS) {
                 if (id == JFXTokenId.WS) {
                     if (!wasWS) {
                         lastNonWSOffset = ts.offset(); // don't decrement the offset; it makes the import appear inside the comments if there's no package declaration (#177269)
                         wasWS = true;
                     }
                 } else {
                     wasWS= false;
                 }
                 continue;
             } else if (id == JFXTokenId.PACKAGE) {
                 moveTo(ts, JFXTokenId.SEMI);
                 return ts.offset() + 1;
             }
             break;
         }
         return lastNonWSOffset;
     }
 
     private void moveTo(TokenSequence<JFXTokenId> ts, JFXTokenId id) {
         while (ts.moveNext()) {
             if (ts.token().id() == id) break;
         }
     }
 
     @SuppressWarnings({"unchecked"})
     private static <JFXTokenId extends TokenId> TokenSequence<JFXTokenId> getTokenSequence(Document doc, int dotPos) {
         TokenHierarchy<Document> th = TokenHierarchy.get(doc);
         TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) th.tokenSequence();
         ts.move(dotPos);
         return ts;
     }
 }
