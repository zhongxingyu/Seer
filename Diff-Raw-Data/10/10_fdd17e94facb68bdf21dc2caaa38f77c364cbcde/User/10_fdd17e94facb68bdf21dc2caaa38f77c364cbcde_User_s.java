 package newsrack.user;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import newsrack.NewsRack;
 import newsrack.archiver.Source;
 import newsrack.database.DB_Interface;
 import newsrack.filter.Issue;
 import newsrack.filter.NR_Collection;
 import newsrack.filter.NR_CollectionType;
 import newsrack.filter.PublicFile;
 import newsrack.filter.UserFile;
 import newsrack.filter.parser.NRLanguageParser;
 import newsrack.util.IOUtils;
 import newsrack.util.ParseUtils;
 import newsrack.util.PasswordService;
 import newsrack.web.EditProfileException;
 import newsrack.web.InvalidPasswordException;
 import newsrack.web.UnknownUserException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * class <code>User</code> encapsulates information about
  * an individual user -- personal details and the user's profile
  *
  * @author Subramanya Sastry
  * @version 1.0 12/05/04
  */
 public class User implements java.io.Serializable
 {
 // ############### STATIC FIELDS AND METHODS ############
    	// Logging output for this class
    private static Log _log = LogFactory.getLog(User.class);
 
 	// These are user ids that are not available and are used by
 	// NewsRack itself to provide administrative and library functionality
 	// NOTE: Even though only "admin" and "library" uids are used, the others
 	// are also reserved to prevent them from being used by others and cause
 	// confusion.
 	private static final String[] _reservedUids = { "admin", "Admin", "administrator", "Administrator", "library", "Library" };
 
    private static DB_Interface _db;
 	private static User        DUMMY_USER = new User();
 	private static Collection<Issue> EMPTY_COLL = new ArrayList<Issue>();
 
    public static void init(DB_Interface db)
    {
       _db = db;
    }
 
 	public static User getDummyUser()
 	{
 		return DUMMY_USER;
 	}
 
    public static User getUser(String uid)
    {
       return _db.getUser(uid);
    }
 
 	/**
 	 * This method returns an iterator with all registered users
 	 */
 	public static List<User> getAllUsers()
 	{
 		return _db.getAllUsers();
 	}
 
 	public static List<Issue> getAllValidatedIssues()
 	{
 		return _db.getAllValidatedIssues();
 	}
 
 	/**
 	 * This method tries to sign in a user with a specified password.
 	 *
 	 * @param  uname   User name that has been specified in the sign-in form
 	 * @param  passwd  Password that has been specified in the sign-in form
 	 * @return         Returns the user object for the user who has been signed in.
 	 * @throws UnknownUserException if the user name is unknown
 	 * @throws InvalidPasswordException if the specified password is incorrect
 	 */
 	public synchronized static User signInUser(final String uname, final String passwd) throws UnknownUserException, InvalidPasswordException
 	{
 		final User u = getUser(uname);
 		if (u == null)
 			throw new UnknownUserException(uname);
 		if (!u.passwordMatches(passwd))
 			throw new InvalidPasswordException(uname);
 
 		u._isAdmin = uname.equals("admin");
 		return u;
 	}
 	
 	/**
 	 * This method checks if the requested user id is available
 	 *
 	 * @param uid  uid which the user has requested
 	 * @return     Returns true if the user id is available, false otherwise.
 	 */
 	public static boolean userIdAvailable(final String uid)
 	{
 		return (getUser(uid) == null) && !(uid.equals("admin") || uid.equals("library"));
 	}
 
 	/**
 	 * This method register a user and allocates space in the underlying database for a new user
 	 * @param uid      Desired user id
 	 * @param password Unencrypted password
 	 * @param name     Real name of the user
 	 * @param emailid  Email id of the user -- required for communication 
 	 * @returns a user object if registration is successful
 	 *          null otherwise (for example, if the user id is not available)
 	 */
 	public synchronized static User registerUser(String uid, String password, String name, String emailId)
 	{
 		if (!userIdAvailable(uid))
 			return null;
 
 		User u = new User(uid, PasswordService.encrypt(password));
 		u.setName(name);
 		u.setEmail(emailId);
 		IOUtils.createDir(NewsRack.getBaseRssDir() + File.separator + u.getUid());
 		_db.registerUser(u);
 		return u;
 	}
 
 	public static List<PublicFile> getPublicFiles()
 	{
 		return _db.getAllPublicUserFiles();
 	}
 
 // ############### NON-STATIC FIELDS AND METHODS ############
    private Long    _key;         // Database key
 	private boolean _isAdmin;		// Is this the administrator??
 	private String  _name;			// Real name
 	private String  _uid;			// User id
 	private String  _password;		// Password (MD5-encrypted)
 	private String  _email;			// Email id
 	private boolean _isInitialized; // Has the profile been initialized
 	private boolean _isParsed;    // Has the profile been parsed
 
 	private Map<String, Issue> _issues;	// Map of issues defined by the user
 	private List<UserFile>    	_files;	// List of files defining the issues
 
 		// These transient fields are not stored in the db
 	private String  _workDir;		//	Work directory
 	private boolean _reclassificationInProgress = false;
 	private boolean _downloadInProgress         = false;
 	private boolean _validationInProgress       = false;
 	private boolean _concurrentProfileChange    = false;
 
 		// This is used temporarily during parsing
 	private List<NR_Collection> _userCollections = null;
 
 	/**
 	 * Dummy constructor
 	 */
 	public User() { }
 
 	private void init(final String uname, final String pwd, final boolean initialized)
 	{
       try {
          _uid      = uname;
          _password = pwd;
          _workDir  = (_db == null) ? "" : _db.getUserSpaceWorkDir(this);
          _isInitialized = initialized;
 		   _isAdmin = uname.equals("admin");
       }
       catch (Exception e) {
          _log.error("Caught exception while creating user object for " + uname, e);
          throw new RuntimeException(e);
       }
 	}
 
 	public User(final String uname, final String pwd)
 	{
 		init(uname, pwd, false);
 	}
 
 	public User(final String uname, final String pwd, boolean initialized)
 	{
 		init(uname, pwd, initialized);
 	}
 
 	public boolean equals(Object o) { return (o != null) && (o instanceof User) && _uid.equals(((User)o)._uid); }
 
 	public int hashCode() { return _uid.hashCode(); }
 
 	/**
 	 * This method checks if the user is an administrator!
 	 */
 	public boolean isAdmin()
 	{
 		return _isAdmin;
 	}
 
 	/**
 	 * Change the password for the user!
 	 */
 	public void changePassword(final String oldPwd, final String newPwd) throws InvalidPasswordException
 	{
 			// Match password
 		if (!passwordMatches(oldPwd))
 			throw new InvalidPasswordException(_uid);
 
 			// Set new password
 		_password = PasswordService.encrypt(newPwd);
 		_db.updateUser(this);
 	}
 
 	/**
 	 * Reset the password for the user!
 	 */
 	public void resetPassword(final String newPwd)
 	{
 			// Set new password
 		_password = PasswordService.encrypt(newPwd);
 		_db.updateUser(this);
 	}
 
 	/**
 	 * If an admin, sign in as another user!
 	 */
 	public synchronized User signInAsUser(final String uname) throws Exception
 	{
 		if (!isAdmin())
 			throw new Exception("No Administrative Privileges to sign in as user " + uname);
 
 		User u = getUser(uname);
 		if (u == null)
 			throw new UnknownUserException(uname);
 
 		u._isAdmin = false;
 
 		return u;
 	}
 
 	/**
 	 * This method spits out user information in XML-format
 	 */
 	public String toString()
 	{
 		final StringBuffer sb = new StringBuffer();
 		sb.append("\t<user name=\"" + _name + "\"\n");
 		sb.append("\t      uid=\"" + _uid + "\"\n");
 		sb.append("\t      password=\"" + _password + "\"\n");
 		sb.append("\t      email=\"" + _email + "\">\n");
 		final Iterator it = getFiles();
 		if (it.hasNext()) {
 			sb.append("\t\t<files>\n");
 			while (it.hasNext())
 				sb.append("\t\t\t<file name=\"" + it.next() + "\" />\n");
 			sb.append("\t\t</files>\n");
 
 			if (isValidated()) {
 				sb.append("\t\t<profile-validated val=\"true\" />\n");
 
 					// IMPORTANT: Spit out list of frozen issues ONLY after the
 					// profile-validated info.
 				sb.append("\t\t<frozen-issues>\n");
 				for (final Issue i: getIssues()) {
 					if (i.isFrozen()) {
 						sb.append("\t\t\t<issue name=\"");
 						sb.append(i.getName());
 						sb.append("\" />\n");
 					}
 				}
 				sb.append("\t\t</frozen-issues>\n");
 			}
 		}
 
 		sb.append("\t</user>\n");
 		return sb.toString();
 	}
 
 	public boolean hasFile(final String f)
 	{
 		if (_files == null)
 			loadUserFilesFromDB();
 
 		for (UserFile uf: _files)
 			if (uf.getName().equals(f))
 				return true;
 
 		return false;
 	}
 
 	private void removeFile(final String f)
 	{
 		if (_files == null)
 			loadUserFilesFromDB();
 
 		for (UserFile uf: _files) {
 			if (uf.getName().equals(f)) {
				_files.remove(f);
 				_db.deleteFile(uf);
 			}
 		}
 	}
 
 	public List<UserFile> getFileList()
 	{
 		if (_files == null)
 			loadUserFilesFromDB();
 
 		return _files;
 	}
 
 	public Iterator getFiles() { return getFileList().iterator(); }
 
 	public boolean isValidated() { return _isInitialized; }
 
 	public boolean concurrentProfileModification()
 	{
 		return _concurrentProfileChange;
 	}
 
    /** Set the database key for this user */
    public void setKey(Long k) { _key = k; }
 
 	/**
 	 * Sets the name of the user
 	 * @param name  User's name
 	 */
 	public void setName(final String name) { _name = name; }
 
 	/**
 	 * Sets the uid of the user
 	 * @param uid   User id of the user
 	 */
 	public void setUid(final String uid) { _uid = uid; }
 
 	/**
 	 * Sets the password of the user
 	 * @param password   User's password
 	 */
 	public void setPassword(final String password) { _password = password; }
 
 	/**
 	 * Sets the email of the user
 	 * @param email   User's email
 	 */
 	public void setEmail(final String email) { _email = email; }
 
 	/** Gets the user's unique key */
    public Long getKey() { return _key; }
 
 	/** Gets the user name */
 	public String getName() { return _name; }
 
 	/** Gets the user's id */
 	public String getUid() { return _uid; }
 
 	/** Gets the user's encryped password */
 	public String getPassword() { return _password; }
 
 	/** Gets the user's email id */
 	public String getEmail() { return _email; }
 
 	private void initProfileFromDatabase(final Object o)
 	{
 		if (o.equals("true"))
 			try {
 				_db.initProfile(this);
 			}
 			catch (final Exception e) {
 				_log.error("Unexpected error initializing profile for user " + _uid + ".  Exception: " + e.toString());
 			}
 	}
 
 	public void setFrozenIssue(final String name)
 	{
 		try {
 			getIssue(name).freeze();
 		}
 		catch (final Exception e) {
 			_log.error("Error freezing issue " + name + " for user " + _uid + ".  Exception: " + e.toString());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Gets the active profile
 	 */
 	public String getLastDownloadTime_String()
 	{ 
 		return NewsRack.DF.format(getLastDownloadTime());
 	}
 
 	public Date getLastDownloadTime()
 	{ 
       return newsrack.archiver.DownloadNewsTask.getLastDownloadTime();
 	}
 
 	public String getWorkDir() { return _workDir; }
 
 	private void invalidate()
    {
 		for (Issue i: getIssues())
 			i.invalidate();
 
 		_db.invalidateUserProfile(this);
 		_issues = null;
 
 			// Reset flags
 		_isInitialized = false;
       _isParsed = false;
 		_reclassificationInProgress = false;
 		_downloadInProgress = false;
 	}
 
 	private void initIssueMap()
 	{
 		_issues = new TreeMap<String, Issue>();
 	}
 
 	private void loadIssuesFromDB()
 	{
 		initIssueMap();
 		for (Issue i: _db.getIssues(this)) {
 			if (_log.isDebugEnabled()) _log.debug("LOAD: Adding issue " + i.getName() + " to the hash table!");
 			_issues.put(i.getName().toLowerCase(), i);
 		}
 	}
 
 	private void loadUserFilesFromDB()
 	{
 		_files = _db.getFiles(this);
 	}
 
 	/**
 	 * Add a issue to the profile -- only during parsing!
 	 */
 	public void addIssue(final Issue i) throws Exception
 	{
 			// Issues are added during parsing
 		if (_issues == null)
 			initIssueMap();
 
 			// Normalize case
 		if (_issues.get(i.getName().toLowerCase()) != null) {
 			throw new Exception("ERROR! An issue already exists with name " + i.getName());
 		}
 		else {
 			_issues.put(i.getName().toLowerCase(), i);
 		}
 	}
 
 	/**
 	 * Gets all the issues defined in this profile
 	 */
 	public Collection<Issue> getIssues()
 	{
 		if (!isValidated())
 			return EMPTY_COLL;
 
 		if (_issues == null)
 			loadIssuesFromDB();
 
 		return _issues.values();
 	}
 
 	public Collection<Source> getSources()
 	{
 		return _db.getMonitoredSourcesForAllTopics(this);
 	}
 
 	/**
 	 * Fetches the issue object given an issue name
 	 * @param iname  Name of the issue
 	 */
 	public Issue getIssue(String iname)
 	{
 		/**
 		if (_issues == null)
 			loadIssuesFromDB();
 
 		return _issues.get(iname);
 		**/
 		return (_issues == null) ? _db.getIssue(this, iname) : _issues.get(iname.toLowerCase());
 	}
 
 	public void invalidateProfile()
 	{
 		invalidateAllIssues();
 
 			// Clear out all my import dependencies so that when my importers validate their profile,
 			// I won't get validated again!
 		_db.clearImportDependenciesForUser(this);
 	}
 
 	/**
 	 * Disables the active profile
 	 */
 	public void invalidateAllIssues()
 	{
 			// If it is not a validated profile, simply return
 		if (!isValidated())
 			return;
 
 		_validationInProgress = false;
 
 		if (_log.isDebugEnabled())
 			_log.debug("Invalidating all issues for user: " + getUid());
 
 			// Invalidate the profile now 
 		invalidate();
 
 			// Update status now
 		_db.updateUser(this);
 
 			// Get the list of keys of users who import my collections
 			// and invalidate all of them
 		List<Long> dependentUsers = _db.getCollectionImportersForUser(this);
 		if (dependentUsers != null) {
 			for (Long uKey: dependentUsers) {
 				User u = _db.getUser(uKey);
 				if (u.isValidated()) {
 					_log.info("Will invalidate dependent user: " + u.getUid());
 					try {
 						u.invalidateAllIssues();
 					} 
 					catch(Exception e) {
 						_log.error("Exception invalidating user: " + u.getUid(), e);
 					}
 				}
 				else {
 					_log.info("Ignoring invalidated dependent user: " + u.getUid());
 				}
 			}
 		}
 		else {
 			_log.info("Found no dependent users!");
 		}
 	}
 
 	/**
 	 * This method uploads a file to the user's account
 	 * Throws an EditProfileException if the addition fails for some reason
 	 * @param fis the file
 	 * @param f   the file
 	 */
 	public void uploadFile(final InputStream fis, final String f) throws EditProfileException
 	{
 		if (hasFile(f)) {
 			throw new EditProfileException("A file exists with name <b>" + f + "</b>.  Cannot overwrite existing file.  You have to delete the existing file before you can add the new file with the same name.");
 		}
 		else {
 			try {
 					// IMPT: do the db call before adding to _files in case the upload fails
 				UserFile uf = new UserFile(this, f);
 				_db.uploadFile(uf, fis);
 				_files.add(uf);
 			}
 			catch (Exception e) {
 				throw new EditProfileException(e.toString());
 			}
 		}
 	}
 
 	public Source getSourceByTag(String tag)
 	{
 		return _db.getSource(this, tag);
 	}
 
 	public String getFileUploadArea()
 	{
 		return _db.getFileUploadArea(this);
 	}
 
 	public String getRelativeFilePath(String fileName)
 	{
 		return _db.getRelativeFilePath(this, fileName);
 	}
 
 	public OutputStream getOutputStream(String fileName) throws java.io.IOException
 	{
 		return _db.getOutputStream(this, fileName);
 	}
 
 	public InputStream getInputStream(String fileName) throws java.io.IOException
 	{
 		return _db.getInputStream(this, fileName);
 	}
 
 	public InputStream getInputStream(String owner, String fileName) throws java.io.IOException
 	{
 		return _db.getInputStream(this, owner, fileName);
 	}
 
 	public Reader getFileReader(String fileName) throws java.io.IOException
 	{
 		return _db.getFileReader(this, fileName);
 	}
 
 	public Reader getFileReader(String owner, String fileName) throws java.io.IOException
 	{
 		return _db.getFileReader(this, owner, fileName);
 	}
 
 	/**
 	 * This method adds a file to the user's account
 	 * Throws an EditProfileException if the addition fails for some reason
 	 * @param f   the file
 	 */
 	public void addFile(final String f) throws EditProfileException
 	{
 		if (hasFile(f)) {
 			throw new EditProfileException("A file exists with name <b>" + f + "</b>.  Cannot overwrite existing file.  You have to delete the existing file before you can add the new file with the same name.");
 		}
 		else {
 			try {
 					// IMPT: do the db call before adding to _files in case the add fails
 				UserFile uf = new UserFile(this, f);
 				_db.addFile(uf);
 				_files.add(uf);
 			}
 			catch (Exception e) {
 				throw new EditProfileException(e.toString());
 			}
 		}
 	}
 
 	public void renameFile(final String oldName, final String newName) throws EditProfileException
 	{
 			/* Check for bad file names first */
 		if (oldName.indexOf(File.separator) != -1)
 			throw new EditProfileException("Invalid source file name: " + oldName);
 		else if (newName.indexOf(File.separator) != -1)
 			throw new EditProfileException("No " + File.separator + " allowed in file name! Invalid file name: " + newName);
 
 			/* Check if renaming will fail next */
 		if (!hasFile(oldName))
 			throw new EditProfileException("File " + oldName + " does not exist");
 		if (hasFile(newName))
 			throw new EditProfileException("A file with name " + newName + " already exists!  Please pick a different name!");
 
 			/* Attempt to rename the file on the file system next */
 		final String rpath = _db.getFileUploadArea(this);
 		final File   f1    = new File(rpath + File.separator + oldName);
 		final File   f2    = new File(rpath + File.separator + newName);
 		if (!f1.renameTo(f2))
 			throw new EditProfileException("Renaming to " + newName + " failed!");
 
 			/* Now, rename the file in the user's listing */
 		for (UserFile uf: _files) {
 			if (uf.getName().equals(oldName)) {
 				uf.renameFile(newName);
 				_db.renameFile(uf, newName);
 				// Sort order has changed -- resort
 				java.util.Collections.sort(_files, new java.util.Comparator() {
 																	public int compare(Object o1, Object o2) { 
 																		return ((UserFile)o1).getName().compareTo(((UserFile)o2).getName()); 
 																	} 
 															  }
 												  );
 				break;
 			}
 		}
 	}
 
 	/**
 	 * This method deletes a file from the user's profile-related files
 	 * @param name  the name of the file to be deleted 
 	 * @throws EditProfileException if the file type is not one of the above
 	 */
 	public void deleteFile(final String name) throws EditProfileException
 	{
 		if (!hasFile(name))
 			throw new EditProfileException("File " + name + " does not exist");
 
 			/* Remove from list of user files */
 		removeFile(name);
 
 			/* Always invalidate the active profile whenever some file 
 			 * is deleted -- I know this is somewhat silly and I can do
 			 * smarter things .. but, this is good enough for now! */
 		invalidateProfile();
 	}
 
 	public void addCollection(NR_Collection c)
 	{
 		if (_log.isDebugEnabled()) _log.debug("Adding collection: " + c);
 		_userCollections.add(c);
 	}
 
 	public void parseProfileFiles() throws Exception
 	{
 		if (_isParsed) 
 			return;
 
 		_userCollections = new ArrayList<NR_Collection>();
 
 		if (_isInitialized)
 			_concurrentProfileChange = true;
 
       _isInitialized = false;
 		(new NRLanguageParser()).parseFiles(this);
 
 		if (ParseUtils.encounteredParseErrors(this)) {
 			throw new Exception("Parsing errors");
 		}
 		else {
 				// If we got through the parse without errors,
 				// commit all parse data structures to the database!
 
 			if (_log.isInfoEnabled()) _log.info("Successfully parsed without errors for user " + getUid());
 
 				// Commit concept collections first
 			for (NR_Collection c: _userCollections) {
 				if (c.getType() == NR_CollectionType.CONCEPT)
 					_db.addProfileCollection(c);
 			}
 
 				// ... then the rest ... because categories in the category collections might reference uncommitted concepts otherwise ...
 			for (NR_Collection c: _userCollections) {
 				if (c.getType() != NR_CollectionType.CONCEPT)
 					_db.addProfileCollection(c);
 			}
 
 			_isParsed = true;
 		}
 
 		_userCollections = null;	// free up space for being GC
 	}
 
 	private void initializeIssues(final boolean genScanners) throws Exception
 	{
 		if (_log.isDebugEnabled())
 			_log.debug("Validating all issues for user: " + getUid());
 
 			// Parse files and initialize the user object
 		parseProfileFiles();
 
 			// Initialize the issue objects
 			// NOTE: Cannot call getIssues() because the user is not validated yet ...
 		if (_issues != null) {
 			for (final Issue i: _issues.values()) {
 				_log.info("Starting initialization of issue " + i.getName());
 
 					// Initializes the issue
 				i.initialize();
 
 					// Generate scanners for future downloading & classifying
 				if (genScanners) {
 					i.gen_JFLEX_RegExps();
 					i.compileScanners(_workDir);
 					_log.info("Done generating scanners for " + i.getName() + " --");
 				}
 
 					// Add to the database!
 				_db.addIssue(i);
 				_log.info("Starting initialization of issue " + i.getName());
 			}
 		}
 	}
 
 	/**
 	 * This method validates the user's profile and builds issues 
 	 * defined in the user's files.  Any errors in the user's profile
 	 * are reported.  News cannot be downloaded until issues are validated!
 	 */
 	public void validateAllIssues(final boolean genScanners) throws Exception
 	{
 			/* This happens when the server is overloaded ... reading of news index files
 			 * and building of news objects might be taking a long time ... browser/user
 			 * can't wait any longer and resubmits the request! */
 		if (_validationInProgress)
 			throw new Exception("Validation in progress ... check again in a little while ... the server is probably overloaded!");
 
 		try {
 			_validationInProgress = true;
 
 				// IMPORTANT: First, disable all active issues, if any!
 			invalidateAllIssues();
 
 				// Clear out all my import dependencies so that I can incorporate more up-to-date
 				// import dependencies after parsing the profile files
 			_db.clearImportDependenciesForUser(this);
 
 				// Now, re-initialize all issues
 		   _isInitialized = false;
 			initializeIssues(genScanners);
 		   _isInitialized = true;
 
 				// Update the db before validating dependent users! 
 			_db.updateUser(this);
 
 				// Get the list of keys of users who import my collections and validate all of them.
 				// The code below implicitly implements a topological order of validations.
 			List<Long> dependentUsers = _db.getCollectionImportersForUser(this);
 			if (dependentUsers != null) {
 				for (Long uKey: dependentUsers) {
 					User u = _db.getUser(uKey);
 					if (!u.isValidated()) {
 						_log.info("Attempting validation of dependent user: " + u.getUid());
 							// Check if all the users who provide me with collections are validated!
 						boolean allDone = true;
 						List<Long> providingUsers = _db.getCollectionExportersForUser(u);
 						for (Long pKey: providingUsers) {
 							User pu = _db.getUser(pKey);
 							if (!pu.isValidated()) {
 								_log.info("... Not yet ready because one of the providers " + pu.getUid() + " is yet to be validated!");
 								allDone = false;
 								break;
 							}
 						}
 
 							// If all my providers have been validated, validate me!
 						if (allDone) {
 							try {
 								_log.info("Validating dependent user: " + u.getUid());
 								u.validateAllIssues(genScanners);
 							} 
 							catch(Exception e) {
 								_log.error("Exception validating user: " + u.getUid(), e);
 							}
 						}
 					}
 					else {
 						_log.info("Ignoring validated dependent user: " + u.getUid());
 					}
 				}
 			}
 			else {
 				_log.info("Found no dependent users!");
 			}
 		}
 		catch (final Exception e) {
 			_log.error("Got exception while validating issues!", e);
 				// Free up space!
 			if (!isValidated())
 				invalidate();
 			throw e;
 		}
 		finally { 
 			_validationInProgress = false;
 			_db.updateUser(this);
 		}
 	}
 
 	/**
 	 * This method verifies if the specified passwords the user's password
 	 * @param p  The password to be checked
 	 */
 	public boolean passwordMatches(final String p)
 	{
 		final String encPassword = PasswordService.encrypt(p);
 		return encPassword.equals(_password);
 	}
 
 	/**
 	 * Check if a different user can access a file in this user's space
 	 * @param u  User trying to access the file
 	 * @param f  The file being accessed
 	 */
 	public boolean fileAccessible(final User u, final String f)
 	{
 		return true;
 	}
 
 	public boolean canAccessFile(final User u, final String f)
 	{
 		return u.fileAccessible(this, f);
 	}
 
 	public boolean canAccessFile(final String uid, final String f)
 	{
 		return canAccessFile(getUser(uid), f);
 	}
 
 	/**
 	 * Downloads latest news and classifies them based on rules specified in
 	 * the user's active profile.
 	 */
 	public void downloadNews() throws Exception
 	{
 		if (!isValidated())
 			throw new Exception("No issues defined yet!  Cannot download news!");
 
 		try {
 			doPreDownloadBookkeeping();
 				// Download all news
 			for (final Issue i: getIssues()) {
 					// Catch profile changes in the middle!!
 				if (concurrentProfileModification())
 					break;
 
 				Collection news = i.downloadNews();
 				i.readInCurrentRSSFeed();
 				i.scanAndClassifyNewsItems(null, news);
 				i.updateRSSFeed();
 				i.storeNewsToArchive();
 				i.freeRSSFeed();
             i.unloadScanners();
 			}
 		}
 		catch (final Exception e) {
 			throw e;
 		}
 		finally {
 			doPostDownloadBookkeeping();
 		}
 	}
 
 	public void doPreDownloadBookkeeping() throws Exception
 	{
 		if (_downloadInProgress)
 			throw new Exception("Download in Progress. Duplicate Request!");
 
 		_concurrentProfileChange = false;
 		_downloadInProgress = true;
 	}
 
 	public void doPostDownloadBookkeeping()
 	{
 		_downloadInProgress = false;
 	}
 
 	/**
 	 * Reclassify news by reading in news from the entire
 	 * archive since the beginning of time!!
 	 *
 	 * @param iname      Issue to reclassify
 	 * @param srcs       Array of sources to classify from
 	 * @param allSrcs    Should all sources be used?
 	 * @param sd         Start date from which news should be classified
 	 * @param ed         End date from which news should be classified
 	 * @param allDates   If true, all news items from the archive will
 	 *                   be classified.  The sdate and edate parameter values
 	 *                   will be ignored
 	 * @param resetCats  If true, the categories will be reset and
 	 *                   all existing news articles will be removed.
 	 *                   If false, existing news articles will be retained.
 	 *
 	 * FIXME: This method should actually take a range of
 	 * dates, a set of dates, or some other input like that.
 	 */
 	public void reclassifyNews(final String iname, final String[] srcs, final boolean allSrcs, final Date sd, final Date ed, final boolean allDates, final boolean resetCats) throws Exception
 	{
 		if (allDates) {
 			final String s = "Sorry! Turned off reclassification for the entire archive!" +
 			           "Please select a subset of news sources and/or a subset of dates (most recent)!" +
 						  "If you really want to query the entire archive for all sources, call this method " +
 						  "one month at a time!  Thanks for your understanding while this feature is fixed!";
 			throw new Exception(s);
 		}
 
 		if (_reclassificationInProgress)
 			throw new Exception("Reclassification in Progress. Duplicate Request!");
 
 		try {
 			_reclassificationInProgress = true;
 			_concurrentProfileChange = false;
 			final Issue issue = getIssue(iname);
 
 				// Clear existing news
 			if (resetCats)
 				issue.clearNews();
 
 				// Initialize issue
 			issue.readInCurrentRSSFeed();
 
 				/* For each source used, fetch a list of all existing index files
 				 * and for each index, classify news for that index */
 			if (_log.isInfoEnabled()) _log.info("Request to reclassify news");
 			if (allSrcs) {
 				int count = 0;
 				for (Source s: issue.getMonitoredSources()) {
 					if (concurrentProfileModification())
 						break;
 					issue.reclassifyNews(s, allDates, sd, ed);
 					count++;
 				}
 			} 
 			else {
 				for (String element : srcs) {
 					if (concurrentProfileModification())
 						break;
 					// I am getting source keys, not tags anymore
 					//issue.reclassifyNews(getSourceById(srcs[i]), allDates, sd, ed);
 					issue.reclassifyNews(_db.getSource(Long.parseLong(element)), allDates, sd, ed);
 				}
 			}
 
 				// Clean up after classification is done
 			issue.freeRSSFeed();
          issue.unloadScanners();
 		}
 		catch (final Exception e) {
 			throw e;
 		}
 		finally {
 			_reclassificationInProgress = false;
 		}
 	}
 }
