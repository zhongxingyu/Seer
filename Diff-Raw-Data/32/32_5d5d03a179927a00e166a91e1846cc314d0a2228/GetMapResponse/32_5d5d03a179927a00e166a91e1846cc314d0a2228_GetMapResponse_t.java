 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.factory.FactoryFinder;
 import org.geotools.filter.Filter;
 import org.geotools.map.DefaultMapLayer;
 import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;

import org.geotools.referencing.crs.GeographicCRS;
 import org.geotools.styling.Style;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.GetMapProducerFactorySpi;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 /**
  * A GetMapResponse object is responsible of generating a map based on a GetMap
  * request. The way the map is generated is independent of this class, wich
  * will use a delegate object based on the output format requested
  *
  * @author Gabriel Roldan, Axios Engineering
  * @version $Id: GetMapResponse.java,v 1.11 2004/03/14 23:29:30 groldan Exp $
  */
 public class GetMapResponse implements Response {
     /** DOCUMENT ME! */
     private static final Logger LOGGER = Logger.getLogger(GetMapResponse.class.getPackage()
                                                                               .getName());
 
     /**
      * The map producer that will be used for the production of a map in the
      * requested format.
      */
     private GetMapProducer delegate;
 
     /**
      * Creates a new GetMapResponse object.
      */
     public GetMapResponse() {
         // intentionally left blank
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param req DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      * @throws WmsException DOCUMENT ME!
      */
     public void execute(Request req) throws ServiceException {
         GetMapRequest request = (GetMapRequest) req;
 
         final String outputFormat = request.getFormat();
 
         this.delegate = getDelegate(outputFormat);
 
         final FeatureTypeInfo[] layers = request.getLayers();
         final Style[] styles = (Style[])request.getStyles().toArray(new Style[]{});
 
         final WMSMapContext map = new WMSMapContext();
         
         //DJB: the WMS spec says that the request must not be 0 area
         //     if it is, throw a service exception!
         Envelope env = request.getBbox();
         if (env.isNull() || (env.getWidth() <=0)|| (env.getHeight() <=0))
         {
         	throw new WmsException("The request bounding box has zero area.");
         }
 
        // DJB DONE: replace by setAreaOfInterest(Envelope,
         // CoordinateReferenceSystem)
         // with the user supplied SRS parameter
        
        //if there's a crs in the request, use that.  If not, assume its 4326
        
        CoordinateReferenceSystem mapcrs;
        if ((request.getCrs() == null) || (request.getCrs().equals("")))
        {
        	mapcrs = GeographicCRS.WGS84;
        }
        else
        {
        	//construct the crs object
        	try {
        		mapcrs = CRS.decode(request.getCrs());
        	}
        	catch (Exception e)
			{
        		//couldnt make it - we send off a service exception with the correct info
        		throw new WmsException(e.getLocalizedMessage(),"InvalidSRS");
			}
        }
        
        map.setAreaOfInterest(request.getBbox(),mapcrs);
         map.setMapWidth(request.getWidth());
         map.setMapHeight(request.getHeight());
         map.setBgColor(request.getBgColor());
         map.setTransparent(request.isTransparent());
 
         LOGGER.fine("setting up map");
 
         MapLayer layer;
 
         FeatureSource source;
         for (int i = 0; i < layers.length; i++) {
             Style style = styles[i];
 
             try {
                 source = layers[i].getFeatureSource();
             } catch (IOException exp) {
                 LOGGER.log(Level.SEVERE,
                     "Getting feature source: " + exp.getMessage(), exp);
                 throw new WmsException(null,
                     "Internal error : " + exp.getMessage());
             }
 
             layer = new DefaultMapLayer(source, style);
 
             Filter definitionFilter = layers[i].getDefinitionQuery();
 
             if (definitionFilter != null) {
                 Query definitionQuery = new DefaultQuery(source.getSchema()
                                                                .getTypeName(),
                         definitionFilter);
                 layer.setQuery(definitionQuery);
             }
 
             map.addLayer(layer);
         }
 
         this.delegate.produceMap(map);
     }
 
     /**
      * asks the internal GetMapDelegate for the MIME type of the map that it
      * will generate or is ready to, and returns it
      *
      * @param gs DOCUMENT ME!
      *
      * @return the MIME type of the map generated or ready to generate
      *
      * @throws IllegalStateException if a GetMapDelegate is not setted yet
      */
     public String getContentType(GeoServer gs) throws IllegalStateException {
         if (this.delegate == null) {
             throw new IllegalStateException("No request has been processed");
         }
 
         return this.delegate.getContentType();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentEncoding() {
         LOGGER.finer("returning content encoding null");
 
         return null;
     }
 
     /**
      * if a GetMapDelegate is set, calls it's abort method. Elsewere do
      * nothing.
      *
      * @param gs DOCUMENT ME!
      */
     public void abort(Service gs) {
         if (this.delegate != null) {
             LOGGER.fine("asking delegate for aborting the process");
             this.delegate.abort();
         }
     }
 
     /**
      * delegates the writing and encoding of the results of the request to the
      * <code>GetMapDelegate</code> wich is actually processing it, and has
      * been obtained when <code>execute(Request)</code> was called
      *
      * @param out the output to where the map must be written
      *
      * @throws ServiceException if the delegate throws a ServiceException
      *         inside its <code>writeTo(OuptutStream)</code>, mostly due to
      * @throws IOException if the delegate throws an IOException inside its
      *         <code>writeTo(OuptutStream)</code>, mostly due to
      * @throws IllegalStateException if this method is called before
      *         <code>execute(Request)</code> has succeed
      */
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         if (this.delegate == null) {
             throw new IllegalStateException(
                 "No GetMapDelegate is setted, make sure you have called execute and it has succeed");
         }
 
         LOGGER.finer("asking delegate for write to " + out);
         this.delegate.writeTo(out);
     }
 
     /**
      * Creates a GetMapDelegate specialized in generating the requested map
      * format
      *
      * @param outputFormat a request parameter object wich holds the processed
      *        request objects, such as layers, bbox, outpu format, etc.
      *
      * @return A specialization of <code>GetMapDelegate</code> wich can produce
      *         the requested output map format
      *
      * @throws WmsException if no specialization is configured for the output
      *         format specified in <code>request</code> or if it can't be
      *         instantiated
      */
     static GetMapProducer getDelegate(String outputFormat)
         throws WmsException {
         LOGGER.finer("request format is " + outputFormat);
 
         GetMapProducerFactorySpi mpf = null;
         Iterator mpfi = FactoryFinder.factories(GetMapProducerFactorySpi.class);
 
         while (mpfi.hasNext()) {
             mpf = (GetMapProducerFactorySpi) mpfi.next();
 
             if (mpf.canProduce(outputFormat)) {
                 break;
             }
 
             mpf = null;
         }
 
         if (mpf == null) {
             throw new WmsException("There is no support for creating maps in "
                 + outputFormat + " format", "InvalidFormat");
         }
 
         GetMapProducer producer = mpf.createMapProducer(outputFormat);
 
         return producer;
     }
 
     /**
      * Convenient mehtod to inspect the available
      * <code>GetMapProducerFactorySpi</code> and return the set of all the map
      * formats' MIME types that the producers can handle
      *
      * @return a Set&lt;String&gt; with the supported mime types.
      */
     public static Set getMapFormats() {
         Set mapFormats = new HashSet();
         GetMapProducerFactorySpi mpf;
         Iterator mpfi = FactoryFinder.factories(GetMapProducerFactorySpi.class);
 
         while (mpfi.hasNext()) {
             mpf = (GetMapProducerFactorySpi) mpfi.next();
             mapFormats.addAll(mpf.getSupportedFormats());
         }
 
         return mapFormats;
     }
 
 }
