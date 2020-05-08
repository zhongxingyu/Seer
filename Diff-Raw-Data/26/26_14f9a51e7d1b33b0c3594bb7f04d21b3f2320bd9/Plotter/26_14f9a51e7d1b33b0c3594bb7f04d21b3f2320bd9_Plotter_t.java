 /**
  * @(#)Plotter.java
  */
 
 package aurora.hwc.report;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.Box;
 import javax.swing.JFrame;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.annotations.XYTextAnnotation;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.block.BlockBorder;
 import org.jfree.chart.plot.CombinedDomainXYPlot;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.LookupPaintScale;
 import org.jfree.chart.renderer.PaintScale;
 import org.jfree.chart.renderer.xy.XYAreaRenderer;
 import org.jfree.chart.renderer.xy.XYBlockRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.title.PaintScaleLegend;
 import org.jfree.data.xy.AbstractXYDataset;
 import org.jfree.data.xy.DefaultXYDataset;
 import org.jfree.data.xy.DefaultXYZDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.ui.RectangleAnchor;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.RectangleInsets;
 import org.jfree.ui.TextAnchor;
 
 import aurora.util.UtilGUI;
 
 
 /**
  * This class uses the JFreeChart library to generate graphics from Slide objects.
  * @author Gabriel Gomes
  */
 public class Plotter {
 
 	public JFreeChart makeSlideChart(Slide S){
 		int i;
 		Range xlimits = new Range();
 		Range ylimits = new Range();
 
 		JFreeChart chart;
 		if(S.plots.size()==1){		// single plot
 			XYPlot plot = makeplot(S.plots.get(0),xlimits,ylimits);
 			if(!xlimits.isempty())
 				plot.getDomainAxis().setRange(xlimits.lower,xlimits.upper);
 			if(!ylimits.isempty())
 				plot.getRangeAxis().setRange(ylimits.lower,ylimits.upper);
 			plot.setBackgroundPaint(Color.white);
 			chart = new JFreeChart(null, null,plot, S.plots.get(0).showlegend);
 			if(S.plots.get(0).elements.get(0).type==PlotElement.Type.contour)
 				configureContourLegendandAnnotation(chart,plot,S.plots.get(0));
 		}
 		else{						// multiple plots
 			CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis(S.plots.get(0).xlabel));
 			for(i=0;i<S.plots.size();i++){
 				XYPlot subplot = makeplot(S.plots.get(i),xlimits,ylimits);
 				subplot.setRangeAxis(new NumberAxis(S.plots.get(i).ylabel));
 				plot.add(subplot);				
 				if(!ylimits.isempty())
 					subplot.getRangeAxis().setRange(ylimits.lower,ylimits.upper);
 			}
 			if(!xlimits.isempty())
 				plot.getDomainAxis().setRange(xlimits.lower,xlimits.upper);
 			plot.setBackgroundPaint(Color.white);
 			chart = new JFreeChart(null, null,plot, S.plots.get(0).showlegend);
 			if(S.plots.get(0).elements.get(0).type==PlotElement.Type.contour){
 				for(i=0;i<S.plots.size();i++){
 					configureContourLegendandAnnotation(chart,(XYPlot) plot.getSubplots().get(i),S.plots.get(i));
 				}
 			}
 		}
 		chart.setBackgroundPaint(Color.white);
 		return chart;
 	}
 	
 	private XYPlot makeplot(Plot P,Range xlimits,Range ylimits){
 
 		int i,j,k;
 		Range zlimits=null;
 		NumberAxis xAxis = new NumberAxis(P.xlabel);
 		xAxis.setAutoRangeIncludesZero(false);
 		NumberAxis yAxis = new NumberAxis(P.ylabel);
 		yAxis.setAutoRangeIncludesZero(false);
 		
 		XYPlot plot = new XYPlot(null,xAxis,yAxis,null);
 		
 		// populate dataset
 		for(i=0;i<P.elements.size();i++){
 			PlotElement e = P.elements.get(i);
 
 			AbstractXYDataset dataset = null;
 			
 			switch(e.type){
 			
 			case contour:
 				zlimits = new Range();
 				dataset = new DefaultXYZDataset();
 				int xSize = e.xdata.size();
 				int ySize = e.ydata.get(0).size();
 				double[][] xyzdata = new double[3][xSize*ySize];
 				int c=0;
 				for(j=0;j<xSize;j++){
 					for(k=0;k<ySize;k++){
 						xyzdata[0][c] = e.xdata.get(j);
 						xyzdata[1][c] = e.ydata.get(0).get(k);
 						xyzdata[2][c] = e.zdata.get(j).get(k);
 						zlimits.include(e.zdata.get(j).get(k));
 						c++;
 					}
 				}
 				((DefaultXYZDataset) dataset).addSeries(0,xyzdata);
 				xlimits.include(e.xdata.firstElement());
 				xlimits.include(e.xdata.lastElement());
 				ylimits.include(e.ydata.get(0).firstElement());
 				ylimits.include(e.ydata.get(0).lastElement());
 				break;
 				
 			case multiline:
 			case scatter:
 				dataset = new DefaultXYDataset();
 				for(j=0;j<e.ydata.size();j++){
 					XYSeries data = new XYSeries(e.legend.get(j));
 					for(k=0;k<e.xdata.size();k++){
 						data.add(e.xdata.get(k),e.ydata.get(j).get(k));
 						xlimits.include(e.xdata.get(k));
 					}
 					((DefaultXYDataset) dataset).addSeries(e.legend.get(j),data.toArray());
 				}
 				break;
 			
 			case minmax:
 				System.out.println("ERROR: NOT IMPLEMENTED");
 				break;
 			}
 
 			plot.setDataset(i,dataset);
 			
 			// assign renderer to this element
 			boolean dofill = e.fill;
 			boolean isscatter = e.type==PlotElement.Type.scatter;
 			boolean iscontour = e.type==PlotElement.Type.contour;
 			
 			if(dofill & !isscatter & !iscontour)
 				plot.setRenderer(i,new XYAreaRenderer());
 
 			if(!dofill & !isscatter & !iscontour){
 				XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
 				renderer.setShapesVisible(false);
 				plot.setRenderer(i,renderer);
 			}
 
 			if(isscatter){
 				XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
 				renderer.setLinesVisible(false);
 				plot.setRenderer(i,renderer);
 			}
 			
 			if(iscontour){
 				
 				XYBlockRenderer renderer = new XYBlockRenderer();
 		        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
 		        
 		        float minVal = zlimits.lower;
 		        float maxVal = zlimits.upper;
		        if (maxVal < minVal) {
		        	minVal = 0;
		        	maxVal = 0;
		        }
		        LookupPaintScale pScale = new LookupPaintScale(minVal,maxVal+aurora.util.Util.EPSILON,Color.white);
 		        Color[] clr = UtilGUI.byrColorScale();		        
 		        double delta = (maxVal - minVal)/(clr.length - 1);
 		        double value = minVal;
 		        pScale.add(value, clr[0]);
 		        value += Double.MIN_VALUE;
 		        for (j=1;j<clr.length;j++) {
 		        	pScale.add(value, clr[j]);
 		        	value += delta;
 		        }
 		        renderer.setPaintScale(pScale);
 		        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
 				plot.setRenderer(i,renderer);
 			}
 			
 			// colors and stroke
 			XYItemRenderer renderer = plot.getRenderer(i);
 			for(j=0;j<e.ydata.size();j++){
 				float r = Integer.parseInt(e.colors.get(j).substring(1,3),16)/255f;
 				float g = Integer.parseInt(e.colors.get(j).substring(3,5),16)/255f;
 				float b = Integer.parseInt(e.colors.get(j).substring(5,7),16)/255f;
 				renderer.setSeriesPaint(j,new Color(r,g,b,0.7f));
 				renderer.setSeriesStroke(j, new BasicStroke(2.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 			}
 		}
 
 		plot.setDomainGridlinePaint(Color.black);
 		plot.setRangeGridlinePaint(Color.black);
 		return plot;
 		
 	}
 	
 	public void showchart(JFreeChart chart,String title){
 		JFrame plotframe = new JFrame();
 		ChartPanel cp = new ChartPanel(chart);
 		cp.setPreferredSize(new Dimension(300, 300));
 		Box perfPanel = Box.createVerticalBox();
 		perfPanel.add(cp);
 		plotframe.setContentPane(new ChartPanel(chart));
 		plotframe.setTitle(title);
 		plotframe.setSize(640, 430);
 		plotframe.setVisible(true);	
 		plotframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
 	}
 	
 	public void savecharttoPNG(JFreeChart chart,File file){
 		try {
 			ChartUtilities.saveChartAsPNG(file,chart,720,500);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void configureContourLegendandAnnotation(JFreeChart chart,XYPlot plot,Plot P){
 		
 		double vert = plot.getRangeAxis().getLowerBound() + plot.getRangeAxis().getRange().getLength()*0.05;
 		double horz = plot.getDomainAxis().getLowerBound() + plot.getDomainAxis().getRange().getLength()*0.01;
 		
 		XYTextAnnotation text = new XYTextAnnotation(P.zlabel,horz,vert);
 		text.setTextAnchor(TextAnchor.BOTTOM_LEFT);
 		text.setFont(new Font ("Arial",Font.BOLD, 12));
 		plot.addAnnotation(text);
 		
 		if(!P.showlegend)
 			return;
 		XYBlockRenderer renderer = (XYBlockRenderer) plot.getRenderer();
 		PaintScale pScale = renderer.getPaintScale();
         NumberAxis scaleAxis = new NumberAxis(P.zlabel);
         scaleAxis.setRange(pScale.getLowerBound(),pScale.getUpperBound());
         PaintScaleLegend psl = new PaintScaleLegend(pScale, scaleAxis);
         psl.setMargin(new RectangleInsets(3, 10, 3, 10));
         psl.setPosition(RectangleEdge.BOTTOM);
         psl.setAxisOffset(5.0);
         psl.setFrame(new BlockBorder(Color.GRAY));
         chart.addSubtitle(psl);
 	}
 	
 	private class Range {
 		public float lower;
 		public float upper;
 		public Range(){
 			upper = Float.NEGATIVE_INFINITY;
 			lower = Float.POSITIVE_INFINITY;
 		}
 		public void include(float x){
 			lower = x<lower ? x : lower;
 			upper = x>upper ? x : upper;
 		}
 		public boolean isempty(){
 			return lower>=upper;
 		}
 	}
 
 }
