 package statistics.queryparser;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.geotools.data.Query;
 import org.geotools.filter.visitor.DefaultFilterVisitor;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.filter.PropertyIsEqualTo;
 import org.opengis.filter.expression.Literal;
 import org.opengis.filter.expression.PropertyName;
 import org.opengis.filter.spatial.BBOX;
 import org.opengis.filter.expression.Expression;
 import statistics.model.indicator.Dimension;
 import statistics.model.indicator.GeographicDimension;
 import statistics.model.indicator.IndicatorConfiguration;
 import statistics.store.feature.FeatureSchemaBuilder;
 
 /**
  *
  * @author Andre Matos
  */
 public class StatisticsQueryParser {
 
     private Query query;
     private final FeatureType featureType;
     private StatisticsRequestParameters requestedParameters;
     private IndicatorConfiguration indicator;
 
     private class QueryVisitor extends DefaultFilterVisitor {
 
         private BBOX bbox;
         private Map<String, Object> queryMap;
 
 
         public QueryVisitor() {
             queryMap = new Hashtable<String, Object>();
         }
 
         @Override
         public Object visit(BBOX filter, Object data) {
 
             Logger.getLogger(StatisticsQueryParser.class.getName()).log (
                 Level.INFO,
                 "bbox visited: " + (bbox != null ? bbox.toString() : "")
             );
 
             //Object obj = super.visit(filter, data);
             bbox = filter;
             //return obj;
             return filter;
         }
 
         @Override
         public Object visit(PropertyIsEqualTo filter, Object data) {
            // Object obj = super.visit(filter, data);
             Expression left = filter.getExpression1();
             Expression right = filter.getExpression2();
             
             if (left instanceof PropertyName) {
                 // aha!  It's a propertyname, we should get the name and pass it in
                 // as context to the tree walker.
                 AttributeDescriptor attType = (AttributeDescriptor)left.evaluate(featureType);
                 if (attType != null) {
                     right.accept(this, attType.getLocalName());
                 }
             }
 
             return null;
         }
 
         @Override
         public Object visit( Literal expression, Object data ) {
 
            Logger.getLogger(StatisticsQueryParser.class.getName()).log (
                Level.INFO,
                "property visited: " +
                    "\n\t" + (String)data + ": " + expression.getValue()
            );
 
             if(data != null && data instanceof String) {
                 
                 queryMap.put((String)data, expression.getValue());
             }
 
             return null;
         }
     }
 
     public StatisticsQueryParser(Query query, FeatureType featureType) {
         this.query = query;
         this.featureType = featureType;
     }
 
     public int getSourceId() {
         
         return getParameters().sourceId;
     }
 
     public int getIndicatorId() {
         
         return getParameters().indicatorId;
     }
 
     public String getShapeLevel() {
 
         return parseParameters().getGeographicDimension().shapeLevel;
     }
 
     public List<String> getShapeIds() {
 
         return parseParameters().getGeographicDimension().dimensionAttributes;
     }
 
     public List<Dimension> getDimensions() {
         return parseParameters().dimensions;
     }
 
     public IndicatorConfiguration getIndicatorConfiguration() {
 
         return parseParameters();
     }
 
     public BBOX getBoundingBox() {
         
         return getParameters().bbox;
     }
 
 
     public StatisticsRequestParameters getParameters() {
 
         if(requestedParameters == null) {
 
             Logger.getLogger(StatisticsQueryParser.class.getName()).log (
                 Level.INFO,
                 "getParameters: converting query into object parameters"
             );
 
             QueryVisitor visitor = new QueryVisitor();
             Filter filter = query.getFilter();
             if(filter != null)
                 filter.accept(visitor, null);
 
             requestedParameters = new StatisticsRequestParameters(
                         visitor.bbox,
                         visitor.queryMap.containsKey(FeatureSchemaBuilder.DIMENSIONS_PROPERTY)    ? (String) visitor.queryMap.get(FeatureSchemaBuilder.DIMENSIONS_PROPERTY)                     : null,
                         visitor.queryMap.containsKey(FeatureSchemaBuilder.INDICATORID_PROPERTY)   ? ((Long) visitor.queryMap.get(FeatureSchemaBuilder.INDICATORID_PROPERTY)).intValue()         : -1,
                         visitor.queryMap.containsKey(FeatureSchemaBuilder.SOURCEID_PROPERTY)      ? ((Long) visitor.queryMap.get(FeatureSchemaBuilder.SOURCEID_PROPERTY)).intValue()            : -1
                     );
 
 //            requestedParameters = new StatisticsRequestParameters(
 //                        null,
 //                        "1,S7A2009,S7A2008,S7A2007,S7A2006,S7A2005,S7A2004,S7A2003,S7A2002,S7A2001,S7A2000#2-NUTS1",
 //                        1,
 //                        1
 //                    );
         }
 
         return requestedParameters;
     }
 
     private IndicatorConfiguration parseParameters() {
 
         if(indicator == null) {
 
             Logger.getLogger(StatisticsQueryParser.class.getName()).log (
                 Level.INFO,
                 "parse parameters: converting strings into objetcts"
             );
 
             StatisticsRequestParameters request = getParameters();
             indicator = new IndicatorConfiguration(
                             request.sourceId,
                             request.indicatorId,
                             parseDimensions(request.dimensions)
                         );
         }
 
 
         return indicator;
     }
 
     /**
      * Parses the dimensions into an object structure
      * @param dimensions
      *
      * The dimension parameter is in the following format:
      *      1,1,2,3,4#2,1,2,3,4
      *      dimensionId1,dimensionAttr1,dimensionAttr2#dimensionId2-geoLevel,dimensionAttr1,dimensionAttr2
      * @return List<Dimension>
      */
     private List<Dimension> parseDimensions(String dimensions){
 
         Logger.getLogger(StatisticsQueryParser.class.getName()).log (
                 Level.INFO,
                 "Parsing dimensions: " + dimensions
             );
 
         if(dimensions == null) return null;
         List<Dimension> dimensionsList = new ArrayList<Dimension>();
 
         String[] splitedDimensions = dimensions.split("#");
         for (String d : splitedDimensions) 
             dimensionsList.add(parseDimension(d));
 
         return dimensionsList;
     }
 
     /**
      * Parses a dimension into object structure
      * @param dimension
      *
      * The dimension parameter is in the following format:
      *      1,1,2,3,4
      *      1-District,1,2,3
      *      
      *      dimensionId1,dimensionAttr1,dimensionAttr2
      *      dimensionId2-geoLevel,dimensionAttr1,dimensionAttr2
      * 
      * @return Dimension
      */
     private Dimension parseDimension(String dimension){
 
         Logger.getLogger(StatisticsQueryParser.class.getName()).log (
                 Level.INFO,
                 "Parsing dimension: " + dimension
             );
 
         Dimension d = null;
         if(dimension == null) return d;
 
         String[] dSplited = dimension.split(",");
         String[] dimensionId = dSplited[0].split("-");
 
         // Check if the current dimension is a geographic dimension.
         // If it is, build a geographic type
         boolean isGeoDimension = dimensionId.length == 2;
         if(isGeoDimension)
             d = new GeographicDimension(dimensionId[0], dimensionId[1]);
         else d = new Dimension(dimensionId[0]);
 
         // Add dimension attributes
         for (int i = 1; i < dSplited.length; i++) {
             d.addAttribute(dSplited[i]);
         }
 
 
         return d;
     }
 }
