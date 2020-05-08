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
 package org.deegree.igeo.mapmodel;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.xml.bind.JAXBElement;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.CollectionUtils.Mapper;
 import org.deegree.framework.util.ImageUtils;
 import org.deegree.graphics.sld.AbstractLayer;
 import org.deegree.graphics.sld.AbstractStyle;
 import org.deegree.graphics.sld.NamedLayer;
 import org.deegree.graphics.sld.StyledLayerDescriptor;
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.config.AbstractDatasourceType;
 import org.deegree.igeo.config.DatabaseDatasourceType;
 import org.deegree.igeo.config.DatasourceType;
 import org.deegree.igeo.config.DefinedStyleType;
 import org.deegree.igeo.config.DirectStyleType;
 import org.deegree.igeo.config.ExternalResourceType;
 import org.deegree.igeo.config.FileDatasourceType;
 import org.deegree.igeo.config.IdentifierType;
 import org.deegree.igeo.config.LayerType;
 import org.deegree.igeo.config.LayerType.MetadataURL;
 import org.deegree.igeo.config.MemoryDatasourceType;
 import org.deegree.igeo.config.NamedStyleType;
 import org.deegree.igeo.config.NamedStyleType.LegendURL;
 import org.deegree.igeo.config.ObjectFactory;
 import org.deegree.igeo.config.OnlineResourceType;
 import org.deegree.igeo.config.StyleType;
 import org.deegree.igeo.config.Util;
 import org.deegree.igeo.config.WCSDatasourceType;
 import org.deegree.igeo.config.WFSDatasourceType;
 import org.deegree.igeo.config.WMSDatasourceType;
 import org.deegree.igeo.dataadapter.Adapter;
 import org.deegree.igeo.dataadapter.AdapterEvent;
 import org.deegree.igeo.dataadapter.DataAccessAdapter;
 import org.deegree.igeo.dataadapter.DataAccessException;
 import org.deegree.igeo.dataadapter.DataAccessFactory;
 import org.deegree.igeo.dataadapter.FeatureAdapter;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.mapmodel.LayerChangedEvent.LAYER_CHANGE_TYPE;
 import org.deegree.io.datastore.PropertyPathResolvingException;
 import org.deegree.model.Identifier;
 import org.deegree.model.feature.Feature;
 import org.deegree.model.feature.FeatureCollection;
 import org.deegree.model.feature.FeatureFactory;
 import org.deegree.model.feature.FeatureProperty;
 import org.deegree.model.feature.schema.FeatureType;
 import org.deegree.model.filterencoding.Filter;
 import org.deegree.model.filterencoding.FilterEvaluationException;
 import org.deegree.model.spatialschema.Envelope;
 import org.deegree.model.spatialschema.Point;
 import org.deegree.ogcbase.PropertyPath;
 
 /**
  * Implementation of interface {@link MapModelEntry} for modelling a Layer. A layer is always assigned to a
  * {@link Datasource} and can access its data via an according {@link DataAccessAdapter}.
  * 
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version. $Revision$, $Date$
  */
 public class Layer implements MapModelEntry {
 
     private static final ILogger LOG = LoggerFactory.getLogger( Layer.class );
 
     transient protected List<ChangeListener> listeners = new ArrayList<ChangeListener>();
 
     private LayerGroup parent;
 
     private MapModel owner;
 
     private List<Datasource> datasources;
 
     private List<NamedStyle> styles;
 
     private List<DataAccessAdapter> dataAccess;
 
     private Map<String, DataAccessException> dataAccessExceptions = new HashMap<String, DataAccessException>();
 
     transient private FeatureCollection selectedFeatures;
 
     transient private List<FeatureProperty> selectedGeometries;
 
     private LayerType layerType;
 
     /**
      * 
      * @param owner
      * @param identifier
      * @param title
      * @param abstract_
      */
     public Layer( MapModel owner, Identifier identifier, String title, String abstract_ ) {
         this( owner, identifier, title, abstract_, new ArrayList<Datasource>(), new ArrayList<MetadataURL>() );
     }
 
     /**
      * 
      * @param owner
      * @param identifier
      * @param title
      * @param abstract_
      * @param datasources
      * @param metadataURLs
      */
     public Layer( MapModel owner, Identifier identifier, String title, String abstract_, List<Datasource> datasources,
                   List<MetadataURL> metadataURLs ) {
 
         layerType = new LayerType();
         IdentifierType id = new IdentifierType();
         id.setValue( identifier.getValue() );
         if ( identifier.getNamespace() != null ) {
             id.setNamespace( identifier.getNamespace().toASCIIString() );
         }
         layerType.setIdentifier( id );
         layerType.setTitle( title );
         layerType.setAbstract( abstract_ );
         layerType.setMinScaleDenominator( 0d );
         layerType.setMaxScaleDenominator( 9E99 );
         layerType.setEditable( true );
         layerType.setQueryable( true );
         layerType.setVisible( true );
         setMetadataURL( metadataURLs );
 
         List<String> crs = new ArrayList<String>();
         crs.add( owner.getCoordinateSystem().getPrefixedName() );
 
         NamedStyleType nst = new NamedStyleType();
         nst.setName( "default" );
         nst.setTitle( "default" );
         nst.setAbstract( "default" );
         nst.setCurrent( true );
         List<StyleType> styleTypes = layerType.getStyle();
         styleTypes.clear();
         StyleType st = new StyleType();
         st.setNamedStyle( new ObjectFactory().createNamedStyle( nst ) );
         styleTypes.add( st );
 
         NamedStyle nStyle = new NamedStyle( nst, this );
         nStyle.setCurrent( true );
         styles = new ArrayList<NamedStyle>();
         styles.add( nStyle );
 
         this.owner = owner;
         // must use set-Method to avoid in consistent states
         setDatasources( datasources );
         this.selectedFeatures = FeatureFactory.createFeatureCollection( new Identifier().getAsQualifiedString(), 10 );
 
     }
 
     /**
      * 
      * @param layerType
      * @param owner
      * @param datasources
      * @param styles
      */
     public Layer( LayerType layerType, MapModel owner, List<Datasource> datasources, List<NamedStyle> styles ) {
         this.layerType = layerType;
         this.styles = styles;
         this.owner = owner;
         // must use set-Method to avoid in consistent states
         setDatasources( datasources );
         this.selectedFeatures = FeatureFactory.createFeatureCollection( new Identifier().getAsQualifiedString(), 10 );
     }
 
     /**
      * 
      * @return encapsulated configuration layer
      */
     public LayerType getLayerType() {
         return layerType;
     }
 
     /**
      * 
      * @return abstract
      */
     public String getAbstract() {
         return layerType.getAbstract();
     }
 
     /**
      * 
      * @param abstract_
      */
     public void setAbstract( String abstract_ ) {
         layerType.setAbstract( abstract_ );
     }
 
     /**
      * 
      * @return list of used datasources
      */
     public List<Datasource> getDatasources() {
         if ( this.datasources == null ) {
             this.datasources = new ArrayList<Datasource>();
         }
         return this.datasources;
     }
 
     /**
      * @return a list of exceptions occured during trying to access a datasource
      */
     public Map<String, DataAccessException> getDataAccessExceptions() {
         return dataAccessExceptions;
     }
 
     /**
      * adds a data source to a layer
      * 
      * @param datasource
      */
     public void addDatasource( Datasource datasource ) {
         if ( this.datasources == null ) {
             this.datasources = new ArrayList<Datasource>();
         }
         this.datasources.add( datasource );
         // a new data access object must be created if a data source is added to a layer
         dataAccess.add( DataAccessFactory.createDataAccessAdapter( datasource, owner, this ) );
 
         DatasourceType dt = new DatasourceType();
 
         AbstractDatasourceType dsType = datasource.getDatasourceType();
 
         JAXBElement<? extends AbstractDatasourceType> ds = null;
         if ( dsType instanceof FileDatasourceType ) {
             ds = new ObjectFactory().createFileDatasource( (FileDatasourceType) dsType );
         } else if ( dsType instanceof DatabaseDatasourceType ) {
             ds = new ObjectFactory().createDatabaseDatasource( (DatabaseDatasourceType) dsType );
         } else if ( dsType instanceof WMSDatasourceType ) {
             ds = new ObjectFactory().createWMSDatasource( (WMSDatasourceType) dsType );
         } else if ( dsType instanceof WFSDatasourceType ) {
             ds = new ObjectFactory().createWFSDatasource( (WFSDatasourceType) dsType );
         } else if ( dsType instanceof WCSDatasourceType ) {
             ds = new ObjectFactory().createWCSDatasource( (WCSDatasourceType) dsType );
         } else if ( dsType instanceof MemoryDatasourceType ) {
             ds = new ObjectFactory().createMemoryDatasource( (MemoryDatasourceType) dsType );
         }
         dt.setAbstractDatasource( ds );
 
         layerType.getDatasource().add( dt );
     }
 
     /**
      * sets a complete new list of {@link Datasource}es
      * 
      * @param datasources
      */
     public void setDatasources( List<Datasource> datasources ) {
         if ( this.datasources == null ) {
             this.datasources = new ArrayList<Datasource>();
         } else {
             this.datasources.clear();
         }
         if ( dataAccess == null ) {
             this.dataAccess = new ArrayList<DataAccessAdapter>( datasources.size() );
         } else {
             this.dataAccess.clear();
         }
         this.dataAccessExceptions.clear();
         layerType.getDatasource().clear();
         for ( Datasource datasource : datasources ) {
             try {
                 addDatasource( datasource );
             } catch ( DataAccessException e ) {
                 this.dataAccessExceptions.put( datasource.getName(), e );
             }
         }
     }
 
     /**
      * removes a datasource from a layer
      * 
      * @param datasource
      */
     public void removeDatasource( Datasource datasource ) {
         this.datasources.remove( datasource );
         if ( dataAccess != null ) {
             ListIterator<DataAccessAdapter> iter = dataAccess.listIterator();
             while ( iter.hasNext() ) {
                 if ( iter.next().getDatasource() == datasource ) {
                     iter.remove();
                 }
             }
         }
     }
 
     /**
      * 
      * @return list of assigend external resources
      */
     public List<ExternalResourceType> getExternalResources() {
         return layerType.getExternalResource();
     }
 
     /**
      * 
      * @param externalResources
      */
     public void setExternalResources( List<ExternalResourceType> externalResources ) {
         List<ExternalResourceType> tmp = layerType.getExternalResource();
         tmp.addAll( externalResources );
     }
 
     /**
      * 
      * @param externalResource
      */
     public void addExternalResources( ExternalResourceType externalResource ) {
         List<ExternalResourceType> tmp = layerType.getExternalResource();
         tmp.add( externalResource );
     }
 
     /**
      * 
      * @param externalResource
      */
     public void removeExternalResources( ExternalResourceType externalResource ) {
         List<ExternalResourceType> tmp = layerType.getExternalResource();
         tmp.remove( externalResource );
     }
 
     /**
      * 
      * @return layers identifier
      */
     public Identifier getIdentifier() {
         return Util.convertIdentifier( layerType.getIdentifier() );
     }
 
     /**
      * 
      * @return true if queryable (e.g. GetFeatureInfo or GetFeature)
      */
     public boolean isQueryable() {
         return layerType.isQueryable();
     }
 
     /**
      * 
      * @param isQueryable
      */
     public void setQueryable( boolean isQueryable ) {
         layerType.setQueryable( isQueryable );
     }
 
     /**
      * 
      * @return true if visible
      */
     public boolean isVisible() {
         return layerType.isVisible();
     }
 
     /**
      * 
      * @param visible
      */
     public void setVisible( boolean visible ) {
         if ( visible != layerType.isVisible() ) {
             layerType.setVisible( visible );
             if ( !visible ) {
                 unselectAllFeatures();
             }
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.visibilityChanged, visible );
         }
     }
 
     /**
      * @return the isEditable
      */
     public boolean isEditable() {
         return layerType.isEditable();
     }
 
     /**
      * @param isEditable
      *            the isEditable to set
      */
     public void setEditable( boolean isEditable ) {
         layerType.setEditable( isEditable );
     }
 
     /**
      * 
      * @return maximum scale denominator
      */
     public double getMaxScaleDenominator() {
         return layerType.getMaxScaleDenominator();
     }
 
     /**
      * 
      * @param maxScaleDenominator
      */
     public void setMaxScaleDenominator( double maxScaleDenominator ) {
         layerType.setMaxScaleDenominator( maxScaleDenominator );
     }
 
     /**
      * 
      * @return list of assigend metadata URLs
      */
     public List<MetadataURL> getMetadataURLs() {
         return layerType.getMetadataURL();
     }
 
     /**
      * 
      * @param metadataURLs
      */
     public void setMetadataURL( List<MetadataURL> metadataURLs ) {
         if ( metadataURLs != null ) {
             List<MetadataURL> tmp = layerType.getMetadataURL();
             tmp.clear();
             tmp.addAll( metadataURLs );
         }
     }
 
     /**
      * 
      * @param metadataURL
      */
     public void addMetadataURL( MetadataURL metadataURL ) {
         List<MetadataURL> tmp = layerType.getMetadataURL();
         tmp.add( metadataURL );
     }
 
     /**
      * 
      * @param metadataURL
      */
     public void removeMetadataURL( MetadataURL metadataURL ) {
         List<MetadataURL> tmp = layerType.getMetadataURL();
         tmp.remove( metadataURL );
     }
 
     /**
      * 
      * @return minimum scale denominator
      */
     public double getMinScaleDenominator() {
         return layerType.getMinScaleDenominator();
     }
 
     /**
      * 
      * @param minScaleDenominator
      */
     public void setMinScaleDenominator( double minScaleDenominator ) {
         layerType.setMinScaleDenominator( minScaleDenominator );
     }
 
     /**
      * 
      * @return id of layers parent; may be <code>null</code>
      */
     public LayerGroup getParent() {
         return this.parent;
     }
 
     /**
      * 
      * @return antecessor layer or <code>null</code> if a layer do not have an antecessor
      */
     public Layer getAntecessor() {
         Layer antecessor = null;
         List<MapModelEntry> mme = this.parent.getMapModelEntries();
         for ( int i = 1; i < mme.size(); i++ ) {
             if ( mme.get( i ).equals( this ) && mme.get( i - 1 ) instanceof Layer ) {
                 antecessor = (Layer) mme.get( i - 1 );
                 break;
             }
         }
         return antecessor;
     }
 
     /**
      * 
      * @param parent
      */
     public void setParent( LayerGroup parent ) {
         if ( parent != null && !parent.equals( this.parent ) ) {
             // remove from old parent
             if ( this.parent != null ) {
                 this.parent.removeLayer( this );
             }
             this.parent = parent;
             // add to new parent
             this.parent.addLayer( this );
         } else if ( parent == null ) {
             this.parent = null;
         }
     }
 
     /**
      * @return the owner
      */
     public MapModel getOwner() {
         return owner;
     }
 
     /**
      * 
      * @return list of action a layer is selected for
      */
     public List<String> getSelectedFor() {
         return layerType.getSelectedFor();
     }
 
     /**
      * 
      * @param selectedFor
      */
     public void setSelectedFor( List<String> selectedFor ) {
         List<String> tmp = layerType.getSelectedFor();
         tmp.clear();
         tmp.addAll( selectedFor );
         fireLayerChangedEvent( LAYER_CHANGE_TYPE.selectedForChanged, selectedFor );
     }
 
     /**
      * 
      * @param selectedFor
      */
     public void addSelectedFor( String selectedFor ) {
         List<String> tmp = layerType.getSelectedFor();
         if ( !tmp.contains( selectedFor ) ) {
             tmp.add( selectedFor );
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.selectedForChanged, selectedFor );
         }
     }
 
     /**
      * 
      * @param selectedFor
      */
     public void removeSelectedFor( String selectedFor ) {
         List<String> tmp = layerType.getSelectedFor();
         if ( tmp.contains( selectedFor ) ) {
             tmp.remove( selectedFor );
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.selectedForChanged, selectedFor );
         }
     }
 
     /**
      * 
      * @return list of assigend styles
      */
     public List<NamedStyle> getStyles() {
         return this.styles;
     }
 
     /**
      * 
      * @param styles
      * @throws URISyntaxException
      * @throws IOException
      */
     public void setStyles( List<NamedStyle> styles )
                             throws URISyntaxException, IOException {
         this.styles = styles;
         List<StyleType> styleTypes = layerType.getStyle();
         styleTypes.clear();
         for ( NamedStyle namedStyle : this.styles ) {
             StyleType st = createStyleType( namedStyle );
             styleTypes.add( st );
         }
         fireLayerChangedEvent( LAYER_CHANGE_TYPE.stylesSet, this.styles );
     }
 
     private StyleType createStyleType( NamedStyle namedStyle )
                             throws URISyntaxException, IOException {
         NamedStyleType nst = null;
         if ( namedStyle instanceof DirectStyle ) {
             nst = new DirectStyleType();
             AbstractLayer al = new NamedLayer( getTitle(), null, new AbstractStyle[] { namedStyle.getStyle() } );
             StyledLayerDescriptor sld = new StyledLayerDescriptor( new AbstractLayer[] { al }, "1.0.0" );
             ( (DirectStyleType) nst ).setSld( sld.exportAsXML() );
         } else {
             nst = new NamedStyleType();
         }
         if ( nst instanceof DefinedStyleType && namedStyle instanceof DefinedStyle ) {
             ( (DefinedStyleType) nst ).setUom( ( (DefinedStyle) namedStyle ).getUom() );
         }
         nst.setCurrent( namedStyle.isCurrent() );
         nst.setAbstract( namedStyle.getAbstract() );
         nst.setName( namedStyle.getName() );
         nst.setTitle( namedStyle.getTitle() );
         if ( namedStyle.getLegendURL() != null ) {
             LegendURL lu = new NamedStyleType.LegendURL();
             OnlineResourceType ort = new OnlineResourceType();
             ort.setHref( namedStyle.getLegendURL().toExternalForm() );
             lu.setOnlineResource( ort );
             nst.setLegendURL( lu );
         } else if ( namedStyle.getLegendImage() != null ) {
             BufferedImage bi = namedStyle.getLegendImage();
             ByteArrayOutputStream bos = new ByteArrayOutputStream( 2000 );
             ImageUtils.saveImage( bi, bos, "png", 1f );
             nst.setLegendImage( bos.toByteArray() );
             bos.close();
         }
 
         StyleType st = new StyleType();
         if ( nst instanceof DirectStyleType ) {
             st.setNamedStyle( new ObjectFactory().createDirectStyle( (DirectStyleType) nst ) );
         } else {
             st.setNamedStyle( new ObjectFactory().createNamedStyle( nst ) );
         }
         return st;
     }
 
     /**
      * 
      * @param style
      * @throws IOException
      * @throws URISyntaxException
      */
     public void addStyle( NamedStyle style )
                             throws URISyntaxException, IOException {
         if ( this.styles == null ) {
             this.styles = new ArrayList<NamedStyle>();
         }
         this.styles.add( style );
         layerType.getStyle().add( createStyleType( style ) );
     }
 
     /**
      * 
      * @param style
      */
     public void removeStyle( NamedStyle style ) {
         this.styles.remove( style );
     }
 
     /**
      * 
      * @return layers title
      */
     public String getTitle() {
         return layerType.getTitle();
     }
 
     /**
      * 
      * @param title
      */
     public void setTitle( String title ) {
         layerType.setTitle( title );
     }
 
     /**
      * 
      * @return data access object for a layer
      */
     public List<DataAccessAdapter> getDataAccess() {
         return dataAccess;
     }
 
     /**
      * 
      * @return current style a layers data shall be displayed
      */
     public NamedStyle getCurrentStyle() {
         for ( NamedStyle style : this.styles ) {
             if ( style.isCurrent() ) {
                 return style;
             }
         }
         this.styles.get( 0 ).setCurrent( true );
         return this.styles.get( 0 );
     }
 
     /**
      * 
      * @return selected features or an empty feature collection, if no features are selected or a adapted layer just
      *         contains grid coverage data sources
      */
     public FeatureCollection getSelectedFeatures() {
         return selectedFeatures;
     }
 
     /**
      * removes all features from the internal feature collection storing currently selected features
      * 
      */
     public void unselectAllFeatures() {
         if ( selectedFeatures.size() > 0 ) {
             FeatureCollection fc = FeatureFactory.createFeatureCollection( new Identifier().getAsQualifiedString(),
                                                                            selectedFeatures.toArray() );
             selectedFeatures.clear();
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.featureUnselected, fc );
         }
     }
 
     /**
      * conveniece method for selecting features by a point
      * 
      * @param point
      * @param additive
      *            if true features at the passed point will be added to current collection otherwise a new collection
      *            will be initialized first
      */
     public void selectFeatures( Point point, boolean additive ) {
         if ( !additive ) {
             unselectAllFeatures();
         }
         int size = selectedFeatures.size();
         for ( DataAccessAdapter adapter : dataAccess ) {
             if ( adapter instanceof FeatureAdapter ) {
                 FeatureCollection fc;
                 try {
                     fc = ( (FeatureAdapter) adapter ).getFeatureCollection( point );
                 } catch ( FilterEvaluationException e ) {
                     LOG.logError( e.getMessage(), e );
                     throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10052" ) );
                 }
                 Iterator<Feature> iterator = fc.iterator();
                 while ( iterator.hasNext() ) {
                     Feature feature2 = (Feature) iterator.next();
                     if ( additive && selectedFeatures.getFeature( feature2.getId() ) != null ) {
                         selectedFeatures.remove( feature2 );
                     } else {
                         selectedFeatures.add( feature2 );
                     }
                 }
             }
         }
         if ( size != selectedFeatures.size() ) {
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.featureSelected, selectedFeatures );
         }
     }
 
     /**
      * conveniece method for selecting features by a bbox
      * 
      * @param envelope
      * @param additive
      *            if true features at the passed point will be added to current collection otherwise a new collection
      *            will be initialized first
      */
     public void selectFeatures( Envelope envelope, boolean additive ) {
         if ( !additive ) {
             unselectAllFeatures();
         }
         int size = selectedFeatures.size();
         for ( DataAccessAdapter adapter : dataAccess ) {
             if ( adapter instanceof FeatureAdapter ) {
                 FeatureCollection fc;
                 try {
                     fc = ( (FeatureAdapter) adapter ).getFeatureCollection( envelope );
                 } catch ( FilterEvaluationException e ) {
                     LOG.logError( e.getMessage(), e );
                     throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10053" ) );
                 }
                 Iterator<Feature> iterator = fc.iterator();
                 while ( iterator.hasNext() ) {
                     Feature feature2 = iterator.next();
                     if ( additive && selectedFeatures.getFeature( feature2.getId() ) != null ) {
                         selectedFeatures.remove( feature2 );
                     } else {
                         selectedFeatures.add( feature2 );
                     }
                 }
             }
         }
         if ( size != selectedFeatures.size() ) {
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.featureSelected, selectedFeatures );
         }
     }
 
     /**
      * selecting features by a user defined filter expression
      * 
      * @param filter
      * @param additive
      *            if true features at the passed point will be added to current collection otherwise a new collection
      *            will be initialized first
      */
     public void selectFeatures( Filter filter, boolean additive ) {
         if ( !additive ) {
             unselectAllFeatures();
         }
         int size = selectedFeatures.size();
         for ( DataAccessAdapter adapter : dataAccess ) {
             if ( adapter instanceof FeatureAdapter ) {
                 FeatureCollection fc;
                 try {
                     fc = ( (FeatureAdapter) adapter ).getFeatureCollection( filter );
                 } catch ( FilterEvaluationException e ) {
                     LOG.logError( e.getMessage(), e );
                     throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10054" ) );
                 }
                 Iterator<Feature> iterator = fc.iterator();
                 while ( iterator.hasNext() ) {
                     Feature feature2 = (Feature) iterator.next();
                     if ( additive && selectedFeatures.getFeature( feature2.getId() ) != null ) {
                         selectedFeatures.remove( feature2 );
                     } else {
                         selectedFeatures.add( feature2 );
                     }
                 }
             }
         }
         if ( size != selectedFeatures.size() ) {
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.featureSelected, selectedFeatures );
         }
     }
 
     /**
      * selects a feature by its fid
      * 
      * @param fids
      * @param additive
      *            if true features at the passed point will be added to current collection otherwise a new collection
      *            will be initialized first
      */
     public void selectFeatures( List<Identifier> fids, boolean additive ) {
         if ( !additive ) {
             unselectAllFeatures();
         }
         int size = selectedFeatures.size();
         for ( DataAccessAdapter adapter : dataAccess ) {
             if ( adapter instanceof FeatureAdapter ) {
                 FeatureCollection fc = ( (FeatureAdapter) adapter ).getFeatureCollection();
                 for ( Identifier fid : fids ) {
                     Feature feature = fc.getFeature( fid.getAsQualifiedString() );
                     if ( feature != null && selectedFeatures.getFeature( feature.getId() ) == null ) {
                         selectedFeatures.add( feature );
                     }
                 }
             }
         }
         if ( size != selectedFeatures.size() ) {
             fireLayerChangedEvent( LAYER_CHANGE_TYPE.featureSelected, selectedFeatures );
         }
     }
 
     /**
      * marks a geometry being part of a feature as selected
      * 
      * @param fid
      *            id of feature from which a geometry should be selected
      * @param propertyPath
      *            path of the geometry to be selected
      * @throws PropertyPathResolvingException
      */
     public void selectGeometry( Identifier fid, PropertyPath propertyPath )
                             throws PropertyPathResolvingException {
         for ( DataAccessAdapter adapter : dataAccess ) {
             if ( adapter instanceof FeatureAdapter ) {
                 FeatureType ft = ( (FeatureAdapter) adapter ).getSchema();
                 if ( ft.getName().equals( propertyPath.getStep( 0 ).getPropertyName() ) ) {
                     FeatureCollection fc = ( (FeatureAdapter) adapter ).getFeatureCollection();
                     Feature feature = fc.getFeature( fid.getAsQualifiedString() );
                     if ( feature != null ) {
                         FeatureProperty featureProperty = feature.getDefaultProperty( propertyPath );
                         if ( selectedGeometries == null ) {
                             selectedGeometries = new ArrayList<FeatureProperty>();
                         }
                         selectedGeometries.add( featureProperty );
                     }
                 }
             }
         }
     }
 
     /**
      * 
      * 
      */
     public void unselectGeometries() {
         if ( selectedGeometries != null ) {
             selectedGeometries.clear();
         }
     }
 
     /**
      * fires a layer changed event encapsulating a feature changed envent that has caused layer changing
      * 
      * @param changeType
      */
     protected void fireLayerChangedEvent( LAYER_CHANGE_TYPE changeType, Object value ) {
         LayerChangedEvent event = new LayerChangedEvent( this, value, changeType, null );
         for ( int i = 0; i < this.listeners.size(); i++ ) {
             this.listeners.get( i ).valueChanged( event );
         }
         owner.valueChanged( event );
     }
 
     /**
      * informes all listeres that the state of a layer been changed in a way that requires repainting
      * 
      */
     public void fireRepaintEvent() {
         LayerChangedEvent event = new LayerChangedEvent( this, null, LAYER_CHANGE_TYPE.dataChanged, null );
         for ( int i = 0; i < this.listeners.size(); i++ ) {
             this.listeners.get( i ).valueChanged( event );
         }
     }
 
     /**
      * informs a layer that one of its data access objects has been refreshed
      * 
      * @param adapter
      */
     public void setDataRefreshed( DataAccessAdapter adapter ) {
         fireLayerChangedEvent( LAYER_CHANGE_TYPE.datasourceChanged, adapter );
     }
 
     /**
      * adds alistener to a layer
      * 
      * @param listener
      */
     public void addChangeListener( ChangeListener listener ) {
         this.listeners.add( listener );
     }
 
     /**
      * removes a listener from a layer
      * 
      * @param listener
      */
     public void removeChangeListener( ChangeListener listener ) {
         this.listeners.remove( listener );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.ChangeListener#valueChanged(org.deegree.igeo.ValueChangedEvent)
      */
     public void valueChanged( ValueChangedEvent event ) {
         LayerChangedEvent ev = null;
         if ( event instanceof AdapterEvent ) {
             ev = new LayerChangedEvent( this, event.getValue(), LAYER_CHANGE_TYPE.dataLoadState, event );
         } else {
             ev = new LayerChangedEvent( this, event.getValue(), LAYER_CHANGE_TYPE.dataChanged, event );
         }
         for ( int i = 0; i < this.listeners.size(); i++ ) {
             listeners.get( i ).valueChanged( ev );
         }
 
     }
 
     /**
      * 
      * @return legend image for a layer
      */
     public BufferedImage getLegend() {
         BufferedImage bi = getCurrentStyle().getLegendImage();
         if ( bi == null ) {
             try {
                 bi = ImageIO.read( Adapter.class.getResource( "missingLegend.png" ) );
                 BufferedImage tmp = new BufferedImage( bi.getWidth() + 130, bi.getHeight(), BufferedImage.TYPE_INT_ARGB );
                 Graphics g = tmp.getGraphics();
                 g.setColor( Color.WHITE );
                 g.fillRect( 0, 0, tmp.getWidth(), tmp.getHeight() );
                 g.setColor( Color.black );
                 g.drawString( getTitle(), bi.getWidth() + 10, bi.getHeight() / 2 + 8 );
                 g.drawImage( bi, 0, 0, null );
                 g.dispose();
                 bi = tmp;
             } catch ( Exception e ) {
                 LOG.logWarning( e.getMessage(), e );
                 throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10055",
                                                                     "missingLegend.png" ) );
             }
         }
         return bi;
     }
 
     @Override
     public boolean equals( Object other ) {
         if ( other == null || !( other instanceof Layer ) ) {
             return false;
         }
         return getIdentifier().equals( ( (Layer) other ).getIdentifier() );
     }
 
     @Override
     public int hashCode() {
         return getIdentifier().hashCode();
     }
 
     @Override
     public String toString() {
         return getTitle();
     }
 
     /**
      * Can be used to convert a collection of layers to a collection of its titles.
      */
     public static final Mapper<String, Layer> ToTitles = new Mapper<String, Layer>() {
         @Override
         public String apply( Layer u ) {
             return u.getTitle();
         }
     };
 
     public void destroy() {
         listeners = null;
        if ( dataAccess != null ) {
            for ( DataAccessAdapter da : this.dataAccess ) {
                da.removeChangeListener( this );
            }
            dataAccess = null;
         }
     }
 
 }
