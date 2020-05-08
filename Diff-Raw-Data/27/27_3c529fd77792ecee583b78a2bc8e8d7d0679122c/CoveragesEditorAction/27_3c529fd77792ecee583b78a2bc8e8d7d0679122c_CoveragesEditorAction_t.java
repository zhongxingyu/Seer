 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.action.data;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.util.MessageResources;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.data.coverage.grid.AbstractGridFormat;
 import org.geotools.factory.FactoryRegistryException;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.parameter.DefaultParameterDescriptor;
 import org.geotools.referencing.CRS;
 import org.opengis.coverage.grid.Format;
 import org.opengis.coverage.grid.GridCoverage;
 import org.opengis.coverage.grid.GridCoverageReader;
 import org.opengis.parameter.GeneralParameterValue;
 import org.opengis.parameter.InvalidParameterValueException;
 import org.opengis.parameter.ParameterDescriptor;
 import org.opengis.parameter.ParameterNotFoundException;
 import org.opengis.parameter.ParameterValue;
 import org.opengis.parameter.ParameterValueGroup;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.cs.AxisDirection;
 import org.opengis.referencing.cs.CoordinateSystem;
 import org.opengis.spatialschema.geometry.MismatchedDimensionException;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.action.HTMLEncoder;
 import org.vfny.geoserver.config.CoverageConfig;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.form.data.CoveragesEditorForm;
 import org.vfny.geoserver.global.CoverageStoreInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.MetaDataLink;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.util.CoverageUtils;
 
 /**
  * These Action handles all the buttons for the Coverage Editor.
  * 
  * <p>
  * Buttons that make this action go:
  * 
  * <ul>
  * <li> Submit: update the CoverageConfig held by the user, punt it back into
  * DataConfig and return to the CoverageSelect screen. </li>
  * </ul>
  * 
  * As usual we will have to uninternationlize the action name provided to us.
  * </p>
  * 
  * @author Richard Gould
  * @author Jody Garnett
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public final class CoveragesEditorAction extends ConfigAction {
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			UserContainer user, HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException {
 		if (LOGGER.isLoggable(Level.FINER)) {
 			LOGGER.finer(new StringBuffer("form bean:").append(
 					form.getClass().getName()).toString());
 		}
 
 		final CoveragesEditorForm coverageForm = (CoveragesEditorForm) form;
 
 		String action = coverageForm.getAction();
 		if (LOGGER.isLoggable(Level.FINER)) {
 			LOGGER.finer(new StringBuffer("CoveragesEditorAction is ").append(
 					action).toString());
 		}
 
 		String newCoverage = coverageForm.getNewCoverage();
 		if (LOGGER.isLoggable(Level.FINER)) {
 			LOGGER.finer(new StringBuffer("CoveragesEditorNew is ").append(
 					newCoverage).toString());
 		}
 
 		Locale locale = (Locale) request.getLocale();
 		MessageResources messages = getResources(request);
 		final String SUBMIT = HTMLEncoder.decode(messages.getMessage(locale,
 				"label.submit"));
 		final String ENVELOPE = HTMLEncoder.decode(messages.getMessage(locale,
 				"config.data.calculateBoundingBox.label"));
 		if (LOGGER.isLoggable(Level.FINER)) {
 			LOGGER.finer(new StringBuffer("ENVELOPE: ").append(ENVELOPE)
 					.toString());
 		}
 
 		if (action.equals(SUBMIT)) {
 			return executeSubmit(mapping, coverageForm, user, request);
 		}
 
 		if (newCoverage != null && newCoverage.equals("true")) {
 			if (LOGGER.isLoggable(Level.FINER)) {
 				LOGGER.finer(new StringBuffer("NEW COVERAGE: ").append(
 						newCoverage).toString());
 			}
 			request.setAttribute(DataCoveragesNewAction.NEW_COVERAGE_KEY,
 					"true");
 		}
 
 		if (action.equals(ENVELOPE)) {
 			return executeEnvelope(mapping, coverageForm, user, request);
 		}
 
 		// Update, Up, Down, Add, Remove need to resync
 		try {
 			sync(coverageForm, user.getCoverageConfig(), request);
 		} catch (FactoryException e) {
 			throw new ServletException(e);
 		}
 		form.reset(mapping, request);
 		return mapping.findForward("config.data.coverage.editor");
 	}
 
 	/**
 	 * Populate the bounding box fields from the source and pass control back to
 	 * the UI
 	 * 
 	 * @param mapping
 	 *            DOCUMENT ME!
 	 * @param coverageForm
 	 *            DOCUMENT ME!
 	 * @param user
 	 *            DOCUMENT ME!
 	 * @param request
 	 *            DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 * 
 	 * @throws IOException
 	 *             DOCUMENT ME!
 	 * @throws ServletException
 	 *             DOCUMENT ME!
 	 */
 	private ActionForward executeEnvelope(ActionMapping mapping,
 			CoveragesEditorForm coverageForm, UserContainer user,
 			HttpServletRequest request) throws IOException, ServletException {
 		final String formatID = coverageForm.getFormatId(); 
 		final Data catalog = getData();
 		CoverageStoreInfo cvStoreInfo = catalog.getFormatInfo(formatID);
 		if(cvStoreInfo == null) {
 			cvStoreInfo = new CoverageStoreInfo(getDataConfig().getDataFormat(formatID).toDTO(), catalog);
 		}
 		GridCoverage gc = null; 
 		
 		try {
 			final Format format = cvStoreInfo.getFormat();
 			final ParameterValueGroup params = format.getReadParameters();
 			GridCoverageReader reader = cvStoreInfo.getReader();
 			if (reader == null)
 				try {
 					reader = ((AbstractGridFormat) format).getReader(CoverageUtils.getResourceAsFile(cvStoreInfo.getUrl(), getServletContext(), null/*catalog.getBaseDir()*/));
 				} catch (MalformedURLException ex) {
 					throw new ServletException(ex);
 				}
             // After extracting params into a map
             List parameters = new ArrayList(); // values used for connection
 			if (params != null) {
 				final List list = params.values();
 				final Iterator it = list.iterator();
 				ParameterValue param;
 				ParameterDescriptor descr;
 				String key;
 
 				Object value;
 				while (it.hasNext()) {
 					param = ((ParameterValue) it.next());
 					descr = (ParameterDescriptor) param.getDescriptor();
 					key = descr.getName().toString();
 					if (AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString().equalsIgnoreCase(key))
 						value = null;
 					else
 					    value = CoverageUtils.getCvParamValue(key, param, cvStoreInfo.getParameters());
 					if (value != null) {
 					    //params.parameter(key).setValue(value);
 					    parameters.add(new DefaultParameterDescriptor(key, value.getClass(), null, value).createValue());
 					}
 				}
 			}
 			gc = reader.read((GeneralParameterValue[]) parameters.toArray(new GeneralParameterValue[parameters.size()]));
 			if (gc == null || !(gc instanceof GridCoverage2D))
 				throw new IOException(
 						"The requested coverage could not be found.");
 		} catch (InvalidParameterValueException e) {
 			throw new ServletException(e);
 		} catch (ParameterNotFoundException e) {
 			throw new ServletException(e);
 		} catch (MalformedURLException e) {
 			throw new ServletException(e);
 		} catch (IllegalArgumentException e) {
 			throw new ServletException(e);
 		} catch (SecurityException e) {
 			throw new ServletException(e);
 		} catch (IOException e) {
 			throw new ServletException(e);
 		}
 
 		try {
 			final GridCoverage2D coverage = (GridCoverage2D) gc;
 			final CoordinateReferenceSystem sourceCRS = coverage
 					.getEnvelope2D().getCoordinateReferenceSystem();
 			final GeneralEnvelope gEnvelope = (GeneralEnvelope) gc
 					.getEnvelope();
 			final GeneralEnvelope targetEnvelope = gEnvelope;
 //			final GeneralEnvelope envelope = CoverageStoreUtils
 //					.adjustEnvelopeLongitudeFirst(sourceCRS, targetEnvelope);
 			final GeneralEnvelope envelope =  targetEnvelope;
 
 			if (!sourceCRS.getIdentifiers().isEmpty()) {
 				coverageForm.setSrsName(sourceCRS.getIdentifiers().toArray()[0]
 						.toString());
 			} else {
 				coverageForm.setSrsName("UNKNOWN");
 			}
 			coverageForm.setWKTString(sourceCRS.toWKT());
 			coverageForm.setMinX(Double.toString(envelope.getLowerCorner()
 					.getOrdinate(0)));
 			coverageForm.setMaxX(Double.toString(envelope.getUpperCorner()
 					.getOrdinate(0)));
 			coverageForm.setMinY(Double.toString(envelope.getLowerCorner()
 					.getOrdinate(1)));
 			coverageForm.setMaxY(Double.toString(envelope.getUpperCorner()
 					.getOrdinate(1)));
 		} catch (FactoryRegistryException e) {
 			throw new ServletException(e);
 		} catch (MismatchedDimensionException e) {
 			throw new ServletException(e);
 		} catch (IndexOutOfBoundsException e) {
 			throw new ServletException(e);
 		} 
 
 		return mapping.findForward("config.data.coverage.editor");
 	}
 
 	/**
 	 * Sync generated attributes with schemaBase.
 	 * 
 	 * @param form
 	 * @param config
 	 * @throws FactoryException
 	 */
 	private void sync(CoveragesEditorForm form, CoverageConfig config,
 			HttpServletRequest request) throws FactoryException {
 		config.setDefaultInterpolationMethod(form
 				.getDefaultInterpolationMethod());
 		config.setDescription(form.getDescription());
 		config.setInterpolationMethods(interpolationMethods(form));
 		config.setKeywords(keyWords(form));
 		config.setLabel(form.getLabel());
 		config.setMetadataLink(metadataLink(form));
 		config.setNativeFormat(form.getNativeFormat());
 		config.setRequestCRSs(requestCRSs(form));
 		config.setResponseCRSs(responseCRSs(form));
 		config.setCrs(CRS.parseWKT(form.getWKTString()));
 		config.setSrsName(form.getSrsName());
 		config.setSrsWKT(form.getWKTString());
 		config.setNativeCRS(form.getNativeCRS());
 		config
 				.setEnvelope(getEnvelope(form, CRS
 						.parseWKT(form.getWKTString())));
 		config.setSupportedFormats(supportedFormats(form));
 		config.setDefaultStyle(form.getStyleId());
 		if (form.getOtherSelectedStyles() != null) {
     		config.getStyles().clear();
     		for (int i=0;i<form.getOtherSelectedStyles().length;i++) {
     			config.addStyle(form.getOtherSelectedStyles()[i]);
     		}
     	}
 		
 		config.setName(form.getName());
 		config.setWmsPath(form.getWmsPath());
 		final StringBuffer temp = new StringBuffer(config.getFormatId());
 		temp.append("_").append(form.getName());
 		config.setDirName(temp.toString());

		/**
		 * Sync params
		 */
		config.setParamKeys(form.getParamKeys());
		config.setParamValues(form.getParamValues());
 	}
 
 	/**
 	 * Execute Submit Action.
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param user
 	 * @param request
 	 * 
 	 * @return
 	 * @throws FactoryException
 	 */
 	private ActionForward executeSubmit(ActionMapping mapping,
 			CoveragesEditorForm form, UserContainer user,
 			HttpServletRequest request) throws IOException {
 		final CoverageConfig config = user.getCoverageConfig();
 		try {
 			sync(form, config, request);
 		} catch (FactoryException e) {
 			final IOException ex = new IOException(e.getLocalizedMessage());
 			ex.initCause(e);
 			throw ex;
 		}
 
 		final DataConfig dataConfig = (DataConfig) getDataConfig();
 		final StringBuffer coverage = new StringBuffer(config.getFormatId());
 		dataConfig.addCoverage(coverage.append(":").append(config.getName())
 				.toString(), config);
 
 		// Don't think reset is needed (as me have moved on to new page)
 		// form.reset(mapping, request);
 		getApplicationState().notifyConfigChanged();
 
 		// Coverage no longer selected
 		user.setCoverageConfig(null);
 
 		return mapping.findForward("config.data.coverage");
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param coverageForm
 	 * @param system
 	 * 
 	 * @return Bounding box in lat long TODO is this correct
 	 */
 	private GeneralEnvelope getEnvelope(CoveragesEditorForm coverageForm,
 			CoordinateReferenceSystem crs) {
 		final double[] coordinates = new double[4];
 		final CoordinateSystem cs = crs.getCoordinateSystem();
 		boolean lonFirst = true;
 		if (cs.getAxis(0).getDirection().absolute().equals(AxisDirection.NORTH)) {
 			lonFirst = false;
 		}
 		boolean swapXY = !lonFirst;
 
 		// latitude index
 		final int latIndex = lonFirst ? 1 : 0;
 
 		final AxisDirection latitude = cs.getAxis(latIndex).getDirection();
 		final AxisDirection longitude = cs.getAxis((latIndex + 1) % 2)
 				.getDirection();
 		final boolean[] reverse = new boolean[] {
 				lonFirst ? !longitude.equals(AxisDirection.EAST) : !latitude
 						.equals(AxisDirection.NORTH),
 				lonFirst ? !latitude.equals(AxisDirection.NORTH) : !longitude
 						.equals(AxisDirection.EAST) };
 
 		coordinates[0] = (!reverse[(latIndex + 1) % 2] ? (!swapXY ? Double
 				.parseDouble(coverageForm.getMinX()) : Double
 				.parseDouble(coverageForm.getMinY())) : (!swapXY ? Double
 				.parseDouble(coverageForm.getMaxX()) : Double
 				.parseDouble(coverageForm.getMaxY())));
 		coordinates[1] = (!reverse[latIndex] ? (!swapXY ? Double
 				.parseDouble(coverageForm.getMinY()) : Double
 				.parseDouble(coverageForm.getMinX())) : (!swapXY ? Double
 				.parseDouble(coverageForm.getMaxY()) : Double
 				.parseDouble(coverageForm.getMaxX())));
 		coordinates[2] = (!reverse[(latIndex + 1) % 2] ? (!swapXY ? Double
 				.parseDouble(coverageForm.getMaxX()) : Double
 				.parseDouble(coverageForm.getMaxY())) : (!swapXY ? Double
 				.parseDouble(coverageForm.getMinX()) : Double
 				.parseDouble(coverageForm.getMinY())));
 		coordinates[3] = (!reverse[latIndex] ? (!swapXY ? Double
 				.parseDouble(coverageForm.getMaxY()) : Double
 				.parseDouble(coverageForm.getMaxX())) : (!swapXY ? Double
 				.parseDouble(coverageForm.getMinY()) : Double
 				.parseDouble(coverageForm.getMinX())));
 
 		GeneralEnvelope envelope = new GeneralEnvelope(new double[] {
 				coordinates[0], coordinates[1] }, new double[] {
 				coordinates[2], coordinates[3] });
 
 		envelope.setCoordinateReferenceSystem(crs);
 
 		return envelope;
 	}
 
 	private MetaDataLink metadataLink(CoveragesEditorForm coverageForm) {
 		MetaDataLink ml = new MetaDataLink();
 
 		if (coverageForm.getMetadataLink() != null
 				&& coverageForm.getMetadataLink().length() > 0) {
 			ml.setAbout(coverageForm.getMetadataLink());
 			ml.setMetadataType("other");
 		} else {
 			ml = null;
 		}
 
 		return ml;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param coverageForm
 	 * 
 	 * @return Set of keywords
 	 */
 	private List keyWords(CoveragesEditorForm coverageForm) {
 		LinkedList keywords = new LinkedList();
 		String[] array = (coverageForm.getKeywords() != null) ? coverageForm
 				.getKeywords().split(" ") : new String[0];
 		final int length = array.length;
 		for (int i = 0; i < length; i++) {
 			keywords.add(array[i]);
 		}
 
 		return keywords;
 	}
 
 	private List interpolationMethods(CoveragesEditorForm coverageForm) {
 		LinkedList interpolationMethods = new LinkedList();
 		String[] array = (coverageForm.getInterpolationMethods() != null) ? coverageForm
 				.getInterpolationMethods().split(",")
 				: new String[0];
 		final int length = array.length;
 		for (int i = 0; i < length; i++) {
 			interpolationMethods.add(array[i]);
 		}
 
 		return interpolationMethods;
 	}
 
 	private List requestCRSs(CoveragesEditorForm coverageForm) {
 		LinkedList requestCRSs = new LinkedList();
 		String[] array = (coverageForm.getRequestCRSs() != null) ? coverageForm
 				.getRequestCRSs().split(",") : new String[0];
 		final int length = array.length;
 		for (int i = 0; i < length; i++) {
 			requestCRSs.add(array[i]);
 		}
 
 		return requestCRSs;
 	}
 
 	private List responseCRSs(CoveragesEditorForm coverageForm) {
 		LinkedList responseCRSs = new LinkedList();
 		String[] array = (coverageForm.getResponseCRSs() != null) ? coverageForm
 				.getResponseCRSs().split(",")
 				: new String[0];
 		final int length = array.length;
 		for (int i = 0; i < length; i++) {
 			responseCRSs.add(array[i]);
 		}
 
 		return responseCRSs;
 	}
 
 	private List supportedFormats(CoveragesEditorForm coverageForm) {
 		LinkedList supportedFormats = new LinkedList();
 		String[] array = (coverageForm.getSupportedFormats() != null) ? coverageForm
 				.getSupportedFormats().split(",")
 				: new String[0];
 		final int length = array.length;
 		for (int i = 0; i < length; i++) {
 			supportedFormats.add(array[i]);
 		}
 
 		return supportedFormats;
 	}
 }
