 package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal;
 
 import gov.nih.nci.cabig.ctms.CommonsSystemException;
 import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.InvalidSuiteUserException;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
 import org.jruby.RubySymbol;
 import org.jruby.embed.ScriptingContainer;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.jruby.javasupport.JavaEmbedUtils.rubyToJava;
 
 /**
  * @author Rhett Sutphin
  */
 public class RubySuiteAuthorizationSource implements SuiteAuthorizationSource {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private ScriptingContainer scriptingContainer;
     private Object rubySuiteAuthorizationSource;
     private Map<String, RubySymbol> symbolMap = new HashMap<String, RubySymbol>();
 
     /**
      * Creates a new bridge source. The provided scripting container must contain a global variable
      * named <code>$suite_authorization_source</code> pointing to an object which provides the
      * interface defined in the README.
      *
      * @param scriptingContainer
      */
     public RubySuiteAuthorizationSource(ScriptingContainer scriptingContainer) {
         this.scriptingContainer = scriptingContainer;
         this.rubySuiteAuthorizationSource = scriptingContainer.get("$suite_authorization_source");
         if (rubySuiteAuthorizationSource == null) {
             throw new CommonsSystemException(
                 "The provided scripting container does not define a $suite_authorization_source");
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     private Collection<SuiteUser> usersFromUserHashes(Object resultArray) {
         if (resultArray == null) return Collections.emptyList();
 
         Collection<Object> hashes = (Collection<Object>) resultArray;
         Collection<SuiteUser> transformed = new ArrayList<SuiteUser>(hashes.size());
         for (Object o : hashes) {
             transformed.add(userFromUserHash(o));
         }
         return transformed;
     }
 
     private SuiteUser userFromUserHash(Object userHash) throws InvalidSuiteUserException {
         if (userHash == null) {
             return null;
         } else {
             Map<?, ?> userMap = (Map<?, ?>) rubyToJava((IRubyObject) userHash);
             SuiteUser.Builder userBuilder = new SuiteUser.Builder(false).
                 username((String) userMap.get(sym("username"))).
                 name((String) userMap.get(sym("first_name")), (String) userMap.get(sym("last_name"))).
                 id(((Number) userMap.get(sym("id"))).intValue()).
                emailAddress((String) userMap.get(sym("email_address"))).
                 accountEndsOn(createDate(userMap.get(sym("account_end_date"))));
             applyRoles(userBuilder, userMap.get(sym("roles")));
             return userBuilder.toUser();
         }
     }
 
     private Date createDate(Object date) {
         if (date == null) {
             return null;
         } else {
             Calendar cal = Calendar.getInstance();
             cal.set(
                 ((Number) scriptingContainer.callMethod(date, "year")).intValue(),
                 ((Number) scriptingContainer.callMethod(date, "month")).intValue() - 1,
                 ((Number) scriptingContainer.callMethod(date, "day")).intValue()
             );
             return cal.getTime();
         }
     }
 
     private void applyRoles(SuiteUser.Builder userBuilder, Object rolesHash) {
         if (rolesHash != null) {
             Map<?, ?> rolesMap = (Map<?, ?>) rubyToJava((IRubyObject) rolesHash);
             for (Map.Entry<?, ?> entry : rolesMap.entrySet()) {
                 SuiteRole role = SuiteRole.getByCsmName(entry.getKey().toString());
                 SuiteRoleMembership membership = new SuiteRoleMembership(role, null, null);
 
                 if (role.isScoped()) {
                     if (entry.getValue() instanceof IRubyObject) {
                         Map<?, ?> value = (Map<?, ?>) rubyToJava((IRubyObject) entry.getValue());
                         applyRoleScopeHash(membership, value);
                     } else {
                         if (role.isSiteScoped()) membership.forAllSites();
                         if (role.isStudyScoped()) membership.forAllStudies();
                     }
                 }
 
                 userBuilder.addRoleMembership(membership);
             }
         }
     }
 
     @SuppressWarnings({ "ChainOfInstanceofChecks", "unchecked" })
     private void applyRoleScopeHash(SuiteRoleMembership membership, Map<?, ?> value) {
         SuiteRole role = membership.getRole();
         for (ScopeType type : ScopeType.values()) {
             if (role.getScopes().contains(type)) {
                 RubySymbol key = sym(type.getPluralName());
                 Object scopeValue = value.get(key);
                 if (scopeValue == null) {
                     log.warn("Missing necessary scope value :{} for :{}", type.getPluralName(), role.getCsmName());
                 } else if (scopeValue instanceof Boolean) {
                     membership.forAll(type);
                 } else if (scopeValue instanceof List) {
                     List<String> scopeList = (List<String>) scopeValue;
                     String[] scopeIdents = scopeList.toArray(new String[scopeList.size()]);
                     membership.forIdentifiers(type, scopeIdents);
                 } else {
                     log.warn("Unexpected value ({}) for scope :{} and role :{}",
                         new Object[] { scopeValue, type.getPluralName(), role.getCsmName() });
                 }
             }
         }
     }
 
     private RubySymbol sym(String s) {
         if (!symbolMap.containsKey(s)) {
             symbolMap.put(s,
                 (RubySymbol) scriptingContainer.runScriptlet(String.format(":\"%s\"", s)));
         }
         return symbolMap.get(s);
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     private RubySymbol enumSymbol(Enum value) {
         return sym(value.name().toLowerCase());
     }
 
     @Override
     public SuiteUser getUser(String username, SuiteUserRoleLevel suiteUserRoleLevel) {
         Object result = scriptingContainer.callMethod(rubySuiteAuthorizationSource,
             "get_user_by_username", username, enumSymbol(suiteUserRoleLevel));
         return userFromUserHash(result);
     }
 
     @Override
     public SuiteUser getUser(int id, SuiteUserRoleLevel suiteUserRoleLevel) {
         Object result = scriptingContainer.callMethod(rubySuiteAuthorizationSource,
             "get_user_by_id", id, enumSymbol(suiteUserRoleLevel));
         return userFromUserHash(result);
     }
 
     @Override
     public Collection<SuiteUser> getUsersByRole(SuiteRole suiteRole) {
         Object result = scriptingContainer.callMethod(
             rubySuiteAuthorizationSource, "get_users_by_role", enumSymbol(suiteRole));
         return usersFromUserHashes(result);
     }
 
     @Override
     public Collection<SuiteUser> searchUsers(SuiteUserSearchOptions opts) {
         Map<RubySymbol, String> criteriaHash = new HashMap<RubySymbol, String>();
         if (opts.getFirstNameSubstring() != null) {
             criteriaHash.put(sym("first_name_substring"), opts.getFirstNameSubstring());
         }
         if (opts.getLastNameSubstring() != null) {
             criteriaHash.put(sym("last_name_substring"), opts.getLastNameSubstring());
         }
         if (opts.getUsernameSubstring() != null) {
             criteriaHash.put(sym("username_substring"), opts.getUsernameSubstring());
         }
 
         Object result = scriptingContainer.callMethod(rubySuiteAuthorizationSource,
             "search_users", criteriaHash);
         return usersFromUserHashes(result);
     }
 }
