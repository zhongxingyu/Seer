 package com.ngdb.web.components.article;
 
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.Game;
 
 public class Thumbnail {
 
 	@Property
 	@Parameter
 	private Article article;
 
 	@Property
 	@Parameter
 	private String size;
 
 	@Property
 	@Parameter
 	private boolean noClick;
 
 	@Property
 	@Parameter
 	private boolean center;
 
 	public String getViewPage() {
 		if (article instanceof Game) {
 			return "article/game/gameView";
 		}
 		return "article/hardware/hardwareView";
 	}
 
 	public String getUrl() {
 		if (size == null) {
 			return article.getCover().getUrl();
 		}
		return article.getCover().getUrl(size);
	}
 
 }
