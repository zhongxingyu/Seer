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
 import org.junit.Test;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static com.github.moolah.ValueObjectContractMatcher.adheresToValueObjectContract;
 
 public class FXRateTest {
     @Test
     public void is_equal_to_another_with_same_properties() {
         assertThat(new FXRate(USDSGD, USDSGD_BID, USDSGD_ASK, USDSGD_UNIT),
                 is(equalTo(new FXRate(USDSGD, USDSGD_BID, USDSGD_ASK,
                             USDSGD_UNIT))));
     }
 
     @Test
     public void adheres_to_hashcode_contract_of_equal_objects() {
         assertThat(new FXRate(USDSGD, USDSGD_BID, USDSGD_ASK, USDSGD_UNIT).hashCode(),
                 is(equalTo(new FXRate(USDSGD, USDSGD_BID, USDSGD_ASK,
                             USDSGD_UNIT).hashCode())));
     }
 
     @Test
     public void adheres_to_value_object_contract() {
         FXRate same = new FXRate(USDSGD, USDSGD_BID, USDSGD_ASK,
                 USDSGD_UNIT);
         FXRate different = new FXRate(CurrencyPair.getInstance("EUR", "JPY"),
                 USDSGD_BID, USDSGD_ASK, USDSGD_UNIT);
 
         assertThat(new FXRate(USDSGD, USDSGD_BID, USDSGD_ASK, USDSGD_UNIT),
                adheresToValueObjectContract(same, different));
     }
 
     private final static CurrencyPair USDSGD =
         CurrencyPair.getInstance("USD", "SGD");
     private final static BigDecimal USDSGD_BID = new BigDecimal("1.2787");
     private final static BigDecimal USDSGD_ASK = new BigDecimal("1.2792");
     private final static int USDSGD_UNIT = 1;
 }
