 /*
  * Copyright (c) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.github.fhirschmann.clozegen.gui;
 
 import com.github.fhirschmann.clozegen.lib.imf.IntermediateFormat;
 import com.google.common.collect.Ranges;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.Highlighter.HighlightPainter;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
@SuppressWarnings("serial")
 public class ClozeTestPane extends JTextPane {
     /**
      * The painter used to highlight enabled gaps.
      */
     private static final HighlightPainter ENABLED_HP =
             new DefaultHighlighter.DefaultHighlightPainter(new Color(181, 255, 166));
 
     /**
      * The painter used to highlight disabled gaps.
      */
     private static final HighlightPainter DISABLED_HP =
             new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 200, 140));
 
     /**
      * The update interval in ms.
      */
     public static final int UPDATE_INTERVAL = 2000;
 
     /**
      * The current language of the pane.
      */
     private String language;
 
     /**
      * Creates a new ClozeTestPane.
      */
     public ClozeTestPane() {
         super();
 
         Timer timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent ae) {
                 update();
             }
 
         });
         timer.start();
         language = "en";
     }
 
     /**
      * Toggles the gap at the current caret position on or off.
      */
     public void toggleGap() {
         Matcher matcher = IntermediateFormat.PATTERN.matcher(getText());
         int pos = getCaretPosition();
         while (matcher.find()) {
             if (Ranges.closed(matcher.start(), matcher.end()).contains(pos)) {
                 if (matcher.group(3).equals("")) {
                     setText(getText().substring(0, matcher.end())
                             + "d" + getText().substring(matcher.end()));
                 } else {
                     setText(getText().substring(0, matcher.end() - 1)
                             + getText().substring(matcher.end()));
                 }
                 setCaretPosition(pos);
                 update();
             }
         }
     }
 
     /**
      * Updates the highlighted gaps.
      */
     public void update() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 int selStart = getSelectionStart();
                 int selEnd = getSelectionEnd();
                 int pos = getCaretPosition();
 
                 Matcher matcher = IntermediateFormat.PATTERN.matcher(getText());
                 getHighlighter().removeAllHighlights();
                 while (matcher.find()) {
                     try {
                         getHighlighter().addHighlight(matcher.start(), matcher.end(),
                                 matcher.group(3).equals("") ? ENABLED_HP : DISABLED_HP);
                     } catch (BadLocationException ex) {
                         Logger.getLogger(ClozeTestPane.class.getName()).
                                 log(Level.SEVERE, null, ex);
                     }
                 }
 
                 setCaretPosition(pos);
                 setSelectionStart(selStart);
                 setSelectionEnd(selEnd);
             }
         });
     }
 
     /**
      * @return the language
      */
     public String getLanguage() {
         return language;
     }
 
     /**
      * @param language the language to set
      */
     public void setLanguage(String language) {
         this.language = language;
     }
 }
