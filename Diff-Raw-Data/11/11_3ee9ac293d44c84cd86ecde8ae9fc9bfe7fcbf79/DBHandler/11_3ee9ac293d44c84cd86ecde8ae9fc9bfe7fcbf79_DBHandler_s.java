 package eu.celarcloud.celar_ms.ServerPack.Database.Cassandra;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.datastax.driver.core.BoundStatement;
 import com.datastax.driver.core.Cluster;
 import com.datastax.driver.core.KeyspaceMetadata;
 import com.datastax.driver.core.PreparedStatement;
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.Row;
 import com.datastax.driver.core.Session;
 import com.datastax.driver.core.policies.Policies;
 
 import eu.celarcloud.celar_ms.ServerPack.IJCatascopiaServer;
 import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
 import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
 import eu.celarcloud.celar_ms.ServerPack.Database.IDBHandler;
 
 public class DBHandler implements IDBHandler{
 	private Cluster cluster;
 	private Session session;
 	private List<String> endpoints;
 	private String keyspace;
 	private IJCatascopiaServer server;
 	
 	private static final String CREATE_AGENT = "INSERT INTO agent_table (agentID, agentIP, status) VALUES (?,?,?)";
 	private static final String UPDATE_AGENT = "UPDATE agent_table SET status=? WHERE agentID=?";
 	private static final String DELETE_AGENT = "DELETE FROM agent_Table WHERE agentID=?";
 	private static final String CREATE_METRIC = "INSERT INTO metric_table (agentID, metricID, name, mgroup, type, units) VALUES (?,?,?,?,?,?)";
 	private static final String DELETE_METRIC = "DELETE FROM metric_table WHERE agentID=? AND metricID =?";
 	private static final String INSERT_METRIC_VALUE = "INSERT INTO metric_value_table " +
 														 "(metricID, event_date, event_timestamp, value, name, mgroup, type, units) VALUES (?,?,?,?,?,?,?,?) USING TTL 2592000";
 
 	private PreparedStatement insertAgentStmt;
 	private PreparedStatement updateAgentStmt;
 	private PreparedStatement deleteAgentStmt;
 	private PreparedStatement insertMetricStmt;
 	private PreparedStatement deleteMetricStmt;
 	private PreparedStatement insertMetricValueStmt;
 
 	public DBHandler(List<String> endpoints, String keyspace, IJCatascopiaServer server){
 		this.endpoints = endpoints;
 		this.keyspace = keyspace.toLowerCase();
 		this.server = server;
 	}
 	
 	public DBHandler(List<String> endpoints, String user, String pass, String keyspace, Integer cnum, IJCatascopiaServer server){
 		this(endpoints, keyspace, server);
 	}
 
 	public void dbConnect(){
 		this.cluster = Cluster.builder().addContactPoints(endpoints.toArray(new String[endpoints.size()]))
 				 						.withRetryPolicy(Policies.defaultRetryPolicy())
 				 						.build();
 		System.out.printf("Successfully connected to cluster: %s\n", cluster.getMetadata().getClusterName());
 		
 		boolean found = false;
 		for(KeyspaceMetadata k : cluster.getMetadata().getKeyspaces())
 			if (keyspace.equals(k.getName()))
 				found = true;
 		
 		if (!found){
 			session = cluster.connect(); //connect to cluster
 			String cql = "CREATE KEYSPACE "+ keyspace +" WITH " + 
 										  "replication = {'class':'SimpleStrategy','replication_factor':1}";
 			session.execute(cql); //create keyspace
 			System.out.println("Successfully created new keyspace: "+ keyspace);
 		}
 		session = cluster.connect(keyspace);
 		System.out.println("Successfully connected and created a new session");
 	}
 
 	public void dbClose(){
 		this.cluster.shutdown();	
 	}
 	
 	public void dbInit(boolean drop_tables){
 	    try {
 			if(drop_tables){ 
 				String[] tables = {"agent_table","metric_table","metric_value_table"};
 				String cql;
 				for(String t : tables){
 					cql = "DROP TABLE IF EXISTS " + t;
 					session.execute(cql);
 				}
 				
 				String table = "agent_table";
 				cql = "CREATE TABLE "+table+" (" + 
 		                " agentID varchar," + 
 		                " agentIP varchar," +
 		                " status varchar," +
 		                " PRIMARY KEY (agentID)" +
 		                ");";
 				session.execute(cql);
 				System.out.println("Successfully dropped and created table: " + table);
 				
 				table = "metric_table";
 				cql = "CREATE TABLE "+table+" (" + 
 		                " agentID varchar," + 
 		                " metricID varchar," +
 		                " name varchar," +
 		                " mgroup varchar," +
 		                " type varchar," +
 		                " units varchar," +
 		                " PRIMARY KEY (agentID,metricID)" +
 		                ");";
 				session.execute(cql);
 				System.out.println("Successfully dropped and created table: " + table);
 				
 				table = "metric_value_table";
 				cql = "CREATE TABLE "+table+" (" + 
 		                " metricID varchar," + 
 		                " event_date text," +
		                " event_timestamp timestamp," +
 		                " value varchar," +
 		                " name varchar," +
 		                " mgroup varchar," +
 		                " type varchar," +
 		                " units varchar," +
 		                " PRIMARY KEY ((metricID,event_date),event_timestamp)" +
 		                ") WITH CLUSTERING ORDER BY (event_timestamp DESC);";
 				session.execute(cql);
 				System.out.println("Successfully dropped and created table: " + table);
 			}
 			
 			this.insertAgentStmt = session.prepare(CREATE_AGENT);
 			this.updateAgentStmt = session.prepare(UPDATE_AGENT);
 			this.deleteAgentStmt = session.prepare(DELETE_AGENT);
 			this.insertMetricStmt = session.prepare(CREATE_METRIC);
 			this.deleteMetricStmt = session.prepare(DELETE_METRIC);
 			this.insertMetricValueStmt = session.prepare(INSERT_METRIC_VALUE);
 	    }
 	    catch(Exception e){
 	    	this.server.writeToLog(Level.SEVERE, "Cassandra DB Handler>> "+e);
 	    }
 	}
 
 	public void createAgent(AgentObj agent){
 		try{
 			BoundStatement bs = this.insertAgentStmt.bind();
 			bs.setString("agentID", agent.getAgentID());
 			bs.setString("agentIP", agent.getAgentIP());
 			bs.setString("status", agent.getStatus().name());
 			session.execute(bs);
 		}
 		catch(Exception e){
 		    this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler createAgent>> "+e);
 		}
 	}
 
 	public void updateAgent(String agentID, String status){
 		try{
 			BoundStatement bs = this.updateAgentStmt.bind();
 			bs.setString("agentID", agentID);
 			bs.setString("status", status);
 			session.execute(bs);		
 		}
 		catch(Exception e){
 		    this.server.writeToLog(Level.SEVERE, "Cassandra DB Handler updateAgent>> "+e);
 		}
 	}
 
 	public void deleteAgent(String agentID){
 		try{
 			BoundStatement bs = this.deleteAgentStmt.bind();
 			bs.setString("agentID", agentID);
 			session.execute(bs);
 		}
 		catch(Exception e){
 			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler deleteAgent>> "+e);
 		}
 	}
 	
 	public void createMetric(MetricObj metric){
 		try{
 			BoundStatement bs = this.insertMetricStmt.bind();
 			bs.setString("agentID", metric.getAgentID());
 			bs.setString("metricID", metric.getMetricID());
 			bs.setString("name", metric.getName());
 			bs.setString("mgroup",metric.getGroup());
 			bs.setString("type", metric.getType());
 			bs.setString("units", metric.getUnits());
 			session.execute(bs);
 		}
 		catch(Exception e){
 			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler createMetric>> "+e);
 		}
 	}
 	
 	public void deleteMetric(String agentID, String metricID){
 		try{
 			BoundStatement bs = this.deleteMetricStmt.bind();
 			bs.setString("agentID", agentID);
 			bs.setString("metricID", metricID);
 			session.execute(bs);
 		}
 		catch(Exception e){
 			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler deleteMetric>> "+e);
 		}
 	}
 	
 	public void insertMetricValue(MetricObj metric){
 		try{
 			BoundStatement bs = this.insertMetricValueStmt.bind();
 			bs.setString("metricID", metric.getMetricID());
			Date d = new Date(metric.getTimestamp());
			String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
			bs.setString("event_date", date);
			bs.setDate("event_timestamp", d);
 			bs.setString("value", metric.getValue());
 			bs.setString("name", metric.getName());
 			bs.setString("mgroup",metric.getGroup());
 			bs.setString("type", metric.getType());
 			bs.setString("units", metric.getUnits());
 			session.execute(bs);
 		}
 		catch(Exception e){
 			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler insertMetricValue>> "+e);
 		}
 	}
 	
 	public void doQuery(String cql, boolean print){
 		try{
 			ResultSet rs = session.execute(cql);
 			if (print){
 				for (Row row : rs) {
 					System.out.println(row.toString());
 				}
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace(); 
 		}
 	}
 }
