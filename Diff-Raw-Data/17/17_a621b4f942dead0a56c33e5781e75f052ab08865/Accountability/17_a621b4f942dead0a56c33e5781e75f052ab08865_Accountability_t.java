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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import module.organization.domain.predicates.PartyPredicate.PartyByAccTypeAndDates;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
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
 
 	    return (o1CreationDate.compareTo(o2CreationDate)) == 0 ? (o1.getExternalId().compareTo(o2.getExternalId()))
 		    : (o1CreationDate.compareTo(o2CreationDate));
 	};
 
     };
 
     protected Accountability() {
 	super();
 	setMyOrg(MyOrg.getInstance());
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
 
 	init(parent, child, type);
 	editDates(begin, end);
     }
 
     protected void init(Party parent, Party child, AccountabilityType type) {
 	super.setParent(parent);
 	super.setChild(child);
 	super.setAccountabilityType(type);
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
 
     public boolean isActive(final LocalDate date) {
 	return contains(date) && !isErased();
     }
 
     /**
      * 
      * @return true if the {@link AccountabilityHistory} item is inactive, false
      *         otherwise
      */
     public boolean isErased() {
 	if (getAccountabilityVersion() == null)
 	    return false;
 	return getAccountabilityVersion().getErased();
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
     public void delete() {
 	setInactive();
     }
 
     static Accountability create(final Party parent, final Party child, final AccountabilityType type, final LocalDate begin,
 	    final LocalDate end) {
 	// TODO Fenix-133: allow the access control to be done in a more dynamic
 	// way, see issue for more info
 	// return parent.isAuthorizedToManage() ? new Accountability(parent,
 	// child, type, begin, end)
 	// : new UnconfirmedAccountability(parent, child, type, begin, end);
 	return new Accountability(parent, child, type, begin, end);
     }
 
     @Override
     @Deprecated
     public void setParent(Party parent) {
 	throw new DomainException("should.not.use.this.method.delete.and.create.another.instead");
     }
 
     @Override
     @Deprecated
     public void setChild(Party child) {
 	throw new DomainException("should.not.use.this.method.delete.and.create.another.instead");
     }
 
     @Override
     @Deprecated
     public void setAccountabilityType(AccountabilityType accountabilityType) {
 	throw new DomainException("should.not.use.this.method.delete.and.create.another.instead");
     }
 
     public void setBeginDate(LocalDate beginDate) {
 	editDates(beginDate, getEndDate());
     }
 
     public void setEndDate(LocalDate endDate) {
 	editDates(getBeginDate(), endDate);
     }
 
     /**
      * Marks the current accountability as an historic one and creates a new one
      * based on the new dates
      * 
      * @param begin
      *            the new begin date
      * @param end
      *            the new end date
      * @return the new Accountability that was just created
      */
     @Service
     public void editDates(final LocalDate begin, final LocalDate end) {
 	check(begin, "error.Accountability.invalid.begin");
 	checkDates(getParent(), begin, end);
 	// let's create the new AccountabilityHistory which is active
 	AccountabilityVersion.insertAccountabilityVersion(begin, end, this, false);
 
     }
 
     /*
      * NOTE: this method returns the interval between the 00:00 of the StartDate
      * and 23:59:59 of the EndDate
      * 
      * @return the Interval between the begin and end date, or today if end date
      * is unspecified (null) public Interval getCurrentScope() { DateTime
      * endDate = (getEndDate() == null) ? new
      * LocalDate().plusDays(1).toDateTimeAtStartOfDay() : getEndDate().plusDays(
      * 1).toDateTimeAtStartOfDay(); DateTime beginDate =
      * getBeginDate().toDateTimeAtStartOfDay(); return new Interval(endDate,
      * beginDate); }
      */
 
     private void setInactive() {
 	AccountabilityVersion.insertAccountabilityVersion(getBeginDate(), getEndDate(), this, true);
 
     }
 
     public boolean intersects(final LocalDate begin, final LocalDate end) {
 	return !isAfter(getBeginDate(), end) && !isAfter(begin, getEndDate());
     }
 
     /**
      * 
      * @param localDate1
      * @param localDate2
      * @return false if any of the dates are null, or if localDate1 isn't after
      *         localDate2, true otherwise
      */
     private static boolean isAfter(final LocalDate localDate1, final LocalDate localDate2) {
 	return localDate1 != null && localDate2 != null && localDate2.isBefore(localDate1);
     }
 
     public static List<Accountability> getActiveAndInactiveAccountabilities(List<AccountabilityType> accTypes,
 	    List<Party> parties, LocalDate startDate, LocalDate endDate) {
 	List<Accountability> accountabilities = new ArrayList<Accountability>();
 
 	// let's iterate through the parties
 	for (Party party : parties) {
 	    accountabilities.addAll(party.getAccountabilitiesAndHistoricItems(accTypes, startDate, endDate));
 	}
 	// if no parties have been specified, we will get all of the
 	// accountabilities!!
 	if (parties == null || parties.isEmpty()) {
 	    final PartyByAccTypeAndDates typeAndDates = new PartyByAccTypeAndDates(startDate, endDate, accTypes);
 	    for (final Accountability accountability : MyOrg.getInstance().getAccountabilities()) {
 		if (typeAndDates.eval(null, accountability)) {
 		    accountabilities.add(accountability);
 		}
 
 	    }
 	}
 
 	Collections.sort(accountabilities, COMPARATOR_BY_CREATION_DATE_FALLBACK_TO_START_DATE);
 
 	return accountabilities;
 
     }
 
     public LocalDate getBeginDate() {
 	return getAccountabilityVersion().getBeginDate();
     }
 
     public LocalDate getEndDate() {
 	return getAccountabilityVersion().getEndDate();
     }
 
     public DateTime getCreationDate() {
 	return getAccountabilityVersion().getCreationDate();
     }
 
     public User getCreatorUser() {
 	return getAccountabilityVersion().getUserWhoCreated();
     }
 
 }
