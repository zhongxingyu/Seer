 /*
  * GetMappingsByArticleHandler.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.action;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import org.dozer.Mapper;
 import org.slf4j.Logger;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.inject.Inject;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
 import com.subitarius.action.GetMappingsByArticle;
 import com.subitarius.action.GetMappingsResult;
 import com.subitarius.action.dto.TagMappingDto;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.domain.DistributedEntity;
 import com.subitarius.domain.DistributedEntity_;
 import com.subitarius.domain.TagMapping;
 import com.subitarius.domain.TagMapping_;
 import com.subitarius.util.logging.InjectLogger;
 
 final class GetMappingsByArticleHandler implements
 		ActionHandler<GetMappingsByArticle, GetMappingsResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final Mapper mapper;
 
 	@Inject
 	private GetMappingsByArticleHandler(EntityManager entityManager,
 			Mapper mapper) {
 		this.entityManager = entityManager;
 		this.mapper = mapper;
 	}
 
 	@Override
 	public GetMappingsResult execute(GetMappingsByArticle action,
 			Dispatcher dispatcher) throws ActionException {
 		String articleUrlHash = action.getArticleUrlHash();
 		ArticleUrl articleUrl = entityManager.find(ArticleUrl.class,
 				articleUrlHash);
 
 		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
 		CriteriaQuery<TagMapping> criteria = builder
 				.createQuery(TagMapping.class);
 		Root<TagMapping> mappingRoot = criteria.from(TagMapping.class);
		criteria.select(mappingRoot);
 		Root<DistributedEntity> entityRoot = criteria
 				.from(DistributedEntity.class);
		criteria.where(
				builder.and(builder.equal(
						mappingRoot.get(TagMapping_.articleUrl), articleUrl)),
				builder.isEmpty(entityRoot.get(DistributedEntity_.children)));
		criteria.distinct(true);
 		List<TagMapping> mappings = entityManager.createQuery(criteria)
 				.getResultList();
 
 		Collection<TagMappingDto> dtos = Collections2.transform(mappings,
 				new Function<TagMapping, TagMappingDto>() {
 					@Override
 					public TagMappingDto apply(TagMapping input) {
 						return mapper.map(input, TagMappingDto.class);
 					}
 				});
 		dtos = Collections2.filter(dtos, action);
 
 		log.info("returned {} mappings for article URL: {}", dtos.size(),
 				articleUrl);
 
 		return new GetMappingsResult(dtos);
 	}
 }
