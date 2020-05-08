 package fr.cg95.cvq.service.authority;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.springframework.core.io.Resource;
 
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Version;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 
 /**
  * Registry for registered local authorities.
  *
  * @author bor@zenexity.fr
  */
 public interface ILocalAuthorityRegistry {
 
     LocalAuthorityConfigurationBean getLocalAuthorityBeanByName(final String name);
 
     LocalAuthority getLocalAuthorityByName(final String name);
 
     /**
      * Retrieve the local authority which has registered this server name
      * or null if none have registered it
      */
     LocalAuthority getLocalAuthorityByServerName(final String serverName);
 
     /**
      * Add the server name to the ones the current local authority listens to,
      * and register it in the mapping
      */
     void addLocalAuthorityServerName(final String serverName);
 
     /**
      * Register this serverName for current local authority in the mapping
      */
     void registerLocalAuthorityServerName(final String serverName);
 
     /**
      * Remove and unregister this serverName for current local authority
      */
     void removeLocalAuthorityServerName(final String serverName);
 
     /**
      * Unregister this serverName for current local authority in the mapping
      */
     void unregisterLocalAuthorityServerName(final String serverName);
 
     /**
      * Return true if the server name isn't used or if it is used by current local authority.
      */
     boolean isAvailableLocalAuthorityServerName(final String serverName);
 
     /**
      * Replace existing serverNames with these new ones for current local authority,
      * and update mapping
      */
     void setLocalAuthorityServerNames(final TreeSet<String> serverNames)
         throws CvqException ;
 
    void saveLocalAuthority(LocalAuthority localAuthority);

     Set<String> getAllLocalAuthoritiesNames();
 
     /**
      * Return a common referential resource located in the referential repository.
      * 
      * @param type one of the types defined in LocalAuthorityResource
      * @param filename name of the file to retrieve
      */
     File getReferentialResource(final Type type, final String filename);
     
     /**
      * Get resource pointed to by filename, for the specified type,
      * for the current local authority.
      *
      * @param type one of the types defined in LocalAuthorityResource
      * @param filename file name of the resource to load
      * @param fallbackToDefault whether or not we fall back to default directory if resource is not
      *        in current local authority's directory
      */
     File getLocalAuthorityResourceFile(final Type type, final String filename,
         final boolean fallbackToDefault);
 
     /**
      * Same as {@link #getLocalAuthorityResourceFile(Type, String, boolean)} but for the given
      * local authority.
      * 
      * FIXME It is still necessary for the local referential service (but is should not)
      */
     File getLocalAuthorityResourceFileForLocalAuthority(final String localAuthorityName,
             final Type type, final String filename, final boolean fallbackToDefault);
 
     /**
      * Same as {@link #getLocalAuthorityResource(Type, String, boolean)} but
      * with file content returned in a string.
      */
     String getBufferedLocalAuthorityResource(final Type type,
         final String filename, final boolean fallbackToDefault);
 
     /**
      * Get the helps data for the given request type as a Map<v=stepName,k=helpDataAsString>.
      */
     Map<String,String> getBufferedCurrentLocalAuthorityRequestHelpMap(final String requestLabel);
 
     /**
      * Get the rules data for the given request type.
      */
     List<String> getLocalAuthorityRules(String requestTypeLabel);
 
     /**
      * Same as {@link #getLocalAuthorityResourceFile(String, LocalAuthorityResource.Version)}
      * for current version.
      */
     File getLocalAuthorityResourceFile(String id)
         throws CvqException;
 
     /**
      * Get the file for this resource id and version in the current local authority assets.
      */
     File getLocalAuthorityResourceFile(String id, Version version)
         throws CvqException;
 
     File getLocalAuthorityResourceFile(Type type, String filename, Version version, boolean fallbackToDefault) throws CvqException;
 
     File getRequestXmlResource(Long id);
     
     void registerLocalAuthorities(Resource[] localAuthoritiesFiles)
         throws CvqConfigurationException;
 
     /**
      * The same as {@link browseAndCallback} but for a specific local authority.
      *
      * @see browseAndCallback
      */
     void callback(String localAuthority, Object object, String callbackMethodName, 
             Object[] methodArgs);
 
     /**
      * Browse all known local authorities and invoke the given callback method for
      * each of them.
      *
      * This method handles all of the context tasks : setting of the security
      * context, transaction management, ... callback methods only have to deal
      * with their business tasks.
      *
      * @param object the instance object to which belongs the callback method
      * @param callbackMethodName the name of the callback method to be called
      * @param methodArgs optional String arguments to be passed to the callback method
      */
     void browseAndCallback(Object object, String callbackMethodName, Object[] methodArgs);
     
     /**
      * Generate a list of deployed local authorities in directory {@link #getAssetsBase()}.
      *
      * Filename is specified in the configuration of this service.
      * This method is called by a Quartz scheduled job and the generated list can be used
      * for various tasks, like databases dumps.
      */
     void generateLocalAuthoritiesList();
     
     String getReferentialBase();
     
     String getAssetsBase();
 
     void saveLocalAuthorityResource(Type type, String filename, byte[] data)
         throws CvqException;
 
     /**
      * Save the data as the current version of this local authority resource in current local authority assets.
      */
     void saveLocalAuthorityResource(String id, byte[] data)
         throws CvqException;
 
     /**
      * Move the file for this local authority resource from oldVersion to newVersion in current local authority assets.
      */
     void renameLocalAuthorityResource(String id, Version oldVersion, Version newVersion)
         throws CvqException;
 
     void renameLocalAuthorityResource(Type type, String oldFilename, String newFilename)
         throws CvqException;
 
     /**
      * Save the data as the current version of this local authority resource in current local authority assets,
      * and backup former current version as old version if it exists.
      */
     void replaceLocalAuthorityResource(String id, byte[] data)
         throws CvqException;
 
     /**
      * Switch current version and old version of this local authority resource in current local authority assets.
      */
     void rollbackLocalAuthorityResource(String id)
         throws CvqException;
 
     /**
      * Checks if this local authority resource has a file for this version in current local authority assets.
      */
     boolean hasLocalAuthorityResource(String id, Version version)
         throws CvqException;
 
     void removeLocalAuthorityResource(Type type, String filename);
 
     /**
      * Deletes all versions of this local authority resource in current local authority assets.
      */
     void removeLocalAuthorityResource(String id)
         throws CvqException;
 
     /**
      * Generate all JPEG images for PDF
      */
     void generateJPEGFiles();
 
     List<String> getLocalAuthorityResourceFileNames(Type type) throws CvqException;
     List<String> getLocalAuthorityResourceFileNames(Type type, String pattern) throws CvqException;
 
     /**
      * Indicates if payments are enabled or disabled for current local authority
      */
     boolean isPaymentEnabled();
 }
