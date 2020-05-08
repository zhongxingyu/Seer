 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
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
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.style.model;
 
 import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
 import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.media.jai.JAI;
 import javax.media.jai.RenderedOp;
 
 import org.apache.batik.transcoder.TranscoderInput;
 import org.apache.batik.transcoder.TranscoderOutput;
 import org.apache.batik.transcoder.image.PNGTranscoder;
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 
 import com.sun.media.jai.codec.MemoryCacheSeekableStream;
 
 /**
  * <code>GraphicSymbol</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class GraphicSymbol extends Symbol implements Comparable<GraphicSymbol> {
 
    private static final ILogger LOG = LoggerFactory.getLogger( Symbol.class );
 
     private URL url;
 
     private double size;
 
     /**
      * @param name
      * @param url
      */
     public GraphicSymbol( String name, URL url ) {
         super( name );
         this.url = url;
     }
 
     /**
      * @param name
      * @param url
      * @param size
      */
     public GraphicSymbol( String name, URL url, double size ) {
         super( name );
         this.url = url;
         this.size = size;
     }
 
     /**
      * @return the url
      */
     public URL getUrl() {
         return url;
     }
 
     /**
      * @param url
      *            the url to set
      */
     public void setUrl( URL url ) {
         this.url = url;
     }
 
     /**
      * @return the size
      */
     public double getSize() {
         return size;
     }
 
     /**
      * @param size
      *            the size to set
      */
     public void setSize( double size ) {
         this.size = size;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.views.swing.style.model.Symbol#getIcon(int, int)
      */
     public BufferedImage getAsImage() {
         try {
             if ( url != null && url.getFile().endsWith( "svg" ) ) {
                 int size = 10;
                 ByteArrayOutputStream bos = new ByteArrayOutputStream( size * size * 4 );
                 TranscoderOutput output = new TranscoderOutput( bos );
                 PNGTranscoder trc = new PNGTranscoder();
                 InputStream in = url.openStream();// new FileInputStream( new File( url.toString() ) );
                 TranscoderInput input = new TranscoderInput( in );
 
                 if ( size > 0 ) {
                     trc.addTranscodingHint( KEY_HEIGHT, new Float( size ) );
                     trc.addTranscodingHint( KEY_WIDTH, new Float( size ) );
                 }
                 trc.transcode( input, output );
                 bos.close();
                 ByteArrayInputStream is = new ByteArrayInputStream( bos.toByteArray() );
                 MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( is );
                 RenderedOp rop = JAI.create( "stream", mcss );
                 return rop.getAsBufferedImage();
             }
             return ImageIO.read( this.url );
         } catch ( Exception e ) {
             LOG.logInfo( "Could not create icon for URL " + url + "! Exception: " + e.getMessage() );
         }
         return null;
     }
 
     /**
      * @return the mime type of the icon; null, if the format is unknown
      */
     public String getFormat() {
         String contentType = url.getFile();
         contentType = contentType.substring( contentType.lastIndexOf( '.' ) + 1, contentType.length() );
         String mimetype = null;
         if ( contentType.equalsIgnoreCase( "jpeg" ) ) {
             mimetype = "image/jpeg";
         } else if ( contentType.equalsIgnoreCase( "jpg" ) ) {
             mimetype = "image/jpeg";
         } else if ( contentType.equalsIgnoreCase( "gif" ) ) {
             mimetype = "image/gif";
         } else if ( contentType.equalsIgnoreCase( "png" ) ) {
             mimetype = "image/png";
         } else if ( contentType.equalsIgnoreCase( "bmp" ) ) {
             mimetype = "image/bmp";
         } else if ( contentType.equalsIgnoreCase( "tif" ) ) {
             mimetype = "image/tiff";
         } else if ( contentType.equalsIgnoreCase( "tiff" ) ) {
             mimetype = "image/tiff";
         } else if ( contentType.equalsIgnoreCase( "svg" ) ) {
             mimetype = "image/svg+xml";
         }
         return mimetype;
     }
 
     public int compareTo( GraphicSymbol o ) {
         if ( o.getName() == null && this.getName() != null ) {
             return 1;
         }
         if ( o.getName() != null && this.getName() == null ) {
             return -1;
         }
         return this.getName().compareTo( o.getName() );
     }
 }
