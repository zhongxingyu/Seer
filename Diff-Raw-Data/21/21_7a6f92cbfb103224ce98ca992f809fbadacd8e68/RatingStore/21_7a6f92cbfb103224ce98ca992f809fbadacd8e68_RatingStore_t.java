 package database;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import neustore.base.LRUBuffer;
 import MovieID_Ratings.MovieID_Ratings;
 import MovieID_Ratings.MovieRatings;
 
 public class RatingStore {
 	
 	private final File indexFile;
 	
 	public RatingStore(File indexFile) {
 		this.indexFile = indexFile;
 	}
 	
 	public File getIndexFile() {
 		return indexFile.getAbsoluteFile();
 	}
 	
 	/**
 	 * Create a new index by loading from a file, or, if a directory is given, from all suitable
 	 * files within the directory
 	 * 
 	 * @param ratingsFile
 	 */
 	public void createFromFile(File ratingsFile) throws FileNotFoundException {
 		loadFromFile(ratingsFile, true);
 	}
 	/**
 	 * Append to an existing by loading from a file, or, if a directory is given, from all suitable
 	 * files within the directory
 	 * 
 	 * @param ratingsFile
 	 */
 	public void appendFromFile(File ratingsDirectory) throws FileNotFoundException {
 		loadFromFile(ratingsDirectory, false);
 	}
 	
 	private void loadFromFile(File ratingsDirectory, boolean create) throws FileNotFoundException {
 		ArrayList<MovieRatings> movieRatingsList = new ArrayList<MovieRatings>();
 		
 		if(!ratingsDirectory.canRead()) {
 			System.err.println("Can't open file: " + ratingsDirectory);
 			throw new FileNotFoundException();
 		}
 		if(ratingsDirectory.isDirectory()) {
 			File[] movieFiles = ratingsDirectory.listFiles();
 			
 			for(File file: movieFiles) {
 				movieRatingsList.add(new MovieRatings(file));
 			}
 		} else {
 			movieRatingsList.add(new MovieRatings(ratingsDirectory));
 		}
 		
 		// Write the movie ratings into an index
 		MovieID_Ratings index = null;
 		try{
 			
 			index = new MovieID_Ratings(new LRUBuffer (5, 4096), indexFile.getAbsolutePath(), create? 1 : 0);
 			for(MovieRatings movieRatings: movieRatingsList) {
 				index.insertEntry(movieRatings);
 			}
 			index.close();
 		} catch(Exception e) {
 			// TODO: handle exceptions in a less retarded way.
 			System.err.println("Insert F##Xed up");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 }
