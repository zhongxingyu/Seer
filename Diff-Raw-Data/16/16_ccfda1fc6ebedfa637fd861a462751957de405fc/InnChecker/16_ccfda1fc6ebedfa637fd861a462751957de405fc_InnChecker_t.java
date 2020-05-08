 /*
         Copyright 2013 Oleg Chirukhin
 
         Licensed under the Apache License, Version 2.0 (the "License");
         you may not use this file except in compliance with the License.
         You may obtain a copy of the License at
 
         http://www.apache.org/licenses/LICENSE-2.0
 
         Unless required by applicable law or agreed to in writing, software
         distributed under the License is distributed on an "AS IS" BASIS,
         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         See the License for the specific language governing permissions and
         limitations under the License.
 */
 
 package com.olegchir.utils.innchecker;
 
 import java.util.ArrayList;
 
 public class InnChecker {
    public static final Integer[] MULT_N1 = {7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
    public static final Integer[] MULT_N2 = {3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
    public static final Integer[] MULT_N =  {2, 4, 10, 3, 5, 9, 4, 6, 8};

     public static boolean checkInn(String innStr) {
         Boolean valid;
         Integer[] inn = stringToIntArray(innStr);
 
         Integer innSize = inn.length;
 
         switch (innSize) {
             case 12:
                Integer N1 = getChecksum(inn,MULT_N1);
                Integer N2 = getChecksum(inn,MULT_N2);
 
                 valid = (inn[inn.length-1].equals(N2) && inn[inn.length-2].equals(N1));
                 break;
             case 10:
                Integer N = getChecksum(inn,MULT_N);
                 valid = (inn[inn.length-1].equals(N));
                 break;
             default:
                 valid = false;
                 break;
         }
         return valid;
     }
 
     public static Integer[] stringToIntArray(String src) {
         char[] chars = src.toCharArray();
         ArrayList<Integer> digits = new ArrayList<Integer>();
         for (char aChar : chars) {
             digits.add(Character.getNumericValue(aChar));
         }
         return digits.toArray(new Integer[digits.size()]);
     }
 
     public static Integer getChecksum(Integer[] digits, Integer[] multipliers) {
         int checksum = 0;
         for (int i=0; i<multipliers.length; i++) {
             checksum+=(digits[i]*multipliers[i]);
         }
         return (checksum % 11) % 10;
     }
 }
