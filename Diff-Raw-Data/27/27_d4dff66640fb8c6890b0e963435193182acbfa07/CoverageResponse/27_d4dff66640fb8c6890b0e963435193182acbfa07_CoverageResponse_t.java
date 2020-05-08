 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wcs.responses;
 
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.media.jai.BorderExtender;
 import javax.media.jai.Interpolation;
 import javax.media.jai.InterpolationNearest;
 
 import org.geotools.coverage.grid.GeneralGridRange;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.GridGeometry2D;
 import org.geotools.data.coverage.grid.AbstractGridFormat;
 import org.geotools.factory.Hints;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.parameter.DefaultParameterDescriptor;
 import org.geotools.referencing.CRS;
 import org.geotools.resources.CRSUtilities;
 import org.opengis.coverage.Coverage;
 import org.opengis.coverage.grid.Format;
 import org.opengis.coverage.grid.GridCoverage;
 import org.opengis.coverage.grid.GridCoverageReader;
 import org.opengis.coverage.grid.GridRange;
 import org.opengis.parameter.GeneralParameterValue;
 import org.opengis.parameter.ParameterDescriptor;
 import org.opengis.parameter.ParameterValue;
 import org.opengis.parameter.ParameterValueGroup;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.cs.AxisDirection;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.CoverageInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.util.CoverageUtils;
 import org.vfny.geoserver.util.WCSUtils;
 import org.vfny.geoserver.wcs.WcsException;
 import org.vfny.geoserver.wcs.requests.CoverageRequest;
 
 /**
  * DOCUMENT ME!
  * 
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public class CoverageResponse implements Response {
 	/** Standard logging instance for class */
 	private static final Logger LOGGER = Logger
 			.getLogger("org.vfny.geoserver.responses");
 
 	private final static Hints LENIENT_HINT = new Hints(
 			Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
 	
 	private final static Hints hints = new Hints(new HashMap(5));
 
 	/**
 	 * Tolerance for NOT drawing a coverage.
 	 * 
 	 * If after a scaling a coverage has all dimensions smaller than
 	 * {@link GridCoverageRenderer#MIN_DIM_TOLERANCE} we just do not draw it.
 	 */
	private static final int MIN_DIM_TOLERANCE = 1;
 	
 	/**
 	 * 
 	 */
 	CoverageResponseDelegate delegate;
 
 	/**
 	 * This is the request provided to the execute( Request ) method.
 	 * 
 	 * <p>
 	 * We save it so we can access the handle provided by the user for error
 	 * reporting during the writeTo( OutputStream ) opperation.
 	 * </p>
 	 * 
 	 * <p>
 	 * This value will be <code>null</code> until execute is called.
 	 * </p>
 	 * 
 	 * @uml.property name="request"
 	 * @uml.associationEnd multiplicity="(0 1)"
 	 */
 	private CoverageRequest request;
 
 	/**
 	 * Empty constructor
 	 */
 	public CoverageResponse() {
 		request = null;
 	}
 
     /**
      * Returns any extra headers that this service might want to set in the HTTP response object.
      * @see org.vfny.geoserver.Response#getResponseHeaders()
      */
     public HashMap getResponseHeaders() {
         return null;
     }
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param gs
 	 *            DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public String getContentType(GeoServer gs) {
 		return delegate.getContentType(gs);
 	}
 
 	public String getContentEncoding() {
 		return delegate.getContentEncoding();
 	}
 
 	public String getContentDisposition() {
 		return delegate.getContentDisposition();
 	}
 
 	/**
 	 * Jody here with one pass replacement for writeTo.
 	 * 
 	 * <p>
 	 * This code is a discussion point, when everyone has had there input we
 	 * will try and set things up properly.
 	 * </p>
 	 * 
 	 * <p>
 	 * I am providing a mirror of the existing desing: - execute gathers the
 	 * resultList - sets up the header
 	 * </p>
 	 * 
 	 * @param out
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws WcsException
 	 *             DOCUMENT ME!
 	 * @throws IOException
 	 *             DOCUMENT ME!
 	 * @throws IllegalStateException
 	 *             DOCUMENT ME!
 	 */
 	public void writeTo(OutputStream out) throws ServiceException, IOException {
 		if ((request == null) || (delegate == null)) {
 			throw new IllegalStateException(
 					"execute has not been called prior to writeTo");
 		}
 
 		delegate.encode(out);
 	}
 
 	/**
 	 * Executes CoverageRequest.
 	 * 
 	 * <p>
 	 * Willing to execute a CoverageRequest.
 	 * </p>
 	 * 
 	 * @param req
 	 *            DOCUMENT ME!
 	 * 
 	 * @throws WcsException
 	 *             DOCUMENT ME!
 	 */
 	public void execute(Request req) throws WcsException {
 		execute((CoverageRequest) req);
 	}
 
 	public void execute(CoverageRequest request) throws WcsException {
 		if (LOGGER.isLoggable(Level.FINEST))
 			LOGGER.finest(new StringBuffer(
 					"execute CoverageRequest response. Called request is: ")
 					.append(request).toString());
 		this.request = request;
 		final String outputFormat = request.getOutputFormat();
 
 		try {
 			delegate = CoverageResponseDelegateFactory.encoderFor(outputFormat);
 		} catch (NoSuchElementException ex) {
 			WcsException newEx = new WcsException(new StringBuffer("output format: ").append(
 					outputFormat).append(" not ").append(
 					"supported by geoserver for this Coverage").toString(), ex);
 			throw newEx;
 		}
 
 		final Data catalog = request.getWCS().getData();
 		CoverageInfo meta = null;
 		GridCoverage coverage = null;
 
 		try {
 			meta = catalog.getCoverageInfo(request.getCoverage());
 			
 			if (!meta.getSupportedFormats().contains(outputFormat.toUpperCase())) {
 				WcsException newEx = new WcsException(new StringBuffer("output format: ").append(
 						outputFormat).append(" not ").append(
 						"supported by geoserver for this Coverage").toString());
 				throw newEx;
 			}
 
 			final Format format = meta.getFormatInfo().getFormat();
 			final GridCoverageReader reader = meta.getReader();
 
 			// /////////////////////////////////////////////////////////
 			//
 			// Setting coverage reading params.
 			//
 			// /////////////////////////////////////////////////////////
 			List parameters = new ArrayList();
 			final ParameterValueGroup params = reader.getFormat()
 					.getReadParameters();
 			final String readGeometryKey = AbstractGridFormat.READ_GRIDGEOMETRY2D
 					.getName().toString();
 			if (params != null) {
 				List list = params.values();
 				Iterator it = list.iterator();
 				ParameterValue param;
 				ParameterDescriptor descr;
 				String key;
 				Object value;
 				while (it.hasNext()) {
 					param = ((ParameterValue) it.next());
 					descr = (ParameterDescriptor) param.getDescriptor();
 
 					key = descr.getName().toString();
 
 					// /////////////////////////////////////////////////////////
 					//
 					// request param for better management of coverage
 					//
 					// /////////////////////////////////////////////////////////
 					if (key.equalsIgnoreCase(readGeometryKey)
 							&& request.getEnvelope() != null) {
 						/* params.parameter(key).setValue(envelope); */
 						continue;
 					} else {
 						// /////////////////////////////////////////////////////////
 						//
 						// format specific params
 						//
 						// /////////////////////////////////////////////////////////
 						value = CoverageUtils.getCvParamValue(key, param, meta.getParameters());
 
 						if (value != null)
 							/* params.parameter(key).setValue(value); */
 							parameters.add(new DefaultParameterDescriptor(key,
 									value.getClass(), null, value).createValue());
 					}
 				}
 			}
 			
 			// /////////////////////////////////////////////////////////
 			//
 			// Reading the coverage
 			//
 			// /////////////////////////////////////////////////////////
 			coverage = reader.read(!parameters.isEmpty() ? (GeneralParameterValue[]) parameters.toArray(new GeneralParameterValue[parameters.size()]) : null);
 			
 			if (coverage == null || !(coverage instanceof GridCoverage2D))
 				throw new IOException(
 						"The requested coverage could not be found.");
 
 			final GridCoverage2D finalCoverage = getFinalCoverage(request, meta, coverage);
 			delegate.prepare(outputFormat, finalCoverage);
 		} catch (IOException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			
 			throw newEx;
 		} catch (NoSuchElementException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		} catch (IllegalArgumentException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		} catch (SecurityException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		} catch (WcsException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		} catch (FactoryException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		} catch (IndexOutOfBoundsException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		} catch (TransformException e) {
 			final WcsException newEx = new WcsException(e, "problem with CoverageResults", request
 					.getHandle());
 			throw newEx;
 		}
 	}
 
 	/**
 	 * Release locks if we are into that sort of thing.
 	 * 
 	 * @see org.vfny.geoserver.responses.Response#abort()
 	 */
 	public void abort(Service gs) {
 		if (request == null) {
 			return; // request was not attempted
 		}
 
 		Data catalog = gs.getData();
 	}
 	
 	/**
 	 * GetCroppedCoverage
 	 * 
 	 * @param request CoverageRequest
 	 * @param meta CoverageInfo
 	 * @param coverage GridCoverage
 	 * @return GridCoverage2D
 	 * @throws WcsException
 	 * @throws IOException
 	 * @throws IndexOutOfBoundsException
 	 * @throws FactoryException
 	 * @throws TransformException
 	 */
 	private static GridCoverage2D getFinalCoverage(CoverageRequest request,
 			CoverageInfo meta, GridCoverage coverage) throws WcsException,
 			IOException, IndexOutOfBoundsException, FactoryException,
 			TransformException {
 
 		// ///////////////////////////////////////////////////////////////////
 		//
 		// HINTS
 		//
 		// ///////////////////////////////////////////////////////////////////
 		hints.add(LENIENT_HINT);
 		/*if (java2dHints != null)
 		 this.hints.add(java2dHints);*/
 
 		// This is the final Response CRS
 		final String responseCRS = request.getResponseCRS();
 		// - first check if the responseCRS is present on the Coverage
 		// ResponseCRSs list
 		if (!meta.getResponseCRSs().contains(responseCRS)) {
 			throw new WcsException(
 			"This Coverage does not support the Response CRS requested.");
 		}
 		// - then create the Coordinate Reference System
 		final CoordinateReferenceSystem targetCRS = CRS.decode(responseCRS,
 				true);
 		// This is the CRS of the requested Envelope
 		final String requestCRS = request.getCRS();
 		// - first check if the requestCRS is present on the Coverage
 		// RequestCRSs list
 		if (!meta.getResponseCRSs().contains(requestCRS)) {
 			throw new WcsException(
 			"This Coverage does not support the CRS requested.");
 		}
 		// - then create the Coordinate Reference System
 		final CoordinateReferenceSystem sourceCRS = CRS.decode(requestCRS);
 		// This is the CRS of the Coverage Envelope
 		final CoordinateReferenceSystem cvCRS = ((GeneralEnvelope) coverage
 				.getEnvelope()).getCoordinateReferenceSystem();
 		final MathTransform GCCRSTodeviceCRSTransformdeviceCRSToGCCRSTransform = CRS
 		.transform(cvCRS, sourceCRS, true);
		final MathTransform GCCRSTodeviceCRSTransform = CRS.transform(cvCRS, targetCRS, true);
 		final MathTransform deviceCRSToGCCRSTransform = GCCRSTodeviceCRSTransformdeviceCRSToGCCRSTransform
 		.inverse();
 
 		com.vividsolutions.jts.geom.Envelope envelope = request.getEnvelope();
 		GeneralEnvelope destinationEnvelope;
 		final boolean lonFirst = sourceCRS.getCoordinateSystem().getAxis(0)
 		.getDirection().absolute().equals(AxisDirection.EAST);
 		// the envelope we are provided with is lon,lat always
 		if (!lonFirst)
 			destinationEnvelope = new GeneralEnvelope(new double[] {
 					envelope.getMinY(), envelope.getMinX() }, new double[] {
 					envelope.getMaxY(), envelope.getMaxX() });
 		else
 			destinationEnvelope = new GeneralEnvelope(new double[] {
 					envelope.getMinX(), envelope.getMinY() }, new double[] {
 					envelope.getMaxX(), envelope.getMaxY() });
 		destinationEnvelope.setCoordinateReferenceSystem(sourceCRS);
 
 		// this is the destination envelope in the coverage crs
 		final GeneralEnvelope destinationEnvelopeInSourceCRS = (!deviceCRSToGCCRSTransform
 				.isIdentity()) ? CRSUtilities.transform(
 						deviceCRSToGCCRSTransform, destinationEnvelope)
 						: new GeneralEnvelope(destinationEnvelope);
 						destinationEnvelopeInSourceCRS.setCoordinateReferenceSystem(cvCRS);
 
 						/**
 						 * Band Select
 						 */
 						Coverage bandSelectedCoverage = WCSUtils.bandSelect(request.getParameters(), coverage);
 
 						/**
 						 * Crop
 						 */
 						final GridCoverage2D croppedGridCoverage = WCSUtils.crop(
 								bandSelectedCoverage,
 								(GeneralEnvelope) coverage.getEnvelope(), 
 								cvCRS, 
 								destinationEnvelopeInSourceCRS);
 
 						/**
 						 * Scale
 						 */
 						GridCoverage2D subCoverage = croppedGridCoverage;
 						if (request.getGridLow() != null && request.getGridHigh() != null) {
 							final int[] lowers = new int[] {
 									request.getGridLow()[0].intValue(),
 									request.getGridLow()[1].intValue() };
 							final int[] highers = new int[] {
 									request.getGridHigh()[0].intValue(),
 									request.getGridHigh()[1].intValue() };
 							
 							Rectangle destinationSize = new Rectangle(lowers[0], lowers[1], highers[0], highers[1]);
 //							// new grid range
 //							final GeneralGridRange newGridrange = new GeneralGridRange(lowers,
 //									highers);
 //
 //							subCoverage = WCSUtils.scale(croppedGridCoverage, newGridrange, coverage, cvCRS);
 
 							// ///////////////////////////////////////////////////////////////////
 							//
 							// DRAWING DIMENSIONS AND RESOLUTION
 							// I am here getting the final drawing dimensions (on the device) and
 							// the resolution for this renderer but in the CRS of the source
 							// coverage
 							// since I am going to compare this info with the same info for the
 							// source coverage. The objective is to come up with the needed scale
 							// factors for the original coverage in order to decide how to proceed.
 							// Options are first scale then reproject or the opposite.
 							//
 							// In case we need to upsample the coverage first we reproject and then
 							// we upsample otherwise we do the opposite in order
 							//
 							// ///////////////////////////////////////////////////////////////////
 							AffineTransform finalGridToWorldInGCCRS;
							if (!GCCRSTodeviceCRSTransform.isIdentity()) {
 								finalGridToWorldInGCCRS = new AffineTransform(
 										(AffineTransform) GridGeometry2D.getTransform(
 												new GeneralGridRange(destinationSize),
 												destinationEnvelopeInSourceCRS, false));
 							} else {
 								finalGridToWorldInGCCRS = new AffineTransform((AffineTransform) GridGeometry2D
 										.getTransform(new GeneralGridRange(destinationSize),
 												destinationEnvelope, false));
 							}
 							
 							// ///////////////////////////////////////////////////////////////////
 							//
 							// SCALE and REPROJECT in the best order.
 							// Let me now scale down or up to the EXACT needed SPATIAL resolution.
 							// This step does not prevent from having loaded an overview of the
 							// original image based on the requested scale but it complements it.
 							//
 							// ///////////////////////////////////////////////////////////////////
 							// //
 							//
 							// First step is computing the needed resolution levels for this
 							// coverage in its original crs to see the scale factors.
 							//
 							// //
 							final AffineTransform croppedCoverageGridToWorldTransform = (AffineTransform) ((GridGeometry2D) croppedGridCoverage
 									.getGridGeometry()).getGridToCoordinateSystem2D();
 							final double actualScaleX = lonFirst ? croppedCoverageGridToWorldTransform
 									.getScaleX()
 									: croppedCoverageGridToWorldTransform.getShearY();
 							final double actualScaleY = lonFirst ? croppedCoverageGridToWorldTransform
 									.getScaleY()
 									: croppedCoverageGridToWorldTransform.getShearX();
 							final double scaleX = actualScaleX
 									/ (lonFirst ? finalGridToWorldInGCCRS.getScaleX()
 											: finalGridToWorldInGCCRS.getShearY());
 							final double scaleY = actualScaleY
 									/ (lonFirst ? finalGridToWorldInGCCRS.getScaleY()
 											: finalGridToWorldInGCCRS.getShearX());
 							if (LOGGER.isLoggable(Level.FINE))
 								LOGGER.fine(new StringBuffer("Scale factors are ").append(scaleX)
 										.append(" ").append(scaleY).toString());
 							final GridRange range = croppedGridCoverage.getGridGeometry().getGridRange();
 							final int actualW = range.getLength(0);
 							final int actualH = range.getLength(1);
 							if (! (Math.round(actualW * scaleX) < MIN_DIM_TOLERANCE
 									&& Math.round(actualH * scaleY) < MIN_DIM_TOLERANCE)) {
 								Interpolation interpolation = null;
 								
 								/**
 								 * Interpolate (if necessary)
 								 */
 								final String interp_requested = request.getInterpolation();
 								if (interp_requested != null) {
 									int interp_type = -1;
 
 									if (interp_requested.equalsIgnoreCase("nearest neighbor"))
 										interp_type = Interpolation.INTERP_NEAREST;
 									else if (interp_requested.equalsIgnoreCase("bilinear"))
 										interp_type = Interpolation.INTERP_BILINEAR;
 									else if (interp_requested.equalsIgnoreCase("bicubic"))
 										interp_type = Interpolation.INTERP_BICUBIC;
 									else if (interp_requested.equalsIgnoreCase("bicubic_2"))
 										interp_type = Interpolation.INTERP_BICUBIC_2;
 									else
 										throw new WcsException(
 										"Unrecognized interpolation type. Allowed values are: nearest_neighbor, bilinear, bicubic, bicubic_2");
 
 									//subCoverage = WCSUtils.interpolate(subCoverage, Interpolation.getInstance(interp_type));
 									interpolation = Interpolation.getInstance(interp_type);
 								}
 								
 								if (LOGGER.isLoggable(Level.FINE))
 									LOGGER.fine(new StringBuffer("Using interpolation ").append(
 											interpolation).toString());
 								// //
 								//
 								// Now if we are upsampling first reproject then scale else first scale
 								// then reproject.
 								//
 								// //
 								final GridCoverage2D preSymbolizer;
 								if (scaleX * scaleY <= 1.0) {
 									int scaleXInt = (int) Math.floor(1 / scaleX);
 									scaleXInt = scaleXInt == 0 ? 1 : scaleXInt;
 									int scaleYInt = (int) Math.floor(1 / scaleY);
 									scaleYInt = scaleYInt == 0 ? 1 : scaleYInt;
 
 									// ///////////////////////////////////////////////////////////////////
 									//
 									// SCALE DOWN to the needed resolution
 									//
 									// ///////////////////////////////////////////////////////////////////
 									// //
 									//
 									// first step for down smapling is filtered subsample which is fast.
 									// 
 									// //
 									if (LOGGER.isLoggable(Level.FINE))
 										LOGGER
 												.fine(new StringBuffer(
 														"Filtered subsample with factors ").append(
 														scaleXInt).append(scaleYInt).toString());
 									final GridCoverage2D preScaledGridCoverage = WCSUtils.filteredSubsample(
 											croppedGridCoverage, scaleXInt, scaleYInt,
 											new InterpolationNearest(), BorderExtender
 													.createInstance(BorderExtender.BORDER_COPY));
 
 									// //
 									//
 									// Second step is scale
 									//
 									// //
 									if (LOGGER.isLoggable(Level.FINE))
 										LOGGER.fine(new StringBuffer("Scale down with factors ")
 												.append(scaleX * scaleXInt).append(scaleY * scaleYInt)
 												.toString());
 									final GridCoverage2D scaledGridCoverage;
 									if (scaleX * scaleXInt == 1.0 && scaleY * scaleYInt == 1.0)
 										scaledGridCoverage = preScaledGridCoverage;
 									else
 										scaledGridCoverage = WCSUtils.scale(scaleX * scaleXInt, scaleY
 												* scaleYInt, 0f, 0f,
												interpolation == null ? new InterpolationNearest()
 														: interpolation, BorderExtender
 														.createInstance(BorderExtender.BORDER_COPY),
 												preScaledGridCoverage);
 
 									// ///////////////////////////////////////////////////////////////////
 									//
 									// REPROJECT to the requested crs.
 									//
 									//
 									// ///////////////////////////////////////////////////////////////////
									if (!GCCRSTodeviceCRSTransform.isIdentity()) {
 										preSymbolizer = WCSUtils.resample(scaledGridCoverage, targetCRS,
												interpolation == null ? new InterpolationNearest()
 														: interpolation);
 										if (LOGGER.isLoggable(Level.FINE))
 											LOGGER.fine(new StringBuffer("Reprojecting to crs ")
 													.append(targetCRS.toWKT()).toString());
 									} else
 										preSymbolizer = scaledGridCoverage;
 
 								} else {
 
 									// ///////////////////////////////////////////////////////////////////
 									//
 									// REPROJECT to the requested crs
 									//
 									//
 									// ///////////////////////////////////////////////////////////////////
 									final GridCoverage2D reprojectedCoverage;
									if (!GCCRSTodeviceCRSTransform.isIdentity()) {
 										reprojectedCoverage = WCSUtils.resample(croppedGridCoverage,
 												targetCRS,
												interpolation == null ? new InterpolationNearest()
 														: interpolation);
 										if (LOGGER.isLoggable(Level.FINE))
 											LOGGER.fine(new StringBuffer("Reprojecting to crs ")
 													.append(targetCRS.toWKT()).toString());
 									} else
 										reprojectedCoverage = croppedGridCoverage;
 
 									// ///////////////////////////////////////////////////////////////////
 									//
 									// SCALE UP to the needed resolution
 									//
 									// ///////////////////////////////////////////////////////////////////
 									if (LOGGER.isLoggable(Level.FINE))
 										LOGGER.fine(new StringBuffer("Scale up with factors ").append(
 												scaleX).append(scaleY).toString());
 									preSymbolizer = (GridCoverage2D) WCSUtils.scale(scaleX, scaleY, 0f, 0f,
											interpolation == null ? new InterpolationNearest()
 													: interpolation, BorderExtender
 													.createInstance(BorderExtender.BORDER_COPY),
 											reprojectedCoverage);
 
 								}
 								
 								subCoverage = preSymbolizer;
 							} else {
 								if (LOGGER.isLoggable(Level.FINE))
 									LOGGER
 											.fine(new StringBuffer(
 													"Skipping the actual coverage because one of the final dimension is null")
 													.toString());
 								subCoverage = croppedGridCoverage;
 								/**
 								 * Reproject
 								 */
 								subCoverage = WCSUtils.reproject(
 										subCoverage,
 										sourceCRS, 
 										targetCRS);
 							}
 						} else {
 							/**
 							 * Reproject
 							 */
 							subCoverage = WCSUtils.reproject(
 									subCoverage,
 									sourceCRS, 
 									targetCRS);							
 						}
 
 						return subCoverage;
 	}
 }
