 package com.nasageeks.upload;
 
 import com.nasageeks.filefilters.geotiffFileFilter;
 import com.nasageeks.filefilters.zipFileFilter;
 import com.nasageeks.restmanager.GeoserverNASARestManager;
 import com.nasageeks.viewer.ViewerManager;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Automatic application that upload NASA geotiff files into a Geoserver website
  *
  */
 public class App {
 
 	public static void main(String[] args) {
 
 		String RESTUSER = "admin";
 		String RESTPW = "sopasperico";
 		FileInputStream file = null;
 
 		try {
 			Properties props = new Properties();
 			file = new FileInputStream("./Params.properties");
 			props.load(file);
 
 			// write 
 			String workspace = props.getProperty("workspace");
 			String server = props.getProperty("server");
 			String filePath = props.getProperty("filePath");
 			String basestore = props.getProperty("store");
 			String layersPath = props.getProperty("layers");
 			System.out.println("Layers ---- "+layersPath);
 			System.out.println("Files ---- "+filePath);
 
 			GeoserverNASARestManager restMan = new GeoserverNASARestManager(server, RESTUSER, RESTPW);
 
 //			restMan.createWorkspace(workspace);
 
 			String layersOutputFile = layersPath + "NewLayers.xml";
 			String layersInputFile = layersPath + "Default/Layers.xml";
 
 			String store;
 
 			String[] mosaicFiles = FileManager.filesInFolder(filePath, new zipFileFilter());
 
 			//Iterates over every file
 			basestore = basestore+ "_pyramid";
 
 			System.out.println("---- Reading Pyramid (zip files)....");
 			File pyramid = null;
 			for (int fileNumber = 0; fileNumber < mosaicFiles.length; fileNumber++) {
 				try{
 					pyramid = new File(mosaicFiles[fileNumber]);
 					store = basestore+ "_" + fileNumber;//Incremental name for store
 
 					//Publishes the geotiff file
 					BoundaryBox bbox = restMan.publishMosaic(pyramid, workspace, store);
 
 
 					//Updates the XML of the map viewer.
 					System.out.println("---- Adding new layer into the Map Visualizer");
					ViewerManager.addNewLayer(FileManager.removeExt(pyramid.getName()), server+"/wms", workspace, bbox, layersInputFile, layersOutputFile);
 
 					//Changes the input file as the output file after the first layer is updated
 					if (fileNumber == 0) {
 						layersInputFile = layersOutputFile;
 					}
 
 				} catch (Exception ex) {
 					System.out.println("ERROR!!!! Fail to upload PYRAMID file "+pyramid.getName() + " EX:"+ex.getMessage());
 				}
 			}
 
 			String[] geotTiffFiles = FileManager.filesInFolder(filePath, new geotiffFileFilter());
 			
 			basestore = props.getProperty("store");
 			basestore = basestore+ "_tiff";
 
 			//Iterates over every file
 			System.out.println("---- Reading Geotiff files....");
 			File geotiff = null;
 			for (int fileNumber = 0; fileNumber < geotTiffFiles.length; fileNumber++) {
 				try{
 					geotiff = new File(geotTiffFiles[fileNumber]);
 
 					store = basestore+ "_" + fileNumber;
 
 					//Publishes the geotiff file
 					BoundaryBox bbox = restMan.publishGeoTiff(geotiff, workspace);
 
 					//Updates the XML of the map viewer.
					ViewerManager.addNewLayer(geotiff.getName(), server+"/wms", workspace, bbox, layersInputFile, layersOutputFile);
 
 					//Changes the input file as the output file after the first layer is updated
 					if (fileNumber == 0) {
 						layersInputFile = layersOutputFile;
 					}
 				} catch (Exception ex) {
 					System.out.println("ERROR!!!! Fail to upload GeoTIFF file "+geotiff.getName() + " EX:"+ex.getMessage());
 				}
			}
 
 		} catch (Exception ex) {
 			System.out.println("ERROR!!!! Exception: "+ex.getMessage());
 		} finally {
 			try {
 				file.close();
 			} catch (IOException ex) {
 				System.out.println("ERROR!!!! IOException: "+ex.getMessage());
 			}
 		}
 	}
 }
