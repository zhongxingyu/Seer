 /*
     Copyright (c) 2000-2012 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package net.sf.jautl.graphics.colors;
 
 import net.sf.jautl.numeric.LinearInterpolator;
 
 public class ColorGreyF {
 	/** The maximum value for the color components. */
 	public static final float MAX_VALUE = 1.0f;
 	private float grey;
 
 	/**
 	 * Default constructor. Create an opaque black.
 	 */
 	public ColorGreyF() {
 		this(0);
 	}
 
 	/**
 	 * The constructor for solid colors.
 	 * @param greyLevel the grey component
 	 */
 	public ColorGreyF(float greyLevel) {
         this.grey = greyLevel;
 	}
 	
 	/**
 	 * The copy constructor.
 	 * @param rhs
 	 */
 	public ColorGreyF(ColorGreyF rhs) {
 		assign(rhs);
 	}
 	
 	/**
 	 * The assignement operator.
 	 * @param rhs
 	 */
 	public void assign(ColorGreyF rhs) {
 		setGrey(rhs.getGrey());
 	}
 
 	/**
 	 * Get the grey level of the color.
 	 * @return the value in the interval [0, MAX_VALUE)
 	 */
 	public final float getGrey() {
 		return grey;
 	}
 
 	/**
 	 * Set the grey level of the color. 
 	 * @param greyLevel the value in the range [0, MAX_VALUE)
 	 */
 	public final void setGrey(float greyLevel) {
 		this.grey = clipValues(greyLevel);
 	}
 
 	/**
 	 * Linearily interpolate a color, component-wise.
 	 * @param left the color when the x value is 0
 	 * @param x the fraction
 	 * @param right the color when the x value is 1
 	 */
 	public void interpolate(ColorGreyF left, double x, ColorGreyF right) {
 		setGrey((float)LinearInterpolator.interpolate(left.getGrey(), x, right.getGrey()));
 	}
 	
 	private float clipValues(float f) {
 		if (f < 0) return 0;
 		if (f > 1) return 1;
 		return f;
 	}
 }
