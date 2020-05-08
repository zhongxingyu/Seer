 package uk.co.brotherlogic.mdb.record;
 
 /**
  * Class to deal with getting groops
  * 
  * @author Simon Tucker
  */
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import uk.co.brotherlogic.mdb.Connect;
 import uk.co.brotherlogic.mdb.artist.Artist;
 import uk.co.brotherlogic.mdb.artist.GetArtists;
 import uk.co.brotherlogic.mdb.categories.GetCategories;
 import uk.co.brotherlogic.mdb.format.GetFormats;
 import uk.co.brotherlogic.mdb.groop.GetGroops;
 import uk.co.brotherlogic.mdb.groop.Groop;
 import uk.co.brotherlogic.mdb.groop.LineUp;
 import uk.co.brotherlogic.mdb.label.GetLabels;
 import uk.co.brotherlogic.mdb.label.Label;
 
 public class GetRecords {
 	public static GetRecords create() throws SQLException {
 		if (singleton == null)
 			singleton = new GetRecords();
 		return singleton;
 	}
 
 	public static void main(String[] args) throws Exception {
 		Connect.setForProduction();
 
 		long sTime = System.currentTimeMillis();
 		Record rec = GetRecords.create().getRecord(5);
 
 		for (Track track : rec.getTracks())
 			track.getTitle();
 
 		long eTime = System.currentTimeMillis();
 
 		System.out.println(eTime - sTime);
 		System.out.println(Connect.getConnection().getSCount());
 		Connect.getConnection().printStats();
 
 	}
 
 	PreparedStatement addRecord;
 
 	PreparedStatement getRecord;
 
 	PreparedStatement getTracks;
 	// Flag indicating overlap of record titles
 	boolean nonOver;
 	Collection<Record> records;
 	PreparedStatement updateTrack;
 
 	PreparedStatement updateRecord;
 
 	PreparedStatement getPersonnel;
 
 	private static GetRecords singleton;
 
 	private static final int RANKED = 4;
 
 	private static final int UNRANKED = 3;
 
 	public static final int SHELVED = 1;
 
 	private GetRecords() throws SQLException {
 		// Create the records
 		records = new Vector<Record>();
 
 		getTracks = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT TrackRefNumber FROM Track WHERE RecordNumber = ? AND TrackNumber = ?");
 		addRecord = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"INSERT INTO Records (Title,BoughtDate,Format,Notes,ReleaseYear,Category,Author,ReleaseMonth,ReleaseType, modified,Owner,purchase_price) VALUES (?,?,?,?,?,?,?,?,?,now(),?,?)");
 		getRecord = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT RecordNumber FROM Records WHERE Title = ? AND BoughtDate = ? AND Format = ? AND Notes = ? ORDER BY RecordNumber DESC");
 		updateTrack = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"UPDATE TRACK SET TrackName = ?, Length = ? WHERE RecordNumber = ? AND TrackNumber = ?");
 		updateRecord = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"UPDATE Records SET Title = ?, BoughtDate = ?, Format = ?, Notes = ?, ReleaseYear = ?, Category = ?, Author = ?, ReleaseMonth = ?, ReleaseType = ?, modified = now(), owner = ?, purchase_price = ?, shelfpos = ? WHERE RecordNumber = ?");
 		getPersonnel = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT Personnel.TrackNumber, Artist.artist_id, Artist.show_name, Artist.sort_name FROM Artist INNER JOIN Personnel ON Artist.artist_id = Personnel.ArtistNumber WHERE (((Personnel.TrackNumber)=?))");
 
 	}
 
 	public void addGroopsAndPersonnel(int trackNumber, Track toAdd)
 			throws SQLException {
 
 		// Now do the personnel
 		int[] artNums = GetArtists.create().addArtists(toAdd.getPersonnel());
 
 		// Add the entries in the personnel table
 		PreparedStatement ps = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"INSERT INTO Personnel (ArtistNumber,TrackNumber) VALUES (?,?)");
 		for (int artNum : artNums) {
 			ps.setInt(1, artNum);
 			ps.setInt(2, trackNumber);
 			ps.addBatch();
 		}
 		ps.executeBatch();
 
 		// Now add the groups
 		Iterator<LineUp> grIt = toAdd.getLineUps().iterator();
 		while (grIt.hasNext())
 			addLineUp(trackNumber, grIt.next());
 	}
 
 	public void addLineUp(int trackNumber, LineUp lineup) throws SQLException {
 		// First get the groop number
 		int lineUpNum = GetGroops.build().addLineUp(lineup);
 
 		// Now add the groop into the line up set
 		PreparedStatement ps = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"INSERT INTO LineUpSet (TrackNumber, LineUpNumber) VALUES (?,?)");
 		ps.setInt(1, trackNumber);
 		ps.setInt(2, lineUpNum);
 		ps.execute();
 	}
 
 	public int addRecord(Record in) throws SQLException {
 		// First get the format number
 		int formatNumber = in.getFormat().save();
 
 		// Get tbe category number
 		int catNum = in.getCategory().save();
 
 		NumberFormat nForm = NumberFormat.getInstance();
 		nForm.setMaximumFractionDigits(2);
 		nForm.setMinimumFractionDigits(2);
 
 		// Add the record itself
 		addRecord.setString(1, in.getTitle());
 		addRecord.setDate(2,
 				new java.sql.Date(in.getDate().getTime().getTime()));
 		addRecord.setInt(3, formatNumber);
 		addRecord.setString(4, in.getNotes());
 		addRecord.setInt(5, in.getReleaseYear());
 		addRecord.setInt(6, catNum);
 		addRecord.setString(7, in.getAuthor());
 		addRecord.setInt(8, in.getReleaseMonth());
 		addRecord.setInt(9, in.getReleaseType());
 		addRecord.setInt(10, in.getOwner());
 		addRecord.setDouble(11, in.getPrice());
 		addRecord.execute();
 
 		getRecord.setString(1, in.getTitle());
 		getRecord.setDate(2,
 				new java.sql.Date(in.getDate().getTime().getTime()));
 		getRecord.setInt(3, formatNumber);
 		getRecord.setString(4, in.getNotes());
 		ResultSet rs = getRecord.executeQuery();
 		rs.next();
 		int recordNumber = rs.getInt(1);
 
 		rs.close();
 
 		// Get the label numbers
 		int[] labNums = new int[in.getLabels().size()];
 		int labPointer = 0;
 		for (Label lab : in.getLabels())
 			labNums[labPointer++] = lab.save();
 
 		for (int labNum : labNums) {
 			// Add the numbers to the label set
 			PreparedStatement ps = Connect
 					.getConnection()
 					.getPreparedStatement(
 							"INSERT INTO LabelSet (RecordNumber,LabelNumber) VALUES (?,?)");
 			ps.setInt(1, recordNumber);
 			ps.setInt(2, labNum);
 			ps.execute();
 		}
 
 		// Add the catalogue numbers
 		Iterator<String> cIt = in.getCatNos().iterator();
 		while (cIt.hasNext()) {
 			String catNo = cIt.next();
 			PreparedStatement ps = Connect
 					.getConnection()
 					.getPreparedStatement(
 							"INSERT INTO CatNoSet (RecordNumber,CatNo) VALUES (?,?)");
 			ps.setInt(1, recordNumber);
 			ps.setString(2, catNo);
 			ps.execute();
 		}
 
 		// Add the tracks
 		Iterator<Track> tIt = in.getTracks().iterator();
 		while (tIt.hasNext())
 			addTrack(recordNumber, tIt.next());
 
 		return recordNumber;
 	}
 
 	public void addTrack(int recordNumber, Track toAdd) throws SQLException {
 
 		// First add the track data and get the track number
 		PreparedStatement ps = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"INSERT INTO Track (RecordNumber,TrackNumber,TrackName,Length) VALUES (?,?,?,?)");
 		ps.setInt(1, recordNumber);
 		ps.setInt(2, toAdd.getTrackNumber());
 		ps.setString(3, toAdd.getTitle());
 		ps.setInt(4, toAdd.getLengthInSeconds());
 		ps.execute();
 
 		// Now get that track number
 		getTracks.setInt(1, recordNumber);
 		getTracks.setInt(2, toAdd.getTrackNumber());
 		ResultSet rs = getTracks.executeQuery();
 
 		if (!rs.next())
 			throw new SQLException("Unable to add track");
 
 		int trackNumber = rs.getInt(1);
 		rs.close();
 
 		addGroopsAndPersonnel(trackNumber, toAdd);
 	}
 
 	public Set<String> getCatNos(int recNumber) throws SQLException {
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT CatNo FROM CatNoSet WHERE RecordNumber = ?");
 		s.setInt(1, recNumber);
 		ResultSet rs = s.executeQuery();
 
 		Set<String> retSet = new TreeSet<String>();
 		while (rs.next())
 			retSet.add(rs.getString(1));
 
 		return retSet;
 	}
 
 	public Collection<Artist> getCompilers(Record rec) throws SQLException {
 		Collection<Artist> artists = new LinkedList<Artist>();
 		String sql = "SELECT artist_id FROM compiler where record_id = ?";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setInt(1, rec.getNumber());
 		ps.execute();
 		ResultSet rs = ps.getResultSet();
 		while (rs.next())
 			artists.add(GetArtists.create().getArtist(rs.getInt(1)));
 		return artists;
 	}
 
 	public Set<Label> getLabels(int recNumber) throws SQLException {
 		PreparedStatement s = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT LabelName FROM Labels,LabelSet WHERE Labels.LabelNumber = LabelSet.LabelNumber AND RecordNumber = ?");
 		s.setInt(1, recNumber);
 		ResultSet rs = s.executeQuery();
 
 		Set<Label> retSet = new TreeSet<Label>();
 		while (rs.next())
 			retSet.add(GetLabels.create().getLabel(rs.getString(1)));
 
 		return retSet;
 	}
 
 	public Set<LineUp> getLineUps(int trackNumber) throws SQLException {
 		// Prepare the set to be returned
 		Set<LineUp> retSet = new TreeSet<LineUp>();
 
 		PreparedStatement s = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT LineUpSet.LineUpNumber,Groops.GroopNumber FROM LineUpSet,Groops,LineUp WHERE LineUp.LineUpNumber = LineUpSet.LineUpNumber AND LineUp.GroopNumber = Groops.GroopNumber AND TrackNumber = ?");
 		s.setInt(1, trackNumber);
 		ResultSet rs = Connect.getConnection().executeQuery(s);
 
 		// Process this query
 		while (rs.next()) {
 			// Get the line up number and groop name
 			int lineUpNumber = rs.getInt(1);
 			int groopNumber = rs.getInt(2);
 
 			Groop tempGroop = GetGroops.build().getGroop(groopNumber);
 			retSet.add(tempGroop.getLineUp(lineUpNumber));
 		}
 
 		return retSet;
 	}
 
 	public boolean getMyState() {
 		return nonOver;
 	}
 
 	public Set<Artist> getPersonnel(int trackNumber) throws SQLException {
 		Set<Artist> retSet = new TreeSet<Artist>();
 
 		// Set the parameter
 		getPersonnel.setInt(1, trackNumber);
 		ResultSet rs = Connect.getConnection().executeQuery(getPersonnel);
 
 		while (rs.next())
 			retSet.add(new Artist(rs.getString(3), rs.getString(4), rs
 					.getInt(2)));
 
 		rs.close();
 
 		return retSet;
 	}
 
 	public List<Record> getRankedRecords(String format) throws SQLException {
 		List<Record> rankedRecords = new LinkedList<Record>();
 
 		PreparedStatement ps = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT recordnumber from records,score_table,formats where format = formatnumber and baseformat = ? AND recordnumber = record_id AND state = ? ORDER BY simon_rank ASC");
 		ps.setString(1, format);
 		ps.setInt(2, Record.RANKED);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			rankedRecords.add(getRecord(rs.getInt(1)));
 
 		return rankedRecords;
 	}
 
 	public Record getRecord(int recNumber) throws SQLException {
 		Record rec = null;
 		try {
 			// Get the single record
 			rec = getSingleRecord(recNumber);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 
 		return rec;
 	}
 
 	public Collection<Integer> getRecordNumbers() throws SQLException {
 		// Use a tree set to keep things in order
 		Set<Integer> titleSet = new TreeSet<Integer>();
 
 		// Collect the titles
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT RecordNumber FROM Records");
 		ResultSet rs = s.executeQuery();
 		while (rs.next())
 			titleSet.add(rs.getInt(1));
 		rs.close();
 		s.close();
 
 		// Return the collection
 		return titleSet;
 	}
 
 	public Collection<Integer> getRecordNumbersWithoutAuthors()
 			throws SQLException {
 		// Use a tree set to keep things in order
 		Set<Integer> titleSet = new TreeSet<Integer>();
 
 		// Collect the titles
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT RecordNumber FROM Records WHERE Author is null");
 		ResultSet rs = s.executeQuery();
 		while (rs.next())
 			titleSet.add(rs.getInt(1));
 		rs.close();
 		s.close();
 
 		// Return the collection
 		return titleSet;
 	}
 
 	public Collection<Record> getRecords(int status, String format)
 			throws SQLException {
 		Collection<Record> records = new LinkedList<Record>();
 
 		if (status == SHELVED) {
 			PreparedStatement s = Connect
 					.getConnection()
 					.getPreparedStatement(
							"SELECT RecordNumber FROM Records,formats WHERE format = formatnumber and baseformat = ? AND shelfpos > 0 AND boxed = 0");
 			s.setString(1, format);
 			ResultSet rs = s.executeQuery();
 			while (rs.next())
 				records.add(getRecord(rs.getInt(1)));
 			rs.close();
 			s.close();
 		}
 
 		return records;
 	}
 
 	public List<Record> getRecords(String title) throws SQLException {
 		Collection<Integer> numbers = new Vector<Integer>();
 		List<Record> records = new Vector<Record>();
 
 		// First generate a list of all the record numbers with this title
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT RecordNumber FROM Records WHERE Title = ?");
 		s.setString(1, title);
 		ResultSet rs = s.executeQuery();
 		while (rs.next())
 			numbers.add(rs.getInt(1));
 		rs.close();
 		s.close();
 
 		// Now get all the records for these numbers
 		Iterator<Integer> lIt = numbers.iterator();
 		while (lIt.hasNext())
 			records.add(getRecord((lIt.next()).intValue()));
 		return records;
 	}
 
 	public Collection<Record> getRecordsFeaturingGroop(String groopName,
 			int groopNumber) throws SQLException {
 		List<Record> featuring = new LinkedList<Record>();
 
 		String sql = "SELECT DISTINCT records.recordnumber from records,track,lineupset,lineup WHERE records.recordnumber = track.recordnumber AND track.trackrefnumber = lineupset.tracknumber AND lineupset.lineupnumber = lineup.lineupnumber AND lineup.groopnumber = ?";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setInt(1, groopNumber);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next()) {
 			Record rec = getRecord(rs.getInt(1));
 			if (!rec.getAuthor().equals(groopName))
 				featuring.add(getRecord(rs.getInt(1)));
 		}
 
 		return featuring;
 	}
 
 	public List<Record> getRecordsToRank() throws SQLException {
 		List<Record> rankedRecords = new LinkedList<Record>();
 
 		PreparedStatement ps = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT recordnumber from records,score_table where recordnumber = record_id AND state = ? ORDER BY simon_rank DESC");
 		ps.setInt(1, Record.RANKED);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			rankedRecords.add(getRecord(rs.getInt(1)));
 
 		return rankedRecords;
 	}
 
 	public List<Record> getRecordsToRank(String format) throws SQLException {
 		List<Record> rankedRecords = new LinkedList<Record>();
 
 		String sql = "SELECT recordnumber from records,score_table,formats where format = formatnumber and baseformat = ? AND recordnumber = record_id AND state = ? ORDER BY simon_rank DESC";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, format);
 		ps.setInt(2, GetRecords.UNRANKED);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			rankedRecords.add(getRecord(rs.getInt(1)));
 
 		return rankedRecords;
 	}
 
 	public Collection<Record> getRecordsWithAuthor(String author)
 			throws SQLException {
 		Collection<Record> records = new LinkedList<Record>();
 
 		String sql = "SELECT recordnumber from records where author = ?";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, author);
 
 		ResultSet rs = ps.executeQuery();
 		while (rs.next())
 			records.add(getRecord(rs.getInt(1)));
 
 		return records;
 
 	}
 
 	public Collection<Record> getRecordsWithPers(int artNumber)
 			throws SQLException {
 		List<Record> featuring = new LinkedList<Record>();
 
 		String sql = "SELECT DISTINCT records.recordnumber from records,track,personnel WHERE records.recordnumber = track.recordnumber AND track.trackrefnumber = personnel.tracknumber AND personnel.artistnumber = ?";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setInt(1, artNumber);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next()) {
 			Record rec = getRecord(rs.getInt(1));
 			featuring.add(rec);
 		}
 
 		return featuring;
 	}
 
 	public Collection<Record> getRecordsWithTrack(String trackName)
 			throws SQLException {
 		List<Record> featuring = new LinkedList<Record>();
 
 		String sql = "SELECT DISTINCT records.recordnumber from records,track WHERE records.recordnumber = track.recordnumber AND track.trackname = ?";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setString(1, trackName);
 		ResultSet rs = ps.executeQuery();
 		while (rs.next()) {
 			Record rec = getRecord(rs.getInt(1));
 			featuring.add(rec);
 		}
 
 		return featuring;
 	}
 
 	public Collection<String> getRecordTitles() throws SQLException {
 		// Use a tree set to keep things in order
 		Set<String> titleSet = new TreeSet<String>();
 
 		// Collect the titles
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT Title FROM Records");
 		ResultSet rs = s.executeQuery();
 		while (rs.next())
 			titleSet.add(rs.getString(1));
 		rs.close();
 		s.close();
 
 		// Return the collection
 		return titleSet;
 	}
 
 	public Record getSingleRecord(int recNumber) throws SQLException,
 			ParseException {
 		long sTime = System.currentTimeMillis();
 
 		// Run the query
 		PreparedStatement s = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"Select Title, BoughtDate, Notes, ReleaseYear, Format, CategoryName,ReleaseMonth,ReleaseType,Author, Owner, purchase_price,shelfpos FROM Records, Categories WHERE Categories.CategoryNumber = Records.Category  AND RecordNumber = ?");
 		s.setInt(1, recNumber);
 		ResultSet rs = s.executeQuery();
 
 		Record currRec;
 
 		// Move the pointer on
 		if (rs.next()) {
 
 			String title = rs.getString(1);
 			Calendar boughtDate = Calendar.getInstance();
 			boughtDate.setTimeInMillis(rs.getDate(2).getTime());
 			int format = rs.getInt(5);
 			String notes = rs.getString(3);
 			int year = rs.getInt(4);
 			String category = rs.getString(6);
 			int month = rs.getInt(7);
 			int type = rs.getInt(8);
 			String aut = rs.getString(9);
 			int own = rs.getInt(10);
 			double price = rs.getDouble(11);
 			int shelfpos = rs.getInt(12);
 
 			currRec = new Record(title, GetFormats.create().getFormat(format),
 					boughtDate, shelfpos);
 			currRec.setNumber(recNumber);
 			currRec.setNotes(notes);
 			currRec.setYear(year);
 			currRec.setReleaseMonth(month);
 			currRec.setReleaseType(type);
 			currRec.setAuthor(aut);
 			currRec.setOwner(own);
 			currRec.setPrice(price);
 
 			currRec.setCategory(GetCategories.build().getCategory(category));
 
 			// Return this record
 			return currRec;
 		} else
 			return null;
 
 	}
 
 	public Set<Track> getTracks(final int recNumber) throws SQLException {
 		Set<Track> retSet = new TreeSet<Track>();
 
 		// First Build the bare track details
 		PreparedStatement s = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT TrackRefNumber, TrackName, Length, TrackNumber FROM Track  WHERE RecordNumber = ? ORDER BY TrackNumber");
 		s.setInt(1, recNumber);
 		ResultSet rs = s.executeQuery();
 
 		// Naive approach to check for spped
 		Track currTrack = null;
 		while (rs.next()) {
 			int trckNum = rs.getInt(4);
 
 			// Create new track
 			String name = rs.getString(2);
 			if (name == null)
 				name = "";
 			int len = rs.getInt(3);
 			int refNum = rs.getInt(1);
 
 			// currTrack = new Track(name, len, getLineUps(refNum),
 			// getPersonnel(refNum), trckNum, refNum);
 			currTrack = new Track(name, len, getLineUps(refNum),
 					getPersonnel(refNum), trckNum, refNum);
 			retSet.add(currTrack);
 		}
 		rs.close();
 		s.close();
 
 		return retSet;
 	}
 
 	public Collection<String> getTrackTitles() throws SQLException {
 		List<String> lis = new LinkedList<String>();
 
 		// Set the parameter
 		PreparedStatement s = Connect.getConnection().getPreparedStatement(
 				"SELECT DISTINCT TrackName FROM Track");
 		ResultSet rs = s.executeQuery();
 
 		while (rs.next())
 			lis.add(rs.getString(1));
 
 		rs.close();
 		s.close();
 
 		return lis;
 
 	}
 
 	public void saveCompilers(Record record) throws SQLException {
 		// Delete the current compilers
 		String delSQL = "DELETE FROM compiler WHERE record_id = ?";
 		PreparedStatement dps = Connect.getConnection().getPreparedStatement(
 				delSQL);
 		dps.setInt(1, record.getNumber());
 		dps.execute();
 
 		// Add the current compilers
 		String addSQL = "INSERT INTO compiler (record_id, artist_id) VALUES (?,?)";
 		PreparedStatement aps = Connect.getConnection().getPreparedStatement(
 				addSQL);
 
 		// Ensure the artists are added
 		GetArtists.create().addArtists(record.getCompilers());
 
 		// Add the compiler details
 		for (Artist compiler : record.getCompilers()) {
 			aps.clearParameters();
 			aps.setInt(1, record.getNumber());
 			aps.setInt(2, compiler.getId());
 			aps.addBatch();
 		}
 
 		aps.executeBatch();
 	}
 
 	public void updateRecord(Record in) throws SQLException {
 		// First get the format number
 		int formatNumber = in.getFormat().save();
 
 		// Get the new category number
 		int catNum = in.getCategory().save();
 
 		// Add the record itself
 		updateRecord.setString(1, in.getTitle());
 		updateRecord.setDate(2, new java.sql.Date(in.getDate().getTime()
 				.getTime()));
 		updateRecord.setInt(3, formatNumber);
 		updateRecord.setString(4, in.getNotes());
 		updateRecord.setInt(5, in.getReleaseYear());
 		updateRecord.setInt(6, catNum);
 		updateRecord.setString(7, in.getAuthor());
 		updateRecord.setInt(8, in.getReleaseMonth());
 		updateRecord.setInt(9, in.getReleaseType());
 		updateRecord.setInt(10, in.getOwner());
 		updateRecord.setDouble(11, in.getPrice());
 		updateRecord.setInt(12, in.getShelfPos());
 		updateRecord.setInt(13, in.getNumber());
 
 		System.err.println(updateRecord);
 
 		updateRecord.execute();
 		int recordNumber = in.getNumber();
 
 		// Delete the label numbers
 		PreparedStatement ps = Connect.getConnection().getPreparedStatement(
 				"DELETE FROM LabelSet WHERE RecordNumber = ?");
 		ps.setInt(1, recordNumber);
 		ps.execute();
 
 		// Get the label numbers
 		int[] labNums = new int[in.getLabels().size()];
 		int labPointer = 0;
 		for (Label lab : in.getLabels())
 			labNums[labPointer++] = lab.save();
 
 		for (int labNum : labNums) {
 			// Add the numbers to the label set
 			PreparedStatement lps = Connect
 					.getConnection()
 					.getPreparedStatement(
 							"INSERT INTO LabelSet (RecordNumber,LabelNumber) VALUES (?,?)");
 			lps.setInt(1, recordNumber);
 			lps.setInt(2, labNum);
 			lps.execute();
 		}
 
 		// Delete the catalogue numbers
 		PreparedStatement lps = Connect.getConnection().getPreparedStatement(
 				"DELETE FROM CatNoSet WHERE RecordNumber = ?");
 		lps.setInt(1, recordNumber);
 		lps.execute();
 
 		// Add the catalogue numbers
 		Iterator<String> cIt = in.getCatNos().iterator();
 		while (cIt.hasNext()) {
 			String catNo = cIt.next();
 			PreparedStatement llps = Connect
 					.getConnection()
 					.getPreparedStatement(
 							"INSERT INTO CatNoSet (RecordNumber,CatNo) VALUES (?,?)");
 			llps.setInt(1, recordNumber);
 			llps.setString(2, catNo);
 			llps.execute();
 		}
 
 		// Deal with the tracks
 		for (Track t : in.getTracks())
 			t.save(in.getNumber());
 
 		// Delete any miscreant tracks
 		PreparedStatement dps = Connect.getConnection().getPreparedStatement(
 				"DELETE FROM Track WHERE recordnumber = ? AND tracknumber > ?");
 		dps.setInt(1, in.getNumber());
 		dps.setInt(2, in.getTracks().size());
 		dps.execute();
 
 	}
 
 	public void updateTrack(int recordNumber, Track newTrack)
 			throws SQLException {
 		// SAFE to assume that this track will exist permantly - so set the
 		// update parameters
 		updateTrack.setString(1, newTrack.getTitle());
 		updateTrack.setInt(2, newTrack.getLengthInSeconds());
 		updateTrack.setInt(3, recordNumber);
 		updateTrack.setInt(4, newTrack.getTrackNumber());
 
 		// Run the update
 		updateTrack.execute();
 
 		// Now get the track reference number
 		PreparedStatement s = Connect
 				.getConnection()
 				.getPreparedStatement(
 						"SELECT TrackRefNumber FROM Track WHERE RecordNumber = ? AND TrackNumber = ?");
 		s.setInt(1, recordNumber);
 		s.setInt(2, newTrack.getTrackNumber());
 		ResultSet rs = s.executeQuery();
 
 		// Move on the result set and collect the reference number
 		rs.next();
 		int refNum = rs.getInt(1);
 
 		rs.close();
 		s.close();
 
 		// Now update the groops and personnel - just delete these
 		PreparedStatement pps = Connect.getConnection().getPreparedStatement(
 				"DELETE FROM Personnel WHERE TrackNumber = ?");
 		pps.setInt(1, refNum);
 		pps.execute();
 		PreparedStatement lps = Connect.getConnection().getPreparedStatement(
 				"DELETE FROM LineUpSet WHERE TrackNumber = ?");
 		lps.setInt(1, refNum);
 		lps.execute();
 
 		// Now add the new data
 		addGroopsAndPersonnel(refNum, newTrack);
 
 	}
 
 }
