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
 import java.util.Currency;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static com.github.moolah.TestUtils.adheresToValueObjectContract;
 import static com.github.moolah.TestUtils.sgd;
 import static com.github.moolah.TestUtils.usd;
 
 public class MoneyTest {
     @Test
     public void adheres_to_value_object_contract() {
         assertThat(new Money("USD", "100"),
                 adheresToValueObjectContract(new Money("USD", 100),
                     new Money("SGD", "100")));
     }
 
     /*
      * plus() tests
      */
 
     @Test
     public void allows_summing_monies_with_the_same_currency() {
         assertThat(usd("100").plus(usd("10.1"), usd("1.01")),
                 is(equalTo(usd("111.11"))));
     }
 
     @Test(expected=CurrencyMismatchException.class)
     public void throws_exception_when_trying_to_sum_monies_of_different_currency() {
         usd("100").plus(sgd("100"));
     }
 
     @Test
     public void plus_returns_itself_when_given_a_null() {
         assertThat(usd("200").plus((Money[]) null), is(equalTo(usd("200"))));
     }
 
     @Test
     public void plus_ignore_nulls() {
         assertThat(usd("100").plus(usd("10"), null, usd("1")),
                 is(equalTo(usd("111"))));
     }
 
     /*
      * minus() tests
      */
 
     @Test
     public void allows_minusing_monies_with_the_same_currency() {
         assertThat(usd("111.11").minus(usd("10.1"), usd("1.01")),
                 is(equalTo(usd("100.00"))));
     }
 
     @Test(expected=CurrencyMismatchException.class)
     public void throws_exception_when_trying_to_minus_monies_of_different_currency() {
         usd("100").minus(sgd("100"));
     }
 
     @Test
     public void minus_returns_itself_when_given_a_null() {
         assertThat(usd("99").minus((Money[]) null), is(equalTo(usd("99"))));
     }
 
     @Test
     public void minus_ignores_nulls() {
         assertThat(usd("99").minus(usd("10"), null, usd("1")),
                 is(equalTo(usd("88"))));
     }

    /*
     * times() tests
     */

    @Test
    public void times_returns_new_money_n_times_the_original() {
        assertThat(usd("100").times(2), is(equalTo(usd("200"))));
    }
 }
