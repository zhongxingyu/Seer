 /*
$Revision: 1.3 $
$Date: 2007-07-05 13:23:30 $
 
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
 
 import java.io.Serializable;
 
 import org.rti.webgenome.util.SystemUtils;
 
 
 
 /**
  * MouseOver Stripe information.
  */
 public class MouseOverStripe implements Serializable {
 	
 	/** Serialized version ID. */
 	private static final long serialVersionUID = 
 		SystemUtils.getLongApplicationProperty("serial.version.uid");
 
 
     // =============================
     //       Attributes
     // =============================
 	
 	/** Primary key value for persistence. */
 	private Long id = null;
 
     /**
      * Start coordinate.  This may be an
      * X- or Y-coordinate, depending on the
      * orientation of the container <code>MouseOverStripes</code>.
      */
     private int start = 0;
 
     /**
      * End coordinate.  This may be an
      * X- or Y-coordinate, depending on the
      * orientation of the container <code>MouseOverStripes</code>.
      */
     private int end = 0;
 
     /**
      * Stripe text.
      */
     private String text = "";
     
 
     // =========================================
     //      Constructors
     // =========================================
 	/**
      * Constructor.
      */
     public MouseOverStripe() {
 
     }
     
     /**
      * Constructor.
      * @param start Start coordinate
      * @param end End coordinate
      * @param text Stripe text
      */
     public MouseOverStripe(final int start, final int end, final String text) {
     	this.start = start;
     	this.end = end;
     	this.text = text;
     }
 
 
     // =========================================
     //      Getters and Setters
     // =========================================
     
     /**
      * Get primary key value for persistence.
      * @return Primary key value
      */
     public Long getId() {
 		return id;
 	}
     
     /**
      * Set primary key value for persistence.
      * @param id Primary key value
      */
 	public void setId(final Long id) {
 		this.id = id;
 	}
 	
 	
 	/**
      * Get end coordinate.  This may be an
      * X- or Y-coordinate, depending on the
      * orientation of the container <code>MouseOverStripes</code>.
 	 * @return Returns the end.
 	 */
 	public int getEnd() {
 		return end;
 	}
 	
 	
 	/**
 	 * Set end coordinate.  This may be an
      * X- or Y-coordinate, depending on the
      * orientation of the container <code>MouseOverStripes</code>.
 	 * @param end The end to set.
 	 */
 	public void setEnd(final int end) {
 		this.end = end;
 	}
 	
 	
 	/**
 	 * Get start coordinate.  This may be an
      * X- or Y-coordinate, depending on the
      * orientation of the container <code>MouseOverStripes</code>.
 	 * @return Returns the start.
 	 */
 	public int getStart() {
 		return start;
 	}
 	
 	
 	/**
 	 * Set start coordinate.  This may be an
      * X- or Y-coordinate, depending on the
      * orientation of the container <code>MouseOverStripes</code>.
 	 * @param start The start to set.
 	 */
 	public void setStart(final int start) {
 		this.start = start;
 	}
 	
 	
 	/**
 	 * Get text value.
 	 * @return Returns the text.
 	 */
 	public String getText() {
 		return text;
 	}
 	
 	
 	/**
 	 * Set text value.
 	 * @param text The text to set.
 	 */
 	public void setText(final String text) {
 		this.text = text;
 	}
 }
