 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package ru.altruix.commons.api.fbprefixes;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author DP118M
  * 
  */
 public abstract class AbstractMessageCodePrefixRegistry<C extends Enum<C>> {
 
     private static final String PREFIX_MESSAGE_NUMBER_SEPARATOR = ".";
 
     private Map<C, String> prefixesByModules;
 
     public static String getMessageNumberSeparator() {
         return PREFIX_MESSAGE_NUMBER_SEPARATOR;
     }
 
    public String getPrefix(final C aModule) {
         return this.prefixesByModules.get(aModule)
                 + PREFIX_MESSAGE_NUMBER_SEPARATOR;
     }
 
     protected AbstractMessageCodePrefixRegistry() {
         this.prefixesByModules = new HashMap<C, String>();
         fillPrefixesByModules(this.prefixesByModules);
     }
 
     protected abstract void fillPrefixesByModules(
             final Map<C, String> aPrefixesByFunctionalBlocks);
 }
