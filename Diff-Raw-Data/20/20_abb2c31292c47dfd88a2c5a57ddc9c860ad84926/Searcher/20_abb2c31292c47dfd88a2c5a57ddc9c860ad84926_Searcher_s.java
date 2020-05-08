 /*
  * Searcher.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.central.search;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import org.slf4j.Logger;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.inject.Inject;
 import com.google.inject.persist.Transactional;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.domain.Source;
 import com.subitarius.domain.Tag;
 import com.subitarius.domain.Tag.Type;
 import com.subitarius.domain.TagMapping;
 import com.subitarius.util.logging.InjectLogger;
 
 public class Searcher {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final SearchProvider searchProvider;
 
 	@Inject
	private Searcher(EntityManager entityManager, SearchProvider searchProvider) {
 		this.entityManager = entityManager;
 		this.searchProvider = searchProvider;
 	}
 
 	public void run(Set<Source> sources) {
 		log.info("starting search");
 		try {
 			Iterable<Tag> searchedTags = getAllTags();
 			searchedTags = Iterables.filter(searchedTags, new Predicate<Tag>() {
 				@Override
 				public boolean apply(Tag input) {
 					return (input.getType() == Type.SEARCHED);
 				}
 			});
 
 			for (Source source : sources) {
 				try {
 					for (Tag tag : searchedTags) {
 						search(tag, source);
 					}
 				} catch (RuntimeException rx) {
 					log.error("unexpected exception was thrown", rx);
 				}
 			}
 			log.info("search complete");
 		} catch (SearchUnavailableException sux) {
 			log.error("search provider was unavailable", sux);
 		}
 	}
 
 	@Transactional
 	void search(Tag tag, Source source) throws SearchUnavailableException {
 		int resultCount = 0;
 		List<ArticleUrl> articleUrls = searchProvider.search(tag, source, 1);
 		for (ArticleUrl articleUrl : articleUrls) {
 			entityManager.persist(articleUrl);
 			log.debug("persisted article URL: {}", articleUrl);
 
 			TagMapping mapping = new TagMapping(tag, articleUrl);
 			TagMapping existing = entityManager.find(TagMapping.class,
 					mapping.getHash());
 			if (existing != null) {
 				log.debug("mapping already exists: {}", mapping);
 			} else {
 				entityManager.persist(mapping);
 				log.debug("persisted mapping: {}", mapping);
 			}
 		}
 		Object[] args = { resultCount, tag, source };
 		log.info("handled {} search result(s) for query: ({}; {})", args);
 	}
 
 	@Transactional
 	Iterable<Tag> getAllTags() {
 		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
 		CriteriaQuery<Tag> criteria = builder.createQuery(Tag.class);
 		Root<Tag> root = criteria.from(Tag.class);
 		criteria.select(root);
 		criteria.distinct(true);
 		return entityManager.createQuery(criteria).getResultList();
 	}
 }
