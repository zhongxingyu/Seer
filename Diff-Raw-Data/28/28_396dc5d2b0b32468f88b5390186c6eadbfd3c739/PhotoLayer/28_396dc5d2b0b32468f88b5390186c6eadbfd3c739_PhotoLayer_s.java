 //$HeadURL$
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2010 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  Occam Labs UG (haftungsbeschränkt)
  Godesberger Allee 139, 53175 Bonn
  Germany
  http://www.occamlabs.de/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 
 package de.occamlabs.deegree.layer.photo;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.List;
 
 import org.deegree.commons.config.DeegreeWorkspace;
 import org.deegree.commons.utils.Pair;
 import org.deegree.commons.utils.Triple;
 import org.deegree.geometry.primitive.Point;
 import org.deegree.layer.AbstractLayer;
 import org.deegree.layer.LayerData;
 import org.deegree.layer.LayerQuery;
 import org.deegree.layer.metadata.LayerMetadata;
 import org.deegree.protocol.ows.exception.OWSException;
 
 /**
  * <code>PhotoLayer</code>
  * 
  * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
  * @author last edited by: $Author: mschneider $
  * 
  * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
  */
 
 public class PhotoLayer extends AbstractLayer {
 
     private int size;
 
     private PhotoDirectoryIndex index;
 
     public PhotoLayer( DeegreeWorkspace workspace, LayerMetadata metadata, File dir, File index, boolean recursive,
                        int size ) {
         super( metadata );
        this.index = new PhotoDirectoryIndex( workspace, dir, index, recursive, getMetadata().getSpatialMetadata() );
         this.size = size;
     }
 
     @Override
     public LayerData mapQuery( LayerQuery query, List<String> headers )
                             throws OWSException {
         List<Triple<Point, BufferedImage, Integer>> points = index.query( query.getEnvelope() );
         return new PhotoLayerData( points, size );
     }
 
     @Override
     public LayerData infoQuery( LayerQuery query, List<String> headers )
                             throws OWSException {
         List<Pair<Point, File>> points = index.queryInfo( query.calcClickBox( (int) Math.round( Math.sqrt( size * size ) ) ) );
         return new PhotoLayerData( points );
     }
 
     @Override
    public void destroy(){
         index.destroy();
     }
    
 }
