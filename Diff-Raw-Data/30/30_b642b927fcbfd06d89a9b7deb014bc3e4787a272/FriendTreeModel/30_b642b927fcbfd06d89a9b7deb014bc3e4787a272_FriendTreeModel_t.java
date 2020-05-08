 /**
  * 
  */
 package com.robonobo.gui.model;
 
 import static com.robonobo.gui.GuiUtil.*;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.swing.SortableTreeNode;
 import com.robonobo.common.swing.SortedTreeModel;
 import com.robonobo.core.RobonoboController;
 import com.robonobo.core.api.LibraryListener;
 import com.robonobo.core.api.UserPlaylistListener;
 import com.robonobo.core.api.model.*;
 import com.robonobo.gui.GuiUtil;
 import com.robonobo.gui.components.FriendTree;
 import com.robonobo.gui.frames.RobonoboFrame;
 
 @SuppressWarnings("serial")
 public class FriendTreeModel extends SortedTreeModel implements UserPlaylistListener, LibraryListener {
 	static final int MAX_FRIEND_PLAYLIST_TITLE_WIDTH = 100;
 	RobonoboFrame frame;
 	RobonoboController control;
 	FriendTree tree;
 	Map<Long, FriendTreeNode> friendNodes = new HashMap<Long, FriendTreeNode>();
 	Map<Long, LibraryTreeNode> libNodes = new HashMap<Long, LibraryTreeNode>();
 	Map<Long, Map<Long, PlaylistTreeNode>> playlistNodes = new HashMap<Long, Map<Long, PlaylistTreeNode>>();
 	Set<Long> playlistIds = new HashSet<Long>();
 	Log log = LogFactory.getLog(getClass());
 	SelectableTreeNode myRoot;
 
 	public FriendTreeModel(RobonoboFrame rFrame) {
 		super(null);
 		myRoot = new SelectableTreeNode("Friends");
 		setRoot(myRoot);
 		frame = rFrame;
 		control = frame.getController();
 		control.addUserPlaylistListener(this);
 		control.addLibraryListener(this);
 	}
 
 	public void loggedIn() {
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				synchronized (FriendTreeModel.this) {
 					getRoot().removeAllChildren();
 					friendNodes.clear();
 					playlistNodes.clear();
 					playlistIds.clear();
 				}
 				nodeStructureChanged(getRoot());
 			}
 		});
 	}
 
 	public void userChanged(final User u) {
 		// If it's me, check to see if any of my friends are no longer friends
 		if (control.getMyUser().equals(u)) {
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					synchronized (FriendTreeModel.this) {
 						for (Long friendId : friendNodes.keySet()) {
 							if (!u.getFriendIds().contains(friendId)) {
 								removeNodeFromParent(friendNodes.get(friendId));
 								friendNodes.remove(friendId);
 								Map<Long, PlaylistTreeNode> toRm = playlistNodes.remove(friendId);
 								playlistIds.removeAll(toRm.keySet());
 							}
 						}
 					}
 				}
 			});
 		} else if (control.getMyUser().getFriendIds().contains(u.getUserId())) {
 			// It's a friend
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					synchronized (FriendTreeModel.this) {
 						if (friendNodes.containsKey(u.getUserId())) {
 							FriendTreeNode ftn = friendNodes.get(u.getUserId());
 							// Already have this friend - check to see if any playlists have been deleted
 							Iterator<Entry<Long, PlaylistTreeNode>> iter = playlistNodes.get(u.getUserId()).entrySet().iterator();
 							while(iter.hasNext()) {
 								Entry<Long, PlaylistTreeNode> entry = iter.next();
 								if(!u.getPlaylistIds().contains(entry.getKey())) {
 									PlaylistTreeNode ptn = entry.getValue();
 									removeNodeFromParent(ptn);
 									iter.remove();
 									playlistIds.remove(ptn.getPlaylist().getPlaylistId());
 								}
 							}
 							// Friend might have changed friendly names, re-order if necessary
 							ftn.setFriend(u);
 							replaceNodeSorted(getRoot(), ftn);
 						} else {
 							// New friend node
 							FriendTreeNode ftn = new FriendTreeNode(u);
 							friendNodes.put(u.getUserId(), ftn);
 							playlistNodes.put(u.getUserId(), new HashMap<Long, PlaylistTreeNode>());
 							insertNodeSorted(getRoot(), ftn);
 						}
 					}
 				}
 			});
 		}
 	}
 
 	public void playlistChanged(final Playlist p) {
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				synchronized (FriendTreeModel.this) {
 					for (FriendTreeNode ftn : friendNodes.values()) {
 						if(ftn.getFriend().getPlaylistIds().contains(p.getPlaylistId())) {
 							// This user has this playlist - do we have a node for this yet?
 							long friendId = ftn.getFriend().getUserId();
 							PlaylistTreeNode ptn = playlistNodes.get(friendId).get(p.getPlaylistId());
 							if(ptn == null) {
 								ptn = new PlaylistTreeNode(p, frame);
 								playlistNodes.get(friendId).put(p.getPlaylistId(), ptn);
 								insertNodeSorted(ftn, ptn);
 								playlistIds.add(p.getPlaylistId());
 								firePathToRootChanged(ptn);
 							} else {
 								ptn.setPlaylist(p, tree.isSelectedNode(ptn));
 								replaceNodeSorted(ftn, ptn);
 								firePathToRootChanged(ptn);
 							}
 						}
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void libraryChanged(final Library lib, final Set<String> newTrackSids) {
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
				LibraryTreeNode ltn;
				FriendTreeNode ftn;
 				synchronized (FriendTreeModel.this) {
 					long uid = lib.getUserId();
					ltn = libNodes.get(uid);
					ftn = friendNodes.get(uid);
 					if(ltn == null) {
 						if(ftn == null) {
 							log.error("ERROR: library updated for userId "+uid+", but there is no friend tree node");
 							return;
 						}
 						ltn = new LibraryTreeNode(frame, lib);
 						insertNodeSorted(ftn, ltn);
 						libNodes.put(uid, ltn);
 					} else
 						ltn.setLib(lib, tree.isSelectedNode(ltn));
 				}
				firePathToRootChanged(ltn);
				
				// There is a stupid bug in some java impls that doesn't repaint the tree nodes unless you call each one individually
				firePathToRootChanged(ftn);
				firePathToRootChanged(getRoot());
 			}
 		});
 	}
 	
