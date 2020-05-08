 package models;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
import javax.persistence.Table;
 import javax.validation.constraints.Min;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.ExpressionList;
 import com.avaje.ebean.validation.Email;
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
 import com.feth.play.module.pa.user.AuthUser;
 import com.feth.play.module.pa.user.AuthUserIdentity;
 import com.feth.play.module.pa.user.EmailIdentity;
 import com.feth.play.module.pa.user.FirstLastNameIdentity;
 import com.feth.play.module.pa.user.NameIdentity;
 
 @Entity
@Table(name="YRUser")
 public class User extends Model {
 	
 	 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 		@Id
 	    @Min(10)
 	    public Long id;
 
 	 	@Required
 	    public String name;
 
 	 	@JsonIgnore
 	    @Required
 	    @Email
 	    public String email;
 
 		public boolean active;
 
 		public Date lastLogin;
 
 		public boolean emailValidated;
 
 		public String firstName;
 
 		public String lastName;
 		
 		public String address;
 		
 		public String lat;
 		
 		public String lon;
 		
 		public String recycledItems;
 		
 		public Boolean hasRegistered;
 		
 		public String coopIds;
 
 		@JsonIgnore
 		@OneToMany(cascade = CascadeType.ALL)
 		public List<LinkedAccount> linkedAccounts;
 	    
 	    public static Model.Finder<String,User> find = new Model.Finder(String.class, User.class);
 	    
 	    
 	    public static User create(final AuthUser authUser) {
 			final User user = new User();
 			// user.permissions = new ArrayList<UserPermission>();
 			// user.permissions.add(UserPermission.findByValue("printers.edit"));
 			user.active = true;
 			user.lastLogin = new Date();
 			user.linkedAccounts = Collections.singletonList(LinkedAccount
 					.create(authUser));
 
 			if (authUser instanceof EmailIdentity) {
 				final EmailIdentity identity = (EmailIdentity) authUser;
 				// Remember, even when getting them from FB & Co., emails should be
 				// verified within the application as a security breach there might
 				// break your security as well!
 				user.email = identity.getEmail();
 				user.emailValidated = false;
 			}
 
 			if (authUser instanceof NameIdentity) {
 				final NameIdentity identity = (NameIdentity) authUser;
 				final String name = identity.getName();
 				if (name != null) {
 					user.name = name;
 				}
 			}
 			
 			if (authUser instanceof FirstLastNameIdentity) {
 			  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
 			  final String firstName = identity.getFirstName();
 			  final String lastName = identity.getLastName();
 			  if (firstName != null) {
 			    user.firstName = firstName;
 			  }
 			  if (lastName != null) {
 			    user.lastName = lastName;
 			  }
 			}
 
 			user.hasRegistered=false;
 			user.save();
 //			user.saveManyToManyAssociations("roles");
 			// user.saveManyToManyAssociations("permissions");
 			return user;
 		}
 	    
 	    
 	    public static void addLinkedAccount(final AuthUser oldUser,
 				final AuthUser newUser) {
 			final User u = User.findByAuthUserIdentity(oldUser);
 			u.linkedAccounts.add(LinkedAccount.create(newUser));
 			u.save();
 		}
 	    
 	    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
 			if (identity == null) {
 				return null;
 			}
 			if (identity instanceof UsernamePasswordAuthUser) {
 				return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
 			} else {
 				return getAuthUserFind(identity).findUnique();
 			}
 		}
 	    
 	    public static User findByUsernamePasswordIdentity(
 				final UsernamePasswordAuthUser identity) {
 			return getUsernamePasswordAuthUserFind(identity).findUnique();
 		}
 	    
 		private static ExpressionList<User> getUsernamePasswordAuthUserFind(
 				final UsernamePasswordAuthUser identity) {
 			return getEmailUserFind(identity.getEmail()).eq(
 					"linkedAccounts.providerKey", identity.getProvider());
 		}
 		
 		private static ExpressionList<User> getEmailUserFind(final String email) {
 			return find.where().eq("active", true).eq("email", email);
 		}
 		
 		private static ExpressionList<User> getAuthUserFind(
 				final AuthUserIdentity identity) {
 			return find.where().eq("active", true)
 					.eq("linkedAccounts.providerUserId", identity.getId())
 					.eq("linkedAccounts.providerKey", identity.getProvider());
 		}
 		
 		public static boolean existsByAuthUserIdentity(
 				final AuthUserIdentity identity) {
 			final ExpressionList<User> exp;
 			if (identity instanceof UsernamePasswordAuthUser) {
 				exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
 			} else {
 				exp = getAuthUserFind(identity);
 			}
 			return exp.findRowCount() > 0;
 		}
 }
