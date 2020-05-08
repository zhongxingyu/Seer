 package com.interview.binary;
 
 import net.jcip.annotations.NotThreadSafe;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * <p/>
  * This problem asked by @see <a href="https://github.com/JingtingWang"> Jingting Wang</a> from
  * <a  href="https://www.stypi.com/xillltmv">stypi</a>
  * </p>
  * Do a binary bitAdd without any Java internal operations.  For example, "1010111" + "11110"
  * Giving you two strings, calculate the sum.  Return value is {@link String}.
  * Any invalid value will treate as "0".  For example,
  * <p/>
  * <pre>
  *     {@code
  *     Binary binary = new Binary("110").add("1").add("1101");
  *     }
  * </pre>
  */
 @NotThreadSafe
 public class Binary {
     public Binary(String input) {
         value = new StringBuilder(setDefaultValue(input));
     }
 
     private StringBuilder value;
 
    static final String HEAD_ONE = "1";

     public Binary add(String input) {
         CharSequence left = value;
         CharSequence right = setDefaultValue(input);
         StringBuilder sb = new StringBuilder();
         int carrier = 0;
         for (int i = left.length() - 1, j = right.length() - 1;
              i >= 0 || j >= 0;
              i--, j--) {
             int a = i < 0 ? 0 : left.charAt(i) == '1' ? 1 : 0;
             int b = j < 0 ? 0 : right.charAt(j) == '1' ? 1 : 0;
             int current = a ^ b ^ carrier;
             sb.insert(0, current);
             carrier = a == 1 ? (b | carrier) == 1 ? 1 : 0 : (b & carrier) == 1 ? 1 : 0;
         }
         if (carrier == 1)
             sb.insert(0, '1');
        value = sb.length() > 1 ? sb.delete(0, sb.indexOf(HEAD_ONE)) : sb;
         return this;
     }
 
     private CharSequence setDefaultValue(String input) {
         CharSequence charSequence;
         if (StringUtils.isNotEmpty(input))
             charSequence = input;
         else
             charSequence = "0";
         return charSequence;
     }
 
     @Override
     public String toString() {
         return value.toString();
     }
 }
