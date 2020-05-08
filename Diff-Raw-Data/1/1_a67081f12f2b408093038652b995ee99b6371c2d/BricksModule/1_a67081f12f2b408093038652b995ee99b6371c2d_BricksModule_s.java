 package org.northstar.bricks.config;
 
 import com.google.inject.Scopes;
 import com.google.inject.persist.PersistFilter;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.sitebricks.SitebricksModule;
 import com.google.sitebricks.SitebricksServletModule;
 import com.google.sitebricks.binding.FlashCache;
 import com.google.sitebricks.binding.HttpSessionFlashCache;
 import org.northstar.bricks.components.GuestbookNavigation;
 import org.northstar.bricks.components.NewCard;
 import org.northstar.bricks.components.UserPager;
 import org.northstar.bricks.dao.EntryDao;
 import org.northstar.bricks.dao.SimpleEntryDao;
 import org.northstar.bricks.dao.UserFinder;
 import org.northstar.bricks.pages.*;
 import org.northstar.bricks.services.Hello;
 
 /**
  * Configures a Sitebrick Module
  * 
  * @author
  */
 public class BricksModule extends SitebricksModule {
 	@Override
 	protected void configureSitebricks() {
 
 		install(new JpaPersistModule("myFirstJpaUnit").addFinder(UserFinder.class));
 

 		bind(FlashCache.class).to(HttpSessionFlashCache.class).in(
 				Scopes.SINGLETON);
         bind(EntryDao.class).to(SimpleEntryDao.class).in(Scopes.SINGLETON);
 		at("static/default.css").export("bricks.css");
 
 		at("/").show(Home.class);
 		at("/flow").show(Flow.class);
         at("/guestbook").show(Guestbook.class);
         at("/guestbook/:id").show(GuestbookEntry.class);
         at("/login").show(Login.class);
         at("/count").show(Count.class);
 
 		at("/hello").serve(Hello.class);
 
 		embed(NewCard.class).as("Card");
         embed(GuestbookNavigation.class).as("navigation");
         embed(UserPager.class).as("Pager");
 	}
 
 	@Override
 	protected SitebricksServletModule servletModule() {
 		return new SitebricksServletModule() {
 			@Override
 			protected void configurePreFilters() {
 				filter("/*").through(PersistFilter.class);
 			}
 		};
 	}
 
 }
