/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package vio.service.rest;
 
 import java.util.Arrays;
 
 /**
  *
  * @author moroz
  */
 public class AbstractRS {
 
     public static final int DEF_RESULTS_COUNT = 15;
     public static final int[] DEF_RESULTS_RANGE = {0, DEF_RESULTS_COUNT};
 
     public int[] getRangeFromHeader(String header) {
         if (header == null) {
             return Arrays.copyOf(DEF_RESULTS_RANGE, DEF_RESULTS_RANGE.length);
         }
         String[] strs = header.split("-");
         int[] result = new int[2];
         try {
             result[0] = Integer.parseInt(strs[0]);
             result[1] = Integer.parseInt(strs[1]);
             if (checkRange(result)) {
                 return result;
             } else {
                 return Arrays.copyOf(DEF_RESULTS_RANGE, DEF_RESULTS_RANGE.length);
             }
         } catch (NumberFormatException e) {
             return Arrays.copyOf(DEF_RESULTS_RANGE, DEF_RESULTS_RANGE.length);
         }
 
     }
 
     private boolean checkRange(int[] range) {
         return (range.length != 2) || (range[0] < 0) || (range[0] >= range[1]) ? false : true;
     }
 
     public String buildContentRangeHeaderValue(int[] range) {
         return range[0] + "-" + range[1];
     }
 }
