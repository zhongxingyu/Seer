 package grisu.model.info;
 
 import grisu.control.ServiceInterface;
 import grisu.jcommons.constants.Constants;
 import grisu.model.info.dto.*;
 import grisu.model.info.dto.Queue;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URI;
 import java.util.*;
 
 /**
  * Implementation of {@link ResourceInformation}.
  *
  * @author markus
  */
 public class ResourceInformationImpl implements ResourceInformation {
 
     static final Logger myLogger = LoggerFactory
             .getLogger(ResourceInformation.class.getName());
 
     public static String getHost(final String urlOrSubmissionLocation) {
         String hostname = null;
 
         if (urlOrSubmissionLocation.contains("://")) {
 
             // int firstIndex = urlOrSubmissionLocation.indexOf("://")+3;
             // int lastIndex = urlOrSubmissionLocation.indexOf("/", firstIndex);
 
             // int firstIndex = 0;
             // int lastIndex = urlOrSubmissionLocation.length();
 
             URI address;
             try {
                 // dodgy, I know
                 address = new URI(urlOrSubmissionLocation);
                 // address = new
                 // URI(urlOrSubmissionLocation.substring(firstIndex,
                 // lastIndex));
             } catch (final Exception e) {
                 myLogger.error("Couldn't create url from: "
                         + urlOrSubmissionLocation);
                 throw new RuntimeException("Couldn't create url from: "
                         + urlOrSubmissionLocation);
             }
             if (address.getHost() == null) {
                 hostname = urlOrSubmissionLocation;
             } else {
                 hostname = address.getHost();
             }
         } else if (urlOrSubmissionLocation.contains(":")
                 && !urlOrSubmissionLocation.contains("/")) {
 
             int startIndex = urlOrSubmissionLocation.indexOf(":") + 1;
             if (startIndex == -1) {
                 startIndex = 0;
             }
             int endIndex = urlOrSubmissionLocation.indexOf("#");
             if (endIndex == -1) {
                 endIndex = urlOrSubmissionLocation.length();
             }
             hostname = urlOrSubmissionLocation.substring(startIndex, endIndex);
         } else {
             myLogger.error("Could not parse url or submissionLocation for String: "
                     + urlOrSubmissionLocation);
             // TODO throw exception maybe?
             return null;
         }
         return hostname;
     }
 
     private final ServiceInterface serviceInterface;
     private Queue[] cachedAllSubmissionLocations = null;
     private final Map<Site, Set<Queue>> cachedSiteAllSubmissionLocationsMap = new TreeMap<Site, Set<Queue>>();
     private Site[] cachedAllSites = null;
     private final Map<String, String> cachedHosts = new HashMap<String, String>();
     private final Map<String, Queue[]> cachedAllSubmissionLocationsPerFqan = new HashMap<String, Queue[]>();
     private final Map<String, Set<Site>> cachedAllSitesPerFqan = new HashMap<String, Set<Site>>();
     private final Map<String, String[]> cachedApplicationPackagesForExecutables = new HashMap<String, String[]>();
 
     private final Map<String, List<String>> cachedStagingFilesystemsPerSubLoc = new HashMap<String, List<String>>();
     private Application[] cachedAllApps;
     private final Map<String, Application[]> cachedAppsPerVO = new HashMap<String, Application[]>();
 
     public ResourceInformationImpl(final ServiceInterface serviceInterface) {
         this.serviceInterface = serviceInterface;
     }
 
     public final Set<String> distillSitesFromSubmissionLocations(
             final Set<String> submissionLocations) {
 
         final Set<String> temp = new TreeSet<String>();
         for (final String subLoc : submissionLocations) {
             String site = null;
             try {
                 site = getSite(subLoc);
                 temp.add(site);
             } catch (final Exception e) {
                 myLogger.error("Could not get site for submissionlocation: "
                         + subLoc + ", ignoring it. Error: "
                         + e.getLocalizedMessage());
             }
         }
         return temp;
     }
 
     public final Set<String> filterSubmissionLocationsForSite(
             final String site, final Set<String> submissionlocations) {
 
         final Set<String> temp = new TreeSet<String>();
         for (final String subLoc : submissionlocations) {
             if (site.equals(getSite(subLoc))) {
                 temp.add(subLoc);
             }
         }
         return temp;
     }
 
     public synchronized Application[] getAllApplications() {
 
         if (cachedAllApps == null) {
             cachedAllApps = serviceInterface.getAllAvailableApplications(null);
             Arrays.sort(cachedAllApps);
         }
         return cachedAllApps;
 
     }
 
     public Application[] getAllApplicationsForFqans(Set<String> fqans) {
 
         final SortedSet<Application> result = new TreeSet<Application>();
         for (final String fqan : fqans) {
 
             if (cachedAppsPerVO.get(fqan) == null) {
                 Application[] temp = serviceInterface
                         .getAllAvailableApplications(DtoStringList
                                 .fromSingleString(fqan));
 
                 if (temp == null) {
                     temp = new Application[]{};
                 }
                 cachedAppsPerVO.put(fqan, temp);
             }
             result.addAll(Arrays.asList(cachedAppsPerVO.get(fqan)));
         }
 
         return result.toArray(new Application[]{});
     }
 
     public final Set<Site> getAllAvailableSites(final String fqan) {
 
         synchronized (fqan) {
 
             if (cachedAllSitesPerFqan.get(fqan) == null) {
                 final Set<Site> temp = new TreeSet<Site>();
                 for (final Queue q : getAllAvailableSubmissionLocations(fqan)) {
                     temp.add(q.getGateway().getSite());
                 }
                 cachedAllSitesPerFqan.put(fqan, temp);
             }
         }
         return cachedAllSitesPerFqan.get(fqan);
     }
 
     public final Queue[] getAllAvailableSubmissionLocations(final String fqan) {
 
         synchronized (fqan) {
 
             if (cachedAllSubmissionLocationsPerFqan.get(fqan) == null) {
                 Queue[] temp = serviceInterface
                         .getAllSubmissionLocationsForFqan(fqan);
                 if (temp == null) {
                     temp = new Queue[]{};
                 }
                 myLogger.debug("Sublocs for {}: " + temp.length);
 
                 cachedAllSubmissionLocationsPerFqan.put(fqan, temp);
             }
         }
         return cachedAllSubmissionLocationsPerFqan.get(fqan);
     }
 
     public synchronized final Site[] getAllSites() {
 
         if (cachedAllSites == null) {
 
             cachedAllSites = serviceInterface.getAllSites();
         }
         return cachedAllSites;
     }
 
     public Queue getSubmissionLocation(String subLoc) {
         if (subLoc == null) {
             return null;
         }
         for (Queue q : getAllSubmissionLocations()) {
             if (q.toString().equalsIgnoreCase(subLoc)) {
                 return q;
             }
         }
         return null;
     }
 
     public synchronized final Queue[] getAllSubmissionLocations() {
 
         if (cachedAllSubmissionLocations == null) {
             cachedAllSubmissionLocations = serviceInterface
                     .getAllSubmissionLocations();
         }
         return cachedAllSubmissionLocations;
     }
 
     // public final Set<Queue> getAllSubmissionLocationsForSite(final String
     // site) {
     //
     // synchronized (site) {
     //
     // if (cachedSiteAllSubmissionLocationsMap.get(site) == null) {
     // // now we are building the complete map, not only for this one
     // // site
     // for (final Queue subLoc : getAllSubmissionLocations()) {
     // final Site sitetemp = subLoc.getGateway().getSite();
     // if (cachedSiteAllSubmissionLocationsMap.get(sitetemp) == null) {
     // cachedSiteAllSubmissionLocationsMap.put(sitetemp,
     // new HashSet<String>());
     // }
     // cachedSiteAllSubmissionLocationsMap.get(sitetemp).add(
     // subLoc);
     // }
     // }
     // }
     // return cachedSiteAllSubmissionLocationsMap.get(site);
     //
     // }
 
     // public String[] getApplicationPackageForExecutable(String executable) {
     //
     // synchronized (executable) {
     //
     // if (cachedApplicationPackagesForExecutables.get(executable) == null) {
     // final String[] result = serviceInterface
     // .getApplicationPackagesForExecutable(executable);
     // cachedApplicationPackagesForExecutables.put(executable, result);
     // }
     //
     // }
     //
     // return cachedApplicationPackagesForExecutables.get(executable);
     // }
 
     public final String getRecommendedStagingFileSystemForSubmissionLocation(
             final String subLoc) {
 
         final List<String> temp = getStagingFilesystemsForSubmissionLocation(subLoc);
         if ((temp != null) && (temp.size() > 0)) {
             return temp.get(0);
         } else {
             return null;
         }
     }
 
     public final String getSite(final String urlOrSubmissionLocation) {
 
         final String host = getHost(urlOrSubmissionLocation);
 
         synchronized (host) {
 
             if (cachedHosts.get(host) == null) {
                 cachedHosts.put(host, serviceInterface.getSite(host));
             }
         }
         return cachedHosts.get(host);
     }
 
     public final List<String> getStagingFilesystemsForSubmissionLocation(
             final String subLoc) {
 
         if ((subLoc == null)
                 || "".equals(subLoc)
                 || Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
                 .equals(subLoc)) {
             return null;
         }
 
         synchronized (subLoc) {
 
             if (cachedStagingFilesystemsPerSubLoc.get(subLoc) == null) {
                 final List<String> temp = serviceInterface
                         .getStagingFileSystemForSubmissionLocation(subLoc)
                         .getStringList();
                 cachedStagingFilesystemsPerSubLoc.put(subLoc, temp);
             }
         }
         return cachedStagingFilesystemsPerSubLoc.get(subLoc);
     }
 
     public boolean submissionLocationSupportsPrologEpilog(String subLoc) {
         Queue q = getSubmissionLocation(subLoc);
         if (q == null) {
             return false;
         } else {
             Middleware mw = q.getGateway().getMiddleware();
            if (mw.getOptions() == null ) {
                return false;
            }
             Object value = mw.getOptions().get(Middleware.PROLOG_EPILOG_AVAILABLE);
             if (value != null && value instanceof String) {
                 boolean available = Boolean.parseBoolean((String) value);
                 return available;
             } else {
                 return false;
             }
         }
     }
 
 }
