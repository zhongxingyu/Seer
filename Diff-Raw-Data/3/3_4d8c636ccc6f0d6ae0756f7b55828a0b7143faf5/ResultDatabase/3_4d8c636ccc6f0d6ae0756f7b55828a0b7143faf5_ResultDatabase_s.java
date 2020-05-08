 package edu.kit.aifb.exrunner;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import edu.kit.aifb.exrunner.model.attribute.Attribute;
 
 public class ResultDatabase {
 
 	private Connection m_conn;
 	private String m_tableName;
 	private List<Attribute> m_attributes;
 	private List<Attribute> m_keyAttributes;
 	private List<String> m_attributeNames;
 	
 	private PreparedStatement m_resultPS;
 	
 	private static final Logger log = LoggerFactory.getLogger(ResultDatabase.class);
 
 	public ResultDatabase(File databaseFile, String systemName, List<Attribute> attributes, List<Attribute> keyAttributes) throws ClassNotFoundException, SQLException {
 		Class.forName("org.sqlite.JDBC");
 		m_conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
 		m_attributes = attributes;
 		m_keyAttributes = keyAttributes;
 		m_tableName = systemName;
 		
 		m_attributeNames = Lists.transform(m_attributes, new Function<Attribute,String>() {
 			@Override
 			public String apply(Attribute input) {
 				return input.getName();
 			}
 		});
 		
 		if (!getTables().contains(m_tableName)) {
 			StringBuilder ct = new StringBuilder();
 			
 			ct.append("CREATE TABLE " + m_tableName + " (system varchar(32), ");
 			
 			ct.append(Joiner.on(",").join(Lists.transform(m_attributes, new Function<Attribute,String>() {
 				@Override
 				public String apply(Attribute input) {
 					return input.toSQLDecl();
 				}
 				
 			})));
 			
 			ct.append(", primary key(");
 			ct.append(Joiner.on(",").join(Lists.transform(m_keyAttributes, new Function<Attribute,String>() {
 				@Override
 				public String apply(Attribute input) {
 					return input.getName();
 				}
 				
 			})));
 			ct.append("))");
 			
 			log.debug(ct.toString());
 			
 			execute(ct.toString());
 		}
 	
 		StringBuilder sb = new StringBuilder();
 		sb.append("INSERT INTO " + m_tableName + " (");
 		sb.append(Joiner.on(",").join(m_attributeNames));
 		sb.append(") VALUES (");
 		sb.append(Joiner.on(",").join(Lists.transform(m_attributes, new Function<Attribute,String>() {
 			@Override
 			public String apply(Attribute input) {
 				return "?";
 			}
 		})));
 		sb.append(")");
 		
 		m_resultPS = m_conn.prepareStatement(sb.toString());
 	}
 	
 	public void close() throws SQLException {
 		m_resultPS.close();
 		m_conn.close();
 	}
 	
 	public void clear() throws SQLException {
 	    execute("DELETE FROM " + m_tableName);
 	}
 	
 	private void execute(String query) throws SQLException {
 		Statement st = m_conn.createStatement();
 		st.execute(query);
 		st.close();
 	}
 
 	private Set<String> getTables() throws SQLException {
 	    Statement st = m_conn.createStatement();
 	    ResultSet resultSet = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
 	    Set<String> tables = new HashSet<String>();
 	    while (resultSet.next())
 	    	tables.add(resultSet.getString(1));
 	    st.close();
 	    return tables;
 	}
 	
 	public void recordResult(Map<Attribute,Object>... maps) throws SQLException {
 		Map<Attribute,Object> values = Maps.newHashMap();
 		for (Map<Attribute,Object> map : maps)
			values.putAll(map);
 		recordResult(values);
 	}
 	
 	public void recordResult(final Map<Attribute,Object> values) throws SQLException {
 		for (int i = 0; i < m_attributes.size(); i++) {
 			m_attributes.get(i).setSQLValue(m_resultPS, i + 1, values.get(m_attributes.get(i)));
 		}
 		m_resultPS.execute();
 	}
 }
