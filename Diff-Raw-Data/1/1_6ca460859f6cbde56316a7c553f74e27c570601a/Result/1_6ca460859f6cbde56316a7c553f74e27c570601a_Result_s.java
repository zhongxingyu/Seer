 package com.ngdb.web.pages;
 
 import java.util.Collection;
 
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.Game;
import com.ngdb.web.services.infrastructure.CurrentUser;
 
 public class Result {
 
 	@Persist
 	private Collection<Article> results;
 
 	@Property
 	private Article result;
 
 	@Persist
 	private String search;
 
 	public void setResults(Collection<Article> results) {
 		this.results = results;
 	}
 
 	public Collection<Article> getResults() {
 		return results;
 	}
 
 	public boolean isGame() {
 		return result instanceof Game;
 	}
 
 	public void setSearch(String search) {
 		this.search = search;
 	}
 
 	public String getSearch() {
 		return search;
 	}
 
 }
