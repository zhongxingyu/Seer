 package net.praqma.jenkins.plugin.monkit;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.praqma.jenkins.plugin.monkit.MonKitPublisher.Case;
 import net.praqma.monkit.MonKitCategory;
 import net.praqma.monkit.MonKitObservation;
 
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
 
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import hudson.model.HealthReport;
 import hudson.model.HealthReportingAction;
 import hudson.model.Result;
 import hudson.util.ChartUtil;
 import hudson.util.ColorPalette;
 import hudson.util.DataSetBuilder;
 import hudson.util.ShiftedCategoryAxis;
 
 public class MonKitBuildAction implements HealthReportingAction, Action {
 
 	private List<MonKitCategory> monkit;
 	private final AbstractBuild<?, ?> build;
 	private boolean onlyStable;
 	private MonKitPublisher publisher;
 	
 	private static Logger logger = Logger.getLogger( MonKitBuildAction.class.getName() );
 
 	public MonKitBuildAction( AbstractBuild<?, ?> build, List<MonKitCategory> monkit ) {
 		this.monkit = monkit;
 		this.build = build;
 		this.onlyStable = false;
 		
 	}
 
 	public void setPublisher( MonKitPublisher publisher ) {
 		this.publisher = publisher;
 	}
 
 	public String getDisplayName() {
 		return "MonKit";
 	}
 
 	public String getIconFileName() {
 		return "graph.gif";
 	}
 
 	public String getUrlName() {
 		return "monkit";
 	}
 
 	public HealthReport getBuildHealth() {
 		Case worst = publisher.getWorst( monkit );
 
 		/* Unstable */
 		if( worst.health == null ) {
 			return new HealthReport( 0, "MonKit Report: " + worst.category + " for " + worst.name );
 		} else if( worst.category == null ) {
 			return new HealthReport( 100, "MonKit Report" );
 		} else {
 			return new HealthReport( worst.health.intValue(), "MonKit Report: " + worst.category + " for " + worst.name );
 		}
 	}
 
 	public List<String> getCategories() {
 		List<String> categories = new ArrayList<String>();
 		for( MonKitCategory mkc : monkit ) {
 			categories.add( mkc.getName() );
 		}
 
 		return categories;
 	}
 
 	public List<MonKitCategory> getMonKitCategories() {
 		return monkit;
 	}
 	
 	public MonKitCategory getMonKitCategory( String category ) {
 		for( MonKitCategory mkc : getMonKitCategories() ) {
 			if( mkc.getName().equalsIgnoreCase( category ) ) {
 				return mkc;
 			}
 		}		
 		return null;
 	}
 	
 	public MonKitObservation getMonKitObservation( MonKitCategory mkc, String name ) {
 		for( MonKitObservation mko : mkc ) {
 			if( mko.getName().equalsIgnoreCase( name ) ) {
 				return mko;
 			}
 		}
 		
 		return null;
 	}
 
 	public AbstractBuild<?, ?> getBuild() {
 		return build;
 	}
 
 	public MonKitBuildAction getPreviousResult() {
 		return getPreviousResult( build );
 	}
 
 	private boolean includeOnlyStable() {
 		return onlyStable;
 	}
 
 	/**
 	 * Gets the previous {@link MonKitBuildAction} of the given build.
 	 */
 	/* package */
 	static MonKitBuildAction getPreviousResult( AbstractBuild<?, ?> start ) {
 		AbstractBuild<?, ?> b = start;
 		while( true ) {
 			b = b.getPreviousNotFailedBuild();
 			if( b == null ) {
 				return null;
 			}
 
 			assert b.getResult() != Result.FAILURE : "We asked for the previous not failed build";
 			MonKitBuildAction r = b.getAction( MonKitBuildAction.class );
 			if( r != null && r.includeOnlyStable() && b.getResult() != Result.SUCCESS ) {
 				r = null;
 			}
 
 			if( r != null ) {
 				return r;
 			}
 		}
 	}
 	
 	public MonKitTarget getMonkitTargetForCategory( String category ) {
 		return publisher.getTarget( category );
 	}
 	
 	public Float[] getThreshold( String category ) {
 		MonKitTarget mkt = publisher.getTarget( category );
 		
 		if( mkt != null ) {
 			Float fu = new Float( mkt.getUnstable() );
 			Float fh = new Float( mkt.getHealthy() );
 			
 			return new Float[]{fu,fh};
 		} else {
 			return null;
 		}
 	}
 	
 	public Float getHealthForCategory( String category, String name ) {
 		Float[] threshold = getThreshold( category );
 		if( threshold == null ) {
 			return 100.0f;
 		}
 		
 		Float fu = threshold[0];
 		Float fh = threshold[1];
 		
 		MonKitCategory mkc = getMonKitCategory( category );
 		MonKitObservation mko = getMonKitObservation( mkc, name );
 		
 		Float f;
 		try {
 			f = new Float( mko.getValue() );
 		} catch (NumberFormatException e) {
 			logger.warning( "[MonKitPlugin] Unknown number " + mko.getValue() );
 			return 100.0f;
 		}
 		
 		boolean isGreater = fu < fh;
 		
 		if( ( isGreater && f < fu ) || ( !isGreater && f > fu ) ) {
 			return 0.0f;
 		} else if( ( isGreater && f < fh ) || ( !isGreater && f > fh ) ) {
 			float diff = fh - fu;
 			float nf1 = f - fu;
 			float inter = ( nf1 / diff ) * 100;
 			return inter;
 		}
 		
 		return 100.0f;
 	}
 
 	public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
 		String category = req.getParameter( "category" );
 		
 		logger.fine( "Graphing " + category );
 
 		int width = 500, height = 200;
 		String w = req.getParameter( "width" );
 		String h = req.getParameter( "height" );
 		if( w != null && w.length() > 0 ) {
 			width = Integer.parseInt( w );
 		}
 
 		if( h != null && h.length() > 0 ) {
 			height = Integer.parseInt( h );
 		}
 
 		// System.out.println("I got cat " + category);
 		if( category == null ) {
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
 
 		float health = 100.0f;
 		int min = 1000000, max = -110001100;
 		String scale = "Unknown";
 
 		boolean latest = true;
 
 		/* For each build, moving backwards */
 		for( MonKitBuildAction a = this; a != null; a = a.getPreviousResult() ) {
 			logger.finest( "Build " + a.getDisplayName() );
 
 			/* Make the x-axis label */
 			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( a.build );
 
 			/* Loop the categories for current build */
 			for( MonKitCategory mkc : a.getMonKitCategories() ) {
 				
 				/* If the category name matches!!! */
 				if( mkc.getName().equalsIgnoreCase( category ) ) {
 
 					/**/
 					MonKitTarget mkt = publisher.getTarget( category );
 
 					Float fu = null;
 					Float fh = null;
 					if( mkt != null ) {
 						fu = new Float( mkt.getUnstable() );
 						fh = new Float( mkt.getHealthy() );
 
 						dsb.add( fh, "<Healthy>", label );
 						if( max < fh ) {
 							max = (int) Math.floor( fh );
 						}
 
 						dsb.add( fu, "<Unstable>", label );
 						if( min > fu ) {
 							min = (int) Math.floor( fu );
 						}
 					}
 					
 					logger.finer( "float unstable: " + fu );
 					logger.finer( "float health: " + fh );
 
 					/* Loop the observations */
 					Float f = 0f;
 					for( MonKitObservation mko : mkc ) {
 						try {
 							f = new Float( mko.getValue() );
 						} catch (NumberFormatException e) {
 							System.err.println( "[MonKitPlugin] Unknown number " + mko.getValue() );
 							continue;
 						}
 						
 						logger.fine( "Observation: " + mko.getName() + "(" + f + ")" );
 						
 						/* Plotting the graph */
 						dsb.add( f, mko.getName(), label );
 
 						if( f.intValue() > max ) {
 							max = f.intValue() + 1;
 						}
 
 						if( f.intValue() < min ) {
 							min = f.intValue();
 							if( min != 0 ) {
 								min--;
 							}
 						}
 
 						/* we're only interested in the last scale */
 						if( latest ) {
 							scale = mkc.getScale();
 						}
 
 						/*
 						 * HEALTH!!! Only consider last build
 						 */
 						if( latest && mkt != null ) {
 							boolean isGreater = fu < fh;
 							logger.finer( "FU=" + fu + ". FH=" + fh + ". ISGREATER=" + isGreater );
 							
 							/* Mark build as unstable */
 							if( ( isGreater && f < fu ) || ( !isGreater && f > fu ) ) {
 								logger.fine( "Build is unstable" );
 								health = 0.0f;
 							} else if( ( isGreater && f < fh ) || ( !isGreater && f > fh ) ) {
 								float diff = fh - fu;
 								float nf1 = f - fu;
 								float inter = ( nf1 / diff ) * 100;
 	
 								logger.finer( "DIFF=" + diff + ". NF1=" + nf1 + ". INTER=" + inter );
 	
 								if( inter < health ) {
 									logger.fine( "INTER: " + inter );
 									health = inter;
 								}
 							}
 						}
 					}
 				}
 			}
 
 			/* Only the latest(first found) build is considered  */
 			latest = false;
 		}
 
 		if( health < 100.0f ) {
 			logger.fine( "HEALTH: " + health );
 			category += " health @ " + ( Math.abs( Math.floor( health * 100 ) ) / 100 ) + "%";
 		}
 
 		ChartUtil.generateGraph( req, rsp, createChart( dsb.build(), category, scale, max, min ), width, height );
 	}
 
 	private JFreeChart createChart( CategoryDataset dataset, String title, String yaxis, int max, int min ) {
 
 		final JFreeChart chart = ChartFactory.createLineChart( title, // chart
 																		// title
 				null, // unused
 				yaxis, // range axis label
 				dataset, // data
 				PlotOrientation.VERTICAL, // orientation
				!publisher.isDisableLegend(), // include legend
 				true, // tooltips
 				false // urls
 		);
 
 		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
 
 		final LegendTitle legend = chart.getLegend();
		if( !publisher.isDisableLegend() ) {
			legend.setPosition( RectangleEdge.RIGHT );
		}
 
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
