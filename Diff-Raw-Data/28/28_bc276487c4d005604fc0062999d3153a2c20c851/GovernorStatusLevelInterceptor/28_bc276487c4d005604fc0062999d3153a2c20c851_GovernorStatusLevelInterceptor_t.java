 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @authors tag. All rights reserved.
  */
 package org.jboss.sbs.spam.interceptor;
 
 import com.jivesoftware.base.User;
 import com.jivesoftware.cache.Cache;
 import com.jivesoftware.community.*;
 import com.jivesoftware.community.annotations.PropertyNames;
 import com.jivesoftware.community.cache.CacheFactory;
 import com.jivesoftware.community.cache.CacheParameters;
 import com.jivesoftware.community.interceptor.GovernorInterceptor;
 import com.jivesoftware.community.lifecycle.JiveApplication;
 import com.jivesoftware.util.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.regex.Pattern;
 
 /**
  * Extended implementation of {@link GovernorInterceptor} which allows users with better status level to have no post
  * time restriction
  *
  * @author Libor Krzyzanek
  */
 @PropertyNames({"postInterval", "pointsLevel", "rejectionMessage", "emailDomainWhitelist"})
 public class GovernorStatusLevelInterceptor implements JiveInterceptor {
 
 	private static final Logger log = Logger.getLogger(GovernorStatusLevelInterceptor.class);
 
 	private int pointsLevel = 10;
 
 	private int postInterval = 30;
 
 	private String rejectionMessage = "Not allowed to post content more than once every {0} seconds.";
 
 	protected String emailDomainWhitelist;
 
 	protected String[] emailDomainAllowed;
 
 	private String cacheName;
 
 	private final static Pattern MSG_FORMAT_TOKEN = Pattern.compile(".*\\{[0-9]*\\}.*");
 
 	/**
 	 * @see GovernorInterceptor
 	 */
 	@SuppressWarnings("rawtypes")
 	private static ConcurrentMap<String, Cache> postLimitCacheRegistry = new ConcurrentHashMap<String, Cache>();
 
 	public GovernorStatusLevelInterceptor() {
 	}
 
 	public GovernorStatusLevelInterceptor(int objectType, long objectID) {
 		cacheName = "Post Limit Status Level Cache - " + objectType + "-" + objectID;
 		initCache();
 	}
 
 	@SuppressWarnings("rawtypes")
 	private void initCache() {
 		// Init standard SBS Cache
 		CacheParameters config = new CacheParameters(cacheName);
 		config.setExpirationPeriod((postInterval / 2) * JiveConstants.SECOND);
 		config.setExpirationTime(postInterval * JiveConstants.SECOND);
 
 		if (log.isDebugEnabled()) {
 			log.debug("Init Cache Configuration: " + config);
 		}
 
 		Cache userCache = CacheFactory.createCache(config);
 
 		postLimitCacheRegistry.put(cacheName, userCache);
 	}
 
 	public int getPostInterval() {
 		return this.postInterval;
 	}
 
 	/**
 	 * @see GovernorInterceptor#setPostInterval(int)
 	 */
 	public void setPostInterval(int postInterval) {
 		if (postInterval < 1) {
 			this.postInterval = 30;
 		} else {
 			this.postInterval = postInterval;
 		}
 		initCache();
 	}
 
 	@Override
 	public void invokeInterceptor(JiveObject object, Type type) throws RejectedException {
 		if (object instanceof JiveContentObject) {
 			JiveContentObject content = (JiveContentObject) object;
			User author = null;
			if (object.getObjectType() == JiveConstants.DOCUMENT) {
				// Issue #14
				// For document in Jive 6 can be checked only initial version.
				Document document = (Document) object;
				if (log.isTraceEnabled()) {
					log.trace("Document: " + document);
				}
				if (document.getVersionID() <= 0) {
					author = document.getUser();
				} else {
					// There is no way how to get who did this edit.
					// document.getLatestVersionAuthor(); returns first author though.
					return;
				}
			} else {
				author = content.getUser();
			}

			if (log.isDebugEnabled()) {
				log.debug("Author: " + author);
			}


 			if (author != null && !author.isAnonymous()) {
 				long points = JiveApplication.getContext().getStatusLevelManager().getPointLevel(author);
 
 				if (log.isTraceEnabled()) {
 					log.trace("Author '" + author.getUsername() + "' has points: " + points + ". Points level is: "
 							+ pointsLevel + ", emailDomainWhitelist: " + emailDomainWhitelist);
 				}
 
				if (emailDomainAllowed != null && emailDomainAllowed.length > 0 && author.getEmail() != null) {
 					for (String emailAllowed : emailDomainAllowed) {
 						if (author.getEmail().endsWith(emailAllowed)) {
 							log.debug("Author has e-mail on domain whitelist. Will not be affected by this interceptor.");
 							return;
 						}
 					}
 				}
 
 
 				if (points > pointsLevel) {
 					log.debug("Author has more points than limit. Will not be affected by this interceptor.");
 					return;
 				}
 				log.debug("Going to check the post limit.");
 				processContent(author.getID(), content);
 			}
 		}
 	}
 
 	/**
 	 * @see GovernorInterceptor
 	 */
 	@SuppressWarnings("unchecked")
 	private void processContent(Long userID, JiveObject content) throws RejectedException {
 		if (postLimitCacheRegistry.get(cacheName).get(userID) != null) {
 			log.trace("User is in cache");
 
 			String rejectMsg = getRejectionMessage();
 			if (MSG_FORMAT_TOKEN.matcher(getRejectionMessage()).matches()) {
 				rejectMsg = StringUtils.replace(rejectMsg, "'", "''");
 			}
 			rejectMsg = MessageFormat.format(rejectMsg,
 					(java.lang.Object[]) new String[]{Integer.toString(postInterval)});
 			throw new RejectedException(rejectMsg, content);
 		} else {
 			log.trace("User is NOT in cache");
 			postLimitCacheRegistry.get(cacheName).put(userID, userID);
 		}
 	}
 
 	public int getPointsLevel() {
 		return pointsLevel;
 	}
 
 	public void setPointsLevel(int pointsLevel) {
 		if (log.isTraceEnabled()) {
 			log.trace("Setting points level to: " + pointsLevel);
 		}
 		this.pointsLevel = pointsLevel;
 	}
 
 	public String getEmailDomainWhitelist() {
 		return emailDomainWhitelist;
 	}
 
 	public void setEmailDomainWhitelist(String emailDomainWhitelist) {
 		if (log.isTraceEnabled()) {
 			log.trace("Setting emailDomainWhitelist to: " + emailDomainWhitelist);
 		}
 		this.emailDomainWhitelist = emailDomainWhitelist;
 		if (StringUtils.isNotBlank(emailDomainWhitelist)) {
 			emailDomainAllowed = StringUtils.split(emailDomainWhitelist);
 		} else {
 			emailDomainAllowed = null;
 		}
 	}
 
 	public String getRejectionMessage() {
 		return rejectionMessage;
 	}
 
 	public void setRejectionMessage(String rejectionMessage) {
 		this.rejectionMessage = rejectionMessage;
 	}
 
 	@Override
 	public List<Type> getTypes() {
 		return new ArrayList<Type>() {
 			private static final long serialVersionUID = 1L;
 
 			{
 				add(Type.TYPE_PRE);
 			}
 		};
 	}
 
 	@Override
 	public boolean isSystemLevel() {
 		return false;
 	}
 
 }
