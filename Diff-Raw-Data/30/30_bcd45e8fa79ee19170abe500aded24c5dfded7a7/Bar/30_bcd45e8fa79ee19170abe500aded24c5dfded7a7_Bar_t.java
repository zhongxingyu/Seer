 /*
$Revision: 1.2 $
$Date: 2007-02-02 18:56:06 $
 
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
 
 package org.rti.webcgh.graphics.widget;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.rti.webcgh.graphics.DrawingCanvas;
 import org.rti.webcgh.graphics.primitive.GraphicPrimitive;
 import org.rti.webcgh.graphics.primitive.Line;
 import org.rti.webcgh.graphics.primitive.Rectangle;
 import org.rti.webcgh.graphics.primitive.Text;
 import org.rti.webcgh.units.HorizontalAlignment;
 
 /**
  * Represents a single bar in a bar graph.  The bar consists
  * of a rectangle projecting up or down from the origin
  * (i.e. horizontal line corresponding to zero), a text
  * label above the bar, and optionally an error bar.
  * @author dhall
  *
  */
 public class Bar implements PlotElement {
 	
 	//
 	//     STATICS
 	//
 	
 	/** Rotation of data track labels in radians. */
 	private static final double LABEL_ROTATION = 1.5 * Math.PI;
 	
 	//
 	//     NON-ACCESSIBLE ATTRIBUTES
 	//
 	
 	/** Graphic primitives. */
 	private Collection<GraphicPrimitive> graphicPrimitives =
 		new ArrayList<GraphicPrimitive>();
 	
 	/** Minimum X-axis coordinate. */
 	private int minX = 0;
 	
 	/** Minimum Y-axis coordinate. */
 	private int minY = 0;
 	
 	/** Maximum X-axis coordinate. */
 	private int maxX = 0;
 	
 	/** Maximum Y-axis coordinate. */
 	private int maxY = 0;
 	
 	/** Y-axis coordinate corresponding to bar origin. */
 	private int zeroY = 0;
 	
 	
 	//
 	//     ACCESSIBLE ATTRIBUTES
 	//
 	
 	/** Font size of label. */
 	private int fontSize = 12;
 	
 	/** Color of text. */
 	private Color fontColor = Color.BLACK;
 	
 	/** Width of bar in pixels. */
 	private int barWidth = 10;
 	
 	/** Color of bar. */
 	private Color barColor = Color.BLACK;
 	
 	/** Whitespace padding around graphical elements. */
 	private int padding = 5;
 	
 	/** Thickness of error bars in pixels. */
 	private int errorBarStroke = 2;
 	
 	//
 	//     GETTERS/SETTERS
 	//
 	
     /**
      * Get text color.
 	 * @return Returns the text color.
 	 */
 	public final Color getFontColor() {
 		return fontColor;
 	}
 
 
 	/**
 	 * Set text color.
 	 * @param fontColor The text color
 	 */
 	public final void setFontColor(final Color fontColor) {
 		this.fontColor = fontColor;
 	}
 
 
 	/**
 	 * Get thickness of error bars in pixels.
 	 * @return Thickness of error bars in pixels
 	 */
 	public final int getErrorBarStroke() {
 		return errorBarStroke;
 	}
 
 
 	/**
 	 * Set thickness of error bars in pixels.
 	 * @param errorBarStroke Thickness of error bars in pixels
 	 */
 	public final void setErrorBarStroke(final int errorBarStroke) {
 		this.errorBarStroke = errorBarStroke;
 	}
 
 
 	/**
 	 * Get padding.
 	 * @return Padding in pixels
 	 */
 	public final int getPadding() {
 		return padding;
 	}
 
 
 	/**
 	 * Set padding.
 	 * @param padding Padding in pixels
 	 */
 	public final void setPadding(final int padding) {
 		this.padding = padding;
 	}
 
 
 	/**
 	 * Get color of bar.
 	 * @return Returns the bar color
 	 */
 	public final Color getBarColor() {
 		return barColor;
 	}
 
 
 	/**
 	 * Set the bar color.
 	 * @param barColor The bar color
 	 */
 	public final void setBarColor(final Color barColor) {
 		this.barColor = barColor;
 	}
 
 
 	/**
 	 * Get bar width.
 	 * @return Returns the bar width in pixels
 	 */
 	public final int getBarWidth() {
 		return barWidth;
 	}
 
 
 	/**
 	 * Set bar width.
 	 * @param barWidth The bar width in pixels
 	 */
 	public final void setBarWidth(final int barWidth) {
 		this.barWidth = barWidth;
 	}
 
 
 	/**
 	 * Get the font size.
 	 * @return Returns the font size.
 	 */
 	public final int getFontSize() {
 		return fontSize;
 	}
 
 
 	/**
 	 * Set font size.
 	 * @param fontSize The font size
 	 */
 	public final void setFontSize(final int fontSize) {
 		this.fontSize = fontSize;
 	}
 	
 	
 	//
 	//     CONSTRUCTORS
 	//
 	
 	
 	/**
 	 * Constructor.
 	 * @param value Value to plot
 	 * @param label Text label
 	 * @param plotMax Maximum value (including error)
 	 * in entire plot
 	 * @param scale Scale of native units to pixels
 	 * @param canvas A drawing canvas
 	 */
 	public Bar(final float value, final String label,
 			final float plotMax,
 			final float scale,
 			final DrawingCanvas canvas) {
 		this(value, Float.NaN, label, plotMax,
 				scale, canvas);
 	}
 	
 	
 	/**
 	 * Constructor.
 	 * @param value Value to plot
 	 * @param error Error value to plot
 	 * @param label Text label
 	 * @param plotMax Maximum value (including error)
 	 * in entire plot
 	 * @param scale Scale of native units to pixels
 	 * @param canvas A drawing canvas
 	 */
 	public Bar(final float value, final float error,
 			final String label,
 			final float plotMax,
 			final float scale,
 			final DrawingCanvas canvas) {
 		
 		// Check args
 		if (error < (float) 0.0) {
 			throw new IllegalArgumentException(
 					"Error must be a positive integer");
 		}
 		if (value > (float) 0.0) {
 			float absMax = value;
 			if (!Float.isNaN(error)) {
 				absMax += error / (float) 2.0;
 			}
 			if (absMax > plotMax) {
 				throw new IllegalArgumentException(
 						"Value to plot cannot be larger than "
 						+ "maximum value in plot");
 			}
 		}
 		
 		// Layout graphics
 		this.layoutLabel(label, canvas);
 		if (value < (float) 0.0) {
 			this.layoutNegativeBar(value, error, plotMax, scale);
 		} else if (value > (float) 0.0) {
 			this.layoutPositiveBar(value, error, plotMax, scale);
 		}
 	}
 	
 	
 	/**
 	 * Layout the label.
 	 * @param label Label text
 	 * @param canvas Drawing canvas
 	 */
 	private void layoutLabel(final String label,
 			final DrawingCanvas canvas) {
 		this.maxX = this.minX + this.barWidth;
 		if (label != null && label.length() > 0) {
 			int textHeight = canvas.renderedWidth(label, this.fontSize);
 			int textX = this.minX + this.barWidth / 2 + this.fontSize / 2;
 			Text text = canvas.newText(label, textX,
 					textHeight, this.fontSize,
 					HorizontalAlignment.LEFT_JUSTIFIED, this.fontColor);
 			text.setRotation(LABEL_ROTATION);
 			this.graphicPrimitives.add(text);
 			int candidateMaxX = this.fontSize + textX;
 			if (candidateMaxX > this.maxX) {
 				this.maxX = candidateMaxX;
 			}
 			this.maxY += textHeight;
 		}
 	}
 	
 	
 	/**
 	 * Layout bar for negative value.
 	 * @param value Value to plot
 	 * @param error Error value to plot
 	 * @param plotMax Maximum value (including error)
 	 * in entire plot
 	 * @param scale Scale of native units to pixels
 	 */
 	private void layoutNegativeBar(final float value, final float error,
 			final float plotMax, final float scale) {
 		assert value < (float) 0.0;
		
		// Adjust maximum value in plot so that it is >= 0
 		float maxValue = plotMax;
 		if (maxValue < (float) 0.0) {
 			maxValue = (float) 0.0;
 		}
 		
		// Find Y-pixel that corresonds to 0.0
		this.zeroY = this.maxY + this.padding
			+ (int) ((maxValue - (float) 0.0) * scale);
		
		// Draw bar
 		int barHeight = (int) ((maxValue - value) * scale);
		Rectangle bar = new Rectangle(this.minX, this.zeroY,
 				this.barWidth, barHeight, this.barColor);
 		this.graphicPrimitives.add(bar);
		int graphMaxPix = this.zeroY + barHeight;
 		
		// Draw error bar
 		if (!Float.isNaN(error)) {
			int topY = this.zeroY + barHeight;
 			int errorBarHeight = (int) (error * scale);
 			int bottomY = topY + errorBarHeight;
 			Line horizLine = new Line(this.minX, bottomY,
 					this.minX + this.barWidth, bottomY, this.errorBarStroke,
 					this.barColor);
 			int midX = this.minX + this.barWidth / 2;
 			Line vertLine = new Line(midX, topY, midX, bottomY,
 					this.errorBarStroke, this.barColor);
 			this.graphicPrimitives.add(vertLine);
 			this.graphicPrimitives.add(horizLine);
 			graphMaxPix = bottomY;
 		}
 		
		// Set maximum Y-pixel value
 		this.maxY = graphMaxPix;
 	}
 	
 	
 	/**
 	 * Layout bar for positive value.
 	 * @param value Value to plot
 	 * @param error Error value to plot
 	 * @param plotMax Maximum value (including error)
 	 * in entire plot
 	 * @param scale Scale of native units to pixels
 	 */
 	private void layoutPositiveBar(final float value, final float error,
 			final float plotMax, final float scale) {
 		assert value > (float) 0.0;
 		int graphMinPix = this.maxY + this.padding;
 		int barMinPix = graphMinPix + (int) ((plotMax - value) * scale);
 		int barHeight = (int) (value * scale);
 		int barMaxPix = barMinPix + barHeight;
 		
 		// Error bar
 		if (!Float.isNaN(error)) {
 			float absMax = value + error / (float) 2.0;
 			int errorMinPix = graphMinPix + (int) ((plotMax - absMax) * scale);
 			Line horizLine = new Line(this.minX, errorMinPix,
 					this.minX + this.barWidth, errorMinPix,
 					this.errorBarStroke, this.barColor);
 			int centerX = this.barWidth / 2 + this.minX;
 			Line vertLine = new Line(centerX, errorMinPix, centerX,
 					barMinPix, this.errorBarStroke,
 					this.barColor);
 			this.graphicPrimitives.add(horizLine);
 			this.graphicPrimitives.add(vertLine);
 		}
 		
 		// Bar
 		Rectangle rect = new Rectangle(this.minX, barMinPix,
 				this.barWidth, barHeight, this.barColor);
 		this.graphicPrimitives.add(rect);
 		
 		// Adjust properties
 		this.maxY = barMaxPix;
 		this.zeroY = barMaxPix;
 	}
 	
 	//
 	//     PlotElement INTERFACE
 	//
 
 
 	/**
      * Paint element.
      * @param canvas A canvas
      */
     public final void paint(final DrawingCanvas canvas) {
     	for (GraphicPrimitive p : this.graphicPrimitives) {
     		canvas.add(p);
     	}
     }
     
     
     /**
      * Point at top left used to align with other plot elements.
      * @return A point
      */
     public final Point topLeftAlignmentPoint() {
     	return new Point(this.minX, this.zeroY);
     }
     
     
     /**
      * Point at bottom left used to align with other plot elements.
      * @return A point
      */
     public final Point bottomLeftAlignmentPoint() {
     	return new Point(this.minX, this.zeroY);
     }
     
     
     /**
      * Point at top right used to align with other plot elements.
      * @return A point
      */
     public final Point topRightAlignmentPoint() {
     	return new Point(this.maxX, this.zeroY);
     }
     
     
     /**
      * Point at bottom right used to align with other plot elements.
      * @return A point
      */
     public final Point bottomRightAlignmentPoint() {
     	return new Point(this.maxX, this.zeroY);
     }
     
     
     /**
      * Width in pixels.
      * @return Width in pixels
      */
     public final int width() {
     	return this.maxX - this.minX;
     }
     
     
     /**
      * Height in pixels.
      * @return Height in pixels
      */
     public final int height() {
     	return this.maxY - this.minY;
     }
     
     
     /**
      * Return point at top left of element.
      * @return A point
      */
     public final Point topLeftPoint() {
     	return new Point(this.minX, this.minY);
     }
     
     
     /**
      * Move element.
      * @param deltaX Number of pixels horizontally
      * @param deltaY Number of pixels vertically
      */
     public final void move(final int deltaX, final int deltaY) {
     	this.minX += deltaX;
     	this.minY += deltaY;
     	this.maxX += deltaX;
     	this.maxY += deltaY;
     	this.zeroY += deltaY;
     	for (GraphicPrimitive p : this.graphicPrimitives) {
     		p.move(deltaX, deltaY);
     	}
     }
 
 }
