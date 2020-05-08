 package com.marshmallowswisdom.liber.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.marshmallowswisdom.liber.domain.Article;
 
 public class RestfulArticle {
 	
 	private final Article article;
 	private final List<Link> links;
 	
 	public RestfulArticle( final Article article ) {
 		this.article = article;
 		links = new ArrayList<Link>();
 	}
 	
 	public void addLink( final Link link ) {
 		links.add( link );
 	}
 	
 	public List<Link> getLinks() {
 		return links;
 	}
 	
 	public int getId() {
 		return article.getId();
 	}
 	
 	public String getName() {
 		return article.getName();
 	}
 	
 	public String getContent() {
 		return article.getContent();
 	}
 
 }
