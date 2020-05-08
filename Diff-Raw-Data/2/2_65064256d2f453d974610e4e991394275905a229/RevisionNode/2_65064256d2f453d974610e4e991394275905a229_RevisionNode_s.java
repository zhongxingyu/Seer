 import java.util.Iterator;
 import java.util.LinkedList;
 
 
 public class RevisionNode {
 	
 	private String date;						//the date the revision was done
 	private String revision;					//the number of the revision in String form
 	private int numberOfRelevants;				//how many relevant files are in this revision
 	private double rating;						//Relevance rating of the revision, based on algorithm #1
 	private LinkedList<String> relevantFiles;	//list of the queried files present in this revision
 	private int totalQuery;						//total number of queried files
 	private int totalChanges;					//total number of files affected by the this revision
 	private String ratingComment;				//the comment attached to the rating, based on various factors
 	private String user;						//user who added the revision
 	
 	/**
 	 * Constructor. Allows the user to also enter the date and number of relevant files 
 	 * @param dat date of revision
 	 * @param rev revision number 
 	 * @param total how many changed files there are
 	 * @param query how many files were entered on the command line
 	 */
 	public RevisionNode(String dat, String rev, int query, String User) {
 		date = dat;
 		revision = rev;
 		totalChanges = 0;
 		rating = 0;
 		relevantFiles = new LinkedList<String>();
 		totalQuery = query;
 		user = User;
 	}
 
 	/**
 	 * sets the comment specifying which case the rating was calculated in
 	 * @param relevant how many of the files are relevant to the query
 	 * @param irrelevant how many files are not in the query search
 	 * @param totalChanged how many changed files there are in total in the revision
 	 * @param amountOfRelevantFiles maximum possible value for relevant
 	 */
 	private void setRatingComment(int relevant, int irrelevant, int totalChanged, int amountOfRelevantFiles) {
 		if (relevant == 0) {
 			ratingComment = "Indeterminate, cannot find clear data here";
 		}
 		else if (relevant == totalChanged && relevant == amountOfRelevantFiles){
 			ratingComment = "Very Relevant, contains only queried files";
 		}
 		else if (relevant == totalChanged && relevant < amountOfRelevantFiles) {
 			ratingComment = "A Pure Subset of some of the queried files";
 		}
 		else if (relevant == amountOfRelevantFiles && relevant != totalChanged){
 			ratingComment = "Impure Superset of the queried files found";
 		}
 		
 		else ratingComment = "Subset of Relevants mixed with Irrelevants";
 	}
 	
 	public void setNumberOfRelevants(int rel){
 		numberOfRelevants = rel; 
 	}
 
 	/**
 	 * setter. allows the  use to set how many files where changed in this revision 
 	 * @param totalChanges the total number of changed files
 	 */
 	public void setTotalChanges(int totalChanges) {
 		this.totalChanges = totalChanges;
 	}
 	
 	/**
 	 * set the date that the revision was done to the repository
 	 * @param Date the new date
 	 */
 	public void setDate(String Date) {
 		date = Date;
 	}
 
 	/**
 	 * sets the revisions relevance rating on a scale of 0-1 where 1 is the highest and 0 is the lowest.
 	 * It also sets the corresponding rating comment as well
 	 */
 	public void setRating() {
 		int i = totalChanges-numberOfRelevants;
 		if (i < 0){
 			i = 0;
 		}
 		rating = calculateRating(numberOfRelevants,i,totalChanges,totalQuery);
 		setRatingComment(numberOfRelevants,i,totalChanges,totalQuery);
 	}
 	
 	/**
 	 * gets the rating of the revision and uses the set rating to ensure an up-to-date representation
 	 * @return the relevance rating of this revision
 	 */
 	public double getRating() {
 		setRating();
 		return rating;
 	}
 
 	/**
 	 * gets the list of relevant files related to this revision
 	 * @return the list of relevant files
 	 */
 	public LinkedList<String> getRelevantFiles() {
 		return relevantFiles;
 	}
 
 	/**
 	 * gets how many files were requested by the user
 	 * @return number of files that the user entered on the command line
 	 */
 	public int getTotalQuery() {
 		return totalQuery;
 	}
 	
 	/**
 	 * gets the revision number
 	 * @return the revision this node represents
 	 */
 	public String getRevision() {
 		return revision;
 	}
 	
 	/**
 	 * gets the user who applied this revision
 	 * @return the user
 	 */
 	public String getUser() {
 		return user;
 	}
 	
 	/**
 	 * gets the rating comment related to the relevance rating, showing some of the logic behind
 	 * the rating system.
 	 * @return the rating comment of this revision
 	 */
 	public String getRatingComment() {
 		return ratingComment;
 	}
 
 	/**
 	 * gets the total number of changed files from this revision 
 	 * @return total number of changed files
 	 */
 	public int getTotalChanges() {
 		return totalChanges;
 	}
 
 	/**
 	 * used to get the date of revision
 	 * @return the date the revision was one to the repository 
 	 */
 	public String getDate() {
 		return date;
 	}
 
 	/**
 	 * get how many of the queried files are present in this revision
 	 * @return the number of contained relevant files
 	 */
 	public int getNumberOfRelevants() {
 		return numberOfRelevants;
 	}
 
 	/**
 	 * String representation of the node
 	 * @return all data in String form
 	 */
 	public String toString(){
 		String out = "";
 		String files = "";
 		Iterator<String> listIt = this.getRelevantFiles().iterator();
 		while(listIt.hasNext()){
 			String nextFile = listIt.next();
			nextFile = nextFile.substring(nextFile.lastIndexOf('/'));
 			if (listIt.hasNext()){
 				files += nextFile+", ";
 			}
 			else {
 				files += nextFile;
 			}
 		}
 		String use = user;
 		if (use.length() < 8){
 			use += "\t";
 		}
 		//information for this rounding found on http://www.java-forums.org/advanced-java/4130-rounding-double-two-decimal-places.html
 		double rat = this.getRating()*100000;
 		rat = Math.round(rat);
 		rat /= 100000;
 		out = revision+"\t"+use+"\t"+date+"\t"+Integer.toString(numberOfRelevants)+"/"+Integer.toString(totalQuery)+" relevant files\t"+Integer.toString(totalChanges)+"\t"+rat+"\t\t"+ratingComment+"\t"+files;
 		return out;
 	}
 	
 	/**
 	 * sets the relevance rating of the revision based on several cases as outlined in algorithm #1
 	 * @param relevant how many of the files are relevant to the query
 	 * @param irrelevant how many files are not in the query search
 	 * @param totalChanged how many changed files there are in total in the revision
 	 * @param amountOfRelevantFiles maximum possible value for relevant
 	 */
 	private double calculateRating(int relevant, int irrelevant, int totalChanged, int amountOfRelevantFiles) {
 		if (relevant == 0) {
 			return 0;
 		}
 		else if (relevant == totalChanged && relevant == amountOfRelevantFiles){
 			return 1;
 		}
 		else if (relevant == totalChanged && relevant < amountOfRelevantFiles) {
 			return ((double)totalChanged/amountOfRelevantFiles);
 		}
 		else if (relevant == amountOfRelevantFiles && relevant != totalChanged){
 			return ((double)relevant/totalChanged);
 		}
 		
 		else return (double)relevant/(amountOfRelevantFiles + (double)irrelevant/totalChanged);
 	}
 	
 	/**
 	 * places a new files in the list of present and relevant files
 	 * @param file the queried file found to be in this revision
 	 */
 	public void newRelevantFile(String file){
 		Iterator<String> listCheck = relevantFiles.iterator();
 		int n = 1;
 		while (listCheck.hasNext()) {
 			String current = listCheck.next();
 			if (!current.split(" ")[0].equals((file.split(" ")[0]))) {
 				n = 1;
 			}
 			else {
 				n = 0;
 				break;
 			}
 		}
 		if (n == 1){
 			relevantFiles.addLast(file);
 			numberOfRelevants++;
 		}
 	}
 
 }
