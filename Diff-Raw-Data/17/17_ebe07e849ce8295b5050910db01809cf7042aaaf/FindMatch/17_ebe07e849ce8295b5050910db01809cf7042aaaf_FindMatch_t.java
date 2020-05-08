 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class FindMatch {
 
     public static void main(String[] args) throws IOException {
 
         Connection con = null;
         PreparedStatement pst = null;
         PreparedStatement pstl = null;
         ResultSet rs = null;
         ResultSet rsl = null;
         Statement st = null;
         Statement del = null;
 
         String url = "jdbc:mysql://mysql11.cp.hostnet.nl:3306/db30984_stayc";
         String user = "u30984_fhd";
         String password = "stayconnected";
         
         List<String> cits = new ArrayList<String>();
         List<String> citsCompare = new ArrayList<String>();
         List<String> foundMatch = new ArrayList<String>();
         
         float match;
         
         int tot;
         int totCompare;
         
         try {
             con = DriverManager.getConnection(url, user, password);
             pst = con.prepareStatement("SELECT * FROM profiles");
             rs = pst.executeQuery();
             
             con.setAutoCommit(false);
 
             while(rs.next()) {
             	cits.clear();
             	tot = 0;
             	
             	String accountName = rs.getString(1);
                 String sex = rs.getString(2);
                 int activity = Integer.parseInt(rs.getString(3));
                 String cities = rs.getString(4);
                 
                 System.out.println("*************************NEXT*****************************");
                 
                 System.out.println("Comparing with: " + accountName);
 
                 System.out.println("*************************NEXT*****************************");
                 
                 cities = cities.substring(1, cities.length()-1);
             	String[] citss = cities.split(",");
             	for(String loc: citss) {
             		if(isInteger(loc.trim())) {
         				tot += Integer.parseInt(loc.trim());
         			}
             		if(!cits.contains(loc.trim()) || isInteger(loc.trim())) {
             			cits.add(loc.trim());
             		}
             	}
                 
                 pstl = con.prepareStatement("SELECT * FROM profiles WHERE accountName != '" + accountName + "'");
                 rsl = pstl.executeQuery();
                 
                 while(rsl.next()) {
                 	citsCompare.clear();
                 	match = 0.0f;
                 	totCompare = 0;
                 	
                 	String accountNameCompare = rsl.getString(1);
                     String sexCompare = rsl.getString(2);
                     int activityCompare = Integer.parseInt(rsl.getString(3));
                     String citiesCompare = rsl.getString(4);
                     
                     System.out.println(accountNameCompare);
                     System.out.println("*************************NEXT*****************************");
                     
                     if(sex.equals(sexCompare)) {
                     	System.out.println("Sex is the same continueing");
                     	System.out.println("");
                     	match = 0;
                     	continue;
                     }
                     
                     float activityPer = 0.0f;
                     activityPer = (1.0f/(Math.abs(activity-activityCompare)+1))*100.0f;
                     System.out.println("Activity Compare: " + activityPer);
                     
                     citiesCompare = citiesCompare.substring(1, citiesCompare.length()-1);
                 	String[] citssCompare = citiesCompare.split(",");
                 	for(String loc: citssCompare) {
             			if(isInteger(loc.trim())) {
             				totCompare += Integer.parseInt(loc.trim());
             			}
                 		if(!citsCompare.contains(loc.trim()) || isInteger(loc.trim())) {
                 			citsCompare.add(loc.trim());
                 		}
                 	}
                 	
                 	
                 	float overeenkomst = 0.0f;
                 	
                 	
             		for(String loc: cits) {
             			for(String locc: citsCompare) {
             				if(loc.equalsIgnoreCase(locc) && !isInteger(locc.trim())) {
             					int i = cits.indexOf(loc);
             					int k = citsCompare.indexOf(locc);
         						float n = Float.valueOf(cits.get(i+1));
         						float m = Float.valueOf(citsCompare.get(k+1));
         						float citPer = (n*100.0f)/tot;
         						float citPerComp = (m*100.0f)/totCompare;
         						if(citPer<citPerComp)
         							overeenkomst += citPer;
         						else
         							overeenkomst += citPerComp;
         						break;
             				}
             			}
             		}
             		
                 	System.out.println("City Compare: " + overeenkomst);
             		
                 	match = (activityPer + overeenkomst) / 2.0f;
                 	System.out.println("match: " + match);
                 	
                 	if(match > 90.0f) {
                 		foundMatch.add(accountName + "," + accountNameCompare);
                 	}
                 	
                 	System.out.println("");
                 	
                 	//Send to database
                 }
             }
 
         	con.setAutoCommit(false);
            st = con.createStatement();
            
            
             for(String loc: foundMatch) {
             	String[] matches = loc.split(",");
             	for(int i = 0; i < matches.length; i+=2) {
	            	//System.out.println(matches[i] + ";;" + matches[i+1]);
 	            	
	            	st.addBatch("INSERT INTO matches (accountName, matches) "+
                			"VALUES('"+ matches[i] +"', '"+ matches[i+1] +"')");

             	}
             }
            st.executeBatch();
             con.commit();
         } catch (SQLException ex) {
         	Logger lgr = Logger.getLogger(MySQLConnection.class.getName());
         	lgr.log(Level.SEVERE, ex.getMessage(), ex);
     	  } 
         
         
         finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (pst != null) {
                     pst.close();
                 }
                 if (pstl != null) {
                     pstl.close();
                 }
                 if (st != null) {
                     st.close();
                 }
                 if (rsl != null) {
                     rsl.close();
                 }
                 if (del != null) {
                     del.close();
                 }
                 if (con != null) {
                     con.close();
                 }
             } catch (SQLException ex) {
                 Logger lgr = Logger.getLogger(FindLocation.class.getName());
                 lgr.log(Level.WARNING, ex.getMessage(), ex);
             }
         }
     }
     
     public static boolean isInteger(String s) {
         try { 
             Integer.parseInt(s); 
         } catch(NumberFormatException e) { 
             return false; 
         }
         // only got here if we didn't return false
         return true;
     }
 }
