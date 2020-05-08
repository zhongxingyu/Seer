 package uk.co.brotherlogic.mdb;
 
 import java.sql.SQLException;
 import java.util.Date;
 
 import junit.framework.TestCase;
 import uk.co.brotherlogic.mdb.artist.Artist;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.groop.Groop;
 import uk.co.brotherlogic.mdb.groop.LineUp;
 import uk.co.brotherlogic.mdb.label.Label;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 import uk.co.brotherlogic.mdb.record.Track;
 
 public class TrackTest extends TestCase
 {
 
    private boolean built = false;
 
    public TrackTest()
    {
       super();
       Connect.setForDevMode();
    }
 
    public void buildEmptyTrackForTest()
    {
       if (!built)
       {
          // Create
          Record r = new Record();
          r.setAuthor("fake-author");
          r.setCategory(new Category("fake-cat", 12));
          r.setCatNo("fake-cat-no");
          r.setDate(new Date());
          r.setDiscogsNum(12);
          r.setFormat(new Format("fake-format", "12"));
          r.setLabel(new Label("fake-label"));
          r.setNotes("fake-notes");
          r.setOwner(1);
          r.setPrice(12.65);
          r.setReleaseMonth(12);
          r.setReleaseType(1);
          r.setTitle("fake-empty-tracks-testing");
          r.setYear(2000);
 
          try
          {
             r.save();
 
             Artist g1 = new Artist("DonkeyManTester");
             Artist g2 = new Artist("Goat ManTester");
             Groop g3 = new Groop("AnimalsTester");
             LineUp lu = new LineUp(g3);
             lu.addArtist(g1);
             lu.addArtist(g2);
 
             Artist p1 = new Artist("Bunny ProducerTester");
 
             Track t1 = new Track(1);
             t1.setFormTrackNumber(1);
 
             Track t2 = new Track(2);
             t2.setFormTrackNumber(2);
 
             // This should cause the record to update
             r.addTrack(t1);
             r.addTrack(t2);
             r.save();
 
             Record r2 = GetRecords.create().getRecords("fake-tracks-testing").get(0);
             assert (r2.getTracks().iterator().next().getLineUps().size() == 1);
 
          }
          catch (SQLException e)
          {
             System.err.println("Exception");
             e.printStackTrace();
             assert (false);
          }
          built = true;
       }
    }
 
    public void buildTrackForTest()
    {
       if (!built)
       {
          // Create
          Record r = new Record();
          r.setAuthor("fake-author");
          r.setCategory(new Category("fake-cat", 12));
          r.setCatNo("fake-cat-no");
          r.setDate(new Date());
          r.setDiscogsNum(12);
          r.setFormat(new Format("fake-format", "12"));
          r.setLabel(new Label("fake-label"));
          r.setNotes("fake-notes");
          r.setOwner(1);
          r.setPrice(12.65);
          r.setReleaseMonth(12);
          r.setReleaseType(1);
          r.setTitle("fake-tracks-testing");
          r.setYear(2000);
 
          try
          {
             r.save();
 
             Artist g1 = new Artist("DonkeyManTester");
             Artist g2 = new Artist("Goat ManTester");
             Groop g3 = new Groop("AnimalsTester");
             LineUp lu = new LineUp(g3);
             lu.addArtist(g1);
             lu.addArtist(g2);
 
             Artist p1 = new Artist("Bunny ProducerTester");
 
             Track t1 = new Track(1);
             t1.setLengthInSeconds(100);
             t1.setTitle("DonkeyTester");
             t1.addLineUp(lu);
             t1.addPersonnel(p1);
             t1.setFormTrackNumber(1);
 
             Track t2 = new Track(2);
             t2.setLengthInSeconds(120);
             t2.setTitle("HelpTester");
             t2.addLineUp(lu);
             t2.addPersonnel(p1);
             t2.setFormTrackNumber(1);
 
             // This should cause the record to update
             r.addTrack(t1);
             r.addTrack(t2);
             r.save();
 
             Record r2 = GetRecords.create().getRecords("fake-tracks-testing").get(0);
             assert (r2.getTracks().iterator().next().getLineUps().size() == 1);
 
          }
          catch (SQLException e)
          {
             System.err.println("Exception");
             e.printStackTrace();
             assert (false);
          }
          built = true;
       }
    }
 
    public void testFormTrack()
    {
       // Create
       Record r = new Record();
       r.setAuthor("fake-author");
       r.setCategory(new Category("fake-cat", 12));
       r.setCatNo("fake-cat-no");
       r.setDate(new Date());
       r.setDiscogsNum(12);
       r.setFormat(new Format("fake-format", "12"));
       r.setLabel(new Label("fake-label"));
       r.setNotes("fake-notes");
       r.setOwner(1);
       r.setPrice(12.65);
       r.setReleaseMonth(12);
       r.setReleaseType(1);
       r.setTitle("fake-titles-format");
       r.setYear(2000);
 
       try
       {
          r.save();
 
          Artist g1 = new Artist("DonkeyMan");
          Artist g2 = new Artist("Goat Man");
          Groop g3 = new Groop("Animals");
          LineUp lu = new LineUp(g3);
          lu.addArtist(g1);
          lu.addArtist(g2);
 
          Artist p1 = new Artist("Bunny Producer");
 
          Track t1 = new Track(1);
          t1.setLengthInSeconds(100);
          t1.setTitle("Donkey");
          t1.addLineUp(lu);
          t1.addPersonnel(p1);
          t1.setFormTrackNumber(1);
 
          Track t2 = new Track(2);
          t2.setLengthInSeconds(120);
          t2.setTitle("Help");
          t2.addLineUp(lu);
          t2.addPersonnel(p1);
          t2.setFormTrackNumber(1);
 
          // This should cause the record to update
          r.addTrack(t1);
          r.addTrack(t2);
          r.save();
 
          Record r2 = GetRecords.create().getRecords("fake-titles-format").get(0);
          assert (r2.getNumberOfFormatTracks() == 1);
       }
       catch (SQLException e)
       {
          System.err.println("Exception");
          e.printStackTrace();
          assert (false);
       }
    }
 
    public void testFormTrackArtist()
    {
       try
       {
          buildTrackForTest();
          Record nrec = GetRecords.create().getRecords("fake-tracks-testing").get(0);
          assert (nrec.getTracks().iterator().next().getLineUps().size() == 1);
          assert (nrec.getTrackRep(1).length() > 0);
       }
       catch (Exception e)
       {
          e.printStackTrace();
          assert (false);
       }
    }
 
    public void testFormTrackPerm()
    {
       try
       {
          buildTrackForTest();
          Record nrec = GetRecords.create().getRecord(9140);
          assert (nrec.getTrackRep(1).length() > 0);
       }
       catch (Exception e)
       {
          e.printStackTrace();
          assert (false);
       }
    }
 
    public void testTrack()
    {
       // Create
       Record r = new Record();
       r.setAuthor("fake-author");
       r.setCategory(new Category("fake-cat", 12));
       r.setCatNo("fake-cat-no");
       r.setDate(new Date());
       r.setDiscogsNum(12);
       r.setFormat(new Format("fake-format", "12"));
       r.setLabel(new Label("fake-label"));
       r.setNotes("fake-notes");
       r.setOwner(1);
       r.setPrice(12.65);
       r.setReleaseMonth(12);
       r.setReleaseType(1);
       r.setTitle("fake-titles");
       r.setYear(2000);
 
       try
       {
          r.save();
 
          Artist g1 = new Artist("DonkeyMan");
          Artist g2 = new Artist("Goat Man");
          Groop g3 = new Groop("Animals");
          LineUp lu = new LineUp(g3);
          lu.addArtist(g1);
          lu.addArtist(g2);
 
          Artist p1 = new Artist("Bunny Producer");
 
          Track t1 = new Track(1);
          t1.setLengthInSeconds(100);
          t1.setTitle("Donkey");
          t1.addLineUp(lu);
          t1.addPersonnel(p1);
          t1.setFormTrackNumber(1);
 
          Track t2 = new Track(2);
          t2.setLengthInSeconds(120);
          t2.setTitle("Help");
          t2.addLineUp(lu);
          t2.addPersonnel(p1);
          t2.setFormTrackNumber(2);
 
          // This should cause the record to update
          r.addTrack(t1);
          r.addTrack(t2);
          r.save();
 
          Record r2 = GetRecords.create().getRecords("fake-titles").get(0);
          assert (r2.getTracks().size() == 2);
          assert (r2.getTracks().iterator().next().getPersonnel().size() == 1);
          assert (r2.getNumberOfFormatTracks() == 2);
          assert (r2.getTracks().iterator().next().getLineUps().size() == 1);
       }
       catch (SQLException e)
       {
          System.err.println("Exception");
          e.printStackTrace();
          assert (false);
       }
    }
 
    public void testTrackRead()
    {
       try
       {
          buildEmptyTrackForTest();
          Record r = GetRecords.create().getRecords("fake-empty-tracks-testing").get(0);
          for (int i = 1; i <= r.getNumberOfFormatTracks(); i++)
          {
             assert (r.getFormTrackArtist(i).length() > 0);
             System.err.println("HERE = " + r.getFormTrackTitle(i));
             assert (r.getFormTrackTitle(i).length() > 0);
          }
 
       }
       catch (SQLException e)
       {
          e.printStackTrace();
          assert (false);
       }
    }
 }
