 /**
  * Copyright 2011 Jason Ferguson.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.server.service;
 
 import com.vividsolutions.jts.geom.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.jason.mapmaker.server.repository.FeatureRepository;
 import org.jason.mapmaker.server.util.ZipUtil;
 import org.jason.mapmaker.shared.exceptions.RepositoryException;
 import org.jason.mapmaker.shared.exceptions.ServiceException;
 import org.jason.mapmaker.shared.model.BorderPoint;
 import org.jason.mapmaker.shared.model.Feature;
 import org.jason.mapmaker.shared.model.FeaturesMetadata;
 import org.jason.mapmaker.shared.model.Location;
 import org.jason.mapmaker.shared.util.FeatureUtil;
 import org.jason.mapmaker.shared.util.GeographyUtils;
 import org.opengis.filter.FilterFactory2;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static org.apache.commons.lang.StringUtils.split;
 
 /**
  * Implementation of FeaturesService interface
  *
  * @since 0.3
  * @author Jason Ferguson
  */
 @Service("featureService")
 public class FeatureServiceImpl implements FeatureService {
 
     private static Logger log = LoggerFactory.getLogger(FeatureServiceImpl.class);
 
     private FeaturesMetadataService featuresMetadataService;
     private FeatureRepository featureRepository;
 
     @Autowired
     public void setFeaturesMetadataService(FeaturesMetadataService featuresMetadataService) {
         this.featuresMetadataService = featuresMetadataService;
     }
 
     @Autowired
     public void setFeatureRepository(FeatureRepository featureRepository) {
         this.featureRepository = featureRepository;
     }
 
     @Override
     public void persist(Feature object) throws ServiceException {
 
         try {
             featureRepository.save(object);
         } catch (Exception e) {
             log.debug("persist() threw RepositoryException: ", e);
             throw new ServiceException(e);
         }
     }
 
     @Override
     public void remove(Feature object) throws ServiceException {
 
         try {
             featureRepository.delete(object);
         } catch (Exception e) {
             log.debug("remove() threw RepositoryException: ", e);
             throw new ServiceException(e);
         }
     }
 
     @Override
     public void saveList(List<Feature> featureList) throws ServiceException {
 
         try {
             featureRepository.saveList(featureList);
         } catch (Exception ex) {
             log.debug("saveList() threw ServiceException", ex);
             throw new ServiceException(ex);
         }
     }
 
     @Override
     public List<String> getFeatureClasses() {
         return featureRepository.getFeatureClasses();
     }
 
     @Override
     public List<Feature> getFeatures(Location location, String featureClassName) {
 
         // get everything of the given feature class within the general bounding box of the location
         List<Feature> initialList = featureRepository.getFeaturesByBoxAndFeatureClassName(location.getBoundingBox(), featureClassName);
 
         // create a polygon of the border points
         int numBorderPoints = location.getBorderPointList().size();
         Coordinate[] coordinates = new Coordinate[numBorderPoints + 1];
         for (int i = 0; i < numBorderPoints; i++) {
             BorderPoint bp = location.getBorderPointList().get(i);
             coordinates[i] = new Coordinate(bp.getLng(), bp.getLat());
         }
 
         // close the polygon
         coordinates[numBorderPoints] = coordinates[0];
 
         GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 
         // create the linear ring of the outer border. However, the outer border is the only border, so pass null
         // for the second argument of createPolygon()
         LinearRing lr = geometryFactory.createLinearRing(coordinates);
         Polygon polygon = geometryFactory.createPolygon(lr, null);
 
         // check if point is in Polygon, if not throw it out of the feature list
         List<Feature> filteredList = new ArrayList<Feature>();
         for (Feature f : initialList) {
             // we can only check if a given Geometry is inside a polygon, so create a Point from the coordinates
             Point point = geometryFactory.createPoint(new Coordinate(f.getLng(), f.getLat()));
             if (polygon.contains(point)) {
                 filteredList.add(f);
             }
         }
 
         return filteredList;
     }
 
     @Override
     public Map<String, Long> getFeatureCounts() {
         return featureRepository.getFeatureCounts();
     }
 
     public void deleteByFeaturesMetadata(FeaturesMetadata fm) throws ServiceException {
 
         String validatedGeoId = StringUtils.left(fm.getStateGeoId(), 2);
         try {
             featureRepository.deleteByStateGeoId(validatedGeoId);
         } catch (RepositoryException e) {
             throw new ServiceException(e);
         }
 
         fm.setCurrentStatus(GeographyUtils.Status.NOT_IMPORTED);
         featuresMetadataService.update(fm);
 
     }
 
     @Override
     public void importFromFeaturesMetadata(FeaturesMetadata fm) throws ServiceException {
 
         String url = generateUrl(fm.getStateGeoId(), fm.getUsgsDate());
         importFromUrl(url, fm);
 
         fm.setCurrentStatus(GeographyUtils.Status.IMPORTED);
         featuresMetadataService.update(fm);
     }
 
     @Override
     public String generateUrl(String geoId, String dateUpdated) {
 
         String abbreviation = GeographyUtils.getAbbreviationForState(GeographyUtils.getStateForGeoId(geoId));
         String result = "http://geonames.usgs.gov/docs/stategaz/" + abbreviation + "_Features_" + dateUpdated + ".zip";
 
         return result;
     }
 
     @Override
     public void importFromUrl(String url, FeaturesMetadata fm) throws ServiceException {
 
        URL u = null;
         try {
             u = new URL(url);
         } catch (MalformedURLException ex) {
             log.debug("Exception thrown:", ex.getMessage());
             throw new ServiceException(ex);
         }
 
         List<File> fileList;
         try {
             fileList = ZipUtil.decompress(u);
         } catch (Exception e) {
             log.debug("Exception thrown:", e.getMessage());
             throw new ServiceException(e);
         }
 
         if (fileList == null || fileList.size() == 0) {
             log.debug("File list is null or zero!");
             throw new ServiceException("File List contains no files!");
         }
 
         File file = fileList.get(0); // get the txt file handler
         try {
 
             BufferedReader reader = new BufferedReader(new FileReader(file));
             reader.readLine(); // ignore header line
 
             String line = reader.readLine();
             List<Feature> featureList = new ArrayList<Feature>(); // arraylist may not be the best choice since I don't know how many features I'm importing
             int counter = 1;
             while (line != null) {
                 String[] splitLine = split(line, "|"); // static import of StringUtils because I don't like regex's
                 if (!NumberUtils.isNumber(splitLine[0]) || !NumberUtils.isNumber(splitLine[9]) || !NumberUtils.isNumber(splitLine[10])) {
                     System.out.println("Feature ID#" + splitLine[0] + " fails isNumeric() test. Skipping.");
                     line = reader.readLine();
                     continue; // "silently" die
                 }
 
                 // only import the manmade features
                 if (FeatureUtil.isManmadeFeature(splitLine[2])) {
                     // setting this to variables and using the non-default Feature constructor means this is
                     // easier to debug. Yay.
                     int id = Integer.parseInt(splitLine[0]);
                     String featureName = StringUtils.left(splitLine[1], 99);
                     String featureClass = StringUtils.left(splitLine[2], 99);
                     double lat = Double.parseDouble(splitLine[9]);
                     double lng = Double.parseDouble(splitLine[10]);
 
                     Feature feature = new Feature(id, featureName, featureClass, lat, lng, fm);
                     feature.setFeatureSource("usgs");
 
                     featureList.add(feature);
 
                     counter++;
                 }
 
                 if (counter % 100000 == 0) {
                     System.out.println("Processed " + counter + " items");
                     try {
                         saveList(featureList);
                         featureList.clear();
                     } catch (ServiceException e) {
                         log.debug("Exception thrown: ", e.getMessage());
                         break;
                     }
                 }
                 line = reader.readLine();
             }
 
             saveList(featureList);
 
             reader.close();
 
 
         } catch (IOException e) {
             log.debug("Exception thrown:", e);
             throw new ServiceException(e);
         }
 
         file.delete();
     }
 
     @Override
     public void update(Feature obj) {
         featureRepository.update(obj);
     }
 
     @Override
     public void deleteAll() {
         featureRepository.deleteAll();
     }
 }
