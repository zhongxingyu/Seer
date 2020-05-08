 package edu.gatech.oad.rocket.findmythings.server.util;
 
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.DisabledAccountException;
 import org.apache.shiro.authc.ExcessiveAttemptsException;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.LockedAccountException;
 import org.apache.shiro.authc.UnknownAccountException;
 
 public final class Messages {
 	
 	private Messages() {}
 	
 	public enum Status {
 		OK("ok"),
 		UNAUTHORIZED("nopeNopeNope");
 		
 	    private final String text;
 
 	    private Status(final String text) {
 	        this.text = text;
 	    }
 
 	    @Override
 	    public String toString() {
 	        return text;
 	    }
 
 	}
 		
 	public enum Permissions {
 		REQUIRES_LOGIN("requiresLogin");
 		
 	    private final String text;
 
 	    private Permissions(final String text) {
 	        this.text = text;
 	    }
 
 	    @Override
 	    public String toString() {
 	        return text;
 	    }
 
 	}
 	
 	public enum Login {
 		NO_SUCH_USER("noSuchUser"),
 		BAD_PASSWORD("badPassword"),
 		ACCNT_LOCKED("accountLocked"),
 		ACCT_DISABLE("accountDisabled"),
 		MANY_ATTEMPT("tooManyAttempts"),
 		INVALID_DATA("invalidData");
 		
 	    private final String text;
 
 	    private Login(final String text) {
 	        this.text = text;
 	    }
 
 	    @Override
 	    public String toString() {
 	        return text;
 	    }
 		
 		public static Messages.Login get(AuthenticationException ae) {
 			if (ae instanceof UnknownAccountException) {
 				return Messages.Login.NO_SUCH_USER;
 			} else if (ae instanceof IncorrectCredentialsException) {
 				return Messages.Login.BAD_PASSWORD;
 			} else if (ae instanceof LockedAccountException) {
 				return Messages.Login.ACCNT_LOCKED;
 			} else if (ae instanceof DisabledAccountException) {
 				return Messages.Login.ACCT_DISABLE;
 			} else if (ae instanceof ExcessiveAttemptsException) {
 				return Messages.Login.MANY_ATTEMPT;
 			}
 			return Messages.Login.INVALID_DATA;
 		}
 		
 		public static String getMessage(AuthenticationException ae) {
 			return get(ae).toString();
 		}
 	}
 	
 }
