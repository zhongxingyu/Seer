 package cz.cuni.mff.odcleanstore.webfrontend.dao;
 
 import java.sql.Blob;
 import java.sql.SQLException;
 
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 public abstract class CustomRowMapper<T> implements ParameterizedRowMapper<T>
 {
 	protected static String blobToString(Blob blob) throws SQLException
 	{
 		return new String(blob.getBytes(1, (int)blob.length()));
 	}
 }
