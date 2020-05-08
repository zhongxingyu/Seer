 package gov.cida.sedmap.data;
 
 import gov.cida.sedmap.io.FileInputStreamWithFile;
 import gov.cida.sedmap.io.IoUtils;
 import gov.cida.sedmap.io.util.StrUtils;
 import gov.cida.sedmap.ogc.FilterLiteralIterator;
 import gov.cida.sedmap.ogc.FilterWithViewParams;
 import gov.cida.sedmap.ogc.OgcUtils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.geotools.data.jdbc.FilterToSQLException;
 
 public class JdbcFetcher extends Fetcher {
 
 
 	private static final Logger logger = Logger.getLogger(JdbcFetcher.class);
 
 	private static final Map<String,String> dataQueries  = new HashMap<String, String>();
 
 	public static final String DEFAULT_DAILY_SITE_SQL = "" // this is for consistent formatting
 			+ " select s.*, HUC_12 as HUC_8, "
 			+ "   NVL(y.sample_years,0) as sample_years "
 			+ " from SM_DAILY_STATIONS s "
 			+ " left join ( "
 			+ "    select usgs_station_id, count(*) sample_years "
 			+ "      from sm_daily_year y "
 			+ "     where y.SAMPLE_YEAR>=? " //yr1
 			+ "       and y.SAMPLE_YEAR<=? " //yr2
 			+ "     group by usgs_station_id) y "
 			+ "   on (y.usgs_station_id = s.usgs_station_id) "
 			+ " where sample_years > 0 and ";
 
 	static final String DEFAULT_DISCRETE_SITE_SQL = ""
 			+ " select s.*, s.HUC_12 as HUC_8, "
 			+ "   NVL(y.sample_count,0) as sample_count "
 			+ " from SM_INST_STATIONS s "
 			+ " left join ( "
 			+ "    select usgs_station_id, count(*) sample_count "
 			+ "      from sm_inst_sample_fact y "
 			+ "     where EXTRACT(year FROM y.datetime)>=? " // yr1
 			+ "       and EXTRACT(year FROM y.datetime)<=? " // yr2
 			+ "     group by usgs_station_id) y "
 			+ "   on (y.usgs_station_id = s.usgs_station_id) "
 			+ " where sample_count > 0 and ";
 
 	static final String DEFAULT_DISCRETE_DATA_SQL = ""
 			+ " select * "
 			+ "   from sm_inst_sample_fact "
 			+ "  where EXTRACT(year FROM datetime)>=? " // yr1
 			+ "    and EXTRACT(year FROM datetime)<=? " // yr2
 			+ "    and usgs_station_id in (_siteList_) "
 			+ "  order by usgs_station_id, datetime";
 
 
 	protected String jndiDS;
 
 	static {
 		// default queries that could be changed other need like testing
 		putQuery("daily_sites",           DEFAULT_DAILY_SITE_SQL);
 		putQuery("discrete_sites",        DEFAULT_DISCRETE_SITE_SQL);
 		putQuery("discrete_data",         DEFAULT_DISCRETE_DATA_SQL);
 	}
 	public static void putQuery(String descriptor, String sql) {
 		dataQueries.put(descriptor, sql);
 	}
 	protected static String getQuery(String descriptor) {
 		return dataQueries.get(descriptor);
 	}
 
 
 	// nice little bundle for the JDBC results set needs
 	protected static class Results {
 		Connection cn;
 		PreparedStatement ps;
 		ResultSet  rs;
 	}
 
 
 
 	public JdbcFetcher() {
 	}
 	public JdbcFetcher(String jndiJdbc) {
 		jndiDS = jndiJdbc;
 	}
 
 	@Override
 	public Fetcher initJndiJdbcStore(String jndiJdbc) throws IOException {
 		jndiDS = jndiJdbc;
 		return this;
 	}
 
 
 
 	@Override
 	protected InputStream handleSiteData(String descriptor, FilterWithViewParams filter, Formatter formatter)
 			throws IOException, SQLException, NamingException {
 
 		FileInputStreamWithFile fileData = null;
 		Results rs = new Results();
 
 		try {
 			String     tableName = getDataTable(descriptor);
 			List<Column> columns = getTableMetadata(tableName);
 			String header = formatter.fileHeader(columns);
 
 			String sql = buildQuery(descriptor, filter);
 			logger.debug(sql);
 			rs = initData(sql);
 			getData(rs, filter, true);
 
 			File   tmp = File.createTempFile(descriptor + StrUtils.uniqueName(12), formatter.getFileType());
 			FileWriter tmpw = new FileWriter(tmp);
 
 			//logger.debug(header);
 			tmpw.write(header);
 			while (rs.rs.next()) {
 				String row = formatter.fileRow(new ResultSetColumnIterator(rs.rs));
 				//logger.debug(row);
 				tmpw.write(row);
 			}
 			IoUtils.quiteClose(tmpw);
 
 			fileData = new FileInputStreamWithFile(tmp);
 			tmp.delete(); // TODO not for delayed download
 
 		} catch (FilterToSQLException e) {
 			throw new SQLException("Failed to convert filter to sql where clause.",e);
 		} finally {
 			IoUtils.quiteClose(rs.rs, rs.ps, rs.cn);
 		}
 
 		return fileData;
 	}
 	@Override
 	protected InputStream handleDiscreteData(Iterator<String> sites, FilterWithViewParams filter, Formatter formatter)
 			throws IOException, SQLException, NamingException {
 
 		FileInputStreamWithFile fileData = null;
 		Results rs = new Results();
 
 		try {
 			String    descriptor = "discrete_data";
 			String     tableName = getDataTable(descriptor);
 			List<Column> columns = getTableMetadata(tableName);
 			String header = formatter.fileHeader(columns);
 
 			StringBuilder sitesClause = new StringBuilder();
 			String join="";
 			while ( sites.hasNext() ) {
 				sitesClause.append(join).append("'").append(sites.next()).append("'");
 				join=",";
 			}
 			logger.debug(sitesClause.toString());
 
 			String sql = getQuery(descriptor);
 			sql=sql.replace("_siteList_", sitesClause.toString() );
 			logger.debug(sql);
 			rs = initData(sql);
 			getData(rs, filter, false);
 
 			File   tmp = File.createTempFile(descriptor + StrUtils.uniqueName(12), formatter.getFileType());
 			FileWriter tmpw = new FileWriter(tmp);
 
 			//logger.debug(header);
 			tmpw.write(header);
 			while (rs.rs.next()) {
 				String row = formatter.fileRow(new ResultSetColumnIterator(rs.rs));
 				//logger.debug(row);
 				tmpw.write(row);
 			}
 			IoUtils.quiteClose(tmpw);
 
 			fileData = new FileInputStreamWithFile(tmp);
 			tmp.delete(); // TODO not for delayed download
 
 		} finally {
 			IoUtils.quiteClose(rs.rs, rs.ps, rs.cn);
 		}
 
 		return fileData;
 	}
 
 
 	protected String buildQuery(String descriptor, FilterWithViewParams filter) throws FilterToSQLException {
 		//		FilterToSQL trans = new FilterToSQL();
 		//		trans.setInline(true);
 
 		String where = OgcUtils.ogcXmlToParameterQueryWherClause(filter.getFilter());
 
 		//		trans.encodeToString(filter);
 		String sql = getQuery(descriptor) + where; // + getQuery(descriptor+"_amount");
 		return sql;
 	}
 
 
 
 	protected String ogc2sql(String ogcXml) {
 		String where = "";
 
 		if ("".equals(ogcXml)) return where;
 		try {
 			where = OgcUtils.ogcXmlToParameterQueryWherClause(ogcXml);
 			if (where != null && where.trim().length()>0) {
 				where = OgcUtils.sqlTranslation(where, conf.FIELD_TRANSLATIONS);
 			} else {
 				where = "";
 			}
 		} catch (Exception e) {
 			// TODO empty results and err msg
 			throw new RuntimeException("Failed to parse OGC", e);
 		}
 
 		return where;
 	}
 
 
 
 	protected Results initData(String sql) throws NamingException, SQLException {
 		Results r = new Results();
 
 		try {
 			Context ctx = getContext();
 			DataSource ds = (DataSource) ctx.lookup(jndiDS);
 			r.cn = ds.getConnection();
 
 			r.ps = r.cn.prepareStatement(sql);
 		} catch (SQLException e) {
 			logger.error(e);
 			throw e;
 		} catch (Exception e) {
 			logger.error(e);
 			e.printStackTrace();
 		}
 
 		return r;
 	}
 	protected Results getData(Results r, FilterWithViewParams filter, boolean doFilterValues) throws NamingException, SQLException {
 
 		try {
 			int index = 1;
 			for (String value : filter) {
 				logger.debug("setting value " + value);
 				r.ps.setString(index++, value);
 			}
 
 			// this is because only sites uses the filter
 			if (doFilterValues) {
 				FilterLiteralIterator values = new FilterLiteralIterator(filter.getFilter());
 				for (String value : values) {
 					logger.debug("setting value " + value);
 					r.ps.setString(index++, value);
 				}
 			}
 			r.rs = r.ps.executeQuery();
 		} catch (SQLException e) {
 			logger.error(e);
 			throw e;
 		} catch (Exception e) {
 			logger.error(e);
 			e.printStackTrace();
 		}
 
 		return r;
 	}
 }
