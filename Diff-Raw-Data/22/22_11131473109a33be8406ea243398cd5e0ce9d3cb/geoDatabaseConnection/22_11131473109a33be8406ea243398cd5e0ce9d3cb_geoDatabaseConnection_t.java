 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.io.ParseException;
 import gao.tools.SQL;
 import ohm.roth.lbs.NavGraph;
 import ohm.roth.lbs.Node;
 import ohm.roth.lbs.Position;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.*;
 
 public class geoDatabaseConnection {
 	private String dbhost = "geo.informatik.fh-nuernberg.de";
 	private int dbport = 5432;
 	private String dbuser = "dbuser";
 	private String dbpasswd = "dbuser";
 	private String dbname = "deproDBMittelfranken";
 
 	private Connection connection = null;
 
 	public String getDbhost() {
 		return dbhost;
 	}
 
 	public void setDbhost(String dbhost) {
 		this.dbhost = dbhost;
 	}
 
 	public int getDbport() {
 		return dbport;
 	}
 
 	public void setDbport(int dbport) {
 		this.dbport = dbport;
 	}
 
 	public String getDbuser() {
 		return dbuser;
 	}
 
 	public void setDbuser(String dbuser) {
 		this.dbuser = dbuser;
 	}
 
 	public String getDbpasswd() {
 		return dbpasswd;
 	}
 
 	public void setDbpasswd(String dbpasswd) {
 		this.dbpasswd = dbpasswd;
 	}
 
 	public String getDbname() {
 		return dbname;
 	}
 
 	public void setDbname(String dbname) {
 		this.dbname = dbname;
 	}
 
 	public geoDatabaseConnection(String dbhost, int dbport, String dbuser,
 			String dbpasswd, String dbname) {
 		super();
 		this.dbhost = dbhost;
 		this.dbport = dbport;
 		this.dbuser = dbuser;
 		this.dbpasswd = dbpasswd;
 		this.dbname = dbname;
 	}
 
 	public void connect() {
 		try {
 			if (this.connection == null || this.connection.isClosed()) {
 				String connectionString = "jdbc:postgresql://" + dbhost + ":"
 						+ dbport + "/" + dbname;
 				connection = DriverManager.getConnection(connectionString,
 						dbuser, dbpasswd);
 				connection.setAutoCommit(false);
 			}
 		} catch (SQLException e) {
 			System.out.println("Error while connecting to DB");
 		}
 	}
 
 	public ResultSet getGraphData(Position p1, Position p2) {
 		try {
 			if (this.connection == null || this.connection.isClosed())
 				this.connect();
 
             GeometryFactory geomfact=new GeometryFactory();
 			Statement statement = null;
 			statement = this.connection.createStatement();
 			statement.setFetchSize(1000000);
 			String Query = "SELECT "
 					+ "link.id, link.crossing_id_from, link.crossing_id_to, link.meters, link.lsiclass, dom.realname, dom.geodata_line, dom.geometry, "
 					+ "from_cross.id, from_cross.long, from_cross.lat, from_cross.gao_id, from_cross.posnr, "
 					+ "to_cross.id, to_cross.long, to_cross.lat, to_cross.gao_id, to_cross.posnr "
 					+ "FROM "
 					+ "link ,crossing AS from_cross, crossing AS to_cross, domain AS dom "
 					+ "WHERE "
 					+ "long_from>="
 					+ p1.getLon()
 					+ " AND long_from<="
 					+ p2.getLon()
 					+ " AND lat_from>="
 					+ p2.getLat()
 					+ " AND lat_from<="
 					+ p1.getLat()
 					+ " AND "
 					+ "from_cross.id = link.crossing_id_from AND "
 					+ "to_cross.id = link.crossing_id_to AND "
 					+ "dom.gao_id = link.gao_id AND "
 					+ "from_cross.gao_id = dom.gao_id AND to_cross.gao_id = dom.gao_id "
 					+ "ORDER BY link.id";
 			return statement.executeQuery(Query);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         return null;
 
 	}
 
 	public NavGraph getGraph(Position p1, Position p2, String n) throws SQLException, IOException {
 		ResultSet resultSet = this.getGraphData(p1, p2);
 		NavGraph graph = new NavGraph(n);
 		FileWriter fstream = new FileWriter("GRAPH.TXT");
 		BufferedWriter out = new BufferedWriter(fstream);
 		while (resultSet.next()) {
 			int link_id = (int) resultSet.getLong(1);
 			String name = (String) resultSet.getString(6);
 			String gao_id = (String) resultSet.getString(12);
 			int from_node = resultSet.getInt(13);
 			int to_node = resultSet.getInt(18);
             byte[] geodata_line=resultSet.getBytes(7);
             LineString geom;
             try {
                 geom= (LineString) SQL.wkb2Geometry(geodata_line);
             } catch (ParseException e) {
                 //TODO: Fehler behandlung wenn Linien Objekt nicht erstellt werden kann
                 geom=null;
             }
             double meter = (double) resultSet.getDouble(4);
 			Position fromPoint = new Position((double) resultSet.getDouble(10),
 					(double) resultSet.getDouble(11));
 			Position toPoint = new Position((double) resultSet.getDouble(15),
 					(double) resultSet.getDouble(16));
 			String fromId = (String) resultSet.getString(9);
 			String toId = (String) resultSet.getString(14);
 			out.write("LINE mode=1 col=255,0,0,150\n");
             for (int i = from_node; i<= to_node; i++) {
                 out.write(geom.getPointN(i).getX() + "," + geom.getPointN(i).getY() + "\n");
             }
 
             graph.addGeo(gao_id, geom);
 			graph.addNode(new Node(fromId, new Position(fromPoint.getLon(),
 					fromPoint.getLat())));
 			graph.addNode(new Node(toId, new Position(toPoint.getLon(), toPoint
 					.getLat())));
 			graph.addConnection(fromId, toId, meter, gao_id, from_node, to_node);
 			graph.addConnection(toId, fromId, meter, gao_id, to_node, from_node);
 			
 		}
 		resultSet.close();
 		out.close();
 		return graph;
 	}
 }
