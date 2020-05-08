 package uk.co.tfd.sm.milton.spase;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.lang.StringUtils;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.CommitHandler;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.content.ContentManager;
 import org.sakaiproject.nakamura.api.lite.lock.AlreadyLockedException;
 import org.sakaiproject.nakamura.api.lite.lock.LockState;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.common.ContentTypeUtils;
 import com.bradmcevoy.http.Auth;
 import com.bradmcevoy.http.CollectionResource;
 import com.bradmcevoy.http.FileItem;
 import com.bradmcevoy.http.FileResource;
 import com.bradmcevoy.http.FolderResource;
 import com.bradmcevoy.http.LockInfo;
 import com.bradmcevoy.http.LockInfo.LockDepth;
 import com.bradmcevoy.http.LockInfo.LockScope;
 import com.bradmcevoy.http.LockInfo.LockType;
 import com.bradmcevoy.http.LockResult;
 import com.bradmcevoy.http.LockResult.FailureReason;
 import com.bradmcevoy.http.LockTimeout;
 import com.bradmcevoy.http.LockTimeout.DateAndSeconds;
 import com.bradmcevoy.http.LockToken;
 import com.bradmcevoy.http.LockableResource;
 import com.bradmcevoy.http.LockingCollectionResource;
 import com.bradmcevoy.http.Range;
 import com.bradmcevoy.http.Request;
 import com.bradmcevoy.http.Request.Method;
 import com.bradmcevoy.http.Resource;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.LockedException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.exceptions.PreConditionFailedException;
 import com.bradmcevoy.property.MultiNamespaceCustomPropertyResource;
 import com.bradmcevoy.property.PropertySource.PropertyAccessibility;
 import com.bradmcevoy.property.PropertySource.PropertyMetaData;
 import com.bradmcevoy.property.PropertySource.PropertySetException;
