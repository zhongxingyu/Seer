 package net.praqma.jenkins.plugin.drmemory;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.CategoryLabelPositions;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.LineAndShapeRenderer;
 import org.jfree.chart.title.LegendTitle;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.RectangleInsets;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import net.praqma.drmemory.DrMemoryResult;
 import net.praqma.jenkins.plugin.drmemory.graphs.AbstractGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.AllLeaksGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.TotalLeaksGraph;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import hudson.model.Result;
 import hudson.util.ChartUtil;
 import hudson.util.ColorPalette;
 import hudson.util.DataSetBuilder;
 import hudson.util.ShiftedCategoryAxis;
 
 public class DrMemoryBuildAction implements Action {
 	
 	private static final Logger logger = Logger.getLogger( DrMemoryBuildAction.class.getName() );
 	
 	public enum GraphType {
 		TOTAL_LEAK
 	}
 	
 	private DrMemoryResult result;
 	private final AbstractBuild<?, ?> build;
 	private DrMemoryPublisher publisher;
 	
 	private DrMemoryBuilder builder;
 	
 	public DrMemoryBuildAction( AbstractBuild<?, ?> build, DrMemoryBuilder builder ) {
 		this.build = build;
 		this.builder = builder;
 	}
 	
 	public void setPublisher( DrMemoryPublisher publisher ) {
 		this.publisher = publisher;
 	}
 	
 	public DrMemoryPublisher getPublisher() {
 		return publisher;
 	}
 	
 	public void setResult( DrMemoryResult result ) {
 		this.result = result;
 	}
 	
 	public DrMemoryBuilder getBuilder() {
 		return builder;
 	}
 
 	public String getDisplayName() {
 		return "DrMemory";
 	}
 
	public String getSearchUrl() {
		return getUrlName();
	}

 	public String getIconFileName() {
 		return "graph.gif";
 	}
 
 	public String getUrlName() {
 		return "drmemory";
 	}
 	
 	public DrMemoryResult getResult() {
 		return result;
 	}
 	
 	public void doIndex( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
 		File dm = new File( build.getRootDir(), "drmemory.txt" );
 		Calendar t = build.getTimestamp();
 		if( dm.exists() ) {
 			rsp.serveFile( req, FileUtils.openInputStream( dm ), t.getTimeInMillis(), dm.getTotalSpace(), "drmemory.txt" );
 		} else {
 			rsp.sendError( HttpServletResponse.SC_NO_CONTENT );
 		}
 	}
 	
 	static DrMemoryBuildAction getPreviousResult( AbstractBuild<?, ?> start ) {
 		AbstractBuild<?, ?> b = start;
 		while( true ) {
 			b = b.getPreviousBuild();
 			if( b == null ) {
 				return null;
 			}
 
 			DrMemoryBuildAction r = b.getAction( DrMemoryBuildAction.class );
 			if( r != null && r.getResult() == null ) {
 				r = null;
 			}
 
 			if( r != null ) {
 				return r;
 			}
 		}
 	}
 	
 	public DrMemoryBuildAction getPreviousResult() {
 		return getPreviousResult( build );
 	}
 	
 	
 	public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
 		String type = req.getParameter( "type" );
 		
 		logger.fine( "Graphing " + type );
 
 		int width = 500, height = 200;
 		String w = req.getParameter( "width" );
 		String h = req.getParameter( "height" );
 		if( w != null && w.length() > 0 ) {
 			width = Integer.parseInt( w );
 		}
 
 		if( h != null && h.length() > 0 ) {
 			height = Integer.parseInt( h );
 		}
 
 		if( type == null ) {
 			throw new IOException( "No type given" );
 		}
 
 		if( ChartUtil.awtProblemCause != null ) {
 			// not available. send out error message
 			rsp.sendRedirect2( req.getContextPath() + "/images/headless.png" );
 			return;
 		}
 
 		Calendar t = build.getTimestamp();
 
 		if( req.checkIfModified( t, rsp ) ) {
 			return; // up to date
 		}
 		
 		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
 		
 		boolean latest = true;
 		String yaxis = "???";
 		float min = 999999999;
 		float max = 0;
 		
 		AbstractGraph g = publisher.getGraphTypes().get( type );
 		if( g == null ) {
 			rsp.sendError( 1 );
 			return;
 		}
 		
 		/* For each build, moving backwards */
 		for( DrMemoryBuildAction a = this; a != null; a = a.getPreviousResult() ) {
 			logger.finest( "Build " + a.getDisplayName() );
 
 			/* Make the x-axis label */
 			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( a.build );
 			
 			float[] ns = g.getNumber( a );
 			g.addX( dsb, ns, label );
 			
 			for( float n : ns ) {
 				if( n > max ) {
 					max = n;
 				}
 				
 				if( n < min ) {
 					min = n;
 				}
 				
 				if( latest ) {
 					yaxis = g.getYAxis();
 				}
 			}
 			
 			latest = false;
 		}
 		
 		ChartUtil.generateGraph( req, rsp, createChart( dsb.build(), g.getTitle(), yaxis, (int)max, (int)min ), width, height );
 	}
 	
 	
 	private JFreeChart createChart( CategoryDataset dataset, String title, String yaxis, int max, int min ) {
 
 		final JFreeChart chart = ChartFactory.createLineChart( title, // chart
 																		// title
 				null, // unused
 				yaxis, // range axis label
 				dataset, // data
 				PlotOrientation.VERTICAL, // orientation
 				true, // include legend
 				true, // tooltips
 				false // urls
 		);
 
 		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
 
 		final LegendTitle legend = chart.getLegend();
 
 
 		chart.setBackgroundPaint( Color.white );
 
 		final CategoryPlot plot = chart.getCategoryPlot();
 
 		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
 		plot.setBackgroundPaint( Color.WHITE );
 		plot.setOutlinePaint( null );
 		plot.setRangeGridlinesVisible( true );
 		plot.setRangeGridlinePaint( Color.black );
 
 		CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
 		plot.setDomainAxis( domainAxis );
 		domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
 		domainAxis.setLowerMargin( 0.0 );
 		domainAxis.setUpperMargin( 0.0 );
 		domainAxis.setCategoryMargin( 0.0 );
 
 		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
 		rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
 		rangeAxis.setUpperBound( max );
 		rangeAxis.setLowerBound( min );
 
 		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
 		renderer.setBaseStroke( new BasicStroke( 2.0f ) );
 		ColorPalette.apply( renderer );
 
 		// crop extra space around the graph
 		plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );
 
 		return chart;
 	}
 
 }
