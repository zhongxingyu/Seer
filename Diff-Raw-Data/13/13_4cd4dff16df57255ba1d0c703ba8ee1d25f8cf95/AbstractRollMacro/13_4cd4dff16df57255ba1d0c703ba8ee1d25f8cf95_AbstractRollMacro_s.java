 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.macro.impl;
 
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.macro.Macro;
 
 public abstract class AbstractRollMacro  implements Macro {
 
     protected void roll(String channel, String roll) {
         
         try {
            MapTool.addMessage(channel, MapTool.getPlayer().getName() + " olls on " + channel + ": " + roll);
         } catch (Exception e) {
             MapTool.addLocalMessage("Unknown roll '" + roll + "', use #d#+#");
         }
     }
 
     private static final Random RANDOM = new Random();
     private static final Pattern BASIC_ROLL = Pattern.compile("(\\d*)\\s*d\\s*(\\d*)(\\s*[\\+,\\-]\\s*\\d+)?");
     protected static int roll(String roll) {
         
         Matcher m = BASIC_ROLL.matcher(roll);
         if (!m.matches()) {
             throw new IllegalArgumentException();
         }
 
         int count = m.group(1) != null && m.group(1).length() > 0 ? Integer.parseInt(m.group(1)) : 1;
         int dice = Integer.parseInt(m.group(2));
         int modifier = 0;
         if (m.group(3) != null) {
             String modStr = m.group(3).replace('+', ' ');
             StringBuilder builder = new StringBuilder();
             for (int i = 0; i < modStr.length(); i++) {
                 char ch = modStr.charAt(i);
                 switch(ch) {
                 case '0': case '1': case '2': case '3': case '4': case '5': 
                 case '6': case '7': case '8': case '9': case '-':
                     builder.append(ch);
                 }
             }
             modifier = Integer.parseInt(builder.toString());
         }
         
         return roll(count, dice, modifier);
     }
 
     protected static int roll(int count, int dice, int modifier) {
         
         int result = 0;
         for (int i = 0; i < count; i++) {
             int roll = (int)(dice * RANDOM.nextFloat());
             result += roll + 1;
         }
         
         return result + modifier;
     }
 
 //    public static void main(String[] args) {
 //        
 //        for (int i = 0; i < 10; i++) {
 //            roll("d2 + 2");
 //        }
 //        
 //        for (int i = 0; i < 10; i++) {
 //            roll("2d4");
 //        }
 //        
 //        for (int i = 0; i < 10; i++) {
 //            roll("2d4+4");
 //        }
 //    }
 }
