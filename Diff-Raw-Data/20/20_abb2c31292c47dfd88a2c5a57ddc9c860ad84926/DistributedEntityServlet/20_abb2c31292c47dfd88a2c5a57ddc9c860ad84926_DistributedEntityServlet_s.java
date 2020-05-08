 /*
  * DistributedEntityServlet.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.central;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.subitarius.domain.DistributedEntity;
 import com.subitarius.util.logging.InjectLogger;
 
 /**
  * Provides access to {@link DistributedEntity} objects stored on the server.
  * The {@code GET} method is used both to retrieve both the entity list and
  * specific entities; it is the only supported method.
  * 
  * @author Meyer Kizner
  * @see #doGet(HttpServletRequest, HttpServletResponse)
  * 
  */
 final class DistributedEntityServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	@InjectLogger
 	private Logger log;
 
 	private final Provider<EntityManager> entityManagerProvider;
 
 	@Inject
 	private DistributedEntityServlet(
 			Provider<EntityManager> entityManagerProvider) {
 		this.entityManagerProvider = entityManagerProvider;
 	}
 
 	/**
 	 * Returns either a list of available entities (with hashes) or the
 	 * serialized form of a specified entity. For this method, the
 	 * {@linkplain HttpServletRequest#getPathInfo() path info} part of the
 	 * request URI is used to request specific entities.
 	 * <p>
 	 * 
 	 * If the path info is empty or simply a single slash character, the
 	 * response will consist of a list of entity hashes available to the client.
 	 * The list consists of each hash, as a hexadecimal string, on its own line;
 	 * the lines are separated by newline characters only.
 	 * <p>
 	 * 
 	 * If the path info starts with a slash but is followed by a character
 	 * string, that string is interpreted as the hash of a requested specific
 	 * entity. No validation is attempted on the entity hash. If an entity is
 	 * found with the hash, its serialized form is written as the response body
 	 * along with status code 200 (OK). Otherwise, status code 404 (not found)
 	 * is returned to the client.
 	 * <p>
 	 * 
 	 * In any other case (if there is one!), status code 400 (bad request) is
 	 * sent.
 	 */
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse res)
 			throws IOException, ServletException {
 		String pathInfo = req.getPathInfo();
		if (pathInfo.isEmpty() || pathInfo.equals("/")) {
 			PrintWriter writer = res.getWriter();
 			int count = 0;
 			for (String hash : fetchHashes()) {
 				writer.println(hash);
 				count++;
 			}
 			writer.flush();
 			log.info("returned {} entity hashes", count);
 		} else if (pathInfo.startsWith("/")) {
 			String hash = pathInfo.substring(1);
 			EntityManager entityManager = entityManagerProvider.get();
 			DistributedEntity entity = entityManager.find(
 					DistributedEntity.class, hash);
 			if (entity == null) {
 				log.info("entity hash not found: {}", hash);
 				res.sendError(HttpServletResponse.SC_NOT_FOUND);
 			} else {
 				OutputStream stream = res.getOutputStream();
 				ObjectOutputStream oos = new ObjectOutputStream(stream);
 				oos.writeObject(entity);
 				stream.flush();
 				log.info("entity found and returned: {}", hash);
 			}
 		} else {
 			log.info("bad request; path info: {}", pathInfo);
 			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 
 	/**
 	 * @return an {@code Iterable} of all entity hashes from the
 	 *         {@link DistributedEntity} table
 	 */
 	private Iterable<String> fetchHashes() {
 		EntityManager entityManager = entityManagerProvider.get();
 
 		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
 		CriteriaQuery<DistributedEntity> criteria = builder
 				.createQuery(DistributedEntity.class);
 		Root<DistributedEntity> root = criteria.from(DistributedEntity.class);
 		criteria.select(root);
 		Iterable<DistributedEntity> entities = entityManager.createQuery(
 				criteria).getResultList();
 
 		return Iterables.transform(entities,
 				new Function<DistributedEntity, String>() {
 					@Override
 					public String apply(DistributedEntity input) {
 						return input.getHash();
 					}
 				});
 	}
 }
