 /*
  * @(#)MissionItem.java
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
 package module.mission.domain;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Set;
 
 import jvstm.cps.ConsistencyPredicate;
 import module.mission.domain.activity.DistributeItemCostsActivityInformation.MissionItemFinancerBean;
 import module.mission.domain.activity.DistributeItemCostsActivityInformation.MissionItemFinancerBeanCollection;
 import module.mission.domain.activity.ItemActivityInformation;
 import module.organization.domain.Person;
 import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.bennu.core.domain.util.Money;
 import pt.ist.bennu.core.util.BundleUtil;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public abstract class MissionItem extends MissionItem_Base {
 
     public static final Comparator<MissionItem> COMPARATOR_BY_OID = new Comparator<MissionItem>() {
 
         @Override
         public int compare(final MissionItem missionItem1, final MissionItem missionItem2) {
             return missionItem1.getExternalId().compareTo(missionItem2.getExternalId());
         }
 
     };
 
     public MissionItem() {
         super();
         setMissionSystem(MissionSystem.getInstance());
         new TemporaryMissionItemEntry(this);
     }
 
     public Mission getMission() {
         return getMissionVersion().getMission();
     }
 
     public boolean isTemporary() {
         return hasTemporaryMissionItemEntry();
     }
 
     @Override
     public void setMissionVersion(final MissionVersion missionVersion) {
         super.setMissionVersion(missionVersion);
         final TemporaryMissionItemEntry temporaryMissionItemEntry = getTemporaryMissionItemEntry();
         if (missionVersion != null && temporaryMissionItemEntry != null) {
             temporaryMissionItemEntry.delete();
         }
     }
 
     public void delete() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             missionItemFinancer.delete();
         }
         getPeopleSet().clear();
         removeMissionVersion();
         final TemporaryMissionItemEntry temporaryMissionItemEntry = getTemporaryMissionItemEntry();
         if (temporaryMissionItemEntry != null) {
             temporaryMissionItemEntry.delete();
         }
         removeMissionSystem();
         deleteDomainObject();
     }
 
     private static final String BUNDLE = "resources.MissionResources";
     private static final String KEY_PREFIX = "label.";
 
     public String getLocalizedName() {
         final String key = KEY_PREFIX + getClass().getName();
         return BundleUtil.getStringFromResourceBundle(BUNDLE, key);
     }
 
     public void setMissionItemFinancers(final MissionItemFinancerBeanCollection missionItemFinancerBeans) {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             final MissionFinancer missionFinancer = missionItemFinancer.getMissionFinancer();
             if (!missionItemFinancerBeans.containsWithValue(missionFinancer)) {
                 missionItemFinancer.delete();
             }
         }
         for (final MissionItemFinancerBean missionItemFinancerBean : missionItemFinancerBeans) {
             setMissionItemFinancer(missionItemFinancerBean);
         }
     }
 
     public void setMissionItemFinancer(final MissionItemFinancerBean missionItemFinancerBean) {
         final MissionFinancer missionFinancer = missionItemFinancerBean.getMissionFinancer();
         final Money amount = missionItemFinancerBean.getAmount();
         if (amount != null && amount.isPositive()) {
             final MissionItemFinancer missionItemFinancer = findOrCreateMissionItemFinancer(missionFinancer);
             missionItemFinancer.setAmount(amount);
         }
     }
 
     private MissionItemFinancer findOrCreateMissionItemFinancer(final MissionFinancer missionFinancer) {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             if (missionFinancer == missionItemFinancer.getMissionFinancer()) {
                 return missionItemFinancer;
             }
         }
         return missionFinancer.isProjectFinancer() ? new MissionItemProjectFinancer(this, missionFinancer) : new MissionItemFinancer(
                 this, missionFinancer);
     }
 
     public abstract String getItemDescription();
 
     public Money getPrevisionaryCosts() {
         return Money.ZERO;
     }
 
     public boolean isVehicleItem() {
         return false;
     }
 
     public Money getValue() {
         return Money.ZERO;
     }
 
     public boolean isPersonelExpenseItem() {
         return false;
     }
 
     public boolean isConsistent() {
         return true;
     }
 
     public void setInfo(final ItemActivityInformation itemActivityInformation) {
         final MissionProcess missionProcess = itemActivityInformation.getProcess();
         final Mission mission = missionProcess.getMission();
 
         setMissionVersion(mission.getMissionVersion());
 
         final Collection<Person> people = itemActivityInformation.getPeople();
         final Set<Person> participants = getPeopleSet();
         participants.addAll(people);
         participants.retainAll(people);
         if (mission.getParticipantesCount() == 1) {
             participants.addAll(mission.getParticipantesSet());
         }
     }
 
     public void distributeCosts(final MissionItemFinancerBeanCollection missionItemFinancerBeanCollection) {
         if (missionItemFinancerBeanCollection.size() == 1) {
             final MissionItemFinancerBean missionItemFinancerBean = missionItemFinancerBeanCollection.iterator().next();
             missionItemFinancerBean.setAmount(getValue());
         }
         setMissionItemFinancers(missionItemFinancerBeanCollection);
     }
 
     public boolean areAllCostsDistributed() {
         Money sum = Money.ZERO;
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             sum = sum.add(missionItemFinancer.getAmount());
         }
         return sum.equals(getValue());
     }
 
     public void hookAfterChanges() {
     }
 
     public void distributeCosts() {
         final Money money = getValue();
         final Mission mission = getMissionVersion().getMission();
         int numberFinancers = mission.getFinancerSet().size();
         final Money shareValues[] = money.allocate(numberFinancers);
         int i = 0;
         for (final MissionFinancer missionFinancer : mission.getFinancerSet()) {
             final MissionItemFinancer missionItemFinancer = findOrCreateMissionItemFinancer(missionFinancer);
             missionItemFinancer.setAmount(shareValues[i++]);
         }
     }
 
     public boolean requiresFundAllocation() {
         return true;
     }
 
     MissionItem createNewVersion(final MissionVersion missionVersion) {
         final MissionItem missionItem = createNewVersionInstance(missionVersion);
         missionItem.setMissionVersion(missionVersion);
         missionItem.getPeopleSet().addAll(getPeopleSet());
         setNewVersionInformation(missionItem);
         return missionItem;
     }
 
     protected abstract MissionItem createNewVersionInstance(final MissionVersion missionVersion);
 
     protected abstract void setNewVersionInformation(final MissionItem missionItem);
 
     public boolean isAvailableForEdit() {
         final MissionVersion missionVersion = getMissionVersion();
         final Mission mission = missionVersion.getMission();
         final MissionProcess missionProcess = mission.getMissionProcess();
         return missionProcess.isUnderConstruction();
     }
 
     public void autoArchive() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             if (canAutoArchive()) {
                 missionItemFinancer.autoArchive();
             }
         }
     }
 
     protected boolean canAutoArchive() {
         return true;
     }
 
     public boolean isArchived() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             if (!missionItemFinancer.isArchived()) {
                 return false;
             }
         }
         return true;
     }
 
     public void archive() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             missionItemFinancer.archiveForAccountingUnit();
         }
     }
 
     public boolean isAccountantForUnArchivedMissionItemFinancer() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             if (missionItemFinancer.isAccountantForUnArchivedMissionItemFinancer()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isDirectAccountantForUnArchivedMissionItemFinancer() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             if (missionItemFinancer.isDirectAccountantForUnArchivedMissionItemFinancer()) {
                 return true;
             }
         }
         return false;
     }
 
     public void unArchive() {
         for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
             missionItemFinancer.unArchive();
         }
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
         return getMissionSystem() == VirtualHost.getVirtualHostForThread().getMissionSystem();
     }
 
     @ConsistencyPredicate
     public boolean checkIsTemporaryXorHasMissionVersion() {
         if (!isTemporary() && hasMissionVersion()) {
             return true;
         }
         if (isTemporary() && !hasMissionVersion()) {
             return true;
         }
         return false;
     }
 }
