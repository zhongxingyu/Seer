 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
  * Microsystems, Inc. All Rights Reserved.
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
  */
 
 package org.netbeans.modules.javafx.refactoring.transformations;
 
 import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.modules.refactoring.api.RefactoringSession;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 import org.openide.text.DataEditorSupport;
 
 /**
  *
  * @author Jaroslav Bachorik <jaroslav.bachorik@sun.com>
  */
 abstract public class Transformer {
     final private static Logger LOGGER = Logger.getLogger(Transformer.class.getName());
     
     private TransformationContext context = new TransformationContext();
    final private Collection<int[]> removals = new ArrayList<int[]>();

     final private static Map<RefactoringSession, Map<FileObject, Transformer>> foTransformers = new WeakHashMap<RefactoringSession, Map<FileObject, Transformer>>();
 
     private String backup;
     private TransformationTarget writer;
 
     protected Transformer(final String content, final TransformationTarget writer) {
         backup = content;
         this.writer = writer;
     }
 
     public static Transformer forText(StringBuilder text) {
         return new StringTransformer(text);
     }
 
     public static Transformer forFileObject(FileObject fo, RefactoringSession session) {
         synchronized(foTransformers) {
             Map<FileObject, Transformer> map = foTransformers.get(session);
             if (map == null) {
                 map = new WeakHashMap<FileObject, Transformer>();
                 foTransformers.put(session, map);
             }
             Transformer t = map.get(fo);
             if (t == null) {
                 try {
                     DataObject dobj = DataObject.find(fo);
                     DataEditorSupport des = (DataEditorSupport) dobj.getCookie(EditorCookie.class);
                     t = new DocumentTransformer((BaseDocument) des.openDocument());
                     map.put(fo, t);
                 } catch (DataObjectNotFoundException dataObjectNotFoundException) {
                     LOGGER.log(Level.WARNING, null, dataObjectNotFoundException);
                 } catch (IOException e) {
                     LOGGER.log(Level.WARNING, null, e);
                 }
             }
             return t;
         }
     }
 
     final synchronized void insertText(int pos, String text) {
        for(int[] interval : removals) {
            if (pos >= interval[0] && pos <= interval[1]) {
                return;
            }
        }
         int startPos = context.getRealOffset(pos);
 
         writer.insertText(startPos, text);
         context.replaceText(pos, 0, text.length());
     }
 
     final synchronized String removeText(int pos, int len) {
        removals.add(new int[]{pos, pos + len - 1});
         int startPos = context.getRealOffset(pos);
         int endPos = context.getRealOffset(pos + len - 1);
 
         String removed = writer.removeText(startPos, endPos - startPos + 1);
         context.replaceText(pos, endPos - startPos + 1, 0);
         return removed;
     }
 
     final synchronized void replaceText(int pos, String oldText, String newText) {
        for(int[] interval : removals) {
            if (pos >= interval[0] && pos <= interval[1]) {
                return;
            }
        }
         int realPos = context.getRealOffset(pos);
         writer.replaceText(realPos, oldText, newText);
         context.replaceText(pos, oldText.length(), newText.length());
     }
 
     final synchronized public void transform(final List<Transformation> transformations) {
         runTransformation(new Runnable() {
             public void run() {
                 Collections.sort(transformations, Transformation.COMPARATOR);
                 for(Transformation t : transformations) {
                     t.perform(Transformer.this);
                 }
             }
         });
     }
 
     final protected String getOriginalContent() {
         return backup;
     }
 
     abstract protected void runTransformation(Runnable task);
 }
