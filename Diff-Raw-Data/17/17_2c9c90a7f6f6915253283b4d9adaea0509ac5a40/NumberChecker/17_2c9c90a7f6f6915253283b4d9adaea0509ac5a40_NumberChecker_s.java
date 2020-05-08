 package uk.co.brotherlogic.mdb.numbers;
 
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.co.brotherlogic.mdb.User;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 public class NumberChecker
 {
    public static void main(String[] args) throws SQLException
    {
       final int TWELVES = 75;
       final int CDS = 1200;
       final int SEVENS = 700;
 
       NumberChecker checker = new NumberChecker();
 
       if (args[0].equalsIgnoreCase("simon"))
          if (args[1].equals("12"))
             checker.checkNumbers("12", User.getUser("Simon"), 16 * TWELVES);
          else if (args[1].equals("CD"))
             checker.checkNumbers("CD", User.getUser("Simon"), CDS);
          else if (args[1].equals("7"))
             checker.checkNumbers("7", User.getUser("Simon"), SEVENS);
         else if (args[0].equalsIgnoreCase("jeanette"))
            if (args[1].equals("12"))
               checker.checkNumbers("12", User.getUser("Jeanette"), 9 * TWELVES);
            else if (args[1].equals("CD"))
               checker.checkNumbers("CD", User.getUser("Simon"), CDS);
            else if (args[1].equals("7"))
               checker.checkNumbers("7", User.getUser("Simon"), 2 * SEVENS);
    }
 
    List<Record> allRecords = null;
 
    public void checkNumbers(String baseFormat, final User user, int count) throws SQLException
    {
       List<Record> records = getAllRecords();
       List<Record> toRemove = new LinkedList<Record>();
       for (Record record : records)
          if ((record.getOwner() != user.getID())
                || !record.getFormat().getBaseFormat().equalsIgnoreCase(baseFormat)
                || record.getCategory().getCatName().equalsIgnoreCase("VinylVulture")
                || (record.getSoldPrice() >= 0))
             toRemove.add(record);
       records.removeAll(toRemove);
 
       if (records.size() > count)
       {
          System.out.println(baseFormat + "s over count by " + (records.size() - count));
          Collections.sort(records, new Comparator<Record>()
          {
 
             @Override
             public int compare(Record o1, Record o2)
             {
                try
                {
                   return (int) (o2.getScore(user) - o1.getScore(user));
                }
                catch (SQLException e)
                {
                   e.printStackTrace();
                }
                return 0;
             }
 
          });
 
          double baseScore = records.get(count).getScore(user);
          for (Record record : records)
             if (record.getScore(user) <= baseScore)
                System.out.println(record.getAuthor() + " - " + record.getTitle() + " ["
                      + record.getScore(user) + " / " + record.getScoreCount(user) + "]");
       }
       else
          System.out.println(baseFormat + "s under count by " + (count - records.size()));
    }
 
    private List<Record> getAllRecords() throws SQLException
    {
       if (allRecords == null)
       {
          allRecords = new LinkedList<Record>();
          for (Integer number : GetRecords.create().getRecordNumbers())
             allRecords.add(GetRecords.create().getRecord(number));
       }
 
       return new LinkedList<Record>(allRecords);
    }
 }
