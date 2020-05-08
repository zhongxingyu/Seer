 package org.jboss.pressgang.ccms.server.contentspec;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.jboss.pressgang.ccms.contentspec.buglinks.BugzillaBugLinkStrategy;
 import org.jboss.pressgang.ccms.contentspec.buglinks.BugzillaBugLinkOptions;
 import org.jboss.pressgang.ccms.contentspec.exceptions.ValidationException;
 
 public class TeiidBugLinkStrategy extends BugzillaBugLinkStrategy {
     private boolean connected = false;
     private Connection connection;
 
     @Override
     public void initialise(final String serverUrl, Object... args) {
         setServerUrl(serverUrl);
     }
 
     protected void connect() {
         try {
             final Context ctx = new InitialContext();
             final DataSource ds = (DataSource) ctx.lookup("java:/TeiidVDB");
             connection = ds.getConnection();
             connected = true;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void disconnect() {
         if (connected) {
             connected = false;
             try {
                 connection.close();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     @Override
     public void validate(final BugzillaBugLinkOptions bugzillaOptions) throws ValidationException {
         try {
             if (!connected) {
                 connect();
             }
             if (!isNullOrEmpty(bugzillaOptions.getProduct())) {
                 final Integer productId = getProductId(bugzillaOptions.getProduct());
                 if (productId == null) {
                     throw new ValidationException("No Bugzilla Product exists for product \"" + bugzillaOptions.getProduct() + "\".");
                 } else {
                     // Validate the Bugzilla Component
                     if (bugzillaOptions.getComponent() != null) {
                         checkComponentExists(bugzillaOptions.getComponent(), productId);
                     }
 
                     // Validate the Bugzilla Version
                     if (bugzillaOptions.getVersion() != null) {
                         checkVersionExists(bugzillaOptions.getVersion(), productId);
                     }
                 }
             }
 
             // Validate the keywords
             if (!isNullOrEmpty(bugzillaOptions.getKeywords())) {
                 final String[] keywords = bugzillaOptions.getKeywords().split("\\s*,\\s*");
                 checkKeywordsExist(keywords);
             }
         } catch (ValidationException e) {
             throw e;
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             disconnect();
         }
     }
 
    protected Integer getProductId(final String product) throws SQLException {
         final PreparedStatement statement = connection.prepareStatement(
                 "SELECT id FROM Bugzilla.products products WHERE products.name = ?");
         statement.setString(1, product);
 
         final ResultSet resultSet = statement.executeQuery();
         Integer projectId = null;
         if (resultSet.next()) {
             projectId = resultSet.getInt("id");
         }
         resultSet.close();
         statement.close();
 
         return projectId;
     }
 
     protected void checkComponentExists(final String component, final Integer productId) throws SQLException, ValidationException {
         final PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, isactive FROM Bugzilla.components components WHERE components.name = ? AND product_id = ?");
         statement.setString(1, component);
         statement.setInt(2, productId);
 
         final ResultSet resultSet = statement.executeQuery();
         Integer componentId = null;
         Boolean isActive = null;
         if (resultSet.next()) {
             componentId = resultSet.getInt("id");
             isActive = resultSet.getBoolean("isactive");
         }
         resultSet.close();
         statement.close();
 
         if (componentId == null) {
             throw new ValidationException("No Bugzilla Component exists for component \"" + component + "\".");
         } else if (isActive == null || !isActive) {
             throw new ValidationException("The Bugzilla Component \"" + component + "\" is not active.");
         }
     }
 
     protected void checkVersionExists(final String version, final Integer productId) throws SQLException, ValidationException {
         final PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, isactive FROM Bugzilla.versions versions WHERE versions.value = ? AND versions.product_id = ?");
         statement.setString(1, version);
         statement.setInt(2, productId);
 
         final ResultSet resultSet = statement.executeQuery();
         Integer versionId = null;
         Boolean isActive = null;
         if (resultSet.next()) {
             versionId = resultSet.getInt("id");
             isActive = resultSet.getBoolean("isactive");
         }
         resultSet.close();
         statement.close();
 
         if (versionId == null) {
             throw new ValidationException("No Bugzilla Version exists for version \"" + version + "\".");
         } else if (isActive == null || !isActive) {
             throw new ValidationException("The Bugzilla Version \"" + version + "\" is not active.");
         }
     }
 
     protected void checkKeywordsExist(final String[] keywords) throws SQLException, ValidationException {
         final PreparedStatement statement = connection.prepareStatement(
                 "SELECT id FROM Bugzilla.keyworddefs keywords WHERE keywords.name = ?");
         for (final String keyword : keywords) {
             if (!doesKeywordExist(statement, keyword)) {
                 throw new ValidationException("No Bugzilla Keyword exists for keyword \"" + keyword + "\".");
             }
         }
         statement.close();
     }
 
     protected boolean doesKeywordExist(final PreparedStatement statement, final String keyword) throws SQLException {
         statement.setString(1, keyword);
 
         final ResultSet resultSet = statement.executeQuery();
         boolean exists = resultSet.next();
         resultSet.close();
 
         return exists;
     }
 }
