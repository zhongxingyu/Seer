 package org.feedeo.clientcomm;
 
 import java.util.Map;
 
 import org.feedeo.Article;
 import org.feedeo.ArticleProperties;
 import org.feedeo.Directory;
 import org.feedeo.Feed;
 import org.feedeo.User;
 import org.feedeo.syndication.FeedReader;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 /**
  * This class handles the JSON requests built by the index.jsp page, and gives
  * appropriate replies in return.
  * 
  * @author Feedeo Team
  * 
  */
 public class FeedeoHandler {
 	private User user;
 
 	private enum TargetObject {
 		folder, article, preferences, feed
 	}
 
 	/**
 	 * Builds an instance of FeedeoHandler for the target client.
 	 * 
 	 * @param login
 	 */
 	public FeedeoHandler(String login) {
 		user = User.getUserByLogin(login);
 	}
 
 	private Session getSession() {
 		return user.getSession();
 	}
 
 	/**
 	 * The "basic" handler method: takes a JSON request object, and makes a JSON
 	 * response object.
 	 * 
 	 * @param request
 	 * @param response
 	 */
 	@SuppressWarnings("unchecked")
 	public void handle(Map<String, Object> request, Map<String, Object> response) {
 		// request may contain (in general):
 		// action = [delete, add, update, get, login, register, move...]
 		// object = [article, user, folder, preferences, feed...]
 		// and parameters relative to the action: id, target, value...
 		// the response object is the one that will be sent to the client.
 		String action = (String) request.get("action");
 		String object = (String) request.get("object");
 		if ("login".equals(action)) {
 			response.put("error", "Already logged");
 		} else {
 			try {
 				getSession().beginTransaction();
 				user = (User) getSession().merge(user);
 				TargetObject target = TargetObject.valueOf(object);
 				switch (target) {
 				case folder:
 					String folderIdString = (String) request.get("folderId");
 					if (folderIdString != null) {
 						Directory targetDirectory;
 						long folderId = Long.parseLong(folderIdString);
 						if (folderId == 0L) {
 							targetDirectory = user.getRootDirectory();
 						} else {
 							targetDirectory = (Directory) getSession().get(
 									Directory.class, folderId);
 						}
 						if (targetDirectory != null) {
 							if ("getChildren".equals(action)) {
 								response.put("children", (targetDirectory
 										.toMap(true)).get("children"));
 								response.put("success", true);
 							} else if ("getArticles".equals(action)) {
 								targetDirectory.updateArticles();
 								response.put("articles", (targetDirectory
 										.toMap(true)).get("articles"));
 								response.put("success", true);
 							} else if ("addFeed".equals(action)) {
 								String feedUrl = (String) request
 										.get("feedUrl");
 								Feed targetFeed = Feed.getFeedByUrl(feedUrl);
 								FeedReader.update(targetFeed);
 								targetDirectory.subscribeFeed(targetFeed);
 								response.put("success", true);
 							}
 						}
 					} else if ("add".equals(action)) {
 						Directory newDirectory = new Directory();
 						newDirectory.setTitle((String) request.get("name"));
 						folderIdString = (String) request.get("parentId");
 						if (folderIdString != null) {
 							long parentId = Long.parseLong(folderIdString);
 							Directory parentDirectory;
 							if (parentId == 0L) {
 								parentDirectory = user.getRootDirectory();
 							} else {
 								parentDirectory = (Directory) getSession().get(
 										Directory.class, parentId);
 							}
 							if (parentDirectory != null) {
 								parentDirectory.attachDirectory(newDirectory);
 								getSession().persist(newDirectory);
 								response.put("folder", newDirectory
 										.toMap(false));
 								response.put("success", true);
 
 							}
 						}
 					}
 					break;
 
 				case article:
 					String articleIdString = (String) request.get("articleId");
 					if (articleIdString != null) {
 						long articleId = Long.parseLong(articleIdString);
 						Article targetArticle = (Article) getSession().get(
 								Article.class, articleId);
 						if ("update".equals(action)) {
 							ArticleProperties properties = user
 									.getArticleProperties(targetArticle);
 							Map<String, Object> changes = (Map<String, Object>) request
 									.get("changes");
 							Boolean read = (Boolean) changes.get("read");
 							Boolean important = (Boolean) changes
 									.get("important");
							if (read != null) {
 								properties.setImportant(important);
 							}
 							if (read != null) {
 								properties.setAlreadyRead(read);
 							}
 							// "On devrait gerer l'echec de tous les changements d'etat"
 							// kezako ?
 							response.put("success", true);
 						}
 					}
 					break;
 				case preferences:
 					if ("get".equals(action)) {
 						response.put("success", true);
 						response.put("preferences", null);
 					}
 					break;
 				case feed:
 					if ("update".equals(action)) {
 						response.put("success", true);
 						response.put("preferences", null);
 					}
 					break;
 				}
 				getSession().getTransaction().commit();
 			} catch (Exception e) {
 				/*
 				 * Building a readable and complete error message
 				 */
 				StringBuilder errorMessage = new StringBuilder();
 				errorMessage.append("Java error: ").append(e.getMessage())
 						.append('\n');
 				response.put("success", false);
 
 				/*
 				 * Trying to roll back the transaction if active.
 				 */
 				if (getSession().getTransaction() != null
 						&& getSession().getTransaction().isActive()) {
 					try {
 						// Second try catch as the rollback could fail as well
 						getSession().getTransaction().rollback();
 						errorMessage
 								.append("Transaction Rollback: successful.\n");
 					} catch (HibernateException e1) {
 						errorMessage.append("Transaction Rollback: failed.\n");
 					}
 				} else {
 					errorMessage.append("Transaction Rollback: unnecessary.\n");
 				}
 				/*
 				 * Including stacktrace in the error message.
 				 */
 				StackTraceElement[] stackTrace = e.getStackTrace();
 				int i;
 				for (i = 0; i < stackTrace.length; i++) {
 					String cName = stackTrace[i].getClassName();
 					if (cName.contains("org.apache.jasper"))
 						break;
 					errorMessage.append(stackTrace[i].toString()).append('\n');
 				}
 				errorMessage.append(stackTrace.length - 1 - i).append(
 						" elements from Tomcat stacktrace skipped.");
 				response.put("error", errorMessage.toString());
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param loginRequest
 	 * @return the login if login is successful.
 	 */
 	public static String login(Map<String, Object> loginRequest) {
 		String login = (String) loginRequest.get("login");
 		String password = (String) loginRequest.get("password");
 		User possibleUser = User.getUserByLogin(login);
 		if (password != null && password.equals(possibleUser.getPassword())) {
 			return login;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Creates a new account, if not already in the database.
 	 * 
 	 * @param newAccountReq
 	 *            a Map specifying "login", "password", "name", "lastName",
 	 *            "email"
 	 * @return the created-user's login
 	 */
 	public static String createAccount(Map<String, Object> newAccountReq) {
 		String login = (String) newAccountReq.get("login");
 		User possibleUser = User.getUserByLogin(login);
 		if (possibleUser.getPassword() == null) {
 			possibleUser.setPassword((String) newAccountReq.get("password"));
 			possibleUser.setFirstName((String) newAccountReq.get("name"));
 			possibleUser.setLastName((String) newAccountReq.get("lastname"));
 			possibleUser.setEmail((String) newAccountReq.get("mail"));
 			possibleUser.saveOrUpdate();
 			return login;
 		} else {
 			return null;
 		}
 	}
 }
