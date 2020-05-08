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
 import static com.github.moolah.TestUtils.bigdec;
 import static com.github.moolah.TestUtils.pair;
 import static com.github.moolah.TestUtils.adheresToValueObjectContract;
 
 public class FXRateTest {
     @Test
     public void adheres_to_value_object_contract() {
         FXRate same = new FXRate(pair("JPY", "SGD"), bigdec("1.2900"),
                 bigdec("1.3000"), 100);
        // main differences lie in decimals.
        FXRate different = new FXRate(pair("JPY", "SGD"), bigdec("1.29"),
                bigdec("1.3"), 100);
 
         assertThat(new FXRate(pair("JPY", "SGD"), bigdec("1.2900"),
                     bigdec("1.3000"), 100),
                 adheresToValueObjectContract(same, different));
     }
 }
