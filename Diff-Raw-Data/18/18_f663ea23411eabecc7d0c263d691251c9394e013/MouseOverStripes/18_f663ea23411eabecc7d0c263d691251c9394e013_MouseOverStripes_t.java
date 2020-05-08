 /*
$Revision: 1.4 $
$Date: 2008-06-16 16:53:40 $
 
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
 
 package org.rti.webgenome.graphics.event;
 
 import java.awt.Point;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rti.webgenome.units.Orientation;
 import org.rti.webgenome.util.SystemUtils;
 
 
 /**
  * Defines an area within a graphic where moving the mouse
  * over a series of horizontal or vertical rectangular
  * "stripes" triggers and event handler.  A typical response
  * will be to show some tool tip text.
 * @author mzmuda
  *
  */
 public class MouseOverStripes implements Serializable {
 	
 	/** Serialized version ID. */
 	private static final long serialVersionUID = 
 		SystemUtils.getLongApplicationProperty("serial.version.uid");
 
 	
 	// ==========================
 	//     Attributes
 	// ==========================
 	
 	/** Primary key value used for persistence. */
 	private Long id = null;
 	
 	/**
 	 * Orientation of stripes.  If horizontal,
 	 * then the <code>start</code> and <code>end</code>
 	 * properties of nested <code>MouseOverStripe</code>
 	 * objects will correspond to X-coordinates.
 	 * If vertical, the these properties will correspond
 	 * to Y-coordinates.
 	 */
 	private Orientation orientation;
 	
 	/** Width of mouseover area in pixels. */
 	private int width;
 	
 	/** Height of mouseover area in pixels. */
 	private int height;
 	
 	/** Origin of mouseover area relative to entire graphic. */
 	private Point origin = new Point(0, 0);
 	
 	/**
 	 * Individual mouseover "stripes."  The start and end
 	 * coordinates of the stripes are relative to
 	 * <code>origin</code>--i.e., they are not absolute
 	 * coordinates relative to the entire graphic.
 	 */
 	private List<MouseOverStripe> stripes =
 		new ArrayList<MouseOverStripe>();
 	
 	
 	// ===============================
 	//      Getters and setters
 	// ===============================
 
 	/**
 	 * Get primary key value for persistence.
 	 * @return Primary key value for persistence.
 	 */
 	public final Long getId() {
 		return id;
 	}
 
 	/**
 	 * Set primary key value for persistence.
 	 * @param id Primary key value for persistence.
 	 */
 	public final void setId(final Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Set height.
 	 * @param height Height in pixels.
 	 */
 	public final void setHeight(final int height) {
 		this.height = height;
 	}
 
 	/**
 	 * Set individual stripes.
 	 * @param stripes Stripes.
 	 */
 	public final void setStripes(final List<MouseOverStripe> stripes) {
 		this.stripes = stripes;
 	}
 
 	/**
 	 * Set width.
 	 * @param width Width in pixels
 	 */
 	public final void setWidth(final int width) {
 		this.width = width;
 	}
 
 	/**
 	 * Get height of mouseover area.
 	 * @return Height in pixels.
 	 */
 	public final int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Get orientation of mouseover area.  If horizontal,
 	 * then the <code>start</code> and <code>end</code>
 	 * properties of nested <code>MouseOverStripe</code>
 	 * objects will correspond to X-coordinates.
 	 * If vertical, the these properties will correspond
 	 * to Y-coordinates.
 	 * @return Orientation
 	 */
 	public final Orientation getOrientation() {
 		return orientation;
 	}
 
 	
 	/**
 	 * Get origin of mouseover area in relation
 	 * to the entire graphic.
 	 * @return Origin of mouseover area.
 	 */
 	public final Point getOrigin() {
 		return origin;
 	}
 
 	
 	/**
 	 * Get mouseover stripes.  The start and end
 	 * coordinates of the stripes are relative to
 	 * <code>origin</code>--i.e., they are not absolute
 	 * coordinates relative to the entire graphic.
 	 * If <code>orientation</code> is horizontal,
 	 * then the <code>start</code> and <code>end</code>
 	 * properties of nested <code>MouseOverStripe</code>
 	 * objects will correspond to X-coordinates.
 	 * If vertical, the these properties will correspond
 	 * to Y-coordinates.
 	 * @return Mouseover stripes.
 	 */
 	public final List<MouseOverStripe> getStripes() {
 		return stripes;
 	}
 
 	
 	/**
 	 * Get width of mouseover area.
 	 * @return Width in pixels.
 	 */
 	public final int getWidth() {
 		return width;
 	}
 	
 	
 	// ================================
 	//       Constructors
 	// ================================
 	
 	/**
 	 * Constructor.
 	 */
 	public MouseOverStripes() {
 		
 	}
 	
 	/**
 	 * Constructor.
 	 * @param orientation Orientation of stripes.  If horizontal,
 	 * then the <code>start</code> and <code>end</code>
 	 * properties of nested <code>MouseOverStripe</code>
 	 * objects will correspond to X-coordinates.
 	 * If vertical, the these properties will correspond
 	 * to Y-coordinates.
 	 * @param width Width of mouseover area in pixels.
 	 * @param height Height of mouseover area in pixels.
 	 */
 	public MouseOverStripes(final Orientation orientation,
 			final int width, final int height) {
 		this(orientation, width, height, new Point(0, 0));
 	}
 	
 	/**
 	 * Constructor.
 	 * @param orientation Orientation of stripes.  If horizontal,
 	 * then the <code>start</code> and <code>end</code>
 	 * properties of nested <code>MouseOverStripe</code>
 	 * objects will correspond to X-coordinates.
 	 * If vertical, the these properties will correspond
 	 * to Y-coordinates.
 	 * @param width Width of mouseover area in pixels.
 	 * @param height Height of mouseover area in pixels.
 	 * @param origin Origin of mouseover area relative to
 	 * entire graphic
 	 */
 	public MouseOverStripes(final Orientation orientation,
 			final int width, final int height,
 			final Point origin) {
 		super();
 		this.orientation = orientation;
 		this.width = width;
 		this.height = height;
 		this.origin = origin;
 	}
 
 	
 	// =================================
 	//      Additional business methods
 	// =================================
 	
 	/**
 	 * Add a mouseover stripe.
 	 * @param stripe A mouseover stripe.
 	 */
 	public final void add(final MouseOverStripe stripe) {
 		this.stripes.add(stripe);
 	}
 	
 	
 	/**
 	 * Get orientation by name.  This is used for persistence.
 	 * @return Name of orientation.
 	 */
 	public String getOrientationByName() {
 		return this.orientation.name();
 	}
 	
 	
 	/**
 	 * Set orientation using name.  This is used for persistence.
 	 * @param name Name of orientation
 	 */
 	public void setOrientationByName(final String name) {
 		this.orientation = Orientation.valueOf(name);
 	}
 	
 	
 	/**
 	 * Set X-coordinate of origin.
 	 * @param x X-coordinate of origin in pixels.
 	 */
 	public void setOriginX(final int x) {
 		this.origin.x = x;
 	}
 	
 	
 	/**
 	 * Get X-coordinate of origin.
 	 * @return X-coordinate of origin in pixels.
 	 */
 	public int getOriginX() {
 		return this.origin.x;
 	}
 	
 	
 	/**
 	 * Set Y-coordinate of origin.
 	 * @param y Y-coordinate of origin in pixels.
 	 */
 	public void setOriginY(final int y) {
 		this.origin.y = y;
 	}
 	
 	
 	/**
 	 * Get Y-coordinate of origin.
 	 * @return Y-coordinate of origin in pixels
 	 */
 	public int getOriginY() {
 		return this.origin.y;
 	}
 }
