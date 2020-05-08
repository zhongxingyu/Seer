 package gov.nih.nci.caintegrator.ui.graphing.chart.plot;
 
 import gov.nih.nci.caintegrator.enumeration.ClinicalFactorType;
 import gov.nih.nci.caintegrator.enumeration.DiseaseType;
 import gov.nih.nci.caintegrator.ui.graphing.data.DataRange;
 import gov.nih.nci.caintegrator.ui.graphing.data.clinical.ClinicalDataPoint;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Shape;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.LegendItem;
 import org.jfree.chart.LegendItemCollection;
 import org.jfree.chart.LegendItemSource;
 import org.jfree.chart.annotations.XYShapeAnnotation;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.title.LegendTitle;
 
 public class ClinicalPlot {
 	
 	private JFreeChart clinicalChart = null;
 	private ClinicalFactorType factor1;
 	private ClinicalFactorType factor2;
 	private Collection<ClinicalDataPoint> dataPoints;
 	//private Map diseaseColorMap = new HashMap();
 
 	public ClinicalPlot(Collection<ClinicalDataPoint> clinicalData, gov.nih.nci.caintegrator.enumeration.ClinicalFactorType factor1, gov.nih.nci.caintegrator.enumeration.ClinicalFactorType factor2) {
 	  this.factor1 = factor1;
 	  this.factor2 = factor2;
 	  this.dataPoints = clinicalData;
 	  
 	  createChart();	  
 	}
 	public JFreeChart getChart() { return clinicalChart; }
 
 	
 	private void createChart() {
 		
 		String xLabel = factor1.toString();
 		String yLabel = factor2.toString();
 			
 		clinicalChart = ChartFactory.createScatterPlot("Clinical Plot",xLabel, yLabel, null,  PlotOrientation.VERTICAL,
 	            true, 
 	            true, 
 	            false );
 		
 		XYPlot plot = (XYPlot) clinicalChart.getPlot();
 		
 		buildLegend();
 		
 	    plot.setNoDataMessage(null);
 	    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
 	    renderer.setToolTipGenerator(new StandardXYToolTipGenerator());
 	    renderer.setUseOutlinePaint(true);
         plot.setRangeCrosshairVisible(false);
 	    plot.setDomainCrosshairVisible(false);
 	       	        
         NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
         NumberAxis rangeAxis  =	(NumberAxis) plot.getRangeAxis();
         
         //should determine axis range using datapoints.
         
         domainAxis.setAutoRangeIncludesZero(false);
         
         //get domain and range of the axis.
         DataRange domainAxisLimits = getDataRange(dataPoints, factor1, true);
         DataRange rangeAxisLimits = getDataRange(dataPoints, factor2,  true);
         
         domainAxis.setRange(domainAxisLimits.getMinRange(), domainAxisLimits.getMaxRange());
         rangeAxis.setRange(rangeAxisLimits.getMinRange(), rangeAxisLimits.getMaxRange());
         
        
         System.out.println("domainAxis=" + domainAxis.getLabel());
         System.out.println("rangeAxis=" + rangeAxis.getLabel());
          
 	    createGlyphsAndAddToPlot(plot);     
 	}
 	
 	/**
 	 * Get the range of values for a given clinical factor
 	 * @param dataPoints
 	 * @param factor
 	 * @return
 	 */
 	private DataRange getDataRange(Collection<ClinicalDataPoint> dataPoints2, gov.nih.nci.caintegrator.enumeration.ClinicalFactorType factor, boolean onlyIncludePositveValues) {
 	  double maxValue = Double.MIN_VALUE;
 	  double minValue = Double.MAX_VALUE;
 	  double value;
 	  ClinicalDataPoint dataPoint;
 	  for (Iterator i=dataPoints.iterator(); i.hasNext(); ) {
 		dataPoint = (ClinicalDataPoint)i.next();
 		value = dataPoint.getFactorValue(factor);
 		if ((value < minValue)&&(value >= 0)) {
 		  minValue = value;
 		}
 		
 		if ((value > maxValue)&&(value >= 0)) {
 		  maxValue = value;
 		}
 	  }
 	  
 	  DataRange range = new DataRange(minValue, maxValue);
 	  return range;
 	}
 	
 	private void createGlyphsAndAddToPlot(XYPlot plot) {
 	  XYShapeAnnotation glyph;
 	  Shape glyphShape;
 	  Color glyphColor;
 	  
 	  ClinicalDataPoint clinicalPoint;
 	  double x, y;
 	  for (Iterator i=dataPoints.iterator(); i.hasNext(); ) {
 	    clinicalPoint = (ClinicalDataPoint) i.next();
 	    
 	    x = clinicalPoint.getFactorValue(factor1);
 	    y = clinicalPoint.getFactorValue(factor2);
 	    
 	    if ((x!= ClinicalDataPoint.MISSING_CLINICAL_FACTOR_VALUE) && 
 	        (y!= ClinicalDataPoint.MISSING_CLINICAL_FACTOR_VALUE)) {
 	    
 	    	Rectangle2D.Double rect = new Rectangle2D.Double();
 		    rect.setFrameFromCenter(x,y, x+1,y+1);
 		    glyphShape = rect;
 		    glyphColor = getColorForDataPoint(clinicalPoint); 
 		    glyph = new XYShapeAnnotation(glyphShape, new BasicStroke(1.0f), Color.BLACK, glyphColor);
		    String tooltip = clinicalPoint.getPatientId() + " " + clinicalPoint.getDiseaseName() + " survivalMonths=" + clinicalPoint.getSurvival();
 		    glyph.setToolTipText(tooltip);
 		    plot.addAnnotation(glyph);
 	    }  
 	  }
 		
 	}
 	
 	private Color getColorForDataPoint(ClinicalDataPoint clinicalPoint) {
         
 	  String diseaseName = clinicalPoint.getDiseaseName();
 	  Color diseaseColor = Color.GRAY;
 	  Color defaultColor = Color.GRAY;
 	  Color retColor = Color.GRAY;
 	  
 	  if (diseaseName != null) {
 	    DiseaseType disease = DiseaseType.valueOf(diseaseName);
 	    diseaseColor = disease.getColor();
 	  }
 	    
 	  int grade = clinicalPoint.getDiseaseGrade();
 	  if (grade > 0) {
 	    for (int i=0; i < grade-1; i++) {
 	      diseaseColor = diseaseColor.brighter();
 	    }
 	  }
 	  retColor = diseaseColor;
 	  
 	  	    
 	  if (retColor == null) {
 	    retColor = defaultColor;
 	  }
 		  
 	  return retColor;
 	}
 	
 	/**
 	 * Build the legend
 	 *
 	 */
 	private void buildLegend() {
 	
 	  LegendTitle legend = clinicalChart.getLegend();
 	  LegendItemSource[] sources = new LegendItemSource[1];
 	  ClinicalLegendItemSource legendSrc = new ClinicalLegendItemSource();
 	  LegendItem item = null;
 	       
       //go through the disease color map and add legend items
       String diseaseName = null;
 	  Color diseaseColor = null;
 	  DiseaseType[] diseases = DiseaseType.values();
 	  for (int i=0; i < diseases.length; i++ ) {
 	    diseaseName = diseases[i].name();
 	    diseaseColor = diseases[i].getColor();
 	    item = new LegendItem(diseaseName, null, null, null, new Line2D.Double(0,0,6,6), new BasicStroke(3.0f), diseaseColor);	 
 	    legendSrc.addLegendItem(item);
 	  }
       
 //      for (Iterator i=diseaseColorMap.keySet().iterator(); i.hasNext(); ) {
 //	    diseaseName = (String) i.next();
 //	    diseaseColor = (Color) diseaseColorMap.get(diseaseName);
 //	    item = new LegendItem(diseaseName, null, null, null, new Line2D.Double(0,0,6,6), new BasicStroke(3.0f), diseaseColor);
 //	    //item = new LegendItem(diseaseName, null, null, null, new Rectangle2D.Double(0,0,6,6), diseaseColor);
 //	    legendSrc.addLegendItem(item);
 //	  }
 		    
 	  sources[0] = legendSrc;
 	  legend.setSources(sources);
 	}
 	
 	/**
 	 * A class for building the legend
 	 * @author harrismic
 	 *
 	 */
 	private class ClinicalLegendItemSource implements LegendItemSource {
 
 		private LegendItemCollection items = new LegendItemCollection();
 		
 		public void addLegendItem(LegendItem item) {
 		  items.add(item); 
 		}
 		
 		public LegendItemCollection getLegendItems() {
 			return items;
 		}
 		
 	}
 	
 }
