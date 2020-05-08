 /*
  * @(#)Accountability.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Organization Module for the MyOrg web application.
  *
  *   The Organization Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Organization Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Organization Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.organization.domain;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jvstm.cps.ConsistencyPredicate;
 import module.organization.domain.predicates.PartyPredicate.PartyByAccTypeAndDates;
 import module.organization.domain.util.OrganizationConsistencyException;
 import myorg.domain.MyOrg;
 import myorg.domain.exceptions.DomainException;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.services.Service;
 
 public class Accountability extends Accountability_Base {
 
     protected static final String LOCAL_DATE_FORMAT = "yyyy-MM-dd";
 
     public static final Comparator<Accountability> COMPARATOR_BY_PARENT_PARTY_NAMES = new Comparator<Accountability>() {
 
 	@Override
 	public int compare(final Accountability o1, final Accountability o2) {
 	    final int c = Party.COMPARATOR_BY_NAME.compare(o1.getParent(), o2.getParent());
 	    return c == 0 ? o1.getExternalId().compareTo(o2.getExternalId()) : c;
 	}
 
     };
 
     public static final Comparator<Accountability> COMPARATOR_BY_CHILD_PARTY_NAMES = new Comparator<Accountability>() {
 
 	@Override
 	public int compare(final Accountability o1, final Accountability o2) {
 	    final int c = Party.COMPARATOR_BY_NAME.compare(o1.getChild(), o2.getChild());
 	    return c == 0 ? o1.getExternalId().compareTo(o2.getExternalId()) : c;
 	}
 
     };
     
     public static final Comparator<Accountability> COMPARATOR_BY_CREATION_DATE_FALLBACK_TO_START_DATE = new Comparator<Accountability>() {
 
 	@Override
 	public int compare(final Accountability o1, final Accountability o2) {
 	    DateTime o1CreationDate = o1.getCreationDate();
 	    DateTime o2CreationDate = o2.getCreationDate();
 
 	    if (o1CreationDate == null) {
 		o1CreationDate = o1.getBeginDate().toDateTimeAtStartOfDay();
 	    }
 	    if (o2CreationDate == null) {
 		o2CreationDate = o2.getBeginDate().toDateTimeAtStartOfDay();
 	    }
 	    return o1CreationDate.compareTo(o2CreationDate);
 	};
 
     };
 
     protected Accountability() {
 	super();
 	setCreationDate(new DateTime());
 	setMyOrg(MyOrg.getInstance());
 	super.setBeginDate(new LocalDate());
 	setCreatorUser(myorg.applicationTier.Authenticate.UserView.getCurrentUser());
     }
 
     protected Accountability(final Party parent, final Party child, final AccountabilityType type, final LocalDate begin,
 	    final LocalDate end) {
 	this();
 
 	check(parent, "error.Accountability.invalid.parent");
 	check(child, "error.Accountability.invalid.child");
 	check(type, "error.Accountability.invalid.type");
 	check(begin, "error.Accountability.invalid.begin");
 	checkDates(parent, begin, end);
 
 	canCreate(parent, child, type);
 
 	setParent(parent);
 	setChild(child);
 	setAccountabilityType(type);
 	super.setBeginDate(begin);
 	super.setEndDate(end);
     }
 
     protected void checkDates(final Party parent, final LocalDate begin, final LocalDate end) {
 	if (begin != null && end != null && begin.isAfter(end)) {
 	    throw new DomainException("error.Accountability.begin.is.after.end");
 	}
 	checkBeginFromOldestParentAccountability(parent, begin);
     }
 
     private void checkBeginFromOldestParentAccountability(final Party parent, final LocalDate begin) throws DomainException {
 	Accountability oldest = null;
 	for (final Accountability accountability : parent.getParentAccountabilitiesSet()) {
 	    if (oldest == null || accountability.getBeginDate().isBefore(oldest.getBeginDate())) {
 		oldest = accountability;
 	    }
 	}
 
 	if (oldest != null && begin.isBefore(oldest.getBeginDate())) {
 	    final String[] args = new String[] { oldest.getChild().getPartyName().getContent(),
 		    oldest.getBeginDate().toString("dd/MM/yyyy") };
 	    throw new DomainException("error.Accountability.begin.starts.before.oldest.parent.begin", args);
 	}
     }
 
     protected void check(final Object obj, final String message) {
 	if (obj == null) {
 	    throw new DomainException(message);
 	}
     }
 
     protected void canCreate(final Party parent, final Party child, final AccountabilityType type) {
 	if (parent.equals(child)) {
 	    throw new DomainException("error.Accountability.parent.equals.child");
 	}
 	if (parent.ancestorsInclude(child, type)) {
 	    throw new DomainException("error.Accountability.parent.ancestors.include.child.with.type");
 	}
 	if (!type.isValid(parent, child)) {
 	    throw new DomainException("error.Accountability.type.doesnot.allow.parent.child");
 	}
     }
 
     public boolean isValid() {
 	return hasParent() && hasChild() && getAccountabilityType().isValid(getParent(), getChild());
     }
 
     @ConsistencyPredicate(OrganizationConsistencyException.class)
     protected boolean checkDateInterval() {
 	return hasBeginDate() && (!hasEndDate() || !getBeginDate().isAfter(getEndDate()));
     }
 
     public boolean isActive(final LocalDate date) {
	return contains(date);
     }
 
     public boolean isActiveNow() {
 	final LocalDate now = new LocalDate();
 	return isActive(now);
     }
 
     public boolean contains(final LocalDate date) {
 	return !getBeginDate().isAfter(date) && (!hasEndDate() || getEndDate().isAfter(date));
     }
 
     public boolean contains(final LocalDate begin, final LocalDate end) {
 	check(begin, "error.Accountability.intercepts.invalid.begin");
 	return (end == null || !getBeginDate().isAfter(end)) && (!hasEndDate() || !begin.isAfter(getEndDate()));
     }
 
     private boolean hasBeginDate() {
 	return getBeginDate() != null;
     }
 
     private boolean hasEndDate() {
 	return getEndDate() != null;
     }
 
     public boolean hasAccountabilityType(AccountabilityType type) {
 	return getAccountabilityType().equals(type);
     }
 
     public String getDetailsString() {
 	final StringBuilder stringBuilder = new StringBuilder();
 	stringBuilder.append(getAccountabilityType().getName().getContent());
 	stringBuilder.append(": ");
 	if (getBeginDate() != null) {
 	    stringBuilder.append(getBeginDate().toString(LOCAL_DATE_FORMAT));
 	}
 	stringBuilder.append(" - ");
 	if (getEndDate() != null) {
 	    stringBuilder.append(getEndDate().toString(LOCAL_DATE_FORMAT));
 	}
 	return stringBuilder.toString();
     }
 
     @Service
     /**
      * It doesn't actually delete the accountability as it actually marks it as an accountability history item
      */
     void delete() {
 	setInactive();
     }
 
     static Accountability create(final Party parent, final Party child, final AccountabilityType type, final LocalDate begin,
 	    final LocalDate end) {
 	//TODO Fenix-133: allow the access control to be done in a more dynamic way, see issue for more info
 	//	return parent.isAuthorizedToManage() ? new Accountability(parent, child, type, begin, end)
 	//		: new UnconfirmedAccountability(parent, child, type, begin, end);
 	return new Accountability(parent, child, type, begin, end);
     }
 
     @Override
     @Deprecated
     public void setBeginDate(LocalDate beginDate) {
 	throw new DomainException("should.not.use.this.method.use.editDates.instead");
     }
 
     @Override
     @Deprecated
     public void setEndDate(LocalDate endDate) {
 	throw new DomainException("should.not.use.this.method.use.editDates.instead");
     }
 
     /**
      * sets the dates of the current accountability to the ones given. It does
      * not mark this accountability as an historic one or creates new ones like
      * editDates does {@link #editDates(LocalDate, LocalDate)} * NOTE * this
      * should only be invoked by constructors that create new accountabilities
      * and does not check the dates as the constructors are responsible for
      * doing that
      * 
      * @param begin
      *            the new begin date one)
      * @param end
      *            the new end date one)
      */
     protected void createDates(final LocalDate begin, final LocalDate end) {
 	super.setEndDate(end);
 	super.setBeginDate(begin);
 
     }
 
     /**
      * Marks the current accountability as an historic one and creates a new one
      * based on the new dates
      * 
      * @param begin
      *            the new begin date
      * @param end
      *            the new end date
      */
     @Service
     public void editDates(final LocalDate begin, final LocalDate end) {
 	check(begin, "error.Accountability.invalid.begin");
 	checkDates(getParent(), begin, end);
 	new Accountability(getParent(), getChild(), getAccountabilityType(), begin, end);
 	setInactive();
     }
 
     /**
      * Removes its regular connections with its parties and assigns the other
      * connections to it
      */
     private void setInactive() {
 	setInactiveChild(getChild());
 	setInactiveParent(getInactiveParent());
 	setChild(null);
 	setParent(null);
 
     }
 
     public boolean intersects(final LocalDate begin, final LocalDate end) {
 	return !isAfter(getBeginDate(), end) && !isAfter(begin, getEndDate());
     }
 
     private static boolean isAfter(final LocalDate localDate1, final LocalDate localDate2) {
 	return localDate1 != null && localDate2 != null && localDate2.isBefore(localDate1);
     }
 
     public static SortedSet<Accountability> getActiveAndInactiveAccountabilities(List<AccountabilityType> accTypes,
 	    List<Party> parties, LocalDate startDate, LocalDate endDate) {
 	SortedSet<Accountability> accountabilities = new TreeSet<Accountability>(
 		Accountability.COMPARATOR_BY_CREATION_DATE_FALLBACK_TO_START_DATE);
 
 	//let's iterate through the parties
 	for (Party party : parties) {
 	    accountabilities.addAll(party.getAccountabilitiesAndHistoricItems(accTypes, startDate, endDate));
 	}
 	//if no parties have been specified, we will get all of the accountabilities!!
 	if (parties == null || parties.isEmpty()) {
 	    final PartyByAccTypeAndDates typeAndDates = new PartyByAccTypeAndDates(startDate, endDate, accTypes);
 	    for (final Accountability accountability : MyOrg.getInstance().getAccountabilities()) {
 		if (typeAndDates.eval(null, accountability)) {
 		    accountabilities.add(accountability);
 		}
 
 	    }
 	}
 
 	return accountabilities;
 
     }
 
 }
