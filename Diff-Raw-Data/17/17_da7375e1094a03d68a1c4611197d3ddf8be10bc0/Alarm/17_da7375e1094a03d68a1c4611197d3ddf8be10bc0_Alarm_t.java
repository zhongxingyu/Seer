 package uk.co.brotherlogic.mdb.alarm;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.co.brotherlogic.mdb.User;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 import de.umass.lastfm.Authenticator;
 import de.umass.lastfm.Session;
 import de.umass.lastfm.Track;
 
 public class Alarm
 {
    private final double minScore;
    private final int overallTime;
    private final long startTime;
    private final int maxLength;
 
    List<Record> records;
    int pointer = 0;
 
    Session cSession = null;
 
    String user, password, secret, key;
 
    public Alarm(double score, int time, int length)
    {
       minScore = score;
       overallTime = time;
       maxLength = length;
       startTime = System.currentTimeMillis();
 
       try
       {
          BufferedReader reader = new BufferedReader(new FileReader("password"));
          user = reader.readLine().trim();
          password = reader.readLine().trim();
          secret = reader.readLine().trim();
          key = reader.readLine().trim();
          reader.close();
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 
    private void getRecords() throws SQLException
    {
       System.err.print("Getting Records");
       records = new LinkedList<Record>();
      Collection<Record> recs = GetRecords.create().getRecords(GetRecords.SHELVED, "CD");
      for (Record r : recs)
       {
          boolean acceptable = false;
         if ((r.getRiploc() != null) && (r.getRiploc().trim().length() > 0))
            for (User user : User.getUsers())
               if (r.getScore(user) >= minScore)
                  acceptable = true;
          if (acceptable)
             records.add(r);
       }
 
       Collections.shuffle(records);
    }
 
    private Song pickSong() throws SQLException
    {
      System.err.println("Picking Song");
       if (records == null)
          getRecords();
       Record r = records.get(pointer++);
 
       List<Integer> tracks = new LinkedList<Integer>();
       for (int i = 1; i <= r.getNumberOfFormatTracks(); i++)
          tracks.add(i);
       Collections.shuffle(tracks);
 
       return new Song(r, tracks.get(1));
    }
 
    private void playSong(Song s)
    {
       System.err.println("Playing Song " + s.r);
       String command = "mplayer " + s.r.getRiploc() + "/" + s.getResolveTrack() + "*";
       try
       {
          System.err.println(command);
          Process p = Runtime.getRuntime().exec(command);
          p.waitFor();
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
       catch (InterruptedException e)
       {
          e.printStackTrace();
       }
    }
 
    public void registerSong(Song s) throws SQLException
    {
       if (cSession == null)
          cSession = Authenticator.getMobileSession(user, password, secret, key);
       Track.updateNowPlaying(s.r.getFormTrackArtist(s.cdTrack), s.r.getFormTrackTitle(s.cdTrack),
             cSession);
    }
 
    public void run()
    {
       while (System.currentTimeMillis() - startTime < overallTime)
          try
          {
             Song song = pickSong();
             playSong(song);
             registerSong(song);
          }
          catch (SQLException e)
          {
             e.printStackTrace();
          }
    }
 }
 
 class Song
 {
    Record r;
    int cdTrack;
 
    public Song(Record rec, int track)
    {
       r = rec;
       cdTrack = track;
    }
 
    public String getResolveTrack()
    {
       if (cdTrack < 10)
          return "00" + cdTrack;
       else if (cdTrack < 100)
          return "0" + cdTrack;
       else
          return "" + cdTrack;
    }
 }
