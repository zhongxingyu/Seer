 package hudson.plugins.testabilityexplorer.report;
 
 import hudson.plugins.testabilityexplorer.PluginBaseTest;
 import hudson.plugins.testabilityexplorer.PluginImpl;
 import hudson.plugins.testabilityexplorer.report.charts.*;
 import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
 import hudson.plugins.testabilityexplorer.report.costs.Statistic;
 import hudson.plugins.testabilityexplorer.report.health.ReportBuilder;
 import hudson.plugins.testabilityexplorer.report.health.TemporaryHealthCalculator;
 import hudson.plugins.testabilityexplorer.report.health.TestabilityReportBuilder;
 import hudson.model.AbstractBuild;
 import org.testng.annotations.Test;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.CategoryPlot;
 
 import static org.testng.Assert.*;
 
 import java.util.*;
 
 /**
  * Tests the TestabilityChartBuilder.
  */
 @Test
 public class AbstractBuildReportChartingTest extends PluginBaseTest
 {
    private static final int MAX_COST_IN_TEST = 999;
 
     public void testOverallTrendChart()
     {
         List<BuildAndResults> buildsAndResults = createBuildAndResults();
 
         AbstractBuildReport abstractBuildReport = createAbstractBuildReport();
         String displayName = abstractBuildReport.getDisplayName();
         assertEquals(displayName, PluginImpl.DISPLAY_NAME);
         String graphName = abstractBuildReport.getGraphName();
         assertEquals(graphName, PluginImpl.GRAPH_NAME);
         String iconFileName = abstractBuildReport.getIconFileName();
         assertEquals(iconFileName, PluginImpl.ICON_FILE_NAME);
         String urlName = abstractBuildReport.getUrlName();
         assertEquals(urlName, PluginImpl.URL);
 
         RangedTrend classesTrend = new RangedOverallTrend(buildsAndResults);
         JFreeChart chartClasses = abstractBuildReport.createChart(classesTrend);
         assertNotNull(chartClasses);
 
         CategoryDataset overallCategoryDataset = classesTrend.getCategoryDataset();
         assertNotNull(overallCategoryDataset);
         assertEquals(overallCategoryDataset.getRowCount(), 1);
         assertEquals(overallCategoryDataset.getColumnCount(), 10);
 
         JFreeChart chartOverall = abstractBuildReport.createChart(classesTrend);
         assertNotNull(chartOverall);
     }
 
     public void testClassesTrendChart()
     {
         List<BuildAndResults> buildsAndResults = createBuildAndResults();
 
         AbstractBuildReport abstractBuildReport = createAbstractBuildReport();
         RangedTrend classesTrend = new RangedClassesTrend(buildsAndResults);
         int maxCost = classesTrend.getUpperBoundRangeAxis();
         assertTrue(maxCost <= MAX_COST_IN_TEST);
 
         JFreeChart chartClasses = abstractBuildReport.createChart(classesTrend);
         assertNotNull(chartClasses);
 
         CategoryDataset categoryDataset = classesTrend.getCategoryDataset();
         assertNotNull(categoryDataset);
         assertEquals(categoryDataset.getRowCount(), 3);
         assertEquals(categoryDataset.getColumnCount(), 10);
 
         JFreeChart chartOverall = abstractBuildReport.createChart(classesTrend);
         assertNotNull(chartOverall);
         CategoryPlot categoryPlot = chartOverall.getCategoryPlot();
         assertNotNull(categoryPlot);
         ValueAxis valueAxis = categoryPlot.getRangeAxis();
         assertNotNull(valueAxis);
         assertTrue(valueAxis.getUpperBound() == maxCost);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testInvalid()
     {
         AbstractBuildReport abstractBuildReport = createAbstractBuildReport();
         abstractBuildReport.createChart(null);
     }
 
     public void testRetrieveExistingBuildsAndResults()
     {
         ChartBuilder chartBuilder = new TestabilityChartBuilder();
         ReportBuilder reportBuilder = new TestabilityReportBuilder(chartBuilder, new TemporaryHealthCalculator());
 
         Collection<Statistic> results = createStatistics();
 
         AbstractBuild<?,?> build = createBuild(1, GregorianCalendar.getInstance());
         AbstractBuildReport abstractBuildReport = new BuildIndividualReport(results, reportBuilder, new CostDetailBuilder())
         {
             @Override
             AbstractBuild<?, ?> getPreviousBuild(AbstractBuild<?, ?> build)
             {
                 return null;
             }
         };
         List items = abstractBuildReport.retrieveExistingBuildsAndResults(build);
         assertEquals(items.size(), 1);
     }
 
     public void testCostTemplates()
     {
         CostSummary costSummary = new CostSummary(1, 2, 3, 20);
         Statistic statistic = new ArrayList<Statistic>(createStatistics(false, costSummary)).get(0);
 
         CostTemplate excellent = RangedClassesTrend.EXCELLENT_COST_TEMPLATE;
         assertEquals(excellent.getCost(statistic), 1);
 
         CostTemplate good = RangedClassesTrend.GOOD_COST_TEMPLATE;
         assertEquals(good.getCost(statistic), 2);
 
         CostTemplate poor = RangedClassesTrend.POOR_COST_TEMPLATE;
         assertEquals(poor.getCost(statistic), 3);
 
         CostTemplate all = RangedOverallTrend.TOTAL_COST_TEMPLATE;
         assertEquals(all.getCost(statistic), 20);
     }
 
     private AbstractBuildReport createAbstractBuildReport()
     {
         ReportBuilder reportBuilder = new TestabilityReportBuilder(null, new TemporaryHealthCalculator());
 
         Collection<Statistic> results = createStatistics();
         return new BuildIndividualReport(results, reportBuilder, new CostDetailBuilder());
     }
 
     private List<BuildAndResults> createBuildAndResults()
     {
         Random randomGenerator = new Random();
         List<BuildAndResults> buildsAndResults = new ArrayList<BuildAndResults>();
         for (int i = 0; i < 10; i++)
         {
             AbstractBuild<?,?> build = createBuild(i + 1, GregorianCalendar.getInstance());
 
             int excellent = randomGenerator.nextInt(MAX_COST_IN_TEST);
             int good = randomGenerator.nextInt(MAX_COST_IN_TEST);
             int needWork = randomGenerator.nextInt(MAX_COST_IN_TEST);
             int total = randomGenerator.nextInt(MAX_COST_IN_TEST);
             CostSummary costSummary = new CostSummary(excellent, good, needWork, total);
             Collection<Statistic> stats = createStatistics(false, costSummary);
 
             buildsAndResults.add(new BuildAndResults(build, stats));
         }
         return buildsAndResults;
     }
 }
