 package com.jwhois.util;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.jwhois.core.Utility;
 import com.jwhois.core.WhoisMap;
 
 public class DBHelper {
 
 	protected Object	outconn;
 	protected String	pre;
 
 	public DBHelper() {
 		this( null, "" );
 	}
 
 	public DBHelper(Connection conn) {
 		this( conn, "" );
 	}
 
 	public DBHelper(Connection conn, String pre) {
 		this.outconn = conn;
 		if (pre != null)
 			this.pre = pre;
 	}
 
 	public String getDBPrefix() {
 		return pre;
 	}
 
 	protected String getPre(String tablename) {
 		return pre + tablename;
 	}
 
 	public void initDB() {
 		updateDB( "DROP TABLE IF EXISTS " + getPre( "domainname" ) );
 		StringBuilder sql = new StringBuilder();
 		sql.append( "CREATE TABLE " + getPre( "domainname" ) + "(" );
 		sql.append( "`id`               int unsigned NOT NULL primary key auto_increment," );
 		sql.append( "`domain`           varchar(100) NOT NULL," );
 		sql.append( "`rawdata`          text NOT NULL," );
 		sql.append( "`querydate`        datetime NOT NULL," );
 		sql.append( "INDEX ( `domain` )" );
 		sql.append( ") ENGINE=MyISAM DEFAULT CHARSET=utf8" );
 		updateDB( sql.toString() );
 
 		updateDB( "DROP TABLE IF EXISTS " + getPre( "regyinfo" ) );
 		sql = new StringBuilder();
 		sql.append( "CREATE TABLE " + getPre( "regyinfo" ) + "(" );
 		sql.append( "`id`               int unsigned NOT NULL primary key auto_increment," );
 		sql.append( "`domID`            int unsigned NOT NULL," );
 		sql.append( "`type`             char(10) NOT NULL," );
 		sql.append( "`registrar`        text," );
 		sql.append( "`referrer`         text," );
 		sql.append( "`whoisserver`      varchar(100)," );
 		sql.append( "INDEX ( `domID` )" );
 		sql.append( ") ENGINE=MyISAM DEFAULT CHARSET=utf8" );
 		updateDB( sql.toString() );
 
 		updateDB( "DROP TABLE IF EXISTS " + getPre( "domain" ) );
 		sql = new StringBuilder();
 		sql.append( "CREATE TABLE " + getPre( "domain" ) + "(" );
 		sql.append( "`id`               int unsigned NOT NULL primary key auto_increment," );
 		sql.append( "`domID`            int unsigned NOT NULL," );
 		sql.append( "`name`             varchar(100) NOT NULL," );
 		sql.append( "`created`          varchar(100)," );
 		sql.append( "`changed`          varchar(100)," );
 		sql.append( "`expires`          varchar(100)," );
 		sql.append( "`status`           varchar(200)," );
 		sql.append( "`sponsor`          varchar(100)," );
 		sql.append( "`nserver`          text," );
 		sql.append( "`ip`               varchar(50)," );
 		sql.append( "`country`          varchar(100)," );
 		sql.append( "`countrycode`      varchar(20)," );
 		sql.append( "INDEX ( `domID` )" );
 		sql.append( ") ENGINE=MyISAM DEFAULT CHARSET=utf8" );
 		updateDB( sql.toString() );
 
 		initContactDB( "owner" );
 		initContactDB( "admin" );
 		initContactDB( "bill" );
 		initContactDB( "tech" );
 		initContactDB( "zone" );
 		initContactDB( "abuse" );
 		initContactDB( "network" );
 	}
 
 	private void initContactDB(String name) {
 		updateDB( "DROP TABLE IF EXISTS " + getPre( name ) );
 		StringBuilder sql = new StringBuilder();
 		sql.append( "CREATE TABLE " + getPre( name ) + "(" );
 		sql.append( "`id`               int unsigned NOT NULL primary key auto_increment," );
 		sql.append( "`domID`            int unsigned NOT NULL," );
 		sql.append( "`name`             varchar(100) NOT NULL," );
 		sql.append( "`email`            varchar(100)," );
 		sql.append( "`phone`            varchar(100)," );
 		sql.append( "`fax`              varchar(100)," );
 		sql.append( "`organization`     varchar(200)," );
 		sql.append( "`address`          text," );
 		sql.append( "`info`             text," );
 		sql.append( "`ip`               varchar(50)," );
 		sql.append( "`created`          varchar(100)," );
 		sql.append( "`changed`          varchar(100)," );
 		sql.append( "INDEX ( `domID` )" );
 		sql.append( ") ENGINE=MyISAM DEFAULT CHARSET=utf8" );
 		updateDB( sql.toString() );
 	}
 
 	// The main entry to save whois info.
 
 	@SuppressWarnings("unchecked")
 	public int insertDB(String dom, WhoisMap whoisMap) {
 		int domID = 0;
 
 		if (!Utility.isValidDom( dom ) || whoisMap == null || whoisMap.isEmpty())
 			return domID;
 
 		//begin to insert database
 
 		String sql = "";
 
 		List<String> rawdata = ( List<String> ) whoisMap.get( "rawdata" );
 
 		sql = "INSERT INTO " + getPre( "domainname" ) + "(domain,rawdata,querydate) VALUES('%1$s','%2$s',NOW())";
 		sql = String.format( sql, escapeQuotes( dom ), list2string( rawdata ) );
 		domID = updateDB( sql );
 
 		if (0 == domID)
 			return domID;
 
 		//update regyinfo table
 
 		Map<String, Object> regyinfo = ( Map<String, Object> ) whoisMap.get( "regyinfo" );
 		if (!Utility.isEmpty( regyinfo )) {
 			String type, registrar, referrer, whoisserver;
 
 			type = getString( regyinfo.get( "type" ), false );
 			registrar = getString( regyinfo.get( "registrar" ), false );
 			referrer = getString( regyinfo.get( "referrer" ), false );
 			whoisserver = getString( regyinfo.get( "servers" ), false );
 
 			sql = "INSERT INTO " + getPre( "regyinfo" )
 					+ "(domID,type,registrar,referrer,whoisserver) VALUES(%1$s,'%2$s','%3$s','%4$s','%5$s')";
 			sql = String.format( sql, domID, type, registrar, referrer, whoisserver );
 			updateDB( sql );
 		}
 
 		Map<String, Object> regrinfo = ( Map<String, Object> ) whoisMap.get( "regrinfo" );
 		if (Utility.isEmpty( regrinfo ))
 			return domID;
 
 		//update domain table
 
 		String[] geo = ( String[] ) whoisMap.get( "geoip" ); //get GeoIP
 		if (geo == null) {
 			geo = new String[3];
 			Arrays.fill( geo, "" );
 		}
 
 		Map<String, Object> domainData = ( Map<String, Object> ) regrinfo.get( "domain" );
 		if (!Utility.isEmpty( domainData )) {
 			String name, created, changed, expires, status, sponsor, nserver;
 
 			name = getString( domainData.get( "name" ), false );
 			created = getString( domainData.get( "created" ), false );
 			changed = getString( domainData.get( "changed" ), false );
 			expires = getString( domainData.get( "expires" ), false );
 			status = getString( domainData.get( "status" ), false );
 			sponsor = getString( domainData.get( "sponsor" ), false );
 			nserver = getString( domainData.get( "nserver" ), false );
 
 			sql = "INSERT INTO "
 					+ getPre( "domain" )
 					+ "(domID,name,created,changed,expires,status,sponsor,nserver,ip,country,countrycode) VALUES(%1$s,'%2$s','%3$s','%4$s','%5$s','%6$s','%7$s','%8$s','%9$s','%10$s','%11$s')";
 			sql = String.format( sql, domID, name, created, changed, expires, status, sponsor, nserver, geo[0], geo[1],
 					geo[2] );
 			updateDB( sql );
 		}
 
 		//update owner table
 		updateContact( regrinfo, "owner", domID );
 
 		//update admin table
 		updateContact( regrinfo, "admin", domID );
 
 		//update tech table 
 		updateContact( regrinfo, "tech", domID );
 
 		//update bill table
 		updateContact( regrinfo, "bill", domID );
 
 		//update zone table
 		updateContact( regrinfo, "zone", domID );
 
 		//update abuse table
 		updateContact( regrinfo, "abuse", domID );
 
 		//update network table 
 		updateContact( regrinfo, "network", domID );
 
 		return domID;
 	}
 
 	public int insertDB(String dom, JSONObject jsonMap) {
 		int domID = 0;
 
 		if (!Utility.isValidDom( dom ) || jsonMap == null)
 			return domID;
 
 		//begin to insert database
 
 		String sql = "";
 
 		String rawdata = "";
 		try {
 			rawdata = getString( jsonMap.get( "rawdata" ), false );
 		}
 		catch (JSONException e) {
 		}
 		sql = "INSERT INTO " + getPre( "domainname" ) + "(domain,rawdata,querydate) VALUES('%1$s','%2$s',NOW())";
 		sql = String.format( sql, escapeQuotes( dom ), rawdata );
 		domID = updateDB( sql );
 
 		if (0 == domID)
 			return domID;
 
 		//update regyinfo table
 
 		JSONObject regyinfo = null;
 		try {
 			regyinfo = jsonMap.getJSONObject( "regyinfo" );
 		}
 		catch (JSONException e) {
 		}
 
 		if (null != regyinfo) {
 			String type = "", registrar = "", referrer = "", whoisserver = "";
 
 			try {
 				type = getString( regyinfo.get( "type" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				registrar = getString( regyinfo.get( "registrar" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				referrer = getString( regyinfo.get( "referrer" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				whoisserver = getString( regyinfo.get( "servers" ), false );
 			}
 			catch (JSONException e) {
 			}
 
 			sql = "INSERT INTO " + getPre( "regyinfo" )
 					+ "(domID,type,registrar,referrer,whoisserver) VALUES(%1$s,'%2$s','%3$s','%4$s','%5$s')";
 			sql = String.format( sql, domID, type, registrar, referrer, whoisserver );
 			updateDB( sql );
 		}
 
 		JSONObject regrinfo = null;
 		try {
 			regrinfo = jsonMap.getJSONObject( "regrinfo" );
 		}
 		catch (JSONException e) {
 		}
 
 		if (null == regrinfo)
 			return domID;
 
 		//update domain table
 
 		String[] geo = new String[] { "", "", "" };
 		try {
 			JSONArray geoip = jsonMap.getJSONArray( "geoip" );
 			geo[0] = geoip.getString( 0 );
 			geo[1] = geoip.getString( 1 );
 			geo[2] = geoip.getString( 2 );
 		}
 		catch (JSONException e) {
 		}
 
 		JSONObject domainData = null;
 		try {
 			domainData = regrinfo.getJSONObject( "domain" );
 		}
 		catch (JSONException e) {
 		}
 
 		if (null != domainData) {
 			String name = "", created = "", changed = "", expires = "", status = "", sponsor = "", nserver = "";
 
 			try {
 				name = getString( domainData.get( "name" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				created = getString( domainData.get( "created" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				changed = getString( domainData.get( "changed" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				expires = getString( domainData.get( "expires" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				status = getString( domainData.get( "status" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				sponsor = getString( domainData.get( "sponsor" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				nserver = getString( domainData.get( "nserver" ), false );
 			}
 			catch (JSONException e) {
 			}
 
 			sql = "INSERT INTO "
 					+ getPre( "domain" )
 					+ "(domID,name,created,changed,expires,status,sponsor,nserver,ip,country,countrycode) VALUES(%1$s,'%2$s','%3$s','%4$s','%5$s','%6$s','%7$s','%8$s','%9$s','%10$s','%11$s')";
 			sql = String.format( sql, domID, name, created, changed, expires, status, sponsor, nserver, geo[0], geo[1],
 					geo[2] );
 			updateDB( sql );
 		}
 
 		//update owner table
 		updateContact( regrinfo, "owner", domID );
 
 		//update admin table
 		updateContact( regrinfo, "admin", domID );
 
 		//update tech table 
 		updateContact( regrinfo, "tech", domID );
 
 		//update bill table
 		updateContact( regrinfo, "bill", domID );
 
 		//update zone table
 		updateContact( regrinfo, "zone", domID );
 
 		//update abuse table
 		updateContact( regrinfo, "abuse", domID );
 
 		//update network table 
 		updateContact( regrinfo, "network", domID );
 
 		return domID;
 	}
 
 	public void updateGeoIP(int domID, String[] geo) {
 		if (domID > 0 && geo != null && geo.length == 3) {
 			String sql = "UPDATE " + getPre( "domain" )
 					+ " SET ip='%1$s',country='%2$s',countrycode='%3$s' WHERE domID=" + domID;
 			sql = String.format( sql, escapeQuotes( geo[0] ), escapeQuotes( geo[1] ), escapeQuotes( geo[2] ) );
 			updateDB( sql );
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void updateContact(Map<String, Object> regrinfo, String contact, int domID) {
 		Map<String, Object> map = null;
 
 		Object info_c = regrinfo.get( contact );
 		if (info_c != null) {
 
 			if (info_c instanceof List) {
 				map = new HashMap<String, Object>();
 				map.put( "info", info_c );
 			}
 			else if (info_c instanceof Map) {
 				map = ( Map<String, Object> ) info_c;
 			}
 			else {
 				return;
 			}
 
 			if (Utility.isEmpty( map ))
 				return;
 
 			String name, email, fax, phone, address, org, info, created, changed;
 
 			name = getString( map.get( "name" ), false );
 			email = getString( map.get( "email" ), true );
 			fax = getString( map.get( "fax" ), true );
 			phone = getString( map.get( "phone" ), true );
 			address = getAddress( map.get( "address" ) );
 			org = getString( map.get( "organization" ), false );
 			info = getString( map.get( "info" ), false );
 			created = getString( map.get( "created" ), false );
 			changed = getString( map.get( "changed" ), false );
 
 			String sql = "INSERT INTO "
 					+ getPre( contact )
 					+ "(domID,name,email,fax,phone,address,organization,info,created,changed) VALUES(%1$s,'%2$s','%3$s','%4$s','%5$s','%6$s','%7$s','%8$s','%9$s','%10$s')";
 			sql = String.format( sql, domID, name, email, fax, phone, address, org, info, created, changed );
 			updateDB( sql );
 		}
 	}
 
 	private void updateContact(JSONObject regrinfo, String contact, int domID) {
 		JSONObject json = null;
 
 		Object info_c = null;
 		try {
 			info_c = regrinfo.get( contact );
 		}
 		catch (JSONException e) {
 		}
 
 		if (info_c != null) {
 
 			if (info_c instanceof JSONArray) {
 				json = new JSONObject();
 				try {
 					json.put( "info", ( JSONArray ) info_c );
 				}
 				catch (JSONException e) {
 					json = null;
 				}
 			}
 			else if (info_c instanceof JSONObject) {
 				json = ( JSONObject ) info_c;
 			}
 			else {
 				return;
 			}
 
 			if (null == json)
 				return;
 
 			String name = "", email = "", fax = "", phone = "", address = "", org = "", info = "", created = "", changed = "";
 
 			try {
 				name = getString( json.get( "name" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				email = getString( json.get( "email" ), true );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				fax = getString( json.get( "fax" ), true );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				phone = getString( json.get( "phone" ), true );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				address = getAddress( json.get( "address" ) );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				org = getString( json.get( "organization" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				info = getString( json.get( "info" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				created = getString( json.get( "created" ), false );
 			}
 			catch (JSONException e) {
 			}
 			try {
 				changed = getString( json.get( "changed" ), false );
 			}
 			catch (JSONException e) {
 			}
 			String sql = "INSERT INTO "
 					+ getPre( contact )
 					+ "(domID,name,email,fax,phone,address,organization,info,created,changed) VALUES(%1$s,'%2$s','%3$s','%4$s','%5$s','%6$s','%7$s','%8$s','%9$s','%10$s')";
 			sql = String.format( sql, domID, name, email, fax, phone, address, org, info, created, changed );
 			updateDB( sql );
 		}
 	}
 
 	protected int updateDB(String sql) {
 		int ret = 0;
 
 		if (null == outconn)
 			return ret;
 
 		try {
 			String domsql = "SELECT LAST_INSERT_ID()";
 			Statement st = (( Connection ) outconn).createStatement();
 			st.execute( sql );
 			ResultSet rs = st.executeQuery( domsql );
 			if (rs.first())
 				ret = rs.getInt( 1 );
 			rs.close();
 			st.close();
 		}
 		catch (SQLException e) {
 			Utility.logWarn( "DBHelper::updateDB: <sql:" + sql + ">", e );
 			ret = 0;
 		}
 
 		return ret;
 	}
 
 	protected List<Object[]> queryDB(String sql) {
 		List<Object[]> ret = new ArrayList<Object[]>();
 
 		if (null == outconn)
 			return ret;
 
 		try {
 			Statement st = (( Connection ) outconn).createStatement();
 			ResultSet rs = st.executeQuery( sql );
 			ResultSetMetaData rsm = rs.getMetaData();
 			int cols = rsm.getColumnCount();
 			while (rs.next()) {
 				Object[] objs = new Object[cols];
 				for (int i = 1; i <= cols; i++) {
 					objs[i - 1] = rs.getObject( i );
 				}
 				ret.add( objs );
 			}
 			rs.close();
 			st.close();
 		}
 		catch (SQLException e) {
 			Utility.logWarn( "DBHelper::queryDB: <sql:" + sql + ">", e );
 		}
 
 		return ret;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String getString(Object obj, boolean check) {
 		String ret = "";
 
 		if (null == obj)
 			return ret;
 
 		if (obj instanceof String) {
 			ret = obj.toString();
 		}
 		else if (obj instanceof List) {
 			if (check) {
 				ret = (( List<String> ) obj).get( 0 );
 			}
 			else {
 				ret = list2string( ( List<String> ) obj );
 			}
 		}
 		else if (obj instanceof JSONArray) {
 			if (check) {
 				try {
 					ret = (( JSONArray ) obj).getString( 0 );
 				}
 				catch (JSONException e) {
 				}
 			}
 			else {
 				ret = list2string( ( JSONArray ) obj );
 			}
 		}
 		return escapeQuotes( ret );
 	}
 
 	@SuppressWarnings("unchecked")
 	private String getAddress(Object obj) {
 		String ret = "";
 
 		if (null == obj)
 			return ret;
 
 		if (obj instanceof List) {
 			ret = list2string( ( List<String> ) obj );
 		}
 		else if (obj instanceof Map) {
 			Map<String, String> ht = ( Map<String, String> ) obj;
 			String tmp = "";
 			for (String key : ht.keySet()) {
 				Object val = ht.get( key );
 				if (val instanceof String) {
 					tmp += key + ":" + val + "\r\n";
 				}
 			}
 		}
 		else if (obj instanceof String) {
 			ret = obj.toString();
 		}
 
 		return escapeQuotes( ret );
 	}
 
 	private String list2string(List<String> list) {
 		if (Utility.isEmpty( list ))
 			return "";
 
 		StringBuilder sb = new StringBuilder();
 
 		for (int i = 0; i < list.size(); i++) {
 			sb.append( list.get( i ) + "\r\n" );
 		}
 
 		return escapeQuotes( sb.toString() );
 	}
 
 	private String list2string(JSONArray json) {
 		if (null == json)
 			return "";
 
 		StringBuilder sb = new StringBuilder();
 
 		for (int i = 0; i < json.length(); i++) {
 			try {
 				sb.append( json.getString( i ) + "\r\n" );
 			}
 			catch (JSONException e) {
 			}
 		}
 
 		return escapeQuotes( sb.toString() );
 	}
 
 	private List<String> string2list(String str) {
 		if (Utility.isEmpty( str ))
 			return null;
 
 		List<String> list = new ArrayList<String>();
 		String[] array = str.split( "\r\n" );
 		for (String s : array) {
 			list.add( s );
 		}
 
 		return list.isEmpty() ? null : list;
 	}
 
 	public static String escapeQuotes(String source) {
 		if (Utility.isEmpty( source ))
 			return "";
 
 		int i = 0;
 		/* Escape \ */
 		while (i <= source.length() - 1) {
 			if (source.charAt( i ) == '\\') {
 				String firstPart = source.substring( 0, i );
 				String secondPart;
 				try {
 					secondPart = source.substring( i + 1 );
 				}
 				catch (Exception e) {
 					secondPart = "";
 				}
 				source = firstPart + "\\\\" + secondPart;
 				i++;
 			}
 			i++;
 		}
 		/* Escape ' and " */
 		i = 0;
 		while (i <= source.length() - 1) {
 			if (source.charAt( i ) == '\'') {
 				String firstPart = source.substring( 0, i );
 				String secondPart;
 				try {
 					secondPart = source.substring( i + 1 );
 				}
 				catch (Exception e) {
 					secondPart = "";
 				}
 				source = firstPart + "\\\'" + secondPart;
 				i++;
 			}
 			if (source.charAt( i ) == '\"') {
 				String firstPart = source.substring( 0, i );
 				String secondPart;
 				try {
 					secondPart = source.substring( i + 1 );
 				}
 				catch (Exception e) {
 					secondPart = "";
 				}
 				source = firstPart + "\\\"" + secondPart;
 				i++;
 			}
 			i++;
 		}
 
 		return source;
 	}
 
 	/**
 	 * Gets the whois history dates from database.
 	 * 
 	 * @param domain
 	 *            Domain name
 	 * @return Whois history date with domID in database.
 	 */
 	public Map<String, Integer> getWhoisHistoryDates(String dom) {
 		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
 		if (!Utility.isValidDom( dom ))
 			return map;
 
 		dom = dom.trim().toLowerCase();
 
 		String sql = "SELECT id,querydate FROM " + getPre( "domainname" ) + " WHERE domain='" + escapeQuotes( dom )
 				+ "' ORDER BY id desc";
 
 		List<Object[]> rt = queryDB( sql );
 		if (rt.isEmpty())
 			return map;
 
 		int domID = 0;
 		String date = null;
 		for (Object[] li : rt) {
 			domID = ( int ) Long.parseLong( li[0].toString() );
 			if (domID > 0) {
 				date = li[1].toString().substring( 0, 10 );
				map.put( date, domID );
 			}
 		}
 
 		return map;
 	}
 
 	/**
 	 * Gets whois history from database.
 	 * 
 	 * @param domID
 	 *            The query domain ID.
 	 * @return Whois history map.
 	 */
 	public WhoisMap getWhoisHistory(int domID) {
 		WhoisMap whoisMap = new WhoisMap();
 
 		if (domID <= 0)
 			return whoisMap;
 
 		String sql = "";
 		String[] geo = new String[3];
 		Arrays.fill( geo, "N/A" );
 
 		WhoisMap map = new WhoisMap();
 
 		// Get the rawdata
 		sql = "SELECT rawdata FROM " + getPre( "domainname" ) + " WHERE id=" + domID;
 
 		List<Object[]> rt = queryDB( sql );
 		if (rt.isEmpty())
 			return whoisMap;
 
 		whoisMap.set( "rawdata", string2list( rt.get( 0 )[0].toString() ) );
 
 		// Get the domain data
 		sql = "SELECT name,created,changed,expires,status,sponsor,nserver,ip,country,countrycode FROM "
 				+ getPre( "domain" ) + " WHERE domID=" + domID;
 
 		rt = queryDB( sql );
 		if (rt.isEmpty())
 			return whoisMap;
 
 		map.set( "name", rt.get( 0 )[0].toString() );
 		map.set( "created", rt.get( 0 )[1].toString() );
 		map.set( "changed", rt.get( 0 )[2].toString() );
 		map.set( "expires", rt.get( 0 )[3].toString() );
 		map.set( "status", rt.get( 0 )[4].toString() );
 		map.set( "sponsor", rt.get( 0 )[5].toString() );
 		map.set( "nserver", rt.get( 0 )[6].toString() );
 		geo[0] = rt.get( 0 )[7].toString();
 		geo[1] = rt.get( 0 )[8].toString();
 		geo[2] = rt.get( 0 )[9].toString();
 
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.domain", map.getMap() );
 
 		whoisMap.set( "geoip", geo );
 
 		// Get owner contact map
 		map = getContactFromDB( domID, "owner" );
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.owner", map.getMap() );
 
 		// Get admin contact map
 		map = getContactFromDB( domID, "admin" );
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.admin", map.getMap() );
 
 		// Get tech contact map
 		map = getContactFromDB( domID, "tech" );
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.tech", map.getMap() );
 
 		// Get bill contact map
 		map = getContactFromDB( domID, "bill" );
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.bill", map.getMap() );
 
 		// Get zone contact map
 		map = getContactFromDB( domID, "zone" );
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.zone", map.getMap() );
 
 		// Get network contact map
 		map = getContactFromDB( domID, "network" );
 		if (!map.getMap().isEmpty())
 			whoisMap.set( "regrinfo.network", map.getMap() );
 
 		// Get abuse contact map
 		map = getContactFromDB( domID, "abuse" );
 		if (!map.isEmpty())
 			whoisMap.set( "regrinfo.abuse", map.getMap() );
 
 		return whoisMap;
 	}
 
 	private WhoisMap getContactFromDB(int domID, String contact) {
 		WhoisMap map = new WhoisMap();
 
 		String sql = "SELECT name,email,phone,fax,organization,address,info,created,changed FROM " + getPre( contact )
 				+ " WHERE domID=" + domID;
 		List<Object[]> rt = queryDB( sql );
 		if (rt.isEmpty())
 			return map;
 
 		map.set( "name", rt.get( 0 )[0].toString() );
 		map.set( "email", rt.get( 0 )[1].toString() );
 		map.set( "phone", rt.get( 0 )[2].toString() );
 		map.set( "fax", rt.get( 0 )[3].toString() );
 		map.set( "organization", rt.get( 0 )[4].toString() );
 		map.set( "address", rt.get( 0 )[5].toString() );
 		map.set( "info", rt.get( 0 )[6].toString() );
 		map.set( "created", rt.get( 0 )[7].toString() );
 		map.set( "changed", rt.get( 0 )[8].toString() );
 
 		return map;
 	}
 
 }
