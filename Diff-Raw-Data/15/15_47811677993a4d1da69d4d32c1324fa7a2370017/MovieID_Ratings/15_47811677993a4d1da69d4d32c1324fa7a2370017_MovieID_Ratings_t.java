 package movieRatings;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 import neustore.base.*;
 
 /**
  * The StructureIndex is a heap file storing information about individual elements in an
  * XML document in the form of StudentIndexRecords. This file is indexed by ElementIndex
  * and references XMLContent and ElementIndex. 
  * @see MovieID_RatingsPage 
  * @author Donghui Zhang <donghui@ccs.neu.edu>
  * @modified Matthew Robertson <mlrobert@calpoly.edu>
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
 	
 	private ArrayList<IDLookup> IDLookups;
 	
 	public MovieID_Ratings(DBBuffer _buffer, String filename, int isCreate) throws IOException {
 		super(_buffer, filename, isCreate);
 		// pageCapacity = (pageSize-8) / 12; // header of 8 bytes, record of 12 bytes (4 user, 4 rating, 4 date)
 		pageCapacity = (pageSize-8) / 5; // header of 8 bytes, record of 5 bytes (4 user, 1 rating)
 		// pageCapacity = 100; // very low and nonexact but it runs
 		
 		if(isCreate == 1) {
 			IDLookups = new ArrayList<IDLookup>();
 			lastPage = new MovieID_RatingsPage(pageSize);
 		}
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
 	
 	
 	/*** Start Index Head Methods ***/
 	/* Our index currently stores lastPageID and the total number of movies stored in our DB; the index itself
 	 * is stored in a separate flat text file (the path to the repository with "_index" suffixed) as Neustore
 	 * is hardcoded to limit the index header to a single page. (non-Javadoc)
 	 * @see neustore.base.DBIndex#readIndexHead(byte[])
 	 */
 	protected void readIndexHead(byte[] head) {
 		ByteArray ba = new ByteArray(head, ByteArray.READ);
 		IDLookups = new ArrayList<IDLookup>();
 		try {
 			lastPageID = ba.readInt();
 			ba.readInt();
 			ba.readInt();
 			int numMovies = ba.readInt();
 			Scanner sc = new Scanner(new File(inputFilename + "_index"));
 			for(int currentMovie = 0; currentMovie < numMovies; currentMovie++)
 				IDLookups.add(new IDLookup(sc.nextInt(), sc.nextInt()));
 			/* for(IDLookup l : IDLookups)
 				System.out.println("read " + l.MovieID + " page " + l.PageID); */
 			sc.close();
 		} catch (IOException e) { System.err.println("IOException in readIndexHead: " + inputFilename); }
 		Collections.sort(IDLookups); /* theoretically unnecessary if we load the movies in ID order but I'm not chancing an error here */
 	}
 	
 	protected void writeIndexHead (byte[] head) {
 		ByteArray ba = new ByteArray(head, ByteArray.WRITE);
 		try {
 			ba.writeInt(lastPageID);
 			ba.writeInt(IDLookups.size());
 			BufferedWriter w = new BufferedWriter(new FileWriter(inputFilename + "_index"));
 			for(IDLookup i : IDLookups) {
 				w.write(i.MovieID + " " + i.PageID + "\n"); /* keeping this human-readable for now--one-time cost, no need to optimize */
 			}
 			w.flush();
 			w.close();
 		} catch (IOException e) {}
 	}
 	
 	/*** End Index Head Methods ***/
 	
 	/**
 	 * Appends a new MovieRating to the end of the file.
 	 * @param key  the SIR to be appended
 	 * @return The page on which the ratings for the inserted movie begin.
 	 * @throws IOException
 	 */
 	public int insertEntry (MovieRatings key) throws IOException {
 		
 		/* we're often going to have to split records across more than one page; that's probably something
 		 * we need to handle right here */
 
 		int firstPageID = -1; // used to track where the ratings for this movie begin
 		
 		while(key.getUserRatings().size() > 0) {
 			lastPageID = allocate();
 			if(firstPageID == -1)
 				firstPageID = lastPageID;
 			lastPage = new MovieID_RatingsPage(pageSize);
 			lastPage.insert(key, pageCapacity); /* insert max # of records that will fit on one page */
 			buffer.writePage(file, lastPageID, lastPage);
 		}
 		
 		IDLookups.add(new IDLookup(key.getMovieID(), firstPageID));
 		// System.out.println("Added new movie " + key.getMovieID() + " at page " + firstPageID);
 		
 		return lastPageID;
 	}
 	
 	public ArrayList<UserRating> getRatingsById (int TargetNodeId)
 	{
 		ArrayList<UserRating> returnRecord = new ArrayList<UserRating>(), ratingsToAdd;
 		
 		int startingPage = Collections.binarySearch(IDLookups, new IDLookup(TargetNodeId, 0));
 		if(startingPage < 0) /* Movie was not found in our database */
 			return null;
 		startingPage = IDLookups.get(startingPage).PageID;
 		try {
 			for ( int currentPageID=startingPage; currentPageID<=lastPageID; currentPageID++ ) {
 				/* check all of this logic after doing the indexing stuff to make sure it's still necessary */
 				MovieID_RatingsPage currentPage = myReadPage(currentPageID);
 				ratingsToAdd = currentPage.getRatingsById(TargetNodeId);
 				if(returnRecord.size() > 0 && ratingsToAdd == null)
 					return returnRecord;
 				else if(ratingsToAdd != null)
 					returnRecord.addAll(ratingsToAdd);
 			}
 		} catch(IOException e) {
 			System.err.println("IOException in getRecordById: " + e.toString());
 			System.exit(1);
 		}
 		
 		/* again, most of this check at the end should be obsoleted by the IDLookups array if all is functioning well */
 		// if(returnRecord.size() > 0)
 		return returnRecord;
 	}
 	
     public int numPages() {
     	return lastPageID;
     }
     
     private class IDLookup implements Comparable<IDLookup> {
     	public int MovieID, PageID;
     	
     	public IDLookup(int MovieID, int PageID) {
     		this.MovieID = MovieID;
     		this.PageID = PageID;
     	}
     	
     	public boolean equals(Object o) {
     		IDLookup l = (IDLookup) o;
     		return MovieID == l.MovieID;
     	}
     	
     	public int compareTo(IDLookup o) {
     		return this.MovieID - o.MovieID;
     	}
     }
 }
