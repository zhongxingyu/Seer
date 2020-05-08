 //$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2012 by:
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
 package org.deegree.igeo.views.swing.georef;
 
 import static java.awt.GridBagConstraints.BOTH;
 import static java.awt.GridBagConstraints.CENTER;
 import static java.awt.GridBagConstraints.NONE;
 import static java.util.Collections.singletonList;
 import static javax.swing.BorderFactory.createTitledBorder;
 import static org.deegree.igeo.i18n.Messages.get;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JToggleButton;
 import javax.xml.bind.JAXBElement;
 
 import org.apache.poi.util.IOUtils;
 import org.deegree.graphics.sld.SLDFactory;
 import org.deegree.graphics.sld.StyledLayerDescriptor;
 import org.deegree.graphics.sld.UserStyle;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.commands.GeoRefCommand;
 import org.deegree.igeo.commands.model.AddFileLayerCommand;
 import org.deegree.igeo.commands.model.ZoomCommand;
 import org.deegree.igeo.config.DirectStyleType;
 import org.deegree.igeo.config.EnvelopeType;
 import org.deegree.igeo.config.LayerType.MetadataURL;
 import org.deegree.igeo.config.MemoryDatasourceType;
 import org.deegree.igeo.config.ObjectFactory;
 import org.deegree.igeo.config.StyleType;
 import org.deegree.igeo.config.ToolbarEntryType;
 import org.deegree.igeo.desktop.IGeoDesktop;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.mapmodel.Datasource;
 import org.deegree.igeo.mapmodel.DirectStyle;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.mapmodel.MemoryDatasource;
 import org.deegree.igeo.mapmodel.NamedStyle;
 import org.deegree.igeo.mapmodel.SystemLayer;
 import org.deegree.igeo.modules.DefaultMapModule;
 import org.deegree.igeo.modules.georef.AffineTransformation;
 import org.deegree.igeo.modules.georef.ControlPointModel;
 import org.deegree.igeo.views.swing.ButtonGroup;
 import org.deegree.igeo.views.swing.map.DefaultMapComponent;
 import org.deegree.igeo.views.swing.util.GenericFileChooser;
 import org.deegree.igeo.views.swing.util.GenericFileChooser.FILECHOOSERTYPE;
 import org.deegree.igeo.views.swing.util.IGeoFileFilter;
 import org.deegree.kernel.CommandProcessedEvent;
 import org.deegree.kernel.CommandProcessedListener;
 import org.deegree.kernel.ProcessMonitor;
 import org.deegree.kernel.ProcessMonitorFactory;
 import org.deegree.model.Identifier;
 import org.deegree.model.feature.FeatureFactory;
 import org.deegree.model.spatialschema.Envelope;
 import org.deegree.model.spatialschema.GeometryFactory;
 
 /**
  * TODO add class documentation
  * 
  * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
  * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
  * @author last edited by: $Author: stranger $
  * 
  * @version $Revision: $, $Date: $
  */
 public class GeoreferencingControlPanel extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = 7031021591515735164L;
 
     DefaultMapModule<?> leftModule, rightModule;
 
     Layer leftLayer, rightLayer;
 
     MapModel left, right;
 
     Buttons buttons = new Buttons();
 
     ControlPointModel points;
 
     private File worldFile;
 
     private File sourceFile;
 
     // needs to be cleaned up upon tool close
     MouseAdapter leftMouseAdapter;
 
     public GeoreferencingControlPanel() {
         setLayout( new GridBagLayout() );
         GridBagConstraints gb = new GridBagConstraints();
 
         gb.gridx = 0;
         gb.gridy = 0;
         gb.gridwidth = 2;
         gb.anchor = CENTER;
         gb.insets = new Insets( 2, 2, 2, 2 );
         add( buttons.load = new JButton( get( "$DI10074" ) ), gb );
         buttons.load.addActionListener( this );
 
         gb = (GridBagConstraints) gb.clone();
         ++gb.gridy;
         buttons.transformList = new JComboBox( new String[] { get( "$DI10075" ) } );
         add( buttons.transformList, gb );
 
         gb = (GridBagConstraints) gb.clone();
         gb.gridx = 2;
         gb.gridy = 0;
         gb.gridwidth = 4;
         gb.gridheight = 2;
         gb.fill = BOTH;
         JPanel panel = new JPanel();
         panel.setBorder( createTitledBorder( get( "$DI10085" ) ) );
         panel.add( new JLabel( get( "$DI10086" ) ) );
         add( panel, gb );
 
         panel = new JPanel();
         panel.setLayout( new GridBagLayout() );
 
         gb = (GridBagConstraints) gb.clone();
         gb.gridx = 0;
         gb.gridy = 0;
         gb.gridwidth = 1;
         gb.gridheight = 1;
         gb.fill = NONE;
         panel.add( buttons.loadTable = new JButton( get( "$DI10077" ) ), gb );
         buttons.loadTable.addActionListener( this );
 
         gb = (GridBagConstraints) gb.clone();
         ++gb.gridx;
         panel.add( buttons.activate = new JToggleButton( get( "$DI10076" ) ), gb );
         buttons.activate.addActionListener( this );
 
         gb = (GridBagConstraints) gb.clone();
         ++gb.gridx;
         panel.add( buttons.saveTable = new JButton( get( "$DI10078" ) ), gb );
         buttons.saveTable.addActionListener( this );
 
         gb = (GridBagConstraints) gb.clone();
         gb.gridx = 0;
         gb.gridwidth = 6;
         gb.gridy = 2;
         add( panel, gb );
 
         gb = (GridBagConstraints) gb.clone();
         gb.gridx = 0;
         gb.gridy = 3;
         gb.gridwidth = 6;
         gb.fill = BOTH;
         gb.weightx = 1;
         gb.weighty = 1;
         buttons.table = new JTable( points = new ControlPointModel() );
         add( new JScrollPane( buttons.table ), gb );
 
         panel = new JPanel();
         panel.setLayout( new GridBagLayout() );
 
         gb = (GridBagConstraints) gb.clone();
         ++gb.gridy;
         gb.gridwidth = 6;
         gb.weightx = 0;
         gb.weighty = 0;
         gb.fill = NONE;
         add( panel, gb );
 
         gb = (GridBagConstraints) gb.clone();
         gb.gridx = 0;
         gb.gridy = 0;
         gb.gridwidth = 1;
         panel.add( buttons.delete = new JButton( get( "$DI10081" ) ), gb );
         buttons.delete.addActionListener( this );
 
         gb = (GridBagConstraints) gb.clone();
         ++gb.gridx;
         panel.add( buttons.reset = new JButton( get( "$DI10082" ) ), gb );
         buttons.reset.addActionListener( this );
 
         gb = (GridBagConstraints) gb.clone();
         ++gb.gridx;
         panel.add( buttons.start = new JButton( get( "$DI10083" ) ), gb );
         buttons.start.addActionListener( this );
 
         buttons.enable( false );
     }
 
     /**
      * 
      * @param leftModule
      * @param left
      * @param rightModule
      * @param right map model. Must not be <code>null</code>.
      */
     public void setMapModel( DefaultMapModule<?> leftModule, MapModel left, DefaultMapModule<?> rightModule,
                              MapModel right ) {
         this.leftModule = leftModule;
         this.left = left;
         this.rightModule = rightModule;
         this.right = right;
 
         leftLayer = addPointsLayer( left );
         rightLayer = addPointsLayer( right );
 
         DefaultMapComponent mc = (DefaultMapComponent) rightModule.getMapContainer();
         mc.addMouseListener( new MouseAdapter() {
             @Override
             public void mouseClicked( MouseEvent e ) {
                 if ( !buttons.activate.isSelected() ) {
                     return;
                 }
                 points.clickedRight( e.getX(), e.getY() );
                 AffineTransformation.approximate( points.getPoints() );
                 points.fireTableDataChanged();
             }
         } );
         mc = (DefaultMapComponent) leftModule.getMapContainer();
 
         mc.addMouseListener( this.leftMouseAdapter = new MouseAdapter() {
             @Override
             public void mouseClicked( MouseEvent e ) {
                 if ( !buttons.activate.isSelected() ) {
                     return;
                 }
                 points.clickedLeft( e.getX(), e.getY() );
                 AffineTransformation.approximate( points.getPoints() );
                 points.fireTableDataChanged();
             }
         } );
 
         points.updateMaps( left, leftLayer, right, rightLayer );
     }
 
     @Override
     public void actionPerformed( ActionEvent e ) {
         if ( e.getSource() == buttons.load ) {
             loadRaster();
         }
         if ( e.getSource() == buttons.start ) {
             startTransformation();
         }
         if ( e.getSource() == buttons.activate ) {
             toggleControlPointMode();
         }
         if ( e.getSource() == buttons.loadTable ) {
             loadCsv();
         }
         if ( e.getSource() == buttons.saveTable ) {
             saveCsv();
         }
         if ( e.getSource() == buttons.reset ) {
             points.removeAll();
         }
         if ( e.getSource() == buttons.delete ) {
             points.remove( buttons.table.getSelectedRows() );
             AffineTransformation.approximate( points.getPoints() );
             points.fireTableDataChanged();
             points.updateMaps();
         }
     }
 
     private void toggleControlPointMode() {
         if ( buttons.activate.isSelected() ) {
             leftModule.getMapTool().resetState();
             rightModule.getMapTool().resetState();
             // TODO some proper support from the maptool would be nice
             Map<String, ButtonGroup> groups = leftModule.getApplicationContainer().getButtonGroups();
             for ( ToolbarEntryType tp : leftModule.getToolBarEntries() ) {
                 ButtonGroup bg = groups.get( tp.getAssignedGroup() );
                 if ( bg != null ) {
                     bg.clearSelection();
                     bg.removeSelection();
                 }
             }
             for ( ToolbarEntryType tp : rightModule.getToolBarEntries() ) {
                 ButtonGroup bg = groups.get( tp.getAssignedGroup() );
                 if ( bg != null ) {
                     bg.clearSelection();
                     bg.removeSelection();
                 }
             }
         }
     }
 
     private void loadCsv() {
         ApplicationContainer<?> appContainer = rightModule.getApplicationContainer();
         File file = null;
         Preferences prefs = Preferences.userNodeForPackage( GeoreferencingControlPanel.class );
         List<IGeoFileFilter> ff = new ArrayList<IGeoFileFilter>();
         ff.add( new IGeoFileFilter( "csv", "pkt", "pas" ) );
         file = GenericFileChooser.showOpenDialog( FILECHOOSERTYPE.externalResource, appContainer,
                                                   ( (IGeoDesktop) appContainer ).getMainWndow(), prefs, "georefCsv", ff );
 
         if ( file != null ) {
             try {
                 points.loadPointsFromFile( file );
                 AffineTransformation.approximate( points.getPoints() );
                 points.fireTableDataChanged();
             } catch ( IOException e ) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     private void saveCsv() {
         ApplicationContainer<?> appContainer = rightModule.getApplicationContainer();
         File file = null;
         Preferences prefs = Preferences.userNodeForPackage( GeoreferencingControlPanel.class );
         List<IGeoFileFilter> ff = new ArrayList<IGeoFileFilter>();
         ff.add( new IGeoFileFilter( "csv", "pkt", "pas" ) );
         file = GenericFileChooser.showSaveDialog( FILECHOOSERTYPE.geoDataFile, appContainer,
                                                   ( (IGeoDesktop) appContainer ).getMainWndow(), prefs, "georefCsv", ff );
 
         if ( file != null ) {
             try {
                 points.savePointsToFile( file );
             } catch ( IOException e ) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     private String findGdal() {
         Preferences prefs = Preferences.userNodeForPackage( GeoreferencingControlPanel.class );
         String prefix = prefs.get( "gdal_location", "" );
         InputStream in = null;
         try {
             ProcessBuilder pb = new ProcessBuilder();
             pb.command( "gdalwarp" );
             Process p = pb.start();
             in = p.getInputStream();
             String s = new String( IOUtils.toByteArray( in ), "UTF-8" );
             if ( p.waitFor() != 1 || !s.startsWith( "Usage:" ) ) {
                 JFileChooser dlg = new JFileChooser( new File( prefix ) );
                 dlg.setDialogTitle( Messages.get( "$DI10092" ) );
                 dlg.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                 int res = dlg.showDialog( this, Messages.get( "$DI10093" ) );
                 if ( res != JFileChooser.APPROVE_OPTION ) {
                     return null;
                 }
                 if ( !new File( dlg.getSelectedFile(), "gdalwarp" ).exists()
                      && !new File( dlg.getSelectedFile(), "gdalwarp.exe" ).exists() ) {
                     return null;
                 }
                 prefix = dlg.getSelectedFile().toString() + File.separator;
                 prefs.put( "gdal_location", prefix );
             }
         } catch ( Throwable e ) {
             JFileChooser dlg = new JFileChooser( new File( prefix ) );
             dlg.setDialogTitle( Messages.get( "$DI10092" ) );
             dlg.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
             int res = dlg.showDialog( this, Messages.get( "$DI10093" ) );
             if ( res != JFileChooser.APPROVE_OPTION ) {
                 return null;
             }
             if ( !new File( dlg.getSelectedFile(), "gdalwarp" ).exists()
                  && !new File( dlg.getSelectedFile(), "gdalwarp.exe" ).exists() ) {
                 return null;
             }
             prefix = dlg.getSelectedFile().toString() + File.separator;
             prefs.put( "gdal_location", prefix );
         } finally {
             if ( in != null ) {
                 try {
                     in.close();
                 } catch ( IOException e ) {
                     // probably was not open
                 }
             }
         }
         return prefix;
     }
 
     private void startTransformation() {
         ApplicationContainer<?> appContainer = rightModule.getApplicationContainer();
         File file = null;
         if ( "Application".equalsIgnoreCase( appContainer.getViewPlatform() ) ) {
             Preferences prefs = Preferences.userNodeForPackage( GeoreferencingControlPanel.class );
             List<IGeoFileFilter> ff = new ArrayList<IGeoFileFilter>();
 
             ff.add( IGeoFileFilter.TIFF );
             ff.add( IGeoFileFilter.PNG );
 
             file = GenericFileChooser.showSaveDialog( FILECHOOSERTYPE.geoDataFile, appContainer,
                                                       ( (IGeoDesktop) appContainer ).getMainWndow(), prefs,
                                                       "georefTarget", ff );
 
         }
         if ( file != null ) {
             runGdal( file );
         }
     }
 
     private void runGdal( File file ) {
         PrintStream out = null;
         try {
             out = new PrintStream( new FileOutputStream( worldFile ) );
 
             // since source projection was identity, we can use the transform directly as .wld
             double[] trans = AffineTransformation.approximate( points.getPoints() );
             for ( double d : trans ) {
                 out.println( d );
             }
 
             String prefix = findGdal();
 
             if ( prefix == null ) {
                 JOptionPane.showMessageDialog( this, Messages.get( "$DI10094" ) );
                 return;
             }
 
             GeoRefCommand command = new GeoRefCommand( prefix, left.getCoordinateSystem().getPrefixedName(),
                                                        sourceFile, file );
 
             final ProcessMonitor pm = ProcessMonitorFactory.createDialogProcessMonitor( rightModule.getApplicationContainer().getViewPlatform(),
                                                                                         Messages.get( "$DI10087" ),
                                                                                         Messages.get( "$DI10088", file ),
                                                                                         0, 100, command );
             command.setProcessMonitor( pm );
             command.addListener( new CommandProcessedListener() {
                 @Override
                 public void commandProcessed( CommandProcessedEvent event ) {
                     try {
                         pm.cancel();
                         JOptionPane.showMessageDialog( rightModule.getApplicationContainer().getMainWndow(),
                                                        Messages.get( "$DI10089" ), Messages.get( "$DI10090" ),
                                                        JOptionPane.INFORMATION_MESSAGE );
                     } catch ( Exception e ) {
                         e.printStackTrace();
                     }
                 }
 
             } );
             rightModule.getApplicationContainer().getCommandProcessor().executeASychronously( command );
         } catch ( FileNotFoundException e ) {
             // unlikely, as the parent directory exists for sure
         } finally {
             if ( out != null ) {
                 out.close();
             }
         }
     }
 
     private void loadRaster() {
         ApplicationContainer<?> appContainer = rightModule.getApplicationContainer();
         File file = null;
         if ( "Application".equalsIgnoreCase( appContainer.getViewPlatform() ) ) {
             Preferences prefs = Preferences.userNodeForPackage( GeoreferencingControlPanel.class );
             List<IGeoFileFilter> ff = new ArrayList<IGeoFileFilter>();
 
             ff.add( IGeoFileFilter.TIFF );
             ff.add( IGeoFileFilter.PNG );
 
             file = GenericFileChooser.showOpenDialog( FILECHOOSERTYPE.geoDataFile, appContainer,
                                                       ( (IGeoDesktop) appContainer ).getMainWndow(), prefs,
                                                       "georefLoad", ff );
 
         }
         if ( file != null ) {
         	sourceFile = file;
         	PrintStream out = null;
             try {
                worldFile = new File( file.toString().substring( 0, file.toString().length() - 4 ) + ".wld" );
                 out = new PrintStream( new FileOutputStream( worldFile ) );
                 // use the image coordinate system here (identity matrix, no translation), just flip
                 out.println( 1 );
                 out.println( 0 );
                 out.println( 0 );
                 out.println( -1 );
                 out.println( 0 );
                 out.println( 0 );
 
                 // zoom to scale = 1, 0/0
                 ZoomCommand cmd = new ZoomCommand( right );
                 Container c = (Container) rightModule.getGUIContainer();
                 Envelope env = GeometryFactory.createEnvelope( 0, -c.getHeight(), c.getWidth(), 0, null );
                 cmd.setZoomBox( env, c.getWidth(), c.getHeight() );
                 appContainer.getCommandProcessor().executeSychronously( cmd, true );
             } catch ( Throwable e ) {
                 e.printStackTrace();
             } finally {
             	if (out != null)
             		out.close();            	
             }
 
             String crsName = right.getCoordinateSystem().getPrefixedName();
             final AddFileLayerCommand command = new AddFileLayerCommand( right, file, null, null, null, crsName );
 
             final ProcessMonitor pm = ProcessMonitorFactory.createDialogProcessMonitor( appContainer.getViewPlatform(),
                                                                                         Messages.get( "$MD11264" ),
                                                                                         Messages.get( "$MD11265", file ),
                                                                                         0, -1, command );
             command.setProcessMonitor( pm );
             command.addListener( new CommandProcessedListener() {
                 @Override
                 public void commandProcessed( CommandProcessedEvent event ) {
                     buttons.enable( true );
                     try {
                         pm.cancel();
                     } catch ( Exception e ) {
                         e.printStackTrace();
                     }
                 }
 
             } );
             appContainer.getCommandProcessor().executeASychronously( command );
         }
     }
 
     private static Layer addPointsLayer( MapModel mm ) {
         if ( mm.getLayerByIdentifier( new Identifier( "georef" ) ) != null ) {
             return mm.getLayerByIdentifier( new Identifier( "georef" ) );
         }
 
         Envelope env = mm.getEnvelope();
         MemoryDatasourceType mdst = new MemoryDatasourceType();
         EnvelopeType et = new EnvelopeType();
         et.setMinx( env.getMin().getX() );
         et.setMiny( env.getMin().getY() );
         et.setMaxx( env.getMax().getX() );
         et.setMaxy( env.getMax().getY() );
         et.setCrs( mm.getEnvelope().getCoordinateSystem().getPrefixedName() );
         mdst.setExtent( et );
         mdst.setMinScaleDenominator( 0d );
         mdst.setMaxScaleDenominator( 100000000d );
         Datasource ds = new MemoryDatasource( mdst, null, null, FeatureFactory.createFeatureCollection( null, 0 ) );
 
         Identifier id = new Identifier( "georef" );
 
         SystemLayer newLayer = new SystemLayer( mm, id, id.getValue(), "", singletonList( ds ),
                                                 Collections.<MetadataURL> emptyList() );
         newLayer.setEditable( false );
         newLayer.setVisibleInLayerTree( false );
         newLayer.setVisible( true );
 
         List<StyleType> styleTypes = new ArrayList<StyleType>();
         StyleType st = new StyleType();
         DirectStyleType dst = new DirectStyleType();
         String litSld = "<?xml version=\"1.0\"?>"
                         + "<StyledLayerDescriptor xmlns=\"http://www.opengis.net/sld\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0.0\">"
                         + "  <NamedLayer>"
                         + "    <Name>georef</Name>"
                         + "    <UserStyle xmlns=\"http://www.opengis.net/sld\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"
                         + "      <Name>default</Name>" + "      <Title>default</Title>"
                         + "      <Abstract>default3</Abstract>" + "      <FeatureTypeStyle>"
                         + "        <Title>Regel 1</Title>" + "        <Rule>" + "          <Name>default</Name>"
                         + "          <Title>default</Title>" + "          <Abstract>default</Abstract>"
                         + "          <MinScaleDenominator>0.0</MinScaleDenominator>"
                         + "          <MaxScaleDenominator>1.0E9</MaxScaleDenominator>" + "          <PointSymbolizer>"
                         + "            <Graphic>" + "              <Mark>"
                         + "                <WellKnownName>x</WellKnownName>" + "                <Fill>"
                         + "                  <CssParameter name=\"fill\">#808080</CssParameter>"
                         + "                  <CssParameter name=\"fill-opacity\">1.0</CssParameter>"
                         + "                </Fill>" + "                <Stroke>"
                         + "                  <CssParameter name=\"stroke\">#8f242a</CssParameter>"
                         + "                </Stroke>" + "              </Mark>"
                         + "              <Opacity>1.0</Opacity>" + "              <Size>20.0</Size>"
                         + "              <Rotation>0.0</Rotation>" + "            </Graphic>"
                         + "          </PointSymbolizer>" + "        </Rule>" + "      </FeatureTypeStyle>"
                         + "    </UserStyle>" + "  </NamedLayer>" + "</StyledLayerDescriptor>";
 
         dst.setSld( litSld );
         JAXBElement<DirectStyleType> dsjx = new ObjectFactory().createDirectStyle( dst );
         st.setNamedStyle( dsjx );
         dst.setCurrent( true );
 
         styleTypes.add( st );
 
         StyledLayerDescriptor sld = null;
         try {
             sld = SLDFactory.createSLD( litSld );
             NamedStyle nStyle = new DirectStyle( dst, (UserStyle) sld.getNamedLayers()[0].getStyles()[0], newLayer );
             nStyle.setCurrent( true );
             ArrayList<NamedStyle> styles = new ArrayList<NamedStyle>();
             styles.add( nStyle );
             newLayer.setStyles( styles );
         } catch ( Throwable e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         mm.insert( newLayer, mm.getLayerGroups().get( 0 ), null, true );
         return newLayer;
     }
 
     static class Buttons {
         JButton load, loadTable, saveTable, delete, reset, start;
 
         JToggleButton activate;
 
         JComboBox transformList;
 
         JTable table;
 
         void enable( boolean b ) {
             loadTable.setEnabled( b );
             saveTable.setEnabled( b );
             delete.setEnabled( b );
             reset.setEnabled( b );
             start.setEnabled( b );
             activate.setEnabled( b );
             transformList.setEnabled( b );
             table.setEnabled( b );
         }
     }
 
 }
