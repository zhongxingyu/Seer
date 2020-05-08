 /*******************************************************************************
  * Copyright (c) 2012 MASConsult Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.masconsult.bgbanking.utils;
 
 import java.text.NumberFormat;
 import java.util.Currency;
 
 import org.acra.ACRA;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class Convert {
 
     private final static String DEFAULT_CURRENCY_FORMAT = "%2$s %1$1.2f";
 
     public static float strToFloat(String text) {
        // remove leading and ending spaces
        text = text.trim().replace("\u00a0", "");
        for (int i = text.length() - 1; i >= 0; i--) {
            char charAt = text.charAt(i);
            if (charAt == ',' || charAt == '.') {
                text = text.substring(0, i).replaceAll("[ ,.]", "")
                        + '.'
                        + text.substring(i + 1).replace(" ", "");
                break;
            }
        }
        return Float.valueOf(text);
     }
 
     public static String formatIBAN(String string) {
         if (string == null) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         int i = 4;
         while (i < string.length()) {
             sb.append(string.substring(i - 4, i)).append(' ');
             i += 4;
         }
         sb.append(string.substring(i - 4));
         return sb.toString();
     }
 
     public static String formatCurrency(float value, String currency) {
         NumberFormat format = (NumberFormat) NumberFormat.getCurrencyInstance().clone();
         if (currency != null) {
             try {
                 format.setCurrency(Currency.getInstance(currency));
             } catch (IllegalArgumentException e) {
                 ACRA.getErrorReporter().handleSilentException(e);
                 EasyTracker.getTracker().trackException("currency " + currency, e, false);
                 return String.format(DEFAULT_CURRENCY_FORMAT, value, currency);
             }
         }
         return format.format(value);
     }
 }
