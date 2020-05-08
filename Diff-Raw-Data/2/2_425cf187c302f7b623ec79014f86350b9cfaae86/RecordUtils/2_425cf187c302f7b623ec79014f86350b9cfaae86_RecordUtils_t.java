 package uk.co.brotherlogic.mdb.record;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.co.brotherlogic.mdb.Connect;
 
 public class RecordUtils
 {
    public static List<Record> getIpodRecords() throws SQLException
    {
       String sql = " select recordnumber,riploc,recrand,COUNT(score_value) as cnt,AVG(score_value) as val from records left join score_history ON records.recordnumber = record_id WHERE riploc IS NOT NULL AND user_id = 1 GROUP BY recordnumber,recrand,riploc HAVING count(score_value) = 1 ORDER by val DESC,recrand DESC LIMIT 5";
       List<Record> rec = new LinkedList<Record>();
 
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
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
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setString(1, baseformat);
       ResultSet rs = Connect.getConnection().executeQuery(ps);
       while (rs.next())
       {
          Record r = GetRecords.create().getRecord(rs.getInt(1));
          System.err.println(r.getAuthor() + " - " + r.getTitle() + " : " + r.getChildren().size());
          if (r.getChildren().size() == 0)
             return r;
       }
 
       return null;
    }
 
    public static List<Record> getNewRecords(String baseformat) throws SQLException
    {
       String cd_extra = "AND riploc IS NOT NULL";
 
       if (!baseformat.equalsIgnoreCase("cd"))
          cd_extra = "";
 
       String sql = "SELECT recordnumber from formats,records LEFT JOIN score_history ON recordnumber = record_id WHERE format = formatnumber "
             + cd_extra + " AND baseformat = ? AND score_value IS NULL";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setString(1, baseformat);
       ResultSet rs = Connect.getConnection().executeQuery(ps);
       List<Record> records = new LinkedList<Record>();
       while (rs.next())
          records.add(GetRecords.create().getRecord(rs.getInt(1)));
       return records;
    }
 
    public static Track getRandomCDTrack(double minScore) throws SQLException
    {
       // Get a random record
       String sql = "SELECT recordnumber from records LEFT JOIN score_history ON recordnumber = record_id WHERE riploc IS NOT NULL GROUP BY recordnumber HAVING avg(score_value) > ? ORDER BY random() LIMIT 1";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setDouble(1, minScore);
       ResultSet rs = Connect.getConnection().executeQuery(ps);
       if (rs.next())
       {
          Record r = GetRecords.create().getRecord(rs.getInt(1));
          List<Track> tracks = new LinkedList<Track>(r.getTracks());
          Collections.shuffle(tracks);
          return tracks.get(0);
       }
 
       return null;
    }
 
    public static Record getRecord(String baseformat) throws SQLException
    {
 
       String cd_extra = "AND riploc IS NOT NULL";
       if (!baseformat.equalsIgnoreCase("cd"))
          cd_extra = "";
 
       String sql = "select recordnumber,count(score_value) as cnt,avg(score_value) AS mean  from records,score_history,formats WHERE format = formatnumber AND "
             + cd_extra
            + "baseformat = ? AND boughtdate < (now() - interval '3 months') AND recordnumber = record_id AND user_id =1 AND salepricepence < 0 GROUP BY records.owner, recrand,recordnumber HAVING count(score_value) = 1 ORDER BY owner, mean ASC LIMIT 10";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setString(1, baseformat);
       ResultSet rs = Connect.getConnection().executeQuery(ps);
       while (rs.next())
       {
          Record r = GetRecords.create().getRecord(rs.getInt(1));
          System.err.println(r.getAuthor() + " - " + r.getTitle() + " : " + r.getChildren().size());
          if (r.getChildren().size() == 0)
             return r;
       }
 
       return null;
    }
 
    public static List<Record> getRecords(String baseformat, int num) throws SQLException
    {
 
       String cd_extra = "AND riploc IS NOT NULL";
       if (!baseformat.equalsIgnoreCase("cd"))
          cd_extra = "";
 
       String sql = "select recordnumber,count(score_value) as cnt,avg(score_value) AS mean from records,score_history,formats WHERE format = formatnumber "
             + cd_extra
             + " AND baseformat = ? AND boughtdate < (now() - interval '3 months') AND recordnumber = record_id AND salepricepence < 0 AND user_id =1 GROUP BY records.owner, recrand,recordnumber HAVING count(score_value) = 1 "
             + "ORDER BY owner, random() LIMIT " + num;
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setString(1, baseformat);
       ResultSet rs = Connect.getConnection().executeQuery(ps);
       List<Record> records = new LinkedList<Record>();
       while (rs.next())
          records.add(GetRecords.create().getRecord(rs.getInt(1)));
 
       return records;
    }
 
    public static Record getRecordToListenTo(String baseformat) throws SQLException
    {
       // Always favour new records
       Record r = getNewRecord(baseformat);
       // r = getRecord(baseformat, 2, 6);
       if (r == null)
          r = getRecord(baseformat);
       return r;
    }
 
    public static Record getRecordToListenTo(String[] baseformats) throws SQLException
    {
 
       Record newRecord = null;
       Record currRecord = null;
       for (String string : baseformats)
       {
          Record tempNew = getNewRecord(string);
          Record currRec = getRecordToListenTo(string);
          {
             if (tempNew != null
                   && (newRecord == null || tempNew.getDate().after(newRecord.getDate())))
                newRecord = tempNew;
             if (currRec != null
                   && (currRecord == null || currRec.getDate().before(currRecord.getDate())))
                currRecord = currRec;
          }
       }
 
       if (newRecord != null)
          return newRecord;
 
       return currRecord;
    }
 
    public static List<Record> getRecordToRip(int n) throws SQLException
    {
       String sql = "SELECT recordnumber from records,formats WHERE baseformat = 'CD' AND format = formatnumber AND riploc IS NULL ORDER BY owner";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ResultSet rs = ps.executeQuery();
       List<Record> recs = new LinkedList<Record>();
       while (rs.next())
       {
          Record rec = (GetRecords.create().getRecord(rs.getInt(1)));
          if (!rec.getFormat().getName().equals("DVD") && rec.getTracks().size() > 0)
             recs.add(rec);
 
          if (recs.size() == n)
             return recs;
       }
       return recs;
    }
 
    public static void main(String[] args) throws Exception
    {
       Record rec = RecordUtils.getRecordToListenTo("12");
       System.out.println(rec.getAuthor() + " - " + rec.getTitle());
    }
 }
