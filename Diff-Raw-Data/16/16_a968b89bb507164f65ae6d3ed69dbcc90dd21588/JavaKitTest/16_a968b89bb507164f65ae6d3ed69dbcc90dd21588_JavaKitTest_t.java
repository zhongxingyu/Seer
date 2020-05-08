 package de.cismet.tools.gui.javakit;
 /*
  * @(#)JavaKitTest.java	1.2 99/05/27
  *
  * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
  *
  * This software is the confidential and proprietary information of Sun
  * Microsystems, Inc. ("Confidential Information").  You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Sun.
  *
  * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
  * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
  * THIS SOFTWARE OR ITS DERIVATIVES.
  *
  */
 
 import java.awt.*;
 import java.net.*;
 import java.io.*;
 import javax.swing.*;
 import javax.swing.text.*;
 
 
 /**
  * Simple wrapper around JEditorPane to browse java text
  * using the JavaEditorKit plug-in.
  *
  * java JavaKitTest filename
  */
 public class JavaKitTest {
 
     public static void main(String[] args) {
 	if (args.length != 1) {
	    System.err.println("need filename argument");  //NOI18N
 	    System.exit(1);
 	}
 	try {
 	    JEditorPane editor = new JEditorPane();
 	    JavaEditorKit kit = new JavaEditorKit();
	    editor.setEditorKitForContentType("text/java", kit);  //NOI18N
	    editor.setContentType("text/java");  //NOI18N
 	    editor.setBackground(Color.white);
	    editor.setFont(new Font("Courier", 0, 12));  //NOI18N
 	    editor.setEditable(true);
 
 	    // PENDING(prinz) This should have a customizer and
 	    // be serialized.  This is a bogus initialization.
 	    JavaContext styles = kit.getStylePreferences();
 	    Style s;
 	    s = styles.getStyleForScanValue(Token.COMMENT.getScanValue());
 	    StyleConstants.setForeground(s, new Color(102, 153, 153));
 	    s = styles.getStyleForScanValue(Token.STRINGVAL.getScanValue());
 	    StyleConstants.setForeground(s, new Color(102, 153, 102));
 	    Color keyword = new Color(102, 102, 255);
 	    for (int code = 70; code <= 130; code++) {
 		s = styles.getStyleForScanValue(code);
 		if (s != null) {
 		    StyleConstants.setForeground(s, keyword);
 		}
 	    }
                 File file = new File(args[0]);
                 editor.read(new FileReader(file), file);
                 JScrollPane scroller = new JScrollPane();
                 JViewport vp = scroller.getViewport();
                 vp.setBackingStoreEnabled(true);
                 vp.add(editor);
 
	    JFrame f = new JFrame(org.openide.util.NbBundle.getMessage(JavaKitTest.class, "JavaKitTest.main(String[]).f.title", args[0]));  //NOI18N
 	    f.getContentPane().setLayout(new BorderLayout());
	    f.getContentPane().add("Center", scroller);  //NOI18N
 	    f.pack();
 	    f.setSize(600, 600);
 	    f.setVisible(true);
 	} catch (Throwable e) {
 	    e.printStackTrace();
 	    System.exit(1);
 	}
     }
 
 }
