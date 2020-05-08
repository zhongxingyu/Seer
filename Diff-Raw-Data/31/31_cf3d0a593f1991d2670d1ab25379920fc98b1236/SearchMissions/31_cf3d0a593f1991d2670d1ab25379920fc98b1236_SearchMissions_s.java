 /*
  * @(#)SearchMissions.java
  *
  * Copyright 2011 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.mission.domain.util;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import module.mission.domain.ForeignMission;
 import module.mission.domain.Mission;
 import module.mission.domain.MissionFinancer;
 import module.mission.domain.MissionProcess;
 import module.mission.domain.MissionSystem;
 import module.mission.domain.MissionYear;
 import module.mission.domain.NationalMission;
 import module.mission.domain.PersonMissionAuthorization;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
import pt.ist.bennu.core.domain.util.Search;
 
 import org.joda.time.Interval;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 /**
  * 
  * @author João Neves
  * @author Luis Cruz
  * 
  */
 public class SearchMissions extends Search<Mission> {
 
     private static class MissionProcessMissionSet implements Set<Mission> {
 
 	private final Set<MissionProcess> missionProcesses;
 
 	private MissionProcessMissionSet(final Set<MissionProcess> missionProcesses) {
 	    this.missionProcesses = missionProcesses;
 	}
 
 	@Override
 	public Iterator<Mission> iterator() {
 	    return new Iterator<Mission>() {
 
 		private final Iterator<MissionProcess> iterator = missionProcesses.iterator();
 
 		@Override
 		public boolean hasNext() {
 		    return iterator.hasNext();
 		}
 
 		@Override
 		public Mission next() {
 		    return iterator.next().getMission();
 		}
 
 		@Override
 		public void remove() {
 		    throw new IllegalAccessError();
 		}
 	    };
 	}
 
 	@Override
 	public boolean add(final Mission e) {
 	    throw new IllegalAccessError();
 	}
 
 	@Override
 	public boolean addAll(Collection<? extends Mission> c) {
 	    throw new IllegalAccessError();
 	}
 
 	@Override
 	public void clear() {
 	    throw new IllegalAccessError();
 	}
 
 	@Override
 	public boolean contains(Object o) {
 	    throw new Error("not.implemented");
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 	    throw new Error("not.implemented");
 	}
 
 	@Override
 	public boolean isEmpty() {
 	    return missionProcesses.isEmpty();
 	}
 
 	@Override
 	public boolean remove(Object o) {
 	    throw new IllegalAccessError();
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 	    throw new IllegalAccessError();
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 	    throw new IllegalAccessError();
 	}
 
 	@Override
 	public int size() {
 	    return missionProcesses.size();
 	}
 
 	@Override
 	public Object[] toArray() {
 	    throw new Error("not.implemented");
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 	    throw new Error("not.implemented");
 	}
 
     }
 
     private String processNumber = Integer.toString(Calendar.getInstance().get(Calendar.YEAR)) + '/';
     private Party missionResponsible;
     private Unit payingUnit;
     private AccountingUnit accountingUnit;
     private Boolean national = Boolean.TRUE;
     private Boolean foreign = Boolean.TRUE;
     private LocalDate date;
     private Interval interval;
 
     private Person requestingPerson;
     private Person participant;
     private Person accountManager;
     private Boolean filterCanceledProcesses = Boolean.TRUE;
     private MissionStage pendingStage;
     private Person participantAuthorizationAuthority;
 
     @Override
     public Set<Mission> search() {
 	final Set<Mission> missions = limitSearchSet();
 	return new SearchResult(missions);
     }
 
     private Set<Mission> limitSearchSet() {
 	if (missionResponsible != null) {
 	    missionResponsible.getMissionsFromResponsibleSet();
 	}
 	if (requestingPerson != null) {
 	    return requestingPerson.getRequestedMissionsSet();
 	}
 	if (participant != null) {
 	    return participant.getMissionsSet();
 	}
 	if (participantAuthorizationAuthority != null) {
 	    return getMissionsFromParticipantAuthorizationAuthority(participantAuthorizationAuthority);
 	}
 	if (processNumber != null && !processNumber.isEmpty()) {
	    final int i = processNumber.indexOf('/');
	    if (i > 0) {
		final String year = processNumber.substring(0, i);
 		final MissionYear missionYear = MissionYear.findMissionYear(Integer.parseInt(year));
 		if (missionYear != null) {
 		    return new MissionProcessMissionSet(missionYear.getMissionProcessSet());
 		}
 	    }
 	}
 	return MissionSystem.getInstance().getMissionsSet();
     }
 
     private Set<Mission> getMissionsFromParticipantAuthorizationAuthority(final Person person) {
 	final Set<Mission> result = new HashSet<Mission>();
 	for (final PersonMissionAuthorization authorization : person.getPersonMissionAuthorizationFromAuthoritySet()) {
 	    final Mission mission = authorization.getAssociatedMission();
 	    result.add(mission);
 	}
 	return result;
     }
 
     protected class SearchResult extends SearchResultSet<Mission> {
 
 	public SearchResult(Collection<? extends Mission> c) {
 	    super(c);
 	}
 
 	@Override
 	protected boolean matchesSearchCriteria(Mission mission) {
 	    return matchProcessNumberCriteria(mission)
 		    && matchRequestingUnitCriteria(mission.getMissionResponsible())
 		    && matchPayingUnitCriteria(mission.getMissionVersion().getFinancer())
 		    && matchAccountingUnitCriteria(mission.getMissionVersion().getFinancer())
 		    && matchMissionTypeCriteria(mission)
 		    && matchDateCriteria(mission)
 		    && matchIntervalCriteria(mission)
 		    && matchRequestingPersonCriteria(mission.getRequestingPerson())
 		    && matchParticipantCriteria(mission)
 		    && matchParticipantAuthorizationAuthorityCriteria(mission)
 		    && matchAccountManagerCriteria(mission)
 		    && matchCanceledCriteria(mission)
 		    && mission.getMissionProcess().isAccessibleToCurrentUser()
 		    && matchStage(mission);
 	}
 
 	private boolean matchCanceledCriteria(final Mission mission) {
 	    return filterCanceledProcesses == null || !filterCanceledProcesses.booleanValue()
 		    || !mission.getMissionProcess().isProcessCanceled();
 	}
 
 	private boolean matchProcessNumberCriteria(final Mission mission) {
 	    return processNumber == null || processNumber.isEmpty()
 		    || mission.getMissionProcess().getProcessIdentification().indexOf(processNumber) >= 0;
 	}
 
 	private boolean matchParticipantCriteria(final Mission mission) {
 	    return participant == null || mission.getParticipantesSet().contains(participant);
 	}
 
 	private boolean matchParticipantAuthorizationAuthorityCriteria(final Mission mission) {
 	    if (participantAuthorizationAuthority == null) {
 		return true;
 	    }
 	    for (final PersonMissionAuthorization authorization : participantAuthorizationAuthority.getPersonMissionAuthorizationFromAuthoritySet()) {
 		if (mission == authorization.getAssociatedMission()) {
 		    return true;
 		}
 	    }
 	    return false;
 	}
 
 	private boolean matchRequestingPersonCriteria(Person rp) {
 	    return requestingPerson == null || requestingPerson == rp;
 	}
 
 	private boolean matchAccountManagerCriteria(final Mission mission) {
 	    return accountManager == null || mission.containsAccountManager(accountManager);
 	}
 
 	private boolean matchDateCriteria(final Mission mission) {
 	    return date == null
 		    || (mission.getDaparture().isBefore(date.plusDays(1).toDateTimeAtStartOfDay()) && mission.getArrival()
 			    .isAfter(date.toDateTimeAtStartOfDay()));
 	}
 
 	private boolean matchIntervalCriteria(final Mission mission) {
 	    return interval == null
 		    || interval.contains(mission.getDaparture())
 		    || interval.contains(mission.getArrival());
 	}
 
 	private boolean matchStage(final Mission mission) {
 	    if (pendingStage == null) {
 		return true;
 	    }
 	    final MissionStageView missionStageView = new MissionStageView(mission.getMissionProcess());
 	    final MissionStageState missionStageState = missionStageView.getMissionStageStates().get(pendingStage);
 	    return missionStageState == MissionStageState.UNDER_WAY;
 	}
 
 	private boolean matchMissionTypeCriteria(Mission mission) {
 	    return (national != null && national.booleanValue() && mission instanceof NationalMission)
 		    || (foreign != null && foreign.booleanValue() && mission instanceof ForeignMission);
 	}
 
 	private boolean matchPayingUnitCriteria(List<MissionFinancer> financers) {
 	    if (payingUnit == null) {
 		return true;
 	    }
 
 	    for (final MissionFinancer missionFinancer : financers) {
 		if (missionFinancer.getUnit() == payingUnit) {
 		    return true;
 		}
 	    }
 
 	    return false;
 	}
 
 	private boolean matchAccountingUnitCriteria(List<MissionFinancer> financers) {
 	    if (getAccountingUnit() == null) {
 		return true;
 	    }
 
 	    for (final MissionFinancer missionFinancer : financers) {
 		if (missionFinancer.getAccountingUnit() == getAccountingUnit()) {
 		    return true;
 		}
 	    }
 
 	    return false;
 	}
 
 	private boolean matchRequestingUnitCriteria(final Party mr) {
 	    return missionResponsible == null || missionResponsible == mr;
 	}
 
     }
 
     public Unit getPayingUnit() {
 	return payingUnit;
     }
 
     public void setPayingUnit(Unit payingUnit) {
 	this.payingUnit = payingUnit;
     }
 
     public Boolean getForeign() {
 	return foreign;
     }
 
     public void setForeign(Boolean foreign) {
 	this.foreign = foreign;
     }
 
     public LocalDate getDate() {
 	return date;
     }
 
     public void setDate(LocalDate date) {
 	this.date = date;
     }
 
     public Person getRequestingPerson() {
 	return requestingPerson;
     }
 
     public void setRequestingPerson(Person requestingPerson) {
 	this.requestingPerson = requestingPerson;
     }
 
     public Person getParticipant() {
 	return participant;
     }
 
     public void setParticipant(Person participant) {
 	this.participant = participant;
     }
 
     public String getProcessNumber() {
 	return processNumber;
     }
 
     public void setProcessNumber(String processNumber) {
 	this.processNumber = processNumber;
     }
 
     public Boolean getNational() {
 	return national;
     }
 
     public void setNational(Boolean national) {
 	this.national = national;
     }
 
     public Boolean getFilterCanceledProcesses() {
 	return filterCanceledProcesses;
     }
 
     public void setFilterCanceledProcesses(Boolean filterCanceledProcesses) {
 	this.filterCanceledProcesses = filterCanceledProcesses;
     }
 
     public Party getMissionResponsible() {
 	return missionResponsible;
     }
 
     public void setMissionResponsible(Party missionResponsible) {
 	this.missionResponsible = missionResponsible;
     }
 
     public Person getAccountManager() {
 	return accountManager;
     }
 
     public void setAccountManager(Person accountManager) {
 	this.accountManager = accountManager;
     }
 
     public void setAccountingUnit(AccountingUnit accountingUnit) {
 	this.accountingUnit = accountingUnit;
     }
 
     public AccountingUnit getAccountingUnit() {
 	return accountingUnit;
     }
 
     public Interval getInterval() {
         return interval;
     }
 
     public void setInterval(Interval interval) {
         this.interval = interval;
     }
 
     public MissionStage getPendingStage() {
         return pendingStage;
     }
 
     public void setPendingStage(MissionStage pendingStage) {
         this.pendingStage = pendingStage;
     }
 
     public Person getParticipantAuthorizationAuthority() {
         return participantAuthorizationAuthority;
     }
 
     public void setParticipantAuthorizationAuthority(Person participantAuthorizationAuthority) {
         this.participantAuthorizationAuthority = participantAuthorizationAuthority;
     }
 
 }
