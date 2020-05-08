 /*
  * DeleteTagHandler.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.instance.server.action;
 
 import javax.persistence.EntityManager;
 import javax.servlet.http.HttpSession;
 
 import org.apache.catalina.User;
 import org.slf4j.Logger;
 
 import com.google.inject.Inject;
 import com.google.inject.persist.Transactional;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
 import com.prealpha.extempdb.instance.shared.action.DeleteTag;
 import com.prealpha.extempdb.instance.shared.action.MutationResult;
 import com.prealpha.extempdb.util.logging.InjectLogger;
 
 class DeleteTagHandler implements ActionHandler<DeleteTag, MutationResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final HttpSession httpSession;
 
 	@Inject
 	public DeleteTagHandler(EntityManager entityManager, HttpSession httpSession) {
 		this.entityManager = entityManager;
 		this.httpSession = httpSession;
 	}
 
 	@Transactional
 	@Override
 	public MutationResult execute(DeleteTag action, Dispatcher dispatcher)
 			throws ActionException {
 		String tagName = action.getTagName();
 		User user = (User) httpSession.getAttribute("user");
 
 		if (user == null) {
 			log.info("denied permission to delete tag \"{}\" (not logged in)",
 					tagName);
 			return MutationResult.PERMISSION_DENIED;
 		} else if (!user.isAdmin()) {
 			log.info(
 					"denied permission to delete tag \"{}\" to user \"{}\" (non-admin)",
 					tagName, user.getName());
 			return MutationResult.PERMISSION_DENIED;
 		}
 
 		Tag tag = entityManager.find(Tag.class, tagName);
 
 		if (tag == null) {
 			log.info(
 					"rejected request from user \"{}\" to delete non-existent tag \"{}\"",
 					user.getName(), tagName);
 			return MutationResult.INVALID_REQUEST;
 		} else {
 			entityManager.remove(tag);
 			log.info("user \"{}\" deleted tag \"{}\"", user.getName(), tagName);
 			return MutationResult.SUCCESS;
 		}
 	}
 }
