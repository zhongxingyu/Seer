 /*
  * GetTagHandler.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.instance.server.action;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import org.dozer.Mapper;
 import org.slf4j.Logger;
 
 import com.google.inject.Inject;
 import com.google.inject.persist.Transactional;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
import com.prealpha.extempdb.domain.Tag;
 import com.prealpha.extempdb.domain.Tag_;
 import com.prealpha.extempdb.instance.shared.action.GetTag;
 import com.prealpha.extempdb.instance.shared.action.GetTagResult;
 import com.prealpha.extempdb.instance.shared.dto.TagDto;
 import com.prealpha.extempdb.util.logging.InjectLogger;
 
 class GetTagHandler implements ActionHandler<GetTag, GetTagResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final Mapper mapper;
 
 	@Inject
 	public GetTagHandler(EntityManager entityManager, Mapper mapper) {
 		this.entityManager = entityManager;
 		this.mapper = mapper;
 	}
 
 	/*
 	 * A bug in Hibernate prevents us from using entityManager.find() to get the
 	 * tag directly. Instead, we have to convert everything to upper case and to
 	 * a criteria query to make a real comparison.
 	 */
 	@Transactional
 	@Override
 	public GetTagResult execute(GetTag action, Dispatcher dispatcher)
 			throws ActionException {
 		String tagName = action.getTagName();
 		try {
 			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
 			CriteriaQuery<Tag> criteria = builder.createQuery(Tag.class);
 			Root<Tag> tagRoot = criteria.from(Tag.class);
 			criteria.where(builder.equal(builder.upper(tagRoot.get(Tag_.name)),
 					tagName.toUpperCase()));
 			Tag tag = entityManager.createQuery(criteria).getSingleResult();
 			log.info("handled request for tag, name \"{}\"", tag.getName());
 			TagDto dto = mapper.map(tag, TagDto.class);
 			return new GetTagResult(dto);
 		} catch (NoResultException nrx) {
 			log.info("handled request for non-existent tag, name \"{}\"",
 					tagName);
 			return new GetTagResult(null);
 		}
 	}
 }
