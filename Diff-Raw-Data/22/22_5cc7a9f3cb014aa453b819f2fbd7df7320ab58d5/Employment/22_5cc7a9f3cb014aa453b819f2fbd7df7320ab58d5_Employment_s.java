 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.model.models;
 
 import static de.aidger.utils.Translation._;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import de.aidger.model.AbstractModel;
 import de.unistuttgart.iste.se.adohive.controller.IEmploymentManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IEmployment;
 
 /**
  * Represents a single entry in the employment column of the database. Contains
  * functions to retrieve and change the data in the database.
  * 
  * @author aidGer Team
  */
 public class Employment extends AbstractModel<IEmployment> implements
         IEmployment {
 
     /**
      * References the corresponding assistant.
      */
     private Integer assistantId;
 
     /**
      * References the corresponding contract.
      */
     private Integer contractId;
 
     /**
      * References the corresponding course.
      */
     private Integer courseId;
 
     /**
      * The funds of the employment.
      */
     private String funds;
 
     /**
      * The month in which the employment took place.
      */
     private Byte month;
 
     /**
      * The year in which the employment took place.
      */
     private Short year;
 
     /**
      * The hours worked during the employment.
      */
     private Double hourCount;
 
     /**
      * The cost unit given by the university.
      */
     private Integer costUnit;
 
     /**
      * The qualification of the employment.
      */
     private String qualification;
 
     /**
      * Remarks regarding the employment.
      */
     private String remark;
 
     /**
      * Initializes the Employment class.
      */
     public Employment() {
         if (getValidators().isEmpty()) {
            validatePresenceOf(new String[] { "funds", "qualification" },
                new String[] { _("Funds"), _("Qualification") });
             validateInclusionOf(new String[] { "qualification" },
                 new String[] { _("Qualification") }, new String[] { "g", "u", "b" });
             validateExistanceOf(new String[] { "assistantId" },
                 new String[] { _("Assistant") }, new Assistant());
             validateExistanceOf(new String[] { "contractId" },
                 new String[] { _("Contract") }, new Contract());
             validateExistanceOf(new String[] { "courseId" },
                 new String[] { _("Course") }, new Course());        
         }
     }
 
     /**
      * Initializes the Employment class with the given employment model.
      * 
      * @param e
      *            the employment model
      */
     public Employment(IEmployment e) {
         this();
         setId(e.getId());
         setAssistantId(e.getAssistantId());
         setContractId(e.getContractId());
         setCostUnit(e.getCostUnit());
         setCourseId(e.getCourseId());
         setFunds(e.getFunds());
         setHourCount(e.getHourCount());
         setMonth(e.getMonth());
         setQualification(e.getQualification());
         setRemark(e.getRemark());
         setYear(e.getYear());
         setNew(false);
     }
 
     /**
      * Clone the current employment.
      */
     @Override
     public Employment clone() {
         Employment e = new Employment();
         e.setId(id);
         e.setAssistantId(assistantId);
         e.setContractId(contractId);
         e.setCourseId(courseId);
         e.setCostUnit(costUnit);
         e.setFunds(funds);
         e.setHourCount(hourCount);
         e.setMonth(month);
         e.setQualification(qualification);
         e.setRemark(remark);
         e.setYear(year);
         e.doClone(this);
         return e;
     }
 
     /**
      * Check if two objects are equal.
      * 
      * @param o
      *            The other object
      * @return True if both are equal
      */
     @Override
     public boolean equals(Object o) {
         if (o instanceof Employment) {
             Employment e = (Employment) o;
             return (id == null ? e.id == null : id.equals(e.id))
                     && (assistantId == null ? e.assistantId == null
                             : assistantId.equals(e.assistantId))
                     && (contractId == null ? e.contractId == null : contractId
                         .equals(e.contractId))
                     && (funds == null ? e.funds == null : funds.equals(e.funds))
                     && (courseId == null ? e.courseId == null : courseId
                         .equals(e.courseId))
                     && (month == null ? e.month == null : month.equals(e.month))
                     && (year == null ? e.year == null : year.equals(e.year))
                     && (costUnit == null ? e.getCostUnit() == null : costUnit
                         .equals(e.getCostUnit()))
                     && (hourCount == null ? e.hourCount == null : hourCount
                         .equals(e.hourCount))
                     && (qualification == null ? e.getQualification() == null
                             : qualification.equals(e.getQualification()))
                     && (remark == null ? e.getRemark() == null : remark
                         .equals(e.getRemark()));
         } else {
             return false;
         }
     }
 
     /**
      * Generate a unique hashcode for this instance.
      * 
      * @return The hashcode
      */
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 29 * hash + (assistantId != null ? assistantId.hashCode() : 0);
         hash = 29 * hash + (contractId != null ? contractId.hashCode() : 0);
         hash = 29 * hash + (courseId != null ? courseId.hashCode() : 0);
         hash = 29 * hash + (funds != null ? funds.hashCode() : 0);
         hash = 29 * hash + (month != null ? month.hashCode() : 0);
         hash = 29 * hash + (year != null ? year.hashCode() : 0);
         hash = 29
                 * hash
                 + (int) (Double.doubleToLongBits(hourCount) ^ (Double
                     .doubleToLongBits(hourCount) >>> 32));
         hash = 29 * hash + (costUnit != null ? costUnit.hashCode() : 0);
         hash = 29 * hash
                 + (qualification != null ? qualification.hashCode() : 0);
         hash = 29 * hash + (remark != null ? remark.hashCode() : 0);
         return hash;
     }
 
     /**
      * Custom validation function
      * 
      * @return True if everything is correct
      */
     public boolean validate() {
         boolean ret = true;
         if (costUnit <= 0) {
             addError("costUnit", _("Cost unit"), _("has to be bigger than 0"));
             ret = false;
         }
         if (hourCount < 0) {
             addError("hourCount", _("Hour count"), _("has to be positive"));
             ret = false;
         }
         if (year <= 1000 || year >= 10000) {
             addError("year", _("Year"), _("is an incorrect year"));
             ret = false;
         }
         if (month <= 0 || month >= 13) {
             addError("month", _("Month"), _("is an incorrect month"));
             ret = false;
         }
         if (funds.length() > 10) {
             addError("funds", _("Funds"),
                 _("can't be longer than 10 characters"));
             ret = false;
         }
         return ret;
     }
 
     /**
      * Get all employments during the given time.
      * 
      * @param start
      *            The start date
      * @param end
      *            The end date
      * @return The employments during the given time
      * @throws AdoHiveException
      */
     public List<Employment> getEmployments(Date start, Date end)
             throws AdoHiveException {
         Calendar cal = Calendar.getInstance();
         cal.setTime(start);
         short startYear = (short) cal.get(Calendar.YEAR);
         byte startMonth = (byte) cal.get(Calendar.MONTH);
 
         cal.setTime(end);
         short endYear = (short) cal.get(Calendar.YEAR);
         byte endMonth = (byte) cal.get(Calendar.MONTH);
 
         return getEmployments(startYear, startMonth, endYear, endMonth);
     }
 
     /**
      * Get all employments during the given time.
      * 
      * @param startYear
      *            The start year
      * @param startMonth
      *            The start month
      * @param endYear
      *            The end year
      * @param endMonth
      *            The end month
      * @return The employments during the given time
      * @throws AdoHiveException
      */
     public List<Employment> getEmployments(short startYear, byte startMonth,
             short endYear, byte endMonth) throws AdoHiveException {
         IEmploymentManager mgr = (IEmploymentManager) getManager();
         return castList(mgr.getEmployments(startYear, startMonth, endYear,
             endMonth));
     }
 
     /**
      * Get all employments with the given contract.
      * 
      * @param contract
      *            The given contract
      * @return The employments with the given contract
      * @throws AdoHiveException
      */
     public List<Employment> getEmployments(Contract contract)
             throws AdoHiveException {
         IEmploymentManager mgr = (IEmploymentManager) getManager();
         return castList(mgr.getEmployments(contract));
     }
 
     /**
      * Get all employments of the given assistant.
      * 
      * @param assistant
      *            The given assistant
      * @return The employments of the given assistant
      * @throws AdoHiveException
      */
     public List<Employment> getEmployments(Assistant assistant)
             throws AdoHiveException {
         IEmploymentManager mgr = (IEmploymentManager) getManager();
         return castList(mgr.getEmployments(assistant));
     }
 
     /**
      * Get all employments for the given course.
      * 
      * @param course
      *            The given course
      * @return The employments for the given course
      * @throws AdoHiveException
      */
     public List<Employment> getEmployments(Course course)
             throws AdoHiveException {
         IEmploymentManager mgr = (IEmploymentManager) getManager();
         return castList(mgr.getEmployments(course));
     }
 
     /**
      * Get all employments in the given semester
      * 
      * @param semester
      *            The given semester
      * @return The employments in the given semester
      * @throws AdoHiveException
      */
     public List<Employment> getEmployments(String semester)
             throws AdoHiveException {
         IEmploymentManager mgr = (IEmploymentManager) getManager();
         return castList(mgr.getEmployments(semester));
     }
 
     /**
      * Get a list of distinct cost units.
      * 
      * @return A list of distinct cost units
      * @throws AdoHiveException
      */
     public List<Integer> getDistinctCostUnits() throws AdoHiveException {
         IEmploymentManager mgr = (IEmploymentManager) getManager();
         return mgr.getDistinctCostUnits();
     }
 
     /**
      * Get the id referencing the assistant.
      * 
      * @return The id of the assistant
      */
     @Override
     public Integer getAssistantId() {
         return assistantId;
     }
 
     /**
      * Get the id referencing the contract.
      * 
      * @return The id of the contract
      */
     @Override
     public Integer getContractId() {
         return contractId;
     }
 
     /**
      * Get the cost unit of the contract.
      * 
      * @return The cost unit of the contract
      */
     @Override
     public Integer getCostUnit() {
         return costUnit;
     }
 
     /**
      * Get the id referencing the course.
      * 
      * @return The id of the course
      */
     @Override
     public Integer getCourseId() {
         return courseId;
     }
 
     /**
      * Get the funds of the employment.
      * 
      * @return The funds of the employment
      */
     @Override
     public String getFunds() {
         return funds;
     }
 
     /**
      * Get the hours worked during the employment.
      * 
      * @return The hours worked during the employment
      */
     @Override
     public Double getHourCount() {
         return hourCount;
     }
 
     /**
      * Get the month of the employment.
      * 
      * @return The month of the employment
      */
     @Override
     public Byte getMonth() {
         return month;
     }
 
     /**
      * Get the qualification of the employment.
      * 
      * @return The qualification of the employment
      */
     @Override
     public String getQualification() {
         return qualification;
     }
 
     /**
      * Get the remarks regarding the employment.
      * 
      * @return The remarks regarding the employment
      */
     @Override
     public String getRemark() {
         return remark;
     }
 
     /**
      * Get the year of the employment.
      * 
      * @return The year of the employment
      */
     @Override
     public Short getYear() {
         return year;
     }
 
     /**
      * Set the id referencing the assistant.
      * 
      * @param id
      *            The id of the assistant
      */
     @Override
     public void setAssistantId(Integer id) {
         assistantId = id;
     }
 
     /**
      * Set the id referencing the contract.
      * 
      * @param id
      *            The id of the contract
      */
     @Override
     public void setContractId(Integer id) {
         contractId = id;
     }
 
     /**
      * Set the cost unit of the contract.
      * 
      * @param cost
      *            The cost unit of the contract
      */
     @Override
     public void setCostUnit(Integer cost) {
         costUnit = cost;
     }
 
     /**
      * Set the id referencing the course.
      * 
      * @param id
      *            The id of the course
      */
     @Override
     public void setCourseId(Integer id) {
         courseId = id;
     }
 
     /**
      * Set the funds of the employment.
      * 
      * @param funds
      *            The funds of the employment
      */
     @Override
     public void setFunds(String funds) {
         this.funds = funds;
     }
 
     /**
      * Set the hours worked during the employment.
      * 
      * @param hours
      *            The hours worked during the employment
      */
     @Override
     public void setHourCount(Double hours) {
         hourCount = hours;
     }
 
     /**
      * Set the month of the employment.
      * 
      * @param month
      *            The month of the employment
      */
     @Override
     public void setMonth(Byte month) {
         this.month = month;
 
     }
 
     /**
      * Set the qualification of the employment.
      * 
      * @param quali
      *            The qualification of the employment
      */
     @Override
     public void setQualification(String quali) {
         qualification = quali;
     }
 
     /**
      * Set the remarks regarding the employment.
      * 
      * @param rem
      *            The remarks regarding the employment
      */
     @Override
     public void setRemark(String rem) {
         remark = rem;
     }
 
     /**
      * Set the year of the employment.
      * 
      * @param year
      *            The year of the employment
      */
     @Override
     public void setYear(Short year) {
         this.year = year;
     }
 
     /**
      * Cast the list from interface to real class.
      * 
      * @param list
      *            The list to cast
      * @return The new list
      */
     protected List<Employment> castList(List<IEmployment> list) {
         List<Employment> ret = new ArrayList<Employment>();
         for (IEmployment emp : list) {
             ret.add(new Employment(emp));
         }
         return ret;
     }
 
 }
