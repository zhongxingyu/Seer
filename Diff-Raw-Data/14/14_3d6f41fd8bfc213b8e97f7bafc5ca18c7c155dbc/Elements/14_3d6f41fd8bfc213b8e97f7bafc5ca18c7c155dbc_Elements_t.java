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
 
 import fast.elements.Elem;
 import fast.elements.Field;
 import fast.operators.CopyOp;
 import fast.soup.PacketType;
 import fast.types.AsciiString;
 import fast.types.UnsignedInt32;
 
 public class Elements {
   public static final Elem<String> TEMPLATE_IDENTIFIER = PacketType.ELEM;
   public static final Field<Long> TIME_SECONDS = new LongField("TimeSeconds");
   public static final Field<Long> TIME_MILLISECONDS = new LongField("TimeMilliseconds");
  public static final Field<String> MESSAGE_TYPE = new CharField("MessageType");
   public static final Field<String> SYMBOL_1 = new StringField("Symbol.1");
   public static final Field<String> SYMBOL_2 = new StringField("Symbol.2");
   public static final Field<String> ORDER_ID_1 = new StringField("OrderId.1");
   public static final Field<String> ORDER_ID_2 = new StringField("OrderId.2");
   public static final Field<String> ORDER_ID_3 = new StringField("OrderId.3");
   public static final Field<Long> ORDER_ID_4 = new LongField("OrderId.4");
   public static final Field<Long> PRICE_1 = new LongField("Price.1");
   public static final Field<String> PRICE_2 = new StringField("Price.2");
   public static final Field<Long> SHARES = new LongField("Shares");
   public static final Field<String> SIDE = new StringField("Side");
   public static final Field<String> EXECUTION_ID_1 = new StringField("ExecutionId.1");
   public static final Field<String> EXECUTION_ID_2 = new StringField("ExecutionId.2");
   public static final Field<String> EXECUTION_ID_3 = new StringField("ExecutionId.3");
   public static final Field<Long> EXECUTION_ID_4 = new LongField("ExecutionId.4");
   public static final Field<String> DISPLAY = new StringField("Display");
   public static final Field<Long> LONG_SHARES = new LongField("LongShares");
   public static final Field<String> LONG_PRICE_1 = new StringField("LongPrice.1");
   public static final Field<Long> LONG_PRICE_2 = new LongField("LongPrice.2");
   public static final Field<Long> LONG_PRICE_3 = new LongField("LongPrice.3");
   public static final Field<String> LONG_PRICE_4 = new StringField("LongPrice.4");
 
   private static class LongField extends Field<Long> {
     protected LongField(String name) {
       super(name, UnsignedInt32.TYPE, new CopyOp<Long>());
     }
   }
 
   private static class StringField extends Field<String> {
     protected StringField(String name) {
       super(name, AsciiString.TYPE, new CopyOp<String>());
     }
   }

  private static class CharField extends Field<String> {
    protected CharField(String name) {
      super(name, AsciiString.TYPE, new CopyOp<String>(), 1);
    }
  }
}
