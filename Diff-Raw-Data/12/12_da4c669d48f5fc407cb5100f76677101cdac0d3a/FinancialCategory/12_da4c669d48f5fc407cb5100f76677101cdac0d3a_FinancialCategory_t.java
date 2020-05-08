 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2013 The aidGer Team
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
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import siena.SienaException;
 import de.aidger.model.AbstractModel;
 import de.aidger.model.ObserverManager;
 import de.aidger.model.validators.ValidationException;
 
 /**
  * Represents a single entry in the financial category column of the database.
  * Contains functions to retrieve and change the data in the database.
  * 
  * @author aidGer Team
  */
 public class FinancialCategory extends AbstractModel<FinancialCategory> {
 
     /**
      * The name of the category.
      */
     private String name;
 
     /**
      * The year the category is valid.
      */
     private Short year;
 
     private ArrayList<InternalFinancialCategory> entries;
 
     /**
      * Initialize the FinancialCategory class.
      */
     public FinancialCategory() {
         if (getValidators().isEmpty()) {
             validatePresenceOf(new String[] { "name", "year", "costUnits",
                     "budgetCosts" }, new String[] { _("Name"), _("Year"),
                     _("Cost unit"), _("Budget Costs") });
         }
         entries = new ArrayList<InternalFinancialCategory>();
     }
 
     /**
      * Initialize the FinancialCategory class with the given financial category
      * model.
      * 
      * @param f
      *            the financial category model
      */
     public FinancialCategory(FinancialCategory f) {
         this();
         setId(f.getId());
         this.entries = f.entries;
         setName(f.getName());
         setYear(f.getYear());
     }
 
     /**
      * Clone the current category.
      */
     @Override
     public FinancialCategory clone() {
         FinancialCategory f = new FinancialCategory();
         f.setId(id);
         f.entries = this.entries;
         f.setName(name);
         f.setYear(year);
         return f;
     }
     
     @Override
     public void markAsNew() {
         for(InternalFinancialCategory entry : entries) {
             entry.markAsNew();
         }
     }
 
     /**
      * Custom validation function.
      * 
      * @return True if the validation is successful
      */
     public boolean validate() {
         boolean ret = true;
         
         if (year != null) {
 	        if (year <= 1000 || year >= 10000) {
 	            addError("year", _("Year"), _("is an incorrect year"));
 	            ret = false;
 	        }
         }
 
         if (this.getBudgetCosts() != null && this.getBudgetCosts().length > 0) {
 	        for (Integer b : this.getBudgetCosts()) {
 	            if(b == null) {
 	                addError("budgetCosts", _("Budget Costs"), _("is empty"));
 	                ret = false;
 	                break;
 	            }
 	            if (b < 0) {
 	                addError("budgetCosts", _("Budget Costs"), _("can't be less than zero"));
 	                ret = false;
 	                break;
 	            }
 	        }
         } else {
             addError("budgetCosts", _("Budget Costs"), _("is empty"));
             ret = false;
         }
         
         if (this.getCostUnits() != null && this.getCostUnits().length > 0) {
 	        for (Integer c : this.getCostUnits()) {
                 if(c == null) {
                     addError("costUnits", _("Cost Units"), _("is empty"));
                     ret = false;
                     break;
                 }
 	        	if (Collections.frequency(Arrays.asList(this.getCostUnits()), c) > 1) {
 	        		addError("costUnits", _("Cost Units"), _("can't be the same"));
 	        		
 	        		ret = false;
 	        		break;
 	        	}
 	        }
         } else {
             addError("costUnits", _("Cost Units"), _("is empty"));
             ret = false;
         }
 
         return ret;
     }
 
     /**
      * Custom validation function for remove().
      * 
      * @return True if everything is correct
      */
     public boolean validateOnRemove() {
         boolean ret = true;
 
         List<Course> courses = (new Course()).getCourses(this);
 
         if (courses.size() > 0) {
             addError(_("Financial Category is still linked to a Course."));
             ret = false;
         }
 
         return ret;
     }
 
     /**
      * Get a list of distinct funds.
      * 
      * @return List of distinct cost units
      */
     public List<Integer> getDistinctCostUnits() {
         // TODO: Make sure that distinct cost units mean
         // "SELECT DISTINCT Kostenstelle from finanzkategorie"
         List<Integer> costUnits = new ArrayList<Integer>();
         List<InternalFinancialCategory> allEntries = new InternalFinancialCategory()
                 .getAll();
         for (InternalFinancialCategory fc : allEntries) {
             if (!costUnits.contains(fc.getCostUnit())) {
                 costUnits.add(fc.getCostUnit());
             }
         }
         return costUnits;
     }
 
     /**
      * Get the budget costs of the category.
      * 
      * @return The budget costs of the category
      */
     public Integer[] getBudgetCosts() {
         Integer[] costs = new Integer[entries.size()];
         for (int i = 0; i < entries.size(); i++) {
             costs[i] = entries.get(i).getBudgetCost();
         }
         return costs;
     }
 
     /**
      * Get the funds of the category.
      * 
      * @return The funds of the category
      */
     public Integer[] getCostUnits() {
         Integer[] costUnits = new Integer[entries.size()];
         for (int i = 0; i < entries.size(); i++) {
             costUnits[i] = entries.get(i).getCostUnit();
         }
         return costUnits;
     }
 
     /**
      * Get the name of the category.
      * 
      * @return The name of the category
      */
     public String getName() {
         return name;
     }
 
     /**
      * Get the year the category is valid.
      * 
      * @return The year the category is valid
      */
     public Short getYear() {
         return year;
     }
 
     /**
      * Set the budget costs of the category.
      * 
      * @param costs
      *            The budget costs of the category
      */
     public void setBudgetCosts(Integer[] costs) {
         if(costs == null) 
             return;
        for (int i = 0; i < costs.length; i++) {
            if (entries.size() >= i + 1) {
                InternalFinancialCategory entry = entries.get(i);
                entry.setBudgetCost(costs[i]);
             } else {
                InternalFinancialCategory entry = newEntry();
                entry.setBudgetCost(costs[i]);
                entries.add(entry);
             }
         }
     }
 
     /**
      * Set the funds of the category.
      * 
      * @param costUnits
      *            The cost units of the category
      */
     public void setCostUnits(Integer[] costUnits) {
         if(costUnits == null)
             return;
         for (int i = 0; i < costUnits.length; i++) {
             if (entries.size() >= i + 1) {
                 InternalFinancialCategory entry = entries.get(i);
                 entry.setCostUnit(costUnits[i]);
             } else {
                 InternalFinancialCategory entry = newEntry();
                 entry.setCostUnit(costUnits[i]);
                 entries.add(entry);
             }
         }
         for (int i = costUnits.length; i < entries.size(); i++) {
             entries.get(i).remove();
             entries.remove(i);
         }
     }
 
     @Override
     public void setId(Long id) {
         this.id = id;
         for (InternalFinancialCategory entry : entries) {
             entry.setGroup(id);
         }
     }
 
     /**
      * Set the name of the category.
      * 
      * @param name
      *            The name of the category
      */
     public void setName(String name) {
         this.name = name;
         for (InternalFinancialCategory entry : entries) {
             entry.setName(name);
         }
     }
 
     /**
      * Set the year the category is valid.
      * 
      * @param year
      *            The year the category is valid.
      */
     public void setYear(Short year) {
         this.year = year;
         for (InternalFinancialCategory entry : entries) {
             entry.setYear(year);
         }
     }
 
     private InternalFinancialCategory newEntry() {
         InternalFinancialCategory entry = new InternalFinancialCategory();
         entry.setGroup(this.id);
         entry.setName(this.name);
         entry.setYear(this.year);
         return entry;
     }
 
     public void save() {
         /* Validation of the model */
         if (!doValidate()) {
             throw new ValidationException(_("Validation failed."));
         } else if (!errors.isEmpty()) {
             throw new ValidationException(
                     _("The model was not saved because the error list is not empty."));
         }
 
         // TODO: implement als transaction to ensure no duplicate groups
         if (id == null) {
             List<InternalFinancialCategory> temp = new InternalFinancialCategory()
                     .all().order("-group").fetch();
             if (temp == null || temp.size() == 0) {
                 this.setId((long) 0);
             } else {
                 this.setId(temp.get(0).getGroup() + 1);
             }
         }
 
         for (InternalFinancialCategory entry : entries) {
             entry.save();
         }
 
         /* Observable calls */
         ObserverManager.getInstance().triggerSave(this);
     }
 
     public void remove() {
         /* Check if there is a custom validation function */
         try {
             java.lang.reflect.Method m = getClass().getDeclaredMethod(
                     "validateOnRemove");
             if (!(Boolean) m.invoke(this, new Object[0])) {
                 throw new ValidationException();
             }
         } catch (NoSuchMethodException x) {
         } catch (IllegalAccessException x) {
         } catch (InvocationTargetException x) {
         }
         for (InternalFinancialCategory entry : entries) {
             entry.remove();
         }
 
         /* Observable calls */
         ObserverManager.getInstance().triggerRemove(this);
     }
 
     @Override
     public List<FinancialCategory> getAll() {
         List<InternalFinancialCategory> allEntries = new InternalFinancialCategory()
                 .getAll();
         HashMap<Long, FinancialCategory> fcMap = new HashMap<Long, FinancialCategory>();
         for (InternalFinancialCategory entry : allEntries) {
             if (fcMap.containsKey(entry.getGroup())) {
                 fcMap.get(entry.getGroup()).entries.add(entry);
             } else {
                 FinancialCategory fc = new FinancialCategory();
                 fc.setId(entry.getGroup());
                 fc.setName(entry.getName());
                 fc.setYear(entry.getYear());
                 fc.entries.add(entry);
                 fcMap.put(fc.getId(), fc);
             }
         }
         return new ArrayList<FinancialCategory>(fcMap.values());
     }
 
     @Override
     public FinancialCategory getById(Long id) {
         for (FinancialCategory fc : this.getAll()) {
             if (fc.getId() == id) {
                 return fc;
             }
         }
         return null;
     }
 
     @Override
     public FinancialCategory getByKey(Object id) {
         List<FinancialCategory> categories = this.getAll();
         if (categories.isEmpty())
             return null;
         for (FinancialCategory fc : categories) {
             if (id == null ? false : id.getClass() == Integer.class
                     || id.getClass() == Long.class) {
                 if (fc.getId() == id)
                     return fc;
             }
         }
         return null;
     }
 
     @Override
     public int size() {
         return this.getAll().size();
     }
 
     @Override
     public void clearTable() {
         try {
             InternalFinancialCategory entry = new InternalFinancialCategory();
             entry.all().delete();
             this.setId(null);
         } catch (SienaException x) {
             if (!x.getMessage().equals("No updated rows")
                     && !x.getMessage().endsWith("rows deleted"))
                 throw x;
         }
     }
 
 }
