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
 import java.math.RoundingMode;
 import java.util.Arrays;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static com.github.moolah.TestUtils.bigdec;
 
 @RunWith(Parameterized.class)
 public class CurrencyPairConfigurationTest {
     @Parameters(name="{index}: converting to {4}")
     public static Iterable<Object[]> data() {
         return Arrays.asList(new Object[][] {
             {RoundingMode.HALF_UP, 2, 1,   1 / 6.0, bigdec("0.17")},
             {RoundingMode.DOWN,    2, 1,   1 / 6.0, bigdec("0.16")},
            {RoundingMode.HALF_UP, 4, 100, 1 / 6.0, bigdec("16.6667")},
            {RoundingMode.HALF_UP, 2, 100, 1 / 6.0, bigdec("16.67")},
            {RoundingMode.HALF_UP, 0, 100, 1 / 6.0, bigdec("17")}});
     }
 
     public CurrencyPairConfigurationTest(RoundingMode roundingMode,
             int scale, int unit, double toConvert, BigDecimal expected) {
         this.roundingMode = roundingMode;
         this.scale = scale;
         this.unit = unit;
         this.toConvert = toConvert;
         this.expected = expected;
     }
 
     @Test
     public void doubleToBigDecimal_converts_numbers_safely() {
         CurrencyPairConfiguration config = new CurrencyPairConfiguration(
                 roundingMode, scale, unit);
 
         assertThat(config.doubleToBigDecimal(toConvert),
                 is(equalTo(expected)));
     }
 
     private RoundingMode roundingMode;
     private int scale;
     private int unit;
     private double toConvert;
     private BigDecimal expected;
 }
