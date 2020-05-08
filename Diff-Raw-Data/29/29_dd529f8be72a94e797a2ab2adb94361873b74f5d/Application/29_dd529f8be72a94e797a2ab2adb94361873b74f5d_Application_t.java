 package controllers;
 
 import play.*;
 import play.cache.Cache;
 import play.mvc.*;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Map.Entry;
 
 import models.*;
 
 public class Application extends Controller {
 
 	static Map<String, Article> q = null;
 
 	@Before
 	public static void loadConf() {
 		//Play.configuration.getProperty("toto.author");
		//Play.configuration.getProperty("toto.author");
 	}
 
 	@Before
 	public static void loadArticles() throws FileNotFoundException, ParseException {
 		q = (Map<String, Article>) Cache.get("articles");
 		if (q == null) {
 			File dir = Play.getFile("/articles");
 			Map<String, Article> newMap = new HashMap<String,Article>();
 			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
 			for (String fileName:dir.list()) {
 				File file = Play.getFile("/articles/"+fileName);
 				Article article = new Article();
 			    try {
 				  BufferedReader br = new BufferedReader(new FileReader(file));
 			      int count = 0;
 			      String line;
 			      while ((line = br.readLine()) != null){
 			    	count++;
 			        if (count < 4) {
 			          String[] tokens = line.split(": ");
 		              switch (count) {
 			              case 1:
 			            	  article.title = tokens[1];
 			              	  break;
 			              case 2:
 			            	  article.author = tokens[1];
 			            	  break;
 			              case 3:
 			            	  article.date = formatter.parse(tokens[1]);
			            	  article.slug = article.title.replace(" ", "-");
 			            	  break;
 			          }
 			        } else {
 			        	if (article.body != null) {
 			        		article.body += line;
 			        	} else {
 			        		article.body = line;
 			        	}
 			        }
 			      }
 
 			    } catch (FileNotFoundException e) {
 			      e.printStackTrace();
 			    } catch (IOException e) {
 			      e.printStackTrace();
 			    }
 				newMap.put(article.slug, article);
 			}
 			q = newMap;
 			Cache.set("articles", q, "1h");
 		}
 	}
 	
	public static void index() {
		List<Article> articles = new ArrayList<Article>(q.values());
    	render(articles);
	}
 
	public static void article(String yyyy, String MM, String dd, String slug) {
 		Article article = q.get(slug);
    	render(article);
	}
 
 	public static void about() {
    	render();
	}
 
 }
