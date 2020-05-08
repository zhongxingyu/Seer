 //$HeadURL$
 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2008 by:
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
 
 package org.deegree.igeo.commands.digitize;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.deegree.datatypes.QualifiedName;
 import org.deegree.framework.util.GeometryUtils;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.commands.CommandHelper;
 import org.deegree.igeo.dataadapter.FeatureAdapter;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.kernel.Command;
 import org.deegree.kernel.CommandException;
 import org.deegree.model.feature.Feature;
 import org.deegree.model.spatialschema.Curve;
 import org.deegree.model.spatialschema.Geometry;
 import org.deegree.model.spatialschema.GeometryException;
 import org.deegree.model.spatialschema.GeometryFactory;
 import org.deegree.model.spatialschema.MultiCurve;
 import org.deegree.model.spatialschema.MultiPoint;
 import org.deegree.model.spatialschema.MultiSurface;
 import org.deegree.model.spatialschema.Point;
 import org.deegree.model.spatialschema.Position;
 import org.deegree.model.spatialschema.Ring;
 import org.deegree.model.spatialschema.Surface;
 import org.deegree.model.spatialschema.SurfaceBoundary;
 import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
 
 /**
  * {@link Command} implementation for deleting one or more vertices from a {@link Surface} or a {@link Curve}
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version. $Revision$, $Date$
  */
 public class DeleteVertexCommand extends MoveVertexCommand {
 
     /**
      * 
      * @param appCont
      * @param feature
      * @param geomProperty
      * @param sourcePoint
      */
     public DeleteVertexCommand( ApplicationContainer<?> appCont, Feature feature, QualifiedName geomProperty,
                                 Point sourcePoint ) {
         super( appCont, feature, geomProperty, sourcePoint, null );
         name = new QualifiedName( "Delete Vertex" );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.kernel.Command#execute()
      */
     public void execute()
                             throws Exception {
         Iterator<Feature> iter = featureCollection.iterator();
         while ( iter.hasNext() ) {
             Feature feature = iter.next();
             if ( geomProperty == null ) {
                 geomProperty = CommandHelper.findGeomProperty( feature );
             }
             Geometry geom = (Geometry) feature.getProperties( geomProperty )[0].getValue();
             old.put( feature, geom );
 
             boolean nearest = verticesOpt.handleNearest();
             if ( geom instanceof Point ) {
                 throw new CommandException( Messages.getMessage( Locale.getDefault(), "$MD10314" ) );
             } else if ( geom instanceof Curve ) {
                 geom = handleCurve( (Curve) geom, nearest );
             } else if ( geom instanceof Surface ) {
                 geom = handleSurface( (Surface) geom, nearest );
             } else if ( geom instanceof MultiPoint ) {
                 geom = handleMultiPoint( (MultiPoint) geom, nearest );
             } else if ( geom instanceof MultiCurve ) {
                 geom = handleMultiCurve( (MultiCurve) geom, nearest );
             } else if ( geom instanceof MultiSurface ) {
                 geom = handleMultiSurface( (MultiSurface) geom, nearest );
             }
             setGeometryProperty( feature, geom );
             Layer layer = appCont.getMapModel( null ).getLayersSelectedForAction( MapModel.SELECTION_EDITING ).get( 0 );
             FeatureAdapter fa = (FeatureAdapter) layer.getDataAccess().get( 0 );
             fa.updateFeature( feature );
         }
         performed = true;
         fireCommandProcessedEvent();
     }
 
     private List<Point> filterPoints( Point[] points, Point filterPoint ) {
         List<Point> pointList = new ArrayList<Point>( points.length );
         for ( Point point : points ) {
             if ( point != filterPoint ) {
                 pointList.add( point );
             }
         }
         return pointList;
     }
 
     private Position[] filterPositions( Position[] positions, int minPos ) {
         double tolerance = sourcePoint.getTolerance();
         List<Position> posList = new ArrayList<Position>( positions.length );
         for ( Position position : positions ) {
             if ( GeometryUtils.distance( position, sourcePoint.getPosition() ) > tolerance ) {
                 posList.add( position );
             }
         }
         if ( posList.size() < minPos ) {
             throw new CommandException( Messages.getMessage( Locale.getDefault(), "$MD10315", minPos ) );
         }
         return posList.toArray( new Position[posList.size()] );
     }
 
     private Position[] filterPositions( Position[] positions, Position[] position, int minPos ) {
         List<Position> posList = new ArrayList<Position>( positions.length );
         for ( Position position2 : positions ) {
             if ( position.length == 1 ) {
                 if ( position2 != position[0] ) {
                     posList.add( position2 );
                 }
             } else {
                 if ( position2 != position[0] && position2 != position[1] ) {
                     posList.add( position2 );
                 }
             }
         }
         if ( posList.size() < minPos ) {
             throw new CommandException( Messages.getMessage( Locale.getDefault(), "$MD10315", minPos ) );
         }
         if ( position.length == 2 ) {
             // first an last point of a ring has been removed so remaining positions
             // must be closed again
             posList.add( GeometryFactory.createPosition( posList.get( 0 ).getX(), posList.get( 0 ).getY(),
                                                          posList.get( 0 ).getZ() ) );
         }
         return posList.toArray( new Position[posList.size()] );
     }
 
     /**
      * 
      * @param feature
      * @param curve
      * @param nearest
      * @return
      * @throws GeometryException
      */
     private Geometry handleCurve( Curve curve, boolean nearest )
                             throws GeometryException {
         Position[] positions = curve.getAsLineString().getPositions();
         if ( nearest ) {
             // just remove position nearest nearest to clickpoint
             Position[] position = new Position[] { findNearest( positions ) };
             positions = filterPositions( positions, position, 2 );
         } else {
             positions = filterPositions( positions, 2 );
         }
         return GeometryFactory.createCurve( positions, curve.getCoordinateSystem() );
 
     }
 
     /**
      * 
      * @param geom
      * @param nearest
      * @return
      * @throws GeometryException
      */
     private Geometry handleMultiCurve( MultiCurve geom, boolean nearest )
                             throws GeometryException {
         Curve[] curves = geom.getAllCurves();
         List<Curve> curveList = new ArrayList<Curve>( curves.length );
         if ( nearest ) {
             // just remove position nearest nearest to clickpoint
             Position[][] positions = new Position[curves.length][];
             for ( int i = 0; i < curves.length; i++ ) {
                 positions[i] = curves[i].getAsLineString().getPositions();
             }
             Position[] position = findNearest( positions );
             for ( int i = 0; i < positions.length; i++ ) {
                 positions[i] = filterPositions( positions[i], position, 0 );
                 if ( positions[i].length > 1 ) {
                     curveList.add( GeometryFactory.createCurve( positions[i], geom.getCoordinateSystem() ) );
                 }
             }
         } else {
             for ( Curve curve : curves ) {
                 Position[] positions = curve.getAsLineString().getPositions();
                 positions = filterPositions( positions, 0 );
                 if ( positions.length > 1 ) {
                     curveList.add( GeometryFactory.createCurve( positions, geom.getCoordinateSystem() ) );
                 }
             }
         }
         if ( curveList.size() == 0 ) {
             throw new CommandException( Messages.getMessage( Locale.getDefault(), "$MD10316" ) );
         }
         return GeometryFactory.createMultiCurve( curves );
     }
 
     /**
      * 
      * @param geom
      * @param nearest
      * @return
      */
     private Geometry handleMultiPoint( MultiPoint geom, boolean nearest ) {
         Point[] points = geom.getAllPoints();
         List<Point> pointList = new ArrayList<Point>( points.length );
         double tolerance = sourcePoint.getTolerance();
         if ( nearest ) {
             // just move position nearest to click point
             Point point = findNearest( points );
             pointList = filterPoints( points, point );
         } else {
             for ( Point point : points ) {
                 if ( GeometryUtils.distance( point.getPosition(), sourcePoint.getPosition() ) > tolerance ) {
                     pointList.add( point );
                 }
             }
         }
         if ( pointList.size() == 0 ) {
             throw new CommandException( Messages.getMessage( Locale.getDefault(), "$MD10317" ) );
         }
         return GeometryFactory.createMultiPoint( points );
     }
 
     /**
      * 
      * @param geom
      * @param nearest
      * @return
      * @throws GeometryException
      */
     private Geometry handleMultiSurface( MultiSurface geom, boolean nearest )
                             throws GeometryException {
         Surface[] surfaces = geom.getAllSurfaces();
         List<Surface> surfaceList = new ArrayList<Surface>( surfaces.length );
         if ( nearest ) {
             // just move position nearest to click point
 
             // find nearest position
             List<Position[]> list = new ArrayList<Position[]>();
             for ( Surface surface : surfaces ) {
                 SurfaceBoundary sb = surface.getSurfaceBoundary();
                 Ring[] inner = sb.getInteriorRings();
                 list.add( sb.getExteriorRing().getPositions() );
                 for ( int i = 0; i < inner.length; i++ ) {
                     list.add( inner[i].getPositions() );
                 }
             }
             Position[][] positions = list.toArray( new Position[list.size()][] );
             Position[] position = findNearest( positions );
 
             // delete nearest position
             for ( Surface surface : surfaces ) {
                 SurfaceBoundary sb = surface.getSurfaceBoundary();
                 Position[] ext = sb.getExteriorRing().getPositions();
                 ext = validateRing( filterPositions( ext, position, 4 ) );
                 Ring[] inner = sb.getInteriorRings();
                 List<Position[]> innerList = new ArrayList<Position[]>( inner.length );
                 for ( Ring ring : inner ) {
                     Position[] in = ring.getPositions();
                     in = filterPositions( in, position, 0 );
                     if ( in.length > 3 ) {
                         innerList.add( validateRing( in ) );
                     }
                 }
                 Position[][] innerPos = innerList.toArray( new Position[innerList.size()][] );
                 surfaceList.add( GeometryFactory.createSurface( ext, innerPos, new SurfaceInterpolationImpl(),
                                                                 surface.getCoordinateSystem() ) );
             }
         } else {
             for ( Surface surface : surfaces ) {
                 surfaceList.add( (Surface) handleSurface( surface, false ) );
             }
         }
         if ( surfaceList.size() == 0 ) {
             throw new CommandException( Messages.getMessage( Locale.getDefault(), "$MD10318" ) );
         }
        return GeometryFactory.createMultiSurface( surfaces );
     }
 
     /**
      * 
      * @param surface
      * @param nearest
      * @return
      * @throws GeometryException
      */
     private Geometry handleSurface( Surface surface, boolean nearest )
                             throws GeometryException {
         SurfaceBoundary sb = surface.getSurfaceBoundary();
         Position[] ext = sb.getExteriorRing().getPositions();
         Ring[] inner = sb.getInteriorRings();
         List<Position[]> innerList = new ArrayList<Position[]>( inner.length );
         if ( nearest ) {
             // just add external ring for searching nearest position
             innerList.add( ext );
             for ( Ring ring : inner ) {
                 Position[] in = ring.getPositions();
                 innerList.add( in );
             }
             Position[][] tmp = innerList.toArray( new Position[innerList.size()][] );
             Position[] position = findNearest( tmp );
             ext = validateRing( filterPositions( tmp[0], position, 4 ) );
             innerList.clear();
             for ( int i = 1; i < tmp.length; i++ ) {
                 Position[] in = filterPositions( tmp[i], position, 0 );
                 if ( in.length > 3 ) {
                     innerList.add( validateRing( in ) );
                 }
             }
         } else {
             ext = validateRing( filterPositions( ext, 4 ) );
             for ( Ring ring : inner ) {
                 Position[] in = ring.getPositions();
                 in = filterPositions( in, 0 );
                 if ( in.length > 3 ) {
                     innerList.add( validateRing( in ) );
                 }
             }
         }
         Position[][] innerPos = innerList.toArray( new Position[innerList.size()][] );
         return GeometryFactory.createSurface( ext, innerPos, new SurfaceInterpolationImpl(),
                                               surface.getCoordinateSystem() );
     }
 }
