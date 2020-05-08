 package uk.co.brotherlogic.mdb;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.label.Label;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 public class RecordTest extends TestCase
 {
 	private static boolean built = false;
 
 	public RecordTest()
 	{
 		super();
 		Connect.setForDevMode();
 	}
 
 	private void buildRecord()
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
 			r.setTitle("fake-title");
 			r.setYear(2000);
 			r.setDiscogsNum(1234);
 
 			try
 			{
 				// Persist
 				r.save();
 				built = true;
 
 				// Set some scores
 				r.addScore(User.getUser("Simon"), 5);
 			}
 			catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void testCatNoSize()
 	{
 		try
 		{
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getCatNos().size() == 1);
 			assert (nrec.getCatNoString().equals("fake-cat-no"));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testDiscog()
 	{
 		try
 		{
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getCatNos().size() == 1);
 			assert (nrec.getLabels().size() == 1);
 			assert (nrec.getDiscogsNum() == 1234);
 			nrec.setDiscogsNum(1235);
 			nrec.save();
 			Record nrec2 = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec2.getDiscogsNum() == 1235);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testFileAdd()
 	{
 		Record r = new Record();
 		r.setAuthor("Burman, R.D.");
 		r.setTitle("Hello");
 		r.setFormat(new Format("12\"", "12"));
 
 		try
 		{
 			assert (!r.getFileAdd().contains("." + File.separator));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}

 	public void testLabels()
 	{
 		try
 		{
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getLabels().size() == 1);
 			assert (nrec.getLabels().iterator().next().getName()
 					.equals("fake-label"));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testNewAuthor()
 	{
 		try
 		{
 			buildRecord();
 			Record r = GetRecords.create().getRecords("fake-title").get(0);
 			r.setAuthor("New Fake Author");
 			r.save();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getAuthor().equals("New Fake Author"));
 
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testOverlap()
 	{
 		try
 		{
 			Collection<Record> records = GetRecords.create().getRecords(
 					"Overload");
 			List<String> overloadReps = new LinkedList<String>();
 			for (Record rec : records)
 				if (rec.getFormat().getBaseFormat().equals("CD"))
 					overloadReps.add(rec.getFileAdd());
 			for (int i = 0; i < overloadReps.size(); i++)
 				for (int j = i + 1; j < overloadReps.size(); j++)
 				{
 					if (overloadReps.get(i).equals(overloadReps.get(j)))
 						System.err.println(overloadReps.get(i) + " vs "
 								+ overloadReps.get(j));
 					assert (!overloadReps.get(i).equals(overloadReps.get(j)));
 				}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testParent()
 	{
 		try
 		{
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getParent() == -1);
 			nrec.setParent(123);
 			nrec.save();
 			Record nrec2 = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec2.getParent() == 123);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testRetrieveSize()
 	{
 		try
 		{
 			buildRecord();
 			// Retrieve
 			List<Record> recs = GetRecords.create().getRecords("fake-title");
 			assert (recs.size() == 1);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testRiploc()
 	{
 		try
 		{
 			buildRecord();
 			// Retrieve
 			Record rec = GetRecords.create().getRecords("fake-title").get(0);
 			rec.setRiploc("testing");
 			rec.save();
 
 			Record rec2 = GetRecords.create().getRecords("fake-title").get(0);
 			assert (rec2.getRiploc().equals("testing"));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testScore()
 	{
 		try
 		{
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			double score = nrec.getScore();
 			double sscore = nrec.getScore(User.getUser("Simon"));
 			assert (score >= 0);
 			assert (sscore >= 0);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testWeaver()
 	{
 		try
 		{
 			Record weaver = GetRecords.create().getRecords(
 					"The Fallen By Watch Bird").get(0);
 			assert (!weaver.getTrackRep(1).contains(Record.REPLACE));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 }
