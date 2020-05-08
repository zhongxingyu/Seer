 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.editor.gui.elementpanels.book;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSplitPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.controllers.book.BookDataControl;
 import es.eucm.eadventure.editor.control.controllers.book.BookParagraphDataControl;
 import es.eucm.eadventure.editor.control.tools.books.AddParagraphElementTool;
 import es.eucm.eadventure.editor.control.tools.books.DeleteParagraphElementTool;
 import es.eucm.eadventure.editor.control.tools.books.MoveParagraphElementDownTool;
 import es.eucm.eadventure.editor.control.tools.books.MoveParagraphElementUpTool;
 import es.eucm.eadventure.editor.gui.DataControlsPanel;
 import es.eucm.eadventure.editor.gui.auxiliar.components.JFiller;
 import es.eucm.eadventure.editor.gui.elementpanels.general.TableScrollPane;
 import es.eucm.eadventure.editor.gui.otherpanels.bookpanels.BookParagraphPreviewPanel;
 
 public class BookParagraphsPanel extends JPanel implements DataControlsPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     public static final int VERTICAL_SPLIT_POSITION = 150;
 
     private BookDataControl dataControl;
 
     private JPanel paragraphsPanel;
 
     private ParagraphsTable paragraphsTable;
 
     private BookParagraphPreviewPanel previewPanel;
 
     private JButton deleteButton;
 
     private JButton moveUpButton;
 
     private JButton moveDownButton;
 
     private JSplitPane infoAndPreview;
 
     public BookParagraphsPanel( BookDataControl dControl ) {
 
         this.dataControl = dControl;
         
         previewPanel = new BookParagraphPreviewPanel( dControl, dControl.getBookParagraphsList( ) );
 
         createParagraphsPanel( previewPanel );
 
         paragraphsTable.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) {
 
             public void valueChanged( ListSelectionEvent e ) {
 
                 updateSelectedParagraph( );
             }
         } );
        
        previewPanel.setMinimumSize( new Dimension( 100, 150 ) );
        paragraphsPanel.setMinimumSize( new Dimension( 0, 100 ) );
 
         //Create a split pane with the two panels: info panel and preview panel
         infoAndPreview = new JSplitPane( JSplitPane.VERTICAL_SPLIT, paragraphsPanel, previewPanel );
         infoAndPreview.setOneTouchExpandable( true );
        infoAndPreview.setDividerLocation( 150 );
        infoAndPreview.setResizeWeight( 0 );
         infoAndPreview.setDividerSize( 10 );
        infoAndPreview.setContinuousLayout( true );
 
         setLayout( new BorderLayout( ) );
         add( infoAndPreview, BorderLayout.CENTER );
 
     }
 
     private void updateSelectedParagraph( ) {
 
         if( paragraphsTable.getSelectedRow( ) < 0 || paragraphsTable.getSelectedRow( ) >= dataControl.getBookParagraphsList( ).getBookParagraphs( ).size( ) ) {
             deleteButton.setEnabled( false );
             moveUpButton.setEnabled( false );
             moveDownButton.setEnabled( false );
         }
         else {
             deleteButton.setEnabled( true );
             moveUpButton.setEnabled( dataControl.getBookParagraphsList( ).getBookParagraphs( ).size( ) > 1 && paragraphsTable.getSelectedRow( ) > 0 );
             moveDownButton.setEnabled( dataControl.getBookParagraphsList( ).getBookParagraphs( ).size( ) > 1 && paragraphsTable.getSelectedRow( ) < paragraphsTable.getRowCount( ) - 1 );
         }
         previewPanel.updatePreview( );
     }
 
     private void createParagraphsPanel( BookParagraphPreviewPanel previewPanel2 ) {
 
         // Create the main panel
         paragraphsPanel = new JPanel( );
         paragraphsPanel.setLayout( new BorderLayout( ) );
 
         // Create the table (CENTER)
         paragraphsTable = new ParagraphsTable( dataControl.getBookParagraphsList( ), previewPanel2 );
         paragraphsTable.addMouseListener( new MouseAdapter( ) {
 
             @Override
             public void mousePressed( MouseEvent e ) {
 
                 // By default the JTable only selects the nodes with the left click of the mouse
                 // With this code, we spread a new call everytime the right mouse button is pressed
                 if( e.getButton( ) == MouseEvent.BUTTON3 ) {
                     // Create new event (with the left mouse button)
                     MouseEvent newEvent = new MouseEvent( e.getComponent( ), e.getID( ), e.getWhen( ), InputEvent.BUTTON1_MASK, e.getX( ), e.getY( ), e.getClickCount( ), e.isPopupTrigger( ) );
 
                     // Take the listeners and make the calls
                     for( MouseListener mouseListener : e.getComponent( ).getMouseListeners( ) )
                         mouseListener.mousePressed( newEvent );
 
                 }
             }
 
             @Override
             public void mouseClicked( MouseEvent evt ) {
 
                 if( evt.getButton( ) == MouseEvent.BUTTON3 ) {
                     JPopupMenu menu = getCompletePopupMenu( );
                     menu.show( evt.getComponent( ), evt.getX( ), evt.getY( ) );
                 }
             }
         } );
         paragraphsPanel.add( new TableScrollPane( paragraphsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );
 
         //Create the buttons panel (SOUTH)
         JPanel buttonsPanel = new JPanel( );
         JButton newButton = new JButton( new ImageIcon( "img/icons/addNode.png" ) );
         newButton.setContentAreaFilled( false );
         newButton.setFocusable( false );
         newButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         newButton.setBorder( BorderFactory.createEmptyBorder( ) );
         newButton.setToolTipText( TC.get( "BookParagraphs.AddParagraph" ) );
         newButton.addMouseListener( new MouseAdapter( ) {
 
             @Override
             public void mouseClicked( MouseEvent evt ) {
 
                 JPopupMenu menu = getAddChildPopupMenu( );
                 menu.show( evt.getComponent( ), evt.getX( ), evt.getY( ) );
             }
         } );
         deleteButton = new JButton( new ImageIcon( "img/icons/deleteNode.png" ) );
         deleteButton.setContentAreaFilled( false );
         deleteButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         deleteButton.setBorder( BorderFactory.createEmptyBorder( ) );
         deleteButton.setFocusable( false );
         deleteButton.setToolTipText( TC.get( "BookParagraphs.DeleteParagraph" ) );
         deleteButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 delete( );
             }
         } );
         moveUpButton = new JButton( new ImageIcon( "img/icons/moveNodeUp.png" ) );
         moveUpButton.setContentAreaFilled( false );
         moveUpButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         moveUpButton.setBorder( BorderFactory.createEmptyBorder( ) );
         moveUpButton.setFocusable( false );
         moveUpButton.setToolTipText( TC.get( "BookParagraphs.MoveParagraphUp" ) );
         moveUpButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 moveUp( );
             }
         } );
         moveDownButton = new JButton( new ImageIcon( "img/icons/moveNodeDown.png" ) );
         moveDownButton.setContentAreaFilled( false );
         moveDownButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         moveDownButton.setBorder( BorderFactory.createEmptyBorder( ) );
         moveDownButton.setFocusable( false );
         moveDownButton.setToolTipText( TC.get( "BookParagraphs.MoveParagraphDown" ) );
         moveDownButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 moveDown( );
             }
         } );
 
         buttonsPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.gridx = 0;
         c.gridy = 0;
         buttonsPanel.add( newButton, c );
         c.gridy = 1;
         buttonsPanel.add( moveUpButton, c );
         c.gridy = 2;
         buttonsPanel.add( moveDownButton, c );
         c.gridy = 4;
         buttonsPanel.add( deleteButton, c );
         c.gridy = 3;
         c.weighty = 2.0;
         c.fill = GridBagConstraints.VERTICAL;
         buttonsPanel.add( new JFiller( ), c );
 
         paragraphsPanel.add( buttonsPanel, BorderLayout.EAST );
     }
 
     /**
      * Returns a popup menu with the add operations.
      * 
      * @return Popup menu with child adding operations
      */
     public JPopupMenu getAddChildPopupMenu( ) {
 
         JPopupMenu addChildPopupMenu = new JPopupMenu( );
 
         // If the element accepts children
         if( dataControl.getBookParagraphsList( ).getAddableElements( ).length > 0 ) {
             // Add an entry in the popup menu for each type of possible child
             for( int type : dataControl.getBookParagraphsList( ).getAddableElements( ) ) {
                 JMenuItem addChildMenuItem = new JMenuItem( TC.get( "TreeNode.AddElement" + type ) );
                 addChildMenuItem.setEnabled( true );
                 addChildMenuItem.addActionListener( new AddElementActionListener( type ) );
                 addChildPopupMenu.add( addChildMenuItem );
             }
         }
 
         // If no element can be added, insert a disabled general option
         else {
             JMenuItem addChildMenuItem = new JMenuItem( TC.get( "TreeNode.AddElement" ) );
             addChildMenuItem.setEnabled( false );
             addChildPopupMenu.add( addChildMenuItem );
         }
 
         return addChildPopupMenu;
     }
 
     /**
      * Returns a popup menu with all the operations.
      * 
      * @return Popup menu with all operations
      */
     public JPopupMenu getCompletePopupMenu( ) {
 
         JPopupMenu completePopupMenu = getAddChildPopupMenu( );
 
         // Separator
         completePopupMenu.addSeparator( );
 
         // Create and add the delete item
         JMenuItem deleteMenuItem = new JMenuItem( TC.get( "TreeNode.DeleteElement" ) );
         deleteMenuItem.setEnabled( deleteButton.isEnabled( ) );
         deleteMenuItem.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 delete( );
             }
         } );
         completePopupMenu.add( deleteMenuItem );
 
         // Separator
         completePopupMenu.addSeparator( );
 
         // Create and add the move up and down item
         JMenuItem moveUpMenuItem = new JMenuItem( TC.get( "TreeNode.MoveElementUp" ) );
         JMenuItem moveDownMenuItem = new JMenuItem( TC.get( "TreeNode.MoveElementDown" ) );
         moveUpMenuItem.setEnabled( moveUpButton.isEnabled( ) );
         moveDownMenuItem.setEnabled( moveDownButton.isEnabled( ) );
         moveUpMenuItem.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 moveUp( );
             }
         } );
         moveDownMenuItem.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 moveDown( );
             }
         } );
         completePopupMenu.add( moveUpMenuItem );
         completePopupMenu.add( moveDownMenuItem );
 
         return completePopupMenu;
     }
 
     private void delete( ) {
 
         BookParagraphDataControl paragraph = dataControl.getBookParagraphsList( ).getBookParagraphs( ).get( paragraphsTable.getSelectedRow( ) );
         Controller.getInstance( ).addTool( new DeleteParagraphElementTool( dataControl, paragraph ) );
         paragraphsTable.clearSelection( );
         paragraphsTable.updateUI( );
     }
 
     private void moveUp( ) {
 
         int selectedRow = paragraphsTable.getSelectedRow( );
         BookParagraphDataControl paragraph = dataControl.getBookParagraphsList( ).getBookParagraphs( ).get( selectedRow );
         Controller.getInstance( ).addTool( new MoveParagraphElementUpTool( dataControl, paragraph ) );
 
         paragraphsTable.getSelectionModel( ).setSelectionInterval( selectedRow - 1, selectedRow - 1 );
         paragraphsTable.updateUI( );
 
     }
 
     private void moveDown( ) {
 
         int selectedRow = paragraphsTable.getSelectedRow( );
         BookParagraphDataControl paragraph = dataControl.getBookParagraphsList( ).getBookParagraphs( ).get( selectedRow );
         Controller.getInstance( ).addTool( new MoveParagraphElementDownTool( dataControl, paragraph ) );
 
         paragraphsTable.getSelectionModel( ).setSelectionInterval( selectedRow + 1, selectedRow + 1 );
         paragraphsTable.updateUI( );
     }
 
     /**
      * This class is the action listener for the add buttons of the popup menus.
      */
     private class AddElementActionListener implements ActionListener {
 
         /**
          * Type of element to be created.
          */
         int type;
 
         /**
          * Constructor
          * 
          * @param type
          *            Type of element the listener must call
          */
         public AddElementActionListener( int type ) {
 
             this.type = type;
         }
 
         public void actionPerformed( ActionEvent e ) {
 
             int selectedRow = paragraphsTable.getSelectedRow( );
 
             Controller.getInstance( ).addTool( new AddParagraphElementTool( dataControl, type, selectedRow ) );
 
             paragraphsTable.clearSelection( );
             if( selectedRow != -1 && selectedRow < dataControl.getBookParagraphsList( ).getBookParagraphs( ).size( ) - 1 )
                 paragraphsTable.getSelectionModel( ).setSelectionInterval( selectedRow + 1, selectedRow + 1 );
 
             paragraphsTable.updateUI( );
 
         }
     }
 
     public void setSelectedItem( List<Searchable> path ) {
 
         if( path.size( ) > 0 ) {
             for( int i = 0; i < dataControl.getBookParagraphsList( ).getBookParagraphs( ).size( ); i++ ) {
                 if( dataControl.getBookParagraphsList( ).getBookParagraphs( ).get( i ) == path.get( path.size( ) - 1 ) )
                     paragraphsTable.changeSelection( i, i, false, false );
             }
         }
     }
 
 }
