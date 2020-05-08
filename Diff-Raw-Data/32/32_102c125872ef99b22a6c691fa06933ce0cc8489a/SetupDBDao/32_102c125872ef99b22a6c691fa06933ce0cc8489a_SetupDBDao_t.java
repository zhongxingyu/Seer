 package net.todd.biblestudy.db;
 
 import java.io.File;
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
 
 		boolean isConnectionClosed = true;
 
 		try
 		{
 			Connection connection = sqlMapper.getDataSource().getConnection();
 			isConnectionClosed = connection.isClosed();
 		}
 		catch (SQLException e)
 		{
 			throw new BiblestudyException(
 					"Something bad happened while trying to connect to the database"
 							+ e.getMessage(), e);
 		}
 
 		if (isConnectionClosed)
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
 
		Integer version = 0;

		if (info != null)
		{
			version = info.getVersion();
		}

		return version;
 	}
 
 	public void processSqlFromFile(File sqlFile)
 	{
 		// TODO Auto-generated method stub
 	}
 }
