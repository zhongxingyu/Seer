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
 
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 
 public class ParticipantAuthorizationChain implements Serializable {
 
     public static class AuthorizationChain implements Serializable {
 	private Unit unit;
 	private AuthorizationChain next;
 
 	public AuthorizationChain(final Unit unit) {
 	    setUnit(unit);
 	}
 
 	public AuthorizationChain(final Unit unit, final AuthorizationChain next) {
 	    this(unit);
 	    setNext(next);
 	}
 
 	public Unit getUnit() {
 	    return unit;
 	}
 	public void setUnit(Unit unit) {
 	    this.unit = unit;
 	}
 	public AuthorizationChain getNext() {
 	    return next;
 	}
 	public void setNext(AuthorizationChain next) {
 	    this.next = next;
 	}
 	public AuthorizationChain last() {
 	    return next == null ? this : next.last();
 	}
 
 	public String exportAsString() {
 	    final StringBuilder stringBuilder = new StringBuilder();
 	    AuthorizationChain authorizationChain = this;
 	    while (authorizationChain != null) {
 		final Unit unit = authorizationChain.getUnit();
 		if (stringBuilder.length() > 0) {
 		    stringBuilder.append('_');
 		}
 		stringBuilder.append(unit.getExternalId());
 		authorizationChain = authorizationChain.next;
 	    }
 	    return stringBuilder.toString();
 	}
 
 	public String getExportString() {
 	    return exportAsString();
 	}
 
 	public static AuthorizationChain importFromString(final String string) {
 	    AuthorizationChain authorizationChain = null;
 	    for (final String externalUnitId : string.split("_")) {
 		final Unit unit = AbstractDomainObject.fromExternalId(externalUnitId);
 		AuthorizationChain next = new AuthorizationChain(unit);
 		if (authorizationChain == null) {
 		    authorizationChain = next;
 		} else {
 		    authorizationChain.last().setNext(next);
 		}
 	    }
 	    return authorizationChain;
 	}
 
 	public int getChainSize() {
 	    return next == null ? 1 : next.getChainSize() + 1;
 	}
     }
 
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
 
    public void setAuthorizationChain(AuthorizationChain authorizationChain) {
        this.authorizationChain = authorizationChain;
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
