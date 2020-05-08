 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.calculation.service;
 
 import com.amee.domain.APIVersion;
 import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.data.*;
 import com.amee.domain.path.InternalValue;
 import com.amee.domain.profile.ProfileItem;
 import com.amee.domain.sheet.Choices;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 
 @Service
 public class CalculationService implements BeanFactoryAware, Serializable {
 
     @Autowired
     private AlgorithmService algoService;
 
     private final Log log = LogFactory.getLog(getClass());
 
     // Set by Spring context. The BeanFactory used to retreive ProfileFinder and DataFinder instances.
     private BeanFactory beanFactory;
 
     /**
      * Calculate and set the CO2 amount for a ProfileItem.
      *
      * @param profileItem - the ProfileItem for which to calculate CO2 amount
      */
     public void calculate(ProfileItem profileItem) {
         if (!profileItem.supportsCalculation()) return;
 
         if (profileItem.isEnd()) {
             profileItem.updateAmount(CO2Amount.ZERO);
             return;
         }
 
         Map<String, Object> values = getValues(profileItem);
 
         //TODO - remove once we have resolved algo handling on undef values.
         BigDecimal amount;
         try {
             amount = calculate(algoService.getAlgorithm(profileItem.getItemDefinition()), values);
         } catch (Exception ex) {
             amount = Decimal.ZERO;
         }
 
         if (amount != null) {
             profileItem.updateAmount(new CO2Amount(amount));
         }
     }
 
     /**
      * Calculate and return the CO2 amount for a DataItem and a set of user specified values.
      *
      * Note: I am unsure if this is in active use (SM)
      *
      * @param dataItem - the DataItem for the calculation
      * @param userValueChoices - user supplied value choices
      * @param version - the APIVersion. This is used to determine the correct ItemValueDefinitions to load into the calculation
      * @return the calculated CO2 amount
      */
     public BigDecimal calculate(DataItem dataItem, Choices userValueChoices, APIVersion version) {
         Map<String, Object> values = getValues(dataItem, userValueChoices, version);
         //TODO - remove once we have resolved algo handling on undef values.
         BigDecimal amount;
         try {
             amount = calculate(algoService.getAlgorithm(dataItem.getItemDefinition()), values);
         } catch (Exception ex) {
             amount = Decimal.ZERO;
         }
         return amount;
     }
 
     /**
      * Calculate and return the CO2 amount for given the provided algorithm and input values.
      *
      * Intended to be used publically in test harnesses when passing the modified algorithm content and input values
      * for execution is desirable.
      *
      * @param algorithm
      * @param values
      * @return the algorithm result as a BigDecimal
      *
      */
     public BigDecimal calculate(Algorithm algorithm, Map<String, Object> values) {
         log.debug("calculate()");
 
         log.debug("calculate() - algorithm uid: " + algorithm.getUid());
         log.debug("calculate() - input values: " + values);
 
         log.debug("calculate() - starting calculation");
         CO2Amount amount = new CO2Amount(algoService.evaluate(algorithm, values));
 
         log.debug("calculate() - finished calculation");
         log.debug("calculate() - CO2 Amount: " + amount);
 
         return amount.getValue();
     }
 
     // Collect all relevant algorithm input values for a ProfileItem calculation.
     public Map<String, Object> getValues(ProfileItem profileItem) {
 
         Map<ItemValueDefinition, InternalValue> values = new HashMap<ItemValueDefinition, InternalValue>();
         profileItem.getItemDefinition().appendInternalValues(values, profileItem.getProfile().getAPIVersion());
         profileItem.getDataItem().appendInternalValues(values);
         profileItem.appendInternalValues(values);
 
         Map<String, Object> returnValues = new HashMap<String, Object>();
         for (ItemValueDefinition ivd : values.keySet()) {
             returnValues.put(ivd.getCannonicalPath(), values.get(ivd).getValue());
         }
 
         initAlgoFinders(profileItem, returnValues);
 
         return returnValues;
     }
 
     private void initAlgoFinders(ProfileItem profileItem, Map<String, Object> values) {
 
         ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
         profileFinder.setProfileItem(profileItem);
 
         DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
         dataFinder.setStartDate(profileItem.getStartDate());
         dataFinder.setEndDate(profileItem.getEndDate());
 
         profileFinder.setDataFinder(dataFinder);
 
         values.put("dataFinder", dataFinder);
         values.put("profileFinder", profileFinder);
     }
 
     // Collect all relevant algorithm input values for a DataItem + auth Choices calculation.
     public Map<String, Object> getValues(DataItem dataItem, Choices userValueChoices, APIVersion version) {
         Map<ItemValueDefinition, InternalValue> values = new HashMap<ItemValueDefinition, InternalValue>();
         dataItem.getItemDefinition().appendInternalValues(values, version);
         dataItem.appendInternalValues(values);
        appendUserValueChoices(dataItem.getItemDefinition(), userValueChoices, values, version);
 
         Map<String, Object> returnValues = new HashMap<String, Object>();
         for (ItemValueDefinition ivd : values.keySet()) {
             returnValues.put(ivd.getCannonicalPath(), values.get(ivd).getValue());
         }
 
         DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
         ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
         profileFinder.setDataFinder(dataFinder);
 
         returnValues.put("dataFinder", dataFinder);
         returnValues.put("profileFinder", profileFinder);
 
         return returnValues;
     }
 
    private void appendUserValueChoices(ItemDefinition itemDefinition, Choices userValueChoices,
                                        Map<ItemValueDefinition, InternalValue> values, APIVersion version) {
         if (userValueChoices != null) {
             Map<ItemValueDefinition, InternalValue> userChoices = new HashMap<ItemValueDefinition, InternalValue>();
            for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                // Add each submitted user Choice that is available in the ItemDefinition and for the user's APIVersion
                if (itemValueDefinition.isFromProfile() &&
                        userValueChoices.containsKey(itemValueDefinition.getPath()) &&
                        itemValueDefinition.isValidIn(version)) {
                     userChoices.put(itemValueDefinition,
                             new InternalValue(userValueChoices.get(itemValueDefinition.getPath()).getValue()));
                 }
             }
             values.putAll(userChoices);
         }
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 }
