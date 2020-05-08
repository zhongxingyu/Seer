 package uk.co.brotherlogic.mdb.record;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.co.brotherlogic.mdb.Connect;
 
 public class RecordUtils
 {
 	public static List<Record> getIpodRecords() throws SQLException
 	{
 		String sql = " select recordnumber,riploc,recrand,COUNT(score_value) as cnt,AVG(score_value) as val from records left join score_history ON records.recordnumber = record_id WHERE riploc IS NOT NULL AND user_id = 1 GROUP BY recordnumber,recrand,riploc HAVING count(score_value) = 1 ORDER by val DESC,recrand DESC LIMIT 5";
 		List<Record> rec = new LinkedList<Record>();
 
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			rec.add(GetRecords.create().getRecord(rs.getInt(1)));
 
 		return rec;
 	}
 
 	private static Record getNewRecord(String baseformat) throws SQLException
 	{
 		String cd_extra = "AND riploc IS NOT NULL";
 
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "SELECT recordnumber from formats,records LEFT JOIN score_history ON recordnumber = record_id WHERE format = formatnumber "
 				+ cd_extra + " AND baseformat = ? AND score_value IS NULL";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ResultSet rs = Connect.getConnection().executeQuery(ps);
 		if (rs.next())
 			return GetRecords.create().getRecord(rs.getInt(1));
 		return null;
 	}
 
	private static List<Record> getNewRecords(String baseformat)
 			throws SQLException
 	{
 		String cd_extra = "AND riploc IS NOT NULL";
 
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "SELECT recordnumber from formats,records LEFT JOIN score_history ON recordnumber = record_id WHERE format = formatnumber "
 				+ cd_extra + " AND baseformat = ? AND score_value IS NULL";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ResultSet rs = Connect.getConnection().executeQuery(ps);
 		List<Record> records = new LinkedList<Record>();
 		if (rs.next())
 			records.add(GetRecords.create().getRecord(rs.getInt(1)));
 		return records;
 	}
 
 	private static Record getRecord(String baseformat) throws SQLException
 	{
 
 		String cd_extra = "AND riploc IS NOT NULL";
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "select recordnumber,count(score_value) as cnt,avg(score_value) AS mean  from records,score_history,formats WHERE format = formatnumber AND "
 				+ cd_extra
 				+ "baseformat = ? AND recordnumber = record_id AND user_id =1 GROUP BY recrand,recordnumber HAVING count(score_value) = 1 ORDER BY avg(score_value) DESC, recrand ASC LIMIT 10";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ResultSet rs = Connect.getConnection().executeQuery(ps);
 		if (rs.next())
 			return GetRecords.create().getRecord(rs.getInt(1));
 
 		return null;
 	}
 
 	private static List<Record> getRecords(String baseformat, int num)
 			throws SQLException
 	{
 
 		String cd_extra = "AND riploc IS NOT NULL";
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "select recordnumber,count(score_value) as cnt,avg(score_value) AS mean from records,score_history,formats WHERE format = formatnumber "
 				+ cd_extra
 				+ " AND baseformat = ? AND recordnumber = record_id AND user_id =1 GROUP BY recrand,recordnumber HAVING count(score_value) = 1 "
 				+ "ORDER BY avg(score_value) DESC, recrand ASC LIMIT " + num;
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		System.out.println(ps);
 		ResultSet rs = Connect.getConnection().executeQuery(ps);
 		List<Record> records = new LinkedList<Record>();
 		while (rs.next())
 			records.add(GetRecords.create().getRecord(rs.getInt(1)));
 
 		return records;
 	}
 
 	public static Record getRecordToListenTo(String baseformat)
 			throws SQLException
 	{
 		// Always favour new records
 		Record r = getNewRecord(baseformat);
 		// r = getRecord(baseformat, 2, 6);
 		if (r == null)
 			r = getRecord(baseformat);
 		return r;
 	}
 
 	public static Record getRecordToListenTo(String[] baseformats)
 			throws SQLException
 	{
 
 		Record newRecord = null;
 		Record currRecord = null;
 		for (String string : baseformats)
 		{
 			Record tempNew = getNewRecord(string);
 			Record currRec = getRecordToListenTo(string);
 			{
 				if (tempNew != null
 						&& (newRecord == null || tempNew.getDate().after(
 								newRecord.getDate())))
 					newRecord = tempNew;
 				if (currRec != null
 						&& (currRecord == null || currRec.getDate().before(
 								currRecord.getDate())))
 					currRecord = currRec;
 			}
 		}
 
 		if (newRecord != null)
 			return newRecord;
 
 		return currRecord;
 	}
 
 	public static Record getRecordToRip() throws SQLException
 	{
 		String sql = "SELECT recordnumber from records,formats WHERE baseformat = 'CD' AND format = formatnumber AND riploc IS NULL ORDER BY owner";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			return (GetRecords.create().getRecord(rs.getInt(1)));
 		return null;
 	}
 
 	public static void main(String[] args) throws SQLException
 	{
 		for (Record rec : RecordUtils.getRecords("12", 10))
 			System.out.println(rec.getAuthor() + " - " + rec.getTitle());
 	}
 }
