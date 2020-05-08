 /* GeoserverGsManagerDaoImpl.java
  * 
  * Copyright (C) 2012
  * 
  * This file is part of project persistence-geo-core
  * 
  * This software is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  * As a special exception, if you link this library with other files to
  * produce an executable, this library does not by itself cause the
  * resulting executable to be covered by the GNU General Public License.
  * This exception does not however invalidate any other reasons why the
  * executable file might be covered by the GNU General Public License.
  * 
  * Authors:: Juan Luis Rodríguez Ponce (mailto:jlrodriguez@emergya.com)
  */
 package com.emergya.persistenceGeo.dao.impl;
 
 import it.geosolutions.geoserver.rest.GeoServerRESTManager;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.ParameterConfigure;
 import it.geosolutions.geoserver.rest.GeoServerRESTReader;
 import it.geosolutions.geoserver.rest.HTTPUtils;
 import it.geosolutions.geoserver.rest.decoder.RESTCoverageList;
 import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
 import it.geosolutions.geoserver.rest.decoder.RESTLayer;
 import it.geosolutions.geoserver.rest.decoder.RESTWorkspaceList;
 import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
 import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
 import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
 import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
 import it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder;
 import it.geosolutions.geoserver.rest.manager.GeoServerRESTStoreManager;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.emergya.persistenceGeo.dao.GeoserverDao;
 import com.emergya.persistenceGeo.exceptions.GeoserverException;
 import com.emergya.persistenceGeo.service.GeoserverService;
 import com.emergya.persistenceGeo.utils.BoundingBox;
 import com.emergya.persistenceGeo.utils.GSFeatureTypeNativeNameEncoder;
 import com.emergya.persistenceGeo.utils.GeoserverUtils;
 import com.emergya.persistenceGeo.utils.GsCoverageDetails;
 import com.emergya.persistenceGeo.utils.GsCoverageStoreData;
 import com.emergya.persistenceGeo.utils.GsFeatureDescriptor;
 import com.emergya.persistenceGeo.utils.GsLayerDescriptor;
 import com.emergya.persistenceGeo.utils.GsRestApiConfiguration;
 
 /**
  * Implementación de {@link GeoserverDao} utilizando las clases de
  * geoserver-manager de Geosolutions para la conexión con Geoserver. Requiere de
  * un objeto {@link com.emergya.persistenceGeo.utils.GsRestApiConfiguration} .
  * 
  * @author <a href="mailto:jlrodriguez@emergya.com">jlrodriguez</a>
  * 
  */
 public class GeoserverGsManagerDaoImpl implements GeoserverDao {
 	private static final Log LOG = LogFactory
 			.getLog(GeoserverGsManagerDaoImpl.class);
 
 	private static final String GET_COVERAGE_DETAILS_URL = "/rest/workspaces/%s/coveragestores/%s/coverages/%s.xml";
 	private static final String GET_COVERAGE_STORE_DATA_URL = "/rest/workspaces/%s/coveragestores/%s.xml";
 	private static final String SET_LAYER_STYLE_URL = "/rest/layers/%s:%s";
 	private static final String SET_LAYER_STYLE_PAYLOAD = "<layer><enabled>true</enabled><defaultStyle><name>%s</name></defaultStyle></layer>";
 
 	@Autowired
 	private GsRestApiConfiguration gsConfiguration;
 
 	/**
 	 * @return the gsConfiguration
 	 */
 	public GsRestApiConfiguration getGsConfiguration() {
 		return gsConfiguration;
 	}
 
 	/**
 	 * @param gsConfiguration
 	 *            the gsConfiguration to set
 	 */
 	public void setGsConfiguration(GsRestApiConfiguration gsConfiguration) {
 		this.gsConfiguration = gsConfiguration;
 	}
 
 	/**
 	 * @return
 	 * @throws MalformedURLException
 	 */
 	private GeoServerRESTPublisher getPublisher() throws MalformedURLException {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Creating GeoServerRESTPublisher.");
 		}
 		GeoServerRESTManager manager = new GeoServerRESTManager(new URL(
 				gsConfiguration.getServerUrl()),
 				gsConfiguration.getAdminUsername(),
 				gsConfiguration.getAdminPassword());
 		GeoServerRESTPublisher publisher = manager.getPublisher();
 		return publisher;
 	}
 
 	private GeoServerRESTReader getReader() throws MalformedURLException {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Creating GeoServerRESTReader.");
 		}
 		GeoServerRESTManager manager = new GeoServerRESTManager(new URL(
 				gsConfiguration.getServerUrl()),
 				gsConfiguration.getAdminUsername(),
 				gsConfiguration.getAdminPassword());
 		GeoServerRESTReader reader = manager.getReader();
 		return reader;
 	}
 
 	private GeoServerRESTStoreManager getDatastoreManager()
 			throws MalformedURLException {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Creating GeoServerRESTStoreManager.");
 		}
 		GeoServerRESTManager manager = new GeoServerRESTManager(new URL(
 				gsConfiguration.getServerUrl()),
 				gsConfiguration.getAdminUsername(),
 				gsConfiguration.getAdminPassword());
 		GeoServerRESTStoreManager dsManager = manager.getStoreManager();
 		return dsManager;
 	}
 
 	/**
 	 * Utility method for transforming a GsFeatureDescriptor to a
 	 * GsFeatureTypeEncoder.
 	 * 
 	 * @param descriptor
 	 * @return
 	 */
 	private GSFeatureTypeNativeNameEncoder tranformToGSFeatureTypeEncoder(
 			GsFeatureDescriptor descriptor) {
 		GSFeatureTypeNativeNameEncoder encoder = new GSFeatureTypeNativeNameEncoder();
 		try {
 			BeanUtils.copyProperties(encoder, descriptor);
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		for (String keyword : descriptor.getKeywords()) {
 			encoder.addKeyword(keyword);
 		}
 		BoundingBox llbb = descriptor.getLatLonBoundingBox();
 		encoder.setLatLonBoundingBox(llbb.getMinx(), llbb.getMiny(),
 				llbb.getMaxx(), llbb.getMaxy(), llbb.getSrs());
 
 		BoundingBox nbb = descriptor.getNativeBoundingBox();
 		encoder.setNativeBoundingBox(nbb.getMinx(), nbb.getMiny(),
 				nbb.getMaxx(), nbb.getMaxy(), nbb.getSrs());
 
 		return encoder;
 	}
 
 	private GSLayerEncoder tranformToGSLayerEncoder(
 			GsLayerDescriptor layerDescriptor) {
 
 		GSLayerEncoder encoder = new GSLayerEncoder();
 		try {
 			BeanUtils.copyProperties(encoder, layerDescriptor);
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return encoder;
 	}
 
 	@Override
 	public boolean createWorkspace(String name) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Creating workspace [name=" + name + "].");
 		}
 		boolean result = true;
 		String sanitizedName = GeoserverUtils.createName(name);
 		try {
 			GeoServerRESTPublisher publisher = getPublisher();
 			result = publisher.createWorkspace(sanitizedName);
 		} catch (IllegalArgumentException e) {
 			result = false;
 		} catch (MalformedURLException e) {
 			result = false;
 		}
 		return result;
 	}
 
 	@Override
 	public RESTWorkspaceList getWorkspaceList() {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Retrieving workspace list.");
 		}
 		GeoServerRESTReader reader;
 		RESTWorkspaceList list = null;
 		try {
 			reader = getReader();
 			list = reader.getWorkspaces();
 
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return list;
 	}
 
 	@Override
 	public boolean deleteWorkspace(String name) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Deleting workspace [name=" + name
 					+ "] and all its content (layers, datasources, ...)");
 		}
 		boolean result = false;
 		try {
 			GeoServerRESTPublisher publisher = getPublisher();
 			// Elimina el workspace y su contenido de forma recursiva
 			// (datastores, coverages, featureTypes, ...)
 			result = publisher.removeWorkspace(name, true);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.dao.GeoserverDao#createNamespace(java.lang
 	 * .String, java.net.URI)
 	 */
 	@Override
 	public boolean createNamespace(String prefix, URI uri) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Creating Namespace [prefix=" + prefix + ", uri=" + uri
 					+ "]");
 		}
 		boolean result = false;
 		GeoServerRESTPublisher publisher;
 
 		String sanitizedPrefix = GeoserverUtils.createName(prefix);
 		try {
 			publisher = getPublisher();
 			result = publisher.createNamespace(sanitizedPrefix, uri);
 
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		} catch (IllegalArgumentException iae) {
 			LOG.error("Los valores de prefix o uri no son válidos", iae);
 			throw new GeoserverException("Error al crear el namespace [prefix="
 					+ prefix + ", uri=" + uri + "]", iae);
 		}
 		return result;
 	}
 
 	public boolean createDatastore(String workspaceName, String datastoreName) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Creating Datastore [workspace=" + workspaceName
 					+ ", datastoreName=" + datastoreName + "]");
 		}
 
 		boolean result = false;
 		GeoServerRESTStoreManager dsManager;
 
 		String sanitizedName = GeoserverUtils.createName(datastoreName);
 
 		try {
 			dsManager = getDatastoreManager();
 			GSPostGISDatastoreEncoder properties = new GSPostGISDatastoreEncoder(
 					sanitizedName);
 			properties.setHost(gsConfiguration.getDbHost());
 			properties.setPort(gsConfiguration.getDbPort());
 			properties.setDatabase(gsConfiguration.getDbName());
 			if (gsConfiguration.getDbSchema() != null
 					&& !gsConfiguration.getDbSchema().isEmpty()) {
 				properties.setSchema(gsConfiguration.getDbSchema());
 			}
 			properties.setUser(gsConfiguration.getDbUser());
 			properties.setPassword(gsConfiguration.getDbPassword());
 
 			result = dsManager.create(workspaceName, properties);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return result;
 	}
 
 	public boolean createDatastoreJndi(String workspaceName,
 			String datastoreName) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Creating Datastore JNDI [workspace=" + workspaceName
 					+ ", datastoreName=" + datastoreName + "]");
 		}
 
 		boolean result = false;
 		GeoServerRESTStoreManager dsManager;
 		String sanitizedName = GeoserverUtils.createName(datastoreName);
 		try {
 			dsManager = getDatastoreManager();
 			GSPostGISDatastoreEncoder properties = new GSPostGISDatastoreEncoder(
 					sanitizedName);
 
 			properties.setDatabaseType(gsConfiguration.getDbType());
 			if (gsConfiguration.getDbSchema() != null
 					&& !gsConfiguration.getDbSchema().isEmpty()) {
 				properties.setSchema(gsConfiguration.getDbSchema());
 			}
 			properties.setType(gsConfiguration.getDatasourceType());
 			properties.setJndiReferenceName(gsConfiguration
 					.getJndiReferenceName());
 			properties.setPreparedStatements(true);
 
 			result = dsManager.create(workspaceName, properties);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.dao.GeoserverDao#checkGeoserverConfiguration()
 	 */
 	public boolean checkGeoserverConfiguration() {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Checking GeoServer configuration [host="
 					+ gsConfiguration.getServerUrl() + "]");
 		}
 		GeoServerRESTReader gsReader;
 		try {
 			gsReader = getReader();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 		return gsReader.existGeoserver();
 	}
 
 	public boolean publishPostgisLayer(String workspace, String storename,
 			GsFeatureDescriptor featureDescriptor,
 			GsLayerDescriptor layerDescriptor) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Publishing Postgis Layer [workspace=" + workspace
 					+ ", storeName=" + storename + ", tableName="
 					+ featureDescriptor.getNativeName() + "]");
 		}
 		boolean result = false;
 		GeoServerRESTPublisher gsPublisher;
 
 		try {
 			gsPublisher = getPublisher();
 			GSFeatureTypeNativeNameEncoder fte = this
 					.tranformToGSFeatureTypeEncoder(featureDescriptor);
 
 			GSLayerEncoder layerEncoder = this
 					.tranformToGSLayerEncoder(layerDescriptor);
 
 			result = gsPublisher.publishDBLayer(workspace, storename, fte,
 					layerEncoder);
 		} catch (MalformedURLException murle) {
 			LOG.error("Malformed Geoserver REST API URL", murle);
 			throw new GeoserverException("Malformed Geoserver REST API URL",
 					murle);
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.dao.GeoserverDao#deletePostgisFeatureTye(java
 	 * .lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean deletePostgisFeatureType(String workspaceName,
 			String datastoreName, String layerName) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Deleting Postgis Layer [workspace=" + workspaceName
 					+ ", datastore=" + datastoreName + "]");
 		}
 		boolean result = false;
 		GeoServerRESTPublisher gsPublisher;
 
 		try {
 			gsPublisher = getPublisher();
 			result = gsPublisher.unpublishFeatureType(workspaceName,
 					datastoreName, layerName);
 
 		} catch (MalformedURLException murle) {
 			LOG.error("Malformed Geoserver REST API URL", murle);
 			throw new GeoserverException("Malformed Geoserver REST API URL",
 					murle);
 		}
 
 		return result;
 	}
 
 	@Override
 	public boolean deleteCoverage(String workspaceName, String coverageLayer) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Deleting Coverage Layer [workspace=" + workspaceName
 					+ ", datastore=" + coverageLayer + "]");
 		}
 		boolean result = false;
 		GeoServerRESTPublisher gsPublisher;
 
 		try {
 			gsPublisher = getPublisher();
 			result = gsPublisher.unpublishCoverage(workspaceName,
 					coverageLayer, coverageLayer);
 
 		} catch (MalformedURLException murle) {
 			LOG.error("Malformed Geoserver REST API URL", murle);
 			throw new GeoserverException("Malformed Geoserver REST API URL",
 					murle);
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.dao.GeoserverDao#existsLayerInWorkspace(java
 	 * .lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean existsLayerInWorkspace(String layerName, String workspaceName) {
 		boolean result = false;
 		try {
 			GeoServerRESTManager manager = new GeoServerRESTManager(new URL(
 					gsConfiguration.getServerUrl()),
 					gsConfiguration.getAdminUsername(),
 					gsConfiguration.getAdminPassword());
 			result = manager.getReader().getLayer(
 					workspaceName + ":" + layerName) != null;
 
 		} catch (IllegalArgumentException e) {
 			LOG.error(
 					"URL de geoserver incorrecta: "
 							+ gsConfiguration.getServerUrl(), e);
 		} catch (MalformedURLException e) {
 			LOG.error(
 					"URL de geoserver incorrecta: "
 							+ gsConfiguration.getServerUrl(), e);
 		}
 
 		return result;
 	}
 
 	@Override
 	public boolean publishGeoTIFF(String workspace, String layerName,
 			File geotiff, String crs) {
 		GeoServerRESTPublisher gsPublisher;
 
 		layerName = GeoserverUtils.createName(layerName);
 		boolean result = false;
 		try {
 			gsPublisher = getPublisher();
 			// result = gsPublisher.publishGeoTIFF(workspace, storeName,
 			// geotiff);
 			result = gsPublisher.publishGeoTIFF(workspace, layerName,
 					layerName, geotiff, crs, ProjectionPolicy.FORCE_DECLARED,
 					DEFAULT_RASTER_STYLE, null);
 			if (result) {
 				result = configureCoverage(workspace, layerName, crs,
 						gsPublisher);
 			}
 
 		} catch (FileNotFoundException e) {
 			LOG.error("File not found", e);
 			throw new GeoserverException("File not found", e);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 		return result;
 	}
 
 	@Override
 	public boolean publishImageMosaic(String workspaceName, String layerName,
 			File zipFile, String crs) {
 		boolean result = false;
 		GeoServerRESTPublisher gsPublisher;
 
 		layerName = GeoserverUtils.createName(layerName);
 
 		try {
 			gsPublisher = getPublisher();
 			NameValuePair paramName = new NameValuePair("coverageName",
 					layerName);
 			result = gsPublisher.publishImageMosaic(workspaceName, layerName,
 					zipFile, ParameterConfigure.FIRST, paramName);
 			if (result) {
 				result = configureCoverage(workspaceName, layerName, crs,
 						gsPublisher);
 			}
 		} catch (FileNotFoundException e) {
 			LOG.error("File not found", e);
 			throw new GeoserverException("File not found", e);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return result;
 	}
 
 	@Override
 	public boolean publishWorldImage(String workspaceName, String layerName,
 			File zipFile, String crs) {
 		boolean result = false;
 		GeoServerRESTPublisher gsPublisher;
 
 		layerName = GeoserverUtils.createName(layerName);
 
 		try {
 			gsPublisher = getPublisher();
 			NameValuePair paramName = new NameValuePair("coverageName",
 					layerName);
 			result = gsPublisher.publishWorldImage(workspaceName, layerName,
 					zipFile, ParameterConfigure.FIRST, paramName);
 
 			// If coverage has been created configure the layer with the SRS
 			// passed.
 			if (result) {
 				result = configureCoverage(workspaceName, layerName, crs,
 						gsPublisher);
 			}
 		} catch (FileNotFoundException e) {
 			LOG.error("File not found", e);
 			throw new GeoserverException("File not found", e);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param workspaceName
 	 * @param storeName
 	 * @param crs
 	 * @param result
 	 * @param gsPublisher
 	 * @return
 	 * @throws MalformedURLException
 	 */
 	private boolean configureCoverage(String workspaceName, String storeName,
 			String crs, GeoServerRESTPublisher gsPublisher)
 			throws MalformedURLException {
 		GeoServerRESTReader gsReader = getReader();
 		boolean result = false;
 		RESTCoverageList coverageList = gsReader.getCoverages(workspaceName,
 				storeName);
 		for (NameLinkElem coverage : coverageList) {
 			String coverageName = coverage.getName();
 
 			GsCoverageDetails details = getCoverageDetails(workspaceName,
 					storeName, coverageName);
 			GSCoverageEncoder enc = new GSCoverageEncoder();
 			if (details != null) {
 				BoundingBox nativeBoundingBox = details.getNativeBoundingBox();
 				try {
 					CoordinateReferenceSystem nativeCRS = CRS
 							.decode(nativeBoundingBox.getSrs());
 					CoordinateReferenceSystem targetCRS = CRS
 							.decode(GeoserverService.DEFAULT_SRS);
 					MathTransform transform = CRS.findMathTransform(nativeCRS,
 							targetCRS);
 					double[] sourceCoords = new double[4];
 					double[] coordTransformed = new double[4];
 
 					// Fill the array with the bounding box
 					sourceCoords[0] = nativeBoundingBox.getMinx();
 					sourceCoords[1] = nativeBoundingBox.getMiny();
 					sourceCoords[2] = nativeBoundingBox.getMaxx();
 					sourceCoords[3] = nativeBoundingBox.getMaxy();
 					transform
 							.transform(sourceCoords, 0, coordTransformed, 0, 2);
 
 					enc.setLatLonBoundingBox(coordTransformed[0],
 							coordTransformed[1], coordTransformed[2],
 							coordTransformed[3], GeoserverService.DEFAULT_SRS);
 				} catch (NoSuchAuthorityCodeException e) {
 					LOG.error(
 							"No se ha encontrado la autoridad especificada en el Sistema de Referencia Nativo",
 							e);
 				} catch (FactoryException e) {
 					LOG.error(
 							"No se ha podido crear la factoría de SRS en GeoserverServiceImpl",
 							e);
 				} catch (TransformException e) {
 					LOG.error(
 							"Error transformando las coordenadas del nativo al delcarado. Se usará como declarado el mismo que el nativo",
 							e);
 				}
 			}
 
 			enc.setName(coverageName);
 			enc.setSRS(crs);
 			enc.setNativeCRS(crs);
 			enc.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
 
 			result = gsPublisher.configureCoverage(enc, workspaceName,
 					storeName);
 
 			if (!result) {
 				break;
 			}
 
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#getCoverageStoreData()
 	 */
 	@Override
 	public GsCoverageStoreData getCoverageStoreData(String workspaceName,
 			String coverageStoreName) {
 
 		// We cant' use GeoServerRESTReader's getCoverageStore because
 		// it doesn't returns the stores file url and we need it.
 		String url = String.format(GET_COVERAGE_STORE_DATA_URL, workspaceName,
 				coverageStoreName);
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("### Retrieving CS from " + url);
 		}
 		return GsCoverageStoreData.build(load(url));
 	}
 
 	private String load(String url) {
 		LOG.info("Loading from REST path " + url);
 		String baseurl = this.gsConfiguration.getServerUrl();
 		String username = this.gsConfiguration.getAdminUsername();
 		String password = this.gsConfiguration.getAdminPassword();
 		try {
 			String response = HTTPUtils.get(baseurl + url, username, password);
 			return response;
 		} catch (MalformedURLException ex) {
 			LOG.warn("Bad URL", ex);
 		}
 
 		return null;
 	}
 
 	private String put(String url, String content) {
 		LOG.info("Loading from REST path " + url);
 		String baseurl = this.gsConfiguration.getServerUrl();
 		String username = this.gsConfiguration.getAdminUsername();
 		String password = this.gsConfiguration.getAdminPassword();
 
 		String response = HTTPUtils.put(baseurl + url, content, "text/xml",
 				username, password);
 		return response;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#deleteGsCoverageStore()
 	 */
 	@Override
 	public boolean deleteGsCoverageStore(String workspaceName,
 			String coverageStoreName) {
 		GeoServerRESTPublisher publisher;
 		try {
 			publisher = this.getPublisher();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return publisher.removeCoverageStore(workspaceName, coverageStoreName,
 				true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#getCoverageDetails()
 	 */
 	@Override
 	public GsCoverageDetails getCoverageDetails(String workspaceName,
 			String coverageStoreName, String coverageName) {
 		// We cant' use GeoServerRESTReader's getCoverageStore because
 		// it doesn't returns the stores file url and we need it.
 		String url = String.format(GET_COVERAGE_DETAILS_URL, workspaceName,
 				coverageStoreName, coverageName);
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("### Retrieving CS from " + url);
 		}
 		return GsCoverageDetails.build(load(url));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#getLayerStyle()
 	 */
 	@Override
 	public String getLayerStyle(String layerName) {
 		GeoServerRESTReader reader;
 		try {
 			reader = this.getReader();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		RESTLayer layer = reader.getLayer(layerName);
                if(layer==null) {
                    return null;
                }
 		return reader.getSLD(layer.getDefaultStyle());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#createStyle()
 	 */
 	@Override
 	public boolean createStyle(String newStyleName, String layerSDLContent) {
 		GeoServerRESTPublisher publisher;
 		try {
 			publisher = this.getPublisher();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		// The name is sanitized.
 		String newStyleNameSanitized = GeoserverUtils.createName(newStyleName);
 		return publisher.publishStyle(layerSDLContent, newStyleNameSanitized);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#setLayerStyle()
 	 */
 	@Override
 	public boolean setLayerStyle(String workspaceName, String layerName,
 			String newLayerStyleName) {
 
 		String url = String.format(SET_LAYER_STYLE_URL, workspaceName,
 				layerName);
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("### Setting layer style using " + url);
 		}
 
 		// The name is sanitized.
 		String newStyleNameSanitized = GeoserverUtils
 				.createName(newLayerStyleName);
 		String payload = String.format(SET_LAYER_STYLE_PAYLOAD,
 				newStyleNameSanitized);
 
 		return this.put(url, payload) != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.dao.GeoserverDao#deleteStyle()
 	 */
 	public boolean deleteStyle(String styleName) {
 		GeoServerRESTPublisher publisher;
 		try {
 			publisher = this.getPublisher();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return publisher.removeStyle(styleName, true);
 	}
 
 	@Override
 	public boolean reset() {
 		GeoServerRESTPublisher publisher;
 		try {
 			publisher = this.getPublisher();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 
 		return publisher.reset();
 	}
 
 	/**
 	 * Retrieves a layer's datastore associated with the layer.
 	 * 
 	 * @param layerName
 	 * 
 	 * @return
 	 */
 	public RESTDataStore getDatastore(String layerName) {
 		GeoServerRESTReader reader;
 		try {
 			reader = getReader();
 			return getReader().getDatastore(
 					reader.getFeatureType(reader.getLayer(layerName)));
 
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		} catch (NullPointerException np) {
 			LOG.error("Incorrect Geoserver layer '" + layerName + "'", np);
 			throw new GeoserverException("Incorrect Geoserver layer '"
 					+ layerName + "'", np);
 		}
 	}
 	
 	/**
 	 * Retrieves all layers' name in geoserver
 	 * 
 	 * @return
 	 */
 	public List<String> getLayersNames(){
 		try {
 			return getReader().getLayers().getNames();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 	}
 	
 
 	/**
 	 * Retrieves the geoserver url configured.
 	 * 
 	 * @return baseUrl to geoserver
 	 */
 	public String getGeoserverUrl() {
 		return getGsConfiguration().getServerUrl();
 	}
 
 	/**
 	 * Obtain native name of a layerName 
 	 * 
 	 * @param layerName
 	 * 
 	 * @return native name of the layer 
 	 */
 	public String getNativeName(String layerName){
 		GeoServerRESTReader reader;
 		try {
 			reader = getReader();
 			return reader.getFeatureType(reader.getLayer(layerName)).getNativeName();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		} catch (NullPointerException np) {
 			LOG.error("Incorrect Geoserver layer '" + layerName + "'", np);
 			throw new GeoserverException("Incorrect Geoserver layer '"
 					+ layerName + "'", np);
 		}
 	}
 	
 	/**
 	 * Retrieves all styles' names in geoserver
 	 * 
 	 * @return styles' names
 	 */
 	public List<String> getStyleNames(){
 		try {
 			return getReader().getStyles().getNames();
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 	}
 	
 	private String [] DEAFAULT_STYLES = {"line", "polygon", "point"};
 	
 	/**
 	 * Retrieves all styles' names in geoserver
 	 * 
 	 * @return styles' names removed
 	 */
 	public List<String> cleanUnusedStyles(){
 		return cleanUnusedStyles(getStyleNames());
 	}
 	
 	/**
 	 * Clean styles unused in style list 
 	 * 
 	 * @param styleNames name of styles to be deleted
 	 * 
 	 * @return list of deleted styles
 	 */
 	public List<String> cleanUnusedStyles(List<String> styleNames){
 		GeoServerRESTReader reader;
 		GeoServerRESTManager manager;
 		GeoServerRESTPublisher publisher;
 		List<String> removedStyles = new LinkedList<String>();
 		try {
 			manager = new GeoServerRESTManager(new URL(
 					gsConfiguration.getServerUrl()),
 					gsConfiguration.getAdminUsername(),
 					gsConfiguration.getAdminPassword());
 			reader = manager.getReader();
 			publisher = manager.getPublisher();
 			for(String style: styleNames){
 				if(canRemove(style, reader)
 						&& removeStyle(style, publisher)){
 					removedStyles.add(style);
 				}
 			}
 			return removedStyles;
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 	}
 
 	private boolean removeStyle(String style, GeoServerRESTPublisher publisher) {
 		return publisher.removeStyle(style);
 	}
 
 	private boolean canRemove(String style, GeoServerRESTReader reader) {
 		// we can't remove default styles
 		for(String defaultStyle: DEAFAULT_STYLES){
 			if(defaultStyle.equals(style)){
 				return false;
 			}
 		}
 		// we can't remove used styles
 		for(String layerName: reader.getLayers().getNames()){
 			RESTLayer layer = reader.getLayer(layerName);
 			if(style.equals(layer.getDefaultStyle())){
 				return false;
 			}
 		}
 		return true;
 	}
 }
