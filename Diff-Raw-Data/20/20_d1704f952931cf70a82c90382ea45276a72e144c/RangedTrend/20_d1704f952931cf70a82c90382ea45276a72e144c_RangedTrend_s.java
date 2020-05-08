 package hudson.plugins.testabilityexplorer.report.charts;
 
 import org.jfree.data.category.CategoryDataset;
 import hudson.plugins.testabilityexplorer.report.costs.Statistic;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Helper class that will ease the creation of JFree line charts for testability trends.
  *
  * @author reik.schatz
  */
 public abstract class RangedTrend
 {
    public static final int DEFAULT_RANGE_AXIS     = 100;
 
     private final List<BuildAndResults> m_items;
 
     public RangedTrend(List<BuildAndResults> items)
     {
         m_items = new ArrayList<BuildAndResults>();
         if (items != null)
         {
             m_items.addAll(items);
         }
     }
 
     List<BuildAndResults> getItems()
     {
         return new ArrayList<BuildAndResults>(m_items);
     }
 
     /**
      * Returns an int which may be used in a JFree line chart to call {@link org.jfree.chart.axis.NumberAxis#setUpperBound}
      * therefore setting the range of the y-axis to the right value.
      *
      * @return int
      */
     public abstract int getUpperBoundRangeAxis();
 
     /**
      * Builds and returns a {@link CategoryDataset} representing a testability trend. This
      * {@link CategoryDataset} may be used to create a JFree line chart.
      *
      * @return CategoryDataset
      */
     public abstract CategoryDataset getCategoryDataset();
 
     /**
      * Returns an int value representing a cost of the specified Statistic collection.
      * The cost of each {@link Statistic} element will be determined using the given {@link CostTemplate}.
      *
      * @param statistics a Collection of {@link Statistic}
      * @param template a {@link CostTemplate}
      * @return int
      */
     protected int summarizeCost(Collection<Statistic> statistics, CostTemplate template)
     {
         int cost = 0;
         for (Statistic statistic : statistics)
         {
             cost += template.getCost(statistic);
         }
         return cost;
     }
 }
