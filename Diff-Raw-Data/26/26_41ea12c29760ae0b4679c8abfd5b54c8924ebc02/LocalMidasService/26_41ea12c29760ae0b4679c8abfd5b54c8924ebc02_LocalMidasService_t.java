 package com.robonobo.midas;
 
 import static com.robonobo.common.util.TextUtil.*;
 import static com.robonobo.common.util.TimeUtil.*;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.common.util.TimeUtil;
 import com.robonobo.core.api.model.*;
 import com.robonobo.midas.dao.*;
 import com.robonobo.midas.model.*;
 import com.robonobo.remote.service.MidasService;
 import com.twmacinta.util.MD5;
 
 @Service("midas")
 public class LocalMidasService implements MidasService {
 	@Autowired
 	private AppConfig appConfig;
 	@Autowired
 	private FacebookService facebook;
 	@Autowired
 	private TwitterService twitter;
 	@Autowired
 	private MessageService message;
 	@Autowired
 	private EventService event;
 	@Autowired
 	private NotificationService notification;
 	@Autowired
 	private FriendRequestDao friendRequestDao;
 	@Autowired
 	private InviteDao inviteDao;
 	@Autowired
 	private LibraryDao libraryDao;
 	@Autowired
 	private PlaylistDao playlistDao;
 	@Autowired
 	private StreamDao streamDao;
 	@Autowired
 	private UserConfigDao userConfigDao;
 	@Autowired
 	private UserDao userDao;
 	@Autowired
 	private CommentDao commentDao;
 	@Autowired
 	ThreadPoolTaskScheduler scheduler;
 	Log log = LogFactory.getLog(getClass());
 	private long lastPlaylistId = -1;
 	private long lastCommentId = -1;
 
 	@Transactional(readOnly = true)
 	public List<MidasUser> getAllUsers() {
 		return userDao.getAll();
 	}
 
 	public MidasUser getUserByEmail(String email) {
 		return populateDefault(userDao.getByEmail(email));
 	}
 
 	public MidasUser getUserById(long userId) {
 		return populateDefault(userDao.getById(userId));
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasUser createUser(MidasUser user) {
 		log.info("Creating user " + user.getEmail());
 		user.setVerified(true);
 		user.setUpdated(now());
 		preventUserXSS(user);
 		// This will have its user id set
 		MidasUser createdUser = userDao.create(user);
 		try {
 			message.sendWelcome(createdUser);
 		} catch (IOException e) {
 			log.error("Error sending welcome mail to " + createdUser.getEmail(), e);
 		}
 		event.newUser(createdUser);
 		return populateDefault(createdUser);
 	}
 
 	private MidasUser populateDefault(MidasUser u) {
 		if (u == null)
 			return u;
 		if (isEmpty(u.getImgUrl()))
 			u.setImgUrl(appConfig.getInitParam("defaultUserImgUrl"));
 		return u;
 	}
 
 	public MidasUser getUserAsVisibleBy(MidasUser targetU, MidasUser requestor) {
 		// If this the user asking for themselves, give them everything. If
 		// they're a friend, they get public and friend-visible playlists, but no friends.
 		// Otherwise, they just get friendly name and image url
 		MidasUser result;
 		if (targetU.equals(requestor)) {
 			result = new MidasUser(targetU);
 		} else if (targetU.getFriendIds().contains(requestor.getUserId())) {
 			result = new MidasUser(targetU);
 			result.setPassword(null);
 			result.getFriendIds().clear();
 			result.setInvitesLeft(0);
 			Iterator<Long> iter = result.getPlaylistIds().iterator();
 			while (iter.hasNext()) {
 				Playlist p = playlistDao.getPlaylistById(iter.next());
 				if (p.getVisibility().equals(Playlist.VIS_ME))
 					iter.remove();
 			}
 		} else {
 			result = new MidasUser();
 			result.setUserId(targetU.getUserId());
 			result.setFriendlyName(targetU.getFriendlyName());
 			result.setImgUrl(targetU.getImgUrl());
 		}
 		return populateDefault(result);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void saveUser(MidasUser user) {
 		preventUserXSS(user);
 		user.setUpdated(now());
 		userDao.save(user);
 	}
 
 	private void preventUserXSS(MidasUser user) {
 		user.setFriendlyName(escapeHtml(user.getFriendlyName()));
 		user.setDescription(escapeHtml(user.getDescription()));
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deleteUser(long userId) {
 		MidasUser u = userDao.getById(userId);
 		// Go through all their playlists - if they are the only owner, delete it, otherwise remove them from the owners
 		// list
 		for (Long plId : u.getPlaylistIds()) {
 			MidasPlaylist p = playlistDao.getPlaylistById(plId);
 			p.getOwnerIds().remove(userId);
 			if (p.getOwnerIds().size() == 0)
 				playlistDao.deletePlaylist(p);
 			else
 				playlistDao.savePlaylist(p);
 		}
 		// Go through all their friends, delete this user from their friendids
 		for (long friendId : u.getFriendIds()) {
 			MidasUser friend = userDao.getById(friendId);
 			friend.getFriendIds().remove(userId);
 			userDao.save(friend);
 		}
 		// Delete any pending friend requests to this user
 		List<MidasFriendRequest> frs = friendRequestDao.retrieveByRequestee(userId);
 		for (MidasFriendRequest fr : frs) {
 			friendRequestDao.delete(fr);
 		}
 		// Delete their userconfig
 		userConfigDao.deleteUserConfig(userId);
 		// Finally, delete the user itself
 		userDao.delete(u);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasPlaylist getPlaylistById(long playlistId) {
 		return playlistDao.getPlaylistById(playlistId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasPlaylist getPlaylistByUserIdAndTitle(long uid, String title) {
 		return playlistDao.getPlaylistByUserIdAndTitle(uid, title);
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public List<MidasPlaylist> getRecentPlaylists(long maxAgeMs) {
 		return playlistDao.getRecentPlaylists(maxAgeMs);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	@Override
 	public MidasPlaylist newPlaylist(MidasPlaylist playlist) {
 		if (playlist.getPlaylistId() > 0)
 			throw new SeekInnerCalmException("newPlaylist called with non-new playlist!");
 		long newPlaylistId;
 		synchronized (this) {
 			if (lastPlaylistId <= 0)
 				lastPlaylistId = playlistDao.getHighestPlaylistId();
 			if (lastPlaylistId == Long.MAX_VALUE)
 				throw new SeekInnerCalmException("playlist ids wrapped!"); // Unlikely
 			else
 				lastPlaylistId++;
 			newPlaylistId = lastPlaylistId;
 		}
 		playlist.setPlaylistId(newPlaylistId);
 		savePlaylist(playlist);
 		return playlist;
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public List<MidasComment> getCommentsForPlaylist(long plId, Date since) {
 		String resourceId = "playlist:" + plId;
 		if (since == null)
 			return commentDao.getAllComments(resourceId);
 		else
 			return commentDao.getCommentsSince(resourceId, since);
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public List<MidasComment> getCommentsForLibrary(long uid, Date since) {
 		String resourceId = "library:" + uid;
 		if (since == null)
 			return commentDao.getAllComments(resourceId);
 		else
 			return commentDao.getCommentsSince(resourceId, since);
 	}
 
 	@Override
 	public MidasComment newCommentForPlaylist(MidasComment comment, long playlistId) {
 		return newComment(comment, "playlist:" + playlistId);
 	}
 
 	@Override
 	public MidasComment newCommentForLibrary(MidasComment comment, long userId) {
 		return newComment(comment, "library:" + userId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	private MidasComment newComment(MidasComment comment, String resourceId) {
 		if (comment.getCommentId() > 0)
 			throw new SeekInnerCalmException("newComment called with non-new comment");
 		long newCommentId;
 		synchronized (this) {
 			if (lastCommentId <= 0)
 				lastCommentId = commentDao.getHighestCommentId();
 			if (lastCommentId == Long.MAX_VALUE)
 				throw new SeekInnerCalmException("comment ids wrapped");
 			else
 				lastCommentId++;
 			newCommentId = lastCommentId;
 		}
 		comment.setCommentId(newCommentId);
 		comment.setResourceId(resourceId);
 		saveComment(comment);
 		return comment;
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public void saveComment(MidasComment c) {
 		if (c.getCommentId() <= 0)
 			throw new SeekInnerCalmException("comment id is not set");
 		preventCommentXSS(c);
 		commentDao.saveComment(c);
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public MidasComment getComment(long commentId) {
 		return commentDao.getComment(commentId);
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public void deleteComment(MidasComment c) {
 		commentDao.deleteComment(c);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void savePlaylist(MidasPlaylist p) {
 		if (p.getPlaylistId() <= 0)
 			throw new SeekInnerCalmException("playlist id is not set");
 		preventPlaylistXSS(p);
 		playlistDao.savePlaylist(p);
 	}
 
 	private void preventPlaylistXSS(MidasPlaylist p) {
 		p.setTitle(escapeHtml(p.getTitle()));
 		p.setDescription(escapeHtml(p.getDescription()));
 	}
 
 	private void preventCommentXSS(MidasComment c) {
 		c.setText(escapeHtml(c.getText()));
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deletePlaylist(MidasPlaylist playlist) {
 		playlistDao.deletePlaylist(playlist);
 		commentDao.deleteAllComments("playlist:" + playlist.getPlaylistId());
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasStream getStreamById(String streamId) {
 		return streamDao.getStream(streamId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void saveStream(MidasStream stream) {
 		streamDao.putStream(stream);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void deleteStream(MidasStream stream) {
 		streamDao.deleteStream(stream);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public Long countUsers() {
 		return userDao.getUserCount();
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasInvite createOrUpdateInvite(String email, MidasUser friend, MidasPlaylist pl) {
 		MidasInvite result = inviteDao.retrieveByEmail(email);
 		if (result == null) {
 			// New invite
 			result = new MidasInvite();
 			result.setEmail(email);
 			result.setInviteCode(generateEmailCode(email));
 		}
 		result.getFriendIds().add(friend.getUserId());
 		if (pl != null)
 			result.getPlaylistIds().add(pl.getPlaylistId());
 		result.setUpdated(now());
 		inviteDao.save(result);
 		return result;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public MidasFriendRequest createOrUpdateFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist pl) {
 		MidasFriendRequest result = friendRequestDao.retrieveByUsers(requestor.getUserId(), requestee.getUserId());
 		if (result == null) {
 			// New friend request
 			result = new MidasFriendRequest();
 			result.setRequestorId(requestor.getUserId());
 			result.setRequesteeId(requestee.getUserId());
 			result.setRequestCode(generateEmailCode(requestee.getEmail()));
 		}
 		if (pl != null)
 			result.getPlaylistIds().add(pl.getPlaylistId());
 		result.setUpdated(now());
 		friendRequestDao.save(result);
 		return result;
 	}
 
 	public MidasFriendRequest getFriendRequest(String requestCode) {
 		return friendRequestDao.retrieveByRequestCode(requestCode);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public String acceptFriendRequest(MidasFriendRequest req) {
 		MidasUser requestor = userDao.getById(req.getRequestorId());
 		if (requestor == null)
 			return "Requesting user " + req.getRequestorId() + " not found";
 		MidasUser requestee = userDao.getById(req.getRequesteeId());
 		if (requestee == null)
 			return "Requested user " + req.getRequesteeId() + " not found";
 		for (Long plId : req.getPlaylistIds()) {
 			MidasPlaylist p = playlistDao.getPlaylistById(plId);
 			p.getOwnerIds().add(requestee.getUserId());
 			playlistDao.savePlaylist(p);
 			requestee.getPlaylistIds().add(plId);
 		}
 		requestor.getFriendIds().add(requestee.getUserId());
 		userDao.save(requestor);
 		requestee.getFriendIds().add(requestor.getUserId());
 		userDao.save(requestee);
 		friendRequestDao.delete(req);
 		try {
 			message.sendFriendConfirmation(requestor, requestee);
 		} catch (IOException e) {
 			log.error("Error sending friend confirmation", e);
 		}
 		event.friendRequestAccepted(requestor, requestee);
 		return null;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void ignoreFriendRequest(MidasFriendRequest request) {
 		friendRequestDao.delete(request);
 	}
 
 	public List<MidasFriendRequest> getPendingFriendRequests(long userId) {
 		return friendRequestDao.retrieveByRequestee(userId);
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	public void inviteAccepted(long acceptedUserId, String inviteCode) {
 		MidasInvite invite = inviteDao.retrieveByInviteCode(inviteCode);
 		if (invite != null) {
 			MidasUser acceptedUser = getUserById(acceptedUserId);
 			// Tell them about their new buddy
 			for (Long friendId : invite.getFriendIds()) {
 				MidasUser friend = getUserById(friendId);
 				if (friend != null) {
 					try {
 						message.sendFriendConfirmation(friend, acceptedUser);
 					} catch (IOException e) {
 						log.error("Error sending friend confirmation", e);
 					}
 				}
 			}
 			event.inviteAccepted(acceptedUser, invite);
 			inviteDao.delete(invite);
 		}
 	}
 
 	public MidasInvite getInvite(String inviteCode) {
 		return inviteDao.retrieveByInviteCode(inviteCode);
 	}
 
 	@Override
 	public MidasInvite getInviteByEmail(String email) {
 		return inviteDao.retrieveByEmail(email);
 	}
 
 	@Override
 	public Library getLibrary(MidasUser u, Date since) {
 		Library lib = libraryDao.getLibrary(u.getUserId());
 		if (lib == null)
 			return null;
 		lib.setUserId(u.getUserId());
 		if (since != null) {
 			Iterator<Entry<String, Date>> it = lib.getTracks().entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, Date> e = it.next();
 				if (since.after(e.getValue()))
 					it.remove();
 			}
 		}
 		return lib;
 	}
 
 	@Transactional(rollbackFor = Exception.class)
 	@Override
 	public void putLibrary(Library lib) {
 		libraryDao.saveLibrary(lib);
 	}
 
 	@Override
 	public MidasUserConfig getUserConfig(MidasUser u) {
 		MidasUserConfig result = userConfigDao.getUserConfig(u.getUserId());
 		if (result == null) {
 			result = new MidasUserConfig();
 			result.setUserId(u.getUserId());
 		}
 		return result;
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public void putUserConfig(MidasUserConfig newCfg) {
 		// Update friends based on facebook & twitter deets
 		long uid = newCfg.getUserId();
 		final MidasUser mu = userDao.getById(uid);
 		MidasUserConfig oldCfg = userConfigDao.getUserConfig(uid);
 		// Check to see if they have added facebook/twitter details
		boolean newFb = (oldCfg == null || oldCfg.getItem("facebookId") == null) && newCfg.getItem("facebookId") != null;
		boolean newTwit = (oldCfg == null || oldCfg.getItem("twitterScreenName") == null) && newCfg.getItem("twitterScreenName") != null;
 		if (newFb || newTwit) {
 			// Post their loves - but wait 30 mins to give them a chance to disable it
 			final Playlist lovesPl = playlistDao.getPlaylistByUserIdAndTitle(uid, "loves");
 			if (lovesPl != null && lovesPl.getStreamIds().size() > 0) {
 				scheduler.schedule(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						Playlist newLovesPl = playlistDao.getPlaylistByUserIdAndTitle(mu.getUserId(), "loves");
 						// We want to exclude any new loves that have been posted, so we pass the new playlist as the old one and vice versa
 						lovesChanged(mu, newLovesPl, lovesPl);
 					}
 				}, TimeUtil.timeInFuture(30 * 60 * 1000));
 			}
 		}
 		if (newFb)
 			facebook.updateFriends(mu, newCfg);
 		userConfigDao.saveUserConfig(newCfg);
 	}
 
 	@Override
 	@Transactional(rollbackFor = Exception.class)
 	public void lovesChanged(MidasUser u, Playlist oldP, Playlist newP) throws IOException {
 		long uid = u.getUserId();
 		// Which artists have just been added?
 		Set<String> curArtists = new HashSet<String>();
 		for(String sid : oldP.getStreamIds()) {
 			Stream s = streamDao.getStream(sid);
 			curArtists.add(s.getArtist());
 		}
 		List<String> newArtists = new ArrayList<String>();
 		for(String sid : newP.getStreamIds()) {
 			if(!oldP.getStreamIds().contains(sid)) {
 				Stream s = streamDao.getStream(sid);
 				String artist = s.getArtist();
 				if(!curArtists.contains(artist))
 					newArtists.add(artist);
 			}
 		}
 		if(newArtists.size() > 0) {
 			List<String> al = new ArrayList<String>(newArtists);
 			Collections.sort(al);
 			// Figure out our message to post - use the twitter limit
 			int msgSizeLimit = 140;
 			String okMsg = "I love " + numItems(newArtists, "artist") + ": ";
 			String url = appConfig.getInitParam("shortUrlBase") + "sp/" + Long.toHexString(uid) + "/loves";
 			for (int i = 0; i < newArtists.size(); i++) {
 				StringBuffer sb = new StringBuffer("I love ");
 				for (int j = 0; j <= i; j++) {
 					if (j != 0)
 						sb.append(", ");
 					sb.append(newArtists.get(j));
 				}
 				if (i < (newArtists.size() - 1)) {
 					int numOtherArtists = newArtists.size() - (i + 1);
 					sb.append(" and ").append(numItems(numOtherArtists, "other artist"));
 				}
 				sb.append(": ");
 				String msg = sb.toString();
 				if ((msg.length() + url.length()) <= msgSizeLimit)
 					okMsg = msg;
 				else
 					break;
 			}
 			// Post our loves to fb/twitter
 			MidasUserConfig muc = userConfigDao.getUserConfig(u.getUserId());
 			if(muc.getItem("facebookId") != null) {
 				String fbStr = muc.getItem("postLovesToFacebook");
 				if(fbStr == null || Boolean.valueOf(fbStr))
 					facebook.postSpecialPlaylistToFacebook(muc, uid, "loves", okMsg);
 			}
 			if(muc.getItem("twitterScreenName") != null) {
 				String twitStr = muc.getItem("postLovesToTwitter");
 				if(twitStr == null || Boolean.valueOf(twitStr))
 					twitter.postSpecialPlaylistToTwitter(muc, uid, "loves", okMsg);
 			}
 			event.specialPlaylistPosted(u, uid, "loves");
 			// Send notifications to friends
 			notification.lovesAdded(u, al);
 		}
 	}
 
 	@Override
 	public void addFriends(long userId, List<Long> friendIds, List<String> friendEmails) {
 		MidasUser fromUser = getUserById(userId);
 		if (fromUser == null) {
 			log.error("addFriends failed: No user with id " + userId);
 			return;
 		}
 		try {
 			for (Long friendId : friendIds) {
 				MidasUser friend = getUserById(friendId);
 				if (friend == null) {
 					log.error("addFriends failed for user " + fromUser.getEmail() + " and non existent user id " + friendId);
 					continue;
 				}
 				message.sendFriendRequest(fromUser, friend, null);
 				event.friendRequestSent(fromUser, friend);
 			}
 			for (String email : friendEmails) {
 				MidasInvite invite = message.sendInvite(fromUser, email, null);
 				event.inviteSent(fromUser, email, invite);
 			}
 		} catch (IOException e) {
 			log.error("addFriends failed", e);
 		}
 	}
 
 	@Override
 	public String requestAccountTopUp(long userId) {
 		MidasUser user = userDao.getById(userId);
 		if (user == null)
 			return "Error: no such user";
 		try {
 			message.sendTopUpRequest(user);
 		} catch (IOException e) {
 			log.error("Error requesting topup", e);
 			return "Error processing request, please contact help@robonobo.com";
 		}
 		return "TopUp request received - please check your account in a few hours.";
 	}
 
 	private String generateEmailCode(String email) {
 		MD5 hash = new MD5();
 		hash.Update(email);
 		hash.Update(getDateFormat().format(now()));
 		return hash.asHex();
 	}
 }
