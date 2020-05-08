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
 
 package ru.altruix.commons.api.debugids;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * @author DP118M
  * 
  */
 public abstract class AbstractDebugIdKey<C extends Enum<C>> {
     private static final int HASH_CODE_MULTIPLIER = 3;
     private C module;
     private String key;
 
    public final void setModule(final C aModule) {
         this.module = aModule;
     }
 
    public final C getModule() {
         return this.module;
     }
 
    public final void setKey(final String aKey) {
         this.key = aKey;
     }
 
    public final String getKey() {
         return this.key;
     }
 
     @SuppressWarnings("rawtypes")
     @Override
    public final boolean equals(final Object aObject) {
         if (aObject == null) {
             return false;
         }
         if (aObject == this) {
             return true;
         }
         if (aObject.getClass() != getClass()) {
             return false;
         }
 
         final AbstractDebugIdKey rhs = (AbstractDebugIdKey) aObject;
         return new EqualsBuilder().append(module, rhs.module)
                 .append(key, rhs.key).isEquals();
     }
 
     @Override
    public final int hashCode() {
         return new HashCodeBuilder(1, HASH_CODE_MULTIPLIER).append(module)
                 .append(key)
                 .toHashCode();
     }
 }
