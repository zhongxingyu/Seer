 package uk.co.brotherlogic.mdb.record;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import uk.co.brotherlogic.mdb.Connect;
 import uk.co.brotherlogic.mdb.User;
 
 public class RecordScore
 {
    public static void add(Record rec, User user, int score) throws SQLException
    {
       if (rec != null && user != null)
       {
          String sql = "INSERT INTO score_history (record_id,user_id,score_date,score_value) VALUES (?,?,now(),?)";
 
          PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql.toString());
 
          ps.setInt(1, rec.getNumber());
          ps.setInt(2, user.getID());
          ps.setInt(3, score);
 
          ps.execute();
          ps.close();
 
          // Update the record to ranked
          rec.save();
       }
    }
 
    public static double get(Record rec) throws SQLException
    {
       double scoreVal = 0.0;
       int count = 0;
       for (User user : User.getUsers())
       {
          double score = get(rec, user);
          if (score >= 0)
          {
             scoreVal += score;
             count++;
          }
       }
 
       if (count > 0)
          return scoreVal / count;
       else
          return 0.0;
    }
 
    public static double get(Record rec, User user) throws SQLException
    {
       String sql = "SELECT score_value from score_history WHERE record_id = ? AND user_id = ?";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setInt(1, rec.getNumber());
       ps.setInt(2, user.getID());
       ResultSet rs = ps.executeQuery();
       int count = 0;
       double sum = 0;
       while (rs.next())
       {
          count++;
          sum += rs.getInt(1);
       }
 
       if (count == 0)
          return -1;
       else
          return sum / count;
    }
 
    public static int getCount(Record rec, User user) throws SQLException
    {
       String sql = "SELECT count(score_value) from score_history WHERE record_id = ? AND user_id = ?";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setInt(1, rec.getNumber());
       ps.setInt(2, user.getID());
       ResultSet rs = ps.executeQuery();
       int count = 0;
       if (rs.next())
          count = rs.getInt(1);
       rs.close();
       ps.close();
 
       return count;
    }
 
    public static double getLastScore(Record rec, User user) throws SQLException
    {
       String sql = "SELECT score_value from score_history WHERE record_id = ? AND user_id = ? ORDER BY score_date DESC LIMIT 1";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setInt(1, rec.getNumber());
       ps.setInt(2, user.getID());
       ResultSet rs = ps.executeQuery();
       double count = 0.0;
       if (rs.next())
          count = rs.getDouble(1);
       rs.close();
       ps.close();
 
       return count;
    }
 
    public static long getLastScoreDate(Record rec, User user) throws SQLException
    {
       String sql = "SELECT score_date from score_history WHERE record_id = ? AND user_id = ? ORDER BY score_date DESC LIMIT 1";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setInt(1, rec.getNumber());
       ps.setInt(2, user.getID());
       ResultSet rs = ps.executeQuery();
       long count = 0;
       if (rs.next())
          count = rs.getTimestamp(1).getTime();
       rs.close();
       ps.close();
 
       return count;
    }
 
    public static Integer[] getScores(Record rec) throws SQLException
    {
       List<Integer> scores = new LinkedList<Integer>();
      String sql = "SELECT score_value from score_history WHERE record_id = ? ORDER BY score_date DESC";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setInt(1, rec.getNumber());
       ResultSet rs = ps.executeQuery();
 
       while (rs.next())
          scores.add(rs.getInt(1));
 
       return scores.toArray(new Integer[0]);
    }
 
    public static void main(String[] args) throws Exception
    {
       Connect.setForProdMode();
       Record r = GetRecords.create().getRecord(6270);
       System.out.println(RecordScore.getScores(r));
    }
 
    public static void scoreRecords(Collection<Record> records) throws SQLException
    {
       Map<Integer, Integer> recordScoreSum = new TreeMap<Integer, Integer>();
       Map<Integer, Integer> scoreCount = new TreeMap<Integer, Integer>();
 
       String sql = "SELECT record_id,score_value from score_history where user_id = ?";
       PreparedStatement ps = Connect.getConnection().getPreparedStatement(sql);
       ps.setInt(1, User.getUser("simon").getID());
 
       ResultSet rs = ps.executeQuery();
       while (rs.next())
       {
          int recId = rs.getInt(1);
          int score = rs.getInt(2);
 
          if (!recordScoreSum.containsKey(recId))
          {
             recordScoreSum.put(recId, score);
             scoreCount.put(recId, 1);
          }
          else
          {
             recordScoreSum.put(recId, recordScoreSum.get(recId) + score);
             scoreCount.put(recId, scoreCount.get(recId) + 1);
          }
       }
 
       for (Record r : records)
          if (recordScoreSum.containsKey(r.getNumber()))
             r.setScore((recordScoreSum.get(r.getNumber()) + 0.0) / scoreCount.get(r.getNumber()));
 
    }
 }