import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 
 public class SparseMiltonContentResource implements FileResource, FolderResource,
 		MultiNamespaceCustomPropertyResource, LockableResource,
 		LockingCollectionResource {
 
 	public static class LockHolder {
 
 		private LockInfo lockInfo;
 		private long timeoutSeconds;
 		private long expiryTime;
 		private String asString;
 		private LockTimeout lockTimeout;
 
 		public LockHolder(LockInfo lockInfo, LockTimeout timeout) {
 			Long timeoutSeconds = timeout.getSeconds();
 			if (timeoutSeconds == null) {
 				timeoutSeconds = 3600L;
 			}
 			DateAndSeconds t = timeout.getLockedUntil(3600L, timeoutSeconds);
 			this.lockInfo = lockInfo;
 			this.lockTimeout = timeout;
 			this.timeoutSeconds = timeoutSeconds;
 			this.expiryTime = t.date.getTime();
 			asString = lockInfo.lockedByUser + ":" + lockInfo.depth + ":"
 					+ lockInfo.scope + ":" + lockInfo.type + ":" + expiryTime
 					+ ":" + timeoutSeconds;
 
 		}
 
 		public LockHolder(String lock, boolean adjustTimeout) {
 			String[] parts = StringUtils.split(lock, ':');
 			this.lockInfo = new LockInfo(LockScope.valueOf(parts[2]),
 					LockType.valueOf(parts[3]), parts[0],
 					LockDepth.valueOf(parts[1]));
 			this.expiryTime = Long.parseLong(parts[4]);
 			if (adjustTimeout) {
 				timeoutSeconds = ((System.currentTimeMillis() - expiryTime) / 1000);
 			} else {
 				timeoutSeconds = Long.parseLong(parts[5]);
 			}
 			this.lockTimeout = new LockTimeout(timeoutSeconds);
 			asString = lockInfo.lockedByUser + ":" + lockInfo.depth + ":"
 					+ lockInfo.scope + ":" + lockInfo.type + ":" + expiryTime
 					+ ":" + timeoutSeconds;
 
 		}
 
 		@Override
 		public String toString() {
 			return asString;
 		}
 
 		public long getTimeoutInSeconds() {
 			return timeoutSeconds;
 		}
 
 		public LockInfo getLockInfo() {
 			return lockInfo;
 		}
 
 		public LockTimeout getLockTimeout() {
 			return lockTimeout;
 		}
 
 	}
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(SparseMiltonContentResource.class);
 	private static final Map<Method, Permission> METHOD_PERMISSIONS = getMethodPermissionMap();
 	private static final Set<Method> PROPERTY_WRITE_METHODS = ImmutableSet
 			.of(Method.PROPPATCH);
 	private static final Set<Method> REDIRECT_METHODS = ImmutableSet.of(
 			Method.ACL, Method.COPY, Method.DELETE, Method.MKCALENDAR,
 			Method.MKCOL, Method.MOVE, Method.POST, Method.PROPPATCH,
 			Method.PUT);
 	private static final Set<Method> WRITE_METHODS = ImmutableSet.of(
 			Method.COPY, Method.ACL, Method.DELETE, Method.MKCALENDAR,
 			Method.MKCOL, Method.MOVE, Method.POST, Method.PROPPATCH,
 			Method.PUT);
 	private static final long LONG_MAX_AGE = 3600L * 24L * 45L;
 
 	private String name;
 	private Repository repository;
 	private String path;
 	private Content content;
 	private Session session;
 	private boolean authorizedToWriteProperties;
 
 	public SparseMiltonContentResource(String name, String path, Session session, Content content) {
 		this.name = name;
 		this.path = path;
 		this.content = content;
 		this.session = session;
 		LOGGER.debug("Created content with content object of {} {} ", this,
 				this.content);
 	}
 
 	public SparseMiltonContentResource(String path, Session session,
 			Content newContent) {
 		this(StorageClientUtils.getObjectName(path), path, session, newContent);
 	}
 
 	private static Map<Method, Permission> getMethodPermissionMap() {
 		Builder<Method, Permission> b = ImmutableMap.builder();
 		b.put(Method.ACL,
 				Permissions.CAN_ANYTHING_ACL.combine(Permissions.CAN_READ));
 		b.put(Method.CONNECT, Permissions.CAN_READ);
 		b.put(Method.COPY, Permissions.CAN_READ); // need to be able to write to
 													// the destination
 		b.put(Method.DELETE,
 				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		b.put(Method.GET, Permissions.CAN_READ);
 		b.put(Method.HEAD, Permissions.CAN_READ);
 		b.put(Method.LOCK, Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		b.put(Method.MKCALENDAR,
 				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		b.put(Method.MKCOL, Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		b.put(Method.MOVE, Permissions.CAN_READ.combine(Permissions.CAN_WRITE)); // need
 																					// to
 																					// check
 																					// destination
 		b.put(Method.OPTIONS, Permissions.CAN_READ);
 		b.put(Method.POST, Permissions.CAN_READ.combine(Permissions.CAN_WRITE)); // might
 																					// need
 																					// more
 																					// here
 		b.put(Method.PROPFIND, Permissions.CAN_READ);
 		b.put(Method.PROPPATCH,
 				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		b.put(Method.PUT, Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		b.put(Method.REPORT, Permissions.CAN_READ);
 		b.put(Method.TRACE, Permissions.CAN_READ);
 		b.put(Method.UNLOCK,
 				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
 		return b.build();
 	}
 
 	public void copyTo(CollectionResource toCollection, String name)
 			throws NotAuthorizedException, BadRequestException,
 			ConflictException {
 		try {
 			String sourcePath = path;
 			String destPath = StorageClientUtils.newPath(
 					((SparseMiltonContentResource) toCollection).getPath(), name);
 			StorageClientUtils.copyTree(session.getContentManager(),
 					sourcePath, destPath, true);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new ConflictException(this, e.getMessage());
 		} catch (AccessDeniedException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new NotAuthorizedException(toCollection);
 		} catch (IOException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new ConflictException(this, e.getMessage());
 		}
 	}
 
 	public String getUniqueId() {
 		LOGGER.debug("Getting Unique ID from {} ", content);
 		return content.getId();
 	}
 
 	public String getName() {
 		LOGGER.debug("Getting name from {} ", content);
 		return name;
 	}
 
 	private String getPath() {
 		return path;
 	}
 
 	public Object authenticate(String user, String password) {
 		LOGGER.debug("Authenticating agains the resource ");
 		try {
 			if (user == null || User.ANON_USER.equals(user)) {
 				return repository.login();
 			}
 			return repository.login(user, password);
 		} catch (ClientPoolException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (AccessDeniedException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return null;
 	}
 
 	public boolean authorise(Request request, Method method, Auth auth) {
 		Session session = (Session) auth.getTag();
 		if (session == null) {
 			LOGGER.debug("Not Authorized, session == null ");
 			return false;
 		}
 		Permission permission = METHOD_PERMISSIONS.get(method);
 		if (permission == null) {
 			LOGGER.debug("Not Authorized, permissions == null ");
 			return false;
 		}
 		try {
 			session.getAccessControlManager().check(Security.ZONE_CONTENT,
 					path, permission);
 			LOGGER.debug("Authorized {} ", permission);
 			authorizedToWriteProperties = PROPERTY_WRITE_METHODS
 					.contains(method);
 			return true;
 		} catch (AccessDeniedException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		LOGGER.debug("Authorize Failed ");
 		return false;
 	}
 
 	public String getRealm() {
 		LOGGER.debug("Get Realm ");
 		return null;
 	}
 
 	public Date getModifiedDate() {
 		LOGGER.debug("Get Modifled ");
 		if (content != null) {
 			if (content.hasProperty(Content.LASTMODIFIED_FIELD)) {
 				return new Date(
 						(Long) content.getProperty(Content.LASTMODIFIED_FIELD));
 			} else {
 				return new Date();
 			}
 		}
 		return new Date(0L);
 	}
 
 	public String checkRedirect(Request request) {
 		LOGGER.debug("Check Redirect ");
 		if (REDIRECT_METHODS.contains(request.getMethod())) {
 			return path;
 		}
 		return null;
 	}
 
 	public void delete() throws NotAuthorizedException, ConflictException,
 			BadRequestException {
 		LOGGER.debug("Delete On {} ", path);
 		try {
 			Iterable<String> i = content.listChildPaths();
 			if (i.iterator().hasNext()) {
 				throw new ConflictException(this,
 						"Cant delete if there are child resources");
 			}
 			session.getContentManager().delete(content.getPath());
 		} catch (AccessDeniedException e) {
 			throw new NotAuthorizedException(this);
 		} catch (StorageClientException e) {
 			throw new BadRequestException(this, e.getMessage());
 		}
 	}
 
 
 	public void sendContent(OutputStream out, Range range,
 			Map<String, String> params, String contentType) throws IOException,
 			NotAuthorizedException, BadRequestException {
 		LOGGER.debug("Send Content ");
 		try {
 			InputStream in;
 			try {
 				in = session.getContentManager().getInputStream(path);
 			} catch (IOException e) {
 				throw new BadRequestException(this, e.getMessage());
 			}
 			if (in == null) {
 				return;
 			}
 			byte[] buffer = new byte[10240];
 			if (range != null) {
 				try {
 					in.skip(range.getStart());
 				} catch (IOException e) {
 					throw new BadRequestException(this, e.getMessage());
 				}
 			}
 			for (;;) {
 				int nr;
 				try {
 					nr = in.read(buffer);
 				} catch (IOException e) {
 					throw new BadRequestException(this, e.getMessage());
 				}
 				if (nr == -1) {
 					break;
 				}
 				out.write(buffer, 0, nr);
 			}
 		} catch (StorageClientException e) {
 			throw new BadRequestException(this, e.getMessage());
 		} catch (AccessDeniedException e) {
 			throw new NotAuthorizedException(this);
 		}
 	}
 
 	public Long getMaxAgeSeconds(Auth auth) {
 		LOGGER.debug("Get Max Age for {} ", auth);
 		Session session = (Session) auth.getTag();
 		if (session == null || User.ANON_USER.equals(session.getUserId())) {
 			return LONG_MAX_AGE;
 		}
 		try {
 			User anonUser = (User) session.getAuthorizableManager()
 					.findAuthorizable(User.ANON_USER);
 			if (session.getAccessControlManager().can(anonUser,
 					Security.ZONE_CONTENT, path, Permissions.CAN_READ)) {
 				return LONG_MAX_AGE;
 			}
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (AccessDeniedException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return null;
 	}
 
 	public String getContentType(String accepts) {
 		LOGGER.debug("Get Content type for {} ", content);
 		if (content == null) {
 			return null;
 		}
 		String contentType = (String) content
 				.getProperty(Content.MIMETYPE_FIELD);
 		if (contentType == null) {
 			return null;
 		}
 		return ContentTypeUtils.findAcceptableContentType(contentType, accepts);
 	}
 
 	public Long getContentLength() {
 		LOGGER.debug("Get Content length {} ", content);
 		if (content == null) {
 			return null;
 		}
 		return (Long) content.getProperty(Content.LENGTH_FIELD);
 	}
 
 	public void moveTo(CollectionResource rDest, String name)
 			throws ConflictException, NotAuthorizedException,
 			BadRequestException {
 		try {
 			String sourcePath = path;
 			String destPath = StorageClientUtils.newPath(
 					((SparseMiltonContentResource) rDest).getPath(), name);
 			LOGGER.debug(
 					"====================================== Moving from {} to {} ",
 					sourcePath, destPath);
 			session.getContentManager().moveWithChildren(sourcePath, destPath);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new ConflictException(this, e.getMessage());
 		} catch (AccessDeniedException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new NotAuthorizedException(rDest);
 		}
 	}
 
 	public String processForm(Map<String, String> parameters,
 			Map<String, FileItem> files) throws BadRequestException,
 			NotAuthorizedException, ConflictException {
 		LOGGER.info("Process form {} {} ", parameters, files);
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Date getCreateDate() {
 		if (content == null) {
 			return null;
 		}
 		Long created = (Long) content.getProperty(Content.CREATED_FIELD);
 		if (created != null) {
 			return new Date(created);
 		}
 		return new Date();
 	}
 
 	public CollectionResource createCollection(String newName)
 			throws NotAuthorizedException, ConflictException,
 			BadRequestException {
 		LOGGER.debug("Create Collection ", newName);
 		try {
 			String newPath = StorageClientUtils.newPath(path, newName);
 			ContentManager contentManager = session.getContentManager();
 			Content newContent = contentManager.get(newPath);
 			if (newContent != null) {
 				throw new ConflictException(this, "Collection already exists");
 			}
 			newContent = new Content(newPath, ImmutableMap.of("collection",
 					(Object) true));
 			contentManager.update(newContent);
 			newContent = contentManager.get(newPath);
 			return new SparseMiltonContentResource(newPath, session, newContent);
 		} catch (StorageClientException e) {
 			throw new BadRequestException(this, e.getMessage());
 		} catch (AccessDeniedException e) {
 			throw new NotAuthorizedException(this);
 		}
 	}
 
 	public Resource child(String childName) {
 		LOGGER.debug("Get Child ", childName);
 		try {
 			String newPath = StorageClientUtils.newPath(path, childName);
 			Content c = session.getContentManager().get(newPath);
 			if (c != null) {
 				return new SparseMiltonContentResource(newPath, session, c);
 			}
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (AccessDeniedException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return null;
 	}
 
 	public List<? extends Resource> getChildren() {
 		// this needs to be disposed by the system.
 		LOGGER.debug("Get Children ");
 		final Iterator<Content> children = content.listChildren().iterator();
		return ImmutableList.copyOf(new PreemptiveIterator<SparseMiltonContentResource>() {
 
 					private SparseMiltonContentResource resource;
 
 					@Override
 					protected boolean internalHasNext() {
 						while (children.hasNext()) {
 							Content n = children.next();
 							if (n != null) {
 								resource = new SparseMiltonContentResource(n
 										.getPath(), session, n);
 								return true;
 							}
 						}
 						resource = null;
 						return false;
 					}
 
 					@Override
 					protected SparseMiltonContentResource internalNext() {
 						return resource;
 					}
 
 				});
 	}
 
 	public Resource createNew(String newName, InputStream inputStream,
 			Long length, String contentType) throws IOException,
 			ConflictException, NotAuthorizedException, BadRequestException {
 		LOGGER.debug("Create new {} ", newName);
 		try {
 			String newPath = StorageClientUtils.newPath(path, newName);
 			ContentManager contentManager = session.getContentManager();
 			Content newContent = contentManager.get(newPath);
 			if (newContent == null) {
 				newContent = new Content(newPath, ImmutableMap.of(
 						Content.MIMETYPE_FIELD, (Object) contentType));
 			} else {
 				newContent.setProperty(Content.MIMETYPE_FIELD,
 						(Object) contentType);
 			}
 			contentManager.update(newContent);
 			contentManager.writeBody(newPath, inputStream);
 			newContent = contentManager.get(newPath);
 			return new SparseMiltonContentResource(newPath, session, newContent);
 		} catch (StorageClientException e) {
 			throw new BadRequestException(this, e.getMessage());
 		} catch (AccessDeniedException e) {
 			throw new NotAuthorizedException(this);
 		}
 	}
 
 	// ========= PROPFIND PROPPATCH Support ===============================
 
 	public Object getProperty(QName name) {
 		String n = getFullName(name);
 		Object o = content.getProperty(n);
 		LOGGER.debug("-------------- GETTING {} as {} --------------", n, o);
 		return o;
 	}
 
 	public void setProperty(QName name, Object value)
 			throws PropertySetException, NotAuthorizedException {
 		String n = getFullName(name);
 		if (value == null) {
 			LOGGER.debug("-------------- REMOVING {} --------------", n);
 			content.removeProperty(n);
 		} else {
 			LOGGER.debug("-------------- SETTING {} as {} --------------", n,
 					value);
 			content.setProperty(n, value);
 		}
 		session.addCommitHandler(content.getId(), new CommitHandler() {
 
 			public void commit() {
 				try {
 					session.getContentManager().update(content);
 				} catch (AccessDeniedException e) {
 					LOGGER.error(e.getMessage());
 				} catch (StorageClientException e) {
 					LOGGER.error(e.getMessage(), e);
 				}
 			}
 		});
 	}
 
 	public PropertyMetaData getPropertyMetaData(QName name) {
 		if (authorizedToWriteProperties) {
 			return new PropertyMetaData(PropertyAccessibility.WRITABLE,
 					Object.class);
 		} else if (content.hasProperty(getFullName(name))) {
 			return new PropertyMetaData(PropertyAccessibility.WRITABLE,
 					Object.class);
 		}
 		return null;
 	}
 
 	private String getFullName(QName name) {
 		String nameSpace = name.getNamespaceURI();
 		if (nameSpace == null || nameSpace.length() == 0) {
 			return "{None}" + name.getLocalPart();
 		}
 		return name.toString();
 	}
 
 	public List<QName> getAllPropertyNames() {
 		List<QName> l = Lists.newArrayList();
 		for (Entry<String, Object> p : content.getProperties().entrySet()) {
 			String name = p.getKey();
 			if (name.startsWith("{")) {
 				int i = name.indexOf('}');
 				l.add(new QName(name.substring(1, i - 1), name.substring(i + 1)));
 			} else {
 				l.add(new QName(name));
 			}
 		}
 		return l;
 	}
 
 	// ========= LOCK Support ===============================
 
 	public LockToken createAndLock(String name, LockTimeout timeout,
 			LockInfo lockInfo) throws NotAuthorizedException {
 		LOGGER.debug("Create And Lock {} {} ", timeout, lockInfo);
 
 		try {
 			String newPath = StorageClientUtils.newPath(path, name);
 			LockHolder lockHolder = new LockHolder(lockInfo, timeout);
 			String token = session.getLockManager().lock(newPath,
 					lockHolder.getTimeoutInSeconds(), lockHolder.toString());
 			return new LockToken(token, lockInfo, timeout);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new NotAuthorizedException(this);
 		} catch (AlreadyLockedException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new NotAuthorizedException(this);
 		}
 	}
 
 	public LockResult lock(LockTimeout timeout, LockInfo lockInfo)
 			throws NotAuthorizedException, PreConditionFailedException,
 			LockedException {
 		try {
 			LockHolder lockHolder = new LockHolder(lockInfo, timeout);
 			String token = session.getLockManager().lock(path,
 					lockHolder.getTimeoutInSeconds(), lockHolder.toString());
 			return LockResult.success(new LockToken(token, lockInfo, timeout));
 		} catch (AlreadyLockedException e) {
 			return LockResult.failed(FailureReason.ALREADY_LOCKED);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new NotAuthorizedException(this);
 		}
 	}
 
 	public LockResult refreshLock(String token) throws NotAuthorizedException,
 			PreConditionFailedException {
 		try {
 			LockState lockState = session.getLockManager().getLockState(path,
 					token);
 			LOGGER.debug("Refreshing lock with {} gave {} ", token, lockState);
 			if (lockState.isOwner() && lockState.hasMatchedToken()
 					&& path.equals(lockState.getLockPath())) {
 				LOGGER.debug("Refreshing lock ");
 				LockHolder lock = new LockHolder(lockState.getExtra(), false);
 				session.getLockManager().refreshLock(path,
 						lock.getTimeoutInSeconds(), lock.toString(), token);
 				LockInfo lockInfo = lock.getLockInfo();
 				LockTimeout timeout = lock.getLockTimeout();
 				return LockResult.success(new LockToken(token, lockInfo,
 						timeout));
 			}
 			LOGGER.debug("Not Refreshing Lock");
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 			throw new NotAuthorizedException(this);
 		}
 		throw new PreConditionFailedException(this);
 	}
 
 	public void unlock(String tokenId) throws NotAuthorizedException,
 			PreConditionFailedException {
 		try {
 			LockState lockState = session.getLockManager().getLockState(path,
 					tokenId);
 			if (lockState.isOwner() && lockState.hasMatchedToken()
 					&& path.equals(lockState.getLockPath())) {
 				session.getLockManager().unlock(path, tokenId);
 				return;
 			}
 		} catch (StorageClientException e) {
 			throw new NotAuthorizedException(this);
 		}
 		throw new PreConditionFailedException(this);
 	}
 
 	public LockToken getCurrentLock() {
 		try {
 			// get the lock regardless, the handlers should deal with locking.
 			LockState lockState = session.getLockManager().getLockState(
 					path, "unknown");
 			if (lockState.isLocked()) {
 				LOGGER.debug(" getCurrentLock() Found Lock {} {} ", path,
 						lockState);
 				String extra = lockState.getExtra();
 				String token = lockState.getToken();
 				LockHolder lockHolder = new LockHolder(extra, true);
 				LockToken lockToken = new LockToken(token, lockHolder.getLockInfo(),
 						lockHolder.getLockTimeout());
 				return new LockToken(token, lockHolder.getLockInfo(),
 						lockHolder.getLockTimeout());
 			}
 			LOGGER.debug("No Lock Present at {} ", path);
 		} catch (StorageClientException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return null;
 	}
 
 
 }
