 package com.robonobo.gui.model;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.swing.SortableTreeNode;
 import com.robonobo.core.api.model.User;
 
 @SuppressWarnings("serial")
 public class FriendTreeNode extends SelectableTreeNode {
 	private User friend;
 	Log log = LogFactory.getLog(getClass());
 
 	public FriendTreeNode(User friend) {
 		super(friend.getFriendlyName());
 		this.friend = friend;
 	}
 
 	public User getFriend() {
 		return friend;
 	}
 
 	public void setFriend(User friend) {
 		this.friend = friend;
 		setUserObject(friend.getFriendlyName());
 	}
 
 	@Override
 	public boolean wantSelect() {
 		return false;
 	}
 	
 	@Override
 	public boolean handleSelect() {
 		return false;
 	}
 
 	@Override
 	public int compareTo(SortableTreeNode o) {
 		if(!(o instanceof FriendTreeNode))
 			return 1;
 		FriendTreeNode other = (FriendTreeNode) o;
 		return friend.getFriendlyName().compareTo(other.getFriend().getFriendlyName());
 	}	
 }
