 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.wupp.geocpm.ie;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.math.BigDecimal;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 import java.util.Properties;
 import java.util.TimeZone;
 
 /**
  * This is a rather dirty implementation of a GeoCPM.ein file parser.
  *
  * @version  $Revision$, $Date$
  */
 // TODO: test with GeoCPMExport
 public class GeoCPMImport {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(GeoCPMImport.class);
 
     public static final String SECTION_CONFIG = "Configuration";      // NOI18N
     public static final String SECTION_POINTS = "POINTS";             // NOI18N
     public static final String SECTION_TRIANGLES = "TRIANGLES";       // NOI18N
     public static final String SECTION_CURVES = "CURVES";             // NOI18N
     public static final String SECTION_SOURCE_DRAIN = "SOURCE-DRAIN"; // NOI18N
     public static final String SECTION_MANHOLES = "MANHOLES";         // NOI18N
     public static final String SECTION_MARKED = "MARKED";             // NOI18N
     public static final String SECTION_RAINCURVE = "RAINCURVE";       // NOI18N
     public static final String SECTION_BK_CONNECT = "BK-CONNECT";     // NOI18N
 
     public static final String CALC_BEGIN = "Beginning of calculation";             // NOI18N
     public static final String CALC_END = "End of calculation";                     // NOI18N
     public static final String WRITE_NODE = "Write full result list Node";          // NOI18N
     public static final String WRITE_EDGE = "Write full result list Edge";          // NOI18N
     public static final String LAST_VALUES = "Last Values";                         // NOI18N
     public static final String SAVE_MARKED = "Save Marked";                         // NOI18N
     public static final String MERGE_TRIANGLES = "Merge triangles";                 // NOI18N
     public static final String MIN_CALC_TRIANGLE_SIZE = "Min. calc. triangle size"; // NOI18N
     public static final String TIME_STEP_RESTRICTION = "Time step restriction";     // NOI18N
     public static final String SAVE_VELOCITY_CURVES = "Save velosity curves";       // NOI18N
     public static final String SAVE_FLOW_CURVES = "Save flow curves";               // NOI18N
     public static final String RESULT_SAVE_LIMIT = "Result save limit";             // NOI18N
     public static final String NUMBER_OF_THREADS = "Number of threads";             // NOI18N
     public static final String Q_IN = "Ansatz Q in";                                // NOI18N
     public static final String Q_OUT = "Ansatz Q out";                              // NOI18N
 
     public static final String FIELD_SEP = "     "; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // NOI18N
 
     private final transient BufferedReader reader;
     private final transient String user;
     private final transient String password;
     private final transient String dbUrl;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new GeoCPMImport object.
      *
      * @param   input     DOCUMENT ME!
      * @param   user      DOCUMENT ME!
      * @param   password  DOCUMENT ME!
      * @param   dbUrl     DOCUMENT ME!
      *
      * @throws  ClassNotFoundException  DOCUMENT ME!
      */
     public GeoCPMImport(final InputStream input, final String user, final String password, final String dbUrl)
             throws ClassNotFoundException {
         this.reader = new BufferedReader(new InputStreamReader(input));
 
         this.user = user;
         this.password = password;
         this.dbUrl = dbUrl;
 
         Class.forName("org.postgresql.Driver"); // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         final Properties p = new Properties();
         p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender"); // NOI18N
         p.put("log4j.appender.Remote.remoteHost", "localhost");                // NOI18N
         p.put("log4j.appender.Remote.port", "4445");                           // NOI18N
         p.put("log4j.appender.Remote.locationInfo", "true");                   // NOI18N
         p.put("log4j.rootLogger", "ALL,Remote");                               // NOI18N
         PropertyConfigurator.configure(p);
 
         final GeoCPMImport importer = new GeoCPMImport(
                 new FileInputStream("/Users/mscholl/projects/sudplan/wupp/GeoCPM_be_test.ein"),
                 "postgres",
                 "cismetz12",
                 "jdbc:postgresql://192.168.100.12/wp6_db");
 
        importer.doImport();
     }
 
     /**
      * DOCUMENT ME!
      *
     * @return  DOCUMENT ME!
     *
      * @throws  SQLException           DOCUMENT ME!
      * @throws  IOException            DOCUMENT ME!
      * @throws  ParseException         DOCUMENT ME!
      * @throws  IllegalStateException  DOCUMENT ME!
      */
    public int doImport() throws SQLException, IOException, ParseException {
         LOG.info("BEGIN IMPORT");
         final long startTime = System.currentTimeMillis();
 
         final Connection con = DriverManager.getConnection(dbUrl, user, password);
         int configId = -1;
 
         try {
             prepare(con);
 
             String line = reader.readLine();
             String section = null;
             final ConfigStruct cs = new ConfigStruct();
             StringBuilder batchSQL = new StringBuilder();
             int lineCount = 0;
 
             while (line != null) {
                 if (lineCount > 10000) {
                     insertBatch(batchSQL, con);
                     batchSQL = new StringBuilder();
                     lineCount = 0;
                 }
 
                 if (line.trim().isEmpty()) {
                     // skip
                 } else if (line.startsWith(SECTION_CONFIG)) {
                     LOG.info("processing section: " + SECTION_CONFIG);
                     section = SECTION_CONFIG;
                 } else if (line.startsWith(SECTION_POINTS)) {
                     LOG.info("processing section: " + SECTION_POINTS);
                     section = SECTION_POINTS;
                     configId = insertConfig(cs, con);
                 } else if (line.startsWith(SECTION_TRIANGLES)) {
                     LOG.info("processing section: " + SECTION_TRIANGLES);
                     section = SECTION_TRIANGLES;
                 } else if (line.startsWith(SECTION_CURVES)) {
                     LOG.info("processing section: " + SECTION_CURVES);
 
                     // insert points before post-processing triangles
                     this.insertBatch(batchSQL, con);
                     batchSQL = new StringBuilder();
                     this.postProcessTriangles(configId, con);
 
                     section = SECTION_CURVES;
                 } else if (line.startsWith(SECTION_SOURCE_DRAIN)) {
                     LOG.info("processing section: " + SECTION_SOURCE_DRAIN);
                     section = SECTION_SOURCE_DRAIN;
                 } else if (line.startsWith(SECTION_MANHOLES)) {
                     LOG.info("processing section: " + SECTION_MANHOLES);
                     section = SECTION_MANHOLES;
                 } else if (line.startsWith(SECTION_MARKED)) {
                     LOG.info("processing section: " + SECTION_MARKED);
                     section = SECTION_MARKED;
                 } else if (line.startsWith(SECTION_RAINCURVE)) {
                     LOG.info("processing section: " + SECTION_RAINCURVE);
                     section = SECTION_RAINCURVE;
                 } else if (line.startsWith(SECTION_BK_CONNECT)) {
                     LOG.info("processing section: " + SECTION_BK_CONNECT);
                     section = SECTION_BK_CONNECT;
                 } else {
                     // we're processing a section
                     if (SECTION_CONFIG.equals(section)) {
                         readConfig(cs, line);
                     } else if (SECTION_POINTS.equals(section)) {
                         readPoint(batchSQL, configId, line);
                     } else if (SECTION_TRIANGLES.equals(section)) {
                         readTriangle(batchSQL, configId, line);
                     } else if (SECTION_CURVES.equals(section)) {
                         readCurve(batchSQL, configId, line);
                     } else if (SECTION_SOURCE_DRAIN.equals(section)) {
                         readSourceDrain(batchSQL, configId, line);
                     } else if (SECTION_MANHOLES.equals(section)) {
                         readManhole(batchSQL, configId, line);
                     } else if (SECTION_MARKED.equals(section)) {
                         readMarked(batchSQL, configId, line);
                     } else if (SECTION_RAINCURVE.equals(section)) {
                         // skip for now
                     } else if (SECTION_BK_CONNECT.equals(section)) {
                         // skip for now
                         readBKConnect(batchSQL, configId, line);
                     } else {
                         throw new IllegalStateException("unknown section: " + section); // NOI18N
                     }
                 }
 
                 line = reader.readLine();
                 lineCount++;
             }
 
             insertBatch(batchSQL, con);
 
             postProcess(configId, con);
         } finally {
             try {
                 finish(con);
             } catch (final SQLException e) {
                 LOG.error("cannot perform import db", e); // NOI18N
 
                 throw e;
             } finally {
                 reader.close();
                 con.close();
             }
         }
 
         final long endTime = System.currentTimeMillis();
         final SimpleDateFormat sdf = new SimpleDateFormat("HH' hours 'mm' minutes 'ss' seconds 'SSS' milliseconds'"); // NOI18N
         sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
         LOG.info("DONE SUCCESSFUL in "
                     + sdf.format(new Date((endTime - startTime) - sdf.getTimeZone().getRawOffset())));

        return configId;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   con  DOCUMENT ME!
      *
      * @throws  SQLException  DOCUMENT ME!
      */
     private void prepare(final Connection con) throws SQLException {
         LOG.info("PREPARING");
 
         final String queryA = "ALTER TABLE geocpm_triangle ADD COLUMN tmp_point_a_id INTEGER;"; // NOI18N
         final String queryB = "ALTER TABLE geocpm_triangle ADD COLUMN tmp_point_b_id INTEGER;"; // NOI18N
         final String queryC = "ALTER TABLE geocpm_triangle ADD COLUMN tmp_point_c_id INTEGER;"; // NOI18N
 
         final Statement stmt = con.createStatement();
         try {
             stmt.execute("BEGIN;");
             stmt.executeUpdate(queryA);
             stmt.executeUpdate(queryB);
             stmt.executeUpdate(queryC);
             stmt.execute("COMMIT;");
         } catch (final SQLException e) {
             LOG.error("cannot cleanup db", e); // NOI18N
 
             throw e;
         } finally {
             stmt.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   configId  DOCUMENT ME!
      * @param   con       DOCUMENT ME!
      *
      * @throws  SQLException  DOCUMENT ME!
      */
     private void postProcessTriangles(final int configId, final Connection con) throws SQLException {
         LOG.info("postProcessing triangle data for new configuration: " + configId); // NOI18N
 
         final String triangleUpdate = "UPDATE geocpm_triangle gt "                                                       // NOI18N
                     + "SET geocpm_point_a_id = gpA.id, geocpm_point_b_id = gpB.id, geocpm_point_c_id = gpC.id, "         // NOI18N
                     + "geom = ST_MakePolygon(ST_MakeLine(array[gpA.geom, gpB.geom, gpC.geom, gpA.geom])) "               // NOI18N
                     + "FROM geocpm_point gpA, geocpm_point gpB, geocpm_point gpC "                                       // NOI18N
                     + "WHERE gt.geocpm_configuration_id = " + configId + " "                                             // NOI18N
                     + "AND gpA.index = gt.tmp_point_a_id AND gpA.geocpm_configuration_id = gt.geocpm_configuration_id "  // NOI18N
                     + "AND gpB.index = gt.tmp_point_b_id AND gpB.geocpm_configuration_id = gt.geocpm_configuration_id "  // NOI18N
                     + "AND gpC.index = gt.tmp_point_c_id AND gpC.geocpm_configuration_id = gt.geocpm_configuration_id;"; // NOI18N
 
         final Statement stmt = con.createStatement();
         try {
             stmt.executeUpdate(triangleUpdate);
         } catch (final SQLException e) {
             LOG.error("cannot post process data of config " + configId, e); // NOI18N
 
             throw e;
         } finally {
             stmt.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   configId  DOCUMENT ME!
      * @param   con       DOCUMENT ME!
      *
      * @throws  SQLException  DOCUMENT ME!
      */
     private void postProcess(final int configId, final Connection con) throws SQLException {
         LOG.info("postProcessing data for new configuration: " + configId); // NOI18N
 
         final PreparedStatement overviewGeomInsertStmt = con.prepareStatement(
                 "INSERT INTO geom (geo_field) VALUES ("                                                  // NOI18N
                         + "(SELECT ST_Union(geom) FROM geocpm_triangle WHERE geocpm_configuration_id = " // NOI18N
                         + configId
                         + "));",                                                                         // NOI18N
                 Statement.RETURN_GENERATED_KEYS);
         final Statement stmt = con.createStatement();
         try {
             overviewGeomInsertStmt.executeUpdate();
 
             final ResultSet s = overviewGeomInsertStmt.getGeneratedKeys();
             s.next();
 
             stmt.executeUpdate("UPDATE geocpm_configuration SET geom = " + s.getInt(1) + " WHERE id = " + configId); // NOI18N
         } catch (final SQLException e) {
             LOG.error("cannot post process data of config " + configId, e);                                          // NOI18N
 
             throw e;
         } finally {
             stmt.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   con  DOCUMENT ME!
      *
      * @throws  SQLException  DOCUMENT ME!
      */
     private void finish(final Connection con) throws SQLException {
         LOG.info("FINISHING");
 
         final String queryA = "ALTER TABLE geocpm_triangle DROP COLUMN tmp_point_a_id;"; // NOI18N
         final String queryB = "ALTER TABLE geocpm_triangle DROP COLUMN tmp_point_b_id;"; // NOI18N
         final String queryC = "ALTER TABLE geocpm_triangle DROP COLUMN tmp_point_c_id;"; // NOI18N
 
         final Statement stmt = con.createStatement();
         try {
             stmt.execute("BEGIN;");
             stmt.executeUpdate(queryA);
             stmt.executeUpdate(queryB);
             stmt.executeUpdate(queryC);
             stmt.execute("COMMIT;");
         } finally {
             stmt.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   struct  DOCUMENT ME!
      * @param   line    DOCUMENT ME!
      *
      * @throws  ParseException         DOCUMENT ME!
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     private void readConfig(final ConfigStruct struct, final String line) throws ParseException {
         final int index = line.indexOf(':');
         final String key = line.substring(0, index).trim();
         final String val = line.substring(index + 1).trim();
 
         if (CALC_BEGIN.equals(key)) {
             struct.calcBegin = new Timestamp(dateFormat.parse(val).getTime());
         } else if (CALC_END.equals(key)) {
             struct.calcEnd = new Timestamp(dateFormat.parse(val).getTime());
         } else if (WRITE_NODE.equals(key)) {
             struct.writeNode = ('y' == val.charAt(0));
         } else if (WRITE_EDGE.equals(key)) {
             struct.writeEdge = ('y' == val.charAt(0));
         } else if (LAST_VALUES.equals(key)) {
             struct.lastValues = ('y' == val.charAt(0));
         } else if (SAVE_MARKED.equals(key)) {
             struct.saveMarked = ('y' == val.charAt(0));
         } else if (MERGE_TRIANGLES.equals(key)) {
             struct.mergeTriangles = ('y' == val.charAt(0));
         } else if (MIN_CALC_TRIANGLE_SIZE.equals(key)) {
             struct.minCalcTriangleSize = new BigDecimal(val);
         } else if (TIME_STEP_RESTRICTION.equals(key)) {
             struct.timeStepRestriction = ('y' == val.charAt(0));
         } else if (SAVE_VELOCITY_CURVES.equals(key)) {
             struct.saveVelocityCurves = ('y' == val.charAt(0));
         } else if (SAVE_FLOW_CURVES.equals(key)) {
             struct.saveFlowCurves = ('y' == val.charAt(0));
         } else if (RESULT_SAVE_LIMIT.equals(key)) {
             struct.resultSaveLimit = new BigDecimal(val);
         } else if (NUMBER_OF_THREADS.equals(key)) {
             struct.numberOfThreads = Integer.valueOf(val);
         } else if (Q_IN.equals(key)) {
             struct.qIn = Integer.valueOf(val);
         } else if (Q_OUT.equals(key)) {
             struct.qOut = Integer.valueOf(val);
         } else {
             throw new IllegalStateException("unknown config key: " + key); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cs   DOCUMENT ME!
      * @param   con  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  SQLException  DOCUMENT ME!
      */
     private int insertConfig(final ConfigStruct cs, final Connection con) throws SQLException {
         final PreparedStatement p = con.prepareStatement(
                 "INSERT INTO geocpm_configuration (calc_begin, calc_end, write_node, "                         // NOI18N
                         + "write_edge, last_values, save_marked, merge_triangles, min_calc_triangle_size, "    // NOI18N
                         + "time_step_restriction, save_velocity_curves, save_flow_curves, result_save_limit, " // NOI18N
                         + "number_of_threads, q_in, q_out) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                 Statement.RETURN_GENERATED_KEYS);                                                              // NOI18N
         try {
             p.setTimestamp(1, cs.calcBegin);
             p.setTimestamp(2, cs.calcEnd);
             p.setBoolean(3, cs.writeNode);
             p.setBoolean(4, cs.writeEdge);
             p.setBoolean(5, cs.lastValues);
             p.setBoolean(6, cs.saveMarked);
             p.setBoolean(7, cs.mergeTriangles);
             p.setBigDecimal(8, cs.minCalcTriangleSize);
             p.setBoolean(9, cs.timeStepRestriction);
             p.setBoolean(10, cs.saveVelocityCurves);
             p.setBoolean(11, cs.saveFlowCurves);
             p.setBigDecimal(12, cs.resultSaveLimit);
             p.setInt(13, cs.numberOfThreads);
             p.setInt(14, cs.qIn);
             p.setInt(15, cs.qOut);
 
             p.executeUpdate();
 
             final ResultSet s = p.getGeneratedKeys();
             s.next();
 
             return s.getInt(1);
         } finally {
             p.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   batchSQL  DOCUMENT ME!
      * @param   con       DOCUMENT ME!
      *
      * @throws  SQLException  DOCUMENT ME!
      * @throws  IOException   DOCUMENT ME!
      */
     private void insertBatch(final StringBuilder batchSQL, final Connection con) throws SQLException, IOException {
         final Statement stmt = con.createStatement();
         try {
             stmt.execute(batchSQL.toString());
         } catch (final SQLException e) {
             LOG.warn("cannot insert data for query:\n" + batchSQL.toString(), e); // NOI18N
 
             final BufferedWriter bw = new BufferedWriter(new FileWriter("out.sql"));
             bw.write(batchSQL.toString(), 0, batchSQL.length());
             bw.close();
 
             throw e;
         } finally {
             stmt.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readPoint(final StringBuilder batchSQL, final int configId, final String line) {
         final String[] split = line.split(FIELD_SEP);
 
         //J-
         batchSQL.append("\nINSERT INTO geocpm_point (geocpm_configuration_id, index, geom) VALUES ("); // NOI18N
         batchSQL.append(configId).append(", ");                                                        // NOI18N
         batchSQL.append(split[0]).append(", ");                                                        // NOI18N
         batchSQL.append("ST_GeomFromEWKT('SRID=31466;POINT(")                                          // NOI18N
                 .append(split[1]).append(' ')
                 .append(split[2]).append(' ')
                 .append(split[3]).append(")'));");                                                     // NOI18N
         //J+
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readTriangle(final StringBuilder batchSQL, final int configId, final String line) {
         final String[] split = line.split(FIELD_SEP);
 
         //J-
         batchSQL.append("\nINSERT INTO geocpm_triangle (geocpm_configuration_id, index, tmp_point_a_id, ")        // NOI18N
                 .append("tmp_point_b_id, tmp_point_c_id, neighbour_a_id, neighbour_b_id, neighbour_c_id, ")       // NOI18N
                 .append("roughness, loss, be_height_a, be_height_b, be_height_c) VALUES (");                      // NOI18N
         batchSQL.append(configId).append(", ");                                                                   // NOI18N
         batchSQL.append(split[0]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[1]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[2]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[3]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[4]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[5]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[6]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[7]).append(", ");                                                                   // NOI18N
         batchSQL.append(split[8]).append(", ");                                                                   // NOI18N
         batchSQL.append(split.length > 9 ? split[9] : "null").append(", ");                                       // NOI18N
         batchSQL.append(split.length > 10 ? split[10] : "null").append(", ");                                     // NOI18N
         batchSQL.append(split.length > 11 ? split[11] : "null").append(");");                                     // NOI18N
         //J+
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readCurve(final StringBuilder batchSQL, final int configId, final String line) {
         final String[] split = line.split(FIELD_SEP);
 
         batchSQL.append("\nINSERT INTO geocpm_curve (geocpm_configuration_id, identifier) VALUES ("); // NOI18N
         batchSQL.append(configId).append(", ");                                                       // NOI18N
         batchSQL.append('\'').append(split[0]).append("');");                                         // NOI18N
 
         for (int i = 1; i < split.length; i += 2) {
             final String k = split[i];
             final String v = split[i + 1];
 
             //J-
             batchSQL.append("\nINSERT INTO geocpm_curve_value (geocpm_curve_id, t, value) VALUES ("); // NOI18N
             batchSQL.append("(SELECT id FROM geocpm_curve WHERE geocpm_configuration_id = ")          // NOI18N
                     .append(configId).append(" AND identifier = '").append(split[0]).append("'), ");  // NOI18N
             batchSQL.append(k).append(", ");                                                          // NOI18N
             batchSQL.append(v).append(");");                                                          // NOI18N
             //J+
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readSourceDrain(final StringBuilder batchSQL, final int configId, final String line) {
         final String[] split = line.split(FIELD_SEP);
 
         //J-
         batchSQL.append("\nINSERT INTO geocpm_source_drain (geocpm_configuration_id, identifier, geocpm_triangle_id, ") // NOI18N
                 .append("max_capacity, geocpm_curve_id) VALUES (");                                                     // NOI18N
         batchSQL.append(configId).append(", ");                                                                         // NOI18N
         batchSQL.append('\'').append(split[0]).append("', ");                                                           // NOI18N
         batchSQL.append("(SELECT id FROM geocpm_triangle WHERE index = ").append(split[1])                              // NOI18N
                 .append(" AND geocpm_configuration_id = ").append(configId).append("), ");                              // NOI18N
         batchSQL.append(split[2]).append(", ");                                                                         // NOI18N
         batchSQL.append("(SELECT id FROM geocpm_curve WHERE identifier = '").append(split[3]).append('\'')              // NOI18N
                 .append(" AND geocpm_configuration_id = ").append(configId).append("));");                              // NOI18N
         //J+
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readManhole(final StringBuilder batchSQL, final int configId, final String line) {
         final String[] split = line.split(FIELD_SEP);
 
         //J-
         batchSQL.append("\nINSERT INTO geocpm_manhole (geocpm_configuration_id, internal_id, cap_height, ")  // NOI18N
                 .append("entry_profile, loss_overfall, loss_emersion, length_emersion, name) VALUES (");     // NOI18N
         batchSQL.append(configId).append(", "); // NOI18N
         batchSQL.append(split[split.length - 7]).append(", ");                                               // NOI18N
         batchSQL.append(split[split.length - 6]).append(", ");                                               // NOI18N
         batchSQL.append(split[split.length - 5]).append(", ");                                               // NOI18N
         batchSQL.append(split[split.length - 4]).append(", ");                                               // NOI18N
         batchSQL.append(split[split.length - 3]).append(", ");                                               // NOI18N
         batchSQL.append(split[split.length - 2]).append(", ");                                               // NOI18N
         batchSQL.append('\'').append(split[split.length - 1]).append("');");                                 // NOI18N
 
         for(int i = 1; i < split.length - 7; ++i){
             batchSQL.append("\nINSERT INTO geocpm_jt_manhole_triangle (geocpm_manhole_id, geocpm_triangle_id")  // NOI18N
                     .append(") VALUES (");                                                                      // NOI18N
             batchSQL.append("(SELECT id FROM geocpm_manhole WHERE geocpm_configuration_id = ").append(configId) // NOI18N
                     .append(" AND name = '").append(split[split.length - 1]).append("'), ");                    // NOI18N
             batchSQL.append("(SELECT id FROM geocpm_triangle WHERE index = ").append(split[i])                  // NOI18N
                     .append(" AND geocpm_configuration_id = ").append(configId).append("));");                  // NOI18N
         }
         //J+
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readMarked(final StringBuilder batchSQL, final int configId, final String line) {
         //J-
         batchSQL.append("\nUPDATE geocpm_triangle SET marked = TRUE WHERE index = ").append(line) // NOI18N
                 .append(" AND geocpm_configuration_id = ").append(configId).append(';');          // NOI18N
         //J+
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  batchSQL  DOCUMENT ME!
      * @param  configId  DOCUMENT ME!
      * @param  line      DOCUMENT ME!
      */
     private void readBKConnect(final StringBuilder batchSQL, final int configId, final String line) {
         final String[] split = line.split(FIELD_SEP);
 
         //J-
         batchSQL.append("\nINSERT INTO geocpm_breaking_edge (geocpm_configuration_id, index, type, height, ") // NOI18N
                 .append("triangle_count_high, triangle_count_low) VALUES (");                                 // NOI18N
         batchSQL.append(configId).append(", ");                                                               // NOI18N
         batchSQL.append(split[0]).append(", ");                                                               // NOI18N
         batchSQL.append(split[1]).append(", ");                                                               // NOI18N
         batchSQL.append(split[2]).append(", ");                                                               // NOI18N
         batchSQL.append(split[3]).append(", ");                                                               // NOI18N
         batchSQL.append(split[4]).append(");");                                                               // NOI18N
 
         final StringBuilder sb = new StringBuilder("\nINSERT INTO geom (geo_field) VALUES (ST_MakeLine(array["); // NOI18N
 
         for(int i = 5; i < split.length; i += 2){
             final char orientation = split[i + 1].charAt(0);
             batchSQL.append("\nINSERT INTO geocpm_jt_breaking_edge_triangle (geocpm_breaking_edge_id, ") // NOI18N
                     .append("geocpm_triangle_id, orientation) VALUES (");                                // NOI18N
             batchSQL.append("(SELECT id FROM geocpm_breaking_edge WHERE index = ").append(split[0])      // NOI18N
                     .append(" AND geocpm_configuration_id = ").append(configId).append("), ");           // NOI18N
             batchSQL.append("(SELECT id FROM geocpm_triangle WHERE index = ").append(split[i])           // NOI18N
                     .append(" AND geocpm_configuration_id = ").append(configId).append("), ");           // NOI18N
             batchSQL.append('\'').append(orientation).append("');");                                     // NOI18N
 
             if(Character.isLowerCase(orientation)){
                 sb.append("(SELECT geom FROM geocpm_point WHERE id = (SELECT geocpm_point_")        // NOI18N
                         .append(orientation == 'a' ? 'b' : orientation == 'b' ? 'c' : 'a')          // NOI18N
                         .append("_id FROM geocpm_triangle WHERE index = ").append(split[i])         // NOI18N
                         .append(" AND geocpm_configuration_id = ").append(configId).append(")), "); // NOI18N
             }
         }
 
         sb.append("(SELECT geom FROM geocpm_point WHERE id = (SELECT geocpm_point_")               // NOI18N
                 .append(split[split.length - 1])
                 .append("_id FROM geocpm_triangle WHERE index = ").append(split[split.length - 2]) // NOI18N
                 .append(" AND geocpm_configuration_id = ").append(configId).append("))]));");      // NOI18N
         batchSQL.append(sb);
 
         batchSQL.append("UPDATE geocpm_breaking_edge SET geom = (SELECT max(id) FROM geom) WHERE index = ") // NOI18N
                 .append(split[3]).append(" AND geocpm_configuration_id = ").append(configId).append(";");   // NOI18N
         //J+
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class ConfigStruct {
 
         //~ Instance fields ----------------------------------------------------
 
         Timestamp calcBegin;
         Timestamp calcEnd;
         boolean writeNode;
         boolean writeEdge;
         boolean lastValues;
         boolean saveMarked;
         boolean mergeTriangles;
         BigDecimal minCalcTriangleSize;
         boolean timeStepRestriction;
         boolean saveVelocityCurves;
         boolean saveFlowCurves;
         BigDecimal resultSaveLimit;
         int numberOfThreads;
         int qIn;
         int qOut;
     }
 }
