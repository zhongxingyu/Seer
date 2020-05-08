 /*
  * GetMappingsByTagHandler.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.instance.server.action;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.dozer.Mapper;
 import org.slf4j.Logger;
 
 import com.google.common.collect.Iterables;
 import com.google.inject.Inject;
 import com.google.inject.persist.Transactional;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
 import com.prealpha.extempdb.domain.TagMapping;
 import com.prealpha.extempdb.instance.shared.action.GetMappingsByTag;
 import com.prealpha.extempdb.instance.shared.action.GetMappingsResult;
 import com.prealpha.extempdb.instance.shared.dto.TagMappingDto;
 import com.prealpha.extempdb.util.logging.InjectLogger;
 
 class GetMappingsByTagHandler implements
 		ActionHandler<GetMappingsByTag, GetMappingsResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final Mapper mapper;
 
 	@Inject
 	public GetMappingsByTagHandler(EntityManager entityManager, Mapper mapper) {
 		this.entityManager = entityManager;
 		this.mapper = mapper;
 	}
 
 	@Transactional
 	@Override
 	public GetMappingsResult execute(GetMappingsByTag action,
 			Dispatcher dispatcher) throws ActionException {
 		String tagName = action.getTagName();
 		Tag tag = entityManager.find(Tag.class, tagName);
 		List<TagMappingDto> dtos = new ArrayList<TagMappingDto>();
 
 		for (TagMapping mapping : tag.getMappings()) {
 			dtos.add(mapper.map(mapping, TagMappingDto.class));
 		}
 
 		if (action.getComparator() != null) {
 			Collections.sort(dtos, action.getComparator());
 		}
 
 		List<TagMappingDto.Key> mappingKeys = new ArrayList<TagMappingDto.Key>();
 		for (TagMappingDto dto : Iterables.filter(dtos, action)) {
 			mappingKeys.add(dto.getKey());
 		}
 
 		log.info(
 				"handled request for mappings to tag \"{}\", returning {} mapping keys",
 				tagName, mappingKeys.size());
 
 		return new GetMappingsResult(mappingKeys);
 	}
 }
