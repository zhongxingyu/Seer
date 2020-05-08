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
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 package org.netbeans.modules.javafx.editor;
 
 import org.netbeans.modules.editor.indent.spi.Context;
 import org.netbeans.modules.editor.indent.spi.IndentTask;
 import org.netbeans.modules.javafx.editor.format.CompilerIndependentJFXIndentTask;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 /**
  * @author Rastislav Komara (<a href="mailto:rastislav.komara@sun.com">RKo</a>)
 * @todo documentation
  */
 public class IndentTaskFactory implements IndentTask.Factory {
 
     private static Logger log = Logger.getLogger(IndentTaskFactory.class.getName());
 
     /**
     * Create reformatting task.
      *
      * @param context non-null indentation context.
     * @return reformatting task or null if the factory cannot handle the given context.
      */
     public IndentTask createTask(Context context) {
         if (log.isLoggable(Level.FINE)) {
            log.fine("Creating reformat factory"); // NOI18N
         }
         return new CompilerIndependentJFXIndentTask(context);
     }
 }
