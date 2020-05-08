 package svd;
 
 import movieRatings.EfficientMovieRatings;
 import movieRatings.MovieID_Ratings;
 import movieRatings.UserRating;
 import neustore.base.LRUBuffer;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import cern.colt.matrix.impl.*;
 import cern.colt.matrix.linalg.*;
 
 public class SVDBuilder {
 
 	/*
	 * rows = movieID
	 * cols = userID
 	 * 
 	 * matrix=> rows X cols = m X n = movieID X userID
 	 */
 	private int rows = 480000;
 	private int cols = 17770;
 	private SingularValueDecomposition svdTab;
 		
 	//public SVDBuilder(String neustoreTrainingSetFile) {
 	public SVDBuilder(File index, String output) throws Exception {
 		
 		SparseDoubleMatrix2D svdbuild = new SparseDoubleMatrix2D(rows, cols);
 		
 		MovieID_Ratings theRatings = new MovieID_Ratings(new LRUBuffer(5, 4096), index.getAbsolutePath(), 0);
 		EfficientMovieRatings ratings;
 		
		for(int row=0; row<rows; ++row) {
			ratings = theRatings.getRatingsById(row+1);
 			for(int x = 0; x < ratings.numRatingsStored; x++) {
				svdbuild.setQuick(row, ratings.UserID[x], ratings.Rating[x]);
 			}
 		}
 		
 		svdbuild.trimToSize();
 		
 		svdTab = new SingularValueDecomposition(svdbuild);
 		
 		try {
 			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(output));
 			os.writeObject(svdTab);
 			os.close();
 		}
 		catch (IOException e) {
 			System.out.println("Unable to open filename " + output + " :" + e.getMessage());
 		}
 	}
 }
