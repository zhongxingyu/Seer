 package de.owkwo.service;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 import org.htmlcleaner.XPatherException;
 
 import de.owkwo.model.posts.NewsPost;
 import de.owkwo.model.posts.ParentNewsPost;
 import de.owkwo.model.profile.User;
 import de.owkwo.service.delegate.interfaces.WKWServiceDelegatable;
 
 public class WKWService {
 
 	/**
 	 * Constants for all the URLs needed on Wer-kennt-wen.de
 	 */
 
 	public static final String WKW_LOGIN_PAGE = "http://mobil.wer-kennt-wen.de/";
 	public static final String WKW_START_PAGE = "http://mobil.wer-kennt-wen.de/start/";
 
 	public static final String LOGIN_URL = "http://mobil.wer-kennt-wen.de/index/";
 	public static final String LOGOUT_URL = "http://mobil.wer-kennt-wen.de/index/logout/";
 	public static final String LOGIN_REDIRECT_SUCCESS = "/start/";
 	public static final String LOGIN_REDIRECT_FAIL = "/";
 	public static final String LOGIN_USERNAME_PARAM = "email";
 	public static final String LOGIN_PASSWPORD_PARAM = "password";
 	public static final String LOGIN_HASH_PARAM = "_hash";
 	public static final String LOGIN_HASHKEY_PARAM = "_hashKey";
 	public static final String LOGIN_SEND_PARAM_VALUE = "Login";
 	public static final String LOGIN_SEND_PARAM = "send";
 	public static final String LOGIN_SETCOOKIE_PARAM = "setCookie";
 	public static final String LOGIN_SETCOOKIE_VALUE = "0";
 
 	public static final String PROFILE_URL = "http://www.wer-kennt-wen.de/person";
 	public static final String PROFILE_ANY_URL = "http://mobil.wer-kennt-wen.de/person/index/id/";
 
 	public static final String SETTINGS_MAIN_URL = "http://www.wer-kennt-wen.de/settings/main/";
 	public static final String SETTINGS_CONTACT_URL = "http://www.wer-kennt-wen.de/settings/contact/";
 	public static final String SETTINGS_SCHOOL_URL = "http://www.wer-kennt-wen.de/settings/school/";
 	public static final String SETTINGS_PERSONAL_URL = "http://www.wer-kennt-wen.de/settings/personal/";
 	public static final String SETTINGS_PHOTO_URL = "http://www.wer-kennt-wen.de/settings/photo/";
 
 	public static final String NEWS_URL = "http://mobil.wer-kennt-wen.de/justnow/news/";
 	public static final String NEWS_PAGE_ANY_URL = "http://mobil.wer-kennt-wen.de/justnow/news/page/";
 	public static final String COMMENT_PAGE_URL = "http://mobil.wer-kennt-wen.de/justnow/index/id/";
 
 	private WKWServiceDelegatable delegate;
 	private DefaultHttpClient client;
 
 	// Used for cashing the hash-Keys
 	private String hash;
 	private String hashKey;
 
 	public void setDelegate(WKWServiceDelegatable delegate) {
 
 		this.delegate = delegate;
 
 	}
 
 	public WKWServiceDelegatable getDelegate() {
 
 		return this.delegate;
 
 	}
 
 	public WKWService(WKWServiceDelegatable delegate) {
 
 		this.delegate = delegate;
 
 		// Setting parameters
 		HttpParams params = new BasicHttpParams();
 
 		// Don't follow redirects. We need it for error handling
 		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
 		ConnManagerParams.setMaxTotalConnections(params, 100);
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		params.setParameter(ClientPNames.COOKIE_POLICY,
 				CookiePolicy.BROWSER_COMPATIBILITY);
 
 		// Create and initialize scheme registry
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory
 				.getSocketFactory(), 80));
 
 		// Create an HttpClient with the ThreadSafeClientConnManager.
 		// This connection manager must be used if more than one thread will
 		// be using the HttpClient.
 		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
 				schemeRegistry);
 		this.client = new DefaultHttpClient(cm, params);
 
 	}
 
 	public void logOut() {
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				HttpGet httpget = new HttpGet(LOGOUT_URL);
 				try {
 
 					HttpResponse resp = client.execute(httpget);
 
 					resp.getEntity().consumeContent();
 
 					delegate.onLogOut(WKWServiceDelegatable.STATUS_SUCCESSFUL);
 
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 
 	}
 
 	public void logIn(final String user, final String password) {
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				// First of alle create hashkeys to emulate browser usage for
 				// wkw
 				initHashKeys(WKW_LOGIN_PAGE);
 
 				HttpPost httppost = new HttpPost(LOGIN_URL);
 				httppost.addHeader(
 						"User-Agent",
 						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.53 Safari/534.3");
 				httppost.addHeader(
 						"Accept",
 						"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
 				httppost.addHeader("Content-Type",
 						"application/x-www-form-urlencoded");
 
 				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
 				formparams.add(new BasicNameValuePair(LOGIN_USERNAME_PARAM,
 						user));
 				formparams.add(new BasicNameValuePair(LOGIN_PASSWPORD_PARAM,
 						password));
 				formparams.add(new BasicNameValuePair(LOGIN_SETCOOKIE_PARAM,
 						LOGIN_SETCOOKIE_VALUE));
 				formparams.add(new BasicNameValuePair("_hash", hash));
 				formparams.add(new BasicNameValuePair("_hashKey", hashKey));
 				formparams.add(new BasicNameValuePair("LOGIN_SEND_PARAM",
 						"LOGIN_SEND_PARAM_VALUE"));
 
 				UrlEncodedFormEntity entity;
 				try {
 
 					entity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
 					httppost.setEntity(entity);
 
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				HttpResponse response;
 				try {
 
 					response = client.execute(httppost);
 					Header[] headers = response.getHeaders("location");
 					String redirectHeader = headers[0].getValue();
 					System.out.println(redirectHeader);
 					HttpEntity respEntity = response.getEntity();
 					respEntity.consumeContent();
 
 					// System.out.println(IOUtils.toString(resp.getEntity()
 					// .getContent()));
 
 					// List<Cookie> cookies =
 					// client.getCookieStore().getCookies();
 					// if (cookies.isEmpty()) {
 					// System.out.println("None");
 					// } else {
 					// System.out.println("Cookie Store:");
 					// for (int i = 0; i < cookies.size(); i++) {
 					// System.out.println("- " + cookies.get(i).toString());
 					// }
 					// }
 					//
 
 					delegate.onLogIn((redirectHeader
 							.equals(LOGIN_REDIRECT_SUCCESS) ? WKWServiceDelegatable.STATUS_SUCCESSFUL
 							: WKWServiceDelegatable.STATUS_ERROR));
 
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 
 	}
 
 	public void createNewPost(final String message) {
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				// First of alle create hashkeys to emulate browser usage for
 				// wkw
 				initHashKeys(WKW_START_PAGE);
 
 				HttpPost httppost = new HttpPost(WKW_START_PAGE);
 				httppost.addHeader(
 						"User-Agent",
 						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.53 Safari/534.3");
 				httppost.addHeader(
 						"Accept",
 						"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
 				httppost.addHeader("Content-Type",
 						"application/x-www-form-urlencoded");
 
 				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
 
 				formparams.add(new BasicNameValuePair("_hash", hash));
 				formparams.add(new BasicNameValuePair("_hashKey", hashKey));
 				formparams.add(new BasicNameValuePair("send", "senden"));
 				formparams.add(new BasicNameValuePair("justnowForm", "1"));
 				formparams.add(new BasicNameValuePair("body", message));
 
 				UrlEncodedFormEntity entity;
 				try {
 
					entity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
 					httppost.setEntity(entity);
 
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				HttpResponse response;
 				try {
 
 					response = client.execute(httppost);
 					HttpEntity respEntity = response.getEntity();
 					int statusCode = response.getStatusLine().getStatusCode();
 					respEntity.consumeContent();
 
 					delegate.onPost(statusCode == 200 ? WKWServiceDelegatable.STATUS_SUCCESSFUL
 							: WKWServiceDelegatable.STATUS_ERROR);
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 
 	}
 
 	protected void initHashKeys(String page) {
 
 		HttpGet httpget = new HttpGet(page);
 		try {
 
 			HttpResponse resp = client.execute(httpget);
 
 			String htmlPage = IOUtils.toString(resp.getEntity().getContent());
 
 			HtmlCleaner cleaner = new HtmlCleaner();
 			TagNode node = cleaner.clean(htmlPage);
 			Object[] hashResults = node
 					.evaluateXPath("//input[@name='_hash']/@value");
 			Object[] hashKeyResults = node
 					.evaluateXPath("//input[@name='_hashKey']/@value");
 
 			this.hash = hashResults[0].toString();
 			this.hashKey = hashKeyResults[0].toString();
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XPatherException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void getCommentsForPost(final String userId, final String postId, final int page) {
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				HttpGet httpget = new HttpGet(COMMENT_PAGE_URL + userId
 						+ "/entry/" + postId + "/page/" + page);
 				try {
 
 					HtmlCleaner htmlCleaner = new HtmlCleaner();
 
 					htmlCleaner.getProperties().setTranslateSpecialEntities(
 							false);
 					HttpEntity entity = client.execute(httpget).getEntity();
 
 					// Can't give html cleaner the inputstream, coz it will fail
 					// due to
 					// missing charset handling. Using IOUtils to convert to
 					// String
 					// Page is small. So no memory issues to frightened of.
 
 					TagNode cleanPage = htmlCleaner.clean((IOUtils.toString(
 							entity.getContent(), "ISO-8859-1")));
 					try {
 						// Gathering all the information with xpath
 						// To all the Brainy Smurfs out there:
 						// I fucking know, that those can be better.
 
 						Object[] foundPicNodes = cleanPage
 								.evaluateXPath("//table[@id='photolist']//td[@class='pic']//img");
 						Object[] foundlistCellNodes = cleanPage
 								.evaluateXPath("//table[@id='photolist']//td[@class='listcell']");
 					
 						// Check if everything is fine. Else send error.
 						if (foundPicNodes == null || foundPicNodes.length == 0)
 							delegate.onCommentForPost(null, userId, postId, page,
 									WKWServiceDelegatable.STATUS_ERROR);
 
 						// Create the authors
 						ArrayList<NewsPost> newsPosts = new ArrayList<NewsPost>();
 						for (int i = 0; i < foundPicNodes.length; i++) {
 
 							// Extract data for Author and Post
 							TagNode img = (TagNode) foundPicNodes[i];
 							String avatarUrl = img.getAttributeByName("src");
 							String userName = img.getAttributeByName("alt");
 
 							TagNode listCell = (TagNode) foundlistCellNodes[i];
 							String postDate = listCell.getElementsByName(
 									"span", true)[0].getText().toString();
 							String postBody = listCell.getElementsByName("p",
 									true)[0].getText().toString();
 
 							User postAuthor = new User(userId, userName,
 									avatarUrl);
 							NewsPost newsPost = new NewsPost(postId,
 									postAuthor, postDate, postBody);
 							newsPosts.add(newsPost);
 
 						}
 
 						delegate.onCommentForPost(newsPosts, userId, postId, page,
 								WKWServiceDelegatable.STATUS_SUCCESSFUL);
 
 					} catch (XPatherException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 
 	}
 
 	public void getPostsForPage(final int page) {
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				HttpGet httpget = new HttpGet(NEWS_PAGE_ANY_URL
 						+ new Integer(page).toString());
 				try {
 
 					HtmlCleaner htmlCleaner = new HtmlCleaner();
 
 					htmlCleaner.getProperties().setTranslateSpecialEntities(
 							false);
 					HttpEntity entity = client.execute(httpget).getEntity();
 
 					// Can't give html cleaner the inputstream, coz it will fail
 					// due to
 					// missing charset handling. Using IOUtils to convert to
 					// String
 					// Page is small. So no memory issues to frightened of.
 
 					TagNode cleanPage = htmlCleaner.clean((IOUtils.toString(
 							entity.getContent(), "ISO-8859-1")));
 					try {
 						// Gathering all the information with xpath
 						// To all the Brainy Smurfs out there:
 						// I fucking know, that those can be better.
 
 						Object[] foundPicNodes = cleanPage
 								.evaluateXPath("//table[@id='photolist']//td[@class='pic']//img");
 						Object[] foundlistCellNodes = cleanPage
 								.evaluateXPath("//table[@id='photolist']//td[@class='listcell']");
 						Object[] foundCommentNodes = cleanPage
 								.evaluateXPath("//a[@class='writeComment']/@href");
 						Object[] commentNumbers = cleanPage
 								.evaluateXPath("//a[@class='writeComment']/text()");
 
 						// Check if everything is fine. Else send error.
 						if (foundPicNodes == null || foundPicNodes.length == 0)
 							delegate.onNewsPageUpdate(null,
 									WKWServiceDelegatable.STATUS_ERROR, page);
 
 						// Create the authors
 						ArrayList<ParentNewsPost> newsPosts = new ArrayList<ParentNewsPost>();
 						for (int i = 0; i < foundPicNodes.length; i++) {
 
 							// Extract data for Author and Post
 							TagNode img = (TagNode) foundPicNodes[i];
 							String avatarUrl = img.getAttributeByName("src");
 							String userName = img.getAttributeByName("alt");
 
 							TagNode listCell = (TagNode) foundlistCellNodes[i];
 							String postDate = listCell.getElementsByName(
 									"span", true)[0].getText().toString();
 							String postBody = listCell.getElementsByName("p",
 									true)[0].getText().toString();
 							String commentString = (String) foundCommentNodes[i];
 							String userId = commentString.subSequence(
 									commentString.lastIndexOf("/id/") + 4,
 									commentString.lastIndexOf("/entry/"))
 									.toString();
 							String postId = commentString.subSequence(
 									commentString.lastIndexOf("/entry/") + 7,
									commentString.length() - 2).toString();
 
 							User postAuthor = new User(userId, userName,
 									avatarUrl);
 							ParentNewsPost newsPost = new ParentNewsPost(
 									postId, postAuthor, postDate, postBody,
 									null);
 
 							// Handle comment Pages
 							String commentNumberString = commentNumbers[i]
 									.toString();
 
 							// Use regex to get the number of comments
 							Pattern numberPattern = Pattern.compile("[0-9]+");
 							Matcher m = numberPattern
 									.matcher(commentNumberString);
 							int number = -1;
 							if (m.find())
 								number = Integer.parseInt(m.group(0));
 
 							if (number < 0) {
 
 								newsPost.setCommentPages(0);
 								newsPost.setCommentNumber(0);
 
 							} else {
 								newsPost.setCommentPages((number / 5 > 0) ? ((number % 5 == 0) ? (number / 5)
 										: (number / 5) + 1)
 										: 1);
 								newsPost.setCommentNumber(number);
 							}
 
 							newsPosts.add(newsPost);
 
 						}
 
 						delegate.onNewsPageUpdate(newsPosts,
 								WKWServiceDelegatable.STATUS_SUCCESSFUL, page);
 
 					} catch (XPatherException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 
 	}
 	
 	public void getProfileHTML(final String userId) {
 		
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				HttpGet httpget = new HttpGet(PROFILE_ANY_URL + userId + "/");
 				try {
 
 					HttpResponse resp = client.execute(httpget);
 
 					HtmlCleaner cleaner = new HtmlCleaner();
 					TagNode cleanHTML = cleaner.clean(resp.getEntity().getContent());
 					TagNode contentDiv = (TagNode) cleanHTML.evaluateXPath("//div[@id='content']")[0];
 					
 					delegate.onProfileHTML(cleaner.getInnerHtml(contentDiv), WKWServiceDelegatable.STATUS_SUCCESSFUL);
 					
 
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (XPatherException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 		
 		
 		
 		
 		
 		
 		
 		
 		
 	}
 
 	/**
 	 * --------------------------------------- 
 	 * FOR TESTING AND DEBUGGING
 	 * PURPOSES 
 	 * ---------------------------------------
 	 * 
 	 * @throws InterruptedException
 	 */
 
 	public static void main(String[] args) throws InterruptedException {
 
 		// First decline service and delegate
 
 		final WKWService access = new WKWService(new WKWServiceDelegatable() {
 
 			@Override
 			public void onNewsPageUpdate(ArrayList<ParentNewsPost> posts,
 					String status, int page) {
 
 				System.out.println("------------------------------------");
 				System.out.println("Posts for Page: " + page);
 				System.out.println("------------------------------------");
 				for (ParentNewsPost item : posts) {
 
 					System.out.println(item.getAuthor().getName() + " says:");
 					System.out.println(item.getMessage());
 					System.out.println("\n at " + item.getPostDate());
 					System.out.println("Mit " + item.getCommentNumber()+ " Kommentaren und " + item.getCommentPages() + " Kommentar-Seiten");
 
 				}
 				//
 				System.out.println();
 
 			}
 
 			@Override
 			public void onPost(String status) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onLogOut(String status) {
 
 				System.out.println("Log Out Handler Call");
 				System.out.println(status);
 
 			}
 
 			@Override
 			public void onLogIn(String status) {
 
 				System.out.println("Log In Handler Call");
 				System.out.println(status);
 
 			}
 
 			@Override
 			public void onCommentForPost(ArrayList<NewsPost> posts,
 					String userId, String postId, int page, String status) {
 
 				System.out.println("------------------------------------");
 				System.out.println("Comments for Post " + postId + " from Page: " + page + " from user " + userId);
 				System.out.println("------------------------------------");
 				for (NewsPost item : posts) {
 
 					System.out.println(item.getAuthor().getName() + " says:");
 					System.out.println(item.getMessage());
 					System.out.println("\n at " + item.getPostDate());				
 
 				}
 				//
 				System.out.println();
 
 			}
 
 			@Override
 			public void onProfileHTML(String html, String status) {
 				
 				System.out.println(html);
 				
 			}
 		});
 
 		// Call the service functions
 
 		// Try to login
		access.logIn("user", "pass");
 		// Sleep view seconds to ensure login
 
 		// get all posts. multi-threaded
 		Thread.sleep(10000);
 //		access.getPostsForPage(1);
 //		access.getPostsForPage(2);
 //		access.getPostsForPage(3);
 //		access.getPostsForPage(4);
 //		access.getPostsForPage(5);
//		access.getCommentsForPost("9npx2i7g", "47psmge9zm", 1 );
		access.getProfileHTML("n9dq8jvr");
 		// access.getCommentsForPost("9npx2i7g", "59tu1k3v6u");
 
 	}
 
 }
