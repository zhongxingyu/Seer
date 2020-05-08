 package com.coacheller.server.domain;
 
 import java.util.EnumSet;
 
 public enum DayEnum {
  FRIDAY("Friday"), SATURDAY("Saturday"), SUNDAY("Sunday");
 
   private String value;
 
   private DayEnum(String value) {
     this.value = value;
   }
 
   public String getValue() {
     return value;
   }
 
   public static DayEnum fromValue(String value) {
     for (final DayEnum element : EnumSet.allOf(DayEnum.class)) {
       if (element.getValue().equals(value)) {
         return element;
       }
     }
     throw new IllegalArgumentException("Cannot be parsed into an enum element : '" + value + "'");
   }
 }
