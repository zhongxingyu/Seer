 package org.antbear.tododont.backend.security.dao;
 
 import org.antbear.tododont.backend.security.entity.CustomUserDetails;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @Transactional(readOnly = true)
 public class CustomUserDetailsServiceImpl extends JdbcDaoImpl implements CustomUserDetailsService {
 
     private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsServiceImpl.class);
 
     public CustomUserDetailsServiceImpl() {
         super.setAuthoritiesByUsernameQuery("SELECT email,authority FROM authorities WHERE email = ?");
         super.setEnableAuthorities(true);
         super.setUsernameBasedPrimaryKey(true);
     }
 
     // --- Custom methods
 
     public boolean isExistingUser(final String email) {
        final String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        final Integer count = getJdbcTemplate().queryForObject(sql, Integer.class, email.toLowerCase());
        return count != null && count > 0;
     }
 
     @Transactional(readOnly = false)
     public void createUser(final CustomUserDetails user) {
         getJdbcTemplate().update(
                 "INSERT INTO users (email,password,enabled,registrationtoken,registered_since) VALUES (?,?,?,?,?)",
                 user.getUsername().toLowerCase(), user.getPassword(),
                 user.isEnabled(), user.getRegistrationToken(),
                 user.getRegisteredSince());
 
         getJdbcTemplate().update(
                 "INSERT INTO authorities (email, authority) VALUES (?, ?)",
                 user.getUsername().toLowerCase(), "ROLE_USER");
     }
 
     public CustomUserDetails loadUserByRegistrationToken(final String registrationToken) {
         log.debug("loadUserByRegistrationToken {}", registrationToken);
         final String query = "SELECT email,password,enabled,registrationtoken,registered_since,passwordresettoken,passwordreset_requested_at"
                 + " FROM users WHERE registrationtoken = ?";
 
         final List<UserDetails> userDetailsList = super.getJdbcTemplate().query(query,
                 new CustomUserDetailsRowMapper(), registrationToken);
 
         if (null == userDetailsList || userDetailsList.isEmpty()) {
             return null;
         }
         return (CustomUserDetails) userDetailsList.get(0);
     }
 
     public List<UserDetails> loadUsersWithMissingActivation() {
         return super.getJdbcTemplate().query("SELECT"
                 + " email,password,enabled,registrationtoken,registered_since,passwordresettoken,passwordreset_requested_at"
                 + " FROM users WHERE enabled = 0 AND registrationtoken IS NOT NULL",
                 new CustomUserDetailsRowMapper());
     }
 
     public List<UserDetails> loadUsersWithMissingPasswordReset() {
         return super.getJdbcTemplate().query("SELECT"
                 + " email,password,enabled,registrationtoken,registered_since,passwordresettoken,passwordreset_requested_at"
                 + " FROM users WHERE enabled = 1 AND passwordresettoken IS NOT NULL",
                 new CustomUserDetailsRowMapper());
     }
 
     @Transactional(readOnly = false)
     public void enableUser(final String email) {
         getJdbcTemplate().update("UPDATE users SET enabled = 1, registrationtoken = NULL WHERE email = ?",
                 email.toLowerCase());
     }
 
     public void clearPasswordResetToken(final String email) {
         updatePasswordResetToken(email, null, null);
     }
 
     @Transactional(readOnly = false)
     public void updatePasswordResetToken(final String email, final String passwordResetToken) {
         updatePasswordResetToken(email, passwordResetToken, new Date());
     }
 
     @Transactional(readOnly = false)
     public void updatePasswordResetToken(final String email, final String passwordResetToken,
                                          final Date passwordResetRequestedAt) {
         getJdbcTemplate().update("UPDATE users SET passwordresettoken = ?, passwordreset_requested_at = ? WHERE email = ?", passwordResetToken, passwordResetRequestedAt, email.toLowerCase());
     }
 
     @Transactional(readOnly = false)
     public void updatePassword(final CustomUserDetails user) {
         getJdbcTemplate().update("UPDATE users SET password = ?, passwordresettoken = NULL, passwordreset_requested_at = NULL WHERE email = ?",
                 user.getPassword(), user.getUsername().toLowerCase());
     }
 
     @Transactional(readOnly = false)
     public void deleteUser(final String email) {
         getJdbcTemplate().update("DELETE FROM users WHERE email = ?", email.toLowerCase());
     }
 
     // --- JdbcDaoImpl overrides
 
     @Override
     protected List<UserDetails> loadUsersByUsername(final String username) {
         log.debug("loadUsersByUsername {}", username.toLowerCase());
         final String query = "SELECT email,password,enabled,registrationtoken,registered_since,passwordresettoken,passwordreset_requested_at"
                 + " FROM users WHERE email = ?";
 
         return super.getJdbcTemplate().query(query, new CustomUserDetailsRowMapper(), username.toLowerCase());
     }
 
     @Override
     protected UserDetails createUserDetails(final String username, final UserDetails userFromUserQuery, final List<GrantedAuthority> combinedAuthorities) {
         final CustomUserDetails user = (CustomUserDetails) userFromUserQuery;
 
         final Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
         grantedAuthorities.addAll(combinedAuthorities);
 
         return new CustomUserDetails(username.toLowerCase(),
                 user.getPassword(),
                 user.isEnabled(),
                 grantedAuthorities,
                 user.getRegistrationToken(),
                 user.getRegisteredSince(),
                 user.getPasswordResetToken(),
                 user.getPasswordResetRequestedAt(),
                 user.isAccountNonExpired(),
                 user.isAccountNonLocked(),
                 user.isCredentialsNonExpired());
     }
 
     private class CustomUserDetailsRowMapper implements RowMapper<UserDetails> {
 
         @Override
         public CustomUserDetails mapRow(final ResultSet rs, final int rowNum) throws SQLException {
             int n = 1;
             final String email = rs.getString(n++).toLowerCase();
             final String password = rs.getString(n++);
             final boolean enabled = rs.getBoolean(n++);
             final String registrationToken = rs.getString(n++);
             final Date registeredSince = rs.getTimestamp(n++);
             final String passwordResetToken = rs.getString(n++);
             final Date passwordResetRequestedAt = rs.getTimestamp(n++);
 
             return new CustomUserDetails(email,
                     password,
                     enabled,
                     new HashSet<GrantedAuthority>(),
                     registrationToken,
                     registeredSince,
                     passwordResetToken,
                     passwordResetRequestedAt,
                     true, true, true);
         }
     }
 }
