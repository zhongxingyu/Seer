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
 import it.geosolutions.geoserver.rest.GeoServerRESTReader;
 import it.geosolutions.geoserver.rest.HTTPUtils;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.CoverageStoreExtension;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.DataStoreExtension;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.DataStoreType;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.ParameterConfigure;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.StoreType;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.UploadMethod;
 import it.geosolutions.geoserver.rest.decoder.RESTWorkspaceList;
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
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.emergya.persistenceGeo.dao.GeoserverDao;
 import com.emergya.persistenceGeo.exceptions.GeoserverException;
 import com.emergya.persistenceGeo.utils.BoundingBox;
 import com.emergya.persistenceGeo.utils.GSFeatureTypeNativeNameEncoder;
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
 		try {
 			GeoServerRESTPublisher publisher = getPublisher();
 			result = publisher.createWorkspace(name);
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
 		try {
 			publisher = getPublisher();
 			result = publisher.createNamespace(prefix, uri);
 
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
 		try {
 			dsManager = getDatastoreManager();
 			GSPostGISDatastoreEncoder properties = new GSPostGISDatastoreEncoder(
 					datastoreName);
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
 		try {
 			dsManager = getDatastoreManager();
 			GSPostGISDatastoreEncoder properties = new GSPostGISDatastoreEncoder(
 					datastoreName);
 
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
 	public boolean deletePostgisFeatureTye(String workspaceName,
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
 	public boolean publishGeoTIFF(String workspace, String storeName,
 			File geotiff, String crs) {
 		GeoServerRESTPublisher gsPublisher;
 		boolean result = false;
 		try {
 			gsPublisher = getPublisher();
 			//result = gsPublisher.publishGeoTIFF(workspace, storeName, geotiff);
 			result =gsPublisher.publishGeoTIFF(workspace, storeName, storeName, geotiff,
					crs, ProjectionPolicy.FORCE_DECLARED,
					DEFAULT_RASTER_STYLE, null);
 		} catch (FileNotFoundException e) {
 			LOG.error("File not found", e);
 			throw new GeoserverException("File not found", e);
 		} catch (MalformedURLException e) {
 			LOG.error("Malformed Geoserver REST API URL", e);
 			throw new GeoserverException("Malformed Geoserver REST API URL", e);
 		}
 		return result;
 	}
 
 }
