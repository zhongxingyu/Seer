 package movieRatings;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import neustore.base.*;
 
 /**
  * The StructureIndex is a heap file storing information about individual elements in an
  * XML document in the form of StudentIndexRecords. This file is indexed by ElementIndex
  * and references XMLContent and ElementIndex. 
  * @see MovieID_RatingsPage 
  * @author Donghui Zhang <donghui@ccs.neu.edu>
  * @modified Matthew Robertson <mlrobert@calpoly.edu>
  */
 
 /*
  * This is a comment
  */
 public class MovieID_Ratings extends DBIndex {
 	/**
 	 * maximum number of records in a page
 	 */
 	private int pageCapacity;
 	/**
 	 * pageID of the last page
 	 */
 	private int lastPageID;
 	/**
 	 * the last page
 	 */
 	private MovieID_RatingsPage lastPage;
 	
 	public MovieID_Ratings(DBBuffer _buffer, String filename, int isCreate) throws IOException {
 		super(_buffer, filename, isCreate);
 		pageCapacity = (pageSize-8) / 12; // header of 8 bytes, record of 12 bytes
 		// pageCapacity = 100; // very low and nonexact but good
 		if(isCreate == 1)
 			lastPage = new MovieID_RatingsPage(pageSize);
 		else
 			lastPage = myReadPage(lastPageID);
 	}
 	
 	protected void initIndexHead() {
 		lastPageID = allocate();
 	}
 	
 	/**
      * Reads a page from buffer and parse it if not parsed already.
 	 * 
 	 * @param   pageID
 	 * @return  the parsed page
 	 * @throws IOException
 	 */
 	protected MovieID_RatingsPage myReadPage( int pageID ) throws IOException {
 		MovieID_RatingsPage page;
 		DBBufferReturnElement ret = buffer.readPage(file, pageID);
 		if ( ret.parsed ) {
 			page = (MovieID_RatingsPage)ret.object;
 		}
 		else {
 			page = new MovieID_RatingsPage(pageSize);
 			page.read((byte[])ret.object);
 		}
 		return page;
 	}
 	
 	protected void readIndexHead(byte[] head) {
 		ByteArray ba = new ByteArray(head, ByteArray.READ);
 		try {
 			lastPageID = ba.readInt();
 		} catch (IOException e) {}
 	}
 	
 	protected void writeIndexHead (byte[] head) {
 		ByteArray ba = new ByteArray(head, ByteArray.WRITE);
 		try {
 			ba.writeInt(lastPageID);
 		} catch (IOException e) {}
 	}
 	
 	/**
 	 * Appends a new MovieRating to the end of the file.
 	 * @param key  the SIR to be appended
 	 * @return The page on which the ratings for the inserted movie begin.
 	 * @throws IOException
 	 */
 	public int insertEntry (MovieRatings key) throws IOException {
 		
 		/* we're often going to have to split records across more than one page; that's probably something
 		 * we need to handle right here */
 		while(key.getUserRatings().size() > 0) {
 			lastPageID = allocate();
 			lastPage = new MovieID_RatingsPage(pageSize);
 			lastPage.insert(key, pageCapacity); /* insert max # of records that will fit on one page */
 			buffer.writePage(file, lastPageID, lastPage);
 		}
 		return lastPageID;
 	}
 	
 	public ArrayList<UserRating> getRatingsById (int TargetNodeId)
 	{
 		ArrayList<UserRating> returnRecord = new ArrayList<UserRating>(), ratingsToAdd;
 		try {
 			for ( int currentPageID=1; currentPageID<=lastPageID; currentPageID++ ) {
 				MovieID_RatingsPage currentPage = myReadPage(currentPageID);
 				ratingsToAdd = currentPage.getRatingsById(TargetNodeId);
 				/* if(ratingsToAdd != null)
 					System.err.println(ratingsToAdd); */
 				if(returnRecord.size() > 0 && ratingsToAdd == null)
 					return returnRecord;
 				else if(ratingsToAdd != null)
 					returnRecord.addAll(ratingsToAdd);
 				buffer.writePage(file, currentPageID, currentPage); // why is this being done on a read? to preserve consistency after improper shutdown? leaving it for now but only pending investigation	
 			}
 		} catch(IOException e) {
 			System.err.println("IOException in getRecordById: " + e.toString());
 			System.exit(1);
 		}
 		
 		if(returnRecord.size() > 0)
 			return returnRecord;
		System.out.println("shouldn't get here unless the movie isn't in the db: " + TargetNodeId);
 		return null;
 	}
 	
     public int numPages() {
     	return lastPageID;
     }
 }
