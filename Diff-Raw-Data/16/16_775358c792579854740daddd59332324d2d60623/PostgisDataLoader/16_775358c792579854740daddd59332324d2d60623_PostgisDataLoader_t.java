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
 
 package org.deegree.igeo.dataadapter.database.postgis;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.framework.util.StringTools;
 import org.deegree.igeo.config.JDBCConnection;
 import org.deegree.igeo.dataadapter.database.AbstractDatabaseLoader;
 import org.deegree.igeo.mapmodel.DatabaseDatasource;
 import org.deegree.io.DBPoolException;
 import org.deegree.io.datastore.sql.postgis.PGgeometryAdapter;
 import org.deegree.model.crs.CoordinateSystem;
 import org.deegree.model.spatialschema.Envelope;
 import org.deegree.model.spatialschema.GeometryException;
 import org.deegree.model.spatialschema.GeometryFactory;
 import org.deegree.model.spatialschema.Surface;
 import org.postgis.PGboxbase;
 import org.postgis.PGgeometry;
 import org.postgresql.PGConnection;
 
 /**
  * class for loading data as feature collection from a postgis database
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version. $Revision$, $Date$
  */
 public class PostgisDataLoader extends AbstractDatabaseLoader {
 
     private static final ILogger LOG = LoggerFactory.getLogger( PostgisDataLoader.class );
 
     private static final String GEOMETRY_DATATYPE_NAME = "geometry";
 
     private static final String BOX3D_DATATYPE_NAME = "box3d";
 
     private static final String PG_GEOMETRY_CLASS_NAME = "org.postgis.PGgeometry";
 
     private static final String PG_BOX3D_CLASS_NAME = "org.postgis.PGbox3d";
 
     private static Class<?> pgGeometryClass;
 
     private static Class<?> pgBox3dClass;
 
     static {
         try {
             pgGeometryClass = Class.forName( PG_GEOMETRY_CLASS_NAME );
         } catch ( ClassNotFoundException e ) {
             LOG.logError( "Cannot find class '" + PG_GEOMETRY_CLASS_NAME + "'.", e );
         }
         try {
             pgBox3dClass = Class.forName( PG_BOX3D_CLASS_NAME );
         } catch ( ClassNotFoundException e ) {
             LOG.logError( "Cannot find class '" + PG_BOX3D_CLASS_NAME + "'.", e );
         }
     }
 
     /**
      * 
      * @param datasource
      */
     public PostgisDataLoader( DatabaseDatasource datasource ) {
         super( datasource );
     }
 
     @Override
     protected Object handleGeometryValue( Object value, CoordinateSystem crs )
                             throws GeometryException {
         value = PGgeometryAdapter.wrap( (PGgeometry) value, crs );
         return value;
     }
 
     @Override
     protected PreparedStatement createPreparedStatement( DatabaseDatasource datasource, Envelope envelope,
                                                          Connection conn )
                             throws GeometryException, SQLException {
         // special case if all features need to be requested, eg. for the classification
         if ( envelope == null ) {
             return conn.prepareStatement( datasource.getSqlTemplate() );
         }
 
         PreparedStatement stmt;
         String envCRS = envelope.getCoordinateSystem().getLocalName();
         String nativeCRS = getSRSCode( datasource.getSRID() );
 
         PGboxbase box = PGgeometryAdapter.export( envelope );
         Surface surface = GeometryFactory.createSurface( envelope, envelope.getCoordinateSystem() );
         PGgeometry pggeom = PGgeometryAdapter.export( surface, Integer.parseInt( envCRS ) );
         StringBuffer query = new StringBuffer( 1000 );
         if ( nativeCRS.equals( "-1" ) ) {
             query.append( " (" );
             query.append( datasource.getGeometryFieldName() );
            query.append( " && ST_SetSRID( ?, -1) " );
            query.append( " AND ST_Intersects(" );
             query.append( datasource.getGeometryFieldName() );
            query.append( ",ST_SetSRID( ?,-1 ) ) ) " );
         } else {
             // use the bbox operator (&&) to filter using the spatial index
             query.append( " (" );
             query.append( datasource.getGeometryFieldName() );
            query.append( " && ST_Transform(ST_SetSRID( ?, " );
             query.append( envCRS );
             query.append( "), " );
             query.append( nativeCRS );
            query.append( ")) AND ST_Intersects(" );
             query.append( datasource.getGeometryFieldName() );
            query.append( ",ST_Transform(?, " );
             query.append( nativeCRS );
             query.append( "))" );
         }
 
         String sql = datasource.getSqlTemplate();
         System.out.println( sql );
         if ( sql.trim().toUpperCase().endsWith( " WHERE" ) ) {
             LOG.logDebug( "performed SQL: ", sql + query );
             stmt = conn.prepareStatement( sql + query );
         } else if ( sql.trim().toUpperCase().indexOf( " WHERE " ) < 0 ) {
             LOG.logDebug( "performed SQL: ", sql + " WHERE " + query );
             stmt = conn.prepareStatement( sql + " WHERE " + query );
         } else {
             LOG.logDebug( "performed SQL: ", sql + " AND " + query );
             stmt = conn.prepareStatement( sql + " AND " + query );
         }
 
         // TODO
         // if connection is not available ask user updated connection parameters
         stmt.setObject( 1, box, java.sql.Types.OTHER );
         stmt.setObject( 2, pggeom, java.sql.Types.OTHER );
         stmt.setMaxRows( maxFeatures );
         // seems that not every postgres version supports this
         // stmt.setQueryTimeout( timeout );
         System.out.println( stmt );
         return stmt;
     }
 
     /**
      * @param srid
      * @return
      */
     private static String getSRSCode( String srid ) {
         if ( srid.indexOf( ":" ) > -1 ) {
             String[] t = StringTools.toArray( srid, ":", false );
             return t[t.length - 1];
         }
         return srid;
     }
 
     @Override
     protected Connection acquireConnection( JDBCConnection jdbc )
                             throws DBPoolException, SQLException {
         Connection conn = super.acquireConnection( jdbc );
         PGConnection pgConn = (PGConnection) conn;
         pgConn.addDataType( GEOMETRY_DATATYPE_NAME, pgGeometryClass );
         pgConn.addDataType( BOX3D_DATATYPE_NAME, pgBox3dClass );
         return conn;
     }
 }
