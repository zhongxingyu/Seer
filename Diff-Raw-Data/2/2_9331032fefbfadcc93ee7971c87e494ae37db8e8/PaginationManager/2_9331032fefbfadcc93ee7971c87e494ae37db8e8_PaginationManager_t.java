 package org.eclipse.emf.emfstore.client.ui.views.historybrowserview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.RangeQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.util.HistoryQueryBuilder;
 
 /**
  * @author Aumann
  * 
  */
 public class PaginationManager {
 	/**
 	 * The version around which history queries are created. At initialization time this is the base version. It gets
 	 * change if the user clicks on 'show next x elements'
 	 */
 	private PrimaryVersionSpec currentCenterVersionShown;
 
 	private int aboveCenterCount, belowCenterCount;
 
 	private List<HistoryInfo> currentlyPresentedInfos = new ArrayList<HistoryInfo>();
 
 	private ProjectSpace projectSpace;
 
 	private boolean nextPage = false;
 
 	private boolean prevPage = false;
 
 	/**
 	 * Creates a new PaginationManager with given page range around the central version. The central version is
 	 * initialized to be the base version.
 	 * 
 	 * Note that the real number of versions shown might be smaller if there are not enough versions above (e.g. base
 	 * version == head version) or below the
 	 * center version (e.g. only x revisions yet, but below is larger).
 	 * 
 	 * @param projectSpace The project space to operate on.
 	 * @param aboveCenterCount The number of versions shown above the central version.
 	 * @param belowCenterCount The number of versions shown below the central version.
 	 */
 	public PaginationManager(ProjectSpace projectSpace, int aboveCenterCount, int belowCenterCount) {
 		this.aboveCenterCount = aboveCenterCount;
 		this.belowCenterCount = belowCenterCount;
 		this.projectSpace = projectSpace;
 	}
 
	// TODO: handle swapover correctly

 	/**
 	 * @return The history info objects to be displayed on the current page.
 	 * @throws EmfStoreException If an exception gets thrown contacting the server.
 	 */
 	public List<HistoryInfo> retrieveHistoryInfos() throws EmfStoreException {
 		PrimaryVersionSpec newCenterVersion;
 		int beforeCurrent = -1, afterCurrent = -1;
 		if ((prevPage || nextPage) && currentCenterVersionShown != null && !currentlyPresentedInfos.isEmpty()) {
 			for (int i = 0; i < currentlyPresentedInfos.size(); i++) {
 				if (currentlyPresentedInfos.get(i).getPrimerySpec().getIdentifier() == currentCenterVersionShown
 					.getIdentifier()) {
 					beforeCurrent = i;
 					break;
 				}
 			}
 			assert beforeCurrent != -1 : "The currently shown center version should be contained in the currently shown history infos, why has it vanished?";
 			afterCurrent = currentlyPresentedInfos.size() - beforeCurrent - 1; // 1 == currentCenter
 			if (prevPage && beforeCurrent >= aboveCenterCount) {
 				// there might be more versions, so swap page if there are
 				newCenterVersion = currentlyPresentedInfos.get(0).getPrimerySpec();
 			} else if (nextPage && afterCurrent >= belowCenterCount) {
 				newCenterVersion = currentlyPresentedInfos.get(currentlyPresentedInfos.size() - 1).getPrimerySpec();
 			} else {
 				newCenterVersion = currentCenterVersionShown;
 			}
 		} else {
 			newCenterVersion = currentCenterVersionShown;
 		}
 		HistoryQuery query = getQuery(newCenterVersion);
 		List<HistoryInfo> historyInfos = projectSpace.getHistoryInfo(query);
 
 		if (newCenterVersion != null && !currentCenterVersionShown.equals(newCenterVersion)) {
 			// set correct center version:
 			// 1) prev page: check if there are enough previous versions
 			// if not: set centerVersion further down to retrieve more lower versions
 			// 2) next page : similiar to prev page
 
 			int idOfCurrentVersionShown = currentlyPresentedInfos.get(beforeCurrent).getPrimerySpec().getIdentifier();
 			int olderVersions = 0, newerVersions = 0;
 			boolean reFoundCurrentVersion = false;
 			for (int i = 0; i < historyInfos.size(); i++) {
 				int idOfI = historyInfos.get(i).getPrimerySpec().getIdentifier();
 				if (idOfI > idOfCurrentVersionShown) {
 					++newerVersions;
 				} else if (idOfI < idOfCurrentVersionShown) {
 					++olderVersions;
 				} else if (idOfI == idOfCurrentVersionShown) {
 					assert !reFoundCurrentVersion : "Should not be in there twice.";
 					reFoundCurrentVersion = true;
 				}
 			}
 
 			if (prevPage) {
 				if (newerVersions < aboveCenterCount) {
 					// not enough versions
 					// go further down
 					newCenterVersion = historyInfos.get(Math.min(olderVersions, historyInfos.size() - 1))
 						.getPrimerySpec();
 					if (newCenterVersion.getIdentifier() != currentCenterVersionShown.getIdentifier()) {
 						currentCenterVersionShown = newCenterVersion;
 						query = getQuery(newCenterVersion); // reretrieve
 						historyInfos = projectSpace.getHistoryInfo(query);
 					}
 				}
 
 			} else {
 				assert nextPage;
 				if (olderVersions < belowCenterCount) {
 					int size = historyInfos.size();
 					newCenterVersion = historyInfos.get(Math.max(size - olderVersions, 0)).getPrimerySpec();
 					if (newCenterVersion.getIdentifier() != currentCenterVersionShown.getIdentifier()) {
 						query = getQuery(newCenterVersion); // reretrieve
 						historyInfos = projectSpace.getHistoryInfo(query);
 					}
 				}
 
 			}
 			currentCenterVersionShown = newCenterVersion;
 		}
 
 		currentlyPresentedInfos = historyInfos;
 		prevPage = nextPage = false;
 		return historyInfos;
 	}
 
 	private HistoryQuery getQuery(PrimaryVersionSpec centerVersion) {
 
 		boolean allVersions = true;
 		PrimaryVersionSpec version;
 		if (centerVersion != null) {
 			version = centerVersion;
 		} else {
 			version = projectSpace.getBaseVersion();
 			currentCenterVersionShown = version;
 		}
 		RangeQuery query = HistoryQueryBuilder.rangeQuery(version, aboveCenterCount, belowCenterCount, allVersions,
 			false, false, true);
 
 		return query;
 	}
 
 	/**
 	 * Swaps to the next page. Call {@link #retrieveHistoryInfos()} to retrieve the new page.
 	 */
 	public void nextPage() {
 		nextPage = true;
 	}
 
 	/**
 	 * Swaps to the previous page. Call {@link #retrieveHistoryInfos()} to retrieve the new page.
 	 */
 	public void previousPage() {
 		prevPage = true;
 	}
 
 }
