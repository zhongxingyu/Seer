 package pl.edu.pw.mini.x1.janiszewskit.SwipeCardReader.Tracks;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * User: root
  * Date: 1/26/13
  * Time: 7:58 PM
  */
 public class TrackTwo extends Track {
 
     private final Integer TRACK_LENGTH = 37;
 
     public TrackTwo(byte[] rawTrack) {
         super(rawTrack);
 
         String track = "";
         for (byte b : rawTrack) {
             track += (char) b;
         }
 
         String[] tracks = track.split("\\?");
        String[] fields = tracks[1].split("=");
 
        primaryAccountNumber = fields[0].split(";")[1];
 
         try {
             DateFormat formatter = new SimpleDateFormat("yy-MM");
            expirationDate = (Date) formatter.parse(fields[1]);
         } catch (ParseException e) {
             //ignore error, just create default date instead
         } finally {
             expirationDate = new Date();
         }

        serviceCode = Integer.parseInt(fields[1].substring(4, 7));
        discretionalData = fields[1].substring(7);
     }
 
 }
