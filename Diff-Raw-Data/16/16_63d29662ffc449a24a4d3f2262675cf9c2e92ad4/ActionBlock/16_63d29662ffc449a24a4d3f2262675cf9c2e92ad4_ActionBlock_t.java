 package com.ngdb.web.components.article;
 
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 
 public class ActionBlock {
 
 	@Inject
 	private CurrentUser currentUser;
 
 	@Property
 	@Parameter(required = false)
 	private boolean showText;
 
 	@Property
 	@Parameter
 	private Article article;
 
	@Inject
	private ArticleFactory articleFactory;

 	public boolean isAddableToCollection() {
 		return currentUser.canAddToCollection(article);
 	}
 
 	public boolean isBuyable() {
		article = articleFactory.findById(article.getId());
 		return article.isBuyable();
 	}
 
 	public boolean isSellable() {
 		return currentUser.canSell(article);
 	}
 
 	public boolean isWishable() {
 		return currentUser.canWish(article);
 	}
 
 	public User getUser() {
 		return currentUser.getUser();
 	}
 
 }
