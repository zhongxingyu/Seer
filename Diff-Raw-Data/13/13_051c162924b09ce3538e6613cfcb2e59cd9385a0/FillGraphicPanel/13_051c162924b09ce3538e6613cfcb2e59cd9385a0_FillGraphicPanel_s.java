 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
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
 
 package org.deegree.igeo.views.swing.style;
 
 import static org.deegree.igeo.i18n.Messages.get;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.settings.GraphicOptions;
 import org.deegree.igeo.style.model.FillPattern;
 import org.deegree.igeo.style.model.GraphicSymbol;
 import org.deegree.igeo.style.model.SldValues;
 import org.deegree.igeo.views.swing.style.renderer.SymbolRenderer;
 import org.deegree.igeo.views.swing.util.GenericFileChooser;
 import org.deegree.igeo.views.swing.util.IGeoFileFilter;
 import org.deegree.igeo.views.swing.util.GenericFileChooser.FILECHOOSERTYPE;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * <code>FillGraphicPanel</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class FillGraphicPanel extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = -4817010814590110063L;
 
     private JComboBox fixedGraphicFillCB;
 
     private JTextField fixedGraphicFillUrlTF;
 
     private JTextField fixedGraphicFillNameTF;
 
     private JButton addNewGraphicBt;
 
     private JButton selectGraphicBt;
 
     private JButton removeGraphicBt;
 
     private JComboBox graphicToRemoveCB;
 
     private GraphicOptions graphicOptions;
 
     private ChangeListener changelistener;
 
     private JSpinner sizeSpinner;
 
     private JRadioButton defaultSizeRB;
 
     private JRadioButton setSizeRB;
 
     /**
      * @param graphicOptions
      *            the graphic options
      */
     public FillGraphicPanel( GraphicOptions graphicOptions ) {
         this( null, graphicOptions );
     }
 
     /**
      * @param changelistener
      *            the change listener to inform, when selected fill graphic has changed
      * @param graphicOptions
      *            the graphic options
      */
     public FillGraphicPanel( ChangeListener changelistener, GraphicOptions graphicOptions ) {
         this.changelistener = changelistener;
         this.graphicOptions = graphicOptions;
         setLayout( new BorderLayout() );
         init();
         updateGraphicComboBoxes();
     }
 
     /**
      * selects the entry with the same url if available, otherwise the URL will be added as fillGraphic to the settings
      * 
      * @param onlineResource
      *            the url of the graphic
      * @param size
      *            the size of the graphic
      */
     public void setValue( URL onlineResource, double size ) {
         if ( Double.isNaN( size ) || size < 0 ) {
             defaultSizeRB.setSelected( true );
         } else {
             sizeSpinner.setValue( size );
             setSizeRB.setSelected( true );
         }
         if ( onlineResource != null ) {
             boolean isInList = false;
             for ( int i = 0; i < fixedGraphicFillCB.getItemCount(); i++ ) {
                 Object item = fixedGraphicFillCB.getItemAt( i );
                 if ( item instanceof GraphicSymbol
                      && ( (GraphicSymbol) item ).getUrl().toExternalForm().equals( onlineResource.toExternalForm() ) ) {
                     fixedGraphicFillCB.setSelectedIndex( i );
                     isInList = true;
                     break;
                 }
             }
             if ( !isInList ) {
                 try {
                     graphicOptions.addFillGraphicDefinition( onlineResource.getFile(), onlineResource.toExternalForm() );
                     updateGraphicComboBoxes();
                     fixedGraphicFillCB.setSelectedItem( graphicOptions.getFillGraphicDefinitions().get(
                                                                                                         onlineResource.getFile() ) );
                 } catch ( MalformedURLException e ) {
                     JOptionPane.showMessageDialog( this, get( "$MD10832" ), get( "$DI10017" ),
                                                    JOptionPane.ERROR_MESSAGE );
                 }
             }
         }
     }
 
     /**
      * set the color and transparancy of fill patterns
      * 
      * @param color
      *            the color indicating the color value
      * @param transColor
      *            the color indication the transparency
      * 
      */
     public void createAndUpdateColor( Color color, Color transColor ) {
         Color newColor = new Color( color.getRed(), color.getGreen(), color.getBlue(), transColor.getAlpha() );
         for ( int i = 0; i < fixedGraphicFillCB.getItemCount(); i++ ) {
             Object item = fixedGraphicFillCB.getItemAt( i );
             if ( item instanceof FillPattern ) {
                 ( (FillPattern) item ).setColor( newColor );
             }
         }
         fireFillGraphicChangedEvent();
     }
 
     /**
      * @return the selected graphic symbol, or null, if no graphic symbol is selected
      */
     public GraphicSymbol getValue() {
         Object selectedItem = fixedGraphicFillCB.getSelectedItem();
         if ( selectedItem != null && selectedItem instanceof GraphicSymbol ) {
             if ( defaultSizeRB.isSelected() ) {
                 ( (GraphicSymbol) selectedItem ).setSize( Double.NaN );
             } else {
                 ( (GraphicSymbol) selectedItem ).setSize( (Double) sizeSpinner.getValue() );
             }
             return (GraphicSymbol) selectedItem;
         }
         return null;
     }
 
     private void init() {
         // init
 
         // well known
         fixedGraphicFillCB = new JComboBox();
         fixedGraphicFillCB.setRenderer( new SymbolRenderer() );
         fixedGraphicFillCB.addActionListener( new ActionListener() {
             public void actionPerformed( ActionEvent e ) {
                 fireFillGraphicChangedEvent();
             }
         } );
 
         // size
 
         defaultSizeRB = new JRadioButton( get( "$MD11113" ) );
         defaultSizeRB.addActionListener( new ActionListener() {
             public void actionPerformed( ActionEvent arg0 ) {
                 fireFillGraphicChangedEvent();
             }
         } );
 
         setSizeRB = new JRadioButton();
         setSizeRB.addActionListener( new ActionListener() {
             public void actionPerformed( ActionEvent arg0 ) {
                 fireFillGraphicChangedEvent();
             }
         } );
 
         ButtonGroup bg = new ButtonGroup();
         bg.add( defaultSizeRB );
         bg.add( setSizeRB );
 
         defaultSizeRB.setSelected( true );
 
         SpinnerModel model = new SpinnerNumberModel( 6.0, 1.0, Integer.MAX_VALUE, 1.0 );
         sizeSpinner = new JSpinner( model );
         sizeSpinner.setMaximumSize( new Dimension( StyleDialogUtils.PREF_COMPONENT_WIDTH,
                                                    StyleDialogUtils.PREF_ONELINE_COMPONENT_HEIGHT ) );
         sizeSpinner.setMinimumSize( new Dimension( StyleDialogUtils.PREF_COMPONENT_WIDTH,
                                                    StyleDialogUtils.PREF_ONELINE_COMPONENT_HEIGHT ) );
         sizeSpinner.setPreferredSize( new Dimension( StyleDialogUtils.PREF_COMPONENT_WIDTH,
                                                      StyleDialogUtils.PREF_ONELINE_COMPONENT_HEIGHT ) );
         sizeSpinner.addChangeListener( new javax.swing.event.ChangeListener() {
             public void stateChanged( ChangeEvent e ) {
                 setSizeRB.setSelected( true );
                 fireFillGraphicChangedEvent();
             }
         } );
 
         // add own
         selectGraphicBt = new JButton( get( "$MD10753" ) );
         selectGraphicBt.addActionListener( this );
         fixedGraphicFillUrlTF = new JTextField();
         addNewGraphicBt = new JButton( get( "$MD10754" ) );
         addNewGraphicBt.addActionListener( this );
         fixedGraphicFillNameTF = new JTextField();
 
         // remove own
         graphicToRemoveCB = new JComboBox();
         graphicToRemoveCB.setRenderer( new SymbolRenderer() );
         removeGraphicBt = new JButton( get( "$MD11161" ) );
         removeGraphicBt.addActionListener( this );
 
         // layout
         FormLayout fl = new FormLayout(
                                         "left:$rgap, left:min, left:min, fill:default:grow(1), min",
                                         "$sepheight, $cpheight, "
                                                                 + "$sepheight, $cpheight, $cpheight, "
                                                                 + "$sepheight, $cpheight, $btheight, $cpheight, $btheight, "
                                                                 + "$sepheight, $cpheight, $btheight" );
 
         DefaultFormBuilder builder = new DefaultFormBuilder( fl );
         CellConstraints cc = new CellConstraints();
 
         builder.addSeparator( get( "$MD10756" ), cc.xyw( 1, 1, 5 ) );
         builder.add( fixedGraphicFillCB, cc.xyw( 2, 2, 4 ) );
 
         builder.addSeparator( get( "$MD11112" ), cc.xyw( 1, 3, 4 ) );
         builder.add( defaultSizeRB, cc.xyw( 2, 4, 3 ) );
         builder.add( setSizeRB, cc.xy( 2, 5 ) );
         builder.add( sizeSpinner, cc.xy( 3, 5 ) );
 
         builder.addSeparator( get( "$MD10757" ), cc.xyw( 1, 6, 5 ) );
         builder.addLabel( get( "$MD10758" ), cc.xyw( 2, 7, 2 ) );
         builder.add( fixedGraphicFillNameTF, cc.xyw( 4, 7, 2 ) );
 
         builder.addLabel( get( "$MD10759" ), cc.xyw( 2, 8, 2 ) );
         builder.add( selectGraphicBt, cc.xyw( 4, 8, 2, CellConstraints.RIGHT, CellConstraints.CENTER ) );
         builder.add( fixedGraphicFillUrlTF, cc.xyw( 2, 9, 4 ) );
         builder.add( addNewGraphicBt, cc.xyw( 2, 10, 4, CellConstraints.CENTER, CellConstraints.CENTER ) );
 
         builder.addSeparator( get( "$MD11160" ), cc.xyw( 1, 11, 5 ) );
         builder.add( graphicToRemoveCB, cc.xyw( 2, 12, 4 ) );
         builder.add( removeGraphicBt, cc.xyw( 2, 13, 4, CellConstraints.CENTER, CellConstraints.CENTER ) );
 
         add( builder.getPanel(), BorderLayout.CENTER );
     }
 
     private void fireFillGraphicChangedEvent() {
         if ( changelistener != null ) {
             if ( fixedGraphicFillCB.getSelectedItem() instanceof GraphicSymbol ) {
                 GraphicSymbol value = (GraphicSymbol) fixedGraphicFillCB.getSelectedItem();
                 if ( defaultSizeRB.isSelected() ) {
                     value.setSize( Double.NaN );
                 } else {
                     value.setSize( (Double) sizeSpinner.getValue() );
                 }
                 changelistener.valueChanged( new FillGraphicChangedEvent( value ) );
             } else {
                 changelistener.valueChanged( new FillGraphicChangedEvent( null ) );
             }
         }
     }
 
     private void updateGraphicComboBoxes() {
         fixedGraphicFillCB.removeAllItems();
         fixedGraphicFillCB.addItem( get( "$MD10752" ) );
 
         graphicToRemoveCB.removeAllItems();
 
         for ( FillPattern fillPattern : SldValues.getFillPatterns() ) {
             fixedGraphicFillCB.addItem( fillPattern );
         }
 
         try {
             Map<String, GraphicSymbol> fillGraphics = graphicOptions.getFillGraphicDefinitions();
             List<GraphicSymbol> values = new ArrayList<GraphicSymbol>();
             values.addAll( fillGraphics.values() );
             Collections.sort( (List<GraphicSymbol>) values );
             for ( GraphicSymbol fillGraphicName : values ) {
                 fixedGraphicFillCB.addItem( fillGraphicName );
                 graphicToRemoveCB.addItem( fillGraphicName );
             }
         } catch ( MalformedURLException e1 ) {
             JOptionPane.showMessageDialog( this, get( "$MD10831" ), get( "$MD10781" ), JOptionPane.INFORMATION_MESSAGE );
         }
         if ( graphicToRemoveCB.getItemCount() > 0 ) {
             removeGraphicBt.setEnabled( true );
         } else {
             removeGraphicBt.setEnabled( false );
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // ACTIONLISTENER
     // //////////////////////////////////////////////////////////////////////////////
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed( ActionEvent event ) {
         if ( event.getSource() == selectGraphicBt ) {
             File file = GenericFileChooser.showOpenDialog( FILECHOOSERTYPE.image, null, this,
                                                           Preferences.systemNodeForPackage( FillGraphicPanel.class ),
                                                            "lastSelectGraphic", IGeoFileFilter.IMAGES );
             if ( file != null ) {
                 try {
                     fixedGraphicFillUrlTF.setText( file.toURI().toURL().toExternalForm() );
                 } catch ( MalformedURLException e ) {
                     // should never happen
                     e.printStackTrace();
                 }
             }
         } else if ( event.getSource() == addNewGraphicBt ) {
             addGraphic();
         } else if ( event.getSource() == removeGraphicBt ) {
             removeGraphic();
         }
 
     }
 
     private void addGraphic() {
         String url = fixedGraphicFillUrlTF.getText();
         String name = fixedGraphicFillNameTF.getText();
         boolean invalidURL = false;
         if ( url != null && url.length() > 0 ) {
             try {
                 URL u = new URL( url );
                 GraphicSymbol newSymbol = new GraphicSymbol( name, u );
                 if ( newSymbol.getFormat() != null ) {
                     graphicOptions.addFillGraphicDefinition( name, u.toExternalForm() );
                     updateGraphicComboBoxes();
                     fixedGraphicFillCB.setSelectedItem( graphicOptions.getFillGraphicDefinitions().get( name ) );
 
                     fixedGraphicFillUrlTF.setText( "" );
                     fixedGraphicFillNameTF.setText( "" );
                 } else {
                     JOptionPane.showMessageDialog( this, get( "$MD10762" ), get( "$MD10763" ),
                                                    JOptionPane.INFORMATION_MESSAGE );
                 }
             } catch ( MalformedURLException e ) {
                 invalidURL = true;
             }
         } else {
             invalidURL = true;
         }
         if ( invalidURL ) {
             JOptionPane.showMessageDialog( this, get( "$MD10764" ), get( "$MD10765" ), JOptionPane.INFORMATION_MESSAGE );
         }
 
     }
 
     private void removeGraphic() {
         if ( graphicToRemoveCB.getSelectedItem() != null
              && !( graphicToRemoveCB.getSelectedItem() instanceof FillPattern ) ) {
             GraphicSymbol gs = (GraphicSymbol) graphicToRemoveCB.getSelectedItem();
             int result = JOptionPane.showOptionDialog( this, get( "$MD11163", gs.getName() ), get( "$MD11162" ),
                                                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                        null, null, null );
             if ( result == JOptionPane.OK_OPTION ) {
                 try {
                     graphicOptions.removeFillGraphicDefinition( gs.getName() );
                     graphicToRemoveCB.removeItem( gs );
                     if ( fixedGraphicFillCB.getSelectedItem() == gs ) {
                         fixedGraphicFillCB.setSelectedIndex( 0 );
                     }
                     fixedGraphicFillCB.removeItem( gs );
                 } catch ( MalformedURLException e ) {
                     JOptionPane.showMessageDialog( this, get( "$MD11164" ), get( "$DI10017" ),
                                                    JOptionPane.INFORMATION_MESSAGE );
                 }
 
                 if ( graphicToRemoveCB.getItemCount() > 0 ) {
                     removeGraphicBt.setEnabled( true );
                 } else {
                     removeGraphicBt.setEnabled( false );
                 }
             }
         }
 
     }
 
     public class FillGraphicChangedEvent extends ValueChangedEvent {
 
         private Object value;
 
         public FillGraphicChangedEvent( Object value ) {
             this.value = value;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.deegree.igeo.ValueChangedEvent#getValue()
          */
         @Override
         public Object getValue() {
             return value;
         }
 
     }
 
 }
