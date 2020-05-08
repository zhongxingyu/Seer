 //$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2012 by:
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
 package org.deegree.igeo.dataadapter.database;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 
 import org.deegree.datatypes.QualifiedName;
 import org.deegree.igeo.config.DatabaseDatasourceType;
 import org.deegree.igeo.config.DatabaseDatasourceType.GeometryField;
 import org.deegree.igeo.config.JDBCConnection;
 import org.deegree.igeo.config.JDBCConnectionType;
 import org.deegree.igeo.jdbc.DatabaseTestConfig;
 import org.deegree.igeo.mapmodel.DatabaseDatasource;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.model.crs.CRSFactory;
 import org.deegree.model.crs.CoordinateSystem;
 import org.deegree.model.crs.UnknownCRSException;
 import org.deegree.model.feature.Feature;
 import org.deegree.model.feature.FeatureFactory;
 import org.deegree.model.feature.FeatureProperty;
 import org.deegree.model.spatialschema.GeometryFactory;
 import org.junit.After;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * test cases for {@link DatabaseFeatureAdapter}
  * 
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { DatabaseTestConfig.class })
 public class DatabaseFeatureAdapterTest {
 
     private static final String INT_COL = "INT_COL";
 
     private static final String GEO_COL = "GEO_COL";
 
     private static final String DECIMAL_COL = "DECIMAL_COL";
 
     private static final String DATE_COL = "DATE_COL";
 
     private static final String STRING_COL = "STRING_COL";
 
     private static final String FLOAT_COL = "FLOAT_COL";
 
     private static final String tableName = "integrationTest";
 
     private static URI appNamespace;
 
     static {
         try {
             appNamespace = new URI( "http://www.deegree.org/igeodesktop" );
         } catch ( URISyntaxException e ) {
             e.printStackTrace();
         }
     }
 
     @Autowired
     @Qualifier("sqlserver2008")
     private JDBCConnection connection;
 
     @Before
     public void createTable()
                             throws Exception {
         Connection conn = aquireConnection();
 
         Statement statement = conn.createStatement();
         String sql = getSQLServer2008CreateTableStatement();
         try {
             statement.execute( sql );
         } finally {
             statement.close();
             conn.close();
         }
     }
 
     @After
     public void deleteTable()
                             throws Exception {
         Connection conn = aquireConnection();
 
         Statement statement = conn.createStatement();
         try {
             String sql = getSQLServer2008DeleteTableStatement();
 
             statement.execute( sql );
         } finally {
             statement.close();
             conn.close();
         }
     }
 
     @Test
     public void testCommitChangesDefaultFeatureWithNullValues()
                             throws Exception {
         String dataSourceName = "itTest";
         DatabaseDatasource datasource = createDatasource( dataSourceName );
         DatabaseFeatureAdapter databaseFeatureAdapter = createFeatureAdapter( datasource );
 
         Feature featureToInsert = getFeature( dataSourceName, databaseFeatureAdapter );
 
         databaseFeatureAdapter.insertFeature( featureToInsert );
         databaseFeatureAdapter.commitChanges();
 
         Connection conn = aquireConnection();
         Statement statement = conn.createStatement();
         try {
             String sql = "SELECT * FROM " + tableName;
             ResultSet resultSet = statement.executeQuery( sql );
             resultSet.next();
             assertAllNull( resultSet );
         } finally {
             statement.close();
             conn.close();
         }
     }
 
     private void assertAllNull( ResultSet resultSet )
                             throws SQLException {
         assertNull( resultSet.getString( STRING_COL ) );
         assertNull( resultSet.getObject( GEO_COL ) );
         assertNull( resultSet.getDate( DATE_COL ) );
         assertNull( resultSet.getObject( INT_COL ) );
         assertNull( resultSet.getObject( DECIMAL_COL ) );
         assertNull( resultSet.getObject( FLOAT_COL ) );
     }
 
     @Test
     public void testCommitChangesFeatureWithValues()
                             throws Exception {
         String dataSourceName = "itTest";
         DatabaseDatasource datasource = createDatasource( dataSourceName );
         DatabaseFeatureAdapter databaseFeatureAdapter = createFeatureAdapter( datasource );
 
         Feature featureToInsert = getFeature( dataSourceName, databaseFeatureAdapter );
 
         int intValue = 42;
         String stringValue = "fantasy";
         Date dateValue = new Date();
         double decimalValue = 42.42;
         float floatValue = 24.24f;
 
         setProperty( featureToInsert, INT_COL, intValue );
         setProperty( featureToInsert, STRING_COL, stringValue );
         setProperty( featureToInsert, DATE_COL, dateValue );
         setProperty( featureToInsert, DECIMAL_COL, decimalValue );
         setProperty( featureToInsert, FLOAT_COL, floatValue );
 
         databaseFeatureAdapter.insertFeature( featureToInsert );
         databaseFeatureAdapter.commitChanges();
 
         Connection conn = aquireConnection();
         Statement statement = conn.createStatement();
         try {
             String sql = "SELECT * FROM " + tableName;
             ResultSet resultSet = statement.executeQuery( sql );
             resultSet.next();
             assertEquals( intValue, resultSet.getInt( INT_COL ) );
             assertEquals( stringValue, resultSet.getString( STRING_COL ) );
             // Assert failed:
             // http://stackoverflow.com/questions/7982969/how-is-sql-servers-timestamp2-supposed-to-work-in-jdbc
             // assertEquals( dateValue.getTime(), resultSet.getTimestamp( DATE_COL ).getTime() );
             assertEquals( decimalValue, resultSet.getDouble( DECIMAL_COL ), 0 );
             assertEquals( floatValue, resultSet.getFloat( FLOAT_COL ), 0 );
         } finally {
             statement.close();
             conn.close();
         }
     }
 
     private void setProperty( Feature featureToInsert, String column, Object value ) {
         featureToInsert.removeProperty( new QualifiedName( column, appNamespace ) );
         featureToInsert.addProperty( createProperty( column, value ) );
     }
 
     private FeatureProperty createProperty( String column, Object value ) {
         return FeatureFactory.createFeatureProperty( new QualifiedName( column, appNamespace ), value );
     }
 
     private Feature getFeature( String dataSourceName, DatabaseFeatureAdapter databaseFeatureAdapter )
                             throws URISyntaxException {
         QualifiedName featureType = new QualifiedName( dataSourceName, appNamespace );
         Feature featureToInsert = databaseFeatureAdapter.getDefaultFeature( featureType );
         return featureToInsert;
     }
 
     private DatabaseFeatureAdapter createFeatureAdapter( DatabaseDatasource datasource )
                             throws UnknownCRSException {
         MapModel mapModel = Mockito.mock( MapModel.class );
         CoordinateSystem crs = CRSFactory.create( "epsg:4326" );
         Mockito.when( mapModel.getMaxExtent() ).thenReturn( GeometryFactory.createEnvelope( -90, -90, 90, 90, crs ) );
         Mockito.when( mapModel.getEnvelope() ).thenReturn( GeometryFactory.createEnvelope( -90, -90, 90, 90, crs ) );
         Mockito.when( mapModel.getCoordinateSystem() ).thenReturn( crs );
         DatabaseFeatureAdapter databaseFeatureAdapter = new DatabaseFeatureAdapter( datasource, null, mapModel, false );
         return databaseFeatureAdapter;
     }
 
     private DatabaseDatasource createDatasource( String dataSourceName ) {
         DatabaseDatasourceType dsType = new DatabaseDatasourceType();
         JDBCConnectionType jdbcJaxb = new JDBCConnectionType();
         jdbcJaxb.setDriver( connection.getDriver() );
         jdbcJaxb.setPassword( connection.getPassword() );
         jdbcJaxb.setUser( connection.getUser() );
         jdbcJaxb.setUrl( connection.getUrl() );
         dsType.setConnection( jdbcJaxb );
         dsType.setNativeCRS( "epsg:4326" );
         GeometryField geometryField = new GeometryField();
         geometryField.setSrs( "epsg:4326" );
         geometryField.setValue( "GEO_COL" );
         dsType.setGeometryField( geometryField );
         dsType.setPrimaryKeyField( INT_COL );
         dsType.setSqlTemplate( "SELECT * FROM " + tableName );
         dsType.setName( dataSourceName );
         DatabaseDatasource datasource = new DatabaseDatasource( dsType, null, null, connection );
         return datasource;
     }
 
     private Connection aquireConnection()
                             throws InstantiationException, IllegalAccessException, ClassNotFoundException {
         try {
             DriverManager.registerDriver( (Driver) Class.forName( connection.getDriver() ).newInstance() );
             Connection conn = DriverManager.getConnection( connection.getUrl(), connection.getUser(),
                                                            connection.getPassword() );
             return conn;
         } catch ( SQLException e ) {
            Assume.assumeNoException( e );
         }
        return null;
     }
 
     private String getSQLServer2008CreateTableStatement() {
         StringBuffer sql = new StringBuffer();
         sql.append( "CREATE TABLE " + tableName + "(" );
         sql.append( INT_COL + " int, " );
         sql.append( STRING_COL + " varchar(20), " );
         sql.append( DECIMAL_COL + " decimal(10,2), " );
         sql.append( FLOAT_COL + " float, " );
         sql.append( DATE_COL + " datetime2, " );
         sql.append( GEO_COL + " geometry" );
         sql.append( ");" );
         return sql.toString();
     }
 
     private String getSQLServer2008DeleteTableStatement() {
         return "DROP TABLE " + tableName;
     }
 
 }
