 package de.skuzzle.polly.core.internal.users;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 
 import org.apache.log4j.Logger;
 
 import de.skuzzle.polly.core.internal.persistence.PersistenceManagerV2Impl;
 import de.skuzzle.polly.core.parser.InputParser;
 import de.skuzzle.polly.core.parser.InputScanner;
 import de.skuzzle.polly.core.parser.ast.declarations.Declaration;
 import de.skuzzle.polly.core.parser.ast.declarations.DeclarationReader;
 import de.skuzzle.polly.core.parser.ast.declarations.Namespace;
 import de.skuzzle.polly.core.parser.ast.expressions.Expression;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.Literal;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.parser.ast.visitor.ExecutionVisitor;
 import de.skuzzle.polly.core.parser.ast.visitor.ParentSetter;
 import de.skuzzle.polly.core.parser.ast.visitor.resolving.TypeResolver;
 import de.skuzzle.polly.core.parser.problems.ProblemReporter;
 import de.skuzzle.polly.core.parser.problems.SimpleProblemReporter;
 import de.skuzzle.polly.core.util.CaseInsensitiveStringKeyMap;
 import de.skuzzle.polly.core.util.TypeMapper;
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.Attribute;
 import de.skuzzle.polly.sdk.FormatManager;
 import de.skuzzle.polly.sdk.PersistenceManagerV2.Atomic;
 import de.skuzzle.polly.sdk.PersistenceManagerV2.Param;
 import de.skuzzle.polly.sdk.PersistenceManagerV2.Write;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.PersistenceManagerV2.Read;
 import de.skuzzle.polly.sdk.constraints.AttributeConstraint;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.UserEvent;
 import de.skuzzle.polly.sdk.eventlistener.UserListener;
 import de.skuzzle.polly.sdk.exceptions.AlreadySignedOnException;
 import de.skuzzle.polly.sdk.exceptions.ConstraintException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.InvalidUserNameException;
 import de.skuzzle.polly.sdk.exceptions.RoleException;
 import de.skuzzle.polly.sdk.exceptions.UnknownAttributeException;
 import de.skuzzle.polly.sdk.exceptions.UnknownUserException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 import de.skuzzle.polly.sdk.roles.RoleManager;
 import de.skuzzle.polly.sdk.time.Time;
 import de.skuzzle.polly.tools.events.Dispatchable;
 import de.skuzzle.polly.tools.events.EventProvider;
 import de.skuzzle.polly.tools.events.Listeners;
 
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 ae73250
  */
 public class UserManagerImpl extends AbstractDisposable implements UserManager {
 
     private static Logger logger = Logger.getLogger(UserManagerImpl.class.getName());
     
     private final static AttributeConstraint NO_CONSTRAINT = new AttributeConstraint() {
         @Override
         public boolean accept(Types value) {
             return true;
         }
     };
     
     
     private final static FileFilter DECLARATION_FILTER = new FileFilter() {
         
         @Override
         public boolean accept(File pathname) {
             return pathname.getName().toLowerCase().endsWith(".decl");
         }
     };
     
     
     private PersistenceManagerV2Impl persistence;
 
     /**
      * Stores the currently signed on users. Key: the nickname in lower case.
      */
     private Map<String, User> onlineCache;
     private File declarationCachePath;
     private Map<String, AttributeConstraint> constraints;
     private EventProvider eventProvider;
     private User admin;
     private boolean registeredStale;
     private List<User> registeredUsers;
     private boolean attributesStale;
     private List<AttributeImpl> allAttributes;
     private RoleManager roleManager;
     private final FormatManager formatter;
     
     
     
     public UserManagerImpl(PersistenceManagerV2Impl persistence, 
             String declarationCachePath, int tempVarLifeTime,
             boolean ignoreUnknownIdentifiers, EventProvider eventProvider, 
             RoleManager roleManager,
             FormatManager formatter) {
         this.formatter = formatter;
         this.eventProvider = eventProvider;
         this.persistence = persistence;
         this.roleManager = roleManager;
         this.onlineCache = Collections.synchronizedMap(
                 new CaseInsensitiveStringKeyMap<User>());
         this.declarationCachePath = new File(declarationCachePath);
         this.constraints = new HashMap<String, AttributeConstraint>();
         try {
             logger.info("Reading declarations...");
             if (!this.declarationCachePath.exists()) {
                 logger.warn("Declaration-cache directory does not exist. " +
                 		"Trying to create folder structure");
                 this.declarationCachePath.mkdirs();
             }
             Namespace.setDeclarationFolder(this.declarationCachePath);
 			readDeclarations(this.declarationCachePath);
 			logger.trace("done");
 		} catch (IOException e) {
 			logger.warn("No declarations restored", e);
 		}
     }
     
     
     
     private static void readDeclarations(File folder) throws IOException {
         for (final File file : folder.listFiles(DECLARATION_FILTER)) {
             DeclarationReader dr = null;
             try {
                 final String nsName = file.getName().substring(
                         0, file.getName().length() - 5);
                 final Namespace ns = Namespace.forName(nsName);
                 dr = new DeclarationReader(file, "ISO-8859-1", ns);
                 dr.readAll();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (dr != null) {
                     dr.close();
                 }
             }
         }
     }
     
     
 
     @Override
     public synchronized Set<String> getDeclaredIdentifiers(String namespace) {
         final Namespace ns = Namespace.forName(namespace);
 
         final Set<String> result = new HashSet<String>();
         for (final List<Declaration> decls : ns.getDeclarations().values()) {
             for (final Declaration decl : decls  ) {
                 result.add(decl.getName().getId());
             }
         }
         return result;
     }
     
     
     
     public void setAdmin(User admin) {
         this.admin = admin;
     }
     
     
     
     public User getAdmin() {
         return this.admin;
     }
     
     
     
     @Override
     public User getUser(IrcUser user) {
         return this.onlineCache.get(user.getNickName());
     }
     
     
     
     @Override
     public User getUser(int id) {
         UserImpl result = 
             (UserImpl) this.persistence.atomic().find(UserImpl.class, id);
         
         if (result != null) {
             result.setUserManager(this);
         }
         return result;
     }
 
     
     
     @Override
     public User getUser(String registeredName) {
         if (registeredName == null) {
             return null;
         }
         try (final Read r = this.persistence.read()) {
             final UserImpl result = r.findSingle(UserImpl.class,
                     "USER_BY_NAME", new Param(registeredName));
             
                 if (result != null) {
                     result.setUserManager(this);
                 }
                 return result;
         }
     }
     
     
 
     @Override
     public User updateUser(final User old, final User updated) {
         try {
             this.persistence.writeAtomic(new Atomic() {
                 @Override
                 public void perform(Write write) throws DatabaseException {
                     old.setHashedPassword(updated.getHashedPassword());
                 }
             });
         } catch (DatabaseException e) {
             e.printStackTrace();
         }
         logger.info("User '" + old + "' updated to '" + updated + "'");
         return old;
     }
     
     
     
     public void addUser(User user) 
                 throws UserExistsException, DatabaseException, InvalidUserNameException {
         
         Matcher m = USER_NAME_PATTERN.matcher(user.getName());
         if (!m.matches()) {
             throw new InvalidUserNameException(user.getName());
         }
         
         
         try (final Write w = this.persistence.write()) {
             User check = w.read().findSingle(User.class, "USER_BY_NAME", 
                     new Param(user.getName()));
             
             if (check != null) {
                 logger.trace("User already exists.");
                 throw new UserExistsException(check);
             }
             w.single(user);
             this.registeredStale = true;
         }
         // Assign registered role to new user.
         try {
             this.roleManager.assignRole(user, RoleManager.DEFAULT_ROLE);
         } catch (RoleException ignore) {
             logger.warn("Ignoring RoleException", ignore);
         }
         
         logger.info("Added user " + user);
     }
     
     
 
     @Override
     public User addUser(String name, String password) 
             throws UserExistsException, DatabaseException, InvalidUserNameException {
         User newUser = this.createUser(name, password);
         this.addUser(newUser);
         return newUser;
     }
 
     
     
     @Override
     public void deleteUser(final User user) throws DatabaseException {
         this.persistence.writeAtomic(new Atomic() {
             
             @Override
             public void perform(Write write) throws DatabaseException {
                 logoff(user);
                 write.remove(user);
             }
         });
         this.registeredStale = true;
         logger.info("Deleted user " + user);
     }
 
     
     
     @Override
     public synchronized User logon(String from, String registeredName, String password) 
             throws UnknownUserException, AlreadySignedOnException {
         logger.info("Trying to log on user '" + registeredName + "'.");
         User user = this.getUser(registeredName);
         if (user == null) {
             throw new UnknownUserException(registeredName);
         }
         
         this.checkAlreadySignedOn(user);
         
         if (user.checkPassword(password)) {
             user.setCurrentNickName(from);
             this.onlineCache.put(user.getCurrentNickName(), user);
             logger.info("Irc User " + from + " successfully logged in as " + 
                     registeredName);
             
             ((UserImpl) user).setLoginTime(Time.currentTimeMillis());
             UserEvent e = new UserEvent(this, user);
             this.fireUserSignedOn(e);
             return user;
         }
         
         logger.warn("Login from '" + from + "' with user name '" + registeredName + 
                 "' failed: Invalid password.");
         return null;
     }
     
     
     
     public synchronized User logonWithoutPassword(String from) throws 
                 AlreadySignedOnException, UnknownUserException {
         logger.info("Trying to autologon user '" + from + "'.");
         User user = this.getUser(from);
         if (user == null) {
             throw new UnknownUserException(from);
         }
         
         this.checkAlreadySignedOn(user);
         
         user.setCurrentNickName(from);
         this.onlineCache.put(user.getCurrentNickName(), user);
         logger.info("Irc User " + from + " successfully logged in as " + 
                 from);
         
         ((UserImpl) user).setLoginTime(Time.currentTimeMillis());
         UserEvent e = new UserEvent(this, user);
         this.fireUserSignedOn(e);
         return user;
     }
     
     
     
     private void checkAlreadySignedOn(User user) throws AlreadySignedOnException {        
         if (this.onlineCache.containsKey(user.getCurrentNickName())) {
             throw new AlreadySignedOnException(user.getName());
         }
     }
 
     
     
     @Override
     public void logoff(User user) {
         logger.info("User " + user + " logged off.");
         UserEvent e = new UserEvent(this, user);
         this.onlineCache.remove(user.getCurrentNickName());
         
         this.fireUserSignedOff(e);
     }
     
     
     
     @Override
     public void logoff(IrcUser user) {
         this.logoff(user, false);
     }
     
     
     
     public synchronized void logoff(IrcUser user, boolean auto) {
         logger.info("User " + user + " logged off.");
         UserEvent e = new UserEvent(this, this.getUser(user), auto);
         this.onlineCache.remove(user.getNickName());
 
         this.fireUserSignedOff(e);
     }
     
     
     
     public synchronized void logoffAll() {
         //  HACK: copy users to not get a concurrent modification exception
         Collection<User> online = new ArrayList<User>(this.onlineCache.values());
         
         for (User user : online) {
             IrcUser tmp = new IrcUser(user.getCurrentNickName(), "", "");
             this.logoff(tmp, true);
         }
     }
     
 
 
     @Override
     public boolean isSignedOn(IrcUser user) {
         return this.onlineCache.containsKey(user.getNickName());
     }
     
     
     
     @Override
     public boolean isSignedOn(User user) {
         return this.onlineCache.containsKey(user.getCurrentNickName());
     }
     
     
     
     @Override
     public synchronized List<User> getRegisteredUsers() {
         if (this.registeredStale || this.registeredUsers == null) {
             final List<de.skuzzle.polly.core.internal.users.UserImpl> all = 
                 this.persistence.atomic().findList(UserImpl.class, UserImpl.ALL_USERS);
             
             for (final de.skuzzle.polly.core.internal.users.UserImpl u : all) {
                 u.setUserManager(this);
             }
             this.registeredUsers = new ArrayList<User>(all);
             this.registeredStale = false;
         }
         return this.registeredUsers;
     }
     
     
     
     @Override
     public Collection<User> getOnlineUsers() {
         return Collections.unmodifiableCollection(this.onlineCache.values());
     }
     
     
     
     public synchronized void traceNickChange(IrcUser oldUser, IrcUser newUser) {
         logger.debug("Tracing nickchange from '" + oldUser + "' to '" + newUser + "'");
         User tmp = this.onlineCache.get(oldUser.getNickName());
         tmp.setCurrentNickName(newUser.getNickName());
         this.onlineCache.remove(oldUser.getNickName());
         this.onlineCache.put(newUser.getNickName(), tmp);
     }
     
     
     
     @Override
     public synchronized Map<String, List<Attribute>> getAllAttributes() {
         if (this.attributesStale || this.allAttributes == null) {
             this.allAttributes = this.persistence.atomic().findList(AttributeImpl.class, 
                 AttributeImpl.ALL_ATTRIBUTES);
         }
         final Map<String, List<Attribute>> result = 
             new HashMap<>(this.allAttributes.size());
         for (final Attribute attr : this.allAttributes) {
             List<Attribute> lst = result.get(attr.getCategory());
             if (lst == null) {
                 lst = new ArrayList<>();
                 result.put(attr.getCategory(), lst);
             }
             lst.add(attr);
         }
         
         return result;
     }
     
     
     
     @Override
     public void addAttribute(final String name, final Types defaultValue, 
             final String description, final String category, 
             final AttributeConstraint constraint) 
                 throws DatabaseException {
         
         this.persistence.writeAtomic(new Atomic() {
             @Override
             public void perform(Write write) throws DatabaseException {
                 
                 constraints.put(name.toLowerCase(), constraint);
                 final AttributeImpl check = write.read().findSingle(AttributeImpl.class, 
                         AttributeImpl.ATTRIBUTE_BY_NAME, new Param(name));
                 
                 if (check != null) {
                     logger.trace("Tried to add an attribute that already existed: " + name + 
                             ". Existing attribute: " + check);
                     return;
                 }
                 
                 final String sDefaultValue = defaultValue.valueString(
                     getPersistenceFormatter());
                 
                 final AttributeImpl att = new AttributeImpl(name, sDefaultValue, 
                     description, category);
                 
                 final List<User> all = getRegisteredUsers();
                 write.single(att);
                 logger.trace("Adding new attribute to each user.");
                 for (User user : all) {
                     final UserImpl u = (UserImpl) user;
                     u.getAttributes().put(name, sDefaultValue);
                 }
                 logger.info("Attribute " + att + " added.");
             }
         });
         this.attributesStale = true;
     }
     
     
     
     @Override
    public String setAttributeFor(User executor, final User user, final String attribute, 
            String value) throws DatabaseException, ConstraintException {
         logger.trace("Trying to set attribute '" + attribute + "' to value '" + 
             value + "'");
         
         // check if attribute exists:
         user.getAttribute(attribute);
         
         if (value.equalsIgnoreCase("%default%")) {
             Attribute attr = this.persistence.atomic().findSingle(Attribute.class, 
                 AttributeImpl.ATTRIBUTE_BY_NAME, new Param(attribute));
             
             value = attr.getDefaultValue();
         }
         
         final Types valueCopy = this.parseValue(executor, value);
         final AttributeConstraint constraint = this.constraints.get(
             attribute.toLowerCase());
         
         if (!constraint.accept(valueCopy)) {
             throw new ConstraintException("'" + value + 
                 "' ist kein gltiger Wert fr das Attribut '" + attribute + "'");
         }
         
         this.persistence.writeAtomic(new Atomic() {
             @Override
             public void perform(Write write) {
                 ((UserImpl) user).setAttribute(attribute, valueCopy);
             }
         });
         return valueCopy.valueString(this.getPersistenceFormatter());
     }
 
     
     
     public FormatManager getPersistenceFormatter() {
         return this.formatter;
     }
     
     
     
     Types parseValue(User executor, String value) {
         if (executor == null) {
             // use admin as namespace if no executor is specified.
             executor = this.admin;
         }
         
         final ProblemReporter reporter = new SimpleProblemReporter();
         final InputScanner is = new InputScanner(value);
         final InputParser ip = new InputParser(is, reporter);
         is.setSkipWhiteSpaces(true);
         
         try {
             final Expression exp = ip.parseSingleExpression();
             exp.visit(new ParentSetter());
             
             final String nsName = executor.getCurrentNickName() == null 
                 ? executor.getName() 
                 : executor.getCurrentNickName(); 
             final Namespace ns = Namespace.forName(nsName);
             final ExecutionVisitor exec = new ExecutionVisitor(ns, ns, reporter);
             // resolve types
             TypeResolver.resolveAST(exp, ns, reporter);
             
             exp.visit(exec);
             final Literal result = exec.getSingleResult();
             return TypeMapper.literalToTypes(result);
         } catch (ASTTraversalException e) {
             // ignore the exception, just use plain value which was submitted
             return new Types.StringType(value);
         }
     }
 
 
     
     @Override
     public void addAttribute(String name, Types defaultValue, String description, 
             String category) throws DatabaseException {
         this.addAttribute(name, defaultValue, description, category, NO_CONSTRAINT);
     }
 
 
 
     @Override
     public void removeAttribute(String name) throws DatabaseException {
         try (final Write w = this.persistence.write()) {
             List<User> all = w.read().findList(User.class, "ALL_USERS");
             final Attribute att = w.read().findSingle(
                     Attribute.class, AttributeImpl.ATTRIBUTE_BY_NAME, new Param(name));
             
             if (att == null) {
                 throw new UnknownAttributeException(name);
             }
             
             logger.trace("Removing attribute from all users.");
             for (User user : all) {
                 final UserImpl u = (UserImpl) user;
                 u.getAttributes().remove(name);
             }
             w.remove(att);
             this.constraints.remove(name);
             logger.info("Attribute " + att + " removed.");
         }
     }
 
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         this.persistence = null;
         this.onlineCache.clear();
         this.onlineCache = null;
     }
 
 
 
     @Override
     public User createUser(String name, String password) {
         final UserImpl result = new UserImpl(name, password);
         result.setUserManager(this);
         final List<AttributeImpl> all = this.persistence.atomic().findList(
             AttributeImpl.class, AttributeImpl.ALL_ATTRIBUTES);
         
         for (Attribute att : all) {
             result.getAttributes().put(att.getName(), att.getDefaultValue());
         }
         return result;
     }
     
     
     
     @Override
     public void addUserListener(UserListener listener) {
         this.eventProvider.addListener(UserListener.class, listener);
     }
     
     
     
     @Override
     public void removeUserListener(UserListener listener) {
         this.eventProvider.removeListener(UserListener.class, listener);
     }
     
     
     
     protected void fireUserSignedOn(final UserEvent e) {
         final Listeners<UserListener> listeners = 
             this.eventProvider.getListeners(UserListener.class);
         
         Dispatchable<UserListener, UserEvent> d = 
             new Dispatchable<UserListener, UserEvent>(listeners, e) {
                 @Override
                 public void dispatch(UserListener listener, UserEvent event) {
                     listener.userSignedOn(event);
                 }
         };
         this.eventProvider.dispatchEvent(d);
     }
     
     
     
     protected void fireUserSignedOff(final UserEvent e) {
         final Listeners<UserListener> listeners = 
             this.eventProvider.getListeners(UserListener.class);
         
         Dispatchable<UserListener, UserEvent> d = 
             new Dispatchable<UserListener, UserEvent>(listeners, e) {
                 @Override
                 public void dispatch(UserListener listener, UserEvent event) {
                     listener.userSignedOff(event);
                 }
         };
         this.eventProvider.dispatchEvent(d);
     }
 }
