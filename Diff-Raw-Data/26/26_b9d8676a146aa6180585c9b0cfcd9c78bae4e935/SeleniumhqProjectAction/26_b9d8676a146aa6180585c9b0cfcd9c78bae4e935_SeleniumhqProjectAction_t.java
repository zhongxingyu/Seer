 package hudson.plugins.seleniumhq;
 
 import hudson.FilePath;
 import hudson.Functions;
 import hudson.model.Actionable;
 import hudson.model.Build;
 import hudson.model.DirectoryBrowserSupport;
 import hudson.model.Project;
 import hudson.model.ProminentProjectAction;
 import hudson.model.Result;
 import hudson.util.Area;
 import hudson.util.ChartUtil;
 import hudson.util.ColorPalette;
 import hudson.util.DataSetBuilder;
 import hudson.util.ShiftedCategoryAxis;
 import hudson.util.StackedAreaRenderer2;
 import hudson.util.ChartUtil.NumberOnlyBuildLabel;
 
 import java.awt.Color;
import java.io.File;
 import java.io.IOException;
import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.CategoryLabelPositions;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.StackedAreaRenderer;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.ui.RectangleInsets;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class SeleniumhqProjectAction extends Actionable implements ProminentProjectAction {
     
 	private final Project<?, ?> project;
 
     public SeleniumhqProjectAction(Project<?, ?> project) {
         this.project = project;
     }
     
 	public String getDisplayName() {
 		return "Selenium Report";
 	}
 
 	public String getIconFileName() {
 		return "graph.gif";
 	}
 
 	public String getUrlName() {
 		return "seleniumhq";
 	}
 
 	public String getSearchUrl() {
 		return getUrlName();
 	}
 
 	public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException,InterruptedException 
 	{
		File rootFile = SeleniumhqPublisher.getSeleniumReportDir(project);
		if (!rootFile.exists())rootFile.mkdir();		
		FilePath rootTarget =  new FilePath(rootFile);		
		if (rootTarget.list().size() == 0)
		{
			// Make index
			String index = "<html><head><title>Selenium result</title></head><body><center><br/><h2>No Selenium Test Result</h2></center></body></html>";
        	OutputStream output = rootTarget.child("index.html").write();
        	output.write(index.getBytes());
        	output.close();
		}
		new DirectoryBrowserSupport(this, "Seleniumhq").serveFile(req, rsp, rootTarget, "graph.gif", false);
 	}
 	
     public SeleniumhqBuildAction getLastResult() {
         for (Build<?, ?> b = project.getLastBuild(); b != null; b = b.getPreviousBuild()) {
             if (b.getResult() == Result.FAILURE)
                 continue;
             SeleniumhqBuildAction r = b.getAction(SeleniumhqBuildAction.class);
             if (r != null)
                 return r;
         }
         return null;
     }
 	
     public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
         if (ChartUtil.awtProblemCause != null || getLastResult() == null) {
             // not available. send out error message
             rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
             return;
         }
         
         if (req.checkIfModified(project.getLastBuild().getTimestamp(), rsp))
             return; 
 
         
         ChartUtil.generateGraph(req, rsp, createChart(req, buildDataSet()), calcDefaultSize());
     }
     
     private CategoryDataset buildDataSet() {
     	DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
 
         for (SeleniumhqBuildAction a = getLastResult(); a != null; a = a.getPreviousResult()) {
             ChartUtil.NumberOnlyBuildLabel label = new NumberOnlyBuildLabel(a.getOwner());            
             dsb.add(a.getResult().getNumTestPasses(), "passes", label);
             dsb.add(a.getResult().getNumTestFailures(), "failed", label);
         }
         return dsb.build();
     }
     
     /**
      * Determines the default size of the trend graph.
      *
      * This is default because the query parameter can choose arbitrary size.
      * If the screen resolution is too low, use a smaller size.
      */
     private Area calcDefaultSize() {
         Area res = Functions.getScreenResolution();
         if(res!=null && res.width<=800)
             return new Area(250,100);
         else
             return new Area(500,200);
     }
     
     private JFreeChart createChart(StaplerRequest req, CategoryDataset dataset) {
     	
     	final String relPath = getRelPath(req);
     	 
         final JFreeChart chart = ChartFactory.createStackedAreaChart(
             null,                   // chart title
             null,                   // unused
             "count",                  // range axis label
             dataset,                  // data
             PlotOrientation.VERTICAL, // orientation
             false,                     // include legend
             true,                     // tooltips
             false                     // urls
         );
 
         chart.setBackgroundPaint(Color.white);
 
         final CategoryPlot plot = chart.getCategoryPlot();
 
         plot.setBackgroundPaint(Color.WHITE);
         plot.setOutlinePaint(null);
         plot.setForegroundAlpha(0.8f);
         plot.setRangeGridlinesVisible(true);
         plot.setRangeGridlinePaint(Color.black);
 
         CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
         plot.setDomainAxis(domainAxis);
         domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
         domainAxis.setLowerMargin(0.0);
         domainAxis.setUpperMargin(0.0);
         domainAxis.setCategoryMargin(0.0);
 
         final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
         StackedAreaRenderer ar = new StackedAreaRenderer2() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
             public String generateURL(CategoryDataset dataset, int row, int column) {
                 NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                 return relPath+label.build.getNumber()+"/testReport/";
             }
         };
         plot.setRenderer(ar);
         ar.setSeriesPaint(0,ColorPalette.RED); // Failures.
         ar.setSeriesPaint(1,ColorPalette.BLUE); // Total.
 
         // crop extra space around the graph
         plot.setInsets(new RectangleInsets(0,0,0,5.0));
 
         return chart;
     }
     
     private String getRelPath(StaplerRequest req) {
         String relPath = req.getParameter("rel");
         if(relPath==null)   return "";
         return relPath;
     }
 }
