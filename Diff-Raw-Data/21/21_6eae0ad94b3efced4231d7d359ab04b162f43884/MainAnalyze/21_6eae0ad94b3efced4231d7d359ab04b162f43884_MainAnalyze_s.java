 package Vergleiche;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  * MainAnalyze is the main class for the starting the whole analyzation. It
  * starts the methods of the class DB and execute the analyzation of the
  * different revisions. Besides it provides additional methods for 
  * analyzing articles.
  * @author Shimal
  * 
  */
 public class MainAnalyze {
 	private ArrayList<Article> articles = new ArrayList<Article>();
 	private AnalyzeRevision analyze;
 
 	public MainAnalyze() {
 		main();
 	}
 
 	/**
 	 * The main methode to start the analyzation
 	 */
 	public void main() {
 		DB db;
 		try {
 			db = new DB();
 			ArrayList<TempArticle> articlesInDB = db.getArticlesInDB();
 			for (int i = 0; i < articlesInDB.size(); i++) {
 				String cat = articlesInDB.get(i).getCategory();
 				ArrayList<Revision> revisions = new ArrayList<Revision>();
 				// For efficiency we reduce the size of revisions
				for (int j = 0; j < (articlesInDB.get(i).getRevisions().size() / 50); j++) {
 					User user = new User(articlesInDB.get(i).getRevisions()
 							.get(j).getAuthor(),
 							articlesInDB.get(i).getTitle(), articlesInDB.get(i)
 									.getRevisions().get(j).getID());
 					File file = new File("res/"
 							+ articlesInDB.get(i).getTitle()
 									.replaceAll(" ", "")
 							+ "_Revision_"
 							+ articlesInDB.get(i).getRevisions().get(j).getID()
 									.replaceAll(" ", "") + ".txt");
 					revisions.add(new Revision(user, file));
 				}
 				articles.add(new Article(articlesInDB.get(i).getTitle(), cat,
 						revisions));
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		for (int m = 0; m < articles.size(); m++) {
 			for (int i = 1; i < articles.get(m).getRevisions().size(); i++) {
 				try {
 					analyze = new AnalyzeRevision(articles.get(m)
 							.getRevisions().get(i - 1), articles.get(m)
 							.getRevisions().get(i));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get all articles
 	 * 
 	 * @param articleName
 	 * @return
 	 */
 	public ArrayList<Article> getAllArticles() {
 		return this.articles;
 	}
 
 	/**
 	 * Get all authors of all articles
 	 * 
 	 * @return
 	 */
 	public ArrayList<User> getAllAuthorsOfAllArtciles() {
 		ArrayList<User> list = new ArrayList<User>();
 		for (int i = 0; i < articles.size(); i++) {
 			for (int j = 0; j < articles.get(i).getRevisions().size(); j++) {
 				list.add(new User(articles.get(i).getRevisions().get(j)
 						.getAuthor().getName(), articles.get(i).getTitle(),
 						articles.get(i).getRevisions().get(j).getID()));
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Get all changes of an article
 	 * 
 	 * @param articleName
 	 *            article name
 	 * @return a list of types of change
 	 */
 	public ArrayList<TypeOfChange> getAllChangesOfArticle(String articleName) {
 		ArrayList<TypeOfChange> changes = new ArrayList<TypeOfChange>();
 		for (int i = 0; i < articles.size(); i++) {
 			if (articles.get(i).getTitle().equals(articleName))
 				for (int j = 0; j < articles.get(i).getRevisions().size(); j++)
 					changes.add(articles.get(i).getRevisions().get(j)
 							.getTypeOfChange());
 		}
 		return changes;
 	}
 
 	/**
 	 * Get all changes of an article
 	 * 
 	 * @param articleName
 	 *            article name
 	 * @return a list of types of change
 	 */
 	public ArrayList<TypeOfChange> getAllChangesOfAllArticles() {
 		ArrayList<TypeOfChange> changes = new ArrayList<TypeOfChange>();
 		for (int i = 0; i < articles.size(); i++)
 			for (int j = 0; j < articles.get(i).getRevisions().size(); j++)
 				changes.add(articles.get(i).getRevisions().get(j)
 						.getTypeOfChange());
 		ArrayList<TypeOfChange> list = new ArrayList<TypeOfChange>(
 				new HashSet<TypeOfChange>(changes));
 		return list;
 	}
 
 	/**
 	 * Get articles edited by a user
 	 * @param author
 	 * @return a list of articles edited by a user
 	 */
 	public ArrayList<Article> getAllChangesOfArticleByUser(String author) {
 		ArrayList<Article> article = new ArrayList<Article>();
 		for (int i = 0; i < articles.size(); i++)
 			for (int j = 0; j < articles.get(i).getRevisions().size(); j++)
 				if (articles.get(i).getRevisions().get(j).getAuthor().getName()
 						.equals(author))
 					article.add(articles.get(i));
 		return article;
 	}
 
 }
