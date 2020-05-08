 /**
 	Copyright (c) 2011, Strata Health Solutions Inc.
  	All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without modification, are permitted 
 	provided that the following conditions are met:
 
 	Redistributions of source code must retain the above copyright notice, this list of conditions 
 	and the following disclaimer.
 
 	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 	and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
 	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 	BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 	OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 	EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
 
 // $codepro.audit.disable fieldJavadoc
 
 package shelob.core;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import javax.annotation.concurrent.Immutable;
 
 import shelob.core.interfaces.IUser;
 
 
 /**
  * @author melllaguno
  * @version $Revision: 1.0 $
  *
  * User parameter object
  */
 @Immutable
 public class User implements IUser {
 
 	private final String email;
 	private final String password;
 	
 	/**
 	 * The User Builder
 	 * @author melllaguno
 	 */
 	public static class Builder {  // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString
 		
 		// Required parameters
 		private final String email;
 		private final String password;
 				
 		/**
 		 * Default Builder Constructor with Required Parameters
 		 * @param email
 		 * @param password
 		 */
 		public Builder(String email, String password) {
 			this.email = email;
 			this.password = password;
 		}
 		
 		/**
 		 * The factory method
 		 * 		
 		 * @return a new instance of the User parameter object 
 		 */
 		public User build() {
 			return new User(this);
 		}
 	}
 	
 	/**
 	 * Limited Scope Default Constructor
 	 * 
	 * @param builder the User.Builder responsible for creating this object
 	 */
	protected User(Builder builder) {
 		
 		checkNotNull(builder);
 		
 		// required
 		email = checkNotNull(builder.email);
 		password = checkNotNull(builder.password);		
 	}
 		
 	/**
 	 * Method getEmail.
 	 * @return String
 	 */
 	public String getEmail() {
 		return email;
 	}
 	
 	/**
 	 * Method getPassword.
 	 * @return String
 	 */
 	public String getPassword() {
 		return password;
 	}
 		
 	/**
 	 * Method toString.
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return " Email : " + getEmail() + " Password : " + getPassword();
 	}
 
 }
