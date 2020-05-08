 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * Portions Copyrighted 2007 Sun Microsystems, Inc.
  */
 package org.netbeans.modules.javafx.editor.semantic;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.swing.text.Document;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenId;
 import org.netbeans.api.lexer.TokenSequence;
 
 /**
  *
  * @author Anton Chechel
  */
 public class ProtectedTokenSequence<T extends TokenId> {
 
     private TokenSequence<T> ts;
     private Document doc;
     private AtomicBoolean cancel;
 
     private ProtectedTokenSequence() {
     }
 
     public ProtectedTokenSequence(final TokenSequence<T> ts, final Document doc, final AtomicBoolean cancel) {
         this.ts = ts;
         this.doc = doc;
         this.cancel = cancel;
     }
 
     public Token<T> token() {
         return ts.token();
     }
 
     public long offset() {
         return ts.offset();
     }
 
     public boolean moveNext() {
         final boolean[] res = new boolean[1];
         doc.render(new Runnable() {
             public void run() {
                 if (cancel.get()) {
                     res[0] = false;
                } else if (!ts.isValid()) {
                     cancel.set(true);
                     res[0] = false;
                } else {
                    res[0] = ts.moveNext();
                 }
             }
         });
         return res[0];
     }
 
     public boolean movePrevious() {
         final boolean[] res = new boolean[1];
         doc.render(new Runnable() {
             public void run() {
                 if (cancel.get()) {
                     res[0] = false;
                } else if (!ts.isValid()) {
                     cancel.set(true);
                     res[0] = false;
                } else {
                    res[0] = ts.movePrevious();
                 }
             }
         });
         return res[0];
     }
 
     public void moveEnd() {
         doc.render(new Runnable() {
             public void run() {
                 if (cancel.get()) {
                     return;
                } else if (!ts.isValid()) {
                     cancel.set(true);
                     return;
                } else {
                    ts.moveEnd();
                 }
             }
         });
     }
    
 }
