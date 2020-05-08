 /*
  * LineStyle2.java
  * Transform
  *
  * Copyright (c) 2009 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.flagstone.transform.linestyle;
 
 
 import com.flagstone.transform.coder.CoderException;
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.Copyable;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncodeable;
 import com.flagstone.transform.coder.SWFEncoder;
 import com.flagstone.transform.coder.SWFFactory;
 import com.flagstone.transform.datatype.Color;
 import com.flagstone.transform.exception.IllegalArgumentRangeException;
 import com.flagstone.transform.fillstyle.FillStyle;
 
 /** TODO(class). */
 public final class LineStyle2 implements SWFEncodeable, Copyable<LineStyle2> {
 
     private static final String FORMAT = "LineStyle2: { width=%d; color=%s;"
             + " fillStyle=%s; startCap=%s; endCap=%s; joinStyle=%s;"
            + " scaledHorizontally=%d; scaledVertically=%d;"
            + " pixelAligned=%s; lineClosed=%d; miterLimit=%d }";
 
     private int width;
     private Color color;
 
     private int startCap;
     private int endCap;
     private int joinStyle;
     private FillStyle fillStyle;
 
     private boolean scaledHorizontally;
     private boolean scaledVertically;
     private boolean pixelAligned;
     private boolean lineClosed;
 
     private int miterLimit;
 
     private transient boolean hasFillStyle;
     private transient boolean hasMiter;
 
     /**
      * Creates and initialises a LineStyle2 object using values encoded
      * in the Flash binary format.
      *
      * @param coder
      *            an SWFDecoder object that contains the encoded Flash data.
      *
      * @param context
      *            a Context object used to manage the decoders for different
      *            type of object and to pass information on how objects are
      *            decoded.
      *
      * @throws CoderException
      *             if an error occurs while decoding the data.
      */
     public LineStyle2(final SWFDecoder coder, final Context context)
             throws CoderException {
 
         width = coder.readWord(2, false);
         unpack(coder.readB16());
 
         if (hasMiter) {
             coder.readWord(2, false);
         }
 
         if (hasFillStyle) {
             final SWFFactory<FillStyle> decoder = context.getRegistry()
                     .getFillStyleDecoder();
             fillStyle = decoder.getObject(coder, context);
         } else {
             color = new Color(coder, context);
         }
     }
 
     /** TODO(method). */
     public LineStyle2(final int width, final Color color) {
         super();
 
         setWidth(width);
         setColor(color);
 
         scaledVertically = true;
         scaledVertically = true;
         lineClosed = true;
     }
 
     /** TODO(method). */
     public LineStyle2(final int width, final FillStyle style) {
         super();
 
         setWidth(width);
         setFillStyle(style);
 
         scaledVertically = true;
         scaledVertically = true;
         lineClosed = true;
     }
 
      /**
      * Creates and initialises a LineStyle2 object using the values copied
      * from another LineStyle2 object.
      *
      * @param object
      *            a LineStyle2 object from which the values will be
      *            copied.
      */
     public LineStyle2(final LineStyle2 object) {
         width = object.width;
         color = object.color;
 
         if (fillStyle != null) {
             object.fillStyle = fillStyle.copy();
         }
 
         startCap = object.startCap;
         endCap = object.endCap;
         joinStyle = object.joinStyle;
 
         scaledHorizontally = object.scaledHorizontally;
         scaledVertically = object.scaledVertically;
         pixelAligned = object.pixelAligned;
         lineClosed = object.lineClosed;
         miterLimit = object.miterLimit;
     }
 
     /**
      * Returns the width of the line.
      */
     public int getWidth() {
         return width;
     }
 
     /**
      * Sets the width of the line.
      *
      * @param width
      *            the width of the line. Must be in the range 0..65535.
      */
     public void setWidth(final int width) {
         if ((width < 0) || (width > 65535)) {
             throw new IllegalArgumentRangeException(0, 65535, width);
         }
         this.width = width;
     }
 
     /**
      * Returns the colour of the line.
      */
     public Color getColor() {
         return color;
     }
 
     /**
      * Sets the colour of the line.
      *
      * @param aColor
      *            the colour of the line. Must be not be null.
      */
     public void setColor(final Color aColor) {
         if (aColor == null) {
             throw new NullPointerException();
         }
         color = aColor;
     }
 
     /** TODO(method). */
     public CapStyle getStartCap() {
         CapStyle style;
         if (startCap == 1) {
             style = CapStyle.NONE;
         } else if (startCap == 2) {
             style = CapStyle.SQUARE;
         } else {
             style = CapStyle.ROUND;
         }
         return style;
     }
 
     /** TODO(method). */
     public void setStartCap(final CapStyle capStyle) {
         switch (capStyle) {
         case NONE:
             startCap = 1;
             break;
         case SQUARE:
             startCap = 2;
             break;
         default:
             startCap = 0;
             break;
         }
     }
 
     /** TODO(method). */
     public CapStyle getEndCap() {
         CapStyle style;
         if (endCap == 1) {
             style = CapStyle.NONE;
         } else if (endCap == 2) {
             style = CapStyle.SQUARE;
         } else {
             style = CapStyle.ROUND;
         }
         return style;
     }
 
     /** TODO(method). */
     public void setEndCap(final CapStyle capStyle) {
         switch (capStyle) {
         case NONE:
             endCap = 1;
             break;
         case SQUARE:
             endCap = 2;
             break;
         default:
             endCap = 0;
             break;
         }
     }
 
     /** TODO(method). */
     public JoinStyle getJoinStyle() {
         JoinStyle style;
         if (endCap == 1) {
             style = JoinStyle.BEVEL;
         } else if (endCap == 2) {
             style = JoinStyle.MITER;
         } else {
             style = JoinStyle.ROUND;
         }
         return style;
     }
 
     /** TODO(method). */
     public void setJoinStyle(final JoinStyle style) {
         switch (style) {
         case BEVEL:
             joinStyle = 1;
             break;
         case MITER:
             joinStyle = 2;
             break;
         default:
             joinStyle = 0;
             break;
         }
     }
 
     /** TODO(method). */
     public boolean isScaledHorizontally() {
         return scaledHorizontally;
     }
 
     /** TODO(method). */
     public void setScaledHorizontally(final boolean scaled) {
         scaledHorizontally = scaled;
     }
 
     /** TODO(method). */
     public boolean isScaledVertically() {
         return scaledVertically;
     }
 
     /** TODO(method). */
     public void setScaledVertically(final boolean scaled) {
         scaledVertically = scaled;
     }
 
     /** TODO(method). */
     public boolean isPixelAligned() {
         return pixelAligned;
     }
 
     /** TODO(method). */
     public void setPixelAligned(final boolean aligned) {
         pixelAligned = aligned;
     }
 
     /** TODO(method). */
     public boolean isLineClosed() {
         return lineClosed;
     }
 
     /** TODO(method). */
     public void setLineClosed(final boolean closed) {
         lineClosed = closed;
     }
 
     /** TODO(method). */
     public int getMiterLimit() {
         return miterLimit;
     }
 
     /** TODO(method). */
     public void setMiterLimit(final int limit) {
         if ((limit < 0) || (limit > 65535)) {
             throw new IllegalArgumentRangeException(0, 65535, limit);
         }
         miterLimit = limit;
     }
 
     /** TODO(method). */
     public FillStyle getFillStyle() {
         return fillStyle;
     }
 
     /** TODO(method). */
     public void setFillStyle(final FillStyle style) {
         fillStyle = style;
     }
 
     /** TODO(method). */
     public LineStyle2 copy() {
         return new LineStyle2(this);
     }
 
     @Override
     public String toString() {
         return String.format(FORMAT, width, color, fillStyle, startCap, endCap,
                 joinStyle, scaledHorizontally, scaledVertically, pixelAligned,
                 lineClosed, miterLimit);
     }
 
     /** {@inheritDoc} */
     public int prepareToEncode(final SWFEncoder coder, final Context context) {
 
         hasFillStyle = fillStyle != null;
         hasMiter = joinStyle == 2;
 
         int length = 4;
 
         if (hasMiter) {
             length += 2;
         }
 
         if (hasFillStyle) {
             length += fillStyle.prepareToEncode(coder, context);
         } else {
             length += 4;
         }
 
         if (scaledHorizontally || scaledVertically) {
             context.getVariables().put(Context.SCALING_STROKE, 1);
         }
 
         return length;
     }
 
     /** {@inheritDoc} */
     public void encode(final SWFEncoder coder, final Context context)
             throws CoderException {
         coder.writeWord(width, 2);
         coder.writeB16(pack());
 
         if (hasMiter) {
             coder.writeWord(miterLimit, 2);
         }
 
         if (hasFillStyle) {
             fillStyle.encode(coder, context);
         } else {
             color.encode(coder, context);
         }
     }
 
     private int pack() {
 
         int value = 0;
 
         switch (startCap) {
         case 1:
             value |= 0x00004000;
             break;
         case 2:
             value |= 0x00008000;
             break;
         default:
             break;
         }
 
         switch (joinStyle) {
         case 1:
             value |= 0x00001000;
             break;
         case 2:
             value |= 0x00002000;
             break;
         default:
             break;
         }
 
         value |= fillStyle == null ? 0 : 0x00000800;
         value |= scaledHorizontally ? 0 : 0x00000400;
         value |= scaledVertically ? 0 : 0x00000200;
         value |= pixelAligned ? 0x00000100 : 0;
         value |= lineClosed ? 0 : 0x00000004;
         value |= endCap;
 
         return value;
     }
 
     private void unpack(final int value) {
 
         if ((value & 0x00004000) > 0) {
             startCap = 1;
         } else if ((value & 0x00008000) > 0) {
             startCap = 2;
         } else {
             startCap = 0;
         }
 
         if ((value & 0x00001000) > 0) {
             joinStyle = 1;
             hasMiter = false;
         } else if ((value & 0x00002000) > 0) {
             joinStyle = 2;
             hasMiter = true;
         } else {
             joinStyle = 0;
             hasMiter = false;
         }
 
         hasFillStyle = (value & 0x00000800) != 0;
         scaledHorizontally = (value & 0x00000400) == 0;
         scaledVertically = (value & 0x00000200) == 0;
         pixelAligned = (value & 0x00000100) != 0;
         lineClosed = (value & 0x00000004) == 0;
         endCap = value & 0x00000003;
     }
 }
