 package jpaoletti.jpm.security.core;
 
 import jpaoletti.jpm.core.PMContext;
import jpaoletti.jpm.core.PresentationManager;
 
 /**
  * This is an abstract implementation for security connectors that uses BCrypt
  * algotithm
  *
  * @author jpaoletti
  */
 public abstract class PMSecurityAbstractConnector implements PMSecurityConnector {
 
     private PMContext ctx;
 
     @Override
     public PMSecurityUser authenticate(String username, String password) throws PMSecurityException {
         final PMSecurityUser user = getUser(username);
         if (user == null) {
             throw new UserNotFoundException();
         }
         if (user.isDeleted()) {
             throw new UserNotFoundException();
         }
         if (!user.isActive()) {
             throw new UserNotActiveException();
         }
        if (isLoggedIn(user) && !PresentationManager.getPm().allowMultipleLogin()) {
             throw new UserAlreadyLogged();
         }
         if (!BCrypt.checkpw(password, user.getPassword())) {
             throw new InvalidPasswordException();
         }
         return user;
     }
 
     @Override
     public void changePassword(String username, String password, String newpassword) throws PMSecurityException {
         final PMSecurityUser user = getUser(username);
         if (user == null) {
             throw new UserNotFoundException();
         }
         if (user.isDeleted()) {
             throw new UserNotFoundException();
         }
         if (password != null && !BCrypt.checkpw(password, user.getPassword())) {
             throw new InvalidPasswordException();
         }
         user.setPassword(encrypt(newpassword));
         updateUser(user);
     }
 
     protected String encrypt(String password) {
         return BCrypt.hashpw(password, BCrypt.gensalt(12));
     }
 
     @Override
     public void setContext(PMContext ctx) {
         this.ctx = ctx;
     }
 
     public PMContext getCtx() {
         return ctx;
     }
 
     protected void checkUserRules(String username, String password) throws PMSecurityException {
         if (username == null) {
             throw new InvalidUserException();
         }
         if (password == null) {
             throw new InvalidPasswordException();
         }
         //TODO Check rules
     }
 
     @Override
     public void removeUser(PMSecurityUser user) throws PMSecurityException {
         user.setDeleted(true);
         updateUser(user);
     }
 
     private boolean isLoggedIn(PMSecurityUser user) {
        return PresentationManager.getPm().getSessionByUser(user.getUsername()) != null;
     }
 }
