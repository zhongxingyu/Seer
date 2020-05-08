 /*******************************************************************************
  * Copyright (c) 2013 Zheng Sun.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Zheng Sun - initial API and implementation
  ******************************************************************************/
 
 package tv.huohua.peterson.misc;
 
 final public class JavaLangUtils {
     public static String implode(final int[] input, final String seperator) {
         final StringBuilder builder = new StringBuilder();
         if (input.length > 0) {
             builder.append(input[0]);
             for (int i = 1; i < input.length; i++) {
                 builder.append(seperator);
                 builder.append(input[i]);
             }
         }
         return builder.toString();
     }
    
     public static String implode(final Object[] input, final String seperator) {
         final StringBuilder builder = new StringBuilder();
         if (input.length > 0) {
             builder.append(input[0]);
             for (int i = 1; i < input.length; i++) {
                 builder.append(seperator);
                 builder.append(input[i]);
             }
         }
         return builder.toString();
     }
 }
