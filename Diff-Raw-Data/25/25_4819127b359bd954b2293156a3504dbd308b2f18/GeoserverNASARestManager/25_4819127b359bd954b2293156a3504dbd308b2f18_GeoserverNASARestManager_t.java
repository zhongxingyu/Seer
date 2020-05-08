 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nasageeks.restmanager;
 
 import com.nasageeks.upload.BoundaryBox;
 import com.nasageeks.upload.FileManager;
 import gov.nasa.worldwind.formats.tiff.GeoCodec;
 import gov.nasa.worldwind.formats.tiff.GeotiffReader;
 import it.geosolutions.geoserver.rest.*;
 import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
 import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
 import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 /**
  * This file should contain the functions related with the upload of geotiff files into
  * geoserver
  *
  * @author olmozavala
  */
 public class GeoserverNASARestManager {
 
 	GeoServerRESTReader readerGeoserver;
 	GeoServerRESTPublisher publisherGeoserver;
 	String server;
 
 	/**
 	 * It initilizes the manager with the Geoserver credentials information
 	 *
 	 * @param server
 	 * @param user
 	 * @param psw
 	 * @throws MalformedURLException
 	 */
 	public GeoserverNASARestManager(String server, String user, String psw) throws MalformedURLException {
 		readerGeoserver = new GeoServerRESTReader(server, user, psw);
 		publisherGeoserver = new GeoServerRESTPublisher(server, user, psw);
 		this.server = server;
 	}
 
 	/**
 	 * Creates a new workspace in Geoserver
 	 *
 	 * @param workspace
 	 */
 	public void createWorkspace(String workspace) {
 		//Adding the nasa workspace into geoserver (only once should be done)
 		boolean created = publisherGeoserver.createWorkspace(workspace);
 		if (created) {
 			System.out.println("**** The workspace " + workspace + " has been created, yeah!");
 		} else {
 			System.out.println("++++ WARN The workspace " + workspace + " has NOT been created.");
 		}
 	}
 
 	/**
 	 * Publishes a geotiff layer into Geoserver
 	 *
 	 * @param geoTiffFile {File} File of the Geotiff
 	 * @param workspace {String} Workspace to add the layer
 	 * @param store {String} Corresponding store for the new layer
 	 * @return
 	 * @throws IOException
 	 */
 	public BoundaryBox publishGeoTiff(File geoTiffFile, String workspace) throws IOException {
 
 		String fileName = geoTiffFile.getName();
 		System.out.println("FileName: " + fileName);
 		String coverageName = fileName;
 
 		String srs = "EPSG:4326";
 		String def = "default";
 
 		ProjectionPolicy proj = ProjectionPolicy.REPROJECT_TO_DECLARED;
 
 
 		//Reads the Geotiff file and extracts the boundaries
 		GeotiffReader reader = new GeotiffReader(geoTiffFile.getAbsoluteFile());
 		boolean created = false;
 
 		if (reader.isGeotiff()) {
 			GeoCodec geo = reader.getGeoCodec();
 
 			//now get bounding box coordinates for entire image
 			@SuppressWarnings("MismatchedReadAndWriteOfArray")
 			double[] bbox = geo.getBoundingBox(reader.getWidth(0), reader.getHeight(0));
 			BoundaryBox BBOX = new BoundaryBox(bbox[0], bbox[2], bbox[3], bbox[1]);
 
 			//---- Creates one new store for file
 
 			System.out.println("\n---- Pusblishing Geotiff : " + fileName + " with BBOX: " + BBOX.toString());
			try {
				created = publisherGeoserver.publishGeoTIFF(workspace, coverageName, geoTiffFile);
			} catch (Exception ex) {
				created = false;
			}

 			if (created) {
 				System.out.println("**** Layer " + workspace + ":" + coverageName + " added SUCCESSFULLY!");
 			} else {
 				System.out.println("++++ WARNING Adding layer " + workspace + ":" + coverageName + " (it may already exist in Geoserver)");
 			}
 
 			//--------- Read the newly uploaded layer and change its Meta data and boundary box
 			RESTCoverage coverage = readerGeoserver.getCoverage(workspace, coverageName, coverageName);
 
 			GSCoverageEncoder layerEncoder = new GSCoverageEncoder();
 			layerEncoder.setNativeBoundingBox(bbox[0], bbox[3], bbox[2], bbox[1], srs);
 			layerEncoder.setName(coverageName);
 			boolean success = publisherGeoserver.configureCoverage(layerEncoder, workspace, coverageName);
 			if (success) {
 				System.out.println("**** BBOX has been updated for " + coverageName);
 			} else {
 				System.out.println("++++ WARN BBOX FAIL to be updated for " + coverageName);
 			}//if
 
 			return BBOX;
 		}//If reader geotiff
 		return null;
 	}
 
 	/**
 	 * Publishes a new mosaic or pyramid into geoserver.
 	 *
 	 * @param mosaicFile {File} File containing a zip file of the mosaic or pyramid
 	 * @param workspace {String} Workspace to be used
 	 * @param store {String} Corresponding store for the new mosaic/pyramid
 	 * @return
 	 * @throws IOException
 	 */
 	public BoundaryBox publishMosaic(File mosaicFile, String workspace, String store) throws IOException {
 
 		String fileName = mosaicFile.getName();
 		String coverageName = fileName;
 
 		//Adding the nasa workspace into geoserver (only once should be done)
 		boolean created = false;
 
 		//---- Creates one new store for file
 		System.out.println("\n---- Pusblishing pyramid: " + mosaicFile.getName() + " in store:" + store + " workspace: " + workspace);
 
 		try {
 			created = publisherGeoserver.publishImageMosaic(workspace, store, mosaicFile);
		} catch (Exception ex) {
 			created = false;
 		}
 
 		if (created) {
 			System.out.println("**** Layer " + workspace + ":" + coverageName + " added SUCCESSFULLY!");
 		} else {
 			System.out.println("++++ WARN adding layer " + workspace + ":" + coverageName + " (it may already exist in Geoserver)");
 		}
 
 		//--------- Read the newly uploaded layer and change its Meta data and boundary box
 		RESTCoverage cov = readerGeoserver.getCoverage(workspace, store, FileManager.removeExt(fileName));
 		BoundaryBox bbox = new BoundaryBox(cov.getMinX(), cov.getMaxX(), cov.getMinY(), cov.getMaxY());
 
 		return bbox;
 	}
 }
