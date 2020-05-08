 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package syncer;
 
 import com.mysql.jdbc.Connection;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Marc.Hoaglin
  */
 public class xbmcHandler {
 
     public final static Logger xbmcLOG = Logger.getLogger(xbmcHandler.class.getName());
 
     public static void query(String szExport) {
         if (new File(szExport).exists()) {
             new File(szExport).delete();
         }
 
         Connection con = null;
         PreparedStatement pst = null;
         ResultSet rs = null;
         String dbhost = Config.readProp("xbmc-db.host", Config.cfgFile);
         String dbport = Config.readProp("xbmc-db.port", Config.cfgFile);
         String dbuser = Config.readProp("xbmc-db.user", Config.cfgFile);
         String dbpass = Config.readProp("xbmc-db.pass", Config.cfgFile);
         String dbname = Config.readProp("xbmc-db.name", Config.cfgFile);
 
         String url = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
         try {
 
             con = (Connection) DriverManager.getConnection(url, dbuser, dbpass);
             pst = con.prepareStatement("SELECT  movie.idFile, \n"
                     + "movie.c00 as Title,\n"
                     + "movie.c07 as Year, \n"
                     + "movie.c09 as imdb_id,\n"
                     + "concat(path.strPath, files.strFilename) as fullpath,\n"
                     + "streamdetails.iVideoWidth as videoQ\n"
                     + "FROM    movie\n"
                     + "as      movie\n"
                     + "left    outer join files\n"
                     + "as      files\n"
                     + "on      movie.idfile = files.idfile\n"
                     + "left    outer join path\n"
                     + "as      path\n"
                     + "on      files.idPath = path.idPath\n"
                     + "left    join streamdetails\n"
                     + "as      streamdetails\n"
                     + "on      movie.idfile = streamdetails.idfile\n"
                     + "where     streamdetails.iVideoWidth <= 720 order by idFile desc limit 1;");
 //                    + "where   movie.idMovie = '3980';");
 //            System.out.println(pst);
             rs = pst.executeQuery();
 
             while (rs.next()) {
 
                 csvWrite(rs.getInt("idFile") + "\t" + rs.getString("Title") + "\t" + rs.getString("Year") + "\t" + rs.getString("imdb_id") + "\t" + rs.getString("fullpath") + "\t" + rs.getString("videoQ"), szExport);
             }
         } catch (SQLException ex) {
             xbmcLOG.severe(ex.getMessage());
             xbmcLOG.severe(ex.getSQLState());
 
         } finally {
 
             try {
                 if (pst != null) {
                     pst.close();
                 }
                 if (con != null) {
                     con.close();
                 }
 
             } catch (SQLException ex) {
                 xbmcLOG.severe(ex.getMessage());
                 xbmcLOG.severe(ex.getSQLState());
             }
         }
     }
 
     private static void csvWrite(String sztext, String szFile) {
         try {
             FileWriter fwrite = new FileWriter(szFile, true);
             BufferedWriter bw = new BufferedWriter(fwrite);
 //            System.out.println(sztext);
             bw.write(sztext);
             bw.newLine();
             bw.close();;
         } catch (Exception ex) {
             xbmcLOG.severe(ex.getMessage());
         }
     }
 //    private static String RemoteList;
 //    private static String RemoteUID;
 
     public static void xbmcSyncMain(final String szXList, final String szRemoteUID) {
 //        RemoteList = szXList;
 //        RemoteUID = szRemoteUID;
         new Thread(new Runnable() {
             public void run() {
                 xbmcSync(szXList, szRemoteUID);
             }
         }).start();
     }
 
     private static void xbmcSync(String szXList, String szUID) {
         String lineArray[] = new String[6];
         try {
             // Open the file that is the first 
             // command line parameter
             FileInputStream fstream = new FileInputStream(szXList);
             // Get the object of DataInputStream
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             //Read File Line By Line
             while ((strLine = br.readLine()) != null) {
                 // Print the content on the console
 //                System.out.println(strLine);
                 lineArray = strLine.split("\t");
                 if (!queryimdb(lineArray[3])) {
 //                    System.out.println("Not in local db " + lineArray[3]);
                     xbmcLOG.fine("Not in local db " + lineArray[3]);
                     if (!new File(Operator.szREQlogfolderXBMC).exists()) {
                         new File(Operator.szREQlogfolderXBMC).mkdirs();
                     }
                     Sender.putmQ(szUID, "REQ,," + lineArray[4] + ",,000000000");
 
                     // Log sent requests
                     csvWrite(strLine, Operator.szREQlogfolderXBMC + szUID + ".txt");
 
                 }
 
             }
             //Close the input stream
             in.close();
             xbmcLOG.info("Done Reading and querying");
 //            System.out.println("Done Reading and querying");
         } catch (Exception e) {//Catch exception if any
             xbmcLOG.severe(e.getMessage());
         }
     }
 
     private static boolean queryimdb(String szImDb) {
         boolean blInDb = true;
         Connection con = null;
         PreparedStatement pst = null;
         ResultSet rs = null;
         String dbhost = Config.readProp("xbmc-db.host", Config.cfgFile);
         String dbport = Config.readProp("xbmc-db.port", Config.cfgFile);
         String dbuser = Config.readProp("xbmc-db.user", Config.cfgFile);
         String dbpass = Config.readProp("xbmc-db.pass", Config.cfgFile);
         String dbname = Config.readProp("xbmc-db.name", Config.cfgFile);
 
         String url = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
         try {
 
             con = (Connection) DriverManager.getConnection(url, dbuser, dbpass);
             pst = con.prepareStatement("SELECT c00 as title FROM movie where c09 =" + "\'" + szImDb + "\';");
             rs = pst.executeQuery();
 
             blInDb = rs.next();
         } catch (SQLException ex) {
             xbmcLOG.severe(ex.getMessage());
             xbmcLOG.severe(ex.getSQLState());
         } finally {
 
             try {
                 if (pst != null) {
                     pst.close();
                 }
                 if (con != null) {
                     con.close();
                 }
 
             } catch (SQLException ex) {
                 xbmcLOG.severe(ex.getMessage());
                 xbmcLOG.severe(ex.getSQLState());
             }
         }
         return blInDb;
     }
 
     static void ReadFile(String szXList, String szUID) {
         String lineArray[] = new String[6];
         try {
             // Open the file that is the first 
             // command line parameter
             FileInputStream fstream = new FileInputStream(szXList);
             // Get the object of DataInputStream
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             //Read File Line By Line
             while ((strLine = br.readLine()) != null) {
                 // Print the content on the console
 //                System.out.println(strLine);
                 lineArray = strLine.split("\t");
                 if (!queryimdb(lineArray[3]) && (!Operator.Resuming.get(szUID).equalsIgnoreCase(lineArray[4]))) {
                     xbmcLOG.fine("Not in local db " + lineArray[3]);
 //                    System.out.println("Not in local db " + lineArray[3]);
                     if (!new File(Operator.szREQlogfolderXBMC).exists()) {
                         new File(Operator.szREQlogfolderXBMC).mkdirs();
                     }
                     xbmcLOG.info("Testing if we have already requested: " + lineArray[4] + " from " + szUID);
                     if (!Operator.FilesRequested.get(lineArray[4]).equals(szUID)) {
                         Sender.putmQ(szUID, "REQ,," + lineArray[4] + ",,000000000");
                         Operator.FilesRequested.put(lineArray[4], szUID);
                     }
 
                 }
 
             }
             //Close the input stream
             in.close();
             xbmcLOG.fine("Done Reading and querying");
         } catch (Exception e) {//Catch exception if any
             xbmcLOG.severe(e.getMessage());
             e.printStackTrace();
         }
     }
 
     public static void removeLineFromFile(String file, String lineToRemove) {

         try {
 
             File inFile = new File(file);
 
             if (!inFile.isFile()) {
                 xbmcLOG.warning("Parameter is not an existing file");
                 xbmcLOG.warning(file + " Does not exist");
                 return;
             }
 
             //Construct the new file that will later be renamed to the original filename. 
             File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
 
             BufferedReader br = new BufferedReader(new FileReader(file));
             PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
 
             String line = null;
 
             //Read from the original file and write to the new 
             //unless content matches data to be removed.
             while ((line = br.readLine()) != null) {
 
                 if (!line.trim().contains(lineToRemove)) {
 
                     pw.println(line);
                     pw.flush();
                 }
             }
             pw.close();
             br.close();
 
             //Delete the original file
             if (!inFile.delete()) {
                 xbmcLOG.warning("Could not delete file " + inFile);
                 return;
             }
 
             //Rename the new file to the filename the original file had.
             if (!tempFile.renameTo(inFile)) {
                 xbmcLOG.warning("Could not rename " + tempFile + " to " + inFile);
             }
 
         } catch (FileNotFoundException ex) {
             xbmcLOG.severe(ex.getMessage());
         } catch (IOException ex) {
             xbmcLOG.severe(ex.getMessage());
         }
     }
 }
