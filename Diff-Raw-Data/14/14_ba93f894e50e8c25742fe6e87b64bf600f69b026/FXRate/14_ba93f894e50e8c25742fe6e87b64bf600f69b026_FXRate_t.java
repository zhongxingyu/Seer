 /* Copyright 2013 Shaolang Ai
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.ackage com.github.moolah;
  */
 package com.github.moolah;
 
 import java.math.BigDecimal;
 
 public class FXRate {
     public FXRate(CurrencyPair currencyPair, BigDecimal bidRate,
             BigDecimal askRate, int unit) {
         this.currencyPair = currencyPair;
         this.bidRate = bidRate;
         this.askRate = askRate;
         this.unit = unit;
     }
 
     public boolean equals(Object other) {
         if (other == null) return false;
         if (!(other instanceof FXRate)) return false;
 
         FXRate that = (FXRate) other;
 
         return currencyPair.equals(that.currencyPair)
            && bidRate.equals(that.bidRate)
            && askRate.equals(that.askRate)
             && unit == that.unit;
     }
 
     public int hashCode() {
         int result = 17;
 
         result = 13 * result + currencyPair.hashCode();
         result = 13 * result + bidRate.hashCode();
         result = 13 * result + askRate.hashCode();
         result = 13 * result + unit;
 
         return result;
     }
 
     public String toString() {
         return new StringBuilder(currencyPair.toString())
             .append(" rate: ")
             .append(bidRate.toString())
             .append("/")
             .append(askRate.toString())
             .append(" [unit: ")
             .append(unit)
             .append(":1]")
             .toString();
     }
 
     private final CurrencyPair currencyPair;
     private final BigDecimal bidRate;
     private final BigDecimal askRate;
     private final int unit;
 }
