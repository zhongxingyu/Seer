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
 package fixengine.messages.fix42.ubs;
 
 import fixengine.messages.MsgTypeValue;
 
 public class MessageFactory extends fixengine.messages.fix42.DefaultMessageFactory {
     public MessageFactory() {
         message(MsgTypeValue.EXECUTION_REPORT, ExecutionReportMessage.class);
         message(MsgTypeValue.ORDER_CANCEL_REJECT, OrderCancelRejectMessage.class);
         message(MsgTypeValue.NEW_ORDER_SINGLE, NewOrderSingleMessage.class);
         message(MsgTypeValue.ORDER_CANCEL_REQUEST, OrderCancelRequestMessage.class);
         message(MsgTypeValue.ORDER_MODIFICATION_REQUEST, OrderModificationRequestMessage.class);
        message(MsgTypeValue.DONT_KNOW_TRADE, DontKnowTradeMessage.class);
     }
 
     @Override protected String getTagsPackage() {
         return "fixengine.tags.fix42.ubs";
     }
 }
