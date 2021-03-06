 /* This code is part of Freenet. It is distributed under the GNU General
  * Public License, version 2 (or at your option any later version). See
  * http://www.gnu.org/ for further details of the GPL. */
 package plugins.FMSPlugin.WoT;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 
 import freenet.keys.FreenetURI;
 import freenet.support.UpdatableSortedLinkedList;
 import freenet.support.UpdatableSortedLinkedListKilledException;
 import freenet.support.UpdatableSortedLinkedListWithForeignIndex;
 
 import plugins.FMSPlugin.FMSBoard;
 import plugins.FMSPlugin.FMSIdentityManager;
 import plugins.FMSPlugin.FMSMessage;
 import plugins.FMSPlugin.FMSMessageManager;
 import plugins.FMSPlugin.FMSOwnIdentity;
 
 public class FMSMessageManagerWoT implements FMSMessageManager {
 	
 	/**
 	 * Contains all boards which where found in a message. References to all messages of a board are stored in
 	 * the board. Adding a newly downloaded message therefore is done by searching its board and calling 
 	 * <code>addMessage()</code> on that board. Further, the message is also added to mMessages, see below.
 	 */
 	private UpdatableSortedLinkedListWithForeignIndex mBoards = new UpdatableSortedLinkedListWithForeignIndex();
 
 	/**
 	 * Contains all messages, even though they are also stored in their FMSBoard. Used for checking whether
 	 * a message was already downloaded or not.
 	 */
 	private Hashtable<FreenetURI, FMSMessageWoT> mMessages = new Hashtable<FreenetURI, FMSMessageWoT>(); 
 
 	private ArrayList<FMSOwnIdentityWoT> mOwnIdentites = new ArrayList<FMSOwnIdentityWoT>();
 	
 	public FMSMessage get(FreenetURI uri) {
 		return mMessages.get(uri);
 	}
 
 	public synchronized FMSBoard getBoardByName(String name) {
 		return (FMSBoard)mBoards.get(name);
 	}
 	
 	public synchronized Iterator<FMSBoard> boardIterator(FMSOwnIdentity identity) {
 		return (Iterator<FMSBoard>)mBoards.iterator();
 	}
 	
 	private synchronized boolean shouldDownloadMessage(FreenetURI uri) {
 		return (mMessages.containsKey(uri));
 	}
 	
 	private synchronized void onMessageReceived(String newMessageData) throws UpdatableSortedLinkedListKilledException { 
		FMSMessageWoT newMessage = new FMSMessageWoT(null, null, null, null, null, null, null, null, null);
 		String boardName = "";
 		String boardDescription = "";
 		FMSBoard board = getBoardByName(boardName);
 		if(board == null) {
 			board = new FMSBoard(this, boardName, boardDescription);
 			mBoards.add(board);
 		}
 		
 		mMessages.put(newMessage.getURI(), newMessage);
 		board.addMessage(newMessage);
 	}
 }
