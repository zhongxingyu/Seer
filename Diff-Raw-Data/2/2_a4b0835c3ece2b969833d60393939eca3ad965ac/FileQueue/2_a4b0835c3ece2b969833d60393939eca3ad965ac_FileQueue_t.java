 package uk.co.fusefm.fusealysis;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Maintains a queue of tracks to be analysed
  *
  * @author Andrew Bonney
  */
 public class FileQueue {
 
     private Connection dbConn;
     private int currentTrackID;
     private double currentInTime, currentOutTime;
     private HashMap<Integer, String> analysisFiles;
     private ArrayList<Integer> analysisIDs;
 
     /**
      * @param db Database connection instance
      */
     public FileQueue(Connection db) {
         dbConn = db;
         analysisFiles = new HashMap();
         analysisIDs = new ArrayList();
     }
 
     /**
      * Generate a list of tracks to be analysed
      *
      * @return False on MySQL connection failure, true otherwise
      */
     public boolean populateAnalysisList() {
         analysisFiles.clear();
         analysisIDs.clear();
         try {
             PreparedStatement tracks = dbConn.prepareStatement("SELECT File_ID,"
                     + "File_Location FROM tbl_files WHERE (File_Fadein is null "
                     + "OR File_Fadeout is null) ORDER BY File_ID ASC");
             ResultSet trackList = tracks.executeQuery();
             while (trackList.next()) {
                 // Remove the x:\ from the start of the track! Also format differently
                 analysisFiles.put(trackList.getInt("File_ID"), trackList.getString("File_Location").substring(3).replace("\\", "/"));
                 analysisIDs.add(trackList.getInt("File_ID"));
             }
             trackList.close();
             tracks.close();
         } catch (SQLException ex) {
             System.out.println(ex.toString());
             return false;
         }
         return true;
     }
 
     /**
      * Check if any more non-analysed tracks exist in the current queue
      *
      * @return
      */
     public boolean hasNext() {
         return !analysisIDs.isEmpty();
     }
 
     /**
      * Get the location of the next track to be analysed
      *
      * @return
      */
     public String next() {
         currentInTime = 0;
         currentOutTime = 0;
         currentTrackID = analysisIDs.get(0);
         return analysisFiles.get(currentTrackID);
     }
 
     /**
      * Set the in time for the current track
      *
      * @param inTime
      */
     public void setInPoint(double inTime) {
         currentInTime = inTime;
     }
 
     /**
      * Set the out time for the current track
      *
      * @param outTime
      */
     public void setOutPoint(double outTime) {
         currentOutTime = outTime;
     }
 
     /**
      * Save the changes to the database
      */
     public void saveTrack() {
         try {
            if (currentInTime > currentOutTime && currentOutTime != 0) {
                 System.out.println("Out time occurs before in time. Invalid. Skipping save");
             } else if (currentInTime < 0 || currentOutTime < 0) {
                 System.out.println("In or out time is negative. Invalid. Skipping save");
             } else {
                 PreparedStatement trackSave = dbConn.prepareStatement("UPDATE tbl_files SET File_Fadein = ?, File_Fadeout = ? WHERE File_ID = ?");
                 if (currentInTime == 0) {
                     trackSave.setNull(1, Types.NUMERIC);
                 } else {
                     trackSave.setDouble(1, currentInTime);
                 }
                 if (currentOutTime == 0) {
                     trackSave.setNull(2, Types.NUMERIC);
                 } else {
                     trackSave.setDouble(2, currentOutTime);
                 }
                 trackSave.setInt(3, currentTrackID);
                 trackSave.execute();
                 trackSave.close();
             }
         } catch (SQLException ex) {
             System.out.println("Failed to save track ID " + currentTrackID + " (" + analysisFiles.get(currentTrackID) + ")");
             System.out.println(ex.toString());
         }
         analysisIDs.remove(analysisIDs.indexOf(currentTrackID));
         analysisFiles.remove(currentTrackID);
     }
 }
