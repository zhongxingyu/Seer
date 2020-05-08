 //
 // ThreeColorColorize.java
 //
 
 /*
 SLIMPlugin for combined spectral-lifetime image analysis.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.slim.colorizer;
 
 import java.awt.Color;
 
 /**
  * Colorizes data based on a sequence of three colors.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/colorizer/ThreeColorColorize.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/colorizer/ThreeColorColorize.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis
  */
 public class ThreeColorColorize extends MultiColorColorize implements IColorize {
     Color m_color1;
     Color m_color2;
     Color m_color3;
 
     /**
      * Constructor.  Specifies the sequence of three colors.
      *
      * @param color1
      * @param color2
      * @param color3
      */
     public ThreeColorColorize(Color color1, Color color2, Color color3) {
         m_color1 = color1;
         m_color2 = color2;
         m_color3 = color3;
     }
 
     /**
      * Colorizes a data value.
      *
      * @param start value associated with starting color
      * @param stop value associated with ending color
      * @param value to colorize
      * @return interpolated Color value
      */
     public Color colorize(double start, double stop, double value) {
         Color returnColor = Color.BLACK;
         if (value > 0.0) {
             if (value >= start && value <= stop) {
                 double range = stop - start;
                value -= start;
                if (value < (range / 2.0)) {
                    returnColor = interpolateColor(m_color1, m_color2, 2.0 * value / range);
                 }
                 else {
                    returnColor = interpolateColor(m_color2, m_color3, 2.0 * (value - (range / 2.0)) / range);
                 }
             }
 
         }
         return returnColor;
     }
 }
