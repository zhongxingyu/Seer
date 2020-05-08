 package uk.co.brotherlogic.mdb.record;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import uk.co.brotherlogic.mdb.Connect;
 
 public class RecordUtils
 {
 	public static Record getRecordToListenTo(String baseformat) throws SQLException
 	{
 		Record r = getNewRecord(baseformat);
 		if (r == null)
 			r = getRecord(baseformat, 2, 6);
 		if (r == null)
 			r = getRecord(baseformat, 1, 3);
 		return r;
 	}
 
 	public static Record getRecordToListenTo(String[] baseformats) throws SQLException
 	{
 		Record toRet = null;
 
 		for (String string : baseformats)
 		{
 			Record r = getRecordToListenTo(string);
 			if (toRet == null || r.getDate().after(toRet.getDate()))
 				toRet = r;
 		}
 
 		return toRet;
 	}
 
 	public static void main(String[] args) throws SQLException
 	{
 		Connect.setForProduction();
 		System.out.println(RecordUtils.getRecordToListenTo(new String[] { "CD" }));
 	}
 
 	private static Record getNewRecord(String baseformat) throws SQLException
 	{
 		String cd_extra = "AND riploc IS NOT NULL";
 
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "SELECT recordnumber from formats,records LEFT JOIN score_table ON recordnumber = record_id WHERE format = formatnumber "
 				+ cd_extra + " AND baseformat = ? AND simon_score IS NULL";
 		PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ResultSet rs = ps.executeQuery();
 		if (rs.next())
 			return GetRecords.create().getRecord(rs.getInt(1));
 		return null;
 	}
 
 	private static Record getRecord(String baseformat, int listenCount, int months) throws SQLException
 	{
 		String cd_extra = "AND riploc IS NOT NULL";
 
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "SELECT recordnumber from formats,records LEFT JOIN score_table ON recordnumber = record_id WHERE format = formatnumber "
 				+ cd_extra + " AND baseformat = ? AND simon_rank_count = ? AND boughtdate < 'today'::date - " + months
 				+ "*'1 month'::interval AND simon_score > 5  ORDER BY boughtdate DESC LIMIT 1";
 		PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ps.setInt(2, listenCount);
 		ResultSet rs = ps.executeQuery();
 		if (rs.next())
 			return GetRecords.create().getRecord(rs.getInt(1));
 
 		return null;
 	}
 
	private static Record getRecordToRip() throws SQLException
	{
		String sql = "SELECT recordnumber from records,formats WHERE baseformat = 'CD' AND riploc IS NULL";
		PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
		ResultSet rs = ps.executeQuery();
		if (rs.next())
			return GetRecords.create().getRecord(rs.getInt(1));
		return null;
	}

 }
