 package gov.usgs.cida.gdp.coreprocessing.analysis.grid;
 
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.prep.PreparedGeometry;
 import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility.IndexToCoordinateBuilder;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.geometry.BoundingBox;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  *
  * @author tkunicki
  */
 public class FeatureCategoricalGridCoverage {
 
     public static void execute(
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
             String attributeName,
             GridDataset gridDataset,
             String variableName,
             BufferedWriter writer,
             Delimiter delimiter)
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
     {
         GridDatatype gridDatatype = 
                 checkNotNull(
                     gridDataset.findGridDatatype(
                         checkNotNull(variableName, "variableName argument may not be null")),
                     "Variable named %s not found in girdded dataset %s", variableName);
         execute(featureCollection, attributeName, gridDatatype, writer, delimiter, true);
     }
 
     public static void execute(
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
             String attributeName,
             GridDatatype gridDataType,
             BufferedWriter writer,
             Delimiter delimiter,
             boolean requireFullCoverage)
             throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
     {
 
         GridCoordSystem gcs = gridDataType.getCoordinateSystem();
         GridType gt = GridType.findGridType(gcs);
         if (gt != GridType.YX) {
            throw new IllegalStateException("Currently require y-x grid for this operation");
         }
 
         // these two calls are used to test for coverage/intersection based on 'requireFullCoverage',
         // if required coverage criterea is not fufilled an exception will be thrown.
         Range[] featureCollectionRanges = GridUtility.getXYRangesFromBoundingBox(featureCollection.getBounds(), gcs, requireFullCoverage);
         gridDataType = gridDataType.makeSubset(null, null, null, null, featureCollectionRanges[1], featureCollectionRanges[0]);
         
         CoordinateReferenceSystem gridCRS = CRSUtility.getCRSFromGridCoordSystem(gcs);        
 		CoordinateReferenceSystem featureCRS =
 				featureCollection.getSchema().getCoordinateReferenceSystem();
 
 		MathTransform gridToFeatureTransform = CRS.findMathTransform(
 				gridCRS,
 				featureCRS,
 				true);
         
         AttributeDescriptor attributeDescriptor =
                 featureCollection.getSchema().getDescriptor(attributeName);
         if (attributeDescriptor == null) {
             throw new IllegalArgumentException(
                     "Attribute " + attributeName + " not found in FeatureCollection.");
         }
 
         boolean attributeComparable = Comparable.class.isAssignableFrom(
                 attributeDescriptor.getType().getBinding());
 
         Map<Object, Map<Integer, Integer>> attributeToCategoricalCoverageMap = attributeComparable ?
                 // rely on Comparable to sort
                 new TreeMap<Object, Map<Integer, Integer>>() :
                 // use order from FeatureCollection.iterator();
                 new LinkedHashMap<Object, Map<Integer, Integer>>();
         SortedSet<Integer> categorySet = new TreeSet<Integer>();
 
         Iterator<SimpleFeature> featureIterator = featureCollection.iterator();
 
         try {
             while (featureIterator.hasNext()) {
 
                 SimpleFeature feature = featureIterator.next();
                 Object attribute = feature.getAttribute(attributeName);
 
                 if (attribute != null) {
 
                     Map<Integer, Integer> categoricalCoverageMap =
                             attributeToCategoricalCoverageMap.get(attribute);
                     if (categoricalCoverageMap == null) {
                         categoricalCoverageMap = new TreeMap<Integer, Integer>();
                         attributeToCategoricalCoverageMap.put(attribute, categoricalCoverageMap);
                     }
 
                     BoundingBox featureBoundingBox = feature.getBounds();
 
                     Geometry featureGeometry = (Geometry)feature.getDefaultGeometry();
 
                     try {
                         Range[] featureRanges = GridUtility.getXYRangesFromBoundingBox(
                                 featureBoundingBox, gridDataType.getCoordinateSystem(), requireFullCoverage);
 
                         GridDatatype featureGridDataType = gridDataType.makeSubset(null, null, null, null, featureRanges[1], featureRanges[0]);
 
                         PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(featureGeometry);
 
                         GridCellTraverser traverser = new GridCellTraverser(featureGridDataType);
                         traverser.traverse(new FeatureGridCellVisitor(
                                 preparedGeometry,
                                 gridToFeatureTransform,
                                 categoricalCoverageMap));
 
                         categorySet.addAll(categoricalCoverageMap.keySet());
                     } catch (InvalidRangeException e) {
                         /* this may happen if the feature doesn't intersect the grid */
                     }
                 }
             }
         } finally {
             featureCollection.close(featureIterator);
         }
 
         SimpleDelimitedWriter delimitedWriter = new SimpleDelimitedWriter(delimiter, writer);
 
         Collection headerRow = new ArrayList<Object>();
         for (Integer i : categorySet) {
             headerRow.add("Category");
         }
         delimitedWriter.writeRow(null, headerRow);
         
         headerRow.clear();
         headerRow.addAll(categorySet);
         headerRow.add("Sample Count");
 
         delimitedWriter.writeRow("Attribute", headerRow);
 
         for (Map.Entry<Object, Map<Integer, Integer>> entry : attributeToCategoricalCoverageMap.entrySet()) {
             Object attributeValue = entry.getKey();
             Map<Integer, Integer> categoricalCoverageMap = entry.getValue();
 
             List<Number> rowValues = new ArrayList<Number>();
             int total = 0;
             // gather total sample count for attribute
             for (Integer count : categoricalCoverageMap.values()) {
                 if (count != null) {
                     total += count;
                 }
             }
             // calculate and store fraction for each categorical type
             for (Integer category : categorySet) {
                 Integer count = categoricalCoverageMap.get(category);
                 float fraction = count == null ?  0 : (float) count / (float) total;
                 rowValues.add(fraction);
             }
             rowValues.add(total);
 
             delimitedWriter.writeRow(attributeValue.toString(), rowValues);
         }
 
     }
 
     protected static class FeatureGridCellVisitor extends GridCellVisitor {
 
         private final GeometryFactory geometryFactory = new GeometryFactory();
         private final PreparedGeometry preparedGeometry;
 		private final MathTransform gridToFeatureTransform;
         private final Map<Integer, Integer> categoryMap;
 
         private IndexToCoordinateBuilder coordinateBuilder;
 
         protected FeatureGridCellVisitor(
                 PreparedGeometry preparedGeometry,
 				MathTransform gridToFeatureTransform,
                 Map<Integer, Integer> categoryMap) {
             this.preparedGeometry = preparedGeometry;
 			this.gridToFeatureTransform = gridToFeatureTransform;
             this.categoryMap = categoryMap;
         }
 
         @Override
         public void traverseStart(GridDatatype gridDatatype) {
             coordinateBuilder = GridUtility.generateIndexToCellCenterCoordinateBuilder(gridDatatype.getCoordinateSystem());
         }
 
         @Override
         public void processGridCell(int xCellIndex, int yCellIndex, double value) {
             Coordinate coordinate =
                     coordinateBuilder.getCoordinate(xCellIndex, yCellIndex);
 			try {
 				JTS.transform(coordinate, coordinate, gridToFeatureTransform);
 			} catch (TransformException e) { }
             if (preparedGeometry.contains(geometryFactory.createPoint(coordinate))) {
                 Integer key = (int) value;
                 Integer count = categoryMap.get(key);
                 count =  count == null ? 1 : count + 1;
                 categoryMap.put(key, count);
             }
             
         }
 
     }
 
     protected static class SimpleDelimitedWriter {
 
         private String delimiter;
         private BufferedWriter writer;
 
         private StringBuilder lineSB = new StringBuilder();
 
         public SimpleDelimitedWriter(
                 Delimiter delimiter,
                 BufferedWriter writer) {
 
             this.delimiter = delimiter.delimiter;
             this.writer = writer;
 
             lineSB = new StringBuilder();
         }
 
         public void writeRow(
                 String rowLabel,
                 Collection<? extends Object> rowValues)
                 throws IOException
         {
             lineSB.setLength(0);
             if (rowLabel != null) {
                 lineSB.append(rowLabel);
             }
 
             for (Object rowValue : rowValues) {
                 lineSB.append(delimiter).append(rowValue);
             }
             writer.write(lineSB.toString());
             writer.newLine();
         }
     }
 
 }
