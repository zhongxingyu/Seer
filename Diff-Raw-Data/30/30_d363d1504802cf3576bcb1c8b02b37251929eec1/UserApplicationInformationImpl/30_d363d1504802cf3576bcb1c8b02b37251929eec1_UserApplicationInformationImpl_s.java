 package org.vpac.grisu.model.info;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.model.UserEnvironmentManager;
 import org.vpac.grisu.model.dto.DtoJob;
 
 import au.org.arcs.mds.GridResource;
 import au.org.arcs.mds.JobSubmissionProperty;
 
 /**
  * Implementation of {@link UserApplicationInformation}.
  * 
  * @author markus
  */
 public class UserApplicationInformationImpl extends ApplicationInformationImpl
 		implements UserApplicationInformation {
 
 	private Set<String> cachedSubmissionLocationsForUser = null;
 	private Set<String> cachedAllSitesForUser = null;
 	private Set<String> cachedAllVersionsForUser = null;
 	private UserEnvironmentManager userInfo = null;
 
 	public UserApplicationInformationImpl(final ServiceInterface serviceInterface,
 			final UserEnvironmentManager userInfo, final String application) {
 		super(serviceInterface, application);
 		this.userInfo = userInfo;
 	}
 
 	public final Set<String> getAllAvailableSubmissionLocationsForUser() {
 
 		if (cachedSubmissionLocationsForUser == null) {
 			cachedSubmissionLocationsForUser = new HashSet<String>();
 			for (String fqan : userInfo.getAllAvailableFqans()) {
 				cachedSubmissionLocationsForUser
 						.addAll(getAvailableSubmissionLocationsForFqan(fqan));
 			}
 		}
 		return cachedSubmissionLocationsForUser;
 	}
 
 	public final Set<String> getAllAvailableSitesForUser() {
 
 		if (cachedAllSitesForUser == null) {
 			cachedAllSitesForUser = new TreeSet<String>();
 			for (String subLoc : getAllAvailableSubmissionLocationsForUser()) {
 				cachedAllSitesForUser.add(getResourceInfo().getSite(subLoc));
 			}
 		}
 		return cachedAllSitesForUser;
 	}
 
 	public final Set<String> getAllAvailableVersionsForUser() {
 
 		if (cachedAllVersionsForUser == null) {
 			cachedAllVersionsForUser = new TreeSet<String>();
 			for (String fqan : userInfo.getAllAvailableFqans()) {
 				cachedAllVersionsForUser
 						.addAll(getAllAvailableVersionsForFqan(fqan));
 			}
 		}
 		return cachedAllVersionsForUser;
 	}
 
 	public final List<GridResource> getBestSubmissionLocations(
 			final Map<JobSubmissionProperty, String> additionalJobProperties,
 			final String fqan) {
 
 		Map<JobSubmissionProperty, String> basicJobProperties = new HashMap<JobSubmissionProperty, String>();
 		basicJobProperties.put(JobSubmissionProperty.APPLICATIONNAME,
 				getApplicationName());
 
 		basicJobProperties.putAll(additionalJobProperties);
 
 		Map<String, String> converterMap = new HashMap<String, String>();
 		for (JobSubmissionProperty key : basicJobProperties.keySet()) {
 			converterMap.put(key.toString(), basicJobProperties.get(key));
 		}
 
 		return getServiceInterface().findMatchingSubmissionLocationsUsingMap(DtoJob.createJob(JobConstants.UNDEFINED, converterMap),
				fqan).getGridResources();
 	}
 
 }