//	private void handleRetardedJ1.5TreeBug() {
//		
//	}
	
 	@Override
 	public void myLibraryUpdated() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void allUsersAndPlaylistsUpdated() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void userConfigChanged(UserConfig cfg) {
 		// Do nothing
 	}
 	
 	public TreePath getPlaylistTreePath(Long playlistId) {
 		// NB If the playlist is in the tree more than once (eg shared
 		// playlist), this will select the first instance only...
 		synchronized (this) {
 			for (Map<Long, PlaylistTreeNode> ptns : playlistNodes.values()) {
 				if(ptns.containsKey(playlistId))
 					return new TreePath(getPathToRoot(ptns.get(playlistId)));
 			}
 		}
 		return null;
 	}
 	
 	public synchronized TreePath getLibraryTreePath(long uid) {
 		if(libNodes.containsKey(uid))
 			return new TreePath(getPathToRoot(libNodes.get(uid)));
 		return null;
 	}
 	
 	@Override
 	public SortableTreeNode getRoot() {
 		return (SortableTreeNode) super.getRoot();
 	}
 	
 	public synchronized boolean hasPlaylist(long playlistId) {
 		return playlistIds.contains(playlistId);
 	}
 	
 	public void setTree(FriendTree tree) {
 		this.tree = tree;
 	}
 }
