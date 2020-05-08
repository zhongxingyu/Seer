 /*
  * @(#)MissionAuthorizationMap.java
  *
  * Copyright 2010 Instituto Superior Tecnico
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
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Set;
 import java.util.TreeSet;
 
 import module.mission.domain.Mission;
 import module.mission.domain.MissionAuthorizationAccountabilityType;
 import module.mission.domain.MissionProcess;
 import module.mission.domain.MissionSystem;
 import module.mission.domain.MissionYear;
 import module.mission.domain.PersonMissionAuthorization;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.OrganizationalModel;
 import module.organization.domain.Party;
 import module.organization.domain.Unit;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.User;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public class MissionAuthorizationMap implements Serializable {
 
     private final int NUMBER_OF_LEVELS = 3;
 
     private final Unit[] levels = new Unit[NUMBER_OF_LEVELS];
     private final Unit[] levelsForUser = new Unit[NUMBER_OF_LEVELS];
     private final Set<PersonMissionAuthorization>[] personMissionAuthorizations = new Set[NUMBER_OF_LEVELS];
 
     private final User user = UserView.getCurrentUser();
 
     public MissionAuthorizationMap(final MissionYear missionYear) {
 	final MissionSystem missionSystem = MissionSystem.getInstance();
 	final OrganizationalModel organizationalModel = missionSystem.getOrganizationalModel();
 	if (organizationalModel == null) {
 	    return;
 	}
	final Set<Party> partiesSet = organizationalModel.getPartiesSet();
	findLevel(0, partiesSet);
 	if (levels[0] != null) {
 	    findLevel(1, levels[0].getChildren(organizationalModel.getAccountabilityTypesSet()));
 	    if (levels[1] != null) {
 		findLevel(2, levels[1].getChildren(organizationalModel.getAccountabilityTypesSet()));
 	    }
 	}
 	findPersonMissionAuthorizations();
     }
 
     private void findPersonMissionAuthorizations() {
 	for (int i = 0; i < levelsForUser.length; i++) {
 	    final Unit unit = levelsForUser[i];
 	    if (unit != null) {
 		personMissionAuthorizations[i] = new TreeSet<PersonMissionAuthorization>(PersonMissionAuthorization.COMPARATOR_BY_PROCESS_NUMBER);
 		for (final PersonMissionAuthorization personMissionAuthorization : unit.getPersonMissionAuthorizationSet()) {
 		    final Mission mission = personMissionAuthorization.getAssociatedMission();
 		    final MissionProcess missionProcess = mission.getMissionProcess();
 		    if (!personMissionAuthorization.hasAuthority()
 			    && !personMissionAuthorization.hasDelegatedAuthority()
 
 			    && (!personMissionAuthorization.hasPrevious()
 				    || (personMissionAuthorization.hasPrevious()
 			    && (personMissionAuthorization.getPrevious().hasAuthority()
 						    || personMissionAuthorization.getPrevious().hasDelegatedAuthority())))
 
                             && missionProcess.isApproved()
                             && missionProcess.canAuthoriseParticipantActivity()
                             && (!missionProcess.getMission().hasAnyFinancer() || 
 				(missionProcess.hasAllAllocatedFunds() && missionProcess.hasAllCommitmentNumbers()))
 
 			    && !personMissionAuthorization.getMissionProcess().isCanceled()
 			    && !personMissionAuthorization.isProcessTakenByOtherUser()
 			    && (personMissionAuthorization.getMissionProcess().hasAllAllocatedFunds()
 				    || !personMissionAuthorization.getMissionProcess().getMission().hasAnyFinancer())
 			    /**
 			    && personMissionAuthorization.isAvailableForAuthorization() */) {
 			personMissionAuthorizations[i].add(personMissionAuthorization);
 		    }
 		}
 	    }
 	}
     }
 
    private void findLevel(final int index, final Collection<Party> partiesSet) {
	for (final Party party : partiesSet) {
 	    if (party.isUnit()) {
 		final Unit unit = (Unit) party;
 		if (unit.getMissionSystemFromUnitWithResumedAuthorizations() != null) {
 		    boolean hasSomeResponsible = false;
 		    for (final Accountability accountability : party.getChildAccountabilitiesSet()) {
 			if (accountability.isActive(new LocalDate())) {
 			    final AccountabilityType accountabilityType = accountability.getAccountabilityType();
 			    if (isResponsibleAccountabilityType(accountabilityType)) {
 				hasSomeResponsible = true;
 				if (accountability.getChild() == user.getPerson()) {
 				    levelsForUser[index] = unit;
 				}
 			    }
 			}
 		    }
 		    if (hasSomeResponsible) {
 			levels[index] = unit;
 			return;
 		    }
 		}
 	    }
 	}
     }
 
     private boolean isResponsibleAccountabilityType(AccountabilityType accountabilityType) {
 	final MissionSystem missionSystem = MissionSystem.getInstance();
 	for (final MissionAuthorizationAccountabilityType missionAuthorizationAccountabilityType : missionSystem.getMissionAuthorizationAccountabilityTypesSet()) {
 	    if (missionAuthorizationAccountabilityType.getAccountabilityTypesSet().contains(accountabilityType)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public Unit[] getLevelsForUser() {
         return levelsForUser;
     }
 
     public Set<PersonMissionAuthorization>[] getPersonMissionAuthorizations() {
         return personMissionAuthorizations;
     }
 
     public boolean hasSomeUnit() {
 	for (int i = 0; i < levelsForUser.length; i++) {
 	    if (levelsForUser[i] != null) {
 		return true;
 	    }
 	}
 	return false;
     }
 
 }
