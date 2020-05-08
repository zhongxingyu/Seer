 package org.dawnsci.plotting.draw2d.swtxy;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Paints the labels on the image trace.
  * @author fcp94556
  *
  */
 class IntensityLabelPainter {
 	
 	private static Logger logger = LoggerFactory.getLogger(IntensityLabelPainter.class);
 	
 	private IImageTrace image;
 	private IPlottingSystem system;
 	private NumberFormat format;
 
 	IntensityLabelPainter(IPlottingSystem system, IImageTrace image) {
 		this.system = system;
 		this.image  = image;
 		this.format = DecimalFormat.getNumberInstance();
 	}
 
 	/**
 	 * Attempts to paint the labels for intensity a the approximate centre of each pixel.
 	 * Does not work if custom axes are used currently.
 	 * 
 	 * Assumes that the caller will do a push and pop on the graphics appropriately.
 	 * 
 	 * @param graphics
 	 */
 	public void paintIntensityLabels(Graphics graphics) {
 		
 		if (system==null)          return;
 		//if (image.getAxes()!=null) return;
 		if (Boolean.getBoolean("org.dawnsci.plotting.draw2d.show.intensity.labels")) return;
 		
 		try {
 			graphics.setFont(new Font(Display.getCurrent(), new FontData("Dialog", 10, SWT.NORMAL)));
 			
 			final IAxis xAxis = system.getSelectedXAxis();
 			final IAxis yAxis = system.getSelectedYAxis();
			// Paint labels at center pixels
			final int xLower = (int)Math.round(Math.min(xAxis.getLower(), xAxis.getUpper()));
			final int xUpper = (int)Math.round(Math.max(xAxis.getLower(), xAxis.getUpper()));
			
			final int yLower = (int)Math.round(Math.min(yAxis.getLower(), yAxis.getUpper()));
			final int yUpper = (int)Math.round(Math.max(yAxis.getLower(), yAxis.getUpper()));
 			
			for (int x = xLower; x <= xUpper; x++) {
				for (int y = yLower; y <= yUpper; y++) {
 					// TODO FIXME check rotations.
 					final double intensity = image.getData().getDouble(y, x);
 					graphics.setAlpha(75);
 					graphics.fillString(format.format(intensity), xAxis.getValuePosition(x+0.5), yAxis.getValuePosition(y+0.5));
 					graphics.setAlpha(255);
 					graphics.drawString(format.format(intensity), xAxis.getValuePosition(x+0.5), yAxis.getValuePosition(y+0.5));
 				}
 			}
 		} catch (Throwable ne) {
 			logger.debug("Unable to process labels!", ne);
 			return;
 		}
 	}
 
 }
