 package pl.radical.open.gg;
 
 import pl.radical.open.gg.User.UserMode;
 import pl.radical.open.gg.event.ConnectionListener;
 import pl.radical.open.gg.event.ContactListListener;
 import pl.radical.open.gg.event.LoginFailedEvent;
 import pl.radical.open.gg.event.LoginListener;
 import pl.radical.open.gg.event.MessageListener;
 import pl.radical.open.gg.event.PublicDirListener;
 import pl.radical.open.gg.event.SessionStateListener;
 import pl.radical.open.gg.event.UserListener;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * @author mateusz
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class Main2 {
 
	@SuppressWarnings("deprecation")
 	public static void main(final String args[]) throws IOException, GGException {
 
 		final ISession session = SessionFactory.createSession();
 
 		final LoginContext loginContext = new LoginContext(1336843, "dupadupa");
 
 		session.addSessionStateListener(new SessionStateListener() {
 
 			public void sessionStateChanged(final SessionState oldSessionState, final SessionState newSessionState) {
 				System.out.println("session state changed, oldState: "+oldSessionState+", newState: "+newSessionState);
 			}
 
 		});
 
 		session.getConnectionService().addConnectionListener(new ConnectionListener.Stub() {
 
 			@Override
 			public void connectionEstablished() throws GGException {
 				System.out.println("Connection established.");
 				session.getLoginService().login(loginContext);
 			}
 
 			@Override
 			public void connectionClosed() {
 				System.out.println("Connection closed.");
 			}
 
 			@Override
 			public void connectionError(final Exception ex) throws GGException {
 				System.out.println("Connection Error: "+ex.getMessage());
 				session.getConnectionService().disconnect();
 				//				IServer server = session.getConnectionService().lookupServer(loginContext.getUin());
 				//				session.getConnectionService().connect(server);
 			}
 
 		});
 		session.getLoginService().addLoginListener(new LoginListener.Stub() {
 
 			@Override
 			public void loginOK() throws GGException {
 				System.out.println("Login OK.");
 
 				OutgoingMessage.createNewMessage(376798, "body");
 				session.getMessageService().sendMessage(OutgoingMessage.createNewMessage(376798, String.valueOf(System.currentTimeMillis())));
 
 				final IUser mati = new User(376798);
 				final IUser andrzej = new User(2507261);
 
 				session.getPresenceService().addMonitoredUser(mati);
 				session.getPresenceService().addMonitoredUser(andrzej);
 
 				//				ISingleChat matiChat = session.getMessageService().createSingleChat(376798);
 				//				matiChat.sendMessage("body");
 				//				matiChat.sendMessage("dupka");
 			}
 
 			@Override
 			public void loginFailed(final LoginFailedEvent loginFailedEvent) throws GGException {
 				System.out.println("Login Failed.");
 			}
 
 			/**
 			 * @see pl.radical.open.gg.event.LoginListener.Stub#loggedOut()
 			 */
 			@Override
 			public void loggedOut() throws GGException {
 				System.out.println("Logged out...");
 				session.getConnectionService().disconnect();
 			}
 
 		});
 		session.getPresenceService().addUserListener(new UserListener() {
 
 			public void userStatusChanged(final IUser user, final IRemoteStatus newStatus) {
 				System.out.println("User changed status: "+user.getUin());
 				System.out.println("Status: "+newStatus.getStatusType());
 				System.out.println("Description: "+newStatus.getDescription());
 				System.out.println("ReturnDate: "+newStatus.getReturnDate());
 			}
 
 			public void localStatusChanged(final ILocalStatus localStatus) {
 				System.out.println("LocalStatus changed: "+localStatus);
 				System.out.println("Description: "+localStatus.getDescription());
 				System.out.println("ReturnDate: "+localStatus.getReturnDate());
 			}
 
 		});
 		session.getMessageService().addMessageListener(new MessageListener.Stub() {
 
 			@Override
 			public void messageArrived(final IIncommingMessage incommingMessage) {
 				System.out.println("MessageArrived, fromUser: "+incommingMessage.getRecipientUin());
 				System.out.println("MessageBody: "+incommingMessage.getMessageBody());
 				System.out.println("MessageID: "+incommingMessage.getMessageID());
 				System.out.println("MessageStatus: "+incommingMessage.getMessageClass());
 				System.out.println("MessageTime: "+incommingMessage.getMessageDate());
 			}
 
 			@Override
 			public void messageDelivered(final int uin, final int messageID, final MessageStatus deliveryStatus) {
 				System.out.println("MessageDelivered, toUser: "+String.valueOf(uin));
 				System.out.println("MessageDelivered, messageID: "+String.valueOf(messageID));
 				System.out.println("MessageDelivered, messageStatus: "+deliveryStatus);
 			}
 
 		});
 
 		session.getContactListService().addContactListListener(new ContactListListener() {
 
 			public void contactListExported() {
 				System.out.println("ContactList successfuly exported.");
 			}
 
 			/**
 			 * @see pl.radical.open.gg.event.ContactListListener#contactListReceived(java.util.Collection)
 			 */
 			public void contactListReceived(final Collection<LocalUser> users) {
 				System.out.println("ContactList received.");
 			}
 
 		});
 
 		session.getPublicDirectoryService().addPublicDirListener(new PublicDirListener() {
 
 			public void onPublicDirectoryUpdated(final int queryID) {
 				System.out.println("Updated pubDir");
 			}
 
 			public void onPublicDirectorySearchReply(final int queryID, final PublicDirSearchReply publicDirSearchReply) {
 				System.out.println("Got pubdir search results.");
 
 			}
 
 			public void onPublicDirectoryRead(final int queryID, final PersonalInfo pubDirReply) {
 				System.out.println("Got pubDir read reply");
 				System.out.println("FirstName: "+pubDirReply.getFirstName());
 				System.out.println("Surname: "+pubDirReply.getLastName());
 			}
 
 		});
 
 		final IUser acze = new User(1136132);
 		final IUser jaffa = new User(1542863, UserMode.BUDDY);
 
 		loginContext.getMonitoredUsers().add(acze);
 		loginContext.getMonitoredUsers().add(jaffa);
 
 		final IServer server = session.getConnectionService().lookupServer(loginContext.getUin());
 		session.getConnectionService().connect(server);
 		//session.getLoginService().login(loginContext);
 
 		//		session.getPublicDirectoryService().readFromPublicDirectory();
 		//		PublicDirInfo publicDirInfo = new PublicDirInfo();
 		//		publicDirInfo.setFirstName("Piotr");
 		//		publicDirInfo.setLastName("Kowalczyk");
 		//		publicDirInfo.setCity("Poznań");
 		//		publicDirInfo.setGender(Gender.MALE);
 		//		publicDirInfo.setNickName("Kowal");
 		//		publicDirInfo.setBirthDate("1967");
 		//		publicDirInfo.setFamilyCity("Kraków");
 		//		publicDirInfo.setFamilyName("Kowalskis Family");
 		//		session.getPublicDirectoryService().writeToPublicDirectory(publicDirInfo);
 		//		session.getPublicDirectoryService().readFromPublicDirectory();
 		//
 		//		PublicDirSearchQuery publicDirQuery = new PublicDirSearchQuery();
 		//		publicDirQuery.setCity("Szczecin");
 		//		publicDirQuery.setGender(Gender.FEMALE);
 		//		publicDirQuery.setActiveOnly(new Boolean(true));
 		//
 		//		session.getPublicDirectoryService().search(publicDirQuery);
 
 		//		loginContext.setPassword("dupadupa");
 		//		session.getLoginService().login();
 
 		//		ISingleChat matiChat = session.getMessageService().createSingleChat(376798);
 		//		matiChat.sendMessage("body");
 		//		matiChat.sendMessage("dupka");
 
 		//		OutgoingMessage.createNewMessage(376798, "body");
 		//		session.getMessageService().sendMessage(OutgoingMessage.createNewMessage(376798, String.valueOf(System.currentTimeMillis())));
 		//		session.getMessageService().sendMessage(OutgoingMessage.createMessage(376798, String.valueOf(System.currentTimeMillis())));
 		//		session.getMessageService().sendMessage(OutgoingMessage.createMessage(376798, String.valueOf(System.currentTimeMillis())));
 		//		session.getMessageService().sendMessage(OutgoingMessage.createMessageWithoutConfirmation(376798, String.valueOf(System.currentTimeMillis())));
 
 		//		LocalUser localUser1 = new LocalUser();
 		//		localUser1.setDisplayName("ziom");
 		//		localUser1.setUin(376712);
 		//
 		//		LocalUser localUser2 = new LocalUser();
 		//		localUser2.setDisplayName("jan");
 		//		localUser2.setUin(326712);
 
 		//		ArrayList localUsers = new ArrayList();
 		//		localUsers.add(localUser1);
 		//		localUsers.add(localUser2);
 
 		//		session.getContactListService().clearUserListRequest();
 		//		session.getContactListService().exportContacts(localUsers);
 		//		session.getContactListService().importContacts();
 
 		//		GGUser user3 = new GGUser(2944956, GGUserMode.BUDDY);
 		//		session.getPresenceService().addMonitoredUser(user3);
 		//
 		//		session.getPresenceService().removeMonitoredUser(user1);
 
 		//		IUser user2 = new GGUser(2040781, GGUserMode.BUDDY);
 		//		session.getPresenceService().addMonitoredUser(user2);
 
 		//		user1.setUserMode(GGUserMode.FRIEND);
 		//		session.getPresenceService().changeMonitoredUserStatus(user1);
 
 		System.out.println("JGGApi simple console MENU");
 		System.out.println("[1] - Zakonczenie programu");
 		System.out.println("[2] - Wylogowanie uzytkownika");
 		System.out.println("[3] - Connect to server.");
 		System.out.println("[4] - Zamiana statusu na dostepny.");
 		System.out.println("[5] - Zamiana statusu na niewidoczny z opisem.");
 		System.out.println("[6] - Zamiana statusu na zajety");
 		System.out.println("[7] - Eksportowanie listy kontaktow");
 		System.out.println("[8] - Importowanie listy kontaktow");
 		System.out.println("[9] - Kasowanie listy kontaktow");
 		System.out.println("[a0] - Dodaj 376798 do monitorowania o zmiane statusu");
 		System.out.println("[a1] - Usun 376798 do monitorowania o zmiane statusu");
 		System.out.println("[a2] - Zmien status na niedostepny");
 
 		boolean active = true;
 		while (active) {
 			final DataInputStream dis = new DataInputStream(System.in);
 			final String line = dis.readLine();
 			if (line.startsWith("1")) {
 				active = false;
 			} else if (line.startsWith("2")) {
 				session.getLoginService().logout();
 			} else if (line.startsWith("3")) {
 				session.getConnectionService().connect(session.getConnectionService().lookupServer(1336843));
 			} else if (line.startsWith("4")) {
 				final ILocalStatus status = session.getPresenceService().getStatus();
 				status.setStatusType(StatusType.ONLINE);
 				session.getPresenceService().setStatus(status);
 			} else if (line.startsWith("5")) {
 				final ILocalStatus status = session.getPresenceService().getStatus();
 				if (status.getStatusType() != StatusType.INVISIBLE_WITH_DESCRIPTION) {
 					status.setStatusType(StatusType.INVISIBLE_WITH_DESCRIPTION);
 					status.setDescription("Invisible desc");
 					session.getPresenceService().setStatus(status);
 				}
 			} else if (line.startsWith("6")) {
 				final ILocalStatus status = session.getPresenceService().getStatus();
 				if (status.getStatusType() != StatusType.OFFLINE_WITH_DESCRIPTION) {
 					status.setStatusType(StatusType.OFFLINE_WITH_DESCRIPTION);
 					status.setDescription("offline desc");
 					session.getPresenceService().setStatus(status);
 				}
 			} else if (line.startsWith("7")) {
 				final LocalUser localUser1 = new LocalUser();
 				localUser1.setDisplayName("mati");
 				localUser1.setNickName("mati");
 				localUser1.setEmailAddress("mati@sz.home.pl");
 				localUser1.setFirstName("Mateusz");
 				localUser1.setLastName("Szczap");
 				localUser1.setGroup("Przyjaciele");
 				localUser1.setTelephone("(91) 4220549");
 				//localUser1.setUin(376798);
 
 				final LocalUser localUser2 = new LocalUser();
 				localUser2.setDisplayName("ziom");
 				localUser2.setNickName("ziom");
 				localUser2.setEmailAddress("ziom@sz.home.pl");
 				localUser2.setFirstName("Jan");
 				localUser2.setLastName("Kurek");
 				localUser2.setGroup("Wrogowie");
 				localUser2.setTelephone("(91) 4356456");
 				//localUser2.setUin(1324545);
 
 				final LocalUser localUser3 = new LocalUser();
 				localUser3.setDisplayName("siara");
 				localUser3.setEmailAddress("siara@ncdc.pl");
 				localUser3.setNickName("siara");
 				localUser3.setFirstName("Stefan");
 				localUser3.setLastName("Siarzewski");
 				localUser3.setGroup("Przyjaciele");
 				localUser3.setTelephone("(95) 4220549");
 				//localUser3.setUin();
 
 				final Collection<LocalUser> users = new ArrayList<LocalUser>();
 				users.add(localUser1);
 				users.add(localUser2);
 				users.add(localUser3);
 
 				session.getContactListService().exportContactList(users);
 			} else if (line.startsWith("8")) {
 				session.getContactListService().addContactListListener(new ContactListListener(){
 
 					public void contactListExported() {
 						// TODO Auto-generated method stub
 
 					}
 
 					public void contactListReceived(final Collection<LocalUser> users) {
 						for (final LocalUser localUser : users) {
 							System.out.println("Uin: "+localUser.getUin());
 							System.out.println("DisplayName: "+localUser.getDisplayName());
 							System.out.println("FirstName: "+localUser.getFirstName());
 							System.out.println("LastName: "+localUser.getLastName());
 							System.out.println("Telephone: "+localUser.getTelephone());
 							System.out.println("Email: "+localUser.getEmailAddress());
 							System.out.println("Group: "+localUser.getGroup());
 							System.out.println("--------------------------");
 						}
 					}
 
 				});
 				session.getContactListService().importContactList();
 			} else if (line.startsWith("9")) {
 				session.getContactListService().clearContactList();
 			} else if (line.startsWith("a0")) {
 				final IUser mati = new User(376798);
 				session.getPresenceService().addMonitoredUser(mati);
 			} else if (line.startsWith("a1")) {
 				final IUser mati = new User(376798);
 				session.getPresenceService().removeMonitoredUser(mati);
 			} else if (line.startsWith("a2")) {
 				final ILocalStatus status = session.getPresenceService().getStatus();
 				status.setStatusType(StatusType.OFFLINE);
 				session.getPresenceService().setStatus(status);
 			} else if (line.startsWith("a3")) {
 				final ILocalStatus status = session.getPresenceService().getStatus();
 				status.setStatusType(StatusType.OFFLINE_WITH_DESCRIPTION);
 				status.setDescription("offline_with_desc");
 				session.getPresenceService().setStatus(status);
 			} else {
 				System.out.println("JGGApi simple console MENU");
 				System.out.println("[1] - Zakonczenie programu");
 				System.out.println("[2] - Wylogowanie uzytkownika");
 				System.out.println("[3] - Wyslij wiadomosc do uzytkownika 376798 z aktualna data.");
 				System.out.println("[4] - Zamiana statusu na dostepny.");
 				System.out.println("[5] - Zamiana statusu na niewidoczny z opisem.");
 				System.out.println("[6] - Zamiana statusu na zajety");
 				System.out.println("[7] - Eksportowanie listy kontaktow");
 				System.out.println("[8] - Importowanie listy kontaktow");
 				System.out.println("[9] - Kasowanie listy kontaktow");
 				System.out.println("[10] - Dodaj 376798 do monitorowania o zmiane statusu");
 				System.out.println("[11] - Usun 376798 do monitorowania o zmiane statusu");
 			}
 			try {
 				Thread.sleep(100);
 			} catch (final InterruptedException e) {
 				session.getLoginService().logout();
 			}
 		}
 
 		System.out.println("Abandon ship...");
 		//session.getConnectionService().disconnect();
 	}
 
 }
