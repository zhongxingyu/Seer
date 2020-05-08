 package org.cagrid.gaards.dorian.service.upgrader;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import org.cagrid.gaards.dorian.federation.TrustedIdP;
 import org.cagrid.gaards.dorian.federation.TrustedIdPManager;
 import org.cagrid.gaards.dorian.idp.UserManager;
 import org.cagrid.gaards.dorian.service.PropertyManager;
 import org.cagrid.tools.database.Database;
 
 
 public class Upgrade1_3To1_4 extends Upgrade {
 
     public String getStartingVersion() {
         return PropertyManager.DORIAN_VERSION_1_3;
     }
 
 
     public String getUpgradedVersion() {
         return PropertyManager.DORIAN_VERSION_1_4;
     }
 
 
     public void upgrade(boolean trialRun) throws Exception {
         Database db = getBeanUtils().getDatabase();
         db.createDatabaseIfNeeded();
         PropertyManager pm = new PropertyManager(db);
         if (pm.getVersion().equals(PropertyManager.DORIAN_VERSION_1_3)) {
             if (!trialRun) {
                 System.out.print("Upgrading version number from " + pm.getVersion() + " to "
                     + PropertyManager.DORIAN_VERSION_1_4 + "....");
                 pm.setVersion(PropertyManager.DORIAN_VERSION_1_4);
                 System.out.println(" COMPLETED.");
             } else {
                 System.out.println("The version needs to be upgraded from " + pm.getVersion() + " to "
                     + PropertyManager.DORIAN_VERSION_1_4 + ".");
             }
             upgradeTrustedIdentityProviders(trialRun);
             upgradeLocalUserManager(trialRun);
         } else {
             if (!trialRun) {
                 throw new Exception("Failed to run upgrader " + getClass().getName()
                     + " the version of Dorian you are running is not " + PropertyManager.DORIAN_VERSION_1_3 + ".");
             }
         }
 
     }
 
 
     private void upgradeLocalUserManager(boolean trialRun) throws Exception {
         Database db = getBeanUtils().getDatabase();
         UserManager um = new UserManager(db, getBeanUtils().getIdentityProviderProperties());
         if (!trialRun) {
             um.buildDatabase();
 
             Connection c = null;
             try {
                 c = db.getConnection();
                 PreparedStatement s = c.prepareStatement("select UID, PASSWORD FROM " + UserManager.IDP_USERS_TABLE);
                 ResultSet rs = s.executeQuery();
                 while (rs.next()) {
                     String uid = rs.getString("UID");
                     String password = rs.getString("PASSWORD");
                     password = password.replace("\n", "");
                    db.update("update " + UserManager.IDP_USERS_TABLE + " SET PASSWORD='" + password + "' where UID='"
                        + uid + "'");
                 }
                 rs.close();
                 s.close();

             } catch (Exception e) {
                 throw e;
             } finally {
                 db.releaseConnection(c);
             }
         }
     }
 
 
     private void upgradeTrustedIdentityProviders(boolean trialRun) throws Exception {
         Database db = getBeanUtils().getDatabase();
         TrustedIdPManager idp = new TrustedIdPManager(getBeanUtils().getIdentityFederationProperties(), db);
         if (!trialRun) {
             idp.buildDatabase();
         }
 
         boolean hasPublish = false;
 
         Connection c = null;
         try {
             c = db.getConnection();
             PreparedStatement s = c.prepareStatement("show COLUMNS FROM " + TrustedIdPManager.TRUST_MANAGER_TABLE);
             ResultSet rs = s.executeQuery();
             while (rs.next()) {
                 if (rs.getString(1).equals(TrustedIdPManager.PUBLISH_FIELD)) {
                     hasPublish = true;
                 }
 
             }
             rs.close();
             s.close();
 
             if (!hasPublish) {
                 if (!trialRun) {
                     System.out.print("Adding " + TrustedIdPManager.PUBLISH_FIELD
                         + " field to the TrustedIdP database....");
                     s = c.prepareStatement("ALTER TABLE " + TrustedIdPManager.TRUST_MANAGER_TABLE + " ADD "
                         + TrustedIdPManager.PUBLISH_FIELD + " VARCHAR(1)");
                     s.execute();
                     s.close();
                     System.out.println(" COMPLETED.");
                 } else {
                     System.out.print("The field " + TrustedIdPManager.DISPLAY_NAME_FIELD
                         + " needs to be added to the TrustedIdP database.");
                 }
             }
 
             if (!trialRun) {
                 TrustedIdP[] list = idp.getTrustedIdPs();
                 if (list != null) {
                     for (int i = 0; i < list.length; i++) {
                         idp.setPublish(list[i], true);
                     }
                 }
             }
         } catch (Exception e) {
             throw e;
         } finally {
             db.releaseConnection(c);
         }
     }
 }
