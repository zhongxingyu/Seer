 package Vergleiche;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import API.GetRevision;
 import API.WikiBot;
 import SQL.Mysql_connect;
 
 /**
  * DB Provides the necessary data to run the analyzation process. It imports
  * data from the Databank and insert them to files.
  * 
  * @author Shimal
  * 
  */
 public class DB {
 
 	private ArrayList<TempArticle> articlesInDB = new ArrayList<TempArticle>();
 
 	/**
 	 * Konstruktor
 	 * 
 	 * @throws IOException
 	 */
 	public DB() throws IOException {
 		startDBOperations();
 	}
 
 	/**
 	 * Starts the necessary DB operations to run the analyzation process
 	 * 
 	 * @throws IOException
 	 */
 	private void startDBOperations() throws IOException {
 
 		// Delete file if it exists
 		deleteFile("ArticleTitles");
 		// Get Article titles from DB
 		getDataFromDB("Select artikel from revision group by artikel",
 				"ArticleTitles");
 
 		// Add articles titles to the article
 		for (int i = 0; i < getDateExtractedFromFile("res/ArticleTitles.txt")
				.size(); i++) {
 			String title = getDateExtractedFromFile("res/ArticleTitles.txt")
 					.get(i);
 			articlesInDB.add(new TempArticle(title));
 		}
 
 		// Get revision IDs of each article from DB
 		for (int i = 0; i < articlesInDB.size(); i++) {
 			String tmp = articlesInDB.get(i).getTitle().replaceAll("  ", "");
 			// tmp.replaceAll(" ", "");
 			// Delete file if it exists
 			deleteFile("RevisionsOf" + tmp);
 			getDataFromDB("Select RevisionID from revision where artikel=\""
 					+ tmp + "\"", "RevisionsOf" + tmp);
 		}
 
 		// Get users of each article from DB
 		for (int i = 0; i < articlesInDB.size(); i++) {
 			String tmp = articlesInDB.get(i).getTitle().replaceAll("  ", "");
 			// Delete file if it exists
 			deleteFile("UsersOf" + tmp);
 			getDataFromDB("Select User from revision where artikel=\""
 					+ articlesInDB.get(i).getTitle() + "\"", "UsersOf" + tmp);
 		}
 
 		// Add revisions and users to the specific article
 		for (int i = 0; i < articlesInDB.size(); i++) {
 			String tmp = articlesInDB.get(i).getTitle().replaceAll(" ", "");
 			articlesInDB.get(i).setRevisions(
 					getDateExtractedFromFile("res/RevisionsOf" + tmp + ".txt",
 							"res/UsersOf" + tmp + ".txt"));
 		}
 
		// Delete file if it exists
		deleteFile("Categories");
 		// Add category to the specific article
 		for (int i = 0; i < articlesInDB.size(); i++) {
			getDataFromDB("Select Kategorie from kategorie where artikel=\""
					+ articlesInDB.get(i).getTitle() + "\"", "Categories");
		}
		
		for (int i = 0; i < articlesInDB.size(); i++) {
 			articlesInDB.get(i).setCategory(
 					getDateExtractedFromFile("res/Categories.txt").get(i));
 		}
 
 		// Importing the content of the revisions from Wikipedia
 		for (int i = 0; i < articlesInDB.size(); i++) {
			for (int j = 0; j < (articlesInDB.get(i).getRevisions().size() / 20); j++) {
 				WikiBot wb = new WikiBot("wissensmanagement", "asdasd");
 				int revID = Integer.parseInt(articlesInDB.get(i).getRevisions()
 						.get(j).getID().replaceAll("  ", ""));
 				GetRevision rev = new GetRevision(wb.getWikiBot(), revID);
 				// Delete file if it exists
 				deleteFile(articlesInDB.get(i).getTitle().replaceAll("  ", "")
 						+ "_Revision_" + revID);
 				writeInFileSQLStatement("res/"
 						+ articlesInDB.get(i).getTitle().replaceAll("  ", "")
 						+ "_Revision_" + revID + ".txt", rev.getRevision()
 						.getContent());
 			}
 		}
 	}
 
 	/**
 	 * Extracts text from file and add it to a list
 	 * 
 	 * @param path
 	 *            of the file
 	 * @return a list which contains the content of the file
 	 * @throws IOException
 	 */
 	private ArrayList<String> getDateExtractedFromFile(String path)
 			throws IOException {
 		ArrayList<String> list = new ArrayList<String>();
 		for (int i = 2; i < readFile(new File(path)).size(); i++) {
 			list.add(readFile(new File(path)).get(i));
 		}
 		return list;
 	}
 
 	/**
 	 * Extracts text from file and add it to a list
 	 * 
 	 * @param pathToRevisionIDFile
 	 *            the path to the file that contains the revisions of an article
 	 * @param pathToAuthorFile
 	 *            the path to the file that contains the users of an article
 	 * @return a list (Typ: TempRevision) which contains the content of the file
 	 * @throws IOException
 	 */
 	private ArrayList<TempRevision> getDateExtractedFromFile(
 			String pathToRevisionIDFile, String pathToAuthorFile)
 			throws IOException {
 		ArrayList<TempRevision> revisions = new ArrayList<TempRevision>();
 		ArrayList<String> ids = readFile(new File(pathToRevisionIDFile));
 		ArrayList<String> authors = readFile(new File(pathToAuthorFile));
 		for (int i = 2; i < ids.size(); i++) {
 			revisions.add(new TempRevision(authors.get(i), ids.get(i)));
 		}
 		return revisions;
 	}
 
 	/**
 	 * Reads the content of a file and put it to a list. Each line will be added
 	 * to a field of the list
 	 * 
 	 * @param file
 	 *            the file that have to be read
 	 * @return a list with the content of file
 	 * @throws IOException
 	 */
 	private ArrayList<String> readFile(File file) throws IOException {
 		@SuppressWarnings("resource")
 		BufferedReader input = new BufferedReader(
 				new FileReader(file.getPath()));
 		String string = input.readLine();
 		ArrayList<String> content = new ArrayList<String>();
 		while ((string != null)) {
 			if (!string.equals(""))
 				content.add(string);
 			string = input.readLine();
 		}
 		return content;
 	}
 
 	/**
 	 * Writes a string in a file
 	 * 
 	 * @param fn
 	 *            is the name of the file
 	 * @param text
 	 *            is the content. This will be added to the file
 	 */
 	public void writeInFileSQLStatement(String fn, String text) {
 		File f = new File(fn);
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
 			bw.write(text);
 			bw.flush();
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Imports data from the DB. The methode contains the login data for the
 	 * server. The user have to define the SQL statement and the filename.
 	 * 
 	 * @param sqlStatement
 	 *            defines the SQL statement
 	 * @param filename
 	 *            the name of the file in which the data have to be saved
 	 */
 	private void getDataFromDB(String sqlStatement, String filename) {
 		String user = "y9r106037";
 		String pass = "basilius789063";
 		String host = "134.0.26.187:3306/";
 		String dbname = "y9r106037_usr_web27_2";
 		Mysql_connect con = new Mysql_connect(host, dbname, user, pass);
 		String content = con.getSelectStatement(sqlStatement);
 		writeInFileSQLStatement("res/" + filename + ".txt", content);
 	}
 
 	/**
 	 * Get temporary articles
 	 * 
 	 * @return
 	 */
 	public ArrayList<TempArticle> getArticlesInDB() {
 		return this.articlesInDB;
 	}
 
 	/**
 	 * Delete files
 	 * 
 	 * @param filename
 	 */
 	private void deleteFile(String filename) {
 		File file = new File("res/" + filename + ".txt");
 		if (file.exists())
 			file.delete();
 	}
 
 }
