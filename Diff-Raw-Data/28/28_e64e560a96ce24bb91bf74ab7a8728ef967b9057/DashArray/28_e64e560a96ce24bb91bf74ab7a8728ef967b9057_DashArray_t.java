 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
 Copyright (C) 2001-20012 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
 E-Mail: info@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.style.model;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import org.deegree.framework.utils.HashCodeUtil;
 
 /**
  * <code>DashArray</code>
  * 
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class DashArray {
 
     private String name;
 
     private float[] dashArray;
 
     /**
      * @param name
      * @param dashArray
      */
     public DashArray( String name, float[] dashArray ) {
         super();
         this.name = name;
         this.dashArray = dashArray;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name
      *            the name to set
      */
     public void setName( String name ) {
         this.name = name;
     }
 
     /**
      * @return the dashArray
      */
     public float[] getDashArray() {
         return dashArray;
     }
 
     /**
      * @param dashArray
      *            the dashArray to set
      */
     public void setDashArray( float[] dashArray ) {
         this.dashArray = dashArray;
     }
 
     /**
      * @return the dash array as image
      */
     public BufferedImage getAsImage() {
         int width = 130;
         BufferedImage img = new BufferedImage( width, 3, BufferedImage.TYPE_INT_ARGB );
         Graphics g = img.getGraphics();
         int position = 0;
         do {
             position = draw( g, width, position );
         } while ( position < width );
         return img;
     }
 
     @Override
     public boolean equals( Object obj ) {
         if ( obj == null || !( obj instanceof DashArray ) ) {
             return false;
         }
         if ( ( name == null && ( (DashArray) obj ).name != null )
              || ( dashArray == null && ( (DashArray) obj ).dashArray != null ) ) {
             return false;
         }
        boolean equalNames = name.equals( ( (DashArray) obj ).name );
        boolean equalArrays = java.util.Arrays.equals( dashArray, ( (DashArray) obj ).getDashArray() );
        return ( equalNames && equalArrays );
 
     }
 
     @Override
     public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, dashArray );
        result = HashCodeUtil.hash( result, name );
        return result;
     }
 
     private int draw( Graphics g, int imageWidth, int position ) {
         for ( int i = 0; i < this.dashArray.length; i++ ) {
             Color c;
             if ( i % 2 == 0 ) {
                 c = Color.BLACK;
             } else {
                 c = new Color( 255, 255, 255, 0 );
             }
             g.setColor( c );
 
             int width = Math.abs( (int) this.dashArray[i] ) * 3;
             int w = width;
             if ( position + width > imageWidth ) {
                 w = imageWidth - position;
             }
             g.fillRect( position, 0, w, 2 );
             position = position + width;
             if ( position >= imageWidth ) {
                 break;
             }
         }
         return position;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return name;
     }
 
 }
