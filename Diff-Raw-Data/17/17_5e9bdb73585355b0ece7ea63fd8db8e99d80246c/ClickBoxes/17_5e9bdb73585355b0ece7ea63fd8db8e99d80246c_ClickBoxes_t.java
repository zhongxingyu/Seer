 /*
$Revision: 1.7 $
$Date: 2008-06-16 16:52:19 $
 
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
 package org.rti.webgenome.graphics.io;
 
 import java.awt.Point;
 import java.io.Serializable;
 
 import org.apache.log4j.Logger;
 import org.rti.webgenome.util.SystemUtils;
 
 
 /**
  * This class is used by the web tier to construct a map
  * of event responses that is overlaid on a graphic.
  * Clicking in a region of the graphic corresponding
  * to an individual box, will invoke a Javascript event
  * handler that does something with the text value in that
  * box.
 * @author mzmuda
  */
 public class ClickBoxes implements Serializable {
 	
 	/** Serialized version ID. */
 	private static final long serialVersionUID = 
 		SystemUtils.getLongApplicationProperty("serial.version.uid");
 	
 	/** Logger. */
 	private static final Logger LOGGER =
 		Logger.getLogger(ClickBoxes.class);
 	
     // =============================
     //       Attributes
     // =============================
 	
 	/** Primary key value for persistence. */
 	private Long id = null;
 	
 	/** Origin of click box region with respect to overall graphic. */
 	private Point origin = new Point();
 
     /**
      * Individual box width.
      */
     private int boxWidth = 0;
 
     /**
      * Individual box height.
      */
     private int boxHeight = 0;
 
     /**
      * Array holding click box text.
      */
     private String[] clickBox = null;
     
     /** Number of rows in clickBox matrix. */
     private int numRows = 0;
     
     /** Number of columns in clickBox matrix. */
     private int numCols = 0;
     
     /** Width of entire click box area in pixels. */
     private int width = 0;
     
     /** Height of entire click box area in pixels. */
     private int height = 0;
 
 
 	// =========================================
     //      Constructors
     // =========================================
     
     /**
      * Constructor.
      */
     public ClickBoxes() {
     	
     }
 
 	/**
      * Constructor.
      * @param width Width of entire click box area in pixels.
      * @param height Height of entire click box area in pixels.
      * @param boxWidth Width of a single click box in pixels.
      * @param boxHeight Height of a single click box in pixels.
      */
     public ClickBoxes(final int width, final int height, final int boxWidth,
     		final int boxHeight) {
     	this(width, height, boxWidth, boxHeight, new Point(0, 0));
     }
 
     /**
      * Constructor.
      * @param width Width of entire click box area in pixels.
      * @param height Height of entire click box area in pixels.
      * @param boxWidth Width of a single click box in pixels.
      * @param boxHeight Height of a single click box in pixels.
 	 * @param origin Origin of clickboxes area relative to
 	 * entire graphic
      */
     public ClickBoxes(final int width, final int height, final int boxWidth,
     		final int boxHeight, final Point origin) {
     	this.width = width;
     	this.height = height;
     	this.boxWidth = boxWidth;
     	this.boxHeight = boxHeight;
     	this.numRows = (int) Math.ceil((double) height
     			/ (double) boxHeight) + 1;
     	this.numCols = (int) Math.ceil((double) width
     			/ (double) boxWidth) + 1;
     	this.clickBox = new String[this.numCols * this.numRows];
     	this.origin = origin;
    	//System.out.println("clickbox " + this.getId() + " created: w{" + this.width + "} h{" + this.height + "} boxw{" + this.boxWidth + "} boxh{" + this.boxHeight + "} #rows{" + this.numRows + "} #cols{" + this.numCols + "} o{" + this.origin + "}");	// DEBUG test - mzmuda
    	//System.out.println(this.toPrettyString());	// DEBUG test - mzmuda
     }
 
 
     // =========================================
     //      Getters and Setters
     // =========================================
     
     /**
      * Get primary key value used for persistence.
      * @return Primary key value
      */
     public Long getId() {
 		return id;
 	}
 
     /**
      * Set primary key value used for persistence.
      * @param id Primary key value
      */
 	public void setId(final Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Get array of click box text.
 	 * @return Click box text
 	 */
 	public String[] getClickBox() {
 		return clickBox;
 	}
 
 	/**
 	 * Set array of click box text.
 	 * @param clickBox Click box text
 	 */
 	public void setClickBox(final String[] clickBox) {
 		this.clickBox = clickBox;
 	}
 
 	/**
 	 * Set height of click box area.
 	 * @param height Height in pixels.
 	 */
 	public void setHeight(final int height) {
 		this.height = height;
 	}
 
 	/**
 	 * Set width of click box area.
 	 * @param width Width in pixels.
 	 */
 	public void setWidth(final int width) {
 		this.width = width;
 	}
 	
 	/**
 	 * Get height of entire click box area in pixels.
 	 * @return Height in pixels
 	 */
 	public int getHeight() {
 		return this.height;
 	}
 	
 	/**
 	 * Get width of entire click box area in pixels.
 	 * @return Width in pixels
 	 */
 	public int getWidth() {
 		return this.width;
 	}
 
 	/**
 	 * Get number of columns.
 	 * @return Number of columns in click box area.
 	 */
 	public int getNumCols() {
 		return numCols;
 	}
 
 	/**
 	 * Set number of columns.
 	 * @param numCols Number of columns in click box area.
 	 */
 	public void setNumCols(final int numCols) {
 		this.numCols = numCols;
 	}
 
 	/**
 	 * Get number of rows in click box area.
 	 * @return Number of rows.
 	 */
 	public int getNumRows() {
 		return numRows;
 	}
 
 	/**
 	 * Set number of rows in click box area.
 	 * @param numRows Number of rows
 	 */
 	public void setNumRows(final int numRows) {
 		this.numRows = numRows;
 	}
 	
 
 	/**
      * Set height of individual boxes.
      * @param boxHeight Height of boxes in pixels.
      */
 	public void setBoxHeight(final int boxHeight) {
 		this.boxHeight = boxHeight;
 	}
 
 	/**
 	 * Set width of individual boxes.
 	 * @param boxWidth Width of boxes in pixels.
 	 */
 	public void setBoxWidth(final int boxWidth) {
 		this.boxWidth = boxWidth;
 	}
     
     
 	/**
 	 * Get origin of click boxes with regards to the
 	 * underlying graphic.
 	 * @return Origin of click boxes.
 	 */
 	public Point getOrigin() {
 		return origin;
 	}
 	
 	
 	/**
 	 * @return Returns the height.
 	 */
 	public int getBoxHeight() {
 		return boxHeight;
 	}
 
 	
 	/**
 	 * @return Returns the width.
 	 */
 	public int getBoxWidth() {
 		return boxWidth;
 	}
 
 	
 	// ===================================
 	//       Business methods
 	// ===================================
 	
 	/**
 	 * Add click box text.
 	 * @param text Text to add
 	 * @param x X-coordinate of some point in the click boxes
 	 * @param y Y-coordinate of some point in the click boxes
 	 */
 	public void addClickBoxText(final String text, final int x, final int y) {
 		int idx = this.getIndex(x, y);
		//System.out.println("clickbox " + this.getId() + " addClickBoxText() text{" + text + "} x{" + x + "} y{" + y + "}");	// DEBUG test - mzmuda
		this.clickBox[idx] = text;
		//System.out.println("clickbox " + this.getId() + " clickBox[" + idx + "] was set to {" + text + "}");	// DEBUG test - mzmuda
 	}
 	
 	/**
 	 * Get click box text.
 	 * @param x X-coordinate of some point in the click boxes
 	 * in pixels
 	 * @param y Y-coordinate of some point in the click boxes
 	 * in pixels
 	 * @return Click box text
 	 */
 	public String getClickBoxTextOfPixel(final int x, final int y) {
 		String text = null;
 		int idx = this.getIndex(x, y);
 		if (idx >= 0 && idx < this.clickBox.length) {
 			text = this.clickBox[idx];
 		}
 		// Code commented out because not really necessary and
 		// leads to alot of output that may bury needed
 		// debugging messages
 //		else {
 //			LOGGER.warn("Pixel (" + x + ", " + y + ") out of range");
 //		}
 		return text;
 	}
 	
 	
 	/**
 	 * Get text that maps to box with given logical
 	 * {@code row} and {@code col} number.
 	 * @param row Logical row number
 	 * @param col Logical column number
 	 * @return Associated click box text
 	 */
 	public String getClickBoxTextOfBox(final int row, final int col) {
 		String value = "";
 		int idx = row * this.numCols + col;
 		if (idx >= 0 && idx < this.clickBox.length) {
 			value = this.clickBox[idx];
 		}
 		// Code commented out because not really necessary and
 		// leads to alot of output that may bury needed
 		// debugging messages
 //		else {
 //			
 //			LOGGER.warn("Box [" + row + "][" + col
 //					+ "] out of range (index = " + idx + " out of "
 //					+ this.clickBox.length + ")");
 //		}
 		return value;
 	}
 	
 	
 	/**
 	 * Get X-coordinate of click boxes origin.
 	 * @return X-coordinate of origin in pixels.
 	 */
 	public int getOriginX() {
 		return this.origin.x;
 	}
 	
 	/**
 	 * Set X-coordinate of click boxes origin.
 	 * @param x X-coordinate of origin in pixels
 	 */
 	public void setOriginX(final int x) {
 		this.origin.x = x;
 	}
 	
 	/**
 	 * Get Y-coordinate of click boxes origin.
 	 * @return Y-coordinate of origin in pixels.
 	 */
 	public int getOriginY() {
 		return this.origin.y;
 	}
 	
 	/**
 	 * Set Y-coordinate of click boxes origin.
 	 * @param y Y-coordinate of origin in pixels
 	 */
 	public void setOriginY(final int y) {
 		this.origin.y = y;
 	}
 	
 	
 	/**
 	 * Debug method that generates readable string
 	 * of click box text.
 	 * @return Readable string of click box text.
 	 */
 	public String toPrettyString() {
 		StringBuffer buff = new StringBuffer();
 		if (this.clickBox != null) {
 			int col = 0;
 			for (int i = 0; i < this.clickBox.length; i++) {
 				buff.append(this.clickBox[i]);
 				col++;
 				if (col % this.numCols == 0) {
 					buff.append("\n");
 					col = 0;
 				} else {
 					buff.append(",");
 				}
 			}
 			buff.append("\n");
 		}
 		return buff.toString();
 	}
 	
 	
 	/**
 	 * Get number of click box areas.
 	 * @return Number of individual boxes.
 	 */
 	public int numBoxes() {
 		return this.numRows * this.numCols;
 	}
 	
 	/**
 	 * Get index of cell in {@code clickBox} containing
 	 * text corresponding to point at pixel location (x, y).
 	 * @param x X-pixel coordinate
 	 * @param y Y-pixel coordinate
 	 * @return Text at corresponding pixel location
 	 */
 	private int getIndex(final int x, final int y) {
		//System.out.print("clickbox getIndex() x{" + x + "} y{" + y + "} ox{" + this.getOriginX() + "} oy{" + this.getOriginY() + "}");	// DEBUG test - mzmuda
 		int row = this.getRowNum(y);
 		int col = this.getColNum(x);
		//System.out.println(" row{" + row + "} col{" + col + "} return{" + (row * this.numCols + col) + "}");	// DEBUG test - mzmuda
 		return row * this.numCols + col;
 	}
 	
 	/**
 	 * Get row number that y-pixel value falls into.
 	 * @param y Y-pixel value
 	 * @return Row
 	 */
 	private int getRowNum(final int y) {
 		return (int) Math.floor((double) y / (double) this.boxHeight);
 	}
 	
 	
 	/**
 	 * Get column number that x-pixel value false into.
 	 * @param x X-pixel value
 	 * @return Column
 	 */
 	private int getColNum(final int x) {
 		return (int) Math.floor((double) x / (double) this.boxWidth);
 	}
 }
