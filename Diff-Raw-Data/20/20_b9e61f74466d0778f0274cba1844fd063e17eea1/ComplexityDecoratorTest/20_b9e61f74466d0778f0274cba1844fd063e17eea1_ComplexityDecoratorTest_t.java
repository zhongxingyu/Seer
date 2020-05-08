 package org.sonar.plugins.qi;
 
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.core.Is.is;
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.measures.Measure;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class ComplexityDecoratorTest {
 
   @Test
   public void testIOMetrics() {
     ComplexityDecorator decorator = new ComplexityDecorator();
     assertThat(decorator.dependedUpon().size(), is(3));
     assertThat(decorator.dependsUpon().size(), is(1));
   }
 
   @Test
   public void testComplexityFactorComputation() {
     DecoratorContext context = mock(DecoratorContext.class);
     when(context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION)).
      thenReturn(null);
 
     ComplexityDecorator decorator = new ComplexityDecorator();
     assertThat(decorator.computeComplexityFactor(context), is(0.0));
    when(context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION)).
      thenReturn(new Measure(QIMetrics.QI_COMPLEX_DISTRIBUTION, "1=2;10=4;20=12;30=2"));
     assertThat(decorator.computeComplexityFactor(context), is(50.0));

    when(context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION)).
      thenReturn(new Measure(QIMetrics.QI_COMPLEX_DISTRIBUTION, "1=0;10=0;20=0;30=12"));
     assertThat(decorator.computeComplexityFactor(context), is(100.0));
 
   }
 
   @Test
   public void testComputeComplexMethodCount() {
     DecoratorContext context = mock(DecoratorContext.class);
     when(context.getMeasure(QIMetrics.QI_COMPLEX_DISTRIBUTION)).
       thenReturn(
         null,
         new Measure(QIMetrics.QI_COMPLEX_DISTRIBUTION, "1=0;10=0;20=0;30=12")
       );
     ComplexityDecorator decorator = new ComplexityDecorator();
     assertThat(decorator.computeComplexMethodCount(context), is(0.0));
     assertThat(decorator.computeComplexMethodCount(context), is(12.0));
 
   }
 }
