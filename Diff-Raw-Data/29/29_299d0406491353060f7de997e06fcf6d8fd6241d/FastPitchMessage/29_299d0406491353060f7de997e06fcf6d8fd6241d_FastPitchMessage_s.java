 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fast.bats.europe;
 
 import fast.Message;
 import fast.templates.MessageTemplate;
 
 public class FastPitchMessage extends Message {
   private static final int LONG_BASE_36_LEN = 6;
 
   public FastPitchMessage(MessageTemplate template) {
     super(template);
   }
 
   public String getSymbol() {
     StringBuffer buf = new StringBuffer();
     buf.append(symbol1());
     buf.append(symbol2());
     return buf.toString().trim();
   }
 
   private String symbol1() {
     return get(Elements.SYMBOL_1);
   }
 
   private String symbol2() {
     return get(Elements.SYMBOL_2);
   }
 
   public String getSide() {
     return get(Elements.SIDE).trim();
   }
 
   public String getOrderId() {
     StringBuffer buf = new StringBuffer();
     buf.append(orderId1());
     buf.append(orderId2());
     buf.append(orderId3());
     buf.append(orderId4());
     return buf.toString().trim();
   }
 
   private String orderId1() {
     return get(Elements.ORDER_ID_1);
   }
 
   private String orderId2() {
     return get(Elements.ORDER_ID_2);
   }
 
   private String orderId3() {
     return get(Elements.ORDER_ID_3);
   }
 
   private String orderId4() {
     long orderId4 = get(Elements.ORDER_ID_4);
     return encodeFixedWidthBase36String(orderId4);
   }
 
   public String getPrice() {
    String price1 = Long.toString(get(Elements.PRICE_1));
     String price2 = get(Elements.PRICE_2);
    return removeLeadingAndTrailingZeroes(price1 + "." + price2).trim();
   }
 
   public String getLongPrice() {
     StringBuffer b = new StringBuffer("");
     b.append(longPrice1());
     b.append(longPrice2());
     b.append(".");
     b.append(longPrice3());
     b.append(longPrice4());
     return removeLeadingAndTrailingZeroes(b.toString()).trim();
   }
 
   private String longPrice1() {
     return String.format("%06d", Integer.valueOf(get(Elements.LONG_PRICE_1)));
   }
 
   private String longPrice2() {
     return String.format("%06d", get(Elements.LONG_PRICE_2));
   }
 
   private String longPrice3() {
     return String.format("%04d", get(Elements.LONG_PRICE_3));
   }
 
   private String longPrice4() {
     return get(Elements.LONG_PRICE_4);
   }
 
   public long getShares() {
     return get(Elements.SHARES);
   }
 
   private static String encodeFixedWidthBase36String(long value) {
     byte[] bytes = new byte[LONG_BASE_36_LEN];
     for (int i = bytes.length - 1; i >= 0; i--) {
       int curLeastSignificantBase36int = (int) (value % 36);
       bytes[i] = (byte) printableChar(curLeastSignificantBase36int);
       value /= 36;
     }
     return new String(bytes);
   }
 
   private static int printableChar(int base36int) {
     if (base36int > 9) {
       int characterIndex = base36int - 10;
       return 'A' + characterIndex;
     } else {
       int numberIndex = base36int;
       return '0' + numberIndex;
     }
   }
 
   private static String removeLeadingAndTrailingZeroes(String s) {
     return s.replaceFirst("^0*", "").replaceFirst("0+\\z", "");
   }
 }
