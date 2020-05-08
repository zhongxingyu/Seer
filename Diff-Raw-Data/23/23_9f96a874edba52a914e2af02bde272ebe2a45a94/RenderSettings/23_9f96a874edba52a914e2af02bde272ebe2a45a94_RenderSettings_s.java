 /*
  * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/RenderSettings.java,v $
 * Date   : $Date: 2005/11/15 14:04:02 $
 * Version: $Revision: 1.1 $
  *
  * This library is part of OpenCms -
  * the Open Source Content Mananagement System
  *
  * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  *
  * For further information about OpenCms, please see the
  * project website: http://www.opencms.org
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.simapi;
 
 import java.awt.Color;
 import java.awt.RenderingHints;
 import java.util.HashMap;
 
 /**
  * Provides rendering hints of different quality for the image processing.<p>
  * 
  * @author Alexander Kandzior
  */
 public class RenderSettings {
 
     /** Rendering hints for the default <code>MEDIUM</code> render settigns. */
     protected static final RenderingHints HINTS_MEDIUM = initHints(Simapi.RENDER_MEDIUM);
 
     /** Rendering hints for the default <code>QUALITY</code> render settigns. */
     protected static final RenderingHints HINTS_QUALITY = initHints(Simapi.RENDER_QUALITY);
 
     /** Rendering hints for the default <code>SPEED</code> render settigns. */
     protected static final RenderingHints HINTS_SPEED = initHints(Simapi.RENDER_SPEED);
 
     /** The image save quality, used for JPEG images (and other formats that support such a setting). */
     private float m_compressionQuality;
 
     /** The rendering hints of this settings object. */
     private RenderingHints m_hints;
 
     /** The backgound color replacement for the transparent color, used if transparency is not supported by the selected image format. */
     private Color m_transparentReplaceColor;
 
     /**
      * Create a new set of render settings, based on the given constant base mode.<p>
      * 
      * @param baseMode the base mode of the settings, for example {@link Simapi#RENDER_QUALITY}
      */
     public RenderSettings(int baseMode) {
 
         this(baseMode, null);
     }
 
     /**
      * Create a new set of render settings, based on the given constant base mode and the provided 
      * special rendering hints.<p>
      * 
      * @param baseMode the base mode of the settings, for example {@link Simapi#RENDER_QUALITY}
      * @param hints the special rendering hints to use for image processing operations like scaling etc.
      */
     public RenderSettings(int baseMode, RenderingHints hints) {
 
         switch (baseMode) {
             case Simapi.RENDER_SPEED:
                 m_hints = HINTS_SPEED;
                m_compressionQuality = 0.3f;
                 break;
             case Simapi.RENDER_MEDIUM:
                 m_hints = HINTS_MEDIUM;
                 m_compressionQuality = 0.5f;
                 break;
             case Simapi.RENDER_QUALITY:
             default:
                 m_hints = HINTS_QUALITY;
                 m_compressionQuality = 0.7f;
                 break;
         }
         if (hints != null) {
             // must create a new object to modify, otherwise constant values would be affected
             RenderingHints newHints = new RenderingHints(null);
             newHints.add(m_hints);
             newHints.add(hints);
             m_hints = newHints;
         }
         m_transparentReplaceColor = Color.WHITE;
     }
 
     /**
      * Initializes the default values for the rendering hints.<p>
      * 
      * @param mode the quality mode to use
      */
     private static RenderingHints initHints(int mode) {
 
         // transformation rendering hints
         HashMap hints = new HashMap();
 
         switch (mode) {
             case Simapi.RENDER_SPEED:
                 hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                 hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                 hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                 hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                 hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                 hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                 hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
                 hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                 hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                 break;
             case Simapi.RENDER_MEDIUM:
                 hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                 hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
                 hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
                 hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
                 hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
                 hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
                 hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
                 hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
                 hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                 break;
             case Simapi.RENDER_QUALITY:
                 hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                 hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                 hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                 hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                 hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                 hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                 hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                 hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                 break;
             default: // ignore
         }
 
         return new RenderingHints(hints);
     }
 
     /**
      * Returns the image save compression quality, used for JPEG images (and other formats that support such a setting).<p>
      * 
      * This is used only if the image <code>type</code> supports different qualities.
      * For example, this it is used when writing JPEG images.
      * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p> 
      * 
      * @return the image save quality, used for JPEG images (and other formats that support such a setting)
      */
     public float getCompressionQuality() {
 
         return m_compressionQuality;
     }
 
     /**
      * Returns the backgound color replacement for the transparent color.<p>
      *
      * This is used if transparency is not supported by the selected image format.<p>
      *
      * The default color is {@link Color#WHITE}.<p>
      *
      * @return the backgound color replacement for the transparent color
      */
     public Color getTransparentReplaceColor() {
 
         return m_transparentReplaceColor;
     }
 
     /**
      * Sets the image save compression quality, used for JPEG images (and other formats that support such a setting).<p>
      * 
      * This is used only if the image <code>type</code> supports different qualities.
      * For example, this it is used when writing JPEG images.
      * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p> 
      * 
      * @param compressionQuality the compression quality to set (must be between 0 and 1)
      */
     public void setCompressionQuality(float compressionQuality) {
 
         if ((compressionQuality < 0f) || (compressionQuality > 1f)) {
             throw new IllegalArgumentException("compression quality must be between 0.0f and 1.0f");
         }
         m_compressionQuality = compressionQuality;
     }
 
     /**
      * Sets the backgound color replacement for the transparent color.<p>
      *
      * This is used if transparency is not supported by the selected image format.<p>
      *
      * @param transparentColor the backgound color replacement for the transparent color to set
      */
     public void setTransparentReplaceColor(Color transparentColor) {
 
         m_transparentReplaceColor = transparentColor;
     }
 
     /**
      * Returns the image rendering hints to to be used for image scaling etc.<p>
      * 
      * This method is protected to avoid changes to the constant values defined in this class.<p>
      * 
      * @return the image rendering hints to to be used for image scaling etc. 
      */
     protected RenderingHints getRenderingHints() {
 
         return m_hints;
     }
 }
