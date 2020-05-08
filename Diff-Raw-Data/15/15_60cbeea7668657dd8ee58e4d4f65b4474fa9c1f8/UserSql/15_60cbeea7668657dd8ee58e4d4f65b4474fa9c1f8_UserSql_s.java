 /*
  * Created On: Jul 20, 2006 2:43:22 PM
  */
 package com.thinkparity.desdemona.model.io.sql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.jabber.JabberIdBuilder;
 
 import com.thinkparity.codebase.model.profile.VerificationKey;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.user.Feature;
 import com.thinkparity.codebase.model.user.Token;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.desdemona.model.io.hsqldb.HypersonicException;
 import com.thinkparity.desdemona.model.io.hsqldb.HypersonicSession;
 
 /**
  * @author raymond@thinkparity.com
  * @version
  */
 public class UserSql extends AbstractSql {
 
     /** Sql to create an email address. */
     private static final String SQL_CREATE_EMAIL =
             new StringBuffer("insert into parityUserEmail ")
             .append("(username,email,verified,verificationKey) ")
             .append("values (?,?,?,?)")
             .toString();
 
     /** Sql to delete an email address. */
     private static final String SQL_DELETE_EMAIL =
             new StringBuffer("delete from PARITYUSEREMAIL ")
             .append("where USERNAME=? and EMAIL=?")
             .toString();
 
     /** Sql to read all users. */
     private static final String SQL_READ =
             new StringBuffer("select * ")
             .append("from jiveUser JU ")
             .append("inner join PARITY_USER_PROFILE PUP on JU.USERNAME=PUP.USERNAME ")
             .append("order by JU.USERNAME")
             .toString();
 
     /** Sql to read an archive user's credentials. */
     private static final String SQL_READ_ARCHIVE_CREDENTIALS =
             new StringBuffer("select USERNAME,PASSWORD ")
             .append("from jiveUser JU ")
             .append("inner join PARITY_USER_ARCHIVE_REL PUAR ")
             .append("on JU.USERNAME=PUAR.ARCHIVENAME ")
             .append("where JU.USERNAME=?")
             .toString();
         
     /** Sql to read a user's archive ids. */
     private static final String SQL_READ_ARCHIVE_IDS =
             new StringBuffer("select PUAR.ARCHIVENAME ")
             .append("from PARITY_USER_ARCHIVE_REL PUAR ")
             .append("where PUAR.USERNAME=? ")
             .append("order by PUAR.ARCHIVENAME asc")
             .toString();
 
     /** Sql to read a user. */
     private static final String SQL_READ_BY_USER_ID =
             new StringBuffer("select JU.USERNAME ")
             .append("from jiveUser JU ")
             .append("where JU.USERNAME=? ")
             .append("order by JU.USERNAME")
             .toString();
 
     /** Sql to read email addresses. */
     private static final String SQL_READ_EMAIL =
             new StringBuffer("select PUE.USERNAME,PUE.EMAIL,PUE.VERIFIED ")
             .append("from parityUserEmail PUE ")
             .append("inner join jiveUser JU on PUE.USERNAME = JU.USERNAME ")
             .append("where JU.USERNAME=? and PUE.EMAIL=? ")
             .append("and PUE.VERIFICATIONKEY=?")
             .toString();
 
     /** Sql to read e-mail addresses. */
     private static final String SQL_READ_EMAILS =
             new StringBuffer("select PUE.USERNAME,PUE.EMAIL,PUE.VERIFIED ")
             .append("from parityUserEmail PUE ")
             .append("inner join jiveUser JU on PUE.USERNAME = JU.USERNAME ")
             .append("where JU.USERNAME=? and PUE.VERIFIED=?")
             .toString();
 
     /** Read all custom features for the user. */
     private static final String SQL_READ_FEATURES =
             new StringBuffer("select PF.FEATURE_ID,PF.FEATURE_NAME ")
             .append("from jiveUser JU ")
             .append("inner join PARITY_USER_FEATURE_REL PUFR on JU.USERNAME=PUFR.USERNAME ")
             .append("inner join PARITY_FEATURE PF on PUFR.FEATURE_ID=PF.FEATURE_ID ")
             .append("where JU.USERNAME=?")
             .toString();
 
     /** Sql to read the user profile's security answer. */
     private static final String SQL_READ_PROFILE_SECURITY_ANSWER =
            new StringBuffer("select PUP.SECURITYANSWER ")
             .append("from PARITY_USER_PROFILE PUP ")
             .append("where PUP.USERNAME=?")
             .toString();
 
     /** Sql to read the user profile's security question. */
     private static final String SQL_READ_PROFILE_SECURITY_QUESTION =
            new StringBuffer("select PUP.SECURITYQUESTION ")
             .append("from PARITY_USER_PROFILE PUP ")
             .append("where PUP.USERNAME=?")
             .toString();
 
     /** Sql to read the user's token. */
     private static final String SQL_READ_PROFILE_TOKEN =
         new StringBuffer("select PUP.TOKEN ")
         .append("from PARITY_USER_PROFILE PUP ")
         .append("inner join jiveUser JU on PUP.USERNAME=JU.USERNAME ")
         .append("where JU.USERNAME=? and PUP.TOKEN is not null")
         .toString();
 
     /** Sql to read the user's vcard. */
     private static final String SQL_READ_PROFILE_VCARD =
         new StringBuffer("select PUP.VCARD ")
         .append("from PARITY_USER_PROFILE PUP ")
         .append("inner join jiveUser JU on PUP.USERNAME=JU.USERNAME ")
         .append("where JU.USERNAME=?")
         .toString();
 
     /** Sql to read a username from an e-mail address. */
     private static final String SQL_READ_USERNAME =
             new StringBuffer("select JU.USERNAME ")
             .append("from parityUserEmail PUE ")
             .append("inner join jiveUser JU on PUE.USERNAME=JU.USERNAME ")
             .append("where PUE.EMAIL=?")
             .toString();
 
     /** Sql to create the user's token. */
     private static final String SQL_UPDATE_PROFILE_TOKEN =
         new StringBuffer("update PARITY_USER_PROFILE PUP ")
         .append("set TOKEN=? where USERNAME=?")
         .toString();
 
     /** Sql to update the user's vcard. */
     private static final String SQL_UPDATE_PROFILE_VCARD =
         new StringBuffer("update PARITY_USER_PROFILE PUP ")
         .append("set VCARD=? where USERNAME=?")
         .toString();
 
     private static final String SQL_VERIFY_EMAIL =
             new StringBuffer("update PARITYUSEREMAIL ")
             .append("set VERIFIED=?, VERIFICATIONKEY=?")
             .append("where USERNAME=? and EMAIL=? and VERIFICATIONKEY=?")
             .toString();
 
     /** Create UserSql. */
     public UserSql() { super(); }
 
     public void createEmail(final JabberId userId, final EMail email,
             final VerificationKey key) throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         try {
             cx = getCx();
             logStatement(SQL_CREATE_EMAIL);
             ps = cx.prepareStatement(SQL_CREATE_EMAIL);
             ps.setString(1, userId.getUsername());
             ps.setString(2, email.toString());
             ps.setBoolean(3, Boolean.FALSE);
             ps.setString(4, key.getKey());
             if (1 != ps.executeUpdate())
                 throw new SQLException("COULD NOT CREATE EMAIL");
             cx.commit();
         } finally {
            close(cx, ps);
         }
     }
 
     public void deleteEmail(final JabberId userId, final EMail email)
             throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         try {
             cx = getCx();
             ps = cx.prepareStatement(SQL_DELETE_EMAIL);
             ps.setString(1, userId.getUsername());
             ps.setString(2, email.toString());
             if (1 != ps.executeUpdate())
                 throw new SQLException("COULD NOT DELETE EMAIL");
             cx.commit();
         } finally {
            close(cx, ps);
         }
     }
 
     /**
      * Read a list of users.
      * 
      * @return A list of users.
      */
     public List<User> read() {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ);
             session.executeQuery();
             final List<User> users = new ArrayList<User>();
             while (session.nextResult()) {
                 users.add(extract(session));
             }
             return users;
         } finally {
             session.close();
         }
     }
 
     public User read(final JabberId userId) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ_BY_USER_ID);
             session.setString(1, userId.getUsername());
             session.executeQuery();
             if (session.nextResult()) {
                 return extract(session);
             } else {
                 return null;
             }
         } finally {
             session.close();
         }
     }
 
     public Credentials readArchiveCredentials(final JabberId archiveId) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ_ARCHIVE_CREDENTIALS);
             session.setString(1, archiveId.getUsername());
             session.executeQuery();
             if (session.nextResult()) {
                 return extractCredentials(session);
             } else {
                 return null;
             }
         } finally {
             session.close();
         }
     }
 
     public List<JabberId> readArchiveIds(final JabberId userId) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ_ARCHIVE_IDS);
             session.setString(1, userId.getUsername());
             session.executeQuery();
             final List<JabberId> archiveIds = new ArrayList<JabberId>();
             while (session.nextResult()) {
                 archiveIds.add(JabberIdBuilder.parseUsername(
                         session.getString("ARCHIVENAME")));
             }
             return archiveIds;
         } finally {
             session.close();
         }
     }
 
     public EMail readEmail(final JabberId userId, final EMail email,
             final VerificationKey key) throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             cx = getCx();
             ps = cx.prepareStatement(SQL_READ_EMAIL);
             ps.setString(1, userId.getUsername());
             ps.setString(2, email.toString());
             ps.setString(3, key.getKey());
             rs = ps.executeQuery();
 
             if (rs.next()) {
                 return EMailBuilder.parse(rs.getString("EMAIL"));
             } else {
                 return null;
             }
         } finally {
             close(cx, ps, rs);
         }
     }
 
     public List<EMail> readEmails(final JabberId jabberId,
             final Boolean verified) throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             cx = getCx();
             ps = cx.prepareStatement(SQL_READ_EMAILS);
             ps.setString(1, jabberId.getUsername());
             ps.setBoolean(2, verified);
             rs = ps.executeQuery();
             final List<EMail> emails = new ArrayList<EMail>();
             while (rs.next()) {
                 emails.add(extractEMail(rs));
             }
             return emails;
         }
         finally { close(cx, ps, rs); }
     }
 
     public List<Feature> readFeatures(final JabberId userId) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ_FEATURES);
             session.setString(1, userId.getUsername());
             session.executeQuery();
             final List<Feature> features = new ArrayList<Feature>();
             while (session.nextResult()) {
                 features.add(extractFeature(session));
             }
             return features;
         } finally {
             session.close();
         }
     }
 
     /**
      * Read the user profile's security answer.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return The security question answer <code>String</code>.
      * @throws SQLException
      */
     public String readProfileSecurityAnswer(final JabberId userId)
             throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             cx = getCx();
             ps = cx.prepareStatement(SQL_READ_PROFILE_SECURITY_ANSWER);
             ps.setString(1, userId.getUsername());
             rs = ps.executeQuery();
             if (rs.next()) {
                 return rs.getString("SECURITY_ANSWER");
             } else {
                 return null;
             }
         } finally {
             close(cx, ps, rs);
         }
     }
 
     /**
      * Read the user profile's security answer.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return The security question answer <code>String</code>.
      * @throws SQLException
      */
     public String readProfileSecurityQuestion(final JabberId userId)
             throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             cx = getCx();
             ps = cx.prepareStatement(SQL_READ_PROFILE_SECURITY_QUESTION);
             ps.setString(1, userId.getUsername());
             rs = ps.executeQuery();
             if (rs.next()) {
                 return rs.getString("SECURITY_QUESTION");
             } else {
                 return null;
             }
         } finally {
             close(cx, ps, rs);
         }
     }
 
     public Token readProfileToken(final JabberId userId) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ_PROFILE_TOKEN);
             session.setString(1, userId.getUsername());
             session.executeQuery();
             if (session.nextResult()) {
                 return extractToken(session);
             } else {
                 return null;
             }
         } finally {
             session.close();
         }
     }
 
     public String readProfileVCard(final JabberId userId) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_READ_PROFILE_VCARD);
             session.setString(1, userId.getUsername());
             session.executeQuery();
             if (session.nextResult()) {
                 return session.getString("VCARD");
             } else {
                 return null;
             }
         } finally {
             session.close();
         }
     }
 
     /**
      * Read a jabber id for an e-mail addreses.
      * 
      * @param email
      *            An e-mail address.
      * @return A jabber id.
      * @throws SQLException
      */
     public String readUsername(final EMail email) throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             cx = getCx();
             logStatement(SQL_READ_USERNAME);
             ps = cx.prepareStatement(SQL_READ_USERNAME);
             set(ps, 1, email.toString());
             rs = ps.executeQuery();
             if(rs.next()) {
                 return rs.getString("USERNAME");
             } else {
                 return null;
             }
         } finally {
             close(cx, ps, rs);
         }
     }
 
     public void updateProfileToken(final JabberId userId, final Token token) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_UPDATE_PROFILE_TOKEN);
             session.setString(1, token.getValue());
             session.setString(2, userId.getUsername());
             if (1 != session.executeUpdate())
                 throw new HypersonicException("Could not update profile token.");
 
             session.commit();
         } catch (final Throwable t) {
             throw translateError(session, t);
         } finally {
             session.close();
         }
     }
 
     public void updateProfileVCard(final JabberId userId, final String vcardXML) {
         final HypersonicSession session = openSession();
         try {
             session.prepareStatement(SQL_UPDATE_PROFILE_VCARD);
             session.setString(1, vcardXML);
             session.setString(2, userId.getUsername());
             if (1 != session.executeUpdate())
                 throw new HypersonicException("Could not update profile vcard.");
 
             session.commit();
         } catch (final Throwable t) {
             throw translateError(session, t);
         } finally {
             session.close();
         }
     }
 
     public void verifyEmail(final JabberId userId, final EMail email,
             final VerificationKey key) throws SQLException {
         Connection cx = null;
         PreparedStatement ps = null;
         try {
             cx = getCx();
             ps = cx.prepareStatement(SQL_VERIFY_EMAIL);
             ps.setBoolean(1, Boolean.TRUE);
             ps.setString(2, null);
             ps.setString(3, userId.getUsername());
             ps.setString(4, email.toString());
             ps.setString(5, key.getKey());
             if (1 != ps.executeUpdate())
                 throw new SQLException("COULD NOT VERIFY EMAIL");
             cx.commit();
         } catch(final SQLException sqlx) {
             cx.rollback();
             throw sqlx;
         } finally {
             close(cx, ps);
         }
     }
 
     /**
      * Extract the user information from the database session.
      * 
      * @param session
      *            A db session.
      * @return A user.
      */
     User extract(final HypersonicSession session) {
         final User user = new User();
         user.setId(JabberIdBuilder.parseUsername(session.getString("USERNAME")));
         return user;
     }
 
     Credentials extractCredentials(final HypersonicSession session) {
         final Credentials credentials = new Credentials();
         credentials.setPassword(session.getString("PASSWORD"));
         credentials.setUsername(session.getString("USERNAME"));
         return credentials;
     }
 
     EMail extractEMail(final ResultSet rs) throws SQLException {
         return EMailBuilder.parse(rs.getString("EMAIL"));
     }
 
     Feature extractFeature(final HypersonicSession session) {
         return Feature.valueOf(session.getString("FEATURE"));
     }
 
     /**
      * Extract a token from the session.
      * 
      * @param session
      *            A database <code>HypersonicSession</code>.
      * @return A <code>Token</code>.
      */
     private Token extractToken(final HypersonicSession session) {
         final Token token = new Token();
         token.setValue(session.getString("TOKEN"));
         return token;
     }
 }
