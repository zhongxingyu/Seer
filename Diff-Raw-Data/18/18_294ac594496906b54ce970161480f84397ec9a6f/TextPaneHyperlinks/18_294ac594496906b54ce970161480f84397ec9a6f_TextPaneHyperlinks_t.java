 /*
  * Collab desktop - Software for shared drawing via internet in real-time
  * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
  *
  * This file is part of Collab desktop.
  *
  * Collab desktop is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Collab desktop is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Collab desktop.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.utils.gui.textpane;
 
 import cz.mgn.collabdesktop.utils.TextUtils;
 import cz.mgn.collabdesktop.utils.Utils;
 import java.awt.Cursor;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JTextPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.*;
 
 /**
  *
  * @author indy
  *
  * JTextPane s automatickou detekci odkazu s moznosti na ne kliknout
  */
 public class TextPaneHyperlinks extends JTextPane implements HyperlinkListener, MouseListener, MouseMotionListener {
 
     protected String lastURL = null;
 
     public TextPaneHyperlinks() {
         init();
     }
 
     private void init() {
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
     @Override
     public void setText(String text) {
         super.setText("");
         insertString(0, text, null);
     }
 
     /**
      * vlozi retezec
     *
      * @param pos pozice na kterou se prida retezec
      * @param string retezec
      * @param style style jakym se ma retezec pridat
      */
     protected void insertString(int pos, String string, Style style) {
         StyledDocument doc = (StyledDocument) getDocument();
         Style linkStyle = doc.addStyle("linkStyle", null);
         if (style != null) {
             linkStyle.addAttributes(style.copyAttributes());
         }
         StyleConstants.setUnderline(linkStyle, true);
         int l = 0;
         ArrayList<Integer> links = TextUtils.getLinkPositions(string);
         for (int i = 0; i < links.size(); i += 2) {
             int s = links.get(i);
             int e = links.get(i + 1);
             String bif = string.substring(l, s);
             if (bif.length() > 0) {
                 try {
                     doc.insertString(l + pos, bif, style);
                 } catch (BadLocationException ex) {
                     Logger.getLogger(TextPaneHyperlinks.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 l += bif.length();
             }
             try {
                 doc.insertString(l + pos, string.substring(s, e), linkStyle);
             } catch (BadLocationException ex) {
                 Logger.getLogger(TextPaneHyperlinks.class.getName()).log(Level.SEVERE, null, ex);
             }
             l = e;
         }
         if (l < string.length()) {
             try {
                 doc.insertString(l + pos, string.substring(l), style);
             } catch (BadLocationException ex) {
                 Logger.getLogger(TextPaneHyperlinks.class.getName()).log(Level.SEVERE, null, ex);
             }
        } else {
            //HACK: if text ending by link, rest of area is clickable, so adding white character after links on end of string
            try {
                doc.insertString(l + pos + string.substring(l).length(), " ", style);
            } catch (BadLocationException ex) {
                Logger.getLogger(TextPaneHyperlinks.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
     }
 
     @Override
     public void hyperlinkUpdate(HyperlinkEvent he) {
         if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            Utils.browse(he.getURL().toString());
         }
         if (he.getEventType() == HyperlinkEvent.EventType.ENTERED) {
             setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         }
         if (he.getEventType() == HyperlinkEvent.EventType.EXITED) {
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
     }
 
     protected void testLink(MouseEvent me, HyperlinkEvent.EventType eventType) {
         Position.Bias[] biasRet = new Position.Bias[1];
         int car = getUI().viewToModel(this, me.getPoint(), biasRet);
         if (car >= 0) {
             String word = TextUtils.getWord(getText(), car);
             if (TextUtils.isLink(word)) {
                 if (eventType == null && (lastURL == null || !word.equals(lastURL))) {
                     eventType = HyperlinkEvent.EventType.ENTERED;
                 }
                 if (eventType != null) {
                     lastURL = word;
                     try {
                         hyperlinkUpdate(new HyperlinkEvent(me.getSource(), eventType, new URL(word)));
                     } catch (MalformedURLException ex) {
                         Logger.getLogger(TextPaneHyperlinks.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             } else {
                 testExitedLink(me, eventType);
             }
         } else {
             testExitedLink(me, eventType);
         }
     }
 
     protected void testExitedLink(MouseEvent me, HyperlinkEvent.EventType eventType) {
         if (eventType == null && lastURL != null) {
             eventType = HyperlinkEvent.EventType.EXITED;
             try {
                 hyperlinkUpdate(new HyperlinkEvent(me.getSource(), eventType, new URL(lastURL)));
             } catch (MalformedURLException ex) {
                 Logger.getLogger(TextPaneHyperlinks.class.getName()).log(Level.SEVERE, null, ex);
             }
             lastURL = null;
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
         testLink(me, HyperlinkEvent.EventType.ACTIVATED);
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
         testLink(me, HyperlinkEvent.EventType.EXITED);
     }
 
     @Override
     public void mouseDragged(MouseEvent me) {
     }
 
     @Override
     public void mouseMoved(MouseEvent me) {
         testLink(me, null);
     }
 }
