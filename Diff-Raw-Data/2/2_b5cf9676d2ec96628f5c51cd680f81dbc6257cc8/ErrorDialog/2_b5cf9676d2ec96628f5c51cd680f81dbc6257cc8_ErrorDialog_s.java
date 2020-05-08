 //$HeadURL$
 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2008 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.views.swing.util;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 
 /**
  * 
  * The <code></code> class TODO add class documentation here.
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * 
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class ErrorDialog extends javax.swing.JDialog {
 
     private static final long serialVersionUID = 913411290864536291L;
 
     private JLabel jLabel1;
 
     private JLabel lbName;
 
     private JPanel jPanel1;
 
     private JButton btClose;
 
     private JScrollPane sc_stacktrace;
 
     private JTextArea ta_message;
 
     private JScrollPane sc_message;
 
     private JTextArea ta_stacktrace;
 
     private JPanel pn_stacktrace;
 
     private JPanel pn_message;
 
     private JTabbedPane tp_messages;
 
     /**
      * 
      * @param name
      * @param message
      */
     public ErrorDialog( String name, String message, String stacktrace ) {
         setTitle( "ERROR" );
         initGUI( name, message, stacktrace );
         setModal( true );
        setLocation( 300, 300 );
         setVisible( true );
         toFront();
         setAlwaysOnTop( true );     
         GuiUtils.addToFrontListener( this );
     }
 
     private void initGUI( String name, String message, String stacktrace ) {
         try {
             {
                 GridBagLayout thisLayout = new GridBagLayout();
                 thisLayout.rowWeights = new double[] { 0.0, 0.0, 0.1 };
                 thisLayout.rowHeights = new int[] { 38, 383, 7 };
                 thisLayout.columnWeights = new double[] { 0.0, 0.1 };
                 thisLayout.columnWidths = new int[] { 74, 20 };
                 getContentPane().setLayout( thisLayout );
                 {
                     jLabel1 = new JLabel( "Name:" );
                     getContentPane().add(
                                           jLabel1,
                                           new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                                                                   GridBagConstraints.HORIZONTAL, new Insets( 0, 10, 0,
                                                                                                              10 ), 0, 0 ) );
                 }
                 {
                     lbName = new JLabel( name );
                     getContentPane().add(
                                           lbName,
                                           new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.HORIZONTAL,
                                                                   new Insets( 0, 0, 0, 8 ), 0, 0 ) );
                 }
                 {
                     jPanel1 = new JPanel();
                     FlowLayout jPanel1Layout = new FlowLayout();
                     jPanel1Layout.setAlignment( FlowLayout.LEFT );
                     getContentPane().add(
                                           jPanel1,
                                           new GridBagConstraints( 0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     jPanel1.setLayout( jPanel1Layout );
                     {
                         btClose = new JButton( "close" );
                         jPanel1.add( btClose );
                         btClose.addActionListener( new ActionListener() {
 
                             public void actionPerformed( ActionEvent e ) {
                                 ErrorDialog.this.dispose();
                             }
 
                         } );
                     }
                 }
                 {
                     tp_messages = new JTabbedPane();
                     getContentPane().add(
                                           tp_messages,
                                           new GridBagConstraints( 0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                                   GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0,
                                                                   0 ) );
                     {
                         pn_message = new JPanel();
                         BorderLayout pn_messageLayout = new BorderLayout();
                         pn_message.setLayout( pn_messageLayout );
                         tp_messages.addTab( "message", null, pn_message, null );
                         {
                             sc_message = new JScrollPane();
                             pn_message.add( sc_message, BorderLayout.CENTER );
                             {
                                 ta_message = new JTextArea( message );
                                 sc_message.setViewportView( ta_message );
                             }
                         }
                     }
                     {
                         pn_stacktrace = new JPanel();
                         BorderLayout pn_stacktraceLayout = new BorderLayout();
                         pn_stacktrace.setLayout( pn_stacktraceLayout );
                         tp_messages.addTab( "stacktrace", null, pn_stacktrace, null );
                         {
                             sc_stacktrace = new JScrollPane();
                             pn_stacktrace.add( sc_stacktrace, BorderLayout.CENTER );
                             {
                                 ta_stacktrace = new JTextArea( stacktrace );
                                 sc_stacktrace.setViewportView( ta_stacktrace );
                             }
                         }
                     }
                 }
             }
             this.setSize( 394, 490 );
         } catch ( Exception e ) {
             e.printStackTrace();
         }
     }
 
 }
