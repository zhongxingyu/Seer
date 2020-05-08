 package net.thesocialos.shared.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Id;
 
 import net.thesocialos.shared.ChannelApiEvents.ChApiChatUserChngState.STATETYPE;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Cached;
 import com.googlecode.objectify.annotation.NotSaved;
 import com.googlecode.objectify.annotation.Unindexed;
 
 @SuppressWarnings("serial")
 @Cached
 public class User implements Serializable {
 	
 	public static User toDTO(User user) {
 		return new User(user.email, user.getUrlAvatar(), user.background, user.firstName, user.lastName, user.role,
 				user.mobilePhone, user.address, user.bio, user.isConnected, user.chatState);
 	}
 	
 	public static User toDTO(String email, String urlAvatar, String background, String firstName, String lastName,
 			String role, String phone, String address, String bio, String tokenChannel) {
 		return new User(email, urlAvatar, background, firstName, lastName, role, phone, address, bio, tokenChannel);
 	}
 	
 	@Id private String email; // Email of the user
 	@NotSaved private Key<User> ownKey;
 	
 	@Unindexed private String password; // Password of the user
 	
 	@Unindexed private String tokenChannel; // The token of channelApi
 	
 	public boolean isConnected; // Is the user connected?
 	
 	@Unindexed public STATETYPE chatState = STATETYPE.OFFLINE;
 	
 	@Unindexed private String urlAvatar; // Avatar of the user
 	
 	@Unindexed private String background; // Background of the user
 	
 	private String firstName; // firstname
 	
 	@Unindexed private String role; // The role of the user
 	
 	private String address; // address of user
 	
 	private String bio;
 	
 	@Unindexed String lastName; // lastnameString address;
 	
 	@Unindexed String mobilePhone;
 	
 	@Unindexed String job;
 	
	private List<Key<SharedHistory>> historykey = new ArrayList<Key<SharedHistory>>();
 	
 	private Date lastTimeActive;
 	
 	Key<Group> groups[];
 	
 	private List<Key<Conversation>> conversations;
 	
 	private List<Key<OutStandingLines>> linesOffline; // Messages send offline
 	
 	List<Key<Session>> sessions = new ArrayList<Key<Session>>();
 	private List<Key<User>> contacts = new ArrayList<Key<User>>();
 	
 	// Las peticiones de amistad de los contactos
 	private List<Key<User>> petitionsContacts = new ArrayList<Key<User>>();
 	
 	List<Key<? extends Account>> accounts = new ArrayList<Key<? extends Account>>();
 	
 	List<Key<Columns>> columns = new ArrayList<Key<Columns>>();
 	
 	private List<Key<SharedHistory>> shared = new ArrayList<Key<SharedHistory>>();
 	
 	public User() {
 		
 	}
 	
 	public User(String email, String urlAvatar, String background, String firstName, String lastName, String role) {
 		this.email = email;
 		this.background = background;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.role = role;
 		this.urlAvatar = urlAvatar;
 	}
 	
 	public User(String email, String urlAvatar, String background, String firstName, String lastName, String role,
 			String phone, String address, String bio, Boolean isConnected, STATETYPE chatState) {
 		this.email = email;
 		this.background = background;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.role = role;
 		mobilePhone = phone;
 		this.address = address;
 		this.bio = bio;
 		this.isConnected = isConnected;
 		this.chatState = chatState;
 		this.urlAvatar = urlAvatar;
 	}
 	
 	public User(String email, String urlAvatar, String background, String firstName, String lastName, String role,
 			String phone, String address, String bio, String tokenChannel) {
 		this.email = email;
 		// password = password;
 		this.background = background;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.role = role;
 		mobilePhone = phone;
 		this.address = address;
 		this.bio = bio;
 		this.tokenChannel = tokenChannel;
 		this.urlAvatar = urlAvatar;
 		
 	}
 	
 	public User(String email, String urlAvatar, String password, String background, String firstName, String lastName,
 			String role, String tokenChannel) {
 		this.email = email;
 		this.password = password;
 		
 		this.background = background;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.role = role;
 		this.tokenChannel = tokenChannel;
 		this.urlAvatar = urlAvatar;
 	}
 	
 	public void addAccount(Key<? extends Account> account) {
 		accounts.add(account);
 	}
 	
 	public void addColumn(Key<Columns> column) {
 		columns.add(column);
 	}
 	
 	/**
 	 * A�ade un contacto al usuario
 	 * 
 	 * @param contact
 	 *            El contacto a a�adir
 	 * @return true si se a podido a�adir. False si ya estaba a�adido
 	 */
 	public boolean addContact(Key<User> contact) {
 		if (contacts.contains(contact)) return false;
 		contacts.add(contact);
 		return true;
 		
 	}
 	
 	/**
 	 * A�ade una conversacion
 	 * 
 	 * @param conversation
 	 *            la key a a�adir
 	 * @return true si correcto | false si no
 	 */
 	public boolean addKeyConversation(Key<Conversation> conversation) {
 		return conversations.add(conversation);
 	}
 	
 	/**
 	 * A�ade un mesaje en modo desconectado
 	 * 
 	 * @param offlineMessage
 	 *            la key a guardar
 	 * @return true si correcto | false si no
 	 */
 	public boolean addKeyOfflineMessage(Key<OutStandingLines> offlineMessage) {
 		return linesOffline.add(offlineMessage);
 	}
 	
 	/**
 	 * A�ade una invitaci�n de contacto al usuario
 	 * 
 	 * @param contact
 	 *            El contacto a a�adir
 	 * @return true si se a podido a�adir. False si ya estaba a�adido
 	 */
 	public boolean addPetitionContacts(Key<User> contact) {
 		if (petitionsContacts.contains(contact) || contacts.contains(contact)) return false;
 		petitionsContacts.add(contact);
 		return true;
 	}
 	
 	/**
 	 * A�ade un contacto desde la lista de invitaciones
 	 * 
 	 * @param contact
 	 *            El contacto a a�adir
 	 * @return true si se a podido a�adir. False si ya estaba a�adido
 	 */
 	public boolean addPetitionContactTOContact(Key<User> contact) {
 		// if (!petitionsContacts.contains(contact) && (contacts.contains(contact))) {
 		contacts.add(contact);
 		petitionsContacts.remove(contact);
 		return true;
 		// }
 		// return false;
 	}
 	
 	/**
 	 * @param historykey
 	 *            the historykey to set
 	 */
 	public void addHistoryKey(Key<SharedHistory> historykey) {
 		this.historykey.add(historykey);
 	}
 	
 	public void addSessions(Key<Session> session) {
 		sessions.add(session);
 	}
 	
 	public List<Key<? extends Account>> getAccounts() {
 		return accounts;
 	}
 	
 	public boolean deleteAccount(Key<Account> keyAccount) {
 		return accounts.remove(keyAccount);
 	}
 	
 	public String getAddress() {
 		return address;
 	}
 	
 	public String getUrlAvatar() {
 		return urlAvatar;
 	}
 	
 	public String getBackground() {
 		return background;
 	}
 	
 	/**
 	 * @return the columns
 	 */
 	public List<Key<Columns>> getColumns() {
 		return columns;
 	}
 	
 	public List<Key<User>> getContacts() {
 		return contacts;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public Key<Group>[] getGroups() {
 		
 		return groups;
 	}
 	
 	public String getLastName() {
 		return lastName;
 	}
 	
 	public Date getLastTimeActive() {
 		return lastTimeActive;
 	}
 	
 	public List<Key<Conversation>> getListKeyConversations() {
 		return conversations;
 	}
 	
 	/**
 	 * Obtiene todos los mensajes en modo desconectado
 	 * 
 	 * @return
 	 */
 	public List<Key<OutStandingLines>> getListKeyOfflineMessages() {
 		return linesOffline;
 	}
 	
 	public String getMobilePhone() {
 		return mobilePhone;
 	}
 	
 	public String getName() {
 		return firstName;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public List<Key<User>> getpetitionsContacts() {
 		return petitionsContacts;
 	}
 	
 	public String getRole() {
 		return role;
 	}
 	
 	public List<Key<Session>> getSessions() {
 		return sessions;
 	}
 	
 	/**
 	 * @return the bio
 	 */
 	public String getBio() {
 		return bio;
 	}
 	
 	/**
 	 * @return the firstName
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	/**
 	 * @return the historykey
 	 */
 	public List<Key<SharedHistory>> getHistorykey() {
 		return historykey;
 	}
 	
 	/**
 	 * @return the ownKey
 	 */
 	public Key<User> getOwnKey() {
 		return ownKey;
 	}
 	
 	/**
 	 * @return the tokenChannel
 	 */
 	public String getTokenChannel() {
 		return tokenChannel;
 	}
 	
 	public void overwriteAccountsList(List<Key<? extends Account>> newAccountsKeys) {
 		accounts.clear();
 		accounts.addAll(newAccountsKeys);
 	}
 	
 	public void overwriteColumnsList(List<Key<Columns>> newColumnsKeys) {
 		columns.clear();
 		columns.addAll(newColumnsKeys);
 	}
 	
 	public void clearAccounts() {
 		accounts.clear();
 	}
 	
 	public void resetContacts() {
 		contacts = null;
 	}
 	
 	public void resetPetionContacts() {
 		petitionsContacts = null;
 	}
 	
 	public void setAccounts(ArrayList<Key<? extends Account>> accounts) {
 		this.accounts = accounts;
 	}
 	
 	/**
 	 * @param columns
 	 *            the columns to set
 	 */
 	public void setColumns(List<Key<Columns>> columns) {
 		this.columns = columns;
 	}
 	
 	public void setGroups(Key<Group>[] groups) {
 		this.groups = groups;
 	}
 	
 	public void setLastTimeActive(Date lastTimeActive) {
 		this.lastTimeActive = lastTimeActive;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	/**
 	 * @param tokenChannel
 	 *            the tokenChannel to set
 	 */
 	public void setTokenChannel(String tokenChannel) {
 		this.tokenChannel = tokenChannel;
 	}
 	
 	public void setOwnKey(Key<User> key) {
 		ownKey = key;
 		
 	}
 	
 	/**
 	 * @param firstName
 	 *            the firstName to set
 	 */
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	
 	/**
 	 * @param address
 	 *            the address to set
 	 */
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
 	/**
 	 * @param bio
 	 *            the bio to set
 	 */
 	public void setBio(String bio) {
 		this.bio = bio;
 	}
 	
 	/**
 	 * @param lastName
 	 *            the lastName to set
 	 */
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	
 	/**
 	 * @param mobilePhone
 	 *            the mobilePhone to set
 	 */
 	public void setMobilePhone(String mobilePhone) {
 		this.mobilePhone = mobilePhone;
 	}
 	
 	/**
 	 * @param avatar
 	 *            the avatar to set
 	 */
 	public void setAvatar(String avatar) {
 		urlAvatar = avatar;
 	}
 	
 	/**
 	 * @return the shared
 	 */
 	public List<Key<SharedHistory>> getShared() {
 		return shared;
 	}
 	
 	/**
 	 * @param shared
 	 *            the shared to set
 	 */
 	public void setShared(List<Key<SharedHistory>> shared) {
 		this.shared = shared;
 	}
 	
 	public void addShare(Key<SharedHistory> share) {
 		shared.add(share);
 	}
 	
 }
