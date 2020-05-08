 /*
  * UpdateTagHandler.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.instance.server.action;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.servlet.http.HttpSession;
 
 import org.apache.catalina.User;
 import org.slf4j.Logger;
 
 import com.google.inject.Inject;
 import com.google.inject.persist.Transactional;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
 import com.prealpha.extempdb.instance.shared.action.MutationResult;
 import com.prealpha.extempdb.instance.shared.action.UpdateTag;
 import com.prealpha.extempdb.util.logging.InjectLogger;
 
 class UpdateTagHandler implements ActionHandler<UpdateTag, MutationResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final HttpSession httpSession;
 
 	@Inject
 	public UpdateTagHandler(EntityManager entityManager, HttpSession httpSession) {
 		this.entityManager = entityManager;
 		this.httpSession = httpSession;
 	}
 
 	@Transactional
 	@Override
 	public MutationResult execute(UpdateTag action, Dispatcher dispatcher)
 			throws ActionException {
 		User user = (User) httpSession.getAttribute("user");
 		if (user == null) {
 			log.info("denied permission for tag update request (not logged in)");
 			return MutationResult.PERMISSION_DENIED;
 		} else if (!user.isAdmin()) {
 			log.info(
 					"denied permission for tag update request by user \"{}\" (non-admin)",
 					user.getName());
 			return MutationResult.PERMISSION_DENIED;
 		}
 
 		String tagName = action.getTagName();
 		Tag tag = entityManager.find(Tag.class, tagName);
 
 		if (tag == null) {
 			tag = new Tag();
 			tag.setName(tagName);
 		}
 
 		tag.setSearched(action.isSearched());
 
 		Set<Tag> parents = new HashSet<Tag>();
 		for (String parentName : action.getParents()) {
 			Tag parent = entityManager.find(Tag.class, parentName);
 			if (parent == null) {
 				Object[] params = { user.getName(), parentName, tagName };
 				log.info(
 						"rejected attempt by user \"{}\" to add non-existent tag name \"{}\" as parent to tag \"{}\"",
 						params);
 				return MutationResult.INVALID_REQUEST;
 			} else {
 				parents.add(parent);
 			}
 		}
 		tag.setParents(parents);
 
 		entityManager.persist(tag);
 		log.info("user \"{}\" created or updated tag with new values: {}",
 				user.getName(), tag);
 		return MutationResult.SUCCESS;
 	}
 }
