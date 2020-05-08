 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 final class RsvpTextStatistics {
   static final class RsvpTextCategory {
     public final String name;
     public final String[] genres;
 
     public RsvpTextCategory(final String name, final String[] genres) {
       this.name = name;
       this.genres = genres;
     }
   }
 
   static final class RsvpQuery {
     public final String name, stmt;
     public final String[] outputFields;
 
     public RsvpQuery(final String name, final String stmt, final String[] outputFields) {
       this.name = name;
       this.stmt = stmt;
       this.outputFields = outputFields;
     }
   }
 
   public static void main(final String[] args) {
    final String deleteStmt = "delete from %s";
     final String categorySt = "select id from category where name = ?";
     final String genreSt = "select id from genre where category = (%s) and name = ?";
     final String featureSt = "insert into %%s values ((%s),%%s)";
    String st = String.format(genreSt, categorySt);
     final String insertSt = String.format(featureSt, st);
     final String inConString = "jdbc:oracle:thin:@SMARTR510-SERV1:1521:orcl";
     final String outConString = "jdbc:sqlite:" + args[0];
     final List<RsvpTextCategory> categories = new ArrayList<RsvpTextCategory>();
     final List<RsvpQuery> queries = new ArrayList<RsvpQuery>();
     Scanner sc = null;
     try {sc = new Scanner(new File(args[1]));}
     catch (final FileNotFoundException ex) {
       throw new RuntimeException(ex);
     }
     while (sc.hasNextLine()) {
       final String[] genres = sc.nextLine().split(",");
       categories.add(new RsvpTextCategory(genres[0], genres));
     }
     sc = new Scanner(System.in);
     while (sc.hasNextLine()) {
       String line = "";
       while (line.isEmpty()) {
         if (sc.hasNextLine()) line = sc.nextLine();
         else break;
       }
       if (line.isEmpty()) break;
       final String name = line.substring(2);
       final String[] fields = sc.nextLine().substring(2).split(",");
       st = "";
       while (sc.hasNextLine()) {
         line = sc.nextLine();
         st += ' ' + line;
         if (line.endsWith(";")) {
           st = st.substring(0, st.length() - 1);
           break;
         }
       }
       queries.add(new RsvpQuery(name, st, fields));
     }
     try {Class.forName("org.sqlite.JDBC");}
     catch (ClassNotFoundException ex) {
       throw new RuntimeException(ex);
     }
     try {
       final Connection inCon = DriverManager.getConnection(inConString, "nicholasg", "nGrasevski1");
       final Connection outCon = DriverManager.getConnection(outConString);
       outCon.setAutoCommit(false);
       final Statement inSt = inCon.createStatement();
      final Statement deleteSt = outCon.createStatement();
       for (RsvpQuery rq : queries) {
         System.out.println(rq.name);
         st = String.format(deleteStmt, rq.name);
        deleteSt.executeUpdate(st);
         for (RsvpTextCategory rtc : categories) {
           System.out.println("  " + rtc.name);
           for (String g : rtc.genres) {
             st = String.format(rq.stmt, rtc.name, g);
             final ResultSet rs = inSt.executeQuery(st);
             while (rs.next()) {
               st = "";
               for (int i=1; i<=rq.outputFields.length; ++i) {
                 st += Integer.toString(rs.getInt(i));
                 if (i < rq.outputFields.length) st += ',';
               }
               st = String.format(insertSt, rq.name, st);
               final PreparedStatement outSt = outCon.prepareStatement(st);
               outSt.setString(1, rtc.name);
               outSt.setString(2, g);
               outSt.executeUpdate();
               outSt.close();
             }
           }
           outCon.commit();
         }
       }
     } catch (final SQLException ex) {
       throw new RuntimeException(ex);
     }
   }
 }
