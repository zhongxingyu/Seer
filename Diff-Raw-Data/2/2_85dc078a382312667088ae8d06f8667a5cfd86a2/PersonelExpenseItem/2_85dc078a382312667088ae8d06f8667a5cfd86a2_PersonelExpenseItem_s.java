 package module.mission.domain;
 
 import module.organization.domain.Person;
 import myorg.domain.util.Money;
 import myorg.util.BundleUtil;
 
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 
 public abstract class PersonelExpenseItem extends PersonelExpenseItem_Base {
 
     private transient Mission missionForCreation;
 
     public PersonelExpenseItem() {
 	super();
     }
 
     @Override
     public String getItemDescription() {
 	return BundleUtil.getFormattedStringFromResourceBundle("resources/MissionResources",
 		"label.mission.items.personel.expense");
     }
 
     @Override
     public boolean isPersonelExpenseItem() {
         return true;
     }
 
     public Mission getMissionForCreation() {
         return missionForCreation;
     }
 
     public void setMissionForCreation(final Mission mission) {
 	missionForCreation = mission;
     }
 
     @Override
     public Money getPrevisionaryCosts() {
         return getValue();
     }
 
     public int calculateNumberOfDays() {
 	final LocalDate startDate = getStart().toLocalDate();
 	final LocalDate endDate = getEnd().toLocalDate().plusDays(1);
 	return Days.daysBetween(startDate, endDate).getDays();
     }
 
     @Override
     public void delete() {
 	removeDailyPersonelExpenseCategory();
         super.delete();
     }
 
     public double getWeightedExpenseFactor() {
 	return getExpenseFactor();
     }
 
     public double getExpenseFactor() {
 	double result = 0.0;
 	for (final Person person : getPeopleSet()) {
 	    result += getExpenseFactor(person);
 	}
 	return result;
     }
 
     public double getExpenseFactor(final Person person) {
 	double result = 0.0;
 	if (getPeopleSet().contains(person)) {
 	    final Mission mission = getMissionVersion().getMission();
 	    final PersonelExpenseItem firstPersonelExpenseItem = findFirstPersonelExpenseItemFor(mission, person);
 	    final PersonelExpenseItem lastPersonelExpenseItem = findLastPersonelExpenseItemFor(mission, person);
 	    int numberOfDays = calculateNumberOfDays();
 	    if (this == firstPersonelExpenseItem) {
 		numberOfDays--;
 		result += mission.getFirstDayPersonelDayExpensePercentage(this);
 	    }
	    if (this == lastPersonelExpenseItem) {
 		numberOfDays--;
 		result += mission.getLastDayPersonelDayExpensePercentage(this);
 	    }
 	    result += numberOfDays;
 	}
 	return result;
     }
 
     private static PersonelExpenseItem findFirstPersonelExpenseItemFor(final Mission mission, final Person person) {
 	PersonelExpenseItem result = null;
 	for (final MissionItem missionItem : mission.getMissionItemsSet()) {
 	    if (missionItem.isPersonelExpenseItem()) {
 		final PersonelExpenseItem personelExpenseItem = (PersonelExpenseItem) missionItem;
 		if (personelExpenseItem.getPeopleSet().contains(person)) {
 		    if (result == null || personelExpenseItem.getStart().isBefore(result.getStart())) {
 			result = personelExpenseItem;
 		    }
 		}
 	    }
 	}
 	return result;
     }
 
     private static PersonelExpenseItem findLastPersonelExpenseItemFor(final Mission mission, final Person person) {
 	PersonelExpenseItem result = null;
 	for (final MissionItem missionItem : mission.getMissionItemsSet()) {
 	    if (missionItem.isPersonelExpenseItem()) {
 		final PersonelExpenseItem personelExpenseItem = (PersonelExpenseItem) missionItem;
 		if (personelExpenseItem.getPeopleSet().contains(person)) {
 		    if (result == null || personelExpenseItem.getStart().isAfter(result.getStart())) {
 			result = personelExpenseItem;
 		    }
 		}
 	    }
 	}
 	return result;	
     }
 
     public static int calculateNumberOfFullPersonelExpenseDays(final Mission mission, final Person person) {
 	int result = 0;
 	for (final MissionItem missionItem : mission.getMissionItemsSet()) {
 	    if (missionItem instanceof PersonelExpenseItem && missionItem.getPeopleSet().contains(person)) {
 		final PersonelExpenseItem personelExpenseItem = (PersonelExpenseItem) missionItem;
 		result += personelExpenseItem.calculateNumberOfDays();
 	    }
 	}
 	return result;
     }
 
     @Override
     public boolean isConsistent() {
 	final Mission mission = getMissionVersion().getMission();
 	final DateTime departure = mission.getDaparture();
 	final DateTime arrival = mission.getArrival();
 	return getStart().isBefore(getEnd()) && !getStart().isBefore(departure) && !getEnd().isAfter(arrival) && super.isConsistent();
     }
 
     @Override
     public void hookAfterChanges() {
         super.hookAfterChanges();
         final Mission mission = getMissionVersion().getMission();
         for (final MissionItem missionItem : mission.getMissionItemsSet()) {
             if (missionItem != this && missionItem.isPersonelExpenseItem()) {
         	if (!missionItem.areAllCostsDistributed()) {
         	    missionItem.distributeCosts();
         	}
             }
         }
     }
 
     public int getNunberOfLunchesToDiscount(final Person person) {
 	int result = 0;
 	if (hasPeople(person)) {
 	    final Mission mission = getMissionVersion().getMission();
 	    final PersonelExpenseItem firstPersonelExpenseItem = findFirstPersonelExpenseItemFor(mission, person);
 	    final PersonelExpenseItem lastPersonelExpenseItem = findLastPersonelExpenseItemFor(mission, person);
 	    int numberOfDays = calculateNumberOfDays();
 	    if (this == firstPersonelExpenseItem) {
 		numberOfDays--;
 		result += mission.getNunberOfLunchesToDiscountOnFirstPersonelExpenseDay(this);
 	    }
 	    if (this == lastPersonelExpenseItem) {
 		numberOfDays--;
 		result += mission.getNunberOfLunchesToDiscountOnLastPersonelExpenseDay(this);
 	    }
 	    result += numberOfDays;
 	}
 	return result;
     }
 
     @Override
     public boolean requiresFundAllocation() {
         return false;
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItem missionItem) {
 	final PersonelExpenseItem personelExpenseItem = (PersonelExpenseItem) missionItem;
 	personelExpenseItem.setStart(getStart());
 	personelExpenseItem.setEnd(getEnd());
 	personelExpenseItem.setDailyPersonelExpenseCategory(getDailyPersonelExpenseCategory());
     }
 
     @Override
     public boolean isAvailableForEdit() {
 	final MissionVersion missionVersion = getMissionVersion();
 	final Mission mission = missionVersion.getMission();
 	return super.isAvailableForEdit() || mission.isTerminatedWithChanges();
     }
 
     @Override
     protected boolean canAutoArchive() {
 	return false;
     }
 
 }
