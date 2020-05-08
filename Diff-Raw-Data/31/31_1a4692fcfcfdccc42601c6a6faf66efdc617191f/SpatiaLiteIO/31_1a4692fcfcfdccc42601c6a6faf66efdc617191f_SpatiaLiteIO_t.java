 /**
  * TerrainGIS 
  * Android program for mapping
  * 
  * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.kalcik.vojta.terraingis.io;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKBReader;
 import com.vividsolutions.jts.io.WKBWriter;
 
 import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
 import cz.kalcik.vojta.terraingis.layer.AttributeType;
 import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
 
 import jsqlite.Constants;
 import jsqlite.Database;
 import jsqlite.Exception;
 import jsqlite.Stmt;
 
 /**
  * class work with SpatiLite database
  * @author jules
  *
  */
 public class SpatiaLiteIO
 {    
     // constants ==========================================================================
     public static final int EPSG_SPHERICAL_MERCATOR = 3857;
     public static final int EPSG_LONLAT = 4326;
     public static final String GEOMETRY_COLUMN_NAME = "Geometry";
     public static final String ID_COLUMN_NAME = "id";
     public static final AttributeType ID_COLUMN_TYPE = AttributeType.INTEGER;
     
     // attributes =========================================================================
     private Database db;
     private WKBReader wkbReader = new WKBReader();
     private WKBWriter mWKBWriter = new WKBWriter();
     
     // public methods ======================================================================
     /**
      * constructor
      * @param path
      * @throws Exception 
      */
     public SpatiaLiteIO(String path) throws Exception
     {
     	open(path);
     }
     
     /**
      * get all layers in spatialite database
      * @return list of Layers
      * @throws Exception 
      */
     public ArrayList<Layer> getLayers() throws Exception
     {
     	ArrayList<Layer> list = new ArrayList<Layer>();
     	
     	String query = "SELECT f_table_name, type, srid FROM geometry_columns";
 
 		Stmt stmt = db.prepare(query);
 		while(stmt.step())
 		{
 			Layer layer = new Layer();
 			layer.name = stmt.column_string(0);
 			layer.type = stmt.column_string(1);
 			layer.srid = stmt.column_int(2);
 			list.add(layer);
 		}
 		stmt.close();
     	
     	return list;
     }
 
     /**
      * get layer from spatialite db by name
      * @return list of Layers
      * @throws Exception 
      */
     public Layer getLayer(String name) throws Exception
     {
         Layer result = null;
         
         String query = "SELECT f_table_name, type, srid FROM geometry_columns " +
         		"WHERE f_table_name = ?";
 
         Stmt stmt = db.prepare(query);
         stmt.bind(1, name);
         
         if(stmt.step())
         {
             result = new Layer();
             result.name = stmt.column_string(0);
             result.type = stmt.column_string(1);
             result.srid = stmt.column_int(2);
         }
         
         stmt.close();
         
         return result;
     }
     
     /**
      * check if layer have index
      * @param name
      * @return
      * @throws Exception 
      */
     public boolean isIndexEnabled(String name) throws Exception
     {
         boolean result = false;
         
         Stmt stmt;
 
         stmt = db.prepare("SELECT spatial_index_enabled FROM geometry_columns WHERE f_table_name = ?");
         stmt.bind(1, name);
         if(stmt.step())
         {
             result = (stmt.column_int(0) == 1);
         }
         stmt.close();
         
         return result;
     }
     
     /**
      * return envelope of layer
      * @param name
      * @return
      * @throws Exception 
      */
     public Envelope getEnvelopeLayer(String name, String column, boolean useRTree) throws Exception
     {
         Envelope result = null;
         
         String[] args = {name};
         
         Stmt stmt;
 
         if(useRTree)
         {
             // TODO use idx_table_column_node
             String cmd = String.format("SELECT MIN(xmin) As xmin, MAX(xmax) As xmax, " +
                     "MIN(ymin) As ymin, MAX(ymax) As ymax "+
                     "FROM \"idx_%s_%s\"", name, column);
             stmt = db.prepare(cmd);
         }
         else
         {
             db.exec("SELECT UpdateLayerStatistics('%q')", null, args);
             
             stmt = db.prepare("SELECT extent_min_x, extent_max_x, extent_min_y, extent_max_y " +
                               "from LAYER_STATISTICS where table_name=?");
             stmt.bind(1, name);
         }
         
         if(stmt.step())
         {
             result = new Envelope(stmt.column_double(0),
                     stmt.column_double(1),
                     stmt.column_double(2),
                     stmt.column_double(3));
         }
         
         stmt.close();
         
         return result;        
     }
     
     /**
      * transform coordinates between two srs
      * @param points
      * @param from
      * @param to
      * @return
      * @throws Exception 
      * @throws ParseException 
      */
     public ArrayList<Coordinate> transformSRS(ArrayList<Coordinate> points, int from, int to)
             throws Exception, ParseException
     {
         if(from == to)
         {
             return (ArrayList<Coordinate>)points.clone();
         }
         
         Stmt stmt = db.prepare("SELECT AsBinary(Transform(MakePoint(?, ?, ?), ?))");
         
         ArrayList<Coordinate> result = new ArrayList<Coordinate>();
         for(Coordinate point: points)
         {
             stmt.bind(1, point.x);
             stmt.bind(2, point.y);
             stmt.bind(3, from);
             stmt.bind(4, to);
             if(stmt.step())
             {
                 result.add(wkbReader.read(stmt.column_bytes(0)).getCoordinate());
             }
             
             stmt.clear_bindings();
             stmt.reset();
         }
         
         stmt.close();
         
         return result;        
     }
  
     /**
      * transform coordinates between two srs
      * @param point
      * @param fromSrid
      * @param toSrid
      * @return
      * @throws Exception 
      * @throws ParseException 
      */
     public Coordinate transformSRS(Coordinate point, int fromSrid, int toSrid)
             throws Exception, ParseException
     {
         Coordinate result = null;
         
         if(fromSrid == toSrid)
         {
             return (Coordinate)point.clone();
         }
                  
         Stmt stmt = db.prepare("SELECT AsBinary(Transform(MakePoint(?, ?, ?), ?))");
         
         stmt.bind(1, point.x);
         stmt.bind(2, point.y);
         stmt.bind(3, fromSrid);
         stmt.bind(4, toSrid);
         if(stmt.step())
         {
             result = wkbReader.read(stmt.column_bytes(0)).getCoordinate();
         }
         stmt.close();
         
         return result;         
     }
     
     /**
      * transform envelop coordinates between two srs
      * returned envelope is new (no reference)
      * @param envelope
      * @param fromSrid
      * @param toSrid
      * @return
      * @throws Exception 
      * @throws ParseException 
      */
     public Envelope transformSRSEnvelope(Envelope envelope, int fromSrid, int toSrid)
             throws Exception, ParseException
     {
         Envelope result = null;
         
         if(fromSrid == toSrid)
         {
             return new Envelope(envelope.getMinX(), envelope.getMaxX(),
                                 envelope.getMinY(), envelope.getMaxY());
         }
                   
         Stmt stmt = db.prepare("SELECT AsBinary(Transform(BuildMbr(?, ?, ?, ?, ?), ?))");
         
         stmt.bind(1, envelope.getMinX());
         stmt.bind(2, envelope.getMinY());
         stmt.bind(3, envelope.getMaxX());
         stmt.bind(4, envelope.getMaxY());
         stmt.bind(5, fromSrid);
         stmt.bind(6, toSrid);
         
         if(stmt.step())
         {
             result = wkbReader.read(stmt.column_bytes(0)).getEnvelope().getEnvelopeInternal();
         }
         
         stmt.close();
         
         return result;
     }
     
     /**
      * @param envelope
      * @param name
      * @param column
      * @param layerSrid
      * @param outputSrid
      * @param useRTree
      * @return objects from envelope
      * @throws Exception 
      * @throws ParseException 
      */
     public SpatialiteGeomIterator getObjectsInEnvelope(Envelope envelope, String name, String column,
                                           int layerSrid, int outputSrid, boolean useRTree)
                                                   throws Exception, ParseException
     {
         String cmd = String.format("SELECT ROWID, AsBinary(Transform(\"%s\", ?)) " +
         		"FROM \"%s\" WHERE %s", column, name, getObjectCondition(
         		        envelope, name, column, layerSrid, outputSrid, useRTree));
         Stmt stmt = db.prepare(cmd);;
         stmt.bind(1, outputSrid);
         
         return new SpatialiteGeomIterator(stmt);
     }
 
     /**
      * @param name
      * @param column
      * @param outputSrid
      * @return all objects
      * @throws Exception
      * @throws ParseException
      */
     public SpatialiteGeomIterator getAllObjects(String name, String column,
             int outputSrid) throws Exception, ParseException
     {
         String cmd = String.format("SELECT ROWID, AsBinary(Transform(\"%s\", ?)) " +
                 "FROM \"%s\"", column, name);
         Stmt stmt = db.prepare(cmd);;
         stmt.bind(1, outputSrid);
         
         return new SpatialiteGeomIterator(stmt);
     }
     
     /**
      * return object with rowid
      * @param name
      * @param column
      * @param outputSrid
      * @param rowid
      * @return
      * @throws Exception 
      * @throws ParseException 
      */
     public Geometry getObject(String name, String column, int outputSrid, int rowid)
             throws Exception, ParseException
     {
         Geometry result = null;
         
 
         String cmd = String.format("SELECT AsBinary(Transform(\"%s\", ?)) " +
         "FROM \"%s\" WHERE ROWID = ?", column, name);
         Stmt stmt = db.prepare(cmd);;
         stmt.bind(1, outputSrid);
         stmt.bind(2, rowid);
         
         if(stmt.step())
         {
             result = wkbReader.read(stmt.column_bytes(0));
         }
         
         stmt.close();
         
         return result;
     }
     
     /**
      * get name of column with geometry
      * @param name
      * @return
      * @throws Exception 
      */
     public String getColumnGeom(String name) throws Exception
     {
         String result = null;
         
         Stmt stmt = db.prepare("SELECT f_geometry_column FROM geometry_columns WHERE f_table_name = ?");
         stmt.bind(1, name);
         if(stmt.step())
         {
             result = stmt.column_string(0);
         }
         stmt.close();
         
         return result;        
     }
     
     /**
      * insert geometry to db
      * @param geom
      * @param name
      * @param column
      * @param srid
      * @throws Exception 
      */
     public void insertObject(Geometry geom, String name, String column, int inputSrid, int tableSrid,
             AttributeHeader header, AttributeRecord attributesValues, boolean usePK) throws Exception
     {
         Stmt stmt = prepareInsert(name, column, inputSrid, tableSrid, header, usePK);
         insertObject(stmt, geom, attributesValues, usePK);
         
         stmt.close(); 
     }
 
     /**
      * insert geometry (can be reused)
      * @param stmt
      * @param geom
      * @throws Exception 
      */
     public void insertObject(Stmt stmt, Geometry geom, AttributeRecord attributes, boolean usePK) throws Exception
     {
         stmt.bind(1, mWKBWriter.write(geom));
         String[] values = attributes.getValues();
         int count = values.length;
         int bindIndex = 2;
         for (int i = 0; i < count; i++)
         {
             if(!usePK && attributes.isColumnPK(i))
             {
                 continue;
             }
             
             AttributeType type = attributes.getColumnType(i);
             if(values[i] == null || type == AttributeType.TEXT)
             {
                 stmt.bind(bindIndex, values[i]);
             }
             else if(type == AttributeType.INTEGER)
             {
                try
                {
                    stmt.bind(bindIndex, Integer.parseInt(values[i]));
                }
                // if number is real
                catch (NumberFormatException e)
                {
                    stmt.bind(bindIndex, Math.round(Double.parseDouble(values[i])));
                }
             }
             else if(type == AttributeType.REAL)
             {
                 stmt.bind(bindIndex, Double.parseDouble(values[i]));
             }
             
             bindIndex++;
         }
         stmt.step();
         
         stmt.clear_bindings();
         stmt.reset();   
     }
     
     /**
      * create compiled SQL for insert more queries
      * @param name
      * @param column
      * @param inputSrid
      * @param layerSrid
      * @return
      * @throws Exception
      */
     public Stmt prepareInsert(String name, String column, int inputSrid, int layerSrid,
             AttributeHeader header, boolean usePK) throws Exception
     {
         String geomDefinition = getGeomDefinition(inputSrid, layerSrid);
 
         String query = String.format("INSERT INTO \"%s\" (\"%s\"%s) VALUES (%s%s)",
                 name, column, header.getComaNameColumns(usePK, true),
                 geomDefinition, header.getInsertSQLArgs(usePK));
         return db.prepare(query);
     }
     
     /**
      * create layer in spatialite db
      * @param name
      * @param geometryColumn
      * @param type
      * @param srid
      * @throws Exception 
      */
     public void createEmptyLayer(String name, String type, String columns, int srid) throws Exception
     {
         String[] argsTable = {name};
         String[] argsGeom = {name, GEOMETRY_COLUMN_NAME, Integer.toString(srid), type};
         String[] argsIndex = {name, GEOMETRY_COLUMN_NAME};
         
 
         String query = "CREATE TABLE \"%q\" " + columns;
         db.exec(query, null, argsTable);
         db.exec("SELECT AddGeometryColumn('%q', '%q', %q, '%q', 'XY')", null, argsGeom);
         db.exec("SELECT CreateSpatialIndex('%q', '%q')", null, argsIndex);
     }
     
     /**
      * remove layer from spatialite db
      * @param name
      * @param geometryColumn
      * @throws Exception 
      */
     public void removeLayer(String name, String geometryColumn, boolean hasIndex) throws Exception
     {
         String[] argsTable = {name};
         String[] argsGeom = {name, geometryColumn};
         
         if(hasIndex)
         {
             db.exec("SELECT DisableSpatialIndex('%q', '%q');", null, argsGeom);
             db.exec("DROP TABLE \"idx_%q_%q\";", null, argsGeom);
         }
         db.exec("SELECT DiscardGeometryColumn('%q', '%q');", null, argsGeom);
         db.exec("DROP TABLE \"%q\";", null, argsTable);
         db.exec("VACUUM;", null, argsTable);
     }
     
     /**
      * @param name of table
      * @return attribute table
      * @throws Exception 
      */
     public AttributeHeader getAttributeTable(String name) throws Exception
     {
         AttributeHeader result = new AttributeHeader();
 
         Stmt stmt = db.prepare(String.format("PRAGMA table_info('%s');", name));
         
         while(stmt.step())
         {
             //  0  |  1   |   2  |    3    |     4      | 5
             // cid | name | type | notnull | dflt_value | pk
             
             boolean isPK = (stmt.column_int(5) == 1);
             AttributeType type = AttributeType.getTypeSpatialite(stmt.column_string(2));
             if(type != null)
             {
                 String nameColumn = stmt.column_string(1);
                 
                 result.addColumn(nameColumn, type, isPK);
             }
         }
         
         stmt.close();
         
         return result;
     }
     
     /**
      * Initialize spatialite DB
      * @throws Exception 
      */
     public void initDB() throws Exception
     {
         db.exec("SELECT InitSpatialMetaData();", null, null);       
     }
     
     /**
      * @param filter names
      * @return SRS names
      * @throws Exception 
      */
     public ArrayList<SpatialiteSRS> findSRSByName(String name) throws Exception
     {
         ArrayList<SpatialiteSRS> result = new ArrayList<SpatialiteSRS>();
         
         Stmt stmt = db.prepare("SELECT srid, ref_sys_name FROM spatial_ref_sys " +
         		"WHERE ref_sys_name LIKE ? ORDER BY ref_sys_name");
         
         stmt.bind(1, "%"+name+"%");
         
         while(stmt.step())
         {
             SpatialiteSRS item = new SpatialiteSRS();
             item.srid = stmt.column_int(0);
             item.name = stmt.column_string(1);
             result.add(item);
         }
         
         stmt.close();
         
         return result;
     }
     
     /**
      * search srid by ref_sys_name column
      * @param wkt
      * @return
      * @throws Exception
      */
     public int getSridByName(String srsName) throws Exception
     {
         int result = -1;
         
         Stmt stmt = db.prepare("SELECT srid FROM spatial_ref_sys " +
                 "WHERE ref_sys_name LIKE ?");
         
         stmt.bind(1, srsName);
         
         if(stmt.step())
         {
             result = stmt.column_int(0);
         }
         
         stmt.close();
         
         return result;        
     }
     
     /**
      * @param srid
      * @return srs_wkt string by srid
      * @throws Exception
      */
     public String getWKTbySrid(int srid) throws Exception
     {
         String result = null;
         
         Stmt stmt = db.prepare("SELECT srs_wkt FROM spatial_ref_sys " +
                 "WHERE srid = ?");
         
         stmt.bind(1, srid);
         
         if(stmt.step())
         {
             result = stmt.column_string(0);
         }
         
         stmt.close();
         
         return result;        
     }
     
     /**
      * get all attributes of table
      * @param name
      * @param header
      * @return
      * @throws Exception 
      */
     public SpatialiteAttributesIterator getAttributes(String name, AttributeHeader header)
             throws Exception
     {
         String query = String.format("SELECT ROWID, %s FROM \"%s\"",
                 header.getComaNameColumns(true, false), name);
         Stmt stmt = db.prepare(query);
         
         return new SpatialiteAttributesIterator(stmt, header.getCountColumns());       
     }
 
     /**
      * get attributes of table by limit and offset
      * @param name
      * @param header
      * @param limit
      * @param offset
      * @return
      * @throws Exception
      */
     public SpatialiteAttributesIterator getAttributes(String name, AttributeHeader header, int offset, int limit)
             throws Exception
     {
         String query = String.format("SELECT ROWID, %s FROM \"%s\" LIMIT ?, ?",
                 header.getComaNameColumns(true, false), name);
         Stmt stmt = db.prepare(query);
         stmt.bind(1, offset);
         stmt.bind(2, limit);
         
         return new SpatialiteAttributesIterator(stmt, header.getCountColumns());       
     }
     
     /**
      * get attributes of one object
      * @param name
      * @param header
      * @param rowid
      * @return
      * @throws Exception
      */
     public String[] getAttributes(String name, AttributeHeader header, int rowid)
             throws Exception
     {
         String[] result = null;
         
         String query = String.format("SELECT %s FROM \"%s\" WHERE ROWID=?",
                 header.getComaNameColumns(true, false), name);
         Stmt stmt = db.prepare(query);
         
         stmt.bind(1, rowid);
         if(stmt.step())
         {
             int count = header.getCountColumns();
             result = new String[count];
                         
             for(int i=0; i < count; i++)
             {
                 result[i] = stmt.column_string(i);
             }
         }
         
         stmt.close();
         
         return result;       
     }
     
     /**
      * update attributes with new names
      * @param name
      * @param values
      * @param rowid
      * @throws Exception 
      */
     public void updateAttributes(String name, String setString, String[] values, int rowid)
             throws Exception
     {
         String query = String.format("UPDATE \"%s\" SET %s WHERE ROWID=?",
                 name, setString);
         Stmt stmt = db.prepare(query);
         
         int count = values.length;
         for(int i=0; i < count;i++)
         {
             stmt.bind(i+1, values[i]);
         }
         
         stmt.bind(count+1, rowid);
         stmt.step();
         
         stmt.close();
     }
     
     /**
      * remove object by ROWID
      * @param name
      * @param rowid
      * @throws Exception
      */
     public void removeObject(String name, int rowid) throws Exception
     {
         String query = String.format("DELETE FROM \"%s\" WHERE ROWID=?",
                 name);
         Stmt stmt = db.prepare(query);
                 
         stmt.bind(1, rowid);
         stmt.step();
         
         stmt.close();        
     }
     
     /**
      * @param envelope
      * @param name
      * @param column
      * @param layerSrid
      * @param mapSrid
      * @param useRTree
      * @param point - in outputSrid projection
      * @param bufferDistance - in units of outputSrid projection
      * @return ROWID of object 
      * @throws Exception 
      * @throws ParseException 
      */
     public ArrayList<String> getRowidNearCoordinate(Envelope envelope, String name, String column,
             int layerSrid, int mapSrid, boolean useRTree, Coordinate point, double bufferDistance)
                     throws Exception, ParseException
     {
         ArrayList<String> result = new ArrayList<String>();
         
         String columnBufferCondition;
         if(layerSrid != mapSrid)
         {
             columnBufferCondition = String.format(Locale.UK, "Transform(\"%s\", %d)", column, mapSrid);
         }
         else
         {
             columnBufferCondition = "\""+column+"\"";
         }
         
         String cmd = String.format(Locale.UK, "SELECT ROWID FROM \"%s\" WHERE " +
         		"%s AND Intersects(Buffer(%s, ?), MakePoint(?, ?, ?)) = 1", name,
         		getObjectCondition(envelope, name, column, layerSrid, mapSrid, useRTree),
         		columnBufferCondition);
         Stmt stmt = db.prepare(cmd);
         stmt.bind(1, bufferDistance);
         stmt.bind(2, point.x);
         stmt.bind(3, point.y);
         stmt.bind(4, mapSrid);
         
         while(stmt.step())
         {
             result.add(stmt.column_string(0));
         }
         
         stmt.close();
         
         return result;
     }
     
     /**
      * update geometry by rowid
      * @param name
      * @param column
      * @param rowid
      * @param geometry
      * @param layerSrid
      * @param mapSrid
      * @throws Exception
      */
     public void updateObject(String name, String column, int rowid, Geometry geometry,
             int layerSrid, int mapSrid) throws Exception
     {
         String geomDefinition = getGeomDefinition(mapSrid, layerSrid);
         
         String query = String.format("UPDATE \"%s\" SET \"%s\"=%s WHERE ROWID=?",
                 name, column, geomDefinition);
         Stmt stmt = db.prepare(query);
         stmt.bind(1, mWKBWriter.write(geometry));
         stmt.bind(2, rowid);
         
         stmt.step();
     }
     
     /**
      * close database
      * @throws Exception
      */
     public void close() throws Exception
     {
         db.close();
     }
     
     /**
      * @param name
      * @return count of objects in layer
      * @throws Exception
      */
     public int countObjects(String name) throws Exception
     {
         int result = -1;
         
         Stmt stmt = db.prepare(String.format("SELECT COUNT(*) FROM \"%s\"", name));
         
         if(stmt.step())
         {
             result = stmt.column_int(0);
         }
         stmt.close();
         
         return result;
     }
     
     /**
      * @param name
      * @param column
      * @return max length of items in table and column
      * @throws Exception
      */
     public int maxLengthOfAttribute(String name, String column) throws Exception
     {
         int result = -1;
         
         Stmt stmt = db.prepare(String.format("SELECT max(length(?)) FROM \"%s\"", name));
         stmt.bind(1, column);
         
         if(stmt.step())
         {
             result = stmt.column_int(0);
         }
         stmt.close();
         
         return result;
     }
     
     // private methods =======================================================================
     /**
      * open spatialite database
      * @param path
      * @throws Exception 
      */
     private void open(String path) throws Exception
     {
         File spatialDbFile = new File(path);
         
         db = new Database();
 
         File file = new File(path);
         boolean exist = file.exists();
         
         db.open(spatialDbFile.getAbsolutePath(), Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);
         db.exec("PRAGMA temp_store = 2;", null, null); // not use temporery files
         
         if(!exist)
         {
             initDB();
         }
     }
     
     /**
      * @param name
      * @param column
      * @param useRTree
      * @return condition for objects in envelope
      * @throws ParseException 
      * @throws Exception 
      */
     private String getObjectCondition(Envelope envelope, String name, String column,
             int layerSrid, int mapSrid, boolean useRTree) throws Exception, ParseException
     {
         if(useRTree)
         {
             if(layerSrid != mapSrid)
             {
                 envelope = transformSRSEnvelope(envelope, mapSrid, layerSrid);
             }            
             
             return String.format(Locale.UK, "ROWID IN (SELECT pkid FROM \"idx_%s_%s\" " +
             		"WHERE pkid MATCH RTreeIntersects(%f, %f, %f, %f))",
                     name, column, envelope.getMinX(), envelope.getMinY(),
                     envelope.getMaxX(), envelope.getMaxY());
         }
         else
         {
             return String.format(Locale.UK, "MbrIntersects(BuildMBR(%f, %f, %f, %f), " +
             		"Transform(%s, %d)) = 1", envelope.getMinX(), envelope.getMinY(),
                     envelope.getMaxX(), envelope.getMaxY(), column, mapSrid);           
         }        
     }
     
     /**
      * @param inputSrid
      * @param layerSrid
      * @return geom definition
      */
     private String getGeomDefinition(int inputSrid, int layerSrid)
     {
         if(inputSrid == layerSrid)
         {
             return String.format(Locale.UK, "GeomFromWKB(?, %d)", inputSrid);
         }
         else
         {
             return String.format(Locale.UK, "Transform(GeomFromWKB(?, %d), %d)", inputSrid, layerSrid);
         }        
     }
     // classes ===============================================================================
     /**
      * class for result layer values
      * @author jules
      *
      */
     public class Layer
     {
         public String name;
         public String type;
         public int srid;
     }
     
     /**
      * @author jules
      * 
      * class for return srid
      */
     public class SpatialiteSRS
     {
         public String name;
         public int srid;
         
         @Override
         public String toString()
         {
             return String.format(Locale.UK, "%s (EPSG: %d)", name, srid);
         }
     }
 }
