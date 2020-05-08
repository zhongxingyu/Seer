 package au.edu.uq.cmm.paul;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
import javax.persistence.CollectionTable;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.JoinColumn;
 import javax.persistence.NoResultException;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.GenericGenerator;
 
 /**
  * This class represents the configuration details of a DataGrabber instance.
  * The details are persisted using Hibernate / JPA.
  * 
  * @author scrawley
  */
 @Entity
 @Table(name = "CONFIGURATION")
 public class PaulConfiguration implements GrabberConfiguration {
     
     private Long id;
     
     private int proxyPort = 1024;
     private int serverPort = 1024;
     private String serverHost;
     private String proxyHost;
     private boolean allowUnknownClients;
     private Set<String> trustedAddresses = Collections.emptySet();
     private Set<InetAddress> trustedInetAddresses = Collections.emptySet();
     private String dummyFacilityName;
     private String dummyFacilityHostId;
     
     private boolean useProject;
     private String captureDirectory;
     private String archiveDirectory;
     private String baseFileUrl;
     private String feedId;
     private String feedTitle;
     private String feedAuthor;
     private String feedAuthorEmail;
     private String feedUrl;
     private int feedPageSize = 20;
     private long queueExpiryTime;
     private long queueExpiryInterval;
     private boolean expireByDeleting;
     private DataGrabberRestartPolicy dataGrabberRestartPolicy = 
             DataGrabberRestartPolicy.NO_AUTO_START;
     private boolean holdDatasetsWithNoUser = true;
     private String primaryRepositoryUrl;
     private String aclsUrl;
     
     public PaulConfiguration() {
         super();
     }
     
     public PaulConfiguration(StaticPaulConfiguration staticConfig) throws UnknownHostException {
         setProxyHost(staticConfig.getProxyHost());
         setServerHost(staticConfig.getServerHost());
         setProxyPort(staticConfig.getProxyPort());
         setServerPort(staticConfig.getServerPort());
         setAllowUnknownClients(staticConfig.isAllowUnknownClients());
         setTrustedAddresses(staticConfig.getTrustedAddresses());
         setDummyFacilityHostId(staticConfig.getDummyFacilityHostId());
         setDummyFacilityName(staticConfig.getDummyFacilityName());
         setBaseFileUrl(staticConfig.getBaseFileUrl());
         setCaptureDirectory(staticConfig.getCaptureDirectory());
         setArchiveDirectory(staticConfig.getArchiveDirectory());
         setFeedId(staticConfig.getFeedId());
         setFeedTitle(staticConfig.getFeedTitle());
         setFeedAuthor(staticConfig.getFeedAuthor());
         setFeedAuthorEmail(staticConfig.getFeedAuthorEmail());
         setFeedUrl(staticConfig.getFeedUrl());
         setFeedPageSize(staticConfig.getFeedPageSize());
         setQueueExpiryInterval(staticConfig.getQueueExpiryInterval());
         setQueueExpiryTime(staticConfig.getQueueExpiryTime());
         setExpireByDeleting(staticConfig.isExpireByDeleting());
         setDataGrabberRestartPolicy(staticConfig.getDataGrabberRestartPolicy());
         setHoldDatasetsWithNoUser(staticConfig.isHoldDatasetsWithNoUser());
         setPrimaryRepositoryUrl(staticConfig.getPrimaryRepositoryUrl());
         setAclsUrl(staticConfig.getAclsUrl());
     }
 
     public int getProxyPort() {
         return proxyPort;
     }
 
     public String getServerHost() {
         return serverHost;
     }
 
     public int getServerPort() {
         return serverPort;
     }
 
     public String getDummyFacilityName() {
         return dummyFacilityName;
     }
 
     public void setDummyFacilityName(String dummyFacilityName) {
         this.dummyFacilityName = dummyFacilityName;
     }
 
     public String getDummyFacilityHostId() {
         return dummyFacilityHostId;
     }
 
     public void setDummyFacilityHostId(String dummyFacilityHostId) {
         this.dummyFacilityHostId = dummyFacilityHostId;
     }
 
     public boolean isUseProject() {
         return useProject;
     }
 
     public void setProxyPort(int proxyPort) {
         this.proxyPort = proxyPort;
     }
 
     public void setServerHost(String serverHost) {
         this.serverHost = serverHost;
     }
 
     public void setServerPort(int serverPort) {
         this.serverPort = serverPort;
     }
 
     public void setUseProject(boolean useProject) {
         this.useProject = useProject;
     }
 
     public String getProxyHost() {
         return proxyHost;
     }
 
     public void setProxyHost(String proxyHost) {
         this.proxyHost = proxyHost;
     }
 
     public String getCaptureDirectory() {
         return captureDirectory;
     }
 
     public void setCaptureDirectory(String captureDirectory) {
         this.captureDirectory = captureDirectory;
     }
 
     public String getArchiveDirectory() {
         return archiveDirectory;
     }
 
     public void setArchiveDirectory(String archiveDirectory) {
         this.archiveDirectory = archiveDirectory;
     }
 
     public String getBaseFileUrl() {
         return baseFileUrl;
     }
 
     public void setBaseFileUrl(String baseFileUrl) {
         this.baseFileUrl = baseFileUrl;
     }
 
     public String getFeedId() {
         return feedId;
     }
 
     public void setFeedId(String feedId) {
         this.feedId = feedId;
     }
 
     public String getFeedTitle() {
         return feedTitle;
     }
 
     public void setFeedTitle(String feedTitle) {
         this.feedTitle = feedTitle;
     }
 
     public String getFeedAuthor() {
         return feedAuthor;
     }
 
     public void setFeedAuthor(String feedAuthor) {
         this.feedAuthor = feedAuthor;
     }
 
     public String getFeedAuthorEmail() {
         return feedAuthorEmail;
     }
 
     public void setFeedAuthorEmail(String feedAuthorEmail) {
         this.feedAuthorEmail = feedAuthorEmail;
     }
     
     public String getFeedUrl() {
         return feedUrl;
     }
 
     public void setFeedUrl(String feedUrl) {
         this.feedUrl = feedUrl;
     }
     
     public int getFeedPageSize() {
         return feedPageSize;
     }
 
     public void setFeedPageSize(int feedPageSize) {
         this.feedPageSize = feedPageSize;
     }
     
     public long getQueueExpiryTime() {
         return queueExpiryTime;
     }
 
     public void setQueueExpiryTime(long queueExpiryTime) {
         this.queueExpiryTime = queueExpiryTime;
     }
 
     public long getQueueExpiryInterval() {
         return queueExpiryInterval;
     }
 
     public void setQueueExpiryInterval(long queueExpiryInterval) {
         this.queueExpiryInterval = queueExpiryInterval;
     }
 
     public boolean isExpireByDeleting() {
         return expireByDeleting;
     }
 
     public void setExpireByDeleting(boolean expireByDeleting) {
         this.expireByDeleting = expireByDeleting;
     }
 
     public DataGrabberRestartPolicy getDataGrabberRestartPolicy() {
         return dataGrabberRestartPolicy;
     }
 
     public void setDataGrabberRestartPolicy(
             DataGrabberRestartPolicy dataGrabberRestartPolicy) {
         this.dataGrabberRestartPolicy = dataGrabberRestartPolicy;
     }
 
     public boolean isHoldDatasetsWithNoUser() {
         return holdDatasetsWithNoUser;
     }
 
     public void setHoldDatasetsWithNoUser(boolean holdDatasetsWithNoUser) {
         this.holdDatasetsWithNoUser = holdDatasetsWithNoUser;
     }
 
     public String getPrimaryRepositoryUrl() {
         return primaryRepositoryUrl;
     }
 
     public void setPrimaryRepositoryUrl(String primaryRepositoryUrl) {
         this.primaryRepositoryUrl = primaryRepositoryUrl;
     }
 
     public String getAclsUrl() {
         return aclsUrl;
     }
 
     public void setAclsUrl(String aclsUrl) {
         this.aclsUrl = aclsUrl;
     }
 
     public boolean isAllowUnknownClients() {
         return allowUnknownClients;
     }
 
     public void setAllowUnknownClients(boolean allowUnknownClients) {
         this.allowUnknownClients = allowUnknownClients;
     }
 
    @CollectionTable(name="trusted_addresses",joinColumns=@JoinColumn(name="addr_id"))
    @ElementCollection()
     public Set<String> getTrustedAddresses() {
         return trustedAddresses;
     }
 
     public void setTrustedAddresses(Set<String> trustedAddresses) 
             throws UnknownHostException {
         this.trustedAddresses = trustedAddresses;
         this.trustedInetAddresses = new HashSet<InetAddress>(trustedAddresses.size());
         for (String address : trustedAddresses) {
             trustedInetAddresses.add(InetAddress.getByName(address));
         }
     }
 
     @Transient
     public Set<InetAddress> getTrustedInetAddresses() {
         return trustedInetAddresses;
     }
     
     public static PaulConfiguration load(EntityManagerFactory entityManagerFactory) {
         return load(entityManagerFactory, false);
     }
 
     public static PaulConfiguration load(EntityManagerFactory entityManagerFactory,
             boolean createIfMissing) {
         EntityManager entityManager = entityManagerFactory.createEntityManager();
         try {
             entityManager.getTransaction().begin();
             try {
                 PaulConfiguration res = entityManager.
                     createQuery("from PaulConfiguration", PaulConfiguration.class).
                     getSingleResult();
                 return res;
             } catch (NoResultException ex) {
                 if (createIfMissing) {
                     PaulConfiguration res = new PaulConfiguration();
                     entityManager.persist(res);
                     entityManager.getTransaction().commit();
                     return res;
                 } else {
                     throw new PaulException("The configuration record is missing", ex);
                 }
             }
         } finally {
             entityManager.close();
         }
     }
 
     @Transient
     public boolean isEmpty() {
         return this.equals(new PaulConfiguration());
     }
 
     @Id
     @GeneratedValue(generator="increment")
     @GenericGenerator(name="increment", strategy = "increment")
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((aclsUrl == null) ? 0 : aclsUrl.hashCode());
         result = prime
                 * result
                 + ((archiveDirectory == null) ? 0 : archiveDirectory.hashCode());
         result = prime * result
                 + ((baseFileUrl == null) ? 0 : baseFileUrl.hashCode());
         result = prime
                 * result
                 + ((captureDirectory == null) ? 0 : captureDirectory.hashCode());
         result = prime
                 * result
                 + ((dataGrabberRestartPolicy == null) ? 0
                         : dataGrabberRestartPolicy.hashCode());
         result = prime
                 * result
                 + ((dummyFacilityHostId == null) ? 0 : dummyFacilityHostId
                         .hashCode());
         result = prime
                 * result
                 + ((dummyFacilityName == null) ? 0 : dummyFacilityName
                         .hashCode());
         result = prime * result + (expireByDeleting ? 1231 : 1237);
         result = prime * result
                 + ((feedAuthor == null) ? 0 : feedAuthor.hashCode());
         result = prime * result
                 + ((feedAuthorEmail == null) ? 0 : feedAuthorEmail.hashCode());
         result = prime * result + ((feedId == null) ? 0 : feedId.hashCode());
         result = prime * result + feedPageSize;
         result = prime * result
                 + ((feedTitle == null) ? 0 : feedTitle.hashCode());
         result = prime * result + ((feedUrl == null) ? 0 : feedUrl.hashCode());
         result = prime * result + (holdDatasetsWithNoUser ? 1231 : 1237);
         result = prime
                 * result
                 + ((primaryRepositoryUrl == null) ? 0 : primaryRepositoryUrl
                         .hashCode());
         result = prime * result
                 + ((proxyHost == null) ? 0 : proxyHost.hashCode());
         result = prime * result + proxyPort;
         result = prime * result
                 + (int) (queueExpiryInterval ^ (queueExpiryInterval >>> 32));
         result = prime * result
                 + (int) (queueExpiryTime ^ (queueExpiryTime >>> 32));
         result = prime * result
                 + ((serverHost == null) ? 0 : serverHost.hashCode());
         result = prime * result + serverPort;
         result = prime * result + (useProject ? 1231 : 1237);
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         PaulConfiguration other = (PaulConfiguration) obj;
         if (aclsUrl == null) {
             if (other.aclsUrl != null) {
                 return false;
             }
         } else if (!aclsUrl.equals(other.aclsUrl)) {
             return false;
         }
         if (archiveDirectory == null) {
             if (other.archiveDirectory != null) {
                 return false;
             }
         } else if (!archiveDirectory.equals(other.archiveDirectory)) {
             return false;
         }
         if (baseFileUrl == null) {
             if (other.baseFileUrl != null) {
                 return false;
             }
         } else if (!baseFileUrl.equals(other.baseFileUrl)) {
             return false;
         }
         if (captureDirectory == null) {
             if (other.captureDirectory != null) {
                 return false;
             }
         } else if (!captureDirectory.equals(other.captureDirectory)) {
             return false;
         }
         if (dataGrabberRestartPolicy != other.dataGrabberRestartPolicy) {
             return false;
         }
         if (dummyFacilityHostId == null) {
             if (other.dummyFacilityHostId != null) {
                 return false;
             }
         } else if (!dummyFacilityHostId.equals(other.dummyFacilityHostId)) {
             return false;
         }
         if (dummyFacilityName == null) {
             if (other.dummyFacilityName != null) {
                 return false;
             }
         } else if (!dummyFacilityName.equals(other.dummyFacilityName)) {
             return false;
         }
         if (expireByDeleting != other.expireByDeleting) {
             return false;
         }
         if (feedAuthor == null) {
             if (other.feedAuthor != null) {
                 return false;
             }
         } else if (!feedAuthor.equals(other.feedAuthor)) {
             return false;
         }
         if (feedAuthorEmail == null) {
             if (other.feedAuthorEmail != null) {
                 return false;
             }
         } else if (!feedAuthorEmail.equals(other.feedAuthorEmail)) {
             return false;
         }
         if (feedId == null) {
             if (other.feedId != null) {
                 return false;
             }
         } else if (!feedId.equals(other.feedId)) {
             return false;
         }
         if (feedPageSize != other.feedPageSize) {
             return false;
         }
         if (feedTitle == null) {
             if (other.feedTitle != null) {
                 return false;
             }
         } else if (!feedTitle.equals(other.feedTitle)) {
             return false;
         }
         if (feedUrl == null) {
             if (other.feedUrl != null) {
                 return false;
             }
         } else if (!feedUrl.equals(other.feedUrl)) {
             return false;
         }
         if (holdDatasetsWithNoUser != other.holdDatasetsWithNoUser) {
             return false;
         }
         if (primaryRepositoryUrl == null) {
             if (other.primaryRepositoryUrl != null) {
                 return false;
             }
         } else if (!primaryRepositoryUrl.equals(other.primaryRepositoryUrl)) {
             return false;
         }
         if (proxyHost == null) {
             if (other.proxyHost != null) {
                 return false;
             }
         } else if (!proxyHost.equals(other.proxyHost)) {
             return false;
         }
         if (proxyPort != other.proxyPort) {
             return false;
         }
         if (queueExpiryInterval != other.queueExpiryInterval) {
             return false;
         }
         if (queueExpiryTime != other.queueExpiryTime) {
             return false;
         }
         if (serverHost == null) {
             if (other.serverHost != null) {
                 return false;
             }
         } else if (!serverHost.equals(other.serverHost)) {
             return false;
         }
         if (serverPort != other.serverPort) {
             return false;
         }
         if (useProject != other.useProject) {
             return false;
         }
         return true;
     }
 }
