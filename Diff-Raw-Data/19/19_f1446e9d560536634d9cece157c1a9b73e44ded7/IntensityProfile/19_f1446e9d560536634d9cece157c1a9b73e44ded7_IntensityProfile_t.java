 package plugins.oeway.viewers;
 /**
  *  this class is modified from the plugin Intensity Profile by Fab.
  * 
  * 
  * 
  * @author Wei Ouyang
  * 
  */
 
 import icy.canvas.IcyCanvas;
 import icy.gui.component.IcySlider;
 import icy.gui.util.ComponentUtil;
 import icy.gui.util.GuiUtil;
 import icy.image.IcyBufferedImage;
 import icy.roi.BooleanMask2D;
 import icy.roi.ROI;
 import plugins.kernel.roi.roi2d.ROI2DLine;
 import plugins.kernel.roi.roi2d.ROI2DShape;
 import icy.sequence.DimensionId;
 import icy.sequence.Sequence;
 import icy.system.thread.ThreadUtil;
 import icy.type.collection.array.Array1DUtil;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
 import org.jfree.chart.plot.Marker;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.ValueMarker;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataItem;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.RectangleAnchor;
 import org.jfree.ui.TextAnchor;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 public class IntensityProfile  {
 
     final IcySlider slider;
 	//JButton exportToExcelButton = new JButton("Export to excel");
 	JCheckBox graphOverZ = new JCheckBox("Graph Z");
 	JButton rowOColBtn = new JButton("row");
 	JButton exportToFileBtn = new JButton("Export to file");
 	JLabel indexLbl = new JLabel("0");
 	JLabel maxIndexLbl = new JLabel("0");
 	public PanningChartPanel chartPanel ;
 	public JFreeChart chart;
 	XYSeriesCollection xyDataset = new XYSeriesCollection();
 	
 	String OPTION_meanAlongZ = "Mean along Z";
 	String OPTION_meanAlongT = "Mean along T";	
 	
 	ArrayList<Marker> markerDomainList = new ArrayList<Marker>();
 	ArrayList<Marker> markerRangeList = new ArrayList<Marker>();
 	
 	CheckComboBox optionComboBox;
 	
 	ROI associatedROI = null;
 	private Sequence sequence;
 	
 	boolean rowMode = true;
 	int lineIndex = 0 ;
 	
 	IcyCanvas mainCanvas;
 	public double posX = 0.0;
 	public double posY = 0.0;
 	
 	double pixelSize = 1.0;
 	int paintersSize = 0;
 	public IntensityProfile(IcyCanvas mainCav,Sequence seq){
 		
 		sequence = seq;
 
 		// option
 		Set<String> mapValue = new HashSet<String>(); 
 		mapValue.add( OPTION_meanAlongZ );
 		mapValue.add( OPTION_meanAlongT );		
 		
 		optionComboBox = new CheckComboBox( mapValue );		
 		ComponentUtil.setFixedHeight( optionComboBox , 22 );		
 		
 		optionComboBox.addSelectionChangedListener( new CheckComboBoxSelectionChangedListener() {
 			
 			@Override
 			public void selectionChanged(int idx) {
 				updateChart();
 			}
 		});
 		//row or column button
 
         rowOColBtn.setFocusPainted(false);
         rowOColBtn.setPreferredSize(new Dimension(65, 22));
         rowOColBtn.setMaximumSize(new Dimension(80, 22));
         rowOColBtn.setMinimumSize(new Dimension(65, 22));
         rowOColBtn.setMargin(new Insets(2, 8, 2, 8));
         rowOColBtn.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String xlabel;
                 //String title;
             	if(rowOColBtn.getText()=="row")
             	{
             		rowOColBtn.setText("column");
             		rowMode = false;	
   
             		xlabel = "Column (Y)";
             		//title = "Intensity Profile of Each Column";
   
             	}
             	else
             	{
             		rowOColBtn.setText("row");
             		rowMode = true;
 
             		xlabel = "Row (X)";
             		//title = "Intensity Profile of Each Row";
   
             	}
             	//chart.setTitle(title);
             	chart.getXYPlot().getDomainAxis().setLabel(xlabel);
             	updateXYNav();
             	updateChart();
             }
         });
         rowOColBtn.setToolTipText("slide in row or column");
   
         exportToFileBtn.setFocusPainted(false);
         exportToFileBtn.setPreferredSize(new Dimension(180, 22));
         exportToFileBtn.setMaximumSize(new Dimension(240, 22));
         exportToFileBtn.setMinimumSize(new Dimension(120, 22));
         exportToFileBtn.setMargin(new Insets(2, 8, 2, 8));
         exportToFileBtn.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
         		int currentT = mainCanvas.getPositionT();
         		int currentZ = mainCanvas.getPositionZ();
         		
 
         		JFileChooser jdir = new JFileChooser();  
 
                 jdir.setFileSelectionMode(JFileChooser.FILES_ONLY);  
  
                 if (JFileChooser.APPROVE_OPTION == jdir.showOpenDialog(null)) {
                     String path = jdir.getSelectedFile().getAbsolutePath();
                     if(!path.contains("."))
                     	path+=".txt";
                     BufferedWriter writer = null;
                     try {
                         //create a temporary file
 
                         File outputFile = new File(path);
                         writer = new BufferedWriter(new FileWriter(outputFile));
 //                		System.out.println("-----------------------------------------");
 //                		System.out.println("---------- Time:"+ currentT +", Z:"+currentZ+" ---------");
                         int maxSize = 0;
                 		for(Object s:xyDataset.getSeries())
                 		{
                 			if(maxSize<((XYSeries)s).getItemCount())
                 				maxSize=((XYSeries)s).getItemCount();
                 		}
                         
                 		String[] dataArr = new String[maxSize+1];
                 		
           				for( int i = 0 ; i <maxSize+1  ; i++ )
             			{
           					dataArr[i] ="";
             			}
                         
             			for( int c= 0 ; c < xyDataset.getSeriesCount(); c++ )
                 		{
             				XYSeries seriesXY = xyDataset.getSeries(c);	
                 			List<XYDataItem> it = seriesXY.getItems();
                 			int size = it.size();
                 			dataArr[0] +=seriesXY.getKey().toString()+"(X)\t"+seriesXY.getKey().toString()+"(Y)\t";
             				for( int i = 1 ; i <maxSize+1  ; i++ )
                 			{
             					if(size>i)
             					{
             						//System.out.println(it.get(i).getXValue() + " , "+ it.get(i).getYValue());
             						dataArr[i]+=(it.get(i).getXValue() + "\t"+ it.get(i).getYValue()+"\t");
             					}
             					else
             					{
             						dataArr[i]+=" \t \t";
             					}
                 			}
 
                 		}
             			for(String s:dataArr)
             			{
 
             				writer.write(s+"\n");
 
             			}
 
                     } catch (Exception e1) {
                         e1.printStackTrace();
                     } finally {
                         try {
                             // Close the writer regardless of what happens...
                             writer.close();
                         } catch (Exception e1) {
                         }
                     }
                     
                     
                 }  
                 
             }
         });
         exportToFileBtn.setToolTipText("Print current line data to the console of Icy.");
         
 
         
         indexLbl.setHorizontalAlignment(JLabel.RIGHT );
         maxIndexLbl.setHorizontalAlignment(JLabel.RIGHT );
         
 		//slide
 		slider = new IcySlider(SwingConstants.HORIZONTAL);
         slider.setFocusable(false);
         slider.setMaximum(0);
         slider.setMinimum(0);
         slider.setToolTipText("Move cursor to navigate in T dimension");
         slider.addChangeListener(new ChangeListener()
         {
             @Override
             public void stateChanged(ChangeEvent e)
             {
             	try
             	{
 	            	indexLbl.setText(String.valueOf(slider.getValue()));
 	            	lineIndex = slider.getValue();
 	            	if(rowMode)
 	            	{
 	            		posY = lineIndex;
 	            		mainCanvas.mouseImagePositionChanged(DimensionId.Y);
 	            	}
 	            	else
 	            	{
 	            		posX = lineIndex;
 	            		mainCanvas.mouseImagePositionChanged(DimensionId.X);
 	            	}
             	}
             	finally
             	{
             		updateChart();            		
             	}
 
             
             }
         });
         ComponentUtil.setFixedHeight(slider, 22);
         String xlabel;
         //String title;
     	if(rowMode)
     	{
     		xlabel = "Row (X)";
     		//title = "Intensity Profile of Each Row";
     	}
     	else
     	{
     		xlabel = "Column (Y)";
     		//title = "Intensity Profile of Each Column";
     	}
         
 		// Chart
 		chart = ChartFactory.createXYLineChart(
 				"",xlabel, "Intensity Value", xyDataset,
				PlotOrientation.VERTICAL, true, true, true);
 		chartPanel = new PanningChartPanel(chart, 1024, 500, 500, 200, 10000, 10000, false, false, true, false, true, true);		
 		
 		chartPanel.addChartMouseListener(new ChartMouseListener() {
 			@Override
 			public void chartMouseClicked(ChartMouseEvent arg0) {
 			       chart.getXYPlot().setDomainCrosshairVisible(true);
 			       chart.getXYPlot().setDomainCrosshairLockedOnData(true);
 			       chart.getXYPlot().setRangeCrosshairVisible(true);
 			       chart.getXYPlot().setRangeCrosshairLockedOnData(true);
 			}
 
 			@Override
 			public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
 
 				Point2D p = chartPanel.translateScreenToJava2D(chartMouseEvent.getTrigger().getPoint());
 				Rectangle2D plotArea = chartPanel.getScreenDataArea();
 				XYPlot plot = (XYPlot) chart.getPlot(); // your plot
 				double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
 				//double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
             	if(rowMode)
             	{
             		posX = chartX;//pos.getX();//chart.getXYPlot().getDomainCrosshairValue();
             		mainCanvas.mouseImagePositionChanged(DimensionId.X);
             	}
             	else
             	{
             		posY = chartX;//pos.getX();//chart.getXYPlot().getDomainCrosshairValue();
             		mainCanvas.mouseImagePositionChanged(DimensionId.Y);
             	}
 			}
 		});
 		    
 		//disable autorange
         chart.getXYPlot().getRangeAxis(0).setAutoRange(true);
         chart.getXYPlot().getDomainAxis(0).setAutoRange(true);	
         
  
         
         chart.getXYPlot().setDomainCrosshairPaint(Color.red);
         chart.getXYPlot().setRangeCrosshairPaint(Color.red);
 		
         XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
         renderer.setLegendItemToolTipGenerator(
             new StandardXYSeriesLabelGenerator("Legend {0}"));
         
 		mainCanvas = mainCav;
 		//add to canvas
 		mainCanvas.add( GuiUtil.createPageBoxPanel(GuiUtil.createLineBoxPanel(optionComboBox,exportToFileBtn),chartPanel,GuiUtil.createLineBoxPanel(rowOColBtn,slider,maxIndexLbl) )) ;
 
 		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
         chart.setBackgroundPaint(Color.white);
 
 //        final StandardLegend legend = (StandardLegend) chart.getLegend();
   //      legend.setDisplaySeriesShapes(true);
         
         // get a reference to the plot for further customisation...
         final XYPlot plot = chart.getXYPlot();
         
         plot.setBackgroundPaint(Color.white );
         
     //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
 
         
         // set the stroke for each series...
         plot.getRenderer().setSeriesStroke(
             0, 
 //            new BasicStroke(
 //                1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
 //                10.0f, new float[] {10.0f, 1.0f}, 0.0f
 //            )
             new BasicStroke(1.0f)
         );
 
         plot.getRenderer().setSeriesShape(0, null);
         //plot.getRenderer().setSeriesPaint(0, Color.blue);
 
         updateXYNav();
     	updateChart();
 	}
 	
 
 	Runnable updateRunnable;
 	
 	public BufferedImage getImage()
 	{
 			BufferedImage objBufferedImage=chart.createBufferedImage(1024,800);
 			return objBufferedImage;
 
 	}
 	
 	private int lastChannelSize=0;
 	public void updateChannelOptions()
 	{
 		int size = sequence.getSizeC();
 		if(lastChannelSize != size)
 		{
 			// update options
 			Set<String> mapValue = new LinkedHashSet<String>(); 
 			mapValue.add( OPTION_meanAlongZ );
 			mapValue.add( OPTION_meanAlongT );	
 			for(int i=0;i<size;i++){
 					mapValue.add("channel " +i+"("+sequence.getChannelName(i)+")");
 			}
 			optionComboBox.resetObjs(mapValue, false);
 			Collection<String> selectedValues= new ArrayList<String>();
 			for(int i=0;i<size;i++){
 				
 				if(i<sequence.getSizeC())
 					selectedValues.add("channel " +i+"("+sequence.getChannelName(i)+")");
 			}
 			optionComboBox.addSelectedItems(selectedValues);
 			lastChannelSize = size;
 		}
 	}
 	public void updateChannelPainters()
 	{
 		//set painter
 		final XYPlot plot = chart.getXYPlot();
 		int size = xyDataset.getSeries().size();
 		if(paintersSize!=size)
 		{
 			if(size ==1 ){
 				plot.getRenderer().setSeriesPaint(0, Color.blue);
 			}else if(size==2)
 			{
 				plot.getRenderer().setSeriesPaint(0, Color.blue);
 				plot.getRenderer().setSeriesPaint(1, Color.red);
 			}
 			else if(size==3)
 			{
 				plot.getRenderer().setSeriesPaint(0, Color.red);
 				plot.getRenderer().setSeriesPaint(1, Color.green);
 				plot.getRenderer().setSeriesPaint(1, Color.blue);
 			}
 			else{
 				
 				for(int i=paintersSize;i<size;i++){
 					
 					int R = (int)(Math.random()*256);
 					int G = (int)(Math.random()*256);
 					int B= (int)(Math.random()*256);
 					Color color = new Color(R, G, B); //random color, but can be bright or dull
 		
 					//to get rainbow, pastel colors
 					Random random = new Random();
 					final float hue = random.nextFloat();
 					final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
 					final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
 					color = Color.getHSBColor(hue, saturation, luminance);
 					plot.getRenderer().setSeriesPaint(i, color);
 					
 				}
 			}
 
 
 		}
 		paintersSize = size;
 		
 		
 	}
 	public void updateChart()
 	{
 		chart.setAntiAlias( true );
 		chart.setTextAntiAlias( true );
 		updateChannelOptions();
 		
 	//	updateChartThreaded();
 		if ( updateRunnable == null )
 		{
 			updateRunnable = new Runnable() {
 
 				@Override
 				public void run() {
 					try
 					{
 						updateChartThreaded();
 						
 					}
 					catch(Exception e)
 					{
 						
 					}
 				}
 			};
 		}
 		
 		ThreadUtil.bgRunSingle( updateRunnable );
 		updateChannelPainters();
 	}
 	
 	int runCount =0;
 	private void updateChartThreaded() { 	
 		
 		// check if ROI still exist in a sequence
 		
 		//removeAllHorizontalRangeMarker();
 		
 		if ( associatedROI != null )
 		{
 			if ( associatedROI.getSequences().size() == 0 )
 			{
 				associatedROI = null;
 			}
 		}
 		
 		// create dataSet		
 
 		//xyDataset.removeAllSeries();		
 
 		// check z to display
 		
 		int currentZ = 0;
 		int currentT = 0;
 		
 		currentT = mainCanvas.getPositionT();
 		currentZ = mainCanvas.getPositionZ();
 		
 		if ( currentZ < 0 ) currentZ = 0; // 3D return -1.
 		if ( currentT < 0 ) currentT = 0;
 		
 		
 		if(rowMode)
 		{
 			if (lineIndex >sequence.getHeight()-1)
 			{
 				lineIndex = sequence.getHeight()-1;
 				slider.setValue( lineIndex);
 				return;
 			}
 			Point2D p1 = new Point2D.Double(0,lineIndex );
 			Point2D p2 = new Point2D.Double(sequence.getWidth(),lineIndex);
 			associatedROI = new ROI2DLine(p1,p2);
 			pixelSize = sequence.getPixelSizeX();
 		}else
 		{
 			if (lineIndex >sequence.getWidth()-1)
 			{
 				lineIndex = sequence.getWidth()-1;
 				slider.setValue( lineIndex);
 				return;
 			}
 			Point2D p1 = new Point2D.Double(lineIndex, 0);
 			Point2D p2 = new Point2D.Double(lineIndex, sequence.getHeight());
 			associatedROI = new ROI2DLine(p1,p2);
 			pixelSize = sequence.getPixelSizeY();
 		}
 		
 		sequence.addROI(associatedROI);
 		
 		if ( associatedROI != null )
 		{
 
 			// compute chart
 	        try
 	        {
 				Sequence sequence = associatedROI.getSequences().get( 0 );
 
 				ROI2DShape roiShape = (ROI2DShape) associatedROI;
 				ArrayList<Point2D> pointList = roiShape.getPoints();
 				
 				if(pointList.size()>1)
 				{
 					
 					
 					computeLineProfile( pointList, currentT, currentZ , sequence );
 					
 					if ( optionComboBox.isItemSelected( OPTION_meanAlongZ ) )
 					{
 						computeZMeanLineProfile( pointList, currentT, sequence );
 					}
 					if ( optionComboBox.isItemSelected( OPTION_meanAlongT ) )
 					{
 						computeTMeanLineProfile( pointList, currentZ , sequence );
 					}
 
 					updateChannelPainters();
 					chart.fireChartChanged();
 
 				}
 				
 				
 				
 //				if(runCount >10)
 //				{
 //					//disable autorange
 //					chart.getXYPlot().getRangeAxis(0).setAutoRange(false);
 //					chart.getXYPlot().getDomainAxis(0).setAutoRange(false);	
 //				}
 //				else
 //				{
 //					//disable autorange
 //					chart.getXYPlot().getRangeAxis(0).setAutoRange(true);
 //					chart.getXYPlot().getDomainAxis(0).setAutoRange(true);	
 //					runCount++;
 //				}
 				
 				
 	        }
 	        finally
 	        {
 			sequence.removeROI(associatedROI);
 			associatedROI= null;
 	        }
 			
 		}
 
 
 
         
 
 	}
 	public void updateXYNav()
 	{
 		if(rowMode)
 		{
 			rowOColBtn.setText("row");
 			slider.setMaximum(sequence.getHeight()-1);
         	slider.setMinimum(0);
         	if(slider.getValue()>sequence.getHeight()-1)
         		slider.setValue(0);
         	indexLbl.setText(String.valueOf(slider.getValue()));
         	maxIndexLbl.setText(String.valueOf(sequence.getHeight()-1));
         	
 		}
 		else
 		{
 			rowOColBtn.setText("column");
 			slider.setMaximum(sequence.getWidth()-1);
         	slider.setMinimum(0);
         	if(slider.getValue()>sequence.getWidth()-1)
         		slider.setValue(0);
         	indexLbl.setText(String.valueOf(slider.getValue()));
         	maxIndexLbl.setText(String.valueOf(sequence.getWidth()-1));
 		}
 	}
 
 	private void getValueForSurfaceAllComponent(BooleanMask2D boolMask, IcyBufferedImage image) {
 		
 		for( int c= 0 ; c < image.getSizeC() ; c++ )
 		{
 			XYSeries seriesXY = new XYSeries("Mean of surface c"+c );
 			double value = getValueForSurface( boolMask, image , c );
 			drawHorizontalSurfaceValue( value , c );
 
 			seriesXY.add( 0, value );
 			//seriesXY.add( 100, value );
 			xyDataset.addSeries(seriesXY);
 		}			
 		
 		
 	}
 
 	private void computeSurfaceProfileAlongZ(BooleanMask2D boolMask, Sequence sequence , int currentT ) {
 
 		for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 		{
 			XYSeries seriesXY = new XYSeries("Mean along Z c" +c );
 			for ( int z = 0 ; z< sequence.getSizeZ() ; z++ )
 			{
 				double value = getValueForSurface( boolMask, sequence.getImage( currentT , z ) , c );				
 				seriesXY.add( z , value );
 			}
 			xyDataset.addSeries(seriesXY);
 		}			
 		
 	}
 
 	private void computeSurfaceProfileAlongT(BooleanMask2D boolMask, Sequence sequence , int currentZ ) {
 
 		for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 		{
 			XYSeries seriesXY = new XYSeries("Mean along T c" +c + " z"+currentZ );
 			for ( int t = 0 ; t< sequence.getSizeT() ; t++ )
 			{
 				double value = getValueForSurface( boolMask, sequence.getImage( t , currentZ ) , c );				
 				seriesXY.add( t , value );
 			}
 			xyDataset.addSeries(seriesXY);
 		}			
 		
 	}
 
 	/**
 	 * Mean value of the surface
 	 */
 	private double getValueForSurface(BooleanMask2D boolMask, IcyBufferedImage image , int component )
 	{
 		double result=0;
 		
 		double[] imageData = Array1DUtil.arrayToDoubleArray( image.getDataXY( component ) , image.isSignedDataType() );
 
 		int minX = boolMask.bounds.x;
 		int minY = boolMask.bounds.y;
 		int maxX = boolMask.bounds.width + minX ;
 		int maxY = boolMask.bounds.height + minY ;
 		
 		int imageWidth = image.getWidth();
 		
 		int offset = 0;
 		for ( int y = minY ; y< maxY ; y++ )
 		{
 			for ( int x = minX ; x< maxX ; x++ )
 			{
 				//boolMask.contains(x, y)
 				//if ( boolMask.mask[(y-minY)*width+(x-minX)] == true )
 				if ( boolMask.mask[offset++] == true )
 				{
 					result+= imageData[y*imageWidth+x];
 				}
 
 			}
 		}
 		if ( offset != 0 )
 		{
 			result /= (double)offset;
 		}
 		
 		return result;
 	}
 
 	private void computeZMeanLineProfile(ArrayList<Point2D> pointList, int currentT , Sequence sequence) {
 		
 		if ( sequence.getSizeZ() > 1 )
 		{
 			double [][] result = null;
 			for ( int z = 0 ; z < sequence.getSizeZ() ; z++ )
 			{
 				Profile profile = getValueForPointList( pointList , 
 						sequence.getImage( currentT , z ) );
 
 				if ( result == null )
 				{
 					result = new double[profile.values.length][profile.values[0].length];
 				}
 
 				for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 				{
 					for( int i = 0 ; i < profile.values[c].length ; i++ )
 					{
 						result[c][i]+= profile.values[c][i];
 					}
 				}					
 			}
 			for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 			{
 				for( int i = 0 ; i < result[c].length ; i++ )
 				{
 					result[c][i] /= sequence.getSizeZ();
 				}
 			}
 
 			for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 			{
 				boolean found = false;
 				XYSeries seriesXY2 =null;
 				try{
 					seriesXY2=xyDataset.getSeries("Mean along Z c" +c);
 					
 					found = true;
 				}
 				catch(Exception e)
 				{
 	
 				}
 				XYSeries seriesXY = new XYSeries("Mean along Z c" +c );		
 				for( int i = 0 ; i < result[c].length ; i++ )
 				{							
 					seriesXY.add(i*pixelSize, result[c][i]);
 				}						
 				xyDataset.addSeries(seriesXY);
 				if(found && seriesXY2 != null)
 					xyDataset.removeSeries(seriesXY2);	
 
 			}
 		}
 		
 	}
 
 
 	private void computeTMeanLineProfile(ArrayList<Point2D> pointList, int currentZ, Sequence sequence) {
 		
 		if ( sequence.getSizeT() > 1 )
 		{
 			double [][] result = null;
 			for ( int t = 0 ; t < sequence.getSizeT() ; t++ )
 			{
 				Profile profile = getValueForPointList( pointList , 
 						sequence.getImage( t , currentZ ) );
 
 				if ( result == null )
 				{
 					result = new double[profile.values.length][profile.values[0].length];
 				}
 
 				for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 				{
 					for( int i = 0 ; i < profile.values[c].length ; i++ )
 					{
 						result[c][i]+= profile.values[c][i];
 					}
 				}					
 			}
 			for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 			{
 				for( int i = 0 ; i < result[c].length ; i++ )
 				{
 					result[c][i] /= sequence.getSizeT();
 				}
 			}
 
 		
 			for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 			{			
 				boolean found = false;
 				XYSeries seriesXY2 =null;
 				try{
 					seriesXY2=xyDataset.getSeries("Mean along T c" +c );
 					
 					found = true;
 				}
 				catch(Exception e)
 				{
 	
 				}
 				XYSeries seriesXY = new XYSeries("Mean along T c" +c );		
 				for( int i = 0 ; i < result[c].length ; i++ )
 				{							
 					seriesXY.add(i*pixelSize, result[c][i]);
 				}						
 				xyDataset.addSeries(seriesXY);
 				if(found && seriesXY2 != null)
 					xyDataset.removeSeries(seriesXY2);				
 			}
 
 
 			
 
 		}
 		
 	}
 	
 	private void computeLineProfile(ArrayList<Point2D> pointList, int currentT, int currentZ, Sequence sequence) {
 				
 		Profile profile = getValueForPointList( pointList , sequence.getImage( currentT , currentZ ) );
 
 		drawVerticalROIBreakBar( profile );
 
 		boolean found=false;
 		for( int c= 0 ; c < sequence.getSizeC() ; c++ )
 		{
 			XYSeries seriesXY =null;
 			try{
 				seriesXY=xyDataset.getSeries("channel " +c);
 				
 				found = true;
 			}
 			catch(Exception e)
 			{
 
 			}
 		
 			if ( optionComboBox.isItemSelected( "channel " +c+"("+sequence.getChannelName(c)+")" ))
 			{
 				XYSeries seriesXY2 = new XYSeries("channel " +c);	
 				for( int i = 0 ; i < profile.values[c].length ; i++ )
 				{							
 					//System.out.println(i*pixelSize + " , "+ profile.values[c][i]);
 					seriesXY2.add(i*pixelSize,  profile.values[c][i]);
 				}
 
 				xyDataset.addSeries(seriesXY2);
 			}
 
 			if(found && seriesXY != null)
 				xyDataset.removeSeries(seriesXY);
 			
 		}
 		
 	}
 
 	private void drawVerticalROIBreakBar(Profile profile) {
 
 		final XYPlot plot = chart.getXYPlot();
 
 		for ( Marker marker : markerDomainList )
 		{
 			plot.removeDomainMarker(marker);
 		}
 		
 		if ( profile.roiLineBreaks.size() <=1 ) return;
 		
 		int nb = 1;
 		for ( Integer i : profile.roiLineBreaks )
 		{
 			final Marker start = new ValueMarker( i );
 			markerDomainList.add( start );
 			start.setPaint(Color.black);
 			start.setLabel( "" + nb );
 			start.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
 			start.setLabelTextAnchor(TextAnchor.TOP_LEFT);
 			plot.addDomainMarker(start);
 			nb++;
 		}
 		
 	}
 
 	void removeAllHorizontalRangeMarker()
 	{
 		ThreadUtil.invokeNow( new Runnable() {
 			
 			@Override
 			public void run() {
 
 				final XYPlot plot = chart.getXYPlot();
 				for ( Marker marker : markerRangeList )
 				{
 					plot.removeRangeMarker(marker);
 				}		
 				
 			}
 		} );
 	}
 	private void drawHorizontalSurfaceValue( final double value , final int c ) {
 
 		ThreadUtil.invokeNow( new Runnable() {
 			
 			@Override
 			public void run() {
 
 				final XYPlot plot = chart.getXYPlot();
 				
 				final Marker start = new ValueMarker( value );
 				markerRangeList.add( start );
 				start.setLabel("channel "+c + " mean value: " +value );
 				
 				start.setPaint(Color.black);
 				switch ( c ) {
 				case 0:
 					start.setPaint(Color.red);
 					start.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
 					start.setLabelTextAnchor(TextAnchor.TOP_LEFT);
 					break;
 				case 1:
 					start.setPaint(Color.green.darker() );	
 					start.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
 					start.setLabelTextAnchor(TextAnchor.TOP_LEFT);
 					break;
 				case 2:
 					start.setPaint(Color.blue);	
 					start.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
 					start.setLabelTextAnchor(TextAnchor.TOP_LEFT);
 					break;
 				}
 				start.setLabelPaint( start.getPaint() );
 				plot.addRangeMarker(start);
 			}
 		});
 		
 		
 	}
 	
 	class Profile
 	{
 		double[][] values;
 		ArrayList<Integer> roiLineBreaks = new ArrayList<Integer>();	
 	}
 	
 	private Profile getValueForPointList( ArrayList<Point2D> pointList , IcyBufferedImage image ) {
 				
 		ArrayList<double[][]> dataList = new ArrayList<double[][]>();
 		ArrayList<Integer> roiLineBreaks = new ArrayList<Integer>();
 		
 		int indexSize = 0;
 		
 		for ( int i = 0 ; i < pointList.size() -1 ; i++ )
 		{
 			double dataTmp[][] = getValueFor1DSegment ( pointList.get( i ), pointList.get( i +1 ) , image ) ;
 			indexSize+= dataTmp[0].length;
 			dataList.add( dataTmp );
 			roiLineBreaks.add( indexSize );
 		}
 		
 		double data[][] = new double[image.getSizeC()][indexSize];
 		
 		int index = 0;
 		for ( double[][] dataToAdd : dataList )
 		{
 			for ( int c = 0 ; c < image.getSizeC() ; c++ )
 			{
 				for ( int i = 0 ; i< dataToAdd[0].length ; i++ )
 				{
 					data[c][index+i] = dataToAdd[c][i];
 				}
 			}
 			index+=dataToAdd[0].length;
 		}
 		
 		Profile profile = new Profile();
 		profile.values = data;
 		profile.roiLineBreaks = roiLineBreaks;
 		return profile;
 
 	}
 	
 	private double[][] getValueFor1DSegment( Point2D p1, Point2D p2 , IcyBufferedImage image ) {
 		
         int distance = (int) p1.distance( p2 );
 
         double vx = ( p2.getX() - p1.getX() ) / (double)distance;
         double vy = ( p2.getY() - p1.getY() ) / (double)distance;
 
         int nbComponent= image.getSizeC();
         double[][] data = new double[nbComponent][distance];
 
         double x = p1.getX();
         double y = p1.getY();
 
         for ( int i = 0 ; i< distance ; i++ )
         {
                    //IcyBufferedImage image = canvas.getCurrentImage();
                    if ( image.isInside( (int)x, (int)y ) )
                    {
                 	   	for ( int component = 0 ; component < nbComponent ; component ++ )
                 	   	{
                               data[component][i] = Array1DUtil.getValue( 
                             		  image.getDataXY( component ) , 
                             		  image.getOffset( (int)x, (int)y ) , 
                             		  image.isSignedDataType() ) ;
                 	   	}     
                               
                    }else
                    {
                 	   for ( int component = 0 ; component < nbComponent ; component ++ )
                 	   {
                 		   data[component][i] = 0 ;
                 	   }
                    }
 
                    x+=vx;
                    y+=vy;
         }
         
         return data;
 
 	}
 
 
 
 //	@Override
 //	public void actionPerformed(ActionEvent e) {
 //		
 //		if ( e.getSource() == exportToExcelButton )
 //		{
 //			System.out.println("Export to excel.");
 //		}
 //		
 //		if ( e.getSource() == associateROIButton )
 //		{
 //			// remove previous listener.
 //					
 //			updateChart();
 //		}
 //
 //	}
 //
 //	@Override
 //	public void roiChanged(ROIEvent event) {		
 //		
 //		updateChart();
 //	
 //	}
 
 
 
 
 	
 	
 	
 	
 	
 
 }
