 package edu.wisc.portlet.blackboardvcportlet.service.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.jasig.portlet.blackboardvcportlet.data.BasicUser;
 import org.jasig.portlet.blackboardvcportlet.data.BasicUserImpl;
 import org.jasig.portlet.blackboardvcportlet.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.jdbc.core.JdbcOperations;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 /**
  * TODO result count limiting
  * 
  * @author Eric Dalquist
  */
 @Repository
 public class IaaUserService implements UserService {
     private static final Pattern NAME_SPLIT = Pattern.compile("\\s+");
     
     private JdbcOperations jdbcOperations;
 
     @Autowired
     public void setJdbcOperations(JdbcOperations jdbcOperations) {
         this.jdbcOperations = jdbcOperations;
     }
 
     @Override
     public BasicUser findUser(String uniqueId) {
         final List<BasicUser> results = this.jdbcOperations.query(
                 "select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS " +
                 "from iaaviews.portal_blackboard_lookup " +
                 "where spvi = (pase.get_pvi_by_eppn(?))", 
                 BasicUserRowMapper.INSTANCE, 
                 uniqueId);
         
         return DataAccessUtils.singleResult(results);
     }
 
     @Override
     public Set<BasicUser> searchForUserByName(String name) {
         final List<String> nameParts = getNameParts(name);
         
         //Nothing useful to search on return an empty set
         if (nameParts.isEmpty()) {
             return Collections.emptySet();
         }
         
         final Object[] args = new Object[3];
         
         args[0] = nameParts.get(0) + "%";
         args[1] = nameParts.get(nameParts.size() - 1) + "%";
         final String displayNameSearch = NAME_SPLIT.matcher(name.trim()).replaceAll("%") + "%";
         args[2] = displayNameSearch;
         
         final String whereClause;
         if (nameParts.size() == 1) {
            whereClause = "upper(first_name) like upper(?) OR upper(last_name) like upper(?) OR upper(FIRST_NAME || ' ' || MIDDLE_NAME || ' ' || LAST_NAME) like upper(?)";
         }
         else {
            whereClause = "(upper(first_name) like upper(?) AND upper(last_name) like upper(?)) OR upper(FIRST_NAME || ' ' || MIDDLE_NAME || ' ' || LAST_NAME) like upper(?)";
         }
         
         final List<BasicUser> results = this.jdbcOperations.query(
                 "select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS " +
                 "from iaaviews.portal_blackboard_lookup " +
                 "where " + whereClause,
                 BasicUserRowMapper.INSTANCE, 
                 args);
         
         return Collections.unmodifiableSet(new LinkedHashSet<BasicUser>(results));
     }
 
     @Override
     public Set<BasicUser> searchForUserByEmail(String email) {
         email = StringUtils.trimToNull(email);
         if (email == null) {
             return Collections.emptySet();
         }
 
         final List<BasicUser> results = this.jdbcOperations.query(
                 "select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS " +
                 "from iaaviews.portal_blackboard_lookup " +
                 "where upper(EMAIL_ADDRESS) like upper(?)",
                 BasicUserRowMapper.INSTANCE, 
                 email + "%");
         
         return Collections.unmodifiableSet(new LinkedHashSet<BasicUser>(results));
     }
 
     protected List<String> getNameParts(String name) {
         final String[] nameParts = NAME_SPLIT.split(name);
         final List<String> usefulNameParts = new ArrayList<String>(nameParts.length);
         for (String namePart : nameParts) {
             namePart = StringUtils.trimToNull(namePart);
             if (namePart != null) {
                 usefulNameParts.add(namePart);
             }
         }
         return usefulNameParts;
     }
 
     private static final class BasicUserRowMapper implements RowMapper<BasicUser> {
         public static final BasicUserRowMapper INSTANCE = new BasicUserRowMapper();
         
         @Override
         public BasicUser mapRow(ResultSet rs, int rowNum) throws SQLException {
             final String uniqueId = rs.getString("EPPN");
             if (uniqueId == null) {
                 throw new IncorrectResultSizeDataAccessException("'EPPN' is a required attribute", 1, 0);
             }
             
             
             final String email = rs.getString("EMAIL_ADDRESS");
             final String firstName = StringUtils.trimToNull(rs.getString("FIRST_NAME"));
             final String middleName = StringUtils.trimToNull(rs.getString("MIDDLE_NAME"));
             final String lastName = StringUtils.trimToNull(rs.getString("LAST_NAME"));
             
             final StringBuilder displayName = new StringBuilder();
             if (firstName != null) {
                 displayName.append(firstName);
             }
             if (middleName != null) {
                 if (displayName.length() > 0) {
                     displayName.append(' ');
                 }
                 displayName.append(middleName);
             }
             if (lastName != null) {
                 if (displayName.length() > 0) {
                     displayName.append(' ');
                 }
                 displayName.append(lastName);
             }
             
             return new BasicUserImpl(uniqueId, email, displayName.length() > 0 ? displayName.toString() : null);
         }
     }
 }
