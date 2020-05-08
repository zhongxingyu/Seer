 package com.rushdevo.twittaddict;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 import com.rushdevo.twittaddict.db.UserDataSource;
 import com.rushdevo.twittaddict.db.model.User;
 import com.rushdevo.twittaddict.twitter.Authenticator;
 import com.rushdevo.twittaddict.twitter.StatusService;
 import com.rushdevo.twittaddict.twitter.UserService;
 import com.rushdevo.twittaddict.twitter.model.TwitterStatus;
 import com.rushdevo.twittaddict.twitter.model.TwitterUser;
 import com.rushdevo.twittaddict.ui.GameView;
 
 public class Twittaddict {
 	private static final String ERROR = "error";
 	private static final String NEW = "new";
 	private static final String AUTHENTICATING = "authenticating";
 	private static final String IN_PROGRESS = "in progress";
 	private static final String COMPLETE = "complete";
 	private static final String PAUSED = "paused";
 	
 	private static final int GAME_LENGTH = 60; // 60 second game length
 	
 	private String state;
 	private User user;
 	private GameView gameView;
 	private Context context;
 	private Authenticator authenticator;
 	private UserDataSource userDataSource;
 	
 	private UserService userService;
 	private StatusService statusService;
 	
 	private TwitterUser twitterUser;
 	private List<TwitterUser> friends;
 	private List<TwitterStatus> statuses;
 	
 	private Long startTime;
 	private Integer score;
 	
 	private List<String> errors;
 	
 	public Twittaddict(GameView gameView) {
 		this.gameView = gameView;
 		this.errors = new ArrayList<String>();
 		this.userDataSource = gameView.getUserDataSource();
 		this.context = this.gameView.getApplicationContext();
 		this.authenticator = new Authenticator(context, gameView);
 		this.userService = new UserService(context, this, authenticator);
 		this.statusService = new StatusService(context, this, authenticator);
 		initialize();
 	}
 	
 	public TwitterUser getTwitterUser() {
 		return this.twitterUser;
 	}
 	
 	public List<TwitterUser> getFriends() {
 		return this.friends;
 	}
 	
 	public List<TwitterStatus> getStatuses() {
 		return this.statuses;
 	}
 	
 	public boolean isErrored() {
 		return this.state.equals(ERROR);
 	}
 	
 	public boolean isNew() {
 		return this.state.equals(NEW);
 	}
 	
 	public boolean isAuthenticating() {
 		return this.state.equals(AUTHENTICATING);
 	}
 	
 	public boolean isAuthenticated() {
 		return user != null;
 	}
 	
 	public boolean isInProgress() {
 		return this.state.equals(IN_PROGRESS);
 	}
 	
 	public boolean isComplete() {
 		return this.state.equals(COMPLETE);
 	}
 	
 	public boolean isPaused() {
 		return this.state.equals(PAUSED);
 	}
 	
 	public void error(String message) {
 		this.state = ERROR;
 		this.errors.add(message);
 	}
 	
 	public void start() {
 		if (isAuthenticated() && queryGameData()) {
 			this.state = IN_PROGRESS;
 			this.score = 0;
 			this.startTime = (System.currentTimeMillis() / 1000);
 		} else {
 			error(context.getString(R.string.oauth_failure));
 		}
 	}
 	
 	public void initialize() {
 		this.errors.clear();
 		this.state = NEW;
 	}
 	
 	public void complete() {
 		this.state = COMPLETE;
 	}
 	
 	public void pause() {
 		this.state = PAUSED;
 	}
 	
 	public void resume() {
 		this.state = IN_PROGRESS;
 	}
 		
 	public List<String> getErrors() {
 		return this.errors;
 	}
 	
 	public Integer getScore() {
 		return this.score;
 	}
 	
 	/**
 	 * Finds the number of seconds remaining in the current game
 	 * 
 	 * @return The number of seconds
 	 */
 	public int getSecondsRemaining() {
 		if (this.startTime == null) {
 			return GAME_LENGTH;
 		} else {
 			long now = (System.currentTimeMillis() / 1000);
 			int diff = (int)(now - startTime);
 			int gameTime = GAME_LENGTH - diff;
 			if (gameTime < 0) return 0;
 			else return gameTime;
 		}
 	}
 	
 	public boolean authenticate() {
 		this.errors.clear();
 		this.state = AUTHENTICATING;
		if (user == null) user = authenticator.authenticateFromDatabase();
 		if (user == null) user = authenticator.authenticateFromTwitterResponse(gameView.getCurrentUri());
 		if (user == null) authenticator.authenticateFromTwitterRequest();
 		if (user != null) {
 			if (user.isNewRecord()) userDataSource.createUser(user);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	//////// PRIVATE HELPERS /////////
 	/**
 	 * Queries all game data from Twitter API
 	 * 
 	 * @return True if all query was successfully queried, and false if not.
 	 *         If false, status should be 'error' and getErrors() should have
 	 *         error messages in it
 	 */
 	private boolean queryGameData() {
 		return (queryUser() && queryFriends() && queryStatuses());
 	}
 	
 	private boolean queryUser() {
 		twitterUser = userService.getAuthenticatedUser();
 		return (twitterUser != null);
 	}
 	
 	private boolean queryFriends() {
 		friends = userService.getFriends(twitterUser);
 		return (friends != null && !friends.isEmpty());
 	}
 	
 	private boolean queryStatuses() {
 		statuses = statusService.getHomeTimeline();
 		return (statuses != null && !statuses.isEmpty());
 	}
 }
