 package server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 
 import shared.ProjectConfig;
 import shared.Utils;
 
 import comm.CommManager;
 
 import crypto.Hash;
 import crypto.SharedKeyCryptoComm;
 import database.DBManager;
 import database.DatabaseAdmin;
 import database.SocialNetworkDatabaseBoards;
 import database.SocialNetworkDatabasePosts;
 import database.SocialNetworkDatabaseRegions;
 
 public class ServerInputProcessor {
 	private OutputStream os;
 	private InputStream is;
 	private Cipher c;
 	private SecretKey sk;
 	
 	private BigInteger sendNonce;
 	private BigInteger recvNonce;
 	
 	private static final boolean DEBUG = ProjectConfig.DEBUG;
 	private static final String INVALID = ProjectConfig.COMMAND_INVALID;
 	private static final String CANCEL = ProjectConfig.COMMAND_CANCEL;
 	private static final String HELP = ProjectConfig.COMMAND_HELP;
 
 	private String user = null;
 	private String[] currentPath; // 0 = board/"freeforall"; 1 = region/FFApost; 2 = post/null
 
 	public void sendWithNonce(String msg) {
 		CommManager.send(msg, os, c, sk, sendNonce);
 		this.sendNonce = this.sendNonce.add(BigInteger.ONE);
 	}
 	
 	public String recvWithNonce() {
 		String msg = CommManager.receive(is, c, sk, recvNonce);
 		this.recvNonce = this.recvNonce.add(BigInteger.ONE);
 		return msg;
 	}
 	
 	public byte[] recvBytesWithNonce() {
 		byte[] buffer = SharedKeyCryptoComm.receiveBytes(is, c, sk, recvNonce);
 		this.recvNonce = this.recvNonce.add(BigInteger.ONE);
 		return buffer;
 	}
 	
 	public void processCommand(String inputLine) throws IOException {
 		if (inputLine.matches("^login .+")) {
 			if (user == null) {
 				processLogin(inputLine);
 			} else {
 				sendWithNonce("print Already logged in.;" );
 			}
 			return;
 		}
 		if (inputLine.matches("^register$")) {
 			if (user == null) {
 				processRegistration();
 			} else {
 				sendWithNonce("print Cannot register while logged in.;" );
 			}
 			return;
 		}
 		if (inputLine.matches("^regRequests$") ||
 				inputLine.matches("^regRequest$")) {
 			if (user != null) {
 				processRegRequests();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^addFriend .*") || inputLine.equals("addFriend")) {
 			if (user != null) {
 				processAddFriend(inputLine);
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^createBoard .+")) {
 			if (user != null) {
 				processCreateBoard(inputLine);
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^refresh$")) {
 			if (user != null) {
 				processRefresh();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^goto .+")) {
 			if (user != null) {
 				processGoto(inputLine);
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^createRegion .+")) {
 			if (user != null) {
 				processCreateRegion(inputLine);
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^post$")) {
 			if (user != null) {
 				processPost();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^reply$")) {
 			if (user != null) {
 				processReply();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^friendRequests$") ||
 				inputLine.matches("^friendRequest$")) {
 			if (user != null) {
 				processFriendRequests();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^deleteUser$") ||
 				inputLine.matches("^deleteUsers$")) {
 			if (user != null) {
 				processDeleteUser();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^showFriends$") ||
 				inputLine.matches("^showFriend$")) {
 			if (user != null) {
 				processShowFriends();
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		if (inputLine.matches("^changeUserRole$")) {
 			if (user != null) {
 				processChangeUserRole();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^transferSA$")) {
 			if (user != null) {
 				processTransferSA();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^logout$")) {
 			if (user != null) {
 				processLogout();
 			} else {
 				sendWithNonce(INVALID );
 			}
 			return;
 		}
 		if (inputLine.matches("^help$")) {
 			if (user != null) {
 				sendWithNonce("help");
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		
 		if (inputLine.matches("^participants$") ||
 				inputLine.matches("^participant$")) {
 			if (user != null) {
 				processParticipants();
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		if (inputLine.matches("^addParticipants$") || 
 				inputLine.matches("^addParticipant$")) {
 			if (user != null) {
 				processAddParticipants();
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		if (inputLine.matches("^removeParticipants$") ||
 				inputLine.matches("^removeParticipant$")) {
 			if (user != null) {
 				processRemoveParticipants();
 			} else {
 				sendWithNonce(INVALID);	
 			}
 			return;
 		}
 		if (inputLine.matches("^editParticipants$") ||
 				inputLine.matches("^editParticipant$")) {
 			if (user != null) {
 				processEditParticipants();
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		if (inputLine.matches("^addAdmins$") ||
 				inputLine.matches("^addAdmin$")) {
 			if (user != null) {
 				processAddAdmins();
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		if (inputLine.matches("^removeAdmins$") ||
 				inputLine.matches("^removeAdmin$")) {
 			if (user != null) {
 				processRemoveAdmins();
 			} else {
 				sendWithNonce(INVALID);
 			}
 			return;
 		}
 		sendWithNonce(INVALID+HELP);
 	}
 
 	public ServerInputProcessor(OutputStream os, InputStream is, 
 			Cipher c, SecretKey sk, BigInteger sendNonce, BigInteger recvNonce) {
 		this.os = os;
 		this.is = is;
 		this.c = c;
 		this.sk = sk;
 		this.currentPath = new String[3];
 		for (int i = 0; i < currentPath.length; i++) {
 			currentPath[i] = null;
 		}
 		this.sendNonce = sendNonce;
 		this.recvNonce = recvNonce;
 	}
 
 	private void processLogin(String inputLine) {
 		Connection conn = DBManager.getConnection();
 		String username = Utils.getValue(inputLine).toLowerCase();
 
 		boolean userExist = false;
 		boolean pwMatch = false;
 		
 		String pwhash = "";
 		String command = "";
 		String salt = "";
 		
 		// check username existence
 		String[] userInfo = DatabaseAdmin.getUserInfo(conn, username);
 		if (userInfo != null) {
 			userExist = true;
 			pwhash = userInfo[1];
 			salt = pwhash.substring(0, Hash.SALT_STRING_LENGTH);
 		}
 //		if (userExist) {
 //			command = "setSalt "+pwhash.substring(0, Hash.SALT_STRING_LENGTH) + ";";
 //		}
 		// ask for password
 		command += "print Input password:;getPassword";
 		sendWithNonce(command);
 //		String enteredPwdHash = recvWithNonce();
 		if (DEBUG) {
 			char[] testChars = "testing".toCharArray();
 			byte[] testBytes = Utils.charToByteArray(testChars);
 			try {
 				System.err.println("testBytes: " + new String(testBytes, "UTF-16"));
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 			char[] testChar2 = Utils.byteToCharArray(testBytes);
 			System.err.print("char array: ");
 			for (char c: testChar2) {
 				System.err.printf("%c", c);
 			}
 			System.err.println();
 		}
 		char[] pwdChars = Utils.byteToCharArray(recvBytesWithNonce());
 		String enteredPwdHash = Hash.hashExistingPwd(salt, pwdChars);
 		
 		
 		// check password
 		if (userExist) {
 			pwMatch = Hash.comparePwd(pwhash, enteredPwdHash);
 		}
 
 		// Output for Client
 		if (userExist && pwMatch) {
 			user = username;
 			command = "setLoggedIn true;" + SocialNetworkAdmin.printUserInfo(conn, username);
 
 			// printing out boards
 			sendWithNonce(command + SocialNetworkNavigation.printPath(currentPath)
 					+ SocialNetworkBoards.viewBoards(user));
 		} else {
 			sendWithNonce("print username does not exist or invalid password.");
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void processRegistration(){
 		String newUser = "";
 		sendWithNonce("print Choose a username that's between 2-50 characters long. " +
 				"Only use digits, letters, and underscore:;" +
 				"askForInput;");
 
 		boolean invalid = true;
 		Connection conn = DBManager.getConnection();
 
 		// check if username already exist
 		while (invalid) {
 			newUser = recvWithNonce().toLowerCase();
 			if (newUser.equals("cancel")) {
 				sendWithNonce(CANCEL);
 				return;
 			}
 			String[] userInfo = DatabaseAdmin.getUserInfo(conn, newUser);
 			if (userInfo == null)
 				invalid = false;
 
 			String command = "";
 			
 			if (invalid || newUser.equals("cancel") || newUser.equals("deleteduser")) {
 				// if the last 2 conds lead to this block, set invalid to true again
 				invalid = true;
 				command = "print Username already exist. Choose a different one.;"
 						+ "askForInput;";
 				sendWithNonce(command);
 			} else if (!newUser.matches("^[0-9a-z_]{2,50}$")) {
 				invalid = true;
 				command = "print Invalid username format. Please choose another one.;" +
 						"askForInput;";
 				sendWithNonce(command);
 			}
 			
 		}
 
 		// username isn't already in the DB
 		boolean groupExist = false;
 		String command = "";
 		Map<Integer, String> groupList = DatabaseAdmin.getGroupList(conn);
 		
 		String groupNum = "";
 		int aid = 0;
 		
 		// check if chosen group exist
 		while (!groupExist) {
 			command += SocialNetworkAdmin.displayGroupList(conn, groupList, newUser);
 			sendWithNonce(command);
 			
 			groupNum = recvWithNonce();
 			if (groupNum.equals("cancel")) {
 				sendWithNonce(CANCEL);
 				return;
 			}
 			
 			try {
 				aid = Integer.parseInt(groupNum);
 				if (!groupList.containsKey(aid)) {
 					command = "print Please choose a group from the list.;";
 				} else {
 					groupExist = true;
 				}
 			} catch (NumberFormatException e) {
 				command = "print Please input the NUMBER corresponding to the group.;";
 			}
 		}
 		
 		// create password
 		sendWithNonce("createPassword");
 		String pwdStore = recvWithNonce();
 		
 		sendWithNonce(SocialNetworkAdmin.insertRegRequest(conn, newUser, aid, pwdStore));
 		DBManager.closeConnection(conn);
 	}
 
 	private void processRegRequests() {
 		Connection conn = DBManager.getConnection();
 		String[] currentUser = DatabaseAdmin.getUserInfo(conn, user);
 		// makes sure user is an admin
 		if (currentUser[3].equals("admin") || currentUser[3].equals("sa")) {
 			String command = SocialNetworkAdmin.regRequests(conn, user);
 			sendWithNonce(command);
 			if (command.endsWith("askForInput;")) {
 				regApproval(conn, recvWithNonce());
 			}
 		} else {
 			sendWithNonce(INVALID+HELP);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void regApproval(Connection conn, String input) {
 		if (input.equals("cancel")) {
 			sendWithNonce(CANCEL);
 			return;
 		}
 		String[] currentUser = DatabaseAdmin.getUserInfo(conn, user);
 		// makes sure user is an admin
 		if (currentUser[3].equals("admin") || currentUser[3].equals("sa")) {
 			String command = "";
 			if (input.matches("^approve.+")) {
 				String value = Utils.getValue(input).toLowerCase();
 				String delim = " *, *";
 				String[] approvedUsers = value.split(delim);
 				for (String u: approvedUsers) {
 					command += SocialNetworkAdmin.regApprove(conn, u);
 				}
 				sendWithNonce(command);
 				return;
 			}
 			if (input.matches("^remove.+")) {
 				String value = Utils.getValue(input).toLowerCase();
 				String delim = " *, *";
 				String[] deletingUsers = value.split(delim);
 				for (String u: deletingUsers) {
 					command += SocialNetworkAdmin.regRemove(conn, u);
 				}
 				sendWithNonce(command);
 				return;
 			}
 		} else {
 			sendWithNonce(INVALID+HELP);
 		}
 	}
 
 	private void processAddFriend(String input) {
 		Connection conn = DBManager.getConnection();
 		// Stores a list of users that is not the current user, who is not a
 		// friend of the
 		// current user, and has not requested current user as friend
 		// Users[0]: username. Users[1]: group name
 		List<String[]> friendableUsers = DatabaseAdmin.getFriendableUsers(conn, user);
 
 		// input of format "addFriend" or "addFriend b"
 		boolean userExist = false;
 		String toFriend = "";
 		String command = "";
 		while (!userExist) {
 			String prefix = "";
 			if (!input.equals("addFriend")) {
 				prefix = Utils.getValue(input);
 			}
 			command += SocialNetworkAdmin.displayFriendableUsers(
 					conn, prefix, friendableUsers);
 			sendWithNonce(command);
 
 			toFriend = recvWithNonce().toLowerCase();
 			if (toFriend.equals("cancel")) {
 				sendWithNonce(CANCEL);
 				return;
 			}
 			for (String[] userInfo : friendableUsers) {
 				if (userInfo[0].equals(toFriend)) {
 					userExist = true;
 					break;
 				}
 			}
 			if (!userExist) {
 				command = "print Cannot friend " + toFriend + ";";
 			}
 		}
 
 		// Gets back name of person to add as friend
 		addFriend(conn, toFriend);
 		DBManager.closeConnection(conn);
 	}
 
 	private void addFriend(Connection conn, String username) {
 		// username exists in the system.
 		sendWithNonce("print Are you sure you want to add " + username
 				+ " as a friend? (y/n);askForInput");
 		String input = recvWithNonce();
 		String command = "";
 		if (input.equals("y")) {
 			command = SocialNetworkAdmin.insertFriendRequest(conn, username, user);
 		} else if (input.equals("n") || input.equals("cancel")) {
 			command = CANCEL;
 		} else {
 			command = INVALID + CANCEL;
 		}
 		sendWithNonce(command);
 	}
 
 	private void processFriendRequests() {
 		Connection conn = DBManager.getConnection();
 		String command = SocialNetworkAdmin.friendRequests(conn, user);
 		sendWithNonce(command);
 		if (command.endsWith("askForInput;")) {
 			friendApproval(conn, recvWithNonce());
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void friendApproval(Connection conn, String input) {
 		String command = "";
 		if (input.equals("cancel")) {
 			command = CANCEL;
 		} else if (input.matches("^approve.+")) {
 			String value = Utils.getValue(input).toLowerCase();
 			String delim = " *, *";
 			String[] approvedFriends = value.split(delim);
 			for (String u: approvedFriends) {
 				command += SocialNetworkAdmin.friendApprove(conn, u, user);
 			}
 		} else if (input.matches("^remove.+")) {
 			String value = Utils.getValue(input).toLowerCase();
 			String delim = " *, *";
 			String[] usersToDelete = value.split(delim);
 			for (String u: usersToDelete) {
 				command += SocialNetworkAdmin.friendReqRemove(conn, u, user);
 			}
 		} else {
 			command = INVALID + CANCEL;
 		}
 		sendWithNonce(command);
 	}
 
 	private void processDeleteUser() {
 		Connection conn = DBManager.getConnection();
 		String[] userInfo = DatabaseAdmin.getUserInfo(conn, user);
 
 		// check to see if user is actually a SA
 		if (userInfo[3].equals("sa")) {
 			// Stores a list of deletable users
 			List<String[]> deletableUsers = DatabaseAdmin.getOtherUsersInGroup(conn, user);
 
 			boolean userDeletable = false;
 			String toDelete = "";
 			String command = "";
 			
 			while (!userDeletable) {
 				command += SocialNetworkAdmin.displayDeletableUsers(deletableUsers);
 				sendWithNonce(command);
 
 				toDelete = recvWithNonce().toLowerCase();
 				if (toDelete.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				for (String[] user: deletableUsers) {
 					if (toDelete.equals(user[0])) {
 						userDeletable = true;
 						break;
 					}
 				}
 				if (!userDeletable) {
 					command = "print Cannot delete " + toDelete + ";";
 				}
 			}
 
 			// toDelete is deletable
 			deleteUser(conn, toDelete);
 		} else {
 			sendWithNonce(INVALID + HELP);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void deleteUser(Connection conn, String username) {
 		// username is a deletable user
 		sendWithNonce("print User deletions cannot be undone.;"
 				+ "print Are you sure you want to delete this user? (y/n);askForInput");
 		String input = recvWithNonce();
 		String command;
 		if (input.equals("y")) {
 			command = SocialNetworkAdmin.deleteUser(conn, username);
 		} else if (input.equals("n") || input.equals("cancel")) {
 			command = CANCEL;
 		} else {
 			command = INVALID+CANCEL;
 		}
 		sendWithNonce(command);
 	}
 
 	private void processShowFriends() {
 		Connection conn = DBManager.getConnection();
 		String command = SocialNetworkAdmin.showFriends(conn, user);
 		sendWithNonce(command);
 		DBManager.closeConnection(conn);
 	}
 
 	private void processChangeUserRole() {
 		Connection conn = DBManager.getConnection();
 		String[] userInfo = DatabaseAdmin.getUserInfo(conn, user);
 		
 		// check if user is SA
 		if (userInfo[3].equals("sa")) {
 			List<String[]> changeableUsers = DatabaseAdmin.getOtherUsersInGroup(conn, user);
 	
 			boolean userChangeable = false;
 			String toChange = "";
 			String role = "";
 			String command = "";
 	
 			while (!userChangeable) {
 				command += SocialNetworkAdmin.displayRoleChange(changeableUsers);
 				sendWithNonce(command);
 	
 				toChange = recvWithNonce().toLowerCase();
 				if (toChange.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				for (String[] u : changeableUsers) {
 					if (toChange.equals(u[0])) {
 						if (u[1].equals("admin")) {
 							role = "member";
 						} else {
 							role = "admin";
 						}
 						userChangeable = true;
 						break;
 					}
 				}
 				if (!userChangeable) {
 					command = "print Cannot change role for " + toChange + ";";
 				}
 			}
 	
 			// toChange is changeable
 			command = SocialNetworkAdmin.changeRole(conn, toChange, role);
 			sendWithNonce(command);
 		} else {
 			sendWithNonce(INVALID + HELP);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void processTransferSA() {
 		Connection conn = DBManager.getConnection();
 		String[] userInfo = DatabaseAdmin.getUserInfo(conn, user);
 		
 		// check if user is SA
 		if (userInfo[3].equals("sa")) {
 			List<String> groupAdmins = DatabaseAdmin.getAdminsOfGroup(conn, user);
 
 			boolean transferableUser = false;
 			String toChange = "";
 			String command = "";
 
 			while (!transferableUser) {
 				command += SocialNetworkAdmin.displaySATransferableUsers(groupAdmins);
 				sendWithNonce(command);
 
 				toChange = recvWithNonce().toLowerCase();
 				if (toChange.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				if (groupAdmins.contains(toChange)) {
 					transferableUser = true;
 				}
 				if (!transferableUser) {
 					command = "print Cannot transfer SA role to " + toChange + ";";
 				}
 			}
 
 			// toChange is an admin that can have SA transferred to
 			command = SocialNetworkAdmin.transferSA(conn, user, toChange);
 			sendWithNonce(command);
 		} else {
 			sendWithNonce(INVALID + HELP);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void processLogout() {
 		user = null;
 		for (int i = 0; i < currentPath.length; i++) {
 			currentPath[i] = null;
 		}
 		sendWithNonce("print Logged out.;setLoggedIn false");
 	}
 
 	private void processParticipants() {
 		Connection conn = DBManager.getConnection();
 		String board = currentPath[0];
 		String command = "";
 		String wrongLocation = "print Goto a region or freeforall post to view participants " +
 				"in that region/post.;";
 
 		if (board == null) {
 			command = wrongLocation;
 		} else {
 			String region = currentPath[1];
 			if (region == null) {
 				command = wrongLocation;
 			} else {
 				command = SocialNetworkAdmin.displayParticipAndAdmins(conn, board, region);
 			}
 		}
 		
 		sendWithNonce(command);
 		DBManager.closeConnection(conn);
 	}
 
 	/**
 	 * Returns the error command if user not able to add participant to the current 
 	 * directory. Returns the empty string if user does have permission.
 	 * @param conn
 	 * @return
 	 */
 	private String participantsError(Connection conn) {
 		String command = "";
 		String wrongLocation = "print Goto a region or freeforall post to view " +
 				"participants in that region/post.;";
 		String board = currentPath[0];
 		if (board == null) {
 			command = wrongLocation;
 		} else {
 			String region = currentPath[1];
 			if (region == null) {
 				command = wrongLocation;
 			} else {
 				boolean hasPerm;
 				if (board.equals("freeforall")) {
 					hasPerm = SocialNetworkDatabasePosts.isFFAPostCreator(
 							conn, user, Integer.parseInt(region));
 				} else {
 					hasPerm = SocialNetworkDatabaseRegions.isRegionManager(
 							conn, user, board, region);
 				}
 				if (!hasPerm) {
 					command = INVALID;
 				}
 			}
 		}
 		return command;
 	}
 
 	private void processAddParticipants() {
 		Connection conn = DBManager.getConnection();
 		String command = participantsError(conn);
 		if (!command.equals("")) {
 			sendWithNonce(command);
 		} else {
 			String board = currentPath[0];
 			String region = currentPath[1];
 			List<String> addables = SocialNetworkAdmin.getAddableParticip(
 					conn, user, board, region);
 
 			List<String> addUsers = null;
 			String priv = "";
 			
 			// Validity check
 			command = "";
 			boolean validParticip = false;
 			while (!validParticip) {
 				command += SocialNetworkAdmin.displayAddableParticip(addables, board);
 				sendWithNonce(command);
 				String input = recvWithNonce().toLowerCase();
 
 				if (input.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				
 				// Checking for valid command
 				boolean validCommand = false;
 				String value = "";
 				if (input.matches("^view .+")) {
 					priv = "view";
 					validCommand = true;
 					value = Utils.getValue(input);
 				} else if (input.matches("^viewpost .+")) {
 					priv = "viewpost";
 					validCommand = true;
 					value = Utils.getValue(input);
 				} else {
 					command = INVALID;
 				}
 				if (validCommand) {
 					addUsers = Arrays.asList(value.split(" *, *"));
 					validParticip = addables.containsAll(addUsers);
 					if (!validParticip) {
 						command = "print You do not have permission to add all the " +
 								"users you specified.;print ;";
 					}
 				}
 			}
 			// Participants to add are valid
 			command = "";
 			for (String u: addUsers) {
 				command += SocialNetworkRegions.addParticipant(conn, board, region, 
 						u, priv, user);
 			}
 			sendWithNonce(command);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void processRemoveParticipants() {
 		Connection conn = DBManager.getConnection();
 		String command = participantsError(conn);
 		if (!command.equals("")) {
 			sendWithNonce(command);
 		} else {
 			String board = currentPath[0];
 			String region = currentPath[1];
 			List<String> removables = DatabaseAdmin.getParticipantsOne(
 					conn, board, region);
 
 			List<String> usersToRemove = null;
 			
 			// Validity check
 			command = "";
 			boolean validParticip = false;
 			while (!validParticip) {
 				command += SocialNetworkAdmin.displayRemoveParticip(conn, board, region);
 				sendWithNonce(command);
 				String input = recvWithNonce().toLowerCase();
 
 				if (input.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				
 				usersToRemove = Arrays.asList(Utils.getValue(input).split(" *, *"));
 				validParticip = removables.containsAll(usersToRemove);
 				if (!validParticip) {
 					command = "print You do not have permission to remove all the " +
 							"users you specified.;print ;";
 				}
 			}
 			
 			// Participants to remove are valid
 			command = "";
 			for (String u: usersToRemove) {
 				command += SocialNetworkAdmin.removeParticipant(conn, board, region, u);
 			}
 			sendWithNonce(command);
 		}
 		DBManager.closeConnection(conn);
 	}
 	
 	private void processEditParticipants() {
 		Connection conn = DBManager.getConnection();
 		String command = participantsError(conn);
 		if (!command.equals("")) {
 			sendWithNonce(command);
 		} else {
 			String board = currentPath[0];
 			String region = currentPath[1];
 			List<String[]> editables = DatabaseAdmin.getParticipants(
 					conn, board, region);
 
 			String toEdit = "";
 			String priv = "";
 			
 			// Validity check
 			command = "";
 			boolean validParticip = false;
 			while (!validParticip) {
 				command += SocialNetworkAdmin.displayEditableParticip(editables);
 				sendWithNonce(command);
 				toEdit = recvWithNonce().toLowerCase();
 
 				if (toEdit.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				
 				for (String[] e: editables) {
 					if (e[0].equals(toEdit)) {
 						validParticip = true;
 						if (e[1].equals("view")) {
 							priv = "viewpost";
 						} else if (e[1].equals("viewpost")) {
 							priv = "view";
 						}
 						break;
 					}
 				}
 				if (!validParticip) {
 					command = "print You are not authorized to change permission " +
 							"for the user you specified.;print ;";
 				}
 			}
 			
 			// Participant to edit is valid
 			command = SocialNetworkAdmin.editParticipant(
 					conn, board, region, toEdit, priv);
 			sendWithNonce(command);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private String adminEditError(Connection conn) {
 		String wrongLocation = "print Goto a board (not freeforall) to add admin to it.;";
 		String board = currentPath[0];
 		if (board == null) {
 			return wrongLocation;
 		} else if (board.equals("freeforall")) {
 			return wrongLocation;
 		} else {
 			if (!SocialNetworkDatabaseBoards.isBoardManager(conn, user, board)) {
 				return INVALID;
 			}
 		}
 		return "";
 	}
 
 	private void processAddAdmins() {
 		Connection conn = DBManager.getConnection();
 		String error = adminEditError(conn);
 		if (!error.equals("")) {
 			sendWithNonce(error);
 		} else {
 			String board = currentPath[0];
 			List<String> addables = DatabaseAdmin.getAddableAdmins(conn, board, user);
 			String command = "";
 			boolean valid = false;
 			List<String> usersToAdd = null;
 			while (!valid) {
 				command += SocialNetworkAdmin.displayAddableAdmins(addables);
 				sendWithNonce(command);
 				String input = recvWithNonce().toLowerCase();
 				
 				if (input.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				
 				usersToAdd = Arrays.asList(input.split(" *, *"));
 				valid = addables.containsAll(usersToAdd);
 				if (!valid) {
 					command = "print You do not have permission to add all the " +
 							"admins you specified.;print ;";
 				}
 			}
 			// Admins to add are valid
 			command = "";
 			for (String u: usersToAdd) {
 				command += SocialNetworkAdmin.addAdmin(conn, board, u);
 			}
 			sendWithNonce(command);
 		}
 		DBManager.closeConnection(conn);
 	}
 	
 	private void processRemoveAdmins() {
 		Connection conn = DBManager.getConnection();
 		String error = adminEditError(conn);
 		if (!error.equals("")) {
 			sendWithNonce(error);
 		} else {
 			String board = currentPath[0];
 			List<String> removables = DatabaseAdmin.getAdminsOfBoard(conn, board);
 			String command = "";
 			boolean valid = false;
 			List<String> usersToRemove = null;
 			while (!valid) {
 				command += SocialNetworkAdmin.displayRemovableAdmins(removables, user);
 				sendWithNonce(command);
 				String input = recvWithNonce().toLowerCase();
 				
 				if (input.equals("cancel")) {
 					sendWithNonce(CANCEL);
 					return;
 				}
 				
 				usersToRemove = Arrays.asList(input.split(" *, *"));
 				valid = removables.containsAll(usersToRemove);
 				if (!valid) {
 					command = "print You do not have permission to remove all the " +
 							"admins you specified.;print ;";
 				}
 			}
 			// Admins to remove are valid
 			command = "";
 			for (String u: usersToRemove) {
 				command += SocialNetworkAdmin.removeAdmin(conn, board, u);
 			}
 			sendWithNonce(command);
 		}
 		DBManager.closeConnection(conn);
 	}
 	
 
 	/**
 	 * Creates a board. MUST be in the home directory.
 	 */
 	private void processCreateBoard(String input) throws IOException {
 		/*
 		 * Ensure the person is in the right place to create a board (on the
 		 * homepage)
 		 */
 		if (currentPath[0] != null) {
 			sendWithNonce("print Must be at Home to create a board");
 		} else {
 			String boardname = input.substring(("createBoard ").length());
 			sendWithNonce(SocialNetworkBoards.createBoard(user, boardname));
 		}
 	}
 
 	/**
 	 * Depending on where the user is, fetches the correct view of information.
 	 * The user prints their current path and the information associated with
 	 * it.
 	 */
 	private void processRefresh() {
 		String boardName = currentPath[0];
 		if (boardName == null) {
 			sendWithNonce(SocialNetworkAdmin.printUserInfo(user) + SocialNetworkNavigation.printPath(currentPath)
 					+ "print ;" + SocialNetworkBoards.viewBoards(user));
 		} else if (boardName.equals("freeforall")) {
 			/* No regions */
 			String postNum = currentPath[1];
 			if (postNum == null) { // Merely in the board
 				sendWithNonce(SocialNetworkAdmin.printUserInfo(user) + SocialNetworkNavigation.printPath(currentPath)
 						+ "print ;"
 						+ SocialNetworkPosts
 								.viewPostList(user, boardName, null, false));
 			} else { // Inside the post
 				sendWithNonce(SocialNetworkAdmin.printUserInfo(user) + SocialNetworkNavigation.printPath(currentPath)
 						+ "print ;"
 						+ SocialNetworkPosts.viewPost(user, boardName, null,
 								Integer.parseInt(postNum), false));
 			}
 		} else { // a regular board
 			String regionName = currentPath[1];
 			if (regionName == null) { // Merely in the board
 				sendWithNonce(SocialNetworkAdmin.printUserInfo(user) + SocialNetworkNavigation.printPath(currentPath)
 						+ "print ;"
 						+ SocialNetworkRegions.viewRegions(user, boardName, false));
 			} else {
 				String postNum = currentPath[2];
 				if (postNum == null) { // Merely in the region
 					sendWithNonce(SocialNetworkAdmin.printUserInfo(user) + SocialNetworkNavigation.printPath(currentPath)
 							+ "print ;"
 							+ SocialNetworkPosts.viewPostList(user, boardName,
 									regionName, false));
 				} else { // Inside the post
 					sendWithNonce(SocialNetworkAdmin.printUserInfo(user) + SocialNetworkNavigation.printPath(currentPath)
 							+ "print ;"
 							+ SocialNetworkPosts.viewPost(user, boardName,
 									regionName, Integer.parseInt(postNum), false));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Depending on where the user is, processes where the user should go.
 	 */
 	private void processGoto(String inputLine) {
 		String destination = inputLine.substring(("goto ").length());
 		int validDest = SocialNetworkNavigation.validDestination(currentPath,
 				destination);
 		switch (validDest) {
 		case -1: /* Go backwards */
 			SocialNetworkNavigation.goBack(currentPath);
 			processRefresh();
 			break;
 		case 2: /* Go immediately home */
 			for (int i = 0; i < currentPath.length; i++) {
 				currentPath[i] = null;
 			}
 			processRefresh();
 			break;
 		case 1: /* Go forward in the hierarchy to destination */
 			/* Have different cases depending on the current path */
 			if (currentPath[0] == null) {
 				sendWithNonce(SocialNetworkNavigation.goToBoard(user,
 						currentPath, destination));
 			} else if (currentPath[0].equals("freeforall")) {
 				Integer postNum = null;
 				try {
 					postNum = Integer.parseInt(destination);
 				} catch (NumberFormatException e) {
 					sendWithNonce("print You entered an invalid post number. Type \"goto ###\", or \"goto ..\" to "
 							+ "go backwards");
 				}
 				if (postNum != null) {
 					sendWithNonce(SocialNetworkNavigation.goToPost(user,
 							currentPath, postNum.intValue()));
 				}
 			} else {
 				if (currentPath[1] != null) {
 					Integer postNum = null;
 					try {
 						postNum = Integer.parseInt(destination);
 					} catch (NumberFormatException e) {
 						sendWithNonce("print You entered an invalid post number. Type \"goto ###\", or \"goto ..\" to "
 								+ "go backwards");
 					}
 					if (postNum != null) {
 						sendWithNonce(SocialNetworkNavigation.goToPost(user,
 								currentPath, postNum));
 					}
 				} else {
 					sendWithNonce(SocialNetworkNavigation.goToRegion(user,
 							currentPath, destination));
 				}
 			}
 			break;
 		default:
			sendWithNonce("print Invalid destination given your current path: ;"
 					+ SocialNetworkNavigation.printPath(currentPath) + ".; "
 					+ "print You can go backwards by typing \"..\" ");
 
 		}
 	}
 
 	/**
 	 * Creates a region for the user. The user must be in a board (except
 	 * freeforall) to execute the command.
 	 */
 	private void processCreateRegion(String inputLine) {
 		String boardName = currentPath[0];
 		String regionName = inputLine.substring(("createRegion ").length());
 		if (boardName == null) {
 			sendWithNonce("print Must be in the desired board in order to create the region.");
 		} else if (boardName.equals("freeforall")) {
 			sendWithNonce("print Cannot create regions in the freeforall board.");
 		} else if (currentPath[1] != null) {
 			sendWithNonce("print Must be exactly in the desired board (i.e., not inside a region in the board) "
 					+ "in order to create the region");
 		} else {
 			sendWithNonce(SocialNetworkRegions.createRegion(user, currentPath[0],
 					regionName));
 		}
 	}
 
 	private void processPost() {
 		/* Verify the user is in the right place to create a post */
 		String boardName = currentPath[0];
 		boolean canPost = false;
 		if (boardName == null) {
 			sendWithNonce("print Must be within a board's region or in the freeforall board to create a post");
 		} else if (boardName.equals("freeforall")) {
 			String postNum = currentPath[1];
 			if (postNum == null) {
 				canPost = true;
 			} else {
 				sendWithNonce("print Must go back to the board page to create a post (not inside a post)");
 			}
 		} else { // in a regular board
 			String regionName = currentPath[1];
 			if (regionName == null) {
 				sendWithNonce("print Must be within a board's region or in the freeforall board to create a post");
 			} else {
 				String postNum = currentPath[2];
 				if (postNum == null) { // in a board, region, not in a post
 					canPost = true;
 				} else {
 					sendWithNonce("print Must go back to the region page to create a post (not inside a post)");
 				}
 			}
 		}
 		if (canPost) {
 			//AUTHORIZATION FUNCTION and EXISTS CHECK
 			String authToPost = SocialNetworkPosts.authorizedToPost(user, currentPath[0], currentPath[1]);
 			if (!authToPost.equals("true")) {
 				sendWithNonce(authToPost);
 				return ;
 			}
 			sendWithNonce("print Start typing your content. Type 'cancel' after any new line to cancel.;print "
 					+ "Press enter once to insert a new line.;print Press enter twice to submit.;askForInput ");
 			String content = recvWithNonce();
 			while (content.equals("")) {
 				sendWithNonce("print Content is empty. Please try again. Type 'cancel' to cancel.;askForInput ");
 				content = recvWithNonce();
 			}
 			boolean cancelled = content.trim().equals("cancel");
 			String additionalContent = "";
 			while (!cancelled) {
 				sendWithNonce("print ;askForInput ");
 				additionalContent = recvWithNonce();
 				if (additionalContent.equals("")) {
 					break;
 				} else if (additionalContent.trim().equals("cancel")) {
 					cancelled = true;
 				} else {
 					content += ";print \t" + additionalContent;
 				}
 			}
 			if (cancelled) {
 				sendWithNonce("print Post Creation cancelled");
 			} else {
 				sendWithNonce(SocialNetworkPosts.createPost(user, content,
 						currentPath[0], currentPath[1]));
 			}
 		}
 	}
 
 	/**
 	 * Similar to processPost basically... except that you must be in a post
 	 * 
 	 * @throws IOException
 	 */
 	private void processReply() {
 		/* Verify the user is in the right place to create a post */
 		String boardName = currentPath[0];
 		String postNum = "";
 		boolean canReply = false;
 		if (boardName == null) {
 			sendWithNonce("print Must be within a post to create a reply");
 		} else if (boardName.equals("freeforall")) {
 			postNum = currentPath[1];
 			if (postNum == null) {
 				sendWithNonce("print Must be within a post to create a reply");
 			} else {
 				canReply = true;
 			}
 		} else { // in a regular board
 			String regionName = currentPath[1];
 			if (regionName == null) {
 				sendWithNonce("print Must be within a post to create a reply");
 			} else {
 				postNum = currentPath[2];
 				if (postNum == null) { // in a board, region, not in a post
 					sendWithNonce("print Must be within a post to create a reply");
 				} else {
 					canReply = true;
 				}
 			}
 		}
 		if (canReply) {
 			//AUTHORIZATION FUNCTION and EXISTS CHECK
 			String authToReply = SocialNetworkPosts.authorizedToReply(user, currentPath[0], currentPath[1], Integer.parseInt(postNum));
 			if (!authToReply.equals("true")) {
 				sendWithNonce(authToReply);
 				return ;
 			}
 			sendWithNonce("print Start typing your content. Type 'cancel' after any new line to cancel.;print "
 					+ "Press enter once to insert a new line.;print Press enter twice to submit.;askForInput ");
 			String content = recvWithNonce();
 			while (content.equals("")) {
 				sendWithNonce("print Content is empty. Please try again. Type 'cancel' to cancel.;askForInput ");
 				content = recvWithNonce();
 			}
 			boolean cancelled = content.trim().equals("cancel");
 			String additionalContent = "";
 			while (!cancelled) {
 				sendWithNonce("print ;askForInput ");
 				additionalContent = recvWithNonce();
 				if (additionalContent.equals("")) {
 					break;
 				} else if (additionalContent.trim().equals("cancel")) {
 					cancelled = true;
 				} else {
 					content += ";print \t" + additionalContent;
 				}
 			}
 			if (cancelled) {
 				sendWithNonce("print Reply Creation cancelled");
 
 			} else {
 				sendWithNonce(SocialNetworkPosts.createReply(user, content,
 						currentPath[0], currentPath[1],
 						Integer.parseInt(postNum)));
 			}
 		}
 	}
 }
