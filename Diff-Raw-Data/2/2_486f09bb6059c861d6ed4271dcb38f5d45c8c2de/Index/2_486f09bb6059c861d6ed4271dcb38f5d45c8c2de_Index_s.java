 package com.ngdb.web.pages;
 
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import com.ngdb.entities.GameFactory;
 import com.ngdb.entities.WishBox;
 import com.ngdb.entities.article.Game;
 
 public class Index {
 
 	@Inject
 	private GameFactory gameFactory;
 
 	@Inject
 	private WishBox wishBox;
 
 	@Property
 	private Game randomGame1;
 
 	@Property
 	private Game randomGame2;
 
 	@Property
 	private Game randomGame3;
 
 	@SetupRender
 	public void init() {
 		randomGame1 = gameFactory.getRandomGameWithMainPicture();
 		randomGame2 = gameFactory.getRandomGameWithMainPicture();
 		randomGame3 = gameFactory.getRandomGameWithMainPicture();
 	}
 
 	public Long getNumGames() {
 		return gameFactory.getNumGames();
 	}
 
 	public Long getNumWhishes() {
 		return wishBox.getNumWishes();
 	}
 
 	public String getRandomGame1MainPicture() {
 		return randomGame1.getMainPicture().getUrl("medium");
 	}
 
 	public String getRandomGame2MainPicture() {
 		return randomGame2.getMainPicture().getUrl("medium");
 	}
 
 	public String getRandomGame3MainPicture() {
		return randomGame2.getMainPicture().getUrl("medium");
 	}
 
 }
