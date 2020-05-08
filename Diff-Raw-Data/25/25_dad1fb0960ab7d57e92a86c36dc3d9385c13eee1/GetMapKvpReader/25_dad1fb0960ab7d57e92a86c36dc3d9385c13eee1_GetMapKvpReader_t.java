 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.requests;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.geoserver.platform.ServiceException;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureReader;
 import org.geotools.data.Query;
 import org.geotools.data.Transaction;
 import org.geotools.data.crs.ForceCoordinateSystemFeatureReader;
 import org.geotools.data.memory.MemoryDataStore;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureTypes;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.styling.FeatureTypeConstraint;
 import org.geotools.styling.NamedLayer;
 import org.geotools.styling.NamedStyle;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleAttributeExtractor;
 import org.geotools.styling.StyleFactory;
 import org.geotools.styling.StyledLayer;
 import org.geotools.styling.StyledLayerDescriptor;
 import org.geotools.styling.UserLayer;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.config.PaletteManager;
 import org.vfny.geoserver.global.CoverageInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.TemporaryFeatureTypeInfo;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.util.SLDValidator;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.responses.palette.InverseColorMapOp;
 import org.vfny.geoserver.wms.servlets.WMService;
 import org.xml.sax.InputSource;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * Builds a GetMapRequest object given by a set of CGI parameters supplied in
  * the constructor.
  * 
  * <p>
  * Mandatory parameters:
  * 
  * <ul>
  * <li> LAYERS layer names, as exposed by the capabilities document, to compose
  * a map with, in the order they may appear, being the first layer the one at
  * the bottom of the layer stack and the last one the one at the top. </li>
  * <li> STYLES list of named styles known by this server and applicable to the
  * requested layers. It can be empty or contain exactly as many style names as
  * layers was requested, in which case empty strings could be used to denote
  * that the default layer style should be used. (exaple:
  * <code>LAYERS=buildings,roads,railroads&STYLES=,centerline,</code>. This
  * example says create a map with roads layer using its default style, roads
  * with "centerline" style, and railroads with its default style. </li>
  * <li> BBOX Area of interest for which to contruct the map image, in the
  * Coordinate Reference System given by the SRS parameter. </li>
  * <li> FORMAT MIME type of the resulting map, must be one of the advertised in
  * the capabilities document. </li>
  * <li> WIDTH desired map witdth in output units (pixels). UNITS support should
  * be added to the spec, and UNITS and DPI parameters added. </li>
  * <li> HEIGHT desired map height in output units (pixels). UNITS support should
  * be added to the spec, and UNITS and DPI parameters added. </li>
  * </ul>
  * </p>
  * 
  * <p>
  * Optional parameters:
  * 
  * <ul>
  * <li> SRS </li>
  * <li> TRANSPARENT boolean indicatin wether to create a map with transparent
  * background or not (if transparency is supported by the requested output
  * format). </li>
  * <li> EXCEPTIONS MIME type of the exception report. </li>
  * <li> BGCOLOR map background color, in <code>0xRRGGBB</code> format. </li>
  * <li> SLD client supplies a URL for a remote SLD document through this
  * parameter. This parameter takes precedence over STYLES. If present, replaces
  * the LAYERS and STYLES parameters, since they're defined in the remote
  * document itself. The document send by this way will be used in "literal" or
  * "library" mode, see explanation bellow. </li>
  * <li> SLD_BODY client spplies the SLD document itself through this parameter,
  * properly encoded for an HTTP query string. This parameter takes precendence
  * over STYLES and SLD. If present, replaces the LAYERS and STYLES parameters,
  * since they're defined in the inline document itself. The document send by
  * this way will be used in "literal" or "library" mode, see explanation bellow.
  * </li>
  * </ul>
  * </p>
  * 
  * <p>
  * As defined by the Styled Layer Descriptor specification, version 1.0.0, the
  * SLD document supplied by the SLD or SLD_BODY parameter can be used in
  * "literal" or "library" mode, depending on whether the <strong>LAYERS=</strong>
  * parameter is present.
  * </p>
  * 
  * <p>
  * Here is the explanation from the spec, section 6.4, page 10: "the SLD can
  * also be used in one of two different modes depending on whether the LAYERS
  * parameter is present in the request. If it is not present, then all layers
  * identified in the SLD document are rendered with all defined styles, which is
  * equivalent to the XML-POST method of usage. If the LAYERS parameter is
  * present, then only the layers identified by that parameter are rendered and
  * the SLD is used as a style library . "
  * </p>
  * 
  * @author Gabriel Roldan, Axios Engineering
  * @author Simone Giannecchini, GeoSolutions
  * @version $Id$
  * 
  * @task TODO: parse and respect SRS parameter (needs reprojection support)
  * @deprecated
  */
 public class GetMapKvpReader extends WmsKvpRequestReader {
 	/** DOCUMENT ME! */
 	private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.requests.readers.wms");
 
 	/** Used to parse SLD documents from SLD and SLD_BODY parameters */
 	private static final StyleFactory styleFactory = CommonFactoryFinder
 			.getStyleFactory(null);
 
 	/**
 	 * Indicates wether STYLES parameter must be parsed. Defaults to
 	 * <code>true</code>, but can be set to false, for example, when parsing
 	 * a GetFeatureInfo request, which shares most of the getmap parameter but
 	 * not STYLES.
 	 * 
 	 * @task TODO: refactor this so it dont stay _so_ ugly
 	 */
 	private boolean stylesRequired = true;
	
	/**
	 * Indicates whether the FORMAT parameter is required or not. Defaults to true as
	 * per the GetMap request requires in the spec, but can be set to false for a 
	 * GetFeatureInfo request using this parser to be permisive about the FORMAT parameter.
	 */
	private boolean formatRequired = true;
 
         /**
          * Creates a new GetMapKvpReader object.
          * 
          * @param kvpPairs
          *            Key Values pairs of the request
          * @param wms
          *            The WMS config object.
          */
         public GetMapKvpReader(Map kvpPairs, WMS wms) {
                 super(kvpPairs, wms);
         }
         
 	/**
 	 * Sets wether the STYLES parameter must be parsed
 	 * 
 	 * @param parseStyles
 	 */
 	public void setStylesRequired(boolean parseStyles) {
 		this.stylesRequired = parseStyles;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public boolean isStylesRquired() {
 		return this.stylesRequired;
 	}
 
 	/**
 	 * Produces a <code>GetMapRequest</code> instance by parsing the GetMap
 	 * mandatory, optional and custom parameters.
 	 * 
 	 * @param httpRequest
 	 *            the servlet request who's application object holds the server
 	 *            configuration
 	 * 
 	 * @return a <code>GetMapRequest</code> completely setted up upon the
 	 *         parameters passed to this reader
 	 * 
 	 * @throws ServiceException
 	 *             DOCUMENT ME!
 	 */
 	public Request getRequest(HttpServletRequest httpRequest)
 			throws ServiceException {
 		GetMapRequest request = new GetMapRequest(getWMS());
 		request.setHttpServletRequest(httpRequest);
 
 		String version = getRequestVersion();
 		request.setVersion(version);
 
 		parseMandatoryParameters(request, true);
 		parseOptionalParameters(request);
 
 		return request;
 	}
 
 	/**
 	 * Parses the optional parameters:
 	 * 
 	 * <ul>
 	 * <li> SRS </li>
 	 * <li> TRANSPARENT </li>
 	 * <li> EXCEPTIONS </li>
 	 * <li> BGCOLOR </li>
 	 * </ul>
 	 * 
 	 * 
 	 * @param request
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 * 
 	 * @task TODO: implement parsing of transparent, exceptions and bgcolor
 	 */
 	public void parseOptionalParameters(GetMapRequest request)
 			throws WmsException {
 		// SRS
 		String epsgCode = getValue("SRS");
 
 		if (epsgCode != null) {
 			try {
 				CoordinateReferenceSystem mapcrs = CRS.decode(epsgCode);
 				request.setCrs(mapcrs);
 				request.setSRS(epsgCode);
 			} catch (Exception e) {
 				// couldnt make it - we send off a service exception with the
 				// correct info
 				throw new WmsException(e.getLocalizedMessage(), "InvalidSRS");
 			}
 		}
 
 		// transparency
 		String transparentValue = getValue("TRANSPARENT");
 		boolean transparent = (transparentValue == null) ? false : Boolean
 				.valueOf(transparentValue).booleanValue();
 		request.setTransparent(transparent);
 
 		// background
 		String bgcolor = getValue("BGCOLOR");
 
 		if (bgcolor != null) {
 			try {
 				request.setBgColor(Color.decode(bgcolor));
 			} catch (NumberFormatException nfe) {
 				throw new WmsException("BGCOLOR " + bgcolor
 						+ " incorrectly specified (0xRRGGBB format expected)");
 			}
 		}
 
 		// filter parsing
 		parseFilterParam(request);
 
 		// buffer
 		String bufferValue = getValue("BUFFER");
 		int buffer = 0;
 
 		if (bufferValue != null) {
 			try {
 				buffer = Integer.parseInt(bufferValue);
 			} catch (NumberFormatException nfe) {
 				throw new WmsException("BUFFER " + bufferValue
 						+ " incorrectly specified (expected an integer)");
 			}
 		}
 
 		request.setBuffer(buffer);
 
 		// palette
 		String paletteValue = getValue("PALETTE");
 
 		if (paletteValue != null) {
 			try {
 				final InverseColorMapOp eicm = PaletteManager
 						.getPalette(paletteValue);
 				if (eicm == null) {
 					throw new WmsException("Palette " + paletteValue
 							+ " could not be found "
 							+ "in $GEOSERVER_DATA_DIR/palettes directory");
 				}
 
 				request.setPalette(eicm);
 			} catch (Exception e) {
 				throw new WmsException(e, "Palette " + paletteValue
 						+ " could not be loaded", null);
 			}
 		}
 
 		// tiling hint
 		String tiledValue = getValue("TILED");
 		request.setTiled("TRUE".equalsIgnoreCase(tiledValue));
 
 		// tiling origin
 		String origin = getValue("TILESORIGIN");
 
 		if (origin != null) {
 			request.setTilesOrigin(parseTilesOrigin(origin));
 		}
 
 		// feature version (for versioned requests)
 		String featureVersion = getValue("FEATUREVERSION");
 		request.setFeatureVersion(featureVersion);
 
 		/** KML/KMZ score value */
 		String KMScore = getValue("KMSCORE");
 
 		if (KMScore != null) {
 			try {
 				// handle special string cases of "vector" or "raster"
 				if (KMScore.equalsIgnoreCase("vector")) {
 					KMScore = "100"; // vector default
 				} else if (KMScore.equalsIgnoreCase("raster")) {
 					KMScore = "0"; // raster default
 				}
 
 				Integer s = new Integer(KMScore);
 				int score = s.intValue();
 
 				if ((score < 0) || (score > 100)) {
 					throw new NumberFormatException(
 							"KMScore not between 0 and 100. "
 									+ "If you wish not to use it, do not specify KMScore as a parameter.");
 				}
 
 				request.setKMScore(score);
 
 				if (LOGGER.isLoggable(Level.INFO)) {
 					LOGGER.info("Set KMScore: " + score);
 				}
 			} catch (NumberFormatException e) {
 				throw new WmsException(
 						"KMScore parameter ("
 								+ KMScore
 								+ ") incorrectly specified. "
 								+ "Expecting an integer value between between 0 and 100");
 			}
 		}
 
 		/** KMattr: 'full' or 'no' attribution for KML placemark <description> */
 		String KMAttr = getValue("KMATTR");
 
 		if (KMAttr != null) {
 			if (KMAttr.equalsIgnoreCase("no")
 					|| KMAttr.equalsIgnoreCase("false")
 					|| KMAttr.equalsIgnoreCase("0")) {
 				request.setKMattr(false);
 			} else {
 				request.setKMattr(true); // default to true
 			}
 		}
 
 		/** KML super overlay */
 		String superOverlay = getValue("SUPEROVERLAY");
 
 		if (superOverlay != null) {
 			request.setSuperOverlay("TRUE".equalsIgnoreCase(superOverlay));
 		}
 
 		/** KML legend */
 		String legend = getValue("LEGEND");
 
 		if (legend != null) {
 			request.setLegend("TRUE".equalsIgnoreCase(legend)
 					|| "ON".equalsIgnoreCase(legend));
 		}
 
 //		/** TIME: a time stamp for multidim coverages <description> */
 //		String time = getValue("TIME");
 //
 //		if (time != null) {
 //			request.setTime(Integer.valueOf(time));
 //
 //			if (LOGGER.isLoggable(Level.INFO)) {
 //				LOGGER.info("Set TIME: " + time);
 //			}
 //		}
 
 		/**
 		 * ELEVATION: elevation (or depth) valu for multidim coverages
 		 * <description>
 		 */
 		String elev = getValue("ELEVATION");
 
 		if (elev != null) {
 			request.setElevation(Integer.valueOf(elev));
 
 			if (LOGGER.isLoggable(Level.INFO)) {
 				LOGGER.info("Set ELEVATION: " + elev);
 			}
 		}
 	}
 
 	private Point2D parseTilesOrigin(String origin) {
 		Object[] coordValues = readFlat(origin, INNER_DELIMETER).toArray();
 
 		if (coordValues.length != 2) {
 			throw new ServiceException(origin + " is not a valid coordinate",
 					getClass().getName());
 		}
 
 		try {
 			double minx = Double.parseDouble(coordValues[0].toString());
 			double miny = Double.parseDouble(coordValues[1].toString());
 
 			return new Point2D.Double(minx, miny);
 		} catch (NumberFormatException ex) {
 			throw new ServiceException(ex,
 					"Illegal value for TILESORIGIN parameter: " + origin,
 					getClass().getName() + "::parseTilesOrigin()");
 		}
 	}
 
 	/**
 	 * Parses the mandatory GetMap request parameters:
 	 * 
 	 * <p>
 	 * Mandatory parameters:
 	 * 
 	 * <ul>
 	 * <li> LAYERS </li>
 	 * <li> STYLES ommited if SLD or SLD_BODY parameters are supplied </li>
 	 * <li> BBOX </li>
 	 * <li> FORMAT </li>
 	 * <li> WIDTH </li>
 	 * <li> HEIGHT </li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param request
 	 *            DOCUMENT ME!
 	 * @parseStylesLayers true = normal operation, false = dont parse the styles
 	 *                    and layers (used by the SLD GET/POST)
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 */
 	public void parseMandatoryParameters(GetMapRequest request,
 			boolean parseStylesLayers) throws WmsException {
 		try {
 			int width = Integer.parseInt(getValue("WIDTH"));
 			int height = Integer.parseInt(getValue("HEIGHT"));
 			request.setWidth(width);
 			request.setHeight(height);
 		} catch (NumberFormatException ex) {
 			throw new WmsException("WIDTH and HEIGHT incorrectly specified");
 		}
 
 		String format = getValue("FORMAT");
 
		if (format == null && isFormatRequired()) {
 			throw new WmsException("parameter FORMAT is required");
 		}
 
 		request.setFormat(format);
 
 		Envelope bbox = parseBbox(getValue("BBOX"));
 		request.setBbox(bbox);
 
 		// let styles and layers parsing for the end to give more trivial
 		// parameters
 		// a chance to fail before incurring in retrieving the SLD or SLD_BODY
 		if (parseStylesLayers) {
 			parseLayersAndStyles(request);
 		}
 	}
 
 	protected Envelope parseBbox(String bboxParam) throws WmsException {
 		// overridden to throw the right exception for this context
 		try {
 			return super.parseBbox(bboxParam);
 		} catch (ServiceException e) {
 			throw new WmsException(e);
 		}
 	}
 
 	/**
 	 * creates a list of requested attributes, wich must be a valid attribute
 	 * name or one of the following special attributes:
 	 * 
 	 * <ul>
 	 * <li> <b>#FID</b>: a map producer capable of handling attributes (such as
 	 * SVGMapResponse), will write the feature id of each feature </li>
 	 * <li> <b>#BOUNDS</b>: a map producer capable of handling attributes (such
 	 * as SVGMapResponse), will write the bounding box of each feature </li>
 	 * </ul>
 	 * 
 	 * 
 	 * @param layers
 	 *            info about the requested map layers
 	 * 
 	 * @return an empty list if no attributes was requested, or a
 	 *         <code>List&lt;List&lt;String&gt;&gt;</code> with an entry for
 	 *         each requested layer, where each of them consists of a List of
 	 *         the attribute names requested
 	 * 
 	 * @throws WmsException
 	 *             if:
 	 *             <ul>
 	 *             <li>the number of attribute sets requested is not equal to
 	 *             the number of layers requested.</li>
 	 *             <li>an illegal attribute name was requested</li>
 	 *             <li>an IOException occurs while fetching a FeatureType
 	 *             schema to ask it for propper attribute names</li>
 	 *             </ul>
 	 */
 	private List parseAttributes(FeatureTypeInfo[] layers) throws WmsException {
 		String rawAtts = getValue("ATTRIBUTES");
 
 		if (LOGGER.isLoggable(Level.FINER)) {
 			LOGGER.finer(new StringBuffer("parsing attributes ")
 					.append(rawAtts).toString());
 		}
 
 		if ((rawAtts == null) || "".equals(rawAtts)) {
 			return Collections.EMPTY_LIST;
 		}
 
 		// raw list of attributes for each feature type requested
 		List byFeatureTypes = readFlat(rawAtts, "|");
 		int nLayers = layers.length;
 
 		if (byFeatureTypes.size() != nLayers) {
 			throw new WmsException(byFeatureTypes.size()
 					+ " lists of attributes specified, expected "
 					+ layers.length, getClass().getName()
 					+ "::parseAttributes()");
 		}
 
 		// fill byFeatureTypes with the split of its raw attributes requested
 		// separated by commas, and check for the validity of each att name
 		SimpleFeatureType schema;
 		List atts;
 		String attName;
 
 		for (int i = 0; i < nLayers; i++) {
 			rawAtts = (String) byFeatureTypes.get(i);
 
 			atts = readFlat(rawAtts, ",");
 			byFeatureTypes.set(i, atts);
 
 			// FeatureType schema = layers[i].getSchema();
 			try {
 				schema = layers[i].getFeatureType();
 
 				// verify that propper attributes has been requested
 				for (Iterator attIt = atts.iterator(); attIt.hasNext();) {
 					attName = (String) attIt.next();
 
 					if (attName.length() > 0) {
 						if (LOGGER.isLoggable(Level.FINER)) {
 							LOGGER.finer(new StringBuffer("checking that ")
 									.append(attName).append(" is valid")
 									.toString());
 						}
 
 						if ("#FID".equalsIgnoreCase(attName)
 								|| "#BOUNDS".equalsIgnoreCase(attName)) {
 							if (LOGGER.isLoggable(Level.FINER)) {
 								LOGGER.finer(new StringBuffer(
 										"special attribute name requested: ")
 										.append(attName).toString());
 							}
 
 							continue;
 						}
 
 						if (schema.getDescriptor(attName) == null) {
 							throw new WmsException("Attribute '" + attName
 									+ "' requested for layer "
 									+ schema.getTypeName() + " does not exists");
 						}
 					} else {
 						if (LOGGER.isLoggable(Level.FINEST)) {
 							LOGGER
 									.finest("removing empty attribute name from request");
 						}
 
 						attIt.remove();
 					}
 				}
 
 				if (LOGGER.isLoggable(Level.FINEST)) {
 					LOGGER.finest(new StringBuffer("attributes requested for ")
 							.append(schema.getTypeName()).append(" checked: ")
 							.append(rawAtts).toString());
 				}
 			} catch (java.io.IOException e) {
 				throw new WmsException(e);
 			}
 		}
 
 		return byFeatureTypes;
 	}
 
 	/**
 	 * Parses the list of style names requested for each requested layer and
 	 * looks up the actual Style objects, which are returned in an ordered list.
 	 * 
 	 * <p>
 	 * A client _may_ request teh default Style using a null value (as in
 	 * "STYLES="). If several layers are requested with a mixture of named and
 	 * default styles, the STYLES parameter includes null values between commas
 	 * (as in "STYLES=style1,,style2,,"). If all layers are to be shown using
 	 * the default style, either the form "STYLES=" or "STYLES=,,," is valid.
 	 * </p>
 	 * 
 	 * @param request
 	 *            DOCUMENT ME!
 	 * @param layers
 	 *            the requested feature types
 	 * 
 	 * @return a full <code>List</code> of the style names requested for the
 	 *         requiered layers with no null style names.
 	 * 
 	 * @throws WmsException
 	 *             if some of the requested styles does not exist or its number
 	 *             if greater than zero and distinct of the number of requested
 	 *             layers
 	 */
 	protected List parseStylesParam(GetMapRequest request, MapLayerInfo[] layers)
 			throws WmsException {
 		String rawStyles = getValue("STYLES");
 		List styles = new ArrayList(layers.length);
 
 		int numLayers = layers.length;
 
 		if ("".equals(rawStyles)) {
 			if (LOGGER.isLoggable(Level.FINER)) {
 				LOGGER
 						.finer("Assigning default style to all the requested layers");
 			}
 
 			for (int i = 0; i < numLayers; i++)
 				if (layers[i].getType() == MapLayerInfo.TYPE_VECTOR) {
 					styles.add(layers[i].getFeature().getDefaultStyle());
 				} else if (layers[i].getType() == MapLayerInfo.TYPE_RASTER) {
 					styles.add(layers[i].getCoverage().getDefaultStyle());
 				}
 		} else {
 			List styleNames = readFlat(rawStyles, INNER_DELIMETER);
 
 			if (numLayers != styleNames.size()) {
 				String msg = numLayers
 						+ " layers requested, but found "
 						+ styleNames.size()
 						+ " styles specified. "
 						+ "Since SLD parameter is not yet implemented, the STYLES parameter "
 						+ "is mandatory and MUST have exactly one value per requested layer";
 				throw new WmsException(msg, getClass().getName());
 			}
 
 			String currStyleName;
 			Style currStyle;
 			MapLayerInfo currLayer;
 
 			for (int i = 0; i < numLayers; i++) {
 				currStyleName = (String) styleNames.get(i);
 				currLayer = layers[i];
 
 				if (currLayer.getType() == MapLayerInfo.TYPE_VECTOR) {
 					if ((null == currStyleName) || "".equals(currStyleName)) {
 						currStyle = currLayer.getFeature().getDefaultStyle();
 					} else {
 						currStyle = findStyle(request, currStyleName);
 
 						if (currStyle == null) {
 							String msg = "No default style has been defined for "
 									+ currLayer.getName();
 							throw new WmsException(msg, "StyleNotDefined");
 						}
 					}
 
 					try {
 						checkStyle(currStyle, layers[i].getFeature()
 								.getFeatureType());
 					} catch (IOException e) {
 						throw new WmsException(
 								"Error obtaining FeatureType for layer "
 										+ layers[i].getName());
 					}
 
 					if (LOGGER.isLoggable(Level.FINE)) {
 						LOGGER.fine(new StringBuffer("establishing ").append(
 								currStyleName).append(" style for ").append(
 								layers[i].getName()).toString());
 					}
 
 					styles.add(currStyle);
 				} else if (currLayer.getType() == MapLayerInfo.TYPE_RASTER) {
 					if ((null == currStyleName) || "".equals(currStyleName)) {
 						currStyle = currLayer.getCoverage().getDefaultStyle();
 					} else {
 						currStyle = findStyle(request, currStyleName);
 
 						if (currStyle == null) {
 							String msg = "No default style has been defined for "
 									+ currLayer.getName();
 							throw new WmsException(msg,
 									"GetMapKvpReader::parseStyles()");
 						}
 					}
 
 					/**
 					 * @task TODO: Check for Style Coverage Compatibility ...
 					 */
 					styles.add(currStyle);
 				}
 			}
 		}
 
 		return styles;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param request
 	 * @param currStyleName
 	 * 
 	 * @return the configured style named <code>currStyleName</code> or
 	 *         <code>null</code> if such a style does not exists on this
 	 *         server.
 	 */
 	public static Style findStyle(GetMapRequest request, String currStyleName) {
 		Style currStyle;
 		Map configuredStyles = request.getWMS().getData().getStyles();
 
 		currStyle = (Style) configuredStyles.get(currStyleName);
 
 		return currStyle;
 	}
 
 	/**
 	 * Method to initialize a user layer which contains inline features.
 	 * 
 	 * @param request
 	 *            The request
 	 * @param mapLayer
 	 *            The map layer.
 	 * 
 	 * @throws Exception
 	 */
 
 	// JD: the reason this method is static is to share logic among the xml
 	// and kvp reader, ugh...
 	public static void initializeInlineFeatureLayer(
 			GetMapRequest getMapRequest, UserLayer ul, MapLayerInfo currLayer)
 			throws Exception {
 		// SPECIAL CASE - we make the temporary version
 		currLayer.setFeature(new TemporaryFeatureTypeInfo(ul
 				.getInlineFeatureDatastore()));
 
 		// what if they didn't put an "srsName" on their geometry in their
 		// inlinefeature?
 		// I guess we should assume they mean their geometry to exist in the
 		// output SRS of the
 		// request they're making.
 		if (ul.getInlineFeatureType().getCoordinateReferenceSystem() == null) {
 			LOGGER
 					.warning("No CRS set on inline features default geometry.  Assuming the requestor has their inlinefeatures in the boundingbox CRS.");
 
 			SimpleFeatureType currFt = ul.getInlineFeatureType();
 			Query q = new DefaultQuery(currFt.getTypeName(), Filter.INCLUDE);
 			FeatureReader<SimpleFeatureType, SimpleFeature> ilReader;
             ilReader = ul.getInlineFeatureDatastore().getFeatureReader(q, Transaction.AUTO_COMMIT);
 			CoordinateReferenceSystem crs = (getMapRequest.getCrs() == null) ? DefaultGeographicCRS.WGS84
 					: getMapRequest.getCrs();
 			MemoryDataStore reTypedDS = new MemoryDataStore(
 					new ForceCoordinateSystemFeatureReader(ilReader, crs));
 			currLayer.setFeature(new TemporaryFeatureTypeInfo(reTypedDS));
 		}
 	}
 
 	/**
 	 * Checks to make sure that the style passed in can process the FeatureType.
 	 * 
 	 * @param style
 	 *            The style to check
 	 * @param fType
 	 *            The source requested.
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 */
 	private void checkStyle(Style style, SimpleFeatureType fType) throws WmsException {
 		StyleAttributeExtractor sae = new StyleAttributeExtractor();
 		sae.visit(style);
 
 		String[] styleAttributes = sae.getAttributeNames();
 		String attName;
 		final int length = styleAttributes.length;
 
 		for (int i = 0; i < length; i++) {
 			attName = styleAttributes[i];
 
 			if (fType.getDescriptor(attName) == null) {
 				throw new WmsException(
 						"The requested Style can not be used with "
 								+ "this featureType.  The style specifies an attribute of "
 								+ attName
 								+ " and the featureType definition is: "
 								+ fType);
 			}
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param request
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 */
 	protected void parseLayersAndStyles(GetMapRequest request)
 			throws WmsException {
 		String sldParam = getValue("SLD");
 		String sldBodyParam = getValue("SLD_BODY");
 
 		if (sldBodyParam != null) {
 			if (LOGGER.isLoggable(Level.FINE)) {
 				LOGGER.fine("Getting layers and styles from SLD_BODY");
 			}
 
 			parseSldBodyParam(request);
 		} else if (sldParam != null) {
 			if (LOGGER.isLoggable(Level.FINE)) {
 				LOGGER.fine("Getting layers and styles from reomte SLD");
 			}
 
 			parseSldParam(request);
 		} else {
 			MapLayerInfo[] featureTypes = null;
 			List styles = null;
 			featureTypes = parseLayersParam(request);
 
 			request.setLayers(featureTypes);
 
 			if (isStylesRquired()) {
 				styles = parseStylesParam(request, featureTypes);
 
 				if (isStylesRquired()) {
 					request.setStyles(styles);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Takes the SLD_BODY parameter value and parses it to a geotools'
 	 * <code>StyledLayerDescriptor</code>, then takes the layers and styles
 	 * to use in the map composition from there.
 	 * 
 	 * @param request
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 */
 	protected void parseSldBodyParam(GetMapRequest request) throws WmsException {
 		final String sldBody = getValue("SLD_BODY");
 
 		if (LOGGER.isLoggable(Level.FINE)) {
 			LOGGER.fine(new StringBuffer("About to parse SLD body: ").append(
 					sldBody).toString());
 		}
 
 		if (getValue("VALIDATESCHEMA") != null) {
 			// Get a reader from the given string
 			Reader reader = getReaderFromString(sldBody);
 
 			// -InputStream in = new StringBufferInputStream(sldBody);
 			// user requested to validate the schema.
 			SLDValidator validator = new SLDValidator();
 			List errors = null;
 
 			// Create a sax input source from the reader
 			InputSource in = new InputSource(reader);
 			errors = validator.validateSLD(in, request.getHttpServletRequest()
 					.getSession().getServletContext());
 
 			if (errors.size() != 0) {
 				reader = getReaderFromString(sldBody);
 				throw new WmsException(SLDValidator.getErrorMessage(reader,
 						errors));
 			}
 
 			// - errors = validator.validateSLD(in,
 			// request.getHttpServletRequest().getSession().getServletContext());
 			// - try{
 			// - in.close();
 			// - }
 			// - catch(Exception e)
 			// - {
 			// - // do nothing
 			// - }
 			// - if (errors.size() != 0)
 			// - {
 			// - in = new StringBufferInputStream(sldBody);
 			// - throw new
 			// WmsException(SLDValidator.getErrorMessage(in,errors));
 			// - }
 		}
 
 		// - InputStream in = new StringBufferInputStream(sldBody);
 		// - SLDParser parser = new SLDParser(styleFactory, in);
 		Reader reader = getReaderFromString(sldBody);
 		SLDParser parser = new SLDParser(styleFactory, reader);
 		StyledLayerDescriptor sld = parser.parseSLD();
 		parseStyledLayerDescriptor(request, sld);
 	}
 
 	/**
 	 * Create a reader of the given String. This reader will be used in the
 	 * InputSource for the sld parser. The advantage with a reader over a input
 	 * stream is that we don't have to consider encoding. The xml declaration
 	 * with encoding is ignored using a Reader in parser. The encoding of the
 	 * string has been appropiate handled by the servlet when streaming in.
 	 * 
 	 * @param sldBody
 	 *            the sldbody to create a reader of.
 	 * @return The created reader
 	 * @see Reader
 	 */
 	private Reader getReaderFromString(String sldBody) {
 		return new StringReader(sldBody);
 	}
 
 	/**
 	 * Gets a sequence of url encoded filters and parses them into Filter
 	 * objects that will be set into the request object
 	 * 
 	 * @param request
 	 * @throws WmsException
 	 */
 	protected void parseFilterParam(GetMapRequest request) throws WmsException {
 		String rawFilter = getValue("FILTER");
 		String rawCqlFilter = getValue("CQL_FILTER");
 		String rawIdFilter = getValue("FEATUREID");
 
 		// in case of a mixed request, get with sld in post body, layers
 		// are not parsed, so we can't parse filters neither...
 		if (request.getLayers() == null) {
 			return;
 		}
 
 		int numLayers = request.getLayers().length;
 
 		if (numLayers == 0) {
 			throw new RuntimeException(
 					"parseFilterParam must be called after the layer list has been built!");
 		}
 
 		List filters = null;
 
 		// if no filter, no need to proceed
 		if ((rawFilter != null) && !rawFilter.equals("")) {
 			try {
 				filters = readOGCFilter(rawFilter);
 			} catch (ServiceException e) {
 				throw new WmsException(e);
 			}
 		}
 
 		if ((rawIdFilter != null) && !rawIdFilter.equals("")) {
 			if (filters != null) {
 				throw new WmsException("GetMap KVP request contained "
 						+ "conflicting filters.  Filter: " + rawFilter
 						+ ", fid: " + rawFilter);
 			}
 
 			filters = readFidFilter(rawIdFilter);
 		}
 
 		if ((rawCqlFilter != null) && !rawCqlFilter.equals("")) {
 			if (filters != null) {
 				throw new WmsException("GetMap KVP request contained "
 						+ "conflicting filters.  Filter: " + rawFilter
 						+ ", fid: " + rawFilter + ", cql: " + rawCqlFilter);
 			}
 
 			try {
 				filters = readCQLFilter(rawCqlFilter);
 			} catch (ServiceException e) {
 				throw new WmsException(e);
 			}
 		}
 
 		if (filters == null) {
 			return;
 		}
 
 		if (numLayers != filters.size()) {
 			// as in wfs getFeatures, perform lenient parsing, if just one
 			// filter, it gets
 			// applied to all layers
 			if (filters.size() == 1) {
 				Filter f = (Filter) filters.get(0);
 				filters = new ArrayList(numLayers);
 
 				for (int i = 0; i < numLayers; i++) {
 					filters.add(f);
 				}
 			} else {
 				String msg = numLayers
 						+ " layers requested, but found "
 						+ filters.size()
 						+ " filters specified. "
 						+ "When you specify the FILTER parameter, you must provide just one, \n"
 						+ " that will be applied to all layers, or exactly one for each requested layer";
 				throw new WmsException(msg, getClass().getName());
 			}
 		}
 
 		request.setFilters(filters);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param request
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 */
 	protected void parseSldParam(GetMapRequest request) throws WmsException {
 		String urlValue = getValue("SLD");
 
 		if (LOGGER.isLoggable(Level.FINE)) {
 			LOGGER
 					.fine(new StringBuffer(
 							"about to load remote SLD document: '").append(
 							urlValue).append("'").toString());
 		}
 
 		URL sldUrl;
 
 		try {
 			sldUrl = new URL(fixURL(urlValue));
 		} catch (MalformedURLException e) {
 			String msg = new StringBuffer("Creating remote SLD url: ").append(
 					e.getMessage()).toString();
 
 			if (LOGGER.isLoggable(Level.WARNING)) {
 				LOGGER.log(Level.WARNING, msg, e);
 			}
 
 			throw new WmsException(e, msg, "parseSldParam");
 		}
 
 		if (getValue("VALIDATESCHEMA") != null) {
 			// user requested to validate the schema.
 			SLDValidator validator = new SLDValidator();
 			List errors = null;
 
 			try {
 				// JD: GEOS-420, Wrap the sldUrl in getINputStream method in
 				// order
 				// to do compression
 				InputStream in = getInputStream(sldUrl);
 				errors = validator.validateSLD(in, request
 						.getHttpServletRequest().getSession()
 						.getServletContext());
 				in.close();
 
 				if (errors.size() != 0) {
 					throw new WmsException(SLDValidator.getErrorMessage(sldUrl
 							.openStream(), errors));
 				}
 			} catch (IOException e) {
 				String msg = new StringBuffer("Creating remote SLD url: ")
 						.append(e.getMessage()).toString();
 
 				if (LOGGER.isLoggable(Level.WARNING)) {
 					LOGGER.log(Level.WARNING, msg, e);
 				}
 
 				throw new WmsException(e, msg, "parseSldParam");
 			}
 		}
 
 		SLDParser parser;
 
 		try {
 			// JD: GEOS-420, Wrap the sldUrl in getINputStream method in order
 			// to do compression
 			parser = new SLDParser(styleFactory, getInputStream(sldUrl));
 		} catch (IOException e) {
 			String msg = new StringBuffer("Creating remote SLD url: ").append(
 					e.getMessage()).toString();
 
 			if (LOGGER.isLoggable(Level.WARNING)) {
 				LOGGER.log(Level.WARNING, msg, e);
 			}
 
 			throw new WmsException(e, msg, "parseSldParam");
 		}
 
 		StyledLayerDescriptor sld = parser.parseSLD();
 		parseStyledLayerDescriptor(request, sld);
 	}
 
 	/**
 	 * URLEncoder.encode does not respect the RFC 2396, so we rolled our own
 	 * little encoder. It's not complete, but should work in most cases
 	 * 
 	 * @param url
 	 * @return
 	 */
 	static String fixURL(String url) {
 		StringBuffer sb = new StringBuffer();
 
 		for (int i = 0; i < url.length(); i++) {
 			char c = url.charAt(i);
 
 			// From RFC, "Only alphanumerics [0-9a-zA-Z], the special
 			// characters "$-_.+!*'(),", and reserved characters used
 			// for their reserved purposes may be used unencoded within a URL
 			// Here we keep all the good ones, and remove the few uneeded in
 			// their
 			// ascii range. We also keep / and : to make sure basic URL elements
 			// don't get encoded
 			if ((c > ' ') && (c < '{') && ("\"\\<>%^[]`+$,".indexOf(c) == -1)) {
 				sb.append(c);
 			} else {
 				sb.append("%").append(Integer.toHexString(c));
 			}
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * Looks in <code>sld</code> for the layers and styles to use in the map
 	 * composition and sets them to the <code>request</code>
 	 * 
 	 * <p>
 	 * If <code>sld</code> is used in "library" mode, that is, the LAYERS
 	 * param is also present, saying what layers must be taken in count, then
 	 * only the layers from the LAYERS parameter are used and <code>sld</code>
 	 * is used as a style library, which means that for each layer requested
 	 * through LAYERS=..., if a style if found in it for that layer it is used,
 	 * and if not, the layers default is used.
 	 * </p>
 	 * 
 	 * <p>
 	 * By the other hand, if the LAYERS parameter is not present all the layers
 	 * found in <code>sld</code> are setted to <code>request</code>.
 	 * </p>
 	 * 
 	 * @param request
 	 *            the GetMap request to which to set the layers and styles
 	 * @param sld
 	 *            a SLD document to take layers and styles from, following the
 	 *            "literal" or "library" rule.
 	 * 
 	 * @throws WmsException
 	 *             if anything goes wrong
 	 * @throws RuntimeException
 	 *             DOCUMENT ME!
 	 */
 	private void parseStyledLayerDescriptor(final GetMapRequest request,
 			final StyledLayerDescriptor sld) throws WmsException {
 		MapLayerInfo[] libraryModeLayers = null;
 
 		if (null != getValue("LAYERS")) {
 			if (LOGGER.isLoggable(Level.INFO)) {
 				LOGGER.info("request comes in \"library\" mode");
 			}
 
 			libraryModeLayers = parseLayersParam(request);
 		}
 
 		final StyledLayer[] styledLayers = sld.getStyledLayers();
 		final int slCount = styledLayers.length;
 
 		if (slCount == 0) {
 			throw new WmsException("SLD document contains no layers");
 		}
 
 		final List layers = new ArrayList();
 		final List styles = new ArrayList();
 
 		MapLayerInfo currLayer = null;
 		Style currStyle = null;
 
 		if (null != libraryModeLayers) {
 			int lCount = libraryModeLayers.length;
 
 			for (int i = 0; i < lCount; i++) {
 				currLayer = libraryModeLayers[i];
 
 				if (currLayer.getType() == MapLayerInfo.TYPE_VECTOR) {
 					currStyle = findStyleOf(request, currLayer.getFeature(),
 							styledLayers);
 				} else if (currLayer.getType() == MapLayerInfo.TYPE_RASTER) {
 					try {
 						currStyle = findStyleOf(request,
 								currLayer.getFeature(), styledLayers);
 					} catch (WmsException wm) {
 						currStyle = findStyleOf(request,
 								currLayer.getFeature(), styledLayers);
 						if (currStyle == null) {
 							// nope, there's no default raster style. Give up.
 							throw new WmsException(
 									wm.getMessage()
 											+ "  Also tried to use "
 											+ "the generic raster style 'raster', but it wasn't available.");
 						}
 					}
 				}
 
 				layers.add(currLayer);
 				styles.add(currStyle);
 			}
 		} else {
 			StyledLayer sl = null;
 			String layerName;
 			UserLayer ul;
 
 			for (int i = 0; i < slCount; i++) {
 				sl = styledLayers[i];
 				layerName = sl.getName();
 
 				if (null == layerName) {
 					throw new WmsException(
 							"A UserLayer without layer name was passed");
 				}
 
 				currLayer = new MapLayerInfo();
 
 				// handle the InLineFeature stuff
 				// TODO: add support for remote WFS here
 				if ((sl instanceof UserLayer)
 						&& ((((UserLayer) sl)).getInlineFeatureDatastore() != null)) {
 					// SPECIAL CASE - we make the temporary version
 					ul = ((UserLayer) sl);
 
 					try {
 						initializeInlineFeatureLayer(request, ul, currLayer);
 					} catch (Exception e) {
 						throw new WmsException(e);
 					}
 				} else {
 					try {
 						currLayer.setFeature(GetMapKvpReader.findFeatureLayer(
 								request, layerName));
 					} catch (WmsException e) {
 						currLayer.setCoverage(GetMapKvpReader
 								.findCoverageLayer(request, layerName));
 					}
 				}
 
 				if (currLayer.getType() == MapLayerInfo.TYPE_VECTOR) {
 					// currStyle = findStyleOf(request, currLayer,
 					// styledLayers); // DJB: this looks like a bug, we should
 					// get the style from styledLayers[i]
 
 					// the correct thing to do its grab the style from
 					// styledLayers[i]
 					// inside the styledLayers[i] will either be :
 					// a) nothing - in which case grab the layer's default style
 					// b) a set of:
 					// i) NameStyle -- grab it from the pre-loaded styles
 					// ii)UserStyle -- grab it from the sld the user uploaded
 					//
 					// NOTE: we're going to get a set of layer->style pairs for
 					// (b).
 					addStyles(request, currLayer, styledLayers[i], layers,
 							styles);
 				} else if (currLayer.getType() == MapLayerInfo.TYPE_RASTER) {
 					try {
 						addStyles(request, currLayer, styledLayers[i], layers,
 								styles);
 					} catch (WmsException wm) {
 						// hmm, well, the style they specified in the wms
 						// request
 						// wasn't found. Let's try the default raster style
 						// named 'raster'
 						currStyle = findStyle(request, "raster");
 						if (currStyle == null) {
 							// nope, there's no default raster style. Give up.
 							throw new WmsException(
 									wm.getMessage()
 											+ "  Also tried to use "
 											+ "the generic raster style 'raster', but it wasn't available.");
 						}
 						layers.add(currLayer);
 						styles.add(currStyle);
 					}
 				}
 			}
 		}
 
 		request.setLayers((MapLayerInfo[]) layers
 				.toArray(new MapLayerInfo[layers.size()]));
 		request.setStyles(styles);
 	}
 
 	/**
 	 * the correct thing to do its grab the style from styledLayers[i] inside
 	 * the styledLayers[i] will either be : a) nothing - in which case grab the
 	 * layer's default style b) a set of: i) NameStyle -- grab it from the
 	 * pre-loaded styles ii)UserStyle -- grab it from the sld the user uploaded
 	 * 
 	 * NOTE: we're going to get a set of layer->style pairs for (b). these are
 	 * added to layers,styles
 	 * 
 	 * NOTE: we also handle some featuretypeconstraints
 	 * 
 	 * @param request
 	 * @param currLayer
 	 * @param layer
 	 * @param layers
 	 * @param styles
 	 */
 	public static void addStyles(GetMapRequest request, MapLayerInfo currLayer,
 			StyledLayer layer, List layers, List styles) throws WmsException {
 		if (currLayer == null) {
 			return; // protection
 		}
 
 		Style[] layerStyles = null;
 		FeatureTypeConstraint[] ftcs = null;
 
 		if (layer instanceof NamedLayer) {
 			ftcs = ((NamedLayer) layer).getLayerFeatureConstraints();
 			layerStyles = ((NamedLayer) layer).getStyles();
 		} else if (layer instanceof UserLayer) {
 			ftcs = ((UserLayer) layer).getLayerFeatureConstraints();
 			layerStyles = ((UserLayer) layer).getUserStyles();
 		}
 
 		// DJB: TODO: this needs to do the whole thing, not just names
 		if (ftcs != null) {
 			FeatureTypeConstraint ftc;
 			final int length = ftcs.length;
 
 			for (int t = 0; t < length; t++) {
 				ftc = ftcs[t];
 
 				if (ftc.getFeatureTypeName() != null) {
 					String ftc_name = ftc.getFeatureTypeName();
 
 					// taken from lite renderer
 					boolean matches;
 
 					try {
 						final SimpleFeatureType currSchema = currLayer.getFeature().getFeatureType();
                         matches = currSchema.getTypeName().equalsIgnoreCase(ftc_name) ||
 								  FeatureTypes.isDecendedFrom(currSchema, null, ftc_name);
 					} catch (Exception e) {
 						matches = false; // bad news
 					}
 
 					if (!matches) {
 						continue; // this layer is fitered out
 					}
 				}
 			}
 		}
 
 		// handle no styles -- use default
 		if ((layerStyles == null) || (layerStyles.length == 0)) {
 			layers.add(currLayer);
 			styles.add(currLayer.getDefaultStyle());
 
 			return;
 		}
 
 		final int length = layerStyles.length;
 		Style s;
 
 		for (int t = 0; t < length; t++) {
 			if (layerStyles[t] instanceof NamedStyle) {
 				layers.add(currLayer);
 				s = findStyle(request, ((NamedStyle) layerStyles[t]).getName());
 
 				if (s == null) {
 					throw new WmsException("couldnt find style named '"
 							+ ((NamedStyle) layerStyles[t]).getName() + "'");
 				}
 
 				styles.add(s);
 			} else {
 				layers.add(currLayer);
 				styles.add(layerStyles[t]);
 			}
 		}
 	}
 
 	/**
 	 * Finds the style for <code>layer</code> in <code>styledLayers</code>
 	 * or the layer's default style if <code>styledLayers</code> has no a
 	 * UserLayer or a NamedLayer with the same name than <code>layer</code>
 	 * <p>
 	 * This method is used to parse the style of a layer for SLD and SLD_BODY
 	 * parameters, both in library and literal mode. Thus, once the declared
 	 * style for the given layer is found, it is checked for validity of
 	 * appliance for that layer (i.e., whether the featuretype contains the
 	 * attributes needed for executing the style filters).
 	 * </p>
 	 * 
 	 * @param request
 	 *            used to find out an internally configured style when
 	 *            referenced by name by a NamedLayer
 	 * 
 	 * @param layer
 	 *            one of the internal FeatureType that was requested through the
 	 *            LAYERS parameter or through and SLD document when the request
 	 *            is in literal mode.
 	 * @param styledLayers
 	 *            a set of StyledLayers from where to find the SLD layer with
 	 *            the same name as <code>layer</code> and extract the style to
 	 *            apply.
 	 * 
 	 * @return the Style applicable to <code>layer</code> extracted from
 	 *         <code>styledLayers</code>.
 	 * 
 	 * @throws RuntimeException
 	 *             if one of the StyledLayers is neither a UserLayer nor a
 	 *             NamedLayer. This shuoldn't happen, since the only allowed
 	 *             subinterfaces of StyledLayer are NamedLayer and UserLayer.
 	 * @throws WmsException
 	 */
 	private Style findStyleOf(GetMapRequest request, FeatureTypeInfo layer,
 			StyledLayer[] styledLayers) throws WmsException {
 		Style style = null;
 		String layerName = layer.getName();
 		StyledLayer sl;
 
 		for (int i = 0; i < styledLayers.length; i++) {
 			sl = styledLayers[i];
 
 			if (layerName.equals(sl.getName())) {
 				if (sl instanceof UserLayer) {
 					Style[] styles = ((UserLayer) sl).getUserStyles();
 
 					if ((null != styles) && (0 < styles.length)) {
 						style = styles[0];
 					}
 				} else if (sl instanceof NamedLayer) {
 					Style[] styles = ((NamedLayer) sl).getStyles();
 
 					if ((null != styles) && (0 < styles.length)) {
 						style = styles[0];
 					}
 
 					if (style instanceof NamedStyle) {
 						style = findStyle(request, style.getName());
 					}
 				} else {
 					throw new RuntimeException("Unknown layer type: " + sl);
 				}
 
 				break;
 			}
 		}
 
 		if (null == style) {
 			style = layer.getDefaultStyle();
 		}
 
 		SimpleFeatureType type;
 
 		try {
 			type = layer.getFeatureType();
 		} catch (IOException ioe) {
 			throw new RuntimeException(
 					"Error getting FeatureType, this should never happen!");
 		}
 
 		checkStyle(style, type);
 
 		return style;
 	}
 
 	/**
 	 * Parses a list of layers in a layer grouping.
 	 * 
 	 * @param layerGroup The layer group.
 	 * 
 	 * @return List of String.
 	 */
 	public static List parseLayerGroup( String layerGroup ) {
 	    return readFlat( layerGroup, INNER_DELIMETER );
 	}
 	/**
 	 * Parses the requested layers given by the LAYERS request parameter and
 	 * looks up their corresponding FeatureTypeInfo objects in the server.
 	 * 
 	 * @param request
 	 * 
 	 * @return
 	 * 
 	 * @throws WmsException
 	 */
 	protected MapLayerInfo[] parseLayersParam(GetMapRequest request)
 			throws WmsException {
 		MapLayerInfo[] layers;
 		String layersParam = getValue("LAYERS");
 		List layerNames = readFlat(layersParam, INNER_DELIMETER);
 		List realLayerNames = new ArrayList();
 
 		// expand base layers, if there is any
 		WMS wms = request.getWMS();
 		if (wms.getBaseMapLayers() != null) {
 			for (int i = 0; i < layerNames.size(); i++) {
 				String layerGroup = (String) wms.getBaseMapLayers().get(
 						layerNames.get(i));
 				if (layerGroup != null) {
 					List layerGroupExpanded = parseLayerGroup(layerGroup);
 					layerNames.remove(i);
 					layerNames.addAll(i, layerGroupExpanded);
 				}
 			}
 		}
 
 		String layerName = null;
 		Data catalog = request.getWMS().getData();
 
 		String rawStyles = getValue("STYLES");
 		List styleNames = readFlat(rawStyles, INNER_DELIMETER);
 
 		int l_counter = 0;
 		int s_counter = styleNames.size();
 
 		// //
 		// Expand the eventually WMS grouped layers into the same WMS Path
 		// element
 		// //
 		for (Iterator it = layerNames.iterator(); it.hasNext();) {
 			layerName = (String) it.next();
 
 			Integer layerType = catalog.getLayerType(layerName);
 
 			if (layerType == null) {
 				// //
 				// Search for grouped layers (attention: heavy process)
 				// //
 				String catalogLayerName = null;
 
 				for (Iterator c_keys = catalog.getLayerNames().iterator(); c_keys
 						.hasNext();) {
 					catalogLayerName = (String) c_keys.next();
 
 					try {
 						FeatureTypeInfo ftype = findFeatureLayer(request,
 								catalogLayerName);
 						String wmsPath = ftype.getWmsPath();
 
 						if ((wmsPath != null)
 								&& wmsPath.matches(".*/" + layerName)) {
 							realLayerNames.add(catalogLayerName);
 							l_counter++;
 
 							if (l_counter > s_counter) {
 								rawStyles = ((rawStyles.length() > 0) ? (rawStyles + ",")
 										: rawStyles)
 										+ ftype.getDefaultStyle().getName();
 							}
 						}
 					} catch (WmsException e_1) {
 						try {
 							CoverageInfo cv = findCoverageLayer(request,
 									catalogLayerName);
 							String wmsPath = cv.getWmsPath();
 
 							if ((wmsPath != null)
 									&& wmsPath.matches(".*/" + layerName)) {
 								realLayerNames.add(catalogLayerName);
 								l_counter++;
 
 								if (l_counter > s_counter) {
 									rawStyles = ((rawStyles.length() > 0) ? (rawStyles + ",")
 											: rawStyles)
 											+ cv.getDefaultStyle().getName();
 								}
 							}
 						} catch (WmsException e_2) {
 						}
 					}
 				}
 			} else {
 				realLayerNames.add(layerName);
 				l_counter++;
 			}
 		}
 
 		// JD: only set if non-null since if the layer is an actual layer
 		// (ie. not something matching wms path) with no style specified we
 		// dont want to create an empty "STYLES" entry
 		if ((rawStyles != null) && !"".equals(rawStyles.trim())) {
 			kvpPairs.put("STYLES", rawStyles);
 		}
 
 		int layerCount = realLayerNames.size();
 
 		if (layerCount == 0) {
 			throw new WmsException("No LAYERS has been requested", getClass()
 					.getName());
 		}
 
 		layers = new MapLayerInfo[layerCount];
 
 		for (int i = 0; i < layerCount; i++) {
 			layerName = (String) layerNames.get(i);
 			layers[i] = new MapLayerInfo();
 
 			try {
 				FeatureTypeInfo ftype = findFeatureLayer(request, layerName);
 
 				layers[i].setFeature(ftype);
 			} catch (WmsException e) {
 				CoverageInfo cv = findCoverageLayer(request, layerName);
 
 				layers[i].setCoverage(cv);
 			}
 		}
 
 		return layers;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param request
 	 * @param layerName
 	 * 
 	 * @return
 	 * 
 	 * @throws WmsException
 	 *             DOCUMENT ME!
 	 */
 	public static FeatureTypeInfo findFeatureLayer(GetMapRequest request,
 			String layerName) throws WmsException {
 		Data catalog = request.getWMS().getData();
 		FeatureTypeInfo ftype = null;
 		Integer layerType = catalog.getLayerType(layerName);
 
 		if (Data.TYPE_VECTOR != layerType) {
 			throw new WmsException(new StringBuffer(layerName).append(
 					": no such layer on this server").toString(),
 					"LayerNotDefined");
 		} else {
 			ftype = catalog.getFeatureTypeInfo(layerName);
 		}
 
 		return ftype;
 	}
 
 	public static CoverageInfo findCoverageLayer(GetMapRequest request,
 			String layerName) throws WmsException {
 		Data catalog = request.getWMS().getData();
 		CoverageInfo cv = null;
 		Integer layerType = catalog.getLayerType(layerName);
 
 		if (Data.TYPE_RASTER != layerType) {
 			throw new WmsException(new StringBuffer(layerName).append(
 					": no such layer on this server").toString(),
 					"LayerNotDefined");
 		} else {
 			cv = catalog.getCoverageInfo(layerName);
 		}
 
 		return cv;
 	}
 
 	/**
 	 * This method gets the correct input stream for a URL. If the URL is a
 	 * http/https connection, the Accept-Encoding: gzip, deflate is added. It
 	 * the paramter is added, the response is checked to see if the response is
 	 * encoded in gzip, deflate or plain bytes. The correct input stream wrapper
 	 * is then selected and returned.
 	 * 
 	 * This method was added as part of GEOS-420
 	 * 
 	 * @param sldUrl
 	 *            The url to the sld file
 	 * @return The InputStream used to validate and parse the SLD xml.
 	 * @throws IOException
 	 */
 	private InputStream getInputStream(URL sldUrl) throws IOException {
 		// Open the connection
 		URLConnection conn = sldUrl.openConnection();
 
 		// If it is the http or https scheme, then ask for gzip if the server
 		// supports it.
 		if (conn instanceof HttpURLConnection) {
 			// Send the requested encoding to the remote server.
 			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
 		}
 
 		// Conect to get the response headers
 		conn.connect();
 
 		// Return the correct inputstream
 		// If the connection is a url, connection, check the response encoding.
 		if (conn instanceof HttpURLConnection) {
 			// Get the content encoding of the server response
 			String encoding = conn.getContentEncoding();
 
 			// If null, set it to a emtpy string
 			if (encoding == null) {
 				encoding = "";
 			}
 
 			if (encoding.equalsIgnoreCase("gzip")) {
 				// For gzip input stream, use a GZIPInputStream
 				return new GZIPInputStream(conn.getInputStream());
 			} else if (encoding.equalsIgnoreCase("deflate")) {
 				// If it is encoded as deflate, then select the inflater
 				// inputstream.
 				return new InflaterInputStream(conn.getInputStream(),
 						new Inflater(true));
 			} else {
 				// Else read the raw bytes
 				return conn.getInputStream();
 			}
 		} else {
 			// Else read the raw bytes.
 			return conn.getInputStream();
 		}
 	}
 
 	/**
 	 * Filters the layers and styles if the user specified "layers=basemap".
 	 * 
 	 * 
 	 * @param layers
 	 * @param styles
 	 */
 	public void filterBaseMap(Map layers, Map styles) {
 		List currentLayers = null;
 
 		try {
 			// read flat can return non modifiable lists
 			currentLayers = new ArrayList(readFlat(getValue("LAYERS"), ","));
 		} catch (NullPointerException e) {
 			// No layers defined. This is either wrong or they are listing the
 			// layers
 			// in an SLD document specified with the SLD= parameter
 			LOGGER
 					.fine("No layers defined. This is either wrong or they are listing the layers"
 							+ " in an SLD document specified with the SLD= parameter");
 
 			return; // just continue ignoring the basemap option
 		}
 
 		List currentStyles = null;
 
 		try {
 			currentStyles = new ArrayList(readFlat(getValue("STYLES"), ","));
 		} catch (NullPointerException e) {
 			currentStyles = new ArrayList();
 		}
 
 		while (currentStyles.size() < currentLayers.size())
 			currentStyles.add("");
 
 		String[] baseLayers = (String[]) layers.keySet().toArray(new String[0]);
 		boolean replacedOne = false;
 
 		for (int i = 0; i < baseLayers.length; i++) {
 			String blTitle = baseLayers[i];
 			int index = currentLayers.indexOf(blTitle);
 
 			if (index > -1) {
 				replacedOne = true;
 				LOGGER.info("Using BASEMAP layer: " + baseLayers[i]);
 
 				// remove the 'basemap layer' from the currentLayers list
 				currentLayers.remove(index);
 
 				List blLayers = new ArrayList(readFlat((String) layers
 						.get(blTitle), ","));
 				currentLayers.addAll(index, blLayers);
 				List blStyles = new ArrayList(readFlat((String) styles
 						.get(blTitle), ","));
 
 				while (blStyles.size() < blLayers.size())
 					blStyles.add("");
 
 				currentStyles.remove(index);
 				currentStyles.addAll(index, blStyles);
 			}
 		}
 
 		if (replacedOne) {
 			kvpPairs.remove("LAYERS");
 			kvpPairs.put("LAYERS", toStringList(currentLayers, ","));
 			kvpPairs.remove("STYLES");
 			kvpPairs.put("STYLES", toStringList(currentStyles, ","));
 		}
 	}
 
 	private String toStringList(List currentLayers, String separator) {
 		StringBuffer sb = new StringBuffer();
 
 		for (Iterator it = currentLayers.iterator(); it.hasNext();) {
 			String item = (String) it.next();
 			sb.append(item);
 
 			if (it.hasNext()) {
 				sb.append(separator);
 			}
 		}
 
 		return sb.toString();
 	}

    public boolean isFormatRequired() {
        return formatRequired;
    }

    public void setFormatRequired(boolean formatRequired) {
        this.formatRequired = formatRequired;
    }
 }
