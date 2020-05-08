 /*
  * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
  * 
  * This file is part of Essiembre ResourceBundle Editor.
  * 
  * Essiembre ResourceBundle Editor is free software; you can redistribute it 
  * and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * Essiembre ResourceBundle Editor is distributed in the hope that it will be 
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with Essiembre ResourceBundle Editor; if not, write to the 
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
  * Boston, MA  02111-1307  USA
  */
 package com.essiembre.eclipse.i18n.resourcebundle.editors;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.editors.text.TextEditor;
 
 import com.essiembre.eclipse.i18n.resourcebundle.preferences.RBPreferences;
 
 /**
  * A single ResourceBundle related resources.
  * @author Pascal Essiembre
  * @version $Author$ $Revision$ $Date$
  */
 public class Bundle {
 
     /** Title for this bundle. */
     private String title;
     /** Locale for this bundle. */
     private Locale locale;
     /** Text editor for this bundle. */
     private TextEditor editor;
     /** All data contained by this bundle. */
     private Map data;
     /** Text box used for this bundle. */
     private Text textBox;
     /** Header comments. */
     private List comments = new ArrayList();
     
     /**
      * Constructor.
      */
     public Bundle() {
         super();
     }
 
     /**
      * Gets the "data" attribute.
      * @return Returns the data.
      */
     public Map getData() {
         return data;
     }
     /**
      * Sets the "data" attribute.
      * @param data The data to set.
      */
     public void setData(Map data) {
         this.data = data;
     }
     /**
      * Gets the "editor" attribute.
      * @return Returns the editor.
      */
     public TextEditor getEditor() {
         return editor;
     }
     /**
      * Sets the "editor" attribute.
      * @param editor The editor to set.
      */
     public void setEditor(TextEditor editor) {
         this.editor = editor;
     }
     /**
      * Gets the "locale" attribute.
      * @return Returns the locale.
      */
     public Locale getLocale() {
         return locale;
     }
     /**
      * Sets the "locale" attribute.
      * @param locale The locale to set.
      */
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
     /**
      * Gets the "title" attribute.
      * @return Returns the title.
      */
     public String getTitle() {
         return title;
     }
     /**
      * Sets the "title" attribute.
      * @param title The title to set.
      */
     public void setTitle(String title) {
         this.title = title;
     }
     /**
      * Gets the "textBox" attribute.
      * @return Returns the textBox.
      */
     public Text getTextBox() {
         return textBox;
     }
     /**
      * Sets the "textBox" attribute.
      * @param textBox The textBox to set.
      */
     public void setTextBox(Text textBox) {
         this.textBox = textBox;
     }
     
     /**
      * Refreshes the text box content according to value associated
      * with the gvien resource bundle key.
      * @param key resource bundle key to get value from.
      */
     public void refreshTextBox(String key) {
         if (key == null || data.get(key) == null) {
             textBox.setText("");
             if (key == null) {
                 textBox.setEnabled(false);
             } else {
                 textBox.setEnabled(true);
             }
         } else {
             textBox.setText((String) data.get(key));
             textBox.setEnabled(true);
         }
     }
     
     /**
      * Refreshes an editor, with bundle data.
      */
     public void refreshEditor() {
         String content = null;
         IDocument doc = editor.getDocumentProvider().getDocument(
                 editor.getEditorInput());
         content = BundleUtils.generateComments(comments);
         content += BundleUtils.generateContent(data);
         doc.set(content);
     }
     
     /**
      * Gets sorted resource bundle keys for this bundle.
      * @param resource bundle keys
      */
     public List getKeys() {
         List keys = new ArrayList();
         keys.addAll(data.keySet());
         Collections.sort(keys);
         return keys;
     }
 
     /**
      * Refreshes bundle data by parsing editor content.
      */
     public void refreshData() {
         IDocument doc = editor.getDocumentProvider().getDocument(
                 editor.getEditorInput());
         StringTokenizer tokenizer =
                 new StringTokenizer(doc.get(), "\n\r");
         Map data = new TreeMap();
         List comments = new ArrayList();
         while (tokenizer.hasMoreTokens()) {
             StringBuffer line = 
                     new StringBuffer(tokenizer.nextToken().trim());
             int equalPosition = line.indexOf("=");
 
             // parse header comment lines
             if (line.indexOf("#") == 0 && data.size() == 0) {
                 comments.add(line.substring(1));
             // parse regular lines
            } else if (line.indexOf("#") != 0 && equalPosition >= 1) {
                 while (line.lastIndexOf("\\") == line.length() -1) {
                     int lineBreakPosition = line.lastIndexOf("\\");
                     line.replace(
                             lineBreakPosition,
                             lineBreakPosition + 1, "");
                     line.append(tokenizer.nextToken().trim());
                 }
                 String key = line.substring(0, equalPosition).trim();
                 String value = line.substring(equalPosition + 1).trim();
                 if (RBPreferences.getConvertEncodedToUnicode()) {
                     key = BundleUtils.convertEncodedToUnicode(key);
                     value = BundleUtils.convertEncodedToUnicode(value);
                 }
                 data.put(key, value);
             }
         }
         this.data = data;
         this.comments = comments;
     }
 
     /**
      * Gets the "comments" attribute.
      * @return Returns the comments.
      */
     public List getComments() {
         return comments;
     }
     /**
      * Sets the "comments" attribute.
      * @param comments The comments to set.
      */
     public void setComments(List comments) {
         this.comments = comments;
     }
 }
