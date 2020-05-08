 package com.amee.domain.item.profile;
 
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.ObjectType;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.item.BaseItem;
 import com.amee.domain.item.data.NuDataItem;
 import com.amee.domain.profile.CO2CalculationService;
 import com.amee.domain.profile.Profile;
 import com.amee.domain.profile.ProfileItem;
 import com.amee.platform.science.ReturnValues;
 import com.amee.platform.science.StartEndDate;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Index;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.annotation.Resource;
 import javax.persistence.*;
 import java.util.Date;
 
 @Entity
 @Table(name = "PROFILE_ITEM")
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 @Configurable(autowire = Autowire.BY_TYPE)
 public class NuProfileItem extends BaseItem {
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "PROFILE_ID")
     private Profile profile;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "DATA_ITEM_ID")
     private NuDataItem dataItem;
 
     @Column(name = "START_DATE")
     @Index(name = "START_DATE_IND")
     protected Date startDate = new Date();
 
     @Column(name = "END_DATE")
     @Index(name = "END_DATE_IND")
     protected Date endDate;
 
     @Transient
     private ReturnValues amounts = new ReturnValues();
 
     @Transient
     @Resource
     private CO2CalculationService calculationService;
 
     @Transient
     private transient ProfileItem adapter;
 
     public NuProfileItem() {
         super();
     }
 
     public NuProfileItem(Profile profile, NuDataItem dataItem) {
         super(dataItem.getDataCategory(), dataItem.getItemDefinition());
         setProfile(profile);
         setDataItem(dataItem);
     }
 
     public NuProfileItem(Profile profile, DataCategory dataCategory, NuDataItem dataItem) {
         super(dataCategory, dataItem.getItemDefinition());
         setProfile(profile);
         setDataItem(dataItem);
     }
 
     public NuProfileItem getCopy() {
         log.debug("getCopy()");
         NuProfileItem profileItem = new NuProfileItem();
         copyTo(profileItem);
         return profileItem;
     }
 
     protected void copyTo(NuProfileItem o) {
         super.copyTo(o);
         o.profile = profile;
         o.dataItem = dataItem;
         o.startDate = (startDate != null) ? (Date) startDate.clone() : null;
         o.endDate = (endDate != null) ? (Date) endDate.clone() : null;
         o.amounts = amounts;
         o.calculationService = calculationService;
     }
 
     @Override
     public boolean isTrash() {
         return status.equals(AMEEStatus.TRASH) || getDataItem().isTrash() || getProfile().isTrash();
     }
 
     /**
      * @return returns true if this Item supports CO2 amounts, otherwise false.
      */
     @Override
     public boolean supportsCalculation() {
         return !getItemDefinition().getAlgorithms().isEmpty();
     }
 
     @Override
     public String getPath() {
         return getUid();
     }
 
     public Profile getProfile() {
         return profile;
     }
 
     public void setProfile(Profile profile) {
         this.profile = profile;
     }
 
     public NuDataItem getDataItem() {
         return dataItem;
     }
 
     public void setDataItem(NuDataItem dataItem) {
         if (dataItem != null) {
             this.dataItem = dataItem;
         }
     }
 
     public StartEndDate getStartDate() {
         return new StartEndDate(startDate);
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public StartEndDate getEndDate() {
         if (endDate != null) {
             return new StartEndDate(endDate);
         } else {
             return null;
         }
     }
 
     public void setEndDate(Date endDate) {
         // May be null.
         this.endDate = endDate;
     }
 
     public boolean isEnd() {
         return (endDate != null) && (startDate.compareTo(endDate) == 0);
     }
 
     public boolean isWithinLifeTime(Date date) {
         return (date.equals(getStartDate()) || date.after(getStartDate())) &&
                 (getEndDate() == null || date.before(getEndDate()));
     }
 
     /**
      * Get the GHG {@link com.amee.platform.science.ReturnValues ReturnValues} for this ProfileItem.
      * <p/>
      * If the ProfileItem does not support calculations (i.e. metadata) an empty ReturnValues object is returned.
      *
      * @param recalculate force recalculation of the amounts. If false, only calculate amounts if amounts is empty.
      * @return - the {@link com.amee.platform.science.ReturnValues ReturnValues} for this ProfileItem
      */
     public ReturnValues getAmounts(boolean recalculate) {
         if (amounts.getReturnValues().isEmpty() || recalculate) {
             log.debug("getAmounts() - calculating amounts");
             calculationService.calculate(ProfileItem.getProfileItem(this));
         }
         return amounts;
     }
 
     /**
      * Get the GHG {@link com.amee.platform.science.ReturnValues ReturnValues} for this ProfileItem.
      * <p/>
      * If the ProfileItem does not support calculations (i.e. metadata) an empty ReturnValues object is returned.
      * <p/>
      * Note: this method only calculates the amounts if amounts is empty, ie, has not already been calculated.
      *
      * @return - the {@link com.amee.platform.science.ReturnValues ReturnValues} for this ProfileItem
      */
     public ReturnValues getAmounts() {
         return getAmounts(false);
     }
 
     /**
      * Returns the default GHG amount for this ProfileItem as a double.
      * This method is only included to provide backwards compatibility for existing Algorithms.
      *
      * @return the double value of the default GHG amount.
      */
     @Deprecated
     public double getAmount() {
         return getAmounts().defaultValueAsDouble();
     }
 
     public void setAmounts(ReturnValues amounts) {
         this.amounts = amounts;
     }
 
     /**
      * Set the effective start date for {@link com.amee.domain.data.LegacyItemValue} look-ups.
      *
      * @param effectiveStartDate - the effective start date for {@link com.amee.domain.data.LegacyItemValue} look-ups. If NULL or
      *                           before {@link com.amee.domain.data.LegacyItem#getStartDate()} this value is ignored.
      */
     public void setEffectiveStartDate(Date effectiveStartDate) {
         if ((effectiveStartDate != null) && effectiveStartDate.before(getStartDate())) {
             super.setEffectiveStartDate(null);
         } else {
             super.setEffectiveStartDate(effectiveStartDate);
         }
     }
 
     /**
      * Get the effective start date for {@link com.amee.domain.data.LegacyItemValue} look-ups.
      *
      * @return - the effective start date. If no date has been explicitly specified,
      *         then the Item startDate is returned.
      */
     public Date getEffectiveStartDate() {
         if (super.getEffectiveStartDate() != null) {
             return super.getEffectiveStartDate();
         } else {
             return getStartDate();
         }
     }
 
     /**
      * Set the effective end date for {@link com.amee.domain.data.LegacyItemValue} look-ups.
      *
      * @param effectiveEndDate - the effective end date for {@link com.amee.domain.data.LegacyItemValue} look-ups. If NULL or
      *                         after {@link com.amee.domain.data.LegacyItem#getEndDate()} (if set) this value is ignored.
      */
     public void setEffectiveEndDate(Date effectiveEndDate) {
         if ((getEndDate() != null) && (effectiveEndDate != null) && effectiveEndDate.after(getEndDate())) {
             super.setEffectiveEndDate(null);
         } else {
             super.setEffectiveEndDate(effectiveEndDate);
         }
     }
 
     /**
      * Get the effective end date for {@link com.amee.domain.data.LegacyItemValue} look-ups.
      *
      * @return - the effective end date. If no date has been explicitly specified,
      *         then the Item endDate is returned.
      */
     public Date getEffectiveEndDate() {
         if (super.getEffectiveEndDate() != null) {
            return getEffectiveEndDate();
         } else {
             return getEndDate();
         }
     }
 
     /**
      * Returns a Duration for the Item which is based on the startDate and endDate values. If there is no
      * endDate then null is returned.
      *
      * @return the Duration or null
      */
     public Duration getDuration() {
         if (getEndDate() != null) {
             return new Duration(getStartDate().getTime(), getEndDate().getTime());
         } else {
             return null;
         }
     }
 
     @Override
     public ObjectType getObjectType() {
         return ObjectType.NPI;
     }
 
     public ProfileItem getAdapter() {
         return adapter;
     }
 
     public void setAdapter(ProfileItem adapter) {
         this.adapter = adapter;
     }
 }
