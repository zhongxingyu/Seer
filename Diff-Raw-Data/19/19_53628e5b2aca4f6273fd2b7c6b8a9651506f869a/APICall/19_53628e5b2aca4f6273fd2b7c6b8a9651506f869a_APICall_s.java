 package com.reddit.worddit.api;
 
 import java.io.IOException;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 public class APICall extends AsyncTask<String,String,Boolean>{
 	private Session mSession;
 	private int mCall;
 	private Object mPayload;
 	
 	private int mLastResponse;
 	private Context mContext;
 	private ProgressDialog mProgress;
 	
 	public APICall(Context c, Session s, int call) {
 		mSession = s;
 		mCall = call;
 		mContext = c;
 	}
 	
 	public Object getPayload() {
 		return mPayload;
 	}
 	
 	@Override
 	protected Boolean doInBackground(String... args) {
 		try {
 			switch(mCall) {
 			case USER_ADD: return doAdd(args);
 			case USER_LOGIN: return doLogin(args);
 			case USER_SETPROFILE: return doSetProfile(args);
 			case USER_SETAVATAR: return doSetAvatar(args);
 			case USER_GAMES: return doGetGames();
 			case USER_FRIENDS: return doGetFriends();
 			case USER_FIND: return doFindUser(args);
 			case USER_BEFRIEND: return doBefriend(args);
 			case USER_DEFRIEND: return doDefriend(args);
 			case USER_ACCEPTFRIEND: return doAcceptFriend(args);
 			case GAME_NEW: return doNewGame(args);
 			case GAME_REQUEST: return doRequestGame(args);
 			case GAME_ACCEPT: return doAcceptGame(args);
 			case GAME_REJECT: return doRejectGame(args);
 			case GAME_BOARD: return doGetBoard(args);
 			case GAME_RACK: return doGetRack(args);
 			case GAME_HISTORY: return doGetGameHistory(args);
 			case GAME_PLAY: return doPlay(args);
 			case GAME_SWAP: return doSwap(args);
 			case GAME_PASS: return doPass(args);
 			case GAME_RESIGN: return doResign(args);
 			case GAME_CHATHISTORY: return doChatHistory(args);
 			case GAME_CHATSEND: return doChatSend(args);
 			default: break;
 			}
 			
 			throw new IllegalArgumentException("Invalid API call: " + mCall);
 		}
 		catch(IOException e) {
 			// TODO: Find out what to do here.
 			return false;
 		}
 	}
 	
 	
 
 	
 	@Override
 	protected void onPreExecute() {
		// TODO Auto-generated method stub
 		mProgress = new ProgressDialog(mContext);
 		mProgress.setIndeterminate(true);
 		mProgress.setMessage("Working...");
 		mProgress.show();
		
		
		
		
 	}
 
 	@Override
 	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
 		mLastResponse = mSession.getLastResponse();
 		mProgress.dismiss();
		Toast.makeText(mContext, "session reported: " + mLastResponse, 1).show();
 	}
 
 	private boolean doAdd(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [email] [password]");
 		}
 		
 		String email = args[0], password = args[1];
 		return mSession.createAccount(email, password);
 	}
 	
 	private boolean doLogin(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [email] [password]");
 		}
 		
 		String email = args[0], password = args[1];
 		return mSession.login(email, password);
 	}
 	
 	private boolean doSetProfile(String args[]) throws IOException {
 		if(args.length != 3) {
 			throw new IllegalArgumentException("Requires [email] [password] [nickname]");
 		}
 		
 		String email = args[0], newPassword = args[1], nickname = args[2];
 		return mSession.setProfile(email, newPassword, nickname);
 	}
 	
 	private boolean doSetAvatar(String args[]) {
 		// TODO: Implement doAvatar.
 		return false;
 	}
 	
 	private boolean doGetGames() throws IOException {
 		return (mPayload = mSession.getGames()) != null;
 	}
 	
 	private boolean doGetFriends() throws IOException {
 		return (mPayload = mSession.getFriends()) != null;
 	}
 	
 	private boolean doFindUser(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id_or_email]");
 		}
 		
 		String id = args[0];
 		return (mPayload = mSession.findUser(id)) != null;
 	}
 	
 	private boolean doBefriend(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.befriend(id);
 	}
 	
 	private boolean doDefriend(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.defriend(id);
 	}
 	
 	private boolean doAcceptFriend(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.acceptFriend(id);
 	}
 	
 	private boolean doNewGame(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [ids] [rules]");
 		}
 		
 		String ids = args[0], rules = args[1];
 		return (mPayload = mSession.newGame(ids, rules)) != null;
 	}
 	
 	private boolean doRequestGame(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [email] [password]");
 		}
 		
 		int players = Integer.parseInt(args[0]);
 		String rules = args[1];
 		
 		return (mPayload = mSession.requestGame(players, rules)) != null;
 	}
 	
 	private boolean doAcceptGame(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.acceptGame(id);
 	}
 	
 	private boolean doRejectGame(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.rejectGame(id);
 	}
 	
 	private boolean doGetBoard(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return (mPayload = mSession.getBoard(id)) != null;
 	}
 	
 	private boolean doGetRack(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return (mPayload = mSession.getRack(id)) != null;
 	}
 	
 	private boolean doGetGameHistory(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [id] [limit]");
 		}
 		
 		String id = args[0];
 		int limit = Integer.parseInt(args[1]);
 		return (mPayload = mSession.getGameHistory(id, limit)) != null;
 	}
 	
 	private boolean doPlay(String args[]) throws IOException {
 		if(args.length != 5) {
 			throw new IllegalArgumentException("Requires [id] [row] [col] [down|right] [tiles]");
 		}
 		
 		int row = Integer.parseInt(args[1]), col = Integer.parseInt(args[2]);
 		String id = args[0], dir = args[3], tiles = args[4];
 		
 		
 		return (mPayload = mSession.play(id,row,col,dir,tiles)) != null;
 	}
 	
 	private boolean doSwap(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [id] [tiles]");
 		}
 		
 		String id = args[0], tiles = args[1];
 		return (mPayload = mSession.swap(id, tiles)) != null;
 	}
 	
 	private boolean doPass(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.pass(id);
 	}
 	
 	private boolean doResign(String args[]) throws IOException {
 		if(args.length != 1) {
 			throw new IllegalArgumentException("Requires [id]");
 		}
 		
 		String id = args[0];
 		return mSession.resign(id);
 	}
 	
 	private boolean doChatHistory(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [id] [limit]");
 		}
 		
 		
 		String id = args[0];
 		int limit = Integer.parseInt(args[1]);
 		
 		return (mPayload = mSession.getChatHistory(id, limit)) != null;
 	}
 	
 	private boolean doChatSend(String args[]) throws IOException {
 		if(args.length != 2) {
 			throw new IllegalArgumentException("Requires [id] [msg]");
 		}
 		
 		
 		String id = args[0], msg = args[1];
 		
 		return mSession.sendChatMessage(id, msg);
 	}
 	
 	public Boolean login(String email, String password) {
 		APICall task = (APICall) this.execute(email, password);
 		return false;
 	}
 	
 	public boolean createAccount (String email, String password) {
 		return false;
 	}
 	
 	/** Constant referring to an API call. */
 	public static final int
 		USER_ADD = 1,
 		USER_LOGIN = USER_ADD + 1,
 		USER_SETPROFILE = USER_ADD + 2,
 		USER_SETAVATAR = USER_ADD + 3,
 		USER_GAMES = USER_ADD + 4,
 		USER_FRIENDS = USER_ADD + 5,
 		USER_FIND = USER_ADD + 6,
 		USER_BEFRIEND = USER_ADD + 7,
 		USER_DEFRIEND = USER_ADD + 8,
 		USER_ACCEPTFRIEND = USER_ADD + 9,
 		GAME_NEW = USER_ADD + 10,
 		GAME_REQUEST = USER_ADD + 11,
 		GAME_ACCEPT = USER_ADD + 12,
 		GAME_REJECT = USER_ADD + 13,
 		GAME_BOARD = USER_ADD + 14,
 		GAME_RACK = USER_ADD + 15,
 		GAME_HISTORY = USER_ADD + 16,
 		GAME_PLAY = USER_ADD + 17,
 		GAME_SWAP = USER_ADD + 18,
 		GAME_PASS = USER_ADD + 19,
 		GAME_RESIGN = USER_ADD + 20,
 		GAME_CHATHISTORY = USER_ADD + 21,
 		GAME_CHATSEND = USER_ADD + 22;
 	
 }
