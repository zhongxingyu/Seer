 //$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2011 by:
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
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.igeo.modules.georef;
 
 import static org.deegree.igeo.modules.georef.ControlPointModel.State.Left;
 import static org.deegree.igeo.modules.georef.ControlPointModel.State.Right;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 import javax.xml.namespace.QName;
 
 import org.deegree.datatypes.QualifiedName;
 import org.deegree.datatypes.UnknownTypeException;
 import org.deegree.graphics.transformation.GeoTransform;
 import org.deegree.igeo.dataadapter.MemoryFeatureAdapter;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.model.feature.Feature;
 import org.deegree.model.feature.FeatureCollection;
 import org.deegree.model.feature.FeatureFactory;
 import org.deegree.model.feature.FeatureProperty;
 import org.deegree.model.feature.schema.FeatureType;
 import org.deegree.model.feature.schema.PropertyType;
 import org.deegree.model.spatialschema.GeometryFactory;
 
 /**
  * 
  * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
  * @author last edited by: $Author: stranger $
  * 
  * @version $Revision: $, $Date: $
  */
 public class ControlPointModel extends AbstractTableModel {
 
     private static final long serialVersionUID = -4947856920124504250L;
 
     private static FeatureType GEOREF_FTYPE;
 
     static {
         try {
             QualifiedName ptname = new QualifiedName( new QName( "http://www.opengis.net/gml", "GeometryPropertyType" ) );
             PropertyType pt = FeatureFactory.createPropertyType( new QualifiedName( new QName( "geometry" ) ), ptname,
                                                                  false );
             GEOREF_FTYPE = FeatureFactory.createFeatureType( "georef", false, new PropertyType[] { pt } );
         } catch ( UnknownTypeException e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private LinkedList<Point> points = new LinkedList<Point>();
 
     private State state = Left;
 
     private MapModel left, right;
 
     private Layer leftLayer, rightLayer;
 
     public void updateMaps( MapModel left, Layer leftLayer, MapModel right, Layer rightLayer ) {
         this.left = left;
         this.leftLayer = leftLayer;
         this.right = right;
         this.rightLayer = rightLayer;
     }
 
     @Override
     public int getRowCount() {
         return points.size();
     }
 
     @Override
     public int getColumnCount() {
         return 6;
     }
 
     @Override
     public String getColumnName( int columnIndex ) {
         switch ( columnIndex ) {
         case 0:
             return "<html>" + Messages.get( "$DI10079" ) + "<br>x";
         case 1:
             return "<html>" + Messages.get( "$DI10079" ) + "<br>y";
         case 2:
             return "<html>" + Messages.get( "$DI10080" ) + "<br>x";
         case 3:
             return "<html>" + Messages.get( "$DI10080" ) + "<br>y";
         case 4:
             return "<html>" + Messages.get( "$DI10084" ) + "<br>x (m)";
         case 5:
             return "<html>" + Messages.get( "$DI10084" ) + "<br>y (m)";
         }
         return null;
     }
 
     @Override
     public Class<?> getColumnClass( int columnIndex ) {
         return Double.class;
     }
 
     @Override
     public boolean isCellEditable( int rowIndex, int columnIndex ) {
         return columnIndex <= 3;
     }
 
     @Override
     public Object getValueAt( int rowIndex, int columnIndex ) {
         Point p = points.get( rowIndex );
         switch ( columnIndex ) {
         case 0:
             return p.x0;
         case 1:
             return p.y0;
         case 2:
             return p.x1;
         case 3:
             return p.y1;
         case 4:
             return p.resx;
         case 5:
             return p.resy;
         }
         return null;
     }
 
     @Override
     public void setValueAt( Object aValue, int rowIndex, int columnIndex ) {
         Double val = (Double) aValue;
         Point p = points.get( rowIndex );
         switch ( columnIndex ) {
         case 0:
             p.x0 = val;
             break;
         case 1:
             p.y0 = val;
             break;
         case 2:
             p.x1 = val;
             break;
         case 3:
             p.y1 = val;
             break;
         }
         updateMaps();
         AffineTransformation.approximate( points );
         fireTableDataChanged();
     }
 
     public void newPoint() {
         points.add( new Point() );
         state = Left;
         fireTableDataChanged();
     }
 
     public void removeAll() {
         points.clear();
         state = Left;
         fireTableDataChanged();
         updateMaps();
     }
 
     public void remove( int[] idx ) {
         List<Point> list = new ArrayList<Point>();
         for ( int i : idx ) {
             list.add( points.get( i ) );
         }
         points.removeAll( list );
         fireTableDataChanged();
         if ( !points.isEmpty() && points.getLast().x0 != null && points.getLast().x1 != null ) {
             newPoint();
         }
     }
 
     public void next( double x, double y ) {
         if ( points.isEmpty() ) {
             newPoint();
         }
         switch ( state ) {
         case Left:
             state = Right;
             points.getLast().x0 = x;
             points.getLast().y0 = y;
             break;
         case Right:
             points.getLast().x1 = x;
             points.getLast().y1 = y;
             newPoint();
             state = Left;
             break;
         }
         fireTableDataChanged();
     }
 
     public State getState() {
         return state;
     }
 
     public List<Point> getPoints() {
         return points;
     }
 
     public void updateMaps() {
        if ( leftLayer == null || rightLayer == null ) {
            return;
        }
         MemoryFeatureAdapter leftData = (MemoryFeatureAdapter) leftLayer.getDataAccess().get( 0 );
         MemoryFeatureAdapter rightData = (MemoryFeatureAdapter) rightLayer.getDataAccess().get( 0 );
         FeatureCollection col = leftData.getFeatureCollection();
         // the memory fa seems to work on the live feature collection...
         while ( col.size() > 0 ) {
             leftData.deleteFeature( col.getFeature( 0 ) );
         }
         col = rightData.getFeatureCollection();
         while ( col.size() > 0 ) {
             rightData.deleteFeature( col.getFeature( 0 ) );
         }
         int cnt = 0;
         for ( Point p : points ) {
             ++cnt;
             if ( p.x0 == null ) {
                 continue;
             }
             org.deegree.model.spatialschema.Point geom = GeometryFactory.createPoint( p.x0, p.y0, null );
             FeatureProperty prop = FeatureFactory.createFeatureProperty( new QualifiedName( new QName( "geometry" ) ),
                                                                          geom );
             Feature f = FeatureFactory.createFeature( "left_" + cnt, GEOREF_FTYPE, new FeatureProperty[] { prop } );
             leftData.insertFeature( f );
 
             if ( p.x1 == null ) {
                 continue;
             }
             geom = GeometryFactory.createPoint( p.x1, p.y1, null );
             prop = FeatureFactory.createFeatureProperty( new QualifiedName( new QName( "geometry" ) ), geom );
             f = FeatureFactory.createFeature( "right_" + cnt, GEOREF_FTYPE, new FeatureProperty[] { prop } );
             rightData.insertFeature( f );
         }
     }
 
     public void clickedLeft( int x, int y ) {
         if ( state == State.Left ) {
             GeoTransform gt = left.getToTargetDeviceTransformation();
             double dx = gt.getSourceX( x );
             double dy = gt.getSourceY( y );
             next( dx, dy );
             org.deegree.model.spatialschema.Point p = GeometryFactory.createPoint( dx, dy, null );
             FeatureProperty prop = FeatureFactory.createFeatureProperty( new QualifiedName( new QName( "geometry" ) ),
                                                                          p );
             Feature f = FeatureFactory.createFeature( "left_" + points.size(), GEOREF_FTYPE,
                                                       new FeatureProperty[] { prop } );
             ( (MemoryFeatureAdapter) leftLayer.getDataAccess().get( 0 ) ).insertFeature( f );
         }
     }
 
     public void clickedRight( int x, int y ) {
         if ( state == State.Right ) {
             GeoTransform gt = right.getToTargetDeviceTransformation();
             double dx = gt.getSourceX( x );
             double dy = gt.getSourceY( y );
             next( dx, dy );
             org.deegree.model.spatialschema.Point p = GeometryFactory.createPoint( dx, dy, null );
             FeatureProperty prop = FeatureFactory.createFeatureProperty( new QualifiedName( new QName( "geometry" ) ),
                                                                          p );
             Feature f = FeatureFactory.createFeature( "right_" + points.size(), GEOREF_FTYPE,
                                                       new FeatureProperty[] { prop } );
             ( (MemoryFeatureAdapter) rightLayer.getDataAccess().get( 0 ) ).insertFeature( f );
         }
     }
 
     public static enum State {
         Left, Right
     }
 
     static class Point {
         Double x0, y0, x1, y1, resx, resy;
     }
 
     public void savePointsToFile( File saveFile )
                             throws IOException {
         PrintStream ps = null;
         try {
             ps = new PrintStream( saveFile );
             for ( Point p : points ) {
                 if ( p.x0 != null && p.y0 != null && p.x1 != null && p.y1 != null ) {
                     ps.println( "\"" + p.x0 + "\";\"" + p.y0 + "\";\"" + p.x1 + "\";\"" + p.y1 + "\"" );
                 } else if ( ( p.x0 != null && p.y0 != null ) && ( p.x1 == null && p.y1 == null ) ) {
                     ps.println( "\"" + p.x0 + "\";\"" + p.y0 + "\";\"\";\"\"" );
                 } else {
                     System.out.println( "ERROR" );
                     // TODO: proper error handling
                 }
             }
         } finally {
             if ( ps != null ) {
                 ps.close();
             }
         }
     }
 
     public void loadPointsFromFile( File openFile )
                             throws IOException {
         removeAll();
         BufferedReader br = null;
         try {
             br = new BufferedReader( new FileReader( openFile ) );
             while ( br.ready() != false ) {
                 String[] splittedCsvLine = br.readLine().split( ";" );
                 double x = Double.valueOf( splittedCsvLine[0].substring( 1, splittedCsvLine[0].length() - 1 ) );
                 double y = Double.valueOf( splittedCsvLine[1].substring( 1, splittedCsvLine[1].length() - 1 ) );
                 next( x, y );
                 x = Double.valueOf( splittedCsvLine[2].substring( 1, splittedCsvLine[2].length() - 1 ) );
                 y = Double.valueOf( splittedCsvLine[3].substring( 1, splittedCsvLine[3].length() - 1 ) );
                 next( x, y );
             }
             updateMaps();
         } finally {
             if ( br != null ) {
                 br.close();
             }
         }
     }
 
 }
