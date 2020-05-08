 package net.todd.biblestudy.db;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import net.todd.biblestudy.common.BiblestudyException;
 
 import com.ibatis.common.resources.Resources;
 import com.ibatis.sqlmap.client.SqlMapClient;
 import com.ibatis.sqlmap.client.SqlMapClientBuilder;
 
 public class SetupDBDao extends BaseDao implements ISetupDBDao
 {
 	public void connectWithCredentials(String user, String pass, String url)
 			throws BiblestudyException
 	{
 		Properties properties = new Properties();
 		properties.setProperty("user", user);
 		properties.setProperty("password", pass);
 		properties.setProperty("url", url);
 
 		SqlMapClient sqlMapper = null;
 
 		try
 		{
 			Reader reader = Resources
 					.getResourceAsReader("net/todd/biblestudy/db/SqlMapConfig.xml");
 			sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader, properties);
 			reader.close();
 		}
 		catch (IOException e)
 		{
 			throw new BiblestudyException(
 					"Something bad happened while building the SqlMapClient instance." + e, e);
 		}
 
		boolean isValidConnection = false;
 
 		try
 		{
 			Connection connection = sqlMapper.getDataSource().getConnection();
			isValidConnection = connection.isValid(0);
 		}
 		catch (SQLException e)
 		{
 			throw new BiblestudyException(
 					"Something bad happened while trying to connect to the database"
 							+ e.getMessage(), e);
 		}
 
		if (!isValidConnection)
 		{
 			throw new BiblestudyException("Bad database credentials");
 		}
 	}
 
 	public int getDatabaseVersion() throws BiblestudyException
 	{
 		DBInfo info = null;
 
 		try
 		{
 			info = (DBInfo) getSqlMapConfig().queryForObject("getDBInfo");
 		}
 		catch (SQLException e)
 		{
 			throw new BiblestudyException(e);
 		}
 
 		return info.getVersion();
 	}
 }
