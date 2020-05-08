 /*
  * U.S.Geological Survey Software User Rights Notice
  * 
  * Copied from http://water.usgs.gov/software/help/notice/ on September 7, 2012.  
  * Please check webpage for updates.
  * 
  * Software and related material (data and (or) documentation), contained in or
  * furnished in connection with a software distribution, are made available by the
  * U.S. Geological Survey (USGS) to be used in the public interest and in the 
  * advancement of science. You may, without any fee or cost, use, copy, modify,
  * or distribute this software, and any derivative works thereof, and its supporting
  * documentation, subject to the following restrictions and understandings.
  * 
  * If you distribute copies or modifications of the software and related material,
  * make sure the recipients receive a copy of this notice and receive or can get a
  * copy of the original distribution. If the software and (or) related material
  * are modified and distributed, it must be made clear that the recipients do not
  * have the original and they must be informed of the extent of the modifications.
  * 
  * For example, modified files must include a prominent notice stating the 
  * modifications made, the author of the modifications, and the date the 
  * modifications were made. This restriction is necessary to guard against problems
  * introduced in the software by others, reflecting negatively on the reputation of the USGS.
  * 
  * The software is public property and you therefore have the right to the source code, if desired.
  * 
  * You may charge fees for distribution, warranties, and services provided in connection
  * with the software or derivative works thereof. The name USGS can be used in any
  * advertising or publicity to endorse or promote any products or commercial entity
  * using this software if specific written permission is obtained from the USGS.
  * 
  * The user agrees to appropriately acknowledge the authors and the USGS in publications
  * that result from the use of this software or in products that include this
  * software in whole or in part.
  * 
  * Because the software and related material are free (other than nominal materials
  * and handling fees) and provided "as is," the authors, the USGS, and the 
  * United States Government have made no warranty, express or implied, as to accuracy
  * or completeness and are not obligated to provide the user with any support, consulting,
  * training or assistance of any kind with regard to the use, operation, and performance
  * of this software nor to provide the user with any updates, revisions, new versions or "bug fixes".
  * 
  * The user assumes all risk for any damages whatsoever resulting from loss of use, data,
  * or profits arising in connection with the access, use, quality, or performance of this software.
  */
 
 package gov.usgs.cida.coastalhazards.wps;
 
 import gov.usgs.cida.coastalhazards.util.Constants;
 import gov.usgs.cida.coastalhazards.util.LayerImportUtil;
 import gov.usgs.cida.coastalhazards.wps.exceptions.InputFileFormatException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.ProjectionPolicy;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geoserver.wps.gs.ImportProcess;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @DescribeProcess(
     title = "Create Result Layer From Statistics",
     description = "Clone transect feature collection, append statistics results",
     version = "1.0.0")
 public class CreateResultsLayerProcess implements GeoServerProcess {
 
     private LayerImportUtil importer;
     
     public CreateResultsLayerProcess(ImportProcess importProcess, Catalog catalog) {
         importer = new LayerImportUtil(catalog, importProcess);
     }
     
     @DescribeResult(name = "resultLayer", description = "Layer containing results of shoreline statistics")
     public String execute(@DescribeParameter(name = "results", description = "Block of text with TransectID and stats results", min = 1, max = 1) StringBuffer results,
             @DescribeParameter(name = "transects", description = "Feature collection of transects to clone", min = 1, max = 1) FeatureCollection<SimpleFeatureType, SimpleFeature> transects,
             @DescribeParameter(name = "workspace", description = "Workspace in which to put results layer", min = 1, max = 1) String workspace,
             @DescribeParameter(name = "store", description = "Store in which to put results", min = 1, max = 1) String store,
             @DescribeParameter(name = "layer", description = "Layer name of results", min = 1, max = 1) String layer) throws Exception {
         
         return new Process(results, transects, workspace, store, layer).execute();
     }
     
     protected class Process {
         public static final String TRANSECT_ID = "transect_ID";
         
         private String results;
         private FeatureCollection<SimpleFeatureType, SimpleFeature> transects;
         private String workspace;
         private String store;
         private String layer;
         
         protected Process(StringBuffer results,
                 FeatureCollection<SimpleFeatureType, SimpleFeature> transects,
                 String workspace,
                 String store,
                 String layer) {
             this.results = results.toString();
             this.transects = transects;
             this.workspace = workspace;
             this.store = store;
             this.layer = layer;
         }
         
         protected String execute() {
             importer.checkIfLayerExists(workspace, layer);
             String[] columnHeaders = getColumnHeaders(results);
             Map<Long, Double[]> resultMap = parseTextToMap(results, columnHeaders);
             List<SimpleFeature> joinedFeatures = joinResultsToTransects(columnHeaders, resultMap, transects);
             SimpleFeatureCollection collection = DataUtilities.collection(joinedFeatures);
             String imported = importer.importLayer(collection, workspace, store, layer, null, ProjectionPolicy.REPROJECT_TO_DECLARED);
             return imported;
         }
 
         protected Map<Long, Double[]> parseTextToMap(String results, String[] headers) {
             String[] lines = results.split("\n");
             Map<Long, Double[]> resultMap = new HashMap<Long, Double[]>();
             int transectColumn = -1;
             for (String line : lines) {
                 String[] columns = line.split("\t");
                 if (transectColumn < 0) {
                     // ignore the first line
                     for (int i=0; i<headers.length; i++) {
                         if (headers[i].equals(TRANSECT_ID)) {
                             transectColumn = i;
                         }
                     }
                     if (transectColumn < 0) {
                         throw new InputFileFormatException("Stats did not contain column named " + TRANSECT_ID);
                     }
                 }
                 else {
                     Long transectId = null;
                     Double[] values = new Double[columns.length-1];
                     int j = 0;
                     for (int i=0; i<columns.length; i++) {
                         if (i == transectColumn) {
                             String id = columns[i].replaceAll("\"", "");
                             transectId = Long.parseLong(id);
                         }
                         else {
                             // may need to remove " here too
                             values[j] = Double.parseDouble(columns[i]);
                             j++;
                         }
                     }
                     resultMap.put(transectId, values);
                 }
             }
             return resultMap;
         }
 
         protected List<SimpleFeature> joinResultsToTransects(String[] columnHeaders, Map<Long, Double[]> resultMap, FeatureCollection<SimpleFeatureType, SimpleFeature> transects) {
             List<SimpleFeature> sfList = new LinkedList<SimpleFeature>();
             
             SimpleFeatureType transectFeatureType = transects.getSchema();
             List<AttributeDescriptor> descriptors = transectFeatureType.getAttributeDescriptors();
             SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
             builder.setName("Results");
             builder.addAll(descriptors);
             for (String header: columnHeaders) {
                 if (!header.equals(TRANSECT_ID)) {
                     builder.add(header, Double.class);
                 }
             }
             builder.add("StartX", Integer.class);
             SimpleFeatureType newFeatureType = builder.buildFeatureType();
             FeatureIterator<SimpleFeature> features = transects.features();
             int startx = 0;
             while (features.hasNext()) {
                 SimpleFeature next = features.next();
                 Object transectId = next.getAttribute(Constants.TRANSECT_ID_ATTR);
                 long id = -1;
                 if (transectId instanceof Integer) {
                     id = new Long(((Integer)transectId).longValue());
                 }
                 else if (transectId instanceof Long) {
                     id = (Long)transectId;
                 }
                 Double[] values = resultMap.get(id);
                Object[] joinedAttrs = new Object[next.getAttributeCount() + values.length];
                 List<Object> oldAttributes = next.getAttributes();
                 oldAttributes.addAll(Arrays.asList(values));
                 oldAttributes.add(new Integer(startx++));
                 oldAttributes.toArray(joinedAttrs);
                 SimpleFeature feature = SimpleFeatureBuilder.build(newFeatureType, joinedAttrs, null);
                 sfList.add(feature);
             }
             return sfList;
         }
 
         private String[] getColumnHeaders(String results) {
             String[] lines = results.split("\n");
             if (lines.length <= 1) {
                 throw new InputFileFormatException("Results must have at least 2 rows");
             }
             String[] header = lines[0].split("\t");
             for (int i=0; i<header.length; i++) {
                 header[i] = header[i].replaceAll("\"", "");
             }
             return header;
         }
     }
 }
