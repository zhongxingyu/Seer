 package com.artisztikum.ac.cache;
 
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.eclipse.jetty.client.ContentExchange;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.artisztikum.ac.Util;
 import com.artisztikum.ac.ac.Company;
 import com.artisztikum.ac.ac.User;
 import com.artisztikum.ac.httpclient.ACHttpClient;
 
 /**
  * User cache.
  *
  * @author Adam DAJKA (dajka@artisztikum.hu)
  *
  */
 public final class UserCache
 {
 	/**
 	 * Logger.
 	 */
 	private static final Logger LOG = LoggerFactory.getLogger(UserCache.class);
 
 	/**
 	 * {@link JAXBContext} for unmarhalling {@link User}s.
 	 */
 	private static JAXBContext userJC;
 	static {
 		try {
 			userJC = JAXBContext.newInstance(User.class, Company.class);
 		} catch (final JAXBException e1) {
 			throw new RuntimeException(e1);
 		}
 	}
 
 	/**
 	 * User cache. Configurable maximum size.
 	 */
 	private final Map<Integer, User> allUsers = Collections
 			.<Integer, User> synchronizedMap(new LinkedHashMap<Integer, User>() {
 				/**
 				 * Serial.
 				 */
 				private static final long serialVersionUID = 1L;
 
 				private static final int MAX_SIZE = 10000;
 
 				@Override
 				protected boolean removeEldestEntry(final Map.Entry<Integer, User> eldest)
 				{
 					return size() > MAX_SIZE;
 				}
 			});
 
 	/**
 	 * Permalink cache. Configurable maximum size. {@code user_id => permalink}
 	 */
 	private final Map<Integer, String> permalinks = Collections
 			.<Integer, String> synchronizedMap(new LinkedHashMap<Integer, String>() {
 				/**
 				 * Serial.
 				 */
 				private static final long serialVersionUID = 1L;
 
 				private static final int MAX_SIZE = 10000;
 
 				@Override
 				protected boolean removeEldestEntry(final Map.Entry<Integer, String> eldest)
 				{
 					return size() > MAX_SIZE;
 				}
 			});
 
 	/**
 	 * The http client.
 	 */
 	private final ACHttpClient client;
 
 	/**
 	 * @param client
 	 *            The Http client to use.
 	 */
 	private UserCache(final ACHttpClient client)
 	{
 		this.client = client;
 	}
 
 	/**
 	 * Refreshes {@link #permalinks} map for people assigned to a project.
 	 *
 	 * @param projectId
 	 *            The project id.
 	 */
 	private void getPermalinksFromProjectPeople(final Integer projectId)
 	{
 		final ContentExchange ex = client.sendGetWait("/projects/%d/people/", projectId);
 
 		final NodeList nl;
 		try {
 			nl = (NodeList) Util.getXpath("//project_users/project_user").evaluate(
 					Util.getSource(ex.getResponseContent()), XPathConstants.NODESET);
 		} catch (final XPathExpressionException e) {
 			throw new RuntimeException(e);
 		} catch (final UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 
 		for (int i = 0; i < nl.getLength(); i++) {
 			String userId = null;
 			String permalink = null;
 
 			final Node n = nl.item(i);
 			final NodeList children = n.getChildNodes();
 			for (int j = 0; j < children.getLength(); j++) {
 				final Node c = children.item(j);
 
 				if (c.getNodeType() == Node.ELEMENT_NODE) {
 
 					if (c.getNodeName().equals("user_id")) {
 						userId = c.getTextContent();
 					}
 
 					if (c.getNodeName().equals("permalink")) {
 						permalink = c.getTextContent();
 					}
 
 					if (null != userId && null != permalink) {
 						break;
 					}
 				}
 			}
 
 			if (null == userId || null == permalink) {
 				LOG.error("userId [{}] or permalink [{}] is missing", userId, permalink);
 				throw new IllegalStateException("Cannot get userid or permalink");
 			}
 
 			LOG.trace("userId: [{}], permalink: [{}]", userId, permalink);
 
 			final Integer intUserId = Integer.valueOf(userId);
 			final String relPermalink = permalink.substring(client.getApiHost().length());
 
 			if (permalinks.containsKey(intUserId)) {
 				if (!permalinks.get(intUserId).equals(relPermalink)) {
 					// company changed, remove user from the cache
 					LOG.debug("User {} changed permalink from {} to {}. Removing from cache.", new Object[] {
 							intUserId, permalinks.get(intUserId), relPermalink });
 					allUsers.remove(intUserId);
 				}
 			}
 			permalinks.put(intUserId, relPermalink);
 
 		}
 	}
 
 	/**
 	 * Gets the {@link User} with {@code userId}. {@code projectId} is used to get the permalink (containing the company
 	 * id) for the given user.
 	 *
 	 * @param projectId
 	 *            The project id.
 	 * @param userId
 	 *            The user id.
 	 * @return The {@link User} or {@code null}, if the user is not associated with the given project.
 	 */
 	public User getUserByProject(final Integer projectId, final Integer userId)
 	{
		if (null != userId && Integer.valueOf(userId) == 0) {
			return null;
		}
		
 		final User r1 = getUserById(userId);
 		if (null != r1) {
 			LOG.trace("User {} found in cache", userId);
 			return r1;
 		}
 
 		getPermalinksFromProjectPeople(projectId);
 
 		if (!permalinks.containsKey(userId)) {
 			LOG.warn("User {} is not associated with project {}", userId, projectId);
 			return null;
 		}
 
 		return getUserByPermalink(permalinks.get(userId));
 	}
 
 	/**
 	 * Gets a {@link User} from the cache.
 	 *
 	 * @param userId
 	 *            The {@code user_id}.
 	 * @return The {@link User} or {@code null} if not in cache.
 	 */
 	public User getUserById(final Integer userId)
 	{
 		return allUsers.get(userId);
 	}
 
 	/**
 	 * Gets a {@link User} directly from the Active Collab API.
 	 *
 	 * @param permalink
 	 *            The {@code path_info} parameter for the User.
 	 * @return The {@link User}
 	 */
 	public User getUserByPermalink(final String permalink)
 	{
 		LOG.debug("Fetching {}", permalink);
 		final ContentExchange ex = client.sendGetWait(permalink);
 
 		User user;
 		try {
 			user = unmarshalUser(new StreamSource(new StringReader(ex.getResponseContent())));
 		} catch (final UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 		allUsers.put(user.getId(), user);
 		return user;
 	}
 
 	/**
 	 * Singleton instance.
 	 */
 	private static UserCache singleton;
 
 	/**
 	 * Initializes singleton instance.
 	 *
 	 * @param client
 	 *            Constructor parameter for {@link #UserCache(ACHttpClient)}.
 	 * @throws IllegalStateException
 	 *             when already initialized.
 	 */
 	public static void init(final ACHttpClient client) throws IllegalStateException
 	{
 		if (null != singleton) {
 			throw new IllegalStateException("Already initialized");
 		}
 
 		singleton = new UserCache(client);
 	}
 
 	/**
 	 * @return the singleton instance.
 	 * @throws IllegalStateException
 	 *             when not initialized yet with {@link #init(ACHttpClient)}.
 	 */
 	public static UserCache get() throws IllegalStateException
 	{
 		if (null == singleton) {
 			throw new IllegalStateException("UserCache not initialized yet!");
 		}
 		return singleton;
 	}
 
 	/**
 	 * Unmarshals a {@link Source} into an {@link User} with {@link #userJC}.
 	 *
 	 * @param src
 	 *            The {@link Source}
 	 * @return The unmarshalled {@link User} instance.
 	 */
 	public static User unmarshalUser(final Source src)
 	{
 		try {
 			final Unmarshaller u = userJC.createUnmarshaller();
 			final JAXBElement<User> doc = u.unmarshal(src, User.class);
 			return doc.getValue();
 		} catch (final JAXBException e) {
 			throw new RuntimeException("Error de-serializing user", e);
 		}
 	}
 
 }
