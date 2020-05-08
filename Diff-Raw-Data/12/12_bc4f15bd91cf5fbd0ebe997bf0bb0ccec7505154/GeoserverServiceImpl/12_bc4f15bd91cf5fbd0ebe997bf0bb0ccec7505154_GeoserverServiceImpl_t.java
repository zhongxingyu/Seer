 /* GeoserverServiceImpl.java
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
 package com.emergya.persistenceGeo.service.impl;
 
 import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
 import it.geosolutions.geoserver.rest.decoder.RESTWorkspaceList;
 import it.geosolutions.geoserver.rest.decoder.RESTWorkspaceList.RESTShortWorkspace;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.emergya.persistenceGeo.dao.GeoserverDao;
 import com.emergya.persistenceGeo.exceptions.GeoserverException;
 import com.emergya.persistenceGeo.service.GeoserverService;
 import com.emergya.persistenceGeo.utils.BoundingBox;
 import com.emergya.persistenceGeo.utils.GsCoverageDetails;
 import com.emergya.persistenceGeo.utils.GsCoverageStoreData;
 import com.emergya.persistenceGeo.utils.GsFeatureDescriptor;
 import com.emergya.persistenceGeo.utils.GsLayerDescriptor;
 import com.emergya.persistenceGeo.utils.GsLayerDescriptor.GeometryType;
 
 /**
  * @author <a href="mailto:jlrodriguez@emergya.com">jlrodriguez</a>
  * 
  */
 public class GeoserverServiceImpl implements GeoserverService {
 	private final static Log LOG = LogFactory
 			.getLog(GeoserverServiceImpl.class);
 	private final static String DATASTORE_SUFFIX = "_datastore";
 
 	@Resource
 	private GeoserverDao gsDao;
 
 	/**
 	 * @return the gsDao
 	 */
 	public GeoserverDao getGsDao() {
 		return gsDao;
 	}
 
 	/**
 	 * @param gsDao
 	 *            the gsDao to set
 	 */
 	public void setGsDao(GeoserverDao gsDao) {
 		this.gsDao = gsDao;
 	}
 
 	/**
 	 * @return the namespaceBaseUrl
 	 */
 	public String getNamespaceBaseUrl() {
 		return namespaceBaseUrl;
 	}
 
 	/**
 	 * @param namespaceBaseUrl
 	 *            the namespaceBaseUrl to set
 	 */
 	public void setNamespaceBaseUrl(String namespaceBaseUrl) {
 		this.namespaceBaseUrl = namespaceBaseUrl;
 	}
 
 	@Resource
 	private String namespaceBaseUrl;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.service.GeoserverService#
 	 * createGsWorkspaceWithDatastore(java.lang.String)
 	 */
 	@Override
 	public boolean createGsWorkspaceWithDatastore(String workspaceName) {
 		if (LOG.isInfoEnabled()) {
 			LOG.info("Creating Geoserver workspace [workspaceName="
 					+ workspaceName + "]");
 		}
 		boolean result = false;
 		String nsUrl;
 		if (this.namespaceBaseUrl.endsWith("/")) {
 			nsUrl = this.namespaceBaseUrl + workspaceName;
 		} else {
 			nsUrl = this.namespaceBaseUrl + "/" + workspaceName;
 		}
 		URI uri;
 		try {
 			uri = new URI(nsUrl);
 			result = gsDao.createNamespace(workspaceName, uri);
 
 			if (!result) {
 				// Can't create Namespace. Stop here.
 				if (LOG.isInfoEnabled()) {
 					LOG.info("Couln't create the namespace and his asocciated "
 							+ "workspace [workspaceName=" + workspaceName + "]");
 				}
 				return result;
 			}
 			String datastoreName = workspaceName + DATASTORE_SUFFIX;
 			result = gsDao.createDatastoreJndi(workspaceName, datastoreName);
 			if (!result) {
 				// Can't create Datastore. Try to delete workspace.
 				if (LOG.isInfoEnabled()) {
 					LOG.info("Couln't create the datastore " + datastoreName
 							+ " in workspace " + workspaceName
 							+ ". Trying to delete workspace...");
 				}
 				boolean deleteResult = gsDao.deleteWorkspace(workspaceName);
 
 				if (LOG.isInfoEnabled()) {
 					if (deleteResult) {
 						LOG.info("Workspace " + workspaceName
 								+ " successfully deleted");
 					} else {
 						LOG.warn("Couldn't delete workspace " + workspaceName);
 					}
 				}
 			}
 
 		} catch (URISyntaxException e) {
 			throw new GeoserverException("Illegal URL syntax detected when"
 					+ "preparing for creating a geoserver workspace [URI="
 					+ nsUrl + "]", e);
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.service.GeoserverService#deleteGsWorkspace
 	 * (java.lang.String)
 	 */
 	@Override
 	public boolean deleteGsWorkspace(String workspaceName) {
 		if (LOG.isInfoEnabled()) {
 			LOG.info("Deleting Geoserver workspace [workspaceName="
 					+ workspaceName + "]");
 		}
 		return gsDao.deleteWorkspace(workspaceName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.service.GeoserverService#publishGsDbLayer()
 	 */
 	@Override
 	public boolean publishGsDbLayer(String workspaceName, String tableName,
 			String layerName, String title, BoundingBox nativeBoundingBox,
 			GeometryType geomType) {
 		if (LOG.isInfoEnabled()) {
 			LOG.info("Publising geoserver database layer [workspaceName="
 					+ workspaceName + ", tableName=" + tableName
 					+ ", layerName=" + layerName + ", geometryType=" + geomType
 					+ "]");
 		}
 		boolean result = false;
 
 		// Transform native bounding box to EPSG:4326
 		String nativeSrs = nativeBoundingBox.getSrs();
 		BoundingBox declaredBBox = new BoundingBox();
 		declaredBBox.setSrs(DEFAULT_SRS);
 		boolean declaredSrsTransformed = false;
 		try {
 			CoordinateReferenceSystem nativeCRS = CRS.decode(nativeSrs);
 			CoordinateReferenceSystem targetCRS = CRS.decode(DEFAULT_SRS);
 			MathTransform transform = CRS.findMathTransform(nativeCRS,
 					targetCRS);
 			double[] sourceCoords = new double[4];
 			double[] coordTransformed = new double[4];
 
 			// Fill the array with the bounding box
 			sourceCoords[0] = nativeBoundingBox.getMinx();
 			sourceCoords[1] = nativeBoundingBox.getMiny();
 			sourceCoords[2] = nativeBoundingBox.getMaxx();
 			sourceCoords[3] = nativeBoundingBox.getMaxy();
 			transform.transform(sourceCoords, 0, coordTransformed, 0, 2);
 
 			declaredBBox.setMinx(coordTransformed[0]);
 			declaredBBox.setMiny(coordTransformed[1]);
 			declaredBBox.setMaxx(coordTransformed[2]);
 			declaredBBox.setMaxy(coordTransformed[3]);
 			declaredSrsTransformed = true;
 
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
 
 		GsFeatureDescriptor fd = new GsFeatureDescriptor();
 		fd.setNativeName(tableName);
 		fd.setTitle(title);
 		fd.setName(layerName);
 		if (nativeBoundingBox != null) {
 			if (declaredSrsTransformed) {
 				fd.setLatLonBoundingBox(declaredBBox);
 
 				// this is not an error. You should assign the native SRS to the
 				// declared SRS.
 				fd.setSRS(nativeBoundingBox.getSrs());
 
 			}
 			fd.setNativeCRS(nativeBoundingBox.getSrs());
 			fd.setNativeBoundingBox(nativeBoundingBox);
 		}
 
 		GsLayerDescriptor ld = new GsLayerDescriptor();
 
 		ld.setType(geomType);
 
 		String datastoreName = workspaceName + DATASTORE_SUFFIX;
 		result = gsDao
 				.publishPostgisLayer(workspaceName, datastoreName, fd, ld);
 
 		return result;
 	}
 
 	/**
 	 * Unpublish layer identified by layerName, workspace and workspace
 	 * 
 	 * @param workspaceName
 	 * @param datastoreName
 	 * @param layer
 	 * 
 	 * @return true if can be unpublish and false otherwise
 	 */
 	public boolean unpublishLayer(String workspaceName, String datastoreName,
 			String layerName) {
 		if (LOG.isInfoEnabled()) {
 			LOG.info("Unpublishig geoserver layer");
 		}
 		boolean result = false;
 		result = gsDao.deletePostgisFeatureType(workspaceName, datastoreName,
 				layerName);
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.emergya.persistenceGeo.service.GeoserverService#unpublishGsLayer
 	 * (boolean)
 	 */
 	@Override
 	public boolean unpublishGsDbLayer(String workspaceName, String layerName) {
 		return unpublishLayer(workspaceName, workspaceName + DATASTORE_SUFFIX,
 				layerName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.service.GeoserverService#existsLayerInWorkspace
 	 * (java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean existsLayerInWorkspace(String layerName, String workspaceName) {
 		return gsDao.existsLayerInWorkspace(layerName, workspaceName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.emergya.persistenceGeo.service.GeoserverService#createDatastoreJndi
 	 * (java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean createDatastoreJndi(String workspaceName,
 			String datastoreName) {
 		return gsDao.createDatastoreJndi(workspaceName, datastoreName);
 	}
 
 	@Override
 	public boolean publishGeoTIFF(String workspace, String layerName,
 			File geotiff, String crs) {
 		return gsDao.publishGeoTIFF(workspace, layerName, geotiff, crs);
 	}
 
 	@Override
 	public boolean publishImageMosaic(String workspaceName, String layerName,
 			File imageFile, String crs) {
 		return gsDao.publishImageMosaic(workspaceName, layerName, imageFile,
 				crs);
 	}
 
 	@Override
 	public boolean publishWorldImage(String workspaceName, String layerName,
 			File imageFile, String crs) {
 
 		return gsDao
 				.publishWorldImage(workspaceName, layerName, imageFile, crs);
 	}
 
 	@Override
 	public GsCoverageStoreData getCoverageStoreData(String workspaceName,
 			String coverageStoreName) {
 		return gsDao.getCoverageStoreData(workspaceName, coverageStoreName);
 	}
 
 	@Override
 	public boolean unpublishGsCoverageLayer(String workspaceName,
 			String coverageLayer) {
 
 		if (!gsDao.deleteCoverage(workspaceName, coverageLayer)) {
 			return false;
 		}
 
 		if (!gsDao.deleteGsCoverageStore(workspaceName, coverageLayer)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	@Override
 	public GsCoverageDetails getCoverageDetails(String workspaceName,
 			String coverageStore, String coverageName) {
 		return gsDao.getCoverageDetails(workspaceName, coverageStore,
 				coverageName);
 	}
 
 	@Override
 	public boolean copyLayerStyle(String sourceLayerName, String newStyleName) {
 		String layerSDLContent = gsDao.getLayerStyle(sourceLayerName);
                
                if(layerSDLContent==null) {
                    return false;
                }
 
 		return gsDao.createStyle(newStyleName, layerSDLContent);
 	}
 
 	@Override
 	public boolean setLayerStyle(String workspaceName, String layerName,
 			String newLayerStyleName) {
 
 		return gsDao.setLayerStyle(workspaceName, layerName, newLayerStyleName);
 	}
 
 	@Override
 	public boolean deleteStyle(String styleName) {
 		return gsDao.deleteStyle(styleName);
 	}
 
 	@Override
 	public boolean reset() {
 		return gsDao.reset();
 	}
 
 	public boolean existsWorkspace(String workspaceName) {
 		RESTWorkspaceList workspaceList = gsDao.getWorkspaceList();
 		for (RESTShortWorkspace workspace : workspaceList) {
 			String currentName = workspace.getName();
 			if (StringUtils.equals(workspaceName, currentName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Retrieves a layer's datastore associated with the layer.
 	 * 
 	 * @param layerName
 	 * 
 	 * @return
 	 */
 	public RESTDataStore getDatastore(String layerName) {
 		return getGsDao().getDatastore(layerName);
 	}
 
 	/**
 	 * Retrieves the geoserver url configured.
 	 * 
 	 * @return baseUrl to geoserver
 	 */
 	public String getGeoserverUrl() {
 		return getGsDao().getGeoserverUrl();
 	}
 
 	public void copyLayer(String workspaceName, String datastoreName,
 			String layerName, String tableName, String title, BoundingBox bbox,
 			GeometryType type, String targetWorkspaceName,
 			String targetDatastoreName, String targetLayerName) {
 		publishGsDbLayer(workspaceName, tableName, targetLayerName, title,
 				bbox, type);
 		copyLayerStyle(layerName, targetLayerName);
 		setLayerStyle(workspaceName, targetLayerName, targetLayerName);
 	}
 	
 	/**
 	 * Retrieves all layers' name in geoserver
 	 * 
 	 * @return
 	 */
 	public List<String> getLayersNames(){
 		return gsDao.getLayersNames();
 	}
 
 	/**
 	 * Obtain native name of a layerName 
 	 * 
 	 * @param layerName
 	 * 
 	 * @return native name of the layer 
 	 */
 	public String getNativeName(String layerName){
 		return gsDao.getNativeName(layerName);
 	}
 	
 	/**
 	 * Retrieves all styles' names in geoserver
 	 * 
 	 * @return styles' names
 	 */
 	public List<String> getStyleNames(){
 		return gsDao.getStyleNames();
 	}
 	
 	/**
 	 * Clean styles unused in style list 
 	 * 
 	 * @param styleNames name of styles to be deleted
 	 * 
 	 * @return list of deleted styles
 	 */
 	public List<String> cleanUnusedStyles(List<String> styleNames){
 		return gsDao.cleanUnusedStyles(styleNames);
 	}
 }
