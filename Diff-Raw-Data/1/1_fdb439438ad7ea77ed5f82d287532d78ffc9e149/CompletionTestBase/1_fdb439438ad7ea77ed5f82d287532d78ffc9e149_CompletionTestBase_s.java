 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
 
 package org.netbeans.modules.javafx.editor.completion;
 
import java.awt.Color;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import javax.swing.text.Document;
 
 import org.netbeans.api.javafx.editor.TestUtilities;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.SourceTestBase;
 import org.netbeans.api.lexer.Language;
 import org.netbeans.junit.AssertionFileFailedError;
 import org.netbeans.modules.editor.completion.CompletionItemComparator;
 import org.netbeans.spi.editor.completion.CompletionItem;
 import org.netbeans.spi.editor.completion.CompletionProvider;
 import org.netbeans.spi.editor.completion.LazyCompletionItem;
 import org.openide.LifecycleManager;
 
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 
 /**
  * Based on java.editor
  */
 public class CompletionTestBase extends SourceTestBase {
 
     static final int FINISH_OUTTIME = 5 * 60 * 1000;
 
     public CompletionTestBase(String testName) {
         super(testName);
     }
 
     /**
      * Return the offset of the given position, indicated by ^ in the line
      * fragment from the fuller text
      */
     private static int getCaretOffset(String text, String caretLine) {
         return getCaretOffsetInternal(text, caretLine);
     }
 
     /**
      * Like <code>getCaretOffset</code>, but the returned
      * <code>CaretLineOffset</code> contains also the modified
      * <code>caretLine</code> param.
 
      * @param text
      * @param caretLine
      * @return offset
      */
     private static int getCaretOffsetInternal(String text, String caretLine) {
         int caretDelta = caretLine.indexOf('^');
         assertTrue(caretDelta != -1);
         caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
         int lineOffset = text.indexOf(caretLine);
         assertTrue("No occurrence of caretLine " + caretLine + " in text '" + text + "'", lineOffset != -1);
         return lineOffset + caretDelta;
     }
 
     protected void checkCompletion(final String source, final String caretLine, final String goldenFileName) throws Exception {
         checkCompletion(source, caretLine, null, goldenFileName);
     }
 
     protected void checkCompletion(final String source, final String caretLine, final String insert, final String goldenFileName) throws Exception {
         File testSource = new File(getWorkDir(), "test/Test.fx");
         testSource.getParentFile().mkdirs();
         File sourceFile = new File(getDataDir(), "org/netbeans/modules/javafx/editor/completion/data/" + source + ".fx");
         String sourceText = slurp(sourceFile);
         int caretPos = getCaretOffset(sourceText, caretLine);
         if (insert != null) {
             // insert a code snippet at the caret and move the caret accordingly,
             // to pretend user typing given text before invoking the code completion
             StringBuilder sb = new StringBuilder(sourceText);
             sb.insert(caretPos, insert);
             caretPos += insert.length();
             sourceText = sb.toString();
         }
         TestUtilities.copyStringToFile(testSource, sourceText);
 
         FileObject testSourceFO = FileUtil.toFileObject(testSource);
         assertNotNull(testSourceFO);
         DataObject testSourceDO = DataObject.find(testSourceFO);
         assertNotNull(testSourceDO);
         EditorCookie ec = testSourceDO.getCookie(EditorCookie.class);
         assertNotNull(ec);
         final Document doc = ec.openDocument();
         assertNotNull(doc);
         doc.putProperty(Language.class, JFXTokenId.language());
         doc.putProperty("mimeType", "text/x-fx");
         JavaFXSource s = JavaFXSource.forDocument(doc);
         Set<? extends CompletionItem> items0 = JavaFXCompletionProvider.query(
                 s, CompletionProvider.COMPLETION_QUERY_TYPE, caretPos, caretPos);
         List<? extends CompletionItem> items = new ArrayList<CompletionItem>(items0);
         Collections.sort(items, CompletionItemComparator.BY_PRIORITY);
 
         File output = new File(getWorkDir(), getName() + ".out");
         Writer out = new FileWriter(output);
         for (Object item : items) {
             if (item instanceof LazyCompletionItem) {
                 ((LazyCompletionItem) item).accept();
             }
             String itemString = item.toString();
             if (!(org.openide.util.Utilities.isMac() && itemString.equals("apple"))) { //ignoring 'apple' package
                 out.write(itemString);
                 out.write("\n");
             }
         }
         out.close();
 
         File goldenFile = new File(getDataDir(), "/goldenfiles/org/netbeans/modules/javafx/editor/completion/JavaFXCompletionProviderTest/" + goldenFileName);
         File diffFile = new File(getWorkDir(), getName() + ".diff");
         String message = "The files:\n  " + goldenFile.getAbsolutePath() + "\n  " +
                 output.getAbsolutePath() + "\nshould have the same content.\n" +
                 "  check diff: " + diffFile.getAbsolutePath();
         try {
             assertFile(message, output, goldenFile, diffFile);
         } catch (AssertionFileFailedError affe) {
             System.err.println(diffFile.getAbsolutePath() + " content:\n" + slurp(diffFile));
             throw affe;
         }
 
         LifecycleManager.getDefault().saveAll();
     }
 
 }
