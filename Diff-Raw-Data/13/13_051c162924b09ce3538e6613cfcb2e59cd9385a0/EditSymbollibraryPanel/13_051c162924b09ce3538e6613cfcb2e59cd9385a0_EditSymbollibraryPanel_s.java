 //$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2010 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.igeo.views.swing.style;
 
 import static org.deegree.igeo.i18n.Messages.get;
 
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.igeo.settings.GraphicOptions;
 import org.deegree.igeo.style.model.GraphicSymbol;
 import org.deegree.igeo.views.swing.util.GenericFileChooser;
 import org.deegree.igeo.views.swing.util.GenericFileChooser.FILECHOOSERTYPE;
 import org.deegree.igeo.views.swing.util.IGeoFileFilter;
 
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * Manage SymbolDefinitions of {@link GraphicOptions}
  * 
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 public class EditSymbollibraryPanel extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = -6882947699218187151L;
 
     private static final ILogger LOG = LoggerFactory.getLogger( EditSymbollibraryPanel.class );
 
     private static List<String> columns = new ArrayList<String>();
 
     private static String setNS = "http://www.deegree.org/settings";
 
     private final GraphicOptions graphicOptions;
 
     private JTextField newSymbolNameTF;
 
     private JButton selectFileBt;
 
     private JTextField newSymbolTF;
 
     private JButton addSymbolBt;
 
     private JButton removeSymbolBt;
 
     private JButton importLibBt;
 
     private JButton exportLibBt;
 
     private JTable symbolTable;
 
     private SymbolTableModel symbolTableModel;
 
     static {
         columns.add( get( "$MD11839" ) );
         columns.add( get( "$MD11840" ) );
         columns.add( get( "$MD11841" ) );
     }
 
     public EditSymbollibraryPanel( GraphicOptions graphicOptions ) {
         this.graphicOptions = graphicOptions;
         init();
     }
 
     private void init() {
         // init
         // add own
         newSymbolNameTF = new JTextField();
         selectFileBt = new JButton( get( "$MD10771" ) );
         selectFileBt.addActionListener( this );
         newSymbolTF = new JTextField();
         addSymbolBt = new JButton( get( "$MD10772" ) );
         addSymbolBt.addActionListener( this );
 
         // remove own
         removeSymbolBt = new JButton( get( "$MD11165" ) );
         removeSymbolBt.addActionListener( this );
         removeSymbolBt.setEnabled( false );
 
         exportLibBt = new JButton( get( "$MD11838" ) );
         exportLibBt.setEnabled( false );
         exportLibBt.addActionListener( this );
 
         importLibBt = new JButton( get( "$MD11843" ) );
         importLibBt.addActionListener( this );
 
         symbolTableModel = new SymbolTableModel();
         symbolTable = new JTable( symbolTableModel );
         symbolTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
         TableColumn iconCol = symbolTable.getColumnModel().getColumn( 0 );
         iconCol.setPreferredWidth( 50 );
         iconCol.setCellRenderer( new SymbolRenderer() );
         symbolTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 75 );
         symbolTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 500 );
         symbolTable.getColumnModel().getColumn( 2 ).setCellRenderer( new URLCellRenderer() );
         symbolTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
             @Override
             public void valueChanged( ListSelectionEvent arg0 ) {
                 if ( symbolTable.getSelectedRowCount() > 0 ) {
                     removeSymbolBt.setEnabled( true );
                     exportLibBt.setEnabled( true );
                 } else {
                     removeSymbolBt.setEnabled( false );
                     exportLibBt.setEnabled( false );
                 }
 
             }
         } );
 
         JScrollPane scrollPane = new JScrollPane( symbolTable );
 
         // layout
         FormLayout fl = new FormLayout(
                                         "left:$rgap, left:min, $ugap, fill:default:grow(0.5), $ugap, fill:default:grow(0.5)",
                                         "$cpheight, 150dlu, $btheight, "
                                                                 + "$sepheight, $cpheight, $btheight, $cpheight, $btheight,"
                                                                 + "$sepheight, $btheight" );
         DefaultFormBuilder builder = new DefaultFormBuilder( fl );
         CellConstraints cc = new CellConstraints();
 
         builder.addSeparator( get( "$MD11842" ), cc.xyw( 1, 1, 6 ) );
         builder.add( scrollPane, cc.xyw( 2, 2, 5, CellConstraints.FILL, CellConstraints.CENTER ) );
 
         builder.add( buildTableButtonBar(), cc.xyw( 1, 3, 6, CellConstraints.CENTER, CellConstraints.CENTER ) );
 
         builder.addSeparator( get( "$MD10775" ), cc.xyw( 1, 4, 6 ) );
         builder.addLabel( get( "$MD10776" ), cc.xy( 2, 5 ) );
         builder.add( newSymbolNameTF, cc.xyw( 4, 5, 3 ) );
 
         builder.addLabel( get( "$MD10777" ), cc.xy( 2, 6 ) );
         builder.add( selectFileBt, cc.xy( 4, 6, CellConstraints.RIGHT, CellConstraints.CENTER ) );
         builder.add( newSymbolTF, cc.xyw( 2, 7, 5 ) );
         builder.add( addSymbolBt, cc.xyw( 2, 8, 5, CellConstraints.CENTER, CellConstraints.CENTER ) );
 
         builder.addSeparator( get( "$MD11837" ), cc.xyw( 1, 9, 6 ) );
         builder.add( importLibBt, cc.xyw( 2, 10, 5, CellConstraints.CENTER, CellConstraints.CENTER ) );
 
         add( builder.getPanel() );
     }
 
     private JPanel buildTableButtonBar() {
         ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
         bbBuilder.addFixed( exportLibBt );
         bbBuilder.addUnrelatedGap();
         bbBuilder.addFixed( removeSymbolBt );
         return bbBuilder.getPanel();
     }
 
     private void selectFile() {
         File file = GenericFileChooser.showOpenDialog( FILECHOOSERTYPE.image,
                                                        null,
                                                        this,
                                                       Preferences.systemNodeForPackage( EditSymbollibraryPanel.class ),
                                                        "lastSelectSymbol", IGeoFileFilter.IMAGES );
         if ( file != null ) {
             try {
                 newSymbolTF.setText( file.toURI().toURL().toExternalForm() );
             } catch ( MalformedURLException e ) {
                 // should never happen
                 e.printStackTrace();
             }
         }
     }
 
     private void addSymbol() {
         String url = newSymbolTF.getText();
         String name = newSymbolNameTF.getText();
         if ( name == null || name.length() == 0 ) {
             JOptionPane.showMessageDialog( this, get( "$MD11856" ), get( "$MD11857" ), JOptionPane.INFORMATION_MESSAGE );
             return;
         } else
             try {
                 if ( graphicOptions.getSymboldefinition( name ) != null ) {
                     int showConfirmDialog = JOptionPane.showConfirmDialog( this, get( "$MD11858" ), get( "$MD11859" ),
                                                                            JOptionPane.YES_NO_OPTION );
                     if ( showConfirmDialog == JOptionPane.NO_OPTION ) {
                         return;
                     }
                 }
             } catch ( MalformedURLException e ) {
                 LOG.logInfo( "Could not read graphicOptions: {}", e.getMessage() );
             }
 
         boolean invalidURL = false;
         if ( url != null && url.length() > 0 ) {
             try {
                 URL u = new URL( url );
                 GraphicSymbol newSymbol = new GraphicSymbol( name, u );
                 if ( newSymbol.getFormat() != null ) {
                     graphicOptions.addSymbolDefinition( name, u.toExternalForm() );
                     newSymbolTF.setText( "" );
                     newSymbolNameTF.setText( "" );
                     symbolTableModel.updateSymbols();
                     symbolTableModel.fireTableDataChanged();
                 } else {
                     JOptionPane.showMessageDialog( this, get( "$MD10780" ), get( "$MD10781" ),
                                                    JOptionPane.INFORMATION_MESSAGE );
                 }
             } catch ( MalformedURLException e ) {
                 invalidURL = true;
             }
         } else {
             invalidURL = true;
         }
         if ( invalidURL ) {
             JOptionPane.showMessageDialog( this, get( "$MD10782" ), get( "$MD10783" ), JOptionPane.INFORMATION_MESSAGE );
         }
     }
 
     private void removeSymbols() {
         if ( symbolTable.getSelectedRows().length > 0 ) {
             int result = JOptionPane.showOptionDialog( this, get( "$MD11168", symbolTable.getSelectedRows().length ),
                                                        get( "$MD11167" ), JOptionPane.OK_CANCEL_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE, null, null, null );
             if ( result == JOptionPane.OK_OPTION ) {
                 try {
                     List<GraphicSymbol> symbolsToRemove = symbolTableModel.getRows( symbolTable.getSelectedRows() );
                     for ( GraphicSymbol gs : symbolsToRemove ) {
                         graphicOptions.removeSymbolDefinition( gs.getName() );
                     }
                     symbolTableModel.updateSymbols();
                     symbolTableModel.fireTableDataChanged();
                 } catch ( MalformedURLException e ) {
                     JOptionPane.showMessageDialog( this, get( "$MD11169" ), get( "$DI10017" ),
                                                    JOptionPane.INFORMATION_MESSAGE );
                 }
             }
         }
     }
 
     private void importSymbols() {
         File file = GenericFileChooser.showOpenDialog( FILECHOOSERTYPE.local,
                                                        null,
                                                        this,
                                                        Preferences.systemNodeForPackage( EditSymbollibraryPanel.class ),
                                                        "lastImportedSymbol", IGeoFileFilter.XML );
         if ( file != null ) {
             FileInputStream fis = null;
             XMLStreamReader reader = null;
             try {
                 fis = new FileInputStream( file );
                 reader = XMLInputFactory.newInstance().createXMLStreamReader( fis, "UTF-8" );
                 reader.nextTag();
                 reader.require( XMLStreamReader.START_ELEMENT, setNS, "SymbolDefinitions" );
                 reader.nextTag();
                 while ( reader.isStartElement() && new QName( setNS, "Graphic", "set" ).equals( reader.getName() ) ) {
                     String name = reader.getAttributeValue( null, "name" );
                     String url = reader.getAttributeValue( null, "file" );
                     addSymbol( name, url, 1 );
                     do {
                         reader.next();
                     } while ( !reader.isStartElement() && reader.getEventType() != XMLStreamReader.END_DOCUMENT );
                 }
                 symbolTableModel.updateSymbols();
                 symbolTableModel.fireTableDataChanged();
             } catch ( FileNotFoundException e ) {
                 JOptionPane.showMessageDialog( this, get( "$MD11846" ), get( "$DI10017" ), JOptionPane.ERROR_MESSAGE );
             } catch ( XMLStreamException e ) {
                 e.printStackTrace();
                 JOptionPane.showMessageDialog( this, get( "$MD11847", e.getMessage() ), get( "$DI10017" ),
                                                JOptionPane.ERROR_MESSAGE );
             } finally {
                 if ( reader != null )
                     try {
                         reader.close();
                         fis.close();
                     } catch ( XMLStreamException e ) {
                     } catch ( IOException e ) {
                     }
             }
         }
     }
 
     private void addSymbol( String name, String url, int index ) {
         try {
             GraphicSymbol symbol = graphicOptions.getSymboldefinition( name );
             if ( symbol != null && !symbol.getUrl().equals( new URL( url ) ) ) {
                 String tmpName = name + " (" + index + ')';
                 symbol = graphicOptions.getSymboldefinition( tmpName );
                 while ( symbol != null && !symbol.getUrl().equals( new URL( url ) ) ) {
                     tmpName = name + " (" + index++ + ')';
                 }
                 addSymbol( tmpName, url, index++ );
             } else {
                 graphicOptions.addSymbolDefinition( name, url );
             }
 
         } catch ( MalformedURLException e ) {
             LOG.logInfo( "Could not resolve URL for symbol with name: " + name );
         }
     }
 
     private void exportSymbols() {
         if ( symbolTable.getSelectedRows().length > 0 ) {
             File file = GenericFileChooser.showSaveDialog( FILECHOOSERTYPE.local,
                                                            null,
                                                            this,
                                                            Preferences.systemNodeForPackage( EditSymbollibraryPanel.class ),
                                                            "lastExportedSymbol", IGeoFileFilter.XML );
             if ( file != null ) {
                 FileOutputStream fos = null;
                 XMLStreamWriter writer = null;
                 try {
                     fos = new FileOutputStream( file );
                     writer = XMLOutputFactory.newInstance().createXMLStreamWriter( fos, "UTF-8" );
                     writer.writeStartDocument( "UTF-8", "1.0" );
                     writer.writeStartElement( "set", "SymbolDefinitions", setNS );
 
                     writer.writeNamespace( "set", setNS );
 
                     List<GraphicSymbol> rows = symbolTableModel.getRows( symbolTable.getSelectedRows() );
                     for ( GraphicSymbol gs : rows ) {
                         writer.writeStartElement( "set", "Graphic", setNS );
                         writer.writeAttribute( "name", gs.getName() );
                         writer.writeAttribute( "file", gs.getUrl().toExternalForm() );
                         writer.writeEndElement();
                     }
                     writer.writeEndElement();
                 } catch ( FileNotFoundException e ) {
                     JOptionPane.showMessageDialog( this, get( "$MD11844" ), get( "$DI10017" ),
                                                    JOptionPane.ERROR_MESSAGE );
                 } catch ( XMLStreamException e ) {
                     JOptionPane.showMessageDialog( this, get( "$MD11845", e.getMessage() ), get( "$DI10017" ),
                                                    JOptionPane.ERROR_MESSAGE );
                 } finally {
                     if ( writer != null )
                         try {
                             writer.close();
                             fos.close();
                         } catch ( XMLStreamException e ) {
                         } catch ( IOException e ) {
                         }
                 }
             }
         }
     }
 
     public void actionPerformed( ActionEvent e ) {
         if ( e.getSource() == selectFileBt ) {
             selectFile();
         } else if ( e.getSource() == addSymbolBt ) {
             addSymbol();
         } else if ( e.getSource() == removeSymbolBt ) {
             removeSymbols();
         } else if ( e.getSource() == exportLibBt ) {
             exportSymbols();
         } else if ( e.getSource() == importLibBt ) {
             importSymbols();
         }
     }
 
     @Override
     public String toString() {
         return get( "$MD11836" );
     }
 
     private class SymbolTableModel extends AbstractTableModel {
 
         private static final long serialVersionUID = 4512703217079702243L;
 
         private List<GraphicSymbol> symbols = new ArrayList<GraphicSymbol>();
 
         public SymbolTableModel() {
             updateSymbols();
         }
 
         private List<GraphicSymbol> getRows( int[] selectedRows ) {
             List<GraphicSymbol> gs = new ArrayList<GraphicSymbol>();
             for ( int i : selectedRows ) {
                 gs.add( symbols.get( i ) );
             }
             return gs;
 
         }
 
         private void updateSymbols() {
             if ( graphicOptions != null ) {
                 try {
                     symbols.clear();
                     Map<String, GraphicSymbol> symbolDefinitions = graphicOptions.getSymbolDefinitions();
                     symbols.addAll( symbolDefinitions.values() );
                     Collections.sort( symbols );
                 } catch ( MalformedURLException e ) {
                     JOptionPane.showMessageDialog( null, get( "$MD10789" ), get( "$DI10017" ),
                                                    JOptionPane.ERROR_MESSAGE );
                 }
             }
         }
 
         @Override
         public int getColumnCount() {
             return columns.size();
         }
 
         @Override
         public String getColumnName( int column ) {
             return columns.get( column );
         }
 
         @Override
         public int getRowCount() {
             return symbols.size();
         }
 
         @Override
         public Object getValueAt( int row, int col ) {
             GraphicSymbol graphicSymbol = symbols.get( row );
             switch ( col ) {
             case 0:
                 return graphicSymbol.getAsImage();
             case 1:
                 return graphicSymbol.getName();
             case 2:
                 return graphicSymbol.getUrl();
             }
             return null;
         }
     }
 
     private class SymbolRenderer extends DefaultTableCellRenderer {
 
         private static final long serialVersionUID = -7866038248000655247L;
 
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column ) {
             JLabel label = new JLabel();
             if ( value != null && value instanceof Image ) {
                 label = new JLabel( new ImageIcon( (Image) value ) );
             }
             if ( isSelected ) {
                 label.setOpaque( true );
                 label.setBackground( table.getSelectionBackground() );
                 label.setForeground( table.getSelectionForeground() );
 
             }
             return label;
         }
     }
 
     private class URLCellRenderer extends DefaultTableCellRenderer {
 
         private static final long serialVersionUID = -1533887940199153148L;
 
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column ) {
             if ( value instanceof URL ) {
                 setToolTipText( ( (URL) value ).toExternalForm().substring( ( (URL) value ).toExternalForm().lastIndexOf( "/" ) + 1 ) );
             }
             return super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
         }
     }
 
 }
