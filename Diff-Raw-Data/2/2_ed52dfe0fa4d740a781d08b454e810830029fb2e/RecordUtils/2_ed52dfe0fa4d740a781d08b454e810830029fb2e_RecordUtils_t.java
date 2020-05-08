 package uk.co.brotherlogic.mdb.record;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.co.brotherlogic.mdb.Connect;
 
 public class RecordUtils {
 	public static List<Record> getIpodRecords() throws SQLException {
		String sql = "select recordnumber,riploc,recrand,COUNT(score_value) as cnt,AVG(score_value) as val from records left join score_history ON records.recordnumber = record_id WHERE riploc IS NOT NULL AND user_id = 1 GROUP BY recordnumber,recrand,riploc HAVING count(score_value) = 1 ORDER by val DESC,recrand DESC LIMIT 5";
 		List<Record> rec = new LinkedList<Record>();
 
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			rec.add(GetRecords.create().getRecord(rs.getInt(1)));
 
 		return rec;
 	}
 
 	private static Record getNewRecord(String baseformat) throws SQLException {
 		String cd_extra = "AND riploc IS NOT NULL";
 
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "SELECT recordnumber from formats,records LEFT JOIN score_table ON recordnumber = record_id WHERE format = formatnumber "
 				+ cd_extra + " AND baseformat = ? AND simon_score IS NULL";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ResultSet rs = Connect.getConnection().executeQuery(ps);
 		if (rs.next())
 			return GetRecords.create().getRecord(rs.getInt(1));
 		return null;
 	}
 
 	private static Record getRecord(String baseformat, int listenCount,
 			int months) throws SQLException {
 
 		String cd_extra = "AND riploc IS NOT NULL";
 		int min_score = 5;
 		if (months > 3)
 			min_score = 7;
 
 		if (!baseformat.equalsIgnoreCase("cd"))
 			cd_extra = "";
 
 		String sql = "SELECT recordnumber from formats,records,score_table,recrand WHERE recordnumber = recrand.record_id AND recordnumber = score_table.record_id AND format = formatnumber "
 				+ cd_extra
 				+ " AND baseformat = ? AND simon_rank_count = ? AND simon_score_date < 'today'::date - "
 				+ months
 				+ "*'1 month'::interval AND simon_score > "
 				+ min_score
 				+ " AND salepricepence < 0 ORDER BY simon_score DESC, randval ASC LIMIT 1";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, baseformat);
 		ps.setInt(2, listenCount);
 		ResultSet rs = Connect.getConnection().executeQuery(ps);
 		if (rs.next())
 			return GetRecords.create().getRecord(rs.getInt(1));
 
 		return null;
 	}
 
 	public static Record getRecordToListenTo(String baseformat)
 			throws SQLException {
 		// Always favour new records
 		Record r = getNewRecord(baseformat);
 		// r = getRecord(baseformat, 2, 6);
 		if (r == null)
 			r = getRecord(baseformat, 1, 3);
 		return r;
 	}
 
 	public static Record getRecordToListenTo(String[] baseformats)
 			throws SQLException {
 
 		Record newRecord = null;
 		Record currRecord = null;
 		for (String string : baseformats) {
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
 
 	public static Record getRecordToRip() throws SQLException {
 		String sql = "SELECT recordnumber from records,formats WHERE baseformat = 'CD' AND format = formatnumber AND riploc IS NULL ORDER BY owner";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			return (GetRecords.create().getRecord(rs.getInt(1)));
 		return null;
 	}
 
 	public static void main(String[] args) throws SQLException {
 		Connect.setForProduction();
 		// System.out.println(RecordUtils.getRecordToListenTo(new String[] { "7"
 		// }));
 		// System.out.println(RecordUtils.getRecordToListenTo(new String[] {
 		// "12",
 		// "10", "7" }));
 		System.out.println(RecordUtils
 				.getRecordToListenTo(new String[] { "CD" }));
 		// System.out.println(getRecordToRip());
 		Connect.getConnection().printStats();
 	}
 }
