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
 
 /**
  * 
  */
 package de.aidger.utils.controlling;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import siena.SienaException;
 
 import de.aidger.model.Runtime;
 import de.aidger.model.models.CostUnit;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.view.UI;
 
 /**
  * This class is used to calculate all the available years, the months of a year
  * and the funds of a given year and month.
  * 
  * @author aidGer Team
  */
 public class ControllingHelper {
 
     /**
      * Initializes a new controlling helper.
      */
     public ControllingHelper() {
     }
 
     /**
      * Determines all the available years of all employments.
      * 
      * @return The years
      */
     public int[] getEmploymentYears() {
         ArrayList<Short> years = new ArrayList<Short>();
         try {
             List<Employment> employments = new Employment().getAll();
             for (Employment employment : employments) {
                 if (!years.contains(employment.getYear())) {
                     years.add(employment.getYear());
                 }
             }
         } catch (SienaException e) {
             UI.displayError(e.toString());
         }
         Collections.sort(years);
         int[] sortedYears = new int[years.size()];
         for (int i = 0; i < sortedYears.length; i++) {
             sortedYears[i] = years.get(i);
         }
         return sortedYears;
     }
 
     /**
      * Determines all the months of the employments of a given year.
      * 
      * @param year
      *            The year of which to get the months.
      * @return The months
      */
     public int[] getYearMonths(int year) {
         ArrayList<Integer> months = new ArrayList<Integer>();
         List<Employment> employments;
         try {
             employments = new Employment().getEmployments((short) year,
                 (byte) 1, (short) year, (byte) 12);
             for (Employment employment : employments) {
                 if (!months.contains((int) employment.getMonth())) {
                     months.add((int) employment.getMonth());
                 }
             }
         } catch (SienaException e) {
             UI.displayError(e.toString());
         }
         Collections.sort(months);
         int[] sortedMonths = new int[months.size()];
         for(int i = 0; i < sortedMonths.length; i++) {
             sortedMonths[i] = (int) months.get(i);
         }
         /*
          * Since the controlling reports always include all the months of a year
          * before the given month, all the months following the first month must
          * be included.
          */
         if (sortedMonths.length == 12 - sortedMonths[0] + 1) {
             return sortedMonths;
         } else {
             int[] fullSortedMonths = new int[12 - sortedMonths[0] + 1];
             for (int i = 0; i < fullSortedMonths.length; i++) {
                 fullSortedMonths[i] = sortedMonths[0] + i;
             }
             return fullSortedMonths;
         }
     }
 
     /**
      * Determines all the funds the employments of a given year and month
      * 
      * @param year
      *            The year of which to get the funds.
      * @param month
      *            The month of which to get the funds.
      * @return The funds.
      */
     public CostUnit[] getFunds(int year, int month) {
         ArrayList<CostUnit> costUnits = new ArrayList<CostUnit>();
         List<Employment> employments;
         try {
             /*
              * Since the controlling reports always include all the months
              * before the given one, the funds of the ones before it need to be
              * included.
              */
             employments = new Employment().getEmployments((short) year,
                 (byte) 1, (short) year, (byte) month);
             for (Employment employment : employments) {
                 CostUnit costUnit = (new CostUnit()).fromTokenDB(employment.getFunds());
                 if (!costUnits.contains(costUnit)) {
                     costUnits.add(costUnit);
                 }
             }
         } catch (SienaException e) {
             UI.displayError(e.toString());
         }
         Collections.sort(costUnits);
        return (CostUnit[]) costUnits.toArray();
     }
 
     /**
      * Gets all the years, in which financial categories exist.
      * 
      * @return A list of years, in which financial categories exist
      */
     public List<Integer> getFinancialYears() {
         List<Integer> years = new ArrayList<Integer>();
         List<FinancialCategory> financialCategories;
         try {
             financialCategories = new FinancialCategory().getAll();
             for (FinancialCategory financialCategory : financialCategories) {
                 if (!years.contains(financialCategory.getYear().intValue())) {
                     years.add(financialCategory.getYear().intValue());
                 }
             }
         } catch (SienaException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         Collections.sort(years);
         return years;
     }
 }
