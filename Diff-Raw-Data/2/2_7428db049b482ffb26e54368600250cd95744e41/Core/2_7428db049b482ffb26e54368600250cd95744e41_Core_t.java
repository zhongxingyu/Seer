 /*
  * Sone - Core.java - Copyright © 2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.sone.core;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.pterodactylus.sone.core.Options.DefaultOption;
 import net.pterodactylus.sone.core.Options.Option;
 import net.pterodactylus.sone.core.Options.OptionWatcher;
 import net.pterodactylus.sone.data.Album;
 import net.pterodactylus.sone.data.Client;
 import net.pterodactylus.sone.data.Image;
 import net.pterodactylus.sone.data.Post;
 import net.pterodactylus.sone.data.Profile;
 import net.pterodactylus.sone.data.Profile.Field;
 import net.pterodactylus.sone.data.Reply;
 import net.pterodactylus.sone.data.Sone;
 import net.pterodactylus.sone.data.TemporaryImage;
 import net.pterodactylus.sone.freenet.wot.Identity;
 import net.pterodactylus.sone.freenet.wot.IdentityListener;
 import net.pterodactylus.sone.freenet.wot.IdentityManager;
 import net.pterodactylus.sone.freenet.wot.OwnIdentity;
 import net.pterodactylus.sone.freenet.wot.Trust;
 import net.pterodactylus.sone.freenet.wot.WebOfTrustException;
 import net.pterodactylus.sone.main.SonePlugin;
 import net.pterodactylus.util.config.Configuration;
 import net.pterodactylus.util.config.ConfigurationException;
 import net.pterodactylus.util.logging.Logging;
 import net.pterodactylus.util.number.Numbers;
 import net.pterodactylus.util.validation.Validation;
 import net.pterodactylus.util.version.Version;
 import freenet.keys.FreenetURI;
 
 /**
  * The Sone core.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Core implements IdentityListener, UpdateListener, ImageInsertListener {
 
 	/**
 	 * Enumeration for the possible states of a {@link Sone}.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public enum SoneStatus {
 
 		/** The Sone is unknown, i.e. not yet downloaded. */
 		unknown,
 
 		/** The Sone is idle, i.e. not being downloaded or inserted. */
 		idle,
 
 		/** The Sone is currently being inserted. */
 		inserting,
 
 		/** The Sone is currently being downloaded. */
 		downloading,
 	}
 
 	/** The logger. */
 	private static final Logger logger = Logging.getLogger(Core.class);
 
 	/** The options. */
 	private final Options options = new Options();
 
 	/** The preferences. */
 	private final Preferences preferences = new Preferences(options);
 
 	/** The core listener manager. */
 	private final CoreListenerManager coreListenerManager = new CoreListenerManager(this);
 
 	/** The configuration. */
 	private Configuration configuration;
 
 	/** Whether we’re currently saving the configuration. */
 	private boolean storingConfiguration = false;
 
 	/** The identity manager. */
 	private final IdentityManager identityManager;
 
 	/** Interface to freenet. */
 	private final FreenetInterface freenetInterface;
 
 	/** The Sone downloader. */
 	private final SoneDownloader soneDownloader;
 
 	/** The image inserter. */
 	private final ImageInserter imageInserter;
 
 	/** The update checker. */
 	private final UpdateChecker updateChecker;
 
 	/** Whether the core has been stopped. */
 	private volatile boolean stopped;
 
 	/** The Sones’ statuses. */
 	/* synchronize access on itself. */
 	private final Map<Sone, SoneStatus> soneStatuses = new HashMap<Sone, SoneStatus>();
 
 	/** Locked local Sones. */
 	/* synchronize on itself. */
 	private final Set<Sone> lockedSones = new HashSet<Sone>();
 
 	/** Sone inserters. */
 	/* synchronize access on this on localSones. */
 	private final Map<Sone, SoneInserter> soneInserters = new HashMap<Sone, SoneInserter>();
 
 	/** All local Sones. */
 	/* synchronize access on this on itself. */
 	private Map<String, Sone> localSones = new HashMap<String, Sone>();
 
 	/** All remote Sones. */
 	/* synchronize access on this on itself. */
 	private Map<String, Sone> remoteSones = new HashMap<String, Sone>();
 
 	/** All new Sones. */
 	private Set<String> newSones = new HashSet<String>();
 
 	/** All known Sones. */
 	/* synchronize access on {@link #newSones}. */
 	private Set<String> knownSones = new HashSet<String>();
 
 	/** All posts. */
 	private Map<String, Post> posts = new HashMap<String, Post>();
 
 	/** All new posts. */
 	private Set<String> newPosts = new HashSet<String>();
 
 	/** All known posts. */
 	/* synchronize access on {@link #newPosts}. */
 	private Set<String> knownPosts = new HashSet<String>();
 
 	/** All replies. */
 	private Map<String, Reply> replies = new HashMap<String, Reply>();
 
 	/** All new replies. */
 	private Set<String> newReplies = new HashSet<String>();
 
 	/** All known replies. */
 	private Set<String> knownReplies = new HashSet<String>();
 
 	/** All bookmarked posts. */
 	/* synchronize access on itself. */
 	private Set<String> bookmarkedPosts = new HashSet<String>();
 
 	/** Trusted identities, sorted by own identities. */
 	private Map<OwnIdentity, Set<Identity>> trustedIdentities = Collections.synchronizedMap(new HashMap<OwnIdentity, Set<Identity>>());
 
 	/** All known albums. */
 	private Map<String, Album> albums = new HashMap<String, Album>();
 
 	/** All known images. */
 	private Map<String, Image> images = new HashMap<String, Image>();
 
 	/** All temporary images. */
 	private Map<String, TemporaryImage> temporaryImages = new HashMap<String, TemporaryImage>();
 
 	/**
 	 * Creates a new core.
 	 *
 	 * @param configuration
 	 *            The configuration of the core
 	 * @param freenetInterface
 	 *            The freenet interface
 	 * @param identityManager
 	 *            The identity manager
 	 */
 	public Core(Configuration configuration, FreenetInterface freenetInterface, IdentityManager identityManager) {
 		this.configuration = configuration;
 		this.freenetInterface = freenetInterface;
 		this.identityManager = identityManager;
 		this.soneDownloader = new SoneDownloader(this, freenetInterface);
 		this.imageInserter = new ImageInserter(this, freenetInterface);
 		this.updateChecker = new UpdateChecker(freenetInterface);
 	}
 
 	//
 	// LISTENER MANAGEMENT
 	//
 
 	/**
 	 * Adds a new core listener.
 	 *
 	 * @param coreListener
 	 *            The listener to add
 	 */
 	public void addCoreListener(CoreListener coreListener) {
 		coreListenerManager.addListener(coreListener);
 	}
 
 	/**
 	 * Removes a core listener.
 	 *
 	 * @param coreListener
 	 *            The listener to remove
 	 */
 	public void removeCoreListener(CoreListener coreListener) {
 		coreListenerManager.removeListener(coreListener);
 	}
 
 	//
 	// ACCESSORS
 	//
 
 	/**
 	 * Sets the configuration to use. This will automatically save the current
 	 * configuration to the given configuration.
 	 *
 	 * @param configuration
 	 *            The new configuration to use
 	 */
 	public void setConfiguration(Configuration configuration) {
 		this.configuration = configuration;
 		saveConfiguration();
 	}
 
 	/**
 	 * Returns the options used by the core.
 	 *
 	 * @return The options of the core
 	 */
 	public Preferences getPreferences() {
 		return preferences;
 	}
 
 	/**
 	 * Returns the identity manager used by the core.
 	 *
 	 * @return The identity manager
 	 */
 	public IdentityManager getIdentityManager() {
 		return identityManager;
 	}
 
 	/**
 	 * Returns the update checker.
 	 *
 	 * @return The update checker
 	 */
 	public UpdateChecker getUpdateChecker() {
 		return updateChecker;
 	}
 
 	/**
 	 * Returns the status of the given Sone.
 	 *
 	 * @param sone
 	 *            The Sone to get the status for
 	 * @return The status of the Sone
 	 */
 	public SoneStatus getSoneStatus(Sone sone) {
 		synchronized (soneStatuses) {
 			return soneStatuses.get(sone);
 		}
 	}
 
 	/**
 	 * Sets the status of the given Sone.
 	 *
 	 * @param sone
 	 *            The Sone to set the status of
 	 * @param soneStatus
 	 *            The status to set
 	 */
 	public void setSoneStatus(Sone sone, SoneStatus soneStatus) {
 		synchronized (soneStatuses) {
 			soneStatuses.put(sone, soneStatus);
 		}
 	}
 
 	/**
 	 * Returns whether the given Sone is currently locked.
 	 *
 	 * @param sone
 	 *            The sone to check
 	 * @return {@code true} if the Sone is locked, {@code false} if it is not
 	 */
 	public boolean isLocked(Sone sone) {
 		synchronized (lockedSones) {
 			return lockedSones.contains(sone);
 		}
 	}
 
 	/**
 	 * Returns all Sones, remote and local.
 	 *
 	 * @return All Sones
 	 */
 	public Set<Sone> getSones() {
 		Set<Sone> allSones = new HashSet<Sone>();
 		allSones.addAll(getLocalSones());
 		allSones.addAll(getRemoteSones());
 		return allSones;
 	}
 
 	/**
 	 * Returns the Sone with the given ID, regardless whether it’s local or
 	 * remote.
 	 *
 	 * @param id
 	 *            The ID of the Sone to get
 	 * @return The Sone with the given ID, or {@code null} if there is no such
 	 *         Sone
 	 */
 	public Sone getSone(String id) {
 		return getSone(id, true);
 	}
 
 	/**
 	 * Returns the Sone with the given ID, regardless whether it’s local or
 	 * remote.
 	 *
 	 * @param id
 	 *            The ID of the Sone to get
 	 * @param create
 	 *            {@code true} to create a new Sone if none exists,
 	 *            {@code false} to return {@code null} if a Sone with the given
 	 *            ID does not exist
 	 * @return The Sone with the given ID, or {@code null} if there is no such
 	 *         Sone
 	 */
 	public Sone getSone(String id, boolean create) {
 		if (isLocalSone(id)) {
 			return getLocalSone(id);
 		}
 		return getRemoteSone(id, create);
 	}
 
 	/**
 	 * Checks whether the core knows a Sone with the given ID.
 	 *
 	 * @param id
 	 *            The ID of the Sone
 	 * @return {@code true} if there is a Sone with the given ID, {@code false}
 	 *         otherwise
 	 */
 	public boolean hasSone(String id) {
 		return isLocalSone(id) || isRemoteSone(id);
 	}
 
 	/**
 	 * Returns whether the given Sone is a local Sone.
 	 *
 	 * @param sone
 	 *            The Sone to check for its locality
 	 * @return {@code true} if the given Sone is local, {@code false} otherwise
 	 */
 	public boolean isLocalSone(Sone sone) {
 		synchronized (localSones) {
 			return localSones.containsKey(sone.getId());
 		}
 	}
 
 	/**
 	 * Returns whether the given ID is the ID of a local Sone.
 	 *
 	 * @param id
 	 *            The Sone ID to check for its locality
 	 * @return {@code true} if the given ID is a local Sone, {@code false}
 	 *         otherwise
 	 */
 	public boolean isLocalSone(String id) {
 		synchronized (localSones) {
 			return localSones.containsKey(id);
 		}
 	}
 
 	/**
 	 * Returns all local Sones.
 	 *
 	 * @return All local Sones
 	 */
 	public Set<Sone> getLocalSones() {
 		synchronized (localSones) {
 			return new HashSet<Sone>(localSones.values());
 		}
 	}
 
 	/**
 	 * Returns the local Sone with the given ID.
 	 *
 	 * @param id
 	 *            The ID of the Sone to get
 	 * @return The Sone with the given ID
 	 */
 	public Sone getLocalSone(String id) {
 		return getLocalSone(id, true);
 	}
 
 	/**
 	 * Returns the local Sone with the given ID, optionally creating a new Sone.
 	 *
 	 * @param id
 	 *            The ID of the Sone
 	 * @param create
 	 *            {@code true} to create a new Sone if none exists,
 	 *            {@code false} to return null if none exists
 	 * @return The Sone with the given ID, or {@code null}
 	 */
 	public Sone getLocalSone(String id, boolean create) {
 		synchronized (localSones) {
 			Sone sone = localSones.get(id);
 			if ((sone == null) && create) {
 				sone = new Sone(id);
 				localSones.put(id, sone);
 				setSoneStatus(sone, SoneStatus.unknown);
 			}
 			return sone;
 		}
 	}
 
 	/**
 	 * Returns all remote Sones.
 	 *
 	 * @return All remote Sones
 	 */
 	public Set<Sone> getRemoteSones() {
 		synchronized (remoteSones) {
 			return new HashSet<Sone>(remoteSones.values());
 		}
 	}
 
 	/**
 	 * Returns the remote Sone with the given ID.
 	 *
 	 * @param id
 	 *            The ID of the remote Sone to get
 	 * @return The Sone with the given ID
 	 */
 	public Sone getRemoteSone(String id) {
 		return getRemoteSone(id, true);
 	}
 
 	/**
 	 * Returns the remote Sone with the given ID.
 	 *
 	 * @param id
 	 *            The ID of the remote Sone to get
 	 * @param create
 	 *            {@code true} to always create a Sone, {@code false} to return
 	 *            {@code null} if no Sone with the given ID exists
 	 * @return The Sone with the given ID
 	 */
 	public Sone getRemoteSone(String id, boolean create) {
 		synchronized (remoteSones) {
 			Sone sone = remoteSones.get(id);
 			if ((sone == null) && create) {
 				sone = new Sone(id);
 				remoteSones.put(id, sone);
 				setSoneStatus(sone, SoneStatus.unknown);
 			}
 			return sone;
 		}
 	}
 
 	/**
 	 * Returns whether the given Sone is a remote Sone.
 	 *
 	 * @param sone
 	 *            The Sone to check
 	 * @return {@code true} if the given Sone is a remote Sone, {@code false}
 	 *         otherwise
 	 */
 	public boolean isRemoteSone(Sone sone) {
 		synchronized (remoteSones) {
 			return remoteSones.containsKey(sone.getId());
 		}
 	}
 
 	/**
 	 * Returns whether the Sone with the given ID is a remote Sone.
 	 *
 	 * @param id
 	 *            The ID of the Sone to check
 	 * @return {@code true} if the Sone with the given ID is a remote Sone,
 	 *         {@code false} otherwise
 	 */
 	public boolean isRemoteSone(String id) {
 		synchronized (remoteSones) {
 			return remoteSones.containsKey(id);
 		}
 	}
 
 	/**
 	 * Returns whether the Sone with the given ID is a new Sone.
 	 *
 	 * @param soneId
 	 *            The ID of the sone to check for
 	 * @return {@code true} if the given Sone is new, false otherwise
 	 */
 	public boolean isNewSone(String soneId) {
 		synchronized (newSones) {
 			return !knownSones.contains(soneId) && newSones.contains(soneId);
 		}
 	}
 
 	/**
 	 * Returns whether the given Sone has been modified.
 	 *
 	 * @param sone
 	 *            The Sone to check for modifications
 	 * @return {@code true} if a modification has been detected in the Sone,
 	 *         {@code false} otherwise
 	 */
 	public boolean isModifiedSone(Sone sone) {
 		return (soneInserters.containsKey(sone)) ? soneInserters.get(sone).isModified() : false;
 	}
 
 	/**
 	 * Returns whether the target Sone is trusted by the origin Sone.
 	 *
 	 * @param origin
 	 *            The origin Sone
 	 * @param target
 	 *            The target Sone
 	 * @return {@code true} if the target Sone is trusted by the origin Sone
 	 */
 	public boolean isSoneTrusted(Sone origin, Sone target) {
 		return trustedIdentities.containsKey(origin) && trustedIdentities.get(origin.getIdentity()).contains(target);
 	}
 
 	/**
 	 * Returns the post with the given ID.
 	 *
 	 * @param postId
 	 *            The ID of the post to get
 	 * @return The post, or {@code null} if there is no such post
 	 */
 	public Post getPost(String postId) {
 		return getPost(postId, true);
 	}
 
 	/**
 	 * Returns the post with the given ID, optionally creating a new post.
 	 *
 	 * @param postId
 	 *            The ID of the post to get
 	 * @param create
 	 *            {@code true} it create a new post if no post with the given ID
 	 *            exists, {@code false} to return {@code null}
 	 * @return The post, or {@code null} if there is no such post
 	 */
 	public Post getPost(String postId, boolean create) {
 		synchronized (posts) {
 			Post post = posts.get(postId);
 			if ((post == null) && create) {
 				post = new Post(postId);
 				posts.put(postId, post);
 			}
 			return post;
 		}
 	}
 
 	/**
 	 * Returns whether the given post ID is new.
 	 *
 	 * @param postId
 	 *            The post ID
 	 * @return {@code true} if the post is considered to be new, {@code false}
 	 *         otherwise
 	 */
 	public boolean isNewPost(String postId) {
 		synchronized (newPosts) {
 			return !knownPosts.contains(postId) && newPosts.contains(postId);
 		}
 	}
 
 	/**
 	 * Returns the reply with the given ID. If there is no reply with the given
 	 * ID yet, a new one is created.
 	 *
 	 * @param replyId
 	 *            The ID of the reply to get
 	 * @return The reply
 	 */
 	public Reply getReply(String replyId) {
 		return getReply(replyId, true);
 	}
 
 	/**
 	 * Returns the reply with the given ID. If there is no reply with the given
 	 * ID yet, a new one is created, unless {@code create} is false in which
 	 * case {@code null} is returned.
 	 *
 	 * @param replyId
 	 *            The ID of the reply to get
 	 * @param create
 	 *            {@code true} to always return a {@link Reply}, {@code false}
 	 *            to return {@code null} if no reply can be found
 	 * @return The reply, or {@code null} if there is no such reply
 	 */
 	public Reply getReply(String replyId, boolean create) {
 		synchronized (replies) {
 			Reply reply = replies.get(replyId);
 			if (create && (reply == null)) {
 				reply = new Reply(replyId);
 				replies.put(replyId, reply);
 			}
 			return reply;
 		}
 	}
 
 	/**
 	 * Returns all replies for the given post, order ascending by time.
 	 *
 	 * @param post
 	 *            The post to get all replies for
 	 * @return All replies for the given post
 	 */
 	public List<Reply> getReplies(Post post) {
 		Set<Sone> sones = getSones();
 		List<Reply> replies = new ArrayList<Reply>();
 		for (Sone sone : sones) {
 			for (Reply reply : sone.getReplies()) {
 				if (reply.getPost().equals(post)) {
 					replies.add(reply);
 				}
 			}
 		}
 		Collections.sort(replies, Reply.TIME_COMPARATOR);
 		return replies;
 	}
 
 	/**
 	 * Returns whether the reply with the given ID is new.
 	 *
 	 * @param replyId
 	 *            The ID of the reply to check
 	 * @return {@code true} if the reply is considered to be new, {@code false}
 	 *         otherwise
 	 */
 	public boolean isNewReply(String replyId) {
 		synchronized (newReplies) {
 			return !knownReplies.contains(replyId) && newReplies.contains(replyId);
 		}
 	}
 
 	/**
 	 * Returns all Sones that have liked the given post.
 	 *
 	 * @param post
 	 *            The post to get the liking Sones for
 	 * @return The Sones that like the given post
 	 */
 	public Set<Sone> getLikes(Post post) {
 		Set<Sone> sones = new HashSet<Sone>();
 		for (Sone sone : getSones()) {
 			if (sone.getLikedPostIds().contains(post.getId())) {
 				sones.add(sone);
 			}
 		}
 		return sones;
 	}
 
 	/**
 	 * Returns all Sones that have liked the given reply.
 	 *
 	 * @param reply
 	 *            The reply to get the liking Sones for
 	 * @return The Sones that like the given reply
 	 */
 	public Set<Sone> getLikes(Reply reply) {
 		Set<Sone> sones = new HashSet<Sone>();
 		for (Sone sone : getSones()) {
 			if (sone.getLikedReplyIds().contains(reply.getId())) {
 				sones.add(sone);
 			}
 		}
 		return sones;
 	}
 
 	/**
 	 * Returns whether the given post is bookmarked.
 	 *
 	 * @param post
 	 *            The post to check
 	 * @return {@code true} if the given post is bookmarked, {@code false}
 	 *         otherwise
 	 */
 	public boolean isBookmarked(Post post) {
 		return isPostBookmarked(post.getId());
 	}
 
 	/**
 	 * Returns whether the post with the given ID is bookmarked.
 	 *
 	 * @param id
 	 *            The ID of the post to check
 	 * @return {@code true} if the post with the given ID is bookmarked,
 	 *         {@code false} otherwise
 	 */
 	public boolean isPostBookmarked(String id) {
 		synchronized (bookmarkedPosts) {
 			return bookmarkedPosts.contains(id);
 		}
 	}
 
 	/**
 	 * Returns all currently known bookmarked posts.
 	 *
 	 * @return All bookmarked posts
 	 */
 	public Set<Post> getBookmarkedPosts() {
 		Set<Post> posts = new HashSet<Post>();
 		synchronized (bookmarkedPosts) {
 			for (String bookmarkedPostId : bookmarkedPosts) {
 				Post post = getPost(bookmarkedPostId, false);
 				if (post != null) {
 					posts.add(post);
 				}
 			}
 		}
 		return posts;
 	}
 
 	/**
 	 * Returns the album with the given ID, creating a new album if no album
 	 * with the given ID can be found.
 	 *
 	 * @param albumId
 	 *            The ID of the album
 	 * @return The album with the given ID
 	 */
 	public Album getAlbum(String albumId) {
 		return getAlbum(albumId, true);
 	}
 
 	/**
 	 * Returns the album with the given ID, optionally creating a new album if
 	 * an album with the given ID can not be found.
 	 *
 	 * @param albumId
 	 *            The ID of the album
 	 * @param create
 	 *            {@code true} to create a new album if none exists for the
 	 *            given ID
 	 * @return The album with the given ID, or {@code null} if no album with the
 	 *         given ID exists and {@code create} is {@code false}
 	 */
 	public Album getAlbum(String albumId, boolean create) {
 		synchronized (albums) {
 			Album album = albums.get(albumId);
 			if (create && (album == null)) {
 				album = new Album(albumId);
 				albums.put(albumId, album);
 			}
 			return album;
 		}
 	}
 
 	/**
 	 * Returns the image with the given ID, creating it if necessary.
 	 *
 	 * @param imageId
 	 *            The ID of the image
 	 * @return The image with the given ID
 	 */
 	public Image getImage(String imageId) {
 		return getImage(imageId, true);
 	}
 
 	/**
 	 * Returns the image with the given ID, optionally creating it if it does
 	 * not exist.
 	 *
 	 * @param imageId
 	 *            The ID of the image
 	 * @param create
 	 *            {@code true} to create an image if none exists with the given
 	 *            ID
 	 * @return The image with the given ID, or {@code null} if none exists and
 	 *         none was created
 	 */
 	public Image getImage(String imageId, boolean create) {
 		synchronized (images) {
 			Image image = images.get(imageId);
 			if (create && (image == null)) {
 				image = new Image(imageId);
 				images.put(imageId, image);
 			}
 			return image;
 		}
 	}
 
 	/**
 	 * Returns the temporary image with the given ID.
 	 *
 	 * @param imageId
 	 *            The ID of the temporary image
 	 * @return The temporary image, or {@code null} if there is no temporary
 	 *         image with the given ID
 	 */
 	public TemporaryImage getTemporaryImage(String imageId) {
 		synchronized (temporaryImages) {
 			return temporaryImages.get(imageId);
 		}
 	}
 
 	//
 	// ACTIONS
 	//
 
 	/**
 	 * Locks the given Sone. A locked Sone will not be inserted by
 	 * {@link SoneInserter} until it is {@link #unlockSone(Sone) unlocked}
 	 * again.
 	 *
 	 * @param sone
 	 *            The sone to lock
 	 */
 	public void lockSone(Sone sone) {
 		synchronized (lockedSones) {
 			if (lockedSones.add(sone)) {
 				coreListenerManager.fireSoneLocked(sone);
 			}
 		}
 	}
 
 	/**
 	 * Unlocks the given Sone.
 	 *
 	 * @see #lockSone(Sone)
 	 * @param sone
 	 *            The sone to unlock
 	 */
 	public void unlockSone(Sone sone) {
 		synchronized (lockedSones) {
 			if (lockedSones.remove(sone)) {
 				coreListenerManager.fireSoneUnlocked(sone);
 			}
 		}
 	}
 
 	/**
 	 * Adds a local Sone from the given ID which has to be the ID of an own
 	 * identity.
 	 *
 	 * @param id
 	 *            The ID of an own identity to add a Sone for
 	 * @return The added (or already existing) Sone
 	 */
 	public Sone addLocalSone(String id) {
 		synchronized (localSones) {
 			if (localSones.containsKey(id)) {
 				logger.log(Level.FINE, "Tried to add known local Sone: %s", id);
 				return localSones.get(id);
 			}
 			OwnIdentity ownIdentity = identityManager.getOwnIdentity(id);
 			if (ownIdentity == null) {
 				logger.log(Level.INFO, "Invalid Sone ID: %s", id);
 				return null;
 			}
 			return addLocalSone(ownIdentity);
 		}
 	}
 
 	/**
 	 * Adds a local Sone from the given own identity.
 	 *
 	 * @param ownIdentity
 	 *            The own identity to create a Sone from
 	 * @return The added (or already existing) Sone
 	 */
 	public Sone addLocalSone(OwnIdentity ownIdentity) {
 		if (ownIdentity == null) {
 			logger.log(Level.WARNING, "Given OwnIdentity is null!");
 			return null;
 		}
 		synchronized (localSones) {
 			final Sone sone;
 			try {
 				sone = getLocalSone(ownIdentity.getId()).setIdentity(ownIdentity).setInsertUri(new FreenetURI(ownIdentity.getInsertUri())).setRequestUri(new FreenetURI(ownIdentity.getRequestUri()));
 			} catch (MalformedURLException mue1) {
 				logger.log(Level.SEVERE, "Could not convert the Identity’s URIs to Freenet URIs: " + ownIdentity.getInsertUri() + ", " + ownIdentity.getRequestUri(), mue1);
 				return null;
 			}
 			sone.setLatestEdition(Numbers.safeParseLong(ownIdentity.getProperty("Sone.LatestEdition"), (long) 0));
 			sone.setClient(new Client("Sone", SonePlugin.VERSION.toString()));
 			/* TODO - load posts ’n stuff */
 			localSones.put(ownIdentity.getId(), sone);
 			final SoneInserter soneInserter = new SoneInserter(this, freenetInterface, sone);
 			soneInserters.put(sone, soneInserter);
 			setSoneStatus(sone, SoneStatus.idle);
 			loadSone(sone);
 			if (!preferences.isSoneRescueMode()) {
 				soneInserter.start();
 			}
 			new Thread(new Runnable() {
 
 				@Override
 				@SuppressWarnings("synthetic-access")
 				public void run() {
 					if (!preferences.isSoneRescueMode()) {
 						soneDownloader.fetchSone(sone);
 						return;
 					}
 					logger.log(Level.INFO, "Trying to restore Sone from Freenet…");
 					coreListenerManager.fireRescuingSone(sone);
 					lockSone(sone);
 					long edition = sone.getLatestEdition();
 					while (!stopped && (edition >= 0) && preferences.isSoneRescueMode()) {
 						logger.log(Level.FINE, "Downloading edition " + edition + "…");
 						soneDownloader.fetchSone(sone, sone.getRequestUri().setKeyType("SSK").setDocName("Sone-" + edition));
 						--edition;
 					}
 					logger.log(Level.INFO, "Finished restoring Sone from Freenet, starting Inserter…");
 					saveSone(sone);
 					coreListenerManager.fireRescuedSone(sone);
 					soneInserter.start();
 				}
 
 			}, "Sone Downloader").start();
 			return sone;
 		}
 	}
 
 	/**
 	 * Creates a new Sone for the given own identity.
 	 *
 	 * @param ownIdentity
 	 *            The own identity to create a Sone for
 	 * @return The created Sone
 	 */
 	public Sone createSone(OwnIdentity ownIdentity) {
 		try {
 			ownIdentity.addContext("Sone");
 		} catch (WebOfTrustException wote1) {
 			logger.log(Level.SEVERE, "Could not add “Sone” context to own identity: " + ownIdentity, wote1);
 			return null;
 		}
 		Sone sone = addLocalSone(ownIdentity);
 		return sone;
 	}
 
 	/**
 	 * Adds the Sone of the given identity.
 	 *
 	 * @param identity
 	 *            The identity whose Sone to add
 	 * @return The added or already existing Sone
 	 */
 	public Sone addRemoteSone(Identity identity) {
 		if (identity == null) {
 			logger.log(Level.WARNING, "Given Identity is null!");
 			return null;
 		}
 		synchronized (remoteSones) {
 			final Sone sone = getRemoteSone(identity.getId()).setIdentity(identity);
 			boolean newSone = sone.getRequestUri() == null;
 			sone.setRequestUri(getSoneUri(identity.getRequestUri()));
 			sone.setLatestEdition(Numbers.safeParseLong(identity.getProperty("Sone.LatestEdition"), (long) 0));
 			if (newSone) {
 				synchronized (newSones) {
 					newSone = !knownSones.contains(sone.getId());
 					if (newSone) {
 						newSones.add(sone.getId());
 					}
 				}
 				if (newSone) {
 					coreListenerManager.fireNewSoneFound(sone);
 				}
 			}
 			remoteSones.put(identity.getId(), sone);
 			soneDownloader.addSone(sone);
 			setSoneStatus(sone, SoneStatus.unknown);
 			new Thread(new Runnable() {
 
 				@Override
 				@SuppressWarnings("synthetic-access")
 				public void run() {
 					soneDownloader.fetchSone(sone);
 				}
 
 			}, "Sone Downloader").start();
 			return sone;
 		}
 	}
 
 	/**
 	 * Retrieves the trust relationship from the origin to the target. If the
 	 * trust relationship can not be retrieved, {@code null} is returned.
 	 *
 	 * @see Identity#getTrust(OwnIdentity)
 	 * @param origin
 	 *            The origin of the trust tree
 	 * @param target
 	 *            The target of the trust
 	 * @return The trust relationship
 	 */
 	public Trust getTrust(Sone origin, Sone target) {
 		if (!isLocalSone(origin)) {
 			logger.log(Level.WARNING, "Tried to get trust from remote Sone: %s", origin);
 			return null;
 		}
 		return target.getIdentity().getTrust((OwnIdentity) origin.getIdentity());
 	}
 
 	/**
 	 * Sets the trust value of the given origin Sone for the target Sone.
 	 *
 	 * @param origin
 	 *            The origin Sone
 	 * @param target
 	 *            The target Sone
 	 * @param trustValue
 	 *            The trust value (from {@code -100} to {@code 100})
 	 */
 	public void setTrust(Sone origin, Sone target, int trustValue) {
 		Validation.begin().isNotNull("Trust Origin", origin).check().isInstanceOf("Trust Origin", origin.getIdentity(), OwnIdentity.class).isNotNull("Trust Target", target).isLessOrEqual("Trust Value", trustValue, 100).isGreaterOrEqual("Trust Value", trustValue, -100).check();
 		try {
 			((OwnIdentity) origin.getIdentity()).setTrust(target.getIdentity(), trustValue, preferences.getTrustComment());
 		} catch (WebOfTrustException wote1) {
 			logger.log(Level.WARNING, "Could not set trust for Sone: " + target, wote1);
 		}
 	}
 
 	/**
 	 * Removes any trust assignment for the given target Sone.
 	 *
 	 * @param origin
 	 *            The trust origin
 	 * @param target
 	 *            The trust target
 	 */
 	public void removeTrust(Sone origin, Sone target) {
 		Validation.begin().isNotNull("Trust Origin", origin).isNotNull("Trust Target", target).check().isInstanceOf("Trust Origin Identity", origin.getIdentity(), OwnIdentity.class).check();
 		try {
 			((OwnIdentity) origin.getIdentity()).removeTrust(target.getIdentity());
 		} catch (WebOfTrustException wote1) {
 			logger.log(Level.WARNING, "Could not remove trust for Sone: " + target, wote1);
 		}
 	}
 
 	/**
 	 * Assigns the configured positive trust value for the given target.
 	 *
 	 * @param origin
 	 *            The trust origin
 	 * @param target
 	 *            The trust target
 	 */
 	public void trustSone(Sone origin, Sone target) {
 		setTrust(origin, target, preferences.getPositiveTrust());
 	}
 
 	/**
 	 * Assigns the configured negative trust value for the given target.
 	 *
 	 * @param origin
 	 *            The trust origin
 	 * @param target
 	 *            The trust target
 	 */
 	public void distrustSone(Sone origin, Sone target) {
 		setTrust(origin, target, preferences.getNegativeTrust());
 	}
 
 	/**
 	 * Removes the trust assignment for the given target.
 	 *
 	 * @param origin
 	 *            The trust origin
 	 * @param target
 	 *            The trust target
 	 */
 	public void untrustSone(Sone origin, Sone target) {
 		removeTrust(origin, target);
 	}
 
 	/**
 	 * Updates the stores Sone with the given Sone.
 	 *
 	 * @param sone
 	 *            The updated Sone
 	 */
 	public void updateSone(Sone sone) {
 		if (hasSone(sone.getId())) {
 			boolean soneRescueMode = isLocalSone(sone) && preferences.isSoneRescueMode();
 			Sone storedSone = getSone(sone.getId());
 			if (!soneRescueMode && !(sone.getTime() > storedSone.getTime())) {
 				logger.log(Level.FINE, "Downloaded Sone %s is not newer than stored Sone %s.", new Object[] { sone, storedSone });
 				return;
 			}
 			synchronized (posts) {
 				if (!soneRescueMode) {
 					for (Post post : storedSone.getPosts()) {
 						posts.remove(post.getId());
 						if (!sone.getPosts().contains(post)) {
 							coreListenerManager.firePostRemoved(post);
 						}
 					}
 				}
 				List<Post> storedPosts = storedSone.getPosts();
 				synchronized (newPosts) {
 					for (Post post : sone.getPosts()) {
 						post.setSone(storedSone);
 						if (!storedPosts.contains(post) && !knownPosts.contains(post.getId())) {
 							newPosts.add(post.getId());
 							coreListenerManager.fireNewPostFound(post);
 						}
 						posts.put(post.getId(), post);
 					}
 				}
 			}
 			synchronized (replies) {
 				if (!soneRescueMode) {
 					for (Reply reply : storedSone.getReplies()) {
 						replies.remove(reply.getId());
 						if (!sone.getReplies().contains(reply)) {
 							coreListenerManager.fireReplyRemoved(reply);
 						}
 					}
 				}
 				Set<Reply> storedReplies = storedSone.getReplies();
 				synchronized (newReplies) {
 					for (Reply reply : sone.getReplies()) {
 						reply.setSone(storedSone);
 						if (!storedReplies.contains(reply) && !knownReplies.contains(reply.getId())) {
 							newReplies.add(reply.getId());
 							coreListenerManager.fireNewReplyFound(reply);
 						}
 						replies.put(reply.getId(), reply);
 					}
 				}
 			}
 			synchronized (storedSone) {
 				if (!soneRescueMode || (sone.getTime() > storedSone.getTime())) {
 					storedSone.setTime(sone.getTime());
 				}
 				storedSone.setClient(sone.getClient());
 				storedSone.setProfile(sone.getProfile());
 				if (soneRescueMode) {
 					for (Post post : sone.getPosts()) {
 						storedSone.addPost(post);
 					}
 					for (Reply reply : sone.getReplies()) {
 						storedSone.addReply(reply);
 					}
 					for (String likedPostId : sone.getLikedPostIds()) {
 						storedSone.addLikedPostId(likedPostId);
 					}
 					for (String likedReplyId : sone.getLikedReplyIds()) {
 						storedSone.addLikedReplyId(likedReplyId);
 					}
 				} else {
 					storedSone.setPosts(sone.getPosts());
 					storedSone.setReplies(sone.getReplies());
 					storedSone.setLikePostIds(sone.getLikedPostIds());
 					storedSone.setLikeReplyIds(sone.getLikedReplyIds());
 				}
 				storedSone.setLatestEdition(sone.getLatestEdition());
 			}
 		}
 	}
 
 	/**
 	 * Deletes the given Sone. This will remove the Sone from the
 	 * {@link #getLocalSone(String) local Sones}, stops its {@link SoneInserter}
 	 * and remove the context from its identity.
 	 *
 	 * @param sone
 	 *            The Sone to delete
 	 */
 	public void deleteSone(Sone sone) {
 		if (!(sone.getIdentity() instanceof OwnIdentity)) {
 			logger.log(Level.WARNING, "Tried to delete Sone of non-own identity: %s", sone);
 			return;
 		}
 		synchronized (localSones) {
 			if (!localSones.containsKey(sone.getId())) {
 				logger.log(Level.WARNING, "Tried to delete non-local Sone: %s", sone);
 				return;
 			}
 			localSones.remove(sone.getId());
 			soneInserters.remove(sone).stop();
 		}
 		try {
 			((OwnIdentity) sone.getIdentity()).removeContext("Sone");
 			((OwnIdentity) sone.getIdentity()).removeProperty("Sone.LatestEdition");
 		} catch (WebOfTrustException wote1) {
 			logger.log(Level.WARNING, "Could not remove context and properties from Sone: " + sone, wote1);
 		}
 		try {
 			configuration.getLongValue("Sone/" + sone.getId() + "/Time").setValue(null);
 		} catch (ConfigurationException ce1) {
 			logger.log(Level.WARNING, "Could not remove Sone from configuration!", ce1);
 		}
 	}
 
 	/**
 	 * Marks the given Sone as known. If the Sone was {@link #isNewPost(String)
 	 * new} before, a {@link CoreListener#markSoneKnown(Sone)} event is fired.
 	 *
 	 * @param sone
 	 *            The Sone to mark as known
 	 */
 	public void markSoneKnown(Sone sone) {
 		synchronized (newSones) {
 			if (newSones.remove(sone.getId())) {
 				knownSones.add(sone.getId());
 				coreListenerManager.fireMarkSoneKnown(sone);
 				saveConfiguration();
 			}
 		}
 	}
 
 	/**
 	 * Loads and updates the given Sone from the configuration. If any error is
 	 * encountered, loading is aborted and the given Sone is not changed.
 	 *
 	 * @param sone
 	 *            The Sone to load and update
 	 */
 	public void loadSone(Sone sone) {
 		if (!isLocalSone(sone)) {
 			logger.log(Level.FINE, "Tried to load non-local Sone: %s", sone);
 			return;
 		}
 
 		/* load Sone. */
 		String sonePrefix = "Sone/" + sone.getId();
 		Long soneTime = configuration.getLongValue(sonePrefix + "/Time").getValue(null);
 		if (soneTime == null) {
 			logger.log(Level.INFO, "Could not load Sone because no Sone has been saved.");
 			return;
 		}
 		String lastInsertFingerprint = configuration.getStringValue(sonePrefix + "/LastInsertFingerprint").getValue("");
 
 		/* load profile. */
 		Profile profile = new Profile();
 		profile.setFirstName(configuration.getStringValue(sonePrefix + "/Profile/FirstName").getValue(null));
 		profile.setMiddleName(configuration.getStringValue(sonePrefix + "/Profile/MiddleName").getValue(null));
 		profile.setLastName(configuration.getStringValue(sonePrefix + "/Profile/LastName").getValue(null));
 		profile.setBirthDay(configuration.getIntValue(sonePrefix + "/Profile/BirthDay").getValue(null));
 		profile.setBirthMonth(configuration.getIntValue(sonePrefix + "/Profile/BirthMonth").getValue(null));
 		profile.setBirthYear(configuration.getIntValue(sonePrefix + "/Profile/BirthYear").getValue(null));
 
 		/* load profile fields. */
 		while (true) {
 			String fieldPrefix = sonePrefix + "/Profile/Fields/" + profile.getFields().size();
 			String fieldName = configuration.getStringValue(fieldPrefix + "/Name").getValue(null);
 			if (fieldName == null) {
 				break;
 			}
 			String fieldValue = configuration.getStringValue(fieldPrefix + "/Value").getValue("");
 			profile.addField(fieldName).setValue(fieldValue);
 		}
 
 		/* load posts. */
 		Set<Post> posts = new HashSet<Post>();
 		while (true) {
 			String postPrefix = sonePrefix + "/Posts/" + posts.size();
 			String postId = configuration.getStringValue(postPrefix + "/ID").getValue(null);
 			if (postId == null) {
 				break;
 			}
 			String postRecipientId = configuration.getStringValue(postPrefix + "/Recipient").getValue(null);
 			long postTime = configuration.getLongValue(postPrefix + "/Time").getValue((long) 0);
 			String postText = configuration.getStringValue(postPrefix + "/Text").getValue(null);
 			if ((postTime == 0) || (postText == null)) {
 				logger.log(Level.WARNING, "Invalid post found, aborting load!");
 				return;
 			}
 			Post post = getPost(postId).setSone(sone).setTime(postTime).setText(postText);
 			if ((postRecipientId != null) && (postRecipientId.length() == 43)) {
 				post.setRecipient(getSone(postRecipientId));
 			}
 			posts.add(post);
 		}
 
 		/* load replies. */
 		Set<Reply> replies = new HashSet<Reply>();
 		while (true) {
 			String replyPrefix = sonePrefix + "/Replies/" + replies.size();
 			String replyId = configuration.getStringValue(replyPrefix + "/ID").getValue(null);
 			if (replyId == null) {
 				break;
 			}
 			String postId = configuration.getStringValue(replyPrefix + "/Post/ID").getValue(null);
 			long replyTime = configuration.getLongValue(replyPrefix + "/Time").getValue((long) 0);
 			String replyText = configuration.getStringValue(replyPrefix + "/Text").getValue(null);
 			if ((postId == null) || (replyTime == 0) || (replyText == null)) {
 				logger.log(Level.WARNING, "Invalid reply found, aborting load!");
 				return;
 			}
 			replies.add(getReply(replyId).setSone(sone).setPost(getPost(postId)).setTime(replyTime).setText(replyText));
 		}
 
 		/* load post likes. */
 		Set<String> likedPostIds = new HashSet<String>();
 		while (true) {
 			String likedPostId = configuration.getStringValue(sonePrefix + "/Likes/Post/" + likedPostIds.size() + "/ID").getValue(null);
 			if (likedPostId == null) {
 				break;
 			}
 			likedPostIds.add(likedPostId);
 		}
 
 		/* load reply likes. */
 		Set<String> likedReplyIds = new HashSet<String>();
 		while (true) {
 			String likedReplyId = configuration.getStringValue(sonePrefix + "/Likes/Reply/" + likedReplyIds.size() + "/ID").getValue(null);
 			if (likedReplyId == null) {
 				break;
 			}
 			likedReplyIds.add(likedReplyId);
 		}
 
 		/* load friends. */
 		Set<String> friends = new HashSet<String>();
 		while (true) {
 			String friendId = configuration.getStringValue(sonePrefix + "/Friends/" + friends.size() + "/ID").getValue(null);
 			if (friendId == null) {
 				break;
 			}
 			friends.add(friendId);
 		}
 
 		/* load albums. */
 		List<Album> topLevelAlbums = new ArrayList<Album>();
 		int albumCounter = 0;
 		while (true) {
 			String albumPrefix = sonePrefix + "/Albums/" + albumCounter++;
 			String albumId = configuration.getStringValue(albumPrefix + "/ID").getValue(null);
 			if (albumId == null) {
 				break;
 			}
 			String albumTitle = configuration.getStringValue(albumPrefix + "/Title").getValue(null);
 			String albumDescription = configuration.getStringValue(albumPrefix + "/Description").getValue(null);
 			String albumParentId = configuration.getStringValue(albumPrefix + "/Parent").getValue(null);
 			if ((albumTitle == null) || (albumDescription == null)) {
 				logger.log(Level.WARNING, "Invalid album found, aborting load!");
 				return;
 			}
 			Album album = getAlbum(albumId).setSone(sone).setTitle(albumTitle).setDescription(albumDescription);
 			if (albumParentId != null) {
 				Album parentAlbum = getAlbum(albumParentId, false);
 				if (parentAlbum == null) {
 					logger.log(Level.WARNING, "Invalid parent album ID: " + albumParentId);
 					return;
 				}
 				parentAlbum.addAlbum(album);
 			} else {
 				topLevelAlbums.add(album);
 			}
 		}
 
 		/* load images. */
 		int imageCounter = 0;
 		while (true) {
 			String imagePrefix = sonePrefix + "/Images/" + imageCounter++;
 			String imageId = configuration.getStringValue(imagePrefix + "/ID").getValue(null);
 			if (imageId == null) {
 				break;
 			}
 			String albumId = configuration.getStringValue(imagePrefix + "/Album").getValue(null);
 			String key = configuration.getStringValue(imagePrefix + "/Key").getValue(null);
 			String title = configuration.getStringValue(imagePrefix + "/Title").getValue(null);
 			String description = configuration.getStringValue(imagePrefix + "/Description").getValue(null);
 			Long creationTime = configuration.getLongValue(imagePrefix + "/CreationTime").getValue(null);
 			Integer width = configuration.getIntValue(imagePrefix + "/Width").getValue(null);
 			Integer height = configuration.getIntValue(imagePrefix + "/Height").getValue(null);
 			if ((albumId == null) || (key == null) || (title == null) || (description == null) || (creationTime == null) || (width == null) || (height == null)) {
 				logger.log(Level.WARNING, "Invalid image found, aborting load!");
 				return;
 			}
 			Album album = getAlbum(albumId, false);
 			if (album == null) {
 				logger.log(Level.WARNING, "Invalid album image encountered, aborting load!");
 				return;
 			}
 			Image image = getImage(imageId).setSone(sone).setCreationTime(creationTime).setKey(key);
 			image.setTitle(title).setDescription(description).setWidth(width).setHeight(height);
 			album.addImage(image);
 		}
 
 		/* load options. */
 		sone.getOptions().addBooleanOption("AutoFollow", new DefaultOption<Boolean>(false));
 		sone.getOptions().getBooleanOption("AutoFollow").set(configuration.getBooleanValue(sonePrefix + "/Options/AutoFollow").getValue(null));
 
 		/* if we’re still here, Sone was loaded successfully. */
 		synchronized (sone) {
 			sone.setTime(soneTime);
 			sone.setProfile(profile);
 			sone.setPosts(posts);
 			sone.setReplies(replies);
 			sone.setLikePostIds(likedPostIds);
 			sone.setLikeReplyIds(likedReplyIds);
 			sone.setFriends(friends);
 			sone.setAlbums(topLevelAlbums);
 			soneInserters.get(sone).setLastInsertFingerprint(lastInsertFingerprint);
 		}
 		synchronized (newSones) {
 			for (String friend : friends) {
 				knownSones.add(friend);
 			}
 		}
 		synchronized (newPosts) {
 			for (Post post : posts) {
 				knownPosts.add(post.getId());
 			}
 		}
 		synchronized (newReplies) {
 			for (Reply reply : replies) {
 				knownReplies.add(reply.getId());
 			}
 		}
 	}
 
 	/**
 	 * Saves the given Sone. This will persist all local settings for the given
 	 * Sone, such as the friends list and similar, private options.
 	 *
 	 * @param sone
 	 *            The Sone to save
 	 */
 	public synchronized void saveSone(Sone sone) {
 		if (!isLocalSone(sone)) {
 			logger.log(Level.FINE, "Tried to save non-local Sone: %s", sone);
 			return;
 		}
 		if (!(sone.getIdentity() instanceof OwnIdentity)) {
 			logger.log(Level.WARNING, "Local Sone without OwnIdentity found, refusing to save: %s", sone);
 			return;
 		}
 
 		logger.log(Level.INFO, "Saving Sone: %s", sone);
 		try {
 			((OwnIdentity) sone.getIdentity()).setProperty("Sone.LatestEdition", String.valueOf(sone.getLatestEdition()));
 
 			/* save Sone into configuration. */
 			String sonePrefix = "Sone/" + sone.getId();
 			configuration.getLongValue(sonePrefix + "/Time").setValue(sone.getTime());
 			configuration.getStringValue(sonePrefix + "/LastInsertFingerprint").setValue(soneInserters.get(sone).getLastInsertFingerprint());
 
 			/* save profile. */
 			Profile profile = sone.getProfile();
 			configuration.getStringValue(sonePrefix + "/Profile/FirstName").setValue(profile.getFirstName());
 			configuration.getStringValue(sonePrefix + "/Profile/MiddleName").setValue(profile.getMiddleName());
 			configuration.getStringValue(sonePrefix + "/Profile/LastName").setValue(profile.getLastName());
 			configuration.getIntValue(sonePrefix + "/Profile/BirthDay").setValue(profile.getBirthDay());
 			configuration.getIntValue(sonePrefix + "/Profile/BirthMonth").setValue(profile.getBirthMonth());
 			configuration.getIntValue(sonePrefix + "/Profile/BirthYear").setValue(profile.getBirthYear());
 
 			/* save profile fields. */
 			int fieldCounter = 0;
 			for (Field profileField : profile.getFields()) {
 				String fieldPrefix = sonePrefix + "/Profile/Fields/" + fieldCounter++;
 				configuration.getStringValue(fieldPrefix + "/Name").setValue(profileField.getName());
 				configuration.getStringValue(fieldPrefix + "/Value").setValue(profileField.getValue());
 			}
 			configuration.getStringValue(sonePrefix + "/Profile/Fields/" + fieldCounter + "/Name").setValue(null);
 
 			/* save posts. */
 			int postCounter = 0;
 			for (Post post : sone.getPosts()) {
 				String postPrefix = sonePrefix + "/Posts/" + postCounter++;
 				configuration.getStringValue(postPrefix + "/ID").setValue(post.getId());
 				configuration.getStringValue(postPrefix + "/Recipient").setValue((post.getRecipient() != null) ? post.getRecipient().getId() : null);
 				configuration.getLongValue(postPrefix + "/Time").setValue(post.getTime());
 				configuration.getStringValue(postPrefix + "/Text").setValue(post.getText());
 			}
 			configuration.getStringValue(sonePrefix + "/Posts/" + postCounter + "/ID").setValue(null);
 
 			/* save replies. */
 			int replyCounter = 0;
 			for (Reply reply : sone.getReplies()) {
 				String replyPrefix = sonePrefix + "/Replies/" + replyCounter++;
 				configuration.getStringValue(replyPrefix + "/ID").setValue(reply.getId());
 				configuration.getStringValue(replyPrefix + "/Post/ID").setValue(reply.getPost().getId());
 				configuration.getLongValue(replyPrefix + "/Time").setValue(reply.getTime());
 				configuration.getStringValue(replyPrefix + "/Text").setValue(reply.getText());
 			}
 			configuration.getStringValue(sonePrefix + "/Replies/" + replyCounter + "/ID").setValue(null);
 
 			/* save post likes. */
 			int postLikeCounter = 0;
 			for (String postId : sone.getLikedPostIds()) {
 				configuration.getStringValue(sonePrefix + "/Likes/Post/" + postLikeCounter++ + "/ID").setValue(postId);
 			}
 			configuration.getStringValue(sonePrefix + "/Likes/Post/" + postLikeCounter + "/ID").setValue(null);
 
 			/* save reply likes. */
 			int replyLikeCounter = 0;
 			for (String replyId : sone.getLikedReplyIds()) {
 				configuration.getStringValue(sonePrefix + "/Likes/Reply/" + replyLikeCounter++ + "/ID").setValue(replyId);
 			}
 			configuration.getStringValue(sonePrefix + "/Likes/Reply/" + replyLikeCounter + "/ID").setValue(null);
 
 			/* save friends. */
 			int friendCounter = 0;
 			for (String friendId : sone.getFriends()) {
 				configuration.getStringValue(sonePrefix + "/Friends/" + friendCounter++ + "/ID").setValue(friendId);
 			}
 			configuration.getStringValue(sonePrefix + "/Friends/" + friendCounter + "/ID").setValue(null);
 
 			/* save albums. first, collect in a flat structure, top-level first. */
 			List<Album> albums = new ArrayList<Album>();
 			albums.addAll(sone.getAlbums());
 			int lastAlbumIndex = 0;
 			while (lastAlbumIndex < albums.size()) {
 				int previousAlbumCount = albums.size();
 				for (Album album : new ArrayList<Album>(albums.subList(lastAlbumIndex, albums.size()))) {
 					albums.addAll(album.getAlbums());
 				}
 				lastAlbumIndex = previousAlbumCount;
 			}
 
 			int albumCounter = 0;
 			for (Album album : albums) {
 				String albumPrefix = sonePrefix + "/Albums/" + albumCounter++;
 				configuration.getStringValue(albumPrefix + "/ID").setValue(album.getId());
 				configuration.getStringValue(albumPrefix + "/Title").setValue(album.getTitle());
 				configuration.getStringValue(albumPrefix + "/Description").setValue(album.getDescription());
 				configuration.getStringValue(albumPrefix + "/Parent").setValue(album.getParent() == null ? null : album.getParent().getId());
 			}
 			configuration.getStringValue(sonePrefix + "/Albums/" + albumCounter + "/ID").setValue(null);
 
 			/* save images. */
 			int imageCounter = 0;
 			for (Album album : albums) {
 				for (Image image : album.getImages()) {
 					if (!image.isInserted()) {
 						continue;
 					}
 					String imagePrefix = sonePrefix + "/Images/" + imageCounter++;
 					configuration.getStringValue(imagePrefix + "/ID").setValue(image.getId());
 					configuration.getStringValue(imagePrefix + "/Album").setValue(album.getId());
 					configuration.getStringValue(imagePrefix + "/Key").setValue(image.getKey());
 					configuration.getStringValue(imagePrefix + "/Title").setValue(image.getTitle());
 					configuration.getStringValue(imagePrefix + "/Description").setValue(image.getDescription());
 					configuration.getLongValue(imagePrefix + "/CreationTime").setValue(image.getCreationTime());
 					configuration.getIntValue(imagePrefix + "/Width").setValue(image.getWidth());
 					configuration.getIntValue(imagePrefix + "/Height").setValue(image.getHeight());
 				}
 			}
 			configuration.getStringValue(sonePrefix + "/Images/" + imageCounter + "/ID").setValue(null);
 
 			/* save options. */
 			configuration.getBooleanValue(sonePrefix + "/Options/AutoFollow").setValue(sone.getOptions().getBooleanOption("AutoFollow").getReal());
 
 			configuration.save();
 			logger.log(Level.INFO, "Sone %s saved.", sone);
 		} catch (ConfigurationException ce1) {
 			logger.log(Level.WARNING, "Could not save Sone: " + sone, ce1);
 		} catch (WebOfTrustException wote1) {
 			logger.log(Level.WARNING, "Could not set WoT property for Sone: " + sone, wote1);
 		}
 	}
 
 	/**
 	 * Creates a new post.
 	 *
 	 * @param sone
 	 *            The Sone that creates the post
 	 * @param text
 	 *            The text of the post
 	 * @return The created post
 	 */
 	public Post createPost(Sone sone, String text) {
 		return createPost(sone, System.currentTimeMillis(), text);
 	}
 
 	/**
 	 * Creates a new post.
 	 *
 	 * @param sone
 	 *            The Sone that creates the post
 	 * @param time
 	 *            The time of the post
 	 * @param text
 	 *            The text of the post
 	 * @return The created post
 	 */
 	public Post createPost(Sone sone, long time, String text) {
 		return createPost(sone, null, time, text);
 	}
 
 	/**
 	 * Creates a new post.
 	 *
 	 * @param sone
 	 *            The Sone that creates the post
 	 * @param recipient
 	 *            The recipient Sone, or {@code null} if this post does not have
 	 *            a recipient
 	 * @param text
 	 *            The text of the post
 	 * @return The created post
 	 */
 	public Post createPost(Sone sone, Sone recipient, String text) {
 		return createPost(sone, recipient, System.currentTimeMillis(), text);
 	}
 
 	/**
 	 * Creates a new post.
 	 *
 	 * @param sone
 	 *            The Sone that creates the post
 	 * @param recipient
 	 *            The recipient Sone, or {@code null} if this post does not have
 	 *            a recipient
 	 * @param time
 	 *            The time of the post
 	 * @param text
 	 *            The text of the post
 	 * @return The created post
 	 */
 	public Post createPost(Sone sone, Sone recipient, long time, String text) {
 		if (!isLocalSone(sone)) {
 			logger.log(Level.FINE, "Tried to create post for non-local Sone: %s", sone);
 			return null;
 		}
 		Post post = new Post(sone, time, text);
 		if (recipient != null) {
 			post.setRecipient(recipient);
 		}
 		synchronized (posts) {
 			posts.put(post.getId(), post);
 		}
 		synchronized (newPosts) {
 			knownPosts.add(post.getId());
 		}
 		sone.addPost(post);
 		saveSone(sone);
 		return post;
 	}
 
 	/**
 	 * Deletes the given post.
 	 *
 	 * @param post
 	 *            The post to delete
 	 */
 	public void deletePost(Post post) {
 		if (!isLocalSone(post.getSone())) {
 			logger.log(Level.WARNING, "Tried to delete post of non-local Sone: %s", post.getSone());
 			return;
 		}
 		post.getSone().removePost(post);
 		synchronized (posts) {
 			posts.remove(post.getId());
 		}
 		saveSone(post.getSone());
 	}
 
 	/**
 	 * Marks the given post as known, if it is currently a new post (according
 	 * to {@link #isNewPost(String)}).
 	 *
 	 * @param post
 	 *            The post to mark as known
 	 */
 	public void markPostKnown(Post post) {
 		synchronized (newPosts) {
 			if (newPosts.remove(post.getId())) {
 				knownPosts.add(post.getId());
 				coreListenerManager.fireMarkPostKnown(post);
 				saveConfiguration();
 			}
 		}
 	}
 
 	/**
 	 * Bookmarks the given post.
 	 *
 	 * @param post
 	 *            The post to bookmark
 	 */
 	public void bookmark(Post post) {
 		bookmarkPost(post.getId());
 	}
 
 	/**
 	 * Bookmarks the post with the given ID.
 	 *
 	 * @param id
 	 *            The ID of the post to bookmark
 	 */
 	public void bookmarkPost(String id) {
 		synchronized (bookmarkedPosts) {
 			bookmarkedPosts.add(id);
 		}
 	}
 
 	/**
 	 * Removes the given post from the bookmarks.
 	 *
 	 * @param post
 	 *            The post to unbookmark
 	 */
 	public void unbookmark(Post post) {
 		unbookmarkPost(post.getId());
 	}
 
 	/**
 	 * Removes the post with the given ID from the bookmarks.
 	 *
 	 * @param id
 	 *            The ID of the post to unbookmark
 	 */
 	public void unbookmarkPost(String id) {
 		synchronized (bookmarkedPosts) {
 			bookmarkedPosts.remove(id);
 		}
 	}
 
 	/**
 	 * Creates a new reply.
 	 *
 	 * @param sone
 	 *            The Sone that creates the reply
 	 * @param post
 	 *            The post that this reply refers to
 	 * @param text
 	 *            The text of the reply
 	 * @return The created reply
 	 */
 	public Reply createReply(Sone sone, Post post, String text) {
 		return createReply(sone, post, System.currentTimeMillis(), text);
 	}
 
 	/**
 	 * Creates a new reply.
 	 *
 	 * @param sone
 	 *            The Sone that creates the reply
 	 * @param post
 	 *            The post that this reply refers to
 	 * @param time
 	 *            The time of the reply
 	 * @param text
 	 *            The text of the reply
 	 * @return The created reply
 	 */
 	public Reply createReply(Sone sone, Post post, long time, String text) {
 		if (!isLocalSone(sone)) {
 			logger.log(Level.FINE, "Tried to create reply for non-local Sone: %s", sone);
 			return null;
 		}
 		Reply reply = new Reply(sone, post, System.currentTimeMillis(), text);
 		synchronized (replies) {
 			replies.put(reply.getId(), reply);
 		}
 		synchronized (newReplies) {
 			knownReplies.add(reply.getId());
 		}
 		sone.addReply(reply);
 		saveSone(sone);
 		return reply;
 	}
 
 	/**
 	 * Deletes the given reply.
 	 *
 	 * @param reply
 	 *            The reply to delete
 	 */
 	public void deleteReply(Reply reply) {
 		Sone sone = reply.getSone();
 		if (!isLocalSone(sone)) {
 			logger.log(Level.FINE, "Tried to delete non-local reply: %s", reply);
 			return;
 		}
 		synchronized (replies) {
 			replies.remove(reply.getId());
 		}
 		sone.removeReply(reply);
 		saveSone(sone);
 	}
 
 	/**
 	 * Marks the given reply as known, if it is currently a new reply (according
 	 * to {@link #isNewReply(String)}).
 	 *
 	 * @param reply
 	 *            The reply to mark as known
 	 */
 	public void markReplyKnown(Reply reply) {
 		synchronized (newReplies) {
 			if (newReplies.remove(reply.getId())) {
 				knownReplies.add(reply.getId());
 				coreListenerManager.fireMarkReplyKnown(reply);
 				saveConfiguration();
 			}
 		}
 	}
 
 	/**
 	 * Creates a new top-level album for the given Sone.
 	 *
 	 * @param sone
 	 *            The Sone to create the album for
 	 * @return The new album
 	 */
 	public Album createAlbum(Sone sone) {
 		return createAlbum(sone, null);
 	}
 
 	/**
 	 * Creates a new album for the given Sone.
 	 *
 	 * @param sone
 	 *            The Sone to create the album for
 	 * @param parent
 	 *            The parent of the album (may be {@code null} to create a
 	 *            top-level album)
 	 * @return The new album
 	 */
 	public Album createAlbum(Sone sone, Album parent) {
 		Album album = new Album();
 		synchronized (albums) {
 			albums.put(album.getId(), album);
 		}
 		album.setSone(sone);
 		if (parent != null) {
 			parent.addAlbum(album);
 		} else {
 			sone.addAlbum(album);
 		}
 		return album;
 	}
 
 	/**
 	 * Creates a new image.
 	 *
 	 * @param sone
 	 *            The Sone creating the image
 	 * @param album
 	 *            The album the image will be inserted into
 	 * @param temporaryImage
 	 *            The temporary image to create the image from
 	 * @return The newly created image
 	 */
 	public Image createImage(Sone sone, Album album, TemporaryImage temporaryImage) {
 		Validation.begin().isNotNull("Sone", sone).isNotNull("Album", album).isNotNull("Temporary Image", temporaryImage).check().is("Local Sone", isLocalSone(sone)).check().isEqual("Owner and Album Owner", sone, album.getSone()).check();
 		Image image = new Image(temporaryImage.getId()).setSone(sone).setCreationTime(System.currentTimeMillis());
 		album.addImage(image);
 		synchronized (images) {
 			images.put(image.getId(), image);
 		}
 		return image;
 	}
 
 	/**
 	 * Creates a new temporary image.
 	 *
 	 * @param mimeType
 	 *            The MIME type of the temporary image
 	 * @param imageData
 	 *            The encoded data of the image
 	 * @return The temporary image
 	 */
 	public TemporaryImage createTemporaryImage(String mimeType, byte[] imageData) {
 		TemporaryImage temporaryImage = new TemporaryImage();
 		temporaryImage.setMimeType(mimeType).setImageData(imageData);
 		synchronized (temporaryImages) {
 			temporaryImages.put(temporaryImage.getId(), temporaryImage);
 		}
 		return temporaryImage;
 	}
 
 	/**
 	 * Deletes the given temporary image.
 	 *
 	 * @param temporaryImage
 	 *            The temporary image to delete
 	 */
	public void deleteTemporaryImage(TemporaryImage temporaryImage) {
 		Validation.begin().isNotNull("Temporary Image", temporaryImage).check();
 		deleteTemporaryImage(temporaryImage.getId());
 	}
 
 	/**
 	 * Deletes the temporary image with the given ID.
 	 *
 	 * @param imageId
 	 *            The ID of the temporary image to delete
 	 */
 	public void deleteTemporaryImage(String imageId) {
 		Validation.begin().isNotNull("Temporary Image ID", imageId).check();
 		synchronized (temporaryImages) {
 			temporaryImages.remove(imageId);
 		}
 	}
 
 	/**
 	 * Starts the core.
 	 */
 	public void start() {
 		loadConfiguration();
 		updateChecker.addUpdateListener(this);
 		updateChecker.start();
 	}
 
 	/**
 	 * Stops the core.
 	 */
 	public void stop() {
 		synchronized (localSones) {
 			for (SoneInserter soneInserter : soneInserters.values()) {
 				soneInserter.stop();
 			}
 		}
 		updateChecker.stop();
 		updateChecker.removeUpdateListener(this);
 		soneDownloader.stop();
 		saveConfiguration();
 		stopped = true;
 	}
 
 	/**
 	 * Saves the current options.
 	 */
 	public void saveConfiguration() {
 		synchronized (configuration) {
 			if (storingConfiguration) {
 				logger.log(Level.FINE, "Already storing configuration…");
 				return;
 			}
 			storingConfiguration = true;
 		}
 
 		/* store the options first. */
 		try {
 			configuration.getIntValue("Option/ConfigurationVersion").setValue(0);
 			configuration.getIntValue("Option/InsertionDelay").setValue(options.getIntegerOption("InsertionDelay").getReal());
 			configuration.getIntValue("Option/PositiveTrust").setValue(options.getIntegerOption("PositiveTrust").getReal());
 			configuration.getIntValue("Option/NegativeTrust").setValue(options.getIntegerOption("NegativeTrust").getReal());
 			configuration.getStringValue("Option/TrustComment").setValue(options.getStringOption("TrustComment").getReal());
 			configuration.getBooleanValue("Option/SoneRescueMode").setValue(options.getBooleanOption("SoneRescueMode").getReal());
 			configuration.getBooleanValue("Option/ClearOnNextRestart").setValue(options.getBooleanOption("ClearOnNextRestart").getReal());
 			configuration.getBooleanValue("Option/ReallyClearOnNextRestart").setValue(options.getBooleanOption("ReallyClearOnNextRestart").getReal());
 
 			/* save known Sones. */
 			int soneCounter = 0;
 			synchronized (newSones) {
 				for (String knownSoneId : knownSones) {
 					configuration.getStringValue("KnownSone/" + soneCounter++ + "/ID").setValue(knownSoneId);
 				}
 				configuration.getStringValue("KnownSone/" + soneCounter + "/ID").setValue(null);
 			}
 
 			/* save known posts. */
 			int postCounter = 0;
 			synchronized (newPosts) {
 				for (String knownPostId : knownPosts) {
 					configuration.getStringValue("KnownPosts/" + postCounter++ + "/ID").setValue(knownPostId);
 				}
 				configuration.getStringValue("KnownPosts/" + postCounter + "/ID").setValue(null);
 			}
 
 			/* save known replies. */
 			int replyCounter = 0;
 			synchronized (newReplies) {
 				for (String knownReplyId : knownReplies) {
 					configuration.getStringValue("KnownReplies/" + replyCounter++ + "/ID").setValue(knownReplyId);
 				}
 				configuration.getStringValue("KnownReplies/" + replyCounter + "/ID").setValue(null);
 			}
 
 			/* save bookmarked posts. */
 			int bookmarkedPostCounter = 0;
 			synchronized (bookmarkedPosts) {
 				for (String bookmarkedPostId : bookmarkedPosts) {
 					configuration.getStringValue("Bookmarks/Post/" + bookmarkedPostCounter++ + "/ID").setValue(bookmarkedPostId);
 				}
 			}
 			configuration.getStringValue("Bookmarks/Post/" + bookmarkedPostCounter++ + "/ID").setValue(null);
 
 			/* now save it. */
 			configuration.save();
 
 		} catch (ConfigurationException ce1) {
 			logger.log(Level.SEVERE, "Could not store configuration!", ce1);
 		} finally {
 			synchronized (configuration) {
 				storingConfiguration = false;
 			}
 		}
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Loads the configuration.
 	 */
 	@SuppressWarnings("unchecked")
 	private void loadConfiguration() {
 		/* create options. */
 		options.addIntegerOption("InsertionDelay", new DefaultOption<Integer>(60, new OptionWatcher<Integer>() {
 
 			@Override
 			public void optionChanged(Option<Integer> option, Integer oldValue, Integer newValue) {
 				SoneInserter.setInsertionDelay(newValue);
 			}
 
 		}));
 		options.addIntegerOption("PositiveTrust", new DefaultOption<Integer>(75));
 		options.addIntegerOption("NegativeTrust", new DefaultOption<Integer>(-100));
 		options.addStringOption("TrustComment", new DefaultOption<String>("Set from Sone Web Interface"));
 		options.addBooleanOption("SoneRescueMode", new DefaultOption<Boolean>(false));
 		options.addBooleanOption("ClearOnNextRestart", new DefaultOption<Boolean>(false));
 		options.addBooleanOption("ReallyClearOnNextRestart", new DefaultOption<Boolean>(false));
 
 		/* read options from configuration. */
 		options.getBooleanOption("ClearOnNextRestart").set(configuration.getBooleanValue("Option/ClearOnNextRestart").getValue(null));
 		options.getBooleanOption("ReallyClearOnNextRestart").set(configuration.getBooleanValue("Option/ReallyClearOnNextRestart").getValue(null));
 		boolean clearConfiguration = options.getBooleanOption("ClearOnNextRestart").get() && options.getBooleanOption("ReallyClearOnNextRestart").get();
 		options.getBooleanOption("ClearOnNextRestart").set(null);
 		options.getBooleanOption("ReallyClearOnNextRestart").set(null);
 		if (clearConfiguration) {
 			/* stop loading the configuration. */
 			return;
 		}
 
 		options.getIntegerOption("InsertionDelay").set(configuration.getIntValue("Option/InsertionDelay").getValue(null));
 		options.getIntegerOption("PositiveTrust").set(configuration.getIntValue("Option/PositiveTrust").getValue(null));
 		options.getIntegerOption("NegativeTrust").set(configuration.getIntValue("Option/NegativeTrust").getValue(null));
 		options.getStringOption("TrustComment").set(configuration.getStringValue("Option/TrustComment").getValue(null));
 		options.getBooleanOption("SoneRescueMode").set(configuration.getBooleanValue("Option/SoneRescueMode").getValue(null));
 
 		/* load known Sones. */
 		int soneCounter = 0;
 		while (true) {
 			String knownSoneId = configuration.getStringValue("KnownSone/" + soneCounter++ + "/ID").getValue(null);
 			if (knownSoneId == null) {
 				break;
 			}
 			synchronized (newSones) {
 				knownSones.add(knownSoneId);
 			}
 		}
 
 		/* load known posts. */
 		int postCounter = 0;
 		while (true) {
 			String knownPostId = configuration.getStringValue("KnownPosts/" + postCounter++ + "/ID").getValue(null);
 			if (knownPostId == null) {
 				break;
 			}
 			synchronized (newPosts) {
 				knownPosts.add(knownPostId);
 			}
 		}
 
 		/* load known replies. */
 		int replyCounter = 0;
 		while (true) {
 			String knownReplyId = configuration.getStringValue("KnownReplies/" + replyCounter++ + "/ID").getValue(null);
 			if (knownReplyId == null) {
 				break;
 			}
 			synchronized (newReplies) {
 				knownReplies.add(knownReplyId);
 			}
 		}
 
 		/* load bookmarked posts. */
 		int bookmarkedPostCounter = 0;
 		while (true) {
 			String bookmarkedPostId = configuration.getStringValue("Bookmarks/Post/" + bookmarkedPostCounter++ + "/ID").getValue(null);
 			if (bookmarkedPostId == null) {
 				break;
 			}
 			synchronized (bookmarkedPosts) {
 				bookmarkedPosts.add(bookmarkedPostId);
 			}
 		}
 
 	}
 
 	/**
 	 * Generate a Sone URI from the given URI and latest edition.
 	 *
 	 * @param uriString
 	 *            The URI to derive the Sone URI from
 	 * @return The derived URI
 	 */
 	private FreenetURI getSoneUri(String uriString) {
 		try {
 			FreenetURI uri = new FreenetURI(uriString).setDocName("Sone").setMetaString(new String[0]);
 			return uri;
 		} catch (MalformedURLException mue1) {
 			logger.log(Level.WARNING, "Could not create Sone URI from URI: " + uriString, mue1);
 			return null;
 		}
 	}
 
 	//
 	// INTERFACE IdentityListener
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void ownIdentityAdded(OwnIdentity ownIdentity) {
 		logger.log(Level.FINEST, "Adding OwnIdentity: " + ownIdentity);
 		if (ownIdentity.hasContext("Sone")) {
 			trustedIdentities.put(ownIdentity, Collections.synchronizedSet(new HashSet<Identity>()));
 			addLocalSone(ownIdentity);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void ownIdentityRemoved(OwnIdentity ownIdentity) {
 		logger.log(Level.FINEST, "Removing OwnIdentity: " + ownIdentity);
 		trustedIdentities.remove(ownIdentity);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void identityAdded(OwnIdentity ownIdentity, Identity identity) {
 		logger.log(Level.FINEST, "Adding Identity: " + identity);
 		trustedIdentities.get(ownIdentity).add(identity);
 		addRemoteSone(identity);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void identityUpdated(OwnIdentity ownIdentity, final Identity identity) {
 		new Thread(new Runnable() {
 
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void run() {
 				Sone sone = getRemoteSone(identity.getId());
 				sone.setIdentity(identity);
 				soneDownloader.addSone(sone);
 				soneDownloader.fetchSone(sone);
 			}
 		}).start();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void identityRemoved(OwnIdentity ownIdentity, Identity identity) {
 		trustedIdentities.get(ownIdentity).remove(identity);
 	}
 
 	//
 	// INTERFACE UpdateListener
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateFound(Version version, long releaseTime, long latestEdition) {
 		coreListenerManager.fireUpdateFound(version, releaseTime, latestEdition);
 	}
 
 	//
 	// INTERFACE ImageInsertListener
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void imageInsertStarted(Image image) {
 		logger.log(Level.WARNING, "Image insert started for " + image);
 		coreListenerManager.fireImageInsertStarted(image);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void imageInsertAborted(Image image) {
 		logger.log(Level.WARNING, "Image insert aborted for " + image);
 		coreListenerManager.fireImageInsertAborted(image);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void imageInsertFinished(Image image, FreenetURI key) {
 		logger.log(Level.WARNING, "Image insert finished for " + image + ": " + key);
 		image.setKey(key.toString());
 		deleteTemporaryImage(image.getId());
 		saveSone(image.getSone());
 		coreListenerManager.fireImageInsertFinished(image);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void imageInsertFailed(Image image, Throwable cause) {
 		logger.log(Level.WARNING, "Image insert failed for " + image, cause);
 		coreListenerManager.fireImageInsertFailed(image, cause);
 	}
 
 	/**
 	 * Convenience interface for external classes that want to access the core’s
 	 * configuration.
 	 *
 	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 	 */
 	public static class Preferences {
 
 		/** The wrapped options. */
 		private final Options options;
 
 		/**
 		 * Creates a new preferences object wrapped around the given options.
 		 *
 		 * @param options
 		 *            The options to wrap
 		 */
 		public Preferences(Options options) {
 			this.options = options;
 		}
 
 		/**
 		 * Returns the insertion delay.
 		 *
 		 * @return The insertion delay
 		 */
 		public int getInsertionDelay() {
 			return options.getIntegerOption("InsertionDelay").get();
 		}
 
 		/**
 		 * Sets the insertion delay
 		 *
 		 * @param insertionDelay
 		 *            The new insertion delay, or {@code null} to restore it to
 		 *            the default value
 		 * @return This preferences
 		 */
 		public Preferences setInsertionDelay(Integer insertionDelay) {
 			options.getIntegerOption("InsertionDelay").set(insertionDelay);
 			return this;
 		}
 
 		/**
 		 * Returns the positive trust.
 		 *
 		 * @return The positive trust
 		 */
 		public int getPositiveTrust() {
 			return options.getIntegerOption("PositiveTrust").get();
 		}
 
 		/**
 		 * Sets the positive trust.
 		 *
 		 * @param positiveTrust
 		 *            The new positive trust, or {@code null} to restore it to
 		 *            the default vlaue
 		 * @return This preferences
 		 */
 		public Preferences setPositiveTrust(Integer positiveTrust) {
 			options.getIntegerOption("PositiveTrust").set(positiveTrust);
 			return this;
 		}
 
 		/**
 		 * Returns the negative trust.
 		 *
 		 * @return The negative trust
 		 */
 		public int getNegativeTrust() {
 			return options.getIntegerOption("NegativeTrust").get();
 		}
 
 		/**
 		 * Sets the negative trust.
 		 *
 		 * @param negativeTrust
 		 *            The negative trust, or {@code null} to restore it to the
 		 *            default value
 		 * @return The preferences
 		 */
 		public Preferences setNegativeTrust(Integer negativeTrust) {
 			options.getIntegerOption("NegativeTrust").set(negativeTrust);
 			return this;
 		}
 
 		/**
 		 * Returns the trust comment. This is the comment that is set in the web
 		 * of trust when a trust value is assigned to an identity.
 		 *
 		 * @return The trust comment
 		 */
 		public String getTrustComment() {
 			return options.getStringOption("TrustComment").get();
 		}
 
 		/**
 		 * Sets the trust comment.
 		 *
 		 * @param trustComment
 		 *            The trust comment, or {@code null} to restore it to the
 		 *            default value
 		 * @return This preferences
 		 */
 		public Preferences setTrustComment(String trustComment) {
 			options.getStringOption("TrustComment").set(trustComment);
 			return this;
 		}
 
 		/**
 		 * Returns whether the rescue mode is active.
 		 *
 		 * @return {@code true} if the rescue mode is active, {@code false}
 		 *         otherwise
 		 */
 		public boolean isSoneRescueMode() {
 			return options.getBooleanOption("SoneRescueMode").get();
 		}
 
 		/**
 		 * Sets whether the rescue mode is active.
 		 *
 		 * @param soneRescueMode
 		 *            {@code true} if the rescue mode is active, {@code false}
 		 *            otherwise
 		 * @return This preferences
 		 */
 		public Preferences setSoneRescueMode(Boolean soneRescueMode) {
 			options.getBooleanOption("SoneRescueMode").set(soneRescueMode);
 			return this;
 		}
 
 		/**
 		 * Returns whether Sone should clear its settings on the next restart.
 		 * In order to be effective, {@link #isReallyClearOnNextRestart()} needs
 		 * to return {@code true} as well!
 		 *
 		 * @return {@code true} if Sone should clear its settings on the next
 		 *         restart, {@code false} otherwise
 		 */
 		public boolean isClearOnNextRestart() {
 			return options.getBooleanOption("ClearOnNextRestart").get();
 		}
 
 		/**
 		 * Sets whether Sone will clear its settings on the next restart.
 		 *
 		 * @param clearOnNextRestart
 		 *            {@code true} if Sone should clear its settings on the next
 		 *            restart, {@code false} otherwise
 		 * @return This preferences
 		 */
 		public Preferences setClearOnNextRestart(Boolean clearOnNextRestart) {
 			options.getBooleanOption("ClearOnNextRestart").set(clearOnNextRestart);
 			return this;
 		}
 
 		/**
 		 * Returns whether Sone should really clear its settings on next
 		 * restart. This is a confirmation option that needs to be set in
 		 * addition to {@link #isClearOnNextRestart()} in order to clear Sone’s
 		 * settings on the next restart.
 		 *
 		 * @return {@code true} if Sone should really clear its settings on the
 		 *         next restart, {@code false} otherwise
 		 */
 		public boolean isReallyClearOnNextRestart() {
 			return options.getBooleanOption("ReallyClearOnNextRestart").get();
 		}
 
 		/**
 		 * Sets whether Sone should really clear its settings on the next
 		 * restart.
 		 *
 		 * @param reallyClearOnNextRestart
 		 *            {@code true} if Sone should really clear its settings on
 		 *            the next restart, {@code false} otherwise
 		 * @return This preferences
 		 */
 		public Preferences setReallyClearOnNextRestart(Boolean reallyClearOnNextRestart) {
 			options.getBooleanOption("ReallyClearOnNextRestart").set(reallyClearOnNextRestart);
 			return this;
 		}
 
 	}
 
 }
