 package statistics.store.feature.builders;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import statistics.model.indicator.IndicatorRangeFuture;
 import statistics.model.indicator.IndicatorValue;
 import statistics.queryparser.StatisticsRequestParameters;
 import statistics.store.shapes.IShapeData;
 
 /**
  *
  * @author Andre Matos
  */
 public class LinearFactFeatureBuilder extends FeatureBuilder {
 
     public LinearFactFeatureBuilder(
             SimpleFeatureType featureType, IndicatorRangeFuture range, StatisticsRequestParameters query, String shapeLevel) {
         super(featureType, range, query, shapeLevel);
     }
 
     @Override
     public SimpleFeature buildFeature(IShapeData shape, List<IndicatorValue> values) {
 
         Logger.getLogger(LinearFactFeatureBuilder.class.getName()).log (
             Level.INFO,
             "buildFeature"
         );
 
         String filterDimensions = query.filterDimensions != null ? query.filterDimensions : "";
         String projectedDimensions = query.projectedDimensions != null ? query.projectedDimensions : "";
         IndicatorValue value = values.get(0);
 
        double percent = value.value / (range.getMaxValue().value - range.getMinValue().value);
 
         SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( featureType );
         featureBuilder.add( shape.getFeatureGeometry() );
         featureBuilder.add( shape.getShapeName() );
         featureBuilder.add( shape.getShapeId() );
         featureBuilder.add( filterDimensions  );
         featureBuilder.add( projectedDimensions  );
         featureBuilder.add( query.indicatorId ); 
         featureBuilder.add( query.sourceId ); 
         featureBuilder.add( shapeLevel ); 
         featureBuilder.add( value.value );
         featureBuilder.add( percent );
 
         return featureBuilder.buildFeature( shape.getFeatureId() );
     }
 
 }
