 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.common;
 
 /**
  * A SOLAException should be thrown for SOLA specific exceptions within the SOLA
  * application. e.g. the results of a failed validation and/or to notify the
  * user of data or actions they must provide or undertake before the web method
  * can be completed.
  * <p>
  * The message passed to the exception should match a message code in the messaging utility.
  * Message parameters can also be set as required. 
  * </p> <p>
  * Note that SOLAException is converted to a SOLAFault by the FaultUtility when
  * using ProcessException. SOLAException it is intended to simplify fault creation
  * in general SOLA code.
  *</p>
  * This  is an unchecked Exception
  * @author soladev
  */
 public class SOLAException extends RuntimeException {
 
     private Object[] messageParameters;
 
     public Object[] getMessageParameters() {
         return messageParameters;
     }
 
    public void setMessageParameters(Object[] messageParameters) {
        this.messageParameters = messageParameters;
     }
 
     public SOLAException(String messageCode) {
         super(messageCode);
     }
 
    public SOLAException(String messageCode, Object[] messageParameters) {
         super(messageCode);
        this.messageParameters = messageParameters;
     }
 
     public SOLAException(String messageCode, Throwable cause) {
         super(messageCode, cause);
     }
 
    public SOLAException(String messageCode, Object[] messageParameters, Throwable cause) {
         super(messageCode, cause);
        this.messageParameters = messageParameters;
     }
 
     @Override
     public String toString() {
         String result = super.toString();
         if (messageParameters != null) {
             int idx = 1;
             for (Object obj : messageParameters) {
                 if (obj != null) {
                     result = result + ", Param" + idx + "=" + obj.toString();
                 } else {
                     result = result + ", Param" + idx + "=null";
                 }
                 idx++;
             }
         }
         return result;
     }
 }
