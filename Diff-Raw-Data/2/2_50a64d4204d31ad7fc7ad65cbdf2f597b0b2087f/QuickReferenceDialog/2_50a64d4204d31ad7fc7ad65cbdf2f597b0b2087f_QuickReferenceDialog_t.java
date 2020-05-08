 /*
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package org.cytoscape.coreplugin.cpath.ui;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Displays a Quick Reference Search Help Page.
  *
  * @author Ethan Cerami.
  */
 public class QuickReferenceDialog implements ActionListener {
     private static final int WIDTH = 400;
     private static final int HEIGHT = 450;
     private JFrame parent;
     private JFrame helpFrame;
 
     /**
      * Constructor.
      *
      * @param parent Parent Frame.
      */
     public QuickReferenceDialog (JFrame parent) {
         this.parent = parent;
         init();
     }
 
     /**
      * Users has requested that we show the Quick Reference Dialog.
      *
      * @param e ActionEvent Object.
      */
     public void actionPerformed (ActionEvent e) {
         if (parent != null) {
             Point point = parent.getLocation();
             Dimension size = parent.getSize();
             int x = (int) (point.getX() + size.getWidth() / 2 - WIDTH / 2);
             int y = (int) (point.getY() + size.getHeight() / 2 - HEIGHT / 2);
             helpFrame.setLocation(x, y);
         }
         helpFrame.setVisible(true);
     }
 
     private void init () {
         helpFrame = new JFrame("Quick Reference Manual");
         Container contentPane = helpFrame.getContentPane();
         contentPane.setLayout(new BorderLayout());
 
         JEditorPane htmlPane = new JEditorPane();
         EmptyBorder border = new EmptyBorder(5, 5, 5, 5);
         htmlPane.setBorder(border);
         htmlPane.setContentType("text/html");
         htmlPane.setEditable(false);
         htmlPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
 
         String html = this.getAboutHtml();
         htmlPane.setText(html);
         htmlPane.setCaretPosition(0);
 
         JScrollPane scrollPane = new JScrollPane(htmlPane);
         contentPane.add(scrollPane, BorderLayout.CENTER);
 
         //  Pack it, but don't show it yet.
         helpFrame.pack();
     }
 
     private String getAboutHtml () {
         StringBuffer html = new StringBuffer();
         html.append("<TABLE WIDTH=100%><TR BGCOLOR=#DDDDDD><TD>");
         html.append("<FONT FACE=ARIAL SIZE=+1>");
         html.append("Quick Reference Manual");
         html.append("</FONT>");
         html.append("</TD></TR></TABLE>");
         html.append("<FONT FACE=ARIAL>");
 
         html.append("<P><U>About cPath</U>");
         html.append("<P>cPath is a database and software suite for storing, visualizing, and "
                 + "analyzing biological pathways."
                 + "<P>A demo version of cPath is currently available at "
                 + "http://www.cbio.mskcc.org/cpath/."
                 + "<P>The demo site contains all public data from IntACT "
                 + "and MINT.");
 
         html.append("<P><U>About the cPath PlugIn</U>");
         html.append("<P>The cPath PlugIn provides interactive access to the "
                 + "the cPath demo site. Cytoscape users can query cPath, download "
                 + "matching interactions and view them within Cytoscape.");
 
         html.append("<P><U>Search Examples:</U>");
         html.append("<P>dna repair");
         html.append("<BR>-- Finds all records that contains the words "
                 + " dna or repair.");
         html.append("<P>dna AND repair");
         html.append("<BR>-- Finds all records the contain the words "
                 + " dna AND repair.");
 
         html.append("<P>dna NOT repair");
         html.append("<BR>-- Finds all records that contain the word "
                 + "dna, but do not contain the word repair.");
 
         html.append("<P>\"dna repair\"");
         html.append("<BR>-- Finds all records containing the exact "
                 + " text:  \"dna repair\".");
 
         html.append("<P>regulat*");
         html.append("<BR>-- Finds all records begin with the wildcard:   "
                 + "\"regulat\". This will match against records, such as "
                 + "regulate, regulatory, etc.");
 
         html.append("</FONT>");
 
         html.append("<P><HR><FONT FACE=ARIAL SIZE=-1>");
        html.append("Copyright 2004-2007 Memorial Sloan-Kettering Cancer Center.");
         html.append("</FONT>");
         return html.toString();
     }
 
     /**
      * Main Method (used to testing purposes).
      *
      * @param args Command Line Arguments.
      */
     public static void main (String args[]) {
         QuickReferenceDialog frame = new QuickReferenceDialog(null);
     }
 }
