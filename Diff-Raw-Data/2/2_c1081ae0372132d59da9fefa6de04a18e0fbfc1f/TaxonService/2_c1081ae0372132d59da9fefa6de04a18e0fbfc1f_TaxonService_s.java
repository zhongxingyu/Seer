 package com.sap.nic.fossil.web.service;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.sap.nic.fossil.db.DbConnectionPool;
 
 @Path("taxon")
 public class TaxonService
 {
 	private static final Logger _logger = Logger.getLogger(TaxonService.class);
 	
 	
 	@GET
	@Path("distribution")
 	public JSONArray getDistByClassYear(
 			@QueryParam("className") String p_className,
 			@QueryParam("yearSelected") Double p_year
 			) throws JSONException, SQLException
 	{
 		ResultSet resultSet = null;
 		if (p_year == null)
 		{
 			 //resultSet = executeSql("CALL FOSSIL195.FS_PROC_SQL_DIVERSITY_CURVE('')");						
 		}
 		else
 		{
 			resultSet = executeSql("CALL FS_PROC_SQL_TAXA_COUNT_MAIN(?, ?)", p_className, p_year);
 		}
 		
 		JSONArray result = new JSONArray();
 
 		while (resultSet.next())
 		{	
 			JSONObject cls = new JSONObject();
 			cls.put("sectionID", "s"+resultSet.getInt("SCTNUM"));
 			cls.put("taxonNumber", resultSet.getInt(2));
 
 			result.put(cls);
 		}
 		return result;
 	}
 
 	
 	
 	
 	@GET
 	@Path("diversity/curve")
 	public JSONArray getDiversityCurve(
 			@QueryParam("class") String p_className
 			) throws JSONException, SQLException
 	{
 		ResultSet resultSet = null;
 		if (p_className == null)
 		{
 			 //resultSet = executeSql("CALL FOSSIL195.FS_PROC_SQL_DIVERSITY_CURVE('')");						
 		}
 		else
 		{
 			if(p_className == "")
 			{				
 				resultSet = executeSql("CALL FOSSIL195.FS_PROC_SQL_DIVERSITY_CURVE(?, ?)", p_className, 0.0001);
 			}
 			else
 			{
 			 resultSet = executeSql("CALL FOSSIL195.FS_PROC_SQL_DIVERSITY_CURVE(?, ?)", p_className, 0.0001);
 			}
 			 
 		}
 		
 		JSONArray result = new JSONArray();
 
 		while (resultSet.next())
 		{	
 			JSONObject cls = new JSONObject();
 			cls.put("ma", resultSet.getDouble("MA"));
 			cls.put("count", resultSet.getInt(2));
 
 			result.put(cls);
 		}
 		return result;
 	}
 	
 	@GET
 	public JSONObject getTaxa() throws JSONException, SQLException
 	{
 		ResultSet resultSet = executeSql("call FOSSIL195.FS_PROC_SQL_TEST_MA()");
 		
 		JSONObject result = new JSONObject();
 		JSONArray taxa = new JSONArray();
 		result.put("taxa", taxa);
 		int i = 0;
 		while (resultSet.next())
 		{
 			double appear = resultSet.getDouble("APPEAR");
 			double disappear = resultSet.getDouble("DISAPPEAR");
 			
 			if (i++ == 0)
 			{
 				result.put("first", appear);
 			}
 			
 			JSONObject taxon = new JSONObject();
 			taxon.put("id", "t" + resultSet.getInt("TAXAID"));
 			taxon.put("author", resultSet.getString("AUTHOR"));
 			taxon.put("genus", resultSet.getString("GENUS"));
 			String name = resultSet.getString("SHORTNAME");
 			name = name.replace(".1111", "").replace(".1", "");
 			taxon.put("name", name);
 			taxa.put(taxon);
 			
 			String cls = resultSet.getString("CLASS");
 			if (cls.trim().equals("") || cls.equals("1") || cls.equals("1111"))
 			{
 				cls = null;
 			} 
 			taxon.put("cls", cls);
 			String genus = resultSet.getString("GENUS");
 			if (genus.trim().equals("") || genus.equals("1") || genus.equals("1111"))
 			{
 				genus = null;
 			}
 			taxon.put("genus", genus);
 			
 			String species = resultSet.getString("SPECIES");
 			if (species.trim().equals("") || species.equals("1") || species.equals("1111"))
 			{
 				species = null;
 			}
 			taxon.put("species", species);
 			
 			String fullName = "";
 			if (genus != null)
 			{
 				fullName = genus;
 			}
 			if (species != null)
 			{
 				if (fullName.equals(""))
 				{
 					fullName = species;
 				}
 				else
 				{
 					fullName += " " + species; 
 				}
 			}
 			taxon.put("fullName", fullName);
 			
 			int start = resultSet.getInt("FAD");
 			int end = resultSet.getInt("LAD");
 			taxon.put("start", start < end ? start : end);
 			taxon.put("end", start < end ? end : start);
 			
 			taxon.put("appear", appear);
 			taxon.put("disappear", disappear);
 			
 			if (!result.has("last") || disappear < result.getDouble("last"))
 			{
 				result.put("last", disappear);
 			}
 		}
 		return result;
 	}
 
 	//@GET
 	public JSONArray getTaxons() throws JSONException, SQLException
 	{
 		ResultSet resultSet = executeSql("call fossil01.FS_PROC_SQL_GET_SPECIES_LIVE_TIME()");
 		
 		JSONArray taxons = new JSONArray();
 		while (resultSet.next())
 		{
 			JSONObject taxon = new JSONObject();
 			taxon.put("id", "t" + resultSet.getInt("TAXAID"));
 			taxon.put("author", resultSet.getString("AUTHOR"));
 			
 			String name = resultSet.getString("SHORTNAME");
 			name = name.replace(".1111", "").replace(".1", "");
 			taxon.put("name", name);
 			
 			String cls = resultSet.getString("CLASS");
 			if (cls.trim().equals("") || cls.equals("1") || cls.equals("1111"))
 			{
 				cls = null;
 			} 
 			taxon.put("cls", cls);
 			
 			String genus = resultSet.getString("GENUS");
 			if (genus.trim().equals("") || genus.equals("1") || genus.equals("1111"))
 			{
 				genus = null;
 			}
 			taxon.put("genus", genus);
 			String species = resultSet.getString("SPECIES");
 			if (species.trim().equals("") || species.equals("1") || species.equals("1111"))
 			{
 				species = null;
 			}
 			taxon.put("species", species);
 			String fullName = "";
 			if (genus != null)
 			{
 				fullName = genus;
 			}
 			if (species != null)
 			{
 				if (fullName.equals(""))
 				{
 					fullName = species;
 				}
 				else
 				{
 					fullName += " " + species; 
 				}
 			}
 			taxon.put("fullName", fullName);
 			
 			
 			int start = resultSet.getInt("START_IDX");
 			int end = resultSet.getInt("END_IDX");
 			taxon.put("start", start < end ? start : end);
 			taxon.put("end", start < end ? end : start);			
 			
 			if (!resultSet.getString("YEAR").equals("*") && !resultSet.getString("YEAR").equals("Nich"))
 			{
 				try
 				{
 					taxon.put("year", resultSet.getInt("YEAR"));
 				}
 				catch (Exception e)
 				{
 					taxon.put("year", 0);
 				}
 			}
 			else
 			{
 				taxon.put("year", 0);
 			}
 			
 			taxons.put(taxon);
 		}
 		return taxons;
 	}
 
 	@GET
 	@Path("{id}/section")
 	public JSONArray getTaxonSectionList(@PathParam("id") String p_id) throws JSONException, SQLException
 	{
 		JSONArray result = new JSONArray();
 		return result;
 	}
 	
 	@GET
 	@Path("diversity")
 	public JSONArray getTaxonDiversity(
 			@QueryParam("from") int p_from,
 			@QueryParam("to") int p_to) throws JSONException, SQLException
 	{
 		ResultSet resultSet = executeSql("CALL FOSSIL01.FS_PROC_SQL_GET_SECTION_COUNT(?, ?)", p_from, p_to);
 		
 		JSONArray result = new JSONArray();
 		while (resultSet.next())
 		{
 			JSONObject time = new JSONObject();
 			time.put("index", resultSet.getInt("RANKID"));
 			time.put("sectionId", resultSet.getInt("SECTIONID"));
 			time.put("taxonCount", resultSet.getInt("TAXACOUNT"));
 			result.put(time);
 		}
 		return result;
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public ResultSet executeSql(String p_sql, Object... p_parameters) throws SQLException
 	{
 		String convertedSQL = p_sql;
 		for (int i = 0; i < p_parameters.length; i++)
 		{
 			if (p_parameters[i] != null)
 			{
 				convertedSQL = convertedSQL.replaceFirst("\\?", p_parameters[i].toString());
 			}
 			else
 			{
 				convertedSQL = convertedSQL.replaceFirst("\\?", "null");
 			}
 		}
 		_logger.info("Executing SQL: " + convertedSQL);
 
 		Connection connection = null;
 		while (connection == null)
 		{
 			connection = DbConnectionPool.getDefaultConnectionPool().getConnection();
 		}
 
 		try
 		{
 			PreparedStatement statement = connection.prepareStatement(p_sql);
 			for (int i = 0; i < p_parameters.length; i++)
 			{
 				Object param = p_parameters[i];
 				statement.setObject(i + 1, param);
 			}
 
 			statement.execute();
 
 			ResultSet rs = statement.getResultSet();
 
 			_logger.info("Finish running SQL: " + convertedSQL);
 			return rs;
 		}
 		finally
 		{
 			DbConnectionPool.getDefaultConnectionPool().returnConnection(connection);
 		}
 	}
 
 	public static void main(String[] args) throws JSONException, SQLException
 	{
 		//System.out.println(new TaxonService().getTaxons().toString());
 //		System.out.println(new TaxonService().getDiversityCurve("").toString());
 		System.out.println(new TaxonService().getDistByClassYear("", 280.09).toString());
 	}
 }
