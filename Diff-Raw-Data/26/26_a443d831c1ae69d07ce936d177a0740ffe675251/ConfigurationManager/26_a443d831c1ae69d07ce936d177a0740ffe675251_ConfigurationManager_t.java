 /*
 * Copyright 2012-2013, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.servlet;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.BatchUpdateException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
 import javax.persistence.RollbackException;
 import javax.persistence.TypedQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.eccles.EcclesProxyConfiguration;
 import au.edu.uq.cmm.eccles.StaticEcclesProxyConfiguration;
 import au.edu.uq.cmm.paul.GrabberFacilityConfig;
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.PaulFacilityMapper;
 import au.edu.uq.cmm.paul.StaticPaulConfiguration;
 import au.edu.uq.cmm.paul.StaticPaulFacilities;
 import au.edu.uq.cmm.paul.StaticPaulFacility;
 import au.edu.uq.cmm.paul.status.DatafileTemplate;
 import au.edu.uq.cmm.paul.status.Facility;
 
 public class ConfigurationManager {
     private static class Configs{
         private PaulConfiguration config;
         private EcclesProxyConfiguration proxyConfig;
         private int facilityCount;
         
         public Configs(Configs configs) {
             this(configs.config, configs.proxyConfig, configs.facilityCount);
         }
         
         public Configs(PaulConfiguration config,
                 EcclesProxyConfiguration proxyConfig,
                 int facilityCount) {
             super();
             this.config = config;
             this.proxyConfig = proxyConfig;
             this.facilityCount = facilityCount;
         }
         
         public boolean incomplete() {
             return this.config == null || this.proxyConfig == null || this.facilityCount == 0;
         }
 
         @Override
         public String toString() {
             return "Configs [config=" + config + ", proxyConfig=" + proxyConfig
                     + ", facilityCount=" + facilityCount + "]";
         }
     }
     
     private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);
 
     private Configs activeConfigs;
     private Configs latestConfigs;
     private StaticPaulConfiguration staticConfig;
     private StaticEcclesProxyConfiguration staticProxyConfig;
     private EntityManagerFactory entityManagerFactory;
     private StaticPaulFacilities staticFacilities;
 
     
     public ConfigurationManager(EntityManagerFactory entityManagerFactory,
             StaticPaulConfiguration staticConfig, 
             StaticEcclesProxyConfiguration staticProxyConfig, 
             StaticPaulFacilities staticFacilities) {
         this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
         this.staticConfig = staticConfig;
         this.staticProxyConfig = staticProxyConfig;
         this.staticFacilities =  staticFacilities;
         activeConfigs = loadConfigurations();
         if (activeConfigs.incomplete()) {
             activeConfigs = doResetConfigurations(activeConfigs.facilityCount == 0);
         }
         latestConfigs = new Configs(activeConfigs);
     }
 
     public PaulConfiguration getActiveConfig() {
         return activeConfigs.config;
     }
 
     public PaulConfiguration getLatestConfig() {
         return latestConfigs.config;
     }
     
     public EcclesProxyConfiguration getActiveProxyConfig() {
         return activeConfigs.proxyConfig;
     }
 
     public EcclesProxyConfiguration getLatestProxyConfig() {
         return latestConfigs.proxyConfig;
     }
 
     public void resetConfigurations() {
         latestConfigs = doResetConfigurations(false);
     }
     
     private Configs loadConfigurations() {
         Configs configs = new Configs(
                 PaulConfiguration.load(entityManagerFactory),
                 EcclesProxyConfiguration.load(entityManagerFactory),
                 PaulFacilityMapper.getFacilityCount(entityManagerFactory));
         LOG.info("Loaded configs from database: " + configs);
         return configs;
     }
 
     private Configs doResetConfigurations(boolean reloadFacilities) {
         LOG.info("Resetting from static configurations");
         LOG.info("Static grabber configs: " + staticConfig);
         LOG.info("Static proxy configs: " + staticProxyConfig);
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             PaulConfiguration newConfig = new PaulConfiguration(staticConfig);
             EcclesProxyConfiguration newProxyConfig = new EcclesProxyConfiguration(staticProxyConfig);
             // FIXME - we do two transactions.  Combining the two transactions 
             // into one transaction is gives constraint errors when adding the new
             // facilities.  (The fix is to version the configurations.)
             em.getTransaction().begin();
            try {
                PaulConfiguration oldConfig = em.
                        createQuery("from PaulConfiguration", PaulConfiguration.class).
                        getSingleResult();
                em.remove(oldConfig);
            } catch (NoResultException ex) {
                // OK
            }
            try {
                EcclesProxyConfiguration oldProxyConfig = em.
                        createQuery("from EcclesProxyConfiguration", EcclesProxyConfiguration.class).
                        getSingleResult();
                em.remove(oldProxyConfig);  
            } catch (NoResultException ex) {
                // OK
            }
             if (reloadFacilities) {
                 List<Facility> facilities = em.
                         createQuery("from Facility", Facility.class).
                         getResultList();
                 for (Facility facility : facilities) {
                     em.remove(facility);
                 }
             }
             em.getTransaction().commit();
             
             // Second transaction
             em.getTransaction().begin();
             em.persist(newConfig);
             em.persist(newProxyConfig);
             if (reloadFacilities) {
                 for (StaticPaulFacility staticFacility : staticFacilities.getFacilities()) {
                     Facility facility = new Facility(staticFacility);
                     em.persist(facility);
                 }
             }
             em.getTransaction().commit();
             return new Configs(newConfig, newProxyConfig, 
                         PaulFacilityMapper.getFacilityCount(entityManagerFactory));
         } catch (UnknownHostException ex) {
             LOG.error("Reset failed", ex);
             return null;
         } finally {
             em.close();
         }
     }
 
     public ValidationResult<Facility> createFacility(Map<?, ?> params) {
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             em.getTransaction().begin();
             Facility facility = new Facility();
             Map<String, String> diags = buildFacility(facility, params, em);
             if (diags.isEmpty()) {
                 em.persist(facility);
                 em.getTransaction().commit();
             } else {
                 em.getTransaction().rollback();
             }
             return new ValidationResult<Facility>(diags, facility);
         } catch (RollbackException ex) {
             diagnoseRollback(ex);
             throw ex;
         } finally {
             em.close();
         }
     }
 
     public ValidationResult<Facility> updateFacility(String facilityName, Map<?, ?> params) {
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             em.getTransaction().begin();
             TypedQuery<Facility> query = em.createQuery(
                     "from Facility f where f.facilityName = :name",
                     Facility.class);
             query.setParameter("name", facilityName);
             Facility facility = query.getSingleResult();
             Map<String, String> diags = buildFacility(facility, params, em);
             if (diags.isEmpty()) {
                 em.getTransaction().commit();
             } else {
                 em.getTransaction().rollback();
             }
             return new ValidationResult<Facility>(diags, facility);
         } catch (RollbackException ex) {
             diagnoseRollback(ex);
             throw ex;
         } finally {
             em.close();
         }
     }
 
     public void deleteFacility(String facilityName) {
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             em.getTransaction().begin();
             TypedQuery<Facility> query = em.createQuery(
                     "from Facility f where f.facilityName = :name",
                     Facility.class);
             query.setParameter("name", facilityName);
             Facility facility = query.getSingleResult();
             em.remove(facility);
             em.getTransaction().commit();
         } catch (RollbackException ex) {
             diagnoseRollback(ex);
             throw ex;
         } finally {
             em.close();
         }
     }
     
     private void diagnoseRollback(RollbackException ex) {
         Throwable e = ex;
         while (e.getCause() != null) {
             e = e.getCause();
         }
         if (e instanceof BatchUpdateException) {
             LOG.error("update failed - next is",
                     ((BatchUpdateException) e).getNextException());
         } else {
             LOG.error("update failed - cause is", e);
         }
     }
     
     public Map<String, String> buildFacility(Facility res, Map<?, ?> params, EntityManager em) {
         Map<String, String> diags = new HashMap<String, String>();
         String facilityName = getNonEmptyString(params, "facilityName", diags);
         String localHostId = getStringOrNull(params, "localHostId", diags);
         String address = getStringOrNull(params, "address", diags);
         checkFacilityNameUnique(facilityName, res.getId(), diags, em);
         res.setMultiplexed(getBoolean(params, "multiplexed", diags));
         if (address != null) {
             checkAddressability(address, localHostId, res.getId(), 
             		res.isMultiplexed(), diags, em);
         }
         checkLocalHostIdUnique(localHostId, res.getId(), diags, em);
         if (address == null && localHostId == null) {
             addDiag(diags, "localHostId",
                     "the local host id must be non-empty if address is empty");
         }
         res.setFacilityDescription(getStringOrNull(params, "facilityDescription", diags));
         res.setAccessName(getStringOrNull(params, "accessName", diags));
         res.setAccessPassword(getStringOrNull(params, "accessPassword", diags));
         res.setFolderName(getNonEmptyString(params, "folderName", diags));
         res.setDriveName(getStringOrNull(params, "driveName", diags));
         if (res.getDriveName() != null && !res.getDriveName().matches("[A-Z]")) {
             addDiag(diags, "driveName", 
                     "the drive name must be a single uppercase letter");
         }
         res.setFileSettlingTime(getInteger(params, "fileSettlingTime", diags));
         if (res.getFileSettlingTime() < 0) {
             addDiag(diags, "fileSettlingTime", 
                     "the file setting time cannot be negative");
         }
         res.setCaseInsensitive(getBoolean(params, "caseInsensitive", diags));
         res.setUserOperated(getBoolean(params, "userOperated", diags));
         res.setUseFileLocks(getBoolean(params, "useFileLocks", diags));
         res.setUseFullScreen(getBoolean(params, "useFullScreen", diags));
         res.setUseTimer(getBoolean(params, "useTimer", diags));
         res.setDisabled(getBoolean(params, "disabled", diags));
         String arg = getNonEmptyString(params, "fileArrivalMode", diags);
         if (arg != null) {
             try {
                 res.setFileArrivalMode(GrabberFacilityConfig.FileArrivalMode.valueOf(arg));        
             } catch (IllegalArgumentException ex) {
                 addDiag(diags, "fileArrivalMode", "unrecognized mode '" + arg + "'");
             }
         }
         
         List<DatafileTemplate> templates = new LinkedList<DatafileTemplate>();
         int last = getInteger(params, "lastTemplate", diags);
         for (int i = 1; i <= last; i++) {
             String baseName = "template" + i;
             // If we have no parameters starting with the basename, skip.
             boolean found = false;
             for (Object paramName : params.keySet()) {
                 if (paramName.toString().startsWith(baseName)) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 continue;
             }
             if (getStringOrNull(params, baseName + "filePattern", diags) == null &&
                     getStringOrNull(params, baseName + "suffix", diags) == null &&
                     getStringOrNull(params, baseName + "mimeType", diags) == null) {
                 // This is a blank template ... ignore it.
                 continue;
             }
             // We have a template 'i' ...
             DatafileTemplate template = new DatafileTemplate();
             template.setFilePattern(getNonEmptyString(params, baseName + "filePattern", diags));
             template.setSuffix(getNonEmptyString(params, baseName + "suffix", diags));
             template.setMimeType(getNonEmptyString(params, baseName + "mimeType", diags));
             if (getStringOrNull(params, baseName + "minimumSize", diags) != null) {
                 int size = getInteger(params, baseName + "minimumSize", diags);
                 if (size < 0) {
                     addDiag(diags, baseName + "minimumSize", 
                             "minimum size cannot be negative");
                 } else {
                     template.setMinimumSize(size);
                 }
             }
             template.setOptional(getBoolean(params, baseName+ "optional", diags));
             for (DatafileTemplate existing : templates) {
                 if (template.getFilePattern() != null &&
                         template.getFilePattern().equals(existing.getFilePattern())) {
                     addDiag(diags, baseName + "filePattern", 
                             "this file pattern has already been used");
                 }
             }
             templates.add(template);
         }
         res.setDatafileTemplates(templates);
         
         // Set the key attributes at the end after we've done the uniqueness checks.
         // If we do them earlier, they may trigger DB level constraint errors due
         // premature updates.  But we DO need to do them even if the checks fail
         // so that the (possibly) bad values show up in the form.
         res.setFacilityName(facilityName);
         res.setAddress(address);
         res.setLocalHostId(localHostId);
         return diags;
     }
 
     private void checkAddressability(String address, String localHostId, Long id,
             boolean multiplexed, Map<String, String> diags, EntityManager em) {
         // Check that the supplied address is valiid / resolves.
         InetAddress inetAddr;
         try {
             inetAddr = InetAddress.getByName(address);
         } catch (UnknownHostException ex) {
             addDiag(diags, "address", ex.getMessage());
             return;
         }
         // If this facility has no local host id, then its address must
         // be unique.  We need to extract all other facility addresses
         // with no associated local host id, resolve them and compare the
         // IP addresses.
         TypedQuery<Object[]> query;
         if (id == null) {
             query = em.createQuery(
                     "select f.facilityName, f.address, f.multiplexed from Facility f", 
                             Object[].class);
         } else {
             query = em.createQuery(
                     "select f.facilityName, f.address, f.multiplexed from Facility f " +
                             "where f.id != :id", 
                             Object[].class);
             query.setParameter("id", id.longValue());
         }
         List<Object[]> others = query.getResultList();
         for (Object[] other : others) {
             try {
                 InetAddress otherAddr = InetAddress.getByName((String) other[1]);
                 Boolean otherMultiplexed = (Boolean) other[2];
                 if (otherAddr.equals(inetAddr) && /* compares IP addresses */
                 	!(multiplexed && otherMultiplexed)) {
                 	addDiag(diags, "address", 
                 			"address also used by facility '" + other[0] + "'.  " +
                 					"Resolve the address conflict or mark " +
                 					"both facilities as 'multiplexed'");
                 }
             } catch (UnknownHostException ex) {
                 // We cannot report this to the user ...
                 LOG.warn("Cannot resolve hostname / address " + other[1] +
                         " for facility '" + other[0] + "'");
             }
         }
     }
 
     private void checkFacilityNameUnique(String name, Long id,
             Map<String, String> diags, EntityManager em) {
         TypedQuery<String> query;
         if (id == null) {
             query = em.createQuery(
                     "select f.facilityName from Facility f " +
                     "where f.facilityName = :name", 
                     String.class);
         } else {
             query = em.createQuery(
                     "select f.facilityName from Facility f " +
                     "where f.facilityName = :name and f.id != :id", 
                     String.class);
             query.setParameter("id", id.longValue());
         }
         query.setParameter("name", name);
         List<String> names = query.getResultList();
         if (!names.isEmpty()) {
             addDiag(diags, "facilityName", "facility name '" + name + 
                     "' already used for another facility");
         }
     }
 
     private void checkLocalHostIdUnique(String localHostId, Long id,
             Map<String, String> diags, EntityManager em) {
         TypedQuery<String> query;
         if (id == null) {
             query = em.createQuery(
                     "select f.facilityName from Facility f " +
                     "where f.localHostId = :localHostId", 
                     String.class);
         } else {
             query = em.createQuery(
                     "select f.facilityName from Facility f " +
                     "where f.localHostId = :localHostId and f.id != :id", 
                     String.class);
             query.setParameter("id", id.longValue());
         }
         query.setParameter("localHostId", localHostId);
         List<String> names = query.getResultList();
         if (!names.isEmpty()) {
             addDiag(diags, "localHostId", "local host id '" + localHostId + 
                     "' already assigned to facility '" + names.get(0) + "'");
         }
     }
 
     private void addDiag(Map<String, String> diags, String key,
             String message) {
         if (diags.get(key) == null) {
             diags.put(key, message);
         }
     }
 
     private String getStringOrNull(Map<?, ?> params, String key, Map<String, String> diags) {
         String str = getString(params, key, diags, false);
         if (str != null && str.isEmpty()) {
             return null;
         } else {
             return str;
         }
     }
     
     private String getString(Map<?, ?> params, String key, 
             Map<String, String> diags, boolean canBeMissing) {
         String[] values = (String[]) params.get(key);
         if (values != null && values.length > 0) {
             return values[0].trim();
         } else {
             if (!canBeMissing) {
                 diags.put(key, "field is missing");
             }
             return null;
         }
     }
     
     private String getNonEmptyString(Map<?, ?> params, String key, Map<String, String> diags) {
         String str = getString(params, key, diags, false);
         if (str != null && str.isEmpty()) {
             addDiag(diags, key, "this field must not be empty");
         }
         return str;
     }
     
     private int getInteger(Map<?, ?> params, String key, Map<String, String> diags) {
         String str = getNonEmptyString(params, key, diags);
         try {
             return Integer.parseInt(str);
         } catch (NumberFormatException ex) {
             addDiag(diags, key, "this value is not a valid integer");
             return 0;
         }
     }
 
     private boolean getBoolean(Map<?, ?> params, String key, Map<String, String> diags) {
         String str = getString(params, key, diags, true);
         if (str == null || str.isEmpty() || str.equalsIgnoreCase("false")) {
             return false;
         } else if (str.equalsIgnoreCase("true")) {
             return true;
         } else {
             addDiag(diags, key, "this value must be 'true' or 'false' / ''");
             return false;
         }
     }
 }
