 package org.sonar.plugins.qi;
 
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.batch.DependedUpon;
 import org.sonar.api.batch.DependsUpon;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.resources.Project;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.utils.KeyValueFormat;
 
 import java.util.List;
 import java.util.Arrays;
 import java.util.Map;
 
 public class ComplexityDecorator extends AbstractDecorator {
   public boolean shouldExecuteOnProject(Project project) {
     return QIPlugin.shouldExecuteOnProject(project);
   }
 
   @DependedUpon
   public List<Metric> dependedUpon() {
     return Arrays.asList(QIMetrics.QI_COMPLEXITY_FACTOR, QIMetrics.QI_COMPLEXITY_FACTOR_METHODS, QIMetrics.QI_COMPLEXITY);
   }
 
   @DependsUpon
   public List<Metric> dependsUpon() {
     return Arrays.asList(QIMetrics.QI_COMPLEX_DISTRIBUTION);
   }
 
   public void decorate(Resource resource, DecoratorContext context) {
     saveMeasure(context, computeComplexityFactor(context), QIMetrics.QI_COMPLEXITY_FACTOR);
     saveMeasure(context, computeComplexMethodCount(context), QIMetrics.QI_COMPLEXITY_FACTOR_METHODS);
     saveMeasure(context, computeComplexityAxis(context), QIMetrics.QI_COMPLEXITY);
   }
 
   private double computeComplexityAxis(DecoratorContext context) {
     double complexity = getWeightedComplexity(context);
     return complexity / getValidLines(context);
   }
 
   protected double computeComplexMethodCount(DecoratorContext context) {
     Measure measure = context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION);
     if (measure == null) {
       return 0;
     }
     Map<Integer, Integer> distribution = KeyValueFormat.parse(measure.getData(), new KeyValueFormat.IntegerNumbersPairTransformer());
     return distribution.get(30);
   }
 
   protected double computeComplexityFactor(DecoratorContext context) {
     double methodWithComplexityCount = getWeightedComplexity(context);
     if (methodWithComplexityCount == 0) {
       return 0;
     }
    double complexityFactor = 5 * getComplexMethods(context) / methodWithComplexityCount * 100;
 
     return complexityFactor > 100 ? 100 : complexityFactor;
   }
 
   private double getWeightedComplexity(DecoratorContext context) {
     Measure measure = context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION);
     if (measure == null) {
       return 0;
     }
     Map<Integer, Integer> distribution = KeyValueFormat.parse(measure.getData(), new KeyValueFormat.IntegerNumbersPairTransformer());
     double methodWithComplexityCount = distribution.get(1) + distribution.get(10) + distribution.get(20) + distribution.get(30);
     return methodWithComplexityCount;
   }
 
   private double getComplexMethods(DecoratorContext context) {
     Measure measure = context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION);
     if (measure == null) {
       return 0;
     }
     Map<Integer, Integer> distribution = KeyValueFormat.parse(measure.getData(), new KeyValueFormat.IntegerNumbersPairTransformer());
     return distribution.get(30);
   }
 }
