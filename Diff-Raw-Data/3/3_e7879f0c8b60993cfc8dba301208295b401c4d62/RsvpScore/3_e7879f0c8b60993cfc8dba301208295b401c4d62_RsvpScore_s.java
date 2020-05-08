 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 final class RsvpScore {
   static final class RsvpQuery {
     public final String name, st;
 
     public RsvpQuery(final String name, final String st) {
       this.name = name;
       this.st = st;
     }
   }
 
   public static void main(final String[] args) {
     final String conStr = "jdbc:oracle:thin:@SMARTR510-SERV1:1521:orcl";
     final String filename = "out/%s_%d_%d.csv";
     final int MIN_GROUP = Integer.parseInt(args[2]);
     final int MAX_GROUP = Integer.parseInt(args[3]);
     final int MIN_WEEK = 1, MAX_WEEK = 6;
     final List<RsvpQuery> queries = new ArrayList<RsvpQuery>();
     final Scanner sc = new Scanner(System.in);
     while (sc.hasNextLine()) {
       String line = sc.nextLine();
       if (line.startsWith("--")) {
         final String name = line.substring(2);
         line = "";
         while (sc.hasNextLine()) {
           line += " " + sc.nextLine();
           if (line.endsWith(";")) {
             line = line.substring(0, line.length() - 1);
             break;
           }
         }
         queries.add(new RsvpQuery(name, line));
       }
     }
     try {
       final Connection con = DriverManager.getConnection(conStr, args[0], args[1]);
       for (RsvpQuery query : queries) {
         final PreparedStatement st = con.prepareStatement(query.st);
         System.out.println(query.name);
         for (int i=MIN_GROUP; i<=MAX_GROUP; ++i) {
           System.out.println("  " + i);
           st.setInt(1, i);
           for (int j=MIN_WEEK; j<=MAX_WEEK; ++j) {
             System.out.println("    " + j);
             final String f = String.format(filename, query.name, i, j);
             BufferedWriter bw = null;
             try {bw = new BufferedWriter(new FileWriter(f));}
             catch (final IOException e) {
               throw new RuntimeException(e);
             }
             final PrintWriter out = new PrintWriter(bw);
             st.setInt(2, j);
             final ResultSet rs = st.executeQuery();
             while (rs.next())
               out.println(rs.getInt(1) + "," + rs.getInt(2));
           }
         }
       }
     } catch (final SQLException e) {throw new RuntimeException(e);}
   }
 }
