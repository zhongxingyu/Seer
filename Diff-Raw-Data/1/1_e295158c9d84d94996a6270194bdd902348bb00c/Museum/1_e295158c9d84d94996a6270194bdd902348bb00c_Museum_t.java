 package com.ngdb.web.pages;
 
 import static com.google.common.collect.Collections2.filter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.ngdb.Predicates;
 import com.ngdb.entities.GameFactory;
 import com.ngdb.entities.HardwareFactory;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.entities.user.User;
 
 public class Museum {
 
 	@Property
 	private Article article;
 
 	@Inject
 	private GameFactory gameFactory;
 
 	@Inject
 	private HardwareFactory hardwareFactory;
 
 	@Inject
 	private ReferenceService referenceService;
 
 	@Property
 	private User user;
 
 	// ---- Platform
 	@Persist
 	private Long filterPlatform;
 
 	@Property
 	private Platform platform;
 
 	// ---- Origin
 
 	@Persist
 	private Long filterOrigin;
 
 	@Property
 	private Origin origin;
 
	@Persist
 	@Property
 	private boolean filteredByGames;
 
 	void onActivate() {
 		if (filterOrigin == null && filterPlatform == null) {
 			this.filterOrigin = referenceService.findOriginByTitle("Japan").getId();
 			this.filterPlatform = referenceService.findPlatformByName("Neo·Geo CD").getId();
 			this.filteredByGames = true;
 		}
 	}
 
 	void onActivate(User user) {
 		this.user = user;
 	}
 
 	public List<Article> getArticles() {
 		Collection<Article> filteredArticles;
 		List<Predicate<Article>> filters = Lists.newArrayList();
 		if (filteredByGames) {
 			filteredArticles = new ArrayList<Article>(gameFactory.findAll());
 		} else {
 			filteredArticles = new ArrayList<Article>(hardwareFactory.findAll());
 		}
 		if (filterOrigin != null) {
 			filters.add(new Predicates.OriginPredicate(referenceService.findOriginById(filterOrigin)));
 		}
 		if (filterPlatform != null) {
 			filters.add(new Predicates.PlatformPredicate(referenceService.findPlatformById(filterPlatform)));
 		}
 		for (Predicate<Article> filter : filters) {
 			filteredArticles = filter(filteredArticles, filter);
 		}
 		if (user != null) {
 			filteredArticles = filter(filteredArticles, keepOnlyOwnedArticlesBy(user));
 		}
 		return new ArrayList<Article>(filteredArticles);
 	}
 
 	private Predicate<Article> keepOnlyOwnedArticlesBy(final User user) {
 		return new Predicate<Article>() {
 			@Override
 			public boolean apply(Article input) {
 				return user.owns(input);
 			}
 		};
 	}
 
 	Object onActionFromClearFilters() {
 		onActivate();
 		return this;
 	}
 
 	Object onActionFromSelectGames() {
 		this.filteredByGames = true;
 		return this;
 	}
 
 	Object onActionFromSelectHardwares() {
 		this.filteredByGames = false;
 		return this;
 	}
 
 	// ---- Platform
 	public List<Platform> getPlatforms() {
 		return referenceService.getPlatforms();
 	}
 
 	Object onActionFromFilterPlatform(Platform platform) {
 		filterPlatform = platform.getId();
 		return this;
 	}
 
 	public boolean isFilteredByThisPlatform() {
 		return platform.getId().equals(filterPlatform);
 	}
 
 	// ---- Origin
 
 	public List<Origin> getOrigins() {
 		return referenceService.getOrigins();
 	}
 
 	Object onActionFromFilterOrigin(Origin origin) {
 		filterOrigin = origin.getId();
 		return this;
 	}
 
 	public boolean isFilteredByThisOrigin() {
 		return origin.getId().equals(filterOrigin);
 	}
 
 }
