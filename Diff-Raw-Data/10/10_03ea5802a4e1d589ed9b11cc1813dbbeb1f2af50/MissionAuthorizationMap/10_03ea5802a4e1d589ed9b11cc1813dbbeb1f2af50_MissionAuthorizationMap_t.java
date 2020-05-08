 package module.mission.domain.util;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import module.mission.domain.MissionAuthorizationAccountabilityType;
 import module.mission.domain.MissionSystem;
 import module.mission.domain.MissionYear;
 import module.mission.domain.PersonMissionAuthorization;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.OrganizationalModel;
 import module.organization.domain.Party;
 import module.organization.domain.Unit;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 
 import org.joda.time.LocalDate;
 
 public class MissionAuthorizationMap implements Serializable {
 
     private final int NUMBER_OF_LEVELS = 3;
 
     private final Unit[] levels = new Unit[NUMBER_OF_LEVELS];
     private final Unit[] levelsForUser = new Unit[NUMBER_OF_LEVELS];
     private final Set<PersonMissionAuthorization>[] personMissionAuthorizations = new Set[NUMBER_OF_LEVELS];
 
     private final User user = UserView.getCurrentUser();
 
     public MissionAuthorizationMap(final MissionYear missionYear) {
 	final MissionSystem missionSystem = MissionSystem.getInstance();
 	final OrganizationalModel organizationalModel = missionSystem.getOrganizationalModel();
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
 		personMissionAuthorizations[i] = new HashSet<PersonMissionAuthorization>();
 		for (final PersonMissionAuthorization personMissionAuthorization : unit.getPersonMissionAuthorizationSet()) {
 		    if (!personMissionAuthorization.hasAuthority()
 			    && !personMissionAuthorization.hasDelegatedAuthority()
 			    && personMissionAuthorization.hasPrevious()
 			    && (personMissionAuthorization.getPrevious().hasAuthority()
 				    || personMissionAuthorization.getPrevious().hasDelegatedAuthority())
 			    && !personMissionAuthorization.getMissionProcess().isCanceled()
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
