 package org.service;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.entity.Article;
 import org.entity.Reply;
 import org.entity.User;
 import org.eweb4j.orm.Db;
 import org.eweb4j.orm.Page;
 import org.eweb4j.orm.dao.DAOFactory;
 import org.eweb4j.util.CommonUtil;
 
 public class ArticleService {
 	
 	public Page<Article> getArticlePage(int pageIndex,int pageSize){
 		System.out.println("pageIndex-->"+pageIndex+" pageSize-->"+pageSize);
 		Page<Article> artilcePage = DAOFactory.getDAO(Article.class).selectAll().getPage(pageIndex, pageSize);
 		
 		Collection<Article> arts = artilcePage.getList();
 		
		for(Article art : arts){
			User u = Db.ar(User.class)
						.dao()
						.select("name", "email")
						.where()
							.field("id").equal(art.getUser().getId())
						.queryOne();
			
			art.getUser().setEmail(u.getEmail());
			art.getUser().setName(u.getName());
			
 		}
 		
 		return artilcePage;
 	}
 	
 	public long postArticle(Article article){
 		long articleId=0;
 		
 		try{
 			//发布时间
 			long pubDate=(CommonUtil.getNow(10));
 			article.setPubDate(String.valueOf(pubDate));
 			
 			//转换html
 			article.setContent(CommonUtil.changeHTML(article.getContent()));
 			
 			articleId=DAOFactory.getInsertDAO().insert(article).intValue();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		return articleId;
 	}
 	
 	public Article getOneArticle(long articleId){
 		Article article=null;
 		
 		try{
 			article = 
 				DAOFactory.getDAO(Article.class)
 					.fetch("user", "replies", "markUsers")
 					.selectAll()
 					.where()
 						.field("id").equal(articleId)
 					.queryOne();
 			
 			if (article != null) {
 				//fetch replies.user
 			 	DAOFactory.getCascadeDAO().select(article.getReplies().toArray(), "user");
 			 	
 			 	/*
 				Collection<Reply> replies=DAOFactory.getDAO(Reply.class).selectAll()
 				.where().field("article").equal(article.getId()).query();
 				
 				article.setReplies((ArrayList<Reply>)replies);
 				*/
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		return article;
 	}
 	
 	public boolean editArticle(Article article){
 		boolean status=false;
 		
 		try{
 			status=DAOFactory.getUpdateDAO().updateByFields(article,"title","content").intValue()>0;
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		return status;
 	}
 	
 	public Collection<Article> getMarkArticlesByUserId(long userId){
 		Collection<Article> articles=null;
 		
 		try{
 			articles=DAOFactory.getDAO(Article.class).alias("a")
 			.join("marks","m")
 			.selectAll()
 			.where().field("m.user").equal(userId)
 			.enableExpress(true)
 			.and("m.article").equal("a.id")
 			.query();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		return articles;
 	}
 
 }
