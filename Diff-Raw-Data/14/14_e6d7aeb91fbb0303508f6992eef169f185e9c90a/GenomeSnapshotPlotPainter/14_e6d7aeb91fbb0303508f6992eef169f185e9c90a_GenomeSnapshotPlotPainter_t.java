 /*
$Revision: 1.3 $
$Date: 2007-12-05 19:59:32 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webgenome.service.plot;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.SortedMap;
 
 import org.rti.webgenome.domain.Experiment;
 import org.rti.webgenome.domain.QuantitationType;
 import org.rti.webgenome.graphics.util.GenomePositionMapper;
 import org.rti.webgenome.graphics.widget.Axis;
 import org.rti.webgenome.graphics.widget.Background;
 import org.rti.webgenome.graphics.widget.Caption;
 import org.rti.webgenome.graphics.widget.GenomeSnapshotPlot;
 import org.rti.webgenome.graphics.widget.Grid;
 import org.rti.webgenome.graphics.widget.Legend;
 import org.rti.webgenome.graphics.widget.PlotPanel;
 import org.rti.webgenome.service.util.ChromosomeArrayDataGetter;
 import org.rti.webgenome.units.HorizontalAlignment;
 import org.rti.webgenome.units.Location;
 import org.rti.webgenome.units.Orientation;
 import org.rti.webgenome.units.VerticalAlignment;
 
 /**
  * Generates genome snapshot plots.
  * @author dhall
  *
  */
 public class GenomeSnapshotPlotPainter extends PlotPainter {
 	
 	//
 	//  C O N S T A N T S
 	//
 	
 	/** Grid color. */
 	private static final Color GRID_COLOR = Color.WHITE;
 	
 	/** Background color. */
 	private static final Color BG_COLOR = new Color(235, 235, 235);
 		
 	
 	//
 	//  C O N S T R U C T O R S
 	//
 	
 	/**
 	 * Constructor.
 	 * @param chromosomeArrayDataGetter Chromosome array data getter
 	 */
 	public GenomeSnapshotPlotPainter(
 			final ChromosomeArrayDataGetter chromosomeArrayDataGetter) {
 		super(chromosomeArrayDataGetter);
 	}
 
 	//
 	//  B U S I N E S S  M E T H O D S
 	//
 	
     /**
      * Paints a plot on the given plot panel.
      * @param panel Plot panel to add the plot to
      * @param experiments Experiments to plot
      * @param params Plotting parameters specified
      * by user
      */
     public void paintPlot(
     		final PlotPanel panel,
     		final Collection<Experiment> experiments,
     		final GenomeSnapshopPlotParameters params) {
         
         // Check args
         if (experiments == null || panel == null) {
             throw new IllegalArgumentException(
                     "Experiments and panel cannot be null");
         }
         
         Axis copyNumberAxis = null;
         Axis expressionAxis = null;
         Axis referenceYAxis = null;
         QuantitationType referenceYAxisQType = null;
         QuantitationType copyNumberQType =
         	Experiment.getCopyNumberQuantitationType(experiments);
 		if (copyNumberQType != null) {
 			copyNumberAxis = new Axis(
					params.getMinY(),
					params.getMaxY(),
 					params.getHeight(), Orientation.VERTICAL,
 					Location.LEFT_OF, panel.getDrawingCanvas());
 			referenceYAxis = copyNumberAxis;
 			referenceYAxisQType = copyNumberQType;
 		}
 		QuantitationType expressionQType =
 			Experiment.getExpressionQuantitationType(experiments);
 		if (expressionQType != null) {
 			Location textLocation = null;
 			if (copyNumberQType != null) {
 				textLocation = Location.RIGHT_OF;
 			} else {
 				textLocation = Location.LEFT_OF;
 			}
 			expressionAxis = new Axis(
					params.getMinY(),
					params.getMaxY(),
 					params.getHeight(), Orientation.VERTICAL,
 					textLocation, panel.getDrawingCanvas());
 			if (referenceYAxis == null) {
 				referenceYAxis = expressionAxis;
 				referenceYAxisQType = expressionQType;
 			}
 		}
         
         // Background
         Background bg = new Background(params.getWidth(),
         		params.getHeight(), BG_COLOR);
         panel.add(bg, true);
         
         // X-axis
         List<Double> hatchPoints = new ArrayList<Double>();
         List<String> hatchLabels = new ArrayList<String>();
         GenomePositionMapper mapper = new GenomePositionMapper(experiments);
         SortedMap<Short, Long> chromStarts =
         	mapper.getChromosomeStartLocations();
         for (Short chrom : chromStarts.keySet()) {
         	hatchLabels.add(chrom.toString());
         	hatchPoints.add(new Double(chromStarts.get(chrom)));
         }
         Axis xAxis = new Axis(params.getWidth(), Orientation.HORIZONTAL,
         		Location.BELOW, panel.getDrawingCanvas(), hatchPoints,
         		hatchLabels);
         String captionText = "Chromosome";
 	    Caption xCaption = new Caption(captionText,
 	            Orientation.HORIZONTAL, false, panel.getDrawingCanvas());
         
         // Grid lines
         if (params.isDrawVertGridLines()) {
         	Grid vertGrid = xAxis.newGrid(params.getWidth(),
         			params.getHeight(), GRID_COLOR, panel);
         	panel.add(vertGrid, HorizontalAlignment.LEFT_JUSTIFIED,
         			VerticalAlignment.TOP_JUSTIFIED);
         }
         if (params.isDrawHorizGridLines()) {
         	Grid horizGrid = referenceYAxis.newGrid(params.getWidth(),
         			params.getHeight(), GRID_COLOR, panel);
         	panel.add(horizGrid,  HorizontalAlignment.LEFT_JUSTIFIED,
         			VerticalAlignment.TOP_JUSTIFIED);
         }
         
         panel.add(xAxis, HorizontalAlignment.LEFT_JUSTIFIED,
 	    		VerticalAlignment.BOTTOM_JUSTIFIED);
 	    panel.add(xCaption, HorizontalAlignment.CENTERED,
 	            VerticalAlignment.BELOW);
         
         // Add reference Y-axes
 	    String yCaptionText = referenceYAxisQType.getName();
         panel.add(referenceYAxis, HorizontalAlignment.LEFT_JUSTIFIED,
         		VerticalAlignment.BOTTOM_JUSTIFIED);
         Caption yCaption = new Caption(yCaptionText,
         		Orientation.HORIZONTAL, true, panel.getDrawingCanvas());
         panel.add(yCaption, HorizontalAlignment.LEFT_OF,
         		VerticalAlignment.CENTERED);
         
         // Add genome snapshot plot
         GenomeSnapshotPlot plot = new GenomeSnapshotPlot(experiments,
         		this.getChromosomeArrayDataGetter(), params.getWidth(),
         		params.getHeight(), params.getMinY(), params.getMaxY());
         plot.setDrawRawLohProbabilities(params.isDrawRawLohProbabilities());
         plot.setInterpolateLohEndpoints(params.isInterpolateLohEndpoints());
         plot.setInterpolationType(params.getInterpolationType());
         plot.setLohThreshold(params.getLohThreshold());
         panel.add(plot, HorizontalAlignment.LEFT_JUSTIFIED,
         		VerticalAlignment.BOTTOM_JUSTIFIED);
 
         // Add second Y-axis if necessary
         if (expressionAxis != null) {
         	panel.add(expressionAxis, HorizontalAlignment.RIGHT_JUSTIFIED,
         			VerticalAlignment.BOTTOM_JUSTIFIED);
         	yCaptionText = expressionQType.getName();
         	yCaption = new Caption(yCaptionText, Orientation.HORIZONTAL,
         			true, panel.getDrawingCanvas());
         	panel.add(yCaption, HorizontalAlignment.RIGHT_OF,
             		VerticalAlignment.CENTERED);
         }
         
         // Legend
         Legend legend = new Legend(experiments, params.getWidth());
         panel.add(legend, HorizontalAlignment.CENTERED,
         		VerticalAlignment.BELOW);
         
         // Plot title
         Caption title = new Caption(params.getPlotName(),
         		Orientation.HORIZONTAL, false, panel.getDrawingCanvas());
         panel.add(title, HorizontalAlignment.CENTERED,
         		VerticalAlignment.ABOVE);
     }
 }
