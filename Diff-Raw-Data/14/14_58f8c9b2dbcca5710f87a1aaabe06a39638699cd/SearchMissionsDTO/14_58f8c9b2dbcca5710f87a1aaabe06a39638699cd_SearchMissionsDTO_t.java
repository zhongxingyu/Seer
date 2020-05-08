 /*
  * @(#)SearchMissionsDTO.java
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
 package module.mission.presentationTier.dto;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Formatter;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import module.mission.domain.Mission;
 import module.mission.domain.util.MissionStage;
 import module.mission.domain.util.SearchMissions;
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.collections.comparators.NullComparator;
 import org.apache.commons.collections.comparators.ReverseComparator;
 import org.apache.commons.lang.StringUtils;
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
 public class SearchMissionsDTO extends SearchMissions {
 
     private String sortBy;
 
     private String orderBy;
 
     public SearchMissionsDTO() {
 	super();
     }
 
     public SearchMissionsDTO(final HttpServletRequest request) {
 	setProcessNumber(StringUtils.isEmpty(request.getParameter("processNumber")) ? "" : request.getParameter("processNumber"));
 	setMissionResponsible(StringUtils.isEmpty(request.getParameter("ruOID")) ? null : (Party) Party.fromExternalId(request
 		.getParameter("ruOID")));
 	setPayingUnit(StringUtils.isEmpty(request.getParameter("puOID")) ? null : (Unit) Unit.fromExternalId(request
 		.getParameter("puOID")));
 	setForeign(StringUtils.isEmpty(request.getParameter("f")) ? null : Boolean.valueOf(request.getParameter("f")));
 	setDate(StringUtils.isEmpty(request.getParameter("d")) ? null : new LocalDate(Long.valueOf(request.getParameter("d"))));
 	setInterval(StringUtils.isEmpty(request.getParameter("i")) ? null : new Interval(request.getParameter("i")));
 	setRequestingPerson(StringUtils.isEmpty(request.getParameter("rpOID")) ? null : (Person) Person.fromExternalId(request
 		.getParameter("rpOID")));
 	setParticipant(StringUtils.isEmpty(request.getParameter("pOID")) ? null : (Person) Person.fromExternalId(request
 		.getParameter("pOID")));
 	setPendingStage(StringUtils.isEmpty(request.getParameter("ps")) ? null : MissionStage.valueOf(request.getParameter("ps")));
	setAccountingUnit(StringUtils.isEmpty(request.getParameter("auOID")) ? null : (AccountingUnit) AccountingUnit.fromExternalId(request.getParameter("auOID")));
 
 	final String sortByParameter = request.getParameter("sortBy");
 	if (!StringUtils.isEmpty(sortByParameter) && sortByParameter.indexOf('=') != -1) {
 	    final String[] splittedSort = sortByParameter.split("=");
 	    if (splittedSort.length > 1) {
 		this.sortBy = splittedSort[0];
 		this.orderBy = splittedSort[1];
 	    }
 	}
 
     }
 
     public List<Mission> sortedSearch() {
 	ArrayList<Mission> results = new ArrayList<Mission>(super.search());
 	Collections.sort(results, getComparator());
 	return results;
     }
 
     private Comparator<Mission> getComparator() {
 	if (!StringUtils.isEmpty(this.sortBy)) {
 	    Comparator<Mission> comparator = new BeanComparator(this.sortBy, new NullComparator());
 	    if (!StringUtils.isEmpty(this.orderBy) && this.orderBy.startsWith("desc")) {
 		return new ReverseComparator(comparator);
 	    }
 	    return comparator;
 	}
 	return Mission.COMPARATOR_BY_PROCESS_IDENTIFICATION;
     }
 
     public String getRequestParameters() {
	return new Formatter().format("processNumber=%s&ruOID=%s&puOID=%s&f=%s&d=%s&i=%s&rpOID=%s&pOID=%s&ps=%s&auOID=%s", getProcessNumber(),
 		getRequestingUnitParameter(), getPayingUnitParameter(), getForeignParameter(), getDateParameter(), getIntervalParameter(),
		getRequestingPersonParameter(), getParticipantParameter(), getPendingStageParameter(), getAccountingUnitParameter(),
 		getSortByParameter(), getOrderByParameter()).toString();
     }
 
     public String getRequestParametersWithSort() {
 	return getRequestParameters()
 		+ new Formatter().format("&sortBy=%s=%s", getSortByParameter(), getOrderByParameter()).toString();
     }
 
     private String getSortByParameter() {
 	return StringUtils.isEmpty(sortBy) ? StringUtils.EMPTY : sortBy;
     }
 
     private String getOrderByParameter() {
 	return StringUtils.isEmpty(orderBy) ? StringUtils.EMPTY : orderBy;
     }
 
     public String getRequestingUnitParameter() {
 	return getMissionResponsible() != null ? getMissionResponsible().getExternalId() : StringUtils.EMPTY;
     }
 
     public String getPayingUnitParameter() {
 	return getPayingUnit() != null ? getPayingUnit().getExternalId() : StringUtils.EMPTY;
     }
 
     public String getForeignParameter() {
 	return getForeign() != null ? getForeign().toString() : StringUtils.EMPTY;
     }
 
    private String getAccountingUnitParameter() {
	return getAccountingUnit() == null ? StringUtils.EMPTY : getAccountingUnit().getExternalId();
    }

     public String getDateParameter() {
 	return getDate() != null ? String.valueOf(getDate().toDateTimeAtStartOfDay().getMillis()) : StringUtils.EMPTY;
     }
 
     public String getIntervalParameter() {
 	return getInterval() != null ? String.valueOf(getInterval().toString()) : StringUtils.EMPTY;
     }
 
     public String getRequestingPersonParameter() {
 	return getRequestingPerson() != null ? getRequestingPerson().getExternalId() : StringUtils.EMPTY;
     }
 
     public String getParticipantParameter() {
 	return getParticipant() != null ? getParticipant().getExternalId() : StringUtils.EMPTY;
     }
 
     private String getPendingStageParameter() {
 	final MissionStage pendingStage = getPendingStage();
 	return pendingStage == null ? StringUtils.EMPTY : pendingStage.name();
     }
 
 }
