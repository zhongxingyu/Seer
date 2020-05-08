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
 
 package org.deegree.igeo.dataadapter.wms;
 
 import static org.deegree.framework.util.MapUtils.DEFAULT_PIXEL_SIZE;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.HttpUtils;
 import org.deegree.framework.util.ImageUtils;
 import org.deegree.framework.util.KVP2Map;
 import org.deegree.framework.util.MapUtils;
 import org.deegree.framework.util.StringTools;
 import org.deegree.framework.xml.XMLFragment;
 import org.deegree.framework.xml.XMLParsingException;
 import org.deegree.framework.xml.XMLTools;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.dataadapter.DataAccessException;
 import org.deegree.igeo.dataadapter.GridCoverageAdapter;
 import org.deegree.igeo.dataadapter.OWSURLUtils;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.mapmodel.Datasource;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.mapmodel.WMSDatasource;
 import org.deegree.igeo.settings.WMSGridCoverageAdapterSettings;
 import org.deegree.model.Identifier;
 import org.deegree.model.coverage.grid.GridCoverage;
 import org.deegree.model.coverage.grid.ImageGridCoverage;
 import org.deegree.model.crs.CRSTransformationException;
 import org.deegree.model.crs.GeoTransformer;
 import org.deegree.model.feature.FeatureCollection;
 import org.deegree.model.feature.GMLFeatureCollectionDocument;
 import org.deegree.model.spatialschema.Envelope;
 import org.deegree.ogcwebservices.OGCWebServiceException;
 import org.deegree.ogcwebservices.OWSUtils;
 import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
 import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
 import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
 import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
 import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
 import org.deegree.ogcwebservices.wms.operation.GetMap;
 import org.xml.sax.SAXException;
 
 /**
  * Concrete implementation of {@link GridCoverageAdapter} for reading raster data from a WMS
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version. $Revision$, $Date$
  */
 public class WMSGridCoverageAdapter extends GridCoverageAdapter {
 
     private static final ILogger LOG = LoggerFactory.getLogger( WMSGridCoverageAdapter.class );
 
     private GetMap baseRequest;
 
     private int timeout = 25000;
 
     private URL getMapHttpGetURL;
 
     @SuppressWarnings("unused")
     private URL getMapHttpPostURL;
 
     private URL getFeatureInfoURL;
 
     private String version;
 
     private WMSGridCoverageAdapterSettings wmsSet;
 
     private static Map<String, XMLFragment> capabilitiesCache;
 
     private URL baseURL;
 
     static {
         if ( capabilitiesCache == null ) {
             capabilitiesCache = new HashMap<String, XMLFragment>();
         }
     }
 
     /**
      * 
      * @param datasource
      * @param layer
      * @param mapModel
      * @param baseURL
      */
     public WMSGridCoverageAdapter( Datasource datasource, Layer layer, MapModel mapModel, URL baseURL ) {
         super( datasource, layer, mapModel );
         this.baseURL = baseURL;
         String tmp = ( (WMSDatasource) datasource ).getBaseRequest();
         Envelope bbox = mapModel.getEnvelope();
         datasource.setExtent( bbox );
         tmp = StringTools.concat( 2000, tmp, "&BBOX=", bbox.getMin().getX(), ',', bbox.getMin().getY(), ',',
                                   bbox.getMax().getX(), ',', bbox.getMax().getY() );
 
         wmsSet = mapModel.getApplicationContainer().getSettings().getWMSGridCoveragesAdapter();
         // read request timeout from deegree configuration
         timeout = wmsSet.getTimeout();
 
         // try using baseURL; if using baseURL fails when performing a request, URLs will be read
         // from capabilities
         useBaseURL( baseURL );
 
         Map<String, String> param = KVP2Map.toMap( tmp );
         // ensure that GetMap request will be created for version read from WMS capabilities
         param.put( "ID", new Identifier().getAsQualifiedString() );
 
         version = ( (WMSDatasource) datasource ).getServiceVersion();
         LOG.logDebug( "requested WMS version: ", version );
         param.put( "VERSION", version );
         // dummy values that will be overwritten when requsting a map
         param.put( "WIDTH", "10" );
         param.put( "HEIGHT", "10" );
         if ( "1.3.0".compareTo( version ) <= 0 ) {
             param.put( "CRS", bbox.getCoordinateSystem().getPrefixedName() );
         } else {
             param.put( "SRS", bbox.getCoordinateSystem().getPrefixedName() );
         }
 
         try {
             baseRequest = GetMap.create( param );
         } catch ( Exception e ) {
             LOG.logError( e.getMessage(), e );
             throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10029", tmp ) );
         }
 
     }
 
     /**
      * 
      * @param baseURL
      */
     private void useBaseURL( URL baseURL ) {
         try {
             URL url = OWSURLUtils.normalizeOWSURL( baseURL );
             getMapHttpGetURL = url;
             getMapHttpPostURL = url;
             getFeatureInfoURL = url;
         } catch ( MalformedURLException e ) {
             LOG.logError( e );
         }
     }
 
     /**
      * reads destination URLs for GetFeature, DescribeFeatureType and Transaction requests from WFS capabilities
      * document
      * 
      * @param baseURL
      */
     private void readURLs( URL baseURL ) {
 
         LOG.logInfo( "read request URLs from capabilities" );
 
         XMLFragment xml;
         String capabilitiesUrl = null;
         try {
             ApplicationContainer<?> appCont = mapModel.getApplicationContainer();
             capabilitiesUrl = baseURL.toURI().toASCIIString();
             String tmp = HttpUtils.normalizeURL( capabilitiesUrl );
             capabilitiesUrl = HttpUtils.addAuthenticationForKVP( capabilitiesUrl, appCont.getUser(),
                                                                  appCont.getPassword(), appCont.getCertificate( tmp ) );
             baseURL = new URL( capabilitiesUrl );
             if ( capabilitiesCache.containsKey( capabilitiesUrl ) ) {
                 xml = capabilitiesCache.get( capabilitiesUrl );
                 LOG.logInfo( "read capabilities from cache: ", capabilitiesUrl );
             } else {
                 InputStream is = HttpUtils.performHttpGet( capabilitiesUrl, null, wmsSet.getTimeout(),
                                                            appCont.getUser(), appCont.getPassword(), null ).getResponseBodyAsStream();
                 xml = new XMLFragment();
                 xml.load( is, baseURL.toExternalForm() );
                 capabilitiesCache.put( capabilitiesUrl, xml );
             }
         } catch ( SAXException e ) {
             LOG.logError( e.getMessage(), e );
             String s = StringTools.stackTraceToString( e );
             throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10033", capabilitiesUrl ) + s );
         } catch ( Exception e ) {
             LOG.logError( e.getMessage(), e );
             String s = StringTools.stackTraceToString( e );
             throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10032", capabilitiesUrl ) + s );
         }
 
         try {
             version = XMLTools.getRequiredAttrValue( "version", null, xml.getRootElement() );
         } catch ( XMLParsingException e ) {
             LOG.logError( e.getMessage(), e );
             throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10034", capabilitiesUrl,
                                                                 xml.getAsPrettyString() ) );
         }
 
         WMSCapabilitiesDocument doc = new WMSCapabilitiesDocument();
         doc.setRootElement( xml.getRootElement() );
         WMSCapabilities wmsCapa;
         try {
             wmsCapa = (WMSCapabilities) doc.parseCapabilities();
         } catch ( InvalidCapabilitiesException e ) {
             LOG.logError( e.getMessage(), e );
             throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10034", capabilitiesUrl,
                                                                 xml.getAsPrettyString() ) );
         }
         getMapHttpGetURL = OWSUtils.getHTTPGetOperationURL( wmsCapa, GetMap.class );
         getMapHttpPostURL = OWSUtils.getHTTPPostOperationURL( wmsCapa, GetMap.class );
         getFeatureInfoURL = OWSUtils.getHTTPGetOperationURL( wmsCapa, GetFeatureInfo.class );
     }
 
     /**
      * 
      * @param coverageDescription
      */
     public void setCoverageDescription( CoverageDescription coverageDescription ) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * 
      * @param gridCoverage
      */
     public void setCoverage( GridCoverage gridCoverage ) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * 
      * @return adapted coverage
      */
     public GridCoverage getCoverage() {
         readMapImage();
         return this.coverage;
     }
 
     /**
      * reads a map image from a WMS and assigns it to the internal grid coverage
      * 
      */
     private void readMapImage() {
         fireStartLoadingEvent();
         // update GetMap request with current state of the map model
         Envelope bbox = mapModel.getEnvelope();
 
         int width = mapModel.getTargetDevice().getPixelWidth();
         int height = mapModel.getTargetDevice().getPixelHeight();
         String srs = mapModel.getCoordinateSystem().getPrefixedName();
         GeoTransformer transformBack = null;
         if ( !datasource.getNativeCoordinateSystem().equals( mapModel.getCoordinateSystem() ) ) {
             // perform CRS transformation of the request bounding box and resizing of the requested image
             // to enable transformation and cutting the result of a GetMap request back to the CRS of
             // the map model without lost of data at the image boundaries
             srs = datasource.getNativeCoordinateSystem().getPrefixedName();
             GeoTransformer transformer = new GeoTransformer( datasource.getNativeCoordinateSystem() );
             transformBack = new GeoTransformer( mapModel.getCoordinateSystem() );
             try {
                 bbox = transformer.transform( bbox, mapModel.getCoordinateSystem(), true );
             } catch ( CRSTransformationException e ) {
                 LOG.logError( e.getMessage(), e );
                 throw new DataAccessException( e.getMessage() );
             }
 
             double scale = MapUtils.calcScale( width, height, mapModel.getEnvelope(), mapModel.getCoordinateSystem(),
                                                DEFAULT_PIXEL_SIZE );
             double newScale = MapUtils.calcScale( width, height, bbox, datasource.getNativeCoordinateSystem(),
                                                   DEFAULT_PIXEL_SIZE );
             double ratio = scale / newScale;
             width = (int) Math.round( width * ratio );
             height = (int) Math.round( height * ratio );
         }
 
         baseRequest.setSrs( srs );
         baseRequest.setBoundingBox( bbox );
         baseRequest.setWidth( width );
         baseRequest.setHeight( height );
 
         InputStream is = null;
         String getMapUrl = null;
         try {
             ApplicationContainer<?> appCont = mapModel.getApplicationContainer();
             getMapUrl = getMapHttpGetURL.toURI().toASCIIString();
 
             boolean allowSwapAxis = ( (WMSDatasource) datasource ).isAllowSwapAxis();
             String param = HttpUtils.addAuthenticationForKVP( baseRequest.getRequestParameter( allowSwapAxis ),
                                                               appCont.getUser(), appCont.getPassword(),
                                                               appCont.getCertificate( getMapUrl ) );
             LOG.logDebug( "base WMS URL ", getMapUrl );
             LOG.logDebug( "WMS GetMap parameter ", param );
             is = HttpUtils.performHttpGet( getMapUrl, param, timeout, appCont.getUser(), appCont.getPassword(), null ).getResponseBodyAsStream();
         } catch ( Exception e ) {
             if ( getMapHttpGetURL == null ) {
                 // read target URLs (and WMS version) from WMS capabilities
                 readURLs( baseURL );
                 readMapImage();
                 return;
             }
             LOG.logError( e.getMessage(), e );
             fireLoadingExceptionEvent();
             try {
                 throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10030", getMapUrl,
                                                                     baseRequest.getRequestParameter() ) );
             } catch ( OGCWebServiceException e1 ) {
                 e1.printStackTrace();
             }
         }
 
         // read data from WMS into a byte array to enable debugging and detailed error messages
         // if result can not be parsed as image
         ByteArrayOutputStream bos = new ByteArrayOutputStream( 100000 );
         byte[] b = new byte[100000];
         try {
             int c = is.read( b );
             do {
                 bos.write( b, 0, c );
                 c = is.read( b );
             } while ( c > 0 );
         } catch ( Exception e ) {
             LOG.logError( e.getMessage(), e );
             try {
                 throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10031", getMapUrl,
                                                                     baseRequest.getRequestParameter(), e.getMessage() ) );
             } catch ( OGCWebServiceException e1 ) {
                 e1.printStackTrace();
             }
         }
 
         BufferedImage image = null;
         try {
             image = ImageUtils.loadImage( new ByteArrayInputStream( bos.toByteArray() ) );
             bos.close();
         } catch ( Exception e ) {
             LOG.logError( e.getMessage(), e );
             try {
                 throw new DataAccessException( Messages.getMessage( Locale.getDefault(), "$DG10031", getMapUrl,
                                                                     baseRequest.getRequestParameter(),
                                                                     new String( bos.toByteArray() ) ) );
             } catch ( OGCWebServiceException e1 ) {
                 e1.printStackTrace();
             }
         }
         if ( !datasource.getNativeCoordinateSystem().equals( mapModel.getCoordinateSystem() ) ) {
             // perform transformation and cutting of the result of a GetMap request if map model CRS and
             // CRS supported by requested layer(s) are not the same
             try {
                 image = transformBack.transform( image, bbox, mapModel.getEnvelope(),
                                                  mapModel.getTargetDevice().getPixelWidth(),
                                                  mapModel.getTargetDevice().getPixelHeight(), 25, 3, null );
             } catch ( CRSTransformationException e ) {
                 LOG.logError( e.getMessage(), e );
                 throw new DataAccessException( e.getMessage() );
             }
         }
         this.coverage = new ImageGridCoverage( null, mapModel.getEnvelope(), image );
         fireLoadingFinishedEvent();
     }
 
     /**
      * 
      * @param envelope
      * @return result to feature info
      */
     public FeatureCollection getFeatureInfo( int x, int y )
                             throws Exception {
         try {
             Envelope bbox = mapModel.getEnvelope();
 
             int width = mapModel.getTargetDevice().getPixelWidth();
             int height = mapModel.getTargetDevice().getPixelHeight();
             String srs = mapModel.getCoordinateSystem().getPrefixedName();
             baseRequest.setSrs( srs );
             baseRequest.setBoundingBox( bbox );
             baseRequest.setWidth( width );
             baseRequest.setHeight( height );
             ApplicationContainer<?> appCont = mapModel.getApplicationContainer();
             String getFiUrl = getFeatureInfoURL.toURI().toASCIIString();
             StringBuilder sb = new StringBuilder( 1000 );
 
             int cnt = appCont.getSettings().getWMSGridCoveragesAdapter().getFeatureCount();
            sb.append( "feature_count=" ).append( cnt ).append( "&x=" ).append( x ).append( "&" );
            sb.append( "y=" ).append( y );
            sb.append( "&version=" ).append( baseRequest.getVersion() );
             sb.append( StringTools.replace( baseRequest.getRequestParameter(), "GetMap", "GetFeatureInfo", false ) );
             sb.append( "&QUERY_layers=" );
             org.deegree.ogcwebservices.wms.operation.GetMap.Layer[] ll = baseRequest.getLayers();
             for ( int i = 0; i < ll.length; i++ ) {
                 sb.append( ll[i].getName() );
                 if ( i < ll.length - 1 ) {
                     sb.append( ',' );
                 }
             }

             String param = HttpUtils.addAuthenticationForKVP( sb.toString(), appCont.getUser(), appCont.getPassword(),
                                                               appCont.getCertificate( getFiUrl ) );
             LOG.logDebug( "GetFeatureInfo request: ", param );
             LOG.logDebug( "GetFeatureInfo request URL: ", getFiUrl );
             InputStream is = HttpUtils.performHttpGet( getFiUrl, param, timeout, appCont.getUser(),
                                                        appCont.getPassword(), null ).getResponseBodyAsStream();
             GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument( true );
             doc.load( is, getFiUrl );
             if ( doc.getRootElement().getLocalName().equalsIgnoreCase( "ServiceExceptionReport" ) ) {
                 throw new Exception( doc.getAsPrettyString() );
             }
             return doc.parse();
         } catch ( Exception e ) {
             if ( getFeatureInfoURL == null ) {
                 // read target URLs (and WMS version) from WMS capabilities
                 readURLs( baseURL );
                 return getFeatureInfo( x, y );
             }
             throw e;
         }
     }
 
     @Override
     public void refresh() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void refresh( boolean forceReload ) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void commitChanges()
                             throws IOException {
         // A WMSGridCoverageAdpter can not deal changes and so can/need not to commit changes to its
         // backend/WMS
     }
 
 }
