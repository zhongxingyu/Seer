 package module.mission.domain;
 
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.mission.domain.util.MissionAuthorizationMap;
 import module.mission.domain.util.MissionPendingProcessCounter;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import module.workflow.domain.WorkflowProcess;
 import module.workflow.widgets.ProcessListWidget;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class MissionYear extends MissionYear_Base {
 
     static {
 	ProcessListWidget.register(new MissionPendingProcessCounter());
     }
 
     public static final Comparator<MissionYear> COMPARATOR_BY_YEAR = new Comparator<MissionYear>() {
 	@Override
 	public int compare(MissionYear o1, MissionYear o2) {
 	    final Integer year1 = o1.getYear();
 	    final Integer year2 = o2.getYear();
 	    return year1.compareTo(year2);
 	}
     };
 
     public static Integer getBiggestYearCounter() {
 	int biggestCounter = 0;
 	for (MissionYear year : MissionSystem.getInstance().getMissionYearSet()) {
 	    if (year.getCounter() > biggestCounter) {
 		biggestCounter = year.getCounter();
 	    }
 	}
 	return biggestCounter;
     }
 
     private MissionYear(final int year) {
 	super();
 	if (findMissionByYearAux(year) != null) {
 	    throw new Error("There can only be one! (MissionYear object for each year)");
 	}
 	setMissionSystem(MissionSystem.getInstance());
 	setYear(new Integer(year));
 	setCounter(Integer.valueOf(0));
     }
 
     private static MissionYear findMissionByYearAux(final int year) {
 	final MissionSystem missionSystem = MissionSystem.getInstance();
 	for (final MissionYear missionYear : missionSystem.getMissionYearSet()) {
 	    if (missionYear.getYear().intValue() == year) {
 		return missionYear;
 	    }
 	}
 	return null;
     }
 
     @Service
     public static MissionYear findMissionYear(final int year) {
 	final MissionYear missionYear = findMissionByYearAux(year);
 	return missionYear == null ? new MissionYear(year) : missionYear;
     }
 
     public Integer nextNumber() {
 	return getAndIncreaseNextNumber();
     }
 
     private Integer getAndIncreaseNextNumber() {
 	setCounter(getCounter().intValue() + 1);
 	return getCounter();
     }
 
     public static MissionYear getCurrentYear() {
 	final int year = new DateTime().getYear();
 	return findMissionYear(year);
     }
 
     private abstract class MissionProcessSearch {
 
 	abstract boolean shouldAdd(final MissionProcess missionProcess, final User user);
 
 	SortedSet<MissionProcess> search() {
 	    final User user = UserView.getCurrentUser();
 	    final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
 	    for (final MissionProcess missionProcess : getMissionProcessSet()) {
 		if (shouldAdd(missionProcess, user)) {
 		    result.add(missionProcess);
 		}
 	    }
 	    return result;
 	}
 
     }
 
     public SortedSet<MissionProcess> getPendingAproval() {
 	return new MissionProcessSearch() {
 	    @Override
 	    boolean shouldAdd(final MissionProcess missionProcess, final User user) {
 		return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
 			&& !missionProcess.isUnderConstruction() && !missionProcess.getIsCanceled()
 			&& missionProcess.isPendingApprovalBy(user);
 	    }
 	}.search();
     }
 
     public SortedSet<MissionProcess> getPendingAuthorization() {
 	return new MissionProcessSearch() {
 	    @Override
 	    boolean shouldAdd(final MissionProcess missionProcess, final User user) {
 		return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
 			&& missionProcess.isApproved() && !missionProcess.getIsCanceled()
 			&& ((missionProcess.isPendingParticipantAuthorisationBy(user)
					&& (!missionProcess.getMission().hasAnyFinancer() || missionProcess.hasAllAllocatedFunds()))
 				|| (//missionProcess.areAllParticipantsAuthorizedForPhaseOne()
 					missionProcess.areAllParticipantsAuthorized()
 					&& missionProcess.hasAllAllocatedFunds()
 					&& missionProcess.isPendingDirectAuthorizationBy(user)));
 	    }
 	}.search();
     }
 
     public SortedSet<MissionProcess> getPendingFundAllocation() {
 	try {
 	    return new MissionProcessSearch() {
 		@Override
 		boolean shouldAdd(final MissionProcess missionProcess, final User user) {
 		    return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
 			    && (isPendingFundAllocation(missionProcess, user) || isPendingFundUnAllocation(missionProcess, user));
 		}
 
 		private boolean isPendingFundAllocation(MissionProcess missionProcess, User user) {
 		    return missionProcess.isApproved()
 			    && !missionProcess.getIsCanceled()
 			    && (((!missionProcess.hasAnyProjectFinancer() || missionProcess.hasAllAllocatedProjectFunds())
 				    && !missionProcess.hasAllAllocatedFunds() && missionProcess.canAllocateFund()) || (!missionProcess
 				    .hasAllAllocatedProjectFunds() && missionProcess.canAllocateProjectFund()));
 		}
 
 		private boolean isPendingFundUnAllocation(final MissionProcess missionProcess, final User user) {
 		    return missionProcess.getIsCanceled().booleanValue()
 			    && ((missionProcess.hasAnyAllocatedFunds() && missionProcess.isAccountingEmployee(user
 				    .getExpenditurePerson())) || (missionProcess.hasAnyAllocatedProjectFunds())
 				    && missionProcess.isProjectAccountingEmployee(user.getExpenditurePerson()));
 		}
 	    }.search();
 	} catch (Throwable t) {
 	    t.printStackTrace();
 	    throw new Error(t);
 	}
     }
 
     public SortedSet<MissionProcess> getDirectPendingFundAllocation() {
 	try {
 	    return new MissionProcessSearch() {
 		@Override
 		boolean shouldAdd(final MissionProcess missionProcess, final User user) {
 		    return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
 			    && (isPendingFundAllocation(missionProcess, user) || isPendingFundUnAllocation(missionProcess, user));
 		}
 
 		private boolean isPendingFundAllocation(MissionProcess missionProcess, User user) {
 		    return missionProcess.isApproved()
 			    && !missionProcess.getIsCanceled()
 			    && (((!missionProcess.hasAnyProjectFinancer() || missionProcess.hasAllAllocatedProjectFunds())
 				    && !missionProcess.hasAllAllocatedFunds() && missionProcess.canAllocateFund())
 				|| (!missionProcess.hasAllAllocatedProjectFunds() && missionProcess.isDirectResponsibleForPendingProjectFundAllocation()));
 		}
 
 		private boolean isPendingFundUnAllocation(final MissionProcess missionProcess, final User user) {
 		    return missionProcess.getIsCanceled().booleanValue()
 			    && ((missionProcess.hasAnyAllocatedFunds() && missionProcess.isAccountingEmployee(user.getExpenditurePerson()))
 				    || (missionProcess.hasAnyAllocatedProjectFunds())
 				    && missionProcess.isDirectProjectAccountingEmployee(user.getExpenditurePerson()));
 		}
 	    }.search();
 	} catch (Throwable t) {
 	    t.printStackTrace();
 	    throw new Error(t);
 	}
     }
 
     public SortedSet<MissionProcess> getPendingProcessingPersonelInformation() {
 	return new MissionProcessSearch() {
 	    @Override
 	    boolean shouldAdd(final MissionProcess missionProcess, final User user) {
 		return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
 			&& (missionProcess.hasCurrentQueue() && missionProcess.getCurrentQueue().isCurrentUserAbleToAccessQueue()
 				&& (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized()) && missionProcess
 				.areAllParticipantsAuthorized()) || missionProcess.isReadyForMissionTermination(user)
 			|| (missionProcess.isTerminated() && !missionProcess.isArchived() && missionProcess.canArchiveMission());
 	    }
 	}.search();
     }
 
     public SortedSet<MissionProcess> getPendingDirectProcessingPersonelInformation() {
 	return new MissionProcessSearch() {
 	    @Override
 	    boolean shouldAdd(final MissionProcess missionProcess, final User user) {
 		return (!missionProcess.hasCurrentOwner() || missionProcess.isTakenByCurrentUser())
 			&& (missionProcess.hasCurrentQueue() && missionProcess.getCurrentQueue().isCurrentUserAbleToAccessQueue()
 				&& (missionProcess.isAuthorized() || missionProcess.hasNoItemsAndParticipantesAreAuthorized()) && missionProcess
 				.areAllParticipantsAuthorized()) || missionProcess.isReadyForMissionTermination(user)
 			|| (missionProcess.isTerminated() && !missionProcess.isArchived() && missionProcess.canArchiveMissionDirect());
 	    }
 	}.search();
     }
 
     public SortedSet<MissionProcess> getRequested() {
 	final User user = UserView.getCurrentUser();
 	final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
 	final Person person = user.getPerson();
 	if (person != null) {
 	    for (final Mission mission : person.getRequestedMissionsSet()) {
 		final MissionProcess missionProcess = mission.getMissionProcess();
 		if (missionProcess.getMissionYear() == this && !missionProcess.getIsCanceled() && !missionProcess.isArchived()) {
 		    result.add(missionProcess);
 		}
 	    }
 	}
 	return result;
     }
 
     private boolean hasValidAuthorization(final Set<Authorization> authorizations) {
 	for (final Authorization authorization : authorizations) {
 	    if (authorization.isValid()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private boolean isDirectlyResponsibleFor(final Set<Authorization> authorizations,
 	    final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit) {
 	final Set<Authorization> authorizationsFromUnit = unit.getAuthorizationsSet();
 	if (intersect(authorizations, authorizationsFromUnit)) {
 	    return true;
 	}
 	if (hasValidAuthorization(authorizationsFromUnit)) {
 	    return false;
 	}
 	final pt.ist.expenditureTrackingSystem.domain.organization.Unit parentUnit = unit.getParentUnit();
 	return parentUnit != null && isDirectlyResponsibleFor(authorizations, parentUnit);
     }
 
     private boolean intersect(final Set<Authorization> authorizations, final Set<Authorization> authorizationsFromUnit) {
 	for (final Authorization authorization : authorizationsFromUnit) {
 	    if (authorizations.contains(authorization)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private Set<Authorization> getAuthorizations(final User user) {
 	final Set<Authorization> authorizations = new HashSet<Authorization>();
 	for (final Authorization authorization : user.getExpenditurePerson().getAuthorizationsSet()) {
 	    if (authorization.isValid()) {
 		authorizations.add(authorization);
 	    }
 	}
 	return authorizations;
     }
 
     public SortedSet<MissionProcess> getAprovalResponsible() {
 	final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
 	final User user = UserView.getCurrentUser();
 	if (user.hasExpenditurePerson()) {
 	    final Set<Authorization> authorizations = getAuthorizations(user);
 	    for (final MissionProcess missionProcess : getMissionProcessSet()) {
 		if (!missionProcess.getIsCanceled() && !missionProcess.isArchived()) {
 		    final Mission mission = missionProcess.getMission();
 		    final Party missionResponsible = mission.getMissionResponsible();
 		    if (missionResponsible != null) {
 			if (missionResponsible.isPerson()) {
 			    if (missionResponsible == user.getPerson()) {
 				result.add(missionProcess);
 			    }
 			} else if (missionResponsible.isUnit() && !authorizations.isEmpty()) {
 			    final pt.ist.expenditureTrackingSystem.domain.organization.Unit unit = getExpenditureUnit(mission,
 				    (module.organization.domain.Unit) missionResponsible);
 			    if (unit != null && isDirectlyResponsibleFor(authorizations, unit)) {
 				result.add(missionProcess);
 			    }
 			}
 		    }
 		}
 	    }
 	}
 	return result;
     }
 
     private Unit getExpenditureUnit(final Mission mission, final module.organization.domain.Unit unit) {
 	return unit.hasExpenditureUnit() ? unit.getExpenditureUnit() : getExpenditureUnit(mission, unit.getParentUnits()
 		.iterator().next());
     }
 
     public SortedSet<MissionProcess> getParticipate() {
 	final User user = UserView.getCurrentUser();
 	final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
 	final Person person = user.getPerson();
 	if (person != null) {
 	    for (final Mission mission : person.getMissionsSet()) {
 		final MissionProcess missionProcess = mission.getMissionProcess();
 		if (missionProcess.getMissionYear() == this && !missionProcess.getIsCanceled() && !missionProcess.isArchived()) {
 		    result.add(missionProcess);
 		}
 	    }
 	}
 	return result;
     }
 
     public MissionAuthorizationMap getMissionAuthorizationMap() {
 	return new MissionAuthorizationMap(this);
     }
 
     public SortedSet<MissionProcess> getTaken() {
 	final User user = UserView.getCurrentUser();
 	final SortedSet<MissionProcess> result = new TreeSet<MissionProcess>(MissionProcess.COMPARATOR_BY_PROCESS_NUMBER);
 	for (final WorkflowProcess workflowProcess : user.getUserProcessesSet()) {
 	    if (workflowProcess instanceof MissionProcess) {
 		final MissionProcess missionProcess = (MissionProcess) workflowProcess;
 		if (missionProcess.getMissionYear() == this && !missionProcess.getIsCanceled()) {
 		    result.add(missionProcess);
 		}
 	    }
 	}
 	return result;
     }
 
 }
