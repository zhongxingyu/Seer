 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.sql.Statement;
 
 public class MySQLConnection {
 
     public static void main(String[] args) {
 
         Connection con = null;
         PreparedStatement pst = null;
         ResultSet rs = null;
         Statement st = null;
 	Statement del = null;
 
         String url = "jdbc:mysql://mysql11.cp.hostnet.nl:3306/db30984_stayc";
         String user = "u30984_fhd";
         String password = "stayconnected";
         
         String accountName;
         String[] locationData = new String[5000];
         String[] addressData = new String[5000];
         float[] time = new float[5000];
         float[] lat = new float[5000];
         float[] lon = new float[5000];
         int locationIndex = 0;
         int addressIndex = 0;
         int timeIndex = 0;
         int latIndex = 0;
         int lonIndex = 0;
 
         try {
             con = DriverManager.getConnection(url, user, password);
             pst = con.prepareStatement("SELECT * FROM testdatabase");
             rs = pst.executeQuery();
 
             while (rs.next()) {
             	accountName = rs.getString(1);
                 //System.out.println(accountName);
                 
                 String location = rs.getString(2);
 		//get inputid for deletion after done
 		String inputid = rs.getString(3);
 		//System.out.println(inputid);
                 location = location.substring(1, location.length()-1);
             	String[] locations = location.split(";");
 
             	for(String loc: locations){
             		String[] locale = loc.split(", ");
             		for(String locs: locale) {
             			if(locs.contains("Location")) {
             				locationData[locationIndex] = (locs);
             				locationIndex++;
             				//System.out.println(locationData[locationIndex-1]);
             			} else {
             				addressData[addressIndex] = (locs);
             				addressIndex++;
             				//System.out.println(addressData[addressIndex-1]);
             			}
             		}
             	}
             	
             	System.out.println("User = " + accountName);
             	System.out.println("Locations = ");
             	for(String loc: locationData) {
             		if(loc != null) {
             			String[] splitLoc = loc.split(",");
             			for(String split: splitLoc) {
             				if(split.contains("mTime")) {
             					String[] splittime = split.split("=");
             					time[timeIndex] = Float.valueOf(splittime[1]);
             					timeIndex++;
                     			System.out.println(splittime[1]);
             				} else if(split.contains("mLatitude")) {
             					String[] splitlat = split.split("=");
             					lat[latIndex] = Float.valueOf(splitlat[1]);
             					latIndex++;
                     			System.out.println(splitlat[1]);
             				} else if(split.contains("mLongitude")) {
             					String[] splitlon = split.split("=");
             					lon[lonIndex] = Float.valueOf(splitlon[1]);
             					lonIndex++;
                     			System.out.println(splitlon[1]);
             				}
             			}
             		}
             	}
 
             	System.out.println("Address's = ");
             	for(String loc: addressData) {
             		if(loc != null) {
             			System.out.println(loc);
             		}
             	}
		//Account {name=hiddehensel@gmail.com, type=com.google}
  		//selecteer stuk tussen = en , 
		// String oldaccount = accountName;
		// accountName = oldaccount.split(",|=")[1];
 
 
 
 
             	con.setAutoCommit(false);
                 st = con.createStatement();
 
                 for(int i = 0; i < time.length; i++) {
                 	if(time[i] != 0.0f) {
                 		st.addBatch("INSERT INTO sortedDatabase(accountName, time, lat, lon, address) "+
                 			"VALUES('"+accountName+"', '"+time[i]+"', '"+lat[i]+"', '"+lon[i]+"', '"+addressData[i]+"')");
                 	}
                 }
 
                 int counts[] = st.executeBatch();
 		
 		//test dit deletion of row from database table
 		del = con.createStatement();
 		del.addBatch("DELETE FROM testDatabase WHERE inputid='"inputid"'");
 		del.executeBatch();
 		//del.execute("DELETE FROM testDatabase WHERE inputid='"inputid"'");
                 con.commit();
 		
 		
                 System.out.println("Committed " + counts.length + " updates");
             }
 
         } catch (SQLException ex) {
             Logger lgr = Logger.getLogger(MySQLConnection.class.getName());
             lgr.log(Level.SEVERE, ex.getMessage(), ex);
 
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (pst != null) {
                     pst.close();
                 }
                 if (st != null) {
                     st.close();
                 }
                 if (con != null) {
                     con.close();
                 }
 
             } catch (SQLException ex) {
                 Logger lgr = Logger.getLogger(MySQLConnection.class.getName());
                 lgr.log(Level.WARNING, ex.getMessage(), ex);
             }
         }
     }
 }
