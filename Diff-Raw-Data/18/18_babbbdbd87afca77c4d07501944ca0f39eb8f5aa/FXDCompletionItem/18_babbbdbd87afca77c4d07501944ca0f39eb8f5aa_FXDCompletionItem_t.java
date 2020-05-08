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
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
 package org.netbeans.modules.javafx.fxd.composer.editor.completion;
 
 import com.sun.javafx.tools.fxd.schema.model.AbstractSchemaElement;
 import com.sun.javafx.tools.fxd.schema.model.Element;
 import com.sun.javafx.tools.fxd.schema.model.Enumeration;
 import com.sun.javafx.tools.fxd.schema.model.Function;
 import com.sun.javafx.tools.fxd.schema.model.PrimitiveType;
 import com.sun.javafx.tools.fxd.schema.model.Property;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JToolTip;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import org.netbeans.api.editor.completion.Completion;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.spi.editor.completion.CompletionItem;
 import org.netbeans.spi.editor.completion.CompletionResultSet;
 import org.netbeans.spi.editor.completion.CompletionTask;
 import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
 import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
 import org.netbeans.spi.editor.completion.support.CompletionUtilities;
 
 /**
  *
  * @author avk
  */
 public class FXDCompletionItem implements CompletionItem {
 
     private static final char[] CODE_COMPL_SUBST_BREAKERS =
         {';', '.', ',', '+', '-', '/', '%', '^', '|', '&', // NOI18N
          '(', ')', '{', '}', '[', ']',                     // NOI18N
          ' ', '\t', '\n', '\r'};                           // NOI18N
     
     protected static final String PRIMITIVE_TYPE_DEF_VALUE_STRING
             = "\"\""; //NOI18N
     protected static final String PRIMITIVE_TYPE_DEF_VALUE_INTEGER
             = "0";//NOI18N
     protected static final String PRIMITIVE_TYPE_DEF_VALUE_NUMBER
             = "0";//NOI18N
     protected static final String PRIMITIVE_TYPE_DEF_VALUE_BOOLEAN
             = "true";//NOI18N
 
     private String m_displayText;
     private String m_text;
     private int m_startOffset;
     private AbstractSchemaElement m_element;
 
     public FXDCompletionItem(String text, int startOffset) {
         this(text, text, startOffset);
     }
 
     public FXDCompletionItem(String text, String displayDext, int startOffset) {
         assert text != null;
         m_text = text;
         m_displayText = displayDext;
         m_startOffset = startOffset;
     }
 
     public FXDCompletionItem(AbstractSchemaElement element, int startOffset) {
         assert element != null;
         m_element = element;
         m_displayText = createDisplayText(element);
         m_startOffset = startOffset;
     }
 
     // TODO in case of completing started word type only the rest
     public void defaultAction(JTextComponent component) {
         if (component != null) {
             Completion.get().hideDocumentation();
             Completion.get().hideCompletion();
             int caretOffset = component.getSelectionEnd();
             substituteText(component, m_startOffset, caretOffset - m_startOffset, getText());
         }
         //Completion.get().hideAll();
     }
 
     protected void substituteText(JTextComponent c, final int offset, 
             int len, final String text) {
         final BaseDocument doc = (BaseDocument) c.getDocument();
         try {
             String tx = doc.getText(0, doc.getLength());
             len = getSubstitutionLenght(tx, offset, len);
         } catch (BadLocationException e) {
         }
         final int length = len;
         doc.runAtomic (new Runnable () {
             public void run () {
                 try {
                     doc.remove(offset, length);
                     doc.insertString(m_startOffset, text, null);
                 } catch (BadLocationException e) {
                     // Can't update
                 }
             }
         });
 
     }
 
     public void processKeyEvent(KeyEvent evt) {
         // TODO
     }
 
     public int getPreferredWidth(Graphics g, Font defaultFont) {
         return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
     }
 
     public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
         CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
     }
 
     public CompletionTask createDocumentationTask() {
         return new AsyncCompletionTask(new AsyncCompletionQuery() {
 
             protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                 completionResultSet.setDocumentation(new FXDCompletionDocumentation(FXDCompletionItem.this));
                 completionResultSet.finish();
             }
         });
     }
 
     public CompletionTask createToolTipTask() {
         return new AsyncCompletionTask(new AsyncCompletionQuery() {
 
             protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                 JToolTip toolTip = new JToolTip();
                 toolTip.setTipText("Press Enter to insert \"" + getText() + "\"");
                 completionResultSet.setToolTip(toolTip);
                 completionResultSet.finish();
             }
         });
     }
 
     public boolean instantSubstitution(JTextComponent component) {
         return false;
     }
 
     public int getSortPriority() {
         return 0;
     }
 
     public CharSequence getSortText() {
         return m_displayText;
     }
 
     public CharSequence getInsertPrefix() {
         return m_text;
     }
 
     protected String getDisplayText() {
         return m_displayText;
     }
 
     protected String getText() {
         if (m_text == null){
             m_text = createText(getSchemaElement());
         }
         return m_text;
     }
 
     protected AbstractSchemaElement getSchemaElement() {
         return m_element;
     }
 
     protected ImageIcon getIcon() {
         return null;
     }
 
     protected String getLeftHtmlText() {
         return getDisplayText();
     }
 
     protected String getRightHtmlText() {
         return null;
     }
 
     private static String createDisplayText(AbstractSchemaElement element) {
         if (element instanceof Property) {
             return element.id;
         } else if (element instanceof Function) {
             return element.id;
         } else if (element instanceof Enumeration) {
             return element.id;
         } else if (element instanceof PrimitiveType){
             return element.id;
         } else if (element instanceof Element) {
             return splitFullName(element.id)[1];
         }
         return element.toString();
     }
 
     private static String createText(AbstractSchemaElement element) {
         if (element instanceof Property) {
             Property prop = (Property) element;
             String text = prop.id;
             if (prop.defaultValue != null){
                 text += " : " + defaultValueToString(prop); // NOI18N
             }
             return text;
         } else if (element instanceof Function) {
             return element.id;
         } else if (element instanceof Enumeration) {
             return element.id;
         } else if (element instanceof PrimitiveType){
             return getPrimitiveTypeDefValue((PrimitiveType)element);
         } else if (element instanceof Element) {
             return splitFullName(element.id)[1];
         }
         return element.toString();
     }
 
     private static String defaultValueToString(Property prop){
         assert prop.defaultValue != null;
         if (prop.isArray){
             String str = prop.defaultValue.toString();
             if (str.equals("]")){
                 str = "[]";
             }
             return str;
         } else {
             return prop.defaultValue.toString();
         }
     }
 
     private static int getSubstitutionLenght(final String text, final int offset, int length) {
         if (text == null) {
             return length;
         }
         int tlength = text.length();
         int index = offset + tlength;
         for (int i = 0; i < CODE_COMPL_SUBST_BREAKERS.length; i++) {
             int k = text.indexOf(CODE_COMPL_SUBST_BREAKERS[i], offset);
             if (k != -1 && k < index) {
                 index = k;
             }
         }
         if (tlength < index) {
             index = tlength;
         }
         int ret = index - offset;
         if (length > ret) {
             ret = length;
         }
         return ret;
     }
 
     public static String[] splitFullName(String classFullName) {
         int index = classFullName.lastIndexOf('-');
         return new String[]{
                     classFullName.substring(0, index),
                     classFullName.substring(index + 1)
                 };
     }
 
     private static String getPrimitiveTypeDefValue(PrimitiveType primType) {
         if (primType == PrimitiveType.STRING) {
             return PRIMITIVE_TYPE_DEF_VALUE_STRING;
         } else if (primType == PrimitiveType.NUMBER) {
             return PRIMITIVE_TYPE_DEF_VALUE_NUMBER;
         } else if (primType == PrimitiveType.INTEGER) {
             return PRIMITIVE_TYPE_DEF_VALUE_INTEGER;
         } else if (primType == PrimitiveType.BOOLEAN) {
             return PRIMITIVE_TYPE_DEF_VALUE_BOOLEAN;
         }
         return null;
     }
 
 
 }
