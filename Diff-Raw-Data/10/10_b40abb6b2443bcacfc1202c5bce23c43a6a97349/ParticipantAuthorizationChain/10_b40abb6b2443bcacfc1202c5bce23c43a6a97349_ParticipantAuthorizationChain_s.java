 package module.mission.domain.util;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Set;
 
 import module.mission.domain.MissionSystem;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.OrganizationalModel;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import module.organization.domain.Unit;
 import myorg.domain.User;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class ParticipantAuthorizationChain implements Serializable {
 
     private Person person;
     private AuthorizationChain authorizationChain;
 
     public ParticipantAuthorizationChain(final Person person, final AuthorizationChain authorizationChain) {
 	setPerson(person);
 	setAuthorizationChain(authorizationChain);
     }
 
     public Person getPerson() {
         return person;
     }
 
     public void setPerson(Person person) {
         this.person = person;
     }
 
     public AuthorizationChain getAuthorizationChain() {
         return authorizationChain;
     }
 
     public void setAuthorizationChain(final AuthorizationChain authorizationChain) {
         this.authorizationChain = removeSelfAuthorizationSteps(authorizationChain);
     }
 
     private AuthorizationChain removeSelfAuthorizationSteps(final AuthorizationChain authorizationChain) {
 	final AuthorizationChain next = authorizationChain.getNext();
 	if (next == null) {
 	    return authorizationChain;
 	}
 	if (canSelfAuthorize(authorizationChain.getUnit())) {
 	    return removeSelfAuthorizationSteps(next);
 	}
 	authorizationChain.setNext(removeSelfAuthorizationSteps(next));
 	return authorizationChain;
     }
 
     private boolean canSelfAuthorize(final Unit unit) {
 	final MissionSystem instance = MissionSystem.getInstance();
 	final Set<AccountabilityType> accountabilityTypes = instance.getAccountabilityTypesThatAuthorize();
 	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
 	    if (accountabilityTypes.contains(accountability.getAccountabilityType())
 		    && accountability.getChild() == person
 		    && accountability.isActiveNow()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private static Collection<AccountabilityType> getAccountabilityTypes() {
 	final OrganizationalModel organizationalModel = MissionSystem.getInstance().getOrganizationalModel();
 	return organizationalModel == null ? null : organizationalModel.getAccountabilityTypesSet();
     }
 
     public static Collection<ParticipantAuthorizationChain> getParticipantAuthorizationChains(final Person person) {
 	final Collection<Accountability> parentAccountabilities = person.getParentAccountabilities(MissionSystem.getInstance().getAccountabilityTypesRequireingAuthorization());
 	final Collection<ParticipantAuthorizationChain> participantAuthorizationChains = new ArrayList<ParticipantAuthorizationChain>();
 	for (final AuthorizationChain authorizationChain : getParticipantAuthorizationChains(parentAccountabilities)) {
 	    final ParticipantAuthorizationChain participantAuthorizationChain = new ParticipantAuthorizationChain(person, authorizationChain);
 	    participantAuthorizationChains.add(participantAuthorizationChain);
 	}
 	return participantAuthorizationChains;
     }
 
     public static Collection<ParticipantAuthorizationChain> getParticipantAuthorizationChains(final Person person, final Unit unit) {
 	final Collection<ParticipantAuthorizationChain> participantAuthorizationChains = new ArrayList<ParticipantAuthorizationChain>();
 
 	final OrganizationalModel organizationalModel = MissionSystem.getInstance().getOrganizationalModel();
 	final Set<AccountabilityType> accountabilityTypes = organizationalModel.getAccountabilityTypesSet();
 	final Set<AccountabilityType> accountabilityTypesThatAuthorize = MissionSystem.getInstance().getAccountabilityTypesThatAuthorize();
 	for (final Party party : organizationalModel.getPartiesSet()) {
 	    if (party.isUnit()) {
 		final Unit topLevelUnot = (Unit) party;
 		for (final Unit childUnit : topLevelUnot.getChildUnits(accountabilityTypes)) {
 		    final AuthorizationChain topLevelUnitChain = new AuthorizationChain(topLevelUnot);
 		    final AuthorizationChain childChain = new AuthorizationChain(childUnit, topLevelUnitChain);
 		    final AuthorizationChain authorizationChain = new AuthorizationChain(unit, childChain);
 
 		    final ParticipantAuthorizationChain participantAuthorizationChain = new ParticipantAuthorizationChain(person, authorizationChain);
 		    participantAuthorizationChains.add(participantAuthorizationChain);
 
 		    if (unit.getChildPersons(accountabilityTypesThatAuthorize).isEmpty()) {
 			createResponsibleForUnit(accountabilityTypesThatAuthorize, unit);
 		    }
 		}
 	    }
 	}
 
 	return participantAuthorizationChains;
     }
 
     @Service
     private static void createResponsibleForUnit(final Set<AccountabilityType> accountabilityTypesThatAuthorize, final Unit unit) {
 	for (final Authorization authorization : unit.getExpenditureUnit().getAuthorizationsSet()) {
 	    if (authorization.isValid()) {
 		final pt.ist.expenditureTrackingSystem.domain.organization.Person authority = authorization.getPerson();
 		final User user = authority.getUser();
 		if (user != null && user.hasPerson()) {
 		    for (final AccountabilityType accountabilityType : accountabilityTypesThatAuthorize) {
 			unit.addChild(user.getPerson(), accountabilityType, new LocalDate(), null);
 		    }
 		}
 	    }
 	}
     }
 
     public static ParticipantAuthorizationChain getMostLikelyParticipantAuthorizationChain(final Person person) {
 	final Collection<ParticipantAuthorizationChain> participantAuthorizationChains = ParticipantAuthorizationChain.getParticipantAuthorizationChains(person);
 	final int count = participantAuthorizationChains.size();
 	if (count > 0) {
 	    if (count == 1) {
 		final ParticipantAuthorizationChain participantAuthorizationChain = participantAuthorizationChains.iterator().next();
 		return participantAuthorizationChain;
 	    } else {
 		return Collections.max(participantAuthorizationChains, new Comparator<ParticipantAuthorizationChain>() {
 		    @Override
 		    public int compare(ParticipantAuthorizationChain o1, ParticipantAuthorizationChain o2) {
 			return o1.getChainSize() - o2.getChainSize();
 		    }});
 	    }
 	}
 	return null;
     }
 
     protected int getChainSize() {
 	return getAuthorizationChain().getChainSize();
     }
 
     private static Collection<AuthorizationChain> getParticipantAuthorizationChains(final Collection<Accountability> accountabilities) {
 	final Collection<AuthorizationChain> result = new ArrayList<AuthorizationChain>();
 	for (final Accountability accountability : accountabilities) {
	    final AccountabilityType accountabilityType = accountability.getAccountabilityType();
	    final Set<AccountabilityType> accountabilityTypes = MissionSystem.getInstance().getAccountabilityTypesForAuthorization(accountabilityType);
	    if (accountabilityTypes != null && !accountabilityTypes.isEmpty()) { 
		result.addAll(getParticipantAuthorizationChains(accountabilityTypes, accountability));
 	    }
 	}
 	return result;
     }
 
     public static Collection<AuthorizationChain> getParticipantAuthorizationChains(final Set<AccountabilityType> accountabilityTypes, final Accountability accountability) {
 	final Party party = accountability.getParent();
 	if (party.isUnit()) {
 	    if (hasPersonResponsible(accountabilityTypes, party)) {
 
 		final Unit unit = (Unit) party;
 		final Collection<Accountability> parentAccountabilities = unit.getParentAccountabilities(getAccountabilityTypes());
 
 		final Collection<AuthorizationChain> result = new ArrayList<AuthorizationChain>();
 		if (parentAccountabilities.isEmpty()) {
 		    final AuthorizationChain authorizationChain = new AuthorizationChain(unit);
 		    result.add(authorizationChain);
 		    return result;
 		} else {
 		    for (final Accountability parentAccountability : parentAccountabilities) {
 			for (final AuthorizationChain parentAuthorizationChain : getParticipantAuthorizationChains(accountabilityTypes, parentAccountability)) {
 			    final AuthorizationChain authorizationChain = new AuthorizationChain(unit, parentAuthorizationChain);
 			    result.add(authorizationChain);	
 			}
 		    }
 		}
 		return result;
 	    } else {
 		final Unit unit = (Unit) party;
 		final Collection<Accountability> parentAccountabilities = unit.getParentAccountabilities(getAccountabilityTypes());
 
 		final Collection<AuthorizationChain> result = new ArrayList<AuthorizationChain>();
 		for (final Accountability parentAccountability : parentAccountabilities) {
 		    result.addAll(getParticipantAuthorizationChains(accountabilityTypes, parentAccountability));
 		}
 		return result;
 	    }
     	}
     	return Collections.emptyList();
     }
 
     private static boolean hasPersonResponsible(final Set<AccountabilityType> accountabilityTypes, final Party party) {
 	for (final Accountability accountability : party.getChildAccountabilitiesSet()) {
 	    if (accountabilityTypes.contains(accountability.getAccountabilityType()) && accountability.isActive(new LocalDate())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
 }
