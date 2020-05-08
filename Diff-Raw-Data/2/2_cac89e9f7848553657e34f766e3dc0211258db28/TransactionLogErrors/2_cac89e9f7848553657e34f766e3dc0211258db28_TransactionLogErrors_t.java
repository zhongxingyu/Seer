 package com.ingenuity.icg.app.util;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pschmidt
  * Date: 5/3/12
  * Time: 2:43 PM
  * <p/>
  * Copyright (C) 2010 Ingenuity Systems, Inc. All rights reserved.
  * <p/>
  * This software is the confidential & proprietary information of Ingenuity Systems, Inc.
  * ("Confidential Information").
  * You shall not disclose such Confidential Information and shall use it only in
  * accordance with the terms of any agreement or agreements you entered into with
  * Ingenuity Systems.
  */
 public enum TransactionLogErrors {
     COUPON_NO_ERROR("no errors found") ,
     COUPON_NOT_FOUND("Sorry, this code is not valid"),
    COUPON_UNKNOWN_ERROR("Unknown error - possible network or database connection problem");
 
     private String name;
 
     TransactionLogErrors(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     @Override
     public String toString() {
         return name;
     }
 }
