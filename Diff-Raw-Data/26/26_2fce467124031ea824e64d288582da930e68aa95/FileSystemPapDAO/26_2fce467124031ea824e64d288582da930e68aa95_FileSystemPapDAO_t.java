 package org.glite.authz.pap.repository.dao.filesystem;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.INIConfiguration;
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.utils.Utils;
 import org.glite.authz.pap.papmanagement.PapContainer;
 import org.glite.authz.pap.repository.RepositoryManager;
 import org.glite.authz.pap.repository.dao.PapDAO;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.glite.authz.pap.repository.exceptions.RepositoryException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Filesystem implementation of the {@link PapDAO} interface.
  * <p>
  * This DAO stores information about the paps and the version of the repository. These information
  * are written in an INI file.
  */
 public class FileSystemPapDAO implements PapDAO {
 
     private static FileSystemPapDAO instance = null;
     private static String dbPath = FileSystemRepositoryManager.getFileSystemDatabaseDir();
 
     @SuppressWarnings("unused")
     private static final Logger log = LoggerFactory.getLogger(FileSystemPapDAO.class);
 
     private static final String PAP_FILE_NAME = "pap_info.ini";
     private static final String PAPS_STANZA = "paps";
     private static final String VERSION_KEY = "general-info.version";
 
     private INIConfiguration iniConfiguration;
 
     /**
      * Constructor.
      * 
      * @throws RepositoryException wrapping a {@link ConfigurationException} if problems occurred
      *             while reading the paps configuration file.
      */
     private FileSystemPapDAO() {
         iniConfiguration = new INIConfiguration();
 
         File iniPAPConfigurationFile = new File(dbPath + File.separator + PAP_FILE_NAME);
 
         iniConfiguration.setFile(iniPAPConfigurationFile);
 
         try {
 
             if (!iniPAPConfigurationFile.exists()) {
                 iniConfiguration.setProperty(VERSION_KEY, RepositoryManager.REPOSITORY_MANAGER_VERSION);
                 iniConfiguration.save();
                 iniConfiguration.clearProperty(VERSION_KEY);
             }
 
             iniConfiguration.load();
         } catch (ConfigurationException e) {
             throw new RepositoryException("Configuration error", e);
         }
 
     }
 
     public static FileSystemPapDAO getInstance() {
         if (instance == null)
             instance = new FileSystemPapDAO();
         return instance;
     }
 
     /**
      * Returns the first part of the key (INI configuration) identifying a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String aliasKey(String papAlias) {
         return PAPS_STANZA + "." + papAlias;
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>dn</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String dnKey(String papAlias) {
         return aliasKey(papAlias) + "." + "dn";
     }
     
     /**
      * Returns the key (INI configuration) holding the <code>enabled</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String enabledKey(String papAlias) {
         return aliasKey(papAlias) + "." + "enabled";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>hostname</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String hostnameKey(String papAlias) {
         return aliasKey(papAlias) + "." + "hostname";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>id</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String idKey(String papAlias) {
         return aliasKey(papAlias) + "." + "id";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>path</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String pathKey(String papAlias) {
         return aliasKey(papAlias) + "." + "path";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>policyLastModificationTime</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String policyLastModificationTimeKey(String papAlias) {
         return aliasKey(papAlias) + "." + "policyLastModificationTime";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>port</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String portKey(String papAlias) {
         return aliasKey(papAlias) + "." + "port";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>protocol</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String protocolKey(String papAlias) {
         return aliasKey(papAlias) + "." + "protocol";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>type</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String typeKey(String papAlias) {
         return aliasKey(papAlias) + "." + "type";
     }
 
     /**
      * Returns the key (INI configuration) holding the <code>visibilityPublic</code> value of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key.
      */
     private static String visibilityPublicKey(String papAlias) {
         return aliasKey(papAlias) + "." + "public";
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete(String papAlias) {
 
         if (!exists(papAlias)) {
             throw new NotFoundException(String.format("Not found: papAlias=%s", papAlias));
         }
 
         String papId = iniConfiguration.getString(idKey(papAlias));
 
         PapContainer papContainer = new PapContainer(get(papAlias));
 
         papContainer.deleteAllPolicies();
         papContainer.deleteAllPolicySets();
 
         File papDir = new File(getPapDirAbsolutePath(papId));
         papDir.delete();
 
         removeFromINIConfiguration(papAlias);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean exists(String papAlias) {
 
         return existsInINIConfiguration(papAlias);
     }
 
     /**
      * {@inheritDoc}
      */
     public Pap get(String papAlias) {
 
         Pap pap = getPapFromINIConfiguration(papAlias);
 
         if (pap == null) {
             throw new NotFoundException(String.format("Not found: papAlias=%s", papAlias));
         }
 
         return pap;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<String> getAliasList() {
         return new ArrayList<String>(Utils.getAliasSet(iniConfiguration, PAPS_STANZA));
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Pap> getAll() {
 
         Set<String> aliases = Utils.getAliasSet(iniConfiguration, PAPS_STANZA);
 
         List<Pap> papList = new ArrayList<Pap>(aliases.size());
 
         for (String alias : aliases) {
             papList.add(getPapFromINIConfiguration(alias));
         }
 
         return papList;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getVersion() {
         return iniConfiguration.getString(VERSION_KEY);
     }
 
     /**
      * {@inheritDoc}
      */
     public void store(Pap pap) {
 
         String papAlias = pap.getAlias();
 
         if (exists(papAlias)) {
             throw new AlreadyExistsException(String.format("Already exists: papAlias=%s", papAlias));
         }
 
         File directory = new File(getPapDirAbsolutePath(pap.getId()));
         if (!directory.mkdir())
             throw new RepositoryException(String.format("Cannot create directory for PAP: %s (id=%s) (dir=%s)",
                                                         papAlias,
                                                         pap.getId(),
                                                         directory));
         saveToINIConfiguration(pap);
     }
 
     /**
      * {@inheritDoc}
      */
     public void update(Pap pap) {
 
         String papAlias = pap.getAlias();
 
         if (!exists(papAlias))
             throw new NotFoundException(String.format("Not found: papAlias=%s", papAlias));
 
         saveToINIConfiguration(pap);
     }
 
     private void clearPapProperties(String papAlias) {
         iniConfiguration.clearProperty(dnKey(papAlias));
         iniConfiguration.clearProperty(hostnameKey(papAlias));
        iniConfiguration.clearProperty(enabledKey(papAlias));
         iniConfiguration.clearProperty(portKey(papAlias));
         iniConfiguration.clearProperty(pathKey(papAlias));
         iniConfiguration.clearProperty(protocolKey(papAlias));
         iniConfiguration.clearProperty(idKey(papAlias));
         iniConfiguration.clearProperty(visibilityPublicKey(papAlias));
         iniConfiguration.clearProperty(policyLastModificationTimeKey(papAlias));
         iniConfiguration.clearProperty(typeKey(papAlias));
     }
 
     private boolean existsInINIConfiguration(String papAlias) {
         return keyExists(idKey(papAlias));
     }
 
     private String getPapDirAbsolutePath(String papId) {
         return dbPath + File.separator + papId;
     }
 
     private Pap getPapFromINIConfiguration(String papAlias) {
 
         if (papAlias == null) {
             return null;
         }
 
         if (!existsInINIConfiguration(papAlias)) {
             return null;
         }
 
         String type = iniConfiguration.getString(typeKey(papAlias));
         String dn = iniConfiguration.getString(dnKey(papAlias));
         String host = iniConfiguration.getString(hostnameKey(papAlias));
         String port = iniConfiguration.getString(portKey(papAlias));
         String protocol = iniConfiguration.getString(protocolKey(papAlias));
         String path = iniConfiguration.getString(pathKey(papAlias));
         String id = iniConfiguration.getString(idKey(papAlias));
         boolean visibilityPublic = iniConfiguration.getBoolean(visibilityPublicKey(papAlias));
         boolean enabled = iniConfiguration.getBoolean(enabledKey(papAlias), false);
 
         Pap pap = new Pap(papAlias, Pap.isLocal(type), dn, host, port, path, protocol, visibilityPublic);
         pap.setId(id);
         pap.setEnabled(enabled);
 
         long policyLastModificationTime;
         try {
             policyLastModificationTime = Long.parseLong(iniConfiguration.getString(policyLastModificationTimeKey(papAlias)));
         } catch (NumberFormatException e) {
             policyLastModificationTime = 0;
         }
         pap.setPolicyLastModificationTime(policyLastModificationTime);
 
         return pap;
     }
 
     private boolean keyExists(String key) {
 
         if (key == null) {
             return false;
         }
 
         String value = iniConfiguration.getString(key);
 
         if (value == null) {
             return false;
         }
 
         if (value.length() == 0) {
             return false;
         }
 
         return true;
     }
 
     private void removeFromINIConfiguration(String papAlias) {
 
         clearPapProperties(papAlias);
 
         try {
             iniConfiguration.save();
         } catch (ConfigurationException e) {
             throw new RepositoryException(e);
         }
     }
 
     private void saveToINIConfiguration(Pap pap) throws RepositoryException {
 
         if (pap == null) {
             throw new RepositoryException("BUG: PAP is null");
         }
 
         clearPapProperties(pap.getAlias());
         setPapProperties(pap);
 
         try {
             iniConfiguration.save();
         } catch (ConfigurationException e) {
             throw new RepositoryException(e);
         }
     }
 
     private void setPapProperties(Pap pap) {
 
         String papAlias = pap.getAlias();
 
         iniConfiguration.setProperty(typeKey(papAlias), pap.getTypeAsString());
         iniConfiguration.setProperty(dnKey(papAlias), pap.getDn());
         iniConfiguration.setProperty(hostnameKey(papAlias), pap.getHostname());
         iniConfiguration.setProperty(portKey(papAlias), pap.getPort());
         iniConfiguration.setProperty(pathKey(papAlias), pap.getPath());
         iniConfiguration.setProperty(protocolKey(papAlias), pap.getProtocol());
         iniConfiguration.setProperty(idKey(papAlias), pap.getId());
         iniConfiguration.setProperty(visibilityPublicKey(papAlias), pap.isVisibilityPublic());
         iniConfiguration.setProperty(enabledKey(papAlias), pap.isEnabled());
         iniConfiguration.setProperty(policyLastModificationTimeKey(papAlias),
                                      pap.getPolicyLastModificationTimeInSecondsString());
     }
 }
