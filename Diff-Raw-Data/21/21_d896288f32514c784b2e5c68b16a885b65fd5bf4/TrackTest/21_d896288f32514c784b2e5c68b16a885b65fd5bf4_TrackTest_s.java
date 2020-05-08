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
 
 	public void testTrack()
 	{
 		//Create
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
 		r.setState(2);
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
 
 			//This should cause the record to update
 			r.addTrack(t1);
 			r.save();
 
 			Record r2 = GetRecords.create().getRecords("fake-titles").get(0);
			assert (r2.getTracks().size() == 1);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 }
