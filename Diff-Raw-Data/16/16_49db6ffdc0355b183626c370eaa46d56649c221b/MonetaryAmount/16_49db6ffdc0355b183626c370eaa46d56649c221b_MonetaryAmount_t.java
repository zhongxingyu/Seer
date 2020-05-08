 /*
  *  Copyright 2013 - present Stephen Colebourne
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.joda.jsr.money;
 
 /**
  * A amount of money.
  * <p>
  * This interface represents a quantity of money, stored as a numeric amount
  * in a single {@link CurrencyUnit currency}.
  * <p>
  * The amount is represented as rational number, consisting of three parts.
  * The main part is the whole number of currency units, such as US dollars.
  * The secondary parts are the numerator and denominator of the fraction of the currency unit.
  * For example, '12 dollars and 25 cents' will have a whole number amount of
  * '12', a numerator of '25 and a denominator of '100'. A similar value would
  * be '12 and one quarter dollars' where the numerator would be '1 'and the
  * denominator '4'.
  * <p>
  * This approach allows up to 18 decimal digits as well as any other fraction
  * such as one third or two sevenths.
  * By representing a fraction, the intermediate step in calculations can be expressed.
  * <p>
  * It is intended that applications declare method signatures and variables
  * in terms of the concrete type implementing this interface, rather than
  * this interface.
  * 
  * <h4>Implementation specification</h4>
  * This interface places no restrictions on the mutability of implementations,
  * however immutability is strongly recommended.
  * Implementations should be serializable wherever possible.
  */
 public interface MonetaryAmount {
 
     /**
      * Gets the currency of the amount.
      * 
      * @return the currency, not null
      */
     CurrencyUnit getCurrency();
 
     /**
      * Gets the amount in terms of whole units of the currency.
      * <p>
      * An amount is defined to consist of an amount of whole currency units
      * plus a fraction of the unit. This method returns the amount of whole
      * units, such as the number of complete US dollars represented.
      * <p>
      * For example, the amount of '12 dollars and 25 cents' would return
      * 12 from this method, as there are 12 whole US dollars in the amount.
     * <p>
     * The sign of the whole amount must be the same as the sign of the
     * fraction numerator.
      * 
     * @return the whole part of the amount, not null
      */
     long getAmountWhole();
 
     /**
      * Gets the numerator of the fractional amount of the currency.
      * <p>
      * An amount is defined to consist of an amount of whole currency units
      * plus a fraction of the unit. This method returns the numerator of the
      * fraction of the whole currency unit.
      * <p>
      * For example, the amount of '12 dollars and 25 cents' would typically
      * return 25 from this method and 100 from the denominator method.
     * <p>
     * The sign of the numerator must be the same as the sign of the
     * whole amount part.
      * 
     * @return the numerator of the fraction part of the amount, not null
      */
     long getAmountFractionNumerator();
 
     /**
      * Gets the denominator of the fractional amount of the currency.
      * <p>
      * An amount is defined to consist of an amount of whole currency units
      * plus a fraction of the unit. This method returns the denominator of the
      * fraction of the whole currency unit.
      * <p>
      * For example, the amount of '12 dollars and 25 cents' would typically
      * return 100 from this method and 25 from the numerator method.
      * 
     * @return the denominator of the fraction part of the amount, not null
      */
     long getAmountFractionDenominator();
 
     /**
      * Queries this monetary amount for a value.
      * <p>
      * This queries this amount using the specified query strategy object. 
      * <p>
      * Implementations must ensure that no observable state is altered when
      * this read-only method is invoked.
      * 
      * @param <R> the type of the result
      * @param adjuster  the query to invoke, not null
      * @return the query result, null may be returned (defined by the query)
      */
     <R> R query(MonetaryQuery<R> query);
 
     /**
      * Returns an adjusted object of the same type as this object with the adjustment made.
      * <p>
      * This adjusts this monetary amount according to the rules of the specified adjuster.
      * A typical adjuster will change the amount and leave the currency unchanged.
      * A more complex adjuster might also change the currency.
      * <p>
      * Some example code indicating how and why this method is used:
      * <pre>
      *  money = money.with(amountMultipliedBy(2));
      *  date = date.with(amountRoundedToNearestWholeUnit());
      * </pre>
      * 
      * @param adjuster  the adjuster to use, not null
      * @return an object of the same type with the specified adjustment made, not null
      */
     MonetaryAmount with(MonetaryAdjuster adjuster);
 
 }
